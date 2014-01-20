/* This program is free software: you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public License
 as published by the Free Software Foundation, either version 3 of
 the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>. */

package org.opentripplanner.routing.edgetype.factory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Stop;
import org.opentripplanner.common.geometry.DistanceLibrary;
import org.opentripplanner.common.geometry.SphericalDistanceLibrary;
import org.opentripplanner.common.model.P2;
import org.opentripplanner.common.pqueue.BinHeap;
import org.opentripplanner.routing.algorithm.NegativeWeightException;
import org.opentripplanner.routing.core.OptimizeType;
import org.opentripplanner.routing.core.RoutingRequest;
import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.core.TraverseMode;
import org.opentripplanner.routing.core.TraverseModeSet;
import org.opentripplanner.routing.edgetype.FrequencyBoard;
import org.opentripplanner.routing.edgetype.PatternEdge;
import org.opentripplanner.routing.edgetype.TransitBoardAlight;
import org.opentripplanner.routing.edgetype.TripPattern;
import org.opentripplanner.routing.graph.Edge;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.routing.graph.Vertex;
import org.opentripplanner.routing.impl.StreetVertexIndexServiceImpl;
import org.opentripplanner.routing.spt.BasicShortestPathTree;
import org.opentripplanner.routing.vertextype.TransitStop;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Coordinate;

public class LocalStopFinder {

    private final static Logger LOG = LoggerFactory.getLogger(LocalStopFinder.class);

    private static final double MAX_SUBOPTIMAL_DISTANCE = 100; /* allow a slop of ~10 seconds */

    private static final double LOCAL_STOP_SEARCH_RADIUS = 1000; /* how far to search for nearby stops */

    private HashSet<TripPattern> patterns;

    private Graph graph;

    private StreetVertexIndexServiceImpl indexService;

    private RoutingRequest walkingOptions;

    private RoutingRequest bikingOptions;

    private HashMap<Stop, HashMap<TripPattern, P2<Double>>> neighborhoods;

    private HashMap<AgencyAndId, TransitStop> transitStops;

    private DistanceLibrary distanceLibrary = SphericalDistanceLibrary.getInstance();
    
    public LocalStopFinder(StreetVertexIndexServiceImpl indexService, Graph graph) {
        this.graph = graph;
        this.indexService = indexService;
    }

    public void markLocalStops() {
        LOG.debug("Finding local stops");
        patterns = new HashSet<TripPattern>();
        transitStops = new HashMap<AgencyAndId, TransitStop>();
        int total = 0;
        for (Vertex gv : graph.getVertices()) {
            if (gv instanceof TransitStop) {
                TransitStop ts = (TransitStop) gv;
                ts.setLocal(true);
                transitStops.put(ts.getStopId(), ts);
                total ++;
            }
            for (Edge e : gv.getOutgoing()) {
                if (e instanceof TransitBoardAlight && ((TransitBoardAlight) e).isBoarding()) {
                        TripPattern pattern = ((TransitBoardAlight) e).getPattern();
                        patterns.add(pattern);
                }
                if (e instanceof FrequencyBoard) {
                    TripPattern pattern = ((FrequencyBoard) e).getPattern();
                    patterns.add(pattern);
                }
            }
        }

        // For each pattern, check if each stop is local

        neighborhoods = new HashMap<Stop, HashMap<TripPattern, P2<Double>>>();

        walkingOptions = new RoutingRequest(new TraverseModeSet(TraverseMode.WALK));
        bikingOptions = new RoutingRequest(new TraverseModeSet(TraverseMode.BICYCLE));
        bikingOptions.optimize = OptimizeType.SAFE;

        int nonLocal = 0;
        for (TripPattern pattern : patterns) {
            List<Stop> stops = getStops(pattern);
            // a stop is local if, in order to get to all other nearby patterns, it is
            // just as good to transfer at the previous stop.
            // so, each stop in the system needs a neighborhood of patterns.

            HashMap<TripPattern, P2<Double>> previousDistances = null;
            HashMap<TripPattern, P2<Double>> distances = null;
            for (int i = 0; i < stops.size() - 1; ++i) {
                Stop stop = stops.get(i);

                TransitStop transitStop = getVertexForStop(stop);

                previousDistances = distances;
                distances = getNeighborhood(stop);
                HashMap<TripPattern, P2<Double>> nextDistances = null;
                Stop nextStop = stops.get(i + 1);
                nextDistances = getNeighborhood(nextStop);

                if (previousDistances == null) {
                    // first stop is never local
                    if (transitStop.isLocal()) {
                        nonLocal ++;
                    }
                    transitStop.setLocal(false);
                    continue;
                } else {
                    boolean local = true;
                    for (Entry<TripPattern, P2<Double>> entry : distances.entrySet()) {
                        TripPattern key = entry.getKey();
                        if (key == pattern) {
                            continue;
                        }
                        P2<Double> distance = entry.getValue();
                        P2<Double> previousDistance = previousDistances.get(key);
                        P2<Double> nextDistance = nextDistances.get(key);
                        if (distance.getFirst() == 0) {
                            local = false;
                        } else if (previousDistance == null) {
                            if (nextDistance == null
                                    || nextDistance.getFirst() + MAX_SUBOPTIMAL_DISTANCE >= distance
                                            .getFirst()
                                    || nextDistance.getSecond() + MAX_SUBOPTIMAL_DISTANCE >= distance
                                            .getSecond()) {
                                local = false;
                                break;
                            }
                        } else if (distance.getFirst() <= previousDistance.getFirst()
                                + MAX_SUBOPTIMAL_DISTANCE
                                && (nextDistance == null || nextDistance.getFirst()
                                        + MAX_SUBOPTIMAL_DISTANCE >= distance.getFirst())) {
                            local = false;
                            break;
                        } else if (distance.getSecond() <= previousDistance.getSecond()
                                + MAX_SUBOPTIMAL_DISTANCE
                                && (nextDistance == null || nextDistance.getSecond()
                                        + MAX_SUBOPTIMAL_DISTANCE >= distance.getSecond())) {
                            local = false;
                            break;
                        }
                    }
                    if (local == false) {
                        if (transitStop.isLocal()) {
                            nonLocal ++;
                        }
                        transitStop.setLocal(false);
                    }
                }
            }
            // last stop is never local
            Stop stop = stops.get(stops.size() - 1);
            TransitStop transitStop = getVertexForStop(stop);
            if (transitStop.isLocal()) {
                nonLocal ++;
            }
            transitStop.setLocal(false);

        }
        LOG.debug("Local stops: " + (total - nonLocal) + " / " + total);
    }

    private HashMap<TripPattern, P2<Double>> getNeighborhood(Stop stop) {
        TransitStop transitStop = getVertexForStop(stop);
        HashMap<TripPattern, P2<Double>> neighborhood = neighborhoods.get(stop);
        if (neighborhood == null) {
            Set<TripPattern> nearbyPatterns = getNearbyPatterns(stop);
            HashMap<TripPattern, Double> walkNeighborhood = getBestDistanceForPatterns(graph, transitStop, walkingOptions, nearbyPatterns);
            HashMap<TripPattern, Double> bikeNeighborhood = getBestDistanceForPatterns(graph, transitStop, bikingOptions, nearbyPatterns);
            neighborhood = new HashMap<TripPattern, P2<Double>>();
            for (TripPattern p : nearbyPatterns) {
                Double walkDistance = walkNeighborhood.get(p);
                if (walkDistance == null) {
                    continue; /* if you can't walk there, there's no point */
                }
                Double bikeDistance = bikeNeighborhood.get(p);
                if (bikeDistance == null) {
                    bikeDistance = Double.MAX_VALUE; /* wrong, but will cause stop to not be marked as local on this pattern's account, which is probably right.*/
                }
                neighborhood.put(p, new P2<Double> (walkDistance, bikeDistance));
            }
            neighborhoods.put(stop, neighborhood);
        }
        return neighborhood;
    }

    /**
     * TODO - Any way this can use the existing search code?  AStar or Dijkstra
     * @param graph
     * @param origin
     * @param options
     * @param nearbyPatterns
     * @return
     */
    private HashMap<TripPattern, Double> getBestDistanceForPatterns(Graph graph, Vertex origin,
            RoutingRequest options, Set<TripPattern> nearbyPatterns) {

        // Iteration Variables
        HashSet<Vertex> closed = new HashSet<Vertex>();
        BinHeap<State> queue = new BinHeap<State>(50);
        BasicShortestPathTree spt = new BasicShortestPathTree(options);
        State initial = new State(origin, options);
        spt.add(initial);
        queue.insert(initial, 0);

        HashMap<TripPattern, Double> patternCosts = new HashMap<TripPattern, Double>();

        int patternsSeen = 0;

        while (!queue.empty()) { // Until the priority queue is empty:

            State u = queue.extract_min(); // get the lowest-weightSum Vertex 'u',
            
            if (! spt.visit(u)) 
            	continue;

            Vertex fromv = u.getVertex();

            closed.add(fromv);

            if (fromv instanceof TransitStop) {
                Vertex departureVertex = null;
                for (Edge e : fromv.getOutgoing()) {
                    // preboard edge, to departure vertex
                    departureVertex = e.getToVertex();
                    for (Edge e2 : departureVertex.getOutgoing()) {
                        if ((e2 instanceof TransitBoardAlight
                                && ((TransitBoardAlight) e2).isBoarding()) || e2 instanceof FrequencyBoard) {
                            /* finally, a boarding edge */
                            TripPattern pattern = ((PatternEdge) e2).getPattern();
                            if (nearbyPatterns.contains(pattern)) {
                                Double cost = patternCosts.get(pattern);
                                if (cost == null) {
                                    patternCosts.put(pattern, u.getWeight());
                                    patternsSeen++;
                                    if (patternsSeen == nearbyPatterns.size()) {
                                        return patternCosts;
                                    }
                                } else if (cost > u.getWeight()) {
                                    patternCosts.put(pattern, u.getWeight());
                                }
                            }
                        }
                    }
                }
            }
            
            if (distanceLibrary .fastDistance(fromv.getCoordinate(), origin.getCoordinate()) > LOCAL_STOP_SEARCH_RADIUS) {
                /* we have now traveled far from the origin, so we know that anything we find
                 * from here on out is going to be too far
                 */
                return patternCosts;
            }

            Iterable<Edge> outgoing = fromv.getOutgoing();

            for (Edge edge : outgoing) {

                State v = edge.traverse(u);

                // When an edge leads nowhere (as indicated by returning NULL), the iteration is
                // over.
                if (v == null)
                    continue;

                double dw = v.getWeight() - u.getWeight();
                if (dw < 0)
                    throw new NegativeWeightException(String.valueOf(dw));
                
                Vertex toVertex = v.getVertex();

                if (closed.contains(toVertex))
                    continue;

                if (spt.add(v))
                    queue.insert(v, v.getWeight());
            }
        }

        return patternCosts;
    }

    private TransitStop getVertexForStop(Stop stop) {
        return transitStops.get(stop.getId());
    }

    private HashSet<TripPattern> getNearbyPatterns(Stop stop) {
        // get all transit stops within about the LOCAL_STOP_SEARCH_RADIUS
        Coordinate c = new Coordinate(stop.getLon(), stop.getLat());
        List<Vertex> localTransitStops = indexService.getLocalTransitStops(c, LOCAL_STOP_SEARCH_RADIUS);

        HashSet<TripPattern> neighborhood = new HashSet<TripPattern>();
        for (Vertex v : localTransitStops) {
            if (v instanceof TransitStop) {
                if (((TransitStop) v).isEntrance()) {
                    // enter to get to actual stop
                    for (Edge e : v.getOutgoing()) {
                        v = e.getToVertex();
                        break;
                    }
                }
                for (Edge e : v.getOutgoing()) {
                    for (Edge e2 : e.getToVertex().getOutgoing()) {
                        if (e2 instanceof TransitBoardAlight && ((TransitBoardAlight) e2).isBoarding()) {
                            neighborhood.add(((TransitBoardAlight) e2).getPattern());
                        } else if (e2 instanceof FrequencyBoard) {
                            neighborhood.add(((FrequencyBoard) e2).getPattern());
                        }
                    }
                }
            }
        }
        return neighborhood;
    }

    private List<Stop> getStops(TripPattern pattern) {
        return pattern.getStops();
    }

}

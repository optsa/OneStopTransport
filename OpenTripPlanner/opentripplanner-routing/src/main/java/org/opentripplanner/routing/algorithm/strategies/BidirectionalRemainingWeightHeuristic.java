/* This program is free software: you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public License
 as published by the Free Software Foundation, either version 3 of
 the License, or (props, at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>. */

package org.opentripplanner.routing.algorithm.strategies;

import java.util.Arrays;

import org.opentripplanner.common.geometry.DistanceLibrary;
import org.opentripplanner.common.geometry.SphericalDistanceLibrary;
import org.opentripplanner.common.pqueue.BinHeap;
import org.opentripplanner.routing.core.RoutingRequest;
import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.graph.AbstractVertex;
import org.opentripplanner.routing.graph.Edge;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.routing.graph.Vertex;
import org.opentripplanner.routing.location.StreetLocation;
import org.opentripplanner.routing.services.RemainingWeightHeuristicFactory;
import org.opentripplanner.routing.vertextype.StreetVertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A heuristic that performs a single-source / all destinations shortest path search backward from the target of the main search, using lower bounds
 * on the weight of each edge.
 * 
 * @author andrewbyrd
 */
public class BidirectionalRemainingWeightHeuristic implements RemainingWeightHeuristic,
        RemainingTimeHeuristic {

    private static final long serialVersionUID = 20111002L;

    private static Logger LOG = LoggerFactory.getLogger(BidirectionalRemainingWeightHeuristic.class);

    Vertex target;

    double cutoff;

    double[] weights;

    int nVertices = 0;

    Graph g;

    private TransitLocalStreetService localStreetService;

    private DistanceLibrary distanceLibrary = SphericalDistanceLibrary.getInstance();

    /**
     * RemainingWeightHeuristic interface
     */

    public BidirectionalRemainingWeightHeuristic(Graph graph) {
        this.g = graph;
        this.localStreetService = g.getService(TransitLocalStreetService.class);
    }

    @Override
    public void initialize(State s, Vertex target) {
        recalculate(s.getVertex(), target, s.getOptions(), false);
    }

    @Override
    public double computeForwardWeight(State s, Vertex target) {
        return computeReverseWeight(s, target);
    }

    @Override
    public double computeReverseWeight(State s, Vertex target) {
        final Vertex v = s.getVertex();
        if (v instanceof StreetLocation)
            return 0;
        if (s.getWeight() < 10 * 60)
            return 0;
        int index = v.getIndex();
        if (index < weights.length) {
            double h = weights[index];
            if (s.isEverBoarded()) {
                if (localStreetService != null
                        && v instanceof StreetVertex
                        && s.getOptions().getMaxWalkDistance() - s.getWalkDistance() < distanceLibrary
                                .fastDistance(v.getCoordinate(), target.getCoordinate())
                        && !localStreetService.transferrable(v)) {
                    return Double.MAX_VALUE;
                }
                if (s.isAlightedLocal()
                        || (!s.isOnboard() && s.getNumBoardings() > s.getOptions()
                                .getMaxTransfers())) {
                    double d = distanceLibrary.fastDistance(v.getCoordinate(),
                            target.getCoordinate());
                    if (d > s.getOptions().getMaxWalkDistance() - s.getWalkDistance()) {
                        return Double.MAX_VALUE;
                    }
                    double walk = d / s.getOptions().getStreetSpeedUpperBound();
                    return Math.max(h, walk);
                }
            }
            // System.out.printf("h=%f at %s\n", h, s.getVertex());
            // return infinite heuristic values
            // so transit boarding is not even attempted useless patterns

            return h;
            // return h == Double.POSITIVE_INFINITY ? 0 : h;

        } else
            return 0;
    }

    private void recalculate(Vertex origin, Vertex target, RoutingRequest options,
            boolean timeNotWeight) {
        if (target != this.target || options.maxWeight > this.cutoff) {
            LOG.debug("recalc");
            this.target = target;
            this.cutoff = options.maxWeight;
            this.nVertices = AbstractVertex.getMaxIndex();
            weights = new double[nVertices];
            Arrays.fill(weights, Double.POSITIVE_INFINITY);
            BinHeap<Vertex> q = new BinHeap<Vertex>();
            long t0 = System.currentTimeMillis();
            if (target instanceof StreetLocation) {
                for (Edge de : ((StreetLocation) target).getExtra()) {
                    Vertex gv;
                    if (options.isArriveBy()) {
                        gv = de.getToVertex();
                    } else {
                        gv = de.getFromVertex();
                    }
                    int gvi = gv.getIndex();
                    if (gv == target)
                        continue;
                    if (gvi >= nVertices)
                        continue;
                    weights[gvi] = 0;
                    q.insert(gv, 0);
                }
            } else {
                int i = target.getIndex();
                weights[i] = 0;
                q.insert(target, 0);
            }
            while (!q.empty()) {
                double uw = q.peek_min_key();
                Vertex u = q.extract_min();
                // cutting off at 2-3 hours seems to improve reaction time
                // (this was previously not the case... #656?)
                // of course in production this would be scaled according to the distance
                if (uw > cutoff)
                    break;
                int ui = u.getIndex();
                if (uw > weights[ui])
                    continue;
                Iterable<Edge> edges;
                if (options.isArriveBy())
                    edges = u.getOutgoing();
                else
                    edges = u.getIncoming();
                for (Edge e : edges) {
                    Vertex v = options.isArriveBy() ? e.getToVertex() : e.getFromVertex();
                    double ew = timeNotWeight ? e.timeLowerBound(options) : e
                            .weightLowerBound(options);
                    if (ew < 0) {
                        LOG.error("negative edge weight {} qt {}", ew, e);
                        continue;
                    }
                    double vw = uw + ew;
                    int vi = v.getIndex();
                    if (weights[vi] > vw) {
                        weights[vi] = vw;
                        // selectively rekeying did not seem to offer any speed advantage
                        q.insert(v, vw);
                        // System.out.println("Insert " + v + " weight " + vw);
                    }
                }
            }
            LOG.info("End SSSP ({} msec)", System.currentTimeMillis() - t0);
        }
    }

    /**
     * RemainingTimeHeuristic interface
     */
    @Override
    public void timeInitialize(State s, Vertex target) {
        recalculate(s.getVertex(), target, s.getOptions(), true);
    }

    @Override
    public double timeLowerBound(State s) {
        return computeReverseWeight(s, null);
    }

    public static class Factory implements RemainingWeightHeuristicFactory {
        @Override
        public RemainingWeightHeuristic getInstanceForSearch(RoutingRequest opt) {
            if (opt.getModes().isTransit()) {
                LOG.debug("Transit itinerary requested.");
                return new BidirectionalRemainingWeightHeuristic (opt.rctx.graph);
            } else {
                LOG.debug("Non-transit itinerary requested.");
                return new DefaultRemainingWeightHeuristic();
            }   
        }
    }

    @Override
    public void reset() {}
    
    @Override
    public void abort() {}

}

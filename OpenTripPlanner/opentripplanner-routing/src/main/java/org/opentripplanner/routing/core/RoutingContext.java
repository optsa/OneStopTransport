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

package org.opentripplanner.routing.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.gtfs.services.calendar.CalendarService;
import org.opentripplanner.common.geometry.DistanceLibrary;
import org.opentripplanner.common.geometry.GeometryUtils;
import org.opentripplanner.common.geometry.SphericalDistanceLibrary;
import org.opentripplanner.common.model.GenericLocation;
import org.opentripplanner.routing.algorithm.strategies.RemainingWeightHeuristic;
import org.opentripplanner.routing.algorithm.strategies.TrivialRemainingWeightHeuristic;
import org.opentripplanner.routing.edgetype.PartialPlainStreetEdge;
import org.opentripplanner.routing.edgetype.PlainStreetEdge;
import org.opentripplanner.routing.edgetype.TimetableResolver;
import org.opentripplanner.routing.error.TransitTimesException;
import org.opentripplanner.routing.error.VertexNotFoundException;
import org.opentripplanner.routing.graph.Edge;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.routing.graph.Vertex;
import org.opentripplanner.routing.impl.DefaultRemainingWeightHeuristicFactoryImpl;
import org.opentripplanner.routing.location.StreetLocation;
import org.opentripplanner.routing.pathparser.PathParser;
import org.opentripplanner.routing.services.RemainingWeightHeuristicFactory;
import org.opentripplanner.routing.services.TransitIndexService;
import org.opentripplanner.routing.vertextype.StreetVertex;
import org.opentripplanner.routing.vertextype.TransitStop;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterables;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;

/**
 * A RoutingContext holds information needed to carry out a search for a particular TraverseOptions, on a specific graph. Includes things like
 * (temporary) endpoint vertices, transfer tables, service day caches, etc.
 * 
 * @author abyrd
 */
public class RoutingContext implements Cloneable {

    private static final Logger LOG = LoggerFactory.getLogger(RoutingContext.class);

    private static RemainingWeightHeuristicFactory heuristicFactory = new DefaultRemainingWeightHeuristicFactoryImpl();

    /* FINAL FIELDS */

    public RoutingRequest opt; // not final so we can reverse-clone

    public final Graph graph;

    public final Vertex fromVertex;

    public final Vertex toVertex;

    // origin means "where the initial state will be located" not "the beginning of the trip from the user's perspective"
    public final Vertex origin;

    // target means "where this search will terminate" not "the end of the trip from the user's perspective"
    public final Vertex target;
    
    // The back edge associated with the origin - i.e. continuing a previous search.
    // NOTE: not final so that it can be modified post-construction for testing.
    // TODO(flamholz): figure out a better way.
    public Edge originBackEdge;

    public final ArrayList<Vertex> intermediateVertices = new ArrayList<Vertex>();

    // public final Calendar calendar;
    public final CalendarService calendarService;

    public final Map<AgencyAndId, Set<ServiceDate>> serviceDatesByServiceId = new HashMap<AgencyAndId, Set<ServiceDate>>();

    public RemainingWeightHeuristic remainingWeightHeuristic;

    public final TransferTable transferTable;

    /** The timetableSnapshot is a {@link TimetableResolver} for looking up real-time updates. */
    public final TimetableResolver timetableSnapshot; 

    /**
     * Cache lists of which transit services run on which midnight-to-midnight periods. This ties a TraverseOptions to a particular start time for the
     * duration of a search so the same options cannot be used for multiple searches concurrently. To do so this cache would need to be moved into
     * StateData, with all that entails.
     */
    public ArrayList<ServiceDay> serviceDays;

    /**
     * The search will be aborted if it is still running after this time (in milliseconds since the epoch). A negative or zero value implies no limit.
     * This provides an absolute timeout, whereas the maxComputationTime is relative to the beginning of an individual search. While the two might
     * seem equivalent, we trigger search retries in various places where it is difficult to update relative timeout value. The earlier of the two
     * timeouts is applied.
     */
    public long searchAbortTime = 0;

    public PathParser[] pathParsers = new PathParser[] {};

    public Vertex startingStop;

    /* CONSTRUCTORS */

    /**
     * Constructor that automatically computes origin/target from RoutingRequest.
     */
    public RoutingContext(RoutingRequest routingRequest, Graph graph) {
        this(routingRequest, graph, null, null, true);
    }

    /**
     * Constructor that takes to/from vertices as input.
     */
    public RoutingContext(RoutingRequest routingRequest, Graph graph, Vertex from, Vertex to) {
        this(routingRequest, graph, from, to, false);
    }

    /**
     * Returns the PlainStreetEdges that overlap between two vertices edge sets.
     */
    private Set<PlainStreetEdge> overlappingPlainStreetEdges(Vertex u, Vertex v) {
        Set<Integer> vIds = new HashSet<Integer>();
        Set<Integer> uIds = new HashSet<Integer>();
        for (Edge e : Iterables.concat(v.getIncoming(), v.getOutgoing())) {
            vIds.add(e.getId());
        }
        for (Edge e : Iterables.concat(u.getIncoming(), u.getOutgoing())) {
            uIds.add(e.getId());
        }
        
        // Intesection of edge IDs between u and v.
        uIds.retainAll(vIds);
        Set<Integer> overlappingIds = uIds;

        // Fetch the edges by ID - important so we aren't stuck with temporary edges.
        Set<PlainStreetEdge> overlap = new HashSet<PlainStreetEdge>();
        for (Integer id : overlappingIds) {
            Edge e = graph.getEdgeById(id);
            if (e == null || !(e instanceof PlainStreetEdge)) {
                continue;
            }

            overlap.add((PlainStreetEdge) e);
        }
        return overlap;
    }

    /**
     * Creates a PartialPlainStreetEdge along the input PlainStreetEdge.
     * 
     * Orders the endpoints u, v so that they are consistent with the direction of the edge e.
     */
    private PartialPlainStreetEdge makePartialEdgeAlong(PlainStreetEdge e, StreetVertex u,
            StreetVertex v) {
        DistanceLibrary dLib = SphericalDistanceLibrary.getInstance();

        Vertex head = e.getFromVertex();
        double uDist = dLib.fastDistance(head.getCoordinate(), u.getCoordinate());
        double vDist = dLib.fastDistance(head.getCoordinate(), v.getCoordinate());

        // Order the vertices along the partial edge by distance from the head of the edge.
        // TODO(flamholz): this logic is insufficient for curvy streets/roundabouts.
        StreetVertex first = u;
        StreetVertex second = v;
        if (vDist < uDist) {
            first = v;
            second = u;
        }

        Geometry parentGeom = e.getGeometry();
        LineString myGeom = GeometryUtils.getInteriorSegment(parentGeom, first.getCoordinate(),
                second.getCoordinate());

        double lengthRatio = myGeom.getLength() / parentGeom.getLength();
        double length = e.getLength() * lengthRatio;

        String name = first.getLabel() + " to " + second.getLabel();
        return new PartialPlainStreetEdge(e, first, second, myGeom, name, length);
    }

    /**
     * Flexible constructor which may compute to/from vertices.
     * 
     * TODO(flamholz): delete this flexible constructor and move the logic to constructors above appropriately.
     * 
     * @param findPlaces if true, compute origin and target from RoutingRequest using spatial indices.
     */
    private RoutingContext(RoutingRequest routingRequest, Graph graph, Vertex from, Vertex to,
            boolean findPlaces) {
        this.opt = routingRequest;
        this.graph = graph;

        Edge fromBackEdge = null;
        Edge toBackEdge = null;
        if (findPlaces) {
            // normal mode, search for vertices based RoutingRequest
            if (!opt.batch || opt.arriveBy) {
                // non-batch mode, or arriveBy batch mode: we need a to vertex
                toVertex = graph.streetIndex.getVertexForLocation(opt.getTo(), opt);
                if (opt.getTo().hasEdgeId()) {
                    toBackEdge = graph.getEdgeById(opt.getTo().getEdgeId());
                }
            } else {
                toVertex = null;
            }
            if (!opt.batch || !opt.arriveBy) {
                // non-batch mode, or depart-after batch mode: we need a from vertex
                fromVertex = graph.streetIndex.getVertexForLocation(opt.getFrom(), opt, toVertex);
                if (opt.getFrom().hasEdgeId()) {
                    fromBackEdge = graph.getEdgeById(opt.getFrom().getEdgeId());
                }
            } else {
                fromVertex = null;
            }
            if (opt.intermediatePlaces != null) {
                for (GenericLocation intermediate : opt.intermediatePlaces) {
                    Vertex vertex = graph.streetIndex.getVertexForLocation(intermediate, opt);
                    intermediateVertices.add(vertex);
                }
            }
        } else {
            // debug mode, force endpoint vertices to those specified rather than searching
            fromVertex = from;
            toVertex = to;
        }

        // If the from and to vertices are generated and lie on some of the same edges, we need to wire them
        // up along those edges so that we don't get odd circuitous routes for really short trips.
        // TODO(flamholz): seems like this might be the wrong place for this code? Can't find a better one.
        if (fromVertex instanceof StreetLocation && toVertex instanceof StreetLocation) {
            StreetVertex fromStreetVertex = (StreetVertex) fromVertex;
            StreetVertex toStreetVertex = (StreetVertex) toVertex;
            Set<PlainStreetEdge> overlap = overlappingPlainStreetEdges(fromStreetVertex,
                    toStreetVertex);

            for (PlainStreetEdge pse : overlap) {
                makePartialEdgeAlong(pse, fromStreetVertex, toStreetVertex);
            }
        }
        
        if (opt.getStartingTransitStopId() != null) {
            TransitIndexService tis = graph.getService(TransitIndexService.class);
            if (tis == null) {
                throw new RuntimeException("Next/Previous/First/Last trip "
                        + "functionality depends on the transit index. Rebuild "
                        + "the graph with TransitIndexBuilder");
            }
            AgencyAndId stopId = opt.getStartingTransitStopId();
            startingStop = tis.getPreBoardEdge(stopId).getToVertex();
        }
        origin = opt.arriveBy ? toVertex : fromVertex;
        originBackEdge = opt.arriveBy ? toBackEdge : fromBackEdge;
        target = opt.arriveBy ? fromVertex : toVertex;
        calendarService = graph.getCalendarService();
        transferTable = graph.getTransferTable();
        // the graph's snapshot may be frequently updated.
        // Grab a reference to ensure a coherent view of the timetables throughout this search.
        if (graph.timetableSnapshotSource != null)
            timetableSnapshot = graph.timetableSnapshotSource.getSnapshot();
        else
            timetableSnapshot = null;
        setServiceDays();
        if (opt.batch)
            remainingWeightHeuristic = new TrivialRemainingWeightHeuristic();
        else
            remainingWeightHeuristic = heuristicFactory.getInstanceForSearch(opt);

        if (this.origin != null) {
            LOG.debug("Origin vertex inbound edges {}", this.origin.getIncoming());
            LOG.debug("Origin vertex outbound edges {}", this.origin.getOutgoing());
        }
        // target is where search will terminate, can be origin or destination depending on arriveBy
        LOG.debug("Target vertex {}", this.target);
        if (this.target != null) {
            LOG.debug("Destination vertex inbound edges {}", this.target.getIncoming());
            LOG.debug("Destination vertex outbound edges {}", this.target.getOutgoing());
        }
    }

    /* INSTANCE METHODS */

    public void check() {
        ArrayList<String> notFound = new ArrayList<String>();

        // check origin present when not doing an arrive-by batch search
        if (!(opt.batch && opt.arriveBy))
            if (fromVertex == null)
                notFound.add("from");

        // check destination present when not doing a depart-after batch search
        if (!opt.batch || opt.arriveBy) //
            if (toVertex == null)
                notFound.add("to");

        for (int i = 0; i < intermediateVertices.size(); i++) {
            if (intermediateVertices.get(i) == null) {
                notFound.add("intermediate." + i);
            }
        }
        if (notFound.size() > 0) {
            throw new VertexNotFoundException(notFound);
        }
        if (opt.getModes().isTransit() && !graph.transitFeedCovers(opt.dateTime)) {
            // user wants a path through the transit network,
            // but the date provided is outside those covered by the transit feed.
            throw new TransitTimesException();
        }
    }

    /**
     * Cache ServiceDay objects representing which services are running yesterday, today, and tomorrow relative to the search time. This information
     * is very heavily used (at every transit boarding) and Date operations were identified as a performance bottleneck. Must be called after the
     * TraverseOptions already has a CalendarService set.
     */
    public void setServiceDays() {
        final long SEC_IN_DAY = 60 * 60 * 24;
        final long time = opt.getSecondsSinceEpoch();
        this.serviceDays = new ArrayList<ServiceDay>(3);
        if (calendarService == null && graph.getCalendarService() != null
                && (opt.getModes() == null || opt.getModes().contains(TraverseMode.TRANSIT))) {
            LOG.warn("RoutingContext has no CalendarService. Transit will never be boarded.");
            return;
        }
        // This should be a valid way to find yesterday and tomorrow,
        // since DST changes more than one hour after midnight in US/EU.
        // But is this true everywhere?
        for (String agency : graph.getAgencyIds()) {
            addIfNotExists(this.serviceDays, new ServiceDay(graph, time - SEC_IN_DAY,
                    calendarService, agency));
            addIfNotExists(this.serviceDays, new ServiceDay(graph, time, calendarService, agency));
            addIfNotExists(this.serviceDays, new ServiceDay(graph, time + SEC_IN_DAY,
                    calendarService, agency));
        }
    }

    private static <T> void addIfNotExists(ArrayList<T> list, T item) {
        if (!list.contains(item)) {
            list.add(item);
        }
    }

    /** check if the start and end locations are accessible */
    public boolean isAccessible() {
        if (opt.isWheelchairAccessible()) {
            return isWheelchairAccessible(fromVertex) && isWheelchairAccessible(toVertex);
        }
        return true;
    }

    // this could be handled by method overloading on Vertex
    public boolean isWheelchairAccessible(Vertex v) {
        if (v instanceof TransitStop) {
            TransitStop ts = (TransitStop) v;
            return ts.hasWheelchairEntrance();
        } else if (v instanceof StreetLocation) {
            StreetLocation sl = (StreetLocation) v;
            return sl.isWheelchairAccessible();
        }
        return true;
    }

    /**
     * Tear down this routing context, removing any temporary edges.
     * 
     * @returns the number of edges removed.
     */
    public int destroy() {
        int nRemoved = 0;
        if (origin != null)
            nRemoved += origin.removeTemporaryEdges();
        if (target != null)
            nRemoved += target.removeTemporaryEdges();
        for (Vertex v : intermediateVertices)
            nRemoved += v.removeTemporaryEdges();
        return nRemoved;
    }

}

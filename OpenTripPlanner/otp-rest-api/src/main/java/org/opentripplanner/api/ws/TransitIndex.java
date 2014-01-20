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
package org.opentripplanner.api.ws;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Setter;

import org.codehaus.jettison.json.JSONException;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.ServiceCalendar;
import org.onebusaway.gtfs.model.ServiceCalendarDate;
import org.onebusaway.gtfs.model.Stop;
import org.opentripplanner.api.model.error.TransitError;
import org.opentripplanner.api.model.transit.AgencyList;
import org.opentripplanner.api.model.transit.CalendarData;
import org.opentripplanner.api.model.transit.ModeList;
import org.opentripplanner.api.model.transit.RouteData;
import org.opentripplanner.api.model.transit.RouteDataList;
import org.opentripplanner.api.model.transit.RouteList;
import org.opentripplanner.api.model.transit.ServiceCalendarData;
import org.opentripplanner.api.model.transit.StopList;
import org.opentripplanner.api.model.transit.StopTime;
import org.opentripplanner.api.model.transit.StopTimeList;
import org.opentripplanner.routing.core.RoutingRequest;
import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.core.TraverseMode;
import org.opentripplanner.routing.graph.Edge;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.routing.graph.Vertex;
import org.opentripplanner.routing.services.GraphService;
import org.opentripplanner.routing.services.StreetVertexIndexService;
import org.opentripplanner.routing.services.TransitIndexService;
import org.opentripplanner.routing.transit_index.RouteSegment;
import org.opentripplanner.routing.transit_index.RouteVariant;
import org.opentripplanner.routing.transit_index.adapters.RouteType;
import org.opentripplanner.routing.transit_index.adapters.ServiceCalendarDateType;
import org.opentripplanner.routing.transit_index.adapters.ServiceCalendarType;
import org.opentripplanner.routing.transit_index.adapters.StopType;
import org.opentripplanner.routing.transit_index.adapters.TripType;
import org.opentripplanner.routing.vertextype.TransitStop;
import org.opentripplanner.routing.vertextype.TransitStopArrive;
import org.opentripplanner.routing.vertextype.TransitStopDepart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.api.core.InjectParam;
import com.sun.jersey.api.spring.Autowire;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;

// NOTE - /ws/transit is the full path -- see web.xml

@Path("/transit")
@XmlRootElement
@Autowire
public class TransitIndex {

    private static final Logger LOG = LoggerFactory.getLogger(TransitIndex.class);

    private static final double STOP_SEARCH_RADIUS = 200;

    @Setter @InjectParam 
    private GraphService graphService;

    private static final long MAX_STOP_TIME_QUERY_INTERVAL = 86400 * 2;

    /**
     * Return a list of all agency ids in the graph
     */
    @GET
    @Path("/agencyIds")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_XML })
    public AgencyList getAgencyIds(@QueryParam("routerId") String routerId) throws JSONException {

        Graph graph = getGraph(routerId);

        AgencyList response = new AgencyList();
        response.agencies = graph.getAgencies();
        return response;
    }

    /**

     Return data about a route, such as its names, color, variants,
     stops, and directions.

     A variant represents a particular stop pattern (ordered list of
     stops) on a particular route. For example, the N train has at
     least four different variants: express (over the Manhattan
     bridge), and local (via lower Manhattan and the tunnel) x to
     Astoria and to Coney Island.

     Variant names are machine-generated, and are guaranteed to be
     unique (among variants for a route) but not stable across graph
     builds.

     A route's stops include stops made by any variant of the route.

    */
    @GET
    @Path("/routeData")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_XML })
    public Object getRouteData(@QueryParam("agency") String agency, @QueryParam("id") String id,
            @QueryParam("references") Boolean references, @QueryParam("extended") Boolean extended,
            @QueryParam("routerId") String routerId) throws JSONException {

        TransitIndexService transitIndexService = getGraph(routerId).getService(
                TransitIndexService.class);
        if (transitIndexService == null) {
            return new TransitError(
                    "No transit index found.  Add TransitIndexBuilder to your graph builder configuration and rebuild your graph.");
        }
        RouteDataList respond = new RouteDataList();

        for (String agencyId : getAgenciesIds(agency, routerId)) {
            AgencyAndId routeId = new AgencyAndId(agencyId, id);

            List<RouteVariant> variants = transitIndexService.getVariantsForRoute(routeId);

            if (variants.isEmpty())
                continue;

            RouteData response = new RouteData();
            response.id = routeId;
            response.variants = variants;
            response.directions = new ArrayList<String>(
                    transitIndexService.getDirectionsForRoute(routeId));
            response.route = new RouteType();
            for (RouteVariant variant : transitIndexService.getVariantsForRoute(routeId)) {
                Route route = variant.getRoute();
                response.route = new RouteType(route, extended);
                break;
            }

            if (references != null && references.equals(true)) {
                response.stops = new ArrayList<StopType>();
                for (org.onebusaway.gtfs.model.Stop stop : transitIndexService
                        .getStopsForRoute(routeId))
                    response.stops.add(new StopType(stop, extended));
            }

            respond.routeData.add(response);
        }

        return respond;
    }

    /**
     * Return a list of route ids
     */
    @GET
    @Path("/routes")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_XML })
    public Object getRoutes(@QueryParam("agency") String agency,
            @QueryParam("extended") Boolean extended, @QueryParam("routerId") String routerId)
            throws JSONException {

        TransitIndexService transitIndexService = getGraph(routerId).getService(
                TransitIndexService.class);
        if (transitIndexService == null) {
            return new TransitError(
                    "No transit index found.  Add TransitIndexBuilder to your graph builder configuration and rebuild your graph.");
        }
        Collection<AgencyAndId> allRouteIds = transitIndexService.getAllRouteIds();
        RouteList response = makeRouteList(allRouteIds, agency, extended, routerId);
        return response;
    }

    private RouteList makeRouteList(Collection<AgencyAndId> routeIds, String agencyFilter,
            @QueryParam("extended") Boolean extended, @QueryParam("routerId") String routerId) {
        RouteList response = new RouteList();
        TransitIndexService transitIndexService = getGraph(routerId).getService(
                TransitIndexService.class);
        for (AgencyAndId routeId : routeIds) {
            for (RouteVariant variant : transitIndexService.getVariantsForRoute(routeId)) {
                Route route = variant.getRoute();
                if (agencyFilter != null && !agencyFilter.equals(route.getAgency().getId()))
                    continue;
                RouteType routeType = new RouteType(route, extended);
                response.routes.add(routeType);
                break;
            }
        }
        return response;
    }

    /**
     * Returns data for a single stop given an id
     */

    @GET
    @Path("/stopData")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_XML })
    public Object getStopData(@QueryParam("agency") String agency, @QueryParam("id") String id,
            @QueryParam("extended") Boolean extended, @QueryParam("routerId") String routerId)
            throws JSONException {
    	
        Graph graph = getGraph(routerId);
    	TransitIndexService transitIndexService = graph.getService(TransitIndexService.class);

    	StopList response = new StopList();
    	
    	AgencyAndId stopId = new AgencyAndId(agency, id);
        
    	Edge preBoardEdge = transitIndexService.getPreBoardEdge(stopId);
        if(preBoardEdge != null) {
        	TransitStopDepart transitStop = (TransitStopDepart) preBoardEdge.getToVertex();
        	response.stops.add(new StopType(transitStop.getStop(), extended));
        }
        else { // check if stop is alight-only        	
	    	Edge preAlightEdge = transitIndexService.getPreAlightEdge(stopId);
	        if(preAlightEdge != null) {
	        	TransitStopArrive transitStop = (TransitStopArrive) preAlightEdge.getFromVertex();
	        	response.stops.add(new StopType(transitStop.getStop(), extended));
	        }
        }
        return response;
    }

    /**
     * Return stops near a point. The default search radius is 200m, but this can be changed with the radius parameter (in meters)
     */
    @GET
    @Path("/stopsNearPoint")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_XML })
    public Object getStopsNearPoint(@QueryParam("agency") String agency,
            @QueryParam("lat") Double lat, @QueryParam("lon") Double lon,
            @QueryParam("extended") Boolean extended, @QueryParam("routerId") String routerId,
            @QueryParam("radius") Double radius) throws JSONException {

        // default search radius.
        Double searchRadius = (radius == null) ? STOP_SEARCH_RADIUS : radius;

        Graph graph = getGraph(routerId);

        if (Double.isNaN(searchRadius) || searchRadius <= 0) {
            searchRadius = STOP_SEARCH_RADIUS;
        }

        StreetVertexIndexService streetVertexIndexService = graph.streetIndex;
        List<TransitStop> stops = streetVertexIndexService.getNearbyTransitStops(new Coordinate(
                lon, lat), searchRadius);
        TransitIndexService transitIndexService = graph.getService(TransitIndexService.class);
        if (transitIndexService == null) {
            return new TransitError(
                    "No transit index found.  Add TransitIndexBuilder to your graph builder configuration and rebuild your graph.");
        }

        StopList response = new StopList();
        for (TransitStop transitStop : stops) {
            AgencyAndId stopId = transitStop.getStopId();
            if (agency != null && !agency.equals(stopId.getAgencyId()))
                continue;
            StopType stop = new StopType(transitStop.getStop(), extended);
            stop.routes = transitIndexService.getRoutesForStop(stopId);
            response.stops.add(stop);
        }

        return response;
    }

    /**
     * Return routes that a stop is served by
     */
    @GET
    @Path("/routesForStop")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_XML })
    public Object getRoutesForStop(@QueryParam("agency") String agency,
            @QueryParam("id") String id, @QueryParam("extended") Boolean extended,
            @QueryParam("routerId") String routerId) throws JSONException {

        TransitIndexService transitIndexService = getGraph(routerId).getService(
                TransitIndexService.class);
        if (transitIndexService == null) {
            return new TransitError(
                    "No transit index found.  Add TransitIndexBuilder to your graph builder configuration and rebuild your graph.");
        }

        RouteList result = new RouteList();

        for (String string : getAgenciesIds(agency, routerId)) {
            List<AgencyAndId> routes = transitIndexService.getRoutesForStop(new AgencyAndId(string,
                    id));
            result.routes.addAll(makeRouteList(routes, null, extended, routerId).routes);
        }

        return result;
    }

    /**
     * Return stop times for a stop, in seconds since the epoch startTime and endTime are in milliseconds since epoch
     */
    @GET
    @Path("/stopTimesForStop")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_XML })
    public Object getStopTimesForStop(@QueryParam("agency") String stopAgency,
            @QueryParam("id") String stopId, @QueryParam("startTime") long startTime,
            @QueryParam("endTime") Long endTime, @QueryParam("extended") Boolean extended,
            @QueryParam("references") Boolean references, @QueryParam("routeId") String routeId,
            @QueryParam("routerId") String routerId) throws JSONException {

        startTime /= 1000;

        if (endTime == null) {
            endTime = startTime + 86400;
        } else {
            endTime /= 1000;
        }

        if (endTime - startTime > MAX_STOP_TIME_QUERY_INTERVAL) {
            return new TransitError("Max stop time query interval is " + (endTime - startTime)
                    + " > " + MAX_STOP_TIME_QUERY_INTERVAL);
        }
        TransitIndexService transitIndexService = getGraph(routerId).getService(
                TransitIndexService.class);
        if (transitIndexService == null) {
            return new TransitError(
                    "No transit index found.  Add TransitIndexBuilder to your graph builder configuration and rebuild your graph.");
        }

        // if no stopAgency is set try to search through all different agencies
        Graph graph = getGraph(routerId);

        // add all departures
        HashSet<TripType> trips = new HashSet<TripType>();
        StopTimeList result = new StopTimeList();
        result.stopTimes = new ArrayList<StopTime>();

        if (references != null && references.equals(true)) {
            result.routes = new HashSet<Route>();
        }

        for (String stopAgencyId : getAgenciesIds(stopAgency, routerId)) {

            AgencyAndId stop = new AgencyAndId(stopAgencyId, stopId);
            Edge preBoardEdge = transitIndexService.getPreBoardEdge(stop);
            if (preBoardEdge == null)
                continue;
            Vertex boarding = preBoardEdge.getToVertex();

            RoutingRequest options = makeTraverseOptions(startTime, routerId);

            HashMap<Long, Edge> seen = new HashMap();
            OUTER: for (Edge e : boarding.getOutgoing()) {
                // each of these edges boards a separate set of trips
                for (StopTime st : getStopTimesForBoardEdge(startTime, endTime, options, e,
                        extended)) {
                    // different parameters
                    st.phase = "departure";
                    if (extended != null && extended.equals(true)) {
                        if (routeId != null && !routeId.equals("")
                                && !st.trip.getRoute().getId().getId().equals(routeId))
                            continue;
                        if (references != null && references.equals(true))
                            result.routes.add(st.trip.getRoute());
                        result.stopTimes.add(st);
                    } else
                        result.stopTimes.add(st);
                    trips.add(st.trip);
                    if (seen.containsKey(st.time)) {
                        Edge old = seen.get(st.time);
                        System.out.println("DUP: " + old);
                        getStopTimesForBoardEdge(startTime, endTime, options, e, extended);
                        // break OUTER;
                    }
                    seen.put(st.time, e);
                }
            }

            // add the arriving stop times for cases where there are no departures
            Edge preAlightEdge = transitIndexService.getPreAlightEdge(stop);
            Vertex alighting = preAlightEdge.getFromVertex();
            for (Edge e : alighting.getIncoming()) {
                for (StopTime st : getStopTimesForAlightEdge(startTime, endTime, options, e,
                        extended)) {
                    if (!trips.contains(st.trip)) {
                        // diffrent parameters
                        st.phase = "arrival";
                        if (extended != null && extended.equals(true)) {
                            if (references != null && references.equals(true))
                                result.routes.add(st.trip.getRoute());
                            if (routeId != null && !routeId.equals("")
                                    && !st.trip.getRoute().getId().getId().equals(routeId))
                                continue;
                            result.stopTimes.add(st);
                        } else
                            result.stopTimes.add(st);
                    }
                }
            }

        }
        Collections.sort(result.stopTimes, new Comparator<StopTime>() {

            @Override
            public int compare(StopTime o1, StopTime o2) {
                if (o1.phase.equals("arrival") && o2.phase.equals("departure"))
                    return 1;
                if (o1.phase.equals("departure") && o2.phase.equals("arrival"))
                    return -1;
                return o1.time - o2.time > 0 ? 1 : -1;
            }

        });

        return result;
    }

    private RoutingRequest makeTraverseOptions(long startTime, String routerId) {
        RoutingRequest options = new RoutingRequest();
        // if (graphService.getCalendarService() != null) {
        // options.setCalendarService(graphService.getCalendarService());
        // options.setServiceDays(startTime, agencies);
        // }
        // TODO: verify correctness
        options.dateTime = startTime;
        Graph graph = getGraph(routerId);
        Collection<Vertex> vertices = graph.getVertices();
        Iterator<Vertex> it = vertices.iterator();
        options.setFromString(it.next().getLabel());
        options.setToString(it.next().getLabel());
        options.setRoutingContext(graph);
        return options;
    }

    /**
     * Return variant for a trip
     */
    @GET
    @Path("/variantForTrip")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_XML })
    public Object getVariantForTrip(@QueryParam("tripAgency") String tripAgency,
            @QueryParam("tripId") String tripId, @QueryParam("routerId") String routerId)
            throws JSONException {

        TransitIndexService transitIndexService = getGraph(routerId).getService(
                TransitIndexService.class);

        if (transitIndexService == null) {
            return new TransitError(
                    "No transit index found.  Add TransitIndexBuilder to your graph builder configuration and rebuild your graph.");
        }

        AgencyAndId trip = new AgencyAndId(tripAgency, tripId);
        RouteVariant variant = transitIndexService.getVariantForTrip(trip);

        return variant;
    }

    /**
     * Return information about calendar for given agency
     */
    @GET
    @Path("/calendar")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_XML })
    public Object getCalendar(@QueryParam("agency") String agency,
            @QueryParam("routerId") String routerId) throws JSONException {

        TransitIndexService transitIndexService = getGraph(routerId).getService(
                TransitIndexService.class);

        if (transitIndexService == null) {
            return new TransitError(
                    "No transit index found.  Add TransitIndexBuilder to your graph builder configuration and rebuild your graph.");
        }

        CalendarData response = new CalendarData();
        response.calendarList = new ArrayList<ServiceCalendarType>();
        response.calendarDatesList = new ArrayList<ServiceCalendarDateType>();

        for (String agencyId : getAgenciesIds(agency, routerId)) {
            List<ServiceCalendar> scList = transitIndexService.getCalendarsByAgency(agencyId);
            List<ServiceCalendarDate> scdList = transitIndexService
                    .getCalendarDatesByAgency(agencyId);

            if (scList != null)
                for (ServiceCalendar sc : scList)
                    response.calendarList.add(new ServiceCalendarType(sc));
            if (scdList != null)
                for (ServiceCalendarDate scd : scdList)
                    response.calendarDatesList.add(new ServiceCalendarDateType(scd));
        }

        return response;
    }

    /**
     * Return subsequent stop times for a trip; time is in milliseconds since epoch
     */
    @GET
    @Path("/stopTimesForTrip")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_XML })
    public Object getStopTimesForTrip(@QueryParam("stopAgency") String stopAgency,
            @QueryParam("stopId") String stopId, @QueryParam("tripAgency") String tripAgency,
            @QueryParam("tripId") String tripId, @QueryParam("time") long time,
            @QueryParam("extended") Boolean extended, @QueryParam("routerId") String routerId)
            throws JSONException {

        time /= 1000;

        AgencyAndId firstStop = null;
        if (stopId != null) {
            firstStop = new AgencyAndId(stopAgency, stopId);
        }
        AgencyAndId trip = new AgencyAndId(tripAgency, tripId);

        TransitIndexService transitIndexService = getGraph(routerId).getService(
                TransitIndexService.class);

        if (transitIndexService == null) {
            return new TransitError(
                    "No transit index found.  Add TransitIndexBuilder to your graph builder configuration and rebuild your graph.");
        }

        RouteVariant variant = transitIndexService.getVariantForTrip(trip);
        RoutingRequest options = makeTraverseOptions(time, routerId);

        StopTimeList result = new StopTimeList();
        result.stopTimes = new ArrayList<StopTime>();
        State state = null;
        RouteSegment start = null;
        for (RouteSegment segment : variant.getSegments()) {
            // this is all segments across all patterns that match this variant
            if (segment.stop.equals(firstStop)) {
                // this might be the correct start segment, but we need to try traversing and see if we get this trip
                // TODO: verify options and state creation correctness (AMB)
                State s0 = new State(segment.board.getFromVertex(), options);
                state = segment.board.traverse(s0);
                if (state == null)
                    continue;
                if (state.getBackTrip().getId().equals(trip)) {
                    start = segment;
                    StopTime st = new StopTime();
                    st.time = state.getTimeSeconds();
                    for (org.onebusaway.gtfs.model.Stop stop : variant.getStops())
                        if (stop.getId().equals(segment.stop)) {
                            st.stop = new StopType(stop, extended);
                        }
                    result.stopTimes.add(st);
                    break;
                }
            }
        }
        if (start == null) {
            return null;
        }

        for (RouteSegment segment : variant.segmentsAfter(start)) {
            // TODO: verify options/state init correctness
            State s0 = new State(segment.hopIn.getFromVertex(), state.getTimeSeconds(), options);
            state = segment.hopIn.traverse(s0);
            StopTime st = new StopTime();
            st.time = state.getTimeSeconds();
            for (org.onebusaway.gtfs.model.Stop stop : variant.getStops())
                if (stop.getId().equals(segment.stop))
                    if (stop.getId().equals(segment.stop)) {
                        if (extended != null && extended.equals(true)) {
                            st.stop = new StopType(stop, extended);
                        }
                    }
            result.stopTimes.add(st);
        }

        return result;
    }

    private List<StopTime> getStopTimesForBoardEdge(long startTime, long endTime,
            RoutingRequest options, Edge e, Boolean extended) {
        List<StopTime> out = new ArrayList<StopTime>();
        State result;
        long time = startTime;
        do {
            // TODO verify options/state correctness
            State s0 = new State(e.getFromVertex(), time, options);
            result = e.traverse(s0);
            if (result == null)
                break;
            time = result.getTimeSeconds();
            if (time > endTime)
                break;
            StopTime stopTime = new StopTime();
            stopTime.time = time;
            stopTime.trip = new TripType(result.getBackTrip(), extended);
            out.add(stopTime);

            time += 1; // move to the next board time
        } while (true);
        return out;
    }

    private List<StopTime> getStopTimesForAlightEdge(long startTime, long endTime,
            RoutingRequest options, Edge e, Boolean extended) {
        List<StopTime> out = new ArrayList<StopTime>();
        State result;
        long time = endTime;
        options = options.reversedClone();
        do {
            // TODO: verify options/state correctness
            State s0 = new State(e.getToVertex(), time, options);
            result = e.traverse(s0);
            if (result == null)
                break;
            time = result.getTimeSeconds();
            if (time < startTime)
                break;
            StopTime stopTime = new StopTime();
            stopTime.time = time;
            stopTime.trip = new TripType(result.getBackTrip(), extended);
            out.add(stopTime);
            time -= 1; // move to the previous alight time
        } while (true);
        return out;
    }

    /**
     * Return a list of all available transit modes supported, if any.
     * 
     * @throws JSONException
     */
    @GET
    @Path("/modes")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_XML })
    public Object getModes(@QueryParam("routerId") String routerId) throws JSONException {
        TransitIndexService transitIndexService = getGraph(routerId).getService(
                TransitIndexService.class);
        if (transitIndexService == null) {
            return new TransitError(
                    "No transit index found.  Add TransitIndexBuilder to your graph builder configuration and rebuild your graph.");
        }

        ModeList modes = new ModeList();
        modes.modes = new ArrayList<TraverseMode>();
        for (TraverseMode mode : transitIndexService.getAllModes()) {
            modes.modes.add(mode);
        }
        return modes;
    }

    private Graph getGraph(String routerId) {
        return graphService.getGraph(routerId);
    }

    public Object getCalendarServiceDataForAgency(@QueryParam("agency") String agency,
            @QueryParam("routerId") String routerId) {
        TransitIndexService transitIndexService = getGraph(routerId).getService(
                TransitIndexService.class);
        if (transitIndexService == null) {
            return new TransitError(
                    "No transit index found.  Add TransitIndexBuilder to your graph builder configuration and rebuild your graph.");
        }

        ServiceCalendarData data = new ServiceCalendarData();

        data.calendars = transitIndexService.getCalendarsByAgency(agency);
        data.calendarDates = transitIndexService.getCalendarDatesByAgency(agency);

        return data;
    }

    /**
     * Return a list of all stops that are inside a rectangle given by lat lon positions.
     */
    @GET
    @Path("/stopsInRectangle")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_XML })
    public Object stopsInRectangle(@QueryParam("agency") String agency,
            @QueryParam("leftUpLat") Double leftUpLat, @QueryParam("leftUpLon") Double leftUpLon,
            @QueryParam("rightDownLat") Double rightDownLat,
            @QueryParam("rightDownLon") Double rightDownLon, @QueryParam("extended") Boolean extended,
            @QueryParam("routerId") String routerId) throws JSONException {

        Graph graph = getGraph(routerId);
        StopList response = new StopList();

        StreetVertexIndexService streetVertexIndexService = graph.streetIndex;
        if (leftUpLat == null || leftUpLon == null || rightDownLat == null || rightDownLon == null) {
            double METERS_PER_DEGREE_LAT = 111111;
            double distance = 2000;
            for (Vertex gv : graph.getVertices()) {
                if (gv instanceof TransitStop) {
                    Coordinate c = gv.getCoordinate();
                    Envelope env = new Envelope(c);
                    double meters_per_degree_lon_here = METERS_PER_DEGREE_LAT
                            * Math.cos(Math.toRadians(c.y));
                    env.expandBy(distance / meters_per_degree_lon_here, distance
                            / METERS_PER_DEGREE_LAT);
                    StopType stop = new StopType(((TransitStop) gv).getStop(), extended);
                    response.stops.add(stop);
                }
            }
        } else {
            Coordinate cOne = new Coordinate(leftUpLon, leftUpLat);
            Coordinate cTwo = new Coordinate(rightDownLon, rightDownLat);
            List<TransitStop> stops = streetVertexIndexService.getNearbyTransitStops(cOne, cTwo);
            TransitIndexService transitIndexService = graph.getService(TransitIndexService.class);
            if (transitIndexService == null) {
                return new TransitError(
                        "No transit index found.  Add TransitIndexBuilder to your graph builder configuration and rebuild your graph.");
            }

            for (TransitStop transitStop : stops) {
                AgencyAndId stopId = transitStop.getStopId();
                if (agency != null && !agency.equals(stopId.getAgencyId()))
                    continue;
                StopType stop = new StopType(transitStop.getStop(), extended);
                if (extended != null && extended.equals(true))
                    stop.routes = transitIndexService.getRoutesForStop(stopId);
                response.stops.add(stop);
            }
        }

        return response;
    }

    /**
     * Return a list of all routes that operate between start stop and end stop.
     */
    @GET
    @Path("/routesBetweenStops")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_XML })
    public Object routesBetweenStops(@QueryParam("startAgency") String startAgency,
            @QueryParam("endAgency") String endAgency,
            @QueryParam("startStopId") String startStopId,
            @QueryParam("endStopId") String endStopId, @QueryParam("extended") Boolean extended,
            @QueryParam("routerId") String routerId) throws JSONException {

        RouteList response = new RouteList();

        RouteList routeList = (RouteList) this.getRoutesForStop(startAgency, startStopId, extended,
                routerId);

        for (RouteType route : routeList.routes) {
            for (String agency : getAgenciesIds(null, routerId)) {
                if (ifRouteBetweenStops(route, agency, routerId, startStopId, endStopId, endAgency))
                    response.routes.add(route);
            }
        }

        return response;
    }

    private Boolean ifRouteBetweenStops(RouteType route, String agency, String routerId,
            String startStopId, String endStopId, String endAgency) throws JSONException {

        RouteDataList routeDataList = (RouteDataList) this.getRouteData(agency, route.getId()
                .getId(), false, false, routerId);
        for (RouteData routeData : routeDataList.routeData)
            for (RouteVariant variant : routeData.variants)
                for (String endStopAgency : getAgenciesIds(endAgency, routerId)) {
                    Boolean start = false;
                    for (Stop stop : variant.getStops()) {
                        if (stop.getId().getId().equals(startStopId))
                            start = true;
                        if (start && stop.getId().equals(new AgencyAndId(endStopAgency, endStopId))) {
                            return true;
                        }
                    }
                }
        return false;
    }

    private ArrayList<String> getAgenciesIds(String agency, String routerId) {

        Graph graph = getGraph(routerId);

        ArrayList<String> agencyList = new ArrayList<String>();
        if (agency == null || agency.equals("")) {
            for (String a : graph.getAgencyIds()) {
                agencyList.add(a);
            }
        } else {
            agencyList.add(agency);
        }
        return agencyList;
    }
}

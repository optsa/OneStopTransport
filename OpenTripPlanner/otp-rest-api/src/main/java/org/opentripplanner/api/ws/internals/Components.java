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

package org.opentripplanner.api.ws.internals;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.annotation.XmlRootElement;

import org.opentripplanner.api.model.internals.GraphComponent;
import org.opentripplanner.api.model.internals.GraphComponentPolygons;
import org.opentripplanner.internals.AnalysisUtils;
import org.opentripplanner.routing.core.RoutingRequest;
import org.opentripplanner.routing.core.TraverseModeSet;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.routing.services.GraphService;
import org.opentripplanner.util.DateUtils;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.security.access.annotation.Secured;

import com.sun.jersey.api.spring.Autowire;
import com.sun.jersey.spi.resource.Singleton;
import com.vividsolutions.jts.geom.Geometry;

/**
 * Data about the components (in a graph-theoretical sense) of the graph. This is useful for
 * debugging connectivity and routing problems.
 *
 * @author novalis
 *
 */
@Path("/components")
@XmlRootElement
@Autowire
@Singleton
public class Components {
    private GraphService graphService;

    /**
     * cache for component geometry (for a specific query)
     */
    private List<Geometry> cachedPolygons;
    private RoutingRequest cachedOptions;
    private long cachedDateTime;

    @Required
    public void setGraphService(GraphService graphService) {
        this.graphService = graphService;
    }

    /**
     * Get polygons covering the components of the graph. The largest component (in terms of number
     * of nodes) will not overlap any other components (it will have holes); the others may overlap
     * each other.
     *
     * @param modes
     * @return
     */
    @Secured({ "ROLE_USER" })
    @GET
    @Path("/polygons")
    @Produces({ MediaType.APPLICATION_JSON })
    public GraphComponentPolygons getComponentPolygons(
            @DefaultValue("TRANSIT,WALK") @QueryParam("modes") TraverseModeSet modes,
            @QueryParam("date") String date, @QueryParam("time") String time,
            @DefaultValue("") @QueryParam("bannedRoutes") String bannedRoutes,
            @QueryParam("routerId") String routerId) {

        RoutingRequest options = new RoutingRequest(modes);
        if (bannedRoutes.length() > 0) {
            options.setBannedRoutes(bannedRoutes);
        }

        Graph graph = graphService.getGraph(routerId);
        long dateTime = DateUtils.toDate(date, time, graph.getTimeZone()).getTime();
        if (cachedPolygons == null || dateTime != cachedDateTime || !options.equals(cachedOptions)) {
            cachedOptions = options;
            cachedDateTime = dateTime;
            cachedPolygons = AnalysisUtils.getComponentPolygons(graph, options, dateTime);
        }
        
        GraphComponentPolygons out = new GraphComponentPolygons();
        out.components = new ArrayList<GraphComponent>();

        for (Geometry geometry : cachedPolygons) {
            GraphComponent component = new GraphComponent();
            component.polygon = geometry;
            out.components.add(component);
        }

        return out;
    }
}

package org.opentripplanner.api.thrift.util;

import org.opentripplanner.api.thrift.definition.GraphVertex;
import org.opentripplanner.routing.graph.Vertex;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * Extends the Thrift GraphVertex
 * 
 * @author avi
 * 
 */
public class GraphVertexExtension extends GraphVertex {

    /**
     * Required for serialization.
     */
    private static final long serialVersionUID = 3024640775481728306L;

    /**
     * Construct from a vertex.
     * 
     * @param v
     */
    public GraphVertexExtension(Vertex v) {
        super();

        setId(v.getIndex());
        setLabel(v.getLabel());
        Coordinate coord = v.getCoordinate();
        if (coord != null) {
            LatLngExtension ll = new LatLngExtension(coord);
            setLat_lng(ll);
        }

        setName(v.getName());
        setIn_degree(v.getDegreeIn());
        setOut_degree(v.getDegreeOut());
    }
}
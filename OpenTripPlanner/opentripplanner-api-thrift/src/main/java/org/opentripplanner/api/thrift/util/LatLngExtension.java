package org.opentripplanner.api.thrift.util;

import org.opentripplanner.api.thrift.definition.LatLng;
import org.opentripplanner.common.model.GenericLocation;

import com.vividsolutions.jts.geom.Coordinate;

public class LatLngExtension extends LatLng {

    /**
     * Required for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Default constructor.
     * 
     * TODO(flamholz): figure out if lombok calls superclass constructor?
     */
    public LatLngExtension() {
        super();
    }

    /**
     * Makes a Thrift LatLng structure from a Coordinate.
     * 
     * @param c
     * @return
     */
    public LatLngExtension(Coordinate c) {
        this();
        setCoordinate(c);
    }

    /**
     * Copy constructor...
     * 
     * @param other
     */
    public LatLngExtension(LatLng other) {
        this();

        setLat(other.getLat());
        setLng(other.getLng());
    }

    /**
     * Set lat and lng correctly mapping y, x = lat, lng
     * 
     * @param c
     */
    public void setCoordinate(Coordinate c) {
        // lat, lng = y, x
        setLat(c.y);
        setLng(c.x);
    }

    /**
     * Create a coordinate.
     * 
     * @return
     */
    public Coordinate toCoordinate() {
        // Coordinates are x,y meaning lng,lat.
        return new Coordinate(getLng(), getLat());
    }

    /**
     * Create a GenericLocation.
     * 
     * @return
     */
    public GenericLocation toGenericLocation() {
        return new GenericLocation(getLat(), getLng());
    }
}

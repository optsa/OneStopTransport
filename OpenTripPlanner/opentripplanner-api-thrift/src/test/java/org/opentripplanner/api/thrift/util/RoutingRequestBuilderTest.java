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

package org.opentripplanner.api.thrift.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.opentripplanner.api.thrift.definition.LatLng;
import org.opentripplanner.api.thrift.definition.Location;
import org.opentripplanner.api.thrift.definition.TravelMode;
import org.opentripplanner.api.thrift.definition.TripParameters;
import org.opentripplanner.common.model.GenericLocation;
import org.opentripplanner.routing.core.RoutingRequest;
import org.opentripplanner.routing.core.TraverseMode;
import org.opentripplanner.routing.core.TraverseModeSet;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * Tests for TripUtil class.
 * 
 * @author flamholz
 */
public class RoutingRequestBuilderTest {

    /**
     * Test behavior for a simple trip.
     */
    @Test
    public void testAddTripParameters() {
        TripParameters tp = new TripParameters();
        tp.addToAllowed_modes(TravelMode.WALK);
        tp.addToAllowed_modes(TravelMode.CAR);

        LatLng originLatLng = new LatLng(1.0, 2.5);
        Location origin = new Location();
        origin.setLat_lng(originLatLng);
        origin.setHeading(137.2);

        LatLng destLatLng = new LatLng(-3.0, 9.7);
        Location dest = new Location();
        dest.setLat_lng(destLatLng);

        tp.setOrigin(origin);
        tp.setDestination(dest);

        RoutingRequest rr = (new RoutingRequestBuilder()).addTripParameters(tp).build();

        GenericLocation from = rr.getFrom();
        Coordinate expectedFromCoord = new Coordinate(2.5, 1.0);
        assertEquals(expectedFromCoord, from.getCoordinate());
        assertEquals(origin.getHeading(), from.getHeading(), 0.0);

        GenericLocation to = rr.getTo();
        Coordinate expectedToCoord = new Coordinate(9.7, -3.0);
        assertEquals(expectedToCoord, to.getCoordinate());
        assertFalse(to.hasHeading());

        for (TravelMode tm : tp.getAllowed_modes()) {
            TraverseModeSet modeSet = rr.getModes();
            TraverseMode traverseMode = (new TravelModeWrapper(tm)).toTraverseMode();
            assertTrue(modeSet.contains(traverseMode));
        }
    }

    @Test
    public void testAddTripParametersWithStartTime() {
        TripParameters tp = new TripParameters();
        tp.setStart_time(getTimeSeconds());

        LatLng originLatLng = new LatLng(1.0, 2.5);
        Location origin = new Location();
        origin.setLat_lng(originLatLng);

        LatLng destLatLng = new LatLng(-3.0, 9.7);
        Location dest = new Location();
        dest.setLat_lng(destLatLng);

        tp.setOrigin(origin);
        tp.setDestination(dest);

        RoutingRequest rr = (new RoutingRequestBuilder()).addTripParameters(tp).build();

        GenericLocation from = rr.getFrom();
        Coordinate expectedFromCoord = new Coordinate(2.5, 1.0);
        assertEquals(expectedFromCoord, from.getCoordinate());

        GenericLocation to = rr.getTo();
        Coordinate expectedToCoord = new Coordinate(9.7, -3.0);
        assertEquals(expectedToCoord, to.getCoordinate());

        assertEquals(tp.getStart_time(), rr.dateTime);
        assertFalse(rr.arriveBy);
    }

    @Test
    public void testAddTripParametersWithArriveBy() {
        TripParameters tp = new TripParameters();
        tp.setArrive_by(getTimeSeconds() + 60 * 30);

        LatLng originLatLng = new LatLng(1.0, 2.5);
        Location origin = new Location();
        origin.setLat_lng(originLatLng);

        LatLng destLatLng = new LatLng(-3.0, 9.7);
        Location dest = new Location();
        dest.setLat_lng(destLatLng);

        tp.setOrigin(origin);
        tp.setDestination(dest);

        RoutingRequest rr = (new RoutingRequestBuilder()).addTripParameters(tp).build();

        GenericLocation from = rr.getFrom();
        Coordinate expectedFromCoord = new Coordinate(2.5, 1.0);
        assertEquals(expectedFromCoord, from.getCoordinate());

        GenericLocation to = rr.getTo();
        Coordinate expectedToCoord = new Coordinate(9.7, -3.0);
        assertEquals(expectedToCoord, to.getCoordinate());

        assertEquals(tp.getArrive_by(), rr.dateTime);
        assertTrue(rr.arriveBy);
    }

    @Test
    public void testAddTripParametersWithBothTimes() {
        TripParameters tp = new TripParameters();
        long now = getTimeSeconds();
        tp.setStart_time(now);
        tp.setArrive_by(now + 60 * 30);

        LatLng originLatLng = new LatLng(1.0, 2.5);
        Location origin = new Location();
        origin.setLat_lng(originLatLng);

        LatLng destLatLng = new LatLng(-3.0, 9.7);
        Location dest = new Location();
        dest.setLat_lng(destLatLng);

        tp.setOrigin(origin);
        tp.setDestination(dest);

        RoutingRequest rr = (new RoutingRequestBuilder()).addTripParameters(tp).build();

        GenericLocation from = rr.getFrom();
        Coordinate expectedFromCoord = new Coordinate(2.5, 1.0);
        assertEquals(expectedFromCoord, from.getCoordinate());

        GenericLocation to = rr.getTo();
        Coordinate expectedToCoord = new Coordinate(9.7, -3.0);
        assertEquals(expectedToCoord, to.getCoordinate());

        // Start time takes precedence
        assertEquals(tp.getStart_time(), rr.dateTime);
        assertFalse(rr.arriveBy);
    }

    @Test
    public void testSetNumItineraries() {
        int n = 3;
        RoutingRequest rr = (new RoutingRequestBuilder()).setNumItineraries(n).build();
        assertEquals(n, rr.getNumItineraries());
    }

    @Test
    public void testSetStartTime() {
        long now = this.getTimeSeconds();
        RoutingRequest rr = (new RoutingRequestBuilder()).setStartTime(now).build();
        assertEquals(now, rr.dateTime);
        assertFalse(rr.arriveBy);
    }

    @Test
    public void testSetArriveBy() {
        long t = this.getTimeSeconds() + 30 * 60;
        RoutingRequest rr = (new RoutingRequestBuilder()).setArriveBy(t).build();
        assertEquals(t, rr.dateTime);
        assertTrue(rr.arriveBy);
    }

    @Test
    public void testSetOriginDestination() {
        LatLng origin = new LatLng(1.0, 2.5);
        LatLng dest = new LatLng(-3.0, 9.7);

        RoutingRequest rr = (new RoutingRequestBuilder()).setOrigin(origin).setDestination(dest)
                .build();

        GenericLocation from = rr.getFrom();
        Coordinate expectedFromCoord = new Coordinate(2.5, 1.0);
        assertEquals(expectedFromCoord, from.getCoordinate());

        GenericLocation to = rr.getTo();
        Coordinate expectedToCoord = new Coordinate(9.7, -3.0);
        assertEquals(expectedToCoord, to.getCoordinate());
    }
    
    @Test
    public void testSetOriginDestinationAsLocations() {
        Location origin = new Location();
        origin.setLat_lng(new LatLng(1.0, 2.5));
        origin.setHeading(137.0);
        Location dest = new Location();
        dest.setLat_lng(new LatLng(-3.0, 9.7));
        dest.setHeading(-12.998);

        RoutingRequest rr = (new RoutingRequestBuilder()).setOrigin(origin).setDestination(dest)
                .build();

        GenericLocation from = rr.getFrom();
        Coordinate expectedFromCoord = new Coordinate(2.5, 1.0);
        assertEquals(expectedFromCoord, from.getCoordinate());
        assertEquals(origin.getHeading(), from.getHeading(), 0.0);

        GenericLocation to = rr.getTo();
        Coordinate expectedToCoord = new Coordinate(9.7, -3.0);
        assertEquals(expectedToCoord, to.getCoordinate());
        assertEquals(dest.getHeading(), to.getHeading(), 0.0);
    }

    /**
     * @return Current time in seconds.
     */
    private long getTimeSeconds() {
        return System.currentTimeMillis() / 1000;
    }
}

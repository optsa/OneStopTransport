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

import static org.junit.Assert.*;

import org.junit.Test;
import org.opentripplanner.api.thrift.definition.LatLng;
import org.opentripplanner.api.thrift.definition.Location;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * Tests for LocationUtil class.
 * 
 * @author flamholz
 */
public class LocationExtensionTest {

    @Test
    public void testMakeLocation() {
        Coordinate c = new Coordinate(1.2, 2.3);
        Location loc = new LocationExtension(c);
        LatLng ll = loc.getLat_lng();

        assertCoordEquals(c, ll);

        // Setting a z value should affect nothing.
        c = new Coordinate(1.2, 2.3, 19.5);
        loc = new LocationExtension(c);
        ll = loc.getLat_lng();

        assertCoordEquals(c, ll);
    }

    private void assertCoordEquals(Coordinate c, LatLng ll) {
        assertEquals(c.x, ll.getLng(), 1e-7);
        assertEquals(c.y, ll.getLat(), 1e-7);
    }

}

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

package org.opentripplanner.common.geometry;

import com.vividsolutions.jts.geom.Coordinate;

public interface DistanceLibrary {

    public abstract double distance(Coordinate from, Coordinate to);

    public abstract double fastDistance(Coordinate from, Coordinate to);

    public abstract double distance(double lat1, double lon1, double lat2, double lon2);

    public abstract double fastDistance(double lat1, double lon1, double lat2, double lon2);

    public abstract double distance(double lat1, double lon1, double lat2, double lon2,
            double radius);


}
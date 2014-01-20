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

package org.opentripplanner.routing.impl.raptor;

import java.io.Serializable;

import org.opentripplanner.routing.vertextype.TransitStop;
import org.opentripplanner.routing.vertextype.TransitStopArrive;
import org.opentripplanner.routing.vertextype.TransitStopDepart;

public class RaptorStop implements Serializable {
    private static final long serialVersionUID = 5308124086666575865L;
    public int index;
    public TransitStop stopVertex;
    public TransitStopArrive arriveVertex;
    public TransitStopDepart departVertex;
    
    public String toString() {
        return "RaptorStop(" + stopVertex + ", " + index + ")";
    }
}

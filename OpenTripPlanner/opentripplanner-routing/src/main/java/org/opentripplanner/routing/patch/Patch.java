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

package org.opentripplanner.routing.patch;

import java.io.Serializable;

import org.opentripplanner.routing.core.StateEditor;
import org.opentripplanner.routing.core.RoutingRequest;
import org.opentripplanner.routing.graph.Graph;

/**
 * A patch modifies a graph by filtering edge traversals.  Traversals 
 * can either be forbidden entirely (in the case of cancelled service), or
 * modified (for instance, to add an alert). 
 * @author novalis
 *
 */
public interface Patch extends Serializable {
    public Alert getAlert();

    public boolean activeDuring(RoutingRequest options, long start, long end);

    public boolean displayDuring(RoutingRequest options, long start, long end);

    public String getId();

    void setId(String id);

    public void apply(Graph graph);

    public void remove(Graph graph);

    public boolean filterTraverseResult(StateEditor result, boolean displayOnly);
}

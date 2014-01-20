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

package org.opentripplanner.routing.graph;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import org.opentripplanner.routing.graph.AbstractVertex.ValidEdgeTypes;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * A vertex in the graph. Each vertex has a longitude/latitude location, as well as a set of
 * incoming and outgoing edges.
 */
public interface Vertex extends Serializable, Cloneable {

    public abstract String toString();
    
    /* EDGE LISTS */
    
    public abstract Collection<Edge> getOutgoing();
    public abstract void addOutgoing(Edge ee);
    public abstract boolean removeOutgoing(Edge ee);
    public abstract int getDegreeOut();

    public abstract Collection<Edge> getIncoming();
    public abstract void addIncoming(Edge ee);
    public abstract boolean removeIncoming(Edge ee);
    public abstract int getDegreeIn();

    
    /* ACCESSOR METHODS */
    
    /** Get the distance from this vertex to the closest transit stop in meters. */
    public abstract double getDistanceToNearestTransitStop();
    
    // TODO: this is a candidate for no-arg message-passing style
    public abstract void setDistanceToNearestTransitStop(double distance);

    /** Get the longitude of the vertex */
    public abstract double getX();

    /** Get the latitude of the vertex */
    public abstract double getY();

    /** Every vertex has a label which is globally unique. */
    public abstract String getLabel();

    /** If this vertex is located on only one street, get that street's name. */
    public abstract String getName();

    public void setStreetName(String streetName);

    public abstract Coordinate getCoordinate();

    /**
     * Get the bearing, in degrees, between this vertex and another coordinate.
     * 
     * @param other
     * @return
     */
    public abstract double azimuthTo(Coordinate other);
    
    /**
     * Get the bearing, in degrees, between this vertex and another.
     * 
     * @param other
     * @return
     */
    public abstract double azimuthTo(Vertex other);

    /** Get this vertex's unique index, that can serve as a hashcode or an index into a table */
    public abstract int getIndex();

    public abstract void setIndex(int index);

    public abstract void setGroupIndex(int groupIndex);

    public abstract int getGroupIndex();

    public abstract List<Edge> getOutgoingStreetEdges();

    /**
     * Merge another vertex into this one.  Useful during graph construction for handling 
     * sequential non-branching streets, and empty dwells.
     */
    public abstract void mergeFrom(Graph graph, Vertex other);

    /**
     * Clear this vertex's outgoing and incoming edge lists, and remove all the edges
     * they contained from this vertex's neighbors.
     */
    public abstract void removeAllEdges();
    
    /** 
     * Clean up before garbage collection. Usually this method does nothing, but temporary vertices 
     * must provide a method to remove their associated temporary edges from adjacent vertices' 
     * edge lists, usually by simply calling detach() on them. 
     * @return the number of edges affected by the cleanup.
     */
    public abstract int removeTemporaryEdges();

    /** Trim edge lists */
    public abstract void compact();


    /* GRAPH COHERENCY AND TYPE CHECKING */

    public abstract ValidEdgeTypes getValidOutgoingEdgeTypes();

    public abstract ValidEdgeTypes getValidIncomingEdgeTypes();

    /** Check that all of this Vertex's incoming and outgoing edges are of the proper types */
    public abstract boolean edgeTypesValid();

}
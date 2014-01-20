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

package org.opentripplanner.routing.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.opentripplanner.routing.core.RoutingRequest;
import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.edgetype.LegSwitchingEdge;
import org.opentripplanner.routing.graph.Edge;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.routing.graph.Vertex;
import org.opentripplanner.routing.pathparser.BasicPathParser;
import org.opentripplanner.routing.pathparser.NoThruTrafficPathParser;
import org.opentripplanner.routing.pathparser.PathParser;
import org.opentripplanner.routing.services.GraphService;
import org.opentripplanner.routing.services.PathService;
import org.opentripplanner.routing.services.SPTService;
import org.opentripplanner.routing.spt.GraphPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class TravelingSalesmanPathService implements PathService {

	private static final Logger LOG = LoggerFactory.getLogger(TravelingSalesmanPathService.class);

	@Autowired
	public GraphService graphService;

	// @Resource("name") or @Qualifier
	@Autowired
	public SPTService tspSptService;

	private PathService chainedPathService;

	@Override
	public List<GraphPath> getPaths(RoutingRequest options) {
		if (!options.hasIntermediatePlaces()) {
			LOG.debug("No intermediates places given, calling underlying path service.");
			System.out.println("No intermediates places given, calling underlying path service.");
			// no intermediate places specified, chain to main path service
			return chainedPathService.getPaths(options);
		}

		/* intermediate places present, intercept request */
		Graph graph = graphService.getGraph(options.getRouterId());
		long time = options.dateTime;
		long originalTime = options.dateTime;
		List<String> waitingTimes = options.intermediatePlacesWaitingTimes;
		options.setRoutingContext(graph);
		options.rctx.pathParsers = new PathParser[] { new BasicPathParser(),
				new NoThruTrafficPathParser() };

		Vertex fromVertex = options.rctx.fromVertex;
		Vertex toVertex = options.rctx.toVertex;
		if (options.intermediatesEffectivelyOrdered()) {
			LOG.debug("Intermediates are ordered.");
			System.out.println("Intermediates are ordered.");

			List<Vertex> vertices = options.rctx.intermediateVertices;
			vertices.add(toVertex);
			options.clearIntermediatePlaces();
			// simple case: intermediate places are in order.
			List<GraphPath> bestPaths = new ArrayList<GraphPath>();
			List<GraphPath> Paths01 = new ArrayList<GraphPath>();
			List<GraphPath> Paths10 = new ArrayList<GraphPath>();
			//bestPaths
			time = originalTime;
			Vertex previousVertex = fromVertex;
			for (int i = 0; i<vertices.size(); i++) {
				Vertex v = vertices.get(i);
				options.dateTime = time;
				options.setRoutingContext(graph, previousVertex, v);
				List<GraphPath> partialPaths = chainedPathService.getPaths(options);
				if (partialPaths == null || partialPaths.size() == 0)
					return null;
				GraphPath path = partialPaths.get(0);
				bestPaths.add(path);
				previousVertex = v;
				time = path.getEndTime();
				if(i<waitingTimes.size())
					time += Integer.parseInt(waitingTimes.get(i));
			}	
			//Paths01
			time = originalTime;
			previousVertex = fromVertex;
			for (int i = 0; i<vertices.size(); i++) {
				Vertex v = vertices.get(i);
				options.dateTime = time;
				options.setRoutingContext(graph, previousVertex, v);
				List<GraphPath> partialPaths = chainedPathService.getPaths(options);
				if (partialPaths == null || partialPaths.size() == 0)
					return null;
				GraphPath path = v == toVertex && partialPaths.size() > 1?partialPaths.get(1):partialPaths.get(0);
				Paths01.add(path);
				previousVertex = v;
				time = path.getEndTime();
				if(i<waitingTimes.size())
					time += Integer.parseInt(waitingTimes.get(i));
			}
			//Paths10
			time = originalTime;
			previousVertex = fromVertex;
			for (int i = 0; i<vertices.size(); i++) {
				Vertex v = vertices.get(i);
				options.dateTime = time;
				options.setRoutingContext(graph, previousVertex, v);
				List<GraphPath> partialPaths = chainedPathService.getPaths(options);
				if (partialPaths == null || partialPaths.size() == 0)
					return null;
				GraphPath path = previousVertex != fromVertex || partialPaths.size() == 1? partialPaths.get(0):partialPaths.get(1);
				Paths10.add(path);
				previousVertex = v;
				time = path.getEndTime();
				if(i<waitingTimes.size())
					time += Integer.parseInt(waitingTimes.get(i));
			}
			return Arrays.asList(joinPaths(bestPaths),joinPaths(Paths01),joinPaths(Paths10));
		}

		// Difficult case: intermediate places can occur in any order (Traveling Salesman Problem)
		LOG.debug("Intermediates are not ordered: attempting to optimize ordering.");
		Map<Vertex, HashMap<Vertex, GraphPath>> paths = new HashMap<Vertex, HashMap<Vertex, GraphPath>>();

		HashMap<Vertex, GraphPath> firstLegPaths = new HashMap<Vertex, GraphPath>();
		paths.put(fromVertex, firstLegPaths);

		// compute shortest paths between each pair of vertices
		@SuppressWarnings("unchecked")
		List<Vertex> intermediates = (List<Vertex>) options.rctx.intermediateVertices.clone();
		for (Vertex v : intermediates) {

			/* Find initial paths from the source vertex to the intermediate vertex */
			options.dateTime = time;
			options.setRoutingContext(graph, fromVertex, v);
			List<GraphPath> partialPaths = chainedPathService.getPaths(options);
			if (partialPaths == null || partialPaths.size() == 0)
				return null;
			firstLegPaths.put(v, partialPaths.get(0));

			/* Find paths from this intermediate vertex to each other intermediate */
			HashMap<Vertex, GraphPath> outPaths = new HashMap<Vertex, GraphPath>();
			paths.put(v, outPaths);

			for (Vertex tv : intermediates) {
				/* We don't need to compute paths where the source and target vertex are the same */
				if (v == tv)
					continue;

				options.setRoutingContext(graph, v, tv);
				List<GraphPath> morePaths = chainedPathService.getPaths(options);
				if (!morePaths.isEmpty()) {
					outPaths.put(tv, morePaths.get(0));
				}
			}

			/* Find paths from the intermediate vertex to the target vertex */
			options.setRoutingContext(graph, v, toVertex);
			List<GraphPath> lastPaths = chainedPathService.getPaths(options);
			if (!lastPaths.isEmpty())
				outPaths.put(toVertex, lastPaths.get(0));
		}

		// compute shortest path overall
		HashSet<Vertex> verticesCopy = new HashSet<Vertex>();
		verticesCopy.addAll(intermediates);
		return Arrays.asList(TSPPathFinder.findShortestPath(toVertex, fromVertex, paths,
				verticesCopy, time, options));
	}

	private GraphPath joinPaths(List<GraphPath> paths) {
		State lastState = paths.get(0).states.getLast();
		GraphPath newPath = new GraphPath(lastState, false);
		Vertex lastVertex = lastState.getVertex();
		for (GraphPath path : paths.subList(1, paths.size())) {
			lastState = newPath.states.getLast();
			// add a leg-switching state
			LegSwitchingEdge legSwitchingEdge = new LegSwitchingEdge(lastVertex, lastVertex);
			lastState = legSwitchingEdge.traverse(lastState);
			newPath.edges.add(legSwitchingEdge);
			newPath.states.add(lastState);
			// add the next subpath
			for (Edge e : path.edges) {
				lastState = e.traverse(lastState);
				if(lastState!=null){
					newPath.edges.add(e);
					newPath.states.add(lastState);
				}
			}
			lastVertex = path.getEndVertex();
		}
		return newPath;
	}

	public PathService getChainedPathService() {
		return chainedPathService;
	}

	@Autowired
	public void setChainedPathService(PathService chainedPathService) {
		this.chainedPathService = chainedPathService;
	}

}

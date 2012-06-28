package mbi;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DirectedMultigraph;

public class DeBruijnGraph extends DirectedMultigraph<String, String> {
	private static final long serialVersionUID = 1L;

	public DeBruijnGraph(DeBruijnEdgeFactory arg0) {
		super(arg0);
		arg0.setGraph(this);
	}

	public boolean isBalanced() {
		return false;
	}

	// TODO: figure out a way to do it.
	public void balance() {

	}
	
	public Set<String> getImbalancedVertices() {
		Map<String, Integer> stats = new HashMap<String, Integer>();

		for (String v : this.vertexSet()) {
			stats.put(v, 0);
		}

		for (String edge : this.edgeSet()) {
			stats.put(this.getEdgeSource(edge),
					stats.get(this.getEdgeSource(edge)) + 1);
			stats.put(this.getEdgeTarget(edge),
					stats.get(this.getEdgeTarget(edge)) - 1);
		}

		Set<String> uv = new HashSet<String>();
		for (String key : stats.keySet()) {
			if (stats.get(key) != 0) {
				uv.add(key);
			}
		}
		return uv;
	}

	public String assemble() {
		Set<String> unbalancedVertices = getImbalancedVertices();
		for (String uv : unbalancedVertices) {
			System.out.println("UNBALANCED VERTEX: " + uv + " balance: "
					+ (this.outDegreeOf(uv) - this.inDegreeOf(uv)));
		}
		if (unbalancedVertices.size() != 0 && unbalancedVertices.size() != 2) {
			System.out.println("Unbalanced graph given. Aborting...");
			return null;
		}
		String start = null, end = null;
		// holds the inDegrees
		Map<String, Integer> inDegrees = new HashMap<String, Integer>();
		for (String v : this.vertexSet()) {
			inDegrees.put(v, this.inDegreeOf(v));
		}

		// determine the start and end of graph traversal
		for (String v : unbalancedVertices) {
			if (this.inDegreeOf(v) > this.outDegreeOf(v)) {
				end = v;
			} else {
				start = v;
			}
		}

		List<List<String>> paths = new LinkedList<List<String>>();
		String current = start;

		inDegrees.put(start, inDegrees.get(start) - 1);
		if (inDegrees.get(start) <= 0) {
			inDegrees.remove(start);
		}

		while (!inDegrees.isEmpty()) {
			paths.add(new LinkedList<String>());
			while (current != null) {
				paths.get(paths.size() - 1).add(current);
				current = getNextStep(this, inDegrees, current);
			}
			current = determineCrossing(paths.get(paths.size() - 1), inDegrees);
		}

		System.out.println(paths.toString());
		return getGenome(paths);
	}

	private String determineCrossing(List<String> lastPath,
			Map<String, Integer> inDegrees) {
		for (String v : lastPath) {
			if (inDegrees.get(v) != null && inDegrees.get(v) > 0) {
				return v;
			}
		}
		return null;
	}

	// achtung: decrements inDegrees!!!
	private String getNextStep(DirectedGraph<String, String> g,
			Map<String, Integer> inDegrees, String currentNode) {
		for (String edge : g.outgoingEdgesOf(currentNode)) {
			String target = g.getEdgeTarget(edge);
			if (inDegrees.get(target) != null && inDegrees.get(target) > 0) {
				if (inDegrees.get(target) == 1) {
					inDegrees.remove(target);
				} else {
					inDegrees.put(target, inDegrees.get(target) - 1);
				}
				return target;
			}
		}
		return null;
	}

	private String getGenome(List<List<String>> paths) {
		List<String> result = new LinkedList<String>();
		result.addAll(paths.get(0));
		paths.remove(0);
		while (paths.size() > 0) {
			for (int i = 0; i < paths.size(); ++i) {
				List<String> path = paths.get(i);
				int anchor = result.lastIndexOf(path.get(0));
				if (anchor == -1) {
					continue;
				}
				if (path.get(0).equals(path.get(path.size() - 1))) {
					result.remove(anchor);
					result.addAll(anchor, path);
					paths.remove(i);
					break;
				} else {
					if (result.get(result.size() - 1).equals(path.get(0))) {
						result.remove(anchor);
						result.addAll(anchor, path);
						paths.remove(i);
						break;
					} else {
						continue;
					}
				}
			}
		}
		StringBuilder str = new StringBuilder();
		for (String read : result) {
			if (!read.equals(result.get(result.size() - 1))) {
				str.append(read.substring(0, 1));
			} else {
				str.append(read);
			}
		}
		return str.toString();
	}

}

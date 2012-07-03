package mbi;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.jgrapht.graph.DirectedMultigraph;

public class DeBruijnGraph extends DirectedMultigraph<String, String> {
	private static final long serialVersionUID = 1L;

	public DeBruijnGraph(DeBruijnEdgeFactory arg0) {
		super(arg0);
		arg0.setGraph(this);
	}

	// TODO: figure out a way to do it.
	public void balance() {

	}

	public Set<String> getImbalancedVertices() {
		Set<String> vers = new HashSet<String>();
		for (String vert : this.vertexSet()) {
			if (this.inDegreeOf(vert) != this.outDegreeOf(vert)) {
				vers.add(vert);
			}
		}
		return vers;
	}

	public synchronized List<String> findEulerPath(boolean verbose)
			throws MbiException {
		List<String> path = new LinkedList<String>();
		DeBruijnGraph g = (DeBruijnGraph) this.clone();

		while (g.vertexSet().size() != 0) {
			String start = null, end = null;
			int startIndex = -1;
			if (path.size() == 0) {
				Set<String> imbalanced = g.getImbalancedVertices();
				if (imbalanced.size() != 2) {
					if (verbose) {
						System.err.println("Imbalanced graph. Aborting...");
					}
					throw new MbiException("Imbalanced graph given");
				}
				for (String vert : imbalanced) {
					if (g.inDegreeOf(vert) < g.outDegreeOf(vert)) {
						start = vert;
					} else if (g.inDegreeOf(vert) > g.outDegreeOf(vert)) {
						end = vert;
					}
				}
				assert (start != null && end != null && !start.equals(end));
			} else { // that is path is not null
				for (String vert : g.vertexSet()) {

					startIndex = path.lastIndexOf(vert);
					if (startIndex >= 0) {
						path.remove(startIndex);
						start = vert;
						break;
					}
				}
			}
			while (start != null) {
				if (startIndex == -1) {
					path.add(start);
				} else {
					path.add(startIndex++, start);
				}
				Set<String> directions = g.outgoingEdgesOf(start);
				if (directions.size() > 0) {
					String direction = directions.toArray(new String[directions
							.size()])[0];
					start = g.getEdgeTarget(direction);
					g.removeEdge(direction);
				} else {
					start = null;
				}
			}
			// remove stranded vertices - could have maintained a list of used
			// vertices...
			Set<String> vertsToRemove = new HashSet<String>();
			for (String vert : g.vertexSet()) {
				if (g.inDegreeOf(vert) == 0 && g.outDegreeOf(vert) == 0) {
					vertsToRemove.add(vert);
				}
			}
			for (String vert : vertsToRemove) {
				g.removeVertex(vert);
			}
		}

		return path;
	}
}

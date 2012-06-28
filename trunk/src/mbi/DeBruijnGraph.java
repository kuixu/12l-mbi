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

	// TODO: figure out a way to do it.
	public void balance() {

	}
	
	public Set<String> getImbalancedVertices() {
		Set<String> vers = new HashSet<String>();
		for(String vert : this.vertexSet()){
			if(this.inDegreeOf(vert)!=this.outDegreeOf(vert)){
				vers.add(vert);
			}
		}
		return vers;
	}

	public String assemble(int patience, boolean verbose) {
		Set<String> imbalancedVertices = getImbalancedVertices();
		if (imbalancedVertices.size() != 2) {
			if(verbose){
				System.err.println("Imbalanced graph. Aborting");
			}
			return null;
		}
		String start = null, end = null;
		// holds the inDegrees
		Map<String, Integer> inDegrees = new HashMap<String, Integer>();
		for (String v : this.vertexSet()) {
			inDegrees.put(v, this.inDegreeOf(v));
		}

		// determine the start and end of graph traversal
		for (String v : imbalancedVertices) {
			if (this.inDegreeOf(v) > this.outDegreeOf(v)) {
				end = v;
			} else {
				start = v;
			}
		}
		if(start==null || end==null){
			return null;
		}

		List<List<String>> paths = new LinkedList<List<String>>();
		String current = start;

		if (inDegrees.get(start) == 0) {
			inDegrees.remove(start);
		}

		int inDegreesOldSize = inDegrees.size();
		
		while (!inDegrees.isEmpty() && patience >0) {
			paths.add(new LinkedList<String>());
			while (current != null) {
				paths.get(paths.size() - 1).add(current);
				current = getNextStep(this, inDegrees, current);
			}
			current = determineCrossing(paths.get(paths.size() - 1), inDegrees);
			patience-=(inDegreesOldSize==inDegrees.size()?1:0);
			inDegreesOldSize=inDegrees.size();
		}

		//System.out.println("PATHS: "+paths.toString());
		if(patience>0){
			return getGenome(paths);
		}else{
			return null;
		}
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
				g.removeEdge(edge);
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
		for(int i=0; i<result.size()-1; ++i){
			str.append(result.get(i).substring(0,1));
		}
		str.append(result.get(result.size()-1));
		return str.toString();
	}

}

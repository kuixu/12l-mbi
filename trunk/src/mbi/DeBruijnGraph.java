package mbi;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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

	
	public synchronized List<String> findEulerPath(boolean verbose){
		List<String> path = new LinkedList<String>();
		DeBruijnGraph g = (DeBruijnGraph)this.clone();

		while (g.vertexSet().size() != 0) {
			String start = null, end = null;
			int startIndex=-1;
			if (path.size() == 0) {
				Set<String> imbalanced = g.getImbalancedVertices();
				if (imbalanced.size() != 2) {
					if (verbose) {
						System.err.println("Imbalanced graph. Aborting...");
					}
					return null;
				}
				for (String vert : imbalanced) {
					if (g.inDegreeOf(vert) < g.outDegreeOf(vert)) {
						start = vert;
					} else if (g.inDegreeOf(vert) > g.outDegreeOf(vert)) {
						end = vert;
					}
				}
				assert (start != null && end != null && !start.equals(end));
			}else{ //that is path is not null
				for(String vert : g.vertexSet()){

					startIndex = path.lastIndexOf(vert);
					if(startIndex>=0){
						path.remove(startIndex);
						start=vert;
						break;
					}
				}
			}
			while (start != null) {
				if(startIndex==-1){
					path.add(start);
				}else{
					path.add(startIndex++, start);
				}
				Set<String> directions = g.outgoingEdgesOf(start);
				if (directions.size() > 0) {
					String direction = directions.toArray(new String[directions
							.size()])[0];
					start = g.getEdgeTarget(direction);
					g.removeEdge(direction);
				} else {
					start=null;
				}
			}
			//remove stranded vertices - could have maintained a list of used vertices...
			Set<String> vertsToRemove = new HashSet<String>();
			for(String vert : g.vertexSet()){
				if(g.inDegreeOf(vert)==0 && g.outDegreeOf(vert)==0){
					vertsToRemove.add(vert);
				}
			}
			for(String vert : vertsToRemove){
				g.removeVertex(vert);
			}
		}
		
		return path;
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

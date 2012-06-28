package mbi.mock;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JApplet;

import mbi.DeBruijnEdgeFactory;

import org.jgraph.JGraph;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.GraphConstants;
import org.jgrapht.DirectedGraph;
import org.jgrapht.Graph;
import org.jgrapht.ext.JGraphModelAdapter;
import org.jgrapht.graph.DirectedMultigraph;

public class GraphExperimental extends JApplet {

	private static final Color DEFAULT_BG_COLOR = Color.decode("#FAFBFF");
	private static final Dimension DEFAULT_SIZE = new Dimension(1200, 900);

	public void init() {
		String sequence = "ABRAKADABRA";
		//String[] reads = {"EKRANA","NASAKR","KRANOS","KRANAN"};
		Set<String> readSet = new HashSet<String>();
		int k = 3;
//		String sequence = generateSequence(40);
//		String sequence = "GACAAAAGTCCTAGTAATCG";
		readSet = safeShotgun(sequence, 5);

		System.out.println("SHOTGUN product: " + readSet.toString());

//		 for(String read : reads){
//			 readSet.add(read);
//		 }
		DirectedGraph<String, String> graph = getTheGraph(readSet, k);
		String result = assemble(graph);
		System.out.println("No of READS: " + readSet.size());
		System.out.println("GENERATED "
				+ (sequence.equals(result) ? "eqals" : "differs from")
				+ " RESULT");
		System.out.println(sequence);
		System.out.println(result);
		if (result != null) {
			System.out.println("GENERATED "
					+ (sequence.indexOf(result, 0) > -1 ? "contains"
							: "doesn't contain") + " RESULT");
			System.out.println("RESULT "
					+ (result.indexOf(sequence, 0) > -1 ? "contains"
							: "doesn't contain") + " GENERATED");
		}
		JGraphModelAdapter<String, String> jGraphAdapter = new JGraphModelAdapter<String, String>(
				graph);
		JGraph jGraph = new JGraph(jGraphAdapter);

		//adjustDisplaySettings(jGraph);
		getContentPane().add(jGraph);
		resize(DEFAULT_SIZE);

		//distributeVertices(jGraphAdapter, graph);
	}

	/**
	 * 
	 * @param length
	 *            the length of desired sequence
	 * @return
	 */

	public static String generateSequence(int length) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < length; ++i) {
			double rand = Math.random();
			if (rand > 0.75) {
				builder.append("C");
			} else if (rand > 0.5) {
				builder.append("G");
			} else if (rand > 0.25) {
				builder.append("T");
			} else {
				builder.append("A");
			}
		}
		return builder.toString();
	}

	/**
	 * @param dna
	 *            input sequence
	 * @param k
	 *            length of desired k-mer
	 * @param n
	 *            number of reads
	 * @return
	 */
	public static Set<String> shotgun(String dna, int k, int n) {
		Set<String> results = new HashSet<String>();
		for (int i = 0; i < n; ++i) {
			int startIndex = (int) (Math.random() * dna.length());
			String sample = (dna.substring(startIndex,
					Math.min(startIndex + k, dna.length())));
			if (sample != null && sample.length() >= k) {
				results.add(sample);
			}
		}
		return results;
	}

	public static Set<String> safeShotgun(String dna, int k) {
		Set<String> results = new HashSet<String>();
		for (int startIndex = 0, endIndex = k; startIndex < dna.length() - k; startIndex += (int) (Math
				.random() * k / 2), endIndex = startIndex + k) {

			results.add(dna.substring(startIndex, endIndex));
		}
		results.add(dna.substring(dna.length() - k, dna.length()));
		return results;
	}

	/**
	 * 
	 * @param reads
	 *            Set of reads to print
	 */
	public static void printReads(Set<String> reads) {
		for (String k : reads) {
			System.out.println(k);
		}
	}

	public static DirectedGraph<String, String> getTheGraph(Set<String> reads,
			int k) {
		DirectedMultigraph<String, String> graph = new DirectedMultigraph<String, String>(new DeBruijnEdgeFactory());
		((DeBruijnEdgeFactory)graph.getEdgeFactory()).setGraph(graph);
		Map<String, Integer> repeats = new HashMap<String, Integer>();
		List<String> reps = new LinkedList<String>();
		List<String> cntx = new LinkedList<String>();
		for (String read : reads) {
			if (k > read.length()) {
				continue;
			} else {
				for (int s = 0, e = k; e <= read.length(); ++s, ++e) {
					// System.out.println(read.substring(s, e));
					String lo = read.substring(s, e - 1);
					String ld = read.substring(s + 1, e);
					if (!graph.containsVertex(lo)) {
						graph.addVertex(lo);
					}
					if (!graph.containsVertex(ld)) {
						graph.addVertex(ld);
					}
					if (!graph.containsEdge(lo, ld)) {
						graph.addEdge(lo, ld);
						// System.out.println("adding edge: "+lo+"-"+ld);
					} else {
						if (!repeats.containsKey(graph.getEdge(lo, ld))) {
							// if()
							//graph.addEdge(lo, ld);
							repeats.put(graph.getEdge(lo, ld), 0);
							reps.add(graph.getEdge(lo, ld));
							cntx.add(read);
						}
						repeats.put(graph.getEdge(lo, ld),
								repeats.get(graph.getEdge(lo, ld)) + 1);
					}
				}
			}
		}
		System.out.println("REPEATS: " + repeats);
		
		
		//for(int i=0; i<cntx.size(); ++i){
		//	graph.containsEdge(arg0)
		//}
		
		//Set<String> imbalancedVerts = getUnbalancedVertices(graph);
		//while(imbalancedVerts.size()!=2){
		//	
		//}
		
//		boolean keepTrying = false;
//		while(keepTrying){
//			//Set<String> imbalancedVerts = getUnbalancedVertices(graph);
//			keepTrying=false;
//			for(String edge : repeats.keySet()){
//				String sv = edge.substring(0, edge.length()-1);
//				String ev = edge.substring(1, edge.length());
//				if(graph.outDegreeOf(sv)<graph.inDegreeOf(sv)
//						|| graph.inDegreeOf(ev)<graph.outDegreeOf(ev)){
//					graph.addEdge(sv, ev);
//					if(repeats.get(edge)==1){
//						repeats.remove(edge);
//					}else{
//						repeats.put(edge, repeats.get(edge)-1);
//					}
//					keepTrying=true;
//					System.out.println("Inserted: "+edge);
//					break;
//				}
//			}
//		}
		
		return graph;
	}

	// SOME STUFF FOR JGRAPH ADAPTER

	
	
	


	/**
	 * Returns a set of vertices violating the balance of graph
	 * 
	 * @param graph
	 * @return
	 */
	public static Set<String> getUnbalancedVertices(Graph<String, String> graph) {
		Map<String, Integer> stats = new HashMap<String, Integer>();

		for (String v : graph.vertexSet()) {
			stats.put(v, 0);
		}

		for (String edge : graph.edgeSet()) {
			stats.put(graph.getEdgeSource(edge),
					stats.get(graph.getEdgeSource(edge)) + 1);
			stats.put(graph.getEdgeTarget(edge),
					stats.get(graph.getEdgeTarget(edge)) - 1);
		}

		Set<String> uv = new HashSet<String>();
		for (String key : stats.keySet()) {
			if (stats.get(key) != 0) {
				uv.add(key);
			}
		}
		return uv;
	}

	private String assemble(DirectedGraph<String, String> graph) {
		Set<String> unbalancedVertices = getUnbalancedVertices(graph);
		for (String uv : unbalancedVertices) {
			System.out.println("UNBALANCED VERTEX: " + uv + " balance: "
					+ (graph.outDegreeOf(uv) - graph.inDegreeOf(uv)));
		}
		if (unbalancedVertices.size() != 0 && unbalancedVertices.size() != 2) {
			System.out.println("Unbalanced graph given. Aborting...");
			return null;
		}
		String start = null, end = null;
		// holds the inDegrees
		Map<String, Integer> inDegrees = new HashMap<String, Integer>();
		for (String v : graph.vertexSet()) {
			inDegrees.put(v, graph.inDegreeOf(v));
		}

		// determine the start and end of graph traversal
		for (String v : unbalancedVertices) {
			if (graph.inDegreeOf(v) > graph.outDegreeOf(v)) {
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
				current = getNextStep(graph, inDegrees, current);
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
	// private void balanceGraph(Graph<String, String> graph){
	// for()
	// }
	
	
	private DirectedGraph<String, String> replaceSourceVertex (DirectedGraph<String, String> graph, String vertOld, String vertNew) {
		Set inEdgV1  = graph.incomingEdgesOf(vertOld);
		graph.addVertex(vertNew);
		for (Iterator it = inEdgV1.iterator(); it.hasNext();) {
	   
			String e = (String) it.next();
			String v = graph.getEdgeSource(e);
			System.out.println("new edge "+v+ " "+vertNew);
			graph.addEdge(v, vertNew);
		}
		graph.removeVertex(vertOld);
		return graph;
	}
    
	private DirectedGraph <String, String> replaceTargetVertex (DirectedGraph<String, String> graph, String vertOld, String vertNew) {
		Set inEdgV1  = graph.outgoingEdgesOf(vertOld);
		for (Iterator it = inEdgV1.iterator(); it.hasNext();) {

			String e = (String) it.next();
			String v = graph.getEdgeTarget(e);
			System.out.println("new edge "+vertNew+ " "+v);
			System.out.println(graph.addEdge(vertNew, v));
		}
		graph.removeVertex(vertOld);
		return graph;
	}
    
    private String contactVertexes ( String v1, String v2) {
        String newVert = "";
        if (v1.length() > v2.length()) {
           newVert = v1.concat(v2.substring(v2.length()));
        }
        else {
            newVert = v1.substring(0,1).concat(v2.substring(0, v2.length()));
        }
        return newVert;
    }
    
    private DirectedGraph<String, String>  simplify(DirectedGraph<String, String> graph) {
        boolean wasNewVert = true;
        while(wasNewVert){
            wasNewVert = false;
            Set<String> tmpVert =  graph.vertexSet();
            String certArr [] = new String[tmpVert.size()];
            int i = 0;
            for (String tV : tmpVert) {
                certArr[i++] = tV;
            }
            
            for (String v1 : certArr) {
                if(graph.containsVertex(v1)) {
                    Set edgs = graph.outgoingEdgesOf(v1);
                    if (edgs.size() == 1) {
                        String e = (String) edgs.iterator().next();
                        String v2 = graph.getEdgeTarget(e);
                        Set edgs2 = graph.incomingEdgesOf(v2);

                        if (edgs2.size() == 1) {
                            
                            String newVert = contactVertexes(v1, v2);
                            System.out.println("Vert1 " + v1 + " Vert2 " +v2+ " to "+newVert);
                            graph = replaceSourceVertex(graph, v1, newVert);
                            graph = replaceTargetVertex(graph, v2, newVert);
                            wasNewVert = true;
                        }
                    }
                }
            }
        }
        return graph;
    }
	
}

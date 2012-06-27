package mbi.mock;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JApplet;

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
		String sequence = "EKRANASAKRANOS";
		String[] reads = {"EKRANA","NASAKR","KRANOS","KRANAN"};
		Set<String> readSet = new HashSet<String>();
		int k = 4;
//		String sequence = generateSequence(40);
//		String sequence = "GACAAAAGTCCTAGTAATCG";
//		readSet = safeShotgun(sequence, 25);

		System.out.println("SHOTGUN product: " + readSet.toString());

		 for(String read : reads){
			 readSet.add(read);
		 }
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

		adjustDisplaySettings(jGraph);
		getContentPane().add(jGraph);
		resize(DEFAULT_SIZE);

		distributeVertices(jGraphAdapter, graph);
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
						}
						repeats.put(graph.getEdge(lo, ld),
								repeats.get(graph.getEdge(lo, ld)) + 1);
					}
				}
			}
		}
		System.out.println("REPEATS: " + repeats);
		return graph;
	}

	// SOME STUFF FOR JGRAPH ADAPTER

	
	
	
	private void adjustDisplaySettings(JGraph jg) {
		jg.setPreferredSize(DEFAULT_SIZE);

		Color c = DEFAULT_BG_COLOR;
		String colorStr = null;

		try {
			colorStr = getParameter("bgcolor");
		} catch (Exception e) {
		}

		if (colorStr != null) {
			c = Color.decode(colorStr);
		}

		jg.setBackground(c);
	}

	private void positionVertexAt(Object vertex, int x, int y,
			JGraphModelAdapter<String, String> m_jgAdapter) {
		DefaultGraphCell cell = m_jgAdapter.getVertexCell(vertex);
		Map attr = cell.getAttributes();
		Rectangle2D b = GraphConstants.getBounds(attr);

		GraphConstants.setBounds(attr, new Rectangle(x, y, (int) b.getWidth(),
				(int) b.getHeight()));

		Map cellAttr = new HashMap();
		cellAttr.put(cell, attr);
		m_jgAdapter.edit(cellAttr, null, null, null);
	}

	private void distributeVertices(JGraphModelAdapter<String, String> ga,
			Graph<String, String> g) {
		int vertices = g.vertexSet().size();
		double step = 2 * Math.PI / vertices;
		double counter = 0;
		double stdSpan = 400;
		for (String vertex : g.vertexSet()) {
			positionVertexAt(vertex, DEFAULT_SIZE.width / 2
					+ (int) (stdSpan * Math.cos(counter)), DEFAULT_SIZE.height
					/ 2 + (int) (stdSpan * Math.sin(counter)), ga);
			counter += step;
		}
	}

	/**
	 * Returns a set of vertices violating the balance of graph
	 * 
	 * @param graph
	 * @return
	 */
	private Set<String> getUnbalancedVertices(Graph<String, String> graph) {
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
}

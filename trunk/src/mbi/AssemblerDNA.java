package mbi;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Just a wrapper class for commonly
 */
public class AssemblerDNA {
	/**
	 * generates
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
	 * @param readLength
	 *            length of desired k-mer
	 * @param number
	 *            number of reads
	 * @return
	 */
	public static Set<String> shotgun(String dna, int readLength, int number) {
		Set<String> results = new HashSet<String>();
		for (int i = 0; i < number; ++i) {
			int startIndex = (int) (Math.random() * dna.length());
			String sample = (dna.substring(startIndex,
					Math.min(startIndex + readLength, dna.length())));
			if (sample != null && sample.length() >= readLength) {
				results.add(sample);
			}
		}
		return results;
	}

	/**
	 * Shotgun with limited overlaps
	 */

	public static Set<String> safeShotgun(String dna, int readLength) {
		Set<String> results = new HashSet<String>();
		for (int startIndex = 0, endIndex = readLength; startIndex < dna
				.length() - readLength; startIndex += (int) (Math.random()
				* readLength / 2), endIndex = startIndex + readLength) {

			results.add(dna.substring(startIndex, endIndex));
		}
		results.add(dna.substring(dna.length() - readLength, dna.length()));
		return results;
	}

	public static List<String> extractKmers(Set<String> reads, int k) {
		List<String> kmers = new LinkedList<String>();
		for (String read : reads) {
			if (k > read.length()) {
				continue;
			} else {
				for (int s = 0, e = k; e <= read.length(); ++s, ++e) {
					// System.out.println(read.substring(s, e));
					kmers.add(read.substring(s, e));
				}
			}
		}
		Collections.shuffle(kmers);
		return kmers;
	}

	/**
	 * No random overlaps, shotgun with ideal step (1). Returns a shuffled list
	 * of k-mers
	 * 
	 * @param dna
	 * @param k
	 * @return
	 */
	public static List<String> perfectShotgun(String dna, int k) {
		List<String> results = new LinkedList<String>();
		for (int i = 0; i <= dna.length() - k; ++i) {
			results.add(dna.substring(i, i + k));
		}
		Collections.shuffle(results);
		return results;
	}

	/**
	 * 
	 * @param str
	 *            Set of reads to print
	 */
	public static void printAll(Collection<String> str) {
		for (String k : str) {
			System.out.println(k);
		}
	}

	public static DeBruijnGraph getDeBruijnGraph(Collection<String> kmers,
			boolean allowRepeatedEdges) {
		DeBruijnGraph graph = new DeBruijnGraph(new DeBruijnEdgeFactory());
		for (String kmer : kmers) {
			int s = 0;
			int e = kmer.length();

			String lo = kmer.substring(s, e - 1);
			String ld = kmer.substring(s + 1, e);
			if (!graph.containsVertex(lo)) {
				graph.addVertex(lo);
			}
			if (!graph.containsVertex(ld)) {
				graph.addVertex(ld);
			}
			if (!graph.containsEdge(lo, ld) || allowRepeatedEdges) {
				graph.addEdge(lo, ld);
			}
		}
		return graph;

	}

	public static String attemptAssembly(List<String> kmers, int attempts,
			int attemptPatience, boolean verbose) {
		String result = null;
		while (attempts-- > 0 && result == null) {
			Collections.shuffle(kmers);
			DeBruijnGraph graph = getDeBruijnGraph(kmers, true);
			//graph=simplify(graph);
			result = graph.assemble(attemptPatience, verbose);
		}
		return result;
	}
	
	public static String assemble2(List<String> kmers, boolean verbose){
		DeBruijnGraph graph = getDeBruijnGraph(kmers, true);
		List<String> path = graph.findEulerPath(verbose);
		if(path!=null){
			return pathToGenome(path);
		}else{
			return null;
		}
	}
	
	public static String pathToGenome(List<String> path){
		StringBuilder sb = new StringBuilder();
		for(int i=0; i<path.size()-1; ++i){
			sb.append(path.get(i).substring(0, 1));
		}
		sb.append(path.get(path.size()-1));
		return sb.toString();
	}

	public static String readSequenceFromFile(String fileName) {
		String subStr = "";
		try {
			FileInputStream fstream = new FileInputStream(fileName);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			while ((strLine = br.readLine()) != null) {
				subStr += strLine;
			}
			in.close();
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());

		}
		return subStr.trim();

	}

	/**
	 * replace source vertex in chain with new vertex
	 * 
	 * @param graph
	 * @param vertOld
	 * @param vertNew
	 * @return
	 */
	private static DeBruijnGraph replaceSourceVertex(DeBruijnGraph graph,
			String vertOld, String vertNew) {
		Set<String> inEdgV1 = graph.incomingEdgesOf(vertOld);
		graph.addVertex(vertNew);
		String vertArr[] = new String[inEdgV1.size()];
		int i = 0;
		for (String e : inEdgV1) {
			vertArr[i++] = graph.getEdgeSource(e);
		}

		// copy all edges to new vertex
		for (String v : vertArr) {
			graph.addEdge(v, vertNew);
		}
		graph.removeVertex(vertOld);
		return graph;
	}

	/**
	 * replace target vertex in chain with new vertex
	 * 
	 * @param graph
	 * @param vertOld
	 * @param vertNew
	 * @return
	 */
	private static DeBruijnGraph replaceTargetVertex(DeBruijnGraph graph,
			String vertOld, String vertNew) {
		Set<String> outEdgV1 = graph.outgoingEdgesOf(vertOld);
		String vertArr[] = new String[outEdgV1.size()];
		int i = 0;
		for (String e : outEdgV1) {
			vertArr[i++] = graph.getEdgeTarget(e);
		}

		// copy all edges to new vertex
		for (String v : vertArr) {
			graph.addEdge(vertNew, v);
		}
		graph.removeVertex(vertOld);
		return graph;
	}

	/**
	 * contact two vertexes to one
	 * 
	 * @param v1
	 * @param v2
	 * @return
	 */
	private static String contactVertexes(String v1, String v2) {
		String newVert = "";
		if (v1.length() > v2.length()) {
			newVert = v1.concat(v2.substring(v2.length() - 1));
		} else {
			newVert = v1.substring(0, 1).concat(v2.substring(0, v2.length()));
		}
		return newVert;
	}

	/**
	 * Function transforms "chains" of vertexes to one longer vertex. Chains
	 * consitst vertexex with only one outgoingn and one ingoing edges.
	 * 
	 * @param graph
	 * @return simplified graph
	 */
	public static DeBruijnGraph simplify(DeBruijnGraph graph) {
		// System.out.println(graph.vertexSet().toString());
		// System.out.println(graph.edgeSet().toString());
		boolean wasNewVert = true;
		while (wasNewVert) {
			wasNewVert = false;
			Set<String> tmpVert = graph.vertexSet();
			String certArr[] = new String[tmpVert.size()];
			int i = 0;
			for (String tV : tmpVert) {
				certArr[i++] = tV;
			}

			for (String v1 : certArr) {
				// check if vertex is still in graph
				if (graph.containsVertex(v1)) {
					Set<String> edgs = graph.outgoingEdgesOf(v1);
					if (edgs.size() == 1) { // check if vertex has only one
											// outgoing edge
						String e = (String) edgs.iterator().next();
						String v2 = graph.getEdgeTarget(e);
						Set<String> edgs2 = graph.incomingEdgesOf(v2);

						if (edgs2.size() == 1) { // check if vertes on the end
													// of that edge, has only
													// one ingoing vertex

							String newVert = contactVertexes(v1, v2); // create
																		// longer
																		// vertex
																		// from
																		// two
																		// vertexes
							graph = replaceSourceVertex(graph, v1, newVert);
							graph = replaceTargetVertex(graph, v2, newVert);
							wasNewVert = true;
						}
					}
				}
			}
		}

//		Set<String> verts = graph.vertexSet();
//		for (String vert : verts.toArray(new String[verts.size()])) {
//			if (graph.outDegreeOf(vert) == 0 && graph.inDegreeOf(vert) == 0
//					&& graph.edgeSet().size() > 0) {
//				graph.removeVertex(vert);
//			}
//		}
		// System.out.println(graph.vertexSet().toString());
		// System.out.println(graph.edgeSet().toString());
		return graph;
	}
}

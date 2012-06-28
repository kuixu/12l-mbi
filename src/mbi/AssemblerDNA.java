package mbi;

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
		for (int startIndex = 0, endIndex = readLength; startIndex < dna.length() - readLength; startIndex += (int) (Math
				.random() * readLength / 2), endIndex = startIndex + readLength) {

			results.add(dna.substring(startIndex, endIndex));
		}
		results.add(dna.substring(dna.length() - readLength, dna.length()));
		return results;
	}
	
	
	public static List<String> extractKmers(Set<String> reads, int k){
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
	 * No random overlaps, shotgun with ideal step (1).
	 * Returns a shuffled list of k-mers
	 * @param dna
	 * @param k
	 * @return
	 */
	public static List<String> perfectShotgun(String dna, int k){
		List<String> results = new LinkedList<String>();
		for(int i=0; i<=dna.length()-k; ++i){
			results.add(dna.substring(i,i+k));
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
	
	
	public static DeBruijnGraph getDeBruijnGraph(Collection<String> kmers, boolean allowRepeatedEdges){
		DeBruijnGraph graph = new DeBruijnGraph(new DeBruijnEdgeFactory());
		for (String kmer : kmers) {
			int s=0;
			int e=kmer.length();
			
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
	
}

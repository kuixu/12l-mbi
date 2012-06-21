package mbi.mock;

import java.util.HashSet;
import java.util.Set;

public class GraphExperimental {
	
	public static void main(String[] args){
		String dna = generateSequence(1000000);
		System.out.println(dna);
		Set<String> reads = shotgun(dna, 39, 500000);
		printReads(reads);
	}
	
	/**
	 * 
	 * @param length the length of desired sequence
	 * @return
	 */
	
	public static String generateSequence(int length){
		StringBuilder builder = new StringBuilder();
		for(int i=0; i<length; ++i){
			double rand = Math.random();
			if(rand>0.75){
				builder.append("C");
			}else if(rand>0.5){
				builder.append("G");
			}else if(rand>0.25){
				builder.append("T");
			}else{
				builder.append("A");
			}
		}
		return builder.toString();
	}
	
	/**
	 * @param dna input sequence
	 * @param k length of desired k-mer
	 * @param n number of reads
	 * @return
	 */
	public static Set<String> shotgun(String dna, int k, int n){
		Set<String> results = new HashSet<String>();
		for(int i=0; i<n; ++i){
			int startIndex = (int)(Math.random()*dna.length());
			results.add(dna.substring(startIndex, Math.min(startIndex+k, dna.length())));
		}
		return results;
	}

	/**
	 * 
	 * @param reads Set of reads to print
	 */
	public static void printReads(Set<String> reads){
		for(String k : reads){
			System.out.println(k);
		}
	}
}

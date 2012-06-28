package mbi;

import java.util.List;

public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String sequence = "SARKAFARKA";
		List<String> kmers = AssemblerDNA.perfectShotgun(sequence, 4);

		System.out.println(kmers.toString());

		DeBruijnGraph graph = AssemblerDNA.getDeBruijnGraph(kmers, true);
		GraphDisplay gDisplay = new GraphDisplay(graph);
		gDisplay.init();

		String result = graph.assemble();
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

	}

}

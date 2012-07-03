package mbi;

import java.awt.BorderLayout;
import java.util.Arrays;
import java.util.List;

import javax.swing.JApplet;
import javax.swing.JFrame;

public class Main {

	public static void printUsage() {
		System.out
				.println("Please provide one of the following sets of parameters:");
		System.out.println("-f <PATH> -k <K> - to read sequence form PATH");
		System.out
				.println("-r <LENGTH> -k <K> - to generate a random sequence of length <LENGTH>");
		System.out
				.println("benchmark (yes, just one word) - run some built-in tests");
		System.out
				.println("#######################################################################");
		System.out
				.println("PATH - path to read the file containing the sequence fro");
		System.out.println("LENGTH - length of the sequence to be generated");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		if (args.length == 0) {
			printUsage();
			System.exit(1);
		}

		String path = null;
		int randLength = -1;
		String sequence = "ABRAKADABRA";
		int k = 5;

		for (int i = 0; i < args.length - 1; ++i) {
			if (args[i].equals("-f")) {
				path = args[i + 1];
			} else if (args[i].equals("-r")) {
				randLength = Integer.parseInt(args[i + 1]);
			} else if (args[i].equals("-k")) {
				k = Integer.parseInt(args[i + 1]);
			}
		}

		if (Arrays.binarySearch(args, "benchmark") >= 0) {
			benchmark();
			return;
		}

		if (randLength > 0) {
			sequence = AssemblerDNA.generateSequence(randLength);
		} else {
			sequence = AssemblerDNA.readSequenceFromFile(path);
		}

		List<String> kmers = AssemblerDNA.idealShotgun(sequence, k);

		DeBruijnGraph graph = AssemblerDNA.getDeBruijnGraph(kmers, true);
		// graph = AssemblerDNA.simplify(graph);

		JApplet grApphlet = new GrApphlet();
		((GrApphlet) grApphlet).setGraph(graph);

		JFrame mainFrame = new JFrame("12l-mbi");
		grApphlet.init();
		mainFrame.add(grApphlet, BorderLayout.CENTER);
		mainFrame.pack();
		mainFrame.setVisible(true);
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		System.out.println("K-MERS: " + kmers.toString());
		System.out.println("input:  " + sequence);
		try {
			String result = AssemblerDNA.assemble(kmers, true);
			System.out.println("INPUT "
					+ (sequence.equals(result) ? "equals" : "differs from")
					+ " RESULT");
			System.out.println("result: " + result);
			if (result != null) {
				System.out.println("INPUT "
						+ (sequence.indexOf(result, 0) > -1 ? "contains"
								: "doesn't contain") + " RESULT");
				System.out.println("RESULT "
						+ (result.indexOf(sequence, 0) > -1 ? "contains"
								: "doesn't contain") + " GENERATED");
			}
		} catch (MbiException e) {
			System.out.println("Exception: " + e.getMessage());
		}

	}

	public static void benchmark() {
		for (int length = 15; length < 10000; length *= 1.5) {
			for (int k = 3; k < 20; ++k) {
				int successes = 0;
				long levelTotalTime = 0;
				for (int test = 0; test < 100; ++test) {
					String sequence = AssemblerDNA.generateSequence(length);
					long startTime = System.currentTimeMillis();
					try {
						String result = AssemblerDNA.assemble(
								AssemblerDNA.idealShotgun(sequence, k), false);
						if (sequence.equals(result)) {
							++successes;
						}
					} catch (MbiException e) {

					} finally {
						levelTotalTime += (System.currentTimeMillis() - startTime);
					}
				}
				System.out.println("Length: " + length + ", K: " + k
						+ ", success ratio: " + successes / (double) 100
						+ ", avg. time: " + levelTotalTime / (double) (100)
						+ " msec.");
			}
		}
	}
}

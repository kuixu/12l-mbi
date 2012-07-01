package mbi;

import java.awt.BorderLayout;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JApplet;
import javax.swing.JFrame;

public class Main {

	public static void printUsage() {
		System.out
				.println("Please provide one of the following sets of parameters:");
		System.out
				.println("-f <PATH> -k <K> -p <PATIENCE> -t <TRIALS> - to read sequence form PATH");
		System.out
				.println("-r <LENGTH> -k <K> -p <PATIENCE> -t <TRIALS> - to generate a random sequence of length <LENGTH>");
		System.out
				.println("benchmark (yes, just one word) - run some auto tests");
		System.out
				.println("#######################################################################");
		System.out
				.println("PATH - path to read the file containing the sequence fro");
		System.out.println("LENGTH - length of the sequence to be generated");
		System.out
				.println("PATIENCE - max. number of iterations without progress while looking for path");
		System.out
				.println("TRIALS - max. number of restarts while assembling the sequence");
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
		int patience = 10;
		int trials = 20;
		int k = 5;

		for (int i = 0; i < args.length - 1; ++i) {
			if (args[i].equals("-f")) {
				path = args[i + 1];
			} else if (args[i].equals("-r")) {
				randLength = Integer.parseInt(args[i + 1]);
			} else if (args[i].equals("-p")) {
				patience = Integer.parseInt(args[i + 1]);
			} else if (args[i].equals("-t")) {
				trials = Integer.parseInt(args[i + 1]);
			} else if (args[i].equals("-k")) {
				k = Integer.parseInt(args[i + 1]);
			} 
		}

		if (Arrays.binarySearch(args, "benchmark")>=0) {
			benchmark(patience, trials);
			return;
		}

		if (randLength > 0) {
			sequence = AssemblerDNA.generateSequence(randLength);
		} else {
			sequence = AssemblerDNA.readSequenceFromFile(path);
		}

		List<String> kmers = AssemblerDNA.perfectShotgun(sequence, k);
		
		DeBruijnGraph graph = AssemblerDNA.getDeBruijnGraph(kmers, true);
		//graph = AssemblerDNA.simplify(graph);
		
		JApplet grApphlet = new GrApphlet();
		((GrApphlet)grApphlet).setGraph(graph);
		
		
		 JFrame mainFrame = new JFrame("12l-mbi");
		 grApphlet.init();	
		 mainFrame.add(grApphlet, BorderLayout.CENTER);
		 mainFrame.pack();
		 mainFrame.setVisible(true);
		 mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		System.out.println("K-MERS: "+kmers.toString());
		System.out.println("input:  "+sequence);
		//String result = AssemblerDNA.attemptAssembly(kmers, trials, patience, true);
		String result = AssemblerDNA.assemble2(kmers, true);
		System.out.println("INPUT "
				+ (sequence.equals(result) ? "equals" : "differs from")
				+ " RESULT");
		System.out.println("result: "+result);
		if (result != null) {
			System.out.println("INPUT "
					+ (sequence.indexOf(result, 0) > -1 ? "contains"
							: "doesn't contain") + " RESULT");
			System.out.println("RESULT "
					+ (result.indexOf(sequence, 0) > -1 ? "contains"
							: "doesn't contain") + " GENERATED");
		}

	}

	public static void benchmark(int pat, int tri) {
		for (int length = 15; length < 10000; length *= 1.2) {
			for (int k = 3; k < 20; ++k) {
				int successes = 0;
				for (int test = 0; test < 100; ++test) {
					String sequence = AssemblerDNA.generateSequence(length);
					String result = AssemblerDNA.attemptAssembly(
							AssemblerDNA.perfectShotgun(sequence, k), tri, pat, false);
					if (sequence.equals(result)) {
						++successes;
					}
				}
				System.out.println("Length: " + length + ", K: " + k
						+ " success ratio: " + successes / (double) 100);
			}
		}
	}
}

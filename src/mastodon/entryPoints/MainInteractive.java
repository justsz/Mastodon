package mastodon.entryPoints;
/**
 * 
 */

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import mastodon.algorithms.*;
import mastodon.core.*;

import jebl.evolution.io.ImportException;
import jebl.evolution.taxa.Taxon;
import jebl.evolution.trees.RootedTree;
import jebl.evolution.trees.SimpleRootedTree;

/**
 * @author justs
 *
 */
public class MainInteractive {

	/**
	 * @param args
	 * @throws ImportException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException, ImportException {
		Scanner scanner = new Scanner(System.in);
		TreeReader reader = new TreeReader();
		BitTreeSystem bts = new BitTreeSystem();
		MHLinearAlgorithm algorithm = new MHLinearAlgorithm();
		boolean inputOk = false;	//used in input-validation loops
		double minScore = 0;
		int maxPrune = 0;
		int maxIterations = 0;


		System.out.println("Welcome to MASTadon.");
		System.out.println("----");
		
		System.out.print("Please input filename for tree set: ");
		while (!inputOk) {
			String fileName = scanner.nextLine();
			try {
				reader.setFile(fileName);
				inputOk = true;
			} catch (IOException e) {
				System.out.println("File not found. Try again.");
				inputOk = false;
			}
		}
		System.out.println("----");
		
		System.out.println("Loading trees...");
		int treeCounter = 0;
		List<RootedTree> trees;		
		do {
			trees = reader.read100RootedTrees();
			bts.addTrees(trees);
			treeCounter += trees.size();
			if (trees.size() != 0)
				System.out.println(treeCounter + "..");
		} while (trees.size() == 100);
		trees = null;
		System.out.println("----");
		System.out.println("Found " + bts.getBitTrees().size() + " trees with " + bts.getTaxaCount() + " unique taxa.");

		System.out.println("Please input the following parameters:");

		boolean anotherRun = true;	//used to let the user do multiple runs with same data set
		int runCounter = 0;
		while(anotherRun) {
			runCounter++;
			inputOk = false;
			System.out.println("Desired minimum MAP score (0.0 - 1.0): ");
			while (!inputOk) {
				try {
					String input = scanner.nextLine();
					minScore = Double.parseDouble(input);

					if(minScore < 0.0 || minScore > 1.0) {
						System.out.println("Invalid range. Try again.");
					} else {
						inputOk = true;
					}
				} catch (Exception e) {
					System.out.println("Invalid input. Decimal required. Try again.");
				}
			}


			inputOk = false;
			System.out.println("Maximum number of taxa to prune (1 - " + bts.getTaxaCount() + "): ");
			while (!inputOk) {
				try {
					String input = scanner.nextLine();
					maxPrune = Integer.parseInt(input);

					if(maxPrune < 1 || maxPrune > bts.getTaxaCount()) {
						System.out.println("Invalid range. Try again.");
					} else {
						inputOk = true;
					}
				} catch (Exception e) {
					System.out.println("Invalid input. Decimal required. Try again.");
				}
			}


			inputOk = false;
			System.out.println("Maximum number of iterations (1+): ");
			while (!inputOk) {
				try {
					String input = scanner.nextLine();
					maxIterations = Integer.parseInt(input);

					if(maxIterations < 1) {
						System.out.println("Invalid range. Try again.");
					} else {
						inputOk = true;
					}
				} catch (Exception e) {
					System.out.println("Invalid input. Decimal required. Try again.");
				}
			}

			System.out.println("I haven't been updated after refactoring.");
//			algorithm.setTrees(bts, bts.getBitTrees());
//			algorithm.setLimits(minScore, maxPrune, maxIterations);
			algorithm.run();

			Map<ArrayList<Taxon>, double[]> result = new HashMap<ArrayList<Taxon>, double[]>(); 
//					algorithm.getTaxa();

			BufferedWriter out = new BufferedWriter(new FileWriter("run" + runCounter + ".txt"));
			out.write("Pruned taxa\t[MAP score for this pruning, number of matching subtrees]\n----\n");

			for(ArrayList<Taxon> taxaList : result.keySet()) {
				for (Taxon taxon : taxaList) {
					out.write(taxon.getName() + ", ");
				}
				out.write("[" + result.get(taxaList)[0] + ", " + (int) result.get(taxaList)[1] + "]\n");
			}

			out.close();
			
			//List<SimpleRootedTree> prunedTrees = algorithm.getHighlightedPrunedMapTrees();
			NexusWriter writer = new NexusWriter("prunedTrees" + runCounter + ".trees");
			//writer.writeTrees(prunedTrees);
			
			System.out.println("Output saved as run" + runCounter + ".txt and prunedTrees" + runCounter + ".trees");

			System.out.println("Would you like to run the algorithm again? y/n : ");
			inputOk = false;
			while (!inputOk) {
				String choice = scanner.nextLine();
				if (choice.toUpperCase().equals("N")) {
					anotherRun = false;
					inputOk = true;
				} else if (choice.toUpperCase().equals("Y")) {
					anotherRun = true;
					inputOk = true;
				}
			}
		}
	}
}

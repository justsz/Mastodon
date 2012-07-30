package mastodon.entryPoints;
/**
 * 
 */

import java.io.BufferedWriter;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


import mastodon.algorithms.*;
import mastodon.core.*;

import org.apache.commons.cli.*;

import jebl.evolution.io.ImportException;
import jebl.evolution.taxa.Taxon;
import jebl.evolution.trees.RootedTree;
import jebl.evolution.trees.SimpleRootedTree;

/**
 * CLI that uses Apache libraries. Replaced by MainCLI2 which uses Arguments.
 * @author justs
 *
 */
public class MainCLI {

	/**
	 * @param args
	 * @throws ImportException 
	 * @throws IOException 
	 * @throws ParseException 
	 */
	public static void main(String[] args) throws IOException, ImportException, ParseException {


		Options options = new Options(); 
		options.addOption("f", true, "filename of dataset to analyse");
		options.addOption("s", true, "desired minimum MAP score");
		options.addOption("p", true, "maximum number of taxa to prune");
		options.addOption("i", true, "maximum number of iterations");


		CommandLineParser parser = new PosixParser();

		CommandLine cmd = parser.parse(options, args);
		System.out.println(cmd.getOptionValue("f"));
		System.out.println(cmd.getOptionValue("s"));
		System.out.println(cmd.getOptionValue("p"));
		System.out.println(cmd.getOptionValue("i"));



		TreeReader reader = new TreeReader();
		BitTreeSystem bts = new BitTreeSystem();
		MHLinearAlgorithm algorithm = new MHLinearAlgorithm();
		//boolean inputOk = false;	//used in input-validation loops
		double minScore = 0;
		int maxPrune = 0;
		int maxIterations = 0;



		try {
			reader.setFile(cmd.getOptionValue("f"));
		} catch (IOException e) {
			System.out.println("File not found.");
			System.exit(1);
		}		
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

		//boolean anotherRun = true;	//used to let the user do multiple runs with same data set
		int runCounter = 0;
		//while(anotherRun) {
		//runCounter++;



		try {
			minScore = Double.parseDouble(cmd.getOptionValue("s"));

			if(minScore < 0.0 || minScore > 1.0) {
				System.out.println("Invalid MAP score range.");
				System.exit(1);
			}						
		} catch (Exception e) {
			System.out.println("Invalid MAP score. Decimal required.");
			System.exit(1);
		}


		try {
			maxPrune = Integer.parseInt(cmd.getOptionValue("p"));

			if(maxPrune < 1 || maxPrune > bts.getTaxaCount()) {
				System.out.println("Invalid maximum number to prune range.");
				System.exit(1);
			} 
		} catch (Exception e) {
			System.out.println("Invalid maximum number to prune input. Decimal required.");
			System.exit(1);
		}



		try {
			maxIterations = Integer.parseInt(cmd.getOptionValue("i"));

			if(maxIterations < 1) {
				System.out.println("Invalid max iteration range.");
				System.exit(1);
			}
		}  catch (Exception e) {
			System.out.println("Invalid max iteration input. Integer required.");
			System.exit(1);
		}


		System.out.println("I haven't been updated after refactoring.");
//		algorithm.setTrees(bts, bts.getBitTrees());
//		algorithm.setLimits(minScore, maxPrune, maxIterations);
		algorithm.run();

		
		List<ArrayList<Taxon>> prunedTaxa = algorithm.getRunResult().getPrunedTaxa();
		List<double[]> scores = algorithm.getRunResult().getPruningScores();

		BufferedWriter out = new BufferedWriter(new FileWriter("run" + runCounter + ".txt"));
		out.write("Pruned taxa\t[MAP score for this pruning, number of matching subtrees]\n----\n");

		for(int i = 0; i < prunedTaxa.size(); i++) {
			for (Taxon taxon : prunedTaxa.get(i)) {
				out.write(taxon.getName() + ", ");
			}
			out.write("[" + scores.get(i)[0] + ", " + (int) scores.get(i)[1] + "]\n");
		}

		out.close();

		List<SimpleRootedTree> prunedTrees = algorithm.getRunResult().getPrunedMapTrees();
		NexusWriter writer = new NexusWriter("prunedTrees" + runCounter + ".trees");
		writer.writeTrees(prunedTrees);

		System.out.println("Output saved as run" + runCounter + ".txt and prunedTrees" + runCounter + ".trees");

//		System.out.println("Would you like to run the algorithm again? y/n : ");


		//				String choice = scanner.nextLine();
		//				if (choice.toUpperCase().equals("N")) {
		//					anotherRun = false;
		//					inputOk = true;
		//				} else if (choice.toUpperCase().equals("Y")) {
		//					anotherRun = true;
		//					inputOk = true;
		//				}

		//}

	}
}

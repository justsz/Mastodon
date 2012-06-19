package entryPoints;
/**
 * 
 */

import java.io.BufferedWriter;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jebl.evolution.io.ImportException;
import jebl.evolution.taxa.Taxon;
import jebl.evolution.trees.SimpleRootedTree;

import algorithms.*;
import core.*;
import core.Arguments.ArgumentException;
import core.Arguments.Option;
import core.Arguments.*;
/**
 * @author justs
 *
 */
public class MainCLI2 {

	/**
	 * @param args
	 * @throws ImportException 
	 * @throws IOException 
	 * @throws ArgumentException 
	 * @throws ParseException 
	 */
	public static void main(String[] args) throws IOException, ImportException, ArgumentException {


		List<Option> options = new ArrayList<Option>();
		options.add(new Option("help", "display this page"));
		options.add(new StringOption("f", "s", "*filename of dataset to analyse"));
		options.add(new RealOption("s", 0.0, 1.0, "*desired minimum MAP score"));
		options.add(new IntegerOption("p", "*maximum number of taxa to prune"));
		options.add(new IntegerOption("i", 1, Integer.MAX_VALUE, "*maximum number of iterations"));

		Option[] optsArray = new Option[options.size()];
		for(int i = 0; i < options.size(); i++) {
			optsArray[i] = options.get(i);
		}

		Arguments cmd = new Arguments(optsArray);
		cmd.parseArguments(args);
		
		if(cmd.hasOption("help")) {
			cmd.printUsage("java -jar MASTodon.jar", "\nStarred entries are required.");
			System.exit(0);
		}
		
		if(!cmd.hasOption("f") || !cmd.hasOption("s") || !cmd.hasOption("p") || !cmd.hasOption("i")) {
			System.out.println("One or more required flags missing. Please refer to -help.");
			System.exit(1);
		}
	
		

//		System.out.println(cmd.getStringOption("f"));
//		System.out.println(cmd.getRealOption("s"));
//		System.out.println(cmd.getIntegerOption("p"));
//		System.out.println(cmd.getIntegerOption("i"));



		TreeReader reader = new TreeReader();
		BitTreeSystem bts = new BitTreeSystem();
		MHBitAlgorithm algorithm = new MHBitAlgorithm();
		float minScore = 0;
		int maxPrune = 0;
		int maxIterations = 0;



		try {
			reader.setFile(cmd.getStringOption("f"));
		} catch (IOException e) {
			System.out.println("File not found.");
			System.exit(1);
		}		
		System.out.println("Loading trees...");
		int treeCounter = 0;
		List<SimpleRootedTree> trees;		
		do {
			trees = reader.read100Trees();
			bts.addTrees(trees);
			treeCounter += trees.size();
			if (trees.size() != 0)
				System.out.println(treeCounter + "..");
		} while (trees.size() == 100);
		trees = null;
		System.out.println("----");
		System.out.println("Found " + bts.getBitTrees().size() + " trees with " + bts.getTaxaCount() + " unique taxa.");

		minScore = (float) cmd.getRealOption("s");
		
		maxPrune = cmd.getIntegerOption("p");
		if (maxPrune > bts.getTaxaCount()) {
			System.out.println("Cannot prune more taxa than there are in total.");
			System.exit(1);
		}

		maxIterations = cmd.getIntegerOption("i");


		algorithm.setTrees(bts, bts.getBitTrees());
		algorithm.setLimits(minScore, maxPrune, maxIterations);
		algorithm.run();

		Map<ArrayList<Taxon>, float[]> result = algorithm.getTaxa();
		
		String prefix = cmd.getStringOption("f").split("\\.")[0];
		
		BufferedWriter out = new BufferedWriter(new FileWriter(prefix + "PrunedTaxa.txt"));
		out.write("Pruned taxa\t[MAP score for this pruning, number of matching subtrees]\n----\n");

		for(ArrayList<Taxon> taxaList : result.keySet()) {
			for (Taxon taxon : taxaList) {
				out.write(taxon.getName() + ", ");
			}
			out.write("[" + result.get(taxaList)[0] + ", " + (int) result.get(taxaList)[1] + "]\n");
		}

		out.close();

		List<SimpleRootedTree> prunedTrees = algorithm.getHighlightedPrunedMapTrees();
		NexusWriter writer = new NexusWriter(prefix + "Pruned.trees");
		writer.writeTrees(prunedTrees);

		System.out.println("Output saved as " + prefix + "PrunedTaxa.txt and " + prefix + "Pruned.trees");
	}
}

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
import mastodon.core.Arguments.*;

import jebl.evolution.io.ImportException;
import jebl.evolution.taxa.Taxon;
import jebl.evolution.trees.SimpleRootedTree;

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
		options.add(new StringOption("n", "s", "stem of output files. Default is stem of input file"));
		options.add(new RealOption("s", 0.0, 1.0, "*desired minimum MAP score [0.0 - 1.0]"));
		options.add(new IntegerOption("p", 1, Integer.MAX_VALUE, "*maximum number of taxa to prune [1+]"));
		options.add(new IntegerOption("i", 1, Integer.MAX_VALUE, "*maximum number of iterations [1+]"));

		Option[] optsArray = new Option[options.size()];
		for(int i = 0; i < options.size(); i++) {
			optsArray[i] = options.get(i);
		}

		Arguments cmd = new Arguments(optsArray);
		cmd.parseArguments(args);

		if(cmd.hasOption("help")) {
			cmd.printUsage("java -jar MASTodon.jar", "\nStarred entries are required.");
			System.out.println("Example:  java -jar MASTodon.jar -s 0.8 -p 10 -i 20000 carnivores.trees");
			System.exit(0);
		}

		if(!cmd.hasOption("s") || !cmd.hasOption("p") || !cmd.hasOption("i")) {
			System.out.println("One or more required flags missing. Please refer to -help.");
			System.exit(1);
		}



		TreeReader reader = new TreeReader();
		BitTreeSystem bts = new BitTreeSystem();
		MHLinearAlgorithm algorithm = new MHLinearAlgorithm();
		double minScore = 0;
		int maxPrune = 0;
		int maxIterations = 0;


		String filename = "";
		if(cmd.getLeftoverArguments() != null) {
			filename = cmd.getLeftoverArguments()[0];
		}

		try {
			reader.setFile(filename);
		} catch (IOException e) {
			System.out.println("File " + filename + " not found.");
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

		minScore = (double) cmd.getRealOption("s");

		maxPrune = cmd.getIntegerOption("p");
		if (maxPrune > bts.getTaxaCount()) {
			System.out.println("Cannot prune more taxa than there are in total.");
			System.exit(1);
		}

		maxIterations = cmd.getIntegerOption("i");


		algorithm.setTrees(bts, bts.getBitTrees());
		algorithm.setLimits(minScore, maxPrune, maxIterations);
		algorithm.run();

		System.out.println();	//new line after progress report

		List<ArrayList<Taxon>> prunedTaxa = algorithm.getRunResult().getPrunedTaxa();
		List<double[]> scores = algorithm.getRunResult().getPruningScores();

		String prefix = "";

		if (cmd.hasOption("n")) {
			prefix = cmd.getStringOption("n");
		} else {
			String[] parts = filename.split("/");
			prefix = parts[parts.length - 1].split("\\.")[0];
		}

		BufferedWriter out = new BufferedWriter(new FileWriter(prefix + "PrunedTaxa.txt"));
		out.write("Pruned taxa\t[MAP score for this pruning, number of matching subtrees]\n----\n");
		System.out.println("Pruned taxa\t[MAP score for this pruning, number of matching subtrees]\n----");

		for(int i = 0; i < prunedTaxa.size(); i++) {
			for (Taxon taxon : prunedTaxa.get(i)) {
				out.write(taxon.getName() + ", ");
				System.out.print(taxon.getName() + ", ");
			}
			out.write("[" + scores.get(i)[0] + ", " + (int) scores.get(i)[1] + "]\n");
			System.out.print("[" + scores.get(i)[0] + ", " + (int) scores.get(i)[1] + "]\n");
		}

		out.close();

		List<SimpleRootedTree> prunedTrees = algorithm.getRunResult().getPrunedMapTrees();
		NexusWriter writer = new NexusWriter(prefix + "Pruned.trees");
		writer.writeTrees(prunedTrees);

		System.out.println("Output saved as " + prefix + "PrunedTaxa.txt and " + prefix + "Pruned.trees");
	}
}

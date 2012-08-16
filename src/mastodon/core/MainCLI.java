/* Copyright (C) 2012 Justs Zarins
 *
 *This file is part of MASTodon.
 *
 *MASTodon is free software: you can redistribute it and/or modify
 *it under the terms of the GNU Lesser General Public License as
 *published by the Free Software Foundation, either version 3
 *of the License, or (at your option) any later version.
 *
 *MASTodon is distributed in the hope that it will be useful,
 *but WITHOUT ANY WARRANTY; without even the implied warranty of
 *MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *GNU Lesser General Public License for more details.
 *
 *You should have received a copy of the GNU Lesser General Public License
 *along with this program.  If not, see http://www.gnu.org/licenses/.
 */

package mastodon.core;
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
import jebl.evolution.trees.RootedTree;
import jebl.evolution.trees.SimpleRootedTree;

/**
 * Command Line Interface (CLI) version of Mastodon that uses Arguments for input parsing. 
 * @author justs
 *
 */
public class MainCLI {

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
		options.add(new StringOption("stem", "s", "*stem of output files. Default is stem of input file"));
		options.add(new RealOption("score", 0.0, 1.0, "*desired minimum MAP score [0.0 - 1.0]"));
		options.add(new IntegerOption("iter", 1, Integer.MAX_VALUE, "*maximum number of iterations [1+]"));
		options.add(new Option("lin", "set search method to Linear between min and max K"));
		options.add(new Option("bis", "set search method to Bisection between min and max K"));
		options.add(new IntegerOption("minK", 1, Integer.MAX_VALUE, "*minimum number of taxa to prune [1+]"));
		options.add(new IntegerOption("maxK", 1, Integer.MAX_VALUE, "*maximum number of taxa to prune [1-all taxa]"));
		options.add(new Option("SA", "set algorithm to Simulated Annealing"));
		options.add(new Option("MH", "set algorithm to Metropolis Hastings"));
		options.add(new RealOption("initT", Double.MIN_VALUE, Double.MAX_VALUE, "initial temperature for SA [>0]"));
		options.add(new RealOption("finalT", Double.MIN_VALUE, Double.MAX_VALUE, "final temperature for SA [>0 and <initT]"));
		options.add(new RealOption("power", Double.MIN_VALUE, Double.MAX_VALUE, "weighing power for MH, higher number is more aggressive [>0]"));
		options.add(new StringOption("root", "s", "outgroup taxon to root against. Exclude if trees are already rooted"));
		options.add(new IntegerOption("burnin", 0, Integer.MAX_VALUE, "ignore first {burnin} trees [0+]"));

		Option[] optsArray = new Option[options.size()];
		for(int i = 0; i < options.size(); i++) {
			optsArray[i] = options.get(i);
		}

		Arguments cmd = new Arguments(optsArray);
		cmd.parseArguments(args);

		if(cmd.hasOption("help")) {
			cmd.printUsage("java -jar MASTodon.jar", "\nStarred entries are always required." +
													"\nYou should also specify a choice of search method and algorithm. ");
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

		minScore = (double) cmd.getRealOption("s");

		maxPrune = cmd.getIntegerOption("p");
		if (maxPrune > bts.getTaxaCount()) {
			System.out.println("Cannot prune more taxa than there are in total.");
			System.exit(1);
		}

		maxIterations = cmd.getIntegerOption("i");

		System.out.println("I haven't been updated after refactoring.");
//		algorithm.setTrees(bts, bts.getBitTrees());
//		algorithm.setLimits(minScore, maxPrune, maxIterations);
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
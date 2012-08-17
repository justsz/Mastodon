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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mastodon.algorithms.*;
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
		
		Launcher launcher = new Launcher(null, false);


		List<Option> options = new ArrayList<Option>();
		options.add(new Option("help", "display this page"));
		options.add(new StringOption("stem", "s", "stem of output files. Default is stem of input file"));
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
		options.add(new StringOption("outgroup", "s", "outgroup taxon to root against. Exclude if trees are already rooted"));
		options.add(new IntegerOption("burnin", 0, Integer.MAX_VALUE, "ignore first {burnin} trees [0+]. Default is 0"));
		options.add(new IntegerOption("repeat", 1, Integer.MAX_VALUE, "number of times to repeat algorithm [0+] (only best result will be shown). Default is 1"));

		Option[] optsArray = new Option[options.size()];
		for(int i = 0; i < options.size(); i++) {
			optsArray[i] = options.get(i);
		}

		Arguments cmd = new Arguments(optsArray);
		cmd.parseArguments(args);

		if(cmd.hasOption("help")) {
			cmd.printUsage("java -jar MASTodon.jar", "\nStarred entries are always required." +
													"\nYou should also specify a choice of search method and algorithm.");
			System.out.println("Example:  java -jar MASTodon.jar -lin -MH -repeat 1 -power 2 -score 0.75 -minK 20 -maxK 40 -iter 10000  carnivores1kUnWeighted.trprobs" +
					"\nThe above will search for common subtrees with MAP score 0.75 using the MH algorithm with weighing power 2, checking" +
					"\nnumber of taxa to prune from 20 to 40. This will be repeated 1 time for 10000 iterations.");
			System.exit(0);
		}
		
		System.out.println("---------\n" +
				"MASTodon CLI v0.3 Copyright Justs Zarins 2012\n" +
				"justs.zarins@gmail.com\n" +
				"http://informatics.nescent.org/wiki/PhyloSoC:_Summary_and_visualization_of_phylogenetic_tree_sets\n" +
							"This program is free to use and modify but comes WITHOUT WARRANTY.\n" +
							"Distributed under GNU Lesser GPL. See http://www.gnu.org/licenses/ for details.\n" +
							"---------");

		
		Map<String, Object> limits = new HashMap<String, Object>(); 
		
		Algorithm algorithm = null;
		
				
		limits.put("minPruning", cmd.getIntegerOption("minK"));
		limits.put("maxPruning", cmd.getIntegerOption("maxK"));
		limits.put("minMapScore", cmd.getRealOption("score"));
		limits.put("totalIterations", cmd.getIntegerOption("iter"));
		if (cmd.hasOption("lin")) {
			if (cmd.hasOption("SA")) {
				limits.put("initTemp", cmd.getRealOption("initT"));
				limits.put("finalTemp", cmd.getRealOption("finalT"));
				algorithm = new SALinearAlgorithm();
			} else if (cmd.hasOption("MH")) {
				limits.put("power", cmd.getRealOption("power"));
				algorithm = new MHLinearAlgorithm();
			} else {
				System.out.println("Algorithm not selected.");
				System.exit(1);
			}
		} else if (cmd.hasOption("bis")) {
			if (cmd.hasOption("SA")) {
				limits.put("initTemp", cmd.getRealOption("initT"));
				limits.put("finalTemp", cmd.getRealOption("finalT"));
				algorithm = new SABisectionAlgorithm();
			} else if (cmd.hasOption("MH")) {
				limits.put("power", cmd.getRealOption("power"));
				algorithm = new SABisectionAlgorithm();
			} else {
				System.out.println("Algorithm not selected.");
				System.exit(1);
			}
		} else {
			System.out.println("Search method not selected.");
			System.exit(1);
		}
		
		
		
		
		String outgroup = "";
		int burnin = 0;
		
		if (cmd.hasOption("outgroup")) {
			outgroup = cmd.getStringOption("outgroup");
		}
		
		if (cmd.hasOption("burnin")) {
			burnin = cmd.getIntegerOption("burnin");
		}


		String filename = "";
		if(cmd.getLeftoverArguments() != null) {
			filename = cmd.getLeftoverArguments()[0];
		}
		launcher.setFileName(filename);
	
		System.out.println("Loading trees...");

		launcher.processFile(burnin, outgroup);
		System.out.println("----");

		launcher.setupAlgorithm(algorithm, limits);
		
		int repeat = 1;
		if (cmd.hasOption("repeat")) {
			repeat = cmd.getIntegerOption("repeat");
		}
		
		launcher.runAlgorithm(repeat);
		RunResult runResult = launcher.getResults();

		System.out.println();	//new line after progress report

		List<ArrayList<Taxon>> prunedTaxa = runResult.getPrunedTaxa();
		List<double[]> scores = runResult.getPruningScores();
		List<SimpleRootedTree> prunedTrees = runResult.getPrunedMapTrees();

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

		
		NexusWriter writer = new NexusWriter(prefix + "Pruned.trees");
		writer.writeTrees(prunedTrees);

		System.out.println("Output saved as " + prefix + "PrunedTaxa.txt and " + prefix + "Pruned.trees");
	}
}

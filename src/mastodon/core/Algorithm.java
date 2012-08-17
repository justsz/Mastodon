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

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.math3.distribution.PoissonDistribution;

import jebl.evolution.taxa.Taxon;
import jebl.evolution.trees.SimpleRootedTree;

/**
 * This is the general layout that a pruning Algorithm has to follow. 
 * A subclass needs to implement the individual steps and then the superclass loops over them until a finishing condition is satisfied.	
 * @author justs
 *
 */
public abstract class Algorithm {
	protected Map<BitSet, double[]> finalPruning;	//the best pruning found
	protected String stub;	//name of the algorithm, numbering could be added to the stub for consequent runs

	protected double minMapScore;
	protected int totalIterations;
	protected Map<Integer, Integer> pruningFreq;	//map of how often each taxa appears in accepted pruning moves
	protected int totalPruningFreq;	//total number of accepted pruning moves
	
	protected int minPrunedSpeciesCount;
	protected int currPrunedSpeciesCount;
	protected int maxPrunedSpeciesCount;

	protected int iterationCounter = 0;

	protected BitTreeSystem bts;
	protected List<BitTree> bitTrees;
	
	private boolean forceStop = false;

	protected double[] maxScore;
	protected double[] prevScore;
	protected double[] currScore;
	protected Map<BitSet, double[]> maxScorePruning;
	protected BitSet prevPruning;
	protected BitSet currPruning;

	protected PoissonDistribution pd;	//used to find a "nearby step" in the space of taxa

	/**
	 * Set the BitTreeSystem to prune.
	 * @param bts - target BitTreeSystem
	 */
	public abstract void setBTS(BitTreeSystem bts);
	/**
	 * Set limits required by specific algorithm.
	 * @param limits - algorithm settings
	 */
	public abstract void setLimits(Map<String, Object> limits);	
	protected abstract void initialize();	
	protected abstract boolean finished();	//condition when the algorithm stops
	protected abstract void choosePruningCount();	//change the number of pruned taxa iteration to iteration, as required
	protected abstract void tryPruning();	//execute a pruning combination and see what score it produces
	protected abstract void setNewBest();	//decide whether to accept this pruning
	protected abstract void afterActions();	//execute some final commands after the algorithm has finished

	/**
	 * Execute the setup algorithm.
	 */
	public void run() {
		//add a check to see if everything has been set correctly
		initialize();
		totalPruningFreq = 0;

		while (!finished() && !forceStop) {
			choosePruningCount();
			tryPruning();
			setNewBest();
			iterationCounter++;
			if(iterationCounter % 500 == 0) {
				System.out.println("Iteration: " + iterationCounter + " Pruning count: " + currPrunedSpeciesCount + " Best MAP score: " + maxScore[0]);
			}
		}
		afterActions();		
		//System.out.println("Number of times a new step was accepted: " + totalPruningFreq);
	}

	/**
	 * Construct a RunResult based on the outcome of the previous run of the algorithm.
	 * @return the result of the algorithm
	 */
	public RunResult getRunResult() {
		List<ArrayList<Taxon>> prunedTaxa = new ArrayList<ArrayList<Taxon>>();
		List<BitSet> prunedTaxaBits = new ArrayList<BitSet>();
		List<double[]> pruningScores = new ArrayList<double[]>();
		List<SimpleRootedTree> prunedMapTrees = new ArrayList<SimpleRootedTree>();
		Map<Taxon, Double> pruningFrequencies = new HashMap<Taxon, Double>();
		
		for(Entry<Integer, Integer> entry : pruningFreq.entrySet()) {
			double freq = 0.0;
			if (totalPruningFreq == 0) {
				//System.out.println("division by zero? no thanks! [Algorithm class]");
			} else {
				freq = (double) entry.getValue() / totalPruningFreq;
			}
			pruningFrequencies.put(bts.getTaxon(entry.getKey()), freq);
		}

		for(Entry<BitSet, double[]> entry : finalPruning.entrySet()) {
			prunedTaxa.add((ArrayList<Taxon>) bts.getTaxa(entry.getKey()));
			prunedTaxaBits.add(entry.getKey());
			pruningScores.add(entry.getValue());
			prunedMapTrees.add(bts.reconstructMapTree(entry.getKey(), pruningFrequencies));
		}
		
		String name = stub;
		return new RunResult(bts, prunedTaxa, prunedTaxaBits, pruningScores, prunedMapTrees, pruningFrequencies, name, minPrunedSpeciesCount, maxPrunedSpeciesCount);
	}
	
	
	/**
	 * Call for the algorithm to finish before the allocated iterations run out.
	 */
	public void stopAlgorithm() {
		forceStop = true;
	}

	public int getIterationCounter() {
		return iterationCounter;
	}

	public void setIterationCounter(int i) {
		iterationCounter = i;
	}
	public int getCurrPrunedSpeciesCount() {
		return currPrunedSpeciesCount;
	}
	public double[] getMaxScore() {
		return maxScore;
	}
}
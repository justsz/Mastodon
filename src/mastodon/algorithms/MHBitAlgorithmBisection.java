
package mastodon.algorithms;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import mastodon.core.*;
import mastodon.graphics.DrawFrame;
import mastodon.graphics.DrawPanel;
import mastodon.scoreCalculators.BitMAPScoreCalculator;

import org.apache.commons.math3.distribution.PoissonDistribution;
import org.apache.commons.math3.util.ArithmeticUtils;


import jebl.evolution.taxa.Taxon;
import jebl.evolution.trees.SimpleRootedTree;
import jebl.math.Random;

/**
 * A BitTree implementation of a Metropolis-Hastings (MH) algorithm for pruning trees.
 * @author justs
 */
public class MHBitAlgorithmBisection{

	private Map<BitSet, double[]> taxa;
	private BitTreeSystem bts;
	List<BitTree> bitTrees;
	double tolerance;
	int stepIterations;
	int mapTreeIndex;
	int runCounter = 0;
	int iterationCounter = 0;

	public void setTrees(BitTreeSystem bts, List<BitTree> bitTrees) {
		this.bts = bts;
		this.bitTrees = bitTrees;			
	}

	public void setLimits(double tolerance, int stepIterations) {
		this.tolerance = tolerance;
		this.stepIterations = stepIterations;
	}


	public void run() {		
		//BitMAPScoreCalculator calc = new BitMAPScoreCalculator();
		mapTreeIndex = bts.getMapTreeIndex();		

		System.out.println("Map tree: " + (mapTreeIndex+1));



		int taxaCount = bts.getTaxaCount();

		//variables for bisection method
		int kLeft = 0;
		int kRight = taxaCount;

		int prunedSpeciesCount = (int) ((kRight + kLeft) / 2);

		boolean lastAdjustment = false;
		boolean repeat = true;

		//initializing variables
		double[] maxScore = {0, 0};
		Map<BitSet, double[]> maxScorePruning = new HashMap<BitSet, double[]>();
		BitSet toPrune = new BitSet();
		BitSet prevPruning = new BitSet();
		double[] prevScore = {0, 0};

		while(repeat) {
			double start = System.currentTimeMillis();
			///////////////
			//FIRST STEP//
			/////////////

			if (!lastAdjustment) {
				maxScore[0] = 0;
				maxScore[1] = 0;
				maxScorePruning = new HashMap<BitSet, double[]>();
				toPrune = new BitSet();

				for(int i = 0; i < prunedSpeciesCount; i++) {
					int choice = 0;
					do {
						choice = (int) (Random.nextDouble() * taxaCount);
					} while (toPrune.get(choice));
					toPrune.set(choice);
				}
				prevPruning = (BitSet) toPrune.clone();


				//		Map<BitSet, BitSet> filters = bts.prune(toPrune);
				//		double[] prevScore = calc.getMAPScore(bitTrees.get(mapTreeIndex), bitTrees);
				//		bts.unPrune(filters);

				prevScore = bts.pruneFast(toPrune, bitTrees.get(mapTreeIndex));

				maxScorePruning.put(prevPruning, prevScore);
			}

			///////////////
			//ITERATIONS//
			/////////////

			double mean = 1.0;	//needed when pruning 1 taxon (can't have a mean of 0 in PoissonDistribution())
			if (prunedSpeciesCount > 1) {
				mean = 0.5 * (prunedSpeciesCount - 1);
			}
			PoissonDistribution pd = new PoissonDistribution(mean);
			for(int i = 0; i < stepIterations; i++) {
				iterationCounter++;


				//toPrune = (BitSet) toPrune.clone();

				//				if(prunedSpeciesCount == 1 && iterations[0] == taxaCount) {
				//					//just prune each taxon in turn
				//					toPrune.clear();
				//					toPrune.set(i);					



				//choose the number of species in list to perturb based on a Poisson distributions with rate equal to variable "mean" above
				int numberToSet = 0;
				int numberToClear = 0;

				while(numberToSet < 1 || numberToSet > prunedSpeciesCount) {
					numberToSet = pd.sample() + 1;
				}

				if (numberToSet > (taxaCount - prunedSpeciesCount)) {
					numberToSet = taxaCount - prunedSpeciesCount;
				}
				numberToClear = numberToSet;

				BitSet bitsToSet = new BitSet();
				BitSet bitsToClear = new BitSet();

				//System.out.println(numberToSet + " " + numberToClear + " " + toPrune.cardinality());

				for(int e = 0; e < numberToSet; e++) {
					int choice = 0;
					while (true) {
						choice = (int) (Random.nextDouble() * taxaCount);
						if (!toPrune.get(choice) && !bitsToSet.get(choice)) {
							break;
						}
					}
					bitsToSet.set(choice);						
				}



				for(int e = 0; e < numberToClear; e++) {
					int choice = 0;
					while (true) {
						choice = (int) (Random.nextDouble() * taxaCount);
						if (toPrune.get(choice) && !bitsToClear.get(choice)) {
							break;
						}
					}	
					bitsToClear.set(choice);
				}

				toPrune.or(bitsToSet);
				toPrune.xor(bitsToClear);


				//				filters = bts.prune(toPrune);
				//				double[] currentscore = calc.getMAPScore(bitTrees.get(mapTreeIndex), bitTrees);		
				//				bts.unPrune(filters);

				double[] currentScore = bts.pruneFast(toPrune, bitTrees.get(mapTreeIndex));
				

				if (currentScore[0] > maxScore[0]) {	//set new optimum
					maxScore = currentScore;	//might need a clone here
					maxScorePruning.clear();
					maxScorePruning.put((BitSet) toPrune.clone(), currentScore.clone());
				} else if (currentScore[0] == maxScore[0] && currentScore[1] != 1) { //save variations with same score, but no need to if it produces no results
					maxScorePruning.put((BitSet) toPrune.clone(), currentScore.clone());
				}


				if (currentScore[0]/prevScore[0] > Random.nextDouble()) {
					prevPruning = (BitSet) toPrune.clone(); 
					prevScore = currentScore.clone();
				} //try different pruning otherwise
			}
			System.out.println(prunedSpeciesCount + " pruned taxa running time: " + (System.currentTimeMillis() - start));
			System.out.println(maxScore[0] + " " + maxScore[1]);
			if (maxScore[0] < tolerance) {
				kLeft = prunedSpeciesCount;
			} else {
				kRight = prunedSpeciesCount;
			}

			prunedSpeciesCount = (int) ((kRight + kLeft) / 2);
			//end  if there is no more bisection adjustment going on, but only if a pruning that satisfies the tolerance has been found
			if ((kRight - kLeft) < 2) {
				lastAdjustment = true;
				if (maxScore[0] > tolerance) {
					runCounter++;
					taxa = maxScorePruning;					
					repeat = false;
				}
			}
		}
	}
	
	public RunResult getRunResult() {
		List<ArrayList<Taxon>> prunedTaxa = new ArrayList<ArrayList<Taxon>>();
		List<double[]> pruningScores = new ArrayList<double[]>();
		List<SimpleRootedTree> prunedMapTrees = new ArrayList<SimpleRootedTree>();
		
		BitTree mapTree = bitTrees.get(mapTreeIndex).clone();
		
		for(Entry<BitSet, double[]> entry : taxa.entrySet()) {
			prunedTaxa.add((ArrayList<Taxon>) bts.getTaxa(entry.getKey()));
			pruningScores.add(entry.getValue());
			prunedMapTrees.add(bts.reconstructTree(mapTree, entry.getKey()));
		}
		
		String name = "Bisection run " + runCounter;
		return new RunResult(prunedTaxa, pruningScores, prunedMapTrees, name);
	}
	
	public int getIterationCounter() {
		return iterationCounter;
	}
	
	public void setIterationCounter(int i) {
		iterationCounter = i;
	}

}

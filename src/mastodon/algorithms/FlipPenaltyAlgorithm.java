
package mastodon.algorithms;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
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
public class FlipPenaltyAlgorithm extends Algorithm{
	
	int bitToFlip;

	public void setBTS(BitTreeSystem bts) {
		this.bts = bts;
		bitTrees = bts.getBitTrees();			
	}

	public void setLimits(Map<String, Object> limits) {
		minMapScore = (Double) limits.get("minMapScore");
		totalIterations = (Integer) limits.get("totalIterations");
	}

	protected void initialize() {
			stub = "Flip Penalty";
		
		pruningFreq = new HashMap<Integer, Integer>();
		for(int i = 0; i < bts.getTaxaCount(); i++) {
			pruningFreq.put(i, 0);
		}

		maxScorePruning = new HashMap<BitSet, double[]>();
		currPruning = new BitSet();
		prevPruning = new BitSet();	
		prevScore = new double[] {0,0};

		maxScore = prevScore.clone();
		maxScorePruning.put(prevPruning, maxScore);	

		iterationCounter = 0;
	}

	protected boolean finished() {
		return maxScore[0] >= minMapScore || iterationCounter >= totalIterations;
	}

	protected void choosePruningCount() {
		//pruning count determined by algorithm naturally
	}
	
	
	protected void tryPruning() {
		bitToFlip = (int) (Random.nextDouble() * bts.getTaxaCount());

		currPruning.flip(bitToFlip);

		currScore = bts.pruneFast(currPruning);
		bts.unPrune();
	}
	
	private double getScore(int k, double currMap, double prevMap) {
		//penalty is a decreasing exponential with (k, penalty). Starts at (0, 1), ends at (taxaCount, baseOfPow)
		double penalty = Math.pow(0.05, k/bts.getTaxaCount());
		double gain = currMap / prevMap;
		return penalty * gain;
	}

	protected void setNewBest() {
		if (currScore[0] > maxScore[0]) {	//set new optimum
			currPrunedSpeciesCount = currPruning.cardinality();
			maxScore = currScore;	//might need a clone here
			maxScorePruning.clear();
			maxScorePruning.put((BitSet) currPruning.clone(), currScore.clone());
		} else if (currScore[0] == maxScore[0] && currScore[1] != 1) { //save variations with same score, but no need to if it produces no results
			maxScorePruning.put((BitSet) currPruning.clone(), currScore.clone());
		}


		if (getScore(currPruning.cardinality(), currScore[0], prevScore[0]) > Random.nextDouble()) {
			prevPruning = (BitSet) currPruning.clone(); 
			prevScore = currScore.clone();	

			for (int a = currPruning.nextSetBit(0); a >= 0; a = currPruning.nextSetBit(a+1)) {
				pruningFreq.put(a, pruningFreq.get(a) + 1);
			}
			totalPruningFreq++;

		} else {
			//undo pruning
			currPruning.flip(bitToFlip);
		}
	}

	protected void afterActions() {
		finalPruning = new LinkedHashMap<BitSet, double[]>(maxScorePruning);
		System.out.println("Pruning count: " + currPrunedSpeciesCount);
		System.out.println("Results: " + maxScore[0] + " " + maxScore[1]);
		System.out.println(pruningFreq);
	}
}
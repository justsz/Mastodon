
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
 * An algorithm that looks for a balance between MAP score and pruned taxa count. 
 * It is functional, but unfinished. The idea is that there's a penalty for adding more taxa to the pruned set and a reward for MAP score increase. The two decide whether a step will be taken.
 * @author justs
 */
public class FlipPenaltyAlgorithm extends Algorithm{

	int bitToFlip1;
	int bitToFlip2;


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

//		Random.setSeed(4443245);

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
		return  iterationCounter >= totalIterations; //maxScore[0] >= minMapScore ||
	}

	protected void choosePruningCount() {
		//pruning count determined by algorithm naturally
	}


	protected void tryPruning() {
		bitToFlip1 = (int) (Random.nextDouble() * bts.getTaxaCount());
		//bitToFlip2 = (int) (Random.nextDouble() * bts.getTaxaCount());

		//		int kBefore = currPruning.cardinality();

		currPruning.flip(bitToFlip1);
		//currPruning.flip(bitToFlip2);
		//		int kAfter = currPruning.cardinality();

		//		if (kAfter < kBefore) {
		//			bts.unPrune();
		//		}

		currScore = bts.pruneFast(currPruning);
		bts.unPrune();

	}

	private int prevK = 0;

	private double getScore(int k, double currMap, double prevMap) {
		int avgK = 30;
		//int functionParameter = avgK * 2;
		int functionParameter = bts.getTaxaCount();
		double targetMap = 0.7;
		double base = 0.01;
		//penalty is a decreasing exponential with (k, penalty). Starts at (0, 1), ends at (taxaCount, baseOfPow)
		double penalty;
		if (k > prevK) {
			//penalty = Math.pow(0.01, (double)k/bts.getTaxaCount()) / Math.pow(0.01, (double)prevK/bts.getTaxaCount());
			penalty = Math.pow(base, (double) k / functionParameter);
			//System.out.println(penalty - Math.exp((double) k/functionParameter * Math.log(0.05)));
			//System.out.println(Math.exp((double) k/functionParameter * Math.log(0.05)));
			//penalty = 1 - (double) k/bts.getTaxaCount();
			//penalty = Math.pow(1.0 - (double) k/bts.getTaxaCount(), 3);
		} else {
			penalty = Math.pow(base, (double) (functionParameter - k) / functionParameter);
			//System.out.println((penalty - Math.exp((double) (functionParameter - k)/functionParameter * Math.log(0.05))));
		}
		//System.out.println("k=" + k + " prevK=" + prevK + " penalty=" + penalty);

		double gain = currMap/prevMap;
		return penalty * gain;
	}

	int pruningCountSum = 0;

	protected void setNewBest() {
		


		if (getScore(currPruning.cardinality(), currScore[0], prevScore[0]) > Random.nextDouble()) {
			if (currScore[0] > maxScore[0]) {	//set new optimum
				currPrunedSpeciesCount = currPruning.cardinality();
				maxScore = currScore;	//might need a clone here
				maxScorePruning.clear();
				maxScorePruning.put((BitSet) currPruning.clone(), currScore.clone());
			} else if (currScore[0] == maxScore[0] && currScore[1] != 1) { //save variations with same score, but no need to if it produces no results
				maxScorePruning.put((BitSet) currPruning.clone(), currScore.clone());
			}	
			
			prevK = currPruning.cardinality();
			pruningCountSum += prevK;
			//System.out.println(prevK);
			//System.out.println("score: " + currScore[0] + " getScore: " + getScore(currPruning.cardinality(), currScore[0], prevScore[0]) + " k: " + currPruning.cardinality() + " penalty: " + Math.pow(0.05, (double) currPruning.cardinality()/bts.getTaxaCount()));
			//System.out.println(currScore[0] + " " + getScore(currPruning.cardinality(), currScore[0], prevScore[0]) + " " + currPruning.cardinality() / 84.0 + " " + Math.pow(0.05, (double) currPruning.cardinality()/bts.getTaxaCount()));
			prevPruning = (BitSet) currPruning.clone(); 
			prevScore = currScore.clone();	

			for (int a = currPruning.nextSetBit(0); a >= 0; a = currPruning.nextSetBit(a+1)) {
				pruningFreq.put(a, pruningFreq.get(a) + 1);
			}
			//			pruningFreq.put(bitToFlip, pruningFreq.get(bitToFlip) + 1);
			totalPruningFreq++;

		} else {
			//undo pruning
			currPruning.flip(bitToFlip1);
			//currPruning.flip(bitToFlip2);
		}
	}

	protected void afterActions() {
		//		bts.unPrune();
		System.out.println("average pruning count: " + pruningCountSum/totalPruningFreq);
		finalPruning = new LinkedHashMap<BitSet, double[]>(maxScorePruning);
		System.out.println("Pruning count: " + currPrunedSpeciesCount);
		System.out.println("Results: " + maxScore[0] + " " + maxScore[1]);
		System.out.println(pruningFreq);
	}
}
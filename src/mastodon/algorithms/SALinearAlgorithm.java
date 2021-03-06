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
package mastodon.algorithms;

import java.util.BitSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.math3.distribution.PoissonDistribution;
import org.apache.commons.math3.util.ArithmeticUtils;

import jebl.math.Random;

import mastodon.core.*;

/**
 * SA algorithm with linear search.
 * @author justs
 *
 */
public class SALinearAlgorithm extends Algorithm{

	private double[] stepIterations;

	private double initTemp;
	private double currTemp;
	private double finalTemp;
	private double coolingRate;	//cooling rate is chose to be a smooth exponential going from the max value to min value within the alloted step iterations

	public void setBTS(BitTreeSystem bts) {
		this.bts = bts;
		bitTrees = bts.getBitTrees();		
	}

	public void setLimits(Map<String, Object> limits) {
		minMapScore = (Double) limits.get("minMapScore");
		minPrunedSpeciesCount = (Integer) limits.get("minPruning");
		maxPrunedSpeciesCount = (Integer) limits.get("maxPruning");
		totalIterations = (Integer) limits.get("totalIterations");

		initTemp = (Double) limits.get("initTemp");
		finalTemp = (Double) limits.get("finalTemp");
	}

	protected void initialize() {
		if (minPrunedSpeciesCount == maxPrunedSpeciesCount) {
			stub = "SA Cons.";
		} else {
			stub = "SA Lin.";	
		}
		
		
		pruningFreq = new HashMap<Integer, Integer>();
		for(int i = 0; i < bts.getTaxaCount(); i++) {
			pruningFreq.put(i, 0);
		}

		currTemp = initTemp;

		currPrunedSpeciesCount = minPrunedSpeciesCount;
		maxScorePruning = new HashMap<BitSet, double[]>();
		currPruning = new BitSet();

		for(int i = 0; i < currPrunedSpeciesCount; i++) {
			int choice = 0;
			do {
				choice = (int) (Random.nextDouble() * bts.getTaxaCount());
			} while (currPruning.get(choice));
			currPruning.set(choice);
		}
		prevPruning = (BitSet) currPruning.clone();	

		prevScore = bts.pruneFast(currPruning);
		bts.unPrune();
		maxScore = prevScore.clone();
		maxScorePruning.put(prevPruning, maxScore);	

		double mean = 1.0;	//needed when pruning 1 taxon (can't have a mean of 0 in PoissonDistribution())
		if (currPrunedSpeciesCount > 1) {
			mean = 0.5 * (currPrunedSpeciesCount - 1);
		}
		pd = new PoissonDistribution(mean);
		
		setupIterations();
		coolingRate = Math.pow(finalTemp/initTemp, 1.0/stepIterations[currPrunedSpeciesCount-1]);		

		iterationCounter = 0;		
	}

	protected boolean finished() {
		return maxScore[0] >= minMapScore || currPrunedSpeciesCount > maxPrunedSpeciesCount || iterationCounter >= totalIterations;
		// || currTemp < finalTemp 
	}

	protected void choosePruningCount() {

		int position = 0;
		for (int i = 0; i < stepIterations.length; i++) {
			position += stepIterations[i];
			if (iterationCounter < position) {
				if (i+1 != currPrunedSpeciesCount) {
					currPrunedSpeciesCount = i+1;
					double mean = 1.0;	//needed when pruning 1 taxon (can't have a mean of 0 in PoissonDistribution())
					if (currPrunedSpeciesCount > 1) {
						mean = 0.5 * (currPrunedSpeciesCount - 1);
					}
					pd = new PoissonDistribution(mean);

					coolingRate = Math.pow(finalTemp/initTemp, 1.0/stepIterations[i]);
					currTemp = initTemp;
				}				
				break;
			}
		}

		currTemp *= coolingRate;
	}

	private void setupIterations() {
		//choose how many iterations to allocate to each "round" of pruning
		stepIterations = new double[maxPrunedSpeciesCount];
		for(int i = minPrunedSpeciesCount - 1; i < maxPrunedSpeciesCount; i++) {
			stepIterations[i] = ArithmeticUtils.binomialCoefficientLog(bts.getTaxaCount(), i+1);
		}
		double sum = 0.0;
		for (double d : stepIterations) {
			sum += d;
		}

		for(int i = minPrunedSpeciesCount - 1; i < maxPrunedSpeciesCount; i++) {
			stepIterations[i] = (int) (stepIterations[i] / sum * totalIterations);
		}

	}


	protected void tryPruning() {
		//choose the number of species in list to perturb based on a Poisson distributions with rate equal to variable "mean" above
		int numberToSet = 0;
		int numberToClear = 0;

		while(numberToSet < 1 || numberToSet > currPrunedSpeciesCount) {
			numberToSet = pd.sample() + 1;
		} 

		if (numberToSet > (bts.getTaxaCount() - currPrunedSpeciesCount)) {
			numberToSet = bts.getTaxaCount() - currPrunedSpeciesCount;
		}

		//if we are pruning by one more species now, clear one species less from the pruning list this time
		if(currPruning.cardinality() < currPrunedSpeciesCount) {
			numberToClear = numberToSet - 1;
		} else {
			numberToClear = numberToSet;
		}


		BitSet bitsToSet = new BitSet();
		BitSet bitsToClear = new BitSet();

		for(int e = 0; e < numberToSet; e++) {
			int choice = 0;
			while (true) {
				choice = (int) (Random.nextDouble() * bts.getTaxaCount());
				if (!currPruning.get(choice) && !bitsToSet.get(choice)) {
					break;
				}
			}
			bitsToSet.set(choice);						
		}


		for(int e = 0; e < numberToClear; e++) {
			int choice = 0;
			while (true) {
				choice = (int) (Random.nextDouble() * bts.getTaxaCount());
				if (currPruning.get(choice) && !bitsToClear.get(choice)) {
					break;
				}
			}	
			bitsToClear.set(choice);
		}

		currPruning.or(bitsToSet);
		currPruning.xor(bitsToClear);

		currScore = bts.pruneFast(currPruning);
		bts.unPrune();
	}

	protected void setNewBest() {
		if (currScore[0] > maxScore[0]) {	//set new optimum
			maxScore = currScore;	//might need a clone here
			maxScorePruning.clear();
			maxScorePruning.put((BitSet) currPruning.clone(), currScore.clone());
		} else if (currScore[0] == maxScore[0] && currScore[1] != 1) { //save variations with same score, but no need to if it produces no results
			maxScorePruning.put((BitSet) currPruning.clone(), currScore.clone());
		}


		if (Random.nextDouble() < Math.exp((currScore[0] - prevScore[0]) / currTemp)) {
			prevPruning = (BitSet) currPruning.clone(); 
			prevScore = currScore.clone();

			for (int a = currPruning.nextSetBit(0); a >= 0; a = currPruning.nextSetBit(a+1)) {
				pruningFreq.put(a, pruningFreq.get(a) + 1);
			}
			totalPruningFreq++;

		} //try different pruning otherwise
	}

	protected void afterActions() {
		finalPruning = new LinkedHashMap<BitSet, double[]>(maxScorePruning);
		stepIterations = null;
		//System.out.println("Results: " + maxScore[0] + " " + maxScore[1]);
	}

}

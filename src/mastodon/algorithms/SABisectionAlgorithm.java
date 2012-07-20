/**
 * 
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
 * @author justs
 *
 */
public class SABisectionAlgorithm extends Algorithm{

	private int stepIterations;

	int kLeft;
	int kRight;

	private double initTemp;
	private double currTemp;
	private double finalTemp;
	private double coolingRate;

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
		//coolingRate = (Double) limits.get("coolingRate");

	}

	protected void initialize() {
		stub = "SA Bi.";

		pruningFreq = new HashMap<Integer, Integer>();
		for(int i = 0; i < bts.getTaxaCount(); i++) {
			pruningFreq.put(i, 0);
		}

		mapTreeIndex = bts.getMapTreeIndex();
		System.out.println("Map tree: " + (mapTreeIndex+1));

		currTemp = initTemp;

		kLeft = minPrunedSpeciesCount;
		kRight = maxPrunedSpeciesCount;
		currPrunedSpeciesCount = (int) ((kRight + kLeft) / 2);

		int numberOfSteps = (int) ((Math.log(maxPrunedSpeciesCount - minPrunedSpeciesCount)) / Math.log(2));
		stepIterations = totalIterations / numberOfSteps;
		totalIterations = numberOfSteps * stepIterations;	//adjust for rounding

		maxScore = new double[2];

		coolingRate = Math.pow(finalTemp/initTemp, 1.0/stepIterations);		

		iterationCounter = 0;
	}

	protected boolean finished() {
		return iterationCounter >= totalIterations;
		// || currTemp < finalTemp 
	}

	protected void choosePruningCount() {
		if (iterationCounter % stepIterations == 0) {
			System.out.println(currPrunedSpeciesCount);
			System.out.println(maxScore[0] + " " + maxScore[1]);

			if (iterationCounter > 0) {
				if (maxScore[0] < minMapScore) {
					kLeft = currPrunedSpeciesCount;
				} else {
					kRight = currPrunedSpeciesCount;
				}
				currPrunedSpeciesCount = (int) ((kRight + kLeft) / 2);
			}

			maxScore = new double[2];
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
			prevScore = bts.pruneFast(currPruning, bitTrees.get(mapTreeIndex));

			maxScorePruning.put(prevPruning, prevScore);

			double mean = 1.0;	//needed when pruning 1 taxon (can't have a mean of 0 in PoissonDistribution())
			if (currPrunedSpeciesCount > 1) {
				mean = 0.5 * (currPrunedSpeciesCount - 1);
			}
			pd = new PoissonDistribution(mean);
			currTemp = initTemp;
		}

		currTemp *= coolingRate;
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

		currScore = bts.pruneFast(currPruning, bitTrees.get(mapTreeIndex));
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
		stepIterations = 0;
		System.out.println("Pruned number " + currPrunedSpeciesCount);
		System.out.println("Results: " + maxScore[0] + " " + maxScore[1]);
	}

}

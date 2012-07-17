
package mastodon.algorithms;

import java.io.IOException;
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
public class SABitAlgorithm {

	private Map<BitSet, double[]> taxa;
	private BitTreeSystem bts;
	List<BitTree> bitTrees;
	double tolerance;
	int prunedSpeciesCount;
	int totalIterations;
	int mapTreeIndex;
	int iterationCounter;
	int runCounter = 0;

	double temperature;
	double minTemperature;

	public void setTrees(BitTreeSystem bts, List<BitTree> bitTrees) {
		this.bts = bts;
		this.bitTrees = bitTrees;			
	}

	public void setLimits(double tolerance, int k, int totalIterations, double temp, double minTemp) {
		this.tolerance = tolerance;
		this.prunedSpeciesCount = k;
		this.totalIterations = totalIterations;
		this.temperature = temp;
		this.minTemperature = minTemp;
	}


	public void run() {		
		//		Map<Integer, Integer> pruningFreq = new HashMap<Integer, Integer>();
		//		for(int i = 0; i < bts.getTaxaCount(); i++) {
		//			pruningFreq.put(i, 0);
		//		}

		//		Map<BitSet, Integer> pruningPairFreq = new HashMap<BitSet, Integer>();
		//		for(int i = 0; i < bts.getTaxaCount(); i++) {
		//			for(int e = 0; e< bts.getTaxaCount(); e++) {
		//				BitSet bs = new BitSet();
		//				bs.set(i);
		//				bs.set(e);
		//				pruningPairFreq.put(bs, 0);
		//			}
		//		}

		//		BitMAPScoreCalculator calc = new BitMAPScoreCalculator();
		mapTreeIndex = bts.getMapTreeIndex();		

		System.out.println("Map tree: " + (mapTreeIndex+1));

		double[] maxScore = {0, 0};
		Map<BitSet, double[]> maxScorePruning = new HashMap<BitSet, double[]>();

		int taxaCount = bts.getTaxaCount();

		///////////////
		//FIRST STEP//
		/////////////

		BitSet toPrune = new BitSet();

		for(int i = 0; i < prunedSpeciesCount; i++) {
			int choice = 0;
			do {
				choice = (int) (Random.nextDouble() * taxaCount);
			} while (toPrune.get(choice));
			toPrune.set(choice);
		}
		BitSet prevPruning = (BitSet) toPrune.clone();


		//		Map<BitSet, BitSet> filters = bts.prune(toPrune);
		//		double[] prevScore = calc.getMAPScore(bitTrees.get(mapTreeIndex), bitTrees);
		//		bts.unPrune(filters);

		double[] prevScore = bts.pruneFast(toPrune, bitTrees.get(mapTreeIndex));

		maxScorePruning.put(prevPruning, prevScore);

		///////////////
		//ITERATIONS//
		/////////////
		boolean repeat = true;
		iterationCounter = 0;
		
		double coolingRate = 0.7;
//		double coolingRate = Math.pow(minTemperature/temperature, 1.0/totalIterations);
		System.out.println("cooling rate: " + coolingRate);

		int stepIterations = (int) - (totalIterations / (Math.log(temperature / minTemperature) / Math.log1p(coolingRate - 1)));
		
				//Math.log(coolingRate)));
		System.out.println("step iterations: " + stepIterations);
		//int stepIterations = totalIterations;
		//		double temperatureDecrement = temperature / totalIterations;

		double mean = 1.0;	//needed when pruning 1 taxon (can't have a mean of 0 in PoissonDistribution())
		if (prunedSpeciesCount > 1) {
			mean = 0.5 * (prunedSpeciesCount - 1);
		}
		PoissonDistribution pd = new PoissonDistribution(mean);
		//		int increment = totalIterations / 100;

		java.io.FileWriter fstream;
		int outputCounter = 0;
		try {
			fstream = new java.io.FileWriter("out.txt");
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			fstream = null;
			e1.printStackTrace();
		}
		java.io.BufferedWriter out = new java.io.BufferedWriter(fstream);
		while(repeat) {
			for(int i = 0; i < stepIterations; i++) {

				//print progress
				//								if ((iterationCounter % increment) == 0) {
				//									System.out.print("\r" + iterationCounter/increment + "%");
				//								}
				iterationCounter++;


				//toPrune = (BitSet) toPrune.clone();


				//choose the number of species in list to perturb based on a Poisson distributions with rate equal to variable "mean" above
				int numberToSet = 0;
				int numberToClear = 0;

				while(numberToSet < 1 || numberToSet > prunedSpeciesCount) {
					numberToSet = pd.sample() + 1;
				} 

				if (numberToSet > (taxaCount - prunedSpeciesCount)) {
					numberToSet = taxaCount - prunedSpeciesCount;
				}

				//if we are pruning by one more species now, clear one species less from the pruning list this time
				if(toPrune.cardinality() < prunedSpeciesCount) {
					numberToClear = numberToSet - 1;
				} else {
					numberToClear = numberToSet;
				}


				BitSet bitsToSet = new BitSet();
				BitSet bitsToClear = new BitSet();

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




				//								Map<BitSet, BitSet> filters = bts.prune(toPrune);
				//								calc.getMAPScore(bitTrees.get(mapTreeIndex), bitTrees);
				//								BitSet forTest1 = calc.getTest();
				//								bts.unPrune(filters);

				double[] currentScore = bts.pruneFast(toPrune, bitTrees.get(mapTreeIndex));
				//				BitSet forTest2 = bts.getTest();

				//				if(!forTest1.equals(forTest2)) {
				//					System.out.println("fail!");
				//					System.out.println(forTest1);
				//					System.out.println(forTest2);
				//					System.exit(2);
				//				}



				if (currentScore[0] > maxScore[0]) {	//set new optimum
					maxScore = currentScore;	//might need a clone here
					maxScorePruning.clear();
					maxScorePruning.put((BitSet) toPrune.clone(), currentScore.clone());
				} else if (currentScore[0] == maxScore[0] && currentScore[1] != 1) { //save variations with same score, but no need to if it produces no results
					maxScorePruning.put((BitSet) toPrune.clone(), currentScore.clone());
				}

				//System.out.println(Math.exp(-(currentScore[0] - prevScore[0]) / temperature));

				if (Random.nextDouble() < Math.exp((currentScore[0] - prevScore[0]) / temperature)) {
					prevPruning = (BitSet) toPrune.clone(); 
					prevScore = currentScore.clone();

				} //try different pruning otherwise
				//System.out.println(prevScore[0]);
				outputCounter++;
				if (outputCounter % 40 == 0) {
					try {
						out.write(prevScore[0]+"\n");
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}

			//maxScore[0] > tolerance && vvvv
			if (temperature < minTemperature) {
				System.out.println("temp " + temperature);
				System.out.println("iter " + iterationCounter);
				runCounter++;
				taxa = new LinkedHashMap<BitSet, double[]>(maxScorePruning);
				repeat = false;
				try {
					out.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				System.out.println(maxScore[0] + " " + maxScore[1]);

			} else {
				//System.out.println("temperature " + temperature);
				temperature = temperature * coolingRate;
			}
			//temperature -= temperatureDecrement;
			//				temperature /= 1.1;
			//				System.out.println(temperature);
		}


		//			runCounter++;
		//			taxa = new LinkedHashMap<BitSet, double[]>(maxScorePruning);
		//			repeat = false;
		//
		//			System.out.println(maxScore[0] + " " + maxScore[1]);
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

		String name = "SA run " + runCounter;
		return new RunResult(prunedTaxa, pruningScores, prunedMapTrees, name);
	}

	public int getIterationCounter() {
		return iterationCounter;
	}

	public void setIterationCounter(int i) {
		iterationCounter = i;
	}

}

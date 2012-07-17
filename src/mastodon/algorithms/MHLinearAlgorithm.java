
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
public class MHLinearAlgorithm extends Algorithm{

	

	double minMapScore;
	private double[] stepIterations;
	int totalIterations;

	Map<Integer, Integer> pruningFreq;

	public void setBTS(BitTreeSystem bts) {
		this.bts = bts;
		bitTrees = bts.getBitTrees();			
	}

	public void setLimits(Map<String, Object> limits) {
		minMapScore = (Double) limits.get("minMapScore");
		minPrunedSpeciesCount = (Integer) limits.get("minPruning");
		maxPrunedSpeciesCount = (Integer) limits.get("maxPruning");
		totalIterations = (Integer) limits.get("totalIterations");
	}

	protected void initialize() {
		stub = "MH";
		
		pruningFreq = new HashMap<Integer, Integer>();
		for(int i = 0; i < bts.getTaxaCount(); i++) {
			pruningFreq.put(i, 0);
		}

		mapTreeIndex = bts.getMapTreeIndex();
		System.out.println("Map tree: " + (mapTreeIndex+1));

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

		prevScore = bts.pruneFast(currPruning, bitTrees.get(mapTreeIndex));
		maxScore = prevScore.clone();
		maxScorePruning.put(prevPruning, maxScore);	
		
		double mean = 1.0;	//needed when pruning 1 taxon (can't have a mean of 0 in PoissonDistribution())
		if (currPrunedSpeciesCount > 1) {
			mean = 0.5 * (currPrunedSpeciesCount - 1);
		}
		pd = new PoissonDistribution(mean);
		
		setupIterations();

		iterationCounter = 0;
	}

	protected boolean finished() {
		return maxScore[0] > minMapScore || currPrunedSpeciesCount > maxPrunedSpeciesCount || iterationCounter >= totalIterations;
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
				}
				break;
			}
		}		
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

//		if ((iterations[0] / sum * totalIterations) > bts.getTaxaCount()) {
//			double temp = iterations[0];
//			iterations[0] = bts.getTaxaCount();		 
//			for(int i = minPrunedSpeciesCount; i < maxPrunedSpeciesCount; i++) {	//might want to fill in spots below minPrun.. with 0's
//				//need to check if temp gives the correct solution here but it's a "lost hope function" anyway
//				iterations[i] = iterations[i] / (sum - temp) * (totalIterations - bts.getTaxaCount());
//			}
//		} else {
			for(int i = minPrunedSpeciesCount - 1; i < maxPrunedSpeciesCount; i++) {
				stepIterations[i] = (int) (stepIterations[i] / sum * totalIterations);
			}
//		}
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


		if (currScore[0]/prevScore[0] > Random.nextDouble()) {
			prevPruning = (BitSet) currPruning.clone(); 
			prevScore = currScore.clone();

			for (int a = currPruning.nextSetBit(0); a >= 0; a = currPruning.nextSetBit(a+1)) {
				pruningFreq.put(a, pruningFreq.get(a) + 1);
			}

		} //try different pruning otherwise
	}

	protected void afterActions() {
		runCounter++;
		finalPruning = new LinkedHashMap<BitSet, double[]>(maxScorePruning);
		stepIterations = null;
		System.out.println(maxScore[0] + " " + maxScore[1]);
		System.out.println(pruningFreq);
	}


	public void runnnnn() {		


		//BitMAPScoreCalculator calc = new BitMAPScoreCalculator();

		///////////////
		//FIRST STEP//
		/////////////

		//		Map<BitSet, BitSet> filters = bts.prune(currPruning);
		//		double[] prevScore = calc.getMAPScore(bitTrees.get(mapTreeIndex), bitTrees);
		//		bts.unPrune(filters);


		///////////////
		//ITERATIONS//
		/////////////

		//		int increment = totalIterations / 100;






		//double start = System.currentTimeMillis();




		//print progress
		//								if ((iterationCounter % increment) == 0) {
		//									System.out.print("\r" + iterationCounter/increment + "%");
		//								}



		//currPruning = (BitSet) currPruning.clone();

		//				if(currPrunedSpeciesCount == 1 && iterations[0] == bts.getTaxaCount()) {
		//					//just prune each taxon in turn
		//					currPruning.clear();
		//					currPruning.set(i);					
		//				} else {






		//								Map<BitSet, BitSet> filters = bts.prune(currPruning);
		//								calc.getMAPScore(bitTrees.get(mapTreeIndex), bitTrees);
		//								BitSet forTest1 = calc.getTest();
		//								bts.unPrune(filters);


		//				BitSet forTest2 = bts.getTest();

		//				if(!forTest1.equals(forTest2)) {
		//					System.out.println("fail!");
		//					System.out.println(forTest1);
		//					System.out.println(forTest2);
		//					System.exit(2);
		//				}






		//System.out.println(currPrunedSpeciesCount + " pruned taxa running time: " + (System.currentTimeMillis() - start));




		//picking best singletons
		//				List<Map.Entry<Integer, Integer>> entries = new ArrayList<Map.Entry<Integer, Integer>>();
		//				for (Map.Entry<Integer, Integer> e : pruningFreq.entrySet()) {
		//					entries.add(e);
		//				}
		//
		//				Comparator<Map.Entry<Integer, Integer>> c = new Comparator<Map.Entry<Integer, Integer>>() {
		//					public int compare(Entry<Integer, Integer> arg0,
		//							Entry<Integer, Integer> arg1) {
		//						return (Integer)arg1.getValue().compareTo(arg0.getValue());
		//					}
		//				};				
		//
		//				Collections.sort(entries, c);
		//				
		//				BitSet bits = new BitSet();
		//				for (int i = 0; i < maxPrunedSpeciesCount; i++) {
		//					bits.set(entries.get(i).getKey());
		//				}
		//				double[] topPruning = bts.pruneFast(bits, bitTrees.get(mapTreeIndex));
		//				System.out.println("top pruning: " + topPruning[1]);
		//				
		//				BitSet paperPruning = new BitSet();
		//				paperPruning.set(3);
		//				paperPruning.set(4);
		//				paperPruning.set(5);
		//				paperPruning.set(7);
		//				paperPruning.set(65);
		//				paperPruning.set(58);
		//				paperPruning.set(59);
		//				paperPruning.set(50);
		//				paperPruning.set(51);
		//				paperPruning.set(48);
		//				paperPruning.set(54);
		//				paperPruning.set(44);
		//				paperPruning.set(41);
		//				paperPruning.set(43);
		//				paperPruning.set(45);
		//				paperPruning.set(39);
		//				paperPruning.set(37);
		//				paperPruning.set(27);
		//				paperPruning.set(28);
		//				paperPruning.set(11);
		//				paperPruning.set(12);
		//				paperPruning.set(13);
		//				paperPruning.set(14);
		//				paperPruning.set(15);
		//				paperPruning.set(8);
		//				paperPruning.set(17);
		//				paperPruning.set(82);
		//				
		//				System.out.println(paperPruning);
		//				
		//				System.out.println("new, paperPruning: " + bts.pruneFast(paperPruning, bitTrees.get(mapTreeIndex))[0]);
		////				List<BitSet> blagh = bitTrees.get(11665).getBits();
		////				for (BitSet bl : blagh) {
		////					System.out.println(bl);
		////				}
		//				
		//				bts.prune(paperPruning);
		//				System.out.println("old, paperPruning: " + calc.getMAPScore(bitTrees.get(mapTreeIndex), bitTrees)[0]);
		//				
		//				
		//				
		//picking best pairs
		//				List<Map.Entry<BitSet, Integer>> pairEntries = new ArrayList<Map.Entry<BitSet, Integer>>();
		//				for (Map.Entry<BitSet, Integer> e : pruningPairFreq.entrySet()) {
		//					pairEntries.add(e);
		//				}
		//
		//				Comparator<Map.Entry<BitSet, Integer>> c2 = new Comparator<Map.Entry<BitSet, Integer>>() {
		//					public int compare(Entry<BitSet, Integer> arg0,
		//							Entry<BitSet, Integer> arg1) {
		//						return (Integer)arg1.getValue().compareTo(arg0.getValue());
		//					}
		//				};				
		//
		//				Collections.sort(pairEntries, c2);
		//				
		//				BitSet pairBS = new BitSet();
		//				Iterator<Entry<BitSet, Integer>> it = pairEntries.iterator();
		//				while(pairBS.cardinality() < maxPrunedSpeciesCount) {
		//					pairBS.or(it.next().getKey());
		//				}
		//				System.out.println("the magic pruner: " + pairBS);
		//				System.out.println(bts.pruneFast(pairBS, bitTrees.get(mapTreeIndex)));

		//call heatmap display
		//				DrawFrame frame = new DrawFrame(pruningPairFreq);
		//				frame.setVisible(true);


	}





	public int getIterationCounter() {
		return iterationCounter;
	}

	public void setIterationCounter(int i) {
		iterationCounter = i;
	}

}


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
public class MHBitAlgorithm implements Algorithm{

	private Map<BitSet, float[]> taxa;
	private BitTreeSystem bts;
	List<BitTree> bitTrees;
	float tolerance;
	int maxPrunedSpeciesCount;
	int totalIterations;
	int mapTreeIndex;
	int iterationCounter;
	int runCounter = 0;

	public void setTrees(BitTreeSystem bts, List<BitTree> bitTrees) {
		this.bts = bts;
		this.bitTrees = bitTrees;			
	}

	public void setLimits(float tolerance, int max, int totalIterations) {
		this.tolerance = tolerance;
		this.maxPrunedSpeciesCount = max;
		this.totalIterations = totalIterations;
	}


	public void run() {		
		Map<Integer, Integer> pruningFreq = new HashMap<Integer, Integer>();
		for(int i = 0; i < bts.getTaxaCount(); i++) {
			pruningFreq.put(i, 0);
		}
		
		Map<BitSet, Integer> pruningPairFreq = new HashMap<BitSet, Integer>();
		for(int i = 0; i < bts.getTaxaCount(); i++) {
			for(int e = 0; e< bts.getTaxaCount(); e++) {
				BitSet bs = new BitSet();
				bs.set(i);
				bs.set(e);
				pruningPairFreq.put(bs, 0);
			}
		}
		
		
		Map<Integer, Integer> pruningFreq2 = new HashMap<Integer, Integer>();
		for(int i = 0; i < bts.getTaxaCount(); i++) {
			pruningFreq2.put(i, 0);
		}
		
		Map<BitSet, Integer> pruningPairFreq2 = new HashMap<BitSet, Integer>();
		for(int i = 0; i < bts.getTaxaCount(); i++) {
			for(int e = 0; e< bts.getTaxaCount(); e++) {
				BitSet bs = new BitSet();
				bs.set(i);
				bs.set(e);
				pruningPairFreq2.put(bs, 0);
			}
		}

		BitMAPScoreCalculator calc = new BitMAPScoreCalculator();
		mapTreeIndex = bts.getMapTreeIndex();		

		System.out.println("Map tree: " + (mapTreeIndex+1));
		int prunedSpeciesCount = 1;

		float[] maxScore = {0, 0};
		Map<BitSet, float[]> maxScorePruning = new HashMap<BitSet, float[]>();

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
		//		float[] prevScore = calc.getMAPScore(bitTrees.get(mapTreeIndex), bitTrees);
		//		bts.unPrune(filters);

		float[] prevScore = bts.pruneFast(toPrune, bitTrees.get(mapTreeIndex));

		maxScorePruning.put(prevPruning, prevScore);

		///////////////
		//ITERATIONS//
		/////////////
		boolean repeat = true;
				iterationCounter = 0;
		//		int increment = totalIterations / 100;

		//choose how many iterations to allocate to each "round" of pruning
		double[] iterations = new double[maxPrunedSpeciesCount];
		for(int i = 0; i < maxPrunedSpeciesCount; i++) {
			iterations[i] = ArithmeticUtils.binomialCoefficientLog(taxaCount, i+1);
		}
		double sum = 0.0;
		for (double d : iterations) {
			sum += d;
		}

		if ((iterations[0] / sum * totalIterations) > taxaCount) {
			double temp = iterations[0];
			iterations[0] = taxaCount;		 
			for(int i = 1; i < maxPrunedSpeciesCount; i++) {
				//need to check if temp gives the correct solution here but it's a "lost hope function" anyway
				iterations[i] = iterations[i] / (sum - temp) * (totalIterations - taxaCount);
			}
		} else {
			for(int i = 0; i < maxPrunedSpeciesCount; i++) {
				iterations[i] = iterations[i] / sum * totalIterations;
			}
		}


		while(repeat) {
			double start = System.currentTimeMillis();
			double mean = 1.0;	//needed when pruning 1 taxon (can't have a mean of 0 in PoissonDistribution())
			if (prunedSpeciesCount > 1) {
				mean = 0.5 * (prunedSpeciesCount - 1);
			}
			PoissonDistribution pd = new PoissonDistribution(mean);
			for(int i = 0; i < (int) iterations[prunedSpeciesCount-1]; i++) {

				//print progress
				//								if ((iterationCounter % increment) == 0) {
				//									System.out.print("\r" + iterationCounter/increment + "%");
				//								}
				iterationCounter++;


				//toPrune = (BitSet) toPrune.clone();

				if(prunedSpeciesCount == 1 && iterations[0] == taxaCount) {
					//just prune each taxon in turn
					toPrune.clear();
					toPrune.set(i);					
				} else {


					//choose the number of species in list to perturb based on a Poisson distributions with rate equal to variable "mean" above
					int numberToSet = 0;
					int numberToClear = 0;

					while(numberToSet == 0 || numberToSet > prunedSpeciesCount) {
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

				}


//								Map<BitSet, BitSet> filters = bts.prune(toPrune);
//								calc.getMAPScore(bitTrees.get(mapTreeIndex), bitTrees);
//								BitSet forTest1 = calc.getTest();
//								bts.unPrune(filters);

				float[] currentScore = bts.pruneFast(toPrune, bitTrees.get(mapTreeIndex));
//				BitSet forTest2 = bts.getTest();
				
//				if(!forTest1.equals(forTest2)) {
//					System.out.println("fail!");
//					System.out.println(forTest1);
//					System.out.println(forTest2);
//					System.exit(2);
//				}

				if (currentScore[0] > prevScore[0]) {
					for (int a = toPrune.nextSetBit(0); a >= 0; a = toPrune.nextSetBit(a+1)) {
						pruningFreq.put(a, pruningFreq.get(a) + 1);
					}
					
					if (prunedSpeciesCount == 2) {
						pruningPairFreq.put(toPrune, pruningPairFreq.get(toPrune) + 1);
					} else if (prunedSpeciesCount > 2) {
						for (int y = toPrune.nextSetBit(0); y >= 0; y = toPrune.nextSetBit(y+1)) {
							for (int z = y; z >= 0; z = toPrune.nextSetBit(z+1)) {
								BitSet bbbb = new BitSet();
								bbbb.set(y);
								bbbb.set(z);
								pruningPairFreq.put(bbbb, pruningPairFreq.get(bbbb) + 1);
							}
						}
					}
				}

				if (currentScore[0] > maxScore[0]) {	//set new optimum
					maxScore = currentScore;	//might need a clone here
					maxScorePruning.clear();
					maxScorePruning.put((BitSet) toPrune.clone(), currentScore.clone());
				} else if (currentScore[0] == maxScore[0] && currentScore[1] != 1) { //save variations with same score, but no need to if it produces no results
					maxScorePruning.put((BitSet) toPrune.clone(), currentScore.clone());
				}


				if (currentScore[0]/prevScore[0] > Random.nextFloat()) {
					prevPruning = (BitSet) toPrune.clone(); 
					prevScore = currentScore.clone();
					
					for (int a = toPrune.nextSetBit(0); a >= 0; a = toPrune.nextSetBit(a+1)) {
						pruningFreq2.put(a, pruningFreq2.get(a) + 1);
					}
					
					if (prunedSpeciesCount == 2) {
						pruningPairFreq2.put(toPrune, pruningPairFreq2.get(toPrune) + 1);
					} else if (prunedSpeciesCount > 2) {
						for (int y = toPrune.nextSetBit(0); y >= 0; y = toPrune.nextSetBit(y+1)) {
							for (int z = y; z >= 0; z = toPrune.nextSetBit(z+1)) {
								BitSet bbbb = new BitSet();
								bbbb.set(y);
								bbbb.set(z);
								pruningPairFreq2.put(bbbb, pruningPairFreq2.get(bbbb) + 1);
							}
						}
					}
				} //try different pruning otherwise
			}
			System.out.println(prunedSpeciesCount + " pruned taxa running time: " + (System.currentTimeMillis() - start));
			if (maxScore[0] < tolerance && prunedSpeciesCount < maxPrunedSpeciesCount) {
				prunedSpeciesCount++;
			} else {
				runCounter++;
				taxa = new LinkedHashMap<BitSet, float[]>(maxScorePruning);
				repeat = false;

				//extra experimental and progress-tracking stuff
				System.out.println(maxScore[0] + " " + maxScore[1]);
				System.out.println("Only better");
				System.out.println(pruningFreq);
				//System.out.println(pruningPairFreq);
				System.out.println("Landscape");
				System.out.println(pruningFreq2);
				//System.out.println(pruningPairFreq2);

				//picking best singletons
				List<Map.Entry<Integer, Integer>> entries = new ArrayList<Map.Entry<Integer, Integer>>();
				for (Map.Entry<Integer, Integer> e : pruningFreq.entrySet()) {
					entries.add(e);
				}

				Comparator<Map.Entry<Integer, Integer>> c = new Comparator<Map.Entry<Integer, Integer>>() {
					public int compare(Entry<Integer, Integer> arg0,
							Entry<Integer, Integer> arg1) {
						return (Integer)arg1.getValue().compareTo(arg0.getValue());
					}
				};				

				Collections.sort(entries, c);
				
//				BitSet bits = new BitSet();
//				for (int i = 0; i < maxPrunedSpeciesCount; i++) {
//					bits.set(entries.get(i).getKey());
//				}
//				float[] topPruning = bts.pruneFast(bits, bitTrees.get(mapTreeIndex));
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
		}
	}

	public List<ArrayList<Taxon>> getPrunedTaxa() {
		List<ArrayList<Taxon>> output = new ArrayList<ArrayList<Taxon>>();
		for(BitSet bits : taxa.keySet()) {
			output.add((ArrayList<Taxon>) bts.getTaxa(bits));
		}
		return output;
	}


	public List<SimpleRootedTree> getOutputTrees() {
		//might need to change the interface for this one
		Map<BitSet, BitSet> filters = bts.prune(taxa.keySet().iterator().next());
		List<SimpleRootedTree> trs = new ArrayList<SimpleRootedTree>();
		for(BitTree bitTree : bitTrees) {
			SimpleRootedTree tr = bts.reconstructTree(bitTree, null);
			trs.add(tr);
		}
		bts.unPrune(filters);
		return trs;
	}

	public Map<ArrayList<Taxon>, float[]> getTaxa() {
		Map<ArrayList<Taxon>, float[]> output = new HashMap<ArrayList<Taxon>, float[]>();

		Object[] keys = taxa.keySet().toArray();
		for(int i = 0; i < keys.length; i ++) {
			output.put((ArrayList<Taxon>) bts.getTaxa((BitSet) keys[i]), taxa.get(keys[i]));
		}

		return output;
	}

	public List<SimpleRootedTree> getPrunedMapTrees() {
		List<SimpleRootedTree> trs = new ArrayList<SimpleRootedTree>();
		BitTree mapTree = bitTrees.get(mapTreeIndex).clone();
		trs.add(bts.reconstructTree(mapTree, null));
		for(BitSet bs : taxa.keySet()) {
			mapTree = bitTrees.get(mapTreeIndex).clone();
			mapTree.pruneTree(bs);
			trs.add(bts.reconstructTree(mapTree, null));
		}				
		return trs;
	}

	public List<SimpleRootedTree> getHighlightedPrunedMapTrees() {
		List<SimpleRootedTree> trs = new ArrayList<SimpleRootedTree>();
		BitTree mapTree = bitTrees.get(mapTreeIndex).clone();
		for(BitSet bs : taxa.keySet()) {
			trs.add(bts.reconstructTree(mapTree, bs));
		}
		return trs;
	}
	
	public RunResult getRunResult() {
		List<ArrayList<Taxon>> prunedTaxa = new ArrayList<ArrayList<Taxon>>();
		List<float[]> pruningScores = new ArrayList<float[]>();
		List<SimpleRootedTree> prunedMapTrees = new ArrayList<SimpleRootedTree>();
		
		BitTree mapTree = bitTrees.get(mapTreeIndex).clone();
		
		for(Entry<BitSet, float[]> entry : taxa.entrySet()) {
			prunedTaxa.add((ArrayList<Taxon>) bts.getTaxa(entry.getKey()));
			pruningScores.add(entry.getValue());
			prunedMapTrees.add(bts.reconstructTree(mapTree, entry.getKey()));
		}
		
		String name = "MH run " + runCounter;
		return new RunResult(prunedTaxa, pruningScores, prunedMapTrees, name);
	}
	
	public int getIterationCounter() {
		return iterationCounter;
	}
	
	public void setIterationCounter(int i) {
		iterationCounter = i;
	}

}

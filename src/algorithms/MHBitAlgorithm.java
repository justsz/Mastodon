
package algorithms;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.math3.distribution.PoissonDistribution;
import org.apache.commons.math3.util.ArithmeticUtils;

import scoreCalculators.BitMAPScoreCalculator;

import jebl.evolution.taxa.Taxon;
import jebl.evolution.trees.SimpleRootedTree;
import core.*;
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

		//BitMAPScoreCalculator calc = new BitMAPScoreCalculator();
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

		int matches = bts.pruneFast(toPrune, bitTrees.get(mapTreeIndex));
		float[] prevScore = {(float) matches/bitTrees.size(), matches};

		maxScorePruning.put(prevPruning, prevScore);

		///////////////
		//ITERATIONS//
		/////////////
		boolean repeat = true;
		//		int iterationCounter = 0;
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
				//				if ((iterationCounter % increment) == 0) {
				//					System.out.print("\r" + iterationCounter/increment + "%");
				//				}
				//				iterationCounter++;


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


				//				filters = bts.prune(toPrune);
				//				float[] currentscore = calc.getMAPScore(bitTrees.get(mapTreeIndex), bitTrees);		
				//				bts.unPrune(filters);

				matches = bts.pruneFast(toPrune, bitTrees.get(mapTreeIndex));
				float[] currentScore = {(float) matches/bitTrees.size(), matches};

				//				if (currentScore[0] > prevScore[0]) {
				//					for (int a = toPrune.nextSetBit(0); a >= 0; a = toPrune.nextSetBit(a+1)) {
				//						pruningFreq.put(a, pruningFreq.get(a) + 1);
				//					}

				if (currentScore[0] > maxScore[0]) {	//set new optimum
					maxScore = currentScore;	//might need a clone here
					maxScorePruning.clear();
					maxScorePruning.put((BitSet) toPrune.clone(), currentScore.clone());
				} else if (currentScore[0] == maxScore[0] && currentScore[1] != 1) { //save variations with same score, but no need to if it produces no results
					maxScorePruning.put((BitSet) toPrune.clone(), currentScore.clone());
				}
				//				}

				if (currentScore[0]/prevScore[0] > Random.nextFloat()) {
					prevPruning = (BitSet) toPrune.clone(); 
					prevScore = currentScore.clone();
				} //try different pruning otherwise
			}
			System.out.println(prunedSpeciesCount + " pruned taxa running time: " + (System.currentTimeMillis() - start));
			if (maxScore[0] < tolerance && prunedSpeciesCount < maxPrunedSpeciesCount) {
				prunedSpeciesCount++;
			} else {
				taxa = maxScorePruning;
				repeat = false;

				//extra experimental and progress-tracking stuff
				System.out.println(maxScore[0] + " " + maxScore[1]);

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

}

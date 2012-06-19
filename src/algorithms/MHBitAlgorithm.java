
package algorithms;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
		BitMAPScoreCalculator calc = new BitMAPScoreCalculator();

		mapTreeIndex = bts.getMapTreeIndex();
		float[] notPrunedScore = calc.getMAPScore(bitTrees.get(mapTreeIndex), bitTrees);		

		System.out.println("Map tree: " + (mapTreeIndex+1));
		int prunedSpeciesCount = 1;

		float[] maxScore = {0, 0};
		Map<BitSet, float[]> maxTaxa = new HashMap<BitSet, float[]>();

		//assume that the first tree in the set is the MAP tree for now
		//final List<Taxon> species = bts.
		int taxaCount = bts.getTaxaCount();

		///////////////
		//FIRST STEP//
		/////////////

		BitSet toPrune = new BitSet();
		//I think I want to allow picking the same combination of Taxa.
		//List<ArrayList<Taxon>> triedCombinations = new ArrayList<ArrayList<Taxon>>();

		for(int e = 0; e < prunedSpeciesCount; e++) {
			int choice = 0;

			do {
				choice = (int) (Random.nextDouble() * taxaCount);
			} while (toPrune.get(choice));

			toPrune.set(choice);
		}
		//triedCombinations.add((ArrayList<Taxon>) toPrune);
		BitSet bestChoice = (BitSet) toPrune.clone();

//		for (BitTree bt : bts.getBitTrees()) {
//			for (BitSet bs : bt.getBits()) {
//				System.out.println(bs);
//			}
//			System.out.println("----");
//		}

		//double star = System.currentTimeMillis();
		Map<BitSet, BitSet> filters = bts.prune(toPrune);
//		for (BitTree bt : bts.getBitTrees()) {
//			for (BitSet bs : bt.getBits()) {
//				System.out.println(bs);
//			}
//			System.out.println("----");
//		}
		//System.out.println(System.currentTimeMillis() - star);
		//star = System.currentTimeMillis();
		float[] bestScore = calc.getMAPScore(bitTrees.get(mapTreeIndex), bitTrees);
//		System.out.println(bestScore[1]);
		//System.out.println(System.currentTimeMillis() - star);
		//star = System.currentTimeMillis();
		bts.unPrune(filters);
		//System.out.println(System.currentTimeMillis() - star);
		

		maxTaxa.put(bestChoice, bestScore);

		///////////////
		//ITERATIONS//
		/////////////
		boolean repeat = true;

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
			iterations[0] = taxaCount;		 
			for(int i = 1; i < maxPrunedSpeciesCount; i++) {
				iterations[i] = iterations[i] / sum * (totalIterations - taxaCount);
			}
		} else {
			for(int i = 0; i < maxPrunedSpeciesCount; i++) {
				iterations[i] = iterations[i] / sum * totalIterations;
			}
		}


		while(repeat) {
			//double start = System.currentTimeMillis();
			double mean = 1.0;	//needed when pruning 1 taxon (can't have a mean of 0)
			if (prunedSpeciesCount > 1) {
				mean = 0.5 * (prunedSpeciesCount - 1);
			}
			PoissonDistribution pd = new PoissonDistribution(mean);
			for(int i = 0; i < (int) iterations[prunedSpeciesCount-1]; i++) {
				toPrune = (BitSet) toPrune.clone();


				if(prunedSpeciesCount == 1 && iterations[0] == taxaCount) {
					//just prune each taxon in turn
					toPrune.clear();
					toPrune.set(i);					
				} else {


					//choose the number of species in list to perturb based on a Poisson distributions with rate equal to variable "mean" above
					int numberToPrune = 0;

					while(numberToPrune == 0 || numberToPrune > prunedSpeciesCount) {
						numberToPrune = pd.sample() + 1;
					} 

					for(int e = 0; e < numberToPrune; e++) {
						int choice = 0;
						do {
							choice = (int) (Random.nextDouble() * taxaCount);
						} while (toPrune.get(choice));
						int spot = -1;
						do {
							for (int a = toPrune.nextSetBit(0); a >= 0; a = toPrune.nextSetBit(a+1)) {
								if (Random.nextDouble() > 0.5) {
									spot = a;
									break;
								}
							}						
						} while (spot == -1);
						//int spot = (int) (Random.nextDouble() * prunedSpeciesCount);

						if(toPrune.cardinality() >= prunedSpeciesCount) {
							toPrune.clear(spot);
						}					
						toPrune.set(choice);
					}
				}


				filters = bts.prune(toPrune);
				float[] score = calc.getMAPScore(bitTrees.get(mapTreeIndex), bitTrees);		
				bts.unPrune(filters);



				if (score[0] > maxScore[0]) {	//set new optimum
					maxScore = score;
					maxTaxa.clear();
					maxTaxa.put((BitSet) toPrune.clone(), score);
				} else if (score[0] == maxScore[0] && score[0] != notPrunedScore[0]) { //save variations with same score, but no need to if it produces no results
					maxTaxa.put((BitSet) toPrune.clone(), score);
				}

				if (score[0]/bestScore[0] > Random.nextFloat()) {
					bestChoice = toPrune; 
					bestScore = score;
				} //try different pruning otherwise
			}
			//System.out.println(prunedSpeciesCount + " pruned taxa running time: " + (System.currentTimeMillis() - start));
			if (maxScore[0] < tolerance && prunedSpeciesCount < maxPrunedSpeciesCount) {
				prunedSpeciesCount++;
			} else {
				taxa = maxTaxa;
				repeat = false;
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

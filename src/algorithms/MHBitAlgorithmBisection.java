
package algorithms;
import graphics.DrawFrame;
import graphics.DrawPanel;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
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
public class MHBitAlgorithmBisection implements Algorithm{

	private Map<BitSet, float[]> taxa;
	private BitTreeSystem bts;
	List<BitTree> bitTrees;
	float tolerance;
	int stepIterations;
	int mapTreeIndex;

	public void setTrees(BitTreeSystem bts, List<BitTree> bitTrees) {
		this.bts = bts;
		this.bitTrees = bitTrees;			
	}

	public void setLimits(float tolerance, int stepIterations) {
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
		float[] maxScore = {0, 0};
		Map<BitSet, float[]> maxScorePruning = new HashMap<BitSet, float[]>();
		BitSet toPrune = new BitSet();
		BitSet prevPruning = new BitSet();
		int matches = 0;
		float[] prevScore = {0, 0};

		while(repeat) {
			double start = System.currentTimeMillis();
			///////////////
			//FIRST STEP//
			/////////////

			if (!lastAdjustment) {
				maxScore[0] = 0;
				maxScore[1] = 0;
				maxScorePruning = new HashMap<BitSet, float[]>();
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
				//		float[] prevScore = calc.getMAPScore(bitTrees.get(mapTreeIndex), bitTrees);
				//		bts.unPrune(filters);

				matches = bts.pruneFast(toPrune, bitTrees.get(mapTreeIndex));
				prevScore[0] = (float) matches/bitTrees.size();
				prevScore[1] = matches;

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


				//toPrune = (BitSet) toPrune.clone();

				//				if(prunedSpeciesCount == 1 && iterations[0] == taxaCount) {
				//					//just prune each taxon in turn
				//					toPrune.clear();
				//					toPrune.set(i);					



				//choose the number of species in list to perturb based on a Poisson distributions with rate equal to variable "mean" above
				int numberToSet = 0;
				int numberToClear = 0;

				while(numberToSet == 0 || numberToSet > prunedSpeciesCount) {
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
				//				float[] currentscore = calc.getMAPScore(bitTrees.get(mapTreeIndex), bitTrees);		
				//				bts.unPrune(filters);

				matches = bts.pruneFast(toPrune, bitTrees.get(mapTreeIndex));
				float[] currentScore = {(float) matches/bitTrees.size(), matches};

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
					taxa = maxScorePruning;					
					repeat = false;
				}
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

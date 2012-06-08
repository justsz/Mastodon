
package algorithms;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import scoreCalculators.BitMAPScoreCalculator;

import jebl.evolution.taxa.Taxon;
import jebl.evolution.trees.MutableRootedTree;
import core.*;
import jebl.math.Random;

/**
 * @author justs
 *
 */
public class MHBitAlgorithm implements Algorithm{

	private Set<BitSet> taxa;
	//private List<MutableRootedTree> originalTrees;
	private boolean weighted;
	private BitTreeSystem bts;
	List<BitTree> bitTrees;
	float limit;
	int maxPrunedSpeciesCount;


	public MHBitAlgorithm(List<MutableRootedTree> trees, boolean weighted, float limit, int max) {
		this.weighted = weighted;
		bts = new BitTreeSystem(trees);
		this.limit = limit;
		this.maxPrunedSpeciesCount = max;
		//originalTrees = trees;
	}

	public void run() {
		bitTrees = bts.makeBits();
		System.out.println("Bits created.");
		BitMAPScoreCalculator calc = new BitMAPScoreCalculator();
		
		int mapTreeIndex = bts.getMapTreeIndex();
		float notPrunedScore = calc.getMAPScore(bitTrees.get(mapTreeIndex), bitTrees, weighted);		
		
		System.out.println("Map tree: " + mapTreeIndex);
		int prunedSpeciesCount = 1;
		int iterations = 500;	//don't know any better for now

		float maxScore = 0.0f;
		Set<BitSet> maxTaxa = new HashSet<BitSet>();

		//assume that the first tree in the set is the MAP tree for now
		//final List<Taxon> species = bts.
		int taxaCount = bts.getTaxaCount();
		BitSet bestChoice = new BitSet();

		///////////////
		//FIRST STEP//
		/////////////

		BitSet toPrune = new BitSet();
		//I think I want to allow picking the same combination of Taxa!
		//List<ArrayList<Taxon>> triedCombinations = new ArrayList<ArrayList<Taxon>>();

		for(int e = 0; e < prunedSpeciesCount; e++) {
			int choice = 0;

			do {
				choice = (int) (Random.nextDouble() * taxaCount);
			} while (toPrune.get(choice));

			toPrune.set(choice);
		}
		//triedCombinations.add((ArrayList<Taxon>) toPrune);
		bestChoice = toPrune;
		maxTaxa.add(bestChoice);

//		System.out.print("pruning: ");
//		System.out.println(toPrune);
//		System.out.println();

		List<BitSet> filters = bts.prune(toPrune);
		float bestScore = calc.getMAPScore(bitTrees.get(mapTreeIndex), bitTrees, weighted);		
		bts.unPrune(filters);
		maxScore = bestScore;

		///////////////
		//ITERATIONS//
		/////////////
		boolean repeat = true;

		while(repeat) {
			double start = System.currentTimeMillis();
			for(int i = 0; i < iterations; i++) {
				toPrune = (BitSet) toPrune.clone();
				
				
				//choose the number of species in list to perturb based on a Gaussian distributions
				int numberToPrune = 0;
				double gaus = Random.nextGaussian();
				if (gaus > 3) {
					numberToPrune = prunedSpeciesCount;
				} else if (gaus < -3) {
					numberToPrune = 1;
				} else {
					numberToPrune = (int) ((gaus + 3) / 6 * prunedSpeciesCount + 1);
				}
				
				//int numberToPrune = (int) (Random.nextDouble() * prunedSpeciesCount + 1);	//prune 1 or more species
				//do {
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
				//} while (triedCombinations);

//				System.out.print("pruning: ");
//				System.out.println(toPrune);
//				System.out.println();

		
				filters = bts.prune(toPrune);
				float score = calc.getMAPScore(bitTrees.get(mapTreeIndex), bitTrees, weighted);		
				bts.unPrune(filters);

				
				
				if (score > maxScore) {	//new optimum
					maxScore = score;
					maxTaxa.clear();
					maxTaxa.add((BitSet) toPrune.clone());
				} else if (score == maxScore && score != notPrunedScore) { //save variations with same score, but no need to if it produces no results
					maxTaxa.add((BitSet) toPrune.clone());
				}
				
				if (score/bestScore > Random.nextFloat()) {
//					System.out.println("Accepted.");
					bestChoice = toPrune; 
					bestScore = score;
				} else {
					//do nothing
				}

			}
			System.out.println(prunedSpeciesCount + " pruned taxa running time: " + (System.currentTimeMillis() - start));
			if (maxScore < limit && prunedSpeciesCount < maxPrunedSpeciesCount) {
				prunedSpeciesCount++;
			} else {
				System.out.println(maxScore);
				taxa = maxTaxa;
				repeat = false;
			}
		}
	}

	public List<ArrayList<Taxon>> getPrunedTaxa() {
		List<ArrayList<Taxon>> output = new ArrayList<ArrayList<Taxon>>();
		for(BitSet bits : taxa) {
			output.add((ArrayList<Taxon>) bts.getTaxa(bits));
		}
		return output;
	}

	public List<MutableRootedTree> getOutputTrees() {
		//might need to change the interface for this one
		List<BitSet> filters = bts.prune(taxa.iterator().next());
		List<MutableRootedTree> trs = new ArrayList<MutableRootedTree>();
		for(BitTree bitTree : bitTrees) {
			MutableRootedTree tr = bts.reconstructTree(bitTree);
			trs.add(tr);
		}
		bts.unPrune(filters);
		return trs;
	}
}


package algorithms;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import jebl.evolution.taxa.Taxon;
import jebl.evolution.trees.MutableRootedTree;
import core.*;
import jebl.math.Random;

/**
 * @author justs
 *
 */
public class MHBitAlgorithm implements Algorithm{

	private BitSet taxa;
	//private List<MutableRootedTree> originalTrees;
	private boolean weighted;
	private BitTrees bts;
	List<ArrayList<BitSet>> bitTrees;


	public MHBitAlgorithm(List<MutableRootedTree> trees, boolean weighted) {
		this.weighted = weighted;
		bts = new BitTrees(trees);
		//originalTrees = trees;
	}

	public void run() {
		bitTrees = bts.makeBits();
		BitMAPScoreCalculator calc = new BitMAPScoreCalculator();
		int maxPrunedSpeciesCount = 3;
		int prunedSpeciesCount = 1;
		float limit = 0.95f;
		int iterations = 5;	//don't know any better for now

		float maxScore = 0.0f;
		BitSet maxTaxa;

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
		maxTaxa = bestChoice;

		System.out.print("pruning: ");
		System.out.println(toPrune);
		System.out.println();

		List<BitSet> filters = bts.prune(toPrune);
		float bestScore = calc.getMAPScore(bitTrees.get(0), bitTrees, weighted);		
		bts.unPrune(filters);
		maxScore = bestScore;

		///////////////
		//ITERATIONS//
		/////////////
		boolean repeat = true;

		while(repeat) {
			for(int i = 0; i < iterations; i++) {
				//toPrune = new ArrayList<Taxon>(toPrune);	//shallow copy the old list

				int numberToPrune = (int) (Random.nextDouble() * prunedSpeciesCount + 1);	//prune 1 or more species
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

				System.out.print("pruning: ");
				System.out.println(toPrune);
				System.out.println();

		
				filters = bts.prune(toPrune);
				float score = calc.getMAPScore(bitTrees.get(0), bitTrees, weighted);		
				bts.unPrune(filters);

				if (score > maxScore) {	//should this accept equality?
					maxScore = score;
					maxTaxa = (BitSet) toPrune.clone();
				}
				
				if (score/bestScore > Random.nextFloat()) {
					System.out.println("Accepted.");
					bestChoice = toPrune; 
					bestScore = score;
				} else {
					//do nothing
				}

			}
			if (maxScore < limit && prunedSpeciesCount < maxPrunedSpeciesCount) {
				prunedSpeciesCount++;
			} else {
				taxa = maxTaxa;
				System.out.println("Final pruning:");
				System.out.println(taxa);
				repeat = false;
			}
		}
	}

	public List<Taxon> getPrunedTaxa() {
		//return taxa;
		return null;
	}

	public List<MutableRootedTree> getOutputTrees() {
		//return MutableRootedTree.prune(originalTrees, taxa);
		return null;
	}
}

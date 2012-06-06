
package algorithms;
import java.util.ArrayList;
import java.util.List;

import jebl.evolution.taxa.Taxon;
import jebl.evolution.trees.MutableRootedTree;
import core.*;
import jebl.math.Random;

/**
 * @author justs
 *
 */
public class MHAlgorithm implements Algorithm{

	private List<Taxon> taxa;
	private List<MutableRootedTree> originalTrees;
	private boolean weighted;


	public MHAlgorithm(List<MutableRootedTree> trees, boolean weighted) {
		this.weighted = weighted;
		originalTrees = trees;
	}

	public void run() {
		MAPScoreCalculator calc = new MAPScoreCalculator();
		int maxPrunedSpeciesCount = 5;
		int prunedSpeciesCount = 1;
		float limit = 0.95f;
		int iterations = 5;	//don't know any better for now

		float maxScore = 0.0f;
		List<Taxon> maxTaxa;

		//assume that the first tree in the set is the MAP tree for now
		//full list of species assumed to be contained in MAP tree (or any tree for that matter)
		final List<Taxon> species = new ArrayList<Taxon>(originalTrees.get(0).getTaxa());
		List<Taxon> bestChoice = new ArrayList<Taxon>(prunedSpeciesCount);

		///////////////
		//FIRST STEP//
		/////////////

		List<Taxon> toPrune = new ArrayList<Taxon>(maxPrunedSpeciesCount);
		//I think I want to allow picking the same combination of Taxa!
		//List<ArrayList<Taxon>> triedCombinations = new ArrayList<ArrayList<Taxon>>();
		List<Integer> alreadyChosen = new ArrayList<Integer>(maxPrunedSpeciesCount);

		for(int e = 0; e < prunedSpeciesCount; e++) {
			int choice = 0;

			do {
				choice = (int) (Random.nextDouble() * species.size());
			} while (alreadyChosen.contains(choice));
			alreadyChosen.add(choice);

			toPrune.add(species.get(choice));
		}
		//triedCombinations.add((ArrayList<Taxon>) toPrune);
		bestChoice = new ArrayList<Taxon>(toPrune);
		maxTaxa = bestChoice;

		System.out.print("pruning: ");
		for(Taxon taxon : toPrune) {
			System.out.print(taxon.getName() + ", ");	
		}
		System.out.println();


		List<MutableRootedTree> prunedTrees = MutableRootedTree.prune(originalTrees, toPrune);
		float bestScore = calc.getMAPScore(prunedTrees.get(0), prunedTrees, weighted);
		maxScore = bestScore;

		///////////////
		//ITERATIONS//
		/////////////
		boolean repeat = true;

		while(repeat) {
			for(int i = 0; i < iterations; i++) {
				toPrune = new ArrayList<Taxon>(toPrune);	//shallow copy the old list

				int numberToPrune = (int) (Random.nextDouble() * prunedSpeciesCount + 1);	//prune 1 or more species
				//do {
				for(int e = 0; e < numberToPrune; e++) {
					//there is a probability that you will make a silly choice loop like AB -> AC -> AB but the probability is low for a large set of taxa
					int choice = 0;
					do {
						choice = (int) (Random.nextDouble() * species.size());
					} while (alreadyChosen.contains(choice));
					int spot = (int) (Random.nextDouble() * prunedSpeciesCount);


					if (alreadyChosen.size() < prunedSpeciesCount) {
						alreadyChosen.add(choice);
						toPrune.add(species.get(choice));
					} else {
						alreadyChosen.set(spot, choice);
						toPrune.set(spot, species.get(choice));
					}


				}
				//} while (triedCombinations);

				System.out.print("pruning: ");
				for(Taxon taxon : toPrune) {
					System.out.print(taxon.getName() + ", ");	
				}
				System.out.println();

				prunedTrees = MutableRootedTree.prune(originalTrees, toPrune);			
				float score = calc.getMAPScore(prunedTrees.get(0), prunedTrees, weighted);
				
				if (score > maxScore) {	//should this accept equality?
					maxScore = score;
					maxTaxa = new ArrayList<Taxon>(toPrune);
				}

				if (score/bestScore > Random.nextFloat()) {
					System.out.println("Accepted.");
					bestChoice = new ArrayList<Taxon>(toPrune); 
					bestScore = score;
					
				} else {
					//do nothing
				}

			}
			if (maxScore < limit && prunedSpeciesCount < maxPrunedSpeciesCount) {
				prunedSpeciesCount++;
			} else {
				taxa = new ArrayList<Taxon>(maxTaxa);
				repeat = false;
			}
		}
	}

	public List<Taxon> getPrunedTaxa() {
		return taxa;
	}

	public List<MutableRootedTree> getOutputTrees() {
		return MutableRootedTree.prune(originalTrees, taxa);
	}
}


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
	
	
	public MHAlgorithm(List<MutableRootedTree> trees) {
		originalTrees = trees;
	}

	public void run() {
		//List<MutableRootedTree> original = new ArrayList<MutableRootedTree>(trees); 
		MAPScoreCalculator calc = new MAPScoreCalculator();
		//MutableRootedTree mapTree = trees.get(0);	//assume that the first tree in the set is the MAP tree for now
		int prunedSpeciesCount = 3;
		int iterations = 10;	//don't know any better for now
		List<Taxon> species = new ArrayList<Taxon>(originalTrees.get(0).getTaxa());	//full list of species assumed to be contained in mapTree (or any tree for that matter)
		List<Taxon> bestChoice = new ArrayList<Taxon>(prunedSpeciesCount);
		
		//
		//FIRST STEP
		//
		
		//Taxon[] toPrune = new Taxon[prunedSpeciesCount];
		List<Taxon> toPrune = new ArrayList<Taxon>(prunedSpeciesCount);	//eternal size issues
		for(int e = 0; e < prunedSpeciesCount; e++) {
			int choice = (int) (Random.nextDouble() * species.size());	//need to add some checks so you don't prune the same taxon twice or the whole list...
			toPrune.add(species.get(choice)); 	//use add instead of set?
		}
		bestChoice = new ArrayList<Taxon>(toPrune);
		
		System.out.print("pruning: ");
		for(Taxon taxon : toPrune) {
			System.out.print(taxon.getName() + ", ");	
		}
		System.out.println();
		
		//List<MutableRootedTree> trees = new ArrayList<MutableRootedTree>(originalTrees);
		//for (MutableRootedTree tree : trees) {
		List<MutableRootedTree> prunedTrees = MutableRootedTree.prune(originalTrees, toPrune);
		//}
		
		float bestScore = calc.getMAPScore(prunedTrees.get(0), prunedTrees);	//maybe stick pruning in here? probably not good design
		
		//
		//ITERATIONS
		//
		
		for(int i = 0; i < iterations; i++) {
			//toPrune = new ArrayList<Taxon>();
			
			for(int e = 0; e < prunedSpeciesCount; e++) {
				int choice = (int) (Random.nextDouble() * species.size());	//need to add some checks so you don't prune the same taxon twice or the whole list... probably remove used taxon from list of all taxa
				toPrune.set(e, species.get(choice));
			}
			
			System.out.print("pruning: ");
			for(Taxon taxon : toPrune) {
				System.out.print(taxon.getName() + ", ");	
			}
			System.out.println();
			
			//trees = new ArrayList<MutableRootedTree>(originalTrees);
			prunedTrees = MutableRootedTree.prune(originalTrees, toPrune);
			
			float score = calc.getMAPScore(prunedTrees.get(0), prunedTrees);	//ehhh, do I need to dig out the old tree and edit it? or will it be edited by reference?
			
			float rand = Random.nextFloat();
			if (score/bestScore > rand) {
				System.out.println("Accepted. Random number: " + rand + "\n");
				bestChoice = new ArrayList<Taxon>(toPrune); 
				bestScore = score;
			} else {
				//do nothing
			}
		}
		
		taxa = new ArrayList<Taxon>(bestChoice);
				
		
	}


	public List<Taxon> getPrunedTaxa() {
		return taxa;
	}


	public List<MutableRootedTree> getOutputTrees() {
		return MutableRootedTree.prune(originalTrees, taxa);
	}

}

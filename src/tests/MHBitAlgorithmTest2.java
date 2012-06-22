/**
 * 
 */
package tests;

import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import algorithms.MHBitAlgorithm2;

import jebl.evolution.io.ImportException;
import jebl.evolution.taxa.Taxon;
import jebl.evolution.trees.MutableRootedTree;
import jebl.evolution.trees.SimpleRootedTree;
import core.*;

/**
 * @author justs
 *
 */
public class MHBitAlgorithmTest2 {

	/**
	 * @param args
	 * @throws Throwable 
	 * 
	 */
	public static void main(String[] args) throws Throwable {
		String test = 
//				"simple.trees";
//				"carnivores.trprobs";
//				"snowflake-48d500.trees";
				"carnivores17k.trprobs";
//				"H3N2_1441_tips.500.trees";
//				"H3N2_1968-2011.338_tips.500.trees";

		TreeReader reader = new TreeReader(test);
		BitTreeSystem3 bts = new BitTreeSystem3();
		List<SimpleRootedTree> trees;
		
		double start = System.currentTimeMillis();
		do {
			trees = reader.read100Trees();
			bts.addTrees(trees);
		} while (trees.size() == 100);
		System.out.println(bts.getClades().size());
		trees = null;	//signals to the GC that this can be disposed of
		System.out.println("tree adding time: " + (System.currentTimeMillis() - start));
		
		List<BitTree> bitTrees = bts.getBitTrees();	//refactor, need to only give bts to setTrees

		MHBitAlgorithm2 mh = new MHBitAlgorithm2();
		start = System.currentTimeMillis();

		mh.setTrees(bts, bitTrees);
		mh.setLimits(0.97f, 10, 10000);
		mh.run();
		System.out.println("pruning time: " + (System.currentTimeMillis() - start));

//		List<SimpleRootedTree> prunedTrees = mh.getPrunedMapTrees();
//		for(int i = 0; i < prunedTrees.size(); i++) {
//			NexusWriter writer = new NexusWriter("MHed" + i + ".trees");
//			List<SimpleRootedTree> tree = new ArrayList<SimpleRootedTree>();
//			tree.add(prunedTrees.get(i));
//			writer.writeTrees(tree);
//		}
		
		List<SimpleRootedTree> prunedTrees = mh.getHighlightedPrunedMapTrees();
		NexusWriter writer = new NexusWriter("Highlighted.trees");
		writer.writeTrees(prunedTrees);
		
		
		
		
		System.out.print("Final Pruned taxa: ");
		for(List<Taxon> taxaList : mh.getPrunedTaxa()) {
			for (Taxon taxon : taxaList) {
				System.out.print(taxon.getName() + ", ");
			}
			System.out.println();
		}

	}

}

/**
 * 
 */
package tests;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import algorithms.MHBitAlgorithm;

import jebl.evolution.io.ImportException;
import jebl.evolution.taxa.Taxon;
import jebl.evolution.trees.MutableRootedTree;
import jebl.evolution.trees.SimpleRootedTree;
import core.*;

/**
 * @author justs
 *
 */
public class MHBitAlgorithmTest {

	/**
	 * @param args
	 * @throws Throwable 
	 * 
	 */
	public static void main(String[] args) throws Throwable {
		String test = 
												"simple.trees";
				//				"carnivores.trprobs";
//								"snowflake-48d.trees";
//								"carnivores_edited.trprobs";
//				"carnivores5k.trprobs";

		TreeReader reader = new TreeReader(test);
		BitTreeSystem bts = new BitTreeSystem();
		List<SimpleRootedTree> trees;
		
		double start = System.currentTimeMillis();
		do {
			trees = reader.read100Trees();
			bts.addTrees(trees);
		} while (trees.size() == 100);
		trees = null;	//signals to the GC that this can be disposed of
		System.out.println("tree adding time: " + (System.currentTimeMillis() - start));
		
		List<BitTree> bitTrees = bts.getBitTrees();

		MHBitAlgorithm mh = new MHBitAlgorithm();
		start = System.currentTimeMillis();

		mh.setTrees(bts, bitTrees);
		mh.setLimits(0.97f, 3, 10000);
		mh.run();
		System.out.println("pruning time: " + (System.currentTimeMillis() - start));

//		List<SimpleRootedTree> prunedTrees = mh.getPrunedMapTrees();
//		for(int i = 0; i < prunedTrees.size(); i++) {
//			NexusWriter writer = new NexusWriter("MHed" + i + ".trees");
//			List<SimpleRootedTree> tree = new ArrayList<SimpleRootedTree>();
//			tree.add(prunedTrees.get(i));
//			writer.writeTrees(tree);
//		}
		
		
		System.out.print("Final Pruned taxa: ");
		for(List<Taxon> taxaList : mh.getPrunedTaxa()) {
			for (Taxon taxon : taxaList) {
				System.out.print(taxon.getName() + ", ");
			}
			System.out.println();
		}

	}

}

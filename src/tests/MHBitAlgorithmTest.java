/**
 * 
 */
package tests;

import java.io.IOException;
import java.util.List;

import algorithms.MHAlgorithm;
import algorithms.MHBitAlgorithm;

import jebl.evolution.io.ImportException;
import jebl.evolution.taxa.Taxon;
import jebl.evolution.trees.MutableRootedTree;
import core.*;

/**
 * @author justs
 *
 */
public class MHBitAlgorithmTest {

	/**
	 * @param args
	 * @throws Throwable 
	 */
	public static void main(String[] args) throws Throwable {
		String test = 
												"simple.trees";
				//				"carnivores.trprobs";
//								"snowflake-48d.trees";
				//				"carnivores_edited.trprobs";
//				"carnivores10k.trprobs";

		TreeReader reader = new TreeReader(test);
		BitTreeSystem bts = new BitTreeSystem();
		List<MutableRootedTree> trees;
		do {
			trees = reader.read100Trees();
			bts.addTrees(trees);
		} while (trees.size() == 100);
		
		
		List<BitTree> bitTrees = bts.getBitTrees();

		MHBitAlgorithm mh = new MHBitAlgorithm();
		double start = System.currentTimeMillis();

		mh.setTrees(bts, bitTrees);
		mh.setLimits(0.5f, 3, 5000);
		mh.run();
		System.out.println("total running time: " + (System.currentTimeMillis() - start));

		//NexusWriter writer = new NexusWriter("MHed.trees");
		//writer.writeTrees(mh.getOutputTrees());
		System.out.print("Final Pruned taxa: ");
		for(List<Taxon> taxaList : mh.getPrunedTaxa()) {
			for (Taxon taxon : taxaList) {
				System.out.print(taxon.getName() + ", ");
			}
			System.out.println();
		}

	}

}

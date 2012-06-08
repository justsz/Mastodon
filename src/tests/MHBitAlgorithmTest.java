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
//								"simple.trees";
				//				"carnivores.trprobs";
				"snowflake-48d.trees";
		//				"carnivores_edited.trprobs";
//						"carnivores10k.trprobs";

		TreeReader reader = new TreeReader(test);
		List<MutableRootedTree> trees = reader.readMutableRootedTrees();

		MHBitAlgorithm mh = new MHBitAlgorithm(trees, false, 0.5f, 10, 10000);

		double start = System.currentTimeMillis();
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

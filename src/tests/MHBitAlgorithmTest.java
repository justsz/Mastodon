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
		String test = "simple.trees";
		//String test = "carnivores.trprobs";
		TreeReader reader = new TreeReader(test);
		List<MutableRootedTree> trees = reader.readMutableRootedTrees();

		MHBitAlgorithm mh = new MHBitAlgorithm(trees, false);

		mh.run();

		//NexusWriter writer = new NexusWriter("MHed.trees");
		//writer.writeTrees(mh.getOutputTrees());
		//System.out.print("Final Pruned taxa: ");
		//for(Taxon taxon : mh.getPrunedTaxa()) {
		//	System.out.print(taxon.getName() + ", ");	
		//}

	}

}

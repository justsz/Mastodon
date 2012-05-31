/**
 * 
 */
package tests;

import java.io.IOException;
import java.util.List;

import algorithms.MHAlgorithm;

import jebl.evolution.io.ImportException;
import jebl.evolution.taxa.Taxon;
import jebl.evolution.trees.MutableRootedTree;
import core.NexusWriter;
import core.TreeReader;

/**
 * @author justs
 *
 */
public class MHAlgorithmTest {

	/**
	 * @param args
	 * @throws Throwable 
	 */
	public static void main(String[] args) throws Throwable {
		String test = "simple.trees";
		//String test = "carnivores.trprobs";
		TreeReader reader = new TreeReader(test);
		List<MutableRootedTree> trees = reader.readMutableRootedTrees();

		MHAlgorithm mh = new MHAlgorithm(trees);

		mh.run();

		NexusWriter writer = new NexusWriter("MHed.trees");
		writer.writeTrees(mh.getOutputTrees());
		System.out.print("Pruned taxa: ");
		for(Taxon taxon : mh.getPrunedTaxa()) {
			System.out.print(taxon.getName() + ", ");	
		}

	}

}

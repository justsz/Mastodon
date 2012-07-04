/**
 * 
 */
package mastodon.tests;

import java.io.IOException;

import jebl.evolution.graphs.Node;
import jebl.evolution.io.ImportException;
import jebl.evolution.taxa.Taxon;
import jebl.evolution.trees.MutableRootedTree;
import jebl.evolution.trees.RootedTreeUtils;

import java.util.ArrayList;
import java.util.List; 

import mastodon.core.NexusWriter;
import mastodon.core.TreeReader;


/**
 * @author justs
 *
 */
public class PruningTest {


	public static void main(String[] args) throws IOException, ImportException {
		String test = "simple.trees";
		TreeReader reader = new TreeReader(test);
		List<MutableRootedTree> trees = reader.readMutableRootedTrees();
		
		Taxon taxon = (Taxon) trees.get(0).getTaxa().toArray()[2];
		List<Taxon> taxa = new ArrayList<Taxon>();
		taxa.add(taxon);
		System.out.println("Pruning taxon " + taxon.getName());
		trees = MutableRootedTree.prune(trees, taxa);
		for(MutableRootedTree tree : trees) {
			//tree.removeTaxa(taxa);
			System.out.println(!tree.getTaxa().contains(taxon));
		}
		System.out.println(RootedTreeUtils.equal(trees.get(0), trees.get(2)));
		
		NexusWriter writer = new NexusWriter("pruned.trees");
		writer.writeTrees(trees);

	}

}

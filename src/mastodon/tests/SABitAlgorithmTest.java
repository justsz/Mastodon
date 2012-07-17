/**
 * 
 */
package mastodon.tests;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mastodon.algorithms.MHLinearAlgorithm;
import mastodon.algorithms.SABisectionAlgorithm;
import mastodon.algorithms.SABitAlgorithm;
import mastodon.algorithms.SALinearAlgorithm;
import mastodon.core.*;


import jebl.evolution.io.ImportException;
import jebl.evolution.taxa.Taxon;
import jebl.evolution.trees.MutableRootedTree;
import jebl.evolution.trees.SimpleRootedTree;

/**
 * @author justs
 *
 */
public class SABitAlgorithmTest {

	/**
	 * @param args
	 * @throws Throwable 
	 * 
	 */
	public static void main(String[] args) throws Throwable {
		String test = 
//				"simple.trees";
//				"snowflake-48d500.trees";
				"carnivores1k.trprobs";
//				"H3N2_1441_tips.500.trees";
//				"H3N2_1968-2011.338_tips.500.trees";

		TreeReader reader = new TreeReader(test);
		BitTreeSystem bts = new BitTreeSystem();
		List<SimpleRootedTree> trees;
		
		double start = System.currentTimeMillis();
		do {
			trees = reader.read100Trees();
			bts.addTrees(trees);
		} while (trees.size() == 100);
		System.out.println(bts.getClades().size());
		trees = null;	//signals to the GC that this can be disposed of
		System.out.println("tree adding time: " + (System.currentTimeMillis() - start));
		
		

		SABitAlgorithm mh = new SABitAlgorithm();
		//SALinearAlgorithm sa = new SALinearAlgorithm();
		SABisectionAlgorithm sa = new SABisectionAlgorithm();
		start = System.currentTimeMillis();
		
		Map<String, Object> limits = new HashMap<String, Object>();
		limits.put("minMapScore", 0.6);
		limits.put("minPruning", 1);
		limits.put("maxPruning", 83);
		limits.put("totalIterations", 10000);
		limits.put("initTemp", 1000.0);
		limits.put("finalTemp", 0.0001);
		
//		sa.setBTS(bts);
//		sa.setLimits(limits);
//		sa.run();

		mh.setTrees(bts, bts.getBitTrees());
		mh.setLimits(0.5f, 43, 10000, 100000, 0.00001);
		mh.run();
		System.out.println("pruning time: " + (System.currentTimeMillis() - start));

//		List<SimpleRootedTree> prunedTrees = mh.getPrunedMapTrees();
//		for(int i = 0; i < prunedTrees.size(); i++) {
//			NexusWriter writer = new NexusWriter("MHed" + i + ".trees");
//			List<SimpleRootedTree> tree = new ArrayList<SimpleRootedTree>();
//			tree.add(prunedTrees.get(i));
//			writer.writeTrees(tree);
//		}
		
//		List<SimpleRootedTree> prunedTrees = mh.getHighlightedPrunedMapTrees();
//		NexusWriter writer = new NexusWriter("Highlighted.trees");
//		writer.writeTrees(prunedTrees);
		
		
		
		
//		System.out.print("Final Pruned taxa: ");
//		for(List<Taxon> taxaList : mh.getPrunedTaxa()) {
//			for (Taxon taxon : taxaList) {
//				System.out.print(taxon.getName() + ", ");
//			}
//			System.out.println();
//		}
		
		

	}

}

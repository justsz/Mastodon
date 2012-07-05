/**
 * 
 */
package mastodon.tests;

import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Map;

import mastodon.core.*;

import jebl.evolution.io.ImportException;
import jebl.evolution.trees.SimpleRootedTree;
import jebl.evolution.trees.RootedTreeUtils;

/**
 * @author justs
 *
 */
public class BitStuffTest {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws ImportException 
	 */
	public static void main(String[] args) throws IOException, ImportException {
//		String test = "simple.trees";
//		String test = "carnivores.trprobs";
		String test = "snowflake-48d.trees";
		TreeReader reader = new TreeReader(test);
		List<SimpleRootedTree> trees = reader.readSimpleRootedTrees();
		

		BitTreeSystem bs = new BitTreeSystem();
		bs.addTrees(trees);
		Map<BitSet, Clade> clades = bs.getClades();
		

		List<BitTree> bitTrees = bs.getBitTrees();
		System.out.println(clades);

		List<SimpleRootedTree> trs = new ArrayList<SimpleRootedTree>();
		for(BitTree bitTree : bitTrees) {
			SimpleRootedTree tr = bs.reconstructTree(bitTree, null);
			trs.add(tr);
		}


		//NexusWriter writer = new NexusWriter("reconstructed.nex");
		//writer.writeTrees(trs);

		
		//
		//Pruning tests
		//
//		System.out.println("before");
//		for(ArrayList<BitSet> rrr : bitTrees) {
//			for(BitSet b : rrr) {
//				System.out.println(b);
//			}
//		}
		BitSet a = new BitSet();
		a.set(0);
		a.set(2);
		Map<BitSet, BitSet> filters = bs.prune(a);
		System.out.println(clades);
		bs.unPrune(filters);
		System.out.println(clades);
//		System.out.println("after");
//		for(ArrayList<BitSet> rrr : bitTrees) {
//			for(BitSet b : rrr) {
//				System.out.println(b);
//			}
//		}
		
		
		trs = new ArrayList<SimpleRootedTree>();
		for(BitTree bitTree : bitTrees) {
			SimpleRootedTree tr = bs.reconstructTree(bitTree, null);
			trs.add(tr);
		}


		//writer = new NexusWriter("reconstructed.nex");
		//writer.writeTrees(trs);
		
	}

}
/**
 * 
 */
package tests;

import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Map;

import jebl.evolution.io.ImportException;
import jebl.evolution.trees.MutableRootedTree;
import core.*;

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
		String test = "simple.trees";
		//String test = "carnivores.trprobs";
		TreeReader reader = new TreeReader(test);
		List<MutableRootedTree> trees = reader.readMutableRootedTrees();

		BitStuff bs = new BitStuff(trees);
		Map<BitSet, Integer> clades = bs.getClades();
		

		List<ArrayList<BitSet>> bitTrees = bs.makeBits();
		System.out.println(clades);

		List<MutableRootedTree> trs = new ArrayList<MutableRootedTree>();
		for(ArrayList<BitSet> bitTree : bitTrees) {
			MutableRootedTree tr = bs.reconstructTree(bitTree);
			trs.add(tr);
		}


		NexusWriter writer = new NexusWriter("reconstructed.nex");
		writer.writeTrees(trs);

		
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
		List<BitSet> filters = bs.prune(a);
		System.out.println(clades);
		bs.unPrune(filters);
		System.out.println(clades);
//		System.out.println("after");
//		for(ArrayList<BitSet> rrr : bitTrees) {
//			for(BitSet b : rrr) {
//				System.out.println(b);
//			}
//		}
		
		
		trs = new ArrayList<MutableRootedTree>();
		for(ArrayList<BitSet> bitTree : bitTrees) {
			MutableRootedTree tr = bs.reconstructTree(bitTree);
			trs.add(tr);
		}


		writer = new NexusWriter("reconstructed.nex");
		writer.writeTrees(trs);
		
	}

}

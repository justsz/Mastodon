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

		List<ArrayList<BitSet>> bitTrees = bs.makeBits();


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
		System.out.println("before");
		for(ArrayList<BitSet> rrr : bitTrees) {
			for(BitSet b : rrr) {
				System.out.println(b);
			}
		}
		bs.pruneEVERYTHING();
		System.out.println("after");
		for(ArrayList<BitSet> rrr : bitTrees) {
			for(BitSet b : rrr) {
				System.out.println(b);
			}
		}
		
		trs = new ArrayList<MutableRootedTree>();
		for(ArrayList<BitSet> bitTree : bitTrees) {
			MutableRootedTree tr = bs.reconstructTree(bitTree);
			trs.add(tr);
		}


		writer = new NexusWriter("reconstructed.nex");
		writer.writeTrees(trs);
		
	}

}

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
		
//		System.out.println(clades);
		
		List<ArrayList<BitSet>> bitTrees = bs.makeBits();
		
//		List<BitSet> tree1Clades = new ArrayList<BitSet>();
//		Object[] ca = clades.keySet().toArray();
//		tree1Clades.add((BitSet) ca[0]);
//		tree1Clades.add((BitSet) ca[1]);
//		tree1Clades.add((BitSet) ca[4]);
//		tree1Clades.add((BitSet) ca[6]);
//		tree1Clades.add((BitSet) ca[7]);
		
//		for(BitSet b : tree1Clades) {
//			System.out.println(b);
//		}
		
//		MutableRootedTree tr = bs.reconstructTree(tree1Clades);
		
		List<MutableRootedTree> trs = new ArrayList<MutableRootedTree>();
		for(ArrayList<BitSet> bitTree : bitTrees) {
			MutableRootedTree tr = bs.reconstructTree(bitTree);
			trs.add(tr);
		}
				
		
		NexusWriter writer = new NexusWriter("reconstructed.nex");
		writer.writeTrees(trs);
		
	}

}

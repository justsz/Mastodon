
package core;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jebl.evolution.trees.RootedTreeUtils;
import jebl.evolution.trees.RootedTree;

/**
 * @author justs
 *
 */
public class BitMAPScoreCalculator {

	public float getMAPScore(List<BitSet> MAPTree, List<ArrayList<BitSet>> trees, boolean weighted) {
		float sum = 0;
		int setSize = trees.size();


		int count = 0;
		Set<BitSet> set1 = new HashSet<BitSet>();
		for(BitSet bs : MAPTree) {
			if(bs.cardinality() > 1) {	//what if it's pruned down to a stump?
				set1.add(bs);
			}
		}
		

		
		for (List<BitSet> tree : trees) {
			Set<BitSet> set2 = new HashSet<BitSet>();
			for(BitSet bs : tree) {
				if(bs.cardinality() > 1) {	//what if it's pruned down to a stump?
					set2.add(bs);
				}
			}
			

			
			if(set1.equals(set2)) {
				count++;
				if (weighted) {
					System.out.println("Don't know how to do weighted BitTrees yet!");
//					try {
//						sum += (Float) tree.getAttribute("weight");
//					} catch(Exception e) {
//						System.out.println("Missing weight attribute.");
//						System.out.println(tree.getAttributeMap());
//						System.exit(1);
//					}
				} else {
					sum++;
				}
			}
		}

		System.out.println("number of matching trees: " + count);
		
		if (weighted) {
			System.out.println(sum);
			return sum;
		} else {
			System.out.println(sum/setSize);
			return sum/setSize;
		}
	}

}

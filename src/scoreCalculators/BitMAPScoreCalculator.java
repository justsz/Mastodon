
package scoreCalculators;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import core.BitTree;

import jebl.evolution.trees.RootedTreeUtils;
import jebl.evolution.trees.RootedTree;

/**
 * @author justs
 *
 */
public class BitMAPScoreCalculator {

	public float getMAPScore(BitTree MAPTree, List<BitTree> trees, boolean weighted) {
		float sum = 0;
		int setSize = trees.size();


		int count = 0;
		
		boolean[] matches = MAPTree.equalsList(trees);

		
		for (int i = 0; i < matches.length; i++) {			

			
			if(matches[i]) {
				count++;
				//if (weighted) {
					
//					try {
						sum += (Float) trees.get(i).getWeight();
//					} catch(Exception e) {
//						System.out.println("Missing weight attribute.");
//						System.out.println(tree.getAttributeMap());
//						System.exit(1);
//					}
				//} else {
				//	sum++;
				//}
			}
		}

//		System.out.println("number of matching trees: " + count);
		
//		if (weighted) {
//			System.out.println(sum);
			return sum;
//		} else {
//			System.out.println(sum/setSize);
//			return sum/setSize;
//		}
	}

}

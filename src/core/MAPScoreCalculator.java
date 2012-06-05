
package core;

import java.util.List;

import jebl.evolution.trees.RootedTreeUtils;
import jebl.evolution.trees.RootedTree;

/**
 * @author justs
 *
 */
public class MAPScoreCalculator {

	public float getMAPScore(RootedTree MAPTree, List<? extends RootedTree> trees, boolean weighted) {
		float sum = 0;
		int setSize = trees.size();


		int count = 0;
		for (RootedTree tree : trees) {
			if(RootedTreeUtils.equal(tree, MAPTree)) {
				count++;
				if (weighted) {
					try {
						sum += (Float) tree.getAttribute("weight");
					} catch(Exception e) {
						System.out.println("Missing weight attribute.");
						System.out.println(tree.getAttributeMap());
						System.exit(1);
					}
				} else {
					sum++;
				}
			}
		}

		System.out.println("number of matching trees: " + count);
		System.out.println(sum);
		if (weighted) {
			return sum;
		} else {
			return sum/setSize;
		}
	}

}

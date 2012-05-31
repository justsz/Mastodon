
package core;

import java.util.List;

import jebl.evolution.trees.RootedTreeUtils;
import jebl.evolution.trees.RootedTree;

/**
 * @author justs
 *
 */
public class MAPScoreCalculator {
	//private List<? extends RootedTree> trees;

//	public MAPScoreCalculator(List<? extends RootedTree> trees) {
//		this.trees = trees;
//	}

	public float getMAPScore(RootedTree MAPTree, List<? extends RootedTree> trees) {
		float sum = 0;
		int setSize = trees.size();


		int count = 0; //used for testing
		for (RootedTree tree : trees) {
			if(RootedTreeUtils.equal(tree, MAPTree)) {
				try {
					sum += (Float) tree.getAttribute("weight");
				} catch(Exception e) {
					System.out.println(tree.getAttributeMap());
					count--;
				}
				count++;
			}

		}

		System.out.println("number of matching trees: " + count);
		System.out.println(sum);
		return sum;
	}

}

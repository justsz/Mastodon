
package mastodon.scoreCalculators;


import java.util.BitSet;
import java.util.List;

import mastodon.core.BitTree;


/**
 * Used to calculate the MAP score of a given tree with respect to all trees in set.
 * @author justs
 *
 */
public class BitMAPScoreCalculator {
	private BitSet forTest = new BitSet();

	/**
	 * Calculates the map score and number of matching trees. 
	 * Weighted trees: adds up the weights for trees matching the map tree.
	 * Un-weighted trees: assumes all trees are of equal probability and does as for weighted.
	 * @param MAPTree - tree which to score
	 * @param trees - all trees to compare against
	 * @return {map score, numbers of trees matched}
	 */
	public float[] getMAPScore(BitTree MAPTree, List<BitTree> trees) {
		//double start = System.currentTimeMillis();
		float[] result = new float[2];
		float sum = 0;
		int count = 0;
		boolean weighted = true;
		forTest.clear();

		boolean[] matches = MAPTree.equalsList(trees);

		for (int i = 0; i < matches.length; i++) {	
			if(matches[i]) {
				forTest.set(i);
				count++;
				float weight = trees.get(i).getWeight();
				if(weight == -1) {
					sum++;
					weighted = false;	//annotation errors that cause trees to not be exclusively weighted or unweighted should have been dealt with while adding trees
				} else {
					sum += trees.get(i).getWeight();
				}

			}
		}
		
		
		result[1] = count;
		if(weighted) {
			result[0] = sum;
		} else {
			result[0] = sum/trees.size();
		}
		//System.out.println(System.currentTimeMillis() - start);
		return result;
	}
	
	public BitSet getTest() {
		return forTest;
	}
}

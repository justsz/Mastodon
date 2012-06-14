
package scoreCalculators;


import java.util.List;
import core.BitTree;


/**
 * Used to calculate the MAP score of a given tree with respect to all trees in set.
 * @author justs
 *
 */
public class BitMAPScoreCalculator {

	/**
	 * Calculates the map score and number of matching trees. 
	 * Weighted trees: adds up the weights for trees matching the map tree.
	 * Un-weighted trees: assumes all trees are of equal probability and does as for weighted.
	 * @param MAPTree - tree which to score
	 * @param trees - all trees to compare against
	 * @return {map score, numbers of trees matched}
	 */
	public float[] getMAPScore(BitTree MAPTree, List<BitTree> trees) {
		float[] result = new float[2];
		float sum = 0;
		int count = 0;
		boolean weighted = true;

		boolean[] matches = MAPTree.equalsList(trees);

		for (int i = 0; i < matches.length; i++) {			
			if(matches[i]) {
				count++;
				float weight = trees.get(i).getWeight();
				if(weight == -1) {
					sum++;
					weighted = false;
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
		
		return result;
	}
}

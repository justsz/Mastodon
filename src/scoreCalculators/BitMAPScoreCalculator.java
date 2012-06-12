
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

/* Copyright (C) 2012 Justs Zarins
 *
 *This file is part of MASTodon.
 *
 *MASTodon is free software: you can redistribute it and/or modify
 *it under the terms of the GNU Lesser General Public License as
 *published by the Free Software Foundation, either version 3
 *of the License, or (at your option) any later version.
 *
 *MASTodon is distributed in the hope that it will be useful,
 *but WITHOUT ANY WARRANTY; without even the implied warranty of
 *MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *GNU Lesser General Public License for more details.
 *
 *You should have received a copy of the GNU Lesser General Public License
 *along with this program.  If not, see http://www.gnu.org/licenses/.
 */
package mastodon.scoreCalculators;


import java.util.BitSet;
import java.util.List;

import mastodon.core.BitTree;


/**
 * Used to calculate the MAP score of a given tree with respect to all trees in set.
 * Use in conjunction with the normal "prune" method. "pruneFast" includes a score calculator.
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
	public double[] getMAPScore(BitTree MAPTree, List<BitTree> trees) {
		//double start = System.currentTimeMillis();
		double[] result = new double[2];
		double sum = 0;
		int count = 0;
		boolean weighted = true;
		forTest.clear();

		boolean[] matches = MAPTree.equalsList(trees);

		for (int i = 0; i < matches.length; i++) {	
			if(matches[i]) {
				forTest.set(i);
				count++;
				double weight = trees.get(i).getWeight();
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

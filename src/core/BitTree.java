package core;

import java.util.BitSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author justs
 *
 */
public class BitTree {
	private List<BitSet> treeClades;
	private float weight;

	BitTree(List<BitSet> tc, float weight) {
		treeClades = tc;
		this.weight = weight;
	}

	public List<BitSet> getBits() {
		return treeClades;
	}
	
	public float getWeight() {
		return weight;
	}

	public boolean equals(List<BitSet> treeClades2) {
		Set<BitSet> set1 = new HashSet<BitSet>();
		for(BitSet bs : treeClades) {
			if(bs.cardinality() > 1) {	
				set1.add(bs);
			}
		}

		Set<BitSet> set2 = new HashSet<BitSet>();
		for(BitSet bs : treeClades2) {
			if(bs.cardinality() > 1) {	
				set2.add(bs);
			}
		}
		return set1.equals(set2);
	}
	
	public boolean[] equalsList(List<BitTree> trees) {
		boolean[] output = new boolean[trees.size()];
		
		Set<BitSet> set1 = new HashSet<BitSet>();
		for(BitSet bs : treeClades) {
			if(bs.cardinality() > 1) {	
				set1.add(bs);
			}
		}
		
		for(int i = 0; i < trees.size(); i++) {
			Set<BitSet> set2 = new HashSet<BitSet>();
			for(BitSet bs : trees.get(i).getBits()) {
				if(bs.cardinality() > 1) {	//what if it's pruned down to a stump?
					set2.add(bs);
				}
			}
			output[i] = set1.equals(set2) ? true : false;
		}		
		return output;		
	}
}

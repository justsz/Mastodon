package core;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This class implements a BitSet implementation of a tree. The topology of a tree is uniquely defined by the list of clades it contains.
 * @author justs
 */
public class BitTree {
	private List<BitSet> treeClades;
	private float weight;

	/**
	 * Standard constructor, specify clades and weight of tree. Weight should be set to -1 if the tree is unweighted.
	 * @param tc - list of all clades in tree with respect to some list of all taxa which decodes the BitSets
	 * @param weight - probability weight of tree
	 */
	BitTree(List<BitSet> tc, float weight) {
		treeClades = tc;
		this.weight = weight;
	}

	/**
	 * Returns a list of all clades of the tree.
	 * @return list of all clades of the tree
	 */
	public List<BitSet> getBits() {
		return treeClades;
	}

	/**
	 * Returns weight of the tree.
	 * @return weight of the tree
	 */
	public float getWeight() {
		return weight;
	}

	/**
	 * NOT JAVADOCCED
	 * 
	 * 
	 */
	public void order() {
		Comparator<BitSet> c = new Comparator<BitSet>() {
			public int compare(BitSet b1, BitSet b2) {
				//return ((Integer)b1.cardinality()).compareTo(b2.cardinality());
				return ((Integer)b1.hashCode()).compareTo(b2.hashCode());
			}
		};

		Collections.sort(treeClades, c);

	}

	//ascending cardinality
	//	Comparator<BitSet> c = new Comparator<BitSet>() {
	//		public int compare(BitSet b1, BitSet b2) {
	//			return ((Integer)b1.cardinality()).compareTo(b2.cardinality());
	//		}
	//	};
	//	List<BitSet> bitSets = bitTree.getBits();
	//	Collections.sort(bitSets, c);

	/**
	 * Prune an individual tree. The called tree is modified.
	 * @param pruner - taxa to prune
	 */
	public void pruneTree(BitSet pruner) {
		for(BitSet bs : treeClades) {
			BitSet filter = (BitSet) pruner.clone();
			filter.and(bs);	//filters can be returned if there is a need to unPrune the trees
			bs.xor(filter);
		}
	}

	/** 
	 * Deep copy of the given BitTree.
	 * @return a clone of the tree 
	 */
	public BitTree clone() {
		List<BitSet> clades = new ArrayList<BitSet>(treeClades.size());
		for(BitSet bs : treeClades) {
			clades.add((BitSet) bs.clone());
		}
		return new BitTree(clades, weight);
	}

	/**
	 * Tests if two BitTrees are equal.
	 * @param treeClades2 - tree to compare this one to
	 * @return true if the trees are equal
	 */
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

	/**
	 * Compares this BitTree to a list of BitTrees. More efficient than calling equals on each pair separately.
	 * @param trees - list of trees that are to be compared against
	 * @return an array of booleans that corresponds in position to equal trees in the list compared to this one
	 */
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
			//			output[i] = set1.hashCode() == set2.hashCode() ? true : false;
		}		
		return output;		
	}

	public boolean[] equalsList2(List<BitTree> trees) {
		boolean[] output = new boolean[trees.size()];

		this.order();
		for(int i = 0; i < trees.size(); i++) {
			output[i] = this.equals2(trees.get(i));
		}

		return output;
	}

	public boolean equals2(BitTree tree) {
		tree.order();

		if (this == tree) {
			//System.out.println("x");
			return true;
		} else {
			//			for(BitSet bs1 : this.getBits()) {
			//				if (bs1.cardinality() > 1)
			//			}
			int index1 = 0;
			int index2 = 0;
			List<BitSet> list1 = this.getBits();
			List<BitSet> list2 = tree.getBits();

			while (index1 < list1.size() && index2 < list2.size()) {
				while (list1.get(index1).cardinality() < 2) {
					index1++;
					if (index1 == list1.size()) {
						//System.out.println("a");
						return false;
					}
				}
				while (list2.get(index2).cardinality() < 2) {
					index2++;
					if (index2 == list2.size()) {
						//System.out.println("b");
						return false;
					}
				}
				BitSet b1 = list1.get(index1);
				BitSet b2 = list2.get(index2);

				if (b1 == null || b2 == null) {
					System.out.println("c");
					return true;
				} else {
					if (b1.equals(b2)) {
						index1++;
						index2++;
						//System.out.println("d");
					} else {
						//System.out.println("e");
						return false;
					}
				}
			}
			return true;
		}

	}
}

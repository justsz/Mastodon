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

package mastodon.core;

import java.io.Serializable;
import java.util.BitSet;
import java.util.Collection;

/**
 * This object represents a tree clade using bitsets. It also includes information about which trees contain this clade.
 * Each tree is represented by a bit in the bitset. A set bit means that this clade is in that tree. So 1000100.. means that this this clade is in the 0th and 4th tree.
 * The clade-to-tree overall bitset that specifies which tree is in which position is created during reading of trees.
 * @author justs
 *
 */
public class Clade {
	private BitSet clade;
	private BitSet cladeToTrees;

	/**
	 * Basic constructor.
	 * @param bits - bitset representing taxa in the clade
	 */
	public Clade(BitSet bits) {
		clade = bits;
		cladeToTrees = new BitSet();
	}

	/**
	 * Constructor where you also specify the clade-to-tree relationship bitset.
	 * @param bits - bitset representing taxa in the clade
	 * @param cladeToTrees - bitset representing which trees contain this clade
	 */
	public Clade(BitSet bits, BitSet cladeToTrees) {
		clade = bits;
		this.cladeToTrees = cladeToTrees;
	}

	/**
	 * Add trees that contain this clade.
	 * @param trees - collection of integers that specify which trees have this clade
	 */
	public void addTrees(Collection<Integer> trees) {
		for (Integer tree : trees) {
			addTree(tree);
		}
	}

	/**
	 * Add tree that contain this clade.
	 * @param tree - integer that specifies which tree has this clade
	 */
	public void addTree(Integer tree) {
		cladeToTrees.set(tree);
	}

	/**
	 * Returs the clade-to-tree relationship bitset.
	 * @return clade-to-tree relationship
	 */
	public BitSet getCladeToTrees() {
		return cladeToTrees;
	}

	/**
	 * Returns the bitset representing this clade.
	 * @return bitset representation of clade
	 */
	public BitSet getCladeBits() {
		return clade;
	}

	/**
	 * Returns the number of times this clade appears in the tree set.
	 * @return number of times this clade appears in the tree set
	 */
	public int getFrequency() {
		return cladeToTrees.cardinality();
	}
}
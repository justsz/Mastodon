/**
 * 
 */
package mastodon.core;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author justs
 *
 */
public class Clade {
	private BitSet clade;
	private BitSet cladeToTrees;
	
	public Clade(BitSet bits) {
		clade = bits;
		cladeToTrees = new BitSet();
	}
	
	public Clade(BitSet bits, BitSet cladeToTrees) {
		clade = bits;
		this.cladeToTrees = cladeToTrees;
	}
	
	public void addTrees(Collection<Integer> trees) {
		for (Integer tree : trees) {
			addTree(tree);
		}
	}
	
	public void addTree(Integer tree) {
		cladeToTrees.set(tree);
	}
	
	public BitSet getCladeToTrees() {
		return cladeToTrees;
	}
	
	public BitSet getCladeBits() {
		return clade;
	}
	
	public int getFrequency() {
		return cladeToTrees.cardinality();
	}
	
}

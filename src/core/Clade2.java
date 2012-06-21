/**
 * 
 */
package core;

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
public class Clade2 {
	private BitSet clade;
	private int count;
	private HashSet<Integer> cladeToTrees;
	
	public Clade2(BitSet bits) {
		clade = bits;
		count = 1;
		cladeToTrees = new HashSet<Integer>();
	}
	
	public Clade2 lightClone() {
		Clade2 out = new Clade2(clade);
		out.addTrees(cladeToTrees);
		return out;
	}
	
	public void addTrees(Collection<Integer> trees) {
		cladeToTrees.addAll(trees);
	}
	
	public void addTree(Integer tree) {
		cladeToTrees.add(tree);
	}
	
	public HashSet<Integer> getCladeToTrees() {
		return cladeToTrees;
	}
	
	public void incrementCount() {
		count++;
	}
	
	public void setCount(int count) {
		this.count = count;
	}
	
	public BitSet getCladeBits() {
		return clade;
	}
	
	public int getFrequency() {
		return count;
	}
	
}

/**
 * 
 */
package core;

import java.util.BitSet;

/**
 * @author justs
 *
 */
public class Clade {
	private BitSet clade;
	private int count;
	
	public Clade(BitSet bits) {
		clade = bits;
		count = 1;
	}
	
	public void incrementCount() {
		count++;
	}
	
	public BitSet getCladeBits() {
		return clade;
	}
	
	public int getFrequency() {
		return count;
	}
	
}

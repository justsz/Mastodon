package core;

import java.util.BitSet;
import java.util.List;


class Clade {
	int count;
	double credibility;
	BitSet bits;
	List<Object[]> attributeValues = null;
	
	public Clade(BitSet bits) {
		this.bits = bits;
		count = 0;
		credibility = 0.0;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public double getCredibility() {
		return credibility;
	}

	public void setCredibility(double credibility) {
		this.credibility = credibility;
	}

	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		final Clade clade = (Clade) o;

		return !(bits != null ? !bits.equals(clade.bits) : clade.bits != null);

	}

	public int hashCode() {
		return (bits != null ? bits.hashCode() : 0);
	}

	public String toString() {
		return "clade " + bits.toString();
	}


}
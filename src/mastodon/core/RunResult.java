/**
 * 
 */
package mastodon.core;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Map;

import jebl.evolution.taxa.Taxon;
import jebl.evolution.trees.SimpleRootedTree;

/**
 * @author justs
 *
 */
public class RunResult {
	private int minPruning;
	private int maxPruning;
	private BitTreeSystem bts;
	private List<ArrayList<Taxon>> prunedTaxa;
	private List<BitSet> prunedTaxaBits;
	private List<double[]> pruningScores;
	private List<SimpleRootedTree> prunedMapTrees;
	private Map<Taxon, Double> pruningFreq;
	private String name;
	private List<BitSet> changeStack;
	private int stackPointer;

	public RunResult(BitTreeSystem bts, List<ArrayList<Taxon>> pt, List<BitSet> ptb, List<double[]> ps, List<SimpleRootedTree> pmt, Map<Taxon, Double> pf, String name, int minK, int maxK) {
		this.bts = bts;
		prunedTaxa = pt;
		prunedTaxaBits = ptb;
		pruningScores = ps;
		prunedMapTrees = pmt;
		pruningFreq = pf;
		this.name = name;
		minPruning = minK;
		maxPruning = maxK;
		changeStack = new ArrayList<BitSet>();
		changeStack.add(new BitSet()); //put empty set at the bottom of stack
		stackPointer = 0;
	}

	public void addChange(BitSet change) {
		
		//this initial block makes the stack tree-like as opposed to storing all changes
		if (stackPointer < changeStack.size() - 1) {
			int stackSize = changeStack.size();
			for (int i = stackPointer + 1; i < stackSize; i++) {
				changeStack.remove(changeStack.size() - 1);
			}
		}
		changeStack.add(change);
		stackPointer++;
	}

	public BitSet getPrevChange() {
		BitSet output = changeStack.get(stackPointer);	
		stackPointer--;
		if (stackPointer < 0) {
			stackPointer = 0;
		}
		return output;
	}
	
	public void printStack() {
		for(int i = 0; i < changeStack.size(); i++) {
			System.out.print(changeStack.get(i));
			if (i == stackPointer) {
				System.out.print(" <-");
			}
			System.out.println();
		}
	}
	
	public BitSet getNextChange() {
		BitSet output = new BitSet();
		stackPointer++;
		if (stackPointer > changeStack.size() - 1) {
			stackPointer--;
		} else {
			output = changeStack.get(stackPointer);
		}
		return output;
	}
	
	public boolean hasPrev() {
		return stackPointer > 0;
	}
	
	public boolean hasNext() {
		return stackPointer < changeStack.size() - 1;
	}

	public List<ArrayList<Taxon>> getPrunedTaxa() {
		return prunedTaxa;
	}

	public void setPrunedTaxa(List<ArrayList<Taxon>> prunedTaxa) {
		this.prunedTaxa = prunedTaxa;
	}

	public List<double[]> getPruningScores() {
		return pruningScores;
	}

	public void setPruningScores(List<double[]> pruningScores) {
		this.pruningScores = pruningScores;
	}

	public List<SimpleRootedTree> getPrunedMapTrees() {
		return prunedMapTrees;
	}

	public void setPrunedMapTrees(List<SimpleRootedTree> prunedMapTrees) {
		this.prunedMapTrees = prunedMapTrees;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Map<Taxon, Double> getPruningFreq() {
		return pruningFreq;
	}

	public void setPruningFreq(Map<Taxon, Double> pruningFreq) {
		this.pruningFreq = pruningFreq;
	}

	public BitTreeSystem getBts() {
		return bts;
	}

	public void setBts(BitTreeSystem bts) {
		this.bts = bts;
	}

	public int getMinPruning() {
		return minPruning;
	}

	public int getMaxPruning() {
		return maxPruning;
	}

	public void setPrunedTaxaBits(List<BitSet> prunedTaxaBits) {
		this.prunedTaxaBits = prunedTaxaBits;
	}

	public List<BitSet> getPrunedTaxaBits() {
		return prunedTaxaBits;
	}

	public void updateRun(int selectedTree) {
		prunedTaxa.set(selectedTree, (ArrayList<Taxon>) bts.getTaxa(prunedTaxaBits.get(selectedTree)));
		pruningScores.set(selectedTree, bts.pruneFast(prunedTaxaBits.get(selectedTree)));
		bts.unPrune();
		prunedMapTrees.set(selectedTree, bts.reconstructMapTree(prunedTaxaBits.get(selectedTree), pruningFreq));	
		//System.out.println(prunedTaxaBits.get(selectedTree));
	}
}

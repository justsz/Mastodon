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
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Map;

import jebl.evolution.taxa.Taxon;
import jebl.evolution.trees.SimpleRootedTree;

/**
 * An object that contains all relevant information about the results of a pruning algorithm run:
 * pruning count upper and lower limits, the BitTreeSystem the run was carried out on, map of pruning frequencies of individual taxa, name of run, 
 * Lists (entry for each pruning set that resulted in the same MAP score) of pruned taxa as Taxon objects or BitSet, score ({MAP score, number of matching trees}) and the MAP tree with pruned nodes annotated.
 * There is also a change stack facility to undo and redo manual pruning actions.
 * @author justs
 *
 */
public class RunResult implements Serializable{
	private int minPruning;
	private int maxPruning;
	private transient BitTreeSystem bts;
	private transient List<ArrayList<Taxon>> prunedTaxa;
	private List<BitSet> prunedTaxaBits;
	private List<double[]> pruningScores;
	private transient List<SimpleRootedTree> prunedMapTrees;
	private transient Map<Taxon, Double> pruningFreq;
	private String name;
	private List<BitSet> changeStack;	//list of chronological changes applied to the pruning. XOR reverts the changes
	private int stackPointer;	//does not necessarily point to the top of the stack

	/**
	 * One big constructor. An empty BitSet is inserted at the bottom of the stack and pointer set to 0.
	 * @param bts - the BitTreeSystem the run was carried out on
	 * @param pt - pruned taxa
	 * @param ptb - pruned taxa as a bitset
	 * @param ps - pruning scores
	 * @param pmt - pruned map trees
	 * @param pf - pruning frequencies
	 * @param name - name of algorithm
	 * @param minK - lower pruning count limit
	 * @param maxK - upper pruning count limit
	 */
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

	
	/**
	 * Add a manual pruning to stack of changes. 
	 * Adding something while in the middle of the stack makes it the added change the new top and deletes the changes that previously followed.
	 * @param change - bitset used in manual pruning
	 */
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
	

	/**
	 * Return the next change in the stack. If the pointer is at the top of the stack, return and empty BitSet.
	 * @return next change in stack
	 */
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
	
	
	/**
	 * Return last performed changed. If the pointer is at the bottom of the stack, returns the empty BitSet placed there at creaetion.
	 * @return last performed change
	 */
	public BitSet getPrevChange() {
		BitSet output = changeStack.get(stackPointer);	
		stackPointer--;
		if (stackPointer < 0) {
			stackPointer = 0;
		}
		return output;
	}
	
	
	/**
	 * Print the stack and current pointer location to console.
	 */
	public void printStack() {
		for(int i = 0; i < changeStack.size(); i++) {
			System.out.print(changeStack.get(i));
			if (i == stackPointer) {
				System.out.print(" <-");
			}
			System.out.println();
		}
	}
	

	/**
	 * Returns if there is a previous element in the stack.
	 * @return if there is a previous element in the stack
	 */
	public boolean hasPrev() {
		return stackPointer > 0;
	}
	
	
	/**
	 * Returns if there is a next element in the stack.
	 * @return if there is a next element in the stack
	 */
	public boolean hasNext() {
		return stackPointer < changeStack.size() - 1;
	}
	
	
	/**
	 * Updates pruned taxa, pruning scores and pruned map trees if the pruning BitSet has been altered.
	 * @param selectedTree - currently displayed tree index in the tree view
	 */
	public void updateRun(int selectedTree) {
		prunedTaxa.set(selectedTree, (ArrayList<Taxon>) bts.getTaxa(prunedTaxaBits.get(selectedTree)));
		pruningScores.set(selectedTree, bts.pruneFast(prunedTaxaBits.get(selectedTree)));
		bts.unPrune();
		prunedMapTrees.set(selectedTree, bts.reconstructMapTree(prunedTaxaBits.get(selectedTree), pruningFreq));
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
}

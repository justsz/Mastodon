/**
 * 
 */
package mastodon.core;

import java.util.ArrayList;
import java.util.List;

import jebl.evolution.taxa.Taxon;
import jebl.evolution.trees.SimpleRootedTree;

/**
 * @author justs
 *
 */
public class RunResult {
	private List<ArrayList<Taxon>> prunedTaxa;
	private List<float[]> pruningScores;
	private List<SimpleRootedTree> prunedMapTrees;
	private String name;

	public RunResult(List<ArrayList<Taxon>> pt, List<float[]> ps, List<SimpleRootedTree> pmt, String name) {
		prunedTaxa = pt;
		pruningScores = ps;
		prunedMapTrees = pmt;
		this.name = name;
	}

	public List<ArrayList<Taxon>> getPrunedTaxa() {
		return prunedTaxa;
	}

	public void setPrunedTaxa(List<ArrayList<Taxon>> prunedTaxa) {
		this.prunedTaxa = prunedTaxa;
	}

	public List<float[]> getPruningScores() {
		return pruningScores;
	}

	public void setPruningScores(List<float[]> pruningScores) {
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
}

package mastodon.core;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.math3.distribution.PoissonDistribution;

import jebl.evolution.taxa.Taxon;
import jebl.evolution.trees.SimpleRootedTree;
import jebl.evolution.trees.Tree;

/**
 * @author justs
 *
 */
public abstract class Algorithm {
	private RunResult runResult;
	protected Map<BitSet, double[]> finalPruning;
	protected String stub;

	protected int minPrunedSpeciesCount;
	protected int currPrunedSpeciesCount;
	protected int maxPrunedSpeciesCount;

	protected int mapTreeIndex;
	protected int iterationCounter = 0;
	protected int runCounter = 0;

	protected BitTreeSystem bts;
	protected List<BitTree> bitTrees;

	protected double[] maxScore;
	protected double[] prevScore;
	protected double[] currScore;
	protected Map<BitSet, double[]> maxScorePruning;
	protected BitSet prevPruning;
	protected BitSet currPruning;

	protected PoissonDistribution pd;

	public abstract void setBTS(BitTreeSystem bts);
	public abstract void setLimits(Map<String, Object> limits);	
	protected abstract void initialize();	
	protected abstract boolean finished();
	protected abstract void choosePruningCount();
	protected abstract void tryPruning();
	protected abstract void setNewBest();
	protected abstract void afterActions();

	public void run() {
		//add a check to see if everything has been set correctly
		initialize();

		while (!finished()) {
			choosePruningCount();
			tryPruning();
			setNewBest();

			iterationCounter++;
		}
		System.out.println(iterationCounter);
		afterActions();		
	}

	public RunResult getRunResult() {
		List<ArrayList<Taxon>> prunedTaxa = new ArrayList<ArrayList<Taxon>>();
		List<double[]> pruningScores = new ArrayList<double[]>();
		List<SimpleRootedTree> prunedMapTrees = new ArrayList<SimpleRootedTree>();

		BitTree mapTree = bitTrees.get(mapTreeIndex).clone();

		for(Entry<BitSet, double[]> entry : finalPruning.entrySet()) {
			prunedTaxa.add((ArrayList<Taxon>) bts.getTaxa(entry.getKey()));
			pruningScores.add(entry.getValue());
			prunedMapTrees.add(bts.reconstructTree(mapTree, entry.getKey()));
		}

		String name = stub + " run " + runCounter;
		return new RunResult(prunedTaxa, pruningScores, prunedMapTrees, name);
	}

	public int getIterationCounter() {
		return iterationCounter;
	}

	public void setIterationCounter(int i) {
		iterationCounter = i;
	}
}
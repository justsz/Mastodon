package core;

import java.util.List;

import jebl.evolution.taxa.Taxon;
import jebl.evolution.trees.Tree;

/**
 * @author justs
 *
 */
public interface Algorithm {
	void run();
	List<? extends List<Taxon>> getPrunedTaxa();
	List<? extends Tree> getOutputTrees();
}


//accepts a set of trees as input
//has a run method
//has an output method that returns a list of pruned taxon and the resulting trees and their MAP scores 
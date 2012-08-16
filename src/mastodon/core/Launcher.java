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

import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.*;

import mastodon.core.*;

import jebl.evolution.io.ImportException;
import jebl.evolution.taxa.Taxon;
import jebl.evolution.trees.RootedTree;
import jebl.evolution.trees.SimpleRootedTree;


/**
 * This class deals with processing input files and launching pruning algorithms in the GUI app.
 * MastadonFrame knows the Launcher, the Launcher knows the trees. 
 * @author justs
 *
 */
public class Launcher {
	private String fileName;
	private RootedTree mapTree;
	private TreeReader reader;
	private BitTreeSystem bts;
	private Algorithm algorithm;
	private int treeCounter;
	private JFrame frame;
	private int repeatCounter = 0;
	private int oneRunIterations = 1;
	private double minMapScore = 0.0;
	private RunResult[] results;


	/**
	 * Basic constructor that sets the parent frame.
	 * @param frame - parent frame
	 */
	public Launcher(JFrame frame) {
		setFrame(frame);
	}


	/**
	 * Process a previously specified file. Apply a burnin and root on outgroup.
	 * Trees are loaded in batches of a 100 to save memory.
	 * @param burnin - first number of trees to ignore
	 * @param outgroupString - taxon to root the trees on
	 * @return true if the trees were successfully loaded
	 * @throws IOException
	 * @throws ImportException
	 */
	public boolean processFile(int burnin, String outgroupString) throws IOException {
		if(reader == null) {
			reader = new TreeReader();
		}
		bts = new BitTreeSystem();
		reader.setFile(fileName);

		treeCounter = 0;
		int readTreeCount = 0;
		if (outgroupString.length() > 0) {
			RootedTree testTree;
			try {
				testTree = reader.readNextTree();
			} catch (ImportException e) {
				testTree = null;
			}
			reader.reset();

			if(testTree == null) {
				JOptionPane.showMessageDialog(frame, "File is empty or trees are corrupted.", "Read error", JOptionPane.ERROR_MESSAGE);
				return false;
			} else {
				if (testTree.getNode(Taxon.getTaxon(outgroupString)) == null) {
					JOptionPane.showMessageDialog(frame, "Taxon \"" + outgroupString + "\" not found in tree set.", "Outgroup error", JOptionPane.ERROR_MESSAGE);
					return false;
				}
			}
		}

		List<RootedTree> trees;
		int filePosition = 0;
		do {
			filePosition++;
			if (outgroupString.length() > 0) {
				try {
					trees = reader.read100ReRootedTrees(outgroupString);
				} catch (ImportException e) {
					JOptionPane.showMessageDialog(frame, "Encountered a problem at tree " + filePosition + ".", "Read error", JOptionPane.ERROR_MESSAGE);
					return false;
				}
			} else {
				try {
					trees = reader.read100RootedTrees();
				} catch (ImportException e) {
					JOptionPane.showMessageDialog(frame, "Encountered a problem at tree " + filePosition + ".", "Read error", JOptionPane.ERROR_MESSAGE);
					return false;
				}
			}
			readTreeCount = trees.size();
			if (trees.size() < 1) {
				//mostly to check for empty file
				break;
			}

			while(burnin > 0 && trees.size() > 0) {
				trees.remove(0);
				burnin--;
			}

			if (trees.size() > 0) {
				bts.addTrees(trees);
				treeCounter += trees.size();
			}
		} while (readTreeCount == 100);

		//mark for garbage collection
		trees = null;
		reader = null;

		if (bts.getBitTrees().size() < 1) {
			JOptionPane.showMessageDialog(frame, "File " + getFileName() + " contains no trees or all were discarded in Burn-in.",
					"Error",
					JOptionPane.ERROR_MESSAGE);
			return false;
		}

		int mapTreeIndex = bts.findMapTree() + 1;	//adjusted to count from 1
		String message = "Read successful.\nFound:\n" + bts.getBitTrees().size() + " trees,\n" + bts.getTaxaCount() + " taxa,\n" + bts.getClades().size() + " unique clades.\nMap tree index: " + mapTreeIndex + ".";
		JOptionPane.showMessageDialog(frame, message, "Data set info", JOptionPane.INFORMATION_MESSAGE);

		return true;
	}


	/**
	 * Setup algorithm ready for running.
	 * @param alg - the algorithm to run
	 * @param limits - algorithm parameters
	 */
	public void setupAlgorithm(Algorithm alg, Map<String, Object> limits) {
		algorithm = alg;
		alg.setBTS(bts);
		alg.setLimits(limits);
		oneRunIterations = (Integer) limits.get("totalIterations");
		minMapScore = (Double) limits.get("minMapScore");
		alg.setIterationCounter(0);
	}


	/**
	 * Run the algorithm "repeats" number of times.
	 * @param repeats - number of times to repeat algorithm
	 */
	public void runAlgorithm(int repeats) {
		results = new RunResult[repeats];
		for(int i = 0; i < repeats; i++) {
			algorithm.run();
			results[i] = algorithm.getRunResult();
			repeatCounter++;
		}
	}


	/**
	 * Select the best result from currently available results.
	 * @return best result from currently available results
	 */
	private RunResult analyseResults() {
		List<Integer> aboveMinimumScore = new ArrayList<Integer>();
		double highestScore = 0;
		int highestScorer = 0;
		
		//first get results with score above the minimum required
		for (int i = 0; i < repeatCounter; i++) {
			double score = results[i].getPruningScores().get(0)[0];//there may be more than one scores per result, but generally they'll all be the same
			if (score > highestScore) {
				highestScore = score;
				highestScorer = i;
			}

			if (score > minMapScore) {
				aboveMinimumScore.add(i);
			}
		}

		
		if (aboveMinimumScore.size() < 1) {	//if none are above the minimum, return the highest score
			repeatCounter = 0;
			return results[highestScorer];
		} else if (aboveMinimumScore.size() == 1) {	//if only one is above the minimum, return that
			repeatCounter = 0;
			return results[aboveMinimumScore.get(0)];
		}

		ArrayList<Integer> smallestK = new ArrayList<Integer>();
		int k = 0;

		//if there are more than 1 above the minimum, select the ones with smallest number of pruned taxa
		for (Integer i : aboveMinimumScore) {
			int newK = results[i].getPrunedTaxaBits().get(0).cardinality();
			if (smallestK.size() < 1) {
				smallestK.add(i);
				k = newK;
			} else {
				if (newK < k) {
					k = newK;
					smallestK.clear();
					smallestK.add(i);
				} else if (newK == k) {
					smallestK.add(i);
				}
			}
		}

		if (smallestK.size() == 1) {	//if there's one with the smallest number of pruned taxa, return that
			repeatCounter = 0;
			return results[smallestK.get(0)];
		} else {	//if there are a number of results with the same K, return the one with the highest score
			int finalChoice = 0;
			double maxScore = 0;
			for(Integer i : smallestK) {
				double currScore = results[i].getPruningScores().get(0)[0];
				if (currScore > maxScore) {
					maxScore = currScore;
					finalChoice = i;
				}
			}
			repeatCounter = 0;
			return results[finalChoice];
		}
	}


	/**
	 * Call the algorithm's stop method.
	 */
	public void stopAlgorithm() {
		algorithm.stopAlgorithm();
	}


	/**
	 * Returns the RunResult after an algorithm run. If no algorithm has been run, set up an empty result.
	 * @return result of algorithm or empty result
	 */
	public RunResult getResults() {
		if (algorithm != null) {
			return analyseResults();
		} else {
			List<ArrayList<Taxon>> a = new ArrayList<ArrayList<Taxon>>();
			a.add(new ArrayList<Taxon>());
			List<BitSet> b = new ArrayList<BitSet>();
			b.add(new BitSet());
			List<double[]> c = new ArrayList<double[]>();
			c.add(bts.pruneFast(new BitSet()));
			bts.unPrune();
			List<SimpleRootedTree> d = new ArrayList<SimpleRootedTree>();
			d.add(bts.reconstructMapTree(null, null));
			Map<Taxon, Double> e = new HashMap<Taxon, Double>();
			for (Taxon taxon : bts.getAllTaxa()) {
				e.put(taxon, 0.0);
			}

			RunResult emptyResult = new RunResult(bts, a, b, c, d, e, "Manual", 0, 0);
			return emptyResult;
		}
	}


	/**
	 * Removes the trees currently pruned from the tree set and creates a new BitTreeSystem.
	 * @param oldLauncher - previous launcher
	 * @param pruning - taxa to be removed
	 */
	public void setCopiedAndPrunedBTS(Launcher oldLauncher, BitSet pruning) {
		BitTreeSystem oldBts = oldLauncher.getBTS();
		oldBts.pruneFast(pruning);
		Map<BitSet, BitSet> prunedClades = oldBts.getPrunedClades();
		this.bts = oldBts.createSubSystem(prunedClades);
		//bts.findMapTree();
		oldBts.unPrune();
	}

	/**
	 * Get a string of the current pruning status.
	 * @return current pruning status
	 */
	public String getStatus() {
		String out = "k=" + algorithm.getCurrPrunedSpeciesCount() + ", Best score: " + algorithm.getMaxScore()[0];
		return out;
	}


	/**
	 * Returns current input filename.
	 * @return current input filename
	 */
	public String getFileName() {
		return fileName;
	}


	/**
	 * Set file to load.
	 * @param fileName - file to load
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}


	/**
	 * Returns current number of loaded trees.
	 * @return current number of loaded trees
	 */
	public int getTreeCounter() {
		return treeCounter;
	}


	/**
	 * Returns parent frame of the Launcher.
	 * @return parent frame of the Launcher
	 */
	public JFrame getFrame() {
		return frame;
	}


	/**
	 * @param frame - parent frame
	 */
	public void setFrame(JFrame frame) {
		this.frame = frame;
	}


	/**
	 * Returns number of completed algorithm iterations.
	 * @return number of completed algorithm iterations
	 */
	public int getCurrentIterations() {
		return (repeatCounter * oneRunIterations) + algorithm.getIterationCounter();
	}


	/**
	 * Returns number of total taxa in dataset.
	 * @return number of total taxa in dataset
	 */
	public int getTaxaCount() {
		return bts.getTaxaCount();
	}


	/**
	 * Returns the MAP tree as a RootedTree instead of BitTree.
	 * @return the MAP tree
	 */
	public RootedTree getMapTree() {
		if (mapTree == null) {
			mapTree = bts.reconstructMapTree(null, null);
		}
		return mapTree;
	}


	/**
	 * Returns the underlying BitTreeSystem that contains the tree data.
	 * @return the underlying BitTreeSystem
	 */
	public BitTreeSystem getBTS() {
		return bts;
	}
}

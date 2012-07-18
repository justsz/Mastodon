/**
 * 
 */
package mastodon.entryPoints;

import jam.framework.Application;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.*;

import mastodon.algorithms.*;
import mastodon.core.*;

import jebl.evolution.io.ImportException;
import jebl.evolution.taxa.Taxon;
import jebl.evolution.trees.RootedTree;
import jebl.evolution.trees.SimpleRootedTree;
import jebl.evolution.trees.Tree;


/**
 * @author justs
 *
 */
public class Launcher {


	private String fileName;
	
	
	private double minScore;
	private int maxPruned;
	private int iterations;
	private int numberToPrune;
	private double initTemp;
	private double minTemp;
	

	private TreeReader reader;
	private BitTreeSystem bts;
	private MHBitAlgorithmBisection bisection = new MHBitAlgorithmBisection();
	private SABitAlgorithm sa = new SABitAlgorithm();
	private MHLinearAlgorithm mh = new MHLinearAlgorithm();
	
	private Algorithm algorithm;
	
	private int treeCounter;

	//	public Launcher(JFrame frame, String filename, double minScore, int maxPruned, int iterations) {
	//		setFrame(frame);
	//		setFileName(filename);
	//		setMinScore(minScore);
	//		setMaxPruned(maxPruned);
	//		setIterations(iterations);
	//		reader = new TreeReader();
	//		mh = new MHBitAlgorithm();
	//	}

	public Launcher(JFrame frame, String filename, String minScore, String maxPruned, String iterations) {
		setFrame(frame);
		setFileName(filename);
		setMinScore(minScore);
		setMaxPruned(maxPruned);
		setIterations(iterations);
		reader = new TreeReader();
		mh = new MHLinearAlgorithm();
	}

	public Launcher(JFrame frame) {
		setFrame(frame);
	}

	public boolean processFile() throws IOException, ImportException{
		if(reader == null) {
			reader = new TreeReader();
		}


		bts = new BitTreeSystem();

		reader.setFile(fileName);

		treeCounter = 0;

		List<SimpleRootedTree> trees;		
		do {
			trees = reader.read100Trees();
			if (trees.size() < 1) {
				//mostly to check for empty file
				break;
			}
			bts.addTrees(trees);
			treeCounter += trees.size();
		} while (trees.size() == 100);

		//mark for garbage collection
		boolean success = treeCounter != 0;
		trees = null;
		reader = null;

		return success;
	}
	
	public void setupAlgorithm(Algorithm alg, Map<String, Object> limits) {
		algorithm = alg;
		alg.setBTS(bts);
		alg.setLimits(limits);
		alg.setIterationCounter(0);
	}
	
	public void runAlgorithm() {
		algorithm.run();
	}
	
	public RunResult getResults() {
		return algorithm.getRunResult();
	}

	public void launchMH() {
//		mh.setTrees(bts, bts.getBitTrees());
//		mh.setLimits((double) minScore, (int) maxPruned, (int) iterations);
		mh.setIterationCounter(0);
		mh.run();
	}
	
	public void launchBisection() {
		bisection.setTrees(bts, bts.getBitTrees());
		bisection.setLimits((double) minScore, iterations);
		bisection.setIterationCounter(0);
		bisection.run();
	}
	
	public void launchSA() {
		sa.setTrees(bts, bts.getBitTrees());
		sa.setLimits(minScore, numberToPrune, iterations, initTemp, minTemp);
		sa.setIterationCounter(0);
		sa.run();
	}


	public RunResult getSAResults() {
		return sa.getRunResult();
	}

	public RunResult getMHResults() {
		return mh.getRunResult();
	}
	
	public RunResult getBisectionResults() {
		return bisection.getRunResult();
	}

	public int getTreeCounter() {
		return treeCounter;
	}


	private JFrame frame;
	/**
	 * @return the frame
	 */
	public JFrame getFrame() {
		return frame;
	}

	/**
	 * @param frame the frame to set
	 */
	public void setFrame(JFrame frame) {
		this.frame = frame;
	}

	/**
	 * @return the fileName
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * @param fileName the fileName to set
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	/**
	 * @return the minScore
	 */
	public double getMinScore() {
		return minScore;
	}

	/**
	 * @param minScore the minScore to set
	 */
	public void setMinScore(String minScore) {
		this.minScore = Double.parseDouble(minScore);
	}

	/**
	 * @return the maxPruned
	 */
	public int getMaxPruned() {
		return maxPruned;
	}

	/**
	 * @param maxPruned the maxPruned to set
	 */
	public void setMaxPruned(String maxPruned) {
		this.maxPruned = Integer.parseInt(maxPruned);
	}

	/**
	 * @return the iterations
	 */
	public int getIterations() {
		return iterations;
	}

	/**
	 * @param iterations the iterations to set
	 */
	public void setIterations(String iterations) {
		this.iterations = Integer.parseInt(iterations);
	}

	public int getCurrentIterations(int selection) {
//		switch (selection) {
//		case 1:
//			return bisection.getIterationCounter();
//		case 2:
//			return sa.getIterationCounter();
//		case 3:
//			return mh.getIterationCounter();
//		}
//		return -1;
		return algorithm.getIterationCounter();
	}

	public int getTaxaCount() {
		return bts.getTaxaCount();
	}

	public void setupMH(String[] input) {
		setMinScore(input[0]);
		setMaxPruned(input[1]);
		setIterations(input[2]);
	}
	
	public void setupBisection(String[] input) {
		setMinScore(input[0]);
		setIterations(input[1]);
	}
	
	public void setupSA(String[] input) {
		setMinScore(input[0]);
		numberToPrune = Integer.parseInt(input[1]);
		initTemp = Double.parseDouble(input[2]);
		minTemp = Double.parseDouble(input[3]);
		setIterations(input[4]);
	}
	
	public RootedTree getMapTree() {
		return bts.reconstructTree(bts.getBitTrees().get(bts.getMapTreeIndex()), null);
	}
}

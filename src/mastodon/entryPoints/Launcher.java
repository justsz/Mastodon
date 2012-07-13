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
import jebl.evolution.trees.SimpleRootedTree;
import jebl.evolution.trees.Tree;


/**
 * @author justs
 *
 */
public class Launcher {


	private String fileName;
	
	
	private float minScore;
	private int maxPruned;
	private int iterations;
	private int numberToPrune;
	private double initTemp;
	private double minTemp;
	

	private TreeReader reader;
	private BitTreeSystem bts;
	private MHBitAlgorithmBisection bisection = new MHBitAlgorithmBisection();
	private SABitAlgorithm sa = new SABitAlgorithm();
	private MHBitAlgorithm mh = new MHBitAlgorithm();
	private int treeCounter;

	//	public Launcher(JFrame frame, String filename, float minScore, int maxPruned, int iterations) {
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
		mh = new MHBitAlgorithm();
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

	public void launchMH() {
		mh.setTrees(bts, bts.getBitTrees());
		mh.setLimits((float) minScore, (int) maxPruned, (int) iterations);
		mh.setIterationCounter(0);
		mh.run();
	}
	
	public void launchBisection() {
		bisection.setTrees(bts, bts.getBitTrees());
		bisection.setLimits((float) minScore, iterations);
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
	public float getMinScore() {
		return minScore;
	}

	/**
	 * @param minScore the minScore to set
	 */
	public void setMinScore(String minScore) {
		this.minScore = Float.parseFloat(minScore);
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
		switch (selection) {
		case 1:
			return bisection.getIterationCounter();
		case 2:
			return sa.getIterationCounter();
		case 3:
			return mh.getIterationCounter();
		}
		return -1;
	}

	public int getTaxaCount() {
		return bts.getTaxaCount();
	}

	public void setupMH(String[] input) {
		setMinScore(input[0]);
		setMaxPruned(input[1]);
		setIterations(input[2]);
	}
			
	public int getMHIterationMax() {
		return iterations;
	}
	
	public void setupBisection(String[] input) {
		setMinScore(input[0]);
		setIterations(input[1]);
	}
	
	public int getBisectionIterationMax() {
		return (int) (iterations * Math.log(getTaxaCount()) / Math.log(2));
	}
	
	public void setupSA(String[] input) {
		setMinScore(input[0]);
		numberToPrune = Integer.parseInt(input[1]);
		initTemp = Double.parseDouble(input[2]);
		minTemp = Double.parseDouble(input[3]);
		setIterations(input[4]);
	}
	
	public int getSAIterationMax() {
		return (int) (iterations * Math.log(initTemp/minTemp) / Math.log(2));
	}
}

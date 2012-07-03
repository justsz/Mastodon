/**
 * 
 */
package entryPoints;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.*;

import jebl.evolution.io.ImportException;
import jebl.evolution.taxa.Taxon;
import jebl.evolution.trees.SimpleRootedTree;

import algorithms.MHBitAlgorithm;
import core.*;

/**
 * @author justs
 *
 */
public class Launcher {


	private String fileName;
	private double minScore;
	private long maxPruned;
	private long iterations;

	public Launcher(JFrame frame, String filename, double minScore, long maxPruned, long iterations) {
		setFrame(frame);
		setFileName(filename);
		setMinScore(minScore);
		setMaxPruned(maxPruned);
		setIterations(iterations);
	}

	public void launchMH() throws IOException, ImportException {
		TreeReader reader = new TreeReader();
		BitTreeSystem bts = new BitTreeSystem();
		MHBitAlgorithm algorithm = new MHBitAlgorithm();

		try {
			reader.setFile(fileName);
		} catch (IOException e) {
			System.out.println("File " + fileName + " not found.");
			System.exit(1);
		}		

		int treeCounter = 0;
		List<SimpleRootedTree> trees;		
		do {
			trees = reader.read100Trees();
			bts.addTrees(trees);
			treeCounter += trees.size();
			if (trees.size() != 0)
				System.out.println(treeCounter + "..");
		} while (trees.size() == 100);
		trees = null;
		
		//check that not pruning more than possible
		
		algorithm.setTrees(bts, bts.getBitTrees());
		algorithm.setLimits((float) minScore, (int) maxPruned, (int) iterations);
		algorithm.run();
		
		Map<ArrayList<Taxon>, float[]> result = algorithm.getTaxa();
		System.out.println(result);

		
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
	public void setMinScore(double minScore) {
		this.minScore = minScore;
	}

	/**
	 * @return the maxPruned
	 */
	public long getMaxPruned() {
		return maxPruned;
	}

	/**
	 * @param maxPruned the maxPruned to set
	 */
	public void setMaxPruned(long maxPruned) {
		this.maxPruned = maxPruned;
	}

	/**
	 * @return the iterations
	 */
	public long getIterations() {
		return iterations;
	}

	/**
	 * @param iterations the iterations to set
	 */
	public void setIterations(long iterations) {
		this.iterations = iterations;
	}
}

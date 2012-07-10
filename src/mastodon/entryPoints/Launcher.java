/**
 * 
 */
package mastodon.entryPoints;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.*;

import mastodon.algorithms.MHBitAlgorithm;
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

	private TreeReader reader;
	private BitTreeSystem bts;
	private MHBitAlgorithm mh;
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

	public void launchMH() throws IOException, ImportException {
		//need to add close actions to frames
		if (!fileName.equals(reader.getFile())) {
			bts = new BitTreeSystem();

			try {
				reader.setFile(fileName);
			} catch (IOException e) {
				System.out.println("File " + fileName + " not found.");
				System.exit(1);
			}	


			treeCounter = 0;
			mh.setIterationCounter(0);
			List<SimpleRootedTree> trees;		
			do {
				trees = reader.read100Trees();
				bts.addTrees(trees);
				treeCounter += trees.size();
				if (trees.size() != 0)
					System.out.println(treeCounter + "..");
			} while (trees.size() == 100);
			trees = null;
		}

		//check that not pruning more than possible
		if (bts.getAllTaxa().size() <= maxPruned) {			
			JOptionPane.showMessageDialog(frame,
					"Can't prune more taxa than are present in the tree.", "Error Massage",
					JOptionPane.ERROR_MESSAGE);
		} else {

			mh.setTrees(bts, bts.getBitTrees());
			mh.setLimits((float) minScore, (int) maxPruned, (int) iterations);
			mh.run();
		}

	}
	
	public RunResult getResults() {
		return mh.getRunResult();
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
	
	public int getCurrentIterations() {
		return mh.getIterationCounter();
	}
}

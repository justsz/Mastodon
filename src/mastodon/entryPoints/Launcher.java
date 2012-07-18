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
	
	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}


	private TreeReader reader;
	private BitTreeSystem bts;	
	private Algorithm algorithm;
	
	private int treeCounter;

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

	public int getCurrentIterations(int selection) {
		return algorithm.getIterationCounter();
	}

	public int getTaxaCount() {
		return bts.getTaxaCount();
	}
	
	public RootedTree getMapTree() {
		return bts.reconstructTree(bts.getBitTrees().get(bts.getMapTreeIndex()), null);
	}
}

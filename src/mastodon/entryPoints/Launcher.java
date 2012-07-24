/**
 * 
 */
package mastodon.entryPoints;

import jam.framework.Application;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
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

	public boolean processFile(int burnin) throws IOException, ImportException{
		if(reader == null) {
			reader = new TreeReader();
		}
		bts = new BitTreeSystem();

		reader.setFile(fileName);

		treeCounter = 0;
		int readTreeCount = 0;

		List<SimpleRootedTree> trees;		
		do {
			trees = reader.read100Trees();
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
		boolean success = treeCounter != 0;
		trees = null;
		reader = null;
		
		if(success) {
			bts.findMapTree();
			String message = "Read successful.\nFound:\n" + bts.getBitTrees().size() + " trees,\n" + bts.getTaxaCount() + " taxa,\n" + bts.getClades().size() + " unique clades.";
			JOptionPane.showMessageDialog(frame, message, "Data set info", JOptionPane.INFORMATION_MESSAGE);
		}

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
		if (algorithm != null) {
			return algorithm.getRunResult();
		} else {
			List<ArrayList<Taxon>> a = new ArrayList<ArrayList<Taxon>>();
			a.add(new ArrayList<Taxon>());
			List<BitSet> b = new ArrayList<BitSet>();
			b.add(new BitSet());
			List<double[]> c = new ArrayList<double[]>();
			c.add(new double[] {0,0});
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
		return bts.reconstructMapTree(null, null);
	}
}

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
		JDialog dialog = new JDialog(frame, "Progress");
		JProgressBar progressBar = new JProgressBar(0, 100);
		progressBar.setValue(0);
		progressBar.setIndeterminate(true);
		progressBar.setStringPainted(true);
		dialog.setLocation(500, 500);


		String progressText = "Processing trees.";
		JTextArea addingProgress = new JTextArea(progressText);
		dialog.add(addingProgress);
		
		dialog.add(progressBar);
		dialog.pack();
		
		dialog.setVisible(true);

		if (!fileName.equals(reader.getFile())) {
			bts = new BitTreeSystem();

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
				progressText += "\n" + treeCounter + "..";
				addingProgress.setText(progressText);
				//addingProgress.repaint();
				//frame.repaint();
				dialog.repaint();
			} while (trees.size() == 100);
			trees = null;
		}

		progressBar.setIndeterminate(false);
		//check that not pruning more than possible

		if (bts.getAllTaxa().size() <= maxPruned) {
			dialog.setVisible(false);
			
//			JOptionPane warn = new JOptionPane("Can't prune more taxa than are present in the tree.", JOptionPane.ERROR_MESSAGE);
//			JDialog warning = warn.createDialog("Error");
//			warning.setVisible(true);
			
			
			JOptionPane.showMessageDialog(frame,
					"Can't prune more taxa than are present in the tree.", "Error Massage",
					JOptionPane.ERROR_MESSAGE);
			
			//return new ArrayList<SimpleRootedTree>();
		} else {

			mh.setTrees(bts, bts.getBitTrees());
			mh.setLimits((float) minScore, (int) maxPruned, (int) iterations);
			mh.run();

			dialog.setVisible(false);

			Map<ArrayList<Taxon>, float[]> result = mh.getTaxa();
			JDialog results = new JDialog(frame, "Results");

			String resultString = "";		
			for(ArrayList<Taxon> taxaList : result.keySet()) {
				for (Taxon taxon : taxaList) {
					resultString += taxon.getName() + "\n";
				}
				resultString += "[" + result.get(taxaList)[0] + ", " + (int) result.get(taxaList)[1] + "]\n";
			}
			
			JTextArea text = new JTextArea(resultString);
			results.add(text);
			results.pack();
			results.setVisible(true);

			//return mh.getHighlightedPrunedMapTrees();
		}

	}
	
	public RunResult getResults() {
		return mh.getRunResult();
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
}

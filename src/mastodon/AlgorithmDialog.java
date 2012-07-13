/**
 * 
 */
package mastodon;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.FileDialog;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.text.NumberFormat;

import jam.panels.OptionsPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import mastodon.inputVerifiers.*;

/**
 * @author justs
 *
 */
public class AlgorithmDialog {
	JFrame frame;
	String fileName;
	JLabel selectedData;
	JPanel cardPanel;

	int selection = 1;
	
	private AbstractAction bisectionAction = new AbstractAction("Bisection") {
		public void actionPerformed(ActionEvent ae) {
			selection = 1;
			((CardLayout)cardPanel.getLayout()).show(cardPanel, "bisection");
		}
	};
	
	private AbstractAction SAAction = new AbstractAction("Simulated Annealing") {
		public void actionPerformed(ActionEvent ae) {
			selection = 2;
			((CardLayout)cardPanel.getLayout()).show(cardPanel, "SA");
		}
	};
	
	private AbstractAction MHAction = new AbstractAction("Metropolis Hastings") {
		public void actionPerformed(ActionEvent ae) {
			selection = 3;
			((CardLayout)cardPanel.getLayout()).show(cardPanel, "MH");
		}
	};
	
	

	private final JDialog dialog;
	private final JOptionPane optionPane;

	
	
	JTextField minBisectionScore = new JTextField("0.0");	
	JTextField bisectionIterations = new JTextField("1");
	
	JTextField minSAScore = new JTextField("0.0");
	JTextField numberToPrune = new JTextField("1");
	JTextField initialTemp = new JTextField("1");
	JTextField minTemp = new JTextField("1");
	JTextField SAIterations = new JTextField("1");
	
	JTextField minMHScore = new JTextField("0.0");
	JTextField maxPruning = new JTextField("1");
	JTextField MHIterations = new JTextField("1");
	
	JRadioButton bisectionButton = new JRadioButton(bisectionAction);
	JRadioButton MHButton = new JRadioButton(MHAction);
	JRadioButton SAButton = new JRadioButton(SAAction);
	
	

	
	
	public AlgorithmDialog(JFrame frame) {
		this.frame = frame;
		
		cardPanel = new JPanel(new CardLayout());
		
		
		OptionsPanel bisectionOptions = new OptionsPanel(12, 12);
		OptionsPanel SAOptions = new OptionsPanel(12, 12);
		OptionsPanel MHOptions = new OptionsPanel(12, 12);
		
		minBisectionScore.setColumns(10);
		bisectionIterations.setColumns(10);
		
		minSAScore.setColumns(10);
		numberToPrune.setColumns(10);
		initialTemp.setColumns(10);
		minTemp.setColumns(10);
		SAIterations.setColumns(10);
		
		
		minMHScore.setColumns(10);
		maxPruning.setColumns(10);
		MHIterations.setColumns(10);
		
//		JPanel bisectionPanel = new JPanel();
//		bisectionPanel.add(minScore);
//		bisectionPanel.add(bisectionIterations);
		
		JPanel buttonsPanel = new JPanel();
		ButtonGroup group = new ButtonGroup();
		bisectionButton.setSelected(true);
		group.add(bisectionButton);
		group.add(MHButton);
		group.add(SAButton);
		buttonsPanel.add(bisectionButton);
		buttonsPanel.add(SAButton);
		buttonsPanel.add(MHButton);
		
		
		
		
		
		bisectionOptions.addComponentWithLabel("Minimum MAP score[fraction]", minBisectionScore);
		bisectionOptions.addComponentWithLabel("Number of iterations to spend in each step[1+]", bisectionIterations);

		SAOptions.addComponentWithLabel("Minimum MAP score[fraction]", minSAScore);
		SAOptions.addComponentWithLabel("Number of taxa to prune[1+]", numberToPrune);
		SAOptions.addComponentWithLabel("Initial temperature[0+]", initialTemp);
		SAOptions.addComponentWithLabel("Minimum temperature[0+]", minTemp);
		SAOptions.addComponentWithLabel("Maximum number of iterations[1+]", SAIterations);
		
		MHOptions.addComponentWithLabel("Minimum MAP score[fraction]", minMHScore);
		MHOptions.addComponentWithLabel("Maximum number of taxa to prune[1+]", maxPruning);
		MHOptions.addComponentWithLabel("Maximum number of iterations[1+]", MHIterations);		
		

		cardPanel.add(bisectionOptions, "bisection");
		cardPanel.add(SAOptions, "SA");
		cardPanel.add(MHOptions, "MH");
		
		JPanel buttonsAndFields = new JPanel(new BorderLayout(0, 0));
		buttonsAndFields.add(buttonsPanel, BorderLayout.NORTH);
		buttonsAndFields.add(cardPanel, BorderLayout.SOUTH);

		optionPane = new JOptionPane(buttonsAndFields,
				JOptionPane.PLAIN_MESSAGE,
				JOptionPane.OK_CANCEL_OPTION,
				null,
				null,
				null);
		optionPane.setBorder(new EmptyBorder(12, 12, 12, 12));
		
		

		
		
		dialog = optionPane.createDialog(frame, "Select pruning algorithm");
		dialog.pack();

	}

	public int showDialog() {
		dialog.setVisible(true);
		int result = JOptionPane.CANCEL_OPTION;
		Integer value = (Integer)optionPane.getValue();
		if (value != null && value.intValue() != -1) {
			result = value.intValue();
		}

		if (result == JOptionPane.OK_OPTION) {
		}

		return result;
	}
	
//	public void setVisible(boolean b) {
//		dialog.setVisible(b);
//	}
	
	public String getFile() {
		return fileName;
	}
	
	public String getMinScore() {
		return minMHScore.getText();
	}
	
	public String getMaxPrunedTaxa() {
		return maxPruning.getText();
	}
	
	public String getIterations() {
		return MHIterations.getText();
		
	}
	
	public int getSelection() {
		return selection;
	}
	
	public String[] getBisectionInput() {
		String[] input = new String[2];
		input[0] = minBisectionScore.getText();
		input[1] = bisectionIterations.getText();
		return input;
	}
	
	public String[] getSAInput() {
		String[] input = new String[5];
		input[0] = minSAScore.getText();
		input[1] = numberToPrune.getText();
		input[2] = initialTemp.getText();
		input[3] = minTemp.getText();
		input[4] = SAIterations.getText();		
		return input;
	}
	
	public String[] getMHInput() {
		String[] input = new String[3];
		input[0] = minMHScore.getText();
		input[1] = maxPruning.getText();
		input[2] = MHIterations.getText();
		return input;
	}
	

}

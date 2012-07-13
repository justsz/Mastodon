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

	
	private AbstractAction buttonAction = new AbstractAction("Select Data") {
		public void actionPerformed(ActionEvent ae) {
			FileDialog dialog = new FileDialog(frame, "Select Data", FileDialog.LOAD);
			dialog.setVisible(true);
			String file = dialog.getFile();
			if (file != null) {
				fileName = dialog.getDirectory() + file;
			}
			selectedData.setText(file);
		}
	};
	
	private AbstractAction bisectionAction = new AbstractAction("Bisection") {
		public void actionPerformed(ActionEvent ae) {
			//add a check to see if you're already on the bisection view?
			((CardLayout)cardPanel.getLayout()).show(cardPanel, "bisection");
		}
	};
	
	private AbstractAction MHAction = new AbstractAction("Metropolis Hastings") {
		public void actionPerformed(ActionEvent ae) {
			//add a check to see if you're already on the bisection view?
			((CardLayout)cardPanel.getLayout()).show(cardPanel, "MH");
		}
	};
	
	private AbstractAction SAAction = new AbstractAction("Simulated Annealing") {
		public void actionPerformed(ActionEvent ae) {
			//add a check to see if you're already on the bisection view?
			((CardLayout)cardPanel.getLayout()).show(cardPanel, "SA");
		}
	};

	private final JDialog dialog;
	private final JOptionPane optionPane;

	private JButton openButton = new JButton(buttonAction);
	
	JTextField minBisectionScore = new JTextField("0.0");	
	JTextField bisectionIterations = new JTextField("1");
	
	JTextField minSAScore = new JTextField("0.0");
	JTextField numberToPrune = new JTextField("1");
	JTextField SAIterations = new JTextField("1");
	JTextField initialTemp = new JTextField("1");
	JTextField minTemp = new JTextField("1");
	
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
		SAIterations.setColumns(10);
		initialTemp.setColumns(10);
		minTemp.setColumns(10);
		
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
		buttonsPanel.add(MHButton);
		buttonsPanel.add(SAButton);
		
		
		bisectionOptions.addComponentWithLabel("Minimum MAP score[fraction]", minBisectionScore);
		bisectionOptions.addComponentWithLabel("Number of iterations to spend in each step[1+]", bisectionIterations);

		SAOptions.addComponentWithLabel("Minimum MAP score[fraction]", minSAScore);
		SAOptions.addComponentWithLabel("Number of taxa to prune[1+]", numberToPrune);
		SAOptions.addComponentWithLabel("Maximum number of iterations[1+]", SAIterations);
		SAOptions.addComponentWithLabel("Initial temperature[0+]", initialTemp);
		SAOptions.addComponentWithLabel("Minimum temperature[0+]", minTemp);
		
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

}

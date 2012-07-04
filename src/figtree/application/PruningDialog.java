/**
 * 
 */
package figtree.application;

import java.awt.FileDialog;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.text.NumberFormat;

import jam.panels.OptionsPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * @author justs
 *
 */
public class PruningDialog implements PropertyChangeListener{
	JFrame frame;
	String fileName;
	JLabel selectedData;


	private JFileChooser fileChooser = new JFileChooser();
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

	private final JDialog dialog;
	private final JOptionPane optionPane;

	private JButton openButton = new JButton(buttonAction);
	JFormattedTextField minScore; 
	JFormattedTextField maxPruning = new JFormattedTextField(NumberFormat.getIntegerInstance());
	JFormattedTextField iterations = new JFormattedTextField(NumberFormat.getIntegerInstance());

	public void propertyChange(PropertyChangeEvent e) {
		if (e.getSource() == minScore) {
			Object val = minScore.getValue();
			if (val != null) {
				double value = ((Number) val).doubleValue();
				if (value > 1.0 || value < 0) {
					minScore.setValue(0);
				}
			}
		}
	}

	public PruningDialog(JFrame frame) {
		this.frame = frame;
		
		OptionsPanel options = new OptionsPanel(12, 12);

		NumberFormat fractionFormat = NumberFormat.getNumberInstance();
		fractionFormat.setMinimumFractionDigits(1);
		fractionFormat.setMaximumFractionDigits(10);
		minScore = new JFormattedTextField(fractionFormat);
		
		
		minScore.addPropertyChangeListener("value", this);

		minScore.setColumns(10);
		maxPruning.setColumns(10);
		minScore.setValue(0);
		iterations.setColumns(10);



		selectedData = options.addComponentWithLabel("Data to prune", openButton);
		options.addComponentWithLabel("Minimum MAP score", minScore);
		options.addComponentWithLabel("Maximum number of taxa to prune", maxPruning);
		options.addComponentWithLabel("Maximum number of iterations", iterations);



		//options.addComponent(fileChooser);

		optionPane = new JOptionPane(options,
				JOptionPane.PLAIN_MESSAGE,
				JOptionPane.OK_CANCEL_OPTION,
				null,
				null,
				null);
		optionPane.setBorder(new EmptyBorder(12, 12, 12, 12));

		dialog = optionPane.createDialog(frame, "Pruning trees");
		dialog.pack();

		//dialog.setVisible(true);		
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
	
	public void setVisible(boolean b) {
		dialog.setVisible(b);
	}
	
	public String getFile() {
		return fileName;
	}
	
	public double getMinScore() {
		return (Double) minScore.getValue();
	}
	
	public long getMaxPrunedTaxa() {
		return (Long) maxPruning.getValue();
	}
	
	public long getIterations() {
		return (Long) iterations.getValue();
	}

}

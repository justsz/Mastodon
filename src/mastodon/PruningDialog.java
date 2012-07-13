/**
 * 
 */
package mastodon;

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
//implements PropertyChangeListener
public class PruningDialog {
	JFrame frame;
	String fileName;
	JLabel selectedData;

	
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
//	JFormattedTextField minScore; 
//	JFormattedTextField maxPruning = new JFormattedTextField(NumberFormat.getIntegerInstance());
//	JFormattedTextField iterations = new JFormattedTextField(NumberFormat.getIntegerInstance());
	
	JTextField minScore = new JTextField("0.0");
	JTextField maxPruning = new JTextField("1");
	JTextField iterations = new JTextField("1");

//	public void propertyChange(PropertyChangeEvent e) {
//		if (e.getSource() == minScore) {
//			Object val = minScore.getValue();
//			if (val != null) {
//				double value = ((Number) val).doubleValue();
//				if (value > 1.0 || value < 0) {
//					minScore.setValue(0);
//				}
//			}
//		} else if (e.getSource() == maxPruning || e.getSource() == iterations) {
//			Object val = ((JFormattedTextField) e.getSource()).getValue();
//			if (val != null) {
//				double value = ((Number) val).intValue();
//				if (value < 0) {
//					((JFormattedTextField) e.getSource()).setValue(-value);
//				} else if (value < 1) {
//					((JFormattedTextField) e.getSource()).setValue(1);
//				}
//			}
//		}
//	}
	
	
	public PruningDialog(JFrame frame) {
		this.frame = frame;
		
		OptionsPanel options = new OptionsPanel(12, 12);

//		NumberFormat fractionFormat = NumberFormat.getNumberInstance();
//		fractionFormat.setMinimumFractionDigits(1);
//		fractionFormat.setMaximumFractionDigits(10);
//		minScore = new JFormattedTextField(fractionFormat);		
		

		minScore.setColumns(10);
		maxPruning.setColumns(10);
		iterations.setColumns(10);
		
//		minScore.setValue(0.0);
//		maxPruning.setValue(1l);
//		iterations.setValue(1l);
		
//		minScore.addPropertyChangeListener("value", this);
//		maxPruning.addPropertyChangeListener("value", this);
//		iterations.addPropertyChangeListener("value", this);

//		minScore.addPropertyChangeListener(this);
//		maxPruning.addPropertyChangeListener(this);
//		iterations.addPropertyChangeListener(this);
		
//		minScore.setInputVerifier(new FractionVerifier());
//		maxPruning.setInputVerifier(new IntegerVerifier());
//		iterations.setInputVerifier(new IntegerVerifier());
		


		selectedData = options.addComponentWithLabel("Data to prune", openButton);
		options.addComponentWithLabel("Minimum MAP score[fraction]", minScore);
		options.addComponentWithLabel("Maximum number of taxa to prune[1+]", maxPruning);
		options.addComponentWithLabel("Maximum number of iterations[1+]", iterations);



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
	
	public String getMinScore() {
//		Object value = minScore.getValue();
//		if (value instanceof Double) {
//			return (Double) value;
//		} else {
//			//otherwise it's a long, 0 or 1
//			return ((Long) value).doubleValue();
//		}
//		return Float.parseFloat(minScore.getText());
		return minScore.getText();
	}
	
	public String getMaxPrunedTaxa() {
		//return (Long) maxPruning.getValue();
		//return Integer.parseInt(maxPruning.getText());
		return maxPruning.getText();
	}
	
	public String getIterations() {
		//return (Long) iterations.getValue();
		//return Integer.parseInt(iterations.getText());
		return iterations.getText();
		
	}

}

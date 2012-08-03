/**
 * 
 */
package mastodon;


import java.awt.CardLayout;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;

import jam.panels.OptionsPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * This class creates a dialog that the user can interact with to setup a pruning algorithm.
 * @author justs
 *
 */
public class AlgorithmDialog {
	JFrame frame;
	JLabel selectedData;
	JPanel searchMethodCardPanel;
	JPanel algorithmChoiceCardPanel;

	//store algorithm choice as a 2 digit integer and decode as needed. Search method is in the tens and algorithm in the ones
	int searchMethodSelection = 10;
	int algorithmSelection = 1;
	
	
	//---Button actions---
	
	private AbstractAction constAction = new AbstractAction("Constant") {
		public void actionPerformed(ActionEvent ae) {
			searchMethodSelection = 10;
			((CardLayout)searchMethodCardPanel.getLayout()).show(searchMethodCardPanel, "const");
		}
	};
	
	private AbstractAction linearAction = new AbstractAction("Linear") {
		public void actionPerformed(ActionEvent ae) {
			searchMethodSelection = 20;
			((CardLayout)searchMethodCardPanel.getLayout()).show(searchMethodCardPanel, "linAndBis");
		}
	};
	
	private AbstractAction bisectionAction = new AbstractAction("Bisection") {
		public void actionPerformed(ActionEvent ae) {
			searchMethodSelection = 30;
			((CardLayout)searchMethodCardPanel.getLayout()).show(searchMethodCardPanel, "linAndBis");
		}
	};
	
	private AbstractAction flipPenaltyAction = new AbstractAction("Flip Penalty") {
		public void actionPerformed(ActionEvent ae) {
			searchMethodSelection = 40;
			((CardLayout)searchMethodCardPanel.getLayout()).show(searchMethodCardPanel, "linAndBis");
		}
	};
	
	
	private AbstractAction SAAction = new AbstractAction("Simulated Annealing") {
		public void actionPerformed(ActionEvent ae) {
			algorithmSelection = 1;
			((CardLayout)algorithmChoiceCardPanel.getLayout()).show(algorithmChoiceCardPanel, "SA");
		}
	};
	
	private AbstractAction MHAction = new AbstractAction("Metropolis Hastings") {
		public void actionPerformed(ActionEvent ae) {
			algorithmSelection = 2;
			((CardLayout)algorithmChoiceCardPanel.getLayout()).show(algorithmChoiceCardPanel, "MH");
		}
	};
	
	

	private final JDialog dialog;
	private final JOptionPane optionPane;

	
	//components
	JTextField numberToPrune = new JTextField("1");
	JTextField minNumberToPrune = new JTextField("1");
	JTextField maxNumberToPrune = new JTextField("1");
	
	JTextField minMapScore = new JTextField("0.0");	
	JTextField totalIterations = new JTextField("1");

	JTextField power = new JTextField("1.0");
	
	JTextField initialTemp = new JTextField("1.0");
	JTextField finalTemp = new JTextField("0.1");	
	
	JRadioButton constButton = new JRadioButton(constAction);
	JRadioButton linearButton = new JRadioButton(linearAction);
	JRadioButton bisectionButton = new JRadioButton(bisectionAction);
	JRadioButton flipPenaltyButton = new JRadioButton(flipPenaltyAction);
	
	JRadioButton MHButton = new JRadioButton(MHAction);
	JRadioButton SAButton = new JRadioButton(SAAction);
	
	

	
	/**
	 * Sets up all fields and creates a dialog window for pruning algorithm selection.
	 * CardLayout is used to swap between different input option depending on the user choice of algorithm.
	 * @param frame - parent frame of this dialog
	 */
	public AlgorithmDialog(JFrame frame) {
		this.frame = frame;
		
		searchMethodCardPanel = new JPanel(new CardLayout());
		algorithmChoiceCardPanel = new JPanel(new CardLayout());		
		
		OptionsPanel constOptions = new OptionsPanel(12, 12, SwingConstants.RIGHT);
		OptionsPanel linearAndBisectionOptions = new OptionsPanel(12, 12, SwingConstants.RIGHT);
		
		OptionsPanel MHOptions = new OptionsPanel(12, 12, SwingConstants.RIGHT);
		
		OptionsPanel SAOptions = new OptionsPanel(12, 12, SwingConstants.RIGHT);
		
		OptionsPanel overallOptions = new OptionsPanel(12, 12, SwingConstants.RIGHT);

		numberToPrune.setColumns(5);
		minNumberToPrune.setColumns(5);
		maxNumberToPrune.setColumns(5);
		
		minMapScore.setColumns(10);
		totalIterations.setColumns(10);
		
		power.setColumns(10);
		
		initialTemp.setColumns(10);
		finalTemp.setColumns(10);
		
//		JPanel bisectionPanel = new JPanel();
//		bisectionPanel.add(minScore);
//		bisectionPanel.add(bisectionIterations);
		
		JPanel searchMethodButtonsPanel = new JPanel();
		ButtonGroup searchGroup = new ButtonGroup();
		constButton.setSelected(true);
		searchGroup.add(constButton);
		searchGroup.add(linearButton);
		searchGroup.add(bisectionButton);
		searchGroup.add(flipPenaltyButton);
		searchMethodButtonsPanel.add(constButton);
		searchMethodButtonsPanel.add(linearButton);
		searchMethodButtonsPanel.add(bisectionButton);
		searchMethodButtonsPanel.add(flipPenaltyButton);
		
		JPanel algorithmChoiceButtonsPanel = new JPanel();
		ButtonGroup algGroup = new ButtonGroup();
		SAButton.setSelected(true);
		algGroup.add(SAButton);
		algGroup.add(MHButton);
		algorithmChoiceButtonsPanel.add(SAButton);
		algorithmChoiceButtonsPanel.add(MHButton);
		
		constOptions.addComponentWithLabel("Number of taxa to prune[1+]", numberToPrune);

		linearAndBisectionOptions.addComponentWithLabel("Min number of taxa to prune[1+]", minNumberToPrune);
		linearAndBisectionOptions.addComponentWithLabel("Max number of taxa to prune[1+]", maxNumberToPrune);
		
		MHOptions.addComponentWithLabel("Weighing power[>0]", power);

		SAOptions.addComponentWithLabel("Initial temperature[>0]", initialTemp);
		SAOptions.addComponentWithLabel("Final temperature[>0]", finalTemp);
		
		overallOptions.addComponentWithLabel("Total iterations[1+]", totalIterations);
		overallOptions.addComponentWithLabel("Desired MAP score[0.0-1.0]", minMapScore);

		searchMethodCardPanel.add(constOptions, "const");
		searchMethodCardPanel.add(linearAndBisectionOptions, "linAndBis");
		
		algorithmChoiceCardPanel.add(SAOptions, "SA");
		algorithmChoiceCardPanel.add(MHOptions, "MH");
		
		JPanel buttonsAndFields = new JPanel();
		buttonsAndFields.setLayout(new BoxLayout(buttonsAndFields, BoxLayout.Y_AXIS));
		//buttonsAndFields.add(new JLabel("Search method to use"));
		buttonsAndFields.add(searchMethodButtonsPanel);
		buttonsAndFields.add(searchMethodCardPanel);
		buttonsAndFields.add(new JSeparator(SwingConstants.HORIZONTAL));
		//buttonsAndFields.add(new JLabel("Algorithm to use"));
		buttonsAndFields.add(algorithmChoiceButtonsPanel);
		buttonsAndFields.add(algorithmChoiceCardPanel);
		buttonsAndFields.add(new JSeparator(SwingConstants.HORIZONTAL));
		buttonsAndFields.add(overallOptions);

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

	/**
	 * Display the dialog and get user button click.
	 * @return integer representing which button the user clicked
	 */
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
	
	/**
	 * Returns the 2 digit integer representing algorithm choice.
	 * @return 2 digit integer representing algorithm choice
	 */
	public int getSelection() {
		return searchMethodSelection + algorithmSelection;
	}
	
	/**
	 * Collects all text entered into al fields and returns as a map.
	 * @return map of all inputs
	 */
	public Map<String, String> getInput() {
		Map<String, String> input = new HashMap<String, String>();
		
		input.put("minMapScore", minMapScore.getText());
		input.put("totalIterations", totalIterations.getText());
		input.put("minPruning", minNumberToPrune.getText());
		input.put("maxPruning", maxNumberToPrune.getText());
		input.put("constPruning", numberToPrune.getText());
		input.put("power", power.getText());
		input.put("initTemp", initialTemp.getText());
		input.put("finalTemp", finalTemp.getText());
		
		return input;
	}
	

}

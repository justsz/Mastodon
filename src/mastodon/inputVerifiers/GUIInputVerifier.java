/**
 * 
 */
package mastodon.inputVerifiers;

import javax.swing.JOptionPane;

/**
 * @author justs
 *
 */
public class GUIInputVerifier {
	public static boolean verifyMHAlgorithmInput(String[] input, int taxaCount) {
		String errorMessage = "";
		boolean isValid = false;
		
		String minScore = input[0];
		String maxPruning = input[1];
		String iterations = input[2];
		
		try {
			float value = Float.parseFloat(minScore);
			if (value > 1.0 || value < 0) {
				errorMessage += "Minimum score " + minScore + " is out of bounds. Expected 0.0 - 1.0\n";
				isValid = false;
			} else {
				isValid = true;
			}
		} catch (NumberFormatException e) {
			errorMessage += "Minimum score " + minScore + " is not a number.\n";
			isValid = false;
		}
		
		
		try {
			int value = Integer.parseInt(maxPruning);
			if (value < 1 || value > taxaCount) {
				errorMessage += "Maximum pruning " + maxPruning + " is out of bounds. Expected 1 - " + taxaCount + ".\n";
				isValid = false;
			} else {
				isValid = isValid & true;
			}
		} catch (NumberFormatException e) {
			errorMessage += "Maximum pruning " + maxPruning + " is not a number or not an integer.\n";
			isValid = false;
		}
		
		
		try {
			int value = Integer.parseInt(iterations);
			if (value < 1) {
				errorMessage += "Iterations " + iterations + " is out of bounds. Expected 1+.\n";
				isValid = false;
			} else {
				isValid = isValid & true;
			}
		} catch (NumberFormatException e) {
			errorMessage += "Iterations " + iterations + " is not a number or not an integer.\n";
			isValid = false;
		}
		
		if (!isValid) {
			JOptionPane.showMessageDialog(null,
					errorMessage, "Invalid input",
					JOptionPane.ERROR_MESSAGE);
		}
		
		return isValid;
	}
	
	
	
	public static boolean verifySAAlgorithmInput(String[] input, int taxaCount) {
		String errorMessage = "";
		boolean isValid = false;
		
		String minScore = input[0];
		String numberToPrune = input[1];
		String initTemp = input[2];
		String minTemp = input[3];
		String iterations = input[4];
		
		try {
			float value = Float.parseFloat(minScore);
			if (value > 1.0 || value < 0) {
				errorMessage += "Minimum score " + minScore + " is out of bounds. Expected 0.0 - 1.0\n";
				isValid = false;
			} else {
				isValid = true;
			}
		} catch (NumberFormatException e) {
			errorMessage += "Minimum score " + minScore + " is not a number.\n";
			isValid = false;
		}
		
		try {
			int value = Integer.parseInt(numberToPrune);
			if (value < 1 || value > taxaCount) {
				errorMessage += "Maximum pruning " + numberToPrune + " is out of bounds. Expected 1 - " + taxaCount + ".\n";
				isValid = false;
			} else {
				isValid = isValid & true;
			}
		} catch (NumberFormatException e) {
			errorMessage += "Number to prune " + numberToPrune + " is not a number or not an integer.\n";
			isValid = false;
		}
		
		try {
			double value = Double.parseDouble(initTemp);
			if (value < 0) {
				errorMessage += "Initial temperature " + initTemp + " is out of bounds. Expected 0+.\n";
				isValid = false;
			} else {
				isValid = isValid & true;
			}
		} catch (NumberFormatException e) {
			errorMessage += "Initial temperature " + numberToPrune + " is not a number.\n";
			isValid = false;
		}
		
		try {
			double value = Double.parseDouble(minTemp);
			if (value < 0) {
				errorMessage += "Minimum temperature " + minTemp + " is out of bounds. Expected 0+.\n";
				isValid = false;
			} else {
				isValid = isValid & true;
			}
		} catch (NumberFormatException e) {
			errorMessage += "Minimum temperatuer " + minTemp + " is not a number.\n";
			isValid = false;
		}
		
		
		try {
			int value = Integer.parseInt(iterations);
			if (value < 1) {
				errorMessage += "Iterations " + iterations + " is out of bounds. Expected 1+.\n";
				isValid = false;
			} else {
				isValid = isValid & true;
			}
		} catch (NumberFormatException e) {
			errorMessage += "Iterations " + iterations + " is not a number or not an integer.\n";
			isValid = false;
		}
		
		if (!isValid) {
			JOptionPane.showMessageDialog(null,
					errorMessage, "Invalid input",
					JOptionPane.ERROR_MESSAGE);
		}
		
		return isValid;
	}
	
	
	
	public static boolean verifyBisectionAlgorithmInput(String[] input) {
		String errorMessage = "";
		boolean isValid = false;
		
		String minScore = input[0];
		String stepIterations = input[1];
		
		try {
			float value = Float.parseFloat(minScore);
			if (value > 1.0 || value < 0) {
				errorMessage += "Minimum score " + minScore + " is out of bounds. Expected 0.0 - 1.0\n";
				isValid = false;
			} else {
				isValid = true;
			}
		} catch (NumberFormatException e) {
			errorMessage += "Minimum score " + minScore + " is not a number.\n";
			isValid = false;
		}
		
		try {
			int value = Integer.parseInt(stepIterations);
			if (value < 0) {
				errorMessage += "Step iterations " + stepIterations + " is out of bounds. Expected 1+.\n";
				isValid = false;
			} else {
				isValid = isValid & true;
			}
		} catch (NumberFormatException e) {
			errorMessage += "Step iterations " + stepIterations + " is not a number or not an integer.\n";
			isValid = false;
		}
		
		if (!isValid) {
			JOptionPane.showMessageDialog(null,
					errorMessage, "Invalid input",
					JOptionPane.ERROR_MESSAGE);
		}
		
		return isValid;
	}
}

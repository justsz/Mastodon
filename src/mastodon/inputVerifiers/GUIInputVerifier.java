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
	public static boolean verifyMHAlgorithmInput(String minScore, String maxPruning, String iterations) {
		String errorMessage = "";
		boolean isValid = false;
		
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
			if (value < 0) {
				errorMessage += "Maximum pruning " + maxPruning + " is out of bounds. Expected 1+.\n";
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
			if (value < 0) {
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
}

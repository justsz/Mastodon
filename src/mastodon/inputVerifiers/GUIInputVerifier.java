/**
 * 
 */
package mastodon.inputVerifiers;

import java.util.HashMap;
import java.util.Map;

import javax.swing.JOptionPane;

/**
 * @author justs
 *
 */
public class GUIInputVerifier {
	static String errorMessage;
	
	public static Map<String, Object> verifyInput(Map<String, String> input, int selection, int taxaCount) {
		int algSelection = selection % 10;
		int searchSelection =  (int) (selection / 10);
		
		errorMessage = "";
		Map<String, Object> output = new HashMap<String, Object>();
		
		int k;
		int kMin;
		int kMax;
		
		if (searchSelection == 1) {
			k = checkInt("Number to prune", input.get("constPruning"), 1, taxaCount);
			kMin = k;
			kMax = k;
		} else {
			k = 0;
			kMin = checkInt("Min number to prune", input.get("minPruning"), 1, taxaCount);
			if (kMin != -1) {
				kMax = checkInt("Max number to prune", input.get("maxPruning"), kMin, taxaCount);
			} else {
				kMax = -1;
			}			
		}
		
		double initTemp;
		double finalTemp;
		
		if (algSelection == 2) {
			initTemp = 0;
			finalTemp = 0;
		} else {
			initTemp = checkDouble("Initial temperature", input.get("initTemp"), 0, Double.MAX_VALUE);
			if (initTemp != - 1) {
				finalTemp = checkDouble("Final temperature", input.get("finalTemp"), 0, initTemp);
			} else {
				finalTemp = -1;
			}
		}
		
		int iterations = checkInt("Total iterations", input.get("totalIterations"), 1, Integer.MAX_VALUE);
		double minMapScore = checkDouble("Desired MAP score", input.get("minMapScore"), 0.0, 1.0);
		
		if (k == -1 || kMin == -1 || kMax == -1 || initTemp == -1 || finalTemp == -1 || iterations == -1 || minMapScore == -1) {
			showError();
			return null;
		} else {
			output.put("minPruning", kMin);
			output.put("maxPruning", kMax);
			output.put("initTemp", initTemp);
			output.put("finalTemp", finalTemp);
			output.put("totalIterations", iterations);
			output.put("minMapScore", minMapScore);
			return output;
		}
	}
	
	private static double checkDouble(String name, String doubleString, double min, double max) {
		float value;
		try {
			value = Float.parseFloat(doubleString);
			if (value > max || value < min) {
				errorMessage += name + " " + value + " is out of bounds. Expected " + min + " - " + max + ".\n";
				return -1;
			} else {
				return value;
			}
		} catch (NumberFormatException e) {
			errorMessage += name + " " + doubleString + " is not a number.\n";
			return -1;
		}
	}
	
	private static int checkInt(String name, String intString, int min, int max) {
		int value;
		try {
			value = Integer.parseInt(intString);
			if (value > max || value < min) {
				errorMessage += name + " " + value + " is out of bounds. Expected " + min + " - " + max + ".\n";
				return -1;
			} else {
				return value;
			}
		} catch (NumberFormatException e) {
			errorMessage += name + " " + intString + " is not a number or not an integer.\n";
			return -1;
		}
	}
	
	private static void showError() {
		JOptionPane.showMessageDialog(null,
				errorMessage, "Invalid input",
				JOptionPane.ERROR_MESSAGE);
	}
	
	public static boolean verifyMHAlgorithmInput(String[] input, int taxaCount) {
		
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

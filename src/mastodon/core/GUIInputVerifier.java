/* Copyright (C) 2012 Justs Zarins
 *
 *This file is part of MASTodon.
 *
 *MASTodon is free software: you can redistribute it and/or modify
 *it under the terms of the GNU Lesser General Public License as
 *published by the Free Software Foundation, either version 3
 *of the License, or (at your option) any later version.
 *
 *MASTodon is distributed in the hope that it will be useful,
 *but WITHOUT ANY WARRANTY; without even the implied warranty of
 *MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *GNU Lesser General Public License for more details.
 *
 *You should have received a copy of the GNU Lesser General Public License
 *along with this program.  If not, see http://www.gnu.org/licenses/.
 */

package mastodon.core;

import java.util.HashMap;
import java.util.Map;

import javax.swing.JOptionPane;

/**
 * An input verifier for algorithm selection user input. Fair bit of hard-coded stuff here due to what input algorithms expect.
 * @author justs
 *
 */
public class GUIInputVerifier {
	static String errorMessage;
	
	/**
	 * Verifies correctness of the user's input and returns a map suitable for passing directly into an algorithm.
	 * @param input - input Strings
	 * @param selection - selected algorithm
	 * @param taxaCount - total number of taxa in data set
	 * @return a Map suitable to pass directly to an algorithm for setup. Null if thre was an error with input 
	 */
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
		double power;
		
		if (algSelection == 2) {
			initTemp = 0;
			finalTemp = 0;
			power = checkDouble("Weighing power", input.get("power"), Double.MIN_VALUE, Double.MAX_VALUE);
		} else {
			power = 0;
			initTemp = checkDouble("Initial temperature", input.get("initTemp"), Double.MIN_VALUE, Double.MAX_VALUE);
			if (initTemp != - 1) {
				finalTemp = checkDouble("Final temperature", input.get("finalTemp"), Double.MIN_VALUE, initTemp);
			} else {
				finalTemp = -1;
			}
		}
		
		int iterations = checkInt("Total iterations", input.get("totalIterations"), 1, Integer.MAX_VALUE);
		double minMapScore = checkDouble("Desired MAP score", input.get("minMapScore"), 0.0, 1.0);
		
		if (k == -1 || kMin == -1 || kMax == -1 || power == -1 || initTemp == -1 || finalTemp == -1 || iterations == -1 || minMapScore == -1) {
			showError();
			return null;
		} else {
			output.put("minPruning", kMin);
			output.put("maxPruning", kMax);
			output.put("power", power);
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
}
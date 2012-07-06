/**
 * 
 */
package mastodon.inputVerifiers;

import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JTextField;

/**
 * @author justs
 *
 */
public class FractionVerifier extends InputVerifier{

	public boolean verify(JComponent comp) {
		JTextField textField = (JTextField) comp;
		try {
			float value = Float.parseFloat(textField.getText());
			if (value > 1.0 || value < 0) {
				return false;
			} else {
				return true;
			}
		} catch (NumberFormatException e) {
			return false;
		}
	}

}

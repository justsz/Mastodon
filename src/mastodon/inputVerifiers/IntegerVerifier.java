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
public class IntegerVerifier extends InputVerifier{

	public boolean verify(JComponent comp) {
		JTextField textField = (JTextField) comp;
		try {
			int value = Integer.parseInt(textField.getText());
			if (value < 0) {
				return false;
			} else {
				return true;
			}
		} catch (NumberFormatException e) {
			return false;
		}
	}

}

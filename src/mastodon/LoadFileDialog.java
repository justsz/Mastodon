/**
 * 
 */
package mastodon;

import javax.swing.*;

import com.lowagie.text.Font;

public class LoadFileDialog {
	JFrame frame;
	JOptionPane pane;
	private final JDialog dialog;
	JTextField burnin = new JTextField(5);
	JTextField outgroup = new JTextField(10);

	public LoadFileDialog(JFrame frame) {
		this.frame = frame;

		JPanel burninPanel = new JPanel();
		burninPanel.add(new JLabel("Discard the first"));
		burnin.setText("0");

		burninPanel.add(burnin);
		burninPanel.add(new JLabel("trees as burn-in."));

		JPanel reRootPanel = new JPanel();
		reRootPanel.add(new JLabel("Root trees on outgroup: "));

		reRootPanel.add(outgroup);

		JPanel options = new JPanel();
		options.setLayout(new BoxLayout(options, BoxLayout.Y_AXIS));
		options.add(burninPanel);
		options.add(reRootPanel);
		JLabel note = new JLabel("leave blank if trees are rooted");
		java.awt.Font font = new java.awt.Font("Courier", Font.ITALIC,10);
		note.setFont(font);
		
		options.add(note);

		pane = new JOptionPane(options,
				JOptionPane.PLAIN_MESSAGE,
				JOptionPane.OK_CANCEL_OPTION,
				null,
				null,
				null);

		dialog = pane.createDialog(frame, "Set file loading options");
		dialog.pack();
	}

	public int showDialog() {
		dialog.setVisible(true);
		int result = JOptionPane.CANCEL_OPTION;
		Integer value = (Integer)pane.getValue();
		if (value != null && value.intValue() != -1) {
			result = value.intValue();
		}

		if (result == JOptionPane.OK_OPTION) {
		}

		return result;
	}

	public String getBurning() {
		return burnin.getText();
	}

	public String getOutgroup() {
		return outgroup.getText();
	}
}

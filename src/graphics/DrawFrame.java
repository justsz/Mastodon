/**
 * 
 */
package graphics;

import java.util.BitSet;
import java.util.Map;

import javax.swing.JFrame;

/**
 * @author justs
 *
 */
public class DrawFrame extends JFrame {
	public DrawFrame(Map<BitSet, Integer> data) {
		DrawPanel panel = new DrawPanel(data);
		getContentPane().add(panel);
		
		setSize(1000, 1000);
	}
}

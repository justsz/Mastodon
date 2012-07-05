/**
 * 
 */
package mastodon;

import jam.framework.*;
import jam.mac.Utils;

import javax.swing.*;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

/**
 * @author justs
 *
 */
public class PruningMenuFactory implements MenuFactory{


	public String getMenuName() {
		return "Pruning";
	}

	public int getPreferredAlignment() {
		return LEFT;
	}


	public void populateMenu(JMenu menu, AbstractFrame frame) {
		JMenuItem item;
		
		//shouldn't be a part of the file menu handeler... create own later
		if (frame instanceof MastodonFileMenuHandler) {
			item = new JMenuItem(((MastodonFileMenuHandler)frame).getPruningOptionAction());
        	//item.addActionListener(pruningAction);
        	//item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_CLOSE_BRACKET, MenuBarFactory.MENU_MASK));
        	menu.add(item);
		} else if (Utils.isMacOSX()) {
			item = new JMenuItem("Pruning");
	        menu.add(item);
		}
		
	}
	
	
}

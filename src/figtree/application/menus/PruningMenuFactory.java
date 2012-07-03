/**
 * 
 */
package figtree.application.menus;

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

	/* (non-Javadoc)
	 * @see jam.framework.MenuFactory#getMenuName()
	 */
	@Override
	public String getMenuName() {
		return "Pruning";
	}

	/* (non-Javadoc)
	 * @see jam.framework.MenuFactory#getPreferredAlignment()
	 */
	@Override
	public int getPreferredAlignment() {
		return LEFT;
	}

	/* (non-Javadoc)
	 * @see jam.framework.MenuFactory#populateMenu(javax.swing.JMenu, jam.framework.AbstractFrame)
	 */
	@Override
	public void populateMenu(JMenu menu, AbstractFrame frame) {
		JMenuItem item;
		
		//shouldn't be a part of the file menu handeler... create own later
		if (frame instanceof FigTreeFileMenuHandler) {
			item = new JMenuItem(((FigTreeFileMenuHandler)frame).getPruningOptionAction());
        	//item.addActionListener(pruningAction);
        	//item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_CLOSE_BRACKET, MenuBarFactory.MENU_MASK));
        	menu.add(item);
		} else if (Utils.isMacOSX()) {
			item = new JMenuItem("Pruning");
			item.setEnabled(false);
	        menu.add(item);
		}
		
	}
	
	
}

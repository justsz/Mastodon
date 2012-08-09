package mastodon;

import javax.swing.JMenu;

import jam.framework.MenuFactory;
import jam.framework.AbstractFrame;
import jam.framework.Application;
import jam.framework.MenuBarFactory;

import javax.swing.*;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

/**
 * @author justs
 *
 */
public class MastodonDefaultPruneMenuFactory implements MenuFactory{

	public String getMenuName() {
		return "Prune";
	}

	
	public int getPreferredAlignment() {
		return LEFT;
	}
	

	public void populateMenu(JMenu menu, AbstractFrame frame) {
		JMenuItem item;
		
		menu.setMnemonic('P');
		
		if (frame instanceof MastodonPruneMenuHandler) {
			item = new JMenuItem(((MastodonPruneMenuHandler)frame).getAlgorithmAction());
			//item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, MenuBarFactory.MENU_MASK));	//disabled because it wasn't working as soon as the program started. Have to click one of the coloring options first
			menu.add(item);
			
			item = new JMenuItem(((MastodonPruneMenuHandler)frame).getRemoveRunAction());
			//item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, MenuBarFactory.MENU_MASK));
			menu.add(item);
			
			menu.addSeparator();
			
			item = new JMenuItem(((MastodonPruneMenuHandler)frame).getManualPruneAction());
			item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, MenuBarFactory.MENU_MASK));
			menu.add(item);
			
			item = new JMenuItem(((MastodonPruneMenuHandler)frame).getUndoAction());
			item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, MenuBarFactory.MENU_MASK));
			menu.add(item);
			
			item = new JMenuItem(((MastodonPruneMenuHandler)frame).getRedoAction());
			item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, MenuBarFactory.MENU_MASK + ActionEvent.SHIFT_MASK));
			menu.add(item);
			
			menu.addSeparator();
			
			item = new JMenuItem(((MastodonPruneMenuHandler)frame).getCommitAction());
			item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, MenuBarFactory.MENU_MASK + ActionEvent.SHIFT_MASK));
			menu.add(item);		
			
		} else {
			//don't show these if the frame isn't a MastodonPruneMenuHandler
		}
		
	}

}

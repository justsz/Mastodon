/* Copyright (C) 2012 Justs Zarins & Andrew Rambaut
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

package mastodon;

import jam.framework.MenuFactory;
import jam.framework.AbstractFrame;
import jam.framework.Application;
import jam.framework.MenuBarFactory;

import javax.swing.*;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

/**
 * @author Justs Zarins
 * @author Andrew Rambaut
 * @version $Id$
 */
public class MastodonDefaultFileMenuFactory implements MenuFactory {


	public MastodonDefaultFileMenuFactory() {
	}

	public String getMenuName() {
		return "File";
	}

	public void populateMenu(JMenu menu, AbstractFrame frame) {

		JMenuItem item;

		Application application = Application.getApplication();
		menu.setMnemonic('F');

		item = new JMenuItem(application.getNewAction());
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, MenuBarFactory.MENU_MASK));
		menu.add(item);
		
		item = new JMenuItem(frame.getImportAction());
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, MenuBarFactory.MENU_MASK));
        menu.add(item);

		item = new JMenuItem(application.getOpenAction());
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, MenuBarFactory.MENU_MASK));
		menu.add(item);
		
		item = new JMenuItem(frame.getSaveAction());
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, MenuBarFactory.MENU_MASK));
		menu.add(item);
		
		item = new JMenuItem(frame.getSaveAsAction());
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, MenuBarFactory.MENU_MASK + ActionEvent.SHIFT_MASK));
		menu.add(item);
		
		menu.addSeparator();

		// On Windows and Linux platforms, each window has its own menu so items which are not needed
		// are simply missing. In contrast, on Mac, the menu is for the application so items should
		// be enabled/disabled as frames come to the front.
		if (frame instanceof MastodonFileMenuHandler) {
			item = new JMenuItem(((MastodonFileMenuHandler)frame).getExportGraphicAction());
			item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, MenuBarFactory.MENU_MASK));
			menu.add(item);
		} else {
			// If the frame is not a TracerFileMenuHandler then leave out the import/export options.
		}

		item = new JMenuItem(frame.getPrintAction());
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, MenuBarFactory.MENU_MASK));
		menu.add(item);

		item = new JMenuItem(application.getPageSetupAction());
		menu.add(item);
		
		menu.addSeparator();

		item = new JMenuItem(application.getExitAction());
		menu.add(item);
	}

	public int getPreferredAlignment() {
		return LEFT;
	}
}
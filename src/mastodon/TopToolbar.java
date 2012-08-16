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

package mastodon;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import jam.toolbar.GenericToolbarItem;
import jam.toolbar.Toolbar;
import jam.toolbar.ToolbarAction;
import jam.toolbar.ToolbarButton;
import jam.util.IconUtils;

import javax.swing.*;

import figtree.treeviewer.TreeViewerListener;

/**
 * This is the toolbar found above the tree viewing panel. 
 * @author justs
 *
 */
public class TopToolbar{
	Toolbar toolBar;
	SimpleTreeViewer treeViewer;
	JTable resultTable;
	MastodonFrame frame;

	Icon nextIcon = IconUtils.getIcon(this.getClass(), "images/next.png");
	Icon prevIcon = IconUtils.getIcon(this.getClass(), "images/prev.png");
	Icon noColorIcon = IconUtils.getIcon(this.getClass(), "images/black.gif");
	Icon prunedIcon = IconUtils.getIcon(this.getClass(), "images/red.gif");
	Icon freqIcon = IconUtils.getIcon(this.getClass(), "images/gradient.gif");
	
	
	//---button actions---
	final ToolbarAction noColorAction =
			new ToolbarAction(null, "No color", noColorIcon) {
		public void actionPerformed(ActionEvent e){
			((FigTreePanel) treeViewer.getParent()).setColourBy(null);
			noColor.setSelected(true);
			pruned.setSelected(false);
			frequencies.setSelected(false);
		}
	};
	
	final ToolbarAction prunedAction =
			new ToolbarAction(null, "Color pruned branches", prunedIcon) {
		public void actionPerformed(ActionEvent e){
			((FigTreePanel) treeViewer.getParent()).setColourBy("pruned");
			noColor.setSelected(false);
			pruned.setSelected(true);
			frequencies.setSelected(false);
		}
	};
	
	final ToolbarAction pruningFreqAction =
			new ToolbarAction(null, "Color by pruning frequencies [Black -> white corresponds to less -> more frequently pruned", freqIcon) {
		public void actionPerformed(ActionEvent e){
			((FigTreePanel) treeViewer.getParent()).setColourBy("pruningFreq");
			noColor.setSelected(false);
			pruned.setSelected(false);
			frequencies.setSelected(true);
		}
	};
	

	final ToolbarAction nextTreeToolbarAction =
			new ToolbarAction(null, "Next pruning combination", nextIcon) {
		public void actionPerformed(ActionEvent e){
			treeViewer.showNextTree();
			((ResultTableModel)resultTable.getModel()).fireTableDataChanged();
		}
	};

	final ToolbarAction prevTreeToolbarAction =
			new ToolbarAction(null, "Previous pruning combination", prevIcon) {
		public void actionPerformed(ActionEvent e){
			treeViewer.showPreviousTree();
			((ResultTableModel)resultTable.getModel()).fireTableDataChanged();
		}
	};
		

	//listens for changes in the tree display to decide if the next/prev tree buttons should be active
	TreeViewerListener l = new TreeViewerListener() {
		public void treeChanged() {
			boolean nextTreeEnabled = treeViewer.getCurrentTreeIndex() < treeViewer.getTreeCount() - 1;
			nextTreeToolbarAction.setEnabled(nextTreeEnabled);

			boolean prevTreeEnabled = treeViewer.getCurrentTreeIndex() > 0;
			prevTreeToolbarAction.setEnabled(prevTreeEnabled);
		}

		public void treeSettingsChanged() {
			// nothing to do
		}
	};
	
	JButton noColor = new ToolbarButton(noColorAction, true);
	JButton pruned = new ToolbarButton(prunedAction, true);
	JButton frequencies = new ToolbarButton(pruningFreqAction, true);	
	JButton commit;
	
	JButton undo;
	JButton redo;
	
	JButton pruneButton;

	/**
	 * Create the toolbar and set all actions.
	 * @param treeViewer - treeViewer that shows the trees
	 * @param resultTable - table that turns a RunResult into table display
	 * @param frame - parent frame for this component
	 */
	public TopToolbar(SimpleTreeViewer treeViewer, JTable resultTable, MastodonFrame frame) {
		this.treeViewer = treeViewer;
		this.resultTable = resultTable;
		this.frame = frame;
		toolBar = new Toolbar();
		toolBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.darkGray));
		toolBar.setRollover(true);
		toolBar.setFloatable(false);

//		pruneToolbarAction.putValue(AbstractAction.MNEMONIC_KEY, new Integer(KeyEvent.VK_P));
		
		pruneButton = new JButton(frame.getManualPruneAction());
		pruneButton.setToolTipText("Flip the pruning status of currently selected taxa");
		undo = new JButton(frame.getUndoAction());
		undo.setToolTipText("Undo last manual pruning");
		redo = new JButton(frame.getRedoAction());
		redo.setToolTipText("Redo last manual pruning");
		commit = new JButton(frame.getCommitAction());
		commit.setToolTipText("Remove red branches and open as new file");
		


		JButton prevTreeToolButton = new ToolbarButton(prevTreeToolbarAction, true);
		prevTreeToolButton.setFocusable(false);
//		prevTreeToolButton.putClientProperty("JButton.buttonType", "segmentedTextured");
//		prevTreeToolButton.putClientProperty("JButton.segmentPosition", "first");
//		prevTreeToolButton.putClientProperty( "Quaqua.Button.style", "toggleWest");


		JButton nextTreeToolButton = new ToolbarButton(nextTreeToolbarAction, true);
		nextTreeToolButton.setFocusable(false);
//		nextTreeToolButton.putClientProperty("JButton.buttonType", "segmentedTextured");
//		nextTreeToolButton.putClientProperty("JButton.segmentPosition", "last");
//		nextTreeToolButton.putClientProperty( "Quaqua.Button.style", "toggleEast");

		nextTreeToolbarAction.setEnabled(treeViewer.getCurrentTreeIndex() < treeViewer.getTreeCount() - 1);
		prevTreeToolbarAction.setEnabled(treeViewer.getCurrentTreeIndex() > 0);

		Box box = Box.createHorizontalBox();
		//box2.add(Box.createVerticalStrut(annotationToolIcon.getIconHeight()));
		box.add(prevTreeToolButton);
		box.add(nextTreeToolButton);
		toolBar.addComponent(new GenericToolbarItem("Prev/Next", "Navigate through different pruning combinations with the same MAP score", box));
				
		
		noColor.setEnabled(false);
		pruned.setEnabled(false);		
		frequencies.setEnabled(false);
		
		frame.getUndoAction().setEnabled(false);
		frame.getRedoAction().setEnabled(false);		
		
		frame.getCommitAction().setEnabled(false);
		
		Box colorBox = Box.createHorizontalBox();
		colorBox.add(noColor);
		colorBox.add(pruned);
		colorBox.add(frequencies);
		
		toolBar.addComponent(new GenericToolbarItem("Coloring", "Choose how to color the tree", colorBox));
		
		
		frame.getManualPruneAction().setEnabled(false);
		
		Box pruneBox = Box.createHorizontalBox();
		pruneBox.add(pruneButton);
		pruneBox.add(undo);
		pruneBox.add(redo);
		pruneBox.add(commit);
		
		toolBar.addComponent(new GenericToolbarItem("Manual pruning options", "(Disabled while a pruning algorithm is running.)", pruneBox));

		treeViewer.addTreeViewerListener(l);
		l.treeChanged();

		toolBar.addFlexibleSpace();
	}

	public Toolbar getToolbar() {
		return toolBar;
	}

	public void fireTreesChanged() {
		l.treeChanged();
	}
	
	public void enableColorButtons(boolean enable) {
		noColor.setEnabled(enable);
		pruned.setEnabled(enable);
		frequencies.setEnabled(enable);
	}
	
	public void enablePruningButtons(boolean enable) {
		frame.getManualPruneAction().setEnabled(enable);
		frame.getUndoAction().setEnabled(enable);
		frame.getRedoAction().setEnabled(enable);
		frame.getCommitAction().setEnabled(enable);
	}
}

/**
 * 
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
	Icon pruneIcon = IconUtils.getIcon(this.getClass(), "images/scissors.png");
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
			new ToolbarAction(null, "Color by pruning frequencies", freqIcon) {
		public void actionPerformed(ActionEvent e){
			((FigTreePanel) treeViewer.getParent()).setColourBy("pruningFreq");
			noColor.setSelected(false);
			pruned.setSelected(false);
			frequencies.setSelected(true);
		}
	};
	

	final ToolbarAction nextTreeToolbarAction =
			new ToolbarAction(null, "Next Tree...", nextIcon) {
		public void actionPerformed(ActionEvent e){
			treeViewer.showNextTree();
			((ResultTableModel)resultTable.getModel()).fireTableDataChanged();
		}
	};

	final ToolbarAction prevTreeToolbarAction =
			new ToolbarAction(null, "Previous Tree...", prevIcon) {
		public void actionPerformed(ActionEvent e){
			treeViewer.showPreviousTree();
			((ResultTableModel)resultTable.getModel()).fireTableDataChanged();
		}
	};
	
	final ToolbarAction pruneToolbarAction =
			new ToolbarAction("Prune", "Prune/UnPrune selected taxa", pruneIcon) {
		
		public void actionPerformed(ActionEvent e){
			frame.pruneTaxa();
			l.treeChanged();
		}
	};
	
	final ToolbarAction commitPruningAction =
			new ToolbarAction("Commit", "Remove red branches and open as new file", null) {
		public void actionPerformed(ActionEvent e){
			frame.commitPruning();
		}
	};
	
	final ToolbarAction undoPruningAction =
			new ToolbarAction("Undo", "Undo last manual pruning", null) {
		public void actionPerformed(ActionEvent e){
			frame.undo();
		}
	};
	
	final ToolbarAction redoPruningAction =
			new ToolbarAction("Redo", "Redo last manual pruning", null) {
		public void actionPerformed(ActionEvent e){
			frame.redo();
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
	JButton commit = new ToolbarButton(commitPruningAction, true);
	
	JButton undo = new ToolbarButton(undoPruningAction, true);
	JButton redo = new ToolbarButton(redoPruningAction, true);
	
	JButton pruneButton = new ToolbarButton(pruneToolbarAction, true);

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

		pruneToolbarAction.putValue(AbstractAction.MNEMONIC_KEY, new Integer(KeyEvent.VK_P));


		JButton prevTreeToolButton = new ToolbarButton(prevTreeToolbarAction, true);
		prevTreeToolButton.setFocusable(false);
		prevTreeToolButton.putClientProperty("JButton.buttonType", "segmentedTextured");
		prevTreeToolButton.putClientProperty("JButton.segmentPosition", "first");
		prevTreeToolButton.putClientProperty( "Quaqua.Button.style", "toggleWest");


		JButton nextTreeToolButton = new ToolbarButton(nextTreeToolbarAction, true);
		nextTreeToolButton.setFocusable(false);
		nextTreeToolButton.putClientProperty("JButton.buttonType", "segmentedTextured");
		nextTreeToolButton.putClientProperty("JButton.segmentPosition", "last");
		nextTreeToolButton.putClientProperty( "Quaqua.Button.style", "toggleEast");

		nextTreeToolbarAction.setEnabled(treeViewer.getCurrentTreeIndex() < treeViewer.getTreeCount() - 1);
		prevTreeToolbarAction.setEnabled(treeViewer.getCurrentTreeIndex() > 0);

		Box box = Box.createHorizontalBox();
		//box2.add(Box.createVerticalStrut(annotationToolIcon.getIconHeight()));
		box.add(prevTreeToolButton);
		box.add(nextTreeToolButton);
		toolBar.addComponent(new GenericToolbarItem("Prev/Next", "Navigate through the trees", box));
				
		
		noColor.setEnabled(false);
		pruned.setEnabled(false);		
		frequencies.setEnabled(false);
		
		undo.setEnabled(false);
		redo.setEnabled(false);		
		
		commit.setEnabled(false);
		
		Box colorBox = Box.createHorizontalBox();
		colorBox.add(noColor);
		colorBox.add(pruned);
		colorBox.add(frequencies);
		
		toolBar.addComponent(new GenericToolbarItem("Coloring", "Choose how to color the tree", colorBox));
		
		
		pruneButton.setEnabled(false);
		pruneButton.registerKeyboardAction(pruneToolbarAction, KeyStroke.getKeyStroke("p"), JComponent.WHEN_IN_FOCUSED_WINDOW);
		
		Box pruneBox = Box.createHorizontalBox();
		pruneBox.add(pruneButton);
		pruneBox.add(undo);
		pruneBox.add(redo);
		pruneBox.add(commit);
		
		toolBar.addComponent(new GenericToolbarItem("Manual Pruning", "(Disabled while a pruning algorithm is running.)", pruneBox));

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
		pruneButton.setEnabled(enable);
		undo.setEnabled(enable);
		redo.setEnabled(enable);
		commit.setEnabled(enable);
	}
}

/**
 * 
 */
package mastodon;

import java.awt.CardLayout;
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

//import figtree.treeviewer.TreeViewer;

/**
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
	
	

	//    private AbstractAction nextTreeAction =
	//            new AbstractAction("Next Tree") {
	//                public void actionPerformed(ActionEvent e){
	//                    treeViewer.showNextTree();
	//                }
	//            };
	//
	//    private AbstractAction previousTreeAction =
	//            new AbstractAction("Previous Tree") {
	//                public void actionPerformed(ActionEvent e){
	//                    treeViewer.showPreviousTree();
	//                }
	//            };

	TreeViewerListener l = new TreeViewerListener() {
		public void treeChanged() {
			boolean nextTreeEnabled = treeViewer.getCurrentTreeIndex() < treeViewer.getTreeCount() - 1;
			//            nextTreeAction.setEnabled(nextTreeEnabled);
			nextTreeToolbarAction.setEnabled(nextTreeEnabled);

			boolean prevTreeEnabled = treeViewer.getCurrentTreeIndex() > 0;
			//            previousTreeAction.setEnabled(prevTreeEnabled);
			prevTreeToolbarAction.setEnabled(prevTreeEnabled);
		}

		public void treeSettingsChanged() {
			// nothing to do
		}
	};
	
	//JRadioButton noColor = new JRadioButton(noColorAction);
	//JRadioButton pruned = new JRadioButton(prunedAction);
	//JRadioButton frequencies = new JRadioButton(pruningFreqAction);
	JButton noColor = new ToolbarButton(noColorAction, true);
	JButton pruned = new ToolbarButton(prunedAction, true);
	JButton frequencies = new ToolbarButton(pruningFreqAction, true);	
	JButton commit = new ToolbarButton(commitPruningAction, true);
	
	JButton undo = new ToolbarButton(undoPruningAction, true);
	JButton redo = new ToolbarButton(redoPruningAction, true);
	
	JButton pruneButton = new ToolbarButton(pruneToolbarAction, true);

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
				
		//noColor.setSelected(true);
		
		noColor.setEnabled(false);
		pruned.setEnabled(false);		
		frequencies.setEnabled(false);
		
		redo.setEnabled(false);
		undo.setEnabled(false);
				
//		ButtonGroup coloringGroup = new ButtonGroup();		
//		coloringGroup.add(noColor);
//		coloringGroup.add(pruned);
//		coloringGroup.add(frequencies);
		
		Box colorBox = Box.createHorizontalBox();
		colorBox.add(noColor);
		colorBox.add(pruned);
		colorBox.add(frequencies);
		
		toolBar.addComponent(new GenericToolbarItem("Coloring", "Choose how to color the tree", colorBox));
		
//		toolBar.addComponent(noColor);
//		toolBar.addComponent(pruned);
//		toolBar.addComponent(frequencies);
		
		
		pruneButton.setEnabled(false);
		pruneButton.registerKeyboardAction(pruneToolbarAction, KeyStroke.getKeyStroke("p"), JComponent.WHEN_IN_FOCUSED_WINDOW);
		toolBar.addComponent(pruneButton);	
		toolBar.addComponent(commit);
		toolBar.addComponent(undo);
		toolBar.addComponent(redo);
		
		


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
	
	public void enablePruneButton(boolean enable) {
		pruneButton.setEnabled(enable);
	}
}

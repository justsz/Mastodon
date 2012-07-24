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
	
	private AbstractAction noColorAction = new AbstractAction("No color") {
		public void actionPerformed(ActionEvent ae) {
			((FigTreePanel) treeViewer.getParent()).setColourBy(null);
		}
	};
	
	private AbstractAction prunedAction = new AbstractAction("Pruned") {
		public void actionPerformed(ActionEvent ae) {
			((FigTreePanel) treeViewer.getParent()).setColourBy("pruned");
		}
	};
	
	private AbstractAction pruningFreqAction = new AbstractAction("Pruning frequencies") {
		public void actionPerformed(ActionEvent ae) {
			((FigTreePanel) treeViewer.getParent()).setColourBy("pruningFreq");
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
		
		JRadioButton noColor = new JRadioButton(noColorAction);
		JRadioButton pruned = new JRadioButton(prunedAction);
		pruned.setSelected(true);
		JRadioButton frequencies = new JRadioButton(pruningFreqAction);
		
		ButtonGroup coloringGroup = new ButtonGroup();		
		coloringGroup.add(noColor);
		coloringGroup.add(pruned);
		coloringGroup.add(frequencies);
		
//		Box colorBox = Box.createHorizontalBox();
//		colorBox.add(noColor);
//		colorBox.add(pruned);
//		colorBox.add(frequencies);
//		
//		toolBar.addComponent(new GenericToolbarItem("No color/MAP pruning/Pruning frequency", "Choose how to color the tree", colorBox));
		
		toolBar.addComponent(noColor);
		toolBar.addComponent(pruned);
		toolBar.addComponent(frequencies);
		
		JButton pruneButton = new ToolbarButton(pruneToolbarAction, true);
		pruneButton.registerKeyboardAction(pruneToolbarAction, KeyStroke.getKeyStroke("p"), JComponent.WHEN_IN_FOCUSED_WINDOW);
		toolBar.addComponent(pruneButton);				


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
}

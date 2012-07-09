/**
 * 
 */
package mastodon;

import java.awt.Color;
import java.awt.event.ActionEvent;

import jam.toolbar.GenericToolbarItem;
import jam.toolbar.Toolbar;
import jam.toolbar.ToolbarAction;
import jam.toolbar.ToolbarButton;
import jam.util.IconUtils;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTable;

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

	Icon nextIcon = IconUtils.getIcon(this.getClass(), "images/next.png");
	Icon prevIcon = IconUtils.getIcon(this.getClass(), "images/prev.png");

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

	public TopToolbar(SimpleTreeViewer treeViewer, JTable resultTable) {
		this.treeViewer = treeViewer;
		this.resultTable = resultTable;
		toolBar = new Toolbar();
		toolBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.darkGray));
		toolBar.setRollover(true);
		toolBar.setFloatable(false);




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

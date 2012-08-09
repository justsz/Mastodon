package mastodon;

import mastodon.core.Algorithm;
import mastodon.algorithms.*;
import mastodon.core.RunResult;
import mastodon.entryPoints.Launcher;
import mastodon.inputVerifiers.GUIInputVerifier;
import figtree.treeviewer.TreePaneSelector.SelectionMode;
import figtree.treeviewer.TreeSelectionListener;
import figtree.treeviewer.TreeViewerListener;
import jam.framework.Application;
import jam.framework.DocumentFrame;
import jam.table.TableRenderer;

import org.freehep.util.export.ExportDialog;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.BorderUIResource;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.text.DefaultCaret;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jebl.evolution.graphs.Node;
import jebl.evolution.io.ImportException;
import jebl.evolution.trees.RootedTree;


/**
 * This is the Grand Central of the app. The class creates the main frame of the application and communicates with the various components for updates.
 * @author Justs Zarins
 * @author Andrew Rambaut
 * @version $Id$
 */
public class MastodonFrame extends DocumentFrame implements MastodonFileMenuHandler, MastodonPruneMenuHandler {
	private final String[] resultTableColumnToolTips = {"", "Checkmark indicates inclusion in the pruning set that produced the highest MAP score"
			, "", "Number of times the taxon is included in a pruning set that is accepted by the search algorithm as the next step, divided by total accepted steps"};
	private final String[] runTableColumnToolTips = {"Algorithm run", "Number of taxa pruned in the pruning set that produced the highest MAP score", "Pruned taxa lower limit", "Pruned taxa upper limit", "MAP score"};

	private FigTreePanel figTreePanel = null;

	private JTable runTable = null;
	private RunTableModel runTableModel = null;
	private JSplitPane splitPane1 = null;
	private JPanel topPanel = null;

	private JPanel cardPanel;

	private TopToolbar topToolbar;

	private JTable resultTable = null;
	private ResultTableModel resultTableModel = null;

	private JScrollPane scrollPane1;

	private JLabel progressLabel;
	private JProgressBar progressBar;

	private JTextArea statusBox;

	private List<RunResult> runResults;
	private int selectedRun;
	private JLabel score;

	private AlgorithmWorker algorithmWorker = new AlgorithmWorker(this);

	private JButton cancelButton = new JButton(new AbstractAction("Cancel") {
		public void actionPerformed(ActionEvent arg0) {
			algorithmWorker.cancel(true);
		}
	});

	String message = "";

	private boolean quiet = false;	//prevents table selection and tree selection from going into an infinite loop

	Launcher launcher;	//each MastadonFrame has an associated Launcher that holds a BitTreeSystem of the loaded data set
	javax.swing.Timer timer;

	AlgorithmDialog algorithmDialog;

	public MastodonFrame(String title) {
		super();

		setTitle(title);

		getOpenAction().setEnabled(false);
		getSaveAction().setEnabled(false);
		getSaveAsAction().setEnabled(false);

		getCutAction().setEnabled(false);
		getCopyAction().setEnabled(false);
		getPasteAction().setEnabled(false);
		getDeleteAction().setEnabled(false);
		getSelectAllAction().setEnabled(false);
		getFindAction().setEnabled(false);

		getZoomWindowAction().setEnabled(false);

		runResults = new ArrayList<RunResult>();
	}

	/**
	 * Updates the score JLabel.
	 */
	TreeViewerListener scoreListener = new TreeViewerListener() {
		public void treeChanged() {
			double[] scores;
			try {	//likely a temporary solution
				scores = runResults.get(selectedRun).getPruningScores().get(figTreePanel.getTreeViewer().getCurrentTreeIndex());
			} catch (IndexOutOfBoundsException e) {
				scores = new double[2];
				scores[0] = 0;
				scores[1] = 0;
			}
			score.setText("Map score: " + scores[0] + " Found in " + (int)scores[1] + " trees.");
		}

		public void treeSettingsChanged() {
		}
	};

	/**
	 * Listens for tree tip selection and highlights the corresponding rows in the results table.
	 */
	TreeSelectionListener selectionListener = new TreeSelectionListener() {
		public void selectionChanged() {
			if (!quiet) {
				quiet = true;
				Set<Node> nodes = figTreePanel.getTreeViewer().getSelectedTips();
				RootedTree tree = launcher.getMapTree();

				if(nodes.size() < 1) {
					resultTable.clearSelection();
				} else {
					boolean firstInterval = true;
					for(Node node : nodes) {
						String taxonName = tree.getTaxon(node).getName();
						int k = resultTable.convertRowIndexToView(searchTable(taxonName));

						if(firstInterval) {
							resultTable.getSelectionModel().setSelectionInterval(k, k);
							firstInterval = false;
						} else {
							resultTable.getSelectionModel().addSelectionInterval(k, k);
						}				
					}
				}
				quiet = false;
			}
		}

	};

	private int searchTable(String taxonName) {
		for(int i = 0; i < resultTableModel.getRowCount(); i++) {
			if (resultTableModel.getValueAt(i, 2) == taxonName) {
				return i;
			}
		}
		return -1;
	}



	public void initializeComponents() {
		setSize(new java.awt.Dimension(1200, 800));

		figTreePanel = new FigTreePanel(FigTreePanel.Style.DEFAULT);
		figTreePanel.setBorder(new BorderUIResource.EmptyBorderUIResource(new java.awt.Insets(12, 6, 12, 12)));
		figTreePanel.getTreeViewer().addTreeViewerListener(scoreListener);
		figTreePanel.getTreeViewer().setSelectionMode(SelectionMode.TAXA);
		figTreePanel.getTreeViewer().addTreeSelectionListener(selectionListener);

		runTableModel = new RunTableModel();
		runTable = new JTable(runTableModel) { 
			//Implement table header tool tips.
			protected JTableHeader createDefaultTableHeader() {
				return new JTableHeader(columnModel) {
					public String getToolTipText(MouseEvent e) {
						java.awt.Point p = e.getPoint();
						int index = columnModel.getColumnIndexAtX(p.x);
						int realIndex = columnModel.getColumn(index).getModelIndex();
						return runTableColumnToolTips[realIndex];
					}
				};
			}
		};

		runTable.setAutoCreateRowSorter(true);
		TableRenderer renderer = new TableRenderer(SwingConstants.LEFT, new Insets(0, 4, 0, 4));
		runTable.getColumnModel().getColumn(0).setCellRenderer(renderer);
		runTable.getColumnModel().getColumn(1).setCellRenderer(renderer);
		runTable.getColumnModel().getColumn(2).setCellRenderer(renderer);
		runTable.getColumnModel().getColumn(3).setCellRenderer(renderer);
		runTable.getColumnModel().getColumn(4).setCellRenderer(renderer);
		runTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		runTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent evt) {
				runTableSelectionChanged();
			}
		});

		scrollPane1 = new JScrollPane(runTable, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		//the little plus/minus sign under top left table
		//		ActionPanel actionPanel1 = new ActionPanel(false);	
		//		actionPanel1.setAddAction(getAlgorithmAction());
		//		actionPanel1.setRemoveAction(getRemoveRunAction());
		//		getAlgorithmAction().setEnabled(false);
		//		getRemoveRunAction().setEnabled(false);

		JPanel actionPanel1 = new JPanel();
		JButton run = new JButton(getAlgorithmAction());
		run.setToolTipText("Run a pruning algorithm (disabled if an algorithm is already running)");
		JButton removeRun = new JButton(getRemoveRunAction());
		removeRun.setToolTipText("Remove selected run from list");
		actionPanel1.add(run);
		actionPanel1.add(removeRun);

		getAlgorithmAction().setEnabled(false);
		getRemoveRunAction().setEnabled(false);
		cancelButton.setEnabled(false);

		topPanel = new JPanel(new BorderLayout(0, 0));
		topPanel.setBorder(new BorderUIResource.EmptyBorderUIResource(new java.awt.Insets(0, 0, 6, 0)));
		topPanel.add(new JLabel("Pruning Runs:"), BorderLayout.NORTH);
		topPanel.add(scrollPane1, BorderLayout.CENTER);
		topPanel.add(actionPanel1, BorderLayout.SOUTH);

		ColorRenderer colorRenderer = new ColorRenderer(SwingConstants.LEFT, new Insets(0, 4, 0, 4));


		resultTableModel = new ResultTableModel(null, figTreePanel);
		resultTable = new JTable(resultTableModel) {
			//Implement table header tool tips.
			protected JTableHeader createDefaultTableHeader() {
				return new JTableHeader(columnModel) {
					public String getToolTipText(MouseEvent e) {
						java.awt.Point p = e.getPoint();
						int index = columnModel.getColumnIndexAtX(p.x);
						int realIndex = columnModel.getColumn(index).getModelIndex();
						return resultTableColumnToolTips[realIndex];
					}
				};
			}
		};


		resultTable.setAutoCreateRowSorter(true);
		resultTable.getColumnModel().getColumn(0).setPreferredWidth(10);
		resultTable.getColumnModel().getColumn(1).setPreferredWidth(5);

		resultTable.getColumnModel().getColumn(0).setCellRenderer(colorRenderer);
		resultTable.getColumnModel().getColumn(1).setCellRenderer(colorRenderer);
		resultTable.getColumnModel().getColumn(2).setCellRenderer(colorRenderer);
		resultTable.getColumnModel().getColumn(3).setCellRenderer(colorRenderer);
		resultTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);


		resultTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent evt) {
				resultTableSelectionChanged();
			}
		});


		JScrollPane scrollPane2 = new JScrollPane(resultTable, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		JPanel bottomPanel = new JPanel(new BorderLayout(0, 0));
		bottomPanel.add(new JLabel("Pruning run details:"), BorderLayout.NORTH);
		bottomPanel.add(scrollPane2, BorderLayout.CENTER);

		JPanel leftPanel = new JPanel(new BorderLayout(0, 0));
		leftPanel.setPreferredSize(new Dimension(400, 300));
		splitPane1 = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, topPanel, bottomPanel);
		splitPane1.setBorder(null);
		splitPane1.setDividerLocation(180);

		JPanel progressPanel = new JPanel(new BorderLayout(0, 0));
		statusBox = new JTextArea(4, 20);
		statusBox.setToolTipText("Current status of algorithm. K is the number of taxa currently being pruned.");
		statusBox.setEditable(false);

		DefaultCaret caret = (DefaultCaret)statusBox.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

		JScrollPane sp = new JScrollPane(statusBox);

		progressLabel = new JLabel("");
		progressBar = new JProgressBar();
		progressBar.setString("");
		progressPanel.add(progressLabel, BorderLayout.NORTH);
		progressPanel.add(progressBar, BorderLayout.CENTER);
		progressPanel.add(cancelButton, BorderLayout.EAST);
		progressPanel.add(sp, BorderLayout.SOUTH);
		progressPanel.setBorder(new BorderUIResource.EmptyBorderUIResource(new java.awt.Insets(6, 0, 0, 0)));

		JPanel scorePanel = new JPanel(new BorderLayout(0, 0));
		score = new JLabel("");
		scorePanel.add(score);
		scorePanel.setBorder(new BorderUIResource.EmptyBorderUIResource(new java.awt.Insets(6, 0, 0, 0)));

		cardPanel = new JPanel(new CardLayout());
		cardPanel.add(progressPanel, "progress");
		cardPanel.add(scorePanel, "score");		
		cardPanel.setBorder(new BorderUIResource.EmptyBorderUIResource(new java.awt.Insets(6, 0, 0, 0)));

		leftPanel.add(splitPane1, BorderLayout.CENTER);
		leftPanel.add(cardPanel, BorderLayout.SOUTH);
		leftPanel.setBorder(new BorderUIResource.EmptyBorderUIResource(new java.awt.Insets(12, 12, 12, 6)));

		topToolbar = new TopToolbar((SimpleTreeViewer) figTreePanel.getTreeViewer(), resultTable, this);
		JPanel rightPanel = new JPanel(new BorderLayout(0,0));
		rightPanel.add(topToolbar.getToolbar(), BorderLayout.NORTH);
		rightPanel.add(figTreePanel, BorderLayout.CENTER);
		rightPanel.setBorder(new BorderUIResource.EmptyBorderUIResource(new java.awt.Insets(12, 12, 12, 6)));


		JSplitPane splitPane2 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, leftPanel, rightPanel);
		splitPane2.setBorder(null);
		splitPane2.setDividerLocation(350);

		Color focusColor = UIManager.getColor("Focus.color");
		Border focusBorder = BorderFactory.createMatteBorder(2, 2, 2, 2, focusColor);
		splitPane1.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		new FileDrop(null, splitPane1, focusBorder, new FileDrop.Listener() {
			public void filesDropped(java.io.File[] files) {
				for(File file : files) {
					Application.getApplication().doOpenFile(file);
				}
			}   // end filesDropped
		}); // end FileDrop.Listener


		getContentPane().setLayout(new java.awt.BorderLayout(0, 0));
		getContentPane().add(splitPane2, BorderLayout.CENTER);

	}


	/**
	 * Put text in the text area under the progress bar.
	 * @param text - String to append
	 */
	private void updateTextArea(final String text) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				statusBox.append(text);
			}
		});
	}


	public void setVisible(boolean b) {
		super.setVisible(b);
	}


	private void removeRun() {
		int selRow = runTable.getSelectedRow();

		if (selRow < 0) {
			return;
		}

		runResults.remove(selRow);
		int prevRow = selRow - 1;
		if (prevRow < 0) {
			prevRow = 0;
		}
		selectedRun = prevRow;
		runTableModel.fireTableDataChanged();
		runTable.getSelectionModel().setSelectionInterval(selectedRun, selectedRun);
	}



	public void runTableSelectionChanged() {
		int selRow = runTable.getSelectedRow();

		if (selRow < 0) {
			getRemoveRunAction().setEnabled(false);
			return;
		}


		if(runResults.size() > 0) {
			topToolbar.enablePruningButtons(true);
			topToolbar.enableColorButtons(true);
			getRemoveRunAction().setEnabled(true);
			getSaveAction().setEnabled(true);
			getSaveAsAction().setEnabled(true);
		} else {
			topToolbar.enablePruningButtons(false);
			topToolbar.enableColorButtons(false);
			getRemoveRunAction().setEnabled(false);
			getSaveAction().setEnabled(false);
			getSaveAsAction().setEnabled(false);
		}
		selectedRun = selRow;
		updateDataDisplay();
	}


	public void updateDataDisplay() {
		RunResult runResult;
		if(runResults.size() > 0) {
			runResult = runResults.get(selectedRun);
		} else {
			runResult = null;
		}
		resultTableModel.setRunResult(runResult);
		if (runResult == null) {
			score.setText("");
			((SimpleTreeViewer)figTreePanel.getTreeViewer()).setTree(null);
			getUndoAction().setEnabled(false);
			getRedoAction().setEnabled(false);
		} else {
			figTreePanel.getTreeViewer().setTrees(runResult.getPrunedMapTrees());	
			getUndoAction().setEnabled(runResult.hasPrev());
			getRedoAction().setEnabled(runResult.hasNext());
		}
		topToolbar.fireTreesChanged(); 
		resultTableModel.fireTableDataChanged();
	}


	/**
	 * Listens for changes in selection in the result table and highlights corresponding tips in the tree view.
	 */
	public void resultTableSelectionChanged() {
		if (!quiet) {
			quiet = true;
			int[] selRows = resultTable.getSelectedRows();
			if (selRows.length > 0) {
				List<String> taxonNames = new ArrayList<String>();
				for(int i = 0; i < selRows.length; i++) {
					taxonNames.add((String) resultTableModel.getValueAt(resultTable.convertRowIndexToModel(selRows[i]), 2));
				}
				figTreePanel.getTreeViewer().selectTaxa(taxonNames);
			} else {
				figTreePanel.getTreeViewer().clearSelectedTaxa();
			}
			quiet = false;
		}
	}

	/**
	 * Brings up an algorithm dialog, collects the user's choices, verifies validity and dispatches the appropriate algorithm.
	 * @throws IOException
	 * @throws ImportException
	 */
	public final void doAlgorithm() throws IOException, ImportException {
		if (algorithmDialog == null) {
			algorithmDialog = new AlgorithmDialog(this);
		}

		if (algorithmDialog.showDialog() == JOptionPane.OK_OPTION) {
			final int selection = algorithmDialog.getSelection();
			Map<String, Object> input = GUIInputVerifier.verifyInput(algorithmDialog.getInput(), selection, launcher.getTaxaCount());

			if(input != null) {
				((CardLayout)cardPanel.getLayout()).show(cardPanel, "progress");
				progressBar.setValue(0);
				progressBar.setMaximum((Integer) input.get("totalIterations"));
				progressBar.setStringPainted(false);


				Algorithm algorithm;

				if((int) (selection / 10) == 3) { //bisection
					if ((int) (selection % 10) == 1) {	//SA
						algorithm = new SABisectionAlgorithm();
					} else { //MH
						algorithm = new MHBisectionAlgorithm();
					}
				} else if((int) (selection / 10) == 4) {	//flip-penalty
					algorithm = new FlipPenaltyAlgorithm();
				} else { //constant or linear
					if ((int) (selection % 10) == 1) {	//SA
						algorithm = new SALinearAlgorithm();
					} else { //MH
						algorithm = new MHLinearAlgorithm();
					}
				}

				launcher.setupAlgorithm(algorithm, input);

				//the algorithm needs to be run in background so that the GUI doesn't freeze up
				algorithmWorker = new AlgorithmWorker(this);
				algorithmWorker.execute();

				statusBox.setText("");

				//a timer created that queries the launcher for progress
				timer = new javax.swing.Timer(1000, new ActionListener() {
					public void actionPerformed(ActionEvent evt) {
						progressBar.setValue(launcher.getCurrentIterations());
						String status = launcher.getStatus();
						updateTextArea(status + "\n");
					}
				});

				timer.start();
				//having multiple algorithms running from the same window feels like asking for trouble
				getAlgorithmAction().setEnabled(false);
				cancelButton.setEnabled(true);
			}//the input verifier will display the input validation error if required
		}
	}


	class AlgorithmWorker extends SwingWorker<Void, Void> {
		JFrame frame;
		public AlgorithmWorker(JFrame frame) {
			this.frame = frame;
		}

		protected Void doInBackground() throws Exception {
			topToolbar.enablePruningButtons(false);
			launcher.runAlgorithm();
			return null;
		}

		protected void done() {
			timer.stop();
			if (isCancelled()) {
				launcher.stopAlgorithm();

				int n = JOptionPane.showOptionDialog(frame, "Algorithm run cancelled.\n" +
						"Discard run completely?\n" +
						"Pressing No will display best found pruning.", "Run cancel action", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, JOptionPane.NO_OPTION);
				if (n == JOptionPane.NO_OPTION) {
					runResults.add(launcher.getResults());
					selectedRun = runResults.size() - 1;
				}
			} else {
				runResults.add(launcher.getResults());
				selectedRun = runResults.size() - 1;			
			}
			runTableModel.fireTableDataChanged();
			//highlight current run in runTable and update display
			runTable.getSelectionModel().setSelectionInterval(selectedRun, selectedRun);

			//switch from progress bar to score panel
			((CardLayout)cardPanel.getLayout()).show(cardPanel, "score");
			getAlgorithmAction().setEnabled(true);
			cancelButton.setEnabled(false);
		}
	}


	/**
	 * Flips the pruning status of selected taxa and updates scores.
	 * Depends on all taxa being in the table and in the correct order for a simple implementation.
	 */
	public void pruneTaxa() {
		RunResult runResult = runResults.get(selectedRun);
		int currentTree = figTreePanel.getTreeViewer().getCurrentTreeIndex();
		BitSet pruning = runResult.getPrunedTaxaBits().get(currentTree);

		int[] selRows = resultTable.getSelectedRows();
		if (selRows.length > 0) {
			BitSet toFlip = new BitSet();
			for(int i = 0; i < selRows.length; i++) {
				//don't prune if it is in the pruned set after a "commit"
				if (!((ResultTableModel) resultTable.getModel()).isPruned(resultTable.convertRowIndexToView(selRows[i]))) {
					toFlip.set(resultTable.convertRowIndexToModel(selRows[i]));
				}
			}
			pruning.xor(toFlip);
			runResult.addChange(toFlip);
			runResult.updateRun(currentTree);
			runTableModel.fireTableDataChanged();
			runTable.getSelectionModel().setSelectionInterval(selectedRun, selectedRun);
		} else {
			//do nothing
		}
	}

	/**
	 * Undo previous manual pruning.
	 */
	public void undo() {
		if(runResults.size() > 0) {
			RunResult runResult = runResults.get(selectedRun);
			int currentTree = figTreePanel.getTreeViewer().getCurrentTreeIndex();
			BitSet pruning = runResult.getPrunedTaxaBits().get(currentTree);

			pruning.xor(runResult.getPrevChange());

			runResult.updateRun(currentTree);
			runTableModel.fireTableDataChanged();
			runTable.getSelectionModel().setSelectionInterval(selectedRun, selectedRun);
		}
	}


	/**
	 * Redo previous manual pruning.
	 */
	public void redo() {
		if (runResults.size() > 0) {
			RunResult runResult = runResults.get(selectedRun);
			int currentTree = figTreePanel.getTreeViewer().getCurrentTreeIndex();
			BitSet pruning = runResult.getPrunedTaxaBits().get(currentTree);

			pruning.xor(runResult.getNextChange());

			runResult.updateRun(currentTree);
			runTableModel.fireTableDataChanged();
			runTable.getSelectionModel().setSelectionInterval(selectedRun, selectedRun);
		}
	}



	/**
	 * Opens a new MastadonFrame with a BitTreeSystem that has all the pruned taxa completely removed from the dataset. 
	 * Clade probabilities are recalculated and pruning can be done as usual.
	 * "Commit" is a bit misleading as the old data won't be lost.
	 */
	public void commitPruning() {
		MastodonFrame fr = (MastodonFrame) Application.getApplication().doNew();
		Launcher newLauncher = new Launcher(fr);
		newLauncher.setCopiedAndPrunedBTS(launcher, runResults.get(selectedRun).getPrunedTaxaBits().get(figTreePanel.getTreeViewer().getCurrentTreeIndex()));
		fr.launcher = newLauncher;

		fr.getAlgorithmAction().setEnabled(true);
		fr.progressBar.setString("");
		fr.runResults.add(fr.launcher.getResults());
		fr.selectedRun = fr.runResults.size() - 1;			
		fr.runTableModel.fireTableDataChanged();

		//highlight current run in runTable and update display
		fr.runTable.getSelectionModel().setSelectionInterval(fr.selectedRun, fr.selectedRun);

		((CardLayout)fr.cardPanel.getLayout()).show(fr.cardPanel, "score");
		//setup RunResult

	}


	private File openDefaultDirectory = null;

	private void setDefaultDir(File file) {
		final String s = file.getAbsolutePath();
		String p = s.substring(0, s.length() - file.getName().length());
		openDefaultDirectory = new File(p);
		if (!openDefaultDirectory.isDirectory()) {
			openDefaultDirectory = null;
		}
	}
	/**
	 * needs JavaDoc
	 */
	protected boolean readFromFile(File file) {
		return false;
	}

	/**
	 * Brings up input file processing options and dispatches the task to read trees and store as BitTrees. 
	 */
	protected void importTrees() {
		FileDialog dialog = new FileDialog(this, "Import Tree Data", FileDialog.LOAD);
		dialog.setVisible(true);

		File file = null;

		if (dialog.getFile() != null) {
			file = new File(dialog.getDirectory(), dialog.getFile());
		} else {
			return;
		}


		LoadFileDialog lfDialog = new LoadFileDialog(this);
		setDefaultDir(file);

		if(lfDialog.showDialog() == JOptionPane.OK_OPTION) {
			int burninInt = 0;
			try {
				burninInt = (int) Double.parseDouble(lfDialog.getBurning());
				if (burninInt < 0) {
					burninInt = 0;
				}
			} catch (Exception ignore) {
			}

			if (launcher == null) {
				launcher = new Launcher(this);
			}
			launcher.setFileName(file.getAbsolutePath());


			//start a timer that will update progress every second
			timer = new javax.swing.Timer(1000, new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					progressBar.setStringPainted(true);
					progressBar.setString(launcher.getTreeCounter() + " trees loaded");
				}
			});
			new ReadFileWorker(burninInt, lfDialog.getOutgroup()).execute();
			timer.start();
			progressBar.setStringPainted(true);
			return;
		}  else {
			return;
		}
	}


	class ReadFileWorker extends SwingWorker<Void, Void> {
		int burnin;
		String outgroupString;
		boolean success;

		ReadFileWorker(int b, String os) {
			burnin = b;
			outgroupString = os;
		}

		protected Void doInBackground() throws Exception {
			success = launcher.processFile(burnin, outgroupString);
			return null;
		}

		protected void done() {
			timer.stop();
			if (success) {
				//set up a Run without an algorithm done first and display it
				getAlgorithmAction().setEnabled(true);
				progressBar.setString("");
				runResults.add(launcher.getResults());
				selectedRun = runResults.size() - 1;			
				runTableModel.fireTableDataChanged();

				//highlight current run in runTable and update display
				runTable.getSelectionModel().setSelectionInterval(selectedRun, selectedRun);
				
				//can't import twice into the same frame
				getImportAction().setEnabled(false);
				((CardLayout)cardPanel.getLayout()).show(cardPanel, "score");
			} else {
				progressBar.setString("");
				progressBar.setStringPainted(false);
			}
			
		}
	}

	/**
	 * Provides options for exporting the tree view using "freehep.jar".
	 */
	public final void doExportGraphic() {
		ExportDialog export = new ExportDialog();
		export.showExportDialog( this, "Export view as ...", figTreePanel.getTreeViewer().getContentPane(), "export" );
	}

	protected boolean writeToFile(File file) {
		try {
			FileOutputStream fileOut = new FileOutputStream(file);
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(runResults);
			out.close();
			fileOut.close();
			return true;
		} catch(IOException i) {
			i.printStackTrace();
			return true;
		}
	}

	public JComponent getExportableComponent() {
		return figTreePanel.getTreeViewer();
	}


	/**
	 * A TableRenderer that grays out taxa that have been removed from the tree after a commit.
	 *
	 */
	private class ColorRenderer extends TableRenderer {
		public ColorRenderer(int arg0, Insets arg1) {
			super(arg0, arg1);
		}

		public Component getTableCellRendererComponent(JTable table, Object value,
				boolean isSelected, boolean hasFocus, int row, int column) {

			Component rendererComp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
					row, column);

			if(((ResultTableModel) table.getModel()).isPruned(table.convertRowIndexToView(row))) {
				rendererComp.setForeground(Color.LIGHT_GRAY);
				rendererComp.setBackground(Color.GRAY);
			}

			return rendererComp;
		}
	}


	/**
	 * Modle for table that displays the different algorithm runs done. 
	 */
	class RunTableModel extends AbstractTableModel {
		final String[] columnNames = {"Run","Pruned k", "Min k", "Max k", "Score"};

		public int getColumnCount() {
			return columnNames.length;
		}

		public int getRowCount() {
			int n = runResults.size();
			if(n == 0) return n+1;
			return n;
		}

		public String getColumnName(int col) {
			return columnNames[col];
		}

		public Object getValueAt(int row, int col) {
			RunResult runResult; 

			if (runResults.size() == 0) {
				switch (col) {
				case 0:
					return "No pruning done yet";
				case 1:
					return "";
				}
			} else {
				runResult = runResults.get(row);
				switch (col) {
				case 0:
					return runResult.getName();
				case 1:
					if (runResult.getPrunedTaxa().size() > 0) {
						return runResult.getPrunedTaxa().get(figTreePanel.getTreeViewer().getCurrentTreeIndex()).size();
					} else {
						return 0;
					}
				case 2:
					return runResult.getMinPruning();
				case 3:
					return runResult.getMaxPruning();
				case 4:
					//justification for .get(0) : 
					//all runs will have at least 1 tree; all scores in the maxima list are equal, though the number of matching trees could be different
					return runResult.getPruningScores().get(0)[0];
				}
			}

			return null;
		}

	}

	public Action getAlgorithmAction() {
		return algorithmAction;
	}

	public Action getRemoveRunAction() {
		return removeRunAction;
	}

	public Action getExportGraphicAction() {
		return exportGraphicAction;
	}

	public Action getManualPruneAction() {
		return manualPruneAction;
	}

	public Action getUndoAction() {
		return undoAction;
	}

	public Action getRedoAction() {
		return redoAction;
	}

	public Action getCommitAction() {
		return commitAction;
	}

	public Action getImportAction() {
		return importAction;
	}

	protected AbstractAction importAction = new AbstractAction("Import trees") {
		public void actionPerformed(ActionEvent ae) {
			importTrees();
		}
	};

	protected AbstractAction undoAction = new AbstractAction("Undo") {
		public void actionPerformed(ActionEvent ae) {
			undo();
		}
	};

	protected AbstractAction redoAction = new AbstractAction("Redo") {
		public void actionPerformed(ActionEvent ae) {
			redo();
		}
	};

	protected AbstractAction commitAction = new AbstractAction("Commit") {
		public void actionPerformed(ActionEvent ae) {
			commitPruning();
		}
	};

	protected AbstractAction manualPruneAction = new AbstractAction("Flip selected") {
		public void actionPerformed(ActionEvent ae) {
			pruneTaxa();
		}
	};

	protected AbstractAction algorithmAction = new AbstractAction("Run") {
		public void actionPerformed(ActionEvent ae) {
			try {
				doAlgorithm();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ImportException e) {
				e.printStackTrace();
			}
		}
	};

	private final AbstractAction removeRunAction = new AbstractAction("Remove") {
		public void actionPerformed(ActionEvent ae) {
			removeRun();
		}
	};

	private final AbstractAction exportGraphicAction = new AbstractAction("Export Graphic...") {
		public void actionPerformed(ActionEvent ae) {
			doExportGraphic();
		}
	};
}	
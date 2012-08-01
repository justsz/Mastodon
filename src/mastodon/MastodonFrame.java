package mastodon;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.DefaultFontMapper;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;
import mastodon.app.gui.MapperPanel;
import mastodon.app.gui.FileDrop;
import mastodon.app.gui.ChartRuntimeException;
import mastodon.app.gui.TableEditorStopper;
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
import jam.framework.MultiDocApplication;
import jam.panels.ActionPanel;
import jam.table.TableRenderer;

import org.freehep.util.export.ExportDialog;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.BorderUIResource;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;
import java.io.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import jebl.evolution.graphs.Node;
import jebl.evolution.io.ImportException;
import jebl.evolution.taxa.Taxon;
import jebl.evolution.trees.RootedTree;
import jebl.evolution.trees.SimpleRootedTree;

/**
 * @author Just Zarins
 * @author Andrew Rambaut
 * @version $Id$
 */
public class MastodonFrame extends DocumentFrame implements MastodonFileMenuHandler {
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

	private List<RunResult> runResults;
	private int selectedRun;
	private JLabel score;

	String message = "";
	private int dividerLocation = -1;

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

		AbstractAction importAction = new AbstractAction("Import Trace File...") {
			public void actionPerformed(ActionEvent ae) {
				doImport();
			}
		};
		setImportAction(importAction);
		setExportAction(exportDataAction);

		setAnalysesEnabled(false);

		runResults = new ArrayList<RunResult>();
	}

	TreeViewerListener scoreListener = new TreeViewerListener() {
		public void treeChanged() {
			//TreeViewer treeViewer = figTreePanel.getTreeViewer();
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

	TreeSelectionListener selectionListener = new TreeSelectionListener() {

		@Override
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

	public int searchTable(String taxonName) {
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
		//runTable.getColumnModel().getColumn(0).setPreferredWidth(30);
		runTable.getColumnModel().getColumn(0).setCellRenderer(renderer);
		//runTable.getColumnModel().getColumn(1).setPreferredWidth(50);
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
		ActionPanel actionPanel1 = new ActionPanel(false);
		actionPanel1.setAddAction(getAlgorithmAction());
		actionPanel1.setRemoveAction(getRemoveRunAction());
		getAlgorithmAction().setEnabled(false);
		getRemoveRunAction().setEnabled(false);

		JPanel controlPanel1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		controlPanel1.add(actionPanel1);

		topPanel = new JPanel(new BorderLayout(0, 0));
		topPanel.setBorder(new BorderUIResource.EmptyBorderUIResource(new java.awt.Insets(0, 0, 6, 0)));
		topPanel.add(new JLabel("Pruning Runs:"), BorderLayout.NORTH);
		topPanel.add(scrollPane1, BorderLayout.CENTER);
		topPanel.add(controlPanel1, BorderLayout.SOUTH);

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

			//			public TableCellRenderer getCellRenderer(int row, int column) {
			//			    // TODO Auto-generated method stub
			//			    return colorRenderer;
			//			}

		};

		//resultTable.setDefaultRenderer(String.class, new ColorRenderer());



		resultTable.setAutoCreateRowSorter(true);
		resultTable.getColumnModel().getColumn(0).setPreferredWidth(10);
		resultTable.getColumnModel().getColumn(1).setPreferredWidth(5);
		//resultTable.getColumnModel().getColumn(1).setPreferredWidth(150);
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



		TableEditorStopper.ensureEditingStopWhenTableLosesFocus(resultTable);

		JScrollPane scrollPane2 = new JScrollPane(resultTable, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		JPanel bottomPanel = new JPanel(new BorderLayout(0, 0));
		bottomPanel.add(new JLabel("Pruned Taxa:"), BorderLayout.NORTH);
		bottomPanel.add(scrollPane2, BorderLayout.CENTER);

		JPanel leftPanel = new JPanel(new BorderLayout(0, 0));
		leftPanel.setPreferredSize(new Dimension(400, 300));
		splitPane1 = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, topPanel, bottomPanel);
		splitPane1.setBorder(null);

		JPanel progressPanel = new JPanel(new BorderLayout(0, 0));
		progressLabel = new JLabel("");
		progressBar = new JProgressBar();
		progressPanel.add(progressLabel, BorderLayout.NORTH);
		progressPanel.add(progressBar, BorderLayout.CENTER);
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
		//rightPanel.setBackground(new Color(231, 237, 246));
		rightPanel.setBorder(new BorderUIResource.EmptyBorderUIResource(new java.awt.Insets(12, 12, 12, 6)));


		JSplitPane splitPane2 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, leftPanel, rightPanel);
		splitPane2.setBorder(null);
		splitPane2.setDividerLocation(350);

		Color focusColor = UIManager.getColor("Focus.color");
		Border focusBorder = BorderFactory.createMatteBorder(2, 2, 2, 2, focusColor);
		splitPane1.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		new FileDrop(null, splitPane1, focusBorder, new FileDrop.Listener() {
			public void filesDropped(java.io.File[] files) {
				importFiles(files);
			}   // end filesDropped
		}); // end FileDrop.Listener

		getContentPane().setLayout(new java.awt.BorderLayout(0, 0));
		getContentPane().add(splitPane2, BorderLayout.CENTER);

		splitPane1.setDividerLocation(2000);
	}

	public void setVisible(boolean b) {
		super.setVisible(b);
		setupDividerLocation();
	}

	private void setupDividerLocation() {

		if (dividerLocation == -1 || dividerLocation == splitPane1.getDividerLocation()) {
			int h0 = topPanel.getHeight();
			int h1 = scrollPane1.getViewport().getHeight();
			int h2 = runTable.getPreferredSize().height;
			dividerLocation = h2 + h0 - h1;

			//		   	int h0 = topPanel.getHeight() - scrollPane1.getViewport().getHeight();
			// 			dividerLocation = traceTable.getPreferredSize().height + h0;

			if (dividerLocation > 400) dividerLocation = 400;
			splitPane1.setDividerLocation(dividerLocation);
		}
	}

	public void setAnalysesEnabled(boolean enabled) {
		getExportAction().setEnabled(enabled);
		getExportDataAction().setEnabled(enabled);
		getExportPDFAction().setEnabled(enabled);
		getCopyAction().setEnabled(true);
	}


	private void removeRun() {
		//figTreePanel.getTreeViewer().
		int selRow = runTable.getSelectedRow();

		if (selRow < 0) {
			return;
		}

		runResults.remove(selRow);
		int prevRow = selRow - 1;
		if (prevRow < 0) {
			//getRemoveRunAction().setEnabled(false);
			prevRow = 0;
		}
		selectedRun = prevRow;
		runTableModel.fireTableDataChanged();
		runTable.getSelectionModel().setSelectionInterval(selectedRun, selectedRun);
		//		setupDividerLocation();
	}



	public void runTableSelectionChanged() {
		int selRow = runTable.getSelectedRow();

		//needed?
		if (selRow < 0) {
			getRemoveRunAction().setEnabled(false);
			//setAnalysesEnabled(false);
			return;
		}
		//

		//what is this?
		setAnalysesEnabled(true);

		if(runResults.size() > 0) {
			topToolbar.enablePruneButton(true);
			topToolbar.enableColorButtons(true);
			getRemoveRunAction().setEnabled(true);
		} else {
			topToolbar.enablePruneButton(false);
			topToolbar.enableColorButtons(false);
			getRemoveRunAction().setEnabled(false);
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
		} else {
			figTreePanel.getTreeViewer().setTrees(runResult.getPrunedMapTrees());			
		}
		topToolbar.fireTreesChanged(); 
		resultTableModel.fireTableDataChanged();

		//figTreePanel.setColourBy("pruned");
	}

	private boolean quiet = false;

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

	public final void doExportData() {

		FileDialog dialog = new FileDialog(this,
				"Export Data...",
				FileDialog.SAVE);

		dialog.setVisible(true);
		if (dialog.getFile() != null) {
			File file = new File(dialog.getDirectory(), dialog.getFile());

			try {
				FileWriter writer = new FileWriter(file);
				//                writer.write(mapperPanel.getExportText());
				writer.close();


			} catch (IOException ioe) {
				JOptionPane.showMessageDialog(this, "Unable to write file: " + ioe,
						"Unable to write file",
						JOptionPane.ERROR_MESSAGE);
			}
		}

	}

	public final void doExportPDF() {
		FileDialog dialog = new FileDialog(this,
				"Export PDF Image...",
				FileDialog.SAVE);

		dialog.setVisible(true);
		if (dialog.getFile() != null) {
			File file = new File(dialog.getDirectory(), dialog.getFile());

			Rectangle2D bounds = figTreePanel.getTreeViewer().getBounds();
			Document document = new Document(new com.lowagie.text.Rectangle((float) bounds.getWidth(), (float) bounds.getHeight()));
			try {
				// step 2
				PdfWriter writer;
				writer = PdfWriter.getInstance(document, new FileOutputStream(file));
				// step 3
				document.open();
				// step 4
				PdfContentByte cb = writer.getDirectContent();
				PdfTemplate tp = cb.createTemplate((float) bounds.getWidth(), (float) bounds.getHeight());
				Graphics2D g2d = tp.createGraphics((float) bounds.getWidth(), (float) bounds.getHeight(), new DefaultFontMapper());
				figTreePanel.getTreeViewer().print(g2d);
				g2d.dispose();
				cb.addTemplate(tp, 0, 0);
			}
			catch (DocumentException de) {
				JOptionPane.showMessageDialog(this, "Error writing PDF file: " + de,
						"Export PDF Error",
						JOptionPane.ERROR_MESSAGE);
			}
			catch (FileNotFoundException e) {
				JOptionPane.showMessageDialog(this, "Error writing PDF file: " + e,
						"Export PDF Error",
						JOptionPane.ERROR_MESSAGE);
			}
			document.close();
		}
	}

	public final void doImport() {
		final JFileChooser chooser = new JFileChooser(openDefaultDirectory);
		chooser.setMultiSelectionEnabled(true);

		FileNameExtensionFilter filter = new FileNameExtensionFilter("BEAST log (*.log) Files", "log", "txt");
		chooser.setFileFilter(filter);

		final int returnVal = chooser.showOpenDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File[] files = chooser.getSelectedFiles();
			importFiles(files);
		}
	}

	private void importFiles(File[] files) {
		//        LogFileTraces[] traces = new LogFileTraces[files.length];
		//
		//        for (int i = 0; i < files.length; i++) {
		//            traces[i] = new LogFileTraces(files[i].getName(), files[i]);
		//        }
		//
		//        processTraces(traces);


	}

	PruningDialog pruningDialog;
	Launcher launcher;
	javax.swing.Timer timer;

	public Action getAlgorithmAction() {
		return algorithmAction;
	}

	protected AbstractAction algorithmAction = new AbstractAction("Run...") {
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

	AlgorithmDialog algorithmDialog;

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
				} else if((int) (selection / 10) == 4) {
					algorithm = new FlipPenaltyAlgorithm();
				} else { //constant or linear
					if ((int) (selection % 10) == 1) {	//SA
						algorithm = new SALinearAlgorithm();
					} else { //MH
						algorithm = new MHLinearAlgorithm();
					}
				}

				launcher.setupAlgorithm(algorithm, input);
				new AlgorithmWorker().execute();

				timer = new javax.swing.Timer(1000, new ActionListener() {
					public void actionPerformed(ActionEvent evt) {
						progressBar.setValue(launcher.getCurrentIterations(selection));
					}
				});

				timer.start();
				getAlgorithmAction().setEnabled(false);
			}//the input verifier will display the input validation error if required
		}
	}

	class AlgorithmWorker extends SwingWorker<Void, Void> {
		protected Void doInBackground() throws Exception {
			topToolbar.enablePruneButton(false);
			launcher.runAlgorithm();
			return null;
		}

		protected void done() {
			timer.stop();
			runResults.add(launcher.getResults());
			selectedRun = runResults.size() - 1;			
			runTableModel.fireTableDataChanged();

			//highlight current run in runTable and update display
			runTable.getSelectionModel().setSelectionInterval(selectedRun, selectedRun);

			//switch from progress bar to score panel
			((CardLayout)cardPanel.getLayout()).show(cardPanel, "score");
			getAlgorithmAction().setEnabled(true);
			topToolbar.enablePruneButton(true);
		}

	}

	public void pruneTaxa() {
		RunResult runResult = runResults.get(selectedRun);
		int currentTree = figTreePanel.getTreeViewer().getCurrentTreeIndex();
		BitSet pruning = runResult.getPrunedTaxaBits().get(currentTree);
		System.out.println(pruning);

		int[] selRows = resultTable.getSelectedRows();
		if (selRows.length > 0) {
			//List<String> taxonNames = new ArrayList<String>();
			for(int i = 0; i < selRows.length; i++) {
				//taxonNames.add((String) resultTableModel.getValueAt(resultTable.convertRowIndexToModel(selRows[i]), 2));
				//don't prune if it is in the pruned set after a "commit"
				if (!((ResultTableModel) resultTable.getModel()).isPruned(resultTable.convertRowIndexToView(selRows[i]))) {
					pruning.flip(resultTable.convertRowIndexToModel(selRows[i]));
				}
			}
			runResult.updateRun(currentTree);
			//			resultTableModel.fireTableDataChanged();
			//			runTableModel.fireTableDataChanged();
			runTableModel.fireTableDataChanged();
			updateDataDisplay();
		} else {
			//do nothing
		}
	}

	//"commit" is a bit misleading as the old data won't be lost
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

	protected boolean readFromFile(File file) throws IOException {
		//String input = JOptionPane.showInputDialog("Discard first x trees as burn-in:", "0");
		LoadFileDialog lfDialog = new LoadFileDialog(this);

		if(lfDialog.showDialog() == JOptionPane.OK_OPTION) {
			//String input2 = JOptionPane.showInputDialog("Specify outgroup (leave blank if trees are rooted):", "");
			//if (input2 != null) {
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
			//		boolean success = false;
			launcher.setFileName(file.getAbsolutePath());

			timer = new javax.swing.Timer(1000, new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					progressBar.setStringPainted(true);
					progressBar.setString(launcher.getTreeCounter() + " trees loaded");
				}
			});
			new ReadFileWorker(burninInt, lfDialog.getOutgroup()).execute();
			timer.start();
			return true;
		}  else {
			//} 
			return false;
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
				if(launcher.getTreeCounter() < 1) {
					JOptionPane.showMessageDialog(launcher.getFrame(), "File " + launcher.getFileName() + " contains no trees or all were discarded in Burn-in.",
							"Error",
							JOptionPane.ERROR_MESSAGE);
				} else {
					//((SimpleTreeViewer)figTreePanel.getTreeViewer()).setTree(launcher.getMapTree());
					getAlgorithmAction().setEnabled(true);
					progressBar.setString("");
					runResults.add(launcher.getResults());
					selectedRun = runResults.size() - 1;			
					runTableModel.fireTableDataChanged();

					//highlight current run in runTable and update display
					runTable.getSelectionModel().setSelectionInterval(selectedRun, selectedRun);


				}
			} else {
				((DocumentFrame) launcher.getFrame()).doCloseWindow();
			}
			((CardLayout)cardPanel.getLayout()).show(cardPanel, "score");
		}

	}

	protected boolean writeToFile(File file) {
		throw new RuntimeException("Cannot write file - this is a read-only application");
	}

	public void doCopy() {

	}


	public JComponent getExportableComponent() {
		return figTreePanel.getTreeViewer();
	}

	private class ColorRenderer extends TableRenderer {
		/**
		 * @param arg0
		 * @param arg1
		 */
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

	public Action getExportDataAction() {
		return exportDataAction;
	}

	public Action getExportPDFAction() {
		return exportPDFAction;
	}


	public Action getRemoveRunAction() {
		return removeRunAction;
	}

	public Action getExportGraphicAction() {
		return exportGraphicAction;
	}

	private final AbstractAction removeRunAction = new AbstractAction() {
		public void actionPerformed(ActionEvent ae) {
			removeRun();
		}
	};

	private final AbstractAction exportDataAction = new AbstractAction("Export Data...") {
		public void actionPerformed(ActionEvent ae) {
			doExportData();
		}
	};

	private final AbstractAction exportPDFAction = new AbstractAction("Export PDF...") {
		public void actionPerformed(ActionEvent ae) {
			doExportPDF();
		}
	};

	private final AbstractAction exportGraphicAction = new AbstractAction("Export Graphic...") {
		public void actionPerformed(ActionEvent ae) {
			doExportGraphic();
		}
	};

	public final void doExportGraphic() {
		ExportDialog export = new ExportDialog();
		export.showExportDialog( this, "Export view as ...", figTreePanel.getTreeViewer().getContentPane(), "export" );
	}

}	
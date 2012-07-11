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
import mastodon.core.RunResult;
import mastodon.entryPoints.Launcher;
import mastodon.inputVerifiers.GUIInputVerifier;
import figtree.application.PruningDialog;
import figtree.treeviewer.TreeViewerListener;
import jam.framework.DocumentFrame;
import jam.panels.ActionPanel;
import jam.table.TableRenderer;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.BorderUIResource;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;
import java.io.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import jebl.evolution.io.ImportException;

/**
 * @author Just Zarins
 * @author Andrew Rambaut
 * @version $Id$
 */
public class MastodonFrame extends DocumentFrame implements MastodonFileMenuHandler {
	private final String[] columnToolTips = {"", "", ""};

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

	TreeViewerListener scoreListner = new TreeViewerListener() {
		public void treeChanged() {
			//TreeViewer treeViewer = figTreePanel.getTreeViewer();
			float[] scores = runResults.get(selectedRun).getPruningScores().get(figTreePanel.getTreeViewer().getCurrentTreeIndex());
			score.setText("Map score: " + scores[0] + " Found in " + (int)scores[1] + " trees.");
		}

		public void treeSettingsChanged() {
			// nothing to do
		}
	};

	public void initializeComponents() {

		setSize(new java.awt.Dimension(1200, 800));

		figTreePanel = new FigTreePanel(FigTreePanel.Style.DEFAULT);
		figTreePanel.setBorder(new BorderUIResource.EmptyBorderUIResource(new java.awt.Insets(12, 6, 12, 12)));
		figTreePanel.getTreeViewer().addTreeViewerListener(scoreListner);

		runTableModel = new RunTableModel();
		runTable = new JTable(runTableModel);
		TableRenderer renderer = new TableRenderer(SwingConstants.LEFT, new Insets(0, 4, 0, 4));
		runTable.getColumnModel().getColumn(0).setPreferredWidth(30);
		runTable.getColumnModel().getColumn(0).setCellRenderer(renderer);
		runTable.getColumnModel().getColumn(1).setPreferredWidth(50);
		runTable.getColumnModel().getColumn(1).setCellRenderer(renderer);
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
		actionPanel1.setAddAction(getPruningOptionAction());
		actionPanel1.setRemoveAction(getRemoveRunAction());
		getRemoveRunAction().setEnabled(false);

		JPanel controlPanel1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		controlPanel1.add(actionPanel1);

		topPanel = new JPanel(new BorderLayout(0, 0));
		topPanel.setBorder(new BorderUIResource.EmptyBorderUIResource(new java.awt.Insets(0, 0, 6, 0)));
		topPanel.add(new JLabel("Pruning Runs:"), BorderLayout.NORTH);
		topPanel.add(scrollPane1, BorderLayout.CENTER);
		topPanel.add(controlPanel1, BorderLayout.SOUTH);

		resultTableModel = new ResultTableModel(null, figTreePanel);
		resultTable = new JTable(resultTableModel) {
			//Implement table header tool tips.
			protected JTableHeader createDefaultTableHeader() {
				return new JTableHeader(columnModel) {
					public String getToolTipText(MouseEvent e) {
						java.awt.Point p = e.getPoint();
						int index = columnModel.getColumnIndexAtX(p.x);
						int realIndex = columnModel.getColumn(index).getModelIndex();
						return columnToolTips[realIndex];
					}
				};
			}
		};
		resultTable.getColumnModel().getColumn(0).setPreferredWidth(150);
		resultTable.getColumnModel().getColumn(0).setCellRenderer(renderer);
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

		topToolbar = new TopToolbar((SimpleTreeViewer) figTreePanel.getTreeViewer(), resultTable);
		JPanel rightPanel = new JPanel(new BorderLayout(0,0));
		rightPanel.add(topToolbar.getToolbar(), BorderLayout.NORTH);
		rightPanel.add(figTreePanel, BorderLayout.CENTER);
		rightPanel.setBackground(new Color(231, 237, 246));
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
		int selRow = runTable.getSelectedRow();
		
		if (selRow < 0) {
			return;
		}
		
		runResults.remove(selRow);
		int prevRow = selRow - 1;
		if (prevRow < 0) {
			getRemoveRunAction().setEnabled(false);
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
			setAnalysesEnabled(false);
			return;
		}
		//

		setAnalysesEnabled(true);
		getRemoveRunAction().setEnabled(true);
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

				
	}


	public void resultTableSelectionChanged() {

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
	public final void doPruning() throws IOException, ImportException {
		if (pruningDialog == null) {
			pruningDialog = new PruningDialog(this);
		}

		if (pruningDialog.showDialog() == JOptionPane.OK_OPTION) {
			if(pruningDialog.getFile() != null) {
				String file = pruningDialog.getFile();
				String minScore = pruningDialog.getMinScore();
				String maxPruning = pruningDialog.getMaxPrunedTaxa();
				String iterations = pruningDialog.getIterations();

				if(GUIInputVerifier.verifyMHAlgorithmInput(minScore, maxPruning, iterations)) {
					if(launcher == null) {
						launcher = new Launcher(this, file, minScore, maxPruning, iterations);
					} else {
						launcher.setFrame(this);
						launcher.setFileName(file);
						launcher.setMinScore(minScore);
						launcher.setMaxPruned(maxPruning);
						launcher.setIterations(iterations);
					}

					((CardLayout)cardPanel.getLayout()).show(cardPanel, "progress");
					progressBar.setMaximum(launcher.getIterations());
					progressBar.setValue(0);					
					progressBar.setString("");
					progressBar.setStringPainted(true);
					timer = new javax.swing.Timer(1000, new ActionListener() {
						public void actionPerformed(ActionEvent evt) {
							if (launcher.getCurrentIterations() > 0) {
								progressBar.setStringPainted(false);
								progressBar.setValue(launcher.getCurrentIterations());
							} else {
								progressBar.setString(launcher.getTreeCounter() + " trees loaded");
							}
						}
					});

					getPruningOptionAction().setEnabled(false);
					new PruningWorker().execute();
					timer.start();
				}//the input verifier will display the input validation error if required
				
			} else {
				JOptionPane.showMessageDialog(this,
						"Please select file.", "Error Massage",
						JOptionPane.ERROR_MESSAGE);
			}

		}
	}

	class PruningWorker extends SwingWorker<Void, Void> {
		protected Void doInBackground() throws Exception {
			launcher.launchMH();
			return null;
		}

		protected void done() {
			timer.stop();
			runResults.add(launcher.getResults());
			selectedRun = runResults.size() - 1;
			RunResult runResult = runResults.get(selectedRun);
			figTreePanel.getTreeViewer().setTrees(runResult.getPrunedMapTrees());
			resultTableModel.setRunResult(runResult);
			figTreePanel.setColourBy("pruned");
			runTableModel.fireTableDataChanged();
			resultTableModel.fireTableDataChanged();
			
			//highlight current run in runTable
			runTable.getSelectionModel().setSelectionInterval(selectedRun, selectedRun);
			
			//switch from progress bar to score panel
			((CardLayout)cardPanel.getLayout()).show(cardPanel, "score");
			getPruningOptionAction().setEnabled(true);
		}

	}



	public Action getPruningOptionAction() {
		return pruningOptionAction;
	}

	protected AbstractAction pruningOptionAction = new AbstractAction("Prune...") {
		public void actionPerformed(ActionEvent ae) {
			try {
				doPruning();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ImportException e) {
				e.printStackTrace();
			}
		}
	};

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
		throw new RuntimeException("Cannot read file - use import instead");
	}

	protected boolean writeToFile(File file) {
		throw new RuntimeException("Cannot write file - this is a read-only application");
	}

	public void doCopy() {

	}


	public JComponent getExportableComponent() {
		return figTreePanel.getTreeViewer();
	}

	
	class RunTableModel extends AbstractTableModel {
		final String[] columnNames = {"Run", "Score"};

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
					return "No runs available";
				case 1:
					return "";
				}
			} else {
				runResult = runResults.get(row);
				switch (col) {
				case 0:
					return runResult.getName();
				case 1:
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

}
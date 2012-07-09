package mastodon;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.DefaultFontMapper;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;
import mastodon.app.gui.Layer;
import mastodon.app.gui.MapperPanel;
import mastodon.app.gui.FileDrop;
import mastodon.app.gui.ChartRuntimeException;
import mastodon.app.gui.TableEditorStopper;
import mastodon.app.gui.LongTask;
import mastodon.core.RunResult;
import mastodon.entryPoints.Launcher;
import mastodon.inputVerifiers.GUIInputVerifier;
import mastodon.trace.*;
import figtree.application.PruningDialog;
import figtree.treeviewer.ExtendedTreeViewer;
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
import jebl.evolution.taxa.Taxon;
import jebl.evolution.trees.SimpleRootedTree;
import jebl.evolution.trees.Tree;

/**
 * @author Just Zarins
 * @author Andrew Rambaut
 * @version $Id$
 */
public class MastodonFrame extends DocumentFrame implements MastodonFileMenuHandler {
	private final String[] columnToolTips = {"", "", ""};

	private FigTreePanel figTreePanel = null;

	private JTable traceTable = null;
	private TraceTableModel traceTableModel = null;
	private JSplitPane splitPane1 = null;
	private JPanel topPanel = null;
	
	private JPanel cardPanel;
	
	private TopToolbar topToolbar;

	private JTable resultTable = null;
	private ResultTableModel resultTableModel = null;


	private JScrollPane scrollPane1;

	private JLabel progressLabel;
	private JProgressBar progressBar;

	private final List<LogFileTraces> traceLists = new ArrayList<LogFileTraces>();
	//private final List<Layer> layers = new ArrayList<Layer>();
	private RunResult runResult;
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
		
	}
	
	TreeViewerListener scoreListner = new TreeViewerListener() {
		public void treeChanged() {
			//TreeViewer treeViewer = figTreePanel.getTreeViewer();
			float[] scores = runResult.getPruningScores().get(figTreePanel.getTreeViewer().getCurrentTreeIndex());
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

		traceTableModel = new TraceTableModel();
		traceTable = new JTable(traceTableModel);
		TableRenderer renderer = new TableRenderer(SwingConstants.LEFT, new Insets(0, 4, 0, 4));
		traceTable.getColumnModel().getColumn(0).setCellRenderer(renderer);
		traceTable.getColumnModel().getColumn(1).setPreferredWidth(50);
		traceTable.getColumnModel().getColumn(1).setCellRenderer(renderer);
		traceTable.getColumnModel().getColumn(2).setPreferredWidth(50);
		traceTable.getColumnModel().getColumn(2).setCellRenderer(renderer);
		traceTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		traceTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent evt) {
				traceTableSelectionChanged();
			}
		});

		scrollPane1 = new JScrollPane(traceTable, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		
		//the little plus/minus sign under top right table?
		ActionPanel actionPanel1 = new ActionPanel(false);
		actionPanel1.setAddAction(getImportAction());
		actionPanel1.setRemoveAction(getRemoveTraceAction());
		getRemoveTraceAction().setEnabled(false);

		JPanel controlPanel1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		controlPanel1.add(actionPanel1);

		topPanel = new JPanel(new BorderLayout(0, 0));
		topPanel.setBorder(new BorderUIResource.EmptyBorderUIResource(new java.awt.Insets(0, 0, 6, 0)));
		topPanel.add(new JLabel("Tree Files:"), BorderLayout.NORTH);
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
		//        layerTable.getColumnModel().getColumn(1).setPreferredWidth(70);
		//        layerTable.getColumnModel().getColumn(1).setCellRenderer(renderer);
		//        layerTable.getColumnModel().getColumn(2).setPreferredWidth(70);
		//        layerTable.getColumnModel().getColumn(2).setCellRenderer(renderer);
		//        ComboBoxRenderer comboBoxRenderer = new ComboBoxRenderer(TraceFactory.TraceType.values());
		//        comboBoxRenderer.putClientProperty("JComboBox.isTableCellEditor", Boolean.TRUE);
		//        layerTable.getColumnModel().getColumn(3).setPreferredWidth(20);
		//        layerTable.getColumnModel().getColumn(3).setCellRenderer(renderer);
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
		bottomPanel.add(new JLabel("Layers:"), BorderLayout.NORTH);
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
			int h2 = traceTable.getPreferredSize().height;
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

	public void addTraceList(LogFileTraces traceList) {

		int[] selRows = traceTable.getSelectedRows();

		traceLists.add(traceList);

		setAnalysesEnabled(true);

		traceTableModel.fireTableDataChanged();

		int newRow = traceLists.size() - 1;
		traceTable.getSelectionModel().setSelectionInterval(newRow, newRow);
		if (selRows.length > 1) {
			for (int row : selRows) {
				if (row == traceLists.size() - 1) {
					row = traceLists.size();
				}
				traceTable.getSelectionModel().addSelectionInterval(row, row);
			}
		}

		setupDividerLocation();
	}

	private void removeTraceList() {
		int[] selRows = traceTable.getSelectedRows();

		LogFileTraces[] tls = new LogFileTraces[selRows.length];
		int i = 0;
		for (int row : selRows) {
			tls[i] = traceLists.get(row);
			i++;
		}
		for (LogFileTraces tl : tls) {
			traceLists.remove(tl);
		}

		traceTableModel.fireTableDataChanged();
		resultTableModel.fireTableDataChanged();

		if (traceLists.size() == 0) {
			getRemoveTraceAction().setEnabled(false);

			setAnalysesEnabled(false);

			resultTableModel.fireTableDataChanged();
		}


		if (traceLists.size() > 0) {
			int row = selRows[0];
			if (row >= traceLists.size()) {
				row = traceLists.size() - 1;
			}
			traceTable.getSelectionModel().addSelectionInterval(row, row);
		}
		setupDividerLocation();
	}

	public void setBurnIn(int index, int burnIn) {
		LogFileTraces trace = traceLists.get(index);
		trace.setBurnIn(burnIn);
		analyseTraceList(trace);
		updateTraceTables();
	}

	public void updateTraceTables() {
		int[] selectedTraces = traceTable.getSelectedRows();
		int[] selectedStatistics = resultTable.getSelectedRows();

		traceTableModel.fireTableDataChanged();
		resultTableModel.fireTableDataChanged();

		traceTable.getSelectionModel().clearSelection();
		for (int row : selectedTraces) {
			traceTable.getSelectionModel().addSelectionInterval(row, row);
		}

		resultTable.getSelectionModel().clearSelection();
		for (int row : selectedStatistics) {
			resultTable.getSelectionModel().addSelectionInterval(row, row);
		}
	}

	public void traceTableSelectionChanged() {
		int[] selRows = traceTable.getSelectedRows();

		if (selRows.length == 0) {
			getRemoveTraceAction().setEnabled(false);
			setAnalysesEnabled(false);
			return;
		}

		setAnalysesEnabled(true);

		getRemoveTraceAction().setEnabled(true);

		int[] rows = resultTable.getSelectedRows();
		resultTableModel.fireTableDataChanged();

		if (rows.length > 0) {
			for (int row : rows) {
				resultTable.getSelectionModel().addSelectionInterval(row, row);
			}
		} else {
			resultTable.getSelectionModel().setSelectionInterval(0, 0);
		}
	}


	public void resultTableSelectionChanged() {

	}

	public void analyseTraceList(TraceList job) {

		if (analyseTask == null) {
			analyseTask = new AnalyseTraceTask();

			javax.swing.Timer timer = new javax.swing.Timer(1000, new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					progressBar.setMaximum(analyseTask.getLengthOfTask());
					progressBar.setValue(analyseTask.getCurrent());
				}
			});

			analyseTask.go();
			timer.start();
		}

		analyseTask.add(job);
	}

	AnalyseTraceTask analyseTask = null;

	class AnalyseTraceTask extends LongTask {

		class AnalysisStack<T> {
			private final java.util.List<T> jobs = new ArrayList<T>();

			public synchronized void add(T job) {
				jobs.add(job);
			}

			public synchronized int getCount() {
				return jobs.size();
			}

			public synchronized T get(int index) {
				return jobs.get(index);
			}

			public synchronized void remove(int index) {
				jobs.remove(index);
			}
		}

		private final AnalysisStack<TraceList> analysisStack = new AnalysisStack<TraceList>();

		public AnalyseTraceTask() {
		}

		public void add(TraceList job) {
			analysisStack.add(job);
			current = 0;
		}

		public int getCurrent() {
			return current;
		}

		public int getLengthOfTask() {
			int count = 0;
			for (int i = 0; i < analysisStack.getCount(); i++) {
				count += analysisStack.get(i).getTraceCount();
			}
			return count;
		}

		public void stop() {
		}

		public boolean done() {
			return false;
		}

		public String getDescription() {
			return "Analysing Trace File...";
		}

		public String getMessage() {
			return null;
		}

		public Object doWork() {

			current = 0;
			boolean textCleared = true;

			do {
				if (analysisStack.getCount() > 0) {
					Object job = analysisStack.get(0);
					TraceList tl = (TraceList) job;

					try {
						for (int i = 0; i < tl.getTraceCount(); i++) {
							progressLabel.setText("Analysing " + tl.getName() + ":");
							textCleared = false;
							tl.analyseTrace(i);
							repaint();
							current += 1;
						}
					} catch (final Exception ex) {
						// do nothing. An exception is sometimes fired when burnin is changed whilst in the
						// middle of an analysis. This doesn't seem to matter as the analysis is restarted.

						ex.printStackTrace();
						//                        EventQueue.invokeLater (
								//								new Runnable () {
									//									public void run () {
										//										JOptionPane.showMessageDialog(TracerFrame.this, "Fatal exception: " + ex.getMessage(),
												//												"Error reading file",
						//												JOptionPane.ERROR_MESSAGE);
						//									}
						//								});
					}
					analysisStack.remove(0);
				} else {
					if (!textCleared) {
						progressLabel.setText("");
						textCleared = true;
					}
					try {
						Thread.sleep(500);
					} catch (InterruptedException ie) {
						// do nothing
					}
				}
			} while (true);
		}

		//private int lengthOfTask = 0;
		private int current = 0;
		//private String message;
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

					pruningDialog.setVisible(false);

					if(launcher == null) {
						launcher = new Launcher(this, 
								file, 
								minScore, 
								maxPruning, 
								iterations);
					} else {
						launcher.setFrame(this);
						launcher.setFileName(file);
						launcher.setMinScore(minScore);
						launcher.setMaxPruned(maxPruning);
						launcher.setIterations(iterations);
					}

					launcher.launchMH();
					runResult = launcher.getResults();

					//temporary implementations

//					for(Tree tree : runResult.getPrunedMapTrees()) {
//						//((ExtendedTreeViewer)figTreePanel.getTreeViewer()).addTree(tree);
//						figTreePanel.setTree(tree);
//					}
					
					figTreePanel.getTreeViewer().setTrees(runResult.getPrunedMapTrees());
					resultTableModel.setRunResult(runResult);
					figTreePanel.setColourBy("pruned");
					resultTableModel.fireTableDataChanged();
					
					//switch from progress bar to score panel
					((CardLayout)cardPanel.getLayout()).show(cardPanel, "score");
					//topToolbar.fireTreesChanged();
					//((ExtendedTreeViewer)figTreePanel.getTreeViewer()).fireTreeChanged();
				}
			} else {
				JOptionPane.showMessageDialog(this,
						"Please select file.", "Error Massage",
						JOptionPane.ERROR_MESSAGE);
			}

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
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ImportException e) {
				// TODO Auto-generated catch block
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

	protected void processTraces(final LogFileTraces[] tracesArray) {
		//
		//        final JFrame frame = this;
		//
		//        // set default dir to directory of last file
		//        setDefaultDir(tracesArray[tracesArray.length - 1].getFile());
		//
		//        if (tracesArray.length == 1) {
		//            try {
		//                final LogFileTraces traces = tracesArray[0];
		//
		//                final String fileName = traces.getName();
		//                final ProgressMonitorInputStream in = new ProgressMonitorInputStream(
		//                        this,
		//                        "Reading " + fileName,
		//                        new FileInputStream(traces.getFile()));
		//                in.getProgressMonitor().setMillisToDecideToPopup(0);
		//                in.getProgressMonitor().setMillisToPopup(0);
		//
		//                final Reader reader = new InputStreamReader(in);
		//
		//                Thread readThread = new Thread() {
		//                    public void run() {
		//                        try {
		//                            traces.loadTraces(reader);
		//
		//                            EventQueue.invokeLater(
		//                                    new Runnable() {
		//                                        public void run() {
		//                                            analyseTraceList(traces);
		//                                            addTraceList(traces);
		//                                        }
		//                                    });
		//
		//                        } catch (final TraceException te) {
		//                            EventQueue.invokeLater(
		//                                    new Runnable() {
		//                                        public void run() {
		//                                            JOptionPane.showMessageDialog(frame, "Problem with trace file: " + te.getMessage(),
		//                                                    "Problem with tree file",
		//                                                    JOptionPane.ERROR_MESSAGE);
		//                                        }
		//                                    });
		//                        } catch (final InterruptedIOException iioex) {
		//                            // The cancel dialog button was pressed - do nothing
		//                        } catch (final IOException ioex) {
		//                            EventQueue.invokeLater(
		//                                    new Runnable() {
		//                                        public void run() {
		//                                            JOptionPane.showMessageDialog(frame, "File I/O Error: " + ioex.getMessage(),
		//                                                    "File I/O Error",
		//                                                    JOptionPane.ERROR_MESSAGE);
		//                                        }
		//                                    });
		////                    } catch (final Exception ex) {
		////                        EventQueue.invokeLater (
		////                                new Runnable () {
		////                                    public void run () {
		////                                        JOptionPane.showMessageDialog(frame, "Fatal exception: " + ex.getMessage(),
		////                                                "Error reading file",
		////                                                JOptionPane.ERROR_MESSAGE);
		////                                    }
		////                                });
		//                        }
		//
		//                    }
		//                };
		//                readThread.start();
		//
		//            } catch (FileNotFoundException fnfe) {
		//                JOptionPane.showMessageDialog(this, "Unable to open file: File not found",
		//                        "Unable to open file",
		//                        JOptionPane.ERROR_MESSAGE);
		//            } catch (IOException ioex) {
		//                JOptionPane.showMessageDialog(this, "File I/O Error: " + ioex,
		//                        "File I/O Error",
		//                        JOptionPane.ERROR_MESSAGE);
		//            } catch (Exception ex) {
		//                JOptionPane.showMessageDialog(this, "Fatal exception: " + ex,
		//                        "Error reading file",
		//                        JOptionPane.ERROR_MESSAGE);
		//            }
		//
		//        } else {
		//            Thread readThread = new Thread() {
		//                public void run() {
		//                    try {
		//                        for (final LogFileTraces traces : tracesArray) {
		//                            final Reader reader = new FileReader(traces.getFile());
		//                            traces.loadTraces(reader);
		//
		//                            EventQueue.invokeLater(
		//                                    new Runnable() {
		//                                        public void run() {
		//                                            analyseTraceList(traces);
		//                                            addTraceList(traces);
		//                                        }
		//                                    });
		//                        }
		//
		//                    } catch (final TraceException te) {
		//                        EventQueue.invokeLater(
		//                                new Runnable() {
		//                                    public void run() {
		//                                        JOptionPane.showMessageDialog(frame, "Problem with trace file: " + te.getMessage(),
		//                                                "Problem with tree file",
		//                                                JOptionPane.ERROR_MESSAGE);
		//                                    }
		//                                });
		//                    } catch (final InterruptedIOException iioex) {
		//                        // The cancel dialog button was pressed - do nothing
		//                    } catch (final IOException ioex) {
		//                        EventQueue.invokeLater(
		//                                new Runnable() {
		//                                    public void run() {
		//                                        JOptionPane.showMessageDialog(frame, "File I/O Error: " + ioex.getMessage(),
		//                                                "File I/O Error",
		//                                                JOptionPane.ERROR_MESSAGE);
		//                                    }
		//                                });
		////                    } catch (final Exception ex) {
		////                        EventQueue.invokeLater (
		////                                new Runnable () {
		////                                    public void run () {
		////                                        JOptionPane.showMessageDialog(frame, "Fatal exception: " + ex.getMessage(),
		////                                                "Error reading file",
		////                                                JOptionPane.ERROR_MESSAGE);
		////                                    }
		////                                });
		//                    }
		//
		//                }
		//            };
		//            readThread.start();
		//
		//        }
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

	class TraceTableModel extends AbstractTableModel {
		final String[] columnNames = {"Trace File", "States", "Burn-In"};

		public int getColumnCount() {
			return columnNames.length;
		}

		public int getRowCount() {
			int n = traceLists.size();
			if (n == 0) n++;
			return n;
		}

		public String getColumnName(int col) {
			return columnNames[col];
		}

		public Object getValueAt(int row, int col) {
			TraceList traceList;

			if (traceLists.size() == 0) {
				switch (col) {
				case 0:
					return "No files loaded";
				case 1:
					return "";
				case 2:
					return "";
				}
			} else {
				traceList = traceLists.get(row);
				switch (col) {
				case 0:
					return traceList.getName();
				case 1:
					return traceList.getMaxState();
				case 2:
					return traceList.getBurnIn();
				}
			}

			return null;
		}

		public void setValueAt(Object value, int row, int col) {
			if (col == 2) {
				setBurnIn(row, (Integer) value);
			}
		}

		public Class getColumnClass(int c) {
			return getValueAt(0, c).getClass();
		}

		public boolean isCellEditable(int row, int col) {
			return col == 2 && row < traceLists.size();
		}
	}

	public Action getExportDataAction() {
		return exportDataAction;
	}

	public Action getExportPDFAction() {
		return exportPDFAction;
	}


	public Action getRemoveTraceAction() {
		return removeTraceAction;
	}

	private final AbstractAction removeTraceAction = new AbstractAction() {
		public void actionPerformed(ActionEvent ae) {
			removeTraceList();
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
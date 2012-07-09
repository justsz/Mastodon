package mastodon;

import javax.swing.table.AbstractTableModel;

import mastodon.core.RunResult;

import jebl.evolution.taxa.Taxon;

	class ResultTableModel extends AbstractTableModel {
		private RunResult runResult;
		private FigTreePanel figTreePanel;
		
		/**
		 * @param runResult
		 * @param figTreePanel
		 */
		public ResultTableModel(RunResult runResult, FigTreePanel figTreePanel) {
			super();
			this.runResult = runResult;
			this.figTreePanel = figTreePanel;
		}
		
		
		final String[] columnNames = {"Taxon name"};

		public int getColumnCount() {
			return columnNames.length;
		}

		public int getRowCount() {
			if(runResult == null) {
				return 0;
			}
			int currentTree = figTreePanel.getTreeViewer().getCurrentTreeIndex();
			return runResult.getPrunedTaxa().get(currentTree).size();
		}

		public String getColumnName(int col) {
			return columnNames[col];
		}

		public Object getValueAt(int row, int col) {
			int currentTree = figTreePanel.getTreeViewer().getCurrentTreeIndex();
			Taxon taxon = runResult.getPrunedTaxa().get(currentTree).get(row);

			if (col == 0) return taxon.getName();
			//if (col == 1) return layer.getType().toString();

			return "";
		}

		public boolean isCellEditable(int row, int col) {
			return false;
		}

		public Class getColumnClass(int c) {
			if (getRowCount() == 0) {
				return Object.class;
			}
			return getValueAt(0, c).getClass();
		}
		
		public void setRunResult(RunResult rr) {
			runResult = rr;
		}
	}
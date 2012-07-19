package mastodon;

import java.text.DecimalFormat;

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
		
		
		final String[] columnNames = {"#", "pruned", "Taxon name", "Pruning frequency"};

		public int getColumnCount() {
			return columnNames.length;
		}

		public int getRowCount() {
			if(runResult == null) {
				return 0;
			}
//			int currentTree = figTreePanel.getTreeViewer().getCurrentTreeIndex();
//			return runResult.getPrunedTaxa().get(currentTree).size();
			
			return runResult.getBts().getAllTaxa().size();
		}

		public String getColumnName(int col) {
			return columnNames[col];
		}

		public Object getValueAt(int row, int col) {
			int currentTree = figTreePanel.getTreeViewer().getCurrentTreeIndex();
			//Taxon taxon = runResult.getPrunedTaxa().get(currentTree).get(row);
			Taxon taxon = (Taxon) runResult.getBts().getAllTaxa().toArray()[row];
			DecimalFormat twoDForm = new DecimalFormat("#.##");

			if (col == 0) return row + 1;
			if (col == 1) {
				if (runResult.getPrunedTaxa().get(currentTree).contains(taxon)) {
					return "Ã";
				} 
				return "";
			}
			if (col == 2) return taxon.getName();
			if (col == 3) return Double.valueOf(twoDForm.format(100 * runResult.getPruningFreq().get(taxon))) + "%";
			

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
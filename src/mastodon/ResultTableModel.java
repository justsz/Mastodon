/* Copyright (C) 2012 Justs Zarins
 *
 *This file is part of MASTodon.
 *
 *MASTodon is free software: you can redistribute it and/or modify
 *it under the terms of the GNU Lesser General Public License as
 *published by the Free Software Foundation, either version 3
 *of the License, or (at your option) any later version.
 *
 *MASTodon is distributed in the hope that it will be useful,
 *but WITHOUT ANY WARRANTY; without even the implied warranty of
 *MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *GNU Lesser General Public License for more details.
 *
 *You should have received a copy of the GNU Lesser General Public License
 *along with this program.  If not, see http://www.gnu.org/licenses/.
 */

package mastodon;

import java.text.DecimalFormat;
import java.util.Set;

import javax.swing.table.AbstractTableModel;

import mastodon.core.RunResult;

import jebl.evolution.taxa.Taxon;

/**
 * This is a Model for the result table. Given a run result, it knows how to format the data and put it in table form.
 * @author justs
 */
class ResultTableModel extends AbstractTableModel {
	private RunResult runResult;
	private FigTreePanel figTreePanel;

	/**
	 * Sets up the Model with an initial runResult. A null runResult will display an empty table.
	 * @param runResult - initial runResult to display
	 * @param figTreePanel - panel that displays the trees
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
		return runResult.getBts().getAllTaxa().size();
	}

	public String getColumnName(int col) {
		return columnNames[col];
	}

	public Object getValueAt(int row, int col) {
		int currentTree = figTreePanel.getTreeViewer().getCurrentTreeIndex();

		Taxon taxon = (Taxon) runResult.getBts().getAllTaxa().toArray()[row];

		DecimalFormat twoDForm = new DecimalFormat("#.##");

		if (col == 0) return row + 1;
		//a tick to indicate a pruned taxon
		if (col == 1) {
			if (runResult.getPrunedTaxa().size() > 0) {
				//tick if the taxon is in the pruned taxa set or if the taxon has been removed from the tree via "commit"
				if (runResult.getPrunedTaxa().get(currentTree).contains(taxon) || runResult.getPrunedMapTrees().get(0).getNode(taxon) == null) {
					return "Ã";
				} 
			}
			return "";
		}
		if (col == 2) return taxon.getName();
		//taxon pruning frequency
		if (col == 3) return Double.valueOf(twoDForm.format(runResult.getPruningFreq().get(taxon)));

		return "";
	}

	/**
	 * Check if the taxon has already been pruned.
	 * @param row - row of interest
	 * @return is the taxon pruned?
	 */
	public boolean isPruned(int row) {
		int currentTree = figTreePanel.getTreeViewer().getCurrentTreeIndex();
		Taxon taxon = (Taxon) runResult.getBts().getAllTaxa().toArray()[row];
		Set<Taxon> unRemovedTaxa = runResult.getPrunedMapTrees().get(currentTree).getTaxa();

		return !unRemovedTaxa.contains(taxon);		
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

	/**
	 * Set a runResult to display in the table.
	 * @param rr - the runResult to set
	 */
	public void setRunResult(RunResult rr) {
		runResult = rr;
	}
}
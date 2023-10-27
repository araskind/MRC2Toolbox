/*******************************************************************************
 *
 * (C) Copyright 2018-2020 MRC2 (http://mrc2.umich.edu).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 * Alexander Raskind (araskind@med.umich.edu)
 *
 ******************************************************************************/

package edu.umich.med.mrc2.datoolbox.gui.cpddatabase.curator.struct;

import java.awt.Component;

import javax.swing.ListSelectionModel;
import javax.swing.table.TableCellRenderer;

import edu.umich.med.mrc2.datoolbox.data.CompoundIdentity;
import edu.umich.med.mrc2.datoolbox.data.CompoundIdentityCluster;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.ChemicalStructureRenderer;

public class CompoundStructureTable extends BasicTable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6719085533971443823L;
	private CompoundIdentityCluster activeCluster;

	public CompoundStructureTable() {
		super();
		model = new CompoundStructureTableModel();
		setModel(model);

		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		setRowSorter(null);

		columnModel.getColumnById(CompoundStructureTableModel.STRUCTURE_COLUMN)
			.setCellRenderer(new ChemicalStructureRenderer());

		finalizeLayout();
	}
	
	public void setModelFromCompoundIdentityCluster(CompoundIdentityCluster cluster) {
		((CompoundStructureTableModel)model).setModelFromCompoundIdentityCluster(cluster);
		activeCluster = cluster;
	}
	
	public CompoundIdentity getSelectedCompoundId() {

		int row = getSelectedRow();
		if(row == -1)
			return null;

		return ((CompoundIdentity) model.getValueAt(
				convertRowIndexToModel(row), 
				model.getColumnIndex(CompoundStructureTableModel.STRUCTURE_COLUMN)));
	}
	
	@Override
	public synchronized void clearTable() {
		
		activeCluster = null;
		super.clearTable();
	}

	public CompoundIdentityCluster getActiveCluster() {
		return activeCluster;
	}
	
	@Override
	public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {

		Component returnComp = null;
		try {
			returnComp = super.prepareRenderer(renderer, row, column);
		} catch (Exception e) {
			//e.printStackTrace();
		}
		if (returnComp != null)
			returnComp.setBackground(WHITE_COLOR);
		
		return returnComp;
	}

	public void selectStructureForIdentity(CompoundIdentity id) {

		clearSelection();
		int idCol = getColumnIndex(CompoundStructureTableModel.STRUCTURE_COLUMN);
		for(int i=0; i<getRowCount(); i++) {
			if(getValueAt(i, idCol).equals(id)) {
				setRowSelectionInterval(i, i);
				scrollToSelected();
				break;
			}
		}		
	}
}

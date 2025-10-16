/*******************************************************************************
 *
 * (C) Copyright 2018-2025 MRC2 (http://mrc2.umich.edu).
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

package edu.umich.med.mrc2.datoolbox.gui.cpdcol.mplex;

import java.util.ArrayList;
import java.util.Collection;

import javax.swing.ListSelectionModel;

import edu.umich.med.mrc2.datoolbox.data.cpdcoll.CompoundMultiplexMixture;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.AutoChoices;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.TableFilterHeader;

public class CompoundMultiplexListingTable extends BasicTable {

	/**
	 *
	 */
	private static final long serialVersionUID = -3817580957098149548L;

	public CompoundMultiplexListingTable() {

		super();

		model = new CompoundMultiplexListingTableModel();
		setModel(model);
		setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		thf = new TableFilterHeader(this, AutoChoices.ENABLED);
		finalizeLayout();
	}

	public void setTableModelFromCompoundMultiplexMixtureCollection(
			Collection<CompoundMultiplexMixture> multiplexes)  {

		thf.setTable(null);
		((CompoundMultiplexListingTableModel)model).
				setTableModelFromCompoundMultiplexMixtureCollection(multiplexes);
		thf.setTable(this);
		adjustColumns();
	}

	public Collection<CompoundMultiplexMixture> getSelectedMultiplexMixtures() {

		Collection<CompoundMultiplexMixture>selectedMultiplexMixtures = 
				new ArrayList<CompoundMultiplexMixture>();
		int[] selectedRows = getSelectedRows();
		if(selectedRows.length == 0)
			return selectedMultiplexMixtures;
		
		int idCol = model.getColumnIndex(CompoundMultiplexListingTableModel.NAME_COLUMN);
		for(int i : selectedRows)
			selectedMultiplexMixtures.add(
				((CompoundMultiplexMixture) model.getValueAt(convertRowIndexToModel(i), idCol)));

		return selectedMultiplexMixtures;
	}
	
	public CompoundMultiplexMixture getSelectedMultiplexMixture() {
		
		int row = getSelectedRow();
		if(row == -1)
			return null;
		else
			return ((CompoundMultiplexMixture) model.getValueAt(
					convertRowIndexToModel(row), 
					model.getColumnIndex(CompoundMultiplexListingTableModel.NAME_COLUMN)));		
	}
}




















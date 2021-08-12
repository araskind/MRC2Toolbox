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

package edu.umich.med.mrc2.datoolbox.gui.library.feditor;

import javax.swing.ListSelectionModel;
import javax.swing.table.TableRowSorter;

import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.TandemMassSpectrum;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;

public class FeatuteTandemMsListingTable extends BasicTable {

	/**
	 *
	 */
	private static final long serialVersionUID = 5088486364694169431L;
	private FeatuteTandemMsListingTableModel model;

	public FeatuteTandemMsListingTable() {

		super();
		model = new FeatuteTandemMsListingTableModel();
		setModel(model);
		rowSorter = new TableRowSorter<FeatuteTandemMsListingTableModel>(model);
		setRowSorter(rowSorter);
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		finalizeLayout();
	}

	public void setTableModelFromFeature(MsFeature feature) {

		model.setTableModelFromFeature(feature);
		tca.adjustColumns();
	}

	public TandemMassSpectrum getSelectedMsMs() {
		
		int row = getSelectedRow();
		if(row == -1)
			return null;

		return (TandemMassSpectrum) model.getValueAt(convertRowIndexToModel(row), 
				model.getColumnIndex(FeatuteTandemMsListingTableModel.MSMS_COLUMN));
	}
}

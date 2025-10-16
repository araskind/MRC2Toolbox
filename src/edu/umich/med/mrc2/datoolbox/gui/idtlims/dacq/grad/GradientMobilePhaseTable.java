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

package edu.umich.med.mrc2.datoolbox.gui.idtlims.dacq.grad;

import javax.swing.ListSelectionModel;

import edu.umich.med.mrc2.datoolbox.data.lims.MobilePhase;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.WordWrapCellRenderer;

public class GradientMobilePhaseTable extends BasicTable {

	/**
	 *
	 */
	private static final long serialVersionUID = -1405921543482090501L;

	public GradientMobilePhaseTable() {
		super();
		setAutoCreateRowSorter(false);
		model =  new GradientMobilePhaseTableModel();
		setModel(model);	
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		columnModel.getColumnById(GradientMobilePhaseTableModel.MOBILE_PHASE_DESCRIPTION_COLUMN)
			.setCellRenderer(new WordWrapCellRenderer());
		columnModel.getColumnById(GradientMobilePhaseTableModel.CHANNEL).setWidth(80);
		columnModel.getColumnById(GradientMobilePhaseTableModel.CHANNEL).setMaxWidth(80);
		fixedWidthColumns.add(model.getColumnIndex(GradientMobilePhaseTableModel.CHANNEL));		
		finalizeLayout();
		setRowSorter(null);
		getTableHeader().setEnabled(false);
		setTableModelFromMobilePhaseArray(null);
	}

	public void setTableModelFromMobilePhaseArray(MobilePhase[]phases) {

		((GradientMobilePhaseTableModel)model).setTableModelFromMobilePhaseArray(phases);
		adjustColumns();
	}

	public MobilePhase getSelectedMobilePhase() {

		int row = getSelectedRow();
		if(row == -1)
			return null;

		return (MobilePhase) model.getValueAt(convertRowIndexToModel(row),
				model.getColumnIndex(GradientMobilePhaseTableModel.MOBILE_PHASE_DESCRIPTION_COLUMN));
	}
}

















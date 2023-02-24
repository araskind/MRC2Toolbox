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

package edu.umich.med.mrc2.datoolbox.gui.mptrack.ref.sampletype;

import java.util.Collection;

import javax.swing.ListSelectionModel;
import javax.swing.table.TableRowSorter;

import edu.umich.med.mrc2.datoolbox.data.motrpac.MotrpacSampleType;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.AutoChoices;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.TableFilterHeader;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.WordWrapCellRenderer;

public class SampleTypeTable extends BasicTable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1472881417476433802L;
	private SampleTypeTableModel model;

	public SampleTypeTable() {
		super();
		model = new SampleTypeTableModel();
		setModel(model);
		rowSorter = new TableRowSorter<SampleTypeTableModel>(model);
		setRowSorter(rowSorter);
		
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		getTableHeader().setReorderingAllowed(false);
		
		columnModel.getColumnById(SampleTypeTableModel.SAMPLE_DESCRIPTION_COLUMN)
			.setCellRenderer(new WordWrapCellRenderer());
		thf = new TableFilterHeader(this, AutoChoices.ENABLED);
		finalizeLayout();
	}

	public void setTableModelFromSamples(Collection<MotrpacSampleType>samples) {
		thf.setTable(null);
		model.setTableModelFromSamples(samples);
		thf.setTable(this);
		tca.adjustColumns();
	}

	public MotrpacSampleType getSelectedSample(){

		int row = getSelectedRow();
		if(row == -1)
			return null;

		return (MotrpacSampleType) model.getValueAt(convertRowIndexToModel(row),
				model.getColumnIndex(SampleTypeTableModel.SAMPLE_TYPE_COLUMN));
	}

	public void selectSample(MotrpacSampleType sample) {

		int colIdx = model.getColumnIndex(SampleTypeTableModel.SAMPLE_TYPE_COLUMN);
		for(int i=0; i<getRowCount(); i++) {

			if(model.getValueAt(convertRowIndexToModel(i), colIdx).equals(sample)){
				setRowSelectionInterval(i, i);
				this.scrollToSelected();
				return;
			}
		}
	}
}

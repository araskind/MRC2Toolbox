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

package edu.umich.med.mrc2.datoolbox.gui.refsamples;

import javax.swing.ListSelectionModel;
import javax.swing.table.TableRowSorter;

import edu.umich.med.mrc2.datoolbox.data.ExperimentalSample;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.AutoChoices;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.TableFilterHeader;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.ExperimentalSampleRenderer;

public class ReferenceSampleTable extends BasicTable {

	/**
	 *
	 */
	private static final long serialVersionUID = 5200794508214563011L;
	private ReferenceSampleTableModel model;

	public ReferenceSampleTable() {

		super();
		model =  new ReferenceSampleTableModel();
		setModel(model);
		rowSorter = new TableRowSorter<ReferenceSampleTableModel>(model);
		setRowSorter(rowSorter);
		
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		columnModel.getColumnById(ReferenceSampleTableModel.SAMPLE_ID_COLUMN).
			setCellRenderer(new ExperimentalSampleRenderer(SortProperty.ID));

		thf = new TableFilterHeader(this, AutoChoices.ENABLED);
		finalizeLayout();
	}

	public void loadReferenceSamples() {
		model.loadReferenceSamples();
		tca.adjustColumns();
	}

	public ExperimentalSample getSelectedSample() {

		int row = getSelectedRow();
		if(row == -1)
			return null;

		return (ExperimentalSample) model.getValueAt(
				convertRowIndexToModel(row),
				model.getColumnIndex(ReferenceSampleTableModel.SAMPLE_ID_COLUMN));
	}

	public void selectSampleRow(ExperimentalSample sample) {

		int sampleRow = -1;
		int idColumn = getColumnIndex(ReferenceSampleTableModel.SAMPLE_ID_COLUMN);
		for(int i=0; i<getRowCount(); i++) {

			if(model.getValueAt(convertRowIndexToModel(i), idColumn).equals(sample)) {
				sampleRow = i;
				break;
			}
		}
		if(sampleRow > -1 && sampleRow <= getRowCount()) {

			setRowSelectionInterval(sampleRow, sampleRow);
			scrollToSelected();
		}
	}
}













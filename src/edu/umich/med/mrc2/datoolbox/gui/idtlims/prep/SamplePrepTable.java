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

package edu.umich.med.mrc2.datoolbox.gui.idtlims.prep;

import java.util.Collection;

import javax.swing.ListSelectionModel;
import javax.swing.table.TableRowSorter;

import edu.umich.med.mrc2.datoolbox.data.compare.LIMSSamplePreparationComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.format.LIMSSamplePreparationFormat;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSSamplePreparation;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.AutoChoices;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.TableFilterHeader;

public class SamplePrepTable extends BasicTable {

	/**
	 *
	 */
	private static final long serialVersionUID = 5111265849253755884L;

	public SamplePrepTable() {
		super();
		model = new SamplePrepTableModel();
		setModel(model);
		rowSorter = new TableRowSorter<SamplePrepTableModel>((SamplePrepTableModel)model);
		setRowSorter(rowSorter);
		rowSorter.setComparator(model.getColumnIndex(SamplePrepTableModel.SAMPLE_PREP_COLUMN),
				new LIMSSamplePreparationComparator(SortProperty.Name));
		
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		thf = new TableFilterHeader(this, AutoChoices.ENABLED);
		thf.getParserModel().setFormat(LIMSSamplePreparation.class,
				new LIMSSamplePreparationFormat(SortProperty.Name));
		thf.getParserModel().setComparator(LIMSSamplePreparation.class,
				new LIMSSamplePreparationComparator(SortProperty.Name));

		finalizeLayout();
	}

	public void setTableModelFromPreps(Collection<LIMSSamplePreparation>preps) {
		thf.setTable(null);
		((SamplePrepTableModel)model).setTableModelFromPreps(preps);
		thf.setTable(this);
		adjustColumns();
	}

	public LIMSSamplePreparation getSelectedPrep() {

		int row = getSelectedRow();
		if(row == -1)
			return null;

		return (LIMSSamplePreparation)model.getValueAt(convertRowIndexToModel(row),
				model.getColumnIndex(SamplePrepTableModel.SAMPLE_PREP_COLUMN));
	}
	
	public void selectSamplePrep(LIMSSamplePreparation prep) {
		
		int col = getColumnIndex(SamplePrepTableModel.SAMPLE_PREP_COLUMN);
		//	TODO handle this through the model if necessary
		if(col == -1)
			return;
		
		for(int i=0; i<getRowCount(); i++) {
			
			if(getValueAt(i, col).equals(prep)) {
				setRowSelectionInterval(i, i);
				scrollToSelected();
				return;
			}
		}
	}

	public void updatePrepData(LIMSSamplePreparation prep) {
		((SamplePrepTableModel)model).updatePrepData(prep);
	}	
}














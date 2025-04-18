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

package edu.umich.med.mrc2.datoolbox.gui.datexp.hm;

import java.util.ArrayList;
import java.util.Collection;

import javax.swing.ListSelectionModel;
import javax.swing.table.TableRowSorter;

import edu.umich.med.mrc2.datoolbox.data.ExperimentalSample;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.AutoChoices;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.TableFilterHeader;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.ExperimentalSampleRenderer;
import edu.umich.med.mrc2.datoolbox.project.Project;

public class SampleGroupTable extends BasicTable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	public SampleGroupTable() {
		super();
		this.model = new SampleGroupTableModel();
		setModel(model);
		rowSorter = new TableRowSorter<SampleGroupTableModel>(
				(SampleGroupTableModel)model);
		setRowSorter(rowSorter);
		
		setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		columnModel.getColumnById(SampleGroupTableModel.SAMPLE_ID_COLUMN).
			setCellRenderer(new ExperimentalSampleRenderer(SortProperty.ID));

		thf = new TableFilterHeader(this, AutoChoices.ENABLED);
		finalizeLayout();
	}
	
	public void loadSampleTypes(Project experiment) {
		((SampleGroupTableModel)model).loadSampleTypes(experiment);
	}
	
	public Collection<ExperimentalSample> getSelectedSamples() {
		
		Collection<ExperimentalSample>selected = new ArrayList<ExperimentalSample>();		
		int selectedCol = model.getColumnIndex(SampleGroupTableModel.INCLUDE_COLUMN);
		int idCol = model.getColumnIndex(SampleGroupTableModel.SAMPLE_ID_COLUMN);		
		
		for(int row=0; row<model.getRowCount(); row++) {
			
			if((Boolean)model.getValueAt(row, selectedCol))
				selected.add((ExperimentalSample) model.getValueAt(row, idCol));	
		}
		return selected;
	}
}

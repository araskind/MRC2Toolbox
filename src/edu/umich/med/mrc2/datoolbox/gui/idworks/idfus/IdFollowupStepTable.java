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

package edu.umich.med.mrc2.datoolbox.gui.idworks.idfus;

import java.util.ArrayList;
import java.util.Collection;

import javax.swing.ListSelectionModel;
import javax.swing.table.TableRowSorter;

import edu.umich.med.mrc2.datoolbox.data.MSFeatureIdentificationFollowupStep;
import edu.umich.med.mrc2.datoolbox.data.compare.IdFollowupStepComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;

public class IdFollowupStepTable extends BasicTable {

	/**
	 *
	 */
	private static final long serialVersionUID = -1405921543482090501L;
	private IdFollowupStepTableModel model;

	public IdFollowupStepTable() {
		super();
		model =  new IdFollowupStepTableModel();
		setModel(model);
		rowSorter = new TableRowSorter<IdFollowupStepTableModel>(model);
		setRowSorter(rowSorter);
		rowSorter.setComparator(model.getColumnIndex(IdFollowupStepTableModel.STEP_COLUMN),
				new IdFollowupStepComparator(SortProperty.Name));
		
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		finalizeLayout();
	}

	public void setTableModelFromFollowupStepList(
			Collection<MSFeatureIdentificationFollowupStep>followupStepList) {
		model.setTableModelFromFollowupStepList(followupStepList);
		tca.adjustColumns();
	}

	public MSFeatureIdentificationFollowupStep getSelectedFollowupStep() {

		if(getSelectedRow() == -1)
			return null;

		return (MSFeatureIdentificationFollowupStep) getValueAt(getSelectedRow(),
				getColumnIndex(IdFollowupStepTableModel.STEP_COLUMN));
	}
	
	public Collection<MSFeatureIdentificationFollowupStep> getSelectedFollowupSteps() {

		Collection<MSFeatureIdentificationFollowupStep>selected = 
				new ArrayList<MSFeatureIdentificationFollowupStep>();
		int col = model.getColumnIndex(IdFollowupStepTableModel.STEP_COLUMN);
		for(int i : getSelectedRows())
			selected.add((MSFeatureIdentificationFollowupStep) model.getValueAt(convertRowIndexToModel(i),col));

		return selected;
	}
	
	public void selectFollowupSteps(
			Collection<MSFeatureIdentificationFollowupStep>followupStepList) {
		
		int col = model.getColumnIndex(IdFollowupStepTableModel.STEP_COLUMN);		
		for(int i=0; i<getRowCount(); i++) {
			
			if(followupStepList.contains(model.getValueAt(convertRowIndexToModel(i),col)))
				addRowSelectionInterval(i, i);
		}
	}
}

















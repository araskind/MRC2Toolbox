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

package edu.umich.med.mrc2.datoolbox.gui.mptrack.study.edit;

import java.util.Collection;

import javax.swing.ListSelectionModel;
import javax.swing.table.TableRowSorter;

import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.motrpac.MoTrPACStudy;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.MotrpacStudyRenderer;

public class StudyTable extends BasicTable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1472881417476433802L;
	private StudyTableModel model;

	public StudyTable() {
		super();
		model = new StudyTableModel();
		setModel(model);
		rowSorter = new TableRowSorter<StudyTableModel>(model);
		setRowSorter(rowSorter);
		
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		getTableHeader().setReorderingAllowed(false);
		
		columnModel.getColumnById(StudyTableModel.STUDY_COLUMN)
			.setCellRenderer(new MotrpacStudyRenderer(SortProperty.ID));

		finalizeLayout();
	}

	public void setTableModelFromStudies(Collection<MoTrPACStudy>studies) {

		model.setTableModelFromStudies(studies);
		tca.adjustColumns();
	}

	public MoTrPACStudy getSelectedStudy(){

		int row = getSelectedRow();
		if(row == -1)
			return null;

		return (MoTrPACStudy) model.getValueAt(convertRowIndexToModel(row),
				model.getColumnIndex(StudyTableModel.STUDY_COLUMN));
	}

	public void selectStudy(MoTrPACStudy study) {

		int colIdx = model.getColumnIndex(StudyTableModel.STUDY_COLUMN);
		for(int i=0; i<getRowCount(); i++) {

			if(model.getValueAt(convertRowIndexToModel(i), colIdx).equals(study)){
				setRowSelectionInterval(i, i);
				this.scrollToSelected();
				return;
			}
		}
	}
}

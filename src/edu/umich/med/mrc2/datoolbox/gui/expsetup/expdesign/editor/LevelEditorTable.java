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

package edu.umich.med.mrc2.datoolbox.gui.expsetup.expdesign.editor;

import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;

import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignFactor;
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignLevel;
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignSubset;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;

public class LevelEditorTable extends BasicTable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private ExperimentDesignSubset activeDesignSubset;
	private ExperimentDesignFactor activeFactor;

	public LevelEditorTable() {

		super();
		model = new LevelEditorTableModel();
		setModel(model);
		
		getTableHeader().setReorderingAllowed(false);
		setRowSorter(null);

		putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		finalizeLayout();
	}

	public void setTableModelFromDesignSubsetFactor(ExperimentDesignSubset designSubset, ExperimentDesignFactor factor) {

		activeDesignSubset = designSubset;
		activeFactor = factor;
		((LevelEditorTableModel)model).setTableModelFromDesignSubsetFactor(designSubset, factor);
		adjustColumns();
	}

	//	TODO replace with table model listener
	@Override
	public void editingStopped(ChangeEvent event) {

		super.editingStopped(event);

		ExperimentDesignLevel selectedLevel = getSelectedLevel();
		if(selectedLevel == null)
			return;

		int row = convertRowIndexToModel(getSelectedRow());
		boolean isEnabled = (boolean) model.getValueAt(row,
				model.getColumnIndex(LevelEditorTableModel.ACTIVE_COLUMN));

		if(isEnabled)
			activeDesignSubset.addLevel(selectedLevel,true);
		else {
			if(activeDesignSubset.getOrderedDesign().get(activeFactor).length > 1)
				activeDesignSubset.removeLevel(selectedLevel,true);
			else {
				model.setValueAt(true, row,
						model.getColumnIndex(LevelEditorTableModel.ACTIVE_COLUMN));
				MessageDialog.showErrorMsg("Disabling the only remaining level is not allowed!");
				return;
			}
		}
		((LevelEditorTableModel)model).setTableModelFromDesignSubsetFactor(activeDesignSubset, activeFactor);
		highlightLevel(selectedLevel);		
	}

	public boolean isSelectedLevelActive() {

		int row = getSelectedRow();
		if(row == -1)
			return false;

		return (boolean) model.getValueAt(convertRowIndexToModel(row),
					model.getColumnIndex(LevelEditorTableModel.ACTIVE_COLUMN));
	}

	public ExperimentDesignLevel getSelectedLevel() {

		int row = getSelectedRow();
		if(row == -1)
			return null;

		return (ExperimentDesignLevel) model.getValueAt(convertRowIndexToModel(row),
				model.getColumnIndex(LevelEditorTableModel.LEVEL_COLUMN));
	}

	public void setEditingAllowed(boolean allowEdit) {
		((LevelEditorTableModel)model).setEditingAllowed(allowEdit);
	}

	public void highlightLevel(ExperimentDesignLevel selectedLevel) {

		int levelColumn = model.getColumnIndex(LevelEditorTableModel.LEVEL_COLUMN);
		for(int i=0; i<getRowCount(); i++) {

			ExperimentDesignLevel level = 
					(ExperimentDesignLevel) model.getValueAt(convertRowIndexToModel(i), levelColumn);
			if(level.equals(selectedLevel)) {
				setRowSelectionInterval(i, i);
				break;
			}
		}
	}
}

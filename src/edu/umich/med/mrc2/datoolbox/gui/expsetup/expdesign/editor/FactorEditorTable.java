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
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignSubset;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;

public class FactorEditorTable extends BasicTable {

	/**
	 *
	 */
	private static final long serialVersionUID = 6616085176709256386L;
	private ExperimentDesignSubset activeDesignSubset;

	public FactorEditorTable() {

		super();
		model = new FactorEditorTableModel();
		setModel(model);
		
		getTableHeader().setReorderingAllowed(false);
		setRowSorter(null);

		putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		finalizeLayout();
	}

	public void setEditingAllowed(boolean allowEdit) {
		((FactorEditorTableModel)model).setEditingAllowed(allowEdit);
	}

	public void setTableModelFromDesignSubset(ExperimentDesignSubset designSubset) {

		activeDesignSubset = designSubset;
		((FactorEditorTableModel)model).setTableModelFromDesignSubset(activeDesignSubset);
		adjustColumns();
	}

	public ExperimentDesignFactor getSelectedFactor() {
		
		int row = getSelectedRow();
		if(row == -1)
			return null;

		return (ExperimentDesignFactor) model.getValueAt(convertRowIndexToModel(row),
				model.getColumnIndex(FactorEditorTableModel.FACTOR_COLUMN));
	}

	public void highlightFactor(ExperimentDesignFactor selectedFactor) {

		int factorColumn = model.getColumnIndex(FactorEditorTableModel.FACTOR_COLUMN);
		for(int i=0; i<getRowCount(); i++) {

			ExperimentDesignFactor factor =
					(ExperimentDesignFactor) model.getValueAt(convertRowIndexToModel(i), factorColumn);
			if(factor.equals(selectedFactor)) {
				setRowSelectionInterval(i, i);
				break;
			}
		}
	}

	//	TODO replace with table model listener
	@Override
	public void editingStopped(ChangeEvent event) {

		super.editingStopped(event);

		ExperimentDesignFactor selectedFactor = getSelectedFactor();

		if(selectedFactor != null) {

			((FactorEditorTableModel)model).setTableModelFromDesignSubset(activeDesignSubset);
			highlightFactor(selectedFactor);
		}
	}

	public ExperimentDesignSubset getActiveDesignSubset() {
		return activeDesignSubset;
	}
}

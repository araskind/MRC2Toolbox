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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignFactor;
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignSubset;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTableModel;
import edu.umich.med.mrc2.datoolbox.gui.tables.ColumnContext;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.ReferenceSamplesManager;

public class FactorEditorTableModel extends BasicTableModel {

	private static final long serialVersionUID = 63624132728676065L;
	public static final String FACTOR_COLUMN = "Factor";
	private boolean allowEdit;

	public FactorEditorTableModel() {
		super();
		columnArray = new ColumnContext[] {
			new ColumnContext(FACTOR_COLUMN, ExperimentDesignFactor.class, false)
		};
	}

	@Override
	public boolean isCellEditable(int row, int col) {

		if(!allowEdit)
			return false;
		else
			return columnArray[col].isEditable;
	}

	public void setTableModelFromDesignSubset(ExperimentDesignSubset designSubset) {

		setRowCount(0);
		allowEdit = true;
		Set<ExperimentDesignFactor> activeSet = designSubset.getOrderedDesign().keySet();

		List<Object[]>rowData = new ArrayList<Object[]>();
		
		//	Add active factors
		activeSet.stream().
			filter(f -> !f.equals(ReferenceSamplesManager.getSampleControlTypeFactor())).
			forEach(f -> rowData.add(new Object[] { f }));

		//	Add inactive factors
		MRC2ToolBoxCore.getActiveMetabolomicsExperiment().getExperimentDesign().getFactors().stream().
			filter(f -> !activeSet.contains(f)).
			filter(f -> !f.equals(ReferenceSamplesManager.getSampleControlTypeFactor())).
			forEach(f -> rowData.add(new Object[] { f }));
		
		addRows(rowData);
	}

	public void setEditingAllowed(boolean allowEdit) {
		this.allowEdit = allowEdit;
	}
}




























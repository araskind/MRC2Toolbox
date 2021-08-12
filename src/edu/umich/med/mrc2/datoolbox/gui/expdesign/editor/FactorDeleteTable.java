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

package edu.umich.med.mrc2.datoolbox.gui.expdesign.editor;

import java.util.ArrayList;
import java.util.List;

import javax.swing.ListSelectionModel;

import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignFactor;
import edu.umich.med.mrc2.datoolbox.gui.projectsetup.expdesign.editor.FactorEditorTable;

public class FactorDeleteTable extends FactorEditorTable {

	/**
	 *
	 */
	private static final long serialVersionUID = 2016928238512487145L;

	public FactorDeleteTable() {
		super();
		getColumnModel().getColumn(0).setHeaderValue("Select");
		getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
	}

	public List<ExperimentDesignFactor> getSelectedFactors() {

		ArrayList<ExperimentDesignFactor>marked = new ArrayList<ExperimentDesignFactor>();
		for(int i : getSelectedRows())
			marked.add((ExperimentDesignFactor) getValueAt(i, 0));

		return marked;
	}
}

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

package edu.umich.med.mrc2.datoolbox.gui.expsetup.expdesign;

import javax.swing.ListSelectionModel;
import javax.swing.table.TableRowSorter;

import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignSubset;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.WordWrapCellRenderer;

public class ExperimentalGroupsTable extends BasicTable {

	/**
	 *
	 */
	private static final long serialVersionUID = 1574891455989936345L;

	public ExperimentalGroupsTable() {

		super();
		model = new ExperimentalGroupsTableModel();
		setModel(model);
		rowSorter = new TableRowSorter<ExperimentalGroupsTableModel>(
				(ExperimentalGroupsTableModel)model);
		setRowSorter(rowSorter);
		
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		longTextRenderer = new WordWrapCellRenderer();
		columnModel.getColumnById(ExperimentalGroupsTableModel.LEVELS_COLUMN)
				.setCellRenderer(longTextRenderer);
		finalizeLayout();
	}

	public void setModelFromDesignSubset(ExperimentDesignSubset activeDesignSubset) {

		((ExperimentalGroupsTableModel)model).setModelFromDesignSubset(activeDesignSubset);
		adjustColumns();
	}
}

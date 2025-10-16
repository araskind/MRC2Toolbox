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

package edu.umich.med.mrc2.datoolbox.gui.mgf;

import javax.swing.table.TableRowSorter;

import edu.umich.med.mrc2.datoolbox.data.MsMsCluster;
import edu.umich.med.mrc2.datoolbox.data.SimpleMsMs;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;

public class MsMsTable extends BasicTable {

	/**
	 *
	 */
	private static final long serialVersionUID = -4165675452825468215L;

	public MsMsTable() {

		model = new MsMsTableModel();
		rowSorter = new TableRowSorter<MsMsTableModel>((MsMsTableModel)model);
		setRowSorter(rowSorter);
		setModel(model);
		finalizeLayout();
	}

	public void setTableModelFromMsMsCluster(MsMsCluster featureCluster) {

		((MsMsTableModel)model).setTableModelFromMsMsCluster(featureCluster);
		adjustColumns();
	}

	public void setTableModelFromSimpleMsMs(SimpleMsMs msms) {

		((MsMsTableModel)model).setTableModelFromSimpleMsMs(msms);
		adjustColumns();
	}
}

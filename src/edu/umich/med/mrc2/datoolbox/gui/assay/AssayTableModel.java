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

package edu.umich.med.mrc2.datoolbox.gui.assay;

import java.util.Collection;

import edu.umich.med.mrc2.datoolbox.data.Assay;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTableModel;
import edu.umich.med.mrc2.datoolbox.gui.tables.ColumnContext;

public class AssayTableModel extends BasicTableModel {

	/**
	 *
	 */
	private static final long serialVersionUID = 4183399206967466908L;

	public static final String ASSAY_COLUMN = "Assay name";
	public static final String ASSAY_ID_COLUMN = "Assay ID";

	public AssayTableModel() {
		super();
		columnArray = new ColumnContext[] {
			new ColumnContext(ASSAY_ID_COLUMN, String.class, false),
			new ColumnContext(ASSAY_COLUMN, Assay.class, false),
		};
	}

	public void setTableModelFromAssayCollection(Collection<Assay> assayCollection) {

		setRowCount(0);

		for(Assay a : assayCollection) {

			Object[] obj = {
					a.getId(),
					a
				};
			super.addRow(obj);
		}
	}
}






























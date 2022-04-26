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

package edu.umich.med.mrc2.datoolbox.gui.rawdata.project.wiz.methods;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import edu.umich.med.mrc2.datoolbox.data.lims.AnalysisMethod;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTableModel;
import edu.umich.med.mrc2.datoolbox.gui.tables.ColumnContext;

public class AcquisitionMethodTableModel extends BasicTableModel {

	/**
	 *
	 */
	private static final long serialVersionUID = 3417856068405445036L;

	public static final String METHOD_ID_COLUMN = "DB ID";
	public static final String METHOD_NAME_COLUMN = "Method name";

	public AcquisitionMethodTableModel() {

		super();
		columnArray = new ColumnContext[] {
			new ColumnContext(METHOD_ID_COLUMN, AnalysisMethod.class, false),
			new ColumnContext(METHOD_NAME_COLUMN, String.class, false),
		};
	}

	public void setTableModelFromMethods(Map<String,AnalysisMethod>methodFilesMap) {

		setRowCount(0);
		if(methodFilesMap.isEmpty())
			return;

		for (Entry<String,AnalysisMethod> methodEntry : methodFilesMap.entrySet()) {

			Object[] obj = {
					methodEntry.getValue(),
					methodEntry.getKey(),
			};
			super.addRow(obj);
		}
	}
	
	public void setTableModelFromMethodCollection(Collection<? extends AnalysisMethod>methods) {

		setRowCount(0);
		if(methods.isEmpty())
			return;

		for (AnalysisMethod method : methods) {

			Object[] obj = {
					method,
					method.getName(),
			};
			super.addRow(obj);
		}
	}
}















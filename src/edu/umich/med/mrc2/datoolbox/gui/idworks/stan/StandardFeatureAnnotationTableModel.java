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

package edu.umich.med.mrc2.datoolbox.gui.idworks.stan;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import edu.umich.med.mrc2.datoolbox.data.StandardFeatureAnnotation;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTableModel;
import edu.umich.med.mrc2.datoolbox.gui.tables.ColumnContext;

public class StandardFeatureAnnotationTableModel extends BasicTableModel {


	/**
	 * 
	 */
	private static final long serialVersionUID = 5912536371038326183L;
	public static final String CODE_COLUMN = "Code";
	public static final String TEXT_COLUMN = "Annotation";

	public StandardFeatureAnnotationTableModel() {

		super();
		columnArray = new ColumnContext[] {
			new ColumnContext(CODE_COLUMN, "Standard annotation code", String.class, false),
			new ColumnContext(TEXT_COLUMN, "Standard annotation text", StandardFeatureAnnotation.class, false),
		};
	}

	public void setTableModelFromStandardFeatureAnnotationList(
			Collection<StandardFeatureAnnotation> annotationList) {

		setRowCount(0);
		if(annotationList == null || annotationList.isEmpty())
			return;

		List<Object[]>rowData = new ArrayList<Object[]>();
		for (StandardFeatureAnnotation annotation : annotationList) {

			Object[] obj = {
				annotation,
				annotation.getText(),
			};
			rowData.add(obj);
		}
		if(!rowData.isEmpty())
			addRows(rowData);
	}
}















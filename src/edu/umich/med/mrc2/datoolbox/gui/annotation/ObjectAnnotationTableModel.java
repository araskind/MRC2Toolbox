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

package edu.umich.med.mrc2.datoolbox.gui.annotation;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import edu.umich.med.mrc2.datoolbox.data.AnnotatedObject;
import edu.umich.med.mrc2.datoolbox.data.compare.ObjectAnnotationDateComparator;
import edu.umich.med.mrc2.datoolbox.data.lims.ObjectAnnotation;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTableModel;
import edu.umich.med.mrc2.datoolbox.gui.tables.ColumnContext;

public class ObjectAnnotationTableModel extends BasicTableModel {

	/**
	 *
	 */
	private static final long serialVersionUID = 63624132728676065L;

	private static final ObjectAnnotationDateComparator dateSorter = new ObjectAnnotationDateComparator();

	public static final String ANNOTATION_COLUMN = "Annotation";
	public static final String LAST_MODIFIED_COLUMN = "Last modified";
	public static final String FILE_PREVIEW_COLUMN = "View";
	public static final String FILE_DOWNLOAD_COLUMN = "Format";


	public ObjectAnnotationTableModel() {
		super();
		columnArray = new ColumnContext[] {
			new ColumnContext(ANNOTATION_COLUMN, ObjectAnnotation.class, false),			
//			new ColumnContext(FILE_PREVIEW_COLUMN, ObjectAnnotation.class, false),
			new ColumnContext(FILE_DOWNLOAD_COLUMN, ObjectAnnotation.class, false),
//			new ColumnContext(LAST_MODIFIED_COLUMN, Date.class, false),
		};
	}

	public void setTableModelFromAnnotatedObject(AnnotatedObject feature) {

		setRowCount(0);
		if(feature.getAnnotations().isEmpty())
			return;

		List<ObjectAnnotation> sortedAnnotations =
				feature.getAnnotations().stream().
				sorted(dateSorter).collect(Collectors.toList());

		List<Object[]>rowData = new ArrayList<Object[]>();		
		for (ObjectAnnotation annotation : sortedAnnotations) {

			Object[] obj = {
					annotation,					
//					annotation,
					annotation,
//					annotation.getLastModified(),
					};
			rowData.add(obj);
		}
		addRows(rowData);
	}
}










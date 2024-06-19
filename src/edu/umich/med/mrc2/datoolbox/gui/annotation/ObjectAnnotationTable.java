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

import edu.umich.med.mrc2.datoolbox.data.AnnotatedObject;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.lims.ObjectAnnotation;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.ObjectAnnotationDocumentTypeRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.ObjectAnnotationTextPreviewRenderer;

public class ObjectAnnotationTable extends BasicTable {

	/**
	 *
	 */
	private static final long serialVersionUID = -7205271819061712691L;
	private AnnotatedObject currentObject;

	public static final int iconSize = 24;

	public ObjectAnnotationTable(int maxAnnotationPreviewLength) {

		super();

		model = new ObjectAnnotationTableModel();
		setModel(model);
		
		columnModel.getColumnById(ObjectAnnotationTableModel.ANNOTATION_COLUMN)
				.setCellRenderer(new ObjectAnnotationTextPreviewRenderer(
						SortProperty.Name, maxAnnotationPreviewLength));
		columnModel.getColumnById(ObjectAnnotationTableModel.FILE_DOWNLOAD_COLUMN)
				.setCellRenderer(new ObjectAnnotationDocumentTypeRenderer(this));
		
		columnModel.getColumnById(
				ObjectAnnotationTableModel.FILE_DOWNLOAD_COLUMN).setWidth(iconSize * 2);
		fixedWidthColumns.add(model.getColumnIndex(
				ObjectAnnotationTableModel.FILE_DOWNLOAD_COLUMN));
		currentObject = null;
		
		finalizeLayout();
	}

	public AnnotatedObject getCurrentFeature() {
		return currentObject;
	}

	public void setTableModelFromAnnotatedObject(AnnotatedObject feature) {

		currentObject = feature;
		((ObjectAnnotationTableModel)model).setTableModelFromAnnotatedObject(feature);
//		getColumnModel().getColumn(
//				model.getColumnIndex(ObjectAnnotationTableModel.FILE_PREVIEW_COLUMN)).setWidth(iconSize * 2);
		
		adjustColumns();
	}

	public void previewAnnotationDocument(ObjectAnnotation annotation) {
		//	TODO
	}
}

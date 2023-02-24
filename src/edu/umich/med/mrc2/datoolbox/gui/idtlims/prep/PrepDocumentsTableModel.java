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

package edu.umich.med.mrc2.datoolbox.gui.idtlims.prep;

import java.util.Collection;
import java.util.Date;

import edu.umich.med.mrc2.datoolbox.data.lims.LIMSUser;
import edu.umich.med.mrc2.datoolbox.data.lims.ObjectAnnotation;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTableModel;
import edu.umich.med.mrc2.datoolbox.gui.tables.ColumnContext;

public class PrepDocumentsTableModel extends BasicTableModel {

	/**
	 *
	 */
	private static final long serialVersionUID = 1754650081223640461L;

	//	TODO - make this annotations table
	
	public static final String ANNOTATION_ID_COLUMN = "ID";
	public static final String DOCUMENT_DESCRIPTION_COLUMN = "Description";
	public static final String FILE_DOWNLOAD_COLUMN = "Format";
	public static final String ADDED_BY_COLUMN = "Added by";
	public static final String ADDED_ON_COLUMN = "Added on";

	public PrepDocumentsTableModel() {

		super();
		columnArray = new ColumnContext[] {

			new ColumnContext(ANNOTATION_ID_COLUMN, ObjectAnnotation.class, false),
			new ColumnContext(DOCUMENT_DESCRIPTION_COLUMN, String.class, false),
			new ColumnContext(ADDED_BY_COLUMN, LIMSUser.class, false),
			new ColumnContext(ADDED_ON_COLUMN, Date.class, false),
			new ColumnContext(FILE_DOWNLOAD_COLUMN, ObjectAnnotation.class, false),
		};
	}

	public void setModelFromAnnotations(Collection<ObjectAnnotation>annotations) {

		setRowCount(0);

		for(ObjectAnnotation annotation : annotations) {
			
			String text = annotation.getText(100);
			if(annotation.getLinkedDocumentId() != null)
				text = annotation.getLinkedDocumentName();
						
			Object[] obj = {
					annotation,
					text,
					annotation.getCreateBy(),
					annotation.getDateCreated(),
					annotation
			};
			super.addRow(obj);
		}
	}
}

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

package edu.umich.med.mrc2.datoolbox.gui.labnote;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import edu.umich.med.mrc2.datoolbox.data.Assay;
import edu.umich.med.mrc2.datoolbox.data.ExperimentalSample;
import edu.umich.med.mrc2.datoolbox.data.lims.AnalysisQcEventAnnotation;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSExperiment;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSInstrument;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSUser;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTableModel;
import edu.umich.med.mrc2.datoolbox.gui.tables.ColumnContext;

public class LabNotesTableModel extends BasicTableModel {

	/**
	 *
	 */
	private static final long serialVersionUID = 3395404432415068892L;

	public static final String DATE_CREATED_COLUMN = "Date";
	public static final String CREATED_BY_COLUMN = "Created by";
	public static final String EXPERIMENT_COLUMN = "Experiment";
	public static final String ASSAY_COLUMN = "Assay";
	public static final String SAMPLE_COLUMN = "Sample";
	public static final String INSTRUMENT_COLUMN = "Instrument";
	public static final String EVENT_CATEGORY_COLUMN = "Category";

	public LabNotesTableModel() {
		super();
		columnArray = new ColumnContext[] {
			new ColumnContext(DATE_CREATED_COLUMN, Date.class, false),
			new ColumnContext(CREATED_BY_COLUMN, LIMSUser.class, false),
			new ColumnContext(EXPERIMENT_COLUMN, LIMSExperiment.class, false),
			new ColumnContext(ASSAY_COLUMN, Assay.class, false),
			new ColumnContext(SAMPLE_COLUMN, ExperimentalSample.class, false),
			new ColumnContext(INSTRUMENT_COLUMN, LIMSInstrument.class, false),
			new ColumnContext(EVENT_CATEGORY_COLUMN, AnalysisQcEventAnnotation.class, false),
		};
	}

	public void setTableModelFromAnnotations(
			Collection<AnalysisQcEventAnnotation>annotations, boolean replace) {

		if(replace)
			setRowCount(0);

		if(annotations == null || annotations.isEmpty())
			return;

		List<Object[]>rowData = new ArrayList<Object[]>();
		for (AnalysisQcEventAnnotation annotation : annotations) {

			Object[] obj = {
					annotation.getDateCreated(),
					annotation.getCreateBy(),
					annotation.getExperiment(),
					annotation.getAssay(),
					annotation.getSample(),
					annotation.getInstrument(),
					annotation,
			};
			rowData.add(obj);
		}
		addRows(rowData);
	}

	public void removeAnnotations(Collection<AnalysisQcEventAnnotation> annotationsToRemove) {

		int annotationIdx = getColumnIndex(EVENT_CATEGORY_COLUMN);
		Collection<AnalysisQcEventAnnotation>toDisplay = new ArrayList<AnalysisQcEventAnnotation>();
		for(int i=0; i<getRowCount(); i++) {

			AnalysisQcEventAnnotation annot = (AnalysisQcEventAnnotation)getValueAt(i, annotationIdx);
			if(!annotationsToRemove.contains(annot))
				toDisplay.add(annot);
		}
		setTableModelFromAnnotations(toDisplay, true);
	}
}















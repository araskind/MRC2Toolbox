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

package edu.umich.med.mrc2.datoolbox.gui.refsamples;

import java.util.ArrayList;
import java.util.List;

import edu.umich.med.mrc2.datoolbox.data.ExperimentDesign;
import edu.umich.med.mrc2.datoolbox.data.ExperimentalSample;
import edu.umich.med.mrc2.datoolbox.data.enums.MoTrPACQCSampleType;
import edu.umich.med.mrc2.datoolbox.data.lims.DataAcquisitionMethod;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTableModel;
import edu.umich.med.mrc2.datoolbox.gui.tables.ColumnContext;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.ReferenceSamplesManager;

public class ExperimentReferenceSampleTableModel extends BasicTableModel {

	/**
	 *
	 */
	private static final long serialVersionUID = -237795112533015325L;

	public static final String SAMPLE_ID_COLUMN = "Sample ID";
	public static final String SAMPLE_NAME_COLUMN = "Sample name";
	public static final String SAMPLE_TYPE_COLUMN = "Sample type";
	public static final String SAMPLE_COUNT_COLUMN = "Count";

	public ExperimentReferenceSampleTableModel() {

		super();
		columnArray = new ColumnContext[] {

			new ColumnContext(SAMPLE_ID_COLUMN, SAMPLE_ID_COLUMN, ExperimentalSample.class, false),
			new ColumnContext(SAMPLE_NAME_COLUMN, SAMPLE_NAME_COLUMN, String.class, false),
			new ColumnContext(SAMPLE_TYPE_COLUMN, SAMPLE_TYPE_COLUMN, MoTrPACQCSampleType.class, false),
			new ColumnContext(SAMPLE_COUNT_COLUMN, "Number of samples of the type in the experiment", Integer.class, false),
		};
	}

	public void loadReferenceSamples(ExperimentDesign design) {

		setRowCount(0);
		if(design == null || design.getSamples().isEmpty())
			return;
		
		DataAcquisitionMethod method = MRC2ToolBoxCore.getActiveMetabolomicsExperiment().
				getActiveDataPipeline().getAcquisitionMethod();
		List<Object[]>rowData = new ArrayList<Object[]>();
		for(ExperimentalSample sample : ReferenceSamplesManager.getReferenceSamples()) {
			
			ExperimentalSample refSample = design.getSampleById(sample.getId());
			if(refSample != null) {
				
				Object[] obj = {
						refSample,
						refSample.getName(),
						refSample.getMoTrPACQCSampleType(),
						refSample.getDataFilesForMethod(method).size(),
					};
				rowData.add(obj);
			}
		}
		if(!rowData.isEmpty())
			addRows(rowData);
	}
}

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

package edu.umich.med.mrc2.datoolbox.gui.datexp.hm;

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
import edu.umich.med.mrc2.datoolbox.project.Project;

public class SampleGroupTableModel extends BasicTableModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	public static final String INCLUDE_COLUMN = "Include";
	public static final String SAMPLE_ID_COLUMN = "ID";
	public static final String SAMPLE_TYPE_COLUMN = "Type";
	public static final String SAMPLE_COUNT_COLUMN = "Count";

	public SampleGroupTableModel() {

		super();
		columnArray = new ColumnContext[] {

			new ColumnContext(INCLUDE_COLUMN, INCLUDE_COLUMN, Boolean.class, true),
			new ColumnContext(SAMPLE_ID_COLUMN, SAMPLE_ID_COLUMN, ExperimentalSample.class, false),
			new ColumnContext(SAMPLE_TYPE_COLUMN, SAMPLE_TYPE_COLUMN, MoTrPACQCSampleType.class, false),
			new ColumnContext(SAMPLE_COUNT_COLUMN, "Number of samples of the type in the experiment", Integer.class, false),
		};
	}

	public void loadSampleTypes(Project experiment) {

		setRowCount(0);
		if(experiment == null || experiment.getExperimentDesign() == null 
				|| experiment.getExperimentDesign().getSamples().isEmpty())
			return;
		
		ExperimentDesign design = experiment.getExperimentDesign();		
		DataAcquisitionMethod method = MRC2ToolBoxCore.getActiveMetabolomicsExperiment().
				getActiveDataPipeline().getAcquisitionMethod();
		List<Object[]>rowData = new ArrayList<Object[]>();
		for(ExperimentalSample sample : ReferenceSamplesManager.getReferenceSamples()) {
			
			ExperimentalSample refSample = design.getSampleById(sample.getId());
			if(refSample != null) {
				
				Object[] obj = createSampleRow(refSample, 
						refSample.getDataFilesForMethod(method).size());
				rowData.add(obj);
			}
		}
		ExperimentalSample regularSample = ReferenceSamplesManager.getGenericRegularSample();
		regularSample.setMoTrPACQCSampleType(MoTrPACQCSampleType.REGULAR_SAMPLE);
		
		long regSampleCount = design.getRegularSamples().stream().
				flatMap(s -> s.getDataFilesForMethod(method).stream()).count();
		Object[] obj = createSampleRow(regularSample, (int)regSampleCount);
		rowData.add(obj);
		
		if(!rowData.isEmpty())
			addRows(rowData);
	}
	
	private Object[]createSampleRow(ExperimentalSample sample, int count){
		
		Object[] obj = {
				true,
				sample,
				sample.getName(),
				count,
			};
		return obj;
	}
}

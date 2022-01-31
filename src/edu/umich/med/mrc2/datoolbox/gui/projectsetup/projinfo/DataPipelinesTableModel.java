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

package edu.umich.med.mrc2.datoolbox.gui.projectsetup.projinfo;

import java.util.Set;

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTableModel;
import edu.umich.med.mrc2.datoolbox.gui.tables.ColumnContext;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class DataPipelinesTableModel extends BasicTableModel {

	/**
	 *
	 */
	private static final long serialVersionUID = 7387427636234648515L;

	public static final String ACTIVE_COLUMN = "Active";
	public static final String DATA_PIPELINE_COLUMN = "Data pipeline";
	public static final String NUM_DATA_FILES_COLUMN = "# of data files";
	public static final String NUM_FEATURES_COLUMN = "# of features";
	public static final String WORKLIST_COLUMN = "Worklist";
	public static final String LIBRARY_COLUMN = "Library";

	public DataPipelinesTableModel() {
		super();
		columnArray = new ColumnContext[] {
			new ColumnContext(ACTIVE_COLUMN, Boolean.class, false),
			new ColumnContext(DATA_PIPELINE_COLUMN, DataPipeline.class, false),
			new ColumnContext(NUM_DATA_FILES_COLUMN, Integer.class, false),
			new ColumnContext(NUM_FEATURES_COLUMN, Integer.class, false),
			new ColumnContext(WORKLIST_COLUMN, Boolean.class, false),	//	TODO convert to int 0,1,2 - no, partial, yes
			new ColumnContext(LIBRARY_COLUMN, Boolean.class, false)
		};
	}

	public void setTableModelFromProject(DataAnalysisProject currentProject) {

		setRowCount(0);

		for (DataPipeline pipeline : currentProject.getDataPipelines()) {

			boolean isActive = false;
			int numSamples = 0;
			int numFeatures = 0;
			boolean hasWorklist = false;
			boolean hasLibrary = false;
			
			if(pipeline.equals(currentProject.getActiveDataPipeline()))
				isActive = true;

			Set<DataFile> dataFiles = 
					currentProject.getDataFilesForAcquisitionMethod(pipeline.getAcquisitionMethod());
			if (dataFiles != null)
				numSamples = dataFiles.size();
			
			Set<MsFeature> features = currentProject.getMsFeaturesForDataPipeline(pipeline);
			if (features != null)
				numFeatures = features.size();

			if (currentProject.getWorklistForDataAcquisitionMethod(pipeline.getAcquisitionMethod()) != null)
				hasWorklist = true;

			if (currentProject.getCompoundLibraryForDataPipeline(pipeline) != null)
				hasLibrary = true;

			Object[] obj = {
					isActive,
					pipeline,
					numSamples,
					numFeatures,
					hasWorklist,
					hasLibrary
				};
			super.addRow(obj);
		}
	}
}
















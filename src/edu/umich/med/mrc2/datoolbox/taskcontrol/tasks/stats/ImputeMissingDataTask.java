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

package edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.stats;

import org.ujmp.core.Matrix;
import org.ujmp.core.calculation.Calculation.Ret;

import edu.umich.med.mrc2.datoolbox.data.enums.DataImputationType;
import edu.umich.med.mrc2.datoolbox.data.enums.DataManipulationType;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;

public class ImputeMissingDataTask  extends AbstractTask {

	private DataAnalysisProject currentProject;
	private DataPipeline activeDataPipeline;
	private DataImputationType imputationMethod;
	private Object[] imputationMethodParameters;

	public ImputeMissingDataTask(DataImputationType method, Object[] parameters) {
		
		imputationMethod = method;
		imputationMethodParameters = parameters;
		currentProject = MRC2ToolBoxCore.getCurrentProject();
		activeDataPipeline = currentProject.getActiveDataPipeline();
	}

	@Override
	public Task cloneTask() {

		return new ImputeMissingDataTask(imputationMethod, imputationMethodParameters);
	}
	
	public DataImputationType getImputationMethod() {
		return imputationMethod;
	}

	@Override
	public void run() {

		setStatus(TaskStatus.PROCESSING);

		try {			
			Matrix imputed = currentProject.getDataMatrixForDataPipeline(activeDataPipeline).impute(Ret.NEW,
					imputationMethod.getMethod(), imputationMethodParameters);
			imputed.setMetaData(DataManipulationType.IMPUTATION_TYPE.name(), imputationMethod);
			currentProject.setImputedDataMatrixForDataPipeline(activeDataPipeline, imputed);
			setStatus(TaskStatus.FINISHED);	

		} catch (Exception e) {

			e.printStackTrace();
			setStatus(TaskStatus.ERROR);
		}			
	}
}

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

package edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.derepl;

import org.ujmp.core.BaseMatrix;
import org.ujmp.core.Matrix;
import org.ujmp.core.calculation.Calculation.Ret;

import edu.umich.med.mrc2.datoolbox.data.BinnerPreferencesObject;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureSet;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskEvent;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskListener;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.utils.DataSetUtils;

public class BinnerProcessingTask extends AbstractTask implements TaskListener {
	
	private BinnerPreferencesObject bpo;
	private DataAnalysisProject currentExperiment;
	
	public BinnerProcessingTask(BinnerPreferencesObject bpo) {
		super();
		this.bpo = bpo;
		currentExperiment = MRC2ToolBoxCore.getActiveMetabolomicsExperiment();
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		setStatus(TaskStatus.PROCESSING);
		try {
			filterInputData();
		} catch (Exception e) {

			e.printStackTrace();
			setStatus(TaskStatus.ERROR);			
			return;
		}
		try {
			binFeaturesByRt();
		} catch (Exception e) {

			e.printStackTrace();
			setStatus(TaskStatus.ERROR);
			return;

		}
		setStatus(TaskStatus.FINISHED);
	}

	private void binFeaturesByRt() {
		
		// Create data matrix for selected files
		
	}

	private void filterInputData() {

		//	Subset the data
		MsFeatureSet activeFeatureSet = 
				currentExperiment.getActiveFeatureSetForDataPipeline(bpo.getDataPipeline());
		Matrix dataMatrixForBinner = DataSetUtils.subsetDataMatrix(
				currentExperiment.getDataMatrixForDataPipeline(bpo.getDataPipeline()), 
				activeFeatureSet.getFeatures(), 
				bpo.getInputFiles());

		Matrix missingnessMatrix  = dataMatrixForBinner.
				replace(Ret.LINK, Double.valueOf("0"), Double.NaN).
				countMissing(Ret.NEW, BaseMatrix.ROW).
				divide(dataMatrixForBinner.getRowCount() / 100.0d);
		long[]coord = new long[] {0,0};
		for(long c=0; c<missingnessMatrix.getColumnCount(); c++) {
			coord[1] = c;
			double missingness = missingnessMatrix.getAsDouble(coord);
			System.out.println(Double.toString(missingness));
			
			//	Write diagnostic list of features removed based on missingness
			
		}
	}

	@Override
	public void statusChanged(TaskEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public Task cloneTask() {
		return new BinnerProcessingTask(bpo) ;
	}
}

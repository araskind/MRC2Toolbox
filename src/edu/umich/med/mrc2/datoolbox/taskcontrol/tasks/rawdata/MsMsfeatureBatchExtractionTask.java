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

package edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.rawdata;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.enums.IntensityMeasure;
import edu.umich.med.mrc2.datoolbox.data.enums.MassErrorType;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskEvent;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskListener;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.utils.Range;

public class MsMsfeatureBatchExtractionTask extends AbstractTask implements TaskListener {

	private Collection<DataFile> rawDataFiles;
	private Map<DataFile, Collection<MsFeature>>msFeatureMap;
	private Range dataExtractionRtRange;
	private boolean removeAllMassesAboveParent;
	private double msMsCountsCutoff;
	private int maxFragmentsCutoff;
	private IntensityMeasure filterIntensityMeasure;
	private double msmsIsolationWindowLowerBorder;
	private double msmsIsolationWindowUpperBorder;	
	private double msmsGroupingRtWindow;
	private double precursorGroupingMassError;
	private MassErrorType precursorGroupingMassErrorType;
	
	public MsMsfeatureBatchExtractionTask(
			Collection<DataFile> rawDataFiles, 
			Range dataExtractionRtRange,
			boolean removeAllMassesAboveParent, 
			double msMsCountsCutoff, 
			int maxFragmentsCutoff,
			IntensityMeasure filterIntensityMeasure,
			double msmsIsolationWindowLowerBorder,
			double msmsIsolationWindowUpperBorder,
			double msmsGroupingRtWindow,
			double precursorGroupingMassError,
			MassErrorType precursorGroupingMassErrorType) {
		super();
		this.rawDataFiles = rawDataFiles;
		this.dataExtractionRtRange = dataExtractionRtRange;
		this.removeAllMassesAboveParent = removeAllMassesAboveParent;
		this.msMsCountsCutoff = msMsCountsCutoff;
		this.maxFragmentsCutoff = maxFragmentsCutoff;
		this.filterIntensityMeasure = filterIntensityMeasure;
		this.msmsIsolationWindowLowerBorder = msmsIsolationWindowLowerBorder;
		this.msmsIsolationWindowUpperBorder = msmsIsolationWindowUpperBorder;
		this.msmsGroupingRtWindow = msmsGroupingRtWindow;
		this.precursorGroupingMassError = precursorGroupingMassError;
		this.precursorGroupingMassErrorType = precursorGroupingMassErrorType;
		msFeatureMap = new TreeMap<DataFile, Collection<MsFeature>>();		
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		setStatus(TaskStatus.PROCESSING);
		taskDescription = "Extracting MSMS features from raw data ... ";
		total = 100;
		processed = 0;
		for(DataFile df : rawDataFiles) {
			
			MsMsfeatureExtractionTask task = new MsMsfeatureExtractionTask(
					df,
					dataExtractionRtRange,
					removeAllMassesAboveParent,
					msMsCountsCutoff,
					maxFragmentsCutoff,
					filterIntensityMeasure,
					msmsIsolationWindowLowerBorder,
					msmsIsolationWindowUpperBorder,
					msmsGroupingRtWindow,
					precursorGroupingMassError,
					precursorGroupingMassErrorType);
			task.addTaskListener(this);
			MRC2ToolBoxCore.getTaskController().addTask(task);
		}
	}
	
	@Override
	public Task cloneTask() {
		return new  MsMsfeatureBatchExtractionTask(
				rawDataFiles, 
				dataExtractionRtRange,
				removeAllMassesAboveParent, 
				msMsCountsCutoff, 
				maxFragmentsCutoff,
				filterIntensityMeasure,
				msmsIsolationWindowLowerBorder,
				msmsIsolationWindowUpperBorder,
				msmsGroupingRtWindow,
				precursorGroupingMassError,
				precursorGroupingMassErrorType);
	}

	@Override
	public void statusChanged(TaskEvent e) {

		if (e.getStatus() == TaskStatus.FINISHED) {
			
			((AbstractTask)e.getSource()).removeTaskListener(this);
			
			if (e.getSource().getClass().equals(MsMsfeatureExtractionTask.class)) {
				MsMsfeatureExtractionTask task = (MsMsfeatureExtractionTask)e.getSource();
				msFeatureMap.put(task.getRawDataFile(), task.getMSMSFeatures());
				processed++;
				if(processed == rawDataFiles.size())
					setStatus(TaskStatus.FINISHED);
			}
		}
	}

	public Map<DataFile, Collection<MsFeature>> getMsFeatureMap() {
		return msFeatureMap;
	}
}

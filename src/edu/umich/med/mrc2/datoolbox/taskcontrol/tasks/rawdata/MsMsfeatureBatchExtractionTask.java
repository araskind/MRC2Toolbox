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
import java.util.stream.Collectors;

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureInfoBundle;
import edu.umich.med.mrc2.datoolbox.data.compare.MsFeatureInfoBundleComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskEvent;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskListener;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;

public class MsMsfeatureBatchExtractionTask extends AbstractTask implements TaskListener {

	private MSMSExtractionParameterSet ps;

	private Collection<DataFile> msmsDataFiles;	
	private Collection<DataFile> msOneDataFiles;	
//	private boolean flagMinorIsotopesPrecursors;
//	private int maxPrecursorCharge;	
//	private double chromatogramExtractionWindow;
//	private int smoothingFilterWidth;
//	
//	private SavitzkyGolayFilter smoothingFilter; 
	
	private Map<DataFile, Collection<MsFeatureInfoBundle>>msFeatureMap;
	

	public MsMsfeatureBatchExtractionTask(
			MSMSExtractionParameterSet ps, 	
			Collection<DataFile> msmsDataFiles,
			Collection<DataFile> msOneDataFiles) {
		super();
		this.ps = ps;
		this.msmsDataFiles = msmsDataFiles;
		this.msOneDataFiles = msOneDataFiles;
//		this.flagMinorIsotopesPrecursors = ps.isFlagMinorIsotopesPrecursors();
//		this.maxPrecursorCharge = ps.getMaxPrecursorCharge();
//		this.smoothingFilterWidth = ps.getSmoothingFilterWidth();
		
		msFeatureMap = 
				new TreeMap<DataFile, Collection<MsFeatureInfoBundle>>();
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		setStatus(TaskStatus.PROCESSING);
		taskDescription = "Extracting MSMS features from raw data ... ";
		total = 100;
		processed = 0;
		for(DataFile df : msmsDataFiles) {			
			MsMsfeatureExtractionTask task = new MsMsfeatureExtractionTask(df, ps);
			task.addTaskListener(this);
			MRC2ToolBoxCore.getTaskController().addTask(task);
		}
	}
	
	@Override
	public Task cloneTask() {
		return new  MsMsfeatureBatchExtractionTask(
				ps,
				msmsDataFiles, 
				msOneDataFiles);
	}

	@Override
	public void statusChanged(TaskEvent e) {

		if (e.getStatus() == TaskStatus.FINISHED) {
			
			((AbstractTask)e.getSource()).removeTaskListener(this);
			
			if (e.getSource().getClass().equals(MsMsfeatureExtractionTask.class)) {
				MsMsfeatureExtractionTask task = (MsMsfeatureExtractionTask)e.getSource();
				msFeatureMap.put(task.getRawDataFile(), task.getMsFeatureInfoBundles());
				processed++;
				if(processed == msmsDataFiles.size() && msOneDataFiles.size() > 0) {
					initChromatogramExtraction();
				}
				else {
					setStatus(TaskStatus.FINISHED);
					return;
				}
			}
			if (e.getSource().getClass().equals(MsFeatureChromatogramBatchExtractionTask.class)) {
				//	TODO attach chromatograms
				setStatus(TaskStatus.FINISHED);
			}
		}
	}

	private void initChromatogramExtraction() {

		Collection<MsFeatureInfoBundle> features = msFeatureMap.entrySet().stream().
					flatMap(e -> e.getValue().stream()).
					sorted(new MsFeatureInfoBundleComparator(SortProperty.RT)).
					collect(Collectors.toList());
		MsFeatureChromatogramBatchExtractionTask task = 
				new MsFeatureChromatogramBatchExtractionTask(
						msOneDataFiles, 
						features, 
						ps.getCommonChromatogramDefinition(),
						ps.getXicTarget());
		task.addTaskListener(this);
		MRC2ToolBoxCore.getTaskController().addTask(task);
	}

	public Map<DataFile, Collection<MsFeatureInfoBundle>> getMsFeatureMap() {
		return msFeatureMap;
	}
}

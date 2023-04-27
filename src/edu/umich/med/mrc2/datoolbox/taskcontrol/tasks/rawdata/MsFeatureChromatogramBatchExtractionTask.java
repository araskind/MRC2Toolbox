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
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;

import edu.umich.med.mrc2.datoolbox.data.ChromatogramDefinition;
import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.ExtractedIonData;
import edu.umich.med.mrc2.datoolbox.data.MSFeatureInfoBundle;
import edu.umich.med.mrc2.datoolbox.data.MassSpectrum;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureChromatogramBundle;
import edu.umich.med.mrc2.datoolbox.data.TandemMassSpectrum;
import edu.umich.med.mrc2.datoolbox.data.enums.MsFeatureChromatogramExtractionTarget;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskEvent;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskListener;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;

public class MsFeatureChromatogramBatchExtractionTask extends AbstractTask implements TaskListener {
	
	private Collection<DataFile>rawDataFiles;
	private Collection<MSFeatureInfoBundle>features;
	private ChromatogramDefinition commonDefinition;
	private MsFeatureChromatogramExtractionTarget xicTarget;	
	private Map<MSFeatureInfoBundle, ChromatogramDefinition>featureChromatogramDefinitions;
	private Map<String, MsFeatureChromatogramBundle>chromatogramMap;
	private int processedFilesCount;	
	private Map<DataFile, Map<MSFeatureInfoBundle, Collection<ExtractedIonData>>>fileChromatogramMap;

	public MsFeatureChromatogramBatchExtractionTask(
			Collection<DataFile> rawDataFiles,
			Collection<MSFeatureInfoBundle> features, 
			ChromatogramDefinition commonDefinition,
			MsFeatureChromatogramExtractionTarget xicTarget) {
		super();
		this.rawDataFiles = rawDataFiles;
		this.features = features;
		this.commonDefinition = commonDefinition;
		this.xicTarget = xicTarget;
		chromatogramMap = 
				new HashMap<String, MsFeatureChromatogramBundle>();
		fileChromatogramMap = 
				new TreeMap<DataFile, Map<MSFeatureInfoBundle, Collection<ExtractedIonData>>>();
	}

	@Override
	public void run() {

		setStatus(TaskStatus.PROCESSING);
		createFeatureChromatogramDefinitions();
		taskDescription = "Extracting feature chromatograms ... ";
		total = 100;
		processed = 20;
		processedFilesCount = 0;
		for(DataFile df : rawDataFiles) {
			
			MsFeatureChromatogramExtractionTask task = 
					new MsFeatureChromatogramExtractionTask(df, featureChromatogramDefinitions);
			task.addTaskListener(this);
			MRC2ToolBoxCore.getTaskController().addTask(task);
		}
	}
	
	private void createFeatureChromatogramDefinitions() {
		
		taskDescription = "Creating chromatogram definitions ... ";
		total = features.size();
		processed = 0;
		featureChromatogramDefinitions = 
				new HashMap<MSFeatureInfoBundle, ChromatogramDefinition>();
		
		for(MSFeatureInfoBundle mfb : features) {
			
			Collection<Double>mzList = new TreeSet<Double>();			
			MassSpectrum spectrum = mfb.getMsFeature().getSpectrum();
			if(spectrum == null) {
				processed++;
				continue;
			}
			if(xicTarget.equals(MsFeatureChromatogramExtractionTarget.MSMSParentIon)) {
				TandemMassSpectrum msms = 
						spectrum.getExperimentalTandemSpectrum();
				if(msms == null) {
					processed++;
					continue;
				}
				else
					mzList.add(msms.getParent().getMz());
			}
			if(xicTarget.equals(MsFeatureChromatogramExtractionTarget.MS1PrimaryAdduct)) {
				
				if(spectrum.getPrimaryAdduct() != null)					
					mzList.add(spectrum.getPrimaryAdductBasePeakMz());
				else {
					double bpmz = spectrum.getBasePeakMz();
					if(bpmz > 0)
						mzList.add(bpmz);
				}
			}
			if(!mzList.isEmpty()) {
				ChromatogramDefinition cd = commonDefinition.clone();
				cd.setMzList(mzList);
				cd.recenterRtRange(mfb.getMsFeature().getRetentionTime());
				cd.setPolarity(mfb.getMsFeature().getPolarity());
				featureChromatogramDefinitions.put(mfb, cd);
			}		
			processed++;
		}
	}

	@Override
	public void statusChanged(TaskEvent e) {
		
		if (e.getStatus() == TaskStatus.FINISHED) {
			
			((AbstractTask)e.getSource()).removeTaskListener(this);
			
			if (e.getSource().getClass().equals(MsFeatureChromatogramExtractionTask.class)) {
				MsFeatureChromatogramExtractionTask task = (MsFeatureChromatogramExtractionTask)e.getSource();
				fileChromatogramMap.put(task.getRawDataFile(), task.getChromatogramMap());				
				//	createMsFeatureChromatogramBundles(task);
				MRC2ToolBoxCore.getTaskController().getTaskQueue().removeTask(task);
				processedFilesCount++;
				
				if(processedFilesCount == rawDataFiles.size()) {
					createMsFeatureChromatogramBundles();
					setStatus(TaskStatus.FINISHED);
				}
			}
		}
	}

	private void createMsFeatureChromatogramBundles() {
		
		taskDescription = "Finalizing chromatogram extraction ... ";

		for(Entry<DataFile, Map<MSFeatureInfoBundle, Collection<ExtractedIonData>>> e : fileChromatogramMap.entrySet()) {
			
			Map<MSFeatureInfoBundle, Collection<ExtractedIonData>> fileChromatograms = e.getValue();
			total = fileChromatograms.size();
			processed = 0;
			DataFile df = e.getKey();
			for(Entry<MSFeatureInfoBundle, Collection<ExtractedIonData>> entry : fileChromatograms.entrySet()) {
				
				String featureId = entry.getKey().getMsFeature().getId();
				if(chromatogramMap.get(featureId) == null) {
					MsFeatureChromatogramBundle cBundle = 
							new MsFeatureChromatogramBundle(
									featureId,
									featureChromatogramDefinitions.get(entry.getKey()));
					chromatogramMap.put(featureId, cBundle);
				}
				try {
					chromatogramMap.get(featureId).addChromatogramsForDataFile(df, entry.getValue());
				} catch (Exception ex) {
					// TODO Auto-generated catch block
					ex.printStackTrace();
				}
				processed++;
			}
		}
	}

	@Override
	public Task cloneTask() {
		return new MsFeatureChromatogramBatchExtractionTask(
				rawDataFiles, features, commonDefinition, xicTarget);
	}

	public Map<String, MsFeatureChromatogramBundle> getChromatogramMap() {
		return chromatogramMap;
	}
}

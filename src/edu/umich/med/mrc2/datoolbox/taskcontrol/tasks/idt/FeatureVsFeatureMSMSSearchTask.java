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

package edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.idt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Collectors;

import edu.umich.med.mrc2.datoolbox.data.MSFeatureInfoBundle;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureInfoBundleCollection;
import edu.umich.med.mrc2.datoolbox.data.MsPoint;
import edu.umich.med.mrc2.datoolbox.data.enums.MSMSSearchDirection;
import edu.umich.med.mrc2.datoolbox.data.enums.MassErrorType;
import edu.umich.med.mrc2.datoolbox.data.msclust.IMSMSClusterDataSet;
import edu.umich.med.mrc2.datoolbox.data.msclust.MSMSClusterDataSet;
import edu.umich.med.mrc2.datoolbox.data.msclust.MSMSClusterDataSetType;
import edu.umich.med.mrc2.datoolbox.data.msclust.MsFeatureInfoBundleCluster;
import edu.umich.med.mrc2.datoolbox.main.FeatureCollectionManager;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.RecentDataManager;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.msmsscore.MSMSScoreCalculator;
import edu.umich.med.mrc2.datoolbox.msmsscore.MSMSSearchParameterSet;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskEvent;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskListener;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.utils.MsUtils;
import edu.umich.med.mrc2.datoolbox.utils.Range;

public class FeatureVsFeatureMSMSSearchTask extends AbstractTask implements TaskListener {
	
	private Collection<MSFeatureInfoBundle>inputFeatures;
	private MsFeatureInfoBundleCollection featureLib;
	private MSMSSearchParameterSet searchParameters;
	private IMSMSClusterDataSet searchResultsDataSet;
	private Map<String,MsPoint[]>normalizedLibrarySpectra;
	private Map<String,MsPoint[]>normalizedUnknownSpectra;	
	private Collection<MSFeatureInfoBundle>inputLibEntries;
	private Collection<MSFeatureInfoBundle>inputUnknowns;
	private Collection<MSFeatureInfoBundle>validLibEntries;
	private Collection<MSFeatureInfoBundle>validUnknowns;
	
	public FeatureVsFeatureMSMSSearchTask(
			Collection<MSFeatureInfoBundle> inputFeatures,
			MsFeatureInfoBundleCollection featureLib, 
			MSMSSearchParameterSet searchParameters) {
		super();
		this.inputFeatures = inputFeatures;
		this.featureLib = featureLib;
		this.searchParameters = searchParameters;
		normalizedLibrarySpectra = new TreeMap<String,MsPoint[]>();
		normalizedUnknownSpectra = new TreeMap<String,MsPoint[]>();
		if(searchParameters.getMsmsSearchDirection().equals(MSMSSearchDirection.DIRECT)) {
			inputLibEntries = featureLib.getFeatures();
			inputUnknowns = inputFeatures;
		}
		else {
			inputLibEntries = inputFeatures; 
			inputUnknowns = featureLib.getFeatures();
		}
		RecentDataManager.addFeatureCollection(featureLib);
	}

	@Override
	public void run() {
		
		setStatus(TaskStatus.PROCESSING);
		if(!mustFetchLibraryFeatures()) {
			
			try {
				searchFeaturesAgainstLibrary();				
			}
			catch (Exception e) {
				e.printStackTrace();
				setStatus(TaskStatus.ERROR);
				return;
			}
		}
	}
	
	private void searchFeaturesAgainstLibrary() {

		prepareSpectra();
		if(validUnknowns.isEmpty() || validLibEntries.isEmpty()) {
			setStatus(TaskStatus.FINISHED);
			return;
		}		
		taskDescription = "Running MSMS search against feature library";
		total = validUnknowns.size();
		processed = 0;
		
		searchResultsDataSet = new MSMSClusterDataSet(
					"MSMS search against " + featureLib.getName(), 
					"MSMS search against " + featureLib.getName() +" (Created " + 
							MRC2ToolBoxConfiguration.defaultTimeStampFormat.format(new Date()) + ")\n"
							+ "Search direction: " + searchParameters.getMsmsSearchDirection().getName(), 
					MRC2ToolBoxCore.getIdTrackerUser());
		searchResultsDataSet.setDataSetType(MSMSClusterDataSetType.MSMS_SEARCH_BASED);
		searchResultsDataSet.setParameters(searchParameters);
		
		double mzErrorValue = searchParameters.getMzErrorValue();
		MassErrorType massErrorType = searchParameters.getMassErrorType();
		boolean ignoreParentIon = searchParameters.ignoreParentIon();
		double rtErrorValue = searchParameters.getRtErrorValue();
		boolean ignoreRt = searchParameters.ignoreRt();
		double msmsSimilarityCutoff = searchParameters.getMsmsSimilarityCutoff();
		MsPoint unkParent;
		
		Collection<MSFeatureInfoBundle>activeLibrarySubset;
		
		for(MSFeatureInfoBundle f : validUnknowns) {
			
			activeLibrarySubset = validLibEntries;

			if(!ignoreRt) {
				
				final Range rtRange = new Range(
						f.getRetentionTime() - rtErrorValue, 
						f.getRetentionTime() + rtErrorValue);
				activeLibrarySubset = activeLibrarySubset.stream().
						filter(e -> rtRange.contains(e.getRetentionTime())).
						collect(Collectors.toList());
				if(activeLibrarySubset.isEmpty()) {
					processed++;
					continue;
				}
			}
			if(!ignoreParentIon) { 
				
				unkParent = f.getMsFeature().getSpectrum().getExperimentalTandemSpectrum().getParent();
				if(unkParent != null) {
					final Range patentMZRange = MsUtils.createMassRange(
							unkParent.getMz(), mzErrorValue, massErrorType);
					activeLibrarySubset = activeLibrarySubset.stream().
							filter(e -> Objects.nonNull(
									e.getMsFeature().getSpectrum().getExperimentalTandemSpectrum().getParent())).
							filter(e -> patentMZRange.contains(
									e.getMsFeature().getSpectrum().getExperimentalTandemSpectrum().getParent().getMz())).
							collect(Collectors.toList());
				}
				if(activeLibrarySubset.isEmpty()) {
					processed++;
					continue;
				}
			}
			MsPoint[] unkNorm = normalizedUnknownSpectra.get(f.getMSFeatureId());
			Collection<MSFeatureInfoBundle>matches = new ArrayList<MSFeatureInfoBundle>();
			for(MSFeatureInfoBundle l : activeLibrarySubset) {
				
				if(!l.getMsFeature().getPolarity().equals(f.getMsFeature().getPolarity()))
					continue;
				
				MsPoint[] libNorm = normalizedLibrarySpectra.get(l.getMSFeatureId());
				double eScore = MSMSScoreCalculator.calculateEntropyBasedMatchScoreForPreparedSpectra(
									unkNorm, 
									libNorm,
									mzErrorValue, 
									massErrorType);		
				if(eScore > msmsSimilarityCutoff)
					matches.add(l);							
			}
			if(!matches.isEmpty()) {
				
				MsFeatureInfoBundleCluster newCluster = new MsFeatureInfoBundleCluster(f);
				for(MSFeatureInfoBundle match : matches) 
					newCluster.addComponent(null, match);
				
				searchResultsDataSet.getClusters().add(newCluster);
			}
			processed++;
		}
		setStatus(TaskStatus.FINISHED);
	}
	
	private void prepareSpectra() {
		
		double mzWindowValue = searchParameters.getEntropyScoreMassError();
		MassErrorType massErrorType = searchParameters.getEntropyScoreMassErrorType();
		double noiseCutoff = searchParameters.getEntropyScoreNoiseCutoff();
	
		validUnknowns = inputUnknowns.stream().
				filter(f -> Objects.nonNull(f.getMsFeature().getSpectrum())).
				filter(f -> Objects.nonNull(f.getMsFeature().getSpectrum().getExperimentalTandemSpectrum())).
				collect(Collectors.toList());
		
		taskDescription = "Preparing unknown spectra";
		total = validUnknowns.size();
		processed = 0;
		for(MSFeatureInfoBundle l : validUnknowns) {
			
			l.setAsMatchingTarget(true);
			MsPoint[] msmsNorm = 
					MSMSScoreCalculator.cleanAndNormalizeSpectrum(
								l.getMsFeature().getSpectrum().getExperimentalTandemSpectrum().getSpectrum(), 
								mzWindowValue, 
								massErrorType,
								noiseCutoff);
			normalizedUnknownSpectra.put(l.getMSFeatureId(), msmsNorm);
			processed++;
		}		
		validLibEntries = inputLibEntries.stream().
			filter(f -> Objects.nonNull(f.getMsFeature().getSpectrum())).
			filter(f -> Objects.nonNull(f.getMsFeature().getSpectrum().getExperimentalTandemSpectrum())).
			filter(f -> !validUnknowns.contains(f)).
			collect(Collectors.toList());
		
		taskDescription = "Preparing library spectra";
		total = validLibEntries.size();
		processed = 0;
		
		for(MSFeatureInfoBundle f : validLibEntries) {
				
			f.setAsMatchingTarget(false);
			MsPoint[] msmsNorm = 
					MSMSScoreCalculator.cleanAndNormalizeSpectrum(
								f.getMsFeature().getSpectrum().getExperimentalTandemSpectrum().getSpectrum(), 
								mzWindowValue, 
								massErrorType,
								noiseCutoff);
			
			normalizedLibrarySpectra.put(f.getMSFeatureId(), msmsNorm);			
			processed++;
		}
	}

	private boolean mustFetchLibraryFeatures() {
		
		if(MRC2ToolBoxCore.getActiveOfflineRawDataAnalysisExperiment() == null) {
			
			IDTMSMSFeatureDataPullTask task = 
					FeatureCollectionManager.getMsFeatureInfoBundleCollectionData(featureLib);
			if(task == null) {
				 return false;
			}
			else {
				taskDescription = "Fetching library features";
				task.addTaskListener(this);
				MRC2ToolBoxCore.getTaskController().addTask(task);
				return true;
			}
		}
		else {
			 return false;
		}
	}
	
	@Override
	public void statusChanged(TaskEvent e) {

		if (e.getStatus() == TaskStatus.FINISHED) {

			((AbstractTask) e.getSource()).removeTaskListener(this);

			if (e.getSource().getClass().equals(IDTMSMSFeatureDataPullTask.class))
				finalizeIDTMSMSFeatureDataPullTask((IDTMSMSFeatureDataPullTask)e.getSource());
		}
	}
	
	private synchronized void finalizeIDTMSMSFeatureDataPullTask(IDTMSMSFeatureDataPullTask task) {
		
		MRC2ToolBoxCore.getTaskController().getTaskQueue().removeTask(task);
		searchFeaturesAgainstLibrary();		
	}
	
	@Override
	public Task cloneTask() {

		return new FeatureVsFeatureMSMSSearchTask(
				inputFeatures, featureLib, searchParameters);
	}

	public IMSMSClusterDataSet getSearchResults() {
		return searchResultsDataSet;
	}
}

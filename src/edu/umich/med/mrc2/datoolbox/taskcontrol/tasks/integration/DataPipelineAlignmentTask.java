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

package edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.integration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import edu.umich.med.mrc2.datoolbox.data.Adduct;
import edu.umich.med.mrc2.datoolbox.data.CompoundLibrary;
import edu.umich.med.mrc2.datoolbox.data.DataPipelineAlignmentParametersObject;
import edu.umich.med.mrc2.datoolbox.data.DataPipelineAlignmentResults;
import edu.umich.med.mrc2.datoolbox.data.LibraryMsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureCluster;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureStatisticalSummary;
import edu.umich.med.mrc2.datoolbox.data.enums.MassErrorType;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.utils.MsUtils;
import edu.umich.med.mrc2.datoolbox.utils.Range;

public class DataPipelineAlignmentTask extends AbstractTask {

	private DataAnalysisProject currentExperiment;
	private double massWindow;
	private MassErrorType massErrorType;
	private double retentionWindow;
	private boolean excludeUndetected;
	private Set<MsFeatureCluster> clusterList;
	private CompoundLibrary referenceLib;
	private DataPipeline referencePipeline;
	private CompoundLibrary queryLib;
	private DataPipeline queryPipeline;
	private Collection<LibraryMsFeature>unmatchedReferenceFeatures;
	private DataPipelineAlignmentParametersObject alignmentSettings;
	private DataPipelineAlignmentResults alignmentResults;
		
	public DataPipelineAlignmentTask(
			DataAnalysisProject currentExperiment,
			DataPipeline pipelineOne, 
			DataPipeline pipelineTwo, 
			double massWindow,
			MassErrorType massErrorType, 
			double retentionWindow,
			boolean excludeUndetected) {
		super();
		this.currentExperiment = currentExperiment;
		this.massWindow = massWindow;
		this.massErrorType = massErrorType;
		this.retentionWindow = retentionWindow;
		this.excludeUndetected = excludeUndetected;
		clusterList = new HashSet<>();
		setQueryAndReference(pipelineOne,pipelineTwo);
		
		alignmentSettings = new DataPipelineAlignmentParametersObject(
				referencePipeline, 
				queryPipeline,
				massWindow, 
				massErrorType, 
				retentionWindow,
				excludeUndetected);
	}
	
	private void setQueryAndReference(
			DataPipeline pipelineOne, 
			DataPipeline pipelineTwo) {
		
		referenceLib = currentExperiment.getAveragedFeatureLibraryForDataPipeline(pipelineOne);
		referencePipeline = pipelineOne;
		queryLib = currentExperiment.getAveragedFeatureLibraryForDataPipeline(pipelineTwo);
		queryPipeline = pipelineTwo;
		
		int comparison = compareReferenceMaxAdductCountToQuery(referenceLib, queryLib);
		if(comparison == 0)
			comparison = compareReferenceIDcountToQuery(referenceLib, queryLib);
		
		if(comparison == 0)
			comparison = Integer.compare(queryLib.getFeatureCount(), referenceLib.getFeatureCount());
		
		if(comparison < 0) {
			referenceLib = currentExperiment.getAveragedFeatureLibraryForDataPipeline(pipelineTwo);
			referencePipeline = pipelineTwo;
			queryLib = currentExperiment.getAveragedFeatureLibraryForDataPipeline(pipelineOne);
			queryPipeline = pipelineOne;
		}
	}
	
	/**
	 * @param referenceLib
	 * @param queryLib
	 * @return < 0 if Max adduct count per feature in reference is lower than in query
	 */
	private int compareReferenceMaxAdductCountToQuery(
			CompoundLibrary referenceLib, CompoundLibrary queryLib) {
		int maxAdductsRef = referenceLib.getFeatures().
				stream().mapToInt(f -> f.getSpectrum().getAdducts().size()).max().orElse(1);
		int maxAdductsQuery = queryLib.getFeatures().
				stream().mapToInt(f -> f.getSpectrum().getAdducts().size()).max().orElse(1);
		
		return Integer.compare(maxAdductsRef, maxAdductsQuery);
	}
	
	/**
	 * @param referenceLib
	 * @param queryLib
	 * @return < 0 if number of identified features in reference is lower than in query
	 */
	private int compareReferenceIDcountToQuery(
			CompoundLibrary referenceLib, CompoundLibrary queryLib) {
		
		long refIDcount = referenceLib.getFeatures().
				stream().filter(f -> f.isIdentified()).count();
		long queryIDcount = queryLib.getFeatures().
				stream().filter(f -> f.isIdentified()).count();
		return Long.compare(refIDcount, queryIDcount);
	}
		
	@Override
	public void run() {

		setStatus(TaskStatus.PROCESSING);
		try {
			copyStatisticalSummaries();
		}
		catch (Exception e) {
			reportErrorAndExit(e);
		}
		try {
			matchFeaturesFromSelectedPipelines();
		}
		catch (Exception e) {
			reportErrorAndExit(e);
		}
		setStatus(TaskStatus.FINISHED);
	}
	
	private void copyStatisticalSummaries() {

		Map<DataPipeline,CompoundLibrary>dpLibMap = new TreeMap<>();
		dpLibMap.put(referencePipeline, referenceLib);
		dpLibMap.put(queryPipeline, queryLib);
		for(Entry<DataPipeline,CompoundLibrary>dpLibMapEntry : dpLibMap.entrySet()) {
			
			Set<MsFeature> featuresWithStats = 
					currentExperiment.getMsFeaturesForDataPipeline(dpLibMapEntry.getKey());
			Map<String,MsFeatureStatisticalSummary>statsMap = new HashMap<>();
			featuresWithStats.stream().
				forEach(f -> statsMap.put(f.getId(), f.getStatsSummary()));
			dpLibMapEntry.getValue().getFeatures().stream().
				forEach(f -> f.setStatsSummary(statsMap.get(f.getParentFeatureId())));
		}
	}

	private void matchFeaturesFromSelectedPipelines() {
		
		taskDescription = "Matching the features from selected data pipelines";
		total = referenceLib.getFeatureCount();
		processed = 0;
		unmatchedReferenceFeatures = new ArrayList<>();
		Collection<LibraryMsFeature> referenceFeatures = referenceLib.getFeatures();
		if(excludeUndetected)
			referenceFeatures = removeUndetectedReferenceFeatures(referenceFeatures);
		
		for(LibraryMsFeature refFeature : referenceFeatures) {
			
			Set<LibraryMsFeature>matches = findMatchingFeaturesInQueryLibrary(refFeature);
			if(!matches.isEmpty()) {
				MsFeatureCluster newCluster = new MsFeatureCluster();
				newCluster.addFeature(refFeature, referencePipeline);
				for(LibraryMsFeature match : matches)
					newCluster.addFeature(match, queryPipeline);
				
				clusterList.add(newCluster);
			}
			else {
				unmatchedReferenceFeatures.add(refFeature);
			}
			processed++;
		}	
		taskDescription = "Calcilating correlations within clusters ...";
		total = clusterList.size();
		processed = 0;
		for(MsFeatureCluster cluster : clusterList) {
			cluster.createCorrelationMatrix(false);
			processed++;
		}
		alignmentResults = new DataPipelineAlignmentResults(
				alignmentSettings,
				clusterList,
				unmatchedReferenceFeatures);
	}
	
	private Collection<LibraryMsFeature> removeUndetectedReferenceFeatures(
			Collection<LibraryMsFeature> referenceFeatures) {

		return referenceFeatures.stream().
			filter(f -> Objects.nonNull(f.getStatsSummary())).
			filter(f -> (f.getStatsSummary().getSampleMedian() > 0.0
					|| f.getStatsSummary().getPooledMedian() > 0.0)).collect(Collectors.toList());
	}

	private Set<LibraryMsFeature> findMatchingFeaturesInQueryLibrary(LibraryMsFeature refFeature) {
		
		Set<LibraryMsFeature>matches = new HashSet<>();
		Range rtRange = new Range(
				refFeature.getRetentionTime() - retentionWindow, 
				refFeature.getRetentionTime() + retentionWindow);
		List<LibraryMsFeature>matchedByRt= 
				queryLib.getFeatures().stream().
				filter(f -> rtRange.contains(f.getRetentionTime())).
				collect(Collectors.toList());
		if(matchedByRt.isEmpty())
			return matches;
		
		Map<Adduct,Collection<LibraryMsFeature>>matchByAdduct = new TreeMap<>();
		for(Adduct refAdduct : refFeature.getSpectrum().getAdducts()) {
			
			int charge = refAdduct.getCharge();
			double refMz = refFeature.getSpectrum().getMsForAdduct(refAdduct)[0].getMz();
			Range mzRange = MsUtils.createMassRange(refMz, massWindow, massErrorType);
			List<LibraryMsFeature>matchedByMz = matchedByRt.stream().
				filter(f -> matchByAnyAdduct(f, mzRange, charge)).
				collect(Collectors.toList());
			if(!matchedByMz.isEmpty())
				matchByAdduct.put(refAdduct,matchedByMz);			
		}
		//	TODO If both ref and match have more than one adduct 
		//	check that corresponding adducts match
		if(!matchByAdduct.isEmpty())
			matches.addAll(matchByAdduct.values().stream().
					flatMap(v -> v.stream()).distinct().
					collect(Collectors.toList()));
		
		return matches;
	}
	
	private boolean matchByAnyAdduct(LibraryMsFeature feature, Range mzRange, int charge) {
		
		for(Adduct adduct : feature.getSpectrum().getAdducts()) {
			
			if(adduct.getCharge() == charge 
					&& mzRange.contains(feature.getSpectrum().getMsForAdduct(adduct)[0].getMz()))
				return true;
		}
		return false;
	}

	@Override
	public Task cloneTask() {

		return new DataPipelineAlignmentTask(
				currentExperiment, 
				referencePipeline, 
				queryPipeline,
				massWindow, 
				massErrorType, 
				retentionWindow,
				excludeUndetected);
	}

	public DataPipelineAlignmentResults getAlignmentResults() {
		return alignmentResults;
	}
}















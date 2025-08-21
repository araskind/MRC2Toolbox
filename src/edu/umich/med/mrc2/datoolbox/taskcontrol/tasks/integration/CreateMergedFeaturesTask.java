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

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.ujmp.core.Matrix;

import edu.umich.med.mrc2.datoolbox.data.Adduct;
import edu.umich.med.mrc2.datoolbox.data.LibraryMsFeature;
import edu.umich.med.mrc2.datoolbox.data.MassSpectrum;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureCluster;
import edu.umich.med.mrc2.datoolbox.data.MsPoint;
import edu.umich.med.mrc2.datoolbox.data.RangeBucket;
import edu.umich.med.mrc2.datoolbox.data.enums.MassErrorType;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.utils.MsUtils;
import edu.umich.med.mrc2.datoolbox.utils.Range;

public class CreateMergedFeaturesTask extends AbstractTask {
	
	private DataAnalysisProject currentExperiment;
	private Set<MsFeatureCluster> clusterSet;
	private double massWindow;
	private MassErrorType massErrorType;
	
	private Matrix combinedFeaturesMatrix;
	private Matrix combinedDatasMatrix;
	private Map<MsFeatureCluster,LibraryMsFeature>mergedFeaturesMap;

	public CreateMergedFeaturesTask(
			DataAnalysisProject currentExperiment, 
			Set<MsFeatureCluster> clusterSet,
			double massWindow, 
			MassErrorType massErrorType) {
		super();
		this.currentExperiment = currentExperiment;
		this.clusterSet = clusterSet;
		this.massWindow = massWindow;
		this.massErrorType = massErrorType;
	}

	@Override
	public void run() {

		taskDescription = "";
		setStatus(TaskStatus.PROCESSING);
		try {
			createCombinedFeatures();
		}
		catch (Exception e) {
			reportErrorAndExit(e);
		}
		try {
			createMatricesForCombinedFeatures();
		}
		catch (Exception e) {
			reportErrorAndExit(e);
		}
		try {
			calculateCombinedFeaturesStats();
		}
		catch (Exception e) {
			reportErrorAndExit(e);
		}
		setStatus(TaskStatus.FINISHED);
	}

	private void createCombinedFeatures() {

		taskDescription = "Merging marked features";
		List<MsFeatureCluster> clustersWithMergeInput =  
				clusterSet.stream().
				filter(c -> c.getMarkedForMerge().size() > 1).
				collect(Collectors.toList());
		total = clustersWithMergeInput.size();
		processed = 0;
		mergedFeaturesMap = new HashMap<MsFeatureCluster,LibraryMsFeature>();
		for(MsFeatureCluster cluster : clustersWithMergeInput) {
			
			LibraryMsFeature merged = mergeMarkedFeatures(cluster);
			if(merged != null)
				mergedFeaturesMap.put(cluster, merged);

			processed++;
		}
	}
	
	private LibraryMsFeature mergeMarkedFeatures(MsFeatureCluster cluster) {

		LibraryMsFeature merged = new LibraryMsFeature();
		MassSpectrum spectrum = new MassSpectrum();
		double rt = 0.0;
		Range rtRange = null;
		for(MsFeature f : cluster.getMarkedForMerge()) {
			
			rt += f.getRetentionTime();
			if(rtRange == null)
				rtRange = f.getRtRange();
			else
				rtRange.extendRange(f.getRtRange());
		}
		rt = rt / cluster.getMarkedForMerge().size();
		merged.setRetentionTime(rt);
		merged.setRtRange(rtRange);

		DataPipeline mergeDp = cluster.getMergeDataPipeline();
		RangeBucket mzRangeBucket = 
				createMZRangeBucket(cluster.getMarkedForMerge());
		if(mzRangeBucket.getRangeSet().size() == 1) {
			
			Collection<MsPoint>adductPoints = new TreeSet<MsPoint>(MsUtils.mzSorter);
			cluster.getMarkedForMerge().stream().
				forEach(f -> adductPoints.addAll(f.getSpectrum().getMsPoints()));
			Collection<MsPoint> averagedSpectrum = 
					MsUtils.averageMassSpectrum(adductPoints, massWindow, massErrorType);
			Adduct ad = cluster.getMarkedForMerge().stream().
				map(f -> f.getSpectrum().getPrimaryAdduct()).
				findFirst().orElse(null);
			spectrum.addSpectrumForAdduct(ad, averagedSpectrum);
			merged.setSpectrum(spectrum);
		}
		if(mzRangeBucket.getRangeSet().size() > 1) {
			
			RangeBucket mzRangeBucketComplete = 
					createMZRangeBucket(cluster.getFeturesForDataPipeline(mergeDp));
			
		}		
		return merged;
	}
	
	private RangeBucket createMZRangeBucket(Collection<MsFeature>features) {
		
		RangeBucket mzRangeBucket = new RangeBucket();
		for(MsFeature f : features) {
			
			for(Adduct ad : f.getSpectrum().getAdducts()) {
				
				Range adductMzRange = MsUtils.createMassRange(
						f.getSpectrum().getMonoisotopicMzForAdduct(ad), massWindow, massErrorType);
				mzRangeBucket.addRange(adductMzRange);
			}
		}		
		return mzRangeBucket;
	}
	
	private void createMatricesForCombinedFeatures() {
		// TODO Auto-generated method stub
		
	}
	
	private void calculateCombinedFeaturesStats() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Task cloneTask() {
		return new CreateMergedFeaturesTask(
				currentExperiment, clusterSet, massWindow, massErrorType);
	}
}

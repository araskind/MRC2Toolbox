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
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
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
		mergedFeaturesMap = new HashMap<>();
		for(MsFeatureCluster cluster : clustersWithMergeInput) {
			
			LibraryMsFeature merged = mergeMarkedFeatures(cluster);
			if(merged != null)
				mergedFeaturesMap.put(cluster, merged);

			processed++;
		}
	}
	
	private LibraryMsFeature mergeMarkedFeatures(MsFeatureCluster cluster) {

		LibraryMsFeature merged = new LibraryMsFeature();
		MassSpectrum spectrum = null;
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
		RangeBucket mzRangeBucketComplete = 
				createMZRangeBucket(cluster.getFeturesForDataPipeline(mergeDp));
		MsFeature adductRef = getAdductReferenceFeature(
				cluster, mzRangeBucketComplete.getRangeSet().size());
		if(adductRef == null) {
			System.err.println("Can not assign adducts to unknown features in cluster " + cluster.toString());
			return null;
		}	
		RangeBucket mzRangeBucket = 
				createMZRangeBucket(cluster.getMarkedForMerge());	
		NavigableMap<Adduct,Collection<MsFeature>>adductFeatureMap = 
				mapFeaturesToAdductsByMZ(mzRangeBucket, adductRef, cluster.getMarkedForMerge());
		
		if(mzRangeBucket.getRangeSet().size() == 1) {
			
			Adduct adduct = adductFeatureMap.firstKey();
			Collection<MsPoint> averagedPoints = mergeSpectraForSingleAdduct(cluster.getMarkedForMerge());
			spectrum = new MassSpectrum();
			spectrum.addSpectrumForAdduct(adduct, averagedPoints);

		}
		if(mzRangeBucket.getRangeSet().size() > 1) {		

			spectrum = new MassSpectrum();
			for(Entry<Adduct,Collection<MsFeature>>afEntry : adductFeatureMap.entrySet()) {
				
				if(afEntry.getValue().size() == 1) {

					spectrum.addSpectrumForAdduct(afEntry.getKey(), 
							afEntry.getValue().iterator().next().getSpectrum().getMsPoints());
				}
				if(afEntry.getValue().size() > 1) {
					Collection<MsPoint> averagedPoints = mergeSpectraForSingleAdduct(afEntry.getValue());
					spectrum.addSpectrumForAdduct(afEntry.getKey(), averagedPoints);
				}
			}
		}
		if(spectrum != null && !spectrum.getAdducts().isEmpty()) {
			merged.setSpectrum(spectrum);
			return merged;
		}
		else
			return null;
	}
	
	private NavigableMap<Adduct,Collection<MsFeature>>mapFeaturesToAdductsByMZ(
			RangeBucket mzRangeBucket, 
			MsFeature adductRef, 
			Collection<MsFeature>featuresToMap){
		
		NavigableMap<Adduct,Collection<MsFeature>>adductFeatureMap = new TreeMap<>();
		for(Range mzRange : mzRangeBucket.getRangeSet()) {
			
			Adduct ad = adductRef.getSpectrum().getAdductInMzRage(mzRange);
			List<MsFeature> featuresInRange = featuresToMap.stream().
					filter(f -> mzRange.contains(f.getSpectrum().getMonoisotopicMz())).
					collect(Collectors.toList());
			if(ad != null && !featuresInRange.isEmpty())
				adductFeatureMap.put(ad, featuresInRange);
		}		
		return adductFeatureMap;	
	}
	
	private Collection<MsPoint> mergeSpectraForSingleAdduct(Collection<MsFeature>featuresToMerge) {

		Collection<MsPoint>adductPoints = new TreeSet<>(MsUtils.mzSorter);
		featuresToMerge.stream().
			forEach(f -> adductPoints.addAll(f.getSpectrum().getMsPoints()));
		return MsUtils.averageMassSpectrum(adductPoints, massWindow, massErrorType);
	}
	
	//	TODO This code is for the cases when feature not intended for merge has more tha one adduct
	//	Specifically, ProFinder output can be used to assign adducts to unknowns
	private MsFeature getAdductReferenceFeature(MsFeatureCluster cluster, int adductCount) {
		
		return cluster.getFeatures().stream().
				filter(f -> f.getSpectrum().getAdducts().size() == adductCount).
				findFirst().orElse(null);
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

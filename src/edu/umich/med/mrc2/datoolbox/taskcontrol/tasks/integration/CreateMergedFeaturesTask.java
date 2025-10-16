/*******************************************************************************
 *
 * (C) Copyright 2018-2025 MRC2 (http://mrc2.umich.edu).
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

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.ujmp.core.Matrix;
import org.ujmp.core.calculation.Calculation.Ret;

import edu.umich.med.mrc2.datoolbox.data.Adduct;
import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.DataPipelineAlignmentResults;
import edu.umich.med.mrc2.datoolbox.data.ExperimentalSample;
import edu.umich.med.mrc2.datoolbox.data.LibraryMsFeature;
import edu.umich.med.mrc2.datoolbox.data.MassSpectrum;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureCluster;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureStatisticalSummary;
import edu.umich.med.mrc2.datoolbox.data.MsPoint;
import edu.umich.med.mrc2.datoolbox.data.RangeBucket;
import edu.umich.med.mrc2.datoolbox.data.enums.MassErrorType;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.main.ReferenceSamplesManager;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.utils.ClusterUtils;
import edu.umich.med.mrc2.datoolbox.utils.DataSetUtils;
import edu.umich.med.mrc2.datoolbox.utils.MsUtils;
import edu.umich.med.mrc2.datoolbox.utils.Range;

public class CreateMergedFeaturesTask extends AbstractTask {
	
	private DataAnalysisProject currentExperiment;
	private DataPipelineAlignmentResults alignmentResults;
	private double massWindow;
	private MassErrorType massErrorType;

	private Matrix combinedDataMatrix;
	private Matrix mergedFeaturesDataMatrix;
	
	private Map<MsFeatureCluster,LibraryMsFeature>mergedFeaturesMap;
	
	private DescriptiveStatistics sampleDescriptiveStatistics;
	private DescriptiveStatistics pooledDescriptiveStatistics;
	private DescriptiveStatistics totalDescriptiveStatistics;

	public CreateMergedFeaturesTask(
			DataAnalysisProject currentExperiment, 
			DataPipelineAlignmentResults alignmentResults,
			double massWindow, 
			MassErrorType massErrorType) {
		super();
		this.currentExperiment = currentExperiment;
		this.alignmentResults = alignmentResults;
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
			subsetDataMatrixForCombinedFeatures();
		}
		catch (Exception e) {
			reportErrorAndExit(e);
		}
		try {
			calculateCombinedFeaturesStatsAndPopulateMergedDataMatrix();
		}
		catch (Exception e) {
			reportErrorAndExit(e);
		}
		try {
			calculateCorrelationMatricesForClusters();
		}
		catch (Exception e) {
			reportErrorAndExit(e);
		}
		setStatus(TaskStatus.FINISHED);
	}

	private void createCombinedFeatures() {

		taskDescription = "Merging marked features";
		List<MsFeatureCluster> clustersWithMergeInput =  
				alignmentResults.getClusters().stream().
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
			merged.createDefaultPrimaryIdentity();
			cluster.getMarkedForMerge().stream().
				forEach(f -> merged.getParentIdSet().add(((LibraryMsFeature)f).getParentFeatureId()));			
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
	
	private void subsetDataMatrixForCombinedFeatures() {
		
		taskDescription = "Creating data matrix for merged features";
		total = 100;
		processed = 0;
		DataPipeline mergePipeline = 
				mergedFeaturesMap.keySet().iterator().next().getMergeDataPipeline();
		Set<String> parentFeatureIds = mergedFeaturesMap.values().stream().
				flatMap(f -> f.getParentIdSet().stream()).collect(Collectors.toSet());
		List<MsFeature>parentFeatures = 
				currentExperiment.getMsFeaturesForDataPipeline(mergePipeline).stream().
					filter(f -> parentFeatureIds.contains(f.getId())).collect(Collectors.toList());				
		Set<DataFile>activeFiles = 
				DataSetUtils.getActiveFilesForPipelineAndDesignSubset(
						currentExperiment, mergePipeline,
						currentExperiment.getExperimentDesign().getActiveDesignSubset(), null);		
		Matrix completeDataMatrix = currentExperiment.getDataMatrixForDataPipeline(mergePipeline);
		combinedDataMatrix = DataSetUtils.subsetDataMatrix(completeDataMatrix,parentFeatures,activeFiles);
		processed = 100;
	}
	
	private void calculateCombinedFeaturesStatsAndPopulateMergedDataMatrix() {

		taskDescription = "Calculating statistics for combined features";
		total = mergedFeaturesMap.size();
		processed = 0;
		
		DataPipeline mergePipeline = 
				mergedFeaturesMap.keySet().iterator().next().getMergeDataPipeline();
		currentExperiment.
			getAveragedFeatureLibraryForDataPipeline(mergePipeline).
			addFeatures(mergedFeaturesMap.values());
		
		TreeSet<DataFile> pooledFiles = new TreeSet<>();
		TreeSet<DataFile> sampleFiles = new TreeSet<>();		
		populateDataMaps(pooledFiles, sampleFiles, mergePipeline);
		
		Set<DataFile>dataFiles = currentExperiment.getDataFilesForAcquisitionMethod(
				mergePipeline.getAcquisitionMethod());
		initMergedFeaturesDataMatrix(dataFiles);

		sampleDescriptiveStatistics = new DescriptiveStatistics();
		pooledDescriptiveStatistics = new DescriptiveStatistics();
		totalDescriptiveStatistics = new DescriptiveStatistics();
		
		long[] dataCoordinates = new long[2];
		long[] mergeDataCoordinates = new long[2];

		Set<String> parentFeatureIds = mergedFeaturesMap.values().stream().
				flatMap(f -> f.getParentIdSet().stream()).collect(Collectors.toSet());
		List<MsFeature>parentFeatures = currentExperiment.getMsFeaturesForDataPipeline(mergePipeline).stream().
				filter(f -> parentFeatureIds.contains(f.getId())).collect(Collectors.toList());
		for(Entry<MsFeatureCluster, LibraryMsFeature> mfe : mergedFeaturesMap.entrySet()) {
			
			mergeDataCoordinates[1] = mergedFeaturesDataMatrix.getColumnForLabel(mfe.getValue());
			
			sampleDescriptiveStatistics.clear();
			pooledDescriptiveStatistics.clear();
			totalDescriptiveStatistics.clear();
						
			for (DataFile df : dataFiles) {

				dataCoordinates[0] = combinedDataMatrix.getRowForLabel(df);
				mergeDataCoordinates[0] = mergedFeaturesDataMatrix.getRowForLabel(df);				
				if(dataCoordinates[0] == -1 || mergeDataCoordinates[0] == -1)
					continue;
								
				double value = 0.0d;
				for(String fid : mfe.getValue().getParentIdSet()) {
					
					MsFeature sourceFeature = parentFeatures.stream().
							filter(f -> f.getId().equals(fid)).findFirst().orElse(null);
					if(sourceFeature != null) {
						
						dataCoordinates[1] = combinedDataMatrix.getColumnForLabel(sourceFeature);
						value += combinedDataMatrix.getAsDouble(dataCoordinates);
					}
				}
				if(value > 0) {

					if (pooledFiles.contains(df))
						pooledDescriptiveStatistics.addValue(value);

					if (sampleFiles.contains(df))
						sampleDescriptiveStatistics.addValue(value);

					totalDescriptiveStatistics.addValue(value);					
					mergedFeaturesDataMatrix.setAsDouble(value, mergeDataCoordinates);
				}
			}
			createAndPopulateFeatureStatisticalSummary(mfe.getValue(), pooledFiles, sampleFiles);
			processed++;
		}
		currentExperiment.setActiveDataPipelineAlignmentResult(alignmentResults);
		alignmentResults.setMergedDataMatrix(mergedFeaturesDataMatrix);
	}
	
	private void initMergedFeaturesDataMatrix(Set<DataFile>dataFiles) {
		
		double[][] quantitativeMatrix = 
				new double[dataFiles.size()][mergedFeaturesMap.size()];
		mergedFeaturesDataMatrix = Matrix.Factory.linkToArray(quantitativeMatrix);
		
		LibraryMsFeature[] featuresArray = 
				mergedFeaturesMap.values().toArray(new LibraryMsFeature[mergedFeaturesMap.size()]);
		mergedFeaturesDataMatrix.setMetaDataDimensionMatrix(
				0, Matrix.Factory.linkToArray((Object[])featuresArray));
		
		DataFile[]fileArray = dataFiles.toArray(new DataFile[dataFiles.size()]);
		mergedFeaturesDataMatrix.setMetaDataDimensionMatrix(
				1, Matrix.Factory.linkToArray((Object[])fileArray).transpose(Ret.NEW));
		
		mergedFeaturesDataMatrix.setMetaDataDimensionMatrix(1, 
				combinedDataMatrix.getMetaDataDimensionMatrix(1));
	}

	private void createAndPopulateFeatureStatisticalSummary(
			MsFeature cf,
			Set<DataFile> pooledFiles,
			Set<DataFile> sampleFiles) {
		
		MsFeatureStatisticalSummary statSummary = cf.getStatsSummary();
		if(statSummary == null) {
			statSummary = new MsFeatureStatisticalSummary(cf);
			cf.setStatsSummary(statSummary);
		}
		if (!pooledFiles.isEmpty()) {

			statSummary.setPooledMean(pooledDescriptiveStatistics.getMean());
			statSummary.setPooledMedian(pooledDescriptiveStatistics.getPercentile(50.0d));
			statSummary.setPooledStDev(pooledDescriptiveStatistics.getStandardDeviation());
			statSummary.setPooledFrequency((double) pooledDescriptiveStatistics.getN() / (double)pooledFiles.size());
		}
		if (!sampleFiles.isEmpty()) {

			statSummary.setSampleMean(sampleDescriptiveStatistics.getMean());
			statSummary.setSampleMedian(sampleDescriptiveStatistics.getPercentile(50.0d));
			statSummary.setSampleStDev(sampleDescriptiveStatistics.getStandardDeviation());
			statSummary.setSampleFrequency((double) sampleDescriptiveStatistics.getN() / (double)sampleFiles.size());
		}
		statSummary.setTotalMedian(totalDescriptiveStatistics.getPercentile(50.0d));
	}
	
	private void populateDataMaps(
			Set<DataFile> pooledFiles,
			Set<DataFile> sampleFiles,
			DataPipeline mergePipeline) {
		
		Set<ExperimentalSample> pooledSamples = currentExperiment.getPooledSamples();
		Set<DataFile>dataFiles = currentExperiment.getDataFilesForAcquisitionMethod(
				mergePipeline.getAcquisitionMethod());
		for (DataFile df : dataFiles) {
			
			if(pooledSamples.contains(df.getParentSample()) && df.isEnabled())
				pooledFiles.add(df);

			if(df.getParentSample().hasLevel(ReferenceSamplesManager.sampleLevel) && df.isEnabled())
				sampleFiles.add(df);
		}
	}
	
	private void calculateCorrelationMatricesForClusters() {
		
		taskDescription = "Calculating statistics for combined features";
		total = mergedFeaturesMap.size();
		processed = 0;
		DataPipeline mergePipeline = mergedFeaturesMap.keySet().iterator().next().getMergeDataPipeline();
		for(Entry<MsFeatureCluster, LibraryMsFeature> mfe : mergedFeaturesMap.entrySet()) {
			
			MsFeatureCluster current = mfe.getKey();
			current.addFeature(mfe.getValue(), mergePipeline);
			ClusterUtils.createClusterCorrelationMatrixForMultiplePipelines(
					current, currentExperiment, mergedFeaturesDataMatrix, false);
			processed++;
		}
	}
	
	public Collection<MsFeatureCluster>getUpdatedFeatureClusters(){
		return mergedFeaturesMap.keySet();
	}

	@Override
	public Task cloneTask() {
		return new CreateMergedFeaturesTask(
				currentExperiment, alignmentResults, massWindow, massErrorType);
	}

	public DataPipelineAlignmentResults getAlignmentResults() {
		return alignmentResults;
	}
}

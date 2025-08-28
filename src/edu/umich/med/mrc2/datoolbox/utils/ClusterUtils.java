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

package edu.umich.med.mrc2.datoolbox.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ArrayUtils;
import org.ujmp.core.Matrix;
import org.ujmp.core.calculation.Calculation.Ret;

import edu.umich.med.mrc2.datoolbox.data.Adduct;
import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.ExperimentalSample;
import edu.umich.med.mrc2.datoolbox.data.LibraryMsFeature;
import edu.umich.med.mrc2.datoolbox.data.MassSpectrum;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureCluster;
import edu.umich.med.mrc2.datoolbox.data.compare.MsFeatureComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortDirection;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class ClusterUtils {

	public static MsFeature getMostIntensiveFeature(Collection<MsFeature> features) {
		return features.stream().
				sorted(new MsFeatureComparator(SortProperty.Area, SortDirection.DESC)).
				findFirst().orElse(null);
	}

	public static MsFeature getMostIntensiveFeature(MsFeatureCluster fcluster) {
		return ClusterUtils.getMostIntensiveFeature(fcluster.getFeatures());
	}
	
	public static MsFeature getMostIntensiveMsmsFeature(MsFeatureCluster fcluster) {
		
		if(fcluster.getFeatures().size() == 1)
			return fcluster.getFeatures().iterator().next();
		else
			return fcluster.getFeatures().stream().
				filter(f -> Objects.nonNull(f.getSpectrum())).
				filter(f -> Objects.nonNull(f.getSpectrum().getExperimentalTandemSpectrum())).
				sorted(new MsFeatureComparator(SortProperty.msmsIntensity, SortDirection.DESC)).
				findFirst().orElse(null);
	}

	public static Matrix createClusterCorrelationMatrix(MsFeatureCluster fcluster, boolean activeOnly) {

		Matrix dataMatrix = MRC2ToolBoxCore.getActiveMetabolomicsExperiment()
				.getDataMatrixForDataPipeline(MRC2ToolBoxCore.getActiveMetabolomicsExperiment().getActiveDataPipeline());

		if(dataMatrix == null)
			return null;

		Collection<MsFeature> sorted = null;
		Matrix corrMatrix = null;

		if (activeOnly)
			sorted = fcluster.getActiveFeatures();
		else
			sorted = fcluster.getFeatures().stream().
				sorted(new MsFeatureComparator(SortProperty.Name)).
				collect(Collectors.toList());
		long[] columnIndex =
				sorted.stream().
				map(f -> dataMatrix.getColumnForLabel(f)).
				mapToLong(i -> i).
				toArray();

		MsFeature[] sortedFeatures = sorted.toArray(new MsFeature[sorted.size()]);

		//	TODO	Select active samples - this is a temporary fix untill design subsets are implemented
		ArrayList<Long>rowList = new ArrayList<Long>();
		for(DataFile file : MRC2ToolBoxCore.getActiveMetabolomicsExperiment().getDataFilesForAcquisitionMethod(
				MRC2ToolBoxCore.getActiveMetabolomicsExperiment().getActiveDataPipeline().getAcquisitionMethod())) {

			if(file.isEnabled())
				rowList.add(dataMatrix.getRowForLabel(file));
		}
		long[] rowIndex = ArrayUtils.toPrimitive(rowList.toArray(new Long[rowList.size()]));
		Matrix raw = dataMatrix.select(Ret.LINK, rowIndex, columnIndex).replace(Ret.NEW, 0.0d, Double.NaN);
		corrMatrix = raw.corrcoef(Ret.LINK, true, false).replace(Ret.NEW, Double.NaN, 0.0d);
		corrMatrix.setMetaDataDimensionMatrix(0, Matrix.Factory.linkToArray((Object[])sortedFeatures));
		corrMatrix.setMetaDataDimensionMatrix(1, Matrix.Factory.linkToArray((Object[])sortedFeatures).transpose(Ret.NEW));
		return corrMatrix;
	}
	
	//	TODO make sure "enabledOnly" is enforced through every step
	public static Matrix createClusterCorrelationMatrixForMultiplePipelines(
			MsFeatureCluster fcluster,
			DataAnalysisProject experiment,
			boolean enabledOnly) {
		
		Set<DataPipeline> pipelines = fcluster.getFeatureMap().keySet();
		Set<ExperimentalSample>commonSamples = 
				DataSetUtils.getCommonSamplesForDataPipelines(experiment, pipelines, enabledOnly);
		Set<ExperimentalSample>commonSamplesClean = 
				selectSamplesWithSameCountOfDataFilesForEachPipeline(commonSamples, pipelines);
		List<Long>rowList = new ArrayList<>();
		List<Long>mergedRowList = new ArrayList<>();
		for(Entry<DataPipeline, Collection<MsFeature>> cfe : fcluster.getFeatureMap().entrySet()) {
			
			rowList.clear();
			mergedRowList.clear();
			
			Matrix dataMatrix = experiment.getDataMatrixForDataPipeline(cfe.getKey());
			commonSamplesClean.stream().
				flatMap(s -> s.getDataFilesForMethod(cfe.getKey().getAcquisitionMethod()).stream()).
				forEach(f -> rowList.add(dataMatrix.getRowForLabel(f)));
			
			Collection<MsFeature>allFeatures = cfe.getValue();
			//	Collect merged feature data
			Collection<LibraryMsFeature>mergedFeatures = 
					fcluster.getMergedFeaturesForDataPipeline(cfe.getKey());
			Matrix mergedMatrixForPipeline = null;
			if(!mergedFeatures.isEmpty()) {
				
				allFeatures.removeAll(mergedFeatures);				
				Matrix mergedDataMatrix = experiment.getMergedDataMatrixForDataPipeline(cfe.getKey());
				long[] mergedColumnIndex = mergedFeatures.stream().
						map(f -> mergedDataMatrix.getColumnForLabel(f)).
						mapToLong(i -> i).toArray();
				commonSamplesClean.stream().
					flatMap(s -> s.getDataFilesForMethod(cfe.getKey().getAcquisitionMethod()).stream()).
					forEach(f -> mergedRowList.add(mergedDataMatrix.getRowForLabel(f)));
				long[] mergedRowIndex = ArrayUtils.toPrimitive(mergedRowList.toArray(new Long[mergedRowList.size()]));
				mergedMatrixForPipeline = mergedDataMatrix.select(
						Ret.LINK, mergedRowIndex, mergedColumnIndex).replace(Ret.NEW, 0.0d, Double.NaN);
			}
			long[] columnIndex = allFeatures.stream().
					map(f -> dataMatrix.getColumnForLabel(f)).
					mapToLong(i -> i).toArray();
			commonSamplesClean.stream().
				flatMap(s -> s.getDataFilesForMethod(cfe.getKey().getAcquisitionMethod()).stream()).
				forEach(f -> rowList.add(dataMatrix.getRowForLabel(f)));
			long[] rowIndex = ArrayUtils.toPrimitive(rowList.toArray(new Long[rowList.size()]));
			Matrix matrixForPipeline = dataMatrix.select(
					Ret.LINK, rowIndex, columnIndex).replace(Ret.NEW, 0.0d, Double.NaN);
			if(mergedMatrixForPipeline != null) {
				
			}

		}
		
		
		return null;
	}
	
	public static Set<ExperimentalSample>selectSamplesWithSameCountOfDataFilesForEachPipeline(
			Set<ExperimentalSample>allSamples,
			Set<DataPipeline> pipelines){
		
		Set<ExperimentalSample>commonSamplesClean = new TreeSet<>();
		Set<Integer>fileCounts = new TreeSet<>();
		for(ExperimentalSample s : allSamples) {
			
			fileCounts.clear();
			pipelines.stream().
				filter(p -> !s.getDataFilesForMethod(p.getAcquisitionMethod()).isEmpty()).
				forEach(p -> fileCounts.add(s.getDataFilesForMethod(p.getAcquisitionMethod()).size()));
			if(fileCounts.size() == 1)
				commonSamplesClean.add(s);
		}		
		return commonSamplesClean;
	}
	
	public static LibraryMsFeature mergeLibraryFeatures(Map<LibraryMsFeature,Adduct>featureAdductMap) {
		
		LibraryMsFeature merged = new LibraryMsFeature();
		MassSpectrum spectrum = new MassSpectrum();
		double rt = 0.0;
		Range rtRange = null;
		for(Entry<LibraryMsFeature,Adduct>mapEntry : featureAdductMap.entrySet()) {

			LibraryMsFeature lf = mapEntry.getKey();
			Adduct ad = mapEntry.getValue();
			
//			Collection<MsPoint>scaledAdductPoints = MsUtils.scaleMsPointCollection(
//					mapEntry.getKey().getSpectrum().getMsPointsForAdduct(mapEntry.getValue()),
//					mapEntry.getKey().getStatsSummary().getSampleMedian());
			spectrum.addSpectrumForAdduct(ad, lf.getSpectrum().getMsPointsForAdduct(ad));
			rt += lf.getRetentionTime();
			if(rtRange == null)
				rtRange = mapEntry.getKey().getRtRange();
			else
				rtRange.extendRange(mapEntry.getKey().getRtRange());
			
			merged.getParentIdSet().add(lf.getId());
		}
		rt = rt / featureAdductMap.size();
		merged.setRetentionTime(rt);
		merged.setRtRange(rtRange);
		merged.createDefaultPrimaryIdentity();
	
		//	TODO merge identities removing duplicate compounds with the same ID confidence and source
		
		return merged;
	}
}
















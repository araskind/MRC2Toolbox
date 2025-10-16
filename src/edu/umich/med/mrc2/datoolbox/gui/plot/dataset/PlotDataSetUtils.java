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

package edu.umich.med.mrc2.datoolbox.gui.plot.dataset;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.ujmp.core.Matrix;

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignFactor;
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignLevel;
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignSubset;
import edu.umich.med.mrc2.datoolbox.data.ExperimentalSample;
import edu.umich.med.mrc2.datoolbox.data.LibraryMsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.compare.DataFileComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.ObjectCompatrator;
import edu.umich.med.mrc2.datoolbox.data.enums.DataScale;
import edu.umich.med.mrc2.datoolbox.data.enums.DataSetQcField;
import edu.umich.med.mrc2.datoolbox.data.enums.FileSortingOrder;
import edu.umich.med.mrc2.datoolbox.data.enums.PlotDataGrouping;
import edu.umich.med.mrc2.datoolbox.data.lims.DataAcquisitionMethod;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.plot.TwoDimDataPlotParameterObject;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.ReferenceSamplesManager;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.utils.DataSetUtils;
import edu.umich.med.mrc2.datoolbox.utils.NormalizationUtils;

public class PlotDataSetUtils {
	
	private PlotDataSetUtils() {}

	public static Map<DataFile,Double>getScaledPeakAreasForFeature(
			DataAnalysisProject experiment,
			MsFeature feature,
			DataPipeline pipeline,
			Collection<DataFile>files,
			DataScale scale){
		
		Map<DataFile,Double>dataMap = new HashMap<>();
		if(experiment == null || pipeline == null 
				|| feature == null || files.isEmpty())
			return dataMap;
		
		DataFile[] sampleFiles = files.toArray(new DataFile[files.size()]);
		long[] coordinates = new long[2];
		Matrix dataMatrix = null;
		if(feature instanceof LibraryMsFeature && ((LibraryMsFeature)feature).isMerged()) {
						
			dataMatrix = getActiveMergedDataMatrixFromProjecr(experiment);			
			if(dataMatrix == null)
				return dataMap;
			
			coordinates[1] = dataMatrix.getColumnForLabel(feature);
			if(coordinates[1] == -1)
				return dataMap;
		}
		else {
			dataMatrix = experiment.getDataMatrixForDataPipeline(pipeline);
			coordinates[1] = DataSetUtils.getColumnForFeature(
					dataMatrix, feature, experiment, pipeline);
			if(coordinates[1] == -1)
				return dataMap;
		}
		double[] fdata = new double[files.size()];
		for (int i = 0; i < sampleFiles.length; i++) {

			coordinates[0] = dataMatrix.getRowForLabel(sampleFiles[i]);
			if(coordinates[0] >= 0)
				fdata[i] = dataMatrix.getAsDouble(coordinates);
		}
		double[] scaledData = NormalizationUtils.scaleData(fdata, scale);
		for(int i=0; i<scaledData.length; i++) {

			if(fdata[i] > 0)
				dataMap.put(sampleFiles[i], scaledData[i]);
			else
				dataMap.put(sampleFiles[i], null);
		}
		return dataMap;
	}
	
	public static Matrix getActiveMergedDataMatrixFromProjecr(DataAnalysisProject experiment) {
		
		if(experiment.getActiveDataIntegrationSet() == null)
			return null;
		else
			return experiment.getActiveDataIntegrationSet().getMergedDataMatrix();
	}
	
	public static Map<String, DataFile[]> createSeriesFileMapForActiveData(
			TwoDimDataPlotParameterObject plotParameters){
		
		DataAnalysisProject experiment = MRC2ToolBoxCore.getActiveMetabolomicsExperiment();
		ExperimentDesignSubset activeDesign = null;
		if(experiment.getExperimentDesign() != null)
			activeDesign = experiment.getExperimentDesign().getActiveDesignSubset();
		
		return createSeriesFileMap(
				experiment,
				experiment.getActiveDataPipeline(),
				plotParameters.getSortingOrder(),
				activeDesign,
				plotParameters.getGroupingType(),
				plotParameters.getCategory(),
				plotParameters.getSubCategory());
	}
	
	public static Map<String, DataFile[]> createSeriesFileMap(
			TwoDimDataPlotParameterObject plotParameters){
		
		return createSeriesFileMap(
				plotParameters.getExperiment(),
				plotParameters.getPipeline(),
				plotParameters.getSortingOrder(),
				plotParameters.getActiveDesign(),
				plotParameters.getGroupingType(),
				plotParameters.getCategory(),
				plotParameters.getSubCategory());
	}
	
	public static Map<String, DataFile[]> createSeriesFileMap(
			DataAnalysisProject experiment,
			DataPipeline pipeline,
			ExperimentDesignSubset activeDesign,
			TwoDimDataPlotParameterObject plotParameters){
		return createSeriesFileMap(
				experiment,
				pipeline,
				plotParameters.getSortingOrder(),
				activeDesign,
				plotParameters.getGroupingType(),
				plotParameters.getCategory(),
				plotParameters.getSubCategory());
	}

	public static Map<String, DataFile[]> createSeriesFileMap(
			DataAnalysisProject experiment,
			DataPipeline pipeline,
			FileSortingOrder sortingOrder,
			ExperimentDesignSubset activeDesign,
			PlotDataGrouping groupingType,
			ExperimentDesignFactor category,
			ExperimentDesignFactor subCategory) {

		if (experiment == null || pipeline == null || activeDesign == null
				|| experiment.getExperimentDesign() == null
				|| experiment.getExperimentDesign().getSamples().isEmpty())
			return null;

		Map<String, DataFile[]> dataFileMap = new LinkedHashMap<>();
		if(groupingType.equals(PlotDataGrouping.IGNORE_DESIGN)) {
			return mapSeriesIgnoreDesign(experiment, pipeline, sortingOrder, activeDesign);
		}
		else {
			Collection<ExperimentalSample> samples =
					experiment.getExperimentDesign().getSamplesForDesignSubset(activeDesign, true);

			Set<Set<ExperimentDesignLevel>>seriesSet =
					createExpLevelSeries(activeDesign, groupingType, category, subCategory);

			Map<String, TreeSet<DataFile>> dataFileMapInterm =  new LinkedHashMap<>();
			for(Set<ExperimentDesignLevel>levelSet : seriesSet) {

				List<String> labList = levelSet.stream().
						map(l -> l.getName()).collect(Collectors.toList());
				String seriesLabel = StringUtils.join(labList, "\n");
				dataFileMapInterm.put(seriesLabel, new TreeSet<>());

				for(ExperimentalSample s : samples) {

					Set<DataFile>activeFiles = 
							s.getDataFilesForMethod(pipeline.getAcquisitionMethod()).stream().
							filter(f -> f.isEnabled()).collect(Collectors.toSet());
					if(!activeFiles.isEmpty() 
							&& s.getDesignCell().values().containsAll(levelSet))
						dataFileMapInterm.get(seriesLabel).addAll(activeFiles);
				}
			}
			for (Entry<String, TreeSet<DataFile>> entry : dataFileMapInterm.entrySet()) {
				
				if(!entry.getValue().isEmpty()) {
					dataFileMap.put(entry.getKey(), entry.getValue().stream().
							sorted(new DataFileComparator(sortingOrder)).
							toArray(size -> new DataFile[size]));
				}
			}
		}
		return dataFileMap;
	}

	public static Set<Set<ExperimentDesignLevel>>createExpLevelSeries(
			ExperimentDesignSubset activeDesign,
			PlotDataGrouping groupingType,
			ExperimentDesignFactor category,
			ExperimentDesignFactor subCategory){

		Set<Set<ExperimentDesignLevel>>seriesSet = new LinkedHashSet<>();
		Set<Set<ExperimentDesignLevel>>filteredSeriesSet = new LinkedHashSet<>();

		if(groupingType.equals(PlotDataGrouping.EACH_FACTOR)) {

			for(ExperimentDesignFactor factor : activeDesign.getOrderedDesign().keySet())
				seriesSet = addFactorToSeries(seriesSet, activeDesign, factor);
		}
		if(groupingType.equals(PlotDataGrouping.ONE_FACTOR) ||
				(groupingType.equals(PlotDataGrouping.TWO_FACTORS) && category.equals(subCategory))) {

			seriesSet = addFactorToSeries(seriesSet, activeDesign, category);
		}
		if(groupingType.equals(PlotDataGrouping.TWO_FACTORS) && !category.equals(subCategory)) {

			seriesSet = addFactorToSeries(seriesSet, activeDesign, category);
			seriesSet = addFactorToSeries(seriesSet, activeDesign, subCategory);
		}
		//	Add reference samples
		ExperimentDesignFactor refFactor = 
				ReferenceSamplesManager.getSampleControlTypeFactor();
		if(activeDesign.getOrderedDesign().keySet().contains(refFactor)) {
			
			ExperimentDesignLevel[]refLevels = activeDesign.getOrderedDesign().get(refFactor);
			for(ExperimentDesignLevel rl : refLevels) {	
				
				if(!rl.equals(ReferenceSamplesManager.sampleLevel))
					seriesSet.add(Set.of(rl));
			}
		}
		return seriesSet;
	}

	public static Set<Set<ExperimentDesignLevel>>addFactorToSeries(
			Set<Set<ExperimentDesignLevel>>seriesSet,
			ExperimentDesignSubset activeDesign,
			ExperimentDesignFactor factorToAdd){

		if(seriesSet.isEmpty()) {

			for(ExperimentDesignLevel l : activeDesign.getOrderedDesign().get(factorToAdd)) {

				Set<ExperimentDesignLevel>levelSet = new LinkedHashSet<>();
				levelSet.add(l);
				seriesSet.add(levelSet);
			}
		}
		else {
			Set<Set<ExperimentDesignLevel>>seriesSetUpdated = new LinkedHashSet<>();

			for(ExperimentDesignLevel l : activeDesign.getOrderedDesign().get(factorToAdd)) {

				for(Set<ExperimentDesignLevel>levelSet : seriesSet) {

					Set<ExperimentDesignLevel>newLset = new LinkedHashSet<>(levelSet);
					newLset.add(l);
					seriesSetUpdated.add(newLset);
				}
			}
			seriesSet = seriesSetUpdated;
		}
		return seriesSet;
	}

	public static Map<String, DataFile[]> mapSeriesIgnoreDesign(
			DataAnalysisProject experiment,
			DataPipeline pipeline,
			FileSortingOrder sortingOrder,
			ExperimentDesignSubset activeDesign){

		if (experiment == null || pipeline == null || activeDesign == null
				|| experiment.getExperimentDesign() == null 
				|| experiment.getExperimentDesign().getSamples().isEmpty())
			return null;
		
		Collection<DataFile> sorted = DataSetUtils.sortFiles(
				experiment, pipeline, sortingOrder, activeDesign);
		if(sorted.isEmpty())
			return null;
		
		Collection<ExperimentalSample> samples = 
				experiment.getExperimentDesign().getSamplesForDesignSubset(activeDesign, true);
		if(samples.isEmpty())
			return null;
		
		Map<String, DataFile[]> dataFileMap = new LinkedHashMap<>();

		if(sortingOrder.equals(FileSortingOrder.SAMPLE_ID) || 
				sortingOrder.equals(FileSortingOrder.SAMPLE_NAME)) {

			LinkedHashMap<ExperimentalSample, ArrayList<DataFile>>sfMap = new LinkedHashMap<>();

			for(DataFile df : sorted) {

				if(!sfMap.containsKey(df.getParentSample()))
					sfMap.put(df.getParentSample(), new ArrayList<>());

				sfMap.get(df.getParentSample()).add(df);
			}
			if(sortingOrder.equals(FileSortingOrder.SAMPLE_ID))
				sfMap.forEach((k,v)->dataFileMap.put(k.getId(), v.toArray(new DataFile[v.size()])));

			if(sortingOrder.equals(FileSortingOrder.SAMPLE_NAME))
				sfMap.forEach((k,v)->dataFileMap.put(k.getName(), v.toArray(new DataFile[v.size()])));
		}
		else {
			for(DataFile df : sorted)
				dataFileMap.put(df.getName(), new DataFile[] {df});
		}
		return dataFileMap;
	}
	
	public static Map<String, DataFile[]> mapSeriesBySampleType(
			DataAnalysisProject experiment,
			DataPipeline pipeline,
			FileSortingOrder sortingOrder,
			ExperimentDesignSubset activeDesign){
		
		if (experiment == null || pipeline == null || activeDesign == null 
				|| experiment.getExperimentDesign() == null 
				|| experiment.getExperimentDesign().getSamples().isEmpty())
			return null;
		
		ExperimentDesignFactor stf = experiment.getExperimentDesign().getSampleTypeFactor();
		Collection<ExperimentDesignLevel> sampleTypes = activeDesign.getLevelsForFactor(stf);
		TreeSet<ExperimentalSample> activeSamples = 
				experiment.getExperimentDesign().getActiveSamplesForDesignSubset(activeDesign);
		DataAcquisitionMethod acqMethod = pipeline.getAcquisitionMethod();
		ObjectCompatrator<DataFile>sorter = new DataFileComparator(sortingOrder);
		Map<String, DataFile[]> dataFileMap = new LinkedHashMap<>();
		for(ExperimentDesignLevel l : sampleTypes) {

			Set<ExperimentalSample> samplesOfType = 
					activeSamples.stream().
					filter(s -> s.getSampleType().equals(l)).
					collect(Collectors.toSet());
			if(!samplesOfType.isEmpty()) {
				
				Set<DataFile> filesOfType = samplesOfType.stream().
						flatMap(s -> s.getDataFilesForMethod(acqMethod).stream()).
						filter(f -> f.isEnabled()).sorted(sorter).
						collect(Collectors.toSet());
				if(!filesOfType.isEmpty())					
					dataFileMap.put(l.getName(), filesOfType.toArray(new DataFile[filesOfType.size()]));				
			}
		}
		return dataFileMap;
	}
	
	public static Map<DataFile,String> mapFilesBySampleType(
			DataAnalysisProject experiment,
			DataPipeline pipeline,
			ExperimentDesignSubset activeDesign){
		
		if (experiment == null || pipeline == null || activeDesign == null 
				|| experiment.getExperimentDesign() == null 
				|| experiment.getExperimentDesign().getSamples().isEmpty())
			return null;
		
		ExperimentDesignFactor stf = experiment.getExperimentDesign().getSampleTypeFactor();
		Collection<ExperimentDesignLevel> sampleTypes = activeDesign.getLevelsForFactor(stf);
		TreeSet<ExperimentalSample> activeSamples = 
				experiment.getExperimentDesign().getActiveSamplesForDesignSubset(activeDesign);
		DataAcquisitionMethod acqMethod = pipeline.getAcquisitionMethod();
		
		final Map<DataFile, String> fileTypeMap =  new HashMap<>();
		for(ExperimentDesignLevel l : sampleTypes) {

			Set<ExperimentalSample> samplesOfType = 
					activeSamples.stream().
					filter(s -> s.getSampleType().equals(l)).
					collect(Collectors.toSet());
			if(!samplesOfType.isEmpty()) {
				
				Set<DataFile> filesOfType = samplesOfType.stream().
						flatMap(s -> s.getDataFilesForMethod(acqMethod).stream()).
						filter(f -> f.isEnabled()).
						collect(Collectors.toSet());
				if(!filesOfType.isEmpty())					
					filesOfType.stream().forEach(f -> fileTypeMap.put(f, l.getName()));				
			}
		}		
		return fileTypeMap;
	}

	
	public static NumberFormat getNumberFormatForDataSetQcField (DataSetQcField field) {
		
		if(field.equals(DataSetQcField.RSD) 
				|| field.equals(DataSetQcField.RSD_TRIM)) {
			return NumberFormat.getNumberInstance();
		}
		else if(field.equals(DataSetQcField.OBSERVATIONS) 
				|| field.equals(DataSetQcField.MISSING)
				|| field.equals(DataSetQcField.OUTLIERS)) {
			return NumberFormat.getIntegerInstance();
		}
		else		
			return MRC2ToolBoxConfiguration.getIntensityFormat();
	}
}









































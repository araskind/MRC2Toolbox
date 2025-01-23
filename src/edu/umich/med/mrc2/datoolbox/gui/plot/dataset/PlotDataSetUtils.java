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
import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.ujmp.core.Matrix;

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignFactor;
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignLevel;
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignSubset;
import edu.umich.med.mrc2.datoolbox.data.ExperimentalSample;
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
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.utils.DataSetUtils;
import edu.umich.med.mrc2.datoolbox.utils.NormalizationUtils;

public class PlotDataSetUtils {

	public static Map<DataFile,Double>getScaledPeakAreasForFeature(
			DataAnalysisProject experiment,
			MsFeature feature,
			DataPipeline pipeline,
			Collection<DataFile>files,
			DataScale scale){
		
		if(experiment == null || pipeline == null || feature == null || files.isEmpty())
			return null;

		Map<DataFile,Double>dataMap = new HashMap<DataFile,Double>();
		DataFile[] sampleFiles = files.toArray(new DataFile[files.size()]);
		Matrix dataMatrix = experiment.getDataMatrixForDataPipeline(pipeline);

		long[] coordinates = new long[2];
		coordinates[1] = dataMatrix.getColumnForLabel(feature);
		double[] fdata = new double[files.size()];
		double[] scaledData = new double[files.size()];

		for (int i = 0; i < sampleFiles.length; i++) {

			coordinates[0] = dataMatrix.getRowForLabel(sampleFiles[i]);
			if(coordinates[0] >= 0)
				fdata[i] = dataMatrix.getAsDouble(coordinates);
		}
		if (scale.equals(DataScale.RAW))
			scaledData = fdata;

		if (scale.equals(DataScale.LN)) {

			for (int i = 0; i < fdata.length; i++) {

				double ln = Math.log(fdata[i]);

				if (Double.isFinite(ln))
					scaledData[i] = ln;
				else
					scaledData[i] = 0.01d;
			}
		}
		if (scale.equals(DataScale.LOG10)) {

			for (int i = 0; i < fdata.length; i++) {

				double ln = Math.log10(fdata[i]);

				if (Double.isFinite(ln))
					scaledData[i] = ln;
				else
					scaledData[i] = 0.01d;
			}
		}
		if (scale.equals(DataScale.SQRT)) {
			
			for (int i = 0; i < fdata.length; i++)
				scaledData[i] = Math.sqrt(fdata[i]);
		}
		if (scale.equals(DataScale.RANGE))
			scaledData = rangeScale(fdata, 0.0d, 100.0d);

		if (scale.equals(DataScale.ZSCORE))
			scaledData = StatUtils.normalize(fdata);
		
		if (scale.equals(DataScale.PARETO))
			scaledData = NormalizationUtils.paretoScale(fdata);
		
		for(int i=0; i<scaledData.length; i++) {

			if(fdata[i] > 0)
				dataMap.put(sampleFiles[i], scaledData[i]);
			else
				dataMap.put(sampleFiles[i], null);
		}
		return dataMap;
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

		Map<String, DataFile[]> dataFileMap = 
				new LinkedHashMap<String, DataFile[]>();

		if(groupingType.equals(PlotDataGrouping.IGNORE_DESIGN)) {

			return mapSeriesIgnoreDesign(experiment, pipeline, sortingOrder, activeDesign);
		}
		else {
			Collection<ExperimentalSample> samples =
					experiment.getExperimentDesign().getSamplesForDesignSubset(activeDesign, true);

			Set<Set<ExperimentDesignLevel>>seriesSet =
					createExpLevelSeries(activeDesign, groupingType, category, subCategory);

			Map<String, TreeSet<DataFile>> dataFileMapInterm = 
					new LinkedHashMap<String, TreeSet<DataFile>>();

			for(Set<ExperimentDesignLevel>levelSet : seriesSet) {

				List<String> labList = levelSet.stream().
						map(l -> l.getName()).collect(Collectors.toList());
				String seriesLabel = StringUtils.join(labList, "\n");
				dataFileMapInterm.put(seriesLabel, new TreeSet<DataFile>());

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

		Set<Set<ExperimentDesignLevel>>seriesSet = 
				new LinkedHashSet<Set<ExperimentDesignLevel>>();
		Set<Set<ExperimentDesignLevel>>filteredSeriesSet = 
				new LinkedHashSet<Set<ExperimentDesignLevel>>();

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
		//	Filter level combinations to exclude non-existent
//		Collection<ExperimentalSample> samples =
//				MRC2ToolBoxCore.getActiveMetabolomicsExperiment().
//					getExperimentDesign().getSamplesForDesignSubset(activeDesign, true);
//
//		for(Set<ExperimentDesignLevel>levelSet : seriesSet) {
//
//			for(ExperimentalSample s : samples) {
//
//				if(s.getDesignCell().values().containsAll(levelSet))
//					filteredSeriesSet.add(levelSet);
//			}
//		}
//		return filteredSeriesSet;
		
		return seriesSet;
	}

	public static Set<Set<ExperimentDesignLevel>>addFactorToSeries(
			Set<Set<ExperimentDesignLevel>>seriesSet,
			ExperimentDesignSubset activeDesign,
			ExperimentDesignFactor factorToAdd){

		if(seriesSet.isEmpty()) {

			for(ExperimentDesignLevel l : activeDesign.getOrderedDesign().get(factorToAdd)) {

				Set<ExperimentDesignLevel>levelSet = 
						new LinkedHashSet<ExperimentDesignLevel>();
				levelSet.add(l);
				seriesSet.add(levelSet);
			}
		}
		else {
			Set<Set<ExperimentDesignLevel>>seriesSetUpdated = 
					new LinkedHashSet<Set<ExperimentDesignLevel>>();

			for(ExperimentDesignLevel l : activeDesign.getOrderedDesign().get(factorToAdd)) {

				for(Set<ExperimentDesignLevel>levelSet : seriesSet) {

					Set<ExperimentDesignLevel>newLset = 
							new LinkedHashSet<ExperimentDesignLevel>(levelSet);
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
		
		Map<String, DataFile[]> dataFileMap = new LinkedHashMap<String, DataFile[]>();

		if(sortingOrder.equals(FileSortingOrder.SAMPLE_ID) || 
				sortingOrder.equals(FileSortingOrder.SAMPLE_NAME)) {

			LinkedHashMap<ExperimentalSample, ArrayList<DataFile>>sfMap = 
					new LinkedHashMap<ExperimentalSample, ArrayList<DataFile>>();

			for(DataFile df : sorted) {

				if(!sfMap.containsKey(df.getParentSample()))
					sfMap.put(df.getParentSample(), new ArrayList<DataFile>());

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
		Map<String, DataFile[]> dataFileMap = new LinkedHashMap<String, DataFile[]>();
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
		
		final Map<DataFile, String> fileTypeMap =  new HashMap<DataFile,String>();
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

	public static double[] rangeScale(double[] input, double min, double max) throws IllegalArgumentException {

		if (min >= max)
			throw new IllegalArgumentException("Max value should be larger than min value!");

		double[] output = new double[input.length];
		double range = max - min;

		DescriptiveStatistics stats = new DescriptiveStatistics(input);
		double minRaw = stats.getMin();
		double rangeRaw = stats.getMax() - minRaw;

		for (int i = 0; i < input.length; i++)
			output[i] = range * (input[i] - minRaw) / rangeRaw + min;

		return output;
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









































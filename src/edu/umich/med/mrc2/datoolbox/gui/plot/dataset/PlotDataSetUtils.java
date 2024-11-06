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

import edu.umich.med.mrc2.datoolbox.data.Assay;
import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignFactor;
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignLevel;
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignSubset;
import edu.umich.med.mrc2.datoolbox.data.ExperimentalSample;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.compare.DataFileComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.DataFileTimeStampComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.ObjectCompatrator;
import edu.umich.med.mrc2.datoolbox.data.enums.DataScale;
import edu.umich.med.mrc2.datoolbox.data.enums.DataSetQcField;
import edu.umich.med.mrc2.datoolbox.data.enums.FileSortingOrder;
import edu.umich.med.mrc2.datoolbox.data.enums.PlotDataGrouping;
import edu.umich.med.mrc2.datoolbox.data.lims.DataAcquisitionMethod;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.utils.DataSetUtils;
import edu.umich.med.mrc2.datoolbox.utils.NormalizationUtils;

public class PlotDataSetUtils {

	//	TODO check if used in the project
	public static Map<Assay, DataFile[]> createDataFileMap(
			MsFeature[] selectedFeatures,
			FileSortingOrder fileSortingOrder,
			ExperimentDesignSubset activeDesign) {

		Map<Assay, DataFile[]> dataFileMap = new HashMap<Assay, DataFile[]>();
		DataAnalysisProject experiment = 
				MRC2ToolBoxCore.getActiveMetabolomicsExperiment();

//		if(experiment != null) {
//
//			for(MsFeature f : selectedFeatures) {
//
//				//	TODO
//				if(dataFileMap.get(f.getAssayMethod()) == null) {
//
//					DataFile[] sortedFiles = 
//							DataSetUtils.sortFiles(f.getAssayMethod(), fileSortingOrder, activeDesign);
//					dataFileMap.put(f.getAssayMethod(), sortedFiles);
//				}
//			}
//		}
//		return dataFileMap;
		
		return null;
	}

	public static Map<DataFile,Double>getNormalizedDataForFeature(
			DataAnalysisProject experiment,
			MsFeature feature,
			DataPipeline pipeline,
			Collection<DataFile>files,
			DataScale scale){

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

	public static Map<String, DataFile[]> createSeriesFileMap(
			DataPipeline pipeline,
			Collection<DataFile>files,
			FileSortingOrder sortingOrder,
			ExperimentDesignSubset activeDesign,
			PlotDataGrouping groupingType,
			ExperimentDesignFactor category,
			ExperimentDesignFactor subCategory) {

		if (MRC2ToolBoxCore.getActiveMetabolomicsExperiment() == null)
			return null;

		if (MRC2ToolBoxCore.getActiveMetabolomicsExperiment().getExperimentDesign() == null)
			return null;

		if (MRC2ToolBoxCore.getActiveMetabolomicsExperiment().getExperimentDesign().getSamples().isEmpty())
			return null;

		Map<String, DataFile[]> dataFileMap = new LinkedHashMap<String, DataFile[]>();

		if(groupingType.equals(PlotDataGrouping.IGNORE_DESIGN)) {

//			if(splitByBatch)
//				return PlotDataSetUtils.mapSeriesByBatch(pipeline, sortingOrder, activeDesign);
//			else
				return PlotDataSetUtils.mapSeriesIgnoreDesign(pipeline, sortingOrder, activeDesign);
		}
		else {
			Collection<ExperimentalSample> samples =
					MRC2ToolBoxCore.getActiveMetabolomicsExperiment().getExperimentDesign().getSamplesForDesignSubset(activeDesign);

			Set<Set<ExperimentDesignLevel>>seriesSet =
					PlotDataSetUtils.createExpLevelSeries(activeDesign, groupingType, category, subCategory);

			Map<String, ArrayList<DataFile>> dataFileMapInterm = new LinkedHashMap<String, ArrayList<DataFile>>();

			for(Set<ExperimentDesignLevel>levelSet : seriesSet) {

				List<String> labList = levelSet.stream().map(l -> l.getName()).collect(Collectors.toList());
				String seriesLabel = StringUtils.join(labList, "\n");
				dataFileMapInterm.put(seriesLabel, new ArrayList<DataFile>());

				for(ExperimentalSample s : samples) {

					if(s.getDesignCell().values().containsAll(levelSet))
						dataFileMapInterm.get(seriesLabel).addAll(
								s.getDataFilesForMethod(pipeline.getAcquisitionMethod()));
				}
			}
//			if(splitByBatch) {
//
//				for (Entry<String, ArrayList<DataFile>> entry : dataFileMapInterm.entrySet()) {
//
//					int[] batches = entry.getValue().stream().mapToInt(f -> f.getBatchNumber()).distinct().sorted().toArray();
//					for(int batch : batches) {
//
//						String batchSeriesKey = entry.getKey() + "\nBatch " + Integer.toString(batch);
//
//						if (sortingOrder.equals(FileSortingOrder.NAME))
//							dataFileMap.put(batchSeriesKey,
//									entry.getValue().stream().filter(f -> f.getBatchNumber() == batch).sorted()
//											.toArray(size -> new DataFile[size]));
//
//						if (sortingOrder.equals(FileSortingOrder.TIMESTAMP))
//							dataFileMap.put(batchSeriesKey,
//									entry.getValue().stream().filter(f -> f.getBatchNumber() == batch)
//											.sorted(new DataFileTimeStampComparator())
//											.toArray(size -> new DataFile[size]));
//					}
//				}
//			}
//			else {
				for (Entry<String, ArrayList<DataFile>> entry : dataFileMapInterm.entrySet()) {

					if(sortingOrder.equals(FileSortingOrder.NAME))
						dataFileMap.put(entry.getKey(), entry.getValue().stream().sorted().toArray(size -> new DataFile[size]));

					if(sortingOrder.equals(FileSortingOrder.TIMESTAMP))
						dataFileMap.put(entry.getKey(), entry.getValue().stream().
								sorted(new DataFileTimeStampComparator()).
								toArray(size -> new DataFile[size]));
				}
//			}
		}
		return dataFileMap;
	}

	private static Map<String, DataFile[]> mapSeriesByBatch(
			DataPipeline pipeline,
			FileSortingOrder sortingOrder,
			ExperimentDesignSubset activeDesign) {

		if (MRC2ToolBoxCore.getActiveMetabolomicsExperiment() == null)
			return null;

		if (MRC2ToolBoxCore.getActiveMetabolomicsExperiment().getExperimentDesign() == null)
			return null;

		if (MRC2ToolBoxCore.getActiveMetabolomicsExperiment().getExperimentDesign().getSamples().isEmpty())
			return null;

		Map<String, DataFile[]> dataFileMap = new LinkedHashMap<String, DataFile[]>();
		DataFile[] sorted = DataSetUtils.sortFiles(pipeline, sortingOrder, activeDesign);
		DataAnalysisProject experiment = MRC2ToolBoxCore.getActiveMetabolomicsExperiment();
		Collection<ExperimentalSample> samples = 
				experiment.getExperimentDesign().getSamplesForDesignSubset(activeDesign);
		int[] batches = experiment.getDataFilesForAcquisitionMethod(pipeline.getAcquisitionMethod()).
				stream().mapToInt(f -> f.getBatchNumber()).distinct().sorted().toArray();

		if(sortingOrder.equals(FileSortingOrder.SAMPLE_ID) || sortingOrder.equals(FileSortingOrder.SAMPLE_NAME)) {

			for(int batch : batches) {

				LinkedHashMap<ExperimentalSample, ArrayList<DataFile>>sfMap = 
						new LinkedHashMap<ExperimentalSample, ArrayList<DataFile>>();

				for(DataFile df : sorted) {

					for(ExperimentalSample sample : samples) {

						if(sample.hasDataFile(df) && df.getBatchNumber() == batch) {

							if(!sfMap.containsKey(sample))
								sfMap.put(sample, new ArrayList<DataFile>());

							sfMap.get(sample).add(df);
						}
					}
				}
				if(sortingOrder.equals(FileSortingOrder.SAMPLE_ID))
					sfMap.forEach((k,v)->dataFileMap.put(k.getId() + "\nBatch" + 
							Integer.toString(batch), v.toArray(new DataFile[v.size()])));

				if(sortingOrder.equals(FileSortingOrder.SAMPLE_NAME))
					sfMap.forEach((k,v)->dataFileMap.put(k.getName() + "\nBatch" + 
							Integer.toString(batch), v.toArray(new DataFile[v.size()])));
			}
		}
		else {
			for(int batch : batches) {

				for(DataFile df : sorted) {

					if(df.getBatchNumber() == batch)
						dataFileMap.put(df.getName() + "\nBatch" + 
								Integer.toString(batch), new DataFile[] {df});
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

		Set<Set<ExperimentDesignLevel>>seriesSet = new LinkedHashSet<Set<ExperimentDesignLevel>>();
		Set<Set<ExperimentDesignLevel>>filteredSeriesSet = new LinkedHashSet<Set<ExperimentDesignLevel>>();

		if(groupingType.equals(PlotDataGrouping.EACH_FACTOR)) {

			for(ExperimentDesignFactor factor : activeDesign.getOrderedDesign().keySet())
				seriesSet = PlotDataSetUtils.addFactorToSeries(seriesSet, activeDesign, factor);
		}
		if(groupingType.equals(PlotDataGrouping.ONE_FACTOR) ||
				(groupingType.equals(PlotDataGrouping.TWO_FACTORS) && category.equals(subCategory))) {

			seriesSet = PlotDataSetUtils.addFactorToSeries(seriesSet, activeDesign, category);
		}
		if(groupingType.equals(PlotDataGrouping.TWO_FACTORS) && !category.equals(subCategory)) {

			seriesSet = PlotDataSetUtils.addFactorToSeries(seriesSet, activeDesign, category);
			seriesSet = PlotDataSetUtils.addFactorToSeries(seriesSet, activeDesign, subCategory);
		}
		//	Filter level combinations to exclude non-existent
		Collection<ExperimentalSample> samples =
				MRC2ToolBoxCore.getActiveMetabolomicsExperiment().
					getExperimentDesign().getSamplesForDesignSubset(activeDesign);

		for(Set<ExperimentDesignLevel>levelSet : seriesSet) {

			for(ExperimentalSample s : samples) {

				if(s.getDesignCell().values().containsAll(levelSet))
					filteredSeriesSet.add(levelSet);
			}
		}
		return filteredSeriesSet;
	}

	public static Set<Set<ExperimentDesignLevel>>addFactorToSeries(
			Set<Set<ExperimentDesignLevel>>seriesSet,
			ExperimentDesignSubset activeDesign,
			ExperimentDesignFactor factorToAdd){

		if(seriesSet.isEmpty()) {

			for(ExperimentDesignLevel l : activeDesign.getOrderedDesign().get(factorToAdd)) {

				Set<ExperimentDesignLevel>levelSet = new LinkedHashSet<ExperimentDesignLevel>();
				levelSet.add(l);
				seriesSet.add(levelSet);
			}
		}
		else {
			Set<Set<ExperimentDesignLevel>>seriesSetUpdated = new LinkedHashSet<Set<ExperimentDesignLevel>>();

			for(ExperimentDesignLevel l : activeDesign.getOrderedDesign().get(factorToAdd)) {

				for(Set<ExperimentDesignLevel>levelSet : seriesSet) {

					Set<ExperimentDesignLevel>newLset = new LinkedHashSet<ExperimentDesignLevel>(levelSet);
					newLset.add(l);
					seriesSetUpdated.add(newLset);
				}
			}
			seriesSet = seriesSetUpdated;
		}
		return seriesSet;
	}


	public static Map<String, DataFile[]> mapSeriesIgnoreDesign(
			DataPipeline pipeline,
			FileSortingOrder sortingOrder,
			ExperimentDesignSubset activeDesign){

		DataAnalysisProject experiment = MRC2ToolBoxCore.getActiveMetabolomicsExperiment();
		if (experiment == null || experiment.getExperimentDesign() == null 
				|| experiment.getExperimentDesign().getSamples().isEmpty())
			return null;

		Map<String, DataFile[]> dataFileMap = new LinkedHashMap<String, DataFile[]>();
		DataFile[] sorted = DataSetUtils.sortFiles(pipeline, sortingOrder, activeDesign);		
		Collection<ExperimentalSample> samples = 
				experiment.getExperimentDesign().getSamplesForDesignSubset(activeDesign);

		if(sortingOrder.equals(FileSortingOrder.SAMPLE_ID) || 
				sortingOrder.equals(FileSortingOrder.SAMPLE_NAME)) {

			LinkedHashMap<ExperimentalSample, ArrayList<DataFile>>sfMap = 
					new LinkedHashMap<ExperimentalSample, ArrayList<DataFile>>();

			for(DataFile df : sorted) {

				for(ExperimentalSample sample : samples) {

					if(sample.hasDataFile(df)) {

						if(!sfMap.containsKey(sample))
							sfMap.put(sample, new ArrayList<DataFile>());

						sfMap.get(sample).add(df);
					}
				}
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
			DataPipeline pipeline,
			FileSortingOrder sortingOrder,
			ExperimentDesignSubset activeDesign){

		DataAnalysisProject experiment = MRC2ToolBoxCore.getActiveMetabolomicsExperiment();
		if (experiment == null || experiment.getExperimentDesign() == null 
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
	
	public static Map<DataFile, ExperimentalSample> createDataFileSampleMap(DataFile[] files) {

		if (MRC2ToolBoxCore.getActiveMetabolomicsExperiment() == null)
			return null;

		if (MRC2ToolBoxCore.getActiveMetabolomicsExperiment().getExperimentDesign() == null)
			return null;

		if (MRC2ToolBoxCore.getActiveMetabolomicsExperiment().getExperimentDesign().getSamples().isEmpty())
			return null;

		Map<DataFile, ExperimentalSample>dataFileMap = new LinkedHashMap<DataFile, ExperimentalSample>();
		DataAnalysisProject experiment = MRC2ToolBoxCore.getActiveMetabolomicsExperiment();

		for(DataFile f : files) {

			for(ExperimentalSample sample : experiment.getExperimentDesign().getSamples()) {

				if(sample.hasDataFile(f))
					dataFileMap.put(f, sample);
			}
		}
		return dataFileMap;
	}

	public static Map<DataFile, ExperimentalSample> createDataFileSampleMap(MsFeature[] selectedFeatures) {

		DataAnalysisProject experiment = MRC2ToolBoxCore.getActiveMetabolomicsExperiment();
		if(experiment == null)
			return null;
		
		Map<DataFile, ExperimentalSample>dataFileMap = 
				new HashMap<DataFile, ExperimentalSample>();
		for(DataPipeline pl : experiment.getDataPipelines()) {

			for(ExperimentalSample sample : experiment.getExperimentDesign().getSamples()) {

				for(DataFile df : sample.getDataFilesForMethod(pl.getAcquisitionMethod()))
					dataFileMap.put(df, sample);
			}
		}		
		return dataFileMap;
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









































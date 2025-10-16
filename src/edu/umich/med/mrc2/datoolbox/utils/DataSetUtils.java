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

package edu.umich.med.mrc2.datoolbox.utils;

import java.util.Collection;
import java.util.List;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.ujmp.core.Matrix;
import org.ujmp.core.calculation.Calculation.Ret;

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignFactor;
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignSubset;
import edu.umich.med.mrc2.datoolbox.data.ExperimentalSample;
import edu.umich.med.mrc2.datoolbox.data.LibraryMsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureSet;
import edu.umich.med.mrc2.datoolbox.data.compare.DataFileComparator;
import edu.umich.med.mrc2.datoolbox.data.enums.FileSortingOrder;
import edu.umich.med.mrc2.datoolbox.data.enums.GlobalDefaults;
import edu.umich.med.mrc2.datoolbox.data.lims.DataAcquisitionMethod;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.plot.TwoDimDataPlotParameterObject;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.ReferenceSamplesManager;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class DataSetUtils {
	
	public static Matrix subsetCurrentlyActiveDataMatrix(MsFeatureSet activeFeatures) {
		
		return subsetDataMatrix(
				MRC2ToolBoxCore.getActiveMetabolomicsExperiment(),
				MRC2ToolBoxCore.getActiveMetabolomicsExperiment().getActiveDataPipeline(),
				MRC2ToolBoxCore.getActiveMetabolomicsExperiment().getExperimentDesign().getActiveDesignSubset(),
				activeFeatures);
	}

	public static Matrix subsetDataMatrix(
			DataAnalysisProject currentExperiment,
			DataPipeline acivePipeline,
			ExperimentDesignSubset activeDesign,
			MsFeatureSet activeFeatures) {

		if(currentExperiment == null || acivePipeline == null || activeDesign == null 
				|| activeFeatures == null || activeFeatures.getFeatures().isEmpty())
			return null;
		
		Set<DataFile>activeFiles = 
				getActiveFilesForPipelineAndDesignSubset(
						currentExperiment,
						acivePipeline,
						activeDesign,
						null);
		
		if(activeFiles.isEmpty())
			return null;
		
		return subsetDataMatrix(
				currentExperiment.getDataMatrixForDataPipeline(acivePipeline), 
				activeFeatures.getFeatures(), 
				activeFiles);
	}
	
	public static Matrix subsetDataMatrix(
			Matrix sourceMatrix,
			Collection <MsFeature>features,
			Collection<DataFile>dataFiles) {
		
		Matrix featureMatrix = sourceMatrix.getMetaDataDimensionMatrix(0);
		Matrix newFeatureMatrix = featureMatrix;
		Matrix fileMatrix = sourceMatrix.getMetaDataDimensionMatrix(1);
		Matrix newFileMatrix = fileMatrix;
		Collection<Long>featureCoordinates = null;
		Collection<Long>fileCoordinates = null;
		if(features != null) {
			
			featureCoordinates = 
					features.stream().map(f -> sourceMatrix.getColumnForLabel(f)).
					sorted().collect(Collectors.toList());
			newFeatureMatrix = featureMatrix.selectColumns(Ret.NEW, featureCoordinates);
		}
		if(dataFiles != null) {
			
			fileCoordinates = dataFiles.stream().
					map(file -> sourceMatrix.getRowForLabel(file)).
					collect(Collectors.toList());
			newFileMatrix = fileMatrix.selectRows(Ret.NEW, fileCoordinates);
		}
		Matrix subset = null;
		if(featureCoordinates != null && fileCoordinates != null)
			subset = sourceMatrix.selectColumns(Ret.LINK, featureCoordinates).selectRows(Ret.NEW, fileCoordinates);
		
		if(featureCoordinates == null && fileCoordinates != null)
			subset = sourceMatrix.selectRows(Ret.NEW, fileCoordinates);
		
		if(featureCoordinates != null && fileCoordinates == null)
			subset = sourceMatrix.selectColumns(Ret.LINK, featureCoordinates);
		
		if(subset != null) {
			subset.setMetaDataDimensionMatrix(0, newFeatureMatrix);
			subset.setMetaDataDimensionMatrix(1, newFileMatrix);
		}
		return subset;
	}
	
	public static Matrix subsetDataMatrixByDataFiles(
			Matrix sourceMatrix,
			Collection<DataFile>dataFiles) {
		return subsetDataMatrix(sourceMatrix, null, dataFiles);
	}
	
	public static Matrix subsetDataMatrixByFeatures(
			Matrix sourceMatrix,
			Collection <MsFeature>features) {
		return subsetDataMatrix(sourceMatrix, features, null);
	}
	
	public static Matrix getFeatureSubsetMatrix(
			Matrix source,
			Collection<MsFeature>activeFeatures) {

		Matrix featureMatrix = source.getMetaDataDimensionMatrix(0);
		Collection<Long>features = activeFeatures.stream().
				map(f -> source.getColumnForLabel(f)).sorted().collect(Collectors.toList());
		Matrix newFeatureMatrix = featureMatrix.selectColumns(Ret.NEW, features);

		Matrix subset = source.selectColumns(Ret.NEW, features);
		subset.setMetaDataDimensionMatrix(0, newFeatureMatrix);
		subset.setMetaDataDimensionMatrix(1, source.getMetaDataDimensionMatrix(1));
		return subset;
	}
	
	public static Set<DataFile> getActiveFilesForPipelineAndDesignSubset(
			TwoDimDataPlotParameterObject plotParameters){
		return getActiveFilesForPipelineAndDesignSubset(
				plotParameters.getExperiment(),
				plotParameters.getPipeline(),
				plotParameters.getActiveDesign(),
				plotParameters.getSortingOrder());
	}
	
	public static Set<DataFile> getActiveFilesForPipelineAndDesignSubset(
			DataAnalysisProject currentExperiment,
			DataPipeline dataPipeline,
			ExperimentDesignSubset activeDesign,
			FileSortingOrder sortingOrder) {
		
		Set<DataFile>files = new TreeSet<>();
		if(sortingOrder != null)
			files = new TreeSet<>(new DataFileComparator(sortingOrder));
		
		if (dataPipeline == null || activeDesign == null 
			|| currentExperiment == null
			|| currentExperiment.getExperimentDesign() == null 
			|| currentExperiment.getExperimentDesign().getSamples().isEmpty())
			return files;
		
		Collection<ExperimentalSample> samples =
				currentExperiment.getExperimentDesign().getSamplesForDesignSubset(activeDesign, true);
		
		if(!samples.isEmpty()) {
			
			for(ExperimentalSample s : samples) {
				
				Set<DataFile>activeFiles = s.getDataFilesForMethod(dataPipeline.getAcquisitionMethod()).
						stream().filter(f -> f.isEnabled()).collect(Collectors.toSet());
				if(!activeFiles.isEmpty())
					files.addAll(activeFiles);
			}
		}
		return files;
	}

	public static Collection<DataFile> sortFiles(
			DataAnalysisProject currentExperiment,
			DataPipeline dataPipeline,
			FileSortingOrder order,
			ExperimentDesignSubset activeDesign) {
		
		return getActiveFilesForPipelineAndDesignSubset(
						currentExperiment, dataPipeline, activeDesign, order);
	}

	public static Set<DataFile> getDataFilesForSamples(
			Collection<ExperimentalSample>samples, 
			DataPipeline dataPipeline, 
			FileSortingOrder fileSortingOrder) {
		
		Set<DataFile>dataFiles = new TreeSet<>();
		if(fileSortingOrder != null)
			dataFiles = new TreeSet<>(new DataFileComparator(fileSortingOrder));
		
		ExperimentDesignFactor refFactor = ReferenceSamplesManager.getSampleControlTypeFactor();
		List<ExperimentalSample>regularSamples = samples.stream().
				filter(s -> s.getLevel(refFactor).equals(ReferenceSamplesManager.sampleLevel)).
				collect(Collectors.toList());
		if(regularSamples.isEmpty())
			return dataFiles;
		
		DataAcquisitionMethod acqMethod = dataPipeline.getAcquisitionMethod();
		for(ExperimentalSample s : regularSamples) {
			
			NavigableSet<DataFile> sampleFiles = s.getDataFilesForMethod(acqMethod);
			if(!sampleFiles.isEmpty())				
				sampleFiles.stream().filter(f -> f.isEnabled()).forEach(dataFiles::add);
		}
		return dataFiles;
	}
	
	public static ExperimentDesignSubset getSamplesOnlyDesignSubset(DataAnalysisProject currentExperiment) {

		if(currentExperiment.getExperimentDesign() == null)
			return null;

		ExperimentDesignSubset samplesOnly = 
				new ExperimentDesignSubset(GlobalDefaults.SAMPLES_ONLY.getName());
		samplesOnly.addLevel(ReferenceSamplesManager.sampleLevel,false);
		return samplesOnly;
	}
	
	public static Set<DataFile> getCurrentActiveFiles(){
		
		DataAnalysisProject experiment = MRC2ToolBoxCore.getActiveMetabolomicsExperiment();
		
		Set<DataFile>files = new TreeSet<>();
		
		if (experiment == null || experiment.getActiveDataPipeline() == null
				|| experiment.getExperimentDesign() == null 
				|| experiment.getExperimentDesign().getSamples().isEmpty()
				|| experiment.getExperimentDesign().getActiveDesignSubset() == null)
			return files;
		
		return getActiveFilesForPipelineAndDesignSubset(
				experiment,
				experiment.getActiveDataPipeline(),
				experiment.getExperimentDesign().getActiveDesignSubset(),
				null);
	}
	
	public static long getColumnForFeature(
			Matrix dataMatrix, 
			MsFeature feature, 
			DataAnalysisProject experiment,
			DataPipeline pipeline) {
		
		long column = dataMatrix.getColumnForLabel(feature);
		if(column == -1 && feature instanceof LibraryMsFeature) {
			String fid = ((LibraryMsFeature)feature).getParentFeatureId();
			if(fid != null)
				feature = experiment.getMsFeatureById(fid, pipeline);
			
			if(feature == null)
				return -1;
			else
				return dataMatrix.getColumnForLabel(feature);
		}
		else
			return column;
	}
	
	public static Set<ExperimentalSample>getCommonSamplesForDataPipelines(
			DataAnalysisProject experiment,
			Collection<DataPipeline>pipelines, 
			boolean enabledOnly){
		
		Set<ExperimentalSample>commonSamples = new TreeSet<>();
		Collection<ExperimentalSample> samples =
				experiment.getExperimentDesign().getSamplesForDesignSubset(
						experiment.getExperimentDesign().getActiveDesignSubset(), enabledOnly);
		for(DataPipeline dp : pipelines) {
			
			Set<DataFile> pipelineFiles = samples.stream().
					flatMap(s -> s.getDataFilesForMethod(dp.getAcquisitionMethod()).stream()).
					collect(Collectors.toSet());
			if(enabledOnly)
				pipelineFiles = pipelineFiles.stream().
					filter(f -> f.isEnabled()).collect(Collectors.toSet());
				
			pipelineFiles.stream().forEach(f -> commonSamples.add(f.getParentSample()));
		}	
		return commonSamples;		
	}
	
	public static boolean designValidForStats(
			DataAnalysisProject currentExperiment,
			DataPipeline activePipeline,
			boolean requirePooled) {

		int pooledCount = 0;
		int sampleCount = 0;

		//	TODO deal with by-batch stats
		for (DataFile df : currentExperiment.getDataFilesForAcquisitionMethod(
				activePipeline.getAcquisitionMethod())) {
			
			if(df.getParentSample() == null) {
				System.out.println(df.getName());
				continue;
			}

			if(df.getParentSample().hasLevel(ReferenceSamplesManager.masterPoolLevel))
				pooledCount++;

			if(df.getParentSample().hasLevel(ReferenceSamplesManager.sampleLevel))
				sampleCount++;
		}
		if (requirePooled && pooledCount == 0)
			return false;

		if (sampleCount > 0)
			return true;

		return false;
	}	
}




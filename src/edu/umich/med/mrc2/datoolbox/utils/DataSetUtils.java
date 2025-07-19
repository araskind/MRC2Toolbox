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

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.ujmp.core.Matrix;
import org.ujmp.core.calculation.Calculation.Ret;

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignFactor;
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignSubset;
import edu.umich.med.mrc2.datoolbox.data.ExperimentalSample;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureSet;
import edu.umich.med.mrc2.datoolbox.data.compare.DataFileComparator;
import edu.umich.med.mrc2.datoolbox.data.enums.FileSortingOrder;
import edu.umich.med.mrc2.datoolbox.data.enums.GlobalDefaults;
import edu.umich.med.mrc2.datoolbox.data.lims.DataAcquisitionMethod;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
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

//		Matrix source = currentExperiment.getDataMatrixForDataPipeline(acivePipeline);
//		Matrix featureMatrix = source.getMetaDataDimensionMatrix(0);
//		Matrix fileMatrix = source.getMetaDataDimensionMatrix(1);
//
//		Collection<Long>features = 
//				activeFeatures.getFeatures().
//				stream().map(f -> source.getColumnForLabel(f)).
//				sorted().collect(Collectors.toList());
//		Matrix newFeatureMatrix = featureMatrix.selectColumns(Ret.NEW, features);
//
//		Collection<Long>files = activeFiles.stream().
//				map(file -> source.getRowForLabel(file)).
//				collect(Collectors.toList());
//		Matrix newFileMatrix = fileMatrix.selectRows(Ret.NEW, files);
//
//		Matrix subset = source.selectColumns(Ret.LINK, features).selectRows(Ret.NEW, files);
//		subset.setMetaDataDimensionMatrix(0, newFeatureMatrix);
//		subset.setMetaDataDimensionMatrix(1, newFileMatrix);
//		return subset;
		
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
			DataAnalysisProject currentExperiment,
			DataPipeline dataPipeline,
			ExperimentDesignSubset activeDesign,
			FileSortingOrder sortingOrder) {
		
		Set<DataFile>files = new TreeSet<DataFile>();
		if(sortingOrder != null)
			files = new TreeSet<DataFile>(new DataFileComparator(sortingOrder));
		
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
		
		Set<DataFile>activeFiles = 
				getActiveFilesForPipelineAndDesignSubset(
						currentExperiment, dataPipeline, activeDesign, order);
		
		return activeFiles;
	}

	public static Set<DataFile> getDataFilesForSamples(
			Collection<ExperimentalSample>samples, 
			DataPipeline dataPipeline, 
			FileSortingOrder fileSortingOrder) {
		
		Set<DataFile>dataFiles = 
				new TreeSet<DataFile>(new DataFileComparator(fileSortingOrder));
		ExperimentDesignFactor refFactor = ReferenceSamplesManager.getSampleControlTypeFactor();
		List<ExperimentalSample>regularSamples = samples.stream().
				filter(s -> s.getLevel(refFactor).equals(ReferenceSamplesManager.sampleLevel)).
				collect(Collectors.toList());
		if(regularSamples.isEmpty())
			return dataFiles;
		
		DataAcquisitionMethod acqMethod = dataPipeline.getAcquisitionMethod();
		for(ExperimentalSample s : regularSamples) {
			
			TreeSet<DataFile> sampleFiles = s.getDataFilesForMethod(acqMethod);
			if(!sampleFiles.isEmpty())				
				sampleFiles.stream().filter(f -> f.isEnabled()).forEach(f -> dataFiles.add(f));
			
//			if(s.equals(ReferenceSamplesManager.getGenericRegularSample())) {
//				
//				for(ExperimentalSample regSample : MRC2ToolBoxCore.getActiveMetabolomicsExperiment().getExperimentDesign().getRegularSamples()) {
//					
//					TreeSet<DataFile> sampleFiles = regSample.getDataFilesForMethod(acqMethod);
//					if(sampleFiles != null && !sampleFiles.isEmpty())
//						dataFiles.addAll(sampleFiles);
//				}
//			}
//			else {
//				TreeSet<DataFile> sampleFiles = s.getDataFilesForMethod(acqMethod);
//				if(sampleFiles != null && !sampleFiles.isEmpty())
//					dataFiles.addAll(sampleFiles);
//			}
		}
		return dataFiles;
	}
	
	public static ExperimentDesignSubset getSamplesOnlyDesignSubset(DataAnalysisProject currentExperiment) {

		if(currentExperiment.getExperimentDesign() == null)
			return null;

		ExperimentDesignSubset samplesOnly = 
				new ExperimentDesignSubset(GlobalDefaults.SAMPLES_ONLY.getName());
		samplesOnly.addLevel(ReferenceSamplesManager.sampleLevel);
		return samplesOnly;
	}
	
	public static Set<DataFile> getCurrentActiveFiles(){
		
		DataAnalysisProject experiment = MRC2ToolBoxCore.getActiveMetabolomicsExperiment();
		
		Set<DataFile>files = new TreeSet<DataFile>();
		
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
}

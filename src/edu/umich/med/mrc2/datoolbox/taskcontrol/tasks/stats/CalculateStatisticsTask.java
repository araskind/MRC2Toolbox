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

package edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.stats;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.ujmp.core.Matrix;
import org.ujmp.core.calculation.Calculation.Ret;

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignSubset;
import edu.umich.med.mrc2.datoolbox.data.ExperimentalSample;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureSet;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureStatisticalSummary;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.main.ReferenceSamplesManager;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;

public class CalculateStatisticsTask extends AbstractTask {

	private DataPipeline dataPipeline;
	private DataAnalysisProject currentExperiment;
	private DescriptiveStatistics sampleDescriptiveStatistics;
	private DescriptiveStatistics pooledDescriptiveStatistics;
	private ExperimentDesignSubset activeDesignSubset;
	private DescriptiveStatistics totalDescriptiveStatistics;

	public CalculateStatisticsTask(
			DataAnalysisProject experiment, DataPipeline dataPipeline) {

		
		this.currentExperiment = experiment;
		this.dataPipeline = dataPipeline;
		activeDesignSubset = 
				this.currentExperiment.getExperimentDesign().getActiveDesignSubset();

		taskDescription = "Calculating statistics for active data pipeline";
		total = currentExperiment.getMsFeaturesForDataPipeline(dataPipeline).size();
		processed = 0;
	}

	@Override
	public void run() {

		setStatus(TaskStatus.PROCESSING);

		try {
			calculateStatistics();

			//	Only cleanup global data set from features without data
			//	and only if all files are enabled
			
			//	TODO deal with removing empty features
//			if(!currentProject.getActiveFeatureSetForDataPipeline(dataPipeline).
//					getName().equals(GlobalDefaults.ALL_FEATURES.getName())) {
//				removeEmptyFeaturesFromSubset();
//			}
//			else {
//				if (currentProject.allDataFilesForAcquisitionMethodEnabled(
//						dataPipeline.getAcquisitionMethod()))
//					removeEmptyFeatures();
//			}
		}
		catch (Exception e) {

			e.printStackTrace();
			setStatus(TaskStatus.ERROR);
			return;
		}
		setStatus(TaskStatus.FINISHED);
	}

	//	TODO add by-batch/global options
	private void calculateStatistics() {

		sampleDescriptiveStatistics = new DescriptiveStatistics();
		pooledDescriptiveStatistics = new DescriptiveStatistics();
		totalDescriptiveStatistics = new DescriptiveStatistics();

		TreeSet<DataFile> pooledFiles = new TreeSet<DataFile>();
		TreeSet<DataFile> sampleFiles = new TreeSet<DataFile>();
		Set<DataFile>dataFiles =
				currentExperiment.getActiveDataFilesForDesignAndAcquisitionMethod(
						activeDesignSubset, dataPipeline.getAcquisitionMethod());
		HashMap<DataFile,Long>fileCoordinates = new HashMap<DataFile,Long>();
		Matrix assayData = currentExperiment.getDataMatrixForDataPipeline(dataPipeline);
		long[] dataCoordinates = new long[2];
		
		Set<ExperimentalSample> pooledSamples = currentExperiment.getPooledSamples();

		for (DataFile df : dataFiles) {
			
			if(pooledSamples.contains(df.getParentSample()) && df.isEnabled())
				pooledFiles.add(df);

			if(df.getParentSample().hasLevel(ReferenceSamplesManager.sampleLevel) && df.isEnabled())
				sampleFiles.add(df);

			fileCoordinates.put(df, assayData.getRowForLabel(df));
		}
		for (MsFeature cf : currentExperiment.getActiveFeatureSetForDataPipeline(dataPipeline).getFeatures()) {

			sampleDescriptiveStatistics.clear();
			pooledDescriptiveStatistics.clear();
			totalDescriptiveStatistics.clear();
			dataCoordinates[1] = assayData.getColumnForLabel(cf);

			for (DataFile df : dataFiles) {

				dataCoordinates[0] = fileCoordinates.get(df);
				if(dataCoordinates[0] == -1)
					continue;
				
				double value = assayData.getAsDouble(dataCoordinates);
				if(value > 0) {

					if (pooledFiles.contains(df))
						pooledDescriptiveStatistics.addValue(value);

					if (sampleFiles.contains(df))
						sampleDescriptiveStatistics.addValue(value);

					totalDescriptiveStatistics.addValue(value);
				}
			}
			MsFeatureStatisticalSummary statSummary = cf.getStatsSummary();
			if(statSummary == null) {
				statSummary = new MsFeatureStatisticalSummary(cf);
				cf.setStatsSummary(statSummary);
			}
			if (pooledFiles.size() > 0) {

				statSummary.setPooledMean(pooledDescriptiveStatistics.getMean());
				statSummary.setPooledMedian(pooledDescriptiveStatistics.getPercentile(50.0d));
				statSummary.setPooledStDev(pooledDescriptiveStatistics.getStandardDeviation());
				statSummary.setPooledFrequency((double) pooledDescriptiveStatistics.getN() / (double)pooledFiles.size());
			}
			if (sampleFiles.size() > 0) {

				statSummary.setSampleMean(sampleDescriptiveStatistics.getMean());
				statSummary.setSampleMedian(sampleDescriptiveStatistics.getPercentile(50.0d));
				statSummary.setSampleStDev(sampleDescriptiveStatistics.getStandardDeviation());
				statSummary.setSampleFrequency((double) sampleDescriptiveStatistics.getN() / (double)sampleFiles.size());
			}
			statSummary.setTotalMedian(totalDescriptiveStatistics.getPercentile(50.0d));
			processed++;
		}
	}

	@Override
	public Task cloneTask() {
		return new CalculateStatisticsTask(
				currentExperiment, dataPipeline);
	}

	public DataPipeline getDataPipeline() {
		return dataPipeline;
	}

	private void removeEmptyFeatures() {

		taskDescription = "Removing features with no data for active data pipeline";
		total = 100;
		processed = 20;

		List<MsFeature> featuresToRemove = 
			currentExperiment.getMsFeaturesForDataPipeline(dataPipeline).stream().
			filter(f -> (f.getStatsSummary().getSampleFrequency() == 0.0d)).
			filter(f -> (f.getStatsSummary().getPooledFrequency() == 0.0d)).
			collect(Collectors.toList());

		if(!featuresToRemove.isEmpty()) {

			Matrix dataMatrix = currentExperiment.getDataMatrixForDataPipeline(dataPipeline);
			Matrix featureMatrix = dataMatrix.getMetaDataDimensionMatrix(0);

			List<Long> rem = featuresToRemove.stream().
					mapToLong(cf -> dataMatrix.getColumnForLabel(cf)).
					boxed().collect(Collectors.toList());

			processed = 50;

			currentExperiment.deleteFeatures(featuresToRemove, dataPipeline);
			Matrix newDataMatrix = dataMatrix.deleteColumns(Ret.NEW, rem);
			Matrix newFeatureMatrix = featureMatrix.deleteColumns(Ret.NEW, rem);
			newDataMatrix.setMetaDataDimensionMatrix(0, newFeatureMatrix);
			newDataMatrix.setMetaDataDimensionMatrix(1, dataMatrix.getMetaDataDimensionMatrix(1));
			currentExperiment.setDataMatrixForDataPipeline(dataPipeline, newDataMatrix);
		}
		processed = 100;
	}

	//	Remove features with no data inside the subset
	private void removeEmptyFeaturesFromSubset() {

		MsFeatureSet subset = 
				currentExperiment.getActiveFeatureSetForDataPipeline(dataPipeline);
		List<MsFeature> featuresToRemove = subset.getFeatures().stream().
				filter(f -> (f.getStatsSummary().getSampleFrequency() == 0.0d)).
				filter(f -> (f.getStatsSummary().getPooledFrequency() == 0.0d)).
				collect(Collectors.toList());

		if(!featuresToRemove.isEmpty())
			subset.removeFeatures(featuresToRemove);
	}
}










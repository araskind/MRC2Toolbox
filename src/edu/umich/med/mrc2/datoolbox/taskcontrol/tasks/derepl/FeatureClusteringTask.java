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

package edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.derepl;

import java.util.Arrays;
import java.util.HashSet;

import org.ujmp.core.Matrix;
import org.ujmp.core.calculation.Calculation.Ret;

import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureCluster;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.utils.AnalysisUtils;

public class FeatureClusteringTask extends AbstractTask {

	private double correlationCutoff;
	private double rtWindow;
	private HashSet<MsFeatureCluster> clusterList;
	private Matrix dataMatrix;
	private Matrix corrMatrix;
	private HashSet<long[]> highCorrCoordinates;
	private MsFeature[] features;
	private double areaWeight, frequencyWeight;
	private DataPipeline dataPipeline;
	private DataAnalysisProject currentExperiment;
	private boolean logTransform;

	public FeatureClusteringTask(
			DataAnalysisProject experiment,
			DataPipeline dataPipeline, 
			boolean logTransform) {

		this.currentExperiment = experiment;
		this.dataPipeline = dataPipeline;
		this.logTransform = logTransform;

		//	TODO
		//	correlationCutoff = CaConfiguration.getCorrelationCutoff();
		rtWindow = MRC2ToolBoxConfiguration.getRtWindow();

		dataMatrix = currentExperiment.getDataMatrixForDataPipeline(dataPipeline);
		Matrix featureMatrix = dataMatrix.getMetaDataDimensionMatrix(0);
		features = Arrays.copyOf(featureMatrix.toObjectArray()[0], 
				featureMatrix.toObjectArray()[0].length, MsFeature[].class);

		clusterList = new HashSet<MsFeatureCluster>();
		highCorrCoordinates = new HashSet<long[]>();

		taskDescription = "Clustering compound features ...";
	}

	private void analyzeMassDifferences() {

		taskDescription = "Creating heat maps ...";
		total = clusterList.size();
		processed = 0;

//		TODO get preferences from panel
//		areaWeight = CaConfiguration.getAreaScoreWeight();
//		frequencyWeight = CaConfiguration.getFrequencyScoreWeight();

		for (MsFeatureCluster fc : clusterList) {

			fc.setClusterCorrMatrix(fc.createClusterCorrelationMatrix(false));
			AnalysisUtils.findTopScoringFeature(fc.getFeatures(), areaWeight, frequencyWeight);
			processed++;
		}
	}

	@Override
	public Task cloneTask() {

		FeatureClusteringTask fct = new FeatureClusteringTask(currentExperiment, dataPipeline, logTransform);
		return fct;
	}

	private void filterCorrelationsByCutoff() {

		taskDescription = "Filtering correlation matrix ...";
		total = (int) corrMatrix.getColumnCount();
		processed = 0;

		long[] coordinates = new long[] { 0, 0 };
		double value;
		highCorrCoordinates.clear();

		total = (int) corrMatrix.getColumnCount();

		for (int i = 0; i < corrMatrix.getColumnCount(); i++) {

			coordinates[1] = i;

			for (int j = i + 1; j < corrMatrix.getRowCount(); j++) {

				coordinates[0] = j;

				value = corrMatrix.getAsDouble(coordinates);

				if (value > correlationCutoff)
					highCorrCoordinates.add(new long[] { i, j });
			}
			processed++;
		}
	}

	private void findFeatureClusters() {

		taskDescription = "Finding clusters ...";
		clusterList.clear();
		HashSet<MsFeature>assigned = new HashSet<MsFeature>();

		MsFeature cfOne, cfTwo;
		double deltaRt;

		total = highCorrCoordinates.size();
		processed = 0;

		for (long[] coordinates : highCorrCoordinates) {

			if (coordinates != null) {

				cfOne = features[(int) coordinates[0]];
				cfTwo = features[(int) coordinates[1]];

				if (!assigned.contains(cfOne) || !assigned.contains(cfTwo)) {

					deltaRt = Math.abs(cfOne.getRetentionTime() - cfTwo.getRetentionTime());
					if (deltaRt < rtWindow) {

						if (!assigned.contains(cfOne) && !assigned.contains(cfTwo)) {

							MsFeatureCluster newCluster = new MsFeatureCluster();
							newCluster.addFeature(cfOne, dataPipeline);
							newCluster.addFeature(cfTwo, dataPipeline);
							clusterList.add(newCluster);
							assigned.add(cfOne);
							assigned.add(cfTwo);
						}
						else {
							if (assigned.contains(cfOne)) {

								for (MsFeatureCluster cluster : clusterList) {

									if (cluster.containsFeature(cfOne)) {
										cluster.addFeature(cfTwo, dataPipeline);
										assigned.add(cfTwo);
										break;
									}
								}
							}
							if (assigned.contains(cfTwo)) {

								for (MsFeatureCluster cluster : clusterList) {

									if (cluster.containsFeature(cfTwo)) {
										cluster.addFeature(cfOne, dataPipeline);
										assigned.add(cfOne);
										break;
									}
								}
							}
						}
					}
				}
			}
			processed++;
		}
	}

	public DataPipeline getDataPipeline() {
		return dataPipeline;
	}

	public Matrix getCorrMatrix() {
		return corrMatrix;
	}

	public HashSet<MsFeatureCluster> getFeatureClusters() {
		return clusterList;
	}

	@Override
	public void run() {

		setStatus(TaskStatus.PROCESSING);

		try {
			runCorrelationAnalysis();
			analyzeMassDifferences();
		} catch (Exception e) {

			e.printStackTrace();
			setStatus(TaskStatus.ERROR);
return;

		}
		setStatus(TaskStatus.FINISHED);
	}

	private void runCorrelationAnalysis() {

		total = 100;
		processed = 20;
		taskDescription = "Creating correlation matrix ...";

		if (logTransform)
			corrMatrix = dataMatrix.log2(Ret.LINK).corrcoef(Ret.NEW, true, false);
		else
			corrMatrix = dataMatrix.corrcoef(Ret.NEW, true, false);

		corrMatrix.setMetaDataDimensionMatrix(0, dataMatrix.getMetaDataDimensionMatrix(0));
		corrMatrix.setMetaDataDimensionMatrix(1, dataMatrix.getMetaDataDimensionMatrix(0).transpose(Ret.NEW));

		filterCorrelationsByCutoff();

		findFeatureClusters();
	}
}

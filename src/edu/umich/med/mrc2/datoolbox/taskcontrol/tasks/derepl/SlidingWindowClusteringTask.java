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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.stat.correlation.KendallsCorrelation;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;
import org.ujmp.core.Matrix;
import org.ujmp.core.calculation.Calculation.Ret;
import org.ujmp.core.doublematrix.calculation.general.missingvalues.Impute.ImputationMethod;
import org.ujmp.core.doublematrix.impl.DefaultDenseDoubleMatrix2D;
import org.ujmp.core.export.destination.DefaultMatrixFileExportDestination;

import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignSubset;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureCluster;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureSet;
import edu.umich.med.mrc2.datoolbox.data.compare.MsFeatureComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.enums.SlidingWindowUnit;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.binner.control.CorrelationFunctionType;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.utils.DataSetUtils;
import edu.umich.med.mrc2.datoolbox.utils.Range;

public class SlidingWindowClusteringTask extends AbstractTask {

	private DataAnalysisProject currentExperiment;
	private DataPipeline dataPipeline;
	private CorrelationFunctionType correlationAlgoritm;
	private double maxClusterWidth;
	private SlidingWindowUnit windowSlidingUnit;
	private int featureNumberWindow;
	private double featureTimeWindow;
	private Matrix sampleMatrix;
	private boolean limitRtRange;
	private Range retentionRange;
	private boolean filterMissing;
	private double maxMissingPercent;
	private boolean imputeMissing;
	private ImputationMethod imputationMethod;
	private int knnClusterNumber;
	private double correlationCutoff;
	private HashSet<MsFeatureCluster> clusterList;
	private Collection<MsFeature>featureSubset;

	private PearsonsCorrelation pearson;
	private SpearmansCorrelation spearman;
	private KendallsCorrelation kendall;

	public SlidingWindowClusteringTask(
			DataAnalysisProject experiment,
			DataPipeline dataPipeline,
			boolean limitRtRange,
			Range retentionRange,
			boolean filterMissing,
			double maxMissingPercent,
			boolean imputeMissing,
			ImputationMethod imputationMethod,
			int knnClusterNumber,
			CorrelationFunctionType correlationAlgoritmh,
			double correlationCutoff,
			double maxClusterWidth,
			SlidingWindowUnit windowSlidingUnit,
			int featureNumberWindow,
			double featureTimeWindow) {

		super();

		this.currentExperiment = experiment;
		this.dataPipeline = dataPipeline;
		this.limitRtRange = limitRtRange;
		this.retentionRange = retentionRange;
		this.filterMissing = filterMissing;
		this.maxMissingPercent = maxMissingPercent;
		this.imputeMissing = imputeMissing;
		this.imputationMethod = imputationMethod;
		this.knnClusterNumber = knnClusterNumber;
		this.correlationAlgoritm = correlationAlgoritmh;
		this.correlationCutoff = correlationCutoff;
		this.maxClusterWidth = maxClusterWidth;
		this.windowSlidingUnit = windowSlidingUnit;
		this.featureNumberWindow = featureNumberWindow;
		this.featureTimeWindow = featureTimeWindow;
	}

	@Override
	public void run() {

		setStatus(TaskStatus.PROCESSING);
		try {
			creatreSampleOnlyDataMatrix();
			createFeatureClusters();
			setStatus(TaskStatus.FINISHED);
		}
		catch (Exception e) {

			e.printStackTrace();
			setStatus(TaskStatus.ERROR);
			return;
		}
		setStatus(TaskStatus.FINISHED);
	}

	private void creatreSampleOnlyDataMatrix() {

		total = 100;
		processed = 20;
		taskDescription = "Creating data matrix without pooled samples ...";

		featureSubset = currentExperiment.getMsFeaturesForDataPipeline(dataPipeline);
		if(limitRtRange && retentionRange != null) {
			featureSubset = currentExperiment.getMsFeaturesForDataPipeline(dataPipeline).stream().
					filter(f -> retentionRange.contains(f.getRetentionTime())).
					collect(Collectors.toSet());
		}
		if(filterMissing && maxMissingPercent < 100.0d) {

			double minFreq = 1.0d - maxMissingPercent / 100.0d;
			featureSubset = featureSubset.stream().
					filter(f -> f.getStatsSummary().getSampleFrequency() > minFreq).
					collect(Collectors.toSet());
		}
		MsFeatureSet allFeatures = new MsFeatureSet("Features", featureSubset);
		ExperimentDesignSubset samplesOnly = DataSetUtils.getSamplesOnlyDesignSubset(currentExperiment);
		sampleMatrix =
				DataSetUtils.subsetDataMatrix(currentExperiment, dataPipeline, samplesOnly, allFeatures).
				replace(Ret.LINK, Double.NaN, 2).
				log2(Ret.LINK).
				replace(Ret.NEW, 1.0d, Double.NaN);

		if(imputeMissing)
			imputeMissingData();
	}

	private void imputeMissingData() {

		total = 100;
		processed = 20;
		taskDescription = "Imputing missing data ...";

		Object[] parameters = new Object[0];
		if(imputationMethod.equals(ImputationMethod.KNN))
			parameters = new Integer[] {knnClusterNumber};

		sampleMatrix = sampleMatrix.impute(Ret.NEW, imputationMethod, parameters);
	}

	private void createFeatureClusters() {

		taskDescription = "Genetaing feature clusters for " + dataPipeline.getName();
		clusterList = new HashSet<MsFeatureCluster>();
		Collection<Set<MsFeature>>featureWindows = createFeatureWindows();

		pearson = new PearsonsCorrelation();
		spearman = new SpearmansCorrelation();
		kendall = new KendallsCorrelation();

		total = featureWindows.size();
		processed = 0;

		//	Create clusters for each feature window
		for(Set<MsFeature>featureSet : featureWindows) {

			Matrix clusterMatrix = DataSetUtils.getFeatureSubsetMatrix(sampleMatrix, featureSet);
			Matrix corrMatrix = createCorrelationMatrix(clusterMatrix);
			Matrix corrMatrixAdj = timeDiffAdjustCorrelations(corrMatrix);

			//	For method development only
			try {
				saveCorrMatrix( corrMatrix, corrMatrixAdj, clusterMatrix, processed + 1);
			} catch (IOException e) {
				e.printStackTrace();
			}
			processed++;
		}
		// Cleanup clusters - remove redundant and deal with overlaping

	}

	private Matrix timeDiffAdjustCorrelations(Matrix corrMatrix) {

		Object[] featureObjects = corrMatrix.getMetaDataDimensionMatrix(0).toObjectArray()[0];
		MsFeature[] features = Arrays.copyOf(featureObjects, featureObjects.length, MsFeature[].class);
		Matrix drt = new DefaultDenseDoubleMatrix2D(features.length, features.length);

		for(int i=0; i<features.length; i++) {

			for(int j=0; j<features.length; j++)
				drt.setAsDouble(Math.abs(features[i].getRetentionTime() - features[j].getRetentionTime()), i, j);
		}
		drt = drt.normalize(Ret.NEW, 0).replace(Ret.NEW, 0.0d, 0.01);
		Matrix adjusted = corrMatrix.divide(Ret.NEW, true, drt);
		adjusted.setMetaDataDimensionMatrix(0, corrMatrix.getMetaDataDimensionMatrix(0));
		adjusted.setMetaDataDimensionMatrix(1, corrMatrix.getMetaDataDimensionMatrix(1));
		return adjusted;
	}

	private void saveCorrMatrix(Matrix corrMatrix, Matrix corrMatrixAdj, Matrix clusterMatrix, int idx) throws IOException {

		File corrFile = new File(currentExperiment.getExportsDirectory().getAbsolutePath() +
				File.separator + "Cluster_" + StringUtils.leftPad(Integer.toString(idx), 4, '0') + ".txt");
		File adjCorrFile = new File(currentExperiment.getExportsDirectory().getAbsolutePath() +
				File.separator + "Cluster_" + StringUtils.leftPad(Integer.toString(idx), 4, '0') + "_adjusted.txt");
		File mtxColumnsFile = new File(currentExperiment.getExportsDirectory().getAbsolutePath() +
				File.separator + "Cluster_" + StringUtils.leftPad(Integer.toString(idx), 4, '0') + "_colnames.txt");
		File dataFile = new File(currentExperiment.getExportsDirectory().getAbsolutePath() +
				File.separator + "Cluster_" + StringUtils.leftPad(Integer.toString(idx), 4, '0') + "_data.txt");

		DefaultMatrixFileExportDestination med = new DefaultMatrixFileExportDestination(corrMatrix, corrFile);
		med.asDenseCSV();
		med = new DefaultMatrixFileExportDestination(corrMatrixAdj, adjCorrFile);
		med.asDenseCSV();
		med = new DefaultMatrixFileExportDestination(corrMatrix.getMetaDataDimensionMatrix(1), mtxColumnsFile);
		med.asDenseCSV();
		med = new DefaultMatrixFileExportDestination(clusterMatrix, dataFile);
		med.asDenseCSV();
	}

	private Matrix createCorrelationMatrix(Matrix clusterMatrix) {

		double[][] doubleMatrix = clusterMatrix.toDoubleArray();
		Matrix corrMatrix = null;

		if(correlationAlgoritm.equals(CorrelationFunctionType.PEARSON))
			corrMatrix = Matrix.Factory.linkToArray(pearson.computeCorrelationMatrix(doubleMatrix).getData());

		if(correlationAlgoritm.equals(CorrelationFunctionType.SPEARMAN))
			corrMatrix = Matrix.Factory.linkToArray(spearman.computeCorrelationMatrix(doubleMatrix).getData());

		if(correlationAlgoritm.equals(CorrelationFunctionType.KENDALL))
			corrMatrix = Matrix.Factory.linkToArray(kendall.computeCorrelationMatrix(doubleMatrix).getData());

		if(corrMatrix != null) {

			corrMatrix.setMetaDataDimensionMatrix(0, clusterMatrix.getMetaDataDimensionMatrix(0));
			corrMatrix.setMetaDataDimensionMatrix(1, clusterMatrix.getMetaDataDimensionMatrix(0).transpose(Ret.NEW));
		}
		return corrMatrix;
	}

	private Collection<Set<MsFeature>> createFeatureWindows() {

		total = 100;
		processed = 20;
		double maxClusterWidthMinutes = maxClusterWidth/60.0d;
		double featureTimeWindowMinutes = featureTimeWindow/60.0d;

		Collection<Set<MsFeature>>featureWindows = new ArrayList<Set<MsFeature>>();

		List<MsFeature>sortedByRt =
				featureSubset.stream().
				sorted(new MsFeatureComparator(SortProperty.RT)).
				collect(Collectors.toList());

		Range fullRtRange = new Range(sortedByRt.get(0).getRetentionTime(), sortedByRt.get(sortedByRt.size()-1).getRetentionTime());
		Range currentRange = new Range(fullRtRange.getMin(), fullRtRange.getMin() + maxClusterWidthMinutes);
		int firstPosition = 0;
		double rangeStart;

		while(currentRange.getMax() < fullRtRange.getMax()) {

			Set<MsFeature>newSet = getFeaturesInRange(sortedByRt, currentRange);

			if(newSet.size() > 1)
				featureWindows.add(newSet);

			if(windowSlidingUnit.equals(SlidingWindowUnit.Features)) {

				firstPosition = firstPosition + featureNumberWindow;

				if(firstPosition < sortedByRt.size())
					rangeStart =  sortedByRt.get(firstPosition).getRetentionTime();
				else
					break;
			}
			else {
				rangeStart =  currentRange.getMin() + featureTimeWindowMinutes;
			}
			currentRange = new Range(rangeStart, rangeStart + maxClusterWidthMinutes);
		}
		return featureWindows;
	}

	private Set<MsFeature>getFeaturesInRange(List<MsFeature>features, Range rtRange){

		return features.stream().filter(f -> rtRange.contains(f.getRetentionTime())).collect(Collectors.toSet());
	}


	@Override
	public Task cloneTask() {

		return new SlidingWindowClusteringTask(
				 currentExperiment,
				 dataPipeline,
				 limitRtRange,
				 retentionRange,
				 filterMissing,
				 maxMissingPercent,
				 imputeMissing,
				 imputationMethod,
				 knnClusterNumber,
				 correlationAlgoritm,
				 correlationCutoff,
				 maxClusterWidth,
				 windowSlidingUnit,
				 featureNumberWindow,
				 featureTimeWindow);
	}

	public HashSet<MsFeatureCluster> getFeatureClusters() {

		return clusterList;
	}
}



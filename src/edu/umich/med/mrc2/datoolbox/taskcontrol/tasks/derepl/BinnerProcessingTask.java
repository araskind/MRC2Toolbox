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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.math3.ml.clustering.CentroidCluster;
import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer;
import org.apache.commons.math3.stat.correlation.KendallsCorrelation;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;
import org.ujmp.core.BaseMatrix;
import org.ujmp.core.Matrix;
import org.ujmp.core.calculation.Calculation.Ret;
import org.ujmp.core.export.destination.DefaultMatrixFileExportDestination;
import org.ujmp.core.util.MathUtil;

import edu.umich.med.mrc2.datoolbox.data.BinnerPreferencesObject;
import edu.umich.med.mrc2.datoolbox.data.ClusterableFeatureData;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureCluster;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureSet;
import edu.umich.med.mrc2.datoolbox.data.compare.MsFeatureComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.binner.control.BinClusteringCutoffType;
import edu.umich.med.mrc2.datoolbox.gui.binner.control.CorrelationFunctionType;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskEvent;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskListener;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.utils.DataSetUtils;
import edu.umich.med.mrc2.datoolbox.utils.ujmp.ImputeMedian;
import edu.umich.med.mrc2.datoolbox.utils.ujmp.Log1pTransform;

public class BinnerProcessingTask extends AbstractTask implements TaskListener {
	
	private BinnerPreferencesObject bpo;
	private DataAnalysisProject currentExperiment;
	private DataPipeline activeDataPipeline;
	private Matrix preparedDataMatrix;
	private Collection<MsFeature> cleanRTSortedFeatureSet;
	private Collection<MsFeatureCluster> featureBins;
	private Collection<MsFeatureCluster> featureClusters;
	private PearsonsCorrelation pearson;
	private SpearmansCorrelation spearman;
	private KendallsCorrelation kendall;
	
	public BinnerProcessingTask(BinnerPreferencesObject bpo) {
		super();
		this.bpo = bpo;
		currentExperiment = MRC2ToolBoxCore.getActiveMetabolomicsExperiment();
		activeDataPipeline = bpo.getDataPipeline();
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		setStatus(TaskStatus.PROCESSING);
		try {
			preparedDataMatrix = filterInputData();
		} catch (Exception e) {

			e.printStackTrace();
			setStatus(TaskStatus.ERROR);			
			return;
		}
		try {
			binFeaturesByRt();
		} catch (Exception e) {

			e.printStackTrace();
			setStatus(TaskStatus.ERROR);
			return;
		}
		try {
			createCorrelationMatrices();
		} catch (Exception e) {

			e.printStackTrace();
			setStatus(TaskStatus.ERROR);
			return;
		}
		try {
			clusterFeatureBins();
		} catch (Exception e) {

			e.printStackTrace();
			setStatus(TaskStatus.ERROR);
			return;
		}
		setStatus(TaskStatus.FINISHED);
	}

	private void clusterFeatureBins() {

		taskDescription = "Creating correlation matrices for bins ...";
		total = featureBins.size();
		processed = 0;
		for(MsFeatureCluster bin : featureBins) {
			
			if(!mustCluster(bin)) {
				
				processed++;
				continue;
			}
			
			processed++;
		}		
	}
	
	private boolean mustCluster(MsFeatureCluster bin) {
		
		if(bpo.getBinClusteringCutoffType().equals(BinClusteringCutoffType.ALL))
			return true;
		
		if(bpo.getBinClusteringCutoffType().equals(BinClusteringCutoffType.ABOVE_SCORE)
				&& bin.getFeatures().size() > bpo.getBinClusteringCutoff())
			return true;
		
		if(bpo.getBinClusteringCutoffType().equals(BinClusteringCutoffType.BELOW_SCORE)
				&& mustClusterIfSizeBelowCutoff(bin))
			return true;
		
		return false;
	}
	
	private boolean mustClusterIfSizeBelowCutoff(MsFeatureCluster bin) {
		
		if(bin.getCorrMatrix() == null)
			return false;
		
		double corrMean = bin.getCorrMatrix().getMeanValue();
		double log2BinSize = Math.log(bin.getFeatures().size()) / Math.log(2);
		double sqrtRTDiff = Math.sqrt(bin.getRtRange().getSize());
		
		double binScore = corrMean * corrMean / (sqrtRTDiff * log2BinSize);
		if (corrMean < 0.0)
			binScore = -binScore;
		
		if(binScore < bpo.getBinClusteringCutoff())
			return true;
		else
			return false;
	}
	
	private void clusterBinFeaturesOnCorrelations(MsFeatureCluster bin) {
		
		/**/
		Matrix distanceMatrix = bin.getCorrMatrix().euklideanDistance(Ret.NEW, true);
	}

	private void createCorrelationMatrices() {
		
		taskDescription = "Creating correlation matrices for bins ...";
		total = featureBins.size();
		processed = 0;
		
		pearson = new PearsonsCorrelation();
		spearman = new SpearmansCorrelation();
		kendall = new KendallsCorrelation();
		
		for(MsFeatureCluster bin : featureBins) {
			
			Collection<MsFeature>sorted = 
					bin.getSortedFeturesForDataPipeline(activeDataPipeline, SortProperty.RTmedObserved);
			MsFeature[]sortedFeatureArray = sorted.toArray(new MsFeature[sorted.size()]);
			long[] columnIndex =
					sorted.stream().
					map(f -> preparedDataMatrix.getColumnForLabel(f)).
					mapToLong(i -> i).
					toArray();			
			Matrix binDataMatrix = preparedDataMatrix.selectColumns(Ret.NEW, columnIndex);
			
			binDataMatrix.setMetaDataDimensionMatrix(0, Matrix.Factory.linkToArray((Object[])sortedFeatureArray));
			binDataMatrix.setMetaDataDimensionMatrix(1, preparedDataMatrix.getMetaDataDimensionMatrix(1));
			clusterFeatures(binDataMatrix);
			
			double[][] doubleMatrix = binDataMatrix.toDoubleArray();
			Matrix corrMatrix = null;
			
			if(bpo.getCorrelationFunctionType().equals(CorrelationFunctionType.PEARSON))
				corrMatrix = Matrix.Factory.linkToArray(pearson.computeCorrelationMatrix(doubleMatrix).getData());

			if(bpo.getCorrelationFunctionType().equals(CorrelationFunctionType.SPEARMAN))
				corrMatrix = Matrix.Factory.linkToArray(spearman.computeCorrelationMatrix(doubleMatrix).getData());

			if(bpo.getCorrelationFunctionType().equals(CorrelationFunctionType.KENDALL))
				corrMatrix = Matrix.Factory.linkToArray(kendall.computeCorrelationMatrix(doubleMatrix).getData());

			if(corrMatrix != null) {

				corrMatrix.setMetaDataDimensionMatrix(0, Matrix.Factory.linkToArray((Object[])sortedFeatureArray));
				corrMatrix.setMetaDataDimensionMatrix(1, Matrix.Factory.linkToArray((Object[])sortedFeatureArray).transpose(Ret.NEW));
				bin.setClusterCorrMatrix(corrMatrix);
			}			
			processed++;
		}
	}
	
	private void clusterFeatures(Matrix featureMatrix) {
		
		KMeansPlusPlusClusterer<ClusterableFeatureData> clusterer = 
				new KMeansPlusPlusClusterer<ClusterableFeatureData>(3);
		List<ClusterableFeatureData>dataToCluster = new ArrayList<ClusterableFeatureData>();
		long[]coord = new long[] {0,0};
		Matrix featureDataMatrix = featureMatrix.getMetaDataDimensionMatrix(0);
		for(long i=0; i<featureDataMatrix.getColumnCount(); i++) {
			
			coord[1] = i;
			MsFeature msf = (MsFeature)featureDataMatrix.getAsObject(coord);
			double[] featureData = featureMatrix.selectColumns(Ret.LINK, i).transpose().toDoubleArray()[0];
			dataToCluster.add(new ClusterableFeatureData(msf, featureData));
		}
		List<CentroidCluster<ClusterableFeatureData>> results = clusterer.cluster(dataToCluster);
		for(CentroidCluster<ClusterableFeatureData>cc : results) {
			
			System.out.println(((ClusterableFeatureData)cc.getCenter()).getFeature().toString());
		}
		
	}

	private void binFeaturesByRt() {
		
		taskDescription = "Creating RT-based bins ...";
		total = cleanRTSortedFeatureSet.size();
		processed = 0;
		featureBins = new ArrayList<MsFeatureCluster>();
		MsFeature[] featureArray = 
				cleanRTSortedFeatureSet.toArray(new MsFeature[cleanRTSortedFeatureSet.size()]);

		double rtGapForBinning = bpo.getRtGap();
		MsFeatureCluster newCluster = new MsFeatureCluster();
		newCluster.addFeature(featureArray[0], activeDataPipeline);
		int maxClusterSize = Math.min(bpo.getBinSizeLimitForAnalysis(), bpo.getBinSizeLimitForOutput());

		for (int i = 1; i < featureArray.length; i++) {

			double rtGap = featureArray[i].getMedianObservedRetention()
					- featureArray[i - 1].getMedianObservedRetention();
			if (rtGap < rtGapForBinning) {
				
				newCluster.addFeature(featureArray[i], activeDataPipeline);
				if(newCluster.getFeatures().size() > maxClusterSize) {
					
					errorMessage = 
							"Some of the feature bins exceede the size limit set in the prefefernces;\n"
							+ newCluster.getFeatures().size() + " vs allowed " + maxClusterSize;
					setStatus(TaskStatus.ERROR);
					return;
				}
			}
			else {
				if(newCluster.getFeatures().size() > 1)
					featureBins.add(newCluster);
				
				newCluster = new MsFeatureCluster();
				newCluster.addFeature(featureArray[i], activeDataPipeline);
			}
			processed++;
		}
		System.out.println("# of bins " + featureBins.size());
//		for(MsFeatureCluster c : featureBins) {
//			System.out.println("Bin size = " + c.getFeatures().size());
//		}		
	}

	private Matrix filterInputData() {

		taskDescription = "Preparing data matrix ...";
		total = 100;
		processed = 0;
		
		//	Subset the data
		MsFeatureSet activeFeatureSet = 
				currentExperiment.getActiveFeatureSetForDataPipeline(activeDataPipeline);
		Matrix dataMatrixForBinner = DataSetUtils.subsetDataMatrix(
				currentExperiment.getDataMatrixForDataPipeline(activeDataPipeline), 
				activeFeatureSet.getFeatures(), 
				bpo.getInputFiles());
		
		cleanRTSortedFeatureSet = 
				new ArrayList<MsFeature>(activeFeatureSet.getFeatures());
		
		//	Convert zero values to missing
		dataMatrixForBinner = dataMatrixForBinner.replace(Ret.NEW, Double.valueOf("0"), Double.NaN);
		
		taskDescription = "Removing features with high % of missing values ...";
		processed = 20;
		dataMatrixForBinner = removeFeaturesMissingAboveThreshold(
						dataMatrixForBinner, bpo.getMissingRemovalThreshold());
		
		taskDescription = "Replacing outliers with missing values ...";
		processed = 40;
		//	Find outlier values and set them as missing
		convertOutliersToMissing(dataMatrixForBinner, bpo.getOutlierSDdeviation());
		
		taskDescription = "Imputing missing values ...";
		processed = 60;
		//	Impute missing data and log-transform if required
		dataMatrixForBinner = imputeMissingData(dataMatrixForBinner);
		if(bpo.isLogTransform()) {
			
			taskDescription = "Log-transforming the data ...";
			processed = 80;
			dataMatrixForBinner = 
					new Log1pTransform(true, dataMatrixForBinner).calc(Ret.NEW);
			
			//	writeMatrixToFile(dataMatrixForBinner, "log_transformed.txt", true);	
		}
		return dataMatrixForBinner;
	}
		
	private Matrix removeFeaturesMissingAboveThreshold(
			Matrix dataMatrixForBinner, 
			double missingRemovalThreshold) {

		Matrix missingnessMatrix  = dataMatrixForBinner.
				countMissing(Ret.NEW, BaseMatrix.ROW).
				divide(dataMatrixForBinner.getRowCount() / 100.0d);
		Matrix featureMetadataMatrix = 
				dataMatrixForBinner.getMetaDataDimensionMatrix(0);
		Collection<MsFeature>featuresToRemove = new ArrayList<MsFeature>();
				
		long[]coord = new long[] {0,0};
		ArrayList<Long> toRemove = new ArrayList<Long>();
//		Map<MsFeature,Double>highMissingnessMap = 
//				new TreeMap<MsFeature,Double>(new MsFeatureComparator(SortProperty.RT));
		for(long c=0; c<missingnessMatrix.getColumnCount(); c++) {
			
			coord[1] = c;
			double missingness = missingnessMatrix.getAsDouble(coord);
			if(missingness > missingRemovalThreshold) {
				toRemove.add(c);
				MsFeature msf = (MsFeature)featureMetadataMatrix.getAsObject(coord);
				featuresToRemove.add(msf);
//				highMissingnessMap.put(
//						(MsFeature)featureMetadataMatrix.getAsObject(coord), 
//						missingness);
			}
		}
		Matrix newMsFeatureLabelMatrix = 
				dataMatrixForBinner.getMetaDataDimensionMatrix(0).deleteColumns(Ret.NEW, toRemove);			
		Matrix newDataMatrixForBinner = dataMatrixForBinner.deleteColumns(Ret.NEW, toRemove);
		newDataMatrixForBinner.setMetaDataDimensionMatrix(0, newMsFeatureLabelMatrix);
		newDataMatrixForBinner.setMetaDataDimensionMatrix(1, dataMatrixForBinner.getMetaDataDimensionMatrix(1));
		
		if(!featuresToRemove.isEmpty())
			cleanRTSortedFeatureSet.removeAll(featuresToRemove);
		
		cleanRTSortedFeatureSet = 
				cleanRTSortedFeatureSet.stream().
				sorted(new MsFeatureComparator(SortProperty.RT)).
				collect(Collectors.toList());
		
		return newDataMatrixForBinner;
	}
	
	private void convertOutliersToMissing(Matrix dataMatrixForBinner, double stDevMargin) {
		
		Matrix standardized = dataMatrixForBinner.standardize(Ret.NEW, BaseMatrix.ROW);
		for(long i=0; i<dataMatrixForBinner.getRowCount(); i++) {
			
			for(long j=0; j<dataMatrixForBinner.getColumnCount(); j++) {
				
				double value = standardized.getAsDouble(i,j);
				if(!MathUtil.isNaNOrInfinite(value) && Math.abs(value) > stDevMargin)
					dataMatrixForBinner.setAsDouble(Double.NaN, i,j);
			}		
		}
		//	writeMatrixToFile(standardized, "z-scores.txt", true);
		//	writeMatrixToFile(dataMatrixForBinner, "outliers_replaced.txt", true);		
	}
	
	//	TODO start with median value, maybe add other options later
	private Matrix imputeMissingData(Matrix dataMatrixForBinner) {
		
		//	writeMatrixToFile(dataMatrixForBinner, "beforeImpute.txt");		

		Matrix imputedMatrixForBinner = 
				new ImputeMedian(BaseMatrix.ROW, dataMatrixForBinner).calc(Ret.NEW);
		
		//	writeMatrixToFile(imputedMatrixForBinner, "afterImpute.txt");
		
		return imputedMatrixForBinner;
	}
	
	@Override
	public void statusChanged(TaskEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public Task cloneTask() {
		return new BinnerProcessingTask(bpo) ;
	}
	
	/**
	 * Debugging helper functions
	 */
	
	/**
	 * @param matrix
	 * @param fileName
	 * @param transpose
	 */
	private void writeMatrixToFile(Matrix matrix, String fileName, boolean transpose) {
		
		Path outputPath = Paths.get(currentExperiment.getExportsDirectory().getAbsolutePath(), fileName);
		Matrix matrixForOutput = matrix;
		if(transpose)
			matrixForOutput = matrixForOutput.transpose();
		
		DefaultMatrixFileExportDestination dest = 
				new DefaultMatrixFileExportDestination(matrixForOutput, outputPath.toFile());
		try {
			dest.asDenseCSV();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void writeOutFeatureCleanupData(Map<MsFeature,Double>dataMap, String fileName) {
		
		ArrayList<String>output = new ArrayList<String>();
		ArrayList<String>line = new ArrayList<String>();
		line.add("FeatureName");
		line.add("MZ");
		line.add("RT");
		line.add("ParameterValue");
		output.add(StringUtils.join(line, '\t'));
		for(Entry<MsFeature,Double>ent : dataMap.entrySet()) {
			
			line.clear();
			line.add(ent.getKey().getName());
			line.add(MRC2ToolBoxConfiguration.getMzFormat().format(ent.getKey().getMonoisotopicMz()));
			line.add(MRC2ToolBoxConfiguration.getRtFormat().format(ent.getKey().getRetentionTime()));
			line.add(MRC2ToolBoxConfiguration.getPpmFormat().format(ent.getValue()));
			output.add(StringUtils.join(line, '\t'));
		}		
		Path outputPath = Paths.get(currentExperiment.getExportsDirectory().getAbsolutePath(), fileName);
		try {
			Files.write(outputPath, 
					output, 
					StandardCharsets.UTF_8,
					StandardOpenOption.CREATE, 
					StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

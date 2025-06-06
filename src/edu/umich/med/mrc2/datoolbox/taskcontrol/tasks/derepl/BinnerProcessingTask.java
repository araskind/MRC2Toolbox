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
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.ujmp.core.Matrix;
import org.ujmp.core.calculation.Calculation.Ret;
import org.ujmp.core.export.destination.DefaultMatrixFileExportDestination;
import org.ujmp.core.util.MathUtil;

import edu.umich.med.mrc2.datoolbox.data.BinnerPreferencesObject;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureSet;
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
	private Matrix preparedDataMatrix;
	
	public BinnerProcessingTask(BinnerPreferencesObject bpo) {
		super();
		this.bpo = bpo;
		currentExperiment = MRC2ToolBoxCore.getActiveMetabolomicsExperiment();
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
		setStatus(TaskStatus.FINISHED);
	}

	private void binFeaturesByRt() {
		
		taskDescription = "Creating RT-based bins ...";
		total = 100;
		processed = 0;	
	}

	private Matrix filterInputData() {

		taskDescription = "Preparing data matrix ...";
		total = 100;
		processed = 0;
		
		//	Subset the data
		MsFeatureSet activeFeatureSet = 
				currentExperiment.getActiveFeatureSetForDataPipeline(bpo.getDataPipeline());
		Matrix dataMatrixForBinner = DataSetUtils.subsetDataMatrix(
				currentExperiment.getDataMatrixForDataPipeline(bpo.getDataPipeline()), 
				activeFeatureSet.getFeatures(), 
				bpo.getInputFiles());
		
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
			
			writeMatrixToFile(dataMatrixForBinner, "log_transformed.txt", true);	
		}
		return dataMatrixForBinner;
	}
		
	private Matrix removeFeaturesMissingAboveThreshold(
			Matrix dataMatrixForBinner, 
			double missingRemovalThreshold) {

		Matrix missingnessMatrix  = dataMatrixForBinner.
				countMissing(Ret.NEW, Matrix.ROW).
				divide(dataMatrixForBinner.getRowCount() / 100.0d);
//		Matrix featureMetadataMatrix = 
//				dataMatrixForBinner.getMetaDataDimensionMatrix(0);
				
		long[]coord = new long[] {0,0};
		ArrayList<Long> toRemove = new ArrayList<Long>();
//		Map<MsFeature,Double>highMissingnessMap = 
//				new TreeMap<MsFeature,Double>(new MsFeatureComparator(SortProperty.RT));
		for(long c=0; c<missingnessMatrix.getColumnCount(); c++) {
			
			coord[1] = c;
			double missingness = missingnessMatrix.getAsDouble(coord);
			if(missingness > missingRemovalThreshold) {
				toRemove.add(c);
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
		return newDataMatrixForBinner;
	}
	
	private void convertOutliersToMissing(Matrix dataMatrixForBinner, double stDevMargin) {
		
		Matrix standardized = dataMatrixForBinner.standardize(Ret.NEW, Matrix.ROW);
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
				new ImputeMedian(Matrix.ROW, dataMatrixForBinner).calc(Ret.NEW);
		
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

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
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.ujmp.core.BaseMatrix;
import org.ujmp.core.Matrix;
import org.ujmp.core.calculation.Calculation.Ret;

import edu.umich.med.mrc2.datoolbox.data.BinnerPreferencesObject;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureSet;
import edu.umich.med.mrc2.datoolbox.data.compare.MsFeatureComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskEvent;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskListener;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.utils.DataSetUtils;

public class BinnerProcessingTask extends AbstractTask implements TaskListener {
	
	private BinnerPreferencesObject bpo;
	private DataAnalysisProject currentExperiment;
	
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
			filterInputData();
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
		
		// Create data matrix for selected files
		
	}

	private void filterInputData() {

		//	Subset the data
		MsFeatureSet activeFeatureSet = 
				currentExperiment.getActiveFeatureSetForDataPipeline(bpo.getDataPipeline());
		Matrix dataMatrixForBinner = DataSetUtils.subsetDataMatrix(
				currentExperiment.getDataMatrixForDataPipeline(bpo.getDataPipeline()), 
				activeFeatureSet.getFeatures(), 
				bpo.getInputFiles());
		
		System.out.println("# features before missing cleanup: " 
				+ dataMatrixForBinner.getColumnCount());
		dataMatrixForBinner = removeFeaturesMissingAboveThreshold(
						dataMatrixForBinner, bpo.getMissingRemovalThreshold());
		System.out.println("# features after missing cleanup: " 
				+ dataMatrixForBinner.getColumnCount());
	}
	
	private Matrix removeFeaturesMissingAboveThreshold(
			Matrix dataMatrixForBinner, 
			double missingRemovalThreshold) {

		Matrix missingnessMatrix  = dataMatrixForBinner.
				replace(Ret.LINK, Double.valueOf("0"), Double.NaN).
				countMissing(Ret.NEW, BaseMatrix.ROW).
				divide(dataMatrixForBinner.getRowCount() / 100.0d);
		Matrix featureMetadataMatrix = 
				dataMatrixForBinner.getMetaDataDimensionMatrix(0);
				
		long[]coord = new long[] {0,0};
		ArrayList<Long> toRemove = new ArrayList<Long>();
		Map<MsFeature,Double>highMissingnessMap = 
				new TreeMap<MsFeature,Double>(new MsFeatureComparator(SortProperty.RT));
		for(long c=0; c<missingnessMatrix.getColumnCount(); c++) {
			
			coord[1] = c;
			double missingness = missingnessMatrix.getAsDouble(coord);
			if(missingness > missingRemovalThreshold) {
				toRemove.add(c);
				highMissingnessMap.put(
						(MsFeature)featureMetadataMatrix.getAsObject(coord), 
						missingness);
			}
		}
		Matrix newMsFeatureLabelMatrix = 
				dataMatrixForBinner.getMetaDataDimensionMatrix(0).deleteColumns(Ret.NEW, toRemove);			
		Matrix newDataMatrixForBinner = dataMatrixForBinner.deleteColumns(Ret.NEW, toRemove);
		newDataMatrixForBinner.setMetaDataDimensionMatrix(0, newMsFeatureLabelMatrix);
		newDataMatrixForBinner.setMetaDataDimensionMatrix(1, dataMatrixForBinner.getMetaDataDimensionMatrix(1));
		
		//	Write diagnostic list of features removed based on missingness
		writeOutFeatureCleanupData(highMissingnessMap, "featuresWithHighMissingness.txt");
		
		return newDataMatrixForBinner;
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

	@Override
	public void statusChanged(TaskEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public Task cloneTask() {
		return new BinnerProcessingTask(bpo) ;
	}
}

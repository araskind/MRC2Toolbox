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

package edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.io;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.math3.util.Precision;
import org.ujmp.core.Matrix;

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignSubset;
import edu.umich.med.mrc2.datoolbox.data.ExperimentalSample;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureCluster;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureClusterSet;
import edu.umich.med.mrc2.datoolbox.data.compare.MsFeatureClusterComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.enums.DataExportFields;
import edu.umich.med.mrc2.datoolbox.data.enums.MissingExportType;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.utils.DataExportUtils;

public class IntegratedDataSetExportTask extends AbstractTask {

	private static final String doubleValueCleanupString = 
			"\n|\r|\\u000a|\\u000d|\t|\\u0009|\\u00ad";
	
	private static final String METABOLITE_NAME = "metabolite_name";
	private static final String MOLECULAR_FORMULA = "formula";
	private static final String SEPARATION_MODE = "mode";
		
	private DataAnalysisProject currentExperiment;
	MsFeatureClusterSet integratedDataSet;
	private MissingExportType exportMissingAs;
	boolean replaceSpecialCharacters;
	private File exportFile;
	
	private DataExportFields namingField;
	private Collection<MsFeature> msFeatureSet4export;
	private ExperimentDesignSubset experimentDesignSubset;
	private TreeSet<ExperimentalSample>activeSamples;
			
	public IntegratedDataSetExportTask(
			DataAnalysisProject currentExperiment, 
			MsFeatureClusterSet integratedDataSet,
			MissingExportType exportMissingAs, 
			boolean replaceSpecialCharacters,
			File exportFile) {
		super();
		this.currentExperiment = currentExperiment;
		this.integratedDataSet = integratedDataSet;
		this.exportMissingAs = exportMissingAs;
		this.replaceSpecialCharacters = replaceSpecialCharacters;
		this.exportFile = exportFile;
	}

	@Override
	public void run() {

		taskDescription = "";
		setStatus(TaskStatus.PROCESSING);
		try {
			exportIntegratedDataSet();
		}
		catch (Exception e) {
			reportErrorAndExit(e);
		}
		setStatus(TaskStatus.FINISHED);
	}
	
	private void exportIntegratedDataSet() {

		taskDescription = "Writing data export file for Metabolomics Workbench ...";
		total = integratedDataSet.getNumberOfClusters();
		processed = 0;
		
		List<String>lines = new ArrayList<>();
		
		//	Create header
		List<String>header = new ArrayList<>();
		header.add(METABOLITE_NAME);
		header.add(MOLECULAR_FORMULA);
		header.add(SEPARATION_MODE);
		
		Map<ExperimentalSample, Map<DataPipeline, DataFile[]>>sampleFileMap = 
				DataExportUtils.createSampleFileMap(
											currentExperiment, 
											currentExperiment.getExperimentDesign().getActiveDesignSubset(), 
											integratedDataSet.getDataPipelines(),
											DataExportFields.SAMPLE_EXPORT_NAME);
		String[]sampleNamesArray =  
				DataExportUtils.createSampleColumnNameArray(
						sampleFileMap, DataExportFields.SAMPLE_EXPORT_NAME);
		Map<String,Integer>columnNameIndexMap = new TreeMap<>();		
		for(int i=0; i<sampleNamesArray.length; i++)
			columnNameIndexMap.put(sampleNamesArray[i], i);
		
		Map<DataPipeline,Map<DataFile,String>>dataFileColumnNameMap 
			= DataExportUtils.createDataFileColumnNameMap(sampleFileMap, DataExportFields.SAMPLE_EXPORT_NAME);
			
		header.addAll(Arrays.asList(sampleNamesArray));		
		lines.add(StringUtils.join(header, MRC2ToolBoxConfiguration.TAB_DATA_DELIMITER));
		long[]coordinates = new long[2];
		
		List<MsFeatureCluster> sortedClusters = integratedDataSet.getClusters().stream().
			sorted(new MsFeatureClusterComparator(SortProperty.pimaryId)).
			collect(Collectors.toList());

		List<String>line = new ArrayList<>();
		for(MsFeatureCluster cluster : sortedClusters) {
			
			line.clear();
			MsFeature feature = cluster.getPrimaryFeature();
			DataPipeline dp = cluster.getDataPipelineForFeature(feature);
			Matrix dataMatrix = currentExperiment.getDataMatrixForDataPipeline(dp);
					
			//	Metadata
			line.add(feature.getName());
			line.add(feature.getPrimaryIdentity().getCompoundIdentity().getFormula());
			line.add(dp.getMotrpacAssay().getBucketCode());			
			
			//	Peak areas
			coordinates[1] = dataMatrix.getColumnForLabel(feature);
			String[]peakAreas = new String[sampleNamesArray.length];
			for(Entry<ExperimentalSample, Map<DataPipeline, DataFile[]>>sfe : sampleFileMap.entrySet()) {
				
				for(DataFile df :sfe.getValue().get(dp)) {

					int index = columnNameIndexMap.get(dataFileColumnNameMap.get(dp).get(df));					
					coordinates[0] = dataMatrix.getRowForLabel(df);
					double value = dataMatrix.getAsDouble(coordinates);
					if (Double.isNaN(value))
						value = 0.0d;
					else
						value = Math.round(value * 10.0) / 10.0;
					
					String valueString = Double.toString(value).replaceAll(doubleValueCleanupString, " ");
					if (Precision.equals(value, 0.0d, Precision.EPSILON)) {

						valueString = "0.0";
						if (exportMissingAs.equals(MissingExportType.AS_MISSING))
							valueString = "";
					}					
					peakAreas[index] = valueString;
				}				
			}
			line.addAll(Arrays.asList(peakAreas));
			lines.add(StringUtils.join(line, MRC2ToolBoxConfiguration.TAB_DATA_DELIMITER));
			processed++;
		}
		try {
		    Files.write(exportFile.toPath(), 
		    		lines,
		            StandardCharsets.UTF_8,
		            StandardOpenOption.CREATE, 
		            StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
		    e.printStackTrace();
		}
	}
	
	@Override
	public Task cloneTask() {

		return new IntegratedDataSetExportTask(
				 currentExperiment, 
				 integratedDataSet,
				 exportMissingAs,
				 replaceSpecialCharacters,
				 exportFile);
	}
}

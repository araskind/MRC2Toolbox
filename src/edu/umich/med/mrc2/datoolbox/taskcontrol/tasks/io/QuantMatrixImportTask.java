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
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.ujmp.core.Matrix;
import org.ujmp.core.calculation.Calculation.Ret;

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.ExperimentalSample;
import edu.umich.med.mrc2.datoolbox.data.MassSpectrum;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureStatisticalSummary;
import edu.umich.med.mrc2.datoolbox.data.compare.MsFeatureComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortDirection;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundAnnotationField;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundDatabaseEnum;
import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.data.enums.MsStringType;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.utils.DataImportUtils;
import edu.umich.med.mrc2.datoolbox.utils.DelimitedTextParser;
import edu.umich.med.mrc2.datoolbox.utils.MsUtils;

public class QuantMatrixImportTask extends AbstractTask {

	private File quantDataFile;
	private DataPipeline dataPipeline;
	private DataAnalysisProject currentExperiment;
	private ArrayList<MsFeature> featureList;
	private ArrayList<DataFile> dataFiles;
	private Matrix dataMatrix;
	private String sampleIdMask, sampleNameMask;
	private Matcher regexMatcher;
	private Pattern sampleIdPattern, sampleNamePattern, offsetFeaturePattern;
	private String[][] quantDataArray;
	private ArrayList<Integer> sampleColumnIndex;
	private HashMap<CompoundDatabaseEnum, Integer> idIndex;
	private int nameColumn = -1;
	private int rtColumn = -1;
	private int neutralMassColumn = -1;
	private int compositeSpectrumColumn = -1;
	private TreeMap<MsFeature, ArrayList<Integer>> featureRowMap;
	private boolean unmatchedSamplesPresent;

	private final static String shiftedRtPattern = " \\+|\\-\\s*\\d+\\.\\d+\\s*:*\\d*$";

	public QuantMatrixImportTask(File quantDataFile, DataPipeline dataPipeline) {

		super();

		this.quantDataFile = quantDataFile;
		this.dataPipeline = dataPipeline;

		currentExperiment = MRC2ToolBoxCore.getActiveMetabolomicsExperiment();
		taskDescription = "Reading quant matrix from " + quantDataFile.getName();
		unmatchedSamplesPresent = false;
		readPreferences();
		offsetFeaturePattern = Pattern.compile(shiftedRtPattern);
		featureList = new ArrayList<MsFeature>();
	}

	private void readPreferences() {

		sampleIdMask = MRC2ToolBoxConfiguration.getSampleIdMask();
		sampleIdPattern = Pattern.compile(sampleIdMask);
		sampleNameMask = MRC2ToolBoxConfiguration.getSampleNameMask();
		sampleNamePattern = Pattern.compile(sampleNameMask);
	}

	@Override
	public void run() {

		setStatus(TaskStatus.PROCESSING);
		try {
			if (!currentExperiment.dataPipelineHasData(dataPipeline)) {

				parseHeader();
				createFeatureRowMap();
				parseQuantData();
				copyQuantFileToExperiment();
			}
		} catch (Exception e) {

			e.printStackTrace();
			setStatus(TaskStatus.ERROR);
return;

		}
		setStatus(TaskStatus.FINISHED);
	}

	@Override
	public Task cloneTask() {
		return new QuantMatrixImportTask(quantDataFile, dataPipeline);
	}

	private void copyQuantFileToExperiment() {

		if (quantDataFile.exists()) {

			File quantFile = Paths.get(currentExperiment.getExperimentDirectory().getAbsolutePath(),
					quantDataFile.getName()).toFile();
			if (!quantFile.exists()) {

				try {
					FileUtils.copyFileToDirectory(quantDataFile, currentExperiment.getExperimentDirectory());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void createFeatureRowMap() {

		featureRowMap = new TreeMap<MsFeature, ArrayList<Integer>>(
				new MsFeatureComparator(SortProperty.Name, SortDirection.ASC));
		HashMap<MsFeature, Integer> offsetMap = new HashMap<MsFeature, Integer>();

		taskDescription = "Parsing MS feature data";
		total = quantDataArray.length - 1;
		processed = 0;

		for (int i = 1; i < quantDataArray.length; i++) {

			// Create feature for annotation
			double rt = 0.0d;
			double neutralMass = 0.0d;

			if (rtColumn >= 0)
				rt = Double.parseDouble(quantDataArray[i][rtColumn]);

			if (neutralMassColumn >= 0)
				neutralMass = Double.parseDouble(quantDataArray[i][neutralMassColumn]);

			MsFeature cf = new MsFeature(quantDataArray[i][nameColumn], neutralMass, rt);
			MsFeatureStatisticalSummary statsSummary = new MsFeatureStatisticalSummary(cf);
			cf.setStatsSummary(statsSummary);

			if(compositeSpectrumColumn >= 0) {
				MassSpectrum ms = MsUtils.parseMsString(
						
						
						quantDataArray[i][compositeSpectrumColumn], MsStringType.MPP);

				cf.setSpectrum(ms);
			}
			regexMatcher = offsetFeaturePattern.matcher(cf.getName());

			if (regexMatcher.find())
				offsetMap.put(cf, i);
			else {
				ArrayList<Integer> rows = new ArrayList<Integer>();
				rows.add(i);
				featureRowMap.put(cf, rows);
			}
			processed++;
		}
		processed = 0;

		for (Entry<MsFeature, ArrayList<Integer>> entry : featureRowMap.entrySet()) {

			if (entry.getKey().getName().startsWith(DataPrefix.MS_LIBRARY_UNKNOWN_TARGET.getName())) {

				offsetFeaturePattern = Pattern.compile("^" + entry.getKey().getName());

				for (Entry<MsFeature, Integer> of : offsetMap.entrySet()) {

					regexMatcher = offsetFeaturePattern.matcher(of.getKey().getName());

					if (regexMatcher.find()) {
						try {

							featureRowMap.get(entry.getKey()).add(of.getValue());
						} catch (Exception e) {
							// TODO Auto-generated catch block
							ArrayList<Integer> itg = featureRowMap.get(entry.getKey());
							e.printStackTrace();
						}
					}
				}
			}
		}
	}

	public DataPipeline getDataPipeline() {
		return dataPipeline;
	}

	public Collection<DataFile> getDataFiles() {
		return dataFiles;
	}

	public Matrix getDataMatrix() {
		return dataMatrix;
	}

	public Collection<MsFeature> getFeatureList() {
		featureList.clear();
		featureList.addAll(featureRowMap.keySet());
		return featureList;
	}

	public boolean hasUnmatchedSamples() {
		return unmatchedSamplesPresent;
	}

	private void parseHeader() {

		nameColumn = -1;
		rtColumn = -1;
		neutralMassColumn = -1;
		sampleColumnIndex = new ArrayList<Integer>();
		idIndex = new HashMap<CompoundDatabaseEnum, Integer>();
		dataFiles = new ArrayList<DataFile>();
		total = 100;
		processed = 20;
		taskDescription = "Parsing sample data";
		quantDataArray = null;

		try {
			quantDataArray = DelimitedTextParser.parseTextFileWithEncoding(
					quantDataFile, MRC2ToolBoxConfiguration.getTabDelimiter());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(quantDataArray == null) {

			setStatus(TaskStatus.ERROR);
			return;
		}
		String sampleId = "";
		String sampleName = "";
		boolean isSample;

		if (quantDataArray != null) {

			// Parse header line
			String[] header = quantDataArray[0];

			for (int i = 0; i < header.length; i++) {

				// Find positions of sample columns
				isSample = true;
				for (CompoundAnnotationField af : CompoundAnnotationField.values()) {

					if (header[i].equals(af.getName())) {
						isSample = false;
						break;
					}
				}
				if (isSample) {

					sampleColumnIndex.add(i);
					sampleId = "";
					sampleName = "";
					ExperimentalSample matchedSample = null;

					// Create data files
					DataFile df = new DataFile(header[i].trim(), 
							dataPipeline.getAcquisitionMethod());
					dataFiles.add(df);

					// Connect data files to samples if possible
					regexMatcher = sampleIdPattern.matcher(df.getName());

					if (regexMatcher.find())
						sampleId = regexMatcher.group();

					if (!sampleId.isEmpty())
						matchedSample = DataImportUtils.findSampleById(sampleId);

					if (matchedSample == null) {

						// Match by name
						regexMatcher = sampleNamePattern.matcher(df.getName());

						if (regexMatcher.find())
							sampleName = regexMatcher.group();

						matchedSample = DataImportUtils.findSampleByName(sampleName);
					}
					if (matchedSample != null)
						matchedSample.addDataFile(df);
					else
						unmatchedSamplesPresent = true;
				}
				// Find positions of annotation columns
				if (header[i].equals(CompoundAnnotationField.NAME.getName())
						|| header[i].equals(CompoundAnnotationField.COMPOUND_NAME.getName()))
					nameColumn = i;

				if (header[i].equals(CompoundAnnotationField.RETENTION.getName())
						|| header[i].equals(CompoundAnnotationField.RT.getName()))
					rtColumn = i;

				if (header[i].equals(CompoundAnnotationField.MASS.getName()))
					neutralMassColumn = i;

				if (header[i].equals(CompoundAnnotationField.SPECTRUM.getName())
						|| header[i].equals(CompoundAnnotationField.MS1_SPECTRUM.getName()))
					compositeSpectrumColumn = i;

				if (header[i].equals(CompoundAnnotationField.CAS.getName())
						|| header[i].equals(CompoundAnnotationField.CAS_ID.getName()))
					idIndex.put(CompoundDatabaseEnum.CAS, i);

				if (header[i].equals(CompoundAnnotationField.KEGG.getName()))
					idIndex.put(CompoundDatabaseEnum.KEGG, i);

				if (header[i].equals(CompoundAnnotationField.LIPIDMAPS.getName()))
					idIndex.put(CompoundDatabaseEnum.LIPIDMAPS, i);

				if (header[i].equals(CompoundAnnotationField.METLIN.getName()))
					idIndex.put(CompoundDatabaseEnum.METLIN, i);

				if (header[i].equals(CompoundAnnotationField.PUBCHEM.getName()))
					idIndex.put(CompoundDatabaseEnum.PUBCHEM, i);
			}
		} else {
			setStatus(TaskStatus.ERROR);
return;

		}
	}

	private void parseQuantData() {

		total = featureRowMap.size();
		processed = 0;

		double[][] quantitativeMatrix = new double[sampleColumnIndex.size()][total];

		Integer[] dataIndex = sampleColumnIndex.toArray(new Integer[sampleColumnIndex.size()]);
		String dpString = "";

		for (Entry<MsFeature, ArrayList<Integer>> entry : featureRowMap.entrySet()) {

			for (int row : entry.getValue()) {

				for (int j = 0; j < dataIndex.length; j++) {

					dpString = quantDataArray[row][dataIndex[j]].trim();

					//	This will be obsolete when CEFs aligned properly
					//	Add data from +/- rt features if not already
					if (!dpString.isEmpty() && quantitativeMatrix[j][processed] == 0.0d)
						quantitativeMatrix[j][processed] = Double.parseDouble(dpString);
				}
			}
			processed++;
		}
		MsFeature[] fcArray = featureRowMap.keySet().toArray(new MsFeature[featureRowMap.keySet().size()]);
		DataFile[] dfArray = dataFiles.toArray(new DataFile[dataFiles.size()]);

		dataMatrix = Matrix.Factory.linkToArray(quantitativeMatrix);
		dataMatrix.setMetaDataDimensionMatrix(0, Matrix.Factory.linkToArray(fcArray));
		dataMatrix.setMetaDataDimensionMatrix(1, Matrix.Factory.linkToArray(dfArray).transpose(Ret.NEW));
	}

}

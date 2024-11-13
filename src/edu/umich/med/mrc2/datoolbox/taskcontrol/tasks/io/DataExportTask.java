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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.commons.compress.utils.FileNameUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.ujmp.core.Matrix;

import edu.umich.med.mrc2.datoolbox.data.AdductMatch;
import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesign;
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignFactor;
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignSubset;
import edu.umich.med.mrc2.datoolbox.data.ExperimentalSample;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureCluster;
import edu.umich.med.mrc2.datoolbox.data.SimpleMsFeature;
import edu.umich.med.mrc2.datoolbox.data.Worklist;
import edu.umich.med.mrc2.datoolbox.data.WorklistItem;
import edu.umich.med.mrc2.datoolbox.data.compare.MsFeatureComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.enums.BinnerExportFields;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundDatabaseEnum;
import edu.umich.med.mrc2.datoolbox.data.enums.DataExportFields;
import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.data.enums.ExperimentDesignFields;
import edu.umich.med.mrc2.datoolbox.data.enums.MPPExportFields;
import edu.umich.med.mrc2.datoolbox.data.enums.MSFeatureSetStatisticalParameters;
import edu.umich.med.mrc2.datoolbox.data.enums.MissingExportType;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.main.AdductManager;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.utils.DataExportUtils;
import edu.umich.med.mrc2.datoolbox.utils.ExperimentUtils;
import edu.umich.med.mrc2.datoolbox.utils.MsUtils;
import edu.umich.med.mrc2.datoolbox.utils.WorklistUtils;

public class DataExportTask extends AbstractTask {

	/*
	 * Comment
	 * */

	private static final String lineSeparator = System.getProperty("line.separator");
	private File exportFile;
	private MainActionCommands exportType;
	private MissingExportType exportMissingAs;
	private DataAnalysisProject currentExperiment;
	private DataPipeline dataPipeline;
	private char columnSeparator;
	private boolean enableFilters;
	private double maxPooledRsd = -1;
	private double minPooledFrequency = -1;
	private TreeMap<DataFile, String> timeMap;
	private Collection<MsFeature> msFeatureSet4export;
	private DataExportFields namingField;
	private ExperimentDesignSubset experimentDesignSubset;
	private TreeSet<ExperimentalSample>activeSamples;
	private boolean exportManifest;
	boolean replaceSpecialCharacters;
	
	private static final NumberFormat rtFormat = MRC2ToolBoxConfiguration.getRtFormat();
	private static final NumberFormat mzFormat = MRC2ToolBoxConfiguration.getMzFormat();
	private static final NumberFormat peakAreaFormat = new DecimalFormat("###");
	private static final NumberFormat percentFormat = NumberFormat.getPercentInstance();

	public DataExportTask(
			DataAnalysisProject experiment,
			DataPipeline dataPipeline,
			File exportFile,
			MainActionCommands exportType,
			MissingExportType missingExportType,
			boolean enableFilters,
			double pooledRsdCutoff,
			double pooledFrequencyCutoff,
			DataExportFields namingField,
			boolean exportManifest,
			boolean replaceSpecialCharacters) {
		
		this.currentExperiment = experiment;
		this.dataPipeline = dataPipeline;
		this.exportFile = exportFile;
		this.exportType = exportType;
		this.exportMissingAs = missingExportType;
		this.enableFilters = enableFilters;
		this.maxPooledRsd = pooledRsdCutoff;
		this.minPooledFrequency = pooledFrequencyCutoff;
		this.namingField = namingField;
		this.exportManifest = exportManifest;
		this.replaceSpecialCharacters = replaceSpecialCharacters;
		
		columnSeparator = MRC2ToolBoxConfiguration.getTabDelimiter();
		timeMap = new TreeMap<DataFile, String>();
	}

	@Override
	public void run() {

		total = 100;
		processed = 50;

		setStatus(TaskStatus.PROCESSING);

		ExperimentDesign design = currentExperiment.getExperimentDesign();
		experimentDesignSubset = design.getActiveDesignSubset();
		activeSamples = design.getActiveSamplesForDesignSubset(experimentDesignSubset);
		
		if(msFeatureSet4export == null || msFeatureSet4export.isEmpty())
			collectFeaturesForExport();
		else {	//	remove empty data
			msFeatureSet4export =
					msFeatureSet4export.stream().
					filter(f -> f.getAveragePeakArea()> 0.0d).
					sorted(new MsFeatureComparator(SortProperty.RT)).
					collect(Collectors.toList());			
		}
		
		if(exportManifest)
			writeManifestFile();
		
		if (exportType.equals(MainActionCommands.EXPORT_RESULTS_4R_COMMAND)) {
			try {
				writeRexportFile();
				setStatus(TaskStatus.FINISHED);
			} catch (Exception e) {
				e.printStackTrace();
				setStatus(TaskStatus.ERROR);
				return;
			}
		}
		if (exportType.equals(MainActionCommands.EXPORT_RESULTS_4MPP_COMMAND)) {
			try {
				writeMPPexportFile();
				setStatus(TaskStatus.FINISHED);
			} catch (Exception e) {
				e.printStackTrace();
				setStatus(TaskStatus.ERROR);
				return;
			}
		}
		if (exportType.equals(MainActionCommands.EXPORT_RESULTS_4BINNER_COMMAND)) {
			try {
				writeBinnerExportFile();
				setStatus(TaskStatus.FINISHED);
			} catch (Exception e) {
				e.printStackTrace();
				setStatus(TaskStatus.ERROR);
				return;
			}
		}
		if (exportType.equals(MainActionCommands.EXPORT_DUPLICATES_COMMAND)) {
			try {
				writeDuplicatesExportFile();
				setStatus(TaskStatus.FINISHED);
			} catch (Exception e) {
				e.printStackTrace();
				setStatus(TaskStatus.ERROR);
				return;
			}
		}
		if (exportType.equals(MainActionCommands.EXPORT_RESULTS_FOR_METABOLOMICS_WORKBENCH_COMMAND)) {
			try {
				writeMetabolomicsWorkbenchExportFile();
				setStatus(TaskStatus.FINISHED);
			} catch (Exception e) {
				e.printStackTrace();
				setStatus(TaskStatus.ERROR);
				return;
			}
		}
		if (exportType.equals(MainActionCommands.EXPORT_MZRT_STATISTICS_COMMAND)) {
			try {
				writeMZRTDataExportFile();
				setStatus(TaskStatus.FINISHED);
			} catch (Exception e) {
				e.printStackTrace();
				setStatus(TaskStatus.ERROR);
				return;
			}
		}
		if (exportType.equals(MainActionCommands.EXPORT_PEAK_WIDTH_STATISTICS_COMMAND)) {
			try {
				writePeakWidthDataExportFile();
				setStatus(TaskStatus.FINISHED);
			} catch (Exception e) {
				e.printStackTrace();
				setStatus(TaskStatus.ERROR);
				return;
			}
		}
		if (exportType.equals(MainActionCommands.EXPORT_ALL_FEATURE_STATISTICS_COMMAND)) {
			try {
				writeMZRTPeakWidthDataExportFiles();
				setStatus(TaskStatus.FINISHED);
			} catch (Exception e) {
				e.printStackTrace();
				setStatus(TaskStatus.ERROR);
				return;
			}
		}		
		if (exportType.equals(MainActionCommands.EXPORT_FEATURE_STATISTICS_COMMAND)) {
			try {
				writeFeatureQCDataExportFile();
				setStatus(TaskStatus.FINISHED);
			} catch (Exception e) {
				e.printStackTrace();
				setStatus(TaskStatus.ERROR);
				return;
			}
		}
	}

	private void writeMZRTDataExportFile() throws Exception {

		taskDescription = "Reading feature data matrix ...";
		Matrix dataMatrix = 
				ExperimentUtils.readFeatureMatrix(currentExperiment, dataPipeline, false);
		if(dataMatrix == null) {
			errorMessage = "Unable to read feature data matrix file";
			setStatus(TaskStatus.ERROR);
			return;
		}
		taskDescription = "Writing M/Z & RT data export files ...";
		String parent = exportFile.getParentFile().getAbsolutePath();
		String baseName = FileNameUtils.getBaseName(exportFile.getName());
		
		File mzExportFile = Paths.get(parent, baseName + "_MZ_VALUES.txt").toFile();
		final Writer mzWriter = new BufferedWriter(new FileWriter(mzExportFile));
		File rtExportFile = Paths.get(parent, baseName + "_RT_VALUES.txt").toFile();
		final Writer rtWriter = new BufferedWriter(new FileWriter(rtExportFile));

		// Create header
		TreeMap<ExperimentalSample, TreeMap<DataPipeline, DataFile[]>>sampleFileMap
			= DataExportUtils.createSampleFileMapForDataPipeline(
					currentExperiment, experimentDesignSubset, dataPipeline, namingField);

		String[] columnList =
			DataExportUtils.createSampleColumnNameArrayForDataPipeline(
					sampleFileMap, namingField, dataPipeline);

		String[] header = new String[columnList.length + 9];
		int columnCount = 0;
		header[columnCount] = BinnerExportFields.FEATURE_NAME.getName();
		header[++columnCount] = BinnerExportFields.METABOLITE_NAME.getName();
		header[++columnCount] = BinnerExportFields.BINNER_NAME.getName();
		header[++columnCount] = BinnerExportFields.NEUTRAL_MASS.getName();
		header[++columnCount] = BinnerExportFields.BINNER_MZ.getName();
		header[++columnCount] = BinnerExportFields.RT_EXPECTED.getName();
		header[++columnCount] = BinnerExportFields.RT_OBSERVED.getName();
		header[++columnCount] = BinnerExportFields.MZ.getName();
		header[++columnCount] = BinnerExportFields.CHARGE.getName();

		HashMap<DataFile, Integer> fileColumnMap = 
				DataExportUtils.createFileColumnMap(sampleFileMap, columnCount);

		if(replaceSpecialCharacters) {
			for(String columnName : columnList)
				header[++columnCount] = columnName.replaceAll("\\p{Punct}+", "-");
		}
		else {
			for(String columnName : columnList)
				header[++columnCount] = columnName;
		}
		mzWriter.append(StringUtils.join(header, columnSeparator));
		mzWriter.append(lineSeparator);
		rtWriter.append(StringUtils.join(header, columnSeparator));
		rtWriter.append(lineSeparator);

		MsFeature[] featureList = msFeatureSet4export.stream().
				sorted(new MsFeatureComparator(SortProperty.RT)).toArray(size -> new MsFeature[size]);

		long[] coordinates = new long[2];
		total = featureList.length;
		processed = 0;

		for( MsFeature msf : featureList){

			String[] mzLine = new String[header.length];
			columnCount = 0;
			//	String compoundName = msf.getName();
			String compoundName = msf.getBicMetaboliteName();
			if(msf.isIdentified()){

				compoundName = msf.getPrimaryIdentity().getCompoundName();

				if(msf.getPrimaryIdentity().getMsRtLibraryMatch() != null) {

					AdductMatch tam = msf.getPrimaryIdentity().getMsRtLibraryMatch().getTopAdductMatch();
					if(tam != null)
						compoundName += " (" + tam.getLibraryMatch().getName() + ")";
				}
			}
			double binnerMass =
				MsUtils.calculateModifiedMz(msf.getNeutralMass(),
						AdductManager.getDefaultAdductForPolarity(msf.getPolarity()));
			if(msf.getSpectrum().getPrimaryAdduct() != null 
					&& Math.abs(msf.getSpectrum().getPrimaryAdduct().getCharge()) == 1
					&& msf.getSpectrum().getPrimaryAdduct().getOligomericState() > 1)
				binnerMass =msf.getMonoisotopicMz();
				
			mzLine[columnCount] = msf.getName();
			mzLine[++columnCount] = compoundName;
			mzLine[++columnCount] = DataPrefix.MS_LIBRARY_UNKNOWN_TARGET.getName() + 
									mzFormat.format(msf.getNeutralMass()) + "_" + 
									rtFormat.format(msf.getRetentionTime());
			mzLine[++columnCount] = mzFormat.format(msf.getNeutralMass());
			mzLine[++columnCount] = mzFormat.format(binnerMass);
			mzLine[++columnCount] = rtFormat.format(msf.getRetentionTime());
			mzLine[++columnCount] = rtFormat.format(msf.getStatsSummary().getMedianObservedRetention());
			mzLine[++columnCount] = mzFormat.format(msf.getMonoisotopicMz());
			mzLine[++columnCount] = Integer.toString(msf.getCharge());

			String[] rtLine = Arrays.copyOf(mzLine, mzLine.length);
			
			// Data
			coordinates[1] = dataMatrix.getColumnForLabel(msf);
			for (Entry<ExperimentalSample, TreeMap<DataPipeline, DataFile[]>> entry : sampleFileMap.entrySet()) {

				for(DataFile df : entry.getValue().get(dataPipeline)) {

					SimpleMsFeature value = null;
					coordinates[0] = dataMatrix.getRowForLabel(df);
					if(coordinates[0] >= 0) //	TODO find out why it happens - screwed up design cleanup?
						value = (SimpleMsFeature)dataMatrix.getAsObject(coordinates);
					else {
						System.out.println(df.getName());
					}
					String mzString = "";
					String rtString = "";
					if(value != null) {
						mzString = MRC2ToolBoxConfiguration.defaultMzFormat.format(
								value.getObservedSpectrum().getMonoisotopicMz());
						rtString = MRC2ToolBoxConfiguration.defaultRtFormat.format(
								value.getRetentionTime());
					}
					mzLine[fileColumnMap.get(df)] = mzString;
					rtLine[fileColumnMap.get(df)] = rtString;
				}
			}			
			mzWriter.append(StringUtils.join(mzLine, columnSeparator));
			mzWriter.append(lineSeparator);
			rtWriter.append(StringUtils.join(rtLine, columnSeparator));
			rtWriter.append(lineSeparator);
			processed++;
		}
		mzWriter.flush();
		mzWriter.close();			
		rtWriter.flush();
		rtWriter.close();	
	}
	
	private void writePeakWidthDataExportFile() throws Exception {
		
		taskDescription = "Reading feature data matrix ...";
		Matrix dataMatrix = 
				ExperimentUtils.readFeatureMatrix(currentExperiment, dataPipeline, false);
		if(dataMatrix == null) {
			errorMessage = "Unable to read feature data matrix file";
			setStatus(TaskStatus.ERROR);
			return;
		}
		taskDescription = "Writing Peak Width data export file ...";
		final Writer pwWriter = new BufferedWriter(new FileWriter(exportFile));

		// Create header
		TreeMap<ExperimentalSample, TreeMap<DataPipeline, DataFile[]>>sampleFileMap
			= DataExportUtils.createSampleFileMapForDataPipeline(
					currentExperiment, experimentDesignSubset, dataPipeline, namingField);

		String[] columnList =
			DataExportUtils.createSampleColumnNameArrayForDataPipeline(
					sampleFileMap, namingField, dataPipeline);

		String[] header = new String[columnList.length + 9];
		int columnCount = 0;
		header[columnCount] = BinnerExportFields.FEATURE_NAME.getName();
		header[++columnCount] = BinnerExportFields.METABOLITE_NAME.getName();
		header[++columnCount] = BinnerExportFields.BINNER_NAME.getName();
		header[++columnCount] = BinnerExportFields.NEUTRAL_MASS.getName();
		header[++columnCount] = BinnerExportFields.BINNER_MZ.getName();
		header[++columnCount] = BinnerExportFields.RT_EXPECTED.getName();
		header[++columnCount] = BinnerExportFields.RT_OBSERVED.getName();
		header[++columnCount] = BinnerExportFields.MZ.getName();
		header[++columnCount] = BinnerExportFields.CHARGE.getName();

		HashMap<DataFile, Integer> fileColumnMap = 
				DataExportUtils.createFileColumnMap(sampleFileMap, columnCount);

		if(replaceSpecialCharacters) {
			for(String columnName : columnList)
				header[++columnCount] = columnName.replaceAll("\\p{Punct}+", "-");
		}
		else {
			for(String columnName : columnList)
				header[++columnCount] = columnName;
		}
		pwWriter.append(StringUtils.join(header, columnSeparator));
		pwWriter.append(lineSeparator);

		MsFeature[] featureList = msFeatureSet4export.stream().
				sorted(new MsFeatureComparator(SortProperty.RT)).toArray(size -> new MsFeature[size]);

		long[] coordinates = new long[2];
		total = featureList.length;
		processed = 0;

		for( MsFeature msf : featureList){

			String[] mzLine = new String[header.length];
			columnCount = 0;
			//	String compoundName = msf.getName();
			String compoundName = msf.getBicMetaboliteName();
			if(msf.isIdentified()){

				compoundName = msf.getPrimaryIdentity().getCompoundName();

				if(msf.getPrimaryIdentity().getMsRtLibraryMatch() != null) {

					AdductMatch tam = msf.getPrimaryIdentity().getMsRtLibraryMatch().getTopAdductMatch();
					if(tam != null)
						compoundName += " (" + tam.getLibraryMatch().getName() + ")";
				}
			}
			double binnerMass =
				MsUtils.calculateModifiedMz(msf.getNeutralMass(),
						AdductManager.getDefaultAdductForPolarity(msf.getPolarity()));
			if(msf.getSpectrum().getPrimaryAdduct() != null 
					&& Math.abs(msf.getSpectrum().getPrimaryAdduct().getCharge()) == 1
					&& msf.getSpectrum().getPrimaryAdduct().getOligomericState() > 1)
				binnerMass =msf.getMonoisotopicMz();
				
			mzLine[columnCount] = msf.getName();
			mzLine[++columnCount] = compoundName;
			mzLine[++columnCount] = DataPrefix.MS_LIBRARY_UNKNOWN_TARGET.getName() + 
									mzFormat.format(msf.getNeutralMass()) + "_" + 
									rtFormat.format(msf.getRetentionTime());
			mzLine[++columnCount] = mzFormat.format(msf.getNeutralMass());
			mzLine[++columnCount] = mzFormat.format(binnerMass);
			mzLine[++columnCount] = rtFormat.format(msf.getRetentionTime());
			mzLine[++columnCount] = rtFormat.format(msf.getStatsSummary().getMedianObservedRetention());
			mzLine[++columnCount] = mzFormat.format(msf.getMonoisotopicMz());
			mzLine[++columnCount] = Integer.toString(msf.getCharge());

			String[] rtLine = Arrays.copyOf(mzLine, mzLine.length);
			
			// Data
			coordinates[1] = dataMatrix.getColumnForLabel(msf);
			for (Entry<ExperimentalSample, TreeMap<DataPipeline, DataFile[]>> entry : sampleFileMap.entrySet()) {

				for(DataFile df : entry.getValue().get(dataPipeline)) {

					SimpleMsFeature value = null;
					coordinates[0] = dataMatrix.getRowForLabel(df);
					if(coordinates[0] >= 0) //	TODO find out why it happens - screwed up design cleanup?
						value = (SimpleMsFeature)dataMatrix.getAsObject(coordinates);
					else {
						System.out.println(df.getName());
					}
					String pwString = "";
					if(value != null && value.getRtRange() != null) {
						pwString = MRC2ToolBoxConfiguration.defaultRtFormat.format(
								value.getRtRange().getSize());
					}
					rtLine[fileColumnMap.get(df)] = pwString;
				}
			}			
			pwWriter.append(StringUtils.join(rtLine, columnSeparator));
			pwWriter.append(lineSeparator);
			processed++;
		}		
		pwWriter.flush();
		pwWriter.close();
	}
	
	private void writeMZRTPeakWidthDataExportFiles() throws Exception {

		taskDescription = "Reading feature data matrix ...";
		Matrix dataMatrix = 
				ExperimentUtils.readFeatureMatrix(currentExperiment, dataPipeline, false);
		if(dataMatrix == null) {
			errorMessage = "Unable to read feature data matrix file";
			setStatus(TaskStatus.ERROR);
			return;
		}
		taskDescription = "Writing M/Z, RT & peak width data export files ...";
		String parent = exportFile.getParentFile().getAbsolutePath();
		String baseName = FileNameUtils.getBaseName(exportFile.getName());
		
		File mzExportFile = Paths.get(parent, baseName + "_MZ_VALUES.txt").toFile();
		final Writer mzWriter = new BufferedWriter(new FileWriter(mzExportFile));
		File rtExportFile = Paths.get(parent, baseName + "_RT_VALUES.txt").toFile();
		final Writer rtWriter = new BufferedWriter(new FileWriter(rtExportFile));
		File peakWidthExportFile = Paths.get(parent, baseName + "_PEAK_WIDTH_VALUES.txt").toFile();
		final Writer pwWriter = new BufferedWriter(new FileWriter(peakWidthExportFile));

		// Create header
		TreeMap<ExperimentalSample, TreeMap<DataPipeline, DataFile[]>>sampleFileMap
			= DataExportUtils.createSampleFileMapForDataPipeline(
					currentExperiment, experimentDesignSubset, dataPipeline, namingField);

		String[] columnList =
			DataExportUtils.createSampleColumnNameArrayForDataPipeline(
					sampleFileMap, namingField, dataPipeline);

		String[] header = new String[columnList.length + 9];
		int columnCount = 0;
		header[columnCount] = BinnerExportFields.FEATURE_NAME.getName();
		header[++columnCount] = BinnerExportFields.METABOLITE_NAME.getName();
		header[++columnCount] = BinnerExportFields.BINNER_NAME.getName();
		header[++columnCount] = BinnerExportFields.NEUTRAL_MASS.getName();
		header[++columnCount] = BinnerExportFields.BINNER_MZ.getName();
		header[++columnCount] = BinnerExportFields.RT_EXPECTED.getName();
		header[++columnCount] = BinnerExportFields.RT_OBSERVED.getName();
		header[++columnCount] = BinnerExportFields.MZ.getName();
		header[++columnCount] = BinnerExportFields.CHARGE.getName();

		HashMap<DataFile, Integer> fileColumnMap = 
				DataExportUtils.createFileColumnMap(sampleFileMap, columnCount);

		if(replaceSpecialCharacters) {
			for(String columnName : columnList)
				header[++columnCount] = columnName.replaceAll("\\p{Punct}+", "-");
		}
		else {
			for(String columnName : columnList)
				header[++columnCount] = columnName;
		}
		String headerString = StringUtils.join(header, columnSeparator) + lineSeparator;
		mzWriter.append(headerString);
		rtWriter.append(headerString);
		pwWriter.append(headerString);

		MsFeature[] featureList = msFeatureSet4export.stream().
				sorted(new MsFeatureComparator(SortProperty.RT)).toArray(size -> new MsFeature[size]);

		long[] coordinates = new long[2];
		total = featureList.length;
		processed = 0;

		for( MsFeature msf : featureList){

			String[] mzLine = new String[header.length];
			columnCount = 0;
			//	String compoundName = msf.getName();
			String compoundName = msf.getBicMetaboliteName();
			if(msf.isIdentified()){

				compoundName = msf.getPrimaryIdentity().getCompoundName();

				if(msf.getPrimaryIdentity().getMsRtLibraryMatch() != null) {

					AdductMatch tam = msf.getPrimaryIdentity().getMsRtLibraryMatch().getTopAdductMatch();
					if(tam != null)
						compoundName += " (" + tam.getLibraryMatch().getName() + ")";
				}
			}
			double binnerMass =
				MsUtils.calculateModifiedMz(msf.getNeutralMass(),
						AdductManager.getDefaultAdductForPolarity(msf.getPolarity()));
			if(msf.getSpectrum().getPrimaryAdduct() != null 
					&& Math.abs(msf.getSpectrum().getPrimaryAdduct().getCharge()) == 1
					&& msf.getSpectrum().getPrimaryAdduct().getOligomericState() > 1)
				binnerMass =msf.getMonoisotopicMz();
				
			mzLine[columnCount] = msf.getName();
			mzLine[++columnCount] = compoundName;
			mzLine[++columnCount] = DataPrefix.MS_LIBRARY_UNKNOWN_TARGET.getName() + 
									mzFormat.format(msf.getNeutralMass()) + "_" + 
									rtFormat.format(msf.getRetentionTime());
			mzLine[++columnCount] = mzFormat.format(msf.getNeutralMass());
			mzLine[++columnCount] = mzFormat.format(binnerMass);
			mzLine[++columnCount] = rtFormat.format(msf.getRetentionTime());
			mzLine[++columnCount] = rtFormat.format(msf.getStatsSummary().getMedianObservedRetention());
			mzLine[++columnCount] = mzFormat.format(msf.getMonoisotopicMz());
			mzLine[++columnCount] = Integer.toString(msf.getCharge());

			String[] rtLine = Arrays.copyOf(mzLine, mzLine.length);
			String[] pwLine = Arrays.copyOf(mzLine, mzLine.length);
			
			// Data
			coordinates[1] = dataMatrix.getColumnForLabel(msf);
			for (Entry<ExperimentalSample, TreeMap<DataPipeline, DataFile[]>> entry : sampleFileMap.entrySet()) {

				for(DataFile df : entry.getValue().get(dataPipeline)) {

					SimpleMsFeature value = null;
					coordinates[0] = dataMatrix.getRowForLabel(df);
					if(coordinates[0] >= 0) //	TODO find out why it happens - screwed up design cleanup?
						value = (SimpleMsFeature)dataMatrix.getAsObject(coordinates);
					else {
						System.out.println(df.getName());
					}
					String mzString = "";
					String rtString = "";
					String pwString = "";
					if(value != null) {
						mzString = MRC2ToolBoxConfiguration.defaultMzFormat.format(
								value.getObservedSpectrum().getMonoisotopicMz());
						rtString = MRC2ToolBoxConfiguration.defaultRtFormat.format(
								value.getRetentionTime());
						if(value.getRtRange() != null) {
							pwString = MRC2ToolBoxConfiguration.defaultRtFormat.format(
								value.getRtRange().getSize());
						}
					}
					mzLine[fileColumnMap.get(df)] = mzString;
					rtLine[fileColumnMap.get(df)] = rtString;
					pwLine[fileColumnMap.get(df)] = pwString;
				}
			}			
			mzWriter.append(StringUtils.join(mzLine, columnSeparator));
			mzWriter.append(lineSeparator);
			rtWriter.append(StringUtils.join(rtLine, columnSeparator));
			rtWriter.append(lineSeparator);
			pwWriter.append(StringUtils.join(pwLine, columnSeparator));
			pwWriter.append(lineSeparator);
			processed++;
		}
		mzWriter.flush();
		mzWriter.close();			
		rtWriter.flush();
		rtWriter.close();	
		pwWriter.flush();
		pwWriter.close();
	}

	private void writeManifestFile() {
		
		String manifestString = WorklistUtils.createManifest(currentExperiment, dataPipeline);
		File outputFile = Paths.get(exportFile.getParentFile().getAbsolutePath(), 
				FilenameUtils.getBaseName(exportFile.getName()) + "_MANIFEST.txt").toFile() ;		
		try {
			FileUtils.writeStringToFile(outputFile, manifestString, Charset.defaultCharset(), false);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void createTimeMap() {

		timeMap.clear();
		Worklist worklist = 
				currentExperiment.getWorklistForDataAcquisitionMethod(dataPipeline.getAcquisitionMethod());
		if(worklist == null)
			return;
			
		Date start =
			worklist.getTimeSortedWorklistItems().stream().
			findFirst().get().getTimeStamp();

		// Find differences
		for (WorklistItem item : worklist.getTimeSortedWorklistItems()) {

			long diff = item.getTimeStamp().getTime() - start.getTime();
			double inj = diff / 3600000.0d;
			String timestamp = "";
			try {
				timestamp = rtFormat.format(inj);
			} catch (Exception e) {
				e.printStackTrace();
			}
			timeMap.put(item.getDataFile(), timestamp);
		}		
	}

	private void collectFeaturesForExport() {
		
		if (enableFilters) {

			msFeatureSet4export =
				currentExperiment.getActiveFeatureSetForDataPipeline(dataPipeline).
				getFeatures().stream().
				filter(f -> f.getAveragePeakArea()> 0.0d).
				filter(f -> (f.getStatsSummary().getPooledFrequency() >= minPooledFrequency)).
				filter(f -> (f.getStatsSummary().getPooledRsd() <= maxPooledRsd)).
				sorted(new MsFeatureComparator(SortProperty.RT)).
				collect(Collectors.toList());
		}
		else {
			msFeatureSet4export =
					currentExperiment.getActiveFeatureSetForDataPipeline(dataPipeline).
					getFeatures().stream().
					filter(f -> f.getAveragePeakArea()> 0.0d).
					sorted(new MsFeatureComparator(SortProperty.RT)).
					collect(Collectors.toList());
		}
	}

	private void writeDuplicatesExportFile() throws Exception {

		taskDescription = "Writing duplicate features data export file ...";
		final Writer writer = new BufferedWriter(new FileWriter(exportFile));

		for (MsFeatureCluster fc : currentExperiment.getDuplicateClustersForDataPipeline(dataPipeline)) {

			for (MsFeature feature : fc.getFeatures()) {

				if (feature.equals(fc.getPrimaryFeature()))
					writer.append(feature.getName());
				else {
					writer.append(columnSeparator);
					writer.append(feature.getName());
				}
			}
			writer.append(lineSeparator);
		}
		writer.flush();
		writer.close();
	}

	private void writeMPPexportFile() throws Exception {

		taskDescription = "Writing data export file for MPP ...";
		final Writer writer = new BufferedWriter(new FileWriter(exportFile));
		final Matrix dataMatrix = currentExperiment.getDataMatrixForDataPipeline(dataPipeline);

		// Create header
		TreeMap<ExperimentalSample, TreeMap<DataPipeline, DataFile[]>>sampleFileMap =
				DataExportUtils.createSampleFileMapForDataPipeline(
						currentExperiment, experimentDesignSubset, dataPipeline, namingField);
		String[] columnList =
				DataExportUtils.createSampleColumnNameArrayForDataPipeline(
						sampleFileMap, namingField, dataPipeline);

		String[] header = new String[columnList.length + 6];
		int columnCount = 0;
		header[columnCount] = MPPExportFields.NAME.getName();
		header[++columnCount] = MPPExportFields.COMPOUND_NAME.getName();
		header[++columnCount] = MPPExportFields.MOL_FORMULA.getName();
		header[++columnCount] = MPPExportFields.CAS.getName();
		header[++columnCount] = MPPExportFields.MASS.getName();
		header[++columnCount] = MPPExportFields.RT.getName();

		HashMap<DataFile, Integer> fileColumnMap =
				DataExportUtils.createFileColumnMap(sampleFileMap, columnCount);

		HashMap<DataFile, Long>matrixFileMap = new HashMap<DataFile, Long>();
		fileColumnMap.keySet().stream().forEach(f -> matrixFileMap.put(f, dataMatrix.getRowForLabel(f)));

		if(replaceSpecialCharacters) {
			for(String columnName : columnList)
				header[++columnCount] = columnName.replaceAll("\\p{Punct}+", "-");
		}
		else {
			for(String columnName : columnList)
				header[++columnCount] = columnName;
		}
		writer.append(StringUtils.join(header, columnSeparator));
		writer.append(lineSeparator);
		long[] coordinates = new long[2];

		total = msFeatureSet4export.size();
		processed = 0;

		for( MsFeature msf : msFeatureSet4export){

			String[] line = new String[header.length];

			//	Annotations
			String formula = "";
			String casId = "";
			String compoundName = msf.getName();

			if(msf.isIdentified()){

				if(msf.getPrimaryIdentity().getCompoundIdentity().getFormula() != null)
					formula = msf.getPrimaryIdentity().getCompoundIdentity().getFormula();

				if(msf.getDatabaseId(CompoundDatabaseEnum.CAS) != null)
					casId = msf.getDatabaseId(CompoundDatabaseEnum.CAS);

				compoundName = msf.getPrimaryIdentity().getCompoundName();

				if(msf.getPrimaryIdentity().getMsRtLibraryMatch() != null) {

					AdductMatch tam = msf.getPrimaryIdentity().getMsRtLibraryMatch().getTopAdductMatch();
					if(tam != null)
						compoundName += " (" + tam.getLibraryMatch().getName() + ")";
				}
			}
			columnCount = 0;
			line[columnCount] = msf.getName();
			line[++columnCount] = compoundName;
			line[++columnCount] = formula;
			line[++columnCount] = casId;
			line[++columnCount] = mzFormat.format(msf.getNeutralMass());
			line[++columnCount] = rtFormat.format(msf.getRetentionTime());

			// Data
			coordinates[1] = dataMatrix.getColumnForLabel(msf);
			for (Entry<ExperimentalSample, TreeMap<DataPipeline, DataFile[]>> entry : sampleFileMap.entrySet()) {

				for(DataFile df : entry.getValue().get(dataPipeline)) {

					coordinates[0] = matrixFileMap.get(df);
					double value = Math.round(dataMatrix.getAsDouble(coordinates));
					String valueString = Double.toString(value).replaceAll("\n|\r|\\u000a|\\u000d|\t|\\u0009|\\u00ad", " ");

					if (value == 0.0d)
						valueString = exportMissingAs.equals(MissingExportType.AS_MISSING) ? "" : "0.0";

					line[fileColumnMap.get(df)] = valueString;
				}
			}
			processed++;
			writer.append(StringUtils.join(line, columnSeparator));
			writer.append(lineSeparator);
		}
		writer.flush();
		writer.close();
	}

	private void writeBinnerExportFile() throws Exception {

		taskDescription = "Writing data export file for Binner ...";
		final Writer writer = new BufferedWriter(new FileWriter(exportFile));
		final Matrix dataMatrix = currentExperiment.getDataMatrixForDataPipeline(dataPipeline);

		// Create header
		TreeMap<ExperimentalSample, TreeMap<DataPipeline, DataFile[]>>sampleFileMap
			= DataExportUtils.createSampleFileMapForDataPipeline(
					currentExperiment, experimentDesignSubset, dataPipeline, namingField);

		String[] columnList =
			DataExportUtils.createSampleColumnNameArrayForDataPipeline(
					sampleFileMap, namingField, dataPipeline);

		String[] header = new String[columnList.length + 9];
		int columnCount = 0;
		header[columnCount] = BinnerExportFields.FEATURE_NAME.getName();
		header[++columnCount] = BinnerExportFields.METABOLITE_NAME.getName();
		header[++columnCount] = BinnerExportFields.BINNER_NAME.getName();
		header[++columnCount] = BinnerExportFields.NEUTRAL_MASS.getName();
		header[++columnCount] = BinnerExportFields.BINNER_MZ.getName();
		header[++columnCount] = BinnerExportFields.RT_EXPECTED.getName();
		header[++columnCount] = BinnerExportFields.RT_OBSERVED.getName();
		header[++columnCount] = BinnerExportFields.MZ.getName();
		header[++columnCount] = BinnerExportFields.CHARGE.getName();

		HashMap<DataFile, Integer> fileColumnMap = 
				DataExportUtils.createFileColumnMap(sampleFileMap, columnCount);

		if(replaceSpecialCharacters) {
			for(String columnName : columnList)
				header[++columnCount] = columnName.replaceAll("\\p{Punct}+", "-");
		}
		else {
			for(String columnName : columnList)
				header[++columnCount] = columnName;
		}
		writer.append(StringUtils.join(header, columnSeparator));
		writer.append(lineSeparator);

		MsFeature[] featureList = msFeatureSet4export.stream().
				sorted(new MsFeatureComparator(SortProperty.RT)).toArray(size -> new MsFeature[size]);

		long[] coordinates = new long[2];
		total = featureList.length;
		processed = 0;

		for( MsFeature msf : featureList){

			String[] line = new String[header.length];
			columnCount = 0;
			//	String compoundName = msf.getName();
			String compoundName = msf.getBicMetaboliteName();
			if(msf.isIdentified()){

				compoundName = msf.getPrimaryIdentity().getCompoundName();

				if(msf.getPrimaryIdentity().getMsRtLibraryMatch() != null) {

					AdductMatch tam = msf.getPrimaryIdentity().getMsRtLibraryMatch().getTopAdductMatch();
					if(tam != null)
						compoundName += " (" + tam.getLibraryMatch().getName() + ")";
				}
			}
			double binnerMass =
				MsUtils.calculateModifiedMz(msf.getNeutralMass(),
						AdductManager.getDefaultAdductForPolarity(msf.getPolarity()));
			
			//	TODO ?? This is for oligomers?
			if(msf.getSpectrum().getPrimaryAdduct() != null 
					&& Math.abs(msf.getSpectrum().getPrimaryAdduct().getCharge()) == 1
					&& msf.getSpectrum().getPrimaryAdduct().getOligomericState() > 1)
				binnerMass = msf.getMonoisotopicMz();
				
			line[columnCount] = msf.getName();
			line[++columnCount] = compoundName;
			line[++columnCount] = DataPrefix.MS_LIBRARY_UNKNOWN_TARGET.getName() + 
									mzFormat.format(binnerMass) + "_" + 
									rtFormat.format(msf.getRetentionTime());
			line[++columnCount] = mzFormat.format(msf.getNeutralMass());
			line[++columnCount] = mzFormat.format(binnerMass);
			line[++columnCount] = rtFormat.format(msf.getRetentionTime());
			line[++columnCount] = rtFormat.format(msf.getStatsSummary().getMedianObservedRetention());
			line[++columnCount] = mzFormat.format(msf.getMonoisotopicMz());
			line[++columnCount] = Integer.toString(msf.getCharge());

			// Data
			coordinates[1] = dataMatrix.getColumnForLabel(msf);
//			if(coordinates[1] == -1)	//	TODO find out why it happens
//				continue;

			for (Entry<ExperimentalSample, TreeMap<DataPipeline, DataFile[]>> entry : sampleFileMap.entrySet()) {

				for(DataFile df : entry.getValue().get(dataPipeline)) {

					double value = 0.0d;
					coordinates[0] = dataMatrix.getRowForLabel(df);
					if(coordinates[0] >= 0) //	TODO find out why it happens - screwed up design cleanup?
						value = Math.round(dataMatrix.getAsDouble(coordinates));
					else {
						System.out.println(df.getName());
					}
					String valueString = Double.toString(value).replaceAll("\n|\r|\\u000a|\\u000d|\t|\\u0009|\\u00ad", " ");

					if (value == 0.0d) {

						valueString = "0.0";
						if (exportMissingAs.equals(MissingExportType.AS_MISSING))
							valueString = "";
					}
					line[fileColumnMap.get(df)] = valueString;
				}
			}
			processed++;
			writer.append(StringUtils.join(line, columnSeparator));
			writer.append(lineSeparator);
		}
		writer.flush();
		writer.close();
	}
	
	private void writeFeatureQCDataExportFile() {
		
		taskDescription = "Writing data export file for feature statistics ...";
		List<MsFeature> featureList = msFeatureSet4export.stream().
				sorted(new MsFeatureComparator(SortProperty.RT)).collect(Collectors.toList());
		total = featureList.size();
		processed = 0;
				
		Collection<String>dataToExport = new ArrayList<String>();
		List<String>lineChunks = new ArrayList<String>();
		//	lineChunks.add("FeatureID");
		lineChunks.add("metabolite_name");
		lineChunks.add("Name");
		for(MSFeatureSetStatisticalParameters o : MSFeatureSetStatisticalParameters.values())
			lineChunks.add(o.getName());
		
		dataToExport.add(StringUtils.join(lineChunks, columnSeparator));
		
		for(MsFeature feature :  featureList) {
			
			lineChunks.clear();
			// lineChunks.add(feature.getId());
			lineChunks.add(feature.getBicMetaboliteName());
			lineChunks.add(feature.getName());
			
			for(MSFeatureSetStatisticalParameters o : MSFeatureSetStatisticalParameters.values()) {
				
				String valueString = 
						formatQCValue(feature.getStatsSummary().getValueOfType(o), o);
				lineChunks.add(valueString);
			}
			dataToExport.add(StringUtils.join(lineChunks, columnSeparator));
			processed++;
		}
		try {
			Files.write(exportFile.toPath(), 
					dataToExport, 
					StandardCharsets.UTF_8,
					StandardOpenOption.CREATE, 
					StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private String formatQCValue(Double value, MSFeatureSetStatisticalParameters field) {
		
		if(value == null || Double.isNaN(value))
			return "";
		
		NumberFormat pcRSDFormat = new DecimalFormat("###.###");
		
		switch (field) {

		case TOTAL_MEDIAN:
			return peakAreaFormat.format(value);
			
		case SAMPLE_MEDIAN:
			return peakAreaFormat.format(value);
			
		case POOLED_MEDIAN:
			return peakAreaFormat.format(value);
			
		case PERCENT_MISSING_IN_SAMPLES:
			return pcRSDFormat.format(value);
			
		case PERCENT_MISSING_IN_POOLS:
			return pcRSDFormat.format(value);
			
		case AREA_RSD_SAMPLES:
			return pcRSDFormat.format(value * 100.0);
			
		case AREA_RSD_POOLS:
			return pcRSDFormat.format(value * 100.0);
			
		case RT_RSD:
			return pcRSDFormat.format(value * 100.0);
			
		case MZ_RSD:
			return pcRSDFormat.format(value * 100.0);

		default:
			break;
		}
		return "";
	}
	
	private void writeMetabolomicsWorkbenchExportFile() throws Exception {

		taskDescription = "Writing data export file for Metabolomics Workbench ...";
		final Writer writer = new BufferedWriter(new FileWriter(exportFile));
		final Matrix dataMatrix = currentExperiment.getDataMatrixForDataPipeline(dataPipeline);

		// Create header
		TreeMap<ExperimentalSample, TreeMap<DataPipeline, DataFile[]>>sampleFileMap
			= DataExportUtils.createSampleFileMapForDataPipeline(
					currentExperiment, experimentDesignSubset, dataPipeline, namingField);

		String[] columnList =
			DataExportUtils.createSampleColumnNameArrayForDataPipeline(
					sampleFileMap, namingField, dataPipeline);

		String[] header = new String[columnList.length + 1];
		int columnCount = 0;
		header[columnCount] = "feature_name";
		HashMap<DataFile, Integer> fileColumnMap = 
				DataExportUtils.createFileColumnMap(sampleFileMap, columnCount);

		for(String columnName : columnList)
			header[++columnCount] = columnName;

		writer.append(StringUtils.join(header, columnSeparator));
		writer.append(lineSeparator);

		MsFeature[] featureList = msFeatureSet4export.stream().
				sorted(new MsFeatureComparator(SortProperty.RT)).toArray(size -> new MsFeature[size]);

		long[] coordinates = new long[2];
		total = featureList.length;
		processed = 0;

		for( MsFeature msf : featureList){

			String[] line = new String[header.length];
			String featureName = MRC2ToolBoxConfiguration.getMzFormat().format(msf.getMonoisotopicMz()) + 
					"_" + MRC2ToolBoxConfiguration.getRtFormat().format(msf.getRetentionTime());		
			line[0] = featureName;

			// Data
			coordinates[1] = dataMatrix.getColumnForLabel(msf);
			for (Entry<ExperimentalSample, TreeMap<DataPipeline, DataFile[]>> entry : sampleFileMap.entrySet()) {

				for(DataFile df : entry.getValue().get(dataPipeline)) {

					double value = 0.0d;
					coordinates[0] = dataMatrix.getRowForLabel(df);
					if(coordinates[0] >= 0) //	TODO find out why it happens - screwed up design cleanup?
						value = Math.round(dataMatrix.getAsDouble(coordinates));
					else {
						System.out.println(df.getName());
					}
					String valueString = "";
					if (value == 0.0d && exportMissingAs.equals(MissingExportType.AS_MISSING)) {
						valueString = "";
					}
					else {
						//	TODO This may throw some exceptions if clean string is not a proper double
						String cleanValueString = Double.toString(value).replaceAll("\n|\r|\\u000a|\\u000d|\t|\\u0009|\\u00ad", " ");
						valueString = peakAreaFormat.format(Double.valueOf(cleanValueString));
					}
					line[fileColumnMap.get(df)] = valueString;
				}
			}
			processed++;
			writer.append(StringUtils.join(line, columnSeparator));
			writer.append(lineSeparator);
		}
		writer.flush();
		writer.close();
	}

	//	TODO - add Batch export for multibatch experiments
	private void writeRexportFile() throws Exception {

		taskDescription = "Writing data export file for R ...";
		final Writer writer = new BufferedWriter(new FileWriter(exportFile));
		final Matrix dataMatrix = 
				currentExperiment.getDataMatrixForDataPipeline(dataPipeline);
		TreeSet<ExperimentDesignFactor>activeFactors =
				experimentDesignSubset.getDesignMap().stream().
				filter(l-> Objects.nonNull(l.getParentFactor())).
				map(l->l.getParentFactor()).
				collect(Collectors.toCollection(TreeSet::new));

		HashMap<MsFeature, Long>matrixFeatureMap = new HashMap<MsFeature, Long>();
		msFeatureSet4export.stream().forEach(
				f -> matrixFeatureMap.put(f, dataMatrix.getColumnForLabel(f)));

		//	Create header
		//	TODO handle variable column # through Collection? 
		String[] header = new String[msFeatureSet4export.size() + activeFactors.size() + 3];

		boolean writeTime = false;

		if (currentExperiment.acquisitionMethodHasLinkedWorklist(dataPipeline.getAcquisitionMethod())) {

			writeTime = true;
			createTimeMap();
			header = new String[msFeatureSet4export.size() + activeFactors.size() + 4];
		}
		if(currentExperiment.isDataAcquisitionMultiBatch(dataPipeline.getAcquisitionMethod()))
			header = new String[msFeatureSet4export.size() + activeFactors.size() + 5];

		int columnCount = 0;
		header[columnCount] = ExperimentDesignFields.DATA_FILE.getName();
		header[++columnCount] = ExperimentDesignFields.SAMPLE_NAME.getName();
		header[++columnCount] = ExperimentDesignFields.SAMPLE_ID.getName();

		if(currentExperiment.isDataAcquisitionMultiBatch(dataPipeline.getAcquisitionMethod()))
			header[++columnCount] = ExperimentDesignFields.BATCH.getName();

		if(writeTime)
			header[++columnCount] =DataExportFields.START_TIME.getName();

		for (ExperimentDesignFactor factor : activeFactors)
			header[++columnCount] = factor.getName();

		for(MsFeature f : msFeatureSet4export)
			header[++columnCount] = f.getName();

		writer.append(StringUtils.join(header, columnSeparator));
		writer.append(lineSeparator);

		//	Write data
		long[] coordinates = new long[2];
		total = activeSamples.size();
		processed = 0;

		for(ExperimentalSample sample : activeSamples) {

			if(sample.getDataFilesForMethod(dataPipeline.getAcquisitionMethod()) != null) {

				for(DataFile f : sample.getDataFilesForMethod(dataPipeline.getAcquisitionMethod())) {

					if(f.isEnabled()) {

						String[] line = new String[header.length];
						columnCount = 0;

						//	Design
						line[columnCount] = f.getName();
						line[++columnCount] = sample.getName();
						line[++columnCount] = sample.getId();

						if(currentExperiment.isDataAcquisitionMultiBatch(dataPipeline.getAcquisitionMethod()))
							line[++columnCount] = Integer.toString(f.getBatchNumber());

						if(writeTime) {

							String timestamp = "";
							if(timeMap.get(f) != null)
								timestamp = timeMap.get(f);

							line[++columnCount] = timestamp;
						}
						for (ExperimentDesignFactor factor : activeFactors)
							line[++columnCount] = sample.getLevel(factor).getName();

						//	Data
						coordinates[0] = dataMatrix.getRowForLabel(f);
						for(MsFeature msf : msFeatureSet4export) {

							coordinates[1] = matrixFeatureMap.get(msf);
							double value = Math.round(dataMatrix.getAsDouble(coordinates));
							String valueString = Double.toString(value).replaceAll("\n|\r|\\u000a|\\u000d|\t|\\u0009|\\u00ad", " ");

							if (value == 0.0d)
								valueString = exportMissingAs.equals(MissingExportType.AS_MISSING) ? "" : "0.0";

							line[++columnCount] = valueString;
						}
						writer.append(StringUtils.join(line, columnSeparator));
						writer.append(lineSeparator);
					}
				}
			}
			processed++;
		}
		writer.flush();
		writer.close();
	}
	
	@Override
	public Task cloneTask() {

		return new DataExportTask(
				currentExperiment,
				dataPipeline,
				exportFile,
				exportType,
				exportMissingAs,
				enableFilters,
				maxPooledRsd,
				minPooledFrequency,
				namingField,
				exportManifest,
				replaceSpecialCharacters);
	}

	public Collection<MsFeature> getMsFeatureSet4export() {
		return msFeatureSet4export;
	}

	public void setMsFeatureSet4export(Collection<MsFeature> msFeatureSet4export) {
		this.msFeatureSet4export = msFeatureSet4export;
	}
}

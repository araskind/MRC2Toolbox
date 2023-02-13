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
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFCreationHelper;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.ujmp.core.Matrix;

import edu.umich.med.mrc2.datoolbox.data.AdductMatch;
import edu.umich.med.mrc2.datoolbox.data.Assay;
import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignFactor;
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignLevel;
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignSubset;
import edu.umich.med.mrc2.datoolbox.data.ExperimentalSample;
import edu.umich.med.mrc2.datoolbox.data.MassSpectrum;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureSet;
import edu.umich.med.mrc2.datoolbox.data.MsRtLibraryMatch;
import edu.umich.med.mrc2.datoolbox.data.compare.MsFeatureComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundAnnotationField;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundIdentificationConfidence;
import edu.umich.med.mrc2.datoolbox.data.enums.DataExportFields;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.utils.DataExportUtils;

public class IntegratedExcelReportExportTask  extends AbstractTask {

	private Map<DataPipeline, MsFeatureSet> assayFeatureMap;
	private File exportFile;
	private ExperimentDesignSubset experimentDesignSubset;
	private MsFeatureSet integratedSet;
	private DataExportFields exportFieldNaming;
	private XSSFWorkbook workbook;
	private DataAnalysisProject currentExperiment;
	private XSSFCellStyle rtStyle, mzStyle, ppmStyle, 
		dataFileStyle, annotationStyle, hlinkStyle, headerStyle, percentStyle;
	private TreeSet<ExperimentalSample>activeSamples;
	private TreeSet<ExperimentDesignFactor>activeFactors;
	private XSSFCreationHelper createHelper;

	private enum FeatureSubset{

		ALL_FEATURES,
		ALL_FEATURES_NO_QC,
		NAMED_ONLY,
		NAMED_ONLY_NO_QC,
		UNKNOWNS_ONLY,
		UNKNOWNS_ONLY_NO_QC,
		QC_ONLY;
	}

	public IntegratedExcelReportExportTask(
			File exportFile,
			ExperimentDesignSubset design,
			Map<DataPipeline, MsFeatureSet>featureMap,
			MsFeatureSet integratedSet,
			DataExportFields exportFieldNaming){

		this.exportFile = exportFile;
		this.experimentDesignSubset = design;
		this.assayFeatureMap = featureMap;
		this.integratedSet = integratedSet;
		this.exportFieldNaming = exportFieldNaming;
		this.currentExperiment = 
				MRC2ToolBoxCore.getActiveMetabolomicsExperiment();

		activeSamples =
			currentExperiment.getExperimentDesign().
				getActiveSamplesForDesignSubset(experimentDesignSubset);

		activeFactors = new TreeSet<ExperimentDesignFactor>();
		for(ExperimentDesignLevel level : experimentDesignSubset.getDesignMap())
			activeFactors.add(level.getParentFactor());
	}

	@Override
	public Task cloneTask() {

		return new IntegratedExcelReportExportTask(
				exportFile,
				experimentDesignSubset,
				assayFeatureMap,
				integratedSet,
				exportFieldNaming);
	}

	@Override
	public void run() {

		setStatus(TaskStatus.PROCESSING);
		//	Create Excel file
		workbook = new XSSFWorkbook();
		createHelper = workbook.getCreationHelper();

		rtStyle = workbook.createCellStyle();
		rtStyle.setDataFormat(workbook.createDataFormat().getFormat("0.000"));

		mzStyle = workbook.createCellStyle();
		mzStyle.setDataFormat(workbook.createDataFormat().getFormat("0.0000"));

		ppmStyle = workbook.createCellStyle();
		ppmStyle.setDataFormat(workbook.createDataFormat().getFormat("0.0"));

		percentStyle = workbook.createCellStyle();
		percentStyle.setDataFormat(workbook.createDataFormat().getFormat("0.00%"));

		Font boldFont= workbook.createFont();
		boldFont.setBold(true);

		dataFileStyle = workbook.createCellStyle();
		dataFileStyle.setWrapText(true);
		dataFileStyle.setRotation((short) 90);
		dataFileStyle.setVerticalAlignment(VerticalAlignment.BOTTOM);
		dataFileStyle.setAlignment(HorizontalAlignment.CENTER);
		dataFileStyle.setFont(boldFont);

		headerStyle = workbook.createCellStyle();
		headerStyle.setFont(boldFont);

		annotationStyle = workbook.createCellStyle();
		annotationStyle.setVerticalAlignment(VerticalAlignment.CENTER);
		annotationStyle.setAlignment(HorizontalAlignment.LEFT);
		annotationStyle.setWrapText(true);
		annotationStyle.setFont(boldFont);

		hlinkStyle = workbook.createCellStyle();
        Font hlink_font = workbook.createFont();
        hlink_font.setUnderline(Font.U_SINGLE);
        hlink_font.setColor(IndexedColors.BLUE.getIndex());
        hlinkStyle.setFont(hlink_font);

		for (Entry<DataPipeline, MsFeatureSet> entry : assayFeatureMap.entrySet()) {

			DataPipeline pipeline = entry.getKey();

			// Write design
			writeDesignForMethod(pipeline);

			//	Write annotations
			writeFeatureAnnotationsForMethod(pipeline);

			//	Write QC data
			//	writeQCDataForMethod(method);
			
			writeFeatureQuantDataForMethod(pipeline, FeatureSubset.QC_ONLY);

			//	Write all data
			writeFeatureQuantDataForMethod(pipeline, FeatureSubset.ALL_FEATURES_NO_QC);

			//	Write data for identified features only
			writeFeatureQuantDataForMethod(pipeline, FeatureSubset.NAMED_ONLY_NO_QC);
		}
		//	Write integrated report
		if(integratedSet != null) {

			if(!integratedSet.getFeatures().isEmpty())
				writeIntegratedReportData();
		}
		//	Write compound ID confidence key
		writeCompoundIdConfidenceKey();

		try {
			FileOutputStream outputStream = new FileOutputStream(exportFile.getCanonicalPath());
			workbook.write(outputStream);
			outputStream.close();
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		setStatus(TaskStatus.FINISHED);
	}

	private void writeCompoundIdConfidenceKey() {

		XSSFSheet sheet = workbook.createSheet("ID confidence key");

		//		Create header row
		Row row = sheet.createRow(0);
		Cell cell = row.createCell(0);
		cell.setCellValue("Confidence level");
		cell.setCellStyle(annotationStyle);
		Cell idcell = row.createCell(1);
		idcell.setCellValue("Level name");
		idcell.setCellStyle(annotationStyle);
		Cell nmcell = row.createCell(2);
		nmcell.setCellValue("Level description");
		nmcell.setCellStyle(annotationStyle);

		int rowCount = 0;
		for(CompoundIdentificationConfidence idc : CompoundIdentificationConfidence.values()) {

			Row levelRow = sheet.createRow(++rowCount);
			Cell newCell = levelRow.createCell(0);
			newCell.setCellValue(idc.getLevelId());
			Cell idCell = levelRow.createCell(1);
			idCell.setCellValue(idc.getName());
			Cell nmCell = levelRow.createCell(2);
			nmCell.setCellValue(idc.getDescription());
		}
		for(int i=0; i<=3; i++)
			sheet.autoSizeColumn(i);
	}

	private void writeDesignForMethod(DataPipeline pipeline) {

		taskDescription = "Writing experiment design for " + pipeline.getName();
		XSSFSheet sheet = workbook.createSheet(pipeline.getName() + " DESIGN");

		//		Create header row
		Row row = sheet.createRow(0);

		Cell cell = row.createCell(0);
		cell.setCellValue(DataExportFields.DATA_FILE.getName());
		cell.setCellStyle(annotationStyle);
		Cell idcell = row.createCell(1);
		idcell.setCellValue(DataExportFields.SAMPLE_ID.getName());
		idcell.setCellStyle(annotationStyle);
		Cell nmcell = row.createCell(2);
		nmcell.setCellValue(DataExportFields.SAMPLE_EXPORT_NAME.getName());
		nmcell.setCellStyle(annotationStyle);

		int columnCount = 2;
		int rowCount = 0;

		for(ExperimentDesignFactor f : activeFactors){

			Cell newCell = row.createCell(++columnCount);
			newCell.setCellValue(f.getName());
			newCell.setCellStyle(annotationStyle);
		}
		for(ExperimentalSample s : activeSamples){

			if(s.getDataFilesForMethod(pipeline.getAcquisitionMethod()) != null) {

				for(DataFile df : s.getDataFilesForMethod(pipeline.getAcquisitionMethod())){

					if(df.isEnabled()){	//	Write out only enabled files

						Row sampleRow = sheet.createRow(++rowCount);
						Cell newCell = sampleRow.createCell(0);
						newCell.setCellValue(df.getName());
						Cell idCell = sampleRow.createCell(1);
						idCell.setCellValue(s.getId());
						Cell nmCell = sampleRow.createCell(2);
						nmCell.setCellValue(s.getName());
						columnCount = 2;
						for(ExperimentDesignFactor f : activeFactors){

							Cell fCell = sampleRow.createCell(++columnCount);
							fCell.setCellValue(s.getLevel(f).getName());
						}
					}
				}
			}
		}
		for(int i=0; i<=columnCount; i++)
			sheet.autoSizeColumn(i);

		sheet.createFreezePane(1, 1);
	}

	private void writeFeatureAnnotationsForMethod(DataPipeline pipeline) {

		taskDescription = "Writing MS feature annotations for " + pipeline.getName();
		XSSFSheet sheet = workbook.createSheet(pipeline.getName() + " ANNOTATIONS");

		total = assayFeatureMap.get(pipeline).getFeatures().size();
		processed = 0;
		int annotationColumnCount = 0;

		//	Create header row
		Row row = sheet.createRow(0);

		Cell featureNameCell = row.createCell(annotationColumnCount);
		featureNameCell.setCellValue(CompoundAnnotationField.FEATURE_NAME.getName());
		featureNameCell.setCellStyle(annotationStyle);
		annotationColumnCount++;

		Cell adductCell = row.createCell(annotationColumnCount);
		adductCell.setCellValue(CompoundAnnotationField.ADDUCT_TYPE.getName());
		adductCell.setCellStyle(annotationStyle);
		annotationColumnCount++;

		Cell libEntryNameCell = row.createCell(annotationColumnCount);
		libEntryNameCell.setCellValue(CompoundAnnotationField.COMPOUND_ID_NAME.getName());
		libEntryNameCell.setCellStyle(annotationStyle);
		annotationColumnCount++;

		Cell compoundNameCell = row.createCell(annotationColumnCount);
		compoundNameCell.setCellValue(CompoundAnnotationField.COMPOUND_NAME.getName());
		compoundNameCell.setCellStyle(annotationStyle);
		annotationColumnCount++;

		Cell idSourceCell = row.createCell(annotationColumnCount);
		idSourceCell.setCellValue(CompoundAnnotationField.ID_SOURCE.getName());
		idSourceCell.setCellStyle(annotationStyle);
		annotationColumnCount++;

		Cell idConfidenceCell = row.createCell(annotationColumnCount);
		idConfidenceCell.setCellValue(CompoundAnnotationField.ID_CONFIDENCE.getName());
		idConfidenceCell.setCellStyle(annotationStyle);
		annotationColumnCount++;

		Cell dbCell = row.createCell(annotationColumnCount);
		dbCell.setCellValue(CompoundAnnotationField.SOURCE_DB.getName());
		dbCell.setCellStyle(annotationStyle);
		annotationColumnCount++;

		Cell dbIdCell = row.createCell(annotationColumnCount);
		dbIdCell.setCellValue(CompoundAnnotationField.DB_ID.getName());
		dbIdCell.setCellStyle(annotationStyle);
		annotationColumnCount++;

		Cell formulaCell = row.createCell(annotationColumnCount);
		formulaCell.setCellValue(CompoundAnnotationField.FORMULA.getName());
		formulaCell.setCellStyle(annotationStyle);
		annotationColumnCount++;

		Cell massCell = row.createCell(annotationColumnCount);
		massCell.setCellValue(CompoundAnnotationField.MONOISOTOPIC_NEUTRAL_MASS.getName());
		massCell.setCellStyle(annotationStyle);
		annotationColumnCount++;

		Cell mzCell = row.createCell(annotationColumnCount);
		mzCell.setCellValue(CompoundAnnotationField.OBSERVED_MZ.getName());
		mzCell.setCellStyle(annotationStyle);
		annotationColumnCount++;

		Cell mzErrorCell = row.createCell(annotationColumnCount);
		mzErrorCell.setCellValue(CompoundAnnotationField.MZ_ERROR_PPM.getName());
		mzErrorCell.setCellStyle(annotationStyle);
		annotationColumnCount++;

		Cell rtObservedCell = row.createCell(annotationColumnCount);
		rtObservedCell.setCellValue(CompoundAnnotationField.RT_OBSERVED.getName());
		rtObservedCell.setCellStyle(annotationStyle);
		annotationColumnCount++;

		Cell rtDeltaCell = row.createCell(annotationColumnCount);
		rtDeltaCell.setCellValue(CompoundAnnotationField.RT_DELTA.getName());
		rtDeltaCell.setCellStyle(annotationStyle);
		annotationColumnCount++;

		Cell inchiCell = row.createCell(annotationColumnCount);
		inchiCell.setCellValue(CompoundAnnotationField.INCHI_KEY.getName());
		inchiCell.setCellStyle(annotationStyle);

		//	Iterate through features
		int rowCount = 0;
		MsFeature[] fArray = createFeatureList(pipeline, FeatureSubset.ALL_FEATURES);

		for( MsFeature f : fArray){

			String idName = "";
			String cpdName = "";
			String idSource = "";
			String idConfidence = "";
			String sourceDatabase = "";
			String databaseId = "";
			String formula = "";
			double mzError = 0.0d;
			double deltaRt = 0.0d;
			String inchiKey = "";
			Hyperlink link = null;

			annotationColumnCount = 0;

			if(f.isIdentified()){

				if(f.getPrimaryIdentity().getIdentityName() != null)
					idName = f.getPrimaryIdentity().getIdentityName();

				if(f.getPrimaryIdentity().getName() != null) {
					cpdName = f.getPrimaryIdentity().getName();
					idConfidence = f.getPrimaryIdentity().getConfidenceLevel().getLevelId();
				}
				if(f.getPrimaryIdentity().getCompoundIdentity().getFormula() != null)
					formula = f.getPrimaryIdentity().getCompoundIdentity().getFormula();

				if(f.getPrimaryIdentity().getPrimaryDatabase() != null){

					sourceDatabase = f.getPrimaryIdentity().getPrimaryDatabase().getName();
					databaseId = f.getPrimaryIdentity().getCompoundIdentity().getDbId(f.getPrimaryIdentity().getPrimaryDatabase());
		            link = createHelper.createHyperlink(HyperlinkType.URL);
		            link.setAddress(f.getPrimaryIdentity().getPrimaryLinkAddress());
				}
				if(f.getPrimaryIdentity().getCompoundIdentity().getInChiKey() != null)
					inchiKey = f.getPrimaryIdentity().getCompoundIdentity().getInChiKey();

				idSource = f.getPrimaryIdentity().getIdSource().getName();

				//	Get mass and RT errors for library entry
				if(f.getPrimaryIdentity().getMsRtLibraryMatch() != null) {

					deltaRt = f.getRetentionTime() - f.getPrimaryIdentity().getMsRtLibraryMatch().getExpectedRetention();
					mzError = getLibraryMzError(f);
				}
				else {
					//	TODO Get mass error got database ID - once database search is implemented
				}
			}
			Row annotationRow = sheet.createRow(++rowCount);
			annotationRow.createCell(annotationColumnCount++).setCellValue(f.getName());
			annotationRow.createCell(annotationColumnCount++).setCellValue(getAdductName(f));
			annotationRow.createCell(annotationColumnCount++).setCellValue(idName);
			annotationRow.createCell(annotationColumnCount++).setCellValue(cpdName);
			annotationRow.createCell(annotationColumnCount++).setCellValue(idSource);
			annotationRow.createCell(annotationColumnCount++).setCellValue(idConfidence);
			annotationRow.createCell(annotationColumnCount++).setCellValue(sourceDatabase);

			//	DB link
			Cell dbLinkCell = annotationRow.createCell(annotationColumnCount++);
			dbLinkCell.setCellValue(databaseId);
			if(link != null){

				dbLinkCell.setHyperlink(link);
				dbLinkCell.setCellStyle(hlinkStyle);
			}
			annotationRow.createCell(annotationColumnCount++).setCellValue(formula);

			Cell nmCell = annotationRow.createCell(annotationColumnCount++);
			nmCell.setCellValue(f.getNeutralMass());
			nmCell.setCellStyle(mzStyle);

			Cell monoMzCell = annotationRow.createCell(annotationColumnCount++);
			monoMzCell.setCellValue(f.getMonoisotopicMz());
			monoMzCell.setCellStyle(mzStyle);

			Cell mzErrorDataCell = annotationRow.createCell(annotationColumnCount++);
			if(mzError != 0.0d) {
				mzErrorDataCell.setCellValue(mzError);
				mzErrorDataCell.setCellStyle(ppmStyle);
			}
			Cell retCell = annotationRow.createCell(annotationColumnCount++);
			retCell.setCellValue(f.getRetentionTime());
			retCell.setCellStyle(rtStyle);

			Cell rtDeltaDataCell = annotationRow.createCell(annotationColumnCount++);
			if(deltaRt != 0.0d) {
				rtDeltaDataCell.setCellValue(deltaRt);
				rtDeltaDataCell.setCellStyle(rtStyle);
			}
			annotationRow.createCell(annotationColumnCount++).setCellValue(inchiKey);
			processed++;
		}
		taskDescription = "Auto-adjusting columns";
		total = annotationColumnCount+1;
		processed = 0;
		for(int i=0; i<=annotationColumnCount; i++) {

			sheet.autoSizeColumn(i);
			processed++;
		}
		sheet.createFreezePane(1, 1);
	}

	private Double getLibraryMzError(MsFeature f) {

		if(f.getSpectrum() == null)
			return null;

		if(f.getPrimaryIdentity().getMsRtLibraryMatch() != null) {

			AdductMatch topAdductMatch = f.getPrimaryIdentity().getMsRtLibraryMatch().getTopAdductMatch();
			if(topAdductMatch == null)
				return null;

			MassSpectrum libSpectrum = f.getPrimaryIdentity().getMsRtLibraryMatch().getLibrarySpectrum();
			if(libSpectrum == null)
				return null;

			double observedMz = f.getSpectrum().getMsForAdduct(topAdductMatch.getUnknownMatch())[0].getMz();
			double expectedMz = libSpectrum.getMsForAdduct(topAdductMatch.getLibraryMatch())[0].getMz();
			return (observedMz - expectedMz) / expectedMz * 1000000.0d;
		}
		return null;
	}

	private MsFeature[] createFeatureList(DataPipeline pipeline, FeatureSubset namedOnlyNoQc) {

		taskDescription = "Selecting features to export for " + pipeline.getName();
		Collection<MsFeature> methodFeatures = assayFeatureMap.get(pipeline).getFeatures();
		total = methodFeatures.size();
		processed = 0;

		HashSet<MsFeature>featuresToExport = new HashSet<MsFeature>();

		for( MsFeature f : assayFeatureMap.get(pipeline).getFeatures()){

			boolean add = false;

			if(namedOnlyNoQc.equals(FeatureSubset.ALL_FEATURES))
				add = true;

			if(namedOnlyNoQc.equals(FeatureSubset.ALL_FEATURES_NO_QC) && !f.isQcStandard())
				add = true;

			if(namedOnlyNoQc.equals(FeatureSubset.NAMED_ONLY) && f.isIdentified())
				add = true;

			if(namedOnlyNoQc.equals(FeatureSubset.NAMED_ONLY_NO_QC) && f.isIdentified() && !f.isQcStandard())
				add = true;

			if(namedOnlyNoQc.equals(FeatureSubset.UNKNOWNS_ONLY) && !f.isIdentified())
				add = true;

			if(namedOnlyNoQc.equals(FeatureSubset.UNKNOWNS_ONLY_NO_QC) && !f.isIdentified() && !f.isQcStandard())
				add = true;

			if(namedOnlyNoQc.equals(FeatureSubset.QC_ONLY) && f.isQcStandard())
				add = true;

			if(add)
				featuresToExport.add(f);

			processed++;
		}
		return featuresToExport.stream().
				sorted(new MsFeatureComparator(SortProperty.pimaryId)).
				toArray(size -> new MsFeature[size]);
	}

	private void writeFeatureQuantDataForMethod(DataPipeline pipeline, FeatureSubset subsetType) {

		MsFeature[] exportArray = createFeatureList(pipeline, subsetType);
		if(exportArray.length == 0)
			return;

		XSSFSheet sheet = null;
		if(subsetType.equals(FeatureSubset.NAMED_ONLY) || subsetType.equals(FeatureSubset.NAMED_ONLY_NO_QC)) {

			taskDescription = "Writing identified MS feature quant data for " + pipeline.getName();
			sheet = workbook.createSheet(pipeline.getName() + " DATA NAMED");
		}
		if(subsetType.equals(FeatureSubset.ALL_FEATURES) || subsetType.equals(FeatureSubset.ALL_FEATURES_NO_QC)) {

			taskDescription = "Writing complete MS feature quant data for " + pipeline.getName();
			sheet = workbook.createSheet(pipeline.getName() + " DATA ALL");
		}
		if(subsetType.equals(FeatureSubset.QC_ONLY)){

			taskDescription = "Writing quality control data for " + pipeline.getName();
			sheet = workbook.createSheet(pipeline.getName() + " QC");
		}
		//	Write header
		int columnCount = 0;

		Row headerRow = sheet.createRow(0);
		Cell cell = headerRow.createCell(columnCount);
		cell.setCellValue(DataExportFields.FEATURE_EXPORT_NAME.getName());
		cell.setCellStyle(annotationStyle);

		Cell adductCell = headerRow.createCell(++columnCount);
		adductCell.setCellValue(CompoundAnnotationField.ADDUCT_TYPE.getName());
		adductCell.setCellStyle(annotationStyle);

		Cell mzhCell = headerRow.createCell(++columnCount);
		mzhCell.setCellValue(CompoundAnnotationField.OBSERVED_MZ.getName());
		mzhCell.setCellStyle(annotationStyle);

		Cell mcell = headerRow.createCell(++columnCount);
		mcell.setCellValue(CompoundAnnotationField.MONOISOTOPIC_NEUTRAL_MASS.getName());
		mcell.setCellStyle(annotationStyle);

		Cell rtcell = headerRow.createCell(++columnCount);
		rtcell.setCellValue(CompoundAnnotationField.RETENTION.getName());
		rtcell.setCellStyle(annotationStyle);

		Cell dbCell = headerRow.createCell(++columnCount);
		dbCell.setCellValue(CompoundAnnotationField.SOURCE_DB.getName());
		dbCell.setCellStyle(annotationStyle);

		Cell dbIdCell = headerRow.createCell(++columnCount);
		dbIdCell.setCellValue(CompoundAnnotationField.DB_ID.getName());
		dbIdCell.setCellStyle(annotationStyle);

		int annotationColumnCount = columnCount;

		//	Add sample columns
		TreeMap<ExperimentalSample, TreeMap<DataPipeline, DataFile[]>>sampleFileMap =
				DataExportUtils.createSampleFileMapForDataPipeline(currentExperiment, experimentDesignSubset, pipeline, exportFieldNaming);

		HashMap<DataFile, Integer> fileColumnMap =
				DataExportUtils.createFileColumnMap(sampleFileMap, columnCount);

		String[] columnList =
				DataExportUtils.createSampleColumnNameArray(sampleFileMap, exportFieldNaming);

		for(String colName : columnList) {

			Cell fileCell = headerRow.createCell(++columnCount);
			fileCell.setCellValue(colName);
			fileCell.setCellStyle(dataFileStyle);
		}
		// TODO Add statistics columns - recalculate values for exported design?

		//	Add data
		Matrix dataMatrix = currentExperiment.getDataMatrixForDataPipeline(pipeline);
		long[] coordinates = new long[2];

		total = exportArray.length;
		processed = 0;
		int rowCount = 0;

		for( MsFeature msf : exportArray){

			//	If feature in matrix - bug?
			coordinates[1] = dataMatrix.getColumnForLabel(msf);
			if(coordinates[1] < 0) {
				System.out.println(msf.getName());
				continue;
			}
			//	Add annotations
			String cpdName = msf.getName();
			String sourceDatabase = "";
			String databaseId = "";
			Hyperlink link = null;

			if(msf.isIdentified()){

				if(msf.getPrimaryIdentity().getIdentityName() != null)
					cpdName = msf.getPrimaryIdentity().getIdentityName();

				if(msf.getPrimaryIdentity().getPrimaryDatabase() != null){

					sourceDatabase = msf.getPrimaryIdentity().getPrimaryDatabase().getName();
					databaseId = msf.getPrimaryIdentity().getCompoundIdentity().getDbId(msf.getPrimaryIdentity().getPrimaryDatabase());
		            link = createHelper.createHyperlink(HyperlinkType.URL);
		            link.setAddress(msf.getPrimaryIdentity().getPrimaryLinkAddress());
				}
			}
			columnCount = 0;
			Row dataRow = sheet.createRow(++rowCount);
			Cell nameCell = dataRow.createCell(columnCount);
			nameCell.setCellValue(cpdName);

			dataRow.createCell(++columnCount).setCellValue(getAdductName(msf));

			Cell mzCell = dataRow.createCell(++columnCount);
			mzCell.setCellValue(msf.getMonoisotopicMz());
			mzCell.setCellStyle(mzStyle);

			Cell nmCell = dataRow.createCell(++columnCount);
			nmCell.setCellValue(msf.getNeutralMass());
			nmCell.setCellStyle(mzStyle);

			Cell rtCell = dataRow.createCell(++columnCount);
			rtCell.setCellValue(msf.getRetentionTime());
			rtCell.setCellStyle(rtStyle);

			dataRow.createCell(++columnCount).setCellValue(sourceDatabase);
			Cell dbLinkCell = dataRow.createCell(++columnCount);
			dbLinkCell.setCellValue(databaseId);
			if(link != null){

				dbLinkCell.setHyperlink(link);
				dbLinkCell.setCellStyle(hlinkStyle);
			}
			//	Add data
			for (Entry<ExperimentalSample, TreeMap<DataPipeline, DataFile[]>> entry : sampleFileMap.entrySet()) {

				for(DataFile df : entry.getValue().get(pipeline)) {

					coordinates[0] = dataMatrix.getRowForLabel(df);
					double value = Math.round(dataMatrix.getAsDouble(coordinates));
					Cell fileCell = dataRow.createCell(fileColumnMap.get(df));

					if(value > 0)
						fileCell.setCellValue(value);
				}
			}
			processed++;
		}
		//	Add QC summary (QC data only)
		if(subsetType.equals(FeatureSubset.QC_ONLY))
			createStatsSummary(sheet, rowCount, exportArray);

//		Integer max = fileColumnMap.values()
//			      .stream()
//			      .mapToInt(v -> v)
//			      .max().orElseThrow(NoSuchElementException::new);

		taskDescription = "Auto-adjusting columns";
		total = annotationColumnCount;
		processed = 0;
		for(int i=0; i<=annotationColumnCount; i++) {

			sheet.autoSizeColumn(i);
			processed++;
		}
		if(!subsetType.equals(FeatureSubset.QC_ONLY))
			sheet.createFreezePane(annotationColumnCount + 1, 1);
	}

	private void createStatsSummary(XSSFSheet sheet, int startRow, MsFeature[] exportArray) {

		taskDescription = "Creating statistical summary";
		total = exportArray.length;
		processed = 0;

		int columnCount = 0;
		int rowCount = startRow + 2;
		Row summaryRow = sheet.createRow(rowCount);

		Cell nameCell = summaryRow.createCell(columnCount);
		nameCell.setCellValue(DataExportFields.FEATURE_EXPORT_NAME.getName());
		nameCell.setCellStyle(annotationStyle);

		Cell adductCell2 = summaryRow.createCell(++columnCount);
		adductCell2.setCellValue(CompoundAnnotationField.ADDUCT_TYPE.getName());
		adductCell2.setCellStyle(annotationStyle);

		Cell pooledMeanCell = summaryRow.createCell(++columnCount);
		pooledMeanCell.setCellValue(CompoundAnnotationField.POOLED_MEAN.getName());
		pooledMeanCell.setCellStyle(annotationStyle);

		Cell pooledMedianCell = summaryRow.createCell(++columnCount);
		pooledMedianCell.setCellValue(CompoundAnnotationField.POOLED_MEDIAN.getName());
		pooledMedianCell.setCellStyle(annotationStyle);

		Cell pooledRsdCell = summaryRow.createCell(++columnCount);
		pooledRsdCell.setCellValue(CompoundAnnotationField.POOLED_RSD.getName());
		pooledRsdCell.setCellStyle(annotationStyle);

		Cell pooledFrequencyCell = summaryRow.createCell(++columnCount);
		pooledFrequencyCell.setCellValue(CompoundAnnotationField.POOLED_FREQUENCY.getName());
		pooledFrequencyCell.setCellStyle(annotationStyle);

		Cell sampleMeanCell = summaryRow.createCell(++columnCount);
		sampleMeanCell.setCellValue(CompoundAnnotationField.SAMPLE_MEAN.getName());
		sampleMeanCell.setCellStyle(annotationStyle);

		Cell sampleMedianCell = summaryRow.createCell(++columnCount);
		sampleMedianCell.setCellValue(CompoundAnnotationField.SAMPLE_MEDIAN.getName());
		sampleMedianCell.setCellStyle(annotationStyle);

		Cell sampleRsdCell = summaryRow.createCell(++columnCount);
		sampleRsdCell.setCellValue(CompoundAnnotationField.SAMPLE_RSD.getName());
		sampleRsdCell.setCellStyle(annotationStyle);

		Cell sampleFrequencyCell = summaryRow.createCell(++columnCount);
		sampleFrequencyCell.setCellValue(CompoundAnnotationField.SAMPLE_FREQUENCY.getName());
		sampleFrequencyCell.setCellStyle(annotationStyle);

		for( MsFeature msf : exportArray){

			//	Add annotations
			String cpdName = msf.getName();

			if(msf.isIdentified()){

				if(msf.getPrimaryIdentity().getName() != null)
					cpdName = msf.getPrimaryIdentity().getName();
			}
			else {
				cpdName = msf.getName();
			}
			columnCount = 0;

			Row qcStatsRow = sheet.createRow(++rowCount);
			qcStatsRow.createCell(columnCount).setCellValue(cpdName);
			qcStatsRow.createCell(++columnCount).setCellValue(getAdductName(msf));

			Cell poolMean = qcStatsRow.createCell(++columnCount);
			poolMean.setCellValue(msf.getStatsSummary().getPooledMean());
			poolMean.setCellStyle(rtStyle);

			Cell poolMedian = qcStatsRow.createCell(++columnCount);
			poolMedian.setCellValue(msf.getStatsSummary().getPooledMedian());
			poolMedian.setCellStyle(rtStyle);

			Cell poolRsd = qcStatsRow.createCell(++columnCount);
			poolRsd.setCellValue(msf.getStatsSummary().getPooledRsd());
			poolRsd.setCellStyle(percentStyle);

			Cell poolFreq = qcStatsRow.createCell(++columnCount);
			poolFreq.setCellValue(msf.getStatsSummary().getPooledFrequency());
			poolFreq.setCellStyle(percentStyle);

			Cell sampleMean = qcStatsRow.createCell(++columnCount);
			sampleMean.setCellValue(msf.getStatsSummary().getSampleMean());
			sampleMean.setCellStyle(rtStyle);

			Cell sampleMedian = qcStatsRow.createCell(++columnCount);
			sampleMedian.setCellValue(msf.getStatsSummary().getSampleMedian());
			sampleMedian.setCellStyle(rtStyle);

			Cell sampleRsd = qcStatsRow.createCell(++columnCount);
			sampleRsd.setCellValue(msf.getStatsSummary().getSampleRsd());
			sampleRsd.setCellStyle(percentStyle);

			Cell sampleFreq = qcStatsRow.createCell(++columnCount);
			sampleFreq.setCellValue(msf.getStatsSummary().getSampleFrequency());
			sampleFreq.setCellStyle(percentStyle);

			processed++;
		}
	}

	private String getAdductName(MsFeature f) {

		if(f.getPrimaryIdentity() != null) {

			MsRtLibraryMatch libid = f.getPrimaryIdentity().getMsRtLibraryMatch();
			if(libid != null) {

				if(libid.getTopAdductMatch() != null)
					return libid.getTopAdductMatch().getLibraryMatch().getName();
			}
		}
		if(f.getSpectrum() != null) {

			if(f.getSpectrum().getPrimaryAdduct() != null)
				return f.getSpectrum().getPrimaryAdduct().getName();
		}
		return "";
	}

	//	TODO figure which data pipelines used in the integrated set
	private void writeIntegratedReportData(){

		taskDescription = "Collecting active methods";
		total = 100;
		processed = 20;

		//	Collect named features from individual methods to filter integrated report
		Set<MsFeature>namedFeatures =
				assayFeatureMap.values().stream().
				flatMap(c -> c.getFeatures().stream()).
				filter(f -> f.isIdentified()).
				collect(Collectors.toSet());

		MsFeature[] exportArray =
				integratedSet.getFeatures().stream().
				filter(f -> !f.isQcStandard()).
				filter(f -> namedFeatures.contains(f)).
				sorted(new MsFeatureComparator(SortProperty.pimaryId)).
				toArray(MsFeature[]::new);

		taskDescription = "Creating sample => file map";
		
		//	TODO this is a placeholder untill data integration is re-written to use data pipelines
		TreeMap<ExperimentalSample, TreeMap<DataPipeline, DataFile[]>>sampleFileMap = 
				new TreeMap<ExperimentalSample, TreeMap<DataPipeline, DataFile[]>>();

		taskDescription = "Writing integrated report data for the experiment";
		total = exportArray.length;
		processed = 0;

		XSSFSheet sheet = workbook.createSheet("Integrated report");

		//	Write header
		Row headerRow = sheet.createRow(0);
		int columnCount = 0;

		Cell cell = headerRow.createCell(columnCount);
		cell.setCellValue(CompoundAnnotationField.COMPOUND_NAME.getName());
		cell.setCellStyle(annotationStyle);

		Cell mcell = headerRow.createCell(++columnCount);
		mcell.setCellValue(CompoundAnnotationField.MONOISOTOPIC_NEUTRAL_MASS.getName());
		mcell.setCellStyle(annotationStyle);

		Cell rtcell = headerRow.createCell(++columnCount);
		rtcell.setCellValue(CompoundAnnotationField.RETENTION.getName());
		rtcell.setCellStyle(annotationStyle);

		Cell assayCell = headerRow.createCell(++columnCount);
		assayCell.setCellValue(CompoundAnnotationField.ASSAY.getName());
		assayCell.setCellStyle(annotationStyle);

		Cell adductCell = headerRow.createCell(++columnCount);
		adductCell.setCellValue(CompoundAnnotationField.ADDUCT_TYPE.getName());
		adductCell.setCellStyle(annotationStyle);

		Cell dbCell = headerRow.createCell(++columnCount);
		dbCell.setCellValue(CompoundAnnotationField.SOURCE_DB.getName());
		dbCell.setCellStyle(annotationStyle);

		Cell dbIdCell = headerRow.createCell(++columnCount);
		dbIdCell.setCellValue(CompoundAnnotationField.DB_ID.getName());
		dbIdCell.setCellStyle(annotationStyle);

		Cell formulaCell = headerRow.createCell(++columnCount);
		formulaCell.setCellValue(CompoundAnnotationField.FORMULA.getName());
		formulaCell.setCellStyle(annotationStyle);

		Cell inchiCell = headerRow.createCell(++columnCount);
		inchiCell.setCellValue(CompoundAnnotationField.INCHI_KEY.getName());
		inchiCell.setCellStyle(annotationStyle);

		int annotationColumnCount = columnCount;

		// TODO Add statistics columns - recalculate values for exported design?
		//	Sample names
		HashMap<DataFile, Integer> fileColumnMap = 
				DataExportUtils.createFileColumnMap(sampleFileMap, columnCount);
		String[] columnList = 
				DataExportUtils.createSampleColumnNameArray(sampleFileMap, exportFieldNaming);

		for(String colName : columnList) {

			Cell fileCell = headerRow.createCell(++columnCount);
			fileCell.setCellValue(colName);
			fileCell.setCellStyle(dataFileStyle);
		}
		//	Add data
		long[] coordinates = new long[2];
		int rowCount = 0;
		Matrix dataMatrix = null;
		total = exportArray.length;
		processed = 0;

		for( MsFeature msf : exportArray){

			String cpdName = msf.getName();
			String sourceDatabase = "";
			String databaseId = "";
			String formula = "";
			String inchiKey = "";
			Hyperlink link = null;

			if(msf.isIdentified()){

				if(msf.getPrimaryIdentity().getName() != null)
					cpdName = msf.getPrimaryIdentity().getName();

				if(msf.getPrimaryIdentity().getCompoundIdentity().getFormula() != null)
					formula = msf.getPrimaryIdentity().getCompoundIdentity().getFormula();

				if(msf.getPrimaryIdentity().getPrimaryDatabase() != null){

					sourceDatabase = msf.getPrimaryIdentity().getPrimaryDatabase().getName();
					databaseId = msf.getPrimaryIdentity().getCompoundIdentity().getDbId(msf.getPrimaryIdentity().getPrimaryDatabase());
		            link = createHelper.createHyperlink(HyperlinkType.URL);
		            link.setAddress(msf.getPrimaryIdentity().getPrimaryLinkAddress());
				}
				if(msf.getPrimaryIdentity().getCompoundIdentity().getInChiKey() != null)
					inchiKey = msf.getPrimaryIdentity().getCompoundIdentity().getInChiKey();
			}
			Row dataRow = sheet.createRow(++rowCount);
			columnCount = 0;
			//	Annotations
			dataRow.createCell(columnCount).setCellValue(cpdName);

			Cell mzCell = dataRow.createCell(++columnCount);
			mzCell.setCellValue(msf.getNeutralMass());
			mzCell.setCellStyle(mzStyle);

			Cell rtCell = dataRow.createCell(++columnCount);
			rtCell.setCellValue(msf.getRetentionTime());
			rtCell.setCellStyle(rtStyle);

			//	TODO figure how to pass the DataPipeline
			//	dataRow.createCell(++columnCount).setCellValue(msf.getAssayMethod().getName());
			dataRow.createCell(++columnCount).setCellValue(getAdductName(msf));
			dataRow.createCell(++columnCount).setCellValue(sourceDatabase);

			//	DB link
			Cell dbLinkCell = dataRow.createCell(++columnCount);
			dbLinkCell.setCellValue(databaseId);
			if(link != null){

				dbLinkCell.setHyperlink(link);
				dbLinkCell.setCellStyle(hlinkStyle);
			}
			dataRow.createCell(++columnCount).setCellValue(formula);
			dataRow.createCell(++columnCount).setCellValue(inchiKey);
			
			//	TODO figure how to pass the DataPipeline 		
			//	Data
//			dataMatrix = currentProject.getDataMatrixForDataPipeline(msf.getAssayMethod());
//			coordinates[1] = dataMatrix.getColumnForLabel(msf);
//			for (Entry<ExperimentalSample, TreeMap<DataPipeline, DataFile[]>> entry : sampleFileMap.entrySet()) {
//
//				for(DataFile df : entry.getValue().get(msf.getAssayMethod())) {
//
//					coordinates[0] = dataMatrix.getRowForLabel(df);
//					double value = Math.round(dataMatrix.getAsDouble(coordinates));
//					Cell fileCell = dataRow.createCell(fileColumnMap.get(df));
//
//					if(value > 0)
//						fileCell.setCellValue(value);
//				}
//			}
			processed++;
		}
		taskDescription = "Auto-adjusting columns";
		total = annotationColumnCount;
		processed = 0;

		for(int i=0; i<=annotationColumnCount; i++) {

			sheet.autoSizeColumn(i);
			processed++;
		}
		sheet.createFreezePane(annotationColumnCount + 1, 1);
	}

	private void writeQCDataForMethod(Assay method) {
		// TODO Auto-generated method stub
		taskDescription = "Writing quality control data for " + method.getName();
		XSSFSheet sheet = workbook.createSheet(method.getName() + " QC");
	}



}

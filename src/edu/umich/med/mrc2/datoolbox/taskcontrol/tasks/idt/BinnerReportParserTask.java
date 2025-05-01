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

package edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.idt;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.github.pjfanning.xlsx.StreamingReader;

import edu.umich.med.mrc2.datoolbox.data.BinnerAnnotation;
import edu.umich.med.mrc2.datoolbox.data.CompoundClassifier;
import edu.umich.med.mrc2.datoolbox.data.CompoundIdentity;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.PostProcessorAnnotation;
import edu.umich.med.mrc2.datoolbox.data.enums.BinnerField;
import edu.umich.med.mrc2.datoolbox.data.enums.BinnerPageNames;
import edu.umich.med.mrc2.datoolbox.data.enums.BinnerPostProcessorField;
import edu.umich.med.mrc2.datoolbox.data.enums.BinnerPostProcessorPageNames;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundDatabaseEnum;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundIdentificationConfidence;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;

public abstract class BinnerReportParserTask extends AbstractTask {

	protected static final String molIonAnnotation = "[M]";
	protected File binnerDataFile;
	protected File postprocessorDataFile;	
	protected Collection<MsFeature>clusteredFeatures;
	protected Collection<PostProcessorAnnotation> ppAnnotations;
	protected Collection<BinnerAnnotation> binnerAnnotations;

	protected void parseBinnerResults() throws IOException {

		binnerAnnotations = new HashSet<BinnerAnnotation>();
		if (!binnerDataFile.exists() || !binnerDataFile.canRead()) {

			MessageDialog.showErrorMsg("Can not read Binner results file!");
			setStatus(TaskStatus.FINISHED);
		} 
		else {
			taskDescription = "Parsing Binner output file ...";
			total = 100;
			processed = 20;
			//	InputStream is = new FileInputStream(binnerDataFile);
			List<Sheet> sheetsToParse = new ArrayList<Sheet>();
			try(Workbook workbook = StreamingReader.builder()
			        .rowCacheSize(100)    // number of rows to keep in memory (defaults to 10)
			        .bufferSize(4096)     // buffer size to use when reading InputStream to file (defaults to 1024)
			        .open(new FileInputStream(binnerDataFile))){           // InputStream or File for XLSX file (required)
				
				for (Sheet sheet : workbook) {
	
					if(sheet.getSheetName().equalsIgnoreCase(BinnerPageNames.CORRELATIONS_BY_CLUSTER_LOC.getName())) {
						sheetsToParse.add(sheet);
						break;
					}
				}
				if(sheetsToParse.isEmpty()) {
					
					for (Sheet sheet : workbook) {
						
						if(sheet.getSheetName().equalsIgnoreCase(BinnerPageNames.PRINCIPAL_IONS.getName())
								|| sheet.getSheetName().equalsIgnoreCase(BinnerPageNames.DEGENERATE_FEATURES.getName()))
							sheetsToParse.add(sheet);
					}
				}
				if(sheetsToParse.isEmpty()) {
					
					MessageDialog.showErrorMsg("Worksheets not found:\n \"" 
							+ BinnerPageNames.CORRELATIONS_BY_CLUSTER_LOC.getName() + "\n"
							+ BinnerPageNames.PRINCIPAL_IONS.getName() + "\n"
							+ BinnerPageNames.DEGENERATE_FEATURES.getName() + "\n");
					setStatus(TaskStatus.FINISHED);
					return;
				}
				for (Sheet sheet : sheetsToParse) {
					
					Collection<BinnerAnnotation>annotations = 
							parseClusterCorrelationWorksheet(sheet);
					if(annotations != null && !annotations.isEmpty())
						binnerAnnotations.addAll(annotations);
				}
			}
			catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	protected Collection<BinnerAnnotation> parseClusterCorrelationWorksheet(Sheet sheet) {

		taskDescription = "Parsing Binner output file, worksheet " + sheet.getSheetName();
		total = sheet.getLastRowNum();
		processed = 0;
		Collection<BinnerAnnotation>annotations = new HashSet<BinnerAnnotation>();
		Cell currentCell;

		Map<BinnerField, Integer> columnMap = getBinnerColumnMap(sheet.iterator().next());
		
		List<BinnerField> obligatoryFields = 
				Arrays.asList(BinnerField.values()).stream().
				filter(v -> v.isObligatory()).collect(Collectors.toList());
		if(!CollectionUtils.containsAll(columnMap.keySet(), obligatoryFields)){
			
			Collection<BinnerField> missingColumns = 
					CollectionUtils.subtract(obligatoryFields, columnMap.keySet());
			
			List<String> missingColumnNames = 
					missingColumns.stream().map(c -> c.getName()).
					collect(Collectors.toList());

			MessageDialog.showErrorMsg(
					"Missing obligatory columns in Binner output:\n\n" +
			StringUtils.join(missingColumnNames, "\n"), 
					MRC2ToolBoxCore.getMainWindow());
			setStatus(TaskStatus.FINISHED);
			return annotations;
		}
		for (Row r : sheet) {
			processed++;
			if(r.getRowNum() > 0 && !r.getCell(0).getStringCellValue().trim().isEmpty()
					&& !r.getCell(columnMap.get(BinnerField.ANNOTATION)).getStringCellValue().trim().isEmpty()) {

				String featureName = 
						r.getCell(columnMap.get(BinnerField.FEATURE)).getStringCellValue();
				short fcol = r.getCell(
						columnMap.get(BinnerField.ANNOTATION)).getCellStyle().getFillForegroundColor();
				BinnerAnnotation ba =
						new BinnerAnnotation(featureName,
								r.getCell(columnMap.get(BinnerField.ANNOTATION)).getStringCellValue());
				if(fcol == 42)
					ba.setPrimary(true);

				if(columnMap.get(BinnerField.DERIVATIONS) != null) {
					
					ba.setDerivations(r.getCell(
							columnMap.get(BinnerField.DERIVATIONS)).getStringCellValue());
					ba.setIsotopes(
							r.getCell(columnMap.get(BinnerField.ISOTOPES)).getStringCellValue());
				}
				if(columnMap.get(BinnerField.MASS_ERROR) != null) {
					
					currentCell = r.getCell(columnMap.get(BinnerField.MASS_ERROR));
					if(currentCell.getCellType().equals(CellType.NUMERIC))
						ba.setMassError(currentCell.getNumericCellValue());
				}
				if(columnMap.get(BinnerField.RMD) != null) {
					
					currentCell = r.getCell(columnMap.get(BinnerField.RMD));
					if(currentCell.getCellType().equals(CellType.NUMERIC))
						ba.setRmd(currentCell.getNumericCellValue());
				}
				if(columnMap.get(BinnerField.FEATURE_GROUP_NUMBER) != null) {
					
					currentCell = r.getCell(columnMap.get(BinnerField.FEATURE_GROUP_NUMBER));
					if(currentCell.getCellType().equals(CellType.NUMERIC))
						ba.setMolIonNumber((int) currentCell.getNumericCellValue());
				}		
				if(columnMap.get(BinnerField.CHARGE_CARRIER) != null) {
					
					ba.setChargeCarrier(r.getCell(
							columnMap.get(BinnerField.CHARGE_CARRIER)).getStringCellValue());
					ba.setAdditionalAdducts(
							r.getCell(columnMap.get(BinnerField.ADDITIONAL_ADDUCTS)).getStringCellValue());
				}
				if(columnMap.get(BinnerField.BIN) != null) {
					
					currentCell = r.getCell(columnMap.get(BinnerField.BIN));
					if(currentCell.getCellType().equals(CellType.NUMERIC))
						ba.setBinNumber((int) currentCell.getNumericCellValue());
				}
				if(columnMap.get(BinnerField.CLUSTER) != null) {
					
					currentCell = r.getCell(columnMap.get(BinnerField.CLUSTER));
					if(currentCell.getCellType().equals(CellType.NUMERIC))
						ba.setCorrClusterNumber((int) currentCell.getNumericCellValue());
				}
				if(columnMap.get(BinnerField.REBIN_SUBCLUSTER) != null) {
					
					currentCell = r.getCell(columnMap.get(BinnerField.REBIN_SUBCLUSTER));
					if(currentCell.getCellType().equals(CellType.NUMERIC))
						ba.setRebinSubclusterNumber((int) currentCell.getNumericCellValue());
				}
				if(columnMap.get(BinnerField.RT_SUBCLUSTER) != null) {
					
					currentCell = r.getCell(columnMap.get(BinnerField.RT_SUBCLUSTER));
					if(currentCell.getCellType().equals(CellType.NUMERIC))
						ba.setRtSubclusterNumber((int) currentCell.getNumericCellValue());
				}
				if(columnMap.get(BinnerField.MZ) != null) {
					
					currentCell = r.getCell(columnMap.get(BinnerField.MZ));
					if(currentCell.getCellType().equals(CellType.NUMERIC))
						ba.setBinnerMz(currentCell.getNumericCellValue());
				}
				if(columnMap.get(BinnerField.RT) != null) {
					
					currentCell = r.getCell(columnMap.get(BinnerField.RT));
					if(currentCell.getCellType().equals(CellType.NUMERIC))
						ba.setBinnerRt(currentCell.getNumericCellValue());
				}
				annotations.add(ba);
			}
		}
		return annotations;
	}

	protected Map<BinnerField,Integer>getBinnerColumnMap(Row header){

		Map<BinnerField,Integer>columnMap = new TreeMap<BinnerField,Integer>();
		int headerLength = header.getPhysicalNumberOfCells();
		for (int i=0; i<headerLength; i++) {

			Cell c = header.getCell(i);
			for(BinnerField field : BinnerField.values()) {

				if(c.getStringCellValue().equals(field.getName()))
					columnMap.put(field, i);
			}
		}
		return columnMap;
	}
	
	protected void parsePostProcessorAnnotations() throws IOException {

		if (!postprocessorDataFile.exists() || !postprocessorDataFile.canRead()) {

			MessageDialog.showWarningMsg("Can not read PostProcessor results file!");
			setStatus(TaskStatus.FINISHED);
		} else {
			taskDescription = "Parsing PostProcessor output file ...";
			total = 100;
			processed = 20;
			try (Workbook workbook = StreamingReader.builder()
					.rowCacheSize(100) // number of rows to keep in memory (defaults to 10)
					.bufferSize(4096) // buffer size to use when reading InputStream to file (defaults to 1024)
					.open(new FileInputStream(postprocessorDataFile))) { // InputStream or File for XLSX file (required)
				ppAnnotations = new ArrayList<PostProcessorAnnotation>();
				for (Sheet sheet : workbook) {

					if (sheet.getSheetName().equalsIgnoreCase(BinnerPostProcessorPageNames.ALL_FEATURES.getName())
							|| sheet.getSheetName()
									.equalsIgnoreCase(BinnerPostProcessorPageNames.ISTD_AND_REDUNDANT.getName())) {
						ppAnnotations.addAll(parsePostProcessorAnnotationsWorksheet(sheet));
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	protected Collection<PostProcessorAnnotation> parsePostProcessorAnnotationsWorksheet(Sheet sheet) {

		taskDescription = "Parsing PostProcessor output file ...";
		total = sheet.getLastRowNum();
		processed = 0;
		Collection<PostProcessorAnnotation>annotations = new ArrayList<PostProcessorAnnotation>();
		Cell currentCell;

		Map<BinnerPostProcessorField,Integer> columnMap = getPostProcessorColumnMap(sheet.iterator().next());
		if(!CollectionUtils.containsAll(columnMap.keySet(), Arrays.asList(BinnerPostProcessorField.values()))){

			MessageDialog.showErrorMsg("PostProcessor column naming mismatch!", MRC2ToolBoxCore.getMainWindow());
			setStatus(TaskStatus.FINISHED);
			return annotations;
		}
		for (Row r : sheet) {
			processed++;
			if(r.getRowNum() > 0) {

				if(r.getCell(columnMap.get(BinnerPostProcessorField.FEATURE)) == null)
					continue;

				if(r.getCell(columnMap.get(BinnerPostProcessorField.FEATURE)).getStringCellValue() == null)
					continue;

				if(r.getCell(columnMap.get(BinnerPostProcessorField.FEATURE)).getStringCellValue().trim().isEmpty())
					continue;

				//	Collect Binner annotation data
				BinnerAnnotation ba = extractBinnerAnnotationData(r, columnMap);
				if(ba != null) {

					CompoundIdentity ci = extractCompoundIdentity(r, columnMap);
					PostProcessorAnnotation ppa = new PostProcessorAnnotation(ci, ba);
					ppa.setVersionInfo(
							r.getCell(columnMap.get(BinnerPostProcessorField.VERSION_INFO)).getStringCellValue());
					ppa.setNote(
							r.getCell(columnMap.get(BinnerPostProcessorField.NOTE)).getStringCellValue());

					//	Synonyms & library match errors
					if(ci != null) {

						extractSynonyms(ppa,r, columnMap);
						extractClassifier(ppa,r, columnMap);

						currentCell = r.getCell(columnMap.get(BinnerPostProcessorField.DELTA_RT));
						if(currentCell.getCellType().equals(CellType.NUMERIC))
							ppa.setRtMatchEror(currentCell.getNumericCellValue());

						currentCell = r.getCell(columnMap.get(BinnerPostProcessorField.DELTA_MASS));
						if(currentCell.getCellType().equals(CellType.NUMERIC))
							ppa.setMzMatchEror(currentCell.getNumericCellValue());

						currentCell = r.getCell(columnMap.get(BinnerPostProcessorField.MSI_ID_LEVEL));
						if(currentCell.getCellType().equals(CellType.NUMERIC)) {

							CompoundIdentificationConfidence idc =
								CompoundIdentificationConfidence.getLevelByNumber((int)currentCell.getNumericCellValue());

							if(idc != null)
								ppa.setIdConfidence(idc);
						}
						if(currentCell.getCellType().equals(CellType.STRING)) {

							CompoundIdentificationConfidence idc =
								CompoundIdentificationConfidence.getLevelByNumber(Integer.parseInt(currentCell.getStringCellValue()));

							if(idc != null)
								ppa.setIdConfidence(idc);
						}
					}
					annotations.add(ppa);
				}
			}
		}
		return annotations;
	}
	
	protected Map<BinnerPostProcessorField,Integer>getPostProcessorColumnMap(Row header){

		Map<BinnerPostProcessorField,Integer>columnMap = new TreeMap<BinnerPostProcessorField,Integer>();
		int headerLength = header.getPhysicalNumberOfCells();
		for (int i=0; i<headerLength; i++) {

			Cell c = header.getCell(i);
			for(BinnerPostProcessorField field : BinnerPostProcessorField.values()) {

				if(c.getStringCellValue().equals(field.getName()))
					columnMap.put(field, i);
			}
		}
		return columnMap;
	}

	protected void extractClassifier(PostProcessorAnnotation ppa, Row r, Map<BinnerPostProcessorField, Integer> columnMap) {

		String superClass = r.getCell(columnMap.get(BinnerPostProcessorField.SUPER_CLASS)).getStringCellValue();
		String mainClass = r.getCell(columnMap.get(BinnerPostProcessorField.MAIN_CLASS)).getStringCellValue();
		String subClass = r.getCell(columnMap.get(BinnerPostProcessorField.SUB_CLASS)).getStringCellValue();

		if(!superClass.isEmpty() || mainClass.isEmpty() || subClass.isEmpty()) {

			CompoundClassifier classifier = new CompoundClassifier();
			classifier.setSuperClass(superClass);
			classifier.setMainClass(mainClass);
			classifier.setSubClass(subClass);
			ppa.setClassifier(classifier);
		}
	}

	protected void extractSynonyms(PostProcessorAnnotation ppa, Row r, Map<BinnerPostProcessorField, Integer> columnMap) {

		ppa.addSynonym(r.getCell(columnMap.get(BinnerPostProcessorField.BROAD_NAME)).getStringCellValue(),
				BinnerPostProcessorField.BROAD_NAME.getName());
		ppa.addSynonym(r.getCell(columnMap.get(BinnerPostProcessorField.HMDB_NAME)).getStringCellValue(),
				BinnerPostProcessorField.HMDB_NAME.getName());
		ppa.addSynonym(r.getCell(columnMap.get(BinnerPostProcessorField.MOTRPAC_COMPOUND_NAME_OLD)).getStringCellValue(),
				BinnerPostProcessorField.MOTRPAC_COMPOUND_NAME_OLD.getName());
		ppa.addSynonym(r.getCell(columnMap.get(BinnerPostProcessorField.TEMPORARY_NAME_FROM_PILOT)).getStringCellValue(),
				BinnerPostProcessorField.TEMPORARY_NAME_FROM_PILOT.getName());
		ppa.addSynonym(r.getCell(columnMap.get(BinnerPostProcessorField.OTHER_NAME1)).getStringCellValue(),
				BinnerPostProcessorField.OTHER_NAME1.getName());
		ppa.addSynonym(r.getCell(columnMap.get(BinnerPostProcessorField.OTHER_NAME2)).getStringCellValue(),
				BinnerPostProcessorField.OTHER_NAME2.getName());
		ppa.addSynonym(r.getCell(columnMap.get(BinnerPostProcessorField.OTHER_NAME3)).getStringCellValue(),
				BinnerPostProcessorField.OTHER_NAME3.getName());
		ppa.addSynonym(r.getCell(columnMap.get(BinnerPostProcessorField.OTHER_NAME4)).getStringCellValue(),
				BinnerPostProcessorField.OTHER_NAME4.getName());
	}

	protected CompoundIdentity extractCompoundIdentity(Row r, Map<BinnerPostProcessorField,Integer> columnMap) {

		String compoundName = r.getCell(columnMap.get(BinnerPostProcessorField.COMPOUND_NAME)).getStringCellValue();
		if(compoundName.isEmpty())
			return null;

		String molFormula = r.getCell(columnMap.get(BinnerPostProcessorField.FORMULA)).getStringCellValue();
		CompoundIdentity cid = new CompoundIdentity(compoundName, molFormula);

		Cell currentCell = r.getCell(columnMap.get(BinnerPostProcessorField.COMPOUND_MASS));
		if(currentCell.getCellType().equals(CellType.NUMERIC))
			cid.setExactMass(currentCell.getNumericCellValue());

		cid.setInChiKey(Objects.toString(
				r.getCell(columnMap.get(BinnerPostProcessorField.INCHI_KEY)).getStringCellValue(), ""));

		String casId = r.getCell(columnMap.get(BinnerPostProcessorField.CAS)).getStringCellValue();
		if(!casId.isEmpty())
			cid.addDbId(CompoundDatabaseEnum.CAS, casId);

		String chebiId = r.getCell(columnMap.get(BinnerPostProcessorField.CHEBI)).getStringCellValue();
		if(!chebiId.isEmpty())
			cid.addDbId(CompoundDatabaseEnum.CHEBI, chebiId);

		String hmdbId = r.getCell(columnMap.get(BinnerPostProcessorField.HMDB_ID)).getStringCellValue();
		if(!chebiId.isEmpty())
			cid.addDbId(CompoundDatabaseEnum.HMDB, hmdbId);

		String keggId = r.getCell(columnMap.get(BinnerPostProcessorField.KEGGS)).getStringCellValue();
		if(!chebiId.isEmpty())
			cid.addDbId(CompoundDatabaseEnum.KEGG, keggId);

		String lipidMapsId = r.getCell(columnMap.get(BinnerPostProcessorField.LIPIDMAPS_ID)).getStringCellValue();
		if(!lipidMapsId.isEmpty())
			cid.addDbId(CompoundDatabaseEnum.LIPIDMAPS, lipidMapsId);

		String pubchemId = r.getCell(columnMap.get(BinnerPostProcessorField.PUBMED_ID)).getStringCellValue();
		if(!pubchemId.isEmpty())
			cid.addDbId(CompoundDatabaseEnum.PUBCHEM, pubchemId);

		String refmetId = r.getCell(columnMap.get(BinnerPostProcessorField.PUBMED_ID)).getStringCellValue();
		if(!refmetId.isEmpty())
			cid.addDbId(CompoundDatabaseEnum.REFMET, refmetId);

		String motrpacId = r.getCell(columnMap.get(BinnerPostProcessorField.MOTRPAC_ID)).getStringCellValue();
		if(!motrpacId.isEmpty())
			cid.addDbId(CompoundDatabaseEnum.MOTRPAC, motrpacId);

		return cid;
	}

	protected BinnerAnnotation extractBinnerAnnotationData(Row r, Map<BinnerPostProcessorField,Integer> columnMap) {

		String featureName = r.getCell(columnMap.get(BinnerPostProcessorField.FEATURE)).getStringCellValue();
		if(featureName.isEmpty())
			return null;

		BinnerAnnotation ba =
				new BinnerAnnotation(featureName,
						r.getCell(columnMap.get(BinnerPostProcessorField.ANNOTATIONS)).getStringCellValue());

		ba.setAdditionalGroupAnnotations(
				r.getCell(columnMap.get(BinnerPostProcessorField.OTHER_ANNOTATIONS_IN_GROUP)).getStringCellValue());
		ba.setFurtherAnnotations(
				r.getCell(columnMap.get(BinnerPostProcessorField.FURTHER_ANNOTATION)).getStringCellValue());
		ba.setDerivations(r.getCell(columnMap.get(BinnerPostProcessorField.DERIVATIONS)).getStringCellValue());
		ba.setIsotopes(r.getCell(columnMap.get(BinnerPostProcessorField.ISOTOPES)).getStringCellValue());
		ba.setAdditionalIsotopes(
				r.getCell(columnMap.get(BinnerPostProcessorField.OTHER_ISOPTOES_IN_GROUP)).getStringCellValue());

		Cell currentCell = r.getCell(columnMap.get(BinnerPostProcessorField.MASS_ERROR));
		if(currentCell.getCellType().equals(CellType.NUMERIC))
			ba.setMassError(currentCell.getNumericCellValue());
		
		currentCell = r.getCell(columnMap.get(BinnerPostProcessorField.FEATURE_GROUP_NUMBER));
		if(currentCell.getCellType().equals(CellType.NUMERIC))
			ba.setMolIonNumber((int) currentCell.getNumericCellValue());

		ba.setChargeCarrier(r.getCell(columnMap.get(BinnerPostProcessorField.CHARGE_CARRIER)).getStringCellValue());

		currentCell = r.getCell(columnMap.get(BinnerPostProcessorField.BIN));
		if(currentCell.getCellType().equals(CellType.NUMERIC))
			ba.setBinNumber((int) currentCell.getNumericCellValue());

		currentCell = r.getCell(columnMap.get(BinnerPostProcessorField.CORR_CLUSTER));
		if(currentCell.getCellType().equals(CellType.NUMERIC))
			ba.setCorrClusterNumber((int) currentCell.getNumericCellValue());

		currentCell = r.getCell(columnMap.get(BinnerPostProcessorField.REBIN_SUBCLUSTER));
		if(currentCell.getCellType().equals(CellType.NUMERIC))
			ba.setRebinSubclusterNumber((int) currentCell.getNumericCellValue());

		currentCell = r.getCell(columnMap.get(BinnerPostProcessorField.RT_SUBCLUSTER));
		if(currentCell.getCellType().equals(CellType.NUMERIC))
			ba.setRtSubclusterNumber((int) currentCell.getNumericCellValue());

		currentCell = r.getCell(columnMap.get(BinnerPostProcessorField.MASS));
		if(currentCell.getCellType().equals(CellType.NUMERIC))
			ba.setBinnerMz(currentCell.getNumericCellValue());

		currentCell = r.getCell(columnMap.get(BinnerPostProcessorField.RT));
		if(currentCell.getCellType().equals(CellType.NUMERIC))
			ba.setBinnerRt(currentCell.getNumericCellValue());

		return ba;
	}

	protected void createFeatureClusters(Collection<BinnerAnnotation> annotations) {

//		taskDescription = "Creating clusters from annotations ...";
//		total = annotations.size();
//		processed = 0;
//		Map<String, Collection<BinnerAnnotation>> annotationClusterMap =
//				new TreeMap<String, Collection<BinnerAnnotation>>();
//
//		for(BinnerAnnotation ba : annotations) {
//
//			String key = ba.getBinNumber() + ";" + ba.getCorrClusterNumber() + ";" +
//					ba.getRebinSubclusterNumber();
//
//			if(!annotationClusterMap.containsKey(key))
//				annotationClusterMap.put(key, new HashSet<BinnerAnnotation>());
//
//			annotationClusterMap.get(key).add(ba);
//			processed++;
//		}
//		taskDescription = "Mapping features to clusters ...";
//		total = annotationClusterMap.size();
//		processed = 0;
//		for (Entry<String, Collection<BinnerAnnotation>> entry : annotationClusterMap.entrySet()) {
//
//			MsFeatureCluster newCluster = new MsFeatureCluster();
//			for(BinnerAnnotation ba : entry.getValue()) {
//
//				MsFeature newFeature =
//					currentProject.getMsFeatureByBinnerNameMzRt(
//							ba.getFeatureName(), dataPipeline, ba.getBinnerMz(), ba.getBinnerRt());
//
//				if (newFeature != null) {
//
//					newFeature.setBinnerAnnotation(ba);
//					newCluster.addFeature(newFeature, dataPipeline);
//					if(newFeature.getBinnerAnnotation().isPrimary())
//						newCluster.setPrimaryFeature(newFeature);
//
//					clusteredFeatures.add(newFeature);
//				}
//				else {
//					System.out.println(ba.getFeatureName());
//				}
//			}
//			if (newCluster.getFeatures().size() > 0) {
//
//				newCluster.setClusterCorrMatrix(ClusterUtils.createClusterCorrelationMatrix(newCluster, false));
//				clusterList.add(newCluster);
//			}
//			processed++;
//		}
	}
	
	protected void createClustersFromPostProcessorAnnotations(Collection<PostProcessorAnnotation> ppAnnotations) {
		// TODO Auto-generated method stub
//		taskDescription = "Creating clusters from annotations ...";
//		total = ppAnnotations.size();
//		processed = 0;
//		Map<String, Collection<PostProcessorAnnotation>> annotationClusterMap =
//				new TreeMap<String, Collection<PostProcessorAnnotation>>();
//
//		for(PostProcessorAnnotation ppa : ppAnnotations) {
//
//			String key = ppa.getBa().getBinNumber() + ";" + ppa.getBa().getCorrClusterNumber() + ";" +
//					ppa.getBa().getRebinSubclusterNumber();
//
//			if(!annotationClusterMap.containsKey(key))
//				annotationClusterMap.put(key, new HashSet<PostProcessorAnnotation>());
//
//			annotationClusterMap.get(key).add(ppa);
//			processed++;
//		}
//		taskDescription = "Mapping features to clusters ...";
//		total = annotationClusterMap.size();
//		processed = 0;
//		for (Entry<String, Collection<PostProcessorAnnotation>> entry : annotationClusterMap.entrySet()) {
//
//			MsFeatureCluster newCluster = new MsFeatureCluster();
//			for(PostProcessorAnnotation ppa : entry.getValue()) {
//
//				MsFeature newFeature =
//					currentProject.getMsFeatureByBinnerNameMzRt(
//							ppa.getBa().getFeatureName(),
//							dataPipeline, 
//							ppa.getBa().getBinnerMz(), 
//							ppa.getBa().getBinnerRt());
//
//				if (newFeature != null) {
//
//					newFeature.addPostProcessorAnnotation(ppa);
//					if(!newCluster.containsFeature(newFeature))
//						newCluster.addFeature(newFeature, dataPipeline);
//
//					clusteredFeatures.add(newFeature);
//				}
//				else {
//					System.out.println(ppa.getBa().getFeatureName());
//				}
//			}
//			if (newCluster.getFeatures().size() > 0) {
//
//				newCluster.setClusterCorrMatrix(ClusterUtils.createClusterCorrelationMatrix(newCluster, false));
//				clusterList.add(newCluster);
//			}
//			processed++;
//		}
	}



	public File getBinnerDataFile() {
		return binnerDataFile;
	}
	

	public File getPostprocessorDataFile() {
		return postprocessorDataFile;
	}
	

	public Collection<PostProcessorAnnotation> getPpAnnotations() {
		return ppAnnotations;
	}
	

	public Collection<BinnerAnnotation> getBinnerAnnotations() {
		return binnerAnnotations;
	}	
}











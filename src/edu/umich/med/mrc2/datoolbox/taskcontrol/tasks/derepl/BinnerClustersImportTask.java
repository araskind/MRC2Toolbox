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
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.ujmp.core.Matrix;

import com.monitorjbl.xlsx.StreamingReader;

import edu.umich.med.mrc2.datoolbox.data.BinnerAnnotation;
import edu.umich.med.mrc2.datoolbox.data.CompoundClassifier;
import edu.umich.med.mrc2.datoolbox.data.CompoundIdentity;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureCluster;
import edu.umich.med.mrc2.datoolbox.data.PostProcessorAnnotation;
import edu.umich.med.mrc2.datoolbox.data.compare.MsFeatureComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.enums.BinnerField;
import edu.umich.med.mrc2.datoolbox.data.enums.BinnerPageNames;
import edu.umich.med.mrc2.datoolbox.data.enums.BinnerPostProcessorField;
import edu.umich.med.mrc2.datoolbox.data.enums.BinnerPostProcessorPageNames;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundDatabaseEnum;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundIdentificationConfidence;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.utils.ClusterUtils;

public class BinnerClustersImportTask extends AbstractTask {

	private static final String molIonAnnotation = "[M]";
	private File binnerDataFile;
	private File postprocessorDataFile;
	private HashSet<MsFeatureCluster> clusterList;
	private DataAnalysisProject currentProject;
	private DataPipeline dataPipeline;
	private Matrix dataMatrix;
	private ArrayList<String> unassignedFeatures;
	private Collection<MsFeature>clusteredFeatures;

	public BinnerClustersImportTask(File inputFile, File postprocessorDataFile) {

		super();
		this.binnerDataFile = inputFile;
		this.postprocessorDataFile = postprocessorDataFile;

		clusterList = new HashSet<MsFeatureCluster>();
		currentProject = MRC2ToolBoxCore.getActiveMetabolomicsExperiment();
		dataPipeline = currentProject.getActiveDataPipeline();
		taskDescription = "Loading Binner-generated clusters";
		dataMatrix = currentProject.getDataMatrixForDataPipeline(dataPipeline);
		Matrix featureMatrix = dataMatrix.getMetaDataDimensionMatrix(0);
		unassignedFeatures = new ArrayList<String>();
		clusteredFeatures = new TreeSet<MsFeature>(new MsFeatureComparator(SortProperty.Name));
	}

	@Override
	public void run() {

		setStatus(TaskStatus.PROCESSING);

		if(postprocessorDataFile != null) {

			//	Post-processor contains all Binner annotations,
			//	no need to import Binner results separately
			importPostProcesssorResults();
			setStatus(TaskStatus.FINISHED);
			return;
		}
		if(binnerDataFile != null)
			importClusteringResults();

		setStatus(TaskStatus.FINISHED);
	}

	private void importPostProcesssorResults() {

		if (!postprocessorDataFile.exists() || !postprocessorDataFile.canRead()) {

			MessageDialog.showWarningMsg("Can not read PostProcessor results file!");
			setStatus(TaskStatus.FINISHED);
		} else {
			resetFeatureAssignmentStatus();
			try {
				parsePostProcessorIdData();
			} catch (Exception e) {
				setStatus(TaskStatus.ERROR);
				e.printStackTrace();
			}
		}
	}

	private void importClusteringResults() {

		if (!binnerDataFile.exists() || !binnerDataFile.canRead()) {

			MessageDialog.showWarningMsg("Can not read Binner results file!");
			setStatus(TaskStatus.FINISHED);
		} else {
			resetFeatureAssignmentStatus();
			try {
				parseClusteringData();
			} catch (Exception e) {
				setStatus(TaskStatus.ERROR);
				e.printStackTrace();
			}
		}
	}

	private void parsePostProcessorIdData() throws Exception {

		taskDescription = "Parsing PostProcessor output file ...";
		total = 100;
		processed = 20;
		InputStream is = new FileInputStream(postprocessorDataFile);
		Workbook workbook = StreamingReader.builder()
		        .rowCacheSize(100)    // number of rows to keep in memory (defaults to 10)
		        .bufferSize(4096)     // buffer size to use when reading InputStream to file (defaults to 1024)
		        .open(is);            // InputStream or File for XLSX file (required)

		Collection<PostProcessorAnnotation> ppAnnotations = new ArrayList<PostProcessorAnnotation>();
		for (Sheet sheet : workbook) {

			if(sheet.getSheetName().equalsIgnoreCase(BinnerPostProcessorPageNames.ALL_FEATURES.getName())
					|| sheet.getSheetName().equalsIgnoreCase(BinnerPostProcessorPageNames.ISTD_AND_REDUNDANT.getName())) {
				ppAnnotations.addAll(parsePostProcessorAnnotationsWorksheet(sheet));
			}
		}
		is.close();
		createClustersFromPostProcessorAnnotations(ppAnnotations);
	}

	private void createClustersFromPostProcessorAnnotations(Collection<PostProcessorAnnotation> ppAnnotations) {
		// TODO Auto-generated method stub
		taskDescription = "Creating clusters from annotations ...";
		total = ppAnnotations.size();
		processed = 0;
		Map<String, Collection<PostProcessorAnnotation>> annotationClusterMap =
				new TreeMap<String, Collection<PostProcessorAnnotation>>();

		for(PostProcessorAnnotation ppa : ppAnnotations) {

			String key = ppa.getBa().getBinNumber() + ";" + ppa.getBa().getCorrClusterNumber() + ";" +
					ppa.getBa().getRebinSubclusterNumber();

			if(!annotationClusterMap.containsKey(key))
				annotationClusterMap.put(key, new HashSet<PostProcessorAnnotation>());

			annotationClusterMap.get(key).add(ppa);
			processed++;
		}
		taskDescription = "Mapping features to clusters ...";
		total = annotationClusterMap.size();
		processed = 0;
		for (Entry<String, Collection<PostProcessorAnnotation>> entry : annotationClusterMap.entrySet()) {

			MsFeatureCluster newCluster = new MsFeatureCluster();
			for(PostProcessorAnnotation ppa : entry.getValue()) {

				MsFeature newFeature =
					currentProject.getMsFeatureByBinnerNameMzRt(
							ppa.getBa().getFeatureName(),
							dataPipeline, 
							ppa.getBa().getBinnerMz(), 
							ppa.getBa().getBinnerRt());

				if (newFeature != null) {

					newFeature.addPostProcessorAnnotation(ppa);
					if(!newCluster.containsFeature(newFeature))
						newCluster.addFeature(newFeature, dataPipeline);

					clusteredFeatures.add(newFeature);
				}
				else {
					System.out.println(ppa.getBa().getFeatureName());
				}
			}
			if (newCluster.getFeatures().size() > 0) {

				newCluster.setClusterCorrMatrix(ClusterUtils.createClusterCorrelationMatrix(newCluster, false));
				clusterList.add(newCluster);
			}
			processed++;
		}
	}

	private Collection<PostProcessorAnnotation> parsePostProcessorAnnotationsWorksheet(Sheet sheet) {

		taskDescription = "Parsing PostProcessor output file ...";
		total = sheet.getLastRowNum();
		processed = 0;
		Collection<PostProcessorAnnotation>annotations = new ArrayList<PostProcessorAnnotation>();
		Cell currentCell;

		Map<BinnerPostProcessorField,Integer> columnMap = getPostProcessorColumnMap(sheet.iterator().next());
		if(!CollectionUtils.containsAll(columnMap.keySet(), Arrays.asList(BinnerPostProcessorField.values()))){

			MessageDialog.showErrorMsg("PostProcessor column naming mismatch!", MRC2ToolBoxCore.getMainWindow());
			setStatus(TaskStatus.FINISHED);
			return null;
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

	private void extractClassifier(PostProcessorAnnotation ppa, Row r, Map<BinnerPostProcessorField, Integer> columnMap) {

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

	private void extractSynonyms(PostProcessorAnnotation ppa, Row r, Map<BinnerPostProcessorField, Integer> columnMap) {

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

	private CompoundIdentity extractCompoundIdentity(Row r, Map<BinnerPostProcessorField,Integer> columnMap) {

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

	private BinnerAnnotation extractBinnerAnnotationData(Row r, Map<BinnerPostProcessorField,Integer> columnMap) {

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
		ba.setAditionalIsotopes(
				r.getCell(columnMap.get(BinnerPostProcessorField.OTHER_ISOPTOES_IN_GROUP)).getStringCellValue());

		Cell currentCell = r.getCell(columnMap.get(BinnerPostProcessorField.MASS_ERROR));
		if(currentCell.getCellType().equals(CellType.NUMERIC))
			ba.setMassError(currentCell.getNumericCellValue());

		currentCell = r.getCell(columnMap.get(BinnerPostProcessorField.KMD));
		if(currentCell.getCellType().equals(CellType.NUMERIC))
			ba.setKmd(currentCell.getNumericCellValue());

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

	private void parseClusteringData() throws Exception {

		taskDescription = "Parsing Binner output file ...";
		total = 100;
		processed = 20;
		InputStream is = new FileInputStream(binnerDataFile);
		Workbook workbook = StreamingReader.builder()
		        .rowCacheSize(100)    // number of rows to keep in memory (defaults to 10)
		        .bufferSize(4096)     // buffer size to use when reading InputStream to file (defaults to 1024)
		        .open(is);            // InputStream or File for XLSX file (required)

		Collection<BinnerAnnotation> annotations = null;

		for (Sheet sheet : workbook) {

			if(sheet.getSheetName().equalsIgnoreCase(BinnerPageNames.CORRELATIONS_BY_CLUSTER_LOC.getName())) {

				annotations = parseClusterCorrelationWorksheet(sheet);
				is.close();
				break;
			}
		}
		if(annotations != null)
			createFeatureClusters(annotations);
	}

	private void createFeatureClusters(Collection<BinnerAnnotation> annotations) {

		taskDescription = "Creating clusters from annotations ...";
		total = annotations.size();
		processed = 0;
		Map<String, Collection<BinnerAnnotation>> annotationClusterMap =
				new TreeMap<String, Collection<BinnerAnnotation>>();

		for(BinnerAnnotation ba : annotations) {

			String key = ba.getBinNumber() + ";" + ba.getCorrClusterNumber() + ";" +
					ba.getRebinSubclusterNumber();

			if(!annotationClusterMap.containsKey(key))
				annotationClusterMap.put(key, new HashSet<BinnerAnnotation>());

			annotationClusterMap.get(key).add(ba);
			processed++;
		}
		taskDescription = "Mapping features to clusters ...";
		total = annotationClusterMap.size();
		processed = 0;
		for (Entry<String, Collection<BinnerAnnotation>> entry : annotationClusterMap.entrySet()) {

			MsFeatureCluster newCluster = new MsFeatureCluster();
			for(BinnerAnnotation ba : entry.getValue()) {

				MsFeature newFeature =
					currentProject.getMsFeatureByBinnerNameMzRt(
							ba.getFeatureName(), dataPipeline, ba.getBinnerMz(), ba.getBinnerRt());

				if (newFeature != null) {

					newFeature.setBinnerAnnotation(ba);
					newCluster.addFeature(newFeature, dataPipeline);
					if(newFeature.getBinnerAnnotation().isPrimary())
						newCluster.setPrimaryFeature(newFeature);

					clusteredFeatures.add(newFeature);
				}
				else {
					System.out.println(ba.getFeatureName());
				}
			}
			if (newCluster.getFeatures().size() > 0) {

				newCluster.setClusterCorrMatrix(ClusterUtils.createClusterCorrelationMatrix(newCluster, false));
				clusterList.add(newCluster);
			}
			processed++;
		}
	}

	private Collection<BinnerAnnotation> parseClusterCorrelationWorksheet(Sheet sheet) {

		taskDescription = "Parsing Binner output file ...";
		total = sheet.getLastRowNum();
		processed = 0;
		Collection<BinnerAnnotation>annotations = new HashSet<BinnerAnnotation>();
		Cell currentCell;

		Map<BinnerField, Integer> columnMap = getBinnerColumnMap(sheet.iterator().next());
		if(!CollectionUtils.containsAll(columnMap.keySet(), Arrays.asList(BinnerField.values()))){

			MessageDialog.showErrorMsg("Binner column naming mismatch!", MRC2ToolBoxCore.getMainWindow());
			setStatus(TaskStatus.FINISHED);
			return null;
		}
		for (Row r : sheet) {
			processed++;
			if(r.getRowNum() > 0 &&!r.getCell(0).getStringCellValue().trim().isEmpty()
					&& !r.getCell(columnMap.get(BinnerField.ANNOTATION)).getStringCellValue().trim().isEmpty()) {

				String featureName = r.getCell(columnMap.get(BinnerField.FEATURE)).getStringCellValue();
				short fcol = r.getCell(columnMap.get(BinnerField.ANNOTATION)).getCellStyle().getFillForegroundColor();
				BinnerAnnotation ba =
						new BinnerAnnotation(featureName,
								r.getCell(columnMap.get(BinnerField.ANNOTATION)).getStringCellValue());
				if(fcol == 42)
					ba.setPrimary(true);

				ba.setDerivations(r.getCell(columnMap.get(BinnerField.DERIVATIONS)).getStringCellValue());
				ba.setIsotopes(r.getCell(columnMap.get(BinnerField.ISOTOPES)).getStringCellValue());

				currentCell = r.getCell(columnMap.get(BinnerField.MASS_ERROR));
				if(currentCell.getCellType().equals(CellType.NUMERIC))
					ba.setMassError(currentCell.getNumericCellValue());

				currentCell = r.getCell(columnMap.get(BinnerField.KMD));
				if(currentCell.getCellType().equals(CellType.NUMERIC))
					ba.setKmd(currentCell.getNumericCellValue());

				currentCell = r.getCell(columnMap.get(BinnerField.FEATURE_GROUP_NUMBER));
				if(currentCell.getCellType().equals(CellType.NUMERIC))
					ba.setMolIonNumber((int) currentCell.getNumericCellValue());

				ba.setChargeCarrier(r.getCell(columnMap.get(BinnerField.CHARGE_CARRIER)).getStringCellValue());
				ba.setAdditionalAdducts(r.getCell(columnMap.get(BinnerField.ADDITIONAL_ADDUCTS)).getStringCellValue());

				currentCell = r.getCell(columnMap.get(BinnerField.BIN));
				if(currentCell.getCellType().equals(CellType.NUMERIC))
					ba.setBinNumber((int) currentCell.getNumericCellValue());

				currentCell = r.getCell(columnMap.get(BinnerField.CLUSTER));
				if(currentCell.getCellType().equals(CellType.NUMERIC))
					ba.setCorrClusterNumber((int) currentCell.getNumericCellValue());

				currentCell = r.getCell(columnMap.get(BinnerField.REBIN_SUBCLUSTER));
				if(currentCell.getCellType().equals(CellType.NUMERIC))
					ba.setRebinSubclusterNumber((int) currentCell.getNumericCellValue());

				currentCell = r.getCell(columnMap.get(BinnerField.RT_SUBCLUSTER));
				if(currentCell.getCellType().equals(CellType.NUMERIC))
					ba.setRtSubclusterNumber((int) currentCell.getNumericCellValue());

				currentCell = r.getCell(columnMap.get(BinnerField.MZ));
				if(currentCell.getCellType().equals(CellType.NUMERIC))
					ba.setBinnerMz(currentCell.getNumericCellValue());

				currentCell = r.getCell(columnMap.get(BinnerField.RT));
				if(currentCell.getCellType().equals(CellType.NUMERIC))
					ba.setBinnerRt(currentCell.getNumericCellValue());

				annotations.add(ba);
			}
		}
		return annotations;
	}


	private Map<BinnerField,Integer>getBinnerColumnMap(Row header){

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

	private Map<BinnerPostProcessorField,Integer>getPostProcessorColumnMap(Row header){

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

	private void resetFeatureAssignmentStatus() {

		for (MsFeature cf : currentProject.getMsFeaturesForDataPipeline(dataPipeline)) {
			cf.setActive(true);
			cf.setBinnerAnnotation(null);
		}
	}

	@Override
	public Task cloneTask() {
		return new BinnerClustersImportTask(
				this.binnerDataFile, this.postprocessorDataFile);
	}

	public HashSet<MsFeatureCluster> getFeatureClusters() {
		return clusterList;
	}

	public ArrayList<String> getUnassignedFeatures() {
		return unassignedFeatures;
	}
}











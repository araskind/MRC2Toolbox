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

package edu.umich.med.mrc2.datoolbox.utils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import edu.umich.med.mrc2.datoolbox.data.Assay;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSExperiment;
import edu.umich.med.mrc2.datoolbox.database.lims.LIMSDataCache;
import edu.umich.med.mrc2.datoolbox.database.lims.LIMSUtils;

public class LIMSReportingUtils {

	public static final String UNTARGETED_ASSAY_ID = "A003";
	public static final String LIPIDOMICS_ASSAY_ID = "A004";
	public static final String CENTRAL_CARBON_METABOLISM_ASSAY_ID = "A049";

	public static Path createExperimentDataDirectoryStructure(String experimentId, File parentDirectory) throws Exception {

		if(parentDirectory == null)
			return null;

		LIMSExperiment experiment = LIMSDataCache.getExperimentById(experimentId);
		if(experiment == null)
			return null;

		//	Create Experiment directory
		String expDirName = experimentId + " - " + experiment.getName().replaceAll("\\W+", " ").trim();
		Path expDirPath = Files.createDirectory(Paths.get(parentDirectory.getAbsolutePath(), expDirName));

		//	Create assay subdirectories
		Collection<Assay>assays = LIMSUtils.getAssaysForExperiment(experimentId);
		if(!assays.isEmpty()) {

			for(Assay assay : assays) {

				Path assayDirPath = Files.createDirectory(Paths.get(expDirPath.toString(),
						assay.getId() + " - " + assay.getName().replaceAll("\\W+", " ").trim()));

				//	Common elements for all assays
				Path rawDataPath = Files.createDirectory(Paths.get(assayDirPath.toString(), "Raw data"));
				Files.createDirectory(Paths.get(assayDirPath.toString(), "Design"));
				Files.createDirectory(Paths.get(assayDirPath.toString(), "Documents"));
				Files.createDirectory(Paths.get(assayDirPath.toString(), "Report"));
				Files.createDirectory(Paths.get(assayDirPath.toString(), "Methods"));				

				//	Folders specific for untargeted assay
				if(assay.getId().equals(UNTARGETED_ASSAY_ID)) {

					Files.createDirectory(Paths.get(assayDirPath.toString(), "BatchMatch"));
					Files.createDirectory(Paths.get(assayDirPath.toString(), "QCaNVaS"));
					
					//	Raw data
					Files.createDirectory(Paths.get(rawDataPath.toString(), "POS"));
					Files.createDirectory(Paths.get(rawDataPath.toString(), "NEG"));

					//	MFE
					Files.createDirectories(Paths.get(assayDirPath.toString(), "MFE", "POS"));
					Files.createDirectories(Paths.get(assayDirPath.toString(), "MFE", "NEG"));

					//	FBF
					Files.createDirectories(Paths.get(assayDirPath.toString(), "FBF-recursive", "POS"));
					Files.createDirectories(Paths.get(assayDirPath.toString(), "FBF-recursive", "NEG"));

					//	Recursion libraries
					Files.createDirectories(Paths.get(assayDirPath.toString(), "Recursion libraries", "POS"));
					Files.createDirectories(Paths.get(assayDirPath.toString(), "Recursion libraries", "NEG"));
				}
				if(assay.getId().equals(LIPIDOMICS_ASSAY_ID)) {
					
					Files.createDirectory(Paths.get(rawDataPath.toString(), "POS"));
					Files.createDirectory(Paths.get(rawDataPath.toString(), "NEG"));

				}
				if(assay.getId().equals(CENTRAL_CARBON_METABOLISM_ASSAY_ID)) {
					
					Files.createDirectory(Paths.get(assayDirPath.toString(), "BatchMatch"));
					Files.createDirectory(Paths.get(assayDirPath.toString(), "QCaNVaS"));
					
					//	Raw data
					Files.createDirectory(Paths.get(rawDataPath.toString(), "NEG"));

					//	MFE
					Files.createDirectories(Paths.get(assayDirPath.toString(), "MFE", "NEG"));

					//	FBF
					Files.createDirectories(Paths.get(assayDirPath.toString(), "FBF-recursive", "NEG"));

					//	Recursion libraries
					Files.createDirectories(Paths.get(assayDirPath.toString(), "Recursion libraries", "NEG"));
				}			
			}
		}
		return expDirPath;
	}
	
	/**
	 * @param tissueTypes - list of tissue types in the experiment
	 * @param assayTypes - list of assays
	 * @param parentDirectory
	 * @throws Exception
	 */
	public static void createMotrpacDataUploadDirectoryStructure(
			List<String> tissueTypes, 
			List<String> assayTypes, 
			File parentDirectory,
			int batchNumber,
			String batchDateIdentifier,
			String processingDateIdentifier,
			String studyPhase) throws Exception {

		if(parentDirectory == null)
			return;

		if(tissueTypes.isEmpty() || assayTypes.isEmpty())
			return;
		
		String batchId = "BATCH" + Integer.toString(batchNumber) + "_" + batchDateIdentifier;
		String processedFolderId  = "PROCESSED_" + processingDateIdentifier;
		
		for(String tissue : tissueTypes) {
						
			for(String assay : assayTypes) {
				
				Files.createDirectories(
						Paths.get(parentDirectory.getAbsolutePath(), tissue, assay, batchId, "RAW"));
				Files.createDirectories(
						Paths.get(parentDirectory.getAbsolutePath(), tissue, assay, batchId, processedFolderId, "NAMED"));
				Files.createDirectories(
						Paths.get(parentDirectory.getAbsolutePath(), tissue, assay, batchId, processedFolderId, "UNNAMED"));
				
				//	Write metadata  phase file
				Path outputPath = Paths.get(parentDirectory.getAbsolutePath(), tissue, assay, batchId, "metadata_phase.txt");
				try {
					Files.write(outputPath, 
							Collections.singleton(studyPhase), 
							StandardCharsets.UTF_8,
							StandardOpenOption.CREATE, 
							StandardOpenOption.TRUNCATE_EXISTING);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}













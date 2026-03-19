/*******************************************************************************
 *
 * (C) Copyright 2018-2026 MRC2 (http://mrc2.umich.edu).
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

package edu.umich.med.mrc2.datoolbox.rqc;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import edu.umich.med.mrc2.datoolbox.gui.rgen.TemplareRbasedProjectGenerator;
import edu.umich.med.mrc2.datoolbox.gui.rgen.mcr.RMultibatchAnalysisInputObject;
import edu.umich.med.mrc2.datoolbox.gui.rgen.modality.ModalityAnalysisParametersObject;
import edu.umich.med.mrc2.datoolbox.utils.FIOUtils;

public class ModalityAnalysisScriptGenerator extends TemplareRbasedProjectGenerator {
	
	public static final String MODALITY_ANALYSIS_PROJECT_BASE_NAME = "ModalityAnalysis-";
	public static final String SCRIPT_FILE_PREFIX = "ModalityAnalysisScript_";
	public static final String DESIGN_OBJECT_SUFFIX = ".design";
	public static final String VALID_SAMPLES_LIST_SUFFIX = ".valid.samples";
	public static final String FEATURE_LIST_SUFFIX = ".features";
	public static final String METADATA_SUFFIX = ".metadata";
	public static final String INPUT_DATA_SUFFIX = ".in";
	public static final String CLEAN_OBJECT_SUFFIX = ".clean";
	public static final String MODE_STATS_SUFFIX = ".mod.stats";
	public static final String OUTPUT_SUFFIX = ".MZRT.modality.stats";
	public static final String OUTPUT_FILE_SUFFIX = "_RTMZ_modality_analysis_";
	
	public static final List<SummaryInputColumns> requiredProperties = 
			Arrays.asList(
					SummaryInputColumns.EXPERIMENT, 
					SummaryInputColumns.BATCH,
					SummaryInputColumns.MANIFEST);
	public static final List<SummaryInputColumns> selectableProperties = 
			Arrays.asList(
					SummaryInputColumns.RT_VALUES,
					SummaryInputColumns.MZ_VALUES, 
					SummaryInputColumns.PEAK_QUALITY,
					SummaryInputColumns.PEAK_WIDTH);
	public static final List<SummaryInputColumns> allProperties = 
			Arrays.asList(
					SummaryInputColumns.EXPERIMENT, 
					SummaryInputColumns.BATCH,
					SummaryInputColumns.RT_VALUES,
					SummaryInputColumns.MZ_VALUES, 
					SummaryInputColumns.PEAK_QUALITY,
					SummaryInputColumns.PEAK_WIDTH);
	
	private ModalityAnalysisParametersObject parameters;
	
	public ModalityAnalysisScriptGenerator(ModalityAnalysisParametersObject parameters) {
		super();
		this.parameters = parameters;

	}

	public void createMultiBatchMZRTDistributionModalityAnalysisScript() {

		createProjectDirectoryStructure();
		
		File dataDir = parameters.getProjectParentDirectory();
//		Set<RMultibatchAnalysisInputObject>mcioSet,
//		int maxPercenrMissing
		initRscript();

		
		for(RMultibatchAnalysisInputObject sdio : parameters.getMetabCombinerFileInputObjectSet()) {
			
			String dataObjectPrefix = 
					sdio.getProperty(SummaryInputColumns.EXPERIMENT) + "." + 
					sdio.getProperty(SummaryInputColumns.BATCH);
			
			rscriptParts.add("\n## Multimodality analysis for " 
					+ sdio.getProperty(SummaryInputColumns.EXPERIMENT) 
					+ "," + sdio.getProperty(SummaryInputColumns.BATCH) + " ####");
			
			createValidSampleObject(sdio, dataDir, dataObjectPrefix, rscriptParts);
			
			String rtResultsObjectName = 
					createMultimodalytyAnalysisBlockForParameter(
							sdio, dataDir, dataObjectPrefix, SummaryInputColumns.RT_VALUES, 
							parameters.getMaxPercenrMissing(), rscriptParts);
			String mzResultsObjectName = 
					createMultimodalytyAnalysisBlockForParameter(
							sdio, dataDir, dataObjectPrefix, SummaryInputColumns.MZ_VALUES, 
							parameters.getMaxPercenrMissing(), rscriptParts);
			
			String metaDataObject = createMetaDataObject(dataObjectPrefix, rscriptParts);
			
			rscriptParts.add("\n#### Generate output ####");
			String resultsObject = dataObjectPrefix + OUTPUT_SUFFIX;
			rscriptParts.add(resultsObject + " <-  merge(" 
					+ rtResultsObjectName + ", " + mzResultsObjectName+ ", by = 0, all = T)");
			rscriptParts.add("colnames(" + resultsObject + ")[1] <- \"Feature\"");
			rscriptParts.add(resultsObject + " <- right_join(" + metaDataObject 
					+ ", " + resultsObject + ") %>% arrange(MZ,RT)");
			
			String outputFileName =
					sdio.getProperty(SummaryInputColumns.EXPERIMENT) + "_" + 
					sdio.getProperty(SummaryInputColumns.BATCH) +
					OUTPUT_FILE_SUFFIX + FIOUtils.getTimestamp()+ ".txt";
			rscriptParts.add("write.table(" + resultsObject + ", file = \"" 
					+ outputFileName + "\", quote = F, sep = \"\\t\", na = \"\", row.names = F)");
		}
		writeScriptToFile();
	}
	
	protected void createProjectDirectoryStructure() {

		File projectParentDir = parameters.getProjectParentDirectory();
		String projectName = MODALITY_ANALYSIS_PROJECT_BASE_NAME + FIOUtils.getTimestamp();
		Path newProjectPath = Paths.get(projectParentDir.getAbsolutePath(), projectName);		
		try {
			Files.createDirectories(newProjectPath);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		projectFolder = newProjectPath.toFile();
		parameters.setProjectDirectory(projectFolder);
		scriptFile = Paths.get(newProjectPath.toString(), 
				SCRIPT_FILE_PREFIX + FIOUtils.getTimestamp() + ".R" ).toFile();
	}
	
	protected void initRscript() {
		
		String workDirForR = parameters.getProjectDirectory().getAbsolutePath().replaceAll("\\\\", "/");
		rscriptParts.add("# Find features with multimodal distribution of RT and MZ values ####\n");
		rscriptParts.add("setwd(\"" + workDirForR + "\")\n");

		rscriptParts.add("library(dplyr)");
		rscriptParts.add("library(purrr)");
		rscriptParts.add("library(multimode)");
		rscriptParts.add("library(future.apply)");
		rscriptParts.add("plan(multisession)\n");
		rscriptParts.add("checkMode <- function(df.line) {");
		rscriptParts.add("\tmt <- modetest(as.numeric(df.line))");
		rscriptParts.add("\tdf <- do.call(rbind, mt)");
		rscriptParts.add("\treturn (df)");
		rscriptParts.add("}\n");
		
		rscriptParts.add("stat.result.names <- c(\"statistic\",\"p.value\",\"null.value\",\"alternative\",\"method\",\"sample.size\",\"data.name\",\"bad.obs\")\n");
		rscriptParts.add("empty.list <- list(statistic = NA, p.value = NA, null.value = NA, "
				+ "alternative = NA, method = NA, sample.size = NA, data.name = NA, bad.obs = NA)");
		rscriptParts.add("class(empty.list) <- \"htest\"");
		rscriptParts.add("edf <- do.call(rbind, empty.list)");
	}
	
	private void createValidSampleObject(
			RMultibatchAnalysisInputObject sdio,
			File dataDir,
			String dataObjectPrefix,			
			List<String>rscriptParts) {
		
		rscriptParts.add("\n### Select only samples and pools ####");
		String designObject = dataObjectPrefix + DESIGN_OBJECT_SUFFIX;
		String filePath = Paths.get(dataDir.getAbsolutePath(), 
				sdio.getProperty(SummaryInputColumns.MANIFEST)).toString();
		rscriptParts.add(designObject + " <- read.delim(r'(" + filePath + ")', check.names=FALSE)");
		String validSamplesObject = dataObjectPrefix + VALID_SAMPLES_LIST_SUFFIX;
		rscriptParts.add(validSamplesObject + " <- " + designObject + 
				" %>% filter(!(sample_type %in% c(\"QC-Blank\",\"QC-InternalStandard\",\"QC-Reference\"))) "
				+ "%>% select(raw_file)");
		rscriptParts.add(validSamplesObject + " <- as.character(" + validSamplesObject + "[,1])");
	}
	
	private String createMetaDataObject(
			String dataObjectPrefix,
			List<String>rscriptParts) {
		
		rscriptParts.add("\n#### Create MZ and RT metadata object ####");
		String metaDataObject = dataObjectPrefix + METADATA_SUFFIX;	
		String dataObjectIn = dataObjectPrefix + "." 
				+ SummaryInputColumns.RT_VALUES.getRName() + INPUT_DATA_SUFFIX;
		rscriptParts.add(metaDataObject + " <- " + dataObjectIn 
				+ " %>% select(\"Feature name\",\"RT observed\",\"Monoisotopic M/Z\")");
		rscriptParts.add("colnames(" + metaDataObject + ") <- c(\"Feature\",\"RT\",\"MZ\")");
				
		return metaDataObject;
	}
	
	private String createMultimodalytyAnalysisBlockForParameter(
			RMultibatchAnalysisInputObject sdio,
			File dataDir,
			String dataObjectPrefix,
			SummaryInputColumns parameter,
			int maxPercenrMissing,
			List<String>rscriptParts) {
		
		rscriptParts.add("\n#### Analyze " + parameter.getName() + " for multimodal distribution ####");
		String paramDataObject = dataObjectPrefix + "." + parameter.getRName();		
		String dataObjectIn = paramDataObject + INPUT_DATA_SUFFIX;
		String filePath = Paths.get(dataDir.getAbsolutePath(), sdio.getProperty(parameter)).toString();
		
		rscriptParts.add(dataObjectIn + " <- read.delim(r'(" + filePath + ")', check.names=FALSE)");
		rscriptParts.add(paramDataObject + " <- " + dataObjectIn + "[,-c(1:9)]");
		
		String validSamplesObject = dataObjectPrefix + VALID_SAMPLES_LIST_SUFFIX;
		rscriptParts.add(paramDataObject + " <- as.data.frame(" + paramDataObject);
		rscriptParts.add("\t%>% select(all_of(" + validSamplesObject + "))");
		rscriptParts.add("\t%>% rowwise() %>% mutate(");
		rscriptParts.add("\t\tnum_missing = sum(is.na(c_across(everything()))),");
		rscriptParts.add("\t\tpercent_missing = (num_missing / ncol(" + paramDataObject + ")) * 100) %>% ");
		rscriptParts.add("ungroup())");		
		rscriptParts.add("rownames(" + paramDataObject + ") <- " + dataObjectIn + "$`Feature name`");
		String paramDataObjectClean = paramDataObject + CLEAN_OBJECT_SUFFIX;
		rscriptParts.add(paramDataObjectClean + " <- " + paramDataObject 
				+ " %>% filter(percent_missing < " + Integer.toString(maxPercenrMissing) 
				+ ") %>% select(-num_missing, -percent_missing)");
		String modStatsObject = dataObjectPrefix + "." + parameter.getRName() + MODE_STATS_SUFFIX;		
		rscriptParts.add(modStatsObject + " <- as.data.frame(t(future_apply(" + 
				paramDataObjectClean + ", 1, possibly(checkMode, otherwise = empty.list))))");
		rscriptParts.add(modStatsObject + "[] <- lapply(" + modStatsObject + ", function(x) as.numeric(as.character(x)))");
		
		rscriptParts.add("colnames("+ modStatsObject + ") <- stat.result.names");
		rscriptParts.add(modStatsObject + " <- " + modStatsObject + " %>% select(\"statistic\",\"p.value\",\"sample.size\",\"bad.obs\")");
		rscriptParts.add("colnames(" + modStatsObject + ") <- paste(colnames(" + modStatsObject + "),\"." 
				+ parameter.getRName() + "\", sep = \"\")");
		
		return modStatsObject;
	}
}

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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.jdom2.Document;
import org.jdom2.Element;

import edu.umich.med.mrc2.datoolbox.data.enums.MoTrPACQCSampleType;
import edu.umich.med.mrc2.datoolbox.data.enums.MoTrPACRawDataManifestFields;
import edu.umich.med.mrc2.datoolbox.gui.rgen.TemplateRbasedProjectGenerator;
import edu.umich.med.mrc2.datoolbox.gui.rgen.mcr.MetabCombinerParametersObject;
import edu.umich.med.mrc2.datoolbox.gui.rgen.mcr.RMultibatchAnalysisInputObject;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.DefaultFormatStore;
import edu.umich.med.mrc2.datoolbox.utils.FIOUtils;
import edu.umich.med.mrc2.datoolbox.utils.XmlUtils;

public class MetabCombinerAlignmentScriptGenerator extends TemplateRbasedProjectGenerator {
	
	public static final String MC_ALIGNMENT_PROJECT_BASE_NAME = "MetabCombinerMultiBatchAlignment-";
	public static final String SCRIPT_FILE_PREFIX = "MetabCombinerMultiBatchAlignmentScript_";
	public static final String ALIGNMENT_SUMMARY_TABLE_FILE_NAME = "AlignmentSummaryTable.txt";
	public static final String MERGED_ALIGNED_DATA_FILE_NAME = "MergedAlignedData.txt";
	public static final String MERGED_ALIGNED_IMPUTED_DATA_FILE_NAME = "MergedAlignedImputedData.txt";
	public static final String CLEAN_DATA_FILE_SUFFIX= "-cleanData.txt";
	public static final String INPUT_STATS_FILE_PREFIX = "MetabCombinerMultiAlignmentInputStats_";
	public static final String CUMMULATIVE_METADATA_FILE_NAME = "CummulativeMetaData.txt";
	public static final String EXTENDED_CUMMULATIVE_METADATA_FILE_NAME = "CummulativeMetaDataEFS.txt";
	public static final String ADDUCT_REPRODUCIBILITY_FILE_NAME = "AdductReproducibility.pdf";
	public static final String ALIGNMENT_HEATMAP_FILE_NAME = "AlignmentHeatmap.pdf";
	public static final String INPUT_FILTERING_PLOT_FILE_NAME = "InputFilteringPlot.pdf";
	public static final String ADDUCT_REPRODUCIBILITY_EXTENDED_FILE_NAME = "AdductReproducibilityEFS.pdf";
	public static final String MERGED_ALIGNED_DATA_EXTENDED_FILE_NAME = "MergedAlignedDataEFS.txt";
	public static final String ALIGNMENT_METADATA_SUFFIX = ".alignmentMetaData";
	public static final String ANCHORS_FILE_NAME_PREFIX = "Anchors-";
	public static final String ALIGNMENT_PLOT_NAME_PREFIX = "AlignmentPlot-";
	public static final String ALIGNMENT_REPORT_NAME_PREFIX = "AlignmentReport-";
	public static final String COMPLETE_ALIGNMENT_REPORT_NAME_PREFIX = "CompleteAlignmentReport-";
	public static final String R_FOLDER_SEPARATOR = "/";
	public static final String ALIGNMENT_SETTINGS_FILE = "MetabCombinerAlignmentSettings.xml";
	public static final String EXPERIMENT_DESIGN_FILE_NAME = "ExperimentDesign.txt";
	public static final String MISSING_PREFILTER_COLUMN = "missing_prefilter";
	public static final String ALIGNMENT_REPORT_FILE_NAME = "MultiBatchMCAlignmentReport";
	
	public static final List<SummaryInputColumns> requiredProperties = 
			Arrays.asList(
					SummaryInputColumns.EXPERIMENT, 
					SummaryInputColumns.BATCH,
					SummaryInputColumns.PEAK_AREAS,
					SummaryInputColumns.MANIFEST);
	
	private MetabCombinerParametersObject parametersObject;
	private Map<RMultibatchAnalysisInputObject,String>metabDataObjectMap;
	
	public enum McAlignmentProjectSubfolders{
		
		RawData,
		Manifests,
		CleanData,
		Anchors,
		Plots,
		AlignmentReports,
		CompleteAlignmentReports,
		;
	}
	
	private Map<String,String>matchListMap;
	private List<String>listParts;
	private Map<RMultibatchAnalysisInputObject,String>overlapObjectMap;
	private Map<RMultibatchAnalysisInputObject,String>unionObjectMap;

	public MetabCombinerAlignmentScriptGenerator(MetabCombinerParametersObject parametersObject) {
		super();
		this.parametersObject = parametersObject;
		rscriptParts = new ArrayList<>();
		metabDataObjectMap = new HashMap<>();
		matchListMap = new TreeMap<>();
		listParts = new ArrayList<>();
		overlapObjectMap = new HashMap<>();
		unionObjectMap = new HashMap<>();
	}
	
	public void createMetabCombinerAlignmentScript() {
		
		if(!parametersObject.isUseExistingAlignment())
			createProjectDirectoryStructure();
		
		initRscript();	
		initSummaryDataFrames();
		createDataImportBlock();
		if(parametersObject.isUseExistingAlignment())
			createExistingAlignmentImportBlock();
		
		createDataAlignmentBlock();
		createStrictMatchingBlock();
		
		if(parametersObject.getMaxMissingBatchCount() > 0)
			createFuzzyMatchingBlock();
		
		createCombinedManifestBlock();
		
		if(parametersObject.isImputeMissingValuesInAlignedData())
			createImputationBlock();
		
		createQCANVASoutputBlock();
		
		createAlignmentHeatMapBlock();
		
		crerateInputFilteringPlotBlock();
		
		createPercentAlignedPlots();
		 
		generateHTMLreport();
			
		rscriptParts.add("\n# End of script ####\n");
		
		writeScriptToFile();
		
		saveAlignmentParameters();
	}

	protected void createProjectDirectoryStructure() {

		File projectParentDir = parametersObject.getProjectParentDirectory();
		String projectName = MC_ALIGNMENT_PROJECT_BASE_NAME + FIOUtils.getTimestamp();
		Path newProjectPath = Paths.get(projectParentDir.getAbsolutePath(), projectName);		
		try {
			Files.createDirectories(newProjectPath);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		projectFolder = newProjectPath.toFile();
		parametersObject.setProjectDirectory(projectFolder);
		
		for(McAlignmentProjectSubfolders folder : McAlignmentProjectSubfolders.values()) {

			Path subDirectoryPath = newProjectPath.resolve(folder.name());
			try {
				Files.createDirectories(subDirectoryPath);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		Path rmdReportTemplatePath = Paths.get(MRC2ToolBoxCore.configDir, ALIGNMENT_REPORT_FILE_NAME + ".Rmd");
        Path rmdReportPath = Paths.get(newProjectPath.toString(), ALIGNMENT_REPORT_FILE_NAME + ".Rmd");
        try {
			Files.copy(rmdReportTemplatePath, rmdReportPath);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		scriptFile = Paths.get(newProjectPath.toString(), 
				SCRIPT_FILE_PREFIX + FIOUtils.getTimestamp() + ".R" ).toFile();
	}

	protected void initRscript() {
				
		rscriptParts.add("# MetabCombiner alignment of multiple batches of untargeted data " + 
				DefaultFormatStore.getDefaultTimeStampFormat().format(new Date())+ " ####\n");
		rscriptParts.add("setwd(dirname(rstudioapi::getActiveDocumentContext()$path))\n");	
		rscriptParts.add("library(metabCombiner)");
		rscriptParts.add("library(dplyr)");
		rscriptParts.add("library(purrr)");
		rscriptParts.add("library(ggplot2)\n");
		
		rscriptParts.add("project_name <- \"" + parametersObject.getProjectTitle() + "\"");
	}
	
	private void createDataImportBlock() {
		
		rscriptParts.add("\n## Read in the data for alignment ####\n");
		
		for(RMultibatchAnalysisInputObject mcio : parametersObject.getMetabCombinerFileInputObjectSet()) {
			
			String dataObjectPrefix = mcio.getExperimentId() + "." + mcio.getBatchId();			
			String dataObject = dataObjectPrefix + ".data";

			rscriptParts.add(dataObject + " <- read.delim(\"." + R_FOLDER_SEPARATOR + McAlignmentProjectSubfolders.RawData.name() +
					R_FOLDER_SEPARATOR + mcio.getDataFile(SummaryInputColumns.PEAK_AREAS).getName() + "\", check.names=FALSE)");

			//    Remove rows with missing RT values
			rscriptParts.add(dataObject + " <- " + dataObject + "[!(" + dataObject + "$rt == \"NaN\"),]");
			
			//	Filter data based on missingness in drift correction and regular samples
			String maxPercentMissingInRegularSamples = 
					Double.toString(parametersObject.getMaxPercentMissingInRegularSamples());
			String maxPercentMissingInDriftCorrSamples = 
					Double.toString(parametersObject.getmaxPercentMissingInDriftCorrSamples());
			
			rscriptParts.add("max_missing_in_samples" + " <- " + maxPercentMissingInRegularSamples);
			rscriptParts.add("max_missing_in_pools" + " <- " + maxPercentMissingInDriftCorrSamples);
		
			String sampleDataObject = dataObject + ".samples";			
			rscriptParts.add(sampleDataObject + " <- " + dataObject + " %>% select(1,contains(\"-S00\"))");
			rscriptParts.add(sampleDataObject + "$pcMissing <- rowMeans(is.na(" + sampleDataObject + "[,-1]) * 100, na.rm = T)");			
			String sampleFeaturesDataObject = sampleDataObject + ".features";
			rscriptParts.add(sampleFeaturesDataObject + " <- " + sampleDataObject + "  %>% filter(pcMissing < " + 
					maxPercentMissingInRegularSamples + ") %>% pull(1) %>% as.list()");
			
			String driftCorrDataObject = dataObject + ".driftcorr";			
			rscriptParts.add(driftCorrDataObject + " <- " + dataObject + " %>% select(1,contains(\"CS00000MP\"))");
			rscriptParts.add(driftCorrDataObject + "$pcMissing <- rowMeans(is.na(" + driftCorrDataObject + "[,-1]) * 100, na.rm = T)");			
			String driftCorrFeaturesDataObject = driftCorrDataObject + ".features";
			rscriptParts.add(driftCorrFeaturesDataObject + " <- " + driftCorrDataObject + "  %>% filter(pcMissing < " + 
					maxPercentMissingInDriftCorrSamples + ") %>% pull(1) %>% as.list()");
			
			String filterFeaturesListObject = dataObjectPrefix + ".filtered.features";
			rscriptParts.add(filterFeaturesListObject +
					" <- intersect(unlist(" + sampleFeaturesDataObject + "), unlist(" + driftCorrFeaturesDataObject + "))");
			String filteredDataObject = dataObject + ".filtered";
			rscriptParts.add(filteredDataObject + " <- " + dataObject + " %>% filter(id %in% " + filterFeaturesListObject + ")");
			
			//	Write out clean data for final join and record in the data frame
			String data4join = McAlignmentProjectSubfolders.CleanData 
					+ R_FOLDER_SEPARATOR + dataObjectPrefix + CLEAN_DATA_FILE_SUFFIX;
			rscriptParts.add("write.table(" + filteredDataObject + "[,-c(2:4)], "
					+ "file = \"" + data4join + "\", quote = F, sep = \"\\t\", na = \"\", row.names = FALSE)");			
						
			String metabDataObject = dataObjectPrefix + ".metabData";			
			String metabDataCommand = 
					metabDataObject + " <- metabData(" + filteredDataObject 
					+ ", samples = \"CS00000MP\""
					+ ", misspc = " + Double.toString(parametersObject.getmaxPercentMissingInDriftCorrSamples())
					+ ", measure = \"" + parametersObject.getPeakAbundanceMeasure().name() + "\"";
			if(parametersObject.getAlignmentRTRange() != null) {
				
				metabDataCommand +=  
					  ", rtmin = " + Double.toString(parametersObject.getAlignmentRTRange().getMin())
					+ ", rtmax = " + Double.toString(parametersObject.getAlignmentRTRange().getMax());
			}			
			metabDataCommand += ", zero = TRUE, duplicate = opts.duplicate())";
			rscriptParts.add(metabDataCommand);
			metabDataObjectMap.put(mcio, metabDataObject);
			
			String statsObject = dataObjectPrefix + ".stats";
			rscriptParts.add(statsObject + " <- as.data.frame(getStats(" + metabDataObject + "))");
			
			rscriptParts.add(statsObject + "$" + MISSING_PREFILTER_COLUMN 
					+ " <- nrow(" + dataObject + ") - rowSums(" + statsObject + "[, -1], na.rm = TRUE)[[1]]");
			rscriptParts.add(statsObject + "$" + SummaryInputColumns.EXPERIMENT.getRName() 
				+ " <- \"" + mcio.getExperimentId() + "\"");
			rscriptParts.add(statsObject + "$" + SummaryInputColumns.BATCH.getRName() 
				+ " <- \"" + mcio.getBatchId() + "\"");
			
			rscriptParts.add("stats.all <- bind_rows(stats.all, " + statsObject + ")");
		}
		//	Write out statistics for all metabData objects
		String statsFileName = INPUT_STATS_FILE_PREFIX + FIOUtils.getTimestamp() + ".txt";
		rscriptParts.add("write.table(stats.all, file = \"" + statsFileName +
				"\", quote = F, sep = \"\\t\", na = \"\", row.names = FALSE)");
	}	

	private void createExistingAlignmentImportBlock() {
		
		rscriptParts.add("## Parse existing alignment data ####");
		rscriptParts.add("### Read alignment summary file ####");
		rscriptParts.add("alignment.summary.df <- read.delim(\"" 
				+ ALIGNMENT_SUMMARY_TABLE_FILE_NAME + "\", check.names=FALSE)");
		
		rscriptParts.add("\n### Read all metadata files ####");
		rscriptParts.add("readMetaData <- function(df) {");
		rscriptParts.add("\tb.one <- df['dsx'][[1]]");
		rscriptParts.add("\tb.two <- df['dsy'][[1]]");
		rscriptParts.add("\t  md.names <- c( b.one, paste(\"mz.\", b.one, sep=\"\"), "
				+ "paste(\"rt.\", b.one, sep=\"\"), paste(\"adductx.\", b.one, sep=\"\"), b.two, "
				+ "paste(\"mz.\", b.two, sep=\"\"), paste(\"rt.\", b.two, sep=\"\"), "
				+ "paste(\"adducty.\", b.two, sep=\"\"))");
		rscriptParts.add("\tdata.report <- read.delim(df['report.file'], check.names=FALSE) "
				+ "%>% select(idx,mzx,rtx,adductx,idy,mzy,rty,adducty)");
		rscriptParts.add("\tassign(df['matched.features'], as.vector(data.report$idx), envir = .GlobalEnv)");
		rscriptParts.add("\tdata.report <-  data.report %>% set_names(md.names)");
		rscriptParts.add("\tassign(df['meta.data'], data.report, envir = .GlobalEnv)");
		rscriptParts.add("}");
		rscriptParts.add("\napply(alignment.summary.df, 1, readMetaData)");
		rscriptParts.add("\n### Recreate feature overlap lists ####");
		rscriptParts.add("batch.list <- alignment.summary.df %>% select(\"dsx\") %>% distinct() %>% pull(dsx)");
		rscriptParts.add("overlap.list.collection <- list()");
		rscriptParts.add("for(batch in batch.list){");
		rscriptParts.add("\tmatch.list.collection <- mget(alignment.summary.df[alignment.summary.df$dsx == batch,]$matched.features)");
		rscriptParts.add("\toverlap.list.collection <- append(overlap.list.collection, list(batch = Reduce(intersect, match.list.collection)))");
		rscriptParts.add("}");
		rscriptParts.add("names(overlap.list.collection) <- batch.list");
	}
	
	private void initSummaryDataFrames() {
		
		rscriptParts.add("alignment.summary.df <- data.frame(dsx = character(), "
				+ "dsy = character(), report.file = character(), meta.data = character(), "
				+ "matched.features = character(), num.matched = integer())");
		rscriptParts.add("overlap.list.collection <- list()");
		rscriptParts.add("stats.all <- data.frame(");
		rscriptParts.add("\texp = character(), batch = character(), input_size = integer(), filtered_by_rt = integer(), ");
		rscriptParts.add("\tfiltered_as_duplicates = integer(), filtered_by_missingness = integer(), final_count = integer(), "
				+ MISSING_PREFILTER_COLUMN + " = integer())");
	}
	
	private void createDataAlignmentBlock() {
		
		//	Create data frame to keep track of alignment results

		for(RMultibatchAnalysisInputObject mcio1 : parametersObject.getMetabCombinerFileInputObjectSet()) {
			
			matchListMap.clear();
			listParts.clear();
			String firstDataSet = mcio1.getExperimentId() + "." + mcio1.getBatchId();
					
			for(RMultibatchAnalysisInputObject mcio2 : parametersObject.getMetabCombinerFileInputObjectSet()) {
				
				if(!mcio1.equals(mcio2)) {
					String matchList = createMetabCombinerAlignmentBlock(mcio1, mcio2);					
					String key = mcio2.getExperimentId() + "." + mcio2.getBatchId();;
					matchListMap.put(key, matchList);
				}
			}
			rscriptParts.add("\n## Find overlap between aligned features for " + firstDataSet + "####");
			String listString = "match.list.collection <- list(";
			for(Entry<String,String>ent : matchListMap.entrySet())
				listParts.add("\"" + ent.getKey() + "\" = " + ent.getValue());
						
			listString += StringUtils.join(listParts, ",") + ")";
			rscriptParts.add(listString);			
			rscriptParts.add(firstDataSet 
					+ ".overlap <- Reduce(intersect, match.list.collection)");	
			overlapObjectMap.put(mcio1, firstDataSet + ".overlap");
			rscriptParts.add(firstDataSet 
					+ ".union <- Reduce(union, match.list.collection)");	
			unionObjectMap.put(mcio1, firstDataSet + ".union");
			rscriptParts.add("rm(match.list.collection)");			
		}
	}
	
	private void createStrictMatchingBlock() {
		
		//	Overlap of features from master batch
		listParts.clear();
		String overlapsListString = "overlap.list.collection <- list(";
		for(Entry<RMultibatchAnalysisInputObject,String>ent : overlapObjectMap.entrySet()) {
			
			String key = ent.getKey().getExperimentId() + "." + ent.getKey().getBatchId();
			listParts.add("\"" + key + "\" = " + ent.getValue());
		}
		overlapsListString += StringUtils.join(listParts, ",") + ")\n";
		rscriptParts.add(overlapsListString);
		
		rscriptParts.add("write.table(alignment.summary.df, file = \"" 
				+ ALIGNMENT_SUMMARY_TABLE_FILE_NAME + "\", "
				+ "quote = F, sep = \"\\t\", na = \"\", row.names = FALSE)");
		
		rscriptParts.add("\n## Find primary batch for alignment ####");
		rscriptParts.add("match.lengths <- sapply(overlap.list.collection, length)");
		rscriptParts.add("primary.batch.name <- names(which.max(match.lengths))[[1]]");
		
		//	Join metadata and calculate median MZ/RT, create common feature names, find most common annotation
		rscriptParts.add("\n## Create cummulative metadata ####");
		rscriptParts.add("meta.data.names.list <- as.vector("
				+ "alignment.summary.df[alignment.summary.df$dsx == primary.batch.name,]$meta.data)");
		rscriptParts.add("meta.data.list <- mget(meta.data.names.list)");
		rscriptParts.add("meta.data.joined <- Reduce(inner_join, meta.data.list)");
		rscriptParts.add("columns.to.remove.from.merged.data <- colnames(meta.data.joined)");		
		rscriptParts.add("meta.data.joined <- meta.data.joined %>%  rowwise() "
				+ "%>%  mutate(mzMedian = median(c_across(starts_with(\"mz\")), na.rm = T))");
		rscriptParts.add("meta.data.joined <- meta.data.joined %>%  rowwise() "
				+ "%>%  mutate(rtMedian = median(c_across(starts_with(\"rt\")), na.rm = T))");
		rscriptParts.add("meta.data.joined <- meta.data.joined %>%  rowwise() "
				+ "%>%  mutate(FeatureID = paste(\"UNK_\", mzMedian, \"_\", rtMedian, sep = \"\"))");		
		rscriptParts.add("meta.data.joined[meta.data.joined == \"[M + H]\"] <- \"[M+H]+\"");
		rscriptParts.add("meta.data.joined[meta.data.joined == \"[M - H]\"] <- \"[M-H]-\"");
		rscriptParts.add("adduct.data <- meta.data.joined %>% select( contains(\"adduct\"))");
		rscriptParts.add("adduct.data.copy <- adduct.data");
		rscriptParts.add("adduct.data$max.frequency <- apply(adduct.data, 1, "
				+ "function(x) max(tabulate(as.factor(x))) / sum(tabulate(as.factor(x))))");
		rscriptParts.add("adduct.data$common.adduct <- apply(adduct.data.copy,1,function(x) names(which.max(table(x))))");
		rscriptParts.add("cum.meta.data.out <- cbind(meta.data.joined, select(adduct.data, c(\"common.adduct\", \"max.frequency\")))");
		rscriptParts.add("write.table(cum.meta.data.out, file = \"" + CUMMULATIVE_METADATA_FILE_NAME + "\", "
				+ "quote = F, sep = \"\\t\", na = \"\", row.names = FALSE)");
		rscriptParts.add("adduct.plot <- ggplot(cum.meta.data.out, aes(max.frequency)) "
				+ "+ geom_bar(color=\"darkblue\", fill=\"lightblue\", alpha=0.5) "
				+ "+ scale_x_binned(show.limits = T) + ggtitle(\"Adduct Reproducibility\")");
		rscriptParts.add("ggsave(\"" + ADDUCT_REPRODUCIBILITY_FILE_NAME 
				+ "\", plot = adduct.plot,  width = 6, height = 6, device = \"pdf\")");
		
		//	Join actual data using best batch and write out resulsts
		rscriptParts.add("\n## Create merged aligned data ####");
		rscriptParts.add("secondary.batch.list <- as.vector("
				+ "alignment.summary.df[alignment.summary.df$dsx == primary.batch.name,]$dsy)");
		rscriptParts.add("meta.data <- meta.data.joined %>% select("
				+ "all_of(c(\"FeatureID\",\"mzMedian\",\"rtMedian\",primary.batch.name,secondary.batch.list)))");
		rscriptParts.add("merged.data <- read.delim(paste(\"" + McAlignmentProjectSubfolders.CleanData 
				+ R_FOLDER_SEPARATOR +"\", primary.batch.name, \"" 
				+ CLEAN_DATA_FILE_SUFFIX + "\", sep = \"\"), check.names=FALSE)");
		rscriptParts.add("colnames(merged.data)[1] <- primary.batch.name");
		rscriptParts.add("merged.data <- inner_join(meta.data.joined, merged.data, by = primary.batch.name)");
		rscriptParts.add("for(sec.batch.name in  secondary.batch.list){");
		rscriptParts.add("\tsec.batch.data <- read.delim(paste(\"" + McAlignmentProjectSubfolders.CleanData 
				+ R_FOLDER_SEPARATOR + "\", sec.batch.name, \"" 
				+ CLEAN_DATA_FILE_SUFFIX + "\", sep = \"\"), check.names=FALSE)");
		rscriptParts.add("\tcolnames(sec.batch.data)[1] <- sec.batch.name");
		rscriptParts.add("\tmerged.data <- inner_join(merged.data, sec.batch.data, by = sec.batch.name)");
		rscriptParts.add("}");
		rscriptParts.add("merged.data <- merged.data %>% select(-any_of(columns.to.remove.from.merged.data))");
		rscriptParts.add("write.table(merged.data, file = \"" + MERGED_ALIGNED_DATA_FILE_NAME + "\", "
				+ "quote = F, sep = \"\\t\", na = \"\", row.names = FALSE)");
	}
	
	private void createFuzzyMatchingBlock() {
		
		rscriptParts.add("\n## Create cummulative metadata for extended alignment (with missing batches allowed) ####\n");
		// Union of features from master batch
		listParts.clear();
		String unionsListString = "union.list.collection <- list(";
		for(Entry<RMultibatchAnalysisInputObject, String> ent : unionObjectMap.entrySet()) {
			
			String key = ent.getKey().getExperimentId() + "." 
					+ ent.getKey().getBatchId();
			listParts.add("\"" + key + "\" = " + ent.getValue());
		}
		unionsListString += StringUtils.join(listParts, ",") + ")\n";
		rscriptParts.add(unionsListString);
		
		rscriptParts.add("match.lengths.union <- sapply(union.list.collection, length)");
		rscriptParts.add("primary.batch.name.union <- names(which.max(match.lengths.union))[[1]]");
		
		rscriptParts.add("meta.data.names.union.list <- as.vector("
				+ "alignment.summary.df[alignment.summary.df$dsx == primary.batch.name.union,]$meta.data)");
		rscriptParts.add("meta.data.union.list <- mget(meta.data.names.union.list)");
		rscriptParts.add("meta.data.union.joined <- Reduce(full_join, meta.data.union.list)");
		rscriptParts.add("meta.data.union.joined.mz <- select(meta.data.union.joined, 1, contains(\"mz\"))");
		rscriptParts.add("meta.data.union.joined.mz$na_count <- apply(meta.data.union.joined.mz, 1, function(x) sum(is.na(x)))");
		
		String missingBatchedCutoff = Integer.toString(parametersObject.getMaxMissingBatchCount() + 1);
		rscriptParts.add("meta.data.union.joined <- meta.data.union.joined.mz %>% select(1, \"na_count\") "
				+ "%>% filter(na_count < " + missingBatchedCutoff + ") %>% left_join(meta.data.union.joined)");
		rscriptParts.add("");
		rscriptParts.add("meta.data.union.joined <- meta.data.union.joined %>%  rowwise() %>%  "
				+ "mutate(mzMedian = median(c_across(starts_with(\"mz\")), na.rm = T))");
		rscriptParts.add("meta.data.union.joined <- meta.data.union.joined %>%  rowwise() %>%  "
				+ "mutate(rtMedian = median(c_across(starts_with(\"rt\")), na.rm = T))");
		rscriptParts.add("meta.data.union.joined <- meta.data.union.joined %>%  rowwise() %>%  "
				+ "mutate(FeatureID = paste(\"UNK_\", mzMedian, \"_\", rtMedian, sep = \"\"))");
		rscriptParts.add("meta.data.union.joined[meta.data.union.joined == \"[M + H]\"] <- \"[M+H]+\"");
		rscriptParts.add("meta.data.union.joined[meta.data.union.joined == \"[M - H]\"] <- \"[M-H]-\"");
		rscriptParts.add("adduct.data.union <- meta.data.union.joined %>% select( contains(\"adduct\"))");
		rscriptParts.add("adduct.data.union.copy <- adduct.data.union");
		rscriptParts.add("adduct.data.union$max.frequency <- apply(adduct.data.union, 1, "
				+ "function(x) max(tabulate(as.factor(x))) / sum(tabulate(as.factor(x))))");
		rscriptParts.add("adduct.data.union$common.adduct <- apply(adduct.data.union.copy,1,"
				+ "function(x) names(which.max(table(x))))");
		rscriptParts.add("cum.meta.data.out.union <- cbind(meta.data.union.joined, select(adduct.data.union, "
				+ "c(\"common.adduct\", \"max.frequency\")))");
		rscriptParts.add("write.table(cum.meta.data.out.union, file = \"" + EXTENDED_CUMMULATIVE_METADATA_FILE_NAME + "\", "
				+ "quote = F, sep = \"\\t\", na = \"\", row.names = FALSE)");
		rscriptParts.add("adduct.plot.union <- ggplot(cum.meta.data.out.union, aes(max.frequency)) "
				+ "+ geom_bar(color=\"darkblue\", fill=\"lightblue\", alpha=0.5) + scale_x_binned(show.limits = T) "
				+ "+ ggtitle(\"Adduct Reproducibility (extended feature set)\")");
		rscriptParts.add("ggsave(\"" + ADDUCT_REPRODUCIBILITY_EXTENDED_FILE_NAME + "\", plot = adduct.plot.union,  "
				+ "width = 6, height = 6, device = \"pdf\")");
		rscriptParts.add("");
		rscriptParts.add("## Create merged aligned data for extended alignment (with missing batches allowed) ####");
		rscriptParts.add("secondary.batch.list.union <- as.vector("
				+ "alignment.summary.df[alignment.summary.df$dsx == primary.batch.name.union,]$dsy)");
		rscriptParts.add("meta.data.union <- meta.data.union.joined %>% "
				+ "select(all_of(c(\"FeatureID\",\"mzMedian\",\"rtMedian\",\"na_count\","
				+ "primary.batch.name.union,secondary.batch.list.union)))");
		rscriptParts.add("merged.data.union <- read.delim(paste(\"" 
				+ McAlignmentProjectSubfolders.CleanData.name() + R_FOLDER_SEPARATOR + "\", primary.batch.name.union, "
				+ "\"" + CLEAN_DATA_FILE_SUFFIX + "\", sep = \"\"), check.names=FALSE)");
		rscriptParts.add("colnames(merged.data.union)[1] <- primary.batch.name.union");
		rscriptParts.add("merged.data.union <- left_join(meta.data.union, merged.data.union, by = primary.batch.name.union)");
		rscriptParts.add("for(sec.batch.name in  secondary.batch.list.union){");
		rscriptParts.add("  sec.batch.data <- read.delim(paste(\""+ McAlignmentProjectSubfolders.CleanData.name() 
			+ R_FOLDER_SEPARATOR + "\", sec.batch.name, \"" + CLEAN_DATA_FILE_SUFFIX + "\", sep = \"\"), check.names=FALSE)");
		rscriptParts.add("  colnames(sec.batch.data)[1] <- sec.batch.name");
		rscriptParts.add("  merged.data.union <- left_join(merged.data.union, sec.batch.data, by = sec.batch.name)");
		rscriptParts.add("}");
		rscriptParts.add("columns.to.remove.from.merged.data.union <- names(overlap.list.collection)");
		rscriptParts.add("merged.data.union <- merged.data.union %>% select(-any_of(columns.to.remove.from.merged.data.union))");
		rscriptParts.add("write.table(merged.data.union, file = \"" + MERGED_ALIGNED_DATA_EXTENDED_FILE_NAME + "\", "
				+ "quote = F, sep = \"\\t\", na = \"\", row.names = FALSE)");
	}

	private String createMetabCombinerAlignmentBlock(
			RMultibatchAnalysisInputObject io,
			RMultibatchAnalysisInputObject io2) {
		
		Set<String>objectsToClear = new TreeSet<>();		
		String outNameSuffix = createOutNameSuffix(io,io2);
			
		rscriptParts.add("\n### Aligning " + outNameSuffix + "####");
		String metabCombinerCommand = 
				"data.combined <- "
				+ "metabCombiner(xdata = " + metabDataObjectMap.get(io) 
				+ ", ydata = " + metabDataObjectMap.get(io2) 
				+ ", binGap = " + Double.toString(parametersObject.getBinGap())
				+ ", rtOrder = " + Boolean.toString(parametersObject.isMcDataSetRtOrderFlag()).toUpperCase()
				+ ", impute = " + Boolean.toString(parametersObject.isImputeMissingData()).toUpperCase()
				+ ", xid = \"d1\", yid = \"d2\")";
				
		rscriptParts.add(metabCombinerCommand);
		objectsToClear.add("data.combined");
		rscriptParts.add("data.report <- combinedTable(data.combined)");
		objectsToClear.add("data.report");
		String selectAnchorsString = 
				"data.combined <- selectAnchors(data.combined, useID = FALSE"
				+ ", windx = " +  Double.toString(parametersObject.getPrimaryDataSetAnchorRtExclusionWindow())
				+ ", windy = " +  Double.toString(parametersObject.getSecondaryDataSetAnchorRtExclusionWindow())
				+ ", tolmz = " +  Double.toString(parametersObject.getAnchorMzTolerance())
				+ ", tolQ = " +  Double.toString(parametersObject.getAnchorRtQuantileTolerance()) + ")";
		rscriptParts.add(selectAnchorsString);

		rscriptParts.add("anchors <- getAnchors(data.combined)");
		objectsToClear.add("anchors");
		String anchorsFileName = McAlignmentProjectSubfolders.Anchors 
				+ R_FOLDER_SEPARATOR + ANCHORS_FILE_NAME_PREFIX + outNameSuffix +  ".txt";
		rscriptParts.add(
				"write.table(anchors"
				+ ", file = \"" + anchorsFileName + "\""
				+ ", quote = F"
				+ ", sep = \"\\t\""
				+ ", na = \"\""
				+ ", row.names = FALSE)");
				
		rscriptParts.add("set.seed(100)");
		String fitGamString = 
				"data.combined <- fit_gam(data.combined"
				+ ", useID = F"
				+ ", k = seq(12, 20, 2)"
				+ ", iterFilter = 2"
				+ ", coef = 2"
				+ ", prop = 0.5"
				+ ", bs = \"bs\""
				+ ", family = \"scat\""
				+ ", weights = 1"
				+ ", method = \"REML\""
				+ ", optimizer = \"newton\")";
		rscriptParts.add(fitGamString);
		
		//	Save plot
		String plotFileName = McAlignmentProjectSubfolders.Plots 
				+ R_FOLDER_SEPARATOR + ALIGNMENT_PLOT_NAME_PREFIX + outNameSuffix +  ".png";
		rscriptParts.add(
				"png(filename = \"" + plotFileName + "\""
				+ ", width = 11"
				+ ", height = 8"
				+ ", units = \"in\""
				+ ",res = 300)");
		
		String ploTitle = "MetabCombiner alignment between " 
				+ io.getExperimentId() + " " + io.getBatchId() + " and "
				+ io2.getExperimentId() + " " + io2.getBatchId();
		String xTitle = io.getExperimentId() + " " + io.getBatchId();
		String yTitle = io2.getExperimentId() + " " + io2.getBatchId();
		rscriptParts.add(
				"plot(data.combined"
				+ ", fit = \"" + parametersObject.getRtFittingModelType().name() + "\""
				+ ", main = \"" + ploTitle + "\""
				+ ", xlab = \"" + xTitle + "\""
				+ ", ylab = \"" + yTitle + "\""
				+ ", pch = 19"
				+ ", lcol = \"red\""
				+ ", pcol = \"black\""
				+ ", outlier = \"s\")");
		rscriptParts.add("dev.off()");
		
		//	Reports
		String calcScoresString = 
				"data.combined <- calcScores(data.combined"
				+ ", A = " + Double.toString(parametersObject.getScoringMZweight())
				+ ", B = " + Double.toString(parametersObject.getScoringRTweight())
				+ ", C = " + Double.toString(parametersObject.getScoringAbundanceWeight())
				+ ", fit = \"" + parametersObject.getRtFittingModelType().name() + "\""
				+ ", useAdduct = " + Boolean.toString(parametersObject.isUseAdductsToAdjustScore()).toUpperCase()
				+ ", usePPM = " + Boolean.toString(parametersObject.isUsePPMforScoringMz()).toUpperCase()
				+ ", groups = NULL)";
		rscriptParts.add(calcScoresString);
		
		String labelRowsString = 
				"data.combined <- labelRows(data.combined"
				+ ", maxRankX = " + Integer.toString(parametersObject.getMaxFeatureRankForPrimaryDataSet())
				+ ", maxRankY = " + Integer.toString(parametersObject.getMaxFeatureRankForSecondaryDataSet())
				+ ", minScore = " + Double.toString(parametersObject.getMinimalAlignmentScore())
				+ ", delta = " + Double.toString(parametersObject.getSubgroupScoreCutoff())
				+ ", maxRTerr = " + Double.toString(parametersObject.getMaxRTerrorForAlignedFeatures())
				+ ", resolveConflicts = " + Boolean.toString(parametersObject.isResolveAlignmentConflictsInOutput()).toUpperCase()
				+ ", rtOrder = " + Boolean.toString(parametersObject.isRtOrderFlagInOutput()).toUpperCase()
				+ ", remove = TRUE"
				+ ")";
		rscriptParts.add(labelRowsString);
		rscriptParts.add("data.report <- combinedTable(data.combined)");
		String reportFileName = McAlignmentProjectSubfolders.AlignmentReports 
				+ R_FOLDER_SEPARATOR + ALIGNMENT_REPORT_NAME_PREFIX + outNameSuffix +  ".txt";		
		rscriptParts.add("write.table(data.report, file = \"" + reportFileName 
				+ "\", quote = F, sep = \"\\t\", na = \"\", row.names = FALSE)");
		
		String xPrefix = io.getExperimentId() + "." + io.getBatchId();
		String yPrefix = io2.getExperimentId() + "." + io2.getBatchId();

		String alignmentMetaDataObject = xPrefix + "." + yPrefix + ALIGNMENT_METADATA_SUFFIX;
		rscriptParts.add(alignmentMetaDataObject + " <- data.report %>% select(idx,mzx,rtx,adductx,idy,mzy,rty,adducty)");
		rscriptParts.add("colnames(" + alignmentMetaDataObject + ") <- "
				+ "c(\"" + xPrefix + "\", \"mz." + xPrefix + "\", \"rt." + xPrefix + "\", \"adductx." + xPrefix + "\", "
				  + "\"" + yPrefix + "\", \"mz." + yPrefix + "\", \"rt." + yPrefix + "\", \"adducty." + yPrefix + "\")");
		
		String firstDataSetMatchedFeatureList = "matched.features." + outNameSuffix.replaceAll("-", ".");
		
		//	Add line to summary data frame
		rscriptParts.add("alignment.summary.df[nrow(alignment.summary.df) + 1,] "
				+ "= list(dsx=\"" + xTitle.replaceAll("\\s+", ".") 
				+ "\", dsy=\"" + yTitle.replaceAll("\\s+", ".") 
				+ "\", report.file = \"" + reportFileName 
				+ "\", meta.data = \"" + alignmentMetaDataObject 
				+ "\", matched.features = \"" + firstDataSetMatchedFeatureList 
				+ "\", num.matched = nrow(data.report))");
				
		rscriptParts.add(firstDataSetMatchedFeatureList + " <- as.vector(data.report$idx)");		
		rscriptParts.add("data.combined <- updateTables(data.combined, xdata = " 
				+ metabDataObjectMap.get(io) + ", ydata = " + metabDataObjectMap.get(io2) + ")");
		rscriptParts.add("data.report <- combinedTable(data.combined)");
		String completeReportFileName = McAlignmentProjectSubfolders.CompleteAlignmentReports 
				+ R_FOLDER_SEPARATOR + COMPLETE_ALIGNMENT_REPORT_NAME_PREFIX + outNameSuffix +  ".txt";
		rscriptParts.add("write.table(data.report, file = \"" + completeReportFileName 
				+ "\", quote = F, sep = \"\\t\", na = \"\", row.names = FALSE)");		

		rscriptParts.add("rm(" + StringUtils.join(objectsToClear, ",") + ")");
				
		return firstDataSetMatchedFeatureList;
	}
	
	private static String createOutNameSuffix(			
			RMultibatchAnalysisInputObject io,
			RMultibatchAnalysisInputObject io2) {
		
		ArrayList<String>outNameParts = new ArrayList<>();
		outNameParts.add(io.getExperimentId());
		outNameParts.add(io.getBatchId());
		outNameParts.add(io2.getExperimentId());
		outNameParts.add(io2.getBatchId());
		
		return StringUtils.join(outNameParts, "-");
	}
	
	private void createImputationBlock() {
		
		rscriptParts.add("\n# Impute missing values in aligned data ####");
		
		String designSelectString = MoTrPACRawDataManifestFields.MOTRPAC_RAW_FILE.getName();
		if(!parametersObject.getFactorsForImputation().isEmpty())
			designSelectString += ", " + StringUtils.join(parametersObject.getFactorsForImputation(), ", ");
		
		String manifestSelectString = MoTrPACRawDataManifestFields.MOTRPAC_RAW_FILE.getName() + ", "
				+ MoTrPACRawDataManifestFields.MOTRPAC_SAMPLE_TYPE.getName() + ", "
				+ MoTrPACRawDataManifestFields.MOTRPAC_BATCH_OVERRIDE.getName();
		
		String factorString = "imputation.design.with.controls$"+ MoTrPACRawDataManifestFields.MOTRPAC_BATCH_OVERRIDE.getName();
		if(!parametersObject.getFactorsForImputation().isEmpty()) {
							
			for (String factor : parametersObject.getFactorsForImputation())
				factorString += ", imputation.design.with.controls$" + factor;
			
			rscriptParts.add("imputation.design.with.controls <- read.delim(\"" + EXPERIMENT_DESIGN_FILE_NAME + "\", check.names=FALSE ) %>% ");
			rscriptParts.add("  select(" + designSelectString + ") %>% ");
			rscriptParts.add("  right_join(manifest_dataset.clean %>% select(" + manifestSelectString + ")) %>% ");
			rscriptParts.add("  filter(!" + MoTrPACRawDataManifestFields.MOTRPAC_SAMPLE_TYPE.getName() + " == \"" + MoTrPACQCSampleType.QC_BLANK.getName() + "\") %>% ");
			rscriptParts.add("  mutate(across(everything(), ~ na_if(., \"\"))) %>%");
			rscriptParts.add("  mutate(across(everything(), ~ coalesce(.x, " + MoTrPACRawDataManifestFields.MOTRPAC_SAMPLE_TYPE.getName() + ")))");
		}
		else {
			factorString += ", imputation.design.with.controls$" + MoTrPACRawDataManifestFields.MOTRPAC_SAMPLE_TYPE.getName();
			rscriptParts.add("imputation.design.with.controls <- manifest_dataset.clean %>% select(" + manifestSelectString + ") %>% ");
			rscriptParts.add("  filter(!" + MoTrPACRawDataManifestFields.MOTRPAC_SAMPLE_TYPE.getName() + " == \"" + MoTrPACQCSampleType.QC_BLANK.getName() + "\")");
		}
		//	factorString += ")";					

		rscriptParts.add("library(bnstruct)");
		rscriptParts.add("data.with.controls_imputed <- knn.impute(");
		rscriptParts.add("  data.matrix(merged.data[,imputation.design.with.controls$" + MoTrPACRawDataManifestFields.MOTRPAC_RAW_FILE.getName() + "]),");
		rscriptParts.add("  k = 10,");
		rscriptParts.add("  cat.var = as.character(" + factorString + "),");
		rscriptParts.add("  to.impute = 1:nrow(merged.data),");
		rscriptParts.add("  using = 1:nrow(merged.data)");
		rscriptParts.add(")");
		rscriptParts.add("blanks <- manifest_dataset.clean %>% filter(" + MoTrPACRawDataManifestFields.MOTRPAC_SAMPLE_TYPE.getName() + 
				" == \"" + MoTrPACQCSampleType.QC_BLANK.getName() + "\") %>% select(raw_file)");
		rscriptParts.add("merged.data.imputed <- cbind(merged.data[,c(1:3)], data.with.controls_imputed, merged.data[,blanks$" + 
				MoTrPACRawDataManifestFields.MOTRPAC_RAW_FILE.getName() + "])");
		rscriptParts.add("write.table(merged.data.imputed, file = \"" + MERGED_ALIGNED_IMPUTED_DATA_FILE_NAME + "\", "
				+ "quote = F, sep = \"\\t\", na = \"\", row.names = FALSE)");
	}	

	private void createQCANVASoutputBlock() {
		
		rscriptParts.add("\n# Format output for QCANVAS ####\n");
		
		rscriptParts.add("qcanvas.header <- data.frame(A1 = character(),");
		rscriptParts.add("\t\tA2 = character(),");
		rscriptParts.add("\t\tA3 = character(),");
		rscriptParts.add("\t\tA4  = character(),");
		rscriptParts.add("\t\tA5  = character(),");
		rscriptParts.add("\t\tA6  = character(),");
		rscriptParts.add("\t\tA7  = character(),");
		rscriptParts.add("\t\tA8  = character(),");
		rscriptParts.add("\t\tA9  = character(),");
		rscriptParts.add("\t\tstringsAsFactors=FALSE)");
		rscriptParts.add("colnames(qcanvas.header) <- c(\"Match Group\",\"# Batches Covered\",\"# Features\",\"Feature\","
				+ "\"Average Monoisotopic M/Z\",\"Average RT\",\"Average Old RT\",\"Median Median Intensity\",\" \")");
		
		//	Without missing batches		
		rscriptParts.add("merged.data.4qcanvas <- merged.data[,-c(1,2)]");
		rscriptParts.add("colnames(merged.data.4qcanvas)[1] <- \"Feature\"");
		rscriptParts.add("combined.4qcanvas <- bind_rows(qcanvas.header, merged.data.4qcanvas)");
		
		String exportFileName = FilenameUtils.getBaseName(MERGED_ALIGNED_DATA_FILE_NAME) + "_4QCANVAS.txt";
		rscriptParts.add("write.table(combined.4qcanvas, file = \"" + exportFileName + "\", quote = F, sep = \"\\t\", na = \"\", row.names = FALSE)");
		
		//	With missing batches
		if(parametersObject.getMaxMissingBatchCount() > 0) {
			
			rscriptParts.add("merged.data.union.4qcanvas <- merged.data.union[,-c(1,2)]");
			rscriptParts.add("colnames(merged.data.union.4qcanvas)[1] <- \"Feature\"");
			rscriptParts.add("combined.union.4qcanvas <- bind_rows(qcanvas.header, merged.data.union.4qcanvas)");
			
			String extendedExportFileName= FilenameUtils.getBaseName(MERGED_ALIGNED_DATA_EXTENDED_FILE_NAME) + "_4QCANVAS.txt";
			rscriptParts.add("write.table(combined.union.4qcanvas, file = \"" + extendedExportFileName + "\", quote = F, sep = \"\\t\", na = \"\", row.names = FALSE)");
		}
		//	With imputed data
		if(parametersObject.isImputeMissingValuesInAlignedData()) {

			rscriptParts.add("merged.data.imputed.4qcanvas <- merged.data.imputed[,-c(1,2)]");
			rscriptParts.add("colnames(merged.data.imputed.4qcanvas)[1] <- \"Feature\"");
			rscriptParts.add("combined.imputed.4qcanvas <- bind_rows(qcanvas.header, merged.data.imputed.4qcanvas)");
			
			String imputedExportFileName= FilenameUtils.getBaseName(MERGED_ALIGNED_IMPUTED_DATA_FILE_NAME) + "_4QCANVAS.txt";
			rscriptParts.add("write.table(combined.imputed.4qcanvas, file = \"" + imputedExportFileName + "\", quote = F, sep = \"\\t\", na = \"\", row.names = FALSE)");
		}
	}

	private void createCombinedManifestBlock() {
		
		//	TODO ensure ms_mode is populated

		rscriptParts.add("\n# Create combined clean manifest ####");
		rscriptParts.add("\n## Read and combine original batch manifests ####\n");
		rscriptParts.add("manifest_file_list <- list.files(path=\"./" + McAlignmentProjectSubfolders.Manifests.name() + "\")");
		rscriptParts.add("manifest_dataset <- data.frame()");
		rscriptParts.add("for (i in 1:length(manifest_file_list)){");
		rscriptParts.add("\tmanifest_file <- paste(\"./Manifests/\", manifest_file_list[i], sep = \"\")");
		rscriptParts.add("\ttemp_data <- read.delim(manifest_file, check.names=F, stringsAsFactors = F)");
		rscriptParts.add("\ttemp_data[] <- lapply(temp_data, function(x) as.character(x))");
		rscriptParts.add("\ttemp_data$batch_override <- paste(\"Batch\", str_pad(i, width = 2, side = \"left\", pad = \"0\"), sep = \"\")");
		rscriptParts.add("\tmanifest_dataset <- bind_rows(manifest_dataset, temp_data)");
		rscriptParts.add("}\n");
		rscriptParts.add("## Remove files not present in aligned data from manifest, "
				+ "Sort manifest by injection time and populate run_order column with incremental numbers ####\n");
		rscriptParts.add("merged.data.files <- data.frame(" + MoTrPACRawDataManifestFields.MOTRPAC_RAW_FILE.getName() + " = colnames(merged.data[-c(1:3)]))");
		rscriptParts.add("manifest_dataset.clean <- left_join(merged.data.files, manifest_dataset) %>% arrange(`Injection time`)");
		rscriptParts.add("manifest_dataset.clean$" + MoTrPACRawDataManifestFields.MOTRPAC_SAMPLE_ORDER.getName() + " <- 1:nrow(manifest_dataset.clean)");
		rscriptParts.add("write.table(manifest_dataset.clean, file = \"MergedCleanManifest.txt\", quote = F, sep = \"\\t\", na = \"\", row.names = FALSE)");
		rscriptParts.add("");
	}
	
	private void crerateInputFilteringPlotBlock() {
		
		rscriptParts.add("mc.input.stats <- stats.all %>% ");
		rscriptParts.add("\tmutate(ExperimentBatch = paste(exp, batch, sep = \"_\")) %>% ");
		rscriptParts.add("\tselect(-c(\"input_size\", \"exp\", \"batch\")) %>%");
		rscriptParts.add("\tas.data.table() %>% data.table::melt(id = c(\"ExperimentBatch\"), variable.name = \"Filter\", value.name = \"Counts\", na.rm = T)");
		rscriptParts.add("mc.input.stats$Filter <- relevel(mc.input.stats$Filter, ref = \"" + MISSING_PREFILTER_COLUMN + "\")");
		rscriptParts.add("");
		rscriptParts.add("input.filtering.plot <- ggplot(mc.input.stats, aes(x = ExperimentBatch, y = Counts, fill = Filter)) +");
		rscriptParts.add("\tgeom_col() + theme(axis.text.x = element_text(angle = 45, hjust = 1))");
		
//		rscriptParts.add("ggsave(\"" + INPUT_FILTERING_PLOT_FILE_NAME 
//				+ "\", plot = input.filtering.plot,  width = 8, height = 6, device = \"pdf\")");
		
		rscriptParts.add("");
	}

	private void createAlignmentHeatMapBlock() {
		
		rscriptParts.add("\n# Create heatamap for the number of features aligned between batches ####");
		rscriptParts.add("alignment.summary.4hm <- alignment.summary.df %>% select(dsx,dsy,num.matched) %>% ");
		rscriptParts.add("	mutate(across(where(is.character), as.factor)) %>% filter(as.numeric(dsx) >= as.numeric(dsy))");
		rscriptParts.add("");
		rscriptParts.add("align.heatmap <- ggplot(alignment.summary.4hm, aes(x = dsx, y = dsy, fill = num.matched)) +");
		rscriptParts.add("	geom_tile(color = \"white\", lwd = 0.5) +");
		rscriptParts.add("	scale_fill_viridis_c() +");
		rscriptParts.add("	theme_minimal() +");
		rscriptParts.add("	labs(title = \"Heatmap of aligned feature numbers between batches\", x = \"First batch\", y = \"Second batch\") +");		
		rscriptParts.add("	theme(axis.text.x = element_text(angle = 45, hjust = 1))");
		
//		rscriptParts.add("ggsave(\"" + ALIGNMENT_HEATMAP_FILE_NAME 
//				+ "\", plot = align.heatmap,  width = 8, height = 6, device = \"pdf\")");
		
		rscriptParts.add("");
	}
	
	private void createPercentAlignedPlots() {
		
		rscriptParts.add("\n# Create % aligned barchart for all batches ####");
		rscriptParts.add("mc.aligned.stats <- stats.all %>% ");
		rscriptParts.add("  mutate(ExperimentBatch = paste(exp, batch, sep = \"_\")) %>% ");
		rscriptParts.add("  mutate(original_count = final_count + missing_prefilter) %>% ");
		rscriptParts.add("  mutate(Aligned = nrow(merged.data) / original_count * 100) %>% ");
		rscriptParts.add("  mutate(Unaligned = 100 - Aligned) %>% ");
		rscriptParts.add("  select(c(\"ExperimentBatch\", \"Unaligned\", \"Aligned\")) %>%");
		rscriptParts.add("  as.data.table() %>% data.table::melt(id = c(\"ExperimentBatch\"), variable.name = \"Subset\", value.name = \"Percent\", na.rm = T)");
		rscriptParts.add("");
		rscriptParts.add("percent.aligned.plot <- ggplot(mc.aligned.stats, aes(x = ExperimentBatch, y = Percent, fill = Subset)) +");
		rscriptParts.add("  geom_col() + theme(axis.text.x = element_text(angle = 45, hjust = 1))");
		rscriptParts.add("");
		rscriptParts.add("mc.aligned.stats.filtered <- stats.all %>% ");
		rscriptParts.add("  mutate(ExperimentBatch = paste(exp, batch, sep = \"_\")) %>% ");
		rscriptParts.add("  mutate(Aligned = nrow(merged.data) / final_count * 100) %>% ");
		rscriptParts.add("  mutate(Unaligned = 100 - Aligned) %>% ");
		rscriptParts.add("  select(c(\"ExperimentBatch\", \"Unaligned\", \"Aligned\")) %>%");
		rscriptParts.add("  as.data.table() %>% data.table::melt(id = c(\"ExperimentBatch\"), variable.name = \"Subset\", value.name = \"Percent\", na.rm = T)");
		rscriptParts.add("");
		rscriptParts.add("percent.aligned.filtered.plot <- ggplot(mc.aligned.stats.filtered, aes(x = ExperimentBatch, y = Percent, fill = Subset)) +");
		rscriptParts.add("  geom_col() + theme(axis.text.x = element_text(angle = 45, hjust = 1))");
		rscriptParts.add("");
	}
	
	private void generateHTMLreport() {

		rscriptParts.add("\n# Generate HTML report ####");
		rscriptParts.add("");
		rscriptParts.add("library(rmarkdown)");
		rscriptParts.add("library(knitr)");
		rscriptParts.add("rmarkdown::render(");
		rscriptParts.add("  input = \"" + ALIGNMENT_REPORT_FILE_NAME + ".Rmd\",");
		rscriptParts.add("  output_file = \"" + ALIGNMENT_REPORT_FILE_NAME + ".html\",");
		rscriptParts.add("  params = list(");
		rscriptParts.add("    project_name = project_name,");
		rscriptParts.add("    input_stats = stats.all,");
		rscriptParts.add("    filter_plot = input.filtering.plot,");
		rscriptParts.add("    alignment_heatmap = align.heatmap,");
		rscriptParts.add("    percent_aligned_plot = percent.aligned.plot,");
		rscriptParts.add("    percent_aligned_filtered_plot = percent.aligned.filtered.plot,");
		rscriptParts.add("    adduct_plot = adduct.plot,");		
		rscriptParts.add("    max_missing_in_pools = max_missing_in_pools,");
		rscriptParts.add("    max_missing_in_samples = max_missing_in_samples");
		rscriptParts.add("  )");
		rscriptParts.add(")");
		rscriptParts.add("");
	}
    

	private void saveAlignmentParameters() {

		File settingsFile = Paths.get(
				projectFolder.getAbsolutePath(), ALIGNMENT_SETTINGS_FILE).toFile();
		Document settingsDocument = new Document();
		
        Element settingsElement = parametersObject.getXmlElement();
        settingsElement.setAttribute("version", "1.0.0.0");
        settingsDocument.addContent(settingsElement);
		
		XmlUtils.writePrettyPrintXMLtoFile(
				settingsDocument, 
				settingsFile);
	}
	
	public File getScriptFile() {
		return scriptFile;
	}
}

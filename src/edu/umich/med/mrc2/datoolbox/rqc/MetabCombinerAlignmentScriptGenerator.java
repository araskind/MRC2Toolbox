/*******************************************************************************
 *
 * (C) Copyright 2018-2025 MRC2 (http://mrc2.umich.edu).
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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;

import edu.umich.med.mrc2.datoolbox.gui.rgen.mcr.MetabCombinerFileInputObject;
import edu.umich.med.mrc2.datoolbox.gui.rgen.mcr.MetabCombinerParametersObject;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.utils.FIOUtils;

public class MetabCombinerAlignmentScriptGenerator {
	
	public static final String SCRIPT_FILE_PREFIX = "MetabCombinerMultyBatchAlignmentScript_";
	public static final String ALIGNMENT_SUMMARY_TABLE_FILE_NAME = "AlignmentSummaryTable.txt";
	public static final String MERGED_ALIGNED_DATA_FILE_NAME = "MergedAlignedData.txt";
	public static final String CLEAN_DATA_FILE_SUFFIX= "-cleanData.txt";
	public static final String INPUT_STATS_FILE_PREFIX = "MetabCombinerMultiAlignmentInputStats_";
	public static final String CUMMULATIVE_METADATA_FILE_NAME = "CummulativeMetaData.txt";
	public static final String EXTENDED_CUMMULATIVE_METADATA_FILE_NAME = "CummulativeMetaDataEFS.txt";
	public static final String ADDUCT_REPRODUCIBILITY_FILE_NAME = "AdductReproducibility.pdf";
	public static final String ADDUCT_REPRODUCIBILITY_EXTENDED_FILE_NAME = "AdductReproducibilityEFS.pdf";
	public static final String ALIGNMENT_METADATA_SUFFIX = ".alignmentMetaData";
	public static final String ANCHORS_FILE_NAME_PREFIX = "Anchors-";
	public static final String ALIGNMENT_PLOT_NAME_PREFIX = "AlignmentPlot-";
	public static final String ALIGNMENT_REPORT_NAME_PREFIX = "AlignmentReport-";
	public static final String COMPLETE_ALIGNMENT_REPORT_NAME_PREFIX = "CompleteAlignmentReport-";
	
	private MetabCombinerParametersObject parametersObject;
	private List<String>rscriptParts;
	private Map<MetabCombinerFileInputObject,String>metabDataObjectMap;
	//	private Map<MetabCombinerFileInputObject,String>statsObjectMap;
	private File scriptFile;
	
	private Map<String,String>matchListMap;
	private List<String>listParts;
	private Map<MetabCombinerFileInputObject,String>overlapObjectMap;
	private Map<MetabCombinerFileInputObject,String>unionObjectMap;

	public MetabCombinerAlignmentScriptGenerator(MetabCombinerParametersObject parametersObject) {
		super();
		this.parametersObject = parametersObject;
		rscriptParts = new ArrayList<>();
		metabDataObjectMap = new HashMap<>();
		//	statsObjectMap = new HashMap<>();
		scriptFile = Paths.get(parametersObject.getWorkDirectory().getAbsolutePath(), 
				SCRIPT_FILE_PREFIX + FIOUtils.getTimestamp() + ".R" ).toFile();
		
		matchListMap = new TreeMap<>();
		listParts = new ArrayList<>();
		overlapObjectMap = new HashMap<>();
		unionObjectMap = new HashMap<>();
	}
	
	public void createMetabCombinerAlignmentScript() {
		
		createAlignmentProjectDirectoryStructure();
		initRscript();
		createDataImportBlock();
		initSummaryDataFrames();
		
		if(parametersObject.isUseExistingAlignment())
			createExistingAlignmentImportBlock();
		
		createDataAlignmentBlock();
		createStrictMatchingBlock();
		
		if(parametersObject.getMaxMissingBatchCount() > 0)
			createFuzzyMatchingBlock();
		
		writeScriptToFile();
	}

	private void createAlignmentProjectDirectoryStructure() {
		// TODO Auto-generated method stub
		
	}

	private void initRscript() {
		
		String workDirForR = parametersObject.getWorkDirectory().getAbsolutePath().replaceAll("\\\\", "/");
		rscriptParts.add("# MetabCombiner alignment of multiple batches of untargeted data " + 
				MRC2ToolBoxConfiguration.defaultTimeStampFormat.format(new Date())+ " ####\n");
		rscriptParts.add("setwd(\"" + workDirForR + "\")\n");
		rscriptParts.add("library(metabCombiner)");
		rscriptParts.add("library(reshape2)");
		rscriptParts.add("library(dplyr)");
		rscriptParts.add("library(purrr)");
		rscriptParts.add("library(ggplot2)\n");
	}
	
	private void createDataImportBlock() {
		
		rscriptParts.add("## Read in the data for alignment ####\n");
		
		for(MetabCombinerFileInputObject mcio : parametersObject.getMetabCombinerFileInputObjectSet()) {
			
			String dataObjectPrefix = mcio.getExperimentId() + "." + mcio.getBatchId();			
			String dataObject = dataObjectPrefix + ".data";
			rscriptParts.add(dataObject + " <- read.delim(\"" + 
					mcio.getDataFile().getName() + "\", check.names=FALSE)");
			
			//	Write out clean data for final join and record in the data frame
			String data4join = dataObjectPrefix + CLEAN_DATA_FILE_SUFFIX;
			rscriptParts.add("write.table(" + dataObject + "[,-c(2:4)], "
					+ "file = \"" + data4join + "\", quote = F, sep = \"\\t\", na = \"\", row.names = FALSE)");			
			rscriptParts.add(dataObject + " <- " + dataObject + "[!(" + dataObject + "$rt == \"NaN\"),]");
			
			String metabDataObject = dataObjectPrefix + ".metabData";			
			String metabDataCommand = 
					metabDataObject + " <- metabData(" + dataObject 
					+ ", samples = \"CS00000MP\""
					+ ", misspc = " + Double.toString(parametersObject.getMaxMissingPercent())
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
			rscriptParts.add(statsObject + "$" + SummaryInputColumns.EXPERIMENT.getRName() 
				+ " <- \"" + mcio.getExperimentId() + "\"");
			rscriptParts.add(statsObject + "$" + SummaryInputColumns.BATCH.getRName() 
				+ " <- \"" + mcio.getBatchId() + "\"");
			rscriptParts.add("stats.all <- bind_rows(stats.all, " + statsObject + ")");
			
			//	statsObjectMap.put(mcio, statsObject);
		}
		//	Write out statistics for all metabData objects
		//	rscriptParts.add("stats.all <- bind_rows(" + StringUtils.join(statsObjectMap.values(), ",") + ")");
		String statsFileName = INPUT_STATS_FILE_PREFIX + FIOUtils.getTimestamp() + ".txt";
		rscriptParts.add("write.table(stats.all, file = \"" + statsFileName +
				"\", quote = F, sep = \"\\t\", na = \"\", row.names = FALSE)");
	}	

	private void createExistingAlignmentImportBlock() {
		
		rscriptParts.add("## Parse existing alignment data ####");
		rscriptParts.add("### Read alignment summary file ####");
		rscriptParts.add("alignment.summary.df <- read.delim(\"" 
				+ ALIGNMENT_SUMMARY_TABLE_FILE_NAME + "\", check.names=FALSE)");
		
//		rscriptParts.add("alignment.summary.df$matched.features <- "
//				+ "paste(\"matched.features.\", alignment.summary.df$dsx, "
//				+ "\".\", alignment.summary.df$dsy, sep = \"\")");
		
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
		rscriptParts.add("\tinput_size = integer(), filtered_by_rt = integer(), filtered_as_duplicates = integer(),");
		rscriptParts.add("\tfiltered_by_missingness = integer(), final_count = integer(), exp = character(), batch = character())");
	}
	
	private void createDataAlignmentBlock() {
		
		//	Create data frame to keep track of alignment results

		for(MetabCombinerFileInputObject mcio1 : parametersObject.getMetabCombinerFileInputObjectSet()) {
			
			matchListMap.clear();
			listParts.clear();
			String firstDataSet = mcio1.getExperimentId() + "." + mcio1.getBatchId();
					
			for(MetabCombinerFileInputObject mcio2 : parametersObject.getMetabCombinerFileInputObjectSet()) {
				
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
		for(Entry<MetabCombinerFileInputObject,String>ent : overlapObjectMap.entrySet()) {
			
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
		rscriptParts.add("merged.data <- read.delim(paste(primary.batch.name, \"" 
				+ CLEAN_DATA_FILE_SUFFIX + "\", sep = \"\"), check.names=FALSE)");
		rscriptParts.add("colnames(merged.data)[1] <- primary.batch.name");
		rscriptParts.add("merged.data <- inner_join(meta.data.joined, merged.data, by = primary.batch.name)");
		rscriptParts.add("for(sec.batch.name in  secondary.batch.list){");
		rscriptParts.add("\tsec.batch.data <- read.delim(paste(sec.batch.name, \"" 
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
		for(Entry<MetabCombinerFileInputObject, String> ent : unionObjectMap.entrySet()) {
			
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
		rscriptParts.add("merged.data.union <- read.delim(paste(primary.batch.name.union, "
				+ "\"" + CLEAN_DATA_FILE_SUFFIX + "\", sep = \"\"), check.names=FALSE)");
		rscriptParts.add("colnames(merged.data.union)[1] <- primary.batch.name.union");
		rscriptParts.add("merged.data.union <- left_join(meta.data.union, merged.data.union, by = primary.batch.name.union)");
		rscriptParts.add("for(sec.batch.name in  secondary.batch.list.union){");
		rscriptParts.add("  sec.batch.data <- read.delim(paste(sec.batch.name, \"-cleanData.txt\", sep = \"\"), check.names=FALSE)");
		rscriptParts.add("  colnames(sec.batch.data)[1] <- sec.batch.name");
		rscriptParts.add("  merged.data.union <- left_join(merged.data.union, sec.batch.data, by = sec.batch.name)");
		rscriptParts.add("}");
		rscriptParts.add("columns.to.remove.from.merged.data.union <- names(overlap.list.collection)");
		rscriptParts.add("merged.data.union <- merged.data.union %>% select(-any_of(columns.to.remove.from.merged.data.union))");
		rscriptParts.add("write.table(merged.data.union, file = \"MergedAlignedDataEFS.txt\", "
				+ "quote = F, sep = \"\\t\", na = \"\", row.names = FALSE)");
	}
	
	private void writeScriptToFile() {

		try {
		    Files.write(scriptFile.toPath(), 
		    		rscriptParts,
		            StandardCharsets.UTF_8,
		            StandardOpenOption.CREATE, 
		            StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
		    e.printStackTrace();
		}
	}
	
	private String createMetabCombinerAlignmentBlock(
			MetabCombinerFileInputObject io,
			MetabCombinerFileInputObject io2) {
		
		Set<String>objectsToClear = new TreeSet<>();		
		String outNameSuffix = createOutNameSuffix(io,io2);
			
		rscriptParts.add("\n### Aligning " + outNameSuffix + "####");
		String metabCombinerCommand = 
				"data.combined <- "
				+ "metabCombiner(xdata = " + metabDataObjectMap.get(io) 
				+ ", ydata = " + metabDataObjectMap.get(io2) 
				+ ", binGap = " + Double.toString(parametersObject.getBinGap())
				+ ", rtOrder = " + Boolean.toString(parametersObject.isMcDataSetRtOrderFlag())
				+ ", impute = " + Boolean.toString(parametersObject.isImputeMissingData())
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
		String anchorsFileName = ANCHORS_FILE_NAME_PREFIX + outNameSuffix +  ".txt";
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
		String plotFileName = ALIGNMENT_PLOT_NAME_PREFIX + outNameSuffix +  ".png";
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
				+ ", useAdduct = " + Boolean.toString(parametersObject.isUseAdductsToAdjustScore())
				+ ", usePPM = " + Boolean.toString(parametersObject.isUsePPMforScoringMz())
				+ ", groups = NULL)";
		rscriptParts.add(calcScoresString);
		
		String labelRowsString = 
				"data.combined <- reduceTable(data.combined"
				+ ", maxRankX = " + Integer.toString(parametersObject.getMaxFeatureRankForPrimaryDataSet())
				+ ", maxRankY = " + Integer.toString(parametersObject.getMaxFeatureRankForSecondaryDataSet())
				+ ", minScore = " + Double.toString(parametersObject.getMinimalAlignmentScore())
				+ ", delta = " + Double.toString(parametersObject.getSubgroupScoreCutoff())
				+ ", maxRTerr = " + Double.toString(parametersObject.getMaxRTerrorForAlignedFeatures())
				+ ", resolveConflicts = " + Boolean.toString(parametersObject.isResolveAlignmentConflictsInOutput())
				+ ", rtOrder = " + Boolean.toString(parametersObject.isRtOrderFlagInOutput())
				+ ")";
		rscriptParts.add(labelRowsString);
		rscriptParts.add("data.report <- combinedTable(data.combined)");
		String reportFileName = ALIGNMENT_REPORT_NAME_PREFIX + outNameSuffix +  ".txt";		
		rscriptParts.add("write.table(data.report, file = \"" + reportFileName 
				+ "\", quote = F, sep = \"\\t\", na = \"\", row.names = FALSE)");
		
		String xPrefix = io.getExperimentId() + "." + io.getBatchId();
		String yPrefix = io2.getExperimentId() + "." + io2.getBatchId();

		String alignmentMetaDataObject = xPrefix + "." + yPrefix + ALIGNMENT_METADATA_SUFFIX;
		rscriptParts.add(alignmentMetaDataObject + " <- data.report %>% select(idx,mzx,rtx,adductx,idy,mzy,rty,adducty)");
		rscriptParts.add("colnames(" + alignmentMetaDataObject + ") <- "
				+ "c(\"" + xPrefix + "\", \"mz." + xPrefix + "\", \"rt." + xPrefix + "\", \"adductx." + xPrefix + "\", "
				  + "\"" + yPrefix + "\", \"mz." + yPrefix + "\", \"rt." + yPrefix + "\", \"adducty." + yPrefix + "\")");
		
		String firstDataSetMatchedFetureList = "matched.features." + outNameSuffix.replaceAll("-", ".");
		
		//	Add line to summary data frame
		rscriptParts.add("alignment.summary.df[nrow(alignment.summary.df) + 1,] "
				+ "= list(dsx=\"" + xTitle.replaceAll("\\s+", ".") 
				+ "\", dsy=\"" + yTitle.replaceAll("\\s+", ".") 
				+ "\", report.file = \"" + reportFileName 
				+ "\", meta.data = \"" + alignmentMetaDataObject 
				+ "\", matched.features = \"" + firstDataSetMatchedFetureList 
				+ "\", num.matched=nrow(data.report))");
				
		rscriptParts.add(firstDataSetMatchedFetureList + " <- as.vector(data.report$idx)");		
		rscriptParts.add("data.combined <- updateTables(data.combined, xdata = " 
				+ metabDataObjectMap.get(io) + ", ydata = " + metabDataObjectMap.get(io2) + ")");
		rscriptParts.add("data.report <- combinedTable(data.combined)");
		String completeReportFileName = COMPLETE_ALIGNMENT_REPORT_NAME_PREFIX + outNameSuffix +  ".txt";
		rscriptParts.add("write.table(data.report, file = \"" + completeReportFileName 
				+ "\", quote = F, sep = \"\\t\", na = \"\", row.names = FALSE)");		

		rscriptParts.add("rm(" + StringUtils.join(objectsToClear, ",") + ")");
				
		return firstDataSetMatchedFetureList;
	}
	
	private static String createOutNameSuffix(			
			MetabCombinerFileInputObject io,
			MetabCombinerFileInputObject io2) {
		
		ArrayList<String>outNameParts = new ArrayList<String>();
		outNameParts.add(io.getExperimentId());
		outNameParts.add(io.getBatchId());
		outNameParts.add(io2.getExperimentId());
		outNameParts.add(io2.getBatchId());
		
		return StringUtils.join(outNameParts, "-");
	}

	public File getScriptFile() {
		return scriptFile;
	}
}

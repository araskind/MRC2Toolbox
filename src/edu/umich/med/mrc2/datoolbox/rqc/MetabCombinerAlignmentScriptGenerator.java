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

import edu.umich.med.mrc2.datoolbox.gui.integration.mcr.MetabCombinerFileInputObject;
import edu.umich.med.mrc2.datoolbox.gui.integration.mcr.MetabCombinerParametersObject;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.utils.FIOUtils;

public class MetabCombinerAlignmentScriptGenerator {

	private MetabCombinerParametersObject parametersObject;
	private List<String>rscriptParts;
	private Map<MetabCombinerFileInputObject,String>metabDataObjectMap;
	private Map<MetabCombinerFileInputObject,String>statsObjectMap;
	private File scriptFile;
	private String cleanDataFileSuffix;
	private Map<String,String>matchListMap;
	private List<String>listParts;
	private Map<MetabCombinerFileInputObject,String>overlapObjectMap;
	private Map<MetabCombinerFileInputObject,String>unionObjectMap;

	public MetabCombinerAlignmentScriptGenerator(MetabCombinerParametersObject parametersObject) {
		super();
		this.parametersObject = parametersObject;
		rscriptParts = new ArrayList<>();
		metabDataObjectMap = new HashMap<>();
		statsObjectMap = new HashMap<>();
		scriptFile = Paths.get(parametersObject.getWorkDirectory().getAbsolutePath(), 
				"MetabCombinerMultyBatchAlignmentScript_" + FIOUtils.getTimestamp() + ".R" ).toFile();
		cleanDataFileSuffix = "-cleanData.txt";
		
		matchListMap = new TreeMap<>();
		listParts = new ArrayList<>();
		overlapObjectMap = new HashMap<>();
		unionObjectMap = new HashMap<>();
	}
	
	public void createMetabCombinerAlignmentScript() {
		
		initRscript();
		createDataImportBlock();
		createDataAlignmentBlock();
		createStrictMatchingBlock();
		
		if(parametersObject.getMaxMissingBatchCount() > 0)
			createFuzzyMatchingBlock();
		
		writeScriptToFile();
	}
	
	private void initRscript() {
		
		String workDirForR = parametersObject.getWorkDirectory().getAbsolutePath().replaceAll("\\\\", "/");
		rscriptParts.add("# MetabCombiner alignment of multiple batches of untargeted data " + 
				MRC2ToolBoxConfiguration.defaultTimeStampFormat.format(new Date())+ " ####\n");
		rscriptParts.add("setwd(\"" + workDirForR + "\")\n");
		rscriptParts.add("library(metabCombiner)");
		rscriptParts.add("library(reshape2)");
		rscriptParts.add("library(dplyr)");
		rscriptParts.add("library(ggplot2)\n");
	}
	
	private void createDataImportBlock() {
		
		rscriptParts.add("## Read in the data for alignment ####\n");
		rscriptParts.add("clean.data.map.df <- data.frame(data.set = character(), file.name = character())");
		
		
		for(MetabCombinerFileInputObject mcio : parametersObject.getMetabCombinerFileInputObjectSet()) {
			
			String dataObjectPrefix = mcio.getExperimentId() + "." + mcio.getBatchId();			
			String dataObject = dataObjectPrefix + ".data";
			rscriptParts.add(dataObject + " <- read.delim(\"" + 
					mcio.getDataFile().getName() + "\", check.names=FALSE)");
			
			//	Write out clean data for final join and record in the data frame
			String data4join = dataObjectPrefix + cleanDataFileSuffix;
			rscriptParts.add("write.table(" + dataObject + "[,-c(2:4)], "
					+ "file = \"" + data4join + "\", quote = F, sep = \"\\t\", na = \"\", row.names = FALSE)");
			rscriptParts.add("clean.data.map.df[nrow(clean.data.map.df) + 1,] "
					+ "= list(data.set = \"" + dataObjectPrefix + "\", file.name = \"" + data4join + "\")"); 
			
			rscriptParts.add(dataObject + " <- " + dataObject + "[!(" + dataObject + "$rt == \"NaN\"),]");
			
			String metabDataObject = dataObjectPrefix + ".metabData";
			
			rscriptParts.add(metabDataObject + " <- metabData(" + dataObject 
					+ ", samples = \"CS00000MP\", measure = \"median\", zero = TRUE, duplicate = opts.duplicate())");
			
			metabDataObjectMap.put(mcio, metabDataObject);
			
			String statsObject = dataObjectPrefix + ".stats";
			rscriptParts.add(statsObject + " <- as.data.frame(getStats(" + metabDataObject + "))");
			rscriptParts.add(statsObject + "$" + SummaryInputColumns.EXPERIMENT.getRName() 
				+ " <- \"" + mcio.getExperimentId() + "\"");
			rscriptParts.add(statsObject + "$" + SummaryInputColumns.BATCH.getRName() 
				+ " <- \"" + mcio.getBatchId() + "\"");
			statsObjectMap.put(mcio, statsObject);
		}
		//	Write out statistics for all metabData objects
		rscriptParts.add("stats.all <- bind_rows(" + StringUtils.join(statsObjectMap.values(), ",") + ")");
		String statsFileName = "MetabCombinerMultiAlignmentInputStats" + FIOUtils.getTimestamp() + ".txt";
		rscriptParts.add("write.table(stats.all, file = \"" + statsFileName +
				"\", quote = F, sep = \"\\t\", na = \"\", row.names = FALSE)");
	}
	
	private void createDataAlignmentBlock() {
		
		//	Create data frame to keep track of alignment results
		rscriptParts.add("alignment.summary.df <- data.frame(dsx = character(), "
				+ "dsy = character(), report.file = character(), "
				+ "meta.data = character(), num.matched = integer())");
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
		
		rscriptParts.add("write.table(alignment.summary.df, file = \"AlignmentSummaryTable.txt\", "
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
		rscriptParts.add("data.out <- cbind(meta.data.joined, select(adduct.data, c(\"common.adduct\", \"max.frequency\")))");
		rscriptParts.add("write.table(data.out, file = \"CummulativeMetaData.txt\", "
				+ "quote = F, sep = \"\\t\", na = \"\", row.names = FALSE)");
		rscriptParts.add("adduct.plot <- ggplot(data.out, aes(max.frequency)) "
				+ "+ geom_bar(color=\"darkblue\", fill=\"lightblue\", alpha=0.5) "
				+ "+ scale_x_binned(show.limits = T) + ggtitle(\"Adduct Reproducibility\")");
		rscriptParts.add("ggsave(\"AdductReproducibility.pdf\", plot = adduct.plot,  width = 6, height = 6, device = \"pdf\")");
		
		//	Join actual data using best batch and write out resulsts
		rscriptParts.add("\n## Create merged aligned data ####");
		rscriptParts.add("secondary.batch.list <- as.vector("
				+ "alignment.summary.df[alignment.summary.df$dsx == primary.batch.name,]$dsy)");
		rscriptParts.add("meta.data <- meta.data.joined %>% select("
				+ "all_of(c(\"FeatureID\",\"mzMedian\",\"rtMedian\",primary.batch.name,secondary.batch.list)))");
		rscriptParts.add("merged.data <- read.delim(paste(primary.batch.name, \"" 
				+ cleanDataFileSuffix + "\", sep = \"\"), check.names=FALSE)");
		rscriptParts.add("colnames(merged.data)[1] <- primary.batch.name");
		rscriptParts.add("merged.data <- inner_join(meta.data.joined, merged.data, by = primary.batch.name)");
		rscriptParts.add("for(sec.batch.name in  secondary.batch.list){");
		rscriptParts.add("\tsec.batch.data <- read.delim(paste(sec.batch.name, \"" 
				+ cleanDataFileSuffix + "\", sep = \"\"), check.names=FALSE)");
		rscriptParts.add("\tcolnames(sec.batch.data)[1] <- sec.batch.name");
		rscriptParts.add("\tmerged.data <- inner_join(merged.data, sec.batch.data, by = sec.batch.name)");
		rscriptParts.add("}");
		rscriptParts.add("merged.data <- merged.data %>% select(-any_of(columns.to.remove.from.merged.data))");
		rscriptParts.add("write.table(merged.data, file = \"MergedAlignedData.txt\", "
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
		rscriptParts.add("data.out.union <- cbind(meta.data.union.joined, select(adduct.data.union, "
				+ "c(\"common.adduct\", \"max.frequency\")))");
		rscriptParts.add("write.table(data.out.union, file = \"CummulativeMetaDataEFS.txt\", "
				+ "quote = F, sep = \"\\t\", na = \"\", row.names = FALSE)");
		rscriptParts.add("adduct.plot.union <- ggplot(data.out.union, aes(max.frequency)) "
				+ "+ geom_bar(color=\"darkblue\", fill=\"lightblue\", alpha=0.5) + scale_x_binned(show.limits = T) "
				+ "+ ggtitle(\"Adduct Reproducibility (extended feature set)\")");
		rscriptParts.add("ggsave(\"AdductReproducibilityEFS.pdf\", plot = adduct.plot.union,  "
				+ "width = 6, height = 6, device = \"pdf\")");
		rscriptParts.add("");
		rscriptParts.add("## Create merged aligned data for extended alignment (with missing batches allowed) ####");
		rscriptParts.add("secondary.batch.list.union <- as.vector("
				+ "alignment.summary.df[alignment.summary.df$dsx == primary.batch.name.union,]$dsy)");
		rscriptParts.add("meta.data.union <- meta.data.union.joined %>% "
				+ "select(all_of(c(\"FeatureID\",\"mzMedian\",\"rtMedian\",\"na_count\","
				+ "primary.batch.name.union,secondary.batch.list.union)))");
		rscriptParts.add("merged.data.union <- read.delim(paste(primary.batch.name.union, "
				+ "\"-cleanData.txt\", sep = \"\"), check.names=FALSE)");
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
		
		Set<String>objectsToClear = new TreeSet<String>();		
		String outNameSuffix = createOutNameSuffix(io,io2);
			
		rscriptParts.add("\n### Aligning " + outNameSuffix + "####");
		rscriptParts.add("data.combined <- metabCombiner(xdata = " 
				+ metabDataObjectMap.get(io) + ", ydata = " + metabDataObjectMap.get(io2) 
				+ ", binGap = 0.005, xid = \"d1\", yid = \"d2\")");
		objectsToClear.add("data.combined");
		rscriptParts.add("data.report <- combinedTable(data.combined)");
		objectsToClear.add("data.report");
		rscriptParts.add("data.combined <- selectAnchors(data.combined, useID = FALSE, "
				+ "windx = 0.03, windy = 0.03, tolmz = 0.003, tolQ = 0.3)");
		rscriptParts.add("anchors <- getAnchors(data.combined)");
		objectsToClear.add("anchors");
		String anchorsFileName = "Anchors-" + outNameSuffix +  ".txt";
		rscriptParts.add("write.table(anchors, file = \"" + anchorsFileName +
				"\", quote = F, sep = \"\\t\", na = \"\", row.names = FALSE)");
				
		rscriptParts.add("set.seed(100)");
		rscriptParts.add("data.combined <- fit_gam(data.combined, "
				+ "useID = F, k = seq(12, 20, 2), iterFilter = 2, coef = 2, "
				+ "prop = 0.5, bs = \"bs\", family = \"scat\", "
				+ "weights = 1, method = \"REML\", optimizer = \"newton\")");
		
		//	Save plot
		String plotFileName = "AlignmentPlot-" + outNameSuffix +  ".png";
		rscriptParts.add("png(filename = \"" + plotFileName 
				+ "\", width = 11, height = 8, units = \"in\",res = 300)");
		
		String ploTitle = "MetabCombiner alignment between " 
				+ io.getExperimentId() + " " + io.getBatchId() + " and "
				+ io2.getExperimentId() + " " + io2.getBatchId();
		String xTitle = io.getExperimentId() + " " + io.getBatchId();
		String yTitle = io2.getExperimentId() + " " + io2.getBatchId();
		rscriptParts.add("plot(data.combined, fit = \"gam\", main = \"" + ploTitle + "\", "
				+ "xlab = \"" + xTitle + "\",  ylab = \"" + yTitle + "\", "
				+ "pch = 19, lcol = \"red\", pcol = \"black\", outlier = \"s\")");
		rscriptParts.add("dev.off()");
		
		//	Reports
		rscriptParts.add("data.combined <- calcScores(data.combined, "
				+ "A = 90, B = 15, C = 0.5, fit = \"gam\", usePPM = FALSE, groups = NULL)");
		rscriptParts.add("data.combined <- reduceTable(data.combined, "
				+ "maxRankX = 2, maxRankY = 2, minScore = 0.5, delta = 0.1)");
		rscriptParts.add("data.report <- combinedTable(data.combined)");
		String reportFileName = "AlignmentReport-" + outNameSuffix +  ".txt";		
		rscriptParts.add("write.table(data.report, file = \"" + reportFileName 
				+ "\", quote = F, sep = \"\\t\", na = \"\", row.names = FALSE)");
		
		String xPrefix = io.getExperimentId() + "." + io.getBatchId();
		String yPrefix = io2.getExperimentId() + "." + io2.getBatchId();
		
		String alignmentMetaDataObject = xPrefix + "." + yPrefix + "alignmentMetaData";
		rscriptParts.add(alignmentMetaDataObject + " <- data.report %>% select(idx,mzx,rtx,adductx,idy,mzy,rty,adducty)");
		rscriptParts.add("colnames(" + alignmentMetaDataObject + ") <- "
				+ "c(\"" + xPrefix + "\", \"mz." + xPrefix + "\", \"rt." + xPrefix + "\", \"adductx." + xPrefix + "\", "
				  + "\"" + yPrefix + "\", \"mz." + yPrefix + "\", \"rt." + yPrefix + "\", \"adducty." + yPrefix + "\")");
		
		//	Add line to summary data frame
		rscriptParts.add("alignment.summary.df[nrow(alignment.summary.df) + 1,] "
				+ "= list(dsx=\"" + xTitle.replaceAll("\\s+", ".") 
				+ "\", dsy=\"" + yTitle.replaceAll("\\s+", ".") 
				+ "\", report.file = \"" + reportFileName 
				+ "\", meta.data = \"" + alignmentMetaDataObject 
				+ "\", num.matched=nrow(data.report))");
		
		String firstDataSetMatchedFetureList = "matched.features." + outNameSuffix.replaceAll("-", ".");		
		rscriptParts.add(firstDataSetMatchedFetureList + " <- as.vector(data.report$idx)");
		
		rscriptParts.add("data.combined <- updateTables(data.combined, xdata = " 
				+ metabDataObjectMap.get(io) + ", ydata = " + metabDataObjectMap.get(io2) + ")");
		rscriptParts.add("data.report <- combinedTable(data.combined)");
		String completeReportFileName = "CompleteAlignmentReport-" + outNameSuffix +  ".txt";
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

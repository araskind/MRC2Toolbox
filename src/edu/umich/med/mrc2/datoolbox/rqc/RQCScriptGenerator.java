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

package edu.umich.med.mrc2.datoolbox.rqc;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;

import edu.umich.med.mrc2.datoolbox.data.enums.BinnerExportFields;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.utils.DelimitedTextParser;
import edu.umich.med.mrc2.datoolbox.utils.FIOUtils;

public class RQCScriptGenerator {
	
	public static void generateMultiBatchMetabCombinerAlignmentScriptScript(
			File rWorkingDir,
			File inputMap) {
		generateMultiBatchMetabCombinerAlignmentScriptScript(rWorkingDir, inputMap, 0);
	}
	
	public static void generateMultiBatchMetabCombinerAlignmentScriptScript(
			File rWorkingDir,
			File inputMap,
			int numMissingBatchesAllowed) {
		
		//	List<String>qcSummaryNames = new ArrayList<String>();
		List<String>rscriptParts = new ArrayList<String>();
		String workDirForR = rWorkingDir.getAbsolutePath().replaceAll("\\\\", "/");
		String[][] inputMapData = DelimitedTextParser.parseTextFile(
				inputMap, MRC2ToolBoxConfiguration.getTabDelimiter());
		
		SummaryInputColumns[]requiredColumns = new SummaryInputColumns[] {
				SummaryInputColumns.EXPERIMENT,
				SummaryInputColumns.BATCH,
				SummaryInputColumns.PEAK_AREAS,
		};
		List<SummarizationDataInputObject>inputObjectList = 
				getDataInputList(inputMapData, requiredColumns);
		
		if(inputObjectList == null) {
			System.err.println("Unable to parse input map file!");
			return;
		}
		rscriptParts.add("# MetabCombiner alignment of multiple batches of untargeted data ####\n");
		rscriptParts.add("setwd(\"" + workDirForR + "\")\n");
		rscriptParts.add("library(metabCombiner)");
		rscriptParts.add("library(reshape2)");
		rscriptParts.add("library(dplyr)");
		rscriptParts.add("library(ggplot2)");
		rscriptParts.add("## Read in the data for alignment ####\n");
		rscriptParts.add("clean.data.map.df <- data.frame(data.set = character(), file.name = character())");
		
		Map<SummarizationDataInputObject,String>metabDataObjectMap = 
				new HashMap<SummarizationDataInputObject,String>();
		Map<SummarizationDataInputObject,String>statsObjectMap = 
				new HashMap<SummarizationDataInputObject,String>();
		
		String columnListToExclude = getBinnerColumnListToExclude();
		String cleanDataFileSuffix = "-cleanData.txt";
		for(SummarizationDataInputObject io : inputObjectList) {
			
			String dataObjectPrefix = io.getField(SummaryInputColumns.EXPERIMENT) 
					+ "." + io.getField(SummaryInputColumns.BATCH);
			
			String dataObject = dataObjectPrefix + ".data";
			rscriptParts.add(dataObject + " <- read.delim(\"" + 
					io.getField(SummaryInputColumns.PEAK_AREAS) + "\", check.names=FALSE)");
			
//							+ " %>% select(-any_of(c(" + columnListToExclude + ")))");
//			rscriptParts.add("names(" + dataObject + ")[names(" + dataObject + ") == \"" 
//					+ BinnerExportFields.FEATURE_NAME.getName() + "\"] <- \"feature\"");
//			rscriptParts.add("names(" + dataObject + ")[names(" + dataObject + ") == \"" 
//					+ BinnerExportFields.RT_OBSERVED.getName() + "\"] <- \"rt\"");
//			rscriptParts.add("names(" + dataObject + ")[names(" + dataObject + ") == \"" 
//					+ BinnerExportFields.MZ.getName() + "\"] <- \"mz\"");
			
			//	Write out clean data for final join and record in the data frame
			String data4join = dataObjectPrefix + cleanDataFileSuffix;
			rscriptParts.add("write.table(" + dataObject + "[,-c(2:4)], "
					+ "file = \"" + data4join + "\", quote = F, sep = \"\\t\", na = \"\", row.names = FALSE)");
			rscriptParts.add("clean.data.map.df[nrow(clean.data.map.df) + 1,] "
					+ "= list(data.set = \"" + dataObjectPrefix + "\", file.name = \"" + data4join + "\")"); 
			
//			rscriptParts.add(dataObject + " <- " + dataObject  
//					+ " %>% select(feature, mz, rt, contains(\"CS00000MP\"))");	
			
			rscriptParts.add(dataObject + " <- " + dataObject + "[!(" + dataObject + "$rt == \"NaN\"),]");
			
			String metabDataObject = dataObjectPrefix + ".metabData";
//			rscriptParts.add(metabDataObject + " <- metabData(" + dataObject 
//					+ ", id = \"feature\", measure = \"median\", zero = TRUE, duplicate = opts.duplicate())");
			
			rscriptParts.add(metabDataObject + " <- metabData(" + dataObject 
					+ ", samples = \"CS00000MP\", measure = \"median\", zero = TRUE, duplicate = opts.duplicate())");
			
			metabDataObjectMap.put(io, metabDataObject);
			
			String statsObject = dataObjectPrefix + ".stats";
			rscriptParts.add(statsObject + " <- as.data.frame(getStats(" + metabDataObject + "))");
			rscriptParts.add(statsObject + "$" + SummaryInputColumns.EXPERIMENT.getRName() + " <- \"" 
					+ io.getField(SummaryInputColumns.EXPERIMENT) + "\"");
			rscriptParts.add(statsObject + "$" + SummaryInputColumns.BATCH.getRName() + " <- \"" 
					+ io.getField(SummaryInputColumns.BATCH) + "\"");
			statsObjectMap.put(io, statsObject);
		}
		rscriptParts.add("stats.all <- bind_rows(" + StringUtils.join(statsObjectMap.values(), ",") + ")");
		String statsFileName = "MetabCombinerMultiAlignmentInputStats" + FIOUtils.getTimestamp() + ".txt";
		rscriptParts.add("write.table(stats.all, file = \"" + statsFileName +
				"\", quote = F, sep = \"\\t\", na = \"\", row.names = FALSE)");
			
		//	Align all to all and save results
		Map<String,String>matchListMap = new TreeMap<>();
		ArrayList<String>listParts = new ArrayList<>();
		Map<SummarizationDataInputObject,String>overlapObjectMap = new HashMap<>();
		Map<SummarizationDataInputObject,String>unionObjectMap = new HashMap<>();
		
		//	Create data frame to keep track of alignment results
		rscriptParts.add("alignment.summary.df <- data.frame(dsx = character(), "
				+ "dsy = character(), report.file = character(), "
				+ "meta.data = character(), num.matched = integer())");
		for(SummarizationDataInputObject io : inputObjectList) {
			
			matchListMap.clear();
			listParts.clear();
			String firstDataSet = io.getField(SummaryInputColumns.EXPERIMENT) + "." 
					+ io.getField(SummaryInputColumns.BATCH);
					
			for(SummarizationDataInputObject io2 : inputObjectList) {
				
				if(!io.equals(io2)) {
					String matchList = createMetabCombinerAlignmentBlock(
							io, io2, metabDataObjectMap, rscriptParts);
					
					String key = io2.getField(SummaryInputColumns.EXPERIMENT) + "." 
						+ io2.getField(SummaryInputColumns.BATCH);
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
			overlapObjectMap.put(io, firstDataSet + ".overlap");
			rscriptParts.add(firstDataSet 
					+ ".union <- Reduce(union, match.list.collection)");	
			unionObjectMap.put(io, firstDataSet + ".union");
			rscriptParts.add("rm(match.list.collection)");			
		}
		//	Overlap of features from master batch
		listParts.clear();
		String overlapsListString = "overlap.list.collection <- list(";
		for(Entry<SummarizationDataInputObject,String>ent : overlapObjectMap.entrySet()) {
			
			String key = ent.getKey().getField(SummaryInputColumns.EXPERIMENT) + "." 
					+ ent.getKey().getField(SummaryInputColumns.BATCH);
			listParts.add("\"" + key + "\" = " + ent.getValue());
		}
		overlapsListString += StringUtils.join(listParts, ",") + ")\n";
		rscriptParts.add(overlapsListString);
		
		rscriptParts.add("write.table(alignment.summary.df, file = \"AlignmentSummaryTable.txt\", "
				+ "quote = F, sep = \"\\t\", na = \"\", row.names = FALSE)");
		
		//	Find the longest overlap
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
		
		/*
		 *	Data with missing batches allowed		
		 */
		if(numMissingBatchesAllowed > 0) {
			
			rscriptParts.add("\n## Create cummulative metadata for extended alignment (with missing batches allowed) ####\n");
			// Union of features from master batch
			listParts.clear();
			String unionsListString = "union.list.collection <- list(";
			for(Entry<SummarizationDataInputObject,String>ent : unionObjectMap.entrySet()) {
				
				String key = ent.getKey().getField(SummaryInputColumns.EXPERIMENT) + "." 
						+ ent.getKey().getField(SummaryInputColumns.BATCH);
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
			
			String missingBatchedCutoff = Integer.toString(numMissingBatchesAllowed + 1);
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

		
		String rScriptFileName = "MetabCombinerMultiAlignment-" + FIOUtils.getTimestamp() + ".R";
		Path outputPath = Paths.get(
				rWorkingDir.getAbsolutePath(), rScriptFileName);
		try {
		    Files.write(outputPath, 
		    		rscriptParts,
		            StandardCharsets.UTF_8,
		            StandardOpenOption.CREATE, 
		            StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
		    e.printStackTrace();
		}
	}
	
	private static String createMetabCombinerAlignmentBlock(
			SummarizationDataInputObject io,
			SummarizationDataInputObject io2, 
			Map<SummarizationDataInputObject,String>metabDataObjectMap,
			List<String> rscriptParts) {
		
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
				+ io.getField(SummaryInputColumns.EXPERIMENT) + " " 
				+ io.getField(SummaryInputColumns.BATCH) + " and "
				+ io2.getField(SummaryInputColumns.EXPERIMENT) + " " 
				+ io2.getField(SummaryInputColumns.BATCH);
		String xTitle = io.getField(SummaryInputColumns.EXPERIMENT) + " " 
				+ io.getField(SummaryInputColumns.BATCH);
		String yTitle = io2.getField(SummaryInputColumns.EXPERIMENT) + " " 
				+ io2.getField(SummaryInputColumns.BATCH);
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
		
		String xPrefix = io.getField(SummaryInputColumns.EXPERIMENT) + "." 
				+ io.getField(SummaryInputColumns.BATCH);
		String yPrefix = io2.getField(SummaryInputColumns.EXPERIMENT) + "." 
				+ io2.getField(SummaryInputColumns.BATCH);
		
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
	
	private static void createMetabCombinerAdductEvaluationBlock() {
		
		
	}
	
	private static String createOutNameSuffix(			
			SummarizationDataInputObject io,
			SummarizationDataInputObject io2) {
		
		ArrayList<String>outNameParts = new ArrayList<String>();
		outNameParts.add(io.getField(SummaryInputColumns.EXPERIMENT));
		outNameParts.add(io.getField(SummaryInputColumns.BATCH));
		outNameParts.add(io2.getField(SummaryInputColumns.EXPERIMENT));
		outNameParts.add(io2.getField(SummaryInputColumns.BATCH));
		
		return StringUtils.join(outNameParts, "-");
	}

	private static String getBinnerColumnListToExclude(){
		
		List<String>columnsToExclude = 
				Arrays.asList(BinnerExportFields.values()).stream().
				map(v -> v.getName()).collect(Collectors.toList());
		columnsToExclude.remove(BinnerExportFields.FEATURE_NAME.getName());
		columnsToExclude.remove(BinnerExportFields.RT_OBSERVED.getName());
		columnsToExclude.remove(BinnerExportFields.MZ.getName());
		
		String columnListToExclude = "\"" + StringUtils.join(columnsToExclude, "\",\"") + "\"";	
		return columnListToExclude;
	}
	
	public static void generateSummaryQcScript(
			String experimentId,
			File rWorkingDir,
			File xlQCfile,
			File inputMap,
			String assayType,
			String assayType4R) {
		
		List<String>qcSummaryNames = new ArrayList<String>();
		List<String>rscriptParts = new ArrayList<String>();
		String workDirForR = rWorkingDir.getAbsolutePath().replaceAll("\\\\", "/");
		String xlFilePath4R = xlQCfile.getAbsolutePath().replaceAll("\\\\", "/");
		String[][] methodListData = DelimitedTextParser.parseTextFile(
				inputMap, MRC2ToolBoxConfiguration.getTabDelimiter());
		
		rscriptParts.add("# " + experimentId + " " + assayType + " summary QC ####\n");
		rscriptParts.add("setwd(\"" + workDirForR + "\")\n");
		rscriptParts.add("library(ggplot2)");
		rscriptParts.add("library(reshape2)");
		rscriptParts.add("library(dplyr)");
		rscriptParts.add("library(readxl)\n");	
		rscriptParts.add("## Feature count and total area summaries ####\n");
		
		for(int i=1; i<=methodListData.length; i++) {
			
			String baseName = assayType4R + ".qcSummary.B" + i;
			rscriptParts.add("\n## " + methodListData[i-1][2] + " ####\n");
			rscriptParts.add(baseName + " <- read_excel(\"" + 
					xlFilePath4R + " \", sheet = \"" + methodListData[i-1][1] + "\")");
			rscriptParts.add(baseName + ".design.in <- read.delim(\"" + 
					methodListData[i-1][0] + "\", check.names=FALSE)");
			rscriptParts.add(baseName + ".design <- as.data.frame(" + 
					baseName + ".design.in[,c(5,2)])");
			rscriptParts.add("colnames(" + baseName + ".design) <- c(\"Data file\",\"sample_type\")");
			rscriptParts.add(baseName + ".design$batch <- \"" + methodListData[i-1][2] + "\"");
			rscriptParts.add(baseName + ".des <- merge(" + 
					baseName + ".design, " + baseName + ", by = \"Data file\", all = T)");
			
			qcSummaryNames.add(baseName + ".des");
		}
		rscriptParts.add("\n## Summary plots ####\n");
		
		String masterName = assayType4R + ".QC.summary";		
		rscriptParts.add(masterName + " <- rbind(" + StringUtils.join(qcSummaryNames, ",") + ")");
		rscriptParts.add(masterName + "$sample_type <- as.factor(" + masterName + "$sample_type)");
		rscriptParts.add(masterName + "$batch <- as.factor(" + masterName + "$batch)");
		rscriptParts.add(masterName +".melt <- melt(" + masterName + 
				", id.vars = c(\"sample_type\",\"batch\"), measure.vars = c(\"Observations\",\"Total area\"))");		
		String pivotName = assayType4R + ".QC.pivot";
		rscriptParts.add(pivotName + " <- " + masterName + ".melt %>% group_by(sample_type, batch, variable) "
				+ "%>% summarise(medianVal = median(value, na.rm = T), "
				+ "meanVal = mean(value, na.rm = T), stDev = sd(value, na.rm = T))");
		rscriptParts.add(pivotName + "$RSD <- " + pivotName + "$stDev / " + pivotName + "$meanVal * 100");
		rscriptParts.add(pivotName + "$sample_type <- as.factor(" + pivotName + "$sample_type)");
		rscriptParts.add(pivotName + "$batch <- as.factor(" + pivotName + "$batch)\n");
		rscriptParts.add(pivotName + ".counts <- " + pivotName + "[(" + pivotName + "$variable %in% c(\"Observations\")),]\n");
		
		rscriptParts.add("featureCountsPlot <- ggplot(" + pivotName + ".counts, aes(x = sample_type, y = medianVal, fill = batch))+");
		rscriptParts.add("\tgeom_col( position = \"dodge\", width = 0.5, alpha = 0.7, color = \"black\", linewidth = 0.1)+");
		rscriptParts.add("\t geom_errorbar(aes(ymin = medianVal-stDev, ymax = medianVal+stDev), "
				+ "position =  position_dodge(width = 0.5), width = 0.2)+");
		rscriptParts.add("ggtitle(\"" + experimentId + " " + assayType + " median feature counts by sample type / batch\")");
		rscriptParts.add("ggsave(\"" + experimentId + "-" + assayType + "-featureCounts.png\","
				+ " plot = featureCountsPlot,  width = 14, height = 8.5)\n");

		rscriptParts.add(pivotName + ".areas <- " + pivotName + "[(" + pivotName + "$variable %in% c(\"Total area\")),]");
		rscriptParts.add("areasPlot <- ggplot(" + pivotName + ".areas, aes(x = sample_type, y = medianVal, fill = batch))+");
		rscriptParts.add("geom_col( position = \"dodge\", width = 0.5, alpha = 0.7, color = \"black\", linewidth = 0.1)+");
		rscriptParts.add("geom_errorbar(aes(ymin = medianVal-stDev, ymax = medianVal+stDev), "
				+ "position =  position_dodge(width = 0.5), width = 0.2)+");
		rscriptParts.add("ggtitle(\"" + experimentId + " " + assayType + " median total areas by sample type / batch\")");
		rscriptParts.add("ggsave(\"" + experimentId + "-" + assayType + "-totalAreas.png\","
				+ " plot = areasPlot,  width = 14, height = 8.5)\n");
		
		String rScriptFileName = experimentId + "-" + assayType + 
				"-SummaryQC-" + FIOUtils.getTimestamp() + ".R";
		Path outputPath = Paths.get(
				rWorkingDir.getAbsolutePath(), rScriptFileName);
		try {
		    Files.write(outputPath, 
		    		rscriptParts,
		            StandardCharsets.UTF_8,
		            StandardOpenOption.CREATE, 
		            StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
		    e.printStackTrace();
		}
	}	
	
	private static List<SummarizationDataInputObject>getDataInputList(
			String[][]inputMap, SummaryInputColumns[]requiredColumns){
		
		List<String>header = Arrays.asList(inputMap[0]);
		for(SummaryInputColumns field : requiredColumns) {
			
			if(!header.contains(field.name())) {
				
				System.err.println(field.name() + " missing in data input map file!");
				return null;
			}
		}
		List<SummarizationDataInputObject>dataInputList = 
				new ArrayList<SummarizationDataInputObject>();
		for(int i = 1; i<inputMap.length; i++) {
			
			SummarizationDataInputObject io = new SummarizationDataInputObject();
			for(int j = 0; j<inputMap[0].length; j++) {
			
				SummaryInputColumns field = SummaryInputColumns.getOptionByName(inputMap[0][j]);
				String value = inputMap[i][j];
				if(value == null || value.isEmpty()) {
					
					System.err.println(field.name() + 
							" missing in data input map file on line " + Integer.toString(i+ 1) + "!");
					return null;
				}					
				io.setField(field, value);			
			}
			dataInputList.add(io);			
		}
		return dataInputList;
	}
	
	private static Map<SummaryInputColumns,Set<String>>createFeildVariationMap(
			Collection<SummarizationDataInputObject>dataInputList){
		
		Map<SummaryInputColumns,Set<String>>feildVariationMap = 
				new TreeMap<SummaryInputColumns,Set<String>>();
		for(SummaryInputColumns field : SummaryInputColumns.values()) {
			
			if(field.isFactor()) {
				
				Set<String>fieldValues = new TreeSet<String>();
				for(SummarizationDataInputObject io : dataInputList) {
					
					String value = io.getField(field);
					if(io != null)
						fieldValues.add(value);
				}
				feildVariationMap.put(field, fieldValues);
			}
		}
		return feildVariationMap;
	}
	
	public static void createMultyBatchDataSummarizationScript(File inputMapFile, File dataDir) {

		List<String>rscriptParts = new ArrayList<String>();
		String workDirForR = inputMapFile.getParentFile().getAbsolutePath().replaceAll("\\\\", "/");
		String dataPathPrefix = dataDir.getAbsolutePath().replaceAll("\\\\", "/") + "/";
		String[][] inputData = DelimitedTextParser.parseTextFile(
				inputMapFile, MRC2ToolBoxConfiguration.getTabDelimiter());
		
		List<SummarizationDataInputObject>dataInputList = 
				getDataInputList(inputData, SummaryInputColumns.values());
		if(dataInputList == null)
			return;
		
		Map<SummaryInputColumns,Set<String>>feildVariationMap = 
				createFeildVariationMap(dataInputList);
		
		List<SummaryInputColumns>nonRedundantFields = 
				new ArrayList<SummaryInputColumns>();
		feildVariationMap.entrySet().stream().
			filter(e -> e.getValue().size() > 1).
			forEach(e -> nonRedundantFields.add(e.getKey()));
		List<SummaryInputColumns>commonFields = 
				new ArrayList<SummaryInputColumns>();
		feildVariationMap.entrySet().stream().
			filter(e -> e.getValue().size() == 1).
			forEach(e -> commonFields.add(e.getKey()));
		
		String prefix = "";
		ArrayList<String>prefixParts = new ArrayList<String>();
		ArrayList<String>nonRedundantParts = new ArrayList<String>();		
		
		if(!commonFields.isEmpty()) {
			
			for(SummaryInputColumns cf : commonFields)
				prefixParts.add(dataInputList.get(0).getField(cf));
			
			prefix = StringUtils.join(prefixParts, "_");
		}	
		rscriptParts.add("# Untargeted data summarization script " + FIOUtils.getTimestamp() + "####\n");
		rscriptParts.add("setwd(\"" + workDirForR + "\")\n");
		rscriptParts.add("library(ggplot2)");
		rscriptParts.add("library(reshape2)");
		rscriptParts.add("library(dplyr)");
		
		Map<SummaryInputColumns,List<String>>mergeComponentsMap = 
				new TreeMap<SummaryInputColumns,List<String>>();
		mergeComponentsMap.put(SummaryInputColumns.PEAK_AREAS, new ArrayList<String>());
		mergeComponentsMap.put(SummaryInputColumns.PEAK_QUALITY, new ArrayList<String>());
		mergeComponentsMap.put(SummaryInputColumns.MZ_VALUES, new ArrayList<String>());
		mergeComponentsMap.put(SummaryInputColumns.RT_VALUES, new ArrayList<String>());
		mergeComponentsMap.put(SummaryInputColumns.PEAK_WIDTH, new ArrayList<String>());
		
		for(SummarizationDataInputObject io : dataInputList) {
			
			nonRedundantParts.clear();
			nonRedundantParts.addAll(prefixParts);
			for(SummaryInputColumns nrs : nonRedundantFields)
				nonRedundantParts.add(io.getField(nrs));
			
			rscriptParts.add("\n## Analysis for" + StringUtils.join(nonRedundantParts, ", ") +" ####\n");
			String dataLinePrefix = StringUtils.join(nonRedundantParts, ".");
			String designObject = dataLinePrefix + ".design";
			
			//	Read in design
			rscriptParts.add("### Read common manifest file ####\n");
			rscriptParts.add(designObject + ".in <- read.delim(\"" + dataPathPrefix +
					io.getField(SummaryInputColumns.MANIFEST) + "\", check.names=FALSE)");
			rscriptParts.add(designObject + " <- as.data.frame(" + designObject + ".in[,2])");
			rscriptParts.add("rownames(" + designObject + ") <- "+ designObject + ".in$`raw_file`");
			rscriptParts.add("colnames(" + designObject + ")[1] <- \"sample_type\"");
			rscriptParts.add(designObject + "$sample_type <- as.factor("+ designObject + "$sample_type)");
			rscriptParts.add("rm(" + designObject + ".in)");
			
			String summaryObject = createParameterSummaryBlock(
					rscriptParts, dataLinePrefix, designObject,
					nonRedundantFields, io, SummaryInputColumns.PEAK_AREAS, dataPathPrefix, true);
			mergeComponentsMap.get(SummaryInputColumns.PEAK_AREAS).add(summaryObject);
			
			summaryObject = createParameterSummaryBlock(
					rscriptParts, dataLinePrefix, designObject,
					nonRedundantFields, io, SummaryInputColumns.PEAK_QUALITY, dataPathPrefix, true);
			mergeComponentsMap.get(SummaryInputColumns.PEAK_QUALITY).add(summaryObject);
			
			summaryObject = createParameterSummaryBlock(
					rscriptParts, dataLinePrefix, designObject,
					nonRedundantFields, io, SummaryInputColumns.MZ_VALUES, dataPathPrefix, true);
			mergeComponentsMap.get(SummaryInputColumns.MZ_VALUES).add(summaryObject);
			
			summaryObject = createParameterSummaryBlock(
					rscriptParts, dataLinePrefix, designObject,
					nonRedundantFields, io, SummaryInputColumns.RT_VALUES, dataPathPrefix, true);
			mergeComponentsMap.get(SummaryInputColumns.RT_VALUES).add(summaryObject);
			
			summaryObject = createParameterSummaryBlock(
					rscriptParts, dataLinePrefix, designObject,
					nonRedundantFields, io, SummaryInputColumns.PEAK_WIDTH, dataPathPrefix, true);
			mergeComponentsMap.get(SummaryInputColumns.PEAK_WIDTH).add(summaryObject);
		}
		rscriptParts.add("\n# Combine and export summaries ####\n");
		String ts = FIOUtils.getTimestamp();
		ArrayList<String>meltIdParts = new ArrayList<String>();
		ArrayList<String>condensedSummaryFields = new ArrayList<String>();
		List<DataSummarizationParameters>paramsToPlot = new ArrayList<DataSummarizationParameters>();
		
		meltIdParts.add("\"" + DataSummarizationParameters.SAMPLE_TYPE.getRName() + "\"");
		condensedSummaryFields.add(DataSummarizationParameters.SAMPLE_TYPE.getRName());
		
		for(SummaryInputColumns nrs : nonRedundantFields) {
			meltIdParts.add("\"" + nrs.getRName() + "\"");
			condensedSummaryFields.add(nrs.getRName());
		}
		condensedSummaryFields.add("variable");
		
		String meltIdString = StringUtils.join(meltIdParts, ", ");
		String condensedSummaryFieldList = StringUtils.join(condensedSummaryFields, ", ");

		//	
		String gridYParam = null;
		String gridXParam = null;
		for(int i=0; i<nonRedundantFields.size(); i++) {
			
			SummaryInputColumns nrf = nonRedundantFields.get(i);
			if(nrf.equals(SummaryInputColumns.BATCH))
				continue;
			
			if(gridYParam == null)
				gridYParam = nrf.getRName();
			
			if(gridYParam != null && gridXParam == null)
				gridXParam = nrf.getRName();
			
			if(gridYParam != null && gridXParam != null)
				break;
		}			
		for(Entry<SummaryInputColumns,List<String>> me : mergeComponentsMap.entrySet()) {
			
			String combinedSummaryObject = me.getKey().getRName() + ".data.summary";
			rscriptParts.add(combinedSummaryObject + 
					" <- bind_rows(" + StringUtils.join(me.getValue(), ", ") + ")");
			String summaryFileName = prefix + "_" + me.getKey().getRName() + "_summary_" + ts + ".txt";
			
			rscriptParts.add("write.table(" + combinedSummaryObject + ", file = \"" + 
					createSafeFileName(summaryFileName) + "\", quote = F, sep = \"\\t\", na = \"\", row.names = F)");
			
			//	Create condensed summary and graphics
			rscriptParts.add(combinedSummaryObject + ".melt <- melt(" + combinedSummaryObject + ", "
					+ "id = c(" + meltIdString + "), measure.vars = c(\"" 
					+ DataSummarizationParameters.MEDIAN_VALUE.getRName() + "\",\"" 
					+ DataSummarizationParameters.PERCENT_MISSING.getRName() + "\",\"" 
					+ DataSummarizationParameters.RSD.getRName() + "\"))");
			
			String condensedSummaryObject = combinedSummaryObject + ".condensed";
			rscriptParts.add(condensedSummaryObject + " <- " + combinedSummaryObject + 
					".melt %>% group_by(" + condensedSummaryFieldList + ") " +
					"%>% summarise(" + DataSummarizationParameters.MEDIAN_VALUE.getRName() 
					+ " = median(value, na.rm = T), "
					+ DataSummarizationParameters.MEAN_VALUE.getRName() + " = mean(value, na.rm = T), " 
					+ DataSummarizationParameters.SD.getRName() + " = sd(value, na.rm = T))");
			rscriptParts.add(condensedSummaryObject + "$" + DataSummarizationParameters.RSD.getRName() 
					+ " <- " + condensedSummaryObject + "$" + DataSummarizationParameters.SD.getRName() 
					+ " / " + condensedSummaryObject + "$" + DataSummarizationParameters.MEAN_VALUE.getRName() + " * 100");
			
			String condensedSummaryFileName = prefix + "_" + me.getKey().getRName() + "_condensed_summary_" + ts + ".txt";
			rscriptParts.add("write.table(" + condensedSummaryObject + ", file = \"" + 
					createSafeFileName(condensedSummaryFileName) + "\", quote = F, sep = \"\\t\", na = \"\", row.names = F)");
			
			paramsToPlot.clear();
			
			//	Charts
			if(me.getKey().equals(SummaryInputColumns.PEAK_AREAS)) {
				
				paramsToPlot.add(DataSummarizationParameters.RSD);
				generateCondensedSummaryBarCharts(
						rscriptParts,
						condensedSummaryObject,
						me.getKey(),
						paramsToPlot,
						SummaryInputColumns.BATCH.getRName(),
						gridYParam,
						gridXParam);
				
				generateSummaryDensityPlots(
						rscriptParts,
						combinedSummaryObject,
						me.getKey(),
						paramsToPlot,
						gridYParam,
						0.0d, 
						150.0d);
				
				paramsToPlot.clear();
				paramsToPlot.add(DataSummarizationParameters.MEDIAN_VALUE);
				generateSummaryDensityPlots(
						rscriptParts,
						combinedSummaryObject,
						me.getKey(),
						paramsToPlot,
						gridYParam,
						0.0d, 
						300000.0d);
				
				paramsToPlot.clear();
				paramsToPlot.add(DataSummarizationParameters.PERCENT_MISSING);
				generateSummaryDensityPlots(
						rscriptParts,
						combinedSummaryObject,
						me.getKey(),
						paramsToPlot,
						gridYParam,
						0.0d, 
						100.0d);	
			}
			if(me.getKey().equals(SummaryInputColumns.MZ_VALUES)) {
				
				paramsToPlot.add(DataSummarizationParameters.RSD);
				generateCondensedSummaryBarCharts(
						rscriptParts,
						condensedSummaryObject,
						me.getKey(),
						paramsToPlot,
						SummaryInputColumns.BATCH.getRName(),
						gridYParam,
						gridXParam);
				
				generateSummaryDensityPlots(
						rscriptParts,
						combinedSummaryObject,
						me.getKey(),
						paramsToPlot,
						gridYParam,
						0.0d, 
						0.001d);
			}
			if(me.getKey().equals(SummaryInputColumns.RT_VALUES)) {
				
				paramsToPlot.add(DataSummarizationParameters.RSD);
				generateCondensedSummaryBarCharts(
						rscriptParts,
						condensedSummaryObject,
						me.getKey(),
						paramsToPlot,
						SummaryInputColumns.BATCH.getRName(),
						gridYParam,
						gridXParam);
				
				generateSummaryDensityPlots(
						rscriptParts,
						combinedSummaryObject,
						me.getKey(),
						paramsToPlot,
						gridYParam,
						0.0d, 
						1.1d);
			}
			if(me.getKey().equals(SummaryInputColumns.PEAK_QUALITY)) {
				
				paramsToPlot.add(DataSummarizationParameters.RSD);
				paramsToPlot.add(DataSummarizationParameters.MEDIAN_VALUE);
				generateCondensedSummaryBarCharts(
						rscriptParts,
						condensedSummaryObject,
						me.getKey(),
						paramsToPlot,
						SummaryInputColumns.BATCH.getRName(),
						gridYParam,
						gridXParam);
				
				paramsToPlot.clear();
				paramsToPlot.add(DataSummarizationParameters.RSD);
				generateSummaryDensityPlots(
						rscriptParts,
						combinedSummaryObject,
						me.getKey(),
						paramsToPlot,
						gridYParam,
						0.0d, 
						60.0d);	
				
				paramsToPlot.clear();
				paramsToPlot.add(DataSummarizationParameters.MEDIAN_VALUE);
				generateSummaryDensityPlots(
						rscriptParts,
						combinedSummaryObject,
						me.getKey(),
						paramsToPlot,
						gridYParam,
						0.0d, 
						105.0d);		
			}	
			if(me.getKey().equals(SummaryInputColumns.PEAK_WIDTH)) {
				
				paramsToPlot.add(DataSummarizationParameters.RSD);				
				generateCondensedSummaryBarCharts(
						rscriptParts,
						condensedSummaryObject,
						me.getKey(),
						paramsToPlot,
						SummaryInputColumns.BATCH.getRName(),
						gridYParam,
						gridXParam);
				
				generateSummaryDensityPlots(
						rscriptParts,
						combinedSummaryObject,
						me.getKey(),
						paramsToPlot,
						gridYParam,
						0.0d, 
						150.0d);	
				
				paramsToPlot.clear();
				paramsToPlot.add(DataSummarizationParameters.MEDIAN_VALUE);
				generateCondensedSummaryBarCharts(
						rscriptParts,
						condensedSummaryObject,
						me.getKey(),
						paramsToPlot,
						SummaryInputColumns.BATCH.getRName(),
						gridYParam,
						gridXParam);
				
				generateSummaryDensityPlots(
						rscriptParts,
						combinedSummaryObject,
						me.getKey(),
						paramsToPlot,
						gridYParam,
						0.0d, 
						0.6d);
			}
		}		
		String rScriptFileName = "UntargetedDataSummarization-" + FIOUtils.getTimestamp() + ".R";
		Path outputPath = Paths.get(inputMapFile.getParentFile().getAbsolutePath(), rScriptFileName);
		try {
		    Files.write(outputPath, 
		    		rscriptParts,
		            StandardCharsets.UTF_8,
		            StandardOpenOption.CREATE, 
		            StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
		    e.printStackTrace();
		}
	}
	
	private static void generateCondensedSummaryBarCharts(
			List<String>rscriptParts,
			String condensedSummaryObject,
			SummaryInputColumns fieldToSummarize,
			List<DataSummarizationParameters>paramsToPlot,
			String barFillParam,
			String gridYParam,
			String gridXParam) {
		
		for(DataSummarizationParameters param : paramsToPlot) {
			
			String plotObject = fieldToSummarize.getRName() + "." + param.getRName() + ".plot";			
			String plotDataObject = plotObject+ ".data";
			rscriptParts.add(plotDataObject + " <- " + condensedSummaryObject +
					"[" + condensedSummaryObject + "$variable == \"" + param.getRName() + "\",]");
			String gridDefinition = ".";
			String extraByString = "";
			if(gridYParam != null) {
				gridDefinition = gridYParam;
				extraByString = "/" + gridYParam;
			}
			if(gridXParam != null) {
				gridDefinition += "~" + gridXParam;
				extraByString += "/" + gridXParam;
			}
			String condensedBarChartString = plotObject +  " <- ggplot(" + plotDataObject 
					+ ", aes(x = sample_type, y = " + DataSummarizationParameters.MEAN_VALUE.getRName() 
					+ ", fill = " + barFillParam + ")) + "
					+ "geom_col( position = \"dodge\", width = 0.5, alpha = 0.7, color = \"black\", linewidth = 0.1) + "
					+ "geom_errorbar(aes(ymin = " 
					+ DataSummarizationParameters.MEAN_VALUE.getRName() 
					+ "-" + DataSummarizationParameters.SD.getRName() 
					+ ", ymax = " + DataSummarizationParameters.MEAN_VALUE.getRName() 
					+ "+" + DataSummarizationParameters.SD.getRName() + "), "
					+ "position =  position_dodge(width = 0.5), width = 0.2)";
			if(!gridDefinition.equals("."))
				condensedBarChartString += " + facet_grid(" + gridDefinition + ") ";
			
			String plotTitle = "Median values for " + fieldToSummarize.getName() 
				+ " " + param.getName() + " by " + barFillParam + "/" 
					+ DataSummarizationParameters.SAMPLE_TYPE.getRName() + extraByString;
			condensedBarChartString += " + ggtitle(\"" + plotTitle + "\")";
			rscriptParts.add(condensedBarChartString);	
			
			String plotFileName = createSafeFileName(plotTitle) + ".png";				
			rscriptParts.add("ggsave(\"" + plotFileName + "\", plot = " + plotObject + ",  width = 14, height = 8.5)");
			rscriptParts.add("###");
		}		
	}
	
	private static void generateSummaryDensityPlots(
			List<String>rscriptParts,
			String summaryObject,
			SummaryInputColumns fieldToSummarize,
			List<DataSummarizationParameters>paramsToPlot,
			String gridYParam,
			double min, 
			double max) {
		
		for(DataSummarizationParameters param : paramsToPlot) {
			
			String plotObject = summaryObject + "." + param.getRName() + ".density.plot";			

			String gridDefinition = "";
			String extraByString = "";
			if(gridYParam != null) {
				gridDefinition = gridYParam;
				extraByString = "/" + gridYParam;
			}
			//	Sample type / Batch ...
			String grDef = gridDefinition + "~" + SummaryInputColumns.BATCH.getRName();
			String densityPlotString = plotObject +  " <- ggplot(" + summaryObject 
					+ ", aes(" + param.getRName() + ", colour = " + DataSummarizationParameters.SAMPLE_TYPE.getRName() 
					+ ")) + geom_density(linewidth=1) + xlim(" 
					+ Double.toString(min) + "," + Double.toString(max) 
					+ ") + facet_grid(" + grDef + ")";
			String plotTitle = "Density plot for " + fieldToSummarize.getName() 
				+ " " + param.getName() + " by " + SummaryInputColumns.BATCH.getName() + "/" 
					+ DataSummarizationParameters.SAMPLE_TYPE.getRName() + extraByString;
			densityPlotString += " + ggtitle(\"" + plotTitle + "\")";
			rscriptParts.add(densityPlotString);	
			
			String plotFileName = createSafeFileName(plotTitle) + ".png";				
			rscriptParts.add("ggsave(\"" + plotFileName + "\", plot = " + plotObject + ",  width = 14, height = 8.5)");

			//	Batch / Sample type ...
			grDef = gridDefinition + "~" + DataSummarizationParameters.SAMPLE_TYPE.getRName();
			densityPlotString = plotObject +  "2 <- ggplot(" + summaryObject 
					+ ", aes(" + param.getRName() + ", colour = " + SummaryInputColumns.BATCH.getRName() 
					+ ")) + geom_density(linewidth=1) + xlim(" 
					+ Double.toString(min) + "," + Double.toString(max) 
					+ ") + facet_grid(" + grDef + ")";
			plotTitle = "Density plot for " + fieldToSummarize.getName() 
				+ " " + param.getName() + " by " + DataSummarizationParameters.SAMPLE_TYPE.getRName() + "/" 
					+ SummaryInputColumns.BATCH.getName() + extraByString;
			densityPlotString += " + ggtitle(\"" + plotTitle + "\")";
			rscriptParts.add(densityPlotString);	
			
			plotFileName = createSafeFileName(plotTitle) + ".png";				
			rscriptParts.add("ggsave(\"" + plotFileName + "\", plot = " + plotObject + "2,  width = 14, height = 8.5)");
			rscriptParts.add("###");
		}		
	}
	
	private static String createSafeFileName(String inputFileName) {
		return inputFileName.replaceAll("[\\\\/:*?\"<>|%]", "_").replaceAll("\\s+", "_");
	}
	
	private static String createParameterSummaryBlock(
			List<String>rscriptParts,
			String dataLinePrefix,
			String designObject,
			List<SummaryInputColumns>nonRedundantFields,
			SummarizationDataInputObject io,
			SummaryInputColumns fieldToSummarize,
			String dataPathPrefix,
			boolean removeExtraControls) {
		
		rscriptParts.add("\n### Summarize " + fieldToSummarize.getName() + " ####\n");
				
		String dataFieldObject = dataLinePrefix + "." + fieldToSummarize.getRName();
		rscriptParts.add(dataFieldObject + ".in <- read.delim(\"" + dataPathPrefix +
				io.getField(fieldToSummarize) + "\", check.names=FALSE)");
		rscriptParts.add(dataFieldObject + " <- " + dataFieldObject + ".in[,-c(1:9)]");
		rscriptParts.add("rownames(" + dataFieldObject + ") <- " + dataFieldObject + ".in$`Feature name`");
		rscriptParts.add(dataFieldObject + ".t <- as.data.frame(t(" + dataFieldObject + "))");
		rscriptParts.add(dataFieldObject + ".with.design <- merge(" + designObject + ", " + dataFieldObject + ".t, by = 0, all = T)");
		rscriptParts.add(dataFieldObject + ".melt <- melt(" + dataFieldObject + ".with.design[,-1], id = c(\"sample_type\"))");
		
		String fieldSummaryObject = dataLinePrefix + "." + fieldToSummarize.getRName() + ".summary";
		rscriptParts.add(fieldSummaryObject + " <- " + dataFieldObject + ".melt %>% group_by(sample_type, variable) "
				+ "%>% summarise(" + DataSummarizationParameters.MEDIAN_VALUE.getRName() 
				+ " = median(value, na.rm = T), meanVal = mean(value, na.rm = T), "
				+ DataSummarizationParameters.SD.getRName() + " = sd(value, na.rm = T), "
				+ DataSummarizationParameters.PERCENT_MISSING.getRName() + " = 100 * mean(is.na(value)))");			
		rscriptParts.add(fieldSummaryObject + "$" + DataSummarizationParameters.RSD.getRName() 
		+ " <- " + fieldSummaryObject + "$" + DataSummarizationParameters.SD.getRName() + " / " + 
				fieldSummaryObject + "$" + DataSummarizationParameters.MEAN_VALUE.getRName() + " * 100");
		rscriptParts.add(fieldSummaryObject + "$sample_type <- as.factor(" + fieldSummaryObject + "$sample_type)");
		
		if(removeExtraControls) {
			
			rscriptParts.add(fieldSummaryObject + " <- " + fieldSummaryObject + 
					"[!(" + fieldSummaryObject + "$sample_type %in% c(\"QC-Blank\",\"QC-InternalStandard\",\"QC-Reference\")),]");
		}	
		for(SummaryInputColumns nrf : nonRedundantFields) {
			
			rscriptParts.add(fieldSummaryObject + "$" + nrf.getRName() + " <- \"" + io.getField(nrf) + "\"");
			rscriptParts.add(fieldSummaryObject + "$" + nrf.getRName() + 
					" <- as.factor(" + fieldSummaryObject + "$" + nrf.getRName() + ")");
		}	
		rscriptParts.add("rm(" + dataFieldObject + ", " + dataFieldObject + 
				".in, " + dataFieldObject + ".melt, " + dataFieldObject + ".t, " + dataFieldObject + ".with.design)"); 
		
		return fieldSummaryObject;
	}
	
	//	TODO
	public static void generateParameterDistributionScript(
			String experimentId,
			String assayType,
			String assayType4R,
			File inputMap,
			File rWorkingDir) {
		String[][] methodListData = DelimitedTextParser.parseTextFile(
				inputMap, MRC2ToolBoxConfiguration.getTabDelimiter());
		String workDirForR = rWorkingDir.getAbsolutePath().replaceAll("\\\\", "/");
		
		List<String>rscriptParts = new ArrayList<String>();
		rscriptParts.add("# " + experimentId + " " + assayType + " running QC, parameter distributions ####\n");
		rscriptParts.add("setwd(\"" + workDirForR + "\")\n");
		rscriptParts.add("library(ggplot2)");
		rscriptParts.add("library(reshape2)");
		rscriptParts.add("library(dplyr)");
		
		for(int i=1; i<=methodListData.length; i++) {
			
		}		
		String rScriptFileName = experimentId + "-" + assayType + 
				"-RunningQC-" + FIOUtils.getTimestamp() + ".R";
		Path outputPath = Paths.get(
				rWorkingDir.getAbsolutePath(), rScriptFileName);
		try {
		    Files.write(outputPath, 
		    		rscriptParts,
		            StandardCharsets.UTF_8,
		            StandardOpenOption.CREATE, 
		            StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
		    e.printStackTrace();
		}
	}
}
























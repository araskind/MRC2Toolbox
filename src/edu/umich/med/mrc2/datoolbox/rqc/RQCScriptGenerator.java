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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;

import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.utils.DelimitedTextParser;
import edu.umich.med.mrc2.datoolbox.utils.FIOUtils;

public class RQCScriptGenerator {
	
	public static void generateMultiBatchMetabCombinerAlignmentScriptScript() {
		
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
	
	public static void createMultyBatchDataSummarizationScript(File inputMapFile, File dataDir) {
		
		List<String>qcSummaryNames = new ArrayList<String>();
		List<String>rscriptParts = new ArrayList<String>();
		String workDirForR = dataDir.getAbsolutePath().replaceAll("\\\\", "/");
		String[][] inputData = DelimitedTextParser.parseTextFile(
				inputMapFile, MRC2ToolBoxConfiguration.getTabDelimiter());
		
		List<String>header = Arrays.asList(inputData[0]);
		for(SummaryInputColumns field : SummaryInputColumns.values()) {
			
			if(!header.contains(field.name())) {
				
				System.err.println(field.name() + " missing in data input map file!");
				return;
			}
		}
		List<SummarizationDataInputObject>dataInputList = 
				new ArrayList<SummarizationDataInputObject>();
		for(int i = 1; i<inputData.length; i++) {
			
			SummarizationDataInputObject io = new SummarizationDataInputObject();
			for(int j = 0; j<inputData[0].length; j++) {
			
				SummaryInputColumns field = SummaryInputColumns.getOptionByName(inputData[0][j]);
				String value = inputData[i][j];
				if(value == null || value.isEmpty()) {
					
					System.err.println(field.name() + 
							" missing in data input map file on line " + Integer.toString(i+ 1) + "!");
					return;
				}
					
				io.setField(field, value);			
			}
			dataInputList.add(io);			
		}
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
		List<SummaryInputColumns>nonRedundantFields = new ArrayList<SummaryInputColumns>();
		feildVariationMap.entrySet().stream().
			filter(e -> e.getValue().size() > 1).
			forEach(e -> nonRedundantFields.add(e.getKey()));
		List<SummaryInputColumns>commonFields = new ArrayList<SummaryInputColumns>();
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
			rscriptParts.add(designObject + ".in <- read.delim(\"" + 
					io.getField(SummaryInputColumns.MANIFEST) + "\", check.names=FALSE)");
			rscriptParts.add(designObject + " <- as.data.frame(" + designObject + ".in[,2])");
			rscriptParts.add("rownames(" + designObject + ") <- "+ designObject + ".in$`raw_file`");
			rscriptParts.add("colnames(" + designObject + ")[1] <- \"sample_type\"");
			rscriptParts.add(designObject + "$sample_type <- as.factor("+ designObject + "$sample_type)");
			rscriptParts.add("rm(" + designObject + ".in)");
			
			String summaryObject = createParameterSummaryBlock(
					rscriptParts, dataLinePrefix, designObject,
					nonRedundantFields, io, SummaryInputColumns.PEAK_AREAS, true);
			mergeComponentsMap.get(SummaryInputColumns.PEAK_AREAS).add(summaryObject);
			
			summaryObject = createParameterSummaryBlock(
					rscriptParts, dataLinePrefix, designObject,
					nonRedundantFields, io, SummaryInputColumns.PEAK_QUALITY, true);
			mergeComponentsMap.get(SummaryInputColumns.PEAK_QUALITY).add(summaryObject);
			
			summaryObject = createParameterSummaryBlock(
					rscriptParts, dataLinePrefix, designObject,
					nonRedundantFields, io, SummaryInputColumns.MZ_VALUES, true);
			mergeComponentsMap.get(SummaryInputColumns.MZ_VALUES).add(summaryObject);
			
			summaryObject = createParameterSummaryBlock(
					rscriptParts, dataLinePrefix, designObject,
					nonRedundantFields, io, SummaryInputColumns.RT_VALUES, true);
			mergeComponentsMap.get(SummaryInputColumns.RT_VALUES).add(summaryObject);
			
			summaryObject = createParameterSummaryBlock(
					rscriptParts, dataLinePrefix, designObject,
					nonRedundantFields, io, SummaryInputColumns.PEAK_WIDTH, true);
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
					summaryFileName + "\", quote = F, sep = \"\\t\", na = \"\", row.names = F)");
			
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
					condensedSummaryFileName + "\", quote = F, sep = \"\\t\", na = \"\", row.names = F)");
			
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
		Path outputPath = Paths.get(dataDir.getAbsolutePath(), rScriptFileName);
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
			
			String plotFileName = plotTitle.replaceAll("[\\\\/:*?\"<>|]", "_").replaceAll("\\s+", "_") + ".png";				
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
			
			String plotFileName = plotTitle.replaceAll("[\\\\/:*?\"<>|]", "_").replaceAll("\\s+", "_") + ".png";				
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
			
			plotFileName = plotTitle.replaceAll("[\\\\/:*?\"<>|]", "_").replaceAll("\\s+", "_") + ".png";				
			rscriptParts.add("ggsave(\"" + plotFileName + "\", plot = " + plotObject + "2,  width = 14, height = 8.5)");
			rscriptParts.add("###");
		}		
	}
	
	private static String createParameterSummaryBlock(
			List<String>rscriptParts,
			String dataLinePrefix,
			String designObject,
			List<SummaryInputColumns>nonRedundantFields,
			SummarizationDataInputObject io,
			SummaryInputColumns fieldToSummarize,
			boolean removeExtraControls) {
		
		rscriptParts.add("\n### Summarize " + fieldToSummarize.getName() + " ####\n");
				
		String dataFieldObject = dataLinePrefix + "." + fieldToSummarize.getRName();
		rscriptParts.add(dataFieldObject + ".in <- read.delim(\"" + 
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
























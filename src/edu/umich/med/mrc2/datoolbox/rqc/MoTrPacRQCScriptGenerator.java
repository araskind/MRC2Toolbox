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
import java.util.List;

import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.FilePreferencesFactory;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.utils.DelimitedTextParser;
import edu.umich.med.mrc2.datoolbox.utils.FIOUtils;

public class MoTrPacRQCScriptGenerator {

	public static void main(String[] args) {

		System.setProperty("java.util.prefs.PreferencesFactory", 
				FilePreferencesFactory.class.getName());
		System.setProperty(FilePreferencesFactory.SYSTEM_PROPERTY_FILE, 
				MRC2ToolBoxCore.configDir + "MRC2ToolBoxPrefs.txt");
		MRC2ToolBoxConfiguration.initConfiguration();

		try {
			generateMoTrPACQCScriptEX01117();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void generateMoTrPACQCScriptEX01117() {
		
		String experimentId = "EX01117";
		File baseDir = new File("Y:\\DataAnalysis\\_Reports\\EX01117 - PASS 1C\\4BIC\\QC-20241010");
		File inputMap = new File("Y:\\DataAnalysis\\_Reports\\EX01117 - PASS 1C\\"
				+ "4BIC\\QC-20241010\\EX01117-MoTrPAC-QC-inputMap.txt");

		generateMoTrPACQCScript(
				experimentId,
				baseDir,
				inputMap);
	}
	
	private static void generateMoTrPACQCScriptEX01263() {
		
		String experimentId = "EX011263";
		File baseDir = new File("Y:\\DataAnalysis\\_Reports\\EX01263 - PASS 1A 1C 18mo P20000245T C20000316E\\4BIC\\"
				+ "QC-2024-10-07\\RGraphs\\Uploaded2BIC");
		File inputMap = new File("Y:\\DataAnalysis\\_Reports\\EX01263 - PASS 1A 1C 18mo P20000245T C20000316E\\4BIC\\"
				+ "QC-2024-10-07\\RGraphs\\Uploaded2BIC\\EX01263-MoTrPAC-QC-inputMap.txt");

		generateMoTrPACQCScript(
				experimentId,
				baseDir,
				inputMap);
	}
	
	private static void generateMoTrPACQCScript(
			String experimentId,
			File baseDir,			
			File inputMap) {
		
		String[][] methodListData = DelimitedTextParser.parseTextFile(
				inputMap, MRC2ToolBoxConfiguration.getTabDelimiter());
		
		List<String>scriptBlock = new ArrayList<String>();
		
		scriptBlock.add("# " + experimentId + " QC for MoTrPAC ####");
		scriptBlock.add("library(ggplot2)");
		scriptBlock.add("library(reshape2)");
		scriptBlock.add("library(dplyr)");
		
		for(int i=0; i<methodListData.length; i++) {
			
			String tissueCodeName = methodListData[i][0];
			String tissueCode = methodListData[i][1];
			String assayMethod = methodListData[i][2];
			String assayCode = methodListData[i][3];
			String manifestFileName = methodListData[i][4];
			String targetedDataFileName = methodListData[i][5];
			String untargetedDataFileName = methodListData[i][6];
			
			String workDirForR = Paths.get(baseDir.getAbsolutePath(), tissueCodeName).toString().replaceAll("\\\\", "/");
								
			scriptBlock.add("# " + tissueCodeName + " | " + assayMethod + "  ####");
			scriptBlock.add("setwd(\"" + workDirForR + "\")");
			scriptBlock.add("\t");
			scriptBlock.add("## Read design ####");
			scriptBlock.add(tissueCode + "." + assayCode + ".design.in <- read.delim(\"" + manifestFileName +".txt\", check.names=FALSE)");
			scriptBlock.add(tissueCode + "." + assayCode + ".design.in <- within(" + tissueCode + "." + assayCode + 
					".design.in, sample_type[grepl('CS0000OP0',sample_id)] <- 'OTHER-Pooled')");
			scriptBlock.add(tissueCode + "." + assayCode + ".design.in <- within(" + tissueCode + "." + assayCode + 
					".design.in, sample_type[grepl('CS0000OP1',sample_id)] <- 'OTHER-Pooled')");
			scriptBlock.add(tissueCode + "." + assayCode + ".design <- as.data.frame(" + tissueCode + "." + assayCode + ".design.in[,2])");
			scriptBlock.add("rownames(" + tissueCode + "." + assayCode + ".design) <- " + tissueCode + "." + assayCode + ".design.in$`sample_id`");
			scriptBlock.add("colnames(" + tissueCode + "." + assayCode + ".design)[1] <- \"sample_type\"");
			scriptBlock.add(tissueCode + "." + assayCode + ".design$sample_type <- as.factor(" + tissueCode + "." + assayCode + ".design$sample_type)");
			scriptBlock.add("\t");
			scriptBlock.add("## Read targeted data ####");
			scriptBlock.add(tissueCode + "." + assayCode + ".named.in <- read.delim(\"" + targetedDataFileName + ".txt\", check.names=FALSE)");
			scriptBlock.add(tissueCode + "." + assayCode + ".named <- as.data.frame(t(" + tissueCode + "." + assayCode + ".named.in))[-1,]");
			scriptBlock.add("colnames(" + tissueCode + "." + assayCode + ".named) <- " + tissueCode + "." + assayCode + ".named.in[,1]");
			scriptBlock.add("rn <- row.names(" + tissueCode + "." + assayCode + ".named)");
			scriptBlock.add(tissueCode + "." + assayCode + ".named <- as.data.frame(sapply(" + tissueCode + "." + assayCode + ".named, as.numeric))");
			scriptBlock.add("row.names(" + tissueCode + "." + assayCode + ".named) <- rn");
			scriptBlock.add(tissueCode + "." + assayCode + ".named.des <- merge(" + tissueCode + "." + assayCode + ".design, " + tissueCode + "." + 
					assayCode + ".named, by = 0, all = T)");
			scriptBlock.add("colnames(" + tissueCode + "." + assayCode + ".named.des)[1] <- \"sample_id\"");
			scriptBlock.add("\t");
			scriptBlock.add("## Read untargeted data ####");
			scriptBlock.add(tissueCode + "." + assayCode + ".unnamed.in <- read.delim(\"" + untargetedDataFileName + ".txt\", check.names=FALSE)");
			scriptBlock.add(tissueCode + "." + assayCode + ".unnamed <- as.data.frame(t(" + tissueCode + "." + assayCode + ".unnamed.in))[-1,]");
			scriptBlock.add("colnames(" + tissueCode + "." + assayCode + ".unnamed) <- " + tissueCode + "." + assayCode + ".unnamed.in[,1]");
			scriptBlock.add("rn <- row.names(" + tissueCode + "." + assayCode + ".unnamed)");
			scriptBlock.add(tissueCode + "." + assayCode + ".unnamed <- as.data.frame(sapply(" + tissueCode + "." + assayCode + ".unnamed, as.numeric))");
			scriptBlock.add("row.names(" + tissueCode + "." + assayCode + ".unnamed) <- rn");
			scriptBlock.add(tissueCode + "." + assayCode + ".unnamed.des <- merge(" + tissueCode + "." + assayCode + ".design, " + tissueCode + "." + 
					assayCode + ".unnamed, by = 0, all = T)");
			scriptBlock.add("colnames(" + tissueCode + "." + assayCode + ".unnamed.des)[1] <- \"sample_id\"");
			scriptBlock.add("\t");
			scriptBlock.add("rm(rn)");
			scriptBlock.add("\t");
			scriptBlock.add("## Create summary for named ####");
			scriptBlock.add("\t");
			scriptBlock.add("### Remove controls ####");
			scriptBlock.add(tissueCode + "." + assayCode + ".named.des.sample <- " + tissueCode + "." + assayCode + ".named.des[!(" + tissueCode + "." + 
					assayCode + ".named.des$sample_type %in% c(\"QC-Blank\",\"QC-InternalStandard\",\"QC-Reference\")),]");
			scriptBlock.add("\t");
			scriptBlock.add("### Summarize named data ####");
			scriptBlock.add(tissueCode + "." + assayCode + ".named.des.sample.melt <- melt(" + tissueCode + "." + assayCode + 
					".named.des.sample[,-1], id = c(\"sample_type\"))");
			scriptBlock.add(tissueCode + "." + assayCode + ".named.des.sample.summary <- " + tissueCode + "." + assayCode + 
					".named.des.sample.melt %>% group_by(sample_type, variable) %>% summarise(medianVal = median(value, na.rm = T), "
					+ "meanVal = mean(value, na.rm = T), stDev = sd(value, na.rm = T), pcMissing = 100*mean(is.na(value)))");
			scriptBlock.add(tissueCode + "." + assayCode + ".named.des.sample.summary$RSD <- " + tissueCode + "." + assayCode + 
					".named.des.sample.summary$stDev / " + tissueCode + "." + assayCode + ".named.des.sample.summary$meanVal * 100");
			scriptBlock.add(tissueCode + "." + assayCode + ".named.des.sample.summary$sample_type <- as.factor(" + tissueCode + 
					"." + assayCode + ".named.des.sample.summary$sample_type)");
			scriptBlock.add("\t");
			scriptBlock.add("### Histograms for named data ####");
			scriptBlock.add("rsdPlot <- ggplot(" + tissueCode + "." + assayCode + ".named.des.sample.summary, "
					+ "aes(RSD, colour = sample_type, fill = sample_type)) + geom_histogram(binwidth = 5, alpha=0.6, aes(y=..density..), boundary = 0) "
					+ "+ facet_wrap(~sample_type) + theme(");
			scriptBlock.add("  legend.position=\"none\", panel.spacing = unit(0.1, \"lines\"), strip.text.x = element_text(size = 8)");
			scriptBlock.add(") + xlab(\"\") + ggtitle(\"RSD distribution by sample type for named compounds, " + tissueCodeName + ", " + assayMethod + "\")");
			scriptBlock.add("ggsave(\"" + experimentId + "-" + tissueCodeName + "-" + assayMethod + "-named-RSD-histogram.png\", "
					+ "plot = rsdPlot,  width = 14, height = 8.5)");
			scriptBlock.add("\t");
			scriptBlock.add("missingPlot <- ggplot(" + tissueCode + "." + assayCode + ".named.des.sample.summary, "
					+ "aes(pcMissing, colour = sample_type, fill = sample_type)) + geom_histogram(binwidth = 5, alpha=0.6, aes(y=..density..), boundary = 0) "
					+ "+ facet_wrap(~sample_type) + theme(");
			scriptBlock.add("  legend.position=\"none\", panel.spacing = unit(0.1, \"lines\"), strip.text.x = element_text(size = 8)");
			scriptBlock.add(") + xlab(\"\") + ggtitle(\"% Missing distribution by sample type for named compounds, " + tissueCodeName + ", " + assayMethod + "\")");
			scriptBlock.add("ggsave(\"" + experimentId + "-" + tissueCodeName + "-" + assayMethod + "-named-Missing-histogram.png\", "
					+ "plot = missingPlot,  width = 14, height = 8.5)");
			scriptBlock.add("\t");
			scriptBlock.add("### Write out summary for named data ####");
			scriptBlock.add(tissueCode + "." + assayCode + ".named.des.sample.summary.4out <- " + tissueCode + "." + assayCode + ".named.des.sample.summary");
			scriptBlock.add(tissueCode + "." + assayCode + ".named.des.sample.summary.4out$Tissue <- \"" + tissueCodeName + "\"");
			scriptBlock.add(tissueCode + "." + assayCode + ".named.des.sample.summary.4out$Method <- \"" + assayMethod + "\"");
			scriptBlock.add("write.table(" + tissueCode + "." + assayCode + ".named.des.sample.summary.4out, file = \"" + experimentId + 
					"-" + tissueCodeName + "-" + assayMethod + "-named-QC-summary.txt\", quote = F, sep = \"\t\", na = \"\", row.names = F)");
			scriptBlock.add("\t");
			scriptBlock.add("### IS plot ####");
			scriptBlock.add("\t");
			scriptBlock.add(tissueCode + "." + assayCode + ".named.des.sample.is <- select(" + tissueCode + "." + assayCode + ".named.des.sample,contains(\"iSTD\"))");
			scriptBlock.add(tissueCode + "." + assayCode + ".named.des.sample.is$sample_id <- " + tissueCode + "." + assayCode + ".named.des.sample$sample_id");
			scriptBlock.add(tissueCode + "." + assayCode + ".named.des.sample.is$sample_type <- " + tissueCode + "." + assayCode + ".named.des.sample$sample_type");
			scriptBlock.add(tissueCode + "." + assayCode + ".named.des.sample.is <- merge(" + tissueCode + "." + assayCode + ".named.des.sample.is, " + 
					tissueCode + "." + assayCode + ".design.in[,c(1,3)], by = \"sample_id\", all = F)");
			scriptBlock.add(tissueCode + "." + assayCode + ".named.des.sample.is <- arrange(" + tissueCode + "." + assayCode + ".named.des.sample.is, sample_order)");
			scriptBlock.add(tissueCode + "." + assayCode + ".named.des.sample.is.melt <- melt(" + tissueCode + "." + assayCode + 
					".named.des.sample.is, id = c(\"sample_id\",\"sample_type\",\"sample_order\"))");
			scriptBlock.add(tissueCode + "." + assayCode + ".named.des.sample.is.melt$sample_id <- "
					+ "as.factor(" + tissueCode + "." + assayCode + ".named.des.sample.is.melt$sample_id)");
			scriptBlock.add(tissueCode + "." + assayCode + ".named.des.sample.is.melt$sample_type <- "
					+ "as.factor(" + tissueCode + "." + assayCode + ".named.des.sample.is.melt$sample_type)");
			scriptBlock.add(tissueCode + "." + assayCode + ".is.plot <- ggplot(" + tissueCode + "." + 
						assayCode + ".named.des.sample.is.melt, aes(sample_order, value, color = sample_type)) + geom_point() + "
								+ "facet_wrap(~variable, ncol = 1, scales = \"free_y\") + theme(legend.position=\"bottom\")");
			scriptBlock.add("\t");
			scriptBlock.add("ggsave(\"" + experimentId + "-" + tissueCodeName + "-" + assayMethod + 
					"-named-IS-plot.png\", plot = " + tissueCode + "." + assayCode + ".is.plot,  width = 8.5, height = 16)");
			scriptBlock.add("\t");
			scriptBlock.add("## Create summary for unnamed ####");
			scriptBlock.add("\t");
			scriptBlock.add("### Remove controls ####");
			scriptBlock.add(tissueCode + "." + assayCode + ".unnamed.des.sample <- " + tissueCode + "." + assayCode + 
					".unnamed.des[!(" + tissueCode + "." + assayCode + ".unnamed.des$sample_type %in% c(\"QC-Blank\",\"QC-InternalStandard\",\"QC-Reference\")),]");
			scriptBlock.add("\t");
			scriptBlock.add("### Summarize unnamed data ####");
			scriptBlock.add(tissueCode + "." + assayCode + ".unnamed.des.sample.melt <- melt(" + tissueCode + "." + assayCode + 
					".unnamed.des.sample[,-1], id = c(\"sample_type\"))");
			scriptBlock.add(tissueCode + "." + assayCode + ".unnamed.des.sample.summary <- " + tissueCode + "." + assayCode + 
					".unnamed.des.sample.melt %>% group_by(sample_type, variable) %>% summarise(medianVal = median(value, na.rm = T), "
					+ "meanVal = mean(value, na.rm = T), stDev = sd(value, na.rm = T), pcMissing = 100*mean(is.na(value)))");
			scriptBlock.add(tissueCode + "." + assayCode + ".unnamed.des.sample.summary$RSD <- " + tissueCode + "." + assayCode + 
					".unnamed.des.sample.summary$stDev / " + tissueCode + "." + assayCode + ".unnamed.des.sample.summary$meanVal * 100");
			scriptBlock.add(tissueCode + "." + assayCode + ".unnamed.des.sample.summary$sample_type <- as.factor(" + tissueCode + "." + assayCode + 
					".unnamed.des.sample.summary$sample_type)");
			scriptBlock.add("\t");
			scriptBlock.add("### Histograms for unnamed data ####");
			scriptBlock.add("rsdPlot <- ggplot(" + tissueCode + "." + assayCode + 
					".unnamed.des.sample.summary, aes(RSD, colour = sample_type, fill = sample_type)) + "
					+ "geom_histogram(binwidth = 5, alpha=0.6, aes(y=..density..), boundary = 0) + facet_wrap(~sample_type) + theme(");
			scriptBlock.add("  legend.position=\"none\", panel.spacing = unit(0.1, \"lines\"), strip.text.x = element_text(size = 8)");
			scriptBlock.add(") + xlab(\"\") + ggtitle(\"RSD distribution by sample type for unnamed compounds, " + tissueCodeName + ", " + assayMethod + "\")");
			scriptBlock.add("ggsave(\"" + experimentId + "-" + tissueCodeName + "-" + assayMethod + "-unnamed-RSD-histogram.png\", plot = rsdPlot,  width = 14, height = 8.5)");
			scriptBlock.add("\t");
			scriptBlock.add("missingPlot <- ggplot(" + tissueCode + "." + assayCode + ".unnamed.des.sample.summary, "
					+ "aes(pcMissing, colour = sample_type, fill = sample_type)) + geom_histogram(binwidth = 5, alpha=0.6, aes(y=..density..), boundary = 0) "
					+ "+ facet_wrap(~sample_type) + theme(");
			scriptBlock.add("  legend.position=\"none\", panel.spacing = unit(0.1, \"lines\"), strip.text.x = element_text(size = 8)");
			scriptBlock.add(") + xlab(\"\") + ggtitle(\"% Missing distribution by sample type for unnamed compounds, " + tissueCodeName + ", " + assayMethod + "\")");
			scriptBlock.add("ggsave(\"" + experimentId + "-" + tissueCodeName + "-" + assayMethod + 
					"-unnamed-Missing-histogram.png\", plot = missingPlot,  width = 14, height = 8.5)");
			scriptBlock.add("\t");
			scriptBlock.add("### Write out summary for unnamed data ####");
			scriptBlock.add(tissueCode + "." + assayCode + ".unnamed.des.sample.summary.4out <- " + tissueCode + "." + assayCode + ".unnamed.des.sample.summary");
			scriptBlock.add(tissueCode + "." + assayCode + ".unnamed.des.sample.summary.4out$Tissue <- \"" + tissueCodeName + "\"");
			scriptBlock.add(tissueCode + "." + assayCode + ".unnamed.des.sample.summary.4out$Method <- \"" + assayMethod + "\"");
			scriptBlock.add("write.table(" + tissueCode + "." + assayCode + ".unnamed.des.sample.summary.4out, file = \"" 
					+ experimentId + "-" + tissueCodeName + "-" + assayMethod + "-unnamed-QC-summary.txt\", quote = F, sep = \"\t\", na = \"\", row.names = F)");
			
			scriptBlock.add("# Clear workspace ####");
			scriptBlock.add("rm(list = ls())");
		}		
		String rScriptFileName = experimentId + "-QC4MoTrPAC" + FIOUtils.getTimestamp() + ".R";
		Path outputPath = Paths.get(
				baseDir.getAbsolutePath(), rScriptFileName);
		try {
		    Files.write(outputPath, 
		    		scriptBlock,
		            StandardCharsets.UTF_8,
		            StandardOpenOption.CREATE, 
		            StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
		    e.printStackTrace();
		}
	}
	
}
























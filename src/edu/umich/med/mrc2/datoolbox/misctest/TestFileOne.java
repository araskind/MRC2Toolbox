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

package edu.umich.med.mrc2.datoolbox.misctest;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;

import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.FilePreferencesFactory;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.utils.FIOUtils;

public class TestFileOne {

	public static String dataDir = "." + File.separator + "data" + File.separator;
	private static String dbHome = dataDir + "database" + File.separator + "CefAnalyzerDB";
	public static void main(String[] args) {

		System.setProperty("java.util.prefs.PreferencesFactory", 
				FilePreferencesFactory.class.getName());
		System.setProperty(FilePreferencesFactory.SYSTEM_PROPERTY_FILE, 
				MRC2ToolBoxCore.configDir + "MRC2ToolBoxPrefs.txt");
		MRC2ToolBoxConfiguration.initConfiguration();

		try {
			generateQcanvasHtmlMainPage();
			// formatDate();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void formatDate() {
		SimpleDateFormat dateTimeFormat = new SimpleDateFormat("MM/DD/YYYY HH:MM:ss");
		System.out.println(dateTimeFormat.format(new Date()));
	}
	
	private static void generateQcanvasHtmlMainPage() {
		
		//	Path basePath = Paths.get("O:\\DataAnalysis\\EX01263 - PASS 1A 1C 18mo\\A003 - Untargeted\\Documents");
		String assay = "A003";
		//	String assay = "A049";
		Path basePath = 
				Paths.get("O:\\_QCANVAS\\EX01263\\" + assay);
		List<Path> dirPathList = null;
		List<String>contents = new ArrayList<String>();
		contents.add("<head><link rel=\"stylesheet\" href=\"https://unpkg.com/simpledotcss@2.0.0/simple-v1.css\"></head>");
		contents.add("<html><body>");
		contents.add("<base target=\"_blank\">");
		contents.add("<H1>" + assay + " - analysis QCaNVaS processing results</H1>");
		try {
			dirPathList = Files.find(basePath, 8, (p, bfa) -> bfa.isDirectory()).collect(Collectors.toList());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String tissue = "";
		for(Path dp : dirPathList) {
			
			int level = dp.getNameCount();
			if(level > 3 && level <= 7) {
				if(level == 4) {
					tissue = dp.getName(3).toString();
					contents.add("<H" + (level - 2 ) + ">" + dp.getName(level-1) + "</H" + (level - 2) + ">");
				}
				else {
					contents.add("<H" + (level - 2 ) + "><FONT COLOR=\"RED\">" + tissue +" | </FONT>" + dp.getName(level-1) + "</H" + (level - 2) + ">");
				}
			}
			if(level == 8 && dp.getName(level-1).toString().equals("Plots")) {
				
				List<Path> plotList = new ArrayList<Path>();
				try {
					plotList = Files.list(dp).collect(Collectors.toList());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				List<String>plotLinkList = new ArrayList<String>();
				List<String>missingLinkList = new ArrayList<String>();
				String missingListHeader = null;
				for(Path pp : plotList) {
					
					if(pp.toString().toLowerCase().endsWith("html") 
							|| pp.toString().toLowerCase().endsWith("png")) {
						
						plotLinkList.add("<LI><A HREF=\"" 
								+ pp.subpath(3, pp.getNameCount()).toString().replaceAll("\\\\", "/") + "\">" 
								+ pp.getName(pp.getNameCount()-1) + "</A></LI>");
					}
					else {
						if(pp.toFile().isDirectory()) {
							
							missingListHeader = "<H6>" + pp.getName(8) + "</H6>";
							List<Path> missingPlotList = new ArrayList<Path>();
							try {
								missingPlotList = Files.list(pp).collect(Collectors.toList());
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							for(Path mp : missingPlotList) {
								
								if(mp.toString().toLowerCase().endsWith("html")) {
									
									missingLinkList.add("<LI><A HREF=\"" 
											+ mp.subpath(3, mp.getNameCount()).toString().replaceAll("\\\\", "/") + "\">" 
											+ mp.getName(mp.getNameCount()-1) + "</A></LI>");
								}
							}
						}
					}			
				}				
				contents.add("<UL>");
				contents.addAll(plotLinkList);
				contents.add("</UL>");
				if(missingListHeader != null && !missingLinkList.isEmpty()) {
					
					contents.add(missingListHeader);
					contents.add("<UL>");
					contents.addAll(missingLinkList);
					contents.add("</UL>");
				}
			}
			//	System.out.println(level + " > " + dp.toString());
		}		
		contents.add("</body></html>");
		
		Path outputPath = Paths.get("E:\\DataAnalysis\\_Reports\\EX01263", assay + "-QCANVAS-output.html");
		try {
			Files.write(outputPath, 
					contents, 
					StandardCharsets.UTF_8,
					StandardOpenOption.CREATE, 
					StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void exctractNormalizationSummaries() {
		
		String assay = "A003";
	//	String assay = "A049";
		Path basePath = 
				Paths.get("O:\\_QCANVAS\\EX01263\\" + assay);
		
		List<Path> summaryList = FIOUtils.findFilesByName(basePath, "normalization_summary.txt");
		Map<Path,Map<NormalizationFields,String>>normDataMap = 
				new TreeMap<Path,Map<NormalizationFields,String>>();
		for(Path sp : summaryList) {
			
			Map<NormalizationFields,String>normData = parseNormalizationSummary(sp);
			normDataMap.put(sp, normData);
		}
		//	Save results to single file
		List<String>output = new ArrayList<String>();
		
		int maxDepth = normDataMap.keySet().stream().mapToInt(p -> p.getNameCount()).max().getAsInt();
		
		String[] strArray = new String[maxDepth];
		Arrays.fill(strArray, "Path");
		List<String> header = new ArrayList<String>();
		header.addAll(Arrays.asList(strArray));
		for(NormalizationFields f : NormalizationFields.values())
			header.add(f.getName());
		
		output.add(StringUtils.join(header, "\t"));
		
		for(Entry<Path,Map<NormalizationFields,String>>entry : normDataMap.entrySet()) {
			
			Path ePath = entry.getKey();
			int depth = ePath.getNameCount();
			List<String>line = new ArrayList<String>();
			for(int i=0; i<maxDepth; i++) {
				
				if(i<depth)
					line.add(ePath.getName(i).toString());
				else
					line.add(" ");					
			}
			for(NormalizationFields f : NormalizationFields.values())				
				line.add(entry.getValue().get(f));			
			
			output.add(StringUtils.join(line, "\t"));
		}
		Path outputPath = Paths.get("E:\\DataAnalysis\\_Reports\\EX01263", assay + "-normalizationSummary.txt");
		try {
			Files.write(
					outputPath, 
					output, 
					StandardCharsets.UTF_8, 
					StandardOpenOption.CREATE,
					StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static Map<NormalizationFields,String>parseNormalizationSummary(Path normalizationSummaryFilePath){
		
		Map<NormalizationFields,String>normData = new TreeMap<NormalizationFields,String>();
		List<String>lines = null;
		try {
			lines = Files.readAllLines(normalizationSummaryFilePath);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		boolean normBlock = false;
		for(int i=0; i< lines.size(); i++) {
			
			String line = lines.get(i);
			if(line.contains("<!--------- raw data --------->"))
				continue;
			
			if(line.contains("<!--------- SERRF --------->")) {
				normBlock = true;
				continue;
			}
			for(NormalizationFields f : NormalizationFields.values()) {
				
				if(line.startsWith(f.getName() + ":")) {
					
					String value = line.replaceAll(f.getName() + ":\\s?", "").trim().replaceAll("\\.$", "");
					if(normBlock) {
						
						if(f.equals(NormalizationFields.AVG_QC_RSD))
							normData.put(NormalizationFields.AVG_QC_RSD_NORM, value);
						
						if(f.equals(NormalizationFields.NUM_COMPOUNDS_BELOW_RSD))
							normData.put(NormalizationFields.NUM_COMPOUNDS_BELOW_RSD_NORM, value);
					}
					else {						
						normData.put(f, value);
					}					
				}
			}			
		}		
		return normData;
	}
	
	private enum NormalizationFields {
		
		NUM_COMPOUNDS("Number of compounds"),
		NUM_SAMPLES("Number of samples"),
		NUM_QCS("Number of QCs"),
		NUM_BATCHES("Number of batches"),
		AVG_QC_RSD("Average QC RSD"),
		NUM_COMPOUNDS_BELOW_RSD("Number of compounds less than 20% QC RSD"),
		AVG_QC_RSD_NORM("Average QC RSD normalized"),
		NUM_COMPOUNDS_BELOW_RSD_NORM("Number of compounds less than 20% QC RSD normalized"),
		;

		private final String name;

		NormalizationFields(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

		public String toString() {
			return name;
		}

	}

}



















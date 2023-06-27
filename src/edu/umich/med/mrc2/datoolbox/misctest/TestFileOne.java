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
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.FilePreferencesFactory;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;

public class TestFileOne {

	public static String dataDir = "." + File.separator + "data" + File.separator;
	private static String dbHome = dataDir + "database" + File.separator + "CefAnalyzerDB";
	private static String dbUser = "CefAnalyzer";
	private static String dbPassword = "CefAnalyzer";

//	public static final String pubchemCidUrl = "https://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/cid/";
//	private static final IChemObjectBuilder builder = SilentChemObjectBuilder.getInstance();
//	private static final SmilesParser smipar = new SmilesParser(builder);
//	private static final SmilesGenerator smilesGenerator = new SmilesGenerator(SmiFlavor.Isomeric);
//	private static final InChITautomerGenerator tautgen = new InChITautomerGenerator(InChITautomerGenerator.KETO_ENOL);
//	private static final MDLV2000Reader mdlReader = new MDLV2000Reader();
//	private static InChIGeneratorFactory igfactory;
//	private static InChIGenerator inChIGenerator;
//	private static Aromaticity aromaticity;
//	private static final DecimalFormat intensityFormat = new DecimalFormat("###");
	
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
		Path basePath = 
				Paths.get("O:\\_QCANVAS\\EX01263\\A003");
		List<Path> dirPathList = null;
		List<String>contents = new ArrayList<String>();
		contents.add("<head><link rel=\"stylesheet\" href=\"https://unpkg.com/simpledotcss@2.0.0/simple-v1.css\"></head>");
		contents.add("<html><body>");
		contents.add("<base target=\"_blank\">");
		contents.add("<H1>A003 - RP Untargeted analysis QCaNVaS processing results</H1>");
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
		
		Path outputPath = Paths.get("E:\\DataAnalysis\\_Reports\\EX01263", "A003-QCANVAS-output.html");
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
}



















/*******************************************************************************
 *
duplicateDataUploadDirectoryForResultsCorrection4PreCovidPlasma * (C) Copyright 2018-2026 MRC2 (http://mrc2.umich.edu).
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

package edu.umich.med.mrc2.datoolbox.motrpac;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.compress.utils.FileNameUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.file.PathUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.openscience.cdk.inchi.InChIGenerator;
import org.openscience.cdk.inchi.InChIGeneratorFactory;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.io.MDLV2000Reader;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.smiles.SmilesParser;

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.ExperimentalSample;
import edu.umich.med.mrc2.datoolbox.data.WorklistItem;
import edu.umich.med.mrc2.datoolbox.data.enums.DataExportFields;
import edu.umich.med.mrc2.datoolbox.data.enums.MoTrPACRawDataManifestFields;
import edu.umich.med.mrc2.datoolbox.data.enums.MoTrPACmetaboliteMetaDataFields;
import edu.umich.med.mrc2.datoolbox.main.ReferenceSamplesManager;
import edu.umich.med.mrc2.datoolbox.main.config.DefaultFormatStore;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.utils.CompressionUtils;
import edu.umich.med.mrc2.datoolbox.utils.DelimitedTextParser;
import edu.umich.med.mrc2.datoolbox.utils.FIOUtils;
import edu.umich.med.mrc2.datoolbox.utils.LIMSReportingUtils;
import edu.umich.med.mrc2.datoolbox.utils.filefilter.DirectoryFileFilterIE;

public class MoTrPACUtils {
	
	public static String dataDir = "." + File.separator + "data" + File.separator;

	public static final String pubchemCidUrl = "https://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/cid/";
	private static final IChemObjectBuilder builder = SilentChemObjectBuilder.getInstance();
	private static final SmilesParser smipar = new SmilesParser(builder);
	private static final MDLV2000Reader molReader  = new MDLV2000Reader();
	private static InChIGeneratorFactory igfactory;
	private static InChIGenerator inChIGenerator;
	private static final DecimalFormat intensityFormat = new DecimalFormat("###");
	
	private static final DateFormat acquisitionDateFormat = 
			new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
	
	public enum AssayModes {
		
		RPPOS("Waters-186003589", "_", 15), 
		RPNEG("Waters-186003589", "_", 15), 
		IONPNEG("Agilent-759700-902", "_US00000000-B", 5);

		private final String columnName;
		private final String counterSuffix;
		private final int counterDigits;

		AssayModes(String columnName, String counterSuffix, int counterDigits) {
			this.columnName = columnName;
			this.counterSuffix = counterSuffix;
			this.counterDigits = counterDigits;
		}

		public String getColumnName() {
			return columnName;
		}
		
		public String getCounterSuffix() {
			return counterSuffix;
		}
		
		public int getCounterDigits() {
			return counterDigits;
		}
	}


	public static void addExtractionDatesAndColumnIdsToManifest(
			File completeManifestFile,
			File commonFileListFile,
			File extrtactionDatesMapFile,
			File batchManifestsBaseDir,
			File columnChangeDatesFile,
			AssayModes assayMode,
			int nextColumnId) {

		List<WorklistItem>worklistItems = parseCompleteManifest(completeManifestFile);
        List<String> commonFiles = new ArrayList<>();
        try {
			commonFiles = Files.readAllLines(
					commonFileListFile.toPath(), Charset.forName("ISO-8859-1"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        final List<String>cfiles = commonFiles;
		worklistItems = worklistItems.stream().filter(wi -> cfiles.contains(wi.getDataFileName()))
				.collect(Collectors.toList());
		
		//	Read individual batch manifests and assign batch and extraction dates
		List<WorklistItem>wklWithExtractionDates = parseBatchManifests(
				extrtactionDatesMapFile, batchManifestsBaseDir);
		
		for(WorklistItem item : worklistItems) {	
            
            Optional<WorklistItem>match = wklWithExtractionDates.stream().
                    filter(wi -> wi.getDataFileName().equals(item.getDataFileName())).findFirst();
            if(match.isPresent()) {
                item.setProperty(MoTrPACRawDataManifestFields.MOTRPAC_EXTRACTION_DATE.getName(), 
                        match.get().getProperty(MoTrPACRawDataManifestFields.MOTRPAC_EXTRACTION_DATE.getName()));
            }
        }
		addColumnIdsToWorklistItems(worklistItems, columnChangeDatesFile, assayMode, nextColumnId);
		
		List<String> lines = new ArrayList<>();		
		List<String> line = new ArrayList<>();
		for (MoTrPACRawDataManifestFields field : MoTrPACRawDataManifestFields.values())
			line.add(field.getName());
		
		lines.add(StringUtils.join(line, MRC2ToolBoxConfiguration.getTabDelimiter()));
		
		for(WorklistItem wklItem : worklistItems) {
            
			line.clear();
			for (MoTrPACRawDataManifestFields field : MoTrPACRawDataManifestFields.values()) {
				
				if (field.equals(MoTrPACRawDataManifestFields.MOTRPAC_RAW_FILE)) {
					line.add(wklItem.getDataFileName() + ".zip");	//	Add .zip suffix to match raw file names in manifest
				}
				else if (field.equals(MoTrPACRawDataManifestFields.MOTRPAC_ACQUISITION_DATE)) {
                    
                    Date injTs = wklItem.getTimeStamp();
                    line.add(acquisitionDateFormat.format(injTs));
                }
				else if(field.equals(MoTrPACRawDataManifestFields.MOTRPAC_MS_MODE)) {
                    
                    String columnId = wklItem.getProperty(field.getName());
                    if (columnId == null)
                        columnId = "";
                    
                    line.add(assayMode.name());
                }
				else {	
					String value = wklItem.getProperty(field.getName());
					if (value == null)
						value = "";
					
					line.add(value);
                }
			}
			lines.add(StringUtils.join(line, MRC2ToolBoxConfiguration.getTabDelimiter()));
        }
		Path outputPath = Paths.get(completeManifestFile.getParent(),
				FileNameUtils.getBaseName(completeManifestFile.getName()) 
				+ "_withExtractionDates.txt");
		try {
		    Files.write(outputPath, 
		    		lines,
		            StandardCharsets.UTF_8,
		            StandardOpenOption.CREATE, 
		            StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
		    e.printStackTrace();
		}
	}
	
	public static List<WorklistItem>parseBatchManifests(
			File extrtactionDatesMapFile,
			File batchManifestsBaseDir) {
        List<WorklistItem>worklistItems = new ArrayList<>();
        String[][] extractionDatesMapData = null;
        try {
        	extractionDatesMapData = DelimitedTextParser.parseTextFileWithEncoding(
        			extrtactionDatesMapFile, MRC2ToolBoxConfiguration.getTabDelimiter());
        } catch (IOException e) {
            e.printStackTrace();
        }
        for(int i=0; i<extractionDatesMapData.length; i++) {
        	
        	String manifestName = extractionDatesMapData[i][0];
        	String extractionDateString = extractionDatesMapData[i][1];
        	File batchManifestFile = 
        			Paths.get(batchManifestsBaseDir.getAbsolutePath(), manifestName).toFile();
        	List<WorklistItem>batchWkl = parseCompleteManifest(batchManifestFile);
			batchWkl.forEach(wi -> wi.setProperty(
					MoTrPACRawDataManifestFields.MOTRPAC_EXTRACTION_DATE.getName(), extractionDateString));
			worklistItems.addAll(batchWkl);
        }
        return worklistItems;
	}
	
	public static List<WorklistItem>parseCompleteManifest(File completeManifestFile) {
        
        List<WorklistItem>worklistItems = new ArrayList<>();
        String[][] completeManifestData = null;
        try {
        	completeManifestData = DelimitedTextParser.parseTextFileWithEncoding(
        			completeManifestFile, MRC2ToolBoxConfiguration.getTabDelimiter());
        } catch (IOException e) {
            e.printStackTrace();
        }
        String[] header = completeManifestData[0];
		for (int i = 1; i < completeManifestData.length; i++) {

			WorklistItem item = new WorklistItem();
			for (int j = 0; j < header.length; j++) {

				if (header[j].equals(MoTrPACRawDataManifestFields.MOTRPAC_RAW_FILE.getName())) {
					item.setDataFile(new DataFile(completeManifestData[i][j]));
				} else if (header[j].equals(DataExportFields.INJECTION_TIME.getName())) {
					
					Date injTs = new Date();
					try {
						injTs = DefaultFormatStore.getDefaultTimeStampFormat().parse(completeManifestData[i][j]);
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					item.setTimeStamp(injTs);
				} else{
					item.setProperty(header[j], completeManifestData[i][j]);
				} 
			}
			worklistItems.add(item);
		}	
        return worklistItems;
	}
	
	public static void addColumnIdsToWorklistItems(
			List<WorklistItem> worklistItems,
			File columnIdMapFile,
			AssayModes assayMode,
			int nextColumnId) {
		
		String[][] columnIdMapData = null;
		try {
			columnIdMapData = DelimitedTextParser.parseTextFileWithEncoding(columnIdMapFile,
					MRC2ToolBoxConfiguration.getTabDelimiter());
		} catch (IOException e) {
			e.printStackTrace();
		}
		SimpleDateFormat sdf = new SimpleDateFormat("M/d/yyyy");
		Set<Date>timeline = new TreeSet<>();
		for (int i = 0; i < columnIdMapData.length; i++) {

			String dateString = columnIdMapData[i][0];
			Date date = null;
			try {
				date = sdf.parse(dateString);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(date != null)
				timeline.add(date);
		}
		NavigableMap<Date,String>dateColumnMap = new TreeMap<>();
		for (Date date : timeline) {

			String columnId = assayMode.getColumnName() + assayMode.getCounterSuffix()
					+ StringUtils.leftPad(Integer.toString(nextColumnId), assayMode.getCounterDigits(), "0");
			dateColumnMap.put(date, columnId);
			nextColumnId++;
		}
		for (WorklistItem item : worklistItems) {
			
			Date injDate = item.getTimeStamp();
			Optional<Entry<Date, String>> match = dateColumnMap.entrySet().stream()
					.filter(e -> e.getKey().compareTo(injDate) >= 0).findFirst();
			if (match.isPresent()) {
				item.setProperty(MoTrPACRawDataManifestFields.MOTRPAC_LC_COLUMN_ID.getName(), 
						dateColumnMap.lowerEntry(match.get().getKey()).getValue());
			}
			else
				System.err.println("No column id found for date "
						+ DefaultFormatStore.getDefaultTimeStampFormat().format(injDate));
		}
	}
	
	//	File manifestMap = new File("Y:\\DataAnalysis\\_Reports\\_MoTrPAC\\manifest_map.txt");
	//	Path outputPath = Paths.get("Y:\\DataAnalysis\\_Reports\\_MoTrPAC\\BatchRunDates.txt");
	public static void extractBatchRunDatesFromManifests(
			File manifestMap,
			File output) {
		
		Pattern pattern = Pattern.compile("BATCH\\d+", Pattern.CASE_INSENSITIVE);
		String[][] manifestMapData = null;
		try {
			manifestMapData = DelimitedTextParser.parseTextFileWithEncoding(manifestMap, '\t');
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		List<BatchMetadataObject>dataList = new ArrayList<>();
		for(int i=0; i<manifestMapData.length; i++) {
			
			String experimentId = manifestMapData[i][0];
			String assay = manifestMapData[i][1];
			String batchName = "";
			List<Path>manifestList = 
					FIOUtils.findFilesByNameContainingString(Paths.get(manifestMapData[i][2]), "_MANIFEST");
			for(Path manifestPath : manifestList) {
				
				Matcher bMatcher = pattern.matcher(manifestPath.getFileName().toString());
				if(bMatcher.find())
					batchName = bMatcher.group();
				
				
				NavigableSet<Date>dateSet = extractDateSetFromManifest(manifestPath);
				BatchMetadataObject bmo = new BatchMetadataObject(
						experimentId, assay, batchName, dateSet.first(), dateSet.last());
				
				dataList.add(bmo);
			}			
		}
		List<String>dataListOutput = new ArrayList<>();
		List<String>line = new ArrayList<>();
		line.add("Experiment");
		line.add("Assay");
		line.add("Batch");
		line.add("Run started");
		line.add("Run ended");
		dataListOutput.add(StringUtils.join(line, "\t"));
		for(BatchMetadataObject bmo : dataList) {
			
			line.clear();
			line.add(bmo.getExperimentId());
			line.add(bmo.getAssay());
			line.add(bmo.getBatchName());
			line.add(DefaultFormatStore.getDefaultTimeStampFormat().format(bmo.getRunStart()));
			line.add(DefaultFormatStore.getDefaultTimeStampFormat().format(bmo.getRunEnd()));
			dataListOutput.add(StringUtils.join(line, "\t"));		
		}		
		try {
		    Files.write(output.toPath(), 
		    		dataListOutput,
		            StandardCharsets.UTF_8,
		            StandardOpenOption.CREATE, 
		            StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
		    e.printStackTrace();
		}
	}
	
	public static NavigableSet<Date> extractDateSetFromManifest(Path manifestPath) {
		
		NavigableSet<Date>dateSet = new TreeSet<>();
		String[][] manifestMapData = null;
		try {
			manifestMapData = DelimitedTextParser.parseTextFileWithEncoding(manifestPath.toFile(), '\t');
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int timeColumnIndex = -1;
		for(int i=0; i<manifestMapData[0].length; i++) {
			
			if(manifestMapData[0][i].equals(DataExportFields.INJECTION_TIME.getName())) {
				timeColumnIndex = i;
				break;
			}
		}
		for(int i=1; i<manifestMapData.length; i++) {
			
			String dateString = manifestMapData[i][timeColumnIndex];
			Date injTime = null;
	        try {
	        	injTime = DefaultFormatStore.getDefaultTimeStampFormat().parse(dateString);

	        } catch (ParseException e) {
	            System.err.println("Error parsing date: " + e.getMessage());
	        }
	        dateSet.add(injTime);
		}
		return dateSet;
	}

	//	File rootFolder = new File("Y:\\DataAnalysis\\_Reports\\EX01263 - PASS 1A 1C 18mo P20000245T C20000316E\\4BIC\\20251016FIX\\PASS1A-18");
	public static void collectFilePathsForReupload(File rootFolder) {
		
		List<String>lookupNames = new ArrayList<>(
				List.of(
					//"metadata_metabolites_named_",
					//"results_metabolites_named_",
					"file_manifest_2023"));
		Set<Path>pathList = new TreeSet<>();
		for(String lookupName : lookupNames)
			pathList.addAll(FIOUtils.findFilesByNameStartingWith(rootFolder.toPath(), lookupName)) ;
		
		List<String>pathnameList = pathList.stream().
				map(p -> p.toString()).collect(Collectors.toList());
		Path output = Paths.get(rootFolder.getAbsolutePath(), "PathList.txt");
		try {
		    Files.write(output, 
		    		pathnameList,
		            StandardCharsets.UTF_8,
		            StandardOpenOption.CREATE, 
		            StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
		    e.printStackTrace();
		}
	}
	

	public static void renameProcessedDirs(File destinationDir, String oldName, String newName) {

		Path newDirPath = Paths.get(destinationDir.getAbsolutePath());
		List<Path>processedDirs = FIOUtils.findDirectoriesByName(newDirPath, oldName);
		for(Path pd : processedDirs) {
			
			Path newProcessedPath = Paths.get(pd.getParent().toString(), newName);
			try {					
				Files.move(pd, newProcessedPath);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	//	File[] experimentFolders = new File[] {
	//			new File("Y:\\DataAnalysis\\_Reports\\EX01117 - MoTrPAC-1C\\4BIC\\20241023FIX\\PASS1A-06"),
	//			new File("Y:\\DataAnalysis\\_Reports\\EX01190 - MoTrPAC\\4BIC\\20241017FIX\\HUMAN\\T02"),
	//			new File("Y:\\DataAnalysis\\_Reports\\EX01242 - preCovid adipose Shipment W20000044X\\4BIC\\20241017FIX\\HUMAN\\T11"),
	//			new File("Y:\\DataAnalysis\\_Reports\\EX01263 - PASS 1A 1C 18mo P20000245T C20000316E\\4BIC\\PASS1A-18"),
	//	};
	//	Path outputPath = Paths.get("E:\\DataAnalysis\\_MOTRPAC_DEVEL\\allMoTrPACnamed.txt");
	public static void collectAllMetaboliteNames(Collection<File>experimentFolders, File outputFile) {
		

		TreeSet<String>allNames = new TreeSet<String>();
		for(File experiment : experimentFolders) {
			
			//	Process metadata_metabolites_named_ ... files
			List<Path> mdmnList = FIOUtils.findFilesByNameStartingWith(
					experiment.toPath(), "metadata_metabolites_named_");
						
			for(Path mdmnPath : mdmnList) {
					
				int metabColumn = -1;
				String[][] compoundMetaData = DelimitedTextParser.parseTextFile(
						mdmnPath.toFile(), MRC2ToolBoxConfiguration.getTabDelimiter());
				
				for(int i=0; i<compoundMetaData[0].length; i++) {
					
					if(compoundMetaData[0][i].equals("metabolite_name")) {
						metabColumn = i;
						break;
					}
				}
				for(int i=1; i<compoundMetaData.length; i++)					
					allNames.add(compoundMetaData[i][metabColumn]);								
			}
		}
		
		try {
		    Files.write(outputFile.toPath(), 
		    		allNames,
		            StandardCharsets.UTF_8,
		            StandardOpenOption.CREATE, 
		            StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
		    e.printStackTrace();
		}		
	}
	
	//	File renameMapFile = new File("E:\\DataAnalysis\\_MOTRPAC_DEVEL\\RefmetNameReplacementMap.txt");
	//	File[] experimentFolders = new File[] {
	//				new File("Y:\\DataAnalysis\\_Reports\\EX01190 - MoTrPAC\\4BIC\\20241017FIX\\HUMAN\\T02"),
	//			new File("Y:\\DataAnalysis\\_Reports\\EX01117 - MoTrPAC-1C\\4BIC\\20241023FIX\\PASS1A-06"),
	//			new File("Y:\\DataAnalysis\\_Reports\\EX01242 - preCovid adipose Shipment W20000044X\\4BIC\\20241017FIX\\HUMAN\\T11"),
	//			new File("Y:\\DataAnalysis\\_Reports\\EX01263 - PASS 1A 1C 18mo P20000245T C20000316E\\4BIC\\20241023FIX\\PASS1A-18"),
	//	};
	public static void fixRefMetNames(Collection<File>experimentFolders, File renameMapFile) {
				
		String[][] renameData = DelimitedTextParser.parseTextFile(
				renameMapFile, MRC2ToolBoxConfiguration.getTabDelimiter());
		
		Map<String,String>renameMap = new TreeMap<String,String>();
		for(int i=0; i<renameData.length; i++) 
			renameMap.put(renameData[i][0], renameData[i][1]);
		

		for(File experiment : experimentFolders) {
			
			//	Process metadata_metabolites_named_ ... files
			List<Path> mdmnList = FIOUtils.findFilesByNameStartingWith(
					experiment.toPath(), "metadata_metabolites_named_");
						
			for(Path mdmnPath : mdmnList) {
				
//				int metabColumn = -1;
				int refmetColumn = -1;
				String[][] compoundMetaData = DelimitedTextParser.parseTextFile(
						mdmnPath.toFile(), MRC2ToolBoxConfiguration.getTabDelimiter());
				
				for(int i=0; i<compoundMetaData[0].length; i++) {
					
//					if(compoundMetaData[0][i].equals("metabolite_name"))
//						metabColumn = i;
					
					if(compoundMetaData[0][i].equals("refmet_name"))
						refmetColumn = i;
				}
				for(int i=1; i<compoundMetaData.length; i++) {
					
//					String newName = renameMap.get(compoundMetaData[i][metabColumn]);
//					if(newName != null) 
//						compoundMetaData[i][metabColumn] = newName;
						
					String newRefMetName = renameMap.get(compoundMetaData[i][refmetColumn]);
					if(newRefMetName != null)	
						compoundMetaData[i][refmetColumn] = newRefMetName;					
				}
				//	Write out file with replaced names
				ArrayList<String>metadataOut = new ArrayList<String>();
				for(int i=0; i<compoundMetaData.length; i++)
					metadataOut.add(StringUtils.join(compoundMetaData[i], MRC2ToolBoxConfiguration.getTabDelimiter()));
				
				try {
				    Files.write(mdmnPath, 
				    		metadataOut,
				            StandardCharsets.UTF_8,
				            StandardOpenOption.CREATE, 
				            StandardOpenOption.TRUNCATE_EXISTING);
				} catch (IOException e) {
				    e.printStackTrace();
				}
			}	
			//	Process results_metabolites_named_ ... files
//			List<Path> resultsList = FIOUtils.findFilesByNameStartingWith(
//					experiment.toPath(), "results_metabolites_named_");
//						
//			for(Path resultPath : resultsList) {
//				
//				int metabColumn = -1;
//				String[][] resultsData = DelimitedTextParser.parseTextFile(
//						resultPath.toFile(), MRC2ToolBoxConfiguration.getTabDelimiter());
//				
//				for(int i=0; i<resultsData[0].length; i++) {
//					
//					if(resultsData[0][i].equals("metabolite_name")) {
//						metabColumn = i;
//						break;
//					}
//				}
//				for(int i=1; i<resultsData.length; i++) {
//					
//					String newName = renameMap.get(resultsData[i][metabColumn]);
//					if(newName != null)
//						resultsData[i][metabColumn] = newName;
//				}
//				//	Write out file with replaced names
//				ArrayList<String>metadataOut = new ArrayList<String>();
//				for(int i=0; i<resultsData.length; i++)
//					metadataOut.add(StringUtils.join(resultsData[i], MRC2ToolBoxConfiguration.getTabDelimiter()));
//				
//				try {
//				    Files.write(resultPath, 
//				    		metadataOut,
//				            StandardCharsets.UTF_8,
//				            StandardOpenOption.CREATE, 
//				            StandardOpenOption.TRUNCATE_EXISTING);
//				} catch (IOException e) {
//				    e.printStackTrace();
//				}
//			}
		}		
	}
	
	public static void createMultiTissueFixScriptGenInputMap(File partialInput, File bicDataDir, File outputFile) {
		
		String[][] inputData = DelimitedTextParser.parseTextFile(
				partialInput, MRC2ToolBoxConfiguration.getTabDelimiter());
		ArrayList<String>outputData = new ArrayList<String>();
		List<Path> manifestPathList = new ArrayList<Path>();
		List<Path> resultPathList = new ArrayList<Path>();
		
		for(int i=0; i<inputData.length; i++) {
			
			manifestPathList.clear();
			resultPathList.clear();
			
			String manifestPath = null;
			String resultPath = null;
			
			Path searchPath = Paths.get(bicDataDir.toPath().toString(), inputData[i][1], inputData[i][2]);
			manifestPathList = FIOUtils.findFilesByNameStartingWith(searchPath, "metadata_sample_");
			for(Path mp : manifestPathList) {
				
				for(Path part : mp) {
					 if(part.toString().equalsIgnoreCase(inputData[i][3])) {
						 manifestPath = mp.toString();
						 break;
					 }
				}
			}	
			resultPathList = FIOUtils.findFilesByNameStartingWith(searchPath, "results_metabolites_");
			for(Path rp : resultPathList) {
				
				for(Path part : rp) {
					 if(part.toString().equalsIgnoreCase(inputData[i][3])) {
						 resultPath = rp.toString();
						 break;
					 }
				}
			}
			String line = StringUtils.join(inputData[i], "\t");
			if(manifestPath == null || resultPath == null) {
				System.err.println(line + " not found");
			}
			else{
				outputData.add(line + "\t" + manifestPath + "\t" + resultPath);
			}
		}
		try {
		    Files.write(outputFile.toPath(), 
		    		outputData,
		            StandardCharsets.UTF_8,
		            StandardOpenOption.CREATE, 
		            StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
		    e.printStackTrace();
		}
	}

	public static void getFulPathsToQcanvasDirs(File dirNamesFile, File lookupDir, File outputFile) {
		
		String[][] inputData = DelimitedTextParser.parseTextFile(
				dirNamesFile, MRC2ToolBoxConfiguration.getTabDelimiter());
		ArrayList<String>outputData = new ArrayList<String>();
		List<Path> pathList = new ArrayList<Path>();
		for(int i=0; i<inputData.length; i++) {
			pathList.clear();
			pathList = FIOUtils.findDirectoriesByName(lookupDir.toPath(), inputData[i][0]);
			if(pathList.isEmpty()) {
				System.err.println(inputData[i][0] + " not found");
			}
			else {
				outputData.add(inputData[i][0] + "\t" + pathList.get(0).toString());
			}
		}
		try {
		    Files.write(outputFile.toPath(), 
		    		outputData,
		            StandardCharsets.UTF_8,
		            StandardOpenOption.CREATE, 
		            StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
		    e.printStackTrace();
		}
	}

	
	public static void duplicateDataUploadDirectoryForResultsCorrectionNoTruncExcludeOldBatch(
			File sourceDir,
			File destinationDir,
			String processingDateIdentifier,
			String excludeBatchIdentifier) {
		
		//	Copy excluding RAW directories
		String[] filters = new String[] {"RAW", excludeBatchIdentifier};
		DirectoryFileFilterIE filter = new DirectoryFileFilterIE(filters, true);
		try {
			FileUtils.copyDirectory(sourceDir, destinationDir, filter);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(destinationDir.exists()) {
			
			//	Rename PROCESSED_ directories 
			Path newDirPath = Paths.get(destinationDir.getAbsolutePath());
			List<Path>processedDirs = FIOUtils.findDirectoriesByNameStartingWith(newDirPath, "PROCESSED_");
			String processedFolderId  = "PROCESSED_" + processingDateIdentifier;
			for(Path pd : processedDirs) {
				
				Path newProcessedPath = Paths.get(pd.getParent().toString(), processedFolderId);
				try {					
					Files.move(pd, newProcessedPath);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}			
		}
	}
	
	public static void duplicateDataUploadDirectoryForResultsCorrection(
			File sourceDir,
			File destinationDir,
			String processingDateIdentifier) {
		
		//	Copy excluding RAW directories
		DirectoryFileFilterIE filter = new DirectoryFileFilterIE("RAW", true);
		try {
			FileUtils.copyDirectory(sourceDir, destinationDir, filter);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(destinationDir.exists()) {
			
			//	Rename PROCESSED_ directories 
			Path newDirPath = Paths.get(destinationDir.getAbsolutePath());
			List<Path>processedDirs = FIOUtils.findDirectoriesByNameStartingWith(newDirPath, "PROCESSED_");
			String processedFolderId  = "PROCESSED_" + processingDateIdentifier;
			for(Path pd : processedDirs) {
				
				Path newProcessedPath = Paths.get(pd.getParent().toString(), processedFolderId);
				try {					
					Files.move(pd, newProcessedPath);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				//	Truncate "results" files
				if(newProcessedPath.toFile().exists()) {
					
					List<Path>results = FIOUtils.findFilesByNameStartingWith(newProcessedPath, "results_metabolites_");
					for(Path res : results) {
						try {
							Files.write(res, new byte[0], StandardOpenOption.TRUNCATE_EXISTING);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}			
		}
	}
		
	public static void createMotrpacDataUploadDirectoryStructure(
			List<String>tissueTypes, 
			File parentDirectory,
			String batchDateIdentifier,
			String processingDateIdentifier,
			String studyPhase) {
		List<String>assayTypes = 
				new ArrayList<String>(Arrays.asList("IONPNEG", "RPNEG", "RPPOS"));
		try {
			LIMSReportingUtils.createMotrpacDataUploadDirectoryStructure(
					tissueTypes, 
					assayTypes, 
					parentDirectory,
					1,
					batchDateIdentifier,
					processingDateIdentifier,
					studyPhase);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void createMoTrPACFileManifest(
			List<String>tissueTypes, 
			File parentDirectory,
			String batchId,
			String processedFolderId,
			String manifestDate,
			boolean processRawFiles) {
		List<String>assayTypes = 
				new ArrayList<>(Arrays.asList("IONPNEG", "RPNEG", "RPPOS"));
		
		for(String tissue : tissueTypes) {
			
			for(String assay : assayTypes) {
				
				try {
					createMoTrPACTissueAssayManifestFile(
							tissue,
							assay,
							parentDirectory,
							batchId,
							processedFolderId,
							manifestDate,
							processRawFiles);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	public static void createMoTrPACTissueAssayManifestFile(
			String tissue,
			String assay,
			File parentDirectory,
			String batchId,
			String processedFolderId,
			String manifestDate,
			boolean processRawFiles) throws IOException {
		
		StringBuffer checkSumData = new StringBuffer();
		checkSumData.append("file_name,md5\n");
		Path batchPath = Paths.get(parentDirectory.getAbsolutePath(), tissue, assay, batchId);
		Path processedPath = Paths.get(batchPath.toString(), processedFolderId);
		Path namedDirPath = Paths.get(processedPath.toString(), "NAMED");
		List<Path> pathList = Files.find(namedDirPath,
				1, (filePath, fileAttr) -> (filePath.toString().toLowerCase().endsWith(".txt"))).
			collect(Collectors.toList());
		for(Path filePath : pathList) {
			String zipHash = DigestUtils.sha256Hex(
					new FileInputStream(filePath.toString()));
			String localPath = processedFolderId + File.separator + "NAMED" + File.separator + filePath.toFile().getName();
			checkSumData.append(localPath + "," + zipHash + "\n");
		}
		Path unnamedDirPath = Paths.get(processedPath.toString(), "UNNAMED");
		pathList = Files.find(unnamedDirPath,
				1, (filePath, fileAttr) -> (filePath.toString().toLowerCase().endsWith(".txt"))).
			collect(Collectors.toList());
		for(Path filePath : pathList) {
			String zipHash = DigestUtils.sha256Hex(
					new FileInputStream(filePath.toString()));
			String localPath = processedFolderId + File.separator + "UNNAMED" + File.separator + filePath.toFile().getName();
			checkSumData.append(localPath + "," + zipHash + "\n");
		}
		pathList = Files.find(processedPath,
				1, (filePath, fileAttr) -> (filePath.toString().toLowerCase().contains("metadata_failedsamples_"))).
			collect(Collectors.toList());
		for(Path filePath : pathList) {
			String zipHash = DigestUtils.sha256Hex(
					new FileInputStream(filePath.toString()));
			String localPath = processedFolderId + File.separator + filePath.toFile().getName();
			checkSumData.append(localPath + "," + zipHash + "\n");
		}
		pathList = Files.find(batchPath,
				1, (filePath, fileAttr) -> (filePath.toString().toLowerCase().contains("metadata_phase"))).
			collect(Collectors.toList());
		for(Path filePath : pathList) {
			String zipHash = DigestUtils.sha256Hex(
					new FileInputStream(filePath.toString()));
			String localPath = processedFolderId + File.separator + filePath.toFile().getName();
			checkSumData.append(localPath + "," + zipHash + "\n");
		}
		if(processRawFiles) {
			
			Path rawDirPath = Paths.get(parentDirectory.getAbsolutePath(), tissue, assay, batchId, "RAW");
			pathList = Files.find(rawDirPath,
					1, (filePath, fileAttr) -> (filePath.toString().toLowerCase().endsWith(".zip"))).
				collect(Collectors.toList());
			for(Path filePath : pathList) {
				String zipHash = DigestUtils.sha256Hex(
						new FileInputStream(filePath.toString()));
				String localPath = filePath.toFile().getName().replace(".zip", "");
				checkSumData.append(localPath + "," + zipHash + "\n");
			}
		}
		String withCorrectedSeparator = checkSumData.toString().replaceAll("\\\\", "/");
		File manifestFile = Paths.get(parentDirectory.getAbsolutePath(), 
				tissue, assay, batchId, "file_manifest_" + manifestDate + ".csv").toFile();
		FileUtils.writeStringToFile(manifestFile, withCorrectedSeparator, Charset.defaultCharset());
	}
	
	public static void createMoTrPACTissueAssayNoRawManifestFile(
			String tissue,
			String assay,
			File parentDirectory,
			String batchId,
			String processedFolderId,
			String manifestDate) throws IOException {
		
		StringBuffer checkSumData = new StringBuffer();
		checkSumData.append("file_name,md5\n");
		Path batchPath = Paths.get(parentDirectory.getAbsolutePath(), tissue, assay, batchId);
		Path processedPath = Paths.get(batchPath.toString(), processedFolderId);
		Path namedDirPath = Paths.get(processedPath.toString(), "NAMED");
		List<Path> pathList = Files.find(namedDirPath,
				1, (filePath, fileAttr) -> (filePath.toString().toLowerCase().endsWith(".txt"))).
			collect(Collectors.toList());
		for(Path filePath : pathList) {
			String zipHash = DigestUtils.sha256Hex(
					new FileInputStream(filePath.toString()));
			String localPath = processedFolderId + File.separator + "NAMED" + File.separator + filePath.toFile().getName();
			checkSumData.append(localPath + "," + zipHash + "\n");
		}
		Path unnamedDirPath = Paths.get(processedPath.toString(), "UNNAMED");
		pathList = Files.find(unnamedDirPath,
				1, (filePath, fileAttr) -> (filePath.toString().toLowerCase().endsWith(".txt"))).
			collect(Collectors.toList());
		for(Path filePath : pathList) {
			String zipHash = DigestUtils.sha256Hex(
					new FileInputStream(filePath.toString()));
			String localPath = processedFolderId + File.separator + "UNNAMED" + File.separator + filePath.toFile().getName();
			checkSumData.append(localPath + "," + zipHash + "\n");
		}
		pathList = Files.find(processedPath,
				1, (filePath, fileAttr) -> (filePath.toString().toLowerCase().contains("metadata_failedsamples_"))).
			collect(Collectors.toList());
		for(Path filePath : pathList) {
			String zipHash = DigestUtils.sha256Hex(
					new FileInputStream(filePath.toString()));
			String localPath = processedFolderId + File.separator + filePath.toFile().getName();
			checkSumData.append(localPath + "," + zipHash + "\n");
		}
		pathList = Files.find(batchPath,
				1, (filePath, fileAttr) -> (filePath.toString().toLowerCase().contains("metadata_phase"))).
			collect(Collectors.toList());
		for(Path filePath : pathList) {
			String zipHash = DigestUtils.sha256Hex(
					new FileInputStream(filePath.toString()));
			String localPath = processedFolderId + File.separator + filePath.toFile().getName();
			checkSumData.append(localPath + "," + zipHash + "\n");
		}
		File manifestFile = Paths.get(parentDirectory.getAbsolutePath(), tissue, assay, batchId, "file_manifest_noRaw" + manifestDate + ".csv").toFile();
		FileUtils.writeStringToFile(manifestFile, checkSumData.toString(), Charset.defaultCharset());
	}
	
	public static void compressRawDataFromMutibatchRun(
			String includeFileListFile, 
			List<String>sourceFolders, 
			String destinationFolder) {
		
		List<String> includeFileList = new ArrayList<String>();
		Path includeFileListPath = Paths.get(includeFileListFile);
		try {
			includeFileList = Files.readAllLines(includeFileListPath);
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		Path destinationPath = Paths.get(destinationFolder);
		for(String rawDataDirectory : sourceFolders) {
			
			Path sourcePath = Paths.get(rawDataDirectory);
			List<Path> pathList = new ArrayList<Path>();
			try {
				pathList = Files.find(sourcePath,
						1, (filePath, fileAttr) -> (filePath.toString().toLowerCase().endsWith(".d"))).
					collect(Collectors.toList());
			} catch (IOException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
			for(Path rdp : pathList) {
				
				String rawFileName = FilenameUtils.getBaseName(rdp.toString());
				if(includeFileList.contains(rawFileName)) {
					
					try {
						FileUtils.deleteDirectory(Paths.get(rdp.toString(), "Results").toFile());
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					File destination = Paths.get(destinationPath.toString(),
							FilenameUtils.getBaseName(rdp.toString()) + ".zip").toFile();
					try {
						CompressionUtils.zipFolder(rdp.toFile(), destination);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}								
				}
			}
		}		
	}
	
	public static void writeMetaboliteMetadataFile(
			Collection<Map<MoTrPACmetaboliteMetaDataFields,String>>metaboliteMetadata,
			Path metaboliteMetadataFilePath) {
		
		ArrayList<String>lines = new ArrayList<String>();
		ArrayList<String>header = new ArrayList<String>();
		for(MoTrPACmetaboliteMetaDataFields field : MoTrPACmetaboliteMetaDataFields.values())
			header.add(field.getName());
		
		lines.add(StringUtils.join(header, MRC2ToolBoxConfiguration.getTabDelimiter()));
		for(Map<MoTrPACmetaboliteMetaDataFields, String> mmd : metaboliteMetadata) {
			
			ArrayList<String>line = new ArrayList<String>();
			for(MoTrPACmetaboliteMetaDataFields field : MoTrPACmetaboliteMetaDataFields.values()) {
				
				String value = mmd.get(field);
				if(value != null)
					line.add(value);
				else
					line.add("");				
			}
			lines.add(StringUtils.join(line, MRC2ToolBoxConfiguration.getTabDelimiter()));
		}
		try {
			Files.write(metaboliteMetadataFilePath, 
					lines, 					
					StandardCharsets.UTF_8,
					StandardOpenOption.CREATE, 
					StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static Collection<Map<MoTrPACmetaboliteMetaDataFields,String>>
			parseMetaboliteMetadataFile(File metaboliteMetadataFile) throws Exception{
		
		Collection<Map<MoTrPACmetaboliteMetaDataFields,String>>metaboliteMetadata = 
				new ArrayList<Map<MoTrPACmetaboliteMetaDataFields,String>>();
		String[][] metaboliteData = null;
		try {
			metaboliteData =
				DelimitedTextParser.parseTextFileWithEncodingSkippingComments(
						metaboliteMetadataFile, MRC2ToolBoxConfiguration.getTabDelimiter(), ">");
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		if(metaboliteData == null) {
			throw new Exception("Unable to read metabolite data file!");
		}
		Map<MoTrPACmetaboliteMetaDataFields,Integer>columnMap = 
				new TreeMap<MoTrPACmetaboliteMetaDataFields,Integer>();
		String[]header = metaboliteData[0];
		for(int i=0; i<header.length; i++) {
			if(header[i].trim().isEmpty())
				continue;
			
			MoTrPACmetaboliteMetaDataFields field = 
					MoTrPACmetaboliteMetaDataFields.getOptionByUIName(header[i].trim());
			
			if(field != null)
				columnMap.put(field, i);
		}
		for(int i=1; i<metaboliteData.length; i++) {
			Map<MoTrPACmetaboliteMetaDataFields,String>mDataMap = new TreeMap<MoTrPACmetaboliteMetaDataFields,String>();
			for(Entry<MoTrPACmetaboliteMetaDataFields, Integer> col : columnMap.entrySet())				
				mDataMap.put(col.getKey(), metaboliteData[i][col.getValue()]);

			metaboliteMetadata.add(mDataMap);
		}
		return metaboliteMetadata;
	}

	private static void compressMoTrPACPreCovidMuscleRawDataFiles() throws IOException {
		
		//	Excluded list
		Path excludeFileListPath = Paths.get(
				"Y:\\DataAnalysis\\_Reports\\EX01094 - ADU870 10063 "
				+ "Clinical Skeletal Muscle Samples Pre COVID 2 19 2021"
				+ "\\4Upload\\_4BIC\\RAW\\exclude_file_list.txt");
		List<String> excludeFileList = new ArrayList<String>();
		try {
			excludeFileList = Files.readAllLines(excludeFileListPath);
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		Map<String,String>sourceDestinationMap = new TreeMap<String,String>();
		sourceDestinationMap.put(
				"Y:\\DataAnalysis\\_Reports\\EX01094 - ADU870 10063 Clinical "
				+ "Skeletal Muscle Samples Pre COVID 2 19 2021\\4Upload\\_4BIC\\RAW\\IONPNEG", 
				"Y:\\DataAnalysis\\_Reports\\EX01094 - ADU870 10063 Clinical "
				+ "Skeletal Muscle Samples Pre COVID 2 19 2021\\4Upload\\_4BIC\\T06\\IONPNEG\\BATCH1_20220404\\RAW");
		sourceDestinationMap.put(
				"Y:\\DataAnalysis\\_Reports\\EX01094 - ADU870 10063 Clinical "
				+ "Skeletal Muscle Samples Pre COVID 2 19 2021\\4Upload\\_4BIC\\RAW\\RPNEG", 
				"Y:\\DataAnalysis\\_Reports\\EX01094 - ADU870 10063 Clinical "
				+ "Skeletal Muscle Samples Pre COVID 2 19 2021\\4Upload\\_4BIC\\T06\\RPNEG\\BATCH1_20220404\\RAW");
		sourceDestinationMap.put(
				"Y:\\DataAnalysis\\_Reports\\EX01094 - ADU870 10063 Clinical "
				+ "Skeletal Muscle Samples Pre COVID 2 19 2021\\4Upload\\_4BIC\\RAW\\RPPOS", 
				"Y:\\DataAnalysis\\_Reports\\EX01094 - ADU870 10063 Clinical "
				+ "Skeletal Muscle Samples Pre COVID 2 19 2021\\4Upload\\_4BIC\\T06\\RPPOS\\BATCH1_20220404\\RAW");
				
		for(Entry<String, String> entry : sourceDestinationMap.entrySet()) {
			
			Path rawDataDirectory = Paths.get(entry.getKey());
			Path zippedRawFilesDirPath = Paths.get(entry.getValue());
			File checkSumFile = Paths.get(zippedRawFilesDirPath.toString(), " checksum.txt").toFile();			
			
			Collection<String>processed = new TreeSet<String>();
			TreeMap<File,Long> fileSizeMap = new TreeMap<File,Long>();
			TreeMap<File,String> fileHashMap = new TreeMap<File,String>();
					
			List<Path> pathList = Files.find(rawDataDirectory,
					1, (filePath, fileAttr) -> (filePath.toString().toLowerCase().endsWith(".d"))).
				collect(Collectors.toList());
			for(Path rdp : pathList) {
				
				FileUtils.deleteDirectory(Paths.get(rdp.toString(), "Results").toFile());
				String rawFileName = FilenameUtils.getBaseName(rdp.toString());
				if(!excludeFileList.contains(rawFileName)) {
					
					File destination = Paths.get(zippedRawFilesDirPath.toString(),
							FilenameUtils.getBaseName(rdp.toString()) + ".zip").toFile();
					try {
						CompressionUtils.zipFolder(rdp.toFile(), destination);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if(destination.exists()) {
						try {
							String zipHash = DigestUtils.sha256Hex(
									new FileInputStream(destination.getAbsolutePath()));

							fileHashMap.put(destination, zipHash);
						} catch (FileNotFoundException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						fileSizeMap.put(destination, destination.length());
						processed.add(rawFileName);
					}								
				}
			}					
			//	Write checksum file
			StringBuffer checkSumData = new StringBuffer();
			checkSumData.append("File name\t");
			checkSumData.append("SHA256\t");
			checkSumData.append("Size\n");
			for(Entry<File, String> csentry : fileHashMap.entrySet()) {
				
				if(fileSizeMap.get(csentry.getKey()) != null) {
					
					checkSumData.append(csentry.getKey().getName() +"\t");
					checkSumData.append(csentry.getValue() +"\t");
					checkSumData.append(Long.toString(fileSizeMap.get(csentry.getKey())) + "\n");
				}
			}
			try {
				FileUtils.writeStringToFile(checkSumFile, checkSumData.toString(), Charset.defaultCharset());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	
	private static void fixMetaboliteNames() throws Exception {
		
		File sourceDirectory = new File("Y:\\DataAnalysis\\_Reports\\EX00979 - PASS 1B\\UploadPrep docs\\metaboliteData2fix");
		String destinationDirName = "Y:\\DataAnalysis\\_Reports\\EX00979 - PASS 1B\\UploadPrep docs\\metaboliteData2fix\\Fixed";
		IOFileFilter metNameFilter = 
				FileFilterUtils.makeFileOnly(new RegexFileFilter("^metadata_metabolites_named_.+\\.(txt)|(TXT)$"));
		Collection<File> metNamefiles = FileUtils.listFiles(
				sourceDirectory,
				metNameFilter,
				null);
		
		Path bucketFileListing = Paths.get(
				"Y:\\DataAnalysis\\_Reports\\EX00979 - PASS 1B\\UploadPrep docs\\metadata_metabolites_named_listing.txt");
		List<String>bucketAddresses = Files.readAllLines(bucketFileListing);
		List<String>copyCommands = new ArrayList<String>();		
		String[]lipidsWithNoIsomers = new String[] {"LPC(15:0)", "MG(14:0)", "LPC(18:1)", "LPC(17:0)",};
		if (!metNamefiles.isEmpty()) {

			for(File mnf : metNamefiles) {
				
				boolean hasChanged = false;
				//	Read file and replace names if necessary
				Collection<Map<MoTrPACmetaboliteMetaDataFields,String>>metaboliteMetadata = 
						parseMetaboliteMetadataFile(mnf);
				
				for(Map<MoTrPACmetaboliteMetaDataFields, String> mData : metaboliteMetadata) {
					
					String mName = mData.get(MoTrPACmetaboliteMetaDataFields.METABOLITE_NAME);
					for(String lipid : lipidsWithNoIsomers) {
						
						if(mName.contains(lipid) && !mName.equals(lipid)) {
							mData.put(MoTrPACmetaboliteMetaDataFields.REFMET_NAME, lipid);
							hasChanged = true;
						}
					}
					if(mName.endsWith("_a")) {
						
						if(mName.endsWith("_rp_a"))
							mData.put(MoTrPACmetaboliteMetaDataFields.REFMET_NAME, mName);
						else
							mData.put(MoTrPACmetaboliteMetaDataFields.REFMET_NAME, mName.replace("_a", "_rp_a"));
						
						hasChanged = true;
					}
					if(mName.endsWith("_b")) {
						
						if(mName.endsWith("_rp_b"))
							mData.put(MoTrPACmetaboliteMetaDataFields.REFMET_NAME, mName);
						else
							mData.put(MoTrPACmetaboliteMetaDataFields.REFMET_NAME, mName.replace("_b", "_rp_b"));
						
						hasChanged = true;
					}
					if(mName.endsWith("_a_b")) {
						if(mName.endsWith("_rp_a_b"))
							mData.put(MoTrPACmetaboliteMetaDataFields.REFMET_NAME, mName.replace("_rp_a_b", ""));
						else
							mData.put(MoTrPACmetaboliteMetaDataFields.REFMET_NAME, mName.replace("_a_b", ""));
						hasChanged = true;
					}
					if(mName.equals("Car(5:0) isomers")) {
						mData.put(MoTrPACmetaboliteMetaDataFields.REFMET_NAME, "CAR(5:0)_rp_a_b");
						hasChanged = true;
					}
				}			
				if(hasChanged) {
					Path newFilePath = Paths.get(destinationDirName, mnf.getName());
					try {
						writeMetaboliteMetadataFile(metaboliteMetadata, newFilePath);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					String fName = mnf.getName();
					String bucketAddres = bucketAddresses.stream().
							filter(l -> l.contains(fName)).
							findFirst().orElse(null);
					
					if(bucketAddres != null) {
						copyCommands.add("gsutil cp \"" + newFilePath.toString() + "\" " + bucketAddres.replace("PROCESSED_20191008", "PROCESSED_20210629"));
					}
				}
			}
		}
		Path copyCommandsFilePath = Paths.get("Y:\\DataAnalysis\\_Reports\\EX00979 - PASS 1B\\UploadPrep docs\\metaboliteData2fix\\Fixed\\copyCommands.txt");
		try {
			Files.write(copyCommandsFilePath, 
					copyCommands, 					
					StandardCharsets.UTF_8,
					StandardOpenOption.CREATE, 
					StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void fixMetaboliteNamesStepTwo() throws Exception {
		
		File sourceDirectory = new File("Y:\\DataAnalysis\\_Reports\\EX00979 - PASS 1B\\UploadPrep docs\\metaboliteData2fix");
		String destinationDirName = "Y:\\DataAnalysis\\_Reports\\EX00979 - PASS 1B\\UploadPrep docs\\metaboliteData2fix\\Fixed";
		IOFileFilter metNameFilter = 
				FileFilterUtils.makeFileOnly(new RegexFileFilter("^metadata_metabolites_named_.+\\.(txt)|(TXT)$"));
		Collection<File> metNamefiles = FileUtils.listFiles(
				sourceDirectory,
				metNameFilter,
				null);
		
		Path bucketFileListing = Paths.get("Y:\\DataAnalysis\\_Reports\\EX00979 - PASS 1B\\UploadPrep docs\\metadata_metabolites_named_listing.txt");
		List<String>bucketAddresses = Files.readAllLines(bucketFileListing);
		List<String>copyCommands = new ArrayList<String>();		
		if (!metNamefiles.isEmpty()) {

			for(File mnf : metNamefiles) {
				
				boolean hasChanged = false;
				//	Read file and replace names if necessary
				Collection<Map<MoTrPACmetaboliteMetaDataFields,String>>metaboliteMetadata = 
						parseMetaboliteMetadataFile(mnf);
				
				for(Map<MoTrPACmetaboliteMetaDataFields, String> mData : metaboliteMetadata) {
					
					String refmetOld = mData.get(MoTrPACmetaboliteMetaDataFields.REFMET_NAME);
					if(refmetOld.equalsIgnoreCase("CAR(3:0(2Me))")) {
						mData.put(MoTrPACmetaboliteMetaDataFields.REFMET_NAME, "CAR(4:0)_rp_a");
						hasChanged = true;
					}				
					if(refmetOld.equalsIgnoreCase("Car(5:0)")) {
						mData.put(MoTrPACmetaboliteMetaDataFields.REFMET_NAME, "CAR(5:0)_rp_b");
						hasChanged = true;
					}
					if(refmetOld.equalsIgnoreCase("Car(5:0) isomers") || refmetOld.equals("CAR(5:0)_rp_a_b")) {
						mData.put(MoTrPACmetaboliteMetaDataFields.REFMET_NAME, "CAR(5:0)");
						hasChanged = true;
					}
					
					if(refmetOld.equalsIgnoreCase("Car(4:0)")) {
						mData.put(MoTrPACmetaboliteMetaDataFields.REFMET_NAME, "CAR(4:0)_rp_b");
						hasChanged = true;
					}
					if(refmetOld.equalsIgnoreCase("Car(4:0) isomers") || refmetOld.equals("CAR(4:0)_rp_a_b")) {
						mData.put(MoTrPACmetaboliteMetaDataFields.REFMET_NAME, "CAR(4:0)");
						hasChanged = true;
					}
					if(refmetOld.equalsIgnoreCase("CAR(4:0(3Me))")) {
						mData.put(MoTrPACmetaboliteMetaDataFields.REFMET_NAME, "CAR(5:0)_rp_a");
						hasChanged = true;
					}
				}			
				if(hasChanged) {
					Path newFilePath = Paths.get(destinationDirName, mnf.getName());
					try {
						writeMetaboliteMetadataFile(metaboliteMetadata, newFilePath);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					String fName = mnf.getName();
					String bucketAddres = bucketAddresses.stream().
							filter(l -> l.contains(fName)).
							findFirst().orElse(null);
					
					if(bucketAddres != null) {
						copyCommands.add("gsutil cp \"" + newFilePath.toString() + "\" " + bucketAddres.replace("PROCESSED_20191008", "PROCESSED_20210629"));
					}
				}
			}
		}
		Path copyCommandsFilePath = Paths.get("Y:\\DataAnalysis\\_Reports\\EX00979 - PASS 1B\\UploadPrep docs\\metaboliteData2fix\\Fixed\\copyCommands.txt");
		try {
			Files.write(copyCommandsFilePath, 
					copyCommands, 					
					StandardCharsets.UTF_8,
					StandardOpenOption.CREATE, 
					StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void updateMotrPacRefSampleAssignment() throws Exception {
		
		//	Read list of files
		Path listPath = Paths.get("Y:\\DataAnalysis\\_Reports\\EX00979 - PASS 1B\\RefSampleCorrection\\PASS1A-06_sample_metadata_file_list.txt");
		String tmpDir = "Y:\\DataAnalysis\\_Reports\\EX00979 - PASS 1B\\RefSampleCorrection\\TMP";
		String gsUtilBinary = "C:\\Users\\Sasha\\AppData\\Local\\Google\\Cloud SDK\\google-cloud-sdk\\bin\\gsutil.cmd";
		List<String>sampleMetaDataList = new ArrayList<String>();
		try {
			sampleMetaDataList = Files.readAllLines(listPath);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		for (String bucketPath : sampleMetaDataList) {

			Path tmpFilePath = Paths.get(tmpDir, "sample_metadata.txt").toAbsolutePath();
			Path correctedTmpFilePath = Paths.get(tmpDir, "sample_metadata_corrected.txt");
			ProcessBuilder pb = new ProcessBuilder(
					gsUtilBinary, "cp", bucketPath, tmpFilePath.toString());
			try {
				Process p = pb.start();
				int exitCode = p.waitFor();
				if (exitCode == 0) {
					p.destroy();
					List<String> sampleDataLines = new ArrayList<String>();
					if(tmpFilePath.toFile() != null && tmpFilePath.toFile().exists())
						sampleDataLines  = Files.readAllLines(tmpFilePath, Charset.forName("ISO-8859-1"));
					
					boolean corrected = false;
					List<String> correctedSampleDataLines = new ArrayList<String>();
					for(String sdl : sampleDataLines) {
						
						if(sdl.startsWith("CS0UM")) {
							correctedSampleDataLines.add(sdl.replace("QC-Reference", "QC-ReCAS"));
							corrected = true;
						}
						else {
							correctedSampleDataLines.add(sdl);
						}
					}
					if(corrected) {						
					    try {
							Files.write(correctedTmpFilePath, 
									correctedSampleDataLines, 
									StandardCharsets.UTF_8,
									StandardOpenOption.CREATE, 
									StandardOpenOption.TRUNCATE_EXISTING);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}					    
					    //	Upload corrected file 
					    if(correctedTmpFilePath.toFile() != null && correctedTmpFilePath.toFile().exists()) {
							pb = new ProcessBuilder(
									gsUtilBinary, "cp", correctedTmpFilePath.toString(), bucketPath);
							p = pb.start();
							exitCode = p.waitFor();
							if (exitCode == 0) {
								System.out.println("Updated " + bucketPath);
							}
					    }
					    //	Delete corrected file					    
					    Files.delete(correctedTmpFilePath);
					}
					Files.delete(tmpFilePath);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		}
	}

	public static void extractRefmetDisrepancies(File logLocationList, File output) {
		
		List<String> logLocations = new ArrayList<String>();
		List<File>qcLogFiles = new ArrayList<File>();
		Set<String>refmetDiscrepancies = new TreeSet<String>();
		try {
			logLocations = Files.readAllLines(logLocationList.toPath());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for(String ll : logLocations) {
			
			File logDir = new File(ll);
			if(logDir.exists()) 				
				qcLogFiles.addAll(Arrays.asList(logDir.listFiles()));			
		}
		List<String> loglines = new ArrayList<String>(); 
		List<String> refmetErrors = new ArrayList<String>(); 
		for(File qcLog : qcLogFiles) {
			
			loglines.clear();
			refmetErrors.clear();
			try {
				loglines = Files.readAllLines(qcLog.toPath());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			refmetErrors = loglines.stream().
					filter(l -> l.contains("(-) `refmet_name`")).
					collect(Collectors.toList());
			if(!refmetErrors.isEmpty()) {
				
				for(String refmetError : refmetErrors) {
					
					String line = refmetError.replace("(-) `refmet_name`", "").
						replaceAll("\\[`", "").replaceAll("`\\]\\s+", "\t").replaceAll("\\(Error.+\\)", "").trim();
					refmetDiscrepancies.add(line);
				}				
			}
		}
		try {
		    Files.write(output.toPath(), 
		    		refmetDiscrepancies,
		            StandardCharsets.UTF_8,
		            StandardOpenOption.CREATE, 
		            StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
		    e.printStackTrace();
		}
	}
	
	public static void createUniqueSampleIdsForControlsAndOrderSamples(File runManifestFile) {
		
		String[][] manifestData = null;
		try {
			manifestData = DelimitedTextParser.parseTextFileWithEncoding(runManifestFile, '\t');
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(manifestData == null || manifestData.length == 0)
			return;
		
		//	Find sample ID and order column indexes
		int sampleIdCol = -1;
		int sampleTypeCol = -1;
		int sampleOrderCol = -1;
		for(int i=0; i<manifestData[0].length; i++) {
			
			if(manifestData[0][i].equals(MoTrPACRawDataManifestFields.MOTRPAC_SAMPLE_ID.getName()))				
				sampleIdCol = i;

			if(manifestData[0][i].equals(MoTrPACRawDataManifestFields.MOTRPAC_SAMPLE_ORDER.getName()))				
				sampleOrderCol = i;
			
			if(manifestData[0][i].equals(MoTrPACRawDataManifestFields.MOTRPAC_SAMPLE_TYPE.getName()))				
				sampleTypeCol = i;
		}
		if(sampleIdCol == -1 || sampleTypeCol == -1 || sampleOrderCol == -1) {
			System.err.println("Obligatory column(s) not found");
			return;
		}
		ReferenceSamplesManager.refreshReferenceSampleList();
		Map<String,Integer>controlCounts = new TreeMap<>();
		for(ExperimentalSample sample : ReferenceSamplesManager.getReferenceSamples())
			controlCounts.put(sample.getId(), 1);
		
		//	Correct names for male/female controls
		Map<String,String>sampleNameMap = getMailFemailControlsMap();
		Map<Integer,String>sampleTypeMap = new TreeMap<>();
		
		String[]controlIdMap = new String[manifestData.length];
		for(int i=1; i<manifestData.length; i++) {
			
			String sampleId = manifestData[i][sampleIdCol];
			String controlId = controlCounts.keySet().stream().
					filter(sampleId::startsWith).findFirst().orElse(null);
			if(controlId != null) {
				int currentCount = controlCounts.get(controlId);
				String newControlId = controlId + "-" 
						+ StringUtils.leftPad(Integer.toString(currentCount), 3, '0');
				controlIdMap[i] = newControlId;
				controlCounts.put(controlId, currentCount + 1);
				
				if(sampleNameMap.containsKey(controlId))
					sampleTypeMap.put(i, sampleNameMap.get(controlId));			
			}
			else {
				controlIdMap[i] = sampleId;
			}
		}
		List<String>lines = new ArrayList<>();
		lines.add(StringUtils.join(manifestData[0], "\t"));
		List<String>nextLine = new ArrayList<>();
		int columnCount = manifestData[0].length;
		for(int i=1; i<manifestData.length; i++) {
			
			nextLine.clear();
			for(int j=0; j<columnCount; j++) {
				
				if(j == sampleIdCol) {
					nextLine.add(controlIdMap[i]);
				}
				else if(j == sampleTypeCol && sampleTypeMap.containsKey(i)) {
					nextLine.add(sampleTypeMap.get(i));
				}
				else if(j == sampleOrderCol) {
					nextLine.add(Integer.toString(i));
				}
				else
					nextLine.add(manifestData[i][j]);
			}
			lines.add(StringUtils.join(nextLine, "\t"));
		}
		Path outputPath = Paths.get(runManifestFile.getParent(), 
				PathUtils.getBaseName(runManifestFile.toPath()) + "_uniqueControls.txt");
		try {
		    Files.write(outputPath, 
		    		lines,
		            StandardCharsets.UTF_8,
		            StandardOpenOption.CREATE, 
		            StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
		    e.printStackTrace();
		}
	}
	
	public static Map<String,String> getMailFemailControlsMap() {
		
		Map<String,String>sampleNameMap = new TreeMap<>();
		sampleNameMap.put("CSMR81030","QC-Reference-Female");
		sampleNameMap.put("CSMR81040","QC-Reference-Male");
		sampleNameMap.put("CSMR81020","QC-Reference-Female");
		sampleNameMap.put("CSMR81010","QC-Reference-Male");
		return sampleNameMap;
	}
}


















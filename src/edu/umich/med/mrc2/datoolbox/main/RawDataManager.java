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

package edu.umich.med.mrc2.datoolbox.main;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.commons.compress.utils.FileNameUtils;
import org.apache.commons.io.FilenameUtils;

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.enums.Polarity;
import edu.umich.med.mrc2.datoolbox.data.enums.SupportedRawDataTypes;
import edu.umich.med.mrc2.datoolbox.data.lims.Injection;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTRawDataUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.ColorUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.IndeterminateProgressDialog;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import umich.ms.datatypes.LCMSData;
import umich.ms.datatypes.LCMSDataSubset;
import umich.ms.datatypes.scan.StorageStrategy;
import umich.ms.datatypes.scancollection.ScanIndex;
import umich.ms.fileio.exceptions.FileParsingException;
import umich.ms.fileio.filetypes.mzml.MZMLFile;
import umich.ms.fileio.filetypes.mzxml.MZXMLFile;
import umich.ms.fileio.filetypes.xmlbased.AbstractXMLBasedDataSource;

public class RawDataManager {

	private static Map<DataFile, LCMSData> rawDataMap 
		= new HashMap<DataFile, LCMSData>();
	private static Map<String,Collection<Path>>fileLocationMap 
		= new TreeMap<String,Collection<Path>>();

	public static Map<String, Collection<Path>> getFileLocationMap() {
		return fileLocationMap;
	}

	public static Map<DataFile, LCMSData> getRawDataMap() {
		return rawDataMap;
	}
	
	public static LCMSData getRawData(DataFile file) {
		
		LCMSData rds = rawDataMap.get(file);
		if(rds == null) {
			
			File rdf = new File(file.getFullPath());
			if(!rdf.exists()) {
				
				if(MRC2ToolBoxCore.getActiveRawDataAnalysisExperiment() != null)
				rdf = Paths.get(
						MRC2ToolBoxCore.getActiveRawDataAnalysisExperiment().getRawDataDirectory().getAbsolutePath(), 
						file.getName()).toFile();
			}
			if(rdf == null || !rdf.exists())
				return null;
			
			file.setFullPath(rdf.getAbsolutePath());
			try {
				rds = createDataSource(new File(file.getFullPath()));
			} catch (FileParsingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(rds != null) {
				
				Color nextColor = ColorUtils.getColor(rawDataMap.size());
				file.setColor(nextColor);
				rawDataMap.put(file, rds);	
			}
		}
		else {
			if(rds.getScans() != null && rds.getScans().isEmpty()) {
				
				try {
					rds.load(LCMSDataSubset.STRUCTURE_ONLY, MRC2ToolBoxCore.getMainWindow());
				} catch (FileParsingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}		
				rds.getScans().isAutoloadSpectra(true);
				rds.getScans().setDefaultStorageStrategy(StorageStrategy.SOFT);
			}
		}
		return rds;
	}
	
	public static Polarity getPolarityForLCMSData(LCMSData data) {
		
		if(data == null)
			return null;
		
		ScanIndex idx = data.getScans().getMapMsLevel2index().get(1);
		if( idx == null)
			idx = data.getScans().getMapMsLevel2index().get(2);
		
		if( idx == null)
			return null;

		Set<Integer> polSet = idx.getNum2scan().values().stream().
				map(s -> s.getPolarity().getSign()).collect(Collectors.toSet());
		
		Polarity pol = null;
		if(polSet.size() > 1) {
			pol = Polarity.Neutral;
		}
		else {	
			int sign = polSet.iterator().next();
			if(sign == 1)
				pol = Polarity.Positive;
			if(sign == -1)
				pol = Polarity.Negative;
		}
		return pol;
	}
	
	public static boolean isDataSourceForInjectionLoaded(String injectionId) {
		
		LCMSData data = rawDataMap.entrySet().stream().
			filter(e -> e.getKey().getInjectionId().equals(injectionId)).
			map(e -> e.getValue()).findFirst().orElse(null);
		
		return data != null;
	}
	
	public static LCMSData getRawData(File rawFile) {
		
		String path = rawFile.getAbsolutePath();
		return rawDataMap.entrySet().stream().
				filter(e -> e.getKey().getFullPath().equals(path)).
				map(e -> e.getValue()).findFirst().orElse(null);
	}
	
	public static void removeDataSource(DataFile file) {
		
		LCMSData data = rawDataMap.get(file);
		if(data != null)
			data.releaseMemory();

		rawDataMap.remove(file);
	}
	
	public static void addDataSource(DataFile file, LCMSData data) {
		rawDataMap.put(file, data);		
	}
	
	public static LCMSData createDataSource(File sourceRawFile) throws FileParsingException {

		Path path = Paths.get(sourceRawFile.getAbsolutePath());
		AbstractXMLBasedDataSource source = null;
		if (FilenameUtils.getExtension(path.toString()).
				equalsIgnoreCase(SupportedRawDataTypes.MZML.name()))
			source = new MZMLFile(path.toString());

		if (FilenameUtils.getExtension(path.toString()).
				equalsIgnoreCase(SupportedRawDataTypes.MZXML.name()))
			source = new MZXMLFile(path.toString());

		LCMSData data = new LCMSData(source);
		data.load(LCMSDataSubset.STRUCTURE_ONLY, MRC2ToolBoxCore.getMainWindow());		
		data.getScans().isAutoloadSpectra(true);
		data.getScans().setDefaultStorageStrategy(StorageStrategy.SOFT);
		return data;
	}
	
	public static void releaseAllDataSources() {	
		
		ReleaseAllDataSourcesTask task = new ReleaseAllDataSourcesTask();
		IndeterminateProgressDialog idp = 
				new IndeterminateProgressDialog("Releasing memory for raw data files ...", 
						MRC2ToolBoxCore.getMainWindow(), task);
		idp.setLocationRelativeTo(MRC2ToolBoxCore.getMainWindow().getContentPane());
		idp.setVisible(true);
		
		//	rawDataMap.values().stream().forEach(d -> d.releaseMemory());
	}
	
	public static void clearDataSourcesMap() {
		rawDataMap.clear();
	}
	
	public static DataFile getDataFileForInjectionId(String injectionId) {
		
		DataFile df = rawDataMap.keySet().stream().
				filter(f -> Objects.nonNull(f.getInjectionId())).
				filter(f -> f.getInjectionId().equals(injectionId)).
				findFirst().orElse(null);
		if(df != null) {
			return df;
		}
		else {
			Injection inj = null;
			try {
				inj = IDTRawDataUtils.getInjectionForId(injectionId);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(inj != null) {
				df = new DataFile(inj);
				Collection<Path> paths = 
						fileLocationMap.get(FileNameUtils.getBaseName(inj.getDataFileName()));
				if(paths != null && !paths.isEmpty())
					df.setFullPath(paths.iterator().next().toString());
									
				return df;
			}
		}
		return null;
	}
	
	public static DataFile getDataFileForSource(LCMSData source) {
		
		return rawDataMap.entrySet().stream().
				filter(e -> e.getValue().equals(source)).
				map(e -> e.getKey()).findFirst().orElse(null);
	}
	
	public static LCMSData getRawDataForInjectionId(String injectionId) {
		
		DataFile df = rawDataMap.keySet().stream().
				filter(f -> Objects.nonNull(f.getInjectionId())).
				filter(f -> f.getInjectionId().equals(injectionId)).
				findFirst().orElse(null);
		if(df != null)
			return rawDataMap.get(df);
		
		df =  getDataFileForInjectionId(injectionId);
		if(df == null)
			return null;
		
		//	Try to find path to file
//		if(df.getFullPath() == null) {
			String fName = FilenameUtils.getBaseName(df.getName());
			if(!fileLocationMap.containsKey(fName))
				return null;
			
			Collection<Path> filePaths = fileLocationMap.get(fName);
			if(filePaths.size() == 1) {
				df.setFullPath(filePaths.iterator().next().toFile().getAbsolutePath());
				return getRawData(df);
			}
			else {
				Injection inj = null;
				try {
					inj = IDTRawDataUtils.getInjectionForId(injectionId);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if(inj == null)
					return null;
				
				for(Path path : filePaths) {
					LCMSData data = null;
					try {
						data = createDataSource(path.toFile());
					} catch (FileParsingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if(data != null) {
						
						if(data.getSource().getRunInfo().getRunStartTime().equals(inj.getTimeStamp())) {
							df.setFullPath(path.toFile().getAbsolutePath());
							rawDataMap.put(df, data);
							return data;
						}
						else
							data.releaseMemory();
					}
				}
			}
//		}
		if(df.getFullPath() == null)
			return null;
		
		return null;
	}
	
	public static void addFilePath(Path filePath) {
		
		String fName = FilenameUtils.getBaseName(filePath.toString());
		if(!fileLocationMap.containsKey(fName))
			fileLocationMap.put(fName, new TreeSet<Path>());
		
		fileLocationMap.get(fName).add(filePath);
	}
	
	public static boolean rawDataIndexed() {
		return !fileLocationMap.isEmpty();
	}
	
	public static void indexRepository() {
		
		Collection<Path>rawPaths = new ArrayList<Path>();
		try {
			String rawDataDirectory = MRC2ToolBoxConfiguration.getRawDataRepository();
			listFiles(Paths.get(rawDataDirectory), rawPaths);			
		} catch (Exception e) {
			e.printStackTrace();
		}
		rawPaths.stream().
			filter(p -> (p.toString().toLowerCase().endsWith(".mzml") || 
					p.toString().toLowerCase().endsWith(".mzxml"))).
			forEach(p -> addFilePath(p));			
	}
	
	private static void listFiles(Path path, Collection<Path>rawPaths) throws IOException {
		
	    try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
	        for (Path entry : stream) {
	            if (Files.isDirectory(entry)) {
	                listFiles(entry, rawPaths);
	            }
	            rawPaths.add(entry);
	        }
	    }
	}
}



















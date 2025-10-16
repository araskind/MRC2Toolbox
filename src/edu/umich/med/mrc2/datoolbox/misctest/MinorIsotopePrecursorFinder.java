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

package edu.umich.med.mrc2.datoolbox.misctest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import edu.umich.med.mrc2.datoolbox.data.MsPoint;
import edu.umich.med.mrc2.datoolbox.data.PrecursorLookupSpectrum;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSExperiment;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCache;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.FilePreferencesFactory;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.utils.MsUtils;
import edu.umich.med.mrc2.datoolbox.utils.Range;

public class MinorIsotopePrecursorFinder {

	private static final String logFolderPath = 
			"E:\\DataAnalysis\\MSMS\\MinorIsotopePrecursors"; 
	private static final String processedExperimentsFilePath = 
			"E:\\DataAnalysis\\MSMS\\MinorIsotopePrecursors\\ProcessedExperiments.txt";
	
	private static final String logFileSuffix = "_MINOR_ISOTOPE_PROCESSED_FEATURES.TXT";
	private static final String updateFileSuffix = "_MINOR_ISOTOPE_FEATURES_4UPDATE.TXT";
	
	public static void main(String[] args) {
		
		System.setProperty("java.util.prefs.PreferencesFactory", 
				FilePreferencesFactory.class.getName());
		System.setProperty(FilePreferencesFactory.SYSTEM_PROPERTY_FILE, 
				MRC2ToolBoxCore.configDir + "MRC2ToolBoxPrefs.txt");
		MRC2ToolBoxConfiguration.initConfiguration();
		try {
		//	findMinorIsotopePrecursors();
			markMinorIsotopePrecursors();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void markMinorIsotopePrecursors() throws Exception{
		
		Set<Path>resultPaths = 
				listPathsInDirectory("E:\\DataAnalysis\\MSMS\\MinorIsotopePrecursors\\4update");
		
		String idLevelId = "IDS007";
		String annotationId = "STAN0021";
			
		Connection conn = ConnectionManager.getConnection();	
		
		String idLevelQueryOne = 
				"UPDATE MSMS_FEATURE "
				+ "SET IDENTIFICATION_LEVEL_ID = ? "
				+ "WHERE MSMS_FEATURE_ID = ?";	
		PreparedStatement idLevelPSOne = conn.prepareStatement(idLevelQueryOne);
		idLevelPSOne.setString(1, idLevelId);
		
		String matchSelectQuery = 
				"SELECT MATCH_ID FROM MSMS_FEATURE_LIBRARY_MATCH WHERE MSMS_FEATURE_ID = ?";
		PreparedStatement matchSelectPs = conn.prepareStatement(matchSelectQuery);
		
		String idLevelQueryTwo = 
				"UPDATE MSMS_FEATURE_LIBRARY_MATCH "
				+ "SET IDENTIFICATION_LEVEL_ID = ? WHERE MATCH_ID = ?";
		PreparedStatement idLevelPSTwo = conn.prepareStatement(idLevelQueryTwo);
		idLevelPSTwo.setString(1, idLevelId);
		
		String parentQuery =
				"SELECT PARENT_FEATURE_ID FROM MSMS_FEATURE WHERE MSMS_FEATURE_ID = ?";
		PreparedStatement parentPs = conn.prepareStatement(parentQuery);
		
		String saQuery =
				"INSERT INTO MSMS_FEATURE_STANDARD_ANNOTATIONS ("
				+ "MSMS_PARENT_FEATURE_ID, STANDARD_ANNOTATION_ID) VALUES(?, ?)";
		PreparedStatement saPs = conn.prepareStatement(saQuery);
		saPs.setString(2, annotationId);
		
		ResultSet rs = null;
		for(Path rp : resultPaths) {
			
			Set<String>msmsIdList = readFileAndSortLines(rp);
			for(String msmsId : msmsIdList) {
				
				//	ID level for unknowns
				idLevelPSOne.setString(2, msmsId);
				idLevelPSOne.executeQuery();
				
				//	ID level for match
				matchSelectPs.setString(1, msmsId);
				rs = matchSelectPs.executeQuery();
				while(rs.next()) {

					idLevelPSTwo.setString(2, rs.getString("MATCH_ID"));
					idLevelPSTwo.executeQuery();
				}
				rs.close();
				
				//	Standard annotation
				parentPs.setString(1, msmsId);
				rs = parentPs.executeQuery();
				while(rs.next()) {

					saPs.setString(1, rs.getString("PARENT_FEATURE_ID"));
					saPs.executeQuery();
				}
				rs.close();				
			}
			System.out.println(rp.getFileName() + " processed");
		}
		idLevelPSOne.close();
		idLevelPSTwo.close();
		matchSelectPs.close();
		saPs.close();
		parentPs.close();
		ConnectionManager.releaseConnection(conn);
	}
	
	public static Set<Path> listPathsInDirectory(String dir) throws IOException {
		
	    try (Stream<Path> stream = Files.list(Paths.get(dir))) {
	        return stream
	          .filter(file -> !Files.isDirectory(file))
	          .collect(Collectors.toSet());
	    }
	}
	
	private static void findMinorIsotopePrecursors() throws Exception{
		
		Collection<LIMSExperiment> experimentList = IDTDataCache.getExperiments();	
		Path processedExperimentsPath = 
				Paths.get(processedExperimentsFilePath);	
		Set<String>processedExperimentIds = 
				readFileAndSortLines(processedExperimentsPath);
		List<LIMSExperiment>unprocessed = experimentList.stream().
				filter(e -> !processedExperimentIds.contains(e.getId())).
				collect(Collectors.toList());
		
		for(LIMSExperiment experiment : unprocessed) {
			
			Path logPath = Paths.get(logFolderPath, 
					experiment.getId() + logFileSuffix);
			Set<String>processedFeatureIds = 
					readFileAndSortLines(logPath);
			
			Collection<PrecursorLookupSpectrum>plSpectra = 
					getPrecursorLookupSpectraForExperiment(
							experiment.getId(), processedFeatureIds);

			List<String> toUpdate = plSpectra.stream().filter(s -> s.isMinorIsotope(15)).
					map(s -> s.getMsmsFeatureId()).distinct().sorted().
					collect(Collectors.toList());
			Path outPath = Paths.get(logFolderPath, "4update",
					experiment.getId() + updateFileSuffix);
			try {
				Files.write(outPath, 
						toUpdate, 
						StandardCharsets.UTF_8, 
						StandardOpenOption.CREATE,
						StandardOpenOption.TRUNCATE_EXISTING);
			} catch (IOException e) {
				e.printStackTrace();
			}			
			appendToFile(processedExperimentsPath, experiment.getId() + "\n");
			System.out.println(experiment.getId() + " processed");
		}
	}
	
	private static Collection<PrecursorLookupSpectrum>getPrecursorLookupSpectraForExperiment(
			String experimentId, Set<String>processedFeatureIds) throws Exception{
		
		Collection<PrecursorLookupSpectrum>spectra = 
				new ArrayList<PrecursorLookupSpectrum>();
		Connection conn = ConnectionManager.getConnection();
		String featureQuery = 
				"SELECT DISTINCT F.FEATURE_ID, F2.MSMS_FEATURE_ID, F.MZ_OF_INTEREST  " +
				"FROM MSMS_PARENT_FEATURE F,  " +
				"DATA_ANALYSIS_MAP M,  " +
				"INJECTION I,  " +
				"PREPARED_SAMPLE P,  " +
				"SAMPLE S,  " +
				"MSMS_FEATURE F2  " +
				"WHERE F.FEATURE_ID  = F2.PARENT_FEATURE_ID " +
				"AND F.HAS_CHROMATOGRAM IS NOT NULL " +
				"AND F.BASE_PEAK IS NOT NULL " +
				"AND F.DATA_ANALYSIS_ID = M.DATA_ANALYSIS_ID  " +
				"AND M.INJECTION_ID = I.INJECTION_ID  " +
				"AND I.PREP_ITEM_ID = P.PREP_ITEM_ID  " +
				"AND P.SAMPLE_ID = S.SAMPLE_ID  " +
				"AND S.EXPERIMENT_ID = ? ";		
		PreparedStatement ps = conn.prepareStatement(featureQuery);
		
		String msQuery = "SELECT MZ, HEIGHT FROM MSMS_PARENT_FEATURE_PEAK WHERE FEATURE_ID = ?";
		PreparedStatement msps = conn.prepareStatement(msQuery);
		
		ps.setString(1, experimentId);
		ResultSet rs = ps.executeQuery();
		ResultSet msrs;
		Range mzRange;
		while(rs.next()) {
			
			String msmsFid = rs.getString("MSMS_FEATURE_ID");
			if(!processedFeatureIds.contains(msmsFid)) {
				
				PrecursorLookupSpectrum pls = 
						new PrecursorLookupSpectrum(msmsFid);
				
				mzRange = MsUtils.createPpmMassRange(rs.getDouble("MZ_OF_INTEREST"), 15);
				msps.setString(1, rs.getString("FEATURE_ID"));
				msrs = msps.executeQuery();
				while(msrs.next()) {
					
					MsPoint p = new MsPoint(
							msrs.getDouble("MZ"), msrs.getDouble("HEIGHT"));
					pls.addMsPoint(p);
					if(mzRange.contains(p.getMz()))
						pls.setPrecursor(p);
				}
				msrs.close();				
				spectra.add(pls);
			}
		}
		rs.close();
		ps.close();
		msps.close();
		ConnectionManager.releaseConnection(conn);
		return spectra;
	}
	
	private static Set<String> readFileAndSortLines(Path filePath) {
		
		Set<String>sortedData = new TreeSet<String>();	
		if(!filePath.toFile().exists())
			return sortedData;
			
		List<String> result = new ArrayList<String>();
		try (Stream<String> lines = Files.lines(filePath)) {
				result = lines.collect(Collectors.toList());
		} catch (Exception e) {
			e.printStackTrace();
		}
		result.stream().filter(l -> Objects.nonNull(l)).
			filter(l -> !l.trim().isEmpty()).
			forEach(l -> sortedData.add(l));
		
		return sortedData;
	}
	
	private static void appendToFile(Path path, String content) throws IOException {

		Files.write(path, 
				content.getBytes(StandardCharsets.UTF_8),
				StandardOpenOption.CREATE,
				StandardOpenOption.APPEND);
	}
}
















/*******************************************************************************
 *
 * (C) Copyright 2018-2026 MRC2 (http://mrc2.umich.edu).
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

package edu.umich.med.mrc2.datoolbox.database.idt;

import java.awt.Component;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.openscience.cdk.exception.CDKException;

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesign;
import edu.umich.med.mrc2.datoolbox.data.ExperimentalSample;
import edu.umich.med.mrc2.datoolbox.data.IDTExperimentalSample;
import edu.umich.med.mrc2.datoolbox.data.StockSample;
import edu.umich.med.mrc2.datoolbox.data.Worklist;
import edu.umich.med.mrc2.datoolbox.data.enums.AnnotatedObjectType;
import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.data.enums.Polarity;
import edu.umich.med.mrc2.datoolbox.data.enums.SoftwareType;
import edu.umich.med.mrc2.datoolbox.data.lims.DataAcquisitionMethod;
import edu.umich.med.mrc2.datoolbox.data.lims.DataExtractionMethod;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.data.lims.DataProcessingSoftware;
import edu.umich.med.mrc2.datoolbox.data.lims.IDTMsSummary;
import edu.umich.med.mrc2.datoolbox.data.lims.Injection;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSBioSpecies;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSClient;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSExperiment;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSInstrument;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSProject;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSProtocol;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSSamplePreparation;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSSampleType;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSUser;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSWorklistItem;
import edu.umich.med.mrc2.datoolbox.data.lims.Manufacturer;
import edu.umich.med.mrc2.datoolbox.data.lims.ObjectAnnotation;
import edu.umich.med.mrc2.datoolbox.data.lims.SopCategory;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.database.DatabaseUtils;
import edu.umich.med.mrc2.datoolbox.database.lims.LIMSDataCache;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.ReferenceSamplesManager;
import edu.umich.med.mrc2.datoolbox.rawdata.MSMSExtractionParameterSet;
import edu.umich.med.mrc2.datoolbox.utils.CompressionUtils;
import edu.umich.med.mrc2.datoolbox.utils.FIOUtils;
import edu.umich.med.mrc2.datoolbox.utils.SQLUtils;
import edu.umich.med.mrc2.datoolbox.utils.XmlUtils;

public class IDTUtils {
	
	private static final Logger logger = LogManager.getLogger(IDTUtils.class);
	
	private IDTUtils() {
		
	}

	public static boolean isSuperUser(Component parent) {
		
		if(MRC2ToolBoxCore.getIdTrackerUser() == null) {
			MessageDialog.showErrorMsg("You are not logged in!", parent);
			return false;
		}
		if(!MRC2ToolBoxCore.getIdTrackerUser().isSuperUser()) {
			MessageDialog.showErrorMsg(
					"You do not have sufficient privileges to delete data from the system!", parent);
			return false;
		}
		else
			return true;
	}

	public static Collection<LIMSExperiment> getExperimentList() throws SQLException{

		Collection<LIMSExperiment> experiments = new TreeSet<>();
		Connection conn = ConnectionManager.getConnection();
		String query  =
			"SELECT E.EXPERIMENT_ID, E.EXPERIMENT_NAME, " +
			"E.PROJECT_ID, E.EXPERIMENT_DESCRIPTION, E.DATE_INITIATED, E.NOTES " +
			"FROM EXPERIMENT E ORDER BY E.EXPERIMENT_ID DESC";
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
	
				LIMSExperiment experiment = new LIMSExperiment(
						rs.getString("EXPERIMENT_ID"),
						rs.getString("EXPERIMENT_NAME"),
						rs.getString("EXPERIMENT_DESCRIPTION"),
						rs.getString("NOTES"),
						new String(""),
						rs.getTimestamp("DATE_INITIATED"));
	
				String projectId = rs.getString("PROJECT_ID");
				LIMSProject project =
					IDTDataCache.getProjects().stream().
						filter(u -> u.getId().equals(projectId)).findFirst().orElse(null);
	
				if(project != null) {
					experiment.setProject(project);
					project.getExperiments().add(experiment);
				}
				experiments.add(experiment);
			}
			rs.close();
		}
		ConnectionManager.releaseConnection(conn);
		return experiments;
	}

	public static Collection<? extends LIMSProject> getProjectList() throws SQLException{

		LIMSDataCache.refreshLimsClientList();
		Collection<LIMSProject> projects = new TreeSet<>();
		Connection conn = ConnectionManager.getConnection();
		String query  =
			"SELECT PROJECT_ID, PROJECT_NAME, PROJECT_DESCRIPTION, START_DATE, " +
			"CLIENT_ID, CONTACTPERSON_ID, NOTES FROM PROJECT ORDER BY PROJECT_ID";
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
	
				LIMSClient client = LIMSDataCache.getLIMSClientById(rs.getString("CLIENT_ID"));
				LIMSProject project = new LIMSProject(
						rs.getString("PROJECT_ID"),
						rs.getString("PROJECT_NAME"),
						rs.getString("PROJECT_DESCRIPTION"),
						rs.getString("NOTES"),
						client);
	
				project.setStartDate(rs.getDate("START_DATE"));
				projects.add(project);
			}
			rs.close();
		}
		ConnectionManager.releaseConnection(conn);
		return projects;
	}

	public static String addNewProject(LIMSProject newProject) throws SQLException{

		LIMSUser sysUser = MRC2ToolBoxCore.getIdTrackerUser();
		if(sysUser == null)
			return null;

		LIMSDataCache.refreshLimsClientList();
		LIMSClient client = LIMSDataCache.getLIMSClientForUser(sysUser);
		newProject.setClient(client);
		Connection conn = ConnectionManager.getConnection();
		String id = SQLUtils.getNextIdFromSequence(conn, 
				"ID_PROJECT_SEQ",
				DataPrefix.ID_PROJECT,
				"0",
				3);
		String query  =
			"INSERT INTO PROJECT(PROJECT_ID, PROJECT_NAME, CLIENT_ID, " +
			"PROJECT_DESCRIPTION, CONTACTPERSON_ID, START_DATE, NOTES) " +
			"VALUES(?, ?, ?, ?, ?, ?, ?)";
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1, id);
			ps.setString(2, newProject.getName());
			ps.setString(3, newProject.getClient().getId());
			ps.setString(4, newProject.getDescription());
			ps.setString(5, sysUser.getId());
			ps.setDate(6, new java.sql.Date(new java.util.Date().getTime()));
			ps.setString(7, newProject.getNotes());
			ps.executeUpdate();
		}
		ConnectionManager.releaseConnection(conn);
		return id;
	}

	public static void updateProject(
			String newName, 
			String newDescription, 
			String newNotes, 
			String projectId) throws SQLException {

		Connection conn = ConnectionManager.getConnection();
		String query = "UPDATE PROJECT SET PROJECT_NAME = ?, "
				+ "PROJECT_DESCRIPTION = ?, NOTES =? WHERE PROJECT_ID = ?";
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1, newName);
			ps.setString(2, newDescription);
			ps.setString(3, newNotes);
			ps.setString(4, projectId);
			ps.executeUpdate();
		}
		ConnectionManager.releaseConnection(conn);
	}

	public static String addNewExperiment(LIMSExperiment newExperiment) throws SQLException {

		Connection conn = ConnectionManager.getConnection();
		String nextExperimentId = SQLUtils.getNextIdFromSequence(conn, 
				"ID_EXPERIMENT_SEQ",
				DataPrefix.ID_EXPERIMENT,
				"0",
				4);
		String query  =
			"INSERT INTO EXPERIMENT( EXPERIMENT_ID, EXPERIMENT_NAME, " +
			"PROJECT_ID, EXPERIMENT_DESCRIPTION, DATE_INITIATED, OWNER_ID, NOTES) " +
			"VALUES (?, ?, ?, ?, ?, ?, ?)";
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1, nextExperimentId);
			ps.setString(2, newExperiment.getName());
			ps.setString(3, newExperiment.getProject().getId());
			ps.setString(4, newExperiment.getDescription());
			ps.setDate(5, new java.sql.Date(new java.util.Date().getTime()));
			ps.setString(6, newExperiment.getCreator().getId());
			ps.setString(7, newExperiment.getNotes());
			ps.executeUpdate();
		}
		ConnectionManager.releaseConnection(conn);
		return nextExperimentId;
	}
	
	public static void setInstrumentForExperiment(
			LIMSExperiment experiment, LIMSInstrument instrument) throws SQLException {
		
		Connection conn = ConnectionManager.getConnection();
		String query  =
			"INSERT INTO EXPERIMENT_INSTRUMENT_MAP(EXPERIMENT_ID, INSTRUMENT_ID) " +
			"VALUES (?, ?)";
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1, experiment.getId());
			ps.setString(2, instrument.getInstrumentId());
			ps.executeUpdate();
		}
		ConnectionManager.releaseConnection(conn);
	}
	
	public static void updateInstrumentForExperiment(
			LIMSExperiment experiment, LIMSInstrument instrument) throws SQLException {
		
		Connection conn = ConnectionManager.getConnection();
		String query  =
			"UPDATE EXPERIMENT_INSTRUMENT_MAP "
			+ "SET INSTRUMENT_ID = ? WHERE EXPERIMENT_ID = ?";
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1, instrument.getInstrumentId());
			ps.setString(2, experiment.getId());
			ps.executeUpdate();
		}
		ConnectionManager.releaseConnection(conn);
	}

	public static void updateExperiment(
			String newName, 
			String newDescription, 
			String newNotes, 
			String projectId,
			String experimentId) throws SQLException {

		Connection conn = ConnectionManager.getConnection();
		String query = "UPDATE EXPERIMENT SET EXPERIMENT_NAME = ?, EXPERIMENT_DESCRIPTION = ?, "
				+ "NOTES =?, PROJECT_ID = ? WHERE EXPERIMENT_ID = ?";
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1, newName);
			ps.setString(2, newDescription);
			ps.setString(3, newNotes);
			ps.setString(4, projectId);
			ps.setString(5, experimentId);
			ps.executeUpdate();
		}
		ConnectionManager.releaseConnection(conn);
	}

	public static ExperimentDesign getDesignForIDTExperiment(String experimentId) throws SQLException{

		ExperimentDesign design = new ExperimentDesign();
		Connection conn = ConnectionManager.getConnection();
		Collection<StockSample> stockSamples = IDTDataCache.getStockSamples();

		String query =
			"SELECT DISTINCT SAMPLE_ID, SAMPLE_NAME, USER_DESCRIPTION, DATE_CREATED, " +
			"STOCK_SAMPLE_ID FROM SAMPLE WHERE EXPERIMENT_ID = ? ORDER BY 1";
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1, experimentId);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
	
				String stockId = rs.getString("STOCK_SAMPLE_ID");
				StockSample ss = stockSamples.stream().
						filter(s -> s.getSampleId().equals(stockId)).
						findFirst().orElse(null);
				IDTExperimentalSample sample = new IDTExperimentalSample(
						rs.getString("SAMPLE_ID"),
						rs.getString("SAMPLE_NAME"),
						rs.getString("USER_DESCRIPTION"),
						new Date(rs.getDate("DATE_CREATED").getTime()),
						ss);
				design.addSample(sample,false);
			}
			rs.close();
		}
		ConnectionManager.releaseConnection(conn);
		
		// Add sample/control type
		design.addFactor(ReferenceSamplesManager.getSampleControlTypeFactor(),false);
		design.getSamples().forEach(s -> s.addDesignLevel(ReferenceSamplesManager.sampleLevel));
		return design;
	}

	public static IDTExperimentalSample getExperimentalSample(
		String sampleId, StockSample stockSample, Connection conn) throws SQLException {

		IDTExperimentalSample sample = null;
		String query =
			"SELECT SAMPLE_NAME, USER_DESCRIPTION, DATE_CREATED FROM SAMPLE WHERE SAMPLE_ID = ?";
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1, sampleId);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				sample = new IDTExperimentalSample(
						sampleId,
						rs.getString("SAMPLE_NAME"),
						rs.getString("USER_DESCRIPTION"),
						new Date(rs.getDate("DATE_CREATED").getTime()),
						stockSample);
			}
			rs.close();
		}
		return sample;
	}
	
	public static IDTExperimentalSample getExperimentalSampleById(
				String sampleId) throws SQLException {
		Connection conn = ConnectionManager.getConnection();
		IDTExperimentalSample sample = 
				getExperimentalSampleById(sampleId, conn);
		ConnectionManager.releaseConnection(conn);
		return sample;
	}	

	public static IDTExperimentalSample getExperimentalSampleById(
			String sampleId, Connection conn) throws SQLException {

		IDTExperimentalSample sample = null;
		String query =
			"SELECT SAMPLE_NAME, USER_DESCRIPTION, DATE_CREATED, "
			+ "STOCK_SAMPLE_ID FROM SAMPLE WHERE SAMPLE_ID = ?";
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1, sampleId);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
	
				StockSample stockSample = IDTDataCache.getStockSampleById(rs.getString("STOCK_SAMPLE_ID"));
				sample = new IDTExperimentalSample(
						sampleId,
						rs.getString("SAMPLE_NAME"),
						rs.getString("USER_DESCRIPTION"),
						new Date(rs.getDate("DATE_CREATED").getTime()),
						stockSample);
			}
			rs.close();
		}
		return sample;
	}
	
	public static Collection<IDTExperimentalSample> getExperimentalSamples(
			Collection<String> sampleIdList, Connection conn) throws SQLException {

		Collection<IDTExperimentalSample> sampleList = 
				new ArrayList<IDTExperimentalSample>();
		Collection<String>cleanList = 
				sampleIdList.stream().collect(Collectors.toCollection(TreeSet::new));
		String query =
			"SELECT SAMPLE_NAME, USER_DESCRIPTION, DATE_CREATED, "
			+ "STOCK_SAMPLE_ID FROM SAMPLE WHERE SAMPLE_ID = ?";
		try(PreparedStatement ps = conn.prepareStatement(query)){		
			for(String sampleId : cleanList) {			
				ps.setString(1, sampleId);
				ResultSet rs = ps.executeQuery();
				while (rs.next()) {
	
					StockSample stockSample = IDTDataCache.getStockSampleById(rs.getString("STOCK_SAMPLE_ID"));
					IDTExperimentalSample sample = new IDTExperimentalSample(
							sampleId,
							rs.getString("SAMPLE_NAME"),
							rs.getString("USER_DESCRIPTION"),
							new Date(rs.getDate("DATE_CREATED").getTime()),
							stockSample);
					sampleList.add(sample);
				}
				rs.close();
			}
		}
		return sampleList;
	}

	public static Collection<StockSample> getStockSampleList() throws SQLException{

		Collection<StockSample> stockSamples = new TreeSet<StockSample>();
		Connection conn = ConnectionManager.getConnection();
		String query  =
				"SELECT DISTINCT STOCK_SAMPLE_ID, SAMPLE_NAME, DESCRIPTION, "
				+ "DATE_CREATED, SAMPLE_TYPE_ID, EXTERNAL_ID, EXTERNAL_SOURCE, "
				+ "TAX_ID, LIMS_SAMPLE_ID, LIMS_EXPERIMENT_ID " +
				"FROM STOCK_SAMPLE ORDER BY 1 ";
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				Date dc = null;
				if(rs.getDate("DATE_CREATED") != null)
					dc = new Date(rs.getDate("DATE_CREATED").getTime());
	
				LIMSSampleType sampleType = getSampleTypeById(rs.getString("SAMPLE_TYPE_ID"), conn);
				LIMSBioSpecies species = getSpeciesByTaxonomyId(rs.getInt("TAX_ID"), conn);
	
				StockSample sample = new StockSample(
						rs.getString("STOCK_SAMPLE_ID"),
						rs.getString("SAMPLE_NAME"),
						rs.getString("DESCRIPTION"),
						sampleType,
						dc,
						rs.getString("LIMS_SAMPLE_ID"),
						rs.getString("EXTERNAL_ID"),
						rs.getString("EXTERNAL_SOURCE"),
						species);
				
				String limsExperimentId = rs.getString("LIMS_EXPERIMENT_ID");
				if(limsExperimentId != null)
					sample.setLimsExperiment(LIMSDataCache.getExperimentById(limsExperimentId));
	
				stockSamples.add(sample);		
			}
			rs.close();
		}
		ConnectionManager.releaseConnection(conn);
		return stockSamples;
	}

	public static LIMSSampleType getSampleTypeById(
			String sampleTypeId, Connection conn) throws SQLException{

		LIMSSampleType type = null;
		String query = "SELECT DESCRIPTION FROM SAMPLE_TYPE WHERE SAMPLE_TYPE_ID = ?";
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1, sampleTypeId);
			ResultSet rs = ps.executeQuery();
			while(rs.next()) {
				type = new LIMSSampleType(sampleTypeId, rs.getString("DESCRIPTION"));
			}
			rs.close();
		}
		return type;
	}

	public static LIMSBioSpecies getSpeciesByTaxonomyId(
			Integer taxonomyId, Connection conn) throws SQLException{

		LIMSBioSpecies species = new LIMSBioSpecies(taxonomyId);
		String query =
			"SELECT NAME_TXT, NAME_CLASS FROM TAXONOMY_NAME WHERE TAX_ID = ?";
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ps.setInt(1, taxonomyId);
			ResultSet rs = ps.executeQuery();
			while (rs.next())
				species.addName(rs.getString("NAME_TXT"), rs.getString("NAME_CLASS"));
			
			rs.close();
		}
		return species;
	}

	public static void deleteProject(LIMSProject project) throws SQLException{

		Connection conn = ConnectionManager.getConnection();
		String query = "DELETE FROM PROJECT WHERE PROJECT_ID = ?";
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1, project.getId());
			ps.executeUpdate();
		}
		ConnectionManager.releaseConnection(conn);
	}

	public static void deleteExperiment(LIMSExperiment experiment) throws SQLException{

		Connection conn = ConnectionManager.getConnection();
		String query = 
				"SELECT DISTINCT S.EXPERIMENT_ID  " +
				"FROM PREPARED_SAMPLE P, SAMPLE S " +
				"WHERE S.SAMPLE_ID = P.SAMPLE_ID " +
				"AND P.SAMPLE_PREP_ID = ? ";
		TreeSet<String>expIds = new TreeSet<String>();
		Collection<LIMSSamplePreparation> preps = 
				IDTDataCache.getExperimentSamplePrepMap().get(experiment);
		
		Collection<LIMSSamplePreparation>prepsToDelete = 
				new HashSet<LIMSSamplePreparation>();
		
		try(PreparedStatement ps = conn.prepareStatement(query)){
			if(preps != null) {
				
				for(LIMSSamplePreparation prep : preps) {
					
					expIds.clear();
					ps.setString(1, prep.getId());
					ResultSet rs = ps.executeQuery();
					while(rs.next())
						expIds.add(rs.getString("EXPERIMENT_ID"));
					
					rs.close();
					
					if(expIds.size() == 1 && expIds.iterator().next().equals(experiment.getId()))
						prepsToDelete.add(prep);
				}
			}
		}
		query = "DELETE FROM EXPERIMENT WHERE EXPERIMENT_ID = ?";
		try(PreparedStatement ps = conn.prepareStatement(query)){		
			ps.setString(1, experiment.getId());
			ps.executeUpdate();
		}
		//	Delete preps for this experiment only
		if(!prepsToDelete.isEmpty()) {
			
			query = "DELETE FROM SAMPLE_PREPARATION WHERE SAMPLE_PREP_ID = ?";
			try(PreparedStatement ps = conn.prepareStatement(query)){
			
				for(LIMSSamplePreparation prep : prepsToDelete) {					
					ps.setString(1, prep.getId());
					ps.addBatch();				
				}
				ps.executeBatch();
			}			
		}
		ConnectionManager.releaseConnection(conn);
	}

	public static Collection<? extends DataExtractionMethod> getDataExtractionMethodList() throws SQLException{

		Collection<DataExtractionMethod>methodList = 
				new TreeSet<DataExtractionMethod>();
		Connection conn = ConnectionManager.getConnection();
		String query  =
			"SELECT EXTRACTION_METHOD_ID, METHOD_NAME, METHOD_DESCRIPTION, "
			+ "CREATED_BY, CREATED_ON, SOFTWARE_ID, METHOD_MD5 " +
			"FROM DATA_EXTRACTION_METHOD ORDER BY 1 ";
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				Date createdOn = null;
				if(rs.getDate("CREATED_ON") != null)
					createdOn = new Date(rs.getDate("CREATED_ON").getTime());
				
				String userId = rs.getString("CREATED_BY");
				LIMSUser createBy = null;
				if(userId != null)
					createBy = IDTDataCache.getUserById(userId);
	
				DataExtractionMethod method = new DataExtractionMethod(
						rs.getString("EXTRACTION_METHOD_ID"),
						rs.getString("METHOD_NAME"),
						rs.getString("METHOD_DESCRIPTION"),
						createBy,
						createdOn);
				
				String softwareId = rs.getString("SOFTWARE_ID");
				if(softwareId != null)
					method.setSoftware(IDTDataCache.getSoftwareById(softwareId));
				
				String md5 = rs.getString("METHOD_MD5");
				if(md5 != null)
					method.setMd5(md5);
				
				methodList.add(method);
			}
			rs.close();
		}
		ConnectionManager.releaseConnection(conn);
		return methodList;
	}

	public static Collection<? extends Manufacturer> getManufacturerList() throws SQLException{

		Collection<Manufacturer>manufacturers = new TreeSet<Manufacturer>();
		Connection conn = ConnectionManager.getConnection();
		String query  =
			"SELECT MANUFACTURER_ID, SUPPLIER_NAME, CATALOGUE_WEB_PAGE FROM MANUFACTURER";
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				Manufacturer method = new Manufacturer(
						rs.getString("MANUFACTURER_ID"),
						rs.getString("SUPPLIER_NAME"),
						rs.getString("CATALOGUE_WEB_PAGE"));
				manufacturers.add(method);
			}
			rs.close();
		}
		ConnectionManager.releaseConnection(conn);
		return manufacturers;
	}
	
	public static String addNewManufacturer(Manufacturer newManufacturer) throws SQLException{
		
		Connection conn = ConnectionManager.getConnection();
		String id = SQLUtils.getNextIdFromSequence(conn, 
				"MANUFACTURER_SEQ",
				DataPrefix.MANUFACTURER,
				"0",
				5);
		newManufacturer.setId(id);	
		String query  =
			"INSERT INTO MANUFACTURER(MANUFACTURER_ID, "
			+ "SUPPLIER_NAME, CATALOGUE_WEB_PAGE) " +
			"VALUES(?, ?, ?)";
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1, id);
			ps.setString(2, newManufacturer.getName());
			ps.setString(3, newManufacturer.getCatalogWebAddress());
			ps.executeUpdate();
		}
		ConnectionManager.releaseConnection(conn);
		return id;
	}
	
	public static void updateManufacturer(Manufacturer manufacturerToEdit) throws SQLException{
		
		Connection conn = ConnectionManager.getConnection();
		String query  =
			"UPDATE MANUFACTURER SET SUPPLIER_NAME = ?, "
			+ "CATALOGUE_WEB_PAGE = ? WHERE MANUFACTURER_ID = ?";
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1, manufacturerToEdit.getName());
			ps.setString(2, manufacturerToEdit.getCatalogWebAddress());
			ps.setString(3, manufacturerToEdit.getId());
			ps.executeUpdate();
		}
		ConnectionManager.releaseConnection(conn);
	}
	
	public static void deleteManufacturer(Manufacturer manufacturerToDelete) throws SQLException{
		
		Connection conn = ConnectionManager.getConnection();
		String query  =
			"DELETE FROM MANUFACTURER WHERE MANUFACTURER_ID = ?";
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1, manufacturerToDelete.getId());
			ps.executeUpdate();
		}
		ConnectionManager.releaseConnection(conn);
	}
	
	public static Collection<? extends DataProcessingSoftware>getSoftwareList() throws SQLException {

		Collection<DataProcessingSoftware>softwareList = new TreeSet<DataProcessingSoftware>();
		Connection conn = ConnectionManager.getConnection();
		String query  =
			"SELECT SOFTWARE_ID, SOFTWARE_TYPE, SOFTWARE_NAME, "
			+ "SOFTWARE_DESCRIPTION, MANUFACTURER_ID, PLATFORM_ID "
			+ "FROM DATA_ANALYSIS_SOFTWARE";
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {			
				Manufacturer vendor = 
						IDTDataCache.getManufacturerById(rs.getString("MANUFACTURER_ID"));
				SoftwareType type = SoftwareType.getOptionByName(rs.getString("SOFTWARE_TYPE"));		
				DataProcessingSoftware item = new DataProcessingSoftware(
						rs.getString("SOFTWARE_ID"),
						type,
						rs.getString("SOFTWARE_NAME"),
						rs.getString("SOFTWARE_DESCRIPTION"),
						vendor);
				String platformId = rs.getString("PLATFORM_ID");
				if(platformId != null)
					item.setPlatform(LIMSDataCache.getInstrumentPlatformById(platformId));
				
				softwareList.add(item);
			}
			rs.close();
		}
		ConnectionManager.releaseConnection(conn);
		return softwareList;
	}
	
	public static String addNewSoftware(DataProcessingSoftware newSoftwareItem) throws SQLException{

		Connection conn = ConnectionManager.getConnection();
		String id = SQLUtils.getNextIdFromSequence(conn, 
				"SOFTWARE_SEQ",
				DataPrefix.SOFTWARE,
				"0",
				4);
		newSoftwareItem.setId(id);		
		String query  =
			"INSERT INTO DATA_ANALYSIS_SOFTWARE(SOFTWARE_ID, SOFTWARE_TYPE, "
			+ "SOFTWARE_NAME, SOFTWARE_DESCRIPTION, MANUFACTURER_ID) " +
			"VALUES(?, ?, ?, ?, ?)";
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1, id);
			ps.setString(2, newSoftwareItem.getSoftwareType().name());
			ps.setString(3, newSoftwareItem.getName());
			ps.setString(4, newSoftwareItem.getDescription());
			ps.setString(5, newSoftwareItem.getVendor().getId());
			ps.executeUpdate();
		}
		ConnectionManager.releaseConnection(conn);
		return id;
	}
	
	public static void updateSoftware(DataProcessingSoftware itemToEdit) throws SQLException{

		Connection conn = ConnectionManager.getConnection();	
		String query  =
			"UPDATE DATA_ANALYSIS_SOFTWARE SET SOFTWARE_NAME = ?, "
			+ "SOFTWARE_DESCRIPTION = ?, MANUFACTURER_ID = ?, SOFTWARE_TYPE = ? " +
			"WHERE SOFTWARE_ID = ?";
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1, itemToEdit.getName());		
			ps.setString(2, itemToEdit.getDescription());			
			ps.setString(3, itemToEdit.getVendor().getId());
			ps.setString(4, itemToEdit.getSoftwareType().name());
			ps.setString(5, itemToEdit.getId());
			ps.executeUpdate();
		}
		ConnectionManager.releaseConnection(conn);
	}
	
	public static void deleteSoftware(DataProcessingSoftware itemToDelete) throws SQLException {

		Connection conn = ConnectionManager.getConnection();	
		String query  =
			"DELETE FROM DATA_ANALYSIS_SOFTWARE WHERE SOFTWARE_ID = ?";
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1, itemToDelete.getId());
			ps.executeUpdate();
		}
		ConnectionManager.releaseConnection(conn);
	}

	public static String addNewDataExtractionMethod(
			DataExtractionMethod selectedMethod, File methodFile) throws SQLException, IOException{

		LIMSUser sysUser = selectedMethod.getCreatedBy();
		if(sysUser == null)
			return null;

		Connection conn = ConnectionManager.getConnection();
		String id = SQLUtils.getNextIdFromSequence(conn, 
				"ID_DATA_EXTRACTION_METHOD_SEQ",
				DataPrefix.DATA_EXTRACTION_METHOD,
				"0",
				4);
		selectedMethod.setId(id);		
		String query  =
			"INSERT INTO DATA_EXTRACTION_METHOD (EXTRACTION_METHOD_ID, METHOD_NAME, " +
			"METHOD_DESCRIPTION, CREATED_BY, CREATED_ON, METHOD_CONTAINER, SOFTWARE_ID) " +
			"VALUES (?, ?, ?, ?, ?, ?, ?)";
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1, id);
			ps.setString(2, selectedMethod.getName());
			ps.setString(3, selectedMethod.getDescription());
			ps.setString(4, sysUser.getId());
			ps.setDate(5, new java.sql.Date(new java.util.Date().getTime()));
	
			// Insert method file
			FileInputStream fis = DatabaseUtils.compressAndSetBlob(methodFile, ps, 6);
			if(selectedMethod.getSoftware() != null)
				ps.setString(7, selectedMethod.getSoftware().getId());
			else
				ps.setNull(7, java.sql.Types.NULL);
			
			ps.executeUpdate();		
			if(fis != null)
				fis.close();
		}
		DatabaseUtils.deleteTemporaryArchive(methodFile);
		ConnectionManager.releaseConnection(conn);
		return id;
	}	

	public static void updateDataExtractionMethod(
			DataExtractionMethod selectedMethod, 
			File methodFile) throws SQLException, IOException{

		Connection conn = ConnectionManager.getConnection();
		String query  = null;
		if(methodFile == null) {
			query  =
				"UPDATE DATA_EXTRACTION_METHOD SET METHOD_NAME = ?, "
				+ "METHOD_DESCRIPTION = ?, SOFTWARE_ID = ?" +
				"WHERE EXTRACTION_METHOD_ID = ?";
		}
		else {
			query  =
				"UPDATE DATA_EXTRACTION_METHOD SET METHOD_NAME = ?, "
				+ "METHOD_DESCRIPTION = ?, SOFTWARE_ID = ?, METHOD_CONTAINER = ? " +
				"WHERE EXTRACTION_METHOD_ID = ?";
		}
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1, selectedMethod.getName());
			ps.setString(2, selectedMethod.getDescription());
			if(selectedMethod.getSoftware() != null)
				ps.setString(3, selectedMethod.getSoftware().getId());
			else
				ps.setNull(3, java.sql.Types.NULL);
			
			if(methodFile == null) {
				ps.setString(4, selectedMethod.getId());
				ps.executeUpdate();
			}
			else {
				FileInputStream fis = DatabaseUtils.compressAndSetBlob(methodFile, ps, 4);
				ps.setString(5, selectedMethod.getId());
				ps.executeUpdate();		
				if(fis != null)
					fis.close();
			}

		}
		DatabaseUtils.deleteTemporaryArchive(methodFile);
		ConnectionManager.releaseConnection(conn);
	}

	public static void deleteDataExtractionMethod(DataExtractionMethod selectedMethod)  throws SQLException{

		Connection conn = ConnectionManager.getConnection();
		String query = "DELETE FROM DATA_EXTRACTION_METHOD WHERE EXTRACTION_METHOD_ID = ?";
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1, selectedMethod.getId());
			ps.executeUpdate();
		}
		ConnectionManager.releaseConnection(conn);
	}

	public static void getDataExtractionMethodFile(
			DataExtractionMethod selectedMethod, File destinationFolder)  throws SQLException{

		File zipFile = Paths.get(destinationFolder.getAbsolutePath(), 
				selectedMethod.getName() + ".zip").toFile();
		Connection conn = ConnectionManager.getConnection();
		String query = "SELECT METHOD_CONTAINER "
				+ "FROM DATA_EXTRACTION_METHOD WHERE EXTRACTION_METHOD_ID = ?";
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1, selectedMethod.getId());
			ResultSet rs = ps.executeQuery();		
			while(rs.next())
				DatabaseUtils.writeBlobToFile(zipFile, rs.getBinaryStream("METHOD_CONTAINER"));
			
			rs.close();
		}
		ConnectionManager.releaseConnection(conn);
		if(zipFile.exists())
			CompressionUtils.extractArchiveAndCleanUp(zipFile, destinationFolder);
	}

	public static Map<LIMSExperiment, Collection<LIMSSamplePreparation>> getExperimentSamplePrepMap() throws SQLException {

		Map<LIMSExperiment, Collection<LIMSSamplePreparation>> prepMap =
				new TreeMap<LIMSExperiment, Collection<LIMSSamplePreparation>>();

		Connection conn = ConnectionManager.getConnection();
		String query  =
			"SELECT DISTINCT S.EXPERIMENT_ID, P.SAMPLE_PREP_ID  " +
			"FROM PREPARED_SAMPLE P, SAMPLE S " +
			"WHERE S.SAMPLE_ID = P.SAMPLE_ID ORDER BY 1,2 ";

		try(PreparedStatement ps = conn.prepareStatement(query)){
			ResultSet rs = ps.executeQuery();
			while(rs.next()) {	
				LIMSExperiment experiment =
						IDTDataCache.getExperimentById(rs.getString("EXPERIMENT_ID"));
				if(experiment != null) {
					prepMap.computeIfAbsent(experiment, v -> new TreeSet<LIMSSamplePreparation>());
					LIMSSamplePreparation prep =
							IDTDataCache.getSamplePrepById(rs.getString("SAMPLE_PREP_ID"));
					if(prep != null)
						prepMap.get(experiment).add(prep);
				}
			}
			rs.close();
		}
		ConnectionManager.releaseConnection(conn);
		return prepMap;
	}

	public static Map<LIMSSamplePreparation, Collection<DataAcquisitionMethod>> 
				getSamplePrepAcquisitionMethodMap() throws SQLException {

		Map<LIMSSamplePreparation, Collection<DataAcquisitionMethod>> prepMap =
				new TreeMap<LIMSSamplePreparation, Collection<DataAcquisitionMethod>>();

		Connection conn = ConnectionManager.getConnection();
		String query  =
			"SELECT DISTINCT P.SAMPLE_PREP_ID, I.ACQUISITION_METHOD_ID " +
			"FROM PREPARED_SAMPLE P, INJECTION I " +
			"WHERE I.PREP_ITEM_ID = P.PREP_ITEM_ID ORDER BY 1,2 ";

		try(PreparedStatement ps = conn.prepareStatement(query)){
			ResultSet rs = ps.executeQuery();
			while(rs.next()) {
				LIMSSamplePreparation prep =
						IDTDataCache.getSamplePrepById(rs.getString("SAMPLE_PREP_ID"));
				if(prep != null) {
					prepMap.computeIfAbsent(prep, v -> new TreeSet<DataAcquisitionMethod>());	
					DataAcquisitionMethod acq =
							IDTDataCache.getAcquisitionMethodById(rs.getString("ACQUISITION_METHOD_ID"));
					if(acq != null)
						prepMap.get(prep).add(acq);
				}
			}
			rs.close();
		}
		ConnectionManager.releaseConnection(conn);
		return prepMap;
	}

	public static Map<DataAcquisitionMethod, Collection<DataExtractionMethod>> 
						getAcquisitionDataExtractionMethodMap() throws SQLException {

		Map<DataAcquisitionMethod, Collection<DataExtractionMethod>> methodMap =
				new TreeMap<DataAcquisitionMethod, Collection<DataExtractionMethod>>();

		Connection conn = ConnectionManager.getConnection();
		String query  =
				"SELECT DISTINCT P.SAMPLE_PREP_ID, I.ACQUISITION_METHOD_ID, M.EXTRACTION_METHOD_ID " +
				"FROM PREPARED_SAMPLE P, INJECTION I " +
				"LEFT JOIN DATA_ANALYSIS_MAP M " +
				"ON I.INJECTION_ID = M.INJECTION_ID " +
				"WHERE I.PREP_ITEM_ID = P.PREP_ITEM_ID " +
				"ORDER BY 1,2, 3 ";

		try(PreparedStatement ps = conn.prepareStatement(query)){
			ResultSet rs = ps.executeQuery();
			while(rs.next()) {
				DataAcquisitionMethod acq =
						IDTDataCache.getAcquisitionMethodById(rs.getString("ACQUISITION_METHOD_ID"));
				if(acq != null) {
					methodMap.computeIfAbsent(acq, v -> new TreeSet<DataExtractionMethod>());
					DataExtractionMethod dex =
							IDTDataCache.getDataExtractionMethodById(rs.getString("EXTRACTION_METHOD_ID"));
					if(dex != null)
						methodMap.get(acq).add(dex);
				}
			}
			rs.close();
		}
		ConnectionManager.releaseConnection(conn);
		return methodMap;
	}
	
	public static Map<LIMSSamplePreparation, Collection<DataPipeline>> 
						getSamplePrepDataPipelineMap() throws SQLException {

		Map<LIMSSamplePreparation, Collection<DataPipeline>> prepMap =
				new TreeMap<LIMSSamplePreparation, Collection<DataPipeline>>();

		Connection conn = ConnectionManager.getConnection();
		String query  =
				"SELECT DISTINCT P.SAMPLE_PREP_ID, I.ACQUISITION_METHOD_ID, M.EXTRACTION_METHOD_ID " +
				"FROM PREPARED_SAMPLE P, INJECTION I, DATA_ANALYSIS_MAP M " +
				"WHERE I.PREP_ITEM_ID = P.PREP_ITEM_ID " +
				"AND I.INJECTION_ID = M.INJECTION_ID " +
				"ORDER BY 1,2, 3";

		try(PreparedStatement ps = conn.prepareStatement(query)){
			ResultSet rs = ps.executeQuery();
			while(rs.next()) {
				LIMSSamplePreparation prep =
						IDTDataCache.getSamplePrepById(rs.getString("SAMPLE_PREP_ID"));
				if(prep != null) {
					prepMap.computeIfAbsent(prep, v -> new TreeSet<DataPipeline>());	
					DataAcquisitionMethod acq =
							IDTDataCache.getAcquisitionMethodById(rs.getString("ACQUISITION_METHOD_ID"));
					DataExtractionMethod daMethod = 
							IDTDataCache.getDataExtractionMethodById(rs.getString("EXTRACTION_METHOD_ID"));
					if(acq != null && daMethod != null) 
						prepMap.get(prep).add(new DataPipeline(acq, daMethod));				
				}
			}
			rs.close();
		}
		ConnectionManager.releaseConnection(conn);
		return prepMap;
	}

	/**
	 * This method will purge all results
	 * for selected experiment and data acquisition method combination
	 * including injection data
	 *
	 * @param experiment
	 * @param acqMethod
	 * @throws SQLException
	 */
	public static void deleteAcquisitionMethodFromExperiment(
			LIMSExperiment experiment,
			DataAcquisitionMethod acqMethod) throws SQLException{

		Connection conn = ConnectionManager.getConnection();
		String query  =
			"DELETE FROM INJECTION J WHERE J.INJECTION_ID IN ( " +
			"SELECT DISTINCT I.INJECTION_ID FROM INJECTION I, PREPARED_SAMPLE P, SAMPLE S " +
			"WHERE S.EXPERIMENT_ID = ? " +
			"AND P.SAMPLE_ID = S.SAMPLE_ID " +
			"AND I.PREP_ITEM_ID = P.PREP_ITEM_ID " +
			"AND I.ACQUISITION_METHOD_ID = ?) ";

		try(PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1, experiment.getId());
			ps.setString(2, acqMethod.getId());
			ps.executeUpdate();
		}
		ConnectionManager.releaseConnection(conn);
	}

	/**
	 * This method will purge all data analysis results
	 * for selected experiment, data acquisition,
	 * data analysis method combination
	 *
	 * @param experiment
	 * @param dexMethod
	 * @param acqMethod
	 * @throws SQLException
	 */
	public static void deleteDataExtractionFromAcquisitionMethod(
			LIMSExperiment experiment,
			DataExtractionMethod dexMethod,
			DataAcquisitionMethod acqMethod)  throws SQLException{

		Connection conn = ConnectionManager.getConnection();
		String query  =
			"DELETE FROM DATA_ANALYSIS_MAP M  " +
			"WHERE M.INJECTION_ID IN ( " +
			"SELECT DISTINCT I.INJECTION_ID  " +
			"FROM INJECTION I, PREPARED_SAMPLE P, SAMPLE S " +
			"WHERE S.EXPERIMENT_ID = ? " +
			"AND P.SAMPLE_ID = S.SAMPLE_ID " +
			"AND I.PREP_ITEM_ID = P.PREP_ITEM_ID " +
			"AND I.ACQUISITION_METHOD_ID = ?) " +
			"AND M.EXTRACTION_METHOD_ID = ? ";

		try(PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1, experiment.getId());
			ps.setString(2, acqMethod.getId());
			ps.setString(3, dexMethod.getId());
			ps.executeUpdate();
		}
		ConnectionManager.releaseConnection(conn);
	}

	public static Collection<LIMSProtocol> getProtocols() throws SQLException{

		Collection<LIMSProtocol>protocols = new TreeSet<LIMSProtocol>();
		Connection conn = ConnectionManager.getConnection();
		String query  =
			"SELECT SOP_ID, SOP_GROUP, SOP_NAME, SOP_DESCRIPTION, SOP_VERSION, " +
			"DATE_CRERATED, CREATED_BY, SOP_CATEGORY " +
			"FROM SOP_PROTOCOL ORDER BY 1 ";
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
	
				Date createdOn = null;
				if(rs.getDate("DATE_CRERATED") != null)
					createdOn = new Date(rs.getDate("DATE_CRERATED").getTime());
	
				LIMSUser user = IDTDataCache.getUserById(rs.getString("CREATED_BY"));	
				LIMSProtocol protocol = new LIMSProtocol(
						rs.getString("SOP_ID"),
						rs.getString("SOP_GROUP"),
						rs.getString("SOP_NAME"),
						rs.getString("SOP_DESCRIPTION"),
						rs.getString("SOP_VERSION"),
						createdOn,
						user);	
				SopCategory sopCategory = 
						IDTDataCache.getSopSopCategoryById(rs.getString("SOP_CATEGORY"));
				protocol.setSopCategory(sopCategory);
				protocols.add(protocol);
			}
			rs.close();
		}
		ConnectionManager.releaseConnection(conn);
		return protocols;
	}

	public static Collection<LIMSSamplePreparation> getSamplePreps() throws SQLException, CDKException, IOException{

		Collection<LIMSSamplePreparation>samplePreps = new TreeSet<LIMSSamplePreparation>();
		Connection conn = ConnectionManager.getConnection();
		Map<String,Collection<String>>sopMap = new TreeMap<String,Collection<String>>();
		String query  =
			"SELECT SAMPLE_PREP_ID, SOP_ID FROM SOP_SAMPLE_PREP_MAP ORDER BY 1,2 ";

		try(PreparedStatement ps = conn.prepareStatement(query)){
		ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				String prepId = rs.getString("SAMPLE_PREP_ID");
				String sopId = rs.getString("SOP_ID");
				if(!sopMap.containsKey(prepId))
					sopMap.put(prepId, new TreeSet<String>());
	
				sopMap.get(prepId).add(sopId);
			}
			rs.close();
		}
		query  =
			"SELECT SAMPLE_PREP_ID, TITLE, PREP_DATE, CREATOR " +
			"FROM SAMPLE_PREPARATION ORDER BY 1 ";
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
	
				Date createdOn = null;
				if(rs.getDate("PREP_DATE") != null)
					createdOn = new Date(rs.getDate("PREP_DATE").getTime());
	
				LIMSUser user = IDTDataCache.getUserById(rs.getString("CREATOR"));
				LIMSSamplePreparation samplePrep = new LIMSSamplePreparation(
						rs.getString("SAMPLE_PREP_ID"),
						rs.getString("TITLE"),
						createdOn,
						user);
	
				if(sopMap.get(samplePrep.getId()) != null) {
	
					sopMap.get(samplePrep.getId()).
						forEach(m -> samplePrep.addProtocol(IDTDataCache.getProtocolById(m)));
				}
				samplePreps.add(samplePrep);
			}
			rs.close();
		}
		query  =
			"SELECT PREP_ITEM_ID, SAMPLE_ID FROM PREPARED_SAMPLE "
			+ "WHERE SAMPLE_PREP_ID = ? ORDER BY 1 ";
		try(PreparedStatement ps = conn.prepareStatement(query)){

			for(LIMSSamplePreparation prep : samplePreps) {
	
				ps.setString(1, prep.getId());
				ResultSet rs = ps.executeQuery();
				while (rs.next())
					prep.addPrepItem(rs.getString("SAMPLE_ID"), rs.getString("PREP_ITEM_ID"));
	
				rs.close();
				
				Collection<ObjectAnnotation>prepAnnotations = 
						AnnotationUtils.getObjectAnnotations(
								AnnotatedObjectType.SAMPLE_PREP, prep.getId(), conn);
				if(!prepAnnotations.isEmpty())
					prep.getAnnotations().addAll(prepAnnotations);
			}
		}
		ConnectionManager.releaseConnection(conn);
		return samplePreps;
	}

	public static void deleteSamplePreparation(LIMSSamplePreparation prep) throws SQLException{

		Connection conn = ConnectionManager.getConnection();
		String query = "DELETE FROM SAMPLE_PREPARATION WHERE SAMPLE_PREP_ID = ?";
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1, prep.getId());
			ps.executeUpdate();
		}
		ConnectionManager.releaseConnection(conn);
	}

	public static void deleteProtocol(LIMSProtocol protocol) throws SQLException{

		Connection conn = ConnectionManager.getConnection();
		String query = "DELETE FROM SOP_PROTOCOL WHERE SOP_ID = ?";
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1, protocol.getSopId());
			ps.executeUpdate();
		}
		ConnectionManager.releaseConnection(conn);
	}

	public static Collection<? extends SopCategory> getSopCategories() throws SQLException{

		Collection<SopCategory> sopCategories = new TreeSet<SopCategory>();
		Connection conn = ConnectionManager.getConnection();
		String query  =
			"SELECT PROTOCOL_CATEGORY, DESCRIPTION FROM SOP_CATEGORY ORDER BY 1";
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
	
				SopCategory sc = new SopCategory(
						rs.getString("PROTOCOL_CATEGORY"),
						rs.getString("DESCRIPTION"));
	
				sopCategories.add(sc);
			}
			rs.close();
		}
		ConnectionManager.releaseConnection(conn);
		return sopCategories;
	}

	public static void getSopProtocolFile(LIMSProtocol protocol, File destination) throws SQLException{

		Connection conn = ConnectionManager.getConnection();
		String query = "SELECT FILE_EXTENSION, SOP_DOCUMENT FROM SOP_PROTOCOL WHERE SOP_ID = ?";
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1, protocol.getSopId());
			ResultSet rs = ps.executeQuery();	
			while(rs.next()) {
	
			   File sopFile = Paths.get(destination.getAbsolutePath(),
					   protocol.getSopName().replaceAll("\\W+", " ") + "." +
					   rs.getString("FILE_EXTENSION")).toFile();
			   DatabaseUtils.writeBlobToFile(sopFile, rs.getBinaryStream("SOP_DOCUMENT"));
			}
			rs.close();
		}
		ConnectionManager.releaseConnection(conn);
	}

	public static void addNewSop(LIMSProtocol protocol, File sopFile) throws SQLException, IOException{

		Connection conn = ConnectionManager.getConnection();
		String query  =
			"INSERT INTO SOP_PROTOCOL (SOP_ID, SOP_GROUP, SOP_NAME, SOP_DESCRIPTION, SOP_VERSION, "+
			"DATE_CRERATED, CREATED_BY, SOP_CATEGORY, FILE_EXTENSION, SOP_DOCUMENT) " +
			"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		
		String sopId = SQLUtils.getNextIdFromSequence(conn, 
				"ID_SOP_SEQ",
				DataPrefix.SOP_PROTOCOL,
				"0",
				5);
		String sopGroupId = SQLUtils.getNextIdFromSequence(conn, 
				"ID_SOP_GROUP_SEQ",
				DataPrefix.SOP_PROTOCOL_GROUP,
				"0",
				4);
		protocol.setSopId(sopId);
		protocol.setSopGroup(sopGroupId);
		
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1, sopId);
			ps.setString(2, sopGroupId);
			
			ps.setString(3, protocol.getSopName());
			ps.setString(4, protocol.getSopDescription());
			ps.setString(5, protocol.getSopVersion());
			ps.setDate(6, new java.sql.Date(new java.util.Date().getTime()));
			ps.setString(7, protocol.getCreatedBy().getId());
			ps.setString(8, protocol.getSopCategory().getCategoryId());
			ps.setString(9, FilenameUtils.getExtension(sopFile.getAbsolutePath()));
			// Insert protocol file
			FileInputStream fis = DatabaseUtils.setBlob(sopFile, ps, 10);
			ps.executeUpdate();
			if(fis != null)
				fis.close();
		}
		ConnectionManager.releaseConnection(conn);
	}

	public static void addNewSopVersion(LIMSProtocol protocol, File sopFile) throws SQLException, IOException{

		Connection conn = ConnectionManager.getConnection();
		String query  =
			"INSERT INTO SOP_PROTOCOL (SOP_ID, SOP_GROUP, SOP_NAME, SOP_DESCRIPTION, SOP_VERSION, "+
			"DATE_CRERATED, CREATED_BY, SOP_CATEGORY, FILE_EXTENSION, SOP_DOCUMENT) " +
			"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";		
		String nextId = SQLUtils.getNextIdFromSequence(conn, 
				"ID_SOP_SEQ",
				DataPrefix.SOP_PROTOCOL,
				"0",
				5);
		protocol.setSopId(nextId);
		
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1, nextId);		
			ps.setString(2, protocol.getSopGroup());
			ps.setString(3, protocol.getSopName());
			ps.setString(4, protocol.getSopDescription());
			ps.setString(5, protocol.getSopVersion());
			ps.setDate(6, new java.sql.Date(new java.util.Date().getTime()));		
			ps.setString(7, protocol.getCreatedBy().getId());
			ps.setString(8, protocol.getSopCategory().getCategoryId());
			ps.setString(9, FilenameUtils.getExtension(sopFile.getAbsolutePath()));
	
			FileInputStream fis = DatabaseUtils.setBlob(sopFile, ps, 10);
			ps.executeUpdate();
			if(fis != null)
				fis.close();
		}
		ConnectionManager.releaseConnection(conn);
	}

	public static void updateSopMetadata(LIMSProtocol protocol) throws SQLException{

		Connection conn = ConnectionManager.getConnection();
		String query  = null;
		query  =
			"UPDATE SOP_PROTOCOL SET SOP_NAME = ?, SOP_DESCRIPTION = ?, " +
			"WHERE SOP_ID = ?";
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1, protocol.getSopName());
			ps.setString(2, protocol.getSopDescription());
			ps.executeUpdate();
		}
		ConnectionManager.releaseConnection(conn);
	}

	public static Collection<IDTExperimentalSample> getSamplesForPrep(LIMSSamplePreparation prep) throws SQLException{

		Connection conn = ConnectionManager.getConnection();
		Collection<IDTExperimentalSample> prepSamples = new TreeSet<IDTExperimentalSample>();
		Collection<StockSample> stockSamples = IDTDataCache.getStockSamples();
		String query =
			"SELECT DISTINCT S.SAMPLE_ID, S.SAMPLE_NAME, S.USER_DESCRIPTION, S.DATE_CREATED, " +
			"S.STOCK_SAMPLE_ID FROM SAMPLE S, PREPARED_SAMPLE P WHERE S.SAMPLE_ID = P.SAMPLE_ID " +
			"AND P.SAMPLE_PREP_ID = ? ORDER BY 1";
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1, prep.getId());
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {	
				String stockId = rs.getString("STOCK_SAMPLE_ID");
				StockSample ss = stockSamples.stream().
						filter(s -> s.getSampleId().equals(stockId)).
						findFirst().orElse(null);
				IDTExperimentalSample sample = new IDTExperimentalSample(
						rs.getString("SAMPLE_ID"),
						rs.getString("SAMPLE_NAME"),
						rs.getString("USER_DESCRIPTION"),
						new Date(rs.getDate("DATE_CREATED").getTime()),
						ss);
				prepSamples.add(sample);
			}
			rs.close();
		}
		ConnectionManager.releaseConnection(conn);
		return prepSamples;
	}

	public static Collection<LIMSProtocol> getSamplePrepSops(LIMSSamplePreparation prep) throws SQLException{

		Collection<LIMSProtocol>protocols = new TreeSet<LIMSProtocol>();
		Connection conn = ConnectionManager.getConnection();
		String query  =
			"SELECT DISTINCT SOP_ID FROM SOP_SAMPLE_PREP_MAP WHERE SAMPLE_PREP_ID = ? ";
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1, prep.getId());
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
	
				LIMSProtocol sop = IDTDataCache.getProtocolById(rs.getString("SOP_ID"));
				if(sop != null)
					protocols.add(sop);
			}
			rs.close();
		}
		ConnectionManager.releaseConnection(conn);
		return protocols;
	}

	public static String addNewSamplePrep(
			LIMSSamplePreparation prep,
			Collection<IDTExperimentalSample>prepSamples,
			Collection<LIMSProtocol>prepSops
			//	TODO add list of documents
			) throws SQLException {

		
		Connection conn = ConnectionManager.getConnection();
		String prepId = SQLUtils.getNextIdFromSequence(conn, 
				"ID_SAMPLE_PREP_SEQ",
				DataPrefix.SAMPLE_PREPARATION,
				"0",
				4);		
		String query = 
				"INSERT INTO SAMPLE_PREPARATION (SAMPLE_PREP_ID, TITLE, PREP_DATE, CREATOR) "
				+ "VALUES (?, ?, ?, ?)";
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1, prepId);
			ps.setString(2, prep.getName());
			ps.setDate(3, new java.sql.Date(prep.getPrepDate().getTime()));
			ps.setString(4, prep.getCreator().getId());
			ps.executeUpdate();
		}
		//	Insert SOPs
		query = "INSERT INTO SOP_SAMPLE_PREP_MAP (SAMPLE_PREP_ID, SOP_ID) VALUES (?,?)";
		try(PreparedStatement ps = conn.prepareStatement(query)){
			for(LIMSProtocol protocol : prepSops) {
				ps.setString(1, prepId);
				ps.setString(2, protocol.getSopId());
				ps.addBatch();
			}
			ps.executeBatch();
		}
		//	Insert prepped samples
		query =
			"INSERT INTO PREPARED_SAMPLE (PREP_ITEM_ID, SAMPLE_PREP_ID, SAMPLE_ID) " +
			"VALUES (?, ?, ?)";
		try(PreparedStatement ps = conn.prepareStatement(query)){
		
			ps.setString(2, prepId);
			for(IDTExperimentalSample sample : prepSamples) {
	
				String prepItemId = SQLUtils.getNextIdFromSequence(conn, 
						"ID_PREP_ITEM_SEQ",
						DataPrefix.PREPARED_SAMPLE,
						"0",
						7);
				ps.setString(1, prepItemId);			
				ps.setString(3, sample.getId());
				ps.addBatch();
			}
			ps.executeBatch();
		}
		//	TODO Insert documents
		ConnectionManager.releaseConnection(conn);
		return prepId;
	}
	
	public static String addNewSamplePrepWithSopsAndAnnotations(
			LIMSSamplePreparation prep,
			Collection<IDTExperimentalSample>prepSamples) throws SQLException, IOException {
		
		Connection conn = ConnectionManager.getConnection();
		String prepId = SQLUtils.getNextIdFromSequence(conn, 
				"ID_SAMPLE_PREP_SEQ",
				DataPrefix.SAMPLE_PREPARATION,
				"0",
				4);	
		prep.setId(prepId);
		String query = 
				"INSERT INTO SAMPLE_PREPARATION (SAMPLE_PREP_ID, TITLE, PREP_DATE, CREATOR) "
				+ "VALUES (?, ?, ?, ?)";
		try(PreparedStatement ps = conn.prepareStatement(query)){
			prep.setId(prepId);
			ps.setString(1, prepId);
			ps.setString(2, prep.getName());
			ps.setDate(3, new java.sql.Date(prep.getPrepDate().getTime()));
			ps.setString(4, prep.getCreator().getId());
			ps.executeUpdate();
		}
		//	Insert prepped samples
		Map<String,String>prepItemMap = new TreeMap<String,String>();
		query =
			"INSERT INTO PREPARED_SAMPLE (PREP_ITEM_ID, SAMPLE_PREP_ID, SAMPLE_ID) VALUES (?, ?, ?)";
		try(PreparedStatement ps = conn.prepareStatement(query)){
			
			ps.setString(2, prepId);
			for(IDTExperimentalSample sample : prepSamples) {			
				String prepItemId = SQLUtils.getNextIdFromSequence(conn, 
						"ID_PREP_ITEM_SEQ",
						DataPrefix.PREPARED_SAMPLE,
						"0",
						7);			
				prepItemMap.put(sample.getId(), prepItemId);
				ps.setString(1, prepItemId);			
				ps.setString(3, sample.getId());
				ps.addBatch();
			}
			ps.executeBatch();
		}
		prep.getPrepItemMap().clear();
		prep.getPrepItemMap().putAll(prepItemMap);
		
		//	Insert SOPs
		query = "INSERT INTO SOP_SAMPLE_PREP_MAP (SAMPLE_PREP_ID, SOP_ID) VALUES (?,?)";
		try(PreparedStatement ps = conn.prepareStatement(query)){
			
			ps.setString(1, prepId);
			for(LIMSProtocol protocol : prep.getProtocols()) {				
				ps.setString(2, protocol.getSopId());
				ps.addBatch();
			}
			ps.executeBatch();
		}
		if(!prep.getAnnotations().isEmpty()) {

			for(ObjectAnnotation annotation : prep.getAnnotations()) {
				annotation.setAnnotatedObjectId(prepId);
				AnnotationUtils.insertNewAnnotation(annotation, conn);
			}
		}		
		ConnectionManager.releaseConnection(conn);
		return prepId;
	}

	public static void updateBasicSamplePrepData(
			LIMSSamplePreparation prep2save) throws SQLException {

		Connection conn = ConnectionManager.getConnection();
		String query = "UPDATE SAMPLE_PREPARATION SET TITLE = ?,"
				+ " PREP_DATE = ?, CREATOR = ? WHERE SAMPLE_PREP_ID = ?";
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1, prep2save.getName());
			ps.setDate(2, new java.sql.Date(prep2save.getPrepDate().getTime()));
			ps.setString(3, prep2save.getCreator().getId());
			ps.setString(4, prep2save.getId());
			ps.executeUpdate();	
		}
		ConnectionManager.releaseConnection(conn);
	}
	
	public static void updateSamplePrepProtocols(
			LIMSSamplePreparation prep2save) throws SQLException {

		Connection conn = ConnectionManager.getConnection();
		String query = "DELETE FROM SOP_SAMPLE_PREP_MAP WHERE SAMPLE_PREP_ID = ?";
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1, prep2save.getId());
			ps.executeUpdate();	
		}
		query = "INSERT INTO SOP_SAMPLE_PREP_MAP (SAMPLE_PREP_ID, SOP_ID) VALUES (?,?)";
		try(PreparedStatement ps = conn.prepareStatement(query)){	
			ps.setString(1, prep2save.getId());
			for(LIMSProtocol sop : prep2save.getProtocols()) {
				ps.setString(2, sop.getSopId());
				ps.addBatch();
			}
			ps.executeBatch();	
		}
		ConnectionManager.releaseConnection(conn);
	}

	public static String addNewIDTSample(
			IDTExperimentalSample sample, LIMSExperiment selectedExperiment) throws SQLException {
		
		Connection conn = ConnectionManager.getConnection();
		String sampleId = SQLUtils.getNextIdFromSequence(conn, 
				"ID_SAMPLE_SEQ",
				DataPrefix.ID_SAMPLE,
				"0",
				6);
		String query  =
			"INSERT INTO SAMPLE (SAMPLE_ID, SAMPLE_NAME, EXPERIMENT_ID, " +
			"USER_DESCRIPTION, DATE_CREATED, STOCK_SAMPLE_ID) " +
			"VALUES (?, ?, ?, ?, ?, ?)";
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1, sampleId);
			ps.setString(2, sample.getName());
			ps.setString(3, selectedExperiment.getId());
			ps.setString(4, sample.getDescription());
			ps.setDate(5, new java.sql.Date(new java.util.Date().getTime()));	
			ps.setString(6, sample.getParentStockSample().getSampleId());
			ps.executeUpdate();
		}
		ConnectionManager.releaseConnection(conn);
		return sampleId;
	}

	public static void updateIDTSample(IDTExperimentalSample sample) throws SQLException {
		
		Connection conn = ConnectionManager.getConnection();
		String query  =
			"UPDATE SAMPLE SET SAMPLE_NAME = ?, USER_DESCRIPTION = ? WHERE SAMPLE_ID = ?";
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1, sample.getName());
			ps.setString(2, sample.getDescription());
			ps.setString(3, sample.getId());
			ps.executeUpdate();
		}
		ConnectionManager.releaseConnection(conn);
	}

	public static void deleteIDTSamples(Collection<IDTExperimentalSample> selectedSamples) throws SQLException {

		Connection conn = ConnectionManager.getConnection();
		String query = "DELETE FROM SAMPLE WHERE SAMPLE_ID = ?";
		try(PreparedStatement ps = conn.prepareStatement(query)){
			for(IDTExperimentalSample sample : selectedSamples) {
				ps.setString(1, sample.getId());
				ps.addBatch();
			}
			ps.executeBatch();
		}
		ConnectionManager.releaseConnection(conn);
	}

	public static Worklist getLimsWorlkistForPrep(
			LIMSSamplePreparation prep, LIMSExperiment experiment) throws SQLException {

		Worklist wkl = new Worklist();
		ExperimentDesign design = experiment.getExperimentDesign();
		Connection conn = ConnectionManager.getConnection();
		String query =
			"SELECT P.SAMPLE_ID, I.ACQUISITION_METHOD_ID, I.DATA_FILE_NAME,  " +
			"I.INJECTION_VOLUME, I.INJECTION_TIMESTAMP, I.PREP_ITEM_ID " +
			"FROM PREPARED_SAMPLE P, INJECTION I " +
			"WHERE P.SAMPLE_PREP_ID = ? " +
			"AND P.PREP_ITEM_ID = I.PREP_ITEM_ID " +
			"ORDER BY 1,2,3 ";
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1, prep.getId());
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
	
				DataAcquisitionMethod method = 
						IDTDataCache.getAcquisitionMethodById(rs.getString("ACQUISITION_METHOD_ID"));
				DataFile df = new DataFile(rs.getString("DATA_FILE_NAME"), method);
				LIMSWorklistItem item = new LIMSWorklistItem(df);
				item.setSamplePrep(prep);
				item.setPrepItemId(rs.getString("PREP_ITEM_ID"));
				item.setAcquisitionMethod(method);
				item.setInjectionVolume(rs.getDouble("INJECTION_VOLUME"));
				item.setTimeStamp(new Date(rs.getDate("INJECTION_TIMESTAMP").getTime()));
				
				ExperimentalSample sample = design.getSampleById(rs.getString("SAMPLE_ID"));
				if(sample == null) 
					logger.debug(String.format("Unable to find in the design sample ID %s", 
							"Instantiating node", rs.getString("SAMPLE_ID")));
				else 
					item.setSample(sample);
				
				wkl.addItem(item);
			}
			rs.close();
		}
		ConnectionManager.releaseConnection(conn);
		return wkl;
	}

	public static void uploadInjectionData(Worklist wkl) throws SQLException {

		List<LIMSWorklistItem> items = wkl.getTimeSortedWorklistItems().stream().
			filter(LIMSWorklistItem.class::isInstance).
			map(LIMSWorklistItem.class::cast).collect(Collectors.toList());

		if(items.isEmpty())
			return;

		Connection conn = ConnectionManager.getConnection();
		String query =
			"INSERT INTO INJECTION (INJECTION_ID, PREP_ITEM_ID, "
			+ "DATA_FILE_NAME, ACQUISITION_METHOD_ID, INJECTION_TIMESTAMP, INJECTION_VOLUME) "
			+ "VALUES (?, ?, ?, ?, ?, ?)";

		try(PreparedStatement ps = conn.prepareStatement(query)){
			for(LIMSWorklistItem item : items) {			
				String injectionId = SQLUtils.getNextIdFromSequence(conn, 
						"ID_INJECTION_SEQ",
						DataPrefix.INJECTION,
						"0",
						9);
				item.getDataFile().setInjectionId(injectionId);
				ps.setString(1, injectionId);
				ps.setString(2, item.getPrepItemId());
				ps.setString(3, item.getDataFile().getName());
				ps.setString(4, item.getAcquisitionMethod().getId());
				ps.setDate(5, new java.sql.Date(item.getTimeStamp().getTime()));
				ps.setDouble(6, item.getInjectionVolume());
				ps.addBatch();
			}
			ps.executeBatch();
		}
		ConnectionManager.releaseConnection(conn);
	}

	public static void mapInjectionsToDataFiles(Collection<DataFile>files, LIMSExperiment experiment) throws SQLException {

		Connection conn = ConnectionManager.getConnection();
		String query =
				"SELECT I.INJECTION_ID, I.ACQUISITION_METHOD_ID, I.INJECTION_TIMESTAMP " +
				"FROM INJECTION I, PREPARED_SAMPLE P, SAMPLE S " +
				"WHERE UPPER(I.DATA_FILE_NAME) = ? " +
				"AND I.PREP_ITEM_ID = P.PREP_ITEM_ID " +
				"AND P.SAMPLE_ID = S.SAMPLE_ID " +
				"AND S.EXPERIMENT_ID = ? ";

		try(PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(2, experiment.getId());
			for(DataFile df : files) {
	
				String fileName = FIOUtils.changeExtension(
						new File(df.getName()), "D").getName().toUpperCase();
				ps.setString(1, fileName);
				ResultSet rs = ps.executeQuery();
				while(rs.next()) {
					DataAcquisitionMethod method = 
							IDTDataCache.getAcquisitionMethodById(rs.getString("ACQUISITION_METHOD_ID"));
					Date timeStamp = new Date(rs.getDate("INJECTION_TIMESTAMP").getTime());
					df.setDataAcquisitionMethod(method);
					df.setInjectionTime(timeStamp);
					df.setInjectionId(rs.getString("INJECTION_ID"));
				}
				rs.close();
			}
		}
		ConnectionManager.releaseConnection(conn);
	}

	public static String addNewDataAnalysis(
			DataExtractionMethod method, String injectionId) throws SQLException {
		
		Connection conn = ConnectionManager.getConnection();
		String daId = addNewDataAnalysis(method, injectionId, conn);
		ConnectionManager.releaseConnection(conn);
		return daId;
	}
	
	public static String addNewDataAnalysis(
			DataExtractionMethod method, 
			String injectionId,
			Connection conn) throws SQLException {
		
		String daId = SQLUtils.getNextIdFromSequence(conn, 
				"ID_DATA_ANALYSIS_SEQ",
				DataPrefix.DATA_ANALYSIS,
				"0",
				10);		
		String query = "INSERT INTO DATA_ANALYSIS_MAP (DATA_ANALYSIS_ID, EXTRACTION_METHOD_ID, "
				+ "CREATED_ON, INJECTION_ID) VALUES (?, ?, ?, ?)";
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1, daId);
			ps.setString(2, method.getId());
			ps.setDate(3, new java.sql.Date((new Date()).getTime()));
			ps.setString(4, injectionId);
			ps.executeUpdate();
		}
		return daId;
	}

	public static Collection<String> getResultValueUnits() throws SQLException{

		Collection<String> resultValueUnits = new TreeSet<String>();
		Connection conn = ConnectionManager.getConnection();
		String query  =
			"SELECT UNITS FROM MEASUREMENT_UNIT "
			+ "WHERE TYPE IN ('concentration','MS') ORDER BY 1";
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ResultSet rs = ps.executeQuery();
			while (rs.next())
				resultValueUnits.add(rs.getString("UNITS"));
	
			rs.close();
		}
		ConnectionManager.releaseConnection(conn);
		return resultValueUnits;
	}

	public static Collection<String> getSampleQuantityUnits() throws SQLException{

		Collection<String> sampleQuantityUnits = new TreeSet<String>();
		Connection conn = ConnectionManager.getConnection();
		String query  =
			"SELECT UNITS FROM MEASUREMENT_UNIT "
			+ "WHERE TYPE NOT IN ('concentration','MS') ORDER BY 1";
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ResultSet rs = ps.executeQuery();
			while (rs.next())
				sampleQuantityUnits.add(rs.getString("UNITS"));
	
			rs.close();
		}
		ConnectionManager.releaseConnection(conn);
		return sampleQuantityUnits;
	}

	public static String addNewReferenceMS1DataBundle(
			LIMSExperiment experiment,
			ExperimentalSample selectedSample,
			DataAcquisitionMethod acquisitionMethod,
			DataExtractionMethod daMethod,
			Connection conn) throws SQLException{		
		String referenceMS1DataBundleId = SQLUtils.getNextIdFromSequence(conn, 
				"SOURCE_DATA_BUNDLE_ID_SEQ",
				DataPrefix.REF_MS_DATA_BUNDLE,
				"0",
				8);
		String query =
			"INSERT INTO POOLED_MS1_DATA_SOURCE "
			+ "(SOURCE_DATA_BUNDLE_ID, EXPERIMENT_ID, SAMPLE_ID, "
			+ "ACQ_METHOD_ID, EXTRACTION_METHOD_ID) VALUES (?, ?, ?, ?, ?)";
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1, referenceMS1DataBundleId);
			ps.setString(2, experiment.getId());
			ps.setString(3, selectedSample.getId());
			ps.setString(4, acquisitionMethod.getId());
			ps.setString(5, daMethod.getId());
			ps.executeUpdate();
		}
		return referenceMS1DataBundleId;
	}

	public static Collection<IDTMsSummary>getIDTMsOneSummary() throws SQLException{

		Collection<IDTMsSummary>summaryList = new ArrayList<IDTMsSummary>();
		Connection conn = ConnectionManager.getConnection();
		String query  =
			"SELECT R.SOURCE_DATA_BUNDLE_ID, R.EXPERIMENT_ID, S.SAMPLE_ID, S.SAMPLE_NAME,  " +
			"S.USER_DESCRIPTION, S.DATE_CREATED, S.STOCK_SAMPLE_ID, " +
			"R.ACQ_METHOD_ID, R.EXTRACTION_METHOD_ID,  " +
			"COUNT (F.POOLED_MS_FEATURE_ID) AS NUM_FEATURES " +
			"FROM POOLED_MS1_DATA_SOURCE R, SAMPLE S, " +
			"POOLED_MS1_FEATURE F " +
			"WHERE R.SOURCE_DATA_BUNDLE_ID = F.SOURCE_DATA_BUNDLE_ID " +
			"AND R.SAMPLE_ID = S.SAMPLE_ID " +
			"GROUP BY R.EXPERIMENT_ID, S.SAMPLE_ID, S.SAMPLE_NAME, S.USER_DESCRIPTION,  " +
			"S.DATE_CREATED, S.STOCK_SAMPLE_ID, R.ACQ_METHOD_ID, R.EXTRACTION_METHOD_ID, R.SOURCE_DATA_BUNDLE_ID " +
			"ORDER BY R.EXPERIMENT_ID, S.SAMPLE_ID, S.SAMPLE_NAME, S.USER_DESCRIPTION,  " +
			"S.DATE_CREATED, S.STOCK_SAMPLE_ID, R.ACQ_METHOD_ID, R.EXTRACTION_METHOD_ID, R.SOURCE_DATA_BUNDLE_ID ";

		try(PreparedStatement ps = conn.prepareStatement(query)){
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				IDTMsSummary summary = new IDTMsSummary(
						rs.getString("SOURCE_DATA_BUNDLE_ID"),
						rs.getInt("NUM_FEATURES"));	
				summary.setExperiment(IDTDataCache.getExperimentById(rs.getString("EXPERIMENT_ID")));
				String stockId = rs.getString("STOCK_SAMPLE_ID");
				StockSample ss = IDTDataCache.getStockSamples().stream().
						filter(s -> s.getSampleId().equals(stockId)).findFirst().orElse(null);
				IDTExperimentalSample sample = new IDTExperimentalSample(
						rs.getString("SAMPLE_ID"),
						rs.getString("SAMPLE_NAME"),
						rs.getString("USER_DESCRIPTION"),
						new Date(rs.getDate("DATE_CREATED").getTime()),
						ss);
				summary.setSample(sample);
				summary.setDataExtractionMethod(
						IDTDataCache.getDataExtractionMethodById(rs.getString("EXTRACTION_METHOD_ID")));
				summary.setAcquisitionMethod(
						IDTDataCache.getAcquisitionMethodById(rs.getString("ACQ_METHOD_ID")));	
				summaryList.add(summary);
			}
			rs.close();
		}
		ConnectionManager.releaseConnection(conn);
		return summaryList;
	}
	
	public static int getMS1featureCountForExperiment(String experimentId) throws SQLException {
		
		int count = 0;
		Connection conn = ConnectionManager.getConnection();
		String sql = 
				"SELECT COUNT (F.POOLED_MS_FEATURE_ID) AS NUM_FEATURES " +
				"FROM POOLED_MS1_DATA_SOURCE R, " +
				"POOLED_MS1_FEATURE F " +
				"WHERE R.SOURCE_DATA_BUNDLE_ID = F.SOURCE_DATA_BUNDLE_ID " +
				"AND R.EXPERIMENT_ID = ? ";
		try(PreparedStatement ps = conn.prepareStatement(sql)){
			ps.setString(1, experimentId);
			ResultSet rs = ps.executeQuery();
			while (rs.next())
				count = rs.getInt("NUM_FEATURES");
			
			rs.close();
		}
		ConnectionManager.releaseConnection(conn);
		return count;
	}

	public static Collection<IDTMsSummary>getIDTMsTwoSummary() throws SQLException{

		Collection<IDTMsSummary>summaryList = new ArrayList<IDTMsSummary>();
		Connection conn = ConnectionManager.getConnection();
		String query  =
			"SELECT S.EXPERIMENT_ID, S.SAMPLE_ID, S.SAMPLE_NAME, S.USER_DESCRIPTION, S.DATE_CREATED,  " +
			"S.STOCK_SAMPLE_ID, I.ACQUISITION_METHOD_ID, M.EXTRACTION_METHOD_ID, MSF.COLLISION_ENERGY, F.POLARITY, " +
			"COUNT (MSF.MSMS_FEATURE_ID) AS NUM_FEATURES " +
			"FROM MSMS_PARENT_FEATURE F, MSMS_FEATURE MSF, DATA_ANALYSIS_MAP M, " +
			"INJECTION I, PREPARED_SAMPLE P, SAMPLE S " +
			"WHERE MSF.PARENT_FEATURE_ID = F.FEATURE_ID " +
			"AND M.DATA_ANALYSIS_ID = F.DATA_ANALYSIS_ID " +
			"AND M.INJECTION_ID = I.INJECTION_ID " +
			"AND I.PREP_ITEM_ID = P.PREP_ITEM_ID " +
			"AND P.SAMPLE_ID = S.SAMPLE_ID " +
			"GROUP BY S.EXPERIMENT_ID, S.SAMPLE_ID, S.SAMPLE_NAME, S.USER_DESCRIPTION, S.DATE_CREATED,  " +
			"S.STOCK_SAMPLE_ID, I.ACQUISITION_METHOD_ID, M.EXTRACTION_METHOD_ID, MSF.COLLISION_ENERGY, F.POLARITY " +
			"ORDER BY S.EXPERIMENT_ID, F.POLARITY, MSF.COLLISION_ENERGY, S.SAMPLE_ID, S.SAMPLE_NAME, S.USER_DESCRIPTION, S.DATE_CREATED,  " +
			"S.STOCK_SAMPLE_ID, I.ACQUISITION_METHOD_ID, M.EXTRACTION_METHOD_ID ";

		try(PreparedStatement ps = conn.prepareStatement(query)){
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				IDTMsSummary summary = new IDTMsSummary(rs.getInt("NUM_FEATURES"));
				summary.setExperiment(IDTDataCache.getExperimentById(rs.getString("EXPERIMENT_ID")));
				String stockId = rs.getString("STOCK_SAMPLE_ID");
				StockSample ss = IDTDataCache.getStockSamples().stream().
						filter(s -> s.getSampleId().equals(stockId)).findFirst().orElse(null);
				IDTExperimentalSample sample = new IDTExperimentalSample(
						rs.getString("SAMPLE_ID"),
						rs.getString("SAMPLE_NAME"),
						rs.getString("USER_DESCRIPTION"),
						new Date(rs.getDate("DATE_CREATED").getTime()),
						ss);
				summary.setSample(sample);
				summary.setDataExtractionMethod(
						IDTDataCache.getDataExtractionMethodById(rs.getString("EXTRACTION_METHOD_ID")));
				summary.setAcquisitionMethod(
						IDTDataCache.getAcquisitionMethodById(rs.getString("ACQUISITION_METHOD_ID")));
				summary.setCollisionEnergy(rs.getDouble("COLLISION_ENERGY"));
				summaryList.add(summary);
			}
			rs.close();
		}
		ConnectionManager.releaseConnection(conn);
		return summaryList;
	}

	public static Collection<Double>getAvailableMsMsCollisionEnergies() throws SQLException{

		Collection<Double>collisionEnergies = new TreeSet<Double>();
		Connection conn = ConnectionManager.getConnection();
		String query = 
				"SELECT DISTINCT COLLISION_ENERGY FROM MSMS_FEATURE "
				+ "WHERE COLLISION_ENERGY > 0  ORDER BY 1";
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ResultSet rs = ps.executeQuery();
			while (rs.next())
				collisionEnergies.add(rs.getDouble("COLLISION_ENERGY"));
	
			rs.close();
		}
		ConnectionManager.releaseConnection(conn);
		return collisionEnergies;
	}

	public static Collection<LIMSSampleType>getAvailableSampleTypes() throws SQLException{

		Collection<LIMSSampleType>sampleTypes = new ArrayList<LIMSSampleType>();
		Connection conn = ConnectionManager.getConnection();
		String query =
			"SELECT DISTINCT Y.SAMPLE_TYPE_ID, Y.DESCRIPTION " +
			"FROM SAMPLE S, STOCK_SAMPLE T, SAMPLE_TYPE Y " +
			"WHERE S.STOCK_SAMPLE_ID = T.STOCK_SAMPLE_ID " +
			"AND T.SAMPLE_TYPE_ID = Y.SAMPLE_TYPE_ID " +
			"ORDER BY 2,1 ";
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				LIMSSampleType st = new LIMSSampleType(
						rs.getString("SAMPLE_TYPE_ID"),
						rs.getString("DESCRIPTION"));
				sampleTypes.add(st);
			}
			rs.close();
		}
		ConnectionManager.releaseConnection(conn);
		return sampleTypes;
	}
	
	public static Collection<LIMSBioSpecies> getAvailableSpecies() throws SQLException{

		Collection<LIMSBioSpecies> availableSpecies = new TreeSet<LIMSBioSpecies>();
		Connection conn = ConnectionManager.getConnection();
		String query = "SELECT DISTINCT TAX_ID FROM STOCK_SAMPLE";
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				LIMSBioSpecies lib = new LIMSBioSpecies(rs.getInt("TAX_ID"));
				availableSpecies.add(lib);
			}
			rs.close();
		}
		if(!availableSpecies.isEmpty()) {

			query =
				"SELECT NAME_TXT, NAME_CLASS FROM TAXONOMY_NAME WHERE TAX_ID = ?";
			try(PreparedStatement ps = conn.prepareStatement(query)){
				for(LIMSBioSpecies s : availableSpecies) {
					ps.setInt(1, s.getTaxonomyId());
					ResultSet rs = ps.executeQuery();
					while (rs.next())
						s.addName(rs.getString("NAME_TXT"), rs.getString("NAME_CLASS"));
					
					rs.close();
				}
			}
		}
		ConnectionManager.releaseConnection(conn);
		return availableSpecies;
	}

	public static Collection<LIMSBioSpecies>lookupSpeciesInTaxonomyDatabase(String queryString) throws SQLException{

		Collection<LIMSBioSpecies>species = new ArrayList<LIMSBioSpecies>();
		Connection conn = ConnectionManager.getConnection();
		String query =
			"SELECT DISTINCT TAX_ID FROM TAXONOMY_NAME "
			+ "WHERE UPPER(NAME_TXT) LIKE ? ORDER BY 1 ";

		try(PreparedStatement ps = conn.prepareStatement(query)){
			String likeString = "%" + queryString.toUpperCase() + "%";
			ps.setString(1, likeString);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				LIMSBioSpecies lib = new LIMSBioSpecies(rs.getInt("TAX_ID"));
				species.add(lib);
			}
			rs.close();
		}
		if(!species.isEmpty()) {

			query =
				"SELECT NAME_TXT, NAME_CLASS FROM TAXONOMY_NAME WHERE TAX_ID = ?";
			try(PreparedStatement ps = conn.prepareStatement(query)){
				for(LIMSBioSpecies s : species) {
	
					ps.setInt(1, s.getTaxonomyId());
					ResultSet rs = ps.executeQuery();
					while (rs.next())
						s.addName(rs.getString("NAME_TXT"), rs.getString("NAME_CLASS"));
					
					rs.close();
				}
			}
		}
		ConnectionManager.releaseConnection(conn);
		return species;
	}

	public static Collection<LIMSSampleType> lookupSampleTypesInDatabase(String sampleTypeQuery) throws SQLException{

		Collection<LIMSSampleType>sampleTypes = new ArrayList<LIMSSampleType>();
		Connection conn = ConnectionManager.getConnection();
		String query =
			"SELECT SAMPLE_TYPE_ID, DESCRIPTION FROM SAMPLE_TYPE "
			+ "WHERE UPPER(DESCRIPTION) LIKE ? ORDER BY 1 ";

		try(PreparedStatement ps = conn.prepareStatement(query)){
			String likeString = "%" + sampleTypeQuery.toUpperCase() + "%";
			ps.setString(1, likeString);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				LIMSSampleType lib = new LIMSSampleType(
						rs.getString("SAMPLE_TYPE_ID"), rs.getString("DESCRIPTION"));
				sampleTypes.add(lib);
			}
			rs.close();
		}
		ConnectionManager.releaseConnection(conn);
		return sampleTypes;
	}

	public static void updateStockSample(StockSample stockSample) throws SQLException{

		Connection conn = ConnectionManager.getConnection();
		String query =
			"UPDATE STOCK_SAMPLE SET  " +
			"SAMPLE_NAME = ?, " +
			"DESCRIPTION = ?, " +
			"SAMPLE_TYPE_ID = ?, " +
			"EXTERNAL_ID = ?, " +
			"EXTERNAL_SOURCE = ?, " +
			"TAX_ID = ? " +
			"LIMS_EXPERIMENT_ID = ? " + 
			"WHERE STOCK_SAMPLE_ID = ? ";

		try(PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1, stockSample.getSampleName());
			ps.setString(2, stockSample.getSampleDescription());
			ps.setString(3, stockSample.getLimsSampleTypeId());
			ps.setString(4, stockSample.getExternalId());
			ps.setString(5, stockSample.getExternalSource());
			ps.setInt(6, stockSample.getTaxonomyId());
			if(stockSample.getLimsExperiment() != null)
				ps.setString(7, stockSample.getLimsExperiment().getId());
			else
				ps.setNull(7, java.sql.Types.NULL);
			
			ps.setString(8, stockSample.getSampleId());
			ps.executeUpdate();
		}
		ConnectionManager.releaseConnection(conn);
	}

	public static void addStockSample(StockSample stockSample) throws SQLException{

		Connection conn = ConnectionManager.getConnection();
		String stockSampleId = SQLUtils.getNextIdFromSequence(conn, 
				"ID_STOCK_SAMPLE_SEQ",
				DataPrefix.STOCK_SAMPLE,
				"0",
				5);
		stockSample.setSampleId(stockSampleId);
		String query =
			"INSERT INTO STOCK_SAMPLE (  " +
			"SAMPLE_NAME, " +
			"DESCRIPTION, " +
			"SAMPLE_TYPE_ID, " +
			"EXTERNAL_ID, " +
			"EXTERNAL_SOURCE, " +
			"TAX_ID, " +
			"STOCK_SAMPLE_ID, "
			+ "LIMS_EXPERIMENT_ID) VALUES (?, ?, ?, ?, ?, ?, ?, ?) ";

		try(PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1, stockSample.getSampleName());
			ps.setString(2, stockSample.getSampleDescription());
			ps.setString(3, stockSample.getLimsSampleTypeId());
			ps.setString(4, stockSample.getExternalId());
			ps.setString(5, stockSample.getExternalSource());
			ps.setInt(6, stockSample.getTaxonomyId());
			ps.setString(7, stockSample.getSampleId());
			if(stockSample.getLimsExperiment() != null)
				ps.setString(8, stockSample.getLimsExperiment().getId());
			else
				ps.setNull(8, java.sql.Types.NULL);		
		}
		ConnectionManager.releaseConnection(conn);
	}

	public static void deleteStockSample(StockSample toDelete) throws SQLException{

		Connection conn = ConnectionManager.getConnection();
		String query = "DELETE FROM STOCK_SAMPLE WHERE STOCK_SAMPLE_ID = ?";
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1, toDelete.getSampleId());
			ps.executeUpdate();
		}
		ConnectionManager.releaseConnection(conn);
	}

	public static Injection getInjectionById(String injectionId) throws SQLException{

		Connection conn = ConnectionManager.getConnection();
		Injection injection = getInjectionById(injectionId, conn);
		ConnectionManager.releaseConnection(conn);
		return injection;
	}

	public static Injection getInjectionById(String injectionId, Connection conn) throws SQLException{

		Injection injection = null;
		String query = 
				"SELECT DATA_FILE_NAME, PREP_ITEM_ID, INJECTION_TIMESTAMP, " +
				"ACQUISITION_METHOD_ID, INJECTION_VOLUME " +
				"FROM INJECTION WHERE INJECTION_ID = ?";
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1, injectionId);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {	
				injection = new Injection(
						injectionId,
						rs.getString("DATA_FILE_NAME"),
						new Date(rs.getDate("INJECTION_TIMESTAMP").getTime()),
						rs.getString("PREP_ITEM_ID"),
						rs.getString("ACQUISITION_METHOD_ID"),
						rs.getDouble("INJECTION_VOLUME"));
			}
			rs.close();
		}
		return injection;
	}
	
	public static Collection<LIMSWorklistItem>checkForUploadedInjections(
			Collection<LIMSWorklistItem>newInjections) throws SQLException {
		
		Collection<LIMSWorklistItem>uploaded = new ArrayList<LIMSWorklistItem>();
		Connection conn = ConnectionManager.getConnection();
		String query = "SELECT INJECTION_ID FROM INJECTION "
				+ "WHERE DATA_FILE_NAME = ? AND INJECTION_TIMESTAMP = ?";
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ResultSet rs = null;
			for(LIMSWorklistItem item : newInjections) {
				
				ps.setString(1, item.getDataFile().getName());
				ps.setDate(2, new java.sql.Date(item.getTimeStamp().getTime()));
				rs = ps.executeQuery();
				while (rs.next())
					uploaded.add(item);

				rs.close();
			}
		}
		ConnectionManager.releaseConnection(conn);		
		return uploaded;		
	}
	
	public static String getTrackerDataAnalysisMethodIdByMD5(String md5string) throws SQLException{
		
		String methodId = null;
		Connection conn = ConnectionManager.getConnection();
		String query = "SELECT EXTRACTION_METHOD_ID "
				+ "FROM DATA_EXTRACTION_METHOD WHERE METHOD_MD5 = ?";
		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, md5string);
		ResultSet rs = ps.executeQuery();
		while(rs.next())
			methodId = rs.getString("EXTRACTION_METHOD_ID");
		
		rs.close();
		ps.close();
		ConnectionManager.releaseConnection(conn);
		return methodId;
	}

	public static DataExtractionMethod addNewTrackerDataExtractionMethod(
			MSMSExtractionParameterSet newTrackerMethod) throws SQLException, IOException {
		
		LIMSUser sysUser = MRC2ToolBoxCore.getIdTrackerUser();
		if(sysUser == null)
			return null;

		String methodMd5 = newTrackerMethod.getParameterSetHash();
		String methodString = newTrackerMethod.getXMLString();
		
		Connection conn = ConnectionManager.getConnection();
		String id = SQLUtils.getNextIdFromSequence(conn, 
				"ID_DATA_EXTRACTION_METHOD_SEQ",
				DataPrefix.DATA_EXTRACTION_METHOD,
				"0",
				4);	
		DataProcessingSoftware trackerSoft = 
				IDTDataCache.getSoftwareByName(MRC2ToolBoxCore.trackerSoftwareName);
		
		DataExtractionMethod newMethod  = new DataExtractionMethod(
				id,
				newTrackerMethod.getName(),
				newTrackerMethod.getDescription(),
				sysUser,
				new Date());
		newMethod.setSoftware(trackerSoft);
		newMethod.setMd5(methodMd5);
		
		String query  =
			"INSERT INTO DATA_EXTRACTION_METHOD (EXTRACTION_METHOD_ID, METHOD_NAME, " +
			"METHOD_DESCRIPTION, CREATED_BY, CREATED_ON, METHOD_CONTAINER, SOFTWARE_ID, METHOD_MD5) " +
			"VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
		
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1, id);
			ps.setString(2, newMethod.getName());
			ps.setString(3, newMethod.getDescription());
			ps.setString(4, sysUser.getId());
			ps.setDate(5, new java.sql.Date(new java.util.Date().getTime()));			
			InputStream is = DatabaseUtils.setBlobFromCompressedString(methodString, ps, 6);	
			if(trackerSoft != null)
				ps.setString(7, trackerSoft.getId());
			else
				ps.setNull(7, java.sql.Types.NULL);
			
			ps.setString(8, methodMd5);		
			ps.executeUpdate();
			
			if(is != null)
				is.close();
		}
		ConnectionManager.releaseConnection(conn);
		return newMethod;
	}
	
	public static Collection<Injection>getInjectionsByIds(Collection<String>injectionIds) throws SQLException {
		
		 Connection conn = ConnectionManager.getConnection();	 
		 Collection<Injection>injections = getInjectionsByIds(injectionIds, conn);
		 ConnectionManager.releaseConnection(conn);
		 return injections;
	}
	
	public static Collection<Injection>getInjectionsByIds(Collection<String>injectionIds, Connection conn) throws SQLException {
		
		Collection<Injection>injections = new HashSet<Injection>();
		String query = 
				"SELECT DATA_FILE_NAME, PREP_ITEM_ID, INJECTION_TIMESTAMP, " +
				"ACQUISITION_METHOD_ID, INJECTION_VOLUME " +
				"FROM INJECTION WHERE INJECTION_ID = ?";
		try(PreparedStatement ps = conn.prepareStatement(query)){
			for(String injectionId : injectionIds) {			
				ps.setString(1, injectionId);
				ResultSet rs = ps.executeQuery();
				while(rs.next()) {
					Injection inj = new Injection(
							injectionId,
							rs.getString("DATA_FILE_NAME"),
							new Date(rs.getDate("INJECTION_TIMESTAMP").getTime()),
							rs.getString("PREP_ITEM_ID"),
							rs.getString("ACQUISITION_METHOD_ID"),
							rs.getDouble("INJECTION_VOLUME"));
					injections.add(inj);
				}
				rs.close();			
			}
		}	
		return injections;
	}
	
	public static Collection<MSMSExtractionParameterSet>getMSMSExtractionParameters() 
			throws SQLException, IOException {
		
		 Connection conn = ConnectionManager.getConnection();	 
		 Collection<MSMSExtractionParameterSet>paramSets = getMSMSExtractionParameters(conn);
		 ConnectionManager.releaseConnection(conn);
		 return paramSets;
	}
	
	public static Collection<MSMSExtractionParameterSet>getMSMSExtractionParameters(Connection conn) 
			throws SQLException, IOException{
		
		Collection<MSMSExtractionParameterSet>paramSets = 
				new ArrayList<MSMSExtractionParameterSet>();
		DataProcessingSoftware trackerSoft = 
				IDTDataCache.getSoftwareByName(MRC2ToolBoxCore.trackerSoftwareName);
		if(trackerSoft == null)
			return paramSets;
			
		String query = 
				"SELECT EXTRACTION_METHOD_ID, METHOD_CONTAINER "
				+ "FROM DATA_EXTRACTION_METHOD WHERE SOFTWARE_ID = ?";
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1, trackerSoft.getId());
			ResultSet rs = ps.executeQuery();
			while(rs.next()) {

				String xmlString = 
						DatabaseUtils.decompressStringFromBlob(rs.getBinaryStream("METHOD_CONTAINER"));
				if(xmlString != null && !xmlString.isEmpty()) {
					MSMSExtractionParameterSet parSet = extractMSMSExtractionParameterSetfromXML(xmlString);
		        	if(parSet != null) {
		        		parSet.setId(rs.getString("EXTRACTION_METHOD_ID"));
		        		paramSets.add(parSet);
		        	}
				}
			}	
			rs.close();
		}			
		return paramSets;
	}
	
	private static MSMSExtractionParameterSet extractMSMSExtractionParameterSetfromXML(String xmlString) {
		
		if(xmlString == null  || xmlString.isEmpty())
			return null;
		
		Document doc = XmlUtils.readXmlString(xmlString);
		Element paramElement = doc.getRootElement();
		if(paramElement == null)
			return null;
		else
			return new MSMSExtractionParameterSet(paramElement);
	}

	public static Map<LIMSExperiment, Collection<StockSample>> getExperimentStockSampleMap() throws SQLException {
				
		 Connection conn = ConnectionManager.getConnection();	 
		 Map<LIMSExperiment, Collection<StockSample>> experimentStockSampleMap = 
				 getExperimentStockSampleMap(conn);
		 ConnectionManager.releaseConnection(conn);			 
		 return experimentStockSampleMap;
	}
	
	public static Map<LIMSExperiment, Collection<StockSample>> getExperimentStockSampleMap(Connection conn) throws SQLException {
		
		Map<LIMSExperiment, Collection<StockSample>> experimentStockSampleMap = 
				new TreeMap<LIMSExperiment, Collection<StockSample>>();
		String query = "SELECT DISTINCT EXPERIMENT_ID, STOCK_SAMPLE_ID FROM SAMPLE ORDER BY 1,2";
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ResultSet rs = ps.executeQuery();
			while(rs.next()) {
				
				LIMSExperiment e = IDTDataCache.getExperimentById(rs.getString("EXPERIMENT_ID"));
				if(e != null) {
					experimentStockSampleMap.computeIfAbsent(e,  v -> new TreeSet<StockSample>());				
					StockSample s = IDTDataCache.getStockSampleById(rs.getString("STOCK_SAMPLE_ID"));
					if(s != null)
						experimentStockSampleMap.get(e).add(s);
				}			
			}
			rs.close();
		}
		return experimentStockSampleMap;		
	}

	public static Map<LIMSExperiment, LIMSInstrument> getExperimentInstrumentMap() throws SQLException {

		 Connection conn = ConnectionManager.getConnection();	 
		 Map<LIMSExperiment, LIMSInstrument> experimentInstrumentMap = 
				 getExperimentInstrumentMap(conn);
		 ConnectionManager.releaseConnection(conn);			 
		 return experimentInstrumentMap;
	}
	
	public static Map<LIMSExperiment, LIMSInstrument> getExperimentInstrumentMap(Connection conn) throws SQLException {

		Map<LIMSExperiment, LIMSInstrument> experimentInstrumentMap = 
				new TreeMap<LIMSExperiment, LIMSInstrument>();
		String query = "SELECT DISTINCT EXPERIMENT_ID, INSTRUMENT_ID "
				+ "FROM EXPERIMENT_INSTRUMENT_MAP ORDER BY 1,2";
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ResultSet rs = ps.executeQuery();
			while(rs.next()) {			
				LIMSExperiment e = IDTDataCache.getExperimentById(rs.getString("EXPERIMENT_ID"));
				LIMSInstrument i = IDTDataCache.getInstrumentById(rs.getString("INSTRUMENT_ID"));
				if(e != null && i != null) 
					experimentInstrumentMap.put(e, i);			
			}
			rs.close();
		}
		return experimentInstrumentMap;
	}
	
	public static Map<LIMSExperiment, Collection<Polarity>> getExperimentPolarityMap() throws SQLException {
		
		 Connection conn = ConnectionManager.getConnection();	 
		 Map<LIMSExperiment, Collection<Polarity>> experimentPolarityMap = 
				 getExperimentPolarityMap(conn);
		 ConnectionManager.releaseConnection(conn);			 
		 return experimentPolarityMap;
	}
	
	public static Map<LIMSExperiment, Collection<Polarity>> getExperimentPolarityMap(Connection conn) throws SQLException {
		
		Map<LIMSExperiment, Collection<Polarity>> experimentPolarityMap = 
				new TreeMap<LIMSExperiment, Collection<Polarity>>();
		String query = 
				"SELECT DISTINCT S.EXPERIMENT_ID, M.POLARITY " +
				"FROM SAMPLE S, " +
				"PREPARED_SAMPLE P, " +
				"INJECTION I, " +
				"DATA_ACQUISITION_METHOD M " +
				"WHERE S.SAMPLE_ID = P.SAMPLE_ID " +
				"AND P.PREP_ITEM_ID = I.PREP_ITEM_ID " +
				"AND I.ACQUISITION_METHOD_ID = M.ACQ_METHOD_ID " +
				"ORDER BY 1,2 ";
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ResultSet rs = ps.executeQuery();
			while(rs.next()) {			
				LIMSExperiment e = IDTDataCache.getExperimentById(rs.getString("EXPERIMENT_ID"));
				if(e != null) {
					experimentPolarityMap.computeIfAbsent(e,  v -> new TreeSet<Polarity>());				
					Polarity p = Polarity.getPolarityByCode(rs.getString("POLARITY"));
					if(p != null)
						experimentPolarityMap.get(e).add(p);
				}			
			}
			rs.close();
		}
		return experimentPolarityMap;		
	}
}












































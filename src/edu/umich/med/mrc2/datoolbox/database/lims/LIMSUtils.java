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

package edu.umich.med.mrc2.datoolbox.database.lims;

import java.io.File;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import edu.umich.med.mrc2.datoolbox.data.Assay;
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesign;
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignFactor;
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignLevel;
import edu.umich.med.mrc2.datoolbox.data.ExperimentalSample;
import edu.umich.med.mrc2.datoolbox.data.ExperimentalSubject;
import edu.umich.med.mrc2.datoolbox.data.InstrumentPlatform;
import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.data.enums.StandardFactors;
import edu.umich.med.mrc2.datoolbox.data.lims.BioSpecies;
import edu.umich.med.mrc2.datoolbox.data.lims.IdTrackerOrganization;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSAcquisitionDetails;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSClient;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSExperiment;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSOrganization;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSProject;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSProtocol;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSUser;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.database.DatabaseUtils;
import edu.umich.med.mrc2.datoolbox.database.idt.UserUtils;
import edu.umich.med.mrc2.datoolbox.main.ReferenceSamplesManager;
import edu.umich.med.mrc2.datoolbox.utils.SQLUtils;

public class LIMSUtils {
	
	private static final Logger logger = LogManager.getLogger(LIMSUtils.class);

	private LIMSUtils() {
		/* This utility class should not be instantiated */
	}

	public static final String MRC2_CLIENT_ID = "CL0203";
	public static final String MRC2_ADMIN_ID = "U00029";
	public static final String MRC2_IDT_ORGANIZATION_ID = "OR0001";

	public static Collection<LIMSProject> getLimsProjectList() throws SQLException{
		
		Connection conn = ConnectionManager.getConnection();
		Collection<LIMSProject> projects =getLimsProjectList(conn);
		ConnectionManager.releaseConnection(conn);
		return projects;
	}
	
	public static Collection<LIMSProject> getLimsProjectList(Connection conn) throws SQLException {		
		
		LIMSDataCache.refreshLimsClientList();
		Collection<LIMSProject> projects = new TreeSet<>();
		String query  =
			"SELECT PROJECT_ID, PROJECT_NAME, CLIENT_ID, PROJECT_DESCRIPTION, CONTACTPERSON_ID, "
			+ "STATUS_ID, START_DATE, NOTES FROM LIMS_PROJECT "
			+ "ORDER BY PROJECT_ID";
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
				if(rs.getDate("START_DATE") != null)
					project.setStartDate(new Date(rs.getDate("START_DATE").getTime()));	
				
				projects.add(project);
			}
			rs.close();
		}
		return projects;
	}
	
	public static void insertMetLimsProjec(LIMSProject project, Connection conn) throws SQLException {
		
		if(project.getStartDate() == null)
			project.setStartDate(new Date());
		
		String sql  =
			"INSERT INTO LIMS_PROJECT (PROJECT_ID, PROJECT_NAME, PROJECT_DESCRIPTION, "
			+ "CLIENT_ID, START_DATE, NOTES) VALUES (?, ?, ?, ?, ?, ?)";			
		try(PreparedStatement ps = conn.prepareStatement(sql)){
			ps.setString(1, project.getId());
			ps.setString(2, project.getName());
			ps.setString(3, project.getDescription());
			ps.setString(4, project.getClient().getId());
			ps.setDate(5, new java.sql.Date(project.getStartDate().getTime()));
			ps.setString(6, project.getNotes());
			ps.executeUpdate();
		}
	}
	

	public static Collection<LIMSExperiment> getExperimentList(String startFromId) throws SQLException{

		Connection conn = ConnectionManager.getConnection();
		Collection<LIMSExperiment> experiments = getExperimentList(startFromId, conn);
		ConnectionManager.releaseConnection(conn);
		return experiments;
	}
	
	public static Collection<LIMSExperiment> getExperimentList(String startFromId, Connection conn) throws SQLException{

		Collection<LIMSExperiment> experiments = new TreeSet<LIMSExperiment>();
		String query  =
			"SELECT E.EXPERIMENT_ID, E.SERVICE_REQUEST_ID, E.EXPERIMENT_NAME, " +
			"E.PROJECT_ID, E.EXPERIMENT_DESCRIPTION, E.DATE_INITIATED, E.NOTES, O.NIH_GRANT_NUMBER " +
			"FROM LIMS_EXPERIMENT E LEFT OUTER JOIN LIMS_SHORTCODE O ON E.EXPERIMENT_ID = O.EXPERIMENT_ID " + 
			"WHERE E.EXPERIMENT_ID >= ? ORDER BY E.EXPERIMENT_ID DESC";
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1,  startFromId);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				LIMSExperiment experiment = new LIMSExperiment(
						rs.getString("EXPERIMENT_ID"),
						rs.getString("EXPERIMENT_NAME"),
						rs.getString("EXPERIMENT_DESCRIPTION"),
						rs.getString("NOTES"),
						rs.getString("SERVICE_REQUEST_ID"),
						rs.getTimestamp("DATE_INITIATED"));
				experiment.setNihGrant(rs.getString("NIH_GRANT_NUMBER"));
				LIMSProject project = 
						LIMSDataCache.getProjectById(rs.getString("PROJECT_ID"));
				experiment.setProject(project);
				experiments.add(experiment);
			}
			rs.close();
		}
		return experiments;
	}
	
	public static void insertMetlimsExperiment(LIMSExperiment experiment, Connection conn) throws SQLException {
		
		String name = null;
		String sql = "SELECT EXPERIMENT_NAME FROM LIMS_EXPERIMENT WHERE EXPERIMENT_ID = ?";
		try(PreparedStatement ps = conn.prepareStatement(sql)){
			ps.setString(1,  experiment.getId());
			ResultSet rs = ps.executeQuery();			
			while(rs.next())
				name = rs.getString("EXPERIMENT_NAME");
			
			rs.close();
		}
		if(name != null)
			return;
				
		sql = "INSERT INTO LIMS_EXPERIMENT (EXPERIMENT_ID, EXPERIMENT_NAME, PROJECT_ID,  " +
				"EXPERIMENT_DESCRIPTION, DATE_INITIATED, NOTES, " +
				"OWNER_ID, SERVICE_REQUEST_ID, CHEAR, MOTRPAC) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ";
				
		try(PreparedStatement ps = conn.prepareStatement(sql)){
			ps.setString(1, experiment.getId());
			ps.setString(2, experiment.getName());
			ps.setString(3, experiment.getProject().getId());
			ps.setString(4, experiment.getDescription());
			ps.setDate(5, new java.sql.Date(experiment.getStartDate().getTime()));
			ps.setString(6, experiment.getNotes());
			ps.setString(7, experiment.getCreator().getId());
			ps.setString(8, experiment.getServiceRequestId());
			ps.setBoolean(9, experiment.isChear());
			ps.setBoolean(10, experiment.isMotrpac());
			ps.executeUpdate();
		}
	}

	public static Collection<ExperimentalSubject> 
		getSubjectListForExperiment(String experimentId) throws SQLException{

		Collection<ExperimentalSubject> subjectss = new TreeSet<ExperimentalSubject>();
		Connection conn = ConnectionManager.getConnection();
		String query  =
			"SELECT DISTINCT T.SUBJECT_ID, T.USER_SUBJECT_ID, T.SUBJECT_TAX_ID, N.NAME_TXT " +
			"FROM LIMS_EXPERIMENTAL_SUBJECT T, TAXONOMY_NAME N, LIMS_SAMPLE S " +
			"WHERE S.EXPERIMENT_ID = ? " +
			"AND S.SUBJECT_ID = T.SUBJECT_ID " +
			"AND T.SUBJECT_TAX_ID = N.TAX_ID " +
			"AND N.NAME_CLASS = 'scientific name' " +
			"ORDER BY 1";
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1, experimentId);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				ExperimentalSubject subject = new ExperimentalSubject(
						rs.getString("SUBJECT_ID"),
						rs.getString("SUBJECT_ID"),
						rs.getString("USER_SUBJECT_ID"),
						rs.getInt("SUBJECT_TAX_ID"),
						rs.getString("NAME_TXT"));	
				subjectss.add(subject);
			}
			rs.close();
		}
		ConnectionManager.releaseConnection(conn);
		return subjectss;
	}

	public static ExperimentDesign getDesignForExperiment(String experimentId) throws SQLException{

		ExperimentDesign design = new ExperimentDesign();
		Connection conn = ConnectionManager.getConnection();

		//	Get factors
		String query  =
			"SELECT T.FACTOR_ID, T.FACTOR_DESCRIPTION, T.FACTOR_NAME " +
			"FROM LIMS_EXPERIMENTAL_FACTOR T " +
			"WHERE T.EXPERIMENT_ID = ? ORDER BY 1";

		try(PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1, experimentId);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
	
				String fName = rs.getString("FACTOR_DESCRIPTION");
				if(fName == null)
					fName = rs.getString("FACTOR_NAME");
	
				ExperimentDesignFactor factor = new ExperimentDesignFactor(
						fName,
						rs.getString("FACTOR_ID"));
	
				factor.setSuppressEvents(true);
				design.addFactor(factor,false);
			}
			rs.close();
		}
		//	Get levels
		query =
			"SELECT F.FACTOR_ID, L.LEVEL_ID, L.LEVEL_DESCRIPTION, L.LEVEL_NAME " +
			"FROM LIMS_EXPERIMENTAL_FACTOR F, " +
			"LIMS_EXPERIMENTAL_FACTOR_LEVEL L " +
			"WHERE F.EXPERIMENT_ID = ? " +
			"AND L.FACTOR_ID = F.FACTOR_ID " +
			"ORDER BY 1,2";

		try(PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1, experimentId);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
	
				String lName = rs.getString("LEVEL_DESCRIPTION");
				if(lName == null)
					lName = rs.getString("LEVEL_NAME");
	
				ExperimentDesignLevel level = new ExperimentDesignLevel(
						lName,
						rs.getString("LEVEL_ID"));
	
				ExperimentDesignFactor parentFactor = design.getFactorById(rs.getString("FACTOR_ID"));
				if(parentFactor != null)
					parentFactor.addLevel(level);
			}
			rs.close();
		}
		//	Get samples
		Map<String,String>sampleSubjectMap = new TreeMap<String,String>();
		Set<String>bioMaterialTypes = new TreeSet<String>();
		query =
			"SELECT DISTINCT S.SAMPLE_ID, S.SAMPLE_NAME, S.SUBJECT_ID, " +
			"T.DESCRIPTION AS SAMPLE_TYPE " +
			"FROM LIMS_SAMPLE S, SAMPLE_TYPE T " +
			"WHERE S.EXPERIMENT_ID = ? " +
			"AND S.SAMPLE_TYPE_ID = T.SAMPLE_TYPE_ID " +
			"ORDER BY 1";

		try(PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1, experimentId);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {	
				String sid = rs.getString("SAMPLE_ID");
				String subid = rs.getString("SUBJECT_ID");	
				ExperimentalSample sample = new ExperimentalSample(sid, rs.getString("SAMPLE_NAME"));
				sample.setLimsSampleType(rs.getString("SAMPLE_TYPE"));
				bioMaterialTypes.add(rs.getString("SAMPLE_TYPE"));
				design.addSample(sample,false);
				sampleSubjectMap.put(sid, subid);
			}
			rs.close();
		}
		//	Add regular design factors
		query =
			"SELECT S.SAMPLE_ID, L.LEVEL_ID  " +
			"FROM LIMS_EXPERIMENT_DESIGN S,  " +
			"LIMS_EXPERIMENTAL_FACTOR_LEVEL L,  " +
			"LIMS_EXPERIMENTAL_FACTOR F  " +
			"WHERE F.EXPERIMENT_ID = ? " +
			"AND F.FACTOR_ID = L.FACTOR_ID " +
			"AND S.LEVEL_ID = L.LEVEL_ID " +
			"ORDER BY 1,2 ";

		try(PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1, experimentId);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {	
				ExperimentalSample eSample = design.getSampleById(rs.getString("SAMPLE_ID"));
				ExperimentDesignLevel eLevel = design.getLevelById(rs.getString("LEVEL_ID"));
				if(eSample != null && eLevel != null)
					eSample.addDesignLevel(eLevel);
			}
			rs.close();
		}
		// Add sample/control type
		//	TODO - all LIMS samples are regular samples now. If this changes this code has to be modified
		design.addFactor(ReferenceSamplesManager.getSampleControlTypeFactor(),false);
		design.getSamples().forEach(s -> s.addDesignLevel(ReferenceSamplesManager.sampleLevel));

		//	Add biological material type as factor
		if(!bioMaterialTypes.isEmpty()) {

			ExperimentDesignFactor bioMaterialFactor = new ExperimentDesignFactor(StandardFactors.BIOLOGICAL_MATERIAL.getName());
			bioMaterialTypes.forEach(t -> bioMaterialFactor.addLevel(new ExperimentDesignLevel(t)));
			design.addFactor(bioMaterialFactor,false);
			design.getSamples().forEach(s -> s.addDesignLevel(design.getLevelByName(s.getLimsSampleType())));
		}
		//	Get subjects data
		Collection<ExperimentalSubject> subjects = new TreeSet<ExperimentalSubject>();
		Map<String,String>subjectSpeciesMap = new TreeMap<String,String>();
		Collection<String> species = new TreeSet<String>();
		query  =
			"SELECT DISTINCT T.SUBJECT_ID, T.USER_SUBJECT_ID, T.SUBJECT_TAX_ID, N.NAME_TXT " +
			"FROM LIMS_EXPERIMENTAL_SUBJECT T, TAXONOMY_NAME N, LIMS_SAMPLE S " +
			"WHERE S.EXPERIMENT_ID = ? " +
			"AND S.SUBJECT_ID = T.SUBJECT_ID " +
			"AND T.SUBJECT_TAX_ID = N.TAX_ID " +
			"AND N.NAME_CLASS = 'scientific name' " +
			"ORDER BY 1";

		try(PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1, experimentId);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				ExperimentalSubject subject = new ExperimentalSubject(
						rs.getString("SUBJECT_ID"),
						rs.getString("SUBJECT_ID"),
						rs.getString("USER_SUBJECT_ID"),
						rs.getInt("SUBJECT_TAX_ID"),
						rs.getString("NAME_TXT"));
	
				subjects.add(subject);
				species.add(rs.getString("NAME_TXT"));
				subjectSpeciesMap.put(rs.getString("SUBJECT_ID"), rs.getString("NAME_TXT"));
			}
			rs.close();
		}
		//	Add subject IDs as factor if more than 1 subject defined
		if(subjects.size() > 1) {

			ExperimentDesignFactor subjectFactor = 
					new ExperimentDesignFactor(StandardFactors.SUBJECT.getName());
			design.addFactor(subjectFactor,false);
			subjects.forEach(s -> subjectFactor.addLevel(new ExperimentDesignLevel(s.getSubjectId())));
			design.getSamples().forEach(s -> s.addDesignLevel(design.getLevelByName(sampleSubjectMap.get(s.getId()))));
		}
		//	Add species as factor if more than one tax id present
		if(species.size() > 1) {

			ExperimentDesignFactor speciesFactor = new ExperimentDesignFactor(StandardFactors.SPECIES.getName());
			design.addFactor(speciesFactor,false);
			species.forEach(s -> speciesFactor.addLevel(new ExperimentDesignLevel(s)));
			for (Map.Entry<String, String> entry : sampleSubjectMap.entrySet()) {

				ExperimentalSample eSample = design.getSampleById(entry.getKey());
				ExperimentDesignLevel specLevel = design.getLevelByName(subjectSpeciesMap.get(entry.getValue()));
				if(eSample != null && specLevel != null)
					eSample.addDesignLevel(specLevel);
			}
		}
		ConnectionManager.releaseConnection(conn);
		design.getFactors().forEach(f -> f.setSuppressEvents(false));
		return design;
	}

	public static Collection<Assay>getAssaysForExperiment(String experimentId) throws SQLException{

		Collection<Assay> assays = new TreeSet<Assay>();
		Connection conn = ConnectionManager.getConnection();
		String sql  =
				"SELECT DISTINCT A.ASSAY_ID, A.ASSAY_NAME " +
				"FROM LIMS_ASSAY A, LIMS_SAMPLE_ASSAY_MAP M, LIMS_SAMPLE S " +
				"WHERE S.EXPERIMENT_ID = ? " +
				"AND S.SAMPLE_ID = M.SAMPLE_ID " +
				"AND M.ASSAY_ID = A.ASSAY_ID " +
				"ORDER BY 1,2";
		
		try(PreparedStatement ps = conn.prepareStatement(sql)){
			ps.setString(1, experimentId);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				Assay assay = new Assay(rs.getString("ASSAY_ID"), rs.getString("ASSAY_NAME"));
				assays.add(assay);
			}
			rs.close();
		}
		ConnectionManager.releaseConnection(conn);
		return assays;
	}

	public static LIMSProtocol getAssaySop(Assay assay, String sopCAtegory) throws SQLException{

		//	TODO This is a temporary solution, there should be a clear connection between protocol and experiment
		LIMSProtocol sop = null;
		Connection conn = ConnectionManager.getConnection();
		String query  =
			"SELECT P.SOP_ID, P.SOP_VERSION, P.SOP_NAME, P.FILE_EXTENSION " +
			"FROM SOP_PROTOCOL P, SOP_ASSAY_MAP M " +
			"WHERE M.ASSAY_ID = ? " +
			"AND M.SOP_ID = P.SOP_ID " +
			"AND P.SOP_CATEGORY = ? " +
			"ORDER BY P.SOP_VERSION DESC, P.DATE_CRERATED DESC ";

		try(PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1, assay.getId().substring(0, 4)); //	TODO - this is a temporary fix to match LIMS assay IDs
			ps.setString(2, sopCAtegory);
			ResultSet rs = ps.executeQuery();
			while(rs.next()) {
				sop = new LIMSProtocol(
						rs.getString("SOP_ID"),
						null,
						rs.getString("SOP_NAME"),
						null,
						rs.getString("SOP_VERSION"),
						null,
						null);
			}
			rs.close();
		}
		ConnectionManager.releaseConnection(conn);
		return sop;
	}

	public static File getSopProtocolFile(LIMSProtocol protocol, File destination) throws SQLException{

		File sopFile = null;
		Connection conn = ConnectionManager.getConnection();
		String query = "SELECT FILE_EXTENSION, SOP_DOCUMENT FROM SOP_PROTOCOL WHERE SOP_ID = ?";
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1, protocol.getSopId());
			ResultSet rs = ps.executeQuery();			
			while(rs.next()) {
			   sopFile = Paths.get(destination.getAbsolutePath(),
					   protocol.getSopName().replaceAll("\\W+", " ") + "." +
					   rs.getString("FILE_EXTENSION")).toFile();	   
			   DatabaseUtils.writeBlobToFile(sopFile, rs.getBinaryStream("SOP_DOCUMENT"));
			}
			rs.close();
		}
		ConnectionManager.releaseConnection(conn);
		return sopFile;
	}

	public static LIMSAcquisitionDetails getAcquisitionDetails(String experimentId, Assay assay)  throws SQLException{

		LIMSAcquisitionDetails acqDetails = null;
		Connection conn = ConnectionManager.getConnection();
		String query  =
			"SELECT M.ACQ_METHOD_ID, T.POLARITY, M.METHOD_NAME, " +
			"M.IONIZATION_TYPE, M.MASS_ANALYZER, M.MS_TYPE, " +
			"C.COLUMN_NAME, C.MANUFACTURER, C.CHEMISTRY, " +
			"I.MODEL, I.MANUFACTURER AS INST_MANUF " +
			"FROM EXPERIMENT_ASSAY_ACQMETHOD T, " +
			"INSTRUMENT I, " +
			"DATA_ACQUISITION_METHOD M LEFT JOIN " +
			"CHROMATOGRAPHIC_COLUMN C ON " +
			"M.COLUMN_TYPE_ID = C.COLUMN_ID " +
			"WHERE T.EXPERIMENT_ID = ? " +
			"AND T.ASSAY_ID = ? " +
			"AND M.ACQ_METHOD_ID = T.METHOD_ID " +
			"AND T.INSTRUMENT_ID = I.INSTRUMENT_ID ";

		try(PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1, experimentId);
			ps.setString(2, assay.getId().substring(0, 4)); //	TODO - this is a temporary fix to match LIMS assay IDs
			ResultSet rs = ps.executeQuery();
			while(rs.next()) {
				acqDetails = new LIMSAcquisitionDetails(
						Objects.toString(rs.getString("POLARITY"), ""),
						Objects.toString(rs.getString("METHOD_NAME"), ""),
						Objects.toString(rs.getString("IONIZATION_TYPE"), ""),
						Objects.toString(rs.getString("MASS_ANALYZER"),""),
						Objects.toString(rs.getString("MS_TYPE"), ""),
						Objects.toString(rs.getString("COLUMN_NAME"), "") +
							" ("+ Objects.toString(rs.getString("MANUFACTURER"), "")+")",
						Objects.toString(rs.getString("CHEMISTRY"), ""),
						Objects.toString(rs.getString("MODEL"), "") +
							" ("+ Objects.toString(rs.getString("INST_MANUF"), "")+")");
			}
			rs.close();
		}
		ConnectionManager.releaseConnection(conn);
		return acqDetails;
	}

	public static Collection<BioSpecies>getSpeciesListBySubString(
			String subString, Connection conn) throws SQLException{

		Map<Integer,BioSpecies>taxMap = new TreeMap<Integer,BioSpecies>();
		String query =
			"SELECT TAX_ID, NAME_CLASS, NAME_TXT FROM TAXONOMY_NAME " +
			"WHERE UPPER(NAME_TXT) LIKE ? " +
			"AND NAME_CLASS IN ('scientific name', 'common name', 'synonym')" +
			"ORDER BY 1,2,3 ";
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1, "%" + subString.toUpperCase() + "%");
			ResultSet rs = ps.executeQuery();		
			while(rs.next()) {
				int taxId = rs.getInt("TAX_ID");
				taxMap.computeIfAbsent(taxId, v -> new BioSpecies(taxId));
				taxMap.get(taxId).addName(rs.getString("NAME_TXT"), rs.getString("NAME_CLASS"));
			}
			rs.close();
		}
		return taxMap.values();
	}

	public static Collection<String>getConcentrationUnits() throws SQLException {

		Collection<String> units = new TreeSet<String>();
		Connection conn = ConnectionManager.getConnection();
		String query = "SELECT UNITS FROM MEASUREMENT_UNIT WHERE TYPE = 'concentration' AND PRIORITY = 1";
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ResultSet rs = ps.executeQuery();
			while (rs.next())
				units.add(rs.getString("UNITS"));
	
			rs.close();
		}
		ConnectionManager.releaseConnection(conn);
		return units;
	}
	
	public static Collection<Assay> getLIMSAssayList() throws SQLException {

		Collection<Assay> assays = new TreeSet<Assay>();
		Connection conn = ConnectionManager.getConnection();
		String query = "SELECT ASSAY_ID, ASSAY_NAME FROM LIMS_ASSAY";
		try(PreparedStatement ps = conn.prepareStatement(query)){			
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				Assay assay = new Assay(
						rs.getString("ASSAY_ID"),
						rs.getString("ASSAY_NAME"));
				assays.add(assay);
			}
			rs.close();
		}
		ConnectionManager.releaseConnection(conn);
		return assays;
	}
	
	public static Collection<IdTrackerOrganization>getOrganizationList() throws SQLException{
		
		Connection conn = ConnectionManager.getConnection();
		Collection<IdTrackerOrganization> organizations = getOrganizationList(conn);
		ConnectionManager.releaseConnection(conn);
		return organizations;
	}

	public static Collection<IdTrackerOrganization>getOrganizationList(Connection conn) throws SQLException{
	
		Collection<IdTrackerOrganization> organizations = new TreeSet<IdTrackerOrganization>();
		Collection<LIMSUser> userList = UserUtils.getCompleteUserList(conn);
		String query  =
			"SELECT ORGANIZATION_ID, NAME, ADDRESS, DEPARTMENT_OR_DIVISION, LABORATORY,  " +
			"PRINCIPAL_INVESTIGATOR_ID, CONTACT_PERSON_ID, MAILING_ADDRESS, CLIENT_ID_TMP " +
			"FROM ORGANIZATION ORDER BY ORGANIZATION_ID";
		try(PreparedStatement ps = conn.prepareStatement(query)){	
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {		
				IdTrackerOrganization organization = new IdTrackerOrganization(
						rs.getString("ORGANIZATION_ID"),
						rs.getString("NAME"),
						rs.getString("ADDRESS"),
						rs.getString("DEPARTMENT_OR_DIVISION"),
						rs.getString("LABORATORY"),
						rs.getString("MAILING_ADDRESS"));
				organization.setMetlimsClientId(rs.getString("CLIENT_ID_TMP"));
				
				String piId = rs.getString("PRINCIPAL_INVESTIGATOR_ID");
				LIMSUser pi = userList.stream().
						filter(e -> e.getId().equals(piId)).findFirst().orElse(null);
				if (pi == null)
					logger.debug(String.format("Can not find user with ID  %s", piId));
				
				organization.setPrincipalInvestigator(pi);
				
				String cpId = rs.getString("CONTACT_PERSON_ID");
				LIMSUser cp = userList.stream().
						filter(e -> e.getId().equals(cpId)).findFirst().orElse(null);
				if (cp == null)
					logger.debug(String.format("Can not find user with ID  %s", cpId));
							
				organization.setContactPerson(cp);
				organizations.add(organization);
			}
			rs.close();
		}
		return organizations;
	}
	
	public static Collection<LIMSOrganization>getLimsOrganizationList() throws SQLException{

		Connection conn = ConnectionManager.getConnection();
		Collection<LIMSOrganization> organizations = getLimsOrganizationList(conn);
		ConnectionManager.releaseConnection(conn);
		return organizations;
	}
	
	public static Collection<LIMSOrganization>getLimsOrganizationList(Connection conn) throws SQLException{

		Collection<LIMSOrganization> organizations = new TreeSet<LIMSOrganization>();
		String sql  =
			"SELECT ORGANIZATION_ID, NAME, ADDRESS FROM LIMS_ORGANIZATION ORDER BY ORGANIZATION_ID";
		try(PreparedStatement ps = conn.prepareStatement(sql)){
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {	
				LIMSOrganization organization = new LIMSOrganization(
						rs.getString("ORGANIZATION_ID"),
						rs.getString("NAME"),
						rs.getString("ADDRESS"));	
				organizations.add(organization);
			}
			rs.close();
		}
		return organizations;
	}
	
	public static String addNewOrganization(IdTrackerOrganization organization) throws SQLException{
		
		Connection conn = ConnectionManager.getConnection();
		String nextId = addNewOrganization(organization, conn);
		ConnectionManager.releaseConnection(conn);
		return nextId;
	}
	
	public static String addNewOrganization(IdTrackerOrganization organization, Connection conn) throws SQLException{

		String nextId = SQLUtils.getNextIdFromSequence(conn, 
				"ORGANIZATION_SEQ",
				DataPrefix.LIMS_ORGANIZATION,
				"0",
				4);
		String sql = 
				"INSERT INTO ORGANIZATION (ORGANIZATION_ID, NAME, ADDRESS, "
				+ "DEPARTMENT_OR_DIVISION, LABORATORY, PRINCIPAL_INVESTIGATOR_ID, "
				+ "CONTACT_PERSON_ID, MAILING_ADDRESS, CLIENT_ID_TMP) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
		try(PreparedStatement ps = conn.prepareStatement(sql)){
			ps.setString(1, nextId);
			ps.setString(2, organization.getName());
			ps.setString(3, organization.getAddress());
			ps.setString(4, organization.getDepartment());
			ps.setString(5, organization.getLaboratory());		
			ps.setString(6, organization.getPrincipalInvestigator().getId());
			ps.setString(7, organization.getContactPerson().getId());
			ps.setString(8, organization.getMailingAddress());	
			ps.setString(9, organization.getMetlimsClientId());
			ps.executeUpdate();
		}
		return nextId;	
	}

	public static void editOrganization(IdTrackerOrganization organization) throws SQLException{
		
		Connection conn = ConnectionManager.getConnection();
		editOrganization(organization, conn);
		ConnectionManager.releaseConnection(conn);
	}
	
	public static void editOrganization(IdTrackerOrganization organization, Connection conn) throws SQLException{

		String sql = 
				"UPDATE ORGANIZATION SET NAME = ?, ADDRESS = ?, "
				+ "DEPARTMENT_OR_DIVISION = ?, LABORATORY = ?, PRINCIPAL_INVESTIGATOR_ID = ?, "
				+ "CONTACT_PERSON_ID = ?, MAILING_ADDRESS = ?, CLIENT_ID_TMP = ? "
				+ "WHERE ORGANIZATION_ID = ?";
		try(PreparedStatement ps = conn.prepareStatement(sql)){		
			ps.setString(1, organization.getName());
			ps.setString(2, organization.getAddress());
			ps.setString(3, organization.getDepartment());
			ps.setString(4, organization.getLaboratory());		
			ps.setString(5, organization.getPrincipalInvestigator().getId());
			ps.setString(6, organization.getContactPerson().getId());
			ps.setString(7, organization.getMailingAddress());	
			ps.setString(8, organization.getMetlimsClientId());
			ps.setString(9, organization.getId());
			ps.executeUpdate();
		}
	}
	
	public static void deleteOrganization(IdTrackerOrganization organization) throws SQLException{
		
		Connection conn = ConnectionManager.getConnection();
		String sql = "DELETE FROM ORGANIZATION WHERE ORGANIZATION_ID = ?";
		try(PreparedStatement ps = conn.prepareStatement(sql)){
			ps.setString(1, organization.getId());
			ps.executeUpdate();
		}
		ConnectionManager.releaseConnection(conn);
	}
	
	public static Collection<InstrumentPlatform> getInstrumentPlatformList() throws SQLException{
			
			Connection conn = ConnectionManager.getConnection();
			Collection<InstrumentPlatform> platforms = getInstrumentPlatformList(conn);
			ConnectionManager.releaseConnection(conn);
			return platforms;
	}

	private static Collection<InstrumentPlatform> getInstrumentPlatformList(Connection conn) throws SQLException{
		
		Collection<InstrumentPlatform>platforms = new TreeSet<InstrumentPlatform>();
		String sql = "SELECT PLATFORM_ID, PLATFORM_DESCRIPTION FROM INSTRUMENT_PLATFORM ORDER BY 1";
		try(PreparedStatement ps = conn.prepareStatement(sql)){
			ResultSet rs = ps.executeQuery();
			while(rs.next()) {
				InstrumentPlatform platform = new InstrumentPlatform(
						rs.getString("PLATFORM_ID"), 
						rs.getString("PLATFORM_DESCRIPTION"));
				platforms.add(platform);
			}
			rs.close();
		}
		return platforms;
	}
	
	public static Collection<LIMSClient> getClientList() throws SQLException{
		
		Connection conn = ConnectionManager.getConnection();
		Collection<LIMSClient> clients = getClientList(conn);
		ConnectionManager.releaseConnection(conn);
		return clients;
	}
	
	public static Collection<LIMSClient> getClientList(Connection conn) throws SQLException{

		Collection<LIMSUser> userList = UserUtils.getCompleteUserList(conn);
		LIMSDataCache.refreshLimsOrganizationList();
		Collection<LIMSClient> clients = new TreeSet<LIMSClient>();
		String sql  =
			"SELECT CLIENT_ID, NAME, ADDRESS, DEPARTMENT_OR_DIVISION, LABORATORY, "
			+ "PRINCIPAL_INVESTIGATOR_ID, CONTACT_PERSON_ID, ORGANIZATION_ID "
			+ "FROM LIMS_CLIENT ORDER BY CLIENT_ID";
		try(PreparedStatement ps = conn.prepareStatement(sql)){
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
	
				LIMSClient client = new LIMSClient(
						rs.getString("CLIENT_ID"),
						rs.getString("DEPARTMENT_OR_DIVISION"),
						rs.getString("LABORATORY"),
						rs.getString("ADDRESS"));
									
				client.setOrganization(LIMSDataCache.getLIMSOrganizationById(rs.getString("ORGANIZATION_ID")));
				String piId = rs.getString("PRINCIPAL_INVESTIGATOR_ID");
				LIMSUser pi = userList.stream().
						filter(e -> e.getId().equals(piId)).findFirst().orElse(null);
				if (pi == null)
					logger.error(String.format("Can not find PI for user ID %s", 
							rs.getString("PRINCIPAL_INVESTIGATOR_ID")));
				
				client.setPrincipalInvestigator(pi);
				
				String cpId = rs.getString("CONTACT_PERSON_ID");
				LIMSUser cp = userList.stream().
						filter(e -> e.getId().equals(cpId)).findFirst().orElse(null);
				if (cp == null)
					logger.error(String.format("Can not find contact person for user ID %s", 
						rs.getString("CONTACT_PERSON_ID")));
							
				client.setContactPerson(cp);
				clients.add(client);
			}
			rs.close();
		}
		return clients;
	}
	
	public static void deleteMRC2LIMSExperiment(String experimentId) throws SQLException {

		Collection<String> subjectIds = new TreeSet<String>();
		Collection<String> subjectIdsToKeep = new TreeSet<String>();
		Connection conn = ConnectionManager.getConnection();
		
		//	Get subject IDs for experiment that do not overlap with other experiments	
		String query = "SELECT DISTINCT S.SUBJECT_ID FROM LIMS_SAMPLE S WHERE S.EXPERIMENT_ID = ?";
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1, experimentId);
			ResultSet rs = ps.executeQuery();
			while (rs.next())
				subjectIds.add(rs.getString("SUBJECT_ID"));
	
			rs.close();
		}
		query = "SELECT DISTINCT S.SUBJECT_ID FROM LIMS_SAMPLE S WHERE S.EXPERIMENT_ID = ?";
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1, experimentId);
			ResultSet rs = ps.executeQuery();
			while (rs.next())
				subjectIdsToKeep.add(rs.getString("SUBJECT_ID"));
	
			rs.close();
		}
		if(!subjectIdsToKeep.isEmpty()) {
			subjectIds  = subjectIds.stream().
				filter(s -> !subjectIdsToKeep.contains(s)).
				collect(Collectors.toSet());
		}
		query = "DELETE FROM LIMS_EXPERIMENTAL_SUBJECT S WHERE S.SUBJECT_ID = ?";
		try(PreparedStatement ps = conn.prepareStatement(query)){
			for(String subjectId : subjectIds) {		
				ps.setString(1, subjectId);
				ps.addBatch();
			}
			ps.executeBatch();
		}
		query = "DELETE FROM LIMS_EXPERIMENT WHERE EXPERIMENT_ID = ?";
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1, experimentId);
			ps.executeUpdate();
		}		
		ConnectionManager.releaseConnection(conn);
	}
}






































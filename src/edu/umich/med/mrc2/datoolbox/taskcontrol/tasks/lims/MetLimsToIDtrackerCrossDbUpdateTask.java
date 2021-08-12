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

package edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.lims;

import java.io.BufferedInputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import edu.umich.med.mrc2.datoolbox.data.Assay;
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignFactor;
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignLevel;
import edu.umich.med.mrc2.datoolbox.data.ExperimentalSubject;
import edu.umich.med.mrc2.datoolbox.data.InstrumentPlatform;
import edu.umich.med.mrc2.datoolbox.data.compare.ExperimentalSubjectComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.enums.UserAffiliation;
import edu.umich.med.mrc2.datoolbox.data.lims.ChromatographicSeparationType;
import edu.umich.med.mrc2.datoolbox.data.lims.IdTrackerOrganization;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSClient;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSExperiment;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSInstrument;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSOrganization;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSProject;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSSample;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSStorageLocation;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSUser;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCash;
import edu.umich.med.mrc2.datoolbox.database.idt.UserUtils;
import edu.umich.med.mrc2.datoolbox.database.lims.LIMSDataCash;
import edu.umich.med.mrc2.datoolbox.database.lims.LIMSUtils;
import edu.umich.med.mrc2.datoolbox.database.lims.MetLIMSConnectionManager;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;

public class MetLimsToIDtrackerCrossDbUpdateTask extends AbstractTask {
	
	private Map<String,String>organizationMap;
	private Collection<LIMSExperiment>newExperiments;
	private Map<String,String>extensionMimeMap;

	public MetLimsToIDtrackerCrossDbUpdateTask() {
		super();
		//	Map MetLIMS organization (key) to ID tracker organization (value)
		organizationMap = new TreeMap<String,String>();		
	}

	@Override
	public Task cloneTask() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void run() {
		setStatus(TaskStatus.PROCESSING);
		try {
			Connection metlimsConn = MetLIMSConnectionManager.getConnection();
			Connection idTrackerConn = ConnectionManager.getConnection();
			
			total = 100;
			processed = 0;
			taskDescription = "Updating user data ...";
			updateResearcherData(metlimsConn, idTrackerConn);
			processed = 5;

			taskDescription = "Updating organization data ...";
			updateLimsOrganizationData(metlimsConn, idTrackerConn);
			updateOrganizationData(metlimsConn, idTrackerConn);
			processed = 10;
						
			taskDescription = "Updating client data ...";
			updateLimsClientData(metlimsConn, idTrackerConn);
			processed = 15;

			taskDescription = "Updating project data ...";
			updateProjectData(metlimsConn, idTrackerConn);
			processed = 20;

			taskDescription = "Updating experiment data ...";
			updateExperimentData(metlimsConn, idTrackerConn);
			processed = 25;

			taskDescription = "Updating subject data ...";
			updateExpSubjectData(metlimsConn, idTrackerConn);
			processed = 30;

			taskDescription = "Updating storage data ...";
			updateStorageLocationData(metlimsConn, idTrackerConn);
			processed = 35;

			taskDescription = "Updating sample data ...";
			updateSampleData(metlimsConn, idTrackerConn);
			processed = 40;

			taskDescription = "Updating storage history data ...";
			updateStorageLocationHistoryData(metlimsConn, idTrackerConn);
			processed = 45;

			taskDescription = "Updating factor data ...";
			updateExperimentalFactorAndLevelData(metlimsConn, idTrackerConn);
			processed = 50;

			taskDescription = "Updating instrument data ...";
			updateInstrumentsData(metlimsConn, idTrackerConn);
			processed = 60;

			taskDescription = "Updating assay data ...";
			updateAssayList(metlimsConn, idTrackerConn);
			processed = 65;

			taskDescription = "Updating sample/assay data ...";
			updateSampleAssayData(metlimsConn, idTrackerConn);
			processed = 70;

			taskDescription = "Updating experiment setup data ...";
			updateExperimentSetupData(metlimsConn, idTrackerConn);

			taskDescription = "Updating compound inventory data ...";
			updateCompoundInventoryData(metlimsConn, idTrackerConn);
			processed = 75;
			
			taskDescription = "Updating protocol reports data ...";
			extensionMimeMap = getExtensionMimeMap(idTrackerConn);
			updateProtocolReports(metlimsConn, idTrackerConn);
			processed = 85;

			taskDescription = "Updating reports data ...";
			updateReports(metlimsConn, idTrackerConn);
			processed = 90;

			MetLIMSConnectionManager.releaseConnection(metlimsConn);
			ConnectionManager.releaseConnection(idTrackerConn);
			setStatus(TaskStatus.FINISHED);
		}
		catch (Exception e) {
			e.printStackTrace();
			setStatus(TaskStatus.ERROR);
		}
	}	

	private void updateResearcherData(Connection metlimsConn, Connection idTrackerConn) throws Exception {

		Collection<LIMSUser> idtUsers = UserUtils.getCompleteUserList(idTrackerConn);
		Collection<LIMSUser> metlimsUsers = getMetlimsUsers(metlimsConn);
		Set<LIMSUser> newUsers = metlimsUsers.stream().
				filter(u -> !idtUsers.contains(u)).
				collect(Collectors.toSet());
		
		if(newUsers.isEmpty())
			return;
		
		String query  =
				"INSERT INTO RESEARCHER (RESEARCHER_ID, USERNAME, LAST_NAME, FIRST_NAME, "
				+ "LAB, EMAIL, PHONE, SUPER_USER, AFFILIATION) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
		PreparedStatement ps = idTrackerConn.prepareStatement(query);
				
		for(LIMSUser user : newUsers) {
			
			ps.setString(1, user.getId());
			ps.setString(2, user.getUserName());
			ps.setString(3, user.getLastName());
			ps.setString(4, user.getFirstName());
			ps.setString(5, user.getLaboratory());
			ps.setString(6, user.getEmail());
			ps.setString(7, user.getPhone());
			ps.setString(8, "0");		
			ps.setString(9, user.getAffiliation());	
			ps.executeUpdate();			
		}
		ps.close();	
		//	UserUtils.addNewMetlimsUser(user, idTrackerConn);		
	}
	
	private Collection<LIMSUser>getMetlimsUsers(Connection metlimsConn) throws Exception {
		
		Collection<LIMSUser>metlimsUsers = new ArrayList<LIMSUser>();
		String sql = 
				"SELECT RESEARCHER_ID, LAST_NAME, FIRST_NAME, LAB, EMAIL, "
				+ "PHONE, USERNAME, FAX_NUMBER FROM METLIMS.RESEARCHER "
				+ "WHERE DELETED IS NULL "
				+ "ORDER BY RESEARCHER_ID";
		PreparedStatement ps = metlimsConn.prepareStatement(sql);
		ResultSet rs = ps.executeQuery();
		while(rs.next()) {
			
			LIMSUser user = new LIMSUser(
					rs.getString("RESEARCHER_ID"),
					rs.getString("LAST_NAME"),
					rs.getString("FIRST_NAME"),
					rs.getString("LAB"),
					rs.getString("EMAIL"),
					rs.getString("PHONE"),
					rs.getString("USERNAME"),
					false,
					UserAffiliation.CLIENT.name());
			metlimsUsers.add(user);	
		}
		rs.close();
		ps.close();
		return metlimsUsers;
	}
	
	private void updateLimsOrganizationData(Connection metlimsConn, Connection idTrackerConn) throws Exception {
		
		Collection<LIMSOrganization>metLimsOrganizationList =
				getLimsOrganizationList(metlimsConn);
		
		Collection<LIMSOrganization>idtLimsOrganizationList = 
				LIMSUtils.getLimsOrganizationList(idTrackerConn);
		
		Collection<String>presentLimsOrgIds = 
				idtLimsOrganizationList.stream().
				map(o -> o.getId()).collect(Collectors.toSet());
				
		List<LIMSOrganization> newOrganizations = 
				metLimsOrganizationList.stream().
				filter(o -> !organizationInList(o, idtLimsOrganizationList)).
				filter(o -> !presentLimsOrgIds.contains(o.getId())).
				collect(Collectors.toList());
		
		if(newOrganizations.isEmpty())
			return;
		
		String sql  =
				"INSERT INTO LIMS_ORGANIZATION (ORGANIZATION_ID, NAME, ADDRESS) "
				+ "VALUES (?, ?, ?)";
		PreparedStatement ps = idTrackerConn.prepareStatement(sql);			
		for(LIMSOrganization org : newOrganizations) {
			ps.setString(1, org.getId());
			ps.setString(2, org.getName());
			ps.setString(3, org.getAddress());
			ps.addBatch();
		}
		ps.executeBatch();
		ps.close();
	}
	
	private boolean organizationInList(
			LIMSOrganization organization, Collection<LIMSOrganization>list) {
		LIMSOrganization inList =  list.stream().
				filter(o -> isSameLimsOrganization(o, organization)).
				findFirst().orElse(null);
		
		return inList != null;
	}
	
	public static Collection<LIMSOrganization>getLimsOrganizationList(Connection metlimsConn) throws Exception{

		Collection<LIMSOrganization> organizations = new TreeSet<LIMSOrganization>();
		String sql  =
			"SELECT ORGANIZATION_ID, NAME, ADDRESS "
			+ "FROM METLIMS.ORGANIZATION ORDER BY ORGANIZATION_ID";

		PreparedStatement ps = metlimsConn.prepareStatement(sql);
		ResultSet rs = ps.executeQuery();
		while (rs.next()) {

			LIMSOrganization organization = new LIMSOrganization(
					rs.getString("ORGANIZATION_ID"),
					rs.getString("NAME"),
					rs.getString("ADDRESS"));

			organizations.add(organization);
		}
		rs.close();
		ps.close();
		return organizations;
	}
	
	private void updateLimsClientData(Connection metlimsConn, Connection idTrackerConn) throws Exception {
		
		Collection<LIMSClient>metLimsClientList = getLimsClientList(metlimsConn);
		Collection<LIMSClient>idtLimsClientList = LIMSUtils.getClientList(idTrackerConn);
		List<LIMSClient> newClients = 
				metLimsClientList.stream().
				filter(o -> !idtLimsClientList.contains(o)).
				collect(Collectors.toList());
		
		if(newClients.isEmpty())
			return;
		
		String sql  =
				"INSERT INTO LIMS_CLIENT (CLIENT_ID, DEPARTMENT_OR_DIVISION, "
				+ "LABORATORY, PRINCIPAL_INVESTIGATOR_ID, CONTACT_PERSON_ID, ORGANIZATION_ID) "
				+ "VALUES (?, ?, ?, ?, ?, ?)";
		PreparedStatement ps = idTrackerConn.prepareStatement(sql);			
		for(LIMSClient client : newClients) {
			
			if(client.getOrganization() == null) {
				System.out.println(client.getId());
				continue;
			}
			ps.setString(1, client.getId());
			ps.setString(2, client.getDepartment());
			ps.setString(3, client.getLaboratory());
			
			String piId = null;
			if(client.getPrincipalInvestigator() != null)
				piId = client.getPrincipalInvestigator().getId();
			ps.setString(4, piId);
			
			String cpId = null;
			if(client.getContactPerson() != null)
				cpId = client.getContactPerson().getId();
			
			ps.setString(5, cpId);
			ps.setString(6, client.getOrganization().getId());
			ps.addBatch();
		}
		ps.executeBatch();
		ps.close();
	}

	private Collection<LIMSClient> getLimsClientList(Connection metlimsConn) throws Exception {
		
		LIMSDataCash.refreshLimsOrganizationList();
		Collection<LIMSUser> userList = UserUtils.getCompleteUserList();
		LIMSDataCash.refreshLimsOrganizationList();
		Collection<LIMSClient> clients = new TreeSet<LIMSClient>();
		String sql  =
				"SELECT CLIENT_ID, DEPTORDIV, LAB, ORGANIZATION_ID,  " +
				"PRINCIPAL_INVESTIGATOR_ID, CONTACT_PERSON_ID " +
				"FROM METLIMS.CLIENT ORDER BY CLIENT_ID ";
		PreparedStatement ps = metlimsConn.prepareStatement(sql);
		ResultSet rs = ps.executeQuery();
		while (rs.next()) {

			LIMSClient client = new LIMSClient(
					rs.getString("CLIENT_ID"),
					rs.getString("DEPTORDIV"),
					rs.getString("LAB"),
					null);
		
			LIMSOrganization organization =
					LIMSDataCash.getLIMSOrganizationById(rs.getString("ORGANIZATION_ID"));			
			if(organization == null) {
				System.out.println("No LIMS organization for client ID " + client.getId());
				//	throw new Exception("No LIMS organization for client ID " + client.getId());
				continue;
			}	
			client.setOrganization(organization);		
			String piId = rs.getString("PRINCIPAL_INVESTIGATOR_ID");
			LIMSUser pi = userList.stream().
					filter(e -> e.getId().equals(piId)).findFirst().orElse(null);
			if (pi == null)
				System.out.println("Can not find PI for user ID " + rs.getString("PRINCIPAL_INVESTIGATOR_ID"));
			
			client.setPrincipalInvestigator(pi);
			
			String cpId = rs.getString("CONTACT_PERSON_ID");
			LIMSUser cp = userList.stream().
					filter(e -> e.getId().equals(cpId)).findFirst().orElse(null);
			if (cp == null)
				System.out.println("Can not find contact person for user ID " + rs.getString("CONTACT_PERSON_ID"));
						
			client.setContactPerson(cp);
			clients.add(client);
		}
		rs.close();
		ps.close();	
		return clients;
	}

	private void updateOrganizationData(Connection metlimsConn, Connection idTrackerConn) throws Exception {

		Collection<IdTrackerOrganization>idtOrganizationList = LIMSUtils.getOrganizationList(idTrackerConn);		
		Collection<IdTrackerOrganization>metLimsOrganizationList =  getMetLimsOrganizationList(metlimsConn);		
		Collection<IdTrackerOrganization>existingOrganizations = new HashSet<IdTrackerOrganization>();	
		
		for(IdTrackerOrganization metlimsOrg : metLimsOrganizationList) {

			for(IdTrackerOrganization org : idtOrganizationList) {
				
				if(isSameOrganization(org, metlimsOrg)) {
					existingOrganizations.add(metlimsOrg);
					break;
				}
			}
		}
		List<IdTrackerOrganization> newOrganizations = metLimsOrganizationList.stream().
				filter(o -> !existingOrganizations.contains(o)).
				collect(Collectors.toList());
		
		if(newOrganizations.isEmpty())
			return;
		
		for(IdTrackerOrganization org : newOrganizations) {
			
			String newOrgId = LIMSUtils.addNewOrganization(org, idTrackerConn);
			organizationMap.put(org.getId(), newOrgId);
			
			if(org.getPrincipalInvestigator() != null)
				updateOrganizationForUser(org.getPrincipalInvestigator().getId(), newOrgId, idTrackerConn);
			
			if(org.getContactPerson() != null)
				updateOrganizationForUser(org.getContactPerson().getId(), newOrgId, idTrackerConn);
		}
	}
	
	private boolean isSameLimsOrganization(LIMSOrganization orgOne, LIMSOrganization orgTwo) {
		
		if(!orgOne.getName().equals(orgTwo.getName()))
			return false;
		
		if(orgOne.getAddress() == null && orgTwo.getAddress() != null)
			return false;
		
		if(orgOne.getAddress() != null && orgTwo.getAddress() == null)
			return false;
		
		if(orgOne.getAddress() != null && orgTwo.getAddress() != null) {
			if(!orgOne.getAddress().equals(orgTwo.getAddress()))
				return false;
		}
		return true;
	}
	
	private boolean isSameOrganization(IdTrackerOrganization orgOne, IdTrackerOrganization orgTwo) {
		
		if(!orgOne.getName().equals(orgTwo.getName()))
			return false;
		
		if(orgOne.getAddress() == null && orgTwo.getAddress() != null)
			return false;
		
		if(orgOne.getAddress() != null && orgTwo.getAddress() == null)
			return false;
		
		if(orgOne.getAddress() != null && orgTwo.getAddress() != null) {
			if(!orgOne.getAddress().equals(orgTwo.getAddress()))
				return false;
		}
		if(orgOne.getPrincipalInvestigator() == null && orgTwo.getPrincipalInvestigator() != null)
			return false;
		
		if(orgOne.getPrincipalInvestigator() != null && orgTwo.getPrincipalInvestigator() == null)
			return false;
		
		if(orgOne.getPrincipalInvestigator() != null && orgTwo.getPrincipalInvestigator() != null) {
			if(!orgOne.getPrincipalInvestigator().equals(orgTwo.getPrincipalInvestigator()))
				return false;
		}
		if(orgOne.getContactPerson() == null && orgTwo.getContactPerson() != null)
			return false;
		
		if(orgOne.getContactPerson() != null && orgTwo.getContactPerson() == null)
			return false;
		
		if(orgOne.getContactPerson() != null && orgTwo.getContactPerson() != null) {
			if(!orgOne.getContactPerson().equals(orgTwo.getContactPerson()))
				return false;
		}
		return true;
	}
	
	private void updateOrganizationForUser(
			String userId, String organizationId, Connection idTrackerConn) throws Exception {
		
		String sql = 
				"UPDATE RESEARCHER SET ORGANIZATION_ID = ? WHERE RESEARCHER_ID = ? "
				+ "AND ORGANIZATION_ID IS NULL";
		 PreparedStatement ps = idTrackerConn.prepareStatement(sql);
		 ps.setString(1, organizationId);
		 ps.setString(2, userId);
		 ps.executeUpdate();
		 ps.close();
	}
	
	private Collection<IdTrackerOrganization>getMetLimsOrganizationList(Connection metlimsConn) throws Exception {
		
		Collection<LIMSUser> userList = UserUtils.getCompleteUserList();
		Collection<IdTrackerOrganization> metLimsOrganizationList = new ArrayList<IdTrackerOrganization>();
		String sql = 
				 "SELECT DISTINCT O.ORGANIZATION_ID, O.NAME, O.ADDRESS, C.DEPTORDIV, C.LAB, "
				 + "C.CLIENT_ID, C.PRINCIPAL_INVESTIGATOR_ID, C.CONTACT_PERSON_ID " + 
		 		"FROM  METLIMS.ORGANIZATION O, METLIMS.CLIENT C " + 
		 		"WHERE C.ORGANIZATION_ID = O.ORGANIZATION_ID";
		PreparedStatement ps = metlimsConn.prepareStatement(sql);
		ResultSet rs = ps.executeQuery();
		while (rs.next()) {

			 IdTrackerOrganization org = new IdTrackerOrganization(
					 	rs.getString("ORGANIZATION_ID"),
						rs.getString("NAME"),
						rs.getString("ADDRESS"),
						rs.getString("DEPTORDIV"),
						rs.getString("LAB"),
						rs.getString("ADDRESS"));
			 
			org.setMetlimsClientId(rs.getString("CLIENT_ID"));
			String piId = rs.getString("PRINCIPAL_INVESTIGATOR_ID");
			LIMSUser pi = userList.stream().
					filter(e -> e.getId().equals(piId)).findFirst().orElse(null);
			if (pi == null)
				System.out.println("Can not find PI for user ID " + rs.getString("PRINCIPAL_INVESTIGATOR_ID"));
			
			org.setPrincipalInvestigator(pi);
			
			String cpId = rs.getString("CONTACT_PERSON_ID");
			LIMSUser cp = userList.stream().
					filter(e -> e.getId().equals(cpId)).findFirst().orElse(null);
			if (cp == null)
				System.out.println("Can not find contact person for user ID " + rs.getString("CONTACT_PERSON_ID"));
						
			org.setContactPerson(cp);
			metLimsOrganizationList.add(org);
		}
		rs.close();
		ps.close();
		return metLimsOrganizationList;
	}
	
	private void updateProjectData(Connection metlimsConn, Connection idTrackerConn) throws Exception {
		
		LIMSDataCash.refreshLimsOrganizationList();
		LIMSDataCash.refreshLimsClientList();
		Collection<LIMSProject>existingLimsProjects = LIMSUtils.getLimsProjectList(idTrackerConn);
		Collection<LIMSProject>metLimsProjects = getMetLimsProjectList(metlimsConn);
		Collection<LIMSProject>newLMetlimsProjects = metLimsProjects.stream().
				filter(p -> !existingLimsProjects.contains(p)).collect(Collectors.toList());
		
		if(newLMetlimsProjects.isEmpty())
			return;
		
		String sql  =
			"INSERT INTO LIMS_PROJECT (PROJECT_ID, PROJECT_NAME, PROJECT_DESCRIPTION, "
			+ "CLIENT_ID, START_DATE, NOTES) VALUES (?, ?, ?, ?, ?, ?)";			
		PreparedStatement ps = idTrackerConn.prepareStatement(sql);
		
		for(LIMSProject project : newLMetlimsProjects) {
			
			if(project.getStartDate() == null)
				project.setStartDate(new Date());
			
			ps.setString(1, project.getId());
			ps.setString(2, project.getName());
			ps.setString(3, project.getDescription());
			ps.setString(4, project.getClient().getId());
			ps.setDate(5, new java.sql.Date(project.getStartDate().getTime()));
			ps.setString(6, project.getNotes());
			ps.executeUpdate();
		}
		ps.close();
		//	LIMSUtilsUni.insertMetLimsProjec(project, idTrackerConn);		
	}
	
	public static Collection<LIMSProject> getMetLimsProjectList(Connection metlimsConn) throws Exception{

		Collection<LIMSProject> projects = new TreeSet<LIMSProject>();
		String sql  =
				"SELECT PROJECT_ID, PROJECT_NAME, PROJECT_DESCRIPTION, CLIENT_ID, START_DATE, NOTES " +
				"FROM METLIMS.PROJECT P ORDER BY PROJECT_ID ";
		
		PreparedStatement ps = metlimsConn.prepareStatement(sql);
		ResultSet rs = ps.executeQuery();
		while (rs.next()) {

			LIMSClient client = LIMSDataCash.getLIMSClientById(rs.getString("CLIENT_ID"));
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
		ps.close();
		return projects;
	}	

	private void updateExperimentData(Connection metlimsConn, Connection idTrackerConn) throws Exception {
		
		LIMSDataCash.refreshProjectList();

		Collection<LIMSExperiment>existingLimsExperiments = 
				LIMSUtils.getExperimentList("EX00000", idTrackerConn);
		Collection<LIMSExperiment>metLimsExperiments = 
				getMetlimsExperimentList("EX00000", metlimsConn);
		
		newExperiments = metLimsExperiments.stream().
				filter(e -> !existingLimsExperiments.contains(e)).
				collect(Collectors.toList());
		
		if(newExperiments.isEmpty())
			return;
		
		String sql = "SELECT EXPERIMENT_NAME FROM LIMS_EXPERIMENT WHERE EXPERIMENT_ID = ?";
		PreparedStatement ps = idTrackerConn.prepareStatement(sql);
		
		String insSql =
				"INSERT INTO LIMS_EXPERIMENT (EXPERIMENT_ID, EXPERIMENT_NAME, PROJECT_ID,  " +
				"EXPERIMENT_DESCRIPTION, DATE_INITIATED, NOTES, " +
				"OWNER_ID, SERVICE_REQUEST_ID, CHEAR, MOTRPAC) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ";				
		PreparedStatement insPs = idTrackerConn.prepareStatement(insSql);

		for(LIMSExperiment experiment : newExperiments) {
			
			ps.setString(1,  experiment.getId());
			ResultSet rs = ps.executeQuery();
			String name = null;
			while(rs.next())
				name = rs.getString("EXPERIMENT_NAME");
			
			rs.close();
			if(name != null) {
				//	ps.close();
				continue;
			}		
			insPs.setString(1, experiment.getId());
			insPs.setString(2, experiment.getName());
			insPs.setString(3, experiment.getProject().getId());
			insPs.setString(4, experiment.getDescription());
			
			java.sql.Date startDate = null;
			if(experiment.getStartDate() != null) {
				try {
					startDate = new java.sql.Date(experiment.getStartDate().getTime());
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			if(startDate != null)
				insPs.setDate(5, new java.sql.Date(experiment.getStartDate().getTime()));
			else
				insPs.setNull(5, java.sql.Types.NULL);
				
			insPs.setString(6, experiment.getNotes());
			
			if(experiment.getCreator() != null)
				insPs.setString(7, experiment.getCreator().getId());
			else
				insPs.setNull(7, java.sql.Types.NULL);
				
			insPs.setString(8, experiment.getServiceRequestId());
			insPs.setBoolean(9, experiment.isChear());
			insPs.setBoolean(10, experiment.isMotrpac());
			insPs.executeUpdate();		
		}
		insPs.close();
	}
	
	private Collection<LIMSExperiment>getMetlimsExperimentList(
			String startFromId, Connection metlimsConn) throws Exception {
		
		Collection<LIMSExperiment>metlimsExperimentList = new ArrayList<LIMSExperiment>();
		String sql = 
				"SELECT DISTINCT E.EXP_ID, E.SERVICE_REQUEST_ID, E.EXP_NAME, E.PROJECT_ID, E.EXP_DESCRIPTION,  " +
				"E.CREATIONDATE, E.NOTES, E.CREATOR, E.IS_CHEAR, E.PRIORITY_TYPE, O.NIH_GRANT_NUMBER  " +
				"FROM METLIMS.EXPERIMENT E LEFT OUTER JOIN METLIMS.SHORTCODES O ON E.EXP_ID = O.EXP_ID   " +
				"WHERE E.EXP_ID >= ? ORDER BY E.EXP_ID";
		PreparedStatement ps = metlimsConn.prepareStatement(sql);
		ps.setString(1,  startFromId);
		ResultSet rs = ps.executeQuery();
		while (rs.next()) {

			LIMSExperiment experiment = new LIMSExperiment(
					rs.getString("EXP_ID"),
					rs.getString("EXP_NAME"),
					rs.getString("EXP_DESCRIPTION"),
					rs.getString("NOTES"),
					rs.getString("SERVICE_REQUEST_ID"),
					rs.getTimestamp("CREATIONDATE"));
			experiment.setNihGrant(rs.getString("NIH_GRANT_NUMBER"));
			experiment.setChear(rs.getBoolean("IS_CHEAR"));			
			LIMSProject project = 
					LIMSDataCash.getProjectById(rs.getString("PROJECT_ID"));
			experiment.setProject(project);
			LIMSUser creator = IDTDataCash.getUserById(rs.getString("CREATOR"));
			experiment.setCreator(creator);
			metlimsExperimentList.add(experiment);
		}
		rs.close();
		ps.close();
		return metlimsExperimentList;
	}

	private void updateExpSubjectData(Connection metlimsConn, Connection idTrackerConn) throws Exception {
		
		Collection<ExperimentalSubject>subjects = 
				getExperimentalSubjectsForNewExperiments(metlimsConn);
		
		String checkQuery = 
				"SELECT SUBJECT_TAX_ID FROM LIMS_EXPERIMENTAL_SUBJECT WHERE SUBJECT_ID = ?";
		PreparedStatement checkPs = idTrackerConn.prepareStatement(checkQuery);
		Collection<ExperimentalSubject>newSubjects = 
				new TreeSet<ExperimentalSubject>(new ExperimentalSubjectComparator(SortProperty.ID));
		
		for(ExperimentalSubject s : subjects) {
			
			checkPs.setString(1, s.getSubjectId());
			boolean inDatabase = false;
			ResultSet rs = checkPs.executeQuery();
			while (rs.next())
				inDatabase = true;
			
			rs.close();
			if(!inDatabase)
				newSubjects.add(s);
		}
		checkPs.close();
		
		String sql = 
				"INSERT INTO LIMS_EXPERIMENTAL_SUBJECT "
				+ "(SUBJECT_ID, SUBJECT_TYPE_ID, SUBJECT_TAX_ID, USER_SUBJECT_ID) "
				+ "VALUES (?, ?, ?, ?)";
		PreparedStatement ps = idTrackerConn.prepareStatement(sql);
		for(ExperimentalSubject s : newSubjects) {
			ps.setString(1, s.getSubjectId());
			ps.setString(2, s.getSubjectType());
			ps.setInt(3, s.getTaxonomyId());
			ps.setString(4, s.getName());
			ps.addBatch();
		}
		ps.executeBatch();
		ps.close();
	}
	
	private Collection<ExperimentalSubject>getExperimentalSubjectsForNewExperiments(Connection metlimsConn) throws Exception {
		
		Map<String, ExperimentalSubject>subjects = new TreeMap<String, ExperimentalSubject>();
		String sql = 
				"SELECT DISTINCT U.SUBJECT_ID, U.SUBJECT_TYPE_ID, "
				+ "U.SUBJECT_TAX_ID, U.USER_SUBJECT_ID " +
				"FROM METLIMS.SUBJECT U, METLIMS.SAMPLE S " +
				"WHERE S.SUBJECT_ID = U.SUBJECT_ID " +
				"AND S.EXP_ID = ? ORDER BY 1";
		PreparedStatement ps = metlimsConn.prepareStatement(sql);
		for(LIMSExperiment experiment : newExperiments) {
			
			ps.setString(1, experiment.getId());
			ResultSet rs = ps.executeQuery();
			while(rs.next()) {
				
				ExperimentalSubject subj = new ExperimentalSubject(
							rs.getString("SUBJECT_ID"),
							rs.getString("USER_SUBJECT_ID"),
							rs.getInt("SUBJECT_TAX_ID"),
							rs.getString("SUBJECT_TYPE_ID"));
				subjects.put(subj.getSubjectId(), subj);
			}
			rs.close();
		}
		ps.close();
		return subjects.values().stream().collect(Collectors.toSet());
	}

	private void updateStorageLocationData(Connection metlimsConn, Connection idTrackerConn) throws Exception {

		Collection<String>newSids = getNewStorageLocations(metlimsConn, idTrackerConn);
		if(newSids.isEmpty())
			return;
		
		Collection<LIMSStorageLocation>newLocations = new ArrayList<LIMSStorageLocation>();
		String sql = "SELECT LOCATIONID, LOCDESCRIPTION, ROOM, UNIT "
				+ "FROM METLIMS.LOCATIONS WHERE LOCATIONID = ?";
		PreparedStatement ps = metlimsConn.prepareStatement(sql);
		for(String sid : newSids) {
			ps.setString(1, sid);
			ResultSet rs = ps.executeQuery();
			while(rs.next()) {
				LIMSStorageLocation loc = new LIMSStorageLocation(
						rs.getString("LOCATIONID"),
						rs.getString("LOCDESCRIPTION"),
						rs.getString("ROOM"),
						rs.getString("UNIT"));
				newLocations.add(loc);
			}
			rs.close();
		}		
		sql = "INSERT INTO STORAGE_LOCATION (LOCATION_ID, LOCATION_DESCRIPTION, ROOM, UNIT) "
				+ "VALUES (?, ?, ?, ?)";
		ps = idTrackerConn.prepareStatement(sql);
		for(LIMSStorageLocation loc : newLocations) {
			ps.setString(1, loc.getId());
			ps.setString(2, loc.getDescription());
			ps.setString(3, loc.getRoom());
			ps.setString(4, loc.getUnit());
			ps.addBatch();
		}
		ps.executeBatch();
		ps.close();
	}
	
	private Collection<String>getNewStorageLocations(Connection metlimsConn, Connection idTrackerConn) throws Exception {
		
		Collection<String>idtrackerStorageIds = new ArrayList<String>();	
		String sql = "SELECT LOCATION_ID FROM STORAGE_LOCATION ORDER BY 1";
		PreparedStatement ps = idTrackerConn.prepareStatement(sql);
		ResultSet rs = ps.executeQuery();
		while(rs.next())
			idtrackerStorageIds.add(rs.getString("LOCATION_ID"));
		
		rs.close();
		
		Collection<String>metlimsStorageIds = new ArrayList<String>();
		sql = "SELECT LOCATIONID FROM METLIMS.LOCATIONS ORDER BY 1";
		ps = metlimsConn.prepareStatement(sql);
		rs = ps.executeQuery();
		while(rs.next())
			metlimsStorageIds.add(rs.getString("LOCATIONID"));
		
		rs.close();
		ps.close();
		
		return metlimsStorageIds.stream().
				filter(i -> !idtrackerStorageIds.contains(i)).
				collect(Collectors.toSet());
	}

	private void updateSampleData(Connection metlimsConn, Connection idTrackerConn) throws Exception {

		Collection<LIMSSample>newSamples = getExperimentalSamplesForNewExperiments(metlimsConn);
		if(newSamples.isEmpty())
			return;
		
		String sql = 
				"INSERT INTO LIMS_SAMPLE (SAMPLE_ID, SAMPLE_NAME, EXPERIMENT_ID,  " +
				"USER_DESCRIPTION, LOCATION_ID, USER_DEFINED_SAMPLE_TYPE,  " +
				"CONCENTRATION, CONCENTRATION_UNIT, INITIAL_VOLUME, INITIAL_VOLUME_UNIT,  " +
				"CURRENT_VOLUME, CURRENT_VOLUME_UNIT, BARCODE_VERIFIED_BY_SCAN,  " +
				"STATUS_ID, NOTES, DATE_CREATED, SAMPLE_TYPE_ID, SUBJECT_ID) " +
				"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ";
		PreparedStatement ps = idTrackerConn.prepareStatement(sql);
		for(LIMSSample sample : newSamples) {
			
			java.sql.Date dateCreated = null;		
			if(sample.getDateCreated() != null)
				dateCreated = new java.sql.Date(sample.getDateCreated().getTime());
			
			ps.setString(1, sample.getId());
			ps.setString(2, sample.getName());
			ps.setString(3, sample.getExperimentId());
			ps.setString(4, sample.getUserDescription());
			ps.setString(5, sample.getLocationId());
			ps.setString(6, sample.getUserDefinedSampleType());
			ps.setDouble(7, sample.getConcentration());
			ps.setString(8, sample.getConcentrationUnit());
			ps.setDouble(9, sample.getInitialVolume());
			ps.setString(10, sample.getInitialVolumeUnit());
			ps.setDouble(11, sample.getCurrentVolume());
			ps.setString(12, sample.getCurrentVolumeUnit());
			ps.setString(13, sample.getBarcodeVerifiedByScan());
			ps.setString(14, sample.getStatusId());
			ps.setString(15, sample.getNotes());
			ps.setDate(16, dateCreated);
			ps.setString(17, sample.getLimsSampleType());
			ps.setString(18, sample.getSubjectId());
			ps.addBatch();
		}
		ps.executeBatch();
		ps.close();
	}
	
	private Collection<LIMSSample>getExperimentalSamplesForNewExperiments(Connection metlimsConn) throws Exception {
		
		Map<String,LIMSSample>newSamples = new TreeMap<String, LIMSSample>();
		String sql = 
				"SELECT SAMPLE_ID, SAMPLE_NAME, EXP_ID, USER_DESCRIPTION, STORAGEINSTANCEID,  " +
				"USER_DEFINED_SAMPLE_TYPE, CONCENTRATION, CONCENTRATIONUNITS_ID, INITIAL_VOLUME,  " +
				"INIT_VOL_UNITS, CURRENT_VOLUME, CURR_VOL_UNITS, BARCODEVERIFIEDBYSCAN,  " +
				"REALOBJECT, NOTES, DATE_CREATED, SAMPLE_TYPE_ID, SUBJECT_ID  " +
				"FROM METLIMS.SAMPLE WHERE EXP_ID = ? ";
		PreparedStatement ps = metlimsConn.prepareStatement(sql);
		for(LIMSExperiment experiment : newExperiments) {
			
			ps.setString(1, experiment.getId());
			ResultSet rs = ps.executeQuery();
			while(rs.next()) {
				
				Date dateCreated = null;
				if(rs.getDate("DATE_CREATED") != null)
					dateCreated = new Date(rs.getDate("DATE_CREATED").getTime());
				
				LIMSSample sample = new LIMSSample(
						rs.getString("SAMPLE_ID"), 
						rs.getString("SAMPLE_NAME"), 
						experiment.getId(),
						rs.getString("USER_DESCRIPTION"),
						rs.getString("SAMPLE_TYPE_ID"),
						rs.getString("STORAGEINSTANCEID"),
						rs.getString("USER_DEFINED_SAMPLE_TYPE"),
						rs.getDouble("CONCENTRATION"),
						rs.getString("CONCENTRATIONUNITS_ID"),
						rs.getDouble("INITIAL_VOLUME"),
						rs.getString("INIT_VOL_UNITS"),
						rs.getDouble("CURRENT_VOLUME"),
						rs.getString("CURR_VOL_UNITS"),
						rs.getString("BARCODEVERIFIEDBYSCAN"),
						rs.getString("REALOBJECT"),
						rs.getString("NOTES"),
						dateCreated,
						rs.getString("SUBJECT_ID"));				
				newSamples.put(sample.getId(), sample);				
			}
			rs.close();
		}
		ps.close();		
		return newSamples.values();		
	}
	
	private void updateStorageLocationHistoryData(Connection metlimsConn, Connection idTrackerConn) throws Exception {
		//	TODO Skip for now, maybe copy the whole thing once in a while
	}

	private void updateExperimentalFactorAndLevelData(Connection metlimsConn, Connection idTrackerConn) throws Exception {

		Map<LIMSExperiment, Collection<ExperimentDesignFactor>>designs = 
				getMetlimsExperimentsDesigns(newExperiments,  metlimsConn);
		
		String sql = "INSERT INTO LIMS_EXPERIMENTAL_FACTOR (FACTOR_ID, EXPERIMENT_ID,  " +
				"FACTOR_NAME, FACTOR_DESCRIPTION) VALUES (?, ?, ?, ?) ";
		String levelSql = "INSERT INTO LIMS_EXPERIMENTAL_FACTOR_LEVEL (LEVEL_ID, FACTOR_ID, " +
				"LEVEL_NAME, LEVEL_DESCRIPTION) VALUES (?, ?, ?, ?) ";
		PreparedStatement ps = idTrackerConn.prepareStatement(sql);
		PreparedStatement levps = idTrackerConn.prepareStatement(levelSql);		
		for (Entry<LIMSExperiment, Collection<ExperimentDesignFactor>> entry : designs.entrySet()) {
		
			ps.setString(2, entry.getKey().getId());
			for(ExperimentDesignFactor  f : entry.getValue()) {
				ps.setString(1, f.getFactorId());
				ps.setString(3, f.getName());
				ps.setString(4, f.getFactorDescription());
				ps.addBatch();
				levps.setString(2, f.getFactorId());
				for(ExperimentDesignLevel l : f.getLevels()) {
					levps.setString(1, l.getLevelId());
					levps.setString(3, l.getName());
					levps.setString(4, l.getLevelDescription());
					levps.addBatch();
				}
			}
			ps.executeBatch();
			levps.executeBatch();
		}
		levps.close();
		ps.close();
	}
	
	private Map<LIMSExperiment, Collection<ExperimentDesignFactor>>getMetlimsExperimentsDesigns(
			Collection<LIMSExperiment> experiments, Connection metlimsConn) throws Exception {
		
		Map<LIMSExperiment, Collection<ExperimentDesignFactor>>expDessigns = 
				new TreeMap<LIMSExperiment, Collection<ExperimentDesignFactor>>();
		
		String sql = "SELECT FACTOR_ID, FACTOR_NAME, DESCRIPTION "
				+ "FROM METLIMS.EXPERIMENTAL_FACTORS WHERE EXPERIMENT_ID = ?";
		String levelSql = "SELECT LEVEL_ID, VALUE, DESCRIPTION "
				+ "FROM METLIMS.FACTOR_LEVELS WHERE FACTOR_ID = ?";
		PreparedStatement ps = metlimsConn.prepareStatement(sql);
		PreparedStatement levps = metlimsConn.prepareStatement(levelSql);
		for(LIMSExperiment experiment : experiments) {
			
			Collection<ExperimentDesignFactor>design = new TreeSet<ExperimentDesignFactor>();
			ps.setString(1, experiment.getId());
			ResultSet rs = ps.executeQuery();
			while(rs.next()) {
				ExperimentDesignFactor factor = new ExperimentDesignFactor(
						rs.getString("FACTOR_ID"),
						rs.getString("FACTOR_NAME"),
						rs.getString("DESCRIPTION"));
				
				levps.setString(1, factor.getFactorId());
				ResultSet levrs = levps.executeQuery();
				while(levrs.next()) {
					
					ExperimentDesignLevel level = new ExperimentDesignLevel(
							levrs.getString("LEVEL_ID"),
							levrs.getString("VALUE"),
							levrs.getString("DESCRIPTION"));
					factor.addLevel(level);
				}
				levrs.close();
				design.add(factor);
			}
			rs.close();
			expDessigns.put(experiment, design);
		}
		levps.close();
		ps.close();
		return expDessigns;
	}

	private void updateInstrumentsData(Connection metlimsConn, Connection idTrackerConn) throws Exception {

		Collection<LIMSInstrument>metlimsInstruments = 
				getMetlimsAnalyticalInstrumentList(metlimsConn);
		Set<String> idtInstrumentIds = 
				LIMSDataCash.getAnalyticalInstruments().stream().
					map(i -> i.getInstrumentId()).collect(Collectors.toSet());		
		Collection<LIMSInstrument>newInstruments = 
				metlimsInstruments.stream().
				filter(i -> !idtInstrumentIds.contains(i.getInstrumentId())).
				collect(Collectors.toSet());
		if(newInstruments.isEmpty())
			return;
		
		String sql = 
				"INSERT INTO INSTRUMENT (INSTRUMENT_ID, NAME, DESCRIPTION, "
				+ "MANUFACTURER, MODEL, SERIAL_NUMBER, SEPARATION_TYPE, "
				+ "MASS_ANALYZER, PLATFORM_ID) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
		PreparedStatement ps = idTrackerConn.prepareStatement(sql);
		for(LIMSInstrument instrument : newInstruments) {
			
			String sepType = null;
			if(instrument.getChromatographicSeparationType() != null)
				sepType = instrument.getChromatographicSeparationType().getId();
			
			String maType = null;
			if(instrument.getMassAnalyzerType() != null)
				maType = instrument.getMassAnalyzerType().getId();
			
			String platfId = null;
			if(instrument.getInstrumentPlatform() != null)
				platfId = instrument.getInstrumentPlatform().getId();
			
			ps.setString(1, instrument.getInstrumentId());
			ps.setString(2, instrument.getInstrumentName());
			ps.setString(3, instrument.getDescription());
			ps.setString(4, instrument.getManufacturer());
			ps.setString(5, instrument.getModel());
			ps.setString(6, instrument.getSerialNumber());
			ps.setString(7, sepType);
			ps.setString(8, maType);
			ps.setString(9, platfId);
			
			ps.executeUpdate();
		}
		ps.close();
	}
	
	private Collection<LIMSInstrument>getMetlimsAnalyticalInstrumentList(
			Connection metlimsConn) throws Exception {
		
		Collection<LIMSInstrument>instruments = new ArrayList<LIMSInstrument>();
		String sql = 
				"SELECT INSTRUMENT_ID, NAME, DESCRIPTION, TYPE, ROOM, "
				+ "MANUFACTURER, MODEL, SERIAL_NUMBER FROM METLIMS.INSTRUMENT "
				+ "WHERE CLASS = 'ANALYTICAL' ORDER BY 1";
		PreparedStatement ps = metlimsConn.prepareStatement(sql);
		ResultSet rs = ps.executeQuery();
		while(rs.next()) {
			ChromatographicSeparationType separationType = 
					LIMSDataCash.getChromatographicSeparationTypeById(rs.getString("TYPE"));
			InstrumentPlatform instrumentPlatform = 
					LIMSDataCash.getInstrumentPlatformByManufacturer(rs.getString("MANUFACTURER"));			
			
			LIMSInstrument instrument = new LIMSInstrument(			
					rs.getString("INSTRUMENT_ID"),
					rs.getString("NAME"),
					rs.getString("DESCRIPTION"),
					null,	//	 METLIMS doesn't have this data
					separationType,
					rs.getString("MANUFACTURER"),
					rs.getString("MODEL"),
					rs.getString("SERIAL_NUMBER")) ;
			instrument.setInstrumentPlatform(instrumentPlatform);
			instruments.add(instrument);
		}
		rs.close();
		ps.close();
		return instruments;
	}

	private void updateAssayList(Connection metlimsConn, Connection idTrackerConn) throws Exception {
		
		Collection<Assay>metlimsAssaysList = getMetlimsAssaysList(metlimsConn);
		Collection<Assay>idtAssaysList = LIMSDataCash.getAssays();
		Set<Assay> newAssays = metlimsAssaysList.stream().
				filter(a -> !idtAssaysList.contains(a)).collect(Collectors.toSet());
		
		if(newAssays.isEmpty()) 
			return;
		
		for(Assay assay : newAssays)
			addNewAssay(assay, idTrackerConn);		
	}
	
	private void addNewAssay(Assay method, Connection conn) throws Exception {

		String query =
			"INSERT INTO LIMS_ASSAY (ASSAY_NAME, ASSAY_ID, PLATFORM_ID, ALTERNATE_NAME) VALUES (?, ?, ?, ?)";
		
		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, method.getName());
		ps.setString(2, method.getId());
		String platformId = null;
		if(method.getInstrumentPlatform() != null)
			platformId = method.getInstrumentPlatform().getId();
		
		ps.setString(3, platformId);
		ps.setString(4, method.getAlternativeName());
		ps.executeUpdate();
		ps.close();
	}
	
	private Collection<Assay>getMetlimsAssaysList(Connection metlimsConn) throws Exception {
		
		Collection<Assay>assays = new ArrayList<Assay>();
		String sql = 
				"SELECT ASSAY_ID, ASSAY_NAME, PLATFORM_ID, "
				+ "ALTERNATE_NAME FROM METLIMS.ASSAYS ORDER BY 1";
		PreparedStatement ps = metlimsConn.prepareStatement(sql);
		ResultSet rs = ps.executeQuery();
		while(rs.next()) {
			Assay newAssay = new Assay(
					rs.getString("ASSAY_ID"), 
					rs.getString("ASSAY_NAME"), 
					rs.getString("ALTERNATE_NAME"));
			InstrumentPlatform ip = LIMSDataCash.getInstrumentPlatformById(rs.getString("PLATFORM_ID"));
			newAssay.setInstrumentPlatform(ip);
			assays.add(newAssay);
		}
		rs.close();
		ps.close();
		return assays;
	}

	private void updateSampleAssayData(Connection metlimsConn, Connection idTrackerConn) throws Exception {

		Map<String, Collection<String>>sampleAssayMap = new TreeMap<String, Collection<String>>();
		String sql = 
			"SELECT A.SAMPLE_ID, A.ASSAY_ID " +
			"FROM METLIMS.SAMPLE_ASSAYS A, " +
			"METLIMS.SAMPLE S WHERE S.EXP_ID = ? " +
			"AND S.SAMPLE_ID = A.SAMPLE_ID ORDER BY 1,2 ";
		PreparedStatement ps = metlimsConn.prepareStatement(sql);
		
		String sqlInsert = 
				"INSERT INTO LIMS_SAMPLE_ASSAY_MAP (SAMPLE_ID, ASSAY_ID) VALUES (?, ?)";
		PreparedStatement psinsert = idTrackerConn.prepareStatement(sqlInsert);
		
		for(LIMSExperiment experiment : newExperiments) {
			
			sampleAssayMap = new TreeMap<String, Collection<String>>();
			ps.setString(1, experiment.getId());
			ResultSet rs = ps.executeQuery();
			while(rs.next()) {
				
				String sampleId = rs.getString("SAMPLE_ID");
				String assayId = rs.getString("ASSAY_ID");
				if(!sampleAssayMap.containsKey(sampleId))
					sampleAssayMap.put(sampleId,  new TreeSet<String>());
				
				sampleAssayMap.get(sampleId).add(assayId);			
			}
			rs.close();
			for (Entry<String, Collection<String>> entry : sampleAssayMap.entrySet()) {

				psinsert.setString(1, entry.getKey());
				for(String assayId : entry.getValue()) {
					psinsert.setString(2, assayId);
					psinsert.addBatch();
				}
			}
			psinsert.executeBatch();
		}
		ps.close();		
		psinsert.close();
	}

	private void updateExperimentSetupData(Connection metlimsConn, Connection idTrackerConn) throws Exception {
				
		String factorQuery = 
			"SELECT FACTOR_ID, FACTOR_NAME, DESCRIPTION  " +
			"FROM METLIMS.EXPERIMENTAL_FACTORS " +
			"WHERE EXPERIMENT_ID = ? ORDER BY 1";
		PreparedStatement factorPs = metlimsConn.prepareStatement(factorQuery);
		String levelQuery = "SELECT L.LEVEL_ID, L.FACTOR_ID, L.VALUE, L.DESCRIPTION  " +
				"FROM METLIMS.FACTOR_LEVELS L,  METLIMS.EXPERIMENTAL_FACTORS F " +
				"WHERE L.FACTOR_ID = F.FACTOR_ID " +
				"AND EXPERIMENT_ID = ? ORDER BY 2,1";
		PreparedStatement levelPs = metlimsConn.prepareStatement(levelQuery);
		for(LIMSExperiment experiment : newExperiments) {
			
			Collection<ExperimentDesignFactor>factors = new ArrayList<ExperimentDesignFactor>();
			factorPs.setString(1, experiment.getId());
			ResultSet fRes = factorPs.executeQuery();
			while(fRes.next()) {
				ExperimentDesignFactor f = new ExperimentDesignFactor(
						fRes.getString("FACTOR_ID"),
						fRes.getString("FACTOR_NAME"), 
						fRes.getString("DESCRIPTION"));
				factors.add(f);
			}
			fRes.close();
			levelPs.setString(1, experiment.getId());
			ResultSet lRes = levelPs.executeQuery();
			while(lRes.next()) {
				
				ExperimentDesignLevel l = new ExperimentDesignLevel(
						lRes.getString("LEVEL_ID"),
						lRes.getString("VALUE"), 
						lRes.getString("DESCRIPTION"));
				String factorId = lRes.getString("FACTOR_ID");
				factors.stream().filter(f -> f.getFactorId().equals(factorId)).forEach(f -> f.addLevel(l));
			}
			//	insertExperimentDesign(experiment, factors, idTrackerConn);
			insertSampleExperimentDesign(experiment, metlimsConn, idTrackerConn);
		}
		factorPs.close();
		levelPs.close();		
	}

	private void insertSampleExperimentDesign(
			LIMSExperiment experiment, Connection metlimsConn, Connection idTrackerConn) throws Exception {

		Map<String, Collection<String>>sampleLevelMap = new TreeMap<String, Collection<String>>();
		String sql = 
				"SELECT U.SAMPLE_ID, U.LEVEL_ID  " +
				"FROM METLIMS.EXPERIMENT_SETUP U, " +
				"METLIMS.SAMPLE S " +
				"WHERE U.SAMPLE_ID = S.SAMPLE_ID " +
				"AND S.EXP_ID = ? ORDER BY 1,2 ";
		PreparedStatement ps = metlimsConn.prepareStatement(sql);
		ps.setString(1, experiment.getId());
		ResultSet rs = ps.executeQuery();
		while(rs.next()) {
			
			String sampleId = rs.getString("SAMPLE_ID");
			String levelId = rs.getString("LEVEL_ID");
			if(!sampleLevelMap.containsKey(sampleId))
				sampleLevelMap.put(sampleId,  new TreeSet<String>());
			
			sampleLevelMap.get(sampleId).add(levelId);	
		}
		rs.close();
		ps.close();
		String sqlInsert = 
				"INSERT INTO LIMS_EXPERIMENT_DESIGN (SAMPLE_ID, LEVEL_ID) VALUES (?, ?)";
		PreparedStatement psinsert = idTrackerConn.prepareStatement(sqlInsert);
		for (Entry<String, Collection<String>> entry : sampleLevelMap.entrySet()) {

			psinsert.setString(1, entry.getKey());
			for(String levelId : entry.getValue()) {
				psinsert.setString(2, levelId);
				psinsert.addBatch();
			}
		}
		psinsert.executeBatch();
		psinsert.close();
	}

	private void insertExperimentDesign(
			LIMSExperiment experiment, 
			Collection<ExperimentDesignFactor> factors, 
			Connection idTrackerConn) throws Exception {

		String factorQuery = 
				"INSERT INTO LIMS_EXPERIMENTAL_FACTOR " +
				"(FACTOR_ID, EXPERIMENT_ID, FACTOR_NAME, FACTOR_DESCRIPTION)  " +
				"VALUES (?, ?, ?, ?) ";
		PreparedStatement factorPs = idTrackerConn.prepareStatement(factorQuery);
		factorPs.setString(2, experiment.getId());
		String levelQuery = 
				"INSERT INTO LIMS_EXPERIMENTAL_FACTOR_LEVEL " +
				"(LEVEL_ID, FACTOR_ID, LEVEL_NAME, LEVEL_DESCRIPTION)  " +
				"VALUES (?, ?, ?, ?) ";
		PreparedStatement levelPs = idTrackerConn.prepareStatement(levelQuery);
		for(ExperimentDesignFactor f : factors) {

			factorPs.setString(1, f.getFactorId());
			factorPs.setString(3, f.getName());
			factorPs.setString(4, f.getFactorDescription());
			factorPs.addBatch();
			
			levelPs.setString(2, f.getFactorId());
			for(ExperimentDesignLevel l : f.getLevels()) {
				levelPs.setString(1, l.getLevelId());
				levelPs.setString(3, l.getName());
				levelPs.setString(4, l.getLevelDescription());
				levelPs.addBatch();
			}			
		}
		factorPs.executeBatch();
		levelPs.executeBatch();
		factorPs.close();
		levelPs.close();
	}

	private void updateCompoundInventoryData(Connection metlimsConn, Connection idTrackerConn) throws Exception {
		//	TODO update once in a while by copying the whole table
	}
	
	private void updateProtocolReports(Connection metlimsConn, Connection idTrackerConn) throws Exception {
		
		TreeSet<String>idtIds = new TreeSet<String>();
		TreeSet<String>metlimsIds = new TreeSet<String>();
		String sql = "SELECT REPORT_ID FROM METLIMS.PROTOCOL_REPORT";
		PreparedStatement ps = metlimsConn.prepareStatement(sql);
		ResultSet rs = ps.executeQuery();
		while(rs.next())
			metlimsIds.add(rs.getString("REPORT_ID"));
		
		rs.close();
		
		sql = "SELECT REPORT_ID FROM LIMS_PROTOCOL_REPORT";
		ps = idTrackerConn.prepareStatement(sql);
		rs = ps.executeQuery();
		while(rs.next())
			idtIds.add(rs.getString("REPORT_ID"));
		
		rs.close();
		ps.close();
		
		Set<String> newIds = metlimsIds.stream().
				filter(i -> !idtIds.contains(i)).collect(Collectors.toSet());
		if(newIds.isEmpty())
			return;
		
		copyProtocolReport(newIds, metlimsConn, idTrackerConn);
	}
	
	private void copyProtocolReport(Collection<String>reportIds, Connection metlimsConn, Connection idTrackerConn) throws Exception {
		
		String pReportSql = 
				"SELECT REPORT_TYPE, REPORT_FILE, REPORT_DESCRIPTION,  " +
				"DATE_CREATED, LOADED_BY, EXPERIMENT_ID, FILE_NAME, FILE_TYPE,  " +
				"ASSAY_ID, DELETED FROM METLIMS.PROTOCOL_REPORT WHERE REPORT_ID = ?";
		PreparedStatement getPs = metlimsConn.prepareStatement(pReportSql);
		String insertSql = 
				"INSERT INTO LIMS_PROTOCOL_REPORT (REPORT_ID, REPORT_TYPE, REPORT_FILE, REPORT_DESCRIPTION,  " +
				"DATE_CREATED, LOADED_BY, EXPERIMENT_ID, FILE_NAME, FILE_TYPE, ASSAY_ID, DELETED) " +
				"VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ";
		PreparedStatement putPs = idTrackerConn.prepareStatement(insertSql);
		for (String reportId : reportIds) {

			getPs.setString(1, reportId);
			ResultSet rs = getPs.executeQuery();
			while (rs.next()) {

				putPs.setString(1, reportId);
				putPs.setString(2, rs.getString("REPORT_TYPE"));
				putPs.setString(4, rs.getString("REPORT_DESCRIPTION"));
				putPs.setDate(5, rs.getDate("DATE_CREATED"));				
				putPs.setString(6, rs.getString("LOADED_BY"));
				putPs.setString(7, rs.getString("EXPERIMENT_ID"));
				putPs.setString(8, rs.getString("FILE_NAME"));
				putPs.setString(9, rs.getString("FILE_TYPE"));
				putPs.setString(10, rs.getString("ASSAY_ID"));
				putPs.setString(11, rs.getString("DELETED"));
				
				//	TODO make this Postgres-compatible ?
				Blob blob = rs.getBlob("REPORT_FILE");
				BufferedInputStream is = new BufferedInputStream(blob.getBinaryStream());				
				putPs.setBinaryStream(3, is, blob.length());

				putPs.executeUpdate();
				is.close();
				blob.free();
			}
			rs.close();
		}
		getPs.close();
		putPs.close();
	}
	
	private void updateReports(Connection metlimsConn, Connection idTrackerConn) throws Exception {
		
		TreeSet<String>idtIds = new TreeSet<String>();
		TreeSet<String>metlimsIds = new TreeSet<String>();
		String sql = "SELECT REPORT_ID FROM METLIMS_LIBRARY.ANALYSIS_REPORT";
		PreparedStatement ps = metlimsConn.prepareStatement(sql);
		ResultSet rs = ps.executeQuery();
		while(rs.next())
			metlimsIds.add(rs.getString("REPORT_ID"));
		
		rs.close();
		
		sql = "SELECT REPORT_ID FROM LIMS_ANALYSIS_REPORT";
		ps = idTrackerConn.prepareStatement(sql);
		rs = ps.executeQuery();
		while(rs.next())
			idtIds.add(rs.getString("REPORT_ID"));
		
		rs.close();
		ps.close();
		
		Set<String> newIds = metlimsIds.stream().
				filter(i -> !idtIds.contains(i)).collect(Collectors.toSet());
		if(newIds.isEmpty())
			return;
		
		copyAnalysisReport(newIds, metlimsConn, idTrackerConn);
	}
	
	private void copyAnalysisReport(Collection<String>reportIds, Connection metlimsConn, Connection idTrackerConn) throws Exception {
		
		String pReportSql = 
				"SELECT REPORT_TYPE, REPORT_FILE, REPORT_DESCRIPTION,  " +
				"DATE_CREATED, LOADED_BY, EXPERIMENT_ID, FILE_NAME, FILE_TYPE,  " +
				"ASSAY_ID, DELETED FROM METLIMS_LIBRARY.ANALYSIS_REPORT WHERE REPORT_ID = ?";
		PreparedStatement getPs = metlimsConn.prepareStatement(pReportSql);
		String insertSql = 
				"INSERT INTO LIMS_ANALYSIS_REPORT (REPORT_ID, REPORT_TYPE, REPORT_FILE, REPORT_DESCRIPTION,  " +
				"DATE_CREATED, LOADED_BY, EXPERIMENT_ID, FILE_NAME, FILE_TYPE, ASSAY_ID, DELETED) " +
				"VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ";
		PreparedStatement putPs = idTrackerConn.prepareStatement(insertSql);
		for (String reportId : reportIds) {

			getPs.setString(1, reportId);
			ResultSet rs = getPs.executeQuery();
			while (rs.next()) {

				putPs.setString(1, reportId);
				putPs.setString(2, rs.getString("REPORT_TYPE"));
				putPs.setString(4, rs.getString("REPORT_DESCRIPTION"));
				putPs.setDate(5, rs.getDate("DATE_CREATED"));				
				putPs.setString(6, rs.getString("LOADED_BY"));
				putPs.setString(7, rs.getString("EXPERIMENT_ID"));
				putPs.setString(8, rs.getString("FILE_NAME"));
				putPs.setString(9, rs.getString("FILE_TYPE"));
				putPs.setString(10, rs.getString("ASSAY_ID"));
				putPs.setString(11, rs.getString("DELETED"));
				
//				TODO make this Postgres-compatible ?
				Blob blob = rs.getBlob("REPORT_FILE");
				BufferedInputStream is = new BufferedInputStream(blob.getBinaryStream());				
				putPs.setBinaryStream(3, is, blob.length());

				putPs.executeUpdate();
				is.close();
				blob.free();
			}
			rs.close();
		}
		getPs.close();
		putPs.close();
	}

	private Map<String,String>getExtensionMimeMap(Connection idTrackerConn) throws Exception {

		Map<String,String>extensionMimeMap = new TreeMap<String,String>();
		String query =
				"SELECT FILE_EXTENSION, MIME_TYPE FROM FILE_FORMAT";

		PreparedStatement ps = idTrackerConn.prepareStatement(query);
		ResultSet rs = ps.executeQuery();
		while(rs.next())
			extensionMimeMap.put(
					rs.getString("FILE_EXTENSION"), 
					rs.getString("MIME_TYPE"));
		rs.close();
		ps.close();
		return extensionMimeMap;
	}
}












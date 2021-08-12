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

package edu.umich.med.mrc2.datoolbox.database.lims;

import java.util.Collection;
import java.util.TreeSet;

import edu.umich.med.mrc2.datoolbox.data.Assay;
import edu.umich.med.mrc2.datoolbox.data.InstrumentPlatform;
import edu.umich.med.mrc2.datoolbox.data.compare.LIMSExperimentComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortDirection;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.lims.ChromatographicSeparationType;
import edu.umich.med.mrc2.datoolbox.data.lims.IdTrackerOrganization;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSClient;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSExperiment;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSInstrument;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSOrganization;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSProject;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSUser;
import edu.umich.med.mrc2.datoolbox.database.idt.AcquisitionMethodUtils;
import edu.umich.med.mrc2.datoolbox.database.idt.AssayDatabaseUtils;
import edu.umich.med.mrc2.datoolbox.database.idt.UserUtils;

public class LIMSDataCash {

	private static Collection<LIMSUser>users = 
			new TreeSet<LIMSUser>();
	private static Collection<IdTrackerOrganization>organizations = 
			new TreeSet<IdTrackerOrganization>();
	private static Collection<LIMSOrganization>limsOrganizations = 
			new TreeSet<LIMSOrganization>();
	private static Collection<LIMSClient>limsClients = 
			new TreeSet<LIMSClient>();	
	private static Collection<LIMSProject>projects = 
			new TreeSet<LIMSProject>();
	private static Collection<LIMSExperiment>experiments =
			new TreeSet<LIMSExperiment>(new LIMSExperimentComparator(SortProperty.ID, SortDirection.DESC));
	private static Collection<Assay>assays = 
			new TreeSet<Assay>();
	private static Collection<InstrumentPlatform>instrumentPlatforms = 
			new TreeSet<InstrumentPlatform>();
	public static Collection<ChromatographicSeparationType>chromatographicSeparationTypes = 
			new TreeSet<ChromatographicSeparationType>();
	public static Collection<LIMSInstrument>analyticalInstruments = 
			new TreeSet<LIMSInstrument>();

	public static void refreshUserList() {
		users.clear();
		getUsers();
	}

	public static void refreshOrganizationList() {
		organizations.clear();
		getOrganizations();
	}
	
	public static void refreshLimsClientList() {
		limsClients.clear();
		getLimsClients();
	}
	
	public static void refreshLimsOrganizationList() {
		limsOrganizations.clear();
		getLimsOrganizations();
	}

	public static void refreshProjectList() {
		projects.clear();
		getProjects();
	}

	public static void refreshExperimentList() {
		experiments.clear();
		getExperiments();
	}

	public static void refreshAssayList() {
		assays.clear();
		getAssays();
	}

	public static void refreshInstrumentPlatformsList() {
		instrumentPlatforms.clear();
		getInstrumentPlatforms();
	}
	
	public static void refreshChromatographicSeparationTypeList() {
		chromatographicSeparationTypes.clear();
		getChromatographicSeparationTypes();
	}
	
	public static void refreshAnalyticalInstrumentList() {
		users.clear();
		getAnalyticalInstruments();
	}
	
	public static Collection<LIMSInstrument> getAnalyticalInstruments() {

		if(analyticalInstruments == null)			
			analyticalInstruments = new TreeSet<LIMSInstrument>();
		
		if(analyticalInstruments.isEmpty()) {
			try {
				analyticalInstruments.addAll(
						AcquisitionMethodUtils.getInstrumentList());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return analyticalInstruments;
	}
	
	public static LIMSInstrument getAnalyticalInstrumentById(String id) {
		return  getAnalyticalInstruments().stream().
				filter(i -> i.getInstrumentId().equals(id)).
				findFirst().orElse(null);
	}

	public static Collection<ChromatographicSeparationType> 
			getChromatographicSeparationTypes() {
		
		if(chromatographicSeparationTypes == null)			
			chromatographicSeparationTypes = new TreeSet<ChromatographicSeparationType>();
		
		if(chromatographicSeparationTypes.isEmpty()) {
			try {
				chromatographicSeparationTypes.addAll(
						AcquisitionMethodUtils.getChromatographicSeparationTypes());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return chromatographicSeparationTypes;
	}
	
	public static ChromatographicSeparationType getChromatographicSeparationTypeById(String id) {
		return getChromatographicSeparationTypes().stream().
				filter(s -> s.getId().equals(id)).findFirst().orElse(null);
	}

	public static Collection<InstrumentPlatform> getInstrumentPlatforms() {

		if(instrumentPlatforms == null)			
			instrumentPlatforms = new TreeSet<InstrumentPlatform>();
		
		if(instrumentPlatforms.isEmpty()) {
			try {
				instrumentPlatforms.addAll(LIMSUtils.getInstrumentPlatformList());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return instrumentPlatforms;
	}
	
	public static InstrumentPlatform getInstrumentPlatformById(String id) {
		return getInstrumentPlatforms().stream().
				filter(p -> p.getId().equals(id)).findFirst().orElse(null);
	}
	
	public static InstrumentPlatform getInstrumentPlatformByManufacturer(String manufacturer) {
		return getInstrumentPlatforms().stream().
				filter(p -> p.getId().equalsIgnoreCase(manufacturer)).findFirst().orElse(null);
	}

	/**
	 * @return the projects
	 */
	public static Collection<LIMSProject> getProjects() {

		if(projects == null)
			projects = new TreeSet<LIMSProject>();

		if(projects.isEmpty()) {
			try {
				projects.addAll(LIMSUtils.getLimsProjectList());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return projects;
	}

	/**
	 * @return the users
	 */
	public static Collection<LIMSUser> getUsers() {

		if(users == null)
			users = new TreeSet<LIMSUser>();

		if(users.isEmpty()) {
			try {
				users.addAll(UserUtils.getCompleteUserList());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return users;
	}

	/**
	 * @return the organizations
	 */
	public static Collection<IdTrackerOrganization> getOrganizations() {

		if(organizations == null)
			organizations = new TreeSet<IdTrackerOrganization>();

		if(organizations.isEmpty()) {
			try {
				organizations.addAll(LIMSUtils.getOrganizationList());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return organizations;
	}
	
	public static Collection<LIMSOrganization> getLimsOrganizations() {
		
		if(limsOrganizations == null)
			limsOrganizations = new TreeSet<LIMSOrganization>();

		if(limsOrganizations.isEmpty()) {
			try {
				limsOrganizations.addAll(LIMSUtils.getLimsOrganizationList());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return limsOrganizations;
	}
	
	public static LIMSOrganization getLIMSOrganizationById(String id) {
		return  getLimsOrganizations().stream().
				filter(o -> o.getId().equals(id)).findFirst().orElse(null);
	}
	
	public static Collection<LIMSClient> getLimsClients() {
		
		if(limsClients == null)
			limsClients = new TreeSet<LIMSClient>();

		if(limsClients.isEmpty()) {
			try {
				limsClients.addAll(LIMSUtils.getClientList());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return limsClients;		
	}
	
	public static LIMSClient getLIMSClientById(String id) {
		return  getLimsClients().stream().
				filter(o -> o.getId().equals(id)).findFirst().orElse(null);
	}
	
	public static LIMSClient getLIMSClientForUser(LIMSUser user) {
		return  getLimsClients().stream().
				filter(o -> (o.getPrincipalInvestigator().equals(user) || 
					o.getContactPerson().equals(user))).sorted().findFirst().orElse(null);
	}

	/**
	 * @return the experiments
	 */
	public static Collection<LIMSExperiment> getExperiments() {

		if(experiments == null)
			experiments = new TreeSet<LIMSExperiment>(
					new LIMSExperimentComparator(SortProperty.ID, SortDirection.DESC));

		if(experiments.isEmpty()) {
			try {
				experiments.addAll(LIMSUtils.getExperimentList("EX00256"));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return experiments;
	}

	public static Collection<Assay>getAssays(){

		if(assays == null)
			assays = new TreeSet<Assay>();

		if(assays.isEmpty()) {
			try {
				assays.addAll(AssayDatabaseUtils.getLimsAssayList());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return assays;
	}

	public static LIMSProject getProjectById(String projectId) {

		return getProjects().stream().
				filter(p -> p.getId().equals(projectId)).
				findFirst().orElse(null);
	}
	
	public static LIMSExperiment getExperimentById(String experimentId) {

		return getExperiments().stream().
				filter(e -> e.getId().equals(experimentId)).
				findFirst().orElse(null);
	}

	public static LIMSUser getUserById(String userId) {

		return getUsers().stream().
				filter(e -> e.getId().equals(userId)).
				findFirst().orElse(null);
	}

	public static Assay getAssayById(String assayId) {

		return getAssays().stream().
				filter(e -> e.getId().equals(assayId)).
				findFirst().orElse(null);
	}
	
	public static IdTrackerOrganization getOrganizationById(String organizationId) {
		return getOrganizations().stream().
				filter(o -> o.getId().contentEquals(organizationId)).
				findFirst().orElse(null);
	}
}





























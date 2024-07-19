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

package edu.umich.med.mrc2.datoolbox.database.idt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;

import edu.umich.med.mrc2.datoolbox.data.CompoundLibrary;
import edu.umich.med.mrc2.datoolbox.data.IonizationType;
import edu.umich.med.mrc2.datoolbox.data.MSFeatureIdentificationFollowupStep;
import edu.umich.med.mrc2.datoolbox.data.MSFeatureIdentificationLevel;
import edu.umich.med.mrc2.datoolbox.data.MSMSDecoyGenerationMethod;
import edu.umich.med.mrc2.datoolbox.data.MassAnalyzerType;
import edu.umich.med.mrc2.datoolbox.data.MsType;
import edu.umich.med.mrc2.datoolbox.data.NISTPepSearchParameterObject;
import edu.umich.med.mrc2.datoolbox.data.ReferenceMsMsLibrary;
import edu.umich.med.mrc2.datoolbox.data.StandardFeatureAnnotation;
import edu.umich.med.mrc2.datoolbox.data.StockSample;
import edu.umich.med.mrc2.datoolbox.data.compare.LIMSExperimentComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.ReferenceMsMsLibraryComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortDirection;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.enums.Polarity;
import edu.umich.med.mrc2.datoolbox.data.enums.UserAffiliation;
import edu.umich.med.mrc2.datoolbox.data.lims.ChromatographicGradient;
import edu.umich.med.mrc2.datoolbox.data.lims.ChromatographicSeparationType;
import edu.umich.med.mrc2.datoolbox.data.lims.DataAcquisitionMethod;
import edu.umich.med.mrc2.datoolbox.data.lims.DataExtractionMethod;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.data.lims.DataProcessingSoftware;
import edu.umich.med.mrc2.datoolbox.data.lims.IdTrackerOrganization;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSChromatographicColumn;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSExperiment;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSInstrument;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSProject;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSProtocol;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSSamplePreparation;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSSampleType;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSUser;
import edu.umich.med.mrc2.datoolbox.data.lims.Manufacturer;
import edu.umich.med.mrc2.datoolbox.data.lims.MobilePhase;
import edu.umich.med.mrc2.datoolbox.data.lims.SopCategory;
import edu.umich.med.mrc2.datoolbox.database.lims.LIMSUtils;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.rawdata.MSMSExtractionParameterSet;

public class IDTDataCache {

	private static Collection<LIMSUser> users = 
			new TreeSet<LIMSUser>();
	private static Collection<IdTrackerOrganization> organizations = 
			new TreeSet<IdTrackerOrganization>();
	private static Collection<LIMSProject> projects = 
			new TreeSet<LIMSProject>();
	private static Collection<LIMSExperiment> experiments = 
			new TreeSet<LIMSExperiment>(
			new LIMSExperimentComparator(SortProperty.ID, SortDirection.DESC));
	private static Collection<StockSample> stockSamples = 
			new TreeSet<StockSample>();
	private static Map<LIMSExperiment, Collection<StockSample>> experimentStockSampleMap =
			new TreeMap<LIMSExperiment, Collection<StockSample>>();
	private static Collection<LIMSChromatographicColumn> chromatographicColumns = 
			new TreeSet<LIMSChromatographicColumn>();
	private static Collection<DataAcquisitionMethod> acquisitionMethods = 
			new TreeSet<DataAcquisitionMethod>();
	private static Collection<DataExtractionMethod> dataExtractionMethods = 
			new TreeSet<DataExtractionMethod>();
	private static Collection<ChromatographicSeparationType> chromatographicSeparationTypes = 
			new TreeSet<ChromatographicSeparationType>();
	private static Collection<Manufacturer> manufacturers = 
			new TreeSet<Manufacturer>();	
	private static Collection<DataProcessingSoftware>softwareList = 
			new TreeSet<DataProcessingSoftware>();
	private static Collection<IonizationType>ionizationTypes = 
			new TreeSet<IonizationType>();
	private static Collection<MassAnalyzerType>massAnalyzerTypes = 
			new TreeSet<MassAnalyzerType>();
	private static Collection<MsType>msTypes = 
			new TreeSet<MsType>();
	private static Collection<SopCategory>sopCategories = 
			new TreeSet<SopCategory>();
	private static Collection<LIMSProtocol>protocols = 
			new TreeSet<LIMSProtocol>();
	private static Collection<LIMSSamplePreparation>samplePreps = 
			new TreeSet<LIMSSamplePreparation>();
	private static Map<LIMSExperiment, Collection<LIMSSamplePreparation>> experimentSamplePrepMap =
			new TreeMap<LIMSExperiment, Collection<LIMSSamplePreparation>>();	
	private static Map<LIMSSamplePreparation, Collection<DataPipeline>> samplePrepDataPipelineMap = 
			new TreeMap<LIMSSamplePreparation, Collection<DataPipeline>>();	
	private static Collection<String>sampleQuantityUnits = 
			new TreeSet<String>();
	private static Collection<String>resultValueUnits = 
			new TreeSet<String>();
	private static Collection<LIMSInstrument>instruments = 
			new TreeSet<LIMSInstrument>();
	private static Map<LIMSExperiment, LIMSInstrument>experimentInstrumentMap = 
			new TreeMap<LIMSExperiment, LIMSInstrument>();
	private static Collection<ReferenceMsMsLibrary>referenceMsMsLibraries = 
			new TreeSet<ReferenceMsMsLibrary>();
	private static Map<String, Boolean> decoyLibraryMap = null;
	private static Collection<NISTPepSearchParameterObject>pepSearchParameters = 
			new TreeSet<NISTPepSearchParameterObject>();
	private static Collection<MobilePhase>mobilePhaseList = 
			new TreeSet<MobilePhase>();
	private static Collection<MSFeatureIdentificationLevel> msFeatureIdentificationLevelList = 
			new TreeSet<MSFeatureIdentificationLevel>();
	private static Collection<MSFeatureIdentificationFollowupStep> msFeatureIdentificationFollowupStepList = 
			new TreeSet<MSFeatureIdentificationFollowupStep>();
	private static Collection<StandardFeatureAnnotation> standardFeatureAnnotationList = 
			new TreeSet<StandardFeatureAnnotation>();	
	private static Collection<MSMSDecoyGenerationMethod>msmsDecoyGenerationMethods = 
			new TreeSet<MSMSDecoyGenerationMethod>();	
	private static Collection<LIMSSampleType>sampleTypes = 
			new TreeSet<LIMSSampleType>();
	private static Collection<Double>collisionEnergies = 
			new TreeSet<Double>();	
	private static Collection<MSMSExtractionParameterSet>msmsExtractionParameters = 
			new HashSet<MSMSExtractionParameterSet>();
	private static Map<LIMSExperiment, Collection<Polarity>> experimentPolarityMap = 
			new TreeMap<LIMSExperiment, Collection<Polarity>>();
	private static Collection<CompoundLibrary>msRtLibraryList = 
			new TreeSet<CompoundLibrary>();	
	private static Map<String, Integer>msRtLibraryEntryCounts = 
			new TreeMap<String, Integer>();	
	private static Collection<ChromatographicGradient>chromatographicGradientList = 
			new HashSet<ChromatographicGradient>();	
	
	public static void refreshChromatographicGradientList() {
		chromatographicGradientList.clear();
		getChromatographicGradientList();
	}
	
	public static void refreshExperimentPolarityMap() {
		experimentPolarityMap.clear();
		getExperimentPolarityMap();
	}
	
	public static void refreshCollisionEnergies() {
		collisionEnergies.clear();
		getCollisionEnergiesList();
	}
	
	public static void refreshSampleTypes() {
		sampleTypes.clear();
		getSampleTypeList();
	}
	
	public static void refreshMSMSDecoyGenerationMethodList() {
		msmsDecoyGenerationMethods.clear();
		getMsmsDecoyGenerationMethodList();
	}

	public static void refreshStandardFeatureAnnotationList() {
		standardFeatureAnnotationList.clear();
		getStandardFeatureAnnotationList();
	}
	
	public static void refreshMsFeatureIdentificationFollowupStepList() {
		msFeatureIdentificationFollowupStepList.clear();
		getMsFeatureIdentificationFollowupStepList();
	}
	
	public static void refreshMsFeatureIdentificationLevelList() {
		msFeatureIdentificationLevelList.clear();
		getMsFeatureIdentificationLevelList();
	}
	
	public static void refreshMobilePhaseList() {
		mobilePhaseList.clear();
		getMobilePhaseList();
		//Collection<MobilePhase>mobilePhaseList;
	}

	public static void refreshReferenceMsMsLibraryList() {
		referenceMsMsLibraries.clear();
		getReferenceMsMsLibraryList();
	}

	public static void refreshInstrumentList() {
		instruments.clear();
		getInstrumentList();
	}
	
	public static void refreshExperimentInstrumentMap() {
		
		experimentInstrumentMap.clear();
		getExperimentInstrumentMap();
	}

	public static void refreshUnits() {
		sampleQuantityUnits.clear();
		resultValueUnits.clear();
		getSampleQuantityUnits();
		getResultValuetUnits();
	}

	public static void refreshUserList() {
		users.clear();
		getUsers();
	}

	public static void refreshOrganizationList() {
		organizations.clear();
		getOrganizations();
	}

	public static void refreshProjectList() {
		projects.clear();
		getProjects();
	}

	public static void refreshExperimentList() {
		experiments.clear();
		getExperiments();
	}

	public static void refreshStockSampleList() {
		stockSamples.clear();
		getStockSamples();
	}

	public static void refreshChromatographicColumnList() {
		chromatographicColumns.clear();
		getChromatographicColumns();
	}

	public static void refreshAcquisitionMethodList() {
		acquisitionMethods.clear();
		getAcquisitionMethods();
	}

	public static void refreshDataExtractionMethodList() {
		dataExtractionMethods.clear();
		getDataExtractionMethods();
	}

	public static void refreshChromatographicSeparationTypes() {
		chromatographicSeparationTypes.clear();
		getChromatographicSeparationTypes();
	}

	public static void refreshManufacturers() {
		manufacturers.clear();
		getManufacturers();
	}
	
	public static void refreshSoftwareList() {
		softwareList.clear();
		getSoftwareList();
	}

	public static void refreshIonizationTypes(){
		ionizationTypes.clear();
		getIonizationTypes();
	}

	public static void refreshMassAnalyzers(){
		massAnalyzerTypes.clear();
		getMassAnalyzerTypes();
	}

	public static void refreshMsTypes(){
		msTypes.clear();
		getMsTypes();
	}

	public static void refreshProtocols() {
		protocols.clear();
		getProtocols();
	}


	public static void refreshSamplePreps() {
		samplePreps.clear();
		getSamplePreps();
	}

	public static void refreshSopCategories() {
		sopCategories.clear();
		getSopCategories();
	}

	public static void refreshExperimentSamplePrepMap() {
		experimentSamplePrepMap.clear();
		getExperimentSamplePrepMap();
	}
	
	public static void refreshNISTPepSearchParameters() {
		pepSearchParameters.clear();
		getNISTPepSearchParameterObjecs();
	}
	
	public static void refreshExperimentStockSampleMap() {
		experimentStockSampleMap.clear();
		getExperimentStockSampleMap();
	}

	public static Map<LIMSExperiment, Collection<StockSample>> getExperimentStockSampleMap() {
		// TODO Auto-generated method stub
		if(experimentStockSampleMap == null)
			experimentStockSampleMap =
				new TreeMap<LIMSExperiment, Collection<StockSample>>();

		if(experimentStockSampleMap.isEmpty()) {
			try {
				experimentStockSampleMap.putAll(IDTUtils.getExperimentStockSampleMap());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return experimentStockSampleMap;
	}
	
	public static Collection<StockSample>getStockSamplesForExperiment(LIMSExperiment e){
		return getExperimentStockSampleMap().get(e);
	}
	
	public static Collection<LIMSExperiment>getLIMSExperimentsForTrackerExperiment(LIMSExperiment e){
		
		Collection<StockSample>ss = getStockSamplesForExperiment(e);
		if(ss == null)
			return null;
		else {
			return ss.stream().
					filter(s -> Objects.nonNull(s.getLimsExperiment())).
					map(s -> s.getLimsExperiment()).
					distinct().sorted().collect(Collectors.toList());
		}
	}

	public static Map<LIMSExperiment, Collection<LIMSSamplePreparation>> getExperimentSamplePrepMap() {

		if(experimentSamplePrepMap == null)
			experimentSamplePrepMap =
				new TreeMap<LIMSExperiment, Collection<LIMSSamplePreparation>>();

		if(experimentSamplePrepMap.isEmpty()) {
			try {
				experimentSamplePrepMap.putAll(IDTUtils.getExperimentSamplePrepMap());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return experimentSamplePrepMap;
	}
	
	
	public static Collection<DataPipeline> getDataPipelinesForExperiment(LIMSExperiment experiment){
		
		Collection<DataPipeline>pipelines = new TreeSet<DataPipeline>();
		Collection<LIMSSamplePreparation>preps = 
				getExperimentSamplePrepMap().get(experiment);
		
		if(preps == null || preps.isEmpty())
			return pipelines;
		
		for(LIMSSamplePreparation prep : preps) {
			
			Collection<DataPipeline> plList = getSamplePrepDataPipelineMap().get(prep);
			if(plList == null || plList.isEmpty())
				continue;
			
			pipelines.addAll(plList);
		}		
		return pipelines;
	}
	
//
//	public static void refreshSamplePrepAcquisitionMethodMap() {
//		samplePrepAcquisitionMethodMap.clear();
//		getSamplePrepAcquisitionMethodMap();
//	}
//
//	public static Map<LIMSSamplePreparation, Collection<DataAcquisitionMethod>> getSamplePrepAcquisitionMethodMap() {
//
//		if(samplePrepAcquisitionMethodMap == null)
//			samplePrepAcquisitionMethodMap =
//				new TreeMap<LIMSSamplePreparation, Collection<DataAcquisitionMethod>>();
//
//		if(samplePrepAcquisitionMethodMap.isEmpty()) {
//			try {
//				samplePrepAcquisitionMethodMap.putAll(IDTUtils.getSamplePrepAcquisitionMethodMap());
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//		return samplePrepAcquisitionMethodMap;
//	}
//
//	public static void refreshAcquisitionDataExtractionMethodMap() {
//		acquisitionDataExtractionMethodMap.clear();
//		getAcquisitionDataExtractionMethodMap();
//	}
//
//	public static Map<DataAcquisitionMethod, Collection<DataExtractionMethod>> getAcquisitionDataExtractionMethodMap() {
//
//		if(acquisitionDataExtractionMethodMap == null)
//			acquisitionDataExtractionMethodMap =
//				new TreeMap<DataAcquisitionMethod, Collection<DataExtractionMethod>>();
//
//		if(acquisitionDataExtractionMethodMap.isEmpty()) {
//			try {
//				acquisitionDataExtractionMethodMap.putAll(IDTUtils.getAcquisitionDataExtractionMethodMap());
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//		return acquisitionDataExtractionMethodMap;
//	}
	
	public static void refreshSamplePrepDataPipelineMap() {
		
		samplePrepDataPipelineMap.clear();
		getSamplePrepDataPipelineMap();
	}
	
	public static Map<LIMSSamplePreparation, Collection<DataPipeline>> getSamplePrepDataPipelineMap(){
		
		if(samplePrepDataPipelineMap == null)
			samplePrepDataPipelineMap =
				new TreeMap<LIMSSamplePreparation, Collection<DataPipeline>>();

		if(samplePrepDataPipelineMap.isEmpty()) {
			try {
				samplePrepDataPipelineMap.putAll(IDTUtils.getSamplePrepDataPipelineMap());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return samplePrepDataPipelineMap;
	}
	
	public static Collection<DataPipeline> getDataPipelinesForSamplePrep(LIMSSamplePreparation prep){
		return getSamplePrepDataPipelineMap().get(prep);
	}

	/**
	 * @return the users
	 */
	public static Collection<LIMSUser> getUsers() {

		if(users == null)
			users = new TreeSet<LIMSUser>();

		if(users.isEmpty()) {
			try {
				users.addAll(UserUtils.getUserList());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return users;
	}

	public static Collection<LIMSUser> getUsers(UserAffiliation affiliation) {

		Collection<LIMSUser> allUsers = getUsers();
		if(affiliation == null)			
			return users;
		else
			return allUsers.stream().
					filter(u -> u.getAffiliation().equals(affiliation.name())).
					collect(Collectors.toCollection(TreeSet::new));
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

	/**
	 * @return the projects
	 */
	public static Collection<LIMSProject> getProjects() {

		if(projects == null)
			projects = new TreeSet<LIMSProject>();

		if(projects.isEmpty()) {
			try {
				projects.addAll(IDTUtils.getProjectList());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return projects;
	}
	
	public static LIMSProject getProjectById(String projectId) {
		
		return getProjects().stream().
				filter(p -> p.getId().equals(projectId)).
				findFirst().orElse(null);
	}

	/**
	 * @return the experiments
	 */
	public static Collection<LIMSExperiment> getExperiments() {

		if(experiments == null)
			experiments = new TreeSet<LIMSExperiment>(new LIMSExperimentComparator(SortProperty.ID, SortDirection.DESC));

		if(experiments.isEmpty()) {
			try {
				experiments.addAll(IDTUtils.getExperimentList());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return experiments;
	}

	/**
	 * @return the experiments
	 */
	public static Collection<StockSample> getStockSamples() {

		if(stockSamples == null)
			stockSamples = new TreeSet<StockSample>();

		if(stockSamples.isEmpty()) {
			try {
				stockSamples.addAll(IDTUtils.getStockSampleList());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return stockSamples;
	}

	public static Collection<ChromatographicGradient>getChromatographicGradientList(){
		
		if(chromatographicGradientList == null)
			chromatographicGradientList = new HashSet<ChromatographicGradient>();	
		
		if(chromatographicGradientList.isEmpty()) {
			
			try {
				chromatographicGradientList.addAll(
						ChromatographyDatabaseUtils.getChromatographicGradientList());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}		
		return chromatographicGradientList;
	}
	
	public static ChromatographicGradient getChromatographicGradientById(String id) {
		
		return getChromatographicGradientList().
				stream().filter(g -> g.getId().equals(id)).
				findFirst().orElse(null);
	}

	public static Collection<LIMSChromatographicColumn> getChromatographicColumns() {

		if(chromatographicColumns == null)
			chromatographicColumns = new TreeSet<LIMSChromatographicColumn>();

		if(chromatographicColumns.isEmpty()) {
			try {
				chromatographicColumns.addAll(AcquisitionMethodUtils.getChromatographicColumnList());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return chromatographicColumns;
	}

	public static Collection<DataAcquisitionMethod> getAcquisitionMethods() {

		if(acquisitionMethods == null)
			acquisitionMethods = new TreeSet<DataAcquisitionMethod>();

		if(acquisitionMethods.isEmpty()) {
			try {
				acquisitionMethods.addAll(AcquisitionMethodUtils.getAcquisitionMethodList());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return acquisitionMethods;
	}

	public static Collection<DataExtractionMethod> getDataExtractionMethods() {

		if(dataExtractionMethods == null)
			dataExtractionMethods = new TreeSet<DataExtractionMethod>();

		if(dataExtractionMethods.isEmpty()) {
			try {
				dataExtractionMethods.addAll(IDTUtils.getDataExtractionMethodList());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return dataExtractionMethods;
	}

	public static Collection<ChromatographicSeparationType>getChromatographicSeparationTypes(){

		if(chromatographicSeparationTypes == null)
			chromatographicSeparationTypes = new TreeSet<ChromatographicSeparationType>();

		if(chromatographicSeparationTypes.isEmpty()) {
			try {
				chromatographicSeparationTypes.addAll(AcquisitionMethodUtils.getChromatographicSeparationTypes());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return chromatographicSeparationTypes;
	}

	public static Collection<Manufacturer> getManufacturers() {

		if(manufacturers == null)
			manufacturers = new TreeSet<Manufacturer>();

		if(manufacturers.isEmpty()) {			
			try {
				manufacturers.addAll(IDTUtils.getManufacturerList());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		}
		return manufacturers;
	}
	
	public static Manufacturer getManufacturerByName(String name) {
		return getManufacturers().stream().
				filter(m -> m.getName().equals(name)).
				findFirst().orElse(null);
	}
	
	public static Manufacturer getManufacturerById(String id) {
		return getManufacturers().stream().
				filter(m -> m.getId().equals(id)).
				findFirst().orElse(null);
	}
	
	public static Collection<DataProcessingSoftware>getSoftwareList(){
		
		if(softwareList == null)
			softwareList = new TreeSet<DataProcessingSoftware>();
		
		if(softwareList.isEmpty()) {
			try {
				softwareList.addAll(IDTUtils.getSoftwareList());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}		
		return softwareList;
	}
	
	public static DataProcessingSoftware getSoftwareById(String id) {
		return getSoftwareList().stream().
				filter(s -> s.getId().equals(id)).
				findFirst().orElse(null);
	}

	public static DataProcessingSoftware getSoftwareByName(String name) {
		return getSoftwareList().stream().
				filter(s -> s.getName().equals(name)).
				findFirst().orElse(null);
	}
	
	public static Collection<IonizationType> getIonizationTypes(){

		if(ionizationTypes == null)
			ionizationTypes = new TreeSet<IonizationType>();

		if(ionizationTypes.isEmpty()) {

			try {
				ionizationTypes.addAll(AcquisitionMethodUtils.getIonizationTypes());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return ionizationTypes;
	}

	public static Collection<MassAnalyzerType> getMassAnalyzerTypes(){

		if(massAnalyzerTypes == null)
			massAnalyzerTypes = new TreeSet<MassAnalyzerType>();

		if(massAnalyzerTypes.isEmpty()) {

			try {
				massAnalyzerTypes.addAll(AcquisitionMethodUtils.getMassAnalyzerTypes());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return massAnalyzerTypes;
	}

	public static Collection<MsType>getMsTypes(){

		if(msTypes == null)
			msTypes = new TreeSet<MsType>();

		if(msTypes.isEmpty()) {

			try {
				msTypes.addAll(AcquisitionMethodUtils.getMsTypes());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return msTypes;
	}

	public static Collection<LIMSProtocol> getProtocols() {

		if(protocols == null)
			protocols = new TreeSet<LIMSProtocol>();

		if(protocols.isEmpty()) {

			try {
				protocols = IDTUtils.getProtocols();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return protocols;
	}

	public static Collection<LIMSSamplePreparation> getSamplePreps() {

		if(samplePreps == null)
			samplePreps = new TreeSet<LIMSSamplePreparation>();

		if(samplePreps.isEmpty()) {

			try {
				samplePreps = IDTUtils.getSamplePreps();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return samplePreps;
	}

	public static Collection<SopCategory> getSopCategories() {

		if(sopCategories == null)
			sopCategories = new TreeSet<SopCategory>();

		if(sopCategories.isEmpty()) {
			try {
				sopCategories.addAll(IDTUtils.getSopCategories());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return sopCategories;
	}

	public static Collection<String>getResultValuetUnits(){

		if(resultValueUnits == null)
			resultValueUnits = new TreeSet<String>();

		if(resultValueUnits.isEmpty()) {
			try {
				resultValueUnits.addAll(IDTUtils.getResultValueUnits());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return resultValueUnits;
	}

	public static Collection<String> getSampleQuantityUnits() {

		if(sampleQuantityUnits == null)
			sampleQuantityUnits = new TreeSet<String>();

		if(sampleQuantityUnits.isEmpty()) {
			try {
				sampleQuantityUnits.addAll(IDTUtils.getSampleQuantityUnits());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return sampleQuantityUnits;
	}

	public static Collection<LIMSInstrument> getInstrumentList() {

		if(instruments == null)
			instruments = new TreeSet<LIMSInstrument>();

		if(instruments.isEmpty()) {
			try {
				instruments.addAll(AcquisitionMethodUtils.getInstrumentList());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return instruments;
	}
	
	public static Map<LIMSExperiment, LIMSInstrument> getExperimentInstrumentMap() {

		if(experimentInstrumentMap == null)
			experimentInstrumentMap = new TreeMap<LIMSExperiment, LIMSInstrument>();
		
		if(experimentInstrumentMap.isEmpty()) {
			try {
				experimentInstrumentMap.putAll(IDTUtils.getExperimentInstrumentMap());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return experimentInstrumentMap;
	}
	
	public static LIMSInstrument getInstrumentForExperiment(LIMSExperiment e) {
		return getExperimentInstrumentMap().get(e);
	}

	public static Collection<ReferenceMsMsLibrary> getReferenceMsMsLibraryList(){

		if(referenceMsMsLibraries == null)
			referenceMsMsLibraries = new TreeSet<ReferenceMsMsLibrary>();

		if(referenceMsMsLibraries.isEmpty()) {
			try {
				referenceMsMsLibraries.addAll(MSMSLibraryUtils.getReferenceMsMsLibraries());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return referenceMsMsLibraries;
	}
	
	public static Map<String,Boolean>getDecoyLibraryMap(){
		
		if(decoyLibraryMap == null || decoyLibraryMap.isEmpty()) {
			
			decoyLibraryMap = new TreeMap<String, Boolean>();
			getReferenceMsMsLibraryList().stream().
				forEach(e -> decoyLibraryMap.put(e.getUniqueId(), e.isDecoy()));
		}
		return decoyLibraryMap;
	}
	
	public static Collection<ReferenceMsMsLibrary> getPrimaryReferenceMsMsLibraryList(){
		return getReferenceMsMsLibraryList().stream().
				filter(l -> !l.isSubset()).
				sorted(new ReferenceMsMsLibraryComparator(SortProperty.Name)).
				collect(Collectors.toList());
	}
	
	public static Collection<MobilePhase>getMobilePhaseList(){
		
		if(mobilePhaseList == null)
			mobilePhaseList = new TreeSet<MobilePhase>();

		if(mobilePhaseList.isEmpty()) {
			try {
				mobilePhaseList.addAll(ChromatographyDatabaseUtils.getMobilePhaseList());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return mobilePhaseList;
	}
	
	public static MobilePhase getMobilePhaseById(String id) {
		
		return getMobilePhaseList().stream().
				filter(p -> p.getId().equals(id)).
				findFirst().orElse(null);
	}
	
	public static MobilePhase getMobilePhaseByNameOrSynonym(String query) {
		
		getMobilePhaseList();
		MobilePhase mp = mobilePhaseList.stream().
				filter(p -> p.getName().equalsIgnoreCase(query)).
				findFirst().orElse(null);
		if(mp != null)
			return mp;
		else {							
			for(MobilePhase p : mobilePhaseList) {
				
				if(!p.getSynonyms().isEmpty()) {
					
					for(String synonym : p.getSynonyms()) {
						
						if(synonym.equalsIgnoreCase(query))
							return p;
					}
				}
			}
			return null;
		}
	}
	
	public static Collection<MSFeatureIdentificationLevel>getMsFeatureIdentificationLevelList(){
		
		if(msFeatureIdentificationLevelList == null) 
			msFeatureIdentificationLevelList = new TreeSet<MSFeatureIdentificationLevel>();
		
		if(msFeatureIdentificationLevelList.isEmpty()) {
			try {
				msFeatureIdentificationLevelList.addAll(
						IdLevelUtils.getMSFeatureIdentificationLevelList());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return msFeatureIdentificationLevelList;
	}
	
	public static MSFeatureIdentificationLevel getTopMSFeatureIdentificationLevel() {		
		return getMsFeatureIdentificationLevelList().stream().sorted().findFirst().orElse(null);
	}
	
	public static Collection<MSFeatureIdentificationFollowupStep>getMsFeatureIdentificationFollowupStepList(){
		
		if(msFeatureIdentificationFollowupStepList == null) 
			msFeatureIdentificationFollowupStepList = new TreeSet<MSFeatureIdentificationFollowupStep>();
		
		if(msFeatureIdentificationFollowupStepList.isEmpty()) {
			try {
				msFeatureIdentificationFollowupStepList.addAll(
						IdFollowupUtils.getMSFeatureIdentificationFollowupStepList());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return msFeatureIdentificationFollowupStepList;
	}
	
	public static Collection<StandardFeatureAnnotation>getStandardFeatureAnnotationList(){
		
		if(standardFeatureAnnotationList == null) 
			standardFeatureAnnotationList = new TreeSet<StandardFeatureAnnotation>();
		
		if(standardFeatureAnnotationList.isEmpty()) {
			try {
				standardFeatureAnnotationList.addAll(
						StandardAnnotationUtils.getStandardFeatureAnnotationList());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return standardFeatureAnnotationList;
	}
	
	public static Collection<MSMSDecoyGenerationMethod>getMsmsDecoyGenerationMethodList() {
		
		if(msmsDecoyGenerationMethods == null)
			msmsDecoyGenerationMethods = new TreeSet<MSMSDecoyGenerationMethod>();
		
		if(msmsDecoyGenerationMethods.isEmpty()) {
			try {
				msmsDecoyGenerationMethods.addAll(
						MSMSDecoyUtils.getMSMSDecoyGenerationMethods());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return msmsDecoyGenerationMethods;
	}
	
	public static Collection<LIMSSampleType>getSampleTypeList(){
		
		if(sampleTypes == null)
			sampleTypes = new TreeSet<LIMSSampleType>();
		
		if(sampleTypes.isEmpty()) {
			try {
				sampleTypes.addAll(IDTUtils.getAvailableSampleTypes());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return sampleTypes;
	}
	
	public static Collection<Double> getCollisionEnergiesList(){
		
		if(collisionEnergies == null)
			collisionEnergies = new TreeSet<Double>();
		
		if(collisionEnergies.isEmpty()) {
			try {
				collisionEnergies.addAll(IDTUtils.getAvailableMsMsCollisionEnergies());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return collisionEnergies;
	}
	
	private static Map<LIMSExperiment, Collection<Polarity>>getExperimentPolarityMap() {
		
		if(experimentPolarityMap == null)
			experimentPolarityMap = new TreeMap<LIMSExperiment, Collection<Polarity>>();
		
		if(experimentPolarityMap.isEmpty()) {
			try {
				experimentPolarityMap.putAll(IDTUtils.getExperimentPolarityMap());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return experimentPolarityMap;
	}
	
	public static Collection<Polarity>getPolaritiesForExperiment(LIMSExperiment e){
		return getExperimentPolarityMap().get(e);
	}
	
	public static LIMSInstrument getInstrumentById(String instrumentId) {

		return getInstrumentList().stream().
				filter(e -> e.getInstrumentId().equals(instrumentId)).findFirst().orElse(null);
	}

	public static LIMSExperiment getExperimentById(String experimentId) {

		return getExperiments().stream().
				filter(e -> e.getId().equals(experimentId)).findFirst().orElse(null);
	}

	public static IdTrackerOrganization getOrganizationForUser(LIMSUser user) {

		return getOrganizations().stream().
				filter(o -> o.getId().equals(user.getOrganizationId())).findFirst().orElse(null);
	}
	
	public static IdTrackerOrganization getOrganizationByMetlimsClientId(String metlimsClientId) {

		return getOrganizations().stream().
				filter(o -> Objects.nonNull(o.getMetlimsClientId())).
				filter(o -> o.getMetlimsClientId().equals(metlimsClientId)).
				findFirst().orElse(null);
	}

	public static DataAcquisitionMethod getAcquisitionMethodById(String methodId) {

		return getAcquisitionMethods().stream().
				filter(e -> e.getId().equals(methodId)).findFirst().orElse(null);
	}
	
	public static DataAcquisitionMethod getAcquisitionMethodByName(String methodName) {

		return getAcquisitionMethods().stream().
				filter(e -> (e.getName().equals(methodName) 
						|| FilenameUtils.getBaseName(e.getName()).equalsIgnoreCase(methodName))).
				findFirst().orElse(null);
	}

	public static DataExtractionMethod getDataExtractionMethodById(String methodId) {

		return getDataExtractionMethods().stream().
				filter(e -> e.getId().equals(methodId)).findFirst().orElse(null);
	}
	
	public static DataExtractionMethod getDataExtractionMethodByName(String methodName) {

		return getDataExtractionMethods().stream().
				filter(e -> e.getName().equalsIgnoreCase(methodName)).findFirst().orElse(null);
	}
	
	public static DataExtractionMethod getDataExtractionMethodByMd5(String md5) {

		return getDataExtractionMethods().stream().
				filter(e -> Objects.nonNull(e.getMd5())).
				filter(e -> e.getMd5().equals(md5)).findFirst().orElse(null);
	}

	public static LIMSProtocol getProtocolById(String protocolId) {

		return getProtocols().stream().
				filter(e -> e.getSopId().equals(protocolId)).findFirst().orElse(null);
	}

	public static LIMSSamplePreparation getSamplePrepById(String spId) {

		return getSamplePreps().stream().
				filter(e -> e.getId().equals(spId)).findFirst().orElse(null);
	}

	public static LIMSUser getUserById(String userId) {

		return getUsers().stream().
				filter(e -> e.getId().equals(userId)).findFirst().orElse(null);
	}

	public static LIMSChromatographicColumn getColumnById(String columnId) {

		return getChromatographicColumns().stream().
				filter(c -> c.getColumnId().equals(columnId)).findFirst().orElse(null);
	}

	public static IonizationType getIonizationTypeById(String itId) {

		return getIonizationTypes().stream().
				filter(u -> u.getId().equals(itId)).findFirst().orElse(null);
	}

	public static MassAnalyzerType getMassAnalyzerTypeById(String maId) {

		return getMassAnalyzerTypes().stream().
				filter(u -> u.getId().equals(maId)).findFirst().orElse(null);
	}

	public static MsType getMsTypeById(String msTypeId) {

		return getMsTypes().stream().
				filter(u -> u.getId().equals(msTypeId)).findFirst().orElse(null);
	}

	public static SopCategory getSopSopCategoryById(String scId) {

		return getSopCategories().stream().
				filter(u -> u.getCategoryId().equals(scId)).findFirst().orElse(null);
	}

	public static ChromatographicSeparationType getChromatographicSeparationTypeById(String stId) {

		return getChromatographicSeparationTypes().stream().
				filter(u -> u.getId().equals(stId)).findFirst().orElse(null);
	}

	public static StockSample getStockSampleById(String ssId) {

		return getStockSamples().stream().
				filter(s -> s.getSampleId().equals(ssId)).findFirst().orElse(null);
	}

	public static Collection<DataAcquisitionMethod>
		getAcquisitionMethodsForExperiment(LIMSExperiment experiment){

		Collection<DataAcquisitionMethod>acqMethods = new ArrayList<DataAcquisitionMethod>();
		Collection<LIMSSamplePreparation> preps =
				IDTDataCache.getExperimentSamplePrepMap().get(experiment);

		if(preps == null || preps.isEmpty())
			return acqMethods;
		
		return getSamplePrepDataPipelineMap().entrySet().
				stream().filter(p -> preps.contains(p.getKey())).
				flatMap(p -> p.getValue().stream()).
				map(p -> p.getAcquisitionMethod()).collect(Collectors.toSet());
	}

	public static ReferenceMsMsLibrary getReferenceMsMsLibraryByCode(String code) {

		return getReferenceMsMsLibraryList().stream().
				filter(l -> l.getSearchOutputCode().equals(code)).findFirst().orElse(null);
	}

	public static ReferenceMsMsLibrary getReferenceMsMsLibraryById(String id) {

		return getReferenceMsMsLibraryList().stream().
				filter(l -> l.getUniqueId().equals(id)).findFirst().orElse(null);
	}
	
	public static String getReferenceMsMsLibraryNameById(String id) {

		ReferenceMsMsLibrary library =  getReferenceMsMsLibraryList().stream().
				filter(l -> l.getUniqueId().equals(id)).findFirst().orElse(null);
		if(library != null)
			return library.getName();
		else
			return null;
	}

	public static ReferenceMsMsLibrary getReferenceMsMsLibraryByPrimaryLibraryId(String primaryId) {
		return getReferenceMsMsLibraryList().stream().
				filter(l -> !l.isSubset()).
				filter(l -> l.getPrimaryLibraryId().equals(primaryId)).findFirst().orElse(null);
	}
	
	public static ReferenceMsMsLibrary getReferenceMsMsLibraryByName(String name) {
		
		return getReferenceMsMsLibraryList().stream().
				filter(l -> l.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
	}
	
	public static Collection<ReferenceMsMsLibrary> getDecoyLibraries() {
		
		return getReferenceMsMsLibraryList().stream().
				filter(l -> l.isDecoy()).sorted().collect(Collectors.toList());
	}
	
	public static Collection<NISTPepSearchParameterObject> getNISTPepSearchParameterObjecs() {
		
		if(pepSearchParameters == null)
			pepSearchParameters = new TreeSet<NISTPepSearchParameterObject>();

		if(pepSearchParameters.isEmpty()) {
			try {
				pepSearchParameters.addAll(DatabaseIdentificationUtils.getNISTPepSearchParameterObjects());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}		
		return pepSearchParameters;
	}
	
	public static NISTPepSearchParameterObject getNISTPepSearchParameterObjectById(String id) {
		
		if(pepSearchParameters == null)
			pepSearchParameters = new TreeSet<NISTPepSearchParameterObject>();
		
		return getNISTPepSearchParameterObjecs().
				stream().filter(p -> p.getId().equals(id)).
				findFirst().orElse(null);
	}	
	
	public static MSFeatureIdentificationLevel getMSFeatureIdentificationLevelById(String id) {
		return getMsFeatureIdentificationLevelList().stream().
				filter(s -> s.getId().equals(id)).findFirst().orElse(null);
	}
	
	public static MSFeatureIdentificationLevel getMSFeatureIdentificationLevelByName(String name) {
		return getMsFeatureIdentificationLevelList().stream().
				filter(s -> s.getName().equals(name)).findFirst().orElse(null);
	}
	
	public static MSFeatureIdentificationFollowupStep getMSFeatureIdentificationFollowupStepById(String id) {
		return getMsFeatureIdentificationFollowupStepList().stream().
				filter(s -> s.getId().equals(id)).findFirst().orElse(null);
	}
	
	public static StandardFeatureAnnotation getStandardFeatureAnnotationById(String id) {
		return getStandardFeatureAnnotationList().stream().
				filter(s -> s.getId().equals(id)).findFirst().orElse(null);
	}
	
	public static LIMSExperiment getExperimentForSamplePrep(LIMSSamplePreparation prep) {
		
		for(Entry<LIMSExperiment, Collection<LIMSSamplePreparation>> entry : getExperimentSamplePrepMap().entrySet()) {
			
			if(entry.getValue().contains(prep))
				return entry.getKey();			
		}		
		return null;
	}
	
	public static void refreshMSMSExtractionParameters() {
		msmsExtractionParameters.clear();
		getMsmsExtractionParameters();
	}
	
	public static Collection<MSMSExtractionParameterSet> getMsmsExtractionParameters() {
		
		if(msmsExtractionParameters == null)
			msmsExtractionParameters =  new HashSet<MSMSExtractionParameterSet>();
		
		if(msmsExtractionParameters.isEmpty()) {
			try {
				msmsExtractionParameters.addAll(IDTUtils.getMSMSExtractionParameters());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return msmsExtractionParameters;
	}
	
	public static MSMSExtractionParameterSet getMSMSExtractionParameterSetById(String id) {
		
		return getMsmsExtractionParameters().stream().
				filter(p -> p.getId().equals(id)).findFirst().orElse(null);
	}

	public static Collection<CompoundLibrary> getMsRtLibraryList() {
		
		if(msRtLibraryList == null)
			msRtLibraryList =  new TreeSet<CompoundLibrary>();
		
		if(msRtLibraryList.isEmpty()) {
			try {
				msRtLibraryList.addAll(MSRTLibraryUtils.getAllLibraries());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return msRtLibraryList;
	}

	public static Map<String, Integer> getMsRtLibraryEntryCount() {
		
		if(msRtLibraryEntryCounts == null)
			msRtLibraryEntryCounts =  new TreeMap<String, Integer>();
		
		if(msRtLibraryEntryCounts.isEmpty()) {
			try {
				msRtLibraryEntryCounts.putAll(MSRTLibraryUtils.getLibraryEntryCount());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return msRtLibraryEntryCounts;
	}
	
	public static void refreshMsRtLibraryList() {
		
		msRtLibraryList.clear();
		getMsRtLibraryList();
		msRtLibraryEntryCounts.clear();
		getMsRtLibraryEntryCount();
	}
	
	public static CompoundLibrary getMSRTLibraryById(String id) {
		
		CompoundLibrary lib = MRC2ToolBoxCore.getActiveMsLibraries().stream().
				filter(l -> l.getLibraryId().equals(id)).findFirst().orElse(null);
		if(lib == null)
			lib = getMsRtLibraryList().stream().
				filter(l -> l.getLibraryId().equals(id)).findFirst().orElse(null);
		return lib;
	}
	
	public static CompoundLibrary getMSRTLibraryByName(String name) {
		
		CompoundLibrary lib = MRC2ToolBoxCore.getActiveMsLibraries().stream().
				filter(l -> l.getLibraryName().equals(name)).findFirst().orElse(null);
		if(lib == null)
			lib = getMsRtLibraryList().stream().
					filter(l -> l.getLibraryName().equals(name)).findFirst().orElse(null);
		return lib;
	}
}


















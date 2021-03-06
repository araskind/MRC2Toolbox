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

package edu.umich.med.mrc2.datoolbox.project;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.commons.compress.utils.FileNameUtils;

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesign;
import edu.umich.med.mrc2.datoolbox.data.ExperimentalSample;
import edu.umich.med.mrc2.datoolbox.data.MSFeatureInfoBundle;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureChromatogramBundle;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureInfoBundleCollection;
import edu.umich.med.mrc2.datoolbox.data.Worklist;
import edu.umich.med.mrc2.datoolbox.data.compare.MSMSClusterDataSetComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.MsFeatureInfoBundleComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.MsFeatureInformationBundleCollectionComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.lims.DataAcquisitionMethod;
import edu.umich.med.mrc2.datoolbox.data.lims.Injection;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSExperiment;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSInstrument;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSSamplePreparation;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSUser;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSWorklistItem;
import edu.umich.med.mrc2.datoolbox.data.msclust.MSMSClusterDataSet;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.main.FeatureCollectionManager;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.MSMSClusterDataSetManager;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.rawdata.MSMSExtractionParameterSet;

public class RawDataAnalysisProject extends Project {
	
	protected File rawDataDirectory;
	protected File uncompressedProjectFilesDirectory;
	protected LIMSInstrument instrument;
	protected Collection<DataFile>msmsDataFiles;
	protected Collection<DataFile>msOneDataFiles;
	protected Collection<Injection>injections;
	protected Map<DataFile, Collection<MSFeatureInfoBundle>>msFeatureMap;	
	protected Map<String, MsFeatureChromatogramBundle>chromatogramMap;
	protected Set<MsFeatureInfoBundleCollection>featureCollections;
	protected Collection<MSMSClusterDataSet> msmsClusterDataSets;
	protected MSMSExtractionParameterSet msmsExtractionParameterSet;
//	protected Set<Injection>injections;
	protected LIMSUser createdBy;
	protected LIMSExperiment idTrackerExperiment;
	
	//	New project
	public RawDataAnalysisProject(
			String projectName, 
			String projectDescription, 
			File parentDirectory,
			LIMSUser createdBy) {

		super(projectName, projectDescription, parentDirectory);
		initNewProject(parentDirectory);
		this.createdBy = createdBy;
		initFields();
	}
	
	public RawDataAnalysisProject(RawDataAnalysisProject activeProject) {
		
		super(activeProject);

		this.exportsDirectory = activeProject.getExportsDirectory();
		this.rawDataDirectory = activeProject.getRawDataDirectory();		
		this.msmsDataFiles = new TreeSet<DataFile>();
		this.msmsDataFiles.addAll(activeProject.getMSMSDataFiles());
		this.msOneDataFiles = new TreeSet<DataFile>();
		this.msmsDataFiles.addAll(activeProject.getMSOneDataFiles());
		this.createdBy = activeProject.getCreatedBy();
		initFields();
	}
	
	public RawDataAnalysisProject(
			String id, 
			String name, 
			String description, 
			File projectFile, 
			Date dateCreated,
			Date lastModified) {
		super(id, name, description, projectFile, dateCreated, lastModified);
		setProjectDirectories();
		initFields();
	}
	
	public void updateProjectLocation(File newProjectFile) {
		
		projectFile = newProjectFile;
		projectDirectory = newProjectFile.getParentFile();
		exportsDirectory = Paths.get(projectDirectory.getAbsolutePath(), 
				MRC2ToolBoxConfiguration.DATA_EXPORT_DIRECTORY).toFile();	
		File newRawDataDirectory = Paths.get(projectDirectory.getAbsolutePath(), 
				MRC2ToolBoxConfiguration.RAW_DATA_DIRECTORY).toFile();
		if(!newRawDataDirectory.equals(rawDataDirectory)) {
			
			for(DataFile df : getDataFiles()) {
				
				File rdf = new File(df.getFullPath());
				if(rdf.getParentFile().equals(rawDataDirectory))
					df.setFullPath(Paths.get(newRawDataDirectory.getAbsolutePath(), df.getName()).toString());
			}
			rawDataDirectory = newRawDataDirectory;
		}
	}
	
	@Override
	protected void initNewProject(File parentDirectory) {
		
		super.initNewProject(parentDirectory);
		rawDataDirectory = Paths.get(projectDirectory.getAbsolutePath(), 
				MRC2ToolBoxConfiguration.RAW_DATA_DIRECTORY).toFile();
		uncompressedProjectFilesDirectory = Paths.get(projectDirectory.getAbsolutePath(), 
				MRC2ToolBoxConfiguration.UNCOMPRESSED_PROJECT_FILES_DIRECTORY).toFile();
		try {
			Files.createDirectories(Paths.get(rawDataDirectory.getAbsolutePath()));
		} catch (IOException e) {
			e.printStackTrace();
			MessageDialog.showWarningMsg("Failed to create raw data directory");
			return;
		}
		try {
			Files.createDirectories(Paths.get(uncompressedProjectFilesDirectory.getAbsolutePath()));
		} catch (IOException e) {
			e.printStackTrace();
			MessageDialog.showWarningMsg("Failed to create project files directory");
			return;
		}
	}
	
	@Override
	protected void setProjectDirectories() {
		
		super.setProjectDirectories();
		
		rawDataDirectory = Paths.get(projectDirectory.getAbsolutePath(), 
				MRC2ToolBoxConfiguration.RAW_DATA_DIRECTORY).toFile();
		uncompressedProjectFilesDirectory = Paths.get(projectDirectory.getAbsolutePath(), 
				MRC2ToolBoxConfiguration.UNCOMPRESSED_PROJECT_FILES_DIRECTORY).toFile();
	}

	private void initFields() {
		
		idTrackerExperiment = new LIMSExperiment(
				null, 
				name, 
				description, 
				null, 
				null, 
				dateCreated);
		idTrackerExperiment.setCreator(createdBy);		
		msmsDataFiles = new TreeSet<DataFile>();
		msOneDataFiles = new TreeSet<DataFile>();
		injections = new HashSet<Injection>();
		msFeatureMap = new TreeMap<DataFile, Collection<MSFeatureInfoBundle>>();
		chromatogramMap = new HashMap<String, MsFeatureChromatogramBundle>();
		featureCollections = 
				new TreeSet<MsFeatureInfoBundleCollection>(
						new MsFeatureInformationBundleCollectionComparator(SortProperty.Name));		
		msmsClusterDataSets = 
				new TreeSet<MSMSClusterDataSet>(
						new MSMSClusterDataSetComparator(SortProperty.Name));
		//	TODO
	}

	public void addMSMSDataFile(DataFile fileToAdd) {
		msmsDataFiles.add(fileToAdd);
		msFeatureMap.put(fileToAdd, new HashSet<MSFeatureInfoBundle>());
		if(fileToAdd.getInjectionId() == null)		
			injections.add(fileToAdd.generateInjectionFromFileData());
		//	TODO
	}
	
	public void addMSMSDataFiles(Collection<DataFile> filesToAdd) {	
		
		msmsDataFiles.addAll(filesToAdd);		
		for(DataFile df : filesToAdd) {
			msFeatureMap.put(df, new HashSet<MSFeatureInfoBundle>());
			if(df.getInjectionId() == null)
				injections.add(df.generateInjectionFromFileData());
		}
		//	TODO
	}

	public void addMSOneDataFile(DataFile fileToAdd) {
		msOneDataFiles.add(fileToAdd);
		msFeatureMap.put(fileToAdd, new HashSet<MSFeatureInfoBundle>());
		if(fileToAdd.getInjectionId() == null)
			injections.add(fileToAdd.generateInjectionFromFileData());
		
		//	TODO
	}
	
	public void addMSOneDataFiles( Collection<DataFile> filesToAdd) {	
		
		msOneDataFiles.addAll(filesToAdd);		
		for(DataFile df : filesToAdd) {
			msFeatureMap.put(df, new HashSet<MSFeatureInfoBundle>());
			if(df.getInjectionId() == null)
				injections.add(df.generateInjectionFromFileData());
		}
		//	TODO
	}
	
	public void removeMSMSDataFile(DataFile fileToRemove) {
		
		msmsDataFiles.remove(fileToRemove);
		msFeatureMap.remove(fileToRemove);
		removeInjectionForDataFile(fileToRemove);
		//	TODO
	}
	
	private void removeInjectionForDataFile(DataFile fileToRemove) {
		
		String fileName = fileToRemove.getName();
		Injection injToRemove = injections.
				stream().filter(i -> i.getDataFileName().equals(fileName)).
				findFirst().orElse(null);
		if(injToRemove != null)
			injections.remove(injToRemove);
	}
	
	public void removeMSOneDataFile(DataFile fileToRemove) {
		
		msOneDataFiles.remove(fileToRemove);
		msFeatureMap.remove(fileToRemove);
		removeInjectionForDataFile(fileToRemove);
		//	TODO
	}
	
	public Collection<MSFeatureInfoBundle>getMsFeaturesForDataFile(DataFile df){
		return msFeatureMap.get(df);
	}
	
	public void addMsFeaturesForDataFile(DataFile df, Collection<MSFeatureInfoBundle>features){
		msFeatureMap.get(df).addAll(features);
	}
	
	public void setMsFeaturesForDataFile(DataFile df, Collection<MSFeatureInfoBundle>features){
		msFeatureMap.put(df, features);
	}
	
	public Collection<DataFile>getDataFilesForAcquisitionMethod(DataAcquisitionMethod method){
		
		return msmsDataFiles.stream().
				filter(f -> f.getDataAcquisitionMethod().equals(method)).
				sorted().collect(Collectors.toList());
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Date getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	public Date getLastModified() {
		return lastModified;
	}

	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}

	public String getId() {
		return id;
	}

	public File getProjectFile() {
		return projectFile;
	}

	public File getProjectDirectory() {
		return projectDirectory;
	}

	public File getExportsDirectory() {
		return exportsDirectory;
	}

	public File getRawDataDirectory() {
		return rawDataDirectory;
	}

	public Collection<DataFile> getMSMSDataFiles() {
		return msmsDataFiles;
	}
	
	public Collection<DataFile> getMSOneDataFiles() {
		return msOneDataFiles;
	}
	
	public Collection<DataFile> getDataFiles() {
		return msFeatureMap.keySet();
	}
	
	public DataFile getDataFileByName(String name) {
		
		String baseName = FileNameUtils.getBaseName(name);
		return getDataFiles().stream().
				filter(f -> FileNameUtils.getBaseName(f.getName()).equals(baseName)).
				findFirst().orElse(null);
	}
	
	public Collection<ExperimentalSample> getExperimentalSamples() {
		
		Collection<ExperimentalSample>samples = new TreeSet<ExperimentalSample>();
		msmsDataFiles.stream().
			filter(f -> Objects.nonNull(f.getParentSample())).
			forEach(f -> samples.add(f.getParentSample()));
		msOneDataFiles.stream().
			filter(f -> Objects.nonNull(f.getParentSample())).
			forEach(f -> samples.add(f.getParentSample()));
		return samples;
	}
	
	public Collection<MSFeatureInfoBundle>getMsMsFeatureBundles(){
		
		return msFeatureMap.values().stream().
				flatMap(v -> v.stream()).
				filter(f -> Objects.nonNull(f.getMsFeature().getSpectrum())).
				filter(f -> Objects.nonNull(f.getMsFeature().getSpectrum().getExperimentalTandemSpectrum())).
				sorted(new MsFeatureInfoBundleComparator(SortProperty.RT)).
				collect(Collectors.toList());
	}
	
	public Collection<MSFeatureInfoBundle>getFeatureBundlesForIds(Collection<String>idList){
		
		return msFeatureMap.values().stream().
				flatMap(v -> v.stream()).
				filter(f -> idList.contains(f.getMsFeature().getId())).
				sorted(new MsFeatureInfoBundleComparator(SortProperty.RT)).
				collect(Collectors.toList());
	}

	public File getUncompressedProjectFilesDirectory() {
		return uncompressedProjectFilesDirectory;
	}

	public Map<String, MsFeatureChromatogramBundle> getChromatogramMap() {
		return chromatogramMap;
	}

	public void setChromatogramMap(Map<String, MsFeatureChromatogramBundle> chromatogramMap) {
		this.chromatogramMap = chromatogramMap;
	}

	public void clearMSMSFeatures() {
		
		for(Entry<DataFile, Collection<MSFeatureInfoBundle>> e : msFeatureMap.entrySet())
			e.getValue().clear();
		
		chromatogramMap.clear();
	}

	public Set<MsFeatureInfoBundleCollection> getFeatureCollections() {
		return featureCollections;
	}
	
	public Collection<MsFeatureInfoBundleCollection> getEditableMsFeatureInfoBundleCollections(){
		return featureCollections.stream().
				filter(c -> !c.equals(FeatureCollectionManager.activeProjectFeatureSet)).
				sorted(new MsFeatureInformationBundleCollectionComparator(SortProperty.Name)).
				collect(Collectors.toList());
	}
	
	public void addMsFeatureInfoBundleCollection(MsFeatureInfoBundleCollection fbCollection) {
			featureCollections.add(fbCollection);
	}
	
	public void removeMsFeatureInfoBundleCollection(MsFeatureInfoBundleCollection fbCollection) {
		featureCollections.remove(fbCollection);
	}

	public LIMSInstrument getInstrument() {
		return instrument;
	}

	public void setInstrument(LIMSInstrument instrument) {
		this.instrument = instrument;
	}

	public MSMSExtractionParameterSet getMsmsExtractionParameterSet() {
		return msmsExtractionParameterSet;
	}

	public void setMsmsExtractionParameterSet(MSMSExtractionParameterSet msmsExtractionParameterSet) {
		this.msmsExtractionParameterSet = msmsExtractionParameterSet;
	}

	public Collection<Injection> getInjections() {	
		
		if(injections == null)
			injections = new HashSet<Injection>();
		
		if(injections.isEmpty())
			injections = getDataFiles().stream().
				map(f -> f.generateInjectionFromFileData()).
				collect(Collectors.toList());
			
		return injections;
	}

	public ExperimentDesign getExperimentDesign() {
		
		if(idTrackerExperiment == null)
			return null;
		else
			return idTrackerExperiment.getExperimentDesign();
	}

	public LIMSUser getCreatedBy() {
		
		if(createdBy == null)
			createdBy = MRC2ToolBoxCore.getIdTrackerUser();
		
		return createdBy;
	}

	public void setCreatedBy(LIMSUser createdBy) {
		this.createdBy = createdBy;
	}

	public LIMSExperiment getIdTrackerExperiment() {
		
		if(idTrackerExperiment == null) {
			
			idTrackerExperiment = new LIMSExperiment(
					null, 
					name, 
					description, 
					null, 
					null, 
					dateCreated);
			
			if(createdBy == null)
				createdBy = MRC2ToolBoxCore.getIdTrackerUser();
			
			idTrackerExperiment.setCreator(createdBy);
			idTrackerExperiment.setDesign(new ExperimentDesign());			
		}
		return idTrackerExperiment;
	}

	public void setIdTrackerExperiment(LIMSExperiment idTrackerExperiment) {
		this.idTrackerExperiment = idTrackerExperiment;
	}
	
	public Worklist getWorklist() {
		
		LIMSSamplePreparation samplePrep = null;
		if(idTrackerExperiment != null 
				&& idTrackerExperiment.getSamplePreps() != null
				&& !idTrackerExperiment.getSamplePreps().isEmpty()) {
			
			//	TODO handle multiple preps?
			samplePrep = idTrackerExperiment.getSamplePreps().iterator().next();
		}
		if(idTrackerExperiment.getExperimentDesign() == null)
			idTrackerExperiment.setDesign(new ExperimentDesign());
			
		Worklist worklist = new Worklist();
		for(DataFile df : getDataFiles()) {
			
			ExperimentalSample parentSample = df.getParentSample();
			if(parentSample == null)
				parentSample = idTrackerExperiment.getExperimentDesign().getSampleByDataFile(df);
						
			LIMSWorklistItem wklItem = new LIMSWorklistItem(
				df,
				parentSample,
				df.getDataAcquisitionMethod(),
				samplePrep,
				df.getPrepItemId(),
				df.getInjectionTime(),
				df.getInjectionVolume());
			worklist.addItem(wklItem);
		}
		return worklist;
	}
	
	public Collection<DataAcquisitionMethod>getDataAcquisitionMethods(){
		return getDataFiles().stream().
				filter(df -> Objects.nonNull(df.getDataAcquisitionMethod())).
				map(df -> df.getDataAcquisitionMethod()).collect(Collectors.toSet());
	}

	public void updateMetadataFromWorklist(Worklist worklist) {
		
		Collection<LIMSWorklistItem>wklItems = 
				worklist.getWorklistItems().stream().
				filter(LIMSWorklistItem.class::isInstance).
				map(LIMSWorklistItem.class::cast).collect(Collectors.toList());
		
		//	Data files
		for(DataFile df : getDataFiles()) {
			
			LIMSWorklistItem item = wklItems.stream().
					filter(i -> i.getDataFile().getBaseName().equals(df.getBaseName())).
					findFirst().orElse(null);
			if(item != null) {
				df.setDataAcquisitionMethod(item.getAcquisitionMethod());
				df.setInjectionTime(item.getTimeStamp());
				df.setInjectionVolume(item.getInjectionVolume());
				df.setParentSample(item.getSample());
				df.setPrepItemId(item.getPrepItemId());
				
				if(idTrackerExperiment != null && idTrackerExperiment.getExperimentDesign() != null) {
					ExperimentalSample sample = 
							idTrackerExperiment.getExperimentDesign().getSampleById(item.getSample().getId());
					if(sample != null)
						sample.addDataFile(df);
				}
			}
		}	
	}

	public Collection<MSMSClusterDataSet> getMsmsClusterDataSets() {
		return msmsClusterDataSets;
	}
	
	public Collection<MSMSClusterDataSet> getEditableMsmsClusterDataSets(){
		return msmsClusterDataSets.stream().
				filter(c -> !c.equals(MSMSClusterDataSetManager.activeProjectDefaultClusterDataSet)).
				sorted(new MSMSClusterDataSetComparator(SortProperty.Name)).
				collect(Collectors.toList());
	}
}













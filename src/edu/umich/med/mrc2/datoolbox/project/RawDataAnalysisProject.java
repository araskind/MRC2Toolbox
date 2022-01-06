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
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.ExperimentalSample;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureChromatogramBundle;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureInfoBundle;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureInfoBundleCollection;
import edu.umich.med.mrc2.datoolbox.data.compare.MsFeatureInfoBundleComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.MsFeatureInformationBundleCollectionComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.lims.DataAcquisitionMethod;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSInstrument;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.main.FeatureCollectionManager;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;

public class RawDataAnalysisProject extends Project {
	
	protected File rawDataDirectory;
	protected File uncompressedProjectFilesDirectory;
	protected LIMSInstrument instrument;
	protected Collection<DataFile>msmsDataFiles;
	protected Collection<DataFile>msOneDataFiles;
	protected Map<DataFile, Collection<MsFeatureInfoBundle>>msFeatureMap;	
	protected Map<String, MsFeatureChromatogramBundle>chromatogramMap;
	protected Set<MsFeatureInfoBundleCollection>featureCollections;
	
	//	New project
	public RawDataAnalysisProject(
			String projectName, 
			String projectDescription, 
			File parentDirectory) {

		super(projectName, projectDescription, parentDirectory);
		initNewProject(parentDirectory);
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
		
		msmsDataFiles = new TreeSet<DataFile>();
		msOneDataFiles = new TreeSet<DataFile>();
		msFeatureMap = new TreeMap<DataFile, Collection<MsFeatureInfoBundle>>();
		chromatogramMap = new HashMap<String, MsFeatureChromatogramBundle>();
		featureCollections = 
				new TreeSet<MsFeatureInfoBundleCollection>(
						new MsFeatureInformationBundleCollectionComparator(SortProperty.Name));
		//	TODO
	}
	
	public void addMSMSDataFile(DataFile fileToAdd) {
		msmsDataFiles.add(fileToAdd);
		msFeatureMap.put(fileToAdd, new HashSet<MsFeatureInfoBundle>());
		//	TODO
	}
	
	public void addMSMSDataFiles(Collection<DataFile> filesToAdd) {		
		msmsDataFiles.addAll(filesToAdd);
		for(DataFile df : filesToAdd)
			msFeatureMap.put(df, new HashSet<MsFeatureInfoBundle>());
		
		//	TODO
	}

	public void addMSOneDataFile(DataFile fileToAdd) {
		msOneDataFiles.add(fileToAdd);
		msFeatureMap.put(fileToAdd, new HashSet<MsFeatureInfoBundle>());
		//	TODO
	}
	
	public void addMSOneDataFiles( Collection<DataFile> filesToAdd) {		
		msOneDataFiles.addAll(filesToAdd);
		for(DataFile df : filesToAdd)
			msFeatureMap.put(df, new HashSet<MsFeatureInfoBundle>());
		
		//	TODO
	}
	
	public void removeMSMSDataFile(DataFile fileToRemove) {
		msmsDataFiles.remove(fileToRemove);
		msFeatureMap.remove(fileToRemove);
		//	TODO
	}
	
	public void removeMSOneDataFile(DataFile fileToRemove) {
		msOneDataFiles.remove(fileToRemove);
		msFeatureMap.remove(fileToRemove);
		//	TODO
	}
	
	public Collection<MsFeatureInfoBundle>getMsFeaturesForDataFile(DataFile df){
		return msFeatureMap.get(df);
	}
	
	public void addMsFeaturesForDataFile(DataFile df, Collection<MsFeatureInfoBundle>features){
		msFeatureMap.get(df).addAll(features);
	}
	
	public void setMsFeaturesForDataFile(DataFile df, Collection<MsFeatureInfoBundle>features){
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
	
	public Collection<ExperimentalSample> getExperimentalSamples() {
		
		Collection<ExperimentalSample>samples = new TreeSet<ExperimentalSample>();
		msmsDataFiles.stream().
			filter(f -> f.getParentSample() != null).
			forEach(f -> samples.add(f.getParentSample()));
		msOneDataFiles.stream().
			filter(f -> f.getParentSample() != null).
			forEach(f -> samples.add(f.getParentSample()));
		return samples;
	}
	
	public Collection<MsFeatureInfoBundle>getMsMsFeatureBundles(){
		
		return msFeatureMap.values().stream().
				flatMap(v -> v.stream()).
				filter(f -> f.getMsFeature().getSpectrum() != null).
				filter(f -> f.getMsFeature().getSpectrum().getExperimentalTandemSpectrum() != null).
				sorted(new MsFeatureInfoBundleComparator(SortProperty.RT)).
				collect(Collectors.toList());
	}
	
	public Collection<MsFeatureInfoBundle>getFeatureBundlesForIds(Collection<String>idList){
		
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
		
		for(Entry<DataFile, Collection<MsFeatureInfoBundle>> e : msFeatureMap.entrySet())
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
}









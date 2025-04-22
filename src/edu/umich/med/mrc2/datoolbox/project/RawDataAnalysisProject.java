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
import java.nio.file.Path;
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
import java.util.concurrent.ConcurrentHashMap;
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
import edu.umich.med.mrc2.datoolbox.data.compare.MsFeatureInfoBundleCollectionComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.MsFeatureInfoBundleComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.lims.DataAcquisitionMethod;
import edu.umich.med.mrc2.datoolbox.data.lims.Injection;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSExperiment;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSInstrument;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSSamplePreparation;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSUser;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSWorklistItem;
import edu.umich.med.mrc2.datoolbox.data.msclust.IMSMSClusterDataSet;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.main.FeatureCollectionManager;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.rawdata.MSMSExtractionParameterSet;

public class RawDataAnalysisProject extends Project {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected LIMSInstrument instrument;
	protected Collection<DataFile>msmsDataFiles;
	protected Collection<DataFile>msOneDataFiles;
	protected Collection<Injection>injections;
	protected Map<DataFile, Collection<MSFeatureInfoBundle>>msFeatureMap;	
	protected Map<String, MsFeatureChromatogramBundle>chromatogramMap;
	protected Set<MsFeatureInfoBundleCollection>featureCollections;
	protected Collection<IMSMSClusterDataSet> msmsClusterDataSets;
	protected MSMSExtractionParameterSet msmsExtractionParameterSet;
		
	/**
	 * This constructor is used when creating new experiment
	 * 
	 * @param experimentName
	 * @param experimentDescription
	 * @param parentDirectory
	 * @param createdBy
	 */
	public RawDataAnalysisProject(
			String experimentName, 
			String experimentDescription, 
			File parentDirectory,
			LIMSUser createdBy) {

		super(ProjectType.RAW_DATA_ANALYSIS, 
				experimentName, 
				experimentDescription);
		createDirectoryStructureForNewExperiment(parentDirectory);
		initFields();
		this.createdBy = createdBy;
	}
	
	/**
	 * This constructor is used when restoring the experiment from XML file
	 * 
	 * @param id
	 * @param name
	 * @param description
	 * @param experimentFile
	 * @param dateCreated
	 * @param lastModified
	 */
	public RawDataAnalysisProject(
			String id, 
			String name, 
			String description, 
			File experimentFile, 
			Date dateCreated,
			Date lastModified) {
		super(id, name, description, experimentFile, dateCreated, lastModified);
		this.projectType = ProjectType.RAW_DATA_ANALYSIS;
		initFields();
	}
	
	@Override
	protected void createDirectoryStructureForNewExperiment(File parentDirectory) {
		
		super.createDirectoryStructureForNewExperiment(parentDirectory);
		
		Path rawDataDirectoryPath = Paths.get(getExperimentDirectory().getAbsolutePath(), 
				MRC2ToolBoxConfiguration.RAW_DATA_DIRECTORY);
		try {
			Files.createDirectories(rawDataDirectoryPath);
		} catch (IOException e) {
			e.printStackTrace();
			MessageDialog.showWarningMsg("Failed to create raw data directory");
		}
		Path uncompressedExperimentFilesDirectoryPath = 
				Paths.get(getExperimentDirectory().getAbsolutePath(), 
						MRC2ToolBoxConfiguration.UNCOMPRESSED_EXPERIMENT_FILES_DIRECTORY);
		try {
			Files.createDirectories(uncompressedExperimentFilesDirectoryPath);
		} catch (IOException e) {
			e.printStackTrace();
			MessageDialog.showWarningMsg("Failed to uncompressed experiment files directory");
		}
	}

	private void initFields() {
		
		limsExperiment = new LIMSExperiment(
				null, 
				name, 
				description, 
				null, 
				null, 
				dateCreated);
		limsExperiment.setCreator(createdBy);		
		msmsDataFiles = ConcurrentHashMap.newKeySet();
		msOneDataFiles = ConcurrentHashMap.newKeySet();
		injections = new HashSet<Injection>();
		msFeatureMap = new TreeMap<DataFile, Collection<MSFeatureInfoBundle>>();
		chromatogramMap = new HashMap<String, MsFeatureChromatogramBundle>();
		featureCollections = 
				new TreeSet<MsFeatureInfoBundleCollection>(
						new MsFeatureInfoBundleCollectionComparator(SortProperty.Name));		
		msmsClusterDataSets = 
				new TreeSet<IMSMSClusterDataSet>(
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
	
	public void addMsFeaturesForDataFile(DataFile df, 
			Collection<MSFeatureInfoBundle>features){
		msFeatureMap.get(df).addAll(features);
	}
	
	public void setMsFeaturesForDataFile(DataFile df, 
			Collection<MSFeatureInfoBundle>features){
		msFeatureMap.put(df, features);
	}
	
	public Collection<DataFile>getDataFilesForAcquisitionMethod(
			DataAcquisitionMethod method){
		
		return msmsDataFiles.stream().
				filter(f -> f.getDataAcquisitionMethod().equals(method)).
				sorted().collect(Collectors.toList());
	}
	
	public File getRawDataDirectory() {
		
		return Paths.get(getExperimentDirectory().getAbsolutePath(), 
				MRC2ToolBoxConfiguration.RAW_DATA_DIRECTORY).toFile();
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
	
	public DataFile getDataFileByNameAndMethod(
			String name, DataAcquisitionMethod method) {
		
		if(method == null)
			return getDataFileByName(name);
		else {
			String baseName = FileNameUtils.getBaseName(name);
			return getDataFiles().stream().
					filter(f -> FileNameUtils.getBaseName(f.getName()).equals(baseName)).
					filter(f -> Objects.nonNull(f.getDataAcquisitionMethod())).
					filter(f -> f.getDataAcquisitionMethod().equals(method)).
					findFirst().orElse(null);
		}
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
	
	public Collection<MSFeatureInfoBundle>getMsOneFeatureBundles(){
		
		return msFeatureMap.values().stream().
				flatMap(v -> v.stream()).
				filter(f -> Objects.nonNull(f.getMsFeature().getSpectrum())).
				filter(f -> Objects.isNull(f.getMsFeature().getSpectrum().getExperimentalTandemSpectrum())).
				sorted(new MsFeatureInfoBundleComparator(SortProperty.RT)).
				collect(Collectors.toList());
	}
	
	public Collection<MSFeatureInfoBundle>getFeatureBundlesForIds(Collection<String>idList){
		
		return msFeatureMap.values().stream().
				flatMap(v -> v.stream()).
				filter(f -> idList.contains(f.getMSFeatureId())).
				sorted(new MsFeatureInfoBundleComparator(SortProperty.RT)).
				collect(Collectors.toList());
	}

	public File getUncompressedExperimentFilesDirectory() {
		
		return  Paths.get(getExperimentDirectory().getAbsolutePath(), 
				MRC2ToolBoxConfiguration.UNCOMPRESSED_EXPERIMENT_FILES_DIRECTORY).toFile();
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
		
		if(featureCollections == null)
			featureCollections = 
					new TreeSet<MsFeatureInfoBundleCollection>(
							new MsFeatureInfoBundleCollectionComparator(SortProperty.Name));
		
		return featureCollections;
	}
	
	public Collection<MsFeatureInfoBundleCollection> getEditableMsFeatureInfoBundleCollections(){
		
		return getFeatureCollections().stream().
				filter(c -> !c.equals(FeatureCollectionManager.activeExperimentFeatureSet)).
				sorted(new MsFeatureInfoBundleCollectionComparator(SortProperty.Name)).
				collect(Collectors.toList());
	}
	
	public void addMsFeatureInfoBundleCollection(MsFeatureInfoBundleCollection fbCollection) {
		getFeatureCollections().add(fbCollection);
	}
	
	public void removeMsFeatureInfoBundleCollection(MsFeatureInfoBundleCollection fbCollection) {
		getFeatureCollections().remove(fbCollection);
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
		
		if(limsExperiment == null)
			return null;
		else
			return limsExperiment.getExperimentDesign();
	}

	public LIMSUser getCreatedBy() {
		
		if(createdBy == null)
			createdBy = MRC2ToolBoxCore.getIdTrackerUser();
		
		return createdBy;
	}

	public void setCreatedBy(LIMSUser createdBy) {
		this.createdBy = createdBy;
	}

	@Override
	public LIMSExperiment getLimsExperiment() {
		
		if(limsExperiment == null) {
			
			limsExperiment = new LIMSExperiment(
					null, 
					name, 
					description, 
					null, 
					null, 
					dateCreated);
			
			if(createdBy == null)
				createdBy = MRC2ToolBoxCore.getIdTrackerUser();
			
			limsExperiment.setCreator(createdBy);
			limsExperiment.setDesign(new ExperimentDesign());			
		}
		return limsExperiment;
	}
	
	public Worklist getWorklist() {
		
		LIMSSamplePreparation samplePrep = null;
		if(limsExperiment != null 
				&& limsExperiment.getSamplePreps() != null
				&& !limsExperiment.getSamplePreps().isEmpty()) {
			
			//	TODO handle multiple preps?
			samplePrep = limsExperiment.getSamplePreps().iterator().next();
		}
		if(limsExperiment.getExperimentDesign() == null)
			limsExperiment.setDesign(new ExperimentDesign());
			
		Worklist worklist = new Worklist();
		for(DataFile df : getDataFiles()) {
			
			ExperimentalSample parentSample = df.getParentSample();
			if(parentSample == null)
				parentSample = limsExperiment.getExperimentDesign().getSampleByDataFile(df);
						
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
				
				if(limsExperiment != null && limsExperiment.getExperimentDesign() != null) {
					ExperimentalSample sample = 
							limsExperiment.getExperimentDesign().getSampleById(item.getSample().getId());
					if(sample != null)
						sample.addDataFile(df);
				}
			}
		}	
	}
	
	public Collection<IMSMSClusterDataSet> getMsmsClusterDataSets() {
		
		if(msmsClusterDataSets == null)
			msmsClusterDataSets = new TreeSet<IMSMSClusterDataSet>(
					new MSMSClusterDataSetComparator(SortProperty.Name));
		
		return msmsClusterDataSets;
	}

	@Override
	public Set<ExperimentalSample> getPooledSamples() {
		// TODO Auto-generated method stub
		return null;
	}
}













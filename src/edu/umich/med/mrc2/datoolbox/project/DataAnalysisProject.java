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
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.commons.compress.utils.FileNameUtils;
import org.jdom2.Element;
import org.ujmp.core.Matrix;
import org.ujmp.core.calculation.Calculation.Ret;

import edu.umich.med.mrc2.datoolbox.data.Assay;
import edu.umich.med.mrc2.datoolbox.data.CompoundLibrary;
import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesign;
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignLevel;
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignSubset;
import edu.umich.med.mrc2.datoolbox.data.ExperimentalSample;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureCluster;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureClusterSet;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureSet;
import edu.umich.med.mrc2.datoolbox.data.Worklist;
import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.data.enums.GlobalDefaults;
import edu.umich.med.mrc2.datoolbox.data.lims.DataAcquisitionMethod;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSExperiment;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSProject;
import edu.umich.med.mrc2.datoolbox.gui.main.MainWindow;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.project.store.MetabolomicsProjectFields;
import edu.umich.med.mrc2.datoolbox.project.store.ObjectNames;
import edu.umich.med.mrc2.datoolbox.utils.ProjectUtils;

public class DataAnalysisProject extends Project {

	/**
	 *
	 */
	private static final long serialVersionUID = 618045235335401590L;

	protected LIMSProject limsProject;	//	TODO - get on the fly from LIMS experiment
	protected TreeSet<DataPipeline> dataPipelines;
	protected DataPipeline activeDataPipeline;	
	protected TreeMap<DataPipeline, CompoundLibrary> libraryMap;
	protected TreeMap<DataPipeline, CompoundLibrary> averagedFeatureMap;
	protected TreeMap<DataPipeline, Set<MsFeature>> featureMap;
	protected TreeMap<DataPipeline, Matrix> dataMatrixMap;
	protected TreeMap<DataPipeline, String> dataMatrixFileMap;
	protected TreeMap<DataPipeline, Matrix> featureMatrixMap;
	protected TreeMap<DataPipeline, String> featureMatrixFileMap;	
	protected TreeMap<DataPipeline, Matrix> imputedDataMatrixMap;
	protected TreeMap<DataPipeline, Matrix> corrMatrixMap;
	protected TreeMap<DataPipeline, Matrix[]> metaDataMap;
	protected TreeMap<DataPipeline, Set<MsFeatureCluster>> duplicatesMap;
	protected TreeMap<DataPipeline, Set<MsFeatureCluster>> clusterMap;
	protected TreeMap<DataPipeline, Boolean> statsCalculatedMap;
	protected TreeMap<DataPipeline, Set<MsFeatureSet>> featureSetMap;
	protected TreeMap<DataAcquisitionMethod, Set<DataFile>> dataFileMap;
	protected TreeMap<DataAcquisitionMethod, Worklist> worklistMap;
	protected Set<MsFeatureClusterSet>dataIntegrationSets;
	
	public DataAnalysisProject(
			String projectName, 
			String projectDescription, 
			File parentDirectory) {

		super(ProjectType.DATA_ANALYSIS, 
				projectName, 
				projectDescription);

		createDirectoryStructureForNewExperiment(parentDirectory);
		initProjectFields(true);
	}

	protected void initProjectFields(boolean initDesign) {

		libraryMap = new TreeMap<DataPipeline, CompoundLibrary>();
		averagedFeatureMap = new TreeMap<DataPipeline, CompoundLibrary>();
		duplicatesMap = new TreeMap<DataPipeline, Set<MsFeatureCluster>>();
		clusterMap = new TreeMap<DataPipeline, Set<MsFeatureCluster>>();
		featureMap = new TreeMap<DataPipeline, Set<MsFeature>>();
		worklistMap = new TreeMap<DataAcquisitionMethod, Worklist>();
		dataFileMap = new TreeMap<DataAcquisitionMethod, Set<DataFile>>();
		statsCalculatedMap = new TreeMap<DataPipeline, Boolean>();
		dataMatrixMap = new TreeMap<DataPipeline, Matrix>();
		dataMatrixFileMap = new TreeMap<DataPipeline, String>();
		featureMatrixMap = new TreeMap<DataPipeline, Matrix>();
		featureMatrixFileMap = new TreeMap<DataPipeline, String>();
		imputedDataMatrixMap = new TreeMap<DataPipeline, Matrix>();
		corrMatrixMap = new TreeMap<DataPipeline, Matrix>();	
		dataPipelines = new TreeSet<DataPipeline>();				
		featureSetMap = new TreeMap<DataPipeline, Set<MsFeatureSet>>();
		dataIntegrationSets = new TreeSet<MsFeatureClusterSet>();
		metaDataMap = new TreeMap<DataPipeline, Matrix[]>();
		activeDataPipeline = null;
		
		if(initDesign)
			experimentDesign = new ExperimentDesign();
	}
	
	public Set<DataPipeline> getDataPipelines() {
		return dataPipelines;
	}

	public LIMSProject getLimsProject() {
		return limsProject;
	}

	public void setLimsProject(LIMSProject limsProject) {
		this.limsProject = limsProject;
	}

	public LIMSExperiment getLimsExperiment() {
		return limsExperiment;
	}

	public void recreateMatrixMaps() {
		dataMatrixMap = new TreeMap<DataPipeline, Matrix>();
		corrMatrixMap = new TreeMap<DataPipeline, Matrix>();
		featureMatrixMap = new TreeMap<DataPipeline, Matrix>();
	}

	public void addDataPipeline(DataPipeline pipeline) {
		
		if(dataPipelines.contains(pipeline)) {
			MainWindow.displayErrorMessage("Error",
					"Data pipeline is already present in the project:\n" +
					"Assay: " + pipeline.getAssay().getName() + "\n" +
					"Acquisition method: " + pipeline.getAcquisitionMethod().getName() + "\n" +
					"Data analysis method: " + pipeline.getDataExtractionMethod().getName());
			return;
		}		
		dataPipelines.add(pipeline);
		String matrixFileName = 
				ProjectUtils.getNewDataMatrixFileNameForDataPipeline(pipeline);
		dataMatrixFileMap.put(pipeline, matrixFileName);
		statsCalculatedMap.put(pipeline, false);
		featureSetMap.put(pipeline, new TreeSet<MsFeatureSet>());
	}
	
	public void addFeatureMatrixForDataPipeline(DataPipeline pipeline, Matrix featureMatrix) {
		
		if(featureMatrix == null)
			return;
		
		if(featureMatrixFileMap == null)
			featureMatrixFileMap = new TreeMap<DataPipeline, String>();
		
		if(featureMatrixMap == null)
			featureMatrixMap = new TreeMap<DataPipeline, Matrix>();
		
		String matrixFileName = 
				ProjectUtils.getNewFeatureMatrixFileNameForDataPipeline(pipeline);
		featureMatrixFileMap.put(pipeline, matrixFileName);
		featureMatrixMap.put(pipeline, featureMatrix);
	}

	public void removeDataPipeline(DataPipeline pipeline) {

		// Remove all objects related to data pipeline
		libraryMap.remove(pipeline);
		averagedFeatureMap.remove(pipeline);
		featureMap.remove(pipeline);		
		corrMatrixMap.remove(pipeline);
		duplicatesMap.remove(pipeline);
		clusterMap.remove(pipeline);	
		statsCalculatedMap.remove(pipeline);
		corrMatrixMap.remove(pipeline);		

		// Delete data matrix and storage file
		dataMatrixMap.remove(pipeline);
		File dataMatrixFile = Paths.get(getDataDirectory().getAbsolutePath(), 
				dataMatrixFileMap.get(pipeline)).toFile();
		if (dataMatrixFile.exists()) {
			if(!dataMatrixFile.delete())
				System.err.println("Unambel to delete data matrix for " + pipeline.getName());
		}
		dataMatrixFileMap.remove(pipeline);
		
		if(featureMatrixFileMap == null)
			featureMatrixFileMap = new TreeMap<DataPipeline, String>();
		
		if(featureMatrixMap == null)
			featureMatrixMap = new TreeMap<DataPipeline, Matrix>();
		
		//	Delete feature matrix and storage file
		featureMatrixMap.remove(pipeline);
		File featureMatrixFile = Paths.get(getDataDirectory().getAbsolutePath(), 
				featureMatrixFileMap.get(pipeline)).toFile();
		if (featureMatrixFile.exists()) {
			if(!featureMatrixFile.delete())
				System.err.println("Unambel to delete feature matrix for " + pipeline.getName());
		}
		featureMatrixFileMap.remove(pipeline);
		
		//	Remove all data linked to acquisition method 
		//	if it is not part of another data pipeline
		DataAcquisitionMethod acqMethod = pipeline.getAcquisitionMethod();
		DataPipeline anotherPipeline = dataPipelines.stream().filter(p -> !p.equals(pipeline)).
				filter(p -> p.getAcquisitionMethod().equals(acqMethod)).findFirst().orElse(null);
		
		if(anotherPipeline == null) {
			
			Collection<DataFile> filesToRemove = 
					getDataFilesForAcquisitionMethod(acqMethod);
			
			experimentDesign.getSamples().stream().
				forEach(s -> filesToRemove.stream().forEach(f -> s.removeDataFile(f)));

			worklistMap.remove(acqMethod);
		}
		dataPipelines.remove(pipeline);
		
		// Load data for another method or clear GUI
		if (pipeline.equals(activeDataPipeline)) {

			if (dataPipelines.isEmpty()) 
				activeDataPipeline = null;
			else 
				activeDataPipeline = dataPipelines.iterator().next();
		}
	}

	public boolean assayPresent(Assay assay) {
		
		return dataPipelines.stream().
				anyMatch(p -> p.getAssay().equals(assay));
	}

	public void addFeatureSetForDataPipeline(			
			MsFeatureSet featureSet, DataPipeline pipeline) {
		
		Set<MsFeatureSet> dpSets = featureSetMap.get(pipeline);
		if(!dpSets.contains(featureSet))
			dpSets.add(featureSet);
	}

	public void clearMetaDataMap() {
		metaDataMap = new TreeMap<DataPipeline, Matrix[]>();
	}

	public void deleteDataFiles(Set<DataFile> filesToRemove) {

		experimentDesign.removeDataFiles(filesToRemove);
		for(DataFile df : filesToRemove) {
			dataFileMap.get(df.getDataAcquisitionMethod()).remove(df);
			worklistMap.get(df.getDataAcquisitionMethod()).removeDataFile(df);
		}
	}

	public void deleteFeature(MsFeature feature, DataPipeline dataPipeline) {

		featureMap.get(dataPipeline).remove(feature);
		for (MsFeatureSet set : featureSetMap.get(dataPipeline))
			set.removeFeature(feature);
	}

	public void deleteFeatures(
			Collection<MsFeature> featuresToRemove, DataPipeline dataPipeline) {

		featureMap.get(dataPipeline).removeAll(featuresToRemove);
		for (MsFeatureSet set : featureSetMap.get(dataPipeline))
			set.removeFeatures(featuresToRemove);
	}

	public DataPipeline getActiveDataPipeline() {
		return activeDataPipeline;
	}

	public Set<DataFile> getActiveDataFilesForDesignAndAcquisitionMethod(
			ExperimentDesignSubset designSubset, 
			DataAcquisitionMethod acquisitionMethod) {

		Set<DataFile> files = new TreeSet<DataFile>();

		for(ExperimentDesignLevel level : designSubset.getDesignMap()){

			for(ExperimentalSample sample : experimentDesign.getSamples()){

				if(sample.getDesignCell().values().contains(level) &&
						sample.getDataFilesForMethod(acquisitionMethod) != null){

					for(DataFile df : sample.getDataFilesForMethod(acquisitionMethod)){

						if(df.isEnabled())
							files.add(df);
					}
				}
			}			
		}
		return files;
	}
	
	public Set<DataFile> getDataFilesForPipeline(DataPipeline dataPipeline, boolean enabledOnly){
		
		Set<DataFile> files = new TreeSet<DataFile>();
		DataAcquisitionMethod method = dataPipeline.getAcquisitionMethod();
		List<DataFile> methodFiles = experimentDesign.getSamples().stream().
				flatMap(s -> s.getDataFilesForMethod(method).stream()).
				collect(Collectors.toList());
		if(enabledOnly) {
			List<DataFile>enabled = methodFiles.stream().filter(f -> f.isEnabled()).collect(Collectors.toList());
			files.addAll(enabled);
		}
		else {
			files.addAll(methodFiles);
		}		
		return files;
	}
	
	public Set<DataFile> getDataFilesWithDataForPipeline(DataPipeline dataPipeline){
		
		DataAcquisitionMethod method = dataPipeline.getAcquisitionMethod();
		Set<DataFile> methodFiles = experimentDesign.getSamples().stream().
				flatMap(s -> s.getDataFilesForMethod(method).stream()).
				filter(f -> hasDataForFileInPipeline(f, dataPipeline)).
				collect(Collectors.toCollection(TreeSet::new));
	
		return methodFiles;
	}

	public MsFeatureSet getActiveFeatureSetForDataPipeline(DataPipeline pipeline) {

		if(pipeline == null)
			return null;
		
		if(featureSetMap.get(pipeline) == null)
			return null;

		return featureSetMap.get(pipeline).stream().
				filter(s -> s.isActive()).findFirst().orElse(null);
	}
	
	public CompoundLibrary getCompoundLibraryForDataPipeline(DataPipeline pipeline) {
		return libraryMap.get(pipeline);
	}
	
	public CompoundLibrary getAveragedFeatureLibraryForDataPipeline(DataPipeline pipeline) {
		return averagedFeatureMap.get(pipeline);
	}
	
	public Matrix getCorrelationMatrixForDataPipeline(DataPipeline pipeline) {
		return corrMatrixMap.get(pipeline);
	}

	public Set<DataFile> getDataFilesForAcquisitionMethod(
			DataAcquisitionMethod acquisitionMethod) {

		if(acquisitionMethod == null)
			return null;
		
		if(dataFileMap.get(acquisitionMethod) == null)
			return null;

		return dataFileMap.get(acquisitionMethod).
				stream().sorted().collect(Collectors.toSet());
	}

	 public Collection<DataFile> getAllDataFiles(){
		 return dataFileMap.values().stream().
				 flatMap(s -> s.stream()).collect(Collectors.toList());
	 }

	public String getDataMatrixFileNameForDataPipeline(DataPipeline pipeline) {
		return dataMatrixFileMap.get(pipeline);
	}

	public Matrix getDataMatrixForDataPipeline(DataPipeline pipeline) {
		return dataMatrixMap.get(pipeline);
	}
	
	public String getFeatureMatrixFileNameForDataPipeline(DataPipeline pipeline) {
		
		if(featureMatrixFileMap == null)
			return null;
		else
			return featureMatrixFileMap.get(pipeline);
	}
	
	public Matrix getFeatureMatrixForDataPipeline(DataPipeline pipeline) {
		
		if(featureMatrixMap == null)
			return null;
		else
			return featureMatrixMap.get(pipeline);
	}

	public Set<MsFeatureCluster> getDuplicateClustersForDataPipeline(DataPipeline pipeline) {
		return duplicatesMap.get(pipeline);
	}

	public MsFeature getMsFeatureById(String id, DataPipeline pipeline) {

		return featureMap.get(pipeline).stream().
				filter(f -> f.getId().equals(id)).
				findFirst().orElse(null);
	}

	public MsFeature getMsFeatureByName(String featureName, DataPipeline pipeline) {

		return featureMap.get(pipeline).stream().
				filter(f -> f.getName().equals(featureName)).
				findFirst().orElse(null);
	}

	public MsFeature getMsFeatureByBinnerNameMzRt(
			String featureName, DataPipeline pipeline, double mz, double rt) {

		List<MsFeature> matchedByName = featureMap.get(pipeline).stream().
				filter(f -> f.getName().contains(featureName)).collect(Collectors.toList());

		if(matchedByName.size() == 1)
			return matchedByName.get(0);

		List<MsFeature> matchedByRt =
				matchedByName.stream().
				filter(f -> MRC2ToolBoxConfiguration.getRtFormat().
				format(f.getStatsSummary().getMedianObservedRetention()).
				equals(MRC2ToolBoxConfiguration.getRtFormat().format(rt))).
				collect(Collectors.toList());

		if(matchedByRt.size() == 1)
			return matchedByRt.get(0);

		return matchedByRt.stream().
			filter(f -> MRC2ToolBoxConfiguration.getMzFormat().
					format(f.getBasePeakMz()).
					equals(MRC2ToolBoxConfiguration.getMzFormat().format(mz))).
			findFirst().orElse(null);
	}
	
	public boolean hasDataForFileInPipeline(DataFile file, DataPipeline pipeline) {
		
		Matrix datamatrix = dataMatrixMap.get(pipeline);
		if(datamatrix == null)
			return false;
		
		return datamatrix.getRowForLabel(file) > -1;
	}

	public Set<MsFeatureCluster> getMsFeatureClustersForDataPipeline(DataPipeline pipeline) {
		return clusterMap.get(pipeline);
	}

	public Set<MsFeatureSet> getMsFeatureSetsForDataPipeline(DataPipeline pipeline) {
		return featureSetMap.get(pipeline);
	}
	
	public Set<MsFeatureSet> getUnlockedMsFeatureSetsForDataPipeline(DataPipeline pipeline) {
		return featureSetMap.get(pipeline).stream().
				filter(s -> !s.isLocked()).collect(Collectors.toCollection(TreeSet::new));
	}

	public Set<MsFeature> getMsFeaturesForDataPipeline(DataPipeline pipeline) {
		return featureMap.get(pipeline);
	}

	public Matrix getImputedDataMatrixForDataPipeline(DataPipeline pipeline) {

		if(imputedDataMatrixMap == null)
			imputedDataMatrixMap = new TreeMap<DataPipeline, Matrix>();

		return imputedDataMatrixMap.get(pipeline);
	}

	public Matrix getMetaDataMatrixForDataPipeline(DataPipeline pipeline, int dimension) {

		if (metaDataMap.containsKey(pipeline))
			return metaDataMap.get(pipeline)[dimension];
		else
			return null;
	}

	public boolean statsCalculetedForDataPipeline(DataPipeline pipeline) {
		
		if(pipeline == null)
			throw new IllegalArgumentException("Pipeline value can not be null!");
		
		return statsCalculatedMap.get(pipeline);
	}

	public Worklist getWorklistForDataAcquisitionMethod(DataAcquisitionMethod method) {
		return worklistMap.get(method);
	}

	public boolean correlationClustersCalculatedForDataPipeline(DataPipeline pipeline) {

		if (clusterMap.get(pipeline) == null)
			return false;

		if (!clusterMap.get(pipeline).isEmpty())
			return true;

		return false;
	}

	public boolean dataPipelineHasData(DataPipeline pipeline) {
		return dataMatrixMap.get(pipeline) != null;
	}

	public boolean hasDuplicateClusters(DataPipeline pipeline) {

		if(duplicatesMap.get(pipeline) == null)
			return false;

		if (!duplicatesMap.get(pipeline).isEmpty())
			return true;

		return false;
	}

	public boolean dataPipelineHasLinkedLibrary(DataPipeline pipeline) {

		if(pipeline == null)
			throw new IllegalArgumentException("Data pipeline can not be null!");

		return libraryMap.containsKey(pipeline);
	}

	public boolean acquisitionMethodHasLinkedWorklist(DataAcquisitionMethod method) {
		return worklistMap.containsKey(method);
	}
	
	public MsFeatureSet getAllFeaturesSetFordataPipeline(DataPipeline pipeline) {
		
		String dsName = GlobalDefaults.ALL_FEATURES.getName();
		return featureSetMap.get(pipeline).stream().
				filter(s -> s.getName().equals(dsName)).findFirst().orElse(null);
	}
	
	public Set<MsFeatureSet> getCustomSetsForDataPipeline(DataPipeline pipeline) {
		
		Set<MsFeatureSet> dpfeatureSets = featureSetMap.get(pipeline);
		MsFeatureSet dpAllFeatureSet = getAllFeaturesSetFordataPipeline(pipeline);
		Set<MsFeatureSet> customSets = 
				dpfeatureSets.stream().
				filter(s -> !s.equals(dpAllFeatureSet)).
				collect(Collectors.toSet());
		return customSets;
	}
	
	public void removeWorklistForMethod(DataAcquisitionMethod method) {
		
		getDataFilesForAcquisitionMethod(method).
			stream().forEach(f -> f.clearInjectionData());
		worklistMap.remove(method);
	}

	public void restoreData() {

//		for (DataPipeline pipeline : dataPipelines) {
//
//			// Restore correlation matrix if clusters are present
//			if (clusterMap.get(pipeline) != null)
//				corrMatrixMap.put(pipeline, 
//						dataMatrixMap.get(pipeline).corrcoef(Ret.LINK, false, false));
//		}
		for (DataPipeline pipeline : dataPipelines) {
			
			for(MsFeature feature : featureMap.get(pipeline)) {
				
				if(feature.getName().startsWith(DataPrefix.MS_LIBRARY_UNKNOWN_TARGET.getName())) {
					
					String featureName = DataPrefix.MS_LIBRARY_UNKNOWN_TARGET.getName() +
							MRC2ToolBoxConfiguration.getMzFormat().format(feature.getMonoisotopicMz()) + "_" +
							MRC2ToolBoxConfiguration.getRtFormat().format(feature.getRetentionTime());
					if(feature.getSpectrum() != null && feature.getSpectrum().getPrimaryAdduct() != null)
						featureName += " " + feature.getSpectrum().getPrimaryAdduct().getName();
					
					feature.setName(featureName);
				}
			}
		}
	}

	public void setActiveDataPipeline(DataPipeline activePipeline) {
		
		this.activeDataPipeline = activePipeline;
		
		if(dataMatrixMap.get(activeDataPipeline) == null)
			ProjectUtils.loadDataMatrixForPipeline(this, activeDataPipeline);
	}

	public void setActiveFeatureSetForDataPipeline(
			MsFeatureSet activeSet, DataPipeline pipeline) {

		MsFeatureSet currentActiveSet = getActiveFeatureSetForDataPipeline(pipeline);
		if(currentActiveSet != null && currentActiveSet.equals(activeSet))
			return;
		
		if(!featureSetMap.get(pipeline).contains(activeSet))
			featureSetMap.get(pipeline).add(activeSet);
		
		featureSetMap.get(pipeline).stream().forEach(s -> {
			s.setSuppressEvents(true);
			s.setActive(false);
			s.setSuppressEvents(false);
		});
		if(!activeSet.isActive())
			activeSet.setActive(true);
	}

	public void setCompoundLibraryForDataPipeline(
			DataPipeline pipeline, CompoundLibrary library) {
		libraryMap.put(pipeline, library);
	}

	public TreeMap<DataPipeline, CompoundLibrary> getAveragedFeatureMap() {
		return averagedFeatureMap;
	}
	
	public void setAveragedFeatureLibraryForDataPipeline(
			DataPipeline pipeline, CompoundLibrary library) {
		
		if(averagedFeatureMap == null)
			averagedFeatureMap = new TreeMap<DataPipeline, CompoundLibrary>();
		averagedFeatureMap.put(pipeline, library);
	}
	
	public void addDataFilesForAcquisitionMethod(
			DataAcquisitionMethod acquisitionMethod, Collection<DataFile> fileSet) {
		
		dataFileMap.computeIfAbsent(
				acquisitionMethod, v -> new TreeSet<DataFile>());			
		dataFileMap.get(acquisitionMethod).addAll(fileSet);
	}

	public void setDataMatrixForDataPipeline(
			DataPipeline pipeline, Matrix dataMatrix) {
		
		dataMatrixMap.put(pipeline, dataMatrix);
		
		Matrix[] mdata = new Matrix[2];
		mdata[0] = dataMatrix.getMetaDataDimensionMatrix(0);
		mdata[1] = dataMatrix.getMetaDataDimensionMatrix(1);
		metaDataMap.put(pipeline, mdata);
	}
	
	public void setFeatureMatrixForDataPipeline(
			DataPipeline pipeline, Matrix featureMatrix) {	
		
		if(featureMatrixMap == null)
			featureMatrixMap = new TreeMap<DataPipeline, Matrix>();
		
		featureMatrixMap.put(pipeline, featureMatrix);
	}

	public void setCorrelationMatrixForDataPipeline(
			DataPipeline pipeline, Matrix corrMatrix) {
		corrMatrixMap.put(pipeline, corrMatrix);
	}
	
	public void setDuplicateClustersForDataPipeline(
			DataPipeline pipeline, Collection<MsFeatureCluster> clusterSet) {
		duplicatesMap.put(pipeline, new HashSet<MsFeatureCluster>(clusterSet));
	}
	public void setFeatureClustersForDataPipeline(
			DataPipeline pipeline, Set<MsFeatureCluster> clusterSet) {
		clusterMap.put(pipeline, clusterSet);
	}

	public void setFeaturesForDataPipeline(
			DataPipeline pipeline, Set<MsFeature> featureSet) {
		featureMap.put(pipeline, featureSet);
	}

	public void setImputedDataMatrixForDataPipeline(
			DataPipeline pipeline, Matrix dataMatrix) {

		if(imputedDataMatrixMap == null)
			imputedDataMatrixMap = new TreeMap<DataPipeline, Matrix>();

		imputedDataMatrixMap.put(pipeline, dataMatrix);
	}

	public void setStatisticsStatusForDataPipeline(
			DataPipeline pipeline, boolean statStatus) {
		statsCalculatedMap.put(pipeline, statStatus);
	}

	public void setWorklistForAcquisitionMethod(
			DataAcquisitionMethod method, Worklist newWorklist) {
		worklistMap.put(method, newWorklist);
	}

	//	TODO when needed
	public void updateFeatureDataFromLibrary(Assay method) {
  
//		if (libraryMap.get(method) != null) {
//
//			for (MsFeature cf : featureMap.get(method)) {
//
//				MsFeature lf = libraryMap.get(method).getFeatureByName(cf.getName());
//				if (lf != null) {
//
//					// Update library spectrum
//					cf.setSpectrum(lf.getSpectrum());
//					for (MsFeatureIdentity cid : lf.getIdentifications())
//						cf.addIdentity(cid);
//				}
//			}
//		}
	}

	public Set<MsFeatureClusterSet> getDataIntegrationClusterSets() {

		if(dataIntegrationSets == null)
			dataIntegrationSets = new TreeSet<MsFeatureClusterSet>();

		return dataIntegrationSets;
	}

	public boolean allDataFilesForAcquisitionMethodEnabled(DataAcquisitionMethod method) {
		
		return experimentDesign.getSamples().stream().
				filter(s -> Objects.nonNull(s.getDataFilesForMethod(method))).
				flatMap(s -> s.getDataFilesForMethod(method).stream()).
				filter(s -> !s.isEnabled()).
				collect(Collectors.toSet()).isEmpty();
	}

	public boolean projectHasData() {

		for(DataPipeline pipeline : dataPipelines) {
			if (dataMatrixMap.get(pipeline) != null)
				return true;
		}
		return false;
	}

	public void replaceAssayMethod(DataPipeline oldPipeline, DataPipeline newPipeline) {

		//	Update assay references in data files
		DataAcquisitionMethod oldMethod = oldPipeline.getAcquisitionMethod();
		DataAcquisitionMethod newMethod = newPipeline.getAcquisitionMethod();
		dataFileMap.get(oldMethod).stream().
			forEach(f -> f.setDataAcquisitionMethod(newMethod));

		//	Replace method in design
		experimentDesign.getSamples().stream().
			forEach(es -> es.replaceDataAcquisitionMethod(oldMethod, newMethod));

		// Update all maps with method key
		dataPipelines.remove(oldPipeline);
		dataPipelines.add(newPipeline);

		if(activeDataPipeline.equals(oldPipeline))
				activeDataPipeline = newPipeline;

		CompoundLibrary lib = libraryMap.remove(oldPipeline);
		libraryMap.put(newPipeline, lib);
		
		CompoundLibrary avgLib = averagedFeatureMap.remove(oldPipeline);
		averagedFeatureMap.put(newPipeline, avgLib);

		Set<MsFeature>features = featureMap.remove(oldPipeline);
		featureMap.put(newPipeline, features);

		Matrix dataMatrix = dataMatrixMap.remove(oldPipeline);
		dataMatrixMap.put(newPipeline, dataMatrix);

		Matrix imputedMatrix = imputedDataMatrixMap.remove(oldPipeline);
		imputedDataMatrixMap.put(newPipeline, imputedMatrix);

		Matrix corrMatrix = corrMatrixMap.remove(oldPipeline);
		corrMatrixMap.put(newPipeline, corrMatrix);

		Matrix[] metaData = metaDataMap.remove(oldPipeline);
		metaDataMap.put(newPipeline, metaData);

		Set<MsFeatureCluster> duplicates = duplicatesMap.remove(oldPipeline);
		duplicatesMap.put(newPipeline, duplicates);

		Set<MsFeatureCluster> clusters = clusterMap.remove(oldPipeline);
		clusterMap.put(newPipeline, clusters);

		String dmfile = dataMatrixFileMap.remove(oldPipeline);
		dataMatrixFileMap.put(newPipeline, dmfile);

		Worklist wklist = worklistMap.remove(oldMethod);
		worklistMap.put(newMethod, wklist);

		Set<DataFile> fileSet = dataFileMap.remove(oldMethod);
		dataFileMap.put(newMethod, fileSet);

		Boolean statCalc = statsCalculatedMap.remove(oldPipeline);
		statsCalculatedMap.put(newPipeline, statCalc);

		Set<MsFeatureSet> featureSets = featureSetMap.remove(oldPipeline);
		featureSetMap.put(newPipeline, featureSets);
	}

	public boolean isDataAcquisitionMultiBatch(DataAcquisitionMethod method) {

		return dataFileMap.get(method).stream().
					mapToInt(f -> f.getBatchNumber()).distinct().count() > 1;
	}

	public int[] getBatchListForAcquisitionMethod(DataAcquisitionMethod method) {

		return dataFileMap.get(method).stream().
				mapToInt(f -> f.getBatchNumber()).distinct().sorted().toArray();
	}

	public void addIntegratedFeatureClusterSet(MsFeatureClusterSet newSet) {

		if(newSet.isActive())
			dataIntegrationSets.stream().forEach(s -> s.setActive(false));

		dataIntegrationSets.add(newSet);
	}

	public void deleteIntegratedFeatureClusterSet(MsFeatureClusterSet theSet) {

		dataIntegrationSets.remove(theSet);

		if(theSet.isActive() && !dataIntegrationSets.isEmpty())
				dataIntegrationSets.iterator().next().setActive(true);		
	}

	public Set<MsFeatureClusterSet>getIntergratedFeatureSets(){
		return dataIntegrationSets;
	}

	public MsFeatureClusterSet getActiveIntegratedFeatureSet() {

		return dataIntegrationSets.stream().
				filter(s -> s.isActive()).
				findFirst().orElse(null);
	}

	public void setActiveIntegratedFeatureSet(MsFeatureClusterSet activeSet) {

		dataIntegrationSets.stream().forEach(s -> s.setActive(false));
		activeSet.setActive(true);
		if(!dataIntegrationSets.contains(activeSet))
			dataIntegrationSets.add(activeSet);
	}
	
	public DataFile getDataFileByNameAndMethod(
			String name, DataAcquisitionMethod method) {
		Set<DataFile> filesForMethod = dataFileMap.get(method);
		if(filesForMethod == null || filesForMethod.isEmpty())
			return null;
		else {
			String baseName = FileNameUtils.getBaseName(name);
			return filesForMethod.stream().
					filter(f -> FileNameUtils.getBaseName(f.getName()).equals(baseName)).
					findFirst().orElse(null);
		}
	}

	@Override
	public Set<ExperimentalSample> getPooledSamples() {
		
		return experimentDesign.getSamples().stream().
				filter(s -> s.isIncloodeInPoolStats()).
				collect(Collectors.toCollection(TreeSet::new));
	}
	
	public Matrix getDataMatrixForFeatureSetAndDesign(
			MsFeatureSet featureSet,
			ExperimentDesignSubset designSubset,
			DataPipeline dataPipeline) {
		
		Matrix allData = getDataMatrixForDataPipeline(dataPipeline);
		if(allData == null)
			return null;
		
		if(featureSet == null 
				|| featureSet.getFeatures().isEmpty()
				|| designSubset == null)
			return null;
		
		Set<DataFile>dataFiles = 
				getActiveDataFilesForDesignAndAcquisitionMethod(
				 designSubset, 
				 dataPipeline.getAcquisitionMethod());
		if(dataFiles.isEmpty())
			return null;
		
		long[] featureCoordinates = new long[featureSet.getFeatures().size()];
		long[] dataFileCoordinates = new long[dataFiles.size()];	
	
		int counter = 0;
		for (MsFeature cf : featureSet.getFeatures()) {
			featureCoordinates[counter] = allData.getColumnForLabel(cf);
			counter++;
		}
		counter = 0;
		for (DataFile df : dataFiles) {
			dataFileCoordinates[counter] = allData.getRowForLabel(df);
			counter++;
		}
		Matrix subsetMatrix = allData.select(
				Ret.NEW, dataFileCoordinates, featureCoordinates);
		
		Matrix featureMetaData = allData.getMetaDataDimensionMatrix(0).selectColumns(
				Ret.NEW, featureCoordinates);
		Matrix fileMetaData = allData.getMetaDataDimensionMatrix(1).selectRows(
				Ret.NEW, dataFileCoordinates);
		
		subsetMatrix.setMetaDataDimensionMatrix(0, featureMetaData);
		subsetMatrix.setMetaDataDimensionMatrix(1, fileMetaData);
		
		return subsetMatrix;
	}
		
	public DataAnalysisProject(Element projectElement) {
		
		super(projectElement);
		initProjectFields(false);
		List<Element>dataPipelineList = 
				projectElement.getChild(MetabolomicsProjectFields.DataPipelineList.name()).
				getChildren(ObjectNames.DataPipeline.name());
		for(Element dpe : dataPipelineList) {
			
			DataPipeline dp = null;
			try {
				dp = new DataPipeline(dpe);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(dp != null)
				addDataPipeline(dp);
		}
	}
	
	@Override
    public Element getXmlElement() {
    	
    	Element metabolomicsProjectElement = super.getXmlElement();
    	metabolomicsProjectElement.setName(ObjectNames.MetabolomicsProject.name());
    	
    	//	Remove LIMS design as redundant
    	Element limsExperiemntElement = 
    			metabolomicsProjectElement.getChild(ObjectNames.limsExperiment.name());
    	if(limsExperiemntElement != null) 
    		limsExperiemntElement.removeChild(ObjectNames.ExperimentDesign.name());
    	
    	//	Add data pipelines
    	Element dataPipelineListElement = 
    			new Element(MetabolomicsProjectFields.DataPipelineList.name());
    	for(DataPipeline dp : dataPipelines)
    		dataPipelineListElement.addContent(dp.getXmlElement());
    	
    	metabolomicsProjectElement.addContent(dataPipelineListElement);

    	return metabolomicsProjectElement;
	}
	
	public Collection<DataPipeline>getPipelinesForDataAcquisitionMethod(DataAcquisitionMethod method){
		
		return dataPipelines.stream().
				filter(p -> p.getAcquisitionMethod().equals(method)).
				collect(Collectors.toList());
	}

}





























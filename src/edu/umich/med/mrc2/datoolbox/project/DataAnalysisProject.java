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
import java.util.UUID;
import java.util.stream.Collectors;

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

public class DataAnalysisProject extends Experiment {

	/**
	 *
	 */
	private static final long serialVersionUID = 618045235335401590L;

	protected LIMSProject limsProject;
	protected LIMSExperiment limsExperiment;
	protected ProjectPreferences projectPreferences;
	

	protected TreeSet<DataPipeline> dataPipelines;
	protected DataPipeline activeDataPipeline;
	
	protected TreeMap<DataPipeline, CompoundLibrary> libraryMap;
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
				projectDescription, 
				parentDirectory);

		createDirectoryStructureForNewExperiment(parentDirectory);
		initNewProject();		
	}
	
	@Override
	protected void createDirectoryStructureForNewExperiment(File parentDirectory) {
		
		super.createDirectoryStructureForNewExperiment(parentDirectory);
		experimentFile = 
				Paths.get(experimentDirectory.getAbsolutePath(), name.replaceAll("\\W+", "-") + "."
				+ MRC2ToolBoxConfiguration.EXPERIMENT_FILE_EXTENSION).toFile();
	}
	
	public void updateExperimentLocation(File newProjectFile) {
		
		experimentFile = newProjectFile;
		experimentDirectory = newProjectFile.getParentFile();
		exportsDirectory = Paths.get(experimentDirectory.getAbsolutePath(), 
				MRC2ToolBoxConfiguration.DATA_EXPORT_DIRECTORY).toFile();	
	}
		
	protected void initNewProject() {

		projectPreferences = new ProjectPreferences();
		libraryMap = new TreeMap<DataPipeline, CompoundLibrary>();
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
		activeDataPipeline = null;
		experimentDesign = new ExperimentDesign();
		featureSetMap = new TreeMap<DataPipeline, Set<MsFeatureSet>>();
		dataIntegrationSets = new TreeSet<MsFeatureClusterSet>();
		metaDataMap = new TreeMap<DataPipeline, Matrix[]>();
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

	public void setLimsExperiment(LIMSExperiment limsExperiment) {
		this.limsExperiment = limsExperiment;
	}

//	public File getExportsDirectory() {
//
//		exportsDirectory = Paths.get(experimentDirectory.getAbsolutePath(), 
//				MRC2ToolBoxConfiguration.DATA_EXPORT_DIRECTORY).toFile();
//
//		if(!exportsDirectory.exists()) {
//
//			if (!createProjectDirectory(exportsDirectory)) {
//				MessageDialog.showWarningMsg("Failed to create exports directory");
//				return null;
//			}
//		}
//		return exportsDirectory;
//	}
	
//	protected boolean createProjectDirectory(File projDir) {		
//		try {
//			Files.createDirectories(Paths.get(projDir.getAbsolutePath()));
//			return true;
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			return false;
//		}
//	}

	public void recreateMatrixMaps() {
		dataMatrixMap = new TreeMap<DataPipeline, Matrix>();
		corrMatrixMap = new TreeMap<DataPipeline, Matrix>();
		featureMatrixMap = new TreeMap<DataPipeline, Matrix>();
	}

	public void setProjectFile(File newProjectFile) {
		experimentFile = newProjectFile;
		experimentDirectory = experimentFile.getParentFile();
		exportsDirectory = getExportsDirectory();
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
		String matrixFileName = DataPrefix.DATA_MATRIX.getName() + UUID.randomUUID().toString() + "." + 
				MRC2ToolBoxConfiguration.DATA_MATRIX_EXTENSION;
		dataMatrixFileMap.put(pipeline, matrixFileName);
		statsCalculatedMap.put(pipeline, false);
		featureSetMap.put(pipeline, new TreeSet<MsFeatureSet>());
	}
	
	public void addFeatureMatrixForDataPipeline(DataPipeline pipeline, Matrix featureMatrix) {
		
		if(featureMatrixFileMap == null)
			featureMatrixFileMap = new TreeMap<DataPipeline, String>();
		
		if(featureMatrixMap == null)
			featureMatrixMap = new TreeMap<DataPipeline, Matrix>();
		
		String matrixFileName = DataPrefix.FEATURE_MATRIX.getName() + UUID.randomUUID().toString() + "." + 
				MRC2ToolBoxConfiguration.DATA_MATRIX_EXTENSION;
		featureMatrixFileMap.put(pipeline, matrixFileName);
		featureMatrixMap.put(pipeline, featureMatrix);
	}

	public void removeDataPipeline(DataPipeline pipeline) {

		// Remove all objects related to data pipeline
		libraryMap.remove(pipeline);
		featureMap.remove(pipeline);		
		corrMatrixMap.remove(pipeline);
		duplicatesMap.remove(pipeline);
		clusterMap.remove(pipeline);	
		statsCalculatedMap.remove(pipeline);
		corrMatrixMap.remove(pipeline);		

		// Delete data matrix and storage file
		dataMatrixMap.remove(pipeline);
		File dataMatrixFile = Paths.get(experimentDirectory.getAbsolutePath(), 
				dataMatrixFileMap.get(pipeline)).toFile();
		if (dataMatrixFile.exists())
			dataMatrixFile.delete();

		dataMatrixFileMap.remove(pipeline);
		
		if(featureMatrixFileMap == null)
			featureMatrixFileMap = new TreeMap<DataPipeline, String>();
		
		if(featureMatrixMap == null)
			featureMatrixMap = new TreeMap<DataPipeline, Matrix>();
		
		//	Delete feature matrix and storage file
		featureMatrixMap.remove(pipeline);
		File featureMatrixFile = Paths.get(experimentDirectory.getAbsolutePath(), 
				featureMatrixFileMap.get(pipeline)).toFile();
		if (featureMatrixFile.exists())
			featureMatrixFile.delete();

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
				filter(p -> p.getAssay().equals(assay)).
				findFirst().isPresent();
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

//	public void createMetaDataMaps() {
//		
//		if(metaDataMap == null)
//			metaDataMap = new TreeMap<DataPipeline, Matrix[]>();
//			
//		for (Entry<DataPipeline, Matrix> entry : dataMatrixMap.entrySet()) {
//
//			Matrix[] mdata = new Matrix[2];
//			mdata[0] = entry.getValue().getMetaDataDimensionMatrix(0);
//			mdata[1] = entry.getValue().getMetaDataDimensionMatrix(1);
//			metaDataMap.put(entry.getKey(), mdata);
//		}
//	}

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

				if(sample.getDesignCell().values().contains(level)){

					if(sample.getDataFilesForMethod(acquisitionMethod) != null){

						for(DataFile df : sample.getDataFilesForMethod(acquisitionMethod)){

							if(df.isEnabled())
								files.add(df);
						}
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
	
	public void removeWorklistForMethod(DataAcquisitionMethod method) {
		
		getDataFilesForAcquisitionMethod(method).
			stream().forEach(f -> f.clearInjectionData());
		worklistMap.remove(method);
	}

	public void restoreData() {

		for (DataPipeline pipeline : dataPipelines) {

			// Restore correlation matrix if clusters are present
			if (clusterMap.get(pipeline) != null)
				corrMatrixMap.put(pipeline, 
						dataMatrixMap.get(pipeline).corrcoef(Ret.LINK, false, false));
		}
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

	public void setDataFilesForAcquisitionMethod(
			DataAcquisitionMethod acquisitionMethod, Collection<DataFile> fileSet) {
				
		dataFileMap.put(acquisitionMethod, new TreeSet<DataFile>(fileSet));
	}
	
	public void addDataFilesForAcquisitionMethod(
			DataAcquisitionMethod acquisitionMethod, Collection<DataFile> fileSet) {
		
		if(!dataFileMap.containsKey(acquisitionMethod))
			dataFileMap.put(acquisitionMethod, new TreeSet<DataFile>());
			
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

		if(theSet.isActive()) {
			if(!dataIntegrationSets.isEmpty())
				dataIntegrationSets.iterator().next().setActive(true);
		}
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

	@Override
	public Set<ExperimentalSample> getPooledSamples() {
		
		if(pooledSamples == null) {
			
			pooledSamples = new TreeSet<ExperimentalSample>();	
			if(experimentDesign != null) {
				
				experimentDesign.getSamples().stream().
					filter(s -> s.isIncloodeInPoolStats()).forEach(s -> pooledSamples.add(s));
			}
		}
		return pooledSamples;
	}
}





























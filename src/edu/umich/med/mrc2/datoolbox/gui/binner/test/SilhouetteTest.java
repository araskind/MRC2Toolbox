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

package edu.umich.med.mrc2.datoolbox.gui.binner.test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.commons.math3.ml.clustering.CentroidCluster;
import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer;
import org.apache.commons.math3.stat.correlation.KendallsCorrelation;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;
import org.apache.commons.math3.util.MathArrays;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.ujmp.core.BaseMatrix;
import org.ujmp.core.Matrix;
import org.ujmp.core.calculation.Calculation.Ret;
import org.ujmp.core.util.MathUtil;

import edu.umich.med.mrc2.datoolbox.data.BinnerPreferencesObject;
import edu.umich.med.mrc2.datoolbox.data.ClusterableFeatureData;
import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesign;
import edu.umich.med.mrc2.datoolbox.data.LibraryMsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureCluster;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureDataPoint;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureSet;
import edu.umich.med.mrc2.datoolbox.data.Worklist;
import edu.umich.med.mrc2.datoolbox.data.WorklistItem;
import edu.umich.med.mrc2.datoolbox.data.compare.MsFeatureComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.enums.GlobalDefaults;
import edu.umich.med.mrc2.datoolbox.data.enums.Polarity;
import edu.umich.med.mrc2.datoolbox.data.lims.DataAcquisitionMethod;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSExperiment;
import edu.umich.med.mrc2.datoolbox.gui.binner.control.BinClusteringCutoffType;
import edu.umich.med.mrc2.datoolbox.gui.binner.control.BinnerParameters;
import edu.umich.med.mrc2.datoolbox.gui.binner.control.ClusterGroupingMethod;
import edu.umich.med.mrc2.datoolbox.gui.binner.control.CorrelationFunctionType;
import edu.umich.med.mrc2.datoolbox.main.ReferenceSamplesManager;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.project.ProjectType;
import edu.umich.med.mrc2.datoolbox.project.store.MetabolomicsProjectFields;
import edu.umich.med.mrc2.datoolbox.project.store.ObjectNames;
import edu.umich.med.mrc2.datoolbox.utils.DataSetUtils;
import edu.umich.med.mrc2.datoolbox.utils.ProjectUtils;
import edu.umich.med.mrc2.datoolbox.utils.ujmp.ImputeMedian;
import edu.umich.med.mrc2.datoolbox.utils.ujmp.Log1pTransform;
import jsat.SimpleDataSet;
import jsat.classifiers.DataPoint;
import jsat.clustering.HDBSCAN;

public class SilhouetteTest {
	
	private File projectFile;
	private String methodPrefix;
	
	private BinnerPreferencesObject bpo;
	private DataAnalysisProject currentExperiment;
	private DataPipeline activeDataPipeline;
	
	private Map<DataPipeline,String[]>orderedDataFileNamesMap;
	private Map<DataPipeline,String[]>orderedMSFeatureIdMap;
	private String[]orderedDataFileNames;
	private String[]orderedMSFeatureIds;	
	
	private Matrix preparedDataMatrix;
	private Collection<MsFeature> cleanRTSortedFeatureSet;
	private Collection<MsFeatureCluster> featureBins;
	private Collection<MsFeatureCluster> featureClusters;
	private PearsonsCorrelation pearson;
	private SpearmansCorrelation spearman;
	private KendallsCorrelation kendall;

	public SilhouetteTest(File projectFile, String methodPrefix) {
		super();
		this.projectFile = projectFile;
		this.methodPrefix = methodPrefix;
	}

	public void parseProject() {
		
		SAXBuilder sax = new SAXBuilder();
		Document doc = null;
		try {
			doc = sax.build(projectFile);
		} catch (JDOMException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return;						
		}
		Element projectElement = null;
		try {
			projectElement = doc.getRootElement();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(projectElement == null)
			return;
		
		currentExperiment = new DataAnalysisProject(projectElement, projectFile);
		currentExperiment.setProjectType(ProjectType.DATA_ANALYSIS_NEW_FORMAT);
		
		if(currentExperiment.getDataPipelines().isEmpty()) {
			parseExperimentDesign(projectElement);
			return;
		}
		parseAcquisitionMethodDataFileMap(projectElement);
		
		parseExperimentDesign(projectElement);
		parseOrderedFileNameMap(projectElement);
		parseOrderedMSFeatureIdMap(projectElement);
		recreateWorklists(projectElement);
				
		activeDataPipeline = currentExperiment.getDataPipelines().stream().
			filter(p -> p.getSaveSafeName().equals(methodPrefix)).
			findFirst().orElse(null);
		
		if(activeDataPipeline == null) {
			System.err.println("No pipeline for " + methodPrefix);
			return;
		}
		readFeatureData();
	}
	
	private void readFeatureData() {
		
		File featureXmlFile = 
				ProjectUtils.getFeaturesFilePath(
						currentExperiment, activeDataPipeline).toFile();		
		if(!featureXmlFile.exists())
			return;
		
		Set<MsFeature>featureSet = new HashSet<MsFeature>();
		try {
			SAXBuilder sax = new SAXBuilder();
			Document doc = sax.build(featureXmlFile);
			Element rootNode = doc.getRootElement();

			List<Element> featureElementList = 
					rootNode.getChildren(ObjectNames.LibraryMsFeature.name());					
			for (Element featureElement : featureElementList)
				featureSet.add(new LibraryMsFeature(featureElement));
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		currentExperiment.setFeaturesForDataPipeline(activeDataPipeline, featureSet);

		MsFeatureSet allFeatures = 
				new MsFeatureSet(GlobalDefaults.ALL_FEATURES.getName(),	
						currentExperiment.getMsFeaturesForDataPipeline(activeDataPipeline));
		allFeatures.setActive(true);
		allFeatures.setLocked(true);
		currentExperiment.addFeatureSetForDataPipeline(allFeatures, activeDataPipeline);
		createDataMatrix();
	}
	
	private void createDataMatrix() {
		
		orderedDataFileNames = orderedDataFileNamesMap.get(activeDataPipeline);
		orderedMSFeatureIds = orderedMSFeatureIdMap.get(activeDataPipeline);
		
		Matrix featureMetaDataMatrix = createFeatureMetaDataMatrix();
		Matrix dataFileMetaDataMatrix = createDataFileMetaDataMatrix();
		Matrix dataMatrix = ProjectUtils.loadDataMatrixForPipelineWitoutMetaData(
				currentExperiment, activeDataPipeline);
		if(dataMatrix != null) {
			
			dataMatrix.setMetaDataDimensionMatrix(0, featureMetaDataMatrix);
			dataMatrix.setMetaDataDimensionMatrix(1, dataFileMetaDataMatrix);
			currentExperiment.setDataMatrixForDataPipeline(activeDataPipeline, dataMatrix);
		}
	}
	
	private Matrix createFeatureMetaDataMatrix() {
		
		MsFeature[]featureArray = new MsFeature[orderedMSFeatureIds.length];
		Set<MsFeature>featureSet = currentExperiment.getMsFeaturesForDataPipeline(activeDataPipeline);
		for(int i=0; i<orderedMSFeatureIds.length; i++) {
			
			String featureId = orderedMSFeatureIds[i];
			MsFeature feature = featureSet.stream().
					filter(f -> f.getId().equals(featureId)).
					findFirst().orElse(null);
			featureArray[i] = feature;
		}
		return Matrix.Factory.linkToArray((Object[])featureArray);
	}
	
	private Matrix createDataFileMetaDataMatrix() {
		
		DataFile[]dataFileArray = new DataFile[orderedDataFileNames.length];
		Set<DataFile>dataFileSet = 
				currentExperiment.getDataFilesForAcquisitionMethod(activeDataPipeline.getAcquisitionMethod());
		for(int i=0; i<orderedDataFileNames.length; i++) {
			
			String fileName = orderedDataFileNames[i];
			DataFile df = dataFileSet.stream().
					filter(f -> f.getName().equals(fileName)).
					findFirst().orElse(null);
			dataFileArray[i] = df;
		}
		return Matrix.Factory.linkToArray((Object[])dataFileArray).transpose(Ret.NEW);
	}
		
	private void parseExperimentDesign(Element projectElement) {
		
		Element designElement = projectElement.getChild(
				ObjectNames.ExperimentDesign.name());
		
		ExperimentDesign experimentDesign = null;
		if(designElement != null)
			experimentDesign  = new ExperimentDesign(designElement, currentExperiment);
		
		if(experimentDesign != null)
			currentExperiment.setExperimentDesign(experimentDesign);
		else
			currentExperiment.setExperimentDesign(new ExperimentDesign());
		
		Element limsExperimentElement = projectElement.getChild(
				ObjectNames.limsExperiment.name());
		
		LIMSExperiment limsExperiment = null;
		if(limsExperimentElement != null) {
			limsExperiment = new LIMSExperiment(limsExperimentElement, currentExperiment);
			currentExperiment.setLimsExperiment(limsExperiment);
		}
	}

	private void parseAcquisitionMethodDataFileMap(Element projectElement) {
		
    	Set<DataAcquisitionMethod>acquisitionMethods = 
    			currentExperiment.getDataPipelines().stream().
    			map(p -> p.getAcquisitionMethod()).collect(Collectors.toSet());
    	
    	List<Element> methodDataFileMapElementList = 
    			projectElement.getChild(MetabolomicsProjectFields.MethodDataFileMap.name()).
    			getChildren(MetabolomicsProjectFields.MethodDataFileMapItem.name());
    	for(Element methodDataFileMapElement : methodDataFileMapElementList) {
    		
    		String methodId = methodDataFileMapElement.getAttributeValue(
    				MetabolomicsProjectFields.DataAcquisitionMethodId.name());
    		DataAcquisitionMethod method = acquisitionMethods.stream().
    				filter(m -> m.getId().equals(methodId)).
    				findFirst().orElse(null);
    		if(method == null)
				return;
    		
    		List<Element> dataFileElementList = 
    				methodDataFileMapElement.getChildren(ObjectNames.DataFile.name());
    		List<DataFile>dataFileList = new ArrayList<DataFile>();
    		for(Element dfElement : dataFileElementList)
    			dataFileList.add(new DataFile(dfElement));
    		   		
    		currentExperiment.addDataFilesForAcquisitionMethod(method, dataFileList);
    	}
	}
	
	private void parseOrderedFileNameMap(Element projectElement) {
		
		orderedDataFileNamesMap = new TreeMap<DataPipeline,String[]>();
    	List<Element> orderedFileListElementList = 
    			projectElement.getChild(MetabolomicsProjectFields.FileIdMap.name()).
    			getChildren(MetabolomicsProjectFields.FileIdList.name());
		
    	for(Element orderedFileListElement : orderedFileListElementList) {
    		
    		String pipelineName = orderedFileListElement.getAttributeValue(
    				MetabolomicsProjectFields.DataPipelineId.name());
    		if(pipelineName != null && !pipelineName.isEmpty()) {
    			
    			DataPipeline dp = currentExperiment.getDataPipelines().stream().
    				filter(p -> p.getSaveSafeName().equals(pipelineName)).
    				findFirst().orElse(null);
    			
    			String[]fileNames = orderedFileListElement.getText().split(",");
    			orderedDataFileNamesMap.put(dp, fileNames);
    		}
    	}
	}
	
	private void parseOrderedMSFeatureIdMap(Element projectElement) {
		
		orderedMSFeatureIdMap = new  TreeMap<DataPipeline,String[]>();
    	List<Element> orderedMSFeatureIdElementList = 
    			projectElement.getChild(MetabolomicsProjectFields.MSFeatureIdMap.name()).
    			getChildren(MetabolomicsProjectFields.MSFeatureIdList.name());
		
    	for(Element orderedMSFeatureIdElement : orderedMSFeatureIdElementList) {
    		
    		String pipelineName = orderedMSFeatureIdElement.getAttributeValue(
    				MetabolomicsProjectFields.DataPipelineId.name());
    		if(pipelineName != null && !pipelineName.isEmpty()) {
    			
    			DataPipeline dp = currentExperiment.getDataPipelines().stream().
    				filter(p -> p.getSaveSafeName().equals(pipelineName)).
    				findFirst().orElse(null);
    			
    			String[]featureIds = orderedMSFeatureIdElement.getText().split(",");
    			orderedMSFeatureIdMap.put(dp, featureIds);
    		}
    	}
	}
	
	private void recreateWorklists(Element projectElement) {

    	Set<DataAcquisitionMethod>acquisitionMethods = 
    			currentExperiment.getDataPipelines().stream().
    			map(p -> p.getAcquisitionMethod()).collect(Collectors.toSet());
		List<Element>worklistMapElementList = 
				projectElement.getChild(MetabolomicsProjectFields.WorklistMap.name()).
				getChildren(ObjectNames.Worklist.name());
		for(Element worklistMapElement : worklistMapElementList) {
			
			String methodId = worklistMapElement.getAttributeValue(
					MetabolomicsProjectFields.DataAcquisitionMethodId.name());
			DataAcquisitionMethod method = acquisitionMethods.stream().
				filter(m -> m.getId().equals(methodId)).findFirst().orElse(null);
			Worklist wkl = new Worklist(worklistMapElement);
			for(WorklistItem item : wkl.getWorklistItems()) {
				
				DataFile df = currentExperiment.getDataFilesForAcquisitionMethod(method).
					stream().filter(f -> f.getName().equals(item.getDataFileName())).
					findFirst().orElse(null);
				item.setDataFile(df);
			}
			currentExperiment.setWorklistForAcquisitionMethod(method, wkl);
		}		
	}
	
	public void createDefaultBinnerPreferencesObject() {
		
		bpo = new BinnerPreferencesObject(activeDataPipeline);
		//	Input data
		Collection<DataFile>sf = currentExperiment.getDataFilesForPipeline(activeDataPipeline, false);
		
		Collection<DataFile>sampleFiles = currentExperiment.getDataFilesForPipeline(activeDataPipeline, false).stream().
				filter(f -> f.getParentSample().hasLevel(ReferenceSamplesManager.sampleLevel)).
				collect(Collectors.toList());
		bpo.getInputFiles().addAll(sampleFiles);
		Set<Polarity>polSet = bpo.getInputFiles().stream().
				map(f -> f.getDataAcquisitionMethod().getPolarity()).
				distinct().collect(Collectors.toSet());
		bpo.setPolarity(polSet.iterator().next());
	    
	    //	Data cleaning
		bpo.setOutlierSDdeviation(Double.parseDouble(
				BinnerParameters.DCOutlierStDevCutoff.getDefaultValue()));		
		bpo.setMissingRemovalThreshold(Double.parseDouble(
				BinnerParameters.DCMissingnessCutoff.getDefaultValue()));		
		bpo.setZeroAsMissing(Boolean.parseBoolean(
				BinnerParameters.DCZeroAsMissing.getDefaultValue()));		
		bpo.setLogTransform(Boolean.parseBoolean(
				BinnerParameters.DCNormalize.getDefaultValue()));	    
		bpo.setDeisotopingMassTolerance(Double.parseDouble(
				BinnerParameters.DCDeisotopeMassTolerance.getDefaultValue()));
		bpo.setDeisotopingRTtolerance(Double.parseDouble(
				BinnerParameters.DCDeisotopeRTTolearance.getDefaultValue()));
		bpo.setDeisotopingCorrCutoff(Double.parseDouble(
				BinnerParameters.DCDeisotopeCorrCutoff.getDefaultValue()));	    
	    bpo.setDeisoMassDiffDistr(Boolean.parseBoolean(
	    		BinnerParameters.DCDeisotopeMassDiffDistribution.getDefaultValue()));	    
	    bpo.setDeisotope(Boolean.parseBoolean(
	    		BinnerParameters.DCDeisotope.getDefaultValue()));
	    
	    //	Feature grouping
	    bpo.setCorrelationFunctionType(CorrelationFunctionType.getOptionByName(
				BinnerParameters.FGCorrFunction.getDefaultValue()));
	    bpo.setRtGap(Double.parseDouble(BinnerParameters.FGRTGap.getDefaultValue()));		    
	    bpo.setMinSubclusterRTgap(Double.parseDouble(BinnerParameters.FGMinRtGap.getDefaultValue()));	    
	    bpo.setMaxSubclusterRTgap(Double.parseDouble(BinnerParameters.FGMaxRtGap.getDefaultValue()));
	    bpo.setClusterGroupingMethod(ClusterGroupingMethod.getOptionByName(
				BinnerParameters.FGGroupingMethod.getDefaultValue()));
	    bpo.setBinClusteringCutoff(Integer.parseInt(
	    		BinnerParameters.FGBinningCutoffValue.getDefaultValue())); 
	    bpo.setBinClusteringCutoffType(BinClusteringCutoffType.getOptionByName(
				BinnerParameters.FGBinningCutoffType.getDefaultValue()));	    
	    bpo.setBinSizeLimitForAnalysis(Integer.parseInt(
	    		BinnerParameters.FGBinSizeLimitForAnalysis.getDefaultValue())); 
	    bpo.setBinSizeLimitForOutput(Integer.parseInt(
	    		BinnerParameters.FGBinSizeLimitForOutput.getDefaultValue())); 	    
	    bpo.setLimitBinSizeForAnalysis(Boolean.parseBoolean(
	    		BinnerParameters.FGBOverrideBinSizeLimitForAnalysis.getDefaultValue()));
	    bpo.setLimitBinSizeForOutput(Boolean.parseBoolean(
	    		BinnerParameters.FGBOverrideBinSizeLimitForOutput.getDefaultValue()));
	    
	    //	Annotations
	    bpo.setAnnotationMassTolerance(Double.parseDouble(
	    		BinnerParameters.ANMassTolerance.getDefaultValue()));
	    bpo.setAnnotationRTTolerance(Double.parseDouble(
	    		BinnerParameters.ANRTTolerance.getDefaultValue()));
	}
	
	/*
	 * Binning algo start
	 * */
	public void runBinner() {

		try {
			preparedDataMatrix = filterInputData();
		} catch (Exception e) {
			e.printStackTrace();		
			return;
		}
		try {
			binFeaturesByRt();
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		try {
			createCorrelationMatrices();
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}
	
	private void createCorrelationMatrices() {
		
		System.out.println("Creating correlation matrices for bins ...");
		
		pearson = new PearsonsCorrelation();
		spearman = new SpearmansCorrelation();
		kendall = new KendallsCorrelation();
		
		for(MsFeatureCluster bin : featureBins) {
			
			Collection<MsFeature>sorted = 
					bin.getSortedFeturesForDataPipeline(activeDataPipeline, SortProperty.RTmedObserved);
			MsFeature[]sortedFeatureArray = sorted.toArray(new MsFeature[sorted.size()]);
			long[] columnIndex =
					sorted.stream().
					map(f -> preparedDataMatrix.getColumnForLabel(f)).
					mapToLong(i -> i).
					toArray();			
			Matrix binDataMatrix = preparedDataMatrix.selectColumns(Ret.NEW, columnIndex);
			
			binDataMatrix.setMetaDataDimensionMatrix(0, Matrix.Factory.linkToArray((Object[])sortedFeatureArray));
			binDataMatrix.setMetaDataDimensionMatrix(1, preparedDataMatrix.getMetaDataDimensionMatrix(1));
			//	clusterFeatures(binDataMatrix);
			opticsClusterFeatures(binDataMatrix);
			
//			double[][] doubleMatrix = binDataMatrix.toDoubleArray();
//			Matrix corrMatrix = null;
//			
//			if(bpo.getCorrelationFunctionType().equals(CorrelationFunctionType.PEARSON))
//				corrMatrix = Matrix.Factory.linkToArray(pearson.computeCorrelationMatrix(doubleMatrix).getData());
//
//			if(bpo.getCorrelationFunctionType().equals(CorrelationFunctionType.SPEARMAN))
//				corrMatrix = Matrix.Factory.linkToArray(spearman.computeCorrelationMatrix(doubleMatrix).getData());
//
//			if(bpo.getCorrelationFunctionType().equals(CorrelationFunctionType.KENDALL))
//				corrMatrix = Matrix.Factory.linkToArray(kendall.computeCorrelationMatrix(doubleMatrix).getData());
//
//			if(corrMatrix != null) {
//
//				corrMatrix.setMetaDataDimensionMatrix(0, Matrix.Factory.linkToArray((Object[])sortedFeatureArray));
//				corrMatrix.setMetaDataDimensionMatrix(1, Matrix.Factory.linkToArray((Object[])sortedFeatureArray).transpose(Ret.NEW));
//				bin.setClusterCorrMatrix(corrMatrix);
//			}			
		}
	}
	
	private void opticsClusterFeatures(Matrix featureMatrix) {
		
		if(featureMatrix.getColumnCount() < 2)
			return;
		
		//	ColumnMajorStore dataStore = new ColumnMajorStore(false);
		List<DataPoint>dataList = new ArrayList<DataPoint>();
		long[]coord = new long[] {0,0};
		Matrix featureDataMatrix = featureMatrix.getMetaDataDimensionMatrix(0);
		
		//	System.err.println("\nClustering " + featureDataMatrix.getColumnCount() + " features:");
		for(long i=0; i<featureDataMatrix.getColumnCount(); i++) {
			
			coord[1] = i;
			MsFeature msf = (MsFeature)featureDataMatrix.getAsObject(coord);
			double[] featureData = featureMatrix.selectColumns(Ret.LINK, i).transpose().toDoubleArray()[0];
			dataList.add(new MsFeatureDataPoint(featureData, msf));
		}
		SimpleDataSet dataForClustering = new SimpleDataSet(dataList);
		HDBSCAN clusterer = new HDBSCAN(2);
		List<List<DataPoint>> generatedClusters = clusterer.cluster(dataForClustering);
		int counter = 1;
		for(List<DataPoint>cluster : generatedClusters) {
			
			System.err.println("\nCluster # " + counter);
			System.out.println(cluster.size() + " features");			
			counter++;
		}
	}
	
	private void clusterFeatures(Matrix featureMatrix) {
		
		KMeansPlusPlusClusterer<ClusterableFeatureData> clusterer = 
				new KMeansPlusPlusClusterer<ClusterableFeatureData>(3);
		List<ClusterableFeatureData>dataToCluster = new ArrayList<ClusterableFeatureData>();
		long[]coord = new long[] {0,0};
		Matrix featureDataMatrix = featureMatrix.getMetaDataDimensionMatrix(0);
		
		System.err.println("\nClustering " + featureDataMatrix.getColumnCount() + "features:");
		for(long i=0; i<featureDataMatrix.getColumnCount(); i++) {
			
			coord[1] = i;
			MsFeature msf = (MsFeature)featureDataMatrix.getAsObject(coord);
			double[] featureData = featureMatrix.selectColumns(Ret.LINK, i).transpose().toDoubleArray()[0];
			dataToCluster.add(new ClusterableFeatureData(msf, featureData));
		}
		if(dataToCluster.size() <=  3)
			return;
		
		List<CentroidCluster<ClusterableFeatureData>> results = clusterer.cluster(dataToCluster);
		//	Calculate silhuettes
		for(CentroidCluster<ClusterableFeatureData>cc : results) {
			
			ClusterableFeatureData cf = findCenterFeature(cc);
			System.out.println(cf.getFeature().toString());
		}		
	}
	
	private ClusterableFeatureData findCenterFeature(CentroidCluster<ClusterableFeatureData>cc) {
		
		double distance = Double.MAX_VALUE;
		double[] centerPoint = cc.getCenter().getPoint();
		ClusterableFeatureData fData = null;
		
		//	MathArrays.distance(a, b);
		for(ClusterableFeatureData dp : cc.getPoints()) {
			
			double newDistance = MathArrays.distance(centerPoint, dp.getPoint());
			if(newDistance < distance) {
				fData = dp;
				distance = newDistance;
			}
		}		
		return fData;
	}
	
	private void binFeaturesByRt() {
		
		System.out.println("Creating RT-based bins ...");
		featureBins = new ArrayList<MsFeatureCluster>();
		MsFeature[] featureArray = 
				cleanRTSortedFeatureSet.toArray(new MsFeature[cleanRTSortedFeatureSet.size()]);

		double rtGapForBinning = bpo.getRtGap();
		MsFeatureCluster newCluster = new MsFeatureCluster();
		newCluster.addFeature(featureArray[0], activeDataPipeline);
		int maxClusterSize = Math.min(bpo.getBinSizeLimitForAnalysis(), bpo.getBinSizeLimitForOutput());

		for (int i = 1; i < featureArray.length; i++) {

			double rtGap = featureArray[i].getMedianObservedRetention()
					- featureArray[i - 1].getMedianObservedRetention();
			if (rtGap < rtGapForBinning) {
				
				newCluster.addFeature(featureArray[i], activeDataPipeline);
				if(newCluster.getFeatures().size() > maxClusterSize) {
					
					System.err.println("Some of the feature bins exceede the size limit set in the prefefernces;\n"
							+ newCluster.getFeatures().size() + " vs allowed " + maxClusterSize);
					return;
				}
			}
			else {
				if(newCluster.getFeatures().size() > 1)
					featureBins.add(newCluster);
				
				newCluster = new MsFeatureCluster();
				newCluster.addFeature(featureArray[i], activeDataPipeline);
			}

		}
		System.out.println("# of bins " + featureBins.size());		
	}
	
	private Matrix filterInputData() {

		//	Subset the data
		MsFeatureSet activeFeatureSet = 
				currentExperiment.getActiveFeatureSetForDataPipeline(activeDataPipeline);
		Matrix dataMatrixForBinner = DataSetUtils.subsetDataMatrix(
				currentExperiment.getDataMatrixForDataPipeline(activeDataPipeline), 
				activeFeatureSet.getFeatures(), 
				bpo.getInputFiles());
		
		cleanRTSortedFeatureSet = 
				new ArrayList<MsFeature>(activeFeatureSet.getFeatures());
		
		//	Convert zero values to missing
		dataMatrixForBinner = dataMatrixForBinner.replace(Ret.NEW, Double.valueOf("0"), Double.NaN);
		
		System.out.println("Removing features with high % of missing values ...");
		dataMatrixForBinner = removeFeaturesMissingAboveThreshold(
						dataMatrixForBinner, bpo.getMissingRemovalThreshold());
		
		System.out.println("Replacing outliers with missing values ...");
		//	Find outlier values and set them as missing
		convertOutliersToMissing(dataMatrixForBinner, bpo.getOutlierSDdeviation());
		
		System.out.println("Imputing missing values ...");
		//	Impute missing data and log-transform if required
		dataMatrixForBinner = imputeMissingData(dataMatrixForBinner);
		if(bpo.isLogTransform()) {
			
			System.out.println("Log-transforming the data ...");
			dataMatrixForBinner = 
					new Log1pTransform(true, dataMatrixForBinner).calc(Ret.NEW);
			
			//	writeMatrixToFile(dataMatrixForBinner, "log_transformed.txt", true);	
		}
		return dataMatrixForBinner;
	}
	
	private Matrix removeFeaturesMissingAboveThreshold(
			Matrix dataMatrixForBinner, 
			double missingRemovalThreshold) {

		Matrix missingnessMatrix  = dataMatrixForBinner.
				countMissing(Ret.NEW, BaseMatrix.ROW).
				divide(dataMatrixForBinner.getRowCount() / 100.0d);
		Matrix featureMetadataMatrix = 
				dataMatrixForBinner.getMetaDataDimensionMatrix(0);
		Collection<MsFeature>featuresToRemove = new ArrayList<MsFeature>();
				
		long[]coord = new long[] {0,0};
		ArrayList<Long> toRemove = new ArrayList<Long>();
		for(long c=0; c<missingnessMatrix.getColumnCount(); c++) {
			
			coord[1] = c;
			double missingness = missingnessMatrix.getAsDouble(coord);
			if(missingness > missingRemovalThreshold) {
				toRemove.add(c);
				MsFeature msf = (MsFeature)featureMetadataMatrix.getAsObject(coord);
				featuresToRemove.add(msf);
			}
		}
		Matrix newMsFeatureLabelMatrix = 
				dataMatrixForBinner.getMetaDataDimensionMatrix(0).deleteColumns(Ret.NEW, toRemove);			
		Matrix newDataMatrixForBinner = dataMatrixForBinner.deleteColumns(Ret.NEW, toRemove);
		newDataMatrixForBinner.setMetaDataDimensionMatrix(0, newMsFeatureLabelMatrix);
		newDataMatrixForBinner.setMetaDataDimensionMatrix(1, dataMatrixForBinner.getMetaDataDimensionMatrix(1));
		
		if(!featuresToRemove.isEmpty())
			cleanRTSortedFeatureSet.removeAll(featuresToRemove);
		
		cleanRTSortedFeatureSet = 
				cleanRTSortedFeatureSet.stream().
				sorted(new MsFeatureComparator(SortProperty.RT)).
				collect(Collectors.toList());
		
		return newDataMatrixForBinner;
	}
	
	private void convertOutliersToMissing(Matrix dataMatrixForBinner, double stDevMargin) {
		
		Matrix standardized = dataMatrixForBinner.standardize(Ret.NEW, BaseMatrix.ROW);
		for(long i=0; i<dataMatrixForBinner.getRowCount(); i++) {
			
			for(long j=0; j<dataMatrixForBinner.getColumnCount(); j++) {
				
				double value = standardized.getAsDouble(i,j);
				if(!MathUtil.isNaNOrInfinite(value) && Math.abs(value) > stDevMargin)
					dataMatrixForBinner.setAsDouble(Double.NaN, i,j);
			}		
		}		
	}
	
	//	TODO start with median value, maybe add other options later
	private Matrix imputeMissingData(Matrix dataMatrixForBinner) {

		Matrix imputedMatrixForBinner = 
				new ImputeMedian(BaseMatrix.ROW, dataMatrixForBinner).calc(Ret.NEW);
		
		return imputedMatrixForBinner;
	}
}

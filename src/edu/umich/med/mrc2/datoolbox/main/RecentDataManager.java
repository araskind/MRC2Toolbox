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

package edu.umich.med.mrc2.datoolbox.main;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import edu.umich.med.mrc2.datoolbox.data.ExperimentPointer;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureInfoBundleCollection;
import edu.umich.med.mrc2.datoolbox.data.compare.MSMSClusterDataSetComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.MsFeatureInformationBundleCollectionComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSExperiment;
import edu.umich.med.mrc2.datoolbox.data.msclust.IMSMSClusterDataSet;
import edu.umich.med.mrc2.datoolbox.project.Experiment;
import edu.umich.med.mrc2.datoolbox.project.ProjectType;
import edu.umich.med.mrc2.datoolbox.utils.ExperimentUtils;

public class RecentDataManager {
	
	private static int MAX_OBJECTS_COUNT = 15;
	
	public static final String DOCUMENT_ROOT = "RecentObjects";
	public static final String RECENT_EXPERIMENTS_ELEMENT = "RecentExperiments";
	public static final String RECENT_FEATURE_COLLECTION_ELEMENT = "RecentFeatureCollections";
	public static final String RECENT_CLUSTER_SET_ELEMENT = "RecentClusterSets";
	
	private static final Path recentObjectFilePath = 
			Paths.get(MRC2ToolBoxCore.configDir, "recentObjects.xml");

	private static Map<ProjectType,Collection<ExperimentPointer>>recentExperimentsMap = 
			new TreeMap<ProjectType,Collection<ExperimentPointer>>();	
	private static Set<MsFeatureInfoBundleCollection>featureCollections = 
			new TreeSet<MsFeatureInfoBundleCollection>(
					new MsFeatureInformationBundleCollectionComparator(SortProperty.Name));
	private static Set<IMSMSClusterDataSet>featureClusterDataSets = 
			new TreeSet<IMSMSClusterDataSet>(new MSMSClusterDataSetComparator(SortProperty.Name));

	public RecentDataManager() {
		super();
		readDataFromFile();
	}

	public static void readDataFromFile() {

		File dataFile = recentObjectFilePath.toFile();
		if(!dataFile.exists()) {
			saveDataToFile();
			return;
		}
		SAXBuilder sax = new SAXBuilder();
		Document doc = null;
		try {
			doc = sax.build(dataFile);
		} catch (JDOMException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return;						
		}
		Element rootElement = doc.getRootElement();
		
		String featureCollectionIdListString = 
				rootElement.getChild(RECENT_FEATURE_COLLECTION_ELEMENT).getText();
		Collection<String> featureCollectionIdList = 
				ExperimentUtils.getIdList(featureCollectionIdListString);
		for(String fcid : featureCollectionIdList) {
			
			MsFeatureInfoBundleCollection fc = 
					FeatureCollectionManager.getMsFeatureInfoBundleCollectionById(fcid);
			if(fc != null) 
				featureCollections.add(fc);			
		}	
		String clusterCollectionIdListString = 
				rootElement.getChild(RECENT_CLUSTER_SET_ELEMENT).getText();
		Collection<String> clusterCollectionIdList = 
				ExperimentUtils.getIdList(clusterCollectionIdListString);
		for(String ccid : clusterCollectionIdList) {
			
			IMSMSClusterDataSet cds = 
					MSMSClusterDataSetManager.getMSMSClusterDataSetById(ccid);
			if(cds != null) 
				featureClusterDataSets.add(cds);			
		}	
		List<Element> experimentElementList = 
				rootElement.getChild(RECENT_EXPERIMENTS_ELEMENT).
				getChildren(ExperimentPointer.ELEMENT_NAME);
		for (Element  experimentElement :  experimentElementList) {
			
			ExperimentPointer ep = new ExperimentPointer(experimentElement);
			if(ep != null && ep.getProjectType() != null) {
				
				if(!recentExperimentsMap.containsKey(ep.getProjectType()))
					recentExperimentsMap.put(ep.getProjectType(), new LinkedHashSet<ExperimentPointer>());
			
				recentExperimentsMap.get(ep.getProjectType()).add(ep);
			}
		}	
	}
	
	public static void saveDataToFile() {
				
        Document document = new Document();
        Element documentRoot = new Element(DOCUMENT_ROOT);
        documentRoot.setAttribute("version", "1.0.0.0");

        Set<String> fcIdSet = featureCollections.stream().
        		filter(c -> Objects.nonNull(c.getId())).
        		map(c -> c.getId()).collect(Collectors.toSet());
        documentRoot.addContent(       		
        		new Element(RECENT_FEATURE_COLLECTION_ELEMENT).
        		setText(StringUtils.join(fcIdSet, ",")));
		
        Set<String> clustCollIdSet = featureClusterDataSets.stream().
        		filter(c -> Objects.nonNull(c.getId())).
        		map(c -> c.getId()).collect(Collectors.toSet());
        documentRoot.addContent(       		
        		new Element(RECENT_CLUSTER_SET_ELEMENT).
        		setText(StringUtils.join(clustCollIdSet, ",")));
        
        Element recentExperimentListElement = 
        		new Element(RECENT_EXPERIMENTS_ELEMENT);
        
        List<ExperimentPointer> experiments = recentExperimentsMap.values().stream().
        		flatMap(v -> v.stream()).collect(Collectors.toList());
        
        for(ExperimentPointer ep : experiments)
        	recentExperimentListElement.addContent(ep.getXmlElement());
        
        documentRoot.addContent(recentExperimentListElement);
        document.setContent(documentRoot);
        
        try {
            FileWriter writer = new FileWriter(recentObjectFilePath.toFile(), false);
            XMLOutputter outputter = new XMLOutputter();
            outputter.setFormat(Format.getCompactFormat());
            outputter.output(document, writer);
         } catch (Exception e) {
            e.printStackTrace();
        }
	}

	public static void setMAX_OBJECTS_COUNT(int maxObjectsCount) {
		MAX_OBJECTS_COUNT = maxObjectsCount;
	}
	
	public static Collection<ExperimentPointer>getRecentExperimentsOfType(ProjectType type){
		return recentExperimentsMap.get(type);
	}
	
	public static void addExperiment(Experiment toAdd) {
		
		if(!recentExperimentsMap.containsKey(toAdd.getProjectType()))
			recentExperimentsMap.put(toAdd.getProjectType(), new LinkedHashSet<ExperimentPointer>());
		
		Collection<ExperimentPointer> projectsOfType = 
				recentExperimentsMap.get(toAdd.getProjectType());
		
		if(projectsOfType.size() == MAX_OBJECTS_COUNT)
			projectsOfType.iterator().remove();
		
		recentExperimentsMap.get(toAdd.getProjectType()).add(new ExperimentPointer(toAdd));
	}
	
	public static void addIDTrackerExperiment(LIMSExperiment limsExperiment) {
		
		if(!recentExperimentsMap.containsKey(ProjectType.ID_TRACKER_DATA_ANALYSIS))
			recentExperimentsMap.put(ProjectType.ID_TRACKER_DATA_ANALYSIS, new LinkedHashSet<ExperimentPointer>());
		
		Collection<ExperimentPointer> projectsOfType = 
				recentExperimentsMap.get(ProjectType.ID_TRACKER_DATA_ANALYSIS);
		
		if(projectsOfType.size() == MAX_OBJECTS_COUNT)
			projectsOfType.iterator().remove();
		
		recentExperimentsMap.get(ProjectType.ID_TRACKER_DATA_ANALYSIS).add(new ExperimentPointer(limsExperiment));
	}

	public static Set<MsFeatureInfoBundleCollection>getRecentFeatureCollections(){
		return featureCollections;
	}
	
	public static void addFeatureCollection(MsFeatureInfoBundleCollection toAdd) {
		
		if(toAdd == null)
			return;
		
		if(featureCollections.contains(toAdd))
			return;
		
		if(featureCollections.size() == MAX_OBJECTS_COUNT)
			featureCollections.iterator().remove();
		
		featureCollections.add(toAdd);
	}
	
	public static Set<IMSMSClusterDataSet>getRecentFeatureClusterDataSets(){
		return featureClusterDataSets;
	}
	
	public static void addIMSMSClusterDataSet(IMSMSClusterDataSet toAdd) {
		
		if(toAdd == null)
			return;
		
		if(featureClusterDataSets.contains(toAdd))
			return;
		
		if(featureClusterDataSets.size() == MAX_OBJECTS_COUNT)
			featureClusterDataSets.iterator().remove();
		
		featureClusterDataSets.add(toAdd);
	}
	
	public static void clearRecentExperiments() { 
		recentExperimentsMap.clear();
	}
	
	public static void clearRecentFeatureCollections() { 
		featureCollections.clear();
	}
	
	public static void clearRecentFeatureClusterDataSets() { 
		featureClusterDataSets.clear();
	}
	
	public static ExperimentPointer getRecentExperimentById(String id) {
		
		return recentExperimentsMap.values().stream().
				flatMap(e -> e.stream()).
				filter(e -> e.getId().equals(id)).
				findFirst().orElse(null);
	}
}















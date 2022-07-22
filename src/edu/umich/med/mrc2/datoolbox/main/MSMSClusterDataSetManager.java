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

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import edu.umich.med.mrc2.datoolbox.data.MsFeatureInfoBundle;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureInfoBundleCollection;
import edu.umich.med.mrc2.datoolbox.data.compare.MSMSClusterDataSetComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.msclust.MSMSClusterDataSet;
import edu.umich.med.mrc2.datoolbox.data.msclust.MsFeatureInfoBundleCluster;
import edu.umich.med.mrc2.datoolbox.database.idt.FeatureCollectionUtils;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.idt.IDTMSMSFeatureDataPullTask;

public class MSMSClusterDataSetManager {
	
	public static final String CURRENT_MSMS_CLUSTER_SEARCH_RESULT = "Current MSMS cluster search";
	public static final String CURRENT_MS1_CLUSTER_SEARCH_RESULT = "Current MS1 clister search";
	public static final String ACTIVE_PROJECT_DEFAULT_CLUSTER_DATA_SET = "Active project default cluster data set";
	
	public static final MSMSClusterDataSet msmsClusterSearchResults = 
			new MSMSClusterDataSet(CURRENT_MSMS_CLUSTER_SEARCH_RESULT);	
	public static final MSMSClusterDataSet msOneClusterSearchResults = 
			new MSMSClusterDataSet(CURRENT_MS1_CLUSTER_SEARCH_RESULT);
	public static final MSMSClusterDataSet activeProjectDefaultClusterDataSet = 
			new MSMSClusterDataSet(ACTIVE_PROJECT_DEFAULT_CLUSTER_DATA_SET);
	
	public static final Map<MSMSClusterDataSet, Set<String>>clusterDataSetsToClusterIdsMap = 
			new TreeMap<MSMSClusterDataSet, Set<String>>(
					new MSMSClusterDataSetComparator(SortProperty.Name));

	public static void clearDefaultCollections() {		
		msmsClusterSearchResults.clearDataSet();
		msOneClusterSearchResults.clearDataSet();
		activeProjectDefaultClusterDataSet.clearDataSet();
	}
		
	public static void refreshMsFeatureInfoBundleCollections() {
		clusterDataSetsToClusterIdsMap.clear();
		clusterDataSetsToClusterIdsMap.put(msmsClusterSearchResults, new TreeSet<String>());
		clusterDataSetsToClusterIdsMap.put(msOneClusterSearchResults, new TreeSet<String>());
		clusterDataSetsToClusterIdsMap.put(activeProjectDefaultClusterDataSet, new TreeSet<String>());
		try {
			
			//	TODO
//			featureCollectionsMSMSIDMap.putAll(
//					FeatureCollectionUtils.getMsFeatureInformationBundleCollections());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static Collection<MSMSClusterDataSet>getMSMSClusterDataSets() {

//		try {
//			featureCollectionsMSMSIDMap.putAll(
//					FeatureCollectionUtils.getMsFeatureInformationBundleCollections());
//		} catch (Exception e) {
//			e.printStackTrace();
//		}		
		return clusterDataSetsToClusterIdsMap.keySet();
	}
	
	public static Collection<MSMSClusterDataSet>getEditableMSMSClusterDataSets() {
		
		return clusterDataSetsToClusterIdsMap.keySet().stream().
			filter(c -> !c.equals(msmsClusterSearchResults)).
			filter(c -> !c.equals(msOneClusterSearchResults)).
			filter(c -> !c.equals(activeProjectDefaultClusterDataSet)).
			sorted(new MSMSClusterDataSetComparator(SortProperty.Name)).
			collect(Collectors.toList());
	}
	
	public static MSMSClusterDataSet getMSMSClusterDataSetById(String id) {
		
		return clusterDataSetsToClusterIdsMap.keySet().stream().
				filter(c -> c.getId().equals(id)).findFirst().orElse(null);
	}
	
	public static IDTMSMSFeatureDataPullTask getMSMSClusterDataSetData(
			MSMSClusterDataSet mbColl) {
	
		//	TODO
//		if(!featureCollectionsMSMSIDMap.containsKey(mbColl))
//			return null;
//		
//		Set<String> idList = featureCollectionsMSMSIDMap.get(mbColl);
//		Collection<String> attachedIdList = mbColl.getMSMSFeatureIds();
//		Set<String> unAttachedIdList = idList.stream().
//				filter(i -> !attachedIdList.contains(i)).collect(Collectors.toSet());
//		
//		//	Check cash data
//		Set<String>missingIds = new TreeSet<String>();
//		for(String id : unAttachedIdList) {
//			MsFeatureInfoBundle cachedFeature = 
//					FeatureCollectionUtils.retrieveMSMSFetureInfoBundleFromCache(id);
//			if(cachedFeature == null)
//				missingIds.add(id);
//			else
//				mbColl.addFeature(cachedFeature);
//		}
//		if(!missingIds.isEmpty()) {
//			IDTMSMSFeatureDataPullTask task = new IDTMSMSFeatureDataPullTask(missingIds);
//			return task;
//		}
//		else	
			return null;
	}	
	
	public static int getMSMSClusterDataSetSize(MSMSClusterDataSet mbColl) {
		
		if(!clusterDataSetsToClusterIdsMap.containsKey(mbColl))
			return 0;
		
		return clusterDataSetsToClusterIdsMap.get(mbColl).size();
	}
	
	public static Collection<MsFeatureInfoBundle>getLoadedMSMSFeaturesByIds(Collection<String>msmsIds){
		return msmsIds.stream().
				map(id -> FeatureCollectionUtils.retrieveMSMSFetureInfoBundleFromCache(id)).
				filter(f -> f != null).collect(Collectors.toSet());
	}
	
	public static void addFeaturesToCollection(
			MsFeatureInfoBundleCollection collection, 
			Collection<MsFeatureInfoBundle>featuresToAdd)  {

		if(!clusterDataSetsToClusterIdsMap.containsKey(collection) || featuresToAdd.isEmpty())
			return;
		
		Set<String>existingIds = clusterDataSetsToClusterIdsMap.get(collection);
		Set<String>featureIdsToAdd = featuresToAdd.stream().
				map(f -> f.getMSMSFeatureId()).
				filter(id -> !existingIds.contains(id)).
				collect(Collectors.toSet());

		if(featureIdsToAdd.isEmpty())
			return;
		
		try {
			FeatureCollectionUtils.addFeaturesToCollection(collection.getId(), featureIdsToAdd);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		clusterDataSetsToClusterIdsMap.get(collection).addAll(featureIdsToAdd);
	}
	
	public static void removeFeaturesFromCollection(
			MSMSClusterDataSet dataSet, 
			Collection<MsFeatureInfoBundleCluster>clustersToRemove) {
		
		if(!clusterDataSetsToClusterIdsMap.containsKey(dataSet) || clustersToRemove.isEmpty())
			return;
		
		Set<String>existingIds = clusterDataSetsToClusterIdsMap.get(dataSet);
		Set<String>clusterIdsToRemove = clustersToRemove.stream().
				map(f -> f.getId()).
				filter(id -> existingIds.contains(id)).
				collect(Collectors.toSet());

		if(clusterIdsToRemove.isEmpty())
			return;
		
		//	TODO
//		try {
//			FeatureCollectionUtils.removeFeaturesFromCollection(collection.getId(), featureIdsToRemove);
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		clusterDataSetsToClusterIdsMap.get(dataSet).removeAll(clusterIdsToRemove);
	}

	public static Map<MSMSClusterDataSet, Set<String>> getFeatureCollectionsMsmsIdMap() {
		return clusterDataSetsToClusterIdsMap;
	}
}

















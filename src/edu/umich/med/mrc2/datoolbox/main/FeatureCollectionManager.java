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
import edu.umich.med.mrc2.datoolbox.data.compare.MsFeatureInformationBundleCollectionComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.database.idt.FeatureCollectionUtils;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.idt.IDTMSMSFeatureDataPullTask;

public class FeatureCollectionManager {
	
	public static final String CURRENT_MSMS_FEATURE_SEARCH_RESULT = "Current MSMS feature search";
	public static final String CURRENT_MS1_FEATURE_SEARCH_RESULT = "Current MS1 feature search";
	public static final String ACTIVE_PROJECT_FEATURE_SET = "Active project complete feature set";
	
	public static final MsFeatureInfoBundleCollection msmsSearchResults = 
			new MsFeatureInfoBundleCollection(CURRENT_MSMS_FEATURE_SEARCH_RESULT);	
	public static final MsFeatureInfoBundleCollection msOneSearchResults = 
			new MsFeatureInfoBundleCollection(CURRENT_MS1_FEATURE_SEARCH_RESULT);
	public static final MsFeatureInfoBundleCollection activeProjectFeatureSet = 
			new MsFeatureInfoBundleCollection(ACTIVE_PROJECT_FEATURE_SET);
	
	public static final Map<MsFeatureInfoBundleCollection, Set<String>>featureCollectionsMSMSIDMap = 
			new TreeMap<MsFeatureInfoBundleCollection, Set<String>>(
					new MsFeatureInformationBundleCollectionComparator(SortProperty.Name));

	public static void clearDefaultCollections() {		
		msmsSearchResults.clearCollection();
		msOneSearchResults.clearCollection();
		activeProjectFeatureSet.clearCollection();
	}
		
	public static void refreshMsFeatureInfoBundleCollections() {
		featureCollectionsMSMSIDMap.clear();
		featureCollectionsMSMSIDMap.put(msmsSearchResults, new TreeSet<String>());
		featureCollectionsMSMSIDMap.put(msOneSearchResults, new TreeSet<String>());
		featureCollectionsMSMSIDMap.put(activeProjectFeatureSet, new TreeSet<String>());
		try {
			featureCollectionsMSMSIDMap.putAll(
					FeatureCollectionUtils.getMsFeatureInformationBundleCollections());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static Collection<MsFeatureInfoBundleCollection>getMsFeatureInfoBundleCollections() {

//		try {
//			featureCollectionsMSMSIDMap.putAll(
//					FeatureCollectionUtils.getMsFeatureInformationBundleCollections());
//		} catch (Exception e) {
//			e.printStackTrace();
//		}		
		return featureCollectionsMSMSIDMap.keySet();
	}
	
	public static Collection<MsFeatureInfoBundleCollection>getEditableMsFeatureInfoBundleCollections() {
		
		return featureCollectionsMSMSIDMap.keySet().stream().
			filter(c -> !c.equals(msmsSearchResults)).
			filter(c -> !c.equals(msOneSearchResults)).
			filter(c -> !c.equals(activeProjectFeatureSet)).
			sorted(new MsFeatureInformationBundleCollectionComparator(SortProperty.Name)).
			collect(Collectors.toList());
	}
	
	public static MsFeatureInfoBundleCollection getMsFeatureInfoBundleCollectionById(String id) {
		
		return featureCollectionsMSMSIDMap.keySet().stream().
				filter(c -> c.getId().equals(id)).findFirst().orElse(null);
	}
	
	
	public static IDTMSMSFeatureDataPullTask getMsFeatureInfoBundleCollectionData(
			MsFeatureInfoBundleCollection mbColl) {
		
		if(!featureCollectionsMSMSIDMap.containsKey(mbColl))
			return null;
		
		Set<String> idList = featureCollectionsMSMSIDMap.get(mbColl);
		Collection<String> attachedIdList = mbColl.getMSMSFeatureIds();
		Set<String> unAttachedIdList = idList.stream().
				filter(i -> !attachedIdList.contains(i)).collect(Collectors.toSet());
		
		//	Check cash data
		Set<String>missingIds = new TreeSet<String>();
		for(String id : unAttachedIdList) {
			MsFeatureInfoBundle cachedFeature = 
					FeatureCollectionUtils.retrieveMSMSFetureInfoBundleFromCache(id);
			if(cachedFeature == null)
				missingIds.add(id);
			else
				mbColl.addFeature(cachedFeature);
		}
		if(!missingIds.isEmpty()) {
			IDTMSMSFeatureDataPullTask task = new IDTMSMSFeatureDataPullTask(missingIds);
			return task;
		}
		else	
			return null;
	}	
	
	public static int getMsFeatureInfoBundleCollectionSize(
			MsFeatureInfoBundleCollection mbColl) {
		
		if(!featureCollectionsMSMSIDMap.containsKey(mbColl))
			return 0;
		
		return featureCollectionsMSMSIDMap.get(mbColl).size();
	}
	
	public static Collection<MsFeatureInfoBundle>getLoadedMSMSFeaturesByIds(Collection<String>msmsIds){
		return msmsIds.stream().
				map(id -> FeatureCollectionUtils.retrieveMSMSFetureInfoBundleFromCache(id)).
				filter(f -> f != null).collect(Collectors.toSet());
	}
	
	public static void addFeaturesToCollection(
			MsFeatureInfoBundleCollection collection, 
			Collection<MsFeatureInfoBundle>featuresToAdd)  {

		if(!featureCollectionsMSMSIDMap.containsKey(collection) || featuresToAdd.isEmpty())
			return;
		
		Set<String>existingIds = featureCollectionsMSMSIDMap.get(collection);
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
		featureCollectionsMSMSIDMap.get(collection).addAll(featureIdsToAdd);
	}
	
	public static void removeFeaturesFromCollection(
			MsFeatureInfoBundleCollection collection, 
			Collection<MsFeatureInfoBundle>featuresToRemove) {
		
		if(!featureCollectionsMSMSIDMap.containsKey(collection) || featuresToRemove.isEmpty())
			return;
		
		Set<String>existingIds = featureCollectionsMSMSIDMap.get(collection);
		Set<String>featureIdsToRemove = featuresToRemove.stream().
				map(f -> f.getMSMSFeatureId()).
				filter(id -> existingIds.contains(id)).
				collect(Collectors.toSet());

		if(featureIdsToRemove.isEmpty())
			return;
		
		try {
			FeatureCollectionUtils.removeFeaturesFromCollection(collection.getId(), featureIdsToRemove);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		featureCollectionsMSMSIDMap.get(collection).removeAll(featureIdsToRemove);
	}

	public static Map<MsFeatureInfoBundleCollection, Set<String>> getFeatureCollectionsMsmsIdMap() {
		return featureCollectionsMSMSIDMap;
	}
}

















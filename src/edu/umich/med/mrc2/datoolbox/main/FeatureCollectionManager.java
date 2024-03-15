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
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import edu.umich.med.mrc2.datoolbox.data.MSFeatureInfoBundle;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureInfoBundleCollection;
import edu.umich.med.mrc2.datoolbox.data.compare.MsFeatureInformationBundleCollectionComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.database.idt.FeatureCollectionUtils;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.idt.IDTMSMSFeatureDataPullTask;
import edu.umich.med.mrc2.datoolbox.utils.DiskCacheUtils;

public class FeatureCollectionManager {
	
	public static final String CURRENT_MSMS_FEATURE_SEARCH_RESULT = "Current MSMS feature search";
	public static final String CURRENT_MS1_FEATURE_SEARCH_RESULT = "Current MS1 feature search";
	public static final String ACTIVE_EXPERIMENT_FEATURE_SET = "Active experiment complete feature set";
	
	public static final MsFeatureInfoBundleCollection msmsSearchResults = 
			new MsFeatureInfoBundleCollection(CURRENT_MSMS_FEATURE_SEARCH_RESULT);	
	public static final MsFeatureInfoBundleCollection msOneSearchResults = 
			new MsFeatureInfoBundleCollection(CURRENT_MS1_FEATURE_SEARCH_RESULT);
	public static final MsFeatureInfoBundleCollection activeExperimentFeatureSet = 
			new MsFeatureInfoBundleCollection(ACTIVE_EXPERIMENT_FEATURE_SET);
	
	public static final Map<MsFeatureInfoBundleCollection, Set<String>>featureCollectionsMSIDMap = 
			new TreeMap<MsFeatureInfoBundleCollection, Set<String>>(
					new MsFeatureInformationBundleCollectionComparator(SortProperty.Name));

	public static void clearDefaultCollections() {		
		msmsSearchResults.clearCollection();
		msOneSearchResults.clearCollection();
		activeExperimentFeatureSet.clearCollection();
	}
		
	public static void refreshMsFeatureInfoBundleCollections() {
		featureCollectionsMSIDMap.clear();
		featureCollectionsMSIDMap.put(msmsSearchResults, new TreeSet<String>());
		featureCollectionsMSIDMap.put(msOneSearchResults, new TreeSet<String>());
		featureCollectionsMSIDMap.put(activeExperimentFeatureSet, new TreeSet<String>());
		try {
			featureCollectionsMSIDMap.putAll(
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
		return featureCollectionsMSIDMap.keySet();
	}
	
	public static Collection<MsFeatureInfoBundleCollection>getEditableMsFeatureInfoBundleCollections() {
		
		return featureCollectionsMSIDMap.keySet().stream().
			filter(c -> !c.equals(msmsSearchResults)).
			filter(c -> !c.equals(msOneSearchResults)).
			filter(c -> !c.equals(activeExperimentFeatureSet)).
			sorted(new MsFeatureInformationBundleCollectionComparator(SortProperty.Name)).
			collect(Collectors.toList());
	}
	
	public static MsFeatureInfoBundleCollection getMsFeatureInfoBundleCollectionById(String id) {		
		return featureCollectionsMSIDMap.keySet().stream().
				filter(c -> c.getId().equals(id)).findFirst().orElse(null);
	}	
	
	public static IDTMSMSFeatureDataPullTask getMsFeatureInfoBundleCollectionData(
			MsFeatureInfoBundleCollection mbColl) {
		
		if(!featureCollectionsMSIDMap.containsKey(mbColl))
			return null;
		
		Set<String> idList = featureCollectionsMSIDMap.get(mbColl);
		if(idList == null || idList.isEmpty()) {
			try {
				idList = 
					FeatureCollectionUtils.getFeatureIdsForMsFeatureInfoBundleCollection(
							mbColl.getId());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(idList == null || idList.isEmpty())
			return null;
		
		Collection<String> attachedIdList = mbColl.getMSMSFeatureIds();
		Set<String> unAttachedIdList = idList.stream().
				filter(i -> !attachedIdList.contains(i)).collect(Collectors.toSet());
		
		//	Check cache data
		Set<String>missingIds = new TreeSet<String>();
		for(String id : unAttachedIdList) {
			MSFeatureInfoBundle cachedFeature = 
					DiskCacheUtils.retrieveMSFeatureInfoBundleFromCache(id);
			if(cachedFeature == null)
				missingIds.add(id);
			else
				mbColl.addFeature(cachedFeature);
		}
		if(!missingIds.isEmpty())
			return new IDTMSMSFeatureDataPullTask(missingIds, mbColl);	
		else	
			return null;
	}	
	
	public static int getMsFeatureInfoBundleCollectionSize(
			MsFeatureInfoBundleCollection mbColl) {
		
		if(!featureCollectionsMSIDMap.containsKey(mbColl))
			return 0;
		
		return featureCollectionsMSIDMap.get(mbColl).size();
	}
	
	public static Collection<MSFeatureInfoBundle>getLoadedMSMSFeaturesByIds(Collection<String>msmsIds){
		return msmsIds.stream().
				map(id -> DiskCacheUtils.retrieveMSFeatureInfoBundleFromCache(id)).
				filter(f -> Objects.nonNull(f)).collect(Collectors.toSet());
	}
	
	public static void addFeaturesToCollection(
			MsFeatureInfoBundleCollection collection, 
			Collection<MSFeatureInfoBundle>featuresToAdd)  {

		if(!featureCollectionsMSIDMap.containsKey(collection) || featuresToAdd.isEmpty())
			return;
		
		Set<String>existingIds = featureCollectionsMSIDMap.get(collection);
		Set<String>featureIdsToAdd = featuresToAdd.stream().
				map(f -> f.getMSFeatureId()).
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
		featureCollectionsMSIDMap.get(collection).addAll(featureIdsToAdd);
	}
	
	public static void removeFeaturesFromCollection(
			MsFeatureInfoBundleCollection collection, 
			Collection<MSFeatureInfoBundle>featuresToRemove) {
		
		if(!featureCollectionsMSIDMap.containsKey(collection) || featuresToRemove.isEmpty())
			return;
		
		Set<String>existingIds = featureCollectionsMSIDMap.get(collection);
		Set<String>featureIdsToRemove = featuresToRemove.stream().
				map(f -> f.getMSFeatureId()).
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
		featureCollectionsMSIDMap.get(collection).removeAll(featureIdsToRemove);
	}

	public static Map<MsFeatureInfoBundleCollection, Set<String>> getFeatureCollectionsMsIdMap() {
		return featureCollectionsMSIDMap;
	}
}

















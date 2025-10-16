/*******************************************************************************
 *
 * (C) Copyright 2018-2025 MRC2 (http://mrc2.umich.edu).
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
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import edu.umich.med.mrc2.datoolbox.data.MSFeatureInfoBundle;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureInfoBundleCollection;
import edu.umich.med.mrc2.datoolbox.data.compare.MsFeatureInfoBundleCollectionComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.database.idt.FeatureCollectionUtils;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.idt.IDTMSMSFeatureDataPullTask;
import edu.umich.med.mrc2.datoolbox.utils.DiskCacheUtils;

public class FeatureCollectionManager {
	
	public static final String CURRENT_MSMS_FEATURE_SEARCH_RESULT = "Current MSMS feature search";
	public static final String CURRENT_MS1_FEATURE_SEARCH_RESULT = "Current MS1 feature search";
	public static final String ACTIVE_EXPERIMENT_FEATURE_SET = "Active experiment complete feature set";
	
	public static final MsFeatureInfoBundleCollection msmsSearchResults = 
			new MsFeatureInfoBundleCollection(
					CURRENT_MSMS_FEATURE_SEARCH_RESULT, 
					CURRENT_MSMS_FEATURE_SEARCH_RESULT);	
	public static final MsFeatureInfoBundleCollection msOneSearchResults = 
			new MsFeatureInfoBundleCollection(
					CURRENT_MS1_FEATURE_SEARCH_RESULT, 
					CURRENT_MS1_FEATURE_SEARCH_RESULT);
	public static final MsFeatureInfoBundleCollection activeExperimentFeatureSet = 
			new MsFeatureInfoBundleCollection(
					ACTIVE_EXPERIMENT_FEATURE_SET,
					ACTIVE_EXPERIMENT_FEATURE_SET);
	
	public static final Set<MsFeatureInfoBundleCollection>featureCollectionsMSIDSet = 
			new TreeSet<MsFeatureInfoBundleCollection>(
					new MsFeatureInfoBundleCollectionComparator(SortProperty.Name));

	public static void clearDefaultCollections() {		
		msmsSearchResults.clearCollection();
		msOneSearchResults.clearCollection();
		activeExperimentFeatureSet.clearCollection();
	}
		
	public static void refreshMsFeatureInfoBundleCollections() {
		featureCollectionsMSIDSet.clear();
		featureCollectionsMSIDSet.add(msmsSearchResults);
		featureCollectionsMSIDSet.add(msOneSearchResults);
		featureCollectionsMSIDSet.add(activeExperimentFeatureSet);
		try {
			featureCollectionsMSIDSet.addAll(
					FeatureCollectionUtils.getMsFeatureInformationBundleCollections());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static Collection<MsFeatureInfoBundleCollection>getMsFeatureInfoBundleCollections() {		
		return featureCollectionsMSIDSet;
	}
	
	public static Collection<MsFeatureInfoBundleCollection>getEditableMsFeatureInfoBundleCollections() {
		
		return featureCollectionsMSIDSet.stream().
			filter(c -> !c.equals(msmsSearchResults)).
			filter(c -> !c.equals(msOneSearchResults)).
			filter(c -> !c.equals(activeExperimentFeatureSet)).
			sorted(new MsFeatureInfoBundleCollectionComparator(SortProperty.Name)).
			collect(Collectors.toList());
	}
	
	public static MsFeatureInfoBundleCollection getMsFeatureInfoBundleCollectionById(String id) {		
		return featureCollectionsMSIDSet.stream().
				filter(c -> c.getId().equals(id)).findFirst().orElse(null);
	}	
	
	public static IDTMSMSFeatureDataPullTask getMsFeatureInfoBundleCollectionData(
			MsFeatureInfoBundleCollection mbColl) {
		
		if(!featureCollectionsMSIDSet.contains(mbColl))
			return null;

//		if(mbColl.getFeatureIds().isEmpty()) {
			Set<String>idList = null;
			try {
				idList = 
					FeatureCollectionUtils.getFeatureIdsForMsFeatureInfoBundleCollection(
							mbColl.getId());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(idList != null)
				mbColl.getFeatureIds().addAll(idList);
//		}
		if(mbColl.getFeatureIds().isEmpty())
			return null;
		
		Collection<String> attachedIdList = mbColl.getMSMSFeatureIds();
		Set<String> unAttachedIdList = mbColl.getFeatureIds().stream().
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

	public static Collection<MSFeatureInfoBundle>getLoadedMSMSFeaturesByIds(Collection<String>msmsIds){
		return msmsIds.stream().
				map(id -> DiskCacheUtils.retrieveMSFeatureInfoBundleFromCache(id)).
				filter(f -> Objects.nonNull(f)).collect(Collectors.toSet());
	}
	
	public static void addFeaturesToCollection(
			MsFeatureInfoBundleCollection collection, 
			Collection<MSFeatureInfoBundle>featuresToAdd)  {

		if(!featureCollectionsMSIDSet.contains(collection) || featuresToAdd.isEmpty())
			return;
		
		if(collection.getFeatureIds().isEmpty()) {
			
			Set<String>dbIds = new TreeSet<String>();
			try {
				dbIds = 
						FeatureCollectionUtils.getFeatureIdsForMsFeatureInfoBundleCollection(collection.getId());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(!dbIds.isEmpty())
				collection.getFeatureIds().addAll(dbIds);
		}		
		Set<String>existingIds = collection.getFeatureIds();
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
		collection.addFeatures(featuresToAdd);		
	}
	
	public static void removeFeaturesFromCollection(
			MsFeatureInfoBundleCollection collection, 
			Collection<MSFeatureInfoBundle>featuresToRemove) {
		
		if(!featureCollectionsMSIDSet.contains(collection) || featuresToRemove.isEmpty())
			return;
		
		Set<String>existingIds = collection.getFeatureIds();
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
		collection.removeFeatures(featuresToRemove);
	}

	public static Set<MsFeatureInfoBundleCollection> getfeatureCollectionsMSIDSet() {
		return featureCollectionsMSIDSet;
	}
}

















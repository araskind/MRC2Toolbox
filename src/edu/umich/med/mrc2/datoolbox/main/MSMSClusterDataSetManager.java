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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import edu.umich.med.mrc2.datoolbox.data.compare.MSMSClusterDataSetComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.msclust.IMSMSClusterDataSet;
import edu.umich.med.mrc2.datoolbox.data.msclust.MSMSClusterDataSet;
import edu.umich.med.mrc2.datoolbox.data.msclust.MSMSClusteringParameterSet;
import edu.umich.med.mrc2.datoolbox.database.idt.MSMSClusteringDBUtils;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.idt.IDTMSMSClusterDataPullTask;

public class MSMSClusterDataSetManager {
	
	public static final String CURRENT_MSMS_CLUSTER_SEARCH_RESULT = 
			"Current MSMS cluster search";
	public static final String CURRENT_MS1_CLUSTER_SEARCH_RESULT = 
			"Current MS1 cluster search";	
	public static final IMSMSClusterDataSet msmsClusterSearchResults = 
			new MSMSClusterDataSet(CURRENT_MSMS_CLUSTER_SEARCH_RESULT);	
	public static final IMSMSClusterDataSet msOneClusterSearchResults = 
			new MSMSClusterDataSet(CURRENT_MS1_CLUSTER_SEARCH_RESULT);	
	public static final Map<IMSMSClusterDataSet, Set<String>>clusterDataSetsToClusterIdsMap = 
			new TreeMap<IMSMSClusterDataSet, Set<String>>(
					new MSMSClusterDataSetComparator(SortProperty.Name));
	
	public static Collection<MSMSClusteringParameterSet>msmsClusteringParameters = 
			new HashSet<MSMSClusteringParameterSet>();

	public static void clearDefaultCollections() {		
		msmsClusterSearchResults.clearDataSet();
		msOneClusterSearchResults.clearDataSet();
	}
		
	public static void refreshMSMSClusterDataSetList() {
		clusterDataSetsToClusterIdsMap.clear();
		clusterDataSetsToClusterIdsMap.put(
				msmsClusterSearchResults, new TreeSet<String>());
		clusterDataSetsToClusterIdsMap.put(
				msOneClusterSearchResults, new TreeSet<String>());
		try {
			clusterDataSetsToClusterIdsMap.putAll(
					MSMSClusteringDBUtils.getMSMSClusterDataSets());		
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static Collection<IMSMSClusterDataSet>getMSMSClusterDataSets() {

		if(clusterDataSetsToClusterIdsMap.isEmpty())
			refreshMSMSClusterDataSetList();
		
		return clusterDataSetsToClusterIdsMap.keySet();
	}
	
	public static Collection<IMSMSClusterDataSet>getEditableMSMSClusterDataSets() {
		
		return clusterDataSetsToClusterIdsMap.keySet().stream().
			filter(c -> !c.equals(msmsClusterSearchResults)).
			filter(c -> !c.equals(msOneClusterSearchResults)).
			sorted(new MSMSClusterDataSetComparator(SortProperty.Name)).
			collect(Collectors.toList());
	}
	
	public static IMSMSClusterDataSet getMSMSClusterDataSetById(String id) {
		
		return clusterDataSetsToClusterIdsMap.keySet().stream().
				filter(c -> c.getId().equals(id)).findFirst().orElse(null);
	}
	
	public static IMSMSClusterDataSet getMSMSClusterDataSetByName(String name) {
		
		return clusterDataSetsToClusterIdsMap.keySet().stream().
				filter(c -> c.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
	}
	
	public static IDTMSMSClusterDataPullTask 
							getMSMSClusterDataSetData(IMSMSClusterDataSet dataSet) {
		return new IDTMSMSClusterDataPullTask(dataSet);
	}	
	
	public static int getMSMSClusterDataSetSize(IMSMSClusterDataSet cds) {
		
		if(cds.getClusters() != null && !cds.getClusters().isEmpty())
			return cds.getClusters().size();
		else {
			if(!clusterDataSetsToClusterIdsMap.containsKey(cds))
				return 0;
			else
				return clusterDataSetsToClusterIdsMap.get(cds).size();
		}			
	}
	
	public static IMSMSClusterDataSet getMsmsMSMSClusterDataSetById(String id) {		
		return getMSMSClusterDataSets().stream().
				filter(s -> s.getId().equals(id)).findFirst().orElse(null);
	}
	
	public static void refreshMsmsClusteringParameters() {
		msmsClusteringParameters.clear();
		getMsmsClusteringParameters();
	}

	public static Collection<MSMSClusteringParameterSet> getMsmsClusteringParameters() {

		if(msmsClusteringParameters == null)
			msmsClusteringParameters = new HashSet<MSMSClusteringParameterSet>();
		
		if(msmsClusteringParameters.isEmpty()) {
			try {
				msmsClusteringParameters.addAll(MSMSClusteringDBUtils.getMSMSClusteringParameterSets());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return msmsClusteringParameters;
	}
	
	public static MSMSClusteringParameterSet getMsmsClusteringParameterSetById(String id) {
		
		return getMsmsClusteringParameters().stream().
				filter(p -> p.getId().equals(id)).findFirst().orElse(null);
	}
	
	public static MSMSClusteringParameterSet getMsmsClusteringParameterSetByMd5(String md5) {
		
		return getMsmsClusteringParameters().stream().
				filter(p -> p.getMd5().equals(md5)).findFirst().orElse(null);
	}
}


















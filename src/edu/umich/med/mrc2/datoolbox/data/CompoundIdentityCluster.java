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

package edu.umich.med.mrc2.datoolbox.data;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import edu.umich.med.mrc2.datoolbox.data.compare.CompoundIdentityComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;

public class CompoundIdentityCluster implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -266161926739380097L;

	private String clusterId;
	private Map<CompoundIdentity, CompoundIdentityInfoBundle>idList;
	private CompoundIdentity primaryIdentity;
	
	public CompoundIdentityCluster() {
		super();
		clusterId = 
				DataPrefix.COMPOUND_ID_CLUSTER.getName() + UUID.randomUUID().toString();
		idList = new TreeMap<CompoundIdentity,CompoundIdentityInfoBundle>(
				new CompoundIdentityComparator(SortProperty.NameAndId));
		primaryIdentity = null;
	}

	public String getClusterId() {
		return clusterId;
	}

	public Collection<CompoundIdentity> getIdList() {
		return idList.keySet();
	}
	
	public Collection<CompoundIdentityInfoBundle> getIdInfoBundleList() {
		return idList.values();
	}

	public CompoundIdentity getPrimaryIdentity() {
		return primaryIdentity;
	}

	public void setPrimaryIdentity(CompoundIdentity newPrimaryIdentity) {
		
		if(idList.keySet().contains(newPrimaryIdentity))
			this.primaryIdentity = newPrimaryIdentity;
	}	
	
	public void addIdentity(CompoundIdentityInfoBundle newIdBundle) {		

		idList.put(newIdBundle.getCompoundIdentity(), newIdBundle);		
		finalizeCluster();
	}
	
	public void removeIdentity(CompoundIdentity toRemove) {
		
		if(idList.containsKey(toRemove))
			idList.remove(toRemove);
		
		if(primaryIdentity != null && primaryIdentity.equals(toRemove))
			primaryIdentity = null;
		
		finalizeCluster();
	}
	
	public void removeIdentity(CompoundIdentityInfoBundle toRemove) {
		
		if(idList.containsKey(toRemove.getCompoundIdentity()))
			idList.remove(toRemove.getCompoundIdentity());
		
		if(primaryIdentity != null && primaryIdentity.equals(toRemove.getCompoundIdentity()))
			primaryIdentity = null;
		
		finalizeCluster();
	}
	
	public void finalizeCluster() {
		
		if(primaryIdentity == null && !idList.isEmpty())
			primaryIdentity = idList.keySet().stream().findFirst().orElse(null);
	}
	
	@Override
	public String toString() {
		
		if(primaryIdentity == null)
			return clusterId;
		else
			return primaryIdentity.getName();		
	}
	
	public String getName() {
		
		if(primaryIdentity == null)
			return clusterId;
		else
			return primaryIdentity.getName();		
	}	
}






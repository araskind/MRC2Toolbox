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
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import edu.umich.med.mrc2.datoolbox.data.compare.MsFeatureClusterComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.enums.ParameterSetStatus;

public class MsFeatureClusterSet implements Comparable<MsFeatureClusterSet>, Serializable, Renamable{

	/**
	 *
	 */
	private static final long serialVersionUID = 9196484589262614732L;

	private Set<MsFeatureCluster> clusters;
	private String clusterSetName;
	private boolean active;
	private boolean locked;
	private ParameterSetStatus status;
	private boolean nameIsValid;

	public MsFeatureClusterSet(String clusterSetName) {

		super();
		this.clusterSetName = clusterSetName;
		active = false;
		clusters = new TreeSet<MsFeatureCluster>(
				new MsFeatureClusterComparator(SortProperty.pimaryId));
		status = ParameterSetStatus.CREATED;
	}

	public MsFeatureClusterSet(
			String clusterSetName, 
			Collection<MsFeatureCluster> clustersToAdd) {

		super();
		this.clusterSetName = clusterSetName;
		active = false;
		clusters = new HashSet<MsFeatureCluster>();
		clusters.addAll(clustersToAdd);
		status = ParameterSetStatus.CREATED;
	}

	public void addCluster(MsFeatureCluster clusterToAdd) {

		clusters.add(clusterToAdd);
		status = ParameterSetStatus.CHANGED;
	}

	public void addClusterCollection(Collection<MsFeatureCluster> clustersToAdd) {

		clusters.addAll(clustersToAdd);
		status = ParameterSetStatus.CHANGED;
	}

	public void removeCluster(MsFeatureCluster clusterToremove) {

		clusters.remove(clusterToremove);
		status = ParameterSetStatus.CHANGED;
	}

	public void clearClusters() {

		clusters.clear();
	}

	public MsFeatureSet getPrimaryFeatures() {

		MsFeatureSet primaryFeatures  = new MsFeatureSet(clusterSetName);
		clusters.stream().forEach(c -> primaryFeatures.addFeature(c.getPrimaryFeature()));

		return primaryFeatures;
	}

	//	TODO rewrite when Cluster id rewritten
	public Collection<Assay>getAssays(){

//		return clusters.stream().
//				flatMap(c -> c.getFeatures().stream()).
//				map(f -> f.getAssayMethod()).
//				collect(Collectors.toCollection(TreeSet::new));
		return null;
	}

	@Override
	public void setName(String name) {
		clusterSetName = name;
	}
	@Override
	public String getName() {
		return clusterSetName;
	}

	@Override
	public String toString() {

		return clusterSetName;
	}

	@Override
	public boolean nameIsValid() {

		return nameIsValid;
	}
	@Override
	public void setNameValid(boolean valid) {

		nameIsValid = valid;
	}
	@Override
	public int compareTo(MsFeatureClusterSet  o) {
		return clusterSetName.compareToIgnoreCase(o.getName());
	}

	public boolean isActive() {
		return active;
	}
	public void setActive(boolean active) {
		this.active = active;
	}
	public boolean isLocked() {
		return locked;
	}
	public void setLocked(boolean locked) {
		this.locked = locked;
	}
	public ParameterSetStatus getStatus() {
		return status;
	}
	public void setStatus(ParameterSetStatus status) {
		this.status = status;
	}

	public Set<MsFeatureCluster> getClusters() {
		return clusters;
	}
}

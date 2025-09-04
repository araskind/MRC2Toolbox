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
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import org.jdom2.Element;
import org.ujmp.core.Matrix;

import edu.umich.med.mrc2.datoolbox.data.compare.MsFeatureClusterComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.data.enums.ParameterSetStatus;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.project.store.CommonFields;
import edu.umich.med.mrc2.datoolbox.project.store.ObjectNames;
import edu.umich.med.mrc2.datoolbox.project.store.XmlStorable;

public class MsFeatureClusterSet implements 
		Comparable<MsFeatureClusterSet>, Serializable, Renamable, XmlStorable{
	
	private static final long serialVersionUID = 9196484589262614732L;

	protected String id;
	protected Set<MsFeatureCluster> clusters;
	protected String clusterSetName;
	protected boolean active;
	protected boolean locked;
	protected ParameterSetStatus status;
	protected boolean nameIsValid;
	protected Matrix mergedDataMatrix;

	public MsFeatureClusterSet(String clusterSetName) {

		super();
		this.id = DataPrefix.MS_FEATURE_CLUSTER_SET.getName() 
				+ UUID.randomUUID().toString();
		this.clusterSetName = clusterSetName;
		active = false;
		clusters = new TreeSet<>(
				new MsFeatureClusterComparator(SortProperty.pimaryId));
		status = ParameterSetStatus.CREATED;
	}

	public MsFeatureClusterSet(
			String clusterSetName, 
			Collection<MsFeatureCluster> clustersToAdd) {

		this(clusterSetName);
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

	public String getId() {
		return id;
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
	
	public Matrix getMergedDataMatrix() {
		return mergedDataMatrix;
	}

	public void setMergedDataMatrix(Matrix mergedDataMatrix) {
		this.mergedDataMatrix = mergedDataMatrix;
	}
	
    @Override
    public boolean equals(Object obj) {

		if (obj == this)
			return true;

        if (obj == null)
            return false;

        if (!MsFeatureClusterSet.class.isAssignableFrom(obj.getClass()))
            return false;

        final MsFeatureClusterSet other = (MsFeatureClusterSet) obj;

        if((this.id == null) ? (other.getId() != null) : !this.id.equals(other.getId()))
            return false;

        return true;
    }

    @Override
    public int hashCode() {

        int hash = 3;
        hash = 53 * hash + (this.id != null ? this.id.hashCode() : 0);
        return hash;
    }

	@Override
	public Element getXmlElement() {
		
		Element msFeatureClusterSetElement = 
				new Element(ObjectNames.MsFeatureClusterSet.name());
		msFeatureClusterSetElement.setAttribute(CommonFields.Id.name(), id);
		msFeatureClusterSetElement.setAttribute(CommonFields.Name.name(), clusterSetName);
		msFeatureClusterSetElement.setAttribute(
				CommonFields.Enabled.name(), Boolean.toString(active));
		msFeatureClusterSetElement.setAttribute(
				CommonFields.Locked.name(), Boolean.toString(locked));
		Element clusterListElement = new Element(CommonFields.ItemList.name());
		for(MsFeatureCluster cluster : clusters)
			clusterListElement.addContent(cluster.getXmlElement());
		
		msFeatureClusterSetElement.addContent(clusterListElement);
		return msFeatureClusterSetElement;
	}
	
	public MsFeatureClusterSet(Element msFeatureClusterSetElement, DataAnalysisProject project) {
		
		this(msFeatureClusterSetElement.getAttributeValue(CommonFields.Name.name()));
		id = msFeatureClusterSetElement.getAttributeValue(CommonFields.Id.name());
		active = Boolean.parseBoolean(
				msFeatureClusterSetElement.getAttributeValue(CommonFields.Enabled.name()));
		locked = Boolean.parseBoolean(
				msFeatureClusterSetElement.getAttributeValue(CommonFields.Locked.name()));
		List<Element>clusterElementList = 
				msFeatureClusterSetElement.getChild(CommonFields.ItemList.name()).
				getChildren(ObjectNames.MsFeatureCluster.name());
		for(Element clusterElement : clusterElementList)
			clusters.add(new MsFeatureCluster(clusterElement, project));
	}
}














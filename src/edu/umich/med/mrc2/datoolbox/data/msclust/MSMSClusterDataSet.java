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

package edu.umich.med.mrc2.datoolbox.data.msclust;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.text.WordUtils;
import org.jdom2.Element;

import edu.umich.med.mrc2.datoolbox.data.BinnerAnnotationCluster;
import edu.umich.med.mrc2.datoolbox.data.MSFeatureInfoBundle;
import edu.umich.med.mrc2.datoolbox.data.MinimalMSOneFeature;
import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.data.lims.DataExtractionMethod;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSUser;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.MSMSClusterDataSetManager;
import edu.umich.med.mrc2.datoolbox.project.store.CommonFields;
import edu.umich.med.mrc2.datoolbox.project.store.MSMSClusterDataSetFields;
import edu.umich.med.mrc2.datoolbox.project.store.ObjectNames;
import edu.umich.med.mrc2.datoolbox.project.store.ProjectStoreUtils;
import edu.umich.med.mrc2.datoolbox.utils.ExperimentUtils;

public class MSMSClusterDataSet implements IMSMSClusterDataSet {
	
	private String id;
	private String name;
	private MSMSClusterDataSetType dataSetType;
	private String description;
	private LIMSUser createdBy;
	private Date dateCreated;
	private Date lastModified;
	private MSMSClusteringParameterSet parameters;
	private Set<IMsFeatureInfoBundleCluster>clusters;
	private Set<String>clusterIds;
	private FeatureLookupList featureLookupDataSet;
	private BinnerAnnotationLookupDataSet binnerAnnotationDataSet;
	
	//	TODO specify data set type in constructor
	
	public MSMSClusterDataSet(
			String name, 
			String description, 
			LIMSUser createdBy) {
		this(name, description, createdBy, new Date(), new Date());
	}
	
	public MSMSClusterDataSet(String name) {
		this(name, name, MRC2ToolBoxCore.getIdTrackerUser(), new Date(), new Date());
	}
	
	public MSMSClusterDataSet(
			String id, 
			String name, 
			String description, 
			LIMSUser createdBy, 
			Date dateCreated,
			Date lastModified) {
		this(name, description, createdBy, dateCreated, lastModified);
		this.id = id;
	}

	public MSMSClusterDataSet(
			String name, 
			String description, 
			LIMSUser createdBy, 
			Date dateCreated,
			Date lastModified) {
		super();
		this.id = DataPrefix.MSMS_CLUSTER_DATA_SET.getName() + 
				UUID.randomUUID().toString().substring(0, 12);
		this.name = name;
		this.description = description;
		this.createdBy = createdBy;
		this.dateCreated = dateCreated;
		this.lastModified = lastModified;
		clusters = new HashSet<IMsFeatureInfoBundleCluster>();
		clusterIds = new TreeSet<String>();
	}
	
	public Set<IMsFeatureInfoBundleCluster> getClusters() {
		return clusters;
	}
	
	public void addCluster(MsFeatureInfoBundleCluster newCluster) {
		clusters.add(newCluster);
	}

	public void removeCluster(MsFeatureInfoBundleCluster toRemove) {
		clusters.remove(toRemove);
	}
	
	@Override
	public String getId() {
		return id;
	}

	@Override
	public void setId(String id) {
		this.id = id;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return name;
	}
	
	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public LIMSUser getCreatedBy() {
		return createdBy;
	}

	@Override
	public void setCreatedBy(LIMSUser createdBy) {
		this.createdBy = createdBy;
	}

	@Override
	public Date getDateCreated() {
		return dateCreated;
	}

	@Override
	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}
	
   @Override
    public boolean equals(Object obj) {

		if (obj == this)
			return true;

        if (obj == null)
            return false;

        if (!MSMSClusterDataSet.class.isAssignableFrom(obj.getClass()))
            return false;

        final MSMSClusterDataSet other = (MSMSClusterDataSet) obj;

        if ((this.id == null) ? (other.getId() != null) : !this.id.equals(other.getId()))
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
	public MSMSClusteringParameterSet getParameters() {
		return parameters;
	}

	@Override
	public void setParameters(MSMSClusteringParameterSet parameters) {
		this.parameters = parameters;
	}	
	
	@Override
	public Collection<String>getInjectionIds(){
		
		return clusters.stream().flatMap(c -> c.getComponents().stream()).
				filter(b -> Objects.nonNull(b.getInjectionId())).
				map(b -> b.getInjectionId()).
				collect(Collectors.toSet());
	}
	
	@Override
	public Collection<DataExtractionMethod>getDataExtractionMethods(){
		
		return clusters.stream().flatMap(c -> c.getComponents().stream()).
				filter(b -> Objects.nonNull(b.getDataExtractionMethod())).
				map(b -> b.getDataExtractionMethod()).
				collect(Collectors.toSet());
	}

	@Override
	public Date getLastModified() {
		return lastModified;
	}

	@Override
	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}
	
	public void clearDataSet() {
		clusters.clear();
	}

	public Set<String> getClusterIds() {
		
		if(clusters == null || clusters.isEmpty())			
			return clusterIds;
		else
			return clusters.stream().
					map(c -> c.getId()).collect(Collectors.toSet());
	}

	@Override
	public Element getXmlElement() {

		Element msmsClusterDataSetElement = 
				new Element(ObjectNames.MSMSClusterDataSet.name());
		msmsClusterDataSetElement.setAttribute(CommonFields.Id.name(), id);	
		ProjectStoreUtils.addTextElement(name, msmsClusterDataSetElement, CommonFields.Name);
		ProjectStoreUtils.addDescriptionElement(description, msmsClusterDataSetElement);
		ProjectStoreUtils.setUserIdAttribute(createdBy, msmsClusterDataSetElement);
		ProjectStoreUtils.setDateAttribute(
				dateCreated, CommonFields.DateCreated, msmsClusterDataSetElement);
		ProjectStoreUtils.setDateAttribute(
				lastModified, CommonFields.LastModified, msmsClusterDataSetElement);	
		msmsClusterDataSetElement.setAttribute(
				MSMSClusterDataSetFields.DsType.name(), getDataSetType().name());
		msmsClusterDataSetElement.addContent(parameters.getXmlElement());
		
        Element clusterListElement = 
        		new Element(MSMSClusterDataSetFields.ClusterList.name());
        if(!clusters.isEmpty()) {
        	
        	for(IMsFeatureInfoBundleCluster fbc : clusters)
        		clusterListElement.addContent(fbc.getXmlElement());      	
        }       
        msmsClusterDataSetElement.addContent(clusterListElement);
        
        if(featureLookupDataSet != null )
        	msmsClusterDataSetElement.addContent(featureLookupDataSet.getXmlElement());
        
		return msmsClusterDataSetElement;
	}
	
	public MSMSClusterDataSet(Element xmlElement) {
		
		id = xmlElement.getAttributeValue(CommonFields.Id.name());
		if(id == null)
			id = DataPrefix.MSMS_CLUSTER_DATA_SET.getName() + 
					UUID.randomUUID().toString().substring(0, 12);
		
		//	TODO remove
		this.name = xmlElement.getAttributeValue(CommonFields.Name.name());
		if(name == null)
			name = ProjectStoreUtils.getTextFromElement(xmlElement, CommonFields.Name);
		
		//	TODO remove
		this.description = xmlElement.getAttributeValue(CommonFields.Description.name());
		if(description == null)
			description = ProjectStoreUtils.getDescriptionFromElement(xmlElement);
		
		dateCreated = ProjectStoreUtils.getDateFromAttribute(
				xmlElement, CommonFields.DateCreated);
		lastModified = ProjectStoreUtils.getDateFromAttribute(
				xmlElement, CommonFields.LastModified);
		
		createdBy = ProjectStoreUtils.getUserFromAttribute(xmlElement);
		if(createdBy == null)
			createdBy = MRC2ToolBoxCore.getIdTrackerUser();

		parameters = new MSMSClusteringParameterSet(
				xmlElement.getChild(ObjectNames.MSMSClusteringParameterSet.name()));
		
		Element lookupListElement = 
				xmlElement.getChild(ObjectNames.FeatureLookupDataSet.name());
        if(lookupListElement != null )
        	featureLookupDataSet = new FeatureLookupList(lookupListElement);     
        
		clusters = new HashSet<IMsFeatureInfoBundleCluster>();
		clusterIds = new TreeSet<String>();
		
		List<Element> clusterListElements = 
				xmlElement.getChildren(MSMSClusterDataSetFields.ClusterList.name());
		if(!clusterListElements.isEmpty()) {
			
			List<Element> clusterList = 
					clusterListElements.get(0).getChildren(
							ObjectNames.MsFeatureInfoBundleCluster.name());
			for(Element clusterElement : clusterList) 
				clusters.add(new MsFeatureInfoBundleCluster(clusterElement));			
		}
		String dsTypeString = 
				xmlElement.getAttributeValue(MSMSClusterDataSetFields.DsType.name());
		if(dsTypeString == null)
			getDataSetType();
		else
			dataSetType = MSMSClusterDataSetType.valueOf(dsTypeString);
	}

	public FeatureLookupList getFeatureLookupDataSet() {
		return featureLookupDataSet;
	}
	
	public Collection<MinimalMSOneFeature>getMatchedLookupFeatures(){
		
		return clusters.stream().
				filter(MsFeatureInfoBundleCluster.class::isInstance).
				map(MsFeatureInfoBundleCluster.class::cast).				
				filter(c -> Objects.nonNull(c.getLookupFeature())).
				map(c -> c.getLookupFeature()).
				collect(Collectors.toSet());
	}
	
	public Collection<BinnerAnnotationCluster>getMatchedBinnerAnnotationClusterss(){
		
		return clusters.stream().
				filter(BinnerBasedMsFeatureInfoBundleCluster.class::isInstance).
				map(BinnerBasedMsFeatureInfoBundleCluster.class::cast).				
				filter(c -> Objects.nonNull(c.getBinnerAnnotationCluster())).
				map(c -> c.getBinnerAnnotationCluster()).
				collect(Collectors.toSet());
	}

	public void setFeatureLookupDataSet(FeatureLookupList featureLookupDataSet) {
		this.featureLookupDataSet = featureLookupDataSet;
	}
	
	@Override
	public Collection<MSFeatureInfoBundle> getAllFeatures(){ 
	
		return clusters.stream().
				flatMap(c -> c.getComponents().stream()).distinct().
				collect(Collectors.toList());
	}

	@Override
	public BinnerAnnotationLookupDataSet getBinnerAnnotationDataSet() {
		return binnerAnnotationDataSet;
	}

	public void setBinnerAnnotationDataSet(BinnerAnnotationLookupDataSet binnerAnnotationDataSet) {
		this.binnerAnnotationDataSet = binnerAnnotationDataSet;
	}

	public MSMSClusterDataSetType getDataSetType() {
		
		if(dataSetType == null) {
			
			if(binnerAnnotationDataSet != null)
				dataSetType = MSMSClusterDataSetType.BINNER_ANNOTATION_BASED;
			else
				dataSetType = MSMSClusterDataSetType.FEATURE_BASED;
		}
		return dataSetType;
	}

	public void setDataSetType(MSMSClusterDataSetType dataSetType) {
		this.dataSetType = dataSetType;
	}

	@Override
	public String getFormattedMetadata() {

		String data = "<html><b>" + name + "</b><br>";
		if(description != null && !description.isEmpty())
			data += WordUtils.wrap(description, 80, "<br>", true) + "<br>";
		
		if(createdBy != null)
			data += "<b>Created by: </b>" + createdBy.getFullName() + "<br>";
		
		data += "<b># of clusters: </b>" + Integer.toString(
				MSMSClusterDataSetManager.getMSMSClusterDataSetSize(this)) + "<br>";
		data += "<b>Created on: </b>" + ExperimentUtils.dateTimeFormat.format(dateCreated) + "<br>";
		data += "<b>Last modified on: </b>" + ExperimentUtils.dateTimeFormat.format(lastModified);		
		return data;
	}
}

		









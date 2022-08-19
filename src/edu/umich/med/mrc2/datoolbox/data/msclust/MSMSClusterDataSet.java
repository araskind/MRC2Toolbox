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

import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Collectors;

import org.jdom2.Element;

import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.data.lims.DataExtractionMethod;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSUser;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCash;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.project.store.FeatureLookupDataSetFields;
import edu.umich.med.mrc2.datoolbox.project.store.MSMSClusterDataSetFields;
import edu.umich.med.mrc2.datoolbox.project.store.MSMSClusteringParameterSetFields;
import edu.umich.med.mrc2.datoolbox.project.store.MsFeatureInfoBundleClusterFields;
import edu.umich.med.mrc2.datoolbox.utils.ProjectUtils;

public class MSMSClusterDataSet {
	
	private String id;
	private String name;
	private String description;
	private LIMSUser createdBy;
	private Date dateCreated;
	private Date lastModified;
	private MSMSClusteringParameterSet parameters;
	private Set<MsFeatureInfoBundleCluster>clusters;
	private Set<String>clusterIds;
	private FeatureLookupDataSet featureLookupDataSet;
	
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
		clusters = new HashSet<MsFeatureInfoBundleCluster>();
		clusterIds = new TreeSet<String>();
	}
	
	public Set<MsFeatureInfoBundleCluster> getClusters() {
		return clusters;
	}
	
	public void addCluster(MsFeatureInfoBundleCluster newCluster) {
		clusters.add(newCluster);
	}

	public void removeCluster(MsFeatureInfoBundleCluster toRemove) {
		clusters.remove(toRemove);
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public LIMSUser getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(LIMSUser createdBy) {
		this.createdBy = createdBy;
	}

	public Date getDateCreated() {
		return dateCreated;
	}

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

	public MSMSClusteringParameterSet getParameters() {
		return parameters;
	}

	public void setParameters(MSMSClusteringParameterSet parameters) {
		this.parameters = parameters;
	}	
	
	public Collection<String>getInjectionIds(){
		
		return clusters.stream().flatMap(c -> c.getComponents().stream()).
				filter(b -> Objects.nonNull(b.getInjectionId())).
				map(b -> b.getInjectionId()).
				collect(Collectors.toSet());
	}
	
	public Collection<DataExtractionMethod>getDataExtractionMethods(){
		
		return clusters.stream().flatMap(c -> c.getComponents().stream()).
				filter(b -> Objects.nonNull(b.getDataExtractionMethod())).
				map(b -> b.getDataExtractionMethod()).
				collect(Collectors.toSet());
	}

	public Date getLastModified() {
		return lastModified;
	}

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

	public Element getXmlElement() {

		Element msmsClusterDataSetElement = 
				new Element(MSMSClusterDataSetFields.MSMSClusterDataSet.name());
		msmsClusterDataSetElement.setAttribute(
				MSMSClusterDataSetFields.Id.name(), id);	
		msmsClusterDataSetElement.setAttribute(
				MSMSClusterDataSetFields.Name.name(), name);
		String descString = description;
		if(descString == null)
			descString = "";
		
		msmsClusterDataSetElement.setAttribute(
				MSMSClusterDataSetFields.Description.name(), descString);
		msmsClusterDataSetElement.setAttribute(
				MSMSClusterDataSetFields.UserId.name(), createdBy.getId());	
		msmsClusterDataSetElement.setAttribute(
				MSMSClusterDataSetFields.DateCreated.name(), 
				ProjectUtils.dateTimeFormat.format(dateCreated));
		msmsClusterDataSetElement.setAttribute(
				MSMSClusterDataSetFields.LastModified.name(), 
				ProjectUtils.dateTimeFormat.format(lastModified));		

		msmsClusterDataSetElement.addContent(parameters.getXmlElement());
		
        Element clusterListElement = 
        		new Element(MSMSClusterDataSetFields.ClusterList.name());
        if(!clusters.isEmpty()) {
        	
        	for(MsFeatureInfoBundleCluster fbc : clusters)
        		clusterListElement.addContent(fbc.getXmlElement());      	
        }       
        msmsClusterDataSetElement.addContent(clusterListElement);
        
        if(featureLookupDataSet != null )
        	msmsClusterDataSetElement.addContent(featureLookupDataSet.getXmlElement());
 	
		return msmsClusterDataSetElement;
	}
	
	public MSMSClusterDataSet(Element xmlElement) {
		
		this.id = xmlElement.getAttributeValue(MSMSClusterDataSetFields.Id.name());
		if(id == null)
			this.id = DataPrefix.MSMS_CLUSTER_DATA_SET.getName() + 
					UUID.randomUUID().toString().substring(0, 12);
		
		this.name = xmlElement.getAttributeValue(MSMSClusterDataSetFields.Name.name());
		this.description = xmlElement.getAttributeValue(MSMSClusterDataSetFields.Description.name());
		try {
			this.dateCreated = ProjectUtils.dateTimeFormat.parse(
					xmlElement.getAttributeValue(MSMSClusterDataSetFields.DateCreated.name()));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			this.lastModified = ProjectUtils.dateTimeFormat.parse(
					xmlElement.getAttributeValue(MSMSClusterDataSetFields.LastModified.name()));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		String userId =  xmlElement.getAttributeValue(MSMSClusterDataSetFields.UserId.name());
		if(userId != null)
			this.createdBy = IDTDataCash.getUserById(userId);
		else
			this.createdBy = MRC2ToolBoxCore.getIdTrackerUser();

		parameters = new MSMSClusteringParameterSet(
				xmlElement.getChild(MSMSClusteringParameterSetFields.MSMSClusteringParameterSet.name()));
		
		Element lookupListElement = 
				xmlElement.getChild(FeatureLookupDataSetFields.FeatureLookupDataSet.name());
        if(lookupListElement != null )
        	featureLookupDataSet = new FeatureLookupDataSet(lookupListElement);
        
		clusters = new HashSet<MsFeatureInfoBundleCluster>();
		clusterIds = new TreeSet<String>();
		
		List<Element> clusterListElements = 
				xmlElement.getChildren(MSMSClusterDataSetFields.ClusterList.name());
		if(clusterListElements.size() > 0) {
			
			List<Element> clusterList = 
					clusterListElements.get(0).getChildren(MsFeatureInfoBundleClusterFields.MsFeatureInfoBundleCluster.name());
			for(Element clusterElement : clusterList) {
				
				MsFeatureInfoBundleCluster newCluster = 
						new MsFeatureInfoBundleCluster(clusterElement);
				if(newCluster != null)
					clusters.add(newCluster);
			}
		}
	}

	public FeatureLookupDataSet getFeatureLookupDataSet() {
		return featureLookupDataSet;
	}

	public void setFeatureLookupDataSet(FeatureLookupDataSet featureLookupDataSet) {
		this.featureLookupDataSet = featureLookupDataSet;
	}
}

		









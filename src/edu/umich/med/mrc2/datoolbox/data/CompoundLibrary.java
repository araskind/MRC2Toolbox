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
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;

import org.jdom2.Element;

import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.data.enums.Polarity;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.project.store.CommonFields;
import edu.umich.med.mrc2.datoolbox.project.store.ObjectNames;
import edu.umich.med.mrc2.datoolbox.project.store.ProjectStoreUtils;
import edu.umich.med.mrc2.datoolbox.project.store.XmlStorable;

public class CompoundLibrary implements Serializable, Comparable<CompoundLibrary>, XmlStorable {

	/**
	 *
	 */
	private static final long serialVersionUID = 8587732551371736548L;
	
	private String id;
	private String name;
	private String description;
	private DataPipeline dataPipeline;
	private Date dateCreated;
	private Date lastModified;
	private boolean enabled;
	private Polarity polarity;

	private Collection<LibraryMsFeature> libraryFeatures;
	
	public CompoundLibrary(
			String id, 
			String name, 
			String description, 
			DataPipeline dataPipeline, 
			Date dateCreated,
			Date lastModified, 
			boolean enabled, 
			Polarity polarity) {
		super();
		this.id = id;
		this.name = name;
		this.description = description;
		this.dataPipeline = dataPipeline;
		this.dateCreated = dateCreated;
		this.lastModified = lastModified;
		this.enabled = enabled;
		this.polarity = polarity;
		
		libraryFeatures = new HashSet<LibraryMsFeature>();
	}

	public CompoundLibrary(
			String libraryId,
			String libraryName,
			String libraryDescription,
			Date dateCreated,
			Date lastModified,
			boolean enabled){

		this(libraryId, libraryName, libraryDescription, 
				null, dateCreated, lastModified, enabled, null);
	}
	
	public CompoundLibrary(
			String libraryId,
			String libraryName,
			String libraryDescription,
			DataPipeline dataPipeline) {
		
		this(libraryId, libraryName, libraryDescription, 
				dataPipeline, new Date(), new Date(), true, null);
	}
	
	public CompoundLibrary(
			String libraryName,
			String libraryDescription,
			Polarity polarity) {
		
		this(null, libraryName, libraryDescription, 
				null, new Date(), new Date(), true, polarity);
	}

	public CompoundLibrary(String libraryName) {
		
		this(DataPrefix.MS_LIBRARY.getName() + UUID.randomUUID().toString(), 
				libraryName, null, 
				null, new Date(), new Date(), true, null);
	}

	public CompoundLibrary(String libraryName, String libraryDescription) {
		
		this(DataPrefix.MS_LIBRARY.getName() + UUID.randomUUID().toString(), 
				libraryName, libraryDescription, 
				null, new Date(), new Date(), true, null);
	}

	public void addFeature(LibraryMsFeature newFeature) {
		libraryFeatures.add(newFeature);
	}

	public void addFeatures(Set<LibraryMsFeature> newFeatures) {
			libraryFeatures.addAll(newFeatures);
	}

	public Map<String, MsFeature>getNameMap(){

		Map<String, MsFeature>idMap = new TreeMap<String, MsFeature>();
		libraryFeatures.stream().forEach(f -> idMap.put(f.getName(), f));
		return idMap;
	}

	public void removeFeature(LibraryMsFeature newFeature) {

		libraryFeatures.remove(newFeature);
	}

	public void removeFeatures(Collection<LibraryMsFeature> toremove) {
		libraryFeatures.removeAll(toremove);
	}

	@Override
	public int compareTo(CompoundLibrary o) {
		return name.compareToIgnoreCase(o.getLibraryName());
	}

	public Collection<LibraryMsFeature>getFeatures() {
		return libraryFeatures;
	}

	public Collection<LibraryMsFeature>getSortedFeatures(Comparator<MsFeature>sorter) {
		return libraryFeatures.stream().
				sorted(sorter).collect(Collectors.toList());
	}

	public DataPipeline getDataPipeline() {
		return dataPipeline;
	}

	public Date getDateCreated() {
		return dateCreated;
	}
	
	public LibraryMsFeature getFeatureByName(String name) {
		return libraryFeatures.stream().
				filter(f -> f.getName().equals(name)).findFirst().orElse(null);
	}

	public LibraryMsFeature getFeatureById(String targetId) {
		return libraryFeatures.stream().
				filter(f -> f.getId().equals(targetId)).findFirst().orElse(null);
	}

	public Date getLastModified() {
		return lastModified;
	}

	public String getLibraryDescription() {
		return description;
	}

	public String getLibraryId() {
		return id;
	}

	public String getLibraryName() {
		return name;
	}

	public String toString() {
		return name;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setDataPipeline(DataPipeline dataPipeline) {
		
		this.dataPipeline = dataPipeline;
		if(dataPipeline != null 
				&& dataPipeline.getAcquisitionMethod() != null)
			this.polarity = dataPipeline.getAcquisitionMethod().getPolarity();
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}

	public void setLibraryDescription(String libraryDescription) {
		this.description = libraryDescription;
	}

	public void setLibraryName(String libraryName) {
		this.name = libraryName;
	}

    @Override
    public boolean equals(Object obj) {

		if (obj == this)
			return true;

        if (obj == null)
            return false;

        if (!CompoundLibrary.class.isAssignableFrom(obj.getClass()))
            return false;

        final CompoundLibrary other = (CompoundLibrary) obj;

        if ((this.name == null) ? (other.getLibraryName() != null) : !this.name.equals(other.getLibraryName()))
            return false;

        return true;
    }

    @Override
    public int hashCode() {

        int hash = 3;
        hash = 53 * hash + (this.name != null ? this.name.hashCode() : 0);
        return hash;
    }

	public Polarity getPolarity() {
		return polarity;
	}

	public void setPolarity(Polarity polarity) {
		this.polarity = polarity;
	}

	public void setLibraryId(String id) {
		this.id = id;
	}
	
	public CompoundLibrary(Element compoundLibraryElement){
		
		id = compoundLibraryElement.getAttributeValue(CommonFields.Id.name());
		
		//	TODO remove
		name = compoundLibraryElement.getAttributeValue(CommonFields.Name.name());
		if(name == null)
			name = ProjectStoreUtils.getTextFromElement(compoundLibraryElement, CommonFields.Name);
		//	TODO remove
		description = compoundLibraryElement.getAttributeValue(
				CommonFields.Description.name());
		if(description == null)
			description = ProjectStoreUtils.getDescriptionFromElement(compoundLibraryElement);
		
		dateCreated = ProjectStoreUtils.getDateFromAttribute(
				compoundLibraryElement, CommonFields.DateCreated);
		lastModified = ProjectStoreUtils.getDateFromAttribute(
				compoundLibraryElement, CommonFields.LastModified);
		libraryFeatures = new HashSet<LibraryMsFeature>();
		Element dataPipelineElement = 
				compoundLibraryElement.getChild(ObjectNames.DataPipeline.name());
		if(dataPipelineElement != null)
			dataPipeline = new DataPipeline(dataPipelineElement);
		
		enabled = Boolean.parseBoolean(
				compoundLibraryElement.getAttributeValue(CommonFields.Enabled.name()));
		
		String polarityCode = 
				compoundLibraryElement.getAttributeValue(CommonFields.Polarity.name());
		if(polarityCode != null)
			polarity = Polarity.getPolarityByCode(polarityCode);
		
		Element featureListElement = 
				compoundLibraryElement.getChild(CommonFields.FeatureList.name());
		if(featureListElement != null) {
			
			List<Element>libFeatureElementList = 
					featureListElement.getChildren(ObjectNames.LibraryMsFeature.name());
			for(Element libFeatureElement : libFeatureElementList) {
				
				LibraryMsFeature libFeature = new LibraryMsFeature(libFeatureElement);
				libraryFeatures.add(libFeature);
			}
		}	
	}
	
	@Override
	public Element getXmlElement() {
		
		Element compoundLibraryElement = 
				new Element(ObjectNames.CompoundLibrary.name());
		compoundLibraryElement.setAttribute(CommonFields.Id.name(), id);
		ProjectStoreUtils.addTextElement(name, compoundLibraryElement, CommonFields.Name);
		ProjectStoreUtils.addDescriptionElement(description, compoundLibraryElement);		
		ProjectStoreUtils.setDateAttribute(
				dateCreated, CommonFields.DateCreated, compoundLibraryElement);
		ProjectStoreUtils.setDateAttribute(
				lastModified, CommonFields.LastModified, compoundLibraryElement);
		
		if(dataPipeline != null)
			compoundLibraryElement.addContent(dataPipeline.getXmlElement());
		
		compoundLibraryElement.setAttribute(
				CommonFields.Enabled.name(), Boolean.toString(enabled));
		
		if(polarity != null)
			compoundLibraryElement.setAttribute(
					CommonFields.Polarity.name(), polarity.getCode());
		
		Element featureListElement = new Element(CommonFields.FeatureList.name());
		if(libraryFeatures != null && !libraryFeatures.isEmpty())			
			libraryFeatures.stream().
				forEach(lf -> featureListElement.addContent(lf.getXmlElement()));		
		
		compoundLibraryElement.addContent(featureListElement);
		
		return compoundLibraryElement;
	}
}













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

package edu.umich.med.mrc2.datoolbox.data.lims;

import java.io.Serializable;
import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.jdom2.Element;

import edu.umich.med.mrc2.datoolbox.data.AnnotatedObject;
import edu.umich.med.mrc2.datoolbox.data.compare.AnalysisMethodComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.enums.AnnotatedObjectType;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCash;
import edu.umich.med.mrc2.datoolbox.project.store.SamplePreparationFields;
import edu.umich.med.mrc2.datoolbox.utils.ProjectUtils;

public class LIMSSamplePreparation implements 
		Serializable, AnnotatedObject, Comparable<LIMSSamplePreparation>{

	/**
	 * 
	 */
	private static final long serialVersionUID = -6800949927068144426L;
	private String prepId;
	private String name;
	private Date prepDate;
	private LIMSUser creator;
	private Collection<LIMSProtocol>protocols;
	private Map<String,String>prepItemMap;
	private TreeSet<DataAcquisitionMethod>assays;
	private TreeSet<ObjectAnnotation> annotations;

	public LIMSSamplePreparation(
			String prepId, 
			String name, 
			Date prepDate, 
			LIMSUser creator) {
		super();
		this.prepId = prepId;
		this.name = name;
		this.prepDate = prepDate;
		this.creator = creator;
		protocols = new TreeSet<LIMSProtocol>();
		prepItemMap = new TreeMap<String,String>();		
		assays = new TreeSet<DataAcquisitionMethod>(
				new AnalysisMethodComparator(SortProperty.Name));
	}
	
	public LIMSSamplePreparation(LIMSSamplePreparation prep) {
		super();
		this.prepId = prep.getId();
		this.name = prep.getName();
		this.prepDate = prep.getPrepDate();
		this.creator = prep.getCreator();
		protocols = new TreeSet<LIMSProtocol>();
		protocols.addAll(prep.getProtocols());
		prepItemMap = new TreeMap<String,String>();	
		prepItemMap.putAll(prep.getPrepItemMap());
		assays = new TreeSet<DataAcquisitionMethod>(
				new AnalysisMethodComparator(SortProperty.Name));
		assays.addAll(prep.getAssays());
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the prepDate
	 */
	public Date getPrepDate() {
		return prepDate;
	}

	/**
	 * @return the creator
	 */
	public LIMSUser getCreator() {
		return creator;
	}

	/**
	 * @return the protocol
	 */
	public Collection<LIMSProtocol> getProtocols() {
		return protocols;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @param prepDate the prepDate to set
	 */
	public void setPrepDate(Date prepDate) {
		this.prepDate = prepDate;
	}

	/**
	 * @param creator the creator to set
	 */
	public void setCreator(LIMSUser creator) {
		this.creator = creator;
	}

	/**
	 * @param protocol the protocol to set
	 */
	public void addProtocol(LIMSProtocol protocol) {
		protocols.add(protocol);
	}

	public void removeProtocol(LIMSProtocol protocol) {
		protocols.remove(protocol);
	}

	@Override
	public int compareTo(LIMSSamplePreparation o) {
		return prepId.compareTo(o.getId());
	}

	@Override
	public String toString() {
		return name;
	}

    @Override
    public boolean equals(Object obj) {

        if (obj == null)
            return false;
        
		if (obj == this)
			return true;

        if (!LIMSSamplePreparation.class.isAssignableFrom(obj.getClass()))
            return false;

        final LIMSSamplePreparation other = (LIMSSamplePreparation) obj;
        if ((this.prepId == null) ? (other.getId() != null) : !this.prepId.equals(other.getId()))
            return false;

        return true;
    }

    @Override
    public int hashCode() {

        int hash = 3;
        hash = 53 * hash + (this.prepId != null ? this.prepId.hashCode() : 0);
        return hash;
    }

    public void addPrepItem(String sampleId, String prepItemId) {
    	prepItemMap.put(prepItemId, sampleId);
    }

    public Collection<String>getPrepItemsForSample(String sampleId){

    	Collection<String>prepItems = new TreeSet<String>();
    	prepItemMap.forEach((k,v) -> {
    		if(v.equals(sampleId))
    			prepItems.add(k);
    		});
    	return prepItems;
    }

    public boolean hasRedundantSamples() {
    	return
    		(!(prepItemMap.values().stream().distinct().count() == prepItemMap.size()));
    }

	/**
	 * @return the prepItemMap
	 */
	public Map<String, String> getPrepItemMap() {
		return prepItemMap;
	}

	/**
	 * @return the assays
	 */
	public Set<DataAcquisitionMethod> getAssays() {
		return assays;
	}

	@Override
	public Collection<ObjectAnnotation> getAnnotations() {
		if(annotations == null)
			annotations = new TreeSet<ObjectAnnotation>();
		
		return annotations;
	}

	@Override
	public void addAnnotation(ObjectAnnotation annotation) {
		getAnnotations().add(annotation);
	}

	@Override
	public void removeAnnotation(ObjectAnnotation annotation) {
		// TODO Auto-generated method stub
		getAnnotations().remove(annotation);
	}

	@Override
	public void setAnnotatedObjectType(AnnotatedObjectType type) {
		
	}

	@Override
	public AnnotatedObjectType getAnnotatedObjectType() {
		return AnnotatedObjectType.SAMPLE_PREP;
	}

	@Override
	public String getId() {
		return prepId;
	}

	@Override
	public void setId(String uniqueId) {
		prepId = uniqueId;		
	}
	
	public Element getXmlElement() {
		
		Element prepElement = 
				new Element(SamplePreparationFields.SamplePrep.name());
		
		if(prepId != null)
			prepElement.setAttribute(
					SamplePreparationFields.Id.name(), prepId);	
		
		if(name != null)
			prepElement.setAttribute(
					SamplePreparationFields.Name.name(), name);

		if(prepDate == null)
			prepDate = new Date();
		
		prepElement.setAttribute(SamplePreparationFields.PrepDate.name(), 
				ProjectUtils.dateTimeFormat.format(prepDate));
		
		if(creator != null)
			prepElement.setAttribute(
					SamplePreparationFields.Creator.name(), creator.getId());	
		
		String protocolIds = "";
		if(!protocols.isEmpty()) {
			List<String> protocolIdList = protocols.stream().
					map(p -> p.getSopId()).collect(Collectors.toList());
			protocolIds = StringUtils.join(protocolIdList, ",");
		}
		Element protocolElement = 
				new Element(SamplePreparationFields.Protocols.name()).setText(protocolIds);
		prepElement.addContent(protocolElement);
		
//		String documentIds = "";
//		if(!protocols.isEmpty()) {
//			List<String> protocolIdList = protocols.stream().
//					map(p -> p.getSopId()).collect(Collectors.toList());
//			documentIds = StringUtils.join(protocolIdList, ",");
//		}
//		Element documentsElement = 
//				new Element(SamplePreparationFields.Docs.name()).setText(documentIds);
//		prepElement.addContent(documentsElement);
		
		String itemMapString = "";
		if(!prepItemMap.isEmpty()) {
			List<String> mapList = prepItemMap.entrySet().stream().
					map(i -> (i.getKey() + "," + i.getValue())).
					collect(Collectors.toList());
			itemMapString = StringUtils.join(mapList, ";");
		}
		Element itemMapElement = 
				new Element(SamplePreparationFields.ItemMap.name()).setText(itemMapString);
		prepElement.addContent(itemMapElement);
		
		return prepElement;
	}
	
	public LIMSSamplePreparation(Element prepElement) {
		
		prepId = prepElement.getAttributeValue(
				SamplePreparationFields.Id.name());
		name = prepElement.getAttributeValue(
				SamplePreparationFields.Name.name());
		
		prepDate = new Date();
		String startDateString = 
				prepElement.getAttributeValue(SamplePreparationFields.PrepDate.name());
		if(startDateString != null) {
			try {
				prepDate = ProjectUtils.dateTimeFormat.parse(startDateString);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
		}
		String userId = 
				prepElement.getAttributeValue(SamplePreparationFields.Creator.name());
		if(userId != null)
			creator = IDTDataCash.getUserById(userId);

		protocols = new TreeSet<LIMSProtocol>();
		Element protocolsElement =
				prepElement.getChild(SamplePreparationFields.Protocols.name());
		if(protocolsElement != null && !protocolsElement.getText().isEmpty()) {
			
			String[]protocolIds = protocolsElement.getText().split(",");
			for(String protocolId : protocolIds) {
				
				LIMSProtocol protocol = IDTDataCash.getProtocolById(protocolId);
				if(protocol != null)
					protocols.add(protocol);
			}		
		}
		prepItemMap = new TreeMap<String,String>();	
		Element prepItemMapElement =
				prepElement.getChild(SamplePreparationFields.ItemMap.name());
		if(prepItemMapElement != null && !prepItemMapElement.getText().isEmpty()) {
			
			String[]pmElements = prepItemMapElement.getText().split(";");
			for(String element : pmElements) {
				
				String[]mItems = element.split(",");
				if(mItems.length == 2) 
					prepItemMap.put(mItems[0], mItems[1]);				
			}
		}
	}
}














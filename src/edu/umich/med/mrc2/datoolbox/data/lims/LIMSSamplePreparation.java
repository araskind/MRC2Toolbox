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
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import edu.umich.med.mrc2.datoolbox.data.AnnotatedObject;
import edu.umich.med.mrc2.datoolbox.data.compare.AnalysisMethodComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.enums.AnnotatedObjectType;

public class LIMSSamplePreparation implements Serializable, AnnotatedObject, Comparable<LIMSSamplePreparation>{

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

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public LIMSSamplePreparation(String prepId, String name, Date prepDate, LIMSUser creator) {
		super();
		this.prepId = prepId;
		this.name = name;
		this.prepDate = prepDate;
		this.creator = creator;
		protocols = new TreeSet<LIMSProtocol>();
		prepItemMap = new TreeMap<String,String>();		
		Comparator comparator = new AnalysisMethodComparator(SortProperty.Name);
		assays = new TreeSet<DataAcquisitionMethod>(comparator);
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
}














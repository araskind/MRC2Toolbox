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

package edu.umich.med.mrc2.datoolbox.data.lims;

import java.io.Serializable;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import org.jdom2.Element;

import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.main.config.NumberFormatStore;
import edu.umich.med.mrc2.datoolbox.project.store.CommonFields;
import edu.umich.med.mrc2.datoolbox.project.store.MobilePhaseFields;
import edu.umich.med.mrc2.datoolbox.project.store.ObjectNames;
import edu.umich.med.mrc2.datoolbox.project.store.XmlStorable;

public class MobilePhase implements Serializable, Comparable<MobilePhase>, XmlStorable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -9155091023958759740L;
	private String id;
	private String name;
	private Set<String>synonyms;
	private double startingPercentage;
	
	public MobilePhase(String id, String name) {
		super();
		this.id = id;
		if(id == null)
			id = DataPrefix.MOBILE_PHASE.getName() + 
				UUID.randomUUID().toString().substring(0, 6);
		
		this.name = name;
		synonyms = new TreeSet<String>();
	}
	
	public MobilePhase(String name) {
		this(null, name);
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	@Override
	public int compareTo(MobilePhase o) {
		return id.compareTo(o.getId());
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	@Override
	public boolean equals(Object obj) {

		if (obj == this)
			return true;

		if (obj == null)
			return false;

		if (!MobilePhase.class.isAssignableFrom(obj.getClass()))
			return false;

		final MobilePhase other = (MobilePhase) obj;

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

	public Set<String> getSynonyms() {
		return synonyms;
	}

	public double getStartingPercentage() {
		return startingPercentage;
	}

	public void setStartingPercentage(double startingPercentage) {
		this.startingPercentage = startingPercentage;
	}
	
	public MobilePhase(Element mobilePhaseElement) {
		
		super();
		id = mobilePhaseElement.getAttributeValue(CommonFields.Id.name());
		name = mobilePhaseElement.getAttributeValue(CommonFields.Name.name());
		startingPercentage = Double.parseDouble(mobilePhaseElement.getAttributeValue(
				MobilePhaseFields.starPcnt.name()));
		
		synonyms = new TreeSet<String>();
		List<Element> synonymsListList = 
				mobilePhaseElement.getChild(MobilePhaseFields.SynonymList.name()).
				getChildren(MobilePhaseFields.Syn.name());
		
		for(Element synonymElement : synonymsListList)
			synonyms.add(synonymElement.getText());	
	}

	@Override
	public Element getXmlElement() {
		
		Element mobilePhaseElement = 
			new Element(ObjectNames.MobilePhase.name());
		mobilePhaseElement.setAttribute(
			CommonFields.Id.name(), id);
		mobilePhaseElement.setAttribute(
			CommonFields.Name.name(), name);

		mobilePhaseElement.setAttribute(
				MobilePhaseFields.starPcnt.name(), 
				NumberFormatStore.getDecimalFormatWithPrecision(3).format(startingPercentage));
		
		Element synonymsListList = 
				new Element(MobilePhaseFields.SynonymList.name());
		for(String synonym : synonyms) {
			
			Element mpspElement = new Element(MobilePhaseFields.Syn.name());
			mpspElement.setText(synonym);
			synonymsListList.addContent(mpspElement);
		}
		mobilePhaseElement.addContent(synonymsListList);

		return mobilePhaseElement;
	}
}

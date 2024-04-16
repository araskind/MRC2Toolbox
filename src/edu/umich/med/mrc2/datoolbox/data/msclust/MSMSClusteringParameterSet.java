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

import java.util.UUID;

import org.jdom2.Element;

import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.data.enums.MassErrorType;
import edu.umich.med.mrc2.datoolbox.project.store.MSMSClusteringParameterSetFields;
import edu.umich.med.mrc2.datoolbox.utils.MSMSClusteringUtils;

public class MSMSClusteringParameterSet {

	protected String id;
	protected String name;
	protected double mzErrorValue;
	protected MassErrorType massErrorType;
	protected double rtErrorValue;
	protected double msmsSimilarityCutoff;
	protected String md5;
	
	public MSMSClusteringParameterSet() {
		super();
		this.id = DataPrefix.MSMS_CLUSTERING_PARAM_SET.getName() + 
				UUID.randomUUID().toString().substring(0, 12);
	}
				
	public MSMSClusteringParameterSet(
			String id, 
			String name, 
			double mzErrorValue, 
			MassErrorType massErrorType,
			double rtErrorValue, 
			double msmsSimilarityCutoff,
			String md5) {
		this(name, mzErrorValue, massErrorType, rtErrorValue, msmsSimilarityCutoff);
		this.id = id;
		this.md5 = md5;
	}

	public MSMSClusteringParameterSet(
			String name, 
			double mzErrorValue, 
			MassErrorType massErrorType,
			double rtErrorValue, 
			double msmsSimilarityCutoff) {
		
		this.id = DataPrefix.MSMS_CLUSTERING_PARAM_SET.getName() + 
				UUID.randomUUID().toString().substring(0, 12);
		this.name = name;
		this.mzErrorValue = mzErrorValue;
		this.massErrorType = massErrorType;
		this.rtErrorValue = rtErrorValue;
		this.msmsSimilarityCutoff = msmsSimilarityCutoff;
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

	public void setName(String name) {
		this.name = name;
	}

	public double getMzErrorValue() {
		return mzErrorValue;
	}

	public void setMzErrorValue(double mzErrorValue) {
		this.mzErrorValue = mzErrorValue;
	}

	public MassErrorType getMassErrorType() {
		return massErrorType;
	}

	public void setMassErrorType(MassErrorType massErrorType) {
		this.massErrorType = massErrorType;
	}

	public double getRtErrorValue() {
		return rtErrorValue;
	}

	public void setRtErrorValue(double rtErrorValue) {
		this.rtErrorValue = rtErrorValue;
	}

	public double getMsmsSimilarityCutoff() {
		return msmsSimilarityCutoff;
	}

	public void setMsmsSimilarityCutoff(double msmsSimilarityCutoff) {
		this.msmsSimilarityCutoff = msmsSimilarityCutoff;
	}

	public String getMd5() {
		return md5;
	}

	public void setMd5(String md5) {
		this.md5 = md5;
	}
	
   @Override
    public boolean equals(Object obj) {

		if (obj == this)
			return true;

        if (obj == null)
            return false;

        if (!MSMSClusteringParameterSet.class.isAssignableFrom(obj.getClass()))
            return false;

        final MSMSClusteringParameterSet other = (MSMSClusteringParameterSet) obj;

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

	public Element getXmlElement() {

		Element msmsClusteringParameterSetElement = 
				new Element(MSMSClusteringParameterSetFields.MSMSClusteringParameterSet.name());
		msmsClusteringParameterSetElement.setAttribute(
				MSMSClusteringParameterSetFields.Id.name(), id);	

		String nameString = name;
		if(nameString == null)
			nameString = "";
		
		msmsClusteringParameterSetElement.setAttribute(
				MSMSClusteringParameterSetFields.Name.name(), nameString);		
		msmsClusteringParameterSetElement.setAttribute(
				MSMSClusteringParameterSetFields.MZErrorValue.name(), 
				Double.toString(mzErrorValue));	
		msmsClusteringParameterSetElement.setAttribute(
				MSMSClusteringParameterSetFields.MassErrorType.name(), 
				massErrorType.name());
		msmsClusteringParameterSetElement.setAttribute(
				MSMSClusteringParameterSetFields.RTErrorValue.name(), 
				Double.toString(rtErrorValue));	
		msmsClusteringParameterSetElement.setAttribute(
				MSMSClusteringParameterSetFields.MSMSSimilarityCutoff.name(), 
				Double.toString(msmsSimilarityCutoff));
		
		return msmsClusteringParameterSetElement;
	}
	
	public MSMSClusteringParameterSet(Element xmlElement) {
				
		id = xmlElement.getAttributeValue(MSMSClusteringParameterSetFields.Id.name());
		name = xmlElement.getAttributeValue(MSMSClusteringParameterSetFields.Name.name());
		if(name.isEmpty())
			name = null;
		
		mzErrorValue = Double.parseDouble(
				xmlElement.getAttributeValue(
						MSMSClusteringParameterSetFields.MZErrorValue.name()));
		massErrorType = MassErrorType.getTypeByName(
				xmlElement.getAttributeValue(
						MSMSClusteringParameterSetFields.MassErrorType.name()));
		rtErrorValue = Double.parseDouble(
				xmlElement.getAttributeValue(
						MSMSClusteringParameterSetFields.RTErrorValue.name()));
		msmsSimilarityCutoff = Double.parseDouble(
				xmlElement.getAttributeValue(
						MSMSClusteringParameterSetFields.MSMSSimilarityCutoff.name()));

		md5 = MSMSClusteringUtils.calculateClusteringParametersMd5(this);
	}
}


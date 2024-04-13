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

package edu.umich.med.mrc2.datoolbox.msmsscore;

import java.util.UUID;

import org.jdom2.Element;

import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.data.enums.MassErrorType;
import edu.umich.med.mrc2.datoolbox.project.store.MSMSSearchParameterSetFields;
import edu.umich.med.mrc2.datoolbox.utils.MSMSSearchUtils;

public class MSMSSearchParameterSet {

	private String id;
	private String name;
	private double mzErrorValue;
	private MassErrorType massErrorType;
	private double rtErrorValue;
	private boolean ignoreRt;
	private double msmsSimilarityCutoff;	
	private double entropyScoreMassError;
	private MassErrorType entropyScoreMassErrorType;
	private double entropyScoreNoiseCutoff;	
	private String md5;
			
	public MSMSSearchParameterSet() {
		super();
		this.id = DataPrefix.MSMS_SEARCH_PARAM_SET.getName() + 
				UUID.randomUUID().toString().substring(0, 12);
	}

	public MSMSSearchParameterSet(
			String id, 
			String name, 
			double mzErrorValue, 
			MassErrorType massErrorType,
			double rtErrorValue, 
			boolean ignoreRt, 
			double msmsSimilarityCutoff, 
			double entropyScoreMassError,
			MassErrorType entropyScoreMassErrorType, 
			double entropyScoreNoiseCutoff) {
		super();
		this.id = id;
		this.name = name;
		this.mzErrorValue = mzErrorValue;
		this.massErrorType = massErrorType;
		this.rtErrorValue = rtErrorValue;
		this.ignoreRt = ignoreRt;
		this.msmsSimilarityCutoff = msmsSimilarityCutoff;
		this.entropyScoreMassError = entropyScoreMassError;
		this.entropyScoreMassErrorType = entropyScoreMassErrorType;
		this.entropyScoreNoiseCutoff = entropyScoreNoiseCutoff;
		this.md5 = MSMSSearchUtils.calculateMSMSSearchParametersMd5(this);
	}

   @Override
    public boolean equals(Object obj) {

		if (obj == this)
			return true;

        if (obj == null)
            return false;

        if (!MSMSSearchParameterSet.class.isAssignableFrom(obj.getClass()))
            return false;

        final MSMSSearchParameterSet other = (MSMSSearchParameterSet) obj;

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

		Element msmsSearchParameterSetElement = 
				new Element(MSMSSearchParameterSetFields.MSMSSearchParameterSet.name());
		msmsSearchParameterSetElement.setAttribute(MSMSSearchParameterSetFields.Id.name(), id);	

		String nameString = name;
		if(nameString == null)
			nameString = "";
		
		msmsSearchParameterSetElement.setAttribute(
				MSMSSearchParameterSetFields.Name.name(), nameString);		
		msmsSearchParameterSetElement.setAttribute(
				MSMSSearchParameterSetFields.MZErrorValue.name(), Double.toString(mzErrorValue));	
		msmsSearchParameterSetElement.setAttribute(
				MSMSSearchParameterSetFields.MassErrorType.name(), massErrorType.name());
		msmsSearchParameterSetElement.setAttribute(
				MSMSSearchParameterSetFields.RTErrorValue.name(), Double.toString(rtErrorValue));	
		msmsSearchParameterSetElement.setAttribute(
				MSMSSearchParameterSetFields.IgnoreRetention.name(), Boolean.toString(ignoreRt));	
		msmsSearchParameterSetElement.setAttribute(
				MSMSSearchParameterSetFields.MSMSSimilarityCutoff.name(), Double.toString(msmsSimilarityCutoff));		
		msmsSearchParameterSetElement.setAttribute(
				MSMSSearchParameterSetFields.EntropyScoreMassError.name(), Double.toString(entropyScoreMassError));	
		msmsSearchParameterSetElement.setAttribute(
				MSMSSearchParameterSetFields.EntropyScoreMassErrorType.name(), entropyScoreMassErrorType.name());
		msmsSearchParameterSetElement.setAttribute(
				MSMSSearchParameterSetFields.EntropyScoreNoiseCutoff.name(), Double.toString(entropyScoreNoiseCutoff));	
		
		return msmsSearchParameterSetElement;
	}
	
	public MSMSSearchParameterSet(Element xmlElement) {
				
		id = xmlElement.getAttributeValue(MSMSSearchParameterSetFields.Id.name());
		name = xmlElement.getAttributeValue(MSMSSearchParameterSetFields.Name.name());
		if(name == null || name.isEmpty())
			name = null;
		
		mzErrorValue = Double.parseDouble(
				xmlElement.getAttributeValue(MSMSSearchParameterSetFields.MZErrorValue.name()));
		massErrorType = MassErrorType.getTypeByName(
				xmlElement.getAttributeValue(MSMSSearchParameterSetFields.MassErrorType.name()));
		rtErrorValue = Double.parseDouble(
				xmlElement.getAttributeValue(MSMSSearchParameterSetFields.RTErrorValue.name()));
		ignoreRt = Boolean.getBoolean(
				xmlElement.getAttributeValue(MSMSSearchParameterSetFields.IgnoreRetention.name()));
		msmsSimilarityCutoff = Double.parseDouble(
				xmlElement.getAttributeValue(MSMSSearchParameterSetFields.MSMSSimilarityCutoff.name()));
		
		entropyScoreMassError = Double.parseDouble(
				xmlElement.getAttributeValue(MSMSSearchParameterSetFields.EntropyScoreMassError.name()));
		entropyScoreMassErrorType =MassErrorType.getTypeByName(
				xmlElement.getAttributeValue(MSMSSearchParameterSetFields.EntropyScoreMassErrorType.name()));
		entropyScoreNoiseCutoff = Double.parseDouble(
				xmlElement.getAttributeValue(MSMSSearchParameterSetFields.EntropyScoreNoiseCutoff.name()));

		md5 = MSMSSearchUtils.calculateMSMSSearchParametersMd5(this);
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

	public boolean isIgnoreRt() {
		return ignoreRt;
	}

	public void setIgnoreRt(boolean ignoreRt) {
		this.ignoreRt = ignoreRt;
	}

	public double getMsmsSimilarityCutoff() {
		return msmsSimilarityCutoff;
	}

	public void setMsmsSimilarityCutoff(double msmsSimilarityCutoff) {
		this.msmsSimilarityCutoff = msmsSimilarityCutoff;
	}

	public double getEntropyScoreMassError() {
		return entropyScoreMassError;
	}

	public void setEntropyScoreMassError(double entropyScoreMassError) {
		this.entropyScoreMassError = entropyScoreMassError;
	}

	public MassErrorType getEntropyScoreMassErrorType() {
		return entropyScoreMassErrorType;
	}


	public void setEntropyScoreMassErrorType(MassErrorType entropyScoreMassErrorType) {
		this.entropyScoreMassErrorType = entropyScoreMassErrorType;
	}

	public double getEntropyScoreNoiseCutoff() {
		return entropyScoreNoiseCutoff;
	}

	public void setEntropyScoreNoiseCutoff(double entropyScoreNoiseCutoff) {
		this.entropyScoreNoiseCutoff = entropyScoreNoiseCutoff;
	}

	public String getMd5() {
		return md5;
	}

	public void setMd5(String md5) {
		this.md5 = md5;
	}
}


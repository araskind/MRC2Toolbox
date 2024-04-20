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

import org.jdom2.Element;

import edu.umich.med.mrc2.datoolbox.data.enums.MSMSSearchDirection;
import edu.umich.med.mrc2.datoolbox.data.enums.MassErrorType;
import edu.umich.med.mrc2.datoolbox.data.msclust.MSMSClusteringParameterSet;
import edu.umich.med.mrc2.datoolbox.project.store.MSMSClusteringParameterSetFields;
import edu.umich.med.mrc2.datoolbox.utils.MSMSClusteringUtils;

public class MSMSSearchParameterSet extends MSMSClusteringParameterSet{

	private boolean ignoreParentIon;
	private boolean ignoreRt;	
	private double entropyScoreMassError;
	private MassErrorType entropyScoreMassErrorType;
	private double entropyScoreNoiseCutoff;	
	private MSMSSearchDirection msmsSearchDirection;
		
	public MSMSSearchParameterSet() {
		super();
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
			double entropyScoreNoiseCutoff,
			boolean ignoreParentIon,
			MSMSSearchDirection msmsSearchDirection) {
		super(name, mzErrorValue, massErrorType, rtErrorValue, msmsSimilarityCutoff);
		this.id = id;
		this.ignoreRt = ignoreRt;
		this.entropyScoreMassError = entropyScoreMassError;
		this.entropyScoreMassErrorType = entropyScoreMassErrorType;
		this.entropyScoreNoiseCutoff = entropyScoreNoiseCutoff;
		this.ignoreParentIon = ignoreParentIon;
		this.msmsSearchDirection = msmsSearchDirection;
		this.md5 = MSMSClusteringUtils.calculateMSMSSearchParametersMd5(this);
	}

	public boolean ignoreRt() {
		return ignoreRt;
	}

	public void setIgnoreRt(boolean ignoreRt) {
		this.ignoreRt = ignoreRt;
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

	public boolean ignoreParentIon() {
		return ignoreParentIon;
	}

	public void setIgnoreParentIon(boolean ignoreParentIon) {
		this.ignoreParentIon = ignoreParentIon;
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

		Element msmsSearchParameterSetElement = super.getXmlElement();
		
		msmsSearchParameterSetElement.setAttribute(
				MSMSClusteringParameterSetFields.IgnoreParent.name(), 
				Boolean.toString(ignoreParentIon));	
		msmsSearchParameterSetElement.setAttribute(
				MSMSClusteringParameterSetFields.IgnoreRetention.name(), 
				Boolean.toString(ignoreRt));	
		msmsSearchParameterSetElement.setAttribute(
				MSMSClusteringParameterSetFields.EntropyScoreMassError.name(), 
				Double.toString(entropyScoreMassError));	
		msmsSearchParameterSetElement.setAttribute(
				MSMSClusteringParameterSetFields.EntropyScoreMassErrorType.name(), 
				entropyScoreMassErrorType.name());
		msmsSearchParameterSetElement.setAttribute(
				MSMSClusteringParameterSetFields.EntropyScoreNoiseCutoff.name(), 
				Double.toString(entropyScoreNoiseCutoff));
		
		if(msmsSearchDirection == null)
			msmsSearchDirection = MSMSSearchDirection.DIRECT;
		msmsSearchParameterSetElement.setAttribute(
				MSMSClusteringParameterSetFields.MSMSSearchDirection.name(), 
				msmsSearchDirection.name());
		
		return msmsSearchParameterSetElement;
	}
	
	public MSMSSearchParameterSet(Element xmlElement) {
		
		super(xmlElement);
			
		ignoreParentIon = Boolean.getBoolean(
				xmlElement.getAttributeValue(
						MSMSClusteringParameterSetFields.IgnoreParent.name()));
		ignoreRt = Boolean.getBoolean(
				xmlElement.getAttributeValue(
						MSMSClusteringParameterSetFields.IgnoreRetention.name()));
		entropyScoreMassError = Double.parseDouble(
				xmlElement.getAttributeValue(
						MSMSClusteringParameterSetFields.EntropyScoreMassError.name()));
		entropyScoreMassErrorType =MassErrorType.getTypeByName(
				xmlElement.getAttributeValue(
						MSMSClusteringParameterSetFields.EntropyScoreMassErrorType.name()));
		entropyScoreNoiseCutoff = Double.parseDouble(
				xmlElement.getAttributeValue(
						MSMSClusteringParameterSetFields.EntropyScoreNoiseCutoff.name()));
		
		msmsSearchDirection = MSMSSearchDirection.getOptionByName(
				xmlElement.getAttributeValue(
						MSMSClusteringParameterSetFields.MSMSSearchDirection.name()));

		md5 = MSMSClusteringUtils.calculateMSMSSearchParametersMd5(this);
	}

	public MSMSSearchDirection getMsmsSearchDirection() {
		return msmsSearchDirection;
	}

	public void setMsmsSearchDirection(MSMSSearchDirection msmsSearchDirection) {
		this.msmsSearchDirection = msmsSearchDirection;
	}
}


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

package edu.umich.med.mrc2.datoolbox.data;

import java.io.Serializable;
import java.util.UUID;

import org.apache.commons.math3.util.Precision;
import org.jdom2.Element;

import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.project.store.CommonFields;
import edu.umich.med.mrc2.datoolbox.project.store.MinimalMSOneFeatureFields;
import edu.umich.med.mrc2.datoolbox.project.store.ObjectNames;

public class MinimalMSOneFeature implements Serializable, Comparable<MinimalMSOneFeature> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3016172144844330033L;
	
	private String id;
	private String name;
	private double mz;
	private double rt;
	private double rank;
	private double foldChange;
	private double pValue;
	private String smiles;
	private String inchiKey;
	
	public MinimalMSOneFeature(double mz, double rt) {
		this(null, null, mz, rt, 0.0d, null, null);
	}

	public MinimalMSOneFeature(String name, double mz, double rt) {
		this(null, name, mz, rt, 0.0d, null, null);
	}

	public MinimalMSOneFeature(String name, double mz, double rt, double rank) {
		this(null, null, mz, rt, rank, null, null);
	}

	public MinimalMSOneFeature(String id, String name, double mz, double rt, double rank) {
		this(id, name, mz, rt, rank, null, null);
	}
	
	public MinimalMSOneFeature(BinnerAnnotation annotation) {
		this(null, 
			annotation.getNameWithAnnotation(), 
			annotation.getBinnerMz(), 
			annotation.getBinnerRt(), 
			0, null, null);
	}

	public MinimalMSOneFeature(
			String dbid,
			String name, 
			double mz, 
			double rt, 
			double rank, 
			String smiles, 
			String inchiKey) {
		super();
		if(dbid == null)
			this.id = DataPrefix.LOOKUP_FEATURE.getName() + 
			UUID.randomUUID().toString().substring(0, 12);
		else
			this.id = dbid;
		
		this.name = name;
		this.mz = mz;
		this.rt = rt;
		this.rank = rank;
		this.smiles = smiles;
		this.inchiKey = inchiKey;
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return name;
	}
	
	public double getMz() {
		return mz;
	}

	public double getRt() {
		return rt;
	}

	@Override
	public boolean equals(Object obj) {

		if (obj == null)
			return false;

		if (!MinimalMSOneFeature.class.isAssignableFrom(obj.getClass()))
			return false;

		final MinimalMSOneFeature other = (MinimalMSOneFeature) obj;

		if ((this.name == null) ? (other.getName() != null) : !this.name.equals(other.getName()))
			return false;

		if (obj == this)
			return true;

		return true;
	}

	@Override
	public int hashCode() {

		int hash = 3;
		hash = 53 * hash + (this.name != null ? this.name.hashCode() : 0);
		return hash;
	}

	@Override
	public int compareTo(MinimalMSOneFeature o) {
		 
		int result = Precision.compareTo(mz, o.getMz(), Precision.EPSILON);
		if(result == 0)
			result = Precision.compareTo(rt, o.getRt(), Precision.EPSILON);
		
		return result;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getRank() {
		return rank;
	}

	public void setRank(double rank) {
		this.rank = rank;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Element getXmlElement() {

		Element featureElement = 
				new Element(ObjectNames.MinimalMSOneFeature.name());
		featureElement.setAttribute(CommonFields.Id.name(), id);	
		featureElement.setAttribute(CommonFields.Name.name(), name);
		featureElement.setAttribute(CommonFields.MZ.name(), Double.toString(mz));
		featureElement.setAttribute(CommonFields.RT.name(), Double.toString(rt));
		featureElement.setAttribute(
				MinimalMSOneFeatureFields.Rank.name(), Double.toString(rank));
		featureElement.setAttribute(
				MinimalMSOneFeatureFields.FoldChange.name(), Double.toString(foldChange));
		featureElement.setAttribute(
				MinimalMSOneFeatureFields.PValue.name(), Double.toString(pValue));
		
		return featureElement;
	}

	public MinimalMSOneFeature(Element featureElement) {

		id = featureElement.getAttributeValue(CommonFields.Id.name());
		name = featureElement.getAttributeValue(CommonFields.Name.name());
		mz = Double.parseDouble(
				featureElement.getAttributeValue(CommonFields.MZ.name()));
		rt = Double.parseDouble(
				featureElement.getAttributeValue(CommonFields.RT.name()));
		
		String rankString = featureElement.getAttributeValue(MinimalMSOneFeatureFields.Rank.name());
		if(rankString != null && !rankString.isEmpty())
			rank = Double.parseDouble(rankString);
		
		String foldChangeString = featureElement.getAttributeValue(MinimalMSOneFeatureFields.FoldChange.name());
		if(foldChangeString != null && !foldChangeString.isEmpty())
			foldChange = Double.parseDouble(foldChangeString);
		
		String pValueString = featureElement.getAttributeValue(MinimalMSOneFeatureFields.PValue.name());
		if(pValueString != null && !pValueString.isEmpty())
			pValue = Double.parseDouble(pValueString);
	}

	public String getSmiles() {
		return smiles;
	}

	public void setSmiles(String smiles) {
		this.smiles = smiles;
	}

	public String getInchiKey() {
		return inchiKey;
	}

	public void setInchiKey(String inchiKey) {
		this.inchiKey = inchiKey;
	}

	public double getFoldChange() {
		return foldChange;
	}

	public void setFoldChange(double foldChange) {
		this.foldChange = foldChange;
	}

	public double getpValue() {
		return pValue;
	}

	public void setpValue(double pValue) {
		this.pValue = pValue;
	}
}

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
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import edu.umich.med.mrc2.datoolbox.data.enums.CompoundIdentificationConfidence;

public class PostProcessorAnnotation implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6303466686293703271L;
	private CompoundIdentity cid;
	private BinnerAnnotation ba;
	private double rtMatchEror;
	private double mzMatchEror;
	private Map<String,String>synonyms;
	private String versionInfo;
	private String note;
	private CompoundClassifier classifier;
	private CompoundIdentificationConfidence idConfidence;

	public PostProcessorAnnotation(
			CompoundIdentity cid, BinnerAnnotation ba) {
		super();
		this.cid = cid;
		this.ba = ba;
		synonyms = new TreeMap<String,String>();
	}

	public void addSynonym(String synonym, String type) {

		if(synonym == null)
			return;

		if(synonym.trim().isEmpty())
			return;

		synonyms.put(synonym, Objects.toString(type, ""));
	}

	/**
	 * @return the cid
	 */
	public CompoundIdentity getCid() {
		return cid;
	}

	/**
	 * @return the ba
	 */
	public BinnerAnnotation getBa() {
		return ba;
	}

	/**
	 * @return the rtMatchEror
	 */
	public double getRtMatchEror() {
		return rtMatchEror;
	}

	/**
	 * @return the mzMatchEror
	 */
	public double getMzMatchEror() {
		return mzMatchEror;
	}

	/**
	 * @param rtMatchEror the rtMatchEror to set
	 */
	public void setRtMatchEror(double rtMatchEror) {
		this.rtMatchEror = rtMatchEror;
	}

	/**
	 * @param mzMatchEror the mzMatchEror to set
	 */
	public void setMzMatchEror(double mzMatchEror) {
		this.mzMatchEror = mzMatchEror;
	}

	/**
	 * @return the synonyms
	 */
	public Map<String,String> getSynonyms() {
		return synonyms;
	}

	/**
	 * @return the versionInfo
	 */
	public String getVersionInfo() {
		return versionInfo;
	}

	/**
	 * @param versionInfo the versionInfo to set
	 */
	public void setVersionInfo(String versionInfo) {
		this.versionInfo = versionInfo;
	}

	/**
	 * @return the note
	 */
	public String getNote() {
		return note;
	}

	/**
	 * @param note the note to set
	 */
	public void setNote(String note) {
		this.note = note;
	}

	/**
	 * @return the classifier
	 */
	public CompoundClassifier getClassifier() {
		return classifier;
	}

	/**
	 * @param classifier the classifier to set
	 */
	public void setClassifier(CompoundClassifier classifier) {
		this.classifier = classifier;
	}

	/**
	 * @return the idConfidence
	 */
	public CompoundIdentificationConfidence getIdConfidence() {
		return idConfidence;
	}

	/**
	 * @param idConfidence the idConfidence to set
	 */
	public void setIdConfidence(CompoundIdentificationConfidence idConfidence) {
		this.idConfidence = idConfidence;
	}


}

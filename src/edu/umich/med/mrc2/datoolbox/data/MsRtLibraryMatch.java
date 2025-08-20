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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.jdom2.Element;

import edu.umich.med.mrc2.datoolbox.main.AdductManager;
import edu.umich.med.mrc2.datoolbox.project.store.MsRtLibraryMatchFields;
import edu.umich.med.mrc2.datoolbox.project.store.ObjectNames;

public class MsRtLibraryMatch implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4358335306874004672L;
	private String libraryId;	//	TODO make sure this is always present
	private String libraryTargetId;
	private String libraryTargetName;
	private double score;
	private Set<AdductMatch>adductScoreMap;
	private double expectedRetention = 0.0d;
	private double observedRetention = 0.0d;
	private MassSpectrum librarySpectrum;

	public MsRtLibraryMatch(
			String libraryId, 			
			String libraryTargetId, 
			String libraryTargetName, 
			double score,
			double expectedRetention, 
			MassSpectrum librarySpectrum,
			Set<AdductMatch>adductScoreMap) {
		super();
		this.libraryId = libraryId;
		this.libraryTargetId = libraryTargetId;
		this.libraryTargetName = libraryTargetName;
		this.score = score;
		this.expectedRetention = expectedRetention;
		this.librarySpectrum = librarySpectrum;
		this.adductScoreMap = new HashSet<AdductMatch>();
		if(adductScoreMap != null && !adductScoreMap.isEmpty())
			this.adductScoreMap.addAll(adductScoreMap);
	}

	public MsRtLibraryMatch(String libraryTargetId) {
		this(null, libraryTargetId, null, 0.0d, 0.0d, null, null);		
	}

	public MsRtLibraryMatch(
			String libraryTargetId,
			Set<AdductMatch> adductScoreMap,
			double expectedRetention,
			MassSpectrum librarySpectrum) {
		this(null, libraryTargetId, null, 0.0d, expectedRetention, librarySpectrum, adductScoreMap);
	}

	public String getLibraryTargetId() {
		return libraryTargetId;
	}

	public double getScore() {		
		return getTopAdductScore();
	}

	public Set<AdductMatch> getAdductScoreMap() {
		return adductScoreMap;
	}

	public double getExpectedRetention() {
		return expectedRetention;
	}

	public void setExpectedRetention(double expectedRetention) {
		this.expectedRetention = expectedRetention;
	}

	public double getTopAdductScore() {

		if(adductScoreMap.isEmpty())
			return 0.0;
		else
			return adductScoreMap.stream().
					mapToDouble(s -> s.getDotProductScore()).
					max().getAsDouble();
	}

	public AdductMatch getTopAdductMatch() {

		return adductScoreMap.stream().
			        sorted().findFirst().orElse(null);
	}

	@Override
	public boolean equals(Object msRtMatch) {

        if (msRtMatch == this)
            return true;

		if(msRtMatch == null)
			return false;

        if (!MsRtLibraryMatch.class.isAssignableFrom(msRtMatch.getClass()))
            return false;

        MsRtLibraryMatch cid = (MsRtLibraryMatch)msRtMatch;

        if ((this.libraryTargetId == null) ? (cid.getLibraryTargetId() != null) :
        	!this.libraryTargetId.equals(cid.getLibraryTargetId()))
        	return false;

        if(this.score != cid.getScore())
        	return false;

        return true;
	}

    @Override
    public int hashCode() {

        int hash = 3;
        hash = 53 * hash +
    		+ (this.libraryTargetId != null ? this.libraryTargetId.hashCode() : 0)
    		+ Double.toString(this.score).hashCode();
        return hash;
    }

	/**
	 * @return the librarySpectrum
	 */
	public MassSpectrum getLibrarySpectrum() {
		return librarySpectrum;
	}

	/**
	 * @param librarySpectrum the librarySpectrum to set
	 */
	public void setLibrarySpectrum(MassSpectrum librarySpectrum) {
		this.librarySpectrum = librarySpectrum;
	}

	/**
	 * @return the libraryId
	 */
	public String getLibraryId() {
		return libraryId;
	}

	/**
	 * @param libraryId the libraryId to set
	 */
	public void setLibraryId(String libraryId) {
		this.libraryId = libraryId;
	}
	
	public String getLibraryTargetName() {
		return libraryTargetName;
	}

	public void setLibraryTargetName(String libraryTargetName) {
		this.libraryTargetName = libraryTargetName;
	}
	
	
	public Element getXmlElement() {
		
		Element refMsRtElement = 
				new Element(ObjectNames.RefMsRt.name());
		
		if(libraryId != null)
			refMsRtElement.setAttribute(
					MsRtLibraryMatchFields.LibId.name(), libraryId);
		
		if(libraryTargetId != null)
			refMsRtElement.setAttribute(
					MsRtLibraryMatchFields.TGID.name(), libraryTargetId);
		
		if(libraryTargetName != null)
			refMsRtElement.setAttribute(
					MsRtLibraryMatchFields.TGName.name(), libraryTargetName);
		
		refMsRtElement.setAttribute(
				MsRtLibraryMatchFields.Score.name(), Double.toString(score));		
		refMsRtElement.setAttribute(
				MsRtLibraryMatchFields.RTExpected.name(), Double.toString(expectedRetention));
		refMsRtElement.setAttribute(
				MsRtLibraryMatchFields.RTObserved.name(), Double.toString(observedRetention));
		
		if(adductScoreMap != null && !adductScoreMap.isEmpty()) {
			
			List<String> amList = adductScoreMap.stream().
					map(s -> s.getLibraryMatch().getId() + "|" 
							+ s.getUnknownMatch().getId() + "|" 
							+ Double.toString(s.getDotProductScore())  + "|" 
							+ Double.toString(s.getEntropyScore())).
					collect(Collectors.toList());
			refMsRtElement.setAttribute(
					MsRtLibraryMatchFields.AdScores.name(), 
					StringUtils.join(amList, "@"));
		}	
		if(librarySpectrum != null) 
			refMsRtElement.addContent(librarySpectrum.getXmlElement());
		
		return refMsRtElement;
	}
	
	public MsRtLibraryMatch(Element msRtMatchElement) {
		
		adductScoreMap = new HashSet<AdductMatch>();
		libraryId = msRtMatchElement.getAttributeValue(
				MsRtLibraryMatchFields.LibId.name());
		libraryTargetId = msRtMatchElement.getAttributeValue(
				MsRtLibraryMatchFields.TGID.name());
		libraryTargetName = msRtMatchElement.getAttributeValue(
				MsRtLibraryMatchFields.TGName.name());
		 
		String scoreString = 
				msRtMatchElement.getAttributeValue(MsRtLibraryMatchFields.Score.name());
		if(scoreString != null)
			score = Double.parseDouble(scoreString);
		
		String expectedRTString = 
				msRtMatchElement.getAttributeValue(MsRtLibraryMatchFields.RTExpected.name());
		if(expectedRTString != null)
			expectedRetention = Double.parseDouble(expectedRTString);
		
		String observedRTString = 
				msRtMatchElement.getAttributeValue(MsRtLibraryMatchFields.RTObserved.name());
		if(observedRTString != null)
			observedRetention = Double.parseDouble(observedRTString);

		String adductScoreMapString = 
				msRtMatchElement.getAttributeValue(MsRtLibraryMatchFields.AdScores.name());
		if(adductScoreMapString != null 
				&& !adductScoreMapString.isEmpty()) {
			String[]adductList = adductScoreMapString.split("@");
			for(String adductListElement : adductList) {
				
				String[]parts = adductListElement.split("\\|");
				if(parts.length == 4) {
					
					Adduct libMatch = AdductManager.getAdductById(parts[0]);
					Adduct unkMatch = AdductManager.getAdductById(parts[1]);
					double amScore = Double.parseDouble(parts[2]);
					double entropyScore = Double.parseDouble(parts[3]);
					if(libMatch != null && unkMatch != null) {
						
						AdductMatch am = new AdductMatch(libMatch, unkMatch, amScore);
						am.setEntropyScore(entropyScore);
						adductScoreMap.add(am);
					}
				}
			}
		}
		Element spectrumElement = msRtMatchElement.getChild(
				ObjectNames.Spectrum.name());
		if(spectrumElement != null)
			librarySpectrum = new MassSpectrum(spectrumElement);
	}

	public double getObservedRetention() {
		return observedRetention;
	}

	public void setObservedRetention(double observedRetention) {
		this.observedRetention = observedRetention;
	}
	
	public Double getRtError() {
		
		if(observedRetention > 0.0d && expectedRetention > 0.0)
			return observedRetention - expectedRetention;
		else
			return null;
	}
}




















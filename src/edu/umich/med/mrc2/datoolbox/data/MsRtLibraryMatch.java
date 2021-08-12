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
import java.util.Set;

public class MsRtLibraryMatch implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4358335306874004672L;
	private String libraryId;	//	TODO make sure this is always present
	private String libraryTargetId;
	private double score;
	private Set<AdductMatch>adductScoreMap;
	private double expectedRetention = 0.0d;
	private MassSpectrum librarySpectrum;

	public MsRtLibraryMatch(String libraryTargetId) {
		super();
		this.libraryTargetId = libraryTargetId;
		adductScoreMap = new HashSet<AdductMatch>();
	}

	public MsRtLibraryMatch(
			String libraryTargetId,
			Set<AdductMatch> adductScoreMap,
			double expectedRetention,
			MassSpectrum librarySpectrum) {
		super();
		this.libraryTargetId = libraryTargetId;
		this.adductScoreMap = adductScoreMap;
		this.expectedRetention = expectedRetention;
		this.librarySpectrum = librarySpectrum;
	}

	/**
	 * @return the libraryTargetId
	 */
	public String getLibraryTargetId() {
		return libraryTargetId;
	}

	/**
	 * @return the score
	 */
	public double getScore() {
		return score;
	}

	/**
	 * @return the adductScoreMap
	 */
	public Set<AdductMatch> getAdductScoreMap() {
		return adductScoreMap;
	}

	/**
	 * @return the expectedRetention
	 */
	public double getExpectedRetention() {
		return expectedRetention;
	}

	/**
	 * @param score the score to set
	 */
	public void setScore(double score) {
		this.score = score;
	}

	/**
	 * @param expectedRetention the expectedRetention to set
	 */
	public void setExpectedRetention(double expectedRetention) {
		this.expectedRetention = expectedRetention;
	}

	public double getTopAdductScore() {

		double topAdductScore = 0.0;

		if(!adductScoreMap.isEmpty())
			return adductScoreMap.stream().map(s -> s.getScore()).reduce(Double::max).get();

		return topAdductScore;
	}

	public AdductMatch getTopAdductMatch() {

		if(!adductScoreMap.isEmpty())
			return adductScoreMap.stream().
			        sorted().
			        findFirst().get();

		return null;
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
}

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

import edu.umich.med.mrc2.datoolbox.data.enums.MSMSMatchType;

public class PepSearchOutputObject implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4905927826398334951L;
	private String msmsFeatureId;
	private String libraryName;
	private String databaseNumber;
	private String nistRegId;
	private String peptide;
	private String originalLibid;
	private String mrc2libid;
	private int matchRank;
	private double deltaMz;
	private double score;
	private double probablility;
	private double dotProduct;
	private double reverseDotProduct;
	private double hybridDotProduct;
	private double hybridScore;
	private double hybridDeltaMz;	
	private MSMSMatchType matchType;
	private boolean decoy;
	private Double deltaScoreWithNextBestMatch;
	private boolean isNextBestMatchDecoy;
	private String libInchiKey;
	private String unknownInchiKey;
	
	private boolean isTrueHit;
	private double pValue;
	private double pValueBaseAll;
	private double qValue;
	private double fdr;

	public PepSearchOutputObject(String msmsFeatureId) {
		super();
		this.msmsFeatureId = msmsFeatureId;
	}

	/**
	 * @return the msmsParentFeatureId
	 */
	public String getMsmsFeatureId() {
		return msmsFeatureId;
	}

	/**
	 * @return the libraryName
	 */
	public String getLibraryName() {
		return libraryName;
	}

	/**
	 * @return the libraryMatchId
	 */
	public String getDatabaseNumber() {
		return databaseNumber;
	}

	/**
	 * @return the matchRank
	 */
	public int getMatchRank() {
		return matchRank;
	}

	/**
	 * @return the deltaMz
	 */
	public double getDeltaMz() {
		return deltaMz;
	}

	/**
	 * @return the score
	 */
	public double getScore() {
		return score;
	}

	/**
	 * @return the dotProduct
	 */
	public double getDotProduct() {
		return dotProduct;
	}

	/**
	 * @return the probablility
	 */
	public double getProbablility() {
		return probablility;
	}

	/**
	 * @param libraryName the libraryName to set
	 */
	public void setLibraryName(String libraryName) {
		this.libraryName = libraryName;
	}

	/**
	 * @param libraryMatchId the libraryMatchId to set
	 */
	public void setDatabaseNumber(String databaseNumber) {
		this.databaseNumber = databaseNumber;
	}

	/**
	 * @param matchRank the matchRank to set
	 */
	public void setMatchRank(int matchRank) {
		this.matchRank = matchRank;
	}

	/**
	 * @param deltaMz the deltaMz to set
	 */
	public void setDeltaMz(double deltaMz) {
		this.deltaMz = deltaMz;
	}

	/**
	 * @param score the score to set
	 */
	public void setScore(double score) {
		this.score = score;
	}

	/**
	 * @param dotProduct the dotProduct to set
	 */
	public void setDotProduct(double dotProduct) {
		this.dotProduct = dotProduct;
	}

	/**
	 * @param probablility the probablility to set
	 */
	public void setProbablility(double probablility) {
		this.probablility = probablility;
	}

	/**
	 * @return the nistRegId
	 */
	public String getNistRegId() {
		return nistRegId;
	}

	/**
	 * @return the peptide
	 */
	public String getPeptide() {
		return peptide;
	}

	/**
	 * @param nistRegId the nistRegId to set
	 */
	public void setNistRegId(String nistRegId) {
		this.nistRegId = nistRegId;
	}

	/**
	 * @param peptide the peptide to set
	 */
	public void setPeptide(String peptide) {
		this.peptide = peptide;
	}

	public double getReverseDotProduct() {
		return reverseDotProduct;
	}

	public void setReverseDotProduct(double reverseDotProduct) {
		this.reverseDotProduct = reverseDotProduct;
	}

	public double getHybridDotProduct() {
		return hybridDotProduct;
	}

	public void setHybridDotProduct(double hybridDotProduct) {
		this.hybridDotProduct = hybridDotProduct;
	}

	public double getHybridScore() {
		return hybridScore;
	}

	public void setHybridScore(double hybridScore) {
		this.hybridScore = hybridScore;
	}

	public double getHybridDeltaMz() {
		return hybridDeltaMz;
	}

	public void setHybridDeltaMz(double hybridDeltaMz) {
		this.hybridDeltaMz = hybridDeltaMz;
	}

	public String getMrc2libid() {
		return mrc2libid;
	}

	public void setMrc2libid(String mrc2libid) {
		this.mrc2libid = mrc2libid;
	}

	public String getOriginalLibid() {
		return originalLibid;
	}

	public void setOriginalLibid(String originalLibid) {
		this.originalLibid = originalLibid;
	}
	
    @Override
    public boolean equals(Object obj) {

		if (obj == this)
			return true;

        if (obj == null)
            return false;

        if (!PepSearchOutputObject.class.isAssignableFrom(obj.getClass()))
            return false;

        final PepSearchOutputObject other = (PepSearchOutputObject) obj;
            
        if ((this.msmsFeatureId == null) ? (other.getMsmsFeatureId() != null) : !this.msmsFeatureId.equals(other.getMsmsFeatureId()))
            return false;
        
        if ((this.mrc2libid == null) ? (other.getMrc2libid() != null) : !this.mrc2libid.equals(other.getMrc2libid()))
            return false;

        return true;
    }
    
    @Override
    public int hashCode() {

        int hash = 3;
        hash = 53 * hash 
        		+ (this.msmsFeatureId != null ? this.msmsFeatureId.hashCode() : 0)
        		+ (this.mrc2libid != null ? this.mrc2libid.hashCode() : 0);
        return hash;
    }

	public boolean isDecoy() {
		return decoy;
	}

	public void setDecoy(boolean decoy) {
		this.decoy = decoy;
	}

	public MSMSMatchType getMatchType() {
		return matchType;
	}

	public void setMatchType(MSMSMatchType matchType) {
		this.matchType = matchType;
	}

	public Double getDeltaScoreWithNextBestMatch() {
		return deltaScoreWithNextBestMatch;
	}

	public void setDeltaScoreWithNextBestMatch(Double deltaScoreWithNextBestMatch) {
		this.deltaScoreWithNextBestMatch = deltaScoreWithNextBestMatch;
	}

	public boolean isNextBestMatchDecoy() {
		return isNextBestMatchDecoy;
	}

	public void setNextBestMatchDecoy(boolean isNextBestMatchDecoy) {
		this.isNextBestMatchDecoy = isNextBestMatchDecoy;
	}

	public String getUnknownInchiKey() {
		return unknownInchiKey;
	}

	public void setUnknownInchiKey(String unknownInchiKey) {
		this.unknownInchiKey = unknownInchiKey;
	}

	public boolean isTrueHit() {
		return isTrueHit;
	}

	public void setTrueHit(boolean isTrueHit) {
		this.isTrueHit = isTrueHit;
	}

	public double getpValue() {
		return pValue;
	}

	public void setpValue(double pValue) {
		this.pValue = pValue;
	}

	public double getqValue() {
		return qValue;
	}

	public void setqValue(double qValue) {
		this.qValue = qValue;
	}

	public String getLibInchiKey() {
		return libInchiKey;
	}

	public void setLibInchiKey(String libInchiKey) {
		this.libInchiKey = libInchiKey;
	}

	public double getFdr() {
		return fdr;
	}

	public void setFdr(double fdr) {
		this.fdr = fdr;
	}

	public double getpValueBaseAll() {
		return pValueBaseAll;
	}

	public void setpValueBaseAll(double pValueBaseAll) {
		this.pValueBaseAll = pValueBaseAll;
	}
}
















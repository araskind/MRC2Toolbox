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

import org.jdom2.Element;

import edu.umich.med.mrc2.datoolbox.data.enums.MSMSMatchType;
import edu.umich.med.mrc2.datoolbox.data.enums.MSMSScoringParameter;
import edu.umich.med.mrc2.datoolbox.database.idt.OfflineExperimentLoadCache;
import edu.umich.med.mrc2.datoolbox.project.store.ReferenceMsMsLibraryMatchFields;

public class ReferenceMsMsLibraryMatch implements Serializable {


	/**
	 * 
	 */
	private static final long serialVersionUID = 1980840150518719361L;
	private MsMsLibraryFeature matchedLibraryFeature;
	private double score;
	private double entropyBasedScore;
	private double forwardScore;
	private double reverseScore;
	private double probability;
	private double dotProduct;
	private double reverseDotProduct;
	private double hybridDotProduct;
	private double hybridScore;
	private MSMSMatchType matchType;
	private double hybridDeltaMz;
	private String searchParameterSetId;
	private boolean decoyMatch;	
	private double percolatorScore; 
	private double qValue;	
	private double posteriorErrorProbability;

	public ReferenceMsMsLibraryMatch(
			MsMsLibraryFeature matchedLibraryFeature,
			double score,
			double forwardScore,
			double reverseScore,
			double probability,
			double dotProduct) {
		super();
		this.matchedLibraryFeature = matchedLibraryFeature;
		this.score = score;
		this.forwardScore = forwardScore;
		this.reverseScore = reverseScore;
		this.probability = probability;
		this.dotProduct = dotProduct;
		matchType = MSMSMatchType.Regular;
		decoyMatch = false;
	}
	
	public ReferenceMsMsLibraryMatch(
			MsMsLibraryFeature matchedLibraryFeature,
			double score, 
			double forwardScore,
			double reverseScore, 
			double probability, 
			double dotProduct, 
			double reverseDotProduct,
			double hybridDotProduct, 
			double hybridScore, 
			double hybridDeltaMz, 
			MSMSMatchType matchType,
			boolean decoyMatch,
			String searchParameterSetId) {
		super();
		this.matchedLibraryFeature = matchedLibraryFeature;
		this.score = score;
		this.forwardScore = forwardScore;
		this.reverseScore = reverseScore;
		this.probability = probability;
		this.dotProduct = dotProduct;
		this.reverseDotProduct = reverseDotProduct;
		this.hybridDotProduct = hybridDotProduct;
		this.hybridScore = hybridScore;
		this.hybridDeltaMz = hybridDeltaMz;
		this.matchType = matchType;
		this.decoyMatch = decoyMatch;
		this.searchParameterSetId = searchParameterSetId;
	}

	public ReferenceMsMsLibraryMatch(String mrc2InternalDbId, double score) {
		super();
		this.matchedLibraryFeature = new MsMsLibraryFeature(mrc2InternalDbId);
		this.score = score;
	}
	
	public ReferenceMsMsLibraryMatch(
			MsMsLibraryFeature matchedLibraryFeature,
			PepSearchOutputObject poo,
			String searchParameterSetId) {
		super();
		this.matchedLibraryFeature = matchedLibraryFeature;
		this.searchParameterSetId = searchParameterSetId;		
		this.score = poo.getScore();
		this.probability = poo.getProbablility();
		this.dotProduct = poo.getDotProduct();
		this.reverseDotProduct = poo.getReverseDotProduct();
		this.hybridDotProduct = poo.getHybridDotProduct();
		this.hybridScore = poo.getHybridScore();
		this.hybridDeltaMz = poo.getHybridDeltaMz();
		this.matchType = poo.getMatchType();
		this.decoyMatch = poo.isDecoy();		
	}
	
	public double getScoreOfType(MSMSScoringParameter scoreType) {
		
		if(scoreType.equals(MSMSScoringParameter.NIST_SCORE))
			return score;
		
		if(scoreType.equals(MSMSScoringParameter.ENTROPY_SCORE)) 
			return entropyBasedScore;
		
		if(scoreType.equals(MSMSScoringParameter.DOT_PRODUCT)) 
			return dotProduct;
		
		if(scoreType.equals(MSMSScoringParameter.PROBABILITY)) 
			return probability;
		
		return 0.0d;
	}

	/**
	 * @return the score
	 */
	public double getScore() {
		return score;
	}

	/**
	 * @return the forwardScore
	 */
	public double getForwardScore() {
		return forwardScore;
	}

	/**
	 * @return the reverseScore
	 */
	public double getReverseScore() {
		return reverseScore;
	}

	/**
	 * @return the probability
	 */
	public double getProbability() {
		return probability;
	}

	/**
	 * @return the dotProduct
	 */
	public double getDotProduct() {
		return dotProduct;
	}

	/**
	 * @param score the score to set
	 */
	public void setScore(double score) {
		this.score = score;
	}

	/**
	 * @param forwardScore the forwardScore to set
	 */
	public void setForwardScore(double forwardScore) {
		this.forwardScore = forwardScore;
	}

	/**
	 * @param reverseScore the reverseScore to set
	 */
	public void setReverseScore(double reverseScore) {
		this.reverseScore = reverseScore;
	}

	/**
	 * @param probability the probability to set
	 */
	public void setProbability(double probability) {
		this.probability = probability;
	}

	/**
	 * @param dotProduct the dotProduct to set
	 */
	public void setDotProduct(double dotProduct) {
		this.dotProduct = dotProduct;
	}

	@Override
	public boolean equals(Object msmsMatch) {

        if (msmsMatch == this)
            return true;

		if(msmsMatch == null)
			return false;

        if (!ReferenceMsMsLibraryMatch.class.isAssignableFrom(msmsMatch.getClass()))
            return false;

        ReferenceMsMsLibraryMatch cid = (ReferenceMsMsLibraryMatch)msmsMatch;

        if ((this.matchedLibraryFeature == null) ? (cid.getMatchedLibraryFeature() != null) :
        	!this.matchedLibraryFeature.equals(cid.getMatchedLibraryFeature()))
        	return false;

        if(this.score != cid.getScore())
        	return false;

        return true;
	}

    @Override
    public int hashCode() {

        int hash = 3;
        hash = 53 * hash +
    		+ (this.matchedLibraryFeature != null ? this.matchedLibraryFeature.hashCode() : 0)
    		+ Double.toString(this.score).hashCode();
        return hash;
    }

	/**
	 * @return the matchedLibraryFeature
	 */
	public MsMsLibraryFeature getMatchedLibraryFeature() {
		return matchedLibraryFeature;
	}

	/**
	 * @param matchedLibraryFeature the matchedLibraryFeature to set
	 */
	public void setMatchedLibraryFeature(MsMsLibraryFeature matchedLibraryFeature) {
		this.matchedLibraryFeature = matchedLibraryFeature;
	}

	/**
	 * @return the searchParameterSetId
	 */
	public String getSearchParameterSetId() {
		return searchParameterSetId;
	}

	/**
	 * @param searchParameterSetId the searchParameterSetId to set
	 */
	public void setSearchParameterSetId(String searchParameterSetId) {
		this.searchParameterSetId = searchParameterSetId;
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

	public boolean isHybridMatch() {
		return matchType.equals(MSMSMatchType.Hybrid);
	}

	public boolean isInSourceMatch() {
		return matchType.equals(MSMSMatchType.InSource);
	}

	public boolean isIdentical(ReferenceMsMsLibraryMatch other) {
		
		if(!matchedLibraryFeature.equals(other.getMatchedLibraryFeature()))
			return false;
		
		if(!matchType.equals(other.getMatchType()))
			return false;
		
		if(score != other.getScore())
			return false;
				
		if(forwardScore != other.getForwardScore())
			return false;
		
		if(reverseScore != other.getReverseScore())
			return false;
				
		if(probability != other.getProbability())
			return false;
		
		if(dotProduct != other.getDotProduct())
			return false;
				
		if(reverseDotProduct != other.getReverseDotProduct())
			return false;
		
		if(hybridDotProduct != other.getHybridDotProduct())
			return false;
				
		if(hybridScore != other.getHybridScore())
			return false;
		
		if(hybridDeltaMz != other.getHybridDeltaMz())
			return false;		
		
		return true;
	}

	public MSMSMatchType getMatchType() {
		return matchType;
	}

	public void setMatchType(MSMSMatchType matchType) {
		this.matchType = matchType;
	}

	public boolean isDecoyMatch() {
		return decoyMatch;
	}

	public void setDecoyMatch(boolean decoyMatch) {
		this.decoyMatch = decoyMatch;
	}

	public double getPercolatorScore() {
		return percolatorScore;
	}

	public void setPercolatorScore(double percolatorScore) {
		this.percolatorScore = percolatorScore;
	}

	public double getqValue() {
		return qValue;
	}

	public void setqValue(double qValue) {
		this.qValue = qValue;
	}

	public double getPosteriorErrorProbability() {
		return posteriorErrorProbability;
	}

	public void setPosteriorErrorProbability(double posteriorErrorProbability) {
		this.posteriorErrorProbability = posteriorErrorProbability;
	}
	
	public Element getXmlElement() {

		Element refMsmsElement = 
				new Element(ReferenceMsMsLibraryMatchFields.RefMsms.name());
		
		if(matchedLibraryFeature != null)
			refMsmsElement.setAttribute(
					ReferenceMsMsLibraryMatchFields.DbId.name(), matchedLibraryFeature.getUniqueId());
		
		if(matchType != null)
			refMsmsElement.setAttribute(
					ReferenceMsMsLibraryMatchFields.MType.name(), matchType.name());
		
		if(searchParameterSetId != null)
			refMsmsElement.setAttribute(
					ReferenceMsMsLibraryMatchFields.ParSet.name(), searchParameterSetId);
		
		if(score > 0.0d)
			refMsmsElement.setAttribute(
					ReferenceMsMsLibraryMatchFields.Score.name(), Double.toString(score));
		
		if(forwardScore > 0.0d)
			refMsmsElement.setAttribute(
					ReferenceMsMsLibraryMatchFields.FwdScore.name(), Double.toString(forwardScore));
		
		if(reverseScore > 0.0d)
			refMsmsElement.setAttribute(
					ReferenceMsMsLibraryMatchFields.RevScore.name(), Double.toString(reverseScore));
		
		if(probability > 0.0d)
			refMsmsElement.setAttribute(
					ReferenceMsMsLibraryMatchFields.Prob.name(), Double.toString(probability));
		
		if(dotProduct > 0.0d)
			refMsmsElement.setAttribute(
					ReferenceMsMsLibraryMatchFields.DotProd.name(), Double.toString(dotProduct));
		
		if(reverseDotProduct > 0.0d)
			refMsmsElement.setAttribute(
					ReferenceMsMsLibraryMatchFields.RevDotProd.name(), Double.toString(reverseDotProduct));		
		
		if(hybridDotProduct > 0.0d)
			refMsmsElement.setAttribute(
					ReferenceMsMsLibraryMatchFields.HybDotProd.name(), Double.toString(hybridDotProduct));
				
		if(hybridScore > 0.0d)
			refMsmsElement.setAttribute(
					ReferenceMsMsLibraryMatchFields.HybScore.name(), Double.toString(hybridScore));
				
		if(hybridDeltaMz > 0.0d)
			refMsmsElement.setAttribute(
					ReferenceMsMsLibraryMatchFields.HybDMZ.name(), Double.toString(hybridDeltaMz));
		
		refMsmsElement.setAttribute(
				ReferenceMsMsLibraryMatchFields.Decoy.name(), Boolean.toString(decoyMatch));
		
		if(percolatorScore > 0.0d)
			refMsmsElement.setAttribute(
					ReferenceMsMsLibraryMatchFields.PercScore.name(), Double.toString(percolatorScore));
		
		if(qValue > 0.0d)
			refMsmsElement.setAttribute(
					ReferenceMsMsLibraryMatchFields.Qval.name(), Double.toString(qValue));
		
		if(posteriorErrorProbability > 0.0d)
			refMsmsElement.setAttribute(
					ReferenceMsMsLibraryMatchFields.PEP.name(), Double.toString(posteriorErrorProbability));
		
		return refMsmsElement;
	}
	
	public ReferenceMsMsLibraryMatch(Element msmsMatch) {

		String msmsId = 
				msmsMatch.getAttributeValue(ReferenceMsMsLibraryMatchFields.DbId.name());
		if(msmsId != null)
			matchedLibraryFeature = 
			OfflineExperimentLoadCache.getMsMsLibraryFeatureById(msmsId);

		String matchTypeId = 
				msmsMatch.getAttributeValue(ReferenceMsMsLibraryMatchFields.MType.name());
		if(matchTypeId != null)
			matchType = MSMSMatchType.getOptionByName(matchTypeId);
		
		searchParameterSetId = 
				msmsMatch.getAttributeValue(ReferenceMsMsLibraryMatchFields.ParSet.name());

		decoyMatch = 
			Boolean.parseBoolean(
					msmsMatch.getAttributeValue(ReferenceMsMsLibraryMatchFields.Decoy.name()));
		
		String scoreString = 
				msmsMatch.getAttributeValue(ReferenceMsMsLibraryMatchFields.Score.name());
		if(scoreString != null)
			score = Double.parseDouble(scoreString);

		String forwardScoreString = 
				msmsMatch.getAttributeValue(ReferenceMsMsLibraryMatchFields.FwdScore.name());
		if(forwardScoreString != null)
			forwardScore = Double.parseDouble(forwardScoreString);
		
		String reverseScoreString = 
				msmsMatch.getAttributeValue(ReferenceMsMsLibraryMatchFields.RevScore.name());
		if(reverseScoreString != null)
			reverseScore = Double.parseDouble(reverseScoreString);
		
		String probabilityString = 
				msmsMatch.getAttributeValue(ReferenceMsMsLibraryMatchFields.Prob.name());
		if(probabilityString != null)
			probability = Double.parseDouble(probabilityString);
		
		String dotProductString = 
				msmsMatch.getAttributeValue(ReferenceMsMsLibraryMatchFields.DotProd.name());
		if(dotProductString != null)
			dotProduct = Double.parseDouble(dotProductString);
		
		String reverseDotProductString = 
				msmsMatch.getAttributeValue(ReferenceMsMsLibraryMatchFields.RevDotProd.name());
		if(reverseDotProductString != null)
			reverseDotProduct = Double.parseDouble(reverseDotProductString);

		String hybridDotProductString = 
				msmsMatch.getAttributeValue(ReferenceMsMsLibraryMatchFields.HybDotProd.name());
		if(hybridDotProductString != null)
			hybridDotProduct = Double.parseDouble(hybridDotProductString);
		
		String hybridScoreString = 
				msmsMatch.getAttributeValue(ReferenceMsMsLibraryMatchFields.HybScore.name());
		if(hybridScoreString != null)
			hybridScore = Double.parseDouble(hybridScoreString);
		
		String hybridDeltaMzString = 
				msmsMatch.getAttributeValue(ReferenceMsMsLibraryMatchFields.HybDMZ.name());
		if(hybridDeltaMzString != null)
			hybridDeltaMz = Double.parseDouble(hybridDeltaMzString);
		
		String percolatorScoreString = 
				msmsMatch.getAttributeValue(ReferenceMsMsLibraryMatchFields.PercScore.name());
		if(percolatorScoreString != null)
			percolatorScore = Double.parseDouble(percolatorScoreString);

		String qValueString = 
				msmsMatch.getAttributeValue(ReferenceMsMsLibraryMatchFields.Qval.name());
		if(qValueString != null)
			qValue = Double.parseDouble(qValueString);
		
		String posteriorErrorProbabilityString = 
				msmsMatch.getAttributeValue(ReferenceMsMsLibraryMatchFields.PEP.name());
		if(posteriorErrorProbabilityString != null)
			posteriorErrorProbability = Double.parseDouble(posteriorErrorProbabilityString);
	}

	public double getEntropyBasedScore() {
		return entropyBasedScore;
	}

	public void setEntropyBasedScore(double entropyBasedScore) {
		this.entropyBasedScore = entropyBasedScore;
	}
}


























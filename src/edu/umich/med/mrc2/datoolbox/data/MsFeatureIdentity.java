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
import java.util.Date;
import java.util.UUID;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import edu.umich.med.mrc2.datoolbox.data.enums.CompoundDatabaseEnum;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundIdSource;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundIdentificationConfidence;
import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSUser;
import edu.umich.med.mrc2.datoolbox.project.store.MsFeatureIdentityFields;

public class MsFeatureIdentity implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7822618308543859536L;
	private String uniqueId;
	private CompoundIdentity compoundIdentity;
	private String compoundIdName;
	private CompoundIdSource idSource;
	private CompoundIdentificationConfidence confidenceLevel;
	private boolean isPrimary;
	private boolean qcStandard;
	private ReferenceMsMsLibraryMatch referenceMsMsLibraryMatch;
	private MsRtLibraryMatch msRtLibraryMatch;
	private Date assignedOn;
	private LIMSUser assignedBy; 
	private MSFeatureIdentificationLevel identificationLevel;
	private Adduct primaryAdduct;
	private double scoreCarryOver;	//	this is a purely service field to carry over the score during parsing

	public MsFeatureIdentity(
			CompoundIdentity compoundIdentity,
			CompoundIdentificationConfidence confidenceLevel) {

		this.uniqueId = DataPrefix.MS_FEATURE_IDENTITY.getName() + UUID.randomUUID().toString();
		this.compoundIdentity = compoundIdentity;
		this.confidenceLevel = confidenceLevel;

		if(confidenceLevel.getLevel() < 4)
			idSource = CompoundIdSource.LIBRARY;

		if(confidenceLevel.getLevel() == 4)
			idSource = CompoundIdSource.DATABASE;

		if(confidenceLevel.getLevel() > 4)
			idSource = CompoundIdSource.UNKNOWN;
	}

	public CompoundIdentity getCompoundIdentity() {
		return compoundIdentity;
	}

	public void setCompoundIdentity(CompoundIdentity compoundIdentity) {
		this.compoundIdentity = compoundIdentity;
	}

	public CompoundIdentificationConfidence getConfidenceLevel() {
		return confidenceLevel;
	}

	public void setConfidenceLevel(CompoundIdentificationConfidence confidenceLevel) {
		this.confidenceLevel = confidenceLevel;
	}

	public String getName() {
		
		if(compoundIdentity == null)
			return null;
		
		return compoundIdentity.getName();
	}

	public CompoundDatabaseEnum getPrimaryDatabase(){
		
		if(compoundIdentity == null)
			return null;
		
		return compoundIdentity.getPrimaryDatabase();
	}

	public String getPrimaryLinkAddress() {
		
		if(compoundIdentity == null)
			return null;
		
		return compoundIdentity.getPrimaryLinkAddress();
	}

	public String getPrimaryLinkLabel() {
		
		if(compoundIdentity == null)
			return null;
		
		return compoundIdentity.getPrimaryDatabaseId();
	}

	public boolean isPrimary() {
		return isPrimary;
	}

	public void setPrimary(boolean isPrimary) {
		this.isPrimary = isPrimary;
	}

	public CompoundIdSource getIdSource() {
		return idSource;
	}

	public void setIdSource(CompoundIdSource idSource) {
		this.idSource = idSource;
	}

	public boolean isQcStandard() {
		return qcStandard;
	}

	public void setQcStandard(boolean qcStandard) {
		this.qcStandard = qcStandard;
	}

	/**
	 * @return the libraryTargetName
	 */
	public String getIdentityName() {
		return compoundIdName;
	}

	/**
	 * @param libraryTargetName the libraryTargetName to set
	 */
	public void setIdentityName(String libraryTargetName) {
		this.compoundIdName = libraryTargetName;
	}

	@Override
	public boolean equals(Object cpdId) {

        if (cpdId == this)
            return true;

		if(cpdId == null)
			return false;

        if (!MsFeatureIdentity.class.isAssignableFrom(cpdId.getClass()))
            return false;

        MsFeatureIdentity cid = (MsFeatureIdentity)cpdId;

        if ((this.compoundIdentity == null) ? (cid.getCompoundIdentity() != null) :
        	!this.compoundIdentity.equals(cid.getCompoundIdentity()))
        	return false;

        if ((this.confidenceLevel == null) ? (cid.getConfidenceLevel() != null) :
        	!this.confidenceLevel.equals(cid.getConfidenceLevel()))
         	return false;

        if ((this.idSource == null) ? (cid.getIdSource() != null) :
        	!this.idSource.equals(cid.getIdSource()))
        	return false;

        if ((this.referenceMsMsLibraryMatch == null) ? (cid.getReferenceMsMsLibraryMatch() != null) :
        	!this.referenceMsMsLibraryMatch.equals(cid.getReferenceMsMsLibraryMatch()))
        	return false;

        if ((this.msRtLibraryMatch == null) ? (cid.getMsRtLibraryMatch() != null) :
        	!this.msRtLibraryMatch.equals(cid.getMsRtLibraryMatch()))
        	return false;

        return true;
	}

    @Override
    public int hashCode() {

        int hash = 3;
        hash = 53 * hash +
    		+ (this.compoundIdentity != null ? this.compoundIdentity.hashCode() : 0)
    		+ (this.confidenceLevel != null ? this.confidenceLevel.name().hashCode() : 0)
    		+ (this.referenceMsMsLibraryMatch != null ? this.referenceMsMsLibraryMatch.hashCode() : 0)
    		+ (this.msRtLibraryMatch != null ? this.msRtLibraryMatch.hashCode() : 0)
    		+ (this.idSource != null ? this.idSource.name().hashCode() : 0);
        return hash;
    }

	/**
	 * @return the uniqueId
	 */
	public String getUniqueId() {

		if(uniqueId == null)
			uniqueId = DataPrefix.MS_FEATURE_IDENTITY.getName() + UUID.randomUUID().toString();

		return uniqueId;
	}

	/**
	 * @param uniqueId the uniqueId to set
	 */
	public void setUniqueId(String uniqueId) {
		this.uniqueId = uniqueId;
	}

	/**
	 * @return the referenceMsMsLibraryMatch
	 */
	public ReferenceMsMsLibraryMatch getReferenceMsMsLibraryMatch() {
		return referenceMsMsLibraryMatch;
	}

	/**
	 * @param referenceMsMsLibraryMatch the referenceMsMsLibraryMatch to set
	 */
	public void setReferenceMsMsLibraryMatch(ReferenceMsMsLibraryMatch referenceMsMsLibraryMatch) {
		this.referenceMsMsLibraryMatch = referenceMsMsLibraryMatch;
	}

	/**
	 * @return the msRtLibraryMatch
	 */
	public MsRtLibraryMatch getMsRtLibraryMatch() {
		return msRtLibraryMatch;
	}

	/**
	 * @param msRtLibraryMatch the msRtLibraryMatch to set
	 */
	public void setMsRtLibraryMatch(MsRtLibraryMatch msRtLibraryMatch) {
		this.msRtLibraryMatch = msRtLibraryMatch;
	}

	public double getScore() {

		if(msRtLibraryMatch != null)
			return msRtLibraryMatch.getScore();

		if(referenceMsMsLibraryMatch != null)
			return referenceMsMsLibraryMatch.getScore();

		return 0.0d;
	}

	/**
	 * @return the assignedOn
	 */
	public Date getAssignedOn() {
		return assignedOn;
	}

	/**
	 * @return the assignedBy
	 */
	public LIMSUser getAssignedBy() {
		return assignedBy;
	}

	/**
	 * @param assignedOn the assignedOn to set
	 */
	public void setAssignedOn(Date assignedOn) {
		this.assignedOn = assignedOn;
	}

	/**
	 * @param assignedBy the assignedBy to set
	 */
	public void setAssignedBy(LIMSUser assignedBy) {
		this.assignedBy = assignedBy;
	}

	/**
	 * @return the identificationStatus
	 */
	public MSFeatureIdentificationLevel getIdentificationLevel() {
		return identificationLevel;
	}

	/**
	 * @param identificationStatus the identificationStatus to set
	 */
	public void setIdentificationLevel(MSFeatureIdentificationLevel identificationStatus) {
		this.identificationLevel = identificationStatus;
	}

	public Adduct getPrimaryAdduct() {
		
		if(primaryAdduct != null)
			return primaryAdduct;
		
		if(msRtLibraryMatch != null && msRtLibraryMatch.getTopAdductMatch() != null)
			return msRtLibraryMatch.getTopAdductMatch().getLibraryMatch();
		
		return null;
	}

	public void setPrimaryAdduct(Adduct primaryAdduct) {
		this.primaryAdduct = primaryAdduct;
	}

	public double getScoreCarryOver() {
		return scoreCarryOver;
	}

	public void setScoreCarryOver(double scoreCarryOver) {
		this.scoreCarryOver = scoreCarryOver;
	}
	
	public Element getXmlElement(Document parentDocument) {
		
		Element msIdElement = parentDocument.createElement(
				MsFeatureIdentityFields.MSFID.name());
		
		msIdElement.setAttribute(MsFeatureIdentityFields.Id.name(), uniqueId);
		if(compoundIdentity != null) 
			msIdElement.setAttribute(MsFeatureIdentityFields.CID.name(), 
					compoundIdentity.getPrimaryDatabaseId());
		
		if(compoundIdName != null)
			msIdElement.setAttribute(MsFeatureIdentityFields.Name.name(), 
					compoundIdName);
		
		if(idSource != null)
			msIdElement.setAttribute(MsFeatureIdentityFields.Source.name(), 
					idSource.name());
		
		if(confidenceLevel != null)
			msIdElement.setAttribute(MsFeatureIdentityFields.Conf.name(), 
					confidenceLevel.name());
		
		msIdElement.setAttribute(MsFeatureIdentityFields.Prim.name(), 
				Boolean.toString(isPrimary));
		msIdElement.setAttribute(MsFeatureIdentityFields.Qc.name(), 
				Boolean.toString(qcStandard));		
		if(referenceMsMsLibraryMatch != null)
			msIdElement.appendChild(
					referenceMsMsLibraryMatch.getXmlElement(parentDocument));
		
		if(msRtLibraryMatch != null) {
			msIdElement.appendChild(
					msRtLibraryMatch.getXmlElement(parentDocument));
		}	
		if(assignedBy != null)
			msIdElement.setAttribute(MsFeatureIdentityFields.User.name(), 
					assignedBy.getId());
		
		if(identificationLevel != null)
			msIdElement.setAttribute(MsFeatureIdentityFields.IdLevel.name(), 
					identificationLevel.getId());
		
		if(primaryAdduct != null)
			msIdElement.setAttribute(MsFeatureIdentityFields.Adduct.name(), 
					primaryAdduct.getId());
		
		if(scoreCarryOver > 0.0d)
			msIdElement.setAttribute(MsFeatureIdentityFields.SCO.name(), 
					Double.toString(scoreCarryOver));
		
		return msIdElement;
	}
}














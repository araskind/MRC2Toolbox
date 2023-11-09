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
import java.text.ParseException;
import java.util.Date;
import java.util.UUID;

import org.jdom2.Element;

import edu.umich.med.mrc2.datoolbox.data.enums.CompoundDatabaseEnum;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundIdSource;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundIdentificationConfidence;
import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSUser;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCache;
import edu.umich.med.mrc2.datoolbox.database.idt.OfflineExperimentLoadCache;
import edu.umich.med.mrc2.datoolbox.main.AdductManager;
import edu.umich.med.mrc2.datoolbox.project.store.MsFeatureIdentityFields;
import edu.umich.med.mrc2.datoolbox.project.store.MsRtLibraryMatchFields;
import edu.umich.med.mrc2.datoolbox.project.store.ReferenceMsMsLibraryMatchFields;
import edu.umich.med.mrc2.datoolbox.utils.ExperimentUtils;

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

		this.uniqueId = 
				DataPrefix.MS_FEATURE_IDENTITY.getName() + UUID.randomUUID().toString();
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

	public String getCompoundName() {
		
		if(compoundIdentity == null)
			return null;
		else
			return compoundIdentity.getName();
	}
	
	//	TODO fix this mess
	public String getIdentityName() {
		
		if(compoundIdName != null)
			return compoundIdName;
		else if(msRtLibraryMatch != null 
				&& msRtLibraryMatch.getLibraryTargetName() != null)
			return msRtLibraryMatch.getLibraryTargetName();
		else if(compoundIdentity != null)
			return compoundIdentity.getName();
		else
			return null;
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
		else
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

        MsFeatureIdentity other = (MsFeatureIdentity)cpdId;
        if ((this.uniqueId == null) ? (other.getUniqueId() != null) : !this.uniqueId.equals(other.getUniqueId()))
            return false;

//        if ((this.compoundIdentity == null) ? (cid.getCompoundIdentity() != null) :
//        	!this.compoundIdentity.equals(cid.getCompoundIdentity()))
//        	return false;
//
//        if ((this.confidenceLevel == null) ? (cid.getConfidenceLevel() != null) :
//        	!this.confidenceLevel.equals(cid.getConfidenceLevel()))
//         	return false;
//
//        if ((this.idSource == null) ? (cid.getIdSource() != null) :
//        	!this.idSource.equals(cid.getIdSource()))
//        	return false;
//
//        if ((this.referenceMsMsLibraryMatch == null) ? (cid.getReferenceMsMsLibraryMatch() != null) :
//        	!this.referenceMsMsLibraryMatch.equals(cid.getReferenceMsMsLibraryMatch()))
//        	return false;
//
//        if ((this.msRtLibraryMatch == null) ? (cid.getMsRtLibraryMatch() != null) :
//        	!this.msRtLibraryMatch.equals(cid.getMsRtLibraryMatch()))
//        	return false;

        return true;
	}

    @Override
    public int hashCode() {

        int hash = 3;
        hash = 53 * hash + (this.uniqueId != null ? this.uniqueId.hashCode() : 0);
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

	public ReferenceMsMsLibraryMatch getReferenceMsMsLibraryMatch() {
		return referenceMsMsLibraryMatch;
	}

	public void setReferenceMsMsLibraryMatch(
			ReferenceMsMsLibraryMatch referenceMsMsLibraryMatch) {
		
		this.referenceMsMsLibraryMatch = referenceMsMsLibraryMatch;
		compoundIdentity = 
				referenceMsMsLibraryMatch.getMatchedLibraryFeature().getCompoundIdentity();
		idSource = CompoundIdSource.LIBRARY_MS2;
		confidenceLevel = CompoundIdentificationConfidence.ACCURATE_MASS_MSMS;
	}

	public MsRtLibraryMatch getMsRtLibraryMatch() {
		return msRtLibraryMatch;
	}

	public void setMsRtLibraryMatch(MsRtLibraryMatch msRtLibraryMatch) {
		
		this.msRtLibraryMatch = msRtLibraryMatch;	
		compoundIdName = msRtLibraryMatch.getLibraryTargetName();
		idSource = CompoundIdSource.LIBRARY;
		confidenceLevel = CompoundIdentificationConfidence.ACCURATE_MASS_RT;
	}

	public double getScore() {

		if(msRtLibraryMatch != null)
			return msRtLibraryMatch.getScore();

		if(referenceMsMsLibraryMatch != null)
			return referenceMsMsLibraryMatch.getScore();

		return 0.0d;
	}
	
	public double getEntropyBasedScore() {

		if(referenceMsMsLibraryMatch != null)
			return referenceMsMsLibraryMatch.getEntropyBasedScore();
		
		if(msRtLibraryMatch != null 
				&& msRtLibraryMatch.getTopAdductMatch() != null)
			return msRtLibraryMatch.getTopAdductMatch().getEntropyScore();

		return 0.0d;
	}

	public Date getAssignedOn() {
		return assignedOn;
	}

	public LIMSUser getAssignedBy() {
		return assignedBy;
	}

	public void setAssignedOn(Date assignedOn) {
		this.assignedOn = assignedOn;
	}

	public void setAssignedBy(LIMSUser assignedBy) {
		this.assignedBy = assignedBy;
	}

	public MSFeatureIdentificationLevel getIdentificationLevel() {
		
		if(compoundIdentity == null)
			return null;
		else			
			return identificationLevel;
	}

	public void setIdentificationLevel(
			MSFeatureIdentificationLevel identificationStatus) {
		if(compoundIdentity == null)
			this.identificationLevel = null;
		else			
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
	
	public Element getXmlElement() {
		
		Element msIdElement = 
				new Element(MsFeatureIdentityFields.MSFID.name());
		
		msIdElement.setAttribute(MsFeatureIdentityFields.Id.name(), uniqueId);
		if(compoundIdentity != null && compoundIdentity.getPrimaryDatabaseId() != null) 
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
			msIdElement.addContent(
					referenceMsMsLibraryMatch.getXmlElement());
		
		if(msRtLibraryMatch != null) {
			msIdElement.addContent(
					msRtLibraryMatch.getXmlElement());
		}	
		if(assignedBy != null)
			msIdElement.setAttribute(MsFeatureIdentityFields.User.name(), 
					assignedBy.getId());
		
		if(assignedOn != null)
			msIdElement.setAttribute(MsFeatureIdentityFields.AssignedOn.name(), 
					ExperimentUtils.dateTimeFormat.format(assignedOn));			
		
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
	
	public MsFeatureIdentity(Element msfIdElement) {

		uniqueId = 
				msfIdElement.getAttributeValue(MsFeatureIdentityFields.Id.name());		
		String cid = msfIdElement.getAttributeValue(MsFeatureIdentityFields.CID.name());
		if(cid != null)
			compoundIdentity = OfflineExperimentLoadCache.getCompoundIdentityByAccession(cid);
		
		compoundIdName = msfIdElement.getAttributeValue(MsFeatureIdentityFields.Name.name());	
		String idSourceString = 
				msfIdElement.getAttributeValue(MsFeatureIdentityFields.Source.name());	
		if(idSourceString != null)
			idSource = CompoundIdSource.getIdSourceByName(idSourceString);
			
		String confString = 
				msfIdElement.getAttributeValue(MsFeatureIdentityFields.Conf.name());
		if(confString != null)
			confidenceLevel = CompoundIdentificationConfidence.getLevelByName(confString);

		String identificationLevelString = 
				msfIdElement.getAttributeValue(MsFeatureIdentityFields.IdLevel.name());
		if(identificationLevelString != null)
			identificationLevel = 
				IDTDataCache.getMSFeatureIdentificationLevelById(identificationLevelString);
		
		isPrimary = Boolean.parseBoolean(
				msfIdElement.getAttributeValue(MsFeatureIdentityFields.Prim.name()));
		qcStandard = Boolean.parseBoolean(
				msfIdElement.getAttributeValue(MsFeatureIdentityFields.Qc.name()));

		Element msmsMatch = 
				msfIdElement.getChild(ReferenceMsMsLibraryMatchFields.RefMsms.name());	
		if(msmsMatch != null)
			referenceMsMsLibraryMatch = 
					new ReferenceMsMsLibraryMatch(msmsMatch);

		Element msRtMatch = 
				msfIdElement.getChild(MsRtLibraryMatchFields.RefMsRt.name());	
		if(msRtMatch != null)
			msRtLibraryMatch = 
					new MsRtLibraryMatch(msRtMatch);
		
		String assignedOnString = 
				msfIdElement.getAttributeValue(MsFeatureIdentityFields.AssignedOn.name());
		if(assignedOnString != null) {
			try {
				assignedOn = ExperimentUtils.dateTimeFormat.parse(assignedOnString);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		String userId = 
				msfIdElement.getAttributeValue(MsFeatureIdentityFields.User.name());
		if(userId != null)
			assignedBy = IDTDataCache.getUserById(userId);
		
		String primaryAdductId = 
				msfIdElement.getAttributeValue(MsFeatureIdentityFields.Adduct.name());
		if(primaryAdductId != null)
			primaryAdduct = AdductManager.getAdductById(primaryAdductId);

		String scoreCarryOverString = 
				msfIdElement.getAttributeValue(MsFeatureIdentityFields.SCO.name());
		if(scoreCarryOverString != null)
			scoreCarryOver = Double.parseDouble(scoreCarryOverString);
	}
}





























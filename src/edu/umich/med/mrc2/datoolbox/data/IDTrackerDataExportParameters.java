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

import java.util.Collection;

import edu.umich.med.mrc2.datoolbox.data.enums.DecoyExportHandling;
import edu.umich.med.mrc2.datoolbox.data.enums.FeatureIDSubset;
import edu.umich.med.mrc2.datoolbox.data.enums.IDTrackerFeatureIdentificationProperties;
import edu.umich.med.mrc2.datoolbox.data.enums.IDTrackerMSMSClusterProperties;
import edu.umich.med.mrc2.datoolbox.data.enums.IDTrackerMsFeatureProperties;
import edu.umich.med.mrc2.datoolbox.data.enums.MSMSMatchType;
import edu.umich.med.mrc2.datoolbox.data.enums.MSMSScoringParameter;
import edu.umich.med.mrc2.datoolbox.data.enums.MassErrorType;
import edu.umich.med.mrc2.datoolbox.data.enums.MsDepth;
import edu.umich.med.mrc2.datoolbox.gui.idworks.clustree.MajorClusterFeatureDefiningProperty;

public class IDTrackerDataExportParameters {

	private MsDepth msLevel;
	private Collection<IDTrackerMSMSClusterProperties> msmsClusterPropertyList;
	private Collection<IDTrackerMsFeatureProperties> featurePropertyList;
	private Collection<IDTrackerFeatureIdentificationProperties> identificationDetailsList; 
	private boolean removeRedundant;
	private double redundantMzWindow;
	private MassErrorType redMzErrorType;
	private double redundantRTWindow;	
	private MSMSScoringParameter msmsScoringParameter;	
	private double minimalMSMSScore;	
	private FeatureIDSubset featureIDSubset;	
	private Collection<MSMSMatchType>msmsSearchTypes;
	private boolean excludeFromExportWhenAllIdsFilteredOut;
	private boolean exportIndividualFeatures;	//	For cluster export
	private DecoyExportHandling decoyExportHandling;
	private MajorClusterFeatureDefiningProperty majorClusterFeatureDefiningProperty;
	private CompoundIdFilter compoundIdFilter;
	
	public IDTrackerDataExportParameters() {
		super();
		// TODO Auto-generated constructor stub
	}

	//	For feature export
	public IDTrackerDataExportParameters(
			MsDepth msLevel, 
			Collection<IDTrackerMsFeatureProperties> featurePropertyList,
			Collection<IDTrackerFeatureIdentificationProperties> identificationDetailsList, 
			boolean removeRedundant,
			double redundantMzWindow, 
			MassErrorType redMzErrorType, 
			double redundantRTWindow,
			MSMSScoringParameter msmsScoringParameter, 
			double minimalMSMSScore, 
			FeatureIDSubset featureIDSubset,
			Collection<MSMSMatchType> msmsSearchTypes,
			boolean excludeFromExportWhenAllIdsFilteredOut) {
		super();
		this.msLevel = msLevel;
		this.featurePropertyList = featurePropertyList;
		this.identificationDetailsList = identificationDetailsList;
		this.removeRedundant = removeRedundant;
		this.redundantMzWindow = redundantMzWindow;
		this.redMzErrorType = redMzErrorType;
		this.redundantRTWindow = redundantRTWindow;
		this.msmsScoringParameter = msmsScoringParameter;
		this.minimalMSMSScore = minimalMSMSScore;
		this.featureIDSubset = featureIDSubset;
		this.msmsSearchTypes = msmsSearchTypes;
		this.excludeFromExportWhenAllIdsFilteredOut = 
				excludeFromExportWhenAllIdsFilteredOut;		
	}
	
	//	For cluster export
	public IDTrackerDataExportParameters(
			MsDepth msLevel, 
			Collection<IDTrackerMSMSClusterProperties> msmsClusterPropertyList,
			Collection<IDTrackerMsFeatureProperties> featurePropertyList,
			Collection<IDTrackerFeatureIdentificationProperties> identificationDetailsList, 
			boolean exportIndividualFeatures,
			MSMSScoringParameter msmsScoringParameter, 
			double minimalMSMSScore, 
			FeatureIDSubset featureIDSubset,
			Collection<MSMSMatchType> msmsSearchTypes,
			boolean excludeFromExportWhenAllIdsFilteredOut,
			MajorClusterFeatureDefiningProperty majorClusterFeatureDefiningProperty) {
		super();
		this.msLevel = msLevel;
		this.msmsClusterPropertyList = msmsClusterPropertyList;
		this.featurePropertyList = featurePropertyList;
		this.identificationDetailsList = identificationDetailsList;
		this.exportIndividualFeatures = exportIndividualFeatures;		
		this.msmsScoringParameter = msmsScoringParameter;
		this.minimalMSMSScore = minimalMSMSScore;
		this.featureIDSubset = featureIDSubset;
		this.msmsSearchTypes = msmsSearchTypes;
		this.excludeFromExportWhenAllIdsFilteredOut = excludeFromExportWhenAllIdsFilteredOut;
		this.majorClusterFeatureDefiningProperty = majorClusterFeatureDefiningProperty;
	}

	public MsDepth getMsLevel() {
		return msLevel;
	}

	public void setMsLevel(MsDepth msLevel) {
		this.msLevel = msLevel;
	}

	public Collection<IDTrackerMsFeatureProperties> getFeaturePropertyList() {
		return featurePropertyList;
	}

	public void setFeaturePropertyList(Collection<IDTrackerMsFeatureProperties> featurePropertyList) {
		this.featurePropertyList = featurePropertyList;
	}

	public Collection<IDTrackerFeatureIdentificationProperties> getIdentificationDetailsList() {
		return identificationDetailsList;
	}

	public void setIdentificationDetailsList(
			Collection<IDTrackerFeatureIdentificationProperties> identificationDetailsList) {
		this.identificationDetailsList = identificationDetailsList;
	}

	public boolean isRemoveRedundant() {
		return removeRedundant;
	}

	public void setRemoveRedundant(boolean removeRedundant) {
		this.removeRedundant = removeRedundant;
	}

	public double getRedundantMzWindow() {
		return redundantMzWindow;
	}

	public void setRedundantMzWindow(double redundantMzWindow) {
		this.redundantMzWindow = redundantMzWindow;
	}

	public MassErrorType getRedMzErrorType() {
		return redMzErrorType;
	}

	public void setRedMzErrorType(MassErrorType redMzErrorType) {
		this.redMzErrorType = redMzErrorType;
	}

	public double getRedundantRTWindow() {
		return redundantRTWindow;
	}

	public void setRedundantRTWindow(double redundantRTWindow) {
		this.redundantRTWindow = redundantRTWindow;
	}

	public MSMSScoringParameter getMsmsScoringParameter() {
		return msmsScoringParameter;
	}

	public void setMsmsScoringParameter(MSMSScoringParameter msmsScoringParameter) {
		this.msmsScoringParameter = msmsScoringParameter;
	}

	public double getMinimalMSMSScore() {
		return minimalMSMSScore;
	}

	public void setMinimalMSMSScore(double minimalMSMSScore) {
		this.minimalMSMSScore = minimalMSMSScore;
	}

	public FeatureIDSubset getFeatureIDSubset() {
		return featureIDSubset;
	}

	public void setFeatureIDSubset(FeatureIDSubset featureIDSubset) {
		this.featureIDSubset = featureIDSubset;
	}

	public Collection<MSMSMatchType> getMsmsSearchTypes() {
		return msmsSearchTypes;
	}

	public void setMsmsSearchTypes(Collection<MSMSMatchType> msmsSearchTypes) {
		this.msmsSearchTypes = msmsSearchTypes;
	}

	public boolean isExcludeFromExportWhenAllIdsFilteredOut() {
		return excludeFromExportWhenAllIdsFilteredOut;
	}

	public void setExcludeFromExportWhenAllIdsFilteredOut(boolean excludeFromExportWhenAllIdsFilteredOut) {
		this.excludeFromExportWhenAllIdsFilteredOut = excludeFromExportWhenAllIdsFilteredOut;
	}

	public Collection<IDTrackerMSMSClusterProperties> getMsmsClusterPropertyList() {
		return msmsClusterPropertyList;
	}

	public void setMsmsClusterPropertyList(Collection<IDTrackerMSMSClusterProperties> msmsClusterPropertyList) {
		this.msmsClusterPropertyList = msmsClusterPropertyList;
	}

	public boolean isExportIndividualFeatures() {
		return exportIndividualFeatures;
	}

	public void setExportIndividualFeatures(boolean exportIndividualFeatures) {
		this.exportIndividualFeatures = exportIndividualFeatures;
	}

	public DecoyExportHandling getDecoyExportHandling() {
		return decoyExportHandling;
	}

	public void setDecoyExportHandling(DecoyExportHandling decoyExportHandling) {
		this.decoyExportHandling = decoyExportHandling;
	}

	public MajorClusterFeatureDefiningProperty getMajorClusterFeatureDefiningProperty() {
		return majorClusterFeatureDefiningProperty;
	}

	public void setMajorClusterFeatureDefiningProperty(
			MajorClusterFeatureDefiningProperty majorClusterFeatureDefiningProperty) {
		this.majorClusterFeatureDefiningProperty = majorClusterFeatureDefiningProperty;
	}

	public CompoundIdFilter getCompoundIdFilter() {
		return compoundIdFilter;
	}

	public void setCompoundIdFilter(CompoundIdFilter compoundIdFilter) {
		this.compoundIdFilter = compoundIdFilter;
	}
}

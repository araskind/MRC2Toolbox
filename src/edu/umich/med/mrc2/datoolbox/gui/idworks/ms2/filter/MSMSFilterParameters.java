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

package edu.umich.med.mrc2.datoolbox.gui.idworks.ms2.filter;

import java.util.Collection;

import edu.umich.med.mrc2.datoolbox.data.enums.FeatureSubsetByIdentification;
import edu.umich.med.mrc2.datoolbox.data.enums.IncludeSubset;
import edu.umich.med.mrc2.datoolbox.gui.idworks.nist.pepsearch.HiResSearchOption;
import edu.umich.med.mrc2.datoolbox.utils.Range;

public class MSMSFilterParameters {

	private Range mzRange; 
	private Range rtRange; 
	private String featureNameSubstring; 
	private boolean doSearchAllIds;
	private FeatureSubsetByIdentification idStatusSubset; 
	private Range entropyRange; 
	private Range peakAreaRange;
	private Range precursorPurityRange;
	private Collection<HiResSearchOption>msmsSearchTypes; 
	private double minimalMSMSScore;
	private Collection<Range> fragmentMZRangeList;
	private IncludeSubset fragmentsIncludeSubset;
	private double fragmentIntensityCutoff;
	private Collection<Range> massDifferencesRangeList;
	private boolean neutralLossesOnly;
	private IncludeSubset massDiffsIncludeSubset;
	private double massDiffsIntensityCutoff;
	
	public MSMSFilterParameters() {
		super();
		// TODO Auto-generated constructor stub
	}

	public MSMSFilterParameters(
			Range mzRange, 
			Range rtRange, 
			String featureNameSubstring, 
			boolean doSearchAllIds,
			FeatureSubsetByIdentification idStatusSubset, 
			Range entropyRange, 
			Range peakAreaRange,
			Range precursorPurityRange,
			Collection<HiResSearchOption> msmsSearchTypes, 
			double minimalMSMSScore,
			Collection<Range> fragmentMZRangeList, 
			IncludeSubset fragmentsIncludeSubset, 
			double fragmentIntensityCutoff,
			Collection<Range> massDifferencesRangeList,			
			boolean neutralLossesOnly,
			IncludeSubset massDiffsIncludeSubset,
			double massDiffsIntensityCutoff) {
		super();
		this.mzRange = mzRange;
		this.rtRange = rtRange;
		this.featureNameSubstring = featureNameSubstring;
		this.doSearchAllIds = doSearchAllIds;
		this.idStatusSubset = idStatusSubset;
		this.entropyRange = entropyRange;
		this.peakAreaRange = peakAreaRange;
		this.precursorPurityRange = precursorPurityRange; 
		this.msmsSearchTypes = msmsSearchTypes;
		this.minimalMSMSScore = minimalMSMSScore;
		this.fragmentMZRangeList = fragmentMZRangeList;
		this.fragmentsIncludeSubset = fragmentsIncludeSubset;
		this.fragmentIntensityCutoff = fragmentIntensityCutoff;
		this.massDifferencesRangeList = massDifferencesRangeList;
		this.neutralLossesOnly = neutralLossesOnly;
		this.massDiffsIncludeSubset = massDiffsIncludeSubset;
		this.massDiffsIntensityCutoff = massDiffsIntensityCutoff;
	}

	public Range getMzRange() {
		return mzRange;
	}

	public void setMzRange(Range mzRange) {
		this.mzRange = mzRange;
	}

	public Range getRtRange() {
		return rtRange;
	}

	public void setRtRange(Range rtRange) {
		this.rtRange = rtRange;
	}

	public String getFeatureNameSubstring() {
		return featureNameSubstring;
	}

	public void setFeatureNameSubstring(String featureNameSubstring) {
		this.featureNameSubstring = featureNameSubstring;
	}

	public boolean isDoSearchAllIds() {
		return doSearchAllIds;
	}

	public void setDoSearchAllIds(boolean doSearchAllIds) {
		this.doSearchAllIds = doSearchAllIds;
	}

	public FeatureSubsetByIdentification getIdStatusSubset() {
		return idStatusSubset;
	}

	public void setIdStatusSubset(FeatureSubsetByIdentification idStatusSubset) {
		this.idStatusSubset = idStatusSubset;
	}

	public Range getEntropyRange() {
		return entropyRange;
	}

	public void setEntropyRange(Range entropyRange) {
		this.entropyRange = entropyRange;
	}

	public Range getPeakAreaRange() {
		return peakAreaRange;
	}

	public void setPeakAreaRange(Range peakAreaRange) {
		this.peakAreaRange = peakAreaRange;
	}

	public Collection<HiResSearchOption> getMsmsSearchTypes() {
		return msmsSearchTypes;
	}

	public void setMsmsSearchTypes(Collection<HiResSearchOption> msmsSearchTypes) {
		this.msmsSearchTypes = msmsSearchTypes;
	}

	public double getMinimalMSMSScore() {
		return minimalMSMSScore;
	}

	public void setMinimalMSMSScore(double minimalMSMSScore) {
		this.minimalMSMSScore = minimalMSMSScore;
	}

	public Collection<Range> getFragmentMZRangeList() {
		return fragmentMZRangeList;
	}

	public void setFragmentMZRangeList(Collection<Range> fragmentMZRangeList) {
		this.fragmentMZRangeList = fragmentMZRangeList;
	}

	public IncludeSubset getFragmentsIncludeSubset() {
		return fragmentsIncludeSubset;
	}

	public void setFragmentsIncludeSubset(IncludeSubset fragmentsIncludeSubset) {
		this.fragmentsIncludeSubset = fragmentsIncludeSubset;
	}

	public double getFragmentIntensityCutoff() {
		return fragmentIntensityCutoff;
	}

	public void setFragmentIntensityCutoff(double fragmentIntensityCutoff) {
		this.fragmentIntensityCutoff = fragmentIntensityCutoff;
	}

	public Collection<Range> getMassDifferencesRangeList() {
		return massDifferencesRangeList;
	}

	public void setMassDifferencesRangeList(Collection<Range> massDifferencesRangeList) {
		this.massDifferencesRangeList = massDifferencesRangeList;
	}

	public IncludeSubset getMassDiffsIncludeSubset() {
		return massDiffsIncludeSubset;
	}

	public void setMassDiffsIncludeSubset(IncludeSubset massDiffsIncludeSubset) {
		this.massDiffsIncludeSubset = massDiffsIncludeSubset;
	}

	public double getMassDiffsIntensityCutoff() {
		return massDiffsIntensityCutoff;
	}

	public void setMassDiffsIntensityCutoff(double massDiffsIntensityCutoff) {
		this.massDiffsIntensityCutoff = massDiffsIntensityCutoff;
	}

	public Range getPrecursorPurityRange() {
		return precursorPurityRange;
	}

	public void setPrecursorPurityRange(Range precursorPurityRange) {
		this.precursorPurityRange = precursorPurityRange;
	}

	public boolean isNeutralLossesOnly() {
		return neutralLossesOnly;
	}

	public void setNeutralLossesOnly(boolean neutralLossesOnly) {
		this.neutralLossesOnly = neutralLossesOnly;
	}
}

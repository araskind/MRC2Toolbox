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

package edu.umich.med.mrc2.datoolbox.cpdmatch;

import java.io.File;
import java.util.Set;
import java.util.TreeSet;

import edu.umich.med.mrc2.datoolbox.data.Adduct;

public class CompoundMatchMergeOutputParametersObject {

	private File originalPCDL;
	private File referenceBatchProFinderResults;
	private File compoundMatchOutput;
	private File customPCDL;
	private double maxPercentMissingPooled;
	private double maxPercentMissingSample;
	private double mzErrorForMissingLookup;
	private double rtErrorForMissingLookup;
	private Set<Adduct> adductSet;
	
	public CompoundMatchMergeOutputParametersObject(
			File originalPCDL, 
			File referenceBatchProFinderResults, 
			File compoundMatchOutput,
			File customPCDL) {
		super();
		this.originalPCDL = originalPCDL;
		this.referenceBatchProFinderResults = referenceBatchProFinderResults;
		this.compoundMatchOutput = compoundMatchOutput;
		this.customPCDL = customPCDL;
		adductSet = new TreeSet<Adduct>();
	}

	public File getOriginalPCDL() {
		return originalPCDL;
	}

	public void setOriginalPCDL(File originalPCDL) {
		this.originalPCDL = originalPCDL;
	}

	public File getReferenceBatchProFinderResults() {
		return referenceBatchProFinderResults;
	}

	public void setReferenceBatchProFinderResults(File referenceBatchProFinderResults) {
		this.referenceBatchProFinderResults = referenceBatchProFinderResults;
	}

	public File getCompoundMatchOutput() {
		return compoundMatchOutput;
	}

	public void setCompoundMatchOutput(File compoundMatchOutput) {
		this.compoundMatchOutput = compoundMatchOutput;
	}

	public File getCustomPCDL() {
		return customPCDL;
	}

	public void setCustomPCDL(File customPCDL) {
		this.customPCDL = customPCDL;
	}

	public double getMaxPercentMissingPooled() {
		return maxPercentMissingPooled;
	}

	public void setMaxPercentMissingPooled(double maxPercentMissingPooled) {
		this.maxPercentMissingPooled = maxPercentMissingPooled;
	}

	public double getMaxPercentMissingSample() {
		return maxPercentMissingSample;
	}

	public void setMaxPercentMissingSample(double maxPercentMissingSample) {
		this.maxPercentMissingSample = maxPercentMissingSample;
	}

	public double getMzErrorForMissingLookup() {
		return mzErrorForMissingLookup;
	}

	public void setMzErrorForMissingLookup(double mzErrorForMissingLookup) {
		this.mzErrorForMissingLookup = mzErrorForMissingLookup;
	}

	public double getRtErrorForMissingLookup() {
		return rtErrorForMissingLookup;
	}

	public void setRtErrorForMissingLookup(double rtErrorForMissingLookup) {
		this.rtErrorForMissingLookup = rtErrorForMissingLookup;
	}

	public Set<Adduct> getAdductSet() {
		return adductSet;
	}
}

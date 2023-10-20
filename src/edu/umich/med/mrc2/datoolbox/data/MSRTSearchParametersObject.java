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

import java.util.Collection;
import java.util.TreeSet;

import edu.umich.med.mrc2.datoolbox.data.enums.MassErrorType;

public class MSRTSearchParametersObject {

	private boolean clearPreviousResults;
	private double massError;
	private MassErrorType massErrorType;
	private boolean relaxMassErrorForMinorIsotopes;
	private boolean ignoreAdductType;
	private double retentionWindow;
	private boolean useCustomRtWindow;
	private int maxHits;
	private Collection<String>libraryIds;
	
	public MSRTSearchParametersObject(
			boolean clearPreviousResults,
			double massError, 
			MassErrorType massErrorType,
			boolean relaxMassErrorForMinorIsotopes, 
			boolean ignoreAdductType, 
			double retentionWindow,
			boolean useCustomRtWindow, 
			int maxHits) {
		super();
		this.clearPreviousResults = clearPreviousResults;
		this.massError = massError;
		this.massErrorType = massErrorType;
		this.relaxMassErrorForMinorIsotopes = relaxMassErrorForMinorIsotopes;
		this.ignoreAdductType = ignoreAdductType;
		this.retentionWindow = retentionWindow;
		this.useCustomRtWindow = useCustomRtWindow;
		this.maxHits = maxHits;
		libraryIds = new TreeSet<String>();
	}

	public double getMassError() {
		return massError;
	}

	public void setMassError(double massError) {
		this.massError = massError;
	}

	public MassErrorType getMassErrorType() {
		return massErrorType;
	}

	public void setMassErrorType(MassErrorType massErrorType) {
		this.massErrorType = massErrorType;
	}

	public boolean isRelaxMassErrorForMinorIsotopes() {
		return relaxMassErrorForMinorIsotopes;
	}

	public void setRelaxMassErrorForMinorIsotopes(
			boolean relaxMassErrorForMinorIsotopes) {
		this.relaxMassErrorForMinorIsotopes = 
				relaxMassErrorForMinorIsotopes;
	}

	public boolean isIgnoreAdductType() {
		return ignoreAdductType;
	}

	public void setIgnoreAdductType(boolean ignoreAdductType) {
		this.ignoreAdductType = ignoreAdductType;
	}

	public double getRetentionWindow() {
		return retentionWindow;
	}

	public void setRetentionWindow(double retentionWindow) {
		this.retentionWindow = retentionWindow;
	}

	public boolean isUseCustomRtWindow() {
		return useCustomRtWindow;
	}

	public void setUseCustomRtWindow(boolean useCustomRtWindow) {
		this.useCustomRtWindow = useCustomRtWindow;
	}

	public int getMaxHits() {
		return maxHits;
	}

	public void setMaxHits(int maxHits) {
		this.maxHits = maxHits;
	}

	public Collection<String> getLibraryIds() {
		return libraryIds;
	}
	
	public void addLibraryId(String libraryId) {
		libraryIds.add(libraryId);
	}

	public boolean isClearPreviousResults() {
		return clearPreviousResults;
	}

	public void setClearPreviousResults(boolean clearPreviousResults) {
		this.clearPreviousResults = clearPreviousResults;
	}	
}

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

package edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.rawdata;

import edu.umich.med.mrc2.datoolbox.data.enums.IntensityMeasure;
import edu.umich.med.mrc2.datoolbox.data.enums.MassErrorType;
import edu.umich.med.mrc2.datoolbox.utils.Range;

public class MSMSExtractionParameterSet {
	
	private Range dataExtractionRtRange;
	private boolean removeAllMassesAboveParent;
	private double msMsCountsCutoff;
	private int maxFragmentsCutoff;
	private IntensityMeasure filterIntensityMeasure;
	private double msmsIsolationWindowLowerBorder;
	private double msmsIsolationWindowUpperBorder;	
	private double msmsGroupingRtWindow;
	private double precursorGroupingMassError;
	private MassErrorType precursorGroupingMassErrorType;
	private boolean flagMinorIsotopesPrecursors;
	private int maxPrecursorCharge;
	private int smoothingFilterWidth;
	private double chromatogramExtractionWindow;
	
	public MSMSExtractionParameterSet(

			Range dataExtractionRtRange, 
			boolean removeAllMassesAboveParent, 
			double msMsCountsCutoff,
			int maxFragmentsCutoff, 
			IntensityMeasure filterIntensityMeasure, 
			double msmsIsolationWindowLowerBorder,
			double msmsIsolationWindowUpperBorder, 
			double msmsGroupingRtWindow, 
			double precursorGroupingMassError,
			MassErrorType precursorGroupingMassErrorType, 
			boolean flagMinorIsotopesPrecursors, 
			int maxPrecursorCharge,
			int smoothingFilterWidth,
			double chromatogramExtractionWindow) {
		super();
		this.dataExtractionRtRange = dataExtractionRtRange;
		this.removeAllMassesAboveParent = removeAllMassesAboveParent;
		this.msMsCountsCutoff = msMsCountsCutoff;
		this.maxFragmentsCutoff = maxFragmentsCutoff;
		this.filterIntensityMeasure = filterIntensityMeasure;
		this.msmsIsolationWindowLowerBorder = msmsIsolationWindowLowerBorder;
		this.msmsIsolationWindowUpperBorder = msmsIsolationWindowUpperBorder;
		this.msmsGroupingRtWindow = msmsGroupingRtWindow;
		this.precursorGroupingMassError = precursorGroupingMassError;
		this.precursorGroupingMassErrorType = precursorGroupingMassErrorType;
		this.flagMinorIsotopesPrecursors = flagMinorIsotopesPrecursors;
		this.maxPrecursorCharge = maxPrecursorCharge;
		this.smoothingFilterWidth = smoothingFilterWidth;
		this.chromatogramExtractionWindow = chromatogramExtractionWindow;
	}

	public MSMSExtractionParameterSet() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Range getDataExtractionRtRange() {
		return dataExtractionRtRange;
	}

	public void setDataExtractionRtRange(Range dataExtractionRtRange) {
		this.dataExtractionRtRange = dataExtractionRtRange;
	}

	public boolean isRemoveAllMassesAboveParent() {
		return removeAllMassesAboveParent;
	}

	public void setRemoveAllMassesAboveParent(boolean removeAllMassesAboveParent) {
		this.removeAllMassesAboveParent = removeAllMassesAboveParent;
	}

	public double getMsMsCountsCutoff() {
		return msMsCountsCutoff;
	}

	public void setMsMsCountsCutoff(double msMsCountsCutoff) {
		this.msMsCountsCutoff = msMsCountsCutoff;
	}

	public int getMaxFragmentsCutoff() {
		return maxFragmentsCutoff;
	}

	public void setMaxFragmentsCutoff(int maxFragmentsCutoff) {
		this.maxFragmentsCutoff = maxFragmentsCutoff;
	}

	public IntensityMeasure getFilterIntensityMeasure() {
		return filterIntensityMeasure;
	}

	public void setFilterIntensityMeasure(IntensityMeasure filterIntensityMeasure) {
		this.filterIntensityMeasure = filterIntensityMeasure;
	}

	public double getMsmsIsolationWindowLowerBorder() {
		return msmsIsolationWindowLowerBorder;
	}

	public void setMsmsIsolationWindowLowerBorder(double msmsIsolationWindowLowerBorder) {
		this.msmsIsolationWindowLowerBorder = msmsIsolationWindowLowerBorder;
	}

	public double getMsmsIsolationWindowUpperBorder() {
		return msmsIsolationWindowUpperBorder;
	}

	public void setMsmsIsolationWindowUpperBorder(double msmsIsolationWindowUpperBorder) {
		this.msmsIsolationWindowUpperBorder = msmsIsolationWindowUpperBorder;
	}

	public double getMsmsGroupingRtWindow() {
		return msmsGroupingRtWindow;
	}

	public void setMsmsGroupingRtWindow(double msmsGroupingRtWindow) {
		this.msmsGroupingRtWindow = msmsGroupingRtWindow;
	}

	public double getPrecursorGroupingMassError() {
		return precursorGroupingMassError;
	}

	public void setPrecursorGroupingMassError(double precursorGroupingMassError) {
		this.precursorGroupingMassError = precursorGroupingMassError;
	}

	public MassErrorType getPrecursorGroupingMassErrorType() {
		return precursorGroupingMassErrorType;
	}

	public void setPrecursorGroupingMassErrorType(MassErrorType precursorGroupingMassErrorType) {
		this.precursorGroupingMassErrorType = precursorGroupingMassErrorType;
	}

	public boolean isFlagMinorIsotopesPrecursors() {
		return flagMinorIsotopesPrecursors;
	}

	public void setFlagMinorIsotopesPrecursors(boolean flagMinorIsotopesPrecursors) {
		this.flagMinorIsotopesPrecursors = flagMinorIsotopesPrecursors;
	}

	public int getMaxPrecursorCharge() {
		return maxPrecursorCharge;
	}

	public void setMaxPrecursorCharge(int maxPrecursorCharge) {
		this.maxPrecursorCharge = maxPrecursorCharge;
	}

	public int getSmoothingFilterWidth() {
		return smoothingFilterWidth;
	}

	public void setSmoothingFilterWidth(int smoothingFilterWidth) {
		this.smoothingFilterWidth = smoothingFilterWidth;
	}

	public double getChromatogramExtractionWindow() {
		return chromatogramExtractionWindow;
	}

	public void setChromatogramExtractionWindow(double chromatogramExtractionWindow) {
		this.chromatogramExtractionWindow = chromatogramExtractionWindow;
	}
}



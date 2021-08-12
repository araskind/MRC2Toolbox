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
import java.util.Collection;

import edu.umich.med.mrc2.datoolbox.data.enums.MassErrorType;
import edu.umich.med.mrc2.datoolbox.data.enums.Polarity;
import edu.umich.med.mrc2.datoolbox.gui.plot.chromatogram.ChromatogramPlotMode;
import edu.umich.med.mrc2.datoolbox.utils.Range;
import edu.umich.med.mrc2.datoolbox.utils.filter.SavitzkyGolayWidth;

public class ChromatogramDefinition  implements Serializable, Cloneable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -8498214730121164940L;
	private ChromatogramPlotMode mode;
	private Polarity polarity;
	private int msLevel;
	private Collection<Double>mzList;
	private boolean sumAllMassChromatograms;
	private Double mzWindowValue;
	private MassErrorType massErrorType;
	private Range rtRange;
	private boolean doSmooth; 
	private SavitzkyGolayWidth filterWidth;
	
	public ChromatogramDefinition(
			ChromatogramPlotMode mode, 
			Polarity polarity, 
			int msLevel, 
			Collection<Double> mzList,
			boolean sumAllMassChromatograms, 
			Double mzWindowValue, 
			MassErrorType massErrorType, 
			Range rtRange) {
		super();
		this.mode = mode;
		this.polarity = polarity;
		this.msLevel = msLevel;
		this.mzList = mzList;
		this.sumAllMassChromatograms = sumAllMassChromatograms;
		this.mzWindowValue = mzWindowValue;
		this.massErrorType = massErrorType;
		this.rtRange = rtRange;
		this.doSmooth = false;
		this.filterWidth = SavitzkyGolayWidth.NINE;
	}
	
	public ChromatogramDefinition(
			ChromatogramPlotMode mode, 
			Polarity polarity, 
			int msLevel, 
			Collection<Double> mzList,
			boolean sumAllMassChromatograms, 
			Double mzWindowValue, 
			MassErrorType massErrorType, 
			Range rtRange, 
			boolean doSmooth, 
			SavitzkyGolayWidth filterWidth) {
		super();
		this.mode = mode;
		this.polarity = polarity;
		this.msLevel = msLevel;
		this.mzList = mzList;
		this.sumAllMassChromatograms = sumAllMassChromatograms;
		this.mzWindowValue = mzWindowValue;
		this.massErrorType = massErrorType;
		this.rtRange = rtRange;
		this.doSmooth = doSmooth;
		this.filterWidth = filterWidth;
	}
	
	public ChromatogramPlotMode getMode() {
		return mode;
	}
	public Polarity getPolarity() {
		return polarity;
	}
	public int getMsLevel() {
		return msLevel;
	}
	public Collection<Double> getMzList() {
		return mzList;
	}
	public Double getMzWindowValue() {
		return mzWindowValue;
	}
	public MassErrorType getMassErrorType() {
		return massErrorType;
	}
	public Range getRtRange() {
		return rtRange;
	}
	public boolean getSumAllMassChromatograms() {
		return sumAllMassChromatograms;
	}
	
	@Override
	public ChromatogramDefinition clone() {
		
		return new ChromatogramDefinition(mode, polarity, msLevel, mzList,
			 sumAllMassChromatograms, mzWindowValue, massErrorType, rtRange);
	}

	public boolean isDoSmooth() {
		return doSmooth;
	}

	public SavitzkyGolayWidth getFilterWidth() {
		return filterWidth;
	}
}

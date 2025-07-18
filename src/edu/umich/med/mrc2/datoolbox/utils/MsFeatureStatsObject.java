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

package edu.umich.med.mrc2.datoolbox.utils;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.stat.descriptive.rank.Percentile;

import edu.umich.med.mrc2.datoolbox.data.MassSpectrum;
import edu.umich.med.mrc2.datoolbox.data.enums.MassErrorType;

public class MsFeatureStatsObject {

	private Percentile median;
	private List<Double>rtValues;
	private List<Double>rtMinValues;
	private List<Double>rtMaxValues;
	private List<MassSpectrum>spectra;
	
	public MsFeatureStatsObject() {
		super();
		rtValues = new ArrayList<Double>();
		rtMinValues = new ArrayList<Double>();
		rtMaxValues = new ArrayList<Double>();
		spectra = new ArrayList<MassSpectrum>();
		median = new Percentile(50);
	}
	
	public void addRtValue(double rt) {
		rtValues.add(rt);
	}
	
	public void addRtMinValue(double rtMin) {
		rtMinValues.add(rtMin);
	}
	
	public void addRtMaxValue(double rtMax) {
		rtMaxValues.add(rtMax);
	}
	
	public void addRtRange(Range rtRange) {
		rtMinValues.add(rtRange.getMin());
		rtMaxValues.add(rtRange.getMax());
	}
	
	public void addSpectrum(MassSpectrum spectrum) {
		spectra.add(spectrum);
	}
	
	public double getMedianRt() {
		double[] rt = rtValues.stream().mapToDouble(d -> d).toArray();
		return median.evaluate(rt);
	}
	
	public double getMedianMinRt() {
		double[] rt = rtMinValues.stream().mapToDouble(d -> d).toArray();
		return median.evaluate(rt);
	}
	
	public double getMedianMaxRt() {
		double[] rt = rtMaxValues.stream().mapToDouble(d -> d).toArray();
		return median.evaluate(rt);
	}
	
	public Range getMedianRtRange() {
		
		double minMedian = getMedianMinRt();
		double maxMedian = getMedianMaxRt();
		if(minMedian <= maxMedian)
			return new Range(minMedian, maxMedian);
		else
			return null;		
	}
	
	public MassSpectrum getAverageScaledMassSpectrum() {		
		return MsUtils.averageMassSpectraByAdduct(spectra, 0.1d, MassErrorType.Da);
	}
}

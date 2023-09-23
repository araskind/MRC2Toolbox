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

package edu.umich.med.mrc2.datoolbox.gui.plot.dataset;

import java.util.Arrays;

import edu.umich.med.mrc2.datoolbox.data.MassSpectrum;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsMsLibraryFeature;
import edu.umich.med.mrc2.datoolbox.data.TandemMassSpectrum;
import edu.umich.med.mrc2.datoolbox.utils.Range;

public class HeadToTaleMsDataSet  extends MsDataSet {

	/**
	 *
	 */
	private static final long serialVersionUID = 6793905715141166824L;
	
	public HeadToTaleMsDataSet() {
		super();
		isNormalized = true;
	}

	public HeadToTaleMsDataSet(
			MsFeature feature, 
			MsFeature reference) {

		this();
		MassSpectrum featureSpectrum = feature.getSpectrum();
		MassSpectrum referenceSpectrum = reference.getSpectrum();

		if(featureSpectrum != null && referenceSpectrum != null) {

			msSeriesScaled.put(0, featureSpectrum.getCompleteNormalizedPattern());
			labels.put(0, feature.getName());
			msSeriesScaled.put(1, referenceSpectrum.getCompleteNormalizedPattern());
			labels.put(1, reference.getName());
			finalizeDataSet();
		}
	}

	//TandemMassSpectrum
	public HeadToTaleMsDataSet(
			TandemMassSpectrum unk, 
			TandemMassSpectrum reference) {

		this();
		if(unk != null && reference != null) {

			msSeriesScaled.put(0, unk.getNormalizedMassSortedSpectrum());
			labels.put(0, unk.getUserFriendlyId());
			msSeriesScaled.put(1, reference.getNormalizedMassSortedSpectrum());
			labels.put(1, reference.getUserFriendlyId());
			finalizeDataSet();
		}
	}

	public HeadToTaleMsDataSet(
			TandemMassSpectrum instrumentSpectrum, 
			MsMsLibraryFeature reference) {

		this();
		if(instrumentSpectrum != null && reference != null) {

			msSeriesScaled.put(0, instrumentSpectrum.getNormalizedMassSortedSpectrum());
			labels.put(0, instrumentSpectrum.getUserFriendlyId());
			msSeriesScaled.put(1, reference.getNormalizedMassSortedSpectrum());
			labels.put(1, reference.getUserFriendlyId());
			finalizeDataSet();
		}
	}
	
	private void finalizeDataSet() {
		
		allPointsScaled.addAll(Arrays.asList(msSeriesScaled.get(0)));
		allPointsScaled.addAll(Arrays.asList(msSeriesScaled.get(1)));
		
		if(allPointsScaled.isEmpty())
			return;
		
		double[]mzArray = 
				allPointsScaled.stream().mapToDouble(p -> p.getMz()).
				sorted().toArray();
		massRange = 
				new Range(mzArray[0], mzArray[mzArray.length - 1]);
		
		double[]intensityArrayScaled = 
				allPointsScaled.stream().mapToDouble(p -> p.getIntensity()).
				sorted().toArray();
		intensityRangeScaled = 
				new Range(intensityArrayScaled[0], 
						intensityArrayScaled[intensityArrayScaled.length - 1]);
	}

	@Override
	public Range getIntensityRange() {
		return intensityRangeScaled;
	}

	@Override
	public int getSeriesCount() {
		return msSeriesScaled.size();
	}
	
	@Override
	public Number getX(int series, int item) {
		return msSeriesScaled.get(series)[item].getMz();
	}

	@Override
	public Number getY(int series, int item) {

		if(series == 0)
			return msSeriesScaled.get(0)[item].getIntensity();
		else if(series == 1)
			return msSeriesScaled.get(1)[item].getIntensity() * -1.0d;
		else
			return 0.0d;
	}

	@Override
	public int getItemCount(int series) {
		return msSeriesScaled.get(series).length;
	}
	
	@Override
	public void setNormalized(boolean isNormalized) {
		//	This data set is always normalized
	}
}

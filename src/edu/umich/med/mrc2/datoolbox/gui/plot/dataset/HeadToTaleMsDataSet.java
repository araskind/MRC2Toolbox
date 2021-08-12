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

import java.util.HashMap;
import java.util.Map.Entry;

import org.jfree.data.xy.AbstractXYDataset;
import org.jfree.data.xy.IntervalXYDataset;

import edu.umich.med.mrc2.datoolbox.data.MassSpectrum;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsMsLibraryFeature;
import edu.umich.med.mrc2.datoolbox.data.MsPoint;
import edu.umich.med.mrc2.datoolbox.data.TandemMassSpectrum;
import edu.umich.med.mrc2.datoolbox.utils.Range;

public class HeadToTaleMsDataSet  extends AbstractXYDataset implements IntervalXYDataset {

	/**
	 *
	 */
	private static final long serialVersionUID = 6793905715141166824L;
	private HashMap<Integer, MsPoint[]> msSeries;
	private HashMap<Integer, String> labels;

	public HeadToTaleMsDataSet(MsFeature feature, MsFeature reference) {

		msSeries = new HashMap<Integer, MsPoint[]>();
		labels = new HashMap<Integer, String>();

		MassSpectrum featureSpectrum = feature.getSpectrum();
		MassSpectrum referenceSpectrum = reference.getSpectrum();

		if(featureSpectrum != null && referenceSpectrum != null) {

			msSeries.put(0, featureSpectrum.getCompleteNormalizedPattern());
			labels.put(0, feature.getName());
			msSeries.put(1, referenceSpectrum.getCompleteNormalizedPattern());
			labels.put(1, reference.getName());
		}
	}

	//TandemMassSpectrum
	public HeadToTaleMsDataSet(TandemMassSpectrum unk, TandemMassSpectrum reference) {

		msSeries = new HashMap<Integer, MsPoint[]>();
		labels = new HashMap<Integer, String>();

		if(unk != null && reference != null) {

			msSeries.put(0, unk.getNormalizedMassSortedSpectrum());
			labels.put(0, unk.getUserFriendlyId());
			msSeries.put(1, reference.getNormalizedMassSortedSpectrum());
			labels.put(1, reference.getUserFriendlyId());
		}
	}

	public HeadToTaleMsDataSet(TandemMassSpectrum instrumentSpectrum, MsMsLibraryFeature reference) {

		msSeries = new HashMap<Integer, MsPoint[]>();
		labels = new HashMap<Integer, String>();

		if(instrumentSpectrum != null && reference != null) {

			msSeries.put(0, instrumentSpectrum.getNormalizedMassSortedSpectrum());
			labels.put(0, instrumentSpectrum.getUserFriendlyId());
			msSeries.put(1, reference.getNormalizedMassSortedSpectrum());
			labels.put(1, reference.getUserFriendlyId());
		}
	}

	public Range getMassRange() {

		Range massRange = new Range(0.0d);
		for (Entry<Integer, MsPoint[]> entry : msSeries.entrySet()) {

			for(MsPoint p : entry.getValue()) {

				if (massRange.getAverage() == 0d)
					massRange = new Range(p.getMz());
				else
					massRange.extendRange(p.getMz());
			}
		}
		return massRange;
	}

	@Override
	public int getSeriesCount() {
		return msSeries.size();
	}

	@Override
	public Comparable<?> getSeriesKey(int series) {
		return labels.get(series);
	}

	public Number getStartX(int series, int item) {
		return getX(series, item).doubleValue();
	}

	public double getStartXValue(int series, int item) {
		return getX(series, item).doubleValue();
	}

	public Number getStartY(int series, int item) {
		return getY(series, item);
	}

	public double getStartYValue(int series, int item) {
		return getYValue(series, item);
	}

	public Number getX(int series, int item) {
		return msSeries.get(series)[item].getMz();
	}

	public Number getY(int series, int item) {

		if(series == 0)
			return msSeries.get(0)[item].getIntensity();
		else if(series == 1)
			return msSeries.get(1)[item].getIntensity() * -1.0d;
		else
			return 0.0d;
	}

	@Override
	public int getItemCount(int series) {
		return msSeries.get(series).length;
	}

	@Override
	public Number getEndX(int series, int item) {
		return getX(series, item).doubleValue();
	}

	@Override
	public double getEndXValue(int series, int item) {
		return getX(series, item).doubleValue();
	}

	@Override
	public Number getEndY(int series, int item) {
		return getY(series, item);
	}

	@Override
	public double getEndYValue(int series, int item) {
		return getYValue(series, item);
	}
}

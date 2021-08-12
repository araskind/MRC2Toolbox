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
import java.util.ArrayList;
import java.util.Arrays;

import edu.umich.med.mrc2.datoolbox.data.compare.MsDataPointComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.enums.Polarity;

public class SimpleMs implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = -3471842324811682740L;
	protected ArrayList<MsPoint> spectrumPoints;
	private MsPoint basePeak, monoisotopicPeak;
	private int absCharge;
	private Polarity polarity;

	public SimpleMs() {

		spectrumPoints = new ArrayList<MsPoint>();
	}

	public SimpleMs(ArrayList<MsPoint> msPoints) {

		spectrumPoints = new ArrayList<MsPoint>();

		for (MsPoint point : msPoints)
			spectrumPoints.add(point);

		finalizeSpectrum();
	}

	public SimpleMs(ArrayList<MsPoint> msPoints, Polarity polarity) {

		spectrumPoints = new ArrayList<MsPoint>();

		for (MsPoint point : msPoints)
			spectrumPoints.add(point);

		this.polarity = polarity;

		finalizeSpectrum();
	}

	public SimpleMs(MsPoint[] msPoints) {

		spectrumPoints = new ArrayList<MsPoint>();

		for (MsPoint point : msPoints)
			spectrumPoints.add(point);

		finalizeSpectrum();
	}

	public SimpleMs(MsPoint[] msPoints, Polarity polarity) {

		spectrumPoints = new ArrayList<MsPoint>();

		for (MsPoint point : msPoints)
			spectrumPoints.add(point);

		this.polarity = polarity;

		finalizeSpectrum();
	}

	public SimpleMs(Polarity polarity) {

		this.polarity = polarity;
		spectrumPoints = new ArrayList<MsPoint>();
	}

	public SimpleMs(String mppCompositeSpectrum) {

		spectrumPoints = new ArrayList<MsPoint>();

		String css = mppCompositeSpectrum.trim();

		if (css.length() > 0) {
			String[] pair;
			String[] pairs = css.substring(1, css.length() - 1).split("[)(]+");

			for (String token : pairs) {

				pair = token.split("[,]+");

				MsPoint point = new MsPoint(Double.parseDouble(pair[0].trim()), Double.parseDouble(pair[1].trim()));
				spectrumPoints.add(point);
			}
			finalizeSpectrum();
		}
	}

	public void finalizeSpectrum() {

		double maxIntensity = 0.0d;
		double diff = 0;

		for (MsPoint point : spectrumPoints) {

			if (point.getIntensity() > maxIntensity) {
				maxIntensity = point.getIntensity();
				basePeak = point;
			}
		}
		MsPoint[] sorted = spectrumPoints.toArray(new MsPoint[spectrumPoints.size()]);
		Arrays.sort(sorted, new MsDataPointComparator(SortProperty.MZ));
		monoisotopicPeak = sorted[0];

		if (spectrumPoints.size() > 1) {

			diff = sorted[1].getMz() - sorted[0].getMz();

			if (diff > 0.45 && diff < 0.55)
				absCharge = 2;
			else if (diff > 0.3 && diff < 0.36)
				absCharge = 3;
			else
				absCharge = 1;
		} else {
			absCharge = 1;
		}
	}

	public int getAbsCharge() {

		return absCharge;
	}

	public MsPoint getBasePeak() {

		return basePeak;
	}

	public MsPoint[] getDataPoints() {

		return spectrumPoints.toArray(new MsPoint[spectrumPoints.size()]);
	}

	public MsPoint getMonoisotopicPeak() {

		return monoisotopicPeak;
	}

	public Polarity getPolarity() {
		return polarity;
	}

	public double getSingleChargeMz() {

		return monoisotopicPeak.getMz() * absCharge;
	}

	public void setPolarity(Polarity polarity) {
		this.polarity = polarity;
	}

}

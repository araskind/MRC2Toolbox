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

import java.util.ArrayList;
import java.util.Collection;
import java.util.DoubleSummaryStatistics;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.jfree.data.xy.AbstractXYDataset;
import org.jfree.data.xy.IntervalXYDataset;
import org.openscience.cdk.formula.IsotopePattern;

import edu.umich.med.mrc2.datoolbox.data.Adduct;
import edu.umich.med.mrc2.datoolbox.data.AverageMassSpectrum;
import edu.umich.med.mrc2.datoolbox.data.LibraryMsFeature;
import edu.umich.med.mrc2.datoolbox.data.MassSpectrum;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureCluster;
import edu.umich.med.mrc2.datoolbox.data.MsMsCluster;
import edu.umich.med.mrc2.datoolbox.data.MsPoint;
import edu.umich.med.mrc2.datoolbox.data.SimpleMsMs;
import edu.umich.med.mrc2.datoolbox.data.TandemMassSpectrum;
import edu.umich.med.mrc2.datoolbox.utils.MsUtils;
import edu.umich.med.mrc2.datoolbox.utils.Range;

public class MsDataSet extends AbstractXYDataset implements IntervalXYDataset {

	/**
	 *
	 */
	private static final long serialVersionUID = -3102688118765527115L;
	private Map<Integer, MsPoint[]> msSeries;
	private Map<Integer, String> labels;
	private double topIntensity;
	private ArrayList<MsPoint> allPoints;
	private Range massRange;
	private Object spectrumSource;

	public MsDataSet(LibraryMsFeature lt, boolean scaleMs) {
		createDataSetFromLibraryTarget(lt, scaleMs);
	}

	public MsDataSet(Collection<MsFeature> featureList, boolean scaleMs) {
		spectrumSource = featureList;
		createDataSetFromFeatureCollection(featureList, scaleMs);
	}

	public MsDataSet(List<SimpleMsMs> selectedFeatures, boolean scaleMs) {
		spectrumSource = selectedFeatures;
		createDataSetFromMsCollection(selectedFeatures, scaleMs);
	}

	public MsDataSet(MsFeature selectedFeature, boolean scaleMs) {

		spectrumSource = selectedFeature;
		
		if(selectedFeature.getClass().equals(MsFeature.class))
			createDataSetFromCompoundFeature(selectedFeature, scaleMs);

		if(selectedFeature.getClass().equals(LibraryMsFeature.class))
			createDataSetFromLibraryTarget((LibraryMsFeature) selectedFeature, scaleMs);
	}

	public MsDataSet(MsFeatureCluster selectedCluster, boolean scaleMs) {
		spectrumSource = selectedCluster;
		createDataSetFromFeatureCollection(selectedCluster.getFeatures(), scaleMs);
	}

	public MsDataSet(MsMsCluster selectedCluster, boolean scaleMs) {
		createDataSetFromMsCollection(selectedCluster.getClusterFeatures(), scaleMs);
	}

	public MsDataSet(
			Collection<MsPoint> featurePoints,
			Collection<MsPoint> libraryPoints,
			String featureLabel,
			String libraryLabel,
			boolean scale) {

		spectrumSource = featurePoints;
		createHeadToTailDataSet(featurePoints, libraryPoints, featureLabel, libraryLabel, scale);
	}

	public MsDataSet(IsotopePattern isoPattern) {

		spectrumSource = isoPattern;
		msSeries = new TreeMap<Integer, MsPoint[]>();
		labels = new TreeMap<Integer, String>();
		massRange = new Range(0d);
		allPoints = new ArrayList<MsPoint>();
		topIntensity = 0;

		MsPoint[] points = new MsPoint[isoPattern.getNumberOfIsotopes()];

		for (int i = 0; i < isoPattern.getNumberOfIsotopes(); i++)
			points[i] = new MsPoint(isoPattern.getIsotope(i).getMass(), isoPattern.getIsotope(i).getIntensity() * 100);

		msSeries.put(0, points);
		labels.put(0, "Predicted spectrum");

		for (MsPoint p : points) {

			allPoints.add(p);

			if (p.getIntensity() > topIntensity)
				topIntensity = p.getIntensity();

			if (massRange.getAverage() == 0d)
				massRange = new Range(p.getMz());
			else
				massRange.extendRange(p.getMz());
		}
	}

	public MsDataSet(TandemMassSpectrum msms) {

		spectrumSource = msms;
		msSeries = new TreeMap<Integer, MsPoint[]>();
		labels = new TreeMap<Integer, String>();
		massRange = new Range(0d);
		allPoints = new ArrayList<MsPoint>();
		topIntensity = 0;

		MsPoint[] points = msms.getMassSortedSpectrum();

		msSeries.put(0, points);
		labels.put(0, msms.getUserFriendlyId());

		for (MsPoint p : points) {

			allPoints.add(p);

			if (p.getIntensity() > topIntensity)
				topIntensity = p.getIntensity();

			if (massRange.getAverage() == 0d)
				massRange = new Range(p.getMz());
			else
				massRange.extendRange(p.getMz());
		}
	}

	public MsDataSet(Collection<MsPoint> msPoints) {

		spectrumSource = msPoints;
		msSeries = new TreeMap<Integer, MsPoint[]>();
		labels = new TreeMap<Integer, String>();
		massRange = new Range(0d);
		allPoints = new ArrayList<MsPoint>();
		topIntensity = 0;

		msSeries.put(0, msPoints.toArray(new MsPoint[msPoints.size()]));
		labels.put(0, "Predicted spectrum");

		for (MsPoint p : msSeries.get(0)) {

			allPoints.add(p);

			if (p.getIntensity() > topIntensity)
				topIntensity = p.getIntensity();

			if (massRange.getAverage() == 0d)
				massRange = new Range(p.getMz());
			else
				massRange.extendRange(p.getMz());
		}
	}

	public MsDataSet(Collection<MsPoint> msPoints, boolean scale, String label) {

		spectrumSource = msPoints;
		msSeries = new HashMap<Integer, MsPoint[]>();
		labels = new HashMap<Integer, String>();
		massRange = null;
		allPoints = new ArrayList<MsPoint>();
		topIntensity = 0;

		MsPoint[] scaledPattern = msPoints.toArray(new MsPoint[msPoints.size()]);
		if(scale)
			scaledPattern = MsUtils.normalizeAndSortMsPattern(msPoints);

		msSeries.put(0, scaledPattern);
		labels.put(0, label);

		for (MsPoint p : scaledPattern) {

			allPoints.add(p);

			if (p.getIntensity() > topIntensity)
				topIntensity = p.getIntensity();

			if (massRange == null)
				massRange = new Range(p.getMz());
			else
				massRange.extendRange(p.getMz());
		}
	}

	public MsDataSet(AverageMassSpectrum averageMassSpectrum) {
		
		spectrumSource = averageMassSpectrum;
		msSeries = new TreeMap<Integer, MsPoint[]>();
		labels = new TreeMap<Integer, String>();
		massRange = new Range(0d);
		allPoints = new ArrayList<MsPoint>();
		topIntensity = 0;
		
		msSeries.put(0, averageMassSpectrum.getMasSpectrum().getCompletePattern());
		labels.put(0, averageMassSpectrum.toString());

		for (MsPoint p : msSeries.get(0)) {

			allPoints.add(p);

			if (p.getIntensity() > topIntensity)
				topIntensity = p.getIntensity();

			if (massRange.getAverage() == 0d)
				massRange = new Range(p.getMz());
			else
				massRange.extendRange(p.getMz());
		}
	}

	private void createDataSetFromCompoundFeature(MsFeature feature, boolean scaleMs) {

		ArrayList<MsFeature>features = new ArrayList<MsFeature>();
		features.add(feature);
		createDataSetFromFeatureCollection(features, scaleMs);
	}

	private void createDataSetFromFeatureCollection(Collection<MsFeature> featureCollection, boolean scaleMs) {

		msSeries = new HashMap<Integer, MsPoint[]>();
		labels = new HashMap<Integer, String>();
		massRange = new Range(0d);

		allPoints = new ArrayList<MsPoint>();
		int featureCount = 0;
		topIntensity = 0;

		for (MsFeature cf : featureCollection) {

			if (cf.getSpectrum() == null)
				continue;

			MsPoint[] libPattern = cf.getSpectrum().getCompletePattern(scaleMs);
			msSeries.put(featureCount, libPattern);
			String fName = cf.getName();
			if(cf instanceof LibraryMsFeature)
				fName += " - library";

			//	TODO show scan #
//			if(cf.getSpectrum() != null && cf.getSpectrum().getS)
			
			labels.put(featureCount, fName);
			for (MsPoint p : libPattern) {

				allPoints.add(p);
				if (p.getIntensity() > topIntensity)
					topIntensity = p.getIntensity();

				if (massRange.getAverage() == 0d)
					massRange = new Range(p.getMz());
				else
					massRange.extendRange(p.getMz());
			}
			featureCount++;
		}
	}

	private void createDataSetFromLibraryTarget(LibraryMsFeature selectedTarget, boolean scaleMs) {

		msSeries = new HashMap<Integer, MsPoint[]>();
		labels = new HashMap<Integer, String>();
		massRange = new Range(0d);

		allPoints = new ArrayList<MsPoint>();
		int featureCount = 0;
		topIntensity = 0;
		if(selectedTarget.getSpectrum() == null)
			return;

		MassSpectrum spectrum = selectedTarget.getSpectrum();
		for(Adduct ad : spectrum.getAdducts()){

			MsPoint[] ms = spectrum.getMsForAdduct(ad, scaleMs);
			if(ms == null)
				continue;

			msSeries.put(featureCount, ms);
			labels.put(featureCount, ad.getName());

			for (MsPoint p : ms) {

				allPoints.add(p);

				if (p.getIntensity() > topIntensity)
					topIntensity = p.getIntensity();

				if (massRange.getAverage() == 0d)
					massRange = new Range(p.getMz());
				else
					massRange.extendRange(p.getMz());
			}
			featureCount++;
		}
	}

	private void createDataSetFromMsCollection(Collection<SimpleMsMs> featureCollection, boolean scaleMs) {

		msSeries = new HashMap<Integer, MsPoint[]>();
		labels = new HashMap<Integer, String>();
		massRange = new Range(0d);

		allPoints = new ArrayList<MsPoint>();
		int featureCount = 0;
		topIntensity = 0;

		for (SimpleMsMs ms : featureCollection) {

			msSeries.put(featureCount, ms.getDataPoints());
			labels.put(featureCount, ms.getTitle());

			for (MsPoint p : ms.getDataPoints()) {

				allPoints.add(p);

				if (p.getIntensity() > topIntensity)
					topIntensity = p.getIntensity();

				if (massRange.getAverage() == 0d)
					massRange = new Range(p.getMz());
				else
					massRange.extendRange(p.getMz());
			}
			featureCount++;
		}
	}

	public void createHeadToTailDataSet(
			Collection<MsPoint> featurePoints,
			Collection<MsPoint> libraryPoints,
			String featureLabel,
			String libraryLabel,
			boolean scale) {

		msSeries = new TreeMap<Integer, MsPoint[]>();
		labels = new TreeMap<Integer, String>();
		massRange = new Range(0d);
		allPoints = new ArrayList<MsPoint>();
		topIntensity = 0;

		Collection<MsPoint> featurePointsScaled = new ArrayList<MsPoint>();
		featurePointsScaled.addAll(featurePoints);
		if(scale) {
			double fMax = featurePoints.stream().mapToDouble(p -> p.getIntensity()).max().getAsDouble();
			double featureScalingCoeff = 100.0d / fMax;
			featurePointsScaled.clear();
			featurePoints.stream().forEach(p -> featurePointsScaled.add(new MsPoint(p.getMz(), p.getIntensity() * featureScalingCoeff)));
		}
		allPoints.addAll(featurePointsScaled);
		msSeries.put(0, featurePointsScaled.toArray(new MsPoint[featurePointsScaled.size()]));
		labels.put(0, featureLabel);

		Collection<MsPoint> libraryPointsInvertedScaled = new ArrayList<MsPoint>();
		libraryPoints.stream().forEach(p -> libraryPointsInvertedScaled.add(new MsPoint(p.getMz(), -p.getIntensity())));
		if(scale) {
			double fMax = libraryPoints.stream().mapToDouble(p -> p.getIntensity()).max().getAsDouble();
			double featureScalingCoeff = 100.0d / fMax;
			libraryPointsInvertedScaled.clear();
			libraryPoints.stream().forEach(p -> libraryPointsInvertedScaled.add(
					new MsPoint(p.getMz(), -p.getIntensity() * featureScalingCoeff)));
		}
		allPoints.addAll(libraryPointsInvertedScaled);
		msSeries.put(1, libraryPointsInvertedScaled.toArray(new MsPoint[libraryPointsInvertedScaled.size()]));
		labels.put(1, libraryLabel);

		DoubleSummaryStatistics mzStats = allPoints.stream().mapToDouble(p -> p.getMz()).summaryStatistics();
		massRange = new Range(mzStats.getMin(), mzStats.getMax());
		topIntensity = allPoints.stream().mapToDouble(p -> p.getIntensity()).max().getAsDouble();
	}

	public Number getEndX(int series, int item) {
		return getX(series, item).doubleValue();
	}

	public double getEndXValue(int series, int item) {
		return getX(series, item).doubleValue();
	}

	public Number getEndY(int series, int item) {
		return getY(series, item);
	}

	public double getEndYValue(int series, int item) {
		return getYValue(series, item);
	}
	
	public double getHighestIntensityInRange(org.jfree.data.Range massRange) {

		double top = 0.001;
		for (MsPoint p : allPoints) {

			if (massRange.contains(p.getMz()) && p.getIntensity() > top)
				top = p.getIntensity();
		}
		return top;
	}

	public double getHighestIntensityInRange(Range massRange) {

		double top = 0.001;
		for (MsPoint p : allPoints) {

			if (massRange.contains(p.getMz()) && p.getIntensity() > top)
				top = p.getIntensity();
		}
		return top;
	}

	public int getItemCount(int series) {
		return msSeries.get(series).length;
	}

	public Range getMassRange() {

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
		return msSeries.get(series)[item].getIntensity();
	}

	public Object getSpectrumSource() {
		return spectrumSource;
	}
}

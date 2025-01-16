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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

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
import edu.umich.med.mrc2.datoolbox.data.SimpleMsFeature;
import edu.umich.med.mrc2.datoolbox.data.SimpleMsMs;
import edu.umich.med.mrc2.datoolbox.data.TandemMassSpectrum;
import edu.umich.med.mrc2.datoolbox.utils.MsUtils;
import edu.umich.med.mrc2.datoolbox.utils.Range;
import edu.umich.med.mrc2.datoolbox.utils.RawDataUtils;
import umich.ms.datatypes.scan.IScan;

public class MsDataSet extends AbstractXYDataset implements IntervalXYDataset {

	/**
	 *
	 */
	private static final long serialVersionUID = -3102688118765527115L;
	protected Map<Integer, MsPoint[]> msSeries;
	protected Map<Integer, MsPoint[]> msSeriesScaled;
	protected boolean isNormalized;
	protected Map<Integer, String> labels;
	protected Collection<MsPoint> allPoints;
	protected Collection<MsPoint> allPointsScaled;
	protected Range massRange;
	protected Range intensityRange;
	protected Range intensityRangeScaled;
	protected Object spectrumSource;
	
	public MsDataSet() {
		
		super();
		initFields();
	}

	public MsDataSet(LibraryMsFeature lt) {
		this();
		createDataSetFromLibraryTarget(lt);
	}

	public MsDataSet(Collection<MsFeature> featureList) {
		
		this();
		spectrumSource = featureList;
		createDataSetFromFeatureCollection(featureList);
	}

	public MsDataSet(List<SimpleMsMs> selectedFeatures) {
		
		this();
		spectrumSource = selectedFeatures;
		createDataSetFromSimpleMSMSCollection(selectedFeatures);
	}

	public MsDataSet(MsFeature selectedFeature) {

		this();
		spectrumSource = selectedFeature;
		
		if(selectedFeature.getClass().equals(MsFeature.class))
			createDataSetFromCompoundFeature(selectedFeature);

		if(selectedFeature.getClass().equals(LibraryMsFeature.class))
			createDataSetFromLibraryTarget((LibraryMsFeature) selectedFeature);
	}

	public MsDataSet(MsFeatureCluster selectedCluster) {
		
		spectrumSource = selectedCluster;
		createDataSetFromFeatureCollection(selectedCluster.getFeatures());
	}

	public MsDataSet(MsMsCluster selectedCluster) {
		createDataSetFromSimpleMSMSCollection(selectedCluster.getClusterFeatures());
	}

	//	HeadToTail
	public MsDataSet(
			Collection<MsPoint> featurePoints,
			Collection<MsPoint> libraryPoints,
			String featureLabel,
			String libraryLabel) {

		spectrumSource = featurePoints;
		createHeadToTailDataSet(
				featurePoints, libraryPoints, featureLabel, libraryLabel);
	}

	public MsDataSet(IsotopePattern isoPattern) {
		
		this();
		spectrumSource = isoPattern;
		MsPoint[] points = new MsPoint[isoPattern.getNumberOfIsotopes()];

		for (int i = 0; i < isoPattern.getNumberOfIsotopes(); i++) {
			
			points[i] = new MsPoint(isoPattern.getIsotope(i).getMass(), 
					isoPattern.getIsotope(i).getIntensity() * 100);
			
			allPoints.add(points[i]);
		}
		msSeries.put(0, points);
		labels.put(0, "Predicted spectrum");	
		createNormalizedData();
		createDataRanges();
	}

	public MsDataSet(TandemMassSpectrum msms) {

		this();
		spectrumSource = msms;

		MsPoint[] points = msms.getMassSortedSpectrum();
		msSeries.put(0, points);
		labels.put(0, msms.getUserFriendlyId());
		allPoints.addAll(msms.getSpectrum());
		createNormalizedData();
		createDataRanges();
	}

	public MsDataSet(Set<MsPoint> msPoints) {

		this();
		spectrumSource = msPoints;
		MsPoint[] points = msPoints.toArray(new MsPoint[msPoints.size()]);
		msSeries.put(0, points);
		msSeriesScaled.put(0, MsUtils.normalizeAndSortMsPattern(points));
		labels.put(0, "Predicted spectrum");
		allPoints.addAll(msPoints);
		allPointsScaled.addAll(Arrays.asList(msSeriesScaled.get(0)));
		createDataRanges();
	}

	public MsDataSet(Set<MsPoint> msPoints, String label) {

		this(msPoints);
		labels.put(0, label);
	}

	public MsDataSet(AverageMassSpectrum averageMassSpectrum) {
		
		this();
		spectrumSource = averageMassSpectrum;	
		MsPoint[] points = averageMassSpectrum.getMasSpectrum().getCompletePattern();
		msSeries.put(0, points);
		labels.put(0, averageMassSpectrum.toString());
		allPoints.addAll(averageMassSpectrum.getMasSpectrum().getMsPoints());
		createNormalizedData();
		createDataRanges();
	}
	
	public MsDataSet(IScan s) {
		
		this();
		spectrumSource = s;
		Collection<MsPoint> msPoints = RawDataUtils.getScanPoints(s, 0.0d);
		MsPoint[] points = msPoints.toArray(new MsPoint[msPoints.size()]);
		msSeries.put(0, points);
		msSeriesScaled.put(0, MsUtils.normalizeAndSortMsPattern(points));
		labels.put(0, RawDataUtils.getScanLabel(s));
		allPoints.addAll(msPoints);
		allPointsScaled.addAll(Arrays.asList(msSeriesScaled.get(0)));
		createDataRanges();
	}
	
	private void initFields() {
		
		msSeries = new TreeMap<Integer, MsPoint[]>();
		msSeriesScaled = new TreeMap<Integer, MsPoint[]>();
		labels = new TreeMap<Integer, String>();
		massRange = new Range(0d);
		allPoints = new TreeSet<MsPoint>(MsUtils.mzSorter);
		allPointsScaled = new TreeSet<MsPoint>(MsUtils.mzSorter);
		isNormalized = false;
	}

	public void createDataSetFromCompoundFeature(MsFeature feature) {
		createDataSetFromFeatureCollection(Collections.singleton(feature));
	}

	public void createDataSetFromFeatureCollection(
			Collection<MsFeature> featureCollection) {

		int featureCount = 0;
		spectrumSource = featureCollection;
		for (MsFeature cf : featureCollection) {

			if (cf.getSpectrum() == null)
				continue;

			MsPoint[] libPattern = cf.getSpectrum().getCompletePattern(false);
			msSeries.put(featureCount, libPattern);
			String fName = Integer.toString(featureCount + 1) + " - " + cf.getName();
			if(cf instanceof LibraryMsFeature)
				fName += " - library";
			
			labels.put(featureCount, fName);			
			allPoints.addAll(cf.getSpectrum().getMsPoints());
			featureCount++;
		}
		createNormalizedData();
		createDataRanges();
	}
	
	public void createDataSetFromSimpleMsFeature(SimpleMsFeature feature) {
		
		createDataSetFromSimpleMsFeatureCollection(Collections.singleton(feature));
	}
	
	public void createDataSetFromSimpleMsFeatureCollection(
			Collection<SimpleMsFeature> featureCollection) {

		int featureCount = 0;
		spectrumSource = featureCollection;
		for (SimpleMsFeature cf : featureCollection) {

			if (cf.getObservedSpectrum() == null)
				continue;

			MsPoint[] libPattern = cf.getObservedSpectrum().getCompletePattern(false);
			msSeries.put(featureCount, libPattern);
			String fName = Integer.toString(featureCount + 1) + " - " + cf.getName();
			
			labels.put(featureCount, fName);			
			allPoints.addAll(cf.getObservedSpectrum().getMsPoints());
			featureCount++;
		}
		createNormalizedData();
		createDataRanges();
	}
	

	public void createDataSetFromLibraryTarget(
			LibraryMsFeature selectedTarget) {
		
		int featureCount = 0;
		if(selectedTarget.getSpectrum() == null)
			return;
		
		spectrumSource = selectedTarget;

		MassSpectrum spectrum = selectedTarget.getSpectrum();
		for(Adduct ad : spectrum.getAdducts()){

			MsPoint[] ms = spectrum.getMsForAdduct(ad, false);
			if(ms == null || ms.length == 0)
				continue;
			
			MsPoint[] msScaled = spectrum.getMsForAdduct(ad, true);
			msSeries.put(featureCount, msScaled);			
			labels.put(featureCount, ad.getName());
			allPoints.addAll(spectrum.getMsPoints());
			featureCount++;
		}
		createNormalizedData();
		createDataRanges();
	}

	public void createDataSetFromSimpleMSMSCollection(
			Collection<SimpleMsMs> featureCollection) {

		int featureCount = 0;
		spectrumSource = featureCollection;
		
		for (SimpleMsMs ms : featureCollection) {

			msSeries.put(featureCount, ms.getDataPoints());
			labels.put(featureCount, ms.getTitle());
			allPoints.addAll(ms.getSpectrumPoints());			
			featureCount++;
		}
		createNormalizedData();
		createDataRanges();
	}
	
	public void createHeadToTailDataSet(
			Collection<MsPoint> featurePoints,
			Collection<MsPoint> libraryPoints,
			String featureLabel,
			String libraryLabel) {
		
		//	Feature 
		spectrumSource = featurePoints;
		MsPoint[] fPoints = featurePoints.toArray(new MsPoint[featurePoints.size()]);
		msSeries.put(0, fPoints);
		labels.put(0, featureLabel);
		allPoints.addAll(featurePoints);
		
		//	Library
		Set<MsPoint> libraryPointsInverted = new TreeSet<MsPoint>(MsUtils.mzSorter);
		libraryPoints.stream().forEach(
				p -> libraryPointsInverted.add(new MsPoint(p.getMz(), p.getIntensity() * -1.0d)));
		MsPoint[] libPoints = 
				libraryPointsInverted.toArray(new MsPoint[libraryPointsInverted.size()]);
		msSeries.put(1, libPoints);
		labels.put(1, libraryLabel);
		allPoints.addAll(libraryPoints);
		
		createNormalizedData();
		createDataRanges();
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
		return getHighestIntensityInRange(new Range(massRange));
	}

	public double getHighestIntensityInRange(Range massRange) {

		MsPoint topPoint = null;
		if(isNormalized) {
			topPoint = allPointsScaled.stream().
					filter(p -> massRange.contains(p.getMz())).
					sorted(MsUtils.reverseIntensitySorter).
					findFirst().orElse(null);
		}
		else {
			topPoint = allPoints.stream().
					filter(p -> massRange.contains(p.getMz())).
					sorted(MsUtils.reverseIntensitySorter).
					findFirst().orElse(null);
		}
		if(topPoint != null)
			return topPoint.getIntensity();
		else
			return 0.001;
	}
	
	protected void createDataRanges() {
		
		if(allPoints.isEmpty())
			return;
		
		double[]mzArray = 
				allPoints.stream().mapToDouble(p -> p.getMz()).
				sorted().toArray();
		massRange = 
				new Range(mzArray[0], mzArray[mzArray.length - 1]);
		
		double[]intensityArray = 
				allPoints.stream().mapToDouble(p -> p.getIntensity()).
				sorted().toArray();
		intensityRange = 
				new Range(intensityArray[0], intensityArray[intensityArray.length - 1]);
		
		double[]intensityArrayScaled = 
				allPointsScaled.stream().mapToDouble(p -> p.getIntensity()).
				sorted().toArray();
		intensityRangeScaled = 
				new Range(intensityArrayScaled[0], 
						intensityArrayScaled[intensityArrayScaled.length - 1]);
	}
	
	protected void createNormalizedData() {
		
		if(allPoints.isEmpty())
			return;
		
		double max = allPoints.stream().
				mapToDouble(p -> p.getIntensity()).max().getAsDouble();
		for(int i=0; i<msSeries.size(); i++) {
			
			MsPoint[]seriesPoints = msSeries.get(i);
			MsPoint[]seriesPointsNorm = new MsPoint[seriesPoints.length];
			for(int j=0; j<seriesPoints.length; j++) {
				seriesPointsNorm[j] =
						new MsPoint(seriesPoints[j].getMz(), 
								seriesPoints[j].getIntensity() / max 
								* MsUtils.SPECTRUM_NORMALIZATION_BASE_INTENSITY);
				allPointsScaled.add(seriesPointsNorm[j]);
			}
			msSeriesScaled.put(i, seriesPointsNorm);
		}
	}

	public int getItemCount(int series) {
		return msSeries.get(series).length;
	}

	public Range getMassRange() {
		return massRange;
	}
	
	public Range getIntensityRange() {
		
		if(isNormalized)
			return intensityRangeScaled;
		else
			return intensityRange;
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
		
		if(isNormalized)
			return msSeriesScaled.get(series)[item].getIntensity();
		else
			return msSeries.get(series)[item].getIntensity();
	}

	public Object getSpectrumSource() {
		return spectrumSource;
	}

	public boolean normalized() {
		return isNormalized;
	}

	public void setNormalized(boolean isNormalized) {
		this.isNormalized = isNormalized;		
	}
	
	public void fireDatasetChanged() {
		super.fireDatasetChanged();
	}
}

/*******************************************************************************
 *
 * (C) Copyright 2018-2025 MRC2 (http://mrc2.umich.edu).
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
import java.util.TreeSet;

import org.apache.commons.jcs3.access.exception.InvalidArgumentException;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import edu.umich.med.mrc2.datoolbox.data.MsMsLibraryFeature;
import edu.umich.med.mrc2.datoolbox.data.MsPlotDataObject;
import edu.umich.med.mrc2.datoolbox.data.MsPoint;
import edu.umich.med.mrc2.datoolbox.data.TandemMassSpectrum;
import edu.umich.med.mrc2.datoolbox.utils.MsUtils;
import edu.umich.med.mrc2.datoolbox.utils.Range;

public class HeadToTailMsDataSet  extends MsDataSet {

	/**
	 *
	 */
	private static final long serialVersionUID = 6793905715141166824L;

	private XYSeriesCollection parentIonDataSet;
	
	public HeadToTailMsDataSet() {
		super();
		isNormalized = true;
	}
	
	public HeadToTailMsDataSet(
			MsPlotDataObject unkData, 
			MsPlotDataObject refData) {
		
		this();
		if(unkData == null || refData == null)
			throw new InvalidArgumentException(
					"Unknown and reference must not be NULL!");
		
		createDataSet(unkData, refData);
	}

	public HeadToTailMsDataSet(
			TandemMassSpectrum instrumentSpectrum, 
			MsMsLibraryFeature reference) {

		this();
		if(instrumentSpectrum == null || reference == null)
			throw new InvalidArgumentException(
					"Instrument MSMS and library reference must not be NULL!");
		
		MsPlotDataObject unkData = new  MsPlotDataObject(
				instrumentSpectrum.getSpectrum(),
				instrumentSpectrum.getParent(),
				instrumentSpectrum.getUserFriendlyId());
		MsPlotDataObject refData = new  MsPlotDataObject(
				reference.getSpectrum(),
				reference.getParent(),
				reference.getUserFriendlyId());

		createDataSet(unkData, refData);
	}
	
	private void createDataSet(
			MsPlotDataObject unkData, 
			MsPlotDataObject refData) {
		
		TreeSet<MsPoint>spec = new TreeSet<MsPoint>(MsUtils.mzSorter);
		parentIonDataSet = new XYSeriesCollection();
		
		//	Unknown			
		spec.addAll(unkData.getSpectrum());
		if(unkData.getParent() != null)
			spec.add(unkData.getParent());

		MsPoint[]normSpec = MsUtils.normalizeAndSortMsPattern(spec);
		msSeriesScaled.put(0, normSpec);
		labels.put(0, unkData.getLabel());			
		if(unkData.getParent() != null) {
			
			MsPoint normParent = findNormalizedParent(normSpec, unkData.getParent().getMz());			
			XYSeries parentSeries = new XYSeries(unkData.getLabel() + " parent");
			parentSeries.add(normParent.getMz(), normParent.getIntensity());			
			parentIonDataSet.addSeries(parentSeries);			
		}			
		//	Reference
		spec.clear();
		spec.addAll(refData.getSpectrum());
		if(refData.getParent() != null)
			spec.add(refData.getParent());
		
		MsPoint[]normRefSpec = MsUtils.normalizeAndSortMsPattern(spec);			
		msSeriesScaled.put(1,normRefSpec);
		labels.put(1, refData.getLabel());
		if(refData.getParent() != null) {
			
			MsPoint normRefParent = 
					findNormalizedParent(normRefSpec, refData.getParent().getMz());
			XYSeries refParentSeries = new XYSeries(refData.getLabel() + " parent");
			refParentSeries.add(normRefParent.getMz(), -normRefParent.getIntensity());			
			parentIonDataSet.addSeries(refParentSeries);			
		}
		finalizeDataSet();
	}
	
	private MsPoint findNormalizedParent(MsPoint[]normSpec, double parentMz) {
		
		Range mzRange = MsUtils.createPpmMassRange(parentMz, 10.0d);
		for(MsPoint p : normSpec) {
			if(mzRange.contains(p.getMz()))
				return p;
		}
		return null;
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

	public XYSeriesCollection getParentIonDataSet() {
		return parentIonDataSet;
	}
		
//	public HeadToTailMsDataSet(
//			MsFeaturePair featurePair, 
//			MsDepth msLevel) {
//		this();		
//		//	TODO deal in a more elegant way to support MSn?
//		if(msLevel.equals(MsDepth.All) || msLevel.equals(MsDepth.MSn))
//			return;
//		
//		MassSpectrum featureSpectrum = featurePair.getUnknownFeature().getSpectrum();
//		MassSpectrum referenceSpectrum = featurePair.getReferenceFeature().getSpectrum();
//		
//		if(featureSpectrum == null || referenceSpectrum == null)
//			return;			
//			
//		labels.put(0, featurePair.getUnknownFeature().getName());
//		labels.put(1, featurePair.getReferenceFeature().getName());
//		
//		if(msLevel.equals(MsDepth.MS1)){
//
//			msSeriesScaled.put(0, featureSpectrum.getCompleteNormalizedPattern());				
//			msSeriesScaled.put(1, referenceSpectrum.getCompleteNormalizedPattern());			
//		}
//		if(msLevel.equals(MsDepth.MS2)){
//
//			TandemMassSpectrum unkMsms = featureSpectrum.getExperimentalTandemSpectrum();
//			TandemMassSpectrum refMsms = referenceSpectrum.getExperimentalTandemSpectrum();
//			if(unkMsms == null || refMsms == null)
//				return;		
//			
//			msSeriesScaled.put(0, MsUtils.normalizeAndSortMsPattern(unkMsms.getSpectrum()));				
//			msSeriesScaled.put(1, MsUtils.normalizeAndSortMsPattern(refMsms.getSpectrum()));			
//		}
//		finalizeDataSet();
//	}
}

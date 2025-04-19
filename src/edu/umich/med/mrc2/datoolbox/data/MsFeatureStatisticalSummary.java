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
import java.io.UnsupportedEncodingException;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.jdom2.Element;

import edu.umich.med.mrc2.datoolbox.data.enums.MSFeatureSetStatisticalParameters;
import edu.umich.med.mrc2.datoolbox.project.store.CommonFields;
import edu.umich.med.mrc2.datoolbox.project.store.MassSpectrumFields;
import edu.umich.med.mrc2.datoolbox.project.store.MsFeatureStatisticalSummaryFields;
import edu.umich.med.mrc2.datoolbox.project.store.ObjectNames;
import edu.umich.med.mrc2.datoolbox.project.store.XmlStorable;
import edu.umich.med.mrc2.datoolbox.utils.NumberArrayUtils;
import edu.umich.med.mrc2.datoolbox.utils.Range;

public class MsFeatureStatisticalSummary implements Serializable, XmlStorable {

	/**
	 *
	 */
	private static final long serialVersionUID = -777181415654522841L;
	private MsFeature feature;
	private double pooledMean, pooledMedian, pooledStDev, pooledFrequency;
	private double sampleMean, sampleMedian, sampleStDev, sampleFrequency;
	private double totalMedian;
	private DescriptiveStatistics rtStatistics;
	private DescriptiveStatistics mzStatistics;
	private DescriptiveStatistics peakWidthStatistics;

	public MsFeatureStatisticalSummary(MsFeature cf) {
		this.feature = cf;
	}

	public MsFeatureStatisticalSummary(MsFeatureStatisticalSummary sourceSummary) {

		feature = sourceSummary.getFeature();
		totalMedian = sourceSummary.getTotalMedian();
		pooledMean = sourceSummary.getPooledMean();
		pooledMedian = sourceSummary.getPooledMedian();
		pooledStDev = sourceSummary.getPooledStDev();
		pooledFrequency = sourceSummary.getPooledFrequency();
		sampleMean = sourceSummary.getSampleMean();
		sampleMedian = sourceSummary.getSampleMedian();
		sampleStDev = sourceSummary.getSampleStDev();
		sampleFrequency = sourceSummary.getSampleFrequency();
		
		rtStatistics = sourceSummary.getRtStatistics();
		mzStatistics = sourceSummary.getMzStatistics();
		peakWidthStatistics = sourceSummary.getPeakWidthStatistics();
	}

	public MsFeature getFeature() {
		return feature;
	}

	public String getName() {
		return feature.getName();
	}

	public double getPooledFrequency() {
		return pooledFrequency;
	}

	public double getPooledMean() {
		return pooledMean;
	}

	public double getPooledMedian() {
		return pooledMedian;
	}

	public double getPooledRsd() {

		double rsd = 0;

		if (pooledMean > 0)
			rsd = pooledStDev / pooledMean;

		return rsd;
	}

	public double getPooledStDev() {
		return pooledStDev;
	}

	public double getSampleFrequency() {
		return sampleFrequency;
	}

	public double getSampleMean() {
		return sampleMean;
	}

	public double getSampleMedian() {
		return sampleMedian;
	}

	public double getSampleRsd() {

		double rsd = 0;

		if (sampleMean > 0)
			rsd = sampleStDev / sampleMean;

		return rsd;
	}

	public double getSampleStDev() {
		return sampleStDev;
	}

	//	TODO
	public boolean hasBetterStatsThan(MsFeatureStatisticalSummary statsToCompare) {

		boolean betterStats = false;

		return betterStats;
	}

	public void setPooledFrequency(double pooledFrequency) {
		this.pooledFrequency = pooledFrequency;
	}

	public void setPooledMean(double pooledMean) {
		this.pooledMean = pooledMean;
	}

	public void setPooledMedian(double pooledMedian) {
		this.pooledMedian = pooledMedian;
	}

	public void setPooledStDev(double pooledStDev) {
		this.pooledStDev = pooledStDev;
	}

	public void setSampleFrequency(double sampleFrequency) {
		this.sampleFrequency = sampleFrequency;
	}

	public void setSampleMean(double sampleMean) {
		this.sampleMean = sampleMean;
	}

	public void setSampleMedian(double sampleMedian) {
		this.sampleMedian = sampleMedian;
	}

	public void setSampleStDev(double sampleStDev) {
		this.sampleStDev = sampleStDev;
	}

	public double getTotalMedian() {
		return totalMedian;
	}
	
	public void setTotalMedian(double totalMedian) {
		this.totalMedian = totalMedian;
	}
	
	public double getMeanObservedRetention() {
		
		if(rtStatistics == null)
			return 0.0d;
		
		return rtStatistics.getMean();
	}

	public double getMedianObservedRetention() {
		
		if(rtStatistics == null)
			return 0.0d;
		
		return rtStatistics.getPercentile(50.0d);
	}

	public Range getRetentionRange() {
		
		if(rtStatistics == null)
			return new Range(0.0d);
		
		return new Range(rtStatistics.getMin(), rtStatistics.getMax());
	}

	public double getMeanObservedMz() {
		
		if(mzStatistics == null)
			return 0.0d;
		
		return mzStatistics.getMean();
	}

	public double getMedianObservedMz() {
		
		if(mzStatistics == null)
			return 0.0d;
		
		return mzStatistics.getPercentile(50.0d);
	}
	
	public Range getMzRange() {
		
		if(mzStatistics == null)
			return new Range(0.0d);
		
		return new Range(mzStatistics.getMin(), mzStatistics.getMax());
	}
	
	public double getMzPpmMargin() {
		
		Range mzRange = getMzRange();
		if(mzRange.getAverage() == 0.0d)
			return 0.0d;
		else
			return (mzRange.getMax() - mzRange.getAverage())/mzRange.getAverage() * 1000000.0d;		
	}

	public DescriptiveStatistics getRtStatistics() {
		return rtStatistics;
	}

	public void setRtStatistics(DescriptiveStatistics rtStatistics) {
		this.rtStatistics = rtStatistics;
	}

	public DescriptiveStatistics getMzStatistics() {
		return mzStatistics;
	}

	public void setMzStatistics(DescriptiveStatistics mzStatistics) {
		this.mzStatistics = mzStatistics;
	}

	public Double getValueOfType(MSFeatureSetStatisticalParameters statsParameter) {
		
		switch (statsParameter) {

			case TOTAL_MEDIAN:
				return totalMedian;
	
			case SAMPLE_MEDIAN:
				return sampleMedian;

			case POOLED_MEDIAN:
				return pooledMedian;
				
			case PERCENT_MISSING_IN_SAMPLES:
				return (1.0d - sampleFrequency) * 100.0d;
				
			case PERCENT_MISSING_IN_POOLS:
				return (1.0d - pooledFrequency) * 100.0d;
				
			case AREA_RSD_SAMPLES:
				return getSampleRsd();
				
			case AREA_RSD_POOLS:
				return getPooledRsd();
				
			case RT_RSD:
				if(rtStatistics != null && rtStatistics.getMean() > 0)
					return rtStatistics.getStandardDeviation() / rtStatistics.getMean();
						
			case MZ_RSD:
				if(mzStatistics != null && mzStatistics.getMean() > 0)
					return mzStatistics.getStandardDeviation() / mzStatistics.getMean();
				
			default:
				break;
		}
		return null;
	}
	
	public Double getValueOfTypeForPlot(MSFeatureSetStatisticalParameters statsParameter) {
		
		switch (statsParameter) {

			case TOTAL_MEDIAN:
				return totalMedian;
	
			case SAMPLE_MEDIAN:
				return sampleMedian;

			case POOLED_MEDIAN:
				return pooledMedian;
				
			case PERCENT_MISSING_IN_SAMPLES:
				return (1.0d - sampleFrequency) * 100.0d;
				
			case PERCENT_MISSING_IN_POOLS:
				return (1.0d - pooledFrequency) * 100.0d;
				
			case AREA_RSD_SAMPLES:
				return getSampleRsd() * 100.0d;
				
			case AREA_RSD_POOLS:
				return getPooledRsd() * 100.0d;
				
			case RT_RSD:
				if(rtStatistics != null && rtStatistics.getMean() > 0)
					return rtStatistics.getStandardDeviation() / rtStatistics.getMean() * 100.0d;
				else 
					return null;
						
			case MZ_RSD:
				if(mzStatistics != null && mzStatistics.getMean() > 0)
					return mzStatistics.getStandardDeviation() / mzStatistics.getMean() * 100.0d;
				else 
					return null;
				
			default:
				break;
		}
		return null;
	}

	public DescriptiveStatistics getPeakWidthStatistics() {
		return peakWidthStatistics;
	}

	public void setPeakWidthStatistics(DescriptiveStatistics peakWidthStatistics) {
		this.peakWidthStatistics = peakWidthStatistics;
	}

	public MsFeatureStatisticalSummary(
			MsFeature cf, Element msfStatSummaryElement) {
		this(cf);

		
		pooledMean = Double.parseDouble(msfStatSummaryElement.getAttributeValue(
				MsFeatureStatisticalSummaryFields.pooledMean.name()));
		pooledMedian = Double.parseDouble(msfStatSummaryElement.getAttributeValue(
				MsFeatureStatisticalSummaryFields.pooledMedian.name()));
		pooledStDev = Double.parseDouble(msfStatSummaryElement.getAttributeValue(
				MsFeatureStatisticalSummaryFields.pooledStDev.name()));
		pooledFrequency = Double.parseDouble(msfStatSummaryElement.getAttributeValue(
				MsFeatureStatisticalSummaryFields.pooledFrequency.name()));
		
		sampleMean = Double.parseDouble(msfStatSummaryElement.getAttributeValue(
				MsFeatureStatisticalSummaryFields.sampleMean.name()));
		sampleMedian = Double.parseDouble(msfStatSummaryElement.getAttributeValue(
				MsFeatureStatisticalSummaryFields.sampleMedian.name()));
		sampleStDev = Double.parseDouble(msfStatSummaryElement.getAttributeValue(
				MsFeatureStatisticalSummaryFields.sampleStDev.name()));
		sampleFrequency = Double.parseDouble(msfStatSummaryElement.getAttributeValue(
				MsFeatureStatisticalSummaryFields.sampleFrequency.name()));
		
		totalMedian = Double.parseDouble(msfStatSummaryElement.getAttributeValue(
				MsFeatureStatisticalSummaryFields.totalMedian.name()));
		
		Element  rtValuesElement = msfStatSummaryElement.getChild(
				MsFeatureStatisticalSummaryFields.rtStatistics.name());
		if(rtValuesElement != null && !rtValuesElement.getText().isEmpty()) {
			
			double[] rtValues = null;
			try {
				rtValues = NumberArrayUtils.decodeNumberArray(rtValuesElement.getText());
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(rtValues != null)
				rtStatistics = new DescriptiveStatistics(rtValues);
		}
		Element  mzValuesElement = msfStatSummaryElement.getChild(
				MsFeatureStatisticalSummaryFields.mzStatistics.name());
		if(mzValuesElement != null) {
			
			double[] mzValues = NumberArrayUtils.decodeValueString(mzValuesElement.getText());
			if(mzValues != null)
				mzStatistics = new DescriptiveStatistics(mzValues);
		}
		Element  pwValuesElement = msfStatSummaryElement.getChild(
				MsFeatureStatisticalSummaryFields.pwStatistics.name());
		if(pwValuesElement != null) {
			
			double[] pwValues = NumberArrayUtils.decodeValueString(pwValuesElement.getText());
			if(pwValues != null)
				peakWidthStatistics = new DescriptiveStatistics(pwValues);
		}
	}
	
	@Override
	public Element getXmlElement() {
		
		Element msFeatureStatisticalSummaryElement = 
				new Element(ObjectNames.MSFStatSummary.name());
//		msFeatureStatisticalSummaryElement.setAttribute(
//				CommonFields.Id.name(), feature.getId());
		
		msFeatureStatisticalSummaryElement.setAttribute(
				MsFeatureStatisticalSummaryFields.pooledMean.name(),
				Double.toString(pooledMean));
		msFeatureStatisticalSummaryElement.setAttribute(
				MsFeatureStatisticalSummaryFields.pooledMedian.name(), 
				Double.toString(pooledMedian));
		msFeatureStatisticalSummaryElement.setAttribute(
				MsFeatureStatisticalSummaryFields.pooledStDev.name(), 
				Double.toString(pooledStDev));
		msFeatureStatisticalSummaryElement.setAttribute(
				MsFeatureStatisticalSummaryFields.pooledFrequency.name(), 
				Double.toString(pooledFrequency));
		
		msFeatureStatisticalSummaryElement.setAttribute(
				MsFeatureStatisticalSummaryFields.sampleMean.name(),
				Double.toString(sampleMean));
		msFeatureStatisticalSummaryElement.setAttribute(
				MsFeatureStatisticalSummaryFields.sampleMedian.name(), 
				Double.toString(sampleMedian));
		msFeatureStatisticalSummaryElement.setAttribute(
				MsFeatureStatisticalSummaryFields.sampleStDev.name(), 
				Double.toString(sampleStDev));
		msFeatureStatisticalSummaryElement.setAttribute(
				MsFeatureStatisticalSummaryFields.sampleFrequency.name(), 
				Double.toString(sampleFrequency));
		
		msFeatureStatisticalSummaryElement.setAttribute(
				MsFeatureStatisticalSummaryFields.totalMedian.name(), 
				Double.toString(totalMedian));
		
		String rtValueString = NumberArrayUtils.encodeStatValues(rtStatistics);
		if(rtValueString != null) {
			
			Element rtValuesElement = 
					new Element(MsFeatureStatisticalSummaryFields.rtStatistics.name());
			rtValuesElement.setText(rtValueString);
			msFeatureStatisticalSummaryElement.addContent(rtValuesElement);
		}
		String mzValueString = NumberArrayUtils.encodeStatValues(mzStatistics);
		if(mzValueString != null) {
			
			Element mzValuesElement = 
					new Element(MsFeatureStatisticalSummaryFields.mzStatistics.name());
			mzValuesElement.setText(mzValueString);
			msFeatureStatisticalSummaryElement.addContent(mzValuesElement);
		}
		String pwValueString = NumberArrayUtils.encodeStatValues(mzStatistics);
		if(pwValueString != null) {
			
			Element pwValuesElement = 
					new Element(MsFeatureStatisticalSummaryFields.pwStatistics.name());
			pwValuesElement.setText(pwValueString);
			msFeatureStatisticalSummaryElement.addContent(pwValuesElement);
		}
		return msFeatureStatisticalSummaryElement;
	}
}













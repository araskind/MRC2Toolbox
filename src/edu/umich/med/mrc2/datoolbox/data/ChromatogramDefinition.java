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
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import edu.umich.med.mrc2.datoolbox.data.enums.MassErrorType;
import edu.umich.med.mrc2.datoolbox.data.enums.Polarity;
import edu.umich.med.mrc2.datoolbox.gui.plot.chromatogram.ChromatogramPlotMode;
import edu.umich.med.mrc2.datoolbox.project.store.XICDefinitionFields;
import edu.umich.med.mrc2.datoolbox.utils.Range;
import edu.umich.med.mrc2.datoolbox.utils.filter.Filter;
import edu.umich.med.mrc2.datoolbox.utils.filter.SavitzkyGolayFilter;
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
	private Filter smoothingFilter;
	
	public ChromatogramDefinition(
			ChromatogramPlotMode mode, 
			Polarity polarity, 
			int msLevel, 
			Collection<Double> mzList,
			boolean sumAllMassChromatograms, 
			Double mzWindowValue, 
			MassErrorType massErrorType, 
			Range rtRange,
			Filter smoothingFilter) {
		super();
		this.mode = mode;
		this.polarity = polarity;
		this.msLevel = msLevel;
		this.mzList = new TreeSet<Double>();
		if(mzList != null)
			this.mzList.addAll(mzList);
		
		this.sumAllMassChromatograms = sumAllMassChromatograms;
		this.mzWindowValue = mzWindowValue;
		this.massErrorType = massErrorType;
		this.rtRange = rtRange;
		this.smoothingFilter = smoothingFilter;
		if(smoothingFilter != null)
			doSmooth = true;
	}

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
		this.mzList = new TreeSet<Double>();
		if(mzList != null)
			this.mzList.addAll(mzList);
		
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
		this.mzList = new TreeSet<Double>();
		if(mzList != null)
			this.mzList.addAll(mzList);
		
		this.sumAllMassChromatograms = sumAllMassChromatograms;
		this.mzWindowValue = mzWindowValue;
		this.massErrorType = massErrorType;
		this.rtRange = rtRange;
		this.doSmooth = doSmooth;
		this.filterWidth = filterWidth;
		if(doSmooth)
			smoothingFilter = new SavitzkyGolayFilter(filterWidth.getWidth());
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
		
		return new ChromatogramDefinition(
				 mode, 
				 polarity, 
				 msLevel, 
				 mzList,
				 sumAllMassChromatograms, 
				 mzWindowValue, 
				 massErrorType, 
				 rtRange,
				 smoothingFilter);
	}

	public boolean isDoSmooth() {
		return doSmooth;
	}

	public SavitzkyGolayWidth getFilterWidth() {
		return filterWidth;
	}

	public Filter getSmoothingFilter() {
		return smoothingFilter;
	}
	
    @Override
    public boolean equals(Object obj) {

		if (obj == this)
			return true;

        if (obj == null)
            return false;

        if (!ChromatogramDefinition.class.isAssignableFrom(obj.getClass()))
            return false;

        final ChromatogramDefinition other = (ChromatogramDefinition) obj;

        if (!this.mode.equals(other.getMode()))
            return false;

        if (!this.polarity.equals(other.getPolarity()))
            return false;
        
        if (this.msLevel != other.getMsLevel())
            return false;
        
        if(!CollectionUtils.isEqualCollection(this.mzList, other.getMzList()))
        	 return false;
        
        if (this.sumAllMassChromatograms != other.getSumAllMassChromatograms())
            return false;
        
        if (this.mzWindowValue != other.getMzWindowValue())
            return false;
        
        if (!this.massErrorType.equals(other.getMassErrorType()))
            return false;
        
        if (!this.rtRange.equals(other.getRtRange()))
            return false;
        
        if (!this.smoothingFilter.equals(other.getSmoothingFilter()))
            return false;
        
        return true;
    }
		
//	public Element getXmlElement(Document parentDocument) {
//		
//		Element chromatogramDefinitionElement = parentDocument.createElement(
//				XICDefinitionFields.XICDefinition.name());
//		chromatogramDefinitionElement.setAttribute(
//				XICDefinitionFields.Mode.name(), mode.name());
//		if(polarity != null)
//			chromatogramDefinitionElement.setAttribute(
//				XICDefinitionFields.Pol.name(), polarity.getCode());
//		
//		chromatogramDefinitionElement.setAttribute(
//				XICDefinitionFields.MsLevel.name(), Integer.toString(msLevel));
//		if(mzList != null && !mzList.isEmpty()) {
//			if(mzList.size() == 1) {
//				chromatogramDefinitionElement.setAttribute(
//						XICDefinitionFields.MZList.name(), 
//						Double.toString(mzList.iterator().next()));
//			}
//			else {
//				List<String> stringList = mzList.stream().
//						map(mz -> Double.toString(mz)).
//						collect(Collectors.toList());
//				chromatogramDefinitionElement.setAttribute(
//						XICDefinitionFields.MZList.name(), 
//						StringUtils.join(stringList, " "));
//			}
//		}
//		chromatogramDefinitionElement.setAttribute(
//				XICDefinitionFields.SumAll.name(), 
//				Boolean.toString(sumAllMassChromatograms));
//		chromatogramDefinitionElement.setAttribute(
//				XICDefinitionFields.MzWindow.name(), 
//				Double.toString(mzWindowValue));
//		
//		if(massErrorType != null)
//			chromatogramDefinitionElement.setAttribute(
//				XICDefinitionFields.METype.name(), massErrorType.name());	
//		
//		if(rtRange != null)
//			chromatogramDefinitionElement.setAttribute(
//					XICDefinitionFields.RTRange.name(), rtRange.getStorableString());	
//		
//		//	TODO save filter type and parameters
//		if(smoothingFilter != null) {
//				chromatogramDefinitionElement.setAttribute(
//						XICDefinitionFields.Filter.name(), smoothingFilter.getCode());			
//		}
//		chromatogramDefinitionElement.setAttribute(
//				XICDefinitionFields.Smooth.name(), 
//				Boolean.toString(doSmooth));
//		return chromatogramDefinitionElement;		
//	}

	public org.jdom2.Element getXmlElement() {
		
		org.jdom2.Element chromatogramDefinitionElement = 
				new org.jdom2.Element(XICDefinitionFields.XICDefinition.name());
		chromatogramDefinitionElement.setAttribute(
				XICDefinitionFields.Mode.name(), mode.name());
		if(polarity != null)
			chromatogramDefinitionElement.setAttribute(
				XICDefinitionFields.Pol.name(), polarity.getCode());
		
		chromatogramDefinitionElement.setAttribute(
				XICDefinitionFields.MsLevel.name(), Integer.toString(msLevel));
		if(mzList != null && !mzList.isEmpty()) {
			if(mzList.size() == 1) {
				chromatogramDefinitionElement.setAttribute(
						XICDefinitionFields.MZList.name(), 
						Double.toString(mzList.iterator().next()));
			}
			else {
				List<String> stringList = mzList.stream().
						map(mz -> Double.toString(mz)).
						collect(Collectors.toList());
				chromatogramDefinitionElement.setAttribute(
						XICDefinitionFields.MZList.name(), 
						StringUtils.join(stringList, " "));
			}
		}
		chromatogramDefinitionElement.setAttribute(
				XICDefinitionFields.SumAll.name(), 
				Boolean.toString(sumAllMassChromatograms));
		chromatogramDefinitionElement.setAttribute(
				XICDefinitionFields.MzWindow.name(), 
				Double.toString(mzWindowValue));
		
		if(massErrorType != null)
			chromatogramDefinitionElement.setAttribute(
				XICDefinitionFields.METype.name(), massErrorType.name());	
		
		if(rtRange != null)
			chromatogramDefinitionElement.setAttribute(
					XICDefinitionFields.RTRange.name(), rtRange.getStorableString());	
		
		//	TODO save filter type and parameters
		if(smoothingFilter != null) {
				chromatogramDefinitionElement.setAttribute(
						XICDefinitionFields.Filter.name(), smoothingFilter.getCode());			
		}
		chromatogramDefinitionElement.setAttribute(
				XICDefinitionFields.Smooth.name(), 
				Boolean.toString(doSmooth));
		
		return chromatogramDefinitionElement;
	}
	
//	public ChromatogramDefinition(Element cdElement) {
//		
//		mode = ChromatogramPlotMode.getChromatogramPlotModeByName(
//				cdElement.getAttribute(XICDefinitionFields.Mode.name()));
//		String polCode = cdElement.getAttribute(XICDefinitionFields.Pol.name());
//		if(!polCode.isEmpty())
//			Polarity.getPolarityByCode(polCode);
//		
//		msLevel = Integer.parseInt(
//				cdElement.getAttribute(XICDefinitionFields.MsLevel.name()));
//
//		mzList = new TreeSet<Double>();
//		String mzListString = cdElement.getAttribute(XICDefinitionFields.MZList.name());
//		if(!mzListString.isEmpty()) {
//			String[] mzChunks = mzListString.split(" ");
//			for(String mz : mzChunks)
//				mzList.add(Double.parseDouble(mz));
//		}
//		sumAllMassChromatograms = Boolean.parseBoolean(
//				cdElement.getAttribute(XICDefinitionFields.SumAll.name()));
//		mzWindowValue = Double.parseDouble(
//				cdElement.getAttribute(XICDefinitionFields.MzWindow.name()));
//		String massErrorTypeString = 
//				cdElement.getAttribute(XICDefinitionFields.METype.name());
//		if(!massErrorTypeString.isEmpty())
//			massErrorType = MassErrorType.getTypeByName(massErrorTypeString);
//		
//		String rtRangeString = cdElement.getAttribute(XICDefinitionFields.RTRange.name());
//		if(!rtRangeString.isEmpty())
//			rtRange = new Range(rtRangeString);
//		
//		doSmooth = Boolean.parseBoolean(
//				cdElement.getAttribute(XICDefinitionFields.Smooth.name()));
//		
//		String filterCode = cdElement.getAttribute(XICDefinitionFields.Filter.name());
//		if(!filterCode.isEmpty()) {
//			//	TODO - restore filter type and parameters
//		}
//	}

	public ChromatogramDefinition(org.jdom2.Element cdElement) {

		mode = ChromatogramPlotMode.getChromatogramPlotModeByName(
				cdElement.getAttributeValue(XICDefinitionFields.Mode.name()));
		String polCode = cdElement.getAttributeValue(XICDefinitionFields.Pol.name());
		if(polCode != null)
			Polarity.getPolarityByCode(polCode);
		
		msLevel = Integer.parseInt(
				cdElement.getAttributeValue(XICDefinitionFields.MsLevel.name()));

		mzList = new TreeSet<Double>();
		String mzListString = cdElement.getAttributeValue(XICDefinitionFields.MZList.name());
		if(mzListString != null) {
			String[] mzChunks = mzListString.split(" ");
			for(String mz : mzChunks)
				mzList.add(Double.parseDouble(mz));
		}
		sumAllMassChromatograms = Boolean.parseBoolean(
				cdElement.getAttributeValue(XICDefinitionFields.SumAll.name()));
		mzWindowValue = Double.parseDouble(
				cdElement.getAttributeValue(XICDefinitionFields.MzWindow.name()));
		String massErrorTypeString = 
				cdElement.getAttributeValue(XICDefinitionFields.METype.name());
		if(massErrorTypeString != null)
			massErrorType = MassErrorType.getTypeByName(massErrorTypeString);
		
		String rtRangeString = cdElement.getAttributeValue(XICDefinitionFields.RTRange.name());
		if(rtRangeString != null)
			rtRange = new Range(rtRangeString);
		
		doSmooth = Boolean.parseBoolean(
				cdElement.getAttributeValue(XICDefinitionFields.Smooth.name()));
		
		String filterCode = cdElement.getAttributeValue(XICDefinitionFields.Filter.name());
		if(filterCode != null) {
			//	TODO - restore filter type and parameters
		}
	}
}




















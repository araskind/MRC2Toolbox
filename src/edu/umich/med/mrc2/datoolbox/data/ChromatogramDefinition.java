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
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jdom2.Element;

import edu.umich.med.mrc2.datoolbox.data.enums.MassErrorType;
import edu.umich.med.mrc2.datoolbox.data.enums.Polarity;
import edu.umich.med.mrc2.datoolbox.gui.plot.chromatogram.ChromatogramPlotMode;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.project.store.SmoothingFilterFields;
import edu.umich.med.mrc2.datoolbox.project.store.XICDefinitionFields;
import edu.umich.med.mrc2.datoolbox.utils.MsUtils;
import edu.umich.med.mrc2.datoolbox.utils.Range;
import edu.umich.med.mrc2.datoolbox.utils.filter.Filter;
import edu.umich.med.mrc2.datoolbox.utils.filter.FilterFactory;
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
		if(rtRange!= null)
			this.rtRange = new Range(rtRange);
		
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
		if(rtRange!= null)
			this.rtRange = new Range(rtRange);
		
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
		if(rtRange!= null)
			this.rtRange = new Range(rtRange);
		
		this.doSmooth = doSmooth;
		this.filterWidth = filterWidth;
		if(doSmooth)
			smoothingFilter = new SavitzkyGolayFilter(filterWidth.getWidth());
	}

	public ChromatogramDefinition(
			Polarity polarity, 
			int msLevel, 
			Collection<Double> mzList,
			Double mzWindowValue, 
			MassErrorType massErrorType, 
			Range rtRange) {
		super();
		this.mode = ChromatogramPlotMode.XIC;
		this.polarity = polarity;
		this.msLevel = msLevel;
		this.mzList = new TreeSet<Double>();
		if(mzList != null)
			this.mzList.addAll(mzList);
		
		this.sumAllMassChromatograms = false;
		this.mzWindowValue = mzWindowValue;
		this.massErrorType = massErrorType;
		if(rtRange!= null)
			this.rtRange = new Range(rtRange);
		
		this.doSmooth = false;
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
	
	public String getMzListString() {
		
		if(mzList.isEmpty())
			return null;
		
		List<String> stringList = mzList.stream().
				map(mz -> MRC2ToolBoxConfiguration.getMzFormat().format(mz)).
				collect(Collectors.toList());
		
		return StringUtils.join(stringList, ", ");
	}
	
	public Double getMzWindowValue() {
		return mzWindowValue;
	}
	
	public MassErrorType getMassErrorType() {
		return massErrorType;
	}
	
	public String getMZWindowString() {
		
		if(mzWindowValue == null || mzWindowValue.equals(Double.NaN) 
				|| mzWindowValue == 0 || massErrorType == null)
			return null;
		else
			return '\u00B1' + MRC2ToolBoxConfiguration.getPpmFormat().
				format(mzWindowValue) + " " + massErrorType.name();
	}
	
	public Range getRtRange() {
		return rtRange;
	}
	
	public boolean getSumAllMassChromatograms() {
		return sumAllMassChromatograms;
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
	
	public void recenterRtRange(double newCenterRt) {
		
		if(rtRange == null)
			return;
		
		double halfWidth = rtRange.getSize() / 2.0d;
		rtRange = new Range(newCenterRt - halfWidth, newCenterRt + halfWidth);
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
    
	public Element getXmlElement() {
		
		Element chromatogramDefinitionElement = 
				new Element(XICDefinitionFields.XICDefinition.name());
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

		if(smoothingFilter != null)
			chromatogramDefinitionElement.addContent(smoothingFilter.getXmlElement());				
		
		chromatogramDefinitionElement.setAttribute(
				XICDefinitionFields.Smooth.name(), 
				Boolean.toString(doSmooth));
		
		return chromatogramDefinitionElement;
	}
	
	public String getChromatogramDefinitionHash(){

		List<String> chunks = new ArrayList<String>();
		chunks.add(mode.name());
		if(polarity != null)
			chunks.add(polarity.getCode());
		
		chunks.add(Integer.toString(msLevel));		
		if(mzList != null && !mzList.isEmpty()) {
			List<String> stringList = mzList.stream().
					map(mz -> MsUtils.spectrumMzExportFormat.format(mz)).
					collect(Collectors.toList());
			chunks.add(StringUtils.join(stringList));
		}		
		chunks.add(MsUtils.spectrumMzExportFormat.format(mzWindowValue));
		if(massErrorType != null)
			chunks.add(massErrorType.name());
		
		if(rtRange != null)
			chunks.add(rtRange.getStorableString());		
		
		//	TODO smoothing filter 
		//	chunks.add("");
		
		chunks.add(Boolean.toString(doSmooth));
	    try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(StringUtils.join(chunks).getBytes(Charset.forName("windows-1252")));
			return DatatypeConverter.printHexBinary(md.digest()).toUpperCase();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	    return null;
	}

	public ChromatogramDefinition(Element cdElement) {

		mode = ChromatogramPlotMode.getChromatogramPlotModeByName(
				cdElement.getAttributeValue(XICDefinitionFields.Mode.name()));
		String polCode = cdElement.getAttributeValue(XICDefinitionFields.Pol.name());
		if(polCode != null)
			polarity = Polarity.getPolarityByCode(polCode);
		
		msLevel = Integer.parseInt(
				cdElement.getAttributeValue(XICDefinitionFields.MsLevel.name()));

		mzList = new TreeSet<Double>();
		String mzListString = 
				cdElement.getAttributeValue(XICDefinitionFields.MZList.name());
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
		Element filterElement = cdElement.getChild(SmoothingFilterFields.Filter.name());
		if(filterElement != null) {
			try {
				smoothingFilter = FilterFactory.getFilter(filterElement);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		}
	}

	public void setMzList(Collection<Double> mzList) {
		this.mzList = mzList;
	}

	public void setPolarity(Polarity polarity) {
		this.polarity = polarity;
	}
}




















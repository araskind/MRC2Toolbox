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

package edu.umich.med.mrc2.datoolbox.data;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.jdom2.Element;

import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.project.store.AvgMSFields;
import edu.umich.med.mrc2.datoolbox.project.store.ObjectNames;
import edu.umich.med.mrc2.datoolbox.utils.Range;

public class AverageMassSpectrum implements Serializable, Comparable<AverageMassSpectrum>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5389615971414831904L;
	private MassSpectrum masSpectrum;
	private DataFile dataFile;
	private int msLevel;
	private Range rtRange;
	private Collection<Integer>scanNumbers;
	
	public AverageMassSpectrum(
			DataFile dataFile, 
			int msLlevel, 
			Range rtRange) {
		super();
		this.dataFile = dataFile;
		this.msLevel = msLlevel;
		this.rtRange = rtRange;
		masSpectrum = new MassSpectrum();
		scanNumbers = new TreeSet<Integer>();
	}

	public AverageMassSpectrum(
			DataFile dataFile, 
			int msLlevel, 
			Collection<Integer> scanNumbers2) {
		super();
		this.dataFile = dataFile;
		this.msLevel = msLlevel;
		this.scanNumbers = new TreeSet<Integer>(scanNumbers);
		masSpectrum = new MassSpectrum();
	}

	public MassSpectrum getMasSpectrum() {
		return masSpectrum;
	}

	public DataFile getDataFile() {
		return dataFile;
	}

	public int getMsLlevel() {
		return msLevel;
	}

	public Range getRtRange() {
		return rtRange;
	}

	public Collection<Integer> getScanNumbers() {
		return scanNumbers;
	}
	
	@Override
	public String toString() {
		String name =  
				"Average MS" + Integer.toString(msLevel) + 
				" for " + dataFile.getName();
		if(rtRange != null)
			name += " | " + rtRange.getFormattedString(
					MRC2ToolBoxConfiguration.getRtFormat());
		
		return name;
	}

	@Override
	public int compareTo(AverageMassSpectrum o) {
		return this.toString().compareTo(o.toString());
	}

	public Element getXmlElement() {
		
		Element avgMsElement = new Element(ObjectNames.AvgMs.name());
		avgMsElement.setAttribute(
				AvgMSFields.MsLevel.name(), Integer.toString(msLevel));
		
		if(scanNumbers != null && !scanNumbers.isEmpty()) {
			
			if(scanNumbers.size() == 1) {
				avgMsElement.setAttribute(
						AvgMSFields.ScanList.name(), 
						Double.toString(scanNumbers.iterator().next()));
			}
			else {
				List<String> stringList = scanNumbers.stream().
						map(mz -> Integer.toString(mz)).
						collect(Collectors.toList());
				avgMsElement.setAttribute(
						AvgMSFields.ScanList.name(), 
						StringUtils.join(stringList, " "));
			}
		}		
		if(rtRange != null)
			avgMsElement.setAttribute(
					AvgMSFields.RTRange.name(), rtRange.getStorableString());
		
		if(masSpectrum != null)
			avgMsElement.addContent(masSpectrum.getXmlElement());
		
		return avgMsElement;
	}
	
	public AverageMassSpectrum(Element avgMsElement, DataFile dataFile2) {

		this.dataFile = dataFile2;
		if(avgMsElement.getAttributeValue(AvgMSFields.MsLevel.name()) != null)
			msLevel = Integer.parseInt(
				avgMsElement.getAttributeValue(AvgMSFields.MsLevel.name()));
		
		scanNumbers = new TreeSet<Integer>();
		String scanListString = 
				avgMsElement.getAttributeValue(AvgMSFields.ScanList.name());
		if(scanListString != null) {
			String[] scanChunks = scanListString.split(" ");
			for(String sc : scanChunks)
				scanNumbers.add(Integer.parseInt(sc));
		}
		String rtRangeString = 
				avgMsElement.getAttributeValue(AvgMSFields.RTRange.name());
		if(rtRangeString != null)
			rtRange = new Range(rtRangeString);
		
		Element spectrumElement = 
				avgMsElement.getChild(ObjectNames.Spectrum.name());
		if(spectrumElement != null)
			masSpectrum = new MassSpectrum(spectrumElement);		
	}
}















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

import java.awt.Color;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Collection;

import org.jdom2.Element;

import edu.umich.med.mrc2.datoolbox.gui.plot.chromatogram.ChromatogramPlotMode;
import edu.umich.med.mrc2.datoolbox.gui.utils.ColorUtils;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.project.store.XICDefinitionFields;
import edu.umich.med.mrc2.datoolbox.project.store.XICFields;
import edu.umich.med.mrc2.datoolbox.utils.NumberArrayUtils;

public class ExtractedChromatogram implements Comparable<ExtractedChromatogram>, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5312501886305484043L;
	private DataFile dataFile;
	private ChromatogramDefinition definition;
	private double[] timeValues;
	private double[] intensityValues;
	private String note;
	private Collection<Integer>inflectionPoints;
	private Color color;
	
	public ExtractedChromatogram(DataFile dataFile, ChromatogramDefinition definition) {
		super();
		this.dataFile = dataFile;
		this.definition = definition;
	}

	public DataFile getDataFile() {
		return dataFile;
	}

	public ChromatogramDefinition getChromatogramDefinition() {
		return definition;
	}

	public double[] getTimeValues() {
		return timeValues;
	}

	public void setTimeValues(double[] timeValues) {
		this.timeValues = timeValues;
	}

	public double[] getIntensityValues() {
		return intensityValues;
	}

	public void setIntensityValues(double[] intensityValues) {
		this.intensityValues = intensityValues;
	}

	public ChromatogramDefinition getDefinition() {
		return definition;
	}
	
	public String toString() {
		
		String name = dataFile.getName() + " MS" + 
				Integer.toString(definition.getMsLevel()) + " " + 
				definition.getMode().name();			
		if(definition.getMode().equals(ChromatogramPlotMode.XIC)) {
			
			name += " | M/Z ";
			for(Double mz : definition.getMzList())
				name +=" " + MRC2ToolBoxConfiguration.getMzFormat().format(mz) + ";";
			
			if(definition.getSumAllMassChromatograms() && definition.getMzList().size() > 1)
				name += " combined";
		}
		if(definition.getRtRange() != null)
			name += " | RT " + definition.getRtRange().getFormattedString(MRC2ToolBoxConfiguration.getRtFormat());
		
		if(definition.getSmoothingFilter() != null)
			name += " " + definition.getSmoothingFilter().getCode();
			
		if(note != null)
			name += " |  " + note;
		
		return name;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public Collection<Integer> getInflectionPoints() {
		return inflectionPoints;
	}

	public void setInflectionPoints(Collection<Integer> inflectionPoints) {
		this.inflectionPoints = inflectionPoints;
	}

	@Override
	public int compareTo(ExtractedChromatogram o) {
		return this.toString().compareTo(o.toString());
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public Element getXmlElement(DataFile dataFile) {

		Element extractedChromatogramElement = 
				new Element(XICFields.XIC.name());		
		String time = "";
		try {
			time = NumberArrayUtils.encodeNumberArray(timeValues);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Element  timeElement = 
				new Element (XICFields.Time.name()).setText(time);		
		extractedChromatogramElement.addContent(timeElement);
		
		String intensity = "";
		try {
			intensity = NumberArrayUtils.encodeNumberArray(intensityValues);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Element  intensityElement = 
				new Element (XICFields.Intensity.name()).setText(intensity);		
		extractedChromatogramElement.addContent(intensityElement);
		
		if(color != null)
			extractedChromatogramElement.setAttribute(XICFields.Color.name(), 
					ColorUtils.rgb2hex(color));	
		
		if(note != null)
			extractedChromatogramElement.setAttribute(XICFields.Note.name(), note);	
		
		extractedChromatogramElement.addContent(definition.getXmlElement());
		
		return extractedChromatogramElement;
	}
	
	public ExtractedChromatogram(Element xicElement, DataFile dataFile2) {

		this.dataFile = dataFile2;
		String timeText =  
				xicElement.getChild(XICFields.Time.name()).getText();
		try {
			timeValues = NumberArrayUtils.decodeNumberArray(timeText);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String intensityText =  
				xicElement.getChild(XICFields.Intensity.name()).getText();
		try {
			intensityValues = NumberArrayUtils.decodeNumberArray(intensityText);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String colorCode = xicElement.getAttributeValue(XICFields.Color.name());
		if(colorCode == null)
			color = Color.BLACK;
		else
			color = ColorUtils.hex2rgb(colorCode);
		
		String noteText = xicElement.getAttributeValue(XICFields.Note.name());
		if(noteText != null)
			note = noteText;
		
		Element cdElement = 
				xicElement.getChild(XICDefinitionFields.XICDefinition.name());		
		definition = new ChromatogramDefinition(cdElement);
	}
}












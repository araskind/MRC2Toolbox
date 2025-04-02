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
import java.io.File;
import java.io.Serializable;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import org.apache.commons.compress.utils.FileNameUtils;
import org.jdom2.Element;

import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.data.lims.DataAcquisitionMethod;
import edu.umich.med.mrc2.datoolbox.data.lims.DataExtractionMethod;
import edu.umich.med.mrc2.datoolbox.data.lims.Injection;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCache;
import edu.umich.med.mrc2.datoolbox.database.idt.OfflineExperimentLoadCache;
import edu.umich.med.mrc2.datoolbox.gui.utils.ColorUtils;
import edu.umich.med.mrc2.datoolbox.project.store.CommonFields;
import edu.umich.med.mrc2.datoolbox.project.store.DataFileFields;
import edu.umich.med.mrc2.datoolbox.project.store.ObjectNames;
import edu.umich.med.mrc2.datoolbox.project.store.XICFields;
import edu.umich.med.mrc2.datoolbox.utils.ExperimentUtils;

public class DataFile implements Comparable<DataFile>, Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 821945926362226483L;
	
	private String name;
	private String fullPath;
	private boolean enabled;
	private ExperimentalSample parentSample;	
	private DataAcquisitionMethod acquisitionMethod;
	private int batchNumber;	
	private String injectionId;
	private String prepItemId;
	private Date injectionTime;	
	private String samplePosition;
	private double injectionVolume;	
	private double scalingFactor;
	private Color color;
	private Collection<ExtractedChromatogram>chromatograms;
	private Collection<AverageMassSpectrum>userSpectra;
	private Map<DataExtractionMethod,ResultsFile>resultFiles;

	public DataFile(String fileName) {

		this.name = fileName;
		injectionTime = null;
		acquisitionMethod = null;
		initFields();
	}
	
	public DataFile(File dataFile) {

		this.name = dataFile.getName();
		fullPath = dataFile.getAbsolutePath();
		injectionTime = null;
		acquisitionMethod = null;
		initFields();
	}

	public DataFile(String fileName, DataAcquisitionMethod method) {

		this.name = fileName;
		injectionTime = null;
		acquisitionMethod = method;
		initFields();
	}
	
	public DataFile(Injection injection) {

		this.name = injection.getDataFileName();	
		injectionId = injection.getId();
		injectionTime = injection.getTimeStamp();
		acquisitionMethod = 
				IDTDataCache.getAcquisitionMethodById(injection.getAcquisitionMethodId());
		prepItemId = injection.getPrepItemId();
		initFields();
	}
	
	private void initFields() {
		
		enabled = true;
		scalingFactor = 1.0d;
		batchNumber = 1;		
		chromatograms = new ArrayList<ExtractedChromatogram>();
		userSpectra = new ArrayList<AverageMassSpectrum>();
		color = Color.BLACK;
		resultFiles = new TreeMap<DataExtractionMethod,ResultsFile>();
	}

	public void addResultFile(ResultsFile result) {
		
		if(result.getMethod() == null)
			throw new IllegalArgumentException(
					"Data analysis method not defined for result file " + result.getName());
		
		resultFiles.put(result.getMethod(), result);
	}
	
	public ResultsFile getResultForDataExtractionMethod(DataExtractionMethod method) {
		return resultFiles.get(method);
	}	

	@Override
	public int compareTo(DataFile df) {
		return this.name.compareToIgnoreCase(df.getName());
	}

	public DataAcquisitionMethod getDataAcquisitionMethod() {
		return acquisitionMethod;
	}

	public String getName() {
		return name;
	}
	
	public String getBaseName() {
		return FileNameUtils.getBaseName(name);
	}

	public String getFullPath() {
		return fullPath;
	}

	public Date getInjectionTime() {
		return injectionTime;
	}

	public double getInjectionVolume() {
		return injectionVolume;
	}

	public ExperimentalSample getParentSample() {
		return parentSample;
	}

	public String getSamplePosition() {
		return samplePosition;
	}

	public double getScalingFactor() {
		return scalingFactor;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setDataAcquisitionMethod(DataAcquisitionMethod method) {
		this.acquisitionMethod = method;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public void setName(String fileName) {
		this.name = fileName;
	}

	public void setFullPath(String fullPath) {
		this.fullPath = fullPath;
	}

	public void setInjectionTime(Date injectionTime) {
		this.injectionTime = injectionTime;
	}

	public void setInjectionVolume(double injectionVolume) {
		this.injectionVolume = injectionVolume;
	}

	public void setParentSample(ExperimentalSample parentSample) {
		this.parentSample = parentSample;
	}

	public void setSamplePosition(String samplePosition) {
		this.samplePosition = samplePosition;
	}

	public void setScalingFactor(double scalingFactor) {
		this.scalingFactor = scalingFactor;
	}

	@Override
	public String toString() {
		return this.name;
	}

	//	Same file name and data acquisition method mean same source raw data file
    @Override
    public boolean equals(Object obj) {

		if (obj == this)
			return true;

        if (obj == null)
            return false;

        if (!DataFile.class.isAssignableFrom(obj.getClass()))
            return false;

        final DataFile other = (DataFile) obj;

        if ((this.name == null) ? (other.getName() != null) : !this.name.equals(other.getName()))
            return false;
              
		if ((this.acquisitionMethod == null) ? (other.getDataAcquisitionMethod() != null)
				: !this.acquisitionMethod.equals(other.getDataAcquisitionMethod()))
			return false;
        
//        if ((this.fullPath == null) ? (other.getFullPath() != null) : !this.fullPath.equals(other.getFullPath()))
//            return false;
//        
//        if ((this.injectionTime == null) ? (other.getInjectionTime() != null) : !this.injectionTime.equals(other.getInjectionTime()))
//            return false;

        return true;
    }

    @Override
    public int hashCode() {

        int hash = 3;
        hash = 53 * hash + (this.name != null ? this.name.hashCode() : 0) + 
//        		(this.fullPath != null ? this.fullPath.hashCode() : 0) +
//        		(this.injectionTime != null ? this.injectionTime.hashCode() : 0)
        		(this.acquisitionMethod != null ? this.acquisitionMethod.hashCode() : 0);

        return hash;
    }

	/**
	 * @return the batchNumber
	 */
	public int getBatchNumber() {
		return batchNumber;
	}

	/**
	 * @param batchNumber the batchNumber to set
	 */
	public void setBatchNumber(int batchNumber) {
		this.batchNumber = batchNumber;
	}

	/**
	 * @return the injectionId
	 */
	public String getInjectionId() {
		return injectionId;
	}

	/**
	 * @param injectionId the injectionId to set
	 */
	public void setInjectionId(String injectionId) {
		this.injectionId = injectionId;
	}

	public void clearInjectionData() {
	
		injectionTime = null;
		injectionId = null;
		samplePosition = null;
		injectionVolume = 0.0d;
	}
	
	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public Collection<ExtractedChromatogram> getChromatograms() {
		return chromatograms;
	}
	
	public Collection<AverageMassSpectrum> getAverageSpectra() {
		return userSpectra;
	}

	public Map<DataExtractionMethod, ResultsFile> getResultFiles() {
		return resultFiles;
	}		

	public String getPrepItemId() {
		return prepItemId;
	}

	public void setPrepItemId(String prepItemId) {
		this.prepItemId = prepItemId;
	}
	
	public Injection generateInjectionFromFileData() {
		
		if(injectionId == null)
			injectionId = DataPrefix.INJECTION.getName() + 
				UUID.randomUUID().toString().substring(0, 12);
		
		String acquisitionMethodId = null;
		if(acquisitionMethod != null)
			acquisitionMethodId = acquisitionMethod.getId();
		
		Injection inj = new Injection(
				injectionId,
				name, 
				injectionTime, 
				prepItemId,
				acquisitionMethodId,
				injectionVolume);
		return inj;
	}

	public Element getXmlElement() {
		Element dataFileElement = 
        		new Element(ObjectNames.DataFile.name());
		dataFileElement.setAttribute(CommonFields.Name.name(), name);	
		dataFileElement.setAttribute(DataFileFields.Path.name(), fullPath);	
		
		if(parentSample != null)
			dataFileElement.setAttribute(
					DataFileFields.Sample.name(), parentSample.getId());	
		
		if(acquisitionMethod != null)
			dataFileElement.setAttribute(
					DataFileFields.AcqMethod.name(), acquisitionMethod.getId());	
		
		if(injectionId != null)
			dataFileElement.setAttribute(
					DataFileFields.Injection.name(), injectionId);	
		
		if(prepItemId != null)
			dataFileElement.setAttribute(
					DataFileFields.PrepItem.name(), prepItemId);
		
		if(samplePosition != null)
			dataFileElement.setAttribute(
					DataFileFields.SamplePosition.name(), samplePosition);	
		
		if(injectionTime != null)
			dataFileElement.setAttribute(DataFileFields.InjTimestamp.name(), 
					ExperimentUtils.dateTimeFormat.format(injectionTime));
		
		if(injectionVolume > 0)
			dataFileElement.setAttribute(DataFileFields.InjVol.name(), 
					Double.toString(injectionVolume));
		
		if(color != null)
			dataFileElement.setAttribute(DataFileFields.Color.name(), 
					ColorUtils.rgb2hex(color));
		
		if(chromatograms != null && !chromatograms.isEmpty()) {
			
			Element xicListElement = new Element(
					DataFileFields.XicList.name());			
			for(ExtractedChromatogram xic : chromatograms)	
				xicListElement.addContent(xic.getXmlElement(this));
			
			dataFileElement.addContent(xicListElement);
		}
		if(userSpectra != null && !userSpectra.isEmpty()) {
			
			Element userSpectraElement = new Element(
					DataFileFields.AvgMsList.name());
			for(AverageMassSpectrum avgMs : userSpectra)
				userSpectraElement.addContent(avgMs.getXmlElement());
			
			dataFileElement.addContent(userSpectraElement);
		}
		return dataFileElement;
	}
	
	public DataFile(Element fileElement) {
		
		name = fileElement.getAttributeValue(CommonFields.Name.name());
		fullPath = fileElement.getAttributeValue(DataFileFields.Path.name());

		String acqMethodId = 
				fileElement.getAttributeValue(DataFileFields.AcqMethod.name());
		if(acqMethodId != null)
			acquisitionMethod = IDTDataCache.getAcquisitionMethodById(acqMethodId);
		
		String colorCode = 
				fileElement.getAttributeValue(DataFileFields.Color.name());
		if(colorCode == null)
			color = Color.BLACK;
		else
			color = ColorUtils.hex2rgb(colorCode);
		
		injectionId = fileElement.getAttributeValue(DataFileFields.Injection.name());
		prepItemId = fileElement.getAttributeValue(DataFileFields.PrepItem.name());
				
		String injTime = 
				fileElement.getAttributeValue(DataFileFields.InjTimestamp.name());
		if(injTime != null) {
			try {
				injectionTime = ExperimentUtils.dateTimeFormat.parse(injTime);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		samplePosition = 
				fileElement.getAttributeValue(DataFileFields.SamplePosition.name());
		
		String injVol = 
				fileElement.getAttributeValue(DataFileFields.InjVol.name());
		if(injVol != null)
			injectionVolume = Double.parseDouble(injVol);
		
		String sampleId = 
				fileElement.getAttributeValue(DataFileFields.Sample.name());
		if(sampleId != null)
			parentSample = 
					OfflineExperimentLoadCache.getExperimentalSampleById(sampleId);
		
		enabled = true;
		chromatograms = new ArrayList<ExtractedChromatogram>();
		userSpectra = new ArrayList<AverageMassSpectrum>();		
		resultFiles = new TreeMap<DataExtractionMethod,ResultsFile>();		
		scalingFactor = 1.0d;
		batchNumber = 1;
		
		Element xicListElement = 
				fileElement.getChild(DataFileFields.XicList.name());
		if(xicListElement != null) {
			List<Element> xicElementList = 
					xicListElement.getChildren(XICFields.XIC.name());
			for (Element xicElement : xicElementList) {
				ExtractedChromatogram xic = 
						new ExtractedChromatogram(xicElement, this);
				chromatograms.add(xic);
			}
		}
		Element avgMsListElement = 
				fileElement.getChild(DataFileFields.AvgMsList.name());
		if(avgMsListElement != null) {
			
			List<Element> avgMsElementList = 
					avgMsListElement.getChildren(ObjectNames.AvgMs.name());		
			for (Element avgMsElement : avgMsElementList) {
				AverageMassSpectrum avgMs = 
						new AverageMassSpectrum(avgMsElement, this);
				userSpectra.add(avgMs);
			}
		}
	}
}












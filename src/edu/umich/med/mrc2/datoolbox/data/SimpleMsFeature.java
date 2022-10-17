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
import java.util.UUID;

import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.data.enums.Polarity;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;

public class SimpleMsFeature implements Serializable {

	/**
	 * 
	 */
	protected static final long serialVersionUID = -8911576246576007819L;
	
	protected String uniqueId;
	protected String libraryTargetId;
	protected String name;
	protected MassSpectrum observedSpectrum;
	protected double retentionTime;
	protected double neutralMass;
	protected double qualityScore;
	protected double area, height, volume;
	protected DataPipeline dataPipeline;	
	protected Polarity polarity;
	
	public SimpleMsFeature(
			String libraryTargetId, 
			MassSpectrum observedSpectrum, 
			double retentionTime,
			DataPipeline dataPipeline) {
		super();		
		this.uniqueId = DataPrefix.MS_FEATURE.getName() + 
				UUID.randomUUID().toString();
		this.libraryTargetId = libraryTargetId;
		this.observedSpectrum = observedSpectrum;
		this.retentionTime = retentionTime;
		this.dataPipeline = dataPipeline;
	}

	public SimpleMsFeature(
			MassSpectrum observedSpectrum, 
			double retentionTime, 
			DataPipeline dataPipeline) {
		super();		
		this.uniqueId = DataPrefix.MS_FEATURE.getName() + 
				UUID.randomUUID().toString();
		this.observedSpectrum = observedSpectrum;
		this.retentionTime = retentionTime;
		this.dataPipeline = dataPipeline;
	}
	
	public SimpleMsFeature(
			MsFeature parent, 
			DataPipeline dataPipeline) {
		super();		
		this.uniqueId = DataPrefix.MS_FEATURE.getName() + 
				UUID.randomUUID().toString();
		this.libraryTargetId = parent.getTargetId();
		this.observedSpectrum = parent.getSpectrum();
		this.retentionTime = parent.getRetentionTime();
		this.dataPipeline = dataPipeline;	
		this.polarity = parent.getPolarity();
		this.area = parent.getArea();
		this.height = parent.getHeight();
	}	

	public String getUniqueId() {
		return uniqueId;
	}

	public void setUniqueId(String uniqueId) {
		this.uniqueId = uniqueId;
	}

	public String getLibraryTargetId() {
		return libraryTargetId;
	}

	public void setLibraryTargetId(String libraryTargetId) {
		this.libraryTargetId = libraryTargetId;
	}

	public MassSpectrum getObservedSpectrum() {
		return observedSpectrum;
	}

	public void setObservedSpectrum(MassSpectrum observedSpectrum) {
		this.observedSpectrum = observedSpectrum;
	}

	public double getRetentionTime() {
		return retentionTime;
	}

	public void setRetentionTime(double retentionTime) {
		this.retentionTime = retentionTime;
	}

	public double getNeutralMass() {
		return neutralMass;
	}

	public void setNeutralMass(double neutralMass) {
		this.neutralMass = neutralMass;
	}

	public double getQualityScore() {
		return qualityScore;
	}

	public void setQualityScore(double qualityScore) {
		this.qualityScore = qualityScore;
	}

	public double getArea() {
		return area;
	}

	public void setArea(double area) {
		this.area = area;
	}

	public double getHeight() {
		return height;
	}

	public void setHeight(double height) {
		this.height = height;
	}

	public double getVolume() {
		return volume;
	}

	public void setVolume(double volume) {
		this.volume = volume;
	}

	public DataPipeline getAssayMethod() {
		return dataPipeline;
	}

	public void setAssayMethod(DataPipeline dataPipeline) {
		this.dataPipeline = dataPipeline;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Polarity getPolarity() {
		return polarity;
	}

	public void setPolarity(Polarity polarity) {
		this.polarity = polarity;
	}
}












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

package edu.umich.med.mrc2.datoolbox.data.lims;

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import edu.umich.med.mrc2.datoolbox.data.InstrumentPlatform;
import edu.umich.med.mrc2.datoolbox.data.MassAnalyzerType;
import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;

public class LIMSInstrument implements Serializable, Comparable<LIMSInstrument>{

	/**
	 * 
	 */
	private static final long serialVersionUID = -2469246064895497950L;
	private String instrumentId;
	private String instrumentName;
	private String description;
	private MassAnalyzerType massAnalyzerType;
	private String manufacturer;
	private String model;
	private String serialNumber;
	private ChromatographicSeparationType chromatographicSeparationType;
	private InstrumentPlatform instrumentPlatform;
	private Map<String,String>properties;

	public LIMSInstrument(
			String instrumentId,
			String instrumentName,
			String description,
			MassAnalyzerType massAnalyzerType,
			ChromatographicSeparationType chromatographicSeparationType,
			String manufacturer,
			String model,
			String serialNumber) {
		super();
		this.instrumentId = instrumentId;
		this.instrumentName = instrumentName;
		this.description = description;
		this.massAnalyzerType = massAnalyzerType;
		this.chromatographicSeparationType = chromatographicSeparationType;
		this.manufacturer = manufacturer;
		this.model = model;
		this.serialNumber = serialNumber;
		
		properties = new TreeMap<String,String>();
	}
	
	public LIMSInstrument(
			String instrumentName,
			String description,
			MassAnalyzerType massAnalyzerType,
			ChromatographicSeparationType chromatographicSeparationType,
			String manufacturer,
			String model,
			String serialNumber) {
		super();
		this.instrumentName = instrumentName;
		this.description = description;
		this.massAnalyzerType = massAnalyzerType;
		this.chromatographicSeparationType = chromatographicSeparationType;
		this.manufacturer = manufacturer;
		this.model = model;
		this.serialNumber = serialNumber;
		
		properties = new TreeMap<String,String>();
	}

	public LIMSInstrument(
			String instrumentName, 
			String manufacturer,
			String model, 
			String serialNumber) {
		super();
		this.instrumentId = DataPrefix.INSTRUMENT.getName() +
				UUID.randomUUID().toString().substring(0, 12);
		this.instrumentName = instrumentName;
		this.manufacturer = manufacturer;
		this.model = model;
		this.serialNumber = serialNumber;
	}

	/**
	 * @return the instrumentId
	 */
	public String getInstrumentId() {
		return instrumentId;
	}

	/**
	 * @return the instrumentName
	 */
	public String getInstrumentName() {
		return instrumentName;
	}

	public String toString() {
		return instrumentName + " (" + instrumentId + ")";
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @return the instrumentType
	 */
	public MassAnalyzerType getMassAnalyzerType() {
		return massAnalyzerType;
	}

	/**
	 * @return the manufacturer
	 */
	public String getManufacturer() {
		return manufacturer;
	}

	/**
	 * @return the model
	 */
	public String getModel() {
		return model;
	}

	/**
	 * @return the serialNumber
	 */
	public String getSerialNumber() {
		return serialNumber;
	}

	/**
	 * @param instrumentId the instrumentId to set
	 */
	public void setInstrumentId(String instrumentId) {
		this.instrumentId = instrumentId;
	}

	/**
	 * @param instrumentName the instrumentName to set
	 */
	public void setInstrumentName(String instrumentName) {
		this.instrumentName = instrumentName;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @param instrumentType the instrumentType to set
	 */
	public void setMassAnalyzerType(MassAnalyzerType massAnalyzerType) {
		this.massAnalyzerType = massAnalyzerType;
	}

	/**
	 * @param manufacturer the manufacturer to set
	 */
	public void setManufacturer(String manufacturer) {
		this.manufacturer = manufacturer;
	}

	/**
	 * @param model the model to set
	 */
	public void setModel(String model) {
		this.model = model;
	}

	/**
	 * @param serialNumber the serialNumber to set
	 */
	public void setSerialNumber(String serialNumber) {
		this.serialNumber = serialNumber;
	}

	@Override
	public int compareTo(LIMSInstrument o) {
		return this.instrumentId.compareTo(o.getInstrumentId());
	}

    @Override
    public boolean equals(Object obj) {

		if (obj == this)
			return true;

        if (obj == null)
            return false;

        if (!LIMSInstrument.class.isAssignableFrom(obj.getClass()))
            return false;

        final LIMSInstrument other = (LIMSInstrument) obj;

        if ((this.instrumentId == null) ? (other.getInstrumentId() != null) : !this.instrumentId.equals(other.getInstrumentId()))
            return false;

        return true;
    }

    @Override
    public int hashCode() {

        int hash = 3;
        hash = 53 * hash + (this.instrumentId != null ? this.instrumentId.hashCode() : 0);
        return hash;
    }

	/**
	 * @return the chromatographicSeparationType
	 */
	public ChromatographicSeparationType getChromatographicSeparationType() {
		return chromatographicSeparationType;
	}

	/**
	 * @param chromatographicSeparationType the chromatographicSeparationType to set
	 */
	public void setChromatographicSeparationType(ChromatographicSeparationType chromatographicSeparationType) {
		this.chromatographicSeparationType = chromatographicSeparationType;
	}

	public InstrumentPlatform getInstrumentPlatform() {
		return instrumentPlatform;
	}

	public void setInstrumentPlatform(InstrumentPlatform instrumentPlatform) {
		this.instrumentPlatform = instrumentPlatform;
	}

	public Map<String, String> getProperties() {
		return properties;
	}
	
	public void addProperty(String name, String value) {
		properties.put(name, value);
	}
	
	public String getProperty(String name) {
		return properties.get(name);
	}
}





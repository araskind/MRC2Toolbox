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
import java.util.ArrayList;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.jdom2.Element;

import edu.umich.med.mrc2.datoolbox.data.Assay;
import edu.umich.med.mrc2.datoolbox.data.motrpac.MoTrPACAssay;
import edu.umich.med.mrc2.datoolbox.project.store.XmlStorable;

public class DataPipeline implements Serializable, Comparable<DataPipeline>, XmlStorable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -2462042658486523831L;
	private String name;
	private String description;
	private Assay assay;
	private MoTrPACAssay motrpacAssay;
	private DataAcquisitionMethod acquisitionMethod;
	private DataExtractionMethod dataExtractionMethod;
	
	public DataPipeline(
			DataAcquisitionMethod acquisitionMethod, 
			DataExtractionMethod dataExtractionMethod) {
		super();
		this.acquisitionMethod = acquisitionMethod;
		this.dataExtractionMethod = dataExtractionMethod;
		this.name = "DP";
		if(acquisitionMethod != null)
			this.name = acquisitionMethod.getName();
		
		if(dataExtractionMethod != null)
			this.name += "\n" + dataExtractionMethod.getName();		
	}

	public DataPipeline(
			String name,
			Assay assay, 
			DataAcquisitionMethod acquisitionMethod,
			DataExtractionMethod dataExtractionMethod) {
		super();
//		if(assay == null || acquisitionMethod == null || dataExtractionMethod == null)
//			throw new IllegalArgumentException("All values should be not NULL!");
		
		this.name = name;
		this.assay = assay;
		this.acquisitionMethod = acquisitionMethod;
		this.dataExtractionMethod = dataExtractionMethod;
	}
	
	//	Mock pipeline for feature clusters
	public DataPipeline() {
		
		this.name = "DP";
		this.assay = new Assay("Assay1", "Assay1");
		this.acquisitionMethod = 
				new DataAcquisitionMethod("DQM", "DQM", null, null, new Date());
		this.dataExtractionMethod = 
				new DataExtractionMethod("DAM", "DAM", null, null, new Date());
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public DataAcquisitionMethod getAcquisitionMethod() {
		return acquisitionMethod;
	}

	public void setAcquisitionMethod(DataAcquisitionMethod acquisitionMethod) {
		this.acquisitionMethod = acquisitionMethod;
	}

	public DataExtractionMethod getDataExtractionMethod() {
		return dataExtractionMethod;
	}

	public void setDataExtractionMethod(DataExtractionMethod dataExtractionMethod) {
		this.dataExtractionMethod = dataExtractionMethod;
	}

	public Assay getAssay() {
		return assay;
	}
	
    @Override
    public boolean equals(Object obj) {

		if (obj == this)
			return true;

        if (obj == null)
            return false;

        if (!DataPipeline.class.isAssignableFrom(obj.getClass()))
            return false;

        final DataPipeline other = (DataPipeline) obj;
        
        if ((this.assay == null) ? (other.getAssay() != null) : !this.assay.equals(other.getAssay()))
            return false;
              
        if ((this.acquisitionMethod == null) ? (other.getAcquisitionMethod() != null) : 
        		!this.acquisitionMethod.equals(other.getAcquisitionMethod()))
            return false;
        
        if ((this.dataExtractionMethod == null) ? (other.getDataExtractionMethod() != null) : 
        		!this.dataExtractionMethod.equals(other.getDataExtractionMethod()))
        	return false;

        return true;
    }
    
    @Override
    public int hashCode() {

        int hash = 3;
        hash = 53 * hash + (this.name != null ? this.name.hashCode() : 0)
        		+ (this.assay != null ? this.assay.hashCode() : 0)
        		+ (this.acquisitionMethod != null ? this.acquisitionMethod.hashCode() : 0)
        		+ (this.dataExtractionMethod != null ? this.dataExtractionMethod.hashCode() : 0);
        return hash;
    }

	@Override
	public int compareTo(DataPipeline o) {
		
		int value = 0;
		if(this.assay != null && o.getAssay() != null)
			value = this.assay.getName().compareTo(o.getAssay().getName());
		
		if(value == 0 && this.acquisitionMethod != null) {
			
			if(o.getAcquisitionMethod() != null)
				value = this.getAcquisitionMethod().getName().compareTo(o.getAcquisitionMethod().getName());
			else
				return 1;
		}
		if(value == 0 && this.dataExtractionMethod != null) {
			
			if(o.getDataExtractionMethod() != null)
				value = this.getDataExtractionMethod().getName().
					compareTo(o.getDataExtractionMethod().getName());
			else
				return 1;
		}
		return value;
	}
	
	public String getCode() {
		
		ArrayList<String>codeParts = new ArrayList<String>();
		if(assay != null)
			codeParts.add(assay.getId());
		
		if(acquisitionMethod != null)
			codeParts.add(acquisitionMethod.getId());
		
		if(dataExtractionMethod != null)
			codeParts.add(dataExtractionMethod.getId());
		
		return StringUtils.join(codeParts, "_");
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
		
	public MoTrPACAssay getMotrpacAssay() {
		return motrpacAssay;
	}

	public void setMotrpacAssay(MoTrPACAssay motrpacAssay) {
		this.motrpacAssay = motrpacAssay;
	}
	
	@Override
	public String toString() {
		return name;
	}

	public void setAssay(Assay assay) {
		this.assay = assay;
	}

	@Override
	public Element getXmlElement() {
		// TODO Auto-generated method stub
		return null;
	}
}

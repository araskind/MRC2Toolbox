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
import java.text.ParseException;
import java.util.Date;

import org.jdom2.Element;

import edu.umich.med.mrc2.datoolbox.project.store.CommonFields;
import edu.umich.med.mrc2.datoolbox.project.store.InjectionFields;
import edu.umich.med.mrc2.datoolbox.project.store.ObjectNames;
import edu.umich.med.mrc2.datoolbox.utils.ProjectUtils;

public class Injection  implements Serializable, Comparable<Injection>{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8052899216236967926L;
	private String id;
	private String dataFileName;
	private Date timeStamp;
	private String prepItemId;
	private String acquisitionMethodId;
	private double injectionVolume;
	
	public Injection(
			String id, 
			String dataFileName, 
			Date timeStamp, 
			String prepItemId, 
			String acquisitionMethodId,
			double injectionVolume) {
		super();
		this.id = id;
		this.dataFileName = dataFileName;
		this.timeStamp = timeStamp;
		this.prepItemId = prepItemId;
		this.acquisitionMethodId = acquisitionMethodId;
		this.injectionVolume = injectionVolume;
	}

	public String getId() {
		return id;
	}

	public String getDataFileName() {
		return dataFileName;
	}

	public Date getTimeStamp() {
		return timeStamp;
	}

	public String getPrepItemId() {
		return prepItemId;
	}

	public String getAcquisitionMethodId() {
		return acquisitionMethodId;
	}

	public double getInjectionVolume() {
		return injectionVolume;
	}
	
   @Override
    public boolean equals(Object obj) {

		if (obj == this)
			return true;

        if (obj == null)
            return false;

        if (!Injection.class.isAssignableFrom(obj.getClass()))
            return false;

        final Injection other = (Injection) obj;

        if ((this.id == null) ? (other.getId() != null) : !this.id.equals(other.getId()))
            return false;

        return true;
    }

    @Override
    public int hashCode() {

        int hash = 3;
        hash = 53 * hash + (this.id != null ? this.id.hashCode() : 0);
        return hash;
    }

	@Override
	public int compareTo(Injection o) {
		return this.id.compareTo(o.getId());
	}
	
	public Element getXmlElement() {
		
		Element injectionElement = 
        		new Element(ObjectNames.Injection.name());
		injectionElement.setAttribute(CommonFields.Id.name(), id);	
		
		if(dataFileName != null)
			injectionElement.setAttribute(
					InjectionFields.DataFile.name(), dataFileName);	
		
		if(timeStamp != null)
			injectionElement.setAttribute(InjectionFields.Timestamp.name(), 
					ProjectUtils.dateTimeFormat.format(timeStamp));
		
		if(prepItemId != null)
			injectionElement.setAttribute(
					InjectionFields.PrepItem.name(), prepItemId);	

		if(acquisitionMethodId != null)
			injectionElement.setAttribute(
					InjectionFields.AcqMethod.name(), acquisitionMethodId);
		
		injectionElement.setAttribute(
				InjectionFields.InjVolume.name(), Double.toString(injectionVolume));
	
		return injectionElement;
	}

	public Injection(Element injectionElement) {
		
		super();
		id = injectionElement.getAttributeValue(CommonFields.Id.name());
		
		//	TMP fix for old name
		if(id == null)
			injectionElement.getAttributeValue("InjId");
			
		dataFileName = injectionElement.getAttributeValue(
				InjectionFields.DataFile.name());
		String injTime = 
				injectionElement.getAttributeValue(InjectionFields.Timestamp.name());
		if(injTime != null) {
			try {
				timeStamp = ProjectUtils.dateTimeFormat.parse(injTime);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		prepItemId = injectionElement.getAttributeValue(
				InjectionFields.PrepItem.name());
		acquisitionMethodId = injectionElement.getAttributeValue(
				InjectionFields.AcqMethod.name());
		String injVol = 
				injectionElement.getAttributeValue(InjectionFields.InjVolume.name());
		if(injVol != null)
			injectionVolume = Double.parseDouble(injVol);
	}
}

















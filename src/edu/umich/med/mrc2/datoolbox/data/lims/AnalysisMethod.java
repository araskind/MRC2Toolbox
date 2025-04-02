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

import org.jdom2.Document;
import org.jdom2.Element;

import edu.umich.med.mrc2.datoolbox.project.store.XmlStorable;

public class AnalysisMethod implements Serializable, Comparable<AnalysisMethod>, XmlStorable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -4868124576297836544L;
	protected String id;
	protected String name;
	protected DataProcessingSoftware software;
	protected String md5;
	
	public AnalysisMethod(String id, String name) {
		super();
		this.id = id;
		this.name = name;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public DataProcessingSoftware getSoftware() {
		return software;
	}

	public void setSoftware(DataProcessingSoftware software) {
		this.software = software;
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	@Override
	public int compareTo(AnalysisMethod o) {
		return id.compareTo(o.getId());
	}
	
    @Override
    public int hashCode() {

        int hash = 3;
        hash = 53 * hash + (this.id != null ? this.id.hashCode() : 0);
        return hash;
    }

	public String getMd5() {
		return md5;
	}

	public void setMd5(String md5) {
		this.md5 = md5;
	}

	@Override
	public Element getXmlElement(Document parentDocument) {
		// TODO Auto-generated method stub
		return null;
	}
}

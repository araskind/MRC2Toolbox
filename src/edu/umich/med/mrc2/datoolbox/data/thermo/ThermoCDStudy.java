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

package edu.umich.med.mrc2.datoolbox.data.thermo;

import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;

import edu.umich.med.mrc2.datoolbox.data.lims.LIMSUser;

public class ThermoCDStudy implements Comparable<ThermoCDStudy> {

	private String id;
	private String name;
	private String description;
	private LIMSUser owner;
	private Date dateCreated;
	private Date lastModified;
	private Map<ThermoCDSample, Collection<ThermoCDRawDatFile>>sampleFileMap;
	private Collection<ThermoCDWorkflow>workflows;
	
	public ThermoCDStudy(String name) {
		super();
		this.name = name;
		id = UUID.randomUUID().toString();
		sampleFileMap = new TreeMap<ThermoCDSample, Collection<ThermoCDRawDatFile>>();
		workflows = new TreeSet<ThermoCDWorkflow>();
	}
	
	public void addSample(ThermoCDSample sample) {
		sampleFileMap.put(sample, new TreeSet<ThermoCDRawDatFile>());
	}
	
	public void addFileForSample(ThermoCDRawDatFile file, String fileSetId) {
		
		ThermoCDSample sample = sampleFileMap.keySet().stream().
				filter(s -> s.getFileSetId().equals(fileSetId)).
				findFirst().orElse(null);
		if(sample != null)
			sampleFileMap.get(sample).add(file);
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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public LIMSUser getOwner() {
		return owner;
	}

	public void setOwner(LIMSUser owner) {
		this.owner = owner;
	}

	public Date getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	public Date getLastModified() {
		return lastModified;
	}

	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}

	public Map<ThermoCDSample, Collection<ThermoCDRawDatFile>> getSampleFileMap() {
		return sampleFileMap;
	}
	
	@Override
	public int compareTo(ThermoCDStudy o) {
		return id.compareTo(o.getId());
	}
	
    @Override
    public boolean equals(Object obj) {

		if (obj == this)
			return true;

        if (obj == null)
            return false;

        if (!ThermoCDStudy.class.isAssignableFrom(obj.getClass()))
            return false;

        final ThermoCDStudy other = (ThermoCDStudy) obj;

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
    
    public ThermoCDRawDatFile getFileByName(String fileName) {
    	return sampleFileMap.values().stream().
    			flatMap(v -> v.stream()).filter(f -> f.getName().equals(fileName)).
    			findFirst().orElse(null);
    }

	public Collection<ThermoCDWorkflow> getWorkflows() {
		return workflows;
	}
	
	public void addWorkflow(ThermoCDWorkflow workflow) {
		workflows.add(workflow);
	}
}

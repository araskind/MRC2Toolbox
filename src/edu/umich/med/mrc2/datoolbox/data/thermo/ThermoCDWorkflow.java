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

package edu.umich.med.mrc2.datoolbox.data.thermo;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

public class ThermoCDWorkflow  implements Comparable<ThermoCDWorkflow> {

	private String id;
	private int analysisWorkflowId;
	private int workflowLevel;
	private String name;
	private String description;
	private Date startDate;
	private int workflowVersion;
	private String softwareVersion;
	private String workflowXml;
	private String machineName;
	private String workflowType;
	private String study;
	private String analysisId;
	private Map<ThermoCDRawDatFile,Integer>fileIdMap;
	private Map<ThermoCDRawDatFile,Collection<ThermoMSFeature>>detectedFeatures;
	
	public ThermoCDWorkflow() {
		super();
		id = UUID.randomUUID().toString();
		fileIdMap = new TreeMap<ThermoCDRawDatFile,Integer>();
		detectedFeatures = new TreeMap<ThermoCDRawDatFile,Collection<ThermoMSFeature>>();
	}
	
	@Override
	public int compareTo(ThermoCDWorkflow o) {
		return id.compareTo(o.getId());
	}
	
    @Override
    public boolean equals(Object obj) {

		if (obj == this)
			return true;

        if (obj == null)
            return false;

        if (!ThermoCDWorkflow.class.isAssignableFrom(obj.getClass()))
            return false;

        final ThermoCDWorkflow other = (ThermoCDWorkflow) obj;

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

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getAnalysisWorkflowId() {
		return analysisWorkflowId;
	}

	public void setAnalysisWorkflowId(int analysisWorkflowId) {
		this.analysisWorkflowId = analysisWorkflowId;
	}

	public int getWorkflowLevel() {
		return workflowLevel;
	}

	public void setWorkflowLevel(int workflowLevel) {
		this.workflowLevel = workflowLevel;
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

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public int getWorkflowVersion() {
		return workflowVersion;
	}

	public void setWorkflowVersion(int workflowVersion) {
		this.workflowVersion = workflowVersion;
	}

	public String getSoftwareVersion() {
		return softwareVersion;
	}

	public void setSoftwareVersion(String softwareVersion) {
		this.softwareVersion = softwareVersion;
	}

	public String getWorkflowXml() {
		return workflowXml;
	}

	public void setWorkflowXml(String workflowXml) {
		this.workflowXml = workflowXml;
	}

	public String getMachineName() {
		return machineName;
	}

	public void setMachineName(String machineName) {
		this.machineName = machineName;
	}

	public String getWorkflowType() {
		return workflowType;
	}

	public void setWorkflowType(String workflowType) {
		this.workflowType = workflowType;
	}

	public String getStudy() {
		return study;
	}

	public void setStudy(String study) {
		this.study = study;
	}

	public String getAnalysisId() {
		return analysisId;
	}

	public void setAnalysisId(String analysisId) {
		this.analysisId = analysisId;
	}

	public Map<ThermoCDRawDatFile, Integer> getFileIdMap() {
		return fileIdMap;
	}
	
	public Collection<ThermoCDRawDatFile>getFiles(){
		return fileIdMap.keySet();
	}
	
	public void addDataFile(ThermoCDRawDatFile file, int fileId) {
		fileIdMap.put(file, fileId);
		detectedFeatures.put(file, new HashSet<ThermoMSFeature>());
	}
	
	public Collection<ThermoMSFeature>getFeaturesForFile(ThermoCDRawDatFile file){
		return detectedFeatures.get(file);
	}
	
	public void addFeaturesForFile(ThermoCDRawDatFile file, Collection<ThermoMSFeature>featuresToAdd){
		detectedFeatures.get(file).addAll(featuresToAdd);
	}

	public Map<ThermoCDRawDatFile, Collection<ThermoMSFeature>> getDetectedFeatures() {
		return detectedFeatures;
	}
}















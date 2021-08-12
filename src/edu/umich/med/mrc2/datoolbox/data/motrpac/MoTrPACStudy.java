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

package edu.umich.med.mrc2.datoolbox.data.motrpac;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import edu.umich.med.mrc2.datoolbox.data.lims.LIMSExperiment;

public class MoTrPACStudy implements Serializable, Comparable<MoTrPACStudy>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5882078352237889962L;
	private String id;
	private String code;
	private MotracSubjectType subjectType;
	private String description;
	private Collection<LIMSExperiment>experiments;
	private Collection<MoTrPACAssay>assays;
	private Map<LIMSExperiment, Collection<MoTrPACTissueCode>>experimentTissueMap;
	
	public MoTrPACStudy(String id, String code, MotracSubjectType subjectType, String description) {
		super();
		this.id = id;
		this.code = code;
		this.subjectType = subjectType;
		this.description = description;
		experiments = new TreeSet<LIMSExperiment>();
		assays = new TreeSet<MoTrPACAssay>();
		experimentTissueMap = new TreeMap<LIMSExperiment, Collection<MoTrPACTissueCode>>();
	}
	
	public MoTrPACStudy(MoTrPACStudy toCopy) {
		super();
		this.id = toCopy.getId();
		this.code = toCopy.getCode();
		this.subjectType = toCopy.getSubjectType();
		this.description = toCopy.getDescription();
		experiments = new TreeSet<LIMSExperiment>();
		experiments.addAll(toCopy.getExperiments());
		assays = new TreeSet<MoTrPACAssay>();
		assays.addAll(toCopy.getAssays());
		experimentTissueMap = new TreeMap<LIMSExperiment, Collection<MoTrPACTissueCode>>();
		for(LIMSExperiment experiment : toCopy.getExperiments()) {
			experimentTissueMap.put(experiment, new TreeSet<MoTrPACTissueCode>());
			experimentTissueMap.get(experiment).addAll(toCopy.getTissueCodesForExperiment(experiment));			
		}
	}

	@Override
	public int compareTo(MoTrPACStudy o) {
		return id.compareTo(o.getId());
	}

	/**
	 * @return the sampleType
	 */
	public String getCode() {
		return code;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	
	@Override
	public String toString() {
		return code + " - " + description;
	}
	
    @Override
    public boolean equals(Object obj) {

		if (obj == this)
			return true;

        if (obj == null)
            return false;

        if (!MoTrPACStudy.class.isAssignableFrom(obj.getClass()))
            return false;

        final MoTrPACStudy other = (MoTrPACStudy) obj;

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

	/**
	 * @return the subject
	 */
	public MotracSubjectType getSubjectType() {
		return subjectType;
	}

	/**
	 * @param subject the subject to set
	 */
	public void setSubjectType(MotracSubjectType subjectType) {
		this.subjectType = subjectType;
	}
	
	public Collection<LIMSExperiment> getExperiments() {
		return experiments;
	}
	
	public void addExperiment(LIMSExperiment experiment) {
		experiments.add(experiment);		
		experimentTissueMap.put(experiment, new TreeSet<MoTrPACTissueCode>());
	}
	
	public void removeExperiment(LIMSExperiment experiment) {
		experiments.remove(experiment);
		experimentTissueMap.remove(experiment);
	}
	
	public Collection<MoTrPACAssay>getAssays(){
		return assays;
	}
	
	public void addAssay(MoTrPACAssay assay) {
		assays.add(assay);
	}
	
	public void removeAssay(MoTrPACAssay assay) {
		assays.remove(assay);
	}
	
	public Collection<MoTrPACTissueCode>getTissueCodesForExperiment(LIMSExperiment experiment){
		return experimentTissueMap.get(experiment);
	}
	
	public void addTissueForExperiment(LIMSExperiment experiment, MoTrPACTissueCode tissue) {	
		
		if(experimentTissueMap.get(experiment) != null)
			experimentTissueMap.get(experiment).add(tissue);
	}
	
	public void removeTissueFromExperiment(LIMSExperiment experiment, MoTrPACTissueCode tissue) {	
		
		if(experimentTissueMap.get(experiment) != null)
			experimentTissueMap.get(experiment).remove(tissue);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setCode(String code) {
		this.code = code;
	}
}









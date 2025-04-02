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
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.jdom2.Element;

import edu.umich.med.mrc2.datoolbox.data.enums.MoTrPACQCSampleType;
import edu.umich.med.mrc2.datoolbox.data.lims.DataAcquisitionMethod;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCache;
import edu.umich.med.mrc2.datoolbox.main.ReferenceSamplesManager;
import edu.umich.med.mrc2.datoolbox.project.RawDataAnalysisExperiment;
import edu.umich.med.mrc2.datoolbox.project.store.CommonFields;
import edu.umich.med.mrc2.datoolbox.project.store.ExperimentalSampleFields;
import edu.umich.med.mrc2.datoolbox.project.store.ObjectNames;

public class ExperimentalSample implements Comparable<ExperimentalSample>, Serializable, Renamable {

	/**
	 *
	 */
	private static final long serialVersionUID = 7684460196045087473L;
	
	protected String id;
	protected String name;
	protected String limsSampleType;
	protected boolean enabled;
	protected String sampleIdDeprecated;
	protected String sampleNameDeprecated;
	protected TreeMap<ExperimentDesignFactor, ExperimentDesignLevel> designCell;
	protected TreeMap<DataAcquisitionMethod, TreeSet<DataFile>> dataFilesMap;
	protected int batchNumber;
	protected boolean nameIsValid;
	protected boolean lockedReference = false;
	protected MoTrPACQCSampleType moTrPACQCSampleType;
	protected boolean incloodeInPoolStats;

	public ExperimentalSample(String sampleId2, String sampleName2) {

		id = sampleId2;
		name = sampleName2;
		designCell = new TreeMap<ExperimentDesignFactor, ExperimentDesignLevel>();
		designCell.put(
				ReferenceSamplesManager.getSampleControlTypeFactor(),
				ReferenceSamplesManager.sampleLevel);
		dataFilesMap = new TreeMap<DataAcquisitionMethod, TreeSet<DataFile>>();
		enabled = true;
		batchNumber = 1;
		incloodeInPoolStats = false;
	}
	
	public ExperimentalSample(ExperimentalSample sample) {
		
		id = sample.getId();
		name = sample.getName();
		designCell = new TreeMap<ExperimentDesignFactor, ExperimentDesignLevel>();
		designCell.putAll(sample.getDesignCell());
		dataFilesMap = new TreeMap<DataAcquisitionMethod, TreeSet<DataFile>>();
		dataFilesMap.putAll(sample.getDataFilesMap());
		enabled = sample.isEnabled();
		batchNumber = sample.getBatchNumber();
		incloodeInPoolStats = sample.isIncloodeInPoolStats();
	}

	public void addDataFile(DataFile newDataFile) {

		DataAcquisitionMethod fileMethod = newDataFile.getDataAcquisitionMethod();
		if (!dataFilesMap.containsKey(fileMethod))
			dataFilesMap.put(fileMethod, new TreeSet<DataFile>());

		dataFilesMap.get(fileMethod).add(newDataFile);
		newDataFile.setParentSample(this);
	}

	public void addDesignLevel(ExperimentDesignLevel level) {

		if(level != null)
			designCell.put(level.getParentFactor(), level);
	}

	@Override
	public int compareTo(ExperimentalSample es) {
		return this.id.compareTo(es.getId());
	}

	public int getBatchNumber() {
		return batchNumber;
	}

	public DataFile[] getDataFiles() {

		return dataFilesMap.values().toArray(new DataFile[dataFilesMap.size()]);
	}

	public TreeSet<DataFile> getDataFilesForMethod(DataAcquisitionMethod method) {

		if(dataFilesMap.get(method) == null)
			return new TreeSet<DataFile>();
		else
			return dataFilesMap.get(method);
	}

	public DataFile[] getDataFileArrayForMethod(DataAcquisitionMethod method) {

		TreeSet<DataFile> methodFiles = dataFilesMap.get(method);
		if(methodFiles == null)
			return new DataFile[0];
		else
			return methodFiles.toArray(new DataFile[methodFiles.size()]);
	}

	public TreeMap<DataAcquisitionMethod, TreeSet<DataFile>> getDataFilesMap() {
		return dataFilesMap;
	}

	public TreeMap<ExperimentDesignFactor, ExperimentDesignLevel> getDesignCell() {
		return designCell;
	}

	public ExperimentDesignLevel getLevel(ExperimentDesignFactor factor) {

		if(getSampleType().equals(ReferenceSamplesManager.sampleLevel))
			return designCell.get(factor);
		else
			return getSampleType();
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public ExperimentDesignLevel getSampleType() {

		if(designCell.get(ReferenceSamplesManager.getSampleControlTypeFactor()) == null) {
			designCell.put(
					ReferenceSamplesManager.getSampleControlTypeFactor(),
					ReferenceSamplesManager.sampleLevel);
		}
		return designCell.get(ReferenceSamplesManager.getSampleControlTypeFactor());
	}

	public boolean hasDataAcquisitionMethod(DataAcquisitionMethod method) {
		return dataFilesMap.containsKey(method);
	}

	public boolean hasDataFile(DataFile file) {
		
		if(dataFilesMap.get(file.getDataAcquisitionMethod()) == null)
			return false;
		
		return dataFilesMap.get(file.getDataAcquisitionMethod()).stream().
				filter(f -> f.equals(file)).
				findFirst().isPresent();
	}

	public boolean hasFactor(ExperimentDesignFactor ef) {
		return designCell.containsKey(ef);
	}

	public boolean hasLevel(ExperimentDesignLevel level) {

		if(level == null)
			return false;

		if(designCell.get(level.getParentFactor()) == null)
			return false;

		return designCell.get(level.getParentFactor()).equals(level);
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void removeDataFile(DataFile df) {

		DataAcquisitionMethod am = df.getDataAcquisitionMethod();
		if (!dataFilesMap.containsKey(am))
			return;

		dataFilesMap.get(am).remove(df);
		df.setParentSample(null);
	}

	public void setBatchNumber(int batchNumber) {
		this.batchNumber = batchNumber;
	}

	public void setEnabled(boolean enabled) {

		this.enabled = enabled;
		dataFilesMap.values().stream().
			flatMap(c -> c.stream()).
			forEach(f -> f.setEnabled(enabled));
	}

	public void setId(String sampleId) {
		this.id = sampleId;
	}

	public void setName(String sampleName) {
		this.name = sampleName;
	}

	@Override
	public String toString() {
		return name;
	}

	//	This method will not replace data for existing factors, only add new ones
	public void appendDesign(TreeMap<ExperimentDesignFactor, ExperimentDesignLevel> designCell2) {

		for (Entry<ExperimentDesignFactor, ExperimentDesignLevel> entry : designCell2.entrySet()) {

			if(!designCell.containsKey(entry.getKey()))
				designCell.put(entry.getKey(), entry.getValue());
		}
	}

	@Override
	public boolean nameIsValid() {
		return nameIsValid;
	}

	@Override
	public void setNameValid(boolean valid) {
		nameIsValid = valid;
	}

	public void replaceDataAcquisitionMethod(
			DataAcquisitionMethod oldMethod, 
			DataAcquisitionMethod newMethod) {
		TreeSet<DataFile> files = dataFilesMap.remove(oldMethod);
		dataFilesMap.put(newMethod, files);
	}

	/**
	 * @return the limsSampleType
	 */
	public String getLimsSampleType() {
		return limsSampleType;
	}

	/**
	 * @param limsSampleType the limsSampleType to set
	 */
	public void setLimsSampleType(String limsSampleType) {
		this.limsSampleType = limsSampleType;
	}

    @Override
    public boolean equals(Object obj) {

		if (obj == this)
			return true;

        if (obj == null)
            return false;

        if (!ExperimentalSample.class.isAssignableFrom(obj.getClass()))
            return false;

        final ExperimentalSample other = (ExperimentalSample) obj;

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

    public Collection<DataFile> removeAssay(Assay assay) {
    	return dataFilesMap.remove(assay);
    }

	/**
	 * @return the lockedReference
	 */
	public boolean isLockedReference() {
		return lockedReference;
	}

	/**
	 * @param lockedReference the lockedReference to set
	 */
	public void setLockedReference(boolean lockedReference) {
		this.lockedReference = lockedReference;
	}

	public void clearDataFileAssignment() {

		dataFilesMap.values().stream().
			flatMap(m -> m.stream()).
			forEach(f -> f.setParentSample(null));
		dataFilesMap.clear();
	}

	/**
	 * @return the moTrPACQCSampleType
	 */
	public MoTrPACQCSampleType getMoTrPACQCSampleType() {
		return moTrPACQCSampleType;
	}

	/**
	 * @param moTrPACQCSampleType the moTrPACQCSampleType to set
	 */
	public void setMoTrPACQCSampleType(MoTrPACQCSampleType moTrPACQCSampleType) {
		this.moTrPACQCSampleType = moTrPACQCSampleType;
	}

	/**
	 * @return the sampleIdDeprecated
	 */
	public String getSampleIdDeprecated() {
		return sampleIdDeprecated;
	}

	/**
	 * @return the sampleNameDeprecated
	 */
	public String getSampleNameDeprecated() {
		return sampleNameDeprecated;
	}

	/**
	 * @param sampleIdDeprecated the sampleIdDeprecated to set
	 */
	public void setSampleIdDeprecated(String sampleIdDeprecated) {
		this.sampleIdDeprecated = sampleIdDeprecated;
	}

	/**
	 * @param sampleNameDeprecated the sampleNameDeprecated to set
	 */
	public void setSampleNameDeprecated(String sampleNameDeprecated) {
		this.sampleNameDeprecated = sampleNameDeprecated;
	}

	public boolean isIncloodeInPoolStats() {
		return incloodeInPoolStats;
	}

	public void setIncloodeInPoolStats(boolean incloodeInPoolStats) {
		this.incloodeInPoolStats = incloodeInPoolStats;
	}
	
	public Element getXmlElement() {
		
		Element sampleElement = 
				new Element(ObjectNames.ExperimentalSample.name());
		
		if(id != null)
			sampleElement.setAttribute(CommonFields.Id.name(), id);
		
		if(name != null)
			sampleElement.setAttribute(CommonFields.Name.name(), name);
		
		if(limsSampleType != null)
			sampleElement.setAttribute(
					ExperimentalSampleFields.LimsSampleType.name(), limsSampleType);

		sampleElement.setAttribute(
				ExperimentalSampleFields.Enabled.name(), Boolean.toString(enabled));
		
		sampleElement.setAttribute(
				ExperimentalSampleFields.LockedReference.name(), Boolean.toString(lockedReference));
		
		sampleElement.setAttribute(
				ExperimentalSampleFields.IncloodeInPoolStats.name(), Boolean.toString(incloodeInPoolStats));

		sampleElement.setAttribute(
				ExperimentalSampleFields.BatchNum.name(), Integer.toString(batchNumber));

		if(moTrPACQCSampleType != null)
			sampleElement.setAttribute(
					ExperimentalSampleFields.MoTrPACQCSampleType.name(), moTrPACQCSampleType.name());
		
		Element designCellContainerElement = 
				new Element(ExperimentalSampleFields.DesignCell.name());
		
		for(Entry<ExperimentDesignFactor, ExperimentDesignLevel> dce : designCell.entrySet()) {
			
			Element designCellElement = 
					new Element(ExperimentalSampleFields.DesignCellElement.name());
			designCellElement.setAttribute(ExperimentalSampleFields.DCFactorId.name(), 
					dce.getKey().getFactorId());
			designCellElement.setAttribute(ExperimentalSampleFields.DCLevelId.name(), 
					dce.getValue().getLevelId());
			designCellContainerElement.addContent(designCellElement);
		}
		sampleElement.addContent(designCellContainerElement);

//		protected TreeMap<DataAcquisitionMethod, TreeSet<DataFile>> dataFilesMap;
		Element dataFileMapContainerElement = 
				new Element(ExperimentalSampleFields.DataFileMap.name());
		for(Entry<DataAcquisitionMethod, TreeSet<DataFile>> dfme : dataFilesMap.entrySet()) {
			
			Element dataFileMapElement = 
					new Element(ExperimentalSampleFields.DataFileMapElement.name());
			dataFileMapElement.setAttribute(ExperimentalSampleFields.AcqMethodId.name(), 
					dfme.getKey().getId());
			
			List<String> fNames = dfme.getValue().stream().
					map(df -> df.getName()).collect(Collectors.toList());
			dataFileMapElement.setText(StringUtils.join(fNames, ","));
			dataFileMapContainerElement.addContent(dataFileMapElement);
		}
		sampleElement.addContent(dataFileMapContainerElement);
		return sampleElement;
	}
	
	public ExperimentalSample(
			Element sampleElement, 
			ExperimentDesign design,
			RawDataAnalysisExperiment parentProject) {		
		id = sampleElement.getAttributeValue(CommonFields.Id.name());
		name = sampleElement.getAttributeValue(CommonFields.Name.name());
		limsSampleType = 
				sampleElement.getAttributeValue(ExperimentalSampleFields.LimsSampleType.name());
		enabled = Boolean.parseBoolean(
				sampleElement.getAttributeValue(ExperimentalSampleFields.Enabled.name()));
		lockedReference = Boolean.parseBoolean(
				sampleElement.getAttributeValue(ExperimentalSampleFields.LockedReference.name()));
		incloodeInPoolStats = Boolean.parseBoolean(
				sampleElement.getAttributeValue(ExperimentalSampleFields.IncloodeInPoolStats.name()));
		
		String batchString = 
				sampleElement.getAttributeValue(ExperimentalSampleFields.BatchNum.name());
		if(batchString != null)
			batchNumber = Integer.parseInt(batchString);
		else
			batchNumber = 1;
		
		if(sampleElement.getAttributeValue(ExperimentalSampleFields.MoTrPACQCSampleType.name()) != null)
			moTrPACQCSampleType = MoTrPACQCSampleType.valueOf(
					sampleElement.getAttributeValue(ExperimentalSampleFields.MoTrPACQCSampleType.name()));
		
		designCell = new TreeMap<ExperimentDesignFactor, ExperimentDesignLevel>();
		List<Element> designCellElements = 
				sampleElement.getChild(ExperimentalSampleFields.DesignCell.name()).getChildren();
		for(Element dcElement : designCellElements) {
			String factorId = dcElement.getAttributeValue(ExperimentalSampleFields.DCFactorId.name());
			String levelId = dcElement.getAttributeValue(ExperimentalSampleFields.DCLevelId.name());
			ExperimentDesignFactor factor = design.getFactorById(factorId);
			ExperimentDesignLevel level = null;
			if(factor != null)
				level = factor.getLevelById(levelId);
			
			if(factor != null && level != null)
				designCell.put(factor, level);
		}
		dataFilesMap = new TreeMap<DataAcquisitionMethod, TreeSet<DataFile>>();
		List<Element> dfMapElements = 
				sampleElement.getChild(ExperimentalSampleFields.DataFileMap.name()).getChildren();
		
		for(Element dfmElement : dfMapElements) {
			
			String daqMethodId = 
					dfmElement.getAttributeValue(ExperimentalSampleFields.AcqMethodId.name());
			DataAcquisitionMethod method = 
					IDTDataCache.getAcquisitionMethodById(daqMethodId);
			String[] fileNames = dfmElement.getText().split(",");			
			if(method != null && fileNames.length > 0) {
				
				TreeSet<DataFile>methodFiles = new TreeSet<DataFile>();
				for(String fName : fileNames) {
					
					DataFile df = parentProject.getDataFileByName(fName);
					if(df != null)
						methodFiles.add(df);
				}
				dataFilesMap.put(method, methodFiles);
			}
		}
	}
}











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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.jdom2.Element;

import edu.umich.med.mrc2.datoolbox.data.compare.NameComparator;
import edu.umich.med.mrc2.datoolbox.data.enums.GlobalDefaults;
import edu.umich.med.mrc2.datoolbox.data.enums.ParameterSetStatus;
import edu.umich.med.mrc2.datoolbox.data.enums.StandardFactors;
import edu.umich.med.mrc2.datoolbox.data.lims.DataAcquisitionMethod;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.database.idt.ReferenceSamplesManager;
import edu.umich.med.mrc2.datoolbox.gui.communication.ExperimentDesignEvent;
import edu.umich.med.mrc2.datoolbox.gui.communication.ExperimentDesignFactorEvent;
import edu.umich.med.mrc2.datoolbox.gui.communication.ExperimentDesignFactorListener;
import edu.umich.med.mrc2.datoolbox.gui.communication.ExperimentDesignListener;
import edu.umich.med.mrc2.datoolbox.project.RawDataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.project.store.ExperimentDesignFields;
import edu.umich.med.mrc2.datoolbox.project.store.IDTExperimentalSampleFields;

public class ExperimentDesign implements ExperimentDesignFactorListener, Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 4767740368780330627L;

	protected TreeSet<ExperimentDesignFactor> factorSet;
	protected ExperimentDesignFactor sampleTypeFactor;
	protected TreeSet<ExperimentalSample> sampleSet;	
	protected TreeSet<ExperimentDesignSubset> designSubsets;
	
	protected ParameterSetStatus designStatus;
	protected Set<ExperimentDesignListener> eventListeners;
	protected boolean suppressEvents;
	
	public ExperimentDesign() {

		super();
		factorSet = new TreeSet<ExperimentDesignFactor>();
		sampleSet = new TreeSet<ExperimentalSample>();
		suppressEvents = false;
		eventListeners = ConcurrentHashMap.newKeySet();
		initCleanDesign();
	}

	private void initCleanDesign() {

		//	Add Sample/control type factor by default
		sampleTypeFactor = new ExperimentDesignFactor(
				StandardFactors.SAMPLE_CONTROL_TYPE.getName());
		sampleTypeFactor.addLevel(
				new ExperimentDesignLevel(ReferenceSamplesManager.REGULAR_SAMPLE));
		sampleTypeFactor.addListener(this);
		factorSet.add(sampleTypeFactor);
		designSubsets  = new TreeSet<ExperimentDesignSubset>();

		//	Create new "All levels" set and unlocked copy
		ExperimentDesignSubset allLevels = 
				new ExperimentDesignSubset(GlobalDefaults.ALL_SAMPLES.getName());
		factorSet.stream().forEach(f -> allLevels.addFactor(f));
		allLevels.setLocked(true);
		allLevels.setActive(false);
		designSubsets.add(allLevels);
		updateCompleteDesignSubsets();
	}

	public void updateCompleteDesignSubsets() {

		String unlockedSetName = 
				GlobalDefaults.ALL_SAMPLES.getName() + GlobalDefaults.UNLOCKED_SUFFIX;
		ExperimentDesignSubset allLevelsUnlocked = 
				designSubsets.stream().
				filter(s -> s.getName().equals(unlockedSetName)).
				findFirst().orElse(null);

		if(allLevelsUnlocked == null) { 
			allLevelsUnlocked = new ExperimentDesignSubset(unlockedSetName);
			designSubsets.add(allLevelsUnlocked);
		}
		allLevelsUnlocked.copyDesignSubset(getCompleteDesignSubset());
		allLevelsUnlocked.setLocked(false);		
		setActiveDesignSubset(allLevelsUnlocked);
	}

	public boolean isEmpty() {

		if (factorSet.size() == 1 && factorSet.contains(
				ReferenceSamplesManager.getSampleControlTypeFactor()) && sampleSet.isEmpty())
			return true;
		else
			return false;
	}

	public void clearDesign() {

		factorSet.clear();
		designSubsets.clear();
		sampleSet.clear();
		initCleanDesign();
		fireExperimentDesignEvent(ParameterSetStatus.CHANGED);
	}

	public void addFactor(ExperimentDesignFactor newFactor) {

		factorSet.add(newFactor);
		newFactor.addListener(this);
		getCompleteDesignSubset().addFactor(newFactor);
		updateCompleteDesignSubsets();
		fireExperimentDesignEvent(ParameterSetStatus.CHANGED);
	}

	public void addLevel(ExperimentDesignLevel newLevel, ExperimentDesignFactor parentFactor) {

		parentFactor.setSuppressEvents(true);
		parentFactor.addLevel(newLevel);
		parentFactor.setSuppressEvents(false);
		getCompleteDesignSubset().addLevel(newLevel);
		updateCompleteDesignSubsets();
		fireExperimentDesignEvent(ParameterSetStatus.CHANGED);
	}

	// TODO	This is a quick fix for handling reference samples
	public void replaceSampleTypeFactor(ExperimentDesignFactor newSampleTypeFactor) {

		if(!newSampleTypeFactor.getName().equals(StandardFactors.SAMPLE_CONTROL_TYPE.getName()))
			return;

		if(sampleTypeFactor == null)
			sampleTypeFactor = new ExperimentDesignFactor(StandardFactors.SAMPLE_CONTROL_TYPE.getName());

		factorSet.remove(sampleTypeFactor);
		sampleTypeFactor = newSampleTypeFactor;
		factorSet.add(sampleTypeFactor);
		updateCompleteDesignSubsets();
	}

	public void addSample(ExperimentalSample newSample) {

		sampleSet.add(newSample);
		fireExperimentDesignEvent(ParameterSetStatus.CHANGED);
	}

	public void addSamples(Collection<? extends ExperimentalSample> newSamples) {

		sampleSet.addAll(newSamples);
		fireExperimentDesignEvent(ParameterSetStatus.CHANGED);
	}

	public void addReferenceSamples(Collection<? extends ExperimentalSample> samples2add) {

		setSuppressEvents(true);
		for(ExperimentalSample rs : samples2add) {

			ExperimentDesignLevel sampleTypeLevel = rs.getDesignCell().get(sampleTypeFactor);
			if(sampleTypeLevel != null) {
				sampleTypeFactor.addLevel(sampleTypeLevel);
				getCompleteDesignSubset().addLevel(sampleTypeLevel);
			}
		}
		addSamples(samples2add);
		updateCompleteDesignSubsets();
		setSuppressEvents(false);
		setSuppressEvents(true);
	}

	public void appendDesign(ExperimentDesign extraDesign) {

		setSuppressEvents(true);

		// Add factors/levels
		for (ExperimentDesignFactor newFactor : extraDesign.getFactors()) {

			ExperimentDesignFactor existingFactor = getFactorByName(newFactor.getName());
			if(existingFactor == null) {
				factorSet.add(newFactor);
			}
			else {
				// Add levels if not present
				for (ExperimentDesignLevel newLevel : newFactor.getLevels()) {

					if(existingFactor.getLevelByName(newLevel.getName()) == null)
						existingFactor.addLevel(newLevel);
				}
			}
			//	Update design subsets
			designSubsets.remove(getCompleteDesignSubset());
			ExperimentDesignSubset allLevels = new ExperimentDesignSubset(GlobalDefaults.ALL_SAMPLES.getName());
			factorSet.stream().forEach(f -> allLevels.addFactor(f));
			allLevels.setLocked(true);
			allLevels.setActive(false);
			designSubsets.add(allLevels);
			updateCompleteDesignSubsets();
		}
		// Add samples
		for (ExperimentalSample newSample : extraDesign.getSamples()) {

			ExperimentalSample refSample = ReferenceSamplesManager.getReferenceSampleById(newSample.getId());
			ExperimentalSample existingSample = getSampleById(newSample.getId());
			if(existingSample != null && refSample == null) {
				existingSample.appendDesign(newSample.getDesignCell());
			}
			else {
				if(refSample == null) {
					sampleSet.add(newSample);
				}
				else {
					if(!sampleSet.contains(refSample))
						sampleSet.add(refSample);
				}
			}
		}
		//	Update global design subset
/*		for (ExperimentDesignSubset set : designSubsets) {

			if (set.getName().equals(GlobalDefaults.ALL_SAMPLES.getName())) {

				TreeSet<ExperimentDesignLevel> presentLevels = set.getDesignMap();
				List<ExperimentDesignLevel> allLevels =
						factorSet.stream().flatMap(f -> f.getLevels().stream()).collect(Collectors.toList());
				for(ExperimentDesignLevel l : allLevels) {

					if(!presentLevels.contains(l))
						set.addLevel(l);
				}
			}
		}*/
		setSuppressEvents(false);
		fireExperimentDesignEvent(ParameterSetStatus.CHANGED);
	}

	public void disableAllFactors() {

		for (ExperimentDesignFactor edf : factorSet) {

			edf.setEnabled(false);

			for (ExperimentDesignLevel l : edf.getLevels())
				l.setEnabled(false);
		}
	}

	public void enableAllFactors() {

		for (ExperimentDesignFactor edf : factorSet) {

			edf.setEnabled(true);

			for (ExperimentDesignLevel l : edf.getLevels())
				l.setEnabled(true);
		}
	}

	public TreeSet<ExperimentDesignFactor> getFactors() {
		return factorSet;
	}

	public ExperimentDesignFactor[] getOrderedFactors() {

		ExperimentDesignFactor[] fArray =
				factorSet.toArray(new ExperimentDesignFactor[factorSet.size()]);
		Arrays.sort(fArray, new NameComparator());

		return fArray;
	}

	public TreeSet<ExperimentalSample> getSamples() {
		return sampleSet;
	}

	public void removeFactor(ExperimentDesignFactor factor2remove) {

		factor2remove.removeListener(this);

		for(ExperimentalSample sample : sampleSet)
			sample.getDesignCell().remove(factor2remove);

		factorSet.remove(factor2remove);
		designSubsets.stream().forEach(s -> s.removeFactor(factor2remove));
		fireExperimentDesignEvent(ParameterSetStatus.CHANGED);
	}

	public void removeLevel(ExperimentDesignLevel level2remove) {

		if(level2remove.getParentFactor() == null)
			return;

		sampleSet.stream().forEach(s -> s.getDesignCell().remove(level2remove.getParentFactor()));
		level2remove.getParentFactor().setSuppressEvents(true);
		level2remove.getParentFactor().removeLevel(level2remove);
		level2remove.getParentFactor().setSuppressEvents(false);
		designSubsets.stream().forEach(s -> s.removeLevel(level2remove));
		fireExperimentDesignEvent(ParameterSetStatus.CHANGED);
	}

	public void fireExperimentDesignEvent() {

		if(eventListeners == null){
			eventListeners = ConcurrentHashMap.newKeySet();
			return;
		}
		if(!suppressEvents) {

			ExperimentDesignEvent event = new ExperimentDesignEvent(this);
			eventListeners.stream().forEach(l -> l.designStatusChanged(event));
		}
	}

	public void fireExperimentDesignEvent(ParameterSetStatus newStatus) {

		if(eventListeners == null){
			eventListeners = ConcurrentHashMap.newKeySet();
			return;
		}
		if(!suppressEvents) {

			ExperimentDesignEvent event = new ExperimentDesignEvent(this, newStatus);
			eventListeners.stream().forEach(l -> l.designStatusChanged(event));
		}
	}

	public void removeSample(ExperimentalSample sample2remove) {

		if(sampleSet.remove(sample2remove))
			fireExperimentDesignEvent(ParameterSetStatus.CHANGED);
	}

	public void removeSamples(Collection<? extends ExperimentalSample> samples2remove) {

		sampleSet.removeAll(samples2remove);
		fireExperimentDesignEvent(ParameterSetStatus.CHANGED);
	}

	public void removeReferenceSamples(Collection<? extends ExperimentalSample> samples2remove) {

		setSuppressEvents(true);
		for(ExperimentalSample rs : samples2remove) {

			ExperimentDesignLevel sampleTypeLevel = rs.getDesignCell().get(sampleTypeFactor);
			ExperimentalSample otherSample = 
					sampleSet.stream().filter(s -> s.getDesignCell().get(sampleTypeFactor).equals(sampleTypeLevel)).
					filter(s -> !s.equals(rs)).findFirst().orElse(null);

			if(otherSample == null && sampleTypeLevel != null) {
				sampleTypeFactor.removeLevel(sampleTypeLevel);
				getCompleteDesignSubset().removeLevel(sampleTypeLevel);
			}
		}
		removeSamples(samples2remove);
		updateCompleteDesignSubsets();
		setSuppressEvents(false);
		fireExperimentDesignEvent(ParameterSetStatus.CHANGED);
	}

	public ParameterSetStatus getStatus() {
		return designStatus;
	}

	public void setDesignStatus(ParameterSetStatus designStatus) {
		this.designStatus = designStatus;
	}

	public void setSuppressEvents(boolean suppressEvents) {
		this.suppressEvents = suppressEvents;
	}

	public void addListener(ExperimentDesignListener listener) {

		if(eventListeners == null)
			eventListeners = ConcurrentHashMap.newKeySet();

		eventListeners.add(listener);
	}

	public void removeListener(ExperimentDesignListener listener) {

		if(eventListeners == null)
			eventListeners = ConcurrentHashMap.newKeySet();

		eventListeners.remove(listener);
	}

	public Set<ExperimentDesignListener>getListeners(){

		if(eventListeners == null)
			eventListeners = ConcurrentHashMap.newKeySet();

		return eventListeners;
	}

	public void removeAllListeners() {
		eventListeners = ConcurrentHashMap.newKeySet();
	}

	public void setListeners(List<ExperimentDesignListener>newListeners){

		eventListeners = ConcurrentHashMap.newKeySet();
		eventListeners.addAll(newListeners);
	}

	public Collection<ExperimentalSample>getSamplesForDesignSubset(ExperimentDesignSubset subset){

		Collection<ExperimentalSample>subsetSamples  = new HashSet<ExperimentalSample>();
		TreeSet<ExperimentDesignLevel> levels = subset.getDesignMap();
		for(ExperimentalSample s : sampleSet) {

			if(!s.getDesignCell().values().stream().filter((e) -> levels.contains(e)).collect(Collectors.toList()).isEmpty())
				subsetSamples.add(s);
		}
		return subsetSamples;
	}

	public ExperimentalSample getSampleById(String sampleId) {
		return sampleSet.stream().
				filter(s -> s.getId().equals(sampleId)).
				findFirst().orElse(null);
	}

	public ExperimentalSample getSampleByName(String sampleName) {
		return sampleSet.stream().
				filter(s -> s.getName().equals(sampleName)).
				findFirst().orElse(null);
	}

	public ExperimentDesignFactor getFactorById(String factorId) {
		return factorSet.stream().
				filter(f -> f.getFactorId().equals(factorId)).
				findFirst().orElse(null);
	}

	public ExperimentDesignFactor getFactorByName(String name) {
		return factorSet.stream().
				filter(f -> f.getName().equals(name)).
				findFirst().orElse(null);
	}

	public ExperimentDesignLevel getLevelById(String levelId) {
		return factorSet.stream().flatMap(f -> f.getLevels().stream()).
				filter(l -> l.getLevelId().equals(levelId)).
				findFirst().orElse(null);
	}

	//	TODO	This may be ambiguous since same level name may be present in different factors
	public ExperimentDesignLevel getLevelByName(String name) {
		return factorSet.stream().flatMap(f -> f.getLevels().stream()).
				filter(l -> l.getName().equals(name)).
				findFirst().orElse(null);
	}

	public boolean containsSample(ExperimentalSample ref) {
		return sampleSet.contains(ref);
	}

	public void removeDataFiles(Collection<DataFile> selectedFiles) {
		sampleSet.stream().
			forEach(s -> selectedFiles.stream().forEach(f -> s.removeDataFile(f)));
	}

	public TreeSet<ExperimentDesignSubset> getDesignSubsets() {
		return designSubsets;
	}

	public void addDesignSubset(ExperimentDesignSubset subset) {
		designSubsets.add(subset);
	}

	public ExperimentDesignSubset getActiveDesignSubset() {
		return designSubsets.stream().
				filter(s -> s.isActive()).
				findFirst().orElse(null);
	}

	public void removeDesignSubset(ExperimentDesignSubset subset) {

		if (subset.isActive()) {

			for (ExperimentDesignSubset set : designSubsets) {

				if (set.getName().equals(GlobalDefaults.ALL_SAMPLES.getName()))
					set.setActive(true);
				else
					set.setActive(false);
			}
			fireExperimentDesignEvent(ParameterSetStatus.CHANGED);
		}
		designSubsets.remove(subset);
	}

	public void setActiveDesignSubset(ExperimentDesignSubset subset) {

		subset.setActive(true);

		if (!designSubsets.contains(subset))
			designSubsets.add(subset);

		designSubsets.stream().filter(s -> !s.equals(subset)).forEach(s -> s.setActive(false));

		fireExperimentDesignEvent(ParameterSetStatus.CHANGED);
	}

	public void replaceDesign(ExperimentDesign newDesign) {

		setSuppressEvents(true);
		clearDesign();
		newDesign.getSamples().stream().forEach(s -> s.clearDataFileAssignment());
		newDesign.getFactors().stream().forEach(f -> addFactor(f));
		for(ExperimentalSample sample : newDesign.getSamples()) {

			ExperimentalSample refSample = ReferenceSamplesManager.getReferenceSampleById(sample.getId());
			if(refSample == null) {
				sampleSet.add(sample);
			}
			else {
				if(!sampleSet.contains(refSample))
					sampleSet.add(refSample);
			}
		}
		sampleSet.stream().forEach(s -> s.clearDataFileAssignment());
		setSuppressEvents(false);
		fireExperimentDesignEvent(ParameterSetStatus.CHANGED);
	}

	public TreeSet<ExperimentalSample>getActiveSamplesForDesignSubset(ExperimentDesignSubset experimentDesignSubset){

		TreeSet<ExperimentDesignLevel> activeLevels = experimentDesignSubset.getDesignMap();
		return sampleSet.stream().
			filter(s -> s.isEnabled()).
			filter(s -> CollectionUtils.containsAny(s.getDesignCell().values(), activeLevels)).
			collect(Collectors.toCollection(TreeSet::new));
	}

	public TreeSet<ExperimentalSample>getActiveSamplesForDesignSubsetAndDataPipeline(
			DataPipeline pipeline,
			ExperimentDesignSubset experimentDesignSubset){
		
		DataAcquisitionMethod acqMethod = pipeline.getAcquisitionMethod();
		TreeSet<ExperimentDesignLevel> levels = experimentDesignSubset.getDesignMap();	
		return sampleSet.stream().
				filter(s -> s.isEnabled()).
				filter(s -> CollectionUtils.containsAny(s.getDesignCell().values(), levels)).
				filter(s -> s.getDataFilesMap().get(acqMethod) != null).
				filter(s -> !s.getDataFilesMap().get(acqMethod).isEmpty()).
				collect(Collectors.toCollection(TreeSet::new));
	}

	public ExperimentDesignSubset getCompleteDesignSubset() {
		return designSubsets.stream().
				filter(s -> s.getName().equals(GlobalDefaults.ALL_SAMPLES.getName())).findFirst().get();
	}

	@Override
	public void factorStatusChanged(ExperimentDesignFactorEvent e) {

		ExperimentDesignFactor changedFactor = (ExperimentDesignFactor)e.getSource();
		Collection<ExperimentDesignLevel> existingLevels = getCompleteDesignSubset().getLevelsForFactor(changedFactor);

		if(e.getStatus().equals(ParameterSetStatus.REMOVED) && existingLevels != null) {

			Set<ExperimentDesignLevel> levelsToremove =
					existingLevels.stream().filter(l -> !changedFactor.getLevels().contains(l)).collect(Collectors.toSet());

			if(!levelsToremove.isEmpty())
				levelsToremove.forEach(l -> removeLevel(l));
		}
		if(e.getStatus().equals(ParameterSetStatus.ADDED) && existingLevels != null) {


			Set<ExperimentDesignLevel> levelsToAdd =
					changedFactor.getLevels().stream().filter(l -> !existingLevels.contains(l)).collect(Collectors.toSet());

			if(!levelsToAdd.isEmpty()) {
				levelsToAdd.forEach(l -> getCompleteDesignSubset().addLevel(l));
				fireExperimentDesignEvent(ParameterSetStatus.CHANGED);
			}
		}
	}

	public int getTotalLevels() {
		return factorSet.stream().
				flatMap(f -> f.getLevels().stream()).
				collect(Collectors.toSet()).size();
	}

	public int getSubjectCount() {

		ExperimentDesignFactor subjectFactor = getFactorByName(StandardFactors.SUBJECT.getName());
		if(subjectFactor == null)
			return 1;
		else
			return subjectFactor.getLevels().size();
	}

	public ExperimentalSample getSampleByDataFile(DataFile dataFile) {

		return sampleSet.stream().
				filter(s -> s.hasDataFile(dataFile)).
				findFirst().orElse(null);
	}

	/**
	 * @return the sampleTypeFactor
	 */
	public ExperimentDesignFactor getSampleTypeFactor() {
		return getFactorByName(StandardFactors.SAMPLE_CONTROL_TYPE.getName());
	}

	/**
	 * @param sampleTypeFactor the sampleTypeFactor to set
	 */
	public void setSampleTypeFactor(ExperimentDesignFactor sampleTypeFactor) {
		this.sampleTypeFactor = sampleTypeFactor;
	}
	
	public Collection<ExperimentalSample> getReferenceSamples() {
		
		ExperimentDesignFactor refFactor = ReferenceSamplesManager.getSampleControlTypeFactor();
		return sampleSet.stream().
				filter(s -> !s.getLevel(refFactor).equals(ReferenceSamplesManager.sampleLevel)).
				collect(Collectors.toList());
	}
	
	public Element getXmlElement() {
		
		Element experimentDesignElement = 
				new Element(ExperimentDesignFields.ExperimentDesign.name());
		
		Element factorSetElement = 
				new Element(ExperimentDesignFields.FactorSet.name());
		for(ExperimentDesignFactor f : factorSet)
			factorSetElement.addContent(f.getXmlElement());
		
		experimentDesignElement.addContent(factorSetElement);
		
		Element sampleSetElement = 
				new Element(ExperimentDesignFields.SampleSet.name());
		for(ExperimentalSample s : sampleSet)
			sampleSetElement.addContent(s.getXmlElement());
		
		experimentDesignElement.addContent(sampleSetElement);
		
		Element designSubsetListElement = 
				new Element(ExperimentDesignFields.DesignSubsetList.name());
		for(ExperimentDesignSubset sub : designSubsets)
			designSubsetListElement.addContent(sub.getXmlElement());
		
		experimentDesignElement.addContent(designSubsetListElement);
		
		return experimentDesignElement;
	}
	
	public ExperimentDesign(
			Element experimentDesignElement, 
			RawDataAnalysisProject parentProject) {
		
		factorSet = new TreeSet<ExperimentDesignFactor>();
		sampleSet = new TreeSet<ExperimentalSample>();
		suppressEvents = false;
		eventListeners = ConcurrentHashMap.newKeySet();
		initCleanDesign();
		suppressEvents = false;
		eventListeners = ConcurrentHashMap.newKeySet();
		
		List<Element> factorListElements = 
				experimentDesignElement.getChild(
						ExperimentDesignFields.FactorSet.name()).getChildren();
		if(factorListElements.size() > 0) {
			
			for(Element factorElement : factorListElements) {
				
				ExperimentDesignFactor factor = 
						new ExperimentDesignFactor(factorElement);
				factorSet.add(factor);
			}
		}
		List<Element> sampleListElements = 
				experimentDesignElement.getChild(
						ExperimentDesignFields.SampleSet.name()).getChildren();
		if(sampleListElements.size() > 0) {
			
			for(Element sampleElement : sampleListElements) {
				
				ExperimentalSample sample = null;
				if(sampleElement.getAttributeValue(IDTExperimentalSampleFields.StockSampleId.name()) != null) 
					sample = new IDTExperimentalSample(sampleElement, this, parentProject);
				else 
					sample = new ExperimentalSample(sampleElement, this, parentProject);
				
				sampleSet.add(sample);
			}
		}
		List<Element> subsetListElements = 
				experimentDesignElement.getChild(
						ExperimentDesignFields.DesignSubsetList.name()).getChildren();
		if(subsetListElements.size() > 0) {
			
			for(Element subsetElement : subsetListElements) {
				
				ExperimentDesignSubset subset = 
						new ExperimentDesignSubset(subsetElement, this);
				designSubsets.add(subset);
			}
		}
	}

	public ExperimentDesign(ExperimentDesign experimentDesign) {

		this();
		factorSet.clear();
		factorSet.addAll(experimentDesign.getFactors());
		sampleSet.clear();
		for(ExperimentalSample sample : experimentDesign.getSamples()) {
			
			if(sample instanceof IDTExperimentalSample)
				sampleSet.add(new IDTExperimentalSample((IDTExperimentalSample)sample));
			else	
				sampleSet.add(new ExperimentalSample(sample));	
		}
	}
}






















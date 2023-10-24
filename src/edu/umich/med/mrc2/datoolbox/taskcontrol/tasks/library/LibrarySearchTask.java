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

package edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.library;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import edu.umich.med.mrc2.datoolbox.data.AdductMatch;
import edu.umich.med.mrc2.datoolbox.data.CompoundLibrary;
import edu.umich.med.mrc2.datoolbox.data.LibraryMsFeature;
import edu.umich.med.mrc2.datoolbox.data.MSRTSearchParametersObject;
import edu.umich.med.mrc2.datoolbox.data.MassSpectrum;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureIdentity;
import edu.umich.med.mrc2.datoolbox.data.MsRtLibraryMatch;
import edu.umich.med.mrc2.datoolbox.data.compare.MsFeatureIdentityComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.enums.MassErrorType;
import edu.umich.med.mrc2.datoolbox.data.enums.Polarity;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.msmsscore.SpectrumMatcher;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskEvent;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskListener;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.utils.MsUtils;

public class LibrarySearchTask  extends AbstractTask implements TaskListener{

	private boolean clearPreviousResults;
	private Collection<CompoundLibrary>targetLibraries;
	private Collection<String>targetLibraryIds;
	private Collection<String>librariesToLoad;
	private Collection<MsFeature>inputFeatures;
	private Collection<MsFeature>identifiedFeatures;
	private double massAccuracy;
	private MassErrorType massErrorType;
	private double rtWindow;
	private boolean useCustomRtWindows;
	private int maxHits;
	private boolean ignoreAddudctMatching;
	private boolean relaxMinorIsotopeError;
	private Map<Polarity, List<LibraryMsFeature>>polarityMap;
	private boolean librariesLoaded;

	public LibrarySearchTask(
			Collection<CompoundLibrary> targetLibraries,
			Collection<MsFeature> inputFeatures,
			double massAccuracy,
			MassErrorType massErrorType,
			double rtWindow,
			boolean useCustomRtWindows,
			int maxHits,
			boolean ignoreAddudctMatching,
			boolean relaxMinorIsotopeError) {

		this.targetLibraries = targetLibraries;
		this.inputFeatures = inputFeatures;
		this.massAccuracy = massAccuracy;
		this.massErrorType = massErrorType;
		this.rtWindow = rtWindow;
		this.useCustomRtWindows = useCustomRtWindows;
		this.maxHits = maxHits;
		this.ignoreAddudctMatching = ignoreAddudctMatching;
		this.relaxMinorIsotopeError = relaxMinorIsotopeError;

		librariesLoaded = true;		
	}

	public LibrarySearchTask(
			Collection<MsFeature> featuresToSearch, 
			MSRTSearchParametersObject spo) {
		
		this.clearPreviousResults = spo.isClearPreviousResults();
		this.targetLibraryIds = spo.getLibraryIds();
		this.inputFeatures = featuresToSearch;
		this.massAccuracy = spo.getMassError();
		this.massErrorType = spo.getMassErrorType();
		this.rtWindow = spo.getRetentionWindow();
		this.useCustomRtWindows = spo.isUseCustomRtWindow();
		this.maxHits = spo.getMaxHits();
		this.ignoreAddudctMatching = spo.isIgnoreAdductType();
		this.relaxMinorIsotopeError = 
				spo.isRelaxMassErrorForMinorIsotopes();
		targetLibraries = new ArrayList<CompoundLibrary>();
		librariesLoaded = false;
	}

	@Override
	public void run() {
		
		setStatus(TaskStatus.PROCESSING);
		
		if(clearPreviousResults)
			clearExistingMSRTSearchResults();
		
		//	Load all libraries
		loadLibraries();
		
		if(librariesLoaded) {
			searchLibraries();
			setStatus(TaskStatus.FINISHED);
		}
	}

	private void clearExistingMSRTSearchResults() {
		
		taskDescription = "Clearing previous search results ...";
		total = inputFeatures.size();
		processed = 0;
		Collection<MsFeatureIdentity>toRemove = null;
		for(MsFeature f : inputFeatures) {

			toRemove = f.getMSRTIdentifications();	
			if(!toRemove.isEmpty()) {
				
				f.setSuppressEvents(true);
				for(MsFeatureIdentity id : toRemove)
					f.removeIdentity(id);
				
				f.setSuppressEvents(false);
			}
			processed++;
		}
	}

	private void loadLibraries() {
		
		taskDescription = "Loading libraries";
		total = 100;
		processed = 20;
		
		librariesToLoad = new TreeSet<String>();
		for(String libId : targetLibraryIds) {
			
			CompoundLibrary activeLib = MRC2ToolBoxCore.getActiveMsLibraries().
					stream().filter(l -> l.getLibraryId().equals(libId)).
					findFirst().orElse(null);
			if(activeLib == null)
				librariesToLoad.add(libId);
			else
				targetLibraries.add(activeLib);
		}
		if(librariesToLoad.isEmpty()) {
			librariesLoaded = true;
			return;
		}
		else {
			for(String libId : librariesToLoad) {
				
				LoadDatabaseLibraryTask task = new LoadDatabaseLibraryTask(libId);
				task.addTaskListener(this);
				MRC2ToolBoxCore.getTaskController().addTask(task);
			}
		}		
	}

	private void searchLibraries() {

		taskDescription = "Searching features against target libraries";
		total = inputFeatures.size();
		processed = 0;
		
		// Put all active library entries in the map by polarity
		polarityMap = targetLibraries.stream()
				.flatMap(lib -> lib.getFeatures().stream())
				.filter(LibraryMsFeature.class::isInstance)
		    	.map(LibraryMsFeature.class::cast)
		    	.filter(f -> f.getSpectrum() != null)
		    	.filter(f -> f.getSpectrum().getPrimaryAdduct() != null)
				.filter(f -> f.isActive())
				.collect(Collectors.groupingBy(LibraryMsFeature::getPolarity));

		identifiedFeatures = new HashSet<MsFeature>();
		for(MsFeature f : inputFeatures) {

			taskDescription = "Looking up " + f.getName() + " ...";
			Set<MsFeatureIdentity>ids = lookupFeatureInLibrary(f);
			boolean addToList = false;
			if(!ids.isEmpty()) {
				
				addToList = true;
				for(MsFeatureIdentity fid : ids)
					f.addIdentity(fid);
					
				f.setTopScoreIdAsDefault();
			}
			if(addToList)
				identifiedFeatures.add(f);

			processed++;
		}
	}

	public Collection<MsFeature> getInputFeatures() {
		return inputFeatures;
	}

	private Set<MsFeatureIdentity> lookupFeatureInLibrary(MsFeature f) {

		Set<MsFeatureIdentity>ids = new HashSet<MsFeatureIdentity>();
		if(polarityMap.get(f.getPolarity()) == null)
			return ids;

		Set<LibraryMsFeature>retentionFiltered = 
				getRetentionFilteredLibrarySubset(f);
		if(retentionFiltered.isEmpty())
			return ids;

		for(LibraryMsFeature mslf : retentionFiltered) {

			MsFeatureIdentity match = matchByMs(f, mslf);
			if(match != null)
				ids.add(match);
		}
		if(!ids.isEmpty() && !f.getMSRTIdentifications().isEmpty()) {
			
			Set<String> existingIds = 
					f.getMSRTIdentifications().stream().
					map(i -> i.getMsRtLibraryMatch().getLibraryTargetId()).
					collect(Collectors.toSet());
			ids = ids.stream().
					filter(i -> !existingIds.contains(i.getMsRtLibraryMatch().getLibraryTargetId())).
					collect(Collectors.toSet());
		}
		if(ids.size() > maxHits)
			return ids.stream().
					sorted(new MsFeatureIdentityComparator(SortProperty.Quality)).
					limit(maxHits).
					collect(Collectors.toSet());
		
		return ids;
	}

	Set<LibraryMsFeature>getRetentionFilteredLibrarySubset(MsFeature f){

		double rt = f.getRetentionTime();
		if(f.getStatsSummary() != null 
				&& f.getStatsSummary().getMedianObservedRetention() > 0)
			rt = f.getStatsSummary().getMedianObservedRetention();
		
		final double effectiveRt = rt;
		return polarityMap.get(f.getPolarity()).stream()
				.filter(x -> x.getLibraryMatchRtRange(rtWindow, useCustomRtWindows).contains(effectiveRt))
				.collect(Collectors.toSet());
	}

	private MsFeatureIdentity matchByMs(MsFeature f, LibraryMsFeature mslf) {

		MsFeatureIdentity match = null;
		MassSpectrum librarySpectrum = mslf.getSpectrum();
		MassSpectrum observedSpectrum = f.getSpectrum();

		if(observedSpectrum == null)
			return null;

		if(observedSpectrum.getAdducts().isEmpty())
			MsUtils.guessAdducts(observedSpectrum, f.getPolarity(), 3);

		//	TODO log spectra where can't guess adducts
		if(observedSpectrum.getAdducts().isEmpty())
			return null;

		Set<AdductMatch>matches =
				SpectrumMatcher.matchAccurateMs(
					observedSpectrum,
					librarySpectrum,
					massAccuracy,
					massErrorType,
					ignoreAddudctMatching,
					relaxMinorIsotopeError);

		if(!matches.isEmpty()) {

			match = new MsFeatureIdentity(
					mslf.getPrimaryIdentity().getCompoundIdentity(),
					mslf.getPrimaryIdentity().getConfidenceLevel());

			match.setIdentityName(mslf.getName());
			match.setQcStandard(mslf.isQcStandard());
			MsRtLibraryMatch rtlMatch = new MsRtLibraryMatch(
					mslf.getId(),
					matches,
					mslf.getRetentionTime(),
					mslf.getSpectrum());
			rtlMatch.setLibraryId(mslf.getLibraryId());
			rtlMatch.setLibraryTargetName(mslf.getName());
			match.setMsRtLibraryMatch(rtlMatch);
		}
		return match;
	}

	@Override
	public Task cloneTask() {

		return new LibrarySearchTask(
				targetLibraries,
				inputFeatures,
				massAccuracy,
				massErrorType,
				rtWindow,
				useCustomRtWindows,
				maxHits,
				ignoreAddudctMatching,
				relaxMinorIsotopeError);
	}

	public Collection<MsFeature> getIdentifiedFeatures() {
		return identifiedFeatures;
	}

	@Override
	public void statusChanged(TaskEvent e) {
		
		if (e.getStatus() == TaskStatus.FINISHED) {

			((AbstractTask) e.getSource()).removeTaskListener(this);
			
			if (e.getSource().getClass().equals(LoadDatabaseLibraryTask.class)) {
				
				CompoundLibrary newLibrary = 
						((LoadDatabaseLibraryTask)e.getSource()).getLibrary();				
				MRC2ToolBoxCore.getActiveMsLibraries().add(newLibrary);
				targetLibraries.add(newLibrary);
				librariesToLoad.remove(newLibrary.getLibraryId());
				if(librariesToLoad.isEmpty()) {
					
					librariesLoaded = true;
					searchLibraries();
					setStatus(TaskStatus.FINISHED);
				}
			}				
		}		
	}
}




































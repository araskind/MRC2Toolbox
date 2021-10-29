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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import edu.umich.med.mrc2.datoolbox.data.compare.MsFeatureIdentityComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.enums.AnnotatedObjectType;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundDatabaseEnum;
import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.data.enums.FeatureIdentificationState;
import edu.umich.med.mrc2.datoolbox.data.enums.KendrickUnits;
import edu.umich.med.mrc2.datoolbox.data.enums.ParameterSetStatus;
import edu.umich.med.mrc2.datoolbox.data.enums.Polarity;
import edu.umich.med.mrc2.datoolbox.data.enums.SpectrumSource;
import edu.umich.med.mrc2.datoolbox.data.lims.ObjectAnnotation;
import edu.umich.med.mrc2.datoolbox.gui.communication.MsFeatureEvent;
import edu.umich.med.mrc2.datoolbox.gui.communication.MsFeatureListener;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.utils.MsUtils;
import edu.umich.med.mrc2.datoolbox.utils.Range;

public class MsFeature implements AnnotatedObject, Serializable {

	protected static final long serialVersionUID = 1674860219322623509L;
	protected String id;
	protected String name;
	protected MassSpectrum spectrum;
	protected double retentionTime;
	protected double medianObservedRetentionTime = -1.0;
	protected Range rtRange;
	protected double neutralMass;	
	protected boolean active;
	protected double qualityScore;
	protected Polarity polarity;

	protected Set<MsFeatureIdentity> identifications;
	protected boolean idDisabled;
	protected MsFeatureIdentity primaryIdentity;
	protected AnnotatedObjectType annotatedObjectType;
	protected TreeSet<ObjectAnnotation> annotations;
	protected MsFeatureStatisticalSummary statsSummary;
	protected Adduct defaultModification;
	protected Adduct suggestedModification;
	protected BinnerAnnotation binnerAnnotation;
	protected Collection<PostProcessorAnnotation>postProcessorAnnotations;
	
	protected ParameterSetStatus status;
	protected Set<MsFeatureListener> eventListeners;
	protected boolean suppressEvents;
	
	// Copy constructor
	public MsFeature(MsFeature source) {

		id = source.getId();
		name = source.getName();
		spectrum = source.getSpectrum();
		polarity = source.getPolarity();
		retentionTime = source.getRetentionTime();
		rtRange = source.getRtRange();
		neutralMass = source.getNeutralMass();
		active = source.isActive();
		qualityScore = source.getQualityScore();
		identifications = new HashSet<MsFeatureIdentity>();
		identifications.addAll(source.getIdentifications());
		primaryIdentity = source.getPrimaryIdentity();
		annotatedObjectType = source.getAnnotatedObjectType();
		annotations = new TreeSet<ObjectAnnotation>();
		annotations.addAll(source.getAnnotations());
		defaultModification = source.getDefaultChemicalModification();
		suggestedModification = null;
		binnerAnnotation = source.getBinnerAnnotation();
		eventListeners = ConcurrentHashMap.newKeySet();
		eventListeners.addAll(source.getFeatureListeners());
	}

	public MsFeature(String name, double neutralMass, double retentionTime) {

		this.id = DataPrefix.MS_FEATURE.getName() +
				UUID.randomUUID().toString().substring(0, 12);
		this.name = name;
		this.neutralMass = neutralMass;
		this.retentionTime = retentionTime;
		rtRange = new Range(retentionTime);

		spectrum = null;
		active = true;
		qualityScore = 0;
		identifications = new HashSet<MsFeatureIdentity>();
		primaryIdentity = null;
		annotations = new TreeSet<ObjectAnnotation>();
		defaultModification = null;
		suggestedModification = null;
		binnerAnnotation = null;
		eventListeners = ConcurrentHashMap.newKeySet();
	}

	public MsFeature(String name, double retentionTime) {

		this.id = DataPrefix.MS_FEATURE.getName() + 
				UUID.randomUUID().toString().substring(0, 12);
		this.name = name;
		this.retentionTime = retentionTime;
		rtRange = new Range(retentionTime);
		spectrum = null;
		neutralMass = 0.0d;
		active = true;
		qualityScore = 0;
		identifications = new HashSet<MsFeatureIdentity>();
		primaryIdentity = null;
		annotations = new TreeSet<ObjectAnnotation>();
		defaultModification = null;
		suggestedModification = null;
		binnerAnnotation = null;
		eventListeners = ConcurrentHashMap.newKeySet();
	}

	public MsFeature(String id, String name, double retentionTime) {

		this.id = id;
		this.name = name;
		this.retentionTime = retentionTime;

		rtRange = new Range(retentionTime);
		spectrum = null;
		neutralMass = 0.0d;
		active = true;
		qualityScore = 0;
		identifications = new HashSet<MsFeatureIdentity>();
		primaryIdentity = null;
		annotations = new TreeSet<ObjectAnnotation>();
		defaultModification = null;
		suggestedModification = null;
		binnerAnnotation = null;
		eventListeners = ConcurrentHashMap.newKeySet();
	}

	public MsFeature(double retentionTime, Polarity polarity) {
		super();
		this.id = DataPrefix.MS_FEATURE.getName() + 
				UUID.randomUUID().toString().substring(0, 12);
		this.retentionTime = retentionTime;
		this.polarity = polarity;
		
		rtRange = new Range(retentionTime);
		spectrum = null;
		neutralMass = 0.0d;
		active = true;
		qualityScore = 0;
		identifications = new HashSet<MsFeatureIdentity>();
		primaryIdentity = null;
		annotations = new TreeSet<ObjectAnnotation>();
		defaultModification = null;
		suggestedModification = null;
		binnerAnnotation = null;
		eventListeners = ConcurrentHashMap.newKeySet();
	}

	/*
	 * Annotation
	 * */
	@Override
	public void addAnnotation(ObjectAnnotation annotation) {

		annotations.add(annotation);
		setStatus(ParameterSetStatus.CHANGED);
	}

	@Override
	public void removeAnnotation(ObjectAnnotation annotation) {

		annotations.remove(annotation);
		setStatus(ParameterSetStatus.CHANGED);
	}

	@Override
	public Collection<ObjectAnnotation> getAnnotations() {
		return annotations;
	}

	public void setAnnotations(TreeSet<ObjectAnnotation> annotations) {

		this.annotations.clear();
		this.annotations.addAll(annotations);
		setStatus(ParameterSetStatus.CHANGED);
	}

	/* 	End annotation	*/

	/*
	 * Binner annotation
	 * */
	public void setBinnerAnnotation(BinnerAnnotation annotation) {
		this.binnerAnnotation = annotation;
	}

	public BinnerAnnotation getBinnerAnnotation() {
		return binnerAnnotation;
	}
	/* 	End Binner annotation	*/

	/*
	 * Identification
	 * TODO go trough the logic again, are all the checks necessary?
	 * */
	public boolean addIdentity(MsFeatureIdentity cid) {

		if(cid == null)
			return false;

		if(cid.getCompoundIdentity() == null)
			return false;

		if(cid.getCompoundIdentity().getName() == null)
			return false;

		if(cid.getCompoundIdentity().getName().trim().isEmpty())
			return false;

		if(identifications.contains(cid))
			return true;
		else {
			identifications.add(cid);
			return true;
		}
	}

	public void removeIdentity(MsFeatureIdentity cid) {

		identifications.remove(cid);
		if(cid.equals(primaryIdentity)) {

			primaryIdentity = null;
			setTopScoreIdAsDefault();
		}
		//	Change name to unknown if necessary
		if(primaryIdentity == null && !name.startsWith(DataPrefix.MS_LIBRARY_UNKNOWN_TARGET.getName()))
			name = DataPrefix.MS_LIBRARY_UNKNOWN_TARGET.getName() +
				MRC2ToolBoxConfiguration.getMzFormat().format(getMonoisotopicMz()) + "_" +
				MRC2ToolBoxConfiguration.getRtFormat().format(retentionTime);

		setStatus(ParameterSetStatus.CHANGED);
	}

	public void clearIdentification() {

		identifications.clear();
		primaryIdentity = null;

		if(!name.startsWith(DataPrefix.MS_LIBRARY_UNKNOWN_TARGET.getName()))
			name = DataPrefix.MS_LIBRARY_UNKNOWN_TARGET.getName() +
				MRC2ToolBoxConfiguration.getMzFormat().format(getMonoisotopicMz()) + "_" +
				MRC2ToolBoxConfiguration.getRtFormat().format(retentionTime);

		setStatus(ParameterSetStatus.CHANGED);
	}

	public void setTopScoreIdAsDefault() {

		if(!identifications.isEmpty()) {

			identifications.stream().forEach(i -> i.setPrimary(false));
			MsFeatureIdentity[] ids = identifications.toArray(new MsFeatureIdentity[identifications.size()]);
			Arrays.sort(ids, new MsFeatureIdentityComparator(SortProperty.Quality));
			primaryIdentity = ids[0];
			primaryIdentity.setPrimary(true);
		}
	}

	public Set<MsFeatureIdentity> getIdentifications() {
		return identifications;
	}

	public void setIdentifications(HashSet<MsFeatureIdentity> identifications) {

		this.identifications = identifications;
		setStatus(ParameterSetStatus.CHANGED);
	}
	
	public void disablePrimaryIdentity() {
		
		primaryIdentity = null;	
		identifications.stream().forEach(i -> i.setPrimary(false));
		double mz = 0.0d;
		if(spectrum.getTandemSpectrum(SpectrumSource.EXPERIMENTAL) != null
				&& spectrum.getTandemSpectrum(SpectrumSource.EXPERIMENTAL).getParent() != null)
			mz = spectrum.getTandemSpectrum(SpectrumSource.EXPERIMENTAL).getParent().getMz();
		
		name = DataPrefix.MS_LIBRARY_UNKNOWN_TARGET.getName() +
				MRC2ToolBoxConfiguration.getMzFormat().format(mz) + "_" +
				MRC2ToolBoxConfiguration.getRtFormat().format(retentionTime);

		setStatus(ParameterSetStatus.CHANGED);
	}

	public void setPrimaryIdentity(MsFeatureIdentity newIdentity) {

		if(newIdentity == null)
			return;

		addIdentity(newIdentity);
		identifications.stream().forEach(i -> i.setPrimary(false));
		if(primaryIdentity == null) {

			primaryIdentity = newIdentity;
			primaryIdentity.setPrimary(true);			
			setStatus(ParameterSetStatus.CHANGED);
			return;
		}
		if(!primaryIdentity.equals(newIdentity)) {

			primaryIdentity = newIdentity;
			primaryIdentity.setPrimary(true);
			setStatus(ParameterSetStatus.CHANGED);
			return;
		}
		idDisabled = false;
	}

	public MsFeatureIdentity getPrimaryIdentity() {
		
		if(idDisabled)
			return null;
		
		return primaryIdentity;
	}

	public String getDatabaseId(CompoundDatabaseEnum db) {

		String idString = "";

		if (identifications != null && !identifications.isEmpty()) {

			for (MsFeatureIdentity id : identifications) {

				if (id != null)
					idString = id.getCompoundIdentity().getDbId(db);
			}
		}
		if (idString == null)
			idString = "";

		return idString;
	}

	public CompoundIdentity getDatabaseIdentification(CompoundDatabaseEnum database) {

		CompoundIdentity id = null;

		for (MsFeatureIdentity present : identifications) {

			if (present.getCompoundIdentity().getDbId(database) != null)
				id = present.getCompoundIdentity();
		}
		return id;
	}
	
	/* 	End identification	*/

	/*
	 * Feature events
	 * */
	public void addListener(MsFeatureListener listener) {

		if(eventListeners == null)
			eventListeners = ConcurrentHashMap.newKeySet();

		eventListeners.add(listener);
	}

	public void removeListener(MsFeatureListener listener) {

		if(eventListeners == null)
			eventListeners = ConcurrentHashMap.newKeySet();

		eventListeners.remove(listener);
	}

	public void removeAllListeners() {
		eventListeners = ConcurrentHashMap.newKeySet();
	}

	public void fireFeatureEvent() {

		if(suppressEvents)
			return;

		if(eventListeners == null) {
			eventListeners = ConcurrentHashMap.newKeySet();
			return;
		}
		MsFeatureEvent event = new MsFeatureEvent(this);
		eventListeners.stream().forEach(l -> l.msFeatureStatusChanged(event));
	}

	public void fireFeatureEvent(ParameterSetStatus newStatus) {

		if(suppressEvents)
			return;

		if(eventListeners == null) {
			eventListeners = ConcurrentHashMap.newKeySet();
			return;
		}
		MsFeatureEvent event = new MsFeatureEvent(this, newStatus);
		eventListeners.stream().forEach(l -> l.msFeatureStatusChanged(event));
	}

	public Set<MsFeatureListener> getFeatureListeners() {

		if(eventListeners == null)
			eventListeners = ConcurrentHashMap.newKeySet();

		return eventListeners;
	}

	public ParameterSetStatus getStatus() {
		return status;
	}

	public Adduct getDefaultChemicalModification() {
		return defaultModification;
	}

	public void setDefaultChemicalModification(Adduct newChemMod) {

		if (defaultModification == null && newChemMod == null)
			return;

		if ((defaultModification == null && newChemMod != null) || (defaultModification != null && newChemMod == null)) {

			defaultModification = newChemMod;
			setStatus(ParameterSetStatus.CHANGED);
			return;
		}
		if (!defaultModification.equals(newChemMod)) {

			defaultModification = newChemMod;
			setStatus(ParameterSetStatus.CHANGED);
			return;
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String newName) {

		if (!newName.equals(name)) {
			name = newName;
			setStatus(ParameterSetStatus.CHANGED);
		}
	}

	public Polarity getPolarity() {
		
		if(polarity == null || polarity.equals(Polarity.Neutral)) {
			
			if(spectrum != null && spectrum.getPrimaryAdduct() != null)
				polarity = spectrum.getPrimaryAdduct().getPolarity();			
		}		
		return polarity;
	}
	
	public void setPolarity(Polarity polarity) {
		this.polarity = polarity;
	}

	public double getQualityScore() {
		return qualityScore;
	}

	public double getRetentionTime() {
		return retentionTime;
	}

	public double getSampleMedianArea() {
		return statsSummary.getSampleMedian();
	}

	public double getAveragePeakArea() {

		if(statsSummary == null)
			return 0.0d;

		if(statsSummary.getSampleMean() > 0.0d)
			return statsSummary.getSampleMean();

		if(statsSummary.getPooledMean() > 0.0d)
			return statsSummary.getPooledMean();
		
		if(statsSummary.getTotalMedian() > 0.0d)
			return statsSummary.getTotalMedian();

		return 0.0d;
	}

	public MassSpectrum getSpectrum() {
		return spectrum;
	}

	public MsFeatureStatisticalSummary getStatsSummary() {
		return statsSummary;
	}

	public Adduct getSuggestedModification() {
		return suggestedModification;
	}

	@Override
	public String getId() {
		return id;
	}

	public boolean isActive() {
		return active;
	}

	public boolean isIdentified() {
		
		if(primaryIdentity == null)
			return false;
		else {
			if(primaryIdentity.getCompoundIdentity() == null)
				return false;
			else
				return true;
		}
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public void setNeutralMass(double neutralMass) {
		this.neutralMass = neutralMass;
	}

	public void setQualityScore(double qualityScore) {
		this.qualityScore = qualityScore;
	}

	public void setRetentionTime(double retentionTime) {
		this.retentionTime = retentionTime;
	}

	public void setSpectrum(MassSpectrum spectrum) {
		this.spectrum = spectrum;
	}

	public void setStatsSummary(MsFeatureStatisticalSummary statsSummary) {

		this.statsSummary = statsSummary;
		setStatus(ParameterSetStatus.CHANGED);
	}

	public void setStatus(ParameterSetStatus status) {

		this.status = status;
		fireFeatureEvent();
	}

	public void setSuggestedModification(Adduct suggestedModification) {
		this.suggestedModification = suggestedModification;
	}

	public boolean isQcStandard() {

		if(primaryIdentity == null)
			return false;
		else
			return primaryIdentity.isQcStandard();
	}

	/*
	 * Get charge
	 * */
	public int getAbsoluteObservedCharge() {

		int absCharge = 0;

		if (spectrum != null) {

			if (spectrum.getPrimaryAdduct() != null)
				absCharge = spectrum.getPrimaryAdductAbsoluteCharge();
			else
				absCharge = spectrum.getAbsoluteCharge();
		}
		return absCharge;
	}

	public int getCharge() {

		if (spectrum != null) {

			if (spectrum.getPrimaryAdduct() != null)
				return spectrum.getPrimaryAdduct().getCharge();
			else {
				return spectrum.getAbsoluteCharge() * polarity.getSign();
			}
		}
		return 0;
	}

	public double getBasePeakMz() {

		if (spectrum != null)
			return spectrum.getBasePeakMz();

		return 0.0d;
	}

	public double getMonoisotopicMz() {

		if (spectrum != null)
			return spectrum.getMonoisotopicMz();

		return 0.0d;
	}

	public double getNeutralMass() {

		if(primaryIdentity != null)
			return primaryIdentity.getCompoundIdentity().getExactMass();

		if (spectrum != null) {

			if(spectrum.getPrimaryAdduct() != null) {

				MsPoint[] ms = spectrum.getMsForAdduct(spectrum.getPrimaryAdduct());
				neutralMass = MsUtils.getNeutralMass(ms[0], spectrum.getPrimaryAdduct());
			}
		}
		return neutralMass;
	}

	/*
	 * Kendrick Mass Defect
	 * */
	public double getKmd() {
		return MsUtils.getKendrickMassDefect(getNeutralMass(), KendrickUnits.METHYLENE);
	}

	public double getModifiedKmd() {
		return MsUtils.getModifiedKendrickMassDefect(getNeutralMass());
	}

	@Override
	public String toString() {

		String screenName = name;
		double mass = getMonoisotopicMz();
		if(mass == 0.0d)
			mass = getNeutralMass();

		screenName += " | " + MRC2ToolBoxConfiguration.getMzFormat().format(mass);
		screenName += " @ " + MRC2ToolBoxConfiguration.getRtFormat().format(getRetentionTime());
		return screenName;
	}

	/**
	 * @param uniqueId the uniqueId to set
	 */
	public void setId(String uniqueId) {
		this.id = uniqueId;
	}

	/**
	 * @return the rtRange
	 */
	public Range getRtRange() {
		return rtRange;
	}

	/**
	 * @param rtRange the rtRange to set
	 */
	public void setRtRange(Range rtRange) {
		this.rtRange = rtRange;
	}

	public boolean isPresent() {

		if(statsSummary == null)
			return false;

		if(statsSummary.getSampleFrequency() > 0 || statsSummary.getPooledFrequency() > 0)
			return true;
		else
			return false;
	}

    @Override
    public boolean equals(Object obj) {

		if (obj == this)
			return true;

        if (obj == null)
            return false;

        if (!MsFeature.class.isAssignableFrom(obj.getClass()))
            return false;

        final MsFeature other = (MsFeature) obj;

        if ((this.id == null) ? (other.getId() != null) : !this.id.equals(other.getId()))
            return false;

        if ((this.name == null) ? (other.getName() != null) : !this.name.equals(other.getName()))
            return false;

        return true;
    }

    @Override
    public int hashCode() {

        int hash = 3;
        hash = 53 * hash + (this.id != null ? this.id.hashCode() : 0)
        		+ (this.name != null ? this.name.hashCode() : 0);
        return hash;
    }

    public void addPostProcessorAnnotation(PostProcessorAnnotation annotation) {

    	if(postProcessorAnnotations == null)
    		postProcessorAnnotations = new ArrayList<PostProcessorAnnotation>();

    	postProcessorAnnotations.add(annotation);
    	binnerAnnotation = annotation.getBa();
		if(annotation.getCid() != null)
			addIdentity(new MsFeatureIdentity(annotation.getCid(), annotation.getIdConfidence()));
    }

    public PostProcessorAnnotation getPostProcessorAnnotationForIdentity(MsFeatureIdentity id) {

    	if(postProcessorAnnotations == null)
    		return null;

    	return postProcessorAnnotations.stream().
    			filter(ppa -> ppa.getCid().equals(id.getCompoundIdentity())).
    			findFirst().orElse(null);
    }

	/**
	 * @param suppressEvents the suppressEvents to set
	 */
	public void setSuppressEvents(boolean suppressEvents) {
		this.suppressEvents = suppressEvents;
	}

	public boolean hasInstrumentMsMs() {

		if(getSpectrum() == null)
			return false;

		if(getSpectrum().getTandemSpectra().isEmpty())
			return false;

		return (getSpectrum().getTandemSpectra().stream().
			filter(t -> t.getSpectrumSource().equals(SpectrumSource.EXPERIMENTAL)).
			findFirst().orElse(null) != null);
	}

	public String getBicMetaboliteName() {

		double rt = 0.0d;
		if(statsSummary == null)
			rt = retentionTime;
		else
			rt = statsSummary.getMedianObservedRetention();

		return MRC2ToolBoxConfiguration.getMzFormat().format(getMonoisotopicMz()) + "_" +
				MRC2ToolBoxConfiguration.getRtFormat().format(rt);
	}

	@Override
	public void setAnnotatedObjectType(AnnotatedObjectType type) {
		this.annotatedObjectType = type;
	}

	@Override
	public AnnotatedObjectType getAnnotatedObjectType() {
		return annotatedObjectType;
	}
	
	public FeatureIdentificationState getIdentificationState() {
		
		if(identifications.isEmpty())
			return FeatureIdentificationState.NO_IDENTIFICATION;
		
		if(identifications.size() == 1) {
			
			if(primaryIdentity == null)
				return FeatureIdentificationState.SINGLE_INACTIVE_ID;
			else
				return FeatureIdentificationState.SINGLE_ACTIVE_ID;
		}
		if(identifications.size() > 1) {
			if(primaryIdentity == null)
				return FeatureIdentificationState.MULTIPLE_INACTIVE_IDS;
			else
				return FeatureIdentificationState.MULTIPLE_ACTIVE_IDS;
		}
		return FeatureIdentificationState.NO_IDENTIFICATION;
	}

	public boolean isIdDisabled() {
		return idDisabled;
	}

	public void setIdDisabled(boolean idDisabled) {
		this.idDisabled = idDisabled;
	}
	
	public double getMedianObservedRetention() {
		
		if(medianObservedRetentionTime == -1.0d)
			medianObservedRetentionTime = statsSummary.getMeanObservedRetention();
		
		return medianObservedRetentionTime;
	}
	
	public int getMSMSLibraryMatchCount() {
		
		if(identifications.isEmpty())
			return 0;
		
		List<MsFeatureIdentity> msmsHits = identifications.stream().
				filter(id -> id.getReferenceMsMsLibraryMatch() != null).
				collect(Collectors.toList());
		
		if(msmsHits.isEmpty())
			return 0;
		else
			return msmsHits.size();
	}
}








































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
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.jdom2.Element;

import edu.umich.med.mrc2.datoolbox.data.compare.MsFeatureIdentityComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.enums.AnnotatedObjectType;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundDatabaseEnum;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundIdentificationConfidence;
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
import edu.umich.med.mrc2.datoolbox.msmsscore.MSMSScoreCalculator;
import edu.umich.med.mrc2.datoolbox.project.store.MassSpectrumFields;
import edu.umich.med.mrc2.datoolbox.project.store.MsFeatureFields;
import edu.umich.med.mrc2.datoolbox.project.store.MsFeatureIdentityFields;
import edu.umich.med.mrc2.datoolbox.utils.MsUtils;
import edu.umich.med.mrc2.datoolbox.utils.Range;

public class MsFeature implements AnnotatedObject, Serializable {

	protected static final long serialVersionUID = 1674860219322623509L;
	protected String id;
	protected String name;
	protected String targetId;
	protected MassSpectrum spectrum;
	protected double retentionTime;
	private double area, height;
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
	
	public static final String DEFAULT_ID_NAME = "UNKNOWN";
	
	// Copy constructor
//	public MsFeature(MsFeature source) {
//
//		id = source.getId();
//		name = source.getName();
//		spectrum = source.getSpectrum();
//		polarity = source.getPolarity();
//		retentionTime = source.getRetentionTime();
//		rtRange = source.getRtRange();
//		neutralMass = source.getNeutralMass();
//		active = source.isActive();
//		qualityScore = source.getQualityScore();
//		identifications = 
//				new HashSet<MsFeatureIdentity>(source.getIdentifications());
//		primaryIdentity = source.getPrimaryIdentity();
//		annotatedObjectType = source.getAnnotatedObjectType();
//		annotations = 
//				new TreeSet<ObjectAnnotation>(source.getAnnotations());
//
//		defaultModification = source.getDefaultChemicalModification();
//		suggestedModification = null;
//		binnerAnnotation = source.getBinnerAnnotation();
//		eventListeners = ConcurrentHashMap.newKeySet();
//		eventListeners.addAll(source.getFeatureListeners());
//	}
//
//	public MsFeature(String name, double neutralMass, double retentionTime) {
//
//		this.id = DataPrefix.MS_FEATURE.getName() +
//				UUID.randomUUID().toString().substring(0, 12);
//		this.name = name;
//		this.neutralMass = neutralMass;
//		this.retentionTime = retentionTime;
//		rtRange = new Range(retentionTime);
//
//		spectrum = null;
//		active = true;
//		qualityScore = 0;
//		identifications = new HashSet<MsFeatureIdentity>();
//		primaryIdentity = null;
//		annotations = new TreeSet<ObjectAnnotation>();
//		defaultModification = null;
//		suggestedModification = null;
//		binnerAnnotation = null;
//		eventListeners = ConcurrentHashMap.newKeySet();
//	}
//
//	public MsFeature(String name, double retentionTime) {
//
//		this.id = DataPrefix.MS_FEATURE.getName() + 
//				UUID.randomUUID().toString().substring(0, 12);
//		this.name = name;
//		this.retentionTime = retentionTime;
//		rtRange = new Range(retentionTime);
//		spectrum = null;
//		neutralMass = 0.0d;
//		active = true;
//		qualityScore = 0;
//		identifications = new HashSet<MsFeatureIdentity>();
//		primaryIdentity = null;
//		annotations = new TreeSet<ObjectAnnotation>();
//		defaultModification = null;
//		suggestedModification = null;
//		binnerAnnotation = null;
//		eventListeners = ConcurrentHashMap.newKeySet();
//	}
//
//	public MsFeature(String id, String name, double retentionTime) {
//
//		this.id = id;
//		this.name = name;
//		this.retentionTime = retentionTime;
//		rtRange = new Range(retentionTime);
//		
//		spectrum = null;
//		neutralMass = 0.0d;
//		active = true;
//		qualityScore = 0;
//		identifications = new HashSet<MsFeatureIdentity>();
//		primaryIdentity = null;
//		annotations = new TreeSet<ObjectAnnotation>();
//		defaultModification = null;
//		suggestedModification = null;
//		binnerAnnotation = null;
//		eventListeners = ConcurrentHashMap.newKeySet();
//	}
//
//	public MsFeature(double retentionTime, Polarity polarity) {
//		super();
//		this.id = DataPrefix.MS_FEATURE.getName() + 
//				UUID.randomUUID().toString().substring(0, 12);
//		this.retentionTime = retentionTime;
//		this.polarity = polarity;
//		
//		rtRange = new Range(retentionTime);
//		spectrum = null;
//		neutralMass = 0.0d;
//		active = true;
//		qualityScore = 0;
//		identifications = new HashSet<MsFeatureIdentity>();
//		primaryIdentity = null;
//		annotations = new TreeSet<ObjectAnnotation>();
//		defaultModification = null;
//		suggestedModification = null;
//		binnerAnnotation = null;
//		eventListeners = ConcurrentHashMap.newKeySet();
//	}

	public MsFeature() {
		super();
		this.id = DataPrefix.MS_FEATURE.getName() +
				UUID.randomUUID().toString().substring(0, 12);
		this.spectrum = null;
		this.active = true;
		this.qualityScore = 0;
		this.identifications = new HashSet<MsFeatureIdentity>();
		this.primaryIdentity = null;
		this.annotations = new TreeSet<ObjectAnnotation>();
		this.defaultModification = null;
		this.suggestedModification = null;
		this.binnerAnnotation = null;
		this.eventListeners = ConcurrentHashMap.newKeySet();
		this.neutralMass = 0.0d;
		
		createDefaultPrimaryIdentity();
	}

	public MsFeature(
			String name, 
			double neutralMass, 
			double retentionTime) {

		this();
		this.name = name;
		this.neutralMass = neutralMass;
		this.retentionTime = retentionTime;
		rtRange = new Range(retentionTime);
	}

	public MsFeature(String name, double retentionTime) {

		this();
		this.name = name;
		this.retentionTime = retentionTime;
		rtRange = new Range(retentionTime);
	}

	public MsFeature(String id, String name, double retentionTime) {

		this();
		this.id = id;
		this.name = name;
		this.retentionTime = retentionTime;
		rtRange = new Range(retentionTime);
	}

	public MsFeature(double retentionTime, Polarity polarity) {
		this();
		this.retentionTime = retentionTime;
		rtRange = new Range(retentionTime);
		this.polarity = polarity;
	}
	
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
		identifications = 
				new HashSet<MsFeatureIdentity>(source.getIdentifications());
		primaryIdentity = source.getPrimaryIdentity();
		annotatedObjectType = source.getAnnotatedObjectType();
		annotations = 
				new TreeSet<ObjectAnnotation>(source.getAnnotations());

		defaultModification = source.getDefaultChemicalModification();
		suggestedModification = null;
		binnerAnnotation = source.getBinnerAnnotation();
		eventListeners = ConcurrentHashMap.newKeySet();
		eventListeners.addAll(source.getFeatureListeners());
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
	 * */
	public boolean addIdentity(MsFeatureIdentity cid) {

		if(cid == null)
			return false;

		identifications.add(cid);
		return true;
	}

	public void removeIdentity(MsFeatureIdentity cid) {

		MsFeatureIdentity defaultUnknown = getDefaultPrimaryIdentity();
		identifications.remove(cid);
		if(cid.equals(primaryIdentity)) {

			primaryIdentity = defaultUnknown;
			if(primaryIdentity == null)
				createDefaultPrimaryIdentity();
		}
		setStatus(ParameterSetStatus.CHANGED);
	}

	public void clearIdentification() {

		primaryIdentity = getDefaultPrimaryIdentity();
		identifications.clear();		
		if(primaryIdentity == null)
			createDefaultPrimaryIdentity();

		setStatus(ParameterSetStatus.CHANGED);
	}

	public void setTopScoreIdAsDefault() {

		if(!identifications.isEmpty()) {

			identifications.stream().forEach(i -> i.setPrimary(false));
			MsFeatureIdentity[] ids = 
					identifications.toArray(new MsFeatureIdentity[identifications.size()]);
			Arrays.sort(ids, new MsFeatureIdentityComparator(SortProperty.Quality));
			primaryIdentity = ids[0];
			primaryIdentity.setPrimary(true);
		}
	}

	public Set<MsFeatureIdentity> getIdentifications() {
		return identifications;
	}
	
	public Set<MsFeatureIdentity> getMSRTIdentifications() {
		
		return identifications.stream().
				filter(id -> Objects.nonNull(id.getMsRtLibraryMatch())).
				collect(Collectors.toSet());
	}
	
	public Set<MsFeatureIdentity> getMSMSIdentifications() {
		
		return identifications.stream().
				filter(id -> Objects.nonNull(id.getReferenceMsMsLibraryMatch())).
				collect(Collectors.toSet());
	}
	
	public MsFeatureIdentity getIdentityByPartialName(String partName) {
		
		if(identifications == null || identifications.isEmpty())
			return null;
		
		String upName = partName.toUpperCase();
		return identifications.stream().
				filter(i -> i.getCompoundName().toUpperCase().contains(upName)).
				findFirst().orElse(null);
	}
	
	public void setIdentifications(HashSet<MsFeatureIdentity> identifications) {

		this.identifications = identifications;
		setStatus(ParameterSetStatus.CHANGED);
	}
	
	public void disablePrimaryIdentity() {

		identifications.stream().forEach(i -> i.setPrimary(false));
		primaryIdentity = getDefaultPrimaryIdentity();
		if(primaryIdentity == null)
			createDefaultPrimaryIdentity();

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

		if ((defaultModification == null && newChemMod != null) 
				|| (defaultModification != null && newChemMod == null)) {

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
		
		if(primaryIdentity == null 
				|| primaryIdentity.getCompoundIdentity() == null)
			return false;
		else
			return true;
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
		
		if(spectrum.getExperimentalTandemSpectrum() != null) {
			updateUnknownPrimaryIdentityBasedOnMSMS();
		}
		else {
			if(primaryIdentity == null)
				createDefaultPrimaryIdentity();
			
			String newName = DataPrefix.MS_LIBRARY_UNKNOWN_TARGET.getName() +
					MRC2ToolBoxConfiguration.getMzFormat().format(getMonoisotopicMz()) + "_" + 
					MRC2ToolBoxConfiguration.getRtFormat().format(getRetentionTime());
			primaryIdentity.setIdentityName(newName);
			primaryIdentity.setConfidenceLevel(
					CompoundIdentificationConfidence.UNKNOWN_ACCURATE_MASS_RT);
		}		
//		if(primaryIdentity != null 
//				&& primaryIdentity.getIdentityName().equals(DEFAULT_ID_NAME) 
//				&& spectrum != null) {
//			
//			if(spectrum.getExperimentalTandemSpectrum() != null 
//					&& spectrum.getExperimentalTandemSpectrum().getParent() != null) {
//				
//				double rt  = spectrum.getExperimentalTandemSpectrum().getParentScanRetentionTime();
//				String newName = DataPrefix.MS_LIBRARY_UNKNOWN_TARGET.getName() +
//						MRC2ToolBoxConfiguration.defaultMzFormat.format(
//								spectrum.getExperimentalTandemSpectrum().getParent().getMz()) + "_" + 
//						MRC2ToolBoxConfiguration.defaultRtFormat.format(rt);
//				primaryIdentity.setIdentityName(newName);
//				primaryIdentity.setConfidenceLevel(
//						CompoundIdentificationConfidence.UNKNOWN_MSMS_RT);
//			}
//			else {
//				String newName = DataPrefix.MS_LIBRARY_UNKNOWN_TARGET.getName() +
//						MRC2ToolBoxConfiguration.getMzFormat().format(getMonoisotopicMz()) + "_" + 
//						MRC2ToolBoxConfiguration.getRtFormat().format(getRetentionTime());
//				primaryIdentity.setIdentityName(newName);
//				primaryIdentity.setConfidenceLevel(
//						CompoundIdentificationConfidence.UNKNOWN_ACCURATE_MASS_RT);
//			}
//		}
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
				absCharge = spectrum.getAbsoluteChargeFromIsotopicPattern();
		}
		return absCharge;
	}

	public int getCharge() {
		
		if (spectrum == null || spectrum.getPrimaryAdduct() == null)
			return polarity.getSign();
		else
			return spectrum.getPrimaryAdduct().getCharge();
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
		
		if(neutralMass > 0.0d)
			return neutralMass;

		if(primaryIdentity != null && primaryIdentity.getCompoundIdentity() != null)
			return primaryIdentity.getCompoundIdentity().getExactMass();

		if (spectrum != null && spectrum.getPrimaryAdduct() != null) {
			
			MsPoint[] ms = spectrum.getMsForAdduct(spectrum.getPrimaryAdduct());			
			if(ms != null)
				neutralMass = MsUtils.getNeutralMass(ms[0], spectrum.getPrimaryAdduct());		
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
		
		if(polarity != null)
			screenName += " (" + polarity.getSign() + ")";
			
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
		long idCount = identifications.stream().
				filter(i -> Objects.nonNull(i.getCompoundIdentity())).count();
		
		if(primaryIdentity == null || primaryIdentity.getCompoundIdentity() == null) {
			
			if(idCount == 1)
				return FeatureIdentificationState.SINGLE_INACTIVE_ID;
			else if(idCount > 1)
				return FeatureIdentificationState.MULTIPLE_INACTIVE_IDS;
			else
				return FeatureIdentificationState.NO_IDENTIFICATION;
		}
		else {
			if(idCount == 1)
				return FeatureIdentificationState.SINGLE_ACTIVE_ID;
			else
				return FeatureIdentificationState.MULTIPLE_ACTIVE_IDS;
		}
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
				filter(id -> Objects.nonNull(id.getReferenceMsMsLibraryMatch())).
				collect(Collectors.toList());
		
		if(msmsHits.isEmpty())
			return 0;
		else
			return msmsHits.size();
	}
	
	public Element getXmlElement() {
		
		Element msFeatureElement = 
				new Element(MsFeatureFields.MsFeature.name());
		msFeatureElement.setAttribute(MsFeatureFields.Id.name(), id);	
		msFeatureElement.setAttribute(MsFeatureFields.Name.name(), name);
		msFeatureElement.setAttribute(MsFeatureFields.rt.name(), Double.toString(retentionTime));
		if(rtRange != null)
			msFeatureElement.setAttribute(MsFeatureFields.rtRange.name(), rtRange.getStorableString());
		
		if(polarity != null)
			msFeatureElement.setAttribute(MsFeatureFields.pol.name(), polarity.getCode());
		
		//	Spectrum
		if(spectrum != null) 
			msFeatureElement.addContent(spectrum.getXmlElement());
		
		//	Identifications
		if(primaryIdentity != null)
			primaryIdentity.setPrimary(true);
			
		if(!identifications.isEmpty()) {
			
			Element cidListElement = 
					new Element(MsFeatureFields.CIDs.name());					
			
			for(MsFeatureIdentity mscid : identifications)			
				cidListElement.addContent(mscid.getXmlElement());	
			
			msFeatureElement.addContent(cidListElement);
		}	
		msFeatureElement.setAttribute(
				MsFeatureFields.IdDisabled.name(), Boolean.toString(idDisabled));	
				
		if(qualityScore > 0)
			msFeatureElement.setAttribute(
					MsFeatureFields.QS.name(), Double.toString(qualityScore));
		
		return msFeatureElement;
	}

	public MsFeature(Element featureElement) {
		
//		identifications = new HashSet<MsFeatureIdentity>();
//		annotations = new TreeSet<ObjectAnnotation>();
//		eventListeners = ConcurrentHashMap.newKeySet();
//		createDefaultPrimaryIdentity();
		this();

		id = featureElement.getAttributeValue(MsFeatureFields.Id.name());
		name = featureElement.getAttributeValue(MsFeatureFields.Name.name());
		retentionTime = Double.parseDouble(
				featureElement.getAttributeValue(MsFeatureFields.rt.name()));
		String polCode = featureElement.getAttributeValue(MsFeatureFields.pol.name());
		if(polCode != null)
			polarity = Polarity.getPolarityByCode(polCode);
		
		String rtRangeString = 
				featureElement.getAttributeValue(MsFeatureFields.rtRange.name());
		if(rtRangeString != null)
			rtRange = new Range(rtRangeString);
		
		setSpectrum(new MassSpectrum(
				featureElement.getChild(MassSpectrumFields.Spectrum.name())));	
		String qsValue = 
				featureElement.getAttributeValue(MsFeatureFields.QS.name());
		if(qsValue != null)
			qualityScore = Double.parseDouble(qsValue);
		
		//	Identifications
		List<Element> msfIdListElements = 
				featureElement.getChildren(MsFeatureFields.CIDs.name());
		if(msfIdListElements.size() > 0) {
			
			primaryIdentity = null;
			identifications.clear();
			
			List<Element> msfIdList = 
					msfIdListElements.get(0).getChildren(MsFeatureIdentityFields.MSFID.name());
			for(Element msfIdElement : msfIdList) {
				
				MsFeatureIdentity msfId = new MsFeatureIdentity(msfIdElement);
				if(spectrum != null && spectrum.getExperimentalTandemSpectrum() != null 
						&& msfId.getReferenceMsMsLibraryMatch() != null) {
					
					msfId.getReferenceMsMsLibraryMatch().setEntropyBasedScore(
							MSMSScoreCalculator.calculateDefaultEntropyMatchScore(
									spectrum.getExperimentalTandemSpectrum(), 
									msfId.getReferenceMsMsLibraryMatch()));
				}
				if(msfId.isPrimary())
					setPrimaryIdentity(msfId);
				else
					addIdentity(msfId);
			}
			String idDisabledString  = 
					featureElement.getAttributeValue(MsFeatureFields.IdDisabled.name());
			if(idDisabledString != null)
				idDisabled = Boolean.parseBoolean(idDisabledString);
			else
				idDisabled = false;
		}
	}
	
	private void createDefaultPrimaryIdentity() {
		
		MsFeatureIdentity defaultId = new MsFeatureIdentity(
				null, CompoundIdentificationConfidence.UNKNOWN_ACCURATE_MASS_RT);
		defaultId.setIdentityName(DEFAULT_ID_NAME);
		setPrimaryIdentity(defaultId);
		if(spectrum == null)
			return;
		
		if(spectrum.getExperimentalTandemSpectrum() != null 
				&& spectrum.getExperimentalTandemSpectrum().getParent() != null) {
			
			double rt  = spectrum.getExperimentalTandemSpectrum().getParentScanRetentionTime();			
			String newName = DataPrefix.MS_LIBRARY_UNKNOWN_TARGET.getName() +
					MRC2ToolBoxConfiguration.defaultMzFormat.format(
							spectrum.getExperimentalTandemSpectrum().getParent().getMz()) + "_" + 
					MRC2ToolBoxConfiguration.defaultRtFormat.format(rt);
			primaryIdentity.setIdentityName(newName);
			primaryIdentity.setConfidenceLevel(
					CompoundIdentificationConfidence.UNKNOWN_MSMS_RT);
		}
		else {
			String newName = DataPrefix.MS_LIBRARY_UNKNOWN_TARGET.getName() +
					MRC2ToolBoxConfiguration.getMzFormat().format(getMonoisotopicMz()) + "_" + 
					MRC2ToolBoxConfiguration.getRtFormat().format(getRetentionTime());
			primaryIdentity.setIdentityName(newName);
			primaryIdentity.setConfidenceLevel(
					CompoundIdentificationConfidence.UNKNOWN_ACCURATE_MASS_RT);
		}
	}
	
	private MsFeatureIdentity getDefaultPrimaryIdentity() {
		
		if(identifications == null || identifications.isEmpty())
			return null;
		
		return identifications.stream().
				filter(i -> Objects.isNull(i.getCompoundIdentity())).
				findFirst().orElse(null);
	}
	
	public void updateUnknownPrimaryIdentityBasedOnMSMS() {
		
		if(primaryIdentity == null) {
			createDefaultPrimaryIdentity();
		}
		else {
			if(primaryIdentity.getCompoundIdentity() == null) {
				
				if(spectrum != null && spectrum.getExperimentalTandemSpectrum() != null 
						&& spectrum.getExperimentalTandemSpectrum().getParent() != null) {
					
					double rt  = spectrum.getExperimentalTandemSpectrum().getParentScanRetentionTime();	
					if(rt == 0.0d)
						rt = retentionTime;
					
					String newName = DataPrefix.MS_LIBRARY_UNKNOWN_TARGET.getName() +
							MRC2ToolBoxConfiguration.defaultMzFormat.format(
									spectrum.getExperimentalTandemSpectrum().getParent().getMz()) + "_" + 
							MRC2ToolBoxConfiguration.defaultRtFormat.format(rt);
					primaryIdentity.setIdentityName(newName);
					primaryIdentity.setConfidenceLevel(
							CompoundIdentificationConfidence.UNKNOWN_MSMS_RT);
				}
			}
		}
	}
	
	public String getTargetId() {
		
//		String targetId = identifications.stream().
//			filter(i -> i.getCompoundIdentity() != null).
//			map(i -> i.getCompoundIdentity()).
//			flatMap(c -> c.getDbIdMap().values().stream()).
//			filter(id -> (id.startsWith(DataPrefix.MS_LIBRARY_TARGET.getName())
//					|| id.startsWith(DataPrefix.MS_FEATURE.getName()))).
//			findFirst().orElse(null);
		
		return targetId;
	}

	public void setTargetId(String targetId) {
		this.targetId = targetId;
	}
	
	public double getArea() {
		return area;
	}

	public void setArea(double area) {
		this.area = area;
	}

	public double getHeight() {
		return height;
	}

	public void setHeight(double height) {
		this.height = height;
	}
	
	public double getAbsoluteMassDefectForPrimaryAdduct() {
		
		if(spectrum.getPrimaryAdduct() == null)
			return 0.0d;
		
		double neutralMass = MsUtils.getNeutralMassForAdduct(
				spectrum.getPrimaryAdductBasePeakMz(), spectrum.getPrimaryAdduct());
		
		//	return Math.abs(neutralMass - (double)Math.round(neutralMass));
		
		return neutralMass % 1;
	}
	
	public double getFractionalMassDefect() {
		
		if(spectrum.getPrimaryAdduct() == null)
			return 0.0d;
		
		return spectrum.getPrimaryAdductBasePeakMz() % 1;
	}
}








































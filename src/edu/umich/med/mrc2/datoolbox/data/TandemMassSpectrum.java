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
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;

import edu.umich.med.mrc2.datoolbox.data.compare.MsDataPointComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortDirection;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.enums.AnnotatedObjectType;
import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.data.enums.Polarity;
import edu.umich.med.mrc2.datoolbox.data.enums.SpectrumSource;
import edu.umich.med.mrc2.datoolbox.data.lims.ObjectAnnotation;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.utils.MsUtils;
import edu.umich.med.mrc2.datoolbox.utils.Range;

public class TandemMassSpectrum implements AnnotatedObject, Serializable {

	/**
	 *
	 */
	protected static final long serialVersionUID = -6286188017684801456L;

	protected String uniqueId;
	protected SpectrumSource spectrumSource;
	protected String detectionAlgorithm;
	protected int depth;
	protected MsPoint parent;
	protected Collection<MsPoint>spectrum;
	protected double fragmenterVoltage;
	protected double cidLevel;
	protected String ionisationType;
	protected int scanNumber;
	protected int parentScanNumber;
	protected Range isolationWindow;
	protected TreeSet<ObjectAnnotation> annotations;
	protected Polarity polarity;
	protected String description;
	protected AnnotatedObjectType annotatedObjectType;
	protected double totalIntensity;
	protected double entropy;
	protected Collection<MsPoint>minorParentIons;
	protected Map<Integer,Integer>averagedScanNumbers;
	protected boolean parentIonIsMinorIsotope;

	public TandemMassSpectrum(
			int depth,
			MsPoint parent,
			Collection<MsPoint> spectrum,
			Polarity polarity) {
		super();
		this.depth = depth;
		this.parent = parent;
		this.spectrum = spectrum;
		this.polarity = polarity;

		uniqueId = DataPrefix.MSMS_SPECTRUM.getName() + 
				UUID.randomUUID().toString().substring(0, 12);
		annotations = new TreeSet<ObjectAnnotation>();
		minorParentIons = new HashSet<MsPoint>();
		averagedScanNumbers = new TreeMap<Integer,Integer>();
	}

	public TandemMassSpectrum(int depth, MsPoint parent, Polarity polarity) {
		super();
		this.depth = depth;
		this.parent = parent;
		this.polarity = polarity;
		spectrum = new HashSet<MsPoint>();
		uniqueId = DataPrefix.MSMS_SPECTRUM.getName() + 
				UUID.randomUUID().toString().substring(0, 12);
		annotations = new TreeSet<ObjectAnnotation>();
		minorParentIons = new HashSet<MsPoint>();
		averagedScanNumbers = new TreeMap<Integer,Integer>();
	}

	public TandemMassSpectrum(TandemMassSpectrum source) {
		super();
		depth = source.getDepth();
		parent = new MsPoint(source.getParent());
		polarity = source.getPolarity();
		spectrum = new HashSet<MsPoint>();
		source.getSpectrum().stream().forEach(dp -> spectrum.add(new MsPoint(dp)));
		fragmenterVoltage = source.getFragmenterVoltage();
		cidLevel = source.getCidLevel();
		uniqueId = source.getId();
		annotations = new TreeSet<ObjectAnnotation>();
		annotations.addAll(source.getAnnotations());
		description = source.getDescription();
		scanNumber = source.getScanNumber();
		parentScanNumber = source.getParentScanNumber();
		
		minorParentIons = new HashSet<MsPoint>();
		minorParentIons.addAll(source.getMinorParentIons());
		averagedScanNumbers = new TreeMap<Integer,Integer>();
		averagedScanNumbers.putAll(source.getAveragedScanNumbers());
	}

	public TandemMassSpectrum(
			String uniqueId,
			int depth,
			double fragmenterVoltage,
			double cidLevel,
			Polarity polarity) {
		super();
		this.uniqueId = uniqueId;
		this.depth = depth;
		this.fragmenterVoltage = fragmenterVoltage;
		this.cidLevel = cidLevel;
		this.polarity = polarity;
		spectrum = new HashSet<MsPoint>();
		annotations = new TreeSet<ObjectAnnotation>();
		minorParentIons = new HashSet<MsPoint>();
		averagedScanNumbers = new TreeMap<Integer,Integer>();
	}

	public TandemMassSpectrum(Polarity polarity) {
		super();
		this.uniqueId = DataPrefix.MSMS_SPECTRUM.getName() + 
				UUID.randomUUID().toString().substring(0, 12);;
		this.depth = 2;
		this.polarity = polarity;
		spectrum = new HashSet<MsPoint>();
		annotations = new TreeSet<ObjectAnnotation>();
		minorParentIons = new HashSet<MsPoint>();
		averagedScanNumbers = new TreeMap<Integer,Integer>();
	}

	public int getDepth() {
		return depth;
	}
	public void setDepth(int depth) {
		this.depth = depth;
	}
	public MsPoint getParent() {
		return parent;
	}
	public void setParent(MsPoint parent) {
		this.parent = parent;
	}
	public Collection<MsPoint> getSpectrum() {
		return spectrum;
	}
	public void setSpectrum(Collection<MsPoint> spectrum) {
		this.spectrum = spectrum;
	}

	public MsPoint[] getMassSortedSpectrum() {

		return spectrum.stream().sorted(new MsDataPointComparator(SortProperty.MZ)).
				toArray(size -> new MsPoint[size]);
	}

	public MsPoint[] getNormalizedMassSortedSpectrum() {

		double maxIntensity = spectrum.stream().
			sorted(MsUtils.reverseIntensitySorter).
			findFirst().get().getIntensity();

		return spectrum.stream().sorted(new MsDataPointComparator(SortProperty.MZ)).
				map(p -> new MsPoint(p.getMz(), p.getIntensity()/maxIntensity * 1000.0d)).
				toArray(size -> new MsPoint[size]);
	}
	
	public MsPoint getBasePeak() {
		
		if(spectrum.isEmpty())
			return null;
		
		return spectrum.stream().
			sorted(MsUtils.reverseIntensitySorter).
			findFirst().get();
	}

	/**
	 * @return the fragmenterVoltage
	 */
	public double getFragmenterVoltage() {
		return fragmenterVoltage;
	}

	/**
	 * @return the cidLevel
	 */
	public double getCidLevel() {
		return cidLevel;
	}

	/**
	 * @param fragmenterVoltage the fragmenterVoltage to set
	 */
	public void setFragmenterVoltage(double fragmenterVoltage) {
		this.fragmenterVoltage = fragmenterVoltage;
	}

	/**
	 * @param cidLevel the cidLevel to set
	 */
	public void setCidLevel(double cidLevel) {
		this.cidLevel = cidLevel;
	}

	public MsPoint getActualParentIon() {

		Range parentRange = MsUtils.createPpmMassRange(parent.getMz(), MRC2ToolBoxConfiguration.getMassAccuracy());

		Optional<MsPoint>actualParent = spectrum.stream().filter(dp -> parentRange.contains(dp.getMz())).
				sorted(new MsDataPointComparator(SortProperty.Intensity, SortDirection.DESC)).findFirst();

		if(actualParent.isPresent())
			return actualParent.get();
		else
			return null;
	}

	public String getUserFriendlyId() {

		String idString = uniqueId;
		//	TODO

		return idString;
	}

	@Override
	public String toString() {
		return getUserFriendlyId();
	}

	/**
	 * @return the uniqueId
	 */
	public String getId() {
		return uniqueId;
	}

	/*
	 * Annotation
	 * */
	@Override
	public void addAnnotation(ObjectAnnotation annotation) {
		annotations.add(annotation);
	}

	@Override
	public void removeAnnotation(ObjectAnnotation annotation) {
		annotations.remove(annotation);
	}

	/**
	 * @return the annotations
	 */
	@Override
	public Collection<ObjectAnnotation> getAnnotations() {
		return annotations;
	}

	@Override
	public void setId(String uniqueId) {
		this.uniqueId = uniqueId;
	}

	/**
	 * @return the scanNumber
	 */
	public int getScanNumber() {
		return scanNumber;
	}

	/**
	 * @return the parentScanNumber
	 */
	public int getParentScanNumber() {
		return parentScanNumber;
	}

	/**
	 * @return the isolationWindow
	 */
	public Range getIsolationWindow() {
		return isolationWindow;
	}

	/**
	 * @param scanNumber the scanNumber to set
	 */
	public void setScanNumber(int scanNumber) {
		this.scanNumber = scanNumber;
	}

	/**
	 * @param parentScanNumber the parentScanNumber to set
	 */
	public void setParentScanNumber(int parentScanNumber) {
		this.parentScanNumber = parentScanNumber;
	}

	/**
	 * @param isolationWindow the isolationWindow to set
	 */
	public void setIsolationWindow(Range isolationWindow) {
		this.isolationWindow = isolationWindow;
	}

	/**
	 * @return the ionisationType
	 */
	public String getIonisationType() {
		return ionisationType;
	}

	/**
	 * @param ionisationType the ionisationType to set
	 */
	public void setIonisationType(String ionisationType) {
		this.ionisationType = ionisationType;
	}

	/**
	 * @return the detectionAlgorithm
	 */
	public String getDetectionAlgorithm() {
		return detectionAlgorithm;
	}

	/**
	 * @param detectionAlgorithm the detectionAlgorithm to set
	 */
	public void setDetectionAlgorithm(String detectionAlgorithm) {
		this.detectionAlgorithm = detectionAlgorithm;
	}

	/**
	 * @return the spectrumSource
	 */
	public SpectrumSource getSpectrumSource() {
		return spectrumSource;
	}

	/**
	 * @param spectrumSource the spectrumSource to set
	 */
	public void setSpectrumSource(SpectrumSource spectrumSource) {
		this.spectrumSource = spectrumSource;
	}

	/**
	 * @return the polarity
	 */
	public Polarity getPolarity() {
		return polarity;
	}

	/**
	 * @param polarity the polarity to set
	 */
	public void setPolarity(Polarity polarity) {
		this.polarity = polarity;
	}

    @Override
    public boolean equals(Object obj) {

		if (obj == this)
			return true;

        if (obj == null)
            return false;

        if (!TandemMassSpectrum.class.isAssignableFrom(obj.getClass()))
            return false;

        final TandemMassSpectrum other = (TandemMassSpectrum) obj;

        if ((this.polarity == null) ? (other.getPolarity() != null) :
    		!this.polarity.equals(other.getPolarity()))
        return false;

        if ((this.spectrumSource == null) ? (other.getSpectrumSource() != null) :
    		!this.spectrumSource.equals(other.getSpectrumSource()))
        return false;

        if (this.parent.getMz() != other.getParent().getMz())
        	return false;

        if (this.cidLevel != other.getCidLevel())
        	return false;

        if ((this.ionisationType == null) ? (other.getIonisationType() != null) :
    		!this.ionisationType.equals(other.getIonisationType()))
        return false;

        if ((this.isolationWindow == null) ? (other.getIsolationWindow() != null) :
    		!this.isolationWindow.equals(other.getIsolationWindow()))
        return false;

        String spectrumHash = MsUtils.calculateSpectrumHash(spectrum);
        String otherHash = MsUtils.calculateSpectrumHash(other.getSpectrum());
        if(!spectrumHash.equals(otherHash))
        	return false;

        return true;
    }

    @Override
    public int hashCode() {

//        int hash =  MsUtils.calculateSpectrumHash(spectrum).hashCode();
//        hash = 53 * hash +
//        		spectrumSource.name().hashCode() +
//        		polarity.name().hashCode() +	//	TODO make sure polarity is always present
//        		Double.toString(cidLevel).hashCode() +
//        		Double.toString(parent.getMz()).hashCode();
//        if(ionisationType != null)
//        	hash += ionisationType.hashCode();

        return MsUtils.calculateSpectrumHash(spectrum).hashCode();
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
	public void setAnnotatedObjectType(AnnotatedObjectType type) {
		this.annotatedObjectType = type;
	}

	@Override
	public AnnotatedObjectType getAnnotatedObjectType() {
		return annotatedObjectType;
	}

	public double getTotalIntensity() {
		return spectrum.stream().mapToDouble(p -> p.getIntensity()).sum();
	}

	public double getEntropy() {
		return entropy;
	}

	public void setEntropy(double entropy) {
		this.entropy = entropy;
	}
	
	public void setSpecrum(Collection<MsPoint>newSpectrum) {
		spectrum.clear();
		spectrum.addAll(newSpectrum);
	}

	public Collection<MsPoint> getMinorParentIons() {
		return minorParentIons;
	}

	public Map<Integer,Integer>getAveragedScanNumbers() {
		return averagedScanNumbers;
	}
	
	public void addAveragedScanNumbers(int msmsScan, int parentScan) {
		averagedScanNumbers.put(msmsScan, parentScan);
	}

	public void setMinorParentIons(Collection<MsPoint> newMinorParentIons) {
		minorParentIons.clear();
		minorParentIons.addAll(newMinorParentIons);
	}
	
	public double getParentIonPurity() {
		
		if(minorParentIons.isEmpty() || parent == null)
			return 1.0;
		
		return parent.getIntensity() / (minorParentIons.stream().
				mapToDouble(p -> p.getIntensity()).sum() + parent.getIntensity());
	}

	public boolean isParentIonMinorIsotope() {
		return parentIonIsMinorIsotope;
	}

	public void setParentIonIsMinorIsotope(boolean parentIonIsMinorIsotope) {
		this.parentIonIsMinorIsotope = parentIonIsMinorIsotope;
	}
}


















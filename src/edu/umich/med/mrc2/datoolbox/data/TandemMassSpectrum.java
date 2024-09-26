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
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.jdom2.Element;

import edu.umich.med.mrc2.datoolbox.data.enums.AnnotatedObjectType;
import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.data.enums.Polarity;
import edu.umich.med.mrc2.datoolbox.data.enums.SpectrumSource;
import edu.umich.med.mrc2.datoolbox.data.lims.ObjectAnnotation;
import edu.umich.med.mrc2.datoolbox.project.store.TandemMassSpectrumFields;
import edu.umich.med.mrc2.datoolbox.utils.MsUtils;
import edu.umich.med.mrc2.datoolbox.utils.NumberArrayUtils;
import edu.umich.med.mrc2.datoolbox.utils.Range;

/**
 * @author Sasha
 *
 */
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
	protected Map<Integer,Double>scanRtMap;
	protected boolean parentIonIsMinorIsotope;
	protected double parentIonPurity = 1.0d;
	protected boolean hasScans;

	public TandemMassSpectrum(
			int depth,
			MsPoint parent,
			Collection<MsPoint> spectrum,
			Polarity polarity) {
		super();
		initSpectrum();
		this.depth = depth;
		this.parent = parent;
		this.spectrum = spectrum;
		this.polarity = polarity;
	}

	public TandemMassSpectrum(int depth, MsPoint parent, Polarity polarity) {
		super();
		initSpectrum();
		this.depth = depth;
		this.parent = parent;
		this.polarity = polarity;
	}	

	public TandemMassSpectrum(
			String uniqueId,
			int depth,
			double fragmenterVoltage,
			double cidLevel,
			Polarity polarity) {
		super();
		initSpectrum();
		this.uniqueId = uniqueId;
		this.depth = depth;
		this.fragmenterVoltage = fragmenterVoltage;
		this.cidLevel = cidLevel;
		this.polarity = polarity;
	}

	public TandemMassSpectrum(Polarity polarity) {
		super();
		initSpectrum();
		this.depth = 2;
		this.polarity = polarity;		
	}
	
	private void initSpectrum() {
		
		this.uniqueId = DataPrefix.MSMS_SPECTRUM.getName() + 
				UUID.randomUUID().toString().substring(0, 12);
		spectrum = new TreeSet<MsPoint>(MsUtils.mzSorter);
		annotations = new TreeSet<ObjectAnnotation>();
		minorParentIons = new TreeSet<MsPoint>(MsUtils.mzSorter);
		averagedScanNumbers = new TreeMap<Integer,Integer>();
		scanRtMap = new TreeMap<Integer,Double>();
	}

	public TandemMassSpectrum(TandemMassSpectrum source) {
		super();
		depth = source.getDepth();
		parent = new MsPoint(source.getParent());
		polarity = source.getPolarity();
		spectrum = new TreeSet<MsPoint>(MsUtils.mzSorter);
		source.getSpectrum().stream().forEach(dp -> spectrum.add(new MsPoint(dp)));
		fragmenterVoltage = source.getFragmenterVoltage();
		cidLevel = source.getCidLevel();
		uniqueId = source.getId();
		annotations = new TreeSet<ObjectAnnotation>();
		annotations.addAll(source.getAnnotations());
		description = source.getDescription();
		scanNumber = source.getScanNumber();
		parentScanNumber = source.getParentScanNumber();
		
		minorParentIons = new TreeSet<MsPoint>(MsUtils.mzSorter);
		minorParentIons.addAll(source.getMinorParentIons());
		averagedScanNumbers = new TreeMap<Integer,Integer>();
		averagedScanNumbers.putAll(source.getAveragedScanNumbers());		
		scanRtMap = new TreeMap<Integer,Double>();
		scanRtMap.putAll(source.getScanRtMap());
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

	public MsPoint[] getMassSortedSpectrum() {

		return spectrum.stream().sorted(MsUtils.mzSorter).
				toArray(size -> new MsPoint[size]);
	}

	public MsPoint[] getNormalizedMassSortedSpectrum() {
		
		return MsUtils.normalizeAndSortMsPattern(spectrum);
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
		
		if(parent == null)
			return null;

		Range parentRange = new Range(
				parent.getMz() - 0.1, parent.getMz() + 0.1);
		MsPoint actualParent = 
				spectrum.stream().filter(dp -> parentRange.contains(dp.getMz())).
				sorted(MsUtils.reverseIntensitySorter).findFirst().orElse(null);
		return actualParent;
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
        
        if (this.parent == null && other.getParent() != null)
        	return false;
        
    	if(this.parent != null && other.getParent() == null)
    		return false;
    	
    	if(this.parent != null && other.getParent() != null
    			&& this.parent.getMz() != other.getParent().getMz())
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
		
		if(entropy == 0.0)
			entropy = MsUtils.calculateCleanedSpectrumEntropyNatLog(spectrum);
		
		return entropy;
	}

	public void setEntropy(double entropy) {
		this.entropy = entropy;
	}
	
	public void setSpectrum(Collection<MsPoint>newSpectrum) {
		spectrum.clear();
		spectrum.addAll(newSpectrum);
	}

	public Collection<MsPoint> getMinorParentIons() {
		return minorParentIons;
	}

	public Map<Integer,Integer>getAveragedScanNumbers() {
		return averagedScanNumbers;
	}
	
	/**
	 * Add pairs of MSMS scan and it's parent MS1 scan
	 * @param msmsScan
	 * @param parentScan
	 */
	public void addAveragedScanNumbers(int msmsScan, int parentScan) {
		averagedScanNumbers.put(msmsScan, parentScan);
	}

	public void setMinorParentIons(
			Collection<MsPoint> newMinorParentIons, 
			MsPoint msOneParent) {
		minorParentIons.clear();
		minorParentIons.addAll(newMinorParentIons);
		calculateParentIonPurity(msOneParent);
	}
	
	public double getParentIonPurity() {		
		return parentIonPurity;
	}
	
	private void calculateParentIonPurity(MsPoint msOneParent) {
		
		if(minorParentIons.isEmpty() || msOneParent == null)
			parentIonPurity = 1.0;
		else
			parentIonPurity = msOneParent.getIntensity() / (minorParentIons.stream().
				mapToDouble(p -> p.getIntensity()).sum() + msOneParent.getIntensity());
	}

	public boolean isParentIonMinorIsotope() {
		return parentIonIsMinorIsotope;
	}

	public void setParentIonIsMinorIsotope(boolean parentIonIsMinorIsotope) {
		this.parentIonIsMinorIsotope = parentIonIsMinorIsotope;
	}
	
	public Element getXmlElement() {
		
		Element msmsElement = 
				new Element(TandemMassSpectrumFields.MSMS.name());
		
		msmsElement.setAttribute(TandemMassSpectrumFields.Id.name(), uniqueId);
		if(spectrumSource != null)
			msmsElement.setAttribute(TandemMassSpectrumFields.Source.name(), spectrumSource.name());
		
		if(detectionAlgorithm != null)
			msmsElement.setAttribute(
					TandemMassSpectrumFields.Algo.name(), detectionAlgorithm);
		
		msmsElement.setAttribute(
				TandemMassSpectrumFields.Depth.name(), Integer.toString(depth));
		
		if(parent != null)
			msmsElement.setAttribute(
					TandemMassSpectrumFields.Parent.name(), parent.toString());
		
		if(spectrum != null && !spectrum.isEmpty()) {
			
			double[]mzValues = spectrum.stream().
					mapToDouble(p -> p.getMz()).toArray();
			String mz = "";
			try {
				mz = NumberArrayUtils.encodeNumberArray(mzValues);
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Element mzElement = 
					new Element(TandemMassSpectrumFields.MZ.name()).
					setText(mz);			
			msmsElement.addContent(mzElement);
			
			double[]intensityValues = spectrum.stream().
					mapToDouble(p -> p.getIntensity()).toArray();
			String intensity = "";
			try {
				intensity = NumberArrayUtils.encodeNumberArray(intensityValues);
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Element intensityElement = 
					new Element(TandemMassSpectrumFields.Intensity.name()).
					setText(intensity);			
			msmsElement.addContent(intensityElement);
		}
		if(fragmenterVoltage > 0.0d)
			msmsElement.setAttribute(TandemMassSpectrumFields.FragV.name(), 
					Double.toString(fragmenterVoltage));
		
		if(cidLevel > 0.0d)
			msmsElement.setAttribute(TandemMassSpectrumFields.CID.name(), 
					Double.toString(cidLevel));
		
		if(ionisationType != null)
			msmsElement.setAttribute(TandemMassSpectrumFields.Ioniz.name(), ionisationType);
		
		if(scanNumber > 0.0d)
			msmsElement.setAttribute(TandemMassSpectrumFields.Scan.name(), 
					Integer.toString(scanNumber));
		
		if(parentScanNumber > 0.0d)
			msmsElement.setAttribute(TandemMassSpectrumFields.PScan.name(), 
					Integer.toString(parentScanNumber));
		
		if(isolationWindow != null)
			msmsElement.setAttribute(TandemMassSpectrumFields.IsolWindow.name(), 
					isolationWindow.getStorableString());
		
		if(polarity != null)
			msmsElement.setAttribute(TandemMassSpectrumFields.Pol.name(), polarity.getCode());
		
		if(description != null)
			msmsElement.setAttribute(TandemMassSpectrumFields.Desc.name(), description);
		
		if(annotatedObjectType != null)
			msmsElement.setAttribute(TandemMassSpectrumFields.AOT.name(), annotatedObjectType.name());
		
		
		msmsElement.setAttribute(TandemMassSpectrumFields.ParentIonPurity.name(), Double.toString(parentIonPurity));
		
		if(minorParentIons != null && !minorParentIons.isEmpty()) {			
			List<String>mpList = minorParentIons.stream().
					map(p -> p.toString()).collect(Collectors.toList());
			msmsElement.setAttribute(
					TandemMassSpectrumFields.MinorParents.name(), 
					StringUtils.join(mpList, ";"));
		}
		if(averagedScanNumbers != null && !averagedScanNumbers.isEmpty()) {
			
			List<String>asList = averagedScanNumbers.entrySet().stream().
				map(e -> Integer.toString(e.getKey()) + "_" + Integer.toString(e.getValue())).
						collect(Collectors.toList());
			msmsElement.setAttribute(
					TandemMassSpectrumFields.AvgScans.name(), 
					StringUtils.join(asList, ";"));
		}
		msmsElement.setAttribute(TandemMassSpectrumFields.IsMinor.name(), 
				Boolean.toString(parentIonIsMinorIsotope));
		
		if(scanRtMap != null && !scanRtMap.isEmpty()) {
			
			List<String>asList = scanRtMap.entrySet().stream().
				map(e -> Integer.toString(e.getKey()) + "_" + Double.toString(e.getValue())).
						collect(Collectors.toList());
			msmsElement.setAttribute(
					TandemMassSpectrumFields.ScanRtMap.name(), 
					StringUtils.join(asList, ";"));
		}
		return msmsElement;
	}
	
	public TandemMassSpectrum(Element msmsElement) {		
		
		spectrum = new TreeSet<MsPoint>(MsUtils.mzSorter);
		annotations = new TreeSet<ObjectAnnotation>();
		minorParentIons = new TreeSet<MsPoint>(MsUtils.mzSorter);
		averagedScanNumbers = new TreeMap<Integer,Integer>();
		scanRtMap = new TreeMap<Integer,Double>();
		
		uniqueId = 
				msmsElement.getAttributeValue(TandemMassSpectrumFields.Id.name());
		String sourceString = 
				msmsElement.getAttributeValue(TandemMassSpectrumFields.Source.name());
		if(sourceString != null)
			spectrumSource = SpectrumSource.getOptionByName(sourceString);
		
		detectionAlgorithm = 
				msmsElement.getAttributeValue(TandemMassSpectrumFields.Algo.name());
		
		depth = Integer.parseInt(
				msmsElement.getAttributeValue(TandemMassSpectrumFields.Depth.name()));

		String parentString = 
				msmsElement.getAttributeValue(TandemMassSpectrumFields.Parent.name());
		if(parentString != null)
			parent = new MsPoint(parentString);
		
		String fragVString = 
				msmsElement.getAttributeValue(TandemMassSpectrumFields.FragV.name());
		if(fragVString != null)
			fragmenterVoltage = Double.parseDouble(fragVString);
		
		String cidString = 
				msmsElement.getAttributeValue(TandemMassSpectrumFields.CID.name());
		if(cidString != null)
			cidLevel = Double.parseDouble(cidString);

		ionisationType = 
				msmsElement.getAttributeValue(TandemMassSpectrumFields.Ioniz.name());

		String scanNumString = 
				msmsElement.getAttributeValue(TandemMassSpectrumFields.Scan.name());
		if(scanNumString != null)
			scanNumber = Integer.parseInt(scanNumString);
		
		String parentScanNumString = 
				msmsElement.getAttributeValue(TandemMassSpectrumFields.PScan.name());
		if(parentScanNumString != null)
			parentScanNumber = Integer.parseInt(parentScanNumString);
		
		String isolationWindowString = 
				msmsElement.getAttributeValue(TandemMassSpectrumFields.IsolWindow.name());
		if(isolationWindowString != null)
			isolationWindow = new Range(isolationWindowString);
		
		String polarityCode = 
				msmsElement.getAttributeValue(TandemMassSpectrumFields.Pol.name());
		if(polarityCode != null)
			polarity = Polarity.getPolarityByCode(polarityCode);
		
		description = 
				msmsElement.getAttributeValue(TandemMassSpectrumFields.Desc.name());
	
		String aotString = 
				msmsElement.getAttributeValue(TandemMassSpectrumFields.AOT.name());
		if(aotString != null)
			annotatedObjectType = AnnotatedObjectType.getOptionByName(aotString);
		
		String parentPurityString = 
				msmsElement.getAttributeValue(TandemMassSpectrumFields.ParentIonPurity.name());
		if(parentPurityString != null)
			parentIonPurity = Double.parseDouble(parentPurityString);
		
		String minorParentIonsString = 
				msmsElement.getAttributeValue(TandemMassSpectrumFields.MinorParents.name());
		if(minorParentIonsString != null) {
			String[]mpChunks = minorParentIonsString.split(";");
			for(String mpChunk : mpChunks) {
				String[]parts = mpChunk.split("_");
				minorParentIons.add(new MsPoint(
						Double.parseDouble(parts[0]), 
						Double.parseDouble(parts[1])));
			}
		}				
		String avgScanString = 
				msmsElement.getAttributeValue(TandemMassSpectrumFields.AvgScans.name());
		if(avgScanString != null) {
			String[]asChunks = avgScanString.split(";");
			for(String asChunk : asChunks) {
				String[]parts = asChunk.split("_");
				averagedScanNumbers.put(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
			}
		}		
		parentIonIsMinorIsotope = Boolean.parseBoolean(
				msmsElement.getAttributeValue(TandemMassSpectrumFields.IsMinor.name()));

		String scanRtMapString = 
				msmsElement.getAttributeValue(TandemMassSpectrumFields.ScanRtMap.name());
		if(scanRtMapString != null) {
			String[]srtChunks = scanRtMapString.split(";");
			for(String srtChunk : srtChunks) {
				String[]parts = srtChunk.split("_");
				scanRtMap.put(Integer.parseInt(parts[0]), Double.parseDouble(parts[1]));
			}
		}	
		double[] mzValues = null;
		double[] intensityValues = null;
		String mzText =  
				msmsElement.getChild(TandemMassSpectrumFields.MZ.name()).getContent().get(0).getValue();
		try {
			mzValues = NumberArrayUtils.decodeNumberArray(mzText);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String intensityText =  
				msmsElement.getChild(TandemMassSpectrumFields.Intensity.name()).getContent().get(0).getValue();
		try {
			intensityValues = NumberArrayUtils.decodeNumberArray(intensityText);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for(int i=0; i<mzValues.length; i++)
			spectrum.add(new MsPoint(mzValues[i], intensityValues[i]));
		
		setEntropy(MsUtils.calculateCleanedSpectrumEntropyNatLog(spectrum));
	}

	public Map<Integer, Double> getScanRtMap() {
		return scanRtMap;
	}
	
	public Collection<Double>getMSMSScanRtList(){
		
		Collection<Double>msmsScanRtList = new TreeSet<Double>();		
		averagedScanNumbers.keySet().stream().forEach(s -> msmsScanRtList.add(scanRtMap.get(s)));
		return msmsScanRtList;
	}
	
	public String getSpectrumAsPythonArray() {		
		return MsUtils.getSpectrumAsPythonArray(spectrum);		
	}

	public boolean getHasScans() {
		return hasScans;
	}

	public void setHasScans(boolean hasScans) {
		this.hasScans = hasScans;
	}
	
	public double getTopIntensityForMzRange(org.jfree.data.Range jfRange) {
		return getTopIntensityForMzRange(new Range(jfRange));
	}
	
	public double getTopIntensityForMzRange(Range mzRange) {
		
		if(spectrum == null || spectrum.isEmpty())
			return 0.0d;
		
		List<MsPoint> pointsInRange = spectrum.stream().
				filter(p -> mzRange.contains(p.getMz())).
				sorted(MsUtils.reverseIntensitySorter).				
				collect(Collectors.toList());
		if(!pointsInRange.isEmpty())
			return pointsInRange.get(0).getIntensity();
		
		return 0.0d;
	}
	
	public double getTopIntensity() {
		
		if(spectrum == null || spectrum.isEmpty())
			return 0.0d;
		
		return  spectrum.stream().
			sorted(MsUtils.reverseIntensitySorter).
			collect(Collectors.toList()).get(0).getIntensity();
	}
		
	public double getParentScanRetentionTime() {
		
		if(parentScanNumber > 0 && scanRtMap.containsKey(parentScanNumber))
			return scanRtMap.get(parentScanNumber);
		else if(!scanRtMap.isEmpty())					
			return scanRtMap.entrySet().iterator().next().getValue();
		else
			return 0.0d;
	}
	
	public double getRawParentIonIntensity() {
		
		if(parent == null) 
			return 0.001d;
		else
			return parent.getIntensity();
	}
	
	public double getNormalizedParentIonIntensity() {
		
		double raw = getRawParentIonIntensity();
		if(raw > 0.001d && !spectrum.isEmpty()) {
			double norm =  raw /  getTopIntensity() 
					* MsUtils.SPECTRUM_NORMALIZATION_BASE_INTENSITY;	
			if(norm > MsUtils.SPECTRUM_NORMALIZATION_BASE_INTENSITY)
				norm = MsUtils.SPECTRUM_NORMALIZATION_BASE_INTENSITY;
			
			return norm;
		}
		else
			return raw;			
	}
	
	public MsPoint getNormalisedParentIon() {
		
		if(parent == null)
			return null;
		else 
			return new MsPoint(parent.getMz(), getNormalizedParentIonIntensity());
	}
}


















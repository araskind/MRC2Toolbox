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
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;

import edu.umich.med.mrc2.datoolbox.data.enums.Polarity;
import edu.umich.med.mrc2.datoolbox.data.enums.SpectrumSource;
import edu.umich.med.mrc2.datoolbox.database.idt.MSMSLibraryUtils;
import edu.umich.med.mrc2.datoolbox.utils.MsUtils;

public class MsMsLibraryFeature implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 308245153962563662L;
	protected String uniqueId;
	protected String msmsLibraryIdentifier;
	protected MsPoint parent;
	protected Polarity polarity;
	protected IonizationType ionizationType;
	protected Collection<MsPoint>spectrum;
	protected Map<MsPoint,String>massAnnotations;
	protected SpectrumSource spectrumSource;
	protected CompoundIdentity compoundIdentity;
	protected Map<String,String>properties;
	protected String collisionEnergyValue;
	protected double spectrumEntropy;

	public MsMsLibraryFeature(String uniqueId) {

		super();
		this.uniqueId = uniqueId;
		spectrum = new TreeSet<MsPoint>(MsUtils.mzSorter);
		massAnnotations = new TreeMap<MsPoint,String>(MsUtils.mzSorter);
		properties = new TreeMap<String,String>();
	}

	public MsMsLibraryFeature(String uniqueId, Polarity polarity) {
		this(uniqueId);
		this.polarity = polarity;
	}

	/**
	 * @return the uniqueId
	 */
	public String getUniqueId() {
		return uniqueId;
	}

	/**
	 * @return the msmsLibraryIdentifier
	 */
	public String getMsmsLibraryIdentifier() {
		return msmsLibraryIdentifier;
	}

	/**
	 * @return the parent
	 */
	public MsPoint getParent() {
		return parent;
	}

	/**
	 * @return the spectrum
	 */
	public Collection<MsPoint> getSpectrum() {
		return spectrum;
	}

	/**
	 * @return the massAnnotations
	 */
	public Map<MsPoint, String> getMassAnnotations() {
		return massAnnotations;
	}

	/**
	 * @return the spectrumSource
	 */
	public SpectrumSource getSpectrumSource() {
		return spectrumSource;
	}

	/**
	 * @return the compoundIdentity
	 */
	public CompoundIdentity getCompoundIdentity() {
		return compoundIdentity;
	}

	/**
	 * @return the properties
	 */
	public Map<String, String> getProperties() {
		return properties;
	}
	
	public boolean hasLibraryAnnotations() {
		return properties.keySet().stream().
				filter(k -> k.startsWith(MSMSLibraryUtils.ANNOTATION_FIELD_NAME)).
				findFirst().isPresent();
	}

	public String getProperty(String propertyName) {
		return properties.get(propertyName);
	}

	public void addProperty(String propertyName, String propertyValue) {
		properties.put(propertyName, propertyValue);
	}

	/**
	 * @param msmsLibraryIdentifier the msmsLibraryIdentifier to set
	 */
	public void setMsmsLibraryIdentifier(String msmsLibraryIdentifier) {
		this.msmsLibraryIdentifier = msmsLibraryIdentifier;
	}

	/**
	 * @param spectrum the spectrum to set
	 */
	public void setSpectrum(Collection<MsPoint> newSpectrum) {
		spectrum.clear();
		spectrum.addAll(newSpectrum);
	}

	/**
	 * @param spectrumSource the spectrumSource to set
	 */
	public void setSpectrumSource(SpectrumSource spectrumSource) {
		this.spectrumSource = spectrumSource;
	}

	/**
	 * @param compoundIdentity the compoundIdentity to set
	 */
	public void setCompoundIdentity(CompoundIdentity compoundIdentity) {
		this.compoundIdentity = compoundIdentity;
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

	/**
	 * @return the ionizationType
	 */
	public IonizationType getIonizationType() {
		return ionizationType;
	}

	/**
	 * @param ionizationType the ionizationType to set
	 */
	public void setIonizationType(IonizationType ionizationType) {
		this.ionizationType = ionizationType;
	}

	/**
	 * @return the collisionEnergyValue
	 */
	public String getCollisionEnergyValue() {
		return collisionEnergyValue;
	}

	/**
	 * @param collisionEnergyValue the collisionEnergyValue to set
	 */
	public void setCollisionEnergyValue(String collisionEnergyValue) {
		this.collisionEnergyValue = collisionEnergyValue;
	}

	/**
	 * @param parent the parent to set
	 */
	public void setParent(MsPoint parent) {
		this.parent = parent;
	}

	public MsPoint[] getNormalizedMassSortedSpectrum() {

//		double maxIntensity = spectrum.stream().
//			sorted(new MsDataPointComparator(SortProperty.Intensity, SortDirection.DESC)).
//			findFirst().get().getIntensity();
//
//		return spectrum.stream().sorted(MsUtils.mzSorter).
//				map(p -> new MsPoint(p.getMz(), p.getIntensity()/maxIntensity * 100.0d)).
//				toArray(size -> new MsPoint[size]);
		
		return MsUtils.normalizeAndSortMsPattern(spectrum);
	}

	public String getUserFriendlyId() {

//		protected String uniqueId;
//		protected String msmsLibraryIdentifier;
//		protected MsPoint parent;
//		protected Polarity polarity;
//		protected IonizationType ionizationType;
//		protected Collection<MsPoint>spectrum;
//		protected Map<MsPoint,String>massAnnotations;
//		protected SpectrumSource spectrumSource;
//		protected CompoundIdentity compoundIdentity;
//		protected Map<String,String>properties;
//		protected String collisionEnergyValue;
		ArrayList<String>nameParts = new ArrayList<String>();
		
		if(compoundIdentity != null && compoundIdentity.getName() != null)
			nameParts.add(compoundIdentity.getName());

		return StringUtils.join(nameParts, ", ");
	}

	@Override
	public boolean equals(Object msmsMatch) {

        if (msmsMatch == this)
            return true;

		if(msmsMatch == null)
			return false;

        if (!MsMsLibraryFeature.class.isAssignableFrom(msmsMatch.getClass()))
            return false;

        MsMsLibraryFeature cid = (MsMsLibraryFeature)msmsMatch;

        if(this.uniqueId != cid.getUniqueId())
        	return false;

        return true;
	}
	
	public double getMaxRawIntensity() {
		if(spectrum.isEmpty())
			return 0.0d;
		else
			return spectrum.stream().
					sorted(MsUtils.reverseIntensitySorter).
					findFirst().get().getIntensity();		
	}
	
	public double getRawParentIonIntensity() {
		
		if(parent == null) 
			return 0.001d;
		else
			return parent.getIntensity();
	}
	
	public double getNormalizedParentIonIntensity() {
		
		double raw = getRawParentIonIntensity();
		if(raw > 0.001 && !spectrum.isEmpty())
			return raw /  getMaxRawIntensity() * 100.0d;	
		else
			return raw;			
	}

    @Override
    public int hashCode() {
        return 53 * 3 + uniqueId.hashCode();
    }

	public double getSpectrumEntropy() {
		return spectrumEntropy;
	}

	public void setSpectrumEntropy(double spectrumEntropy) {
		this.spectrumEntropy = spectrumEntropy;
	}
	
	public String getSpectrumAsPythonArray() {		
		return MsUtils.getSpectrumAsPythonArray(spectrum);		
	}
	

	public MsPoint getNormalisedParentIon() {
		
		if(parent == null)
			return null;
		else
			return new MsPoint(
					parent.getMz(), 
					getNormalizedParentIonIntensity());
	}
}

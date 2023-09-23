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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.jdom2.Element;

import edu.umich.med.mrc2.datoolbox.data.compare.AdductComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.MsDataPointComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortDirection;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.data.enums.Polarity;
import edu.umich.med.mrc2.datoolbox.data.enums.SpectrumSource;
import edu.umich.med.mrc2.datoolbox.main.AdductManager;
import edu.umich.med.mrc2.datoolbox.project.store.MassSpectrumFields;
import edu.umich.med.mrc2.datoolbox.project.store.TandemMassSpectrumFields;
import edu.umich.med.mrc2.datoolbox.utils.MsUtils;
import edu.umich.med.mrc2.datoolbox.utils.NumberArrayUtils;

public class MassSpectrum implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 8329755008254129461L;

	private TreeMap<Adduct, List<MsPoint>> adductMap;
	private TreeMap<String, List<MsPoint>> patternMap;
	private Set<MsPoint> msPoints;
	private Adduct primaryAdduct;
	private Set<TandemMassSpectrum>tandemSpectra;
	private String detectionAlgorithm;
	private double mcMillanCutoff;
	private double mcMillanCutoffPercentDelta;

	public MassSpectrum() {

		super();
		msPoints = new TreeSet<MsPoint>(MsUtils.mzSorter);
		adductMap = new TreeMap<Adduct, List<MsPoint>>(new AdductComparator(SortProperty.Name));
		primaryAdduct = null;
		tandemSpectra = new HashSet<TandemMassSpectrum>();
		patternMap = new TreeMap<String, List<MsPoint>>();
	}

	public void addDataPoint(double mz, double intensity, String adductType, double rt) {

		MsPoint dp = new MsPoint(mz, intensity, adductType, rt);
		msPoints.add(dp);
	}

	public void addDataPoints(Collection<MsPoint>points) {
		msPoints.addAll(points);
	}
	
	public void replaceDataPoints(Collection<MsPoint>points) {
		
		clearAdductMap();
		clearPatternMap();
		msPoints.clear();
		msPoints.addAll(points);
	}

	public void addDataPoint(double mz, double intensity, String adductType, int charge) {

		MsPoint dp = new MsPoint(mz, intensity, adductType, charge);
		msPoints.add(dp);
	}

	public void addSpectrumForAdduct(Adduct adduct, Collection<MsPoint>dataPoints){

		List<MsPoint>sorted = dataPoints.stream().
				distinct().
				sorted(MsUtils.mzSorter).
				collect(Collectors.toList());
		adductMap.put(adduct, sorted);
		msPoints.addAll(dataPoints);
		findPrimaryAdduct();
	}
	
	private void findPrimaryAdduct() {
		
		double maxIntensity = 0.0d;
		for (Entry<Adduct, List<MsPoint>> entry : adductMap.entrySet()) {

			if(entry.getValue() == null || entry.getValue().isEmpty())
				continue;

			MsPoint basePeak = 
					entry.getValue().stream().
					sorted(MsUtils.reverseIntensitySorter).
					findFirst().orElse(null);			

			if(basePeak != null && basePeak.getIntensity() > maxIntensity) {
				maxIntensity = basePeak.getIntensity();
				primaryAdduct = entry.getKey();
			}
		}
	}	
	
	public void silentlyAddSpectrumForAdduct(Adduct adduct, Collection<MsPoint>dataPoints){
		
		List<MsPoint>sorted = dataPoints.stream().
				distinct().
				sorted(MsUtils.mzSorter).
				collect(Collectors.toList());
		adductMap.put(adduct, sorted);
		findPrimaryAdduct();
	}

	public void addSpectrumForPattern(Collection<MsPoint>dataPoints){

		List<MsPoint>sorted = dataPoints.stream().
				distinct().
				sorted(MsUtils.mzSorter).
				collect(Collectors.toList());

		String key = DataPrefix.MS_PATTERN.getName() + UUID.randomUUID().toString().substring(0, 10);
		patternMap.put(key, sorted);
		msPoints.addAll(dataPoints);
	}

	public Set<String> finalizeCefImportSpectrum() {

		adductMap.clear();
		String adduct;
		double maxIntensity = 0.0d;
		double minMz = 100000000.0d;
		Set<String> unmatchedAdducts = new HashSet<String>();

		for (MsPoint dp : msPoints) {

			Adduct mod = null;
			if(dp.getAdductType() != null)
				mod = AdductManager.getAdductByCefNotation(dp.getAdductType().replaceAll("\\+[0-9]+$", ""));
			
			if (mod != null) {

				if (!adductMap.containsKey(mod))
					adductMap.put(mod, new ArrayList<MsPoint>());

				adductMap.get(mod).add(dp);
				if (dp.getIntensity() > maxIntensity) {

					maxIntensity = dp.getIntensity();
					primaryAdduct = mod;
				}
				if (dp.getMz() < minMz)
					minMz = dp.getMz();
			}
			else {
				if(dp.getAdductType() != null)
					unmatchedAdducts.add(dp.getAdductType());
			}
		}
		// Sort points for each adduct
		for (Entry<Adduct, List<MsPoint>> entry : adductMap.entrySet()) {

			List<MsPoint>sorted = entry.getValue().stream().
				sorted(new MsDataPointComparator(SortProperty.MZ, SortDirection.ASC)).
				collect(Collectors.toList());
			entry.getValue().clear();
			entry.getValue().addAll(sorted);
		}
		return unmatchedAdducts;
	}

	public void clearAdductMap() {

		adductMap.clear();
		primaryAdduct = null;
	}
	
	public void clearPatternMap() {
		patternMap.clear();
	}

	public Collection<BasicIsotopicPattern>getIsotopicGroups(){

		ArrayList<BasicIsotopicPattern>isoPatterns = new ArrayList<BasicIsotopicPattern>();

		if(!adductMap.isEmpty()){

			for (Entry<Adduct, List<MsPoint>> entry : adductMap.entrySet()) {

				BasicIsotopicPattern bip = new BasicIsotopicPattern(entry.getValue().get(0));
				entry.getValue().stream().skip(1).map(dp -> bip.addDataPoint(dp, Math.abs(entry.getKey().getCharge())));
				isoPatterns.add(bip);
			}
		}
		else{
			MsPoint[] pattern = getCompletePattern();
			isoPatterns.add(new BasicIsotopicPattern(pattern[0]));
			boolean added;

			for(int j=1; j<pattern.length; j++) {

				added = false;

				for(int i = 3; i>0; i--) {

					for(BasicIsotopicPattern bip : isoPatterns) {

						if(bip.addDataPoint(pattern[j], i)) {

							added = true;
							break;
						}
					}
				}
				if(!added)
					isoPatterns.add(new BasicIsotopicPattern(pattern[j]));
			}
		}
		return isoPatterns;
	}

	public Set<Adduct>getAdducts() {
		return adductMap.keySet();
	}

	public MsPoint getBasePeak() {

		MsPoint basePeak = null;

		if(!msPoints.isEmpty()) {

			return msPoints.stream().
					sorted(new MsDataPointComparator(SortProperty.Intensity, SortDirection.DESC)).
					toArray(size -> new MsPoint[size])[0];
		}
		return basePeak;
	}

	public double getBasePeakMz() {

		double bpMz = 0.0d;
		MsPoint basePeak = getBasePeak();

		if(basePeak != null)
			bpMz = basePeak.getMz();

		return bpMz;
	}

	public MsPoint[] getCompletePattern(boolean scale) {

		if(scale)
			return getCompleteNormalizedPattern();
		else
			return getCompletePattern();
	}

	public MsPoint[] getCompletePattern() {

		return msPoints.stream().
				sorted(MsUtils.mzSorter).
				toArray(size -> new MsPoint[size]);
	}

	public MsPoint[] getCompleteNormalizedPattern() {
		return MsUtils.normalizeAndSortMsPattern(msPoints);
	}

	public MsPoint getMonoisotopicPeak() {

		MsPoint miPeak = null;

		if(!msPoints.isEmpty()) {

			return msPoints.stream().
					sorted(MsUtils.mzSorter).
					toArray(size -> new MsPoint[size])[0];
		}
		return miPeak;
	}

	public double getMonoisotopicMz() {

		double miMz = 0.0d;
		MsPoint miPeak = getMonoisotopicPeak();

		if(miPeak != null)
			miMz = miPeak.getMz();

		return miMz;
	}

	public MsPoint[] getMsForAdduct(Adduct adduct) {

		if(adductMap.get(adduct) == null)
			return null;

		if(adductMap.get(adduct).isEmpty())
			return null;

		return adductMap.get(adduct).stream().
			sorted(MsUtils.mzSorter).
			toArray(size -> new MsPoint[size]);
	}

	public MsPoint[] getMsForAdduct(Adduct adduct, boolean scale) {

		if(!scale)
			return getMsForAdduct(adduct);

		if(adductMap.get(adduct) == null)
			return null;

		if(adductMap.get(adduct).isEmpty())
			return null;

		return MsUtils.normalizeAndSortMsPattern(adductMap.get(adduct));
	}
	
	public double getPrimaryAdductBasePeakMz() {
		
		if(primaryAdduct == null)
			return -1.0d;
		
		MsPoint[] ms = getMsForAdduct(primaryAdduct);
		if(ms == null)
			return -1.0d;
		else
			return ms[0].getMz();
	}

	public Adduct getPrimaryAdduct() {
		return primaryAdduct;
	}

	public int getAbsoluteChargeFromIsotopicPattern() {

		int absCharge = 0;
		MsPoint[] pattern = getCompletePattern();

		if(pattern.length > 1)
			absCharge = (int) Math.round(1.0d / (pattern[1].getMz() - pattern[0].getMz()));

		return absCharge;
	}

	public int getPrimaryAdductAbsoluteCharge() {
		return Math.abs(primaryAdduct.getCharge());
	}

	public boolean isEmpty(){
		return msPoints.isEmpty();
	}

	public void addTandemMs(TandemMassSpectrum tandemMs) {

		if(tandemMs.getPolarity() == null) {

			if(getPrimaryAdduct() != null) {

				if(getPrimaryAdduct().getCharge() < 0)
					tandemMs.setPolarity(Polarity.Negative);

				if(getPrimaryAdduct().getCharge() > 0)
					tandemMs.setPolarity(Polarity.Positive);
			}
		}
		tandemSpectra.add(tandemMs);
	}

	public boolean hasTandemMs(int depth) {

		boolean hasMs = false;

		for(TandemMassSpectrum tms : tandemSpectra) {

			if(tms.getDepth() == depth)
				return true;
		}
		return hasMs;
	}

	public Set<TandemMassSpectrum> getTandemSpectra() {
		return tandemSpectra;
	}

	public TandemMassSpectrum getTandemSpectrum(SpectrumSource source) {
		return tandemSpectra.stream().
				filter(t -> t.getSpectrumSource().equals(source)).
				findFirst().orElse(null);
	}
	
	public TandemMassSpectrum getReferenceTandemSpectrum() {
		return tandemSpectra.stream().
				filter(t -> !t.getSpectrumSource().equals(SpectrumSource.EXPERIMENTAL)).
				findFirst().orElse(null);
	}
	
	public TandemMassSpectrum getExperimentalTandemSpectrum() {
		return tandemSpectra.stream().
				filter(t -> t.getSpectrumSource().equals(SpectrumSource.EXPERIMENTAL)).
				findFirst().orElse(null);
	}

	public TreeMap<String, List<MsPoint>> getPatternMap() {
		return patternMap;
	}

	public List<MsPoint>getPattern(String patternKey){

		if(patternMap.containsKey(patternKey))
			return patternMap.get(patternKey);
		else
			return null;
	}

	public List<MsPoint> removePattern(String patternKey){

		if(patternMap.containsKey(patternKey))
			return patternMap.remove(patternKey);
		else
			return null;
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

	public double getMcMillanCutoff() {
		return mcMillanCutoff;
	}

	public void setMcMillanCutoff(double mcMillanCutoff) {
		this.mcMillanCutoff = mcMillanCutoff;
	}

	public double getMcMillanCutoffPercentDelta() {
		return mcMillanCutoffPercentDelta;
	}

	public void setMcMillanCutoffPercentDelta(double mcMillanCutoffPercentDelta) {
		this.mcMillanCutoffPercentDelta = mcMillanCutoffPercentDelta;
	}

	public Set<MsPoint> getMsPoints() {
		return msPoints;
	}
	
	public double getTotalArea() {
		return msPoints.stream().mapToDouble(p -> p.getIntensity()).sum();
	}

	public Element getXmlElement() {
		
		Element spectrumElement = new Element(
				MassSpectrumFields.Spectrum.name());
		if(msPoints != null && !msPoints.isEmpty()) {
			double[]mzValues = msPoints.stream().
					mapToDouble(p -> Math.floor(p.getMz() * 1000000) / 1000000).toArray();
			String mz = "";
			try {
				mz = NumberArrayUtils.encodeNumberArray(mzValues);
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Element mzElement = 
					new Element(MassSpectrumFields.MZ.name()).setText(mz);			
			spectrumElement.addContent(mzElement);
			double[]intensityValues = msPoints.stream().
					mapToDouble(p -> Math.floor(p.getIntensity() * 100) / 100).toArray();
			String intensity = "";
			try {
				intensity = NumberArrayUtils.encodeNumberArray(intensityValues);
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Element intensityElement = 
					new Element(MassSpectrumFields.Intensity.name()).setText(intensity);			
			spectrumElement.addContent(intensityElement);
		}
		if(detectionAlgorithm != null)
			spectrumElement.setAttribute(MassSpectrumFields.Algo.name(), detectionAlgorithm);
		
		if(adductMap != null && !adductMap.isEmpty()) {
			
			Element adductMapElement = 
					new Element(MassSpectrumFields.AdductMap.name());
			for(Entry<Adduct, List<MsPoint>>am : adductMap.entrySet()) {
				
				Element amEntryElement = 
						new Element(MassSpectrumFields.Adduct.name());
				amEntryElement.setAttribute(MassSpectrumFields.AName.name(), am.getKey().getId());
				List<String>mpList = am.getValue().stream().
						map(p -> p.toString()).collect(Collectors.toList());
				amEntryElement.setAttribute(
						MassSpectrumFields.ASpec.name(), StringUtils.join(mpList, ";"));
				adductMapElement.addContent(amEntryElement);
			}	
			spectrumElement.addContent(adductMapElement);
		}
		if(primaryAdduct != null)
			spectrumElement.setAttribute(
					MassSpectrumFields.PAdduct.name(), primaryAdduct.getId());
		
		if(tandemSpectra != null && !tandemSpectra.isEmpty()) {
			
			Element msmsListElement = 
					new Element(MassSpectrumFields.MsmsList.name());
			
			for(TandemMassSpectrum msms : tandemSpectra) 
				msmsListElement.addContent(msms.getXmlElement());
			
			spectrumElement.addContent(msmsListElement);
		}
		return spectrumElement;
	}
	
	public MassSpectrum(Element spectrumElement) {
		
		msPoints = new TreeSet<MsPoint>(MsUtils.mzSorter);
		adductMap = new TreeMap<Adduct, 
				List<MsPoint>>(new AdductComparator(SortProperty.Name));
		primaryAdduct = null;
		tandemSpectra = new HashSet<TandemMassSpectrum>();
		patternMap = new TreeMap<String, List<MsPoint>>();	
		detectionAlgorithm = 
				spectrumElement.getAttributeValue(MassSpectrumFields.Algo.name());

		String adductId = 
				spectrumElement.getAttributeValue(MassSpectrumFields.PAdduct.name());
		if(adductId != null)
			primaryAdduct = AdductManager.getAdductById(adductId);
		
		double[] mzValues = null;
		double[] intensityValues = null;
		String mzText =  
				spectrumElement.getChild(MassSpectrumFields.MZ.name()).getContent().get(0).getValue();
		try {
			mzValues = NumberArrayUtils.decodeNumberArray(mzText);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String intensityText =  
				spectrumElement.getChild(MassSpectrumFields.Intensity.name()).getContent().get(0).getValue();
		try {
			intensityValues = NumberArrayUtils.decodeNumberArray(intensityText);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for(int i=0; i<mzValues.length; i++)
			msPoints.add(new MsPoint(mzValues[i], intensityValues[i]));
		
		List<Element>msmsElementList = spectrumElement.getChildren(MassSpectrumFields.MsmsList.name());
		if(!msmsElementList.isEmpty()) {
			
			Element msmsListElement = spectrumElement.getChildren(MassSpectrumFields.MsmsList.name()).get(0);
			List<Element> msmsList = msmsListElement.getChildren(TandemMassSpectrumFields.MSMS.name());
			for(Element msmsElement : msmsList) {
				
				if(msmsElement.getName().equals(TandemMassSpectrumFields.MSMS.name()))
					tandemSpectra.add(new TandemMassSpectrum(msmsElement));
			}
		}
	}
}

















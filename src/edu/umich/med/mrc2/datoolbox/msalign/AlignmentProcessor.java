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

package edu.umich.med.mrc2.datoolbox.msalign;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.jdom2.DataConversionException;
import org.jdom2.Document;
import org.jdom2.Element;

import edu.umich.med.mrc2.datoolbox.data.Adduct;
import edu.umich.med.mrc2.datoolbox.data.MassSpectrum;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureSet;
import edu.umich.med.mrc2.datoolbox.data.MsPoint;
import edu.umich.med.mrc2.datoolbox.data.compare.MsFeatureComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortDirection;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.enums.AgilentCefFields;
import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.data.enums.Polarity;
import edu.umich.med.mrc2.datoolbox.main.AdductManager;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.utils.MsUtils;
import edu.umich.med.mrc2.datoolbox.utils.Range;
import edu.umich.med.mrc2.datoolbox.utils.XmlUtils;

public class AlignmentProcessor {

	private final MsFeatureComparator revIntensitySorter = 
			new MsFeatureComparator(SortProperty.Area, SortDirection.DESC);
	private final MsFeatureComparator rtSorter = 
			new MsFeatureComparator(SortProperty.RT);
	private final double minAnchorDistance = 0.01d;
	
	private File cefFileOne;
	private File cefFileTwo;
	private int numRtIntervals;
	private long numRefsPerInterval;
	private double massAccuracyPpm;
			
	private MsFeatureSet referenceList;
	private MsFeatureSet queryList;
	private Map<MsFeature,MsFeature>anchorMap;
		
	public AlignmentProcessor(
			File cefFileOne, 
			File cefFileTwo, 
			int numRtIntervals, 
			long numRefsPerInterval,
			double massAccuracyPpm) {
		super();
		this.cefFileOne = cefFileOne;
		this.cefFileTwo = cefFileTwo;
		this.numRtIntervals = numRtIntervals;
		this.numRefsPerInterval = numRefsPerInterval;
		this.massAccuracyPpm = massAccuracyPpm;
	}

	public Collection<String> runAlignment() {
		
//		final long startTime = System.currentTimeMillis();
		Collection<String>messages = new ArrayList<String>();
		List<MsFeature>featureListOne = null;
		List<MsFeature>featureListTwo = null;
		try {
			featureListOne = readInputCefFile(cefFileOne);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			featureListTwo = readInputCefFile(cefFileTwo);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
//		final long endTime = System.currentTimeMillis();
//		System.out.println("Total execution time: " + (endTime - startTime));
		
		if(featureListOne == null || featureListTwo == null 
				|| featureListOne.isEmpty() ||featureListTwo.isEmpty()) {
			messages.add("Bad data");
			return messages;
		}
		if(featureListOne.size() > featureListTwo.size()) {
			referenceList = new MsFeatureSet(cefFileOne.getName(), featureListOne);
			queryList = new MsFeatureSet(cefFileTwo.getName(), featureListTwo);
		}
		else {
			queryList = new MsFeatureSet(cefFileOne.getName(), featureListOne);
			referenceList = new MsFeatureSet(cefFileTwo.getName(), featureListTwo);
		}
		selectAlignmentAnchors(
				featureListOne, featureListTwo, 
				numRtIntervals, numRefsPerInterval, massAccuracyPpm);
		
		return messages;
	}
	
	private void selectAlignmentAnchors(
			List<MsFeature> featureListOne, 
			List<MsFeature> featureListTwo,
			int numRtIntervals,
			long numRefsPerInterval,
			double massAccuracyPpm) {
		Range sampleOneRtRange = getCompleteRtRange(featureListOne);
		double intervalOne = sampleOneRtRange.getSize() / (double)numRtIntervals;
		Range sampleTwoRtRange = getCompleteRtRange(featureListTwo);
		double intervalTwo = sampleTwoRtRange.getSize() / (double)numRtIntervals;
		Range[][]rangeMap = new Range[numRtIntervals][2];
		Map<MsFeature,MsFeature>tmpAnchorMap = new HashMap<MsFeature,MsFeature>();
		
		for(int i=0; i<numRtIntervals; i++) {
			
			rangeMap[i][0] = new Range(intervalOne * i, intervalOne * (i+1));
			List<MsFeature>tfOne = findTopFeatures(featureListOne, rangeMap[i][0], numRefsPerInterval);
			rangeMap[i][1] = new Range(intervalTwo * i, intervalTwo * (i+1));
			List<MsFeature>tfTwo = findTopFeatures(featureListTwo, rangeMap[i][1], numRefsPerInterval);
			
			for(MsFeature one : tfOne ) {
				
				Range mzRange = MsUtils.createPpmMassRange(one.getMonoisotopicMz(), massAccuracyPpm);
				List<MsFeature> matches = tfTwo.stream().
						filter(f -> f.getCharge() == one.getCharge()).
						filter(f -> mzRange.contains(f.getMonoisotopicMz())).
						collect(Collectors.toList());
				if(!matches.isEmpty())
					matches.stream().forEach(m -> tmpAnchorMap.put(one,m));
//				if(matches.size() == 1)
//					anchorMap.put(one, matches.get(0));							
			}						
		}
		anchorMap = cleanAnchorMap(tmpAnchorMap);
	}
	
	private Map<MsFeature,MsFeature> cleanAnchorMap(Map<MsFeature,MsFeature>tmpAnchorMap) {
		
		Map<MsFeature,MsFeature>cleanAnchorMap = new TreeMap<MsFeature,MsFeature>(rtSorter);
		int posCount = 0;
		int negCount = 0;
		for(Entry<MsFeature,MsFeature>anchor : tmpAnchorMap.entrySet()) {
			double rtDiff = anchor.getKey().getRetentionTime() - anchor.getValue().getRetentionTime();
			if(rtDiff > 0)
				posCount++;
			else
				negCount++;
		}
		double multiplier = 1.0d;
		if(negCount > posCount)
			multiplier = -1.0d;
		
		for(Entry<MsFeature,MsFeature>anchor : tmpAnchorMap.entrySet()) {
			
			Range newKeyRange = new Range(
					anchor.getKey().getRetentionTime() - minAnchorDistance, 
					anchor.getKey().getRetentionTime() + minAnchorDistance);
			
			MsFeature existingQuery = null;
			if(multiplier * (anchor.getKey().getRetentionTime() - anchor.getValue().getRetentionTime()) >= 0.0d) {
				
				existingQuery = cleanAnchorMap.get(anchor.getKey());
				if(existingQuery != null) { 
					
					if(existingQuery.getArea() < anchor.getValue().getArea())
						cleanAnchorMap.put(anchor.getKey(), anchor.getValue());
				}
				else {
					MsFeature featureInRange = cleanAnchorMap.keySet().stream().
							filter(f -> newKeyRange.contains(f.getRetentionTime())).
							findFirst().orElse(null);
					if(featureInRange == null)
						cleanAnchorMap.put(anchor.getKey(), anchor.getValue());
				}
			}
		}	
		return cleanAnchorMap;
	}

	private List<MsFeature>findTopFeatures(
			Collection<MsFeature>allFeatures, 
			Range rtRange, 
			long number){
		
		List<MsFeature>topFeatures = 
				allFeatures.stream().filter(f -> rtRange.contains(f.getRetentionTime())).
					sorted(revIntensitySorter).limit(number).sorted(rtSorter).
					collect(Collectors.toList());
		
		return topFeatures;
	}
	
	private Range getCompleteRtRange(Collection<MsFeature>featureList) {
		
		TreeSet<Double>rtSet = featureList.stream().map(MsFeature::getRetentionTime).
				collect(Collectors.toCollection(TreeSet::new));
		return new Range(rtSet.first(), rtSet.last());
	}

	private List<MsFeature>readInputCefFile(File inputCefFile) throws Exception {
		
		System.out.println("Reading data from " + inputCefFile.getName());
		List<MsFeature>fileFeatures = new ArrayList<MsFeature>();
		if(inputCefFile == null || !inputCefFile.exists())
			return fileFeatures;
				
		Document cefDocument = XmlUtils.readXmlFile(inputCefFile);
		if(cefDocument == null)
			return fileFeatures;
		
		List<Element>featureNodes = 
				cefDocument.getRootElement().getChild("CompoundList").getChildren("Compound");

		for (Element cpdElement : featureNodes) {

			MsFeature feature = null;
			try {
				feature = parseCefCompoundElement(cpdElement);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(feature != null)
				fileFeatures.add(feature);

		}
		fileFeatures.stream().forEach(MsFeature::setAreaFromMsOneSpectrum);		
		return fileFeatures;
	}

	private MsFeature parseCefCompoundElement(Element cpdElement) throws Exception {
		
		Element location = cpdElement.getChild("Location");
		double rt = location.getAttribute("rt").getDoubleValue();
		double neutralMass = 0.0d;
		if(location.getAttribute("m") != null)
			neutralMass = location.getAttribute("m").getDoubleValue();
				
		String name = "";
		Element resultsElement = cpdElement.getChild("Results");
		Element moleculeElement = null;
		if(resultsElement != null) {
			
			moleculeElement = resultsElement.getChild("Molecule");						
			if(moleculeElement != null){
				
				name = moleculeElement.getAttributeValue("name");

				// Work-around for old data
				if (name == null || name.isEmpty())
					name = moleculeElement.getAttributeValue("formula");
			}
			else{
				name = DataPrefix.MS_LIBRARY_UNKNOWN_TARGET.getName() +
					MRC2ToolBoxConfiguration.getMzFormat().format(neutralMass) + "_" + 
					MRC2ToolBoxConfiguration.getRtFormat().format(rt);
			}
		}
		else{
			name = DataPrefix.MS_LIBRARY_UNKNOWN_TARGET.getName() +
				MRC2ToolBoxConfiguration.getMzFormat().format(neutralMass) + "_" + 
				MRC2ToolBoxConfiguration.getRtFormat().format(rt);
		}
		MsFeature feature = new MsFeature(name, rt);
		//	In ProFinder export RT range is inside location element
		if(location.getAttribute("rts") != null && location.getAttribute("rte") != null) {
			double min = location.getAttribute("rts").getDoubleValue();
			double max = location.getAttribute("rte").getDoubleValue();
			if(min <= max) 
				feature.setRtRange(new Range(min, max));
		}
		feature.getIdentifications().clear();
		feature.setPrimaryIdentity(null);
		
		//	feature.setNeutralMass(neutralMass);
		if(location.getAttribute("a") != null)
			feature.setArea(location.getAttribute("a").getDoubleValue());

		if(location.getAttribute("y") != null)
			feature.setHeight(location.getAttribute("y").getDoubleValue());
		
		parseSpectra(cpdElement, feature);
		if(feature.getName().startsWith(DataPrefix.MS_LIBRARY_UNKNOWN_TARGET.getName())) {
			
			name = DataPrefix.MS_LIBRARY_UNKNOWN_TARGET.getName() +
					MRC2ToolBoxConfiguration.getMzFormat().format(feature.getMonoisotopicMz()) + "_" +
					MRC2ToolBoxConfiguration.getRtFormat().format(feature.getRetentionTime());
			if(feature.getSpectrum() != null && feature.getSpectrum().getPrimaryAdduct() != null)
				name += " " + feature.getSpectrum().getPrimaryAdduct().getName();
			
			feature.setName(name);
		}
		return feature;
	}
	
	private void parseSpectra(Element cpdElement,  MsFeature feature) throws DataConversionException {
		
		List<Element> spectrumElements = cpdElement.getChildren("Spectrum");	
		MassSpectrum spectrum = new MassSpectrum();
		
		Element forPeakWidthElement = null;
		for(Element spectrumElement : spectrumElements) {
			
			String spectrumType = spectrumElement.getAttributeValue("type");
			if(spectrumType.equals(AgilentCefFields.MS1_SPECTRUM.getName())) {
				forPeakWidthElement = spectrumElement;
				break;
			}
			if(spectrumType.equals(AgilentCefFields.MFE_SPECTRUM.getName())
					&& forPeakWidthElement == null) {
				forPeakWidthElement = spectrumElement;
			}
		}
		//	Add RT range
		if(forPeakWidthElement != null) {
			
			if(forPeakWidthElement.getChild("RTRanges") != null
					&& !forPeakWidthElement.getChild("RTRanges").getChildren().isEmpty()) {
				Element rtRangeElement = 
						forPeakWidthElement.getChild("RTRanges").getChild("RTRange");
				if(rtRangeElement != null) {
					
					double min = rtRangeElement.getAttribute("min").getDoubleValue();
					double max = rtRangeElement.getAttribute("max").getDoubleValue();
					if(min <= max) 
						feature.setRtRange(new Range(min, max));						
				}
			}
		}
		for(Element spectrumElement : spectrumElements) {

			String spectrumType = spectrumElement.getAttributeValue("type");
			
			if(spectrumType.equals(AgilentCefFields.MS1_SPECTRUM.getName())
					|| spectrumType.equals(AgilentCefFields.FBF_SPECTRUM.getName())
					|| spectrumType.equals(AgilentCefFields.MFE_SPECTRUM.getName())) {
				
				String sign = spectrumElement.getChild("MSDetails").getAttributeValue("p");
				Polarity pol = null;
				if(sign.equals("+"))
					pol = Polarity.Positive;

				if(sign.equals("-"))
					pol = Polarity.Negative;
				
				feature.setPolarity(pol);
				
				Map<Adduct,Collection<MsPoint>>adductMap = 
						parseMsOneSpectrumElement(spectrumElement);
				adductMap.entrySet().stream().
					forEach(e -> spectrum.addSpectrumForAdduct(e.getKey(), e.getValue()));
				
				String detectionAlgorithm = spectrumElement.getAttributeValue("cpdAlgo");
				if(detectionAlgorithm != null && !detectionAlgorithm.isEmpty())
					spectrum.setDetectionAlgorithm(detectionAlgorithm);
			}
		}
		feature.setSpectrum(spectrum);
	}
	
	private Map<Adduct,Collection<MsPoint>>parseMsOneSpectrumElement(Element spectrumElement) 
			throws DataConversionException{

		String sign = spectrumElement.getChild("MSDetails").getAttributeValue("p");
		Polarity pol = null;
		if(sign.equals("+"))
			pol = Polarity.Positive;
	
		if(sign.equals("-"))
			pol = Polarity.Negative;
	
		Map<Adduct,Collection<MsPoint>>cmMap = 
				new TreeMap<Adduct,Collection<MsPoint>>();
		List<Element> peaks;
		if(spectrumElement.getChild("MSPeaks") == null)	
			return cmMap;
		else
			peaks = spectrumElement.getChild("MSPeaks").getChildren("p");
		
		if(peaks.isEmpty())
			return cmMap;
		
		//	Check if no adducts are specified
		if(peaks.get(0).getAttribute("s") == null 
				|| peaks.get(0).getAttributeValue("s").isEmpty()) {
			
			Set<MsPoint> points = new TreeSet<MsPoint>(MsUtils.mzSorter);
			for(Element peak : peaks) {
				points.add(new MsPoint(
						peak.getAttribute("x").getDoubleValue(),
						peak.getAttribute("y").getDoubleValue()));
			}
			cmMap.put(AdductManager.getDefaultAdductForPolarity(pol), points);
			return cmMap;
		}
		Map<String,Collection<MsPoint>>adductMap = 
				new TreeMap<String,Collection<MsPoint>>();
		for(Element peak : peaks) {
						
			String adduct = peak.getAttributeValue("s").replaceAll("\\+[0-9]+$", "");
			adductMap.computeIfAbsent(adduct, v -> new TreeSet<MsPoint>(MsUtils.mzSorter));
//			if(!adductMap.containsKey(adduct))
//				adductMap.put(adduct, new TreeSet<MsPoint>(MsUtils.mzSorter));
	
			adductMap.get(adduct).add(new MsPoint(
					peak.getAttribute("x").getDoubleValue(),
					peak.getAttribute("y").getDoubleValue()));
		}
		for (Entry<String, Collection<MsPoint>> entry : adductMap.entrySet()) {
	
			Adduct adduct = AdductManager.getAdductByCefNotation(entry.getKey());
			if(adduct != null)
				cmMap.put(adduct, entry.getValue());
		}
		return cmMap;
	}

	public MsFeatureSet getReferenceList() {
		return referenceList;
	}

	public MsFeatureSet getQueryList() {
		return queryList;
	}

	public Map<MsFeature, MsFeature> getAnchorMap() {
		return anchorMap;
	}
}

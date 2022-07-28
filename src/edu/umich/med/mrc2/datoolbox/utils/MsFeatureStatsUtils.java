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

package edu.umich.med.mrc2.datoolbox.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import edu.umich.med.mrc2.datoolbox.data.MSFeatureInfoBundle;
import edu.umich.med.mrc2.datoolbox.data.MsPoint;
import edu.umich.med.mrc2.datoolbox.data.TandemMassSpectrum;
import edu.umich.med.mrc2.datoolbox.data.enums.FeatureSubsetByIdentification;
import edu.umich.med.mrc2.datoolbox.data.enums.IncludeSubset;
import edu.umich.med.mrc2.datoolbox.data.enums.MSMSMatchType;
import edu.umich.med.mrc2.datoolbox.gui.idworks.ms2.filter.MSMSFilterParameters;
import edu.umich.med.mrc2.datoolbox.gui.idworks.nist.pepsearch.HiResSearchOption;

public class MsFeatureStatsUtils {
	
	private static final DescriptiveStatistics descStats = new DescriptiveStatistics();

	public static double getMedianRtForFeatureCollection(Collection<MSFeatureInfoBundle>features) {
		
		if(features.isEmpty())
			return 0.0d;
		
		if(features.size() == 1)
			return features.iterator().next().getRetentionTime();
		
		descStats.clear();
		features.stream().forEach(b -> descStats.addValue(b.getRetentionTime()));
		return descStats.getPercentile(50.0d);
	}
	
	public static double getMedianParentIonMzForFeatureCollection(Collection<MSFeatureInfoBundle>features) {
		
		if(features.isEmpty())
			return 0.0d;
		
		if(features.size() == 1) {
			TandemMassSpectrum msms = features.iterator().next().getMsFeature().
					getSpectrum().getExperimentalTandemSpectrum();
			if(msms == null || msms.getParent() == null)
				return 0.0d;
			else
				return msms.getParent().getMz();
		}		
		descStats.clear();
		getMSMSFeaturesOnly(features).
			stream().map(b -> b.getMsFeature()).
			forEach(f -> descStats.addValue(f.getSpectrum().
					getExperimentalTandemSpectrum().getParent().getMz()));	
		return descStats.getPercentile(50.0d);
	}
	
	public static double getMedianMSMSAreaForFeatureCollection(Collection<MSFeatureInfoBundle>features) {
		
		if(features.isEmpty())
			return 0.0d;
		
		if(features.size() == 1) {
			TandemMassSpectrum msms = features.iterator().next().getMsFeature().
					getSpectrum().getExperimentalTandemSpectrum();
			if(msms == null || msms.getParent() == null)
				return 0.0d;
			else
				return msms.getParent().getMz();
		}		
		descStats.clear();		
		getMSMSFeaturesOnly(features).
			stream().map(b -> b.getMsFeature()).
			forEach(f -> descStats.addValue(f.getSpectrum().
					getExperimentalTandemSpectrum().getTotalIntensity()));	
		return descStats.getPercentile(50.0d);
	}
	
	public static Collection<MSFeatureInfoBundle>getMSMSFeaturesOnly(
			Collection<MSFeatureInfoBundle>inputFeatures){
	
		return inputFeatures.stream().
				filter(b -> Objects.nonNull(b.getMsFeature().getSpectrum())).
				filter(b -> Objects.nonNull(b.getMsFeature().
						getSpectrum().getExperimentalTandemSpectrum())).
				collect(Collectors.toList());
	}
	
	public static Collection<MSFeatureInfoBundle>filterFeaturesByMSMSFragmentMassDifferences(
			Collection<MSFeatureInfoBundle>inputFeatures,
			Collection<Range> massDiffRanges,
			IncludeSubset massDiffIncludeSubset,
			double massDiffIntensityCutoff,
			boolean neutralLossesOnly){
		
		Collection<MSFeatureInfoBundle> msmsFeatures = getMSMSFeaturesOnly(inputFeatures);
		Collection<MSFeatureInfoBundle>filtered = new ArrayList<MSFeatureInfoBundle>();
		Map<Range,Boolean>foundRanges = new HashMap<Range,Boolean>();
		for(MSFeatureInfoBundle b : msmsFeatures) {
			
			TandemMassSpectrum msms = 
					b.getMsFeature().getSpectrum().getExperimentalTandemSpectrum();
			Collection<Double>massDiffs = new ArrayList<Double>();
					
			if(neutralLossesOnly)
				massDiffs = MsUtils.getNeutralLosses(msms, massDiffIntensityCutoff);
			
			else
				massDiffs = 
				MsUtils.getMassDifferences(msms.getSpectrum(), massDiffIntensityCutoff);
			
			if(massDiffIncludeSubset.equals(IncludeSubset.Any)) {
				
				boolean mdFound = false;
				for(Range mzRange : massDiffRanges) {
					
					for(Double md : massDiffs) {
						
						if(mzRange.contains(md)) {
							
							filtered.add(b);
							mdFound = true;
							break;
						}
					}
					if(mdFound)
						break;
				}
			}
			else {
				foundRanges.clear();
				for(Range mzRange : massDiffRanges)
					foundRanges.put(mzRange, Boolean.FALSE);
				
				for(Range mzRange : massDiffRanges) {
					
					for(Double md : massDiffs) {
						
						if(mzRange.contains(md)) {							
							foundRanges.put(mzRange, Boolean.TRUE);
							break;
						}					
					}
				}
				if(!foundRanges.values().contains(Boolean.FALSE))
					filtered.add(b);
			}
		}
		return filtered;		
	}
	
	public static Collection<MSFeatureInfoBundle> filterMSMSFeatureTable(
			Collection<MSFeatureInfoBundle>inputFeatures, 
			MSMSFilterParameters filterParameters) {
		
		Collection<MSFeatureInfoBundle> features = getMSMSFeaturesOnly(inputFeatures);
		Collection<MSFeatureInfoBundle> msmsLibMatched = features.stream().
				filter(f -> Objects.nonNull(f.getMsFeature().getPrimaryIdentity())).
				filter(f -> Objects.nonNull(f.getMsFeature().
						getPrimaryIdentity().getReferenceMsMsLibraryMatch())).
				collect(Collectors.toList());
		
		if(filterParameters.getIdStatusSubset().equals(FeatureSubsetByIdentification.IDENTIFIED_ONLY)) {
			features = features.stream().
					filter(f -> Objects.nonNull(f.getMsFeature().getPrimaryIdentity())).
					collect(Collectors.toList());
			if(features.isEmpty())
				return features;
		}
		if(filterParameters.getIdStatusSubset().equals(FeatureSubsetByIdentification.UNKNOWN_ONLY)) {
			features = features.stream().
					filter(f -> Objects.isNull(f.getMsFeature().getPrimaryIdentity())).
					collect(Collectors.toList());
			if(features.isEmpty())
				return features;
		}
		Range mzRange = filterParameters.getMzRange();
		if(mzRange != null) {
			features = features.stream().
				filter(f -> Objects.nonNull(f.getMsFeature().getSpectrum().
						getExperimentalTandemSpectrum().getParent())).
				filter(f -> mzRange.contains(f.getMsFeature().getSpectrum().
						getExperimentalTandemSpectrum().getParent().getMz())).
				collect(Collectors.toList());
			if(features.isEmpty())
				return features;
		}
		Range rtRange = filterParameters.getRtRange();
		if(rtRange != null) {
			features = features.stream().
					filter(f -> rtRange.contains(f.getRetentionTime())).
					collect(Collectors.toList());			
			if(features.isEmpty())
				return features;
		}		
		if(filterParameters.getFeatureNameSubstring() != null) {
			String upName = filterParameters.getFeatureNameSubstring().toUpperCase();		
			if(filterParameters.isDoSearchAllIds()) {
				features = features.stream().
						filter(f -> Objects.nonNull(f.getMsFeature().getPrimaryIdentity())).
						filter(f -> f.getMsFeature().getIdentifications().stream().
								filter(i -> i.getName().toUpperCase().
										contains(upName)).findFirst().orElse(null) != null).
						collect(Collectors.toList());
			}
			else {
				features = features.stream().
						filter(f -> Objects.nonNull(f.getMsFeature().getPrimaryIdentity())).
						filter(f -> f.getMsFeature().getPrimaryIdentity().
								getName().toUpperCase().contains(upName)).
						collect(Collectors.toList());
			}
			if(features.isEmpty())
				return features;
		}
		Range precursorPurityRange = filterParameters.getPrecursorPurityRange();
		if(filterParameters.getPrecursorPurityRange() != null) {
			features = features.stream().
				filter(f -> precursorPurityRange.contains(
						f.getMsFeature().getSpectrum().
						getExperimentalTandemSpectrum().getParentIonPurity())).
					collect(Collectors.toList());			
			if(features.isEmpty())
				return features;
		}		
		//	Fragment masses
		Collection<Range> fragMzRanges = filterParameters.getFragmentMZRangeList();
		if(fragMzRanges != null && !fragMzRanges.isEmpty()) {
			
			features = filterFeaturesByMSMSFragments(
					features, fragMzRanges,
					filterParameters.getFragmentsIncludeSubset(),
					filterParameters.getFragmentIntensityCutoff());
			if(features.isEmpty())
				return features;
		}		
		//	Mass differences
		Collection<Range> massDiffRanges = filterParameters.getMassDifferencesRangeList();
		if(massDiffRanges != null && !massDiffRanges.isEmpty()) {
			
			features = MsFeatureStatsUtils.filterFeaturesByMSMSFragmentMassDifferences(
							features, 
							massDiffRanges,
							filterParameters.getMassDiffsIncludeSubset(),
							filterParameters.getMassDiffsIntensityCutoff(),
							filterParameters.isNeutralLossesOnly());
			if(features.isEmpty())
				return features;
		}		
		Range entropyRange = filterParameters.getEntropyRange();
		if(entropyRange != null) {
			features = features.stream().
					filter(f -> entropyRange.contains(f.getMsFeature().getSpectrum().
							getExperimentalTandemSpectrum().getEntropy())).
					collect(Collectors.toList());			
			if(features.isEmpty())
				return features;
		}
		Range peakAreaRange = filterParameters.getPeakAreaRange();
		if(peakAreaRange != null) {
			features = features.stream().
					filter(f -> peakAreaRange.contains(f.getMsFeature().getSpectrum().
							getExperimentalTandemSpectrum().getTotalIntensity())).
					collect(Collectors.toList());			
			if(features.isEmpty())
				return features;
		}
		double minimalMSMSScore = filterParameters.getMinimalMSMSScore();
		if(minimalMSMSScore > 0.0d) {
			
			features = features.stream().
					filter(f -> Objects.nonNull(f.getMsFeature().getPrimaryIdentity())).
					filter(f -> Objects.nonNull(f.getMsFeature().
							getPrimaryIdentity().getReferenceMsMsLibraryMatch())).
					filter(f -> f.getMsFeature().getPrimaryIdentity().
							getReferenceMsMsLibraryMatch().getScore() > minimalMSMSScore).
					collect(Collectors.toList());
			if(features.isEmpty())
				return features;
		}
		Collection<HiResSearchOption> msmsSearchTypes = filterParameters.getMsmsSearchTypes();
		if(!msmsSearchTypes.isEmpty()) {
			
			if(!msmsSearchTypes.contains(HiResSearchOption.z)) {
				
				features = msmsLibMatched.stream().
						filter(f -> !f.getMsFeature().getPrimaryIdentity().
								getReferenceMsMsLibraryMatch().getMatchType().equals(MSMSMatchType.Regular)).
						collect(Collectors.toList());
				if(features.isEmpty())
					return features;
			}
			if(!msmsSearchTypes.contains(HiResSearchOption.u)) {
				features = msmsLibMatched.stream().
						filter(f -> !f.getMsFeature().getPrimaryIdentity().
								getReferenceMsMsLibraryMatch().getMatchType().equals(MSMSMatchType.InSource)).
						collect(Collectors.toList());
				if(features.isEmpty())
					return features;
			}
			if(!msmsSearchTypes.contains(HiResSearchOption.y)) {
				features = msmsLibMatched.stream().
						filter(f -> !f.getMsFeature().getPrimaryIdentity().
								getReferenceMsMsLibraryMatch().getMatchType().equals(MSMSMatchType.Hybrid)).
						collect(Collectors.toList());
				if(features.isEmpty())
					return features;
			}
		}
		return features;
	}
	
	public static Collection<MSFeatureInfoBundle>filterFeaturesByMSMSFragments(
			Collection<MSFeatureInfoBundle>inputFeatures,
			Collection<Range> fragMzRanges,
			IncludeSubset fragmentsIncludeSubset,
			double fragIntensityCutoff){
		
		List<MSFeatureInfoBundle> msmsFeatures = inputFeatures.stream().
			filter(b -> Objects.nonNull(b.getMsFeature().getSpectrum())).
			filter(b -> Objects.nonNull(b.getMsFeature().getSpectrum().getExperimentalTandemSpectrum())).
			collect(Collectors.toList());
		Collection<MSFeatureInfoBundle>filtered = new ArrayList<MSFeatureInfoBundle>();
		Map<Range,Boolean>foundRanges = new HashMap<Range,Boolean>();
		for(MSFeatureInfoBundle b : msmsFeatures) {
			
			Collection<MsPoint> msms = 
					b.getMsFeature().getSpectrum().getExperimentalTandemSpectrum().getSpectrum();
			MsPoint[] msmsNorm = MsUtils.normalizeAndSortMsPattern(msms, 1.0d);
			if(fragmentsIncludeSubset.equals(IncludeSubset.Any)) {
				
				boolean fragFound = false;
				for(Range mzRange : fragMzRanges) {
					
					for(MsPoint p : msmsNorm) {
						
						if(mzRange.contains(p.getMz()) && p.getIntensity() > fragIntensityCutoff) {
							
							filtered.add(b);
							fragFound = true;
							break;
						}
					}
					if(fragFound)
						break;
				}
			}
			else {
				foundRanges.clear();
				for(Range mzRange : fragMzRanges)
					foundRanges.put(mzRange, Boolean.FALSE);
				
				for(Range mzRange : fragMzRanges) {
					
					for(MsPoint p : msmsNorm) {
						
						if(mzRange.contains(p.getMz()) && p.getIntensity() > fragIntensityCutoff) {							
							foundRanges.put(mzRange, Boolean.TRUE);
							break;
						}					
					}
				}
				if(!foundRanges.values().contains(Boolean.FALSE))
					filtered.add(b);
			}		
		}
		return filtered;	
	}
	
	public static Collection<MSFeatureInfoBundle>filterFeaturesByIdSubset(
			Collection<MSFeatureInfoBundle>inputFeatures,
			FeatureSubsetByIdentification idSubset) {
		
		if(idSubset.equals(FeatureSubsetByIdentification.ALL))
			return new ArrayList<MSFeatureInfoBundle>(inputFeatures);
			
		if(idSubset.equals(FeatureSubsetByIdentification.IDENTIFIED_ONLY))
			return inputFeatures.stream().
				filter(f -> Objects.nonNull(f.getMsFeature().getPrimaryIdentity())).
				collect(Collectors.toList());
		
		if(idSubset.equals(FeatureSubsetByIdentification.UNKNOWN_ONLY))
			return inputFeatures.stream().
				filter(f -> Objects.isNull(f.getMsFeature().getPrimaryIdentity())).
				collect(Collectors.toList());
		
		return null;
	}
	
	public static Collection<MSFeatureInfoBundle>getFeaturesWithMSMSLibMatch(
			Collection<MSFeatureInfoBundle>toFilter){
		
		return toFilter.stream().
				filter(f -> Objects.nonNull(f.getMsFeature().getPrimaryIdentity())).
				filter(f -> Objects.nonNull(f.getMsFeature().getPrimaryIdentity().
						getReferenceMsMsLibraryMatch())).
				collect(Collectors.toList());
	}
	
	public static Collection<MSFeatureInfoBundle>getFeaturesWithMSRTLibMatch(
			Collection<MSFeatureInfoBundle>toFilter){
		
		return toFilter.stream().
				filter(f -> Objects.nonNull(f.getMsFeature().getPrimaryIdentity())).
				filter(f -> Objects.nonNull(f.getMsFeature().
						getPrimaryIdentity().getMsRtLibraryMatch())).
				collect(Collectors.toList());
	}
}




















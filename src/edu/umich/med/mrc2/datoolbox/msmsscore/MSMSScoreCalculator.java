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

package edu.umich.med.mrc2.datoolbox.msmsscore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

import edu.umich.med.mrc2.datoolbox.data.MsPoint;
import edu.umich.med.mrc2.datoolbox.data.NISTPepSearchParameterObject;
import edu.umich.med.mrc2.datoolbox.data.ReferenceMsMsLibraryMatch;
import edu.umich.med.mrc2.datoolbox.data.SpectumPair;
import edu.umich.med.mrc2.datoolbox.data.TandemMassSpectrum;
import edu.umich.med.mrc2.datoolbox.data.compare.MsDataPointComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortDirection;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.enums.MassErrorType;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCash;
import edu.umich.med.mrc2.datoolbox.utils.MsUtils;
import edu.umich.med.mrc2.datoolbox.utils.Range;

public class MSMSScoreCalculator {

	public static final MsDataPointComparator mzSorter = 
			new MsDataPointComparator(SortProperty.MZ);
	public static final MsDataPointComparator reverseIntensitySorter = 
			new MsDataPointComparator(SortProperty.Intensity, SortDirection.DESC);
	
	public static final double DEFAULT_MS_REL_INT_NISE_CUTOFF = 0.01d;
	
	public static MsPoint[] createMassBankWeigtedPattern(Collection<MsPoint>spectrum) {	
		return createWeigtedPattern(spectrum, MSMSWeigtingType.MASS_BANK);
	}
	
	public static MsPoint[] createWeigtedPattern(
			Collection<MsPoint>spectrum, MSMSWeigtingType wType) {	
		return createWeigtedPattern(
				spectrum, wType.getIntensityPower(), wType.getMzPower());
	}
	
	public static MsPoint[] createWeigtedPattern(
			Collection<MsPoint>spectrum, double intensityPower, double mzPower) {
		
		if(spectrum == null || spectrum.isEmpty())
			return null;
		
		MsPoint[]weigtedPattern = new MsPoint[spectrum.size()];
		int count = 0;
		for(MsPoint p :spectrum) {
			double weightedIntensity = 
					Math.pow(p.getIntensity(), intensityPower) *
					Math.pow(p.getMz(), mzPower);
			weigtedPattern[count] = new MsPoint(p.getMz(), weightedIntensity);
			count++;
		}
		Arrays.sort(weigtedPattern, mzSorter);
		return weigtedPattern;
	}
	
	public static SpectumPair getCommonPeaks(
			Collection<MsPoint> unknownSpectrum, 
			Collection<MsPoint> librarySpectrum,
			double mzWindowValue) {
		
		return  getCommonPeaks(
				unknownSpectrum, librarySpectrum, mzWindowValue, MassErrorType.ppm);
	}
	
	public static SpectumPair getCommonPeaks(
			Collection<MsPoint> unknownSpectrum, 
			Collection<MsPoint> librarySpectrum,
			double mzWindowValue, 
			MassErrorType massErrorType) {
		
		Collection<MsPoint> commonUnknownSpectrum = new ArrayList<MsPoint>(); 
		Collection<MsPoint> commonLibrarySpectrum = new ArrayList<MsPoint>();
		for(MsPoint u : unknownSpectrum) {
			Range mzRange = MsUtils.createMassRange(
					u.getMz(), mzWindowValue, massErrorType);
			List<MsPoint>matchingPoints = librarySpectrum.stream().
				filter(p -> mzRange.contains(p.getMz())).
				sorted(reverseIntensitySorter).
				collect(Collectors.toList());
			if(!matchingPoints.isEmpty()) {				
				commonUnknownSpectrum.add(u);
				commonLibrarySpectrum.add(matchingPoints.get(0));
			}
		}	
		return new SpectumPair(commonUnknownSpectrum, commonLibrarySpectrum);
	}
		
	public static double calculateMassBankMatchScore(			
			Collection<MsPoint> unknownSpectrum, 
			Collection<MsPoint> librarySpectrum,
			double mzWindowValue, 
			MassErrorType massErrorType) {
		
		return calculateMatchScore(			
				unknownSpectrum, 
				librarySpectrum,
				mzWindowValue, 
				massErrorType,
				MSMSWeigtingType.MASS_BANK);
	}
	
	public static double calculateMatchScore(			
			Collection<MsPoint> unknownSpectrum, 
			Collection<MsPoint> librarySpectrum,
			double mzWindowValue, 
			MassErrorType massErrorType,
			MSMSWeigtingType wType) {
		
		SpectumPair commonPeaks = getCommonPeaks(
						unknownSpectrum, 
						librarySpectrum,
						mzWindowValue, 
						massErrorType);
		
		if(commonPeaks.getLibrarySpectrum().isEmpty() 
				|| commonPeaks.getUnknownSpectrum().isEmpty())
			return 0.0d;
		
		MsPoint[]weigthedUnknownSpectrum = 
				createWeigtedPattern(unknownSpectrum, wType);
		MsPoint[]weigthedLibrarySpectrum = 
				createWeigtedPattern(librarySpectrum, wType);
		
		double libUnkSum = 0.0d;
		double libSum = 0.0d;
		double unkSum = 0.0d;
		
		for(int i=0; i<weigthedUnknownSpectrum.length; i++) {
			libUnkSum += weigthedUnknownSpectrum[i].getIntensity() * 
					weigthedLibrarySpectrum[i].getIntensity();
			libSum += weigthedLibrarySpectrum[i].getIntensity() * 
					weigthedLibrarySpectrum[i].getIntensity();
			unkSum += weigthedUnknownSpectrum[i].getIntensity() * 
					weigthedUnknownSpectrum[i].getIntensity();
		}
		double cosineCorrelation =  (libUnkSum * libUnkSum)/(libSum * unkSum);		
		double factorRsum = 0.0d;
		for(int i=1; i<weigthedUnknownSpectrum.length; i++) {
			double fiSum =  (weigthedLibrarySpectrum[i].getIntensity() / weigthedLibrarySpectrum[i-1].getIntensity()) * 
					(weigthedUnknownSpectrum[i-1].getIntensity() / weigthedUnknownSpectrum[i].getIntensity());			
			if(fiSum <= 1)
				factorRsum += fiSum;
			else
				factorRsum += 1/fiSum;
		}
		double factorR = factorRsum / weigthedUnknownSpectrum.length;
		double score = (unknownSpectrum.size() * cosineCorrelation + weigthedUnknownSpectrum.length * factorR) / 
				(unknownSpectrum.size() + weigthedUnknownSpectrum.length);
		
		return score;
	}
	
	public static SpectumPair alignSpectra(			
			Collection<MsPoint> unknownSpectrum, 
			Collection<MsPoint> librarySpectrum,
			double mzWindowValue, 
			MassErrorType massErrorType,
			MatchDirection direction) {
		
		unknownSpectrum = 
				MsUtils.averageMassSpectrum(unknownSpectrum, mzWindowValue, massErrorType);
		librarySpectrum = 
				MsUtils.averageMassSpectrum(librarySpectrum, mzWindowValue, massErrorType);
		
		Collection<MsPoint> alignedUnknownSpectrum = new ArrayList<MsPoint>(); 
		Collection<MsPoint> alignedLibrarySpectrum = new ArrayList<MsPoint>();
		
		if(direction.equals(MatchDirection.Forward)) {
			
			alignedLibrarySpectrum.addAll(librarySpectrum);
			for(MsPoint u : unknownSpectrum) {
				
				Range mzRange = MsUtils.createMassRange(u.getMz(), mzWindowValue, massErrorType);
				List<MsPoint>matchingPoints = librarySpectrum.stream().
					filter(p -> mzRange.contains(p.getMz())).
					sorted(reverseIntensitySorter).
					collect(Collectors.toList());
				if(!matchingPoints.isEmpty())				
					alignedUnknownSpectrum.add(matchingPoints.get(0));
				else
					alignedUnknownSpectrum.add(new MsPoint(u.getMz(), 0.0d));
			}
		}
		if(direction.equals(MatchDirection.Reverse)) {
			
			alignedUnknownSpectrum.addAll(unknownSpectrum);
			for(MsPoint u : librarySpectrum) {
				
				Range mzRange = MsUtils.createMassRange(u.getMz(), mzWindowValue, massErrorType);
				List<MsPoint>matchingPoints = unknownSpectrum.stream().
					filter(p -> mzRange.contains(p.getMz())).
					sorted(reverseIntensitySorter).
					collect(Collectors.toList());
				if(!matchingPoints.isEmpty())				
					alignedLibrarySpectrum.add(matchingPoints.get(0));
				else
					alignedLibrarySpectrum.add(new MsPoint(u.getMz(), 0.0d));
			}
		}		
		return new SpectumPair(alignedUnknownSpectrum, alignedLibrarySpectrum);
	}
	
	public static MsPoint[] createEntropyWeigtedPattern(Collection<MsPoint>spectrum) {
		
		if(spectrum == null || spectrum.isEmpty())
			return null;
		
		double entropy = MsUtils.calculateSpectrumEntropyNatLog(spectrum);		
		MsPoint[]weigtedPattern = new MsPoint[spectrum.size()];
		int count = 0;
		for(MsPoint p :spectrum) {
			double weightedIntensity = p.getIntensity();
			if(entropy < 3.0d)
				weightedIntensity = Math.pow(p.getIntensity(), (0.25d + entropy / 4.0d));

			weigtedPattern[count] = new MsPoint(p.getMz(), weightedIntensity);
			count++;
		}
		Arrays.sort(weigtedPattern, mzSorter);
		return weigtedPattern;
	}
	
	public static MsPoint[] normalizeToUnitSum(Collection<MsPoint>spectrum) {
		
		MsPoint[]unitNormPattern = new MsPoint[spectrum.size()];
		double totalIntensity = 
				spectrum.stream().mapToDouble(p -> p.getIntensity()).sum();
		int count = 0;
		for(MsPoint p : spectrum) {
			unitNormPattern[count] = new MsPoint(p.getMz(), p.getIntensity() / totalIntensity);
			count++;
		}
		Arrays.sort(unitNormPattern, mzSorter);
		return unitNormPattern;
	}
	
	public static double calculateEntropyBasedMatchScore(			
			Collection<MsPoint> unknownSpectrum, 
			Collection<MsPoint> librarySpectrum,
			double mzWindowValue, 
			MassErrorType massErrorType) {
		
		Collection<MsPoint>unkAvgSpectrum = 
				MsUtils.averageMassSpectrumAndRemoveNoise(
						unknownSpectrum, mzWindowValue, massErrorType, DEFAULT_MS_REL_INT_NISE_CUTOFF);
		Collection<MsPoint>libAvgSpectrum = 
				MsUtils.averageMassSpectrumAndRemoveNoise(
						librarySpectrum, mzWindowValue, massErrorType, DEFAULT_MS_REL_INT_NISE_CUTOFF);
		
		MsPoint[]unknownSpectrumWeighted = createEntropyWeigtedPattern(unkAvgSpectrum);
		MsPoint[]librarySpectrumWeighted = createEntropyWeigtedPattern(libAvgSpectrum);
		
		Collection<MsPoint>allPoints = new TreeSet<MsPoint>(MsUtils.mzSorter);
		allPoints.addAll(Arrays.asList(unknownSpectrumWeighted));
		allPoints.addAll(Arrays.asList(librarySpectrumWeighted));
		Collection<MsPoint>avgSpectrum = 
				MsUtils.averageMassSpectrum(allPoints, mzWindowValue, massErrorType);	
		
		//MsPoint[]avgSpectrumWeighted = createEntropyWeigtedPattern(avgSpectrum);
		MsPoint[]avgSpectrumWeighted = normalizeToUnitSum(avgSpectrum);
		
		double unknownEntropy = 
				MsUtils.calculateSpectrumEntropyNatLog(unknownSpectrumWeighted);
		double libraryEntropy = 
				MsUtils.calculateSpectrumEntropyNatLog(librarySpectrumWeighted);
		double avgEntropy = 
				MsUtils.calculateSpectrumEntropyNatLog(avgSpectrumWeighted);
		
		double score = 
				1 - ((2.0d * avgEntropy - unknownEntropy - libraryEntropy) / Math.log(4.0d));
		
		return score;
	}
	
	//	This is a stop-gap to calculate entropy score for 
	//	METLIN hits that don't have mass error specified
	public static double calculateEntropyMatchScore(
			TandemMassSpectrum msms, 
			ReferenceMsMsLibraryMatch match) {

		NISTPepSearchParameterObject params = 
				IDTDataCash.getNISTPepSearchParameterObjectById(match.getSearchParameterSetId());
		if(params == null) {
			return calculateEntropyBasedMatchScore(			
					msms.getSpectrum(), 
					match.getMatchedLibraryFeature().getSpectrum(),
					20, 
					MassErrorType.ppm);
		}
		else
			return calculateEntropyBasedMatchScore(			
				msms.getSpectrum(), 
				match.getMatchedLibraryFeature().getSpectrum(),
				params.getFragmentMzErrorValue(), 
				params.getFragmentMzErrorType());
	}
}
















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
import edu.umich.med.mrc2.datoolbox.data.MsPointBucket;
import edu.umich.med.mrc2.datoolbox.data.ReferenceMsMsLibraryMatch;
import edu.umich.med.mrc2.datoolbox.data.SpectumPair;
import edu.umich.med.mrc2.datoolbox.data.TandemMassSpectrum;
import edu.umich.med.mrc2.datoolbox.data.compare.MsDataPointComparator;
import edu.umich.med.mrc2.datoolbox.data.enums.MSMSSearchDirection;
import edu.umich.med.mrc2.datoolbox.data.enums.MassErrorType;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.utils.MsUtils;
import edu.umich.med.mrc2.datoolbox.utils.Range;

public class MSMSScoreCalculator {

	public static final MsDataPointComparator mzSorter = MsUtils.mzSorter;
	public static final MsDataPointComparator reverseIntensitySorter = 
				MsUtils.reverseIntensitySorter;
	
	public static final double DEFAULT_MS_REL_INT_NOISE_CUTOFF = 0.01d;
	
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
			MSMSSearchDirection direction) {
		
		unknownSpectrum = 
				MsUtils.averageMassSpectrum(unknownSpectrum, mzWindowValue, massErrorType);
		librarySpectrum = 
				MsUtils.averageMassSpectrum(librarySpectrum, mzWindowValue, massErrorType);
		
		Collection<MsPoint> alignedUnknownSpectrum = new ArrayList<MsPoint>(); 
		Collection<MsPoint> alignedLibrarySpectrum = new ArrayList<MsPoint>();
		
		if(direction.equals(MSMSSearchDirection.DIRECT)) {
			
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
		if(direction.equals(MSMSSearchDirection.REVERSE)) {
			
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
	
	public static double[] createEntropyWeigtedPattern(double[]spectrum) {
		
		if(spectrum == null || spectrum.length == 0)
			return new double[] {0};
		
		double entropy = calculateEntropyNatLog(spectrum);		
		double[]weigtedPattern = new double[spectrum.length];
		int count = 0;
		for(double p :spectrum) {
			double weightedIntensity = p;
			if(entropy < 3.0d)
				weightedIntensity = Math.pow(p, (0.25d + entropy / 4.0d));

			weigtedPattern[count] = weightedIntensity;
			count++;
		}
		// Arrays.sort(weigtedPattern);
		return normalizeToUnitSum(weigtedPattern);
	}
	
	public static double calculateEntropyNatLog(double[]spectrum) {
		
		double totalIntensity = Arrays.stream(spectrum).sum();
		double entropy = 0.0d;
		for(double p : spectrum) {
			
			if(p > 0.0d) {
				double norm = p / totalIntensity;
				entropy += norm * Math.log(norm);
			}
		}	
		return -entropy;
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
		return normalizeToUnitSum(weigtedPattern);
	}
	
	public static MsPoint[] normalizeToUnitSum(MsPoint[]spectrum) {
		return normalizeToUnitSum(Arrays.asList(spectrum));
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
	
	public static double[] normalizeToUnitSum(double[]spectrum) {
		
		double[]unitNormPattern = new double[spectrum.length];
		double totalIntensity = Arrays.stream(spectrum).sum();
		int count = 0;
		for(double p : spectrum) {
			unitNormPattern[count] = p / totalIntensity;
			count++;
		}
		//	Arrays.sort(unitNormPattern);
		return unitNormPattern;
	}
	
	public static Collection<MsPoint> normalizeToUnitSumCollection(Collection<MsPoint>spectrum) {
		
		MsPoint[]unitNormPattern = new MsPoint[spectrum.size()];
		double totalIntensity = 
				spectrum.stream().mapToDouble(p -> p.getIntensity()).sum();
		int count = 0;
		for(MsPoint p : spectrum) {
			unitNormPattern[count] = new MsPoint(p.getMz(), p.getIntensity() / totalIntensity);
			count++;
		}
		Arrays.sort(unitNormPattern, mzSorter);
		return Arrays.asList(unitNormPattern);
	}
	
	public static MsPoint[] cleanAndNormalizeSpectrum(			
			Collection<MsPoint> spectrum, 
			double mzWindowValue, 
			MassErrorType massErrorType,
			double noiseCutoff) {
		
		Collection<MsPoint>avgSpectrum = 
				MsUtils.averageAndDenoiseMassSpectrum(
						spectrum, mzWindowValue, massErrorType, noiseCutoff);

		return normalizeToUnitSum(avgSpectrum);
	}
	
	public static double calculateEntropyBasedMatchScore(			
			Collection<MsPoint> unknownSpectrum, 
			Collection<MsPoint> librarySpectrum,
			double mzWindowValue, 
			MassErrorType massErrorType,
			double noiseCutoff) {
		
		MsPoint[]unknownSpectrumUnorm = 
				cleanAndNormalizeSpectrum(
						unknownSpectrum, mzWindowValue, massErrorType, noiseCutoff);
		MsPoint[]librarySpectrumUnorm = 
				cleanAndNormalizeSpectrum(
						librarySpectrum, mzWindowValue, massErrorType, noiseCutoff);
		
		Collection<MsPoint>allPoints = new TreeSet<MsPoint>(MsUtils.mzSorter);
		allPoints.addAll(Arrays.asList(unknownSpectrumUnorm));
		allPoints.addAll(Arrays.asList(librarySpectrumUnorm));		
		MsPoint[] points = allPoints.stream().
				sorted(mzSorter).
				toArray(size -> new MsPoint[size]);
		ArrayList<MsPointBucket> msBins = new ArrayList<MsPointBucket>();
		MsPointBucket first = new MsPointBucket(points[0], mzWindowValue, massErrorType);
		msBins.add(first);
		for(int i=1; i<points.length; i++) {
			
			MsPointBucket current = msBins.get(msBins.size()-1);
			if(!current.addPoint(points[i]))
				msBins.add(new MsPointBucket(points[i], mzWindowValue, massErrorType));
		}
		double[][]matchedIntensities = new double[3][msBins.size()];
		for(int i=0; i<msBins.size(); i++) {
			
			MsPointBucket bin = msBins.get(i);
			for(int j=0; j<unknownSpectrumUnorm.length; j++) {
				if(bin.pointBelongs(unknownSpectrumUnorm[j]))
					matchedIntensities[0][i] = unknownSpectrumUnorm[j].getIntensity();
			}
			for(int j=0; j<librarySpectrumUnorm.length; j++) {
				if(bin.pointBelongs(librarySpectrumUnorm[j]))
					matchedIntensities[1][i] = librarySpectrumUnorm[j].getIntensity();
			}
		}	
		double[]unknownSpectrumWeighted = createEntropyWeigtedPattern(matchedIntensities[0]);
		double[]librarySpectrumWeighted = createEntropyWeigtedPattern(matchedIntensities[1]);
		for(int i=0; i<msBins.size(); i++)
			matchedIntensities[2][i] = unknownSpectrumWeighted[i] + librarySpectrumWeighted[i]; 
		
		double score = 
				1 - ((2.0d * calculateEntropyNatLog(matchedIntensities[2]) 
						- calculateEntropyNatLog(unknownSpectrumWeighted) 
						- calculateEntropyNatLog(librarySpectrumWeighted)) / Math.log(4.0d));
		return score;
	}
	
	public static double calculateEntropyBasedMatchScoreForPreparedSpectra(			
			MsPoint[]unknownSpectrumUnorm, 
			MsPoint[]librarySpectrumUnorm,
			double mzWindowValue, 
			MassErrorType massErrorType) {
		
		MsPoint[] points = Arrays.copyOf(
				unknownSpectrumUnorm, 
				unknownSpectrumUnorm.length + librarySpectrumUnorm.length);
	    System.arraycopy(librarySpectrumUnorm, 0, points, 
	    		unknownSpectrumUnorm.length, librarySpectrumUnorm.length);
	    Arrays.sort(points, mzSorter);
		
//		Collection<MsPoint>allPoints = new TreeSet<MsPoint>(MsUtils.mzSorter);
//		allPoints.addAll(Arrays.asList(unknownSpectrumUnorm));
//		allPoints.addAll(Arrays.asList(librarySpectrumUnorm));		
//		MsPoint[] points = allPoints.stream().
//				sorted(mzSorter).
//				toArray(size -> new MsPoint[size]);
		ArrayList<MsPointBucket> msBins = new ArrayList<MsPointBucket>();
		MsPointBucket first = new MsPointBucket(points[0], mzWindowValue, massErrorType);
		msBins.add(first);
		for(int i=1; i<points.length; i++) {
			
			MsPointBucket current = msBins.get(msBins.size()-1);
			if(current.pointBelongs(points[i]))
				current.addPoint(points[i]);
			else
				msBins.add(new MsPointBucket(points[i], mzWindowValue, massErrorType));
		}
		double[][]matchedIntensities = new double[3][msBins.size()];
		for(int i=0; i<msBins.size(); i++) {
			
			MsPointBucket bin = msBins.get(i);
			for(int j=0; j<unknownSpectrumUnorm.length; j++) {
				if(bin.pointBelongs(unknownSpectrumUnorm[j]))
					matchedIntensities[0][i] = unknownSpectrumUnorm[j].getIntensity();
			}
			for(int j=0; j<librarySpectrumUnorm.length; j++) {
				if(bin.pointBelongs(librarySpectrumUnorm[j]))
					matchedIntensities[1][i] = librarySpectrumUnorm[j].getIntensity();
			}
		}	
		double[]unknownSpectrumWeighted = createEntropyWeigtedPattern(matchedIntensities[0]);
		double[]librarySpectrumWeighted = createEntropyWeigtedPattern(matchedIntensities[1]);
		for(int i=0; i<msBins.size(); i++)
			matchedIntensities[2][i] = unknownSpectrumWeighted[i] + librarySpectrumWeighted[i]; 
		
		double score = 
				1 - ((2.0d * calculateEntropyNatLog(matchedIntensities[2]) 
						- calculateEntropyNatLog(unknownSpectrumWeighted) 
						- calculateEntropyNatLog(librarySpectrumWeighted)) / Math.log(4.0d));
		return score;
	}

	//	This is a stop-gap to calculate entropy score for 
	//	METLIN hits that don't have mass error specified
//	public static double calculateEntropyMatchScore(
//			TandemMassSpectrum msms, 
//			ReferenceMsMsLibraryMatch match) {
//
//		NISTPepSearchParameterObject params = 
//				IDTDataCache.getNISTPepSearchParameterObjectById(match.getSearchParameterSetId());
//		if(params == null) {
//			return calculateEntropyBasedMatchScore(			
//					msms.getSpectrum(), 
//					match.getMatchedLibraryFeature().getSpectrum(),
//					20, 
//					MassErrorType.ppm,
//					DEFAULT_MS_REL_INT_NOISE_CUTOFF);
//		}
//		else
//			return calculateEntropyBasedMatchScore(			
//				msms.getSpectrum(), 
//				match.getMatchedLibraryFeature().getSpectrum(),
//				params.getFragmentMzErrorValue(), 
//				params.getFragmentMzErrorType(),
//				DEFAULT_MS_REL_INT_NOISE_CUTOFF);
//	}
	
	public static double calculateDefaultEntropyMatchScore(
			TandemMassSpectrum msms, 
			ReferenceMsMsLibraryMatch match) {

			return calculateEntropyBasedMatchScore(			
					msms.getSpectrum(), 
					match.getMatchedLibraryFeature().getSpectrum(),
					MRC2ToolBoxConfiguration.getSpectrumEntropyMassError(), 
					MRC2ToolBoxConfiguration.getSpectrumEntropyMassErrorType(),
					MRC2ToolBoxConfiguration.getSpectrumEntropyNoiseCutoff());
	}
}
















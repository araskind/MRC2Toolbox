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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import edu.umich.med.mrc2.datoolbox.data.Adduct;
import edu.umich.med.mrc2.datoolbox.data.AdductMatch;
import edu.umich.med.mrc2.datoolbox.data.MassSpectrum;
import edu.umich.med.mrc2.datoolbox.data.MsPoint;
import edu.umich.med.mrc2.datoolbox.data.enums.MassErrorType;
import edu.umich.med.mrc2.datoolbox.utils.MsUtils;
import edu.umich.med.mrc2.datoolbox.utils.Range;

public class SpectrumMatcher {

	public static Set<AdductMatch> matchAccurateMs(
			MassSpectrum observedSpectrum,
			MassSpectrum librarySpectrum,
			double massError,
			MassErrorType errorType,
			boolean ignoreAddudctMatching,
			boolean relaxMinorIsotopeError) {

		Set<AdductMatch> matchMap = new HashSet<AdductMatch>();

		// If experimental feature has only one adduct match to any available library adduct by mass and isotope ratio
		if(observedSpectrum.getAdducts().size() == 1) {

			for(Adduct ad : librarySpectrum.getAdducts()) {

				if(observedSpectrum.getPrimaryAdduct().getCharge() == ad.getCharge()) {

					double matchScore = matchMsPatterns(
							observedSpectrum.getMsForAdduct(observedSpectrum.getPrimaryAdduct()),
							librarySpectrum.getMsForAdduct(ad),
							massError,
							errorType,
							relaxMinorIsotopeError);

					if(matchScore > 0.0d)
						matchMap.add(new AdductMatch(
								ad, observedSpectrum.getPrimaryAdduct(), matchScore));					
				}
			}
		}
		if(observedSpectrum.getAdducts().size() > 1) {

			// Ignore pre-assigned adduct typeas in the observed spectrum
			if(ignoreAddudctMatching) {

				for(Adduct obsAd : observedSpectrum.getAdducts()) {

					for(Adduct ad : librarySpectrum.getAdducts()) {

						if(obsAd.getCharge() == ad.getCharge()) {

							double matchScore = matchMsPatterns(
									observedSpectrum.getMsForAdduct(obsAd),
									librarySpectrum.getMsForAdduct(ad),
									massError,
									errorType,
									relaxMinorIsotopeError);

							if(matchScore > 0.0d)
								matchMap.add(new AdductMatch(ad, obsAd, matchScore));
						}
					}
				}
			}
			else {	//	Check if same adducts present in both observed and library spectra
				HashSet<Adduct>commonAdducts = findCommonAdducts(observedSpectrum, librarySpectrum);

				if(!commonAdducts.isEmpty()) {

					for(Adduct ad : commonAdducts) {

						double matchScore = matchMsPatterns(
								observedSpectrum.getMsForAdduct(ad),
								librarySpectrum.getMsForAdduct(ad),
								massError,
								errorType,
								relaxMinorIsotopeError);

						if(matchScore > 0.0d)
							matchMap.add(new AdductMatch(ad, ad, matchScore));
					}
				}
			}
		}
		if(!matchMap.isEmpty()) {
			
			for(AdductMatch m : matchMap) {
				
				double entropyScore = calculateEntropyScoreForAdductMatch(
						observedSpectrum,  librarySpectrum, m, massError, errorType);
				m.setEntropyScore(entropyScore);
			}
		}
		return matchMap;
	}
	
	public static double calculateEntropyScoreForAdductMatch(
			MassSpectrum observedSpectrum, 
			MassSpectrum librarySpectrum, 
			AdductMatch m,
			double massError,
			MassErrorType errorType) {
		
		Collection<MsPoint> observed = observedSpectrum.getMsPointsForAdduct(m.getUnknownMatch());
		Collection<MsPoint> library = librarySpectrum.getMsPointsForAdduct(m.getLibraryMatch());
		return MSMSScoreCalculator.calculateEntropyBasedMatchScore(
				observed, library, massError, errorType, 
				MSMSScoreCalculator.DEFAULT_MS_REL_INT_NOISE_CUTOFF);
	}


	/**
	 * Compare two arrays of MS data points for high resolution MS matching
	 *
	 * @param observedPattern - observed isotopic pattern, noramalized to 100 and sorted by mass
	 * @param libraryPattern - library isotopic pattern, noramalized to 100 and sorted by mass
	 * @param massError - absolute mass error
	 * @param errorType - mass error type, ppm or mDa
	 * @param relaxMinorIsotopeError - flag to relax mass error when comparing minor isotopes
	 * @return - match score
	 */
	public static double matchMsPatterns(
			MsPoint[] observedPattern,
			MsPoint[] libraryPattern,
			double massError,
			MassErrorType errorType,
			boolean relaxMinorIsotopeError) {

		double score = 0.0d;
		double massErrorAdj;

		int length = Math.min(observedPattern.length, libraryPattern.length);
		if(length > 3)
			length = 3;

		//	Normalize MS
		MsPoint[] observedNormalized = normalizeAndSortMsPattern(observedPattern, length);
		MsPoint[] libraryNormalized = normalizeAndSortMsPattern(libraryPattern, length);

		//	Compare monoisotopis peaks
		Range mzRange = MsUtils.createMassRange(libraryNormalized[0].getMz(), massError, errorType);

		if(!mzRange.contains(observedNormalized[0].getMz()))
			return score;

		//	Matching by single mass set score at 50%
		if(observedNormalized.length == 1 && libraryNormalized.length > 1)
			return 50.0d;

		double[] unkIntensities = new double[3];
		double[] libIntensities = new double[3];

		unkIntensities[0] = observedNormalized[0].getIntensity();
		libIntensities[0] = libraryNormalized[0].getIntensity();

		int matchedIsotopeCount = 1;
		ArrayList<Double>intensityErrors = new ArrayList<Double>();
		intensityErrors.add(Math.abs(observedNormalized[0].getIntensity() 
				- libraryNormalized[0].getIntensity())/libraryNormalized[0].getIntensity());
		int isotopeNumber = Math.min(libraryNormalized.length, observedNormalized.length);

		if(isotopeNumber > 1) {

			for(int i=1; i<isotopeNumber; i++) {

				massErrorAdj = massError;
				if(relaxMinorIsotopeError)
					massErrorAdj = massError * 2;

				mzRange = MsUtils.createMassRange(
						libraryNormalized[i].getMz(), massErrorAdj, errorType);
				libIntensities[i] = libraryNormalized[i].getIntensity();

				if(mzRange.contains(observedNormalized[i].getMz())) {

					unkIntensities[i] = observedNormalized[i].getIntensity();
					intensityErrors.add(Math.abs(observedNormalized[i].getIntensity() 
							- libraryNormalized[i].getIntensity())/libraryNormalized[i].getIntensity());
					matchedIsotopeCount++;
				}
			}
			score = getSpectraScore(libIntensities, unkIntensities);
			//double meanIntensityError = StatUtils.mean(intensityErrors.stream().mapToDouble(Double::doubleValue).toArray());
			//score = ((double)matchedIsotopeCount / (double)isotopeNumber - meanIntensityError) * 100;
		}
		return score;
	}

	public static double getSpectraScore(
			double[] libIntensities, double[] unkIntensities) {

		if(libIntensities.length != unkIntensities.length)
			return 0.0;

		Vector3D vLib = new Vector3D(libIntensities);
		Vector3D vUnk = new Vector3D(unkIntensities);

		double dpDir = Vector3D.dotProduct(vLib, vUnk);
		double score = dpDir * dpDir / vLib.getNormSq() / vUnk.getNormSq();

		return score;
	}

	public static HashSet<Adduct> findCommonAdducts(
			MassSpectrum observedSpectrum,
			MassSpectrum librarySpectrum) {

		HashSet<Adduct>commonAdducts = new HashSet<Adduct>();

		for(Adduct obsAd : observedSpectrum.getAdducts()) {

			for(Adduct ad : librarySpectrum.getAdducts()) {

				if(obsAd.equals(ad))
					commonAdducts.add(ad);
			}
		}
		return commonAdducts;
	}

	//	Scale data point intensity and sort points by mass, 
	//	return only # of most intensive isotopes
	public static MsPoint[] normalizeAndSortMsPattern(MsPoint[] pattern, int topIons) {

		MsPoint basePeak = Collections.max(
				Arrays.asList(pattern), Comparator.comparing(MsPoint::getIntensity));
		double maxIntensity  = basePeak.getIntensity();

		return Arrays.stream(pattern).
				map(dp -> new MsPoint(dp.getMz(), 
						dp.getIntensity() / maxIntensity 
							* MsUtils.SPECTRUM_NORAMALIZATION_BASE_INTENSITY, 
							dp.getAdductType(), dp.getRt())).
				sorted(MsUtils.reverseIntensitySorter).
				limit(topIons).
				sorted(MsUtils.mzSorter).
				toArray(size -> new MsPoint[size]);
	}
}

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

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.lang3.StringUtils;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.formula.IsotopePattern;
import org.openscience.cdk.formula.IsotopePatternGenerator;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.interfaces.IIsotope;
import org.openscience.cdk.interfaces.IMolecularFormula;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;

import edu.umich.med.mrc2.datoolbox.data.Adduct;
import edu.umich.med.mrc2.datoolbox.data.AdductExchange;
import edu.umich.med.mrc2.datoolbox.data.AdductMatch;
import edu.umich.med.mrc2.datoolbox.data.BasicIsotopicPattern;
import edu.umich.med.mrc2.datoolbox.data.CompoundIdentity;
import edu.umich.med.mrc2.datoolbox.data.LabeledMolecularFormula;
import edu.umich.med.mrc2.datoolbox.data.LibraryMsFeature;
import edu.umich.med.mrc2.datoolbox.data.MassSpectrum;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureIdentity;
import edu.umich.med.mrc2.datoolbox.data.MsPoint;
import edu.umich.med.mrc2.datoolbox.data.MsPointBucket;
import edu.umich.med.mrc2.datoolbox.data.MsRtLibraryMatch;
import edu.umich.med.mrc2.datoolbox.data.SimpleMsMs;
import edu.umich.med.mrc2.datoolbox.data.TandemMassSpectrum;
import edu.umich.med.mrc2.datoolbox.data.compare.AdductComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.MsDataPointComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortDirection;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundIdSource;
import edu.umich.med.mrc2.datoolbox.data.enums.KendrickUnits;
import edu.umich.med.mrc2.datoolbox.data.enums.MassErrorType;
import edu.umich.med.mrc2.datoolbox.data.enums.ModificationType;
import edu.umich.med.mrc2.datoolbox.data.enums.MsStringType;
import edu.umich.med.mrc2.datoolbox.data.enums.Polarity;
import edu.umich.med.mrc2.datoolbox.data.enums.SpectrumSource;
import edu.umich.med.mrc2.datoolbox.database.idt.MSRTLibraryUtils;
import edu.umich.med.mrc2.datoolbox.main.AdductManager;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;

public class MsUtils {

	public static final double NEUTRON_MASS = 1.003354838;
	public static final double CARBON_MASS = 12.0;
	public static final double CARBON_13_NATURAL_ABUNDANCE = 1.07;
	public static final double HYDROGEN_MASS = 1.007825032;
	public static final double PROTON_MASS = 1.007276;
	public static final double ELECTRON_MASS = 5.4857990943E-4;
	public static final double MIN_ISOTOPE_ABUNDANCE = 0.001;
	public static final double MIN_PPM_DISTANCE = 100;
	public static final double CL_ISOTOPE_DISTANCE = 1.997049893;
	public static final double BR_ISOTOPE_DISTANCE = 1.997953413;

	public static final double SODIUM_PROTON_EXCHANGE = 21.9819;
	public static final double CL_FORMATE_EXCHANGE = 10.0288;
	
    //	Spectra normalization
    public static final double SPECTRUM_NORMALIZATION_BASE_INTENSITY = 999.0d;
    
    //	Minor isotope annotations
    public static final String minorIsotopeIdentificationLevelId = "IDS007";
    public static final String minorIsotopeStandardAnnotationId = "STAN0021";

	public static final IChemObjectBuilder builder = SilentChemObjectBuilder.getInstance();
	public static final SmilesParser smipar = new SmilesParser(builder);
	public static final Pattern isoLabelSmilesPattern = Pattern.compile("(\\[\\d+)");
	public static final LabeledIsotopePatternGenerator isotopePatternGenerator = 
			new LabeledIsotopePatternGenerator(MIN_ISOTOPE_ABUNDANCE);
	
	public static final NumberFormat spectrumMzFormat = new DecimalFormat("#.####");
	public static final NumberFormat spectrumMzExportFormat = new DecimalFormat("#.######");
	public static final NumberFormat spectrumIntensityFormat = new DecimalFormat("#.##");
	public static final DecimalFormat mspIntensityFormat = new DecimalFormat("###");
	public static final DecimalFormat pythonIntensityFormat = new DecimalFormat("###.#");
	
	public static final MsDataPointComparator reverseIntensitySorter = 
			new MsDataPointComparator(SortProperty.Intensity, SortDirection.DESC);
	public static final MsDataPointComparator directIntensitySorter = 
			new MsDataPointComparator(SortProperty.Intensity);
	public static final MsDataPointComparator mzSorter = 
			new MsDataPointComparator(SortProperty.MZ);
	public static final MsDataPointComparator scanSorter = 
			new MsDataPointComparator(SortProperty.scanNumber);
	
	public static double calculateModifiedMz(double neutralMass, Adduct mod) {

		double modifiedMz = neutralMass;

		IMolecularFormula addedFormula = null;
		IMolecularFormula lostFormula = null;
		IsotopePatternGenerator ipg;
		double addedMass = 0.0d;
		double lostMass = 0.0d;

		if (mod.getAddedGroup() != null && !mod.getAddedGroup().isEmpty()) {

			addedFormula = MolecularFormulaManipulator.getMolecularFormula(mod.getAddedGroup(),
					DefaultChemObjectBuilder.getInstance());
			ipg = new IsotopePatternGenerator();
			addedMass = ipg.getIsotopes(addedFormula).getMonoIsotope().getMass();
		}
		if (mod.getRemovedGroup() != null && !mod.getRemovedGroup().isEmpty()) {

			lostFormula = MolecularFormulaManipulator.getMolecularFormula(mod.getRemovedGroup(),
					DefaultChemObjectBuilder.getInstance());
			ipg = new IsotopePatternGenerator();
			lostMass = ipg.getIsotopes(lostFormula).getMonoIsotope().getMass();
		}
		modifiedMz = neutralMass * mod.getOligomericState() + addedMass - lostMass;

		if(mod.getCharge() != 0)
			modifiedMz = (modifiedMz - ELECTRON_MASS * mod.getCharge())/ Math.abs(mod.getCharge());

		return modifiedMz;
	}

	public static double calculateNeutralMass(double mz, Adduct mod) {

		double neutralMass = mz;

		IMolecularFormula addedFormula = null;
		IMolecularFormula lostFormula = null;
		IsotopePatternGenerator ipg;
		double addedMass = 0.0d;
		double lostMass = 0.0d;

		if (mod.getAddedGroup() != null && !mod.getAddedGroup().isEmpty()) {

			addedFormula = MolecularFormulaManipulator.getMolecularFormula(mod.getAddedGroup(),
					DefaultChemObjectBuilder.getInstance());
			ipg = new IsotopePatternGenerator();
			addedMass = ipg.getIsotopes(addedFormula).getMonoIsotope().getMass();
		}
		if (mod.getRemovedGroup() != null && !mod.getRemovedGroup().isEmpty()) {

			lostFormula = MolecularFormulaManipulator.getMolecularFormula(mod.getRemovedGroup(),
					DefaultChemObjectBuilder.getInstance());
			ipg = new IsotopePatternGenerator();
			lostMass = ipg.getIsotopes(lostFormula).getMonoIsotope().getMass();
		}
		if(mod.getCharge() == 0)
			neutralMass = mz;
		else
			neutralMass = (mz * Math.abs(mod.getCharge()) - addedMass + lostMass) / mod.getOligomericState();

		return neutralMass;
	}

	public static MsPoint[] createAverageSpectrum(ArrayList<SimpleMsMs> msSet, double massAccuracy) {

		ArrayList<MsPoint> points2average = new ArrayList<MsPoint>();
		boolean added;

		for (SimpleMsMs msms : msSet)
			points2average.addAll(Arrays.asList(msms.getDataPoints()));

		ArrayList<MsPointBucket> pointBaskets = new ArrayList<MsPointBucket>();

		for (MsPoint p : points2average) {

			added = false;

			for (MsPointBucket basket : pointBaskets) {

				if (basket.addPoint(p)) {
					added = true;
					break;
				}
			}
			if (!added) {

				MsPointBucket newBasket = 
						new MsPointBucket(p, massAccuracy, MassErrorType.ppm);
				pointBaskets.add(newBasket);
			}
		}
		ArrayList<MsPoint> avgPoints = new ArrayList<MsPoint>();

		for (MsPointBucket basket : pointBaskets)
			avgPoints.add(new MsPoint(basket.getMz(), basket.getAverageIntensity()));

		return avgPoints.toArray(new MsPoint[avgPoints.size()]);
	}

	public static Range createPpmMassRange(double mz, double accuracyPpm) {

		double b1 = mz * (1 - accuracyPpm / 1000000);
		double b2 = mz * (1 + accuracyPpm / 1000000);
		Range mzRange = new Range(Math.min(b1, b2), Math.max(b1, b2));
		return mzRange;
	}

	public static Range createMassRange(
			double mz, double accuracy, MassErrorType errorType) {

		double b1 = mz;
		double b2 = mz;

		if(errorType.equals(MassErrorType.ppm)) {
			b1 = mz * (1 - accuracy / 1000000.0d);
			b2 = mz * (1 + accuracy / 1000000.0d);
		}
		if(errorType.equals(MassErrorType.mDa)) {
			b1 = mz - accuracy / 1000.0d;
			b2 = mz + accuracy / 1000.0d;
		}
		if(errorType.equals(MassErrorType.Da)) {
			b1 = mz - accuracy;
			b2 = mz + accuracy;
		}
		return new Range(Math.min(b1, b2), Math.max(b1, b2));
	}

	public static Range createMassRangeWithReference(
			double keyMass, double refMass, double massAccuracy) {

		double diff = refMass * massAccuracy / 1000000;
		Range mzRange = new Range(keyMass - diff, keyMass + diff);
		return mzRange;
	}
	
	public static double calculateAbsoluteMassError(MsPoint p1, MsPoint p2, MassErrorType type) {		
		return calculateAbsoluteMassError(p1.getMz(), p2.getMz(), type);
	}
	
	public static double calculateAbsoluteMassError(double p1, double p2, MassErrorType type) {		
		
		if(type.equals(MassErrorType.mDa)) {
			return Math.abs(p1 - p2) / 1000.0d;
		}
		if(type.equals(MassErrorType.ppm)) {
			
			double maxMass = Math.max(p1, p2);
			return Math.abs(p1 - p2) / maxMass * 1000000.0d;
		}		
		return 0.0d;
	}

	public static double getAdductMz(double neutralMass, Adduct adduct) {

		double adjusted = neutralMass * (double) adduct.getOligomericState()
				+ calculateMassCorrectionFromAddedRemovedGroups(adduct);
		double mz = adjusted / (double) Math.abs(adduct.getCharge());
		return mz;
	}
	
	public static double calculateMassCorrectionFromAddedRemovedGroups(Adduct adduct) {

		IMolecularFormula addedGroupMf, removedGroupMf;
		double addedMass = 0.0d;
		double removedMass = 0.0d;
		double totalCorrection = 0.0d;
		
		adduct.finalizeModification();

		if (!adduct.getAddedGroup().trim().isEmpty()) {

			addedGroupMf = MolecularFormulaManipulator.getMolecularFormula(
					adduct.getAddedGroup().trim(), builder);
			if(addedGroupMf == null) {
				System.err.println("Unable to parse " + adduct.getAddedGroup().trim());
				return 0.0d;
			}
			addedMass = MolecularFormulaManipulator.getMass(
					addedGroupMf, MolecularFormulaManipulator.MonoIsotopic);
			//	addedMass = MolecularFormulaManipulator.getMajorIsotopeMass(addedGroupMf);
		}
		if (!adduct.getRemovedGroup().trim().isEmpty()) {

			removedGroupMf = MolecularFormulaManipulator.getMolecularFormula(
					adduct.getRemovedGroup().trim(), builder);
			if(removedGroupMf == null) {
				System.err.println("Unable to parse " + adduct.getRemovedGroup().trim());
				return 0.0d;
			}
			removedMass = MolecularFormulaManipulator.getMass(removedGroupMf, MolecularFormulaManipulator.MonoIsotopic);
		//	removedMass = MolecularFormulaManipulator.getMajorIsotopeMass(removedGroupMf);
		}
		totalCorrection = addedMass - removedMass - ELECTRON_MASS * adduct.getCharge();
		return totalCorrection;
	}
	
	public static double calculateExchangeMassDifference(AdductExchange exchange) {

		double removed = calculateMassCorrectionFromAddedRemovedGroups(exchange.getLeavingAdduct());
		double added = calculateMassCorrectionFromAddedRemovedGroups(exchange.getComingAdduct());
		return added - removed;
	}

	public static IsotopePatternGenerator getIsotopePatternGenerator() {
		return new IsotopePatternGenerator(MIN_ISOTOPE_ABUNDANCE);
	}


	public static Map<Adduct, Range> getModifiedMassRanges(double parentMass,
			Adduct parentMod, Collection<Adduct> adducts, double massAccuracy) {

		HashMap<Adduct, Range> modifiedMassRanges = new HashMap<Adduct, Range>();
		double neutralMass = MsUtils.getNeutralMassForAdduct(parentMass, parentMod);

		for (Adduct cm : adducts) {

			double mz = MsUtils.getAdductMz(neutralMass, cm);
			Range newRange = MsUtils.createPpmMassRange(mz, massAccuracy);
			modifiedMassRanges.put(cm, newRange);
		}
		return modifiedMassRanges;
	}

	public static Map<Adduct, Range> getNeutralLossRanges(double parentMass,
			Adduct parentMod, Collection<Adduct> repeats, double massAccuracy) {

		HashMap<Adduct, Range> modifiedMassRanges = new HashMap<Adduct, Range>();
		double neutralMass = MsUtils.getNeutralMassForAdduct(parentMass, parentMod);

		for (Adduct cm : repeats) {

			if (cm.getModificationType().equals(ModificationType.REPEAT) && cm.getMassCorrection() < 0) {

				double mz = neutralMass + cm.getMassCorrection();
				Range newRange = MsUtils.createPpmMassRange(mz, massAccuracy);
				modifiedMassRanges.put(cm, newRange);
			}
		}
		return modifiedMassRanges;
	}

	public static double getNeutralMass(MsPoint basePeak, Adduct primaryAdduct) {

		double chargeCorrected = basePeak.getMz() * Math.abs(primaryAdduct.getCharge());

		double correctForAdditionLossOligo = (chargeCorrected
				- calculateMassCorrectionFromAddedRemovedGroups(primaryAdduct)) / primaryAdduct.getOligomericState()
				+ primaryAdduct.getCharge() * ELECTRON_MASS;

		return correctForAdditionLossOligo;
	}

	public static double getNeutralMassForAdduct(double parentMz, Adduct adduct) {

		double chargeAdjusted = parentMz * (double) Math.abs(adduct.getCharge());
		double neutralMass = (chargeAdjusted - calculateMassCorrectionFromAddedRemovedGroups(adduct))
				/ (double) adduct.getOligomericState();
		return neutralMass;
	}

	public static boolean isBrominated(MsPoint[] pattern, double massAccuracy) {

		boolean brominated = false;
		if (pattern.length < 3)
			return false;

		Arrays.sort(pattern, MsUtils.mzSorter);
		Range refRange = MsUtils.createPpmMassRange(pattern[0].getMz(), massAccuracy);
		Range clRange = new Range(refRange.getMin() + BR_ISOTOPE_DISTANCE, refRange.getMax() + BR_ISOTOPE_DISTANCE);
		if (clRange.contains(pattern[2].getMz())
				&& pattern[2].getIntensity() / pattern[0].getIntensity() > 0.7d
				&& pattern[2].getIntensity() / pattern[1].getIntensity() > 1.2d)
			brominated = true;

		return brominated;
	}

	public static boolean isChlorinated(MsPoint[] pattern, double massAccuracy) {

		boolean chlorinated = false;
		if (pattern.length < 3)
			return false;

		Arrays.sort(pattern, MsUtils.mzSorter);
		Range refRange = MsUtils.createPpmMassRange(pattern[0].getMz(), massAccuracy);
		Range clRange = new Range(refRange.getMin() + CL_ISOTOPE_DISTANCE, refRange.getMax() + CL_ISOTOPE_DISTANCE);

		if (clRange.contains(pattern[2].getMz())
				&& pattern[2].getIntensity() / pattern[0].getIntensity() > 0.25d
				&& pattern[2].getIntensity() / pattern[1].getIntensity() > 1.2d)
			chlorinated = true;

		return chlorinated;
	}

	public static MassSpectrum parseMsString(String msString, MsStringType type) {

		MassSpectrum newMs = null;

		if(type.equals(MsStringType.MPP)) {

			String[] splitString = msString.replaceAll("\\)\\(", "\\\t").replace("(", "").replace(")", "").split("\\\t");
			ArrayList<MsPoint>points = new ArrayList<MsPoint>();
			for(String chunk : splitString) {

				String[] splitChunk = chunk.split(", ");
				double mz = Double.parseDouble(splitChunk[0].trim());
				double intensity = Double.parseDouble(splitChunk[1].trim());
				if(mz > 0 && intensity > 0) {

					MsPoint p = new MsPoint(mz, intensity);
					points.add(p);
				}
			}
			if(!points.isEmpty()) {

				newMs = new MassSpectrum();
				newMs.addDataPoints(points);
			}
		}
		return newMs;
	}

	public static double findMassDiffPpm(double m1, double m2) {
		return Math.abs((m1 - m2) / m1 * 1000000);
	}

	public static boolean matchesFeature(
			MsFeature reference,
			MsFeature possibleMatch,
			double ppmMassAccuracy,
			double rtWindow,
			boolean ignoreAdductAssignment) {

		boolean match = true;
		
		MassSpectrum referenceMs = reference.getSpectrum();
		if(referenceMs == null)
			return false;

		MassSpectrum possibleMatchMs = possibleMatch.getSpectrum();
		if(possibleMatchMs == null)
			return false;

		//	Check retention difference - observed if available, then library if no observed
		boolean rtChecked = false;
		if(reference.getStatsSummary() != null && possibleMatch.getStatsSummary() != null) {

			double refRt = reference.getMedianObservedRetention();
			double pmrt = possibleMatch.getMedianObservedRetention();
			if(refRt > 0 && pmrt > 0) {

				if(Math.abs(refRt - pmrt) > rtWindow)
					return false;
				else
					rtChecked = true;
			}
		}
		if (!rtChecked && Math.abs(reference.getRetentionTime() - possibleMatch.getRetentionTime()) > rtWindow)
			return false;

		//	Check mass difference
		double deltaPpm = ppmMassAccuracy * 3.0d;
		//	If ignore adduct assignment (including cases when adducts not specified for the feature)
		if(ignoreAdductAssignment || referenceMs.getAdducts().isEmpty() || possibleMatchMs.getAdducts().isEmpty()) {

			deltaPpm = Math.abs((referenceMs.getMonoisotopicMz() - possibleMatchMs.getMonoisotopicMz())/
					referenceMs.getMonoisotopicMz() * 1000000.0d);
			if(deltaPpm > ppmMassAccuracy)
				return false;
			
//			for(BasicIsotopicPattern ref : referenceMs.getIsotopicGroups()) {
//
//				for(BasicIsotopicPattern pm  : possibleMatchMs.getIsotopicGroups()) {
//
//					double refMz = ref.getDataPoints().iterator().next().getMz();
//					double pmMz = pm.getDataPoints().iterator().next().getMz();
//
//					deltaPpm = Math.abs((refMz - pmMz)/refMz * 1000000.0d);
//					if(deltaPpm < ppmMassAccuracy)
//						return true;
//				}
//			}
		}
		else{ //If consider adduct assignment
			for(Adduct refAd : referenceMs.getAdducts()) {

				for(Adduct pmAd : possibleMatchMs.getAdducts()) {

					if(refAd.equals(pmAd)) {

						double refMz = referenceMs.getMsForAdduct(refAd)[0].getMz();
						double pmMz = possibleMatchMs.getMsForAdduct(pmAd)[0].getMz();

						deltaPpm = Math.abs((refMz - pmMz)/refMz * 1000000.0d);
						if(deltaPpm < ppmMassAccuracy)
							return true;
					}
				}
			}
		}
		if (deltaPpm > ppmMassAccuracy)
			return false;

		return match;
	}
	
	public static boolean matchesFeature(
			MsFeature reference,
			MsFeature possibleMatch,
			double massAccuracy,
			MassErrorType massErrorType,
			double rtWindow,
			boolean ignoreAdductAssignment) {

		boolean match = false;
		MassSpectrum referenceMs = reference.getSpectrum();
		if(referenceMs == null)
			return false;

		MassSpectrum possibleMatchMs = possibleMatch.getSpectrum();
		if(possibleMatchMs == null)
			return false;

		//	Check retention difference - observed if available, then library if no observed
		boolean rtChecked = false;
		if(reference.getStatsSummary() != null && possibleMatch.getStatsSummary() != null) {

			double refRt = reference.getStatsSummary().getMedianObservedRetention();
			double pmrt = possibleMatch.getStatsSummary().getMedianObservedRetention();
			if(refRt > 0 && pmrt > 0) {

				if(Math.abs(refRt - pmrt) > rtWindow)
					return false;
				else
					rtChecked = true;
			}
		}
		if (!rtChecked && Math.abs(reference.getRetentionTime() - possibleMatch.getRetentionTime()) > rtWindow)
			return false;

		//	If ignore adduct assignment (including cases when adducts not specified for the feature)
		if(ignoreAdductAssignment || referenceMs.getAdducts().isEmpty() || possibleMatchMs.getAdducts().isEmpty()) {

			for(BasicIsotopicPattern ref : referenceMs.getIsotopicGroups()) {

				for(BasicIsotopicPattern pm  : possibleMatchMs.getIsotopicGroups()) {

					double refMz = ref.getDataPoints().iterator().next().getMz();
					double pmMz = pm.getDataPoints().iterator().next().getMz();					
					double error = Math.abs(calculateMassError(refMz, pmMz, massErrorType));
					if(error < massAccuracy)
						return true;
				}
			}
		}
		else{ //If consider adduct assignment
			for(Adduct refAd : referenceMs.getAdducts()) {

				for(Adduct pmAd : possibleMatchMs.getAdducts()) {

					if(refAd.equals(pmAd)) {

						double refMz = referenceMs.getMsForAdduct(refAd)[0].getMz();
						double pmMz = possibleMatchMs.getMsForAdduct(pmAd)[0].getMz();
						double error = Math.abs(calculateMassError(refMz, pmMz, massErrorType));
						if(error < massAccuracy)
							return true;
					}
				}
			}
		}
		return match;
	}
	
	public static boolean matchesOnMSMSParentIon(
			MsFeature reference,
			MsFeature possibleMatch,
			double massAccuracy,
			MassErrorType massErrorType,
			double rtWindow,
			boolean ignoreAdductAssignment) {

		MassSpectrum referenceMs = reference.getSpectrum();
		MassSpectrum possibleMatchMs = possibleMatch.getSpectrum();
		if(referenceMs == null || possibleMatchMs == null)
			return false;
		
		TandemMassSpectrum refMsMs = referenceMs.getExperimentalTandemSpectrum();
		TandemMassSpectrum matchMsMs = possibleMatchMs.getExperimentalTandemSpectrum();
		if(refMsMs == null || matchMsMs == null)
			return false;

		//	Check retention difference - observed if available, then library if no observed
		boolean rtChecked = false;
		if(reference.getStatsSummary() != null && possibleMatch.getStatsSummary() != null) {

			double refRt = reference.getStatsSummary().getMedianObservedRetention();
			double pmrt = possibleMatch.getStatsSummary().getMedianObservedRetention();
			if(refRt > 0 && pmrt > 0) {

				if(Math.abs(refRt - pmrt) > rtWindow)
					return false;
				else
					rtChecked = true;
			}
		}
		if (!rtChecked 
				&& Math.abs(reference.getRetentionTime() - possibleMatch.getRetentionTime()) > rtWindow)
			return false;

		double error = calculateAbsoluteMassError(
				refMsMs.getParent().getMz(), 
				matchMsMs.getParent().getMz(), 
				massErrorType);
		if(error < massAccuracy)
			return true;
		else
			return false;
	}
	
	public static Collection<MsPoint[]>getIsotopicGroupsForSpectrum(MassSpectrum spectrum, int maxCharge){

		MsPoint[] pattern = spectrum.getCompletePattern();
		ArrayList<BasicIsotopicPattern>isoPatterns = new ArrayList<BasicIsotopicPattern>();
		ArrayList<MsPoint[]>isoPatternPoints = new ArrayList<MsPoint[]>();
		isoPatterns.add(new BasicIsotopicPattern(pattern[0]));
		boolean added;

		for(int j=1; j<pattern.length; j++) {

			added = false;

			for(int i = maxCharge; i>0; i--) {

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
		for(BasicIsotopicPattern ipp : isoPatterns)
			isoPatternPoints.add(ipp.getDataPoints().toArray(new MsPoint[ipp.getDataPoints().size()]));

		return isoPatternPoints;
	}

	public static void guessAdducts(MassSpectrum spectrum, Polarity polarity, int maxCharge) {

		// Isolate aducts
		MsPoint[] pattern = spectrum.getCompletePattern();
		ArrayList<BasicIsotopicPattern>isoPatterns = new ArrayList<BasicIsotopicPattern>();
		isoPatterns.add(new BasicIsotopicPattern(pattern[0]));
		boolean added;

		for(int j=1; j<pattern.length; j++) {

			added = false;

			for(int i = maxCharge; i>0; i--) {

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
		//	Assign aducts
		if(isoPatterns.size() == 1) {

			Adduct adduct = AdductManager.getDefaultAdductForCharge(isoPatterns.get(0).getCharge());
			Set<MsPoint> points = isoPatterns.get(0).getDataPoints();

			if(adduct != null && !points.isEmpty()) {

				spectrum.clearAdductMap();
				spectrum.addSpectrumForAdduct(adduct, points);
			}
		}
		if(isoPatterns.size() > 1) {

			//	TODO Debug only, replace with real adduct assignment
			Adduct adduct = AdductManager.getDefaultAdductForCharge(isoPatterns.get(0).getCharge());
			Set<MsPoint> points = isoPatterns.get(0).getDataPoints();

			if(adduct != null && !points.isEmpty()) {

				spectrum.clearAdductMap();
				spectrum.addSpectrumForAdduct(adduct, points);
			}
		}
	}
	
	public static Double getPpmMassErrorForTopAdductMatch(MsFeature feature) {
		
		if(feature.getPrimaryIdentity() == null 
				|| feature.getPrimaryIdentity().getMsRtLibraryMatch() == null)
			return Double.NaN;
		
		AdductMatch adductMatch = 
				feature.getPrimaryIdentity().getMsRtLibraryMatch().getTopAdductMatch();
		if(adductMatch == null)
			return Double.NaN;
		
		MsPoint[] observed = feature.getSpectrum().getMsForAdduct(adductMatch.getUnknownMatch());
		MsPoint[] library = feature.getPrimaryIdentity().getMsRtLibraryMatch().
				getLibrarySpectrum().getMsForAdduct(adductMatch.getLibraryMatch());
		if(observed == null || library == null)
			return Double.NaN;
		
		return (observed[0].getMz() - library[0].getMz()) / library[0].getMz() * 1000000.0d;
	}

	public static double getPpmMassErrorForIdentity(MsFeature parentFeature, MsFeatureIdentity id) {

		double error = 0.0d;
		LibraryMsFeature mslf = null;
		MassSpectrum obsSpec = parentFeature.getSpectrum();

		if(obsSpec == null)
			return 0.0d;

		if(id.getIdSource().equals(CompoundIdSource.LIBRARY)) {
			
			MsRtLibraryMatch libMatch = id.getMsRtLibraryMatch();
			if(libMatch == null)
				return 0.0d;

			if(libMatch.getTopAdductMatch() == null)
				return 0.0d;
			
			Adduct idAdduct = libMatch.getTopAdductMatch().getUnknownMatch();
			if(idAdduct != null) {

				//	Compare to library feature if present
				if(libMatch.getLibraryTargetId() != null) {

					try {
						mslf = MSRTLibraryUtils.getLibraryFeatureById(libMatch.getLibraryTargetId());
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if(mslf != null) {

						MsPoint[] ms = obsSpec.getMsForAdduct(idAdduct);
						Adduct matchAdduct = libMatch.getTopAdductMatch().getLibraryMatch();

						if(matchAdduct != null) {

							MsPoint[] libMs = mslf.getSpectrum().getMsForAdduct(matchAdduct);
							if(libMs != null)
								error = (libMs[0].getMz() - ms[0].getMz())/libMs[0].getMz() * 1000000.0d;
						}
					}
				}
				//	TODO compare to database ID if present
			}
		}		
		//	Calculate error for MSMS match based on parent M/Z in experimental and library spectrum
		if(id.getIdSource().equals(CompoundIdSource.LIBRARY_MS2) || 
				id.getIdSource().equals(CompoundIdSource.LIBRARY_MS2_RT)) {

			if(id.getReferenceMsMsLibraryMatch() != null) {
				
				MsPoint parentPeak = id.getReferenceMsMsLibraryMatch().getMatchedLibraryFeature().getParent();
				if(parentPeak == null) {
					//	System.out.println("No parent peak for " + id.getName());
					return 0.0d;
				}
				double libParentMz = parentPeak.getMz();
				TandemMassSpectrum msms = parentFeature.getSpectrum().getTandemSpectrum(SpectrumSource.EXPERIMENTAL);
				if(msms != null) {
					double expParentMz = msms.getParent().getMz();
					error = (expParentMz - libParentMz)/libParentMz * 1000000.0d;
				}
			}
		}
		return error;
	}
	
	public static double getMassErrorForIdentity(
			MsFeature parentFeature, 
			MsFeatureIdentity id, 
			MassErrorType errorType) {

		double error = 0.0d;
		LibraryMsFeature mslf = null;
		MassSpectrum obsSpec = parentFeature.getSpectrum();

		if(obsSpec == null)
			return 0.0d;

		if(id.getIdSource().equals(CompoundIdSource.LIBRARY)) {
			
			MsRtLibraryMatch libMatch = id.getMsRtLibraryMatch();
			if(libMatch == null)
				return 0.0d;

			if(libMatch.getTopAdductMatch() == null)
				return 0.0d;
			
			Adduct idAdduct = libMatch.getTopAdductMatch().getUnknownMatch();
			if(idAdduct != null) {

				//	Compare to library feature if present
				if(libMatch.getLibraryTargetId() != null) {

					try {
						mslf = MSRTLibraryUtils.getLibraryFeatureById(libMatch.getLibraryTargetId());
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if(mslf != null) {

						MsPoint[] ms = obsSpec.getMsForAdduct(idAdduct);
						Adduct matchAdduct = libMatch.getTopAdductMatch().getLibraryMatch();
						if(matchAdduct != null) {

							MsPoint[] libMs = mslf.getSpectrum().getMsForAdduct(matchAdduct);
							if(libMs != null)
								return calculateMassError(libMs[0].getMz(), ms[0].getMz(), errorType);
						}
					}
				}
			}
		}		
		//	Calculate error for MSMS match based on parent M/Z in experimental and library spectrum
		if(id.getIdSource().equals(CompoundIdSource.LIBRARY_MS2) || 
				id.getIdSource().equals(CompoundIdSource.LIBRARY_MS2_RT)) {

			if(id.getReferenceMsMsLibraryMatch() != null) {
				
				MsPoint parentPeak = id.getReferenceMsMsLibraryMatch().getMatchedLibraryFeature().getParent();
				if(parentPeak == null) {
					System.out.println("No parent peak for " + id.getCompoundName());
					return 0.0d;
				}
				TandemMassSpectrum msms = parentFeature.getSpectrum().getExperimentalTandemSpectrum();
				if(msms != null)
					return calculateMassError(parentPeak.getMz(), msms.getParent().getMz(), errorType);
				
			}
		}
		return error;
	}
	
	public static double getMassErrorForIdentity(
			double expectedMz, 
			MsFeatureIdentity id, 
			MassErrorType errorType) {

		double error = 0.0d;
		LibraryMsFeature mslf = null;

		if(id.getIdSource().equals(CompoundIdSource.LIBRARY)) {
			
			MsRtLibraryMatch libMatch = id.getMsRtLibraryMatch();
			if(libMatch == null)
				return 0.0d;

			if(libMatch.getTopAdductMatch() == null)
				return 0.0d;
			
			Adduct idAdduct = libMatch.getTopAdductMatch().getUnknownMatch();
			if(idAdduct != null) {

				//	Compare to library feature if present
				if(libMatch.getLibraryTargetId() != null) {

					try {
						mslf = MSRTLibraryUtils.getLibraryFeatureById(libMatch.getLibraryTargetId());
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if(mslf != null) {

						Adduct matchAdduct = libMatch.getTopAdductMatch().getLibraryMatch();
						if(matchAdduct != null) {

							MsPoint[] libMs = mslf.getSpectrum().getMsForAdduct(matchAdduct);
							if(libMs != null)
								return calculateMassError(libMs[0].getMz(), expectedMz, errorType);
						}
					}
				}
			}
		}		
		//	Calculate error for MSMS match based on parent M/Z in experimental and library spectrum
		if(id.getIdSource().equals(CompoundIdSource.LIBRARY_MS2) || 
				id.getIdSource().equals(CompoundIdSource.LIBRARY_MS2_RT)) {

			if(id.getReferenceMsMsLibraryMatch() != null) {
				
				MsPoint parentPeak = id.getReferenceMsMsLibraryMatch().getMatchedLibraryFeature().getParent();
				if(parentPeak == null) {
					System.out.println("No parent peak for " + id.getCompoundName());
					return 0.0d;
				}
				return calculateMassError(parentPeak.getMz(), expectedMz, errorType);				
			}
		}
		return error;
	}
	
	public static double calculateMassError(
			double ref, 
			double unknown, 
			MassErrorType errorType) {
		
		if(errorType.equals(MassErrorType.Da))
			return unknown - ref;
		
		if(errorType.equals(MassErrorType.mDa))
			return (unknown - ref) * 1000.0d;
		
		if(errorType.equals(MassErrorType.ppm))
			return (unknown - ref) / ref * 1000000.0d;
		
		return 0.0d;
	}
	
	public static Collection<MsPoint>calculateIsotopeDistribution(String formulaString, Adduct adduct) {
		return calculateIsotopeDistribution(formulaString, adduct, true);
	}
	
	public static Collection<MsPoint>calculateIsotopeDistribution(
			String formulaString, 
			Adduct adduct, 
			boolean cleanup) {

		IMolecularFormula queryFormula = null;		
		try {
			queryFormula = MolecularFormulaManipulator.getMolecularFormula(formulaString, builder);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(queryFormula == null)
			return null;
		
		return calculateIsotopeDistribution(queryFormula, adduct, cleanup);
	}
	
	public static Collection<MsPoint>calculateIsotopeDistribution(
			IMolecularFormula queryFormula, 
			Adduct adduct, 
			boolean cleanup) {
	
		IMolecularFormula adductCompleteFormula = createCompleteAdductFormula(queryFormula, adduct);
		if(adductCompleteFormula == null)
			return null;
		
		return calculateIsotopeDistribution(adductCompleteFormula, cleanup);
	}
	
	public static Collection<MsPoint>calculateIsotopeDistribution(
			IMolecularFormula queryFormula, 
			boolean cleanup) {
		
		List<MsPoint> calculatedPattern = null;
		IsotopePattern isoPattern = 
				isotopePatternGenerator.getIsotopes(queryFormula);

		if(queryFormula.getCharge() == null || queryFormula.getCharge() == 0) {
			
			calculatedPattern  =  isoPattern.getIsotopes().stream().
				map(ic -> new MsPoint(ic.getMass(), ic.getIntensity() 
						* SPECTRUM_NORMALIZATION_BASE_INTENSITY)).
				sorted(mzSorter).collect(Collectors.toList());
		}
		else {
			int charge = Math.abs(queryFormula.getCharge());
			calculatedPattern =  isoPattern.getIsotopes().stream().
				map(ic -> new MsPoint((ic.getMass() - MsUtils.ELECTRON_MASS * charge)/charge, ic.getIntensity() 
						* SPECTRUM_NORMALIZATION_BASE_INTENSITY)).
				sorted(mzSorter).collect(Collectors.toList());
		}		
		if(cleanup)
			return cleanupIsotopicPattern(calculatedPattern, 100.0d, MassErrorType.mDa);
		else
			return calculatedPattern;
	}
	
	public static IMolecularFormula createCompleteAdductFormula(IMolecularFormula queryFormula, Adduct adduct){
		
		LabeledMolecularFormula adductCompleteFormula = new LabeledMolecularFormula(queryFormula);

		// Add isotopes for oligomers
		if (adduct.getOligomericState() > 1) {

			for (IIsotope iso : adductCompleteFormula.isotopes())
				adductCompleteFormula.addIsotope(iso, adductCompleteFormula.getIsotopeCount(iso) * (adduct.getOligomericState() - 1));
		}
		if (!adduct.getAddedGroup().isEmpty()) {

			IMolecularFormula addedGroup = 
					MolecularFormulaManipulator.getMajorIsotopeMolecularFormula(adduct.getAddedGroup(), builder);
			for (IIsotope iso : addedGroup.isotopes())
				adductCompleteFormula.addIsotope(iso, addedGroup.getIsotopeCount(iso));
		}
		if (!adduct.getRemovedGroup().isEmpty()) {

			IMolecularFormula neutralLoss =
					MolecularFormulaManipulator.getMajorIsotopeMolecularFormula(adduct.getRemovedGroup(), builder);
			
			for (IIsotope nliso : neutralLoss.isotopes()) {				
				try {
					adductCompleteFormula.removeIsotope(nliso, neutralLoss.getIsotopeCount(nliso));
				} catch (Exception e) {
					// TODO Auto-generated catch block
					//	e.printStackTrace();
					System.out.println(e.getMessage());
					return null;
				}
			}
		}
		adductCompleteFormula.setCharge(adduct.getCharge());
		return adductCompleteFormula;
	}
	
	public static Collection<MsPoint> calculateIsotopeDistributionFromSmiles(String smiles, Adduct adduct){
		return calculateIsotopeDistributionFromSmiles(smiles, adduct, true);
	}
	
	public static Collection<MsPoint> calculateIsotopeDistributionFromSmiles(String smiles, Adduct adduct, boolean cleanup) {

		IMolecularFormula adductCompleteFormula = getMolecularFormulaForAdductFromSmiles(smiles, adduct);
		if(adductCompleteFormula == null)
			return null;

		return calculateIsotopeDistribution(adductCompleteFormula, cleanup);
	}

	public static IMolecularFormula getMolecularFormulaForAdductFromSmiles(String smiles, Adduct adduct) {
		
		LabeledMolecularFormula adductCompleteFormula = null;
		IAtomContainer mlab = null;
		try {
			mlab = smipar.parseSmiles(smiles);
		} catch (InvalidSmilesException e) {
			e.printStackTrace();
		}
		if(mlab == null)
			return null;
	
		AtomContainerManipulator.convertImplicitToExplicitHydrogens(mlab);		
		adductCompleteFormula = new LabeledMolecularFormula(mlab);
		 
		// Add isotopes for oligomers
		if (adduct.getOligomericState() > 1) {
			
			int mult = adduct.getOligomericState() - 1;
			for (IIsotope iso : adductCompleteFormula.isotopes())
				adductCompleteFormula.addIsotope(iso, adductCompleteFormula.getIsotopeCount(iso) * mult);
		}
		if (!adduct.getAddedGroup().isEmpty()) {

			IMolecularFormula addedGroup = 
					MolecularFormulaManipulator.getMolecularFormula(adduct.getAddedGroup(), builder);
			for (IIsotope iso : addedGroup.isotopes())
				adductCompleteFormula.addIsotope(iso, addedGroup.getIsotopeCount(iso));
		}
		if (!adduct.getRemovedGroup().isEmpty()) {

			IMolecularFormula neutralLoss = 
					MolecularFormulaManipulator.getMolecularFormula(adduct.getRemovedGroup(), builder);
			for (IIsotope nliso : neutralLoss.isotopes()) {

				try {
					adductCompleteFormula.removeIsotope(nliso, neutralLoss.getIsotopeCount(nliso));
				} catch (Exception e) {
					// TODO Auto-generated catch block
					//	e.printStackTrace();
					return null;
				}
			}
		}
		adductCompleteFormula.setCharge(adduct.getCharge());
		return adductCompleteFormula;
	}
	
	public static Collection<MsPoint>cleanupIsotopicPattern(
			Collection<MsPoint>inputPoints, Double mzBinWidth, MassErrorType errorType) {
		
		MsPoint[] points = inputPoints.stream().
				sorted(MsUtils.mzSorter).
				toArray(size -> new MsPoint[size]);
		
		ArrayList<MsPointBucket> msBins = new ArrayList<MsPointBucket>();
		MsPointBucket first = new MsPointBucket(points[0], mzBinWidth, errorType);
		msBins.add(first);
		for(int i= 1; i< points.length; i++) {
			
			MsPointBucket current = msBins.get(msBins.size()-1);
			if(!current.addPoint(points[i]))
				msBins.add(new MsPointBucket(points[i], mzBinWidth, errorType));
		}
		return msBins.stream().map(b -> b.getMostIntensivePoint()).
				sorted(mzSorter).collect(Collectors.toList());
	}

	public static Collection<MsPoint>calculateIsotopeDistributionOld(IMolecularFormula queryFormula, Adduct adduct) {

		IMolecularFormula adductCompleteFormula = null;
		try {
			adductCompleteFormula = (IMolecularFormula) queryFormula.clone();
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (adductCompleteFormula != null) {

			// Add isotopes for oligomers
			if (adduct.getOligomericState() > 1) {

				for (IIsotope iso : adductCompleteFormula.isotopes())
					adductCompleteFormula.addIsotope(iso, adductCompleteFormula.getIsotopeCount(iso) * (adduct.getOligomericState() - 1));
			}
			if (!adduct.getAddedGroup().isEmpty()) {

				IMolecularFormula addedGroup =
						MolecularFormulaManipulator.getMolecularFormula(adduct.getAddedGroup(), builder);

				for (IIsotope iso : addedGroup.isotopes())
					adductCompleteFormula.addIsotope(iso, addedGroup.getIsotopeCount(iso));
			}
			if (!adduct.getRemovedGroup().isEmpty()) {

				IMolecularFormula neutralLoss =
						MolecularFormulaManipulator.getMolecularFormula(adduct.getRemovedGroup(), builder);

				for (IIsotope nliso : neutralLoss.isotopes()) {

					if (adductCompleteFormula.getIsotopeCount(nliso) > neutralLoss.getIsotopeCount(nliso)) {

						int count = adductCompleteFormula.getIsotopeCount(nliso) - neutralLoss.getIsotopeCount(nliso);
						adductCompleteFormula.removeIsotope(nliso);
						adductCompleteFormula.addIsotope(nliso, count);
					}
					else if (adductCompleteFormula.getIsotopeCount(nliso) == neutralLoss.getIsotopeCount(nliso)) {
						adductCompleteFormula.removeIsotope(nliso);
					} else {
						return null;
					}
				}
			}
			double charge = Math.abs(adduct.getCharge());
			IsotopePattern isoPattern = MsUtils.getIsotopePatternGenerator().getIsotopes(adductCompleteFormula);
			if(charge == 0) {
				return isoPattern.getIsotopes().stream().
						map(ic -> new MsPoint(ic.getMass(), ic.getIntensity() 
								* SPECTRUM_NORMALIZATION_BASE_INTENSITY)).
						collect(Collectors.toList());
			}
			else
				return isoPattern.getIsotopes().stream().
						map(ic -> new MsPoint((ic.getMass() - MsUtils.ELECTRON_MASS 
								* adduct.getCharge())/charge, ic.getIntensity() 
								* SPECTRUM_NORMALIZATION_BASE_INTENSITY)).
						collect(Collectors.toList());
		}
		else
			return null;
	}
	
	public static Collection<MsPoint> normalizeAndSortMsPointsCollection(Collection<MsPoint>pattern) {
		return normalizeAndSortMsPointsCollection(pattern, SPECTRUM_NORMALIZATION_BASE_INTENSITY);
	}
	
	public static Collection<MsPoint> normalizeAndSortMsPointsCollection(MsPoint[] pattern) {
		return normalizeAndSortMsPointsCollection(
				Arrays.asList(pattern), SPECTRUM_NORMALIZATION_BASE_INTENSITY);
	}
	
	public static Collection<MsPoint> normalizeAndSortMsPointsCollection(Collection<MsPoint>pattern, double max) {
		
		MsPoint basePeak = Collections.max(pattern, Comparator.comparing(MsPoint::getIntensity));
		double maxIntensity  = basePeak.getIntensity();
		return pattern.stream()
				.map(dp -> new MsPoint(dp.getMz(), dp.getIntensity()/maxIntensity * max))
				.sorted(MsUtils.mzSorter).collect(Collectors.toList());
	}

	public static MsPoint[] normalizeAndSortMsPattern(Collection<MsPoint>pattern) {
		return normalizeAndSortMsPattern(pattern, SPECTRUM_NORMALIZATION_BASE_INTENSITY);
	}
	
	public static MsPoint[] normalizeAndSortMsPattern(MsPoint[] pattern) {
		return normalizeAndSortMsPattern(
				Arrays.asList(pattern), SPECTRUM_NORMALIZATION_BASE_INTENSITY);
	}
	
	public static MsPoint[] normalizeAndSortMsPattern(Collection<MsPoint>pattern, double max) {
		
		MsPoint basePeak = Collections.max(pattern, Comparator.comparing(MsPoint::getIntensity));
		double maxIntensity  = basePeak.getIntensity();
		return pattern.stream()
				.map(dp -> new MsPoint(dp.getMz(), dp.getIntensity()/maxIntensity * max))
				.sorted(MsUtils.mzSorter).
				toArray(size -> new MsPoint[size]);
	}
	
	public static Collection<Double> getMassDifferences(
			Collection<MsPoint>pattern, double relativeIntensityCutoff) {
		
		MsPoint[]patternNorm =  normalizeAndSortMsPattern(pattern, 1.0d);
		MsPoint[]filtered = Arrays.asList(patternNorm).stream().
			filter(p -> p.getIntensity() > relativeIntensityCutoff).
			sorted(MsUtils.mzSorter).
			toArray(size -> new MsPoint[size]);		
		Collection<Double>massDiffs = new ArrayList<Double>();
		if(filtered.length < 2)
			return massDiffs;
		
		for(int i=0; i<filtered.length-1; i++) {
			
			for(int j=i+1; j<filtered.length; j++)				
				massDiffs.add(filtered[j].getMz() - filtered[i].getMz());			
		}	
		return massDiffs;
	}
	
	public static Collection<Double> getNeutralLosses(
			TandemMassSpectrum msms, double relativeIntensityCutoff) {
		
		MsPoint[]patternNorm =  normalizeAndSortMsPattern(msms.getSpectrum(), 1.0d);
		double parentMz = msms.getParent().getMz();
		MsPoint[]filtered = Arrays.asList(patternNorm).stream().
			filter(p -> p.getMz() < parentMz).
			filter(p -> p.getIntensity() > relativeIntensityCutoff).
			sorted(MsUtils.mzSorter).
			toArray(size -> new MsPoint[size]);		
		Collection<Double>massDiffs = new ArrayList<Double>();
		if(filtered.length < 1)
			return massDiffs;		
		
		for(int i=0; i<filtered.length-1; i++) 
			massDiffs.add(parentMz - filtered[i].getMz());			
		
		return massDiffs;
	}

	public static Map<Adduct,Collection<MsPoint>>createIsotopicPatternCollection(
			CompoundIdentity id, Collection<Adduct>adducts){

		Map<Adduct,Collection<MsPoint>>adductMap = 
				new TreeMap<Adduct,Collection<MsPoint>>(new AdductComparator(SortProperty.Name));
		String smiles = id.getSmiles();
		if(smiles != null) {

			if(!smiles.isEmpty()) {

				IAtomContainer mol = null;
				IMolecularFormula molFormula = null;
				try {
					mol = smipar.parseSmiles(id.getSmiles());
				} catch (Exception e1) {
					e1.printStackTrace();
					return adductMap;
				}
				if(isoLabelSmilesPattern.matcher(smiles).find()) {

					for (Adduct adduct : adducts) {

						Collection<MsPoint> adductPoints =
								MsUtils.calculateIsotopeDistributionFromSmiles(smiles, adduct);
						if (adductPoints != null)
							adductMap.put(adduct, adductPoints);
					}
					return adductMap;
				}
			}
		}
		String formula = id.getFormula();
		IMolecularFormula queryFormula = null;
		try {
			queryFormula =
					MolecularFormulaManipulator.getMolecularFormula(
							formula, DefaultChemObjectBuilder.getInstance());
			for (Adduct adduct : adducts) {

				Collection<MsPoint>adductPoints = 
						calculateIsotopeDistribution(queryFormula, adduct, true);
				if (adductPoints != null)
					adductMap.put(adduct, adductPoints);
			}
			return adductMap;
		}
		catch (Exception e) {
			e.printStackTrace();;
		}
		return adductMap;
	}
	
	public static double getExactMassForCompoundIdentity(CompoundIdentity id) {
		
		if(id == null)
			return 0.0;
		
		String formula = id.getFormula();
		String smiles = id.getSmiles();
		
		if(formula == null && smiles == null)
			return 0.0;
			
		if(smiles != null && MsUtils.isoLabelSmilesPattern.matcher(smiles).find()) {
			
			Collection<MsPoint>points =
					MsUtils.calculateIsotopeDistributionFromSmiles(
							smiles, AdductManager.getDefaultAdductForCharge(0));
			
			if(points == null || points.isEmpty())
				return 0.0d;
			else
				return points.iterator().next().getMz();
		}
		else {
			IMolecularFormula mf = null;
			if(formula != null) {
				
				try {
					mf = MolecularFormulaManipulator.getMolecularFormula(
							formula, DefaultChemObjectBuilder.getInstance());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if(mf != null)
					return MolecularFormulaManipulator.getMajorIsotopeMass(mf);
			}
			if(mf == null) {
				
				Collection<MsPoint>points =
						MsUtils.calculateIsotopeDistributionFromSmiles(
								smiles, AdductManager.getDefaultAdductForCharge(0));
				
				if(points == null || points.isEmpty())
					return 0.0d;
				else
					return points.iterator().next().getMz();
			}
		}		
		return 0.0d;
	}

	public static String calculateSpectrumHash(Collection<MsPoint>spectrum){

		List<String> chunks = spectrum.stream().sorted(MsUtils.mzSorter).
			map(p -> spectrumMzFormat.format(p.getMz()) +
					spectrumIntensityFormat.format(p.getIntensity())).
			collect(Collectors.toList());

	    try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(StringUtils.join(chunks).getBytes(Charset.forName("windows-1252")));
			return DatatypeConverter.printHexBinary(md.digest()).toUpperCase();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	    return null;
	}
	
	public static String calculateMzHash(TreeSet<Double>mzValues){

		List<String> chunks = mzValues.stream().map(p -> spectrumMzFormat.format(p)).
			collect(Collectors.toList());
	    try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(StringUtils.join(chunks).getBytes(Charset.forName("windows-1252")));
			return DatatypeConverter.printHexBinary(md.digest()).toUpperCase();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	    return null;
	}
		
	/**
	 * @param spectrum
	 * @return pattern recognition entropy
	 * Based on paper
	 * https://www.sciencedirect.com/science/article/pii/S0021967318304758
	 * "Pattern recognition entropy"
	 */
	public static double calculateSpectrumEntropy(Collection<MsPoint>spectrum) {
		
		double totalIntensity = spectrum.stream().mapToDouble(p -> p.getIntensity()).sum();
		double entropy = 0.0d;
		double log2 = Math.log(2);
		for(MsPoint p : spectrum) {
			
			if(p.getIntensity() == 0)
				continue;
			
			double norm = p.getIntensity() / totalIntensity;
			entropy += norm * Math.log(norm) / log2;
		}	
		return -entropy;
	}
	
	public static double calculateSpectrumEntropy(MsPoint[]spectrum) {
		return calculateSpectrumEntropy(Arrays.asList(spectrum));
	}
	
	/**
	 * @param spectrum
	 * @return pattern recognition entropy
	 * Based on paper
	 * https://www.nature.com/articles/s41592-021-01331-z
	 */
	public static double calculateSpectrumEntropyNatLog(Collection<MsPoint>spectrum) {
		
		double totalIntensity = 
				spectrum.stream().mapToDouble(p -> p.getIntensity()).sum();
		double entropy = 0.0d;
		for(MsPoint p : spectrum) {
			
			if(p.getIntensity() > 0.0d) {
				double norm = p.getIntensity() / totalIntensity;
				entropy += norm * Math.log(norm);
			}
		}	
		return -entropy;
	}
	
	public static double calculateSpectrumEntropyNatLog(MsPoint[]spectrum) {
		return calculateSpectrumEntropyNatLog(Arrays.asList(spectrum));
	}
	
	public static double calculateCleanedSpectrumEntropyNatLog(Collection<MsPoint>spectrum) {
		
		Collection<MsPoint>cleanedSpectrum = 
				MsUtils.averageAndDenoiseMassSpectrum(
						spectrum, 					
						MRC2ToolBoxConfiguration.getSpectrumEntropyMassError(), 
						MRC2ToolBoxConfiguration.getSpectrumEntropyMassErrorType(),
						MRC2ToolBoxConfiguration.getSpectrumEntropyNoiseCutoff());
		
		double totalIntensity = 
				cleanedSpectrum.stream().mapToDouble(p -> p.getIntensity()).sum();
		double entropy = 0.0d;
		for(MsPoint p : cleanedSpectrum) {
			
			if(p.getIntensity() > 0.0d) {
				double norm = p.getIntensity() / totalIntensity;
				entropy += norm * Math.log(norm);
			}
		}	
		return -entropy;
	}
	
	public static Collection<MsPoint>averageTwoSpectraWithInterpolation(
			Collection<MsPoint>spectrumOne, 
			Collection<MsPoint>spectrumTwo, 
			double splitRatio, 
			Double mzBinWidth, 
			MassErrorType errorType){
		
		double multiplier = 1.0 - splitRatio;	
		List<MsPoint> rawInputPoints = spectrumTwo.stream().
				map(p -> new MsPoint(p.getMz(), p.getIntensity() * splitRatio, p.getScanNum())).
				collect(Collectors.toList());
		List<MsPoint> rawInputPointsTwo = spectrumOne.stream().
				map(p -> new MsPoint(p.getMz(), p.getIntensity() * multiplier, p.getScanNum())).
				collect(Collectors.toList());
		rawInputPoints.addAll(rawInputPointsTwo);
		return averageMassSpectrum(rawInputPoints, mzBinWidth, errorType);
	}
	
	public static Collection<MsPoint>averageMassSpectrum(
			Collection<MsPoint>inputPoints, double mzBinWidth, MassErrorType errorType) {
		
		if(inputPoints.isEmpty())
			return inputPoints;
		
		MsPoint[] points = inputPoints.stream().
				sorted(mzSorter).
				toArray(size -> new MsPoint[size]);
		
		ArrayList<MsPointBucket> msBins = new ArrayList<MsPointBucket>();
		MsPointBucket first = new MsPointBucket(points[0], mzBinWidth, errorType);
		msBins.add(first);
		for(int i=1; i<points.length; i++) {
			
			MsPointBucket current = msBins.get(msBins.size()-1);
			if(!current.addPoint(points[i]))
				msBins.add(new MsPointBucket(points[i], mzBinWidth, errorType));
		}
		Collection<MsPoint>avgSpectrum =  msBins.stream().
				map(b -> b.getAveragePoint()).
				map(p -> new MsPoint(p.getMz(), p.getIntensity())).
				sorted(mzSorter).
				collect(Collectors.toList());
		 
		 //	msBins.stream().forEach(b -> b = null); 		 
		 return avgSpectrum;
	}
	
	public static Collection<MsPoint>denoiseAndAverageMassSpectrum(
			Collection<MsPoint>inputPoints, 
			double mzBinWidth, 
			MassErrorType errorType,
			double relIntNoiseCutoff) {
		
		double intensityCutoff = inputPoints.stream().
				mapToDouble(p -> p.getIntensity()).
				max().getAsDouble() * relIntNoiseCutoff;
		Collection<MsPoint>cleanMs = inputPoints.stream().
				filter(p -> p.getIntensity() > intensityCutoff).
				sorted(mzSorter).
				collect(Collectors.toList());
		
		Collection<MsPoint>cleanAvgMs = 
				averageMassSpectrum(cleanMs, mzBinWidth, errorType);		
		
		return cleanAvgMs;
	}
	
	public static Collection<MsPoint>averageAndDenoiseMassSpectrum(
			Collection<MsPoint>inputPoints, 
			double mzBinWidth, 
			MassErrorType errorType,
			double relIntNoiseCutoff) {
		
		if(inputPoints.isEmpty())
			return inputPoints;
		
		Collection<MsPoint>avgMs = 
				averageMassSpectrum(inputPoints, mzBinWidth, errorType);
		
		double intensityCutoff = avgMs.stream().
				mapToDouble(p -> p.getIntensity()).
				max().getAsDouble() * relIntNoiseCutoff;
		Collection<MsPoint>cleanAvgMs = avgMs.stream().
				filter(p -> p.getIntensity() > intensityCutoff).
				sorted(mzSorter).
				collect(Collectors.toList());
		
		return cleanAvgMs;
	}
	
	public static MsPoint getAveragePoint(Collection<MsPoint>inputPoints) {
		
		if(inputPoints.isEmpty())
			return null;
		
		if(inputPoints.size() == 1)
			return inputPoints.iterator().next();
		
		double totalIntensity =  
				inputPoints.stream().mapToDouble(p -> p.getIntensity()).sum();
		double massIntensityProductSum = 
				inputPoints.stream().mapToDouble(p -> p.getMz() * p.getIntensity()).sum();
		double avgMz = massIntensityProductSum / totalIntensity;
		return new MsPoint(avgMz, totalIntensity);
	}
	
	public static void calculateMcMillanMassDefectForSpectrum(MassSpectrum spectrum) {
		
		double mz = spectrum.getPrimaryAdductBasePeakMz();
		if(Math.abs(spectrum.getPrimaryAdduct().getCharge()) > 1) {
			
			double neutralMass= getNeutralMassForAdduct(mz, spectrum.getPrimaryAdduct());
			mz = getAdductMz(neutralMass, 
					AdductManager.getDefaultAdductForPolarity(
							spectrum.getPrimaryAdduct().getPolarity()));
		}
		double mcMillanCutoff = 0.00112 * mz + 0.01953;
		double massDefect = mz % 1;
		double mcMillanCutoffPercentDelta = (massDefect - mcMillanCutoff) / mcMillanCutoff * 100;
		spectrum.setMcMillanCutoff(mcMillanCutoff);
		spectrum.setMcMillanCutoffPercentDelta(mcMillanCutoffPercentDelta);
	}

	public static double getKendrickNominalMass(double mz, KendrickUnits unit) {		
		return Math.round(mz * unit.getMultiplier());
	}
	
	public static double getKendrickMassDefect(double mz, KendrickUnits unit) {		
		return mz * unit.getMultiplier() - Math.round(mz * unit.getMultiplier());
	}
	
	public static double getKendrickMassDefectOld(double mz) {

		double km = mz * 14 / 14.01565;
		double kmd = Math.floor(km) - km;
		return kmd;
	}

	public static double getModifiedKendrickMassDefect(double mz) {

		double km = mz * 14 / 14.01565;
		double kmd = Math.floor(mz) - km;
		return kmd;
	}
	
	public static double getRelativeMassDefectPpm(double mz) {		
		double md = mz - Math.round(mz);
		return md/mz * 1000000.0d;
	}
	
	public static String getSpectrumAsPythonArray(Collection<MsPoint>spectrum) {
		
		if(spectrum == null || spectrum.isEmpty())
			return "";
		
		MsPoint[] spectrumNorm = normalizeAndSortMsPattern(spectrum);
		ArrayList<String>line = new ArrayList<String>();
		for(MsPoint point : spectrumNorm) {
			
			String msPoint = "[" +  MRC2ToolBoxConfiguration.getMzFormat().format(point.getMz())
					+ ", " + MsUtils.pythonIntensityFormat.format(point.getIntensity()) + "]";
			line.add(msPoint);
		}
		String arrayString = "[" + StringUtils.join(line, ", ") + "]";
		return arrayString;
	}
	
	public static Collection<MsPoint>trimSpectrum(
			Collection<MsPoint>inputSpectrum, double center, double width){
		
		Range trimRange = new Range(center - width, center + width);
		return inputSpectrum.stream().
				filter(p -> trimRange.contains(p.getMz())).
				distinct().sorted(mzSorter).
				collect(Collectors.toList());
	}
	
	public static MsPoint getBasePeak(Collection<MsPoint>inputSpectrum) {		
		return inputSpectrum.stream().sorted(reverseIntensitySorter).findFirst().orElse(null);
	}
	
	public static boolean isParentIonMinorIsotope(MsFeature feature, double massAccuracyPpm) {
		
		TandemMassSpectrum msms = feature.getSpectrum().getExperimentalTandemSpectrum();
		if(msms == null)
			return false;
			
		MsPoint precursor = msms.getParent();
		if(precursor == null)
			return false;
		
		Range lookupRange = createPpmMassRange(
						precursor.getMz() - MsUtils.NEUTRON_MASS, massAccuracyPpm);
		double precIntensity = precursor.getIntensity();
		MsPoint lighterIsotope = feature.getSpectrum().getMsPoints().stream().
				filter(p -> lookupRange.contains(p.getMz())).
				filter(p -> (p.getIntensity() > precIntensity)).
				findFirst().orElse(null);
		
		return (lighterIsotope) != null;
	}
	
	public static String getMsOneSpectrumForPrintout(MassSpectrum ms) {
		
		String msString = "";
		if(ms.getAdducts() == null || ms.getAdducts().isEmpty())
			return msString;
		
		for(Adduct adduct : ms.getAdducts()) {
			
			for(MsPoint p : ms.getMsPointsForAdduct(adduct)) {
				msString += adduct.getName() + "\t" 
					+ spectrumMzExportFormat.format(p.getMz()) + "\t" 
					+ spectrumIntensityFormat.format(p.getIntensity()) + "\n";
			}
		}		
		return msString;
	}
	
	public static Map<Adduct, Collection<MsPoint>>scaleAllAdductsToBasePeak(MassSpectrum inputSpectrum){
		
		Map<Adduct, Collection<MsPoint>>adductMsPointsMap = 
				new TreeMap<Adduct, Collection<MsPoint>>();
		double maxIntensity = inputSpectrum.getBasePeak().getIntensity();
		for(Adduct adduct : inputSpectrum.getAdducts()) {
			
			List<MsPoint> normPoints = inputSpectrum.getMsPointsForAdduct(adduct).stream()
				.map(dp -> new MsPoint(dp.getMz(), 
						dp.getIntensity()/maxIntensity * SPECTRUM_NORMALIZATION_BASE_INTENSITY))
				.sorted(mzSorter).collect(Collectors.toList());
			if(!normPoints.isEmpty())
				adductMsPointsMap.put(adduct, normPoints);
		}
		return adductMsPointsMap;
	}
	
	public static MassSpectrum averageMassSpectraByAdduct(
			Collection<MassSpectrum>inputSpectra,
			double mzBinWidth,
			MassErrorType errorType){
		
		MassSpectrum averagedSpectrum = new MassSpectrum();
		Map<Adduct, List<MsPoint>>adductMsPointsMap = new TreeMap<Adduct, List<MsPoint>>();
		for(MassSpectrum ms : inputSpectra) {
			
			Map<Adduct, Collection<MsPoint>>normalizedToBase = scaleAllAdductsToBasePeak(ms);
			for(Entry<Adduct, Collection<MsPoint>>normEntry : normalizedToBase.entrySet()) {
				
				if(!adductMsPointsMap.containsKey(normEntry.getKey()))
					adductMsPointsMap.put(normEntry.getKey(), new ArrayList<MsPoint>());
				
				adductMsPointsMap.get(normEntry.getKey()).addAll(normEntry.getValue());
			}			
		}
		for(Entry<Adduct, List<MsPoint>>adductEntry : adductMsPointsMap.entrySet()) {
			Collection<MsPoint> averageAdductSpectrum = 
					averageMassSpectrum(adductEntry.getValue(), mzBinWidth, errorType);
			
			if(averageAdductSpectrum != null && !averageAdductSpectrum.isEmpty()) {
				
				averagedSpectrum.addSpectrumForAdduct(
						adductEntry.getKey(), averageAdductSpectrum);
			}
		}
		return averagedSpectrum;
	}
}


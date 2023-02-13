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

package edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.derepl;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import edu.umich.med.mrc2.datoolbox.data.Adduct;
import edu.umich.med.mrc2.datoolbox.data.AdductExchange;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureCluster;
import edu.umich.med.mrc2.datoolbox.data.MsPoint;
import edu.umich.med.mrc2.datoolbox.data.enums.ModificationType;
import edu.umich.med.mrc2.datoolbox.data.enums.Polarity;
import edu.umich.med.mrc2.datoolbox.data.enums.PrimaryFeatureSelectionType;
import edu.umich.med.mrc2.datoolbox.main.AdductManager;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.utils.ClusterUtils;
import edu.umich.med.mrc2.datoolbox.utils.MsUtils;
import edu.umich.med.mrc2.datoolbox.utils.Range;

public class AdductAssignmentTask extends AbstractTask {

	private MsFeatureCluster activeCluster;
	private HashSet<MsFeatureCluster> clusterSet;
	private HashSet<HashMap<MsFeature, Adduct>> modificationMap;
	private List<MsFeature> clusterFeatures;
	private double massAccuracy;
	private Map<Adduct, Range> modifiedMassRanges;
	private MsFeature primaryFeature;
	private Adduct primaryFeatureForm;
	private DataAnalysisProject currentExperiment;
	private Polarity polarity;
	private Collection<Adduct> adducts;
	private Collection<Adduct> repeats;
	private Collection<Adduct> losses;
	private Collection<Adduct> pureAdducts;
	private Collection<AdductExchange> exchanges;

	private PrimaryFeatureSelectionType pfSelectionType;
	private int maxClusterSize = 2000;
	private boolean useSubmittedFeature;

	public AdductAssignmentTask(
			MsFeatureCluster currentCluster,
			MsFeature feature,
			Adduct chmod,
			double massError) {

		clusterSet = new HashSet<MsFeatureCluster>();
		clusterSet.add(currentCluster);
		primaryFeature = feature;
		primaryFeatureForm = chmod;
		maxClusterSize = currentCluster.getFeatures().size() + 1;
		pfSelectionType = PrimaryFeatureSelectionType.AS_MARKED;
		useSubmittedFeature = true;

		modificationMap = new HashSet<HashMap<MsFeature, Adduct>>();
		modifiedMassRanges = new HashMap<Adduct, Range>();

		currentExperiment = MRC2ToolBoxCore.getActiveMetabolomicsExperiment();
		polarity = currentExperiment.getActiveDataPipeline().
				getAcquisitionMethod().getPolarity();
		massAccuracy = massError;
		
		initAdducts(false, 0, 0);
		
//		adducts = ChemicalModificationsManager.getChargedModifications(polarity);
//		pureAdducts = ChemicalModificationsManager.getAdducts(polarity);
//		repeats = ChemicalModificationsManager.getRepeats();
//		losses = ChemicalModificationsManager.getLosses();
//		exchanges = ChemicalModificationsManager.getExchanges(polarity);

		taskDescription = "Finding possible adduct combinations ...";
	}

	public AdductAssignmentTask(
			MsFeatureCluster currentCluster,
			MsFeature feature,
			Adduct chmod,
			double massError,
			boolean generateAdducts,
			int maxAdductCharge,
			int maxOligomer) {

		clusterSet = new HashSet<MsFeatureCluster>();
		clusterSet.add(currentCluster);
		primaryFeature = feature;
		primaryFeatureForm = chmod;
		maxClusterSize = currentCluster.getFeatures().size() + 1;
		pfSelectionType = PrimaryFeatureSelectionType.AS_MARKED;
		useSubmittedFeature = true;

		modificationMap = new HashSet<HashMap<MsFeature, Adduct>>();
		modifiedMassRanges = new HashMap<Adduct, Range>();

		currentExperiment = MRC2ToolBoxCore.getActiveMetabolomicsExperiment();
		polarity = currentExperiment.getActiveDataPipeline().
				getAcquisitionMethod().getPolarity();
		massAccuracy = massError;
		
		initAdducts(generateAdducts, maxAdductCharge, maxOligomer);
	}

	public AdductAssignmentTask(
			PrimaryFeatureSelectionType type,
			double massError,
			int maxSize) {

		pfSelectionType = type;
		massAccuracy = massError;
		maxClusterSize = maxSize;
		useSubmittedFeature = false;

		primaryFeature = null;
		primaryFeatureForm = null;

		modificationMap = new HashSet<HashMap<MsFeature, Adduct>>();
		modifiedMassRanges = new HashMap<Adduct, Range>();

		currentExperiment = MRC2ToolBoxCore.getActiveMetabolomicsExperiment();
		polarity = currentExperiment.getActiveDataPipeline().
				getAcquisitionMethod().getPolarity();

		clusterSet = (HashSet<MsFeatureCluster>) currentExperiment
				.getMsFeatureClustersForDataPipeline(currentExperiment.getActiveDataPipeline());

		initAdducts(false, 0, 0);
		
//		adducts = ChemicalModificationsManager.getChargedModifications(polarity);
//		pureAdducts = ChemicalModificationsManager.getAdducts(polarity);
//		repeats = ChemicalModificationsManager.getRepeats();
//		losses = ChemicalModificationsManager.getLosses();
//		exchanges = ChemicalModificationsManager.getExchanges(polarity);
	}

	public AdductAssignmentTask(
			PrimaryFeatureSelectionType type,
			double massError,
			int maxClusterSize2,
			boolean generateAdducts,
			int maxAdductCharge,
			int maxOligomer) {

		pfSelectionType = type;
		massAccuracy = massError;
		maxClusterSize = maxClusterSize2;
		useSubmittedFeature = false;

		primaryFeature = null;
		primaryFeatureForm = null;

		modificationMap = new HashSet<HashMap<MsFeature, Adduct>>();
		modifiedMassRanges = new HashMap<Adduct, Range>();

		currentExperiment = MRC2ToolBoxCore.getActiveMetabolomicsExperiment();
		polarity = currentExperiment.getActiveDataPipeline().
				getAcquisitionMethod().getPolarity();

		clusterSet = (HashSet<MsFeatureCluster>) currentExperiment
				.getMsFeatureClustersForDataPipeline(currentExperiment.getActiveDataPipeline());

		initAdducts(generateAdducts, maxAdductCharge, maxOligomer);
		
//		if (generateAdducts) {
//
//			adducts = ChemicalModificationsManager
//					.createAdductSetFromElementaryAdducts(polarity, maxAdductCharge, maxOligomer);
//			pureAdducts = adducts;
//		} else {
//
//			adducts = ChemicalModificationsManager.getChargedModifications(polarity);
//			pureAdducts = ChemicalModificationsManager.getAdducts(polarity);
//		}
//		repeats = ChemicalModificationsManager.getRepeats();
//		losses = ChemicalModificationsManager.getLosses();
//		exchanges = ChemicalModificationsManager.getExchanges(polarity);
	}
	
	private void initAdducts(boolean generateAdducts, int maxAdductCharge, int maxOligomer) {
		
//		TODO ? 
//		if (generateAdducts) {			
//			adducts = ChemicalModificationsManager.createAdductSetFromElementaryAdducts(
//					polarity, maxAdductCharge, maxOligomer);
//			pureAdducts = adducts;
//		} else {
			adducts = AdductManager.getAdductsForPolarity(polarity);
			pureAdducts = AdductManager.getAdductsForTypeAndPolarity(
					ModificationType.ADDUCT, polarity);
//		}
		repeats = AdductManager.getNeutralAdducts();
		losses = AdductManager.getNeutralLosses();
		exchanges = AdductManager.getAdductExchangeListForPolarity(polarity);
	}

	private void acceptInterpretationResults() {

		processed = 0;

		for (MsFeatureCluster cluster : clusterSet) {

			if (cluster.getAnnotationMap() != null) {

				for (MsFeature f : cluster.getFeatures()){

					if(cluster.getAnnotationMap().get(f) != null){

						if(!cluster.getAnnotationMap().get(f).isEmpty()){

							f.setDefaultChemicalModification(cluster.getAnnotationMap().get(f).iterator().next());
							f.setSuggestedModification(f.getDefaultChemicalModification());
						}
					}
				}
			}
			processed++;
		}
	}

	private void acceptSuggestedResults() {

		processed = 0;

		for (MsFeatureCluster cluster : clusterSet) {

			if (cluster.getAnnotationMap() != null) {

				for (MsFeature f : cluster.getFeatures()){

					if(cluster.getAnnotationMap().get(f) != null){

						if(!cluster.getAnnotationMap().get(f).isEmpty())
							f.setSuggestedModification(cluster.getAnnotationMap().get(f).iterator().next());
					}

				}
			}
			processed++;
		}
	}

	@Override
	public Task cloneTask() {

		return null;
	}

	//	TODO rewrite based on new adduct system
	// Allow two different kinds of repeats with up to three total units
	private HashSet<Adduct> findComplexAnnotationsForAdduct(MsFeature unknown, MsFeature known, Adduct knownMod) {

		HashSet<Adduct> modifications = new HashSet<Adduct>();
//		int maxTotalRepeats = 3;
//		double mz = unknown.getMonoisotopicMz();
//		Map<Adduct, Range> adductMassRanges = MsUtils.getModifiedMassRanges(
//				primaryFeature.getMonoisotopicMz(), primaryFeatureForm, pureAdducts, massAccuracy);
//
//		MassSpectrum spectrum = unknown.getSpectrum();
//
//		if(spectrum != null) {
//
//			MsPoint[] pattern = spectrum.getMsForAdduct(spectrum.getPrimaryAdduct());
//			boolean isHalogenated = (MsUtils.isBrominated(pattern, massAccuracy) || MsUtils.isChlorinated(pattern, massAccuracy));
//
//			Adduct[] repeatArray = repeats.toArray(new Adduct[repeats.size()]);
//
//			// Iterate through all posible adducts
//			for (Entry<Adduct, Range> modEntry : adductMassRanges.entrySet()) {
//
//				if (modEntry.getKey().getCharge() == unknown.getCharge()) {
//
//					for(int i=0; i<repeatArray.length; i++){
//
//						if(i == 11 && modEntry.getValue().getAverage() > 802.7 && modEntry.getValue().getAverage() < 802.9d)
//							System.out.println();
//
//						for(int j=i+1; j<repeatArray.length && j != i; j++){
//
//							for (int repNumber = 1; repNumber <= maxTotalRepeats; repNumber++) {
//
//								for (int repNumberTwo = 0; repNumberTwo <= (maxTotalRepeats - repNumber); repNumberTwo++) {
//
//									double correction = repeatArray[i].getMassCorrection() * repNumber + repeatArray[j].getMassCorrection() * repNumberTwo;
//									Range correctedRange = new Range(modEntry.getValue().getMin() + correction, modEntry.getValue().getMax() + correction);
//
//									if (correctedRange.contains(mz)) {
//
//										Adduct complexMod = ChemicalModificationsManager
//												.createComplexModification(modEntry.getKey(), repeatArray[i], repNumber, repeatArray[j],
//														repNumberTwo);
//
//										if (complexMod.isHalogenated() && isHalogenated)
//											modifications.add(complexMod);
//
//										if (!complexMod.isHalogenated() && !isHalogenated)
//											modifications.add(complexMod);
//									}
//								}
//							}
//						}
//					}
//					// Iterate through losses
//					for (Adduct loss : losses) {
//
//						Range correctedRange = new Range(modEntry.getValue().getMin() + loss.getMassCorrection(),
//								modEntry.getValue().getMax() + loss.getMassCorrection());
//
//						if (correctedRange.contains(mz)) {
//
//							Adduct complexMod = ChemicalModificationsManager
//									.createComplexModification(modEntry.getKey(), loss, 1, null, 0);
//							modifications.add(complexMod);
//						}
//					}
//				}
//			}
//		}
		return modifications;
	}

	public HashSet<MsFeatureCluster> getClusterSet() {
		return clusterSet;
	}

	public HashSet<HashMap<MsFeature, Adduct>> getModificationMap() {
		return modificationMap;
	}

	private void initPrimaryValues() {

		if (primaryFeature != null) {

			if(primaryFeature.getSpectrum() != null) {

				if (primaryFeature.getSpectrum().getAdducts().size() > 1) {

					// If more than one adduct - assignment is correct
					primaryFeatureForm = primaryFeature.getSpectrum().getPrimaryAdduct();
					return;
				}
			}
		}
		if (polarity.equals(Polarity.Positive)) {

			if (primaryFeature.getCharge() == 1) {

				primaryFeatureForm = AdductManager.getDefaultAdductForCharge(1);

				// Check if common adduct exchanges alter primary adduct
				// assignment
				for (AdductExchange ex : exchanges) {

					if (ex.getComingAdduct().getCharge() == 1) {

						Range exchangeRange = MsUtils.createMassRangeWithReference(Math.abs(ex.getMassDifference()),
								primaryFeature.getMonoisotopicMz(), massAccuracy);

						for (MsFeature f : activeCluster.getFeatures()) {

							if (f.getCharge() == 1) {

								double delta = primaryFeature.getMonoisotopicMz()
										- f.getMonoisotopicMz();

								if (exchangeRange.contains(Math.abs(delta))) {

									if (delta * ex.getMassDifference() > 0)
										primaryFeatureForm = ex.getComingAdduct();

									if (delta * ex.getMassDifference() < 0)
										primaryFeatureForm = ex.getLeavingAdduct();

									break;
								}
							}
						}
					}
				}
			}
			// TODO handle exchanges ?
			if (primaryFeature.getCharge() == 2)
				primaryFeatureForm = AdductManager.getDefaultAdductForCharge(2);

			return;
		}
		if (polarity.equals(Polarity.Negative)) {

			if (primaryFeature.getCharge() == -1) {

				Adduct pa = null;
				MsPoint[] pattern = null;

				if(primaryFeature.getSpectrum() != null) {

					pa = primaryFeature.getSpectrum().getPrimaryAdduct();
					pattern = primaryFeature.getSpectrum().getMsForAdduct(pa);
				}
				if (MsUtils.isChlorinated(pattern, massAccuracy))
					primaryFeatureForm = AdductManager.getAdductByName("M+Cl");
				else
					primaryFeatureForm = AdductManager.getDefaultAdductForCharge(-1);

				// Check if common adduct exchanges alter primary adduct
				// assignment
				for (AdductExchange ex : exchanges) {

					if (ex.getComingAdduct().getCharge() == -1) {

						Range exchangeRange = MsUtils.createMassRangeWithReference(Math.abs(ex.getMassDifference()),
								primaryFeature.getMonoisotopicMz(), massAccuracy);

						for (MsFeature f : activeCluster.getFeatures()) {

							if (f.getCharge() == -1) {

								double delta = primaryFeature.getMonoisotopicMz()
										- f.getMonoisotopicMz();

								if (exchangeRange.contains(Math.abs(delta))) {

									if (delta * ex.getMassDifference() > 0)
										primaryFeatureForm = ex.getComingAdduct();

									if (delta * ex.getMassDifference() < 0)
										primaryFeatureForm = ex.getLeavingAdduct();

									break;
								}
							}
						}
					}
				}
			}
			if (primaryFeature.getCharge() == -2)
				primaryFeatureForm = AdductManager.getDefaultAdductForCharge(-2);

			return;
		}
	}

	@Override
	public void run() {

		setStatus(TaskStatus.PROCESSING);

		total = clusterSet.size();
		processed = 0;

		for (MsFeatureCluster cluster : clusterSet) {

			activeCluster = cluster;

			if (activeCluster.getFeatures().size() <= maxClusterSize && !activeCluster.isLocked()) {

				Map<MsFeature, Set<Adduct>> annotationMap = new HashMap<MsFeature, Set<Adduct>>();
				Map<MsFeature, Set<Adduct>> complexAnnotationMap = new HashMap<MsFeature, Set<Adduct>>();

				// Reset existing assignment
				cluster.setAnnotationMap(null);

				for (MsFeature f : activeCluster.getFeatures()){

					f.setDefaultChemicalModification(null);
					f.setSuggestedModification(null);
				}
				clusterFeatures = activeCluster.getFeatures();
				// primaryFeature = null;

				if (!useSubmittedFeature) {

					if (pfSelectionType.equals(PrimaryFeatureSelectionType.AUTOMATIC))
						primaryFeature = ClusterUtils.getMostIntensiveFeature(cluster);

					if (pfSelectionType.equals(PrimaryFeatureSelectionType.AS_MARKED))
						primaryFeature = cluster.getPrimaryFeature();
				}
				if (primaryFeature == null)
					primaryFeature = ClusterUtils.getMostIntensiveFeature(cluster);

				if (primaryFeatureForm == null || pfSelectionType.equals(PrimaryFeatureSelectionType.AUTOMATIC))
					initPrimaryValues();

				Set<Adduct> set = new TreeSet<Adduct>();
				set.add(primaryFeatureForm);
				annotationMap.put(primaryFeature, set);

				// Create expected ranges for adducts
				modifiedMassRanges = MsUtils.getModifiedMassRanges(primaryFeature.getMonoisotopicMz(),
						primaryFeatureForm, adducts, massAccuracy);

				// Find adducts
				for (MsFeature f : clusterFeatures) {

					if(!f.equals(primaryFeature)){

						double mz = f.getMonoisotopicMz();
						MsPoint[] pattern = null;

						if(f.getSpectrum() != null)
							pattern = f.getSpectrum().getMsForAdduct(f.getSpectrum().getPrimaryAdduct());

						boolean isHalogenated = (MsUtils.isBrominated(pattern, massAccuracy) || MsUtils.isChlorinated(pattern, massAccuracy));

						annotationMap.put(f, new TreeSet<Adduct>());

						for (Entry<Adduct, Range> modEntry : modifiedMassRanges.entrySet()) {

							if (modEntry.getValue().contains(mz) && f.getCharge() == modEntry.getKey().getCharge()) {

								if (modEntry.getKey().isHalogenated() && isHalogenated)
									annotationMap.get(f).add(modEntry.getKey());

								if (!modEntry.getKey().isHalogenated() && !isHalogenated)
									annotationMap.get(f).add(modEntry.getKey());
							}
						}
					}
				}
				// Go through unexplained features
				for (MsFeature unknown : clusterFeatures) {

					if(!unknown.equals(primaryFeature)){

						complexAnnotationMap.put(unknown, new TreeSet<Adduct>());

						Set<Adduct> complexAnnotations = findComplexAnnotationsForAdduct(unknown,
								primaryFeature, primaryFeatureForm);

						if (!complexAnnotations.isEmpty())
							complexAnnotationMap.get(unknown).addAll(complexAnnotations);
					}
				}
				for (Entry<MsFeature, Set<Adduct>> entry : complexAnnotationMap.entrySet()){

					if(!entry.getValue().isEmpty())
						annotationMap.get(entry.getKey()).addAll(entry.getValue());
				}
				// TODO Find anion/cation radicals not working??
				modifiedMassRanges = MsUtils.getNeutralLossRanges(primaryFeature.getMonoisotopicMz(), primaryFeatureForm, repeats, massAccuracy);

				for (MsFeature f : clusterFeatures) {

					if(!f.equals(primaryFeature)){

						MsPoint[] pattern = null;

						if(f.getSpectrum() != null)
							pattern = f.getSpectrum().getMsForAdduct(f.getSpectrum().getPrimaryAdduct());

						boolean isHalogenated = (MsUtils.isBrominated(pattern, massAccuracy) || MsUtils.isChlorinated(pattern, massAccuracy));

						for (Entry<Adduct, Range> modEntry : modifiedMassRanges.entrySet()) {

							if (modEntry.getValue().contains(f.getMonoisotopicMz())) {

								if (modEntry.getKey().isHalogenated() && isHalogenated)
									annotationMap.get(f).add(modEntry.getKey());

								if (!modEntry.getKey().isHalogenated() && isHalogenated)
									annotationMap.get(f).add(modEntry.getKey());
							}
						}
					}
				}
				cluster.setAnnotationMap(annotationMap);
			}
			processed++;
		}
		if (clusterSet.size() > 1)
			acceptInterpretationResults();
		else
			acceptSuggestedResults();

		setStatus(TaskStatus.FINISHED);
	}
}

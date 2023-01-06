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

package edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.idt;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import edu.umich.med.mrc2.datoolbox.data.MSFeatureIdentificationLevel;
import edu.umich.med.mrc2.datoolbox.data.MSFeatureInfoBundle;
import edu.umich.med.mrc2.datoolbox.data.MinimalMSOneFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureIdentity;
import edu.umich.med.mrc2.datoolbox.data.MsMsLibraryFeature;
import edu.umich.med.mrc2.datoolbox.data.MsPoint;
import edu.umich.med.mrc2.datoolbox.data.ReferenceMsMsLibrary;
import edu.umich.med.mrc2.datoolbox.data.ReferenceMsMsLibraryMatch;
import edu.umich.med.mrc2.datoolbox.data.TandemMassSpectrum;
import edu.umich.med.mrc2.datoolbox.data.compare.MsFeatureInfoBundleClusterComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.enums.ClassyFireClassificationLevels;
import edu.umich.med.mrc2.datoolbox.data.enums.FeatureIDSubset;
import edu.umich.med.mrc2.datoolbox.data.enums.IDTrackerFeatureIdentificationProperties;
import edu.umich.med.mrc2.datoolbox.data.enums.IDTrackerMSMSClusterProperties;
import edu.umich.med.mrc2.datoolbox.data.enums.IDTrackerMsFeatureProperties;
import edu.umich.med.mrc2.datoolbox.data.enums.MSMSMatchType;
import edu.umich.med.mrc2.datoolbox.data.enums.MSMSScoringParameter;
import edu.umich.med.mrc2.datoolbox.data.enums.MassErrorType;
import edu.umich.med.mrc2.datoolbox.data.enums.RefMetClassificationLevels;
import edu.umich.med.mrc2.datoolbox.data.lims.Injection;
import edu.umich.med.mrc2.datoolbox.data.msclust.MSMSClusterDataSet;
import edu.umich.med.mrc2.datoolbox.data.msclust.MsFeatureInfoBundleCluster;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCash;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.utils.MsUtils;

public class IDTrackerMSMSClusterDataExportTask extends IDTrackerFeatureExportTask {
	
	private MSMSClusterDataSet msmsClusterDataSet;
	private Collection<IDTrackerMSMSClusterProperties> msmsClusterPropertyList;	
	private boolean exportIndividualFeatureData;

	public IDTrackerMSMSClusterDataExportTask(
			MSMSClusterDataSet msmsClusterDataSet,
			Collection<IDTrackerMSMSClusterProperties> msmsClusterPropertyList,
			Collection<IDTrackerMsFeatureProperties> msmsFeaturePropertyList, 
			Collection<IDTrackerFeatureIdentificationProperties> identificationDetailsList,
			boolean exportIndividualFeatureData, 
			MSMSScoringParameter scoringParam, 
			double minimalMSMSScore, 
			FeatureIDSubset idSubset, 
			Collection<MSMSMatchType> msmsSearchTypes, 
			boolean excludeIfNoIdsLeft, 
			File outputFile) {
		super();
		this.msmsClusterDataSet = msmsClusterDataSet;
		this.msmsClusterPropertyList = msmsClusterPropertyList;
		this.msmsFeaturePropertyList = msmsFeaturePropertyList;
		this.identificationDetailsList = identificationDetailsList;	
		this.exportIndividualFeatureData = exportIndividualFeatureData; 
		this.scoringParam = scoringParam; 
		this. minimalMSMSScore = minimalMSMSScore; 
		this.idSubset = idSubset; 
		this.msmsSearchTypes = msmsSearchTypes; 
		this.excludeIfNoIdsLeft = excludeIfNoIdsLeft;		
		this.outputFile = outputFile;
	}

	@Override
	public void run() {
		
		setStatus(TaskStatus.PROCESSING);
		
		Set<MSFeatureInfoBundle> bundles = msmsClusterDataSet.getClusters().stream().
				flatMap(c -> c.getComponents().stream()).collect(Collectors.toSet());
		
		if(msmsClusterPropertyList.contains(IDTrackerMSMSClusterProperties.PRIMARY_ID_RAW_DATA_FILE)) {
			
			try {
				getInjections(bundles);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}	
		accessions = getAccessions(msmsClusterDataSet.getClusters());
		
		if(identificationDetailsList.contains(IDTrackerFeatureIdentificationProperties.SYSTEMATIC_NAME)) {
			try {
				getSystematicNames();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(identificationDetailsList.contains(IDTrackerFeatureIdentificationProperties.REFMET_NAME)) {
			try {
				getRefMetNames();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}		
		if(identificationDetailsList.contains(IDTrackerFeatureIdentificationProperties.REFMET_CLASSIFICATION)) {
			try {
				getRefMetClassifications();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(identificationDetailsList.contains(IDTrackerFeatureIdentificationProperties.CLASSYFIRE_CLASSIFICATION)) {
			try {
				getClassyFireClassifications();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try {
			writeExportFile();
			setStatus(TaskStatus.FINISHED);
		}
		catch (Exception e1) {
			setStatus(TaskStatus.ERROR);
			e1.printStackTrace();
		}
	}

	private Collection<String>getAccessions(Collection<MsFeatureInfoBundleCluster> msmsClusters){
		
		Collection<String>accessions = new ArrayList<String>();
		if(exportIndividualFeatureData) {
			
			accessions = msmsClusters.stream().
					flatMap(c -> c.getComponents().stream()).
					flatMap(b -> b.getMsFeature().getIdentifications().stream()).				
					filter(i -> Objects.nonNull(i.getCompoundIdentity())).
					map(i -> i.getCompoundIdentity().getPrimaryDatabaseId()).
					distinct().sorted().collect(Collectors.toList());
		}
		else {
			accessions = msmsClusters.stream().
					filter(f -> Objects.nonNull(f.getPrimaryIdentity())).
					filter(f -> Objects.nonNull(f.getPrimaryIdentity().getCompoundIdentity())).
					map(f -> f.getPrimaryIdentity().
							getCompoundIdentity().getPrimaryDatabaseId()).
					distinct().sorted().collect(Collectors.toList());
		}
		return accessions;
	}
	
	private void writeExportFile() throws IOException {

		taskDescription = "Wtiting output";
		total = msmsClusterDataSet.getClusters().size();
		processed = 0;
		final Writer writer = new BufferedWriter(
				new FileWriter(outputFile, StandardCharsets.UTF_8));
		
		//	Header
		ArrayList<String>header = new ArrayList<String>();
		msmsClusterPropertyList.stream().forEach(v -> header.add(v.getName()));
		
		// Add Identification fields
		for(IDTrackerFeatureIdentificationProperties idField : identificationDetailsList) {
			
			if(idField.equals(IDTrackerFeatureIdentificationProperties.REFMET_CLASSIFICATION)) {
				
				for(RefMetClassificationLevels rmLevel : RefMetClassificationLevels.values())
					header.add(rmLevel.getName());			
			}
			else if(idField.equals(IDTrackerFeatureIdentificationProperties.CLASSYFIRE_CLASSIFICATION)) {
				
				for(ClassyFireClassificationLevels cfLevel : ClassyFireClassificationLevels.values())
					header.add(cfLevel.getName());	
			}
			else {
				header.add(idField.getName());
			}		
		}		
		writer.append(StringUtils.join(header, columnSeparator));
		writer.append(lineSeparator);
		
		List<MsFeatureInfoBundleCluster> toExport = msmsClusterDataSet.getClusters().stream().
				sorted(new MsFeatureInfoBundleClusterComparator(SortProperty.RT)).
				collect(Collectors.toList());
		ArrayList<String>line;
		for(MsFeatureInfoBundleCluster msmsCluster : toExport) {

			line = new ArrayList<String>();
			for(IDTrackerMSMSClusterProperties property : msmsClusterPropertyList)
				line.add(getClusterProperty(msmsCluster, property));
			
			String accession = null;
			if(msmsCluster.getPrimaryIdentity() != null 
					&& msmsCluster.getPrimaryIdentity().getCompoundIdentity() != null)
				accession = msmsCluster.getPrimaryIdentity().getCompoundIdentity().getPrimaryDatabaseId();
			
			for(IDTrackerFeatureIdentificationProperties property : identificationDetailsList) {
				
				if(property.equals(IDTrackerFeatureIdentificationProperties.REFMET_CLASSIFICATION)) {

					if(accession != null) {
						Map<RefMetClassificationLevels,String> rmClassMap = refMetClassifications.get(accession);
						for(RefMetClassificationLevels rmLevel : RefMetClassificationLevels.values())
							line.add(rmClassMap.get(rmLevel));
					}
					else {
						for(RefMetClassificationLevels rmLevel : RefMetClassificationLevels.values())
							line.add("");
					}
				}
				else if(property.equals(IDTrackerFeatureIdentificationProperties.CLASSYFIRE_CLASSIFICATION)) {
					
					if(accession != null) {
						Map<ClassyFireClassificationLevels,String> cfClassMap = classyFireClassifications.get(accession);
						if(cfClassMap != null) {
							
							for(ClassyFireClassificationLevels cfLevel : ClassyFireClassificationLevels.values())
								if(cfLevel != null)
									line.add(cfClassMap.get(cfLevel));
								else
									line.add("");
						}
						else {
							for(ClassyFireClassificationLevels cfLevel : ClassyFireClassificationLevels.values())
								line.add("");
						}
					}
					else {
						for(ClassyFireClassificationLevels cfLevel : ClassyFireClassificationLevels.values())
							line.add("");
					}
				}
				else {
					line.add(getFeatureIdentificationProperty(msmsCluster, property));
				}
			}
			writer.append(StringUtils.join(line, columnSeparator));
			writer.append(lineSeparator);
			processed++;
		}
		writer.flush();
		writer.close();
	}
	
	private String getClusterProperty(
			MsFeatureInfoBundleCluster cluster, 
			IDTrackerMSMSClusterProperties property) {
		
		MinimalMSOneFeature lookupFeature = cluster.getLookupFeature();

		if(property.equals(IDTrackerMSMSClusterProperties.CLUSTER_ID))
			return cluster.getId();
		
		if(property.equals(IDTrackerMSMSClusterProperties.MEDIAN_RETENTION_TIME))
			return rtFormat.format(cluster.getRt());
		
		if(property.equals(IDTrackerMSMSClusterProperties.MEDIAN_MZ))
			return mzFormat.format(cluster.getMz());
		
		if(property.equals(IDTrackerMSMSClusterProperties.LOOKUP_FEATURE_MZ)) {
			
			if(lookupFeature != null)
				return mzFormat.format(lookupFeature.getMz());
			else
				return "";			
		}
		if(property.equals(IDTrackerMSMSClusterProperties.LOOKUP_FEATURE_RT)) {
			
			if(lookupFeature != null)
				return rtFormat.format(lookupFeature.getRt());
			else
				return "";			
		}
		if(property.equals(IDTrackerMSMSClusterProperties.LOOKUP_FEATURE_NAME)) {
			
			if(lookupFeature != null)
				return lookupFeature.getName();
			else
				return "";			
		}
		if(property.equals(IDTrackerMSMSClusterProperties.MZ_ERROR_PPM)) {
			
			if(lookupFeature != null) {				
				double mzError = ((cluster.getMz() - lookupFeature.getMz()) / lookupFeature.getMz()) * 1000000.0d;
				return  ppmFormat.format(mzError);
			}
			else
				return "";			
		}
		if(property.equals(IDTrackerMSMSClusterProperties.RT_ERROR)) {
			
			if(lookupFeature != null)
				return rtFormat.format(cluster.getRt() - lookupFeature.getRt());
			else
				return "";			
		}
		if(property.equals(IDTrackerMSMSClusterProperties.RANK)) {
			
			if(lookupFeature != null)
				return entropyFormat.format(lookupFeature.getRank());
			else
				return "";			
		}
		if(property.equals(IDTrackerMSMSClusterProperties.PRIMARY_ID_RAW_DATA_FILE)) {
			
			MSFeatureInfoBundle bundle = cluster.getMSFeatureInfoBundleForPrimaryId();
			if(bundle == null)
				bundle = cluster.getComponents().iterator().next();
			
			String injId = bundle.getInjectionId();
			Injection inj = injections.stream().
					filter(i -> i.getId().equals(injId)).
					findFirst().orElse(null);
			if(inj != null)
				return inj.getDataFileName();
			else
				return bundle.getDataFile().getName();
		}
		return "";		
	}

	private String getFeatureIdentificationProperty(
			MsFeatureInfoBundleCluster msmsCluster,
			IDTrackerFeatureIdentificationProperties property) {

		MsFeatureIdentity id = msmsCluster.getPrimaryIdentity();
		if(id == null)
			return "";
		
		String compoundName = "";
		String idLevelName = "";
		MSFeatureIdentificationLevel idLevel = null;
		if(id != null) {
			compoundName = id.getIdentityName();
			if(id.getCompoundIdentity() != null)
				compoundName = id.getName();
			
			if(compoundName == null)
				compoundName = "";
			
			idLevel = id.getIdentificationLevel();
			if(idLevel != null)
				idLevelName = idLevel.getName();
		}
		boolean hasAnnotations = !msmsCluster.hasAnnotations();
		boolean hasFollowup = !msmsCluster.hasIdFollowupSteps();		
		double deltaMz = MsUtils.getMassErrorForIdentity(
				msmsCluster.getMz(), id, MassErrorType.ppm);

		if(property.equals(IDTrackerFeatureIdentificationProperties.COMPOUND_NAME))
			return compoundName;
				 
		if(id.getCompoundIdentity() != null) {
			
			if(id.getCompoundIdentity().getPrimaryDatabase() != null) {
				
				if(property.equals(IDTrackerFeatureIdentificationProperties.DATABASE_ID))
					return id.getCompoundIdentity().getPrimaryDatabaseId();
					
				if(property.equals(IDTrackerFeatureIdentificationProperties.SOURCE_DATABASE))
					return id.getCompoundIdentity().getPrimaryDatabase().getName();	
				
				if(property.equals(IDTrackerFeatureIdentificationProperties.FORMULA))
					return id.getCompoundIdentity().getFormula();	
				
				if(property.equals(IDTrackerFeatureIdentificationProperties.INCHI_KEY))
					return id.getCompoundIdentity().getInChiKey();	
				
				if(property.equals(IDTrackerFeatureIdentificationProperties.SMILES))
					return id.getCompoundIdentity().getSmiles();
								
				if(property.equals(IDTrackerFeatureIdentificationProperties.REFMET_NAME))
					return refMetNames.get(id.getCompoundIdentity().getPrimaryDatabaseId());
								
				if(property.equals(IDTrackerFeatureIdentificationProperties.SYSTEMATIC_NAME))
					return systematicNames.get(id.getCompoundIdentity().getPrimaryDatabaseId());			
			}
		}
		if(property.equals(IDTrackerFeatureIdentificationProperties.ID_SOURCE))
			return id.getIdSource().getName();
		
		if(property.equals(IDTrackerFeatureIdentificationProperties.ANNOTATIONS) && hasAnnotations)
			return trueMarker;
		
		if(property.equals(IDTrackerFeatureIdentificationProperties.FOLLOWUPS) && hasFollowup)
			return trueMarker;
		
		if(property.equals(IDTrackerFeatureIdentificationProperties.MRC2_ID_LEVEL))
			return idLevelName;
		
		if(property.equals(IDTrackerFeatureIdentificationProperties.ID_SCORE)) {
			
			if(id.getScore() > 0.0d)
				return entropyFormat.format(id.getScore());
		}
		if(property.equals(IDTrackerFeatureIdentificationProperties.MSMS_ENTROPY_SCORE)) {
			
			if(id.getEntropyBasedScore() > 0.0d)
				return entropyFormat.format(id.getEntropyBasedScore());
		}		
		if(property.equals(IDTrackerFeatureIdentificationProperties.MASS_ERROR) && deltaMz != 0.0d)
			return ppmFormat.format(deltaMz);

		if(property.equals(IDTrackerFeatureIdentificationProperties.RETENTION_ERROR)) {
			Double deltaRt = calculateRetentionShift(id, msmsCluster.getRt());
			if(deltaRt == null)
				return "";
			else
				return rtFormat.format(deltaRt);
		}
		double parentMz = 0.0d;
		String collisionEnergyValue = null;
		boolean isHybrid = false;
		boolean isInSource = false;
		double forwardScore = 0.0d;
		double reverseScore = 0.0d;
		double probability = 0.0d;
		double dotProduct = 0.0d;		
		double revDotProduct = 0.0d;
		double hybDotProduct = 0.0d;
		double hybScore = 0.0d;
		double hybDmz = 0.0d;
		double msmsEntropy = 0.0d;			
		double qValue = 0.0d;
		double posteriorProbability = 0.0d;
		double percolatorScore = 0.0d;
		String featureSpectrumArray = "";
		String libraryHitSpectrumArray = "";		
		double libraryPrecursorDeltaMz = 0.0d;
		double neutralMassDeltaMz = 0.0d;
		double precursorPurity = 1.0d;
		
		TandemMassSpectrum instrumentMsMs = msmsCluster.getMSFeatureInfoBundleForPrimaryId().
				getMsFeature().getSpectrum().getExperimentalTandemSpectrum();
		
		if(instrumentMsMs != null) {

			if(instrumentMsMs.getParent() != null)
				parentMz = instrumentMsMs.getParent().getMz();
			
			featureSpectrumArray = instrumentMsMs.getSpectrumAsPythonArray();				
			precursorPurity = instrumentMsMs.getParentIonPurity();
		}
		ReferenceMsMsLibraryMatch msmslibMatch = id.getReferenceMsMsLibraryMatch();
		ReferenceMsMsLibrary lib = null;
		MsMsLibraryFeature matchFeature = null;
		if(msmslibMatch != null) {
			
			matchFeature = msmslibMatch.getMatchedLibraryFeature();
			lib = IDTDataCash.getReferenceMsMsLibraryById(matchFeature.getMsmsLibraryIdentifier());
			collisionEnergyValue = matchFeature.getCollisionEnergyValue();
			forwardScore = msmslibMatch.getForwardScore();
			reverseScore = msmslibMatch.getReverseScore();
			probability = msmslibMatch.getProbability();
			dotProduct = msmslibMatch.getDotProduct();
			isHybrid = msmslibMatch.isHybridMatch();
			isInSource = msmslibMatch.isInSourceMatch();
			revDotProduct = msmslibMatch.getReverseDotProduct();
			hybDotProduct = msmslibMatch.getHybridDotProduct();
			hybScore = msmslibMatch.getHybridScore();
			hybDmz = msmslibMatch.getHybridDeltaMz();
			msmsEntropy = matchFeature.getSpectrumEntropy();				
			if(!isHybrid)
				deltaMz = MsUtils.getMassErrorForIdentity(
						msmsCluster.getMz(), id, MassErrorType.ppm);
			
			qValue = msmslibMatch.getqValue();
			posteriorProbability = msmslibMatch.getPosteriorErrorProbability();
			percolatorScore = msmslibMatch.getPercolatorScore();				
			libraryHitSpectrumArray = matchFeature.getSpectrumAsPythonArray();

			if(id.getCompoundIdentity() != null) {
				double neutralMass = id.getCompoundIdentity().getExactMass();
				neutralMassDeltaMz = instrumentMsMs.getParent().getMz() - neutralMass;
			}
			MsPoint libPrecursor = msmslibMatch.getMatchedLibraryFeature().getParent();
			if(libPrecursor != null) 
				libraryPrecursorDeltaMz = instrumentMsMs.getParent().getMz() - libPrecursor.getMz();				
		}	
		if(property.equals(IDTrackerFeatureIdentificationProperties.MSMS_LIBRARY) && lib != null)
			return lib.getName();
		
//		if(property.equals(IDTrackerFeatureIdentificationProperties.MSMS_LIBRARY_ENTRY_ID))
//			msmslibMatch.getMatchedLibraryFeature().getMsmsLibraryIdentifier();
			
		if(property.equals(IDTrackerFeatureIdentificationProperties.COLLISION_ENERGY) && collisionEnergyValue != null)
			return collisionEnergyValue;
		
		if(property.equals(IDTrackerFeatureIdentificationProperties.REVERSE_SCORE) && reverseScore != 0.0d)
			return entropyFormat.format(reverseScore);

		if(property.equals(IDTrackerFeatureIdentificationProperties.PROBABILITY) && probability != 0.0d) 
			return entropyFormat.format(probability);
		
		if(property.equals(IDTrackerFeatureIdentificationProperties.DOT_PRODUCT_COLUMN) && dotProduct != 0.0d)
			return entropyFormat.format(dotProduct);
		
		if(property.equals(IDTrackerFeatureIdentificationProperties.REVERSE_DOT_PRODUCT) && revDotProduct != 0.0d)
			return entropyFormat.format(revDotProduct);
		
		if(property.equals(IDTrackerFeatureIdentificationProperties.MATCH_TYPE)) {
			if(isHybrid)
				return MSMSMatchType.Hybrid.name();
			else if(isInSource)
				return MSMSMatchType.InSource.name();
			else
				return MSMSMatchType.Regular.name();
		}
		if(property.equals(IDTrackerFeatureIdentificationProperties.HYBRID_DOT_PRODUCT) && hybDotProduct != 0.0d)
			return entropyFormat.format(hybDotProduct);
		
		if(property.equals(IDTrackerFeatureIdentificationProperties.HYBRID_SCORE) && hybScore != 0.0d)
			return entropyFormat.format(hybScore);
		
		if(property.equals(IDTrackerFeatureIdentificationProperties.HYBRID_DELTA_MZ) && hybDmz != 0.0d)
			return mzFormat.format(hybDmz);
		
		if(property.equals(IDTrackerFeatureIdentificationProperties.SPECTRUM_ENTROPY) && msmsEntropy != 0.0d)
			return entropyFormat.format(msmsEntropy);
		
		if(property.equals(IDTrackerFeatureIdentificationProperties.LIBRARY_PRECURSOR_DELTA_MZ) && libraryPrecursorDeltaMz != 0.0d)
			return mzFormat.format(libraryPrecursorDeltaMz);
		
		if(property.equals(IDTrackerFeatureIdentificationProperties.NEUTRAL_MASS_PRECURSOR_DELTA_MZ) && neutralMassDeltaMz != 0.0d)
			return mzFormat.format(neutralMassDeltaMz);
		
		if(property.equals(IDTrackerFeatureIdentificationProperties.PRECURSOR_PURITY) && precursorPurity != 0.0d)
			return entropyFormat.format(precursorPurity);
		
		if(property.equals(IDTrackerFeatureIdentificationProperties.FDR_Q_VALUE) && qValue != 0.0d) {
			if(qValue < 0.001d || qValue > 1000.d)
				return sciFormatter.format(qValue);
			else
				return entropyFormat.format(qValue);
		}
		if(property.equals(IDTrackerFeatureIdentificationProperties.POSTERIOR_PROBABILITY) && posteriorProbability != 0.0d) {
			if(posteriorProbability < 0.001d || posteriorProbability > 1000.d)
				return sciFormatter.format(posteriorProbability);
			else
				return entropyFormat.format(posteriorProbability);	
		}
		if(property.equals(IDTrackerFeatureIdentificationProperties.PERCOLATOR_SCORE) && percolatorScore != 0.0d)
			return entropyFormat.format(percolatorScore);	
		
		if(property.equals(IDTrackerFeatureIdentificationProperties.FEATURE_MSMS))
			return featureSpectrumArray;	
		
		if(property.equals(IDTrackerFeatureIdentificationProperties.LIBRARY_MATCH_MSMS))
			return libraryHitSpectrumArray;
		
		return "";
	}

	@Override
	public Task cloneTask() {

		return new IDTrackerMSMSClusterDataExportTask(
				msmsClusterDataSet,
				msmsClusterPropertyList,
				msmsFeaturePropertyList,
				identificationDetailsList,
				exportIndividualFeatureData,
				scoringParam, 
				minimalMSMSScore, 
				idSubset, 
				msmsSearchTypes, 
				excludeIfNoIdsLeft,
				outputFile);
	}
	
	public File getOutputFile() {
		return outputFile;
	}
}

/*******************************************************************************
 *
 * (C) Copyright 2018-2025 MRC2 (http://mrc2.umich.edu).
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

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import edu.umich.med.mrc2.datoolbox.data.IDTrackerDataExportParameters;
import edu.umich.med.mrc2.datoolbox.data.MSFeatureInfoBundle;
import edu.umich.med.mrc2.datoolbox.data.MinimalMSOneFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureIdentity;
import edu.umich.med.mrc2.datoolbox.data.compare.MsFeatureInfoBundleClusterComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.enums.ClassyFireClassificationLevels;
import edu.umich.med.mrc2.datoolbox.data.enums.IDTrackerFeatureIdentificationProperties;
import edu.umich.med.mrc2.datoolbox.data.enums.IDTrackerMSMSClusterProperties;
import edu.umich.med.mrc2.datoolbox.data.enums.IDTrackerMsFeatureProperties;
import edu.umich.med.mrc2.datoolbox.data.enums.RefMetClassificationLevels;
import edu.umich.med.mrc2.datoolbox.data.lims.Injection;
import edu.umich.med.mrc2.datoolbox.data.msclust.IMSMSClusterDataSet;
import edu.umich.med.mrc2.datoolbox.data.msclust.IMsFeatureInfoBundleCluster;
import edu.umich.med.mrc2.datoolbox.gui.idworks.clustree.MajorClusterFeatureDefiningProperty;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;

public class IDTrackerMSMSClusterDataExportTask extends IDTrackerFeatureExportTask {
	
	private IMSMSClusterDataSet msmsClusterDataSet;
	private Collection<IDTrackerMSMSClusterProperties> msmsClusterPropertyList;	
	private boolean exportIndividualFeatureData;
	private MajorClusterFeatureDefiningProperty mcfdp;

	public IDTrackerMSMSClusterDataExportTask(
			IMSMSClusterDataSet msmsClusterDataSet,
			IDTrackerDataExportParameters params,
			File outputFile) {
		super();
		this.params = params;
		this.featureIDSubset = params.getFeatureIDSubset();
		this.msLevel = params.getMsLevel();
		this.msmsClusterDataSet = msmsClusterDataSet;
		this.msmsClusterPropertyList = params.getMsmsClusterPropertyList();
		this.featurePropertyList = params.getFeaturePropertyList();
		this.identificationDetailsList = params.getIdentificationDetailsList();	
		this.exportIndividualFeatureData = params.isExportIndividualFeatures(); 
		this.msmsScoringParameter = params.getMsmsScoringParameter(); 
		this.minimalMSMSScore = params.getMinimalMSMSScore(); 
		this.featureIdSubset = params.getFeatureIDSubset(); 
		this.msmsSearchTypes = params.getMsmsSearchTypes(); 
		this.excludeIfNoIdsLeft = params.isExcludeFromExportWhenAllIdsFilteredOut();	
		this.mcfdp = params.getMajorClusterFeatureDefiningProperty();
		this.outputFile = outputFile;		
	}

	@Override
	public void run() {
		
		setStatus(TaskStatus.PROCESSING);
		
		Set<MSFeatureInfoBundle> bundles = msmsClusterDataSet.getClusters().stream().
				flatMap(c -> c.getComponents().stream()).collect(Collectors.toSet());
		
		if(msmsClusterPropertyList.contains(IDTrackerMSMSClusterProperties.PRIMARY_ID_RAW_DATA_FILE)
				|| featurePropertyList.contains(IDTrackerMsFeatureProperties.RAW_DATA_FILE)) {
			
			try {
				getInjections(bundles);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}	
		accessions = getAccessionsForClusters(msmsClusterDataSet.getClusters());
		
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
			e1.printStackTrace();
			setStatus(TaskStatus.ERROR);
			return;
		}
	}

	private Collection<String>getAccessionsForClusters(Collection<IMsFeatureInfoBundleCluster> msmsClusters){
		
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
		List<String>dataToExport = new ArrayList<String>();
		String header = createExportFileHeader();
		dataToExport.add(header);
		
		List<IMsFeatureInfoBundleCluster> toExport = msmsClusterDataSet.getClusters().stream().
				sorted(new MsFeatureInfoBundleClusterComparator(SortProperty.ID)).
				collect(Collectors.toList());
		
		for(IMsFeatureInfoBundleCluster msmsCluster : toExport) {
						
			Map<MSFeatureInfoBundle, Collection<MsFeatureIdentity>>idMap =
					filterClusterIdentifications(msmsCluster);
						
			for(Entry<MSFeatureInfoBundle, Collection<MsFeatureIdentity>>me : idMap.entrySet()) {
								
				if (me.getValue().isEmpty()) {

					if ((!me.getKey().getMsFeature().getIdentifications().isEmpty() && !excludeIfNoIdsLeft)
							|| me.getKey().getMsFeature().getPrimaryIdentity() == null) {

						ArrayList<String>line = new ArrayList<String>();
						
						for(IDTrackerMSMSClusterProperties property : msmsClusterPropertyList)
							line.add(getClusterProperty(msmsCluster, property));
						
						for (IDTrackerMsFeatureProperties property : featurePropertyList)
							line.add(getFeatureProperty(me.getKey(), property));

						for (IDTrackerFeatureIdentificationProperties property : identificationDetailsList)
							line.add("");
						
						dataToExport.add(StringUtils.join(line, columnSeparator));
					}					
				}
				else {			
					for(MsFeatureIdentity fid : me.getValue()) {
						
						ArrayList<String>line = new ArrayList<String>();
						
						for(IDTrackerMSMSClusterProperties property : msmsClusterPropertyList)
							line.add(getClusterProperty(msmsCluster, property));
						
						for(IDTrackerMsFeatureProperties property : featurePropertyList)
							line.add(getFeatureProperty(me.getKey(), property));
						
						String accession = null;
						if(fid != null && fid.getCompoundIdentity() != null)
							accession = fid.getCompoundIdentity().getPrimaryDatabaseId();
						
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
								line.add(getFeatureIdentificationProperty(fid, me.getKey().getMsFeature(), property));
							}
						}
						dataToExport.add(StringUtils.join(line, columnSeparator));
					}
				}							
			}
			processed++;
		}
		Path outputPath = Paths.get(outputFile.getAbsolutePath());
		try {
			Files.write(outputPath, 
					dataToExport, 
					StandardCharsets.UTF_8,
					StandardOpenOption.CREATE, 
					StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private Map<MSFeatureInfoBundle, Collection<MsFeatureIdentity>>
			filterClusterIdentifications(IMsFeatureInfoBundleCluster cluster) {
		
		Map<MSFeatureInfoBundle, Collection<MsFeatureIdentity>>idMap = 
				new HashMap<MSFeatureInfoBundle, Collection<MsFeatureIdentity>>();
		if(exportIndividualFeatureData) {
			
			for(MSFeatureInfoBundle bundle : cluster.getComponents())
				idMap.put(bundle, filterIdentities(bundle));			
		}
		else {
			if(mcfdp == null) {
				
				MsFeatureIdentity primId = cluster.getPrimaryIdentity();
				
				if(primId != null) {
					
					MSFeatureInfoBundle idBundle = cluster.getMSFeatureInfoBundleForPrimaryId();				
					if(idBundle != null) {
						
						Collection<MsFeatureIdentity> filteredBundleIds = filterIdentities(idBundle);
						if(filteredBundleIds.contains(primId))
							idMap.put(idBundle, Collections.singleton(primId));
					}
				}
			}
			else {
				MSFeatureInfoBundle idBundle = cluster.getDefiningFeature(mcfdp);
				if(idBundle != null && idBundle.getMsFeature().getPrimaryIdentity() != null)
					idMap.put(idBundle, Collections.singleton(idBundle.getMsFeature().getPrimaryIdentity()));
			}
		}		
		return idMap;
	}
	
	private String createExportFileHeader() {
		
		//	Cluster properties
		ArrayList<String>header = new ArrayList<String>();
		msmsClusterPropertyList.stream().forEach(v -> header.add(v.getName()));
		
		//	Feature properties
		featurePropertyList.stream().forEach(v -> header.add(v.getName()));
		
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
		return StringUtils.join(header, columnSeparator);
	}
	
//	private void writeExportFileOld() throws IOException {
//
//		taskDescription = "Wtiting output";
//		total = msmsClusterDataSet.getClusters().size();
//		processed = 0;
//		final Writer writer = new BufferedWriter(
//				new FileWriter(outputFile, StandardCharsets.UTF_8));
//		
//		//	Header
//		//	Cluster properties
//		ArrayList<String>header = new ArrayList<String>();
//		msmsClusterPropertyList.stream().forEach(v -> header.add(v.getName()));
//		
//		//	Feature properties
//		
//		// Add Identification fields
//		for(IDTrackerFeatureIdentificationProperties idField : identificationDetailsList) {
//			
//			if(idField.equals(IDTrackerFeatureIdentificationProperties.REFMET_CLASSIFICATION)) {
//				
//				for(RefMetClassificationLevels rmLevel : RefMetClassificationLevels.values())
//					header.add(rmLevel.getName());			
//			}
//			else if(idField.equals(IDTrackerFeatureIdentificationProperties.CLASSYFIRE_CLASSIFICATION)) {
//				
//				for(ClassyFireClassificationLevels cfLevel : ClassyFireClassificationLevels.values())
//					header.add(cfLevel.getName());	
//			}
//			else {
//				header.add(idField.getName());
//			}		
//		}		
//		writer.append(StringUtils.join(header, columnSeparator));
//		writer.append(lineSeparator);
//		
//		List<MsFeatureInfoBundleCluster> toExport = msmsClusterDataSet.getClusters().stream().
//				sorted(new MsFeatureInfoBundleClusterComparator(SortProperty.RT)).
//				collect(Collectors.toList());
//		ArrayList<String>line;
//		for(MsFeatureInfoBundleCluster msmsCluster : toExport) {
//
//			line = new ArrayList<String>();
//			for(IDTrackerMSMSClusterProperties property : msmsClusterPropertyList)
//				line.add(getClusterProperty(msmsCluster, property));
//			
//			String accession = null;
//			if(msmsCluster.getPrimaryIdentity() != null 
//					&& msmsCluster.getPrimaryIdentity().getCompoundIdentity() != null)
//				accession = msmsCluster.getPrimaryIdentity().getCompoundIdentity().getPrimaryDatabaseId();
//			
//			for(IDTrackerFeatureIdentificationProperties property : identificationDetailsList) {
//				
//				if(property.equals(IDTrackerFeatureIdentificationProperties.REFMET_CLASSIFICATION)) {
//
//					if(accession != null) {
//						Map<RefMetClassificationLevels,String> rmClassMap = refMetClassifications.get(accession);
//						for(RefMetClassificationLevels rmLevel : RefMetClassificationLevels.values())
//							line.add(rmClassMap.get(rmLevel));
//					}
//					else {
//						for(RefMetClassificationLevels rmLevel : RefMetClassificationLevels.values())
//							line.add("");
//					}
//				}
//				else if(property.equals(IDTrackerFeatureIdentificationProperties.CLASSYFIRE_CLASSIFICATION)) {
//					
//					if(accession != null) {
//						Map<ClassyFireClassificationLevels,String> cfClassMap = classyFireClassifications.get(accession);
//						if(cfClassMap != null) {
//							
//							for(ClassyFireClassificationLevels cfLevel : ClassyFireClassificationLevels.values())
//								if(cfLevel != null)
//									line.add(cfClassMap.get(cfLevel));
//								else
//									line.add("");
//						}
//						else {
//							for(ClassyFireClassificationLevels cfLevel : ClassyFireClassificationLevels.values())
//								line.add("");
//						}
//					}
//					else {
//						for(ClassyFireClassificationLevels cfLevel : ClassyFireClassificationLevels.values())
//							line.add("");
//					}
//				}
//				else {
//					line.add(getFeatureIdentificationProperty(msmsCluster, property));
//				}
//			}
//			writer.append(StringUtils.join(line, columnSeparator));
//			writer.append(lineSeparator);
//			processed++;
//		}
//		writer.flush();
//		writer.close();
//	}
	
	private String getClusterProperty(
			IMsFeatureInfoBundleCluster cluster, 
			IDTrackerMSMSClusterProperties property) {
		
		MinimalMSOneFeature lookupFeature = cluster.getLookupFeature();
		
		//	TODO deal with binner identifications

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

	@Override
	public Task cloneTask() {

		return new IDTrackerMSMSClusterDataExportTask(
				msmsClusterDataSet,
				params,
				outputFile);
	}
	
	public File getOutputFile() {
		return outputFile;
	}
}

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

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import edu.umich.med.mrc2.datoolbox.data.IDTrackerDataExportParameters;
import edu.umich.med.mrc2.datoolbox.data.MSFeatureInfoBundle;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureCluster;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureIdentity;
import edu.umich.med.mrc2.datoolbox.data.compare.MsFeatureInfoBundleComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.enums.ClassyFireClassificationLevels;
import edu.umich.med.mrc2.datoolbox.data.enums.IDTrackerFeatureIdentificationProperties;
import edu.umich.med.mrc2.datoolbox.data.enums.IDTrackerMsFeatureProperties;
import edu.umich.med.mrc2.datoolbox.data.enums.MassErrorType;
import edu.umich.med.mrc2.datoolbox.data.enums.MsDepth;
import edu.umich.med.mrc2.datoolbox.data.enums.RefMetClassificationLevels;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;

public class IDTrackerDataExportTask extends IDTrackerFeatureExportTask {
	
	private Collection<MSFeatureInfoBundle>featuresToExport;
	
	private boolean removeRedundant;
	private double redundantMzWindow;
	private MassErrorType redMzErrorType;
	private double redundantRTWindow;
	
	public IDTrackerDataExportTask(
			Collection<MSFeatureInfoBundle> featuresToExport,
			IDTrackerDataExportParameters params,
			File outputFile) {
		super();
		this.params = params;
		
		this.msLevel = params.getMsLevel();
		this.featuresToExport = featuresToExport;
		this.featurePropertyList = params.getFeaturePropertyList();
		this.identificationDetailsList = params.getIdentificationDetailsList();
		this.removeRedundant = params.isRemoveRedundant();
		this.redundantMzWindow = params.getRedundantMzWindow();
		this.redMzErrorType = params.getRedMzErrorType();
		this.redundantRTWindow = params.getRedundantRTWindow();
		
		this.msmsScoringParameter = params.getMsmsScoringParameter();	
		this.minimalMSMSScore = params.getMinimalMSMSScore();	
		this.featureIDSubset = params.getFeatureIDSubset();	
		this.msmsSearchTypes = params.getMsmsSearchTypes();
		this.excludeIfNoIdsLeft = params.isExcludeFromExportWhenAllIdsFilteredOut();
		this.decoyExportHandling = params.getDecoyExportHandling();
		
		this.outputFile = outputFile;
	}

	@Override
	public void run() {
		
		setStatus(TaskStatus.PROCESSING);
		if(removeRedundant)
			removeRedundantFeatures();
		
		accessions = getAccessions(featuresToExport);
		if(featurePropertyList.contains(IDTrackerMsFeatureProperties.RAW_DATA_FILE)) {
			try {
				getInjections(featuresToExport);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}	
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
		createIdentificationsMap(featuresToExport);
		try {
			writeExportFile();
			setStatus(TaskStatus.FINISHED);
		}
		catch (Exception e1) {
			setStatus(TaskStatus.ERROR);
			e1.printStackTrace();
		}
	}

	private void removeRedundantFeatures() {
		
		taskDescription = "Removing redundant features ...";
		total = featuresToExport.size();
		processed = 0;
			
		MSFeatureInfoBundle fb = featuresToExport.iterator().next();
		DataPipeline exportDataPipeline = new DataPipeline(
				fb.getAcquisitionMethod(), fb.getDataExtractionMethod());
		HashSet<MsFeature>assigned = new HashSet<MsFeature>();
		ArrayList<MsFeatureCluster> clusters = new ArrayList<MsFeatureCluster>();
		
		if(msLevel.equals(MsDepth.MS1)) {
			
			for (MSFeatureInfoBundle cf : featuresToExport) {
	
				for (MsFeatureCluster fClust : clusters) {
					
					if (fClust.matches(cf.getMsFeature(), redundantMzWindow, redMzErrorType, redundantRTWindow)) {
	
						fClust.addFeature(cf.getMsFeature(), exportDataPipeline);
						assigned.add(cf.getMsFeature());
						break;
					}
				}
				if (!assigned.contains(cf.getMsFeature())) {
	
					MsFeatureCluster newCluster = new MsFeatureCluster();
					newCluster.addFeature(cf.getMsFeature(), exportDataPipeline);
					assigned.add(cf.getMsFeature());
					clusters.add(newCluster);
				}
				processed++;
			}
		}
		if(msLevel.equals(MsDepth.MS2)) {
			
			for (MSFeatureInfoBundle cf : featuresToExport) {
	
				for (MsFeatureCluster fClust : clusters) {
					
					if (fClust.matchesOnMSMSParentIon(cf.getMsFeature(), 
							redundantMzWindow, redMzErrorType, redundantRTWindow)) {
	
						fClust.addFeature(cf.getMsFeature(), exportDataPipeline);
						assigned.add(cf.getMsFeature());
						break;
					}
				}
				if (!assigned.contains(cf.getMsFeature())) {
	
					MsFeatureCluster newCluster = new MsFeatureCluster();
					newCluster.addFeature(cf.getMsFeature(), exportDataPipeline);
					assigned.add(cf.getMsFeature());
					clusters.add(newCluster);
				}
				processed++;
			}
		}
		Set<MsFeature> cleanedFeatures = clusters.stream().
				map(c -> c.getMostIntensiveFeature()).collect(Collectors.toSet());
		
		featuresToExport = featuresToExport.stream().
				filter(b -> cleanedFeatures.contains(b.getMsFeature())).
				sorted(new MsFeatureInfoBundleComparator(SortProperty.RT)).
				collect(Collectors.toList());
	}
	
	private void writeExportFile() throws IOException {

		taskDescription = "Wtiting output";
		total = featuresToExport.size();
		processed = 0;
		List<String>dataToExport = new ArrayList<String>();
		String header = createExportFileHeader();
		dataToExport.add(header);
		
		List<MSFeatureInfoBundle> toExport = featuresToExport.stream().
				sorted(new MsFeatureInfoBundleComparator(SortProperty.RT)).
				collect(Collectors.toList());
		
		for(MSFeatureInfoBundle bundle : toExport) {

			MsFeature feature = bundle.getMsFeature();
			Collection<MsFeatureIdentity> featureIds = identificationMap.get(bundle);
			if(featureIds == null || featureIds.isEmpty()) {
				
				if((!feature.getIdentifications().isEmpty() && !excludeIfNoIdsLeft)
						|| feature.getPrimaryIdentity() == null) {
					
					ArrayList<String>line = new ArrayList<String>();
					for(IDTrackerMsFeatureProperties property : featurePropertyList)
						line.add(getFeatureProperty(bundle, property));
					
					for(IDTrackerFeatureIdentificationProperties property : identificationDetailsList)
						line.add("");
					
					dataToExport.add(StringUtils.join(line, columnSeparator));
				}
			}
			else {
				for(MsFeatureIdentity fid : featureIds) {
					
					ArrayList<String>line = new ArrayList<String>();
					for(IDTrackerMsFeatureProperties property : featurePropertyList)
						line.add(getFeatureProperty(bundle, property));
										
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
							line.add(getFeatureIdentificationProperty(fid, feature, property));
						}
					}
					dataToExport.add(StringUtils.join(line, columnSeparator));
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
	
	private String createExportFileHeader(){
		
		ArrayList<String>header = new ArrayList<String>();
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

	@Override
	public Task cloneTask() {

		return new IDTrackerDataExportTask(
				featuresToExport,
				params,
				outputFile);
	}
	
	public File getOutputFile() {
		return outputFile;
	}
}

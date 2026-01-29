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
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import edu.umich.med.mrc2.datoolbox.data.IDTrackerDataExportParameters;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureIdentity;
import edu.umich.med.mrc2.datoolbox.data.compare.MsFeatureInfoBundleClusterComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.enums.ClassyFireClassificationLevels;
import edu.umich.med.mrc2.datoolbox.data.enums.IDTrackerFeatureIdentificationProperties;
import edu.umich.med.mrc2.datoolbox.data.enums.IDTrackerMSMSClusterProperties;
import edu.umich.med.mrc2.datoolbox.data.enums.MassErrorType;
import edu.umich.med.mrc2.datoolbox.data.enums.MsDepth;
import edu.umich.med.mrc2.datoolbox.data.enums.RefMetClassificationLevels;
import edu.umich.med.mrc2.datoolbox.data.enums.SummaryIdentificationProperties;
import edu.umich.med.mrc2.datoolbox.data.msclust.IMSMSClusterDataSet;
import edu.umich.med.mrc2.datoolbox.data.msclust.IMsFeatureInfoBundleCluster;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.utils.ClusterUtils;

public class IDTrackerMSMSClusterIDSummaryExportTask extends IDTrackerMSMSClusterDataExportTask {

	private Collection<SummaryIdentificationProperties> identificationSummaryPropertyList;
	private double redundantMzWindow;
	private MassErrorType redMzErrorType;
	
	public IDTrackerMSMSClusterIDSummaryExportTask(
			IMSMSClusterDataSet msmsClusterDataSet,
			IDTrackerDataExportParameters params, 
			File outputFile) {
		super(msmsClusterDataSet, params, outputFile);
		this.identificationSummaryPropertyList = 
				params.getIdentificationSummaryPropertyList();
		this.exportIndividualFeatureData = false;
		this.featurePropertyList = new ArrayList<>();
		this.msLevel = MsDepth.MS2;
		this.redundantMzWindow = params.getRedundantMzWindow();
		this.redMzErrorType = params.getRedMzErrorType();
	}
	
	@Override
	public void run() {
		
		setStatus(TaskStatus.PROCESSING);
		
		getInjectionData();
		getCompoundMetadata();
		try {
			writeExportFile();
			setStatus(TaskStatus.FINISHED);
		}
		catch (Exception e1) {
			e1.printStackTrace();
			setStatus(TaskStatus.ERROR);
		}
	}

	@Override
	protected void writeExportFile() throws IOException {

		taskDescription = "Wtiting output";
		total = msmsClusterDataSet.getClusters().size();
		processed = 0;
		List<String>dataToExport = new ArrayList<>();
		String header = createExportFileHeader();
		dataToExport.add(header);
		
		List<IMsFeatureInfoBundleCluster> toExport = msmsClusterDataSet.getClusters().stream().
				sorted(new MsFeatureInfoBundleClusterComparator(SortProperty.RT)).
				collect(Collectors.toList());
		
		for(IMsFeatureInfoBundleCluster msmsCluster : toExport) {
			
			MsFeatureIdentity prinmaryId = msmsCluster.getPrimaryIdentity();
			if(prinmaryId == null || prinmaryId.getCompoundIdentity() == null) {				
				processed++;
				continue;
			}
			ArrayList<String>line = new ArrayList<>();
			
			for(IDTrackerMSMSClusterProperties property : msmsClusterPropertyList)
				line.add(getClusterProperty(msmsCluster, property));				

			String accession = prinmaryId.getCompoundIdentity().getPrimaryDatabaseId();
			
			for(IDTrackerFeatureIdentificationProperties property : identificationDetailsList) {
				
				if(property.equals(IDTrackerFeatureIdentificationProperties.REFMET_CLASSIFICATION)) {
					addRefMetClassification(accession, line);
				}
				else if(property.equals(IDTrackerFeatureIdentificationProperties.CLASSYFIRE_CLASSIFICATION)) {
					addClassyFireClassification(accession, line);
				}
				else {
					if(msmsCluster.getMSFeatureInfoBundleForPrimaryId() == null) {
						System.out.println("***");
					}
					else {
						line.add(getFeatureIdentificationProperty(prinmaryId, 
								msmsCluster.getMSFeatureInfoBundleForPrimaryId().getMsFeature(), property));
					}
				}
			}
			for(SummaryIdentificationProperties summaryProperty : identificationSummaryPropertyList)				
				line.add(getClusterIDSummaryProperty(msmsCluster, summaryProperty));
			
			dataToExport.add(StringUtils.join(line, columnSeparator));
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
	
	@Override
	protected String createExportFileHeader() {
		
		//	Cluster properties
		ArrayList<String>header = new ArrayList<>();
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
		//	Add summary fields
		identificationSummaryPropertyList.stream().forEach(v -> header.add(v.name()));
				
		return StringUtils.join(header, columnSeparator);
	}
	
	protected String getClusterIDSummaryProperty(
			IMsFeatureInfoBundleCluster cluster, 
			SummaryIdentificationProperties property) {
		
		if(property.equals(SummaryIdentificationProperties.NUMBER_OF_HITS)){
			return Long.toString(cluster.getFeatureNumber());
		}
		if(property.equals(SummaryIdentificationProperties.MEDIAN_TOP_ENTROPY_SCORE)){
			return entropyFormat.format(ClusterUtils.getMedianTopEntropyMatchScoreForCluster(cluster));
		}
		if(property.equals(SummaryIdentificationProperties.RT_RANGE)){
			return cluster.getRTrange().getFormattedString(MRC2ToolBoxConfiguration.getRtFormat());
		}
		if(property.equals(SummaryIdentificationProperties.FRAGMENTATION_ENERGIES_NUMBER)){
			Set<Double>fragEnergies = ClusterUtils.getFragmentationEnergiesForCluster(cluster);
			return Integer.toString(fragEnergies.size());
		}
		if(property.equals(SummaryIdentificationProperties.FRAGMENTATION_ENERGIES)){
			
			List<String>fragEnergies = 
					ClusterUtils.getFragmentationEnergiesForCluster(cluster).
						stream().map(e -> Double.toString(e)).collect(Collectors.toList());
			return StringUtils.join(fragEnergies, ",");
		}
		if(property.equals(SummaryIdentificationProperties.COLLISION_ENERGIES_NUMBER)){
			Set<Double>collisionEnergies = ClusterUtils.getCollisionVoltagesForCluster(cluster);
			return Integer.toString(collisionEnergies.size());
		}
		if(property.equals(SummaryIdentificationProperties.COLLISION_ENERGIES)){
			
			List<String>collisionEnergies = 
					ClusterUtils.getCollisionVoltagesForCluster(cluster).
						stream().map(e -> Double.toString(e)).collect(Collectors.toList());
			return StringUtils.join(collisionEnergies, ",");
		}
		if(property.equals(SummaryIdentificationProperties.PARENT_IONS_NUMBER)){
			
			Set<Double>parentIons = ClusterUtils.getParentIonsForCluster(cluster);
			return Integer.toString(parentIons.size());
		}
		if(property.equals(SummaryIdentificationProperties.PARENT_IONS)){
			
			List<String>parentIons = 
					ClusterUtils.getBinnedParentIonsForCluster(cluster, redundantMzWindow, redMzErrorType).
					stream().map(mzFormat::format).collect(Collectors.toList());
			return StringUtils.join(parentIons, ",");
		}
		if(property.equals(SummaryIdentificationProperties.LIB_ADDUCTS_NUMBER)){
			
			Set<String>adducts =  ClusterUtils.getLibraryAdductsForCluster(cluster);
			return Integer.toString(adducts.size());
		}
		if(property.equals(SummaryIdentificationProperties.LIB_ADDUCTS)){
			Set<String>adducts =  ClusterUtils.getLibraryAdductsForCluster(cluster);
			return StringUtils.join(adducts, ",");
		}
		if(property.equals(SummaryIdentificationProperties.MATCH_TYPES)){
			
			Set<String>matchTypes =  ClusterUtils.getMSMSmatchTypesForCluster(cluster);
			return StringUtils.join(matchTypes, ",");
		}		
		return "";
	}
	
	
}

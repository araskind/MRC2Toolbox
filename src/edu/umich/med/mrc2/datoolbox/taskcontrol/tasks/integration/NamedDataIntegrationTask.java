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

package edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.integration;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureCluster;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureClusterSet;
import edu.umich.med.mrc2.datoolbox.data.compare.MsFeatureClusterComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.enums.PrimaryFeatureSelectionOption;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;

public class NamedDataIntegrationTask extends AbstractTask {
	
	private DataAnalysisProject project;
	private Collection<DataPipeline> selectedDataPipelines;
	private PrimaryFeatureSelectionOption primaryFeatureSelectionOption;
	private String dataSetName;
	private MsFeatureClusterSet integratedDataSet;
	
	private static final String[]compoundAnnotationMasks 
		= new String[] { "\\(duplicate \\d\\)", " \\(variant\\)" };
	
	public NamedDataIntegrationTask(
			DataAnalysisProject project, 
			Collection<DataPipeline> selectedDataPipelines,
			PrimaryFeatureSelectionOption primaryFeatureSelectionOption, 
			String dataSetName) {
		super();
		this.project = project;
		this.selectedDataPipelines = selectedDataPipelines;
		this.primaryFeatureSelectionOption = primaryFeatureSelectionOption;
		this.dataSetName = dataSetName;
		
		integratedDataSet = new MsFeatureClusterSet(dataSetName);
	}

	@Override
	public void run() {

		taskDescription = "";
		setStatus(TaskStatus.PROCESSING);
		try {
			createIdClusters();
		}
		catch (Exception e) {
			reportErrorAndExit(e);
		}
		try {
			assignPrimaryFeature();
		}
		catch (Exception e) {
			reportErrorAndExit(e);
		}
		setStatus(TaskStatus.FINISHED);
	}
	
	private void assignPrimaryFeature() {

		taskDescription = "Assigning primary features ...";
		total = integratedDataSet.getNumberOfClusters();
		processed = 0;
		for(MsFeatureCluster cluster : integratedDataSet.getClusters()) {
			
			if(cluster.getFeatures().size() > 1) {
				
				MsFeature primary = cluster.findPrimaryFeature(primaryFeatureSelectionOption);
				if(primary != null)
					cluster.setPrimaryFeature(primary);
			}
			else {
				cluster.setPrimaryFeature(cluster.getFeatures().iterator().next()); 
			}
			processed++;
		}
	}

	private void createIdClusters() {
		
		taskDescription = "Creating feature clusters based on compound ID ...";
//		TreeMap<String, MsFeatureCluster>idClusterMap =  new TreeMap<>();
		Set<MsFeatureCluster>featureClusters = new HashSet<>();
//		Pattern[]annotationPatterns = createAnnotationPatterns();
				
		for(DataPipeline pipeine : selectedDataPipelines) {		
			
			Set<MsFeature> features = project.getMsFeaturesForDataPipeline(pipeine);
			total = features.size();
			processed = 0;
			for(MsFeature f : features) {
					
				String label = f.getPrimaryIdentity().getPrimaryLinkLabel();
//				if(f.getName().contains("(duplicate"))
//					System.out.println(f.getName());
				
				MsFeatureCluster existingCluster = 
						featureClusters.stream().
						filter(c -> c.getPrimaryIdentity().getPrimaryLinkLabel().equals(label)).
						findFirst().orElse(null);
				if(existingCluster == null) {
					MsFeatureCluster newCluster = new MsFeatureCluster();
					newCluster.addFeature(f, pipeine);
					featureClusters.add(newCluster);
				}
				else{
					if(existingCluster.getFeturesForDataPipeline(pipeine) == null
							|| existingCluster.getFeturesForDataPipeline(pipeine).isEmpty())
						existingCluster.addFeature(f, pipeine);
					else {
						MsFeatureCluster newCluster = new MsFeatureCluster();
						newCluster.addFeature(f, pipeine);
						featureClusters.add(newCluster);
					}
				}
//				if(isAmbiguous(f, annotationPatterns))
//					label = f.getName();
//				
//				idClusterMap.computeIfAbsent(label, k -> new MsFeatureCluster());				
//				idClusterMap.get(label).addFeature(f,pipeine);				
				processed++;
			}
		}
		List<MsFeatureCluster> idSorted = featureClusters.stream().
				sorted(new MsFeatureClusterComparator(SortProperty.pimaryId)).
				collect(Collectors.toList());
		
		integratedDataSet.addClusterCollection(idSorted);
	}
	
	private boolean isAmbiguous(MsFeature msf, Pattern[]annotationPatterns) {
		
		for(Pattern ap : annotationPatterns) {
			
			if(ap.matcher(msf.getName()).find())
				return true;
		}
		return false;
	}
	
	private Pattern[] createAnnotationPatterns() {
		
		Pattern[]annotationPatterns = new Pattern[compoundAnnotationMasks.length];
		for(int i=0; i<compoundAnnotationMasks.length; i++)
			annotationPatterns[i] = Pattern.compile(compoundAnnotationMasks[i]);
				
		return annotationPatterns;
	}
	
	@Override
	public Task cloneTask() {

		return new NamedDataIntegrationTask(
				project, 
				selectedDataPipelines,
				primaryFeatureSelectionOption, 
				dataSetName);
	}

	public MsFeatureClusterSet getIntegratedDataSet() {
		return integratedDataSet;
	}
}

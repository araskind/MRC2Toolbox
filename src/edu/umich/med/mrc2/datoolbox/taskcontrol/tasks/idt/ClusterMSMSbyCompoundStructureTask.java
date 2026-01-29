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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import edu.umich.med.mrc2.datoolbox.data.MSFeatureInfoBundle;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureIdentity;
import edu.umich.med.mrc2.datoolbox.data.compare.MsFeatureIdentityComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.MsFeatureInfoBundleComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortDirection;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.msclust.IMSMSClusterDataSet;
import edu.umich.med.mrc2.datoolbox.data.msclust.IMsFeatureInfoBundleCluster;
import edu.umich.med.mrc2.datoolbox.data.msclust.MSMSClusterDataSet;
import edu.umich.med.mrc2.datoolbox.data.msclust.MSMSClusteringParameterSet;
import edu.umich.med.mrc2.datoolbox.data.msclust.MsFeatureInfoBundleCluster;
import edu.umich.med.mrc2.datoolbox.gui.idworks.nist.pepsearch.HiResSearchOption;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.utils.NISTPepSearchUtils;
import edu.umich.med.mrc2.datoolbox.utils.Range;

public class ClusterMSMSbyCompoundStructureTask extends AbstractTask {

	private String clusterSetName;
	private Collection<MSFeatureInfoBundle> featuresToProcess;
	private double msmsRtGroupingWindow;
	private boolean usePrimaryIdOnly;
	private double entropyScoreCutoff;
	private boolean useAssignedPrimaryIds;
	
	private IMSMSClusterDataSet clusterDataSet;
		
	
	public ClusterMSMSbyCompoundStructureTask(
			String clusterSetName, 
			Collection<MSFeatureInfoBundle> featuresToProcess,
			double msmsRtGroupingWindow, 
			boolean usePrimaryIdOnly,
			double entropyScoreCutoff,
			boolean useAssignedPrimaryIds) {
		super();
		this.clusterSetName = clusterSetName;
		this.featuresToProcess = featuresToProcess;
		this.msmsRtGroupingWindow = msmsRtGroupingWindow;
		this.usePrimaryIdOnly = usePrimaryIdOnly;
		this.entropyScoreCutoff = entropyScoreCutoff;
		this.useAssignedPrimaryIds = useAssignedPrimaryIds;
		
		clusterDataSet = new MSMSClusterDataSet(clusterSetName);
		MSMSClusteringParameterSet params = new MSMSClusteringParameterSet();		
		params.setRtErrorValue(msmsRtGroupingWindow);
		clusterDataSet.setParameters(params);
	}

	@Override
	public void run() {

		taskDescription = "";
		setStatus(TaskStatus.PROCESSING);
		if(usePrimaryIdOnly) {
			try {
				clusterFeaturesByPrimaryIdentity();
			}
			catch (Exception e) {
				reportErrorAndExit(e);
			}
		}
		else {
			try {
				clusterFeaturesUsingMatchScores();
			}
			catch (Exception e) {
				reportErrorAndExit(e);
			}
		}
		if(useAssignedPrimaryIds) {
			reassignPrimaryIdentitiesBasedOnFeatureData();
		}
		setStatus(TaskStatus.FINISHED);
	}

	private void clusterFeaturesByPrimaryIdentity() {
		
		taskDescription = "Clustering features using primary identities";
		total = featuresToProcess.size();
		processed = 0;
		Range rtWindow = null;
		
		MsFeatureInfoBundleComparator revIntensitySorter = 
				new MsFeatureInfoBundleComparator(SortProperty.msmsIntensity, SortDirection.DESC);
		
		List<MSFeatureInfoBundle> featuresToProcessSorted = 
				featuresToProcess.stream().
				filter(b -> Objects.nonNull(b.getMsFeature().getPrimaryIdentity().getReferenceMsMsLibraryMatch())).
				filter(b -> Objects.nonNull(b.getMsFeature().getPrimaryIdentity().getCompoundIdentity().getInChiKey2D())).
				sorted(revIntensitySorter).
				collect(Collectors.toList());
		IMsFeatureInfoBundleCluster firstCluster = 
				new MsFeatureInfoBundleCluster(featuresToProcessSorted.get(0));
		clusterDataSet.getClusters().add(firstCluster);
		boolean featureAdded = false;
		for(int i=1; i<featuresToProcessSorted.size(); i++) {
			
			featureAdded = false;
			MSFeatureInfoBundle msfb = featuresToProcessSorted.get(i);
			for(IMsFeatureInfoBundleCluster cluster : clusterDataSet.getClusters()) {
				
				rtWindow = new Range(
						cluster.getRt() - msmsRtGroupingWindow / 2.0d , 
						cluster.getRt() + msmsRtGroupingWindow / 2.0d);
				if(rtWindow.contains(msfb.getRetentionTime())) {
					
					String clusterIdTag = cluster.getPrimaryIdentity().getCompoundIdentity().getInChiKey2D();
					String featureIdTag = msfb.getMsFeature().getPrimaryIdentity().getCompoundIdentity().getInChiKey2D();
					if(clusterIdTag.equals(featureIdTag)) {
						cluster.addComponent(null, msfb);
						featureAdded = true;
						break;
					}
				}
			}
			if(!featureAdded)
				clusterDataSet.getClusters().add(new MsFeatureInfoBundleCluster(msfb));
			
			processed++;
		}
	}
	
	private void clusterFeaturesUsingMatchScores() {
		
		taskDescription = "Clustering features using match scores";
		total = featuresToProcess.size();
		processed = 0;
		
		for(MSFeatureInfoBundle msfb : featuresToProcess) {
			
			
			processed++;
		}
	}
	
	private void reassignPrimaryIdentitiesBasedOnFeatureData() {

		taskDescription = "Setting primary IDs";
		total = clusterDataSet.getClusters().size();
		processed = 0;
		MsFeatureIdentityComparator eScoreComparator = 
				new MsFeatureIdentityComparator(SortProperty.msmsEntropyScore, SortDirection.DESC);

		for(IMsFeatureInfoBundleCluster cluster : clusterDataSet.getClusters()) {
			
			if(cluster.getComponents().size() > 1) {
				
				List<MsFeatureIdentity> idList = cluster.getComponents().stream().
						map(c -> c.getMsFeature().getPrimaryIdentity()).
						collect(Collectors.toList());
				
				Map<String,HiResSearchOption>searchTypeMap = 
						NISTPepSearchUtils.getSearchTypeMap(cluster.getComponents());
				Map<HiResSearchOption,Collection<MsFeatureIdentity>>stIdMap = 
						NISTPepSearchUtils.getSearchTypeIdentityMap( idList, searchTypeMap, true);
				if(!stIdMap.get(HiResSearchOption.z).isEmpty()) {
					
					List<MsFeatureIdentity> mwpHitList = 
							stIdMap.get(HiResSearchOption.z).stream().
							sorted(eScoreComparator).
							collect(Collectors.toList());
					cluster.setPrimaryIdentity(mwpHitList.get(0));
				}
				else {
					if(!stIdMap.get(HiResSearchOption.u).isEmpty()) {
						
						List<MsFeatureIdentity> isHitList = 
								stIdMap.get(HiResSearchOption.u).stream().
								sorted(eScoreComparator).
								collect(Collectors.toList());
						cluster.setPrimaryIdentity(isHitList.get(0));
					}
				}
			}
			else {
				cluster.setPrimaryIdentity(cluster.getComponents().
						iterator().next().getMsFeature().getPrimaryIdentity());
			}
			processed++;
		}
	}
	
	@Override
	public Task cloneTask() {

		return new ClusterMSMSbyCompoundStructureTask(
				clusterSetName, 
				featuresToProcess, 
				msmsRtGroupingWindow, 
				usePrimaryIdOnly, 
				entropyScoreCutoff,
				useAssignedPrimaryIds);
	}

	public IMSMSClusterDataSet getFeatureClusterDataSet() {
		return clusterDataSet;
	}
}

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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import edu.umich.med.mrc2.datoolbox.data.MSFeatureInfoBundle;
import edu.umich.med.mrc2.datoolbox.data.MinimalMSOneFeature;
import edu.umich.med.mrc2.datoolbox.data.MsPoint;
import edu.umich.med.mrc2.datoolbox.data.enums.MassErrorType;
import edu.umich.med.mrc2.datoolbox.data.msclust.MSMSClusterDataSet;
import edu.umich.med.mrc2.datoolbox.data.msclust.MSMSClusteringParameterSet;
import edu.umich.med.mrc2.datoolbox.data.msclust.MsFeatureInfoBundleCluster;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.msmsscore.MSMSScoreCalculator;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.utils.MsUtils;
import edu.umich.med.mrc2.datoolbox.utils.Range;

public class MSMSFeatureClusteringTask extends AbstractTask {
	
	private Collection<MSFeatureInfoBundle> msmsFeatures;
	private Collection<MSFeatureInfoBundle> filteredMsmsFeatures;
	private MSMSClusteringParameterSet params;
	private Collection<MinimalMSOneFeature> lookupFeatures;
	private Collection<MsFeatureInfoBundleCluster>featureClusters;
	private MSMSClusterDataSet msmsClusterDataSet;
	private double rtError;
	private double mzError;
	private MassErrorType mzErrorType;
	private double minMsMsScore;
	private static final double SPECTRUM_ENTROPY_NOISE_CUTOFF_DEFAULT = 0.01d;
	
	public MSMSFeatureClusteringTask(
			Collection<MSFeatureInfoBundle> msmsFeatures, 
			MSMSClusteringParameterSet params,
			Collection<MinimalMSOneFeature> lookupFeatures) {
		super();
		this.msmsFeatures = msmsFeatures;
		this.params = params;
		this.lookupFeatures = lookupFeatures;
		
		msmsClusterDataSet = new MSMSClusterDataSet(
				"Active data set", 
				"", 
				MRC2ToolBoxCore.getIdTrackerUser());
		msmsClusterDataSet.setParameters(params);	
		featureClusters = msmsClusterDataSet.getClusters();
		rtError = params.getRtErrorValue();
		mzError = params.getMzErrorValue();
		mzErrorType = params.getMassErrorType();
		minMsMsScore = params.getMsmsSimilarityCutoff();
	}

	@Override
	public void run() {
		taskDescription = "Clustering selected MSMS features";
		setStatus(TaskStatus.PROCESSING);
		if(lookupFeatures != null && !lookupFeatures.isEmpty()) {
			try {
				clusterFilteredFeatures();
			}
			catch (Exception e) {
				setStatus(TaskStatus.ERROR);
				e.printStackTrace();
			}
			setStatus(TaskStatus.FINISHED);
		}
		else {
			try {
				clusterAllFeatures();
			}
			catch (Exception e) {
				setStatus(TaskStatus.ERROR);
				e.printStackTrace();
			}
			setStatus(TaskStatus.FINISHED);
		}
	}
	
	private void clusterFilteredFeatures() {

		taskDescription = "Clustering MSMS features based on filter list ...";
		total = lookupFeatures.size();
		processed = 0;
		filteredMsmsFeatures = new HashSet<MSFeatureInfoBundle>();
		
		for(MinimalMSOneFeature b : lookupFeatures) {
			
			Range rtRange = new Range(b.getRt() - rtError, b.getRt() + rtError);
			Range mzRange = MsUtils.createMassRange(b.getMz(), mzError, mzErrorType);
			List<MSFeatureInfoBundle> clusterFeatures = msmsFeatures.stream().
				filter(f -> Objects.nonNull(f.getMsFeature().
						getSpectrum().getExperimentalTandemSpectrum())).
				filter(f -> rtRange.contains(f.getRetentionTime())).
				filter(f -> mzRange.contains(f.getMsFeature().
						getSpectrum().getExperimentalTandemSpectrum().getParent().getMz())).
				collect(Collectors.toList());
			if(clusterFeatures.isEmpty()) {
				processed++;
				continue;
			}	
			while(!clusterFeatures.isEmpty()) {
				MsFeatureInfoBundleCluster newCluster = 
						clusterBasedOnMSMSSimilarity(b, clusterFeatures);
				featureClusters.add(newCluster);				
			}
			processed++;
		}
	}
	
	private MsFeatureInfoBundleCluster clusterBasedOnMSMSSimilarity(
			MinimalMSOneFeature b,
			List<MSFeatureInfoBundle> featuresToCluster) {
		
		if(featuresToCluster.isEmpty())
			return null;
		
		if(featuresToCluster.size() == 1) {		
			MsFeatureInfoBundleCluster newCluster = new MsFeatureInfoBundleCluster(b);
			newCluster.addComponent(featuresToCluster.get(0));
			featuresToCluster.clear();
			return newCluster;
		}
		if(featuresToCluster.size() > 1) {
			
			List<MSFeatureInfoBundle> featuresToRemove = 
					new ArrayList<MSFeatureInfoBundle>();
			MsFeatureInfoBundleCluster newCluster = new MsFeatureInfoBundleCluster(b);
			MSFeatureInfoBundle maxInt = featuresToCluster.get(0);
			double maxArea = maxInt.getMsFeature().getSpectrum().
					getExperimentalTandemSpectrum().getTotalIntensity();
			for(int i=1; i<featuresToCluster.size(); i++) {
				double area = featuresToCluster.get(i).getMsFeature().getSpectrum().
						getExperimentalTandemSpectrum().getTotalIntensity();
				if(area > maxArea) {
					maxArea = area;
					maxInt = featuresToCluster.get(i);
				}
			}
			newCluster.addComponent(maxInt);
			Collection<MsPoint> refMsMs = maxInt.getMsFeature().getSpectrum().
					getExperimentalTandemSpectrum().getSpectrum();
			featuresToRemove.add(maxInt);
			for(int i=0; i<featuresToCluster.size(); i++) {
				
				MSFeatureInfoBundle f = featuresToCluster.get(i);
				if(f.equals(maxInt))
					continue;
				
				Collection<MsPoint>msms = f.getMsFeature().getSpectrum().
						getExperimentalTandemSpectrum().getSpectrum();
				
				double score = MSMSScoreCalculator.calculateEntropyBasedMatchScore(
						msms, refMsMs, mzError, mzErrorType, SPECTRUM_ENTROPY_NOISE_CUTOFF_DEFAULT);
				if(score >= minMsMsScore) {
					newCluster.addComponent(f);
					featuresToRemove.add(f);
				}
			}
			featuresToCluster.removeAll(featuresToRemove);
			return newCluster;
		}		
		return null;
	}
	
	private void clusterAllFeatures() {
		
		taskDescription = "Clustering MSMS features ...";
		total = msmsFeatures.size();
		processed = 0;
		params = msmsClusterDataSet.getParameters();
		boolean added = false;
		for(MSFeatureInfoBundle b : msmsFeatures) {
			
			added = false;
			for(MsFeatureInfoBundleCluster cluster : featureClusters) {
				
				if(cluster.addNewBundle(b, params)) {
					added = true;
					break;
				}
			}	
			if(!added) {
				MsFeatureInfoBundleCluster newCluster = 
						new MsFeatureInfoBundleCluster(b);
				featureClusters.add(newCluster);
			}
			processed++;
		}
	}

	@Override
	public Task cloneTask() {
		
		return new MSMSFeatureClusteringTask(
				msmsFeatures, params, lookupFeatures);
	}

	public Collection<MinimalMSOneFeature> getLookupFeatures() {
		return lookupFeatures;
	}

	public MSMSClusterDataSet getMsmsClusterDataSet() {
		return msmsClusterDataSet;
	}
}






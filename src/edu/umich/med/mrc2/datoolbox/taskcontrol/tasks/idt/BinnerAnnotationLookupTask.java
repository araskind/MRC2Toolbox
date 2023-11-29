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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import edu.umich.med.mrc2.datoolbox.data.BinnerAnnotation;
import edu.umich.med.mrc2.datoolbox.data.BinnerAnnotationCluster;
import edu.umich.med.mrc2.datoolbox.data.MSFeatureInfoBundle;
import edu.umich.med.mrc2.datoolbox.data.MsPoint;
import edu.umich.med.mrc2.datoolbox.data.enums.MassErrorType;
import edu.umich.med.mrc2.datoolbox.data.msclust.BinnerAnnotationLookupDataSet;
import edu.umich.med.mrc2.datoolbox.data.msclust.BinnerBasedMsFeatureInfoBundleCluster;
import edu.umich.med.mrc2.datoolbox.data.msclust.IMSMSClusterDataSet;
import edu.umich.med.mrc2.datoolbox.data.msclust.IMsFeatureInfoBundleCluster;
import edu.umich.med.mrc2.datoolbox.data.msclust.MSMSClusterDataSet;
import edu.umich.med.mrc2.datoolbox.data.msclust.MSMSClusteringParameterSet;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.msmsscore.MSMSScoreCalculator;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.utils.MsUtils;
import edu.umich.med.mrc2.datoolbox.utils.Range;

public class BinnerAnnotationLookupTask extends AbstractTask {
	
	private Collection<MSFeatureInfoBundle> msmsFeatures;
	private MSMSClusteringParameterSet params;
	private BinnerAnnotationLookupDataSet binnerAnnotationsDataSet;
	private Collection<IMsFeatureInfoBundleCluster>featureClusters;
	private IMSMSClusterDataSet msmsClusterDataSet;
	private double rtError;
	private double mzError;
	private MassErrorType mzErrorType;
	private double minMsMsScore;
	private static final double SPECTRUM_ENTROPY_NOISE_CUTOFF_DEFAULT = 0.01d;
	
	public BinnerAnnotationLookupTask(
			Collection<MSFeatureInfoBundle> msmsFeatures, 
			MSMSClusteringParameterSet params,
			BinnerAnnotationLookupDataSet balds) {
		super();
		this.msmsFeatures = msmsFeatures;
		this.params = params;
		this.binnerAnnotationsDataSet = balds;
		String description  = "Based on Binner annotations data set \"" + 
				binnerAnnotationsDataSet.getName() +"\"";
		 
		msmsClusterDataSet = new MSMSClusterDataSet(
				"Binner based MSMS clusters data set (" + 
						MRC2ToolBoxConfiguration.defaultTimeStampFormat.format(new Date()) +")", 
				description, 
				MRC2ToolBoxCore.getIdTrackerUser());
		msmsClusterDataSet.setParameters(params);	
		msmsClusterDataSet.setBinnerAnnotationDataSet(binnerAnnotationsDataSet);
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
		try {
			clusterFilteredFeatures();
		}
		catch (Exception e) {
			setStatus(TaskStatus.ERROR);
			e.printStackTrace();
		}
		setStatus(TaskStatus.FINISHED);		
	}
	
	private void clusterFilteredFeatures() {

		taskDescription = "Clustering MSMS features based on filter list ...";
		Set<BinnerAnnotationCluster> lookupClusters = 
				binnerAnnotationsDataSet.getBinnerAnnotationClusters();
		total = lookupClusters.size();
		processed = 0;
		Map<MSFeatureInfoBundle,Double>featureScoreMap = 
				new HashMap<MSFeatureInfoBundle,Double>();
		
		for(BinnerAnnotationCluster lookupCluster : lookupClusters) {
			
			BinnerBasedMsFeatureInfoBundleCluster newCluster = 
					new BinnerBasedMsFeatureInfoBundleCluster(lookupCluster);
			
			for(BinnerAnnotation binnerAnnotation : lookupCluster.getAnnotations()) {
				
				featureScoreMap.clear();
				Range rtRange = new Range(
						binnerAnnotation.getBinnerRt() - rtError, 
						binnerAnnotation.getBinnerRt() + rtError);
				Range mzRange = MsUtils.createMassRange(binnerAnnotation.getBinnerMz(), mzError, mzErrorType);
				List<MSFeatureInfoBundle> clusterFeatures = msmsFeatures.stream().
					filter(f -> Objects.nonNull(f.getMsFeature().
							getSpectrum().getExperimentalTandemSpectrum())).
					filter(f -> Objects.nonNull(f.getMsFeature().
							getSpectrum().getExperimentalTandemSpectrum().getParent())).
					filter(f -> rtRange.contains(f.getRetentionTime())).
					filter(f -> mzRange.contains(f.getMsFeature().
							getSpectrum().getExperimentalTandemSpectrum().getParent().getMz())).
					collect(Collectors.toList());
				if(clusterFeatures.isEmpty())
					continue;
					
				for(int i=0; i<clusterFeatures.size(); i++) {
					
					MSFeatureInfoBundle fOne = clusterFeatures.get(i);
					featureScoreMap.put(fOne, 0.0d);
					Collection<MsPoint>msmsOne = fOne.getMsFeature().getSpectrum().
							getExperimentalTandemSpectrum().getSpectrum();
					
					for(int j=1; j<clusterFeatures.size(); j++) {
						
						Collection<MsPoint>msmsTwo = clusterFeatures.get(j).getMsFeature().getSpectrum().
								getExperimentalTandemSpectrum().getSpectrum();
						
						double score = MSMSScoreCalculator.calculateEntropyBasedMatchScore(
								msmsOne, msmsTwo, mzError, mzErrorType, SPECTRUM_ENTROPY_NOISE_CUTOFF_DEFAULT);
						
						if(score > featureScoreMap.get(fOne))
							featureScoreMap.replace(fOne, score);
					}
				}
				featureScoreMap.entrySet().stream().
					filter(e -> (e.getValue() >= minMsMsScore)).
					forEach(e -> newCluster.addComponent(binnerAnnotation, e.getKey()));
			}
			if(!newCluster.getComponents().isEmpty())
				featureClusters.add(newCluster);
			
			processed++;
		}
	}

	@Override
	public Task cloneTask() {
		
		return new BinnerAnnotationLookupTask(
				msmsFeatures, params, binnerAnnotationsDataSet);
	}

	public IMSMSClusterDataSet getMSMSClusterDataSet() {
		return msmsClusterDataSet;
	}

	public BinnerAnnotationLookupDataSet getBinnerAnnotationLookupDataSet() {
		return binnerAnnotationsDataSet;
	}
}






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

package edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.id;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.ujmp.core.Matrix;
import org.ujmp.core.calculation.Calculation;

import edu.umich.med.mrc2.datoolbox.data.Adduct;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureCluster;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureIdentity;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.utils.DataSetUtils;

public class CleanupLibrarySerchResultsTask extends AbstractTask {

	private int maxPercentMissing;
	private double maxDistanceBetweenAdducts;
	private int minPercentOfMinorAdduct;
	private double minCorrelationBetweenAdducts;
	private Collection<MsFeatureCluster>featureClusters;
	private Collection<MsFeature>inputFeatures;
	private DataPipeline dataPipeline;
	private Matrix dataSubset;
	private PearsonsCorrelation pearson;
	private List<MsFeature> filteredFeatures;
	
	public CleanupLibrarySerchResultsTask(
			Collection<MsFeature> inputFeatures,
			DataPipeline dataPipeline,
			int maxPercentMissing, 
			double maxDistanceBetweenAdducts,
			int minPercentOfMinorAdduct, 
			double minCorrelationBetweenAdducts) {
		super();
		this.inputFeatures = inputFeatures;
		this.dataPipeline = dataPipeline;
		this.maxPercentMissing = maxPercentMissing;
		this.maxDistanceBetweenAdducts = maxDistanceBetweenAdducts;
		this.minPercentOfMinorAdduct = minPercentOfMinorAdduct;
		this.minCorrelationBetweenAdducts = minCorrelationBetweenAdducts;
		featureClusters = new ArrayList<>();
		pearson = new PearsonsCorrelation();
	}

	@Override
	public void run() {

		taskDescription = "";
		setStatus(TaskStatus.PROCESSING);
		filterFeatures();
		try {
			createFeatureClusters();
		}
		catch (Exception e) {
			reportErrorAndExit(e);
		}
		try {
			cleanFeatureClusters();
		}
		catch (Exception e) {
			reportErrorAndExit(e);
		}
		setStatus(TaskStatus.FINISHED);
	}
	
	private void filterFeatures() {

		double maxMissingness = maxPercentMissing / 100.0d;
		filteredFeatures = inputFeatures.stream().
			filter(MsFeature::isIdentified).
			filter(f -> Objects.nonNull(f.getPrimaryIdentity().getMsRtLibraryMatch())).
			filter(f -> Objects.nonNull(f.getStatsSummary())).
			filter(f -> f.getStatsSummary().getSampleMissingness() < maxMissingness).
			collect(Collectors.toList());
	}

	private void cleanFeatureClusters() {

		if(featureClusters.isEmpty())
			return;
		
		taskDescription = "Cleaning up feature clusters ...";
		total = featureClusters.size();
		processed = 0;
		Matrix sourceMatrix = MRC2ToolBoxCore.getActiveMetabolomicsExperiment().
				getDataMatrixForDataPipeline(dataPipeline);
		dataSubset = DataSetUtils.subsetDataMatrixByFeatures(sourceMatrix,filteredFeatures);
		List<MsFeature>toRemove = new ArrayList<>();
		double minPercentArea = minPercentOfMinorAdduct / 100.d;
		for(MsFeatureCluster cluster : featureClusters) {
			
			toRemove.clear();
			if(cluster.getFeatures().size() > 1) {
				
				MsFeature topFeature = cluster.getMostIntensiveFeature();
				Adduct topAdduct = topFeature.getPrimaryIdentity().
						getMsRtLibraryMatch().getTopAdductMatch().getLibraryMatch();
				double topArea = cluster.getTopArea();
				for(MsFeature f : cluster.getFeatures()) {
					
					if(!f.equals(topFeature)) {
						
						Adduct fAdduct = f.getPrimaryIdentity().
								getMsRtLibraryMatch().getTopAdductMatch().getLibraryMatch();
						if(!fAdduct.equals(topAdduct)) {
							
							double fArea = f.getAveragePeakArea();
							if(fArea / topArea < minPercentArea) {
								toRemove.add(f);
								continue;
							}
							double corr = calculateFeatureCorrelation(topFeature, f);
							if(corr < minCorrelationBetweenAdducts){
								toRemove.add(f);
								continue;
							}
						}
					}
				}
				if(!toRemove.isEmpty())
					cluster.removeFeatures(toRemove);			
			}			
			processed++;
		}		
	}

	private void createFeatureClusters() {
		
		if(filteredFeatures.isEmpty())
			return;
		
		taskDescription = "Filtering and grouping identified features ...";
		total = filteredFeatures.size();
		processed = 0;
		
		MsFeature[]featureArray = filteredFeatures.toArray(new MsFeature[filteredFeatures.size()]);
		MsFeatureCluster firstCluster = new MsFeatureCluster();
		firstCluster.addFeature(featureArray[0], dataPipeline);
		featureClusters.add(firstCluster);
		for(int i=1; i<featureArray.length; i++) {
			
			boolean assigned = false;
			for(MsFeatureCluster c : featureClusters) {				
				
				if(featureBelongs(featureArray[i], c)) {
					c.addFeature(featureArray[i], dataPipeline);
					assigned = true;
				}
			}
			if(!assigned) {
				MsFeatureCluster nextCluster = new MsFeatureCluster();
				nextCluster.addFeature(featureArray[i], dataPipeline);
				featureClusters.add(nextCluster);
			}
			processed++;
		}
	}

	private boolean featureBelongs(MsFeature msFeature, MsFeatureCluster c) {

		if(Math.abs(msFeature.getRetentionTime() - c.getRtRange().getMin()) > maxDistanceBetweenAdducts
				|| Math.abs(msFeature.getRetentionTime() - c.getRtRange().getMax()) > maxDistanceBetweenAdducts)
			return false;
		
		MsFeatureIdentity pid = msFeature.getPrimaryIdentity();
		for(MsFeature f : c.getFeatures()) {
			
			MsFeatureIdentity fid = f.getPrimaryIdentity();
			 if(!fid.getCompoundIdentity().equals(pid.getCompoundIdentity())
					 || Math.abs(msFeature.getRetentionTime() - f.getRetentionTime()) > maxDistanceBetweenAdducts)		
				 return false;
		}
		return true;
	}
	
	private double calculateFeatureCorrelation(MsFeature fOne, MsFeature fTwo) {
		
		long colOneIndex = dataSubset.getColumnForLabel(fOne);
		double[] fOneArray = dataSubset.selectColumns(Calculation.Ret.NEW, colOneIndex)
                .toColumnVector(Calculation.Ret.LINK)
                .toDoubleArray()[0];
		long colTwoIndex = dataSubset.getColumnForLabel(fTwo);
		double[] fTwoArray = dataSubset.selectColumns(Calculation.Ret.NEW, colTwoIndex)
                .toColumnVector(Calculation.Ret.LINK)
                .toDoubleArray()[0];

		return pearson.correlation(fOneArray, fTwoArray);
	}

	@Override
	public Task cloneTask() {

		return new CleanupLibrarySerchResultsTask(
				inputFeatures,
				dataPipeline,
				maxPercentMissing, 
				maxDistanceBetweenAdducts,
				minPercentOfMinorAdduct, 
				minCorrelationBetweenAdducts);
	}

	public Collection<MsFeatureCluster> getFeatureClusters() {
		return featureClusters;
	}

}

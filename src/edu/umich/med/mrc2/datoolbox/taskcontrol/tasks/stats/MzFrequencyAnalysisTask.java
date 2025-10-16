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

package edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.stats;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureCluster;
import edu.umich.med.mrc2.datoolbox.data.MzFrequencyObject;
import edu.umich.med.mrc2.datoolbox.data.compare.MsFeatureClusterComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.MsFeatureComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.MzFrequencyObjectComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.enums.MassErrorType;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.utils.Range;

public class MzFrequencyAnalysisTask extends AbstractTask {
	
	private Collection<MsFeature>featuresToProcess;
	private Collection<MsFeature>featuresToProcessFiltered = 
			new ArrayList<MsFeature>();
	private MzFrequencyType mzFrequencyType;
	private double massWindowSize;
	private MassErrorType massWindowType;
	private Collection<MsFeatureCluster>mzClusters = 
			new TreeSet<MsFeatureCluster>(
					new MsFeatureClusterComparator(SortProperty.MZ));
	private Collection<MzFrequencyObject>mzFrequencyObjects = 
			new TreeSet<MzFrequencyObject>(
					new MzFrequencyObjectComparator(SortProperty.rangeMidpoint));
	private DataPipeline dp = new DataPipeline();
	private Range dataSetRtRange;
	
	public MzFrequencyAnalysisTask(
			Collection<MsFeature> featuresToProcess, 
			MzFrequencyType mzFrequencyType,
			double massWindowSize, 
			MassErrorType massWindowType) {
		super();
		this.featuresToProcess = featuresToProcess;
		this.mzFrequencyType = mzFrequencyType;
		this.massWindowSize = massWindowSize;
		this.massWindowType = massWindowType;
	}

	@Override
	public void run() {
		
		setStatus(TaskStatus.PROCESSING);
		try {
			calculateMzDistribution();
		}
		catch (Exception e) {

			e.printStackTrace();
			errorMessage = e.getMessage();
			setStatus(TaskStatus.ERROR);
return;

		}
		try {
			summarizeData();
		}
		catch (Exception e) {

			e.printStackTrace();
			errorMessage = e.getMessage();
			setStatus(TaskStatus.ERROR);
return;

		}
		setStatus(TaskStatus.FINISHED);
	}

	private void summarizeData() {

		taskDescription = "Creating data summaries";
		total = mzClusters.size();
		processed = 0;
		int totalCount = featuresToProcessFiltered.size();
		if(mzFrequencyType.equals(MzFrequencyType.MS1_BASEPEAK_FREQUENCY)) {
			
			for(MsFeatureCluster cluster : mzClusters) {
				
				MzFrequencyObject mzfo = new MzFrequencyObject(cluster);
				mzfo.setDataSetRtRange(dataSetRtRange);
				mzfo.setFrequency(((double)cluster.getFeatures().size()) / totalCount);
				mzfo.setMzRange(cluster.getBasePeakMzRange(dp));
				mzFrequencyObjects.add(mzfo);
				processed++;
			}
		}
		if(mzFrequencyType.equals(MzFrequencyType.MSMS_PARENT_FREQUENCY)) {
			
			for(MsFeatureCluster cluster : mzClusters) {
				
				MzFrequencyObject mzfo = new MzFrequencyObject(cluster);
				mzfo.setDataSetRtRange(dataSetRtRange);
				mzfo.setFrequency(((double)cluster.getFeatures().size()) / totalCount);
				mzfo.setMzRange(cluster.getMSMSParentIonMzRange(dp));
				mzFrequencyObjects.add(mzfo);
				processed++;
			}
		}
	}

	private void calculateMzDistribution() {

		taskDescription = "Calculating M/Z distribution";
		if(mzFrequencyType.equals(MzFrequencyType.MS1_BASEPEAK_FREQUENCY)) {
			
			featuresToProcessFiltered = featuresToProcess.stream().
					filter(f -> Objects.nonNull(f.getSpectrum())).
					sorted(new MsFeatureComparator(SortProperty.BasePeakMZ)).
					collect(Collectors.toList());
			
			if(featuresToProcessFiltered.isEmpty())
				return;
			
			calculateDataSetRtRange(featuresToProcessFiltered);
			createFeatureClustersBasedOnBasePeak();
		}
		if(mzFrequencyType.equals(MzFrequencyType.MSMS_PARENT_FREQUENCY)) {
			
			featuresToProcessFiltered = featuresToProcess.stream().
				filter(f -> Objects.nonNull(f.getSpectrum())).
				filter(f -> Objects.nonNull(f.getSpectrum().getExperimentalTandemSpectrum())).
				filter(f -> Objects.nonNull(f.getSpectrum().getExperimentalTandemSpectrum().getParent())).
				sorted(new MsFeatureComparator(SortProperty.ParentIonMZ)).
				collect(Collectors.toList()); 
			
			if(featuresToProcessFiltered.isEmpty())
				return;
			
			calculateDataSetRtRange(featuresToProcessFiltered);
			createFeatureClustersBasedOnParentIon();
		}
	}
	
	private void calculateDataSetRtRange(Collection<MsFeature>dataSet) {
		
		DescriptiveStatistics ds = new DescriptiveStatistics();
		dataSet.stream().
			forEach(f -> ds.addValue(f.getRetentionTime()));
		
		dataSetRtRange = new Range(ds.getMin(), ds.getMax());
	}
	
	private void createFeatureClustersBasedOnBasePeak() {
		
		total = featuresToProcessFiltered.size();
		processed = 0;
		boolean assigned = false;				
		for(MsFeature cf : featuresToProcessFiltered) {
			
			assigned = false;			
			for(MsFeatureCluster cluster : mzClusters) {
				
				if(cluster.matchesPrimaryFeatureOnBasePeakMz(cf, dp, massWindowSize, massWindowType)) {
					
					cluster.addFeature(cf, dp);
					assigned = true;
					break;
				}
			}
			if(!assigned) {
				MsFeatureCluster newCluster = new MsFeatureCluster();
				newCluster.addFeature(cf, dp);
				mzClusters.add(newCluster);
			}
			processed++;
		}
	}
	
	private void createFeatureClustersBasedOnParentIon() {
		
		total = featuresToProcessFiltered.size();
		processed = 0;
		boolean assigned = false;	
		for(MsFeature cf : featuresToProcessFiltered) {
			
			assigned = false;
			MsFeatureCluster match = mzClusters.stream().
					filter(c -> c.matchesPrimaryFeatureOnMSMSParentIonMz(cf, dp, massWindowSize, massWindowType)).
					findFirst().orElse(null);
				
			if(match != null) {					
				match.addFeature(cf, dp);
			} else {
				MsFeatureCluster newCluster = new MsFeatureCluster();
				newCluster.addFeature(cf, dp);
				mzClusters.add(newCluster);
			}
			processed++;
		}
	}

	@Override
	public Task cloneTask() {

		return new MzFrequencyAnalysisTask(
				featuresToProcess, 
				mzFrequencyType,
				massWindowSize, 
				massWindowType) ;
	}

	public MzFrequencyType getMzFrequencyType() {
		return mzFrequencyType;
	}

	public double getMassWindowSize() {
		return massWindowSize;
	}

	public MassErrorType getMassWindowType() {
		return massWindowType;
	}

	public Collection<MzFrequencyObject> getMzFrequencyObjects() {
		return mzFrequencyObjects;
	}
}

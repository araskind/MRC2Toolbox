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

package edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.stats;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureCluster;
import edu.umich.med.mrc2.datoolbox.data.compare.MsFeatureComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortDirection;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.enums.MassErrorType;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.mzdelta.MZDeltaAnalysisParametersObject;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.utils.MsUtils;
import edu.umich.med.mrc2.datoolbox.utils.Range;

public class MZDeltaAnalysisTask extends AbstractTask {
	
	private MZDeltaAnalysisParametersObject parameters;
	private Set<MsFeatureCluster>featureClusters;
	private Set<Double>massDifferences;
	private Set<Double>rtSeriesMassSet;
	private DataPipeline dataPipeline;
	private double anchorMassError;
	private MassErrorType anchorMassErrorType;
	private double anchorRTError;
	private double rtSeriesMassError;
	private MassErrorType rtSeriesMassErrorType;
	private double rtSeriesMinStep;
	private MsFeatureComparator rtSorter;
	private MsFeatureComparator reverseIntensitySorter;
	private Set<MsFeature>assignedFeatures;
	
	public MZDeltaAnalysisTask(MZDeltaAnalysisParametersObject parameters) {
		super();
		this.parameters = parameters;
		dataPipeline = parameters.getDataPipeline();
		
		anchorMassError = parameters.getAnchorMassError();
		anchorMassErrorType = parameters.getAnchorMassErrorType();
		rtSeriesMassSet = parameters.getRtSeriesMassSet();
		anchorRTError = parameters.getAnchorRTError();
		rtSeriesMassError = parameters.getRtSeriesMassError();
		rtSeriesMassErrorType = parameters.getRtSeriesMassErrorType();
		rtSeriesMinStep = parameters.getRtSeriesMinStep();
		rtSorter = new MsFeatureComparator(SortProperty.RTmedObserved);
	}

	@Override
	public void run() {
	
		setStatus(TaskStatus.PROCESSING);
		createMassDifferenceSet();
		try {
			findFeatureClusters();
		}
		catch (Exception e) {
			reportErrorAndExit(e);
		}
		setStatus(TaskStatus.FINISHED);
	}

	private void createMassDifferenceSet() {

		massDifferences = new TreeSet<>();
		double[]anchors = parameters.getAnchorMassSet().stream().
				mapToDouble(Double::doubleValue).toArray();
		for(int i=0; i<anchors.length-1; i++) {
			
			for(int j=1; j<anchors.length; j++)
				massDifferences.add(Math.abs(anchors[i] - anchors[j]));			
		}		
	}

	private void findFeatureClusters() {

		taskDescription = "Creating feature clusters";
		featureClusters = new HashSet<>();
		assignedFeatures = new HashSet<>();
		Collection<MsFeature> features = parameters.getFeatureSet().getFeatures();
		Set<Range> anchorMassRangeSet = createMassRangeSet(parameters.getAnchorMassSet(), anchorMassError,
				anchorMassErrorType);
		Collection<MsFeatureCluster> anchorClusters = 
				findMsFeatureCluster(features, anchorMassRangeSet);
		featureClusters.addAll(anchorClusters);
		anchorClusters.stream().
			flatMap(c -> c.getFeturesForDataPipeline(dataPipeline).stream()).
			forEach(f -> assignedFeatures.add(f));
		// Find other contaminant clusters up and down the RT
		for(double deltaMass : rtSeriesMassSet) {
			
			findAdditionalClusters(anchorClusters, deltaMass, SortDirection.ASC);
			findAdditionalClusters(anchorClusters, deltaMass, SortDirection.DESC);
		}
		System.out.println("***");
	}
	
	private void findAdditionalClusters(
			Collection<MsFeatureCluster> anchorClusters, 
			double deltaMass,
			SortDirection direction) {
		
		Collection<MsFeatureCluster>newClusters = new ArrayList<>();
		for(MsFeatureCluster anchorCluster : anchorClusters) {
					
			Set<Double>lookupMassSet = createLookupMassSet(anchorCluster, deltaMass, direction);
			System.out.println("\nNext Mass Set:");
			for(double mz : lookupMassSet)
				System.out.println(MRC2ToolBoxConfiguration.getMzFormat().format(mz));
			
			System.out.println("\n_________________");
			
			Collection<MsFeature> features = new ArrayList<>();
			Set<Range>lookupMassRangeSet = createMassRangeSet(
					lookupMassSet, 
					anchorMassError, 
					anchorMassErrorType);
			double deltaRt = rtSeriesMinStep;
			double rtCutoff = anchorCluster.getRtRange(dataPipeline).getAverage();
			if(direction.equals(SortDirection.DESC)) {
				double rtc = rtCutoff - deltaRt;
				features = parameters.getFeatureSet().getFeatures().stream().
						filter(f -> f.getRetentionTime() < rtc).
						filter(f -> !assignedFeatures.contains(f)).
						sorted(rtSorter).collect(Collectors.toList());
			}
			else {
				double rtc = rtCutoff + deltaRt;
				features = parameters.getFeatureSet().getFeatures().stream().
						filter(f -> f.getRetentionTime() > rtc).
						filter(f -> !assignedFeatures.contains(f)).
						sorted(rtSorter).collect(Collectors.toList());
			}
			Collection<MsFeatureCluster> newClusterSubset = findMsFeatureCluster(features, lookupMassRangeSet);
			if(!newClusterSubset.isEmpty()) {
				newClusters.addAll(newClusterSubset);
				featureClusters.addAll(newClusterSubset);
				newClusterSubset.stream().
					flatMap(c -> c.getFeturesForDataPipeline(dataPipeline).stream()).
					forEach(f -> assignedFeatures.add(f));
			}
		}	
		if(!newClusters.isEmpty())
			findAdditionalClusters(newClusters, deltaMass, direction);	
	}
	
	private Set<Double>createLookupMassSet(MsFeatureCluster cluster, double deltaMass, SortDirection direction){
		
		Set<Double>lookupMassSet = new TreeSet<>();
		if((deltaMass > 0.0d && direction.equals(SortDirection.DESC)) 
				|| (deltaMass < 0.0d && direction.equals(SortDirection.ASC)))
			deltaMass = deltaMass * -1.0d;
		
		final double delta = deltaMass;
		cluster.getMonoisotopicMzSet(dataPipeline).forEach(m -> lookupMassSet.add(m + delta));		
		return lookupMassSet;
	}
	
	//	TODO allow missing in series?
	private Collection<MsFeatureCluster> findMsFeatureCluster(
			Collection<MsFeature> features, 
			Set<Range>anchorMassRangeSet) {
		
		List<MsFeatureCluster>clusters = new ArrayList<>();
		Map<Range,List<MsFeature>>featuresByRange = new TreeMap<>();
		int maxCount = 0;
		Range maxCountRange = null;
		for(Range r : anchorMassRangeSet) {
			List<MsFeature> inRange = features.stream().
					filter(f -> r.contains(f.getMonoisotopicMz())).
					collect(Collectors.toList());
			if(inRange.isEmpty())
				return clusters;
			
			if(inRange.size() > maxCount) {
				maxCount = inRange.size();
				maxCountRange = r;
			}			
			featuresByRange.put(r, inRange);
		}		
		Set<Range>secondaryRanges = new TreeSet<>(anchorMassRangeSet);
		secondaryRanges.remove(maxCountRange);
		for(MsFeature f : featuresByRange.get(maxCountRange)) {
			
			Range refRtRange = new 
					Range(f.getMedianObservedRetention() - anchorRTError, 
							f.getMedianObservedRetention() + anchorRTError);
			MsFeatureCluster cluster = new MsFeatureCluster();
			cluster.addFeature(f, dataPipeline);
			int countAdded = 0;
			for(Range r : secondaryRanges) {	
				
				List<MsFeature>inRtRange =  featuresByRange.get(r).stream().
						filter(msf -> refRtRange.contains(msf.getMedianObservedRetention())).
						collect(Collectors.toList());
				if(!inRtRange.isEmpty()) {
					
					for(MsFeature sf : inRtRange) {
						
						//	TODO if more than 1 match - best RT match
						cluster.addFeature(sf, dataPipeline);
						countAdded++; 						
					}
				}
			}
			if(countAdded == secondaryRanges.size())
				clusters.add(cluster);
		}		
		return clusters;
	}
	
	private Set<Range>createMassRangeSet(
			Set<Double>masses, double massError, MassErrorType massErrorType){
		
		Set<Range>rangeSet = new TreeSet<>();
		for(Double mass : masses)
			rangeSet.add(MsUtils.createMassRange(mass, massError, massErrorType));
		
		return rangeSet;
	}

	@Override
	public Task cloneTask() {
		return new MZDeltaAnalysisTask(parameters);
	}

	public Collection<MsFeatureCluster> getFeatureClusters() {
		return featureClusters;
	}

	public MZDeltaAnalysisParametersObject getParameters() {
		return parameters;
	}
}

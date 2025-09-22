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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureCluster;
import edu.umich.med.mrc2.datoolbox.data.enums.MassErrorType;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.mzdelta.MZDeltaAnalysisParametersObject;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.utils.MsUtils;
import edu.umich.med.mrc2.datoolbox.utils.Range;

public class MZDeltaAnalysisTask extends AbstractTask {
	
	private MZDeltaAnalysisParametersObject parameters;
	private Collection<MsFeatureCluster>featureClusters;
	private Set<Double>massDifferences;
	private DataPipeline dataPipeline;
	private double anchorMassError;
	private MassErrorType anchorMassErrorType;
	private double anchorRTError;
	private double rtSeriesMassError;
	private MassErrorType rtSeriesMassErrorType;
	private double rtSeriesMinStep;
	

	public MZDeltaAnalysisTask(MZDeltaAnalysisParametersObject parameters) {
		super();
		this.parameters = parameters;
		dataPipeline = parameters.getDataPipeline();
		
		anchorMassError = parameters.getAnchorMassError();
		anchorMassErrorType = parameters.getAnchorMassErrorType();
		anchorRTError = parameters.getAnchorRTError();
		rtSeriesMassError = parameters.getRtSeriesMassError();
		rtSeriesMassErrorType = parameters.getRtSeriesMassErrorType();
		rtSeriesMinStep = parameters.getRtSeriesMinStep();
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
			
			for(int j=1; j<anchors.length; j++) {
				massDifferences.add(Math.abs(anchors[i] - anchors[j]));
			}
		}		
	}

	private void findFeatureClusters() {

		taskDescription = "Creating feature clusters";
		featureClusters = new ArrayList<>();
		Collection<MsFeature> features = parameters.getFeatureSet().getFeatures();
		Set<Range>anchorMassRangeSet = createMassRangeSet(
				parameters.getAnchorMassSet(), 
				anchorMassError, 
				anchorMassErrorType);
		Collection<MsFeatureCluster> anchorClusters = 
				findMsFeatureCluster(features, anchorMassRangeSet, anchorRTError);
		
		System.out.println("***");
	}
	
	//	TODO allow missing in series?
	private Collection<MsFeatureCluster> findMsFeatureCluster(
			Collection<MsFeature> features, 
			Set<Range>anchorMassRangeSet,
			double anchorRtError) {
		
		List<MsFeatureCluster>clusters = new ArrayList<>();
		Map<Range,List<MsFeature>>featuresByRange = new TreeMap<>();
		int maxCount = 0;
		Range maxCountRange = null;
		for(Range r : anchorMassRangeSet) {
			List<MsFeature> inRange = features.stream().
					filter(f -> r.contains(f.getMonoisotopicMz())).
					collect(Collectors.toList());
			if(inRange.isEmpty())
				return null;
			
			if(inRange.size() > maxCount) {
				maxCount = inRange.size();
				maxCountRange = r;
			}			
			featuresByRange.put(r, inRange);
		}		
		Set<Range>secondaryRanges = new TreeSet<>(anchorMassRangeSet);
		secondaryRanges.remove(maxCountRange);
		for(MsFeature f : featuresByRange.get(maxCountRange)) {
			
			MsFeatureCluster cluster = new MsFeatureCluster();
			cluster.addFeature(f, dataPipeline);
			int countAdded = 0;
			for(Range r : secondaryRanges) {				
				
				for(MsFeature sf : featuresByRange.get(r)) {
					
					//	TODO if more than 1 match - best RT match
					if(cluster.matchesOnRt(sf, dataPipeline, anchorRtError)) {
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
}

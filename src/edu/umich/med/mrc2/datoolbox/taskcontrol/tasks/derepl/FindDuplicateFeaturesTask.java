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

package edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.derepl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Collectors;

import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureCluster;
import edu.umich.med.mrc2.datoolbox.data.compare.MsFeatureComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.enums.MassErrorType;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.utils.ClusterUtils;

public class FindDuplicateFeaturesTask extends AbstractTask {

	private DataPipeline dataPipeline;
	private DataAnalysisProject currentExperiment;
	private double rtWindow;
	private double massAccuracy;
	private Collection<MsFeature> featureList;
	private Collection<MsFeatureCluster> duplicateList;

	public FindDuplicateFeaturesTask(
			DataAnalysisProject experiment,
			DataPipeline dataPipeline,
			double massAccuracy,
			double rtWindow) {

		this.dataPipeline = dataPipeline;
		this.currentExperiment = experiment;
		this.massAccuracy = massAccuracy;
		this.rtWindow = rtWindow;

		taskDescription = "Finding duplicate features for " + 
				dataPipeline.getName();
	}

	@Override
	public void run() {

		setStatus(TaskStatus.PROCESSING);
		try {
			sortFeaturesByRetention();
			findDuplicateFeatures();
			calculateDuplicateScores();
		} catch (Exception e) {

			e.printStackTrace();
			setStatus(TaskStatus.ERROR);
			return;
		}
		setStatus(TaskStatus.FINISHED);
	}

	private void sortFeaturesByRetention() {

		total = 100;
		processed = 10;
		taskDescription = "Sorting features by retention";
		featureList =
				currentExperiment.getMsFeaturesForDataPipeline(dataPipeline).stream().
				filter(f -> f.isPresent()).sorted(new MsFeatureComparator(SortProperty.RT)).
				collect(Collectors.toList());
	}

	public Collection<MsFeatureCluster> findDuplicateFeatures() {

		total = featureList.size();
		processed = 0;
		taskDescription = "Finding duplicates";
		duplicateList = new HashSet<MsFeatureCluster>();
		Collection<MsFeature>assignedFeatures = new HashSet<MsFeature>();
		ArrayList<MsFeatureCluster> clusters = new ArrayList<MsFeatureCluster>();				
		analyzeDuplicates(assignedFeatures, clusters);
		
//		if(duplicateList.isEmpty())
//			return duplicateList;
//		
//		taskDescription = "Finding duplicates, path 2";
//		assignedFeatures = 
//				duplicateList.stream().flatMap(c -> c.getFeatures().stream()).
//				collect(Collectors.toSet());
//		
//		//	Second path
//		analyzeDuplicates(assignedFeatures, duplicateList);
		
		return duplicateList;
	}
	
	private void analyzeDuplicates(
			Collection<MsFeature>assignedFeatures, 
			Collection<MsFeatureCluster> clusters) {
		
		total = featureList.size();
		processed = 0;
		boolean assigned = false;
		for (MsFeature cf : featureList) {
			
			if(isCanceled())
				return;
			
			//	DEBUG - print feature stats if MZ standard deviation > 0.1
//			if(cf.getStatsSummary() != null 
//					&& cf.getStatsSummary().getMzStatistics().getStandardDeviation() > 0.1d) {
//				System.out.println(cf.getName() + " | Charge = "+ cf.getCharge());
//				//	DebugUtils.printMsFeatureStatisticalSummary(cf.getStatsSummary());
//			}			
			assigned = false;
			if(assignedFeatures.contains(cf))
				continue;

			for (MsFeatureCluster fClust : clusters) {

				if (fClust.matches(
						cf, dataPipeline, massAccuracy, MassErrorType.ppm, rtWindow)) {

					fClust.addFeature(cf, dataPipeline);
					assignedFeatures.add(cf);
					assigned = true;
					break;
				}
			}
			if (!assigned) {

				MsFeatureCluster newCluster = new MsFeatureCluster();
				newCluster.addFeature(cf, dataPipeline);
				assignedFeatures.add(cf);
				clusters.add(newCluster);
			}
			processed++;
		}
		duplicateList = clusters.stream().
				filter(c -> c.getFeatures().size() > 1).
				collect(Collectors.toList());
		
		//	DEBUG print 
		Collection<MsFeatureCluster>chargeMismatched = 
				duplicateList.stream().
				filter(c -> c.hasChargeMismatch()).
				collect(Collectors.toList());
//		if(!chargeMismatched.isEmpty()) {
//			
//			System.out.println("\n*************************");
//			System.out.println("Stats for charge-mismatched");
//			for(MsFeatureCluster clust :chargeMismatched) {
//				System.out.println("");
//				System.out.println(clust.toString());
//				for(MsFeature f : clust.getFeatures())
//					DebugUtils.printMsFeatureStatisticalSummary(f.getStatsSummary());
//				
//				System.out.println("\n*************************\n");
//			}
//		}
	}

	private void calculateDuplicateScores() {

		total = duplicateList.size();
		processed = 0;

		taskDescription = "Calculating duplicate scores";
		HashSet<MsFeature> named = new HashSet<MsFeature>();
		HashSet<MsFeature> unknown = new HashSet<MsFeature>();
		for (MsFeatureCluster fClust : duplicateList) {

			named.clear();
			unknown.clear();
			MsFeature primary = null;
			MsFeature dataSource = null;

			for (MsFeature cf : fClust.getFeatures()) {

				if (cf.isIdentified())
					named.add(cf);
				else
					unknown.add(cf);
			}
			// Choose best unknown if no named
			if (named.isEmpty())
				primary = ClusterUtils.getMostIntensiveFeature(unknown);

			// Only one named
			if (named.size() == 1)
				primary = named.iterator().next();

			// More than one named
			if (named.size() > 1)
				primary = ClusterUtils.getMostIntensiveFeature(named);
			
			if(primary == null)
				primary = fClust.getFeatures().iterator().next();

			fClust.setPrimaryFeature(primary);
			dataSource = ClusterUtils.getMostIntensiveFeature(fClust);
			for (MsFeature cf : fClust.getFeatures()) {

				if (!cf.equals(dataSource))
					fClust.setFeatureEnabled(cf, false);
			}
			processed++;
		}
	}

	public DataPipeline getDataPipeline() {
		return dataPipeline;
	}

	public Collection<MsFeatureCluster> getDuplicateList() {
		return duplicateList;
	}

	@Override
	public Task cloneTask() {

		return new FindDuplicateFeaturesTask(
				currentExperiment, dataPipeline, massAccuracy, rtWindow);
	}
}

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

package edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.derepl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import edu.umich.med.mrc2.datoolbox.data.DoubleValueBin;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureCluster;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.utils.Range;

public class MassDifferenceExtractionTask extends AbstractTask {

	private Set<MsFeatureCluster> clusters;
	private Range massDiffRange;
	private double binningWindow;
	private Collection<DoubleValueBin>massDifferenceBins;
	private Map<Range,Integer>countsByRange;
	private double[][] binsHistogram;
	private int minFrequency;
	private	int maxClusterSize;

	public MassDifferenceExtractionTask(
			Set<MsFeatureCluster> clusters,
			Range massDiffRange,
			double binningWindow,
			int minFrequency,
			int maxClusterSize) {

		super();
		this.clusters = clusters;
		this.massDiffRange = massDiffRange;
		this.binningWindow = binningWindow;
		this.minFrequency = minFrequency;
		this.maxClusterSize = maxClusterSize;
	}

	@Override
	public void run() {

		setStatus(TaskStatus.PROCESSING);

		taskDescription = "Extracting mass difference statistics ...";
		total = 100;
		processed = 20;

		massDifferenceBins = new ArrayList<DoubleValueBin>();
		Collection<Double>massDifferences = new ArrayList<Double>();
		Set<DataPipeline>pipelines = clusters.stream().
				flatMap(c -> c.getFeatureMap().keySet().stream()).
				distinct().collect(Collectors.toSet());
		
		for(DataPipeline pl : pipelines) {
			
			clusters.stream().filter(c -> c.getFeatureMap().get(pl).size() <= maxClusterSize).
				forEach(c -> massDifferences.addAll(c.getMassDifferencesWithinRange(massDiffRange, pl)));
		}
		double[] diffs = massDifferences.stream().sorted().
				mapToDouble(Double::doubleValue).toArray();

		taskDescription = "Creating mass difference histogram ...";
//		binsHistogram = StatUtils.histogramBins(diffs, binningWindow/1000.0d, massDiffRange.getMin(), massDiffRange.getMax());

		massDifferenceBins.add(new DoubleValueBin(diffs[0], binningWindow/1000.0d));
		total = diffs.length;
		processed = 0;
		for(int i=1; i<diffs.length; i++) {

			boolean binned = false;
			for(DoubleValueBin bin : massDifferenceBins) {
				binned = bin.addValue(diffs[i]);
				if(binned)
					break;
			}
			if(!binned)
				massDifferenceBins.add(new DoubleValueBin(diffs[i], binningWindow/1000.0d));

			processed = i;
		}
		massDifferenceBins =  massDifferenceBins.stream().
				filter(b -> b.getStatistics().getN() >= minFrequency).
				collect(Collectors.toList());
		setStatus(TaskStatus.FINISHED);
	}

	@Override
	public Task cloneTask() {
		// TODO Auto-generated method stub
		return new MassDifferenceExtractionTask(clusters, massDiffRange, binningWindow, minFrequency, maxClusterSize);
	}

	/**
	 * @return the massDifferenceBins
	 */
	public Collection<DoubleValueBin> getMassDifferenceBins() {
		return massDifferenceBins;
	}
}

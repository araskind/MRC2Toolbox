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

import java.util.Collection;
import java.util.Set;

import edu.umich.med.mrc2.datoolbox.data.Adduct;
import edu.umich.med.mrc2.datoolbox.data.ModificationBlock;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureCluster;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.main.AdductManager;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.utils.MsUtils;
import edu.umich.med.mrc2.datoolbox.utils.Range;

public class MassDifferenceAssignmentTask extends AbstractTask {

	private Collection<Adduct> experimentChemMods;
	private Set<MsFeatureCluster> featureClusters;
	private double massAccuracy;
	private DataAnalysisProject experiment;
	private DataPipeline dataPipeline;

	public MassDifferenceAssignmentTask(double massAccuracy) {
		super();
		this.massAccuracy = massAccuracy;

		experiment = MRC2ToolBoxCore.getActiveMetabolomicsExperiment();
		dataPipeline = experiment.getActiveDataPipeline();
		featureClusters = 
				experiment.getMsFeatureClustersForDataPipeline(dataPipeline);
		experimentChemMods = AdductManager.getNeutralModifications();
		
		taskDescription = "Assigning mass differences ...";
	}

	@Override
	public Task cloneTask() {

		return new MassDifferenceAssignmentTask(massAccuracy);
	}

	@Override
	public void run() {

		setStatus(TaskStatus.PROCESSING);

		processed = 0;
		total = featureClusters.size();
		double massDiff, refMass;
		Range massRange;

		try {
			for (MsFeatureCluster cluster : featureClusters) {

				cluster.getChemicalModificationsMap().clear();

				MsFeature[] features = cluster.getFeatures().toArray(new MsFeature[cluster.getFeatures().size()]);

				for (int i = 0; i < features.length; i++) {

					for (int j = 0; j < features.length; j++) {

						if (features[i].getCharge() == features[j].getCharge() && i != j) {

							massDiff = (features[i].getMonoisotopicMz() * features[i].getAbsoluteObservedCharge()
									- features[j].getMonoisotopicMz() * features[j].getAbsoluteObservedCharge());

							refMass = (double) Math.max(
									features[i].getMonoisotopicMz() * features[i].getAbsoluteObservedCharge(),
									features[j].getMonoisotopicMz() * features[j].getAbsoluteObservedCharge());

							for (Adduct mod : experimentChemMods) {

								massRange = MsUtils.createMassRangeWithReference(mod.getMassCorrection(), refMass,
										massAccuracy);

								if (massRange.contains(massDiff)) {

									ModificationBlock newModBlock = new ModificationBlock(features[i], features[j],
											mod);
									cluster.addModificationBlock(newModBlock);
								}
							}
						}
					}
				}
				processed++;
			}
			setStatus(TaskStatus.FINISHED);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			setStatus(TaskStatus.ERROR);
		}
		return;
	}

}

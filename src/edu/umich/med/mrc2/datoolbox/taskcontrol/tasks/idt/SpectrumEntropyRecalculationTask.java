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
import java.util.Objects;
import java.util.stream.Collectors;

import edu.umich.med.mrc2.datoolbox.data.MSFeatureInfoBundle;
import edu.umich.med.mrc2.datoolbox.data.ReferenceMsMsLibraryMatch;
import edu.umich.med.mrc2.datoolbox.data.TandemMassSpectrum;
import edu.umich.med.mrc2.datoolbox.msmsscore.MSMSScoreCalculator;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.utils.MsUtils;

public class SpectrumEntropyRecalculationTask extends AbstractTask {

	private Collection<MSFeatureInfoBundle>msmsFeatureBundles;
	
	public SpectrumEntropyRecalculationTask(Collection<MSFeatureInfoBundle> msmsFeatureBundles) {
		super();
		this.msmsFeatureBundles = msmsFeatureBundles;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		taskDescription = "Retrieving data from database ...";
		setStatus(TaskStatus.PROCESSING);
		total = msmsFeatureBundles.size();
		processed = 0;

		try {
			for(MSFeatureInfoBundle b :  msmsFeatureBundles) {
				
				processed++;
				if(b.getMsFeature().getSpectrum()  == null) 
					continue;
					
				TandemMassSpectrum msms = 
						b.getMsFeature().getSpectrum().getExperimentalTandemSpectrum();
				if(msms != null) {
					double entropy = 
							MsUtils.calculateCleanedSpectrumEntropyNatLog(msms.getSpectrum());
					msms.setEntropy(entropy);
					
					List<ReferenceMsMsLibraryMatch> matches = 
						b.getMsFeature().getIdentifications().stream().
							filter(id -> Objects.nonNull(id.getReferenceMsMsLibraryMatch())).
							map(id -> id.getReferenceMsMsLibraryMatch()).
							collect(Collectors.toList());
					
					if(!matches.isEmpty()) {
						
						for(ReferenceMsMsLibraryMatch match : matches) {
							match.setEntropyBasedScore(
							MSMSScoreCalculator.calculateDefaultEntropyMatchScore(msms, match));
						}
					}
				}
			}			
			setStatus(TaskStatus.FINISHED);
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			setStatus(TaskStatus.ERROR);
		}
		setStatus(TaskStatus.FINISHED);
	}

	@Override
	public Task cloneTask() {
		return new SpectrumEntropyRecalculationTask(msmsFeatureBundles);
	}
}

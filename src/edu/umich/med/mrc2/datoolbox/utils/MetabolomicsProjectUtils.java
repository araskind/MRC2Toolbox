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

package edu.umich.med.mrc2.datoolbox.utils;

import edu.umich.med.mrc2.datoolbox.data.MsFeatureSet;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class MetabolomicsProjectUtils {

	public static void switchActiveMsFeatureSet(MsFeatureSet selectedSet) {
		
		DataAnalysisProject currentProject = 
				MRC2ToolBoxCore.getActiveMetabolomicsExperiment();
		if(currentProject == null)
			return;
		
		DataPipeline activeDataPipeline = currentProject.getActiveDataPipeline();
		if(activeDataPipeline ==  null)
			return;
		
		if(selectedSet.isActive())
			return;
		
		currentProject.getMsFeatureSetsForDataPipeline(activeDataPipeline).stream().forEach(s -> {
			s.setSuppressEvents(true);
			s.setActive(false);
			s.setSuppressEvents(false);
		});
		selectedSet.setActive(true);
	}
}

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

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.database.idt.ReferenceSamplesManager;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class ProjectUtils {

	public static boolean designValidForStats(
			DataAnalysisProject currentProject,
			DataPipeline activePipeline,
			boolean requirePooled) {

		int pooledCount = 0;
		int sampleCount = 0;

		//	TODO deal with by-batch stats
		for (DataFile df : currentProject.getDataFilesForAcquisitionMethod(
				activePipeline.getAcquisitionMethod())) {

			if(df.getParentSample().hasLevel(ReferenceSamplesManager.masterPoolLevel))
				pooledCount++;

			if(df.getParentSample().hasLevel(ReferenceSamplesManager.sampleLevel))
				sampleCount++;
		}
		if (requirePooled && pooledCount == 0)
			return false;

		if (sampleCount > 0)
			return true;

		return false;
	}
}

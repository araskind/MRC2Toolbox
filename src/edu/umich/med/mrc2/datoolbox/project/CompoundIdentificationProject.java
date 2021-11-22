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

package edu.umich.med.mrc2.datoolbox.project;

import java.io.File;
import java.util.Date;

import edu.umich.med.mrc2.datoolbox.data.lims.LIMSExperiment;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;

public class CompoundIdentificationProject extends DataAnalysisProject{

	/**
	 *
	 */
	private static final long serialVersionUID = -2049061925890261338L;

	public CompoundIdentificationProject(
			String projectName,
			String projectDescription,
			File parentDirectory,
			LIMSExperiment idTrackerExperiment) {

		super(projectName, projectDescription, parentDirectory);

		this.limsExperiment = idTrackerExperiment;
		dateCreated = new Date();
		lastModified = new Date();

		projectDirectory = new File(parentDirectory + File.separator + projectName);
		projectFile = new File(projectDirectory.getAbsolutePath() + File.separator + projectName + "."
				+ MRC2ToolBoxConfiguration.ID_PROJECT_FILE_EXTENSION);

		exportsDirectory = new File(
				projectDirectory.getAbsolutePath() + File.separator + MRC2ToolBoxConfiguration.DATA_EXPORT_DIRECTORY);

		if (!createProjectDirectory(projectDirectory)) {
			MessageDialog.showWarningMsg("Failed to create project directory");
			return;
		}
		if (!createProjectDirectory(exportsDirectory)) {
			MessageDialog.showWarningMsg("Failed to create exports directory");
			return;
		}
		else {
			initNewProject();
		}
	}
}


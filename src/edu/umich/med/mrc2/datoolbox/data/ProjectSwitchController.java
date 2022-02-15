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

package edu.umich.med.mrc2.datoolbox.data;

import edu.umich.med.mrc2.datoolbox.data.lims.LIMSExperiment;
import edu.umich.med.mrc2.datoolbox.project.ProjectType;

public class ProjectSwitchController {

	private boolean saveCurrentProject;
	private boolean exitProgram;
	private ProjectType activeProjectType;
	private ProjectType newProjectType;
	private ProjectState projectState;
	private LIMSExperiment limsExperiment;
	
	public enum ProjectState{
		NEW_PROJECT,
		EXISTING_PROJECT,
		CLOSING_PROJECT,
		;
	}
	
	public ProjectSwitchController() {
		super();
		// TODO Auto-generated constructor stub
	}

	public ProjectSwitchController(
			boolean saveCurrentProject, 
			ProjectState projectState,
			boolean exitProgram,
			ProjectType activeProjectType, 
			ProjectType newProjectType) {
		super();
		this.saveCurrentProject = saveCurrentProject;
		this.projectState = projectState;
		this.exitProgram = exitProgram;
		this.activeProjectType = activeProjectType;
		this.newProjectType = newProjectType;
	}

	public boolean doSaveCurrentProject() {
		return saveCurrentProject;
	}

	public void setSaveCurrentProject(boolean saveCurrentProject) {
		this.saveCurrentProject = saveCurrentProject;
	}

	public boolean isExitProgram() {
		return exitProgram;
	}

	public void setExitProgram(boolean exitProgram) {
		this.exitProgram = exitProgram;
	}

	public ProjectType getActiveProjectType() {
		return activeProjectType;
	}

	public void setActiveProjectType(ProjectType activeProjectType) {
		this.activeProjectType = activeProjectType;
	}

	public ProjectType getNewProjectType() {
		return newProjectType;
	}

	public void setNewProjectType(ProjectType newProjectType) {
		this.newProjectType = newProjectType;
	}

	public ProjectState getProjectState() {
		return projectState;
	}

	public void setProjectState(ProjectState projectState) {
		this.projectState = projectState;
	}

	public LIMSExperiment getLimsExperiment() {
		return limsExperiment;
	}

	public void setLimsExperiment(LIMSExperiment limsExperiment) {
		this.limsExperiment = limsExperiment;
	}
}

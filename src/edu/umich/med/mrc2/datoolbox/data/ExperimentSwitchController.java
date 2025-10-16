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

package edu.umich.med.mrc2.datoolbox.data;

import edu.umich.med.mrc2.datoolbox.data.lims.LIMSExperiment;
import edu.umich.med.mrc2.datoolbox.project.ProjectType;

public class ExperimentSwitchController {

	private boolean saveCurrentExperiment;
	private boolean exitProgram;
	private ProjectType activeExperimentType;
	private ProjectType newExperimentType;
	private ExperimentState experimentState;
	private LIMSExperiment limsExperiment;
	
	public enum ExperimentState{
		NEW_EXPERIMENT,
		EXISTING_EXPERIMENT,
		CLOSING_EXPERIMENT,
		;
	}
	
	public ExperimentSwitchController() {
		super();
		// TODO Auto-generated constructor stub
	}

	public ExperimentSwitchController(
			boolean saveCurrentExperiment, 
			ExperimentState projectState,
			boolean exitProgram,
			ProjectType activeExperimentType, 
			ProjectType newExperimentType) {
		super();
		this.saveCurrentExperiment = saveCurrentExperiment;
		this.experimentState = projectState;
		this.exitProgram = exitProgram;
		this.activeExperimentType = activeExperimentType;
		this.newExperimentType = newExperimentType;
	}

	public boolean doSaveCurrentExperiment() {
		return saveCurrentExperiment;
	}

	public void setSaveCurrentExperiment(boolean saveCurrentExperiment) {
		this.saveCurrentExperiment = saveCurrentExperiment;
	}

	public boolean isExitProgram() {
		return exitProgram;
	}

	public void setExitProgram(boolean exitProgram) {
		this.exitProgram = exitProgram;
	}

	public ProjectType getActiveExperimentType() {
		return activeExperimentType;
	}

	public void setActiveExperimentType(ProjectType activeExperimentType) {
		this.activeExperimentType = activeExperimentType;
	}

	public ProjectType getNewExperimentType() {
		return newExperimentType;
	}

	public void setNewExperimentType(ProjectType newExperimentType) {
		this.newExperimentType = newExperimentType;
	}

	public ExperimentState getExperimentState() {
		return experimentState;
	}

	public void setExperimentState(ExperimentState experimentState) {
		this.experimentState = experimentState;
	}

	public LIMSExperiment getLimsExperiment() {
		return limsExperiment;
	}

	public void setLimsExperiment(LIMSExperiment limsExperiment) {
		this.limsExperiment = limsExperiment;
	}
}

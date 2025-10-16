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

package edu.umich.med.mrc2.datoolbox.gui.idworks.ms2.filter;

import java.util.Collection;

import edu.umich.med.mrc2.datoolbox.data.MSFeatureIdentificationFollowupStep;
import edu.umich.med.mrc2.datoolbox.data.MSFeatureIdentificationLevel;
import edu.umich.med.mrc2.datoolbox.data.StandardFeatureAnnotation;

public class IDLAnnotationFilterParameters {

	private Collection<MSFeatureIdentificationLevel>idLevels;
	private Collection<StandardFeatureAnnotation>standardAnnotations;
	private Collection<MSFeatureIdentificationFollowupStep>followupSteps;
	private boolean includeIdLevels;
	private boolean includeStandardAnnotations;
	private boolean includeFollowupSteps;
	
	public IDLAnnotationFilterParameters(
			Collection<MSFeatureIdentificationLevel> idLevels,
			Collection<StandardFeatureAnnotation> standardAnnotations,
			Collection<MSFeatureIdentificationFollowupStep> followupSteps, 
			boolean includeIdLevels,
			boolean includeStandardAnnotations, 
			boolean includeFollowupSteps) {
		super();
		this.idLevels = idLevels;
		this.standardAnnotations = standardAnnotations;
		this.followupSteps = followupSteps;
		this.includeIdLevels = includeIdLevels;
		this.includeStandardAnnotations = includeStandardAnnotations;
		this.includeFollowupSteps = includeFollowupSteps;
	}

	public Collection<MSFeatureIdentificationLevel> getIdLevels() {
		return idLevels;
	}

	public void setIdLevels(Collection<MSFeatureIdentificationLevel> idLevels) {
		this.idLevels = idLevels;
	}

	public Collection<StandardFeatureAnnotation> getStandardAnnotations() {
		return standardAnnotations;
	}

	public void setStandardAnnotations(Collection<StandardFeatureAnnotation> standardAnnotations) {
		this.standardAnnotations = standardAnnotations;
	}

	public Collection<MSFeatureIdentificationFollowupStep> getFollowupSteps() {
		return followupSteps;
	}

	public void setFollowupSteps(Collection<MSFeatureIdentificationFollowupStep> followupSteps) {
		this.followupSteps = followupSteps;
	}

	public boolean isIncludeIdLevels() {
		return includeIdLevels;
	}

	public void setIncludeIdLevels(boolean includeIdLevels) {
		this.includeIdLevels = includeIdLevels;
	}

	public boolean isIncludeStandardAnnotations() {
		return includeStandardAnnotations;
	}

	public void setIncludeStandardAnnotations(boolean includeStandardAnnotations) {
		this.includeStandardAnnotations = includeStandardAnnotations;
	}

	public boolean isIncludeFollowupSteps() {
		return includeFollowupSteps;
	}

	public void setIncludeFollowupSteps(boolean includeFollowupSteps) {
		this.includeFollowupSteps = includeFollowupSteps;
	}
}

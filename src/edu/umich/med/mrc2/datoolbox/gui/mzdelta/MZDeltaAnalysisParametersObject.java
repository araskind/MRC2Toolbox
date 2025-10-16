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

package edu.umich.med.mrc2.datoolbox.gui.mzdelta;

import java.util.Set;

import edu.umich.med.mrc2.datoolbox.data.MsFeatureSet;
import edu.umich.med.mrc2.datoolbox.data.enums.MassErrorType;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class MZDeltaAnalysisParametersObject {

	private DataAnalysisProject project;
	private DataPipeline dataPipeline;
	private MsFeatureSet featureSet;
	private Set<Double>anchorMassSet;
	private double anchorMassError;
	private MassErrorType anchorMassErrorType;
	private double anchorRTError;
	private Set<Double>rtSeriesMassSet;
	private double rtSeriesMassError;
	private MassErrorType rtSeriesMassErrorType;
	private double rtSeriesMinStep;
	
	public MZDeltaAnalysisParametersObject(
			DataAnalysisProject project, 
			DataPipeline dataPipeline,
			MsFeatureSet featureSet, 
			Set<Double> anchorMassSet, 
			double anchorMassError,
			MassErrorType anchorMassErrorType, 
			double anchorRTError, 
			Set<Double> rtSeriesMassSet,
			double rtSeriesMassError, 
			MassErrorType rtSeriesMassErrorType, 
			double rtSeriesMinStep) {
		super();
		this.project = project;
		this.dataPipeline = dataPipeline;
		this.featureSet = featureSet;
		this.anchorMassSet = anchorMassSet;
		this.anchorMassError = anchorMassError;
		this.anchorMassErrorType = anchorMassErrorType;
		this.anchorRTError = anchorRTError;
		this.rtSeriesMassSet = rtSeriesMassSet;
		this.rtSeriesMassError = rtSeriesMassError;
		this.rtSeriesMassErrorType = rtSeriesMassErrorType;
		this.rtSeriesMinStep = rtSeriesMinStep;
	}

	public DataAnalysisProject getProject() {
		return project;
	}

	public DataPipeline getDataPipeline() {
		return dataPipeline;
	}

	public MsFeatureSet getFeatureSet() {
		return featureSet;
	}

	public Set<Double> getAnchorMassSet() {
		return anchorMassSet;
	}

	public double getAnchorMassError() {
		return anchorMassError;
	}

	public MassErrorType getAnchorMassErrorType() {
		return anchorMassErrorType;
	}

	public double getAnchorRTError() {
		return anchorRTError;
	}

	public Set<Double> getRtSeriesMassSet() {
		return rtSeriesMassSet;
	}

	public double getRtSeriesMassError() {
		return rtSeriesMassError;
	}

	public MassErrorType getRtSeriesMassErrorType() {
		return rtSeriesMassErrorType;
	}

	public double getRtSeriesMinStep() {
		return rtSeriesMinStep;
	}
}

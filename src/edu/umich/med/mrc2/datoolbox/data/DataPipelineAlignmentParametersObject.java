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

import org.jdom2.Element;

import edu.umich.med.mrc2.datoolbox.data.enums.MassErrorType;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.project.store.DataPipelineAlignmentParametersObjectFields;
import edu.umich.med.mrc2.datoolbox.project.store.ObjectNames;
import edu.umich.med.mrc2.datoolbox.project.store.XmlStorable;

public class DataPipelineAlignmentParametersObject implements XmlStorable{

	private DataPipeline referencePipeline;
	private DataPipeline queryPipeline;	
	private double massWindow;
	private MassErrorType massErrorType;
	private double retentionWindow;
	
	public DataPipelineAlignmentParametersObject(
			DataPipeline referencePipeline, 
			DataPipeline queryPipeline,
			double massWindow, 
			MassErrorType massErrorType, 
			double retentionWindow) {
		super();
		this.referencePipeline = referencePipeline;
		this.queryPipeline = queryPipeline;
		this.massWindow = massWindow;
		this.massErrorType = massErrorType;
		this.retentionWindow = retentionWindow;
	}

	public DataPipeline getReferencePipeline() {
		return referencePipeline;
	}

	public DataPipeline getQueryPipeline() {
		return queryPipeline;
	}

	public double getMassWindow() {
		return massWindow;
	}

	public MassErrorType getMassErrorType() {
		return massErrorType;
	}

	public double getRetentionWindow() {
		return retentionWindow;
	}
	
	public String getName() {
		return referencePipeline.getName() 
				+ " aligned to " + queryPipeline.getName();
	}

	@Override
	public Element getXmlElement() {

		Element dataPipelineAlignmentParametersObjectElement = 
				new Element(ObjectNames.DataPipelineAlignmentParametersObject.name());
		dataPipelineAlignmentParametersObjectElement.setAttribute(
				DataPipelineAlignmentParametersObjectFields.referencePipeline.name(), 
				referencePipeline.getName());
		dataPipelineAlignmentParametersObjectElement.setAttribute(
				DataPipelineAlignmentParametersObjectFields.queryPipeline.name(), 
				queryPipeline.getName());
		dataPipelineAlignmentParametersObjectElement.setAttribute(
				DataPipelineAlignmentParametersObjectFields.massWindow.name(), 
				Double.toString(massWindow));
		dataPipelineAlignmentParametersObjectElement.setAttribute(
				DataPipelineAlignmentParametersObjectFields.massErrorType.name(), 
				massErrorType.name());
		dataPipelineAlignmentParametersObjectElement.setAttribute(
				DataPipelineAlignmentParametersObjectFields.retentionWindow.name(), 
				Double.toString(retentionWindow));
		
		return dataPipelineAlignmentParametersObjectElement;
	}
	
	public DataPipelineAlignmentParametersObject(
			Element dataPipelineAlignmentParametersObjectElement, 
			DataAnalysisProject project) {
		
		String refPipelineName = dataPipelineAlignmentParametersObjectElement.getAttributeValue(
				DataPipelineAlignmentParametersObjectFields.referencePipeline.name());
		referencePipeline = project.getDataPipelineByName(refPipelineName);
		
		String queryPipelineName = dataPipelineAlignmentParametersObjectElement.getAttributeValue(
				DataPipelineAlignmentParametersObjectFields.queryPipeline.name());
		queryPipeline = project.getDataPipelineByName(queryPipelineName);
		
		massWindow = Double.parseDouble(
				dataPipelineAlignmentParametersObjectElement.getAttributeValue(
						DataPipelineAlignmentParametersObjectFields.massWindow.name()));
		
		massErrorType = MassErrorType.getTypeByName(
				dataPipelineAlignmentParametersObjectElement.getAttributeValue(
						DataPipelineAlignmentParametersObjectFields.massErrorType.name()));
		
		retentionWindow = Double.parseDouble(
				dataPipelineAlignmentParametersObjectElement.getAttributeValue(
						DataPipelineAlignmentParametersObjectFields.retentionWindow.name()));
	}			
}



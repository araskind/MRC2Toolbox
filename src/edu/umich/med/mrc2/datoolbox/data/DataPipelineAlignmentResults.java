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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.jdom2.Element;

import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.project.store.DataPipelineAlignmentResultsFields;
import edu.umich.med.mrc2.datoolbox.project.store.ObjectNames;

public class DataPipelineAlignmentResults extends MsFeatureClusterSet {

	private static final long serialVersionUID = -3271646008576341113L;
	private DataPipelineAlignmentParametersObject alignmentSettings;
	private Collection<LibraryMsFeature>unmatchedReferenceFeatures;

	public DataPipelineAlignmentResults(
			DataPipelineAlignmentParametersObject alignmentSettings,
			Set<MsFeatureCluster> clusterSet, 
			Collection<LibraryMsFeature> unmatchedReferenceFeatures) {
		super(alignmentSettings.getName(), clusterSet);
		this.alignmentSettings = alignmentSettings;
		this.unmatchedReferenceFeatures = unmatchedReferenceFeatures;
	}

	public DataPipelineAlignmentParametersObject getAlignmentSettings() {
		return alignmentSettings;
	}

	public Collection<LibraryMsFeature> getUnmatchedReferenceFeatures() {
		return unmatchedReferenceFeatures;
	}
	
	public Collection<MsFeature> getUnmatchedReferenceFeaturesAsMsFetures() {
		return unmatchedReferenceFeatures.stream().
				map(MsFeature.class::cast).collect(Collectors.toList());
	}
	
	public boolean includesPipeline(DataPipeline dp) {
		
		if(alignmentSettings.getReferencePipeline().equals(dp) 
				|| alignmentSettings.getQueryPipeline().equals(dp))
			return true;
		else
			return false;
	}
	
	@Override
	public Element getXmlElement() {
		
		Element dataPipelineAlignmentResults = super.getXmlElement();
		dataPipelineAlignmentResults.setName(ObjectNames.DataPipelineAlignmentResults.name());
		
		dataPipelineAlignmentResults.addContent(alignmentSettings.getXmlElement());
		Element unmatchedReferenceFeaturesElement = 
				new Element(DataPipelineAlignmentResultsFields.unmatchedReferenceFeatures.name());
		unmatchedReferenceFeaturesElement.setAttribute(ObjectNames.DataPipeline.name(), 
				alignmentSettings.getReferencePipeline().getName());
		List<String>unmatchedIds = unmatchedReferenceFeatures.stream().
				map(f -> f.getId()).collect(Collectors.toList());
		unmatchedReferenceFeaturesElement.setText(StringUtils.join(unmatchedIds, ","));
		dataPipelineAlignmentResults.addContent(unmatchedReferenceFeaturesElement);

		return dataPipelineAlignmentResults;
	}
	
	public DataPipelineAlignmentResults(
			Element dataPipelineAlignmentResults, 
			DataAnalysisProject project) {
		super(dataPipelineAlignmentResults, project);
		
		Element alignmentSettingsElement = 
				dataPipelineAlignmentResults.getChild(
						ObjectNames.DataPipelineAlignmentParametersObject.name());
		alignmentSettings = 
				new DataPipelineAlignmentParametersObject(alignmentSettingsElement, project);
		Element unmatchedFeaturesElement = dataPipelineAlignmentResults.getChild(
				DataPipelineAlignmentResultsFields.unmatchedReferenceFeatures.name());
		unmatchedReferenceFeatures = new ArrayList<>();
		String[]unmatchedFeatureIds = unmatchedFeaturesElement.getText().split(",");
		if(unmatchedFeatureIds.length > 0) {
			String piplineName = 
					unmatchedFeaturesElement.getAttributeValue(ObjectNames.DataPipeline.name());
			DataPipeline dp = project.getDataPipelineByName(piplineName);
			if(dp != null) {
				
				CompoundLibrary avgLib = project.getAveragedFeatureLibraryForDataPipeline(dp);
				if(avgLib != null) {
					for(String fid : unmatchedFeatureIds) {
						LibraryMsFeature feature =  avgLib.getFeatureById(fid);
						if(feature != null)
							unmatchedReferenceFeatures.add(feature);
					}
				}
			}
		}
	}
}









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

package edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.io;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.jdom2.DataConversionException;
import org.jdom2.Document;
import org.jdom2.Element;
import org.ujmp.core.Matrix;

import edu.umich.med.mrc2.datoolbox.data.CompoundIdentity;
import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureQCDataObject;
import edu.umich.med.mrc2.datoolbox.data.SimpleMsFeature;
import edu.umich.med.mrc2.datoolbox.data.enums.AgilentCefFields;
import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.cef.CEFProcessingTask;
import edu.umich.med.mrc2.datoolbox.utils.Range;
import edu.umich.med.mrc2.datoolbox.utils.XmlUtils;

public class CefPeakQualityImportTask extends CEFProcessingTask {

	private DataFile targetFile;
	private Matrix featureMatrix;
	private Map<String,Long>featureCoordinateMap;
	private Collection<MsFeatureQCDataObject>qcData;

	public CefPeakQualityImportTask(
			DataFile targetFile,
			File sourceCefFile, 
			Matrix featureMatrix, 
			Map<String, Long> featureCoordinateMap) {
		super();
		this.targetFile = targetFile;
		this.inputCefFile = sourceCefFile;
		this.featureMatrix = featureMatrix;
		this.featureCoordinateMap = featureCoordinateMap;
	}

	@Override
	public void run() {

		setStatus(TaskStatus.PROCESSING);
		try {
			parseInputCefFile(inputCefFile);
		} catch (Exception e) {
			errorMessage = "Failed to parse " + inputCefFile.getName();
			e.printStackTrace();
			setStatus(TaskStatus.ERROR);
			return;
		}
		if(getStatus().equals(TaskStatus.PROCESSING)) {
			
			try {
				updateFeaturesWithQCdata();
			} catch (Exception e) {
				errorMessage = "Failed to update data for " + inputCefFile.getName();
				setStatus(TaskStatus.ERROR);
				e.printStackTrace();
				return;
			}
		}
		setStatus(TaskStatus.FINISHED);
	}
	
	@Override
	protected void parseInputCefFile(File fileToParse) throws Exception {
		
		inputCefFile = fileToParse;
		if(inputCefFile == null || !inputCefFile.exists()) {
			errorMessage = "Input file not found";
			setStatus(TaskStatus.ERROR);
			return;
		}
		taskDescription = 
				"Reading features from " + inputCefFile.getName();
				
		Document cefDocument = XmlUtils.readXmlFile(inputCefFile);
		if(cefDocument == null) {
			errorMessage = "Failed to parse input file";
			setStatus(TaskStatus.ERROR);
			return;
		}
		List<Element>featureNodes = 
				cefDocument.getRootElement().getChild("CompoundList").getChildren("Compound");
		qcData = new HashSet<MsFeatureQCDataObject>();
		
		total = featureNodes.size();
		processed = 0;
		for (Element compoundElement : featureNodes) {
			
			MsFeatureQCDataObject qdo = extractFeatureQualityData(compoundElement);
			if(qdo == null) {
				
				errorMessage = "No QC data in " + fileToParse.getName();
				System.err.println(errorMessage);
				setStatus(TaskStatus.ERROR);
				return;
			}
			qcData.add(qdo);
			processed++;
		}
	}
	
	private MsFeatureQCDataObject extractFeatureQualityData(Element compoundElement) throws DataConversionException{
		
		if(compoundElement.getChild("Results") == null)
			return null;
		
		if(compoundElement.getChild("Results").getChildren("Molecule").isEmpty())
			return null;
		
		double score = 0.0d;
		String targetId = null;
		for(Element molElement : compoundElement.getChild("Results").getChildren("Molecule")) {
			
			String name = molElement.getAttributeValue("name");
			if(name.startsWith(DataPrefix.MS_LIBRARY_UNKNOWN_TARGET.getName())) {
				
				CompoundIdentity unkId = new CompoundIdentity(name, null);
				addDatabaseReferencesToCompoundIdentity(unkId, molElement);
				String dbId = unkId.getPrimaryDatabaseId();
				if(dbId != null && dbId.startsWith(DataPrefix.MS_LIBRARY_TARGET.getName())
						&& !dbId.startsWith(DataPrefix.MS_FEATURE.getName())
						&& !dbId.startsWith(DataPrefix.MS_LIBRARY_TARGET_OLD.getName())) {
					targetId = dbId;
				}				
				if(molElement.getChild("MatchScores") != null) {
					
					for(Element scoreElement : molElement.getChild("MatchScores").getChildren("Match")) {
						
						if(scoreElement.getAttributeValue("algo").equals("overall"))
								score = scoreElement.getAttribute("score").getDoubleValue();
					}
				}
			}			
		}
		Range rtRange = null;
		Element forPeakWidthElement = null;
		for(Element spectrumElement : compoundElement.getChildren("Spectrum")) {
			
			String spectrumType = spectrumElement.getAttributeValue("type");
			if(spectrumType.equals(AgilentCefFields.MS1_SPECTRUM.getName())) {
				forPeakWidthElement = spectrumElement;
				break;
			}
			if(spectrumType.equals(AgilentCefFields.MFE_SPECTRUM.getName())
					&& forPeakWidthElement == null) {
				forPeakWidthElement = spectrumElement;
			}
		}
		//	Add RT range
		if(forPeakWidthElement != null) {
			
			if(forPeakWidthElement.getChild("RTRanges") != null
					&& !forPeakWidthElement.getChild("RTRanges").getChildren().isEmpty()) {
				Element rtRangeElement = 
						forPeakWidthElement.getChild("RTRanges").getChild("RTRange");
				if(rtRangeElement != null) {
					
					double min = rtRangeElement.getAttribute("min").getDoubleValue();
					double max = rtRangeElement.getAttribute("max").getDoubleValue();
					if(min <= max) 
						rtRange = new Range(min, max);	
				}
			}
		}		
		if(score > 0 && rtRange != null && targetId != null) {
		
			MsFeatureQCDataObject qdo = 
					new MsFeatureQCDataObject(targetId, score, rtRange);
			return qdo;
		}		
		return null;
	}
	
	private void updateFeaturesWithQCdata() throws Exception {
		
		long[] featureCoordinates = new long[2];
		featureCoordinates[0] = featureMatrix.getRowForLabel(targetFile);		
		for(MsFeatureQCDataObject qdo : qcData) {
			
			Long featurePosition = featureCoordinateMap.get(qdo.getLibraryTargetId());
			if(featurePosition != null) {
				
				featureCoordinates[1] = featurePosition;
				SimpleMsFeature simpleFeature = (SimpleMsFeature)featureMatrix.getAsObject(featureCoordinates);
				if(simpleFeature != null) {
					simpleFeature.setQualityScore(qdo.getQualityScore());
					if(simpleFeature.getRtRange() == null)
						simpleFeature.setRtRange(qdo.getRtRange());
				}
			}
		}
	}
	
	@Override
	public Task cloneTask() {

		return new CefPeakQualityImportTask(
				targetFile,
				inputCefFile, 
				featureMatrix, 
				featureCoordinateMap);
	}
}

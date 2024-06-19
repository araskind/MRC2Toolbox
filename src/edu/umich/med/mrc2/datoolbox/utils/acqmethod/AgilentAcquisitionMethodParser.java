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

package edu.umich.med.mrc2.datoolbox.utils.acqmethod;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;

import edu.umich.med.mrc2.datoolbox.data.compare.SortDirection;
import edu.umich.med.mrc2.datoolbox.data.lims.ChromatographicGradient;
import edu.umich.med.mrc2.datoolbox.data.lims.ChromatographicGradientStep;
import edu.umich.med.mrc2.datoolbox.utils.MapUtils;
import edu.umich.med.mrc2.datoolbox.utils.XmlUtils;

public class AgilentAcquisitionMethodParser {

	public static final String ISOCRATIC_PUMP_PARAMETERS_FILE = "IsoPump_1.xml";
	public static final String BINARY_PUMP_PARAMETERS_FILE = "BinPump_1.xml";
	public static final String QUATERNARY_PUMP_PARAMETERS_FILE = "QuatPump_1.xml";
	public static final String COLUMN_COMPARTMENT_PARAMETERS_FILE = "TCC_1.xml";
	public static final String AUTOSAMPLER_PARAMETERS_FILE = "HiP-ALS_1.xml";
	
	public static final String TIMETABLE_ENTRY_NODE = "//PumpMethod/Timetable/TimetableEntry";
	public static final String GLOBAL_FLOW_NODE = "//PumpMethod/Flow";
	public static final String STOP_TIME_NODE = "//PumpMethod/StopTime/StopTimeMode";
	
	public static final Namespace xsiNamespace = 
			Namespace.getNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
		
	private File methodFolder;
	//	private File tmpProcessingFolder;
	
	@SuppressWarnings("unused")
	private Document 
		methodDocument,
		isocraticPumpParameterDocument,
		binaryPumpParameterDocument,
		quaternaryPumpParameterDocument,
		columnCompartmentParameterDocument,
		autosamplerParameterDocument;
	
	public AgilentAcquisitionMethodParser(
			File methodFolder) {
		super();
		this.methodFolder = methodFolder;
		
		isocraticPumpParameterDocument = null;
		binaryPumpParameterDocument = null;
		quaternaryPumpParameterDocument = null;
		columnCompartmentParameterDocument = null;
		autosamplerParameterDocument = null;
	}
	
	public void parseParameterFiles() {
		
		parseIsocraticPumpParameters();
		parseBinaryPumpParameters();
		parseQuaternaryPumpParameters();
		parseColumnCompartmentParameters();
		parseAutosamplerParameters();
	}
	
	@SuppressWarnings("unchecked")
	public ChromatographicGradient extractGradientData() throws XPathExpressionException {
		
		//	Needs binary or quaternary pump. 
		//	Iso will work for isocratic elution, but for now it will be too complicated
		
		Document gradientDoc = getGradientDefinitionDocument();
		if(gradientDoc == null)
			return null;

		ChromatographicGradient grad = new ChromatographicGradient();
		Element pumpConfigElement = gradientDoc.getRootElement();
		Namespace ns = pumpConfigElement.getNamespace();
		Element stopTimeElement = 
				pumpConfigElement.getChild("StopTime", ns).getChild("StopTimeValue", ns);
		
		double stopTime = 0.0d;
		if(stopTimeElement != null && stopTimeElement.getText() != null
				&& !stopTimeElement.getText().trim().isEmpty()) {
			stopTime = Double.parseDouble(stopTimeElement.getText());
			grad.setStopTime(stopTime);
		}
				
		Element startingFlowElement = pumpConfigElement.getChild("Flow", ns);	
		double startingFlowRate = 0.0d;
		if(startingFlowElement != null && startingFlowElement.getText() != null
				&& !startingFlowElement.getText().trim().isEmpty())
			startingFlowRate = Double.parseDouble(startingFlowElement.getText());
		
		Element timeTableElement = pumpConfigElement.getChild("Timetable", ns);
		if(timeTableElement != null) {
			Set<ChromatographicGradientStep>gradSteps = 
					parseTimetableElement(timeTableElement, ns, startingFlowRate);
			grad.getGradientSteps().addAll(gradSteps);
		}
		//	Optionally get column compartment temperature. How to handle 2 compartments?
		if(columnCompartmentParameterDocument != null) {
			
		}		
		return grad;
	}
	
	private Set<ChromatographicGradientStep> parseTimetableElement(
			Element timeTableElement,
			Namespace ns,
			double startingFlowRate){
		
		List<Element> timetableEntries = 
				timeTableElement.getChildren("TimetableEntry", ns);
		
		Map<String,List<AgilentGradientTimetableEntry>>entries = 
				new TreeMap<String,List<AgilentGradientTimetableEntry>>();
		for(Element timetableEntry : timetableEntries) {
			
			Attribute ti = timetableEntry.getAttribute("type", xsiNamespace);
			if(ti != null) {
				
				String typeString = ti.getValue();
				AgilentGradientTimetableEntryType type = 
						AgilentGradientTimetableEntryType.valueOf(typeString);
				String timeCode = timetableEntry.getChildText("Time", ns);
				AgilentGradientTimetableEntry newEntry = 
						new AgilentGradientTimetableEntry(type, timeCode);
				if(!entries.containsKey(timeCode))
					entries.put(timeCode, new ArrayList<AgilentGradientTimetableEntry>());
				
				if(type.equals(AgilentGradientTimetableEntryType.ChangeFlowType)) {
					
					Element flowElement = timetableEntry.getChild("Flow", ns);	
					if(flowElement != null && flowElement.getText() != null
							&& !flowElement.getText().trim().isEmpty())
						newEntry.setFlow(Double.parseDouble(flowElement.getText()));
				}
				if(type.equals(AgilentGradientTimetableEntryType.ChangePressureType)) {
					
					Element pressureElement = timetableEntry.getChild("HighPressureLimit", ns);	
					if(pressureElement != null && pressureElement.getText() != null
							&& !pressureElement.getText().trim().isEmpty())
						newEntry.setHighPressureLimit(Double.parseDouble(pressureElement.getText()));
				}
				if(type.equals(AgilentGradientTimetableEntryType.ChangeSolventCompositionType)) {
					
					double percentA = 0.0d;
					double percentB = 0.0d;
					double percentC = 0.0d;
					double percentD = 0.0d;
					
					Element percentAelement = timetableEntry.getChild("PercentA", ns);			
					if(percentAelement != null && percentAelement.getText() != null
							&& !percentAelement.getText().trim().isEmpty())
						percentA = Double.parseDouble(percentAelement.getText());
					
					Element percentBelement = timetableEntry.getChild("PercentB", ns);
					if(percentBelement != null && percentBelement.getText() != null
							&& !percentBelement.getText().trim().isEmpty())
						percentB = Double.parseDouble(percentBelement.getText());
					
					Element percentCelement = timetableEntry.getChild("PercentC", ns);
					if(percentCelement != null && percentCelement.getText() != null
							&& !percentCelement.getText().trim().isEmpty())
						percentC = Double.parseDouble(percentCelement.getText());
					
					Element percentDelement = timetableEntry.getChild("PercentD", ns);
					if(percentDelement != null && percentDelement.getText() != null
							&& !percentDelement.getText().trim().isEmpty())
						percentD = Double.parseDouble(percentDelement.getText());
					
					newEntry.setPercentA(percentA);
					newEntry.setPercentB(percentB);
					newEntry.setPercentC(percentC);
					newEntry.setPercentD(percentD);
				}
				entries.get(timeCode).add(newEntry);
			}		
		}
		Set<ChromatographicGradientStep>gradSteps = 
				new TreeSet<ChromatographicGradientStep>();
		Map<String,Double>timeCodeMap = new HashMap<String,Double>();
		for(String tc : entries.keySet())
			timeCodeMap.put(tc, Double.parseDouble(tc));

		Map<String,Double>timeCodeMapSorted = 
				MapUtils.sortMapByValue(timeCodeMap, SortDirection.ASC);
		for(String tc : timeCodeMapSorted.keySet()) {
			
			List<AgilentGradientTimetableEntry>tcEntries = entries.get(tc);
			double startTime = timeCodeMapSorted.get(tc);
			double flowRate = 0.0d;
			double percentA = 0.0d;
			double percentB = 0.0d;
			double percentC = 0.0d;
			double percentD = 0.0d;
			
			for(AgilentGradientTimetableEntry tce : tcEntries) {
				
				if(tce.getType().equals(AgilentGradientTimetableEntryType.ChangeFlowType))
					flowRate = tce.getFlow();
				
				if(tce.getType().equals(AgilentGradientTimetableEntryType.ChangeSolventCompositionType)) {
					
					percentA = tce.getPercentA();
					percentB = tce.getPercentB();
					percentC = tce.getPercentC();
					percentD = tce.getPercentD();
				}
			}
			ChromatographicGradientStep newStep = new ChromatographicGradientStep(
					startTime, 
					flowRate, 
					percentA,
					percentB, 
					percentC, 
					percentD);
			gradSteps.add(newStep);
		}
		//	Add zero time step
		ChromatographicGradientStep firstStep =
				((TreeSet<ChromatographicGradientStep>)gradSteps).first();
		if(firstStep.getStartTime() > 0) {
			
			ChromatographicGradientStep zeroStep = 
					new ChromatographicGradientStep(
					0.0d, 
					startingFlowRate, 
					firstStep.getMobilePhaseStartingPercent()[0],
					firstStep.getMobilePhaseStartingPercent()[1], 
					firstStep.getMobilePhaseStartingPercent()[2], 
					firstStep.getMobilePhaseStartingPercent()[3]);
			gradSteps.add(zeroStep);
		}		
		return gradSteps;
	}
	
	@SuppressWarnings("unchecked")
	private double getStartingFlowRate(Document gradientDoc, XPathFactory factory) 
			throws IllegalArgumentException, XPathExpressionException{
		
		double flow = 0.0;
		XPath xpath = factory.newXPath();
		XPathExpression expr = xpath.compile(GLOBAL_FLOW_NODE);
		List<Element>flowNodes = (List<Element>) expr.evaluate(gradientDoc, XPathConstants.NODESET);
		if(flowNodes.size() == 0)
			throw new IllegalArgumentException("Flow rate not specified");
		
		Element flowElement = flowNodes.get(0);
		flow = Double.parseDouble(flowElement.getChildren().get(0).getText());
		return flow;
	}
	
	@SuppressWarnings("unchecked")
	private double getStopTime(Document gradientDoc, XPathFactory factory) 
			throws IllegalArgumentException, XPathExpressionException{
		
		double stopTime = 0.0;
		XPath xpath = factory.newXPath();
		XPathExpression expr = xpath.compile(STOP_TIME_NODE);
		List<Element>flowNodes = (List<Element>) expr.evaluate(gradientDoc, XPathConstants.NODESET);
		if(flowNodes.size() == 0)
			throw new IllegalArgumentException("Stop time not specified");
		
		Element stopTimeElement = flowNodes.get(0);
		stopTime = Double.parseDouble(stopTimeElement.getChildren().get(0).getText());
		return stopTime;
	}
	
	private Document getGradientDefinitionDocument() {
		
		if(binaryPumpParameterDocument == null && quaternaryPumpParameterDocument == null)
			return null;
		
		if(quaternaryPumpParameterDocument != null)
			return quaternaryPumpParameterDocument;
		else
			return binaryPumpParameterDocument;
	}
	

	
	private void parseIsocraticPumpParameters() {
		
		File isoPumpParameterFile = 
				Paths.get(methodFolder.getAbsolutePath(), ISOCRATIC_PUMP_PARAMETERS_FILE).toFile();
		if(!isoPumpParameterFile.exists())
			return;
		
		isocraticPumpParameterDocument = XmlUtils.readXmlFile(isoPumpParameterFile);
	}
	
	private void parseBinaryPumpParameters() {
		
		File binaryPumpParameterFile = 
				Paths.get(methodFolder.getAbsolutePath(), BINARY_PUMP_PARAMETERS_FILE).toFile();
		if(!binaryPumpParameterFile.exists())
			return;
		
		binaryPumpParameterDocument = XmlUtils.readXmlFile(binaryPumpParameterFile);
	}
	
	private void parseQuaternaryPumpParameters() {
		
		File quaternaryPumpParameterFile = 
				Paths.get(methodFolder.getAbsolutePath(), QUATERNARY_PUMP_PARAMETERS_FILE).toFile();
		if(!quaternaryPumpParameterFile.exists())
			return;
		
		quaternaryPumpParameterDocument = XmlUtils.readXmlFile(quaternaryPumpParameterFile);
	}
	
	private void parseColumnCompartmentParameters() {
		
		File columnCompartmentParameterFile = 
				Paths.get(methodFolder.getAbsolutePath(), COLUMN_COMPARTMENT_PARAMETERS_FILE).toFile();
		if(!columnCompartmentParameterFile.exists())
			return;
		
		columnCompartmentParameterDocument = XmlUtils.readXmlFile(columnCompartmentParameterFile);
	}
	
	private void parseAutosamplerParameters() {
		
		File autosamplerParameterFile = 
				Paths.get(methodFolder.getAbsolutePath(), AUTOSAMPLER_PARAMETERS_FILE).toFile();
		if(!autosamplerParameterFile.exists())
			return;
		
		autosamplerParameterDocument = XmlUtils.readXmlFile(autosamplerParameterFile);
	}
	
//	private void parseMainDocumet() {
//		
//	}
//	
//	public void extractMethodData() {
//		
//		File methodDocumentFile = 
//				Paths.get(methodFolder.getAbsolutePath(), ISOCRATIC_PUMP_PARAMETERS_FILE).toFile();
//		if(!methodDocumentFile.exists())
//			return;
//		
//		methodDocument = XmlUtils.readXmlFile(methodDocumentFile);
//	}
//	
//	public void extractEncodedSubdocuments() {
//		
//		
//	}
	
}

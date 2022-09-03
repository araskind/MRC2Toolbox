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
import java.util.List;
import java.util.TreeSet;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.jdom2.Document;
import org.jdom2.Element;

import edu.umich.med.mrc2.datoolbox.data.lims.ChromatographicGradient;
import edu.umich.med.mrc2.datoolbox.data.lims.ChromatographicGradientStep;
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
	
	
	private File methodFolder;
	@SuppressWarnings("unused")
	private Document 
		isocraticPumpParameterDocument,
		binaryPumpParameterDocument,
		quaternaryPumpParameterDocument,
		columnCompartmentParameterDocument,
		autosamplerParameterDocument;
	
	public AgilentAcquisitionMethodParser(File methodFolder) {
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

		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();
		XPathExpression expr = xpath.compile(TIMETABLE_ENTRY_NODE);
		List<Element>timetableNodes = 
				(List<Element>)expr.evaluate(gradientDoc, XPathConstants.NODESET);
		
		double startingFlowRateFlow = getStartingFlowRate(gradientDoc, factory);
		double stopTime = getStopTime(gradientDoc, factory);
		
		TreeSet<ChromatographicGradientStep> gradientSteps = new TreeSet<ChromatographicGradientStep>();
		for (int i = 0; i < timetableNodes.size(); i++) {

			Element timetableElement = (Element) timetableNodes.get(i);
			if(timetableElement.getAttribute("xsi:type").getValue().equals("ChangeSolventCompositionType")) {
				
				
			}
			//	TODO ignore for now, see if required
			if(timetableElement.getAttribute("xsi:type").getValue().equals("ChangeSolventCompositionType")) {
				
			}
//			double rt = Double.parseDouble(locationElement.getAttribute("rt"));
//			double neutralMass = Double.parseDouble(locationElement.getAttribute("m"));
		}
		
		//	Optionally get column compartment temperature. How to handle 2 compartments?
		if(columnCompartmentParameterDocument != null) {
			
		}		
		return null;
	}
	
	private ChromatographicGradientStep parseSolventCompositionChangeElement(Element timetableElement){
//		public ChromatographicGradientStep(
//				double startTime, 
//				double flowRate, 
//				double mobilePhaseApercent,
//				double mobilePhaseBpercent, 
//				double mobilePhaseCpercent, 
//				double mobilePhaseDpercent) {
		
		return null;
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
	
}

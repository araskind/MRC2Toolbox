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

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;

import edu.umich.med.mrc2.datoolbox.data.compare.SortDirection;
import edu.umich.med.mrc2.datoolbox.data.lims.ChromatographicGradient;
import edu.umich.med.mrc2.datoolbox.data.lims.ChromatographicGradientStep;
import edu.umich.med.mrc2.datoolbox.data.lims.MobilePhase;
import edu.umich.med.mrc2.datoolbox.utils.MapUtils;
import edu.umich.med.mrc2.datoolbox.utils.XmlUtils;

public class AgilentAcquisitionMethodParser {

	public static final String ISOCRATIC_PUMP_PARAMETERS_FILE = "IsoPump_1.xml";
	public static final String BINARY_PUMP_PARAMETERS_FILE = "BinPump_1.xml";
	// public static final String BINARY_PUMP_PARAMETERS_FILE = "BinPump_2.xml";
	public static final String QUATERNARY_PUMP_PARAMETERS_FILE = "QuatPump_1.xml";
	public static final String COLUMN_COMPARTMENT_PARAMETERS_FILE = "TCC_1.xml";
	public static final String AUTOSAMPLER_PARAMETERS_FILE = "HiP-ALS_1.xml";
	
	public static final Namespace xsiNamespace = 
			Namespace.getNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
		
	private File methodFolder;
	private Map<String,Integer>agilentChannelMap;
	
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
		
		agilentChannelMap = new TreeMap<String,Integer>();
		agilentChannelMap.put("Channel_A", 0);
		agilentChannelMap.put("Channel_B", 1);
		agilentChannelMap.put("Channel_C", 2);
		agilentChannelMap.put("Channel_D", 3);
	}
	
	public void parseParameterFiles() {
		
		parseIsocraticPumpParameters();
		parseBinaryPumpParameters();
		parseQuaternaryPumpParameters();
		parseColumnCompartmentParameters();
		parseAutosamplerParameters();
	}
	
	public ChromatographicGradient extractGradientData() {
		
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
		
		Element solventCompositionElement = 
				pumpConfigElement.getChild("SolventComposition", ns);
		
		MobilePhase[]mobilePhases = new MobilePhase[4];
		if(solventCompositionElement != null) {
			
			mobilePhases = 
					parseSolventCompositionElement(solventCompositionElement,  ns);
			for(int i=0; i<4; i++)
				grad.setMobilePhase(mobilePhases[i], i);			
		}	
		Set<ChromatographicGradientStep>gradSteps = null;
		Element timeTableElement = pumpConfigElement.getChild("Timetable", ns);
		if(timeTableElement != null) {
			
			gradSteps = parseTimetableElement(
					timeTableElement, ns, startingFlowRate);
			grad.getGradientSteps().addAll(gradSteps);
		}
		//	When empty - isocratic method, read percentages from solvent composition
		if(gradSteps == null || gradSteps.isEmpty()) {
			
			double percentA = 0.0d;
			double percentB = 0.0d;
			double percentC = 0.0d;
			double percentD = 0.0d;
			
			if(mobilePhases[0] != null)
				percentA = mobilePhases[0].getStartingPercentage();
			
			if(mobilePhases[1] != null)
				percentB = mobilePhases[1].getStartingPercentage();
			
			if(mobilePhases[2] != null)
				percentC = mobilePhases[2].getStartingPercentage();
			
			if(mobilePhases[3] != null)
				percentD = mobilePhases[3].getStartingPercentage();
						
			ChromatographicGradientStep zeroStep = 
					new ChromatographicGradientStep(
					0.0d, 
					startingFlowRate, 
					percentA,
					percentB, 
					percentC, 
					percentD);
			grad.getGradientSteps().add(zeroStep);
		}
		//	Optionally get column compartment temperature. 
		//	How to handle 2 compartments?
		//		if(columnCompartmentParameterDocument != null) {
		//			
		//			Element ccConfigElement = columnCompartmentParameterDocument.getRootElement();
		//			ns = ccConfigElement.getNamespace();
		//		}		
		return grad;
	}
	
	private MobilePhase[]parseSolventCompositionElement(
			Element solventCompositionElement,
			Namespace ns){
		
		MobilePhase[]mobilePhases = new MobilePhase[4];
		List<Element> solventElementList = 
				solventCompositionElement.getChildren("SolventElement", ns);
		
		for(int i=0; i<4; i++) {
			
			if(solventElementList.size() > i) {
				
				Element solventElement = solventElementList.get(i);
				if(solventElement.getChild("Used").getText().equalsIgnoreCase("true")) {
					
					String channelName = solventElement.getChild("Channel").getText();
					int mpIndex = agilentChannelMap.get(channelName);
					String solventName = null;
					
					String selectedSolventChannelDefElementName = 
							solventElement.getChild("SelectedSolventChannel").getText() + 
							"ExtendedSolventType";	
					
					Element extendedSolventTypeElement = 
							solventElement.getChild(selectedSolventChannelDefElementName);
					if(extendedSolventTypeElement != null) {
						
						Element solvDefElement = 
								extendedSolventTypeElement.getChild("SolventDescription");
						if(solvDefElement != null) {
							
							solventName = 
									solvDefElement.getChild("Definition").
									getChildText("Name").replaceFirst("V\\.\\d\\d", "").trim();
							if(solvDefElement.getChild("Definition").getChildText("IsPure").equalsIgnoreCase("false")) {
								
								String percentString = ", " + solvDefElement.getChildText("Percent") + "%";
								solventName += percentString;						
							}	
						}					
					}
					else {
						selectedSolventChannelDefElementName = 
								solventElement.getChild("SelectedSolventChannel").getText() + 
								"SolventType";
						Element simpleSolventTypeElement = 
								solventElement.getChild(selectedSolventChannelDefElementName);
						if(simpleSolventTypeElement != null) {
							
							solventName = simpleSolventTypeElement.getChildText("SolventName");
						
//							String percentage = solventElement.getChildText("Percentage");
//							if(percentage != null && !percentage.equals("100") && !percentage.equals("0"))
//								solventName += ", " + percentage + "%";
						}
						else {								
							solventName = solventElement.getChildText(
									solventElement.getChild("SelectedSolventChannel").getText() + "UserName");
						}
					}
					if(solventName != null && !solventName.isEmpty()) {
						
						mobilePhases[mpIndex] = new MobilePhase(solventName);						
						String startingPercentage = solventElement.getChildText("Percentage");
						if(startingPercentage != null && !startingPercentage.isEmpty()) {
							
							Double startingPercentageValue = null;
							try {
								startingPercentageValue = Double.parseDouble(startingPercentage);
							} catch (NumberFormatException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							if(startingPercentageValue != null)
								mobilePhases[mpIndex].setStartingPercentage(startingPercentageValue);
						}					
					}
				}
			}
		}		
		return mobilePhases;
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
			if(newStep.getFlowRate() <= 0)
				newStep.setFlowRate(startingFlowRate);
			
			gradSteps.add(newStep);
		}
		//	Add zero time step
		if(!gradSteps.isEmpty()) {
			
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
		}		
		return gradSteps;
	}
	
	private Document getGradientDefinitionDocument() {
		
		if(binaryPumpParameterDocument == null 
				&& quaternaryPumpParameterDocument == null)
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

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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.lang3.math.NumberUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;

import edu.umich.med.mrc2.datoolbox.data.lims.ChromatographicGradient;
import edu.umich.med.mrc2.datoolbox.data.lims.ChromatographicGradientStep;
import edu.umich.med.mrc2.datoolbox.data.lims.MobilePhase;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCache;
import edu.umich.med.mrc2.datoolbox.utils.XmlUtils;

public class ExtractedAgilentAcquisitionMethodParser {
	
	public static final String HIP_SAMPLER = "HiP Sampler";
	public static final String MULTISAMPLER = "Multisampler";
	public static final String ISO_PUMP = "Iso. Pump";
	public static final String BINARY_PUMP = "Binary Pump";
	public static final String QUAT_PUMP = "Quat. Pump";
	public static final String COLUMN_COMP = "Column Comp.";
	public static final String QTOF = "Q-TOF";
	public static final String QQQ = "QQQ";
	public static final String STOP_TIME = "StopTime_StopTimeValue";
	public static final String FLOW = "Flow";
	public static final String SOLVENT_COMPOSITION_TABLE_NAME = "Solvent Composition";
	public static final String TIME_TABLE_NAME = "Timetable";
	
	private List<String>errorLog;
	private File rcDevicesXmlFile;
	private boolean doNotMatchMobilePhases;
	
	public ExtractedAgilentAcquisitionMethodParser(File rcDevicesXmlFile) {
		super();
		this.rcDevicesXmlFile = rcDevicesXmlFile;
		errorLog = new ArrayList<String>();
	}
	
	public String extractMethodDescriptionFromFile() {
		
		Document methodDocument = XmlUtils.readXmlFile(rcDevicesXmlFile);
		Namespace ns = methodDocument.getRootElement().getNamespace();
		Element mInfo = methodDocument.getRootElement().getChild("MethodInfo", ns);
		if(mInfo == null)
			return null;
		else
			return mInfo.getChildText("MethodDescription", ns);
	}
	
	public String extractInstrumentTypeFromFile() {

		List<String>deviceOptions = new ArrayList<String>();
		deviceOptions.add("QQQ");
		deviceOptions.add("Q-TOF");
		Document methodDocument = XmlUtils.readXmlFile(rcDevicesXmlFile);
		Namespace ns = methodDocument.getRootElement().getNamespace();
		List<Element> deviceElements = 
				methodDocument.getRootElement().getChildren("Device", ns);
		List<String>devices = new ArrayList<String>();
		for(Element de : deviceElements) {

			String devName = de.getChildText("Name", ns);
			if(devName != null && deviceOptions.contains(devName))
				return devName;
		}
		return null;
	}

	public ChromatographicGradient extractChromatographicGradientFromFile() {
		
		ChromatographicGradient grad = new ChromatographicGradient();
		Document methodDocument = XmlUtils.readXmlFile(rcDevicesXmlFile);
		Namespace ns = methodDocument.getRootElement().getNamespace();
		List<Element> deviceElements = 
				methodDocument.getRootElement().getChildren("Device", ns);
		List<String>devices = new ArrayList<String>();
		for(Element de : deviceElements) {

			if(de.getChildText("Name", ns) != null)
				devices.add(de.getChildText("Name", ns));
		}	
		List<Element> sectionElements = 
				methodDocument.getRootElement().getChildren("Section", ns);
		
		String pumpSection = null;
		if(devices.contains(BINARY_PUMP))
			pumpSection = BINARY_PUMP;
		
		if(devices.contains(QUAT_PUMP))
			pumpSection = QUAT_PUMP;
		
		double stopTime = -1.0d;
		double columnTemperature = -100.0d;
		double flow = -1.0d;
		List<Element>solventCompositionElements = new ArrayList<Element>();
		List<Element>timeTableElements = new ArrayList<Element>();
		
		for(Element sectionElement : sectionElements) {
							
			String module = sectionElement.getChildText("ModuleDisplayName", ns);
			String parId = sectionElement.getChildText("ParameterID", ns);
			String tableName = sectionElement.getChildText("TableName", ns);
			
			if(module == null || parId == null)
				continue;
			
			if(module.equals(pumpSection)) {
				
				if(parId.equals(STOP_TIME) 
						&& sectionElement.getChildText("ParameterValue", ns) != null) {
					
					try {
						stopTime = Double.parseDouble(sectionElement.getChildText("ParameterValue", ns));
					} catch (NumberFormatException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}					
				}
				if(parId.equals(FLOW) 
						&& sectionElement.getChildText("ParameterValue", ns) != null) {
					
					try {
						flow = Double.parseDouble(sectionElement.getChildText("ParameterValue", ns));
					} catch (NumberFormatException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}					
				}
				if(tableName != null) {
					
					if(tableName.equals(SOLVENT_COMPOSITION_TABLE_NAME))
						solventCompositionElements.add(sectionElement);
					
					if(tableName.equals(TIME_TABLE_NAME))
						timeTableElements.add(sectionElement);
				}
			}
			if(module.equals(COLUMN_COMP)) {
				
				if((parId.equals("LeftTemperatureControl_Temperature") 
						|| parId.equals("RightTemperatureControl_Temperature"))
						&& sectionElement.getChildText("ParameterValue", ns) != null) {
					try {
						columnTemperature = Double.parseDouble(sectionElement.getChildText("ParameterValue", ns));
					} catch (NumberFormatException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		if(solventCompositionElements.isEmpty() 
				|| timeTableElements.isEmpty() 
				|| flow < 0.0d) {
			
			if(solventCompositionElements.isEmpty())
				errorLog.add(rcDevicesXmlFile.getName() +  "\tNo mobile phase information extracted");
			
			if(timeTableElements.isEmpty())
				errorLog.add(rcDevicesXmlFile.getName() +  "\tNo timetable information extracted");
			
			if(flow < 0.0d)
				errorLog.add(rcDevicesXmlFile.getName() +  "\tInitial flow rate not specified");
		
			return null;
		}
		if(stopTime > -1.0d)
			grad.setStopTime(stopTime);
		
		if(columnTemperature > -100.0d)
			grad.setColumnCompartmentTemperature(columnTemperature);
		
		MobilePhase[]mobilePhases = 
				extractSolventComposition(solventCompositionElements, ns);
		for(int i=0; i<4; i++)
			grad.setMobilePhase(mobilePhases[i], i);
				
		Set<ChromatographicGradientStep>timeTable = 
				extractTimetable(timeTableElements, ns, flow);
		
		grad.getGradientSteps().addAll(timeTable);
		
		return grad;
	}
	
	private MobilePhase[]extractSolventComposition(
			List<Element> sectionElements, Namespace ns){
		
		Map<String,Integer>channelMap = new TreeMap<String,Integer>();
		channelMap.put("A", 0);
		channelMap.put("B", 1);
		channelMap.put("C", 2);
		channelMap.put("D", 3);
		
		MobilePhase[]solvents = new MobilePhase[4];
		Integer channelIndex = null;
		for(int i=0; i<sectionElements.size(); i++) {
			
			String parId = sectionElements.get(i).getChildText("ParameterID", ns);
			
			if(parId.equals("SolventComposition_Channel")) {
				String channel = sectionElements.get(i).getChildText("ParameterValue", ns);
				channelIndex = channelMap.get(channel);
				if(channelIndex == null) 
					throw new IllegalArgumentException("Unknown solvent channel: " + channel);
				
				solvents[channelIndex] = new MobilePhase(null, null);
			}
			if(parId.equals("SolventComposition_Channel1ExtendedSolventType_Parameter")
					|| parId.equals("SolventComposition_Channel2ExtendedSolventType_Parameter")) {
				String solventName = sectionElements.get(i).getChildText("ParameterValue", ns);
				if(solventName != null && !solventName.isEmpty() && solvents[channelIndex] != null)
					solvents[channelIndex].setName(solventName);
			}
			if(parId.equals("SolventComposition_Channel1UserName")
					|| parId.equals("SolventComposition_Channel2UserName")) {
				String solventUserName = sectionElements.get(i).getChildText("ParameterValue", ns);
				if(solventUserName != null && !solventUserName.isEmpty() && solvents[channelIndex] != null)
					solvents[channelIndex].getSynonyms().add(solventUserName);
			}
			if(parId.equals("SolventComposition_Used")) {
				
				String solventUsed = sectionElements.get(i).getChildText("ParameterValue", ns);
				if(solventUsed.equalsIgnoreCase("No"))
					solvents[channelIndex] = null;
			}
			if(parId.equals("SolventComposition_Percentage")) {
				
				String percentage = sectionElements.get(i).getChildText("ParameterValue", ns);
				double startingPercentage = -1.0d;				
				try {
					startingPercentage = Double.parseDouble(percentage);
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if(startingPercentage > -1.0d && solvents[channelIndex] != null)
					solvents[channelIndex].setStartingPercentage(startingPercentage);
			}
		}
		if(!doNotMatchMobilePhases) {
			
			for(int i=0; i<4; i++) {
				
				if(solvents[i] != null) {
					
					MobilePhase existing = IDTDataCache.getMobilePhaseByNameOrSynonym(solvents[i].getName());
					if(existing == null && !solvents[i].getSynonyms().isEmpty())
						existing = IDTDataCache.getMobilePhaseByNameOrSynonym(solvents[i].getSynonyms().iterator().next());
					
					if(existing == null) {
						
						String line = rcDevicesXmlFile.getName() +  "\tUnknown mobile phase\t" + solvents[i].getName();
						if(!solvents[i].getSynonyms().isEmpty())
							line += "\t" + solvents[i].getSynonyms().iterator().next();
						
						errorLog.add(line);
					}
					else 
						solvents[i] = existing;			
				}
			}
		}
		return solvents;
	}
	
	private Set<ChromatographicGradientStep>extractTimetable(
			List<Element> sectionElements, Namespace ns, double flow) {
		
		Set<ChromatographicGradientStep>gradientSteps = 
				new TreeSet<ChromatographicGradientStep>();
		Map<String,Integer>channelMap = new TreeMap<String,Integer>();
		channelMap.put("PercentA", 0);
		channelMap.put("PercentB", 1);
		channelMap.put("PercentC", 2);
		channelMap.put("PercentD", 3);
		
		ChromatographicGradientStep currentStep = null;
		for(int i=0; i<sectionElements.size(); i++) {
			
			String parId = sectionElements.get(i).getChildText("ParameterID", ns);
			if(parId.equalsIgnoreCase("Time")) {
				
				if(currentStep != null) {
					gradientSteps.add(currentStep);
					currentStep = null;
				}				
				double startTime = -1.0d;
				String timeString = sectionElements.get(i).getChildText("ParameterValue", ns);
				if(timeString.equals("Start. Cond."))
					startTime = 0.0d;
				else {
					try {
						startTime = Double.parseDouble(sectionElements.get(i).getChildText("ParameterValue", ns));
					} catch (NumberFormatException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				if(startTime > -1.0d)
					currentStep  = new ChromatographicGradientStep(startTime, flow);
			}
			if(channelMap.containsKey(parId)) {
				
				double solvPercent = -1.0d;
				String solvPercentString = sectionElements.get(i).getChildText("ParameterValue", ns);
				if(solvPercentString != null && NumberUtils.isCreatable(solvPercentString)) {
					
					try {
						solvPercent = Double.parseDouble(solvPercentString);
					} catch (NumberFormatException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				if(currentStep != null && solvPercent > -1.0d)
					currentStep.getMobilePhaseStartingPercent()[channelMap.get(parId)] = solvPercent;					
			}	
			if(parId.equalsIgnoreCase("Flow_DisplayValue")) {
				
				String flowString = sectionElements.get(i).getChildText("ParameterValue", ns);
				if(flowString != null && NumberUtils.isCreatable(flowString)) {
					
					double stepFlow = Double.parseDouble(flowString);
					if(currentStep != null)
						currentStep.setFlowRate(stepFlow);
				}
			}
		}
		if(currentStep != null)
			gradientSteps.add(currentStep);
		
		if(!gradientSteps.isEmpty()) {
			
			ChromatographicGradientStep firstStep =
					((TreeSet<ChromatographicGradientStep>)gradientSteps).first();
			if(firstStep.getStartTime() > 0) {
				
				ChromatographicGradientStep zeroStep = 
						new ChromatographicGradientStep(
						0.0d, 
						flow, 
						firstStep.getMobilePhaseStartingPercent()[0],
						firstStep.getMobilePhaseStartingPercent()[1], 
						firstStep.getMobilePhaseStartingPercent()[2], 
						firstStep.getMobilePhaseStartingPercent()[3]);
				gradientSteps.add(zeroStep);
			}
		}
		return gradientSteps;		
	}

	public List<String> getErrorLog() {
		return errorLog;
	}

	public void setDoNotMatchMobilePhases(boolean doNotMatchMobilePhases) {
		this.doNotMatchMobilePhases = doNotMatchMobilePhases;
	}
}










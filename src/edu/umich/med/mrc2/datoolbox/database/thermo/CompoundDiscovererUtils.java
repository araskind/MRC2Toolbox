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

package edu.umich.med.mrc2.datoolbox.database.thermo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import edu.umich.med.mrc2.datoolbox.data.thermo.ThermoActivationType;
import edu.umich.med.mrc2.datoolbox.data.thermo.ThermoBestHitType;
import edu.umich.med.mrc2.datoolbox.data.thermo.ThermoCDRawDatFile;
import edu.umich.med.mrc2.datoolbox.data.thermo.ThermoCDStudy;
import edu.umich.med.mrc2.datoolbox.data.thermo.ThermoCDWorkflow;
import edu.umich.med.mrc2.datoolbox.data.thermo.ThermoIonizationType;
import edu.umich.med.mrc2.datoolbox.data.thermo.ThermoMSFeature;
import edu.umich.med.mrc2.datoolbox.data.thermo.ThermoMSOrderType;
import edu.umich.med.mrc2.datoolbox.data.thermo.ThermoMassAnalyzerType;
import edu.umich.med.mrc2.datoolbox.data.thermo.ThermoPolarityType;
import edu.umich.med.mrc2.datoolbox.data.thermo.ThermoScanType;

public class CompoundDiscovererUtils {
	
	public static final DateFormat cdTimeStampFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a");

	public static ThermoCDStudy parseAnalysisDefinition(Connection conn) throws Exception {
		
		ThermoCDStudy study = null;
		
//		Document defXml = getAnalysisDefinitionDocument(conn);
//		if(defXml == null)
//			return null;
//		
//		XPathFactory factory = XPathFactory.newInstance();
//		XPath xpath = factory.newXPath();
//		XPathExpression expr = xpath.compile("//AnalysisDefinition/StudyDefinition");
//		NodeList studyNodes = (NodeList) expr.evaluate(defXml, XPathConstants.NODESET);
//		
//		Element studyElement = (Element) studyNodes.item(0);
//		study  = new ThermoCDStudy(studyElement.getAttribute("Name"));
//		study.setDescription(studyElement.getAttribute("Description"));
//		study.setDateCreated(cdTimeStampFormat.parse(studyElement.getAttribute("CreationDate")));
//		study.setLastModified(cdTimeStampFormat.parse(studyElement.getAttribute("LastChangeDate")));
//		
//		//	Read samples
//		expr = xpath.compile("//AnalysisDefinition/StudyDefinition/Samples/Sample");
//		NodeList sampleNodes = (NodeList) expr.evaluate(defXml, XPathConstants.NODESET);
//		for (int i = 0; i < sampleNodes.getLength(); i++) {
//
//			Element sampleElement = (Element) sampleNodes.item(i);		
//			ThermoCDSample sample = new ThermoCDSample(
//					sampleElement.getAttribute("Id"), 
//					sampleElement.getAttribute("Name"), 
//					sampleElement.getAttribute("FileSetId"));
//			study.addSample(sample);
//		}
//		//		Read raw data files
//		expr = xpath.compile("//AnalysisDefinition/StudyDefinition/FileSets/FileSet");
//		NodeList filesetNodes = (NodeList) expr.evaluate(defXml, XPathConstants.NODESET);
//		for (int i = 0; i < filesetNodes.getLength(); i++) {
//
//			Element filesetElement = (Element) filesetNodes.item(i);
//			String fileSetId = filesetElement.getAttribute("Id");
//			NodeList fileNodes  = filesetElement.getElementsByTagName("File");
//			for (int j = 0; j < fileNodes.getLength(); j++) {
//				
//				Element fileElement = (Element) fileNodes.item(j);
//				ThermoCDRawDatFile file = new ThermoCDRawDatFile(
//						fileElement.getAttribute("FileName"),
//						cdTimeStampFormat.parse(fileElement.getAttribute("FileTime")), 
//						Integer.parseInt(fileElement.getAttribute("FileSize").replace(" [Byte]", "")));
//				study.addFileForSample(file, fileSetId);
//			}
//		}
		return study;
	}
	
//	public static Document getAnalysisDefinitionDocument(Connection conn) throws Exception {
//		
//		String sql = "SELECT ANALYSISDEFINITIONXML FROM ANALYSISDEFINITION";
//		PreparedStatement ps = conn.prepareStatement(sql);
//		String adXml = null;
//		ResultSet rs = ps.executeQuery();
//		while(rs.next())
//			adXml = rs.getString(1);
//		
//		rs.close();
//		ps.close();
//		
//		if(adXml == null || adXml.isEmpty())
//			return null;
//		
//		Document xmlDocument = null;
//		try {
//			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
//			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
//			xmlDocument = dBuilder.parse(new InputSource(new StringReader(adXml)));
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		return xmlDocument;		
//	}
	
	public static Collection<ThermoCDWorkflow> getAnalysisWorkflows(Connection conn) throws Exception {
		
		Collection<ThermoCDWorkflow>workflows = new ArrayList<ThermoCDWorkflow>();
		String sql = 
				"SELECT WORKFLOWID, LEVEL, WORKFLOWNAME, WORKFLOWDESCRIPTION, "
				+ "WORKFLOWSTARTDATE, VERSION, SOFTWAREVERSION, "
				+ "WORKFLOWXML, MACHINENAME, WORKFLOWTYPE, STUDY "
				+ "FROM WORKFLOWS ORDER BY WORKFLOWID";
		PreparedStatement ps = conn.prepareStatement(sql);		
		ResultSet rs = ps.executeQuery();
		while(rs.next()) {
			
			ThermoCDWorkflow workflow = new ThermoCDWorkflow();
			workflow.setAnalysisWorkflowId(rs.getInt("WORKFLOWID"));
			workflow.setWorkflowLevel(rs.getInt("LEVEL"));
			workflow.setName(rs.getString("WORKFLOWNAME"));
			workflow.setDescription(rs.getString("WORKFLOWDESCRIPTION"));
			workflow.setStartDate(new Date(rs.getDate("WORKFLOWSTARTDATE").getTime()));
			workflow.setWorkflowVersion(rs.getInt("VERSION"));
			workflow.setSoftwareVersion(rs.getString("SOFTWAREVERSION"));			
			workflow.setWorkflowXml(rs.getString("WORKFLOWXML"));
			workflow.setMachineName(rs.getString("MACHINENAME"));
			workflow.setWorkflowType(rs.getString("WORKFLOWTYPE"));
			workflow.setStudy(rs.getString("STUDY"));			
			workflows.add(workflow);
		}		
		rs.close();
		ps.close();
		return workflows;
	}
	
	public static void mapStudyFilesForWorkflows(
			Collection<ThermoCDWorkflow>workflows, 
			ThermoCDStudy study, 
			Connection conn) throws Exception {
		
		String sql = 
				"SELECT I.ID, I.SAMPLE, I.SAMPLEIDENTIFIER, I.STUDYFILEID, I.FILENAME, " +
				"I.SAMPLETYPE, F.WORKFLOWINPUTFILESFILEID " +
				"FROM STUDYINFORMATION I, " +
				"STUDYINFORMATIONWORKFLOWINPUTFILES F " +
				"WHERE I.ID = F.STUDYINFORMATIONID " +
				"AND WORKFLOWINPUTFILESWORKFLOWID = ?";
		PreparedStatement ps = conn.prepareStatement(sql);	
		for(ThermoCDWorkflow workflow : workflows) {
			
			ps.setInt(1, workflow.getAnalysisWorkflowId());			
			ResultSet rs = ps.executeQuery();
			while(rs.next()) {
				
				String fileName = rs.getString("FILENAME");
				int wfFileId = rs.getInt("WORKFLOWINPUTFILESFILEID");
				ThermoCDRawDatFile file = study.getFileByName(fileName);
				if(file != null) {
					workflow.addDataFile(file, wfFileId);
				}
				else {
					System.out.println("Couldn't find the file " + fileName + " in the study data.");
				}
			}			
			rs.close();
		}
		ps.close();
	}
	
	public static void getMsFeaturesForWorkflow(
			ThermoCDWorkflow workflow, 
			Connection conn) throws Exception {
		
		Map<Integer, Collection<ThermoMSFeature>>dataMap = new TreeMap<Integer, Collection<ThermoMSFeature>>();
		workflow.getFileIdMap().values().stream().forEach(i -> dataMap.put(i, new HashSet<ThermoMSFeature>()));
		String sql = 
				"SELECT H.ID AS BHID, M.ID AS MSID, H.BESTHITTYPE,  " +
				"H.IONDESCRIPTION, H.CHARGE, H.MOLECULARWEIGHT, H.MASS, H.RETENTIONTIME AS BHRT, " +
				"M.RETENTIONTIME AS MSRT, H.INTENSITY, H.AREA, H.FILEID, M.MSORDER,  " +
				"M.POLARITY, M.RESOLUTIONATMASS200, M.ACTIVATIONTYPE, M.SCANTYPE, M.IONIZATION, " +
				"M.MASSANALYZER, M.ISOLATIONWIDTH " +
				"FROM BESTHITIONINSTANCEITEMS H,  " +
				"MASSSPECTRUMITEMS M,  " +
				"BESTHITIONINSTANCEITEMSMASSSPECTRUMITEMS L " +
				"WHERE H.WORKFLOWID = ? " +
				"AND M.WORKFLOWID = H.WORKFLOWID " +
				"AND L.BESTHITIONINSTANCEITEMSWORKFLOWID = H.WORKFLOWID " +
				"AND H.ID = L.BESTHITIONINSTANCEITEMSID " +
				"AND M.ID = L.MASSSPECTRUMITEMSID " +
				"ORDER BY H.ID ";
		PreparedStatement ps = conn.prepareStatement(sql);	
		ps.setInt(1, workflow.getAnalysisWorkflowId());
		ResultSet rs = ps.executeQuery();
		while(rs.next()) {
			
			ThermoMSFeature feature = new ThermoMSFeature();
			feature.setActivationType(ThermoActivationType.getThermoActivationTypeByCode(rs.getInt("ACTIVATIONTYPE")));
			feature.setAdduct(rs.getString("IONDESCRIPTION"));
			feature.setArea(rs.getDouble("AREA"));
			feature.setBestHitId(rs.getInt("BHID"));
			feature.setBestHitRt(rs.getDouble("BHRT"));
			feature.setBestHitType(ThermoBestHitType.getThermoBestHitTypeByCode(rs.getInt("BESTHITTYPE")));
			feature.setCharge(rs.getInt("CHARGE"));
			feature.setFileId(rs.getInt("FILEID"));
			feature.setIntensity(rs.getDouble("INTENSITY"));
			feature.setIonization(ThermoIonizationType.getThermoIonizationTypeByCode(rs.getInt("IONIZATION")));
			feature.setIsolationWidth(rs.getDouble("ISOLATIONWIDTH"));
			feature.setMassAnalyzer(ThermoMassAnalyzerType.getThermoMassAnalyzerTypeByCode(rs.getInt("MASSANALYZER")));
			feature.setMsId(rs.getInt("MSORDER"));
			feature.setMsOrder(ThermoMSOrderType.getThermoMSOrderTypeByCode(rs.getInt("MSORDER")));
			feature.setMw(rs.getDouble("MOLECULARWEIGHT"));
			feature.setMz(rs.getDouble("MASS"));
			feature.setPolarity(ThermoPolarityType.getThermoPolarityTypeByCode(rs.getInt("POLARITY")));
			feature.setResolutionAsMass200(rs.getInt("RESOLUTIONATMASS200"));
			feature.setRt(rs.getDouble("MSRT"));
			feature.setScanType(ThermoScanType.getThermoScanTypeByCode(rs.getInt("SCANTYPE")));

			dataMap.get(feature.getFileId()).add(feature);
			
			System.out.println(feature.toString());
		}
		rs.close();
		ps.close();		
		for(Entry<ThermoCDRawDatFile, Integer> entry: workflow.getFileIdMap().entrySet())			
			workflow.addFeaturesForFile(entry.getKey(), dataMap.get(entry.getValue()));	
		
		System.out.println("Done parsing MS features");
	}
}



























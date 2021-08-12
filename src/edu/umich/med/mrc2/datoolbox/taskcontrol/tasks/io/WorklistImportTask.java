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
import java.io.IOException;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.Worklist;
import edu.umich.med.mrc2.datoolbox.data.WorklistItem;
import edu.umich.med.mrc2.datoolbox.data.enums.AgilentSampleInfoFields;
import edu.umich.med.mrc2.datoolbox.data.enums.MSInstrumentVendor;
import edu.umich.med.mrc2.datoolbox.data.enums.WorklistImportType;
import edu.umich.med.mrc2.datoolbox.data.lims.DataAcquisitionMethod;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.utils.DelimitedTextParser;

public class WorklistImportTask extends AbstractTask {

	private Document xmlWorklist;
	private File sourceFileOrDirectory;
	private Worklist worklist;
	private DataAcquisitionMethod dataAcquisitionMethod;
	private DataAnalysisProject currentProject;
	private boolean appendWorklist;
	private MSInstrumentVendor vendor;
	private WorklistImportType importType;

	//	TODO - handle different instrument vendors
	public WorklistImportTask(
			File sourceFileOrDirectory,
			DataAcquisitionMethod dataAcquisitionMethod,
			boolean appendWorklist,
			WorklistImportType importType) {
		super();
		this.sourceFileOrDirectory = sourceFileOrDirectory;
		this.dataAcquisitionMethod = dataAcquisitionMethod;
		this.importType = importType;
		this.appendWorklist = appendWorklist;
		
		taskDescription = "Importing worklist data from " + 
				sourceFileOrDirectory.getName();
		
		if(dataAcquisitionMethod != null) 
			taskDescription += " for " + dataAcquisitionMethod.getName();
		
		worklist = null;
		processed = 0;
		total = 100;
		currentProject = MRC2ToolBoxCore.getCurrentProject();		
	}

	@Override
	public void run() {

		setStatus(TaskStatus.PROCESSING);

		if(importType.equals(WorklistImportType.VENDOR_WORKLIST)) {
			try {
				readWorklistFile();
			} catch (Exception e) {

				e.printStackTrace();
				setStatus(TaskStatus.ERROR);
			}
		}
		if(importType.equals(WorklistImportType.RAW_DATA_DIRECTORY_SCAN)) {

			if (sourceFileOrDirectory == null || !sourceFileOrDirectory.exists() || !sourceFileOrDirectory.canRead()) {
				setStatus(TaskStatus.ERROR);
				return;
			}
			IOFileFilter dotDfilter = 
					FileFilterUtils.makeDirectoryOnly(new RegexFileFilter(".+\\.[dD]$"));
			Collection<File> dotDfiles = FileUtils.listFilesAndDirs(
					sourceFileOrDirectory,
					DirectoryFileFilter.DIRECTORY,
					dotDfilter);

			if (!dotDfiles.isEmpty()) {

				try {
					scanDirectoryForSampleInfo(dotDfiles);
				} catch (Exception e) {

					e.printStackTrace();
					setStatus(TaskStatus.ERROR);
				}
			}
		}
		if(importType.equals(WorklistImportType.PLAIN_TEXT_FILE)) {
			//	TODO
		}
		if(importType.equals(WorklistImportType.EXCEL_FILE)) {
			//	TODO
		}
		setStatus(TaskStatus.FINISHED);
	}

	@Override
	public Task cloneTask() {
		return new WorklistImportTask(sourceFileOrDirectory, dataAcquisitionMethod, appendWorklist, importType);
	}

	public Worklist getWorklist() {
		return worklist;
	}

	//	TODO implement reading Agilent worklist
	private void readAgilentWorklist() throws Exception {

		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		xmlWorklist = dBuilder.parse(sourceFileOrDirectory);

		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();
		NodeList itemNodes;

		XPathExpression expr = xpath.compile("//WorklistManager/WorklistInfo/JobDataList/JobData/SampleInfo");
		itemNodes = (NodeList) expr.evaluate(xmlWorklist, XPathConstants.NODESET);
	}

	private void readWorklistFile() throws Exception {

		// Check if worklist is TSV or XML
		String extension = FilenameUtils.getExtension(sourceFileOrDirectory.getName());
		worklist = new Worklist();

		if (extension.equalsIgnoreCase("TXT"))
			readWorklistFromTextFile();

		if (extension.equalsIgnoreCase("WKL"))
			readAgilentWorklist();
	}

	private void readWorklistFromTextFile() {

		String[][] worklistData = null;
		try {
			worklistData = DelimitedTextParser.parseTextFileWithEncoding(
					sourceFileOrDirectory, MRC2ToolBoxConfiguration.getTabDelimiter());
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		if(worklistData == null)
			return;

		String[] header = worklistData[0];
		int timeIndex = -1;
		int fileIndex = -1;
		File dataFile;
		String fileName, baseName;

		total = worklistData.length;
		processed = 0;

		DateFormat dFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		// Parse header
		TreeMap<String, Integer> fieldMap = new TreeMap<String, Integer>();
		for (int i = 0; i < header.length; i++) {

			for (AgilentSampleInfoFields field : AgilentSampleInfoFields.values()) {

				if (header[i].equals(field.getName()))
					fieldMap.put(field.getName(), i);
			}
		}
		if (fieldMap.get(AgilentSampleInfoFields.ACQUISITION_TIME.getName()) != null)
			timeIndex = fieldMap.get(AgilentSampleInfoFields.ACQUISITION_TIME.getName());
		else
			timeIndex = fieldMap.get(AgilentSampleInfoFields.ACQTIME.getName());

		fileIndex = fieldMap.get(AgilentSampleInfoFields.DATA_FILE.getName());

		Set<DataFile> assayFiles = 
				currentProject.getDataFilesForAcquisitionMethod(dataAcquisitionMethod);

		for (int i = 1; i < worklistData.length; i++) {

			fileName = FilenameUtils.getName(worklistData[i][fileIndex]);
			baseName = FilenameUtils.getBaseName(worklistData[i][fileIndex]);

			WorklistItem newItem = null;

			String timeString = worklistData[i][timeIndex];
			timeString = timeString.replace('T', ' ').replace('Z', ' ').trim();
			Date injectionTime = null;
			try {
				injectionTime = dFormat.parse(timeString);
			} catch (ParseException e) {
				// e.printStackTrace();
			}
			// Update files in the project
			for (DataFile df : assayFiles) {

				if (df.getName().equals(baseName) && df.getInjectionTime() == null) {

					newItem = new WorklistItem(df);
					newItem.setTimeStamp(injectionTime);
					df.setInjectionTime(injectionTime);
					break;
				}
			}
			// Ignore files not present in raw data
			// Create new data file if not in the project already, and new
			// worklist item
			// if(newItem == null){
			//
			// DataFile newFile = new DataFile(fileName, assay);
			// newFile.setInjectionTime(injectionTime);
			// newFile.setFullPath(worklistData[i][fileIndex]);
			//
			// currentProject.getDataFilesForAssay(assay).add(newFile);
			//
			// newItem = new WorklistItem(newFile);
			// newItem.setTimeStamp(injectionTime);
			// }
			// Fill in all worklist item properties
			if (newItem != null) {

				for (Entry<String, Integer> entry : fieldMap.entrySet())
					newItem.addProperty(entry.getKey(), worklistData[i][entry.getValue()]);

				worklist.addItem(newItem);
			}
			processed++;
		}
	}

	private Document readXmlFile(File file) throws Exception {

		Document sampleInfo = null;
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		sampleInfo = dBuilder.parse(file);
		return sampleInfo;
	}

	private void scanDirectoryForSampleInfo(Collection<File> dataDiles) throws Exception {

		String baseName, timeString;
		DateFormat dFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();
		XPathExpression expr = xpath.compile("//SampleInfo/Field");

		total = dataDiles.size();
		processed = 0;
		worklist = new Worklist();
		Set<DataFile> assayFiles = 
				currentProject.getDataFilesForAcquisitionMethod(dataAcquisitionMethod);

		for (File df : dataDiles) {

			baseName = null;
			timeString = null;
			Date injectionTime = null;
			File sampleInfoFile = Paths.get(df.getAbsolutePath(), "AcqData", "sample_info.xml").toFile();
			if (sampleInfoFile.exists()) {

				Document sampleInfo = readXmlFile(sampleInfoFile);
				if (sampleInfo != null) {

					NodeList fieldNodes = (NodeList) expr.evaluate(sampleInfo, XPathConstants.NODESET);
					TreeMap<String, String> sampleData = new TreeMap<String, String>();
					for (int i = 0; i < fieldNodes.getLength(); i++) {

						Element fieldElement = (Element) fieldNodes.item(i);

						String name = fieldElement.getElementsByTagName("Name").
								item(0).getFirstChild().getNodeValue().trim();
						String value = fieldElement.getElementsByTagName("Value").
								item(0).getFirstChild().getNodeValue().trim();

						if (name != null)
							sampleData.put(name, value);
					}
					baseName = FilenameUtils.getBaseName(df.getName());
					if (sampleData.get(AgilentSampleInfoFields.ACQUISITION_TIME.getName()) != null)
						timeString = sampleData.get(AgilentSampleInfoFields.ACQUISITION_TIME.getName());
					else {
						if (sampleData.get(AgilentSampleInfoFields.ACQTIME.getName()) != null)
							timeString = sampleData.get(AgilentSampleInfoFields.ACQTIME.getName());
					}
					if (timeString != null) {

						timeString = timeString.replace('T', ' ').replace('Z', ' ').trim();
						try {
							injectionTime = dFormat.parse(timeString);
						} catch (ParseException e) {
							// e.printStackTrace();
						}
					}
					for (DataFile af : assayFiles) {

						if (af.getName().equals(baseName)) {

							WorklistItem newItem = new WorklistItem(af);
							newItem.setTimeStamp(injectionTime);
							af.setInjectionTime(injectionTime);
							for (Entry<String, String> entry : sampleData.entrySet())
								newItem.addProperty(entry.getKey(), entry.getValue());

							worklist.addItem(newItem);
							break;
						}
					}
				}
			}
			processed++;
		}
	}

	public boolean isAppendWorklist() {
		return appendWorklist;
	}
	
	public DataAcquisitionMethod getDataAcquisitionMethod() {
		return dataAcquisitionMethod;
	}
}

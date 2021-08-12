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
import java.nio.file.Paths;
import java.util.Collection;
import java.util.TreeMap;
import java.util.TreeSet;

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
import org.w3c.dom.NodeList;

import edu.umich.med.mrc2.datoolbox.data.Worklist;
import edu.umich.med.mrc2.datoolbox.data.enums.MSInstrumentVendor;
import edu.umich.med.mrc2.datoolbox.data.enums.WorklistImportType;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;

public class LIMSWorklistAcquisitiomMethodScanTask extends AbstractTask {

	private Document xmlWorklist;
	private File sourceFileOrDirectory;
	private Worklist worklist;
	private MSInstrumentVendor vendor;
	private WorklistImportType importType;
	private TreeMap<String,File>methodMap;
	private Collection<File> rawDataFiles;

	//	TODO - handle different instrument vendors
	public LIMSWorklistAcquisitiomMethodScanTask(
			File inFile,
			WorklistImportType importType) {
		super();
		sourceFileOrDirectory = inFile;
		this.importType = importType;
		taskDescription = "Scanning worklist data from " + sourceFileOrDirectory.getName();
		worklist = null;
		processed = 0;
		total = 100;
		rawDataFiles = new TreeSet<File>();
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

			if (sourceFileOrDirectory == null) {
				setStatus(TaskStatus.ERROR);
				return;
			}
			if(!sourceFileOrDirectory.exists()) {
				setStatus(TaskStatus.ERROR);
				return;
			}
			methodMap = new TreeMap<String,File>();
			IOFileFilter dotDfilter = 
					FileFilterUtils.makeDirectoryOnly(new RegexFileFilter(".+\\.[dD]$"));
			rawDataFiles = FileUtils.listFilesAndDirs(
					sourceFileOrDirectory,
					DirectoryFileFilter.DIRECTORY,
					dotDfilter);

			rawDataFiles.remove(sourceFileOrDirectory);
			if (!rawDataFiles.isEmpty()) {
				collectMethodsFromAgilentRawDataFiles();
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
	
	private void collectMethodsFromAgilentRawDataFiles() {		
		
		IOFileFilter dotMfilter = 
				FileFilterUtils.makeDirectoryOnly(new RegexFileFilter(".+\\.[mM]$"));				
		for(File dotd : rawDataFiles) {

			File acqDataDir = Paths.get(dotd.getAbsolutePath(), "AcqData").toFile();
			Collection<File> methodFiles = FileUtils.listFilesAndDirs(acqDataDir, DirectoryFileFilter.DIRECTORY, dotMfilter);
			methodFiles.remove(acqDataDir);
			File mmf = methodFiles.stream().findFirst().orElse(null);
			if(mmf != null)
				methodMap.put(mmf.getName(), mmf);				
		}
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
/*
		String[][] worklistData = null;
		try {
			worklistData = DelimitedTextParser.parseTextFileWithEncoding(sourceFileOrDirectory, CefAnalyzerCore.getDataDelimiter());
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

		Set<DataFile> assayFiles = currentProject.getAllDataFilesForAssay(assay);

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

				if (df.getFileName().equals(baseName) && df.getInjectionTime() == null) {

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
		}*/
	}

	private Document readXmlFile(File file) throws Exception {

		Document sampleInfo = null;
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		sampleInfo = dBuilder.parse(file);
		return sampleInfo;
	}

	public Worklist getWorklist() {
		return worklist;
	}

	@Override
	public Task cloneTask() {
		return new LIMSWorklistAcquisitiomMethodScanTask(
			sourceFileOrDirectory, importType);
	}
	
	public TreeMap<String, File> getMethodNameToFileMap() {
		return methodMap;
	}
}

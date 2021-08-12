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
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;
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
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.Worklist;
import edu.umich.med.mrc2.datoolbox.data.WorklistItem;
import edu.umich.med.mrc2.datoolbox.data.enums.AgilentSampleInfoFields;
import edu.umich.med.mrc2.datoolbox.data.enums.MSInstrumentVendor;
import edu.umich.med.mrc2.datoolbox.data.enums.WorklistImportType;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.utils.DelimitedTextParser;
import edu.umich.med.mrc2.datoolbox.utils.FIOUtils;

public class WorklistExtractionTask extends AbstractTask {

	private Document xmlWorklist;
	private File sourceFileOrDirectory, outputFile;
	private Worklist worklist;
	private MSInstrumentVendor vendor;
	private WorklistImportType importType;
	private boolean writeWorklistToFile;
	private boolean appendWorklist;

	//	TODO - handle different instrument vendors
	public WorklistExtractionTask(
			File sourceFileOrDirectory,
			WorklistImportType importType) {
		this(sourceFileOrDirectory, importType, true, false);		
	}
	
	public WorklistExtractionTask(
			File sourceFileOrDirectory,
			WorklistImportType importType,
			boolean writeWorklistToFile,
			boolean appendWorklist) {
		super();
		this.sourceFileOrDirectory = sourceFileOrDirectory;
		this.importType = importType;
		this.writeWorklistToFile = writeWorklistToFile;
		this.appendWorklist = appendWorklist;
		taskDescription = "Importing worklist data from " + 
				sourceFileOrDirectory.getName();
		worklist = null;
		processed = 0;
		total = 100;		
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
		if(writeWorklistToFile)
			writeOutExtractedWorklist();
		
		setStatus(TaskStatus.FINISHED);
	}

	@Override
	public Task cloneTask() {
		return new WorklistExtractionTask(
				sourceFileOrDirectory, 
				importType, 
				writeWorklistToFile, 
				appendWorklist);
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

		for (int i = 1; i < worklistData.length; i++) {

			fileName = FilenameUtils.getName(worklistData[i][fileIndex]);
			baseName = FilenameUtils.getBaseName(worklistData[i][fileIndex]);
			DataFile df = new DataFile(baseName);
			WorklistItem newItem = null;
			String timeString = worklistData[i][timeIndex];
			timeString = timeString.replace('T', ' ').replace('Z', ' ').trim();
			Date injectionTime = null;
			try {
				injectionTime = dFormat.parse(timeString);
			} catch (ParseException e) {
				// e.printStackTrace();
			}
			newItem = new WorklistItem(df);
			newItem.setTimeStamp(injectionTime);
			df.setInjectionTime(injectionTime);

			for (Entry<String, Integer> entry : fieldMap.entrySet())
				newItem.addProperty(entry.getKey(), worklistData[i][entry.getValue()]);

			worklist.addItem(newItem);		
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

		DateFormat dFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();
		XPathExpression expr = xpath.compile("//SampleInfo/Field");

		total = dataDiles.size();
		processed = 0;
		worklist = new Worklist();

		for (File df : dataDiles) {
		
			String baseName = FilenameUtils.getBaseName(df.getName());
			DataFile af = new DataFile(baseName);
			String timeString = null;
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
					WorklistItem newItem = new WorklistItem(af);
					newItem.setTimeStamp(injectionTime);
					af.setInjectionTime(injectionTime);
					for (Entry<String, String> entry : sampleData.entrySet())
						newItem.addProperty(entry.getKey(), entry.getValue());

					worklist.addItem(newItem);
				}
			}
			processed++;
		}
	}
	
	private void writeOutExtractedWorklist() {
		
		if(worklist == null || worklist.getWorklistItems().isEmpty())
			return;
		
		
		if(sourceFileOrDirectory.isDirectory())
			outputFile = Paths.get(
					sourceFileOrDirectory.getAbsolutePath(), 
					FilenameUtils.getBaseName(sourceFileOrDirectory.getName()) + 
					"_WORKLIST_" + FIOUtils.getTimestamp() + ".TXT").toFile();
		
		
		if(sourceFileOrDirectory.isFile())
			outputFile = Paths.get(
					sourceFileOrDirectory.getParentFile().getAbsolutePath(), 
					FilenameUtils.getBaseName(sourceFileOrDirectory.getName()) + 
					"_WORKLIST_" + FIOUtils.getTimestamp() + ".TXT").toFile();
		
		
		String[] columnNames = getWorklistColumns();
		String worklistString = StringUtils.join(columnNames, "\t") + "\n";
		for (WorklistItem item : worklist.getTimeSortedWorklistItems()) {

			String[] newRow = new String[columnNames.length];

			for (int i = 0; i < columnNames.length; i++) {

				if (columnNames[i].equals(AgilentSampleInfoFields.ACQUISITION_TIME.getName())
						|| columnNames[i].equals(AgilentSampleInfoFields.ACQTIME.getName()))
					newRow[i] = MRC2ToolBoxConfiguration.getDateTimeFormat().format(item.getTimeStamp());
				else if (columnNames[i].equals(AgilentSampleInfoFields.DATA_FILE.getName()))
					newRow[i] = item.getDataFile().getName();
				else if (columnNames[i].equals(AgilentSampleInfoFields.INJ_VOL.getName())) {
					 if(NumberUtils.isCreatable(item.getProperty(AgilentSampleInfoFields.INJ_VOL.getName())))
						 newRow[i] = item.getProperty(AgilentSampleInfoFields.INJ_VOL.getName());
					 else
						 newRow[i] = "0.0"; // newRow[i] = item.getProperty(AgilentSampleInfoFields.INJ_VOL.getName());
				}
				else
					newRow[i] = item.getProperty(columnNames[i]);
			}
			worklistString += StringUtils.join(newRow, "\t") + "\n";
		}		
		if(outputFile != null) {
			
			try {
				FileUtils.writeStringToFile(outputFile, worklistString, Charset.defaultCharset());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private String[] getWorklistColumns() {

		Collection<String> allColumnNames = new TreeSet<String>();
		for (WorklistItem item : worklist.getTimeSortedWorklistItems())
			allColumnNames.addAll(item.getProperties().keySet());

		HashMap<String, Integer> valueCount = new HashMap<String, Integer>();

		for (String field : allColumnNames) {

			valueCount.put(field, 0);

			for (WorklistItem item : worklist.getTimeSortedWorklistItems()) {

				if(item.getProperty(field) == null)
					continue;

				if (!item.getProperty(field).isEmpty()) {
					Integer current = valueCount.get(field) + 1;
					valueCount.replace(field, current);
				}
			}
		}
		Collection<String>columnNames = new ArrayList<String>();
		for (Entry<String, Integer> entry : valueCount.entrySet()) {

			if (entry.getValue() > 0) 
				columnNames.add(entry.getKey());
		}
		return columnNames.toArray(new String[columnNames.size()]);
	}
	
	public File getOutputFile() {
		return outputFile;
	}

	public boolean isAppendWorklist() {
		return appendWorklist;
	}
}

















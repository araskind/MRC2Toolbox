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

package edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.io;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.jdom2.Document;
import org.jdom2.Element;

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.Worklist;
import edu.umich.med.mrc2.datoolbox.data.WorklistItem;
import edu.umich.med.mrc2.datoolbox.data.enums.AgilentSampleInfoFields;
import edu.umich.med.mrc2.datoolbox.data.enums.MSInstrumentVendor;
import edu.umich.med.mrc2.datoolbox.data.enums.WorklistImportType;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.utils.DelimitedTextParser;
import edu.umich.med.mrc2.datoolbox.utils.FIOUtils;
import edu.umich.med.mrc2.datoolbox.utils.XmlUtils;

public abstract class WorklistTask extends AbstractTask {

	protected Document xmlWorklist;
	protected File sourceFileOrDirectory;
	protected Worklist worklist;
	protected MSInstrumentVendor vendor;
	protected WorklistImportType importType;
	protected File outputFile;

	public WorklistTask(
			File sourceFileOrDirectory,
			WorklistImportType importType) {
		super();
		this.sourceFileOrDirectory = sourceFileOrDirectory;
		this.importType = importType;
	}

	protected void readWorklistFile() throws Exception {

		// Check if worklist is TSV or XML
		String extension = 
				FilenameUtils.getExtension(sourceFileOrDirectory.getName());
		worklist = new Worklist();

		if (extension.equalsIgnoreCase("TXT"))
			readWorklistFromTextFile();

		if (extension.equalsIgnoreCase("WKL"))
			readAgilentWorklist();
	}
	
	//	TODO implement reading Agilent worklist
	protected void readAgilentWorklist() throws Exception {

//		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
//		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
//		xmlWorklist = dBuilder.parse(sourceFileOrDirectory);
//
//		XPathFactory factory = XPathFactory.newInstance();
//		XPath xpath = factory.newXPath();
//		NodeList itemNodes;
//
//		XPathExpression expr = xpath.compile("//WorklistManager/WorklistInfo/JobDataList/JobData/SampleInfo");
//		itemNodes = (NodeList) expr.evaluate(xmlWorklist, XPathConstants.NODESET);
	}

	protected void readWorklistFromTextFile() {

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
				newItem.setProperty(entry.getKey(), worklistData[i][entry.getValue()]);

			worklist.addItem(newItem);		
			processed++;
		}
	}

	protected void scanDirectoryForSampleInfo(Collection<File> dataFiles) throws Exception {

		DateFormat dFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		total = dataFiles.size();
		processed = 0;
		worklist = new Worklist();

		for (File df : dataFiles) {
		
			String baseName = FilenameUtils.getBaseName(df.getName());
			DataFile af = new DataFile(baseName);
			String timeString = null;
			Date injectionTime = null;
			File sampleInfoFile = Paths.get(df.getAbsolutePath(), "AcqData", "sample_info.xml").toFile();
			File peakBinaryFile = Paths.get(df.getAbsolutePath(), "AcqData", "MSPeak.bin").toFile();
			if (sampleInfoFile.exists()) {

				Document sampleInfo = XmlUtils.readXmlFile(sampleInfoFile);
				if (sampleInfo != null) {

					List<Element> fieldElements = sampleInfo.getRootElement().getChildren("Field");
					TreeMap<String, String> sampleData = new TreeMap<String, String>();
					for (Element fieldElement : fieldElements) {

						String name = fieldElement.getChild("Name").getText().trim();
						Element valueElement = fieldElement.getChild("Value");
						if (valueElement != null)
							sampleData.put(name, fieldElement.getChild("Value").getText().trim());
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
						newItem.setProperty(entry.getKey(), entry.getValue());

					if(peakBinaryFile.exists()) {
						long fileSize = Files.size(peakBinaryFile.toPath());
						newItem.setProperty(
								AgilentSampleInfoFields.PEAK_BINARY_FILE_SIZE.getName(), Long.toString(fileSize));
					}						
					worklist.addItem(newItem);
				}
			}			
			processed++;
		}
	}
	
	protected void writeOutExtractedWorklist() {
		
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
	
	protected String[] getWorklistColumns() {

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
	
	protected Collection<File> getAgiletDFileList(File sorceDirectory){
		
		if (sorceDirectory == null || !sorceDirectory.exists())
			return new ArrayList<File>();
		
		IOFileFilter dotDfilter = new RegexFileFilter(".+\\.[dD]$");
		dotDfilter = FileFilterUtils.makeDirectoryOnly(new RegexFileFilter(".+\\.[dD]$"));
		Collection<File> dataFiles = FileUtils.listFilesAndDirs(
				sorceDirectory,
				DirectoryFileFilter.DIRECTORY,
				dotDfilter);
		dataFiles.remove(sorceDirectory);
		return dataFiles;
	}

	public Worklist getWorklist() {
		return worklist;
	}

	public File getOutputFile() {
		return outputFile;
	}

	public void setOutputFile(File outputFile) {
		this.outputFile = outputFile;
	}
}

















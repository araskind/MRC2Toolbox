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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
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
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.ExperimentalSample;
import edu.umich.med.mrc2.datoolbox.data.Worklist;
import edu.umich.med.mrc2.datoolbox.data.enums.AgilentSampleInfoFields;
import edu.umich.med.mrc2.datoolbox.data.enums.MSInstrumentVendor;
import edu.umich.med.mrc2.datoolbox.data.enums.WorklistImportType;
import edu.umich.med.mrc2.datoolbox.data.lims.DataAcquisitionMethod;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSExperiment;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSSamplePreparation;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSWorklistItem;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCash;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;

public class LIMSWorklistImportTask extends AbstractTask {

	private Document xmlWorklist;
	private File sourceFileOrDirectory;
	private Worklist worklist;
	private MSInstrumentVendor vendor;
	private WorklistImportType importType;
	private LIMSExperiment limsExperiment;
	private LIMSSamplePreparation samplePrep;
	private Collection<String>missingMethods;

	//	TODO - handle different instrument vendors
	public LIMSWorklistImportTask(
			File inFile,
			WorklistImportType importType,
			LIMSExperiment limsExperiment,
			LIMSSamplePreparation samplePrep) {
		super();
		sourceFileOrDirectory = inFile;
		this.importType = importType;
		this.limsExperiment = limsExperiment;
		this.samplePrep = samplePrep;
		taskDescription = "Importing worklist data from " + sourceFileOrDirectory.getName();
		worklist = null;
		processed = 0;
		total = 100;
		missingMethods = new TreeSet<String>();
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
			IOFileFilter dotDfilter = new RegexFileFilter(".+\\.[dD]$");
			dotDfilter = FileFilterUtils.makeDirectoryOnly(new RegexFileFilter(".+\\.[dD]$"));
			Collection<File> dataDiles = FileUtils.listFilesAndDirs(
					sourceFileOrDirectory,
					DirectoryFileFilter.DIRECTORY,
					dotDfilter);

			if (!dataDiles.isEmpty()) {

				try {
					scanDirectoryForSampleInfo(dataDiles);
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

	private void scanDirectoryForSampleInfo(Collection<File> dataDiles) throws Exception {

		String baseName, timeString;
		DateFormat dFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();
		XPathExpression expr = xpath.compile("//SampleInfo/Field");
		total = dataDiles.size();
		processed = 0;
		worklist = new Worklist();
		Collection<DataAcquisitionMethod> methods = IDTDataCash.getAcquisitionMethods();
		TreeSet<ExperimentalSample> samples = limsExperiment.getExperimentDesign().getSamples();

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
					//	Acquisition method
					File acqMethodFile = new File(sampleData.get(AgilentSampleInfoFields.METHOD.getName()));
					String nameNoExtension = FilenameUtils.getBaseName(acqMethodFile.getName());
					DataAcquisitionMethod limsMethod = methods.stream().
							filter(m -> (m.getName().equals(nameNoExtension)||
							m.getName().equals(acqMethodFile.getName()))).findFirst().orElse(null);

					if(limsMethod == null) {
						missingMethods.add(acqMethodFile.getName());
						continue;
					}
					//	Find matching sample
					String sampleId = sampleData.get(AgilentSampleInfoFields.SAMPLE_ID.getName());
					String sampleName = sampleData.get(AgilentSampleInfoFields.SAMPLE_NAME.getName());
					ExperimentalSample sample = samples.stream().
						filter(s -> (s.getId().equals(sampleId) || s.getName().equals(sampleName))).
						findFirst().orElse(null);

					File rawDataFile = new File(sampleData.get(AgilentSampleInfoFields.DATA_FILE.getName()));
					DataFile rdf = new DataFile(rawDataFile.getName());
					rdf.setFullPath(rawDataFile.getAbsolutePath());
					LIMSWorklistItem newItem = new LIMSWorklistItem(rdf);
					newItem.setSample(sample);
					newItem.setAcquisitionMethod(limsMethod);
					newItem.setSamplePrep(samplePrep);

					//	TODO handle multiple preps for sample
					if(sample != null)
						newItem.setPrepItemId(samplePrep.getPrepItemsForSample(sample.getId()).iterator().next());

					newItem.setTimeStamp(injectionTime);
					rdf.setInjectionTime(injectionTime);
					double injectionVolume = Double.parseDouble(sampleData.get(AgilentSampleInfoFields.INJ_VOL.getName()));
					newItem.setInjectionVolume(injectionVolume);

					for (Entry<String, String> entry : sampleData.entrySet())
						newItem.addProperty(entry.getKey(), entry.getValue());

					worklist.addItem(newItem);
				}
			}
			processed++;
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
		return new LIMSWorklistImportTask(
			sourceFileOrDirectory, importType, limsExperiment, samplePrep);
	}

	/**
	 * @return the missingMethods
	 */
	public Collection<String> getMissingMethods() {
		return missingMethods;
	}

}

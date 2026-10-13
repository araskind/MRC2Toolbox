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

package edu.umich.med.mrc2.datoolbox.dmutils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jdom2.Document;
import org.jdom2.Element;

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.Worklist;
import edu.umich.med.mrc2.datoolbox.data.WorklistItem;
import edu.umich.med.mrc2.datoolbox.data.compare.ChromatographicGradientComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.enums.AgilentSampleInfoFields;
import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.data.lims.ChromatographicGradient;
import edu.umich.med.mrc2.datoolbox.data.lims.MobilePhase;
import edu.umich.med.mrc2.datoolbox.utils.FIOUtils;
import edu.umich.med.mrc2.datoolbox.utils.XmlUtils;
import edu.umich.med.mrc2.datoolbox.utils.acqmethod.AgilentAcquisitionMethodBatchUtils;
import edu.umich.med.mrc2.datoolbox.utils.acqmethod.AgilentAcquisitionMethodParser;
import edu.umich.med.mrc2.datoolbox.utils.acqmethod.ChromatographicGradientUtils;
import edu.umich.med.mrc2.datoolbox.utils.acqmethod.ExtractedAgilentAcquisitionMethodParser;

public class AgilentRunDataExtractor {

	private static final DateFormat dFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	private static Map<WorklistItem,String>gradientMap;
	private static List<ChromatographicGradient>chromatographicGradientList;
	private static final String GRADIENT_ID = "Gradient ID";
	
	public static void main(String[] args) {

//		File rootFolder = new File("Z:\\Reference Data\\Individual Standards run on QTOFs\\LC10_LC2-IonP");
		File rootFolder = new File("S:\\DataAnalysis\\__Standards");
		
//		File excelReportFile = new File("Z:\\Reference Data\\Individual Standards run on QTOFs\\LC10_LC2-IonP\\MethodReport.xlsx");
//		gradientMap = new HashMap<>();
//		chromatographicGradientList = new ArrayList<>();
//		System.out.println("Extracting data\n**************\n");
//		Worklist worklist = extractRunData(rootFolder);
//		System.out.println("\nWriting the report\\n**************\\n");
//		try {
//			//	createReport(worklist, excelReportFile);.
//			
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		extractSampleSCICDevicesFile();
		
		 extractPolarityData(rootFolder);
	}
	
	private static void extractSampleSCICDevicesFile() {

		 File reportFile = new File("Z:\\Reference Data\\Individual Standards run on QTOFs\\LC10_LC2-IonP\\2-OH butyrate-01.d\\AcqData\\AcqMethod.xml");
		 //AgilentAcquisitionMethodBatchUtils.extractSCICDevicesDocumentFromAcqMethodReportFile(reportFile);
		 String pol = extractPolarity(reportFile);
		 System.out.println("***\n" + pol + "***");
	}
	
	private static String extractPolarity(File acqMethodFile) {
		
		if(!acqMethodFile.exists())
			return null;
		
		Document scicDevicesDocument = 
				AgilentAcquisitionMethodBatchUtils.getSCICDevicesDocumentFromAcqMethodReportFile(acqMethodFile);
		for(Element sectionInfoElement : scicDevicesDocument.getRootElement().getChildren("SectionInfo")) {
			
			if(sectionInfoElement.getChildText("ID").equals("ionPolarity"))
				return sectionInfoElement.getChildText("Value");
		}
		return null;
	}
	
	private static void extractPolarityData(File rootFolder) {
		
		List<String>polarityMap = new ArrayList<>();
		List<Path> dataFilesList = FIOUtils.findDirectoriesByExtension(rootFolder.toPath(), ".D");
		for (Path dataFilePath : dataFilesList) {
			
			File df = dataFilePath.toFile();
			File acqMethodFile = Paths.get(df.getAbsolutePath(), "AcqData", "AcqMethod.xml").toFile();
			String polarity =  extractPolarity(acqMethodFile);
			if(polarity == null)
				polarity = "";
			
			polarityMap.add(dataFilePath.toString() + "\t" + polarity);
		}
		Path outputPath = Paths.get(rootFolder.getAbsolutePath(), "DataFilePolarityMap.txt");
		try {
		    Files.write(outputPath, 
		    		polarityMap,
		            StandardCharsets.UTF_8,
		            StandardOpenOption.CREATE, 
		            StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
		    e.printStackTrace();
		}
	}
	
	private static Worklist extractRunData(File rootFolder) {

		Worklist worklist = new Worklist();
		List<Path> dataFilesList = FIOUtils.findDirectoriesByExtension(rootFolder.toPath(), ".D");
		for (Path dataFilePath : dataFilesList) {
			File df = dataFilePath.toFile();
			String baseName = FilenameUtils.getBaseName(df.getName());
			File sampleInfoFile = Paths.get(df.getAbsolutePath(), "AcqData", "sample_info.xml").toFile();
			File peakBinaryFile = Paths.get(df.getAbsolutePath(), "AcqData", "MSPeak.bin").toFile();
			File acqMethodFile = Paths.get(df.getAbsolutePath(), "AcqData", "AcqMethod.xml").toFile();
			if (!peakBinaryFile.exists()) {
				peakBinaryFile = Paths.get(df.getAbsolutePath(), "AcqData", "MSProfile.bin").toFile();
			}
			if (sampleInfoFile.exists()) {
				WorklistItem runData = extractRunData(sampleInfoFile, peakBinaryFile, baseName, dataFilePath);
				worklist.addItem(runData);
				ChromatographicGradient newGradient = extractChromatographicGradient(runData);
				if (newGradient == null) {
					System.out.println("Failed to extract gradient from " + df.getAbsolutePath());
					continue;
				}
				String runGradientId = addNewUniqueGradient(newGradient);
				gradientMap.put(runData, runGradientId);
			}
		}
		return worklist;
	}
	
	private static void createReport(Worklist worklist, File excelReportFile) throws IOException {
		
		try (Workbook workbook = new XSSFWorkbook()) {
			
			CreationHelper createHelper = workbook.getCreationHelper();
			Font boldFont= workbook.createFont();
			boldFont.setBold(true);
			CellStyle headerStyle = workbook.createCellStyle();
			headerStyle.setFont(boldFont);
			CellStyle timestampStyle = workbook.createCellStyle();
			short dateFormat = createHelper.createDataFormat().getFormat("yyyy-MM-dd HH:mm:ss");
			timestampStyle.setDataFormat(dateFormat);
			
			CellStyle fileSizeStyle = workbook.createCellStyle();
            DataFormat format = workbook.createDataFormat();
            fileSizeStyle.setDataFormat(format.getFormat("#,##0"));
			
			Sheet worklistSheet = workbook.createSheet("Worklist");
			worklistSheet.createFreezePane(1, 1);
			Row row = worklistSheet.createRow(0);	//	Header
			String[]headerColumns = new String[] {
					AgilentSampleInfoFields.DATA_FILE.getName(), 
					AgilentSampleInfoFields.INSTRUMENTNAME.getName(),
					AgilentSampleInfoFields.METHOD.getName(),
					GRADIENT_ID,
					AgilentSampleInfoFields.ACQUISITION_TIME.getName(),
					AgilentSampleInfoFields.PEAK_BINARY_FILE_SIZE.getName(),
					};
			for(int i=0; i<headerColumns.length; i++) {
				Cell cell = row.createCell(i);
				cell.setCellValue(headerColumns[i]);
				cell.setCellStyle(headerStyle);
			}
			int rowCount = 1;
			for(WorklistItem wli : worklist.getWorklistItems()) {
				
				Row itemRow = worklistSheet.createRow(rowCount);
				Cell dfCell = itemRow.createCell(0);
				dfCell.setCellValue(wli.getProperty(headerColumns[0]));
				Cell instCell = itemRow.createCell(1);
				instCell.setCellValue(wli.getProperty(headerColumns[1]));
				Cell methodCell = itemRow.createCell(2);
				methodCell.setCellValue(wli.getProperty(headerColumns[2]));
				Cell gradCell = itemRow.createCell(3);				
				gradCell.setCellValue(gradientMap.get(wli));
				Cell injTimeCell = itemRow.createCell(4);
				injTimeCell.setCellValue(wli.getTimeStamp());
				injTimeCell.setCellStyle(timestampStyle);
				Cell sizeCell = itemRow.createCell(5);
				if(wli.getProperty(headerColumns[5]) != null) {
					sizeCell.setCellValue(Integer.parseInt(wli.getProperty(headerColumns[5])));
					sizeCell.setCellStyle(fileSizeStyle);					
				}
				else
					sizeCell.setCellValue("");
					
				rowCount++;
			}
			for (int i = 0; i < worklistSheet.getRow(0).getLastCellNum(); i++) 
				worklistSheet.autoSizeColumn(i);
			
			// Gradients	
			CellStyle vaStyle = workbook.createCellStyle();
			vaStyle.setVerticalAlignment(VerticalAlignment.TOP);
			CellStyle wrapStyle = workbook.createCellStyle();
	        wrapStyle.setWrapText(true);
	        wrapStyle.setVerticalAlignment(VerticalAlignment.TOP);
	        
			Sheet gradientSheet = workbook.createSheet("Gradients");
			gradientSheet.createFreezePane(0, 1);
			Row gradHeaderRow = gradientSheet.createRow(0);
			String[]gradientHeaderColumns = new String[] {
					"Gradient ID", 
					"Mobile phase A",
					"Mobile phase B",
					"Mobile phase C",
					"Mobile phase D",
					"Timetable",
				};
			for(int i=0; i<gradientHeaderColumns.length; i++) {
				Cell cell = gradHeaderRow.createCell(i);
				cell.setCellValue(gradientHeaderColumns[i]);
				cell.setCellStyle(headerStyle);
			}
			List<ChromatographicGradient> sortedGrads = chromatographicGradientList.stream().
					sorted(new ChromatographicGradientComparator(SortProperty.ID)).
					collect(Collectors.toList());
			
			int gradCounter = 1;
			for(ChromatographicGradient grad : sortedGrads) {
				
				Row gradRow = gradientSheet.createRow(gradCounter);
				Cell idCell = gradRow.createCell(0);
				idCell.setCellValue(grad.getId());
				idCell.setCellStyle(vaStyle);
				
				Cell mpaCell = gradRow.createCell(1);
				MobilePhase mpa = grad.getMobilePhases()[0];
				if(mpa != null)
					mpaCell.setCellValue(mpa.getName());
				else
					mpaCell.setCellValue("");
				
				mpaCell.setCellStyle(vaStyle); 
				
				Cell mpbCell = gradRow.createCell(2);
				MobilePhase mpb = grad.getMobilePhases()[1];
				if(mpb != null)
					mpbCell.setCellValue(mpb.getName());
				else
					mpbCell.setCellValue("");
				
				mpbCell.setCellStyle(vaStyle);
				
				Cell mpcCell = gradRow.createCell(3);
				MobilePhase mpc = grad.getMobilePhases()[2];
				if(mpc != null)
					mpcCell.setCellValue(mpc.getName());
				else
					mpcCell.setCellValue("");
				
				mpcCell.setCellStyle(vaStyle);
				
				Cell mpdCell = gradRow.createCell(4);
				MobilePhase mpd = grad.getMobilePhases()[3];
				if(mpd != null)
					mpdCell.setCellValue(mpd.getName());
				else
					mpdCell.setCellValue("");
				
				mpdCell.setCellStyle(vaStyle);
				
				Cell timeTableCell = gradRow.createCell(5);
				timeTableCell.setCellValue(grad.getGradientSummary().replaceAll(";", "\n"));
				timeTableCell.setCellStyle(wrapStyle);
				
				gradRow.setHeight((short) -1); 
				
				gradCounter++;
			}
			for (int i = 0; i < 5; i++) 
				gradientSheet.autoSizeColumn(i);
			
			gradientSheet.setColumnWidth(5, 60 * 256);
			
            try (FileOutputStream fileOut = new FileOutputStream(excelReportFile)) {
                workbook.write(fileOut);
            }
		}
	}
	
	private static String addNewUniqueGradient(ChromatographicGradient newGradient) {

		for(ChromatographicGradient cg : chromatographicGradientList) {
			
			if(ChromatographicGradientUtils.gradientsEquivalent(newGradient, cg))
				return cg.getId();
		}
		String newId = DataPrefix.CROMATOGRAPHIC_GRADIENT.getName() +
				"_" + String.format("%05d", (chromatographicGradientList.size() + 1));
		newGradient.setId(newId);
		chromatographicGradientList.add(newGradient);
		return newGradient.getId();	
	}
	
	private static ChromatographicGradient extractChromatographicGradient(WorklistItem runData) {
		String methodName =
				Paths.get(runData.getProperty(AgilentSampleInfoFields.METHOD.getName())).getFileName().toString();
		Path methodPath = 
				Paths.get(runData.getProperty(AgilentSampleInfoFields.DATA_FILE.getName()), "AcqData", methodName);
		Path quadPumpParamPath = Paths.get(methodPath.toString(), 
				AgilentAcquisitionMethodParser.QUATERNARY_PUMP_PARAMETERS_FILE);
		if(quadPumpParamPath.toFile().exists()) {
			AgilentAcquisitionMethodParser amp = 
					new AgilentAcquisitionMethodParser(methodPath.toFile());
			amp.parseParameterFiles();
			return amp.extractGradientData();
		}
		else {
			File methodReport =
					Paths.get(runData.getProperty(AgilentSampleInfoFields.DATA_FILE.getName()), "AcqData", "AcqMethod.xml").toFile();
			File rcDevicesXmlOutput = Paths.get(methodReport.getParent(), "RCDevices.xml").toFile();
			AgilentAcquisitionMethodBatchUtils.extractRCDevicesXmlFromAcqMethodReport(methodReport, rcDevicesXmlOutput);
			
			ExtractedAgilentAcquisitionMethodParser emp = new ExtractedAgilentAcquisitionMethodParser(rcDevicesXmlOutput);
			emp.setDoNotMatchMobilePhases(true);
			return emp.extractChromatographicGradientFromFile();
		}
	}
		
	private static WorklistItem extractRunData(
			File sampleInfoFile, 
			File peakBinaryFile, 
			String baseName, 
			Path dataFilePath) {
		
		Document sampleInfo = XmlUtils.readXmlFile(sampleInfoFile);
		if (sampleInfo == null)
			return null;

		String timeString = null;
		Date injectionTime = null;
		DataFile af = new DataFile(baseName);
		List<Element> fieldElements = sampleInfo.getRootElement().getChildren("Field");
		TreeMap<String, String> sampleData = new TreeMap<>();
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
				//	
			}
		}
		WorklistItem newItem = new WorklistItem(af);
		newItem.setTimeStamp(injectionTime);
		af.setInjectionTime(injectionTime);
		for (Entry<String, String> entry : sampleData.entrySet())
			newItem.setProperty(entry.getKey(), entry.getValue());

		if(peakBinaryFile.exists()) {
			long fileSize = 0L;		
			try {
				fileSize = Files.size(peakBinaryFile.toPath());
			} catch (IOException e) {
				e.printStackTrace();
			}
			newItem.setProperty(
					AgilentSampleInfoFields.PEAK_BINARY_FILE_SIZE.getName(), Long.toString(fileSize));
		}
		newItem.setProperty(
				AgilentSampleInfoFields.DATA_FILE.getName(), dataFilePath.toString());
		return newItem;
	}
}

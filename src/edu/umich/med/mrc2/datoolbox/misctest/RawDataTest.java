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

package edu.umich.med.mrc2.datoolbox.misctest;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import edu.umich.med.mrc2.datoolbox.data.enums.SupportedRawDataTypes;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.FilePreferencesFactory;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.utils.FIOUtils;
import umich.ms.datatypes.LCMSData;
import umich.ms.datatypes.LCMSDataSubset;
import umich.ms.datatypes.lcmsrun.LCMSRunInfo;
import umich.ms.datatypes.scan.StorageStrategy;
import umich.ms.fileio.exceptions.FileParsingException;
import umich.ms.fileio.filetypes.LCMSDataSource;
import umich.ms.fileio.filetypes.mzml.MZMLFile;
import umich.ms.fileio.filetypes.mzxml.MZXMLFile;
import umich.ms.fileio.filetypes.xmlbased.AbstractXMLBasedDataSource;

public class RawDataTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.setProperty("java.util.prefs.PreferencesFactory", 
				FilePreferencesFactory.class.getName());
		System.setProperty(FilePreferencesFactory.SYSTEM_PROPERTY_FILE, 
				MRC2ToolBoxCore.configDir + "MRC2ToolBoxPrefs.txt");
		MRC2ToolBoxConfiguration.initConfiguration();
		try {
			examineRawData();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void examineRawData() {

//		File dataFile = new File("Z:\\Personal Directories- For CORE only\\Sasha\\2022_0117_BEH_Amide_Rat_FM\\"
//				+ "ThermoRAW\\2022_0117_1P_Rat_FM_05x_5uL_60min_ID01.raw");
//		int length  = 1024*1024;
//		readTheroRawHeader(dataFile, length);

		File dataFile = new File("Y:\\DataAnalysis\\IDTRACKER_RAW\\EX00979\\Liver\\RP"
				+ "\\iDDA\\NEG\\20191021-EX00979-A003-IN0028-CS00000MP-7-IDDA_ce10_1-N.mzML");
		LCMSData rawData = null;
		try {
			rawData = createDataSource(dataFile);
		} catch (FileParsingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(rawData != null) {
			
			LCMSDataSource<?> source = rawData.getSource();
			LCMSRunInfo info = source.getRunInfo();
			System.err.println("***");
		}
		if(rawData != null)
			rawData.releaseMemory();
	}
	
	public static LCMSData createDataSource(File sourceRawFile) throws FileParsingException {

		Path path = Paths.get(sourceRawFile.getAbsolutePath());
		AbstractXMLBasedDataSource source = null;
		if (FilenameUtils.getExtension(path.toString()).
				equalsIgnoreCase(SupportedRawDataTypes.MZML.name()))
			source = new MZMLFile(path.toString());

		if (FilenameUtils.getExtension(path.toString()).
				equalsIgnoreCase(SupportedRawDataTypes.MZXML.name()))
			source = new MZXMLFile(path.toString());

		LCMSData data = new LCMSData(source);
		data.load(LCMSDataSubset.STRUCTURE_ONLY);		
		data.getScans().isAutoloadSpectra(true);
		data.getScans().setDefaultStorageStrategy(StorageStrategy.SOFT);
		return data;
	}
	
	private static void readTheroRawHeader(File rawFile, int length) {

		RandomAccessFile raf = null;
		byte[] bytes = new byte[length];
		try {
			raf = new RandomAccessFile(rawFile.getAbsolutePath(), "r");			
			raf.readFully(bytes);

		} catch (IOException e) {
			e.printStackTrace();
		}		
		String parsed = new String(bytes, StandardCharsets.UTF_8);
		String result = parsed.chars()
			    .filter(c -> isAsciiPrintable((char) c))
			    .mapToObj(c -> String.valueOf((char) c))
			    .collect(Collectors.joining());
		
//		String result = parsed.replaceAll("[\\p{Cc}\\p{Cf}\\p{Co}\\p{Cn}]", "");
		System.err.println("***");
		
//		String result = Arrays.stream(hexString.split(" "))
//                .map(binary -> Integer.parseInt(binary, 2))
//                .map(Character::toString)
//                .collect(Collectors.joining());
		
//		byte[] decodedBytes = null;
//		try {
//			decodedBytes = Hex.decodeHex(hexString.toCharArray());
//		} catch (DecoderException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
//		String result = "";
//		if(decodedBytes != null) {
//			try {
//				result = new String(bytes, "UTF-8");
//			} catch (UnsupportedEncodingException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}	
//		}
		if(result != null) {
			File outputFile = FIOUtils.changeExtension(rawFile, "txt") ;
			try {
				//	TODO create manifest
				FileUtils.writeStringToFile(outputFile, result, StandardCharsets.UTF_8, false);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(raf != null) {
			try {
				raf.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private static boolean isAsciiPrintable(char ch) {
		return ch >= 32 && ch < 127;
	}
}












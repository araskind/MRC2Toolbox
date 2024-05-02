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

package edu.umich.med.mrc2.datoolbox.utils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.compress.utils.FileNameUtils;
import org.apache.commons.lang.StringUtils;

import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;

public class WorklistPlateRemapper {
	
	private static final String[]rowLabels = new String[] {"A","B","C","D","E","F","G","H"};
	private static Map<String,Integer>rowMap;

	public static void main(String[] args) {

		rowMap = new TreeMap<String,Integer>();
		for(int row=0; row<8; row++)
			rowMap.put(rowLabels[row], row);
		
		File inputWorklistFile = 
				new File("E:\\DataAnalysis\\PlateLayout\\EX01426 - MoTrPAC plasma batch 1 54 well worklist.txt");
		remapWorklistFrom54VialTo96WellPlate(inputWorklistFile);
	}
		
	private static void remapWorklistFrom54VialTo96WellPlate(File inputWorklistFile) {

		String[][] inputData = null;
		try {
			inputData = DelimitedTextParser.parseTextFileWithEncoding(
					inputWorklistFile, MRC2ToolBoxConfiguration.getTabDelimiter());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int positionColumn = 0;
		int sampleNameColumn = 0;
		
		for(int i=0; i<inputData[0].length; i++) {
			
			if(inputData[0][i].equals("Sample Position"))
				positionColumn = i;
			
			if(inputData[0][i].equals("Sample Name"))
				sampleNameColumn = i;
		}
		List<String>newWorkListData = new ArrayList<String>();
		List<String>newLine = new ArrayList<String>();
		newLine.addAll(Arrays.asList(inputData[0]));
		newWorkListData.add(StringUtils.join(newLine, "\t"));
		
		for(int i=1; i<inputData.length; i++) {
			
			newLine.clear();
			if(inputData[i][sampleNameColumn].startsWith("S00")) {
				
				String vialPosition = inputData[i][positionColumn];
				String wellPosition = get96WellPositionFor54VialPosition(vialPosition);
				inputData[i][positionColumn] = wellPosition;
			}
			newLine.addAll(Arrays.asList(inputData[i]));
			newWorkListData.add(StringUtils.join(newLine, "\t"));
		}
		Path outputPath = Paths.get(
				inputWorklistFile.getParentFile().getAbsolutePath(), 
				FileNameUtils.getBaseName(inputWorklistFile.getName()) + "-Remapped.txt");
		
		  try {
			Files.write(outputPath, 
					newWorkListData,
					StandardCharsets.UTF_8,
					StandardOpenOption.CREATE, 
					StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static String get96WellPositionFor54VialPosition(String positionOn54VialPlate) {
					
		String[]parts = positionOn54VialPlate.split("-");
		int vialPlateNumber = Integer.parseInt(parts[0].replace("P", ""));
		int row = rowMap.get(parts[1].substring(0, 1));
		int col = Integer.parseInt(parts[1].substring(1));
		int sampleNumber = (vialPlateNumber - 1) * 54 + row * 9 + col; 

		int plateNumber = 1;
		double value = (double)sampleNumber / 96;		
		double fractionalPart = value % 1;
		double integralPart = value - fractionalPart;
		
		if(value % 1 == 0.0d)
			plateNumber = (int)integralPart;
		else
			plateNumber = (int)integralPart + 1;
				
		return get96WellPlatePositionForSampleNumber(sampleNumber, plateNumber);
	}
	
	private static String get96WellPlatePositionForSampleNumber(
			int sampleNumber, int plateNumber) {
				
		sampleNumber = sampleNumber - 96 * (plateNumber - 1);
		int wellCount = 0;
		for(int col = 1; col<=12; col++) {
			
			for(int row=0; row<8; row++) {

				wellCount++;
				if(sampleNumber == wellCount)
					return "P" + Integer.toString(plateNumber) + "-" + rowLabels[row] + Integer.toString(col);
			}
		}		
		return null;
	}
	
	private static void testMapping() {
		
		for(int p=1; p<=3; p++) {
			
			for(int row=0; row<6; row++) {
				
				for(int col=1; col <=9; col++) {
					
					String vialPlateLocation = "P" + Integer.toString(p) + "-" + rowLabels[row] + Integer.toString(col);
					String wellPlateLocation = get96WellPositionFor54VialPosition(vialPlateLocation);
					System.out.println(vialPlateLocation + "\t" + wellPlateLocation);
				}
			}
		}
	}
}

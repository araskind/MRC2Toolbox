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

package edu.umich.med.mrc2.datoolbox.dmutils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.thermo.raw.ThermoRawMetadata;
import edu.umich.med.mrc2.datoolbox.data.thermo.raw.ThermoRawMetadataComparator;
import edu.umich.med.mrc2.datoolbox.data.thermo.raw.ThermoUtils;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.utils.JSONUtils;

public class ThermoMetadataExtractor {

	public static void main(String[] args) {
		
		File jsonFolder = new File("E:\\DataAnalysis\\R03\\RawData\\HILIC\\NEG\\JSON");
		File ouputFile = new File("E:\\DataAnalysis\\R03\\RawData\\HILIC\\NEG\\Contrepois_HILIC_NEG_worklist.txt");
		
		try {
			readThermoWorklistFromJson(jsonFolder, ouputFile);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void readThermoWorklistFromJson(File jsonFolder, File ouputFile) {
		
		File[] jsonFileList = JSONUtils.getJsonFileList(jsonFolder);
		Collection<ThermoRawMetadata>metadataList = 
				new ArrayList<ThermoRawMetadata>();

		for(File jsonFile : jsonFileList) {
			
			JSONObject jso = JSONUtils.readJsonFromFile(jsonFile);	
			
			ThermoRawMetadata md = 
					ThermoUtils.parseMetadataObjectFromJson(
							FilenameUtils.getBaseName(jsonFile.getName().replace("-metadata", "")), jso);
			if(md != null)
				metadataList.add(md);
		}
		metadataList = metadataList.stream().
				sorted(new ThermoRawMetadataComparator(SortProperty.injectionTime)).
				collect(Collectors.toList());		
		
		//	Create output 
		Collection<String>dataToExport = new ArrayList<String>();
		String[] header = new String[] {
			"MRC2 sample ID",	
			"sample_id",	
			"raw_file",	
			"Injection time",	
			"Sample Position",	
			"Sample Name",	
			"sample_type",	
			"sample_order",	
			"batch_override",	
		};
		dataToExport.add(StringUtils.join(header, "\t"));
		Collection<String>line = new ArrayList<String>();
		int counter = 1;
		for(ThermoRawMetadata md : metadataList) {
			
			line.clear();
			line.add(""); //"MRC2 sample ID",	
			line.add(md.getSampleName()); //"sample_id",	
			line.add(md.getFileName()); //"raw_file",	
			line.add(MRC2ToolBoxConfiguration.defaultTimeStampFormat.format(md.getInjectionTime())); //"Injection time",	
			line.add(md.getSamplePosition()); //"Sample Position",	
			line.add(md.getSampleName()); //"Sample Name",	
			line.add(""); //"sample_type",	
			line.add(Integer.toString(counter)); //"sample_order",	
			line.add(""); //"batch_override",
			dataToExport.add(StringUtils.join(line, "\t"));
			counter++;
		}	
		try {
			Files.write(ouputFile.toPath(), 
					dataToExport, 
					StandardCharsets.UTF_8,
					StandardOpenOption.CREATE, 
					StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}

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

package edu.umich.med.mrc2.datoolbox.utils.acqmethod;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

public class AgilentAcquisitionMethodReportParser {

	
	public static Map<String,String>parseAgilentAcquisitionMethodReportFile(File acquisitionMethodReportFile){
		
		Map<String,String>methodParameters = new TreeMap<String,String>();		
//		InputStream is = null;
//		try {
//			is = new FileInputStream(acquisitionMethodReportFile);
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		if(is == null)
//			return null;
//		
//		Workbook workbook = StreamingReader.builder()
//		        .rowCacheSize(100)    // number of rows to keep in memory (defaults to 10)
//		        .bufferSize(4096)     // buffer size to use when reading InputStream to file (defaults to 1024)
//		        .open(is);    
		
		Workbook workbook = null;
		try {
			workbook = WorkbookFactory.create(acquisitionMethodReportFile);
		} catch (EncryptedDocumentException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(workbook == null)
			return null;

		Sheet methodSheet = workbook.getSheetAt(0);
		for (Row r : methodSheet) {

			int last = r.getLastCellNum();
	        for (int j = r.getFirstCellNum(); j < last; j++) {
	
	        	if(r.getCell(j) == null || r.getCell(j).toString() == null)
	        		continue;
	        	
	            if(r.getCell(j).toString().equals("Method Name")) {
	
	            	for(int k = j+1; j<last; k++) {
	            		
	            		if(!r.getCell(k).toString().trim().isEmpty()) {
	            			methodParameters.put("Method Name", r.getCell(k).toString());
	            			break;
	            		}
	            	}
	            }
	            if(r.getCell(j).toString().equals("Isolation Width MS/MS")) {
	            	
	            	for(int k = j+1; k<last; k++) {
	            		if(!r.getCell(k).toString().trim().isEmpty()) {
	            			methodParameters.put("Isolation Width MS/MS", r.getCell(k).toString());
	            			break;
	            		}
	            	}
	            	break;
	            }
	        }	        
		}
//		if(is != null)
//			try {
//				is.close();
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}	
		return methodParameters;
	}
}



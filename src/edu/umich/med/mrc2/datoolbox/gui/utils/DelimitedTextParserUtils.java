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

package edu.umich.med.mrc2.datoolbox.gui.utils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

public class DelimitedTextParserUtils {
	
	private DelimitedTextParserUtils() {
		/* This utility class should not be instantiated */
	}

	public static CSVParser parseTabDelimitedFile(File file) {
		
		CSVParser csvParser = null;
		CSVFormat tsvFormat = CSVFormat.TDF.builder()
                .setHeader() // Empty method infers header from first row
                .setSkipHeaderRecord(true)
                .setIgnoreEmptyLines(true)
                .get();
		try (Reader reader = new FileReader(file)) {
			csvParser = tsvFormat.parse(reader);
		} catch (IOException e) {
			e.printStackTrace();
		}
        return csvParser;
	}
	
	public static List<CSVRecord> getRecordsFromTabDelimitedFile(File file) {

		List<CSVRecord> records = new ArrayList<>();
		CSVParser csvParser = null;
		CSVFormat tsvFormat = CSVFormat.TDF.builder()
                .setHeader() // Empty method infers header from first row
                .setSkipHeaderRecord(true)
                .setIgnoreEmptyLines(true)
                .get();
		try (Reader reader = new FileReader(file)) {
			csvParser = tsvFormat.parse(reader);
			if (csvParser != null) {
				try {
					csvParser.getRecords().forEach(records::add);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return records;
	}
}

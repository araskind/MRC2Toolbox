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

package edu.umich.med.mrc2.datoolbox.cpdmatch;

import java.util.Map;
import java.util.TreeMap;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

public class CompoundMatcherUtils {
	
	private CompoundMatcherUtils() {
		/* This utility class should not be instantiated */
	}

	public static Map<CompoundMatcherField,Integer>getColumnMap4MatchGroupsSheet(Row header){

		Map<CompoundMatcherField,Integer>columnMap = new TreeMap<CompoundMatcherField,Integer>();
		int headerLength = header.getPhysicalNumberOfCells();
		for (int i=0; i<headerLength; i++) {

			Cell c = header.getCell(i);
			for(CompoundMatcherField field : CompoundMatcherField.values()) {

				if(c != null && c.getStringCellValue().equals(field.getName()))
					columnMap.put(field, i);
			}
		}
		return columnMap;
	}
	
	public static Map<CompoundMatchFullPicksFields,Integer>getColumnMap4NamedMatchGroupsSheet(Row header){

		Map<CompoundMatchFullPicksFields,Integer>columnMap = new TreeMap<CompoundMatchFullPicksFields,Integer>();
		int headerLength = header.getPhysicalNumberOfCells();
		for (int i=0; i<headerLength; i++) {

			Cell c = header.getCell(i);
			for(CompoundMatchFullPicksFields field : CompoundMatchFullPicksFields.values()) {

				if(c != null && c.getStringCellValue().equals(field.getName()))
					columnMap.put(field, i);
			}
		}
		return columnMap;
	}
}

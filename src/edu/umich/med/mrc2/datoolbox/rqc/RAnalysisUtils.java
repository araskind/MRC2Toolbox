/*******************************************************************************
 *
 * (C) Copyright 2018-2026 MRC2 (http://mrc2.umich.edu).
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

package edu.umich.med.mrc2.datoolbox.rqc;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import edu.umich.med.mrc2.datoolbox.data.compare.RMultibatchAnalysisInputObjectComparator;
import edu.umich.med.mrc2.datoolbox.gui.rgen.mcr.RMultibatchAnalysisInputObject;

public class RAnalysisUtils {
	
	private RAnalysisUtils() {
        // TODO Auto-generated constructor stub
	}

	public static Set<RMultibatchAnalysisInputObject> createRmultibatchInputSet(
			String[][] mcInputData, 
			List<SummaryInputColumns>obligatoryColumns,
			List<SummaryInputColumns>selectableColumns,
			File workDirectory,
			List<String>errorList){
		
		Set<RMultibatchAnalysisInputObject>inputSet = 
				new TreeSet<>(new RMultibatchAnalysisInputObjectComparator());
		Map<SummaryInputColumns,Integer>inputColumnMap = new TreeMap<>();
		for(int i=0; i<mcInputData[0].length; i++) {
			
			SummaryInputColumns column = SummaryInputColumns.getOptionByName(mcInputData[0][i]);
			if(column != null)
				inputColumnMap.put(column, i);
		}
		for(SummaryInputColumns oc : obligatoryColumns) {
            
            if(!inputColumnMap.containsKey(oc)) {
                errorList.add("Input list file doesn't include \"" + oc.name() + "\" column");
                return new TreeSet<>();
            }
        }
		if(!selectableColumns.isEmpty()) {
			int countSelectable = 0;
			for(SummaryInputColumns sc : selectableColumns) {
	            
	            if(inputColumnMap.containsKey(sc))
	                countSelectable++;
			}
			if (countSelectable == 0) {

				errorList.add("None of the selectable columns are present in data input map file!");
				return new TreeSet<>();
			}
		}
		List<SummaryInputColumns>allColumns = new ArrayList<>();
		allColumns.addAll(obligatoryColumns);
		allColumns.addAll(selectableColumns);
		for(int i=1; i<mcInputData.length; i++) {
			
			RMultibatchAnalysisInputObject mcio = new RMultibatchAnalysisInputObject();
			
			for(SummaryInputColumns oc : allColumns) {
				
				String value = mcInputData[i][inputColumnMap.get(oc)];
				if (value == null || value.isBlank()) {

					errorList.add("Missing data in the input file for "
									+ oc.name() + " on line " + Integer.toString(i + 1) + "!");
					return new TreeSet<>();
				}
				if (oc.isFactor()) 
					mcio.setProperty(oc, value);
				else {
					File inputFile = Paths.get(workDirectory.getAbsolutePath(), value).toFile();
					if(!inputFile.exists()) {
						errorList.add("File " + inputFile.getAbsolutePath() + " not found");
						continue;
					}
					else {
						mcio.setDataFile(oc, inputFile);
					}
				}
			}
			inputSet.add(mcio);
		}
		return inputSet;
	}
}

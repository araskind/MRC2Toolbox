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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import edu.umich.med.mrc2.datoolbox.data.Adduct;
import edu.umich.med.mrc2.datoolbox.data.enums.AgilentProFinderSimpleCSVexportColumns;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskEvent;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskListener;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.utils.DelimitedTextParser;

public class ProFinderArchivePreprocessingTask extends AbstractTask implements TaskListener {
	
	private File proFinderArchiveFile;
	private File proFinderSimpleCsvExportFile;
	private Collection<Adduct>selectedAdducts;
	private boolean isLibraryParsed;
	private Map<AgilentProFinderSimpleCSVexportColumns, Integer>dataFieldMap;

	public ProFinderArchivePreprocessingTask(
			File proFinderArchiveFile, 
			File proFinderSimpleCsvExportFile,
			Collection<Adduct>selectedAdducts) {
		super();
		this.proFinderArchiveFile = proFinderArchiveFile;
		this.proFinderSimpleCsvExportFile = proFinderSimpleCsvExportFile;
		this.selectedAdducts = selectedAdducts;
		isLibraryParsed = false;
	}

	@Override
	public void run() {

		setStatus(TaskStatus.PROCESSING);
		taskDescription = "Preparing ProFinder data set for import ...";
		total = 100;
		processed = 2;
		try {
			parseAndMatchLibraryDataFromSimpleCsvExportFile();
		} catch (Exception e) {
			errorMessage = "";
			e.printStackTrace();
			setStatus(TaskStatus.ERROR);
			return;
		}
		setStatus(TaskStatus.FINISHED);
	}

	private boolean parseAndMatchLibraryDataFromSimpleCsvExportFile() {
		
		taskDescription = "Parsing library data ...";
		total = 100;
		processed = 2;
		
		Map<String,Double>nameRetentionMap = new HashMap<String,Double>();
		String[][] compoundDataArray = 
				DelimitedTextParser.parseTextFile(proFinderSimpleCsvExportFile, ',');
		
		boolean dataValid = createAndValidateFieldMap(compoundDataArray[0]);
		if(!dataValid)
			return false;
		
		int nameColumnIndex = dataFieldMap.get(AgilentProFinderSimpleCSVexportColumns.COMPOUND_NAME);
		int rtColumnIndex = dataFieldMap.get(AgilentProFinderSimpleCSVexportColumns.RT);
		for(int i=1; i<compoundDataArray.length; i++) {
			
			String rtString = compoundDataArray[i][rtColumnIndex];
			if(rtString.isEmpty() || !NumberUtils.isParsable(rtString)) {
				errorMessage = "Some values in the " 
						+ AgilentProFinderSimpleCSVexportColumns.RT.getName()
						+ " column are not valid numbers";
				return false;
			}
			double rt = NumberUtils.createDouble(rtString);
			nameRetentionMap.put(compoundDataArray[i][nameColumnIndex], rt);
		}
		return true;
	}

	private boolean createAndValidateFieldMap(String[]header) {
		
		dataFieldMap = new TreeMap<AgilentProFinderSimpleCSVexportColumns, Integer>();
		for(int i=0; i<header.length; i++) {
			
			AgilentProFinderSimpleCSVexportColumns f = 
					AgilentProFinderSimpleCSVexportColumns.getOptionByUIName(header[i]);
			if(f != null)
				dataFieldMap.put(f, i);
		}
		ArrayList<String>missingFields = new ArrayList<String>();
		if(!dataFieldMap.containsKey(AgilentProFinderSimpleCSVexportColumns.COMPOUND_NAME))
			missingFields.add(AgilentProFinderSimpleCSVexportColumns.COMPOUND_NAME.getName());

		if(!dataFieldMap.containsKey(AgilentProFinderSimpleCSVexportColumns.RT))
			missingFields.add(AgilentProFinderSimpleCSVexportColumns.RT.getName());
		
		if(missingFields.isEmpty())
			return true;
		else {
			errorMessage = 
					"The following obligatory fields are missing form the input data:\n"
					+ StringUtils.join(missingFields, ", ");
			return false;
		}		
	}	
	
	@Override
	public void statusChanged(TaskEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public Task cloneTask() {

		return new ProFinderArchivePreprocessingTask(
				proFinderArchiveFile, 
				proFinderSimpleCsvExportFile,
				selectedAdducts);
	}
	
}

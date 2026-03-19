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

package edu.umich.med.mrc2.datoolbox.gui.rgen;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;

import edu.umich.med.mrc2.datoolbox.data.compare.RMultibatchAnalysisInputObjectComparator;
import edu.umich.med.mrc2.datoolbox.gui.rgen.mcr.RMultibatchAnalysisInputObject;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTableModel;
import edu.umich.med.mrc2.datoolbox.gui.tables.ColumnContext;
import edu.umich.med.mrc2.datoolbox.rqc.SummaryInputColumns;

public class RMultibatchScriptInputTableModel extends BasicTableModel {

	private static final long serialVersionUID = 1L;
	
	private List<SummaryInputColumns> propCols;
	private List<SummaryInputColumns> fileCols;

	public RMultibatchScriptInputTableModel(
			List<SummaryInputColumns> propCols,
			List<SummaryInputColumns> fileCols) {
		super();
		this.propCols = propCols;
		this.fileCols = fileCols;
		columnArray = new ColumnContext[propCols.size() + fileCols.size()];
		int offset = propCols.size();
		for(int i=0; i<columnArray.length; i++) {
			
			if(i < offset) {
				
				columnArray[i] = new ColumnContext(
						propCols.get(i).getName(), 
						propCols.get(i).getName(), 
						String.class, true);
			}
			else {
				columnArray[i] = new ColumnContext(
						fileCols.get(i-offset).getName(), 
						fileCols.get(i-offset).getName(), 
						File.class, true);
			}
		}
	}

	public void setModelFromDataFiles(
			SummaryInputColumns column,
			Collection<File> files, 
			boolean replace) {
	
		if(files == null || files.isEmpty())
			return;
		
		File[] sortedFiles = files.stream().sorted().toArray(size -> new File[size]);
		int colIndex = getColumnIndex(column.getName());
		int rowIndex = getRowCount();
		if(replace)
			rowIndex = 0;
		
		for (int i = 0; i < sortedFiles.length; i++) {
			
			if (rowIndex + i >= getRowCount())
				addRow(new Object[columnArray.length]);

			setValueAt(sortedFiles[i], rowIndex + i, colIndex);
		}
	}

	public void setModelFromInputObjects(
			Collection<RMultibatchAnalysisInputObject> mcioCollection, boolean replace) {
		
		if(replace)
			setRowCount(0);
		
		if(mcioCollection == null || mcioCollection.isEmpty())
			return;
		
		TreeSet<RMultibatchAnalysisInputObject>sortedInputObjects = 
				new TreeSet<>(new RMultibatchAnalysisInputObjectComparator());
		sortedInputObjects.addAll(mcioCollection);
		List<Object[]>rowData = new ArrayList<>();
		for (RMultibatchAnalysisInputObject mcio : sortedInputObjects)
			rowData.add(createRow(mcio));
		
		if(!rowData.isEmpty())
			addRows(rowData);		
	}
	
	private Object[] createRow(RMultibatchAnalysisInputObject mcio) {

		Object[] obj = new Object[columnArray.length];
		int colIndex = 0;
		for (int i = 0; i < propCols.size(); i++) {
			obj[i] = mcio.getProperty(propCols.get(i));
			colIndex++;
		}
		for (int i = 0; i < fileCols.size(); i++) {
			obj[colIndex + i] = mcio.getFile(fileCols.get(i));
		}		
		return obj;
	}
}













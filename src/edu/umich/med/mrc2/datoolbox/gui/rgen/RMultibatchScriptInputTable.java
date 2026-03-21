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
import java.util.Set;
import java.util.TreeSet;

import javax.swing.table.TableRowSorter;

import edu.umich.med.mrc2.datoolbox.data.compare.FileBaseNameComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.RMultibatchAnalysisInputObjectComparator;
import edu.umich.med.mrc2.datoolbox.data.format.FileBaseNameFormat;
import edu.umich.med.mrc2.datoolbox.gui.rgen.mcr.RMultibatchAnalysisInputObject;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.AutoChoices;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.TableFilterHeader;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.FileRenderer;
import edu.umich.med.mrc2.datoolbox.rqc.SummaryInputColumns;

public class RMultibatchScriptInputTable extends BasicTable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private List<SummaryInputColumns> propCols;
	private List<SummaryInputColumns> fileCols;
	
	public RMultibatchScriptInputTable(List<SummaryInputColumns> allCols) {
		
		super();
		propCols = new ArrayList<>();
		fileCols = new ArrayList<>();
		for (SummaryInputColumns c : allCols) {

			if (RMultibatchAnalysisInputObject.propertyColumns.contains(c))
				propCols.add(c);

			if (RMultibatchAnalysisInputObject.dataFileColumns.contains(c))
				fileCols.add(c);
		}
		model = new RMultibatchScriptInputTableModel(propCols, fileCols);
		setModel(model);
		rowSorter = new TableRowSorter<RMultibatchScriptInputTableModel>(
				(RMultibatchScriptInputTableModel)model);
		setRowSorter(rowSorter);
		for(SummaryInputColumns c : fileCols) {			
			rowSorter.setComparator(
					model.getColumnIndex(c.getName()), new FileBaseNameComparator());
			columnModel.getColumnById(c.getName()).setCellRenderer(new FileRenderer());		
		}
		thf = new TableFilterHeader(this, AutoChoices.ENABLED);
		thf.getParserModel().setComparator(File.class, new FileBaseNameComparator());
		thf.getParserModel().setFormat(File.class, new FileBaseNameFormat());
		finalizeLayout();		
	}
	
	public void setModelFromDataFiles(Collection<File> files, SummaryInputColumns fileColumn) {

		thf.setTable(null);
		((RMultibatchScriptInputTableModel)model).setModelFromDataFiles(fileColumn, files, true);
		thf.setTable(this);
		adjustColumns();
	}
	
	public void addDataFiles(Collection<File> files, SummaryInputColumns fileColumn) {

		thf.setTable(null);
		((RMultibatchScriptInputTableModel)model).setModelFromDataFiles(fileColumn, files, false);
		thf.setTable(this);
		adjustColumns();
	}
	
	public void setModelFromInputObjects(Collection<RMultibatchAnalysisInputObject> mcioCollection) {

		thf.setTable(null);
		((RMultibatchScriptInputTableModel)model).setModelFromInputObjects(mcioCollection, true);
		thf.setTable(this);
		adjustColumns();
	}
	
	public void addInputObjects(Collection<RMultibatchAnalysisInputObject> mcioCollection) {

		thf.setTable(null);
		((RMultibatchScriptInputTableModel)model).setModelFromInputObjects(mcioCollection, false);
		thf.setTable(this);
		adjustColumns();
	}
	
	public Collection<File>getSelectedFiles(SummaryInputColumns fileColumn){
		
		Collection<File>selected = new ArrayList<>();
		if(getSelectedRowCount() == 0 || 
				!RMultibatchAnalysisInputObject.dataFileColumns.contains(fileColumn))
			return selected;
		
		int fileColumnIndex = model.getColumnIndex(fileColumn.getName());
		for(int i : getSelectedRows())
			selected.add((File)model.getValueAt(convertRowIndexToModel(i),fileColumnIndex));
				
		return selected;
	}
	
	public Collection<File>getAllFiles(SummaryInputColumns fileColumn){
		
		Collection<File>files = new ArrayList<>();
		int fileColumnIndex = model.getColumnIndex(fileColumn.getName());
		for(int i=0; i<model.getRowCount(); i++)
			files.add((File)model.getValueAt(i,fileColumnIndex));
				
		return files;
	}
	
	public Set<RMultibatchAnalysisInputObject>getMetabCombinerFileInputObjects(){
		
		Set<RMultibatchAnalysisInputObject>mcfSet = 
				new TreeSet<>(new RMultibatchAnalysisInputObjectComparator());
		for(int i=0; i<model.getRowCount(); i++) {
			
			RMultibatchAnalysisInputObject mco = new RMultibatchAnalysisInputObject();
			for (SummaryInputColumns c : propCols) {
				int colIndex = model.getColumnIndex(c.getName());
				String property = (String)model.getValueAt(i, colIndex);
				if(property != null && !property.isBlank())
					mco.setProperty(c, property);
			}
			for (SummaryInputColumns c : fileCols) {
				int colIndex = model.getColumnIndex(c.getName());
				File dataFile = (File)model.getValueAt(i, colIndex);
				if(dataFile != null)
					mco.setDataFile(c, dataFile);
			}
			mcfSet.add(mco);
		}
		return mcfSet;
	}
}











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

package edu.umich.med.mrc2.datoolbox.gui.binner.control;

import java.util.Collection;
import java.util.TreeSet;

import javax.swing.ListSelectionModel;
import javax.swing.table.TableRowSorter;

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.ExperimentalSample;
import edu.umich.med.mrc2.datoolbox.data.compare.ExperimentalSampleComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.format.ExperimentalSampleFormatExtended;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.AutoChoices;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.TableFilterHeader;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.DataFileCellRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.ExperimentalSampleRendererExtended;

public class DataFileTable extends BasicTable {

	/**
	 *
	 */
	private static final long serialVersionUID = -5022129546919771647L;

	public DataFileTable() {
		super();
		model = new DataFileTableModel();
		setModel(model);
		rowSorter = new TableRowSorter<DataFileTableModel>((DataFileTableModel)model);
		setRowSorter(rowSorter);
		setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		setDefaultRenderer(DataFile.class, new DataFileCellRenderer());
		setDefaultRenderer(ExperimentalSample.class, 
				new ExperimentalSampleRendererExtended());
		getColumnModel().getColumn(0).setPreferredWidth(80);
		getColumnModel().getColumn(0).setMaxWidth(80);
		fixedWidthColumns.add(0);
		thf = new TableFilterHeader(this, AutoChoices.ENABLED);
		thf.getParserModel().setFormat(ExperimentalSample.class,
				new ExperimentalSampleFormatExtended());
		thf.getParserModel().setComparator(ExperimentalSample.class,
				new ExperimentalSampleComparator(SortProperty.ID));
		
		finalizeLayout();
		//	TableLayoutManager.setTableLayout(this);
	}

	public void setTableModelFromFileCollection(Collection<DataFile> files) {
		thf.setTable(null);
		((DataFileTableModel)model).setTableModelFromFileCollection(files);
		thf.setTable(this);
		adjustColumns();
	}
	
	public Collection<DataFile> getEnabledFiles() {
		
		Collection<DataFile>selectedFiles = new TreeSet<DataFile>();
		int selectedCol = model.getColumnIndex(DataFileTableModel.SELECTED_COLUMN);
		int dfCol = model.getColumnIndex(DataFileTableModel.DATA_FILE_COLUMN);
		for(int i=0; i<model.getRowCount(); i++) {
			
			if((boolean)model.getValueAt(i, selectedCol))				
				selectedFiles.add((DataFile)model.getValueAt(i, dfCol));			
		}
		return selectedFiles;
	}
	
	public Collection<DataFile> getSelectedFiles() {
		
		Collection<DataFile>files = new TreeSet<DataFile>();
		int[] selected = getSelectedRows();
		if(selected.length == 0)
			return files;

		int fileCol = model.getColumnIndex(DataFileTableModel.DATA_FILE_COLUMN);
		for(int i : selected)
			files.add((DataFile)model.getValueAt(convertRowIndexToModel(i), fileCol));
			
		return files;		
	}
}

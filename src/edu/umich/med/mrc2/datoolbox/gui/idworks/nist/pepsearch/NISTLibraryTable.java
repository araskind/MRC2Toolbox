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

package edu.umich.med.mrc2.datoolbox.gui.idworks.nist.pepsearch;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.ListSelectionModel;
import javax.swing.table.TableRowSorter;

import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;

public class NISTLibraryTable extends BasicTable {

	/**
	 *
	 */
	private static final long serialVersionUID = 3916578878036401634L;

	private NISTLibraryTableModel model;

	public NISTLibraryTable() {
		super();
		model = new NISTLibraryTableModel();
		setModel(model);
		rowSorter = new TableRowSorter<NISTLibraryTableModel>(model);
		setRowSorter(rowSorter);
		getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		getColumn(NISTLibraryTableModel.ACTIVE_COLUMN).setMaxWidth(80);
	}

	public void addLibraryFile(File libFile) {
		model.addLibraryFile(libFile);
	}

	public void removeLibraryFiles(Collection<File> libFiles) {
		model.removeLibraryFiles(libFiles);
	}

	public Collection<File>getLibraryFiles(){

		Collection<File>libFiles = new ArrayList<File>();
		int colIdx = model.getColumnIndex(NISTLibraryTableModel.LIBRARY_FILE_COLUMN);
		for(int i=0; i<model.getRowCount(); i++)
			libFiles.add((File)model.getValueAt(i, colIdx));

		return libFiles;
	}
	
	public Map<File,Boolean>getLibraryFilesMap(){

		Map<File,Boolean>libFilesMap = new TreeMap<File,Boolean>();
		int colIdx = model.getColumnIndex(NISTLibraryTableModel.LIBRARY_FILE_COLUMN);
		int colActiveIdx = model.getColumnIndex(NISTLibraryTableModel.ACTIVE_COLUMN);
		for(int i=0; i<model.getRowCount(); i++)
			libFilesMap.put((File)model.getValueAt(i, colIdx), (Boolean)model.getValueAt(i, colActiveIdx));

		return libFilesMap;
	}

	public Collection<File>getSelectedLibraryFiles(){

		Collection<File>libFiles = new ArrayList<File>();
		if(getSelectedRowCount() == 0)
			return libFiles;

		int colIdx = model.getColumnIndex(NISTLibraryTableModel.LIBRARY_FILE_COLUMN);
		for(int i : getSelectedRows())
			libFiles.add((File)model.getValueAt(convertRowIndexToModel(i), colIdx));

		return libFiles;
	}
	
	public Collection<File>getEnabledLibraryFiles(){

		Collection<File>libFiles = new ArrayList<File>();
		int colIdx = model.getColumnIndex(NISTLibraryTableModel.LIBRARY_FILE_COLUMN);
		int colActiveIdx = model.getColumnIndex(NISTLibraryTableModel.ACTIVE_COLUMN);
		for(int i=0; i<model.getRowCount(); i++) {
			
			if((boolean)model.getValueAt(i, colActiveIdx))
				libFiles.add((File)model.getValueAt(i, colIdx));
		}
		return libFiles;
	}

	public void setModelFromFiles(Map<File, Boolean> libFiles) {
		model.setModelFromFiles(libFiles);
	}
}










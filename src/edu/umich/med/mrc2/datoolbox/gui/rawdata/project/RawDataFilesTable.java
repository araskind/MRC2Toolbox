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

package edu.umich.med.mrc2.datoolbox.gui.rawdata.project;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import javax.swing.ListSelectionModel;
import javax.swing.table.TableRowSorter;

import edu.umich.med.mrc2.datoolbox.data.compare.FileBaseNameComparator;
import edu.umich.med.mrc2.datoolbox.data.format.FileBaseNameFormat;
import edu.umich.med.mrc2.datoolbox.gui.coderazzi.filters.gui.AutoChoices;
import edu.umich.med.mrc2.datoolbox.gui.coderazzi.filters.gui.TableFilterHeader;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.FileBaseNameRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.WordWrapCellRenderer;

public class RawDataFilesTable extends BasicTable {

	/**
	 *
	 */
	private static final long serialVersionUID = -1405921543482090501L;
	private RawDataFilesTableModel model;

	public RawDataFilesTable() {
		super();
		model =  new RawDataFilesTableModel();
		setModel(model);
		
		rowSorter = new TableRowSorter<RawDataFilesTableModel>(model);
		setRowSorter(rowSorter);
		rowSorter.setComparator(model.getColumnIndex(RawDataFilesTableModel.RAW_DATA_FILE_COLUMN),
				new FileBaseNameComparator());
		
		setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		columnModel.getColumnById(RawDataFilesTableModel.RAW_DATA_FILE_COLUMN)
			.setCellRenderer(new FileBaseNameRenderer());
		columnModel.getColumnById(RawDataFilesTableModel.FULL_PATH_COLUMN)
			.setCellRenderer(new WordWrapCellRenderer());

		thf = new TableFilterHeader(this, AutoChoices.ENABLED);
		thf.getParserModel().setFormat(File.class, new FileBaseNameFormat());
		thf.getParserModel().setComparator(File.class, new FileBaseNameComparator());
		
		finalizeLayout();
	}
	
	public void setTableModelFromDataFiles(Collection<File>dataFiles) {
		
		File[]files = dataFiles.toArray(new File[dataFiles.size()]);
		setTableModelFromDataFiles(files);
	}
	
	public void setTableModelFromDataFiles(File[]dataFiles) {
		
		thf.setTable(null);
		model.setTableModelFromDataFiles(dataFiles, true);
		thf.setTable(this);
		tca.adjustColumns();
	}
	
	public void addDataFiles(File[]dataFiles) {
		
		//	Check for duplicates
		Collection<File>current =  getDataFiles();
		File[]filesToAdd = Arrays.asList(dataFiles).stream().
				filter(f -> !current.contains(f)).toArray(size -> new File[size]);
		if(filesToAdd.length == 0)
			return;
		
		thf.setTable(null);
		model.setTableModelFromDataFiles(filesToAdd, false);
		thf.setTable(this);
		tca.adjustColumns();
	}
	
	public Collection<File> getSelectedDataFiles() {
		
		Collection<File>selected = new ArrayList<File>();
		if(getSelectedRows().length == 0)
			return selected;
		
		int fileColumn = model.getColumnIndex(RawDataFilesTableModel.RAW_DATA_FILE_COLUMN);
		for(int i : getSelectedRows())
			selected.add((File)model.getValueAt(convertRowIndexToModel(i), fileColumn));
		
		return selected;
	}
	
	public Collection<File> getDataFiles() {
		
		Collection<File>selected = new ArrayList<File>();
		int fileColumn = model.getColumnIndex(RawDataFilesTableModel.RAW_DATA_FILE_COLUMN);
		for(int i=0; i<model.getRowCount(); i++)
			selected.add((File)model.getValueAt(i, fileColumn));
		
		return selected;
	}
}

















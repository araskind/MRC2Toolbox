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

package edu.umich.med.mrc2.datoolbox.gui.io;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.ListSelectionModel;
import javax.swing.table.TableRowSorter;

import edu.umich.med.mrc2.datoolbox.data.compare.FileBaseNameComparator;
import edu.umich.med.mrc2.datoolbox.data.format.FileBaseNameFormat;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.AutoChoices;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.TableFilterHeader;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.FileBaseNameRenderer;

public class MinimalDataFileListingTable extends BasicTable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7128358130733304926L;

	public MinimalDataFileListingTable() {

		super();

		model = new MinimalDataFileListingTableModel();
		setModel(model);
		rowSorter = new TableRowSorter<MinimalDataFileListingTableModel>((MinimalDataFileListingTableModel)model);
		setRowSorter(rowSorter);
		rowSorter.setComparator(model.getColumnIndex(MinimalDataFileListingTableModel.DATA_FILE_COLUMN),
				new FileBaseNameComparator());

		setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
	
		columnModel.getColumnById(MinimalDataFileListingTableModel.DATA_FILE_COLUMN)
			.setCellRenderer(new FileBaseNameRenderer());

		thf = new TableFilterHeader(this, AutoChoices.ENABLED);
		thf.getParserModel().setComparator(File.class, new FileBaseNameComparator());
		thf.getParserModel().setFormat(File.class, new FileBaseNameFormat());
		finalizeLayout();
	}

	public void setModelFromDataFiles(Collection<File> files) {

		thf.setTable(null);
		((MinimalDataFileListingTableModel)model).setModelFromDataFiles(files, true);
		thf.setTable(this);
		adjustColumns();
	}
	
	public void addDataFiles(Collection<File> files) {

		thf.setTable(null);
		((MinimalDataFileListingTableModel)model).setModelFromDataFiles(files, false);
		thf.setTable(this);
		adjustColumns();
	}
	
	public Collection<File>getSelectedFiles(){
		
		Collection<File>selected = new ArrayList<>();
		if(getSelectedRowCount() == 0)
			return selected;
		
		int fileColumn = model.getColumnIndex(MinimalDataFileListingTableModel.DATA_FILE_COLUMN);
		for(int i : getSelectedRows())
			selected.add((File)model.getValueAt(convertRowIndexToModel(i),fileColumn));
				
		return selected;
	}
	
	public Collection<File>getAllFiles(){
		
		Collection<File>files = new ArrayList<>();
		int fileColumn = model.getColumnIndex(MinimalDataFileListingTableModel.DATA_FILE_COLUMN);
		for(int i=0; i<model.getRowCount(); i++)
			files.add((File)model.getValueAt(i,fileColumn));
				
		return files;
	}
}


























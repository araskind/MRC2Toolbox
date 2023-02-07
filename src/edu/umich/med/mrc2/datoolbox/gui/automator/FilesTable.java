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

package edu.umich.med.mrc2.datoolbox.gui.automator;

import java.io.File;
import java.util.Collection;
import java.util.TreeSet;

import javax.swing.ListSelectionModel;
import javax.swing.table.TableRowSorter;

import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.AutoChoices;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.TableFilterHeader;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.FileRenderer;

public class FilesTable extends BasicTable {

	/**
	 *
	 */
	private static final long serialVersionUID = -5022129546919771647L;
	private FilesTableModel model;

	public FilesTable() {
		super();
		model = new FilesTableModel();
		setModel(model);
		rowSorter = new TableRowSorter<FilesTableModel>(model);
		setRowSorter(rowSorter);
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		columnModel.getColumnById(FilesTableModel.FILES_COLUMN).setCellRenderer(new FileRenderer());
		thf = new TableFilterHeader(this, AutoChoices.ENABLED);
		finalizeLayout();
	}

	public void setTableModelFromFileCollection(Collection<File> files) {
		thf.setTable(null);;
		model.setTableModelFromAssayCollection(files);
		thf.setTable(this);
		tca.adjustColumns();
	}
	
	public File getSelectedFile() {
		
		if(getSelectedRow() == -1)
			return null;
		
		return (File)getValueAt(getSelectedRow(), getColumnIndex(FilesTableModel.FILES_COLUMN));
	}
	
	public Collection<File> getAllFiles() {
		
		Collection<File>files = new TreeSet<File>();
		int fileCol = model.getColumnIndex(FilesTableModel.FILES_COLUMN);
		for(int i = 0; i<model.getRowCount(); i++) {
			
			File f = (File)model.getValueAt(i, fileCol);
			if(f != null)
				files.add(f);
		}		
		return files;		
	}
}

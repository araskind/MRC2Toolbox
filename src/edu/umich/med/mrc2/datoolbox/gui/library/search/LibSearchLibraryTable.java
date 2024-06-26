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

package edu.umich.med.mrc2.datoolbox.gui.library.search;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.ListSelectionModel;
import javax.swing.table.TableRowSorter;

import edu.umich.med.mrc2.datoolbox.data.CompoundLibrary;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.AutoChoices;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.TableFilterHeader;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.PolarityRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.WordWrapCellRenderer;

public class LibSearchLibraryTable extends BasicTable {

	/**
	 *
	 */
	private static final long serialVersionUID = 2890411818566368839L;

	public LibSearchLibraryTable() {

		super();
		model = new LibSearchLibraryTableModel();
		setModel(model);
		rowSorter = new TableRowSorter<LibSearchLibraryTableModel>(
				(LibSearchLibraryTableModel)model);
		setRowSorter(rowSorter);
		
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		columnModel.getColumnById(LibSearchLibraryTableModel.LIBRARY_COLUMN)
			.setCellRenderer(new WordWrapCellRenderer());
		columnModel.getColumnById(LibSearchLibraryTableModel.DESCRIPTION_COLUMN)
			.setCellRenderer(new WordWrapCellRenderer());
		columnModel.getColumnById(LibSearchLibraryTableModel.POLARITY_COLUMN)
			.setCellRenderer(new PolarityRenderer());
		
		columnModel.getColumnById(LibSearchLibraryTableModel.USE_COLUMN).setMaxWidth(50);
		columnModel.getColumnById(LibSearchLibraryTableModel.POLARITY_COLUMN).setMaxWidth(50);
		fixedWidthColumns.add(model.getColumnIndex(LibSearchLibraryTableModel.USE_COLUMN));
		fixedWidthColumns.add(model.getColumnIndex(LibSearchLibraryTableModel.POLARITY_COLUMN));

		thf = new TableFilterHeader(this, AutoChoices.ENABLED);
		finalizeLayout();
	}

	public void setTableModelFromLibraryCollection(Collection<CompoundLibrary> libraryCollection) {
		thf.setTable(null);
		((LibSearchLibraryTableModel)model).setTableModelFromLibraryCollection(libraryCollection);
		thf.setTable(this);
		adjustColumns();
	}

	public List<CompoundLibrary>getSelectedLibraries(){

		int useIndex = model.getColumnIndex(LibSearchLibraryTableModel.USE_COLUMN);
		int libIndex = model.getColumnIndex(LibSearchLibraryTableModel.LIBRARY_COLUMN);
		ArrayList<CompoundLibrary>selected = new ArrayList<CompoundLibrary>();

		for(int i=0; i<model.getRowCount(); i++) {

			if((Boolean)model.getValueAt(i, useIndex) == Boolean.TRUE)
				selected.add((CompoundLibrary)model.getValueAt(i, libIndex));
		}
		return selected;
	}
}

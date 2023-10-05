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

package edu.umich.med.mrc2.datoolbox.gui.library.manager;

import java.util.Collection;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableRowSorter;

import edu.umich.med.mrc2.datoolbox.data.CompoundLibrary;
import edu.umich.med.mrc2.datoolbox.data.compare.CompoundLibraryComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.format.CompoundLibraryFormat;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.AutoChoices;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.TableFilterHeader;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.CompoundLibraryRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.PolarityRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.WordWrapCellRenderer;

public class LibraryListingTable extends BasicTable {

	/**
	 *
	 */
	private static final long serialVersionUID = -8952035680682723707L;
	private LibraryListingTableModel model;

	public LibraryListingTable() {

		super();
		model = new LibraryListingTableModel();
		setModel(model);
		rowSorter = new TableRowSorter<LibraryListingTableModel>(model);
		setRowSorter(rowSorter);
		rowSorter.setComparator(model.getColumnIndex(LibraryListingTableModel.LIBRARY_COLUMN),
				new CompoundLibraryComparator(SortProperty.Name));
		
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		columnModel.getColumnById(LibraryListingTableModel.LIBRARY_COLUMN)
			.setCellRenderer(new CompoundLibraryRenderer(SortProperty.Name));
		columnModel.getColumnById(LibraryListingTableModel.DESCRIPTION_COLUMN)
			.setCellRenderer(new WordWrapCellRenderer());
		columnModel.getColumnById(LibraryListingTableModel.POLARITY_COLUMN)
			.setCellRenderer(new PolarityRenderer());
		
		setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		thf = new TableFilterHeader(this, AutoChoices.ENABLED);
		thf.getParserModel().setFormat(CompoundLibrary.class, 
				new CompoundLibraryFormat(SortProperty.Name));
		thf.getParserModel().setComparator(CompoundLibrary.class, 
				new CompoundLibraryComparator(SortProperty.Name));
		finalizeLayout();
	}

	public void setTableModelFromLibraryCollection(
			Collection<CompoundLibrary> libraryCollection, 
			CompoundLibrary activeLibrary) {

		thf.setTable(null);
		model.setTableModelFromLibraryCollection(libraryCollection, activeLibrary);
		thf.setTable(this);
		tca.adjustColumns();
		
		if(activeLibrary != null)
			selectLibrary(activeLibrary);
	}
	
	public CompoundLibrary getSelectedLibrary(){
		
		int row = getSelectedRow();
		if(row == -1)
			return null;

		return (CompoundLibrary) model.getValueAt(convertRowIndexToModel(row), 
				model.getColumnIndex(LibraryListingTableModel.LIBRARY_COLUMN));
	}
	
	public void selectLibrary(CompoundLibrary toSelect){
		
		int col = model.getColumnIndex(LibraryListingTableModel.LIBRARY_COLUMN);
		for(int i = 0; i<model.getRowCount(); i++) {
			
			if(toSelect.equals(model.getValueAt(i, col))) {
				setRowSelectionInterval(i, i);
				scrollToSelected();
				return;
			}
		}
	}
}

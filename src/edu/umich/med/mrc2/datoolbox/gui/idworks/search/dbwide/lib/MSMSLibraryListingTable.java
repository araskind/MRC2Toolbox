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

package edu.umich.med.mrc2.datoolbox.gui.idworks.search.dbwide.lib;

import java.util.ArrayList;
import java.util.Collection;

import javax.swing.ListSelectionModel;
import javax.swing.table.TableRowSorter;

import edu.umich.med.mrc2.datoolbox.data.ReferenceMsMsLibrary;
import edu.umich.med.mrc2.datoolbox.data.compare.ReferenceMsMsLibraryComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.AutoChoices;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.TableFilterHeader;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.ReferenceMsMsLibraryRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.WordWrapCellRenderer;

public class MSMSLibraryListingTable extends BasicTable {

	/**
	 *
	 */
	private static final long serialVersionUID = -1405921543482090501L;
	private MSMSLibraryListingTableModel model;

	public MSMSLibraryListingTable() {
		super();
		model =  new MSMSLibraryListingTableModel();
		setModel(model);
		rowSorter = new TableRowSorter<MSMSLibraryListingTableModel>(model);
		setRowSorter(rowSorter);
		rowSorter.setComparator(model.getColumnIndex(MSMSLibraryListingTableModel.ID_COLUMN),
				new ReferenceMsMsLibraryComparator(SortProperty.ID));
		
		columnModel.getColumnById(MSMSLibraryListingTableModel.ID_COLUMN)
				.setCellRenderer(new ReferenceMsMsLibraryRenderer(SortProperty.ID));		
		columnModel.getColumnById(MSMSLibraryListingTableModel.DESCRIPTION_COLUMN)
			.setCellRenderer(new WordWrapCellRenderer());
		
		setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		
		thf = new TableFilterHeader(this, AutoChoices.ENABLED);
		finalizeLayout();
	}

	public void setTableModelFromReferenceMsMsLibraryList(Collection<ReferenceMsMsLibrary>libList) {
		thf.setTable(null);
		model.setTableModelFromReferenceMsMsLibraryList(libList);
		thf.setTable(this);
		tca.adjustColumns();
	}
	
	public void selectLibraries(Collection<ReferenceMsMsLibrary>libList) {

		int col = model.getColumnIndex(MSMSLibraryListingTableModel.ID_COLUMN);
		for(int i=0; i<getRowCount(); i++) {
			
			if(libList.contains(model.getValueAt(convertRowIndexToModel(i), col)))
				addRowSelectionInterval(i, i);
		}
	}

	public ReferenceMsMsLibrary getSelectedLibrary() {

		int row = getSelectedRow();
		if(row == -1)
			return null;

		return (ReferenceMsMsLibrary) model.getValueAt(convertRowIndexToModel(row),
				model.getColumnIndex(MSMSLibraryListingTableModel.ID_COLUMN));
	}
	
	public Collection<ReferenceMsMsLibrary>getSelectedLibraries() {

		Collection<ReferenceMsMsLibrary>selected = 
				new ArrayList<ReferenceMsMsLibrary>();
		int col = model.getColumnIndex(MSMSLibraryListingTableModel.ID_COLUMN);
		for(int i : getSelectedRows())
			selected.add((ReferenceMsMsLibrary) model.getValueAt(convertRowIndexToModel(i), col));

		return selected;
	}
}

















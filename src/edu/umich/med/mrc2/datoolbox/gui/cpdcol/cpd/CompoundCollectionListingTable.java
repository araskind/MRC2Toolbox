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

package edu.umich.med.mrc2.datoolbox.gui.cpdcol.cpd;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.Collection;

import javax.swing.ListSelectionModel;
import javax.swing.table.TableRowSorter;

import edu.umich.med.mrc2.datoolbox.data.cpdcoll.CompoundCollection;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.AutoChoices;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.TableFilterHeader;

public class CompoundCollectionListingTable extends BasicTable {

	/**
	 *
	 */
	private static final long serialVersionUID = -3817580957098149548L;

	public CompoundCollectionListingTable() {

		super();

		model = new CompoundCollectionListingTableModel();
		setModel(model);

		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		rowSorter = new TableRowSorter<CompoundCollectionListingTableModel>(
				(CompoundCollectionListingTableModel)model);
		setRowSorter(rowSorter);

		//	URL adapter
		MouseMotionAdapter mma = new MouseMotionAdapter() {

			public void mouseMoved(MouseEvent e) {

				Point p = e.getPoint();

				if(columnModel.isColumnVisible(columnModel.getColumnById(CompoundCollectionListingTableModel.URL_COLUMN))) {

					if (columnAtPoint(p) == columnModel.getColumnIndex(CompoundCollectionListingTableModel.URL_COLUMN))
						setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
					else
						setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				}
				else
					setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			}
		};
		addMouseMotionListener(mma);
		thf = new TableFilterHeader(this, AutoChoices.ENABLED);
		finalizeLayout();
	}
	
	public void setTableModelFromCompoundCollections(
			Collection<CompoundCollection> compoundCollections) {

		thf.setTable(null);
		((CompoundCollectionListingTableModel)model).
			setTableModelFromCompoundCollections(compoundCollections);
		thf.setTable(this);
		adjustColumns();
	}

	public CompoundCollection getSelectedCompoundCollection() {

		int row = getSelectedRow();
		if(row == -1)
			return null;

		return (CompoundCollection) model.getValueAt(
				convertRowIndexToModel(row), 
				model.getColumnIndex(CompoundCollectionListingTableModel.COLLECTION_NAME_COLUMN));
	}
}




















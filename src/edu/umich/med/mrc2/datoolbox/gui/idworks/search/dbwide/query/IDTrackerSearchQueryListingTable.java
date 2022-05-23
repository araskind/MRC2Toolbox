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

package edu.umich.med.mrc2.datoolbox.gui.idworks.search.dbwide.query;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.Collection;

import javax.swing.ListSelectionModel;
import javax.swing.table.TableRowSorter;

import edu.umich.med.mrc2.datoolbox.data.IDTSearchQuery;
import edu.umich.med.mrc2.datoolbox.data.compare.LIMSUserComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSUser;
import edu.umich.med.mrc2.datoolbox.gui.coderazzi.filters.gui.AutoChoices;
import edu.umich.med.mrc2.datoolbox.gui.coderazzi.filters.gui.TableFilterHeader;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.LIMSUserRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.WordWrapCellRenderer;

public class IDTrackerSearchQueryListingTable extends BasicTable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -363757863489122914L;
	private IDTrackerSearchQueryListingTableModel model;

	public IDTrackerSearchQueryListingTable() {
		super();
		model = new IDTrackerSearchQueryListingTableModel();
		setModel(model);
		getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		rowSorter = new TableRowSorter<IDTrackerSearchQueryListingTableModel>(model);
		setRowSorter(rowSorter);
		rowSorter.setComparator(model.getColumnIndex(IDTrackerSearchQueryListingTableModel.USER_COLUMN),
				new LIMSUserComparator(SortProperty.Name));
		
		LIMSUserRenderer userRenderer = new LIMSUserRenderer();
		setDefaultRenderer(LIMSUser.class, userRenderer);
		MouseMotionAdapter mma = new MouseMotionAdapter() {

			public void mouseMoved(MouseEvent e) {

				Point p = e.getPoint();

				if(columnModel.isColumnVisible(columnModel.getColumnById(IDTrackerSearchQueryListingTableModel.USER_COLUMN)) &&
					columnAtPoint(p) == columnModel.getColumnIndex(IDTrackerSearchQueryListingTableModel.USER_COLUMN))
					setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				else
					setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			}
		};
		addMouseMotionListener(mma);
		addMouseListener(userRenderer);
		addMouseMotionListener(userRenderer);
		
		columnModel.getColumnById(IDTrackerSearchQueryListingTableModel.QUERY_COLUMN)
			.setCellRenderer(new WordWrapCellRenderer());
		
		thf = new TableFilterHeader(this, AutoChoices.ENABLED);
		finalizeLayout();
	}
	
	public void setTableModelFromQueryList(Collection<IDTSearchQuery>queryList) {
		
		thf.setTable(null);
		model.setTableModelFromQueryList(queryList);
		thf.setTable(this);
		tca.adjustColumns();
	}
	
	public IDTSearchQuery getSelectedQuery() {
		
		int row = getSelectedRow();
		if(row == -1)
			return null;
		
		return (IDTSearchQuery)model.getValueAt(convertRowIndexToModel(row), 
				model.getColumnIndex(IDTrackerSearchQueryListingTableModel.QUERY_COLUMN));	
	}
	
	public void selectQuery(IDTSearchQuery query) {
		
		int col = model.getColumnIndex(IDTrackerSearchQueryListingTableModel.QUERY_COLUMN);
		for(int i=0; i<getRowCount(); i++) {
			if(model.getValueAt(convertRowIndexToModel(i), col).equals(query)) {
				addRowSelectionInterval(i, i);
				scrollToSelected();
				return;
			}
		}
	}

	public void removeQuery(IDTSearchQuery query) {
		
		int col = model.getColumnIndex(IDTrackerSearchQueryListingTableModel.QUERY_COLUMN);
		for(int i=0; i<getRowCount(); i++) {
			if(model.getValueAt(convertRowIndexToModel(i), col).equals(query)) {
				model.removeRow(convertRowIndexToModel(i));
				return;
			}
		}
	}	
}














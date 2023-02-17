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

package edu.umich.med.mrc2.datoolbox.gui.tables;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.RowSorterEvent;
import javax.swing.event.RowSorterListener;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;

import edu.umich.med.mrc2.datoolbox.gui.preferences.TableLayoutManager;

public class TableLayoutListener implements TableColumnModelListener {
	
	private BasicTable table;

	public TableLayoutListener(BasicTable table) {
		super();
		this.table = table;
		table.getColumnModel().addColumnModelListener(this);
		if(table.getRowSorter() != null) {
			
			table.getRowSorter().addRowSorterListener(new RowSorterListener() {
			    @Override
			    public void sorterChanged(RowSorterEvent e) {
			    	TableLayoutManager.setTableLayout(table);
			    }
			});
		}
	}

	@Override
	public void columnAdded(TableColumnModelEvent e) {
		TableLayoutManager.setTableLayout(table);
	}

	@Override
	public void columnRemoved(TableColumnModelEvent e) {
		TableLayoutManager.setTableLayout(table);
	}

	@Override
	public void columnMoved(TableColumnModelEvent e) {
		TableLayoutManager.setTableLayout(table);
	}

	@Override
	public void columnMarginChanged(ChangeEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void columnSelectionChanged(ListSelectionEvent e) {
		// TODO Auto-generated method stub

	}

}

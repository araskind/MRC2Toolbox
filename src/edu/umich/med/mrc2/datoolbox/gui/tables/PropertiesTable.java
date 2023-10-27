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

import java.util.Map;

import javax.swing.ListSelectionModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;

import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.AutoChoices;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.TableFilterHeader;

public class PropertiesTable extends BasicTable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5567062758292304236L;

	public PropertiesTable() {
		super();
		model = new PropertiesTableModel();
		setModel(model);
		rowSorter = new TableRowSorter<PropertiesTableModel>(
				(PropertiesTableModel) model);
		setRowSorter(rowSorter);
		
		setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		thf = new TableFilterHeader(this, AutoChoices.ENABLED);
		finalizeLayout();
	}
	
	public void setTableModelFromPropertyMap(Map<? extends Object,? extends Object>properties) {
		
		thf.setTable(null);
		((PropertiesTableModel)model).setTableModelFromPropertyMap(properties);
		thf.setTable(this);
		adjustColumns();
	}
	
	public void setPropertyNameRenderer(TableCellRenderer renderer) {		
		columnModel.getColumnById(PropertiesTableModel.PROPERTY_COLUMN).setCellRenderer(renderer);
		//	TODO add custom sorter and format to thf
		//	TODO add custom sorter to column, if necessary
	}
	
	public void setPropertyValueRenderer(TableCellRenderer renderer) {		
		columnModel.getColumnById(PropertiesTableModel.VALUE_COLUMN).setCellRenderer(renderer);
		//	TODO add custom sorter and format to thf
		//	TODO add custom sorter to column, if necessary
	}
}

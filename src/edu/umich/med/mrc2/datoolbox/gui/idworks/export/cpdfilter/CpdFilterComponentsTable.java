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

package edu.umich.med.mrc2.datoolbox.gui.idworks.export.cpdfilter;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.ListSelectionModel;
import javax.swing.table.TableRowSorter;

import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.AutoChoices;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.TableFilterHeader;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.WordWrapCellRenderer;

public class CpdFilterComponentsTable extends BasicTable {

	/**
	 *
	 */
	private static final long serialVersionUID = -392741375728418413L;

	public CpdFilterComponentsTable() {

		super();
		model = new CpdFilterComponentsTableModel();
		setModel(model);
		rowSorter = new TableRowSorter<CpdFilterComponentsTableModel>(
				(CpdFilterComponentsTableModel)model);
		setRowSorter(rowSorter);
		
		getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		setDefaultRenderer(String.class, new WordWrapCellRenderer());

		thf = new TableFilterHeader(this, AutoChoices.ENABLED);
		finalizeLayout();
	}
	
	public void setTableModelFromValuePairs(Map<String,String> valuePairs) {
		((CpdFilterComponentsTableModel)model).setTableModelFromValuePairs(valuePairs);
	}
	
	public Set<String>getFilterComponents(){
		
		Set<String>filterComponents = new TreeSet<String>();
		int col = model.getColumnIndex(
				CpdFilterComponentsTableModel.FILTER_COMPONENT_COLUMN);
		for(int i=0; i<model.getRowCount(); i++)
			filterComponents.add((String)model.getValueAt(i, col));
				
		return filterComponents;		
	}
}







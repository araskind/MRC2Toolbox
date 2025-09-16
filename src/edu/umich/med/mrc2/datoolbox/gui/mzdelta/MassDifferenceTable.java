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

package edu.umich.med.mrc2.datoolbox.gui.mzdelta;

import java.awt.Component;

import javax.swing.table.TableRowSorter;

import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.editors.DoubleValueCellEditor;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.AutoChoices;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.TableFilterHeader;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;

public class MassDifferenceTable extends BasicTable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public MassDifferenceTable() {
		super();
		model = new MassDifferenceTableModel();
		setModel(model);
		rowSorter = new TableRowSorter<MassDifferenceTableModel>(
				(MassDifferenceTableModel)model);
		setRowSorter(rowSorter);
		columnModel.getColumnById(MassDifferenceTableModel.MZ_DIFF_COLUMN)
			.setCellRenderer(mzRenderer);
		
		setDefaultEditor(Double.class, 
				new DoubleValueCellEditor(MRC2ToolBoxConfiguration.getMzFormat()));
		
		thf = new TableFilterHeader(this, AutoChoices.ENABLED);
		finalizeLayout();
	}

	public void addRowAndStartEditing() {
		
		((MassDifferenceTableModel)model).addNewRow();
		int column = model.getColumnIndex(MassDifferenceTableModel.MZ_DIFF_COLUMN);
		for(int i=0; i<model.getRowCount(); i++) {
			
			if(model.getValueAt(i, column) == null) {
				
		        editCellAt(convertRowIndexToView(i), column);

		        Component editorComponent = getEditorComponent();
		        if (editorComponent != null)
		            editorComponent.requestFocusInWindow();		
		        
		       break;
			}
		}
	}
	

}

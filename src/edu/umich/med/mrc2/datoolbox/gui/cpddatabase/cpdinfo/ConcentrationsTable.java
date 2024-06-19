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

package edu.umich.med.mrc2.datoolbox.gui.cpddatabase.cpdinfo;

import java.util.Collection;

import javax.swing.ListSelectionModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;

import edu.umich.med.mrc2.datoolbox.data.CompoundConcentration;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.AutoChoices;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.TableFilterHeader;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.WordWrapCellRenderer;

public class ConcentrationsTable extends BasicTable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5171270869259062864L;
	
	public ConcentrationsTable() {
		
		super();
		model = new ConcentrationsTableModel();
		setModel(model);
		rowSorter = new TableRowSorter<ConcentrationsTableModel>(
				(ConcentrationsTableModel)model);
		setRowSorter(rowSorter);
		
		setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		TableColumn commentsColumn = 
				columnModel.getColumnById(ConcentrationsTableModel.COMMENTS_COLUMN);
		commentsColumn.setCellRenderer(new WordWrapCellRenderer()); 
		commentsColumn.setMinWidth(200);
		fixedWidthColumns.add(commentsColumn.getModelIndex());

		thf = new TableFilterHeader(this, AutoChoices.ENABLED);
		finalizeLayout();
	}

	public void setModelFromConcentrations(Collection<CompoundConcentration>concentrations) {
		
		thf.setTable(null);
		((ConcentrationsTableModel)model).setModelFromConcentrations(concentrations);		
		thf.setTable(this);
		adjustColumns();
	}
}







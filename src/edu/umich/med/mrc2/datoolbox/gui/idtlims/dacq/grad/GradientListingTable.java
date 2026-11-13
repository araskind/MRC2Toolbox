/*******************************************************************************
 *
 * (C) Copyright 2018-2026 MRC2 (http://mrc2.umich.edu).
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

package edu.umich.med.mrc2.datoolbox.gui.idtlims.dacq.grad;

import java.util.Collection;

import javax.swing.ListSelectionModel;
import javax.swing.table.TableRowSorter;

import edu.umich.med.mrc2.datoolbox.data.compare.ChromatographicGradientComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.format.ChromatographicGradientFormat;
import edu.umich.med.mrc2.datoolbox.data.lims.ChromatographicGradient;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.AutoChoices;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.TableFilterHeader;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.ChromatographicGradientRenderer;

public class GradientListingTable extends BasicTable {


	/**
	 * 
	 */
	private static final long serialVersionUID = -2695577206283079527L;

	public GradientListingTable() {
		super();
		model =  new GradientListingTableModel();
		setModel(model);
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		rowSorter = new TableRowSorter<GradientListingTableModel>(
				(GradientListingTableModel)model);
		setRowSorter(rowSorter);
		rowSorter.setComparator(model.getColumnIndex(GradientListingTableModel.ID_COLUMN),
				new ChromatographicGradientComparator(SortProperty.ID));
		columnModel.getColumnById(GradientListingTableModel.ID_COLUMN)
			.setCellRenderer(new ChromatographicGradientRenderer(SortProperty.ID));
		
		thf = new TableFilterHeader(this, AutoChoices.ENABLED);
		thf.getParserModel().setFormat(ChromatographicGradient.class, new ChromatographicGradientFormat(SortProperty.ID));
		thf.getParserModel().setComparator(ChromatographicGradient.class, new ChromatographicGradientComparator(SortProperty.ID));
		finalizeLayout();
	}

	public void setTableModelFromGradientCollection(Collection<ChromatographicGradient> gradients) {
		thf.setTable(null);
		((GradientListingTableModel)model).setTableModelFromGradientCollection(gradients);
		thf.setTable(this);
		adjustColumns();
	}
	
	public ChromatographicGradient getSelectedGradient() {
		
		int row = getSelectedRow();
		if(row == -1)
			return null;
		else
			return (ChromatographicGradient)model.getValueAt(
					convertRowIndexToModel(row), model.getColumnIndex(GradientListingTableModel.ID_COLUMN));		
	}
}

















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

package edu.umich.med.mrc2.datoolbox.gui.binner.control;

import java.util.Arrays;
import java.util.Collection;

import javax.swing.ListSelectionModel;
import javax.swing.table.TableRowSorter;

import edu.umich.med.mrc2.datoolbox.data.BinnerAdductList;
import edu.umich.med.mrc2.datoolbox.data.compare.LIMSUserComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.format.LIMSUserFormat;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSUser;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.AutoChoices;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.TableFilterHeader;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.BinnerAnnotationListRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.WordWrapCellRenderer;

public class BinnerAnnotationListsTable extends BasicTable {

	/**
	 *
	 */
	private static final long serialVersionUID = 1511489263877797538L;
	
	public BinnerAnnotationListsTable() {

		super();
		model = new BinnerAnnotationListsTableModel();
		setModel(model);
		
		rowSorter = new TableRowSorter<BinnerAnnotationListsTableModel>(
				(BinnerAnnotationListsTableModel)model);
		setRowSorter(rowSorter);
		rowSorter.setComparator(model.getColumnIndex(BinnerAnnotationListsTableModel.OWNER_COLUMN),
				new LIMSUserComparator(SortProperty.Name));
		
		getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		setDefaultRenderer(BinnerAdductList.class, new BinnerAnnotationListRenderer(SortProperty.Name));
		createInteractiveUserRenderer(Arrays.asList(BinnerAnnotationListsTableModel.OWNER_COLUMN));
		columnModel.getColumnById(BinnerAnnotationListsTableModel.DESCRIPTION_COLUMN)
			.setCellRenderer(new WordWrapCellRenderer());

		thf = new TableFilterHeader(this, AutoChoices.ENABLED);
		thf.getParserModel().setFormat(LIMSUser.class, new LIMSUserFormat());
		thf.getParserModel().setComparator(LIMSUser.class, new LIMSUserComparator(SortProperty.Name));
		finalizeLayout();
	}

	public void setTableModelFromBinnerAdductCollection(Collection<BinnerAdductList> collection) {
		
		thf.setTable(null);
		((BinnerAnnotationListsTableModel)model).setTableModelFromBinnerAdductListCollection(collection);
		thf.setTable(this);
		adjustColumns();
	}
	
	public BinnerAdductList getSelectedBinnerAdductList() {
		
		int row = getSelectedRow();
		if(row < 0)
			return null;
		else {
			int column = model.getColumnIndex(BinnerAnnotationListsTableModel.NAME_COLUMN);
			BinnerAdductList selected = (BinnerAdductList)model.getValueAt(convertRowIndexToModel(row), column);
			return selected;
		}
	}
}

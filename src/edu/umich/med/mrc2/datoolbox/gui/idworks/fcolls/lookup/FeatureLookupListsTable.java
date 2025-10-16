/*******************************************************************************
 *
 * (C) Copyright 2018-2025 MRC2 (http://mrc2.umich.edu).
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

package edu.umich.med.mrc2.datoolbox.gui.idworks.fcolls.lookup;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

import javax.swing.ListSelectionModel;
import javax.swing.table.TableRowSorter;

import edu.umich.med.mrc2.datoolbox.data.compare.LIMSUserComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.format.LIMSUserFormat;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSUser;
import edu.umich.med.mrc2.datoolbox.data.msclust.FeatureLookupList;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.AutoChoices;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.TableFilterHeader;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.TimestampRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.WordWrapCellRenderer;

public class FeatureLookupListsTable extends BasicTable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6499296198033405250L;

	public FeatureLookupListsTable() {
		super();
		model = new FeatureLookupListsTableModel();
		setModel(model);
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		rowSorter = new TableRowSorter<FeatureLookupListsTableModel>(
				(FeatureLookupListsTableModel)model);
		setRowSorter(rowSorter);
		rowSorter.setComparator(model.getColumnIndex(FeatureLookupListsTableModel.OWNER_COLUMN),
				new LIMSUserComparator(SortProperty.Name));
		
		WordWrapCellRenderer ltr = new WordWrapCellRenderer();
		columnModel.getColumnById(FeatureLookupListsTableModel.DATA_SET_COLUMN)
			.setCellRenderer(ltr);
		columnModel.getColumnById(FeatureLookupListsTableModel.DESCRIPTION_COLUMN)
			.setCellRenderer(ltr);
		
		columnModel.getColumnById(FeatureLookupListsTableModel.DATA_SET_COLUMN)
			.setMinWidth(120);
		columnModel.getColumnById(FeatureLookupListsTableModel.DESCRIPTION_COLUMN)
			.setMinWidth(120);
		createInteractiveUserRenderer(Arrays.asList(
				FeatureLookupListsTableModel.OWNER_COLUMN));
		
		setDefaultRenderer(Date.class, new TimestampRenderer());
		
		thf = new TableFilterHeader(this, AutoChoices.ENABLED);
		thf.getParserModel().setFormat(LIMSUser.class, new LIMSUserFormat());
		thf.getParserModel().setComparator(LIMSUser.class, 
				new LIMSUserComparator(SortProperty.Name));
		
		finalizeLayout();
	}

	public void setTableModelFromFeatureLookupListCollection(
			Collection<FeatureLookupList>dataSetList) {
		
		thf.setTable(null);
		((FeatureLookupListsTableModel)model).setTableModelFromFeatureLookupDataSetList(dataSetList);			
		thf.setTable(this);
		adjustColumns();
	}
	
	public FeatureLookupList getSelectedDataSet() {
		
		int row = getSelectedRow();
		if(row == -1)
			return null;
		
		return (FeatureLookupList)model.getValueAt(
				convertRowIndexToModel(row), 
				model.getColumnIndex(FeatureLookupListsTableModel.DATA_SET_COLUMN));
	}
	
	public void selectDataSet(FeatureLookupList toSelect) {
		
		int col = getColumnIndex(FeatureLookupListsTableModel.DATA_SET_COLUMN);
		for(int i=0; i<getRowCount(); i++) {
			
			if(toSelect.equals(getValueAt(i, col))) {
				setRowSelectionInterval(i, i);
//				scrollToSelected();
				return;
			}
		}
	}
	
	public void updateCollectionData(FeatureLookupList edited) {
		thf.setTable(null);
		((FeatureLookupListsTableModel)model).updateCollectionData(edited);
		thf.setTable(this);
		adjustColumns();
	}
}
















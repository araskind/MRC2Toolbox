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

package edu.umich.med.mrc2.datoolbox.gui.idworks.fcolls.binner;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

import javax.swing.ListSelectionModel;
import javax.swing.table.TableRowSorter;

import edu.umich.med.mrc2.datoolbox.data.compare.LIMSUserComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.format.LIMSUserFormat;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSUser;
import edu.umich.med.mrc2.datoolbox.data.msclust.BinnerAnnotationLookupDataSet;
import edu.umich.med.mrc2.datoolbox.data.msclust.FeatureLookupList;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.AutoChoices;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.TableFilterHeader;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.TimestampRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.WordWrapCellRenderer;

public class BinnerAnnotationLookupDataSetListTable extends BasicTable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6499296198033405250L;

	public BinnerAnnotationLookupDataSetListTable() {
		super();
		model = new BinnerAnnotationLookupDataSetListTableModel();
		setModel(model);
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		rowSorter = new TableRowSorter<BinnerAnnotationLookupDataSetListTableModel>(
				(BinnerAnnotationLookupDataSetListTableModel)model);
		setRowSorter(rowSorter);
		rowSorter.setComparator(model.getColumnIndex(BinnerAnnotationLookupDataSetListTableModel.OWNER_COLUMN),
				new LIMSUserComparator(SortProperty.Name));
		
		WordWrapCellRenderer ltr = new WordWrapCellRenderer();
		columnModel.getColumnById(BinnerAnnotationLookupDataSetListTableModel.DATA_SET_COLUMN)
			.setCellRenderer(ltr);
		columnModel.getColumnById(BinnerAnnotationLookupDataSetListTableModel.DESCRIPTION_COLUMN)
			.setCellRenderer(ltr);
		
		columnModel.getColumnById(BinnerAnnotationLookupDataSetListTableModel.DATA_SET_COLUMN)
			.setMinWidth(120);
		columnModel.getColumnById(BinnerAnnotationLookupDataSetListTableModel.DESCRIPTION_COLUMN)
			.setMinWidth(120);
		createInteractiveUserRenderer(Arrays.asList(
				BinnerAnnotationLookupDataSetListTableModel.OWNER_COLUMN));
		
		setDefaultRenderer(Date.class, new TimestampRenderer());
		
		thf = new TableFilterHeader(this, AutoChoices.ENABLED);
		thf.getParserModel().setFormat(LIMSUser.class, new LIMSUserFormat());
		thf.getParserModel().setComparator(LIMSUser.class, 
				new LIMSUserComparator(SortProperty.Name));
		
		finalizeLayout();
	}

	public void setTableModelFromBinnerAnnotationLookupDataSetList(
			Collection<BinnerAnnotationLookupDataSet>dataSetList) {
		
		thf.setTable(null);
		((BinnerAnnotationLookupDataSetListTableModel)model).
			setTableModelFromBinnerAnnotationLookupDataSetList(dataSetList);			
		thf.setTable(this);
		adjustColumns();
	}
	
	public BinnerAnnotationLookupDataSet getSelectedDataSet() {
		
		int row = getSelectedRow();
		if(row == -1)
			return null;
		
		return (BinnerAnnotationLookupDataSet)model.getValueAt(
				convertRowIndexToModel(row), 
				model.getColumnIndex(BinnerAnnotationLookupDataSetListTableModel.DATA_SET_COLUMN));
	}
	
	public void selectDataSet(BinnerAnnotationLookupDataSet toSelect) {
		
		int col = getColumnIndex(BinnerAnnotationLookupDataSetListTableModel.DATA_SET_COLUMN);
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
		((BinnerAnnotationLookupDataSetListTableModel)model).updateCollectionData(edited);
		thf.setTable(this);
		adjustColumns();
	}
}
















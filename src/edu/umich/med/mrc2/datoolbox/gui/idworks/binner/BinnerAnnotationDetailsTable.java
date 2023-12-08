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

package edu.umich.med.mrc2.datoolbox.gui.idworks.binner;

import javax.swing.ListSelectionModel;
import javax.swing.table.TableRowSorter;

import edu.umich.med.mrc2.datoolbox.data.BinnerAnnotation;
import edu.umich.med.mrc2.datoolbox.data.msclust.BinnerBasedMsFeatureInfoBundleCluster;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.AutoChoices;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.TableFilterHeader;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.RadioButtonRenderer;

public class BinnerAnnotationDetailsTable extends BasicTable {

	/**
	 *
	 */
	private static final long serialVersionUID = 5111265849253755884L;
	private BinnerAnnotationDetailsTableModel model;
	
	public BinnerAnnotationDetailsTable() {
		super();
		model = new BinnerAnnotationDetailsTableModel();
		setModel(model);
		rowSorter = new TableRowSorter<BinnerAnnotationDetailsTableModel>(model);
		setRowSorter(rowSorter);
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		columnModel.getColumnById(BinnerAnnotationDetailsTableModel.PRIMARY_COLUMN)
			.setCellRenderer(new RadioButtonRenderer());
		columnModel.getColumnById(BinnerAnnotationDetailsTableModel.RT_COLUMN)
			.setCellRenderer(rtRenderer);
		columnModel.getColumnById(BinnerAnnotationDetailsTableModel.MZ_COLUMN)
			.setCellRenderer(mzRenderer);
		columnModel.getColumnById(BinnerAnnotationDetailsTableModel.MASS_ERROR_COLUMN)
			.setCellRenderer(mzRenderer);
		columnModel.getColumnById(BinnerAnnotationDetailsTableModel.RMD_COLUMN)
			.setCellRenderer(ppmRenderer);
		
		setExactColumnWidth(BinnerAnnotationDetailsTableModel.PRIMARY_COLUMN, 50);
		setExactColumnWidth(BinnerAnnotationDetailsTableModel.DETECTED_COLUMN, 50);
		
		thf = new TableFilterHeader(this, AutoChoices.ENABLED);
		addTablePopupMenu(new BinnerAnnotationDetailsTablePopupMenu(this, this));
		finalizeLayout();		
	}

	public void setTableModelFromBinnerAnnotationCluster(
			BinnerBasedMsFeatureInfoBundleCluster baCluster) {
		thf.setTable(null);
		model.setTableModelFromBinnerAnnotationCluster(baCluster);
		thf.setTable(this);
		tca.adjustColumns();
	}

	public BinnerAnnotation getSelectedBinnerAnnotation() {

		int row = getSelectedRow();
		if(row == -1)
			return null;

		return (BinnerAnnotation)model.getValueAt(convertRowIndexToModel(row),
				model.getColumnIndex(BinnerAnnotationDetailsTableModel.ANNOTATION_COLUMN));
	}
}

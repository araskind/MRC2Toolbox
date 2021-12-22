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

package edu.umich.med.mrc2.datoolbox.gui.cpddatabase.curator.ctab;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.Collections;

import javax.swing.ListSelectionModel;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableRowSorter;

import edu.umich.med.mrc2.datoolbox.data.CompoundIdentity;
import edu.umich.med.mrc2.datoolbox.data.CompoundIdentityCluster;
import edu.umich.med.mrc2.datoolbox.data.compare.CompoundIdentityComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundIdentityField;
import edu.umich.med.mrc2.datoolbox.data.format.CompoundIdentityFormat;
import edu.umich.med.mrc2.datoolbox.gui.coderazzi.filters.gui.AutoChoices;
import edu.umich.med.mrc2.datoolbox.gui.coderazzi.filters.gui.TableFilterHeader;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.CompoundIdentityDatabaseLinkRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.RadioButtonRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.WordWrapCellRenderer;

public class CompoundIdentityClusterTable extends BasicTable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4196101350391353613L;
	
	private CompoundIdentityClusterTableModel model;
	private CompoundIdentityCluster activeCluster;
	private TableModelListener defaultIdTableModelListener;

	public CompoundIdentityClusterTable() {
		super();
		model = new CompoundIdentityClusterTableModel();
		setModel(model);

		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		rowSorter = new TableRowSorter<CompoundIdentityClusterTableModel>(model);
		setRowSorter(rowSorter);
		
		columnModel.getColumnById(CompoundIdentityClusterTableModel.PRIMARY_COLUMN)
			.setCellRenderer(new RadioButtonRenderer());
		msfIdRenderer = new CompoundIdentityDatabaseLinkRenderer();
		columnModel.getColumnById(CompoundIdentityClusterTableModel.ID_COLUMN)
			.setCellRenderer(msfIdRenderer);
		columnModel.getColumnById(CompoundIdentityClusterTableModel.MASS_COLUMN)
			.setCellRenderer(mzRenderer); // Neutral mass		
		columnModel.getColumnById(CompoundIdentityClusterTableModel.COMPOUND_NAME_COLUMN)
			.setCellRenderer(new WordWrapCellRenderer());

		//	Database link adapter
		MouseMotionAdapter mma = new MouseMotionAdapter() {

			public void mouseMoved(MouseEvent e) {

				Point p = e.getPoint();

				if(columnModel.isColumnVisible(columnModel.getColumnById(CompoundIdentityClusterTableModel.ID_COLUMN))) {

					if (columnAtPoint(p) == columnModel.getColumnIndex(CompoundIdentityClusterTableModel.ID_COLUMN))
						setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
					else
						setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				}
				else
					setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			}
		};
		addMouseMotionListener(mma);
		addMouseListener(msfIdRenderer);
		addMouseMotionListener(msfIdRenderer);

		thf = new TableFilterHeader(this, AutoChoices.ENABLED);
		thf.getParserModel().setFormat(CompoundIdentity.class, 
				new CompoundIdentityFormat(CompoundIdentityField.DB_ID));
		thf.getParserModel().setComparator(CompoundIdentity.class, 
				new CompoundIdentityComparator(SortProperty.ID));
		columnModel.getColumnById(CompoundIdentityClusterTableModel.PRIMARY_COLUMN).setWidth(50);
		columnModel.getColumnById(CompoundIdentityClusterTableModel.COMPOUND_NAME_COLUMN).setMinWidth(250);
		finalizeLayout();
	}
	
	public void setModelFromCompoundIdentityCluster(CompoundIdentityCluster cluster) {
		
		removeModelListeners();
		thf.setTable(null);
		model.setModelFromCompoundIdentityCluster(cluster);
		activeCluster = cluster;
		tca.adjustColumnsExcluding(Collections.singleton(
				columnModel.getColumnIndex(CompoundIdentityClusterTableModel.PRIMARY_COLUMN)));
		columnModel.getColumnById(CompoundIdentityClusterTableModel.COMPOUND_NAME_COLUMN).setMinWidth(250);
		thf.setTable(this);
		addModelListeners();
	}
	
	public CompoundIdentity getSelectedCompoundId() {

		int row = getSelectedRow();
		if(row == -1)
			return null;

		return ((CompoundIdentity) model.getValueAt(
				convertRowIndexToModel(row), 
				model.getColumnIndex(CompoundIdentityClusterTableModel.ID_COLUMN)));
	}
	
	@Override
	public synchronized void clearTable() {
		
		activeCluster = null;
		super.clearTable();
	}

	public CompoundIdentityCluster getActiveCluster() {
		return activeCluster;
	}
	
	public CompoundIdentity getCompoundIdentityAtPopup() {

		if(popupRow == -1)
			return null;
			
		return (CompoundIdentity) model.getValueAt(convertRowIndexToModel(popupRow), 
				model.getColumnIndex(CompoundIdentityClusterTableModel.ID_COLUMN));
	}

	public void setDefaultIdTableModelListener(TableModelListener defaultIdTableModelListener) {
		this.defaultIdTableModelListener = defaultIdTableModelListener;
		model.addTableModelListener(defaultIdTableModelListener);
	}
	
	private void removeModelListeners() {
		
		if(defaultIdTableModelListener != null)
			model.removeTableModelListener(defaultIdTableModelListener);
	}
	
	private void addModelListeners() {
		
		if(defaultIdTableModelListener != null)
			model.addTableModelListener(defaultIdTableModelListener);
	}
}

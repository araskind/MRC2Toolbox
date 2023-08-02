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

package edu.umich.med.mrc2.datoolbox.gui.cpddatabase;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import javax.swing.ListSelectionModel;
import javax.swing.table.TableRowSorter;

import edu.umich.med.mrc2.datoolbox.data.CompoundIdentity;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureIdentity;
import edu.umich.med.mrc2.datoolbox.data.compare.MsFeatureIdentityComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundIdentityField;
import edu.umich.med.mrc2.datoolbox.data.format.MsFeatureIdentityFormat;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.AutoChoices;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.TableFilterHeader;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.BooleanColorCircleFlagRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.CompoundIdentityDatabaseLinkRenderer;

public class CuratedDatabaseCompoundTable extends BasicTable {

	/**
	 *
	 */
	private static final long serialVersionUID = -3817580957098149548L;

	private CuratedDatabaseCompoundTableModel model;
	private MouseMotionAdapter mma;

	public CuratedDatabaseCompoundTable() {

		super();

		model = new CuratedDatabaseCompoundTableModel();
		setModel(model);

		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		rowSorter = new TableRowSorter<CuratedDatabaseCompoundTableModel>(model);
		setRowSorter(rowSorter);

		rowSorter.setComparator(model.getColumnIndex(CuratedDatabaseCompoundTableModel.ID_COLUMN),
				new MsFeatureIdentityComparator(SortProperty.ID));

		// Database ID column
		//cidRenderer = new CompoundIdentityRenderer(CompoundIdentityField.DB_ID);
		msfIdRenderer = new CompoundIdentityDatabaseLinkRenderer();
		columnModel.getColumnById(CuratedDatabaseCompoundTableModel.ID_COLUMN)
			.setCellRenderer(msfIdRenderer);
		columnModel.getColumnById(CuratedDatabaseCompoundTableModel.MASS_COLUMN)
			.setCellRenderer(mzRenderer); // Neutral mass
		columnModel.getColumnById(CuratedDatabaseCompoundTableModel.CURATED_COLUMN)
		.setCellRenderer(new BooleanColorCircleFlagRenderer(15));
		

		//	Database link adapter
		mma = new MouseMotionAdapter() {

			public void mouseMoved(MouseEvent e) {

				Point p = e.getPoint();

				if(columnModel.isColumnVisible(columnModel.getColumnById(CuratedDatabaseCompoundTableModel.ID_COLUMN))) {

					if (columnAtPoint(p) == columnModel.getColumnIndex(CuratedDatabaseCompoundTableModel.ID_COLUMN))
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
		thf.getParserModel().setFormat(MsFeatureIdentity.class, new MsFeatureIdentityFormat(CompoundIdentityField.DB_ID));
		thf.getParserModel().setComparator(MsFeatureIdentity.class, new MsFeatureIdentityComparator(SortProperty.ID));
		finalizeLayout();
	}

	public void addCompoundPopupListener(ActionListener listener) {

		CompoundPopupMenu popupMenu = new CompoundPopupMenu(listener);
		setComponentPopupMenu(popupMenu);
		addMouseListener(new MouseAdapter() {
			private int popupRow;

			@Override
			public void mousePressed(MouseEvent e) {
				popupRow = rowAtPoint(e.getPoint());
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				popupRow = rowAtPoint(e.getPoint());
			}
		});
	}

	public void setTableModelFromCompoundCollection(Map<CompoundIdentity,Boolean> compoundCollection) {

		thf.setTable(null);
		model.setTableModelFromCompoundCollection(compoundCollection);
		thf.setTable(this);
		tca.adjustColumns();
	}

	public MsFeatureIdentity getSelectedIdentity() {

		int row = getSelectedRow();
		if(row == -1)
			return null;

		return (MsFeatureIdentity) model.getValueAt(
				convertRowIndexToModel(row), 
				model.getColumnIndex(CuratedDatabaseCompoundTableModel.ID_COLUMN));
	}
	
	public CompoundIdentity getSelectedCompound() {

		int row = getSelectedRow();
		if(row == -1)
			return null;

		return ((MsFeatureIdentity) model.getValueAt(
				convertRowIndexToModel(row), 
				model.getColumnIndex(CuratedDatabaseCompoundTableModel.ID_COLUMN))).
					getCompoundIdentity();
	}

	public Collection<CompoundIdentity> getListedCompounds() {

		Collection<CompoundIdentity>listedCompounds = new ArrayList<CompoundIdentity>();
		int idCol = model.getColumnIndex(CuratedDatabaseCompoundTableModel.ID_COLUMN);
		for(int i=0; i<getRowCount(); i++)
			listedCompounds.add(
				((MsFeatureIdentity) model.getValueAt(
						convertRowIndexToModel(i), idCol)).getCompoundIdentity());

		return listedCompounds;
	}

	public void updateMSFidData(MsFeatureIdentity id, boolean isCurated) {
		model.updateMSFidData(id, isCurated);
	}
	
	public void updateCidData(CompoundIdentity cid, boolean isCurated) {
		model.updateCidData(cid, isCurated);
	}

	public void selectCompoundIdentity(CompoundIdentity cid) {
		
		int idCol = model.getColumnIndex(CuratedDatabaseCompoundTableModel.ID_COLUMN);
		for(int i=0; i<getRowCount(); i++) {
			
			CompoundIdentity rowId = ((MsFeatureIdentity) model.getValueAt(
					convertRowIndexToModel(i), idCol)).getCompoundIdentity();			
			if(rowId.equals(cid)) {
				setRowSelectionInterval(i, i);
				scrollToSelected();
				return;
			}
		}
	}
}




















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

package edu.umich.med.mrc2.datoolbox.gui.idworks.ms2;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.JCheckBox;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableRowSorter;

import edu.umich.med.mrc2.datoolbox.data.CompoundIdentity;
import edu.umich.med.mrc2.datoolbox.data.MSFeatureInfoBundle;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureIdentity;
import edu.umich.med.mrc2.datoolbox.data.ReferenceMsMsLibrary;
import edu.umich.med.mrc2.datoolbox.data.compare.CompoundIdentityComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.MsFeatureIdentityComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.ReferenceMsMsLibraryComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundIdentityField;
import edu.umich.med.mrc2.datoolbox.data.format.CompoundIdentityFormat;
import edu.umich.med.mrc2.datoolbox.data.format.MsFeatureIdentityFormat;
import edu.umich.med.mrc2.datoolbox.data.format.ReferenceMsMsLibraryFormat;
import edu.umich.med.mrc2.datoolbox.gui.coderazzi.filters.gui.AutoChoices;
import edu.umich.med.mrc2.datoolbox.gui.coderazzi.filters.gui.TableFilterHeader;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.editors.RadioButtonEditor;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.CompoundIdentityDatabaseLinkRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.FormattedDecimalRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.IdentityWordWrapCellRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.RadioButtonRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.ReferenceMsMsLibraryRenderer;

public class MSMSLibraryMatchTable extends BasicTable {

	/**
	 *
	 */
	private static final long serialVersionUID = -8853294038580302429L;
	private MSMSLibraryMatchTableModel model;
	private MouseMotionAdapter mma;
	private MSFeatureInfoBundle selectedMsMsFeatureBundle;
	private MSMSLibMatchTableModelListener msmsMatchModelListener;

	public MSMSLibraryMatchTable() {
		super();
		model = new MSMSLibraryMatchTableModel();
		setModel(model);
		rowSorter = new TableRowSorter<MSMSLibraryMatchTableModel>(model);
		setRowSorter(rowSorter);
		rowSorter.setComparator(model.getColumnIndex(MSMSLibraryMatchTableModel.MS_FEATURE_ID_COLUMN),
				new MsFeatureIdentityComparator(SortProperty.Name));
		rowSorter.setComparator(model.getColumnIndex(MSMSLibraryMatchTableModel.DATABASE_LINK_COLUMN),
				new CompoundIdentityComparator(SortProperty.ID));
		rowSorter.setComparator(model.getColumnIndex(MSMSLibraryMatchTableModel.MSMS_LIB_COLUMN),
				new ReferenceMsMsLibraryComparator(SortProperty.Name));
		
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		//	Renderers
		setDefaultRenderer(ReferenceMsMsLibrary.class, new ReferenceMsMsLibraryRenderer(SortProperty.Name));
		columnModel.getColumnById(MSMSLibraryMatchTableModel.DEFAULT_ID_COLUMN)
			.setCellRenderer(new RadioButtonRenderer()); // Primary identification
		columnModel.getColumnById(MSMSLibraryMatchTableModel.DEFAULT_ID_COLUMN)
			.setCellEditor(new RadioButtonEditor(new JCheckBox()));

		columnModel.getColumnById(MSMSLibraryMatchTableModel.NEUTRAL_MASS_COLUMN)
			.setCellRenderer(mzRenderer); // Neutral mass
		columnModel.getColumnById(MSMSLibraryMatchTableModel.PARENT_MZ_COLUMN)
			.setCellRenderer(mzRenderer); // Parent mass

		FormattedDecimalRenderer scoreRenderer = new FormattedDecimalRenderer(new DecimalFormat("#.##"), true);
		columnModel.getColumnById(MSMSLibraryMatchTableModel.COLLISION_ENERGY_COLUMN)
			.setCellRenderer(scoreRenderer); // Parent mass
		columnModel.getColumnById(MSMSLibraryMatchTableModel.LIB_SCORE_COLUMN)
			.setCellRenderer(scoreRenderer); // Parent mass
		columnModel.getColumnById(MSMSLibraryMatchTableModel.FWD_SCORE_COLUMN)
			.setCellRenderer(scoreRenderer); // Parent mass
		columnModel.getColumnById(MSMSLibraryMatchTableModel.REV_SCORE_COLUMN)
			.setCellRenderer(scoreRenderer); // Parent mass
		columnModel.getColumnById(MSMSLibraryMatchTableModel.PROBABILITY_COLUMN)
			.setCellRenderer(scoreRenderer); // Parent mass
		columnModel.getColumnById(MSMSLibraryMatchTableModel.DOT_PRODUCT_COLUMN)
			.setCellRenderer(scoreRenderer); // Parent mass

		msfIdRenderer = new CompoundIdentityDatabaseLinkRenderer();
		setDefaultRenderer(MsFeatureIdentity.class,  new IdentityWordWrapCellRenderer(CompoundIdentityField.NAME));
		setDefaultRenderer(CompoundIdentity.class, msfIdRenderer);

		//	Database link adapter
		mma = new MouseMotionAdapter() {

			public void mouseMoved(MouseEvent e) {

				Point p = e.getPoint();

				if(columnModel.isColumnVisible(columnModel.getColumnById(MSMSLibraryMatchTableModel.DATABASE_LINK_COLUMN))) {

					if (columnAtPoint(p) == columnModel.getColumnIndex(MSMSLibraryMatchTableModel.DATABASE_LINK_COLUMN))
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

		//	Table header filter
		thf = new TableFilterHeader(this, AutoChoices.ENABLED);
		thf.getParserModel().setFormat(MsFeatureIdentity.class, new MsFeatureIdentityFormat(CompoundIdentityField.DB_ID));
		thf.getParserModel().setComparator(MsFeatureIdentity.class, new MsFeatureIdentityComparator(SortProperty.ID));
		thf.getParserModel().setComparator(CompoundIdentity.class, new CompoundIdentityComparator(SortProperty.ID));
		thf.getParserModel().setFormat(CompoundIdentity.class, new CompoundIdentityFormat(CompoundIdentityField.DB_ID));
		thf.getParserModel().setFormat(ReferenceMsMsLibrary.class, new ReferenceMsMsLibraryFormat(SortProperty.Name));
		thf.getParserModel().setComparator(ReferenceMsMsLibrary.class, new ReferenceMsMsLibraryComparator(SortProperty.Name));

		finalizeLayout();
	}

	public void setTableModelFromFeatureBundle(MSFeatureInfoBundle selectedMsMsFeatureBundle) {

		if(msmsMatchModelListener != null)
			model.removeTableModelListener(msmsMatchModelListener);
		
		this.selectedMsMsFeatureBundle = selectedMsMsFeatureBundle;
		thf.setTable(null);
		model.setTableModelFromFeatureBundle(selectedMsMsFeatureBundle);
		thf.setTable(this);
		tca.adjustColumns();
		
		if(msmsMatchModelListener != null)
			model.addTableModelListener(msmsMatchModelListener);
	}

	public MsFeatureIdentity getSelectedIdentity() {

		int row = getSelectedRow();
		if(row == -1)
			return null;

		return (MsFeatureIdentity)model.getValueAt(convertRowIndexToModel(row),
				model.getColumnIndex(MSMSLibraryMatchTableModel.MS_FEATURE_ID_COLUMN));
	}

	public Collection<MsFeatureIdentity> getSelectedIdentities() {

		Collection<MsFeatureIdentity>bundles = new ArrayList<MsFeatureIdentity>();
		if(getSelectedRowCount() == 0)
			return bundles;
		
		int colIdx = model.getColumnIndex(MSMSLibraryMatchTableModel.MS_FEATURE_ID_COLUMN);
		for(int row : getSelectedRows())
			 bundles.add((MsFeatureIdentity)model.getValueAt(convertRowIndexToModel(row), colIdx));

		return bundles;
	}

	@Override
	public synchronized void clearTable() {

		if(msmsMatchModelListener != null)
			model.removeTableModelListener(msmsMatchModelListener);
		
		super.clearTable();
		
		if(msmsMatchModelListener != null)
			model.addTableModelListener(msmsMatchModelListener);
		
		selectedMsMsFeatureBundle = null;
	}

	/**
	 * @param msmsMatchModelListener the msmsMatchModelListener to set
	 */
	public void setMsmsMatchModelListener(MSMSLibMatchTableModelListener msmsMatchModelListener) {
		this.msmsMatchModelListener = msmsMatchModelListener;
		model.addTableModelListener(this.msmsMatchModelListener);
	}

	public void selectPrimaryIdentity() {

		clearSelection();
		int primaryCol = model.getColumnIndex(MSMSLibraryMatchTableModel.DEFAULT_ID_COLUMN);
		for(int i=0; i<getRowCount(); i++) {
			
			if((boolean)model.getValueAt(convertRowIndexToModel(i), primaryCol)) {
				setRowSelectionInterval(i, i);
				break;
			}
		}
	}
}





















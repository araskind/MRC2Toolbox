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

package edu.umich.med.mrc2.datoolbox.gui.idtable;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.JCheckBox;
import javax.swing.ListSelectionModel;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableRowSorter;

import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureIdentity;
import edu.umich.med.mrc2.datoolbox.data.compare.MsFeatureIdentityComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundIdSource;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundIdentityField;
import edu.umich.med.mrc2.datoolbox.data.format.MsFeatureIdentityFormat;
import edu.umich.med.mrc2.datoolbox.gui.idtable.uni.IDTrackerIdentificationTableModelListener;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.editors.RadioButtonEditor;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.AutoChoices;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.TableFilterHeader;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.AdductRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.CompoundIdentityDatabaseLinkRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.FormattedDecimalRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.RadioButtonRenderer;

public class IdentificationResultsTable extends BasicTable {

	/**
	 *
	 */
	private static final long serialVersionUID = 4342547780389381013L;
	private IdentificationResultsTableModel model;
	private FormattedDecimalRenderer ppmRenderer, scoreRenderer;
	private MsFeature parentFeature;
	private IdTableModelListener modelListener;
	private MouseMotionAdapter mma;
	private IDTrackerIdentificationTableModelListener identificationTableModelListener;

	public IdentificationResultsTable() {

		super();

		model = new IdentificationResultsTableModel();
		setModel(model);
		modelListener = new IdTableModelListener();
		model.addTableModelListener(modelListener);
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		rowSorter = new TableRowSorter<IdentificationResultsTableModel>(model);
		setRowSorter(rowSorter);
		rowSorter.setComparator(model.getColumnIndex(IdentificationResultsTableModel.COMPOUND_ID_COLUMN),
				new MsFeatureIdentityComparator(SortProperty.ID));

//		radioRenderer = new RadioButtonRenderer();
//		radioEditor = new RadioButtonEditor(new JCheckBox());
//		radioEditor.addCellEditorListener(this);

		msfIdRenderer = new CompoundIdentityDatabaseLinkRenderer();
		ppmRenderer = new FormattedDecimalRenderer(new DecimalFormat("###.#"), true);
		scoreRenderer = new FormattedDecimalRenderer(new DecimalFormat("###.##"), true);
		chmodRenderer = new AdductRenderer();

		columnModel.getColumnById(IdentificationResultsTableModel.DEFAULT_ID_COLUMN)
				.setCellRenderer(new RadioButtonRenderer()); // Primary identification
		columnModel.getColumnById(IdentificationResultsTableModel.DEFAULT_ID_COLUMN)
				.setCellEditor(new RadioButtonEditor(new JCheckBox()));
		columnModel.getColumnById(IdentificationResultsTableModel.QC_COLUMN)
			.setCellRenderer(new RadioButtonRenderer());
		columnModel.getColumnById(IdentificationResultsTableModel.COMPOUND_ID_COLUMN)
			.setCellRenderer(msfIdRenderer);
		columnModel.getColumnById(IdentificationResultsTableModel.NEUTRAL_MASS_COLUMN)
				.setCellRenderer(mzRenderer);
		columnModel.getColumnById(IdentificationResultsTableModel.MASS_ERROR_COLUMN)
				.setCellRenderer(ppmRenderer);
		columnModel.getColumnById(IdentificationResultsTableModel.ID_SCORE_COLUMN)
				.setCellRenderer(scoreRenderer);
		columnModel.getColumnById(IdentificationResultsTableModel.EXPECTED_RETENTION_COLUMN)
				.setCellRenderer(rtRenderer);
		columnModel.getColumnById(IdentificationResultsTableModel.RETENTION_ERROR_COLUMN)
				.setCellRenderer(rtRenderer);
		columnModel.getColumnById(IdentificationResultsTableModel.BEST_MATCH_ADDUCT_COLUMN)
			.setCellRenderer(chmodRenderer);

		//	Database link adapter
		mma = new MouseMotionAdapter() {

			public void mouseMoved(MouseEvent e) {

				Point p = e.getPoint();

				if(columnModel.isColumnVisible(columnModel.getColumnById(IdentificationResultsTableModel.COMPOUND_ID_COLUMN))) {

					if (columnAtPoint(p) == columnModel.getColumnIndex(IdentificationResultsTableModel.COMPOUND_ID_COLUMN))
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

	public MsFeatureIdentity getFeatureIdAtPopup() {

		if(popupRow == -1)
			return null;
		
		return (MsFeatureIdentity) model.getValueAt(convertRowIndexToModel(popupRow), 
				model.getColumnIndex(IdentificationResultsTableModel.COMPOUND_ID_COLUMN));
	}

	private class IdTableModelListener implements TableModelListener {

		public void tableChanged(TableModelEvent e) {

			int row = e.getFirstRow();
			int col = e.getColumn();
			if (col == model.getColumnIndex(IdentificationResultsTableModel.DEFAULT_ID_COLUMN)) {

				MsFeatureIdentity selectedId = (MsFeatureIdentity) model.getValueAt(row,
						model.getColumnIndex(IdentificationResultsTableModel.COMPOUND_ID_COLUMN));

				if(!selectedId.equals(parentFeature.getPrimaryIdentity())) {

					parentFeature.setPrimaryIdentity(selectedId);
					setModelFromMsFeature(parentFeature);
					setRowSelectionInterval(row, row);
				}
			}
		}
	}

	@Override
	public synchronized void clearTable() {

		thf.setTable(null);
		removeModelListeners();
		super.clearTable();		
		parentFeature = null;
		addModelListeners();
		thf.setTable(this);
	}

	public void setModelFromMsFeature(MsFeature feature) {

		thf.setTable(null);
		removeModelListeners();
		parentFeature = feature;
		model.setModelFromFeature(parentFeature);
		thf.setTable(this);
		tca.adjustColumns();
		addModelListeners();
		
	}
	
	public void setModelFromMsFeature(MsFeature feature, Collection<CompoundIdSource>sorcesToExclude) {
		
		thf.setTable(null);
		Set<MsFeatureIdentity> idList = feature.getIdentifications();
		if(sorcesToExclude != null)
			idList = feature.getIdentifications().stream().
			filter(id -> !sorcesToExclude.contains(id.getIdSource())).collect(Collectors.toSet());
			
		removeModelListeners();
		parentFeature = feature;
		model.setParentFeature(parentFeature);
		model.setModelFromIdList(idList, parentFeature.getPrimaryIdentity());
		thf.setTable(this);
		tca.adjustColumns();		
		addModelListeners();
	}
	
	private void removeModelListeners() {
		
		if(modelListener != null)
			model.removeTableModelListener(modelListener);
			
		if(identificationTableModelListener != null)
			model.removeTableModelListener(identificationTableModelListener);
	}
	
	private void addModelListeners() {
		
		if(modelListener != null)
			model.addTableModelListener(modelListener);
			
		if(identificationTableModelListener != null)
			model.addTableModelListener(identificationTableModelListener);
	}

	public MsFeatureIdentity getSelectedIdentity() {
		
		int row = getSelectedRow();
		if(row == -1)
			return null;

		return (MsFeatureIdentity) model.getValueAt(convertRowIndexToModel(row), 
				model.getColumnIndex(IdentificationResultsTableModel.COMPOUND_ID_COLUMN));
	}

	/**
	 * @param identificationTableModelListener the identificationTableModelListener to set
	 */
	public void setIdentificationTableModelListener(
			IDTrackerIdentificationTableModelListener identificationTableModelListener) {
		
		model.removeTableModelListener(modelListener);
		this.identificationTableModelListener = identificationTableModelListener;
		model.addTableModelListener(this.identificationTableModelListener);
	}

	public void selectPrimaryIdentity() {

		clearSelection();
		int primaryCol = getColumnIndex(IdentificationResultsTableModel.DEFAULT_ID_COLUMN);
		for(int i=0; i<getRowCount(); i++) {
			if((boolean)getValueAt(i, primaryCol)) {
				setRowSelectionInterval(i, i);
				break;
			}
		}
	}
}






















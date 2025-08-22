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

package edu.umich.med.mrc2.datoolbox.gui.library;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.Collection;
import java.util.stream.Collectors;

import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.table.TableRowSorter;

import edu.umich.med.mrc2.datoolbox.data.CompoundLibrary;
import edu.umich.med.mrc2.datoolbox.data.LibraryMsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureIdentity;
import edu.umich.med.mrc2.datoolbox.data.compare.CompoundIdentificationConfidenceComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.MsFeatureComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.MsFeatureIdentityComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundIdentificationConfidence;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundIdentityField;
import edu.umich.med.mrc2.datoolbox.data.format.CompoundIdentificationConfidenceFormat;
import edu.umich.med.mrc2.datoolbox.data.format.MsFeatureFormat;
import edu.umich.med.mrc2.datoolbox.data.format.MsFeatureIdentityFormat;
import edu.umich.med.mrc2.datoolbox.database.idt.MSRTLibraryUtils;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.AutoChoices;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.TableFilterHeader;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.CompoundIdConfidenceRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.CompoundIdentityDatabaseLinkRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.MsFeatureRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.RadioButtonRenderer;

public class LibraryFeatureTable extends BasicTable {

	/**
	 *
	 */
	private static final long serialVersionUID = -1796705932786773010L;

	private CompoundIdConfidenceRenderer compoundIdConfidenceRenderer;

	public LibraryFeatureTable() {

		super();
		model = new LibraryFeatureTableModel();
		setModel(model);
		rowSorter = new TableRowSorter<LibraryFeatureTableModel>(
				(LibraryFeatureTableModel)model);
		setRowSorter(rowSorter);
		rowSorter.setComparator(model.getColumnIndex(LibraryFeatureTableModel.FEATURE_COLUMN),
				new MsFeatureComparator(SortProperty.Name));
		rowSorter.setComparator(model.getColumnIndex(LibraryFeatureTableModel.ID_COLUMN),
				new MsFeatureIdentityComparator(SortProperty.ID));
		rowSorter.setComparator(model.getColumnIndex(LibraryFeatureTableModel.ID_CONFIDENCE_COLUMN),
				new CompoundIdentificationConfidenceComparator());

		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		radioRenderer = new RadioButtonRenderer();
		compoundIdConfidenceRenderer = new CompoundIdConfidenceRenderer();
		cfRenderer = new MsFeatureRenderer(SortProperty.Name);
		msfIdRenderer = new CompoundIdentityDatabaseLinkRenderer();

		columnModel.getColumnById(LibraryFeatureTableModel.MS_COLUMN).setCellRenderer(radioRenderer);
		columnModel.getColumnById(LibraryFeatureTableModel.MSMS_COLUMN).setCellRenderer(radioRenderer);
		columnModel.getColumnById(LibraryFeatureTableModel.ANNOTATIONS_COLUMN).setCellRenderer(radioRenderer);
		columnModel.getColumnById(LibraryFeatureTableModel.ID_CONFIDENCE_COLUMN).setCellRenderer(compoundIdConfidenceRenderer);
		columnModel.getColumnById(LibraryFeatureTableModel.FEATURE_COLUMN).setCellRenderer(cfRenderer);
		columnModel.getColumnById(LibraryFeatureTableModel.ID_COLUMN).setCellRenderer(msfIdRenderer);
		columnModel.getColumnById(LibraryFeatureTableModel.RT_COLUMN).setCellRenderer(rtRenderer); // Retention time
		columnModel.getColumnById(LibraryFeatureTableModel.MASS_COLUMN).setCellRenderer(mzRenderer); // Neutral mass

		//	Database link adapter
		MouseMotionAdapter mma = new MouseMotionAdapter() {

			public void mouseMoved(MouseEvent e) {

				Point p = e.getPoint();

				if(columnModel.isColumnVisible(columnModel.getColumnById(LibraryFeatureTableModel.ID_COLUMN))) {

					if (columnAtPoint(p) == columnModel.getColumnIndex(LibraryFeatureTableModel.ID_COLUMN))
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
		thf.getParserModel().setFormat(MsFeature.class, new MsFeatureFormat(SortProperty.Name));
		thf.getParserModel().setComparator(MsFeature.class, new MsFeatureComparator(SortProperty.Name));
		thf.getParserModel().setFormat(CompoundIdentificationConfidence.class, new CompoundIdentificationConfidenceFormat());
		thf.getParserModel().setComparator(CompoundIdentificationConfidence.class, new CompoundIdentificationConfidenceComparator());
		finalizeLayout();
	}

	public void setTableModelFromCompoundLibrary(CompoundLibrary library) {

		Collection<LibraryMsFeature> features =	library.getFeatures().stream()
	    	.filter(LibraryMsFeature.class::isInstance)
	    	.map(LibraryMsFeature.class::cast)
	    	.sorted(new MsFeatureComparator(SortProperty.Name)).collect(Collectors.toList());

		thf.setTable(null);
		((LibraryFeatureTableModel)model).setTableModelFromFeatureList(features);
		thf.setTable(this);
		adjustColumns();
	}

	@Override
	public Object getValueAt(int row, int column) {
		
		if(convertColumnIndexToModel(column) == 
				model.getColumnIndex(LibraryFeatureTableModel.ORDER_COLUMN))
			return row+1;
		else
			return super.getValueAt(row, column);
	}
	
	public int getFeatureRow(LibraryMsFeature feature) {

		int fCol = model.getColumnIndex(LibraryFeatureTableModel.FEATURE_COLUMN);
		for(int i=0; i<model.getRowCount(); i++) {

			if(feature.equals((LibraryMsFeature)model.getValueAt(i, fCol)))
				return convertRowIndexToView(i);
		}
		return -1;
	}

	public LibraryMsFeature getSelectedFeature() {

		int row = getSelectedRow();
		if(row == -1)
			return null;
			
		return (LibraryMsFeature) model.getValueAt(
				convertRowIndexToModel(row), 
				model.getColumnIndex(LibraryFeatureTableModel.FEATURE_COLUMN));		
	}

	public LibraryMsFeature getFeatureAtPopup() {

		if(popupRow == -1)
			return null;

		return (LibraryMsFeature) model.getValueAt(convertRowIndexToModel(popupRow), 
				model.getColumnIndex(LibraryFeatureTableModel.FEATURE_COLUMN));
	}

	//	TODO replace with table model listener
	@Override
	public void editingStopped(ChangeEvent event) {

		super.editingStopped(event);
		
		if(getSelectedRow() > -1 && ((DefaultCellEditor)event.getSource()).getComponent() instanceof JCheckBox) {

			int targetCol = model.getColumnIndex(LibraryFeatureTableModel.FEATURE_COLUMN);
			int enabledColumn  = model.getColumnIndex(LibraryFeatureTableModel.ENABLED_COLUMN);
			int qcColumn  = model.getColumnIndex(LibraryFeatureTableModel.QC_COLUMN);
			int row = convertRowIndexToModel(getSelectedRow());
			int col = convertColumnIndexToModel(getSelectedColumn());
			
			LibraryMsFeature selectedTarget = (LibraryMsFeature) model.getValueAt(row, targetCol);
			if(col == enabledColumn) {

				boolean isEnabled = (boolean) model.getValueAt(row, enabledColumn);
				selectedTarget.setActive(isEnabled);
				try {
					MSRTLibraryUtils.setTargetEnabled(
							selectedTarget.getId(),
							selectedTarget.isActive());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if(col == qcColumn) {

				boolean isQc = (boolean) model.getValueAt(row, qcColumn);
				selectedTarget.getPrimaryIdentity().setQcStandard(isQc);
				try {
					MSRTLibraryUtils.setTargetQcStatus(
							selectedTarget.getId(), 
							selectedTarget.isQcStandard());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
}



































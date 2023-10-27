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

package edu.umich.med.mrc2.datoolbox.gui.idtable.uni;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.stream.Collectors;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.InputMap;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.ListSelectionModel;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableRowSorter;

import edu.umich.med.mrc2.datoolbox.data.Adduct;
import edu.umich.med.mrc2.datoolbox.data.CompoundIdentity;
import edu.umich.med.mrc2.datoolbox.data.MSFeatureIdentificationLevel;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureIdentity;
import edu.umich.med.mrc2.datoolbox.data.ReferenceMsMsLibrary;
import edu.umich.med.mrc2.datoolbox.data.ReferenceMsMsLibraryMatch;
import edu.umich.med.mrc2.datoolbox.data.compare.AdductComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.CompoundIdentityComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.MsFeatureIdentityComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.ReferenceMsMsLibraryComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.ReferenceMsMsLibraryMatchTypeComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundIdSource;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundIdentityField;
import edu.umich.med.mrc2.datoolbox.data.format.AdductFormat;
import edu.umich.med.mrc2.datoolbox.data.format.CompoundIdentityFormat;
import edu.umich.med.mrc2.datoolbox.data.format.MsFeatureIdentityFormat;
import edu.umich.med.mrc2.datoolbox.data.format.ReferenceMsMsLibraryFormat;
import edu.umich.med.mrc2.datoolbox.data.format.ReferenceMsMsLibraryMatchTypeFormat;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCache;
import edu.umich.med.mrc2.datoolbox.gui.idworks.idlevel.IdLevelIcon;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.editors.RadioButtonEditor;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.AutoChoices;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.TableFilterHeader;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.AdductRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.ColorCircleFlagRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.CompoundIdentityDatabaseLinkRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.FormattedDecimalRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.IdentityWordWrapCellRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.MSFeatureIdentificationLevelColorRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.RadioButtonRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.ReferenceMsMsLibraryRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.WordWrapCellRenderer;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.utils.IdentificationUtils;

public class UniversalIdentificationResultsTable extends BasicTable {

	/**
	 *
	 */
	private static final long serialVersionUID = 4342547780389381013L;

	private FormattedDecimalRenderer scoreRenderer;
	private MsFeature parentFeature;
	private TableModelListener identificationTableModelListener;

	public UniversalIdentificationResultsTable() {

		super();
		this.putClientProperty(
				MRC2ToolBoxCore.COMPONENT_IDENTIFIER, this.getClass().getSimpleName());
		
		model = new UniversalIdentificationResultsTableModel();
		setModel(model);
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		rowSorter = new TableRowSorter<UniversalIdentificationResultsTableModel>(
				(UniversalIdentificationResultsTableModel)model);
		setRowSorter(rowSorter);

		rowSorter.setComparator(model.getColumnIndex(UniversalIdentificationResultsTableModel.IDENTIFICATION_COLUMN),
				new MsFeatureIdentityComparator(SortProperty.ID));
		rowSorter.setComparator(model.getColumnIndex(UniversalIdentificationResultsTableModel.COMPOUND_ID_COLUMN),
				new CompoundIdentityComparator(SortProperty.ID));
		rowSorter.setComparator(model.getColumnIndex(UniversalIdentificationResultsTableModel.BEST_MATCH_ADDUCT_COLUMN),
				new AdductComparator(SortProperty.Name));
		rowSorter.setComparator(model.getColumnIndex(UniversalIdentificationResultsTableModel.MSMS_MATCH_TYPE_COLUMN),
				new ReferenceMsMsLibraryMatchTypeComparator());		
		
		scoreRenderer = new FormattedDecimalRenderer(new DecimalFormat("###.##"), true);
		chmodRenderer = new AdductRenderer();
		msfIdRenderer = new CompoundIdentityDatabaseLinkRenderer();
		
		columnModel.getColumnById(UniversalIdentificationResultsTableModel.DEFAULT_ID_COLUMN)
				.setCellRenderer(new RadioButtonRenderer()); // Primary identification
		columnModel.getColumnById(UniversalIdentificationResultsTableModel.DEFAULT_ID_COLUMN)
				.setCellEditor(new RadioButtonEditor(new JCheckBox()));	
		columnModel.getColumnById(UniversalIdentificationResultsTableModel.ID_LEVEL_COLUMN)
			.setCellRenderer(new MSFeatureIdentificationLevelColorRenderer());		
		columnModel.getColumnById(UniversalIdentificationResultsTableModel.IDENTIFICATION_COLUMN)
			.setCellRenderer(new IdentityWordWrapCellRenderer(CompoundIdentityField.NAME));		
		columnModel.getColumnById(UniversalIdentificationResultsTableModel.COMPOUND_ID_COLUMN)
			.setCellRenderer(msfIdRenderer);
		columnModel.getColumnById(UniversalIdentificationResultsTableModel.NEUTRAL_MASS_COLUMN)
				.setCellRenderer(mzRenderer);	
		columnModel.getColumnById(UniversalIdentificationResultsTableModel.PARENT_MZ_COLUMN)
				.setCellRenderer(mzRenderer);		
		columnModel.getColumnById(UniversalIdentificationResultsTableModel.MASS_ERROR_COLUMN)
				.setCellRenderer(ppmRenderer);
		columnModel.getColumnById(UniversalIdentificationResultsTableModel.ID_SCORE_COLUMN)
				.setCellRenderer(scoreRenderer);
		columnModel.getColumnById(UniversalIdentificationResultsTableModel.RETENTION_ERROR_COLUMN)
				.setCellRenderer(rtRenderer);
		columnModel.getColumnById(UniversalIdentificationResultsTableModel.BEST_MATCH_ADDUCT_COLUMN)
				.setCellRenderer(chmodRenderer);
		columnModel.getColumnById(UniversalIdentificationResultsTableModel.QC_COLUMN)
				.setCellRenderer(new RadioButtonRenderer());
		columnModel.getColumnById(UniversalIdentificationResultsTableModel.MSRT_LIB_COLUMN)
				.setCellRenderer(new WordWrapCellRenderer()); 
		columnModel.getColumnById(UniversalIdentificationResultsTableModel.ID_SCORE_COLUMN)
				.setCellRenderer(scoreRenderer); 		
		columnModel.getColumnById(UniversalIdentificationResultsTableModel.MSMS_LIB_COLUMN)
				.setCellRenderer(new ReferenceMsMsLibraryRenderer(SortProperty.Name));
		columnModel.getColumnById(UniversalIdentificationResultsTableModel.FWD_SCORE_COLUMN)
				.setCellRenderer(scoreRenderer); 
		columnModel.getColumnById(UniversalIdentificationResultsTableModel.REV_SCORE_COLUMN)
				.setCellRenderer(scoreRenderer); 
		columnModel.getColumnById(UniversalIdentificationResultsTableModel.PROBABILITY_COLUMN)
				.setCellRenderer(scoreRenderer);
		columnModel.getColumnById(UniversalIdentificationResultsTableModel.DOT_PRODUCT_COLUMN)
				.setCellRenderer(scoreRenderer); 
		columnModel.getColumnById(UniversalIdentificationResultsTableModel.REVERSE_DOT_PRODUCT_COLUMN)
				.setCellRenderer(scoreRenderer); 
		columnModel.getColumnById(UniversalIdentificationResultsTableModel.HYBRID_DOT_PRODUCT_COLUMN)
				.setCellRenderer(scoreRenderer);
		columnModel.getColumnById(UniversalIdentificationResultsTableModel.HYBRID_SCORE_COLUMN)
				.setCellRenderer(scoreRenderer);
		columnModel.getColumnById(UniversalIdentificationResultsTableModel.SPECTRUM_ENTROPY_COLUMN)
				.setCellRenderer(scoreRenderer);
		columnModel.getColumnById(UniversalIdentificationResultsTableModel.HYBRID_DELTA_MZ_COLUMN)
				.setCellRenderer(mzRenderer); 
		columnModel.getColumnById(UniversalIdentificationResultsTableModel.MSMS_MATCH_TYPE_COLUMN)
			.setCellRenderer(new ColorCircleFlagRenderer(16)); 

		FormattedDecimalRenderer fdrRenderer = new FormattedDecimalRenderer(new DecimalFormat("###.###"), true, true);
		columnModel.getColumnById(UniversalIdentificationResultsTableModel.Q_VALUE_COLUMN)
			.setCellRenderer(fdrRenderer);
		columnModel.getColumnById(UniversalIdentificationResultsTableModel.POSTERIOR_PROBABILITY_COLUMN)
			.setCellRenderer(fdrRenderer);
		columnModel.getColumnById(UniversalIdentificationResultsTableModel.PERCOLATOR_SCORE_COLUMN)
			.setCellRenderer(scoreRenderer);
		
		columnModel.getColumnById(UniversalIdentificationResultsTableModel.DEFAULT_ID_COLUMN).setWidth(50);
		columnModel.getColumnById(UniversalIdentificationResultsTableModel.ID_LEVEL_COLUMN).setWidth(50);
		columnModel.getColumnById(UniversalIdentificationResultsTableModel.MSMS_MATCH_TYPE_COLUMN).setWidth(50);
		columnModel.getColumnById(UniversalIdentificationResultsTableModel.ID_CONFIDENCE_COLUMN).setWidth(50);
		columnModel.getColumnById(UniversalIdentificationResultsTableModel.IDENTIFICATION_COLUMN).setMinWidth(200);
		
		fixedWidthColumns.add(getColumnIndex(UniversalIdentificationResultsTableModel.DEFAULT_ID_COLUMN));
		fixedWidthColumns.add(getColumnIndex(UniversalIdentificationResultsTableModel.ID_LEVEL_COLUMN));
		fixedWidthColumns.add(getColumnIndex(UniversalIdentificationResultsTableModel.MSMS_MATCH_TYPE_COLUMN));
		fixedWidthColumns.add(getColumnIndex(UniversalIdentificationResultsTableModel.ID_CONFIDENCE_COLUMN));
		
		//	Database link adapter
		MouseMotionAdapter mma = new MouseMotionAdapter() {

			public void mouseMoved(MouseEvent e) {

				Point p = e.getPoint();
				if(columnModel.isColumnVisible(
						columnModel.getColumnById(UniversalIdentificationResultsTableModel.COMPOUND_ID_COLUMN))) {

					if (columnAtPoint(p) == columnModel.getColumnIndex(UniversalIdentificationResultsTableModel.COMPOUND_ID_COLUMN))
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
		thf.getParserModel().setFormat(MsFeatureIdentity.class, new MsFeatureIdentityFormat(CompoundIdentityField.NAME));
		thf.getParserModel().setComparator(MsFeatureIdentity.class, new MsFeatureIdentityComparator(SortProperty.Name));
		thf.getParserModel().setComparator(CompoundIdentity.class, new CompoundIdentityComparator(SortProperty.ID));
		thf.getParserModel().setFormat(CompoundIdentity.class, new CompoundIdentityFormat(CompoundIdentityField.DB_ID));
		thf.getParserModel().setFormat(ReferenceMsMsLibrary.class, new ReferenceMsMsLibraryFormat(SortProperty.Name));
		thf.getParserModel().setComparator(ReferenceMsMsLibrary.class, new ReferenceMsMsLibraryComparator(SortProperty.Name));		
		thf.getParserModel().setFormat(Adduct.class, new AdductFormat(SortProperty.Name));
		thf.getParserModel().setComparator(Adduct.class, new AdductComparator(SortProperty.Name));		
		thf.getParserModel().setFormat(ReferenceMsMsLibraryMatch.class, new ReferenceMsMsLibraryMatchTypeFormat());
		thf.getParserModel().setComparator(ReferenceMsMsLibraryMatch.class, new ReferenceMsMsLibraryMatchTypeComparator());
		

		
		finalizeLayout();
		
		createIdLevelActions();
	}

	public MsFeatureIdentity getFeatureIdAtPopup() {

		if(popupRow == -1)
			return null;
			
		return (MsFeatureIdentity) model.getValueAt(convertRowIndexToModel(popupRow), 
				model.getColumnIndex(UniversalIdentificationResultsTableModel.COMPOUND_ID_COLUMN));
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

	public void setModelFromMsFeature(
			MsFeature feature, 			
			boolean showUniqueIdsOnly) {

		thf.setTable(null);
		MsFeatureIdentity selectedId = this.getSelectedIdentity();
		Collection<MsFeatureIdentity> idList = feature.getIdentifications();
		removeModelListeners();
		parentFeature = feature;
		if(showUniqueIdsOnly)
			idList = IdentificationUtils.getBestMatchIds(feature);			
		
		((UniversalIdentificationResultsTableModel)model).setParentFeature(parentFeature);
		((UniversalIdentificationResultsTableModel)model).setModelFromIdList(idList, parentFeature.getPrimaryIdentity());	
		thf.setTable(this);
		adjustColumns();
		if(idList.contains(selectedId))
			selectIdentity(selectedId);
		else
			selectPrimaryIdentity();
		
		addModelListeners();
	}
	
//	private void adjustVarWidthColumns() {
//		
//		fixedWidthColumns.clear();
//		fixedWidthColumns.add(getColumnIndex(UniversalIdentificationResultsTableModel.DEFAULT_ID_COLUMN));
//		fixedWidthColumns.add(getColumnIndex(UniversalIdentificationResultsTableModel.ID_LEVEL_COLUMN));
//		fixedWidthColumns.add(getColumnIndex(UniversalIdentificationResultsTableModel.MSMS_MATCH_TYPE_COLUMN));
//		fixedWidthColumns.add(getColumnIndex(UniversalIdentificationResultsTableModel.ID_CONFIDENCE_COLUMN));
//		tca.adjustColumnsExcluding(fixedWidthColumns);
//		columnModel.getColumnById(UniversalIdentificationResultsTableModel.IDENTIFICATION_COLUMN).setMinWidth(200);
//	}
	
	public void setModelFromMsFeature(
			MsFeature feature, 
			Collection<CompoundIdSource> sorcesToExclude, 
			boolean showUniqueIdsOnly) {
		
		thf.setTable(null);
		MsFeatureIdentity selectedId = this.getSelectedIdentity();
		
		Collection<MsFeatureIdentity> idList = feature.getIdentifications();
		if(showUniqueIdsOnly)
			idList = IdentificationUtils.getBestMatchIds(feature);
			
		if(sorcesToExclude != null && !sorcesToExclude.isEmpty())
			idList = feature.getIdentifications().stream().
				filter(id -> !sorcesToExclude.contains(id.getIdSource())).
				collect(Collectors.toSet());
			
		removeModelListeners();
		parentFeature = feature;
		((UniversalIdentificationResultsTableModel)model).setParentFeature(parentFeature);
		((UniversalIdentificationResultsTableModel)model).setModelFromIdList(idList, parentFeature.getPrimaryIdentity());
		thf.setTable(this);
		
		if(idList.contains(selectedId))
			selectIdentity(selectedId);
		else
			selectPrimaryIdentity();
		
		adjustColumns();
		addModelListeners();
	}
	
	public void removeModelListeners() {
		
		if(identificationTableModelListener != null)
			model.removeTableModelListener(identificationTableModelListener);
	}
	
	public void addModelListeners() {
		
		if(identificationTableModelListener != null)
			model.addTableModelListener(identificationTableModelListener);
	}

	public MsFeatureIdentity getSelectedIdentity() {
		
		int row = getSelectedRow();
		if(row == -1)
			return null;

		return (MsFeatureIdentity) model.getValueAt(convertRowIndexToModel(row), 
				model.getColumnIndex(UniversalIdentificationResultsTableModel.IDENTIFICATION_COLUMN));
	}

	/**
	 * @param identificationTableModelListener the identificationTableModelListener to set
	 */
	public void setIdentificationTableModelListener(
			TableModelListener identificationTableModelListener) {
		
		this.identificationTableModelListener = identificationTableModelListener;
		model.addTableModelListener(this.identificationTableModelListener);
	}
	
	public void toggleIdentificationTableModelListener(boolean enable) {
		
		if(identificationTableModelListener != null) {
			
			if(enable)
				model.addTableModelListener(this.identificationTableModelListener);
			else
				model.removeTableModelListener(identificationTableModelListener);
		}
	}
	
	public void selectPrimaryIdentity() {

		clearSelection();
		int primaryCol = model.getColumnIndex(UniversalIdentificationResultsTableModel.DEFAULT_ID_COLUMN);
		for(int i=0; i<getRowCount(); i++) {
			if((boolean)model.getValueAt(convertRowIndexToModel(i), primaryCol)) {
				setRowSelectionInterval(i, i);
				break;
			}
		}
	}

	public void selectIdentity(MsFeatureIdentity id) {

		clearSelection();
		int idCol = model.getColumnIndex(UniversalIdentificationResultsTableModel.IDENTIFICATION_COLUMN);

		for(int i=0; i<getRowCount(); i++) {
			if(model.getValueAt(convertRowIndexToModel(i), idCol).equals(id)) {
				setRowSelectionInterval(i, i);
				scrollToSelected();
				break;
			}
		}
	}
	
	public MsFeature getParentFeature() {
		return parentFeature;
	}
	
	private void createIdLevelActions() {
		
		InputMap inputMap = this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		
	    Action idLevelAction = new AbstractAction() {
	        @Override
	        public void actionPerformed(ActionEvent e) {

	        }
	    };
	    
		for(MSFeatureIdentificationLevel level : IDTDataCache.getMsFeatureIdentificationLevelList()) {
			
			Icon levelIcon = new IdLevelIcon(24, level.getColorCode());
			//	GuiUtils.addMenuItem(idLevelMenu, level.getName(), listener, level.getName(), levelIcon);
		}
	}	
}






















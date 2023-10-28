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

package edu.umich.med.mrc2.datoolbox.gui.idworks.ms1;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.ListSelectionModel;
import javax.swing.table.TableRowSorter;

import edu.umich.med.mrc2.datoolbox.data.Adduct;
import edu.umich.med.mrc2.datoolbox.data.ExperimentalSample;
import edu.umich.med.mrc2.datoolbox.data.MSFeatureInfoBundle;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureIdentity;
import edu.umich.med.mrc2.datoolbox.data.compare.AdductComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.AnalysisMethodComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.ExperimentalSampleComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.LIMSExperimentComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.MsFeatureIdentityComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.MsFeatureInfoBundleComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SampleTypeComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundIdentityField;
import edu.umich.med.mrc2.datoolbox.data.enums.FeatureIdentificationState;
import edu.umich.med.mrc2.datoolbox.data.format.AdductFormat;
import edu.umich.med.mrc2.datoolbox.data.format.AnalysisMethodFormat;
import edu.umich.med.mrc2.datoolbox.data.format.ExperimentalSampleFormat;
import edu.umich.med.mrc2.datoolbox.data.format.FeatureIdentificationStateFormat;
import edu.umich.med.mrc2.datoolbox.data.format.LIMSExperimentFormat;
import edu.umich.med.mrc2.datoolbox.data.format.MsFeatureIdentityFormat;
import edu.umich.med.mrc2.datoolbox.data.format.MsFeatureInfoBundleFormat;
import edu.umich.med.mrc2.datoolbox.data.format.SampleTypeFormat;
import edu.umich.med.mrc2.datoolbox.data.lims.DataAcquisitionMethod;
import edu.umich.med.mrc2.datoolbox.data.lims.DataExtractionMethod;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSExperiment;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSSampleType;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.AutoChoices;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.TableFilterHeader;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.AdductRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.AnalysisMethodRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.CompoundIdentityDatabaseLinkRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.ExperimentalSampleRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.FeatureIdentificationStateRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.IdentificationFollowupStateRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.LIMSExperimentRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.MSFeatureIdentificationLevelColorRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.MsFeatureInfoBundleRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.RadioButtonRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.SampleTypeRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.WordWrapCellRenderer;

public class ReferenceMsOneFeatureTable extends BasicTable {

	/**
	 *
	 */
	private static final long serialVersionUID = -8853294038580302429L;
	
	public ReferenceMsOneFeatureTable() {
		super();
		model = new ReferenceMsOneFeatureTableModel();
		setModel(model);
		rowSorter = new TableRowSorter<ReferenceMsOneFeatureTableModel>(
				(ReferenceMsOneFeatureTableModel)model);
		setRowSorter(rowSorter);
		rowSorter.setComparator(model.getColumnIndex(ReferenceMsOneFeatureTableModel.MS_FEATURE_COLUMN),
				new MsFeatureInfoBundleComparator(SortProperty.Name));
		rowSorter.setComparator(model.getColumnIndex(ReferenceMsOneFeatureTableModel.DATABSE_LINK_COLUMN),
				new MsFeatureIdentityComparator(SortProperty.ID));
		rowSorter.setComparator(model.getColumnIndex(ReferenceMsOneFeatureTableModel.ACQ_METHOD_ID_COLUMN),
				new AnalysisMethodComparator(SortProperty.ID));
		rowSorter.setComparator(model.getColumnIndex(ReferenceMsOneFeatureTableModel.DEX_METHOD_ID_COLUMN),
				new AnalysisMethodComparator(SortProperty.ID));
		rowSorter.setComparator(model.getColumnIndex(ReferenceMsOneFeatureTableModel.EXPERIMENT_COLUMN),
				new LIMSExperimentComparator(SortProperty.ID));
		rowSorter.setComparator(model.getColumnIndex(ReferenceMsOneFeatureTableModel.SAMPLE_COLUMN),
				new ExperimentalSampleComparator(SortProperty.ID));
		rowSorter.setComparator(model.getColumnIndex(ReferenceMsOneFeatureTableModel.SAMPLE_TYPE_COLUMN),
				new SampleTypeComparator(SortProperty.Name));
		rowSorter.setComparator(model.getColumnIndex(ReferenceMsOneFeatureTableModel.CHEM_MOD_COLUMN),
				new AdductComparator(SortProperty.Name));
		
		setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		//	Renderers
		setDefaultRenderer(MSFeatureInfoBundle.class, new MsFeatureInfoBundleRenderer(SortProperty.Name));
		setDefaultRenderer(DataAcquisitionMethod.class, new AnalysisMethodRenderer());
		setDefaultRenderer(DataExtractionMethod.class, new AnalysisMethodRenderer());
		setDefaultRenderer(LIMSExperiment.class, new LIMSExperimentRenderer());
		setDefaultRenderer(ExperimentalSample.class, new ExperimentalSampleRenderer(SortProperty.ID));
		setDefaultRenderer(LIMSSampleType.class, new SampleTypeRenderer(SortProperty.Name));
		setDefaultRenderer(Adduct.class, new AdductRenderer());
		
		columnModel.getColumnById(ReferenceMsOneFeatureTableModel.COMPOUND_NAME_COLUMN).
			setCellRenderer(new WordWrapCellRenderer());
		msfIdRenderer = new CompoundIdentityDatabaseLinkRenderer();
		columnModel.getColumnById(ReferenceMsOneFeatureTableModel.DATABSE_LINK_COLUMN)
			.setCellRenderer(msfIdRenderer);	// Database ID column
//		columnModel.getColumnById(ReferenceMsOneFeatureTableModel.CHEM_MOD_COLUMN)
//			.setCellRenderer(new AdductRenderer()); // Compound form column
		columnModel.getColumnById(ReferenceMsOneFeatureTableModel.RETENTION_COLUMN)
			.setCellRenderer(rtRenderer); // Retention time
		columnModel.getColumnById(ReferenceMsOneFeatureTableModel.NEUTRAL_MASS_COLUMN)
			.setCellRenderer(mzRenderer); // Neutral mass
		columnModel.getColumnById(ReferenceMsOneFeatureTableModel.AMBIGUITY_COLUMN)
			.setCellRenderer(new FeatureIdentificationStateRenderer());
		columnModel.getColumnById(ReferenceMsOneFeatureTableModel.ANNOTATIONS_COLUMN)
			.setCellRenderer(new RadioButtonRenderer());
		columnModel.getColumnById(ReferenceMsOneFeatureTableModel.FOLLOWUP_COLUMN)
			.setCellRenderer(new IdentificationFollowupStateRenderer()); 
		columnModel.getColumnById(ReferenceMsOneFeatureTableModel.ID_LEVEL_COLUMN)
			.setCellRenderer(new MSFeatureIdentificationLevelColorRenderer());
		
		//	Database link adapter
		MouseMotionAdapter mma = new MouseMotionAdapter() {

			public void mouseMoved(MouseEvent e) {

				Point p = e.getPoint();

				if(columnModel.isColumnVisible(columnModel.getColumnById(ReferenceMsOneFeatureTableModel.DATABSE_LINK_COLUMN))) {

					if (columnAtPoint(p) == columnModel.getColumnIndex(ReferenceMsOneFeatureTableModel.DATABSE_LINK_COLUMN))
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
		
		columnModel.getColumnById(ReferenceMsOneFeatureTableModel.COMPOUND_NAME_COLUMN).setMinWidth(200);
		columnModel.getColumnById(ReferenceMsOneFeatureTableModel.AMBIGUITY_COLUMN).setMaxWidth(50);
		columnModel.getColumnById(ReferenceMsOneFeatureTableModel.ID_LEVEL_COLUMN).setMaxWidth(50);
		columnModel.getColumnById(ReferenceMsOneFeatureTableModel.ANNOTATIONS_COLUMN).setMaxWidth(50);
		columnModel.getColumnById(ReferenceMsOneFeatureTableModel.FOLLOWUP_COLUMN).setMaxWidth(50);
		
//		fixedWidthColumns.add(getColumnIndex(ReferenceMsOneFeatureTableModel.AMBIGUITY_COLUMN));
//		fixedWidthColumns.add(getColumnIndex(ReferenceMsOneFeatureTableModel.ID_LEVEL_COLUMN));
//		fixedWidthColumns.add(getColumnIndex(ReferenceMsOneFeatureTableModel.ANNOTATIONS_COLUMN));
//		fixedWidthColumns.add(getColumnIndex(ReferenceMsOneFeatureTableModel.FOLLOWUP_COLUMN));
		
		//	Table header filter
		thf = new TableFilterHeader(this, AutoChoices.ENABLED);
		thf.getParserModel().setFormat(MSFeatureInfoBundle.class, new MsFeatureInfoBundleFormat(SortProperty.Name));
		thf.getParserModel().setComparator(MSFeatureInfoBundle.class, new MsFeatureInfoBundleComparator(SortProperty.Name));
		thf.getParserModel().setFormat(MsFeatureIdentity.class, new MsFeatureIdentityFormat(CompoundIdentityField.DB_ID));
		thf.getParserModel().setComparator(MsFeatureIdentity.class, new MsFeatureIdentityComparator(SortProperty.ID));
		thf.getParserModel().setComparator(DataAcquisitionMethod.class, new AnalysisMethodComparator(SortProperty.ID));
		thf.getParserModel().setFormat(DataAcquisitionMethod.class, new AnalysisMethodFormat(SortProperty.ID));
		thf.getParserModel().setFormat(DataExtractionMethod.class, new AnalysisMethodFormat(SortProperty.ID));
		thf.getParserModel().setComparator(DataExtractionMethod.class, new AnalysisMethodComparator(SortProperty.ID));
		thf.getParserModel().setFormat(LIMSExperiment.class, new LIMSExperimentFormat());
		thf.getParserModel().setComparator(LIMSExperiment.class, new LIMSExperimentComparator(SortProperty.ID));
		thf.getParserModel().setFormat(ExperimentalSample.class, new ExperimentalSampleFormat(SortProperty.ID));
		thf.getParserModel().setComparator(ExperimentalSample.class, new ExperimentalSampleComparator(SortProperty.ID));
		thf.getParserModel().setFormat(LIMSSampleType.class, new SampleTypeFormat(SortProperty.Name));
		thf.getParserModel().setComparator(LIMSSampleType.class, new SampleTypeComparator(SortProperty.Name));
		thf.getParserModel().setFormat(FeatureIdentificationState.class, new FeatureIdentificationStateFormat());
		thf.getParserModel().setFormat(Adduct.class, new AdductFormat(SortProperty.Name));
		thf.getParserModel().setComparator(Adduct.class, new AdductComparator(SortProperty.Name));
		
		finalizeLayout();
	}

	public void setTableModelFromFeatureList(Collection<MSFeatureInfoBundle> selectedMsOneFeatures) {

		thf.setTable(null);
		((ReferenceMsOneFeatureTableModel)model).setTableModelFromFeatureList(selectedMsOneFeatures);
		thf.setTable(this);
		adjustColumns();
	}
	
//	private void adjustVariableColumns() {
//		
//		fixedWidthColumns.clear();
//		fixedWidthColumns.add(getColumnIndex(ReferenceMsOneFeatureTableModel.AMBIGUITY_COLUMN));
//		fixedWidthColumns.add(getColumnIndex(ReferenceMsOneFeatureTableModel.ID_LEVEL_COLUMN));
//		fixedWidthColumns.add(getColumnIndex(ReferenceMsOneFeatureTableModel.ANNOTATIONS_COLUMN));
//		fixedWidthColumns.add(getColumnIndex(ReferenceMsOneFeatureTableModel.FOLLOWUP_COLUMN));
//				
//		tca.adjustColumnsExcluding(fixedWidthColumns);
//		columnModel.getColumnById(ReferenceMsOneFeatureTableModel.COMPOUND_NAME_COLUMN).setMinWidth(200);
//	}

	public MSFeatureInfoBundle getSelectedBundle() {

		int row = getSelectedRow();
		if(row == -1)
			return null;

		return (MSFeatureInfoBundle)model.getValueAt(convertRowIndexToModel(row),
				model.getColumnIndex(ReferenceMsOneFeatureTableModel.MS_FEATURE_COLUMN));
	}
	
	public MSFeatureInfoBundle getMSFeatureInfoBundleAtPopup() {

		if(popupRow == -1)
			return null;
		else
			return (MSFeatureInfoBundle)model.getValueAt(convertRowIndexToModel(popupRow),
					model.getColumnIndex(ReferenceMsOneFeatureTableModel.MS_FEATURE_COLUMN));
	}
	
	public Collection<MSFeatureInfoBundle>getMultipleSelectedBundles() {

		Collection<MSFeatureInfoBundle>selected = new ArrayList<MSFeatureInfoBundle>();
		int col = model.getColumnIndex(ReferenceMsOneFeatureTableModel.MS_FEATURE_COLUMN);
		for(int row : getSelectedRows())
			selected.add((MSFeatureInfoBundle)model.getValueAt(convertRowIndexToModel(row), col));
		
		return selected;
	}
	
	public Collection<MSFeatureInfoBundle> getFilteredBundles() {

		Collection<MSFeatureInfoBundle>bundles = new ArrayList<MSFeatureInfoBundle>();
		int colIdx = model.getColumnIndex(ReferenceMsOneFeatureTableModel.MS_FEATURE_COLUMN);
		for(int row=0; row<getRowCount(); row++)
			 bundles.add((MSFeatureInfoBundle)model.getValueAt(convertRowIndexToModel(row), colIdx));

		return bundles;
	}
	
	public Collection<MSFeatureInfoBundle> getAllBundles() {

		Collection<MSFeatureInfoBundle>bundles = new ArrayList<MSFeatureInfoBundle>();
		int colIdx = model.getColumnIndex(ReferenceMsOneFeatureTableModel.MS_FEATURE_COLUMN);
		for(int row=0; row<model.getRowCount(); row++)
			 bundles.add((MSFeatureInfoBundle)model.getValueAt(row, colIdx));

		return bundles;
	}

	public void updateFeatureData(MSFeatureInfoBundle selectedBundle) {
		thf.setTable(null);
		((ReferenceMsOneFeatureTableModel)model).updateFeatureData(selectedBundle);
		thf.setTable(this);
		adjustColumns();
	}

	public void selectBundle(MSFeatureInfoBundle toSelect) {

		int colIdx = model.getColumnIndex(ReferenceMsOneFeatureTableModel.MS_FEATURE_COLUMN);
		
		for(int i=0; i<getRowCount(); i++) {

			if(((MSFeatureInfoBundle)model.getValueAt(convertRowIndexToModel(i), colIdx)).equals(toSelect)) {
				setRowSelectionInterval(i, i);
				scrollToSelected();
				break;
			}
		}
	}
}

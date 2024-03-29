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
import java.util.List;

import javax.swing.ListSelectionModel;
import javax.swing.table.TableRowSorter;

import edu.umich.med.mrc2.datoolbox.data.Adduct;
import edu.umich.med.mrc2.datoolbox.data.ExperimentalSample;
import edu.umich.med.mrc2.datoolbox.data.MSFeatureInfoBundle;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureIdentity;
import edu.umich.med.mrc2.datoolbox.data.ReferenceMsMsLibraryMatch;
import edu.umich.med.mrc2.datoolbox.data.compare.AdductComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.AnalysisMethodComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.ExperimentalSampleComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.LIMSExperimentComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.MsFeatureIdentityComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.MsFeatureInfoBundleComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.ReferenceMsMsLibraryMatchTypeComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SampleTypeComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundIdentityField;
import edu.umich.med.mrc2.datoolbox.data.enums.FeatureIdentificationState;
import edu.umich.med.mrc2.datoolbox.data.enums.Polarity;
import edu.umich.med.mrc2.datoolbox.data.format.AdductFormat;
import edu.umich.med.mrc2.datoolbox.data.format.AnalysisMethodFormat;
import edu.umich.med.mrc2.datoolbox.data.format.ExperimentalSampleFormat;
import edu.umich.med.mrc2.datoolbox.data.format.FeatureIdentificationStateFormat;
import edu.umich.med.mrc2.datoolbox.data.format.LIMSExperimentFormat;
import edu.umich.med.mrc2.datoolbox.data.format.MsFeatureIdentityFormat;
import edu.umich.med.mrc2.datoolbox.data.format.MsFeatureInfoBundleFormat;
import edu.umich.med.mrc2.datoolbox.data.format.ReferenceMsMsLibraryMatchTypeFormat;
import edu.umich.med.mrc2.datoolbox.data.format.SampleTypeFormat;
import edu.umich.med.mrc2.datoolbox.data.lims.DataAcquisitionMethod;
import edu.umich.med.mrc2.datoolbox.data.lims.DataExtractionMethod;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSExperiment;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSSampleType;
import edu.umich.med.mrc2.datoolbox.data.msclust.IMsFeatureInfoBundleCluster;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTableModel;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.AutoChoices;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.TableFilterHeader;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.AdductRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.AnalysisMethodRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.ColorCircleFlagRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.CompoundIdentityDatabaseLinkRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.ExperimentalSampleRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.FeatureIdentificationStateRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.FormattedDecimalRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.IdentificationFollowupStateRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.LIMSExperimentRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.MSFeatureIdentificationLevelColorRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.MsFeatureInfoBundleRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.PercentValueRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.PolarityRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.RadioButtonRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.SampleTypeRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.WordWrapCellRenderer;
import edu.umich.med.mrc2.datoolbox.gui.utils.LongTableUpdateTask;
import edu.umich.med.mrc2.datoolbox.gui.utils.TableUpdateProgressDialog;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;

public class MSMSFeatureTable extends BasicTable {

	/**
	 *
	 */
	private static final long serialVersionUID = -8853294038580302429L;

	private TableUpdateProgressDialog idp;
	private FormattedDecimalRenderer scoreRenderer;

	public MSMSFeatureTable() {
		super();
		this.putClientProperty(
				MRC2ToolBoxCore.COMPONENT_IDENTIFIER, this.getClass().getSimpleName());
		model = new MSMSFeatureTableModel(this);
		setModel(model);
		setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		rowSorter = new TableRowSorter<MSMSFeatureTableModel>((MSMSFeatureTableModel)model);
		setRowSorter(rowSorter);
		rowSorter.setComparator(model.getColumnIndex(MSMSFeatureTableModel.MS_FEATURE_COLUMN),
				new MsFeatureInfoBundleComparator(SortProperty.Name));
		rowSorter.setComparator(model.getColumnIndex(MSMSFeatureTableModel.DATABASE_LINK_COLUMN),
				new MsFeatureIdentityComparator(SortProperty.ID));
		rowSorter.setComparator(model.getColumnIndex(MSMSFeatureTableModel.ACQ_METHOD_ID_COLUMN),
				new AnalysisMethodComparator(SortProperty.ID));
		rowSorter.setComparator(model.getColumnIndex(MSMSFeatureTableModel.DEX_METHOD_ID_COLUMN),
				new AnalysisMethodComparator(SortProperty.ID));
		rowSorter.setComparator(model.getColumnIndex(MSMSFeatureTableModel.EXPERIMENT_COLUMN),
				new LIMSExperimentComparator(SortProperty.ID));
		rowSorter.setComparator(model.getColumnIndex(MSMSFeatureTableModel.SAMPLE_COLUMN),
				new ExperimentalSampleComparator(SortProperty.ID));
		rowSorter.setComparator(model.getColumnIndex(MSMSFeatureTableModel.SAMPLE_TYPE_COLUMN),
				new SampleTypeComparator(SortProperty.Name));
		rowSorter.setComparator(model.getColumnIndex(MSMSFeatureTableModel.ADDUCT_COLUMN),
				new AdductComparator(SortProperty.Name));
		rowSorter.setComparator(model.getColumnIndex(MSMSFeatureTableModel.MSMS_MATCH_TYPE_COLUMN),
				new ReferenceMsMsLibraryMatchTypeComparator());	
		
		//	Renderers
		setDefaultRenderer(MSFeatureInfoBundle.class, new MsFeatureInfoBundleRenderer(SortProperty.Name));
		setDefaultRenderer(DataAcquisitionMethod.class, new AnalysisMethodRenderer());
		setDefaultRenderer(DataExtractionMethod.class, new AnalysisMethodRenderer());
		setDefaultRenderer(LIMSExperiment.class, new LIMSExperimentRenderer());
		setDefaultRenderer(ExperimentalSample.class, new ExperimentalSampleRenderer(SortProperty.ID));
		setDefaultRenderer(LIMSSampleType.class, new SampleTypeRenderer(SortProperty.Name));
		setDefaultRenderer(Adduct.class, new AdductRenderer());
		setDefaultRenderer(Polarity.class, new PolarityRenderer());
						
		columnModel.getColumnById(MSMSFeatureTableModel.COMPOUND_NAME_COLUMN).
			setCellRenderer(new WordWrapCellRenderer());	

		msfIdRenderer = new CompoundIdentityDatabaseLinkRenderer();
		columnModel.getColumnById(MSMSFeatureTableModel.DATABASE_LINK_COLUMN)
			.setCellRenderer(msfIdRenderer);	// Database ID column
		columnModel.getColumnById(MSMSFeatureTableModel.RETENTION_COLUMN)
			.setCellRenderer(rtRenderer); // Retention time 
		columnModel.getColumnById(MSMSFeatureTableModel.PARENT_MZ_COLUMN)
			.setCellRenderer(mzRenderer); // Parent mass			
		columnModel.getColumnById(MSMSFeatureTableModel.NEUTRAL_MASS_PRECURSOR_DELTA_MZ_COLUMN)
			.setCellRenderer(mzRenderer); // Parent mass
		columnModel.getColumnById(MSMSFeatureTableModel.LIBRARY_PRECURSOR_DELTA_MZ_COLUMN)
			.setCellRenderer(mzRenderer); // Parent mass		
		columnModel.getColumnById(MSMSFeatureTableModel.AMBIGUITY_COLUMN)
			.setCellRenderer(new FeatureIdentificationStateRenderer());		
		columnModel.getColumnById(MSMSFeatureTableModel.ANNOTATIONS_COLUMN)
			.setCellRenderer(new RadioButtonRenderer());
		columnModel.getColumnById(MSMSFeatureTableModel.FOLLOWUP_COLUMN)
			.setCellRenderer(new IdentificationFollowupStateRenderer()); 
		columnModel.getColumnById(MSMSFeatureTableModel.ID_LEVEL_COLUMN)
			.setCellRenderer(new MSFeatureIdentificationLevelColorRenderer());
		columnModel.getColumnById(MSMSFeatureTableModel.PARENT_ION_PURITY_COLUMN)
			.setCellRenderer(new PercentValueRenderer());
		columnModel.getColumnById(MSMSFeatureTableModel.PARENT_ION_IS_MINOR_ISOTOPE_COLUMN)
			.setCellRenderer(new RadioButtonRenderer());
		columnModel.getColumnById(MSMSFeatureTableModel.SPECTRUM_ENTROPY_COLUMN)
			.setCellRenderer(new FormattedDecimalRenderer(new DecimalFormat("##.###")));
		columnModel.getColumnById(MSMSFeatureTableModel.SPECTRUM_TOTAL_INTENSITY_COLUMN)
			.setCellRenderer(areaRenderer);
		scoreRenderer = new FormattedDecimalRenderer(new DecimalFormat("###.##"), true);
		columnModel.getColumnById(MSMSFeatureTableModel.SPECTRUM_ENTROPY_COLUMN)
			.setCellRenderer(scoreRenderer);
		columnModel.getColumnById(MSMSFeatureTableModel.MSMS_MATCH_TYPE_COLUMN)
			.setCellRenderer(new ColorCircleFlagRenderer(16)); 

		//	Database link adapter
		MouseMotionAdapter mma = new MouseMotionAdapter() {

			public void mouseMoved(MouseEvent e) {

				Point p = e.getPoint();

				if(columnModel.isColumnVisible(columnModel.getColumnById(MSMSFeatureTableModel.DATABASE_LINK_COLUMN))) {

					if (columnAtPoint(p) == columnModel.getColumnIndex(MSMSFeatureTableModel.DATABASE_LINK_COLUMN))
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
		
		columnModel.getColumnById(MSMSFeatureTableModel.AMBIGUITY_COLUMN).setMaxWidth(50);
		columnModel.getColumnById(MSMSFeatureTableModel.ID_LEVEL_COLUMN).setMaxWidth(50);
		columnModel.getColumnById(MSMSFeatureTableModel.ANNOTATIONS_COLUMN).setMaxWidth(50);
		columnModel.getColumnById(MSMSFeatureTableModel.FOLLOWUP_COLUMN).setMaxWidth(50);	
		columnModel.getColumnById(MSMSFeatureTableModel.COMPOUND_NAME_COLUMN).setMinWidth(200);

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
		thf.getParserModel().setFormat(ReferenceMsMsLibraryMatch.class, new ReferenceMsMsLibraryMatchTypeFormat());
		thf.getParserModel().setComparator(ReferenceMsMsLibraryMatch.class, new ReferenceMsMsLibraryMatchTypeComparator());
		
		finalizeLayout();
	}
	
	public void setTableModelFromFeatureList(Collection<MSFeatureInfoBundle> featureList) {

		((MSMSFeatureTableModel)model).removeFeatureCluster();
		TableUpdateTask task = new TableUpdateTask(featureList);
		String message = "Loading data for " + 
				Integer.toString(featureList.size()) + " features, please wait ...";
		idp = new TableUpdateProgressDialog(message, this, task);
		idp.setLocationRelativeTo(this);
		idp.setVisible(true);
	}
	
	public void setTableModelFromFeatureCluster(IMsFeatureInfoBundleCluster featureCluster) {
		
		thf.setTable(null);
		((MSMSFeatureTableModel)model).setTableModelFromFeatureCluster(featureCluster);
		thf.setTable(this);
		adjustColumns();
	}
	
	class TableUpdateTask extends LongTableUpdateTask {

	    private final Collection<MSFeatureInfoBundle> featureList;

	    public TableUpdateTask(Collection<MSFeatureInfoBundle> featureList) {
	        this.featureList = featureList;	       
	    }

		@Override
		public BasicTableModel doInBackground() throws Exception {

			thf.setTable(null);
			model.setRowCount(0);
			List<Object[]> modelData = ((MSMSFeatureTableModel)model).createModelData(featureList);
			Thread.sleep(200);
			for (int index = 0; index < modelData.size(); index++) {

				Object[] data = modelData.get(index);
				try {
					publish(data);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Thread.yield();
			}
			return model;
		}

		@Override
		protected void process(List<Object[]> chunks) {
			try {
				model.addRows(chunks);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		@Override
		public void done() {
			try {
				thf.setTable(MSMSFeatureTable.this);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			adjustColumns();
			idp.dispose();
		}
	}

	public MSFeatureInfoBundle getSelectedBundle() {

		int row = getSelectedRow();
		if(row == -1)
			return null;

		return (MSFeatureInfoBundle)model.getValueAt(convertRowIndexToModel(row),
				model.getColumnIndex(MSMSFeatureTableModel.MS_FEATURE_COLUMN));
	}
	
	public MSFeatureInfoBundle getMSFeatureInfoBundleAtPopup() {

		if(popupRow == -1)
			return null;
		else
			return (MSFeatureInfoBundle)model.getValueAt(convertRowIndexToModel(popupRow),
					model.getColumnIndex(MSMSFeatureTableModel.MS_FEATURE_COLUMN));
	}

	public Collection<MSFeatureInfoBundle> getMultipleSelectedBundles() {

		Collection<MSFeatureInfoBundle>bundles = new ArrayList<MSFeatureInfoBundle>();
		if(getSelectedRowCount() == 0)
			return bundles;

		int colIdx = model.getColumnIndex(MSMSFeatureTableModel.MS_FEATURE_COLUMN);
		for(int row : getSelectedRows())
			 bundles.add((MSFeatureInfoBundle)model.getValueAt(convertRowIndexToModel(row), colIdx));

		return bundles;
	}

	public Collection<MSFeatureInfoBundle> getFilteredBundles() {

		Collection<MSFeatureInfoBundle>bundles = new ArrayList<MSFeatureInfoBundle>();
		int colIdx = model.getColumnIndex(MSMSFeatureTableModel.MS_FEATURE_COLUMN);
		for(int row=0; row<getRowCount(); row++)
			 bundles.add((MSFeatureInfoBundle)model.getValueAt(convertRowIndexToModel(row), colIdx));

		return bundles;
	}
	
	public Collection<MSFeatureInfoBundle> getAllBundles() {

		Collection<MSFeatureInfoBundle>bundles = new ArrayList<MSFeatureInfoBundle>();
		int colIdx = model.getColumnIndex(MSMSFeatureTableModel.MS_FEATURE_COLUMN);
		for(int row=0; row<model.getRowCount(); row++)
			 bundles.add((MSFeatureInfoBundle)model.getValueAt(row, colIdx));

		return bundles;
	}

	public void updateFeatureData(Collection<MSFeatureInfoBundle> bundlesToUpdate) {
		((MSMSFeatureTableModel)model).updateFeatureData(bundlesToUpdate);
	}

	public void selectBundle(MSFeatureInfoBundle toSelect) {

		int colIdx = model.getColumnIndex(MSMSFeatureTableModel.MS_FEATURE_COLUMN);
		
		for(int i=0; i<getRowCount(); i++) {

			if(((MSFeatureInfoBundle)model.getValueAt(
					convertRowIndexToModel(i), colIdx)).equals(toSelect)) {
				setRowSelectionInterval(i, i);
				scrollToSelected();
				break;
			}
		}
	}
}





















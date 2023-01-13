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

package edu.umich.med.mrc2.datoolbox.gui.fdata;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.TableRowSorter;

import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureCluster;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureIdentity;
import edu.umich.med.mrc2.datoolbox.data.compare.MsFeatureComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.MsFeatureIdentityComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundIdentityField;
import edu.umich.med.mrc2.datoolbox.data.format.MsFeatureFormat;
import edu.umich.med.mrc2.datoolbox.data.format.MsFeatureIdentityFormat;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicFeatureTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.TableColumnAdjuster;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.AutoChoices;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.TableFilterHeader;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.ChemicalModificationRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.CompoundIdentityDatabaseLinkRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.McMillanDeltaPercentColorRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.PieChartFrequencyRenderer;

public class FeatureDataTable extends BasicFeatureTable {

	/**
	 *
	 */
	private static final long serialVersionUID = 7633347730576608261L;

	private FeatureDataTableModel model;
	private TableRowSorter<FeatureDataTableModel> featureSorter;
	private MouseMotionAdapter mma;
	private PieChartFrequencyRenderer pieChartFrequencyRenderer;

	public FeatureDataTable() {

		super();
		model = new FeatureDataTableModel();
		setModel(model);
		setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		//	Row sorters
		featureSorter = new TableRowSorter<FeatureDataTableModel>(model);
		setRowSorter(featureSorter);
		featureSorter.setComparator(model.getColumnIndex(FeatureDataTableModel.MS_FEATURE_COLUMN),
				new MsFeatureComparator(SortProperty.Name));
		featureSorter.setComparator(model.getColumnIndex(FeatureDataTableModel.DATABSE_LINK_COLUMN),
				new MsFeatureIdentityComparator(SortProperty.ID));

		msfIdRenderer = new CompoundIdentityDatabaseLinkRenderer();
		chmodRenderer = new ChemicalModificationRenderer();
		pieChartFrequencyRenderer = new PieChartFrequencyRenderer();

		//	Renderers
		columnModel.getColumnById(FeatureDataTableModel.MS_FEATURE_COLUMN)
			.setCellRenderer(cfRenderer);	// Name column
		columnModel.getColumnById(FeatureDataTableModel.DATABSE_LINK_COLUMN)
			.setCellRenderer(msfIdRenderer);	// Database ID column
		columnModel.getColumnById(FeatureDataTableModel.AMBIGUITY_COLUMN)
			.setCellRenderer(radioRenderer);	// Multiple IDs column
		columnModel.getColumnById(FeatureDataTableModel.CHEM_MOD_OBSERVED_COLUMN)
			.setCellRenderer(chmodRenderer); // Compound form column
		columnModel.getColumnById(FeatureDataTableModel.CHEM_MOD_LIBRARY_COLUMN)
			.setCellRenderer(chmodRenderer); // Compound form column
		columnModel.getColumnById(FeatureDataTableModel.RETENTION_COLUMN)
			.setCellRenderer(rtRenderer); // Retention time
		columnModel.getColumnById(FeatureDataTableModel.RETENTION_OBSERVED_MEDIAN_COLUMN)
			.setCellRenderer(rtRenderer); // Retention time median
		columnModel.getColumnById(FeatureDataTableModel.NEUTRAL_MASS_COLUMN)
			.setCellRenderer(mzRenderer); // Neutral mass
		columnModel.getColumnById(FeatureDataTableModel.MONOISOTOPIC_PEAK_COLUMN)
			.setCellRenderer(mzRenderer); // Base peak mass
		columnModel.getColumnById(FeatureDataTableModel.MCMILLAN_PERCENT_DELTA_COLUMN)
			.setCellRenderer(new McMillanDeltaPercentColorRenderer(new DecimalFormat("#.#"))); // McMillan % diff	
		columnModel.getColumnById(FeatureDataTableModel.POOLED_MEAN_COLUMN)
			.setCellRenderer(intensityRenderer); // Pooled median
		columnModel.getColumnById(FeatureDataTableModel.POOLED_RSD_COLUMN)
			.setCellRenderer(percentRenderer); // RSD pooled
		columnModel.getColumnById(FeatureDataTableModel.SAMPLE_RSD_COLUMN)
			.setCellRenderer(percentRenderer); // RSD sample
		columnModel.getColumnById(FeatureDataTableModel.SAMPLE_MEAN_COLUMN)
			.setCellRenderer(intensityRenderer); // Pooled median
		columnModel.getColumnById(FeatureDataTableModel.POOLED_FREQUENCY_COLUMN)
			.setCellRenderer(pieChartFrequencyRenderer); // Pooled frequency
		columnModel.getColumnById(FeatureDataTableModel.SAMPLE_FREQUENCY_COLUMN)
			.setCellRenderer(pieChartFrequencyRenderer); // Sample frequency

		//	Database link adapter
		mma = new MouseMotionAdapter() {

			public void mouseMoved(MouseEvent e) {

				Point p = e.getPoint();

				if(columnModel.isColumnVisible(columnModel.getColumnById(FeatureDataTableModel.DATABSE_LINK_COLUMN))) {

					if (columnAtPoint(p) == columnModel.getColumnIndex(FeatureDataTableModel.DATABSE_LINK_COLUMN))
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

		addColumnSelectorPopup();

		//	Table header filter
		thf = new TableFilterHeader(this, AutoChoices.ENABLED);
		thf.getParserModel().setFormat(MsFeature.class, new MsFeatureFormat(SortProperty.Name));
		thf.getParserModel().setComparator(MsFeature.class, new MsFeatureComparator(SortProperty.Name));
		thf.getParserModel().setFormat(MsFeatureIdentity.class, new MsFeatureIdentityFormat(CompoundIdentityField.DB_ID));
		thf.getParserModel().setComparator(MsFeatureIdentity.class, new MsFeatureIdentityComparator(SortProperty.ID));

		tca = new TableColumnAdjuster(this);
		tca.adjustColumns();
	}

	public MsFeature getFeatureAtPopup() {

		MsFeature msf = null;

		if(popupRow > -1) {

			int col = getColumnIndex(FeatureDataTableModel.MS_FEATURE_COLUMN);
			msf = (MsFeature) getValueAt(popupRow, col);
		}
		return msf;
	}

	public synchronized void clearTable() {
		model.setRowCount(0);
	}

	public Collection<MsFeature>getSelectedFeatures() {

		Collection<MsFeature>selected = new ArrayList<MsFeature>();
		int col = getColumnIndex(FeatureDataTableModel.MS_FEATURE_COLUMN);

		for (int i : getSelectedRows()) {

			MsFeature rowFeature = (MsFeature) getValueAt(i, col);
			selected.add(rowFeature);
		}
		return selected;
	}

	@Override
	public MsFeature getSelectedFeature() {

		MsFeature selected = null;
		int col = getColumnModel().getColumnIndex(FeatureDataTableModel.MS_FEATURE_COLUMN);
		int row = getSelectedRow();

		if(row > -1)
			selected = (MsFeature) model.getValueAt(convertRowIndexToModel(row), col);

		return selected;
	}

	public Set<MsFeature>getVisibleFeatures(){

		Set<MsFeature> visible = new HashSet<MsFeature>();
		int col = getColumnIndex(FeatureDataTableModel.MS_FEATURE_COLUMN);

		for (int i=0; i<getRowCount(); i++) {

			MsFeature rowFeature = (MsFeature) getValueAt(i, col);
			visible.add(rowFeature);
		}
		return visible;
	}

	public Set<MsFeature>getAllFeatures(){

		Set<MsFeature> allFeatures = new HashSet<MsFeature>();
		int col = model.getColumnIndex(FeatureDataTableModel.MS_FEATURE_COLUMN);

		for (int i=0; i<model.getRowCount(); i++) {

			MsFeature rowFeature = (MsFeature) model.getValueAt(i, col);
			allFeatures.add(rowFeature);
		}
		return allFeatures;
	}

	public void setTableModelFromFeatureMap(
			Map<DataPipeline, Collection<MsFeature>>featureMap) {
		
		if(featureMap == null) {
			clearTable();
			return;
		}
		Runnable swingCode = new Runnable() {

			public void run() {

				JTable table = thf.getTable();
				thf.setTable(null);
				model.removeTableModelListener(table);
				model.setTableModelFromFeatureMap(featureMap);
				model.addTableModelListener(table);
				model.fireTableDataChanged();
				thf.setTable(table);
				tca.adjustColumns();
			}
		};
		try {
			if (SwingUtilities.isEventDispatchThread())
				swingCode.run();
			else
				SwingUtilities.invokeAndWait(swingCode);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public int getFeatureRow(MsFeature feature) {

		int row = -1;
		int col = getColumnIndex(FeatureDataTableModel.MS_FEATURE_COLUMN);

		for (int i = 0; i < getRowCount(); i++) {

			if (feature.equals((MsFeature)getValueAt(i, col)))
				return i;
		}
		return row;
	}

	@Override
	public void setTableModelFromFeatureCluster(MsFeatureCluster selectedCluster) {

		clearTable();
		if(selectedCluster != null)
			model.setTableModelFromFeatureCluster(selectedCluster);
	}
	
	@Override
	public Map<DataPipeline, Collection<MsFeature>> getSelectedFeaturesMap() {

		Map<DataPipeline, Collection<MsFeature>>featureMap = 
				new TreeMap<DataPipeline, Collection<MsFeature>>();
		int fcol = model.getColumnIndex(FeatureDataTableModel.MS_FEATURE_COLUMN);
		int dpcol = model.getColumnIndex(FeatureDataTableModel.DATA_PIPELINE_COLUMN);
		for (int i : getSelectedRows()) {
			
			MsFeature rowFeature = (MsFeature) getValueAt(i, fcol);
			DataPipeline dp = (DataPipeline) getValueAt(i, dpcol);
			if(!featureMap.containsKey(dp))
				featureMap.put(dp, new TreeSet<MsFeature>(new MsFeatureComparator(SortProperty.Name)));

			featureMap.get(dp).add(rowFeature);
		}
		return featureMap;
	}
}

























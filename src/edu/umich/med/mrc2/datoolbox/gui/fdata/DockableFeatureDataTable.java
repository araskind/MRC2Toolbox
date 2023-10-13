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

import java.awt.Component;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.table.TableCellRenderer;

import org.jsoup.Jsoup;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureCluster;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureIdentity;
import edu.umich.med.mrc2.datoolbox.data.enums.TableRowSubset;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.IndeterminateProgressDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.LongUpdateTask;

public class DockableFeatureDataTable extends DefaultSingleCDockable{

	private static final Icon componentIcon = GuiUtils.getIcon("table", 16);
	private FeatureDataTable featureDataTable;
	private IndeterminateProgressDialog idp;

	public DockableFeatureDataTable(String id, String title) {

		super(id, componentIcon, title, null, Permissions.MIN_MAX_STACK);
		setCloseable(false);

		featureDataTable = new FeatureDataTable();
		add(new JScrollPane(featureDataTable));
	}

	/**
	 * @return the libraryFeatureTable
	 */
	public FeatureDataTable getTable() {
		return featureDataTable;
	}

	public MsFeature getFeatureAtPopup() {
		return featureDataTable.getFeatureAtPopup();
	}

	public synchronized void clearTable() {
		featureDataTable.clearTable();
	}
	
	public Collection<MsFeature>getFeaturees(TableRowSubset subset){
		
		if(subset.equals(TableRowSubset.SELECTED))
			return featureDataTable.getSelectedFeatures();
		else if(subset.equals(TableRowSubset.FILTERED))
			return featureDataTable.getVisibleFeatures();
		else
			return featureDataTable.getAllFeatures();
	}

	public Collection<MsFeature>getSelectedFeatures() {
		return featureDataTable.getSelectedFeatures();
	}
	
	public Map<DataPipeline, Collection<MsFeature>> getSelectedFeaturesMap() {
		return featureDataTable.getSelectedFeaturesMap();
	}

	public Set<MsFeature>getVisibleFeatures(){
		return featureDataTable.getVisibleFeatures();
	}

	public Set<MsFeature>getAllFeatures(){
		return featureDataTable.getAllFeatures();
	}

	public void setTableModelFromFeatureMap(
			Map<DataPipeline, Collection<MsFeature>>featureMap) {

		TableUpdateTask task = new TableUpdateTask(featureMap);
		idp = new IndeterminateProgressDialog(
				"Uptating table data ...", featureDataTable, task);
		idp.setLocationRelativeTo(this.getContentPane());
		idp.setVisible(true);
	}

	public int getFeatureRow(MsFeature feature) {
		return featureDataTable.getFeatureRow(feature);
	}

	public void setTableModelFromFeatureCluster(MsFeatureCluster selectedCluster) {

		TableUpdateTask task = new TableUpdateTask(selectedCluster);
		idp = new IndeterminateProgressDialog(
				"Uptating table data ...", featureDataTable, task);
		idp.setLocationRelativeTo(this.getContentPane());
		idp.setVisible(true);
	}

	public boolean hasHiddenRows() {
		return (featureDataTable.getRowCount() < featureDataTable.getModel().getRowCount());
	}

	public void selectFeatureRow(int featureRow) {

		if(featureRow > -1 && featureRow <= featureDataTable.getRowCount()) {

			featureDataTable.setRowSelectionInterval(featureRow, featureRow);
			featureDataTable.scrollRectToVisible(
					new Rectangle(featureDataTable.getCellRect(featureRow, 0, true)));
		}
	}

	public void updateFeatureData(MsFeature source) {

		((FeatureDataTableModel) featureDataTable.getModel()).updateFeatureData(source);
		featureDataTable.scrollToSelected();
	}

	public void copySelectedFeaturesData(boolean includeHeader) {

		if (featureDataTable.getSelectedRowCount() > 0) {

			StringBuffer featureData = new StringBuffer();
			int numCols = featureDataTable.getColumnCount();

			if (includeHeader) {

				featureData.append(featureDataTable.getColumnName(0));

				for (int i = 1; i < numCols; i++)
					featureData.append("\t" + featureDataTable.getColumnName(i));

				featureData.append("\n");
			}
			for (int i : featureDataTable.getSelectedRows()) {

				for (int j = 0; j < numCols; j++) {

					final TableCellRenderer renderer = featureDataTable.getCellRenderer(i, j);
					final Component comp = featureDataTable.prepareRenderer(renderer, i, j);

					String txt = null;

					if (comp instanceof JLabel)
						txt = ((JLabel) comp).getText();

					if (comp instanceof JTextPane)
						txt = ((JTextPane) comp).getText();

					if (featureDataTable.getColumnClass(j).equals(MsFeatureIdentity.class))
						featureData.append(Jsoup.parse(txt).text());
					else
						featureData.append(txt.trim());

					if (j < numCols - 1)
						featureData.append("\t");
					else
						featureData.append("\n");
				}
			}
			StringSelection stringSelection = new StringSelection(featureData.toString());
			Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
			clpbrd.setContents(stringSelection, null);
		}
	}

	class TableUpdateTask extends LongUpdateTask {
		/*
		 * Main task. Executed in background thread.
		 */
		private Map<DataPipeline, Collection<MsFeature>>featureMap;
		private MsFeatureCluster selectedCluster;

		public TableUpdateTask(MsFeatureCluster selectedCluster) {
			super();
			this.selectedCluster = selectedCluster;
			this.featureMap = null;
		}

		public TableUpdateTask(Map<DataPipeline, Collection<MsFeature>>featureMap) {
			super();
			this.featureMap = featureMap;
			this.selectedCluster = null;
		}

		@Override
		public Void doInBackground() {

			if (selectedCluster != null)
				featureDataTable.setTableModelFromFeatureCluster(selectedCluster);
			if (featureMap != null)
				featureDataTable.setTableModelFromFeatureMap(featureMap);

			return null;
		}
	}
}
















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

package edu.umich.med.mrc2.datoolbox.gui.idworks.clustree.summary;

import java.util.ArrayList;
import java.util.Collection;

import javax.swing.table.TableRowSorter;

import edu.umich.med.mrc2.datoolbox.data.MinimalMSOneFeature;
import edu.umich.med.mrc2.datoolbox.data.msclust.MsFeatureInfoBundleCluster;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.AutoChoices;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.TableFilterHeader;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.WordWrapCellRenderer;

public class FoundLookupFeaturesTable extends BasicTable {

	/**
	 *
	 */
	private static final long serialVersionUID = -3232039944841480790L;
	private FoundLookupFeaturesTableModel model;

	public FoundLookupFeaturesTable() {

		super();
		model = new FoundLookupFeaturesTableModel();		
		setModel(model);
		rowSorter = new TableRowSorter<FoundLookupFeaturesTableModel>(model);
		setRowSorter(rowSorter);
		columnModel.getColumnById(
				FoundLookupFeaturesTableModel.CLUSTER_COLUMN).setCellRenderer(
						new WordWrapCellRenderer());
		columnModel.getColumnById(
				FoundLookupFeaturesTableModel.FEATURE_COLUMN).setCellRenderer(
						new WordWrapCellRenderer());
		columnModel.getColumnById(
				FoundLookupFeaturesTableModel.MZ_COLUMN).setMaxWidth(80);
		columnModel.getColumnById(
				FoundLookupFeaturesTableModel.RT_COLUMN).setMaxWidth(80);
		columnModel.getColumnById(
				FoundLookupFeaturesTableModel.RANK_COLUMN).setMaxWidth(80);
		
		thf = new TableFilterHeader(this, AutoChoices.ENABLED);	
		finalizeLayout();
	}
	
	public void setTableModelFromMsFeatureInfoBundleClusterCollection(
			Collection<MsFeatureInfoBundleCluster>clusters) {
		thf.setTable(null);
		model.setTableModelFromMsFeatureInfoBundleClusterCollection(clusters);
		thf.setTable(this);
		tca.adjustColumns();

	}
	
	public Collection<MsFeatureInfoBundleCluster>getSelectedClusters(){
		
		Collection<MsFeatureInfoBundleCluster>selectedFeatures = 
				new ArrayList<MsFeatureInfoBundleCluster>();
		int[]selectedRows = getSelectedRows();
		if(selectedRows.length == 0)
			return selectedFeatures;
		
		int featureCol = model.getColumnIndex(FoundLookupFeaturesTableModel.CLUSTER_COLUMN);
		for(int i : selectedRows)
			selectedFeatures.add(
					(MsFeatureInfoBundleCluster)model.getValueAt(convertRowIndexToModel(i), featureCol));
		
		return selectedFeatures;
	}
	
	public Collection<MinimalMSOneFeature>getSelectedFeatures(){
		
		Collection<MinimalMSOneFeature>selectedFeatures = new ArrayList<MinimalMSOneFeature>();
		int[]selectedRows = getSelectedRows();
		if(selectedRows.length == 0)
			return selectedFeatures;
		
		int featureCol = model.getColumnIndex(FoundLookupFeaturesTableModel.FEATURE_COLUMN);
		for(int i : selectedRows)
			selectedFeatures.add(
					(MinimalMSOneFeature)model.getValueAt(convertRowIndexToModel(i), featureCol));
		
		return selectedFeatures;
	}
	
	public Collection<MinimalMSOneFeature>getAllFeatures(){
		
		Collection<MinimalMSOneFeature>selectedFeatures = new ArrayList<MinimalMSOneFeature>();
		int featureCol = model.getColumnIndex(FoundLookupFeaturesTableModel.FEATURE_COLUMN);
		for(int i=0; i<model.getRowCount(); i++)
			selectedFeatures.add((MinimalMSOneFeature)model.getValueAt(i, featureCol));
		
		return selectedFeatures;
	}
}

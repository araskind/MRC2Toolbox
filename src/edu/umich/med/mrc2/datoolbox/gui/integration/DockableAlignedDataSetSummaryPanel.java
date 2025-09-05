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

package edu.umich.med.mrc2.datoolbox.gui.integration;

import java.util.Collection;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.JScrollPane;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureCluster;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.integration.dpalign.AlignedDataSetSummaryTable;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class DockableAlignedDataSetSummaryPanel extends DefaultSingleCDockable {

	private static final Icon componentIcon = GuiUtils.getIcon("dataSetAlignmentManagerIcon", 16);
	private AlignedDataSetSummaryTable alignedDataSetSummaryTable;
	
	public DockableAlignedDataSetSummaryPanel(DataIntegratorPanel parentPanel) {
		
		super("DockableAlignedDataSetSummaryPanel", 
				componentIcon, "Aligned data set summary", null, Permissions.MIN_MAX_STACK);
		setCloseable(false);
		
		alignedDataSetSummaryTable = new AlignedDataSetSummaryTable();
		add(new JScrollPane(alignedDataSetSummaryTable));

		alignedDataSetSummaryTable.getSelectionModel().addListSelectionListener(parentPanel);
	}
	
	public int getFeatureRow(MsFeature feature) {
		return alignedDataSetSummaryTable.getFeatureRow(feature);
	}

	public void setTableModelFromFeatureMap(Map<DataPipeline,Collection<MsFeature>> featureMap) {
		//	alignedDataSetSummaryTable.setTableModelFromFeatureMap(featureMap);
	}

	public void setTableModelFromFeatureCluster(MsFeatureCluster featureCluster) {
		alignedDataSetSummaryTable.setTableModelFromFeatureCluster(featureCluster);
	}

	public Collection<MsFeature> getSelectedFeatures() {
		return alignedDataSetSummaryTable. getSelectedFeatures();
	}

	public MsFeature getSelectedFeature() {
		return alignedDataSetSummaryTable. getSelectedFeature();
	}

	public Map<DataPipeline, Collection<MsFeature>> getSelectedFeaturesMap() {
		return alignedDataSetSummaryTable.getSelectedFeaturesMap();
	}
}

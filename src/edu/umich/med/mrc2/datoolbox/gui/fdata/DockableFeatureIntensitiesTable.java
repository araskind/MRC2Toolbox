/*******************************************************************************
 *
 * (C) Copyright 2018-2025 MRC2 (http://mrc2.umich.edu).
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

import java.util.Collection;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.JScrollPane;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class DockableFeatureIntensitiesTable extends DefaultSingleCDockable{

	private static final Icon componentIcon = GuiUtils.getIcon("dataTable", 16);
	private FeatureIntensitiesTable featureIntensitiesTable;

	public DockableFeatureIntensitiesTable(String id, String title) {

		super(id, componentIcon, title, null, Permissions.MIN_MAX_STACK);
		setCloseable(false);

		featureIntensitiesTable = new FeatureIntensitiesTable();
		add(new JScrollPane(featureIntensitiesTable));
	}

	public void setTableModelFromFeatureAndPipeline(
			MsFeature feature, DataPipeline dataPipeline) {
		featureIntensitiesTable.
				setTableModelFromFeatureAndPipeline(feature, dataPipeline);
	}

	public void setTableModelFromFeatureMap(
			Map<DataPipeline, Collection<MsFeature>> selectedFeaturesMap) {
		featureIntensitiesTable.setTableModelFromFeatureMap(selectedFeaturesMap);
	}

	public void sortByFeatureAndSample() {
		featureIntensitiesTable.sortByFeatureAndSample();
	}

	public synchronized void clearTable() {
		featureIntensitiesTable.clearTable();
	}
}

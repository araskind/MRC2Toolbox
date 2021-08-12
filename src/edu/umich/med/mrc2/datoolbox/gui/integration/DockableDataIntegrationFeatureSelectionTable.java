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

import javax.swing.Icon;
import javax.swing.JScrollPane;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import edu.umich.med.mrc2.datoolbox.gui.dereplication.ClusterDisplayPanel;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicFeatureTable;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class DockableDataIntegrationFeatureSelectionTable extends DefaultSingleCDockable {

	private DataIntegrationFeatureSelectionTable featureSelectionTable;

	private static final Icon componentIcon = GuiUtils.getIcon("dataIntegrationTable", 16);

	public DockableDataIntegrationFeatureSelectionTable(ClusterDisplayPanel parentPanel) {

		super("DockableDataIntegrationFeatureSelectionTable", 
				componentIcon, "Select representative feature", null, Permissions.MIN_MAX_STACK);
		setCloseable(false);

		featureSelectionTable = new DataIntegrationFeatureSelectionTable();
		add(new JScrollPane(featureSelectionTable));

		featureSelectionTable.getSelectionModel().addListSelectionListener(parentPanel);
		featureSelectionTable.addTablePopupMenu(new IntegrationFeaturePopupMenu(parentPanel));
	}

	/**
	 * @return the libraryFeatureTable
	 */
	public BasicFeatureTable getTable() {
		return featureSelectionTable;
	}
}

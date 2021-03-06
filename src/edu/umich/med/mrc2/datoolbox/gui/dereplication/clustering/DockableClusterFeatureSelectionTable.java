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

package edu.umich.med.mrc2.datoolbox.gui.dereplication.clustering;

import javax.swing.Icon;
import javax.swing.JScrollPane;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import edu.umich.med.mrc2.datoolbox.gui.dereplication.ClusterDisplayPanel;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicFeatureTable;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class DockableClusterFeatureSelectionTable extends DefaultSingleCDockable {

	private ClusterFeatureSelectionTable clusterFeatureSelectionTable;
	private static final Icon componentIcon = GuiUtils.getIcon("clusterFeatureTable", 16);

	public DockableClusterFeatureSelectionTable(ClusterDisplayPanel parentPanel) {

		super("DockableClusterFeatureSelectionTable", componentIcon, "Cluster features", null, Permissions.MIN_MAX_STACK);
		setCloseable(false);

		clusterFeatureSelectionTable = new ClusterFeatureSelectionTable();
		add(new JScrollPane(clusterFeatureSelectionTable));

		clusterFeatureSelectionTable.getSelectionModel().addListSelectionListener(parentPanel);
		clusterFeatureSelectionTable.addTablePopupMenu(new CorrelationClusterFeaturePopupMenu(parentPanel));
	}

	/**
	 * @return the libraryFeatureTable
	 */
	public BasicFeatureTable getTable() {
		return clusterFeatureSelectionTable;
	}
}

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

package edu.umich.med.mrc2.datoolbox.gui.dereplication.clustering;

import java.util.Collection;

import javax.swing.Icon;
import javax.swing.JScrollPane;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureCluster;
import edu.umich.med.mrc2.datoolbox.gui.dereplication.ClusterDisplayPanel;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.IndeterminateProgressDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.LongUpdateTask;

public class DockableMassDifferenceTable extends DefaultSingleCDockable {

	private MassDifferenceTable massDifferenceTable;
	private IndeterminateProgressDialog idp;
	private static final Icon componentIcon = GuiUtils.getIcon("assignDeltas", 16);

	public DockableMassDifferenceTable(ClusterDisplayPanel parentPanel) {

		super("DockableMassDifferenceTable", componentIcon, "Pairwise differences", null, Permissions.MIN_MAX_STACK);
		setCloseable(false);

		massDifferenceTable = new MassDifferenceTable();
		add(new JScrollPane(massDifferenceTable));

		massDifferenceTable.getSelectionModel().addListSelectionListener(parentPanel);
	}

	/**
	 * @return the libraryFeatureTable
	 */
	public MassDifferenceTable getTable() {
		return massDifferenceTable;
	}

	public synchronized void clearTable() {
		massDifferenceTable.clearTable();
	}

	public void setTableModelFromFeatureCluster(MsFeatureCluster activeCluster) {

		TableUpdateTask task = new TableUpdateTask(activeCluster);
		idp = new IndeterminateProgressDialog("Uptating table data ...", massDifferenceTable, task);
		idp.setLocationRelativeTo(this.getContentPane());
		idp.setVisible(true);
	}

	public void setTableModelFromFeatures(Collection<MsFeature> selectedFeatures, MsFeatureCluster activeCluster) {

		TableUpdateTask task = new TableUpdateTask(selectedFeatures, activeCluster);
		idp = new IndeterminateProgressDialog("Uptating table data ...", massDifferenceTable, task);
		idp.setLocationRelativeTo(this.getContentPane());
		idp.setVisible(true);
	}

	class TableUpdateTask extends LongUpdateTask {
		/*
		 * Main task. Executed in background thread.
		 */
		private Collection<MsFeature> featureList;
		private MsFeatureCluster selectedCluster;

		public TableUpdateTask(MsFeatureCluster selectedCluster) {
			super();
			this.selectedCluster = selectedCluster;
			this.featureList = null;
		}

		public TableUpdateTask(Collection<MsFeature> featureList, MsFeatureCluster activeCluster) {
			super();
			this.featureList = featureList;
			this.selectedCluster = activeCluster;
		}

		@Override
		public Void doInBackground() {

			if (selectedCluster != null && featureList == null)
				massDifferenceTable.setTableModelFromFeatureCluster(selectedCluster);
			if (selectedCluster != null && featureList != null)
				massDifferenceTable.setTableModelFromFeatures(featureList, selectedCluster);

			return null;
		}
	}
}

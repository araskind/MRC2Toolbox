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

package edu.umich.med.mrc2.datoolbox.gui.mgf;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JScrollPane;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import edu.umich.med.mrc2.datoolbox.data.MsMsCluster;
import edu.umich.med.mrc2.datoolbox.data.SimpleMsMs;
import edu.umich.med.mrc2.datoolbox.gui.mgf.mgftree.MsMsTreeMouseHandler;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;

public class DockableMsMsClusterTable extends DefaultSingleCDockable implements ActionListener {

	private MsMsClusterTable msMsClusterTable;

	private static final Icon componentIcon = GuiUtils.getIcon("clusterFeatureTable", 16);

	public DockableMsMsClusterTable(MgfPanel mgfPanel) {

		super("DockableMsMsClusterTable", componentIcon, "Features in cluster", null, Permissions.MIN_MAX_STACK);
		setCloseable(false);

		msMsClusterTable = new MsMsClusterTable();
		add(new JScrollPane(msMsClusterTable));

		msMsClusterTable.getSelectionModel().addListSelectionListener(mgfPanel);
	}

	/**
	 * @return the libraryFeatureTable
	 */
	public MsMsClusterTable getTable() {
		return msMsClusterTable;
	}

	public synchronized void clearTable() {
		msMsClusterTable.clearTable();
	}

	@Override
	public void actionPerformed(ActionEvent event) {

		String command = event.getActionCommand();

		if (command.equals(MsMsTreeMouseHandler.LOOKUP_FEATURE_MSMS_COMMAND))
			lookupFeatureMsMs();

		if (command.equals(MsMsTreeMouseHandler.COPY_FEATURE_MSMS_COMMAND))
			msMsClusterTable.copyFeatureMsMs();

		msMsClusterTable.actionPerformed(event);
	}

	private void lookupFeatureMsMs() {

		MessageDialog.showInfoMsg("LOOKUP_FEATURE_MSMS_COMMAND");
	}

	public SimpleMsMs getSelectedMsMs() {
		return msMsClusterTable.getSelectedMsMs();
	}

	public void setTableModelFromMsMsCluster(MsMsCluster fc) {
		msMsClusterTable.setTableModelFromMsMsCluster(fc);
	}
}

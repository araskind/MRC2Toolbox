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

package edu.umich.med.mrc2.datoolbox.gui.dereplication;

import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JButton;

import edu.umich.med.mrc2.datoolbox.data.MsFeatureClusterSet;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.CommonToolbar;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public abstract class ClusterDisplayToolbar  extends CommonToolbar {

	/**
	 *
	 */
	private static final long serialVersionUID = 6819241957401589419L;

	protected static final Icon filterIcon = GuiUtils.getIcon("filterClusters", 32);
	protected static final Icon resetFilterIcon = GuiUtils.getIcon("resetFilter", 32);
	protected static final Icon expandIcon = GuiUtils.getIcon("expand", 32);
	protected static final Icon collapseIcon = GuiUtils.getIcon("collapse", 32);

	protected JButton
		filterClustersButton,
		resetFilterButton,
		expandCollapseButton;

	public ClusterDisplayToolbar(ActionListener commandListener) {

		super(commandListener);

		expandCollapseButton = GuiUtils.addButton(this, null, expandIcon, commandListener,
				MainActionCommands.EXPAND_CLUSTERS_COMMAND.getName(),
				MainActionCommands.EXPAND_CLUSTERS_COMMAND.getName(), buttonDimension);

		filterClustersButton = GuiUtils.addButton(this, null, filterIcon, commandListener,
				MainActionCommands.SHOW_CLUSTER_FILTER_COMMAND.getName(),
				MainActionCommands.SHOW_CLUSTER_FILTER_COMMAND.getName(), buttonDimension);

		resetFilterButton = GuiUtils.addButton(this, null, resetFilterIcon, commandListener,
				MainActionCommands.RESET_FILTER_CLUSTERS_COMMAND.getName(),
				MainActionCommands.RESET_FILTER_CLUSTERS_COMMAND.getName(), buttonDimension);

		addSeparator(buttonDimension);
	}

	public void treeExpanded(boolean expanded) {

		if (expanded) {
			expandCollapseButton.setIcon(collapseIcon);
			expandCollapseButton.setActionCommand(MainActionCommands.COLLAPSE_CLUSTERS_COMMAND.getName());
			expandCollapseButton.setToolTipText(MainActionCommands.COLLAPSE_CLUSTERS_COMMAND.getName());
		} else {
			expandCollapseButton.setIcon(expandIcon);
			expandCollapseButton.setActionCommand(MainActionCommands.EXPAND_CLUSTERS_COMMAND.getName());
			expandCollapseButton.setToolTipText(MainActionCommands.EXPAND_CLUSTERS_COMMAND.getName());
		}
	}

	public abstract void updateGuiFromActiveSet(MsFeatureClusterSet integratedSet);

}

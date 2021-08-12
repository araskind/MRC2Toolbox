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

import java.awt.event.ActionListener;

import edu.umich.med.mrc2.datoolbox.gui.clustertree.ClusterFeaturePopupMenu;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class IntegrationFeaturePopupMenu extends ClusterFeaturePopupMenu {

	/**
	 *
	 */
	private static final long serialVersionUID = 880889997868033497L;

	public IntegrationFeaturePopupMenu(ActionListener listener) {
		super(listener);
		populateMenu();
	}

	@Override
	protected void populateMenu() {

		newClusterFromSelectedMenuItem = GuiUtils.addMenuItem(this,
				MainActionCommands.NEW_CLUSTER_FROM_SELECTED_COMMAND.getName(), listener,
				MainActionCommands.NEW_CLUSTER_FROM_SELECTED_COMMAND.getName());
		newClusterFromSelectedMenuItem.setIcon(newClusterIcon);

		removeSelecteFromClusterMenuItem = GuiUtils.addMenuItem(this,
				MainActionCommands.REMOVE_SELECTED_FROM_CLUSTER_COMMAND.getName(), listener,
				MainActionCommands.REMOVE_SELECTED_FROM_CLUSTER_COMMAND.getName());
		removeSelecteFromClusterMenuItem.setIcon(deleteFromClusterIcon);

		this.addSeparator();

		matchFeatureToLibraryMenuItem = GuiUtils.addMenuItem(this,
				MainActionCommands.SEARCH_FEATURE_AGAINST_LIBRARY_COMMAND.getName(), listener,
				MainActionCommands.SEARCH_FEATURE_AGAINST_LIBRARY_COMMAND.getName());
		matchFeatureToLibraryMenuItem.setIcon(searchLibraryIcon);

		searchFeatureAgainstDatabaseMenuItem = GuiUtils.addMenuItem(this,
				MainActionCommands.SEARCH_FEATURE_AGAINST_DATABASE_COMMAND.getName(), listener,
				MainActionCommands.SEARCH_FEATURE_AGAINST_DATABASE_COMMAND.getName());
		searchFeatureAgainstDatabaseMenuItem.setIcon(searchDatabaseIcon);

		this.addSeparator();

		textAnnotationMenuItem = GuiUtils.addMenuItem(this, MainActionCommands.EDIT_FEATURE_METADATA_COMMAND.getName(),
				listener, MainActionCommands.EDIT_FEATURE_METADATA_COMMAND.getName());
		textAnnotationMenuItem.setIcon(textAnnotationIcon);
	}
}

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

package edu.umich.med.mrc2.datoolbox.gui.integration.dsmanager;

import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JMenuItem;

import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTablePopupMenu;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class MsFeatureClusterSetTablePopupMenu extends BasicTablePopupMenu {

	private static final long serialVersionUID = 1L;
	private static final Icon openDataSetIcon = GuiUtils.getIcon("open", 24);
	private static final Icon deleteDataSetIcon = GuiUtils.getIcon("deleteFromCluster", 24);
	private static final Icon newFeatureCollectionIcon = GuiUtils.getIcon("newFeatureSubset", 24);
	

	public MsFeatureClusterSetTablePopupMenu(
			ActionListener listener,
			BasicTable copyListener) {
		super(listener, copyListener);

		JMenuItem openMenuItem = GuiUtils.addMenuItem(this,
				MainActionCommands.LOAD_SELECTED_FEATURE_CLUSTER_SET.getName(), mainActionListener,
				MainActionCommands.LOAD_SELECTED_FEATURE_CLUSTER_SET.getName());
		openMenuItem.setIcon(openDataSetIcon);
		
		addSeparator();

		JMenuItem deleteMenuItem = GuiUtils.addMenuItem(this,
				MainActionCommands.DELETE_SELECTED_CLUSTER_SETS.getName(), listener,
				MainActionCommands.DELETE_SELECTED_CLUSTER_SETS.getName());
		deleteMenuItem.setIcon(deleteDataSetIcon);	
		
		addSeparator();
		
		JMenuItem newFeatureCollectionMenuItem = GuiUtils.addMenuItem(this,
				MainActionCommands.NEW_FEATURE_COLLECTION_FROM_SELECTED_CLUSTER_SETS.getName(), listener,
				MainActionCommands.NEW_FEATURE_COLLECTION_FROM_SELECTED_CLUSTER_SETS.getName());
		newFeatureCollectionMenuItem.setIcon(newFeatureCollectionIcon);	
	}
}

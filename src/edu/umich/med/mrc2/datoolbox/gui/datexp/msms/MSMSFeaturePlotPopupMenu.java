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

package edu.umich.med.mrc2.datoolbox.gui.datexp.msms;

import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JMenuItem;

import edu.umich.med.mrc2.datoolbox.gui.idworks.FeatureListPopupMenu;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class MSMSFeaturePlotPopupMenu extends FeatureListPopupMenu {

	private static final long serialVersionUID = -1420991571317499448L;
	
	private static final Icon filterSelectedFeaturesIcon = GuiUtils.getIcon("filter", 24);
	private JMenuItem filterSelectedFeaturesMenuItem;

	public MSMSFeaturePlotPopupMenu(ActionListener listener) {

		super(listener, null, true);
		
		filterSelectedFeaturesMenuItem = GuiUtils.addMenuItem(this,
				MainActionCommands.FILTER_SELECTED_MSMS_FEATURES_IN_TABLE.getName(), mainActionListener,
				MainActionCommands.FILTER_SELECTED_MSMS_FEATURES_IN_TABLE.getName());
		filterSelectedFeaturesMenuItem.setIcon(filterSelectedFeaturesIcon);
		
		addSeparator();
		
		addFeatureCollectionMenu(false);
	}
}


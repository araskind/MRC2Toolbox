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

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Icon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class MsMsClusterTableMouseHandler extends MouseAdapter {

	private static final Icon lookupMsMsIcon = GuiUtils.getIcon("searchMsMs", 24);
	private static final Icon copyMsMsIcon = GuiUtils.getIcon("copy", 24);

	public static final String LOOKUP_FEATURE_MSMS_COMMAND = "LOOKUP_FEATURE_MSMS";
	public static final String COPY_FEATURE_MSMS_COMMAND = "COPY_FEATURE_MSMS";

	private MsMsClusterTable clusterTable;
	private JPopupMenu featurePopupMenu;
	private static JMenuItem
		lookupFeatureMsMsMenuItem,
		copyFeatureMsMsMenuItem;

	/**
	 * Constructor
	 */
	public MsMsClusterTableMouseHandler(MsMsClusterTable clusterTable) {

		this.clusterTable = clusterTable;

		featurePopupMenu = new JPopupMenu();
		lookupFeatureMsMsMenuItem = GuiUtils.addMenuItem(featurePopupMenu, "Lookup feature MSMS", clusterTable,
				LOOKUP_FEATURE_MSMS_COMMAND);
		lookupFeatureMsMsMenuItem.setIcon(lookupMsMsIcon);
		copyFeatureMsMsMenuItem = GuiUtils.addMenuItem(featurePopupMenu, "Copy feature MSMS", clusterTable,
				COPY_FEATURE_MSMS_COMMAND);
		copyFeatureMsMsMenuItem.setIcon(copyMsMsIcon);
	}

	private void handlePopupTriggerEvent(MouseEvent e) {

		if (e.getSource() instanceof MsMsClusterTable) {

			MsMsClusterTable source = (MsMsClusterTable) e.getSource();
			int row = source.rowAtPoint(e.getPoint());

			if (!source.isRowSelected(row))
				source.changeSelection(row, 2, false, false);

			// SimpleMsMs selectedMsMs = (SimpleMsMs)
			// source.getValueAt(source.getSelectedRow(), 2);
			featurePopupMenu.show(e.getComponent(), e.getX(), e.getY());
		}
	}

	public void mousePressed(MouseEvent e) {

		if (e.isPopupTrigger())
			handlePopupTriggerEvent(e);
	}

	public void mouseReleased(MouseEvent e) {

		if (e.isPopupTrigger())
			handlePopupTriggerEvent(e);
	}
}

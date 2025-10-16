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

package edu.umich.med.mrc2.datoolbox.gui.tables;

import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.table.TableColumn;

import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class ColumnSelectorPopup extends JPopupMenu {

	/**
	 *
	 */
	private static final long serialVersionUID = -2033690614424025202L;

	private static final Icon showAllIcon = GuiUtils.getIcon("eyeOpen", 16);
	private static final Icon adjustAllColumnsIcon = GuiUtils.getIcon("fit_x", 16);
	private static final Icon resetFiltersIcon = GuiUtils.getIcon("resetFilter", 16);
	private static final Icon preferencesIcon = GuiUtils.getIcon("processingMethod", 16);

	private JMenuItem
		titleItem,
		preferencesItem,
		showAllItem,
		resetFiltersItem,
		adjustAllItem;

	public ColumnSelectorPopup(ActionListener listener, XTableColumnModel columnModel) {

		super();
		titleItem = GuiUtils.addMenuItem(this, "Select visible columns", null, null);
		titleItem.setFont(new Font("SansSerif", Font.BOLD, 12));

		addSeparator();

		for(int i=0; i<columnModel.getColumnCount(false); i++) {

			TableColumn col = columnModel.getColumn(i, false);
			JCheckBoxMenuItem cbMenuItem = new JCheckBoxMenuItem(col.getHeaderValue().toString(), columnModel.isColumnVisible(col));
			cbMenuItem.addActionListener(listener);
			cbMenuItem.setActionCommand(col.getHeaderValue().toString());
			this.add(cbMenuItem);
		}
		addSeparator();
		
		preferencesItem = GuiUtils.addMenuItem(this,
				MainActionCommands.SHOW_TABLE_PREFERENCES_COMMAND.getName(), listener,
				MainActionCommands.SHOW_TABLE_PREFERENCES_COMMAND.getName());
		preferencesItem.setIcon(preferencesIcon);
		
		addSeparator();

		showAllItem = GuiUtils.addMenuItem(this,
				MainActionCommands.SHOW_ALL_TABLE_COLUMNS_COMMAND.getName(), listener,
				MainActionCommands.SHOW_ALL_TABLE_COLUMNS_COMMAND.getName());
		showAllItem.setIcon(showAllIcon);

		resetFiltersItem = GuiUtils.addMenuItem(this,
				MainActionCommands.RESET_COLUMN_FILTERS_COMMAND.getName(), listener,
				MainActionCommands.RESET_COLUMN_FILTERS_COMMAND.getName());
		resetFiltersItem.setIcon(resetFiltersIcon);

		adjustAllItem = GuiUtils.addMenuItem(this,
				MainActionCommands.ADJUST_ALL_TABLE_COLUMNS_COMMAND.getName(), listener,
				MainActionCommands.ADJUST_ALL_TABLE_COLUMNS_COMMAND.getName());
		adjustAllItem.setIcon(adjustAllColumnsIcon);
	}

	public void enableAllColumnItems() {

		for(Component c : getComponents()) {

			if(c instanceof JCheckBoxMenuItem)
				((JCheckBoxMenuItem)c).setSelected(true);
		}
	}
}

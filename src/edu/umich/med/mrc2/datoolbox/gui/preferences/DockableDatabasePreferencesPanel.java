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

package edu.umich.med.mrc2.datoolbox.gui.preferences;

import java.awt.BorderLayout;

import javax.swing.Icon;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class DockableDatabasePreferencesPanel extends DefaultSingleCDockable {

	private static final Icon componentIcon = GuiUtils.getIcon("database", 16);
	private DatabasePreferencesPanel databasePreferencesPanel;

	public DockableDatabasePreferencesPanel() {

		super("DockableDatabasePreferencesPanel", componentIcon, "Database connection", null, Permissions.MIN_MAX_STACK);
		setCloseable(false);
		setLayout(new BorderLayout(0, 0));
		databasePreferencesPanel = new DatabasePreferencesPanel(); 
		add(databasePreferencesPanel, BorderLayout.CENTER);
	}
	
	public String getUserName() {
		return databasePreferencesPanel.getUserName();
	}

	public String getPassword() {
		return databasePreferencesPanel.getPassword();
	}
}


















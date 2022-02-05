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

package edu.umich.med.mrc2.datoolbox.gui.idtlims;

import java.awt.Component;
import java.awt.event.ActionListener;

import javax.swing.Icon;

import bibliothek.gui.dock.action.DefaultDockActionSource;
import bibliothek.gui.dock.action.DockAction;
import bibliothek.gui.dock.action.LocationHint;
import bibliothek.gui.dock.action.actions.SimpleMenuAction;
import bibliothek.gui.dock.common.DefaultSingleCDockable;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.gui.main.MainWindow;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;

public abstract class AbstractIDTrackerLimsPanel extends DefaultSingleCDockable 
							implements ActionListener, BackedByPreferences {
	
	protected static final Icon actionIcon = GuiUtils.getIcon("cog", 16);
	protected DefaultDockActionSource menuActions;
	protected IDTrackerLimsManagerPanel idTrackerLimsManager;

	public AbstractIDTrackerLimsPanel(
			IDTrackerLimsManagerPanel idTrackerLimsManager,
			String id, 
			Icon icon, 
			String title, 
			Component content, 
			Permissions permissions) {
		
		super(id, icon, title, content, permissions);
		this.idTrackerLimsManager = idTrackerLimsManager;
	}
	
	protected void initActions() {
		
		DefaultDockActionSource actions = new DefaultDockActionSource(
				new LocationHint(LocationHint.DOCKABLE, LocationHint.LEFT));
		
        menuActions = new DefaultDockActionSource();		

		SimpleMenuAction actionMenu = new SimpleMenuAction(menuActions);
		actionMenu.setIcon(actionIcon);
		actionMenu.setText("Actions");       
		actions.add((DockAction)actionMenu);
		intern().setActionOffers(actions);
	}

	protected boolean isConnected() {
		
		if (!ConnectionManager.connectionDefined()) {
			MainWindow.displayErrorMessage("Connection error", 
					"Database connection not defined!");
			return false;
		}
		if (MRC2ToolBoxCore.getIdTrackerUser() == null) {
			MessageDialog.showErrorMsg("You are not logged in ID tracker!", this.getContentPane());
			return false;
		}
		return true;
	}
}

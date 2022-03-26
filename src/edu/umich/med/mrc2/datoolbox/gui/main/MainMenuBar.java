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

package edu.umich.med.mrc2.datoolbox.gui.main;

import java.awt.event.ActionListener;
import java.util.HashMap;

import javax.swing.Icon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSUser;
import edu.umich.med.mrc2.datoolbox.gui.utils.CommonMenuBar;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.main.BuildInformation;
import edu.umich.med.mrc2.datoolbox.main.StartupConfiguration;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class MainMenuBar extends CommonMenuBar {

	/**
	 *
	 */
	private static final long serialVersionUID = -6288875491040382193L;

	

	// Icons
	private static final Icon newProjectIcon = GuiUtils.getIcon("newProject", 24);
	private static final Icon openProjectIcon = GuiUtils.getIcon("open", 24);
	private static final Icon saveProjectIcon = GuiUtils.getIcon("save", 24);
	private static final Icon saveProjectCopyIcon = GuiUtils.getIcon("saveAs", 24);
	private static final Icon newRdaProjectIcon = GuiUtils.getIcon("newRawDataAnalysisProject", 24);
	private static final Icon editRdaProjectIcon = GuiUtils.getIcon("editRawDataAnalysisProject", 24);
	private static final Icon openRdaProjectIcon = GuiUtils.getIcon("openRawDataAnalysisProject", 24);
	private static final Icon saveRdaProjectIcon = GuiUtils.getIcon("saveRawDataAnalysisProject", 24);		
	private static final Icon closeProjectIcon = GuiUtils.getIcon("close", 24);
	private static final Icon exitIcon = GuiUtils.getIcon("shutDown", 24);	
	private static final Icon idTrackerLoginIcon = GuiUtils.getIcon("idTrackerLogin", 24);
	private static final Icon activeUserIcon = GuiUtils.getIcon("activeUser", 24);
	private static final Icon superUserIcon = GuiUtils.getIcon("superUser", 24);
	private static final Icon loggedOutUserIcon = GuiUtils.getIcon("loggedOutUser", 24);
	private static final Icon manageUsersIcon = GuiUtils.getIcon("manageUsers", 24);
	private static final Icon organizationIcon = GuiUtils.getIcon("organization", 24);	
	private static final Icon msToolboxIcon = GuiUtils.getIcon("toolbox", 24);
	private static final Icon chemModIcon = GuiUtils.getIcon("chemModList", 24);
	private static final Icon refSampleIcon = GuiUtils.getIcon("standardSample", 24);
	private static final Icon assayManagerIcon = GuiUtils.getIcon("acqMethod", 24);	
	private static final Icon preferencesIcon = GuiUtils.getIcon("preferences", 24);
	private static final Icon helpIcon = GuiUtils.getIcon("help", 24);
	private static final Icon dataFileToolsIcon = GuiUtils.getIcon("dataFileTools", 24);
	private static final Icon aboutIcon = GuiUtils.getIcon("infoGreen", 24);
	
	// Menus
	private JMenu
		projectMenu,
		panelsMenu,
		toolsMenu,
		dbAccessMenu,
		preferencesMenu,
		helpMenu;

	// Project items
	private JMenuItem
		newProjectMenuItem,
		newIDProjectMenuItem,
		openProjectMenuItem,
		openRdaProjectMenuItem,
		saveProjectMenuItem,
		saveProjectAsMenuItem,				
		editIDProjectMenuItem,		
		closeProjectMenuItem,		
		exitMenuItem;

	// Tools menu items
	private JMenuItem
		msToolsMenuItem,
		adductManagerMenuItem,
		rawDataToolsMenuItem;

	// dbAccess
	private JMenuItem
		loginMenuItem,
		userManagerMenuItem,
		organizationManagerMenuItem;

	// Preferences
	private JMenuItem
		preferencesMenuItem;
	
	//	Help
	private JMenuItem
		helpMenuItem,
		aboutSoftwareMenuItem;

	public MainMenuBar(ActionListener listener) {

		super(listener);

		// Project
		projectMenu = new JMenu("Project");
//		projectMenu.setPreferredSize(preferredSize);
		
		if(BuildInformation.getStartupConfiguration().equals(StartupConfiguration.COMPLETE_TOOLBOX)) {

			newProjectMenuItem = addItem(projectMenu, 
					MainActionCommands.NEW_PROJECT_COMMAND, newProjectIcon);
		}
		newIDProjectMenuItem = addItem(projectMenu, 
				MainActionCommands.NEW_RAW_DATA_PROJECT_SETUP_COMMAND, newRdaProjectIcon);
		
		projectMenu.addSeparator();

		openProjectMenuItem = addItem(projectMenu, 
				MainActionCommands.OPEN_PROJECT_COMMAND, openProjectIcon);
		
		openRdaProjectMenuItem = addItem(projectMenu, 
				MainActionCommands.OPEN_RAW_DATA_PROJECT_COMMAND, openRdaProjectIcon);
		
		projectMenu.addSeparator();
		
		editIDProjectMenuItem = addItem(projectMenu, 
				MainActionCommands.EDIT_RAW_DATA_PROJECT_SETUP_COMMAND, editRdaProjectIcon);		
		editIDProjectMenuItem.setEnabled(false);
		
		saveProjectMenuItem = addItem(projectMenu, 
				MainActionCommands.SAVE_PROJECT_COMMAND, saveProjectIcon);
		saveProjectAsMenuItem = addItem(projectMenu, 
				MainActionCommands.SAVE_PROJECT_COPY_COMMAND, saveProjectCopyIcon);
		saveProjectAsMenuItem.setEnabled(false);
		
		projectMenu.addSeparator();
		
		closeProjectMenuItem = addItem(projectMenu, 
				MainActionCommands.CLOSE_PROJECT_COMMAND, closeProjectIcon);
		projectMenu.addSeparator();
		
		exitMenuItem = addItem(projectMenu, 
				MainActionCommands.EXIT_COMMAND, exitIcon);
		add(projectMenu);
		
		// Panels
		panelsMenu = new JMenu("Panels");
//		panelsMenu.setPreferredSize(preferredSize);
		add(panelsMenu);
		
		//	Tools
		toolsMenu = new JMenu("Tools");
//		toolsMenu.setPreferredSize(preferredSize);
		msToolsMenuItem = addItem(toolsMenu, 
				MainActionCommands.SHOW_MS_TOOLBOX_COMMAND, msToolboxIcon);
		adductManagerMenuItem = addItem(toolsMenu, 
				MainActionCommands.SHOW_CHEM_MOD_EDITOR_COMMAND, chemModIcon);
		rawDataToolsMenuItem = addItem(toolsMenu, 
				MainActionCommands.SHOW_RAW_DATA_FILE_TOOLS_COMMAND, dataFileToolsIcon);
		add(toolsMenu);
		
		//	DB Access
		dbAccessMenu = new JMenu("Database access");
//		dbAccessMenu.setPreferredSize(preferredSize);
		
		loginMenuItem = addItem(dbAccessMenu, 
				MainActionCommands.SHOW_ID_TRACKER_LOGIN_COMMAND, loggedOutUserIcon);
		userManagerMenuItem = addItem(dbAccessMenu, 
				MainActionCommands.SHOW_USER_MANAGER_COMMAND, manageUsersIcon);
		userManagerMenuItem.setEnabled(false);
		organizationManagerMenuItem = addItem(dbAccessMenu, 
				MainActionCommands.SHOW_ORGANIZATION_MANAGER_COMMAND, organizationIcon);
		organizationManagerMenuItem.setEnabled(false);
		
		add(dbAccessMenu);
		
		//	Preferences
		preferencesMenu = new JMenu("Preferences");
//		preferencesMenu.setPreferredSize(preferredSize);
		preferencesMenuItem = addItem(preferencesMenu, 
				MainActionCommands.EDIT_PREFERENCES_COMMAND, preferencesIcon);
		
		add(preferencesMenu);
		
		//	Help
		helpMenu = new JMenu("Help");
//		helpMenu.setPreferredSize(preferredSize);
		helpMenuItem = addItem(helpMenu, 
				MainActionCommands.SHOW_HELP_COMMAND, helpIcon);
		helpMenuItem.setEnabled(false);
		
		aboutSoftwareMenuItem = addItem(helpMenu, 
				MainActionCommands.ABOUT_BOX_COMMAND, aboutIcon);
		
		add(helpMenu);
	}
	
	public void setIdTrackerUser(LIMSUser user) {

		if(user == null) {
			loginMenuItem.setIcon(loggedOutUserIcon);
			loginMenuItem.setActionCommand(MainActionCommands.SHOW_ID_TRACKER_LOGIN_COMMAND.getName());
			loginMenuItem.setToolTipText(MainActionCommands.SHOW_ID_TRACKER_LOGIN_COMMAND.getName());
			loginMenuItem.setText(MainActionCommands.SHOW_ID_TRACKER_LOGIN_COMMAND.getName());
			organizationManagerMenuItem.setEnabled(false);
			userManagerMenuItem.setEnabled(false);
		}
		else {
			loginMenuItem.setIcon(activeUserIcon);
			loginMenuItem.setActionCommand(MainActionCommands.ID_TRACKER_LOGOUT_COMMAND.getName());
			loginMenuItem.setToolTipText(MainActionCommands.ID_TRACKER_LOGOUT_COMMAND.getName());
			loginMenuItem.setText(user.getFullName() + " (click to log out)");
			if(user.isSuperUser()) {
				userManagerMenuItem.setEnabled(true);
				organizationManagerMenuItem.setEnabled(true);
				loginMenuItem.setIcon(superUserIcon);
			}
		}		
	}

	public void refreshPanelsMenu(HashMap<PanelList, Boolean> activePanelMap) {

		panelsMenu.removeAll();

		for (PanelList panel : PanelList.getPanelListForConfiguration(BuildInformation.getStartupConfiguration())) {

			JCheckBoxMenuItem cbMenuItem = new JCheckBoxMenuItem(panel.getName(), activePanelMap.get(panel));
			cbMenuItem.addActionListener(alistener);
			cbMenuItem.setActionCommand(panel.getName());
			panelsMenu.add(cbMenuItem);
		}
	}

	public void updateMenuFromProject(DataAnalysisProject currentProject, DataPipeline activePipeline) {
		// TODO Auto-generated method stub

	}
}

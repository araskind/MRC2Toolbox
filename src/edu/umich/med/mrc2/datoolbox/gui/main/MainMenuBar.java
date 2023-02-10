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

import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.util.HashMap;

import javax.swing.Icon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

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
	private static final int MASK =
		    Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();	

	// Icons
	private static final Icon newExperimentIcon = GuiUtils.getIcon("newProject", 24);
	private static final Icon openExperimentIcon = GuiUtils.getIcon("open", 24);
	private static final Icon saveExperimentIcon = GuiUtils.getIcon("save", 24);
	private static final Icon saveExperimentCopyIcon = GuiUtils.getIcon("saveAs", 24);
	private static final Icon newRdaExperimentIcon = GuiUtils.getIcon("newRawDataAnalysisProject", 24);
	private static final Icon editRdaExperimentIcon = GuiUtils.getIcon("editRawDataAnalysisProject", 24);
	private static final Icon openRdaExperimentIcon = GuiUtils.getIcon("openRawDataAnalysisProject", 24);
	private static final Icon saveRdaExperimentIcon = GuiUtils.getIcon("saveRawDataAnalysisProject", 24);		
	private static final Icon closeExperimentIcon = GuiUtils.getIcon("close", 24);
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
		experimentMenu,
		panelsMenu,
		toolsMenu,
		dbAccessMenu,
		preferencesMenu,
		helpMenu;

	// Experiment items
	private JMenuItem
		newExperimentMenuItem,
		newIDExperimentMenuItem,
		openExperimentMenuItem,
		openRdaExperimentMenuItem,
		saveExperimentMenuItem,
		saveExperimentAsMenuItem,				
		editIDExperimentMenuItem,		
		closeExperimentMenuItem,		
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

		// Experiment
		experimentMenu = new JMenu("Experiment");

		if(BuildInformation.getStartupConfiguration().equals(StartupConfiguration.COMPLETE_TOOLBOX)) {

			newExperimentMenuItem = addItem(experimentMenu, 
					MainActionCommands.NEW_METABOLOMICS_EXPERIMENT_COMMAND, newExperimentIcon);
			newExperimentMenuItem.setAccelerator(KeyStroke.getKeyStroke('N', MASK | InputEvent.SHIFT_DOWN_MASK));
		}
		newIDExperimentMenuItem = addItem(experimentMenu, 
				MainActionCommands.NEW_RAW_DATA_EXPERIMENT_SETUP_COMMAND, newRdaExperimentIcon);
		newIDExperimentMenuItem.setAccelerator(KeyStroke.getKeyStroke('N', MASK));
		
		experimentMenu.addSeparator();

		if(BuildInformation.getStartupConfiguration().equals(StartupConfiguration.COMPLETE_TOOLBOX)) {
			
			openExperimentMenuItem = addItem(experimentMenu, 
					MainActionCommands.OPEN_METABOLOMICS_EXPERIMENT_COMMAND, openExperimentIcon);
			openExperimentMenuItem.setAccelerator(KeyStroke.getKeyStroke('O', MASK | InputEvent.SHIFT_DOWN_MASK));
		}
		openRdaExperimentMenuItem = addItem(experimentMenu, 
				MainActionCommands.OPEN_RAW_DATA_EXPERIMENT_COMMAND, openRdaExperimentIcon);
		openRdaExperimentMenuItem.setAccelerator(KeyStroke.getKeyStroke('O', MASK));
		
		experimentMenu.addSeparator();
		
		editIDExperimentMenuItem = addItem(experimentMenu, 
				MainActionCommands.EDIT_RAW_DATA_EXPERIMENT_SETUP_COMMAND, editRdaExperimentIcon);		
		editIDExperimentMenuItem.setEnabled(false);
		
		saveExperimentMenuItem = addItem(experimentMenu, 
				MainActionCommands.SAVE_EXPERIMENT_COMMAND, saveExperimentIcon);
		saveExperimentMenuItem.setAccelerator(KeyStroke.getKeyStroke('S', MASK));
		
		saveExperimentAsMenuItem = addItem(experimentMenu, 
				MainActionCommands.SAVE_EXPERIMENT_COPY_COMMAND, saveExperimentCopyIcon);
		saveExperimentAsMenuItem.setEnabled(false);
		
		experimentMenu.addSeparator();
		
		closeExperimentMenuItem = addItem(experimentMenu, 
				MainActionCommands.CLOSE_EXPERIMENT_COMMAND, closeExperimentIcon);
		closeExperimentMenuItem.setAccelerator(KeyStroke.getKeyStroke('W', MASK));
		
		experimentMenu.addSeparator();
		
		exitMenuItem = addItem(experimentMenu, 
				MainActionCommands.EXIT_COMMAND, exitIcon);
		exitMenuItem.setAccelerator(KeyStroke.getKeyStroke('Q', MASK));
		
		add(experimentMenu);
		
		//	Tools
		toolsMenu = new JMenu("Tools");
		msToolsMenuItem = addItem(toolsMenu, 
				MainActionCommands.SHOW_MS_TOOLBOX_COMMAND, msToolboxIcon);
		msToolsMenuItem.setAccelerator(KeyStroke.getKeyStroke('T', MASK));
		
		adductManagerMenuItem = addItem(toolsMenu, 
				MainActionCommands.SHOW_CHEM_MOD_EDITOR_COMMAND, chemModIcon);
		adductManagerMenuItem.setAccelerator(KeyStroke.getKeyStroke('M', MASK));	
		
		rawDataToolsMenuItem = addItem(toolsMenu, 
				MainActionCommands.SHOW_RAW_DATA_FILE_TOOLS_COMMAND, dataFileToolsIcon);
		rawDataToolsMenuItem.setAccelerator(KeyStroke.getKeyStroke('R', MASK));	
		
		add(toolsMenu);
		
		//	DB Access
		dbAccessMenu = new JMenu("Database access");
//		dbAccessMenu.setPreferredSize(preferredSize);
		
		loginMenuItem = addItem(dbAccessMenu, 
				MainActionCommands.SHOW_IDTRACKER_LOGIN_COMMAND, loggedOutUserIcon);
		userManagerMenuItem = addItem(dbAccessMenu, 
				MainActionCommands.SHOW_USER_MANAGER_COMMAND, manageUsersIcon);
		userManagerMenuItem.setEnabled(false);
		organizationManagerMenuItem = addItem(dbAccessMenu, 
				MainActionCommands.SHOW_ORGANIZATION_MANAGER_COMMAND, organizationIcon);
		organizationManagerMenuItem.setEnabled(false);
		
		add(dbAccessMenu);

		// Panels
		panelsMenu = new JMenu("Panels");
		add(panelsMenu);
		
		//	Preferences
		preferencesMenu = new JMenu("Preferences");
		preferencesMenuItem = addItem(preferencesMenu, 
				MainActionCommands.EDIT_PREFERENCES_COMMAND, preferencesIcon);
		preferencesMenuItem.setAccelerator(KeyStroke.getKeyStroke('P', MASK | InputEvent.SHIFT_DOWN_MASK));
		
		add(preferencesMenu);
		
		//	Help
		helpMenu = new JMenu("Help");
		helpMenuItem = addItem(helpMenu, 
				MainActionCommands.SHOW_HELP_COMMAND, helpIcon);
		helpMenuItem.setEnabled(false);
		
		aboutSoftwareMenuItem = addItem(helpMenu, 
				MainActionCommands.ABOUT_BOX_COMMAND, aboutIcon);
		aboutSoftwareMenuItem.setAccelerator(KeyStroke.getKeyStroke('I', MASK));	
		
		add(helpMenu);
	}
	
	public void setIdTrackerUser(LIMSUser user) {
		
		if(user == null) {
			loginMenuItem.setIcon(loggedOutUserIcon);
			loginMenuItem.setActionCommand(MainActionCommands.SHOW_IDTRACKER_LOGIN_COMMAND.getName());
			loginMenuItem.setToolTipText(MainActionCommands.SHOW_IDTRACKER_LOGIN_COMMAND.getName());
			loginMenuItem.setText(MainActionCommands.SHOW_IDTRACKER_LOGIN_COMMAND.getName());
			organizationManagerMenuItem.setEnabled(false);
			userManagerMenuItem.setEnabled(false);
			
			for(int i=0; i<this.getMenuCount(); i++) {
				
				JMenu m = this.getMenu(i);
				if(!m.equals(dbAccessMenu))
					m.setEnabled(false);
			}			
		}
		else {
			loginMenuItem.setIcon(activeUserIcon);
			loginMenuItem.setActionCommand(MainActionCommands.IDTRACKER_LOGOUT_COMMAND.getName());
			loginMenuItem.setToolTipText(MainActionCommands.IDTRACKER_LOGOUT_COMMAND.getName());
			loginMenuItem.setText(user.getFullName() + " (click to log out)");
			if(user.isSuperUser()) {
				userManagerMenuItem.setEnabled(true);
				organizationManagerMenuItem.setEnabled(true);
				loginMenuItem.setIcon(superUserIcon);
			}
			else {
				userManagerMenuItem.setEnabled(false);
				organizationManagerMenuItem.setEnabled(false);
			}
			for(int i=0; i<this.getMenuCount(); i++)				
				this.getMenu(i).setEnabled(true);
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

	public void updateMenuFromExperiment(DataAnalysisProject currentProject, DataPipeline activePipeline) {
		// TODO Auto-generated method stub

	}
}

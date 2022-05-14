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

package edu.umich.med.mrc2.datoolbox.gui.idtlims.software;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.util.prefs.Preferences;

import javax.swing.Icon;
import javax.swing.JScrollPane;

import edu.umich.med.mrc2.datoolbox.gui.idtlims.AbstractIDTrackerLimsPanel;
import edu.umich.med.mrc2.datoolbox.gui.idtlims.IDTrackerLimsManagerPanel;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class DockableSoftwareManagerPanel extends AbstractIDTrackerLimsPanel {

	private static final Icon componentIcon = GuiUtils.getIcon("software", 16);
	private static final Icon addSoftwareIcon = GuiUtils.getIcon("addSoftware", 24);
	private static final Icon editSoftwareIcon = GuiUtils.getIcon("editSoftware", 24);
	private static final Icon deleteSoftwareIcon = GuiUtils.getIcon("deleteSoftware", 24);
	private SoftwareManagerToolbar toolbar;
	private SoftwareTable softwareTable;
	
	public DockableSoftwareManagerPanel(IDTrackerLimsManagerPanel idTrackerLimsManager) {

		super(idTrackerLimsManager, "DockableSoftwareManagerPanel", 
				componentIcon, "Software", null, Permissions.MIN_MAX_STACK);

		setCloseable(false);
		setLayout(new BorderLayout(0, 0));

		toolbar = new SoftwareManagerToolbar(this);
		getContentPane().add(toolbar, BorderLayout.NORTH);

		softwareTable =  new SoftwareTable();
		JScrollPane designScrollPane = new JScrollPane(softwareTable);
		getContentPane().add(designScrollPane, BorderLayout.CENTER);
		
		initActions();
	}

	@Override
	protected void initActions() {

		super.initActions();
		
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.ADD_SOFTWARE_COMMAND.getName(),
				MainActionCommands.ADD_SOFTWARE_COMMAND.getName(), 
				addSoftwareIcon, this));
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.EDIT_SOFTWARE_COMMAND.getName(),
				MainActionCommands.EDIT_SOFTWARE_COMMAND.getName(), 
				editSoftwareIcon, this));
		
		menuActions.addSeparator();
		
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.DELETE_SOFTWARE_COMMAND.getName(),
				MainActionCommands.DELETE_SOFTWARE_COMMAND.getName(), 
				deleteSoftwareIcon, this));
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {

		if(!isConnected())
			return;

		String command = e.getActionCommand();		
		if(command.equals(MainActionCommands.ADD_SOFTWARE_COMMAND.getName())) {
			//	showSoftwareEditor(null);
		}
		if(command.equals(MainActionCommands.EDIT_SOFTWARE_COMMAND.getName())) {

//			StockSample sample = stockSampleTable.getSelectedSample();
//			if(sample != null)
//				showSoftwareEditor(sample);	
		}		
		if(command.equals(MainActionCommands.SAVE_SOFTWARE_DETAILS_COMMAND.getName())) {
			//	saveSoftwareDetails();
		}
		if(command.equals(MainActionCommands.DELETE_SOFTWARE_COMMAND.getName())) {
			//	deleteSoftware();
		}
	}

	@Override
	public void loadPreferences(Preferences preferences) {
		// TODO Auto-generated method stub

	}

	@Override
	public void loadPreferences() {
		// TODO Auto-generated method stub

	}

	@Override
	public void savePreferences() {
		// TODO Auto-generated method stub

	}
}

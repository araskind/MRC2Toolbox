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

package edu.umich.med.mrc2.datoolbox.gui.labnote;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;

import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSInstrument;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSUser;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.CommonToolbar;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class LabNoteToolbar extends CommonToolbar {

	/**
	 *
	 */
	private static final long serialVersionUID = -3482138543981141677L;
	private static final Icon idTrackerLoginIcon = GuiUtils.getIcon("idTrackerLogin", 32);
	private static final Icon idTrackerUserIcon = GuiUtils.getIcon("idTrackerUser", 32);
	private static final Icon loggedOutUserIcon = GuiUtils.getIcon("loggedOutUser", 32);
	private static final Icon instrumentIcon = GuiUtils.getIcon("msInstrument", 32);
	private static final Icon newAnnotationIcon = GuiUtils.getIcon("newFeatureSubset", 32);
	private static final Icon editAnnotationIcon = GuiUtils.getIcon("editCollection", 32);
	private static final Icon deleteAnnotationIcon = GuiUtils.getIcon("deleteCollection", 32);
	private static final Icon searchAnnotationIcon = GuiUtils.getIcon("searchDatabase", 32);

	@SuppressWarnings("unused")
	private JButton
		idTrackerLoginButton,
		idTrackerUserButton,
		instrumentButton,
		newAnnotationButton,
		editAnnotationButton,
		deleteAnnotationButton,
		searchAnnotationButton;

	private JLabel loggedUserLabel;
	private JLabel instrumentLabel;

	public LabNoteToolbar(ActionListener commandListener) {
		super(commandListener);

		newAnnotationButton = GuiUtils.addButton(this, null, newAnnotationIcon, commandListener,
				MainActionCommands.CREATE_NEW_LAB_NOTE_COMMAND.getName(),
				MainActionCommands.CREATE_NEW_LAB_NOTE_COMMAND.getName(), buttonDimension);

		editAnnotationButton = GuiUtils.addButton(this, null, editAnnotationIcon, commandListener,
				MainActionCommands.EDIT_LAB_NOTE_COMMAND.getName(),
				MainActionCommands.EDIT_LAB_NOTE_COMMAND.getName(), buttonDimension);

		deleteAnnotationButton = GuiUtils.addButton(this, null, deleteAnnotationIcon, commandListener,
				MainActionCommands.DELETE_LAB_NOTE_COMMAND.getName(),
				MainActionCommands.DELETE_LAB_NOTE_COMMAND.getName(), buttonDimension);

		addSeparator(buttonDimension);

//		searchAnnotationButton = GuiUtils.addButton(this, null, searchAnnotationIcon, commandListener,
//				MainActionCommands.DELETE_LAB_NOTE_COMMAND.getName(),
//				MainActionCommands.DELETE_LAB_NOTE_COMMAND.getName(), buttonDimension);
//
//		addSeparator(buttonDimension);

		Component horizontalGlue = Box.createHorizontalGlue();
		add(horizontalGlue);

		idTrackerUserButton =
				GuiUtils.addButton(this, null, loggedOutUserIcon, commandListener, null, null, buttonDimension);
		loggedUserLabel = new JLabel("");
		loggedUserLabel.setFont(new Font("Tahoma", Font.BOLD, 12));
		add(loggedUserLabel);

		addSeparator(buttonDimension);

		instrumentButton = GuiUtils.addButton(this, null, instrumentIcon, commandListener,
				MainActionCommands.SHOW_MS_INSTRUMENT_SELECTOR_COMMAND.getName(),
				MainActionCommands.SHOW_MS_INSTRUMENT_SELECTOR_COMMAND.getName(), buttonDimension);

		instrumentLabel = new JLabel("");
		instrumentLabel.setForeground(Color.BLUE);
		instrumentLabel.setFont(new Font("Tahoma", Font.BOLD, 12));
		add(instrumentLabel);
	}

	public void setIdTrackerUser(LIMSUser user) {

		if(user == null) {
			idTrackerUserButton.setIcon(idTrackerLoginIcon);
			idTrackerUserButton.setActionCommand(MainActionCommands.SHOW_IDTRACKER_LOGIN_COMMAND.getName());
			idTrackerUserButton.setToolTipText(MainActionCommands.SHOW_IDTRACKER_LOGIN_COMMAND.getName());
			loggedUserLabel.setText("");
		}
		else {
			idTrackerUserButton.setIcon(idTrackerUserIcon);
			idTrackerUserButton.setActionCommand(MainActionCommands.IDTRACKER_LOGOUT_COMMAND.getName());
			idTrackerUserButton.setToolTipText(MainActionCommands.IDTRACKER_LOGOUT_COMMAND.getName());
			loggedUserLabel.setText(user.getFullName());
		}
	}

	@Override
	public void updateGuiFromExperimentAndDataPipeline(DataAnalysisProject project, DataPipeline newDataPipeline) {
		// TODO Auto-generated method stub

	}

	public void setInstrument(LIMSInstrument activeInstrument) {

		if(activeInstrument == null)
			instrumentLabel.setText("");
		else
			instrumentLabel.setText(activeInstrument.getInstrumentName() +
					" (" + activeInstrument.getInstrumentId() +")");
	}

}

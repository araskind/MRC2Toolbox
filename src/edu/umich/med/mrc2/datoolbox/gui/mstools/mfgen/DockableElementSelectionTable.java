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

package edu.umich.med.mrc2.datoolbox.gui.mstools.mfgen;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class DockableElementSelectionTable  extends DefaultSingleCDockable implements ActionListener{

	private ElementSelectionTable elementSelectionTable;
	private JButton btnEditElementsList;
	private JButton resetButton;

	private static final Icon componentIcon = GuiUtils.getIcon("table", 16);

	public DockableElementSelectionTable() {

		super("DockableElementSelectionTable", componentIcon, "Set element number limits", null, Permissions.MIN_MAX_STACK);
		setCloseable(false);

		setLayout(new BorderLayout(0, 0));

		elementSelectionTable = new ElementSelectionTable();
		add(new JScrollPane(elementSelectionTable), BorderLayout.CENTER);

		JPanel buttonPanel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) buttonPanel.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);

		btnEditElementsList = new JButton(MainActionCommands.EDIT_ELEMENT_LIST_COMMAND.getName());
		btnEditElementsList.setActionCommand(MainActionCommands.EDIT_ELEMENT_LIST_COMMAND.getName());
		btnEditElementsList.addActionListener(this);
		buttonPanel.add(btnEditElementsList);

		resetButton = new JButton(MainActionCommands.RESET_ELEMENTS_COMMAND.getName());
		resetButton.setActionCommand(MainActionCommands.RESET_ELEMENTS_COMMAND.getName());
		resetButton.addActionListener(this);
		buttonPanel.add(resetButton);

		add(buttonPanel, BorderLayout.SOUTH);
	}

	/**
	 * @return the elementSelectionTable
	 */
	public ElementSelectionTable getTable() {
		return elementSelectionTable;
	}

	@Override
	public void actionPerformed(ActionEvent event) {

		String command = event.getActionCommand();

		if (command.equals(MainActionCommands.RESET_ELEMENTS_COMMAND.getName()))
			elementSelectionTable.resetElementLimitsToDefault();

		if (command.equals(MainActionCommands.EDIT_ELEMENT_LIST_COMMAND.getName())) {
			//	TODO
		}
	}

}

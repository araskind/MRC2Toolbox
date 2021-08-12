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

package edu.umich.med.mrc2.datoolbox.gui.projectsetup.expdesign.editor;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JToolBar;

import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class ReorderingToolbar extends JToolBar {

	/**
	 *
	 */
	private static final long serialVersionUID = -7978720437576581103L;

	private static final Icon goTopIcon = GuiUtils.getIcon("goTop", 24);
	private static final Icon goUpIcon = GuiUtils.getIcon("goUp", 24);
	private static final Icon goDownIcon = GuiUtils.getIcon("goDown", 24);
	private static final Icon goBottomIcon = GuiUtils.getIcon("goBottom", 24);
	private static final Icon editIcon = GuiUtils.getIcon("edit", 24);
	private static final Icon renameIcon = GuiUtils.getIcon("rename", 24);

	public enum DataEditMode {

		EDIT,
		RENAME;
	}

	private JButton
		goTopButton,
		goUpButton,
		goDownButton,
		goBottomButton,
		editButton;

	protected static final Dimension buttonDimension = new Dimension(26, 26);
	protected ActionListener commandListener;

	public ReorderingToolbar(ActionListener actionListener, DataEditMode mode, String editActionCommand) {

		super(JToolBar.VERTICAL);
		this.commandListener = actionListener;

		//setLayout(new FlowLayout(FlowLayout.LEFT));
		setOpaque(false);
		setFloatable(false);
		setBackground(Color.LIGHT_GRAY);
		setAlignmentX(Component.LEFT_ALIGNMENT);

		goTopButton = GuiUtils.addButton(this, null, goTopIcon, commandListener,
				MainActionCommands.MOVE_TO_TOP_COMMAND.getName(),
				MainActionCommands.MOVE_TO_TOP_COMMAND.getName(), buttonDimension);

		goUpButton = GuiUtils.addButton(this, null, goUpIcon, commandListener,
				MainActionCommands.MOVE_UP_COMMAND.getName(),
				MainActionCommands.MOVE_UP_COMMAND.getName(), buttonDimension);

		goDownButton = GuiUtils.addButton(this, null, goDownIcon, commandListener,
				MainActionCommands.MOVE_DOWN_COMMAND.getName(),
				MainActionCommands.MOVE_DOWN_COMMAND.getName(), buttonDimension);

		goBottomButton = GuiUtils.addButton(this, null, goBottomIcon, commandListener,
				MainActionCommands.MOVE_TO_BOTTOM_COMMAND.getName(),
				MainActionCommands.MOVE_TO_BOTTOM_COMMAND.getName(), buttonDimension);

//		addSeparator();
//
//		editButton = GuiUtils.addButton(this, null, editIcon, commandListener,
//				editActionCommand,
//				editActionCommand, buttonDimension);

//		if(mode.equals(DataEditMode.RENAME))
//			editButton.setIcon(renameIcon);
	}

	public void setEditingAllowed(boolean allowEdit) {

		goTopButton.setEnabled(allowEdit);
		goUpButton.setEnabled(allowEdit);
		goDownButton.setEnabled(allowEdit);
		goBottomButton.setEnabled(allowEdit);
//		editButton.setEnabled(allowEdit);
	}
}






















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

package edu.umich.med.mrc2.datoolbox.gui.idtlims.dacq.mobph;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JToolBar;

import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class MobilePhaseSynonymsToolbar extends JToolBar{

	/**
	 *
	 */
	private static final long serialVersionUID = -4239285828590902572L;

	private static final Icon addLevelIcon = GuiUtils.getIcon("addCollection", 24);
	private static final Icon deleteLevelIcon = GuiUtils.getIcon("deleteCollection", 24);

	@SuppressWarnings("unused")
	private JButton
		addLevelButton,
		removeLevelButton;

	protected static final Dimension buttonDimension = new Dimension(26, 26);
	protected ActionListener commandListener;

	public MobilePhaseSynonymsToolbar(ActionListener actionListener) {

		super(JToolBar.VERTICAL);
		this.commandListener = actionListener;

		setOpaque(false);
		setFloatable(false);
		setBackground(Color.LIGHT_GRAY);
		setAlignmentX(Component.LEFT_ALIGNMENT);

		addLevelButton = GuiUtils.addButton(this, null, addLevelIcon, commandListener,
				MainActionCommands.ADD_MOBILE_PHASE_SYNONYM_COMMAND.getName(),
				MainActionCommands.ADD_MOBILE_PHASE_SYNONYM_COMMAND.getName(), buttonDimension);

		removeLevelButton = GuiUtils.addButton(this, null, deleteLevelIcon, commandListener,
				MainActionCommands.DELETE_MOBILE_PHASE_SYNONYM_COMMAND.getName(),
				MainActionCommands.DELETE_MOBILE_PHASE_SYNONYM_COMMAND.getName(), buttonDimension);
	}
}

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

package edu.umich.med.mrc2.datoolbox.gui.io.excel;

import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;

import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.CommonToolbar;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class ExcelWorksheetPreviewToolbar extends CommonToolbar {

	/**
	 *
	 */
	private static final long serialVersionUID = 3073109513983956403L;
	private static final Icon enableSelectedIcon = GuiUtils.getIcon("checkboxFull", 32);
	private static final Icon disableSelectedIcon = GuiUtils.getIcon("checkboxEmpty", 32);
	private static final Icon enableAllIcon = GuiUtils.getIcon("enableAll", 32);
	private static final Icon disableAllIcon = GuiUtils.getIcon("disableAll", 32);
	private static final Icon invertEnabledIcon = GuiUtils.getIcon("invertSelection", 32);

	@SuppressWarnings("unused")
	private JButton
		enableSelectedButton,
		disableSelectedButton,
		enableAllButton,
		disableAllButton,
		invertEnabledButton;

	private JComboBox directionComboBox;

	public ExcelWorksheetPreviewToolbar(ActionListener commandListener) {

		super(commandListener);

		enableSelectedButton = GuiUtils.addButton(this, null, enableSelectedIcon, commandListener,
				MainActionCommands.ENABLE_SELECTED_SAMPLES_COMMAND.getName(),
				MainActionCommands.ENABLE_SELECTED_SAMPLES_COMMAND.getName(), buttonDimension);
		disableSelectedButton = GuiUtils.addButton(this, null, disableSelectedIcon, commandListener,
				MainActionCommands.DISABLE_SELECTED_SAMPLES_COMMAND.getName(),
				MainActionCommands.DISABLE_SELECTED_SAMPLES_COMMAND.getName(), buttonDimension);
		enableAllButton = GuiUtils.addButton(this, null, enableAllIcon, commandListener,
				MainActionCommands.ENABLE_ALL_SAMPLES_COMMAND.getName(),
				MainActionCommands.ENABLE_ALL_SAMPLES_COMMAND.getName(), buttonDimension);
		disableAllButton = GuiUtils.addButton(this, null, disableAllIcon, commandListener,
				MainActionCommands.DISABLE_ALL_SAMPLES_COMMAND.getName(),
				MainActionCommands.DISABLE_ALL_SAMPLES_COMMAND.getName(), buttonDimension);
		invertEnabledButton = GuiUtils.addButton(this, null, invertEnabledIcon, commandListener,
				MainActionCommands.INVERT_ENABLED_SAMPLES_COMMAND.getName(),
				MainActionCommands.INVERT_ENABLED_SAMPLES_COMMAND.getName(), buttonDimension);

		directionComboBox = new JComboBox(new DefaultComboBoxModel<>(DataDirection.values()));
		directionComboBox.setMinimumSize(new Dimension(150, 30));
		directionComboBox.setMaximumSize(new Dimension(150, 30));
		directionComboBox.setPreferredSize(new Dimension(150, 30));
		directionComboBox.setSize(new Dimension(150, 30));
		add(directionComboBox);
		directionComboBox.addItemListener((ItemListener) commandListener);
	}

	@Override
	public void updateGuiFromProjectAndDataPipeline(DataAnalysisProject project, DataPipeline newDataPipeline) {
		// TODO Auto-generated method stub

	}

	public DataDirection getDataDirection() {
		return (DataDirection)directionComboBox.getSelectedItem();
	}
}















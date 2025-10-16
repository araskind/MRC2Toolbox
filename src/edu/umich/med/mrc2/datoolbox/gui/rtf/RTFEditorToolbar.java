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

package edu.umich.med.mrc2.datoolbox.gui.rtf;

import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JButton;

import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.CommonToolbar;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class RTFEditorToolbar extends CommonToolbar{

	/**
	 *
	 */
	private static final long serialVersionUID = 1012090993122208276L;

	private final static Icon undoIcon = GuiUtils.getIcon("edit-undo", 24);
	private final static Icon redoIcon = GuiUtils.getIcon("edit-redo", 24);

	private final static Icon copyIcon = GuiUtils.getIcon("copy", 24);
	private final static Icon cutIcon = GuiUtils.getIcon("cut", 24);
	private final static Icon pasteIcon = GuiUtils.getIcon("paste", 24);

	private final static Icon findIcon = GuiUtils.getIcon("find", 24);
	private final static Icon findReplaceIcon = GuiUtils.getIcon("findReplace", 24);

	private final static Icon alignLeftIcon = GuiUtils.getIcon("format-justify-left", 24);
	private final static Icon alignCenterIcon = GuiUtils.getIcon("format-justify-center", 24);
	private final static Icon alignRightIcon = GuiUtils.getIcon("format-justify-right", 24);
	private final static Icon justifyIcon = GuiUtils.getIcon("format-justify-fill", 24);

	private final static Icon indentLessIcon = GuiUtils.getIcon("format-indent-less", 24);
	private final static Icon indentMoreIcon = GuiUtils.getIcon("format-indent-more", 24);

	private final static Icon boldIcon = GuiUtils.getIcon("format-text-bold", 24);
	private final static Icon italicIcon = GuiUtils.getIcon("format-text-italic", 24);
	private final static Icon strikethroughIcon = GuiUtils.getIcon("format-text-strikethrough", 24);
	private final static Icon underlineIcon = GuiUtils.getIcon("format-text-underline", 24);

	private final static Icon colorPickerIcon = GuiUtils.getIcon("colorPicker", 24);
	private final static Icon fontSelectIcon = GuiUtils.getIcon("fontOtf", 24);
	private final static Icon paragraphIcon = GuiUtils.getIcon("format-paragraph", 24);

	@SuppressWarnings("unused")
	private JButton
		copyButton,
		cutButton,
		pasteButton,
		alignLeftButton,
		alignCenterButton,
		alignRightButton,
		justifyButton,
		indentLessButton,
		indentMoreButton,
		boldButton,
		italicButton,
		strikethroughButton,
		underlineButton,
		colorPickerButton,
		fontSelectButton,
		paragraphButton;

	public RTFEditorToolbar(ActionListener commandListener) {
		super(commandListener);

		copyButton = GuiUtils.addButton(this, null, copyIcon, commandListener,
				MainActionCommands.RTF_COPY_COMMAND.getName(),
				MainActionCommands.RTF_COPY_COMMAND.getName(), buttonDimension);

		cutButton = GuiUtils.addButton(this, null, cutIcon, commandListener,
				MainActionCommands.RTF_CUT_COMMAND.getName(),
				MainActionCommands.RTF_CUT_COMMAND.getName(), buttonDimension);

		pasteButton = GuiUtils.addButton(this, null, pasteIcon, commandListener,
				MainActionCommands.RTF_PASTE_COMMAND.getName(),
				MainActionCommands.RTF_PASTE_COMMAND.getName(), buttonDimension);

		addSeparator(buttonDimension);

		alignLeftButton = GuiUtils.addButton(this, null, alignLeftIcon, commandListener,
				MainActionCommands.ALIGN_LEFT_COMMAND.getName(),
				MainActionCommands.ALIGN_LEFT_COMMAND.getName(), buttonDimension);

		alignCenterButton = GuiUtils.addButton(this, null, alignCenterIcon, commandListener,
				MainActionCommands.ALIGN_CENTER_COMMAND.getName(),
				MainActionCommands.ALIGN_CENTER_COMMAND.getName(), buttonDimension);

		alignRightButton = GuiUtils.addButton(this, null, alignRightIcon, commandListener,
				MainActionCommands.ALIGN_RIGHT_COMMAND.getName(),
				MainActionCommands.ALIGN_RIGHT_COMMAND.getName(), buttonDimension);

		justifyButton = GuiUtils.addButton(this, null, justifyIcon, commandListener,
				MainActionCommands.JUSTIFY_COMMAND.getName(),
				MainActionCommands.JUSTIFY_COMMAND.getName(), buttonDimension);

		addSeparator(buttonDimension);

		indentLessButton = GuiUtils.addButton(this, null, indentLessIcon, commandListener,
				MainActionCommands.DECREASE_INDENT_COMMAND.getName(),
				MainActionCommands.DECREASE_INDENT_COMMAND.getName(), buttonDimension);

		indentMoreButton = GuiUtils.addButton(this, null, indentMoreIcon, commandListener,
				MainActionCommands.INCREASE_INDENT_COMMAND.getName(),
				MainActionCommands.INCREASE_INDENT_COMMAND.getName(), buttonDimension);

		addSeparator(buttonDimension);

		boldButton = GuiUtils.addButton(this, null, boldIcon, commandListener,
				MainActionCommands.BOLD_FONT_COMMAND.getName(),
				MainActionCommands.BOLD_FONT_COMMAND.getName(), buttonDimension);

		italicButton = GuiUtils.addButton(this, null, italicIcon, commandListener,
				MainActionCommands.ITALIC_FONT_COMMAND.getName(),
				MainActionCommands.ITALIC_FONT_COMMAND.getName(), buttonDimension);

		strikethroughButton = GuiUtils.addButton(this, null, strikethroughIcon, commandListener,
				MainActionCommands.STRIKETHROUGH_FONT_COMMAND.getName(),
				MainActionCommands.STRIKETHROUGH_FONT_COMMAND.getName(), buttonDimension);

		underlineButton = GuiUtils.addButton(this, null, underlineIcon, commandListener,
				MainActionCommands.UNDERLINE_FONT_COMMAND.getName(),
				MainActionCommands.UNDERLINE_FONT_COMMAND.getName(), buttonDimension);

		addSeparator(buttonDimension);

		colorPickerButton = GuiUtils.addButton(this, null, colorPickerIcon, commandListener,
				MainActionCommands.COLORPICKER_COMMAND.getName(),
				MainActionCommands.COLORPICKER_COMMAND.getName(), buttonDimension);

		fontSelectButton = GuiUtils.addButton(this, null, fontSelectIcon, commandListener,
				MainActionCommands.FONT_SELECTOR_COMMAND.getName(),
				MainActionCommands.FONT_SELECTOR_COMMAND.getName(), buttonDimension);

		paragraphButton = GuiUtils.addButton(this, null, paragraphIcon, commandListener,
				MainActionCommands.PARAGRAPH_EDITOR_COMMAND.getName(),
				MainActionCommands.PARAGRAPH_EDITOR_COMMAND.getName(), buttonDimension);
	}

	@Override
	public void updateGuiFromExperimentAndDataPipeline(
			DataAnalysisProject experiment, DataPipeline newDataPipeline) {
		// TODO Auto-generated method stub

	}

}

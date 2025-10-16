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

package edu.umich.med.mrc2.datoolbox.gui.utils;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;

public class MessageDialog {

	private static final Icon strongWarningIcon = GuiUtils.getIcon("stopSign", 64);

	/**
	 *
	 * @param yesNoQuestion
	 * @return JOptionPane.NO_OPTION or JOptionPane.YES_OPTION
	 */
	public static int showChoiceMsg(String yesNoQuestion) {
		return JOptionPane.showConfirmDialog(MRC2ToolBoxCore.getMainWindow(), yesNoQuestion, "Please choose",
				JOptionPane.YES_NO_OPTION);
	}

	public static int showChooseOrCancelMsg(String yesNoQuestion) {
		return JOptionPane.showConfirmDialog(MRC2ToolBoxCore.getMainWindow(), yesNoQuestion, "Please choose",
				JOptionPane.YES_NO_CANCEL_OPTION);
	}

	public static int showChoiceWithWarningMsg(String yesNoQuestion) {

		return JOptionPane.showConfirmDialog(MRC2ToolBoxCore.getMainWindow(), yesNoQuestion, "Attention!",
				JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, strongWarningIcon);
	}

	public static void showErrorMsg(String message) {
		JOptionPane.showMessageDialog(MRC2ToolBoxCore.getMainWindow(), message, "Error", JOptionPane.ERROR_MESSAGE);
	}

	public static void showInfoMsg(String message) {

		final JPanel panel = new JPanel();
		JOptionPane.showMessageDialog(panel, message, "Information", JOptionPane.INFORMATION_MESSAGE);
	}

	public static void showInfoMsg(String message, Component parent) {

		JOptionPane.showMessageDialog(parent, message, "Information", JOptionPane.INFORMATION_MESSAGE);
	}

	public static void showWarningMsg(String message) {
		JOptionPane.showMessageDialog(MRC2ToolBoxCore.getMainWindow(), message, "Warning", JOptionPane.WARNING_MESSAGE);
	}

	public static int showChoiceMsg(String yesNoQuestion, Component parent) {
		return JOptionPane.showConfirmDialog(parent, yesNoQuestion, "Please choose", JOptionPane.YES_NO_OPTION);
	}

	public static int showChooseOrCancelMsg(String yesNoQuestion, Component parent) {
		return JOptionPane.showConfirmDialog(parent, yesNoQuestion, "Please choose", JOptionPane.YES_NO_CANCEL_OPTION);
	}

	public static int showChoiceWithWarningMsg(String yesNoQuestion, Component parent) {

		return JOptionPane.showConfirmDialog(parent, yesNoQuestion, "Attention!", JOptionPane.YES_NO_OPTION,
				JOptionPane.WARNING_MESSAGE, strongWarningIcon);
	}

	public static void showErrorMsg(String message, Component parent) {
		JOptionPane.showMessageDialog(parent, message, "Error", JOptionPane.ERROR_MESSAGE);
	}

	public static void showWarningMsg(String message, Component parent) {
		JOptionPane.showMessageDialog(parent, message, "Warning", JOptionPane.WARNING_MESSAGE);
	}
}

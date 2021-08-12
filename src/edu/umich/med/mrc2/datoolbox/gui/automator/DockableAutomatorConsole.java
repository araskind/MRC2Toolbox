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

package edu.umich.med.mrc2.datoolbox.gui.automator;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.Icon;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class DockableAutomatorConsole  extends DefaultSingleCDockable{

	private static final Icon componentIcon = GuiUtils.getIcon("actions", 16);
	private JTextArea consoleTextArea;
	private JScrollPane areaScrollPane;

	public DockableAutomatorConsole() {

		super("DockableAutomatorConsole", componentIcon, "Console", null, Permissions.MIN_MAX_STACK);
		setCloseable(false);
		setLayout(new BorderLayout(0,0));

		consoleTextArea = new JTextArea();
		areaScrollPane = new JScrollPane(consoleTextArea);
		areaScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		areaScrollPane.setPreferredSize(new Dimension(600, 250));

		add(areaScrollPane, BorderLayout.CENTER);
	}

	public void setText(String string) {
		consoleTextArea.setText(string);
	}

	/**
	 * @return the consoleTextArea
	 */
	public JTextArea getConsoleTextArea() {
		return consoleTextArea;
	}
}

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

package edu.umich.med.mrc2.datoolbox.gui.labnote;

import java.awt.BorderLayout;

import javax.swing.Icon;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class DockableLabNotePlainTextViewer extends DefaultSingleCDockable {

	private static final Icon componentIcon = GuiUtils.getIcon("text", 16);
	private JTextArea textArea;

	public DockableLabNotePlainTextViewer() {

		super("DockableLabNotePlainTextViewer", 
				componentIcon, "Plain text", null, Permissions.MIN_MAX_STACK);
		setCloseable(false);

		textArea = new JTextArea();
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		textArea.setEditable(false);
		JScrollPane designScrollPane = new JScrollPane(textArea);
		getContentPane().add(designScrollPane, BorderLayout.CENTER);
	}

	public void setAnnotationText(String text) {
		textArea.setText(text);
	}

	public synchronized void clearPanel() {
		textArea.setText("");
	}

	public String getText() {
		return textArea.getText();
	}

	public void setEditable(boolean b) {
		textArea.setEditable(b);
	}

	public void focusOntext() {
		textArea.grabFocus();
	}
}

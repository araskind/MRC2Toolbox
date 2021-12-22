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

package edu.umich.med.mrc2.datoolbox.gui.rtf;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JRootPane;
import javax.swing.text.Document;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import edu.umich.med.mrc2.datoolbox.gui.rtf.jwp.JWordProcessor;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import rtf.AdvancedRTFEditorKit;

public class DockableRTFEditor extends DefaultSingleCDockable implements ActionListener{

	private static final Icon componentIcon = GuiUtils.getIcon("rtf", 16);
	private JWordProcessor wordProcessor;

	public DockableRTFEditor(String id, String panelTitle, boolean displayOnly) {

		super(id, componentIcon, panelTitle, null, Permissions.MIN_MAX_STACK);
		setCloseable(false);

		JRootPane panel = new JRootPane();
		getContentPane().add(panel, BorderLayout.CENTER);
		wordProcessor = new JWordProcessor(displayOnly);
		panel.getContentPane().add(wordProcessor, BorderLayout.CENTER);

		if(!displayOnly) {
			panel.getContentPane().add(wordProcessor.getF_toolBar(), BorderLayout.NORTH);
			panel.setJMenuBar(wordProcessor.getMenubar());
		}
	}

	@Override
	public void actionPerformed(ActionEvent event) {

		String command = event.getActionCommand();


	}

	/**
	 * @return the wordProcessor
	 */
	public JWordProcessor getWordProcessor() {
		return wordProcessor;
	}

	public synchronized void clearPanel() {
		wordProcessor.clearPanel();
	}

	public Document getDocument() {
		return wordProcessor.getStyledDocument();
	}

    public void loadDocument(Document doc) {
    	wordProcessor.loadDocument(doc);
    }

	/**
	 * @return the w_kit
	 */
	public AdvancedRTFEditorKit getRTFEditorKit() {
		return wordProcessor.getRTFEditorKit();
	}
}

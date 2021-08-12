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

package edu.umich.med.mrc2.datoolbox.gui.cpddatabase.dataimport;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JRootPane;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class DatabaseCompoundImportDialog extends JDialog {

	/**
	 *
	 */
	private static final long serialVersionUID = 4116589640338682352L;
	private FileImportPanel fileImportPanel;
	private NetworkImportPanel networkImportPanel;

	private static final Icon importLibraryToDbIcon = GuiUtils.getIcon("importLibraryToDb", 32);

	public DatabaseCompoundImportDialog(ActionListener listener) {

		super();
		setTitle("Import new compound(s) in the database");
		setIconImage(((ImageIcon) importLibraryToDbIcon).getImage());

		setSize(new Dimension(600, 480));
		setPreferredSize(new Dimension(600, 480));
		setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		getContentPane().setLayout(new BorderLayout(0, 0));

		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		getContentPane().add(tabbedPane, BorderLayout.CENTER);

		fileImportPanel = new FileImportPanel(listener);
		tabbedPane.addTab("From file", null, fileImportPanel, null);

		networkImportPanel = new NetworkImportPanel(listener);
		tabbedPane.addTab("From on-line resource", null, networkImportPanel, null);

		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				setVisible(false);
			}
		};
		JRootPane rootPane = SwingUtilities.getRootPane(tabbedPane);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);

		pack();
		setVisible(false);
	}

}

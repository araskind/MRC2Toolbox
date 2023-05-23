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

package edu.umich.med.mrc2.datoolbox.gui.cpddatabase.msready;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.prefs.Preferences;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;

public class TautomerGeneratorSettingsDialog extends JDialog implements BackedByPreferences {

	/**
	 *
	 */
	private static final long serialVersionUID = 5041872153346492046L;
	private static final Icon dialogIcon = GuiUtils.getIcon("tautomerSettings", 32);
	//
	private Preferences preferences;

	public TautomerGeneratorSettingsDialog(ActionListener listener) {
		super();
		setTitle("Tautomer Generator Settings");
		setIconImage(((ImageIcon) dialogIcon).getImage());
		setPreferredSize(new Dimension(640, 480));
		setSize(new Dimension(640, 480));
		setModalityType(ModalityType.APPLICATION_MODAL);
		setResizable(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		JPanel panel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		getContentPane().add(panel, BorderLayout.SOUTH);

		JButton btnCancel = new JButton("Cancel");
		panel.add(btnCancel);
		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				dispose();
			}
		};
		btnCancel.addActionListener(al);

		JButton btnSave = new JButton(
				MainActionCommands.SAVE_TAUTOMER_GENERATOR_SETTINGS_COMMAND.getName());
		btnSave.setActionCommand(
				MainActionCommands.SAVE_TAUTOMER_GENERATOR_SETTINGS_COMMAND.getName());
		btnSave.addActionListener(listener);
		panel.add(btnSave);
		JRootPane rootPane = SwingUtilities.getRootPane(btnSave);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(btnSave);

		loadPreferences();
		pack();
	}
	
	@Override
	public void loadPreferences(Preferences prefs) {
		
		preferences = prefs;
//		baseDirectory =
//			new File(preferences.get(BASE_DIRECTORY,
//				MRC2ToolBoxConfiguration.getDefaultDataDirectory()));
	}

	@Override
	public void loadPreferences(){
		
		loadPreferences(Preferences.userRoot().node(
				MRC2ToolBoxConfiguration.TAUTOMER_GENERATOR_PREFERENCES_NODE));
	}

	@Override
	public void savePreferences() {
		
		preferences = Preferences.userRoot().node(
				MRC2ToolBoxConfiguration.TAUTOMER_GENERATOR_PREFERENCES_NODE);
	//	preferences.put(BASE_DIRECTORY, baseDirectory.getAbsolutePath());
	}
}

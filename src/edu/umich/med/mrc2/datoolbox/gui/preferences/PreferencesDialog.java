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

package edu.umich.med.mrc2.datoolbox.gui.preferences;

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
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import bibliothek.gui.dock.common.CControl;
import bibliothek.gui.dock.common.CGrid;
import bibliothek.gui.dock.common.theme.ThemeMap;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;

public class PreferencesDialog extends JDialog implements BackedByPreferences, ActionListener{

	/**
	 *
	 */
	private static final long serialVersionUID = -5548488892954773492L;

	private static final Icon preferencesIcon = GuiUtils.getIcon("preferences", 32);

	private Preferences prefs;
	private DockableGeneralPreferencesPanel generalPreferencesPanel;
	private DockableDatabasePreferencesPanel databasePreferencesPanel;
	private DockableTemplatesPanel templatesPanel;
	private DockableDataParsingPanel dataParsingPanel;
	private JButton cancelButton;
	private JButton saveButton;
	private JFileChooser chooser;
	private CControl control;
	private CGrid grid;

	public PreferencesDialog() {

		super(MRC2ToolBoxCore.getMainWindow(), "Application preferences", true);
		setIconImage(((ImageIcon) preferencesIcon).getImage());

		setSize(new Dimension(640, 480));
		setPreferredSize(new Dimension(640, 480));
		setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		getContentPane().setLayout(new BorderLayout(0, 0));

		generalPreferencesPanel = new DockableGeneralPreferencesPanel();
		databasePreferencesPanel = new DockableDatabasePreferencesPanel();
		templatesPanel = new DockableTemplatesPanel();
		dataParsingPanel = new DockableDataParsingPanel();

		control = new CControl( MRC2ToolBoxCore.getMainWindow());
		control.setTheme(ThemeMap.KEY_ECLIPSE_THEME);
		grid = new CGrid( control );
		grid.add( 0, 0, 1, 1, generalPreferencesPanel, 
				databasePreferencesPanel, templatesPanel, dataParsingPanel);
		control.getContentArea().deploy( grid );
		add(control.getContentArea(), BorderLayout.CENTER);
		control.getController().setFocusedDockable(generalPreferencesPanel.intern(), false);

		JPanel buttonPanel = new JPanel();
		FlowLayout fl_buttonPanel = (FlowLayout) buttonPanel.getLayout();
		fl_buttonPanel.setAlignment(FlowLayout.RIGHT);
		buttonPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);

		cancelButton = new JButton("Cancel");
		buttonPanel.add(cancelButton);

		saveButton = new JButton("Save changes");
		saveButton.setActionCommand(MainActionCommands.SAVE_PREFERENCES_COMMAND.getName());
		saveButton.addActionListener(this);
		buttonPanel.add(saveButton);

		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				setVisible(false);
			}
		};
		cancelButton.addActionListener(al);
		JRootPane rootPane = SwingUtilities.getRootPane(saveButton);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(saveButton);

		pack();
		setVisible(false);
	}

	@Override
	public void actionPerformed(ActionEvent event) {

		String command = event.getActionCommand();

		if (command.equals(MainActionCommands.SAVE_PREFERENCES_COMMAND.getName()))
			savePreferences();
	}

	@Override
	public void loadPreferences(Preferences preferences) {

		prefs = preferences;

		generalPreferencesPanel.loadPreferences(preferences);
		templatesPanel.loadPreferences(preferences);
		dataParsingPanel.loadPreferences(preferences);
	}

	@Override
	public void loadPreferences() {

		if(prefs != null)
			loadPreferences(prefs);
	}

	@Override
	public void savePreferences() {

		generalPreferencesPanel.savePreferences();
		templatesPanel.savePreferences();
		dataParsingPanel.savePreferences();

		this.setVisible(false);

		if(MRC2ToolBoxCore.getCurrentProject() == null || 
				MRC2ToolBoxCore.getCurrentProject().getActiveDataPipeline() == null) 
			return;

		MRC2ToolBoxCore.getMainWindow().switchDataPipeline(
				MRC2ToolBoxCore.getCurrentProject(),
				MRC2ToolBoxCore.getCurrentProject().getActiveDataPipeline());		
	}
}

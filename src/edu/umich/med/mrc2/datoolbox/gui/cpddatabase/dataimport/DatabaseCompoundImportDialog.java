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

package edu.umich.med.mrc2.datoolbox.gui.cpddatabase.dataimport;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.prefs.Preferences;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.jnafilechooser.api.JnaFileChooser;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;

public class DatabaseCompoundImportDialog extends JDialog 
	implements ActionListener, BackedByPreferences {

	/**
	 *
	 */
	private static final long serialVersionUID = 4116589640338682352L;
	private NetworkImportPanel networkImportPanel;

	private static final Icon importLibraryToDbIcon = GuiUtils.getIcon("importLibraryToDb", 32);
	private Preferences preferences;
	public static final String PREFS_NODE = "edu.umich.med.mrc2.datoolbox.DatabaseCompoundImportDialog";
	public static final String BASE_DIRECTORY = "BASE_DIRECTORY";
	private File baseDirectory;
	
	private JTextField compoundFileTextField;
	private static final String BROWSE_FOR_COMPOUND_FILE = "BROWSE_FOR_COMPOUND_FILE";

	public DatabaseCompoundImportDialog(ActionListener listener) {

		super();
		setTitle("Import new compound(s) in the database");
		setIconImage(((ImageIcon) importLibraryToDbIcon).getImage());

		setSize(new Dimension(600, 480));
		setPreferredSize(new Dimension(600, 300));
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		getContentPane().setLayout(new BorderLayout(0, 0));

		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		getContentPane().add(tabbedPane, BorderLayout.CENTER);

		//	fileImportPanel = new FileImportPanel(listener);
		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new EmptyBorder(10, 10, 10, 10));
		tabbedPane.addTab("From file", null, panel_1, null);
		GridBagLayout gbl_panel_1 = new GridBagLayout();
		gbl_panel_1.columnWidths = new int[]{0, 221, 0, 0};
		gbl_panel_1.rowHeights = new int[]{0, 0, 0};
		gbl_panel_1.columnWeights = new double[]{1.0, 1.0, 1.0, Double.MIN_VALUE};
		gbl_panel_1.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		panel_1.setLayout(gbl_panel_1);
		
		compoundFileTextField = new JTextField();
		compoundFileTextField.setEditable(false);
		GridBagConstraints gbc_compoundFileTextField = new GridBagConstraints();
		gbc_compoundFileTextField.insets = new Insets(0, 0, 5, 0);
		gbc_compoundFileTextField.gridwidth = 3;
		gbc_compoundFileTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_compoundFileTextField.gridx = 0;
		gbc_compoundFileTextField.gridy = 0;
		panel_1.add(compoundFileTextField, gbc_compoundFileTextField);
		compoundFileTextField.setColumns(10);
		
		JButton selectFileButton = new JButton("Select file");
		selectFileButton.setActionCommand(BROWSE_FOR_COMPOUND_FILE);
		selectFileButton.addActionListener(this);
		GridBagConstraints gbc_selectFileButton = new GridBagConstraints();
		gbc_selectFileButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_selectFileButton.gridx = 2;
		gbc_selectFileButton.gridy = 1;
		panel_1.add(selectFileButton, gbc_selectFileButton);

		networkImportPanel = new NetworkImportPanel(listener);
		tabbedPane.addTab("From on-line resource", null, networkImportPanel, null);
		
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

		JButton btnSave = 
				new JButton(MainActionCommands.IMPORT_COMPOUNDS_TO_DATABASE_COMMAND.getName());
		btnSave.setActionCommand(
				MainActionCommands.IMPORT_COMPOUNDS_TO_DATABASE_COMMAND.getName());
		btnSave.addActionListener(listener);
		panel.add(btnSave);
		JRootPane rootPane = SwingUtilities.getRootPane(btnSave);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(btnSave);
		
		pack();
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		if(e.getActionCommand().equals(BROWSE_FOR_COMPOUND_FILE))
			selectCompoundFile();
	}
	
	private void selectCompoundFile() {
		
		JnaFileChooser fc = new JnaFileChooser(baseDirectory);
		fc.setMode(JnaFileChooser.Mode.Files);
		fc.addFilter("SDF files", "sdf", "SDF");
		fc.addFilter("XML files", "xml", "XML");
		fc.setTitle("Import compound data");
		fc.setMultiSelectionEnabled(false);
		if (fc.showOpenDialog(this)) {
			
			File cpdFile = fc.getSelectedFile();
			compoundFileTextField.setText(cpdFile.getAbsolutePath());			
			baseDirectory = cpdFile.getParentFile();
			savePreferences();
		}
	}
	
	@Override
	public void loadPreferences(Preferences prefs) {
		preferences = prefs;
		baseDirectory =  
				new File(preferences.get(BASE_DIRECTORY, 
						MRC2ToolBoxConfiguration.getDefaultDataDirectory()));
	}

	@Override
	public void loadPreferences() {
		loadPreferences(Preferences.userRoot().node(PREFS_NODE));
	}

	@Override
	public void savePreferences() {
		preferences = Preferences.userRoot().node(PREFS_NODE);
		preferences.put(BASE_DIRECTORY, baseDirectory.getAbsolutePath());
	}
}

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

package edu.umich.med.mrc2.datoolbox.gui.worklist;

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
import java.nio.file.Paths;
import java.util.prefs.Preferences;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
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

public class WorklistImportDialog extends JDialog 
		implements ActionListener, BackedByPreferences{

	/**
	 * 
	 */
	private static final long serialVersionUID = 8716421349210019279L;
	protected static final Icon dialogIcon = GuiUtils.getIcon("editCollection", 32);
	private File baseDirectory;
	private Preferences preferences;
	public static final String PREFS_NODE = WorklistImportDialog.class.getName();
	public static final String BASE_DIRECTORY = "BASE_DIRECTORY";
	public static final String BROWSE = "BROWSE";
	
	private WorklistPanel worklistPanel;
	private boolean appendWorklist;
	private JTextField inputFileTextField;

	public WorklistImportDialog(WorklistPanel worklistPanel, boolean appendWorklist) {
		super();
		setPreferredSize(new Dimension(640, 150));
		
		setTitle("Import worklist from file");
		setIconImage(((ImageIcon)dialogIcon).getImage());
		setModalityType(ModalityType.APPLICATION_MODAL);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setSize(new Dimension(640, 150));
		setResizable(true);
		getContentPane().setLayout(new BorderLayout(0, 0));
		this.appendWorklist = appendWorklist;
		this.worklistPanel = worklistPanel;

		JPanel panel1 = new JPanel();
		panel1.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(panel1, BorderLayout.CENTER);
		GridBagLayout gbl_dataPanel = new GridBagLayout();
		gbl_dataPanel.columnWidths = new int[]{517, 0, 0};
		gbl_dataPanel.rowHeights = new int[]{0, 0, 0};
		gbl_dataPanel.columnWeights = new double[]{1.0, 1.0, Double.MIN_VALUE};
		gbl_dataPanel.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		panel1.setLayout(gbl_dataPanel);
		
		JLabel lblNewLabel = new JLabel("Worklist file:");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 0;
		panel1.add(lblNewLabel, gbc_lblNewLabel);
		
		inputFileTextField = new JTextField();
		inputFileTextField.setEditable(false);
		GridBagConstraints gbc_inputFileTextField = new GridBagConstraints();
		gbc_inputFileTextField.insets = new Insets(0, 0, 0, 5);
		gbc_inputFileTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_inputFileTextField.gridx = 0;
		gbc_inputFileTextField.gridy = 1;
		panel1.add(inputFileTextField, gbc_inputFileTextField);
		inputFileTextField.setColumns(10);
		
		JButton btnNewButton = new JButton("Browse");
		btnNewButton.setActionCommand(BROWSE);
		btnNewButton.addActionListener(this);
		GridBagConstraints gbc_btnNewButton = new GridBagConstraints();
		gbc_btnNewButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnNewButton.gridx = 1;
		gbc_btnNewButton.gridy = 1;
		panel1.add(btnNewButton, gbc_btnNewButton);
		
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
		
		String command = MainActionCommands.LOAD_WORKLIST_COMMAND.getName();
		if(appendWorklist)
			command = MainActionCommands.ADD_WORKLIST_COMMAND.getName();

		JButton btnSave = new JButton(command);
		btnSave.setActionCommand(MainActionCommands.LOAD_WORKLIST_COMMAND.getName());
		btnSave.addActionListener(this);
		panel.add(btnSave);
		JRootPane rootPane = SwingUtilities.getRootPane(btnSave);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(btnSave);
		loadPreferences();
		pack();
	}
	
	@Override
	public void dispose() {		
		savePreferences();
		super.dispose();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
	
		if(e.getActionCommand().equals(BROWSE))
				selectWorklistFile();
			
		if(e.getActionCommand().equals(MainActionCommands.LOAD_WORKLIST_COMMAND.getName()))
			loadWorklistFromFile();			
	}
	
	private void selectWorklistFile() {
		
		JnaFileChooser fc = new JnaFileChooser(baseDirectory);
		fc.setMode(JnaFileChooser.Mode.Files);
		fc.addFilter("Agilent worklist files", "wkl", "WKL");
		fc.addFilter("Text files", "txt", "TXT", "tsv", "TSV");
		fc.setTitle("Select worklist file");
		fc.setMultiSelectionEnabled(false);
		if (fc.showOpenDialog(this)) {
			
			File inputFile = fc.getSelectedFile();			
			inputFileTextField.setText(inputFile.getAbsolutePath());
			baseDirectory = inputFile.getParentFile();		
			savePreferences();	
		}
	}
	
	private void loadWorklistFromFile() {
		
		if(inputFileTextField.getText().isEmpty())
			return;
		
		File inputFile = Paths.get(inputFileTextField.getText()).toFile();
		worklistPanel.loadWorklistFromFile(inputFile, appendWorklist);
		dispose();
	}

	@Override
	public void loadPreferences(Preferences prefs) {
		preferences = prefs;
		baseDirectory =  
				new File(preferences.get(BASE_DIRECTORY, 
						MRC2ToolBoxConfiguration.getDefaultExperimentsDirectory()));
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

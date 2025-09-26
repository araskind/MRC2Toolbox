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

package edu.umich.med.mrc2.datoolbox.gui.integration.mcr;

import java.awt.BorderLayout;
import java.awt.Color;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.prefs.Preferences;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import org.apache.commons.lang3.StringUtils;

import edu.umich.med.mrc2.datoolbox.gui.io.MinimalDataFileListingTable;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;

public class MetabCombinerScriptDialog extends JDialog implements ActionListener, BackedByPreferences {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static final Icon dialogIcon = GuiUtils.getIcon("rScriptMC", 32);
	private Preferences preferences;
	public static final String WORK_DIRECTORY = "WORK_DIRECTORY";
	public static final String BROWSE_COMMAND = "Browse";
	
	private File workDirectory;
	private MetabCombinerScriptDialogToolbar toolbar;
	private JTextField workDirectoryTextField;
	private MinimalDataFileListingTable fileListingTable;
	
	public MetabCombinerScriptDialog() {
		super();
		setTitle("Generate MetabCombiner script for multiple batch alignment");
		setIconImage(((ImageIcon) dialogIcon).getImage());
		setPreferredSize(new Dimension(1000, 800));
		setSize(new Dimension(1000, 800));
		setModalityType(ModalityType.APPLICATION_MODAL);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		
		toolbar =new MetabCombinerScriptDialogToolbar(this);
		getContentPane().add(toolbar, BorderLayout.NORTH);

		JPanel dataPanel = new JPanel();
		dataPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(dataPanel, BorderLayout.CENTER);
		GridBagLayout gbl_dataPanel = new GridBagLayout();
		gbl_dataPanel.columnWidths = new int[]{0, 0, 105, 0, 0};
		gbl_dataPanel.rowHeights = new int[]{0, 0, 0, 0, 0, 0};
		gbl_dataPanel.columnWeights = new double[]{0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_dataPanel.rowWeights = new double[]{0.0, 1.0, 0.0, 1.0, 1.0, Double.MIN_VALUE};
		dataPanel.setLayout(gbl_dataPanel);
		
		JLabel lblNewLabel = new JLabel("Work directory");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 0;
		dataPanel.add(lblNewLabel, gbc_lblNewLabel);
		
		workDirectoryTextField = new JTextField();	
		workDirectoryTextField.setEditable(false);
		GridBagConstraints gbc_textField = new GridBagConstraints();
		gbc_textField.gridwidth = 2;
		gbc_textField.insets = new Insets(0, 0, 5, 5);
		gbc_textField.fill = GridBagConstraints.HORIZONTAL;
		gbc_textField.gridx = 1;
		gbc_textField.gridy = 0;
		dataPanel.add(workDirectoryTextField, gbc_textField);
		workDirectoryTextField.setColumns(10);
		
		JButton selectWorkingDirButton = new JButton(BROWSE_COMMAND);
		selectWorkingDirButton.setActionCommand(BROWSE_COMMAND);
		selectWorkingDirButton.addActionListener(this);
		GridBagConstraints gbc_btnNewButton = new GridBagConstraints();
		gbc_btnNewButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnNewButton.insets = new Insets(0, 0, 5, 5);
		gbc_btnNewButton.gridx = 3;
		gbc_btnNewButton.gridy = 0;
		dataPanel.add(selectWorkingDirButton, gbc_btnNewButton);
		
		fileListingTable = new MinimalDataFileListingTable();
		GridBagConstraints gbc_table = new GridBagConstraints();
		gbc_table.gridwidth = 4;
		gbc_table.insets = new Insets(0, 0, 5, 5);
		gbc_table.fill = GridBagConstraints.BOTH;
		gbc_table.gridx = 0;
		gbc_table.gridy = 1;
		JScrollPane scrollPane = new JScrollPane(fileListingTable);
		scrollPane.setBorder(new CompoundBorder(
				new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), 
				new Color(160, 160, 160)), "MetabCombiner input files for alignment", 
				TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)), new EmptyBorder(5, 5, 5, 5)));
		dataPanel.add(scrollPane, gbc_table);
		
		JButton selectMCFilesButton = new JButton(
				MainActionCommands.SELECT_METAB_COMBINER_INPUT_FILES_COMMAND.getName());
		selectMCFilesButton.setActionCommand(
				MainActionCommands.SELECT_METAB_COMBINER_INPUT_FILES_COMMAND.getName());
		selectMCFilesButton.addActionListener(this);
		
		JButton clearMCFilesButton = new JButton(
				MainActionCommands.CLEAR_METAB_COMBINER_INPUT_FILES_COMMAND.getName());
		clearMCFilesButton.setActionCommand(
				MainActionCommands.CLEAR_METAB_COMBINER_INPUT_FILES_COMMAND.getName());
		clearMCFilesButton.addActionListener(this);
		
		GridBagConstraints gbc_btnNewButton_1 = new GridBagConstraints();
		gbc_btnNewButton_1.anchor = GridBagConstraints.EAST;
		gbc_btnNewButton_1.insets = new Insets(0, 0, 5, 5);
		gbc_btnNewButton_1.gridx = 1;
		gbc_btnNewButton_1.gridy = 2;
		dataPanel.add(clearMCFilesButton, gbc_btnNewButton_1);
		GridBagConstraints gbc_selectMCFilesButton = new GridBagConstraints();
		gbc_selectMCFilesButton.gridwidth = 2;
		gbc_selectMCFilesButton.anchor = GridBagConstraints.EAST;
		gbc_selectMCFilesButton.insets = new Insets(0, 0, 5, 5);
		gbc_selectMCFilesButton.gridx = 2;
		gbc_selectMCFilesButton.gridy = 2;
		dataPanel.add(selectMCFilesButton, gbc_selectMCFilesButton);
		
		JPanel dataImportParametersPanel = new JPanel();
		dataImportParametersPanel.setBorder(new CompoundBorder(
				new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), 
				new Color(160, 160, 160)), "Data import and preprocessing parameters", 
				TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)), new EmptyBorder(5, 5, 5, 5)));
		dataPanel.add(scrollPane, gbc_table);
		GridBagConstraints gbc_panel_1 = new GridBagConstraints();
		gbc_panel_1.gridwidth = 4;
		gbc_panel_1.insets = new Insets(0, 0, 5, 5);
		gbc_panel_1.fill = GridBagConstraints.BOTH;
		gbc_panel_1.gridx = 0;
		gbc_panel_1.gridy = 3;
		dataPanel.add(dataImportParametersPanel, gbc_panel_1);
		
		JPanel alignmentAndFilteringParametersPanel = new JPanel();
		alignmentAndFilteringParametersPanel.setBorder(new CompoundBorder(
				new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), 
				new Color(160, 160, 160)), "Alignment and filtering parameters", 
				TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)), new EmptyBorder(5, 5, 5, 5)));
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.insets = new Insets(0, 0, 0, 5);
		gbc_panel.gridwidth = 4;
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 4;
		dataPanel.add(alignmentAndFilteringParametersPanel, gbc_panel);

		JPanel buttonPanel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) buttonPanel.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);

		JButton btnCancel = new JButton("Cancel");
		buttonPanel.add(btnCancel);
		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				dispose();
			}
		};
		btnCancel.addActionListener(al);

		JButton btnSave = new JButton(
				MainActionCommands.GENERATE_METAB_COMBINER_SCRIPT_COMMAND.getName());
		btnSave.setActionCommand(
				MainActionCommands.GENERATE_METAB_COMBINER_SCRIPT_COMMAND.getName());
		btnSave.addActionListener(this);
		buttonPanel.add(btnSave);
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

		String command = e.getActionCommand();
		
		if (command.equals(BROWSE_COMMAND))
			selectWorkingDirectory();
		
		if (command.equals(MainActionCommands.SELECT_METAB_COMBINER_INPUT_FILES_COMMAND.getName()))
			selectMetabCombinerInputFiles();		
		
		if (command.equals(MainActionCommands.CLEAR_METAB_COMBINER_INPUT_FILES_COMMAND.getName()))
			cleartMetabCombinerInputFiles();
		
		if (command.equals(MainActionCommands.GENERATE_METAB_COMBINER_SCRIPT_COMMAND.getName()))
			generateMetabCombinerScript();
	}

	private void selectWorkingDirectory() {
		// TODO Auto-generated method stub
		
	}

	private void selectMetabCombinerInputFiles() {
		// TODO Auto-generated method stub
		
	}
	
	private void cleartMetabCombinerInputFiles() {

		if(!fileListingTable.getAllFiles().isEmpty()) {
			
			int res = MessageDialog.showChoiceWithWarningMsg(
					"Do you want to clear input files table?", this);
			if(res == JOptionPane.YES_OPTION)
				fileListingTable.clearTable();
		}
	}

	private void generateMetabCombinerScript() {
		// TODO Auto-generated method stub
		Collection<String>errors = validateFormData();
		if(!errors.isEmpty()){
		    MessageDialog.showErrorMsg(StringUtils.join(errors, "\n"), this);
		    return;
		}
		
		
		
		//	Open script folder
		
		dispose();
	}
	
	private Collection<String>validateFormData(){
	    
	    Collection<String>errors = new ArrayList<String>();
	    
		
	    return errors;
	}


	@Override
	public void loadPreferences(Preferences preferences) {
		
		this.preferences = preferences;
		String baseDirPath = preferences.get(WORK_DIRECTORY, 
						MRC2ToolBoxConfiguration.getDefaultExperimentsDirectory());
		try {
			workDirectory = Paths.get(baseDirPath).toFile();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void loadPreferences() {
		loadPreferences(Preferences.userNodeForPackage(this.getClass()));		
	}

	@Override
	public void savePreferences() {

		preferences = Preferences.userNodeForPackage(this.getClass());
		preferences.put(WORK_DIRECTORY, workDirectory.getAbsolutePath());
	}
}

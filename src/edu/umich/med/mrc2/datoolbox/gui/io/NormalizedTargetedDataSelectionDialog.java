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

package edu.umich.med.mrc2.datoolbox.gui.io;

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
import java.util.ArrayList;
import java.util.Collection;
import java.util.prefs.Preferences;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.jnafilechooser.api.JnaFileChooser;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.utils.TextUtils;

public class NormalizedTargetedDataSelectionDialog extends JDialog implements ActionListener, BackedByPreferences {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static final Icon dialogIcon = GuiUtils.getIcon("loess", 32);
	
	private Preferences preferences;
	public static final String FILE_NAME_MASK = "FILE_NAME_MASK";
	private static final String LINES_TO_SKIP = "LINES_TO_SKIP";
	
	private File inputFile;
	private File baseLibraryDirectory;
	private JTextField inputFileTextField;
	private JTextField fileNameMaskField;
	private JSpinner lineSkipSpinner;
	
	private static final String BROWSE = "BROWSE";

	public NormalizedTargetedDataSelectionDialog(ActionListener parserListener, File baseLibraryDirectory) {
		super();
		this.baseLibraryDirectory = baseLibraryDirectory;
		setTitle("Select normalized targeted data file");
		setIconImage(((ImageIcon) dialogIcon).getImage());
		setPreferredSize(new Dimension(600, 250));
		setSize(new Dimension(600, 250));
		setModalityType(ModalityType.APPLICATION_MODAL);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		JPanel dataPanel = new JPanel();
		dataPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(dataPanel, BorderLayout.CENTER);
		GridBagLayout gbl_dataPanel = new GridBagLayout();
		gbl_dataPanel.columnWidths = new int[]{0, 0, 0, 0, 0};
		gbl_dataPanel.rowHeights = new int[]{0, 0, 0, 0, 0};
		gbl_dataPanel.columnWeights = new double[]{0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
		gbl_dataPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		dataPanel.setLayout(gbl_dataPanel);
		
		JLabel lblNewLabel = new JLabel("Input file");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.gridwidth = 2;
		gbc_lblNewLabel.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 0;
		dataPanel.add(lblNewLabel, gbc_lblNewLabel);
		
		inputFileTextField = new JTextField();
		inputFileTextField.setEditable(false);
		GridBagConstraints gbc_inputFileTextField = new GridBagConstraints();
		gbc_inputFileTextField.gridwidth = 3;
		gbc_inputFileTextField.insets = new Insets(0, 0, 5, 5);
		gbc_inputFileTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_inputFileTextField.gridx = 0;
		gbc_inputFileTextField.gridy = 1;
		dataPanel.add(inputFileTextField, gbc_inputFileTextField);
		inputFileTextField.setColumns(10);
		
		JButton btnNewButton = new JButton("Browse");
		btnNewButton.setActionCommand(BROWSE);
		btnNewButton.addActionListener(this);
		GridBagConstraints gbc_btnNewButton = new GridBagConstraints();
		gbc_btnNewButton.insets = new Insets(0, 0, 5, 0);
		gbc_btnNewButton.gridx = 3;
		gbc_btnNewButton.gridy = 1;
		dataPanel.add(btnNewButton, gbc_btnNewButton);
		
		JLabel lblNewLabel_1 = new JLabel("Ignore");
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_1.gridx = 0;
		gbc_lblNewLabel_1.gridy = 2;
		dataPanel.add(lblNewLabel_1, gbc_lblNewLabel_1);
		
		lineSkipSpinner = new JSpinner();
		lineSkipSpinner.setModel(new SpinnerNumberModel(0, 0, 10, 1));
		GridBagConstraints gbc_lineSkipSpinner = new GridBagConstraints();
		gbc_lineSkipSpinner.insets = new Insets(0, 0, 5, 5);
		gbc_lineSkipSpinner.gridx = 1;
		gbc_lineSkipSpinner.gridy = 2;
		dataPanel.add(lineSkipSpinner, gbc_lineSkipSpinner);
		
		JLabel lblNewLabel_2 = new JLabel("lines after header");
		GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
		gbc_lblNewLabel_2.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel_2.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_2.gridx = 2;
		gbc_lblNewLabel_2.gridy = 2;
		dataPanel.add(lblNewLabel_2, gbc_lblNewLabel_2);
		
		JLabel lblNewLabel_3 = new JLabel("File name mask");
		GridBagConstraints gbc_lblNewLabel_3 = new GridBagConstraints();
		gbc_lblNewLabel_3.gridwidth = 2;
		gbc_lblNewLabel_3.insets = new Insets(0, 0, 0, 5);
		gbc_lblNewLabel_3.gridx = 0;
		gbc_lblNewLabel_3.gridy = 3;
		dataPanel.add(lblNewLabel_3, gbc_lblNewLabel_3);
		
		fileNameMaskField = new JTextField();
		GridBagConstraints gbc_fileNameMaskField = new GridBagConstraints();
		gbc_fileNameMaskField.insets = new Insets(0, 0, 0, 5);
		gbc_fileNameMaskField.fill = GridBagConstraints.HORIZONTAL;
		gbc_fileNameMaskField.gridx = 2;
		gbc_fileNameMaskField.gridy = 3;
		dataPanel.add(fileNameMaskField, gbc_fileNameMaskField);
		fileNameMaskField.setColumns(10);

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
				MainActionCommands.PARSE_NORMALIZED_TARGETED_DATA_COMMAND.getName());
		btnSave.setActionCommand(
				MainActionCommands.PARSE_NORMALIZED_TARGETED_DATA_COMMAND.getName());
		btnSave.addActionListener(parserListener);
		buttonPanel.add(btnSave);
		JRootPane rootPane = SwingUtilities.getRootPane(btnSave);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(btnSave);

		pack();
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		if(e.getActionCommand().equals(BROWSE))
			selectNormalizedTargetedDataFile();
	}
	
	private void selectNormalizedTargetedDataFile() {
		
		JnaFileChooser fc = new JnaFileChooser(baseLibraryDirectory);
		fc.setTitle("Select normalized targeted data file (TAB-separated)");
		fc.setMode(JnaFileChooser.Mode.Files);
		fc.addFilter("Text files", "txt", "TXT", "tsv", "TSV");
		fc.setMultiSelectionEnabled(false);
		if (fc.showOpenDialog(this)) {
			
			inputFile = fc.getSelectedFile();
			inputFileTextField.setText(inputFile.getAbsolutePath());
		}
	}

	public File getInputFile() {
		return inputFile;
	}
	
	public String getFileNameMask() {
		return fileNameMaskField.getText().trim();
	}
	
	public int getNumberOfLinesToSkipAfterHeader() {
		return (int)lineSkipSpinner.getValue();
	}
	
	public Collection<String>validateFormData(){
	    
	    Collection<String>errors = new ArrayList<>();
	    if(inputFile == null || !inputFile.exists())
	    	errors.add("Input file not specified or invalid");
	    
	    if(getFileNameMask().isEmpty())
	    	errors.add("File name mask not specified");
	    else {
	    	if(!TextUtils.isValidRegex(getFileNameMask()))
	    		errors.add("File name mask is not a valid regular expression");
	    }	    	
	    return errors;
	} 

	@Override
	public void loadPreferences(Preferences preferences) {
		
		this.preferences = preferences;
		String fileNameMask = preferences.get(FILE_NAME_MASK, 
						MRC2ToolBoxConfiguration.CORE_DATA_FILE_MASK_DEFAULT);
		fileNameMaskField.setText(fileNameMask);
		lineSkipSpinner.setValue(preferences.getInt(LINES_TO_SKIP, 1));
	}

	@Override
	public void loadPreferences() {
		loadPreferences(Preferences.userNodeForPackage(this.getClass()));		
	}

	@Override
	public void savePreferences() {

		preferences = Preferences.userNodeForPackage(this.getClass());
		preferences.put(FILE_NAME_MASK, getFileNameMask());
		preferences.putInt(LINES_TO_SKIP, getNumberOfLinesToSkipAfterHeader());
	}
}

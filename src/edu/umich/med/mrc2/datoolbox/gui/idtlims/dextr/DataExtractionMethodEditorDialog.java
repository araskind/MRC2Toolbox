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

package edu.umich.med.mrc2.datoolbox.gui.idtlims.dextr;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
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
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import edu.umich.med.mrc2.datoolbox.data.enums.SoftwareType;
import edu.umich.med.mrc2.datoolbox.data.lims.DataExtractionMethod;
import edu.umich.med.mrc2.datoolbox.data.lims.DataProcessingSoftware;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCache;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.jnafilechooser.api.JnaFileChooser;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;

public class DataExtractionMethodEditorDialog extends JDialog implements ActionListener, BackedByPreferences{

	/**
	 *
	 */
	private static final long serialVersionUID = 7684989595475342241L;

	private Preferences preferences;
	public static final String PREFS_NODE = "edu.umich.med.mrc2.cefanalyzer.gui.DataExtractionMethodEditorDialog";
	public static final String BASE_DIRECTORY = "BASE_DIRECTORY";
	private static final String BROWSE_COMMAND = "BROWSE_COMMAND";
	private File baseDirectory;
	
	private static final Icon editMethodIcon = GuiUtils.getIcon("editDataProcessingMethod", 32);
	private static final Icon addMethodIcon = GuiUtils.getIcon("addDataProcessingMethod", 32);

	private DataExtractionMethod method;
	private DataProcessingSoftware software;
	private JButton btnSave;
	private JTextField methodFileTextField;
	private JLabel dateCreatedLabel;
	private JTextArea descriptionTextArea;
	private JButton btnBrowse;
		
	private JTextField methodNameTextField;
	private JLabel idValueLabel;
	private JLabel methodAuthorLabel;
	private JTextField softwareTextField;
	private SoftwareSelectorDialog softwareSelectorDialog;

	public DataExtractionMethodEditorDialog(DataExtractionMethod method, ActionListener actionListener) {
		super();
		setPreferredSize(new Dimension(640, 300));
		setModalityType(ModalityType.APPLICATION_MODAL);
		setResizable(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		
		this.method = method;
		JPanel dataPanel = new JPanel();
		dataPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(dataPanel, BorderLayout.CENTER);
		GridBagLayout gbl_dataPanel = new GridBagLayout();
		gbl_dataPanel.columnWidths = new int[]{0, 114, 126, 140, 0, 0};
		gbl_dataPanel.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0};
		gbl_dataPanel.columnWeights = new double[]{0.0, 1.0, 1.0, 1.0, 1.0, Double.MIN_VALUE};
		gbl_dataPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE};
		dataPanel.setLayout(gbl_dataPanel);

		JLabel lblId = new JLabel("ID");
		lblId.setForeground(Color.BLUE);
		lblId.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_lblId = new GridBagConstraints();
		gbc_lblId.anchor = GridBagConstraints.EAST;
		gbc_lblId.insets = new Insets(0, 0, 5, 5);
		gbc_lblId.gridx = 0;
		gbc_lblId.gridy = 0;
		dataPanel.add(lblId, gbc_lblId);

		idValueLabel = new JLabel("");
		GridBagConstraints gbc_idValueLabel = new GridBagConstraints();
		gbc_idValueLabel.anchor = GridBagConstraints.WEST;
		gbc_idValueLabel.insets = new Insets(0, 0, 5, 5);
		gbc_idValueLabel.gridx = 1;
		gbc_idValueLabel.gridy = 0;
		dataPanel.add(idValueLabel, gbc_idValueLabel);

		JLabel lblCreated = new JLabel("Created on");
		GridBagConstraints gbc_lblCreated = new GridBagConstraints();
		gbc_lblCreated.anchor = GridBagConstraints.EAST;
		gbc_lblCreated.insets = new Insets(0, 0, 5, 5);
		gbc_lblCreated.gridx = 3;
		gbc_lblCreated.gridy = 0;
		dataPanel.add(lblCreated, gbc_lblCreated);

		dateCreatedLabel = new JLabel("");
		GridBagConstraints gbc_dateCreatedLabel = new GridBagConstraints();
		gbc_dateCreatedLabel.fill = GridBagConstraints.HORIZONTAL;
		gbc_dateCreatedLabel.insets = new Insets(0, 0, 5, 0);
		gbc_dateCreatedLabel.gridx = 4;
		gbc_dateCreatedLabel.gridy = 0;
		dataPanel.add(dateCreatedLabel, gbc_dateCreatedLabel);

		JLabel lblCreatedBy = new JLabel("Created by");
		GridBagConstraints gbc_lblCreatedBy = new GridBagConstraints();
		gbc_lblCreatedBy.anchor = GridBagConstraints.NORTHEAST;
		gbc_lblCreatedBy.insets = new Insets(0, 0, 5, 5);
		gbc_lblCreatedBy.gridx = 0;
		gbc_lblCreatedBy.gridy = 1;
		dataPanel.add(lblCreatedBy, gbc_lblCreatedBy);

		methodAuthorLabel = new JLabel("");
		GridBagConstraints gbc_methodAuthorLabel = new GridBagConstraints();
		gbc_methodAuthorLabel.fill = GridBagConstraints.HORIZONTAL;
		gbc_methodAuthorLabel.gridwidth = 2;
		gbc_methodAuthorLabel.insets = new Insets(0, 0, 5, 5);
		gbc_methodAuthorLabel.gridx = 1;
		gbc_methodAuthorLabel.gridy = 1;
		dataPanel.add(methodAuthorLabel, gbc_methodAuthorLabel);

		JLabel lblName = new JLabel("Name");
		GridBagConstraints gbc_lblName = new GridBagConstraints();
		gbc_lblName.anchor = GridBagConstraints.EAST;
		gbc_lblName.insets = new Insets(0, 0, 5, 5);
		gbc_lblName.gridx = 0;
		gbc_lblName.gridy = 2;
		dataPanel.add(lblName, gbc_lblName);

		methodNameTextField = new JTextField();
		methodNameTextField.setEditable(false);
		GridBagConstraints gbc_methodNameTextField = new GridBagConstraints();
		gbc_methodNameTextField.gridwidth = 4;
		gbc_methodNameTextField.insets = new Insets(0, 0, 5, 0);
		gbc_methodNameTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_methodNameTextField.gridx = 1;
		gbc_methodNameTextField.gridy = 2;
		dataPanel.add(methodNameTextField, gbc_methodNameTextField);
		methodNameTextField.setColumns(10);

		JLabel lblDescription = new JLabel("Description");
		GridBagConstraints gbc_lblDescription = new GridBagConstraints();
		gbc_lblDescription.anchor = GridBagConstraints.NORTH;
		gbc_lblDescription.insets = new Insets(0, 0, 5, 5);
		gbc_lblDescription.gridx = 0;
		gbc_lblDescription.gridy = 3;
		dataPanel.add(lblDescription, gbc_lblDescription);

		descriptionTextArea = new JTextArea();
		descriptionTextArea.setRows(3);
		descriptionTextArea.setWrapStyleWord(true);
		descriptionTextArea.setLineWrap(true);
		GridBagConstraints gbc_textArea = new GridBagConstraints();
		gbc_textArea.gridwidth = 4;
		gbc_textArea.insets = new Insets(0, 0, 5, 0);
		gbc_textArea.fill = GridBagConstraints.BOTH;
		gbc_textArea.gridx = 1;
		gbc_textArea.gridy = 3;
		dataPanel.add(descriptionTextArea, gbc_textArea);
		
		softwareTextField = new JTextField();
		softwareTextField.setEditable(false);
		GridBagConstraints gbc_softwareTextField = new GridBagConstraints();
		gbc_softwareTextField.gridwidth = 4;
		gbc_softwareTextField.insets = new Insets(0, 0, 5, 5);
		gbc_softwareTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_softwareTextField.gridx = 0;
		gbc_softwareTextField.gridy = 4;
		dataPanel.add(softwareTextField, gbc_softwareTextField);
		softwareTextField.setColumns(10);
		
		JButton selectSoftButton = new JButton("Select software");
		selectSoftButton.setActionCommand(
				MainActionCommands.SHOW_SOFTWARE_SELECTOR_COMMAND.getName());		
		selectSoftButton.addActionListener(this);
		GridBagConstraints gbc_selectSoftButton = new GridBagConstraints();
		gbc_selectSoftButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_selectSoftButton.insets = new Insets(0, 0, 5, 0);
		gbc_selectSoftButton.gridx = 4;
		gbc_selectSoftButton.gridy = 4;
		dataPanel.add(selectSoftButton, gbc_selectSoftButton);

		methodFileTextField = new JTextField();
		methodFileTextField.setEditable(false);
		GridBagConstraints gbc_methodFileTextField = new GridBagConstraints();
		gbc_methodFileTextField.gridwidth = 4;
		gbc_methodFileTextField.insets = new Insets(0, 0, 0, 5);
		gbc_methodFileTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_methodFileTextField.gridx = 0;
		gbc_methodFileTextField.gridy = 5;
		dataPanel.add(methodFileTextField, gbc_methodFileTextField);
		methodFileTextField.setColumns(10);

		btnBrowse = new JButton("Select method file ...");
		btnBrowse.setActionCommand(BROWSE_COMMAND);	//	TODO
		btnBrowse.addActionListener(this);
		GridBagConstraints gbc_btnBrowse = new GridBagConstraints();
		gbc_btnBrowse.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnBrowse.gridx = 4;
		gbc_btnBrowse.gridy = 5;
		dataPanel.add(btnBrowse, gbc_btnBrowse);

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

		btnSave = new JButton("Save");
		btnSave.addActionListener(actionListener);
		panel.add(btnSave);
		JRootPane rootPane = SwingUtilities.getRootPane(btnSave);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(btnSave);

		loadMethodData();
	}

	private void loadMethodData() {

		if(method == null) {

			setTitle("Add new data extraction method");
			setIconImage(((ImageIcon) addMethodIcon).getImage());
			btnSave.setActionCommand(MainActionCommands.ADD_DATA_EXTRACTION_METHOD_COMMAND.getName());
		}
		else {
			setTitle("Edit information for " + method.getName());
			setIconImage(((ImageIcon) editMethodIcon).getImage());
			btnSave.setActionCommand(MainActionCommands.EDIT_DATA_EXTRACTION_METHOD_COMMAND.getName());
			idValueLabel.setText(method.getId());

			if (method.getCreatedOn() != null)
				dateCreatedLabel.setText(MRC2ToolBoxConfiguration.getDateTimeFormat().format(method.getCreatedOn()));

			methodNameTextField.setText(method.getName());
			descriptionTextArea.setText(method.getDescription());

			if(method.getCreatedBy() != null)
			methodAuthorLabel.setText(method.getCreatedBy().getInfo());
			
			software = method.getSoftware();
			if(software != null)
				softwareTextField.setText(software.getName());
		}
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
		
		if(e.getActionCommand().equals(MainActionCommands.SHOW_SOFTWARE_SELECTOR_COMMAND.getName()))
			showSoftwareSelector();
		
		if(e.getActionCommand().equals(MainActionCommands.SELECT_SOFTWARE_COMMAND.getName()))
			selectSoftware();

		if(e.getActionCommand().equals(BROWSE_COMMAND))
			selectMethodFile();
	}
	
	private void selectMethodFile() {
		
		JnaFileChooser fc = new JnaFileChooser(baseDirectory);
		fc.setMode(JnaFileChooser.Mode.FilesAndDirectories);
		fc.setTitle("Select data extraction method file");
		fc.setMultiSelectionEnabled(false);
		if (fc.showOpenDialog(SwingUtilities.getWindowAncestor(this.getContentPane()))) {
			
			File methodFile = fc.getSelectedFile();
			baseDirectory = methodFile.getParentFile();
			methodFileTextField.setText(methodFile.getAbsolutePath());
			if(method == null) {
				methodNameTextField.setText(methodFile.getName());
				descriptionTextArea.setText(methodFile.getName());
			}
			savePreferences();
		}
	}
	
	private void showSoftwareSelector() {

		softwareSelectorDialog = new SoftwareSelectorDialog(this, SoftwareType.DATA_ANALYSIS);
		softwareSelectorDialog.setLocationRelativeTo(this);
		softwareSelectorDialog.setVisible(true);
	}
	
	private void selectSoftware() {
		
		if(softwareSelectorDialog.getSelectedSoftware() != null) {
			
			software = softwareSelectorDialog.getSelectedSoftware();
			softwareTextField.setText(software.getName());
			softwareSelectorDialog.dispose();
		}
	}



	public DataExtractionMethod getMethod() {
		return method;
	}

	public String getMethodName() {
		return methodNameTextField.getText().trim();
	}

	public String getMethodDescription() {
		return descriptionTextArea.getText().trim();
	}

	public File getMethodFile() {

		if(methodFileTextField.getText().trim().isEmpty())
			return null;

		return new File(methodFileTextField.getText().trim());
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
	
	public Collection<String>validateMethodData(){
				
		Collection<String>errors = new ArrayList<String>();
		if(getMethodName().isEmpty()) 
			errors.add("Method name can not be empty.");
		
		if(method == null && getMethodFile() == null) 
			errors.add("Method file should be specified for new method definition.");
		
		DataExtractionMethod existingFileMethod = null;
		
		//	Check if method file was already used
		if(getMethodFile() != null) {
			existingFileMethod = IDTDataCache.getDataExtractionMethodByName(getMethodFile().getName());
			if(existingFileMethod != null)
				errors.add("Method file \"" + getMethodFile().getName() + "\" is already in the database.");
		}
		//	Check if method name was already used
		if(!getMethodName().isEmpty()) {
			
			if(method == null) {	//	New method 
				existingFileMethod = IDTDataCache.getDataExtractionMethodByName(getMethodName());
				if(existingFileMethod != null)
					errors.add("Method \"" + getMethodName() + "\" is already in the database.");
			}
			else {
				String newName = getMethodName();
				existingFileMethod = IDTDataCache.getDataExtractionMethods().stream().
					filter(m -> !m.equals(method)).
					filter(m -> m.getName().equals(newName)).
					findFirst().orElse(null);
				if(existingFileMethod != null)
					errors.add("Another method named \"" + newName + "\" is already in the database.");
			}
		}
		if(software == null)
			errors.add("Software should be specified for method definition.");
		
		return errors;
	}

	public DataProcessingSoftware getSoftware() {
		return software;
	}
}












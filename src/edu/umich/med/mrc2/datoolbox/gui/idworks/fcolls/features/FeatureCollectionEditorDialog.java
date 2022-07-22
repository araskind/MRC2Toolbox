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

package edu.umich.med.mrc2.datoolbox.gui.idworks.fcolls.features;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.prefs.Preferences;

import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import edu.umich.med.mrc2.datoolbox.data.MsFeatureInfoBundle;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureInfoBundleCollection;
import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.database.idt.FeatureCollectionUtils;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.IndeterminateProgressDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.LongUpdateTask;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.fc.ImprovedFileChooser;
import edu.umich.med.mrc2.datoolbox.main.FeatureCollectionManager;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;

public class FeatureCollectionEditorDialog extends JDialog 
		implements ActionListener, BackedByPreferences{

	/**
	 *
	 */
	private static final long serialVersionUID = 7684989595475342241L;

	private Preferences preferences;
	public static final String PREFS_NODE = "edu.umich.med.mrc2.cefanalyzer.gui.MsFeatureCollectionEditorDialog";
	public static final String BASE_DIRECTORY = "BASE_DIRECTORY";
	private static final String BROWSE_COMMAND = "BROWSE_COMMAND";
	
	private static final Icon addFeatureCollectionIcon = GuiUtils.getIcon("newFeatureSubset", 32);
	private static final Icon editFeatureCollectionIcon = GuiUtils.getIcon("editCollection", 32);
	
	private MsFeatureInfoBundleCollection featureCollection;
	private JButton btnSave;
	private JLabel dateCreatedLabel, lastModifiedLabel;
	private JTextArea descriptionTextArea;	
	private ImprovedFileChooser chooser;
	private File baseDirectory;
	private JTextField methodNameTextField;
	private JLabel idValueLabel;
	private JLabel methodAuthorLabel;
	private Collection<MsFeatureInfoBundle> featuresToAdd;
	private Set<String> featureIdsToAdd;
	private JTextField featureFileTextField;
	private JCheckBox loadCollectionCheckBox;

	private IndeterminateProgressDialog idp;

	public FeatureCollectionEditorDialog(
			MsFeatureInfoBundleCollection collection, 
			Collection<MsFeatureInfoBundle> featuresToAdd,
			ActionListener actionListener) {
		super();
		setPreferredSize(new Dimension(700, 300));
		setModalityType(ModalityType.APPLICATION_MODAL);
		setResizable(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		
		this.featureCollection = collection;
		this.featuresToAdd = featuresToAdd;
		featureIdsToAdd = new TreeSet<String>();
		
		JPanel dataPanel = new JPanel();
		dataPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(dataPanel, BorderLayout.CENTER);
		GridBagLayout gbl_dataPanel = new GridBagLayout();
		gbl_dataPanel.columnWidths = new int[]{0, 83, 114, 77, 100, 78, 0, 0};
		gbl_dataPanel.rowHeights = new int[]{0, 0, 0, 0, 0, 0};
		gbl_dataPanel.columnWeights = new double[]{0.0, 1.0, 1.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		gbl_dataPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
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
		gbc_lblCreated.gridx = 2;
		gbc_lblCreated.gridy = 0;
		dataPanel.add(lblCreated, gbc_lblCreated);
		
		dateCreatedLabel = new JLabel("");
		GridBagConstraints gbc_dateCreatedLabel = new GridBagConstraints();
		gbc_dateCreatedLabel.anchor = GridBagConstraints.WEST;
		gbc_dateCreatedLabel.insets = new Insets(0, 0, 5, 5);
		gbc_dateCreatedLabel.gridx = 3;
		gbc_dateCreatedLabel.gridy = 0;
		dataPanel.add(dateCreatedLabel, gbc_dateCreatedLabel);
		
		JLabel lblNewLabel = new JLabel("Last modified");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.gridx = 4;
		gbc_lblNewLabel.gridy = 0;
		dataPanel.add(lblNewLabel, gbc_lblNewLabel);
		
		lastModifiedLabel = new JLabel("");
		GridBagConstraints gbc_lastModifiedLabel = new GridBagConstraints();
		gbc_lastModifiedLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lastModifiedLabel.gridx = 5;
		gbc_lastModifiedLabel.gridy = 0;
		dataPanel.add(lastModifiedLabel, gbc_lastModifiedLabel);

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
		gbc_methodAuthorLabel.gridwidth = 4;
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
		GridBagConstraints gbc_methodNameTextField = new GridBagConstraints();
		gbc_methodNameTextField.gridwidth = 6;
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
		descriptionTextArea.setBorder(new LineBorder(new Color(0, 0, 0)));
		descriptionTextArea.setRows(3);
		descriptionTextArea.setWrapStyleWord(true);
		descriptionTextArea.setLineWrap(true);
		GridBagConstraints gbc_textArea = new GridBagConstraints();
		gbc_textArea.insets = new Insets(0, 0, 5, 0);
		gbc_textArea.gridwidth = 6;
		gbc_textArea.fill = GridBagConstraints.BOTH;
		gbc_textArea.gridx = 1;
		gbc_textArea.gridy = 3;
		dataPanel.add(descriptionTextArea, gbc_textArea);
		
		JLabel lblNewLabel_1 = new JLabel("Feature list from file");
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_1.gridwidth = 2;
		gbc_lblNewLabel_1.insets = new Insets(0, 0, 0, 5);
		gbc_lblNewLabel_1.gridx = 0;
		gbc_lblNewLabel_1.gridy = 4;
		dataPanel.add(lblNewLabel_1, gbc_lblNewLabel_1);
		
		featureFileTextField = new JTextField();
		featureFileTextField.setEditable(false);
		GridBagConstraints gbc_textField = new GridBagConstraints();
		gbc_textField.gridwidth = 4;
		gbc_textField.insets = new Insets(0, 0, 0, 5);
		gbc_textField.fill = GridBagConstraints.HORIZONTAL;
		gbc_textField.gridx = 2;
		gbc_textField.gridy = 4;
		dataPanel.add(featureFileTextField, gbc_textField);
		featureFileTextField.setColumns(10);
		
		JButton browseForFileButton = new JButton("Browse ...");
		browseForFileButton.setActionCommand(BROWSE_COMMAND);
		browseForFileButton.addActionListener(this);
		GridBagConstraints gbc_btnNewButton = new GridBagConstraints();
		gbc_btnNewButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnNewButton.gridx = 6;
		gbc_btnNewButton.gridy = 4;
		dataPanel.add(browseForFileButton, gbc_btnNewButton);

		JPanel panel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		getContentPane().add(panel, BorderLayout.SOUTH);
		
		loadCollectionCheckBox = 
				new JCheckBox("Load collection in the workbench");
		panel.add(loadCollectionCheckBox);
		
		Component horizontalStrut = Box.createHorizontalStrut(50);
		panel.add(horizontalStrut);

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

		loadCollectionData();
	}

	private void loadCollectionData() {

		if(featureCollection == null) {

			setTitle("Create new feature collection");
			setIconImage(((ImageIcon) addFeatureCollectionIcon).getImage());
			
			if(featuresToAdd == null) 
				btnSave.setText(MainActionCommands.ADD_FEATURE_COLLECTION_COMMAND.getName());
			else 
				btnSave.setText(MainActionCommands.ADD_FEATURE_COLLECTION_WITH_FEATURES_COMMAND.getName());
			
			dateCreatedLabel.setText(MRC2ToolBoxConfiguration.getDateTimeFormat().format(new Date()));
			lastModifiedLabel.setText(MRC2ToolBoxConfiguration.getDateTimeFormat().format(new Date()));			
			methodAuthorLabel.setText(MRC2ToolBoxCore.getIdTrackerUser().getInfo());
		}
		else {
			setTitle("Edit information for " + featureCollection.getName());
			setIconImage(((ImageIcon) editFeatureCollectionIcon).getImage());
			btnSave.setActionCommand(MainActionCommands.EDIT_FEATURE_COLLECTION_COMMAND.getName());
			idValueLabel.setText(featureCollection.getId());

			if (featureCollection.getDateCreated() != null)
				dateCreatedLabel.setText(MRC2ToolBoxConfiguration.getDateTimeFormat().format(
						featureCollection.getDateCreated()));

			if (featureCollection.getLastModified() != null)
				lastModifiedLabel.setText(MRC2ToolBoxConfiguration.getDateTimeFormat().format(
						featureCollection.getLastModified()));
			
			methodNameTextField.setText(featureCollection.getName());
			descriptionTextArea.setText(featureCollection.getDescription());

			if(featureCollection.getOwner() != null)
				methodAuthorLabel.setText(featureCollection.getOwner().getInfo());
		}
		loadPreferences();
		initChooser();
		pack();
	}
	
	public Collection<String>validateCollectionData() {
		
		Collection<String>errors = new ArrayList<String>();
		String newName = getFeatureCollectionName();
		if(newName.isEmpty())
			errors.add("Name can not be empty.");
		
		String nameError = null;
		if(MRC2ToolBoxCore.getActiveRawDataAnalysisProject() == null)
			nameError = validateNameAgainstDatabase(newName);
		else
			nameError = validateNameAgainstProject(newName);
		
		if(nameError != null)
			errors.add(nameError);
		
		return errors;
	}
	
	private String validateNameAgainstDatabase(String newName) {
				
		FeatureCollectionManager.refreshMsFeatureInfoBundleCollections();
		MsFeatureInfoBundleCollection existing = null;
		if(this.featureCollection == null) {
			existing = FeatureCollectionManager.getMsFeatureInfoBundleCollections().stream().
				filter(f -> f.getName().equalsIgnoreCase(newName)).
				findFirst().orElse(null);
		}
		else {
			String id = featureCollection.getId();
			existing = FeatureCollectionManager.getMsFeatureInfoBundleCollections().stream().
					filter(f -> !f.getId().equals(id)).
					filter(f -> f.getName().equalsIgnoreCase(newName)).
					findFirst().orElse(null);
		}
		if(existing != null)
			return "Collection \"" + newName + "\" already exists.";
		else
			return null;
	}
	
	private String validateNameAgainstProject(String newName) {
		
		MsFeatureInfoBundleCollection existing = null;
		Set<MsFeatureInfoBundleCollection> projectCollections = 
				MRC2ToolBoxCore.getActiveRawDataAnalysisProject().getFeatureCollections();
		if(this.featureCollection == null) {
			existing = projectCollections.stream().
					filter(f -> f.getName().equalsIgnoreCase(newName)).
					findFirst().orElse(null);
		}
		else {
			String id = featureCollection.getId();
			existing = projectCollections.stream().
					filter(f -> !f.getId().equals(id)).
					filter(f -> f.getName().equalsIgnoreCase(newName)).
					findFirst().orElse(null);
		}
		if(existing != null)
			return "Collection \"" + newName + "\" already exists.";
		else
			return null;
	}
	
	@Override
	public void dispose() {
		
		savePreferences();
		super.dispose();
	}	

	private void initChooser() {

		chooser = new ImprovedFileChooser();
		chooser.setBorder(new EmptyBorder(10, 10, 10, 10));
		chooser.addActionListener(this);		
		chooser.setMultiSelectionEnabled(false);
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setCurrentDirectory(baseDirectory);
		chooser.setAcceptAllFileFilterUsed(false);
		FileNameExtensionFilter txtFilter = new FileNameExtensionFilter("Text files", "txt", "tsv", "csv");
		chooser.addChoosableFileFilter(txtFilter);
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		if(e.getActionCommand().equals(BROWSE_COMMAND))
			chooser.showOpenDialog(this);

		if(e.getSource().equals(chooser) && e.getActionCommand().equals(JFileChooser.APPROVE_SELECTION))
			validateFeatureListFile();
	}
	
	private void validateFeatureListFile() {
		
		File inputFile = chooser.getSelectedFile();
		if(inputFile.exists() && inputFile.canRead()) {
			
			List<String>lines = null;
			try {
				lines = Files.readAllLines(Paths.get(inputFile.getAbsolutePath()));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(lines != null && !lines.isEmpty()) {
				
				for(String line : lines) {
					
					String trimmed = line.trim();
					if(trimmed.startsWith(DataPrefix.MSMS_SPECTRUM.getName()) && trimmed.length() == 16)
						featureIdsToAdd.add(trimmed);				
				}
			}
			if(!featureIdsToAdd.isEmpty()) {
				
				ValidateMSMSIDlistTask task = new ValidateMSMSIDlistTask(featureIdsToAdd);
				idp = new IndeterminateProgressDialog("Validating MSMS feature IDs ...", this, task);
				idp.setLocationRelativeTo(this);
				idp.setVisible(true);
				
				if(!featureIdsToAdd.isEmpty()) {
					featureFileTextField.setText(chooser.getSelectedFile().getAbsolutePath());
					btnSave.setText(MainActionCommands.ADD_FEATURE_COLLECTION_WITH_FEATURES_COMMAND.getName());
					MessageDialog.showInfoMsg("Extracted " + Integer.toString(featureIdsToAdd.size()) + 
							" valid MSMS feature IDs", this);
				}
				else {
					String message = "No valid MSMS feature IDs found in " + inputFile.getName() + "\n"
							+ "Correct MSMS feature ID format is " + DataPrefix.MSMS_SPECTRUM.getName() + " followed by 12 digits.";
					MessageDialog.showWarningMsg(message, this);
					return;
				}
			}
			else {
				String message = "No valid MSMS feature IDs found in " + inputFile.getName() + "\n"
						+ "Correct MSMS feature ID format is " + DataPrefix.MSMS_SPECTRUM.getName() + " followed by 12 digits.";
				MessageDialog.showWarningMsg(message, this);
				return;
			}
		}
		else {
			MessageDialog.showErrorMsg("Can not read input file", this);
		}
		baseDirectory = inputFile.getParentFile();		
		savePreferences();	
	}
	
	class ValidateMSMSIDlistTask extends LongUpdateTask {
		/*
		 * Main task. Executed in background thread.
		 */
		private Set<String>idsToValidate;

		public ValidateMSMSIDlistTask(Set<String>idsToValidate) {
			this.idsToValidate = idsToValidate;
		}

		@SuppressWarnings("unchecked")
		@Override
		public Void doInBackground() {

			try {
				featureIdsToAdd = FeatureCollectionUtils.validateMSMSIDlist(idsToValidate);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
	}

	public MsFeatureInfoBundleCollection getFeatureCollection() {
		return featureCollection;
	}

	public String getFeatureCollectionName() {
		return methodNameTextField.getText().trim();
	}

	public String getFeatureCollectionDescription() {
		return descriptionTextArea.getText().trim();
	}

	public File getMethodFile() {

//		if(methodFileTextField.getText().trim().isEmpty())
//			return null;
//
//		return new File(methodFileTextField.getText().trim());
		
		return null;
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

	public Collection<MsFeatureInfoBundle> getFeaturesToAdd() {
		return featuresToAdd;
	}

	public Set<String> getFeatureIdsToAdd() {
		return featureIdsToAdd;
	}
	
	public boolean loadCollectionIntoWorkBench() {
		return loadCollectionCheckBox.isSelected();
	}
}












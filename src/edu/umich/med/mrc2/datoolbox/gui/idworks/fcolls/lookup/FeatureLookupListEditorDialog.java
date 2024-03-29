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

package edu.umich.med.mrc2.datoolbox.gui.idworks.fcolls.lookup;

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
import java.util.Date;
import java.util.Set;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import edu.umich.med.mrc2.datoolbox.data.MinimalMSOneFeature;
import edu.umich.med.mrc2.datoolbox.data.msclust.FeatureLookupList;
import edu.umich.med.mrc2.datoolbox.gui.idworks.search.FeatureListImportPanel;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.main.FeatureLookupListManager;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;

public class FeatureLookupListEditorDialog extends JDialog 
		implements ActionListener, BackedByPreferences{

	/**
	 *
	 */
	private static final long serialVersionUID = 7684989595475342241L;

	private Preferences preferences;
	public static final String PREFS_NODE = "edu.umich.med.mrc2.cefanalyzer.gui.FeatureLookupDataSetEditorDialog";
	public static final String BASE_DIRECTORY = "BASE_DIRECTORY";
	
	private static final Icon addFeatureCollectionIcon = GuiUtils.getIcon("newFeatureSubset", 32);
	private static final Icon editFeatureCollectionIcon = GuiUtils.getIcon("editCollection", 32);
	
	private FeatureLookupList dataSet;
	private JButton btnSave;
	private JLabel dateCreatedLabel, lastModifiedLabel;
	private File baseDirectory;
	private JLabel idValueLabel;
	private JLabel methodAuthorLabel;

	private FeatureListImportPanel featureListImportPanel;
	
	public FeatureLookupListEditorDialog(
			FeatureLookupList datSet,
			ActionListener actionListener) {
		super();
		setPreferredSize(new Dimension(700, 500));
		setModalityType(ModalityType.APPLICATION_MODAL);
		setResizable(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		
		this.dataSet = datSet;
	
		JPanel dataPanel = new JPanel();
		dataPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(dataPanel, BorderLayout.CENTER);
		GridBagLayout gbl_dataPanel = new GridBagLayout();
		gbl_dataPanel.columnWidths = new int[]{0, 83, 114, 77, 100, 78, 0, 0};
		gbl_dataPanel.rowHeights = new int[]{0, 0, 0, 0};
		gbl_dataPanel.columnWeights = new double[]{0.0, 1.0, 1.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		gbl_dataPanel.rowWeights = new double[]{0.0, 0.0, 1.0, Double.MIN_VALUE};
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
		
		featureListImportPanel = new FeatureListImportPanel();
		GridBagConstraints gbc_panel_1 = new GridBagConstraints();
		gbc_panel_1.gridwidth = 7;
		gbc_panel_1.fill = GridBagConstraints.BOTH;
		gbc_panel_1.gridx = 0;
		gbc_panel_1.gridy = 2;
		dataPanel.add(featureListImportPanel, gbc_panel_1);

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
		loadDataSet();
	}

	private void loadDataSet() {

		if(dataSet == null) {

			setTitle("Create new feature lookup data set");
			setIconImage(((ImageIcon) addFeatureCollectionIcon).getImage());
			btnSave.setText(MainActionCommands.ADD_FEATURE_LOOKUP_LIST_COMMAND.getName());			
			dateCreatedLabel.setText(MRC2ToolBoxConfiguration.getDateTimeFormat().format(new Date()));
			lastModifiedLabel.setText(MRC2ToolBoxConfiguration.getDateTimeFormat().format(new Date()));			
			methodAuthorLabel.setText(MRC2ToolBoxCore.getIdTrackerUser().getInfo());
			featureListImportPanel.disableLoadingFeaturesFromDatabase();
		}
		else {
			setTitle("Edit information for " + dataSet.getName());
			setIconImage(((ImageIcon) editFeatureCollectionIcon).getImage());
			btnSave.setActionCommand(MainActionCommands.EDIT_FEATURE_LOOKUP_LIST_COMMAND.getName());
			idValueLabel.setText(dataSet.getId());

			if (dataSet.getDateCreated() != null)
				dateCreatedLabel.setText(MRC2ToolBoxConfiguration.getDateTimeFormat().format(
						dataSet.getDateCreated()));

			if (dataSet.getLastModified() != null)
				lastModifiedLabel.setText(MRC2ToolBoxConfiguration.getDateTimeFormat().format(
						dataSet.getLastModified()));
			
			if(dataSet.getCreatedBy() != null)
				methodAuthorLabel.setText(dataSet.getCreatedBy().getInfo());
			
			featureListImportPanel.disableLoadingFeatures();
			featureListImportPanel.loadDataSet(dataSet);
		}
		loadPreferences();
		pack();
	}
	
	public Collection<String>validateDataSet() {
		
		Collection<String>errors = new ArrayList<String>();
		String newName = getDataSetName();
		if(newName.isEmpty())
			errors.add("Name can not be empty.");
		
		String nameError = null;
		if(MRC2ToolBoxCore.getActiveOfflineRawDataAnalysisExperiment() == null)
			nameError = validateNameAgainstDatabase(newName);
		else
			nameError = validateNameAgainstProject(newName);
		
		if(nameError != null)
			errors.add(nameError);
		
		if(dataSet == null && featureListImportPanel.getAllFeatures().isEmpty())
			errors.add("Feature list not specified");
				
		return errors;
	}
	
	private String validateNameAgainstDatabase(String newName) {
		
		FeatureLookupListManager.refreshFeatureLookupListCollection();
		FeatureLookupList existing = null;
		if(this.dataSet == null) {
			existing = FeatureLookupListManager.getFeatureLookupListByName(newName);
		}
		else {
			String id = dataSet.getId();
			existing = FeatureLookupListManager.getFeatureLookupListCollection().stream().
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
		
		FeatureLookupList existing = null;
		Set<FeatureLookupList> projectFeatureLookupDataSets = 
				MRC2ToolBoxCore.getActiveOfflineRawDataAnalysisExperiment().
					getMsmsClusterDataSets().stream().
					filter(d -> d.getFeatureLookupDataSet() != null).
					map(d -> d.getFeatureLookupDataSet()).collect(Collectors.toSet());
		if(projectFeatureLookupDataSets.isEmpty())
			return null;
		
		if(this.dataSet == null) {
			existing = projectFeatureLookupDataSets.stream().
					filter(d -> d.getName().equals(newName)).
					findFirst().orElse(null);
		}
		else {
			String id = dataSet.getId();
			existing = projectFeatureLookupDataSets.stream().
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
	
	@Override
	public void actionPerformed(ActionEvent e) {

		//if(e.getActionCommand().equals(BROWSE_COMMAND))

	}
	
	public FeatureLookupList getFeatureLookupDataSet() {
		return dataSet;
	}
	
	public String getDataSetName() {
		return featureListImportPanel.getDataSetName();
	}

	public String getDataSetDescription() {
		return featureListImportPanel.getDataSetDescription();
	}
	
	public Collection<MinimalMSOneFeature>getSelectedFeatures(){
		return featureListImportPanel.getSelectedFeatures();
	}
	
	public Collection<MinimalMSOneFeature>getAllFeatures(){
		return featureListImportPanel.getAllFeatures();
	}

	@Override
	public void loadPreferences(Preferences prefs) {
		preferences = prefs;
		baseDirectory =
			new File(preferences.get(BASE_DIRECTORY,
				MRC2ToolBoxConfiguration.getDefaultDataDirectory()));
		featureListImportPanel.setBaseDirectory(baseDirectory);
	}

	@Override
	public void loadPreferences() {
		loadPreferences(Preferences.userRoot().node(PREFS_NODE));
	}

	@Override
	public void savePreferences() {
		preferences = Preferences.userRoot().node(PREFS_NODE);
		preferences.put(BASE_DIRECTORY, 
				featureListImportPanel.getBaseDirectory().getAbsolutePath());
	}
}












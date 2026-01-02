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

package edu.umich.med.mrc2.datoolbox.gui.integration;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;
import java.util.prefs.Preferences;

import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import edu.umich.med.mrc2.datoolbox.data.MsFeatureClusterSet;
import edu.umich.med.mrc2.datoolbox.data.enums.PrimaryFeatureSelectionOption;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.integration.dpalign.DataPipelineSelectionTable;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;

public class DataIntegrationSetupDialog extends JDialog implements BackedByPreferences{
	
	private static final long serialVersionUID = 1L;
	
	private static final Icon collectIDDataIcon = GuiUtils.getIcon("createIntegration", 32);
	
	private Preferences preferences;
	public static final String PRIMARY_FEATURE_SELECTION_OPTION = "PRIMARY_FEATURE_SELECTION_OPTION";
	
	private JButton cancelButton, integrateButton;
	private JTextField featureSetNameTextField;
	private DataPipelineSelectionTable assaySelectionTable;
	private JComboBox<PrimaryFeatureSelectionOption> primarySelectionCriteriaComboBox;
	private MsFeatureClusterSet activeClusterSet;
	
	public DataIntegrationSetupDialog(
			ActionListener listener, 
			MsFeatureClusterSet activeClusterSet) {

		super(MRC2ToolBoxCore.getMainWindow(), "Data integration parameters");
		setModalityType(ModalityType.APPLICATION_MODAL);
		setIconImage(((ImageIcon) collectIDDataIcon).getImage());
		setSize(new Dimension(640, 300));
		setPreferredSize(new Dimension(640, 300));
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		
		this.activeClusterSet = activeClusterSet;
		
		JPanel panel = new JPanel();
		getContentPane().add(panel, BorderLayout.CENTER);
		panel.setLayout(new BorderLayout(0, 0));
		
		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new EmptyBorder(10, 10, 5, 5));
		panel.add(panel_1, BorderLayout.NORTH);
		GridBagLayout gbl_panel_1 = new GridBagLayout();
		gbl_panel_1.columnWidths = new int[]{46, 86, 0};
		gbl_panel_1.rowHeights = new int[]{20, 0};
		gbl_panel_1.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gbl_panel_1.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		panel_1.setLayout(gbl_panel_1);
		
		JLabel lblNewLabel = new JLabel("Name ");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel.insets = new Insets(0, 0, 0, 5);
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 0;
		panel_1.add(lblNewLabel, gbc_lblNewLabel);
		
		featureSetNameTextField = new JTextField();
		if(activeClusterSet != null)
			featureSetNameTextField.setText(activeClusterSet.getName());
		
		GridBagConstraints gbc_featureSetNameTextField = new GridBagConstraints();
		gbc_featureSetNameTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_featureSetNameTextField.anchor = GridBagConstraints.NORTH;
		gbc_featureSetNameTextField.gridx = 1;
		gbc_featureSetNameTextField.gridy = 0;
		panel_1.add(featureSetNameTextField, gbc_featureSetNameTextField);
		featureSetNameTextField.setColumns(10);
		
		JPanel panel_3 = new JPanel(new BorderLayout(0, 0));
		panel_3.setBorder(new TitledBorder(null, 
				"Select assays to include in data integration", 
				TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel.add(panel_3, BorderLayout.CENTER);
			
		assaySelectionTable = new DataPipelineSelectionTable(); 
		if(MRC2ToolBoxCore.getActiveMetabolomicsExperiment() != null) {
			
			Set<DataPipeline> selectedPipelines = new TreeSet<>();
			if(activeClusterSet != null)
				selectedPipelines = activeClusterSet.getDataPipelines();
						
			assaySelectionTable.setTableModelFromExperimentAndMarkSelected(
					MRC2ToolBoxCore.getActiveMetabolomicsExperiment(), selectedPipelines);
		}
		panel_3.add(new JScrollPane(assaySelectionTable), BorderLayout.CENTER);	
		
		JPanel panel_4 = new JPanel();
		panel_4.setBorder(new EmptyBorder(10, 5, 10, 5));
		panel_3.add(panel_4, BorderLayout.SOUTH);
		GridBagLayout gbl_panel_4 = new GridBagLayout();
		gbl_panel_4.columnWidths = new int[]{0, 0, 0};
		gbl_panel_4.rowHeights = new int[]{0, 0};
		gbl_panel_4.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gbl_panel_4.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		panel_4.setLayout(gbl_panel_4);
		
		JLabel lblNewLabel_1 = new JLabel("Select representative feature based on ");
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.insets = new Insets(0, 0, 0, 5);
		gbc_lblNewLabel_1.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_1.gridx = 0;
		gbc_lblNewLabel_1.gridy = 0;
		panel_4.add(lblNewLabel_1, gbc_lblNewLabel_1);
		
		primarySelectionCriteriaComboBox = new JComboBox<>(
				new DefaultComboBoxModel<>(PrimaryFeatureSelectionOption.values()));
		GridBagConstraints gbc_primarySelectionCriteriaComboBox = new GridBagConstraints();
		gbc_primarySelectionCriteriaComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_primarySelectionCriteriaComboBox.gridx = 1;
		gbc_primarySelectionCriteriaComboBox.gridy = 0;
		panel_4.add(primarySelectionCriteriaComboBox, gbc_primarySelectionCriteriaComboBox);
		
		JPanel panel_2 = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel_2.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		panel.add(panel_2, BorderLayout.SOUTH);
		
		cancelButton = new JButton("Cancel");
		panel_2.add(cancelButton);
		
		integrateButton = new JButton(
				MainActionCommands.INTEGRATE_NAMED_DATA_COMMAND.getName());
		integrateButton.setActionCommand(
				MainActionCommands.INTEGRATE_NAMED_DATA_COMMAND.getName());
		integrateButton.addActionListener(listener);
		panel_2.add(integrateButton);
		
		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				dispose();
			}
		};
		cancelButton.addActionListener(al);

		JRootPane rootPane = SwingUtilities.getRootPane(integrateButton);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(integrateButton);
		loadPreferences();
		pack();
	}
	
	@Override
	public void dispose() {
		savePreferences();
		super.dispose();
	}
	
	public Collection<DataPipeline> getSelectedDataPipelines(){
		return assaySelectionTable.getCheckedDataPipelines();
	}
	
	public String getDataSetName() {		
		return featureSetNameTextField.getText().trim();
	}
	
	public PrimaryFeatureSelectionOption getPrimaryFeatureSelectionOption() {
		return (PrimaryFeatureSelectionOption)primarySelectionCriteriaComboBox.getSelectedItem();
	}
	
	public Collection<String>validateFormData(){
	    
	    Collection<String>errors = new ArrayList<>();
	    
	    String dataSetName = getDataSetName();
	    if(dataSetName.isEmpty())
	        errors.add("Data set name not defined");
	    else {
			MsFeatureClusterSet nm = MRC2ToolBoxCore.getActiveMetabolomicsExperiment().
					getFeatureClusterSets().stream().
					filter(s -> s.getName().equalsIgnoreCase(dataSetName)).
					findFirst().orElse(null);

			if (nm != null && !nm.equals(activeClusterSet))
				errors.add("Integrated data set with the name \"" 
						+ dataSetName + "\" alredy exists in the project");
	    }
	    if(getSelectedDataPipelines().size() < 2)
	        errors.add("Please select at least 2 assay methods");
	    
	    if(getPrimaryFeatureSelectionOption() == null)
	    	errors.add("Primary feature selection option is not specified");
	    		
	    return errors;
	}
	
	@Override
	public void loadPreferences(Preferences preferences) {
		
		this.preferences = preferences;
		String optionName = preferences.get(PRIMARY_FEATURE_SELECTION_OPTION, 
				PrimaryFeatureSelectionOption.MIN_MISSING.name());
		PrimaryFeatureSelectionOption option = PrimaryFeatureSelectionOption.getOptionByName(optionName);
		if(option == null)
			option = PrimaryFeatureSelectionOption.MIN_MISSING;
		
		primarySelectionCriteriaComboBox.setSelectedItem(option);
	}

	@Override
	public void loadPreferences() {
		loadPreferences(Preferences.userNodeForPackage(this.getClass()));		
	}

	@Override
	public void savePreferences() {

		preferences = Preferences.userNodeForPackage(this.getClass());
		preferences.put(PRIMARY_FEATURE_SELECTION_OPTION, getPrimaryFeatureSelectionOption().name());
	}
}

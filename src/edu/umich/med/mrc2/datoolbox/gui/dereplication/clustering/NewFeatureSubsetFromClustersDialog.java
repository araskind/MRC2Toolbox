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

package edu.umich.med.mrc2.datoolbox.gui.dereplication.clustering;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
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

import edu.umich.med.mrc2.datoolbox.data.MsFeatureClusterSet;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureSet;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class NewFeatureSubsetFromClustersDialog extends JDialog {

	private static final long serialVersionUID = 1L;	
	private static final Icon dialogIcon = GuiUtils.getIcon("newFeatureSubset", 32);
	private MsFeatureClusterSet activeClusterSet;
	private JTextField nameTextField;
	private JCheckBox includeOnlyEnabledCheckBox;
	
	public NewFeatureSubsetFromClustersDialog(
			ActionListener actionListener, 
			MsFeatureClusterSet activeClusterSet) {
		super();
		this.activeClusterSet = activeClusterSet;
		setTitle("Create new feature subset from clusters");
		setIconImage(((ImageIcon) dialogIcon).getImage());
		setPreferredSize(new Dimension(600, 250));
		setSize(new Dimension(600, 250));
		setModalityType(ModalityType.APPLICATION_MODAL);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		JPanel dataPanel = new JPanel();
		dataPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(dataPanel, BorderLayout.CENTER);
		GridBagLayout gbl_dataPanel = new GridBagLayout();
		gbl_dataPanel.columnWidths = new int[]{0, 0};
		gbl_dataPanel.rowHeights = new int[]{0, 0, 0, 0};
		gbl_dataPanel.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_dataPanel.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		dataPanel.setLayout(gbl_dataPanel);
		
		JLabel lblNewLabel = new JLabel("New feature set name:");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 0);
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 0;
		dataPanel.add(lblNewLabel, gbc_lblNewLabel);
		
		nameTextField = new JTextField();
		GridBagConstraints gbc_nameTextField = new GridBagConstraints();
		gbc_nameTextField.insets = new Insets(0, 0, 5, 0);
		gbc_nameTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_nameTextField.gridx = 0;
		gbc_nameTextField.gridy = 1;
		dataPanel.add(nameTextField, gbc_nameTextField);
		nameTextField.setText("Features from " + activeClusterSet.getName());
		
		includeOnlyEnabledCheckBox = new JCheckBox("Include only enabled features");
		GridBagConstraints gbc_chckbxNewCheckBox = new GridBagConstraints();
		gbc_chckbxNewCheckBox.anchor = GridBagConstraints.WEST;
		gbc_chckbxNewCheckBox.gridx = 0;
		gbc_chckbxNewCheckBox.gridy = 2;
		dataPanel.add(includeOnlyEnabledCheckBox, gbc_chckbxNewCheckBox);

		JPanel buttonPanel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) buttonPanel.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);

		JButton btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(e -> dispose());
		buttonPanel.add(btnCancel);

		JButton btnSave = new JButton(
				MainActionCommands.ADD_NEW_FEATURE_SUBSET_TO_PROJECT_COMMAND.getName());
		btnSave.setActionCommand(
				MainActionCommands.ADD_NEW_FEATURE_SUBSET_TO_PROJECT_COMMAND.getName());
		btnSave.addActionListener(actionListener);
		buttonPanel.add(btnSave);
		
		JRootPane rootPane = SwingUtilities.getRootPane(btnSave);		
		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		rootPane.registerKeyboardAction(al -> { dispose(); }, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(btnSave);

		pack();
	}
	
	public boolean includeOnlyEnabled() {
		return includeOnlyEnabledCheckBox.isSelected();
	}
	
	public String getFeatureSetName() {
		return nameTextField.getText().trim();
	}

	public MsFeatureClusterSet getActiveClusterSet() {
		return activeClusterSet;
	}
	
	public Collection<String>validateFormData(){
	    
	    Collection<String>errors = new ArrayList<String>();
	    String newName = getFeatureSetName();
	    if(newName.isBlank())
	        errors.add("Feature set name cannot be empty.");
	    else {
			DataAnalysisProject experiment = 
					MRC2ToolBoxCore.getActiveMetabolomicsExperiment();
			Set<MsFeatureSet> allSubsets = 
					experiment.getMsFeatureSetsForDataPipeline(experiment.getActiveDataPipeline());
			MsFeatureSet existing = allSubsets.stream().filter(s -> s.getName().equalsIgnoreCase(newName)).findFirst().orElse(null);;
	
			if (existing != null)
				errors.add("Feature subset \"" + newName + "\" already exists.");
	    }
	    return errors;
	}
}

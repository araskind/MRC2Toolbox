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

package edu.umich.med.mrc2.datoolbox.gui.expsetup.featurelist;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.util.Collection;

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

import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureSet;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.SortedComboBoxModel;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class SimpleFeatureSubsetDialog extends JDialog implements ItemListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8240437488922625409L;
	private static final Icon createNewCollectionIcon = GuiUtils.getIcon("newFeatureSubset", 32);
	private static final Icon addToExistingCollectionIcon = GuiUtils.getIcon("addCollection", 32);
	
	private MsFeatureSet subset;
	private Collection<MsFeature>featuresToAdd;
	private SubsetFeaturesTable featuresTable;
	private JTextField nameTextField;
	private JComboBox featureSubsetComboBox;
	
	@SuppressWarnings("unchecked")
	public SimpleFeatureSubsetDialog(
			MsFeatureSet subset, 
			Collection<MsFeature>featuresToAdd,
			ActionListener actionListener) {
		super();		
		setModalityType(ModalityType.APPLICATION_MODAL);
		setSize(new Dimension(600, 400));
		setPreferredSize(new Dimension(600, 400));
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		this.subset = subset;
		this.featuresToAdd = featuresToAdd;
		if(subset == null) {
			setTitle("Create new feature subset");
			setIconImage(((ImageIcon)addToExistingCollectionIcon).getImage());			
		}
		else {
			setTitle("Edit feature subset \"" + subset.getName() + "\"");
			setIconImage(((ImageIcon)createNewCollectionIcon).getImage());
		}	
		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(panel_1, BorderLayout.NORTH);
		GridBagLayout gbl_panel_1 = new GridBagLayout();
		gbl_panel_1.columnWidths = new int[]{0, 0, 0};
		gbl_panel_1.rowHeights = new int[]{0, 0, 0};
		gbl_panel_1.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gbl_panel_1.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		panel_1.setLayout(gbl_panel_1);
		
		JLabel lblNewLabel_1 = new JLabel("Subset ");
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.anchor = GridBagConstraints.NORTHEAST;
		gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_1.gridx = 0;
		gbc_lblNewLabel_1.gridy = 0;
		panel_1.add(lblNewLabel_1, gbc_lblNewLabel_1);
		
		DataAnalysisProject project = MRC2ToolBoxCore.getActiveMetabolomicsExperiment();
		Collection<MsFeatureSet> subsets = 
				project.getUnlockedMsFeatureSetsForDataPipeline(project.getActiveDataPipeline());
		featureSubsetComboBox = new JComboBox<MsFeatureSet>();
		featureSubsetComboBox.setModel(new SortedComboBoxModel<MsFeatureSet>(subsets));
		GridBagConstraints gbc_comboBox = new GridBagConstraints();
		gbc_comboBox.insets = new Insets(0, 0, 5, 0);
		gbc_comboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBox.gridx = 1;
		gbc_comboBox.gridy = 0;
		panel_1.add(featureSubsetComboBox, gbc_comboBox);
		
		JLabel lblNewLabel = new JLabel("Name ");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.insets = new Insets(0, 0, 0, 5);
		gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 1;
		panel_1.add(lblNewLabel, gbc_lblNewLabel);
		
		nameTextField = new JTextField();
		GridBagConstraints gbc_nameTextField = new GridBagConstraints();
		gbc_nameTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_nameTextField.gridx = 1;
		gbc_nameTextField.gridy = 1;
		panel_1.add(nameTextField, gbc_nameTextField);
		nameTextField.setColumns(10);
		
		featuresTable = new SubsetFeaturesTable();
		getContentPane().add(new JScrollPane(featuresTable), BorderLayout.CENTER);
				
		if(this.subset != null) {
			featureSubsetComboBox.setSelectedItem(this.subset);
			nameTextField.setText(this.subset.getName());
			nameTextField.setEnabled(false);
			featureSubsetComboBox.addItemListener(this);
			featuresTable.setTableModelFromFeatureSet(subset);
			
			if(featuresToAdd != null && !featuresToAdd.isEmpty())
				featuresTable.addFeatures(featuresToAdd);
		}
		else {
			featureSubsetComboBox.setSelectedIndex(-1);
			featureSubsetComboBox.setEnabled(false);
			featuresTable.setTableModelFromFeatures(featuresToAdd);
		}		
		JPanel panel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		getContentPane().add(panel, BorderLayout.SOUTH);

		JButton btnCancel = new JButton("Cancel");
		panel.add(btnCancel);
		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				disposeWithoutSavingPreferences();
			}
		};
		btnCancel.addActionListener(al);

		JButton btnSave = new JButton(MainActionCommands.SAVE_CHANGES_TO_FEATURE_SUBSET_COMMAND.getName());
		btnSave.setActionCommand(MainActionCommands.SAVE_CHANGES_TO_FEATURE_SUBSET_COMMAND.getName());
		btnSave.addActionListener(actionListener);
		panel.add(btnSave);
		JRootPane rootPane = SwingUtilities.getRootPane(btnSave);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(btnSave);
	}
	
	private void disposeWithoutSavingPreferences() {
		super.dispose();
	}
	
	public String getSubsetName() {
		return nameTextField.getText().trim();
	}

	public MsFeatureSet getSubset() {
		return (MsFeatureSet)featureSubsetComboBox.getSelectedItem();
	}

	public Collection<MsFeature>getFeatures(){
		return featuresTable.getAllFeatures();
	}

	@Override
	public void itemStateChanged(ItemEvent e) {

		if (e.getItem() instanceof MsFeatureSet && e.getStateChange() == ItemEvent.SELECTED) {			
			this.subset = (MsFeatureSet)e.getItem();
			nameTextField.setText(this.subset.getName());
		}
	}
}

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

package edu.umich.med.mrc2.datoolbox.gui.expsetup.featurelist;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureSet;
import edu.umich.med.mrc2.datoolbox.data.enums.TableRowSubset;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class FeatureSubsetDialog extends JDialog {

	/**
	 *
	 */
	private static final long serialVersionUID = 1633596808735466315L;
	private ActionListener aListener;
	private FeatureSubsetDialogToolbar toolBar;
	private JSplitPane splitPane;
	private JPanel subsetInfoPanel;
	private JScrollPane scrollPane;
	private JLabel lblName;
	private JTextField subsetNameTextField;
	private JLabel lblOfFeatures;
	private JLabel numFeaturesLabel;
	private MsFeatureSet activeSet;
	private SubsetFeaturesTable featuresTable;
	private JLabel lblCopyAllFrom;
	private JComboBox<MsFeatureSet> featureSetComboBox;
	private JLabel lblAddFromFeatures;
	private JComboBox<TableRowSubset> tableRowChoiceComboBox;

	public FeatureSubsetDialog(ActionListener listener) {

		super(MRC2ToolBoxCore.getMainWindow(), "Create or edit feature subset");

		aListener = listener;

		setModalityType(ModalityType.MODELESS);
		setSize(new Dimension(500, 600));
		setPreferredSize(new Dimension(500, 600));
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		toolBar = new FeatureSubsetDialogToolbar(aListener);
		getContentPane().add(toolBar, BorderLayout.NORTH);

		splitPane = new JSplitPane();
		splitPane.setEnabled(false);
		splitPane.setDividerSize(2);
		splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		getContentPane().add(splitPane, BorderLayout.CENTER);

		subsetInfoPanel = new JPanel();
		subsetInfoPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		subsetInfoPanel.setPreferredSize(new Dimension(500, 75));
		splitPane.setLeftComponent(subsetInfoPanel);
		GridBagLayout gbl_subsetInfoPanel = new GridBagLayout();
		gbl_subsetInfoPanel.columnWidths = new int[] { 0, 0, 0, 0, 0 };
		gbl_subsetInfoPanel.rowHeights = new int[] { 30, 30, 30, 0 };
		gbl_subsetInfoPanel.columnWeights = new double[] { 0.0, 1.0, 0.0, 1.0, Double.MIN_VALUE };
		gbl_subsetInfoPanel.rowWeights = new double[] { 0.0, 0.0, 0.0, Double.MIN_VALUE };
		subsetInfoPanel.setLayout(gbl_subsetInfoPanel);

		lblName = new JLabel("Name ");
		GridBagConstraints gbc_lblName = new GridBagConstraints();
		gbc_lblName.insets = new Insets(0, 0, 5, 5);
		gbc_lblName.anchor = GridBagConstraints.EAST;
		gbc_lblName.gridx = 0;
		gbc_lblName.gridy = 0;
		subsetInfoPanel.add(lblName, gbc_lblName);

		subsetNameTextField = new JTextField();
		GridBagConstraints gbc_textField = new GridBagConstraints();
		gbc_textField.gridwidth = 3;
		gbc_textField.insets = new Insets(0, 0, 5, 0);
		gbc_textField.fill = GridBagConstraints.HORIZONTAL;
		gbc_textField.gridx = 1;
		gbc_textField.gridy = 0;
		subsetInfoPanel.add(subsetNameTextField, gbc_textField);
		subsetNameTextField.setColumns(10);

		lblOfFeatures = new JLabel("# of features");
		GridBagConstraints gbc_lblOfFeatures = new GridBagConstraints();
		gbc_lblOfFeatures.anchor = GridBagConstraints.EAST;
		gbc_lblOfFeatures.insets = new Insets(0, 0, 5, 5);
		gbc_lblOfFeatures.gridx = 0;
		gbc_lblOfFeatures.gridy = 1;
		subsetInfoPanel.add(lblOfFeatures, gbc_lblOfFeatures);

		numFeaturesLabel = new JLabel("");
		numFeaturesLabel.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_numFeaturesLabel = new GridBagConstraints();
		gbc_numFeaturesLabel.insets = new Insets(0, 0, 5, 5);
		gbc_numFeaturesLabel.anchor = GridBagConstraints.WEST;
		gbc_numFeaturesLabel.gridx = 1;
		gbc_numFeaturesLabel.gridy = 1;
		subsetInfoPanel.add(numFeaturesLabel, gbc_numFeaturesLabel);

		lblAddFromFeatures = new JLabel("Add from features table: ");
		GridBagConstraints gbc_lblAddFromFeatures = new GridBagConstraints();
		gbc_lblAddFromFeatures.anchor = GridBagConstraints.EAST;
		gbc_lblAddFromFeatures.insets = new Insets(0, 0, 5, 5);
		gbc_lblAddFromFeatures.gridx = 2;
		gbc_lblAddFromFeatures.gridy = 1;
		subsetInfoPanel.add(lblAddFromFeatures, gbc_lblAddFromFeatures);

		tableRowChoiceComboBox = new JComboBox<TableRowSubset>();
		tableRowChoiceComboBox.setModel(new DefaultComboBoxModel<TableRowSubset>(TableRowSubset.values()));
		tableRowChoiceComboBox.setSelectedIndex(-1);
		GridBagConstraints gbc_tableRowChoiceComboBox = new GridBagConstraints();
		gbc_tableRowChoiceComboBox.insets = new Insets(0, 0, 5, 0);
		gbc_tableRowChoiceComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_tableRowChoiceComboBox.gridx = 3;
		gbc_tableRowChoiceComboBox.gridy = 1;
		subsetInfoPanel.add(tableRowChoiceComboBox, gbc_tableRowChoiceComboBox);

		lblCopyAllFrom = new JLabel("Copy all from: ");
		GridBagConstraints gbc_lblCopyAllFrom = new GridBagConstraints();
		gbc_lblCopyAllFrom.anchor = GridBagConstraints.EAST;
		gbc_lblCopyAllFrom.insets = new Insets(0, 0, 0, 5);
		gbc_lblCopyAllFrom.gridx = 0;
		gbc_lblCopyAllFrom.gridy = 2;
		subsetInfoPanel.add(lblCopyAllFrom, gbc_lblCopyAllFrom);

		featureSetComboBox = new JComboBox<MsFeatureSet>();
		GridBagConstraints gbc_featureSetComboBox = new GridBagConstraints();
		gbc_featureSetComboBox.gridwidth = 3;
		gbc_featureSetComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_featureSetComboBox.gridx = 1;
		gbc_featureSetComboBox.gridy = 2;
		subsetInfoPanel.add(featureSetComboBox, gbc_featureSetComboBox);

		scrollPane = new JScrollPane();
		featuresTable = new SubsetFeaturesTable();
		scrollPane.add(featuresTable);
		scrollPane.setViewportView(featuresTable);
		scrollPane.setPreferredSize(featuresTable.getPreferredScrollableViewportSize());
		//	scrollPane.addComponentListener(new ResizeTableAdjuster());
		splitPane.setRightComponent(scrollPane);
	}

	public void disableSetSelector() {

		featureSetComboBox.setEnabled(false);
	}

	public void enableSetSelector() {

		featureSetComboBox.setEnabled(true);
	}

	public MsFeatureSet getActiveSet() {
		return activeSet;
	}

	public TableRowSubset getFeaturesSelectionToAdd() {
		return (TableRowSubset) tableRowChoiceComboBox.getSelectedItem();
	}

	public MsFeature[] getSelectedFeatures() {

		ArrayList<MsFeature> selected = new ArrayList<MsFeature>();
		int[] rows = featuresTable.getSelectedRows();

		for (int i : rows) {

			MsFeature cf = (MsFeature) featuresTable.getValueAt(i, 1);
			selected.add(cf);
		}
		return selected.toArray(new MsFeature[selected.size()]);
	}

	public MsFeatureSet getSelectedSourceSubset() {

		MsFeatureSet source = null;

		if (featureSetComboBox.getSelectedIndex() > -1)
			source = (MsFeatureSet) featureSetComboBox.getSelectedItem();

		return source;
	}

	public String getSubsetName() {

		return subsetNameTextField.getText().trim();
	}

	public void loadFeatureSubset(MsFeatureSet setToLoad) {

		activeSet = setToLoad;
		subsetNameTextField.setText(activeSet.getName());
		numFeaturesLabel.setText(Integer.toString(activeSet.getFeatures().size()));
		featuresTable.setTableModelFromFeatureSet(setToLoad);
		featureSetComboBox.setSelectedIndex(-1);
		tableRowChoiceComboBox.setSelectedIndex(-1);
	}

	public void populateSetSelector() {
		
		DataAnalysisProject currentProject = MRC2ToolBoxCore.getActiveMetabolomicsExperiment();
		if(currentProject == null)
			return;
		
		DataPipeline activePipeline = currentProject.getActiveDataPipeline();
		if(activePipeline == null)
			return;

		ArrayList<MsFeatureSet> sets = new ArrayList<MsFeatureSet>();
		for (MsFeatureSet set : currentProject.getMsFeatureSetsForDataPipeline(activePipeline)) {

			if (!set.equals(activeSet))
				sets.add(set);
		}
		DefaultComboBoxModel<MsFeatureSet> model = new DefaultComboBoxModel<MsFeatureSet>(
				sets.toArray(new MsFeatureSet[sets.size()]));

		featureSetComboBox.setModel(model);
		featureSetComboBox.setSelectedIndex(-1);		
	}
}

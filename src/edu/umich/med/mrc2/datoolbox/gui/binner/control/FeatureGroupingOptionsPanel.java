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

package edu.umich.med.mrc2.datoolbox.gui.binner.control;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import edu.umich.med.mrc2.datoolbox.gui.utils.ValidatableForm;
import edu.umich.med.mrc2.datoolbox.main.config.NumberFormatStore;

public class FeatureGroupingOptionsPanel extends JPanel implements ItemListener, ValidatableForm{

	private static final long serialVersionUID = 1L;
	
	private JFormattedTextField rtGapField;
	private JComboBox<ClusterGroupingMethod> clusterGroupingMethodComboBox;
	
	public FeatureGroupingOptionsPanel() {
		super();
		setBorder(new EmptyBorder(10, 10, 10, 10));
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, 0.0, 0.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		JLabel lblNewLabel = new JLabel("Retention time gap size");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 0;
		add(lblNewLabel, gbc_lblNewLabel);
		
		rtGapField = new JFormattedTextField(
				NumberFormatStore.getDefaultRTformat());
		rtGapField.setPreferredSize(new Dimension(80, 20));
		rtGapField.setMinimumSize(new Dimension(80, 20));
		rtGapField.setColumns(10);
		GridBagConstraints gbc_rtGapField = new GridBagConstraints();
		gbc_rtGapField.insets = new Insets(0, 0, 5, 5);
		gbc_rtGapField.fill = GridBagConstraints.HORIZONTAL;
		gbc_rtGapField.gridx = 1;
		gbc_rtGapField.gridy = 0;
		add(rtGapField, gbc_rtGapField);
		
		JLabel lblNewLabel_1 = new JLabel("min.");
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 0);
		gbc_lblNewLabel_1.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel_1.gridx = 2;
		gbc_lblNewLabel_1.gridy = 0;
		add(lblNewLabel_1, gbc_lblNewLabel_1);
		
		JPanel panel = new JPanel();
		panel.setBorder(new CompoundBorder(new TitledBorder(
				new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), 
						new Color(160, 160, 160)), "Cluster sub-division parameters", 
				TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)), 
				new EmptyBorder(10, 10, 10, 10)));
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.gridwidth = 3;
		gbc_panel.insets = new Insets(0, 0, 0, 5);
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 1;
		add(panel, gbc_panel);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{0, 0, 0};
		gbl_panel.rowHeights = new int[]{0, 0};
		gbl_panel.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);
		
		JLabel lblNewLabel_2 = new JLabel("Grouping method");
		GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
		gbc_lblNewLabel_2.insets = new Insets(0, 0, 0, 5);
		gbc_lblNewLabel_2.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_2.gridx = 0;
		gbc_lblNewLabel_2.gridy = 0;
		panel.add(lblNewLabel_2, gbc_lblNewLabel_2);
		
		clusterGroupingMethodComboBox = new JComboBox<ClusterGroupingMethod>(
				new DefaultComboBoxModel<ClusterGroupingMethod>(ClusterGroupingMethod.values()));
		clusterGroupingMethodComboBox.setSelectedIndex(-1);
		GridBagConstraints gbc_clusterGroupingMethodComboBox = new GridBagConstraints();
		gbc_clusterGroupingMethodComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_clusterGroupingMethodComboBox.gridx = 1;
		gbc_clusterGroupingMethodComboBox.gridy = 0;
		panel.add(clusterGroupingMethodComboBox, gbc_clusterGroupingMethodComboBox);
		clusterGroupingMethodComboBox.addItemListener(this);
		
		
		
		
		//	To trigger proper option selection
		clusterGroupingMethodComboBox.setSelectedItem(
				ClusterGroupingMethod.CLUSTER_ON_RT);
	}
	
	public double getRTgap() {
		return Double.parseDouble(rtGapField.getText());
	}
	
	public void setRTgap(double rtGapSize) {
		rtGapField.setText(Double.toString(rtGapSize));
	}
	
	public ClusterGroupingMethod getClusterGroupingMethod() {
		return (ClusterGroupingMethod)clusterGroupingMethodComboBox.getSelectedItem();
	}
	
	public void getClusterGroupingMethod(ClusterGroupingMethod cgm) {
		clusterGroupingMethodComboBox.setSelectedItem(cgm);
	}

	@Override
	public Collection<String> validateFormData() {

		Collection<String>errors = new ArrayList<String>();
		
		
		return errors;
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		// TODO Auto-generated method stub
		
	}
}

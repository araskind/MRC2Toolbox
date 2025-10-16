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

package edu.umich.med.mrc2.datoolbox.gui.library.feditor;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import edu.umich.med.mrc2.datoolbox.data.enums.AdductSubset;
import edu.umich.med.mrc2.datoolbox.data.enums.Polarity;

public class AdductSelectorControlPanel extends JPanel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6337748110064202863L;
	private JComboBox<Polarity>polarityComboBox;
	private JComboBox<AdductSubset> adductSubsetComboBox;
	
	public AdductSelectorControlPanel(ItemListener controlListener) {
		
		setBorder(new EmptyBorder(10, 10, 10, 10));
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		JLabel lblPolarity = new JLabel("Polarity ");
		lblPolarity.setFont(new Font("Tahoma", Font.BOLD, 12));
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel.insets = new Insets(0, 0, 0, 5);
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 0;
		add(lblPolarity, gbc_lblNewLabel);
		
		polarityComboBox = new JComboBox<Polarity>(
				new DefaultComboBoxModel<Polarity>(new Polarity[] {
						Polarity.Positive,
						Polarity.Neutral,
						Polarity.Negative}));

		polarityComboBox.setSelectedItem(Polarity.Neutral);
		polarityComboBox.setPreferredSize(new Dimension(80, 22));
		polarityComboBox.setMinimumSize(new Dimension(80, 22));
		polarityComboBox.addItemListener(controlListener);
		GridBagConstraints gbc_comboBox = new GridBagConstraints();
		gbc_comboBox.insets = new Insets(0, 0, 0, 5);
		gbc_comboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBox.gridx = 1;
		gbc_comboBox.gridy = 0;
		add(polarityComboBox, gbc_comboBox);
		
		JLabel lblNewLabel_1 = new JLabel("Adduct subset ");
		lblNewLabel_1.setFont(new Font("Tahoma", Font.BOLD, 12));
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_1.insets = new Insets(0, 0, 0, 5);
		gbc_lblNewLabel_1.gridx = 2;
		gbc_lblNewLabel_1.gridy = 0;
		add(lblNewLabel_1, gbc_lblNewLabel_1);
		
		adductSubsetComboBox = new JComboBox<AdductSubset>(
				new DefaultComboBoxModel<AdductSubset>(AdductSubset.values()));
		adductSubsetComboBox.setPreferredSize(new Dimension(120, 22));
		adductSubsetComboBox.setMinimumSize(new Dimension(100, 22));
		adductSubsetComboBox.addItemListener(controlListener);
		GridBagConstraints gbc_adductSubsetComboBox = new GridBagConstraints();
		gbc_adductSubsetComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_adductSubsetComboBox.gridx = 3;
		gbc_adductSubsetComboBox.gridy = 0;
		add(adductSubsetComboBox, gbc_adductSubsetComboBox);
	}
	
	public void setPolarity(Polarity polarity) {
		polarityComboBox.setSelectedItem(polarity);
	}

	public Polarity getPolarity() {
		return (Polarity) polarityComboBox.getSelectedItem();
	}
	
	public void setAndLockFeaturePolarity(Polarity polarity) {
		polarityComboBox.setSelectedItem(polarity);
		polarityComboBox.setEnabled(false);
	}

	public void setAdductSubset(AdductSubset subset) {
		adductSubsetComboBox.setSelectedItem(subset);
	}

	public AdductSubset getAdductSubset() {
		return (AdductSubset) adductSubsetComboBox.getSelectedItem();
	}
}
















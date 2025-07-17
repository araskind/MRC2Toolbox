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

package edu.umich.med.mrc2.datoolbox.gui.adducts.adduct;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collection;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

import edu.umich.med.mrc2.datoolbox.data.Adduct;
import edu.umich.med.mrc2.datoolbox.data.enums.AdductSubset;
import edu.umich.med.mrc2.datoolbox.data.enums.Polarity;
import edu.umich.med.mrc2.datoolbox.gui.library.feditor.AdductSelectionTable;

public class AdductSelectorPanel extends JPanel implements ItemListener {

	private static final long serialVersionUID = -3669455891091965631L;

	private JComboBox polarityComboBox;
	private AdductSelectionTable adductsTable;
	private JComboBox<AdductSubset> adductSubsetComboBox;
	
	public AdductSelectorPanel() {
		
		super();
		setBorder(new EmptyBorder(10, 10, 10, 10));
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{0, 150, 150, 0};
		gbl_panel.rowHeights = new int[]{0, 0, 0, 0};
		gbl_panel.columnWeights = new double[]{0.0, 1.0, 1.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, 0.0, 1.0, Double.MIN_VALUE};
		setLayout(gbl_panel);
		
		JLabel lblNewLabel = new JLabel("Polarity");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 0;
		add(lblNewLabel, gbc_lblNewLabel);
		
		polarityComboBox = new JComboBox<Polarity>(
				new DefaultComboBoxModel<Polarity>(
						new Polarity[] {Polarity.Positive, Polarity.Negative}));
		polarityComboBox.setSelectedIndex(-1);
		polarityComboBox.addItemListener(this);
		GridBagConstraints gbc_polarityComboBox = new GridBagConstraints();
		gbc_polarityComboBox.insets = new Insets(0, 0, 5, 5);
		gbc_polarityComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_polarityComboBox.gridx = 1;
		gbc_polarityComboBox.gridy = 0;
		add(polarityComboBox, gbc_polarityComboBox);
		
		JLabel adductSubsetLabel = new JLabel("Adduct subset ");
		GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
		gbc_lblNewLabel_2.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_2.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_2.gridx = 0;
		gbc_lblNewLabel_2.gridy = 1;
		add(adductSubsetLabel, gbc_lblNewLabel_2);
		
		adductSubsetComboBox = new JComboBox<AdductSubset>(
				new DefaultComboBoxModel<AdductSubset>(
						new AdductSubset[] {
								AdductSubset.MOST_COMMON, 
								AdductSubset.COMPLETE_LIST
						}));
		adductSubsetComboBox.setSelectedItem(AdductSubset.MOST_COMMON);
		adductSubsetComboBox.addItemListener(this);
		GridBagConstraints gbc_comboBox = new GridBagConstraints();
		gbc_comboBox.insets = new Insets(0, 0, 5, 5);
		gbc_comboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBox.gridx = 1;
		gbc_comboBox.gridy = 1;
		add(adductSubsetComboBox, gbc_comboBox);
		
		adductsTable = new AdductSelectionTable();
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.gridwidth = 3;
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 2;
		add(new JScrollPane(adductsTable), gbc_scrollPane);
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		// TODO Auto-generated method stub
		if (e.getStateChange() == ItemEvent.SELECTED) {
			
			if((e.getItem() instanceof Polarity 
					|| e.getItem() instanceof AdductSubset)) {

				adductsTable.setTableModelFromAdductListForPolarityAndSubset(
						getPolarity(), getAdductSubset());
			}
		}
	}

	public Polarity getPolarity() {
		return (Polarity)polarityComboBox.getSelectedItem();
	}
	
	public AdductSubset getAdductSubset() {
		return (AdductSubset) adductSubsetComboBox.getSelectedItem();
	}
	
	public Collection<Adduct>getSelectedAdducts(){
		return adductsTable.getSelectedAdducts();
	}	
}

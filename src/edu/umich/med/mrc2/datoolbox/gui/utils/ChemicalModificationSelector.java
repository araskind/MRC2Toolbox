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

package edu.umich.med.mrc2.datoolbox.gui.utils;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import edu.umich.med.mrc2.datoolbox.data.Adduct;
import edu.umich.med.mrc2.datoolbox.data.enums.ModificationType;
import edu.umich.med.mrc2.datoolbox.data.enums.Polarity;
import edu.umich.med.mrc2.datoolbox.main.AdductManager;

public class ChemicalModificationSelector extends JPanel implements ItemListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2472296053905182215L;
	
	@SuppressWarnings("rawtypes")
	private JComboBox 
		polarityComboBox,
		chargeComboBox,
		nMerComboBox,
		modTypeComboBox,
		adductComboBox;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ChemicalModificationSelector() {

		setBorder(new EmptyBorder(10, 10, 10, 10));
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0, 0, 0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 1.0, 0.0, 1.0, 0.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);

		JLabel lblPolarity = new JLabel("Polarity");
		GridBagConstraints gbc_lblPolarity = new GridBagConstraints();
		gbc_lblPolarity.insets = new Insets(0, 0, 5, 5);
		gbc_lblPolarity.anchor = GridBagConstraints.EAST;
		gbc_lblPolarity.gridx = 0;
		gbc_lblPolarity.gridy = 0;
		add(lblPolarity, gbc_lblPolarity);

		polarityComboBox = new JComboBox();
		polarityComboBox.setModel(
				new DefaultComboBoxModel<Polarity>(Polarity.values()));
		polarityComboBox.setSelectedItem(Polarity.Neutral);
		polarityComboBox.addItemListener(this);
		GridBagConstraints gbc_comboBox = new GridBagConstraints();
		gbc_comboBox.insets = new Insets(0, 0, 5, 5);
		gbc_comboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBox.gridx = 1;
		gbc_comboBox.gridy = 0;
		add(polarityComboBox, gbc_comboBox);

		JLabel lblCharge = new JLabel("Charge");
		GridBagConstraints gbc_lblCharge = new GridBagConstraints();
		gbc_lblCharge.anchor = GridBagConstraints.EAST;
		gbc_lblCharge.insets = new Insets(0, 0, 5, 5);
		gbc_lblCharge.gridx = 2;
		gbc_lblCharge.gridy = 0;
		add(lblCharge, gbc_lblCharge);

		chargeComboBox = new JComboBox();
		chargeComboBox.setModel(new DefaultComboBoxModel(new Integer[] {0, 1, 2, 3}));
		chargeComboBox.setSelectedIndex(0);
		chargeComboBox.addItemListener(this);
		GridBagConstraints gbc_comboBox_2 = new GridBagConstraints();
		gbc_comboBox_2.insets = new Insets(0, 0, 5, 5);
		gbc_comboBox_2.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBox_2.gridx = 3;
		gbc_comboBox_2.gridy = 0;
		add(chargeComboBox, gbc_comboBox_2);

		JLabel lblNmer = new JLabel("N-mer");
		GridBagConstraints gbc_lblNmer = new GridBagConstraints();
		gbc_lblNmer.anchor = GridBagConstraints.EAST;
		gbc_lblNmer.insets = new Insets(0, 0, 5, 5);
		gbc_lblNmer.gridx = 4;
		gbc_lblNmer.gridy = 0;
		add(lblNmer, gbc_lblNmer);

		nMerComboBox = new JComboBox();
		nMerComboBox.setModel(new DefaultComboBoxModel(new Integer[] {1, 2, 3}));
		nMerComboBox.setSelectedIndex(0);
		nMerComboBox.addItemListener(this);
		GridBagConstraints gbc_comboBox_3 = new GridBagConstraints();
		gbc_comboBox_3.insets = new Insets(0, 0, 5, 0);
		gbc_comboBox_3.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBox_3.gridx = 5;
		gbc_comboBox_3.gridy = 0;
		add(nMerComboBox, gbc_comboBox_3);

		JLabel lblType = new JLabel("Type");
		GridBagConstraints gbc_lblType = new GridBagConstraints();
		gbc_lblType.insets = new Insets(0, 0, 0, 5);
		gbc_lblType.anchor = GridBagConstraints.EAST;
		gbc_lblType.gridx = 0;
		gbc_lblType.gridy = 1;
		add(lblType, gbc_lblType);

		modTypeComboBox = new JComboBox<ModificationType>();
		modTypeComboBox.setModel(new DefaultComboBoxModel<ModificationType>(
			new ModificationType[] {
				ModificationType.ADDUCT,
				ModificationType.LOSS,
				ModificationType.REPEAT,
				ModificationType.COMPOSITE
			}));

		modTypeComboBox.setSelectedItem(ModificationType.ADDUCT);
		modTypeComboBox.addItemListener(this);
		GridBagConstraints gbc_comboBox_1 = new GridBagConstraints();
		gbc_comboBox_1.insets = new Insets(0, 0, 0, 5);
		gbc_comboBox_1.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBox_1.gridx = 1;
		gbc_comboBox_1.gridy = 1;
		add(modTypeComboBox, gbc_comboBox_1);

		JLabel lblAdduct = new JLabel("Adduct");
		lblAdduct.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_lblAdduct = new GridBagConstraints();
		gbc_lblAdduct.anchor = GridBagConstraints.EAST;
		gbc_lblAdduct.insets = new Insets(0, 0, 0, 5);
		gbc_lblAdduct.gridx = 2;
		gbc_lblAdduct.gridy = 1;
		add(lblAdduct, gbc_lblAdduct);

		adductComboBox = new JComboBox<Adduct>();
		GridBagConstraints gbc_comboBox_4 = new GridBagConstraints();
		gbc_comboBox_4.gridwidth = 3;
		gbc_comboBox_4.insets = new Insets(0, 0, 0, 5);
		gbc_comboBox_4.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBox_4.gridx = 3;
		gbc_comboBox_4.gridy = 1;
		add(adductComboBox, gbc_comboBox_4);

		updateSelectors();
	}

	@SuppressWarnings("unchecked")
	private void updateSelectors() {

		polarityComboBox.removeItemListener(this);
		modTypeComboBox.removeItemListener(this);
		chargeComboBox.removeItemListener(this);
		nMerComboBox.removeItemListener(this);

		try {
			if(!((ModificationType)modTypeComboBox.getSelectedItem()).equals(ModificationType.ADDUCT)
					&& !((ModificationType)modTypeComboBox.getSelectedItem()).equals(ModificationType.COMPOSITE)) {

				polarityComboBox.setSelectedItem(Polarity.Neutral);
				nMerComboBox.setSelectedItem(1);
				chargeComboBox.setSelectedItem(0);
				polarityComboBox.setEnabled(false);
				nMerComboBox.setEnabled(false);
				chargeComboBox.setEnabled(false);
			}
			else {
				if(((Polarity)polarityComboBox.getSelectedItem()).equals(Polarity.Neutral)) {
					chargeComboBox.setSelectedItem(0);
				}
				else {
					if((int) chargeComboBox.getSelectedItem() == 0)
						chargeComboBox.setSelectedItem(1);
				}
				polarityComboBox.setEnabled(true);
				nMerComboBox.setEnabled(true);
				chargeComboBox.setEnabled(true);
			}
			Polarity polarity = ((Polarity)polarityComboBox.getSelectedItem());
			ModificationType mtype = (ModificationType)modTypeComboBox.getSelectedItem();
			int charge = (int) chargeComboBox.getSelectedItem();
			int nMer = (int) nMerComboBox.getSelectedItem();

			Adduct[] modArray = AdductManager.getAdductList().stream().
					filter(m -> m.getModificationType().equals(mtype)).
					filter(m -> m.getPolarity().equals(polarity)).
					filter(m -> (m.getCharge() * m.getPolarity().getSign() == charge )).
					filter(m -> (m.getOligomericState() == nMer )).
					sorted().toArray(size -> new Adduct[size]);

			adductComboBox.setModel(new DefaultComboBoxModel<Adduct>(modArray));
			adductComboBox.setSelectedItem(
					AdductManager.getDefaultAdductForPolarity(polarity));
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		polarityComboBox.addItemListener(this);
		modTypeComboBox.addItemListener(this);
		chargeComboBox.addItemListener(this);
		nMerComboBox.addItemListener(this);
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		updateSelectors();
	}

	public Adduct getSelectedModification() {
		return (Adduct) adductComboBox.getSelectedItem();
	}
}






















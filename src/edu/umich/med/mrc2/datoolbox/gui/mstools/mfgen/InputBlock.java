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

package edu.umich.med.mrc2.datoolbox.gui.mstools.mfgen;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.NumberFormat;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import edu.umich.med.mrc2.datoolbox.data.enums.Polarity;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.ChemicalModificationSelector;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;

public class InputBlock extends JPanel implements ItemListener, ActionListener {

	private JButton btnGenerate;
	private JFormattedTextField ppmTextField;
	private JComboBox<Polarity> polarityComboBox;
	private JFormattedTextField mzTextField;
	private Polarity activePolarity;

	private static final NumberFormat mzFormat = MRC2ToolBoxConfiguration.getMzFormat();
	public static final NumberFormat ppmFormat = MRC2ToolBoxConfiguration.getPpmFormat();

	private ChemicalModificationSelector chModPanel;

	public InputBlock() {
		super();
		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(10, 10, 10, 10));
		add(panel, BorderLayout.NORTH);

		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWeights = new double[] { 1.0, 1.0, 0.0, 0.0, 1.0 };
		gbl_panel.columnWidths = new int[] { 50, 0, 78, 97, 139 };

		gbl_panel.rowWeights = new double[] { 0.0, 0.0 };
		panel.setLayout(gbl_panel);

		JLabel mzLabel = new JLabel("Input mass");
		mzLabel.setHorizontalAlignment(SwingConstants.LEFT);
		GridBagConstraints gbc_mzLabel = new GridBagConstraints();
		gbc_mzLabel.anchor = GridBagConstraints.EAST;
		gbc_mzLabel.insets = new Insets(0, 0, 5, 5);
		gbc_mzLabel.gridx = 0;
		gbc_mzLabel.gridy = 0;
		panel.add(mzLabel, gbc_mzLabel);

		mzTextField = new JFormattedTextField(mzFormat);
		mzTextField.setColumns(15);
		GridBagConstraints gbc_mzTextField = new GridBagConstraints();
		gbc_mzTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_mzTextField.insets = new Insets(0, 0, 5, 5);
		gbc_mzTextField.gridx = 1;
		gbc_mzTextField.gridy = 0;
		panel.add(mzTextField, gbc_mzTextField);

		JLabel lblErrorPpm = new JLabel("Error, ppm");
		GridBagConstraints gbc_lblErrorPpm = new GridBagConstraints();
		gbc_lblErrorPpm.anchor = GridBagConstraints.EAST;
		gbc_lblErrorPpm.insets = new Insets(0, 0, 5, 5);
		gbc_lblErrorPpm.gridx = 2;
		gbc_lblErrorPpm.gridy = 0;
		panel.add(lblErrorPpm, gbc_lblErrorPpm);

		ppmTextField = new JFormattedTextField(ppmFormat);
		ppmTextField.setColumns(6);
		ppmTextField.setText(Double.toString(MRC2ToolBoxConfiguration.getMassAccuracy()));
		GridBagConstraints gbc_ppmTextField = new GridBagConstraints();
		gbc_ppmTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_ppmTextField.insets = new Insets(0, 0, 5, 5);
		gbc_ppmTextField.gridx = 3;
		gbc_ppmTextField.gridy = 0;
		panel.add(ppmTextField, gbc_ppmTextField);

		chModPanel = new ChemicalModificationSelector();
		GridBagConstraints gbc_chmodSelector = new GridBagConstraints();
		gbc_chmodSelector.fill = GridBagConstraints.HORIZONTAL;
		gbc_chmodSelector.gridwidth = 5;
		gbc_chmodSelector.insets = new Insets(0, 0, 5, 5);
		gbc_chmodSelector.gridx = 0;
		gbc_chmodSelector.gridy = 1;
		panel.add(chModPanel, gbc_chmodSelector);

		btnGenerate = new JButton(MainActionCommands.GENERATE_FORMULA_COMMAND.getName());
		btnGenerate.setActionCommand(MainActionCommands.GENERATE_FORMULA_COMMAND.getName());
		btnGenerate.addActionListener(this);
		GridBagConstraints gbc_btnGenerate = new GridBagConstraints();
		gbc_btnGenerate.insets = new Insets(0, 0, 5, 0);
		gbc_btnGenerate.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnGenerate.anchor = GridBagConstraints.NORTH;
		gbc_btnGenerate.gridx = 4;
		gbc_btnGenerate.gridy = 0;
		panel.add(btnGenerate, gbc_btnGenerate);
	}

	private void updateAdductSelector(Polarity activePolarity2) {
		// TODO Auto-generated method stub

	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub

	}

}

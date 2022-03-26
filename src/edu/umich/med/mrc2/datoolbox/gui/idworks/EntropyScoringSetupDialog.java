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

package edu.umich.med.mrc2.datoolbox.gui.idworks;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.prefs.Preferences;

import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import edu.umich.med.mrc2.datoolbox.data.enums.MassErrorType;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;

public class EntropyScoringSetupDialog extends JDialog implements BackedByPreferences {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3210896696637124641L;
	private static final Icon entropyIcon = GuiUtils.getIcon("spectrumEntropy", 32);

	private JFormattedTextField massErrorTextField;
	private JComboBox massErrorTypeComboBox;
	private JFormattedTextField noiseCutoffTextField;
		
	@SuppressWarnings("rawtypes")
	public EntropyScoringSetupDialog(ActionListener actionListener) {
		super();
		setTitle("Entropy-based MSMS score calculation parameters");
		setIconImage(((ImageIcon) entropyIcon).getImage());
		setPreferredSize(new Dimension(400, 150));
		setSize(new Dimension(400, 150));
		setModalityType(ModalityType.APPLICATION_MODAL);
		setResizable(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		
		JPanel dataPanel = new JPanel();
		dataPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(dataPanel, BorderLayout.CENTER);
		GridBagLayout gbl_dataPanel = new GridBagLayout();
		gbl_dataPanel.columnWidths = new int[]{0, 0, 0, 0};
		gbl_dataPanel.rowHeights = new int[]{0, 0, 0};
		gbl_dataPanel.columnWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_dataPanel.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		dataPanel.setLayout(gbl_dataPanel);
		
		JLabel lblNewLabel = new JLabel("Mass window");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 0;
		dataPanel.add(lblNewLabel, gbc_lblNewLabel);
		
		massErrorTextField = new JFormattedTextField(new DecimalFormat("###.###"));
		massErrorTextField.setColumns(10);
		GridBagConstraints gbc_massErrorTextField = new GridBagConstraints();
		gbc_massErrorTextField.insets = new Insets(0, 0, 5, 5);
		gbc_massErrorTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_massErrorTextField.gridx = 1;
		gbc_massErrorTextField.gridy = 0;
		dataPanel.add(massErrorTextField, gbc_massErrorTextField);
		
		massErrorTypeComboBox = new JComboBox<MassErrorType>(
				new DefaultComboBoxModel<MassErrorType>(MassErrorType.values()));
		massErrorTypeComboBox.setMinimumSize(new Dimension(80, 22));
		massErrorTypeComboBox.setPreferredSize(new Dimension(80, 22));
		GridBagConstraints gbc_massErrorTypeComboBox = new GridBagConstraints();
		gbc_massErrorTypeComboBox.insets = new Insets(0, 0, 5, 0);
		gbc_massErrorTypeComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_massErrorTypeComboBox.gridx = 2;
		gbc_massErrorTypeComboBox.gridy = 0;
		dataPanel.add(massErrorTypeComboBox, gbc_massErrorTypeComboBox);
		
		JLabel lblNewLabel_1 = new JLabel("Noise cutoff");
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_1.insets = new Insets(0, 0, 0, 5);
		gbc_lblNewLabel_1.gridx = 0;
		gbc_lblNewLabel_1.gridy = 1;
		dataPanel.add(lblNewLabel_1, gbc_lblNewLabel_1);
		
		noiseCutoffTextField = new JFormattedTextField(new DecimalFormat("###.#"));
		noiseCutoffTextField.setColumns(10);
		GridBagConstraints gbc_noiseCutoffTextField = new GridBagConstraints();
		gbc_noiseCutoffTextField.insets = new Insets(0, 0, 0, 5);
		gbc_noiseCutoffTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_noiseCutoffTextField.gridx = 1;
		gbc_noiseCutoffTextField.gridy = 1;
		dataPanel.add(noiseCutoffTextField, gbc_noiseCutoffTextField);
		
		JLabel lblNewLabel_2 = new JLabel("% (0.0 - 100.0)");
		GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
		gbc_lblNewLabel_2.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel_2.gridx = 2;
		gbc_lblNewLabel_2.gridy = 1;
		dataPanel.add(lblNewLabel_2, gbc_lblNewLabel_2);
		
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

		JButton btnSave = new JButton(MainActionCommands.RECALCULATE_SPECTRUM_ENTROPY_SCORES.getName());
		btnSave.setActionCommand(MainActionCommands.RECALCULATE_SPECTRUM_ENTROPY_SCORES.getName());
		btnSave.addActionListener(actionListener);
		panel.add(btnSave);
		JRootPane rootPane = SwingUtilities.getRootPane(btnSave);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(btnSave);
		
		loadPreferences();
		pack();
	}
	
	public double getMassError() {
		
		double massError = 0.0d;
		if(massErrorTextField.getText() != null 
				&& !massErrorTextField.getText().trim().isEmpty())
			massError = Double.parseDouble(massErrorTextField.getText().trim());
		
		return massError;
	}
	
	public MassErrorType getMassErrorType() {
		return (MassErrorType)massErrorTypeComboBox.getSelectedItem();
	}	
	
	public double getNoizeCutoff() {
		
		double noiseCutoff = 0.0d;
		if(noiseCutoffTextField.getText() != null 
				&& !noiseCutoffTextField.getText().trim().isEmpty())
			noiseCutoff = Double.parseDouble(noiseCutoffTextField.getText().trim());
		
		return noiseCutoff / 100.0d;
	}
	
	public Collection<String>validateParameters(){
		
		ArrayList<String>errors = new ArrayList<String>();
		if(getMassError() == 0)
			errors.add("Mass error must be > 0");
		
		if(getMassErrorType() == null)
			errors.add("Mass error type must be specified");
		
		return errors;
	}

	@Override
	public void loadPreferences(Preferences prefs) {
		loadPreferences();
	}

	@Override
	public void loadPreferences() {
		
		double massError = 
				MRC2ToolBoxConfiguration.getSpectrumEntropyMassError();
		massErrorTextField.setText(Double.toString(massError));
		
		massErrorTypeComboBox.setSelectedItem(
				MRC2ToolBoxConfiguration.getSpectrumEntropyMassErrorType());
		
		double noiseCutoff = 
				MRC2ToolBoxConfiguration.getSpectrumEntropyNoiseCutoff() * 100.0d;
		noiseCutoffTextField.setText(Double.toString(noiseCutoff));
	}

	@Override
	public void savePreferences() {
		
		double massError = getMassError();
		if(massError > 0.0d)
			MRC2ToolBoxConfiguration.setSpectrumEntropyMassError(massError);
		
		MassErrorType met = getMassErrorType();
		if(met != null)
			MRC2ToolBoxConfiguration.setSpectrumEntropyMassErrorType(met);
		
		MRC2ToolBoxConfiguration.setSpectrumEntropyNoiseCutoff(getNoizeCutoff());
	}
}
















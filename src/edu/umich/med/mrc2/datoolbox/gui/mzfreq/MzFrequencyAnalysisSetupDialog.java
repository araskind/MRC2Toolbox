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

package edu.umich.med.mrc2.datoolbox.gui.mzfreq;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
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

public class MzFrequencyAnalysisSetupDialog extends JDialog implements BackedByPreferences{

	/**
	 * 
	 */
	private static final long serialVersionUID = -7961141412461487743L;
	
	private static final Icon mzFrequencyIcon = GuiUtils.getIcon("mzFrequency", 32);
	private JComboBox massErrorTypeComboBox;
	private JFormattedTextField massWidowTextField;
	
	private Preferences preferences;
	public static final String MASS_WINDOW_VALUE = "MASS_WINDOW_VALUE";
	public static final String MASS_WINDOW_TYPE = "MASS_WINDOW_TYPE";
	
	public MzFrequencyAnalysisSetupDialog(
			ActionListener actionListener,
			String title) {
		super();
		
		setTitle(title);
		setIconImage(((ImageIcon) mzFrequencyIcon).getImage());
		setSize(new Dimension(400, 150));
		setPreferredSize(new Dimension(400, 150));
		setModalityType(ModalityType.APPLICATION_MODAL);
		setResizable(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		
		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(panel_1, BorderLayout.CENTER);
		GridBagLayout gbl_panel_1 = new GridBagLayout();
		gbl_panel_1.columnWidths = new int[]{0, 0, 0, 0};
		gbl_panel_1.rowHeights = new int[]{0, 0};
		gbl_panel_1.columnWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_panel_1.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		panel_1.setLayout(gbl_panel_1);
		
		JLabel lblNewLabel = new JLabel("M/Z binning window");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.insets = new Insets(0, 0, 0, 5);
		gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 0;
		panel_1.add(lblNewLabel, gbc_lblNewLabel);
		
		massWidowTextField = new JFormattedTextField(MRC2ToolBoxConfiguration.getPpmFormat());
		massWidowTextField.setColumns(10);
		GridBagConstraints gbc_formattedTextField = new GridBagConstraints();
		gbc_formattedTextField.insets = new Insets(0, 0, 0, 5);
		gbc_formattedTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_formattedTextField.gridx = 1;
		gbc_formattedTextField.gridy = 0;
		panel_1.add(massWidowTextField, gbc_formattedTextField);
		
		massErrorTypeComboBox = new JComboBox<MassErrorType>(
				new DefaultComboBoxModel<MassErrorType>(MassErrorType.values()));
		massErrorTypeComboBox.setMinimumSize(new Dimension(80, 22));
		massErrorTypeComboBox.setPreferredSize(new Dimension(80, 22));
		GridBagConstraints gbc_comboBox = new GridBagConstraints();
		gbc_comboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBox.gridx = 2;
		gbc_comboBox.gridy = 0;
		panel_1.add(massErrorTypeComboBox, gbc_comboBox);
		
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

		JButton btnRun = new JButton(MainActionCommands.RUN_MZ_FREQUENCY_ANALYSIS_COMMAND.getName());
		btnRun.setActionCommand(MainActionCommands.RUN_MZ_FREQUENCY_ANALYSIS_COMMAND.getName());
		btnRun.addActionListener(actionListener);
		panel.add(btnRun);
		JRootPane rootPane = SwingUtilities.getRootPane(btnRun);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(btnRun);
		
		loadPreferences();
		pack();
	}
	
	@Override
	public void dispose() {
		
		savePreferences();
		super.dispose();
	}

	public double getMZWindow() {
		
		String valueString = massWidowTextField.getText().trim();
		if(!valueString.isEmpty())
			return Double.parseDouble(valueString);
		else	
		return 0.0d;		
	}
	
	public MassErrorType getMassErrorType() {
		return (MassErrorType)massErrorTypeComboBox.getSelectedItem();		
	}

	@Override
	public void loadPreferences() {
		loadPreferences(Preferences.userNodeForPackage(this.getClass()));
	}

	@Override
	public void loadPreferences(Preferences prefs) {

		preferences = prefs;

		double massError = preferences.getDouble(MASS_WINDOW_VALUE, 20.0d);
		massWidowTextField.setText(Double.toString(massError));

		MassErrorType met = 
				MassErrorType.getTypeByName(preferences.get(MASS_WINDOW_TYPE, MassErrorType.mDa.name()));
		if(met == null)
			met = MassErrorType.mDa;
			
		massErrorTypeComboBox.setSelectedItem(met);
	}

	@Override
	public void savePreferences() {

		preferences = Preferences.userNodeForPackage(this.getClass());

		if(!massWidowTextField.getText().isEmpty())
			preferences.putDouble(MASS_WINDOW_VALUE, Double.parseDouble(massWidowTextField.getText()));

		preferences.put(MASS_WINDOW_TYPE, getMassErrorType().name());
	}
}

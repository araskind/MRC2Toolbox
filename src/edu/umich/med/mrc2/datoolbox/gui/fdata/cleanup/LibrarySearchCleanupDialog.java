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

package edu.umich.med.mrc2.datoolbox.gui.fdata.cleanup;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.prefs.Preferences;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JSpinner;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;

public class LibrarySearchCleanupDialog extends JDialog implements BackedByPreferences{

	private static final long serialVersionUID = 1L;
	private static final Icon dialogIcon = GuiUtils.getIcon("filterIdStatus", 32);
	
	private Preferences preferences;
	private JSpinner maxPercentMissingSpinner;
	private JFormattedTextField maxDistanceBetweenAdductsTextField;
	private JSpinner minPercentOfMinorAdductSpinner;
	private JSpinner minCorrelationBetweenAdductsSpinner;
	public static final String MAX_PERCENT_MISSING = "MAX_PERCENT_MISSING";		
	public static final String MAX_RT_DISTANCE_BETWEEN_ADDUCTS = "MAX_RT_DISTANCE_BETWEEN_ADDUCTS";
	public static final String MIN_PERCENT_OF_MINOR_ADDUCT = "MIN_PERCENT_OF_MINOR_ADDUCT";
	public static final String MIN_CORRELATION_BETWEEN_ADDUCTS = "MIN_CORRELATION_BETWEEN_ADDUCTS";
	
	public LibrarySearchCleanupDialog(ActionListener actionListener) {
		super();
		setTitle("Cleanup library search results");
		setIconImage(((ImageIcon) dialogIcon).getImage());
		setPreferredSize(new Dimension(400, 200));
		setModalityType(ModalityType.APPLICATION_MODAL);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		JPanel dataPanel = new JPanel();
		dataPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(dataPanel, BorderLayout.CENTER);
		GridBagLayout gbl_dataPanel = new GridBagLayout();
		gbl_dataPanel.columnWidths = new int[]{0, 0, 0, 0};
		gbl_dataPanel.rowHeights = new int[]{0, 0, 0, 0, 0};
		gbl_dataPanel.columnWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_dataPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		dataPanel.setLayout(gbl_dataPanel);
		
		JLabel lblNewLabel = new JLabel("Max. % missing in samples");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 0;
		dataPanel.add(lblNewLabel, gbc_lblNewLabel);
		
		maxPercentMissingSpinner = new JSpinner();
		maxPercentMissingSpinner.setModel(new SpinnerNumberModel(0, 0, 100, 1));
		maxPercentMissingSpinner.setPreferredSize(new Dimension(80, 20));
		GridBagConstraints gbc_maxPercentMissingSpinner = new GridBagConstraints();
		gbc_maxPercentMissingSpinner.insets = new Insets(0, 0, 5, 5);
		gbc_maxPercentMissingSpinner.gridx = 1;
		gbc_maxPercentMissingSpinner.gridy = 0;
		dataPanel.add(maxPercentMissingSpinner, gbc_maxPercentMissingSpinner);
		
		JLabel lblNewLabel_1 = new JLabel("Max. distance between adducts");
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_1.gridx = 0;
		gbc_lblNewLabel_1.gridy = 1;
		dataPanel.add(lblNewLabel_1, gbc_lblNewLabel_1);
		
		maxDistanceBetweenAdductsTextField = 
				new JFormattedTextField(MRC2ToolBoxConfiguration.getRtFormat());
		GridBagConstraints gbc_maxDistanceBetweenAdductsTextField = new GridBagConstraints();
		gbc_maxDistanceBetweenAdductsTextField.insets = new Insets(0, 0, 5, 5);
		gbc_maxDistanceBetweenAdductsTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_maxDistanceBetweenAdductsTextField.gridx = 1;
		gbc_maxDistanceBetweenAdductsTextField.gridy = 1;
		dataPanel.add(maxDistanceBetweenAdductsTextField, gbc_maxDistanceBetweenAdductsTextField);
		
		JLabel lblNewLabel_3 = new JLabel("min.");
		GridBagConstraints gbc_lblNewLabel_3 = new GridBagConstraints();
		gbc_lblNewLabel_3.insets = new Insets(0, 0, 5, 0);
		gbc_lblNewLabel_3.gridx = 2;
		gbc_lblNewLabel_3.gridy = 1;
		dataPanel.add(lblNewLabel_3, gbc_lblNewLabel_3);
		
		JLabel lblNewLabel_2 = new JLabel("Min. % of minor adduct");
		GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
		gbc_lblNewLabel_2.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_2.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_2.gridx = 0;
		gbc_lblNewLabel_2.gridy = 2;
		dataPanel.add(lblNewLabel_2, gbc_lblNewLabel_2);
		
		minPercentOfMinorAdductSpinner = new JSpinner();
		minPercentOfMinorAdductSpinner.setModel(new SpinnerNumberModel(0, 0, 100, 1));
		GridBagConstraints gbc_minPercentOfMinorAdductSpinner = new GridBagConstraints();
		gbc_minPercentOfMinorAdductSpinner.insets = new Insets(0, 0, 5, 5);
		gbc_minPercentOfMinorAdductSpinner.fill = GridBagConstraints.HORIZONTAL;
		gbc_minPercentOfMinorAdductSpinner.gridx = 1;
		gbc_minPercentOfMinorAdductSpinner.gridy = 2;
		dataPanel.add(minPercentOfMinorAdductSpinner, gbc_minPercentOfMinorAdductSpinner);
		
		JLabel lblNewLabel_4 = new JLabel("Min. correlation between adducts");
		GridBagConstraints gbc_lblNewLabel_4 = new GridBagConstraints();
		gbc_lblNewLabel_4.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_4.insets = new Insets(0, 0, 0, 5);
		gbc_lblNewLabel_4.gridx = 0;
		gbc_lblNewLabel_4.gridy = 3;
		dataPanel.add(lblNewLabel_4, gbc_lblNewLabel_4);
		
		minCorrelationBetweenAdductsSpinner = new JSpinner();
		minCorrelationBetweenAdductsSpinner.setModel(new SpinnerNumberModel(0.0, 0.0, 1.0, 0.01));
		GridBagConstraints gbc_spinner = new GridBagConstraints();
		gbc_spinner.fill = GridBagConstraints.HORIZONTAL;
		gbc_spinner.insets = new Insets(0, 0, 0, 5);
		gbc_spinner.gridx = 1;
		gbc_spinner.gridy = 3;
		dataPanel.add(minCorrelationBetweenAdductsSpinner, gbc_spinner);

		JPanel buttonPanel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) buttonPanel.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);

		JButton btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(e -> dispose());
		buttonPanel.add(btnCancel);

		JButton btnSave = new JButton(
				MainActionCommands.CLEANUP_LIBRARY_SEARCH_RESULTS_COMMAND.getName());
		btnSave.setActionCommand(
				MainActionCommands.CLEANUP_LIBRARY_SEARCH_RESULTS_COMMAND.getName());
		btnSave.addActionListener(actionListener);
		buttonPanel.add(btnSave);
		
		JRootPane rootPane = SwingUtilities.getRootPane(btnSave);		
		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		rootPane.registerKeyboardAction(al -> dispose(), stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(btnSave);

		loadPreferences();
		pack();
	}
	
	@Override
	public void dispose() {
		savePreferences();
		super.dispose();
	}
	
	public int getMaxPercentMissing() {
		return (int)maxPercentMissingSpinner.getValue();
	}
	
	public double getMaxDistanceBetweenAdducts() {
		return Double.parseDouble(maxDistanceBetweenAdductsTextField.getText());
	}
	
	public int getMinPercentOfMinorAdduct() {
		return (int)minPercentOfMinorAdductSpinner.getValue();
	}
	
	public double getMinCorrelationBetweenAdducts() {
		return (double)minCorrelationBetweenAdductsSpinner.getValue();
	}

	@Override
	public void loadPreferences(Preferences prefs) {
	    preferences = prefs;
	    maxPercentMissingSpinner.setValue( preferences.getInt(MAX_PERCENT_MISSING, 10));
	    maxDistanceBetweenAdductsTextField.setText(
	    		Double.toString(preferences.getDouble(MAX_RT_DISTANCE_BETWEEN_ADDUCTS, 0.025d)));
	    minPercentOfMinorAdductSpinner.setValue(preferences.getInt(MIN_PERCENT_OF_MINOR_ADDUCT, 10));
	    minCorrelationBetweenAdductsSpinner.setValue(preferences.getDouble(MIN_CORRELATION_BETWEEN_ADDUCTS, 0.8d));
	}

	@Override
	public void loadPreferences() {
	    loadPreferences(Preferences.userNodeForPackage(this.getClass()));

	}

	@Override
	public void savePreferences() {
	    preferences = Preferences.userNodeForPackage(this.getClass());
	    preferences.putInt(MAX_PERCENT_MISSING, getMaxPercentMissing());
	    preferences.putDouble(MAX_RT_DISTANCE_BETWEEN_ADDUCTS, getMaxDistanceBetweenAdducts());
	    preferences.putInt(MIN_PERCENT_OF_MINOR_ADDUCT, getMinPercentOfMinorAdduct());
	    preferences.putDouble(MIN_CORRELATION_BETWEEN_ADDUCTS, getMinCorrelationBetweenAdducts());
	}
}



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

package edu.umich.med.mrc2.datoolbox.gui.fdata;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JSpinner;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import edu.umich.med.mrc2.datoolbox.data.enums.DataImputationType;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;

public class DataImputationSetupDialog extends JDialog {
	
	private JButton cancelButton;
	private JButton imputeButton;
	private JSpinner knnSpinner;
	private JComboBox<DataImputationType> impMethodComboBox;

	public DataImputationSetupDialog(ActionListener listener) {

		super(MRC2ToolBoxCore.getMainWindow(), "Set parameters for missing data imputation", true);

		setSize(new Dimension(400, 220));
		setPreferredSize(new Dimension(400, 220));
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		getContentPane().setLayout(new BorderLayout(0, 0));
		
		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(panel, BorderLayout.CENTER);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{0, 0, 0};
		gbl_panel.rowHeights = new int[]{0, 0, 0, 0, 0, 0};
		gbl_panel.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);
		
		JLabel lblNewLabel = new JLabel("  ");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 0;
		panel.add(lblNewLabel, gbc_lblNewLabel);
		
		JLabel lblImputationMethod = new JLabel("Imputation method");
		GridBagConstraints gbc_lblImputationMethod = new GridBagConstraints();
		gbc_lblImputationMethod.insets = new Insets(0, 0, 5, 5);
		gbc_lblImputationMethod.anchor = GridBagConstraints.EAST;
		gbc_lblImputationMethod.gridx = 0;
		gbc_lblImputationMethod.gridy = 1;
		panel.add(lblImputationMethod, gbc_lblImputationMethod);
		
		impMethodComboBox = new JComboBox<DataImputationType>();
		impMethodComboBox.setPreferredSize(new Dimension(200, 25));
		impMethodComboBox.setMinimumSize(new Dimension(200, 25));
		
		@SuppressWarnings("rawtypes")
		ComboBoxModel model = new DefaultComboBoxModel<DataImputationType>(DataImputationType.values());
		impMethodComboBox.setModel(model);
		
		GridBagConstraints gbc_impMethodComboBox = new GridBagConstraints();
		gbc_impMethodComboBox.insets = new Insets(0, 0, 5, 0);
		gbc_impMethodComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_impMethodComboBox.gridx = 1;
		gbc_impMethodComboBox.gridy = 1;
		panel.add(impMethodComboBox, gbc_impMethodComboBox);
		
		JLabel lblNnNumber = new JLabel("NN number (for KNN only)");
		GridBagConstraints gbc_lblNnNumber = new GridBagConstraints();
		gbc_lblNnNumber.anchor = GridBagConstraints.EAST;
		gbc_lblNnNumber.insets = new Insets(0, 0, 5, 5);
		gbc_lblNnNumber.gridx = 0;
		gbc_lblNnNumber.gridy = 2;
		panel.add(lblNnNumber, gbc_lblNnNumber);
		
		knnSpinner = new JSpinner();
		knnSpinner.setModel(new SpinnerNumberModel(new Integer(3), new Integer(1), null, new Integer(1)));
		knnSpinner.setPreferredSize(new Dimension(50, 25));
		GridBagConstraints gbc_spinner = new GridBagConstraints();
		gbc_spinner.insets = new Insets(0, 0, 5, 0);
		gbc_spinner.anchor = GridBagConstraints.WEST;
		gbc_spinner.gridx = 1;
		gbc_spinner.gridy = 2;
		panel.add(knnSpinner, gbc_spinner);
		
		JLabel lblNewLabel_1 = new JLabel("  ");
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_1.gridx = 0;
		gbc_lblNewLabel_1.gridy = 3;
		panel.add(lblNewLabel_1, gbc_lblNewLabel_1);
		
		cancelButton = new JButton("Cancel");
		GridBagConstraints gbc_btnNewButton_1 = new GridBagConstraints();
		gbc_btnNewButton_1.anchor = GridBagConstraints.WEST;
		gbc_btnNewButton_1.insets = new Insets(0, 0, 0, 5);
		gbc_btnNewButton_1.gridx = 0;
		gbc_btnNewButton_1.gridy = 4;
		panel.add(cancelButton, gbc_btnNewButton_1);
		
		imputeButton = new JButton("Impute missing data");
		imputeButton.setActionCommand(MainActionCommands.IMPUTE_DATA_COMMAND.getName());
		imputeButton.addActionListener(listener);
		GridBagConstraints gbc_btnNewButton = new GridBagConstraints();
		gbc_btnNewButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnNewButton.gridx = 1;
		gbc_btnNewButton.gridy = 4;
		panel.add(imputeButton, gbc_btnNewButton);
		
		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				disposeWithoutSavingPreferences();
			}
		};
		cancelButton.addActionListener(al);
		JRootPane rootPane = SwingUtilities.getRootPane(imputeButton);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(imputeButton);

		pack();
		setLocationRelativeTo(MRC2ToolBoxCore.getMainWindow());
		setVisible(false);
	}
	
	private void disposeWithoutSavingPreferences() {
		super.dispose();
	}
	
	public int getKnnParameter() {
		
		return (int) knnSpinner.getValue();
	}
	
	public DataImputationType getSelectedImputationMethod() {
		
		return (DataImputationType) impMethodComboBox.getSelectedItem();
	}
}


























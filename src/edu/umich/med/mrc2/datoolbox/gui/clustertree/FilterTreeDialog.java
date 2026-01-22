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

package edu.umich.med.mrc2.datoolbox.gui.clustertree;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.utils.Range;

public class FilterTreeDialog extends JDialog implements ActionListener {

	/**
	 *
	 */
	private static final long serialVersionUID = -5028494778833454970L;

	private static final String RESET_COMMAND = "Reset";
	private JTextField nameTextField;
	private JFormattedTextField rtFromTextField;
	private JFormattedTextField rtToTextField;
	private JFormattedTextField bpmzMinTextField;
	private JFormattedTextField bpMzMaxTextField;
	private JButton cancelButton;
	private JButton filterButton;
	private JButton resetButton;
	private JCheckBox multIdCheckBox;

	public FilterTreeDialog(ActionListener listener) {

		super();
		setTitle("Filter feature clusters");
		setModalityType(ModalityType.APPLICATION_MODAL);
		setSize(new Dimension(400, 230));
		setPreferredSize(new Dimension(400, 230));
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(panel, BorderLayout.CENTER);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[] { 0, 110, 32, 0, 0 };
		gbl_panel.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0 };
		gbl_panel.columnWeights = new double[] { 0.0, 1.0, 0.0, 1.0, Double.MIN_VALUE };
		gbl_panel.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		panel.setLayout(gbl_panel);

		JLabel lblNewLabel = new JLabel("Feature name contains: ");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 0;
		panel.add(lblNewLabel, gbc_lblNewLabel);

		nameTextField = new JTextField();
		GridBagConstraints gbc_nameTextField = new GridBagConstraints();
		gbc_nameTextField.gridwidth = 3;
		gbc_nameTextField.insets = new Insets(0, 0, 5, 0);
		gbc_nameTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_nameTextField.gridx = 1;
		gbc_nameTextField.gridy = 0;
		panel.add(nameTextField, gbc_nameTextField);
		nameTextField.setColumns(10);

		JLabel lblRetentionTimeFrom = new JLabel("Retention time from ");
		GridBagConstraints gbc_lblRetentionTimeFrom = new GridBagConstraints();
		gbc_lblRetentionTimeFrom.anchor = GridBagConstraints.EAST;
		gbc_lblRetentionTimeFrom.insets = new Insets(0, 0, 5, 5);
		gbc_lblRetentionTimeFrom.gridx = 0;
		gbc_lblRetentionTimeFrom.gridy = 1;
		panel.add(lblRetentionTimeFrom, gbc_lblRetentionTimeFrom);

		rtFromTextField = new JFormattedTextField(MRC2ToolBoxConfiguration.getRtFormat());
		rtFromTextField.setColumns(10);
		GridBagConstraints gbc_rtFromTextField = new GridBagConstraints();
		gbc_rtFromTextField.insets = new Insets(0, 0, 5, 5);
		gbc_rtFromTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_rtFromTextField.gridx = 1;
		gbc_rtFromTextField.gridy = 1;
		panel.add(rtFromTextField, gbc_rtFromTextField);

		JLabel lblTo = new JLabel(" to");
		GridBagConstraints gbc_lblTo = new GridBagConstraints();
		gbc_lblTo.insets = new Insets(0, 0, 5, 5);
		gbc_lblTo.anchor = GridBagConstraints.EAST;
		gbc_lblTo.gridx = 2;
		gbc_lblTo.gridy = 1;
		panel.add(lblTo, gbc_lblTo);

		rtToTextField = new JFormattedTextField(MRC2ToolBoxConfiguration.getRtFormat());
		rtToTextField.setColumns(10);
		GridBagConstraints gbc_rtToTextField = new GridBagConstraints();
		gbc_rtToTextField.insets = new Insets(0, 0, 5, 0);
		gbc_rtToTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_rtToTextField.gridx = 3;
		gbc_rtToTextField.gridy = 1;
		panel.add(rtToTextField, gbc_rtToTextField);

		JLabel lblBasePeakMz = new JLabel("Base peak M/Z from ");
		GridBagConstraints gbc_lblBasePeakMz = new GridBagConstraints();
		gbc_lblBasePeakMz.anchor = GridBagConstraints.EAST;
		gbc_lblBasePeakMz.insets = new Insets(0, 0, 5, 5);
		gbc_lblBasePeakMz.gridx = 0;
		gbc_lblBasePeakMz.gridy = 2;
		panel.add(lblBasePeakMz, gbc_lblBasePeakMz);

		bpmzMinTextField = new JFormattedTextField(MRC2ToolBoxConfiguration.getMzFormat());
		GridBagConstraints gbc_bpmzMinTextField = new GridBagConstraints();
		gbc_bpmzMinTextField.insets = new Insets(0, 0, 5, 5);
		gbc_bpmzMinTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_bpmzMinTextField.gridx = 1;
		gbc_bpmzMinTextField.gridy = 2;
		panel.add(bpmzMinTextField, gbc_bpmzMinTextField);

		JLabel lblTo_1 = new JLabel("to");
		GridBagConstraints gbc_lblTo_1 = new GridBagConstraints();
		gbc_lblTo_1.anchor = GridBagConstraints.EAST;
		gbc_lblTo_1.insets = new Insets(0, 0, 5, 5);
		gbc_lblTo_1.gridx = 2;
		gbc_lblTo_1.gridy = 2;
		panel.add(lblTo_1, gbc_lblTo_1);

		bpMzMaxTextField = new JFormattedTextField(MRC2ToolBoxConfiguration.getMzFormat());
		GridBagConstraints gbc_bpMxMaxTextField = new GridBagConstraints();
		gbc_bpMxMaxTextField.insets = new Insets(0, 0, 5, 0);
		gbc_bpMxMaxTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_bpMxMaxTextField.gridx = 3;
		gbc_bpMxMaxTextField.gridy = 2;
		panel.add(bpMzMaxTextField, gbc_bpMxMaxTextField);

		filterButton = new JButton("Filter");
		filterButton.addActionListener(listener);

		multIdCheckBox = new JCheckBox(
				"Show only clusters with multiple identified features");
		GridBagConstraints gbc_multIdCheckBox = new GridBagConstraints();
		gbc_multIdCheckBox.anchor = GridBagConstraints.WEST;
		gbc_multIdCheckBox.gridwidth = 4;
		gbc_multIdCheckBox.insets = new Insets(0, 0, 5, 5);
		gbc_multIdCheckBox.gridx = 0;
		gbc_multIdCheckBox.gridy = 3;
		panel.add(multIdCheckBox, gbc_multIdCheckBox);

		cancelButton = new JButton("Cancel");
		GridBagConstraints gbc_cancelButton = new GridBagConstraints();
		gbc_cancelButton.insets = new Insets(0, 0, 0, 5);
		gbc_cancelButton.gridx = 0;
		gbc_cancelButton.gridy = 5;
		panel.add(cancelButton, gbc_cancelButton);

		resetButton = new JButton("Clear filters");
		resetButton.addActionListener(this);
		resetButton.setActionCommand(RESET_COMMAND);
		GridBagConstraints gbc_resetButton = new GridBagConstraints();
		gbc_resetButton.anchor = GridBagConstraints.EAST;
		gbc_resetButton.insets = new Insets(0, 0, 0, 5);
		gbc_resetButton.gridx = 1;
		gbc_resetButton.gridy = 5;
		panel.add(resetButton, gbc_resetButton);
		filterButton.setActionCommand(MainActionCommands.FILTER_CLUSTERS_COMMAND.getName());
		GridBagConstraints gbc_filterButton = new GridBagConstraints();
		gbc_filterButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_filterButton.gridx = 3;
		gbc_filterButton.gridy = 5;
		panel.add(filterButton, gbc_filterButton);

		JRootPane rootPane = SwingUtilities.getRootPane(filterButton);
		rootPane.setDefaultButton(filterButton);

		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				disposeWithoutSavingPreferences();
			}
		};
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		cancelButton.addActionListener(al);
	}

	private void disposeWithoutSavingPreferences() {
		super.dispose();
	}
	
	@Override
	public void actionPerformed(ActionEvent event) {

		String command = event.getActionCommand();
		if (command.equals(RESET_COMMAND)) {

			nameTextField.setText("");
			rtFromTextField.setText("");
			rtToTextField.setText("");
			bpmzMinTextField.setText("");
			bpMzMaxTextField.setText("");
			multIdCheckBox.setSelected(false);
		}
	}

	public String getFeatureNameSubstring() {

		String name = null;
		if (!nameTextField.getText().trim().isEmpty())
			name = nameTextField.getText().trim();

		return name;
	}

	public Range getMzRange() {

		Range mzRange = null;

		if (!bpmzMinTextField.getText().isEmpty() && !bpMzMaxTextField.getText().isEmpty()) {

			double from = Double.parseDouble(bpmzMinTextField.getText());
			double to = Double.parseDouble(bpMzMaxTextField.getText());
			if (from >= to) {
				MessageDialog.showErrorMsg("M/Z values in wrong order!");
				return null;
			} else {
				mzRange = new Range(from, to);
			}
		}
		return mzRange;
	}

	public Range getRtRange() {

		Range rtRange = null;

		if (!rtFromTextField.getText().isEmpty() && !rtToTextField.getText().isEmpty()) {

			double from = Double.parseDouble(rtFromTextField.getText());
			double to = Double.parseDouble(rtToTextField.getText());
			if (from >= to) {
				MessageDialog.showErrorMsg("RT values in wrong order!");
				return null;
			} else {
				rtRange = new Range(from, to);
			}
		}
		return rtRange;
	}

	public boolean multIdOnly() {

		return multIdCheckBox.isSelected();
	}
}

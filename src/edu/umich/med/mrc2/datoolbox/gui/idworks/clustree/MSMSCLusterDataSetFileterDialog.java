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

package edu.umich.med.mrc2.datoolbox.gui.idworks.clustree;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import edu.umich.med.mrc2.datoolbox.data.MSFeatureIdentificationFollowupStep;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class MSMSCLusterDataSetFileterDialog extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2451814964627936355L;
	private static final Icon addIdStatusIcon = GuiUtils.getIcon("addIdStatus", 32);
	private static final Icon editIdStatusIcon = GuiUtils.getIcon("editIdStatus", 32);

	private JButton saveButton, cancelButton;
	private JPanel panel_1;
	private JLabel lblTitle;
	private JTextField stepNameTextField;
	private MSFeatureIdentificationFollowupStep step;

	public MSMSCLusterDataSetFileterDialog(MSFeatureIdentificationFollowupStep step, ActionListener listener) {
		super((JDialog)listener);
		setSize(new Dimension(600, 150));
		setPreferredSize(new Dimension(600, 150));
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setModal(true);
		this.step = step;

		panel_1 = new JPanel();
		panel_1.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(panel_1, BorderLayout.CENTER);
		GridBagLayout gbl_panel_1 = new GridBagLayout();
		gbl_panel_1.columnWidths = new int[]{0, 0, 0};
		gbl_panel_1.rowHeights = new int[]{0, 0};
		gbl_panel_1.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gbl_panel_1.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		panel_1.setLayout(gbl_panel_1);

		lblTitle = new JLabel("Step name");
		GridBagConstraints gbc_lblTitle = new GridBagConstraints();
		gbc_lblTitle.insets = new Insets(0, 0, 0, 5);
		gbc_lblTitle.anchor = GridBagConstraints.EAST;
		gbc_lblTitle.gridx = 0;
		gbc_lblTitle.gridy = 0;
		panel_1.add(lblTitle, gbc_lblTitle);

		stepNameTextField = new JTextField();
		GridBagConstraints gbc_statusNameTextField = new GridBagConstraints();
		gbc_statusNameTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_statusNameTextField.gridx = 1;
		gbc_statusNameTextField.gridy = 0;
		panel_1.add(stepNameTextField, gbc_statusNameTextField);
		stepNameTextField.setColumns(10);

		JPanel panel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		getContentPane().add(panel, BorderLayout.SOUTH);

		cancelButton = new JButton("Cancel");
		panel.add(cancelButton);

		saveButton = new JButton("Save");
		saveButton.addActionListener(listener);
		panel.add(saveButton);
		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				dispose();
			}
		};
		cancelButton.addActionListener(al);

		JRootPane rootPane = SwingUtilities.getRootPane(saveButton);
		rootPane.registerKeyboardAction(al, stroke,
				JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(saveButton);
		loadFollowupStep();
		pack();
	}
	
	private void loadFollowupStep() {
		
		if(step == null) {
			setTitle("Create new MS feature identification follow-up step");
			setIconImage(((ImageIcon) addIdStatusIcon).getImage());
			saveButton.setActionCommand(
					MainActionCommands.ADD_ID_FOLLOWUP_STEP_COMMAND.getName());
		}
		else {
			setTitle("Edit MS feature identification follow-up step");
			setIconImage(((ImageIcon) editIdStatusIcon).getImage());
			saveButton.setActionCommand(
					MainActionCommands.EDIT_ID_FOLLOWUP_STEP_COMMAND.getName());
			
			stepNameTextField.setText(step.getName());
		}
	}

	public MSFeatureIdentificationFollowupStep getStep() {
		return step;
	}
	
	public String getStepName() {
		return stepNameTextField.getText().trim();
	}
}



















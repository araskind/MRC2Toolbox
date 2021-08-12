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

package edu.umich.med.mrc2.datoolbox.gui.refsamples;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
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

import edu.umich.med.mrc2.datoolbox.data.ExperimentalSample;
import edu.umich.med.mrc2.datoolbox.data.enums.MoTrPACQCSampleType;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class ReferenceSampleEditorDialog extends JDialog {

	/**
	 *
	 */
	private static final long serialVersionUID = 5807682896514071039L;
	private static final Icon addRefSampleIcon = GuiUtils.getIcon("addStandardSample", 32);
	private static final Icon editRefSampleIcon = GuiUtils.getIcon("editStandardSample", 32);

	private ExperimentalSample sample;
	private JTextField sampleIdTextField;
	private JTextField sampleNameTextField;
	private JButton cancelButton;
	private JButton saveButton;
	private JComboBox sampleTypeComboBox;

	public ReferenceSampleEditorDialog(ExperimentalSample sampleToEdit, ActionListener listener) {

		super();
		sample = sampleToEdit;
		setModalityType(ModalityType.APPLICATION_MODAL);
		setSize(new Dimension(300, 170));
		setPreferredSize(new Dimension(300, 170));
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(10, 10, 0, 10));
		getContentPane().add(panel, BorderLayout.CENTER);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{0, 0, 0};
		gbl_panel.rowHeights = new int[]{0, 0, 0, 0};
		gbl_panel.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);

		JLabel lblSampleId = new JLabel("Sample ID");
		GridBagConstraints gbc_lblSampleId = new GridBagConstraints();
		gbc_lblSampleId.anchor = GridBagConstraints.EAST;
		gbc_lblSampleId.insets = new Insets(0, 0, 5, 5);
		gbc_lblSampleId.gridx = 0;
		gbc_lblSampleId.gridy = 0;
		panel.add(lblSampleId, gbc_lblSampleId);

		sampleIdTextField = new JTextField();
		GridBagConstraints gbc_sampleIdTextField = new GridBagConstraints();
		gbc_sampleIdTextField.insets = new Insets(0, 0, 5, 0);
		gbc_sampleIdTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_sampleIdTextField.gridx = 1;
		gbc_sampleIdTextField.gridy = 0;
		panel.add(sampleIdTextField, gbc_sampleIdTextField);
		sampleIdTextField.setColumns(10);

		JLabel lblSampleName = new JLabel("Sample name");
		GridBagConstraints gbc_lblSampleName = new GridBagConstraints();
		gbc_lblSampleName.insets = new Insets(0, 0, 5, 5);
		gbc_lblSampleName.anchor = GridBagConstraints.EAST;
		gbc_lblSampleName.gridx = 0;
		gbc_lblSampleName.gridy = 1;
		panel.add(lblSampleName, gbc_lblSampleName);

		sampleNameTextField = new JTextField();
		GridBagConstraints gbc_sampleNameTextField = new GridBagConstraints();
		gbc_sampleNameTextField.insets = new Insets(0, 0, 5, 0);
		gbc_sampleNameTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_sampleNameTextField.gridx = 1;
		gbc_sampleNameTextField.gridy = 1;
		panel.add(sampleNameTextField, gbc_sampleNameTextField);
		sampleNameTextField.setColumns(10);
		
		JLabel lblSampleType = new JLabel("Sample type");
		GridBagConstraints gbc_lblSampleType = new GridBagConstraints();
		gbc_lblSampleType.anchor = GridBagConstraints.EAST;
		gbc_lblSampleType.insets = new Insets(0, 0, 0, 5);
		gbc_lblSampleType.gridx = 0;
		gbc_lblSampleType.gridy = 2;
		panel.add(lblSampleType, gbc_lblSampleType);
		
		sampleTypeComboBox = new JComboBox(
				new DefaultComboBoxModel<MoTrPACQCSampleType>(MoTrPACQCSampleType.values()));
		sampleTypeComboBox.setSelectedIndex(-1);
		GridBagConstraints gbc_sampleTypeComboBox = new GridBagConstraints();
		gbc_sampleTypeComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_sampleTypeComboBox.gridx = 1;
		gbc_sampleTypeComboBox.gridy = 2;
		panel.add(sampleTypeComboBox, gbc_sampleTypeComboBox);

		JPanel panel_1 = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel_1.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		getContentPane().add(panel_1, BorderLayout.SOUTH);

		cancelButton = new JButton("Cancel");
		panel_1.add(cancelButton);

		saveButton = new JButton("Save changes");
		saveButton.addActionListener(listener);
		panel_1.add(saveButton);

		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				dispose();
			}
		};
		cancelButton.addActionListener(al);

		JRootPane rootPane = SwingUtilities.getRootPane(saveButton);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(saveButton);

		if(sample == null) {
			setTitle("Create new reference sample");
			setIconImage(((ImageIcon)addRefSampleIcon).getImage());
			saveButton.setActionCommand(MainActionCommands.ADD_REFERENCE_SAMPLE_COMMAND.getName());
		}
		else {
			setTitle("Edit reference sample");
			setIconImage(((ImageIcon)editRefSampleIcon).getImage());
			saveButton.setActionCommand(MainActionCommands.EDIT_REFERENCE_SAMPLE_COMMAND.getName());
			sampleIdTextField.setText(sample.getId());
			sampleNameTextField.setText(sample.getName());
			sampleTypeComboBox.setSelectedItem(sample.getMoTrPACQCSampleType());
		}
		pack();
	}

	public String getSampleId() {
		return sampleIdTextField.getText().trim();
	}

	public String getSampleName() {
		return sampleNameTextField.getText().trim();
	}

	/**
	 * @return the sample
	 */
	public ExperimentalSample getSample() {
		return sample;
	}
	
	public MoTrPACQCSampleType getMoTrPACQCSampleType() {
		return (MoTrPACQCSampleType)sampleTypeComboBox.getSelectedItem();
	}	
}






























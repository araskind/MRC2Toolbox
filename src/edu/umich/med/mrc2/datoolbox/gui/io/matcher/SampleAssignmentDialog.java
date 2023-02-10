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

package edu.umich.med.mrc2.datoolbox.gui.io.matcher;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignFactor;
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignLevel;
import edu.umich.med.mrc2.datoolbox.data.ExperimentalSample;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.SortedComboBoxModel;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;

public class SampleAssignmentDialog extends JDialog {

	/**
	 *
	 */
	private static final long serialVersionUID = 3086453967218181125L;
	private static final Icon batchDropdownIcon = GuiUtils.getIcon("dropdown", 32);
	private Collection<ExperimentalSample>samples;
	private JButton btnCancel;
	private JButton assignLevelsButton;
	private Map<ExperimentDesignFactor, JComboBox<ExperimentDesignLevel>>selectorMap;
	private JLabel lblSelectSample;
	private JLabel label;
	private JComboBox sampleComboBox;

	public SampleAssignmentDialog(ActionListener listener){

		super();
		setSize(new Dimension(450, 130));
		setTitle("Assign sample to selected data files");
		this.samples = samples;

		setModalityType(ModalityType.APPLICATION_MODAL);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setIconImage(((ImageIcon) batchDropdownIcon).getImage());

		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(panel, BorderLayout.CENTER);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{0, 0, 0, 0};
		gbl_panel.rowHeights = new int[]{0, 0, 0, 0};
		gbl_panel.columnWeights = new double[]{0.0, 1.0, 1.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);

		lblSelectSample = new JLabel("Select sample");
		GridBagConstraints gbc_lblSelectSample = new GridBagConstraints();
		gbc_lblSelectSample.anchor = GridBagConstraints.EAST;
		gbc_lblSelectSample.insets = new Insets(0, 0, 5, 5);
		gbc_lblSelectSample.gridx = 0;
		gbc_lblSelectSample.gridy = 0;
		panel.add(lblSelectSample, gbc_lblSelectSample);

		Collection<ExperimentalSample>samples = new ArrayList<ExperimentalSample>();
		if(MRC2ToolBoxCore.getActiveMetabolomicsExperiment() != null)
			samples = MRC2ToolBoxCore.getActiveMetabolomicsExperiment().getExperimentDesign().getSamples();

		sampleComboBox = new JComboBox(new SortedComboBoxModel(samples));
		sampleComboBox.setSelectedIndex(-1);
		GridBagConstraints gbc_sampleComboBox = new GridBagConstraints();
		gbc_sampleComboBox.gridwidth = 2;
		gbc_sampleComboBox.insets = new Insets(0, 0, 5, 0);
		gbc_sampleComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_sampleComboBox.gridx = 1;
		gbc_sampleComboBox.gridy = 0;
		panel.add(sampleComboBox, gbc_sampleComboBox);

		label = new JLabel(" ");
		GridBagConstraints gbc_label = new GridBagConstraints();
		gbc_label.gridwidth = 2;
		gbc_label.insets = new Insets(0, 0, 5, 0);
		gbc_label.gridx = 1;
		gbc_label.gridy = 1;
		panel.add(label, gbc_label);

		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				dispose();
			}
		};
		btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(al);
		GridBagConstraints gbc_btnCancel = new GridBagConstraints();
		gbc_btnCancel.anchor = GridBagConstraints.EAST;
		gbc_btnCancel.insets = new Insets(0, 0, 0, 5);
		gbc_btnCancel.gridx = 1;
		gbc_btnCancel.gridy = 2;
		panel.add(btnCancel, gbc_btnCancel);

		assignLevelsButton = new JButton(MainActionCommands.ASSIGN_SAMPLESS_COMMAND.getName());
		assignLevelsButton.setActionCommand(MainActionCommands.ASSIGN_SAMPLESS_COMMAND.getName());
		assignLevelsButton.addActionListener(listener);
		GridBagConstraints gbc_assignBatchButton = new GridBagConstraints();
		gbc_assignBatchButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_assignBatchButton.gridx = 2;
		gbc_assignBatchButton.gridy = 2;
		panel.add(assignLevelsButton, gbc_assignBatchButton);

		JRootPane rootPane = SwingUtilities.getRootPane(assignLevelsButton);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(assignLevelsButton);
		pack();
	}

	public ExperimentalSample getSelectedSample() {
		return (ExperimentalSample)sampleComboBox.getSelectedItem();
	}
}

















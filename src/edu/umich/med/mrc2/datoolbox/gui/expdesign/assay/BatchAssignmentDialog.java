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

package edu.umich.med.mrc2.datoolbox.gui.expdesign.assay;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Collection;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
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

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.enums.ParameterSetStatus;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;

public class BatchAssignmentDialog extends JDialog implements ActionListener{

	/**
	 *
	 */
	private static final long serialVersionUID = 3086453967218181125L;
	private static final Icon batchDropdownIcon = GuiUtils.getIcon("batchDropdown", 32);
	private Collection<DataFile>dataFiles;
	private JButton btnCancel;
	private JButton assignBatchButton;
	private JSpinner batchNumberSpinner;

	public static final String ASSIGN_BATCH_COMMAND = "Assign batch";

	public BatchAssignmentDialog(Collection<DataFile>dataFiles){

		super();
		setTitle("Assign batch # to selected data files");
		this.dataFiles = dataFiles;

		setModalityType(ModalityType.APPLICATION_MODAL);
		setSize(new Dimension(250, 150));
		setPreferredSize(new Dimension(250, 150));
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setIconImage(((ImageIcon) batchDropdownIcon).getImage());

		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(panel, BorderLayout.CENTER);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{0, 0, 0};
		gbl_panel.rowHeights = new int[]{0, 0, 0, 0};
		gbl_panel.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);

		JLabel lblBatch = new JLabel("Batch #");
		GridBagConstraints gbc_lblBatch = new GridBagConstraints();
		gbc_lblBatch.anchor = GridBagConstraints.EAST;
		gbc_lblBatch.insets = new Insets(0, 0, 5, 5);
		gbc_lblBatch.gridx = 0;
		gbc_lblBatch.gridy = 0;
		panel.add(lblBatch, gbc_lblBatch);

		batchNumberSpinner = new JSpinner(
				new SpinnerNumberModel(Integer.valueOf(1), Integer.valueOf(1), null, Integer.valueOf(1)));
		batchNumberSpinner.setPreferredSize(new Dimension(60, 20));
		batchNumberSpinner.setMinimumSize(new Dimension(60, 20));
		GridBagConstraints gbc_spinner = new GridBagConstraints();
		gbc_spinner.insets = new Insets(0, 0, 5, 0);
		gbc_spinner.anchor = GridBagConstraints.WEST;
		gbc_spinner.gridx = 1;
		gbc_spinner.gridy = 0;
		panel.add(batchNumberSpinner, gbc_spinner);

		JLabel label = new JLabel(" ");
		GridBagConstraints gbc_label = new GridBagConstraints();
		gbc_label.insets = new Insets(0, 0, 5, 0);
		gbc_label.gridx = 1;
		gbc_label.gridy = 1;
		panel.add(label, gbc_label);

		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				disposeWithoutSavingPreferences();
			}
		};
		btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(al);

		GridBagConstraints gbc_btnCancel = new GridBagConstraints();
		gbc_btnCancel.insets = new Insets(0, 0, 0, 5);
		gbc_btnCancel.gridx = 0;
		gbc_btnCancel.gridy = 2;
		panel.add(btnCancel, gbc_btnCancel);

		assignBatchButton = new JButton(ASSIGN_BATCH_COMMAND);
		assignBatchButton.setActionCommand(ASSIGN_BATCH_COMMAND);
		assignBatchButton.addActionListener(this);
		GridBagConstraints gbc_assignBatchButton = new GridBagConstraints();
		gbc_assignBatchButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_assignBatchButton.gridx = 1;
		gbc_assignBatchButton.gridy = 2;
		panel.add(assignBatchButton, gbc_assignBatchButton);

		JRootPane rootPane = SwingUtilities.getRootPane(assignBatchButton);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(assignBatchButton);
	}

	private void disposeWithoutSavingPreferences() {
		super.dispose();
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {

		if(e.getActionCommand().equals(ASSIGN_BATCH_COMMAND)) {

			int batchNumber = (int) batchNumberSpinner.getValue();
			dataFiles.stream().forEach(f -> f.setBatchNumber(batchNumber));

			if(MRC2ToolBoxCore.getActiveMetabolomicsExperiment() != null)
				MRC2ToolBoxCore.getActiveMetabolomicsExperiment().getExperimentDesign().fireExperimentDesignEvent(ParameterSetStatus.CHANGED);

			dispose();
		}
	}
}

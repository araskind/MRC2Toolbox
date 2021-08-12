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

package edu.umich.med.mrc2.datoolbox.gui.expdesign.editor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

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
import javax.swing.border.TitledBorder;

import edu.umich.med.mrc2.datoolbox.data.ExperimentDesign;
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignFactor;
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignLevel;
import edu.umich.med.mrc2.datoolbox.data.ExperimentalSample;
import edu.umich.med.mrc2.datoolbox.data.enums.ParameterSetStatus;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.SortedComboBoxModel;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;

public class DesignLevelAssignmentDialog extends JDialog implements ActionListener{

	/**
	 *
	 */
	private static final long serialVersionUID = 3086453967218181125L;
	private static final Icon batchDropdownIcon = GuiUtils.getIcon("dropdown", 32);
	private Collection<ExperimentalSample>samples;
	private JButton btnCancel;
	private JButton assignLevelsButton;
	private Map<ExperimentDesignFactor, JComboBox<ExperimentDesignLevel>>selectorMap;

	public static final String ASSIGN_LEVELS_COMMAND = "Assign levels";

	public DesignLevelAssignmentDialog(Collection<ExperimentalSample>samples){

		super();
		setTitle("Edit design for selected samples");
		this.samples = samples;

		setModalityType(ModalityType.APPLICATION_MODAL);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setIconImage(((ImageIcon) batchDropdownIcon).getImage());

		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(panel, BorderLayout.CENTER);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{0, 0, 0};
		gbl_panel.rowHeights = new int[]{0, 0, 0, 0};
		gbl_panel.columnWeights = new double[]{1.0, 1.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);

		//	Instructions
		JPanel instructionsPanel = new JPanel(new BorderLayout(10,10));
		instructionsPanel.setBorder(new TitledBorder(null, "Instructions", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GridBagConstraints gbc_Instructions = new GridBagConstraints();
		gbc_Instructions.fill = GridBagConstraints.HORIZONTAL;
		gbc_Instructions.gridwidth = 2;
		gbc_Instructions.insets = new Insets(0, 0, 5, 5);
		gbc_Instructions.gridx = 0;
		gbc_Instructions.gridy = 0;
		panel.add(instructionsPanel, gbc_Instructions);

		String instructionText =
				"<html>Choose the desired value for each factor to apply to all selected samples.<br>" +
				"Leave the factors you don't want to edit as is (no selection)" +
				"</html>";
		JLabel lblNewLabel = new JLabel(instructionText);
		instructionsPanel.add(lblNewLabel, BorderLayout.CENTER);

		selectorMap = new TreeMap<ExperimentDesignFactor, JComboBox<ExperimentDesignLevel>>();
		int rowCount = 1;

		if(MRC2ToolBoxCore.getCurrentProject() != null) {

			ExperimentDesign design = MRC2ToolBoxCore.getCurrentProject().getExperimentDesign();

			for(ExperimentDesignFactor factor : design.getFactors()) {

				JLabel lblFactor = new JLabel(factor.getName());
				GridBagConstraints gbc_lblBatch = new GridBagConstraints();
				gbc_lblBatch.anchor = GridBagConstraints.EAST;
				gbc_lblBatch.insets = new Insets(0, 0, 5, 5);
				gbc_lblBatch.gridx = 0;
				gbc_lblBatch.gridy = rowCount;
				panel.add(lblFactor, gbc_lblBatch);

				ExperimentDesignLevel[] modelOptions = factor.getLevels().toArray(new ExperimentDesignLevel[factor.getLevels().size()]);
				JComboBox comboBox = new JComboBox<ExperimentDesignLevel>(new SortedComboBoxModel(modelOptions));
				comboBox.setSelectedIndex(-1);

				GridBagConstraints gbc_fCombo = new GridBagConstraints();
				gbc_fCombo.insets = new Insets(0, 0, 5, 0);
				//	gbc_fCombo.anchor = GridBagConstraints.WEST;
				gbc_fCombo.fill = GridBagConstraints.HORIZONTAL;
				gbc_fCombo.gridx = 1;
				gbc_fCombo.gridy = rowCount;
				panel.add(comboBox, gbc_fCombo);

				selectorMap.put(factor, comboBox);
				rowCount++;
			}
		}
		int height = 50 * rowCount + 30;
		setSize(new Dimension(450, height));
		setPreferredSize(new Dimension(450, height));

		JLabel label = new JLabel(" ");
		GridBagConstraints gbc_label = new GridBagConstraints();
		gbc_label.insets = new Insets(0, 0, 5, 0);
		gbc_label.gridx = 1;
		gbc_label.gridy = rowCount;
		panel.add(label, gbc_label);
		rowCount++;

		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				dispose();
			}
		};
		btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(al);

		GridBagConstraints gbc_btnCancel = new GridBagConstraints();
		gbc_btnCancel.insets = new Insets(0, 0, 0, 5);
		gbc_btnCancel.gridx = 0;
		gbc_btnCancel.gridy = rowCount;
		panel.add(btnCancel, gbc_btnCancel);

		assignLevelsButton = new JButton(ASSIGN_LEVELS_COMMAND);
		assignLevelsButton.setActionCommand(ASSIGN_LEVELS_COMMAND);
		assignLevelsButton.addActionListener(this);
		GridBagConstraints gbc_assignBatchButton = new GridBagConstraints();
		gbc_assignBatchButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_assignBatchButton.gridx = 1;
		gbc_assignBatchButton.gridy = rowCount;
		panel.add(assignLevelsButton, gbc_assignBatchButton);

		JRootPane rootPane = SwingUtilities.getRootPane(assignLevelsButton);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(assignLevelsButton);
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		if(e.getActionCommand().equals(ASSIGN_LEVELS_COMMAND)) {

			for (Entry<ExperimentDesignFactor, JComboBox<ExperimentDesignLevel>> entry : selectorMap.entrySet()) {

				if(entry.getValue().getSelectedIndex() > -1) {

					ExperimentDesignLevel selectedLevel = (ExperimentDesignLevel) entry.getValue().getSelectedItem();
					samples.stream().forEach(s -> s.getDesignCell().put(entry.getKey(), selectedLevel));
				}
			}
			MRC2ToolBoxCore.getCurrentProject().getExperimentDesign().fireExperimentDesignEvent(ParameterSetStatus.CHANGED);
			dispose();
		}
	}
}

















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

package edu.umich.med.mrc2.datoolbox.gui.expsetup.expdesign.stucturedit;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignFactor;
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignLevel;
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignSubset;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class ExpDesignEditorDialog extends JDialog implements ActionListener {

	/**
	 *
	 */
	private static final long serialVersionUID = -4477376609903476894L;

	static final Icon factorIcon = GuiUtils.getIcon("factor", 32);

	private JPanel panel;
	private ExpDesignTree designTree;
	private JScrollPane scrollPane;
	private JPanel panel_1;
	private JPanel panel_2;
	private JLabel lblName;
	private JTextField subsetNameTextField;
	private JPanel panel_3;
	private JButton btnSave;

	private JButton btnCancel;
	private DesignTreeToolbar toolBar;

	public ExpDesignEditorDialog(ActionListener listener) {

		super(MRC2ToolBoxCore.getMainWindow(), "Create new design subset");

		setModalityType(ModalityType.APPLICATION_MODAL);
		setSize(new Dimension(400, 400));
		setPreferredSize(new Dimension(400, 400));
		setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		setIconImage(((ImageIcon) factorIcon).getImage());

		panel = new JPanel();
		getContentPane().add(panel, BorderLayout.CENTER);

		panel.setBorder(null);
		panel.setLayout(new BorderLayout(0, 0));

		toolBar = new DesignTreeToolbar(this);
		panel.add(toolBar, BorderLayout.NORTH);

		designTree = new ExpDesignTree();
		designTree.setBorder(new EmptyBorder(10, 10, 10, 10));
		designTree.setRowHeight(0);

		scrollPane = new JScrollPane(designTree);
		scrollPane.setBorder(null);
		panel.add(scrollPane, BorderLayout.CENTER);

		panel_1 = new JPanel();
		panel_1.setPreferredSize(new Dimension(300, 70));
		panel_1.setSize(new Dimension(300, 70));
		panel_1.setMinimumSize(new Dimension(300, 70));
		panel.add(panel_1, BorderLayout.SOUTH);
		panel_1.setLayout(new BorderLayout(0, 0));

		panel_2 = new JPanel();
		panel_2.setBorder(new EmptyBorder(5, 5, 5, 5));
		panel_1.add(panel_2, BorderLayout.NORTH);
		GridBagLayout gbl_panel_2 = new GridBagLayout();
		gbl_panel_2.columnWidths = new int[]{31, 86, 0};
		gbl_panel_2.rowHeights = new int[]{20, 0};
		gbl_panel_2.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gbl_panel_2.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		panel_2.setLayout(gbl_panel_2);

		lblName = new JLabel("Name:");
		GridBagConstraints gbc_lblName = new GridBagConstraints();
		gbc_lblName.anchor = GridBagConstraints.EAST;
		gbc_lblName.insets = new Insets(0, 0, 0, 5);
		gbc_lblName.gridx = 0;
		gbc_lblName.gridy = 0;
		panel_2.add(lblName, gbc_lblName);

		subsetNameTextField = new JTextField();
		GridBagConstraints gbc_subsetNameTextField = new GridBagConstraints();
		gbc_subsetNameTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_subsetNameTextField.gridx = 1;
		gbc_subsetNameTextField.gridy = 0;
		panel_2.add(subsetNameTextField, gbc_subsetNameTextField);
		subsetNameTextField.setColumns(10);

		panel_3 = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel_3.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		panel_1.add(panel_3, BorderLayout.CENTER);

		btnCancel = new JButton("Cancel");
		panel_3.add(btnCancel);

		btnSave = new JButton("Save");
		btnSave.setActionCommand(MainActionCommands.NEW_DESIGN_SUBSET_COMMAND.getName());
		btnSave.addActionListener(listener);

		panel_3.add(btnSave);

		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				setVisible(false);
			}
		};
		btnCancel.addActionListener(al);
		JRootPane rootPane = SwingUtilities.getRootPane(btnSave);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(btnSave);

		pack();
		setLocationRelativeTo(MRC2ToolBoxCore.getMainWindow());
		setVisible(false);
	}

	public void setEditingEnabled(boolean editingEnabled) {

		btnSave.setEnabled(editingEnabled);
		subsetNameTextField.setEnabled(editingEnabled);
		designTree.setEnabled(editingEnabled);

		if(editingEnabled) {
			btnCancel.setText("Cancel");
			setTitle("Create new design subset");
		}
		else {
			btnCancel.setText("Close");
			setTitle("View design subset");
		}
	}

	public Set<ExperimentDesignLevel>getSelectedLevels(){

		return MRC2ToolBoxCore.getActiveMetabolomicsExperiment().getExperimentDesign().getFactors().
				stream().flatMap(f ->f.getLevels().stream()).filter(l -> l.isEnabled()).collect(Collectors.toSet());
	}

	public String getSubsetName(){

		return subsetNameTextField.getText().trim();
	}

	public void loadCompleteDesign() {

		DataAnalysisProject currentProject = MRC2ToolBoxCore.getActiveMetabolomicsExperiment();
		designTree.resetTree();

		if (currentProject != null && currentProject.getExperimentDesign() != null) {

			for (ExperimentDesignFactor F : currentProject.getExperimentDesign().getFactors()) {

				F.setEnabled(true);

				for(ExperimentDesignLevel level : F.getLevels())
					level.setEnabled(true);

				((ExpDesignTreeModel) designTree.getModel()).addObject(F);
			}
			designTree.expandAllNodes();
		}
		subsetNameTextField.setText("");
	}

	public void loadDesignSubset(ExperimentDesignSubset subset) {

		subsetNameTextField.setText(subset.getName());

		DataAnalysisProject currentProject = MRC2ToolBoxCore.getActiveMetabolomicsExperiment();
		designTree.resetTree();

		if (currentProject != null && currentProject.getExperimentDesign() != null) {

			for (ExperimentDesignFactor F : currentProject.getExperimentDesign().getFactors()) {

				for(ExperimentDesignLevel level : F.getLevels()){

					if(subset.getDesignMap().contains(level))
						level.setEnabled(true);
					else
						level.setEnabled(false);
				}
			}
			for (ExperimentDesignFactor F : currentProject.getExperimentDesign().getFactors()) {

				((ExpDesignTreeModel) designTree.getModel()).addObject(F);
			}
			designTree.expandAllNodes();
		}
	}

	@Override
	public void actionPerformed(ActionEvent event) {

		String command = event.getActionCommand();


		if (command.equals(MainActionCommands.ENABLE_ALL_LEVELS_COMMAND.getName()))
			toggleAllLevels(true);

		if (command.equals(MainActionCommands.DISABLE_ALL_LEVELS_COMMAND.getName()))
			toggleAllLevels(false);
	}

	private void toggleAllLevels(boolean enabled) {

		DataAnalysisProject currentProject = MRC2ToolBoxCore.getActiveMetabolomicsExperiment();
		designTree.resetTree();

		if (currentProject != null && currentProject.getExperimentDesign() != null) {

			for (ExperimentDesignFactor F : currentProject.getExperimentDesign().getFactors()) {

				F.setEnabled(enabled);

				for(ExperimentDesignLevel level : F.getLevels())
					level.setEnabled(enabled);

				((ExpDesignTreeModel) designTree.getModel()).addObject(F);
			}
			designTree.expandAllNodes();
		}
	}
}




























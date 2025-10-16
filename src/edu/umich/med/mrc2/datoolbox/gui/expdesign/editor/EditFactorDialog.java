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

package edu.umich.med.mrc2.datoolbox.gui.expdesign.editor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import edu.umich.med.mrc2.datoolbox.data.ExperimentDesign;
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignFactor;
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignLevel;
import edu.umich.med.mrc2.datoolbox.data.enums.ParameterSetStatus;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.ReferenceSamplesManager;

public class EditFactorDialog extends JDialog implements ActionListener, ItemListener {

	/**
	 *
	 */
	private static final long serialVersionUID = 819695784892235224L;
	private static final Icon editFactorIcon = GuiUtils.getIcon("editFactor", 32);
	private static final Icon addFactorIcon = GuiUtils.getIcon("addFactor", 32);

	private LevelsTable levelsTable;
	private ExpLevelsToolbar toolBar;
	private JButton cancelButton;
	private JTextField factorNameTextField;
	private JComboBox factorComboBox;
	private ExperimentDesignFactor activeFactor;
	private JButton saveNewFactorButton;
	private JLabel lblEditName;
	private ExperimentDesign experimentDesign;

	public EditFactorDialog(ExperimentDesignFactor activeFactor) {

		super();
		setTitle("Edit experimental factors");

		this.activeFactor = activeFactor;
		setModalityType(ModalityType.APPLICATION_MODAL);
		setSize(new Dimension(400, 300));
		setPreferredSize(new Dimension(400, 300));
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setIconImage(((ImageIcon) editFactorIcon).getImage());

		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(panel, BorderLayout.NORTH);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{46, 209, 0, 0};
		gbl_panel.rowHeights = new int[]{0, 0, 0};
		gbl_panel.columnWeights = new double[]{0.0, 1.0, 0.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);

		JLabel lblFactor = new JLabel("Factor ");
		GridBagConstraints gbc_lblFactor = new GridBagConstraints();
		gbc_lblFactor.insets = new Insets(0, 0, 5, 5);
		gbc_lblFactor.anchor = GridBagConstraints.EAST;
		gbc_lblFactor.gridx = 0;
		gbc_lblFactor.gridy = 0;
		panel.add(lblFactor, gbc_lblFactor);

		factorComboBox = new JComboBox<ExperimentDesignFactor>();
		factorComboBox.addItemListener(this);
		factorComboBox.setPreferredSize(new Dimension(28, 25));
		factorComboBox.setMinimumSize(new Dimension(28, 25));
		GridBagConstraints gbc_comboBox = new GridBagConstraints();
		gbc_comboBox.gridwidth = 2;
		gbc_comboBox.insets = new Insets(0, 0, 5, 0);
		gbc_comboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBox.gridx = 1;
		gbc_comboBox.gridy = 0;
		panel.add(factorComboBox, gbc_comboBox);

		lblEditName = new JLabel("Edit name");
		GridBagConstraints gbc_lblEditName = new GridBagConstraints();
		gbc_lblEditName.insets = new Insets(0, 0, 0, 5);
		gbc_lblEditName.anchor = GridBagConstraints.EAST;
		gbc_lblEditName.gridx = 0;
		gbc_lblEditName.gridy = 1;
		panel.add(lblEditName, gbc_lblEditName);

		factorNameTextField = new JTextField();
		factorNameTextField.setPreferredSize(new Dimension(6, 25));
		factorNameTextField.setMinimumSize(new Dimension(6, 25));
		GridBagConstraints gbc_factorNameTextField = new GridBagConstraints();
		gbc_factorNameTextField.gridwidth = 2;
		gbc_factorNameTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_factorNameTextField.gridx = 1;
		gbc_factorNameTextField.gridy = 1;
		panel.add(factorNameTextField, gbc_factorNameTextField);
		factorNameTextField.setColumns(10);

		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(panel_1, BorderLayout.SOUTH);

		GridBagLayout gbl_panel_1 = new GridBagLayout();
		gbl_panel_1.columnWidths = new int[]{241, 89, 0};
		gbl_panel_1.rowHeights = new int[]{23, 0};
		gbl_panel_1.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gbl_panel_1.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		panel_1.setLayout(gbl_panel_1);
		cancelButton = new JButton("Close dialog");
		GridBagConstraints gbc_cancelButton = new GridBagConstraints();
		gbc_cancelButton.insets = new Insets(0, 0, 0, 5);
		gbc_cancelButton.anchor = GridBagConstraints.NORTHEAST;
		gbc_cancelButton.gridx = 0;
		gbc_cancelButton.gridy = 0;
		panel_1.add(cancelButton, gbc_cancelButton);

		saveNewFactorButton = new JButton(MainActionCommands.SAVE_NEW_FACTOR_COMMAND.getName());
		saveNewFactorButton.setActionCommand(MainActionCommands.SAVE_NEW_FACTOR_COMMAND.getName());
		saveNewFactorButton.addActionListener(this);
		GridBagConstraints gbc_saveNewFactorButton = new GridBagConstraints();
		gbc_saveNewFactorButton.anchor = GridBagConstraints.EAST;
		gbc_saveNewFactorButton.gridx = 1;
		gbc_saveNewFactorButton.gridy = 0;
		panel_1.add(saveNewFactorButton, gbc_saveNewFactorButton);

		JPanel panel_2 = new JPanel();
		getContentPane().add(panel_2, BorderLayout.CENTER);
		panel_2.setLayout(new BorderLayout(0, 0));

		toolBar = new ExpLevelsToolbar(this);
		toolBar.setOrientation(SwingConstants.VERTICAL);
		panel_2.add(toolBar, BorderLayout.EAST);

		levelsTable = new LevelsTable();
		JScrollPane scrollPane = new JScrollPane(levelsTable);
		panel_2.add(scrollPane, BorderLayout.CENTER);

		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				dispose();
			}
		};
		cancelButton.addActionListener(al);
		JRootPane rootPane = SwingUtilities.getRootPane(saveNewFactorButton);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(saveNewFactorButton);

		loadCurrentDesign();
		pack();
	}

	@SuppressWarnings("unchecked")
	private void loadCurrentDesign() {

		if(MRC2ToolBoxCore.getActiveMetabolomicsExperiment() == null)
			return;

		experimentDesign = MRC2ToolBoxCore.getActiveMetabolomicsExperiment().getExperimentDesign();
		if(activeFactor == null) {

			setIconImage(((ImageIcon) addFactorIcon).getImage());
			setTitle("Add new experimental factor");

			activeFactor = new ExperimentDesignFactor("New factor");
			factorNameTextField.setText(activeFactor.getName());

			factorComboBox.setModel(new DefaultComboBoxModel<ExperimentDesignFactor>(new ExperimentDesignFactor[] {activeFactor}));
			factorComboBox.setSelectedItem(activeFactor);
			levelsTable.setTableModelFromFactor(activeFactor);
			saveNewFactorButton.setText(MainActionCommands.SAVE_NEW_FACTOR_COMMAND.getName());
			saveNewFactorButton.setActionCommand(MainActionCommands.SAVE_NEW_FACTOR_COMMAND.getName());
		}
		else {
			setIconImage(((ImageIcon) editFactorIcon).getImage());
			setTitle("Edit experimental factors");

			//	TODO exclude "Sample type" factor
			ExperimentDesignFactor[] factors = MRC2ToolBoxCore.getActiveMetabolomicsExperiment().getExperimentDesign().
				getFactors().stream().filter(f -> !f.equals(ReferenceSamplesManager.getSampleControlTypeFactor())).
				sorted().toArray(size -> new ExperimentDesignFactor[size]);

			factorComboBox.setModel(new DefaultComboBoxModel<ExperimentDesignFactor>(factors));
			factorComboBox.setSelectedItem(activeFactor);
			factorNameTextField.setText(activeFactor.getName());
			levelsTable.setTableModelFromFactor(activeFactor);
			saveNewFactorButton.setText(MainActionCommands.SAVE_EDITED_FACTOR_COMMAND.getName());
			saveNewFactorButton.setActionCommand(MainActionCommands.SAVE_EDITED_FACTOR_COMMAND.getName());
		}
	}

	@Override
	public void actionPerformed(ActionEvent event) {

		String command = event.getActionCommand();

		if (command.equals(MainActionCommands.ADD_LEVEL_COMMAND.getName()))
			addLevel();

		if (command.equals(MainActionCommands.DELETE_LEVEL_COMMAND.getName()))
			removeLevel();

		if(command.equals(MainActionCommands.RENAME_FACTOR_COMMAND.getName()))
			renameFactor();

		if(command.equals(MainActionCommands.SAVE_NEW_FACTOR_COMMAND.getName()) ||
				command.equals(MainActionCommands.SAVE_EDITED_FACTOR_COMMAND.getName()))
			saveFactor();
	}

	private void addLevel() {

		if(activeFactor == null)
			return;

		ExperimentDesignLevel newLevel = new ExperimentDesignLevel("New level");
		if(activeFactor.getLevelByName(newLevel.getName()) != null || levelsTable.getLevelByName(newLevel.getName()) != null) {
			MessageDialog.showErrorMsg("Level \"New level\" already exists!", this);
			return;
		}
		levelsTable.addLevel(newLevel);


//		int row = levelsTable.getRowCount()-1;
//		if(row == -1)
//			row = 0;
//
//		levelsTable.editCellAt(levelsTable.getRowCount()-1, levelsTable.getColumnIndex(LevelsTableModel.LEVEL_COLUMN));
	}

	private void saveFactor() {

		//	Check naming
		String newName = factorNameTextField.getText().trim();
		experimentDesign.setSuppressEvents(true);

		if(newName.isEmpty()) {

			MessageDialog.showErrorMsg("Factor name can not be empty!", this);
			return;
		}
		for(ExperimentDesignFactor factor : experimentDesign.getFactors()) {

			if(!factor.equals(activeFactor) && factor.getName().equals(newName)) {

				MessageDialog.showErrorMsg("Factor \"" + factor.getName() + "\" already exists!", this);
				return;
			}
		}
		//	Check levels
		if(levelsTable.getRowCount() < 2) {

			MessageDialog.showErrorMsg("Factor should contain at least two levels!", this);
			return;
		}
		//	Check for empty and repeated names
		int newLevelCount = levelsTable.getLevels().size();
		int validLevelCount = (int) levelsTable.getLevels().stream().
				filter(l -> !l.getName().trim().isEmpty()).map(l -> l.getName()).distinct().count();

		if(validLevelCount < newLevelCount) {

			MessageDialog.showErrorMsg("Level names can not be empty and can not repeat!", this);
			return;
		}
		//	Set new name
		activeFactor.setName(newName);

		//	Update existing factor
		if(experimentDesign.getFactors().contains(activeFactor)) {

			Set<ExperimentDesignLevel> levelsToRemove = activeFactor.getLevels().stream()
					.filter(f -> !levelsTable.getLevels().contains(f)).collect(Collectors.toSet());

			if(!levelsToRemove.isEmpty())
				levelsToRemove.forEach(l -> experimentDesign.removeLevel(l, false));

			Set<ExperimentDesignLevel> levelsToAdd = levelsTable.getLevels().stream()
					.filter(l -> !activeFactor.getLevels().contains(l)).collect(Collectors.toSet());

			if(!levelsToAdd.isEmpty())
				levelsToRemove.forEach(l -> experimentDesign.addLevel(l, activeFactor, false));
		}
		else {
			//	Add new factor
			levelsTable.getLevels().stream().forEach(l -> activeFactor.addLevel(l));
			experimentDesign.addFactor(activeFactor,false);
		}
		experimentDesign.setSuppressEvents(false);
		experimentDesign.fireExperimentDesignEvent(ParameterSetStatus.CHANGED);
		dispose();
	}

	private void renameFactor() {

		String newName = factorNameTextField.getText().trim();

		if(newName.isEmpty()) {

			MessageDialog.showErrorMsg("Factor name can not be empty!", this);
			return;
		}
		for(ExperimentDesignFactor factor : MRC2ToolBoxCore.getActiveMetabolomicsExperiment().getExperimentDesign().getFactors()) {

			if(!factor.equals(activeFactor) && factor.getName().equals(newName)) {

				MessageDialog.showErrorMsg("Factor \"" + factor.getName() + "\" already exists!", this);
				return;
			}
		}
		activeFactor.setName(newName);
		loadCurrentDesign();
		factorComboBox.setSelectedItem(activeFactor);
		experimentDesign.fireExperimentDesignEvent(ParameterSetStatus.CHANGED);
	}

	private void removeLevel() {

		if(levelsTable.getSelectedRow() > -1) {

			if(levelsTable.getRowCount() < 3) {

				MessageDialog.showErrorMsg("Factor should contain at least two levels!", this);
				return;
			}
			ExperimentDesignLevel levelToDelete =
					(ExperimentDesignLevel) levelsTable.getValueAt(
							levelsTable.getSelectedRow(),
							levelsTable.getColumnIndex(LevelsTableModel.LEVEL_COLUMN));

			int approve = MessageDialog.showChoiceWithWarningMsg(
					"Delete level \"" + levelToDelete.getName() +"\" from factor \"" + activeFactor.getName() +"\"?",
					this);

			if (approve == JOptionPane.YES_OPTION) {

				//experimentDesign.removeLevel(levelToDelete);
				((LevelsTableModel)levelsTable.getModel()).
					removeRow(levelsTable.convertRowIndexToModel(levelsTable.getSelectedRow()));
			}
		}
	}

	@Override
	public void itemStateChanged(ItemEvent event) {

		if (event.getItem() instanceof ExperimentDesignFactor && event.getStateChange() == ItemEvent.SELECTED) {

			activeFactor = (ExperimentDesignFactor) factorComboBox.getSelectedItem();
			factorNameTextField.setText(activeFactor.getName());
			levelsTable.setTableModelFromFactor(activeFactor);
		}
	}
}



















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

package edu.umich.med.mrc2.datoolbox.gui.idworks.idlevel;

import java.awt.BorderLayout;
import java.awt.Color;
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
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import edu.umich.med.mrc2.datoolbox.data.MSFeatureIdentificationLevel;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.ColorPickerDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class IdLevelEditorDialog extends JDialog implements ActionListener{

	/**
	 * 
	 */
	private static final long serialVersionUID = 2451814964627936355L;
	private static final Icon addIdStatusIcon = GuiUtils.getIcon("addIdStatus", 32);
	private static final Icon editIdStatusIcon = GuiUtils.getIcon("editIdStatus", 32);
	private static final Icon colorPickerIcon = GuiUtils.getIcon("colorPicker", 24);

	private JButton saveButton, cancelButton;
	private JPanel panel_1;
	private JLabel lblTitle;
	private JTextField levelNameTextField;

	private MSFeatureIdentificationLevel level;
	private JSpinner rankSpinner;
	private JLabel lblRank;
	private JLabel lblColorCode;
	private JLabel colorSampleLabel;
	private JButton btnChooseColor;
	private ColorPickerDialog colorPicker;
	private Color levelColor;

	public IdLevelEditorDialog(MSFeatureIdentificationLevel level, ActionListener listener) {
		super((JDialog)listener);
		setSize(new Dimension(600, 150));
		setPreferredSize(new Dimension(600, 150));
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setModalityType(ModalityType.APPLICATION_MODAL);
		this.level = level;

		panel_1 = new JPanel();
		panel_1.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(panel_1, BorderLayout.CENTER);
		GridBagLayout gbl_panel_1 = new GridBagLayout();
		gbl_panel_1.columnWidths = new int[]{0, 0, 0, 0, 0, 0};
		gbl_panel_1.rowHeights = new int[]{0, 0, 0};
		gbl_panel_1.columnWeights = new double[]{0.0, 0.0, 1.0, 1.0, 1.0, Double.MIN_VALUE};
		gbl_panel_1.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		panel_1.setLayout(gbl_panel_1);

		lblTitle = new JLabel("Level ");
		GridBagConstraints gbc_lblTitle = new GridBagConstraints();
		gbc_lblTitle.insets = new Insets(0, 0, 5, 5);
		gbc_lblTitle.anchor = GridBagConstraints.EAST;
		gbc_lblTitle.gridx = 0;
		gbc_lblTitle.gridy = 0;
		panel_1.add(lblTitle, gbc_lblTitle);

		levelNameTextField = new JTextField();
		GridBagConstraints gbc_statusNameTextField = new GridBagConstraints();
		gbc_statusNameTextField.gridwidth = 4;
		gbc_statusNameTextField.insets = new Insets(0, 0, 5, 0);
		gbc_statusNameTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_statusNameTextField.gridx = 1;
		gbc_statusNameTextField.gridy = 0;
		panel_1.add(levelNameTextField, gbc_statusNameTextField);
		levelNameTextField.setColumns(10);
		
		lblRank = new JLabel("Rank");
		GridBagConstraints gbc_lblRank = new GridBagConstraints();
		gbc_lblRank.anchor = GridBagConstraints.EAST;
		gbc_lblRank.insets = new Insets(0, 0, 0, 5);
		gbc_lblRank.gridx = 0;
		gbc_lblRank.gridy = 1;
		panel_1.add(lblRank, gbc_lblRank);
		
		rankSpinner = new JSpinner();
		rankSpinner.setSize(new Dimension(60, 20));
		rankSpinner.setPreferredSize(new Dimension(60, 20));
		rankSpinner.setMinimumSize(new Dimension(60, 20));
		GridBagConstraints gbc_spinner = new GridBagConstraints();
		gbc_spinner.insets = new Insets(0, 0, 0, 5);
		gbc_spinner.anchor = GridBagConstraints.WEST;
		gbc_spinner.gridx = 1;
		gbc_spinner.gridy = 1;
		panel_1.add(rankSpinner, gbc_spinner);
		
		lblColorCode = new JLabel("Color code");
		GridBagConstraints gbc_lblColorCode = new GridBagConstraints();
		gbc_lblColorCode.anchor = GridBagConstraints.EAST;
		gbc_lblColorCode.insets = new Insets(0, 0, 0, 5);
		gbc_lblColorCode.gridx = 2;
		gbc_lblColorCode.gridy = 1;
		panel_1.add(lblColorCode, gbc_lblColorCode);
		
		colorSampleLabel = new JLabel("");
		colorSampleLabel.setOpaque(true);
		GridBagConstraints gbc_colorSampleLabel = new GridBagConstraints();
		gbc_colorSampleLabel.fill = GridBagConstraints.BOTH;
		gbc_colorSampleLabel.insets = new Insets(0, 0, 0, 5);
		gbc_colorSampleLabel.gridx = 3;
		gbc_colorSampleLabel.gridy = 1;
		panel_1.add(colorSampleLabel, gbc_colorSampleLabel);
		
		btnChooseColor = new JButton("");
		btnChooseColor.setIcon(colorPickerIcon);
		btnChooseColor.setActionCommand(MainActionCommands.SHOW_COLOR_PICKER_COMMAND.getName());
		btnChooseColor.addActionListener(this);
		GridBagConstraints gbc_btnChooseColor = new GridBagConstraints();
		gbc_btnChooseColor.anchor = GridBagConstraints.WEST;
		gbc_btnChooseColor.gridx = 4;
		gbc_btnChooseColor.gridy = 1;
		panel_1.add(btnChooseColor, gbc_btnChooseColor);

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
		loadLevel();
		pack();
	}
	
	private void loadLevel() {
		
		if(level == null) {
			setTitle("Create new MS feature identification level");
			setIconImage(((ImageIcon) addIdStatusIcon).getImage());
			saveButton.setActionCommand(
					MainActionCommands.ADD_ID_LEVEL_COMMAND.getName());
		}
		else {
			setTitle("Edit MS feature identification level");
			setIconImage(((ImageIcon) editIdStatusIcon).getImage());
			saveButton.setActionCommand(
					MainActionCommands.EDIT_ID_LEVEL_COMMAND.getName());
			
			levelNameTextField.setText(level.getName());
			rankSpinner.setValue(level.getRank());
			levelColor = level.getColorCode();			
			if(levelColor != null)
				colorSampleLabel.setBackground(levelColor);
		}
	}

	public MSFeatureIdentificationLevel getLevel() {
		return level;
	}
	
	public String getLevelName() {
		return levelNameTextField.getText().trim();
	}
	
	public int getLevelRank() {
		return (int)rankSpinner.getValue();
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		if(e.getActionCommand().equals(MainActionCommands.SHOW_COLOR_PICKER_COMMAND.getName())) {
			colorPicker =  new ColorPickerDialog(this);
			if(level != null)
				colorPicker.setSelectedColor(level.getColorCode());
			
			colorPicker.setLocationRelativeTo(this);
			colorPicker.setVisible(true);
		}
		if (e.getActionCommand().equals(MainActionCommands.SELECT_COLOR_COMMAND.getName())) {

			levelColor = colorPicker.getNewColor();
			colorSampleLabel.setBackground(levelColor);
			colorPicker.dispose();
		}
	}

	/**
	 * @return the levelColor
	 */
	public Color getLevelColor() {
		return levelColor;
	}
}



















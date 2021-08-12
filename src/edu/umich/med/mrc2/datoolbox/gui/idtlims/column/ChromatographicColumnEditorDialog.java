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

package edu.umich.med.mrc2.datoolbox.gui.idtlims.column;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

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

import edu.umich.med.mrc2.datoolbox.data.lims.ChromatographicSeparationType;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSChromatographicColumn;
import edu.umich.med.mrc2.datoolbox.data.lims.Manufacturer;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCash;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.SortedComboBoxModel;

public class ChromatographicColumnEditorDialog extends JDialog {

	/**
	 *
	 */
	private static final long serialVersionUID = -8958656243635154039L;

	private static final Icon addColumnIcon = GuiUtils.getIcon("addColumn", 32);
	private static final Icon editColumnIcon = GuiUtils.getIcon("editColumn", 32);

	private LIMSChromatographicColumn column;
	private JTextField nameTextField;
	private JTextField catalogNumberTextField;
	private JTextField columnChemistryTextField;
	private JButton btnSave;
	private JComboBox manufacturerComboBox;
	private JComboBox separationTypeComboBox;
	private JLabel idValueLabel;

	public ChromatographicColumnEditorDialog(LIMSChromatographicColumn column, ActionListener actionListener) {
		super();
		this.column = column;

		setPreferredSize(new Dimension(600, 250));
		setSize(new Dimension(600, 250));
		setModalityType(ModalityType.APPLICATION_MODAL);
		setResizable(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		JPanel dataPanel = new JPanel();
		dataPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(dataPanel, BorderLayout.CENTER);
		GridBagLayout gbl_dataPanel = new GridBagLayout();
		gbl_dataPanel.columnWidths = new int[]{0, 0, 0};
		gbl_dataPanel.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0};
		gbl_dataPanel.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gbl_dataPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		dataPanel.setLayout(gbl_dataPanel);

		JLabel lblId = new JLabel("ID");
		lblId.setForeground(Color.BLUE);
		lblId.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_lblId = new GridBagConstraints();
		gbc_lblId.anchor = GridBagConstraints.EAST;
		gbc_lblId.insets = new Insets(0, 0, 5, 5);
		gbc_lblId.gridx = 0;
		gbc_lblId.gridy = 0;
		dataPanel.add(lblId, gbc_lblId);

		idValueLabel = new JLabel("");
		idValueLabel.setForeground(Color.BLACK);
		idValueLabel.setFont(new Font("Tahoma", Font.BOLD, 12));
		GridBagConstraints gbc_idValueLabel = new GridBagConstraints();
		gbc_idValueLabel.insets = new Insets(0, 0, 5, 0);
		gbc_idValueLabel.anchor = GridBagConstraints.SOUTHWEST;
		gbc_idValueLabel.gridx = 1;
		gbc_idValueLabel.gridy = 0;
		dataPanel.add(idValueLabel, gbc_idValueLabel);

		JLabel lblName = new JLabel("Name");
		lblName.setForeground(Color.BLACK);
		lblName.setFont(new Font("Tahoma", Font.PLAIN, 11));
		GridBagConstraints gbc_lblName = new GridBagConstraints();
		gbc_lblName.anchor = GridBagConstraints.EAST;
		gbc_lblName.insets = new Insets(0, 0, 5, 5);
		gbc_lblName.gridx = 0;
		gbc_lblName.gridy = 1;
		dataPanel.add(lblName, gbc_lblName);

		nameTextField = new JTextField();
		GridBagConstraints gbc_nameTextField = new GridBagConstraints();
		gbc_nameTextField.insets = new Insets(0, 0, 5, 0);
		gbc_nameTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_nameTextField.gridx = 1;
		gbc_nameTextField.gridy = 1;
		dataPanel.add(nameTextField, gbc_nameTextField);
		nameTextField.setColumns(10);

		JLabel lblType = new JLabel("Type");
		GridBagConstraints gbc_lblType = new GridBagConstraints();
		gbc_lblType.anchor = GridBagConstraints.EAST;
		gbc_lblType.insets = new Insets(0, 0, 5, 5);
		gbc_lblType.gridx = 0;
		gbc_lblType.gridy = 2;
		dataPanel.add(lblType, gbc_lblType);

		separationTypeComboBox = new JComboBox(new SortedComboBoxModel<ChromatographicSeparationType>(
				IDTDataCash.getChromatographicSeparationTypes()));

		GridBagConstraints gbc_separationTypeComboBox = new GridBagConstraints();
		gbc_separationTypeComboBox.insets = new Insets(0, 0, 5, 0);
		gbc_separationTypeComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_separationTypeComboBox.gridx = 1;
		gbc_separationTypeComboBox.gridy = 2;
		dataPanel.add(separationTypeComboBox, gbc_separationTypeComboBox);

		JLabel lblChemistry = new JLabel("Chemistry");
		GridBagConstraints gbc_lblChemistry = new GridBagConstraints();
		gbc_lblChemistry.anchor = GridBagConstraints.EAST;
		gbc_lblChemistry.insets = new Insets(0, 0, 5, 5);
		gbc_lblChemistry.gridx = 0;
		gbc_lblChemistry.gridy = 3;
		dataPanel.add(lblChemistry, gbc_lblChemistry);

		columnChemistryTextField = new JTextField();
		GridBagConstraints gbc_columnChemistryTextField = new GridBagConstraints();
		gbc_columnChemistryTextField.insets = new Insets(0, 0, 5, 0);
		gbc_columnChemistryTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_columnChemistryTextField.gridx = 1;
		gbc_columnChemistryTextField.gridy = 3;
		dataPanel.add(columnChemistryTextField, gbc_columnChemistryTextField);
		columnChemistryTextField.setColumns(10);

		JLabel lblManufacturer = new JLabel("Manufacturer");
		GridBagConstraints gbc_lblManufacturer = new GridBagConstraints();
		gbc_lblManufacturer.anchor = GridBagConstraints.EAST;
		gbc_lblManufacturer.insets = new Insets(0, 0, 5, 5);
		gbc_lblManufacturer.gridx = 0;
		gbc_lblManufacturer.gridy = 4;
		dataPanel.add(lblManufacturer, gbc_lblManufacturer);

		manufacturerComboBox = new JComboBox(new SortedComboBoxModel<Manufacturer>(
				IDTDataCash.getManufacturers()));
		GridBagConstraints gbc_manufacturerComboBox = new GridBagConstraints();
		gbc_manufacturerComboBox.insets = new Insets(0, 0, 5, 0);
		gbc_manufacturerComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_manufacturerComboBox.gridx = 1;
		gbc_manufacturerComboBox.gridy = 4;
		dataPanel.add(manufacturerComboBox, gbc_manufacturerComboBox);

		JLabel lblCatalog = new JLabel("Catalog #");
		GridBagConstraints gbc_lblCatalog = new GridBagConstraints();
		gbc_lblCatalog.anchor = GridBagConstraints.EAST;
		gbc_lblCatalog.insets = new Insets(0, 0, 0, 5);
		gbc_lblCatalog.gridx = 0;
		gbc_lblCatalog.gridy = 5;
		dataPanel.add(lblCatalog, gbc_lblCatalog);

		catalogNumberTextField = new JTextField();
		GridBagConstraints gbc_catalogNumberTextField = new GridBagConstraints();
		gbc_catalogNumberTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_catalogNumberTextField.gridx = 1;
		gbc_catalogNumberTextField.gridy = 5;
		dataPanel.add(catalogNumberTextField, gbc_catalogNumberTextField);
		catalogNumberTextField.setColumns(10);

		JPanel panel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		getContentPane().add(panel, BorderLayout.SOUTH);

		JButton btnCancel = new JButton("Cancel");
		panel.add(btnCancel);
		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				dispose();
			}
		};
		btnCancel.addActionListener(al);

		btnSave = new JButton("Save");
		btnSave.addActionListener(actionListener);
		panel.add(btnSave);
		JRootPane rootPane = SwingUtilities.getRootPane(btnSave);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(btnSave);

		loadColumnData();
	}

	private void loadColumnData() {

		if(column == null) {

			setTitle("Add new chromatographic column");
			setIconImage(((ImageIcon) addColumnIcon).getImage());
			btnSave.setActionCommand(MainActionCommands.ADD_CHROMATOGRAPHIC_COLUMN_COMMAND.getName());
			separationTypeComboBox.setSelectedIndex(-1);
			manufacturerComboBox.setSelectedIndex(-1);
		}
		else {
			setTitle("Edit information for " + column.getColumnName());
			setIconImage(((ImageIcon) editColumnIcon).getImage());
			btnSave.setActionCommand(MainActionCommands.EDIT_CHROMATOGRAPHIC_COLUMN_COMMAND.getName());
			idValueLabel.setText(column.getColumnId());
			nameTextField.setText(column.getColumnName());
			separationTypeComboBox.setSelectedItem(column.getSeparationType());
			columnChemistryTextField.setText(column.getChemistry());
			manufacturerComboBox.setSelectedItem(column.getManufacturer());
			catalogNumberTextField.setText(column.getCatalogNumber());
		}
		pack();
	}

	public LIMSChromatographicColumn getColumn() {
		return column;
	}

	public String getColumnName() {
		return nameTextField.getText().trim();
	}

	public ChromatographicSeparationType getChromatographicSeparationType() {
		return (ChromatographicSeparationType )separationTypeComboBox.getSelectedItem();
	}

	public String getColumnChemistry() {
		return columnChemistryTextField.getText().trim();
	}

	public Manufacturer getManufacturer() {
		return (Manufacturer)manufacturerComboBox.getSelectedItem();
	}

	public String getCatalogNumber() {
		return catalogNumberTextField.getText().trim();
	}
}


































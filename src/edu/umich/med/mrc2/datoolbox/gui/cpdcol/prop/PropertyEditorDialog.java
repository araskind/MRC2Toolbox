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

package edu.umich.med.mrc2.datoolbox.gui.cpdcol.prop;

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
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;

import edu.umich.med.mrc2.datoolbox.data.cpdcoll.CpdMetadataField;
import edu.umich.med.mrc2.datoolbox.data.cpdcoll.CpdMetadataFieldCategory;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.SortedComboBoxModel;

public class PropertyEditorDialog extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1591752976832073167L;
	private static final Icon addProperty = GuiUtils.getIcon("addCollection", 32);
	private static final Icon editProperty = GuiUtils.getIcon("edit", 32);

	private CpdMetadataField field; 
	private String propertyValue;
	private JTextArea propertyValueTextArea;
	private JButton btnSave;
	private JComboBox fieldComboBox;
	private JComboBox categoryComboBox;

	public PropertyEditorDialog(
			CpdMetadataField field, 
			String propertyValue, 
			ActionListener actionListener) {
		super();
		this.field = field;
		this.propertyValue = propertyValue;

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
		gbl_dataPanel.rowHeights = new int[]{0, 0, 0, 0};
		gbl_dataPanel.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gbl_dataPanel.rowWeights = new double[]{0.0, 0.0, 1.0, Double.MIN_VALUE};
		dataPanel.setLayout(gbl_dataPanel);

		JLabel lblType = new JLabel("Category");
		GridBagConstraints gbc_lblType = new GridBagConstraints();
		gbc_lblType.anchor = GridBagConstraints.EAST;
		gbc_lblType.insets = new Insets(0, 0, 5, 5);
		gbc_lblType.gridx = 0;
		gbc_lblType.gridy = 0;
		dataPanel.add(lblType, gbc_lblType);

		categoryComboBox = 
				new JComboBox<CpdMetadataFieldCategory>();
		GridBagConstraints gbc_separationTypeComboBox = new GridBagConstraints();
		gbc_separationTypeComboBox.insets = new Insets(0, 0, 5, 0);
		gbc_separationTypeComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_separationTypeComboBox.gridx = 1;
		gbc_separationTypeComboBox.gridy = 0;
		dataPanel.add(categoryComboBox, gbc_separationTypeComboBox);

		JLabel lblManufacturer = new JLabel("Property");
		GridBagConstraints gbc_lblManufacturer = new GridBagConstraints();
		gbc_lblManufacturer.anchor = GridBagConstraints.EAST;
		gbc_lblManufacturer.insets = new Insets(0, 0, 5, 5);
		gbc_lblManufacturer.gridx = 0;
		gbc_lblManufacturer.gridy = 1;
		dataPanel.add(lblManufacturer, gbc_lblManufacturer);

		fieldComboBox = new JComboBox<CpdMetadataField>();
		GridBagConstraints gbc_manufacturerComboBox = new GridBagConstraints();
		gbc_manufacturerComboBox.insets = new Insets(0, 0, 5, 0);
		gbc_manufacturerComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_manufacturerComboBox.gridx = 1;
		gbc_manufacturerComboBox.gridy = 1;
		dataPanel.add(fieldComboBox, gbc_manufacturerComboBox);

		JLabel lblCatalog = new JLabel("Value");
		GridBagConstraints gbc_lblCatalog = new GridBagConstraints();
		gbc_lblCatalog.anchor = GridBagConstraints.NORTHEAST;
		gbc_lblCatalog.insets = new Insets(0, 0, 0, 5);
		gbc_lblCatalog.gridx = 0;
		gbc_lblCatalog.gridy = 2;
		dataPanel.add(lblCatalog, gbc_lblCatalog);

		propertyValueTextArea = new JTextArea();
		propertyValueTextArea.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		GridBagConstraints gbc_catalogNumberTextField = new GridBagConstraints();
		gbc_catalogNumberTextField.fill = GridBagConstraints.BOTH;
		gbc_catalogNumberTextField.gridx = 1;
		gbc_catalogNumberTextField.gridy = 2;
		dataPanel.add(propertyValueTextArea, gbc_catalogNumberTextField);
		propertyValueTextArea.setColumns(10);

		JPanel panel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		getContentPane().add(panel, BorderLayout.SOUTH);

		JButton btnCancel = new JButton("Cancel");
		panel.add(btnCancel);
		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				disposeWithoutSavingPreferences();
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
	
	private void disposeWithoutSavingPreferences() {
		super.dispose();
	}

	@SuppressWarnings("unchecked")
	private void loadColumnData() {

		if(field == null) {

			setTitle("Add new metadata entry");
			setIconImage(((ImageIcon) addProperty).getImage());
			btnSave.setActionCommand(MainActionCommands.ADD_CHROMATOGRAPHIC_COLUMN_COMMAND.getName());
			
			//	Populate selectors
			categoryComboBox.setSelectedIndex(-1);
			fieldComboBox.setSelectedIndex(-1);
		}
		else {
			setTitle("Edit value for " + field.getName());
			setIconImage(((ImageIcon) editProperty).getImage());
			btnSave.setActionCommand(MainActionCommands.SAVE_CHANGES_COMMAND.getName());
					
			SortedComboBoxModel<CpdMetadataFieldCategory> categoryModel = 
					new SortedComboBoxModel<CpdMetadataFieldCategory>(
							new CpdMetadataFieldCategory[] {field.getCategory()});
			categoryComboBox.setModel(categoryModel);
			categoryComboBox.setSelectedItem(field.getCategory());
			categoryComboBox.setEnabled(false);
			
			SortedComboBoxModel<CpdMetadataField> fieldModel = 
					new SortedComboBoxModel<CpdMetadataField>(new CpdMetadataField[] {field});
			fieldComboBox.setModel(fieldModel);
			fieldComboBox.setSelectedItem(field);
			fieldComboBox.setEnabled(false);
			
			propertyValueTextArea.setText(propertyValue);
		}
		pack();
	}

	public CpdMetadataField getField() {
		return (CpdMetadataField)fieldComboBox.getSelectedItem();
	}

	public CpdMetadataFieldCategory getCpdMetadataFieldCategory() {
		return (CpdMetadataFieldCategory )categoryComboBox.getSelectedItem();
	}
	
	public String getPropertyValue() {
		return propertyValueTextArea.getText().trim();
	}
}


































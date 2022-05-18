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

package edu.umich.med.mrc2.datoolbox.gui.idtlims.vendor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import org.apache.commons.validator.routines.UrlValidator;

import edu.umich.med.mrc2.datoolbox.data.lims.Manufacturer;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCash;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class VendorEditorDialog extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4134991839687534786L;

	private static final Icon addVendorIcon = GuiUtils.getIcon("addVendor", 32);
	private static final Icon editVendorIcon = GuiUtils.getIcon("editVendor", 32);

	private JLabel idValueLabel;
	private JTextField vendorNameTextField;
	private JTextArea webAddressTextArea;
	private Manufacturer manufacturer;
	
	public VendorEditorDialog(
			ActionListener listener, 
			Manufacturer manufacturer) {
		super();
		// TODO Auto-generated constructor stub
		setModalityType(ModalityType.APPLICATION_MODAL);
		setResizable(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		this.manufacturer = manufacturer;
		
		JPanel dataPanel = new JPanel();
		dataPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(dataPanel, BorderLayout.CENTER);
		GridBagLayout gbl_dataPanel = new GridBagLayout();
		gbl_dataPanel.columnWidths = new int[]{0, 114, 126, 78, 0, 0};
		gbl_dataPanel.rowHeights = new int[]{0, 0, 0, 0};
		gbl_dataPanel.columnWeights = new double[]{0.0, 1.0, 1.0, 1.0, 1.0, Double.MIN_VALUE};
		gbl_dataPanel.rowWeights = new double[]{0.0, 0.0, 1.0, Double.MIN_VALUE};
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
		GridBagConstraints gbc_idValueLabel = new GridBagConstraints();
		gbc_idValueLabel.anchor = GridBagConstraints.WEST;
		gbc_idValueLabel.insets = new Insets(0, 0, 5, 5);
		gbc_idValueLabel.gridx = 1;
		gbc_idValueLabel.gridy = 0;
		dataPanel.add(idValueLabel, gbc_idValueLabel);

		JLabel lblName = new JLabel("Vendor name");
		GridBagConstraints gbc_lblName = new GridBagConstraints();
		gbc_lblName.anchor = GridBagConstraints.EAST;
		gbc_lblName.insets = new Insets(0, 0, 5, 5);
		gbc_lblName.gridx = 0;
		gbc_lblName.gridy = 1;
		dataPanel.add(lblName, gbc_lblName);

		vendorNameTextField = new JTextField();
		GridBagConstraints gbc_softwareNameTextField = new GridBagConstraints();
		gbc_softwareNameTextField.gridwidth = 4;
		gbc_softwareNameTextField.insets = new Insets(0, 0, 5, 0);
		gbc_softwareNameTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_softwareNameTextField.gridx = 1;
		gbc_softwareNameTextField.gridy = 1;
		dataPanel.add(vendorNameTextField, gbc_softwareNameTextField);
		vendorNameTextField.setColumns(10);

		JLabel lblDescription = new JLabel("Web address");
		GridBagConstraints gbc_lblDescription = new GridBagConstraints();
		gbc_lblDescription.anchor = GridBagConstraints.NORTH;
		gbc_lblDescription.insets = new Insets(0, 0, 0, 5);
		gbc_lblDescription.gridx = 0;
		gbc_lblDescription.gridy = 2;
		dataPanel.add(lblDescription, gbc_lblDescription);

		webAddressTextArea = new JTextArea();
		webAddressTextArea.setRows(3);
		webAddressTextArea.setWrapStyleWord(true);
		webAddressTextArea.setLineWrap(true);
		GridBagConstraints gbc_textArea = new GridBagConstraints();
		gbc_textArea.gridwidth = 4;
		gbc_textArea.fill = GridBagConstraints.BOTH;
		gbc_textArea.gridx = 1;
		gbc_textArea.gridy = 2;
		dataPanel.add(webAddressTextArea, gbc_textArea);

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
		JButton btnSave = new JButton(MainActionCommands.SAVE_VENDOR_DETAILS_COMMAND.getName());
		btnSave.setActionCommand(MainActionCommands.SAVE_VENDOR_DETAILS_COMMAND.getName());
		btnSave.addActionListener(listener);
		panel.add(btnSave);
		JRootPane rootPane = SwingUtilities.getRootPane(btnSave);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(btnSave);
		loadVendorData();
		pack();
	}

	private void loadVendorData() {
		//	New vendor
		if(manufacturer == null) {
			setTitle("Add new vendor/manufacturer");
			setIconImage(((ImageIcon) addVendorIcon).getImage());
		}
		else{	//	Edit software
			setTitle("Edit vendor/manufacturer \"" + manufacturer.getName() +"\"");
			setIconImage(((ImageIcon) editVendorIcon).getImage());
			idValueLabel.setText(manufacturer.getId());
			vendorNameTextField.setText(manufacturer.getName());
			webAddressTextArea.setText(manufacturer.getCatalogWebAddress());
		}
	}

	public Manufacturer getManufacturer() {
		return manufacturer;
	}	
	
	public String getVendorName() {
		return vendorNameTextField.getText().trim();
	}
	
	public String getVendorWebAddress() {
		return webAddressTextArea.getText().trim();
	}
	
	public Manufacturer getVendor() {
		return manufacturer;
	}
	
	public Collection<String>validateSoftware(){
		
		Collection<String>errors = new ArrayList<String>();
		if(getVendorName().isEmpty())
			errors.add("Vendor/Manufacturer's name must be specified");

		//	Validate web address
		String url = getVendorWebAddress();
		if(!url.isEmpty()) {
			String[] schemes = {"http","https"};
			UrlValidator urlValidator = new UrlValidator(schemes);
			if(!url.startsWith("http"))
				url = "https://" + url;
			
			if (!urlValidator.isValid(url)) {
				errors.add("Web address \"" + url +"\" is not valid.");
			} 
		}		
		//	Check for duplicate names
		Manufacturer sameName = null;
		if(!getVendorName().isEmpty()) {
			
			if(manufacturer == null) {							
				sameName = IDTDataCash.getManufacturerByName(getVendorName());
			}
			else{
				sameName = IDTDataCash.getManufacturers().stream().
					filter(m -> !m.getId().equals(manufacturer.getId())).
					filter(m -> m.getName().equals(manufacturer.getName())).
					findFirst().orElse(null);
			}
		}
		if(sameName != null) {
			errors.add("A different vendor/manufacturer \"" + 
					sameName.getName() +"\" is already in the database.");
		}
		return errors;
	}
}














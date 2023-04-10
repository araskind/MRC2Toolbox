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

package edu.umich.med.mrc2.datoolbox.gui.idtlims.software;

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
import java.util.ArrayList;
import java.util.Collection;

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
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

import edu.umich.med.mrc2.datoolbox.data.enums.SoftwareType;
import edu.umich.med.mrc2.datoolbox.data.lims.DataProcessingSoftware;
import edu.umich.med.mrc2.datoolbox.data.lims.Manufacturer;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCache;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class SoftwareEditorDialog extends JDialog implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4134991839687534786L;

	private static final Icon addSoftwareIcon = GuiUtils.getIcon("addSoftware", 32);
	private static final Icon editSoftwareIcon = GuiUtils.getIcon("editSoftware", 32);

	private JLabel idValueLabel;
	private JTextField softwareNameTextField;
	private JTextArea descriptionTextArea;
	private DataProcessingSoftware softwareItem;
	private JTextField vendorTextField;
	private Manufacturer softwareVendor;
	private VendorSelectorDialog vendorSelectorDialog;
	private JComboBox softwareTypeComboBox;
	
	public SoftwareEditorDialog(
			ActionListener listener, 
			DataProcessingSoftware softwareItem) {
		super();
		setPreferredSize(new Dimension(700, 300));
		// TODO Auto-generated constructor stub
		setModalityType(ModalityType.APPLICATION_MODAL);
		setResizable(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		this.softwareItem = softwareItem;
		
		JPanel dataPanel = new JPanel();
		dataPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(dataPanel, BorderLayout.CENTER);
		GridBagLayout gbl_dataPanel = new GridBagLayout();
		gbl_dataPanel.columnWidths = new int[]{0, 85, 126, 132, 0, 0};
		gbl_dataPanel.rowHeights = new int[]{0, 0, 0, 0, 0, 0};
		gbl_dataPanel.columnWeights = new double[]{0.0, 0.0, 1.0, 1.0, 1.0, Double.MIN_VALUE};
		gbl_dataPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
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

		JLabel lblName = new JLabel("Name");
		GridBagConstraints gbc_lblName = new GridBagConstraints();
		gbc_lblName.anchor = GridBagConstraints.EAST;
		gbc_lblName.insets = new Insets(0, 0, 5, 5);
		gbc_lblName.gridx = 0;
		gbc_lblName.gridy = 1;
		dataPanel.add(lblName, gbc_lblName);

		softwareNameTextField = new JTextField();
		GridBagConstraints gbc_softwareNameTextField = new GridBagConstraints();
		gbc_softwareNameTextField.gridwidth = 4;
		gbc_softwareNameTextField.insets = new Insets(0, 0, 5, 0);
		gbc_softwareNameTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_softwareNameTextField.gridx = 1;
		gbc_softwareNameTextField.gridy = 1;
		dataPanel.add(softwareNameTextField, gbc_softwareNameTextField);
		softwareNameTextField.setColumns(10);
		
		JLabel lblNewLabel_1 = new JLabel("Software type");
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_1.gridx = 1;
		gbc_lblNewLabel_1.gridy = 2;
		dataPanel.add(lblNewLabel_1, gbc_lblNewLabel_1);
		
		softwareTypeComboBox = new JComboBox<SoftwareType>(
				new DefaultComboBoxModel<SoftwareType>(SoftwareType.values()));
		softwareTypeComboBox.setSelectedIndex(-1);
		GridBagConstraints gbc_softwareTypeComboBox = new GridBagConstraints();
		gbc_softwareTypeComboBox.gridwidth = 2;
		gbc_softwareTypeComboBox.insets = new Insets(0, 0, 5, 0);
		gbc_softwareTypeComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_softwareTypeComboBox.gridx = 2;
		gbc_softwareTypeComboBox.gridy = 2;
		dataPanel.add(softwareTypeComboBox, gbc_softwareTypeComboBox);

		JLabel lblDescription = new JLabel("Description");
		GridBagConstraints gbc_lblDescription = new GridBagConstraints();
		gbc_lblDescription.anchor = GridBagConstraints.NORTH;
		gbc_lblDescription.insets = new Insets(0, 0, 5, 5);
		gbc_lblDescription.gridx = 0;
		gbc_lblDescription.gridy = 3;
		dataPanel.add(lblDescription, gbc_lblDescription);

		descriptionTextArea = new JTextArea();
		descriptionTextArea.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		descriptionTextArea.setRows(3);
		descriptionTextArea.setWrapStyleWord(true);
		descriptionTextArea.setLineWrap(true);
		GridBagConstraints gbc_textArea = new GridBagConstraints();
		gbc_textArea.gridwidth = 4;
		gbc_textArea.insets = new Insets(0, 0, 5, 0);
		gbc_textArea.fill = GridBagConstraints.BOTH;
		gbc_textArea.gridx = 1;
		gbc_textArea.gridy = 3;
		dataPanel.add(descriptionTextArea, gbc_textArea);
		
		JLabel lblNewLabel = new JLabel("Vendor");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.insets = new Insets(0, 0, 0, 5);
		gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 4;
		dataPanel.add(lblNewLabel, gbc_lblNewLabel);

		vendorTextField = new JTextField();
		vendorTextField.setEditable(false);
		GridBagConstraints gbc_vendorTextField = new GridBagConstraints();
		gbc_vendorTextField.gridwidth = 3;
		gbc_vendorTextField.insets = new Insets(0, 0, 0, 5);
		gbc_vendorTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_vendorTextField.gridx = 1;
		gbc_vendorTextField.gridy = 4;
		dataPanel.add(vendorTextField, gbc_vendorTextField);
		vendorTextField.setColumns(10);

		JButton btnSelectVendor = 
				new JButton(MainActionCommands.SHOW_SOFTWARE_VENDOR_SELECTOR_COMMAND.getName());
		btnSelectVendor.setActionCommand(
				MainActionCommands.SHOW_SOFTWARE_VENDOR_SELECTOR_COMMAND.getName());	//	TODO
		btnSelectVendor.addActionListener(this);
		GridBagConstraints gbc_btnSelectVendor = new GridBagConstraints();
		gbc_btnSelectVendor.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnSelectVendor.gridx = 4;
		gbc_btnSelectVendor.gridy = 4;
		dataPanel.add(btnSelectVendor, gbc_btnSelectVendor);

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
		JButton btnSave = new JButton(MainActionCommands.SAVE_SOFTWARE_DETAILS_COMMAND.getName());
		btnSave.setActionCommand(MainActionCommands.SAVE_SOFTWARE_DETAILS_COMMAND.getName());
		btnSave.addActionListener(listener);
		panel.add(btnSave);
		JRootPane rootPane = SwingUtilities.getRootPane(btnSave);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(btnSave);
		loadSoftwareData();
		pack();
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {

		if(e.getActionCommand().equals(
				MainActionCommands.SHOW_SOFTWARE_VENDOR_SELECTOR_COMMAND.getName()))
			showSoftwareVendorSelector();
		
		if(e.getActionCommand().equals(
				MainActionCommands.SELECT_SOFTWARE_VENDOR_COMMAND.getName())) 
			selectSoftwareVendor();		
	}
	
	private void showSoftwareVendorSelector() {
		
		vendorSelectorDialog = new VendorSelectorDialog(this);
		vendorSelectorDialog.setLocationRelativeTo(this);
		vendorSelectorDialog.setVisible(true);
	}

	private void selectSoftwareVendor() {

		if(vendorSelectorDialog.getSelectedVendor() != null) {
			
			softwareVendor = vendorSelectorDialog.getSelectedVendor();
			vendorTextField.setText(softwareVendor.getName());
			vendorSelectorDialog.dispose();
		}
	}

	private void loadSoftwareData() {
		
		//	New software
		if(softwareItem == null) {
			setTitle("Add new software");
			setIconImage(((ImageIcon) addSoftwareIcon).getImage());
		}
		else{	//	Edit software
			setTitle("Edit software \"" + softwareItem.getName() +"\"");
			setIconImage(((ImageIcon) editSoftwareIcon).getImage());
			idValueLabel.setText(softwareItem.getId());
			softwareNameTextField.setText(softwareItem.getName());
			softwareTypeComboBox.setSelectedItem(softwareItem.getSoftwareType());
			descriptionTextArea.setText(softwareItem.getDescription());
			softwareVendor = softwareItem.getVendor();
			vendorTextField.setText(softwareVendor.getName());
		}
	}

	public DataProcessingSoftware getSoftwareItem() {
		return softwareItem;
	}	
	
	public String getSoftwareName() {
		return softwareNameTextField.getText().trim();
	}
	
	public String getSoftwareDescription() {
		return descriptionTextArea.getText().trim();
	}
	
	public Manufacturer getSoftwareVendor() {
		return softwareVendor;
	}
	
	public SoftwareType getSoftwareType() {
		return (SoftwareType)softwareTypeComboBox.getSelectedItem();
	}
	
	public Collection<String>validateSoftware(){
		
		Collection<String>errors = new ArrayList<String>();
		if(getSoftwareName().isEmpty())
			errors.add("Software name must be specified");
		
		if(softwareVendor == null)
			errors.add("Software vendor must be specified");

		DataProcessingSoftware exisingSoftware = null;
		String name = getSoftwareName();
		if(!name.isEmpty()) {
			
			if(softwareItem == null) {
				
				exisingSoftware = IDTDataCache.getSoftwareList().stream().
					filter(s -> s.getName().equals(name)).
					findFirst().orElse(null);			
			}
			else {
				String id = softwareItem.getId();
				exisingSoftware = IDTDataCache.getSoftwareList().stream().
						filter(s -> !s.getId().equals(id)).
						filter(s -> s.getName().equals(name)).
						findFirst().orElse(null);	
			}
		}
		if(getSoftwareType() == null)
			errors.add("Software type must be specified");
		
		if(exisingSoftware != null) {
			errors.add("Software \"" + exisingSoftware.getName() +
					"\" is already in the database.");
		}
		return errors;
	}
}














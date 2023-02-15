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

package edu.umich.med.mrc2.datoolbox.gui.idtlims.sop;

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
import java.io.File;
import java.util.prefs.Preferences;

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

import edu.umich.med.mrc2.datoolbox.data.lims.LIMSProtocol;
import edu.umich.med.mrc2.datoolbox.data.lims.SopCategory;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCash;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.SortedComboBoxModel;
import edu.umich.med.mrc2.datoolbox.gui.utils.jnafilechooser.api.JnaFileChooser;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;

public class ProtocolEditorDialog extends JDialog implements ActionListener, BackedByPreferences{

	/**
	 *
	 */
	private static final long serialVersionUID = -8958656243635154039L;
	private Preferences preferences;
	public static final String PREFS_NODE = "edu.umich.med.mrc2.cefanalyzer.gui.ProtocolEditorDialog";
	public static final String BASE_DIRECTORY = "BASE_DIRECTORY";
	private static final Icon addProtocolIcon = GuiUtils.getIcon("addSop", 32);
	private static final Icon editProtocolIcon = GuiUtils.getIcon("editSop", 32);
	private static final String BROWSE_COMMAND = "BROWSE_COMMAND";

	private LIMSProtocol protocol;
	private JTextField nameTextField;
	private JButton btnSave;
	private JComboBox sopCategoryComboBox;
	private JLabel idValueLabel;
	private JTextField sopFileTextField;
	private JLabel groupValueLabel;
	private JLabel versionValueLabel;
	private JTextArea descriptionTextArea;
	private JLabel authorInfoLabel;
	private JLabel dateCreatedValueLabel;
	private JButton browseButton;
	private File baseDirectory;

	public ProtocolEditorDialog(LIMSProtocol protocol, ActionListener actionListener) {
		super();
		this.protocol = protocol;

		setPreferredSize(new Dimension(600, 400));
		setSize(new Dimension(600, 400));
		setModalityType(ModalityType.APPLICATION_MODAL);
		setResizable(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		JPanel dataPanel = new JPanel();
		dataPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(dataPanel, BorderLayout.CENTER);
		GridBagLayout gbl_dataPanel = new GridBagLayout();
		gbl_dataPanel.columnWidths = new int[] {50, 100, 50, 100, 50, 100};
		gbl_dataPanel.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0};
		gbl_dataPanel.columnWeights = new double[]{1.0, 0.0, 0.0, 0.0, 0.0, 1.0};
		gbl_dataPanel.rowWeights = new double[]{0.0, 0.0, 1.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
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
		gbc_idValueLabel.insets = new Insets(0, 0, 5, 5);
		gbc_idValueLabel.anchor = GridBagConstraints.SOUTHWEST;
		gbc_idValueLabel.gridx = 1;
		gbc_idValueLabel.gridy = 0;
		dataPanel.add(idValueLabel, gbc_idValueLabel);

		JLabel groupLabel = new JLabel("Group");
		groupLabel.setForeground(Color.BLUE);
		groupLabel.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_groupLabel = new GridBagConstraints();
		gbc_groupLabel.anchor = GridBagConstraints.EAST;
		gbc_groupLabel.insets = new Insets(0, 0, 5, 5);
		gbc_groupLabel.gridx = 2;
		gbc_groupLabel.gridy = 0;
		dataPanel.add(groupLabel, gbc_groupLabel);

		groupValueLabel = new JLabel("");
		GridBagConstraints gbc_groupValueLabel = new GridBagConstraints();
		gbc_groupValueLabel.fill = GridBagConstraints.HORIZONTAL;
		gbc_groupValueLabel.insets = new Insets(0, 0, 5, 5);
		gbc_groupValueLabel.gridx = 3;
		gbc_groupValueLabel.gridy = 0;
		dataPanel.add(groupValueLabel, gbc_groupValueLabel);

		JLabel lblVersion = new JLabel("Version");
		lblVersion.setForeground(Color.BLUE);
		lblVersion.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_lblVersion = new GridBagConstraints();
		gbc_lblVersion.anchor = GridBagConstraints.EAST;
		gbc_lblVersion.insets = new Insets(0, 0, 5, 5);
		gbc_lblVersion.gridx = 4;
		gbc_lblVersion.gridy = 0;
		dataPanel.add(lblVersion, gbc_lblVersion);

		versionValueLabel = new JLabel("");
		GridBagConstraints gbc_versionValueLabel = new GridBagConstraints();
		gbc_versionValueLabel.fill = GridBagConstraints.BOTH;
		gbc_versionValueLabel.insets = new Insets(0, 0, 5, 0);
		gbc_versionValueLabel.gridx = 5;
		gbc_versionValueLabel.gridy = 0;
		dataPanel.add(versionValueLabel, gbc_versionValueLabel);

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
		gbc_nameTextField.gridwidth = 5;
		gbc_nameTextField.insets = new Insets(0, 0, 5, 0);
		gbc_nameTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_nameTextField.gridx = 1;
		gbc_nameTextField.gridy = 1;
		dataPanel.add(nameTextField, gbc_nameTextField);
		nameTextField.setColumns(10);

		JLabel lblDescription = new JLabel("Description");
		GridBagConstraints gbc_lblDescription = new GridBagConstraints();
		gbc_lblDescription.anchor = GridBagConstraints.NORTHEAST;
		gbc_lblDescription.insets = new Insets(0, 0, 5, 5);
		gbc_lblDescription.gridx = 0;
		gbc_lblDescription.gridy = 2;
		dataPanel.add(lblDescription, gbc_lblDescription);

		descriptionTextArea = new JTextArea();
		descriptionTextArea.setLineWrap(true);
		descriptionTextArea.setWrapStyleWord(true);
		GridBagConstraints gbc_descriptionTextArea = new GridBagConstraints();
		gbc_descriptionTextArea.gridwidth = 5;
		gbc_descriptionTextArea.insets = new Insets(0, 0, 5, 0);
		gbc_descriptionTextArea.fill = GridBagConstraints.BOTH;
		gbc_descriptionTextArea.gridx = 1;
		gbc_descriptionTextArea.gridy = 2;
		dataPanel.add(descriptionTextArea, gbc_descriptionTextArea);

		JLabel lblAuthor = new JLabel("Author");
		GridBagConstraints gbc_lblAuthor = new GridBagConstraints();
		gbc_lblAuthor.anchor = GridBagConstraints.NORTHEAST;
		gbc_lblAuthor.insets = new Insets(0, 0, 5, 5);
		gbc_lblAuthor.gridx = 0;
		gbc_lblAuthor.gridy = 3;
		dataPanel.add(lblAuthor, gbc_lblAuthor);

		authorInfoLabel = new JLabel("");
		GridBagConstraints gbc_authorInfoLabel = new GridBagConstraints();
		gbc_authorInfoLabel.fill = GridBagConstraints.HORIZONTAL;
		gbc_authorInfoLabel.gridwidth = 2;
		gbc_authorInfoLabel.insets = new Insets(0, 0, 5, 5);
		gbc_authorInfoLabel.gridx = 1;
		gbc_authorInfoLabel.gridy = 3;
		dataPanel.add(authorInfoLabel, gbc_authorInfoLabel);

		JLabel lblDateCreated = new JLabel("Date created");
		GridBagConstraints gbc_lblDateCreated = new GridBagConstraints();
		gbc_lblDateCreated.anchor = GridBagConstraints.NORTHEAST;
		gbc_lblDateCreated.insets = new Insets(0, 0, 5, 5);
		gbc_lblDateCreated.gridx = 4;
		gbc_lblDateCreated.gridy = 3;
		dataPanel.add(lblDateCreated, gbc_lblDateCreated);

		dateCreatedValueLabel = new JLabel("");
		GridBagConstraints gbc_dateValueLAbelLabel = new GridBagConstraints();
		gbc_dateValueLAbelLabel.anchor = GridBagConstraints.NORTH;
		gbc_dateValueLAbelLabel.fill = GridBagConstraints.HORIZONTAL;
		gbc_dateValueLAbelLabel.insets = new Insets(0, 0, 5, 0);
		gbc_dateValueLAbelLabel.gridx = 5;
		gbc_dateValueLAbelLabel.gridy = 3;
		dataPanel.add(dateCreatedValueLabel, gbc_dateValueLAbelLabel);

		JLabel lblManufacturer = new JLabel("Category");
		GridBagConstraints gbc_lblManufacturer = new GridBagConstraints();
		gbc_lblManufacturer.anchor = GridBagConstraints.EAST;
		gbc_lblManufacturer.insets = new Insets(0, 0, 5, 5);
		gbc_lblManufacturer.gridx = 0;
		gbc_lblManufacturer.gridy = 4;
		dataPanel.add(lblManufacturer, gbc_lblManufacturer);

		sopCategoryComboBox = new JComboBox(new SortedComboBoxModel<SopCategory>(
				IDTDataCash.getSopCategories()));
		GridBagConstraints gbc_sopCategoryComboBox = new GridBagConstraints();
		gbc_sopCategoryComboBox.gridwidth = 2;
		gbc_sopCategoryComboBox.insets = new Insets(0, 0, 5, 5);
		gbc_sopCategoryComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_sopCategoryComboBox.gridx = 1;
		gbc_sopCategoryComboBox.gridy = 4;
		dataPanel.add(sopCategoryComboBox, gbc_sopCategoryComboBox);

		sopFileTextField = new JTextField();
		sopFileTextField.setEditable(false);
		GridBagConstraints gbc_textField = new GridBagConstraints();
		gbc_textField.gridwidth = 5;
		gbc_textField.insets = new Insets(0, 0, 0, 5);
		gbc_textField.fill = GridBagConstraints.HORIZONTAL;
		gbc_textField.gridx = 0;
		gbc_textField.gridy = 5;
		dataPanel.add(sopFileTextField, gbc_textField);

		browseButton = new JButton("Select protocol file");
		browseButton.setActionCommand(BROWSE_COMMAND);	//	TODO
		browseButton.addActionListener(this);
		GridBagConstraints gbc_browseButton = new GridBagConstraints();
		gbc_browseButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_browseButton.gridx = 5;
		gbc_browseButton.gridy = 5;
		dataPanel.add(browseButton, gbc_browseButton);

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

		loadProtocolData();
		loadPreferences();
		pack();
	}

	private void loadProtocolData() {

		if(protocol == null) {

			setTitle("Add new SOP protocol");
			setIconImage(((ImageIcon) addProtocolIcon).getImage());
			btnSave.setActionCommand(MainActionCommands.ADD_SOP_PROTOCOL_COMMAND.getName());
			sopCategoryComboBox.setSelectedIndex(-1);
		}
		else {
			setTitle("Edit information for " + protocol.getSopName());
			setIconImage(((ImageIcon) editProtocolIcon).getImage());
			btnSave.setActionCommand(MainActionCommands.EDIT_SOP_PROTOCOL_COMMAND.getName());
			idValueLabel.setText(protocol.getSopId());
			groupValueLabel.setText(protocol.getSopGroup());
			versionValueLabel.setText(protocol.getSopVersion());
			nameTextField.setText(protocol.getSopName());
			descriptionTextArea.setText(protocol.getSopDescription());

			if (protocol.getDateCrerated() != null)
				dateCreatedValueLabel.setText(MRC2ToolBoxConfiguration.getDateTimeFormat().format(protocol.getDateCrerated()));

			if(protocol.getCreatedBy() != null)
				authorInfoLabel.setText(protocol.getCreatedBy().getInfo());

			sopCategoryComboBox.setSelectedItem(protocol.getSopCategory());
			sopCategoryComboBox.setEnabled(false);
		}
		pack();
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		if(e.getActionCommand().equals(BROWSE_COMMAND))
			selectSOPFile();
	}
	
	private void selectSOPFile() {
		
		JnaFileChooser fc = new JnaFileChooser(baseDirectory);
		fc.setMode(JnaFileChooser.Mode.Files);
		fc.setTitle("Select SOP file");
		fc.setMultiSelectionEnabled(false);
		if (fc.showOpenDialog(this)) {
			
			File inputFile = fc.getSelectedFile();
			baseDirectory = inputFile.getParentFile();
			sopFileTextField.setText(inputFile.getAbsolutePath());
			savePreferences();
		}
	}

	public LIMSProtocol getProtocol() {
		return protocol;
	}

	public String getProtocolName() {
		return nameTextField.getText().trim();
	}

	public String getProtocolDescription() {
		return descriptionTextArea.getText().trim();
	}

	public SopCategory getSopCategory() {
		return (SopCategory)sopCategoryComboBox.getSelectedItem();
	}

	public File getSopFile() {

		if(sopFileTextField.getText().trim().isEmpty())
			return null;

		return new File(sopFileTextField.getText().trim());
	}

	@Override
	public void loadPreferences(Preferences prefs) {
		preferences = prefs;
		baseDirectory =  new File(preferences.get(
				BASE_DIRECTORY, MRC2ToolBoxConfiguration.getDefaultDataDirectory()));
	}

	@Override
	public void loadPreferences() {		
		loadPreferences(Preferences.userRoot().node(PREFS_NODE));
	}

	@Override
	public void savePreferences() {
		preferences = Preferences.userRoot().node(PREFS_NODE);
		preferences.put(BASE_DIRECTORY, baseDirectory.getAbsolutePath());
	}
}














































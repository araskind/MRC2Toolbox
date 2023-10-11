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

package edu.umich.med.mrc2.datoolbox.gui.library.manager;

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
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.prefs.Preferences;

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
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

import edu.umich.med.mrc2.datoolbox.data.Adduct;
import edu.umich.med.mrc2.datoolbox.data.CompoundLibrary;
import edu.umich.med.mrc2.datoolbox.data.enums.Polarity;
import edu.umich.med.mrc2.datoolbox.database.idt.MSRTLibraryUtils;
import edu.umich.med.mrc2.datoolbox.gui.library.feditor.AdductSelectionTable;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.jnafilechooser.api.JnaFileChooser;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;

public class LibraryInfoDialog extends JDialog 
	implements ActionListener, ItemListener, BackedByPreferences{

	/**
	 *
	 */
	private static final long serialVersionUID = 7917036676205745402L;
	private JTextField nameTextField;
	private JTextArea textArea;
	private JButton cancelButton, saveButton;
	private CompoundLibrary currentLibrary;

	private static final Icon newLibraryIcon = GuiUtils.getIcon("newLibrary", 32);
	private static final Icon editLibInfoIcon = GuiUtils.getIcon("editLibrary", 32);
	
	private JComboBox polarityComboBox;
	private JTextField libFileTextField;
	private JButton btnBrowse;
	private File inputLibraryFile;
	
	private static final String BROWSE = "BROWSE";
	
	private Preferences preferences;
	public static final String PREFS_NODE = "edu.umich.med.mrc2.datoolbox.LibraryInfoDialog"; 
	public static final String BASE_DIRECTORY = "BASE_DIRECTORY";
	private File baseDirectory;
	private JLabel createDefaultAdductsCheckBox;
	private JScrollPane scrollPane;
	private AdductSelectionTable adductsTable;
	
	public LibraryInfoDialog(ActionListener listener) {

		super();
		setSize(new Dimension(500, 500));
		setPreferredSize(new Dimension(500, 500));
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setModalityType(ModalityType.APPLICATION_MODAL);
		getContentPane().setLayout(new BorderLayout(0, 0));

		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(panel, BorderLayout.CENTER);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{0, 151, 166, 0, 0};
		gbl_panel.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0};
		gbl_panel.columnWeights = new double[]{0.0, 1.0, 1.0, 1.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);

		JLabel nameLabel = new JLabel("Name");
		GridBagConstraints gbc_nameLabel = new GridBagConstraints();
		gbc_nameLabel.insets = new Insets(0, 0, 5, 5);
		gbc_nameLabel.anchor = GridBagConstraints.EAST;
		gbc_nameLabel.gridx = 0;
		gbc_nameLabel.gridy = 0;
		panel.add(nameLabel, gbc_nameLabel);

		nameTextField = new JTextField();
		GridBagConstraints gbc_nameTextField = new GridBagConstraints();
		gbc_nameTextField.gridwidth = 3;
		gbc_nameTextField.insets = new Insets(0, 0, 5, 0);
		gbc_nameTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_nameTextField.gridx = 1;
		gbc_nameTextField.gridy = 0;
		panel.add(nameTextField, gbc_nameTextField);
		nameTextField.setColumns(10);
		
		JLabel lblNewLabel = new JLabel("Polarity");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 1;
		panel.add(lblNewLabel, gbc_lblNewLabel);
		
		polarityComboBox = new JComboBox<Polarity>(
				new DefaultComboBoxModel<Polarity>(
						new Polarity[] {Polarity.Positive, Polarity.Negative}));
		polarityComboBox.setSelectedIndex(-1);
		polarityComboBox.addItemListener(this);
		GridBagConstraints gbc_polarityComboBox = new GridBagConstraints();
		gbc_polarityComboBox.insets = new Insets(0, 0, 5, 5);
		gbc_polarityComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_polarityComboBox.gridx = 1;
		gbc_polarityComboBox.gridy = 1;
		panel.add(polarityComboBox, gbc_polarityComboBox);

		JLabel lblDescription = new JLabel("Description");
		GridBagConstraints gbc_lblDescription = new GridBagConstraints();
		gbc_lblDescription.anchor = GridBagConstraints.NORTH;
		gbc_lblDescription.insets = new Insets(0, 0, 5, 5);
		gbc_lblDescription.gridx = 0;
		gbc_lblDescription.gridy = 2;
		panel.add(lblDescription, gbc_lblDescription);

		textArea = new JTextArea();
		textArea.setRows(3);
		textArea.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		GridBagConstraints gbc_textArea = new GridBagConstraints();
		gbc_textArea.gridwidth = 3;
		gbc_textArea.insets = new Insets(0, 0, 5, 0);
		gbc_textArea.fill = GridBagConstraints.BOTH;
		gbc_textArea.gridx = 1;
		gbc_textArea.gridy = 2;
		panel.add(textArea, gbc_textArea);
		
		JLabel lblNewLabel_1 = new JLabel("Import data from file:");
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel_1.gridwidth = 2;
		gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_1.gridx = 0;
		gbc_lblNewLabel_1.gridy = 3;
		panel.add(lblNewLabel_1, gbc_lblNewLabel_1);
		
		libFileTextField = new JTextField();
		libFileTextField.setEditable(false);
		GridBagConstraints gbc_libFileTextField = new GridBagConstraints();
		gbc_libFileTextField.gridwidth = 3;
		gbc_libFileTextField.insets = new Insets(0, 0, 5, 5);
		gbc_libFileTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_libFileTextField.gridx = 0;
		gbc_libFileTextField.gridy = 4;
		panel.add(libFileTextField, gbc_libFileTextField);
		libFileTextField.setColumns(10);
		
		btnBrowse = new JButton("Browse");
		btnBrowse.setActionCommand(BROWSE);
		btnBrowse.addActionListener(this);
		GridBagConstraints gbc_btnNewButton = new GridBagConstraints();
		gbc_btnNewButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnNewButton.insets = new Insets(0, 0, 5, 0);
		gbc_btnNewButton.gridx = 3;
		gbc_btnNewButton.gridy = 4;
		panel.add(btnBrowse, gbc_btnNewButton);
		
		createDefaultAdductsCheckBox = 
				new JLabel("Create selected adducts during import:");
		GridBagConstraints gbc_chckbxNewCheckBox = new GridBagConstraints();
		gbc_chckbxNewCheckBox.anchor = GridBagConstraints.WEST;
		gbc_chckbxNewCheckBox.gridwidth = 2;
		gbc_chckbxNewCheckBox.insets = new Insets(0, 0, 5, 5);
		gbc_chckbxNewCheckBox.gridx = 0;
		gbc_chckbxNewCheckBox.gridy = 5;
		panel.add(createDefaultAdductsCheckBox, gbc_chckbxNewCheckBox);
		
		adductsTable = new AdductSelectionTable();
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.gridwidth = 4;
		gbc_scrollPane.insets = new Insets(0, 0, 5, 5);
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 6;
		panel.add(new JScrollPane(adductsTable), gbc_scrollPane);

		cancelButton = new JButton("Cancel");
		GridBagConstraints gbc_cancelButton = new GridBagConstraints();
		gbc_cancelButton.anchor = GridBagConstraints.WEST;
		gbc_cancelButton.insets = new Insets(0, 0, 0, 5);
		gbc_cancelButton.gridx = 1;
		gbc_cancelButton.gridy = 7;
		panel.add(cancelButton, gbc_cancelButton);

		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				dispose();
			}
		};
		cancelButton.addActionListener(al);
		
		saveButton = new JButton("Save");
		saveButton.addActionListener(listener);
		GridBagConstraints gbc_saveButton = new GridBagConstraints();
		gbc_saveButton.gridwidth = 2;
		gbc_saveButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_saveButton.gridx = 2;
		gbc_saveButton.gridy = 7;
		panel.add(saveButton, gbc_saveButton);

		JRootPane rootPane = SwingUtilities.getRootPane(saveButton);
		rootPane.setDefaultButton(saveButton);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		loadPreferences();
		pack();
	}
	
	@Override
	public void dispose() {
		
		savePreferences();
		super.dispose();
	}
	
	public void initNewLibrary(){

		setTitle("Create new library");
		setIconImage(((ImageIcon) newLibraryIcon).getImage());
		currentLibrary = new CompoundLibrary("New library");
		nameTextField.setText(currentLibrary.getLibraryName());
		textArea.setText(currentLibrary.getLibraryDescription());
		saveButton.setActionCommand(MainActionCommands.CREATE_NEW_LIBRARY_COMMAND.getName());
	}

	public void loadLibraryData(CompoundLibrary library){

		setTitle("Edit details for library \"" + library.getLibraryName() + "\"");
		setIconImage(((ImageIcon) editLibInfoIcon).getImage());
		currentLibrary = library;
		nameTextField.setText(library.getLibraryName());
		polarityComboBox.setSelectedItem(library.getPolarity());
		polarityComboBox.setEnabled(false);
		textArea.setText(library.getLibraryDescription());
		saveButton.setActionCommand(MainActionCommands.EDIT_MS_LIBRARY_INFO_COMMAND.getName());
		btnBrowse.setEnabled(false);
	}
	
	public void loadLibraryInfoAndDataForImport(CompoundLibrary library, File importFile){

		setTitle("Edit details for library \"" + library.getLibraryName() + "\"");
		setIconImage(((ImageIcon) editLibInfoIcon).getImage());
		currentLibrary = library;
		nameTextField.setText(library.getLibraryName());
		polarityComboBox.setSelectedItem(library.getPolarity());
		polarityComboBox.setEnabled(false);
		textArea.setText(library.getLibraryDescription());
		inputLibraryFile = importFile;
		libFileTextField.setText(inputLibraryFile.getAbsolutePath());
		btnBrowse.setEnabled(false);
		saveButton.setActionCommand(MainActionCommands.IMPORT_PCDL_COMPOUND_LIBRARY_COMMAND.getName());		
	}
	
	public CompoundLibrary getEditedLibrary(){
		return currentLibrary;
	}

	public String getLibraryDescription(){
		return textArea.getText().trim();
	}

	public String getLibraryName(){
		return nameTextField.getText().trim();
	}
	
	public Polarity getPolarity() {
		return (Polarity)polarityComboBox.getSelectedItem();
	}

	public Collection<String>validateLibraryData(){ 
		
		Collection<String>errors = new ArrayList<String>();
		String name = getLibraryName();
		if(name == null || name.isEmpty()) {
			errors.add("Library name cannot be empty");
		}
		else {
			//	Check for name conflict
			Collection<CompoundLibrary> libList = new ArrayList<CompoundLibrary>();			
			try {
				libList = MSRTLibraryUtils.getAllLibraries();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			for (CompoundLibrary l : libList) {
				
				if(l.getLibraryName().equals(name) 
						&& !l.getLibraryId().equals(currentLibrary.getLibraryId())) {
					errors.add("A different library with name \"" + name + "\" already exists");
				}
				
			}
		}
		if(getPolarity() == null)
			errors.add("Library polarity must be specified");
		
		//	Default adducts for text library
//		if(inputLibraryFile != null) {
//			
//			if(inputLibraryFile.getName().toLowerCase().endsWith(".txt") 
//					|| inputLibraryFile.getName().toLowerCase().endsWith(".tsv")) {
//				
//				if(createDefaultAdductsCheckBox.isSelected() 
//						&& adductsTable.getSelectedAdducts().isEmpty()) {
//					errors.add("\"Create default adducts during import\" option is enabled,\n"
//							+ "but no adducts selected. Please select the adducts from the table.");
//				}
//			}
//		}
		return errors;
	}

	public File getInputLibraryFile() {
		return inputLibraryFile;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		
		if(e.getActionCommand().equals(BROWSE))
			selectLibraryFile();
	}
	
	private void selectLibraryFile() {

		JnaFileChooser fc = new JnaFileChooser(baseDirectory);
		fc.setMode(JnaFileChooser.Mode.Files);
		fc.addFilter("CEF files", "cef", "CEF");
		fc.addFilter("Library Editor files", "xml", "XML");	
		fc.addFilter("PCDL Compound list files (TAB-separated)", "txt", "TXT", "tsv", "TSV");
		fc.setTitle("Select library file to import");
		fc.setMultiSelectionEnabled(false);
		if (fc.showOpenDialog(SwingUtilities.getWindowAncestor(this.getContentPane()))) {
			
			inputLibraryFile = fc.getSelectedFile();
			baseDirectory = inputLibraryFile.getParentFile();
			libFileTextField.setText(inputLibraryFile.getAbsolutePath());
			if(!inputLibraryFile.getName().toLowerCase().endsWith(".txt") 
					&& !inputLibraryFile.getName().toLowerCase().endsWith(".tsv")) {
//				createDefaultAdductsCheckBox.setSelected(false);
//				createDefaultAdductsCheckBox.setEnabled(false);
				adductsTable.setEnabled(false);
			}
			else {
				createDefaultAdductsCheckBox.setEnabled(true);
				adductsTable.setEnabled(true);
			}
			savePreferences();
		}					
	}

	@Override
	public void loadPreferences(Preferences prefs) {
		preferences = prefs;
		baseDirectory =  
				new File(preferences.get(BASE_DIRECTORY, 
						MRC2ToolBoxConfiguration.getDefaultDataDirectory()));
	}

	@Override
	public void loadPreferences() {
		loadPreferences(Preferences.userRoot().node(PREFS_NODE));
	}

	@Override
	public void savePreferences() {
		preferences = Preferences.userRoot().node(PREFS_NODE);
		if(baseDirectory != null && baseDirectory.exists())
			preferences.put(BASE_DIRECTORY, baseDirectory.getAbsolutePath());
	}

	@Override
	public void itemStateChanged(ItemEvent e) {

		if (e.getStateChange() == ItemEvent.SELECTED
				&& e.getItem() instanceof Polarity) {

			adductsTable.setTableModelFromAdductListForPolarity((Polarity)e.getItem());
		}
	}
	
	public Collection<Adduct>getSelectedAdducts(){
		
		if(adductsTable.getSelectedAdducts().isEmpty())
			return null;
		else
			return adductsTable.getSelectedAdducts();
	}
	
//	public boolean createDefaultAdducts() {
//		return createDefaultAdductsCheckBox.isSelected();
//	}
}


















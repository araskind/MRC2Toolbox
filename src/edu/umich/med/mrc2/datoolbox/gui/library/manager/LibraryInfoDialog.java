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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
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
import java.util.Date;
import java.util.prefs.Preferences;

import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
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
import edu.umich.med.mrc2.datoolbox.data.enums.AdductSubset;
import edu.umich.med.mrc2.datoolbox.data.enums.Polarity;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCache;
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
	
	private static final Icon newLibraryIcon = GuiUtils.getIcon("newLibrary", 32);
	private static final Icon editLibInfoIcon = GuiUtils.getIcon("editLibrary", 32);
	private static final Icon duplicateLibraryIcon = GuiUtils.getIcon("duplicateLibrary", 32);
	
	private JTextField nameTextField;
	private JTextArea libraryDescriptionTextArea;
	private JButton cancelButton, saveButton;
	private JComboBox polarityComboBox;
	private JTextField libFileTextField;
	private JButton btnBrowse;
	
	
	private static final String BROWSE = "BROWSE";
	
	private Preferences preferences;
	public static final String PREFS_NODE = "edu.umich.med.mrc2.datoolbox.LibraryInfoDialog"; 
	public static final String BASE_DIRECTORY = "BASE_DIRECTORY";
	private File baseDirectory;
	private JLabel createDefaultAdductsLabel;
	private AdductSelectionTable adductsTable;
	private JComboBox<AdductSubset> adductSubsetComboBox;
	private JCheckBox clearRtCheckBox;
	private JCheckBox clearAnnotationsCheckBox;
	private JLabel neutralPolarityWarningLabel;
	private JCheckBox preserveSpectraOnCopyCheckBox;
	private JLabel adductSubsetLabel;
	private JScrollPane adductScroll;
	private JLabel idfLabel;
	
	private File inputLibraryFile;
	private CompoundLibrary currentLibrary;
	
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
		gbl_panel.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		gbl_panel.columnWeights = new double[]{0.0, 1.0, 1.0, 1.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
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
		
		neutralPolarityWarningLabel = new JLabel("Create template library without spectra");
		neutralPolarityWarningLabel.setForeground(Color.RED);
		neutralPolarityWarningLabel.setFont(new Font("Tahoma", Font.BOLD, 12));
		GridBagConstraints gbc_neutralPolarityWarningLabel = new GridBagConstraints();
		gbc_neutralPolarityWarningLabel.anchor = GridBagConstraints.WEST;
		gbc_neutralPolarityWarningLabel.gridwidth = 2;
		gbc_neutralPolarityWarningLabel.insets = new Insets(0, 0, 5, 0);
		gbc_neutralPolarityWarningLabel.gridx = 2;
		gbc_neutralPolarityWarningLabel.gridy = 1;
		panel.add(neutralPolarityWarningLabel, gbc_neutralPolarityWarningLabel);
		neutralPolarityWarningLabel.setVisible(false);

		JLabel lblDescription = new JLabel("Description");
		GridBagConstraints gbc_lblDescription = new GridBagConstraints();
		gbc_lblDescription.anchor = GridBagConstraints.NORTH;
		gbc_lblDescription.insets = new Insets(0, 0, 5, 5);
		gbc_lblDescription.gridx = 0;
		gbc_lblDescription.gridy = 2;
		panel.add(lblDescription, gbc_lblDescription);

		libraryDescriptionTextArea = new JTextArea();
		libraryDescriptionTextArea.setRows(3);
		libraryDescriptionTextArea.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		libraryDescriptionTextArea.setLineWrap(true);
		libraryDescriptionTextArea.setWrapStyleWord(true);
		GridBagConstraints gbc_textArea = new GridBagConstraints();
		gbc_textArea.gridwidth = 3;
		gbc_textArea.insets = new Insets(0, 0, 5, 0);
		gbc_textArea.fill = GridBagConstraints.BOTH;
		gbc_textArea.gridx = 1;
		gbc_textArea.gridy = 2;
		panel.add(libraryDescriptionTextArea, gbc_textArea);
		
		idfLabel = new JLabel("Import data from file:");
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel_1.gridwidth = 2;
		gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_1.gridx = 0;
		gbc_lblNewLabel_1.gridy = 3;
		panel.add(idfLabel, gbc_lblNewLabel_1);
		
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
		
		preserveSpectraOnCopyCheckBox = 
				new JCheckBox("Preserve spectra when creating library copy");
		GridBagConstraints gbc_preserveSpectraOnCopyCheckBox = new GridBagConstraints();
		gbc_preserveSpectraOnCopyCheckBox.anchor = GridBagConstraints.WEST;
		gbc_preserveSpectraOnCopyCheckBox.gridwidth = 3;
		gbc_preserveSpectraOnCopyCheckBox.insets = new Insets(0, 0, 5, 5);
		gbc_preserveSpectraOnCopyCheckBox.gridx = 0;
		gbc_preserveSpectraOnCopyCheckBox.gridy = 5;
		panel.add(preserveSpectraOnCopyCheckBox, gbc_preserveSpectraOnCopyCheckBox);
		preserveSpectraOnCopyCheckBox.setVisible(false);
		preserveSpectraOnCopyCheckBox.addItemListener(this);
		
		createDefaultAdductsLabel = 
				new JLabel("Create selected adducts during import / copy:");
		GridBagConstraints gbc_createDefaultAdductsCheckBox = new GridBagConstraints();
		gbc_createDefaultAdductsCheckBox.anchor = GridBagConstraints.WEST;
		gbc_createDefaultAdductsCheckBox.gridwidth = 3;
		gbc_createDefaultAdductsCheckBox.insets = new Insets(0, 0, 5, 5);
		gbc_createDefaultAdductsCheckBox.gridx = 0;
		gbc_createDefaultAdductsCheckBox.gridy = 6;
		panel.add(createDefaultAdductsLabel, gbc_createDefaultAdductsCheckBox);
		
		adductSubsetLabel = new JLabel("Adduct subset ");
		GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
		gbc_lblNewLabel_2.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_2.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_2.gridx = 1;
		gbc_lblNewLabel_2.gridy = 7;
		panel.add(adductSubsetLabel, gbc_lblNewLabel_2);
		
		adductSubsetComboBox = new JComboBox<AdductSubset>(
				new DefaultComboBoxModel<AdductSubset>(
						new AdductSubset[] {
								AdductSubset.MOST_COMMON, 
								AdductSubset.COMPLETE_LIST
						}));
		adductSubsetComboBox.setSelectedItem(AdductSubset.MOST_COMMON);
		adductSubsetComboBox.addItemListener(this);
		GridBagConstraints gbc_comboBox = new GridBagConstraints();
		gbc_comboBox.insets = new Insets(0, 0, 5, 5);
		gbc_comboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBox.gridx = 2;
		gbc_comboBox.gridy = 7;
		panel.add(adductSubsetComboBox, gbc_comboBox);
		
		adductsTable = new AdductSelectionTable();
		adductScroll = new JScrollPane(adductsTable);
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.gridwidth = 4;
		gbc_scrollPane.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 8;
		panel.add(adductScroll, gbc_scrollPane);
		
		clearRtCheckBox = new JCheckBox("Clear retention times");
		GridBagConstraints gbc_clearRtCheckBox = new GridBagConstraints();
		gbc_clearRtCheckBox.anchor = GridBagConstraints.WEST;
		gbc_clearRtCheckBox.insets = new Insets(0, 0, 0, 5);
		gbc_clearRtCheckBox.gridx = 1;
		gbc_clearRtCheckBox.gridy = 9;
		panel.add(clearRtCheckBox, gbc_clearRtCheckBox);
		clearRtCheckBox.setVisible(false);
		
		clearAnnotationsCheckBox = new JCheckBox("Clear annotations");
		GridBagConstraints gbc_clearAnnotationsCheckBox = new GridBagConstraints();
		gbc_clearAnnotationsCheckBox.anchor = GridBagConstraints.WEST;
		gbc_clearAnnotationsCheckBox.insets = new Insets(0, 0, 0, 5);
		gbc_clearAnnotationsCheckBox.gridx = 2;
		gbc_clearAnnotationsCheckBox.gridy = 9;
		panel.add(clearAnnotationsCheckBox, gbc_clearAnnotationsCheckBox);
		clearAnnotationsCheckBox.setVisible(false);

		JPanel buttonPanel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) buttonPanel.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);

		cancelButton = new JButton("Cancel");
		buttonPanel.add(cancelButton);
		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				dispose();
			}
		};
		cancelButton.addActionListener(al);

		saveButton = new JButton("Save");
		saveButton.addActionListener(listener);
		buttonPanel.add(saveButton);
		JRootPane rootPane = SwingUtilities.getRootPane(saveButton);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(saveButton);
		
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
		libraryDescriptionTextArea.setText(currentLibrary.getLibraryDescription());
		saveButton.setActionCommand(MainActionCommands.CREATE_NEW_LIBRARY_COMMAND.getName());
	}

	@SuppressWarnings("unchecked")
	public void loadLibraryData(
			CompoundLibrary library, 
			boolean configureForDuplication){

		setTitle("Edit details for library \"" + library.getLibraryName() + "\"");
		setIconImage(((ImageIcon) editLibInfoIcon).getImage());
		currentLibrary = library;
		nameTextField.setText(library.getLibraryName());
		polarityComboBox.setSelectedItem(library.getPolarity());
		polarityComboBox.setEnabled(false);
		libraryDescriptionTextArea.setText(library.getLibraryDescription());
		saveButton.setActionCommand(MainActionCommands.EDIT_MS_LIBRARY_INFO_COMMAND.getName());
		btnBrowse.setEnabled(false);
		if(configureForDuplication) {

			setTitle("Duplicate library \"" + library.getLibraryName() + "\"");
			setIconImage(((ImageIcon) duplicateLibraryIcon).getImage());
			
			nameTextField.setText(library.getLibraryName() + 
					" Copy-" + MRC2ToolBoxConfiguration.getFileTimeStampFormat().format(new Date()));
			libraryDescriptionTextArea.setText(library.getLibraryDescription() + 
					"\nCopy-" + MRC2ToolBoxConfiguration.getFileTimeStampFormat().format(new Date()));
			
			clearRtCheckBox.setVisible(true);			
			clearAnnotationsCheckBox.setVisible(true);
			preserveSpectraOnCopyCheckBox.setVisible(true);
			
			((DefaultComboBoxModel<Polarity>)polarityComboBox.getModel()).addElement(Polarity.Neutral);
			polarityComboBox.setEnabled(true);
			polarityComboBox.setSelectedItem(library.getPolarity());
			
			boolean isNeutral = library.getPolarity().equals(Polarity.Neutral);
			neutralPolarityWarningLabel.setVisible(isNeutral);
			preserveSpectraOnCopyCheckBox.setVisible(!isNeutral);
			preserveSpectraOnCopyCheckBox.setSelected(!isNeutral);
			toggleFileImportBlock(false);
			saveButton.setActionCommand(MainActionCommands.DUPLICATE_LIBRARY_COMMAND.getName());
		}
		else {
			toggleAdductSelector(false);
			toggleFileImportBlock(false);
		}
	}
	
	public void loadLibraryInfoAndDataForImport(CompoundLibrary library, File importFile){

		setTitle("Edit details for library \"" + library.getLibraryName() + "\"");
		setIconImage(((ImageIcon) editLibInfoIcon).getImage());
		currentLibrary = library;
		nameTextField.setText(library.getLibraryName());
		polarityComboBox.setSelectedItem(library.getPolarity());
		polarityComboBox.setEnabled(false);
		preserveSpectraOnCopyCheckBox.setVisible(false);
		libraryDescriptionTextArea.setText(library.getLibraryDescription());
		inputLibraryFile = importFile;
		libFileTextField.setText(inputLibraryFile.getAbsolutePath());
		btnBrowse.setEnabled(false);
		saveButton.setActionCommand(MainActionCommands.IMPORT_PCDL_COMPOUND_LIBRARY_COMMAND.getName());		
	}
	
	public CompoundLibrary getLibrary(){
		return currentLibrary;
	}

	public String getLibraryDescription(){
		return libraryDescriptionTextArea.getText().trim();
	}

	public String getLibraryName(){
		return nameTextField.getText().trim();
	}
	
	public Polarity getPolarity() {
		return (Polarity)polarityComboBox.getSelectedItem();
	}
	
	public AdductSubset getAdductSubset() {
		return (AdductSubset) adductSubsetComboBox.getSelectedItem();
	}

	public Collection<String>validateLibraryData(){ 
		
		Collection<String>errors = new ArrayList<String>();
		String name = getLibraryName();
		if(name == null || name.isEmpty()) {
			errors.add("Library name cannot be empty");
		}
		else {
			//	Check for name conflict
			Collection<CompoundLibrary> libList = 
					IDTDataCache.getMsRtLibraryList();			
			for (CompoundLibrary l : libList) {
				
				if(l.getLibraryName().equals(name) 
						&& !l.getLibraryId().equals(currentLibrary.getLibraryId())) {
					errors.add("A different library with name \"" + name + "\" already exists");
				}				
			}
		}
		if(!saveButton.getActionCommand().equals(
				MainActionCommands.DUPLICATE_LIBRARY_COMMAND.getName())				
				&& getPolarity() == null) {
			errors.add("Library polarity must be specified");
		}
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
				createDefaultAdductsLabel.setEnabled(true);
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
		
		if(e.getSource().equals(preserveSpectraOnCopyCheckBox)) {
			
			toggleAdductSelector(!preserveSpectraOnCopyCheckBox.isSelected());
			return;
		}
		if (e.getStateChange() == ItemEvent.SELECTED) {
			
			if((e.getItem() instanceof Polarity 
					|| e.getItem() instanceof AdductSubset)) {

				adductsTable.setTableModelFromAdductListForPolarityAndSubset(
						getPolarity(), getAdductSubset());
			}
			if(e.getItem() instanceof Polarity) {
								
				boolean isNeutral = getPolarity().equals(Polarity.Neutral);
				if(preserveSpectraOnCopyCheckBox.isSelected() 
						&& (isNeutral || !getPolarity().equals(currentLibrary.getPolarity())))
					preserveSpectraOnCopyCheckBox.setSelected(false);
				
				if(isNeutral || !getPolarity().equals(currentLibrary.getPolarity()))
					preserveSpectraOnCopyCheckBox.setEnabled(false);
				else
					preserveSpectraOnCopyCheckBox.setEnabled(true);
				
				toggleAdductSelector(!isNeutral);
				neutralPolarityWarningLabel.setVisible(isNeutral);
			}			
		}
	}
	
	private void toggleAdductSelector(boolean visible) {
		
		adductSubsetComboBox.setVisible(visible);
		adductScroll.setVisible(visible);
		createDefaultAdductsLabel.setVisible(visible);
		adductSubsetLabel.setVisible(visible);		
	}
	
	private void toggleFileImportBlock(boolean visible) {
		
		idfLabel.setVisible(visible);		
		libFileTextField.setVisible(visible);
		btnBrowse.setVisible(visible);
	}
	
	public Collection<Adduct>getSelectedAdducts(){
		
		if(adductsTable.getSelectedAdducts().isEmpty())
			return null;
		else
			return adductsTable.getSelectedAdducts();
	}
	
	public boolean clearRetention() {
		return clearRtCheckBox.isSelected();
	}
	
	public boolean clearAnnotations() {
		return clearAnnotationsCheckBox.isSelected();
	}
	
	public boolean preserveSpectraOnCopy() {
		
		if(preserveSpectraOnCopyCheckBox.isVisible() 
				&& preserveSpectraOnCopyCheckBox.isEnabled())		
			return preserveSpectraOnCopyCheckBox.isSelected();
		else
			return false;
	}
	
}


















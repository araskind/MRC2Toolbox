/*******************************************************************************
 *
 * (C) Copyright 2018-2026 MRC2 (http://mrc2.umich.edu).
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
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.prefs.Preferences;

import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
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

import edu.umich.med.mrc2.datoolbox.data.Adduct;
import edu.umich.med.mrc2.datoolbox.data.CompoundLibrary;
import edu.umich.med.mrc2.datoolbox.data.enums.AdductSubset;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundValidationType;
import edu.umich.med.mrc2.datoolbox.data.enums.Polarity;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCache;
import edu.umich.med.mrc2.datoolbox.gui.adducts.adduct.AdductSelectorPanel;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.jnafilechooser.api.JnaFileChooser;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;

public class ExternalLibraryVerificationAndUploadDialog extends JDialog implements ActionListener, BackedByPreferences, ItemListener{

	private static final long serialVersionUID = 1L;
	private static final Icon pcdlLibraryIcon = GuiUtils.getIcon("newPCDLfromBase", 32);
	private static final String BROWSE = "BROWSE";

	private Preferences preferences;
	public static final String BASE_DIRECTORY = "BASE_DIRECTORY";
	private File baseDirectory;
	private File inputLibraryFile;
	private CompoundLibrary masterLibrary;
	
	private JTextField nameTextField;
	private JTextArea libraryDescriptionTextArea;
	private JTextField libFileTextField;
	private JCheckBox createDefaultAdductsCheckbox;
	private JComboBox<CompoundValidationType> compoundVerificationTypeComboBox;
	private AdductSelectorPanel adductSelectorPanel;
	private LibraryListingTable libraryListingTable;
	private int rowCount;
	private JLabel neutralPolarityWarningLabel;
	private JLabel adductSubsetLabel;

	public ExternalLibraryVerificationAndUploadDialog(ActionListener listener, boolean verifyOnly) {
		super();
		int height = 800;
		if(verifyOnly) 
			height = 250;
		
		setSize(new Dimension(800, height));
		setPreferredSize(new Dimension(800, height));
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setModalityType(ModalityType.APPLICATION_MODAL);
		getContentPane().setLayout(new BorderLayout(0, 0));

		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(panel, BorderLayout.CENTER);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{0, 293, 262, 0, 0};
		gbl_panel.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0};
		gbl_panel.columnWeights = new double[]{1.0, 1.0, 1.0, 1.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);
		
		rowCount = 0;
		
		JLabel idfLabel = new JLabel("Select external library file:");
		GridBagConstraints gbc_idfLabel = new GridBagConstraints();
		gbc_idfLabel.anchor = GridBagConstraints.WEST;
		gbc_idfLabel.gridwidth = 3;
		gbc_idfLabel.insets = new Insets(0, 0, 5, 5);
		gbc_idfLabel.gridx = 0;
		gbc_idfLabel.gridy = rowCount;
		panel.add(idfLabel, gbc_idfLabel);
		
		rowCount++;
		
		libFileTextField = new JTextField();
		libFileTextField.setEditable(false);
		GridBagConstraints gbc_libFileTextField = new GridBagConstraints();
		gbc_libFileTextField.gridwidth = 3;
		gbc_libFileTextField.insets = new Insets(0, 0, 5, 5);
		gbc_libFileTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_libFileTextField.gridx = 0;
		gbc_libFileTextField.gridy = rowCount;
		panel.add(libFileTextField, gbc_libFileTextField);
		libFileTextField.setColumns(10);
		
		JButton btnBrowse = new JButton("Browse");
		btnBrowse.setActionCommand(BROWSE);
		btnBrowse.addActionListener(this);
		GridBagConstraints gbc_btnNewButton = new GridBagConstraints();
		gbc_btnNewButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnNewButton.insets = new Insets(0, 0, 5, 0);
		gbc_btnNewButton.gridx = 3;
		gbc_btnNewButton.gridy = rowCount;
		panel.add(btnBrowse, gbc_btnNewButton);
		
		rowCount++;
		
		JLabel lblNewLabel_1 = new JLabel("Verify library compounds");
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_1.gridx = 0;
		gbc_lblNewLabel_1.gridy = rowCount;
		panel.add(lblNewLabel_1, gbc_lblNewLabel_1);
		
		compoundVerificationTypeComboBox = new JComboBox<>(
				new DefaultComboBoxModel<>(CompoundValidationType.values()));
		compoundVerificationTypeComboBox.setEditable(false);
		GridBagConstraints gbc_compoundVerificationTypeComboBox = new GridBagConstraints();
		gbc_compoundVerificationTypeComboBox.insets = new Insets(0, 0, 5, 0);
		gbc_compoundVerificationTypeComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_compoundVerificationTypeComboBox.gridx = 1;
		gbc_compoundVerificationTypeComboBox.gridy = rowCount;
		panel.add(compoundVerificationTypeComboBox, gbc_compoundVerificationTypeComboBox);
		
		rowCount++;
		
		if(verifyOnly == false)
			createAdductSelectionBlock(panel);
		
		JPanel buttonPanel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) buttonPanel.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);

		JButton btnCancel = new JButton("Cancel");
		buttonPanel.add(btnCancel);
		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		btnCancel.addActionListener(e -> dispose());

		//	TODO
		JButton btnSave = new JButton("TODO");
		btnSave.setActionCommand("TODO");
		btnSave.addActionListener(listener);
		buttonPanel.add(btnSave);
		JRootPane rootPane = SwingUtilities.getRootPane(btnSave);
		rootPane.registerKeyboardAction(al -> dispose(), stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(btnSave);
		
		loadPreferences();
		pack();
	}
	
	private void createMasterLibrarySelectionBlock(JPanel parent) {
		
		libraryListingTable = new LibraryListingTable();
		
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.gridwidth = 2;
		gbc_scrollPane.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 0;
		parent.add(new JScrollPane(libraryListingTable), gbc_scrollPane);
	}
	
	private void createAdductSelectionBlock(JPanel panel) {
		
		createDefaultAdductsCheckbox = 
				new JCheckBox("Create selected adducts when importing the library:");
		GridBagConstraints gbc_createDefaultAdductsCheckBox = new GridBagConstraints();
		gbc_createDefaultAdductsCheckBox.anchor = GridBagConstraints.WEST;
		gbc_createDefaultAdductsCheckBox.gridwidth = 2;
		gbc_createDefaultAdductsCheckBox.insets = new Insets(0, 0, 5, 5);
		gbc_createDefaultAdductsCheckBox.gridx = 0;
		gbc_createDefaultAdductsCheckBox.gridy = rowCount;
		panel.add(createDefaultAdductsCheckbox, gbc_createDefaultAdductsCheckBox);	
		
		neutralPolarityWarningLabel = new JLabel("Create template library without spectra");
		neutralPolarityWarningLabel.setForeground(Color.RED);
		neutralPolarityWarningLabel.setFont(new Font("Tahoma", Font.BOLD, 12));
		GridBagConstraints gbc_neutralPolarityWarningLabel = new GridBagConstraints();
		gbc_neutralPolarityWarningLabel.anchor = GridBagConstraints.WEST;
		gbc_neutralPolarityWarningLabel.gridwidth = 2;
		gbc_neutralPolarityWarningLabel.insets = new Insets(0, 0, 5, 0);
		gbc_neutralPolarityWarningLabel.gridx = 2;
		gbc_neutralPolarityWarningLabel.gridy = rowCount;
		panel.add(neutralPolarityWarningLabel, gbc_neutralPolarityWarningLabel);
		neutralPolarityWarningLabel.setVisible(false);

		rowCount++;
		
		adductSelectorPanel = new AdductSelectorPanel(true);
		GridBagConstraints gbc_adductSelectorPanel = new GridBagConstraints();
		gbc_adductSelectorPanel.anchor = GridBagConstraints.WEST;
		gbc_adductSelectorPanel.gridwidth = 4;
		gbc_adductSelectorPanel.insets = new Insets(0, 0, 5, 0);
		gbc_adductSelectorPanel.fill = GridBagConstraints.BOTH;
		gbc_adductSelectorPanel.gridx = 0;
		gbc_adductSelectorPanel.gridy = rowCount;
		gbc_adductSelectorPanel.weighty = 1.0d;
		panel.add(adductSelectorPanel, gbc_adductSelectorPanel);
		adductSelectorPanel.addPolarityListener(this);
		
		rowCount++;
				
		adductSubsetLabel = new JLabel("   ");
		GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
		gbc_adductSelectorPanel.insets = new Insets(0, 0, 5, 0);
		gbc_adductSelectorPanel.fill = GridBagConstraints.BOTH;
		gbc_lblNewLabel_2.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_2.gridx = 1;
		gbc_lblNewLabel_2.gridy = rowCount;
		gbc_lblNewLabel_2.weighty = 1.0d;
		panel.add(adductSubsetLabel, gbc_lblNewLabel_2);
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		if(e.getActionCommand().equals(BROWSE))
			selectLibraryFile();
	}
	
	private void selectLibraryFile() {

		JnaFileChooser fc = new JnaFileChooser(baseDirectory);
		fc.setMode(JnaFileChooser.Mode.Files);
//		fc.addFilter("CEF files", "cef", "CEF");
//		fc.addFilter("Library Editor files", "xml", "XML");	
		fc.addFilter("TAB-separated text files", "txt", "TXT", "tsv", "TSV");
		fc.setTitle("Select library file to scan");
		fc.setMultiSelectionEnabled(false);
		if (fc.showOpenDialog(SwingUtilities.getWindowAncestor(this.getContentPane()))) {		

			if (inputLibraryFile.getName().toLowerCase().endsWith(".txt")
					|| inputLibraryFile.getName().toLowerCase().endsWith(".tsv")) {
				inputLibraryFile = fc.getSelectedFile();
				baseDirectory = inputLibraryFile.getParentFile();
				libFileTextField.setText(inputLibraryFile.getAbsolutePath());
				savePreferences();
			}		
			//	TODO Agilent Library Editor files
			if (inputLibraryFile.getName().toLowerCase().endsWith("mslibrary.xml")) {

				MessageDialog.showWarningMsg(
						"Library Editor files validation under development.", this);
			}
			//	TODO Agilent CEF files
			if (inputLibraryFile.getName().toLowerCase().endsWith(".cef")) {
				MessageDialog.showWarningMsg(
						"CEF library validation under development.", this);
			}
		}					
	}

	public String getLibraryDescription(){
		return libraryDescriptionTextArea.getText().trim();
	}

	public String getLibraryName(){
		return nameTextField.getText().trim();
	}
	
	public Polarity getPolarity() {
		return adductSelectorPanel.getPolarity();
	}
	
	public AdductSubset getAdductSubset() {
		return adductSelectorPanel.getAdductSubset();
	}
	
	public Collection<Adduct>getSelectedAdducts(){
		return adductSelectorPanel.getSelectedAdducts();
	}
	
	public boolean createDefaultAdducts() {
		return createDefaultAdductsCheckbox.isSelected();
	}
	
	@Override
	public void loadPreferences(Preferences preferences) {
		
		this.preferences = preferences;
		baseDirectory = Paths.get(preferences.get(BASE_DIRECTORY, 
				MRC2ToolBoxConfiguration.getDefaultExperimentsDirectory())).toFile();
	}

	@Override
	public void loadPreferences() {
		loadPreferences(Preferences.userNodeForPackage(this.getClass()));		
	}

	@Override
	public void savePreferences() {

		preferences = Preferences.userNodeForPackage(this.getClass());
		if(baseDirectory != null)
			preferences.put(BASE_DIRECTORY, baseDirectory.getAbsolutePath());
	}

	public CompoundLibrary getBasePCDLlibrary() {
		return masterLibrary;
	}

	public void setBasePCDLlibrary(CompoundLibrary basePCDLlibrary) {
		this.masterLibrary = basePCDLlibrary;
		//	compoundVerificationTypeComboBox.setText(basePCDLlibrary.getLibraryName());
	}
	
	public Collection<String>validateLibraryData(){
	    
	    Collection<String>errors = new ArrayList<String>();
	    
	    if(masterLibrary == null || masterLibrary.getFeatures().isEmpty())
	        errors.add("Missing or empty base PCDL library.");
	    
	    String libName = getLibraryName();
	    
	    if(libName.isEmpty())
	        errors.add("New library name can not be empty.");
	    else {
		    CompoundLibrary exitingLibrary = IDTDataCache.getMSRTLibraryByName(libName);
		    if(exitingLibrary != null)
		    	 errors.add("Library \"" + libName + "\" already exists.");
	    }
	    if(getSelectedAdducts().isEmpty())
	        errors.add("Adduct list can not be empty.");
	    	    
	    if(getPolarity() == null)
	    	errors.add("Library polarity not specified.");
	    
	    if(inputLibraryFile == null || !inputLibraryFile.exists())
	    	errors.add("Library file to import not selected.");
	    		    
	    return errors;
	}

	public File getInputLibraryFile() {
		return inputLibraryFile;
	}

	@Override
	public void itemStateChanged(ItemEvent e) {

		if(e.getSource().equals(createDefaultAdductsCheckbox))			
			toggleAdductSelector(!createDefaultAdductsCheckbox.isSelected());
		
		if(e.getStateChange() == ItemEvent.SELECTED && e.getItem() instanceof Polarity) {
			
			boolean isNeutral = getPolarity().equals(Polarity.Neutral);
			neutralPolarityWarningLabel.setVisible(isNeutral);
			
			if(isNeutral)
				adductSelectorPanel.clearAdductList();
		}		
	}

	private void toggleAdductSelector(boolean enabled) {

		adductSelectorPanel.setVisible(enabled);
		adductSubsetLabel.setVisible(!enabled);
		revalidate();
		repaint();		
	}
}

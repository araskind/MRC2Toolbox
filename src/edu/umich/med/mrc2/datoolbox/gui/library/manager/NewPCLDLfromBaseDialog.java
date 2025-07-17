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
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.prefs.Preferences;

import javax.swing.Icon;
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
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import edu.umich.med.mrc2.datoolbox.data.Adduct;
import edu.umich.med.mrc2.datoolbox.data.CompoundLibrary;
import edu.umich.med.mrc2.datoolbox.data.enums.AdductSubset;
import edu.umich.med.mrc2.datoolbox.data.enums.Polarity;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCache;
import edu.umich.med.mrc2.datoolbox.gui.adducts.adduct.AdductSelectorPanel;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.jnafilechooser.api.JnaFileChooser;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;

public class NewPCLDLfromBaseDialog extends JDialog implements ActionListener, BackedByPreferences{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Icon pcdlLibraryIcon = GuiUtils.getIcon("newPCDLfromBase", 32);

	private JTextField nameTextField;
	private JTextArea libraryDescriptionTextArea;
	private JTextField libFileTextField;
		
	private static final String BROWSE = "BROWSE";
	
	private Preferences preferences;
	public static final String BASE_DIRECTORY = "BASE_DIRECTORY";
	private File baseDirectory;
	private JLabel createDefaultAdductsLabel;
	
	private File inputLibraryFile;
	private CompoundLibrary basePCDLlibrary;
	private JTextField pcdlBaseNameField;
	private AdductSelectorPanel adductSelectorPanel;

	public NewPCLDLfromBaseDialog(ActionListener listener) {
		super();
		setSize(new Dimension(500, 500));
		setPreferredSize(new Dimension(500, 500));
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setModalityType(ModalityType.APPLICATION_MODAL);
		getContentPane().setLayout(new BorderLayout(0, 0));

		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(panel, BorderLayout.WEST);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{0, 151, 166, 0, 0};
		gbl_panel.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0};
		gbl_panel.columnWeights = new double[]{1.0, 1.0, 1.0, 1.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);
		
		JLabel lblNewLabel_1 = new JLabel("PCDL base");
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_1.gridx = 0;
		gbc_lblNewLabel_1.gridy = 0;
		panel.add(lblNewLabel_1, gbc_lblNewLabel_1);
		
		pcdlBaseNameField = new JTextField();
		pcdlBaseNameField.setEditable(false);
		GridBagConstraints gbc_pcdlBaseNameField = new GridBagConstraints();
		gbc_pcdlBaseNameField.gridwidth = 3;
		gbc_pcdlBaseNameField.insets = new Insets(0, 0, 5, 0);
		gbc_pcdlBaseNameField.fill = GridBagConstraints.HORIZONTAL;
		gbc_pcdlBaseNameField.gridx = 1;
		gbc_pcdlBaseNameField.gridy = 0;
		panel.add(pcdlBaseNameField, gbc_pcdlBaseNameField);
		pcdlBaseNameField.setColumns(10);

		JLabel nameLabel = new JLabel("Name");
		GridBagConstraints gbc_nameLabel = new GridBagConstraints();
		gbc_nameLabel.insets = new Insets(0, 0, 5, 5);
		gbc_nameLabel.anchor = GridBagConstraints.EAST;
		gbc_nameLabel.gridx = 0;
		gbc_nameLabel.gridy = 1;
		panel.add(nameLabel, gbc_nameLabel);

		nameTextField = new JTextField();
		GridBagConstraints gbc_nameTextField = new GridBagConstraints();
		gbc_nameTextField.gridwidth = 3;
		gbc_nameTextField.insets = new Insets(0, 0, 5, 0);
		gbc_nameTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_nameTextField.gridx = 1;
		gbc_nameTextField.gridy = 1;
		panel.add(nameTextField, gbc_nameTextField);
		nameTextField.setColumns(10);
		
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
		gbc_textArea.gridheight = 2;
		gbc_textArea.gridwidth = 3;
		gbc_textArea.insets = new Insets(0, 0, 5, 0);
		gbc_textArea.fill = GridBagConstraints.BOTH;
		gbc_textArea.gridx = 1;
		gbc_textArea.gridy = 2;
		panel.add(libraryDescriptionTextArea, gbc_textArea);
		
		JLabel idfLabel = new JLabel("Import data from file:");
		GridBagConstraints gbc_idfLabel = new GridBagConstraints();
		gbc_idfLabel.anchor = GridBagConstraints.WEST;
		gbc_idfLabel.gridwidth = 3;
		gbc_idfLabel.insets = new Insets(0, 0, 5, 5);
		gbc_idfLabel.gridx = 0;
		gbc_idfLabel.gridy = 4;
		panel.add(idfLabel, gbc_idfLabel);
		
		libFileTextField = new JTextField();
		libFileTextField.setEditable(false);
		GridBagConstraints gbc_libFileTextField = new GridBagConstraints();
		gbc_libFileTextField.gridwidth = 3;
		gbc_libFileTextField.insets = new Insets(0, 0, 5, 5);
		gbc_libFileTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_libFileTextField.gridx = 0;
		gbc_libFileTextField.gridy = 5;
		panel.add(libFileTextField, gbc_libFileTextField);
		libFileTextField.setColumns(10);
		
		JButton btnBrowse = new JButton("Browse");
		btnBrowse.setActionCommand(BROWSE);
		btnBrowse.addActionListener(this);
		GridBagConstraints gbc_btnNewButton = new GridBagConstraints();
		gbc_btnNewButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnNewButton.insets = new Insets(0, 0, 5, 0);
		gbc_btnNewButton.gridx = 3;
		gbc_btnNewButton.gridy = 5;
		panel.add(btnBrowse, gbc_btnNewButton);
		
		adductSelectorPanel = new AdductSelectorPanel();
		adductSelectorPanel.setBorder(new TitledBorder(null, "Generate adducts during import", 
				TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GridBagConstraints gbc_adductSelectorPanel = new GridBagConstraints();
		gbc_adductSelectorPanel.gridwidth = 4;
		gbc_adductSelectorPanel.insets = new Insets(0, 0, 0, 5);
		gbc_adductSelectorPanel.fill = GridBagConstraints.BOTH;
		gbc_adductSelectorPanel.gridx = 0;
		gbc_adductSelectorPanel.gridy = 6;
		panel.add(adductSelectorPanel, gbc_adductSelectorPanel);
		
		JPanel buttonPanel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) buttonPanel.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);

		JButton btnCancel = new JButton("Cancel");
		buttonPanel.add(btnCancel);
		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				dispose();
			}
		};
		btnCancel.addActionListener(al);

		JButton btnSave = new JButton(
				MainActionCommands.NEW_PCDL_LIBRARY_FROM_PCDL_TEXT_FILE_COMMAND.getName());
		btnSave.setActionCommand(
				MainActionCommands.NEW_PCDL_LIBRARY_FROM_PCDL_TEXT_FILE_COMMAND.getName());
		btnSave.addActionListener(listener);
		buttonPanel.add(btnSave);
		JRootPane rootPane = SwingUtilities.getRootPane(btnSave);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(btnSave);
		
		loadPreferences();
		pack();
	}
		
	@Override
	public void dispose() {
		
		savePreferences();
		super.dispose();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		if(e.getActionCommand().equals(BROWSE))
			selectLibraryFile();
	}
	
	private void selectLibraryFile() {

		JnaFileChooser fc = new JnaFileChooser(baseDirectory);
		fc.setMode(JnaFileChooser.Mode.Files);
		fc.addFilter("PCDL Compound list files (TAB-separated)", "txt", "TXT", "tsv", "TSV");
		fc.setTitle("Select PCDL text export file to import");
		fc.setMultiSelectionEnabled(false);
		if (fc.showOpenDialog(SwingUtilities.getWindowAncestor(this.getContentPane()))) {
			
			inputLibraryFile = fc.getSelectedFile();
			baseDirectory = inputLibraryFile.getParentFile();
			libFileTextField.setText(inputLibraryFile.getAbsolutePath());
			savePreferences();
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
		return basePCDLlibrary;
	}

	public void setBasePCDLlibrary(CompoundLibrary basePCDLlibrary) {
		this.basePCDLlibrary = basePCDLlibrary;
		pcdlBaseNameField.setText(basePCDLlibrary.getLibraryName());
	}
	
	public Collection<String>validateLibraryData(){
	    
	    Collection<String>errors = new ArrayList<String>();
	    
	    if(basePCDLlibrary == null || basePCDLlibrary.getFeatures().isEmpty())
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
}

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

package edu.umich.med.mrc2.datoolbox.gui.idworks.nist.nistms;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.io.FileUtils;

import edu.umich.med.mrc2.datoolbox.gui.idworks.nist.NISTMassErrorType;
import edu.umich.med.mrc2.datoolbox.gui.idworks.nist.NISTPreSearchType;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.fc.ImprovedFileChooser;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;

public class NISTSearchSetupDialog extends JDialog implements ActionListener, BackedByPreferences{

	/**
	 *
	 */
	private static final long serialVersionUID = -8270328541430511802L;

	private static final Icon dialogIcon = GuiUtils.getIcon("NISTMS", 32);

	private JFormattedTextField precursorMzToleranceTextField;
	private JComboBox precursorMzUnitsComboBox;
	private JTextField fragmentMzToleranceTextField;
	private JComboBox fragmentMzUnitsomboBox;
	private JButton searchButton;
	private JCheckBox reverseSearchCheckBox;
	private JCheckBox ionModeCheckBox;
	private JComboBox preSearchTypeComboBox;
	private JPanel panel_1;
	private JPanel panel_2;
	private JTextField mspFileTextField;
	private JButton mspSelectButton;
	private JLabel lblMspFile;
	private JPanel panel_3;
	private JFileChooser fileChooser;
	private File baseDirectory;
	private FileNameExtensionFilter xmlFilter;
	private FileNameExtensionFilter mspFilter;	
	
	private File nistIniFile;
	private File nistIniDestinationFile;
	private File firstLocatorFile;
	private File firstLocatorDestinationFile;
	private File secondLocatorFile;

	private Preferences preferences;
	private static final String SELECT_MSP_FILE = "Select MSP input file";
	public static final String PRECURSOR_MASS_ACCURACY = "PRECURSOR_MASS_ACCURACY";
	public static final String PRECURSOR_MASS_ACCURACY_UNITS = "PRECURSOR_MASS_ACCURACY_UNITS";
	public static final String FRAGMENT_MASS_ACCURACY = "FRAGMENT_MASS_ACCURACY";
	public static final String FRAGMENT_MASS_ACCURACY_UNITS = "FRAGMENT_MASS_ACCURACY_UNITS";
	public static final String PRESEARCH_MODE = "PRESEARCH_MODE";
	public static final String USE_ION_MODE = "USE_ION_MODE";
	public static final String USE_REVERSE_SEARCH = "USE_REVERSE_SEARCH";

	public NISTSearchSetupDialog(ActionListener listener) {

		super(MRC2ToolBoxCore.getMainWindow(), "NIST MS search setup");
		setIconImage(((ImageIcon) dialogIcon).getImage());
		setModalityType(ModalityType.APPLICATION_MODAL);
		setSize(new Dimension(450, 350));
		setPreferredSize(new Dimension(450, 350));
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		panel_3 = new JPanel();
		panel_3.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(panel_3, BorderLayout.CENTER);
		panel_3.setLayout(new BorderLayout(0, 0));

		panel_2 = new JPanel();
		panel_3.add(panel_2, BorderLayout.SOUTH);
		FlowLayout flowLayout = (FlowLayout) panel_2.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);

		panel_1 = new JPanel();
		panel_3.add(panel_1, BorderLayout.NORTH);
		panel_1.setBorder(new TitledBorder(null, "Input source", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GridBagLayout gbl_panel_1 = new GridBagLayout();
		gbl_panel_1.columnWidths = new int[]{0, 0, 0, 0};
		gbl_panel_1.rowHeights = new int[]{0, 0, 0};
		gbl_panel_1.columnWeights = new double[]{0.0, 1.0, 0.0, Double.MIN_VALUE};
		gbl_panel_1.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		panel_1.setLayout(gbl_panel_1);

		lblMspFile = new JLabel("MSP file");
		GridBagConstraints gbc_lblMspFile = new GridBagConstraints();
		gbc_lblMspFile.insets = new Insets(0, 0, 5, 5);
		gbc_lblMspFile.anchor = GridBagConstraints.EAST;
		gbc_lblMspFile.gridx = 0;
		gbc_lblMspFile.gridy = 0;
		panel_1.add(lblMspFile, gbc_lblMspFile);

		mspFileTextField = new JTextField();
		mspFileTextField.setEditable(false);
		GridBagConstraints gbc_textField = new GridBagConstraints();
		gbc_textField.insets = new Insets(0, 0, 5, 5);
		gbc_textField.fill = GridBagConstraints.HORIZONTAL;
		gbc_textField.gridx = 1;
		gbc_textField.gridy = 0;
		panel_1.add(mspFileTextField, gbc_textField);
		mspFileTextField.setColumns(10);

		mspSelectButton = new JButton("Browse");
		mspSelectButton.setActionCommand(SELECT_MSP_FILE);
		mspSelectButton.addActionListener(this);
		GridBagConstraints gbc_btnNewButton = new GridBagConstraints();
		gbc_btnNewButton.insets = new Insets(0, 0, 5, 0);
		gbc_btnNewButton.gridx = 2;
		gbc_btnNewButton.gridy = 0;
		panel_1.add(mspSelectButton, gbc_btnNewButton);

		JPanel panel = new JPanel();
		panel_3.add(panel, BorderLayout.CENTER);
		panel.setBorder(new TitledBorder(null, "NIST search parameters", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[] { 0, 0, 0, 0 };
		gbl_panel.rowHeights = new int[] { 0, 0, 0, 0, 0, 0 };
		gbl_panel.columnWeights = new double[] { 1.0, 1.0, 0.0, Double.MIN_VALUE };
		gbl_panel.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		panel.setLayout(gbl_panel);

		JLabel lblNewLabel = new JLabel("Precursor m/z tolerance");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 0;
		panel.add(lblNewLabel, gbc_lblNewLabel);

		precursorMzToleranceTextField = new JFormattedTextField(MRC2ToolBoxConfiguration.getPpmFormat());
		precursorMzToleranceTextField.setColumns(10);
		GridBagConstraints gbc_precursorMzToleranceTextField = new GridBagConstraints();
		gbc_precursorMzToleranceTextField.insets = new Insets(0, 0, 5, 5);
		gbc_precursorMzToleranceTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_precursorMzToleranceTextField.gridx = 1;
		gbc_precursorMzToleranceTextField.gridy = 0;
		panel.add(precursorMzToleranceTextField, gbc_precursorMzToleranceTextField);

		precursorMzUnitsComboBox = new JComboBox(
				new DefaultComboBoxModel<NISTMassErrorType>(NISTMassErrorType.values()));
		precursorMzUnitsComboBox.setSize(new Dimension(60, 20));
		precursorMzUnitsComboBox.setPreferredSize(new Dimension(60, 20));
		GridBagConstraints gbc_precursorMzUnitsComboBox = new GridBagConstraints();
		gbc_precursorMzUnitsComboBox.anchor = GridBagConstraints.WEST;
		gbc_precursorMzUnitsComboBox.insets = new Insets(0, 0, 5, 0);
		gbc_precursorMzUnitsComboBox.gridx = 2;
		gbc_precursorMzUnitsComboBox.gridy = 0;
		panel.add(precursorMzUnitsComboBox, gbc_precursorMzUnitsComboBox);

		JLabel lblFragmentsMzTolerance = new JLabel("Fragments m/z tolerance");
		GridBagConstraints gbc_lblFragmentsMzTolerance = new GridBagConstraints();
		gbc_lblFragmentsMzTolerance.anchor = GridBagConstraints.EAST;
		gbc_lblFragmentsMzTolerance.insets = new Insets(0, 0, 5, 5);
		gbc_lblFragmentsMzTolerance.gridx = 0;
		gbc_lblFragmentsMzTolerance.gridy = 1;
		panel.add(lblFragmentsMzTolerance, gbc_lblFragmentsMzTolerance);

		fragmentMzToleranceTextField = new JFormattedTextField(MRC2ToolBoxConfiguration.getPpmFormat());
		fragmentMzToleranceTextField.setColumns(10);
		GridBagConstraints gbc_fragmentMzToleranceTextField = new GridBagConstraints();
		gbc_fragmentMzToleranceTextField.insets = new Insets(0, 0, 5, 5);
		gbc_fragmentMzToleranceTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_fragmentMzToleranceTextField.gridx = 1;
		gbc_fragmentMzToleranceTextField.gridy = 1;
		panel.add(fragmentMzToleranceTextField, gbc_fragmentMzToleranceTextField);

		fragmentMzUnitsomboBox = new JComboBox(new DefaultComboBoxModel<NISTMassErrorType>(NISTMassErrorType.values()));
		fragmentMzUnitsomboBox.setPreferredSize(new Dimension(60, 20));
		fragmentMzUnitsomboBox.setSize(new Dimension(60, 20));
		GridBagConstraints gbc_fragmentMzUnitsomboBox = new GridBagConstraints();
		gbc_fragmentMzUnitsomboBox.anchor = GridBagConstraints.WEST;
		gbc_fragmentMzUnitsomboBox.insets = new Insets(0, 0, 5, 0);
		gbc_fragmentMzUnitsomboBox.gridx = 2;
		gbc_fragmentMzUnitsomboBox.gridy = 1;
		panel.add(fragmentMzUnitsomboBox, gbc_fragmentMzUnitsomboBox);

		JLabel lblPresearchType = new JLabel("Presearch type");
		GridBagConstraints gbc_lblPresearchType = new GridBagConstraints();
		gbc_lblPresearchType.anchor = GridBagConstraints.EAST;
		gbc_lblPresearchType.insets = new Insets(0, 0, 5, 5);
		gbc_lblPresearchType.gridx = 0;
		gbc_lblPresearchType.gridy = 2;
		panel.add(lblPresearchType, gbc_lblPresearchType);

		preSearchTypeComboBox = new JComboBox(new DefaultComboBoxModel<NISTPreSearchType>(NISTPreSearchType.values()));
		GridBagConstraints gbc_preSearchTypeComboBox = new GridBagConstraints();
		gbc_preSearchTypeComboBox.insets = new Insets(0, 0, 5, 5);
		gbc_preSearchTypeComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_preSearchTypeComboBox.gridx = 1;
		gbc_preSearchTypeComboBox.gridy = 2;
		panel.add(preSearchTypeComboBox, gbc_preSearchTypeComboBox);

		ionModeCheckBox = new JCheckBox("Match ion mode");
		GridBagConstraints gbc_chckbxNewCheckBox = new GridBagConstraints();
		gbc_chckbxNewCheckBox.anchor = GridBagConstraints.WEST;
		gbc_chckbxNewCheckBox.insets = new Insets(0, 0, 5, 5);
		gbc_chckbxNewCheckBox.gridx = 1;
		gbc_chckbxNewCheckBox.gridy = 3;
		panel.add(ionModeCheckBox, gbc_chckbxNewCheckBox);

		reverseSearchCheckBox = new JCheckBox("Use reverse search");
		GridBagConstraints gbc_chckbxNewCheckBox_1 = new GridBagConstraints();
		gbc_chckbxNewCheckBox_1.anchor = GridBagConstraints.WEST;
		gbc_chckbxNewCheckBox_1.insets = new Insets(0, 0, 0, 5);
		gbc_chckbxNewCheckBox_1.gridx = 1;
		gbc_chckbxNewCheckBox_1.gridy = 4;
		panel.add(reverseSearchCheckBox, gbc_chckbxNewCheckBox_1);

		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				dispose();
			}
		};
		JButton btnCancel = new JButton("Cancel");
		panel_2.add(btnCancel);
		btnCancel.addActionListener(al);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);

		searchButton = new JButton(MainActionCommands.NIST_MS_SEARCH_RUN_COMMAND.getName());
		panel_2.add(searchButton);
		searchButton.setActionCommand(MainActionCommands.NIST_MS_SEARCH_RUN_COMMAND.getName());
		searchButton.addActionListener(listener);
		JRootPane rootPane = SwingUtilities.getRootPane(searchButton);
		rootPane.setDefaultButton(searchButton);

		initStandardFiles();
		initFileChooser();		
		loadPreferences();
		pack();
	}
	
	private void initStandardFiles() {
		
		nistIniFile = Paths.get(
				MRC2ToolBoxCore.configDir, 
				MRC2ToolBoxConfiguration.NIST_CONFIGURATION_FILE).toFile();
		nistIniDestinationFile = Paths.get(
				MRC2ToolBoxConfiguration.getrNistMsDir(), 
				MRC2ToolBoxConfiguration.NIST_CONFIGURATION_FILE).toFile();		
		firstLocatorFile = Paths.get(
				MRC2ToolBoxCore.configDir, 
				MRC2ToolBoxConfiguration.NIST_PRIMARY_LOCATOR_FILE).toFile();
		firstLocatorDestinationFile = Paths.get(
				MRC2ToolBoxConfiguration.getrNistMsDir(), 
				MRC2ToolBoxConfiguration.NIST_PRIMARY_LOCATOR_FILE).toFile();
		secondLocatorFile = Paths.get(
				MRC2ToolBoxCore.configDir, 
				MRC2ToolBoxConfiguration.NIST_SECONDARY_LOCATOR_FILE).toFile();
	}
	
	public Collection<String>validateSearchParameters(){
		
		ArrayList<String>errors = new ArrayList<String>();
		if(getInputFile() == null || !getInputFile().exists()) 
			errors.add("Input file not specified or doesn't exist.");

		if(!nistIniFile.exists())
			errors.add("NIST.INI file is missing!");
		
		return errors;
	}
	
	public boolean createNistIniFile() {
		
		try {
			List<String> iniFileLines = FileUtils.readLines(nistIniFile);
			List<String> iniFileLinesOut = new ArrayList<String>();
			for(String oldLine : iniFileLines) {

				String line = oldLine;

				//	Set precursor mass accuracy
				if(line.startsWith("Precursor Tolerance=")) {

					String precTol = Double.toString(getPrecursorMzTolerance());
					line = "Precursor Tolerance=" + precTol;
				}
				//	Set precursor mass accuracy units
				if(line.startsWith("Precursor Tolerance Units="))
					line = "Precursor Tolerance Units=" + Integer.toString(getPrecursorMzToleranceUnits().getValue());

				//	Set fragment mass accuracy
				if(line.startsWith("Ions Tolerance="))
					line = "Ions Tolerance=" + Double.toString(getFragmentMzTolerance());

				//	Set fragment mass accuracy units
				if(line.startsWith("Ions Tolerance Units="))
					line = "Ions Tolerance Units=" + Integer.toString(getFragmentMzToleranceUnits().getValue());

				//	Set pre-search mode
				if(line.startsWith("Preasearch="))
					line = "Preasearch=" + Integer.toString(getPreSearchMode().getValue());

				//	Set use ion mode
				if(line.startsWith("Ion Mode=")) {

					String ionMode = "0";
					if(getUseIonMode())
						ionMode = "1";

					line = "Ion Mode=" + ionMode;
				}
				//	Set use reverse search
				if(line.startsWith("Reverse=")) {

					String reverse = "0";
					if(getUseReverseSearch())
						reverse = "1";

					line = "Reverse=" + reverse;
				}
				iniFileLinesOut.add(line);
			}
			FileUtils.writeLines(nistIniFile, iniFileLinesOut);
			Files.copy(nistIniFile.toPath(), nistIniDestinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			MessageDialog.showErrorMsg("Can not update NIST.INI file!", this);
			return false;
		}
		return true;
	}
	
	//  Update or create AUTOIMP.MSD file in MSSEARCH directory
	public boolean createAutoimpMsdFile() {		
		try {
			final Writer writer = new BufferedWriter(new FileWriter(firstLocatorFile));
			writer.append(secondLocatorFile.getCanonicalPath());
			writer.flush();
			writer.close();
			Files.copy(firstLocatorFile.toPath(), firstLocatorDestinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			MessageDialog.showErrorMsg(
					"Can not update "+ MRC2ToolBoxConfiguration.NIST_PRIMARY_LOCATOR_FILE + " file!", this);
			return false;
		}
		return true;
	}
	
	//	Create second locator file FILESPEC.FIL in config directory pointing to source file
	public boolean createSecondLocator() {		
		try {
			final Writer writer = new BufferedWriter(new FileWriter(secondLocatorFile));
			writer.append(getInputFile().getCanonicalPath() + " OVERWRITE");
			writer.flush();
			writer.close();
			Files.copy(firstLocatorFile.toPath(), firstLocatorDestinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			MessageDialog.showErrorMsg(
					"Can not update "+ MRC2ToolBoxConfiguration.NIST_SECONDARY_LOCATOR_FILE + " file!", this);
			return false;
		}
		return true;
	}

	private void initFileChooser() {

		fileChooser = new ImprovedFileChooser();
		fileChooser.setBorder(new EmptyBorder(10, 10, 10, 10));
		fileChooser.addActionListener(this);
		fileChooser.setAcceptAllFileFilterUsed(false);
		fileChooser.setMultiSelectionEnabled(false);
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fileChooser.getActionMap().get("viewTypeDetails").actionPerformed(null);
		baseDirectory = new File(MRC2ToolBoxConfiguration.getDefaultProjectsDirectory()).getAbsoluteFile();
		fileChooser.setCurrentDirectory(baseDirectory);

		xmlFilter = new FileNameExtensionFilter("Agilent XML MSMS export files", "xml", "XML");
		fileChooser.addChoosableFileFilter(xmlFilter);
		mspFilter = new FileNameExtensionFilter("NIST MSP MSMS files", "msp", "MSP");
		fileChooser.addChoosableFileFilter(mspFilter);
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		if(e.getActionCommand().equals(SELECT_MSP_FILE)) {

			if(fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
				mspFileTextField.setText(fileChooser.getSelectedFile().getAbsolutePath());
		}
	}

	public File getInputFile() {

		if(mspFileTextField.getText().trim().isEmpty())
			return null;

		File msmsFile = new File(mspFileTextField.getText().trim());
		if(msmsFile.exists())
			return msmsFile;
		else
			return null;
	}

	public double getPrecursorMzTolerance() {
		return Double.parseDouble(precursorMzToleranceTextField.getText());
	}

	public NISTMassErrorType getPrecursorMzToleranceUnits() {
		return (NISTMassErrorType) precursorMzUnitsComboBox.getSelectedItem();
	}

	public double getFragmentMzTolerance() {
		return Double.parseDouble(fragmentMzToleranceTextField.getText());
	}

	public NISTMassErrorType getFragmentMzToleranceUnits() {
		return (NISTMassErrorType) fragmentMzUnitsomboBox.getSelectedItem();
	}

	public NISTPreSearchType getPreSearchMode() {
		return (NISTPreSearchType) preSearchTypeComboBox.getSelectedItem();
	}

	public boolean getUseIonMode() {
		return ionModeCheckBox.isSelected();
	}

	public boolean getUseReverseSearch() {
		return reverseSearchCheckBox.isSelected();
	}

	@Override
	public void dispose() {

		savePreferences();
		super.dispose();
	}

	public void loadPreferences(Preferences prefs) {

		preferences = prefs;

		double precursorMassAccuracy = preferences.getDouble(PRECURSOR_MASS_ACCURACY, 20.0d);
		precursorMzToleranceTextField.setText(Double.toString(precursorMassAccuracy));
		String precursorMassAccuracyUnits = preferences.get(PRECURSOR_MASS_ACCURACY_UNITS, NISTMassErrorType.ppm.name());
		precursorMzUnitsComboBox.setSelectedItem(NISTMassErrorType.getByName(precursorMassAccuracyUnits));

		double fragmentMassAccuracy = preferences.getDouble(FRAGMENT_MASS_ACCURACY, 50.0d);
		fragmentMzToleranceTextField.setText(Double.toString(fragmentMassAccuracy));
		String fragmentMassAccuracyUnits = preferences.get(FRAGMENT_MASS_ACCURACY_UNITS, NISTMassErrorType.ppm.name());
		fragmentMzUnitsomboBox.setSelectedItem(NISTMassErrorType.getByName(fragmentMassAccuracyUnits));

		String presearchMode = preferences.get(PRESEARCH_MODE, NISTPreSearchType.Default.name());
		preSearchTypeComboBox.setSelectedItem(NISTPreSearchType.getByName(presearchMode));

		boolean useIonMode = preferences.getBoolean(USE_ION_MODE, true);
		ionModeCheckBox.setSelected(useIonMode);

		boolean useRevereseSearch = preferences.getBoolean(USE_REVERSE_SEARCH, false);
		reverseSearchCheckBox.setSelected(useRevereseSearch);
	}

	@Override
	public void loadPreferences() {
		loadPreferences(Preferences.userNodeForPackage(this.getClass()));
	}

	public void savePreferences() {

		preferences = Preferences.userNodeForPackage(this.getClass());

		preferences.putDouble(PRECURSOR_MASS_ACCURACY, Double.parseDouble(precursorMzToleranceTextField.getText()));
		preferences.put(PRECURSOR_MASS_ACCURACY_UNITS, ((NISTMassErrorType)precursorMzUnitsComboBox.getSelectedItem()).name());

		preferences.putDouble(FRAGMENT_MASS_ACCURACY, Double.parseDouble(fragmentMzToleranceTextField.getText()));
		preferences.put(FRAGMENT_MASS_ACCURACY_UNITS, ((NISTMassErrorType)fragmentMzUnitsomboBox.getSelectedItem()).name());

		preferences.put(PRESEARCH_MODE, ((NISTPreSearchType)preSearchTypeComboBox.getSelectedItem()).name());

		preferences.putBoolean(USE_ION_MODE, ionModeCheckBox.isSelected());
		preferences.putBoolean(USE_REVERSE_SEARCH, reverseSearchCheckBox.isSelected());
	}
}




























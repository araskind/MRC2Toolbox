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

package edu.umich.med.mrc2.datoolbox.gui.library.upload;

import java.awt.BorderLayout;
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
import java.io.IOException;
import java.util.prefs.Preferences;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
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

import org.apache.commons.io.FilenameUtils;

import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.SortedComboBoxModel;
import edu.umich.med.mrc2.datoolbox.gui.utils.fc.ImprovedFileChooser;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.utils.DelimitedTextParser;

public class LibraryRtImportDialog extends JDialog implements ActionListener, BackedByPreferences{

	/**
	 *
	 */
	private static final long serialVersionUID = -1037730660741004828L;
	private static final Icon importRtIcon = GuiUtils.getIcon("importLibraryRtValues", 32);

	private Preferences preferences;
	public static final String PREFS_NODE = LibraryRtImportDialog.class.getName();
	public static final String BASE_DIRECTORY = "BASE_DIRECTORY";

	private static final String BROWSE_COMMAND = "BROWSE_COMMAND";
	private JButton btnImport;
	private JTextField sourceFileTextField;
	private JComboBox inChIKeyColumnComboBox;
	private JComboBox formulaColumnComboBox;
	private JComboBox idColumnComboBox;
	private JComboBox nameColumnComboBox;
	private JComboBox rtColumnComboBox;
	private JButton browseButton;
	private ImprovedFileChooser chooser;
	private File baseDirectory;
	private String[][] inputData;
	private String[] header;

	public LibraryRtImportDialog(ActionListener actionListener) {

		super();
		setTitle("Import library retention times from file");
		setModalityType(ModalityType.APPLICATION_MODAL);
		setSize(new Dimension(500, 300));
		setPreferredSize(new Dimension(500, 300));
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setIconImage(((ImageIcon) importRtIcon).getImage());

		JPanel contentsPanel = new JPanel(new BorderLayout(0,0));
		contentsPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(contentsPanel, BorderLayout.CENTER);

		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new TitledBorder(null, "Select source data file", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		contentsPanel.add(panel_1, BorderLayout.NORTH);
		GridBagLayout gbl_panel_1 = new GridBagLayout();
		gbl_panel_1.columnWidths = new int[]{0, 0, 0};
		gbl_panel_1.rowHeights = new int[]{0, 0};
		gbl_panel_1.columnWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		gbl_panel_1.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		panel_1.setLayout(gbl_panel_1);

		sourceFileTextField = new JTextField();
		sourceFileTextField.setEditable(false);
		GridBagConstraints gbc_textField = new GridBagConstraints();
		gbc_textField.insets = new Insets(0, 0, 0, 5);
		gbc_textField.fill = GridBagConstraints.HORIZONTAL;
		gbc_textField.gridx = 0;
		gbc_textField.gridy = 0;
		panel_1.add(sourceFileTextField, gbc_textField);
		sourceFileTextField.setColumns(10);

		browseButton = new JButton("Browse");
		browseButton.setActionCommand(BROWSE_COMMAND);
		browseButton.addActionListener(this);
		GridBagConstraints gbc_btnNewButton = new GridBagConstraints();
		gbc_btnNewButton.gridx = 1;
		gbc_btnNewButton.gridy = 0;
		panel_1.add(browseButton, gbc_btnNewButton);

		JPanel dataPanel = new JPanel();
		dataPanel.setBorder(new TitledBorder(null, "Select data columns", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		contentsPanel.add(dataPanel, BorderLayout.CENTER);
		GridBagLayout gbl_dataPanel = new GridBagLayout();
		gbl_dataPanel.columnWidths = new int[]{0, 0, 0};
		gbl_dataPanel.rowHeights = new int[]{0, 0, 0, 0, 0, 0};
		gbl_dataPanel.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gbl_dataPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		dataPanel.setLayout(gbl_dataPanel);

		JLabel lblRetentionTime = new JLabel("Retention time");
		lblRetentionTime.setFont(new Font("Tahoma", Font.BOLD, 12));
		GridBagConstraints gbc_lblRetentionTime = new GridBagConstraints();
		gbc_lblRetentionTime.insets = new Insets(0, 0, 5, 5);
		gbc_lblRetentionTime.anchor = GridBagConstraints.EAST;
		gbc_lblRetentionTime.gridx = 0;
		gbc_lblRetentionTime.gridy = 0;
		dataPanel.add(lblRetentionTime, gbc_lblRetentionTime);

		rtColumnComboBox = new JComboBox();
		GridBagConstraints gbc_rtColumnComboBox = new GridBagConstraints();
		gbc_rtColumnComboBox.insets = new Insets(0, 0, 5, 0);
		gbc_rtColumnComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_rtColumnComboBox.gridx = 1;
		gbc_rtColumnComboBox.gridy = 0;
		dataPanel.add(rtColumnComboBox, gbc_rtColumnComboBox);

		JLabel lblName = new JLabel("Name");
		GridBagConstraints gbc_lblName = new GridBagConstraints();
		gbc_lblName.anchor = GridBagConstraints.EAST;
		gbc_lblName.insets = new Insets(0, 0, 5, 5);
		gbc_lblName.gridx = 0;
		gbc_lblName.gridy = 1;
		dataPanel.add(lblName, gbc_lblName);

		nameColumnComboBox = new JComboBox();
		GridBagConstraints gbc_nameColumnComboBox = new GridBagConstraints();
		gbc_nameColumnComboBox.insets = new Insets(0, 0, 5, 0);
		gbc_nameColumnComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_nameColumnComboBox.gridx = 1;
		gbc_nameColumnComboBox.gridy = 1;
		dataPanel.add(nameColumnComboBox, gbc_nameColumnComboBox);

		JLabel lblCompoundId = new JLabel("Compound ID");
		GridBagConstraints gbc_lblCompoundId = new GridBagConstraints();
		gbc_lblCompoundId.anchor = GridBagConstraints.EAST;
		gbc_lblCompoundId.insets = new Insets(0, 0, 5, 5);
		gbc_lblCompoundId.gridx = 0;
		gbc_lblCompoundId.gridy = 2;
		dataPanel.add(lblCompoundId, gbc_lblCompoundId);

		idColumnComboBox = new JComboBox();
		GridBagConstraints gbc_idColumnComboBox = new GridBagConstraints();
		gbc_idColumnComboBox.insets = new Insets(0, 0, 5, 0);
		gbc_idColumnComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_idColumnComboBox.gridx = 1;
		gbc_idColumnComboBox.gridy = 2;
		dataPanel.add(idColumnComboBox, gbc_idColumnComboBox);

		JLabel lblFormula = new JLabel("Formula");
		GridBagConstraints gbc_lblFormula = new GridBagConstraints();
		gbc_lblFormula.anchor = GridBagConstraints.EAST;
		gbc_lblFormula.insets = new Insets(0, 0, 5, 5);
		gbc_lblFormula.gridx = 0;
		gbc_lblFormula.gridy = 3;
		dataPanel.add(lblFormula, gbc_lblFormula);

		formulaColumnComboBox = new JComboBox();
		GridBagConstraints gbc_formulaColumnComboBox = new GridBagConstraints();
		gbc_formulaColumnComboBox.insets = new Insets(0, 0, 5, 0);
		gbc_formulaColumnComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_formulaColumnComboBox.gridx = 1;
		gbc_formulaColumnComboBox.gridy = 3;
		dataPanel.add(formulaColumnComboBox, gbc_formulaColumnComboBox);

		JLabel lblInc = new JLabel("InChIKey");
		GridBagConstraints gbc_lblInc = new GridBagConstraints();
		gbc_lblInc.anchor = GridBagConstraints.EAST;
		gbc_lblInc.insets = new Insets(0, 0, 0, 5);
		gbc_lblInc.gridx = 0;
		gbc_lblInc.gridy = 4;
		dataPanel.add(lblInc, gbc_lblInc);

		inChIKeyColumnComboBox = new JComboBox();
		GridBagConstraints gbc_InChIKeyColumnComboBox = new GridBagConstraints();
		gbc_InChIKeyColumnComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_InChIKeyColumnComboBox.gridx = 1;
		gbc_InChIKeyColumnComboBox.gridy = 4;
		dataPanel.add(inChIKeyColumnComboBox, gbc_InChIKeyColumnComboBox);


		JPanel panel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		getContentPane().add(panel, BorderLayout.SOUTH);

		JButton btnCancel = new JButton("Cancel");
		panel.add(btnCancel);
		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				savePreferences();
				dispose();
			}
		};
		btnCancel.addActionListener(al);

		btnImport = new JButton("Import RT values");
		btnImport.setActionCommand(MainActionCommands.IMPORT_LIBRARY_FEATURE_RT_COMMAND.getName());
		btnImport.addActionListener(actionListener);
		panel.add(btnImport);
		JRootPane rootPane = SwingUtilities.getRootPane(btnImport);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(btnImport);

		loadPreferences();
		initChooser();
		pack();
	}

	private void initChooser() {

		chooser = new ImprovedFileChooser();
		chooser.setBorder(new EmptyBorder(10, 10, 10, 10));
		chooser.addActionListener(this);
		chooser.setAcceptAllFileFilterUsed(false);
		chooser.setMultiSelectionEnabled(false);
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setCurrentDirectory(baseDirectory);
		chooser.addChoosableFileFilter(new FileNameExtensionFilter("Text files", "txt", "TXT", "csv", "CSV"));
		chooser.addChoosableFileFilter(new FileNameExtensionFilter("Excel files", "xlsx"));
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		if(e.getActionCommand().equals(BROWSE_COMMAND))
			chooser.showOpenDialog(this);

		if(e.getSource().equals(chooser) && e.getActionCommand().equals(JFileChooser.APPROVE_SELECTION)) {

			File inputFile = chooser.getSelectedFile();
			baseDirectory = inputFile.getParentFile();
			sourceFileTextField.setText(inputFile.getAbsolutePath());
			savePreferences();

			//	Parse file and populate comboboxes
			if(FilenameUtils.getExtension(inputFile.getName()).equalsIgnoreCase("xlsx"))
				parseInputExcelFile(inputFile);

			if(FilenameUtils.getExtension(inputFile.getName()).equalsIgnoreCase("txt"))
				parseInputTextFile(inputFile, MRC2ToolBoxConfiguration.getTabDelimiter());

			if(FilenameUtils.getExtension(inputFile.getName()).equalsIgnoreCase("csv"))
				parseInputTextFile(inputFile, ',');
		}
	}

	private void parseInputTextFile(File inputFile, char delimiter) {

		inputData = null;
		try {
			inputData = DelimitedTextParser.parseTextFileWithEncoding(inputFile, delimiter);
		} catch (IOException e) {
			MessageDialog.showErrorMsg("File parsing error", this);
			e.printStackTrace();
			return;
		}
		if(inputData.length > 1) {
			header = inputData[0];
			populateColumnSelectors(header);
		}
	}

	private void parseInputExcelFile(File inputFile) {
		// TODO Auto-generated method stub

	}

	private void populateColumnSelectors(String[] columnNames) {

		inChIKeyColumnComboBox.setModel(new SortedComboBoxModel<String>(columnNames));
		formulaColumnComboBox.setModel(new SortedComboBoxModel<String>(columnNames));
		idColumnComboBox.setModel(new SortedComboBoxModel<String>(columnNames));
		nameColumnComboBox.setModel(new SortedComboBoxModel<String>(columnNames));
		rtColumnComboBox.setModel(new SortedComboBoxModel<String>(columnNames));

		inChIKeyColumnComboBox.setSelectedIndex(-1);
		formulaColumnComboBox.setSelectedIndex(-1);
		idColumnComboBox.setSelectedIndex(-1);
		nameColumnComboBox.setSelectedIndex(-1);
		rtColumnComboBox.setSelectedIndex(-1);
	}

	public int getRtColumnIndex() {

		if(rtColumnComboBox.getSelectedIndex() == -1)
			return -1;

		for(int i=0; i<header.length; i++) {
			if(header[i].equals((String) rtColumnComboBox.getSelectedItem()))
				return i;
		}
		return -1;
	}

	public int getNameColumnIndex() {

		if(nameColumnComboBox.getSelectedIndex() == -1)
			return -1;

		for(int i=0; i<header.length; i++) {
			if(header[i].equals((String) nameColumnComboBox.getSelectedItem()))
				return i;
		}
		return -1;
	}

	public int getCompoundIdColumnIndex() {

		if(idColumnComboBox.getSelectedIndex() == -1)
			return -1;

		for(int i=0; i<header.length; i++) {
			if(header[i].equals((String) idColumnComboBox.getSelectedItem()))
				return i;
		}
		return -1;
	}

	public int getFormulaColumnIndex() {

		if(formulaColumnComboBox.getSelectedIndex() == -1)
			return -1;

		for(int i=0; i<header.length; i++) {
			if(header[i].equals((String) formulaColumnComboBox.getSelectedItem()))
				return i;
		}
		return -1;
	}

	public int getinChIKeyColumnIndex() {

		if(inChIKeyColumnComboBox.getSelectedIndex() == -1)
			return -1;

		for(int i=0; i<header.length; i++) {
			if(header[i].equals((String) inChIKeyColumnComboBox.getSelectedItem()))
				return i;
		}
		return -1;
	}

	@Override
	public void loadPreferences(Preferences prefs) {
		preferences = prefs;
		loadPreferences();
	}

	@Override
	public void loadPreferences() {
		preferences = Preferences.userRoot().node(PREFS_NODE);
		baseDirectory =  new File(preferences.get(BASE_DIRECTORY, MRC2ToolBoxConfiguration.getDefaultDataDirectory()));
	}

	@Override
	public void savePreferences() {
		preferences = Preferences.userRoot().node(PREFS_NODE);
		preferences.put(BASE_DIRECTORY, baseDirectory.getAbsolutePath());
	}

	public File getSourceFile() {

		if(sourceFileTextField.getText().trim().isEmpty())
			return null;

		return new File(sourceFileTextField.getText().trim());
	}

	public String[][] getInputData() {
		return inputData;
	}
}










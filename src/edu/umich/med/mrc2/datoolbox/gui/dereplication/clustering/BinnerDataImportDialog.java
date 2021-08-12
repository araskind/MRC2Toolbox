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

package edu.umich.med.mrc2.datoolbox.gui.dereplication.clustering;

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
import java.util.prefs.Preferences;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
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
import javax.swing.filechooser.FileNameExtensionFilter;

import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.fc.ImprovedFileChooser;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;

public class BinnerDataImportDialog extends JDialog implements BackedByPreferences, ActionListener{

	/**
	 *
	 */
	private static final long serialVersionUID = -2641420837255688552L;
	private static final Icon addMethodIcon = GuiUtils.getIcon("importBins", 32);

	private Preferences preferences;
	private File baseDirectory;
	private ImprovedFileChooser chooser;
	private JButton btnImport;
	public static final String PREFS_NODE = "edu.umich.med.mrc2.cefanalyzer.gui.dereplication.BinnerDataImportDialog";
	public static final String BASE_DIRECTORY = "BASE_DIRECTORY";
	private JTextField binnerReportTextField;
	private JTextField postProcessorReportTextField;
	private JButton postProccessorBrowseButton;
	private JButton binnerReportBrowseButton;
	private String chooserFileType;

	public BinnerDataImportDialog(ActionListener actionListener) {
		super();
		setTitle("Select Binner report files");
		setIconImage(((ImageIcon) addMethodIcon).getImage());
		setPreferredSize(new Dimension(600, 200));
		setSize(new Dimension(600, 200));
		setModalityType(ModalityType.APPLICATION_MODAL);
		setResizable(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(panel_1, BorderLayout.CENTER);
		GridBagLayout gbl_panel_1 = new GridBagLayout();
		gbl_panel_1.columnWidths = new int[]{0, 0, 0};
		gbl_panel_1.rowHeights = new int[]{0, 0, 0, 0, 0};
		gbl_panel_1.columnWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		gbl_panel_1.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		panel_1.setLayout(gbl_panel_1);

		JLabel lblBinnerReportFile = new JLabel("Binner report file");
		GridBagConstraints gbc_lblBinnerReportFile = new GridBagConstraints();
		gbc_lblBinnerReportFile.anchor = GridBagConstraints.WEST;
		gbc_lblBinnerReportFile.insets = new Insets(0, 0, 5, 5);
		gbc_lblBinnerReportFile.gridx = 0;
		gbc_lblBinnerReportFile.gridy = 0;
		panel_1.add(lblBinnerReportFile, gbc_lblBinnerReportFile);

		binnerReportTextField = new JTextField();
		GridBagConstraints gbc_binnerReportTextField = new GridBagConstraints();
		gbc_binnerReportTextField.insets = new Insets(0, 0, 5, 5);
		gbc_binnerReportTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_binnerReportTextField.gridx = 0;
		gbc_binnerReportTextField.gridy = 1;
		panel_1.add(binnerReportTextField, gbc_binnerReportTextField);
		binnerReportTextField.setColumns(10);

		binnerReportBrowseButton = new JButton("Browse");
		binnerReportBrowseButton.setActionCommand(
				MainActionCommands.BROWSE_FOR_BINNER_REPORT_COMMAND.getName());
		binnerReportBrowseButton.addActionListener(this);
		GridBagConstraints gbc_btnBrowse = new GridBagConstraints();
		gbc_btnBrowse.insets = new Insets(0, 0, 5, 0);
		gbc_btnBrowse.gridx = 1;
		gbc_btnBrowse.gridy = 1;
		panel_1.add(binnerReportBrowseButton, gbc_btnBrowse);

		JLabel lblBinnerPostprocessorReport = new JLabel("Binner Postprocessor report file");
		GridBagConstraints gbc_lblBinnerPostprocessorReport = new GridBagConstraints();
		gbc_lblBinnerPostprocessorReport.anchor = GridBagConstraints.WEST;
		gbc_lblBinnerPostprocessorReport.insets = new Insets(0, 0, 5, 5);
		gbc_lblBinnerPostprocessorReport.gridx = 0;
		gbc_lblBinnerPostprocessorReport.gridy = 2;
		panel_1.add(lblBinnerPostprocessorReport, gbc_lblBinnerPostprocessorReport);

		postProcessorReportTextField = new JTextField();
		postProcessorReportTextField.setColumns(10);
		GridBagConstraints gbc_textField = new GridBagConstraints();
		gbc_textField.insets = new Insets(0, 0, 0, 5);
		gbc_textField.fill = GridBagConstraints.HORIZONTAL;
		gbc_textField.gridx = 0;
		gbc_textField.gridy = 3;
		panel_1.add(postProcessorReportTextField, gbc_textField);

		postProccessorBrowseButton = new JButton("Browse");
		postProccessorBrowseButton.setActionCommand(
				MainActionCommands.BROWSE_FOR_BINNER_POSTPROCESSOR_REPORT_COMMAND.getName());
		postProccessorBrowseButton.addActionListener(this);
		GridBagConstraints gbc_button = new GridBagConstraints();
		gbc_button.gridx = 1;
		gbc_button.gridy = 3;
		panel_1.add(postProccessorBrowseButton, gbc_button);

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

		btnImport = new JButton("Import Binner results");
		btnImport.setActionCommand(MainActionCommands.IMPORT_BINNER_DATA_COMMAND.getName());
		btnImport.addActionListener(actionListener);
		panel.add(btnImport);
		JRootPane rootPane = SwingUtilities.getRootPane(btnImport);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(btnImport);
		
		loadPreferences();
		initChooser();
		pack();
	}

	@Override
	public void dispose() {
		savePreferences();
		super.dispose();
	}	
	
	private void initChooser() {

		chooser = new ImprovedFileChooser();
		chooser.setBorder(new EmptyBorder(10, 10, 10, 10));
		chooser.addActionListener(this);
		chooser.setAcceptAllFileFilterUsed(false);
		chooser.setMultiSelectionEnabled(false);
		chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		chooser.setCurrentDirectory(baseDirectory);
		chooser.setFileFilter(new FileNameExtensionFilter("Excel files", "xlsx"));
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		if(e.getActionCommand().equals(MainActionCommands.BROWSE_FOR_BINNER_REPORT_COMMAND.getName())) {

			chooserFileType = MainActionCommands.BROWSE_FOR_BINNER_REPORT_COMMAND.getName();
			chooser.setSelectedFile(null);
			chooser.rescanCurrentDirectory();
			chooser.showOpenDialog(this);
		}
		if(e.getActionCommand().equals(MainActionCommands.BROWSE_FOR_BINNER_POSTPROCESSOR_REPORT_COMMAND.getName())) {

			chooserFileType = MainActionCommands.BROWSE_FOR_BINNER_POSTPROCESSOR_REPORT_COMMAND.getName();
			chooser.setSelectedFile(null);
			chooser.rescanCurrentDirectory();
			chooser.showOpenDialog(this);
		}
		if(e.getSource().equals(chooser) && e.getActionCommand().equals(JFileChooser.APPROVE_SELECTION)) {

			File inputFile = chooser.getSelectedFile();
			baseDirectory = inputFile.getParentFile();
			savePreferences();

			if(chooserFileType.equals(MainActionCommands.BROWSE_FOR_BINNER_REPORT_COMMAND.getName()))
				binnerReportTextField.setText(inputFile.getAbsolutePath());

			if(chooserFileType.equals(MainActionCommands.BROWSE_FOR_BINNER_POSTPROCESSOR_REPORT_COMMAND.getName()))
				postProcessorReportTextField.setText(inputFile.getAbsolutePath());
		}
	}

	public File getBinnerReportFile() {

		if(binnerReportTextField.getText().trim().isEmpty())
			return null;

		return Paths.get(binnerReportTextField.getText()).toFile();
	}

	public File getPostProcessorReportFile() {

		if(postProcessorReportTextField.getText().trim().isEmpty())
			return null;

		return Paths.get(postProcessorReportTextField.getText()).toFile();
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


}

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

package edu.umich.med.mrc2.datoolbox.gui.io.txt;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
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
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.projectsetup.dpl.DataPipelineDefinitionPanel;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.fc.ImprovedFileChooser;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;

public class TextDataImportDialog extends JDialog
		implements ActionListener, BackedByPreferences{

	/**
	 *
	 */
	private static final long serialVersionUID = -1705304754522545102L;

	private static final Icon importTextFileIcon = GuiUtils.getIcon("importTextfile", 32);

	private Preferences preferences;
	private File baseLibraryDirectory;
	private File dataFileDirectory;
	private JTextField libraryTextField;
	private JButton importDataButton;
	private ImprovedFileChooser chooser;
	private FileNameExtensionFilter txtFilter;
	private FileNameExtensionFilter xmlFilter;
	private FileNameExtensionFilter mgfFilter;
	private TextFileImportToolbar toolbar;
	private String currentTask;
	private File libraryFile;
	private DataPipelineDefinitionPanel dataPipelineDefinitionPanel;

	public static final String PREFS_NODE = TextDataImportDialog.class.getName();
	public static final String BASE_LIBRARY_DIRECTORY = "BASE_LIBRARY_DIRECTORY";
	public static final String BASE_DATA_FILES_DIRECTORY = "BASE_DATA_FILES_DIRECTORY";
	private ColumnFieldMatchTable table;

	public TextDataImportDialog() {

		super();
		setTitle("Import data from multiple files");
		setIconImage(((ImageIcon) importTextFileIcon).getImage());
		setPreferredSize(new Dimension(800, 640));
		setModalityType(ModalityType.APPLICATION_MODAL);
		setSize(new Dimension(800, 480));
		setResizable(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		toolbar = new TextFileImportToolbar(this);
		getContentPane().add(toolbar, BorderLayout.NORTH);

		JPanel main = new JPanel(new BorderLayout(0, 0));
		main.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(main, BorderLayout.CENTER);

		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new EmptyBorder(10, 10, 10, 10));
		main.add(panel_1, BorderLayout.SOUTH);
		GridBagLayout gbl_panel_1 = new GridBagLayout();
		gbl_panel_1.columnWidths = new int[]{81, 290, 65, 89, 0};
		gbl_panel_1.rowHeights = new int[]{0, 23, 0};
		gbl_panel_1.columnWeights = new double[]{1.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		gbl_panel_1.rowWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		panel_1.setLayout(gbl_panel_1);
		
		JPanel panel_2 = new JPanel();
		main.add(panel_2, BorderLayout.CENTER);
		panel_2.setLayout(new BorderLayout(0, 0));

		JPanel libChooserPanel = new JPanel();
		libChooserPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		GridBagLayout gbl_libChooserPanel = new GridBagLayout();
		gbl_libChooserPanel.columnWidths = new int[]{0, 0, 0};
		gbl_libChooserPanel.rowHeights = new int[]{0, 0};
		gbl_libChooserPanel.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gbl_libChooserPanel.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		libChooserPanel.setLayout(gbl_libChooserPanel);

		JLabel libFileLabel = new JLabel("Library: ");
		GridBagConstraints gbc_libFileLabel = new GridBagConstraints();
		gbc_libFileLabel.anchor = GridBagConstraints.EAST;
		gbc_libFileLabel.insets = new Insets(0, 0, 0, 5);
		gbc_libFileLabel.gridx = 0;
		gbc_libFileLabel.gridy = 0;
		libChooserPanel.add(libFileLabel, gbc_libFileLabel);

		libraryTextField = new JTextField();
		libraryTextField.setEditable(false);
		GridBagConstraints gbc_textField = new GridBagConstraints();
		gbc_textField.fill = GridBagConstraints.HORIZONTAL;
		gbc_textField.gridx = 1;
		gbc_textField.gridy = 0;
		libChooserPanel.add(libraryTextField, gbc_textField);
		libraryTextField.setColumns(10);

		panel_2.add(libChooserPanel, BorderLayout.NORTH);

		table = new ColumnFieldMatchTable();
		panel_2.add(new JScrollPane(table), BorderLayout.CENTER);

		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {

//				for(DataFile df : matchPanel.getDataFiles()) {
//
//					if(df.getParentSample() != null)
//						df.getParentSample().removeDataFile(df);
//				}
				dispose();
			}
		};		
		dataPipelineDefinitionPanel = new DataPipelineDefinitionPanel();
		dataPipelineDefinitionPanel.setBorder(
				new CompoundBorder(new EmptyBorder(10, 0, 0, 0), 
					new TitledBorder(UIManager.getBorder("TitledBorder.border"),
						"Define new data pipeline", TitledBorder.LEADING, 
						TitledBorder.TOP, null, new Color(0, 0, 0))));
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.gridwidth = 4;
		gbc_panel.insets = new Insets(0, 0, 5, 5);
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 0;
		panel_1.add(dataPipelineDefinitionPanel, gbc_panel);
		
		JButton cancelButton = new JButton("Cancel");
		GridBagConstraints gbc_cancelButton = new GridBagConstraints();
		gbc_cancelButton.anchor = GridBagConstraints.NORTHWEST;
		gbc_cancelButton.insets = new Insets(0, 0, 0, 5);
		gbc_cancelButton.gridx = 2;
		gbc_cancelButton.gridy = 1;
		panel_1.add(cancelButton, gbc_cancelButton);
		cancelButton.addActionListener(al);

		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);

		importDataButton = new JButton("Import data");
		importDataButton.setActionCommand(MainActionCommands.IMPORT_DATA_COMMAND.getName());
		importDataButton.addActionListener(this);
		GridBagConstraints gbc_importDataButton = new GridBagConstraints();
		gbc_importDataButton.anchor = GridBagConstraints.NORTHEAST;
		gbc_importDataButton.gridx = 3;
		gbc_importDataButton.gridy = 1;
		panel_1.add(importDataButton, gbc_importDataButton);
		JRootPane rootPane = SwingUtilities.getRootPane(importDataButton);
		rootPane.setDefaultButton(importDataButton);

		loadPreferences();
		initChooser();
		pack();
	}

	private void initChooser() {

		chooser = new ImprovedFileChooser();
		chooser.setBorder(new EmptyBorder(10, 10, 10, 10));
		chooser.addActionListener(this);
		chooser.setAcceptAllFileFilterUsed(false);
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setCurrentDirectory(baseLibraryDirectory);
		chooser.setMultiSelectionEnabled(false);

		txtFilter = new FileNameExtensionFilter("Text files", "txt", "tsv");
		xmlFilter = new FileNameExtensionFilter("XML files", "xml", "cef", "CEF");
		mgfFilter = new FileNameExtensionFilter("MGF files", "mgf");
	}

	@Override
	public void loadPreferences(Preferences prefs) {
		preferences = prefs;
		baseLibraryDirectory =  
				new File(preferences.get(BASE_LIBRARY_DIRECTORY, 
						MRC2ToolBoxConfiguration.getDefaultDataDirectory()));
		dataFileDirectory =  
				new File(preferences.get(BASE_DATA_FILES_DIRECTORY, 
						MRC2ToolBoxConfiguration.getDefaultDataDirectory()));
	}

	@Override
	public void loadPreferences() {
		loadPreferences(Preferences.userRoot().node(PREFS_NODE));
	}

	@Override
	public void savePreferences() {
		preferences = Preferences.userRoot().node(PREFS_NODE);
		preferences.put(BASE_LIBRARY_DIRECTORY, 
				baseLibraryDirectory.getAbsolutePath());
		preferences.put(BASE_DATA_FILES_DIRECTORY, 
				dataFileDirectory.getAbsolutePath());
	}

	@Override
	public void actionPerformed(ActionEvent event) {

		if(!event.getSource().equals(chooser))
			currentTask = event.getActionCommand();

		if (event.getSource().equals(chooser) && event.getActionCommand().equals(JFileChooser.APPROVE_SELECTION)) {

			if(currentTask.equals(MainActionCommands.SELECT_INPUT_LIBRARY_COMMAND.getName()))
				addSelectedLibraryFile();
		
			if(currentTask.equals(MainActionCommands.ADD_DATA_FILES_COMMAND.getName()))
				addSelectedDataFile();
		}
		if (event.getActionCommand().equals(MainActionCommands.SELECT_INPUT_LIBRARY_COMMAND.getName()))
			selectLibraryFile();

		if (event.getActionCommand().equals(MainActionCommands.ADD_DATA_FILES_COMMAND.getName()))
			selectDataFile();

		if (event.getActionCommand().equals(MainActionCommands.IMPORT_DATA_COMMAND.getName()))
			importData();

		if (currentTask.equals(MainActionCommands.CLEAR_DATA_COMMAND.getName())) {

			if (MessageDialog.showChoiceMsg("Clear input data?", this) == JOptionPane.YES_OPTION)
				clearPanel();
		}
	}

	private void addSelectedLibraryFile() {
		libraryFile = chooser.getSelectedFile();
		libraryTextField.setText(libraryFile.getPath());
		baseLibraryDirectory = libraryFile.getParentFile();
		savePreferences();
	}

	private void addSelectedDataFile() {
		DataPipeline pipeline = dataPipelineDefinitionPanel.getDataPipeline();
		if(pipeline == null)
			return;

		if(MRC2ToolBoxCore.getCurrentProject().getDataPipelines().contains(pipeline)) {
			MessageDialog.showErrorMsg("The project already contains data pipeline \n"
					+ "with selected combination of assay, data acquisition and data analysis methods."
					+ "Please adjust you selection.\nIf you want to replace the existing data\n"
					+ "please delete them first and then re-upload.", 
					this);
			return;
		}	
		File dataFile = chooser.getSelectedFile();
		dataFileDirectory = dataFile.getParentFile();
		savePreferences();
	}

	public void clearPanel() {

		libraryFile = null;
		libraryTextField.setText("");
	}

	private void importData() {
		// TODO Auto-generated method stub

	}

	private void selectDataFile() {

		chooser.resetChoosableFileFilters();
		chooser.setFileFilter(txtFilter);
		chooser.setCurrentDirectory(dataFileDirectory);
		chooser.rescanCurrentDirectory();
		chooser.showOpenDialog(this);
	}

	private void selectLibraryFile() {

		chooser.resetChoosableFileFilters();
		chooser.setFileFilter(xmlFilter);
		chooser.setCurrentDirectory(baseLibraryDirectory);
		chooser.rescanCurrentDirectory();
		chooser.showOpenDialog(this);
	}
}

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

package edu.umich.med.mrc2.datoolbox.gui.mptrack.upload;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import edu.umich.med.mrc2.datoolbox.data.motrpac.MoTrPACAssay;
import edu.umich.med.mrc2.datoolbox.gui.automator.FilesTable;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.jnafilechooser.api.JnaFileChooser;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.mp.AgilentDataCompressionTask;

public class CompressionSetupPanel extends JPanel implements ActionListener{

	/**
	 * 
	 */
	private static final long serialVersionUID = 2020736082862742526L;
	
	private MoTrPACAssay assay;
	private File baseDirectory;
	private File destinationDir;
	private File inputFileList;
	private JTextField fileListPathField;
	private JTextField destinationPathTextField;
	private FilesTable table;
	JDialog parent;
	
	public CompressionSetupPanel(MoTrPACAssay assay, JDialog parent) {
		super();
		setBorder(new EmptyBorder(10, 10, 10, 10));
		this.assay = assay;
		this.parent = parent;
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 1.0, 0.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 1.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		fileListPathField = new JTextField();
		fileListPathField.setEditable(false);
		GridBagConstraints gbc_fileListPathField = new GridBagConstraints();
		gbc_fileListPathField.gridwidth = 2;
		gbc_fileListPathField.insets = new Insets(0, 0, 5, 5);
		gbc_fileListPathField.fill = GridBagConstraints.HORIZONTAL;
		gbc_fileListPathField.gridx = 0;
		gbc_fileListPathField.gridy = 0;
		add(fileListPathField, gbc_fileListPathField);
		fileListPathField.setColumns(10);
		
		JButton selectFileListButton = new JButton(
				MainActionCommands.SELECT_FILE_LIST_FOR_COMPRESSION_COMMAND.getName());
		selectFileListButton.setActionCommand(
				MainActionCommands.SELECT_FILE_LIST_FOR_COMPRESSION_COMMAND.getName());
		selectFileListButton.addActionListener(this);
		
		GridBagConstraints gbc_btnNewButton = new GridBagConstraints();
		gbc_btnNewButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnNewButton.insets = new Insets(0, 0, 5, 0);
		gbc_btnNewButton.gridx = 2;
		gbc_btnNewButton.gridy = 0;
		add(selectFileListButton, gbc_btnNewButton);
		
		JLabel lblNewLabel_1 = new JLabel("Raw data directories");
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel_1.gridwidth = 2;
		gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_1.gridx = 0;
		gbc_lblNewLabel_1.gridy = 1;
		add(lblNewLabel_1, gbc_lblNewLabel_1);
		
		table = new FilesTable();
		JScrollPane scrollPane = new JScrollPane(table);
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane.gridwidth = 3;
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 2;
		add(scrollPane, gbc_scrollPane);
		
		JButton selectRawDirsButton = new JButton(
				MainActionCommands.SELECT_RAW_DATA_DIRECTORIES_COMMAND.getName());
		selectRawDirsButton.setActionCommand(
				MainActionCommands.SELECT_RAW_DATA_DIRECTORIES_COMMAND.getName());
		selectRawDirsButton.addActionListener(this);
		GridBagConstraints gbc_btnSelectDirsButton = new GridBagConstraints();
		gbc_btnSelectDirsButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnSelectDirsButton.insets = new Insets(0, 0, 5, 0);
		gbc_btnSelectDirsButton.gridx = 2;
		gbc_btnSelectDirsButton.gridy = 3;
		add(selectRawDirsButton, gbc_btnSelectDirsButton);
		
		JLabel lblNewLabel = new JLabel("   ");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.gridx = 1;
		gbc_lblNewLabel.gridy = 4;
		add(lblNewLabel, gbc_lblNewLabel);
		
		destinationPathTextField = new JTextField();
		destinationPathTextField.setEditable(false);
		GridBagConstraints gbc_destinationPathTextField = new GridBagConstraints();
		gbc_destinationPathTextField.gridwidth = 2;
		gbc_destinationPathTextField.insets = new Insets(0, 0, 0, 5);
		gbc_destinationPathTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_destinationPathTextField.gridx = 0;
		gbc_destinationPathTextField.gridy = 5;
		add(destinationPathTextField, gbc_destinationPathTextField);
		destinationPathTextField.setColumns(10);
		
		JButton selectDestinationButton = new JButton(
				MainActionCommands.SELECT_DESTINATION_FOR_COMPRESSED_FILES_COMMAND.getName());
		selectDestinationButton.setActionCommand(
				MainActionCommands.SELECT_DESTINATION_FOR_COMPRESSED_FILES_COMMAND.getName());
		selectDestinationButton.addActionListener(this);
		
		GridBagConstraints gbc_btnNewButton_1 = new GridBagConstraints();
		gbc_btnNewButton_1.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnNewButton_1.gridx = 2;
		gbc_btnNewButton_1.gridy = 5;
		add(selectDestinationButton, gbc_btnNewButton_1);
		baseDirectory = new File(MRC2ToolBoxConfiguration.getDefaultDataDirectory());
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		String command = e.getActionCommand();
		
		if(command.equals(MainActionCommands.SELECT_FILE_LIST_FOR_COMPRESSION_COMMAND.getName()))
			selectInputFileList();
		
		if(command.equals(MainActionCommands.SELECT_RAW_DATA_DIRECTORIES_COMMAND.getName()))
			selectRawDataDirectories();		
		
		if(command.equals(MainActionCommands.SELECT_DESTINATION_FOR_COMPRESSED_FILES_COMMAND.getName()))
			selectDestinationDirectory();
	}
	
	private void selectInputFileList() {
		
		JnaFileChooser fc = new JnaFileChooser(baseDirectory);
		fc.setMode(JnaFileChooser.Mode.Files);
		fc.setTitle("Select inputFileList");
		fc.setMultiSelectionEnabled(false);
		fc.setOpenButtonText("Select file");
		fc.addFilter("Text files", "txt", "TXT", "tsv", "TSV", "csv", "CSV");
		if (fc.showOpenDialog(SwingUtilities.getWindowAncestor(parent))) {
			
			inputFileList = fc.getSelectedFile();			
			fileListPathField.setText(inputFileList.getAbsolutePath());
			baseDirectory = inputFileList.getParentFile();		
		}
	}
	
	private void selectRawDataDirectories() {
		
		JnaFileChooser fc = new JnaFileChooser(baseDirectory);
		fc.setMode(JnaFileChooser.Mode.Directories);
		fc.setTitle("Select raw data directories");
		fc.setMultiSelectionEnabled(true);
		fc.setOpenButtonText("Select directories");
		if (fc.showOpenDialog(SwingUtilities.getWindowAncestor(parent))) {
						
			File[] dirs = fc.getSelectedFiles();
			Collection<File> currentList = table.getAllFiles();		
			for(File d : dirs)
				currentList.add(d);
			
			table.setTableModelFromFileCollection(currentList);
		}
	}
	
	private void selectDestinationDirectory() {
		
		JnaFileChooser fc = new JnaFileChooser(baseDirectory);
		fc.setMode(JnaFileChooser.Mode.Directories);
		fc.setTitle("Select destination directory for compressed files");
		fc.setMultiSelectionEnabled(false);
		fc.setOpenButtonText("Select directory");
		if (fc.showOpenDialog(SwingUtilities.getWindowAncestor(parent))) {
						
			destinationDir = fc.getSelectedFile();
			baseDirectory = destinationDir.getParentFile();
			destinationPathTextField.setText(destinationDir.getAbsolutePath());
		}
	}
	
	public MoTrPACAssay getAssay() {
		return assay;
	}

	public File getInputFileList() {
		return inputFileList;
	}
	
	public Collection<File> getRawDataDirectories() {
		return table.getAllFiles();
	}
	
	public File getDestinationDir() {
		return destinationDir;
	}
	
	public Collection<String>validateInput(){
		
		Collection<String>errors = new ArrayList<String>();
		if(inputFileList == null && destinationDir == null && getRawDataDirectories().isEmpty())
			return errors;
		
		if(inputFileList == null)
			errors.add("File list missing for " + assay.getDescription());
		
		if(getRawDataDirectories().isEmpty())
			errors.add("Raw data directories not specified for " + assay.getDescription());
		
		if(destinationDir == null)
			errors.add("Destination directory not specified for " + assay.getDescription());
		
		return errors;
	}
	
	public AgilentDataCompressionTask getCompressionTask() {
		
		if(!validateInput().isEmpty() || inputFileList == null 
				|| getRawDataDirectories().isEmpty() || destinationDir == null)
			return null;
		
		AgilentDataCompressionTask task = 
				new AgilentDataCompressionTask(
				inputFileList, 
				getRawDataDirectories(), 
				destinationDir,
				assay.getDescription());
		
		return task;
	}	
}

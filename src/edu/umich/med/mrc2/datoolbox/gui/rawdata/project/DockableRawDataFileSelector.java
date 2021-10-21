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

package edu.umich.med.mrc2.datoolbox.gui.rawdata.project;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Collection;

import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.fc.ImprovedFileChooser;

public class DockableRawDataFileSelector extends DefaultSingleCDockable 
		implements ActionListener {

	private static final Icon chromIcon = GuiUtils.getIcon("chromatogram", 16);

	private ImprovedFileChooser chooser;
	private File baseDirectory;
	private RawDataFilesTable rawDataFilesTable;
	private RawDataFileSelectorToolbar toolbar;
	private JCheckBox copyFilesCheckBox;

	public DockableRawDataFileSelector() {

		super("DockableRawDataFileSelector", chromIcon, "Select raw data files", null, Permissions.MIN_MAX_STACK);
		setCloseable(false);
		setLayout(new BorderLayout(0, 0));
		toolbar = new RawDataFileSelectorToolbar(this);
		add(toolbar, BorderLayout.NORTH);

		rawDataFilesTable = new RawDataFilesTable();
		JScrollPane scroll = new JScrollPane(rawDataFilesTable);
		add(scroll, BorderLayout.CENTER);
		
		JPanel cbPanel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) cbPanel.getLayout();
		flowLayout.setAlignment(FlowLayout.LEFT);			
		copyFilesCheckBox = new JCheckBox("Copy raw data to project");
		cbPanel.add(copyFilesCheckBox);
		add(cbPanel, BorderLayout.SOUTH);
		
		initChooser();
	}

	private void initChooser() {

		chooser = new ImprovedFileChooser();
		chooser.setBorder(new EmptyBorder(10, 10, 10, 10));
		chooser.setAcceptAllFileFilterUsed(false);
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setMultiSelectionEnabled(true);
		chooser.setFileFilter(
				new FileNameExtensionFilter("Raw MS files", "mzml", "mzML", "mzXML", "mzxml"));
	}
	
	public void setBaseDirectory(File baseDir) {
		
		baseDirectory = baseDir;
		if(baseDirectory.exists() && baseDirectory.isDirectory())
			chooser.setCurrentDirectory(baseDirectory);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {

		String command = e.getActionCommand();
		if(command.equals(MainActionCommands.ADD_DATA_FILES_COMMAND.getName()))
			addRawDataFiles();
		
		if(command.equals(MainActionCommands.REMOVE_DATA_FILES_COMMAND.getName()))
			removeSelectedRawDataFiles();
		
		if(command.equals(MainActionCommands.CLEAR_DATA_COMMAND.getName())) 
			removeAllRawDataFiles();	
	}
	
	private void addRawDataFiles() {
		
		if(chooser.showOpenDialog(this.getContentPane()) == JFileChooser.APPROVE_OPTION) {
			
			File[] selectedFiles = chooser.getSelectedFiles();
			if(selectedFiles.length == 0)
				return;
			
			rawDataFilesTable.addDataFiles(selectedFiles);
			baseDirectory = selectedFiles[0].getParentFile();
		}
	}
	
	private void removeSelectedRawDataFiles() {
		
		if(rawDataFilesTable.getSelectedDataFiles().isEmpty())
			return;
		Collection<File> allFiles = rawDataFilesTable.getDataFiles();
		allFiles.removeAll(
				rawDataFilesTable.getSelectedDataFiles());
		rawDataFilesTable.setTableModelFromDataFiles(allFiles);
	}
	
	private void removeAllRawDataFiles() {
		rawDataFilesTable.clearTable();
	}
	
	public Collection<File> getDataFiles() {
		return rawDataFilesTable.getDataFiles();
	}

	public File getBaseDirectory() {
		return baseDirectory;
	}
	
	public boolean copyRawDataToProject() {
		return copyFilesCheckBox.isSelected();
	}

}

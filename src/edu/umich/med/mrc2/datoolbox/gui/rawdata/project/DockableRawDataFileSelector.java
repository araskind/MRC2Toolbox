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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Collection;

import javax.swing.Icon;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.jnafilechooser.api.JnaFileChooser;

public class DockableRawDataFileSelector extends DefaultSingleCDockable 
		implements ActionListener {

	private File baseDirectory;
	private RawDataFilesTable rawDataFilesTable;
	private RawDataFileSelectorToolbar toolbar;
	private RawDataAnalysisExperimentSetupDialog parentDialog;

	public DockableRawDataFileSelector(
			String title, 
			Icon chromIcon,
			RawDataAnalysisExperimentSetupDialog parentDialog) {

		super(title.replace(" ", ""), chromIcon, title, null, Permissions.MIN_MAX_STACK);
		setCloseable(false);
		setLayout(new BorderLayout(0, 0));
		toolbar = new RawDataFileSelectorToolbar(this);
		add(toolbar, BorderLayout.NORTH);

		this.parentDialog = parentDialog;
		rawDataFilesTable = new RawDataFilesTable();
		JScrollPane scroll = new JScrollPane(rawDataFilesTable);
		add(scroll, BorderLayout.CENTER);
	}

	public void setBaseDirectory(File baseDir) {
		
		if(baseDir != null && baseDir.exists() && baseDir.isDirectory())
			baseDirectory = baseDir;
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
		
		JnaFileChooser fc = new JnaFileChooser(baseDirectory);
		fc.setMode(JnaFileChooser.Mode.Files);
		fc.addFilter("Raw MS files", "mzml", "mzML", "mzXML", "mzxml");
		fc.setTitle("Select raw data files");
		fc.setMultiSelectionEnabled(true);
		if (fc.showOpenDialog(SwingUtilities.getWindowAncestor(this.getContentPane()))) {
			
			File[] selectedFiles = fc.getSelectedFiles();
			if(selectedFiles.length == 0)
				return;
			
			rawDataFilesTable.addDataFiles(selectedFiles);
			baseDirectory = selectedFiles[0].getParentFile();			
			parentDialog.adjustRawDataBaseDirectory(baseDirectory);
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
}



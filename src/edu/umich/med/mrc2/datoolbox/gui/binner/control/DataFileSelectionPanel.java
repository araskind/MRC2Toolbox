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

package edu.umich.med.mrc2.datoolbox.gui.binner.control;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.enums.Polarity;
import edu.umich.med.mrc2.datoolbox.gui.expdesign.assay.AssayDesignPopupMenu;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.ValidatableForm;
import edu.umich.med.mrc2.datoolbox.main.ReferenceSamplesManager;

public class DataFileSelectionPanel extends JPanel implements ActionListener, ValidatableForm{

	private static final long serialVersionUID = 1L;
	
	private DataFileTable dataFileTable;
	private DataFileSelectionToolbar toolbar;

	public DataFileSelectionPanel() {
		
		super(new BorderLayout(0, 0));
		
		toolbar = new DataFileSelectionToolbar(this);
		add(toolbar, BorderLayout.NORTH);
		dataFileTable = new DataFileTable();
		dataFileTable.addTablePopupMenu(new AssayDesignPopupMenu(this, dataFileTable));	
		add(new JScrollPane(dataFileTable), BorderLayout.CENTER);
	}	

	@Override
	public void actionPerformed(ActionEvent e) {

		String command = e.getActionCommand();
		
		if(command.equals(MainActionCommands.DISABLE_REFERENCE_SAMPLES_COMMAND.getName()))
			disableDataFilesForReferenceSamples();
		
		if(command.equals(MainActionCommands.ENABLE_SELECTED_SAMPLES_COMMAND.getName()))
			changeStatusForSelectedDataFiles(true);
		
		if(command.equals(MainActionCommands.DISABLE_SELECTED_SAMPLES_COMMAND.getName()))
			changeStatusForSelectedDataFiles(false);
		
		if(command.equals(MainActionCommands.INVERT_ENABLED_SAMPLES_COMMAND.getName()))
			invertEnabledDataFileSelection();
	}

	private void invertEnabledDataFileSelection() {
		
		 Collection<DataFile>enabledFiles = dataFileTable.getEnabledFiles();		 
		 Collection<DataFile>allFiles = dataFileTable.getAllFiles();		 
		 Collection<DataFile>disabledFiles = 
				 allFiles.stream().filter(f -> !enabledFiles.contains(f)).
				 collect(Collectors.toList());
		 dataFileTable.changeStatusForDataFiles(enabledFiles, false);
		 dataFileTable.changeStatusForDataFiles(disabledFiles, true);
	}

	private void changeStatusForSelectedDataFiles(boolean setEnabled) {

		 Collection<DataFile>selectedFiles = dataFileTable.getSelectedFiles();
		 if(selectedFiles.isEmpty())
			 return;
		 
		 dataFileTable.changeStatusForDataFiles(selectedFiles, setEnabled);		
	}

	private void disableDataFilesForReferenceSamples() {

		Collection<DataFile>allFiles = dataFileTable.getAllFiles();
		Collection<DataFile>sampleFiles = allFiles.stream().
				filter(f -> f.getParentSample().hasLevel(ReferenceSamplesManager.sampleLevel)).
				collect(Collectors.toList());
		Collection<DataFile>controlFiles = allFiles.stream().
				filter(f -> !sampleFiles.contains(f)).
				collect(Collectors.toList());
		
		 dataFileTable.changeStatusForDataFiles(controlFiles, false);
		 dataFileTable.changeStatusForDataFiles(sampleFiles, true);
	}

	public void setTableModelFromFileCollection(Collection<DataFile> files) {
		dataFileTable.setTableModelFromFileCollection(files);
	}
	
	public Collection<DataFile> getEnabledFiles() {
		return dataFileTable.getEnabledFiles();
	}

	@Override
	public Collection<String> validateFormData() {

		Collection<String>errors = new ArrayList<String>();
		if(getEnabledFiles().isEmpty())
			errors.add("No data files selected");
		
		Set<Polarity>polSet = getEnabledFiles().stream().
				map(f -> f.getDataAcquisitionMethod().getPolarity()).
				distinct().collect(Collectors.toSet());
		if(polSet.size() > 1)
			errors.add("All data files must have the same polarity for MS acquisition.");
		
		return errors;
	}
}

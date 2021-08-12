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

package edu.umich.med.mrc2.datoolbox.gui.io.matcher;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.ExperimentalSample;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;

public class DataFileSampleMatchPanelOld extends JPanel implements ActionListener{

	/**
	 * 
	 */
	private static final long serialVersionUID = 274484571199077303L;
	
	private DataFileSampleMatchTableOld matchTable;
	private SampleAssignmentDialog sampleAssignmentDialog;
	private Set<DataFile>dataFilesForMethod;

	public DataFileSampleMatchPanelOld() {

		super();

		setLayout(new BorderLayout(0, 0));
		matchTable = new DataFileSampleMatchTableOld();
		matchTable.addTablePopupMenu(new SampleMatcherPopupMenu(this));
		JScrollPane jsp = new JScrollPane(matchTable);
		add(jsp, BorderLayout.CENTER);
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		String command = e.getActionCommand();
		if(command.equals(MainActionCommands.REMOVE_DATA_FILES_COMMAND.getName())) {

			if(matchTable.getSelectedRows().length == 0)
				return;

			if (MessageDialog.showChoiceMsg("Remove selected files?", this) == JOptionPane.YES_OPTION)
				removeSelectedDataFiles();
		}
		if(command.equals(MainActionCommands.EDIT_DESIGN_FOR_SELECTED_SAMPLES_COMMAND.getName())) {

			if(matchTable.getSelectedRows().length == 0)
				return;

			sampleAssignmentDialog = new SampleAssignmentDialog(this);
			sampleAssignmentDialog.setLocationRelativeTo(this);
			sampleAssignmentDialog.setVisible(true);
		}
		if(command.equals(MainActionCommands.ASSIGN_SAMPLESS_COMMAND.getName())) {

			ExperimentalSample sample = sampleAssignmentDialog.getSelectedSample();
			if(sample == null) {
				MessageDialog.showErrorMsg("Please select the sample!", this);
				return;
			}
			matchTable.updateSampleAssignmentForDataFiles(matchTable.getSelectedDataFiles(), sample);
			sampleAssignmentDialog.dispose();
		}
	}
	
	public void addFilesForDataPipeline(File[] inputFiles, DataPipeline pipeline) {
		matchTable.addFilesForDataPipeline(inputFiles, pipeline);
	}

	public Collection<DataFile>getAllDataFiles(){
		return matchTable.getAllDataFiles();
	}
	
	public DataFile[] getActiveDataFiles() {
		return matchTable.getActiveDataFiles();
	}

	public void removeSelectedDataFiles() {		
		matchTable.removeSelectedDataFiles();
	}
	
	public Map<DataFile, ExperimentalSample> getDataFileSampleMap() {
		return matchTable.getDataFileSampleMap();
	}
	
	public void refreshSampleEditor() {
		matchTable.refreshSampleEditor();
	}
	
	public void assignSamples(Map<DataFile, ExperimentalSample> fileSampleMap) {
		matchTable.assignSamples(fileSampleMap);
	}
	
	public void clearTable() {
		matchTable.clearTable();
	}
	
	public Collection<DataFile> getUnmatchedEnabledFiles(){
		return matchTable.getUnmatchedEnabledFiles();
	}
	
	public Collection<DataFile>getSelectedDataFiles(){
		return matchTable.getSelectedDataFiles();
	}

	public Set<DataFile> getDataFilesForMethod() {
		return dataFilesForMethod;
	}

	public void setDataFilesForMethod(Set<DataFile> dataFilesForMethod) {
		this.dataFilesForMethod = dataFilesForMethod;
	}	
}

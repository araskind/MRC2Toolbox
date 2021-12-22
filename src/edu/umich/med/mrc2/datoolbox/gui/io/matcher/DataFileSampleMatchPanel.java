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
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.ExperimentalSample;
import edu.umich.med.mrc2.datoolbox.data.SampleDataResultObject;
import edu.umich.med.mrc2.datoolbox.data.compare.SampleDataResultObjectComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;

public class DataFileSampleMatchPanel extends JPanel implements ActionListener{

	/**
	 * 
	 */
	private static final long serialVersionUID = 274484571199077303L;
	
	private DataFileSampleMatchTable matchTable;
	private SampleAssignmentDialog sampleAssignmentDialog;

	public DataFileSampleMatchPanel() {

		super();

		setLayout(new BorderLayout(0, 0));
		matchTable = new DataFileSampleMatchTable();
		matchTable.addTablePopupMenu(new SampleMatcherPopupMenu(this));
		JScrollPane jsp = new JScrollPane(matchTable);
		add(jsp, BorderLayout.CENTER);
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		String command = e.getActionCommand();		
		if(command.equals(MainActionCommands.EDIT_DESIGN_FOR_SELECTED_SAMPLES_COMMAND.getName()))
			showSampleAssignmentEditor();
		
		if(command.equals(MainActionCommands.ASSIGN_SAMPLESS_COMMAND.getName())) 
			setExperimentalSampleForSelectedFiles();
		
		if(command.equals(MainActionCommands.REMOVE_DATA_FILES_COMMAND.getName()))
			removeSelected();
	}
	
	public void loadSampleDataResultObject(Set<SampleDataResultObject> sdrObjects) {
		matchTable.setModelFromSampleDataResultObjects(sdrObjects);
	}
	
	private void showSampleAssignmentEditor() {
		
		if(matchTable.getSelectedRows().length == 0)
			return;

		sampleAssignmentDialog = new SampleAssignmentDialog(this);
		sampleAssignmentDialog.setLocationRelativeTo(this);
		sampleAssignmentDialog.setVisible(true);
	}
	private void setExperimentalSampleForSelectedFiles() {
		
		ExperimentalSample sample = sampleAssignmentDialog.getSelectedSample();
		if(sample == null) {
			MessageDialog.showErrorMsg("Please select the sample!", this);
			return;
		}
		Set<SampleDataResultObject> selectedSdrObjects = 
				matchTable.getSelectedSampleDataResultObjects();
		if(selectedSdrObjects.isEmpty())
			return;
		
		Set<SampleDataResultObject>sampleDataResultObjects = 
				matchTable.getSampleDataResultObject(false);
		for(SampleDataResultObject o : sampleDataResultObjects) {
			
			if(selectedSdrObjects.contains(o))
				o.setSample(sample);
		}
		matchTable.setModelFromSampleDataResultObjects(sampleDataResultObjects);
		sampleAssignmentDialog.dispose();
	}
	
	public void removeSelected() {	
		
		if(matchTable.getSelectedRows().length == 0)
			return;
		
		if (MessageDialog.showChoiceMsg("Remove selected?", this) != JOptionPane.YES_OPTION)
			return;
		
		Set<SampleDataResultObject> selectedSdrObjects = 
				matchTable.getSelectedSampleDataResultObjects();		
		Set<SampleDataResultObject>cleanSampleDataResultObjects = new TreeSet<SampleDataResultObject>(
				new SampleDataResultObjectComparator(SortProperty.sample));
		
		for(SampleDataResultObject o : matchTable.getSampleDataResultObject(false)) {
			
			if(!selectedSdrObjects.contains(o))
				cleanSampleDataResultObjects.add(o);
		}
		matchTable.setModelFromSampleDataResultObjects(cleanSampleDataResultObjects);
	}
	
	public void refreshSampleEditor() {
		matchTable.refreshSampleEditor();
	}
	
	public void assignSamples(Map<DataFile, ExperimentalSample> fileSampleMap) {
		//	TODO
		//	matchTable.assignSamples(fileSampleMap);
	}
	
	public synchronized void clearTable() {
		matchTable.clearTable();
	}

	public Set<SampleDataResultObject> getSampleDataResultObjects(boolean enabledOnly) {
		return matchTable.getSampleDataResultObject(enabledOnly);
	}	
}

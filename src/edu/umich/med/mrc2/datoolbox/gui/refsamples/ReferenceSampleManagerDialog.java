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

package edu.umich.med.mrc2.datoolbox.gui.refsamples;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;

import org.apache.commons.lang3.StringUtils;

import edu.umich.med.mrc2.datoolbox.data.ExperimentalSample;
import edu.umich.med.mrc2.datoolbox.data.enums.MoTrPACQCSampleType;
import edu.umich.med.mrc2.datoolbox.data.enums.ParameterSetStatus;
import edu.umich.med.mrc2.datoolbox.database.idt.ReferenceSamplesManager;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;

public class ReferenceSampleManagerDialog extends JDialog implements ActionListener {

	/**
	 *
	 */
	private static final long serialVersionUID = -2583577627221420094L;
	private static final Icon refSampleIcon = GuiUtils.getIcon("standardSample", 32);
	public static final int MAX_SAMPLE_ID_SIZE = 40;
	public static final int MAX_SAMPLE_NAME_SIZE = 100;
	private ReferenceSampleTable refSampleTable;
	private ReferenceSampleManagerToolbar toolBar;
	private ReferenceSampleEditorDialog referenceSampleEditorDialog;

	public ReferenceSampleManagerDialog() {

		super();
		setIconImage(((ImageIcon) refSampleIcon).getImage());
		setTitle("Manage reference samples");
		setPreferredSize(new Dimension(500, 500));

		setModalityType(ModalityType.APPLICATION_MODAL);
		setSize(new Dimension(500, 500));
		setResizable(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		JPanel panel = new JPanel();
		getContentPane().add(panel, BorderLayout.CENTER);
		panel.setLayout(new BorderLayout(0, 0));

		refSampleTable = new ReferenceSampleTable();
		JScrollPane scrollPane = new JScrollPane(refSampleTable);
		panel.add(scrollPane, BorderLayout.CENTER);

		toolBar = new ReferenceSampleManagerToolbar(this);
		getContentPane().add(toolBar, BorderLayout.NORTH);

		refSampleTable.loadReferenceSamples();
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		String command = e.getActionCommand();
		if(command.equals(MainActionCommands.ADD_REFERENCE_SAMPLE_DIALOG_COMMAND.getName()))
			showReferenceSampleEditor(null);

		if(command.equals(MainActionCommands.ADD_REFERENCE_SAMPLE_COMMAND.getName()))
			addReferenceSample();

		if(command.equals(MainActionCommands.EDIT_REFERENCE_SAMPLE_DIALOG_COMMAND.getName())) {

			if(refSampleTable.getSelectedSample() != null)
				showReferenceSampleEditor(refSampleTable.getSelectedSample());
		}

		if(command.equals(MainActionCommands.EDIT_REFERENCE_SAMPLE_COMMAND.getName()))
			editReferenceSample();

		if(command.equals(MainActionCommands.DELETE_REFERENCE_SAMPLE_COMMAND.getName()))
			deleteSelectedSample();
	}

	private void showReferenceSampleEditor(ExperimentalSample sampleToEdit) {

		//	Edit existing reference sample
		if(sampleToEdit != null) {

			if(sampleToEdit.isLockedReference()) {

				MessageDialog.showWarningMsg("Selected reference sample is locked and can not be edited", this);
				return;
			}
			else {
				referenceSampleEditorDialog = new ReferenceSampleEditorDialog(sampleToEdit, this);
				referenceSampleEditorDialog.setLocationRelativeTo(this);
				referenceSampleEditorDialog.setVisible(true);
			}
		}
		//	Create new reference sample
		else {
			referenceSampleEditorDialog = new ReferenceSampleEditorDialog(null, this);
			referenceSampleEditorDialog.setLocationRelativeTo(this);
			referenceSampleEditorDialog.setVisible(true);
		}
	}

	private void editReferenceSample() {

		ExperimentalSample sample = referenceSampleEditorDialog.getSample();
		if(sample == null)
			return;

		Collection<String>errors =
				validateSampleEdits(sample,
						referenceSampleEditorDialog.getSampleId(),
						referenceSampleEditorDialog.getSampleName(),
						referenceSampleEditorDialog.getMoTrPACQCSampleType());
		if(!errors.isEmpty()) {
			MessageDialog.showErrorMsg(StringUtils.join(errors, "\n"), referenceSampleEditorDialog);
			return;
		}
		try {
			if(ReferenceSamplesManager.updateReferenceSample(
					sample, referenceSampleEditorDialog.getSampleId(), 
					referenceSampleEditorDialog.getSampleName(),
					referenceSampleEditorDialog.getMoTrPACQCSampleType()) > 0) {

				refSampleTable.loadReferenceSamples();
				refSampleTable.selectSampleRow(sample);

				if(MRC2ToolBoxCore.getCurrentProject() != null) {
					if(MRC2ToolBoxCore.getCurrentProject().getExperimentDesign().getSampleById(sample.getId()) != null)
						MRC2ToolBoxCore.getCurrentProject().getExperimentDesign().fireExperimentDesignEvent(ParameterSetStatus.CHANGED);
				}
				referenceSampleEditorDialog.dispose();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void addReferenceSample() {

		ExperimentalSample newRefSample = new ExperimentalSample(
					referenceSampleEditorDialog.getSampleId(),
					referenceSampleEditorDialog.getSampleName());
		newRefSample.setMoTrPACQCSampleType(referenceSampleEditorDialog.getMoTrPACQCSampleType());

		Collection<String>errors = validateNewSample(newRefSample);
		if(!errors.isEmpty()) {
			MessageDialog.showErrorMsg(StringUtils.join(errors, "\n"), referenceSampleEditorDialog);
			return;
		}
		try {
			if(ReferenceSamplesManager.addReferenceSample(newRefSample) > 0) {

				refSampleTable.loadReferenceSamples();
				refSampleTable.selectSampleRow(newRefSample);
				referenceSampleEditorDialog.dispose();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private Collection<String>validateNewSample(ExperimentalSample newRefSample){

		Collection<String>errors = new ArrayList<String>();

		if(newRefSample.getId().isEmpty())
			errors.add("Sample ID can not be empty.");

		if(ReferenceSamplesManager.getReferenceSampleById(newRefSample.getId()) != null)
			errors.add("Reference sample with ID " + newRefSample.getId() + " already exists.");

		if(newRefSample.getId().length() > MAX_SAMPLE_ID_SIZE)
			errors.add("Reference sample ID too long (max size " +
					Integer.toString(MAX_SAMPLE_ID_SIZE) + " characters).");

		if(newRefSample.getName().isEmpty())
			errors.add("Sample name can not be empty.");

		if(ReferenceSamplesManager.getReferenceSampleByName(newRefSample.getName()) != null)
			errors.add("Reference sample with the name \"" + newRefSample.getName() + "\" already exists.");

		if(newRefSample.getName().length() > MAX_SAMPLE_NAME_SIZE)
			errors.add("Reference sample name too long (max size " +
					Integer.toString(MAX_SAMPLE_NAME_SIZE) + " characters).");
		
		if(newRefSample.getMoTrPACQCSampleType() == null)
			errors.add("Reference sample type should be specified.");

		return errors;
	}

	private Collection<String>validateSampleEdits(
			ExperimentalSample refSample, 
			String newId, String newName, 
			MoTrPACQCSampleType moTrPACQCSampleType){

		Collection<String>errors = new ArrayList<String>();

		if(newId.isEmpty())
			errors.add("Sample ID can not be empty!");

		ExperimentalSample sameId = ReferenceSamplesManager.getReferenceSampleById(newId);

		if(sameId != null) {
			if(!sameId.equals(refSample))
				errors.add("Another reference sample with ID " + newId + " already exists!");
		}
		if(newId.length() > MAX_SAMPLE_ID_SIZE)
			errors.add("Reference sample ID too long (max size " +
					Integer.toString(MAX_SAMPLE_ID_SIZE) + " characters)!");

		if(newName.isEmpty())
			errors.add("Sample name can not be empty!");

		ExperimentalSample sameName = ReferenceSamplesManager.getReferenceSampleByName(newName);
		if(sameName != null) {
			if(!sameName.equals(refSample))
				errors.add("Reference sample with the name \"" + newName + "\" already exists!");
		}
		if(newName.length() > MAX_SAMPLE_NAME_SIZE)
			errors.add("Another reference sample name too long (max size " +
					Integer.toString(MAX_SAMPLE_NAME_SIZE) + " characters)!");
		
		if(moTrPACQCSampleType == null)
			errors.add("Reference sample type should be specified.");

		return errors;
	}

	private void deleteSelectedSample() {

		if(refSampleTable.getSelectedSample() == null)
			return;

		if(refSampleTable.getSelectedSample().isLockedReference()) {

			MessageDialog.showWarningMsg("Selected reference sample is locked and can not be deleted", this);
			return;
		}
		else {
			int result = MessageDialog.showChoiceWithWarningMsg(
					"Do you want to delete selected reference sample?", this);
			if(result == JOptionPane.YES_OPTION) {

				try {
					ExperimentalSample toDelete = refSampleTable.getSelectedSample();
					ReferenceSamplesManager.deleteReferenceSample(toDelete);
					refSampleTable.loadReferenceSamples();

					if(MRC2ToolBoxCore.getCurrentProject() != null) {
						if(MRC2ToolBoxCore.getCurrentProject().getExperimentDesign().getSampleById(toDelete.getId()) != null)
							MRC2ToolBoxCore.getCurrentProject().getExperimentDesign().removeSample(toDelete);
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
}




























/*******************************************************************************
 *
 * (C) Copyright 2018-2025 MRC2 (http://mrc2.umich.edu).
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

package edu.umich.med.mrc2.datoolbox.gui.idtlims.worklist;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Collection;
import java.util.Date;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.ExperimentalSample;
import edu.umich.med.mrc2.datoolbox.data.Worklist;
import edu.umich.med.mrc2.datoolbox.data.lims.DataAcquisitionMethod;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSExperiment;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSSamplePreparation;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSWorklistItem;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class InstrumentSequenceImportDialog extends JDialog implements ActionListener {

	/**
	 *
	 */
	private static final long serialVersionUID = -8958656243635154039L;
	private static final Icon loadWorklistFromFileIcon = GuiUtils.getIcon("loadWorklist", 32);
	private JButton btnSave;
	private InstrumentSequenceTable table;
	private BatchSampleAssignmentDialog batchSampleAssignmentDialog;
	private LIMSExperiment experiment;
	private LIMSSamplePreparation activeSamplePrep;
	private Worklist wkl;

	public InstrumentSequenceImportDialog(ActionListener actionListener) {
		super();

		setPreferredSize(new Dimension(800, 640));
		setSize(new Dimension(800, 640));
		setModalityType(ModalityType.APPLICATION_MODAL);
		setResizable(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setTitle("Load sample injection data");
		setIconImage(((ImageIcon) loadWorklistFromFileIcon).getImage());

		JPanel dataPanel = new JPanel();
		dataPanel.setBorder(null);
		getContentPane().add(dataPanel, BorderLayout.CENTER);
		dataPanel.setLayout(new BorderLayout(0, 0));

		table = new InstrumentSequenceTable();
		table.addTablePopupMenu(new WorklistImportPopupMenu(this));
		JScrollPane scrollPane = new JScrollPane(table);
		dataPanel.add(scrollPane, BorderLayout.CENTER);

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

		btnSave = new JButton("Save");
		btnSave.setActionCommand(MainActionCommands.SEND_WORKLIST_TO_DATABASE.getName());
		btnSave.addActionListener(actionListener);
		panel.add(btnSave);
		JRootPane rootPane = SwingUtilities.getRootPane(btnSave);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(btnSave);

		pack();
	}

	public void loadWorklist(
			Worklist wkl,
			LIMSExperiment experiment,
			LIMSSamplePreparation activeSamplePrep) {

		this.wkl = wkl;
		this.experiment = experiment;
		this.activeSamplePrep = activeSamplePrep;
		table.populateTableFromWorklistExperimentAndSamplePrep(wkl, experiment, activeSamplePrep);
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		if(e.getActionCommand().equals(MainActionCommands.EDIT_DESIGN_FOR_SELECTED_SAMPLES_COMMAND.getName())) {

			Collection<DataFile> selectedDataFiles = table.getSelectedDataFiles();
			if(selectedDataFiles.isEmpty())
				return;

			batchSampleAssignmentDialog = new BatchSampleAssignmentDialog(
					selectedDataFiles, experiment, activeSamplePrep, this);
			batchSampleAssignmentDialog.setLocationRelativeTo(this);
			batchSampleAssignmentDialog.setVisible(true);
		}
		if(e.getActionCommand().equals(MainActionCommands.ASSIGN_SAMPLE_TO_DATA_FILES_COMMAND.getName())) {

			Collection<DataFile> selectedFiles = batchSampleAssignmentDialog.getDataFiles();
			ExperimentalSample selectedSample = batchSampleAssignmentDialog.getSelectedSample();
			String selectedPrep = batchSampleAssignmentDialog.getSelectedPrepItem();
			if(selectedSample == null && selectedPrep == null)
				return;

			if(selectedSample != null) {
				wkl.getTimeSortedWorklistItems().stream().
					filter(LIMSWorklistItem.class::isInstance).
					map(LIMSWorklistItem.class::cast).filter(i -> selectedFiles.contains(i.getDataFile())).
					forEach(i -> i.setSample(selectedSample));
			}
			if(selectedPrep != null) {
				wkl.getTimeSortedWorklistItems().stream().
					filter(LIMSWorklistItem.class::isInstance).
					map(LIMSWorklistItem.class::cast).filter(i -> selectedFiles.contains(i.getDataFile())).
					forEach(i -> i.setPrepItemId(selectedPrep));
			}
			table.populateTableFromWorklistExperimentAndSamplePrep(wkl, experiment, activeSamplePrep);
			batchSampleAssignmentDialog.dispose();
		}
	}

	/**
	 * @return the wkl
	 */
	public Worklist getWorklist() {

		Worklist newWorklist = new Worklist();
		InstrumentSequenceTableModel model = (InstrumentSequenceTableModel)table.getModel();
		for(int i=0; i<model.getRowCount(); i++) {

			DataFile df =
				(DataFile) model.getValueAt(i, model.getColumnIndex(InstrumentSequenceTableModel.DATA_FILE_COLUMN));
			ExperimentalSample sample  =
				(ExperimentalSample) model.getValueAt(i, model.getColumnIndex(InstrumentSequenceTableModel.SAMPLE_COLUMN));
			DataAcquisitionMethod method =
				(DataAcquisitionMethod) model.getValueAt(i, model.getColumnIndex(InstrumentSequenceTableModel.ACQ_METHOD_COLUMN));
			LIMSSamplePreparation prep =
				(LIMSSamplePreparation) model.getValueAt(i, model.getColumnIndex(InstrumentSequenceTableModel.SAMPLE_PREP_COLUMN));
			String prepItem =
				(String) model.getValueAt(i, model.getColumnIndex(InstrumentSequenceTableModel.SAMPLE_PREP_ITEM_COLUMN));
			Date timestamp =
				(Date) model.getValueAt(i, model.getColumnIndex(InstrumentSequenceTableModel.INJECTION_TIME_COLUMN));
			double injVolume =
				(double) model.getValueAt(i, model.getColumnIndex(InstrumentSequenceTableModel.INJECTION_VOLUME_COLUMN));

			LIMSWorklistItem newItem = new LIMSWorklistItem(
					df,
					sample,
					method,
					prep,
					prepItem,
					timestamp,
					injVolume);

			newWorklist.addItem(newItem);
		}
		return newWorklist;
	}
}






































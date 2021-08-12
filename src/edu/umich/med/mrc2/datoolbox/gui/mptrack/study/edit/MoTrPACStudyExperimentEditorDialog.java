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

package edu.umich.med.mrc2.datoolbox.gui.mptrack.study.edit;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Collection;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import edu.umich.med.mrc2.datoolbox.data.compare.LIMSExperimentComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortDirection;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSExperiment;
import edu.umich.med.mrc2.datoolbox.data.motrpac.MoTrPACStudy;
import edu.umich.med.mrc2.datoolbox.database.lims.LIMSDataCash;
import edu.umich.med.mrc2.datoolbox.gui.idworks.search.se.IDTrackerExperimentListingTable;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.CommonToolbar;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class MoTrPACStudyExperimentEditorDialog extends JDialog implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2457637035355313241L;
	private static final Icon editExperimentsIcon = GuiUtils.getIcon("idExperiment", 32);
	private static final Icon addSampleIcon = GuiUtils.getIcon("addSample", 32);
	private static final Icon deleteSampleIcon = GuiUtils.getIcon("deleteSample", 32);

	private JButton addButton, removeButton;
	private MoTrPACStudy study;

	private IDTrackerExperimentListingTable availableExperimentsTable;
	private IDTrackerExperimentListingTable assignedExperimentsTable;
	
	public MoTrPACStudyExperimentEditorDialog(MoTrPACStudy study, ActionListener listener) {
		super();
		this.study = study;
		setIconImage(((ImageIcon) editExperimentsIcon).getImage());
		setTitle("Edit experiments for study \"" + study.getDescription() + "\"");
		setPreferredSize(new Dimension(1000, 500));
		setSize(new Dimension(1000, 500));
		setResizable(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setModalityType(ModalityType.APPLICATION_MODAL);
		
		JPanel contents = new JPanel();
		contents.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(contents, BorderLayout.CENTER);
		
		GridBagLayout gbl_contents = new GridBagLayout();
		gbl_contents.columnWidths = new int[]{300, 50, 300};
		gbl_contents.rowHeights = new int[]{0, 0, 0, 0, 0};
		gbl_contents.columnWeights = new double[]{1.0, 0.1, 1.0};
		gbl_contents.rowWeights = new double[]{0.0, 1.0, 1.0, 1.0, 1.0};
		contents.setLayout(gbl_contents);

		JLabel lblAvailableReferenceSamples = new JLabel("Available experiments");
		GridBagConstraints gbc_lblAvailableReferenceSamples = new GridBagConstraints();
		gbc_lblAvailableReferenceSamples.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblAvailableReferenceSamples.insets = new Insets(0, 0, 5, 5);
		gbc_lblAvailableReferenceSamples.gridx = 0;
		gbc_lblAvailableReferenceSamples.gridy = 0;
		contents.add(lblAvailableReferenceSamples, gbc_lblAvailableReferenceSamples);

		JLabel lblReferenceSamplesPresent = new JLabel("Assigned experiments");
		GridBagConstraints gbc_lblReferenceSamplesPresent = new GridBagConstraints();
		gbc_lblReferenceSamplesPresent.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblReferenceSamplesPresent.insets = new Insets(0, 0, 5, 0);
		gbc_lblReferenceSamplesPresent.gridx = 2;
		gbc_lblReferenceSamplesPresent.gridy = 0;
		contents.add(lblReferenceSamplesPresent, gbc_lblReferenceSamplesPresent);

		availableExperimentsTable = new IDTrackerExperimentListingTable(); 
		availableExperimentsTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		JScrollPane scrollPane = new JScrollPane(availableExperimentsTable);
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridheight = 4;
		gbc_scrollPane.insets = new Insets(0, 0, 0, 5);
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 1;
		contents.add(scrollPane, gbc_scrollPane);

		assignedExperimentsTable = new IDTrackerExperimentListingTable();
		assignedExperimentsTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		JScrollPane scrollPane_1 = new JScrollPane(assignedExperimentsTable);
		GridBagConstraints gbc_scrollPane_1 = new GridBagConstraints();
		gbc_scrollPane_1.fill = GridBagConstraints.BOTH;
		gbc_scrollPane_1.gridheight = 4;
		gbc_scrollPane_1.gridx = 2;
		gbc_scrollPane_1.gridy = 1;
		contents.add(scrollPane_1, gbc_scrollPane_1);
		
		addButton = new JButton(addSampleIcon);
		addButton.setActionCommand(MainActionCommands.ADD_EXPERIMENTS_TO_MOTRPAC_STUDY_COMMAND.getName());
		addButton.addActionListener(this);
		addButton.setSize(CommonToolbar.buttonDimension);
		addButton.setPreferredSize(CommonToolbar.buttonDimension);
		GridBagConstraints gbc_addButton = new GridBagConstraints();
		gbc_addButton.anchor = GridBagConstraints.SOUTH;
		gbc_addButton.insets = new Insets(0, 0, 5, 5);
		gbc_addButton.gridx = 1;
		gbc_addButton.gridy = 2;
		contents.add(addButton, gbc_addButton);
	
		removeButton = new JButton(deleteSampleIcon);
		removeButton.setActionCommand(MainActionCommands.REMOVE_EXPERIMENTS_FROM_MOTRPAC_STUDY_COMMAND.getName());
		removeButton.addActionListener(this);
		removeButton.setSize(CommonToolbar.buttonDimension);
		removeButton.setPreferredSize(CommonToolbar.buttonDimension);
		GridBagConstraints gbc_removeButton = new GridBagConstraints();
		gbc_removeButton.anchor = GridBagConstraints.NORTH;
		gbc_removeButton.insets = new Insets(0, 0, 5, 5);
		gbc_removeButton.gridx = 1;
		gbc_removeButton.gridy = 3;
		contents.add(removeButton, gbc_removeButton);
		
		
		JPanel panel_1 = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel_1.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		getContentPane().add(panel_1, BorderLayout.SOUTH);

		JButton btnCancel = new JButton("Cancel");
		panel_1.add(btnCancel);

		JButton saveButton = new JButton(MainActionCommands.SAVE_MOTRPAC_STUDY_EXPERIMENTS_COMMAND.getName());
		saveButton.setActionCommand(MainActionCommands.SAVE_MOTRPAC_STUDY_EXPERIMENTS_COMMAND.getName());
		saveButton.addActionListener(listener);
		panel_1.add(saveButton);

		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				dispose();
			}
		};
		btnCancel.addActionListener(al);
		JRootPane rootPane = SwingUtilities.getRootPane(saveButton);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(saveButton);
		
		loadStudyExperiments(study.getExperiments());
		pack();
	}
	
	private void loadStudyExperiments(Collection<LIMSExperiment>assignedExperiments) {
		
		Collection<LIMSExperiment>availableExperiments = 
				LIMSDataCash.getExperiments().stream().
				filter(e -> !assignedExperiments.contains(e)).
				collect(Collectors.toList());
		
		populateExperimentTables(availableExperiments, assignedExperiments);
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		String command = e.getActionCommand();
		if(command.equals(MainActionCommands.ADD_EXPERIMENTS_TO_MOTRPAC_STUDY_COMMAND.getName()))
			addSelectedExperiments();
		
		if(command.equals(MainActionCommands.REMOVE_EXPERIMENTS_FROM_MOTRPAC_STUDY_COMMAND.getName()))
			removeSelectedExperiments();		
	}
	
	private void addSelectedExperiments() {
		
		Collection<LIMSExperiment> selectedExperiments = availableExperimentsTable.getSelectedExperiments();
		if(selectedExperiments.isEmpty())
			return;
		
		Collection<LIMSExperiment>assignedExperiments = assignedExperimentsTable.getAllExperiments();
		assignedExperiments.addAll(selectedExperiments);
		
		Collection<LIMSExperiment>availableExperiments = availableExperimentsTable.getAllExperiments();
		availableExperiments.removeAll(selectedExperiments);
		
		populateExperimentTables(availableExperiments, assignedExperiments);
	}
	
	private void removeSelectedExperiments() {
		
		Collection<LIMSExperiment> selectedExperiments = assignedExperimentsTable.getSelectedExperiments();
		if(selectedExperiments.isEmpty())
			return;
		
		Collection<LIMSExperiment>assignedExperiments = assignedExperimentsTable.getAllExperiments();
		assignedExperiments.removeAll(selectedExperiments);
		
		Collection<LIMSExperiment>availableExperiments = availableExperimentsTable.getAllExperiments();
		availableExperiments.addAll(selectedExperiments);
		
		populateExperimentTables(availableExperiments, assignedExperiments);
	}
	
	private void populateExperimentTables(Collection<LIMSExperiment>availableExperiments, Collection<LIMSExperiment>assignedExperiments) {
		
		TreeSet<LIMSExperiment>availableExperimentsSorted = 
				new TreeSet<LIMSExperiment>(new LIMSExperimentComparator(SortProperty.ID, SortDirection.DESC));
		availableExperimentsSorted.addAll(availableExperiments);
		availableExperimentsTable.setTableModelFromExperimentList(availableExperimentsSorted);
		
		TreeSet<LIMSExperiment>assignedExperimentsSorted = 
				new TreeSet<LIMSExperiment>(new LIMSExperimentComparator(SortProperty.ID, SortDirection.DESC));
		assignedExperimentsSorted.addAll(assignedExperiments);
		assignedExperimentsTable.setTableModelFromExperimentList(assignedExperimentsSorted);
	}
	
	public Collection<LIMSExperiment>getAssignedExperiments(){
		return assignedExperimentsTable.getAllExperiments();
	}
}













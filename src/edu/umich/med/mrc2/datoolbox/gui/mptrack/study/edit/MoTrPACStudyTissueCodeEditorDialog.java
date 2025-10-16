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

import edu.umich.med.mrc2.datoolbox.data.lims.LIMSExperiment;
import edu.umich.med.mrc2.datoolbox.data.motrpac.MoTrPACStudy;
import edu.umich.med.mrc2.datoolbox.data.motrpac.MoTrPACTissueCode;
import edu.umich.med.mrc2.datoolbox.database.mp.MoTrPACDatabaseCache;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.mptrack.ref.tcode.TissueCodeTable;
import edu.umich.med.mrc2.datoolbox.gui.utils.CommonToolbar;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class MoTrPACStudyTissueCodeEditorDialog extends JDialog implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2457637035355313241L;
	private static final Icon editTissuesIcon = GuiUtils.getIcon("tissueCode", 32);
	private static final Icon addSampleIcon = GuiUtils.getIcon("addSample", 32);
	private static final Icon deleteSampleIcon = GuiUtils.getIcon("deleteSample", 32);

	private JButton addButton, removeButton;
	private MoTrPACStudy study;
	private LIMSExperiment experiment;
	private TissueCodeTable availableTissueCodesTable;
	private TissueCodeTable assignedTissueCodesTable;
	
	public MoTrPACStudyTissueCodeEditorDialog(LIMSExperiment experiment, MoTrPACStudy study, ActionListener listener) {
		super();
		this.experiment = experiment;
		this.study = study;
		setIconImage(((ImageIcon) editTissuesIcon).getImage());
		setTitle("Edit tissues for experiment \"" + experiment.getName() + "\"");
		setPreferredSize(new Dimension(800, 640));
		setSize(new Dimension(800, 640));
		setResizable(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setModalityType(ModalityType.APPLICATION_MODAL);
		
		// TODO Auto-generated constructor stub
		JPanel contents = new JPanel();
		contents.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(contents, BorderLayout.CENTER);
		
		GridBagLayout gbl_contents = new GridBagLayout();
		gbl_contents.columnWidths = new int[]{347, -32, 100};
		gbl_contents.rowHeights = new int[]{0, 0, 0, 0, 0};
		gbl_contents.columnWeights = new double[]{0.0, 0.0, 1.0};
		gbl_contents.rowWeights = new double[]{0.0, 1.0, 1.0, 1.0, 1.0};
		contents.setLayout(gbl_contents);

		JLabel lblAvailableReferenceSamples = new JLabel("Available tissues");
		GridBagConstraints gbc_lblAvailableReferenceSamples = new GridBagConstraints();
		gbc_lblAvailableReferenceSamples.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblAvailableReferenceSamples.insets = new Insets(0, 0, 5, 5);
		gbc_lblAvailableReferenceSamples.gridx = 0;
		gbc_lblAvailableReferenceSamples.gridy = 0;
		contents.add(lblAvailableReferenceSamples, gbc_lblAvailableReferenceSamples);

		JLabel lblReferenceSamplesPresent = new JLabel("Assigned tissues");
		GridBagConstraints gbc_lblReferenceSamplesPresent = new GridBagConstraints();
		gbc_lblReferenceSamplesPresent.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblReferenceSamplesPresent.insets = new Insets(0, 0, 5, 0);
		gbc_lblReferenceSamplesPresent.gridx = 2;
		gbc_lblReferenceSamplesPresent.gridy = 0;
		contents.add(lblReferenceSamplesPresent, gbc_lblReferenceSamplesPresent);

		availableTissueCodesTable = new TissueCodeTable(); 
		availableTissueCodesTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		JScrollPane scrollPane = new JScrollPane(availableTissueCodesTable);
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridheight = 4;
		gbc_scrollPane.insets = new Insets(0, 0, 0, 5);
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 1;
		contents.add(scrollPane, gbc_scrollPane);

		assignedTissueCodesTable = new TissueCodeTable();
		assignedTissueCodesTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		JScrollPane scrollPane_1 = new JScrollPane(assignedTissueCodesTable);
		GridBagConstraints gbc_scrollPane_1 = new GridBagConstraints();
		gbc_scrollPane_1.fill = GridBagConstraints.BOTH;
		gbc_scrollPane_1.gridheight = 4;
		gbc_scrollPane_1.gridx = 2;
		gbc_scrollPane_1.gridy = 1;
		contents.add(scrollPane_1, gbc_scrollPane_1);
		
		addButton = new JButton(addSampleIcon);
		addButton.setActionCommand(MainActionCommands.ADD_MOTRPAC_STUDY_TISSUES_COMMAND.getName());
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
		removeButton.setActionCommand(MainActionCommands.REMOVE_MOTRPAC_STUDY_TISSUES_COMMAND.getName());
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

		JButton saveButton = new JButton(MainActionCommands.SAVE_MOTRPAC_STUDY_TISSUES_COMMAND.getName());
		saveButton.setActionCommand(MainActionCommands.SAVE_MOTRPAC_STUDY_TISSUES_COMMAND.getName());
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
		
		pack();
	}
	
	public void loadTissueCodes(Collection<MoTrPACTissueCode> experimentTissueCodes) {
		
		Collection<MoTrPACTissueCode>availableCodes = 
				MoTrPACDatabaseCache.getMotrpacTissueCodeList().stream().
				filter(f -> !experimentTissueCodes.contains(f) ).
				collect(Collectors.toList());
		
		availableTissueCodesTable.setTableModelFromTissueCodes(availableCodes);
		assignedTissueCodesTable.setTableModelFromTissueCodes(experimentTissueCodes);
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		String command = e.getActionCommand();
		if(command.equals(MainActionCommands.ADD_MOTRPAC_STUDY_TISSUES_COMMAND.getName()))
			addSelectedTissues();
		
		if(command.equals(MainActionCommands.REMOVE_MOTRPAC_STUDY_TISSUES_COMMAND.getName()))
			removeSelectedTissues();		
	}
	
	private void addSelectedTissues() {
		
		Collection<MoTrPACTissueCode> selectedCodes = availableTissueCodesTable.getSelectedCodes();
		if(selectedCodes.isEmpty())
			return;
		
		Collection<MoTrPACTissueCode>assignedCodes = assignedTissueCodesTable.getAllCodes();
		assignedCodes.addAll(selectedCodes);
		
		Collection<MoTrPACTissueCode>availableCodes = availableTissueCodesTable.getAllCodes();
		availableCodes.removeAll(selectedCodes);
		
		availableTissueCodesTable.setTableModelFromTissueCodes(new TreeSet<MoTrPACTissueCode>(availableCodes));
		assignedTissueCodesTable.setTableModelFromTissueCodes(new TreeSet<MoTrPACTissueCode>(assignedCodes));
	}
	
	private void removeSelectedTissues() {
		
		Collection<MoTrPACTissueCode> selectedCodes = assignedTissueCodesTable.getSelectedCodes();
		if(selectedCodes.isEmpty())
			return;
		
		Collection<MoTrPACTissueCode>assignedCodes = assignedTissueCodesTable.getAllCodes();
		assignedCodes.removeAll(selectedCodes);
		
		Collection<MoTrPACTissueCode>availableCodes = availableTissueCodesTable.getAllCodes();
		availableCodes.addAll(selectedCodes);
		
		availableTissueCodesTable.setTableModelFromTissueCodes(new TreeSet<MoTrPACTissueCode>(availableCodes));
		assignedTissueCodesTable.setTableModelFromTissueCodes(new TreeSet<MoTrPACTissueCode>(assignedCodes));
	}
	
	public Collection<MoTrPACTissueCode>getAssignedTissueCodes(){
		return assignedTissueCodesTable.getAllCodes();
	}

	public LIMSExperiment getExperiment() {
		return experiment;
	}
}











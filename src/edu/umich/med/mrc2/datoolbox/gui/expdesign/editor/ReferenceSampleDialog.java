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

package edu.umich.med.mrc2.datoolbox.gui.expdesign.editor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesign;
import edu.umich.med.mrc2.datoolbox.data.ExperimentalSample;
import edu.umich.med.mrc2.datoolbox.data.enums.ParameterSetStatus;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSExperiment;
import edu.umich.med.mrc2.datoolbox.database.idt.ReferenceSamplesManager;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.ExperimentalSampleListCellRenderer;
import edu.umich.med.mrc2.datoolbox.gui.utils.CommonToolbar;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.SortedListModel;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class ReferenceSampleDialog extends JDialog implements ActionListener{

	/**
	 *
	 */
	private static final long serialVersionUID = -6105883973403855121L;
	private static final Icon rsEditIcon = GuiUtils.getIcon("editSample", 32);
	private static final Icon addSampleIcon = GuiUtils.getIcon("addSample", 32);
	private static final Icon deleteSampleIcon = GuiUtils.getIcon("deleteSample", 32);

	private JButton addButton, removeButton;
	private JList availableReferenceList;
	private JList usedReferenceList;

	private JButton btnSaveChanges;

	DataAnalysisProject cefAnalyzerProject;
	LIMSExperiment limsExperiment;
	ActionListener listener;

	public ReferenceSampleDialog(ActionListener parent, LIMSExperiment experiment) {

		super();
		this.limsExperiment = experiment;
		this.listener = parent;
		initGui();
		populateSampleListsFromLimsExperiment(limsExperiment);
		pack();
	}

	public ReferenceSampleDialog(ActionListener parent, DataAnalysisProject project) {

		super();
		this.cefAnalyzerProject = project;
		this.listener = parent;
		initGui();
		populateSampleListsFromCefAnalyzerProject(cefAnalyzerProject);
		pack();
	}

	private void initGui() {

		setTitle("Edit reference sample(s)");
		setIconImage(((ImageIcon) rsEditIcon).getImage());
		setModalityType(ModalityType.APPLICATION_MODAL);
		setSize(new Dimension(600, 400));
		setResizable(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(panel, BorderLayout.CENTER);
		
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{200, 40, 100, 100};
		gbl_panel.rowHeights = new int[]{0, 0, 0, 0, 0, 0};
		gbl_panel.columnWeights = new double[]{1.0, 0.2, 1.0, 1.0};
		gbl_panel.rowWeights = new double[]{0.0, 1.0, 1.0, 1.0, 1.0, 0.0};
		panel.setLayout(gbl_panel);

		JLabel lblAvailableReferenceSamples = new JLabel("Available reference samples");
		GridBagConstraints gbc_lblAvailableReferenceSamples = new GridBagConstraints();
		gbc_lblAvailableReferenceSamples.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblAvailableReferenceSamples.insets = new Insets(0, 0, 5, 5);
		gbc_lblAvailableReferenceSamples.gridx = 0;
		gbc_lblAvailableReferenceSamples.gridy = 0;
		panel.add(lblAvailableReferenceSamples, gbc_lblAvailableReferenceSamples);

		JLabel lblReferenceSamplesPresent = new JLabel("Used reference samples");
		GridBagConstraints gbc_lblReferenceSamplesPresent = new GridBagConstraints();
		gbc_lblReferenceSamplesPresent.gridwidth = 2;
		gbc_lblReferenceSamplesPresent.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblReferenceSamplesPresent.insets = new Insets(0, 0, 5, 0);
		gbc_lblReferenceSamplesPresent.gridx = 2;
		gbc_lblReferenceSamplesPresent.gridy = 0;
		panel.add(lblReferenceSamplesPresent, gbc_lblReferenceSamplesPresent);

		ExperimentalSampleListCellRenderer slr = new ExperimentalSampleListCellRenderer();
		availableReferenceList = new JList<ExperimentalSample>();
		availableReferenceList.setCellRenderer(slr);
		availableReferenceList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		JScrollPane scrollPane = new JScrollPane(availableReferenceList);
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridheight = 4;
		gbc_scrollPane.insets = new Insets(0, 0, 5, 5);
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 1;
		panel.add(scrollPane, gbc_scrollPane);

		usedReferenceList = new JList<ExperimentalSample>();
		usedReferenceList.setCellRenderer(slr);
		usedReferenceList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		JScrollPane scrollPane_1 = new JScrollPane(usedReferenceList);
		GridBagConstraints gbc_scrollPane_1 = new GridBagConstraints();
		gbc_scrollPane_1.fill = GridBagConstraints.BOTH;
		gbc_scrollPane_1.gridheight = 4;
		gbc_scrollPane_1.gridwidth = 2;
		gbc_scrollPane_1.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane_1.gridx = 2;
		gbc_scrollPane_1.gridy = 1;
		panel.add(scrollPane_1, gbc_scrollPane_1);

		addButton = new JButton(addSampleIcon);
		addButton.setActionCommand(MainActionCommands.ADD_REFERENCE_SAMPLES.getName());
		addButton.addActionListener(this);
		addButton.setSize(CommonToolbar.buttonDimension);
		addButton.setPreferredSize(CommonToolbar.buttonDimension);
		GridBagConstraints gbc_addButton = new GridBagConstraints();
		gbc_addButton.anchor = GridBagConstraints.SOUTH;
		gbc_addButton.insets = new Insets(0, 0, 5, 5);
		gbc_addButton.gridx = 1;
		gbc_addButton.gridy = 2;
		panel.add(addButton, gbc_addButton);

		removeButton = new JButton(deleteSampleIcon);
		removeButton.setActionCommand(MainActionCommands.REMOVE_REFERENCE_SAMPLES.getName());
		removeButton.addActionListener(this);
		removeButton.setSize(CommonToolbar.buttonDimension);
		removeButton.setPreferredSize(CommonToolbar.buttonDimension);
		GridBagConstraints gbc_removeButton = new GridBagConstraints();
		gbc_removeButton.anchor = GridBagConstraints.NORTH;
		gbc_removeButton.insets = new Insets(0, 0, 5, 5);
		gbc_removeButton.gridx = 1;
		gbc_removeButton.gridy = 3;
		panel.add(removeButton, gbc_removeButton);

		JButton btnCancel = new JButton("Cancel");
		GridBagConstraints gbc_btnCancel = new GridBagConstraints();
		gbc_btnCancel.insets = new Insets(0, 0, 0, 5);
		gbc_btnCancel.gridx = 2;
		gbc_btnCancel.gridy = 5;
		panel.add(btnCancel, gbc_btnCancel);

		btnSaveChanges = new JButton("Save changes");
		btnSaveChanges.setActionCommand(MainActionCommands.EDIT_REFERENCE_SAMPLES_COMMAND.getName());
		btnSaveChanges.addActionListener(listener);
		GridBagConstraints gbc_btnSaveChanges = new GridBagConstraints();
		gbc_btnSaveChanges.gridx = 3;
		gbc_btnSaveChanges.gridy = 5;
		panel.add(btnSaveChanges, gbc_btnSaveChanges);
		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				dispose();
			}
		};
		btnCancel.addActionListener(al);
		JRootPane rootPane = SwingUtilities.getRootPane(btnSaveChanges);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(btnSaveChanges);
	}


	@SuppressWarnings("unchecked")
	private void populateSampleListsFromLimsExperiment(LIMSExperiment limsExperiment2) {

		if(ReferenceSamplesManager.getReferenceSamples() == null || limsExperiment2 == null)
			return;

		SortedListModel<ExperimentalSample> availableReferenceListModel = new SortedListModel<ExperimentalSample>();
		SortedListModel<ExperimentalSample> usedReferenceListModel = new SortedListModel<ExperimentalSample>();
		Collection<ExperimentalSample>available = new ArrayList<ExperimentalSample>();
		Collection<ExperimentalSample>used = new ArrayList<ExperimentalSample>();
		for(ExperimentalSample ref : ReferenceSamplesManager.getReferenceSamples()) {

			if(limsExperiment2.getExperimentDesign().containsSample(ref))
				used.add(ref);
			else
				available.add(ref);
		}
		usedReferenceListModel.addAll(used);
		availableReferenceListModel.addAll(available);
		availableReferenceList.setModel(availableReferenceListModel);
		usedReferenceList.setModel(usedReferenceListModel);
	}

	@SuppressWarnings("unchecked")
	private void populateSampleListsFromCefAnalyzerProject(DataAnalysisProject cefAnalyzerProject2) {

		if(ReferenceSamplesManager.getReferenceSamples() == null || cefAnalyzerProject2 == null)
			return;

		SortedListModel<ExperimentalSample> availableReferenceListModel = new SortedListModel<ExperimentalSample>();
		SortedListModel<ExperimentalSample> usedReferenceListModel = new SortedListModel<ExperimentalSample>();
		Collection<ExperimentalSample>available = new ArrayList<ExperimentalSample>();
		Collection<ExperimentalSample>used = new ArrayList<ExperimentalSample>();
		for(ExperimentalSample ref : ReferenceSamplesManager.getReferenceSamples()) {

			if(cefAnalyzerProject2.getExperimentDesign().containsSample(ref))
				used.add(ref);
			else
				available.add(ref);
		}
		usedReferenceListModel.addAll(used);
		availableReferenceListModel.addAll(available);
		availableReferenceList.setModel(availableReferenceListModel);
		usedReferenceList.setModel(usedReferenceListModel);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void actionPerformed(ActionEvent e) {

		if(e.getActionCommand().equals(MainActionCommands.ADD_REFERENCE_SAMPLES.getName())) {

			List<ExperimentalSample>selected = availableReferenceList.getSelectedValuesList();
			((SortedListModel)usedReferenceList.getModel()).addAll(selected);

			for(ExperimentalSample ref : selected)
				((SortedListModel)availableReferenceList.getModel()).removeElement(ref);
		}
		if(e.getActionCommand().equals(MainActionCommands.REMOVE_REFERENCE_SAMPLES.getName())) {

			List<ExperimentalSample>selected = usedReferenceList.getSelectedValuesList();
			((SortedListModel)availableReferenceList.getModel()).addAll(selected);

			for(ExperimentalSample ref : selected)
				((SortedListModel)usedReferenceList.getModel()).removeElement(ref);
		}
	}

	public Collection<ExperimentalSample>getAvailableReferenceSamples(){

		@SuppressWarnings("rawtypes")
		SortedListModel model = ((SortedListModel)availableReferenceList.getModel());
        TreeSet<ExperimentalSample> items = new TreeSet<ExperimentalSample>();
		int size = model.getSize();
		for (int i = 0; i < size; i++)
			items.add((ExperimentalSample) model.getElementAt(i));

		return items;
	}

	public Collection<ExperimentalSample>getUsedReferenceSamples(){

		@SuppressWarnings("rawtypes")
		SortedListModel model = ((SortedListModel)usedReferenceList.getModel());
        TreeSet<ExperimentalSample> items = new TreeSet<ExperimentalSample>();
		int size = model.getSize();
		for (int i = 0; i < size; i++)
			items.add((ExperimentalSample) model.getElementAt(i));

		return items;
	}
	
	public void editReferenceSamples() {
		
		DataAnalysisProject currentProject = MRC2ToolBoxCore.getCurrentProject();
		if(currentProject == null)
			return;
		
		ExperimentDesign experimentDesign = currentProject.getExperimentDesign();
		if(experimentDesign == null)
			return;
		
		experimentDesign.setSuppressEvents(true);		
		Collection<ExperimentalSample> presentRefs = experimentDesign.getReferenceSamples();
		Collection<ExperimentalSample> selectedRefs = getUsedReferenceSamples();		
		Set<ExperimentalSample> samplesToAdd = 
				selectedRefs.stream().filter(r -> !presentRefs.contains(r)).collect(Collectors.toSet());
		Set<ExperimentalSample> samplesToRemove = 
				presentRefs.stream().filter(r -> !selectedRefs.contains(r)).collect(Collectors.toSet());
		
		if(!samplesToRemove.isEmpty())
			experimentDesign.removeReferenceSamples(samplesToRemove);
		
		if(!samplesToAdd.isEmpty())
			experimentDesign.addReferenceSamples(samplesToAdd);
		
		Collection<DataFile> projectFiles = currentProject.getAllDataFiles();
		for(ExperimentalSample sample : experimentDesign.getSamples()) {

			Collection<DataFile> misingFiles =
					sample.getDataFilesMap().values().
					stream().flatMap(m -> m.stream()).filter(s -> !projectFiles.contains(s)).
					collect(Collectors.toList());

			misingFiles.stream().forEach(f -> sample.removeDataFile(f));
		}
		experimentDesign.setSuppressEvents(false);
		experimentDesign.fireExperimentDesignEvent(ParameterSetStatus.CHANGED);
	}
}






































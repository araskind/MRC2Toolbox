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

package edu.umich.med.mrc2.datoolbox.gui.projectsetup.expdesign;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.apache.commons.lang3.StringUtils;

import edu.umich.med.mrc2.datoolbox.data.ExperimentDesign;
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignLevel;
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignSubset;
import edu.umich.med.mrc2.datoolbox.data.Renamable;
import edu.umich.med.mrc2.datoolbox.data.enums.ParameterSetStatus;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.communication.ExperimentDesignEvent;
import edu.umich.med.mrc2.datoolbox.gui.communication.ExperimentDesignSubsetEvent;
import edu.umich.med.mrc2.datoolbox.gui.communication.ExperimentDesignSubsetListener;
import edu.umich.med.mrc2.datoolbox.gui.communication.FeatureSetEvent;
import edu.umich.med.mrc2.datoolbox.gui.communication.MsFeatureEvent;
import edu.umich.med.mrc2.datoolbox.gui.fdata.FeatureDataPanel;
import edu.umich.med.mrc2.datoolbox.gui.main.DockableMRC2ToolboxPanel;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.main.PanelList;
import edu.umich.med.mrc2.datoolbox.gui.projectsetup.expdesign.editor.ProjectDesignEditorPanel;
import edu.umich.med.mrc2.datoolbox.gui.projectsetup.expdesign.stucturedit.ExpDesignEditorDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.RenameDialog;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskEvent;

public class DesignSubsetPanel extends DockableMRC2ToolboxPanel implements TableModelListener, ListSelectionListener {

	/**
	 *
	 */
	private static final long serialVersionUID = 8912035949876480069L;

	private DesignSubsetToolbar toolbar;
	private DesignSubsetTable designSubsetTable;
	private JScrollPane scrollPane, groupsScrollPane;
	private JPanel wrapper;
	private ListSelectionModel subsetSelectionModel;

	private ExperimentDesignSubset activeSet;
	private LinkedList<ExperimentDesignSubsetListener> setListeners;
	private boolean linked;
	private boolean creatingNewSet;

	private ExpDesignEditorDialog expDesignEditorDialog;

	private ProjectDesignEditorPanel designEditorPanel;
	private static final Icon componentIcon = GuiUtils.getIcon("editDesignSubset", 16);
	private static final File layoutConfigFile = new File(MRC2ToolBoxCore.configDir + "DesignSubsetPanel.layout");

	public DesignSubsetPanel() {

		super("DesignSubsetPanel", "Experiment design subsets", componentIcon);
		setLayout(new BorderLayout(0, 0));

		setListeners = new LinkedList<ExperimentDesignSubsetListener>();

		toolbar = new DesignSubsetToolbar(this);
		add(toolbar, BorderLayout.NORTH);

		wrapper = new JPanel();
		wrapper.setBorder(new EmptyBorder(10, 10, 10, 10));
		add(wrapper, BorderLayout.CENTER);
		wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));

		scrollPane = new JScrollPane();
		scrollPane.setBorder(new EmptyBorder(0, 0, 20, 0));
		designSubsetTable = new DesignSubsetTable(this);
		designSubsetTable.setBorder(new EmptyBorder(10, 0, 20, 0));

		subsetSelectionModel = designSubsetTable.getSelectionModel();
		subsetSelectionModel.addListSelectionListener(this);

		scrollPane.add(designSubsetTable);
		scrollPane.setViewportView(designSubsetTable);
		wrapper.add(scrollPane);

		designEditorPanel = new ProjectDesignEditorPanel();
		wrapper.add(designEditorPanel);

		expDesignEditorDialog = new ExpDesignEditorDialog(this);
		initActions();
		loadLayout(layoutConfigFile);
	}

	@Override
	public void actionPerformed(ActionEvent event) {

		if (event.getActionCommand().equals(MainActionCommands.SHOW_DESIGN_SUBSET_DIALOG_COMMAND.getName())) {

			creatingNewSet = true;
			expDesignEditorDialog.setEditingEnabled(true);
			expDesignEditorDialog.loadCompleteDesign();
			expDesignEditorDialog.setLocationRelativeTo(MRC2ToolBoxCore.getMainWindow());
			expDesignEditorDialog.setVisible(true);
		}
		if (event.getActionCommand().equals(MainActionCommands.NEW_DESIGN_SUBSET_COMMAND.getName()))
			createNewDesignSubset();

		if (event.getActionCommand().equals(MainActionCommands.VIEW_DESIGN_SUBSET_COMMAND.getName()))
			modifySelectedDesignSubset(MainActionCommands.VIEW_DESIGN_SUBSET_COMMAND.getName());

		if (event.getActionCommand().equals(MainActionCommands.COPY_DESIGN_SUBSET_COMMAND.getName()))
			modifySelectedDesignSubset(MainActionCommands.COPY_DESIGN_SUBSET_COMMAND.getName());

		if (event.getActionCommand().equals(MainActionCommands.RENAME_DESIGN_SUBSET_COMMAND.getName()))
			modifySelectedDesignSubset(MainActionCommands.RENAME_DESIGN_SUBSET_COMMAND.getName());

		if (event.getActionCommand().equals(MainActionCommands.DELETE_DESIGN_SUBSET_COMMAND.getName()))
			modifySelectedDesignSubset(MainActionCommands.DELETE_DESIGN_SUBSET_COMMAND.getName());

		if (event.getActionCommand().equals(MainActionCommands.LOCK_DESIGN_SUBSET_COMMAND.getName()))
			modifySelectedDesignSubset(MainActionCommands.LOCK_DESIGN_SUBSET_COMMAND.getName());

		if (event.getActionCommand().equals(MainActionCommands.UNLOCK_DESIGN_SUBSET_COMMAND.getName()))
			modifySelectedDesignSubset(MainActionCommands.UNLOCK_DESIGN_SUBSET_COMMAND.getName());

		if (event.getActionCommand().equals(MainActionCommands.LINK_FEATURE_SUBSET_COMMAND.getName()))
			linkToFeatureDataPanel();

		if (event.getActionCommand().equals(MainActionCommands.UNLINK_FEATURE_SUBSET_COMMAND.getName()))
			unlinkFromFeatureDataPanel();
	}

	private void createNewDesignSubset() {

		ArrayList<String>messages = new ArrayList<String>();
		String subsetName = expDesignEditorDialog.getSubsetName();
		ExperimentDesign design = currentProject.getExperimentDesign();

		if(subsetName.isEmpty())
			messages.add("Name can not be empty!");

		for(ExperimentDesignSubset subset : design.getDesignSubsets()) {

			if(subset.getName().equals(subsetName))
				messages.add("Subset with this name already exists!");
		}
		Set<ExperimentDesignLevel>selectedLevels = expDesignEditorDialog.getSelectedLevels();

		if(selectedLevels.isEmpty())
			messages.add("No experimental levels is selected!");

		if(!messages.isEmpty()) {

			MessageDialog.showErrorMsg(StringUtils.join(messages, "\n"));
			return;
		}
		ExperimentDesignSubset newSubset = new ExperimentDesignSubset(subsetName);
		for(ExperimentDesignLevel newLevel : selectedLevels)
			newSubset.addLevel(newLevel);

		design.addDesignSubset(newSubset);
		design.setActiveDesignSubset(newSubset);
		expDesignEditorDialog.setVisible(false);
		designSubsetTable.setModelFromProject(currentProject);
		designSubsetTable.selectActiveSubset();
	}

	private void modifySelectedDesignSubset(String modificationCommand) {

		if (designSubsetTable.getSelectedRow() > -1) {

			ExperimentDesignSubset selectedSubset = (ExperimentDesignSubset) designSubsetTable
					.getValueAt(designSubsetTable.getSelectedRow(), 1);

			//	View
			if(modificationCommand.equals(MainActionCommands.VIEW_DESIGN_SUBSET_COMMAND.getName())) {

				expDesignEditorDialog.setEditingEnabled(false);
				expDesignEditorDialog.loadDesignSubset(selectedSubset);
				expDesignEditorDialog.setLocationRelativeTo(MRC2ToolBoxCore.getMainWindow());
				expDesignEditorDialog.setVisible(true);
				return;
			}
			//	Copy
			if(modificationCommand.equals(MainActionCommands.COPY_DESIGN_SUBSET_COMMAND.getName())) {

				ExperimentDesignSubset copySubset = new ExperimentDesignSubset(selectedSubset.getName() + " copy");
				for(ExperimentDesignLevel level : selectedSubset.getDesignMap())
					copySubset.addLevel(level);

				Collection<Renamable>allSets = new TreeSet<Renamable>();
				allSets.addAll(currentProject.getExperimentDesign().getDesignSubsets());

				RenameDialog rnd = new RenameDialog("Name new design subset", copySubset, allSets);

				if(copySubset.nameIsValid()) {

					currentProject.getExperimentDesign().addDesignSubset(copySubset);
					switchDataPipeline(currentProject, activeDataPipeline);
				}
				return;
			}
			//	Lock/unlock
			if(modificationCommand.equals(MainActionCommands.LOCK_DESIGN_SUBSET_COMMAND.getName())) {

				selectedSubset.setLocked(true);
				toolbar.setToolbarState(selectedSubset);
				designEditorPanel.setEditingAllowed(false);
				return;
			}
			if(modificationCommand.equals(MainActionCommands.UNLOCK_DESIGN_SUBSET_COMMAND.getName())) {

				selectedSubset.setLocked(false);
				toolbar.setToolbarState(selectedSubset);
				designEditorPanel.setEditingAllowed(true);
				return;
			}
			if (selectedSubset.isLocked()) {
				MessageDialog.showErrorMsg(
						"Design '" + selectedSubset.getName() + "' is locked and can not be modified or deleted!");
			}
			else {
				//	Delete subset
				if(modificationCommand.equals(MainActionCommands.DELETE_DESIGN_SUBSET_COMMAND.getName())) {

					int delete = MessageDialog.showChoiceMsg(
							"Are you sure you want to delete '" + selectedSubset.getName() + "' design subset?");

					if (delete == JOptionPane.YES_OPTION) {

						currentProject.getExperimentDesign().removeDesignSubset(selectedSubset);
						switchDataPipeline(currentProject, activeDataPipeline);
						fireExpDesignSetEvent(activeSet, ParameterSetStatus.ENABLED);
						return;
					}
					else return;
				}
				//	Rename
				if(modificationCommand.equals(MainActionCommands.RENAME_DESIGN_SUBSET_COMMAND.getName())) {

					Collection<Renamable>allSets = new TreeSet<Renamable>();
					allSets.addAll(currentProject.getExperimentDesign().getDesignSubsets());
					RenameDialog rnd = new RenameDialog("Rename design subset", selectedSubset, allSets);

					if(selectedSubset.nameIsValid()) {

						fireExpDesignSetEvent(selectedSubset, ParameterSetStatus.CHANGED);
						switchDataPipeline(currentProject, activeDataPipeline);
						return;
					}
				}
			}
		}
	}

	private void fireExpDesignSetEvent(
			ExperimentDesignSubset activeSet2, ParameterSetStatus changed) {

		ExperimentDesignSubsetEvent event = new ExperimentDesignSubsetEvent(activeSet2, changed);

		for (ExperimentDesignSubsetListener t : this.setListeners)
			t.designSetStatusChanged(event);
	}

	private void linkToFeatureDataPanel() {

		linked = true;
		toolbar.setLinkedState(linked);

		// TODO
		setListeners.add(((FeatureDataPanel) 
				MRC2ToolBoxCore.getMainWindow().getPanel(PanelList.FEATURE_DATA)));
		fireExpDesignSetEvent(activeSet, ParameterSetStatus.CHANGED);
	}

	private void saveDesignSubsetData() {

		String subsetName = expDesignEditorDialog.getSubsetName();
		if(subsetName.isEmpty()){

			MessageDialog.showErrorMsg("Name can not be empty!", expDesignEditorDialog);
			return;
		}
		Set<ExperimentDesignLevel>selectedLevels = expDesignEditorDialog.getSelectedLevels();
		if(selectedLevels.isEmpty()){

			MessageDialog.showErrorMsg("No experimental levels is selected!", expDesignEditorDialog);
			return;
		}
		if(creatingNewSet){ //	New subset

			ExperimentDesignSubset newSubset = new ExperimentDesignSubset(subsetName);
			newSubset.getDesignMap().addAll(selectedLevels);
			currentProject.getExperimentDesign().addDesignSubset(newSubset);
			currentProject.getExperimentDesign().setActiveDesignSubset(newSubset);
			expDesignEditorDialog.setVisible(false);
			designSubsetTable.setModelFromProject(currentProject);
//			groupsTable.setModelFromDesignSubset(newSubset);
		}
		else{ //	Existing subset
			ExperimentDesignSubset selectedSubset = (ExperimentDesignSubset) designSubsetTable
					.getValueAt(designSubsetTable.getSelectedRow(), 1);

			selectedSubset.setName(subsetName);
			selectedSubset.getDesignMap().clear();
			selectedSubset.getDesignMap().addAll(selectedLevels);
			expDesignEditorDialog.setVisible(false);
			designSubsetTable.setModelFromProject(currentProject);
//			groupsTable.setModelFromDesignSubset(selectedSubset);
		}
	}

	@Override
	public void tableChanged(TableModelEvent e) {
		// TODO Auto-generated method stub
		System.out.println();
	}

	private void unlinkFromFeatureDataPanel() {

		linked = false;
		toolbar.setLinkedState(linked);

		// TODO
		setListeners
				.remove(((FeatureDataPanel) MRC2ToolBoxCore.getMainWindow().getPanel(PanelList.FEATURE_DATA)));
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {

		if(!e.getValueIsAdjusting()) {

			int selected = designSubsetTable.getSelectedRow();

			if (selected > -1) {

				ExperimentDesignSubset selectedSubset =
						(ExperimentDesignSubset) designSubsetTable.getValueAt(selected, 1);
				designEditorPanel.loadDesignSubset(selectedSubset);
				toolbar.setToolbarState(selectedSubset);
			}
		}
	}

	@Override
	public void reloadDesign() {
		switchDataPipeline(currentProject, activeDataPipeline);
	}

	@Override
	public void switchDataPipeline(DataAnalysisProject project, DataPipeline newPipeline) {

		clearPanel();
		super.switchDataPipeline(project, newPipeline);
		activeSet = null;
		designSubsetTable.setModelFromProject(currentProject);
		scrollPane.setPreferredSize(designSubsetTable.getPreferredScrollableViewportSize());
		if(currentProject !=  null) {

			activeSet = currentProject.getExperimentDesign().getActiveDesignSubset();
			if(activeSet != null) {

				designSubsetTable.selectActiveSubset();
				toolbar.setToolbarState(activeSet);
			}
		}
	}

	@Override
	public void closeProject() {

		super.closeProject();
		clearPanel();
		toolbar.updateGuiFromProjectAndDataPipeline(null, null);
	}

	@Override
	public synchronized void clearPanel() {
		// TODO Auto-generated method stub

		designSubsetTable.clearTable();
		designEditorPanel.clearPanel();
	}

	@Override
	public void statusChanged(TaskEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void designStatusChanged(ExperimentDesignEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void designSetStatusChanged(ExperimentDesignSubsetEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void msFeatureStatusChanged(MsFeatureEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void featureSetStatusChanged(FeatureSetEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public File getLayoutFile() {
		return layoutConfigFile;
	}

	@Override
	protected void initActions() {
		// TODO Auto-generated method stub
		
	}
}

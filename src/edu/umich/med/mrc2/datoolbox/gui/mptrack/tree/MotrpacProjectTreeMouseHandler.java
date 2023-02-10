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

package edu.umich.med.mrc2.datoolbox.gui.mptrack.tree;

import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Icon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import edu.umich.med.mrc2.datoolbox.data.lims.DataAcquisitionMethod;
import edu.umich.med.mrc2.datoolbox.data.lims.DataExtractionMethod;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSExperiment;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSProject;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSSamplePreparation;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class MotrpacProjectTreeMouseHandler extends MouseAdapter {

	private static final Icon deleteIcon = GuiUtils.getIcon("delete", 24);
	private static final Icon editIcon = GuiUtils.getIcon("edit", 24);

	private MotrpacProjectTree tree;
	private JPopupMenu
		projectPopupMenu,
		experimentPopupMenu,
		samplePrepPopupMenu,
		acquisitionMethodPopupMenu,
		dataExtractionMethodPopupMenu;

	private static JMenuItem
		editProjectMenuItem,
		deleteProjectMenuItem,
		editExperimentMenuItem,
		deleteExperimentMenuItem,
		editSamplePrepMenuItem,
		deleteSamplePrepMenuItem,
		editAcquisitionMethodMenuItem,
		deleteAcquisitionMethodMenuItem,
		editDataExtractionMethodMenuItem,
		deleteDataExtractionMethodMenuItem;

	public MotrpacProjectTreeMouseHandler(MotrpacProjectTree tree, ActionListener popupListener) {

		super();
		this.tree = tree;

		//	Project
		projectPopupMenu = new JPopupMenu();

		editProjectMenuItem = GuiUtils.addMenuItem(projectPopupMenu,
				MainActionCommands.EDIT_IDTRACKER_PROJECT_DIALOG_COMMAND.getName(), popupListener,
				MainActionCommands.EDIT_IDTRACKER_PROJECT_DIALOG_COMMAND.getName());
		editProjectMenuItem.setIcon(editIcon);

		deleteProjectMenuItem = GuiUtils.addMenuItem(projectPopupMenu,
				MainActionCommands.DELETE_IDTRACKER_PROJECT_COMMAND.getName(), popupListener,
				MainActionCommands.DELETE_IDTRACKER_PROJECT_COMMAND.getName());
		deleteProjectMenuItem.setIcon(deleteIcon);

		//	Experiment
		experimentPopupMenu = new JPopupMenu();

		editExperimentMenuItem = GuiUtils.addMenuItem(experimentPopupMenu,
				MainActionCommands.EDIT_IDTRACKER_EXPERIMENT_DIALOG_COMMAND.getName(), popupListener,
				MainActionCommands.EDIT_IDTRACKER_EXPERIMENT_DIALOG_COMMAND.getName());
		editExperimentMenuItem.setIcon(editIcon);

		deleteExperimentMenuItem = GuiUtils.addMenuItem(experimentPopupMenu,
				MainActionCommands.DELETE_IDTRACKER_EXPERIMENT_COMMAND.getName(), popupListener,
				MainActionCommands.DELETE_IDTRACKER_EXPERIMENT_COMMAND.getName());
		deleteExperimentMenuItem.setIcon(deleteIcon);

		//	Sample prep
		samplePrepPopupMenu = new JPopupMenu();

		editSamplePrepMenuItem = GuiUtils.addMenuItem(samplePrepPopupMenu,
				MainActionCommands.EDIT_SAMPLE_PREP_DIALOG_COMMAND.getName(), popupListener,
				MainActionCommands.EDIT_SAMPLE_PREP_DIALOG_COMMAND.getName());
		editSamplePrepMenuItem.setIcon(editIcon);

		deleteSamplePrepMenuItem = GuiUtils.addMenuItem(samplePrepPopupMenu,
				MainActionCommands.DELETE_SAMPLE_PREP_COMMAND.getName(), popupListener,
				MainActionCommands.DELETE_SAMPLE_PREP_COMMAND.getName());
		deleteSamplePrepMenuItem.setIcon(deleteIcon);

		acquisitionMethodPopupMenu = new JPopupMenu();

		//	Data acquisition
		editAcquisitionMethodMenuItem = GuiUtils.addMenuItem(acquisitionMethodPopupMenu,
				MainActionCommands.EDIT_ACQUISITION_METHOD_DIALOG_COMMAND.getName(), popupListener,
				MainActionCommands.EDIT_ACQUISITION_METHOD_DIALOG_COMMAND.getName());
		editAcquisitionMethodMenuItem.setIcon(editIcon);

		deleteAcquisitionMethodMenuItem = GuiUtils.addMenuItem(acquisitionMethodPopupMenu,
				MainActionCommands.DELETE_ACQUISITION_METHOD_COMMAND.getName(), popupListener,
				MainActionCommands.DELETE_ACQUISITION_METHOD_COMMAND.getName());
		deleteAcquisitionMethodMenuItem.setIcon(deleteIcon);

		//	Data extraction
		dataExtractionMethodPopupMenu = new JPopupMenu();

		editDataExtractionMethodMenuItem = GuiUtils.addMenuItem(dataExtractionMethodPopupMenu,
				MainActionCommands.EDIT_DATA_EXTRACTION_METHOD_DIALOG_COMMAND.getName(), popupListener,
				MainActionCommands.EDIT_DATA_EXTRACTION_METHOD_DIALOG_COMMAND.getName());
		editDataExtractionMethodMenuItem.setIcon(editIcon);

		deleteDataExtractionMethodMenuItem = GuiUtils.addMenuItem(dataExtractionMethodPopupMenu,
				MainActionCommands.DELETE_DATA_EXTRACTION_METHOD_COMMAND.getName(), popupListener,
				MainActionCommands.DELETE_DATA_EXTRACTION_METHOD_COMMAND.getName());
		deleteDataExtractionMethodMenuItem.setIcon(deleteIcon);
	}

	private void handleDoubleClickEvent(MouseEvent e) {

		TreePath clickedPath = tree.getPathForLocation(e.getX(), e.getY());

		if (clickedPath == null)
			return;

		DefaultMutableTreeNode node = (DefaultMutableTreeNode) clickedPath.getLastPathComponent();
		Object clickedObject = node.getUserObject();

		if (clickedObject instanceof LIMSProject) {

		}

		if (clickedObject instanceof LIMSExperiment) {

		}
	}

	private void handlePopupTriggerEvent(MouseEvent e) {

		TreePath clickedPath = tree.getPathForLocation(e.getX(), e.getY());
		if (clickedPath == null)
			return;

		DefaultMutableTreeNode node = (DefaultMutableTreeNode) clickedPath.getLastPathComponent();
		Object clickedObject = node.getUserObject();

		if (clickedObject instanceof LIMSProject)
			projectPopupMenu.show(e.getComponent(), e.getX(), e.getY());

		if (clickedObject instanceof LIMSExperiment)
			experimentPopupMenu.show(e.getComponent(), e.getX(), e.getY());

		if (clickedObject instanceof LIMSSamplePreparation)
			samplePrepPopupMenu.show(e.getComponent(), e.getX(), e.getY());

		if (clickedObject instanceof DataAcquisitionMethod)
			acquisitionMethodPopupMenu.show(e.getComponent(), e.getX(), e.getY());

		if (clickedObject instanceof DataExtractionMethod)
			dataExtractionMethodPopupMenu.show(e.getComponent(), e.getX(), e.getY());
	}

	public void mousePressed(MouseEvent e) {

		if (e.isPopupTrigger())
			handlePopupTriggerEvent(e);

		if ((e.getClickCount() == 2) && (e.getButton() == MouseEvent.BUTTON1))
			handleDoubleClickEvent(e);
	}

	public void mouseReleased(MouseEvent e) {
		if (e.isPopupTrigger())
			handlePopupTriggerEvent(e);
	}
}

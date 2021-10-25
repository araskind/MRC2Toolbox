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

package edu.umich.med.mrc2.datoolbox.gui.rawdata;

import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JButton;

import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.rawdata.tree.TreeGrouping;
import edu.umich.med.mrc2.datoolbox.gui.utils.CommonToolbar;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class RawDataExaminerToolbar extends CommonToolbar {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5123511136162707646L;
	
	private static final Icon newRdaProjectIcon = GuiUtils.getIcon("newRawDataAnalysisProject", 32);
	private static final Icon editRdaProjectIcon = GuiUtils.getIcon("editRawDataAnalysisProject", 32);
	private static final Icon openProjectIcon = GuiUtils.getIcon("openRawDataAnalysisProject", 32);
	private static final Icon closeProjectIcon = GuiUtils.getIcon("closeRawDataAnalysisProject", 32);
	private static final Icon saveProjectIcon = GuiUtils.getIcon("saveRawDataAnalysisProject", 32);	
	private static final Icon extractMSMSFeaturesIcon = GuiUtils.getIcon("findMSMSFeatures", 32);	
	private static final Icon expandTreeIcon = GuiUtils.getIcon("expand", 32);
	private static final Icon collapseTreeIcon = GuiUtils.getIcon("collapse", 32);
	private static final Icon byFileTreeIcon = GuiUtils.getIcon("groupByFile", 32);
	private static final Icon byTypeTreeIcon = GuiUtils.getIcon("groupByType", 32);
	private static final Icon openDataFileIcon = GuiUtils.getIcon("openDataFile", 32);
	private static final Icon closeDataFileIcon = GuiUtils.getIcon("closeDataFile", 32);
	private static final Icon msConvertIcon = GuiUtils.getIcon("msConvert", 32);
	private static final Icon indexRawFilesIcon = GuiUtils.getIcon("indexRawFiles", 32);
	

	private JButton 
		newProjectButton,	
		openProjectButton,
		closeProjectButton,
		saveProjectButton,
		editProjectButton,
		expandCollapseTreeButton, 
		toggleTreeGroupingButton,
		openDataFileButton,
		closeDataFileButton,
		msConvertButton,
		indexRawFilesButton,
		extractMSMSFeaturesButton;

	public RawDataExaminerToolbar(ActionListener commandListener2) {

		super(commandListener2);
		
//		NEW_RAW_DATA_PROJECT_COMMAND("New raw data analysis project"),
//		OPEN_RAW_DATA_PROJECT_COMMAND("Open raw data analysis pproject"),
//		CLOSE_RAW_DATA_PROJECT_COMMAND("Close raw data analysis pproject"),
//		SAVE_RAW_DATA_PROJECT_COMMAND("Save raw data analysis pproject"),

		newProjectButton = GuiUtils.addButton(this, null, newRdaProjectIcon, commandListener,
				MainActionCommands.NEW_RAW_DATA_PROJECT_SETUP_COMMAND.getName(),
				MainActionCommands.NEW_RAW_DATA_PROJECT_SETUP_COMMAND.getName(),
				buttonDimension);

		addSeparator(buttonDimension);

		openProjectButton = GuiUtils.addButton(this, null, openProjectIcon, commandListener,
				MainActionCommands.OPEN_RAW_DATA_PROJECT_COMMAND.getName(),
				MainActionCommands.OPEN_RAW_DATA_PROJECT_COMMAND.getName(),
				buttonDimension);

		closeProjectButton = GuiUtils.addButton(this, null, closeProjectIcon, commandListener,
				MainActionCommands.CLOSE_RAW_DATA_PROJECT_COMMAND.getName(),
				MainActionCommands.CLOSE_RAW_DATA_PROJECT_COMMAND.getName(),
				buttonDimension);

		saveProjectButton = GuiUtils.addButton(this, null, saveProjectIcon, commandListener,
				MainActionCommands.SAVE_RAW_DATA_PROJECT_COMMAND.getName(),
				MainActionCommands.SAVE_RAW_DATA_PROJECT_COMMAND.getName(),
				buttonDimension);
		
		editProjectButton = GuiUtils.addButton(this, null, editRdaProjectIcon, commandListener,
				MainActionCommands.EDIT_RAW_DATA_PROJECT_SETUP_COMMAND.getName(),
				MainActionCommands.EDIT_RAW_DATA_PROJECT_SETUP_COMMAND.getName(),
				buttonDimension);
		
		addSeparator(buttonDimension);
		
		extractMSMSFeaturesButton = GuiUtils.addButton(this, null, extractMSMSFeaturesIcon, commandListener,
				MainActionCommands.MSMS_FEATURE_EXTRACTION_SETUP_COMMAND.getName(),
				MainActionCommands.MSMS_FEATURE_EXTRACTION_SETUP_COMMAND.getName(),
				buttonDimension);

		addSeparator(buttonDimension);
		
		expandCollapseTreeButton = GuiUtils.addButton(this, null, expandTreeIcon, commandListener,
				MainActionCommands.EXPAND_TREE.getName(), 
				MainActionCommands.EXPAND_TREE.getName(), buttonDimension);

		toggleTreeGroupingButton = GuiUtils.addButton(this, null, byFileTreeIcon, commandListener,
				MainActionCommands.GROUP_TREE_BY_TYPE.getName(), 
				MainActionCommands.GROUP_TREE_BY_TYPE.getName(),
				buttonDimension);
		
		addSeparator(buttonDimension);
		
		openDataFileButton = GuiUtils.addButton(this, null, openDataFileIcon, commandListener,
				MainActionCommands.OPEN_RAW_DATA_FILE_COMMAND.getName(), 
				MainActionCommands.OPEN_RAW_DATA_FILE_COMMAND.getName(), buttonDimension);

		closeDataFileButton = GuiUtils.addButton(this, null, closeDataFileIcon, commandListener,
				MainActionCommands.CLOSE_RAW_DATA_FILE_COMMAND.getName(), 
				MainActionCommands.CLOSE_RAW_DATA_FILE_COMMAND.getName(),
				buttonDimension);
		
		addSeparator(buttonDimension);
		
		msConvertButton = GuiUtils.addButton(this, null, msConvertIcon, commandListener,
				MainActionCommands.SETUP_RAW_DATA_CONVERSION_COMMAND.getName(),
				MainActionCommands.SETUP_RAW_DATA_CONVERSION_COMMAND.getName(),
				buttonDimension);
		
//		addSeparator(buttonDimension);
//		
//		indexRawFilesButton= GuiUtils.addButton(this, null, indexRawFilesIcon, commandListener,
//				MainActionCommands.INDEX_RAW_DATA_REPOSITORY_COMMAND.getName(),
//				MainActionCommands.INDEX_RAW_DATA_REPOSITORY_COMMAND.getName(),
//				buttonDimension);
	}

	public void groupTree(TreeGrouping grouping) {

		if (grouping.equals(TreeGrouping.BY_DATA_FILE)) {

			toggleTreeGroupingButton.setIcon(byFileTreeIcon);
			toggleTreeGroupingButton.setActionCommand(MainActionCommands.GROUP_TREE_BY_TYPE.getName());
			toggleTreeGroupingButton.setToolTipText(MainActionCommands.GROUP_TREE_BY_TYPE.getName());
		}
		if (grouping.equals(TreeGrouping.BY_OBJECT_TYPE)) {

			toggleTreeGroupingButton.setIcon(byTypeTreeIcon);
			toggleTreeGroupingButton.setActionCommand(MainActionCommands.GROUP_TREE_BY_FILE.getName());
			toggleTreeGroupingButton.setToolTipText(MainActionCommands.GROUP_TREE_BY_FILE.getName());
		}
		//	Tree collapse tree to default state after resorting, so button is reset
		treeExpanded(false);
	}

	public void treeExpanded(boolean expanded) {

		if (expanded) {

			expandCollapseTreeButton.setIcon(collapseTreeIcon);
			expandCollapseTreeButton.setActionCommand(MainActionCommands.COLLAPSE_TREE.getName());
			expandCollapseTreeButton.setToolTipText("Collapse all file nodes");
		} else {
			expandCollapseTreeButton.setIcon(expandTreeIcon);
			expandCollapseTreeButton.setActionCommand(MainActionCommands.EXPAND_TREE.getName());
			expandCollapseTreeButton.setToolTipText("Expand all file nodes");
		}

	}

	@Override
	public void updateGuiFromProjectAndDataPipeline(DataAnalysisProject project, DataPipeline newDataPipeline) {
		// TODO Auto-generated method stub
		
	}

}

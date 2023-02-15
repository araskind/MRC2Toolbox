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

package edu.umich.med.mrc2.datoolbox.gui.idtlims.worklist;

import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class WorklistImportPopupMenu extends JPopupMenu {

	/**
	 *
	 */
	private static final long serialVersionUID = 6109578300557179546L;
	private static final Icon assignLevelIcon = GuiUtils.getIcon("dropdown", 24);
	private static final Icon assignAcqMethodIcon = GuiUtils.getIcon("addDataAcquisitionMethod", 24);
	private static final Icon assignInjVolumeIcon = GuiUtils.getIcon("samplePrep", 24);	
	private static final Icon assignInjTimeIcon = GuiUtils.getIcon("clock", 24);	
	private static final Icon deleteIcon = GuiUtils.getIcon("delete", 24);
		
	private JMenuItem 
		assignLevelsMenuItem, 
		assignAcqMethodMenuItem, 
		assignInjVolumeMenuItem, 
		assignInjTimeMenuItem,
		deleteFileMenuItem;

	public WorklistImportPopupMenu(ActionListener listener) {

		super();

		assignLevelsMenuItem = GuiUtils.addMenuItem(this,
				MainActionCommands.EDIT_DESIGN_FOR_SELECTED_SAMPLES_COMMAND.getName(), listener,
				MainActionCommands.EDIT_DESIGN_FOR_SELECTED_SAMPLES_COMMAND.getName());
		assignLevelsMenuItem.setIcon(assignLevelIcon);
		
		assignAcqMethodMenuItem = GuiUtils.addMenuItem(this,
				MainActionCommands.CHOOSE_ACQ_METHOD_FOR_SELECTED_DATA_FILES_COMMAND.getName(), listener,
				MainActionCommands.CHOOSE_ACQ_METHOD_FOR_SELECTED_DATA_FILES_COMMAND.getName());
		assignAcqMethodMenuItem.setIcon(assignAcqMethodIcon);
		
		assignInjVolumeMenuItem = GuiUtils.addMenuItem(this,
				MainActionCommands.SPECIFY_INJ_VOLUME_FOR_SELECTED_DATA_FILES_COMMAND.getName(), listener,
				MainActionCommands.SPECIFY_INJ_VOLUME_FOR_SELECTED_DATA_FILES_COMMAND.getName());
		assignInjVolumeMenuItem.setIcon(assignInjVolumeIcon);
		
		assignInjTimeMenuItem = GuiUtils.addMenuItem(this,
				MainActionCommands.SPECIFY_INJ_TIME_FOR_SELECTED_DATA_FILES_COMMAND.getName(), listener,
				MainActionCommands.SPECIFY_INJ_TIME_FOR_SELECTED_DATA_FILES_COMMAND.getName());
		assignInjTimeMenuItem.setIcon(assignInjTimeIcon);
				
		addSeparator();
		
		deleteFileMenuItem = GuiUtils.addMenuItem(this,
				MainActionCommands.DELETE_DATA_FILES_COMMAND.getName(), listener,
				MainActionCommands.DELETE_DATA_FILES_COMMAND.getName());
		deleteFileMenuItem.setIcon(deleteIcon);		
	}
}

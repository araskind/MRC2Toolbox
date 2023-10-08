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

package edu.umich.med.mrc2.datoolbox.gui.fdata;

import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JMenuItem;

import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTablePopupMenu;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class FeaturePopupMenu extends BasicTablePopupMenu {

	/**
	 *
	 */
	private static final long serialVersionUID = -6064748415801630180L;

	private static final Icon deleteFromListIcon = GuiUtils.getIcon("deleteCollection", 24);
	private static final Icon searchLibraryIcon = GuiUtils.getIcon("searchLibrary", 24);
	private static final Icon searchDatabaseIcon = GuiUtils.getIcon("searchDatabase", 24);
	private static final Icon textAnnotationIcon = GuiUtils.getIcon("edit", 24);
	private static final Icon mergeSelectedFeaturesIcon = GuiUtils.getIcon("merge", 24);
	private static final Icon clearIdentificationsIcon = GuiUtils.getIcon("clearIdentifications", 24);

	private JMenuItem mergeSelectedFeaturesMenuItem;
	private JMenuItem removeSelecteFromListMenuItem;
	private JMenuItem matchFeatureToLibraryMenuItem;
	private JMenuItem searchFeatureAgainstDatabaseMenuItem;
	private JMenuItem clearFeatureIdentificationsMenuItem;
	private JMenuItem textAnnotationMenuItem;

	public FeaturePopupMenu(
			ActionListener listener,
			BasicTable copyListener) {

		super(listener, copyListener);

		textAnnotationMenuItem = GuiUtils.addMenuItem(this,
				MainActionCommands.EDIT_FEATURE_METADATA_COMMAND.getName(), listener,
				MainActionCommands.EDIT_FEATURE_METADATA_COMMAND.getName());
		textAnnotationMenuItem.setIcon(textAnnotationIcon);

		this.addSeparator();

		mergeSelectedFeaturesMenuItem = GuiUtils.addMenuItem(this,
				MainActionCommands.MERGE_SELECTED_FEATURES_COMMAND.getName(), listener,
				MainActionCommands.MERGE_SELECTED_FEATURES_COMMAND.getName());
		mergeSelectedFeaturesMenuItem.setIcon(mergeSelectedFeaturesIcon);

		removeSelecteFromListMenuItem = GuiUtils.addMenuItem(this,
				MainActionCommands.REMOVE_SELECTED_FEATURES_FROM_SUBSET_COMMAND.getName(), listener,
				MainActionCommands.REMOVE_SELECTED_FEATURES_FROM_SUBSET_COMMAND.getName());
		removeSelecteFromListMenuItem.setIcon(deleteFromListIcon);

		this.addSeparator();

		matchFeatureToLibraryMenuItem = GuiUtils.addMenuItem(this,
				MainActionCommands.SEARCH_FEATURE_AGAINST_LIBRARY_COMMAND.getName(), listener,
				MainActionCommands.SEARCH_FEATURE_AGAINST_LIBRARY_COMMAND.getName());
		matchFeatureToLibraryMenuItem.setIcon(searchLibraryIcon);

		searchFeatureAgainstDatabaseMenuItem = GuiUtils.addMenuItem(this,
				MainActionCommands.SEARCH_FEATURE_AGAINST_DATABASE_COMMAND.getName(), listener,
				MainActionCommands.SEARCH_FEATURE_AGAINST_DATABASE_COMMAND.getName());
		searchFeatureAgainstDatabaseMenuItem.setIcon(searchDatabaseIcon);

		clearFeatureIdentificationsMenuItem = GuiUtils.addMenuItem(this,
				MainActionCommands.CLEAR_SELECTED_FEATURE_IDENTIFICATION_COMMAND.getName(), listener,
				MainActionCommands.CLEAR_SELECTED_FEATURE_IDENTIFICATION_COMMAND.getName());
		clearFeatureIdentificationsMenuItem.setIcon(clearIdentificationsIcon);
		
		addCopyBlock();
	}
}

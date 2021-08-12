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

package edu.umich.med.mrc2.datoolbox.gui.library;

import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class LibraryFeaturePopupMenu extends JPopupMenu {

	/**
	 *
	 */
	private static final long serialVersionUID = 4117453365511909305L;

	private static final Icon newFeatureIcon = GuiUtils.getIcon("newLibraryFeature", 24);
	private static final Icon duplicateIcon = GuiUtils.getIcon("duplicates", 24);
	private static final Icon deleteFeatureIcon = GuiUtils.getIcon("deleteFeature", 24);
	private static final Icon searchDatabaseByIdIcon = GuiUtils.getIcon("searchDatabase", 24);
	private static final Icon searchDatabaseByFormulaIcon = GuiUtils.getIcon("searchDatabaseByFormula", 24);
	private static final Icon searchDatabaseByMassIcon = GuiUtils.getIcon("searchDatabaseByMass", 24);
	private static final Icon copyFormulaIcon = GuiUtils.getIcon("copyFormula", 24);
	private static final Icon copyAccessionIcon = GuiUtils.getIcon("copyId", 24);
	private static final Icon copyNameIcon = GuiUtils.getIcon("copyName", 24);

	@SuppressWarnings("unused")
	private JMenuItem
		newFeatureMenuItem,
		duplicateFeatureMenuItem,
		deleteFeatureMenuItem,
		searchDatabaseByAccessionMenuItem,
		searchFeatureFormulaAgainstDatabaseMenuItem,
		searchFeatureMassAgainstDatabaseMenuItem,
		copyFormulaMenuItem,
		copyAccessionMenuItem,
		copyNameMenuItem;

	public LibraryFeaturePopupMenu(ActionListener listener) {

		super();

		newFeatureMenuItem = GuiUtils.addMenuItem(this,
				MainActionCommands.NEW_LIBRARY_FEATURE_DIAOG_COMMAND.getName(), listener,
				MainActionCommands.NEW_LIBRARY_FEATURE_DIAOG_COMMAND.getName(), 0, false,
				newFeatureIcon);
		newFeatureMenuItem.setEnabled(false);

		duplicateFeatureMenuItem = GuiUtils.addMenuItem(this,
				MainActionCommands.DUPLICATE_LIBRARY_FEATURE_COMMAND.getName(), listener,
				MainActionCommands.DUPLICATE_LIBRARY_FEATURE_COMMAND.getName(), 0, false,
				duplicateIcon);

		deleteFeatureMenuItem = GuiUtils.addMenuItem(this,
				MainActionCommands.DELETE_LIBRARY_FEATURE_COMMAND.getName(), listener,
				MainActionCommands.DELETE_LIBRARY_FEATURE_COMMAND.getName(), 0, false,
				deleteFeatureIcon);

		this.addSeparator();

		copyFormulaMenuItem = GuiUtils.addMenuItem(this,
				MainActionCommands.COPY_COMPOUND_FORMULA_COMMAND.getName(), listener,
				MainActionCommands.COPY_COMPOUND_FORMULA_COMMAND.getName());
		copyFormulaMenuItem.setIcon(copyFormulaIcon);

		copyAccessionMenuItem = GuiUtils.addMenuItem(this,
				MainActionCommands.COPY_COMPOUND_ACCESSION_COMMAND.getName(), listener,
				MainActionCommands.COPY_COMPOUND_ACCESSION_COMMAND.getName());
		copyAccessionMenuItem.setIcon(copyAccessionIcon);

		copyNameMenuItem = GuiUtils.addMenuItem(this,
				MainActionCommands.COPY_COMPOUND_NAME_COMMAND.getName(), listener,
				MainActionCommands.COPY_COMPOUND_NAME_COMMAND.getName());
		copyNameMenuItem.setIcon(copyNameIcon);

		this.addSeparator();

		searchDatabaseByAccessionMenuItem = GuiUtils.addMenuItem(this,
				MainActionCommands.SEARCH_FEATURE_ACCESSION_IN_DATABASE_COMMAND.getName(), listener,
				MainActionCommands.SEARCH_FEATURE_ACCESSION_IN_DATABASE_COMMAND.getName(), 0, false,
				searchDatabaseByIdIcon);

		searchFeatureFormulaAgainstDatabaseMenuItem = GuiUtils.addMenuItem(this,
				MainActionCommands.SEARCH_FEATURE_FORMULA_IN_DATABASE_COMMAND.getName(), listener,
				MainActionCommands.SEARCH_FEATURE_FORMULA_IN_DATABASE_COMMAND.getName(), 0, false,
				searchDatabaseByFormulaIcon);

		searchFeatureMassAgainstDatabaseMenuItem = GuiUtils.addMenuItem(this,
				MainActionCommands.SEARCH_FEATURE_MASS_IN_DATABASE_COMMAND.getName(), listener,
				MainActionCommands.SEARCH_FEATURE_MASS_IN_DATABASE_COMMAND.getName(), 0, false,
				searchDatabaseByMassIcon);
	}
}

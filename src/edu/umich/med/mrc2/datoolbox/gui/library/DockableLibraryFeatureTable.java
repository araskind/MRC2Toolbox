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

package edu.umich.med.mrc2.datoolbox.gui.library;

import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

import javax.swing.Icon;
import javax.swing.JScrollPane;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import edu.umich.med.mrc2.datoolbox.data.LibraryMsFeature;
import edu.umich.med.mrc2.datoolbox.data.compare.MsFeatureComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class DockableLibraryFeatureTable extends DefaultSingleCDockable {

	private LibraryFeatureTable libraryFeatureTable;
	private static final Icon componentIcon = GuiUtils.getIcon("text", 16);

	public DockableLibraryFeatureTable(String id, String title, ActionListener listener) {

		super(id, componentIcon, title, null, Permissions.MIN_MAX_STACK);
		setCloseable(false);

		libraryFeatureTable = new LibraryFeatureTable();
		libraryFeatureTable.addTablePopupMenu(
				new LibraryFeaturePopupMenu(listener, libraryFeatureTable));
		add(new JScrollPane(libraryFeatureTable));
	}

	/**
	 * @return the libraryFeatureTable
	 */
	public LibraryFeatureTable getTable() {
		return libraryFeatureTable;
	}

	public Collection<LibraryMsFeature> getFilteredTargets() {

		Collection<LibraryMsFeature> filtered = new ArrayList<LibraryMsFeature>();
		int ltColumn = libraryFeatureTable.getColumnIndex(LibraryFeatureTableModel.FEATURE_COLUMN);

		for (int i = 0; i < libraryFeatureTable.getRowCount(); i++) {

			LibraryMsFeature lt = (LibraryMsFeature) libraryFeatureTable.getValueAt(i, ltColumn);
			filtered.add(lt);
		}
		return filtered.stream().sorted(new MsFeatureComparator(SortProperty.RT)).collect(Collectors.toList());
	}
}

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

package edu.umich.med.mrc2.datoolbox.gui.idtable;

import java.util.Collection;

import javax.swing.Icon;
import javax.swing.JScrollPane;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureIdentity;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundIdSource;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class DockableIdentificationResultsTable extends DefaultSingleCDockable {

	private static final Icon componentIcon = GuiUtils.getIcon("text", 16);
	private IdentificationResultsTable idTable;

	public DockableIdentificationResultsTable(String id, String title) {

		super(id, componentIcon, title, null, Permissions.MIN_MAX_STACK);
		setCloseable(false);

		idTable = new IdentificationResultsTable();
		add(new JScrollPane(idTable));
	}

	public IdentificationResultsTable getTable() {
		return idTable;
	}
	public MsFeatureIdentity getFeatureIdAtPopup() {
		return idTable.getFeatureIdAtPopup();
	}

	public void setValueAt(Object value, int row, int col) {
		idTable.setValueAt(value, row, col);
	}

	public synchronized void clearTable() {
		idTable.clearTable();
	}

	public void setModelFromMsFeature(MsFeature feature) {
		idTable.setModelFromMsFeature(feature);
	}
	
	public void setModelFromMsFeature(MsFeature feature, Collection<CompoundIdSource>sorcesToExclude) {
		idTable.setModelFromMsFeature(feature, sorcesToExclude);
	}

	public MsFeatureIdentity getSelectedIdentity() {
		return idTable.getSelectedIdentity();
	}
}














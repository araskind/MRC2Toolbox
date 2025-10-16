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

package edu.umich.med.mrc2.datoolbox.gui.cpddatabase;

import java.awt.BorderLayout;
import java.util.Collection;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.JScrollPane;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import edu.umich.med.mrc2.datoolbox.data.CompoundIdentity;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureIdentity;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class DockableCuratedDatabaseCompoundTable extends DefaultSingleCDockable{

	private static final Icon componentIcon = GuiUtils.getIcon("table", 16);
	private CuratedDatabaseCompoundTable databaseCompoundTable;

	public DockableCuratedDatabaseCompoundTable(String id, String title) {

		super(id, componentIcon, title, null, Permissions.MIN_MAX_STACK);
		setCloseable(false);
		setLayout(new BorderLayout(0,0));

		databaseCompoundTable = new CuratedDatabaseCompoundTable();
		add(new JScrollPane(databaseCompoundTable));
	}

	public void setTableModelFromCompoundCollection(Map<CompoundIdentity,Boolean> compoundCollection) {
		databaseCompoundTable.setTableModelFromCompoundCollection(compoundCollection);
	}

	public CompoundIdentity getSelectedCompound() {
		return databaseCompoundTable.getSelectedCompound();
	}

	public MsFeatureIdentity getSelectedIdentity() {
		return databaseCompoundTable.getSelectedIdentity();
	}

	public void updateMSFidData(MsFeatureIdentity id, boolean isCurated) {
		databaseCompoundTable.updateMSFidData(id, isCurated);
	}
	
	public void updateCidData(CompoundIdentity id, boolean isCurated) {
		databaseCompoundTable.updateCidData(id, isCurated);
	}

	public CuratedDatabaseCompoundTable getTable() {
		return databaseCompoundTable;
	}

	public synchronized void clearTable() {
		databaseCompoundTable.clearTable();
	}

	public Collection<CompoundIdentity> getListedCompounds() {
		return databaseCompoundTable.getListedCompounds();
	}
	
	public void selectCompoundIdentity(CompoundIdentity cid) {
		databaseCompoundTable.selectCompoundIdentity(cid);
	}
}

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

package edu.umich.med.mrc2.datoolbox.gui.cpddatabase.curator.struct;

import javax.swing.Icon;
import javax.swing.JScrollPane;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import edu.umich.med.mrc2.datoolbox.data.CompoundIdentity;
import edu.umich.med.mrc2.datoolbox.data.CompoundIdentityCluster;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class DockableCompoundStructureTable extends DefaultSingleCDockable {

	private static final Icon componentIcon = GuiUtils.getIcon("pubChem", 16);	

	private CompoundStructureTable idTable;

	public DockableCompoundStructureTable() {

		super("DockableCompoundStructureTable", componentIcon, 
					"Compound structures", null, Permissions.MIN_MAX_STACK);
		setCloseable(false);

		idTable = new CompoundStructureTable();
		add(new JScrollPane(idTable));
	}

	public CompoundStructureTable getTable() {
		return idTable;
	}
	
	public void clearTable() {
		idTable.clearTable();
	}

	public void setModelFromCompoundIdentityCluster(CompoundIdentityCluster cluster) {
		idTable.setModelFromCompoundIdentityCluster(cluster);
	}
	
	public CompoundIdentity getSelectedIdentity() {
		return idTable.getSelectedCompoundId();
	}
	
	public void selectStructureForIdentity(CompoundIdentity id) {
		idTable.selectStructureForIdentity(id);
	}
}














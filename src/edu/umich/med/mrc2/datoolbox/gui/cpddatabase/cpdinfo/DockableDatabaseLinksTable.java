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

package edu.umich.med.mrc2.datoolbox.gui.cpddatabase.cpdinfo;

import java.util.List;

import javax.swing.Icon;
import javax.swing.JScrollPane;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import edu.umich.med.mrc2.datoolbox.data.CompoundIdentity;
import edu.umich.med.mrc2.datoolbox.database.cpd.CompoundDatabaseUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class DockableDatabaseLinksTable extends DefaultSingleCDockable {

	private static final Icon componentIcon = GuiUtils.getIcon("webLink", 16);
	private DbLinksTable dbLinksTable;

	public DockableDatabaseLinksTable() {

		super("DockableDatabaseLinksTable", componentIcon, "Database links", null, Permissions.MIN_MAX_STACK);
		setCloseable(false);

		dbLinksTable = new DbLinksTable();
		add(new JScrollPane(dbLinksTable));
	}

	public void setModelFromLinks(List<CompoundIdentity> dbLinks) {
		dbLinksTable.setModelFromLinks(dbLinks);
	}

	public synchronized void clearTable() {
		dbLinksTable.clearTable();
	}

	public void loadCompoundData(CompoundIdentity cpd) {

		List<CompoundIdentity> dbLinks = null;
		try {
			dbLinks = CompoundDatabaseUtils.getDbIdList(cpd.getPrimaryDatabaseId());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(dbLinks != null)
			dbLinksTable.setModelFromLinks(dbLinks);
	}
}

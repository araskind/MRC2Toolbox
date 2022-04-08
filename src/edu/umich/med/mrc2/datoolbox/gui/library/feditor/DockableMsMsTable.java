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

package edu.umich.med.mrc2.datoolbox.gui.library.feditor;

import java.util.Collection;

import javax.swing.Icon;
import javax.swing.JScrollPane;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import edu.umich.med.mrc2.datoolbox.data.MsPoint;
import edu.umich.med.mrc2.datoolbox.data.TandemMassSpectrum;
import edu.umich.med.mrc2.datoolbox.gui.tables.ms.MsMsTable;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import umich.ms.datatypes.scan.IScan;

public class DockableMsMsTable extends DefaultSingleCDockable {

	private MsMsTable msmsTable;
	private static final Icon componentIcon = GuiUtils.getIcon("msms", 16);

	public DockableMsMsTable(String id, String title) {

		super(id, componentIcon, title, null, Permissions.MIN_MAX_STACK);
		setCloseable(false);

		msmsTable = new MsMsTable();
		add(new JScrollPane(msmsTable));
	}

	public void setTableModelFromTandemMs(TandemMassSpectrum msms) {
		msmsTable.setTableModelFromTandemMs(msms);
	}

	public void setTableModelFromDataPoints(Collection<MsPoint> points, MsPoint parent) {
		msmsTable.setTableModelFromDataPoints(points, parent);
	}
	
	public void setTableModelFromScan(IScan scan) {
		msmsTable.setTableModelFromScan(scan);
	}
	
	public synchronized void clearTable() {
		msmsTable.clearTable();
	}
}

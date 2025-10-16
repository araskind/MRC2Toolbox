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

package edu.umich.med.mrc2.datoolbox.gui.tables.ms;

import java.util.HashSet;

import javax.swing.Icon;
import javax.swing.JScrollPane;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import edu.umich.med.mrc2.datoolbox.data.Adduct;
import edu.umich.med.mrc2.datoolbox.data.MassSpectrum;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import umich.ms.datatypes.scan.IScan;

public class DockableMsTable extends DefaultSingleCDockable {

	private MsOneTable msOneTable;
	private static final Icon componentIcon = GuiUtils.getIcon("table", 16);

	public DockableMsTable(String id, String title) {

		super(id, componentIcon, title, null, Permissions.MIN_MAX_STACK);
		setCloseable(false);

		msOneTable = new MsOneTable();
		add(new JScrollPane(msOneTable));
	}

	public HashSet<Adduct>getVisibleAdducts(){
		return msOneTable.getVisibleAdducts();
	}

	public void setTableModelFromMsFeature(MsFeature feature) {
		msOneTable.setTableModelFromMsFeature(feature);
	}
	
	public void setTableModelFromSpectrum(MassSpectrum spectrum) {
		msOneTable.setTableModelFromSpectrum(spectrum);
	}
	
	public void setTableModelFromScan(IScan scan) {
		msOneTable.setTableModelFromScan(scan);
	}

	public synchronized void clearTable() {
		msOneTable.clearTable();
	}
}

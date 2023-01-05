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

package edu.umich.med.mrc2.datoolbox.gui.cpdcol.mplex;

import java.awt.BorderLayout;
import java.util.Collection;

import javax.swing.Icon;
import javax.swing.JScrollPane;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import edu.umich.med.mrc2.datoolbox.data.CompoundIdentity;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureIdentity;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class DockableCompoundMultiplexListingTable extends DefaultSingleCDockable{

	private static final Icon componentIcon = GuiUtils.getIcon("table", 16);
	private CompoundMultiplexListingTable compoundTable;

	public DockableCompoundMultiplexListingTable() {

		super("DockableCompoundMultiplexListingTable", componentIcon, "Compound multiplex mixtures", null, Permissions.MIN_MAX_STACK);
		setCloseable(false);
		setLayout(new BorderLayout(0,0));

		compoundTable = new CompoundMultiplexListingTable();
		add(new JScrollPane(compoundTable));
	}

	public void setTableModelFromCompoundCollection(Collection<CompoundIdentity> compoundCollection) {
		compoundTable.setTableModelFromCompoundCollection(compoundCollection);
	}

	public CompoundIdentity getSelectedCompound() {
		return compoundTable.getSelectedCompound();
	}

	public MsFeatureIdentity getSelectedIdentity() {
		return compoundTable.getSelectedIdentity();
	}

	public void updateCidData(MsFeatureIdentity id) {
		compoundTable.updateCidData(id);
	}

	/**
	 * @return the compoundDatabaseTable
	 */
	public CompoundMultiplexListingTable getTable() {
		return compoundTable;
	}

	public synchronized void clearTable() {
		compoundTable.clearTable();
	}

	public Collection<CompoundIdentity> getListedCompounds() {
		return compoundTable.getListedCompounds();
	}
}

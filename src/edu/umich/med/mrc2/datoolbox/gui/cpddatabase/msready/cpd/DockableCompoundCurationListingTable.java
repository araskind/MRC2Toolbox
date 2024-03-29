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

package edu.umich.med.mrc2.datoolbox.gui.cpddatabase.msready.cpd;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;

import javax.swing.Icon;
import javax.swing.JScrollPane;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import edu.umich.med.mrc2.datoolbox.data.CompoundIdentity;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureIdentity;
import edu.umich.med.mrc2.datoolbox.gui.cpddatabase.DatabaseCompoundTable;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class DockableCompoundCurationListingTable 
		extends DefaultSingleCDockable implements ActionListener{

	private static final Icon componentIcon = GuiUtils.getIcon("compoundCollection", 16);

	private DatabaseCompoundTable cpdTable;
	public DockableCompoundCurationListingTable() {

		super("DockableCompoundCurationListingTable", componentIcon, 
				"Compound for MS-ready curation", null, Permissions.MIN_MAX_STACK);
		setCloseable(false);
		setLayout(new BorderLayout(0,0));
		cpdTable = new DatabaseCompoundTable();
		add(new JScrollPane(cpdTable));
	}

	public void setTableModelFromCompoundCollection(
			Collection<CompoundIdentity> compoundCollection) {
		cpdTable.setTableModelFromCompoundCollection(compoundCollection);
	}

	public MsFeatureIdentity getSelectedIdentity() {
		return cpdTable.getSelectedIdentity();
	}

	public DatabaseCompoundTable getTable() {
		return cpdTable;
	}

	public synchronized void clearTable() {
		cpdTable.clearTable();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		
		String command = e.getActionCommand();		
	}
}













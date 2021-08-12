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

package edu.umich.med.mrc2.datoolbox.gui.mptrack.study;

import java.awt.BorderLayout;
import java.util.Collection;

import javax.swing.Icon;
import javax.swing.JScrollPane;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import edu.umich.med.mrc2.datoolbox.data.motrpac.MoTrPACAssay;
import edu.umich.med.mrc2.datoolbox.gui.mptrack.ref.asssay.MotrpacAssayTable;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class DockableMoTrPACStudyAssayListingPanel extends DefaultSingleCDockable {

	private static final Icon componentIcon = GuiUtils.getIcon("acqMethod", 16);
	private MotrpacAssayTable assayTable;

	public DockableMoTrPACStudyAssayListingPanel() {

		super("DockableMotrpacStudyAssayListingPanel", componentIcon, "Study assays", null, Permissions.MIN_MAX_STACK);
		setCloseable(false);
		setLayout(new BorderLayout(0, 0));

		assayTable =  new MotrpacAssayTable();
		JScrollPane designScrollPane = new JScrollPane(assayTable);
		getContentPane().add(designScrollPane, BorderLayout.CENTER);
	}

	public void loadAssays(Collection<MoTrPACAssay>assays) {
		assayTable.setTableModelFromAssays(assays);
	}

	public MoTrPACAssay getSelectedAssay(){
		return assayTable.getSelectedAssay();
	}
	
	public void clearPanel() {
		assayTable.clearTable();
	}
	
	public MotrpacAssayTable getTable() {
		return assayTable;
	}
}

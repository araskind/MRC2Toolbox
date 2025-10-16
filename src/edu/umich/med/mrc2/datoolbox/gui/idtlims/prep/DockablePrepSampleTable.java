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

package edu.umich.med.mrc2.datoolbox.gui.idtlims.prep;

import java.awt.BorderLayout;
import java.util.Collection;

import javax.swing.Icon;
import javax.swing.JScrollPane;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import edu.umich.med.mrc2.datoolbox.data.ExperimentalSample;
import edu.umich.med.mrc2.datoolbox.data.IDTExperimentalSample;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTablePopupMenu;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class DockablePrepSampleTable extends DefaultSingleCDockable {

	private static final Icon componentIcon = GuiUtils.getIcon("editSample", 16);
	private PrepSampleTable prepSampleTable;

	public DockablePrepSampleTable() {

		super("DockablePrepSampleTable", componentIcon, "Sample list", null, Permissions.MIN_MAX_STACK);
		setCloseable(false);

		prepSampleTable = new PrepSampleTable();
		prepSampleTable.addTablePopupMenu(
				new BasicTablePopupMenu(null, prepSampleTable, true));
		JScrollPane designScrollPane = new JScrollPane(prepSampleTable);
		getContentPane().add(designScrollPane, BorderLayout.CENTER);
	}

	public void setTableModelFromSamples(Collection<? extends ExperimentalSample>samples) {
		prepSampleTable.setTableModelFromSamples(samples);
	}

	public Collection<IDTExperimentalSample>getSelectedSamples(){
		return prepSampleTable.getSelectedSamples();
	}

	public synchronized void clearTable() {
		prepSampleTable.clearTable();
	}
}

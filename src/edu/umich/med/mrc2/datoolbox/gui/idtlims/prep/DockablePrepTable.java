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

package edu.umich.med.mrc2.datoolbox.gui.idtlims.prep;

import java.awt.BorderLayout;
import java.util.Collection;

import javax.swing.Icon;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionListener;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSSamplePreparation;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class DockablePrepTable extends DefaultSingleCDockable {

	private static final Icon componentIcon = GuiUtils.getIcon("table", 16);
	private SamplePrepTable samplePrepTable;
	private ListSelectionListener tableSelectionListener;

	public DockablePrepTable(ListSelectionListener lsl) {

		super("DockablePrepTable", componentIcon, "Preparations list", null, Permissions.MIN_MAX_STACK);
		setCloseable(false);

		samplePrepTable = new SamplePrepTable();
		tableSelectionListener = lsl;
		samplePrepTable.getSelectionModel().addListSelectionListener(tableSelectionListener);
		JScrollPane designScrollPane = new JScrollPane(samplePrepTable);
		getContentPane().add(designScrollPane, BorderLayout.CENTER);
	}

	public void setTableModelFromPreps(Collection<LIMSSamplePreparation>preps) {
		
		samplePrepTable.getSelectionModel().removeListSelectionListener(tableSelectionListener);
		samplePrepTable.setTableModelFromPreps(preps);
		samplePrepTable.getSelectionModel().addListSelectionListener(tableSelectionListener);
	}

	public LIMSSamplePreparation getSelectedPrep() {
		return samplePrepTable.getSelectedPrep();
	}

	public void clearTable() {
		samplePrepTable.clearTable();
	}
	
	public void clearSelection() {
		samplePrepTable.clearSelection();;
	}
	
	public void selectSamplePrep(LIMSSamplePreparation prep) {
		samplePrepTable.selectSamplePrep(prep);
	}
	
	public SamplePrepTable getTable() {
		return samplePrepTable;
	}
}

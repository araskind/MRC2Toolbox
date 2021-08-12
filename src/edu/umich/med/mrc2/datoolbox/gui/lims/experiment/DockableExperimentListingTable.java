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

package edu.umich.med.mrc2.datoolbox.gui.lims.experiment;

import java.util.Collection;

import javax.swing.Icon;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionListener;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSExperiment;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class DockableExperimentListingTable extends DefaultSingleCDockable {

	private ExperimentListingTable experimentListingTable;
	private static final Icon componentIcon = GuiUtils.getIcon("table", 16);

	public DockableExperimentListingTable(ListSelectionListener lsListener) {

		super("DockableExperimentListingTable", componentIcon, "Experiments listing", null, Permissions.MIN_MAX_STACK);
		setCloseable(false);

		experimentListingTable = new ExperimentListingTable();
		add(new JScrollPane(experimentListingTable));
		experimentListingTable.getSelectionModel().addListSelectionListener(lsListener);
	}

	/**
	 * @return the libraryFeatureTable
	 */
	public ExperimentListingTable getTable() {
		return experimentListingTable;
	}

	public void setModelFromExperimentCollection(Collection<LIMSExperiment>experimentCollection) {
		experimentListingTable.setModelFromExperimentCollection(experimentCollection);
	}

	public void clearPanel() {
		experimentListingTable.clearTable();
	}

	public LIMSExperiment getSelectedExperiment() {
		return experimentListingTable.getSelectedExperiment();
	}
}

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

package edu.umich.med.mrc2.datoolbox.gui.idworks.search.dbwide.se;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import edu.umich.med.mrc2.datoolbox.data.lims.LIMSExperiment;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTableModel;
import edu.umich.med.mrc2.datoolbox.gui.tables.ColumnContext;

public class IDTrackerExperimentListingTableModel extends BasicTableModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3261097067591853752L;
	public static final String EXPERIMENT_ID_COLUMN = "ID";
	public static final String EXPERIMENT_NAME_COLUMN = "Experiment name";

	public IDTrackerExperimentListingTableModel() {
		super();
		columnArray = new ColumnContext[] {

			new ColumnContext(EXPERIMENT_ID_COLUMN, "Experiment ID", LIMSExperiment.class, false),
			new ColumnContext(EXPERIMENT_NAME_COLUMN, EXPERIMENT_NAME_COLUMN, String.class, false),
		};
	}
	
	public void setTableModelFromExperimentList(Collection<LIMSExperiment>experimentList) {

		setRowCount(0);		
		if(experimentList == null || experimentList.isEmpty())
			return;
		
		List<Object[]>rowData = new ArrayList<Object[]>();
		for(LIMSExperiment experiment : experimentList) {
			
			if(experiment == null)
				continue;
			
			Object[] obj = {
				experiment,
				experiment.getName(),
			};
			rowData.add(obj);
		}
		if(!rowData.isEmpty())
			addRows(rowData);
	}
}

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

package edu.umich.med.mrc2.datoolbox.gui.idworks.search.byexp;

import java.awt.BorderLayout;
import java.util.Collection;

import javax.swing.Icon;
import javax.swing.JScrollPane;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class DockableDataPipelinesTable extends DefaultSingleCDockable {

	private static final Icon componentIcon = GuiUtils.getIcon("editDataProcessingMethod", 16);
	
	private DataPipelinesTable dataPipelinesTable;

	public DockableDataPipelinesTable()  {

		super("DockableDataPipelinesTable", componentIcon, "Data pipelines", null, Permissions.MIN_MAX_STACK);
		setCloseable(false);
		
		dataPipelinesTable = new DataPipelinesTable();
		add(new JScrollPane(dataPipelinesTable), BorderLayout.CENTER);
	}
	
	public Collection<DataPipeline> getSelectedDataPipelines() {
		return dataPipelinesTable.getSelectedDataPipelines();
	}
	
	public Collection<DataPipeline> getAllDataPipelines() {
		return dataPipelinesTable.getAllDataPipelines();
	}

	public void setTableModelFromDataPipelineCollection(Collection<DataPipeline> allPipelines) {
		dataPipelinesTable.setTableModelFromDataPipelineCollection(allPipelines);	
	}
}

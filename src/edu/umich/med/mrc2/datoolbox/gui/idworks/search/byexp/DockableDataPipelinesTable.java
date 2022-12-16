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
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.prefs.Preferences;

import javax.swing.Icon;
import javax.swing.JScrollPane;

import edu.umich.med.mrc2.datoolbox.data.enums.ParameterSetStatus;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.communication.DockableParametersPanel;
import edu.umich.med.mrc2.datoolbox.gui.communication.FormChangeEvent;
import edu.umich.med.mrc2.datoolbox.gui.communication.FormChangeListener;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class DockableDataPipelinesTable extends DockableParametersPanel {

	private static final Icon componentIcon = GuiUtils.getIcon("editDataProcessingMethod", 16);
	
	private DataPipelinesTable dataPipelinesTable;

	public DockableDataPipelinesTable()  {

		super("DockableDataPipelinesTable", componentIcon, "Data pipelines", Permissions.MIN_MAX_STACK);
		setCloseable(false);
		
		dataPipelinesTable = new DataPipelinesTable();
		dataPipelinesTable.getSelectionModel().addListSelectionListener(this);
		add(new JScrollPane(dataPipelinesTable), BorderLayout.CENTER);
	}
	
	public Collection<DataPipeline> getSelectedDataPipelines() {
		return dataPipelinesTable.getSelectedDataPipelines();
	}
	
	public void setSelectedDataPipelines(Collection<DataPipeline>selected) {
		dataPipelinesTable.setSelectedDataPipelines(selected);
	}
	
	public Collection<DataPipeline> getAllDataPipelines() {
		return dataPipelinesTable.getAllDataPipelines();
	}
	
	public void clearSelection() {
		dataPipelinesTable.clearSelection();
	}
	
	public void clearPanel() {
		dataPipelinesTable.clearTable();
	}

	public void setTableModelFromDataPipelineCollection(Collection<DataPipeline> allPipelines) {
		dataPipelinesTable.setTableModelFromDataPipelineCollection(allPipelines);			
	}
	
	@Override
	public void fireFormChangeEvent(ParameterSetStatus newStatus) {

		FormChangeEvent event = new FormChangeEvent(dataPipelinesTable, newStatus);
		changeListeners.stream().forEach(l -> ((FormChangeListener) l).
				formDataChanged(event));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Collection<String> validateInput() {

		Collection<String>errors = new ArrayList<String>();
		
		return errors;
	}

	@Override
	public void resetPanel(Preferences preferences) {
		dataPipelinesTable.clearSelection();
	}

	@Override
	public boolean hasSpecifiedConstraints() {
		return !dataPipelinesTable.getSelectedDataPipelines().isEmpty();
	}
}

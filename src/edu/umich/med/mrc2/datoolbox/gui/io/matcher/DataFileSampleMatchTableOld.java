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

package edu.umich.med.mrc2.datoolbox.gui.io.matcher;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import javax.swing.table.TableRowSorter;

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.ExperimentalSample;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.editors.ExperimentalSampleSelectorEditor;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.AutoChoices;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.TableFilterHeader;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.DataFileCellRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.ExperimentalSampleRendererExtended;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;

public class DataFileSampleMatchTableOld extends BasicTable {

	/**
	 *
	 */
	private static final long serialVersionUID = -3111543056268144390L;
	private DataFileSampleMatchTableModelOld model;

	public DataFileSampleMatchTableOld() {

		super();

		getTableHeader().setReorderingAllowed(false);
		model = new DataFileSampleMatchTableModelOld();
		setModel(model);
		rowSorter = new TableRowSorter<DataFileSampleMatchTableModelOld>(model);
		setRowSorter(rowSorter);
		
		setDefaultRenderer(ExperimentalSample.class, new ExperimentalSampleRendererExtended());
		setDefaultRenderer(DataFile.class, new DataFileCellRenderer());

		thf = new TableFilterHeader(this, AutoChoices.ENABLED);
		finalizeLayout();
		columnModel.getColumnById(DataFileSampleMatchTableModelOld.ENABLED_COLUMN).setWidth(50);
	}

	public void setTableModelFromFiles(File[] inputFiles, Collection<ExperimentalSample>samples) {

		model.addFilesForDataPipeline(inputFiles, null);
		setDefaultEditor(ExperimentalSample.class,
			new ExperimentalSampleSelectorEditor(samples, this));

		tca.adjustColumnsExcluding(Collections.singleton(
				getColumnIndex(DataFileSampleMatchTableModelOld.ENABLED_COLUMN)));
	}
	
	public void addFilesForDataPipeline(File[] inputFiles, DataPipeline pipeline) {

		model.addFilesForDataPipeline(inputFiles, pipeline);
		setDefaultEditor(ExperimentalSample.class,
				new ExperimentalSampleSelectorEditor(
						MRC2ToolBoxCore.getCurrentProject().getExperimentDesign().getSamples(), this));

		tca.adjustColumnsExcluding(Collections.singleton(
				getColumnIndex(DataFileSampleMatchTableModelOld.ENABLED_COLUMN)));
	}
	
	public void refreshSampleEditor() {
		
		setDefaultEditor(ExperimentalSample.class,
				new ExperimentalSampleSelectorEditor(
						MRC2ToolBoxCore.getCurrentProject().getExperimentDesign().getSamples(), this));

		tca.adjustColumnsExcluding(Collections.singleton(
				getColumnIndex(DataFileSampleMatchTableModelOld.ENABLED_COLUMN)));
	}

	public DataFile[] getActiveDataFiles() {

		ArrayList<DataFile>files = new ArrayList<DataFile>();
		int fileCol = model.getColumnIndex(DataFileSampleMatchTableModelOld.DATA_FILE_COLUMN);
		int activeCol = model.getColumnIndex(DataFileSampleMatchTableModelOld.ENABLED_COLUMN);		
		for(int i=0; i<model.getRowCount(); i++) {

			if((boolean) model.getValueAt(i, activeCol))
				files.add((DataFile) model.getValueAt(i, fileCol));
		}
		return files.toArray(new DataFile[files.size()]);
	}

	public Collection<DataFile>getAllDataFiles(){
		return model.getDataFileSampleMap().keySet();
	}

	public Collection<DataFile>getSelectedDataFiles(){

		Collection<DataFile>files = new ArrayList<DataFile>();
		if(getSelectedRowCount() == 0)
			return files;

		int dfIndex = model.getColumnIndex(DataFileSampleMatchTableModelOld.DATA_FILE_COLUMN);
		for(int i : getSelectedRows())
			files.add((DataFile) model.getValueAt(convertRowIndexToModel(i), dfIndex));

		return files;
	}

	public Collection<DataFile>getUnmatchedEnabledFiles() {
		
		model.forceSampleAssignment();
		Collection<DataFile>unmatched = new ArrayList<DataFile>();
		int dfIndex = model.getColumnIndex(DataFileSampleMatchTableModelOld.DATA_FILE_COLUMN);
		int sidIndex = model.getColumnIndex(DataFileSampleMatchTableModelOld.SAMPLE_ID_COLUMN);
		int activeCol = model.getColumnIndex(DataFileSampleMatchTableModelOld.ENABLED_COLUMN);		
		for(int i=0; i<model.getRowCount(); i++) {

			DataFile df = (DataFile) model.getValueAt(i, dfIndex);
			ExperimentalSample sample = (ExperimentalSample) model.getValueAt(i, sidIndex);
			if(sample == null && (Boolean)model.getValueAt(i, activeCol))
				unmatched.add(df);	
		}
		return unmatched;
	}
	


	public void removeSelectedDataFiles() {
		
		Collection<DataFile>toRemove = getSelectedDataFiles();
		if(!toRemove.isEmpty())
			model.removeDataFiles(toRemove);
	}

	public void updateSampleAssignmentForDataFiles(Collection<DataFile> selectedDataFiles, ExperimentalSample sample) {
		model.updateSampleAssignmentForDataFiles(selectedDataFiles, sample);
	}

	public void assignSamples(Map<DataFile, ExperimentalSample> fileSampleMap) {
		model.assignSamples(fileSampleMap);
	}
	
	public Map<DataFile, ExperimentalSample> getDataFileSampleMap() {
		return model.getDataFileSampleMap();
	}
}

















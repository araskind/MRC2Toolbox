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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.IntStream;

import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.ExperimentalSample;
import edu.umich.med.mrc2.datoolbox.data.lims.DataAcquisitionMethod;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.editors.ExperimentalSampleSelectorEditor;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.AutoChoices;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.TableFilterHeader;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.DataFileCellRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.ExperimentalSampleRendererExtended;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;

public class ExtendedDataFileSampleMatchTable extends BasicTable {

	/**
	 *
	 */
	private static final long serialVersionUID = -3111543056268144390L;
	private ExtendedDataFileSampleMatchTableModel model;

	public ExtendedDataFileSampleMatchTable() {

		super();

		getTableHeader().setReorderingAllowed(false);
		model = new ExtendedDataFileSampleMatchTableModel();
		setModel(model);
		rowSorter = new TableRowSorter<ExtendedDataFileSampleMatchTableModel>(model);
		setRowSorter(rowSorter);

		setDefaultRenderer(ExperimentalSample.class, new ExperimentalSampleRendererExtended());
		setDefaultRenderer(DataFile.class, new DataFileCellRenderer());

		thf = new TableFilterHeader(this, AutoChoices.ENABLED);
		finalizeLayout();
		//columnModel.getColumnById(ExtendedDataFileSampleMatchTableModel.ENABLED_COLUMN).setWidth(50);
	}

	public void setTableModelFromReportData(
			String[] sampleIds,
			String[] sampleNames,
			String[] dataFileNames,
			DataAcquisitionMethod acquisitionMethod) {

		model.setTableModelFromReportData(
			sampleIds, sampleNames, dataFileNames, acquisitionMethod);
		setDefaultEditor(ExperimentalSample.class,
				new ExperimentalSampleSelectorEditor(
						MRC2ToolBoxCore.getCurrentProject().getExperimentDesign().getSamples(), this));

		tca.adjustColumns();
	}

	public DataFile[] getActiveDataFiles() {

		ArrayList<DataFile>files = new ArrayList<DataFile>();
		int fileColumn = model.getColumnIndex(ExtendedDataFileSampleMatchTableModel.DATA_FILE_COLUMN);
		int activeColumn = model.getColumnIndex(ExtendedDataFileSampleMatchTableModel.ENABLED_COLUMN);		
		for(int i=0; i<model.getRowCount(); i++) {

			if((boolean) model.getValueAt(i, activeColumn))
				files.add((DataFile) model.getValueAt(i, fileColumn));
		}
		return files.toArray(new DataFile[files.size()]);
	}

	public Collection<DataFile>getDataFiles(){

		Collection<DataFile>files = new ArrayList<DataFile>();
		int dfIndex = model.getColumnIndex(ExtendedDataFileSampleMatchTableModel.DATA_FILE_COLUMN);
		for(int i=0; i<model.getRowCount(); i++)
			files.add((DataFile) model.getValueAt(i, dfIndex));

		return files;
	}

	public Collection<DataFile>getSelectedDataFiles(){

		Collection<DataFile>files = new ArrayList<DataFile>();
		if(getSelectedRowCount() == 0)
			return files;

		int dfIndex = model.getColumnIndex(ExtendedDataFileSampleMatchTableModel.DATA_FILE_COLUMN);
		for(int i : getSelectedRows())
			files.add((DataFile) model.getValueAt(convertRowIndexToModel(i), dfIndex));

		return files;
	}

	public boolean hasUnmatchedFiles() {

		int dfIndex = model.getColumnIndex(ExtendedDataFileSampleMatchTableModel.DATA_FILE_COLUMN);
		for(int i=0; i<model.getRowCount(); i++) {

			if(((DataFile) model.getValueAt(i, dfIndex)).getParentSample() == null)
				return true;
		}
		return false;
	}

	public void removeSelectedDataFiles() {

		IntStream.of(getSelectedRows())
	        .boxed().map(i -> convertRowIndexToModel(i))
	        .sorted(Collections.reverseOrder())
	        .forEach(((DefaultTableModel)getModel())::removeRow);
	}

	public void updateSampleAssignmentForDataFiles(Collection<DataFile> selectedDataFiles, ExperimentalSample sample) {

		int dfIndex = model.getColumnIndex(ExtendedDataFileSampleMatchTableModel.DATA_FILE_COLUMN);
		int sampleIndex = model.getColumnIndex(ExtendedDataFileSampleMatchTableModel.SAMPLE_ID_COLUMN);
		for(int i=0; i<model.getRowCount(); i++) {

			if(selectedDataFiles.contains(model.getValueAt(i, dfIndex)))
				model.setValueAt(sample, i, sampleIndex);
		}
	}
}

















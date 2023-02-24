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

package edu.umich.med.mrc2.datoolbox.gui.idtlims.dataimport;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.IntStream;

import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.ExperimentalSample;
import edu.umich.med.mrc2.datoolbox.data.Worklist;
import edu.umich.med.mrc2.datoolbox.data.lims.DataExtractionMethod;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSExperiment;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.AutoChoices;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.TableFilterHeader;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.DataFileCellRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.ExperimentalSampleRendererExtended;

public class IDtrackerDataFileSampleMatchTable  extends BasicTable {

	/**
	 *
	 */
	private static final long serialVersionUID = -3111543056268144390L;
	private IDtrackerDataFileSampleMatchTableModel model;

	public IDtrackerDataFileSampleMatchTable() {

		super();

		getTableHeader().setReorderingAllowed(false);
		model = new IDtrackerDataFileSampleMatchTableModel();
		setModel(model);
		rowSorter = new TableRowSorter<IDtrackerDataFileSampleMatchTableModel>(model);
		setRowSorter(rowSorter);
		
		setDefaultRenderer(DataFile.class, new DataFileCellRenderer());
		setDefaultRenderer(ExperimentalSample.class, new ExperimentalSampleRendererExtended());

		thf = new TableFilterHeader(this, AutoChoices.ENABLED);
		finalizeLayout();
	}
	
	public void addDataFilesUsingWorklist(File[] inputFiles, Worklist worklist) {
		
		thf.setTable(null);
		model.addDataFilesUsingWorklist(inputFiles, worklist);
		thf.setTable(this);
		tca.adjustColumns();
	}

	public void setTableModelFromFiles(File[] inputFiles, LIMSExperiment experiment) {
		thf.setTable(null);
		model.setTableModelFromFiles(inputFiles, experiment);
		thf.setTable(this);
		tca.adjustColumns();
	}

	public Collection<DataFile>getDataFiles(){

		Collection<DataFile>files = new ArrayList<DataFile>();
		int dfIndex = model.getColumnIndex(IDtrackerDataFileSampleMatchTableModel.DATA_FILE_COLUMN);
		for(int i=0; i<model.getRowCount(); i++)
			files.add((DataFile) getValueAt(i, dfIndex));

		return files;
	}

	public Collection<DataFile>getSelectedDataFiles(){

		Collection<DataFile>files = new ArrayList<DataFile>();
		if(getSelectedRowCount() == 0)
			return files;

		int dfIndex = model.getColumnIndex(
				IDtrackerDataFileSampleMatchTableModel.DATA_FILE_COLUMN);
		for(int i : getSelectedRows())
			files.add((DataFile) model.getValueAt(convertRowIndexToModel(i), dfIndex));

		return files;
	}

	public boolean hasUnmatchedFiles() {

		int dfIndex = model.getColumnIndex(
				IDtrackerDataFileSampleMatchTableModel.DATA_FILE_COLUMN);
		for(int i=0; i<model.getRowCount(); i++) {

			if(((DataFile) model.getValueAt(i, dfIndex)).getParentSample() == null)
				return true;
		}
		return false;
	}

	public void removeSelectedDataFiles() {

		thf.setTable(null);
		IntStream.of(getSelectedRows())
	        .boxed().map(i -> convertRowIndexToModel(i))
	        .sorted(Collections.reverseOrder())
	        .forEach(((DefaultTableModel)getModel())::removeRow);
		thf.setTable(this);
	}

	public Map<DataFile,DataExtractionMethod>getFileDaMethodMap(){

		Map<DataFile,DataExtractionMethod>fileDaMethodMap = 
				new TreeMap<DataFile,DataExtractionMethod>();
		int dfIndex = getColumnIndex(
				IDtrackerDataFileSampleMatchTableModel.DATA_FILE_COLUMN);
		int methodIndex = getColumnIndex(
				IDtrackerDataFileSampleMatchTableModel.DA_METHOD_COLUMN);
//		int activeIndex = getColumnIndex(IDtrackerDataFileSampleMatchTableModel.ENABLED_COLUMN);
		for(int i=0; i<model.getRowCount(); i++) {

//			if((boolean)model.getValueAt(i, activeIndex)) {

				DataFile df = (DataFile) model.getValueAt(i, dfIndex);
				DataExtractionMethod method = 
						(DataExtractionMethod) model.getValueAt(i, methodIndex);
				if(method != null)
					fileDaMethodMap.put(df, method);
//			}
		}
		return fileDaMethodMap;
	}

	public void setDaMethodFoFiles(
			Collection<DataFile> files, DataExtractionMethod method) {

		thf.setTable(null);
		int dfIndex = model.getColumnIndex(
				IDtrackerDataFileSampleMatchTableModel.DATA_FILE_COLUMN);
		int methodIndex = model.getColumnIndex(
				IDtrackerDataFileSampleMatchTableModel.DA_METHOD_COLUMN);
		for(int i=0; i<model.getRowCount(); i++) {

				DataFile df = (DataFile) model.getValueAt(i, dfIndex);
				if(files.contains(df))
					model.setValueAt(method, i, methodIndex);
		}
		thf.setTable(this);
	}
	
	public boolean hasMissingInjectionData() {
		
		return getDataFiles().stream().
				filter(f -> (f.getDataAcquisitionMethod() == null || f.getInjectionTime() == null)).
				count() > 0;
	}
	
	public boolean hasMissingDataAnalysisMethod() {
		
		int methodIndex = getColumnIndex(IDtrackerDataFileSampleMatchTableModel.DA_METHOD_COLUMN);
		for(int i=0; i<model.getRowCount(); i++) {

			DataExtractionMethod method = (DataExtractionMethod) model.getValueAt(i, methodIndex);
			if(method == null)
				return true;			
		}
		return false;
	}
}



















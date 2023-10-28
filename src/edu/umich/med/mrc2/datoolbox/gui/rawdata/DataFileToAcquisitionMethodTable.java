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

package edu.umich.med.mrc2.datoolbox.gui.rawdata;

import java.util.ArrayList;
import java.util.Collection;

import javax.swing.ListSelectionModel;
import javax.swing.table.TableRowSorter;

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.compare.AnalysisMethodComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.format.AnalysisMethodFormat;
import edu.umich.med.mrc2.datoolbox.data.lims.DataAcquisitionMethod;
import edu.umich.med.mrc2.datoolbox.gui.idworks.ms1.ReferenceMsOneFeatureTableModel;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.AutoChoices;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.TableFilterHeader;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.AnalysisMethodRenderer;

public class DataFileToAcquisitionMethodTable extends BasicTable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2087167895707677516L;

	public DataFileToAcquisitionMethodTable() {

		super();

		model = new DataFileToAcquisitionMethodTableModel();
		setModel(model);
		setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		
		rowSorter = new TableRowSorter<DataFileToAcquisitionMethodTableModel>(
				(DataFileToAcquisitionMethodTableModel)model);
		setRowSorter(rowSorter);
		rowSorter.setComparator(model.getColumnIndex(
				ReferenceMsOneFeatureTableModel.ACQ_METHOD_ID_COLUMN),
				new AnalysisMethodComparator(SortProperty.Name));

		setDefaultRenderer(DataAcquisitionMethod.class, new AnalysisMethodRenderer());
		thf = new TableFilterHeader(this, AutoChoices.ENABLED);
		thf.getParserModel().setComparator(DataAcquisitionMethod.class, 
				new AnalysisMethodComparator(SortProperty.ID));
		thf.getParserModel().setFormat(DataAcquisitionMethod.class, 
				new AnalysisMethodFormat(SortProperty.ID));
		finalizeLayout();
	}

	public void setModelFromDataFiles(Collection<DataFile> files, boolean append) {

		thf.setTable(null);
		((DataFileToAcquisitionMethodTableModel)model).setModelFromDataFiles(files, append);
		thf.setTable(this);
		adjustColumns();
	}
	
	public Collection<DataFile>getSelectedFiles(){
		
		Collection<DataFile>selected = new ArrayList<DataFile>();
		if(getSelectedRowCount() == 0)
			return selected;
		
		int fileColumn = model.getColumnIndex(DataFileToAcquisitionMethodTableModel.DATA_FILE_COLUMN);
		for(int i : getSelectedRows())
			selected.add((DataFile)model.getValueAt(convertRowIndexToModel(i),fileColumn));
				
		return selected;
	}

	public void selectFiles(Collection<DataFile> dataFiles) {

		clearSelection();
		if(dataFiles == null || dataFiles.isEmpty())
			return;
		
		int fileColumn = model.getColumnIndex(DataFileToAcquisitionMethodTableModel.DATA_FILE_COLUMN);
		for(int i=0; i<getRowCount(); i++) {
			DataFile rowFile = ((DataFile)model.getValueAt(convertRowIndexToModel(i),fileColumn));
			if(dataFiles.contains(rowFile))
				addRowSelectionInterval(i, i);
		}
		scrollToSelected();
	}

	public void removeDataFiles(Collection<DataFile> filesToRemove) {
		((DataFileToAcquisitionMethodTableModel)model).removeFiles(filesToRemove);
	}
}


























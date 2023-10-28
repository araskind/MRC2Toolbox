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

package edu.umich.med.mrc2.datoolbox.gui.idtlims.wizard.methods;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.ListSelectionModel;
import javax.swing.table.TableRowSorter;

import edu.umich.med.mrc2.datoolbox.data.compare.AnalysisMethodComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.lims.AnalysisMethod;
import edu.umich.med.mrc2.datoolbox.data.lims.DataAcquisitionMethod;
import edu.umich.med.mrc2.datoolbox.data.lims.DataExtractionMethod;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCache;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.AutoChoices;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.TableFilterHeader;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.AnalysisMethodRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.FileBaseNameRenderer;

public class MethodTable extends BasicTable {

	/**
	 *
	 */
	private static final long serialVersionUID = -1405921543482090501L;

	public MethodTable() {
		super();
		model =  new MethodTableModel();
		setModel(model);
		rowSorter = new TableRowSorter<MethodTableModel>((MethodTableModel)model);
		setRowSorter(rowSorter);
		rowSorter.setComparator(getColumnIndex(MethodTableModel.METHOD_ID_COLUMN), 
				new AnalysisMethodComparator(SortProperty.ID));
		
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		setDefaultRenderer(AnalysisMethod.class, new AnalysisMethodRenderer(SortProperty.ID));

		columnModel.getColumnById(MethodTableModel.METHOD_NAME_COLUMN)
				.setCellRenderer(new FileBaseNameRenderer());
		columnModel.getColumnById(MethodTableModel.METHOD_ID_COLUMN).setWidth(80);
		fixedWidthColumns.add(model.getColumnIndex(MethodTableModel.METHOD_ID_COLUMN));

		
		thf = new TableFilterHeader(this, AutoChoices.ENABLED);
		finalizeLayout();
	}

	public void setTableModelFromAcquisitionMethods(Collection<String> methodNames) {
		
		Map<String,AnalysisMethod>methodFilesMap = new TreeMap<String,AnalysisMethod>();
		for(String methodName : methodNames) {
			
			DataAcquisitionMethod existingMethod = 
					IDTDataCache.getAcquisitionMethodByName(methodName);
			
			methodFilesMap.put(methodName, existingMethod);
		}
		thf.setTable(null);
		((MethodTableModel)model).setTableModelFromMethods(methodFilesMap);
		thf.setTable(this);
		adjustColumns();
	}

	public void setTableModelFromDataAnalysisMethods(Collection<DataExtractionMethod> dataExtractionMethods) {

		Map<String,AnalysisMethod>methodFilesMap = new TreeMap<String,AnalysisMethod>();
		for(DataExtractionMethod mf : dataExtractionMethods)
			methodFilesMap.put(mf.getName(), mf);
		
		thf.setTable(null);
		((MethodTableModel)model).setTableModelFromMethods(methodFilesMap);
		thf.setTable(this);
		adjustColumns();
	}
	
	public AnalysisMethod getSelectedAnalysisMethod() {
		
		int row = getSelectedRow();
		if(row == -1)
			return null;
		
		return (AnalysisMethod) model.getValueAt(convertRowIndexToModel(row), 
				model.getColumnIndex(MethodTableModel.METHOD_ID_COLUMN));
	}
	
	public String getSelectedAnalysisMethodName() {
		
		int row = getSelectedRow();
		if(row == -1)
			return null;
		
		return (String) model.getValueAt(convertRowIndexToModel(row), 
				model.getColumnIndex(MethodTableModel.METHOD_NAME_COLUMN));
	}
	
	public Collection<DataAcquisitionMethod>getAvailableDataAcquisitionMethods(){
		
		Collection<DataAcquisitionMethod>available = new TreeSet<DataAcquisitionMethod>();
		int methodCol = model.getColumnIndex(MethodTableModel.METHOD_ID_COLUMN);
		for(int i=0; i<model.getRowCount(); i++) {
			
			Object value = model.getValueAt(i, methodCol);
			if(value != null && value instanceof DataAcquisitionMethod)
				available.add((DataAcquisitionMethod)value);
		}		
		return available;
	}
	
	public Collection<DataExtractionMethod>getAvailableDataExtractionMethods(){
		
		Collection<DataExtractionMethod>available = new TreeSet<DataExtractionMethod>();
		int methodCol = model.getColumnIndex(MethodTableModel.METHOD_ID_COLUMN);
		for(int i=0; i<model.getRowCount(); i++) {
			
			Object value = model.getValueAt(i, methodCol);
			if(value != null && value instanceof DataExtractionMethod)
				available.add((DataExtractionMethod)value);
		}		
		return available;
	}
	
	public Collection<String>getMissingMethodNames(){
		
		Collection<String>missing = new TreeSet<String>();
		int nameCol = model.getColumnIndex(MethodTableModel.METHOD_NAME_COLUMN);
		int methodCol = model.getColumnIndex(MethodTableModel.METHOD_ID_COLUMN);
		for(int i=0; i<model.getRowCount(); i++) {
			
			Object name = model.getValueAt(i, nameCol);
			Object method = model.getValueAt(i, methodCol);
			if(name != null && method == null)
				missing.add((String)name);
		}		
		return missing;
	}

	public Collection< String> getMethodNames() {

		Collection<String>names = new TreeSet<String>();
		int nameCol = model.getColumnIndex(MethodTableModel.METHOD_NAME_COLUMN);
		for(int i=0; i<model.getRowCount(); i++) {
			
			Object name = model.getValueAt(i, nameCol);
			if(name != null)
				names.add((String)name);
		}	
		return names;
	}

	public void removeSelectedRow() {
		
		int row = getSelectedRow();
		if(row == -1)
			return;
		
		thf.setTable(null);
		model.removeRow(convertRowIndexToModel(row));
		thf.setTable(this);
	}
}


















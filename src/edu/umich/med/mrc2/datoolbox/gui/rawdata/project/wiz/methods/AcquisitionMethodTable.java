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

package edu.umich.med.mrc2.datoolbox.gui.rawdata.project.wiz.methods;

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
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCash;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.AutoChoices;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.TableFilterHeader;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.AnalysisMethodRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.FileBaseNameRenderer;

public class AcquisitionMethodTable extends BasicTable {

	/**
	 *
	 */
	private static final long serialVersionUID = -1405921543482090501L;
	private AcquisitionMethodTableModel model;

	public AcquisitionMethodTable() {
		super();
		model =  new AcquisitionMethodTableModel();
		setModel(model);
		rowSorter = new TableRowSorter<AcquisitionMethodTableModel>(model);
		setRowSorter(rowSorter);
		rowSorter.setComparator(getColumnIndex(AcquisitionMethodTableModel.METHOD_ID_COLUMN), 
				new AnalysisMethodComparator(SortProperty.ID));
		
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		setDefaultRenderer(AnalysisMethod.class, new AnalysisMethodRenderer(SortProperty.ID));

		columnModel.getColumnById(AcquisitionMethodTableModel.METHOD_NAME_COLUMN)
				.setCellRenderer(new FileBaseNameRenderer());
		columnModel.getColumnById(AcquisitionMethodTableModel.METHOD_ID_COLUMN).setMaxWidth(80);
		
		columnModel.getColumnById(AcquisitionMethodTableModel.METHOD_ID_COLUMN).
				setPreferredWidth(80);
		
		thf = new TableFilterHeader(this, AutoChoices.ENABLED);
		finalizeLayout();
	}

	public void setTableModelFromAcquisitionMethodsNames(Collection<String> methodNames) {
		
		Map<String,AnalysisMethod>methodFilesMap = new TreeMap<String,AnalysisMethod>();
		for(String methodName : methodNames) {
			
			DataAcquisitionMethod existingMethod = 
					IDTDataCash.getAcquisitionMethodByName(methodName);
			
			methodFilesMap.put(methodName, existingMethod);
		}	
		thf.setTable(null);
		model.setTableModelFromMethods(methodFilesMap);
		thf.setTable(this);
		tca.adjustColumns();
	}
	
	public void setTableModelFromAcquisitionMethodsCollection(
			Collection<? extends AnalysisMethod> methods) {
		thf.setTable(null);
		model.setTableModelFromMethodCollection(methods);
		thf.setTable(this);
		tca.adjustColumns();
	}

	public void setTableModelFromDataAnalysisMethods(
			Collection<DataExtractionMethod> dataExtractionMethods) {

		Map<String,AnalysisMethod>methodFilesMap = new TreeMap<String,AnalysisMethod>();
		for(DataExtractionMethod mf : dataExtractionMethods)
			methodFilesMap.put(mf.getName(), mf);
		
		thf.setTable(null);
		model.setTableModelFromMethods(methodFilesMap);
		thf.setTable(this);
		tca.adjustColumns();
	}
	
	public AnalysisMethod getSelectedAnalysisMethod() {
		
		int row = getSelectedRow();
		if(row == -1)
			return null;
		
		return (AnalysisMethod) model.getValueAt(convertRowIndexToModel(row), 
				model.getColumnIndex(AcquisitionMethodTableModel.METHOD_ID_COLUMN));
	}
	
	public String getSelectedAnalysisMethodName() {
		
		int row = getSelectedRow();
		if(row == -1)
			return null;
		
		return (String) model.getValueAt(convertRowIndexToModel(row), 
				model.getColumnIndex(AcquisitionMethodTableModel.METHOD_NAME_COLUMN));
	}
	
	public Collection<DataAcquisitionMethod>getAvailableDataAcquisitionMethods(){
		
		Collection<DataAcquisitionMethod>available = new TreeSet<DataAcquisitionMethod>();
		int methodCol = model.getColumnIndex(AcquisitionMethodTableModel.METHOD_ID_COLUMN);
		for(int i=0; i<model.getRowCount(); i++) {
			
			Object value = model.getValueAt(i, methodCol);
			if(value != null && value instanceof DataAcquisitionMethod)
				available.add((DataAcquisitionMethod)value);
		}		
		return available;
	}
	
	public Collection<DataExtractionMethod>getAvailableDataExtractionMethods(){
		
		Collection<DataExtractionMethod>available = new TreeSet<DataExtractionMethod>();
		int methodCol = model.getColumnIndex(AcquisitionMethodTableModel.METHOD_ID_COLUMN);
		for(int i=0; i<model.getRowCount(); i++) {
			
			Object value = model.getValueAt(i, methodCol);
			if(value != null && value instanceof DataExtractionMethod)
				available.add((DataExtractionMethod)value);
		}		
		return available;
	}
	
	public Collection<String>getMissingMethodNames(){
		
		Collection<String>missing = new TreeSet<String>();
		int nameCol = model.getColumnIndex(AcquisitionMethodTableModel.METHOD_NAME_COLUMN);
		int methodCol = model.getColumnIndex(AcquisitionMethodTableModel.METHOD_ID_COLUMN);
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
		int nameCol = model.getColumnIndex(AcquisitionMethodTableModel.METHOD_NAME_COLUMN);
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
		
		model.removeRow(convertRowIndexToModel(row));
	}

	public void addMethods(Collection<DataAcquisitionMethod> toAdd) {
		
		thf.setTable(null);
		model.addMethods(toAdd);
		thf.setTable(this);
		tca.adjustColumns();
	}
}


















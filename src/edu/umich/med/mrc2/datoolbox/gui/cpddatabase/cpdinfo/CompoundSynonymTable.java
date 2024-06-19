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

package edu.umich.med.mrc2.datoolbox.gui.cpddatabase.cpdinfo;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.JCheckBox;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;

import edu.umich.med.mrc2.datoolbox.data.CompoundNameSet;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.editors.RadioButtonEditor;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.AutoChoices;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.TableFilterHeader;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.RadioButtonRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.WordWrapCellRenderer;

public class CompoundSynonymTable extends BasicTable {

	/**
	 *
	 */
	private static final long serialVersionUID = -7302585893077525162L;
	private CompoundNameSet currentNameSet;
	private SynonymTableModelListener modelListener;

	public CompoundSynonymTable() {
		super();

		model = new CompoundSynonymTableModel();
		setModel(model);
		rowSorter = new TableRowSorter<CompoundSynonymTableModel>(
				(CompoundSynonymTableModel)model);
		setRowSorter(rowSorter);
		TableColumn defaultColumn = 
				columnModel.getColumnById(CompoundSynonymTableModel.DEFAULT_COLUMN);
		defaultColumn.setCellRenderer(new RadioButtonRenderer());
		defaultColumn.setCellEditor(new RadioButtonEditor(new JCheckBox()));
		defaultColumn.setWidth(70);
		fixedWidthColumns.add(defaultColumn.getModelIndex());
		
		columnModel.getColumnById(CompoundSynonymTableModel.SYNONYM_COLUMN).
				setCellRenderer(new WordWrapCellRenderer());

		thf = new TableFilterHeader(this, AutoChoices.ENABLED);
		finalizeLayout();
	}
	
	public void disableEditing() {
		((CompoundSynonymTableModel)model).disableEditing();
	}

	@Override
	public synchronized void clearTable() {

		thf.setTable(null);
		model.removeTableModelListener(modelListener);
		super.clearTable();
		model.addTableModelListener(modelListener);
		thf.setTable(this);
	}

	public void setModelFromCompoundNameSet(CompoundNameSet nameSet) {

		thf.setTable(null);
		model.removeTableModelListener(modelListener);
		currentNameSet = nameSet;
		((CompoundSynonymTableModel)model).setModelFromCompoundNameSet(currentNameSet);		
		model.addTableModelListener(modelListener);
		thf.setTable(this);
		adjustColumns();
	}

	public Map<String, Boolean> getSelectedNames() {

		Map<String, Boolean>nameMap = new LinkedHashMap<String, Boolean>();
		if(getSelectedRowCount() == 0)
			return nameMap;

		int primaryCol = model.getColumnIndex(CompoundSynonymTableModel.DEFAULT_COLUMN);
		int nameCol = model.getColumnIndex(CompoundSynonymTableModel.SYNONYM_COLUMN);
		for(int i : getSelectedRows()) {
			int modelRow = convertRowIndexToModel(i);
			nameMap.put(
					(String)model.getValueAt(modelRow, nameCol), 
					(Boolean)model.getValueAt(modelRow, primaryCol));
		}
		return nameMap;
	}

	/**
	 * @return the currentNameSet
	 */
	public CompoundNameSet getCurrentNameSet() {
		return currentNameSet;
	}

	/**
	 * @param modelListener the modelListener to set
	 */
	public void setSynonymsModelListener(SynonymTableModelListener modelListener) {
		this.modelListener = modelListener;
	}
}




















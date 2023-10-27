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

package edu.umich.med.mrc2.datoolbox.gui.expsetup.expdesign;

import javax.swing.JCheckBox;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableRowSorter;

import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignSubset;
import edu.umich.med.mrc2.datoolbox.data.compare.ExpDesignSubsetComparator;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.editors.RadioButtonEditor;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.ExpDesignSubsetRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.RadioButtonRenderer;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class DesignSubsetTable extends BasicTable {

	/**
	 *
	 */
	private static final long serialVersionUID = 3448932509470903799L;

	private TableModelListener mlistener;
	private ExpDesignSubsetRenderer edsRenderer;

	public DesignSubsetTable(TableModelListener modelListener) {

		super();
		model = new DesignSubsetTableModel();
		setModel(model);
		rowSorter = new TableRowSorter<DesignSubsetTableModel>(
				(DesignSubsetTableModel)model);
		setRowSorter(rowSorter);
		rowSorter.setComparator(model.getColumnIndex(DesignSubsetTableModel.DESIGN_SUBSET_COLUMN),
				new ExpDesignSubsetComparator());
		
		putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		mlistener = modelListener;
		model.addTableModelListener(mlistener);

		edsRenderer = new ExpDesignSubsetRenderer();
		radioRenderer = new RadioButtonRenderer();
		radioEditor = new RadioButtonEditor(new JCheckBox());
		radioEditor.addCellEditorListener(this);

		columnModel.getColumnById(DesignSubsetTableModel.ACTIVE_COLUMN)
				.setCellRenderer(radioRenderer);
		columnModel.getColumnById(DesignSubsetTableModel.ACTIVE_COLUMN)
				.setCellEditor(radioEditor);
		columnModel.getColumnById(DesignSubsetTableModel.DESIGN_SUBSET_COLUMN)
				.setCellRenderer(edsRenderer);
		columnModel.getColumnById(DesignSubsetTableModel.ACTIVE_COLUMN).setWidth(50);
		fixedWidthColumns.add(model.getColumnIndex(DesignSubsetTableModel.ACTIVE_COLUMN));
		finalizeLayout();
	}

	//	TODO replace by table model listener
	@Override
	public void editingStopped(ChangeEvent event) {

		if (event.getSource().getClass().equals(RadioButtonEditor.class) && this.getSelectedRow() > -1) {

			int setColumn = model.getColumnIndex(DesignSubsetTableModel.DESIGN_SUBSET_COLUMN);
			int activeColumn = model.getColumnIndex(DesignSubsetTableModel.ACTIVE_COLUMN);
			int row = convertRowIndexToModel(getSelectedRow());

			boolean selected = (boolean) model.getValueAt(row, activeColumn);
			ExperimentDesignSubset selectedSubset = (ExperimentDesignSubset) model.getValueAt(row, setColumn);
			if(!selected) {

				if(!selectedSubset.isActive())
					MRC2ToolBoxCore.getActiveMetabolomicsExperiment().getExperimentDesign().setActiveDesignSubset(selectedSubset);

				((DesignSubsetTableModel)model).setModelFromProject(MRC2ToolBoxCore.getActiveMetabolomicsExperiment());
				super.editingStopped(event);
				return;
			}
		}
		super.editingStopped(event);
	}

	public void setModelFromProject(DataAnalysisProject currentProject) {

		((DesignSubsetTableModel)model).setModelFromProject(currentProject);
		columnModel.getColumnById(DesignSubsetTableModel.ACTIVE_COLUMN).setWidth(50);
	}

	public void selectActiveSubset() {

		int subIndex = model.getColumnIndex(DesignSubsetTableModel.DESIGN_SUBSET_COLUMN);
		for(int i=0; i<getRowCount(); i++) {

			ExperimentDesignSubset subset = (ExperimentDesignSubset) 
					model.getValueAt(convertRowIndexToModel(i), subIndex);
			if(subset.isActive()) {
				setRowSelectionInterval(i, i);
				break;
			}
		}
	}
}


























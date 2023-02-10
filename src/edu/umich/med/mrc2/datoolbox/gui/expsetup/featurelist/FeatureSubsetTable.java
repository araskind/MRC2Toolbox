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

package edu.umich.med.mrc2.datoolbox.gui.expsetup.featurelist;

import javax.swing.JCheckBox;
import javax.swing.ListSelectionModel;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableRowSorter;

import edu.umich.med.mrc2.datoolbox.data.MsFeatureSet;
import edu.umich.med.mrc2.datoolbox.data.compare.NameComparator;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.editors.RadioButtonEditor;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.CompoundFeatureSetRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.RadioButtonRenderer;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class FeatureSubsetTable extends BasicTable {

	/**
	 *
	 */
	private static final long serialVersionUID = -3122391202783544387L;
	private RadioButtonRenderer radioRenderer;
	private RadioButtonEditor radioEditor;
	private FeatureSubsetTableModel model;
	private CompoundFeatureSetRenderer cfsRenderer;
	private FeatureSubsetTableModelListener modelListener;

	public FeatureSubsetTable() {

		super();

		model = new FeatureSubsetTableModel();
		setModel(model);
		rowSorter = new TableRowSorter<FeatureSubsetTableModel>(model);
		setRowSorter(rowSorter);
		rowSorter.setComparator(model.getColumnIndex(FeatureSubsetTableModel.FEATURES_SUBSET_COLUMN),
				new NameComparator());
		
		modelListener = new FeatureSubsetTableModelListener();
		model.addTableModelListener(modelListener);

		putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		cfsRenderer = new CompoundFeatureSetRenderer();
		radioRenderer = new RadioButtonRenderer();
		radioEditor = new RadioButtonEditor(new JCheckBox());
		radioEditor.addCellEditorListener(this);

		columnModel.getColumnById(FeatureSubsetTableModel.ACTIVE_COLUMN)
			.setCellRenderer(radioRenderer);
		columnModel.getColumnById(FeatureSubsetTableModel.ACTIVE_COLUMN)
			.setCellEditor(radioEditor);
		columnModel.getColumnById(FeatureSubsetTableModel.FEATURES_SUBSET_COLUMN)
			.setCellRenderer(cfsRenderer);

		finalizeLayout();
	}

	private class FeatureSubsetTableModelListener implements TableModelListener {

		public void tableChanged(TableModelEvent e) {

			if(getRowCount() == 0)
				return;

			int row = e.getFirstRow();
			int col = e.getColumn();
			if (col == model.getColumnIndex(FeatureSubsetTableModel.ACTIVE_COLUMN) 
					&& e.getType() == TableModelEvent.UPDATE
					&& (boolean)model.getValueAt(row, col)) {

				DataAnalysisProject currentProject = MRC2ToolBoxCore.getActiveMetabolomicsExperiment();
				DataPipeline activeDataPipeline = currentProject.getActiveDataPipeline();
				MsFeatureSet selectedSet = (MsFeatureSet) model.getValueAt(row,
						model.getColumnIndex(FeatureSubsetTableModel.FEATURES_SUBSET_COLUMN));
				if(selectedSet.isActive())
					return;
				
				currentProject.getMsFeatureSetsForDataPipeline(activeDataPipeline).stream().forEach(s -> {
					s.setSuppressEvents(true);
					s.setActive(false);
					s.setSuppressEvents(false);
				});
				selectedSet.setActive(true);
//				for (MsFeatureSet msfs : currentProject.getMsFeatureSetsForDataPipeline(activeDataPipeline)) {
//
//					if(!msfs.equals(selectedSet))
//						msfs.setActive(false);
//				}
//				setModelFromProject(currentProject, activeDataPipeline);
			}
		}
	}

	public void setModelFromProject(DataAnalysisProject currentProject, DataPipeline activeDataPipeline) {

		model.removeTableModelListener(modelListener);
		model.setModelFromProject(currentProject, activeDataPipeline);
		columnModel.getColumnById(FeatureSubsetTableModel.ACTIVE_COLUMN).setWidth(30);
		columnModel.getColumnById(FeatureSubsetTableModel.NUM_FEATURES_COLUMN).setWidth(50);
		model.addTableModelListener(modelListener);
	}
}

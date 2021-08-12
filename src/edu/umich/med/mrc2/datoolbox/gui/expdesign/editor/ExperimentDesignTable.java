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

package edu.umich.med.mrc2.datoolbox.gui.expdesign.editor;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableCellRenderer;

import edu.umich.med.mrc2.datoolbox.data.ExperimentDesign;
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignLevel;
import edu.umich.med.mrc2.datoolbox.data.ExperimentalSample;
import edu.umich.med.mrc2.datoolbox.data.compare.ExperimentalSampleComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.enums.ParameterSetStatus;
import edu.umich.med.mrc2.datoolbox.data.format.ExperimentDesignLevelFormat;
import edu.umich.med.mrc2.datoolbox.data.format.ExperimentalSampleFormat;
import edu.umich.med.mrc2.datoolbox.gui.coderazzi.filters.gui.AutoChoices;
import edu.umich.med.mrc2.datoolbox.gui.coderazzi.filters.gui.TableFilterHeader;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.TableColumnAdjuster;
import edu.umich.med.mrc2.datoolbox.gui.tables.editors.ExperimentalLevelSelectorEditor;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.ExperimentalLevelRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.ExperimentalSampleRenderer;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class ExperimentDesignTable extends BasicTable {

	/**
	 *
	 */
	private static final long serialVersionUID = -8544713814473026420L;

	private ExperimentalLevelRenderer levelRenderer;
	private ExperimentalLevelSelectorEditor levelEditor;
	private ExperimentalSampleRenderer sampleRenderer;
	private DataAnalysisProject currentProject;
	private ExperimentDesign experimentDesign;
	private ExperimentDesignTableModel model;

	public ExperimentDesignTable() {
		this(null);
	}

	public ExperimentDesignTable(DataAnalysisProject cefAnalyzerProject) {

		super();

		currentProject = cefAnalyzerProject;

		if(currentProject != null)
			experimentDesign = currentProject.getExperimentDesign();

		setAutoCreateColumnsFromModel(false);
		setRowSelectionAllowed(true);
		setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		getTableHeader().setReorderingAllowed(false);

		model = new ExperimentDesignTableModel();
		if(experimentDesign != null)
			model.loadDesignFromDesignObject(experimentDesign);

		setModel(model);
		createDefaultColumnsFromModel();

		//	Column editors and renderers
		sampleRenderer = new ExperimentalSampleRenderer(SortProperty.ID);
		setDefaultRenderer(ExperimentalSample.class, sampleRenderer);
		levelRenderer = new ExperimentalLevelRenderer();
		setDefaultRenderer(ExperimentDesignLevel.class, levelRenderer);
		levelEditor = new ExperimentalLevelSelectorEditor(new JComboBox<ExperimentDesignLevel>());
		setDefaultEditor(ExperimentDesignLevel.class, levelEditor);
		finalizeTable();
	}

	private class DesignTableModelListener implements TableModelListener {

		public void tableChanged(TableModelEvent e) {

			int row = e.getFirstRow();
			int column = e.getColumn();

			if(e.getType() == TableModelEvent.UPDATE && row > -1 && column > -1) {

				ExperimentalSample sample = 
						(ExperimentalSample) getValueAt(row, 
								model.getColumnIndex(ExperimentDesignTableModel.SAMPLE_ID_COLUMN));

				//	Update sample name
				if(column == model.getColumnIndex(ExperimentDesignTableModel.SAMPLE_NAME_COLUMN)) {

					String newName = (String) model.getValueAt(row, column);
					if(!newName.equals(sample.getName())) {
						sample.setName(newName);
						experimentDesign.fireExperimentDesignEvent(ParameterSetStatus.CHANGED);
						return;
					}
				}
				//	Update design level
				if(model.getColumnClass(column).equals(ExperimentDesignLevel.class)) {

					ExperimentDesignLevel newLevel = (ExperimentDesignLevel) model.getValueAt(row, column);
					if(!sample.hasLevel(newLevel)) {

						sample.addDesignLevel(newLevel);
						experimentDesign.fireExperimentDesignEvent(ParameterSetStatus.CHANGED);
						return;
					}
				}
				//	Enable/disable sample
				if(column == model.getColumnIndex(ExperimentDesignTableModel.ENABLED_COLUMN)) {

					Boolean enabled =  (Boolean) model.getValueAt(row, column);
					if(!enabled.equals(sample.isEnabled())) {
						sample.setEnabled(enabled);
						experimentDesign.fireExperimentDesignEvent(ParameterSetStatus.CHANGED);
						return;
					}
				}
			}
		}
	}

	private void finalizeTable() {

		thf = new TableFilterHeader(this, AutoChoices.ENABLED);
		thf.getParserModel().setFormat(ExperimentalSample.class, new ExperimentalSampleFormat(SortProperty.ID));
		thf.getParserModel().setComparator(ExperimentalSample.class, new ExperimentalSampleComparator(SortProperty.ID));
		thf.getParserModel().setFormat(ExperimentDesignLevel.class, new ExperimentDesignLevelFormat(SortProperty.Name));
		tca = new TableColumnAdjuster(this);
		tca.adjustColumns();
		getColumnModel().getColumn(0).setMaxWidth(50);
		model.addTableModelListener(new DesignTableModelListener());
		addColumnSelectorPopup();
		setTablePopupEnabled(getModel().getRowCount() > 0);
	}

	public Collection<ExperimentalSample>getSelectedSamples(){

		Collection<ExperimentalSample>selected = new ArrayList<ExperimentalSample>();
		if(getSelectedRowCount() == 0)
			return selected;

		int col = model.getColumnIndex(ExperimentDesignTableModel.SAMPLE_ID_COLUMN);
		for(int row : getSelectedRows())
			selected.add((ExperimentalSample) model.getValueAt(convertRowIndexToModel(row), col));

		return selected;
	}

	public void setModelFromProject(DataAnalysisProject cefAnalyzerProject) {

		thf.setTable(null);
		currentProject = cefAnalyzerProject;
		experimentDesign = currentProject.getExperimentDesign();
		model = new ExperimentDesignTableModel();
		if(experimentDesign != null)
			model.loadDesignFromDesignObject(experimentDesign);

		setModel(model);
		createDefaultColumnsFromModel();
		finalizeTable();
	}

	public void setModelFromDesign(ExperimentDesign newDesign) {

		thf.setTable(null);
		currentProject = null;
		experimentDesign = newDesign;
		model = new ExperimentDesignTableModel();
		if(experimentDesign != null)
			model.loadDesignFromDesignObject(experimentDesign);

		setModel(model);
		createDefaultColumnsFromModel();
		finalizeTable();
	}

	@Override
	public void clearTable() {

		thf.setTable(null);
		model = new ExperimentDesignTableModel();
		setModel(model);
		createDefaultColumnsFromModel();
		thf.setTable(this);
		tca.adjustColumns();
		addColumnSelectorPopup();
		model.addTableModelListener(new DesignTableModelListener());
		setTablePopupEnabled(false);
	}

	//	Ommitt "Enabled" column
	public String getDesignDataAsString() {

		StringBuffer designtData = new StringBuffer();
		int numCols = this.getColumnCount();

		designtData.append(this.getColumnName(1));

		for(int i=2; i<numCols; i++)
			designtData.append("\t" + this.getColumnName(i));

		designtData.append("\n");

		for(int i=0;  i<this.getRowCount(); i++){

			for(int j=1; j<numCols; j++){

                final TableCellRenderer renderer = this.getCellRenderer(i, j);
                final Component comp = this.prepareRenderer(renderer, i, j);
                String txt = null;
                Object value = getValueAt(i, j);

                if(value instanceof Boolean)
                	txt = Boolean.toString((Boolean)value);

                if (comp instanceof JLabel)
                	txt = ((JLabel) comp).getText();

                if (comp instanceof JTextPane)
                	txt = ((JTextPane) comp).getText();

                if (comp instanceof JTextArea)
                	txt = ((JTextArea) comp).getText();

            	designtData.append(txt.trim());

                if(j<numCols-1)
                	designtData.append("\t");
                else
                	designtData.append("\n");
			}
		}
		return designtData.toString();
	}

	public void setSamplesEnabledStatus(boolean b) {

		if(getSelectedRowCount() == 0)
			return;

		int col = model.getColumnIndex(ExperimentDesignTableModel.SAMPLE_ID_COLUMN);
		for(int row : getSelectedRows())
			((ExperimentalSample) model.getValueAt(convertRowIndexToModel(row), col)).setEnabled(b);

		if(currentProject != null)
			currentProject.getExperimentDesign().fireExperimentDesignEvent(ParameterSetStatus.CHANGED);
	}

	public void invertEnabledSamples() {

		int col = model.getColumnIndex(ExperimentDesignTableModel.SAMPLE_ID_COLUMN);
		for(int i=0;  i<model.getRowCount(); i++) {

			ExperimentalSample sample = (ExperimentalSample) model.getValueAt(i, col);
			sample.setEnabled(!sample.isEnabled());
		}
		if(currentProject != null)
			currentProject.getExperimentDesign().fireExperimentDesignEvent(ParameterSetStatus.CHANGED);
	}
}





























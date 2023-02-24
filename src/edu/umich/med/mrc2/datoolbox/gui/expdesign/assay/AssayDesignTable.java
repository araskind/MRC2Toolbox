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

package edu.umich.med.mrc2.datoolbox.gui.expdesign.assay;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignLevel;
import edu.umich.med.mrc2.datoolbox.data.ExperimentalSample;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.editors.ExperimentalSampleSelectorEditor;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.AutoChoices;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.TableFilterHeader;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.DataFileCellRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.DateTimeCellRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.ExperimentalLevelRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.ExperimentalSampleRendererExtended;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class AssayDesignTable extends BasicTable {

	/**
	 *
	 */
	private static final long serialVersionUID = 3707183517390410985L;
	private AssayDesignTableModel model;
	private DataAnalysisProject currentProject;
	private DataPipeline activeDataPipeline;
	private AssayDesignTableModelListener modelListener;

	public AssayDesignTable() {

		super();

		setAutoCreateColumnsFromModel(false);
		model = new AssayDesignTableModel();
		setModel(model);
		getTableHeader().setReorderingAllowed(false);
		
		modelListener = new AssayDesignTableModelListener();
		model.addTableModelListener(modelListener);

		dtRenderer = new DateTimeCellRenderer();
		setDefaultRenderer(Date.class, dtRenderer);
		setDefaultRenderer(DataFile.class, 
				new DataFileCellRenderer());
		setDefaultRenderer(ExperimentalSample.class, 
				new ExperimentalSampleRendererExtended());
		setDefaultRenderer(ExperimentDesignLevel.class, 
				new ExperimentalLevelRenderer());
		
		addColumnSelectorPopup();
		//	finalizeLayout();
		fixedWidthColumns.add(0);
		fixedWidthColumns.add(1);
		thf = new TableFilterHeader(this, AutoChoices.ENABLED);
	}

	public void setTableModelFromExperimentDesign(
			DataAnalysisProject currentProject, DataPipeline activeDataPipeline) {

		this.currentProject = currentProject;
		this.activeDataPipeline = activeDataPipeline;
		thf.setTable(null);
		model.removeTableModelListener(modelListener);
		model.setTableModelFromExperimentDesign(currentProject, activeDataPipeline);
		createDefaultColumnsFromModel();
		if(currentProject != null)
			setDefaultEditor(ExperimentalSample.class,
					new ExperimentalSampleSelectorEditor(
							currentProject.getExperimentDesign().getSamples(), this));
		addColumnSelectorPopup();
		thf.setTable(this);
		getColumnModel().getColumn(0).setPreferredWidth(30);
		getColumnModel().getColumn(1).setPreferredWidth(30);
		tca.adjustColumnsExcluding(fixedWidthColumns);
		model.addTableModelListener(modelListener);
		setTablePopupEnabled(model.getRowCount() > 0);
	}

	public Collection<DataFile> getDataFiles(boolean selectedOnly) {

		Collection<DataFile> files = new ArrayList<DataFile>();
		int fileColumn = model.getColumnIndex(AssayDesignTableModel.DATA_FILE_COLUMN);
		if (selectedOnly) {
			for (int i : this.getSelectedRows())
				files.add((DataFile) model.getValueAt(convertRowIndexToModel(i), fileColumn));
		} else {
			for (int i = 0; i < model.getRowCount(); i++)
				files.add((DataFile) model.getValueAt(i, fileColumn));
		}
		return files.stream().filter(Objects::nonNull).collect(Collectors.toList());
	}

	public void setDataFileStatus(boolean enable, boolean selectedOnly) {

		for(DataFile dataFile : getDataFiles(selectedOnly))
			dataFile.setEnabled(enable);

		setTableModelFromExperimentDesign(currentProject, activeDataPipeline);
	}

    private class AssayDesignTableModelListener implements TableModelListener {

        public void tableChanged(TableModelEvent e) {

        	AssayDesignTableModel tm = (AssayDesignTableModel) e.getSource();
        	int row = e.getFirstRow();
        	int column = e.getColumn();

            if (column == model.getColumnIndex(AssayDesignTableModel.ENABLED_COLUMN)) {

            	DataFile df = (DataFile) tm.getValueAt(row, model.getColumnIndex(AssayDesignTableModel.DATA_FILE_COLUMN));
            	boolean enabled = (boolean) tm.getValueAt(row, column);
            	if(df != null)
            		df.setEnabled(enabled);
            }
            if (column == model.getColumnIndex(AssayDesignTableModel.SAMPLE_ID_COLUMN)) {

            	DataFile df = (DataFile) tm.getValueAt(row, model.getColumnIndex(AssayDesignTableModel.DATA_FILE_COLUMN));
    			ExperimentalSample sample = (ExperimentalSample) tm.getValueAt(row, column);
				if (df != null && sample != null) {

					if(df.getParentSample() != sample) {

						sample.addDataFile(df);
						currentProject.getExperimentDesign().getSamples().stream().
							filter(s -> !s.equals(sample)).
							forEach(s -> s.removeDataFile(df));
					}
					df.setParentSample(sample);
				}
				if (df != null && sample == null)
					df.setParentSample(null);
            }
        }
    }

	@Override
	public synchronized void clearTable() {

		thf.setTable(null);
		model.removeTableModelListener(modelListener);
		model.setTableModelFromExperimentDesign(null, null);
		createDefaultColumnsFromModel();
		model.addTableModelListener(modelListener);
		setTablePopupEnabled(false);
		thf.setTable(this);
	}
}





























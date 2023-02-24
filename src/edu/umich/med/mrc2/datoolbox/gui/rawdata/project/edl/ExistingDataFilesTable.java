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

package edu.umich.med.mrc2.datoolbox.gui.rawdata.project.edl;

import java.util.Collection;
import java.util.Date;
import java.util.Map;

import javax.swing.ListSelectionModel;
import javax.swing.table.TableRowSorter;

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.ExperimentalSample;
import edu.umich.med.mrc2.datoolbox.data.compare.AnalysisMethodComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.ExperimentalSampleComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.LIMSExperimentComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.format.AnalysisMethodFormat;
import edu.umich.med.mrc2.datoolbox.data.format.ExperimentalSampleFormat;
import edu.umich.med.mrc2.datoolbox.data.format.LIMSExperimentFormat;
import edu.umich.med.mrc2.datoolbox.data.lims.DataAcquisitionMethod;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSExperiment;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.AutoChoices;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.TableFilterHeader;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.DateTimeCellRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.ExperimentalSampleRendererExtended;

public class ExistingDataFilesTable extends BasicTable {

	/**
	 *
	 */
	private static final long serialVersionUID = 5111265849253755884L;
	private ExistingDataFilesTableModel model;

	public ExistingDataFilesTable() {
		super();
		model = new ExistingDataFilesTableModel();
		setModel(model);
		rowSorter = new TableRowSorter<ExistingDataFilesTableModel>(model);
		setRowSorter(rowSorter);
		
		rowSorter.setComparator(model.getColumnIndex(ExistingDataFilesTableModel.EXPERIMENT_COLUMN),
				new LIMSExperimentComparator(SortProperty.ID));
		rowSorter.setComparator(model.getColumnIndex(ExistingDataFilesTableModel.SAMPLE_COLUMN),
				new ExperimentalSampleComparator(SortProperty.Name));
		rowSorter.setComparator(model.getColumnIndex(ExistingDataFilesTableModel.ACQ_METHOD_COLUMN),
				new AnalysisMethodComparator(SortProperty.Name));
		
		setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		setDefaultRenderer(ExperimentalSample.class, new ExperimentalSampleRendererExtended());
		setDefaultRenderer(Date.class, new DateTimeCellRenderer());

		thf = new TableFilterHeader(this, AutoChoices.ENABLED);
		
		thf.getParserModel().setFormat(LIMSExperiment.class,
				new LIMSExperimentFormat());
		thf.getParserModel().setComparator(LIMSExperiment.class,
				new LIMSExperimentComparator(SortProperty.ID));
		thf.getParserModel().setFormat(ExperimentalSample.class,
				new ExperimentalSampleFormat(SortProperty.ID));
		thf.getParserModel().setComparator(ExperimentalSample.class,
				new ExperimentalSampleComparator(SortProperty.ID));
		thf.getParserModel().setComparator(DataAcquisitionMethod.class,
				new AnalysisMethodComparator(SortProperty.Name));
		thf.getParserModel().setFormat(DataAcquisitionMethod.class,
				new AnalysisMethodFormat(SortProperty.Name));

		finalizeLayout();
	}

	public void setTableModelFromExistingDataFiles(
			Map<LIMSExperiment, Collection<DataFile>>existingDataFiles) {
		
		thf.setTable(null);
		model.setTableModelFromExistingDataFiles(existingDataFiles);
		thf.setTable(this);
		tca.adjustColumns();
	}
}

























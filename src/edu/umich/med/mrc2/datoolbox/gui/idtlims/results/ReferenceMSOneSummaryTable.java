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

package edu.umich.med.mrc2.datoolbox.gui.idtlims.results;

import java.util.Collection;

import javax.swing.table.TableRowSorter;

import edu.umich.med.mrc2.datoolbox.data.ExperimentalSample;
import edu.umich.med.mrc2.datoolbox.data.compare.AnalysisMethodComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.ChromatographicColumnComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.ExperimentalSampleComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.LIMSExperimentComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.format.AnalysisMethodFormat;
import edu.umich.med.mrc2.datoolbox.data.format.ChromatographicColumnFormat;
import edu.umich.med.mrc2.datoolbox.data.format.ExperimentalSampleFormat;
import edu.umich.med.mrc2.datoolbox.data.format.LIMSExperimentFormat;
import edu.umich.med.mrc2.datoolbox.data.lims.DataAcquisitionMethod;
import edu.umich.med.mrc2.datoolbox.data.lims.DataExtractionMethod;
import edu.umich.med.mrc2.datoolbox.data.lims.IDTMsSummary;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSChromatographicColumn;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSExperiment;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.AutoChoices;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.TableFilterHeader;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.AnalysisMethodRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.ChromatographicColumnRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.ExperimentalSampleRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.LIMSExperimentRenderer;

public class ReferenceMSOneSummaryTable extends BasicTable {

	/**
	 *
	 */
	private static final long serialVersionUID = 8422881492134548457L;
	private ReferenceMSOneSummaryTableModel model;

	public ReferenceMSOneSummaryTable() {
		super();
		model = new ReferenceMSOneSummaryTableModel();
		setModel(model);
		rowSorter = new TableRowSorter<ReferenceMSOneSummaryTableModel>(model);
		setRowSorter(rowSorter);
		rowSorter.setComparator(model.getColumnIndex(ReferenceMSOneSummaryTableModel.ACQ_METHOD),
				new AnalysisMethodComparator(SortProperty.Name));
		rowSorter.setComparator(model.getColumnIndex(ReferenceMSOneSummaryTableModel.CHROMATOGRAPHIC_COLUMN),
				new ChromatographicColumnComparator(SortProperty.Name));
		rowSorter.setComparator(model.getColumnIndex(ReferenceMSOneSummaryTableModel.DA_METHOD),
				new AnalysisMethodComparator(SortProperty.Name));
		rowSorter.setComparator(model.getColumnIndex(ReferenceMSOneSummaryTableModel.SAMPLE_COLUMN),
				new ExperimentalSampleComparator(SortProperty.Name));
		rowSorter.setComparator(model.getColumnIndex(ReferenceMSOneSummaryTableModel.EXPERIMENT_COLUMN),
				new LIMSExperimentComparator(SortProperty.ID));		

		setDefaultRenderer(LIMSExperiment.class, new LIMSExperimentRenderer());
		setDefaultRenderer(ExperimentalSample.class, new ExperimentalSampleRenderer(SortProperty.Name));
		setDefaultRenderer(DataAcquisitionMethod.class, new AnalysisMethodRenderer());
		setDefaultRenderer(DataExtractionMethod.class, new AnalysisMethodRenderer());
		setDefaultRenderer(LIMSChromatographicColumn.class, new ChromatographicColumnRenderer());

		thf = new TableFilterHeader(this, AutoChoices.ENABLED);
		thf.getParserModel().setComparator(DataAcquisitionMethod.class,
				new AnalysisMethodComparator(SortProperty.Name));
		thf.getParserModel().setFormat(DataAcquisitionMethod.class,
				new AnalysisMethodFormat(SortProperty.Name));
		thf.getParserModel().setFormat(LIMSChromatographicColumn.class,
				new ChromatographicColumnFormat(SortProperty.Name));
		thf.getParserModel().setComparator(LIMSChromatographicColumn.class,
				new ChromatographicColumnComparator(SortProperty.Name));
		thf.getParserModel().setComparator(DataExtractionMethod.class,
				new AnalysisMethodComparator(SortProperty.Name));
		thf.getParserModel().setFormat(DataExtractionMethod.class,
				new AnalysisMethodFormat(SortProperty.Name));
		thf.getParserModel().setFormat(ExperimentalSample.class,
				new ExperimentalSampleFormat(SortProperty.ID));
		thf.getParserModel().setComparator(ExperimentalSample.class,
				new ExperimentalSampleComparator(SortProperty.ID));
		thf.getParserModel().setFormat(LIMSExperiment.class,
				new LIMSExperimentFormat());
		thf.getParserModel().setComparator(LIMSExperiment.class,
				new LIMSExperimentComparator(SortProperty.ID));

		finalizeLayout();
	}

	public void setTableModelFromSummaryCollection(Collection<IDTMsSummary>dataSummaries) {
		thf.setTable(null);
		model.setTableModelFromSummaryCollection(dataSummaries);
		thf.setTable(this);
		tca.adjustColumns();
	}
}

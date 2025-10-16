/*******************************************************************************
 * 
 * (C) Copyright 2018-2025 MRC2 (http://mrc2.umich.edu).
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

package edu.umich.med.mrc2.datoolbox.gui.mptrack.report;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

import javax.swing.ListSelectionModel;
import javax.swing.table.TableRowSorter;

import edu.umich.med.mrc2.datoolbox.data.compare.LIMSExperimentComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.LIMSUserComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.MoTrPACAssayComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.MoTrPACTissueCodeComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.format.LIMSExperimentFormat;
import edu.umich.med.mrc2.datoolbox.data.format.LIMSUserFormat;
import edu.umich.med.mrc2.datoolbox.data.format.MoTrPACAssayFormat;
import edu.umich.med.mrc2.datoolbox.data.format.MotrpacTissueCodeFormat;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSExperiment;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSUser;
import edu.umich.med.mrc2.datoolbox.data.motrpac.MoTrPACAssay;
import edu.umich.med.mrc2.datoolbox.data.motrpac.MoTrPACReport;
import edu.umich.med.mrc2.datoolbox.data.motrpac.MoTrPACReportCode;
import edu.umich.med.mrc2.datoolbox.data.motrpac.MoTrPACTissueCode;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.AutoChoices;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.TableFilterHeader;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.DateTimeCellRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.LIMSExperimentRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.MoTrPACReportCodeRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.MoTrPACReportDocumentTypeRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.MotrpacAssayRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.MotrpacTissueCodeRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.WordWrapCellRenderer;

public class MoTrPACReportTable extends BasicTable {

	/**
	 *
	 */
	private static final long serialVersionUID = -7297021115977628847L;
	public static final int iconSize = 24;
	
	public MoTrPACReportTable() {

		super();
		model = new MoTrPACReportTableModel();
		setModel(model);
		rowSorter = new TableRowSorter<MoTrPACReportTableModel>(
				(MoTrPACReportTableModel)model);
		setRowSorter(rowSorter);
		rowSorter.setComparator(model.getColumnIndex(MoTrPACReportTableModel.CREATED_BY_COLUMN),
				new LIMSUserComparator(SortProperty.Name));
		rowSorter.setComparator(model.getColumnIndex(MoTrPACReportTableModel.EXPERIMENT_COLUMN),
				new LIMSExperimentComparator(SortProperty.ID));
		rowSorter.setComparator(model.getColumnIndex(MoTrPACReportTableModel.TISSUE_COLUMN),
				new MoTrPACTissueCodeComparator(SortProperty.Name));
		rowSorter.setComparator(model.getColumnIndex(MoTrPACReportTableModel.ASSAY_COLUMN),
				new MoTrPACAssayComparator(SortProperty.Name));	
		rowSorter.setComparator(model.getColumnIndex(MoTrPACReportTableModel.FILE_DOWNLOAD_COLUMN),
				null);
		
		getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		createInteractiveUserRenderer(Arrays.asList(
				MoTrPACReportTableModel.CREATED_BY_COLUMN, 
				MoTrPACReportTableModel.FILE_DOWNLOAD_COLUMN));

		setDefaultRenderer(Date.class, new DateTimeCellRenderer());
		setDefaultRenderer(LIMSExperiment.class, new LIMSExperimentRenderer());
		setDefaultRenderer(MoTrPACAssay.class, new MotrpacAssayRenderer(SortProperty.Name));
		setDefaultRenderer(MoTrPACTissueCode.class, new MotrpacTissueCodeRenderer(SortProperty.Name));
		//	TODO is download implemented?
		setDefaultRenderer(MoTrPACReport.class, new MoTrPACReportDocumentTypeRenderer());	
		setDefaultRenderer(MoTrPACReportCode.class, new MoTrPACReportCodeRenderer());	
		
		columnModel.getColumnById(MoTrPACReportTableModel.DOCUMENT_NAME_COLUMN)
			.setCellRenderer(new WordWrapCellRenderer());

		thf = new TableFilterHeader(this, AutoChoices.ENABLED);
		thf.getParserModel().setFormat(LIMSUser.class, new LIMSUserFormat());
		thf.getParserModel().setComparator(LIMSUser.class, new LIMSUserComparator(SortProperty.Name));
		thf.getParserModel().setFormat(LIMSExperiment.class, new LIMSExperimentFormat());
		thf.getParserModel().setComparator(LIMSExperiment.class, new LIMSExperimentComparator(SortProperty.ID));
		thf.getParserModel().setFormat(MoTrPACAssay.class, new MoTrPACAssayFormat(SortProperty.Name));
		thf.getParserModel().setComparator(MoTrPACAssay.class, new MoTrPACAssayComparator(SortProperty.Name));
		thf.getParserModel().setFormat(MoTrPACTissueCode.class, new MotrpacTissueCodeFormat(SortProperty.Name));
		thf.getParserModel().setComparator(MoTrPACTissueCode.class, new MoTrPACTissueCodeComparator(SortProperty.Name));
		
		finalizeLayout();
	}
	
	public void setTableModelFromReports(Collection<MoTrPACReport>reports) {
		thf.setTable(null);
		((MoTrPACReportTableModel)model).setTableModelFromReports(reports);
		thf.setTable(this);
		adjustColumns();
	}
	
	public MoTrPACReport getSelectedReport() {
		
		int row = getSelectedRow();
		if(row == -1)
			return null;
		
		return (MoTrPACReport)model.getValueAt(
				convertRowIndexToModel(row), 
				model.getColumnIndex(MoTrPACReportTableModel.FILE_DOWNLOAD_COLUMN));
	}
}











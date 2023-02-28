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

package edu.umich.med.mrc2.datoolbox.gui.mptrack.report;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import edu.umich.med.mrc2.datoolbox.data.lims.LIMSExperiment;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSUser;
import edu.umich.med.mrc2.datoolbox.data.motrpac.MoTrPACAssay;
import edu.umich.med.mrc2.datoolbox.data.motrpac.MoTrPACReport;
import edu.umich.med.mrc2.datoolbox.data.motrpac.MoTrPACReportCode;
import edu.umich.med.mrc2.datoolbox.data.motrpac.MoTrPACReportCodeBlock;
import edu.umich.med.mrc2.datoolbox.data.motrpac.MoTrPACTissueCode;
import edu.umich.med.mrc2.datoolbox.database.mp.MoTrPACDatabaseCash;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTableModel;
import edu.umich.med.mrc2.datoolbox.gui.tables.ColumnContext;

public class MoTrPACReportTableModel extends BasicTableModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3934304420978080317L;
//	public static final String REPORT_COLUMN = "ID";
	public static final String DOCUMENT_NAME_COLUMN = "Report file name";
	public static final String DOCUMENT_VERSION_COLUMN = "Version";
	public static final String CREATED_BY_COLUMN = "Created by";
	public static final String DATE_CREATED_COLUMN = "Created on";	
	public static final String EXPERIMENT_COLUMN = "Experiment";
	public static final String ASSAY_COLUMN = "Assay";
	public static final String TISSUE_COLUMN = "Tissue";
	public static final String FILE_DOWNLOAD_COLUMN = "Download";

	public MoTrPACReportTableModel() {

		super();
		Collection<MoTrPACReportCodeBlock> codeBlocks = MoTrPACDatabaseCash.getMotrpacReportCodeBlocks();
		columnArray = new ColumnContext[8 + codeBlocks.size()];
		columnArray[0] = new ColumnContext(DOCUMENT_NAME_COLUMN, String.class, false);	
		columnArray[1] = new ColumnContext(DOCUMENT_VERSION_COLUMN, Integer.class, false);
		columnArray[2] = new ColumnContext(CREATED_BY_COLUMN, LIMSUser.class, false);	
		columnArray[3] = new ColumnContext(DATE_CREATED_COLUMN, Date.class, false);	
		columnArray[4] = new ColumnContext(EXPERIMENT_COLUMN, LIMSExperiment.class, false);	
		columnArray[5] = new ColumnContext(ASSAY_COLUMN, MoTrPACAssay.class, false);
		columnArray[6] = new ColumnContext(TISSUE_COLUMN, MoTrPACTissueCode.class, false);	
		columnArray[7] = new ColumnContext(FILE_DOWNLOAD_COLUMN, MoTrPACReport.class, false);
		int columnCount = 8;
		for(MoTrPACReportCodeBlock block : codeBlocks) {
			columnArray[columnCount] = 
					new ColumnContext(block.getBlockId(), MoTrPACReportCode.class, false);
			columnCount++;
		}
	}

	public void setTableModelFromReports(Collection<MoTrPACReport> reports) {
		
		setRowCount(0);
		if(reports == null || reports.isEmpty())
			return;
		
		Collection<MoTrPACReportCodeBlock> codeBlocks = 
				MoTrPACDatabaseCash.getMotrpacReportCodeBlocks();
		
		List<Object[]>rowDataList = new ArrayList<Object[]>();
		for(MoTrPACReport report : reports) {
			
			Object[] rowData = new Object[8 + codeBlocks.size()];
			rowData[0] = report.getLinkedDocumentName();
			rowData[1] = report.getVersionNumber();
			rowData[2] = report.getCreateBy();
			rowData[3] = report.getDateCreated();
			rowData[4] = report.getExperiment();
			rowData[5] = report.getAssay();
			rowData[6] = report.getTissueCode();
			rowData[7] = report;
			int columnCount = 8;
			
			Map<MoTrPACReportCodeBlock, MoTrPACReportCode> reportStage = 
					report.getReportStage();
			for(MoTrPACReportCodeBlock block : codeBlocks) {
				rowData[columnCount] = reportStage.get(block);
				columnCount++;
			}
			rowDataList.add(rowData);
		}
		addRows(rowDataList);
	}
}




















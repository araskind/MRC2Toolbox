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

package edu.umich.med.mrc2.datoolbox.gui.idworks.search.byexp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import edu.umich.med.mrc2.datoolbox.data.enums.Polarity;
import edu.umich.med.mrc2.datoolbox.data.lims.DataAcquisitionMethod;
import edu.umich.med.mrc2.datoolbox.data.lims.DataExtractionMethod;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTableModel;
import edu.umich.med.mrc2.datoolbox.gui.tables.ColumnContext;

public class DataPipelinesTableModel extends BasicTableModel {

	/**
	 *
	 */
	private static final long serialVersionUID = 7387427636234648515L;

	public static final String POLARITY_COLUMN = "Polarity";
	public static final String DATA_ACQ_METHOD_COLUMN = "Data acquisition method";
	public static final String DATA_PROC_METHOD_COLUMN = "Data processing method";
	public static final String CODE_COLUMN = "Code";

	public DataPipelinesTableModel() {
		super();
		columnArray = new ColumnContext[] {
			new ColumnContext(POLARITY_COLUMN, POLARITY_COLUMN, Polarity.class, false),
			new ColumnContext(DATA_ACQ_METHOD_COLUMN, DATA_ACQ_METHOD_COLUMN, DataAcquisitionMethod.class, false),
			new ColumnContext(DATA_PROC_METHOD_COLUMN, DATA_PROC_METHOD_COLUMN, DataExtractionMethod.class, false),
			new ColumnContext(CODE_COLUMN, "Data pipeline code", DataPipeline.class, false)
		};
	}

	public void setTableModelFromDataPipelineCollection(
			Collection<DataPipeline>pipelines) {

		setRowCount(0);
		if(pipelines == null || pipelines.isEmpty())
			return;
		
		List<Object[]>rowData = new ArrayList<Object[]>();
		for (DataPipeline pipeline : pipelines) {

			Object[] obj = {
					pipeline.getAcquisitionMethod().getPolarity(),
					pipeline.getAcquisitionMethod(),
					pipeline.getDataExtractionMethod(),
					pipeline,
				};
			rowData.add(obj);
		}
		if(!rowData.isEmpty())
			addRows(rowData);
	}
}
















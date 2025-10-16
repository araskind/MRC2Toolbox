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

package edu.umich.med.mrc2.datoolbox.gui.idtlims.prep;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import edu.umich.med.mrc2.datoolbox.data.ExperimentalSample;
import edu.umich.med.mrc2.datoolbox.data.IDTExperimentalSample;
import edu.umich.med.mrc2.datoolbox.data.StockSample;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTableModel;
import edu.umich.med.mrc2.datoolbox.gui.tables.ColumnContext;

public class PrepSampleTableModel extends BasicTableModel {

	/**
	 *
	 */
	private static final long serialVersionUID = 5870792768594170904L;
	public static final String ENABLED_COLUMN = "Add";
	public static final String SAMPLE_ID_COLUMN = "Sample ID";
	public static final String SAMPLE_NAME_COLUMN = "Sample name";
	public static final String SAMPLE_DESCRIPTION_COLUMN = "Description";
	public static final String PARENT_SAMPLE_ID_COLUMN = "Stock sample ID";
	public static final String PARENT_SAMPLE_NAME_COLUMN = "Stock sample name";
	public static final String PARENT_SAMPLE_TYPE_COLUMN = "Sample type";
	public static final String PARENT_SAMPLE_SPECIES_COLUMN = "Species";
	public static final String PARENT_SAMPLE_EXTERNAL_SOURCE_COLUMN = "External source";
	public static final String PARENT_SAMPLE_EXTERNAL_ID_COLUMN = "External ID";

	public PrepSampleTableModel() {
		super();
		columnArray = new ColumnContext[] {
			new ColumnContext(ENABLED_COLUMN, ENABLED_COLUMN, Boolean.class, true),
			new ColumnContext(SAMPLE_ID_COLUMN, SAMPLE_ID_COLUMN, IDTExperimentalSample.class, false),
			new ColumnContext(SAMPLE_NAME_COLUMN, SAMPLE_NAME_COLUMN, String.class, false),
			new ColumnContext(SAMPLE_DESCRIPTION_COLUMN, SAMPLE_DESCRIPTION_COLUMN, String.class, false),
			new ColumnContext(PARENT_SAMPLE_ID_COLUMN, PARENT_SAMPLE_ID_COLUMN, StockSample.class, false),
			new ColumnContext(PARENT_SAMPLE_NAME_COLUMN, PARENT_SAMPLE_NAME_COLUMN, String.class, false),
			new ColumnContext(PARENT_SAMPLE_TYPE_COLUMN, PARENT_SAMPLE_TYPE_COLUMN, String.class, false),
			new ColumnContext(PARENT_SAMPLE_SPECIES_COLUMN, PARENT_SAMPLE_SPECIES_COLUMN, String.class, false),
			new ColumnContext(PARENT_SAMPLE_EXTERNAL_SOURCE_COLUMN, PARENT_SAMPLE_EXTERNAL_SOURCE_COLUMN, String.class, false),
			new ColumnContext(PARENT_SAMPLE_EXTERNAL_ID_COLUMN, PARENT_SAMPLE_EXTERNAL_ID_COLUMN, String.class, false),
		};
	}

	public void setTableModelFromSamples(Collection<? extends ExperimentalSample>samples) {

		setRowCount(0);
		if(samples == null || samples.isEmpty())
			return;

		List<Object[]>rowData = new ArrayList<Object[]>();
		for (ExperimentalSample sample : samples) {

			StockSample ss = ((IDTExperimentalSample)sample).getParentStockSample();
			Object[] obj = {
				true,
				sample,
				sample.getName(),
				((IDTExperimentalSample)sample).getDescription(),
				ss,
				ss.getSampleName(),
				ss.getLimsSampleType(),
				ss.getSpecies(),
				ss.getExternalSource(),
				ss.getExternalId()
			};
			rowData.add(obj);
		}
		if(!rowData.isEmpty())
			addRows(rowData);
	}
}

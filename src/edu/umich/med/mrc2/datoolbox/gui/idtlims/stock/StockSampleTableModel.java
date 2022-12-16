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

package edu.umich.med.mrc2.datoolbox.gui.idtlims.stock;

import java.util.Collection;

import edu.umich.med.mrc2.datoolbox.data.StockSample;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTableModel;
import edu.umich.med.mrc2.datoolbox.gui.tables.ColumnContext;

public class StockSampleTableModel extends BasicTableModel {

	/**
	 *
	 */
	private static final long serialVersionUID = -1135263422810449762L;
	public static final String SAMPLE_ID_COLUMN = "Stock sample ID";
	public static final String SAMPLE_NAME_COLUMN = "Name";
	public static final String SAMPLE_DESCRIPTION_COLUMN = "Description";
	public static final String SAMPLE_TYPE_COLUMN = "Sample type";
	public static final String SAMPLE_SPECIES_COLUMN = "Species";
	public static final String LIMS_EXPERIMENT_COLUMN = "LIMS experiment";
	public static final String SAMPLE_EXTERNAL_SOURCE_COLUMN = "External source";
	public static final String SAMPLE_EXTERNAL_ID_COLUMN = "External ID";

	public StockSampleTableModel() {

		super();
		columnArray = new ColumnContext[] {
			new ColumnContext(SAMPLE_ID_COLUMN, StockSample.class, false),
			new ColumnContext(SAMPLE_NAME_COLUMN, String.class, false),
			new ColumnContext(SAMPLE_DESCRIPTION_COLUMN, String.class, false),
			new ColumnContext(SAMPLE_TYPE_COLUMN, String.class, false),
			new ColumnContext(SAMPLE_SPECIES_COLUMN, String.class, false),
			new ColumnContext(LIMS_EXPERIMENT_COLUMN, String.class, false),
			new ColumnContext(SAMPLE_EXTERNAL_SOURCE_COLUMN, String.class, false),
			new ColumnContext(SAMPLE_EXTERNAL_ID_COLUMN, String.class, false),
		};
	}

	public void setTableModelFromSamples(Collection<StockSample>samples) {

		setRowCount(0);

		if(samples.isEmpty())
			return;

		for (StockSample sample : samples) {
			
			String limsExperimentId = "";
			if(sample.getLimsExperiment() != null)
				limsExperimentId = sample.getLimsExperiment().getId();

			Object[] obj = {

				sample,
				sample.getSampleName(),
				sample.getSampleDescription(),
				sample.getLimsSampleType(),
				sample.getSpecies(),
				limsExperimentId,
				sample.getExternalSource(),
				sample.getExternalId()
			};
			super.addRow(obj);
		}
	}
}

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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import edu.umich.med.mrc2.datoolbox.data.IDTExperimentalSample;
import edu.umich.med.mrc2.datoolbox.data.lims.DataAcquisitionMethod;
import edu.umich.med.mrc2.datoolbox.data.lims.DataExtractionMethod;
import edu.umich.med.mrc2.datoolbox.data.lims.IDTMsSummary;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSChromatographicColumn;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSExperiment;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTableModel;
import edu.umich.med.mrc2.datoolbox.gui.tables.ColumnContext;

public class ReferenceMSMSSummaryTableModel extends BasicTableModel {

	/**
	 *
	 */
	private static final long serialVersionUID = 8801188983251641288L;

	public static final String EXPERIMENT_COLUMN = "Experiment";
	public static final String SAMPLE_COLUMN = "Sample";
	public static final String SAMPLE_TYPE_COLUMN = "Sample type";
	public static final String SAMPLE_SPECIES_COLUMN = "Species";
	public static final String ACQ_METHOD = "Acquisition method";
	public static final String POLARITY_COLUMN = "Polarity";
	public static final String CHROMATOGRAPHIC_COLUMN = "Column";
	public static final String DA_METHOD = "DA method";
	public static final String COLLISION_ENERGY = "Collision energy";
	public static final String NUM_FEATURES = "# of features";

	public ReferenceMSMSSummaryTableModel() {

		super();
		columnArray = new ColumnContext[] {
			new ColumnContext(EXPERIMENT_COLUMN, EXPERIMENT_COLUMN, LIMSExperiment.class, false),
			new ColumnContext(SAMPLE_COLUMN, SAMPLE_COLUMN, IDTExperimentalSample.class, false),
			new ColumnContext(SAMPLE_TYPE_COLUMN, SAMPLE_TYPE_COLUMN, String.class, false),
			new ColumnContext(SAMPLE_SPECIES_COLUMN, SAMPLE_SPECIES_COLUMN, String.class, false),
			new ColumnContext(ACQ_METHOD, ACQ_METHOD, DataAcquisitionMethod.class, false),
			new ColumnContext(POLARITY_COLUMN, POLARITY_COLUMN, String.class, false),
			new ColumnContext(COLLISION_ENERGY, COLLISION_ENERGY, Double.class, false),
			new ColumnContext(CHROMATOGRAPHIC_COLUMN, "Chromatographic column", LIMSChromatographicColumn.class, false),
			new ColumnContext(DA_METHOD, "Data extraction method", DataExtractionMethod.class, false),
			new ColumnContext(NUM_FEATURES,"Number of features",  Integer.class, false),
		};
	}

	public void setTableModelFromSummaryCollection(Collection<IDTMsSummary>dataSummaries) {

		setRowCount(0);

		if(dataSummaries == null || dataSummaries.isEmpty())
			return;

		List<Object[]>rowData = new ArrayList<Object[]>();
		for (IDTMsSummary s : dataSummaries) {

			Object[] obj = {
					s.getExperiment(),
					s.getSample(),
					s.getSample().getParentStockSample().getLimsSampleType(),
					s.getSample().getParentStockSample().getSpecies(),
					s.getAcquisitionMethod(),
					s.getAcquisitionMethod().getPolarity().name(),
					s.getCollisionEnergy(),
					s.getAcquisitionMethod().getColumn(),
					s.getDataExtractionMethod(),
					s.getFeatureCount(),
			};
			rowData.add(obj);
		}
		if(!rowData.isEmpty())
			addRows(rowData);
	}
}


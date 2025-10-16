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

package edu.umich.med.mrc2.datoolbox.gui.plot.dataset;

import java.util.Collection;
import java.util.Map;

import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset;

import edu.umich.med.mrc2.datoolbox.data.Assay;
import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignFactor;
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignSubset;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.enums.DataScale;
import edu.umich.med.mrc2.datoolbox.data.enums.FileSortingOrder;
import edu.umich.med.mrc2.datoolbox.data.enums.PlotDataGrouping;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class BoxplotDataSet extends DefaultBoxAndWhiskerCategoryDataset {

	/**
	 *
	 */
	private static final long serialVersionUID = -735602867894943589L;
	private Map<Assay, DataFile[]>dataFileMap;
	private DataAnalysisProject experiment;
	private MsFeature[] featuresToPlot;

	public BoxplotDataSet(
			Collection<MsFeature> dataSetStats,
			FileSortingOrder sortingOrder,
			DataScale dataScale,
			ExperimentDesignSubset activeDesign,
			PlotDataGrouping groupingType,
			ExperimentDesignFactor category,
			ExperimentDesignFactor subCategory,
			boolean splitByBatch) {

		// TODO Auto-generated constructor stub
	}

	public MsFeature[] getPlottedFeatures() {
		return featuresToPlot;
	}
}

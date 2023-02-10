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

package edu.umich.med.mrc2.datoolbox.gui.plot.dataset;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset;

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignFactor;
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignSubset;
import edu.umich.med.mrc2.datoolbox.data.ExperimentalSample;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.enums.DataScale;
import edu.umich.med.mrc2.datoolbox.data.enums.FileSortingOrder;
import edu.umich.med.mrc2.datoolbox.data.enums.PlotDataGrouping;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.plot.stats.StatsPlotType;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class BoxAndWhiskerCategoryDatasetCa extends DefaultBoxAndWhiskerCategoryDataset{

	/**
	 *
	 */
	private static final long serialVersionUID = 7334649551613689395L;
	private DataAnalysisProject project;
	private MsFeature[] featuresToPlot;

	public MsFeature[] getFeaturesToPlot() {
		return featuresToPlot;
	}

	public BoxAndWhiskerCategoryDatasetCa(
			Map<DataPipeline,Collection<MsFeature>> selectedFeaturesMap,
			StatsPlotType boxplotType,
			FileSortingOrder sortingOrder,
			DataScale dataScale,
			ExperimentDesignSubset activeDesign,
			PlotDataGrouping groupingType,
			ExperimentDesignFactor category,
			ExperimentDesignFactor subCategory,
			boolean splitByBatch) {

		super();
		project = MRC2ToolBoxCore.getActiveMetabolomicsExperiment();
		featuresToPlot = selectedFeaturesMap.values().stream().
				flatMap(c -> c.stream()).toArray(size -> new MsFeature[size]);

		//	Collect data
		Collection<ExperimentalSample> samples = 
				project.getExperimentDesign().getSamplesForDesignSubset(activeDesign);

		for (Entry<DataPipeline, Collection<MsFeature>> entry : selectedFeaturesMap.entrySet()) {
			
			for(MsFeature msf : entry.getValue()) {

				Set<DataFile> files = samples.stream().
						flatMap(s -> s.getDataFilesForMethod(entry.getKey().getAcquisitionMethod()).stream()).
						filter(s -> s.isEnabled()).sorted().
						collect(Collectors.toCollection(LinkedHashSet::new));
				
				Map<DataFile, Double> dataMap = 
						PlotDataSetUtils.getNormalizedDataForFeature(project, msf, entry.getKey(),  files, dataScale);
				Map<String, DataFile[]> seriesFileMap = 
						PlotDataSetUtils.createSeriesFileMap(entry.getKey(), files,
						sortingOrder, activeDesign, groupingType, category, subCategory, splitByBatch);

				//	Add data
				for (Entry<String, DataFile[]> seriesEntry : seriesFileMap.entrySet()) {

					Integer count = 1;
					ArrayList<Double>seriesValues = new ArrayList<Double>();

					for(DataFile df : seriesEntry.getValue())
						seriesValues.add(dataMap.get(df));

					if(boxplotType.equals(StatsPlotType.BOXPLOT_BY_FEATURE))
						add(seriesValues, entry.getKey(), msf.getName());

					if(boxplotType.equals(StatsPlotType.BOXPLOT_BY_GROUP))
						add(seriesValues, msf.getName(), entry.getKey());
				}
			}
		}
	}
}


























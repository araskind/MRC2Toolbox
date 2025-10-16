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

import org.jfree.data.xy.XYSeriesCollection;

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.DataFileStatisticalSummary;
import edu.umich.med.mrc2.datoolbox.data.enums.DataSetQcField;
import edu.umich.med.mrc2.datoolbox.data.enums.FileSortingOrder;
import edu.umich.med.mrc2.datoolbox.gui.plot.qc.twod.TwoDqcPlotParameterObject;

public class QcScatterDataSet extends XYSeriesCollection {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public QcScatterDataSet(
			Collection<DataFileStatisticalSummary> dataSetStats, 
			FileSortingOrder sortingOrder,
			DataSetQcField statsField) {
		
		if (statsField != DataSetQcField.RAW_VALUES) {

			DataFile[] files = dataSetStats.stream().
					map(s -> s.getFile()).sorted().
					toArray(size -> new DataFile[size]);
			
			int count = 1;
			NamedXYSeries series = new NamedXYSeries(statsField.getName());

			for (DataFile f : files) {

				for (DataFileStatisticalSummary stats : dataSetStats) {

					if (f.equals(stats.getFile())) {

						series.add(count, stats.getProperty(statsField).doubleValue(), f.getName());
						count++;
					}
				}
			}
			addSeries(series);
		}
		// TODO Auto-generated constructor stub
	}
	
	public QcScatterDataSet(
			Collection<DataFileStatisticalSummary> dataSetStats, 
			DataSetQcField statsField) {
		
		if (statsField != DataSetQcField.RAW_VALUES) {

			DataFile[] files = dataSetStats.stream().map(s -> s.getFile()).sorted().toArray(size -> new DataFile[size]);
			
			int count = 1;
			NamedXYSeries series = new NamedXYSeries(statsField.getName());

			for (DataFile f : files) {

				for (DataFileStatisticalSummary stats : dataSetStats) {

					if (f.equals(stats.getFile())) {

						series.add(count, stats.getProperty(statsField).doubleValue(), f.getName());
						count++;
					}
				}
			}
			addSeries(series);
		}
	}

	public QcScatterDataSet(TwoDqcPlotParameterObject plotParameters) {
		this(plotParameters.getDataSetStats(), 
				plotParameters.getSortingOrder(),
				plotParameters.getStatsField());
	}
}

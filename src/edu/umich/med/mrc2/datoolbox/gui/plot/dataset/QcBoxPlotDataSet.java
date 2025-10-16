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
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset;

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.DataFileStatisticalSummary;
import edu.umich.med.mrc2.datoolbox.data.compare.DataFileComparator;
import edu.umich.med.mrc2.datoolbox.data.enums.FileSortingOrder;
import edu.umich.med.mrc2.datoolbox.gui.plot.qc.twod.TwoDqcPlotParameterObject;

public class QcBoxPlotDataSet extends DefaultBoxAndWhiskerCategoryDataset {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public QcBoxPlotDataSet(Collection<DataFileStatisticalSummary> dataSetStats, FileSortingOrder sortingOrder) {


		List<DataFile>files = dataSetStats.stream().
				map(s -> s.getFile()).distinct().
				sorted(new DataFileComparator(sortingOrder)).
				collect(Collectors.toList());
		
		 Map<DataFile, DataFileStatisticalSummary> map = dataSetStats.stream()
			      .collect(Collectors.toMap(DataFileStatisticalSummary::getFile, Function.identity()));

		 files.stream().forEach(f -> add(map.get(f).getBoxplotItem(), f.getName(), ""));
	}

	public QcBoxPlotDataSet(TwoDqcPlotParameterObject plotParameters) {
		this(plotParameters.getDataSetStats(), 
				plotParameters.getSortingOrder());
	}

}

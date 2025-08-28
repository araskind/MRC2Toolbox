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

import java.util.TreeSet;

import org.jfree.data.xy.XYSeriesCollection;
import org.ujmp.core.Matrix;

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.ExperimentalSample;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.utils.DataSetUtils;

public class FeatureCorrelationPlotDataSet extends XYSeriesCollection {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public FeatureCorrelationPlotDataSet(
			MsFeature fOne, 
			DataPipeline dataPipelineOne,
			MsFeature fTwo,
			DataPipeline dataPipelineTwo,
			DataAnalysisProject experiment) {
		super();
		
		NamedXYSeries series = new NamedXYSeries("Data");
		
		//		Get data matrices for merged features if necessary
		
		Matrix matrixOne = experiment.getDataMatrixForDataPipeline(dataPipelineOne);
		Matrix matrixTwo = experiment.getDataMatrixForDataPipeline(dataPipelineTwo);

		long[] coordinatesOne = new long[2];
		long[] coordinatesTwo = new long[2];
		
		coordinatesOne[1] = DataSetUtils.getColumnForFeature(
				matrixOne, fOne, experiment, dataPipelineOne);
		if(coordinatesOne[1] == -1)
			return;
		
		coordinatesTwo[1] = DataSetUtils.getColumnForFeature(
				matrixTwo, fTwo, experiment, dataPipelineTwo);
		if(coordinatesTwo[1] == -1)
			return;

		TreeSet<ExperimentalSample> samples =
				experiment.getExperimentDesign().getActiveSamplesForDesignSubset(
						experiment.getExperimentDesign().getActiveDesignSubset());
		DataFile[] filesOne;
		DataFile[] filesTwo;

		for(ExperimentalSample sample : samples) {

			filesOne = sample.getDataFileArrayForMethod(dataPipelineOne.getAcquisitionMethod());
			filesTwo = sample.getDataFileArrayForMethod(dataPipelineTwo.getAcquisitionMethod());

			int count = Math.min(filesOne.length, filesTwo.length);

			for(int i=0; i<count; i++) {

				if(filesOne[i].isEnabled() && filesTwo[i].isEnabled()) {

					coordinatesOne[0] = matrixOne.getRowForLabel(filesOne[i]);
					coordinatesTwo[0] = matrixTwo.getRowForLabel(filesTwo[i]);
					double x = 0.0d;
					double y = 0.0d;
					String label = createDataFilePointLabel(
							filesOne[i], dataPipelineOne, filesTwo[i], dataPipelineTwo);
					
					if(coordinatesOne[0] >=0)
						x = matrixOne.getAsDouble(coordinatesOne);
					
					if(coordinatesTwo[0] >=0)
						y = matrixTwo.getAsDouble(coordinatesTwo);

					if(x > 0 && y > 0)
						series.add(x, y, label);
				}
			}
		}
		addSeries(series);
	}
	
	private String createDataFilePointLabel(
			DataFile fOne, 
			DataPipeline dataPipelineOne,
			DataFile fTwo,
			DataPipeline dataPipelineTwo) {
		
		StringBuilder sb = new StringBuilder(200);
		sb.append("<HTML>");

		if(dataPipelineOne.equals(dataPipelineTwo)) {
			
			sb.append(dataPipelineOne.getName());
			sb.append("<BR>");
			if(fOne.equals(fTwo))
				sb.append(fOne.getName());
			else {
				sb.append(fOne.getName());
				sb.append("<BR>");
				sb.append(fTwo.getName());
			}
		}
		else {
			if(fOne.equals(fTwo)) {
				sb.append(fOne.getName());
				sb.append("<BR>");
				sb.append(dataPipelineOne.getName());
				sb.append("<BR>");
				sb.append(dataPipelineTwo.getName());
			}
			else {
				sb.append(dataPipelineOne.getName());
				sb.append(": ");
				sb.append(fOne.getName());					
				sb.append("<BR>");
				sb.append(dataPipelineTwo.getName());
				sb.append(": ");
				sb.append(fTwo.getName());
			}
		}
		return sb.toString();
	}

}

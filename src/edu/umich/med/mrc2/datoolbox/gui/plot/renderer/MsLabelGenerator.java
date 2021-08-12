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

package edu.umich.med.mrc2.datoolbox.gui.plot.renderer;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.labels.XYItemLabelGenerator;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYDataset;

import edu.umich.med.mrc2.datoolbox.gui.plot.LCMSPlotPanel;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;

public class MsLabelGenerator implements XYItemLabelGenerator {

	/*
	 * Number of screen pixels to reserve for each label, so that the labels do
	 * not overlap
	 */
	public static final int POINTS_RESERVE_X = 100;

	private ChartPanel plot;

	/**
	 * @param chartPanel
	 */
	public MsLabelGenerator(LCMSPlotPanel chartPanel) {

		this.plot = chartPanel;
	}

	/**
	 * @see org.jfree.chart.labels.XYItemLabelGenerator#generateLabel(org.jfree.data.xy.XYDataset,
	 *      int, int)
	 */
	public String generateLabel(XYDataset dataset, int series, int item) {

		// X and Y values of current data point
		double originalX = dataset.getX(series, item).doubleValue();
		double originalY = dataset.getY(series, item).doubleValue();

		// Calculate data size of 1 screen pixel
		double xLength = (double) ((XYPlot) ((LCMSPlotPanel) plot).getPlot()).getDomainAxis().getRange().getLength();
		double pixelX = xLength / plot.getWidth();

		// Size of data set
		int itemCount = dataset.getItemCount(series);

		// Search for data points higher than this one in the interval
		// from limitLeft to limitRight
		double limitLeft = originalX - ((POINTS_RESERVE_X / 2) * pixelX);
		double limitRight = originalX + ((POINTS_RESERVE_X / 2) * pixelX);

		// Iterate data points to the left and right
		for (int i = 1; (item - i > 0) || (item + i < itemCount); i++) {

			// If we get out of the limit we can stop searching
			if ((item - i > 0) && (dataset.getXValue(series, item - i) < limitLeft)
					&& ((item + i >= itemCount) || (dataset.getXValue(series, item + i) > limitRight)))
				break;

			if ((item + i < itemCount) && (dataset.getXValue(series, item + i) > limitRight)
					&& ((item - i <= 0) || (dataset.getXValue(series, item - i) < limitLeft)))
				break;

			// If we find higher data point, bail out
			if ((item - i > 0) && (originalY <= dataset.getYValue(series, item - i)))
				return null;

			if ((item + i < itemCount) && (originalY <= dataset.getYValue(series, item + i)))
				return null;

		}

		// Create label
		String label = null;

		if (label == null) {

			double mzValue = dataset.getXValue(series, item);
			label = MRC2ToolBoxConfiguration.getMzFormat().format(mzValue);
		}
		return label;
	}
}

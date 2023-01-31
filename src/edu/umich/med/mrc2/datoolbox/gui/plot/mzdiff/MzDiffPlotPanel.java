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

package edu.umich.med.mrc2.datoolbox.gui.plot.mzdiff;

import java.awt.Color;
import java.util.Collection;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.ui.RectangleInsets;

import edu.umich.med.mrc2.datoolbox.data.DoubleValueBin;
import edu.umich.med.mrc2.datoolbox.gui.plot.MasterPlotPanel;
import edu.umich.med.mrc2.datoolbox.gui.plot.dataset.ScatterDataSet;

public class MzDiffPlotPanel extends MasterPlotPanel{

	/**
	 *
	 */
	private static final long serialVersionUID = 5995453754510129102L;
	protected NumberAxis xAxis, yAxis;

	public MzDiffPlotPanel() {
		super();

		legendVisible = false;
		initChart();
		initLegend(RectangleEdge.RIGHT, legendVisible);
		initPlot();
	}

	@Override
	protected void initAxes() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void initChart() {

		chart = ChartFactory.createXYLineChart(
				"",
		         "Mass difference",
		         "Frequency",
		         null,
		         PlotOrientation.VERTICAL,
		         true, true, false);

		chart.setBackgroundPaint(Color.white);
		setChart(chart);
	}

	@Override
	protected void initPlot() {

		plot = chart.getXYPlot();
		plot.setBackgroundPaint(Color.white);
		plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
		plot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
		plot.setDomainGridlinePaint(GRID_COLOR);
		plot.setRangeGridlinePaint(GRID_COLOR);
		plot.setDomainCrosshairVisible(false);
		plot.setRangeCrosshairVisible(false);
		plot.setDomainPannable(true);
		plot.setRangePannable(true);
	}

	@Override
	public void removeAllDataSets() {

		if(plot == null)
			return;
			
		int count = plot.getDatasetCount();
		for (int i = 0; i < count; i++)
			plot.setDataset(i, null);

		numberOfDataSets = 0;
	}

	public void showMzDifferenceDistribution(Collection<DoubleValueBin> massDifferenceBins) {

		MsDifferenceHistogramRenderer renderer = new MsDifferenceHistogramRenderer(Color.BLUE, 2f);
		plot.setRenderer(renderer);
		ScatterDataSet ds = new ScatterDataSet(massDifferenceBins);
		plot.setDataset(0, ds);;
	}
}


























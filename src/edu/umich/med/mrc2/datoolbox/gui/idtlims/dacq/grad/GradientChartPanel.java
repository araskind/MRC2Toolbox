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

package edu.umich.med.mrc2.datoolbox.gui.idtlims.dacq.grad;

import java.awt.Color;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.ui.RectangleInsets;

import edu.umich.med.mrc2.datoolbox.data.lims.ChromatographicGradient;
import edu.umich.med.mrc2.datoolbox.gui.plot.MasterPlotPanel;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;

public class GradientChartPanel extends MasterPlotPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2416827203428639664L;
	private NumberAxis xAxis, yAxis;
	private Color markerColor;

	public GradientChartPanel() {
		
		super();
		legendVisible = true;
		annotationsVisible = false;
		dataPointsVisible = false;
		markerColor = new Color(150, 150, 150);
		
		initChart();
		initPlot();
		initAxes();
		initTitles();
		initLegend(RectangleEdge.BOTTOM, legendVisible);
		
		setMouseWheelEnabled(true);
	}
	
	@Override
	protected void initAxes() {
		
		xAxis = (NumberAxis) plot.getDomainAxis();
		xAxis.setLabel("Elution time");
		xAxis.setNumberFormatOverride(MRC2ToolBoxConfiguration.getRtFormat());				

		xAxis.setUpperMargin(0.05);
		xAxis.setLowerMargin(0.001);
		xAxis.setTickLabelInsets(new RectangleInsets(0, 0, 20, 20));

		yAxis = (NumberAxis) plot.getRangeAxis();
		yAxis.setLabel("Mobile phase %");
		yAxis.setNumberFormatOverride(MRC2ToolBoxConfiguration.getPpmFormat());
		yAxis.setUpperMargin(0.1);
		yAxis.setLowerMargin(0.1);	
	}

	@Override
	protected void initChart() {

		chart = ChartFactory.createXYLineChart("", // title
				"", // x-axis label
				"", // y-axis label
				null, // data set
				PlotOrientation.VERTICAL, // orientation
				legendVisible, // create legend?
				true, // generate tooltips?
				false // generate URLs?
		);
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
	}

	@Override
	public synchronized void removeAllDataSets() {

		if(plot == null)
			return;

		for (int i = 0; i < plot.getDatasetCount(); i++)
			plot.setDataset(i, null);

		plot.clearAnnotations();
		numberOfDataSets = 0;		
	}
	
	public void showGradient(ChromatographicGradient gradient) {

		//	TODO
	}

}

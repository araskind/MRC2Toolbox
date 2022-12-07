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

package edu.umich.med.mrc2.datoolbox.gui.idworks.summary;

import java.awt.Color;
import java.util.Objects;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;

import edu.umich.med.mrc2.datoolbox.data.MsFeatureInfoBundleCollection;
import edu.umich.med.mrc2.datoolbox.gui.plot.MasterPlotPanel;

public class DataSetStatsPlotPanel extends MasterPlotPanel {

		
	/**
	 * 
	 */
	private static final long serialVersionUID = 3359294404327470333L;
	private MsFeatureInfoBundleCollection activeFeatureCollection;

	public DataSetStatsPlotPanel(MsFeatureInfoBundleCollection activeFeatureCollection) {
		super();
		this.activeFeatureCollection = activeFeatureCollection;
		initChart();
		createPlot(DataSetSummaryPlotType.PERCENT_IDENTIFIED_ANNOTATED);
	}

	@Override
	protected void initAxes() {
		// TODO Auto-generated method stub

	}
	
	private void createPlot(DataSetSummaryPlotType plotType) {
		
		removeAllDataSets();
		if(plotType.equals(DataSetSummaryPlotType.PERCENT_IDENTIFIED_ANNOTATED)) {
			
			PieDataset pds = createPieDataset(plotType);
			if(chart.getPlot() instanceof PiePlot)
				((PiePlot) chart.getPlot()).setDataset(pds);
		}		
	}
	
	@Override
	protected void initChart() {

//		chart = ChartFactory.createBarChart("", // title
//				"", // x-axis label - categories
//				"Value", // y-axis label
//				null, // data set
//				PlotOrientation.VERTICAL, // orientation
//				true, // create legend?
//				true, // generate tooltips?
//				false // generate URLs?
//		);	
		chart = ChartFactory.createPieChart(
				DataSetSummaryPlotType.PERCENT_IDENTIFIED_ANNOTATED.getName(), // chart title
				null, // data
				true, // include legend
				true, 
				false);
		chart.setBackgroundPaint(Color.white);
		chart.getPlot().setBackgroundPaint(Color.white);
		setChart(chart);
	}
	
	private PieDataset createPieDataset(DataSetSummaryPlotType plotType) {

		DefaultPieDataset dataset = new DefaultPieDataset();
		if (plotType.equals(DataSetSummaryPlotType.PERCENT_IDENTIFIED_ANNOTATED)) {

			long numIdentified = activeFeatureCollection.getFeatures().stream()
					.filter(f -> Objects.nonNull(f.getMsFeature().getPrimaryIdentity())).count();

			dataset.setValue("Identified", numIdentified);
			dataset.setValue("Unknowns", activeFeatureCollection.getFeatures().size() - numIdentified);
			return dataset;
		}
		return null;
	}

	@Override
	protected void initPlot() {
		
		if(chart.getPlot() instanceof XYPlot) {
			
			XYPlot xyPlot = (XYPlot)chart.getPlot();
			xyPlot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
			xyPlot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
			xyPlot.setDomainGridlinePaint(GRID_COLOR);
			xyPlot.setRangeGridlinePaint(GRID_COLOR);
			xyPlot.setDomainCrosshairVisible(false);
			xyPlot.setRangeCrosshairVisible(false);
			xyPlot.setDomainPannable(true);
			xyPlot.setRangePannable(true);
		}
		if(chart.getPlot() instanceof CategoryPlot) {
			
			CategoryPlot catPlot = (CategoryPlot)chart.getPlot();
			catPlot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
			catPlot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
			catPlot.setDomainGridlinePaint(GRID_COLOR);
			catPlot.setRangeGridlinePaint(GRID_COLOR);
			catPlot.setDomainCrosshairVisible(false);
			catPlot.setRangeCrosshairVisible(false);
			catPlot.setRangePannable(false);
		}
	}

	@Override
	public void removeAllDataSets() {

		Plot activePlot = chart.getPlot();	
		if(activePlot instanceof XYPlot) {
			
			for (int i = 0; i < ((XYPlot)activePlot).getDatasetCount(); i++)
				((XYPlot)chart.getPlot()).setDataset(i, null);						
		}
		if(activePlot instanceof CategoryPlot) {
			
			for (int i = 0; i < ((CategoryPlot)activePlot).getDatasetCount(); i++)
				((CategoryPlot)chart.getPlot()).setDataset(i, null);
		}		
		if(activePlot instanceof PiePlot)			
			((PiePlot)activePlot).setDataset(null);
	}
}

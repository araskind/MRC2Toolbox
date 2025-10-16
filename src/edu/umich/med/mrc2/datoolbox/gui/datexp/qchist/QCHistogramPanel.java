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

package edu.umich.med.mrc2.datoolbox.gui.datexp.qchist;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

import org.apache.commons.collections4.CollectionUtils;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.data.statistics.SimpleHistogramDataset;

import edu.umich.med.mrc2.datoolbox.data.MsFeatureSet;
import edu.umich.med.mrc2.datoolbox.data.enums.MSFeatureSetStatisticalParameters;
import edu.umich.med.mrc2.datoolbox.gui.plot.MasterPlotPanel;
import edu.umich.med.mrc2.datoolbox.utils.HistogramUtils;

public class QCHistogramPanel extends MasterPlotPanel {

	private MSFeatureSetStatisticalParameters qcParam;
	
	public QCHistogramPanel(MSFeatureSetStatisticalParameters qcParam) {
		super();
		this.qcParam = qcParam;
		initChart();
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected void initAxes() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void initChart() {
		
		chart = ChartFactory.createHistogram(
				qcParam.getName() + " distribution",
	            null, 
	            "Frequency", 
	            null,
	            PlotOrientation.VERTICAL, 
	            true, 
	            false,
	            false);
		chart.setBackgroundPaint(Color.white);
		setBasicPlotGui(chart.getPlot());
		setChart(chart);
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

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void removeAllDataSets() {

		Plot activePlot = chart.getPlot();			
		if(activePlot == null)
			return;
			
		if(activePlot instanceof XYPlot) {
			
			XYPlot p = (XYPlot)activePlot;
			int count = p.getDatasetCount();
			for (int i = 0; i < count; i++)
				p.setDataset(i, null);	
			
			p.clearAnnotations();
		}
		if(activePlot instanceof CategoryPlot) {
			
			CategoryPlot p = (CategoryPlot)activePlot;
			int count = p.getDatasetCount();
			for (int i = 0; i < count; i++)
				p.setDataset(i, null);
			
			p.clearAnnotations();
		}		
		if(activePlot instanceof PiePlot)			
			((PiePlot)activePlot).setDataset(null);

		numberOfDataSets = 0;
	}
	
	private void setBasicPlotGui(Plot newPlot) {
		
		newPlot.setBackgroundPaint(Color.white);
		
		if(newPlot instanceof XYPlot) {
			
			XYPlot xyPlot = (XYPlot)newPlot;
			
			xyPlot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
			xyPlot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
			xyPlot.setDomainGridlinePaint(GRID_COLOR);
			xyPlot.setRangeGridlinePaint(GRID_COLOR);
			xyPlot.setDomainCrosshairVisible(false);
			xyPlot.setRangeCrosshairVisible(false);
			xyPlot.setDomainPannable(true);
			xyPlot.setRangePannable(true);
		}
		if(newPlot instanceof CategoryPlot) {
			
			CategoryPlot catPlot = (CategoryPlot)newPlot;
			catPlot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
			catPlot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
			catPlot.setDomainGridlinePaint(GRID_COLOR);
			catPlot.setRangeGridlinePaint(GRID_COLOR);
			catPlot.setDomainCrosshairVisible(false);
			catPlot.setRangeCrosshairVisible(false);
			catPlot.setRangePannable(false);
		}
	}
	
	public void createQCvalueHistogram(
			MsFeatureSet featureSet,
			MSFeatureSetStatisticalParameters par) {

		removeAllDataSets();
		this.qcParam = par;
		chart.setTitle(qcParam.getName() + " distribution");
		XYPlot histogram = (XYPlot) chart.getPlot();
		histogram.getDomainAxis().setLabel(par.getName());
		
		XYBarRenderer renderer = (XYBarRenderer)histogram.getRenderer();
		renderer.setBarPainter(new StandardXYBarPainter());
		renderer.setDefaultPaint(Color.BLUE);	
		
		Collection<Double>data = new ArrayList<Double>();
		featureSet.getFeatures().stream().
		filter(f -> Objects.nonNull(f.getStatsSummary())).
		forEach(f -> CollectionUtils.addIgnoreNull(
				data, f.getStatsSummary().getValueOfType(par)));	
		data.removeIf(v -> Double.isNaN(v));
		double[] histData = data.stream().mapToDouble(d -> d).toArray();
		SimpleHistogramDataset dataSet = 
			HistogramUtils.calcHistogram(histData, par.getName(), false);
		histogram.setDataset(dataSet);		 
	}
}

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

package edu.umich.med.mrc2.datoolbox.gui.dereplication.vis.heatmap;

import java.awt.Color;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.SymbolAxis;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.renderer.LookupPaintScale;
import org.jfree.chart.renderer.xy.XYBlockRenderer;
import org.jfree.chart.title.PaintScaleLegend;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.ui.RectangleInsets;

import edu.umich.med.mrc2.datoolbox.gui.datexp.MZRTPlotParameterObject;
import edu.umich.med.mrc2.datoolbox.gui.plot.ColorGradient;
import edu.umich.med.mrc2.datoolbox.gui.plot.ColorScale;
import edu.umich.med.mrc2.datoolbox.gui.plot.HeatMapDataRange;
import edu.umich.med.mrc2.datoolbox.gui.plot.MasterPlotPanel;
import edu.umich.med.mrc2.datoolbox.gui.plot.dataset.CorrelationMapDataSet;
import edu.umich.med.mrc2.datoolbox.gui.utils.ColorCodingUtils;
import edu.umich.med.mrc2.datoolbox.utils.Range;

public class JFHeatChart extends MasterPlotPanel implements ChartMouseListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3935280341423856649L;

	private SymbolAxis xAxis;
	private SymbolAxis yAxis;
	private XYBlockRenderer renderer;
	private ColorScale colorScale;
	private ColorGradient colorGradient;
	private HeatMapDataRange heatMapDataRange;
	private int colorValueDistance;
	private Range dataRange;
	private HeatChartType chartType;
	private LookupPaintScale lookupPaintScale;

	private JFHeatChartTooltipGenerator toolTipGenerator;

	public JFHeatChart(HeatChartType chartType) {

		super();
		this.chartType = chartType;
		colorGradient = ColorGradient.GREEN_RED;
		colorScale = ColorScale.LINEAR;
		
		initChart();
		initPlot();
		setMouseWheelEnabled(true);
	}

	@Override
	protected void initChart() {
		// TODO Auto-generated method stub
		if(chartType.equals(HeatChartType.FeatureCorrelationMatrix)) {
			chart = ChartFactory.createScatterPlot("Correlation", "Features", "Features", null);
			chart.setBackgroundPaint(Color.white);
			setChart(chart);
			dataRange = new Range(-1.0d, 1.0d);
			heatMapDataRange = HeatMapDataRange.CORRELATION;
		}
		if(chartType.equals(HeatChartType.FeatureSetHeatmap)) {
			//	TODO
//			chart = ChartFactory.createScatterPlot("Correlation", "Features", "Features", null);
//			chart.setBackgroundPaint(Color.white);
//			setChart(chart);
		}
	}
	
	@Override
	protected void initPlot() {

		plot = chart.getXYPlot();
		plot.setBackgroundPaint(Color.white);
		plot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
		plot.setDomainGridlinesVisible(false);
		plot.setRangeGridlinesVisible(false);
		plot.setDomainCrosshairVisible(true);
		plot.setRangeCrosshairVisible(true);
		plot.setDomainPannable(true);
		plot.setRangePannable(true);

		renderer = new XYBlockRenderer();
		lookupPaintScale = ColorCodingUtils.createLookupPaintScale(
				dataRange, colorGradient, colorScale, 256);
		renderer.setPaintScale(lookupPaintScale);
		toolTipGenerator = new JFHeatChartTooltipGenerator();
		renderer.setDefaultToolTipGenerator(toolTipGenerator);

		plot.setRenderer(renderer);

		xAxis = new SymbolAxis("Features", new String[0]);
		yAxis = new SymbolAxis("Features", new String[0]);
		plot.setDomainAxis(xAxis);
		plot.setRangeAxis(yAxis);

		NumberAxis scaleAxis = new NumberAxis();
		scaleAxis.setRange(dataRange.getMin(), dataRange.getMax());
		PaintScaleLegend psl = new PaintScaleLegend(lookupPaintScale, scaleAxis);
		psl.setAxisOffset(5.0);
		psl.setPosition(RectangleEdge.RIGHT);
		psl.setMargin(new RectangleInsets(5, 5, 5, 5));
		chart.removeSubtitle(chart.getSubtitle(0));
		chart.addSubtitle(psl);
	}

	@Override
	protected void initAxes() {
		// TODO Auto-generated method stub
		
	}

	public synchronized void removeAllDataSets() {
		
		if(plot == null)
			return;
			
		int count = plot.getDatasetCount();
		for (int i = 0; i < count; i++)
			plot.setDataset(i, null);
		
		plot.clearAnnotations();
		numberOfDataSets = 0;
	}
	
	public void showFeatureCorrelationMatrix(
			CorrelationMapDataSet dataSet, 
			MZRTPlotParameterObject plotParams) {

		removeAllDataSets();

		xAxis = new SymbolAxis("Features", dataSet.getColumnLabels());
		xAxis.setVerticalTickLabels(true);
		plot.setDomainAxis(xAxis);
		yAxis = new SymbolAxis("Features", dataSet.getRowLabels());
		plot.setRangeAxis(yAxis);
		
		
		colorGradient = plotParams.getColorGradient();
		colorScale = plotParams.getColorScale();
		heatMapDataRange = plotParams.getHeatMapDataRange();
		
		if(heatMapDataRange.equals(HeatMapDataRange.CORRELATION)) {			
			dataRange = new Range(-1.0d, 1.0d);
		}
		else {
			dataRange = new Range(
					dataSet.getDataRange().getMin(),
					dataSet.getDataRange().getMax());
		}		
		lookupPaintScale = ColorCodingUtils.createLookupPaintScale(
				dataRange, colorGradient, colorScale, 256); 
		renderer.setPaintScale(lookupPaintScale);
		
		plot.setDataset(dataSet);
	}

	@Override
	public void chartMouseClicked(ChartMouseEvent event) {
		// TODO Auto-generated method stub

	}

	@Override
	public void chartMouseMoved(ChartMouseEvent event) {
		// TODO Auto-generated method stub

	}

//	private Color getCellColor(double data, double min, double max) {
//
//		return null;
//		double range = max - min;
//		double position = data - min;
//		double percentPosition = position / range;
//		int colourPosition = getColorPosition(percentPosition);
//
//		int r = lowValueColor.getRed();
//		int g = lowValueColor.getGreen();
//		int b = lowValueColor.getBlue();
//
//		for (int i = 0; i < colourPosition; i++) {
//			int rDistance = r - highValueColor.getRed();
//			int gDistance = g - highValueColor.getGreen();
//			int bDistance = b - highValueColor.getBlue();
//
//			if ((Math.abs(rDistance) >= Math.abs(gDistance)) && (Math.abs(rDistance) >= Math.abs(bDistance))) {
//				// Red must be the largest.
//				r = changeColorValue(r, rDistance);
//			} else if (Math.abs(gDistance) >= Math.abs(bDistance)) {
//				// Green must be the largest.
//				g = changeColorValue(g, gDistance);
//			} else {
//				// Blue must be the largest.
//				b = changeColorValue(b, bDistance);
//			}
//		}
//		return new Color(r, g, b);
//	}

	/*
	 * Returns how many colour shifts are required from the lowValueColour to
	 * get to the correct colour position. The result will be different
	 * depending on the colour scale used: LINEAR, LOGARITHMIC, EXPONENTIAL.
	 */
//	private int getColorPosition(double percentPosition) {
//		return (int) Math.round(colorValueDistance * Math.pow(percentPosition, colorScale.getValue()));
//	}

//	private void updateColorDistance() {
//		
//		int r1 = lowValueColor.getRed();
//		int g1 = lowValueColor.getGreen();
//		int b1 = lowValueColor.getBlue();
//		int r2 = highValueColor.getRed();
//		int g2 = highValueColor.getGreen();
//		int b2 = highValueColor.getBlue();
//
//		colorValueDistance = Math.abs(r1 - r2);
//		colorValueDistance += Math.abs(g1 - g2);
//		colorValueDistance += Math.abs(b1 - b2);
//	}

//	public void setColorPalette(ColorGradient gradient) {
//		
//		lowValueColor = gradient.getLowValueColor();
//		highValueColor = gradient.getHighValueColor();
//		updateColorDistance();		
//		CorrelationMapDataSet dataSet = (CorrelationMapDataSet) plot.getDataset();
//		initPlot();
//		showFeatureCorrelationMatrix(dataSet);
//	}
	
//	public void setColorScale(ColorScale scale) {		
//
//		colorScale = scale;	
//		CorrelationMapDataSet dataSet = (CorrelationMapDataSet) plot.getDataset();
//		initPlot();
//		showFeatureCorrelationMatrix(dataSet);
//	}
	


}

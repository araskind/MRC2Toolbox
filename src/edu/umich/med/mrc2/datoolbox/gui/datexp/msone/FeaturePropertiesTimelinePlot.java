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

package edu.umich.med.mrc2.datoolbox.gui.datexp.msone;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ItemEvent;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.TreeMap;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.AbstractRenderer;
import org.jfree.chart.ui.RectangleAnchor;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.ui.RectangleInsets;

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.SimpleMsFeature;
import edu.umich.med.mrc2.datoolbox.data.compare.ChartColorOption;
import edu.umich.med.mrc2.datoolbox.data.compare.DataFileComparator;
import edu.umich.med.mrc2.datoolbox.data.enums.FileSortingOrder;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.plot.AbstractControlledDataPlot;
import edu.umich.med.mrc2.datoolbox.gui.plot.IFeaturePropertiesPlot;
import edu.umich.med.mrc2.datoolbox.gui.plot.dataset.TimedScatterDataSet;
import edu.umich.med.mrc2.datoolbox.gui.plot.dataset.TimedScatterDataSetWithCustomErrors;
import edu.umich.med.mrc2.datoolbox.gui.plot.renderer.XYCustomErrorRenderer;
import edu.umich.med.mrc2.datoolbox.gui.plot.stats.DataPlotControlsPanel;
import edu.umich.med.mrc2.datoolbox.gui.plot.tooltip.ObjectMappedTimeSeriesToolTipGenerator;
import edu.umich.med.mrc2.datoolbox.gui.utils.ColorUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.JFreeChartUtils;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class FeaturePropertiesTimelinePlot extends AbstractControlledDataPlot implements IFeaturePropertiesPlot {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	protected MSQualityDataPlotParameterObject plotParameters;
	protected XYPlot dataPlot;
	protected LCMSPlotType plotType;
	private final static double plotSymbolScale = 0.5d;
	
	public FeaturePropertiesTimelinePlot(LCMSPlotType plotType) {
		super();
		this.plotType = plotType;
		initChart();
		initTitles();
		initLegend(RectangleEdge.BOTTOM, legendVisible);
	}

	@Override
	protected void initAxes() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void initChart() {
		
		if(plotType.equals(LCMSPlotType.RT_AND_PEAK_WIDTH))
			initRTwidthPlot();
		
		if(plotType.equals(LCMSPlotType.MZ))
			initMZplot();
		
		if(plotType.equals(LCMSPlotType.FEATURE_QUALITY))
			initFeatureQualityPlot();
		
		scalePlotSymbols();
		setBasicPlotGui(dataPlot);
		dataPlot.getRenderer().setDefaultToolTipGenerator(
				new ObjectMappedTimeSeriesToolTipGenerator());

		chart = new JFreeChart(dataPlot);
		chart.setBackgroundPaint(Color.white);
		setChart(chart);
	}
	
	private void initFeatureQualityPlot() {
		
		dataPlot = (XYPlot) ChartFactory.createScatterPlot(
				"", // title
				"", // x-axis label
				"Quality score", // y-axis label
				null, // data set
				PlotOrientation.VERTICAL, // orientation
				false, // create legend?
				true, // generate tooltips?
				false // generate URLs?
		).getPlot();
		((NumberAxis) dataPlot.getRangeAxis()).
			setNumberFormatOverride(MRC2ToolBoxConfiguration.getPpmFormat());
	}
	
	private void initMZplot() {
		
		dataPlot = (XYPlot) ChartFactory.createScatterPlot(
				"", // title
				"", // x-axis label
				"M/Z, Da", // y-axis label
				null, // data set
				PlotOrientation.VERTICAL, // orientation
				false, // create legend?
				true, // generate tooltips?
				false // generate URLs?
		).getPlot();
		((NumberAxis) dataPlot.getRangeAxis()).
			setNumberFormatOverride(MRC2ToolBoxConfiguration.getMzFormat());
	}
	
	private void initRTwidthPlot() {
		
		dataPlot = (XYPlot) ChartFactory.createScatterPlot(
				"", // title
				"", // x-axis label
				"RT, min", // y-axis label
				null, // data set
				PlotOrientation.VERTICAL, // orientation
				false, // create legend?
				true, // generate tooltips?
				false // generate URLs?
		).getPlot();
		((NumberAxis) dataPlot.getRangeAxis()).
			setNumberFormatOverride(MRC2ToolBoxConfiguration.getRtFormat());
		
		XYCustomErrorRenderer peakWidtRenderer = new XYCustomErrorRenderer();
		peakWidtRenderer.setDrawYError(true);
		peakWidtRenderer.setDrawXError(false);
		dataPlot.setRenderer(peakWidtRenderer);	
	}
	
	private void scalePlotSymbols() {
		
		AbstractRenderer plotRenderer = (AbstractRenderer) dataPlot.getRenderer();
		for(int i=0; i<20; i++) {
			plotRenderer.setSeriesPaint(i, ColorUtils.getColor(i));
			JFreeChartUtils.resizeSymbolForRenderer(plotRenderer, i, 0.3d);
		}	
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
	}

	@Override
	public void removeAllDataSets() {
		
		if(dataPlot == null)
			return;
			
		for (int i = 0; i < dataPlot.getDatasetCount(); i++)
			dataPlot.setDataset(i, null);

		dataPlot.clearAnnotations();
		dataPlot.clearRangeMarkers();
	}

	private void createFeatureSingleValueDataPlot(
			MsFeature feature, 
			TreeMap<DataFile, SimpleMsFeature> sortedFileFeatureMap,
			FileSortingOrder sortingOrder, 
			ChartColorOption colorOption, 
			DataAnalysisProject currentExperiment,
			DataPipeline dataPipeline,
			LCMSPlotType dataType) {

		if(sortingOrder.equals(FileSortingOrder.TIMESTAMP)) {
			
			TimedScatterDataSet tsds = 
					new TimedScatterDataSet(
							feature, 
							sortedFileFeatureMap, 
							colorOption, 
							currentExperiment, 
							dataPipeline,
							dataType);
			
//			AbstractRenderer mzRenderer = (AbstractRenderer) dataPlot.getRenderer();
//			for(int i=0; i<tsds.getSeriesCount(); i++) {
//				mzRenderer.setSeriesPaint(i, ColorUtils.getColor(i));
//				JFreeChartUtils.resizeSymbolForRenderer(mzRenderer, i, 0.5);
//			}		
			dataPlot.setDataset(tsds);
			
			if(dataType.equals(LCMSPlotType.FEATURE_QUALITY) 
					&& dataPlot.getDataRange(dataPlot.getRangeAxis()) != null) {
				double border  = dataPlot.getDataRange(dataPlot.getRangeAxis()).getUpperBound() * 1.15;
				if(border <= 0)
					border = 0.01;
				
				dataPlot.getRangeAxis().setRange(new org.jfree.data.Range(0.0d, border));
			}			
			addSDRangeMarkers(tsds, dataPlot);
		}	
	}

	//	Peak width plot
	private void createRtPeakWidthPlot(			
			MsFeature feature,
			Map<DataFile, SimpleMsFeature> sortedFileFeatureMap, 
			FileSortingOrder sortingOrder,
			ChartColorOption colorOption, 
			DataAnalysisProject currentExperiment, 
			DataPipeline dataPipeline) {

		if(sortingOrder.equals(FileSortingOrder.TIMESTAMP)) {
			
			TimedScatterDataSetWithCustomErrors tsds = 
					new TimedScatterDataSetWithCustomErrors(
							feature, sortedFileFeatureMap, currentExperiment, dataPipeline);
			
//			AbstractRenderer peakWidtRenderer = (AbstractRenderer)dataPlot.getRenderer();
//			for(int i=0; i<tsds.getSeriesCount(); i++) {
//				peakWidtRenderer.setSeriesPaint(i, ColorUtils.getColor(i));
//				JFreeChartUtils.resizeSymbolForRenderer(peakWidtRenderer, i, 0.5);
//			}			
			dataPlot.setDataset(tsds);
			addMedianToBottomRangeMarkers(tsds, dataPlot);
		}
	}

	private void addMedianToBottomRangeMarkers(TimedScatterDataSetWithCustomErrors tsds, XYPlot targetPlot) {
		
		if(tsds.getDataSetStats() == null)
			return;
			
		if(tsds.getDataSetStats().getValueStats() != null) {
			
			ValueMarker marker = new ValueMarker(tsds.getDataSetStats().getValueStats().getPercentile(50.0d)); 
			marker.setPaint(Color.red);
			targetPlot.addRangeMarker(marker);
		}
		if(tsds.getDataSetStats().getLowerBorderStats() != null) {
			
			ValueMarker lbmarker = new ValueMarker(tsds.getDataSetStats().getLowerBorderStats().getPercentile(50.0d)); 
			lbmarker.setPaint(Color.black);
			targetPlot.addRangeMarker(lbmarker);
		}
		if(tsds.getDataSetStats().getUpperBorderStats() != null) {
			
			ValueMarker ubmarker = new ValueMarker(tsds.getDataSetStats().getUpperBorderStats().getPercentile(50.0d)); 
			ubmarker.setPaint(Color.black);
			targetPlot.addRangeMarker(ubmarker);
		}		
	}
	
	private void addSDRangeMarkers(TimedScatterDataSet tsds, XYPlot targetPlot) {

		if(tsds.getDataSetStats() == null 
				|| tsds.getDataSetStats().getValueStats() == null)
			return;
		
		double median = tsds.getDataSetStats().getValueStats().getPercentile(50.0d);
		ValueMarker marker = new ValueMarker(median); 
		marker.setPaint(Color.red);
		targetPlot.addRangeMarker(marker);
		
		double min = tsds.getDataSetStats().getValueStats().getMin();
		double max = tsds.getDataSetStats().getValueStats().getMax();
		Font labelFont = new Font("Arial", Font.PLAIN, 18);
		
		ValueMarker upperMarker = new ValueMarker(max); 
		upperMarker.setPaint(ColorUtils.getBrewerColor(2));
		upperMarker.setLabelAnchor(RectangleAnchor.CENTER);
		upperMarker.setLabelFont(labelFont);
		
		ValueMarker lowerMarker = new ValueMarker(min); 
		lowerMarker.setPaint(ColorUtils.getBrewerColor(2));
		lowerMarker.setLabelAnchor(RectangleAnchor.CENTER);
		lowerMarker.setLabelFont(labelFont);
		
		if(plotType.equals(LCMSPlotType.MZ)) {
			
			double upperPpm = (max - median)/median * 1000000.0d;
			double lowerPpm = (median - min)/median * 1000000.0d;
			upperMarker.setLabel("+" + MRC2ToolBoxConfiguration.getPpmFormat().format(upperPpm) + " ppm");
			lowerMarker.setLabel("-" + MRC2ToolBoxConfiguration.getPpmFormat().format(lowerPpm) + " ppm");
		}
		if(plotType.equals(LCMSPlotType.FEATURE_QUALITY)) {
			
			upperMarker.setLabel(MRC2ToolBoxConfiguration.getPpmFormat().format(max));
			lowerMarker.setLabel(MRC2ToolBoxConfiguration.getPpmFormat().format(min));
		}
		targetPlot.addRangeMarker(upperMarker);
		targetPlot.addRangeMarker(lowerMarker);		
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateParametersFromControls() {
		// TODO Auto-generated method stub
		
		
	}

	@Override
	public void redrawPlot() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setDataPlotControlsPanel(DataPlotControlsPanel dataPlotControlsPanel) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void initPlot() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void showFeatureData(
			DataAnalysisProject currentExperiment,
			MSQualityDataPlotParameterObject plotParametersObject) {

		this.plotParameters = plotParametersObject;
		removeAllDataSets();
		
		if(currentExperiment == null || currentExperiment.getActiveDataPipeline() == null
				|| currentExperiment.getExperimentDesign() == null 
				|| currentExperiment.getExperimentDesign().getSamples().isEmpty()
				|| plotParameters.getFileFeatureMap().isEmpty())
			return;

		TreeMap<DataFile, SimpleMsFeature> sortedFileFeatureMap = 
				new TreeMap<DataFile, SimpleMsFeature>(
						new DataFileComparator(plotParameters.getSortingOrder()));
		sortedFileFeatureMap.putAll(plotParameters.getFileFeatureMap());
		
		if(plotParameters.getSortingOrder().equals(FileSortingOrder.TIMESTAMP)) {

			DateAxis dateAxis = new DateAxis("Timestamp");
			dateAxis.setDateFormatOverride(new SimpleDateFormat("MM/dd HH:mm"));
			
			if(dataPlot instanceof XYPlot)
				dataPlot.setDomainAxis(dateAxis);
		}
		if(plotType.equals(LCMSPlotType.RT_AND_PEAK_WIDTH)) {
			
			createRtPeakWidthPlot(
					plotParameters.getMsFeature(),
					sortedFileFeatureMap, 
					plotParameters.getSortingOrder(),
					plotParameters.getChartColorOption(), 
					currentExperiment, 
					currentExperiment.getActiveDataPipeline());
		}
		if(plotType.equals(LCMSPlotType.MZ) || plotType.equals(LCMSPlotType.FEATURE_QUALITY)) {
			
			createFeatureSingleValueDataPlot(
					plotParameters.getMsFeature(),
					sortedFileFeatureMap, 
					plotParameters.getSortingOrder(),
					plotParameters.getChartColorOption(), 
					currentExperiment, 
					currentExperiment.getActiveDataPipeline(),
					plotType);
		}
	}
}




















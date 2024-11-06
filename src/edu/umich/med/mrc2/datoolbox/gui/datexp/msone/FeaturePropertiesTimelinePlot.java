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
import java.awt.event.ItemEvent;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.TreeMap;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.ui.RectangleInsets;

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.SimpleMsFeature;
import edu.umich.med.mrc2.datoolbox.data.compare.ChartColorOption;
import edu.umich.med.mrc2.datoolbox.data.compare.DataFileComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.enums.FileSortingOrder;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.plot.AbstractControlledDataPlot;
import edu.umich.med.mrc2.datoolbox.gui.plot.TwoDimDataPlotParameterObject;
import edu.umich.med.mrc2.datoolbox.gui.plot.dataset.TimedScatterDataSetWithCustomErrors;
import edu.umich.med.mrc2.datoolbox.gui.plot.renderer.XYCustomErrorRenderer;
import edu.umich.med.mrc2.datoolbox.gui.plot.stats.DataPlotControlsPanel;
import edu.umich.med.mrc2.datoolbox.gui.utils.ColorUtils;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class FeaturePropertiesTimelinePlot extends AbstractControlledDataPlot {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	protected TwoDimDataPlotParameterObject plotParameters;
	protected CombinedDomainXYPlot dataPlot;
	protected XYPlot rtPlot;
	protected XYPlot mzPlot;
	
	public FeaturePropertiesTimelinePlot() {
		super();
		initChart();
		initPlot();
		initTitles();
		initLegend(RectangleEdge.BOTTOM, false);
	}

	@Override
	protected void initAxes() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void initChart() {

		chart = new JFreeChart(new CombinedDomainXYPlot());
		dataPlot = (CombinedDomainXYPlot) chart.getPlot();
		chart.setBackgroundPaint(Color.white);
		setChart(chart);
	}

	private void setBasicPlotGui(XYPlot newPlot) {

		if(newPlot instanceof XYPlot) {
			
			XYPlot xyPlot = (XYPlot)newPlot;
			newPlot.setBackgroundPaint(Color.white);
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
	protected void initPlot() {

		rtPlot = (XYPlot) ChartFactory.createScatterPlot(
				"", // title
				"", // x-axis label
				"RT, min", // y-axis label
				null, // data set
				PlotOrientation.VERTICAL, // orientation
				false, // create legend?
				true, // generate tooltips?
				false // generate URLs?
		).getPlot();
		setBasicPlotGui(rtPlot);
		XYCustomErrorRenderer peakWidtRenderer = new XYCustomErrorRenderer();
		peakWidtRenderer.setDrawYError(true);
		peakWidtRenderer.setDrawXError(false);
		rtPlot.setRenderer(peakWidtRenderer);		
		dataPlot.add(rtPlot);
		
		mzPlot = (XYPlot) ChartFactory.createScatterPlot(
				"", // title
				"", // x-axis label
				"M/Z, Da", // y-axis label
				null, // data set
				PlotOrientation.VERTICAL, // orientation
				false, // create legend?
				true, // generate tooltips?
				false // generate URLs?
		).getPlot();
		setBasicPlotGui(mzPlot);
		dataPlot.add(mzPlot);
	}

	@Override
	public void removeAllDataSets() {

		if(rtPlot != null) {
			for (int i = 0; i < rtPlot.getDatasetCount(); i++)
				rtPlot.setDataset(i, null);

			rtPlot.clearAnnotations();
			rtPlot.clearRangeMarkers();
		}
		if(mzPlot != null) {
			for (int i = 0; i < mzPlot.getDatasetCount(); i++)
				mzPlot.setDataset(i, null);

			mzPlot.clearAnnotations();
			mzPlot.clearRangeMarkers();
		}
	}
	
	public void showFeatureData(
			MsFeature feature,
			Map<DataFile, SimpleMsFeature> fileFeatureMap, 
			FileSortingOrder sortingOrder,
			ChartColorOption colorOption, 
			DataAnalysisProject currentExperiment, 
			DataPipeline dataPipeline) {
	
		removeAllDataSets();
		if(!sortingOrder.equals(FileSortingOrder.NAME) 
				&& !sortingOrder.equals(FileSortingOrder.TIMESTAMP))
			return;
		
		TreeMap<DataFile, SimpleMsFeature> sortedFileFeatureMap = null;
		if(sortingOrder.equals(FileSortingOrder.NAME)) {
			sortedFileFeatureMap = new TreeMap<DataFile, SimpleMsFeature>(
					new DataFileComparator(SortProperty.Name));
		}
		if(sortingOrder.equals(FileSortingOrder.TIMESTAMP)) {
			sortedFileFeatureMap = new TreeMap<DataFile, SimpleMsFeature>(
					new DataFileComparator(SortProperty.injectionTime));
			DateAxis dateAxis = new DateAxis("Timestamp");
			dateAxis.setDateFormatOverride(new SimpleDateFormat("MM/dd HH:mm"));
			dataPlot.setDomainAxis(dateAxis);
		}
		if(sortedFileFeatureMap == null)
			return;
		
		sortedFileFeatureMap.putAll(fileFeatureMap);
		if(sortingOrder.equals(FileSortingOrder.TIMESTAMP)) {
			
			//	Peak width plot
			TimedScatterDataSetWithCustomErrors tsds = 
					new TimedScatterDataSetWithCustomErrors(
							feature, sortedFileFeatureMap, currentExperiment, dataPipeline);
			
			XYItemRenderer peakWidtRenderer = rtPlot.getRenderer();
			for(int i=0; i<tsds.getSeriesCount(); i++)
				peakWidtRenderer.setSeriesPaint(i, ColorUtils.getColor(i));
			
			rtPlot.setDataset(tsds);

			addRangeMarkers(tsds, rtPlot);
		}
	}
	
	private void addRangeMarkers(TimedScatterDataSetWithCustomErrors tsds, XYPlot targetPlot) {
		
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
}




















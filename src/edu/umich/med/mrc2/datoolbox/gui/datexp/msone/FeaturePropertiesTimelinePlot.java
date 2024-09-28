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
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.TreeMap;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.ui.RectangleInsets;

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.SimpleMsFeature;
import edu.umich.med.mrc2.datoolbox.data.compare.ChartColorOption;
import edu.umich.med.mrc2.datoolbox.data.compare.DataFileComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.enums.FileSortingOrder;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.plot.MasterPlotPanel;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class FeaturePropertiesTimelinePlot extends MasterPlotPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
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
		}
		if(mzPlot != null) {
			for (int i = 0; i < mzPlot.getDatasetCount(); i++)
				mzPlot.setDataset(i, null);

			mzPlot.clearAnnotations();
		}
	}
	
	public void showFeatureData(
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
		sortedFileFeatureMap.putAll(fileFeatureMap);
		
//		if (plotParameters.getSortingOrder().equals(FileSortingOrder.TIMESTAMP)) {
//
//			DateAxis dateAxis = new DateAxis("Timestamp");
//			dateAxis.setDateFormatOverride(new SimpleDateFormat("MM/dd HH:mm"));
//			scatterPlot.setDomainAxis(dateAxis);
//			TimedScatterDataSet tscs = 
//					new TimedScatterDataSet(
//							plottedFeaturesMap, activeDesign, plotParameters.getDataScale());
//
//			if(tscs.getRangeBounds(true) == null)
//				return;
//
//			for(int i=0; i<tscs.getSeriesCount(); i++) {
//
//				scatterPlot.getRenderer().setSeriesPaint(i, getSeriesPaint(i));
//				scatterPlot.getRenderer().setSeriesShape(i, getSeriesShape(i));
//			}
//			scatterPlot.setDataset(tscs);
//			double upperBound = tscs.getRangeBounds(true).getUpperBound() + 
//					Math.abs(tscs.getRangeBounds(true).getUpperBound()) * 0.05;
//			double lowerBound = tscs.getRangeBounds(true).getLowerBound() - 
//					Math.abs(tscs.getRangeBounds(true).getLowerBound()) * 0.05;
//			scatterPlot.getRangeAxis().setRange(new Range(lowerBound, upperBound));
//			valuePlot.add(scatterPlot);
//			valuePlot.getRangeAxis().setRange(new Range(lowerBound, upperBound));
//		}
//		if (plotParameters.getSortingOrder().equals(FileSortingOrder.NAME)) {
//
//			ScatterDataSet scs = 
//					new ScatterDataSet(
//							plottedFeaturesMap, activeDesign, plotParameters.getDataScale());
//			for(int i=0; i<scs.getSeriesCount(); i++) {
//
//				scatterPlot.getRenderer().setSeriesPaint(i, getSeriesPaint(i));
//				scatterPlot.getRenderer().setSeriesShape(i, getSeriesShape(i));
//			}
//			scatterPlot.setDataset(scs);
//			double upperBound = scs.getRangeUpperBound(true) + Math.abs(scs.getRangeUpperBound(true)) * 0.05;
//			double lowerBound = scs.getRangeLowerBound(true) - Math.abs(scs.getRangeLowerBound(true)) * 0.05;
//			scatterPlot.getRangeAxis().setRange(new Range(lowerBound, upperBound));
//			valuePlot.add(scatterPlot);
//			valuePlot.getRangeAxis().setRange(new Range(lowerBound, upperBound));
//		}
	}
}




















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

package edu.umich.med.mrc2.datoolbox.gui.plot.stats;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.Axis;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.CombinedRangeCategoryPlot;
import org.jfree.chart.plot.CombinedRangeXYPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.data.Range;
import org.jfree.data.statistics.BoxAndWhiskerCategoryDataset;

import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignSubset;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.enums.FileSortingOrder;
import edu.umich.med.mrc2.datoolbox.data.enums.PlotDataGrouping;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.plot.MasterPlotPanel;
import edu.umich.med.mrc2.datoolbox.gui.plot.dataset.BoxAndWhiskerCategoryDatasetCa;
import edu.umich.med.mrc2.datoolbox.gui.plot.dataset.ScatterDataSet;
import edu.umich.med.mrc2.datoolbox.gui.plot.dataset.TimedScatterDataSet;
import edu.umich.med.mrc2.datoolbox.gui.plot.dataset.VariableCategorySizeBarChartDataSet;
import edu.umich.med.mrc2.datoolbox.gui.plot.qc.twod.TwoDimQCPlot;
import edu.umich.med.mrc2.datoolbox.gui.plot.renderer.category.VariableCategorySizeBarRenderer;
import edu.umich.med.mrc2.datoolbox.gui.plot.renderer.category.VariableCategorySizeCategoryAxis;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class MultiPanelDataPlot extends TwoDimQCPlot{

	/**
	 * 
	 */
	private static final long serialVersionUID = -7135007776817939808L;
	
	protected CombinedRangeCategoryPlot categoryPlot;
	protected CombinedRangeXYPlot valuePlot;
	protected CategoryAxis categoryAxis;
	protected NumberAxis yAxis;
	protected Axis xAxis;

	protected DataAnalysisProject experiment;
	protected ExperimentDesignSubset activeDesign;
	protected Map<DataPipeline, Collection<MsFeature>> plottedFeaturesMap;
	protected TwoDimFeatureDataPlotParameterObject plotParameters;

	public MultiPanelDataPlot() {

		super();
		plotType = StatsPlotType.BARCHART;
		initChart();
		initTitles();
		initLegend(RectangleEdge.BOTTOM, legendVisible);
		plottedFeaturesMap = 
				new TreeMap<DataPipeline, Collection<MsFeature>>();
	}

	@Override
	protected void initChart() {

		if (plotType.equals(StatsPlotType.BARCHART)
				|| plotType.equals(StatsPlotType.BOXPLOT_BY_FEATURE)
				|| plotType.equals(StatsPlotType.BOXPLOT_BY_GROUP)) {

			chart = new JFreeChart(new CombinedRangeCategoryPlot());
			categoryPlot = (CombinedRangeCategoryPlot) chart.getPlot();
			valuePlot = null;
		}
		else {
			chart = new JFreeChart(new CombinedRangeXYPlot());
			valuePlot = (CombinedRangeXYPlot) chart.getPlot();
			categoryPlot = null;
		}
		chart.setBackgroundPaint(Color.white);
		setChart(chart);
	}

	@Override
	protected void initAxes() {

		yAxis = new NumberAxis();
		yAxis.setNumberFormatOverride(MRC2ToolBoxConfiguration.getIntensityFormat());
		yAxis.setUpperMargin(0.1);
		yAxis.setLowerMargin(0.1);

		if(categoryPlot != null)
			categoryPlot.setRangeAxis(yAxis);

		if(valuePlot != null)
			valuePlot.setRangeAxis(yAxis);
	}

	@Override
	protected void initPlot() {
		//	TODO remove method as obsolete
	}

	private CategoryPlot getNewBarchart() {

		CategoryPlot newPlot = (CategoryPlot) ChartFactory.createBarChart(
			"", // title
			"", // x-axis label - categories
			"", // y-axis label
			null, // data set
			PlotOrientation.VERTICAL, // orientation
			false, // create legend?
			true, // generate tooltips?
			false // generate URLs?
		).getPlot();
		setBasicPlotGui(newPlot);
		return newPlot;
	}

	private CategoryPlot getNewBoxPlot() {

		BoxAndWhiskerCategoryDataset bwset = null;

		CategoryPlot newPlot = (CategoryPlot) ChartFactory.createBoxAndWhiskerChart(
			"", // title
			"", // y-axis label
			"", // x-axis label
			bwset , // data set
			false // create legend?
		).getPlot();

		setBasicPlotGui(newPlot);

		return newPlot;
	}

	private XYPlot getNewScatterPlot() {

		XYPlot newPlot = (XYPlot) ChartFactory.createScatterPlot(
				"", // title
				"", // x-axis label
				"Intensity", // y-axis label
				null, // data set
				PlotOrientation.VERTICAL, // orientation
				false, // create legend?
				true, // generate tooltips?
				false // generate URLs?
		).getPlot();

		setBasicPlotGui(newPlot);

		return newPlot;
	}

	private XYPlot getNewLinePlot() {

		XYPlot newPlot = (XYPlot) ChartFactory.createXYLineChart(
				"", // title
				"", // x-axis label
				"Intensity", // y-axis label
				null, // data set
				PlotOrientation.VERTICAL, // orientation
				false, // create legend?
				true, // generate tooltips?
				false // generate URLs?
		).getPlot();

		setBasicPlotGui(newPlot);

		return newPlot;
	}

	@Override
	public void actionPerformed(ActionEvent event) {

		String command = event.getActionCommand();

		if (command.equals(ChartPanel.ZOOM_IN_RANGE_COMMAND)) {

			if (categoryPlot != null) {

				categoryPlot.getRangeAxis().resizeRange(1 / ZOOM_FACTOR);
				categoryPlot.getRangeAxis().setLowerBound(0d);
			}
			if (plot != null) {

				plot.getRangeAxis().resizeRange(1 / ZOOM_FACTOR);
				plot.getRangeAxis().setLowerBound(0d);
			}
			return;
		}
		else if (command.equals(ChartPanel.ZOOM_OUT_RANGE_COMMAND)) {

			if (categoryPlot != null) {

				categoryPlot.getRangeAxis().resizeRange(ZOOM_FACTOR);
				categoryPlot.getRangeAxis().setLowerBound(0d);
			}
			if (plot != null) {

				plot.getRangeAxis().resizeRange(ZOOM_FACTOR);
				plot.getRangeAxis().setLowerBound(0d);
			}
			return;
		}
		else if (command.equals(ChartPanel.ZOOM_RESET_RANGE_COMMAND)) {

			if (categoryPlot != null)
				categoryPlot.getRangeAxis().setAutoRange(true);

			if (plot != null)
				plot.getRangeAxis().setAutoRange(true);
			
			return;
		}
		else if (command.equals(ChartPanel.ZOOM_RESET_BOTH_COMMAND)) {
			this.restoreAutoBounds();
			return;
		}
		else if (command.equals(MasterPlotPanel.TOGGLE_ANNOTATIONS_COMMAND)) {
			toggleAnnotations();
			return;
		}
		else if (command.equals(MasterPlotPanel.TOGGLE_DATA_POINTS_COMMAND)) {
			toggleDataPoints();
			return;
		}
		else
			super.actionPerformed(event);
	}

	public void clearPlotPanel(){

		clearPlotMatrix();
		plottedFeaturesMap.clear();		
		activeDesign = null;
		plotParameters = null;
	}

	public void redrawPlot() {
		
		if (plottedFeaturesMap != null 
				&& !plottedFeaturesMap.isEmpty())
			loadMultipleFeatureData(plottedFeaturesMap);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void clearPlotMatrix() {

		if(categoryPlot != null) {

			List subplots = new ArrayList(categoryPlot.getSubplots());
			for(Object spl : subplots)
				categoryPlot.remove((CategoryPlot) spl);
		}
		if(valuePlot != null) {

			List subplots = new ArrayList(valuePlot.getSubplots());
			for(Object spl : subplots)
				valuePlot.remove((XYPlot) spl);
		}
	}

	public void loadMultipleFeatureData(Map<DataPipeline, Collection<MsFeature>> pfm) {

		clearPlotMatrix();
		experiment = MRC2ToolBoxCore.getActiveMetabolomicsExperiment();
		if(experiment == null || experiment.getExperimentDesign() == null 
				|| pfm == null || pfm.isEmpty())
			return;
		
		activeDesign = experiment.getExperimentDesign().getActiveDesignSubset();
		if(activeDesign == null)
			return;
		
		//	Copy map to avoid side effects
		copyFeatureMap(pfm);
		updateParametersFromControls();

		// Create and display data set
		if (plotType.equals(StatsPlotType.BARCHART))
			createFeatureBarChart();

		if (plotType.equals(StatsPlotType.BOXPLOT_BY_FEATURE))
			createFeatureBoxPlot(StatsPlotType.BOXPLOT_BY_FEATURE);

		if (plotType.equals(StatsPlotType.BOXPLOT_BY_GROUP))
			createFeatureBoxPlot(StatsPlotType.BOXPLOT_BY_GROUP);

		if (plotType.equals(StatsPlotType.LINES))
			createFeatureLinePlot();

		if (plotType.equals(StatsPlotType.SCATTER))
			createFeatureScatterPlot();
	}
	
	private void copyFeatureMap(Map<DataPipeline, Collection<MsFeature>> pfm) {
		
		if(pfm.equals(plottedFeaturesMap))
			return;
		
		plottedFeaturesMap.clear();
		plottedFeaturesMap.putAll(pfm);
	}

	private void createFeatureLinePlot() {

		if(plotParameters.getGroupingType().equals(PlotDataGrouping.IGNORE_DESIGN)) {

			XYPlot linePlot = getNewLinePlot();
			if (plotParameters.getSortingOrder().equals(FileSortingOrder.TIMESTAMP)) {

				DateAxis dateAxis = new DateAxis("Timestamp");
				dateAxis.setDateFormatOverride(new SimpleDateFormat("MM/dd HH:mm"));
				linePlot.setDomainAxis(dateAxis);
				TimedScatterDataSet tscs = 
						new TimedScatterDataSet(
								plottedFeaturesMap, activeDesign, plotParameters.getDataScale());
				for(int i=0; i<tscs.getSeriesCount(); i++) {

					linePlot.getRenderer().setSeriesPaint(i, getSeriesPaint(i));
					linePlot.getRenderer().setSeriesShape(i, getSeriesShape(i));
				}
				linePlot.setDataset(tscs);
				double upperBound = tscs.getRangeBounds(true).getUpperBound() + 
						Math.abs(tscs.getRangeBounds(true).getUpperBound()) * 0.05;
				double lowerBound = tscs.getRangeBounds(true).getLowerBound() - 
						Math.abs(tscs.getRangeBounds(true).getLowerBound()) * 0.05;
				linePlot.getRangeAxis().setRange(new Range(lowerBound, upperBound));
				valuePlot.add(linePlot);
				valuePlot.getRangeAxis().setRange(new Range(lowerBound, upperBound));
			}
			if (plotParameters.getSortingOrder().equals(FileSortingOrder.NAME)) {

				ScatterDataSet scs = new ScatterDataSet(
						plottedFeaturesMap, activeDesign, plotParameters.getDataScale());
				for(int i=0; i<scs.getSeriesCount(); i++) {

					linePlot.getRenderer().setSeriesPaint(i, getSeriesPaint(i));
					linePlot.getRenderer().setSeriesShape(i, getSeriesShape(i));
				}
				linePlot.setDataset(scs);
				double upperBound = scs.getRangeUpperBound(true) + Math.abs(scs.getRangeUpperBound(true)) * 0.05;
				double lowerBound = scs.getRangeLowerBound(true) - Math.abs(scs.getRangeLowerBound(true)) * 0.05;
				linePlot.getRangeAxis().setRange(new Range(lowerBound, upperBound));
				valuePlot.add(linePlot);
				valuePlot.getRangeAxis().setRange(new Range(lowerBound, upperBound));
			}
		}
		else {
			//	TODO ?
/*			for(MsFeature msf : plottedFeatures) {

				XYPlot scatterPlot = getNewScatterPlot();

				valuePlot.add(scatterPlot);
			}*/
		}
	}

	private void createFeatureScatterPlot() {

		if(plotParameters.getGroupingType().equals(PlotDataGrouping.IGNORE_DESIGN)) {

			XYPlot scatterPlot = getNewScatterPlot();

			if (plotParameters.getSortingOrder().equals(FileSortingOrder.TIMESTAMP)) {

				DateAxis dateAxis = new DateAxis("Timestamp");
				dateAxis.setDateFormatOverride(new SimpleDateFormat("MM/dd HH:mm"));
				scatterPlot.setDomainAxis(dateAxis);
				TimedScatterDataSet tscs = 
						new TimedScatterDataSet(
								plottedFeaturesMap, activeDesign, plotParameters.getDataScale());

				if(tscs.getRangeBounds(true) == null)
					return;

				for(int i=0; i<tscs.getSeriesCount(); i++) {

					scatterPlot.getRenderer().setSeriesPaint(i, getSeriesPaint(i));
					scatterPlot.getRenderer().setSeriesShape(i, getSeriesShape(i));
				}
				scatterPlot.setDataset(tscs);
				double upperBound = tscs.getRangeBounds(true).getUpperBound() + 
						Math.abs(tscs.getRangeBounds(true).getUpperBound()) * 0.05;
				double lowerBound = tscs.getRangeBounds(true).getLowerBound() - 
						Math.abs(tscs.getRangeBounds(true).getLowerBound()) * 0.05;
				scatterPlot.getRangeAxis().setRange(new Range(lowerBound, upperBound));
				valuePlot.add(scatterPlot);
				valuePlot.getRangeAxis().setRange(new Range(lowerBound, upperBound));
			}
			if (plotParameters.getSortingOrder().equals(FileSortingOrder.NAME)) {

				ScatterDataSet scs = 
						new ScatterDataSet(
								plottedFeaturesMap, activeDesign, plotParameters.getDataScale());
				for(int i=0; i<scs.getSeriesCount(); i++) {

					scatterPlot.getRenderer().setSeriesPaint(i, getSeriesPaint(i));
					scatterPlot.getRenderer().setSeriesShape(i, getSeriesShape(i));
				}
				scatterPlot.setDataset(scs);
				double upperBound = scs.getRangeUpperBound(true) + Math.abs(scs.getRangeUpperBound(true)) * 0.05;
				double lowerBound = scs.getRangeLowerBound(true) - Math.abs(scs.getRangeLowerBound(true)) * 0.05;
				scatterPlot.getRangeAxis().setRange(new Range(lowerBound, upperBound));
				valuePlot.add(scatterPlot);
				valuePlot.getRangeAxis().setRange(new Range(lowerBound, upperBound));
			}
		}
		else {
			//TODO ??
/*			for(MsFeature msf : plottedFeatures) {

				XYPlot scatterPlot = getNewScatterPlot();

				valuePlot.add(scatterPlot);
			}*/
		}
	}

	private void createFeatureBoxPlot(StatsPlotType boxplotType) {

		//for(MsFeature msf : plottedFeatures) {}


			CategoryPlot boxPlot = getNewBoxPlot();
			
			BoxAndWhiskerCategoryDatasetCa ds = 
					new BoxAndWhiskerCategoryDatasetCa(plotParameters,experiment,activeDesign);
			
//			BoxAndWhiskerCategoryDatasetCa ds = new BoxAndWhiskerCategoryDatasetCa(
//					plottedFeaturesMap,
//					boxplotType,
//					plotParameters.getSortingOrder(),
//					plotParameters.getDataScale(),
//					activeDesign,
//					plotParameters.getGroupingType(),
//					plotParameters.getCategory(),
//					plotParameters.getSubCategory());

			boxPlot.setDataset(ds);

			//	BoxAndWhiskerRenderer renderer
//			boxPlot.getDomainAxis().setLabel(msf.getName());
//			boxPlot.getDomainAxis().setCategoryLabelPositions(getCategoryLabelPosition());
			boxPlot.getDomainAxis().setMaximumCategoryLabelLines(calculateLineNumberForCategoryLabels());
			categoryPlot.add(boxPlot);
	}

	private void createFeatureBarChart() {
		
		for (Entry<DataPipeline, Collection<MsFeature>> entry : plottedFeaturesMap.entrySet()) {
			
			for(MsFeature msf : entry.getValue()) {
				
				CategoryPlot barChart = getNewBarchart();

				//	MsFeatureBarChartDataSet ds = new MsFeatureBarChartDataSet(msf, plotParameters);
				VariableCategorySizeBarChartDataSet ds = 
						new VariableCategorySizeBarChartDataSet(msf, plotParameters);
				VariableCategorySizeBarRenderer renderer = new VariableCategorySizeBarRenderer();
		        renderer.setDefaultToolTipGenerator(
		        		new StatsPlotDataFileToolTipGenerator(NumberFormat.getNumberInstance(), null));
				for(int i=0; i<ds.getRowCount(); i++)
					renderer.setSeriesPaint(i, ds.getSeriesPaintMap().get(i));

				barChart.setDomainAxis(new VariableCategorySizeCategoryAxis());
				barChart.setRenderer(renderer);				
				barChart.setDataset(ds);
				
				CategoryAxis axis = barChart.getDomainAxis();
				axis.setLabel(msf.getName());
				axis.setCategoryLabelPositions(CategoryLabelPositions.STANDARD);
				axis.setMaximumCategoryLabelLines(calculateLineNumberForCategoryLabels());
				axis.setLowerMargin(0.1);
				axis.setUpperMargin(0.1);
				axis.setCategoryMargin(0.1);
				
				if(plotParameters.getGroupingType().equals(PlotDataGrouping.IGNORE_DESIGN))
					axis.setTickLabelsVisible(false);
				
				categoryPlot.add(barChart);
			}
		}
	}

	private CategoryLabelPositions getCategoryLabelPosition() {

		CategoryLabelPositions pos = CategoryLabelPositions.STANDARD;

		if(plotParameters.getGroupingType().equals(PlotDataGrouping.IGNORE_DESIGN))
			pos = CategoryLabelPositions.UP_45;

		return pos;
	}

	private int calculateLineNumberForCategoryLabels() {

		if(plotParameters.getGroupingType().equals(PlotDataGrouping.IGNORE_DESIGN) 
				|| plotParameters.getGroupingType().equals(PlotDataGrouping.TWO_FACTORS)) {
				return 2;
		}
		else if(plotParameters.getGroupingType().equals(PlotDataGrouping.ONE_FACTOR)) {
				return 1;
		}
		else if(plotParameters.getGroupingType().equals(PlotDataGrouping.EACH_FACTOR)) {
			return activeDesign.getOrderedDesign().size();
		}
		else
			return 1;
	}

	@SuppressWarnings("unchecked")
	public void toggleAnnotations() {

		annotationsVisible = !annotationsVisible;

		if (categoryPlot != null) {

			List<CategoryPlot> subplots = new ArrayList<CategoryPlot>(categoryPlot.getSubplots());

			for(CategoryPlot subPlot : subplots) {

				for (int i = 0; i < subPlot.getDatasetCount(); i++)
					subPlot.getRenderer(i).setDefaultItemLabelsVisible(annotationsVisible);
			}
		}
		if (valuePlot != null) {

			List<XYPlot> subplots = new ArrayList<XYPlot>(valuePlot.getSubplots());

			for(XYPlot subPlot : subplots) {

				for (int i = 0; i < subPlot.getDatasetCount(); i++)
					subPlot.getRenderer(i).setDefaultItemLabelsVisible(annotationsVisible);
			}
		}
	}

	@SuppressWarnings({ "unchecked" })
	public void toggleDataPoints() {

		dataPointsVisible = !dataPointsVisible;

		if (categoryPlot != null) {
			// Nothing to change
		}
		if (valuePlot != null) {

			List<XYPlot> subplots = new ArrayList<XYPlot>(valuePlot.getSubplots());

			for(XYPlot subPlot : subplots) {

				for (int i = 0; i < subPlot.getDatasetCount(); i++)
					 ((XYLineAndShapeRenderer)subPlot.getRenderer(i)).setDefaultShapesVisible(dataPointsVisible);
			}
		}
	}

	@Override
	public void removeAllDataSets() {
		clearPlotMatrix();
	}
	
//	public void updatePlotType(StatsPlotType newPlotType) {
//		
//		if(chart == null || !newPlotType.equals(plotType)) {
//			
//			this.plotType = newPlotType;
//			initChart();
//			initTitles();
//			initAxes();
//			initLegend(RectangleEdge.RIGHT, legendVisible);
//		}
//		dataPlotControlsPanel.updatePlotGroupingOptions(plotType);
//	}

	@Override
	public void updateParametersFromControls() {
		
		if(chart == null || !toolbar.getStatsPlotType().equals(plotType)) {
			
			plotType = toolbar.getStatsPlotType();
			initChart();
			initTitles();
			initAxes();
			initLegend(RectangleEdge.BOTTOM, legendVisible);
			dataPlotControlsPanel.updatePlotGroupingOptions(plotType);
			dataPlotControlsPanel.updatePlotGroupingOptions(plotType);
		}				
		plotParameters = 
				new TwoDimFeatureDataPlotParameterObject(
					plottedFeaturesMap,
					toolbar.getSortingOrder(), 
					toolbar.getDataScale(),
					toolbar.getChartColorOption(),
					dataPlotControlsPanel.getDataGroupingType(), 
					dataPlotControlsPanel.getCategory(), 
					dataPlotControlsPanel.getSububCategory(),
					plotType);
	}
}

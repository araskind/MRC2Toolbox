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
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;

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
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.data.Range;
import org.jfree.data.statistics.BoxAndWhiskerCategoryDataset;

import edu.umich.med.mrc2.datoolbox.data.DataFileStatisticalSummary;
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignFactor;
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignSubset;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.compare.MsFeatureComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.enums.DataScale;
import edu.umich.med.mrc2.datoolbox.data.enums.DataSetQcField;
import edu.umich.med.mrc2.datoolbox.data.enums.FileSortingOrder;
import edu.umich.med.mrc2.datoolbox.data.enums.PlotDataGrouping;
import edu.umich.med.mrc2.datoolbox.data.enums.PlotDataType;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.plot.MasterPlotPanel;
import edu.umich.med.mrc2.datoolbox.gui.plot.dataset.BarChartDataSet;
import edu.umich.med.mrc2.datoolbox.gui.plot.dataset.BoxAndWhiskerCategoryDatasetCa;
import edu.umich.med.mrc2.datoolbox.gui.plot.dataset.ScatterDataSet;
import edu.umich.med.mrc2.datoolbox.gui.plot.dataset.TimedScatterDataSet;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;

public class MultiPanelDataPlot extends MasterPlotPanel implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7135007776817939808L;
	protected CombinedRangeCategoryPlot categoryPlot;
	protected CombinedRangeXYPlot valuePlot;

	protected StatsPlotType plotType;
	protected FileSortingOrder sortingOrder;
	protected DataScale dataScale;
	protected CategoryAxis categoryAxis;
	protected NumberAxis yAxis;
	protected Axis xAxis;
	protected PlotDataType plotDataType;
	protected PlotDataGrouping groupingType;
	protected ExperimentDesignFactor category;
	protected ExperimentDesignFactor subCategory;
	protected ExperimentDesignSubset activeDesign;

	protected Map<DataPipeline, Collection<MsFeature>> plottedFeaturesMap;
	protected Collection<DataFileStatisticalSummary> dataSetStats;
	protected DataSetQcField statsField;

	protected MultiPanelDataPlotToolbar toolbar;
	private boolean splitByBatch;

	public MultiPanelDataPlot(StatsPlotType type, PlotDataType dataType) {

		super();

		plotType = type;
		plotDataType = dataType;

		sortingOrder = FileSortingOrder.NAME;
		dataScale = DataScale.RAW;
		groupingType = PlotDataGrouping.IGNORE_DESIGN;

		initChart();
		initTitles();
		initLegend(RectangleEdge.RIGHT, legendVisible);
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
		}
		else if (command.equals(ChartPanel.ZOOM_RESET_RANGE_COMMAND)) {

			if (categoryPlot != null)
				categoryPlot.getRangeAxis().setAutoRange(true);

			if (plot != null)
				plot.getRangeAxis().setAutoRange(true);
		}
		else if (command.equals(ChartPanel.ZOOM_RESET_BOTH_COMMAND))
			this.restoreAutoBounds();

		else if (command.equals(MasterPlotPanel.TOGGLE_ANNOTATIONS_COMMAND))
			toggleAnnotations();

		else if (command.equals(MasterPlotPanel.TOGGLE_DATA_POINTS_COMMAND))
			toggleDataPoints();
		else
			super.actionPerformed(event);
	}

	public void updateParametersFromToolbar() {

		plotType = toolbar.getPlotType();
		sortingOrder = toolbar.getFileOrder();
		dataScale = toolbar.getPlotScale();
		groupingType = toolbar.getDataGroupingType();
		category = toolbar.getCategory();
		subCategory = toolbar.getSubCategory();
		splitByBatch = toolbar.splitByBatch();

		initChart();
		initTitles();
		initAxes();
		initLegend(RectangleEdge.RIGHT, legendVisible);
	}

	public void clearPlotPanel(){

		clearPlotMatrix();

		if (plottedFeaturesMap != null)
			plottedFeaturesMap.clear();

		if (dataSetStats != null)
			dataSetStats.clear();

		plotType = toolbar.getPlotType();
		groupingType = toolbar.getDataGroupingType();
		sortingOrder = toolbar.getFileOrder();
		category = toolbar.getCategory();
		subCategory = toolbar.getSubCategory();
		activeDesign = null;
	}

	public void redrawPlot() {

		if (plotDataType.equals(PlotDataType.FEATURE_DATA)) {

			if (plottedFeaturesMap != null)
				loadMultipleFeatureData(plottedFeaturesMap);
		}
		if (plotDataType.equals(PlotDataType.DATA_SET_STATS)) {

			if (dataSetStats != null)
				loadDataSetStats(dataSetStats);
		}
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

	public void loadDataSetStats(Collection<DataFileStatisticalSummary> dataSetStats2) {
		// TODO Auto-generated method stub

	}

	public void loadMultipleFeatureData(
			Map<DataPipeline, Collection<MsFeature>> selectedFeaturesMap,
			PlotDataGrouping dataGroupingType,
			ExperimentDesignFactor category2,
			ExperimentDesignFactor subCategory2) {

		groupingType = dataGroupingType;
		category = category2;
		subCategory = subCategory2;

		loadMultipleFeatureData(selectedFeaturesMap);
	}

	public void loadMultipleFeatureData(Map<DataPipeline, Collection<MsFeature>> pfm) {

		clearPlotMatrix();
		if(MRC2ToolBoxCore.getCurrentProject() == null)
			return;
		
		if(pfm.isEmpty())
			return;
		
		//	Copy map to avoid side effects
		copyFeatureMap(pfm);
		activeDesign = MRC2ToolBoxCore.getCurrentProject().
				getExperimentDesign().getActiveDesignSubset();

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
		
		plottedFeaturesMap = new TreeMap<DataPipeline, Collection<MsFeature>>();
		for (Entry<DataPipeline, Collection<MsFeature>> entry : pfm.entrySet()) {
			plottedFeaturesMap.put(entry.getKey(), 
					new TreeSet<MsFeature>(new MsFeatureComparator(SortProperty.MZ)));
			plottedFeaturesMap.get(entry.getKey()).addAll(entry.getValue());
		}
	}

	private void createFeatureLinePlot() {

		if(groupingType.equals(PlotDataGrouping.IGNORE_DESIGN)) {

			XYPlot linePlot = getNewLinePlot();
			if (sortingOrder.equals(FileSortingOrder.TIMESTAMP)) {

				DateAxis dateAxis = new DateAxis("Timestamp");
				dateAxis.setDateFormatOverride(new SimpleDateFormat("MM/dd HH:mm"));
				linePlot.setDomainAxis(dateAxis);
				TimedScatterDataSet tscs = 
						new TimedScatterDataSet(plottedFeaturesMap, activeDesign, dataScale);
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
			if (sortingOrder.equals(FileSortingOrder.NAME)) {

				ScatterDataSet scs = new ScatterDataSet(plottedFeaturesMap, activeDesign, dataScale);
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

		if(groupingType.equals(PlotDataGrouping.IGNORE_DESIGN)) {

			XYPlot scatterPlot = getNewScatterPlot();

			if (sortingOrder.equals(FileSortingOrder.TIMESTAMP)) {

				DateAxis dateAxis = new DateAxis("Timestamp");
				dateAxis.setDateFormatOverride(new SimpleDateFormat("MM/dd HH:mm"));
				scatterPlot.setDomainAxis(dateAxis);
				TimedScatterDataSet tscs = 
						new TimedScatterDataSet(plottedFeaturesMap, activeDesign, dataScale);

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
			if (sortingOrder.equals(FileSortingOrder.NAME)) {

				ScatterDataSet scs = 
						new ScatterDataSet(plottedFeaturesMap, activeDesign, dataScale);
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
			BoxAndWhiskerCategoryDatasetCa ds = new BoxAndWhiskerCategoryDatasetCa(
					plottedFeaturesMap,
					boxplotType,
					sortingOrder,
					dataScale,
					activeDesign,
					groupingType,
					category,
					subCategory,
					splitByBatch);

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
				((BarRenderer)barChart.getRenderer()).setBarPainter(new StandardBarPainter());

				BarChartDataSet ds = new BarChartDataSet(
						msf,
						entry.getKey(),
						sortingOrder,
						dataScale,
						activeDesign,
						groupingType,
						category,
						subCategory,
						splitByBatch);

				for(int i=0; i<ds.getRowCount(); i++)
					((BarRenderer)barChart.getRenderer()).setSeriesPaint(i, ds.getSeriesPaintMap().get(i));

				barChart.setDataset(ds);

				//	TODO add slider to plot to allow adjustment
				if(ds.getRowCount() == ds.getColumnCount())
					((BarRenderer)barChart.getRenderer()).setItemMargin(0.02);
				
				CategoryAxis axis = barChart.getDomainAxis();
				axis.setLabel(msf.getName());
				axis.setCategoryLabelPositions(getCategoryLabelPosition());
				axis.setMaximumCategoryLabelLines(calculateLineNumberForCategoryLabels());

				axis.setLowerMargin(0.1);
				axis.setUpperMargin(0.1);
				axis.setCategoryMargin(0.1);
				categoryPlot.add(barChart);
			}
		}
	}

	private CategoryLabelPositions getCategoryLabelPosition() {

		CategoryLabelPositions pos = CategoryLabelPositions.STANDARD;

		if(groupingType.equals(PlotDataGrouping.IGNORE_DESIGN))
			pos = CategoryLabelPositions.UP_45;

		return pos;
	}

	private int calculateLineNumberForCategoryLabels() {

		if(groupingType.equals(PlotDataGrouping.IGNORE_DESIGN) || groupingType.equals(PlotDataGrouping.TWO_FACTORS)) {

			if(splitByBatch)
				return 3;
			else
				return 2;
		}
		if(groupingType.equals(PlotDataGrouping.ONE_FACTOR)) {

			if(splitByBatch)
				return 2;
			else
				return 1;
		}
		if(groupingType.equals(PlotDataGrouping.EACH_FACTOR)) {

			if(splitByBatch)
				return activeDesign.getOrderedDesign().size() + 1;
			else
				return activeDesign.getOrderedDesign().size();
		}
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
		toolbar.toggleAnnotationsIcon(annotationsVisible);
	}

	public void setToolbar(MultiPanelDataPlotToolbar toolbar) {

		this.toolbar = toolbar;
	}

	@SuppressWarnings("unused")
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
		toolbar.toggleDataPointsIcon(dataPointsVisible);
	}

	public void setDataScale(DataScale dataScale) {
		this.dataScale = dataScale;
	}

	public void setPlotType(StatsPlotType plotType) {
		this.plotType = plotType;
	}

	public void setSortingOrder(FileSortingOrder sortingOrder) {
		this.sortingOrder = sortingOrder;
	}

	@Override
	public void removeAllDataSets() {

		clearPlotMatrix();
	}

	public FileSortingOrder getSortingOrder() {
		return sortingOrder;
	}

	public PlotDataGrouping getGroupingType() {
		return groupingType;
	}

	public StatsPlotType getPlotType() {
		return plotType;
	}

	public DataScale getDataScale() {
		return dataScale;
	}

	@Override
	public void toggleLegend() {

		if (legendVisible) {

			chart.removeLegend();
			legendVisible = false;
		} else {
			chart.addLegend(legend);
			legendVisible = true;
		}
		toolbar.toggleLegendIcon(legendVisible);
	}


}

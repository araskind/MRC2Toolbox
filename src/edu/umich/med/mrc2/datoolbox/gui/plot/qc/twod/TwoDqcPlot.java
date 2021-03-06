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

package edu.umich.med.mrc2.datoolbox.gui.plot.qc.twod;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collection;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.BoxAndWhiskerRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.data.statistics.BoxAndWhiskerCategoryDataset;

import edu.umich.med.mrc2.datoolbox.data.DataFileStatisticalSummary;
import edu.umich.med.mrc2.datoolbox.data.enums.DataScale;
import edu.umich.med.mrc2.datoolbox.data.enums.DataSetQcField;
import edu.umich.med.mrc2.datoolbox.data.enums.FileSortingOrder;
import edu.umich.med.mrc2.datoolbox.gui.plot.MasterPlotPanel;
import edu.umich.med.mrc2.datoolbox.gui.plot.dataset.QcBarChartDataSet;
import edu.umich.med.mrc2.datoolbox.gui.plot.dataset.QcBoxPlotDataSet;
import edu.umich.med.mrc2.datoolbox.gui.plot.dataset.QcScatterDataSet;
import edu.umich.med.mrc2.datoolbox.gui.plot.dataset.QcTimedScatterSet;
import edu.umich.med.mrc2.datoolbox.gui.plot.stats.QcPlotType;
import edu.umich.med.mrc2.datoolbox.gui.plot.tooltip.FileStatsBoxAndWhiskerToolTipGenerator;

public class TwoDqcPlot extends MasterPlotPanel implements ActionListener, ItemListener {
	
	private QcPlotType plotType;
	private FileSortingOrder sortingOrder;
	private DataScale dataScale;
	private DataSetQcField qcParameter;
	private TwoDqcPlotToolbar toolbar;
	private Collection<DataFileStatisticalSummary> dataSetStats;
	private Plot activePlot;

	public TwoDqcPlot() {

		super();
		
		plotType = QcPlotType.BARCHART;
		sortingOrder = FileSortingOrder.NAME;		
		dataScale = DataScale.RAW;
		qcParameter = DataSetQcField.OBSERVATIONS;		
	}
	
	/**
	 * @param toolbar the toolbar to set
	 */
	public void setToolbar(TwoDqcPlotToolbar toolbar) {
		
		this.toolbar = toolbar;
	}
	
	public void loadDataSetStats(Collection<DataFileStatisticalSummary> dataSetStats2) {
		
		dataSetStats = dataSetStats2;		
		
		if (plotType.equals(QcPlotType.BARCHART))
			chart.getCategoryPlot().setDataset(new QcBarChartDataSet(dataSetStats, sortingOrder, qcParameter));
		
		if ((plotType.equals(QcPlotType.LINES) || plotType.equals(QcPlotType.SCATTER)) && sortingOrder.equals(FileSortingOrder.NAME))
			chart.getXYPlot().setDataset(new QcScatterDataSet(dataSetStats, qcParameter));
		
		if ((plotType.equals(QcPlotType.LINES) || plotType.equals(QcPlotType.SCATTER)) && sortingOrder.equals(FileSortingOrder.TIMESTAMP))
			chart.getXYPlot().setDataset(new QcTimedScatterSet(dataSetStats, qcParameter));
		
		if (plotType.equals(QcPlotType.BOXPLOT))
			chart.getCategoryPlot().setDataset(new QcBoxPlotDataSet(dataSetStats, sortingOrder));
	}
	
	public void redrawPlot() {

		removeAllDataSets();
		
		if (dataSetStats != null)
			loadDataSetStats(dataSetStats);		
	}	
	
	public void updateParametersFromToolbar() {

		plotType = toolbar.getQcPlotType();
		sortingOrder = toolbar.getSortingOrder();
		dataScale = toolbar.getDataScale();
		qcParameter = toolbar.getStatParameter();
		
		initChart();
		initTitles();
		initAxes();
		initLegend(RectangleEdge.RIGHT, legendVisible);
	}
	
	@Override
	public void actionPerformed(ActionEvent event) {

		String command = event.getActionCommand();
		
		
		if (command.equals(ChartPanel.ZOOM_IN_RANGE_COMMAND)) {

			if (activePlot instanceof CategoryPlot) {

				((CategoryPlot)activePlot).getRangeAxis().resizeRange(1 / ZOOM_FACTOR);
				((CategoryPlot)activePlot).getRangeAxis().setLowerBound(0d);
			}
			if (plot != null) {

				plot.getRangeAxis().resizeRange(1 / ZOOM_FACTOR);
				plot.getRangeAxis().setLowerBound(0d);
			}
		}
		else if (command.equals(ChartPanel.ZOOM_OUT_RANGE_COMMAND)) {

			if (activePlot instanceof CategoryPlot) {

				((CategoryPlot)activePlot).getRangeAxis().resizeRange(ZOOM_FACTOR);
				((CategoryPlot)activePlot).getRangeAxis().setLowerBound(0d);
			}
			if (activePlot instanceof XYPlot) {

				((XYPlot)activePlot).getRangeAxis().resizeRange(ZOOM_FACTOR);
				((XYPlot)activePlot).getRangeAxis().setLowerBound(0d);
			}
		}
		else if (command.equals(ChartPanel.ZOOM_RESET_RANGE_COMMAND)) {

			if (activePlot instanceof CategoryPlot)
				((CategoryPlot)activePlot).getRangeAxis().setAutoRange(true);

			if (activePlot instanceof XYPlot)
				((XYPlot)activePlot).getRangeAxis().setAutoRange(true);
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
	
	@SuppressWarnings("unchecked")
	public void toggleAnnotations() {

		annotationsVisible = !annotationsVisible;

		if (activePlot instanceof CategoryPlot)
			((CategoryPlot) activePlot).getRenderer().setDefaultItemLabelsVisible(annotationsVisible);

		if (activePlot instanceof XYPlot)
			((XYPlot) activePlot).getRenderer().setDefaultItemLabelsVisible(annotationsVisible);
				
		toolbar.toggleAnnotationsIcon(annotationsVisible);
	}

	@SuppressWarnings("unused")
	public void toggleDataPoints() {

		dataPointsVisible = !dataPointsVisible;

		if (activePlot instanceof XYPlot) 			
			((XYLineAndShapeRenderer)((XYPlot) activePlot).getRenderer()).setDefaultShapesVisible(dataPointsVisible);		
		
		toolbar.toggleDataPointssIcon(dataPointsVisible);
	}
	
	@Override
	public void itemStateChanged(ItemEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void initAxes() {
		
		if(activePlot instanceof XYPlot) {
			
			if (sortingOrder.equals(FileSortingOrder.NAME))
				((XYPlot)activePlot).setDomainAxis(new NumberAxis("Data files"));
			
			if (sortingOrder.equals(FileSortingOrder.TIMESTAMP))
				((XYPlot)activePlot).setDomainAxis(new DateAxis("Injection time"));
		}
	}

	@Override
	protected void initChart() {

		if (plotType.equals(QcPlotType.BARCHART)) {
			
			chart = ChartFactory.createBarChart("", // title
					"", // x-axis label - categories
					"Value", // y-axis label
					null, // data set
					PlotOrientation.VERTICAL, // orientation
					true, // create legend?
					true, // generate tooltips?
					false // generate URLs?
			);			
		}
		if (plotType.equals(QcPlotType.BOXPLOT)) {

			BoxAndWhiskerCategoryDataset bwset = null;

			chart = ChartFactory.createBoxAndWhiskerChart("", // title
					"Data files", // y-axis label
					"Value", // x-axis label
					bwset, // data set
					true // create legend?
			);			
			BoxAndWhiskerRenderer renderer = (BoxAndWhiskerRenderer) chart.getCategoryPlot().getRenderer();
			renderer.setFillBox(true);
			renderer.setMeanVisible(false);
			renderer.setDefaultToolTipGenerator(new FileStatsBoxAndWhiskerToolTipGenerator());
		}
		if (plotType.equals(QcPlotType.SCATTER)) {

			chart = ChartFactory.createScatterPlot("", // title
					"", // x-axis label
					"Value", // y-axis label
					null, // data set
					PlotOrientation.VERTICAL, // orientation
					true, // create legend?
					true, // generate tooltips?
					false // generate URLs?
			);
		}
		if (plotType.equals(QcPlotType.LINES)) {

			chart = ChartFactory.createXYLineChart("", // title
					"", // x-axis label
					"Value", // y-axis label
					null, // data set
					PlotOrientation.VERTICAL, // orientation
					true, // create legend?
					true, // generate tooltips?
					false // generate URLs?
			);
		}
		chart.setBackgroundPaint(Color.white);
		activePlot = chart.getPlot();
		setBasicPlotGui(activePlot);
		setChart(chart);
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
	protected void initPlot() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeAllDataSets() {

		if(chart.getPlot() instanceof XYPlot) {
			
			for (int i = 0; i < ((XYPlot)chart.getPlot()).getDatasetCount(); i++)
				((XYPlot)chart.getPlot()).setDataset(i, null);						
		}
		if(chart.getPlot() instanceof CategoryPlot) {
			
			for (int i = 0; i < ((CategoryPlot)chart.getPlot()).getDatasetCount(); i++)
				((CategoryPlot)chart.getPlot()).setDataset(i, null);
		}
	}

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

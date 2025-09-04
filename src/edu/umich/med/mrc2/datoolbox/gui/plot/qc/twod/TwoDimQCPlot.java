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
import java.awt.event.ItemEvent;
import java.io.File;
import java.text.NumberFormat;
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
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.data.statistics.BoxAndWhiskerCategoryDataset;

import edu.umich.med.mrc2.datoolbox.data.DataFileStatisticalSummary;
import edu.umich.med.mrc2.datoolbox.data.enums.DataSetQcField;
import edu.umich.med.mrc2.datoolbox.data.enums.FileSortingOrder;
import edu.umich.med.mrc2.datoolbox.data.enums.ImageExportFormat;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.plot.AbstractControlledDataPlot;
import edu.umich.med.mrc2.datoolbox.gui.plot.dataset.PlotDataSetUtils;
import edu.umich.med.mrc2.datoolbox.gui.plot.dataset.QcBoxPlotDataSet;
import edu.umich.med.mrc2.datoolbox.gui.plot.dataset.QcScatterDataSet;
import edu.umich.med.mrc2.datoolbox.gui.plot.dataset.QcTimedScatterSet;
import edu.umich.med.mrc2.datoolbox.gui.plot.dataset.VariableCategorySizeBarChartDataSet;
import edu.umich.med.mrc2.datoolbox.gui.plot.renderer.category.VariableCategorySizeBarRenderer;
import edu.umich.med.mrc2.datoolbox.gui.plot.renderer.category.VariableCategorySizeCategoryAxis;
import edu.umich.med.mrc2.datoolbox.gui.plot.stats.DataPlotControlsPanel;
import edu.umich.med.mrc2.datoolbox.gui.plot.stats.StatsPlotType;
import edu.umich.med.mrc2.datoolbox.gui.plot.tooltip.FileStatsBoxAndWhiskerToolTipGenerator;
import edu.umich.med.mrc2.datoolbox.gui.plot.tooltip.StatsPlotDataFileToolTipGenerator;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.project.Project;
import edu.umich.med.mrc2.datoolbox.utils.FIOUtils;

public class TwoDimQCPlot extends AbstractControlledDataPlot {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8509961200306648220L;
	
	protected StatsPlotType plotType;
	protected TwoDqcPlotParameterObject plotParameters;
	protected Collection<DataFileStatisticalSummary> dataSetStats;
	protected Plot activePlot;
	protected DataPlotControlsPanel dataPlotControlsPanel;
	protected TwoDqcPlotToolbar toolbar;

	public TwoDimQCPlot() {
		super();
		setHandlePlotSaveExternally(true);
	}
	
	public void loadDataSetStats(Collection<DataFileStatisticalSummary> dataSetStats2) {
		
		if(dataSetStats2 == null) {
			this.dataSetStats = null;
			removeAllDataSets();
			return;
		}
		this.dataSetStats = dataSetStats2;
		updateParametersFromControls();		
		
		if (plotType.equals(StatsPlotType.BARCHART))			
			loadBarChart();
		
		if ((plotType.equals(StatsPlotType.LINES) || plotType.equals(StatsPlotType.SCATTER)) 
				&& plotParameters.getSortingOrder().equals(FileSortingOrder.NAME))
			chart.getXYPlot().setDataset(new QcScatterDataSet(plotParameters));
		
		if ((plotType.equals(StatsPlotType.LINES) || plotType.equals(StatsPlotType.SCATTER)) 
				&& plotParameters.getSortingOrder().equals(FileSortingOrder.TIMESTAMP))
			chart.getXYPlot().setDataset(new QcTimedScatterSet(plotParameters));
		
		if (plotType.equals(StatsPlotType.BOXPLOT))
			chart.getCategoryPlot().setDataset(new QcBoxPlotDataSet(plotParameters));
	}
	
	protected void loadBarChart() {
		
		//	Not supported for QC plot type
		if(plotParameters.getStatsField().equals(DataSetQcField.RAW_VALUES)) {
			MessageDialog.showWarningMsg("Option not supported for QC barchart plot", this);
			return;
		}
 		VariableCategorySizeBarChartDataSet ds = 
 				new VariableCategorySizeBarChartDataSet(plotParameters);		
		VariableCategorySizeBarRenderer renderer = new VariableCategorySizeBarRenderer();
		
		NumberFormat dataFormat = 
				PlotDataSetUtils.getNumberFormatForDataSetQcField(plotParameters.getStatsField());
        renderer.setDefaultToolTipGenerator(new StatsPlotDataFileToolTipGenerator(dataFormat, null));
		for(int i=0; i<ds.getRowCount(); i++)
			renderer.setSeriesPaint(i, ds.getSeriesPaintMap().get(i));

		((CategoryPlot)chart.getPlot()).setDomainAxis(new VariableCategorySizeCategoryAxis());
		((CategoryPlot)chart.getPlot()).setRenderer(renderer);
		
		chart.getCategoryPlot().setDataset(ds);
	}
	
	@Override
	public void redrawPlot() {

		removeAllDataSets();		
		if (dataSetStats != null)
			loadDataSetStats(dataSetStats);		
	}	
	
	@Override
	public void updateParametersFromControls() {
		
		if(chart == null || !toolbar.getStatsPlotType().equals(plotType)) {
			
			plotType = toolbar.getStatsPlotType();
			initChart();
			initTitles();
			initAxes();
			initLegend(RectangleEdge.BOTTOM, legendVisible);
			dataPlotControlsPanel.updatePlotGroupingOptions(plotType);
		}
		plotParameters = 
				new TwoDqcPlotParameterObject(
					dataSetStats,
					toolbar.getStatParameter(),
					dataPlotControlsPanel.getCategory(), 
					dataPlotControlsPanel.getSububCategory(),
					dataPlotControlsPanel.getDataGroupingType(), 
					toolbar.getSortingOrder(), 
					toolbar.getDataScale(),
					plotType,
					toolbar.getChartColorOption());	
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
		else if (command.equals(ChartPanel.ZOOM_RESET_BOTH_COMMAND)) {
			this.restoreAutoBounds();
		}
		else if(command.equals(ChartPanel.SAVE_COMMAND) 
				|| command.equals(MainActionCommands.SAVE_AS_PNG.name()) 
				|| command.equals(MainActionCommands.SAVE_AS_SVG.name()) 
				|| command.equals(MainActionCommands.SAVE_AS_PDF.name())){
				saveQCplot(command);
		}
		else
			super.actionPerformed(event);
	}
	
	private void saveQCplot(String command) {
		
		Project experiment = MRC2ToolBoxCore.getActiveMetabolomicsExperiment();
		if(experiment == null)
			experiment = MRC2ToolBoxCore.getActiveOfflineRawDataAnalysisExperiment();
		
		if(experiment == null)
			return;
		
		String prefix = experiment.getName();
		if(experiment.getLimsExperiment() != null)
			prefix = experiment.getLimsExperiment().getId();
		
		File exportFolder = experiment.getExportsDirectory();
		ImageExportFormat format = ImageExportFormat.PNG;
		
		if(command.equals(MainActionCommands.SAVE_AS_PDF.name()))
			format = ImageExportFormat.PDF;
		
		if(command.equals(MainActionCommands.SAVE_AS_SVG.name()))
			format = ImageExportFormat.SVG;
		
		String plotFileName = prefix + "_"
				+ plotParameters.getDesignDescriptor()
				+ "_" + FIOUtils.getTimestamp()
				+ "." + format.getExtension();
		String plotFileNameClean = FIOUtils.createSaveSafeName(plotFileName).replaceAll("\\s+", "_");
		
		saveChartAsImageToFile(exportFolder, plotFileNameClean, format);
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void initAxes() {
		
		if(activePlot instanceof XYPlot) {
			
			if (plotParameters.getSortingOrder().equals(FileSortingOrder.NAME))
				((XYPlot)activePlot).setDomainAxis(new NumberAxis("Data files"));
			
			if (plotParameters.getSortingOrder().equals(FileSortingOrder.TIMESTAMP))
				((XYPlot)activePlot).setDomainAxis(new DateAxis("Injection time"));
		}
	}

	@Override
	protected void initChart() {

		if (plotType.equals(StatsPlotType.BARCHART)) {
			
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
		if (plotType.equals(StatsPlotType.BOXPLOT)) {

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
		if (plotType.equals(StatsPlotType.SCATTER)) {

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
		if (plotType.equals(StatsPlotType.LINES)) {

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
	
	protected void setBasicPlotGui(Plot newPlot) {
		
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
		numberOfDataSets = 0;		
	}

	
	public void setDataPlotControlsPanel(DataPlotControlsPanel dataPlotControlsPanel) {
		this.dataPlotControlsPanel = dataPlotControlsPanel;
	}


	public void setToolbar(TwoDqcPlotToolbar toolbar) {
		this.toolbar = toolbar;
	}

}

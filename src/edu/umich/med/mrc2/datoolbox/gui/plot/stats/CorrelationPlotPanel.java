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
import java.awt.Dimension;
import java.awt.Font;
import java.awt.geom.Ellipse2D;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.TreeSet;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.renderer.xy.XYDotRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.data.function.LineFunction2D;
import org.jfree.data.general.DatasetUtils;
import org.jfree.data.statistics.Regression;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.ujmp.core.Matrix;

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.ExperimentalSample;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.plot.MasterPlotPanel;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class CorrelationPlotPanel extends MasterPlotPanel {

	/**
	 *
	 */
	private static final long serialVersionUID = 5294133151768346173L;
	private XYSeriesCollection inputData;
	private XYDataset dataset;
	private XYLineAndShapeRenderer regressionRenderer;
	private XYDotRenderer dataRenderer;
	private final NumberFormat corrFormat = new DecimalFormat("#.##");
	private PearsonsCorrelation pc;

	public CorrelationPlotPanel() {

		super();
		initChart();
		initPlot();
		initTitles();

		dataRenderer = new XYDotRenderer();
		dataRenderer.setDefaultShape(new Ellipse2D.Float(100.0f, 100.0f, 100.0f, 100.0f));
		dataRenderer.setDotWidth(10);
		dataRenderer.setDotHeight(10);
		dataRenderer.setSeriesPaint(0, Color.MAGENTA);
		plot.setRenderer(0, dataRenderer);
		regressionRenderer = new XYLineAndShapeRenderer(true, false);
		regressionRenderer.setSeriesPaint(0, Color.BLUE);
		plot.setRenderer(1, regressionRenderer);
		pc = new PearsonsCorrelation();
	}

    @Override
    public Dimension getPreferredSize() {
        // Relies on being the only component
        // in a layout that will center it without
        // expanding it to fill all the space.
        Dimension d = this.getParent().getSize();
        int newSize = d.width > d.height ? d.height : d.width;
        newSize = newSize < 300 ? 300 : newSize;
        return new Dimension(newSize, newSize);
    }

	/*
	 * Get the parameters 'a' and 'b' for an equation y = a + b * x, fitted to
	 * the inputData using ordinary least squares regression. a -
	 * regressionParameters[0], b - regressionParameters[1]
	 */
	private void drawRegressionLine() {

		if(inputData.getItemCount(0) > 1) {

			double regressionParameters[] = Regression.getOLSRegression(inputData, 0);

			// Prepare a line function using the found parameters
			LineFunction2D linefunction2d = new LineFunction2D(regressionParameters[0], regressionParameters[1]);

			// Creates a dataset by taking sample values from the line function
			dataset = DatasetUtils.sampleFunction2D(linefunction2d, inputData.getSeries(0).getMinX(),
					inputData.getSeries(0).getMaxX(), 100, "Fitted Regression Line");

			plot.setDataset(1, dataset);

			double corr = computeCorrelation(inputData.getSeries(0));
			double xPos = inputData.getSeries(0).getMinX() + (inputData.getSeries(0).getMaxX() - inputData.getSeries(0).getMinX())/8.0d;
			XYTextAnnotation ta = new XYTextAnnotation("Corr " + corrFormat.format(corr), xPos, inputData.getSeries(0).getMaxY());
			ta.setFont(new Font("SansSerif", Font.BOLD, 12));
			plot.addAnnotation(ta);
		}
	}

	private double computeCorrelation(XYSeries series) {

		double corr = 0.0d;
		double[] x = new double[series.getItemCount()];
		double[] y = new double[series.getItemCount()];

		for(int i=0; i<series.getItemCount(); i++) {

			x[i] = (double) ((XYDataItem) series.getItems().get(i)).getX();
			y[i] = (double) ((XYDataItem) series.getItems().get(i)).getY();
		}
		corr = pc.correlation(x, y);
		return corr;
	}

	@Override
	protected void initPlot() {

		plot = chart.getXYPlot();
		plot.setBackgroundPaint(Color.white);
		plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
		plot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
		plot.setDomainGridlinePaint(GRID_COLOR);
		plot.setRangeGridlinePaint(GRID_COLOR);
		plot.setDomainCrosshairVisible(false);
		plot.setRangeCrosshairVisible(false);
		plot.setDomainPannable(true);
	}

	public void showCorrelationPlot(
			MsFeature fOne, 
			MsFeature fTwo,
			DataPipeline dataPipeline) {

		removeAllDataSets();
		inputData = new XYSeriesCollection();
		XYSeries series = new XYSeries("Data");
		Matrix datamatrix = MRC2ToolBoxCore.getActiveMetabolomicsExperiment().
				getDataMatrixForDataPipeline(dataPipeline);

		long[] coordinatesOne = new long[2];
		long[] coordinatesTwo = new long[2];
		coordinatesOne[1] = datamatrix.getColumnForLabel(fOne);
		coordinatesTwo[1] = datamatrix.getColumnForLabel(fTwo);

		//	TODO Temporary fix until design subsets properly implemented
		for(DataFile file : MRC2ToolBoxCore.getActiveMetabolomicsExperiment().
				getDataFilesForAcquisitionMethod(dataPipeline.getAcquisitionMethod())) {

			if(file.isEnabled()) {

				long i = datamatrix.getRowForLabel(file);

				coordinatesOne[0] = i;
				coordinatesTwo[0] = i;
				double x = datamatrix.getAsDouble(coordinatesOne);
				double y = datamatrix.getAsDouble(coordinatesTwo);

				if(x > 0 && y > 0)
					series.add(x, y);
			}
		}
		inputData.addSeries(series);
		plot.setDataset(0, inputData);
		plot.getDomainAxis().setLabel(fOne.getName());
		plot.getRangeAxis().setLabel(fTwo.getName());
		drawRegressionLine();
		chart.removeLegend();
	}

	public void showMultiAssayCorrelationPlot(
			MsFeature fOne, 
			DataPipeline dataPipelineOne,
			MsFeature fTwo,
			DataPipeline dataPipelineTwo) {

		removeAllDataSets();
		if(fOne == null || fTwo == null)
			return;

		inputData = new XYSeriesCollection();
		XYSeries series = new XYSeries("Data");
		DataAnalysisProject experiment = MRC2ToolBoxCore.getActiveMetabolomicsExperiment();

		Matrix matrixOne = experiment.getDataMatrixForDataPipeline(dataPipelineOne);
		Matrix matrixTwo = experiment.getDataMatrixForDataPipeline(dataPipelineTwo);

		long[] coordinatesOne = new long[2];
		long[] coordinatesTwo = new long[2];
		coordinatesOne[1] = matrixOne.getColumnForLabel(fOne);
		coordinatesTwo[1] = matrixTwo.getColumnForLabel(fTwo);

		TreeSet<ExperimentalSample> samples =
				experiment.getExperimentDesign().getActiveSamplesForDesignSubset(
						experiment.getExperimentDesign().getActiveDesignSubset());
		DataFile[] filesOne, filesTwo;

		for(ExperimentalSample sample : samples) {

			filesOne = sample.getDataFileArrayForMethod(dataPipelineOne.getAcquisitionMethod());
			filesTwo = sample.getDataFileArrayForMethod(dataPipelineTwo.getAcquisitionMethod());

			int count = Math.min(filesOne.length, filesTwo.length);

			for(int i=0; i<count; i++) {

				if(filesOne[i].isEnabled() && filesTwo[i].isEnabled()) {

					coordinatesOne[0] = matrixOne.getRowForLabel(filesOne[i]);
					coordinatesTwo[0] = matrixTwo.getRowForLabel(filesTwo[i]);

					double x = matrixOne.getAsDouble(coordinatesOne);
					double y = matrixTwo.getAsDouble(coordinatesTwo);

					if(x > 0 && y > 0)
						series.add(x, y);
				}
			}
		}
		inputData.addSeries(series);
		plot.setDataset(0, inputData);
		plot.getDomainAxis().setLabel(fOne.getName());
		plot.getRangeAxis().setLabel(fTwo.getName());
		drawRegressionLine();
		chart.removeLegend();
	}

	public RealMatrix getDataMatrix(){

		if(inputData == null)
			return null;

		if(inputData.getSeries(0) == null)
			return null;

		if(inputData.getSeries(0).getItemCount() == 0)
			return null;

		int length = inputData.getSeries(0).getItemCount();
		double[][] doubleMatrix = new double[length][2];
		for(int i=0; i<length; i++) {

			doubleMatrix[i][0] = (double) inputData.getSeries(0).getX(i);
			doubleMatrix[i][1] = (double) inputData.getSeries(0).getY(i);
		}
		return new Array2DRowRealMatrix(doubleMatrix);
	}

	@Override
	protected void initAxes() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void initChart() {

		chart = ChartFactory.createScatterPlot("Correlation", "", "", null);
		chart.setBackgroundPaint(Color.white);
		setChart(chart);
	}

	@Override
	public synchronized void removeAllDataSets() {

		if(plot == null)
			return;
			
		int count = plot.getDatasetCount();
		for (int i = 0; i < count; i++)
			plot.setDataset(i, null);

		plot.clearAnnotations();
		numberOfDataSets = 0;		
	}
}

















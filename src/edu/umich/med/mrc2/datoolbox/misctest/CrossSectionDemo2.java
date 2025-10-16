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

package edu.umich.med.mrc2.datoolbox.misctest;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.concurrent.ThreadLocalRandom;

import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.event.ChartChangeEvent;
import org.jfree.chart.event.ChartChangeListener;
import org.jfree.chart.plot.Crosshair;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.LookupPaintScale;
import org.jfree.chart.ui.ApplicationFrame;
import org.jfree.chart.ui.UIUtils;
import org.jfree.data.Range;
import org.jfree.data.general.DefaultHeatMapDataset;
import org.jfree.data.general.HeatMapDataset;
import org.jfree.data.general.HeatMapUtils;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeriesCollection;

/**
 * A test for the heat map annotations.
 */
public class CrossSectionDemo2 extends ApplicationFrame {

	static class MyDemoPanel extends DemoPanel implements ChangeListener, ChartChangeListener {

		/**
		 * 
		 */
		private static final long serialVersionUID = -7016764643638669339L;
		private HeatMapDataset dataset;
		private JFreeChart mainChart;

		private Crosshair crosshair1;
		private Crosshair crosshair2;

		private Range lastXRange;

		private Range lastYRange;

		public MyDemoPanel() {

			super(new BorderLayout());
			ChartPanel chartPanel = (ChartPanel) createMainPanel();
			chartPanel.setPreferredSize(new java.awt.Dimension(500, 500));

			add(chartPanel);

			this.mainChart.setNotify(true);
		}

		/**
		 * See if the axis ranges have changed in the main chart and, if so,
		 * update the subchards.
		 *
		 * @param event
		 */
		public void chartChanged(ChartChangeEvent event) {

			XYPlot plot = (XYPlot) this.mainChart.getPlot();

			if (!plot.getDomainAxis().getRange().equals(this.lastXRange)) {
				this.lastXRange = plot.getDomainAxis().getRange();
			}
			if (!plot.getRangeAxis().getRange().equals(this.lastYRange)) {
				this.lastYRange = plot.getRangeAxis().getRange();
			}
		}

		/**
		 * Creates a chart.
		 *
		 * @param dataset
		 *            a dataset.
		 *
		 * @return A chart.
		 */
		private JFreeChart createChart(XYDataset dataset) {

			JFreeChart chart = ChartFactory.createScatterPlot("CrossSectionDemo1", "X", "Y", dataset,
					PlotOrientation.VERTICAL, true, false, false);

			this.dataset = createMapDataset();

			// PaintScale ps = new GrayPaintScale(-1.0, 1.0, 128);
			LookupPaintScale ps = new LookupPaintScale(-1.0, 1.0, Color.black);
			ps.add(-1.0, Color.green);
			ps.add(0.0, Color.orange);
			ps.add(1.0, Color.red);

			BufferedImage image = HeatMapUtils.createHeatMapImage(this.dataset, ps);
			// XYDataImageAnnotation ann = new XYDataImageAnnotation(image,
			// -250.5, -250.5, 501.0, 501.0, true);

			XYPlot plot = (XYPlot) chart.getPlot();
			plot.setDomainPannable(true);
			plot.setRangePannable(true);
			// plot.getRenderer().addAnnotation(ann, Layer.BACKGROUND);
			NumberAxis xAxis = (NumberAxis) plot.getDomainAxis();
			xAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
			xAxis.setLowerMargin(0.0);
			xAxis.setUpperMargin(0.0);
			NumberAxis yAxis = (NumberAxis) plot.getRangeAxis();
			yAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
			yAxis.setLowerMargin(0.0);
			yAxis.setUpperMargin(0.0);
			return chart;
		}

		/**
		 * Creates a panel for the demo (used by SuperDemo.java).
		 *
		 * @return A panel.
		 */
		public JPanel createMainPanel() {

			this.mainChart = createChart(new XYSeriesCollection());
			this.mainChart.addChangeListener(this);
			ChartPanel panel = new ChartPanel(this.mainChart);
			panel.setFillZoomRectangle(true);
			panel.setMouseWheelEnabled(true);
			return panel;
		}

		@Override
		public void stateChanged(ChangeEvent e) {
			// TODO Auto-generated method stub

		}
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 5332198628013011020L;

	public static JPanel createDemoPanel() {
		return new MyDemoPanel();
	}

	private static HeatMapDataset createMapDataset() {
		DefaultHeatMapDataset d = new DefaultHeatMapDataset(20, 20, 0, 19, 0, 19);

		for (int i = 0; i < 20; i++) {

			for (int j = 0; j < 20; j++) {

				d.setZValue(i, j, ThreadLocalRandom.current().nextInt(-10, 20) / 10);
			}
		}
		return d;
	}

	/**
	 * Starting point for the demonstration application.
	 *
	 * @param args
	 *            ignored.
	 */
	public static void main(String[] args) {
		CrossSectionDemo2 demo = new CrossSectionDemo2("JFreeChart: CrossSectionDemo1");
		demo.pack();
		UIUtils.centerFrameOnScreen(demo);
		demo.setVisible(true);
	}

	/**
	 * A demonstration application showing how to create a simple time series
	 * chart. This example uses monthly data.
	 *
	 * @param title
	 *            the frame title.
	 */
	public CrossSectionDemo2(String title) {
		super(title);
		JPanel content = createDemoPanel();
		setContentPane(content);
	}

}

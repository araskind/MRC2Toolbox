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

package edu.umich.med.mrc2.datoolbox.gui.plot.renderer;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.DrawingSupplier;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRendererState;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;

public class LineRenderer  extends XYLineAndShapeRenderer {

	/**
	 *
	 */
	private static final long serialVersionUID = 993531310317888388L;

	public static final float TRANSPARENCY = 0.8f;
	public static final AlphaComposite alphaComp = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, TRANSPARENCY);

	// data points shape
	private static final Shape dataPointsShape = new Ellipse2D.Double(-2, -2, 5, 5);
	private boolean isTransparent;

	public LineRenderer(Color color, boolean isTransparent) {

		this.isTransparent = isTransparent;

		// Set painting color
		setDefaultPaint(color);
		setDefaultFillPaint(color);
		setUseFillPaint(true);

		// Set shape properties
		setDefaultShape(dataPointsShape);
		setDefaultShapesFilled(true);
		setDefaultShapesVisible(false);
		setDrawOutlines(false);

		setSeriesItemLabelsVisible(0, false);
		setSeriesShapesVisible(0, true);
		setSeriesPaint(0, color);
		setSeriesShape(0, dataPointsShape);
		setSeriesStroke(0, new BasicStroke(1.5f));
	}

	public void drawItem(Graphics2D g2, XYItemRendererState state, Rectangle2D dataArea, PlotRenderingInfo info,
			XYPlot plot, ValueAxis domainAxis, ValueAxis rangeAxis, XYDataset dataset, int series, int item,
			CrosshairState crosshairState, int pass) {

		if (isTransparent)
			g2.setComposite(alphaComp);

		super.drawItem(g2, state, dataArea, info, plot, domainAxis, rangeAxis, dataset, series, item, crosshairState,
				pass);

	}

	/**
	 * This method returns null, because we don't want to change the colors
	 * dynamically.
	 */
	public DrawingSupplier getDrawingSupplier() {
		return null;
	}
}


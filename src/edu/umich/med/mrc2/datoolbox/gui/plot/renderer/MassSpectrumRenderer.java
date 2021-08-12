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
import java.awt.geom.Rectangle2D;

import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.DrawingSupplier;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYItemRendererState;
import org.jfree.data.xy.XYDataset;

public class MassSpectrumRenderer extends XYBarRenderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1413011134971429865L;
	public static final float TRANSPARENCY = 0.8f;
	public static final AlphaComposite alphaComp = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, TRANSPARENCY);
	private boolean isTransparent;
	private MsToolTipGenerator toolTipGenerator;

	/**
	 * 
	 */
	public MassSpectrumRenderer() {

		this.isTransparent = false;

		// Shadow makes fake peaks
		setShadowVisible(false);

		// Set the tooltip generator
		toolTipGenerator = new MsToolTipGenerator();
		setDefaultToolTipGenerator(toolTipGenerator);

		// Paint the peaks using simple color without any gradient effects
		setBarPainter(new StandardXYBarPainter());
	}

	public MassSpectrumRenderer(Color color, boolean isTransparent) {

		this.isTransparent = isTransparent;

		// Set painting color
		setDefaultPaint(color);

		// Shadow makes fake peaks
		setShadowVisible(false);

		// Set the tooltip generator
		toolTipGenerator = new MsToolTipGenerator();
		setDefaultToolTipGenerator(toolTipGenerator);

		// Paint the peaks using simple color without any gradient effects
		setBarPainter(new StandardXYBarPainter());
	}

	public MassSpectrumRenderer(Color color, float width) {

		this.isTransparent = false;

		setDefaultPaint(color);
		setDefaultFillPaint(color);

		setAutoPopulateSeriesStroke(false);
		setDefaultStroke(new BasicStroke(width));

		// Shadow makes fake peaks
		setShadowVisible(false);

		// Set the tooltip generator
		toolTipGenerator = new MsToolTipGenerator();
		setDefaultToolTipGenerator(toolTipGenerator);

		// Paint the peaks using simple color without any gradient effects
		setBarPainter(new StandardXYBarPainter());
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

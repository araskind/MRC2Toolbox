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

package edu.umich.med.mrc2.datoolbox.gui.plot.renderer;

import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYErrorRenderer;
import org.jfree.chart.renderer.xy.XYItemRendererState;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.util.PaintUtils;
import org.jfree.chart.util.SerialUtils;
import org.jfree.data.Range;
import org.jfree.data.general.DatasetUtils;
import org.jfree.data.xy.XYDataset;

import edu.umich.med.mrc2.datoolbox.gui.plot.dataset.DataSetWithCustomErrors;

public class XYCustomErrorRenderer extends XYErrorRenderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	//	TODO
	@Override
	protected Range findRangeBounds(XYDataset dataset, boolean includeInterval) {
		if (dataset == null) {
			return null;
		}
		if (getDataBoundsIncludesVisibleSeriesOnly()) {
			List visibleSeriesKeys = new ArrayList();
			int seriesCount = dataset.getSeriesCount();
			for (int s = 0; s < seriesCount; s++) {
				if (isSeriesVisible(s)) {
					visibleSeriesKeys.add(dataset.getSeriesKey(s));
				}
			}
			// the bounds should be calculated using just the items within
			// the current range of the x-axis...if there is one
			Range xRange = null;
			XYPlot p = getPlot();
			if (p != null) {
				ValueAxis xAxis = null;
				int index = p.getIndexOf(this);
				if (index >= 0) {
					xAxis = p.getDomainAxisForDataset(index);
				}
				if (xAxis != null) {
					xRange = xAxis.getRange();
				}
			}
			if (xRange == null) {
				xRange = new Range(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
			}
			return DatasetUtils.findRangeBounds(dataset, visibleSeriesKeys, xRange, includeInterval);
		}
		return DatasetUtils.findRangeBounds(dataset, includeInterval);
	}
	
	   /**
     * Draws the visual representation for one data item.
     *
     * @param g2  the graphics output target.
     * @param state  the renderer state.
     * @param dataArea  the data area.
     * @param info  the plot rendering info.
     * @param plot  the plot.
     * @param domainAxis  the domain axis.
     * @param rangeAxis  the range axis.
     * @param dataset  the dataset.
     * @param series  the series index.
     * @param item  the item index.
     * @param crosshairState  the crosshair state.
     * @param pass  the pass index.
     */
    @Override
    public void drawItem(Graphics2D g2, XYItemRendererState state,
            Rectangle2D dataArea, PlotRenderingInfo info, XYPlot plot,
            ValueAxis domainAxis, ValueAxis rangeAxis, XYDataset dataset,
            int series, int item, CrosshairState crosshairState, int pass) {

        if (pass == 0 && dataset instanceof DataSetWithCustomErrors
                && getItemVisible(series, item)) {
        	DataSetWithCustomErrors ixyd = (DataSetWithCustomErrors) dataset;
            PlotOrientation orientation = plot.getOrientation();
            if (this.getDrawXError()) {
                // draw the error bar for the x-interval
                double x0 = ixyd.getLowerXBorder(series, item);
                double x1 = ixyd.getUpperXBorder(series, item);
                double y = dataset.getYValue(series, item);
                RectangleEdge edge = plot.getDomainAxisEdge();
                double xx0 = domainAxis.valueToJava2D(x0, dataArea, edge);
                double xx1 = domainAxis.valueToJava2D(x1, dataArea, edge);
                double yy = rangeAxis.valueToJava2D(y, dataArea,
                        plot.getRangeAxisEdge());
                Line2D line;
                Line2D cap1;
                Line2D cap2;
                double adj = this.getCapLength() / 2.0;
                if (orientation == PlotOrientation.VERTICAL) {
                    line = new Line2D.Double(xx0, yy, xx1, yy);
                    cap1 = new Line2D.Double(xx0, yy - adj, xx0, yy + adj);
                    cap2 = new Line2D.Double(xx1, yy - adj, xx1, yy + adj);
                }
                else {  // PlotOrientation.HORIZONTAL
                    line = new Line2D.Double(yy, xx0, yy, xx1);
                    cap1 = new Line2D.Double(yy - adj, xx0, yy + adj, xx0);
                    cap2 = new Line2D.Double(yy - adj, xx1, yy + adj, xx1);
                }
                if (this.getErrorPaint() != null) {
                    g2.setPaint(this.getErrorPaint());
                }
                else {
                    g2.setPaint(getItemPaint(series, item));
                }
                if (this.getErrorStroke() != null) {
                    g2.setStroke(this.getErrorStroke());
                }
                else {
                    g2.setStroke(getItemStroke(series, item));
                }
                g2.draw(line);
                g2.draw(cap1);
                g2.draw(cap2);
            }
            if (this.getDrawYError()) {
                // draw the error bar for the y-interval
                double y0 = ixyd.getLowerYBorder(series, item);
                double y1 = ixyd.getUpperYBorder(series, item);
                double x = dataset.getXValue(series, item);
                RectangleEdge edge = plot.getRangeAxisEdge();
                double yy0 = rangeAxis.valueToJava2D(y0, dataArea, edge);
                double yy1 = rangeAxis.valueToJava2D(y1, dataArea, edge);
                double xx = domainAxis.valueToJava2D(x, dataArea,
                        plot.getDomainAxisEdge());
                Line2D line;
                Line2D cap1;
                Line2D cap2;
                double adj = this.getCapLength() / 2.0;
                if (orientation == PlotOrientation.VERTICAL) {
                    line = new Line2D.Double(xx, yy0, xx, yy1);
                    cap1 = new Line2D.Double(xx - adj, yy0, xx + adj, yy0);
                    cap2 = new Line2D.Double(xx - adj, yy1, xx + adj, yy1);
                }
                else {  // PlotOrientation.HORIZONTAL
                    line = new Line2D.Double(yy0, xx, yy1, xx);
                    cap1 = new Line2D.Double(yy0, xx - adj, yy0, xx + adj);
                    cap2 = new Line2D.Double(yy1, xx - adj, yy1, xx + adj);
                }
                if (this.getErrorPaint() != null) {
                    g2.setPaint(this.getErrorPaint());
                }
                else {
                    g2.setPaint(getItemPaint(series, item));
                }
                if (this.getErrorStroke() != null) {
                    g2.setStroke(this.getErrorStroke());
                }
                else {
                    g2.setStroke(getItemStroke(series, item));
                }
                g2.draw(line);
                g2.draw(cap1);
                g2.draw(cap2);
            }
        }
        super.drawItem(g2, state, dataArea, info, plot, domainAxis, rangeAxis,
                dataset, series, item, crosshairState, pass);
    }

    /**
     * Tests this instance for equality with an arbitrary object.
     *
     * @param obj  the object ({@code null} permitted).
     *
     * @return A boolean.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof XYCustomErrorRenderer)) {
            return false;
        }
        XYCustomErrorRenderer that = (XYCustomErrorRenderer) obj;
        if (this.getDrawXError() != that.getDrawXError()) {
            return false;
        }
        if (this.getDrawYError() != that.getDrawYError()) {
            return false;
        }
        if (this.getCapLength() != that.getCapLength()) {
            return false;
        }
        if (!PaintUtils.equal(this.getErrorPaint(), that.getErrorPaint())) {
            return false;
        }
        if (!Objects.equals(this.getErrorStroke(), that.getErrorStroke())) {
            return false;
        }
        return super.equals(obj);
    }

    /**
     * Provides serialization support.
     *
     * @param stream  the input stream.
     *
     * @throws IOException  if there is an I/O error.
     * @throws ClassNotFoundException  if there is a classpath problem.
     */
    private void readObject(ObjectInputStream stream)
            throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        this.setErrorPaint(SerialUtils.readPaint(stream));
        this.setErrorStroke(SerialUtils.readStroke(stream));
    }

    /**
     * Provides serialization support.
     *
     * @param stream  the output stream.
     *
     * @throws IOException  if there is an I/O error.
     */
    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.defaultWriteObject();
        SerialUtils.writePaint(this.getErrorPaint(), stream);
        SerialUtils.writeStroke(this.getErrorStroke(), stream);
    }
}

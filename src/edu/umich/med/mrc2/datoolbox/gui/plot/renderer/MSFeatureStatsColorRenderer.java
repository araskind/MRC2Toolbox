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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.Objects;
import java.util.TreeSet;

import org.apache.commons.collections4.CollectionUtils;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRendererState;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYZDataset;

import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.enums.MSFeatureSetStatisticalParameters;
import edu.umich.med.mrc2.datoolbox.gui.datexp.dataset.MsFeatureBubbleDataSet;
import edu.umich.med.mrc2.datoolbox.gui.plot.ColorGradient;
import edu.umich.med.mrc2.datoolbox.gui.plot.ColorScale;
import edu.umich.med.mrc2.datoolbox.utils.Range;

public class MSFeatureStatsColorRenderer extends ColorCodedXYLineAndShapeRenderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private MSFeatureSetStatisticalParameters statsParameter;
	
	public MSFeatureStatsColorRenderer(
			MsFeatureBubbleDataSet datasetToRender, 
			MSFeatureSetStatisticalParameters statsParameter) {
		super(datasetToRender);
		this.statsParameter = statsParameter;
		createLookupPaintScale();
	}

	public MSFeatureStatsColorRenderer(
			MsFeatureBubbleDataSet datasetToRender, 
			ColorGradient colorGradient, 
			ColorScale colorScale,
			MSFeatureSetStatisticalParameters statsParameter) {
		super(datasetToRender, colorGradient, colorScale);
		this.statsParameter = statsParameter;
		createLookupPaintScale();
	}

	@Override
	protected Range createDataRange() {
				
		if(statsParameter.equals(MSFeatureSetStatisticalParameters.PERCENT_MISSING_IN_POOLS)
			|| statsParameter.equals(MSFeatureSetStatisticalParameters.PERCENT_MISSING_IN_SAMPLES))
			return new Range(0.0d, 100.0d);
			
		TreeSet<Double>data = new TreeSet<Double>();
		MsFeatureBubbleDataSet ds = 
				(MsFeatureBubbleDataSet)datasetToRender;
		Collection<MsFeature>allFeatures = ds.getAllFeatures();
		allFeatures.stream().
			filter(f -> Objects.nonNull(f.getStatsSummary())).
			forEach(f -> CollectionUtils.addIgnoreNull(
					data, f.getStatsSummary().getValueOfTypeForPlot(statsParameter)));	
		data.removeIf(v -> Double.isNaN(v));
		
		Range dataRange = new Range(data.first(), data.last());
		return dataRange;
	}

	@Override
	public Paint getItemPaint(int row, int column) {

		MsFeature feature = 
				((MsFeatureBubbleDataSet)datasetToRender).getMsFeature(row, column);
		if(feature.getStatsSummary() == null 
				|| feature.getStatsSummary().getValueOfTypeForPlot(statsParameter) == null)
			return Color.LIGHT_GRAY;
		
		double value = feature.getStatsSummary().getValueOfTypeForPlot(statsParameter);
		
		return lookupPaintScale.getPaint(value);
	}

	public void setStatsParameter(MSFeatureSetStatisticalParameters statsParameter) {
		
		this.statsParameter = statsParameter;
		createLookupPaintScale();
	}
	
    /**
     * Draws the visual representation of a single data item.
     *
     * @param g2  the graphics device.
     * @param state  the renderer state.
     * @param dataArea  the area within which the data is being drawn.
     * @param info  collects information about the drawing.
     * @param plot  the plot (can be used to obtain standard color
     *              information etc).
     * @param domainAxis  the domain (horizontal) axis.
     * @param rangeAxis  the range (vertical) axis.
     * @param dataset  the dataset (an {@link XYZDataset} is expected).
     * @param series  the series index (zero-based).
     * @param item  the item index (zero-based).
     * @param crosshairState  crosshair information for the plot
     *                        ({@code null} permitted).
     * @param pass  the pass index.
     */
    @Override
    public void drawItem(Graphics2D g2, XYItemRendererState state,
            Rectangle2D dataArea, PlotRenderingInfo info, XYPlot plot,
            ValueAxis domainAxis, ValueAxis rangeAxis, XYDataset dataset,
            int series, int item, CrosshairState crosshairState, int pass) {

        // return straight away if the item is not visible
        if (!getItemVisible(series, item)) {
            return;
        }

        PlotOrientation orientation = plot.getOrientation();

        // get the data point...
        double x = dataset.getXValue(series, item);
        double y = dataset.getYValue(series, item);
        double z = Double.NaN;
        if (dataset instanceof XYZDataset) {
            XYZDataset xyzData = (XYZDataset) dataset;
            z = xyzData.getZValue(series, item);
        }
        if (!Double.isNaN(z)) {
            RectangleEdge domainAxisLocation = plot.getDomainAxisEdge();
            RectangleEdge rangeAxisLocation = plot.getRangeAxisEdge();
            double transX = domainAxis.valueToJava2D(x, dataArea,
                    domainAxisLocation);
            double transY = rangeAxis.valueToJava2D(y, dataArea,
                    rangeAxisLocation);
            double zero1 = domainAxis.valueToJava2D(0.0, dataArea,
                    domainAxisLocation);
            double zero2 = rangeAxis.valueToJava2D(0.0, dataArea,
                    rangeAxisLocation);
            double transDomain = domainAxis.valueToJava2D(z, dataArea,
                    domainAxisLocation) - zero1;
            double transRange = zero2 - rangeAxis.valueToJava2D(z, dataArea,
                    rangeAxisLocation);           
            transDomain = Math.abs(transDomain);
            transRange = Math.abs(transRange);
            Ellipse2D circle = null;
            if (orientation == PlotOrientation.VERTICAL) {
                circle = new Ellipse2D.Double(transX - z / 2.0,
                        transY - z / 2.0, z, z);
            }
            else if (orientation == PlotOrientation.HORIZONTAL) {
                circle = new Ellipse2D.Double(transY - z / 2.0,
                        transX - z / 2.0, z, z);
            } else {
                throw new IllegalStateException();
            }
            g2.setPaint(getItemPaint(series, item));
            g2.fill(circle);
            g2.setStroke(getItemOutlineStroke(series, item));
            g2.setPaint(getItemOutlinePaint(series, item));
            g2.draw(circle);

            if (isItemLabelVisible(series, item)) {
                if (orientation == PlotOrientation.VERTICAL) {
                    drawItemLabel(g2, orientation, dataset, series, item,
                            transX, transY, false);
                }
                else if (orientation == PlotOrientation.HORIZONTAL) {
                    drawItemLabel(g2, orientation, dataset, series, item,
                            transY, transX, false);
                }
            }

            // add an entity if this info is being collected
            if (info != null) {
                EntityCollection entities 
                        = info.getOwner().getEntityCollection();
                if (entities != null && circle.intersects(dataArea)) {
                    addEntity(entities, circle, dataset, series, item,
                            circle.getCenterX(), circle.getCenterY());
                }
            }

            int datasetIndex = plot.indexOf(dataset);
            updateCrosshairValues(crosshairState, x, y, datasetIndex,
                    transX, transY, orientation);
        }
    }
}











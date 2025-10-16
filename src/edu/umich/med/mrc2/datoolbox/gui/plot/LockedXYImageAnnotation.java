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

package edu.umich.med.mrc2.datoolbox.gui.plot;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.jfree.chart.annotations.XYImageAnnotation;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.ui.RectangleAnchor;
import org.jfree.chart.ui.RectangleEdge;

public class LockedXYImageAnnotation extends XYImageAnnotation {

	private static final long serialVersionUID = 1L;

	private double edgeOffset;
	private float opacity = 1.0f;
	
	public LockedXYImageAnnotation(
			Image image, 
			RectangleAnchor anchor,
			double edgeOffset,
			float opacity) {
		super(0.0d, 0.0d, image, anchor);
		this.edgeOffset = edgeOffset;
		this.opacity = opacity;
	}

    /**
     * Draws the annotation.  This method is called by the drawing code in the
     * {@link XYPlot} class, you don't normally need to call this method
     * directly.
     *
     * @param g2  the graphics device.
     * @param plot  the plot.
     * @param dataArea  the data area.
     * @param domainAxis  the domain axis.
     * @param rangeAxis  the range axis.
     * @param rendererIndex  the renderer index.
     * @param info  if supplied, this info object will be populated with
     *              entity information.
     */
    @Override
    public void draw(Graphics2D g2, XYPlot plot, Rectangle2D dataArea,
                     ValueAxis domainAxis, ValueAxis rangeAxis,
                     int rendererIndex,
                     PlotRenderingInfo info) {

        PlotOrientation orientation = plot.getOrientation();
        RectangleEdge domainEdge = Plot.resolveDomainAxisLocation(
                plot.getDomainAxisLocation(), orientation);
        RectangleEdge rangeEdge = Plot.resolveRangeAxisLocation(
                plot.getRangeAxisLocation(), orientation);
        
        float xx = 0.0f;
        float yy = 0.0f;
        float j2DX = getXanchor(g2, dataArea, domainAxis, domainEdge);
        float j2DY = getYanchor(g2, dataArea, rangeAxis, rangeEdge);

        if (orientation == PlotOrientation.HORIZONTAL) {
            xx = j2DY;
            yy = j2DX;
        }
        else if (orientation == PlotOrientation.VERTICAL) {
            xx = j2DX;
            yy = j2DY;
        }
        int w = getImage().getWidth(null);
        int h = getImage().getHeight(null);

        Rectangle2D imageRect = new Rectangle2D.Double(0, 0, w, h);
        Point2D anchorPoint = getImageAnchor().getAnchorPoint(imageRect);
        xx = xx - (float) anchorPoint.getX();
        yy = yy - (float) anchorPoint.getY();
        
        if(opacity < 1.0f) {
        	Composite tmpComp = g2.getComposite();       	
	        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));        
	        g2.drawImage(getImage(), (int) xx, (int) yy, null);
	        g2.setComposite(tmpComp);
        }
        else {
        	 g2.drawImage(getImage(), (int) xx, (int) yy, null);        	 
        }
        String toolTip = getToolTipText();
        String url = getURL();
        if (toolTip != null || url != null) {
            addEntity(info, new Rectangle2D.Float(xx, yy, w, h), rendererIndex,
                    toolTip, url);
        }
    }
    
    private float getXanchor(
    		Graphics2D g2, 
    		Rectangle2D dataArea,
            ValueAxis domainAxis,
            RectangleEdge domainEdge) {
    	
        float anchorX = 0.0f;
        if(getImageAnchor().equals(RectangleAnchor.LEFT)
        		|| getImageAnchor().equals(RectangleAnchor.BOTTOM_LEFT)
        		|| getImageAnchor().equals(RectangleAnchor.TOP_LEFT)) {
        	
            anchorX = (float) domainAxis.valueToJava2D(
            		(domainAxis.getRange().getLowerBound() + 
            				(domainAxis.getRange().getLength() * edgeOffset)), dataArea, domainEdge);
        }
        if(getImageAnchor().equals(RectangleAnchor.CENTER)
        		|| getImageAnchor().equals(RectangleAnchor.BOTTOM)
        		|| getImageAnchor().equals(RectangleAnchor.TOP)) {
        	
            anchorX = (float) (domainAxis.valueToJava2D(
            		(domainAxis.getRange().getLowerBound() + 
            				(domainAxis.getRange().getLength() / 2)), dataArea, domainEdge));
        }
        if(getImageAnchor().equals(RectangleAnchor.RIGHT)
        		|| getImageAnchor().equals(RectangleAnchor.BOTTOM_RIGHT)
        		|| getImageAnchor().equals(RectangleAnchor.TOP_RIGHT)) {
        	
            anchorX = (float) (domainAxis.valueToJava2D(
            		(domainAxis.getRange().getUpperBound() - 
            				(domainAxis.getRange().getLength() * edgeOffset)), dataArea, domainEdge));
        }
    	return anchorX;
    }
    
    private float getYanchor(
    		Graphics2D g2, 
    		Rectangle2D dataArea,
            ValueAxis rangeAxis,
            RectangleEdge rangeEdge) {
    	
        float anchorY = 0.0f;
        if(getImageAnchor().equals(RectangleAnchor.TOP)
        		|| getImageAnchor().equals(RectangleAnchor.TOP_LEFT)
        		|| getImageAnchor().equals(RectangleAnchor.TOP_RIGHT)) {
        	
        	anchorY = (float) rangeAxis.valueToJava2D(
                    (rangeAxis.getRange().getUpperBound() - 
                    		(rangeAxis.getRange().getLength() * edgeOffset)), dataArea, rangeEdge);
        }
        if(getImageAnchor().equals(RectangleAnchor.BOTTOM)
        		|| getImageAnchor().equals(RectangleAnchor.BOTTOM_LEFT)
        		|| getImageAnchor().equals(RectangleAnchor.BOTTOM_RIGHT)) {

          	anchorY = (float) (rangeAxis.valueToJava2D(
                    (rangeAxis.getRange().getLowerBound() + 
                    		(rangeAxis.getRange().getLength() * edgeOffset)), dataArea, rangeEdge));
        }
    	return anchorY;
    }
}

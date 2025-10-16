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

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.text.TextUtils;
import org.jfree.chart.ui.RectangleEdge;

public class LockedXYTextAnnotation extends XYTextAnnotation {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private double edgeOffset;	//	fraction of axis length from the edges of the plot;
	public LockedXYTextAnnotation(String text, double edgeOffset) {
		super(text, 0.0d, 0.0d);
		this.edgeOffset = edgeOffset;
	}

	/**
     * Draws the annotation.
     *
     * @param g2  the graphics device.
     * @param plot  the plot.
     * @param dataArea  the data area.
     * @param domainAxis  the domain axis.
     * @param rangeAxis  the range axis.
     * @param rendererIndex  the renderer index.
     * @param info  an optional info object that will be populated with
     *              entity information.
     */
    @Override
	public void draw(
			Graphics2D g2, XYPlot plot, Rectangle2D dataArea, 
			ValueAxis domainAxis, ValueAxis rangeAxis,
			int rendererIndex, PlotRenderingInfo info) {

		PlotOrientation orientation = plot.getOrientation();
		RectangleEdge domainEdge = Plot.resolveDomainAxisLocation(
				plot.getDomainAxisLocation(), orientation);
		RectangleEdge rangeEdge = Plot.resolveRangeAxisLocation(
				plot.getRangeAxisLocation(), orientation);

		float anchorX = getXanchor(g2, dataArea, domainAxis, domainEdge);
		float anchorY = getYanchor(g2, dataArea, rangeAxis, rangeEdge);

		if (orientation == PlotOrientation.HORIZONTAL) {
			float tempAnchor = anchorX;
			anchorX = anchorY;
			anchorY = tempAnchor;
		}
		g2.setFont(getFont());
		if(getText().split("\n").length == 1) {
			
			Rectangle2D hotspot = TextUtils.calcAlignedStringBounds(
					getText(), g2, anchorX, anchorY, getTextAnchor());
			if (this.getBackgroundPaint() != null) {
				g2.setPaint(this.getBackgroundPaint());
				g2.fill(hotspot);
			}
			g2.setPaint(getPaint());
			TextUtils.drawAlignedString(
					getText(), g2, anchorX, anchorY, getTextAnchor());
			if (this.isOutlineVisible()) {
				g2.setStroke(this.getOutlineStroke());
				g2.setPaint(this.getOutlinePaint());
				g2.draw(hotspot);
			}
			String toolTip = getToolTipText();
			String url = getURL();
			if (toolTip != null || url != null) {
				addEntity(info, hotspot, rendererIndex, toolTip, url);
			}
		}
		if(getText().split("\n").length > 1) {
			
			g2.setPaint(getPaint());
			float lineHeight = (float)g2.getFontMetrics().getStringBounds(getText(), g2).getHeight();
			for(String line : getText().split("\n")) {
				
				TextUtils.drawAlignedString(
						line, g2, anchorX, anchorY, getTextAnchor());
				anchorY = anchorY + lineHeight;
			}
		}
	}

    private float getXanchor(
    		Graphics2D g2, 
    		Rectangle2D dataArea,
            ValueAxis domainAxis,
            RectangleEdge domainEdge) {
    	
        float anchorX = 0.0f;
        if(getTextAnchor().isLeft()) {
        	
            anchorX = (float) domainAxis.valueToJava2D(
            		(domainAxis.getRange().getLowerBound() + 
            				(domainAxis.getRange().getLength() * edgeOffset)), dataArea, domainEdge);
        }
        if(getTextAnchor().isHorizontalCenter()) {
        	
            anchorX = (float) (domainAxis.valueToJava2D(
            		(domainAxis.getRange().getLowerBound() + 
            				(domainAxis.getRange().getLength() / 2)), dataArea, domainEdge));
        }
        if(getTextAnchor().isRight()) {
        	
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
        if(getTextAnchor().isTop()) {
        	
        	anchorY = (float) rangeAxis.valueToJava2D(
                    (rangeAxis.getRange().getUpperBound() - 
                    		(rangeAxis.getRange().getLength() * edgeOffset)), dataArea, rangeEdge);
        }
        if(getTextAnchor().isBottom()) {

            Rectangle2D bounds = TextUtils.getTextBounds(getText(), g2, g2.getFontMetrics());
            double lineNum = getText().split("\n").length;
            float extraOffset = (float)((bounds.getHeight() * lineNum) + bounds.getY());
          	anchorY = (float) (rangeAxis.valueToJava2D(
                    (rangeAxis.getRange().getLowerBound() + 
                    		(rangeAxis.getRange().getLength() * edgeOffset)), dataArea, rangeEdge) - extraOffset );
        }
    	return anchorY;
    }
}














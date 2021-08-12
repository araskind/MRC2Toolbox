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

package edu.umich.med.mrc2.datoolbox.gui.datexp;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.IntervalMarker;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.ui.Layer;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.ui.RectangleInsets;

import edu.umich.med.mrc2.datoolbox.gui.plot.MasterPlotPanel;
import edu.umich.med.mrc2.datoolbox.utils.Range;

public class DataExplorerPlotPanel extends MasterPlotPanel {

	/**
	 *
	 */
	private static final long serialVersionUID = 5995453754510129102L;
	private NumberAxis xAxis, yAxis;
	private DataExplorerPlotType plotType;
	public static final String DEFAULT_MASS_DEFECT_X_AXIS_TITLE = "M/Z";
	public static final String DEFAULT_MASS_DEFECT_Y_AXIS_TITLE = "Mass defect";

	protected transient Rectangle2D markerRectangle = null;
	protected Point2D markerStartPoint = null;
//	protected Point2D markerEndPoint = null;
	protected static final Color markerColor = new Color(255, 0, 0, 70);
	protected static final Color markerOutlineColor = Color.RED;
	protected static final Paint invizibleMarkerColor = new Color(255, 255, 255, 0);
	
	protected IntervalMarker domainMarker;
	protected IntervalMarker rangeMarker;
	protected ValueMarker xMarker, yMarker;
	protected JPopupMenu plotPopupMenu;

	public DataExplorerPlotPanel(DataExplorerPlotType plotType) {
		super();

		this.plotType = plotType;
		legendVisible = false;
		initChart();
		initLegend(RectangleEdge.RIGHT, legendVisible);
		initPlot();
		addDoubleClickReset();
		setPopupMenu(null);
	}

	@Override
	protected void initAxes() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void initChart() {

		if (plotType.equals(DataExplorerPlotType.MZRT)) {
			chart = ChartFactory.createBubbleChart(
					"", 
					"Retention time", 
					"M/Z", 
					null, 
					PlotOrientation.VERTICAL, 
					true,
					true, 
					false);
		}
		if (plotType.equals(DataExplorerPlotType.MASS_DEFECT_MZ)) {
			chart = ChartFactory.createScatterPlot(
					"",
					DEFAULT_MASS_DEFECT_X_AXIS_TITLE, 
					DEFAULT_MASS_DEFECT_Y_AXIS_TITLE, 
					null, 
					PlotOrientation.VERTICAL, 
					true, 
					true, 
					false);
		}
		if (plotType.equals(DataExplorerPlotType.MSMS_MZ_RT)) {
			
			chart = ChartFactory.createScatterPlot(
					"",
					"Retention time", 
					"M/Z", 
					null, 
					PlotOrientation.VERTICAL, 
					true, 
					true, 
					false);
		}		
		chart.setBackgroundPaint(Color.white);
		addCrosshair();
		setChart(chart);
	}
	
	private void addCrosshair() {
		
		addChartMouseListener(new ChartMouseListener() {

			@Override
			public void chartMouseMoved(ChartMouseEvent event) {
				try {					  					
					Point2D p = getChartPointAtMouse(event);
					yMarker.setValue(p.getY());


					xMarker.setValue(p.getX());

				//	repaint();
				} catch (Exception e) {

				}
			}

			@Override
			public void chartMouseClicked(ChartMouseEvent event) {
				// TODO Auto-generated method stub

			}
		});
	}
	
	private void removeForegroundMarkers() {
		
		plot.getDomainMarkers(Layer.FOREGROUND);
	}

	@Override
	public void mouseDragged(MouseEvent e) {

//		markerRectangle = null;
//		protected Point2D markerStartPoint 
		if (MouseEvent.getMouseModifiersText(e.getModifiers()).equals("Shift+Button1")) {

	        // if no initial zoom point was set, ignore dragging...
	        if (this.markerStartPoint == null) {
	            return;
	        }
	        Graphics2D g2 = (Graphics2D) getGraphics();
	        boolean hZoom, vZoom;
	        if (plot.getOrientation() == PlotOrientation.HORIZONTAL) {
	            hZoom = this.isRangeZoomable();
	            vZoom = this.isDomainZoomable();
	        }
	        else {
	            hZoom = this.isDomainZoomable();
	            vZoom = this.isRangeZoomable();
	        }
	        markerRectangle = getScreenDataArea(
	                (int) this.markerStartPoint.getX(), (int) this.markerStartPoint.getY());
	        if (hZoom && vZoom) {
	            // selected rectangle shouldn't extend outside the data area...
	            double xmax = Math.min(e.getX(), markerRectangle.getMaxX());
	            double ymax = Math.min(e.getY(), markerRectangle.getMaxY());
	            markerRectangle = new Rectangle2D.Double(
	                    this.markerStartPoint.getX(), this.markerStartPoint.getY(),
	                    xmax - this.markerStartPoint.getX(), ymax - this.markerStartPoint.getY());
	        }
	        else if (hZoom) {
	            double xmax = Math.min(e.getX(), markerRectangle.getMaxX());
	            markerRectangle = new Rectangle2D.Double(
	                    this.markerStartPoint.getX(), markerRectangle.getMinY(),
	                    xmax - this.markerStartPoint.getX(), markerRectangle.getHeight());
	        }
	        else if (vZoom) {
	            double ymax = Math.min(e.getY(), markerRectangle.getMaxY());
	            markerRectangle = new Rectangle2D.Double(
	            		markerRectangle.getMinX(), this.markerStartPoint.getY(),
	            		markerRectangle.getWidth(), ymax - this.markerStartPoint.getY());
	        }
	        drawMarkerRectangle(g2, true);	       
	        g2.dispose();
	        setInvizibleMarkers();
		} 
		else {
			super.mouseDragged(e);
		}
	}
	
	@Override
	public void mousePressed(MouseEvent e) {

		if (MouseEvent.getMouseModifiersText(e.getModifiers()).equals("Shift+Button1")) {

//			markerEndPoint = null;
            Rectangle2D screenDataArea = getScreenDataArea(e.getX(), e.getY());
            if (screenDataArea != null) {
                this.markerStartPoint = getPointInRectangle(e.getX(), e.getY(),
                        screenDataArea);
            }
            else {
                this.markerStartPoint = null;
            }
            markerRectangle = null;
		} 
		else if (SwingUtilities.isRightMouseButton(e) && markerRectangle != null 
				&& markerRectangle.contains(e.getX(), e.getY())) {
				 displayFeatureSelectionPopupMenu(e.getX(), e.getY());
		}
		else {
			super.mousePressed(e);	
		}		
	}
	
	private void  displayFeatureSelectionPopupMenu(int x, int y) {
		
		if(plotPopupMenu != null)
			plotPopupMenu.show(this, x, y);
	}

	@Override
	public void mouseReleased(MouseEvent e) {

		if (MouseEvent.getMouseModifiersText(e.getModifiers()).equals("Shift+Button1")) {

//            Rectangle2D screenDataArea = getScreenDataArea(e.getX(), e.getY());
//            if (screenDataArea != null) {
//                this.markerEndPoint = getPointInRectangle(e.getX(), e.getY(),
//                        screenDataArea);
//            }
//            else {
//                this.markerEndPoint = null;
//            }
            setInvizibleMarkers();
		} 
		else {
			super.mouseReleased(e);
		}
	}
	
	private void setInvizibleMarkers() {
		
		if(markerRectangle == null 
//				|| markerEndPoint == null
				) {
			zeroMarkers();
			return;
		}
//		Point2D startPoint = getChartPointForCoordinates(markerStartPoint.getX(), markerStartPoint.getY());
//		Point2D endPoint = getChartPointForCoordinates(markerEndPoint.getX(), markerEndPoint.getY());
		
		Point2D startPoint = getChartPointForCoordinates(markerRectangle.getMinX(), markerRectangle.getMinY());
		Point2D endPoint = getChartPointForCoordinates(markerRectangle.getMaxX(), markerRectangle.getMaxY());
		
		if(startPoint == null || endPoint == null) {
			zeroMarkers();
			return;
		}
//		domainMarker.setStartValue(Math.min(startPoint.getX(), endPoint.getX()));
//		domainMarker.setEndValue(Math.max(startPoint.getX(), endPoint.getX()));
//		rangeMarker.setStartValue(Math.min(startPoint.getY(), endPoint.getY()));
//		rangeMarker.setEndValue(Math.max(startPoint.getY(), endPoint.getY()));
		
		domainMarker.setStartValue(Math.min(startPoint.getX(), endPoint.getX()));
		domainMarker.setEndValue(Math.max(startPoint.getX(), endPoint.getX()));
		rangeMarker.setStartValue(Math.min(startPoint.getY(), endPoint.getY()));
		rangeMarker.setEndValue(Math.max(startPoint.getY(), endPoint.getY()));
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
		
		if (MouseEvent.getMouseModifiersText(e.getModifiers()).equals("Shift+Button1")) {

			markerStartPoint = null;
//			markerEndPoint = null;
			markerRectangle = null;
			zeroMarkers();
			repaint();
		} 
		else {
			super.mouseClicked(e);
		}
	}
	
	private void zeroMarkers() {
		
		domainMarker.setStartValue(0.0d);
		domainMarker.setEndValue(0.0d);
		rangeMarker.setStartValue(0.0d);
		rangeMarker.setEndValue(0.0d);
	}
	
    private void drawMarkerRectangle(Graphics2D g2, boolean xor) {
    	
        if (markerRectangle != null) {
            if (xor) {
                 // Set XOR mode to draw the zoom rectangle
                g2.setXORMode(Color.LIGHT_GRAY);
            }
            g2.setPaint(markerColor);
            g2.draw(this.markerRectangle);
            if (xor) {
                // Reset to the default 'overwrite' mode
                g2.setPaintMode();
            }
        }
    }
    
	@Override
	public void paint(Graphics g) {
		
		super.paint(g);
		Graphics2D g2 = (Graphics2D) g;
		if (markerRectangle != null) {

			updateMarkerRectangleFromIntervalMarkers();
			g2.setColor(markerOutlineColor);
			g2.draw(markerRectangle);
			g2.setColor(markerColor);
			g2.fill(markerRectangle);
		}
	}
	
	private void updateMarkerRectangleFromIntervalMarkers() {
		
		if(domainMarker.getEndValue() > 0 && rangeMarker.getEndValue() > 0) {
			
	        Rectangle2D dataArea = getChartRenderingInfo().getPlotInfo().getDataArea();
	        double xMin = plot.getDomainAxis().valueToJava2D(
	        	domainMarker.getStartValue(), dataArea, plot.getDomainAxisEdge());
	        double yMax = plot.getRangeAxis().valueToJava2D(
	        	rangeMarker.getStartValue(), dataArea, plot.getRangeAxisEdge());
	        double xMax = plot.getDomainAxis().valueToJava2D(
		        	domainMarker.getEndValue(), dataArea, plot.getDomainAxisEdge());
	        double yMin = plot.getRangeAxis().valueToJava2D(
	        	rangeMarker.getEndValue(), dataArea, plot.getRangeAxisEdge());		
	
            markerRectangle = new Rectangle2D.Double(
            		xMin, yMin,
            		xMax - xMin, 
                    yMax - yMin);
		}		
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
		plot.setRangePannable(true);
		
		domainMarker = new IntervalMarker(0.0d, 0.0d);
		domainMarker.setPaint(invizibleMarkerColor);
		domainMarker.setOutlineStroke(null);
		plot.addDomainMarker(domainMarker, Layer.BACKGROUND);
		rangeMarker = new IntervalMarker(0.0d, 0.0d);
		rangeMarker.setPaint(invizibleMarkerColor);
		rangeMarker.setOutlineStroke(null);
		plot.addDomainMarker(rangeMarker, Layer.BACKGROUND);
		
		xMarker = new ValueMarker(0.0d);
		xMarker.setPaint(Color.darkGray);
		plot.addDomainMarker(xMarker,Layer.FOREGROUND);			
		yMarker = new ValueMarker(0.0d);
		yMarker.setPaint(Color.darkGray);
		plot.addRangeMarker(yMarker,Layer.FOREGROUND);
	}

	@Override
	public void removeAllDataSets() {

		for (int i = 0; i < plot.getDatasetCount(); i++)
			plot.setDataset(i, null);

		numberOfDataSets = 0;
		markerRectangle = null;
	}

	public DataExplorerPlotType getPlotType() {
		return plotType;
	}
	
	public Range getSelectedRtRange() {
		
		Range rtRange = null;
		if(domainMarker != null && domainMarker.getEndValue() > 0.0d)
			rtRange = new Range(domainMarker.getStartValue(), domainMarker.getEndValue());
		
		return rtRange;
	}	
	
	public Range getSelectedMzRange() {
		
		Range mzRange = null;
		if(rangeMarker != null && rangeMarker.getEndValue() > 0.0d)
			mzRange = new Range(rangeMarker.getStartValue(), rangeMarker.getEndValue());
		
		return mzRange;
	}

	public void setFeaturePlotPopupMenu(JPopupMenu popupMenu) {
		this.plotPopupMenu = popupMenu;
	}
}














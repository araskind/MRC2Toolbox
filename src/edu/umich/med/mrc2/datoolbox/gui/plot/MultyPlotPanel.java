package edu.umich.med.mrc2.datoolbox.gui.plot;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.IntervalMarker;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.renderer.xy.XYSplineRenderer;
import org.jfree.chart.ui.Layer;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.data.Range;

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.gui.plot.renderer.DefaultSplineRenderer;
import edu.umich.med.mrc2.datoolbox.gui.plot.renderer.FilledChromatogramRenderer;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;

public class MultyPlotPanel extends MasterPlotPanel {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4200598991774229644L;
	
	private CombinedDomainXYPlot xicPlot;
	private NumberAxis xAxis, yAxis;
	private FilledChromatogramRenderer filledChromatogramRenderer;
	private FilledChromatogramRenderer linesChromatogramRenderer;
	private DefaultSplineRenderer splineRenderer;
	private Map<DataFile, XYPlot>filePlotMap;
	
	private final Paint markerColor = new Color(150, 150, 150);
	private Range defaultRtRange;
	private double rtWindowExtensionWidth;
	private double markerStart, markerEnd;
	private Point2D markerStartPoint;
	private int subPlotHeight;

	public MultyPlotPanel() {
		
		super();
		initChart();
		initAxes();
		initTitles();
		initLegend(RectangleEdge.RIGHT, legendVisible);
		initRenderer();
		setMouseWheelEnabled(false);
		filePlotMap = new TreeMap<DataFile, XYPlot>();
	}
	
	public void actionPerformed(ActionEvent event) {

		String command = event.getActionCommand();

		if (command.equals(ChartPanel.ZOOM_IN_DOMAIN_COMMAND)) {
			zoomInDomain();
			return;
		}
		if (command.equals(ChartPanel.ZOOM_OUT_DOMAIN_COMMAND)) {
			zoomOutDomain();
			return;
		}
		if (command.equals(ChartPanel.ZOOM_IN_RANGE_COMMAND)) {			
			zoomInRange();
			return;
		}
		if (command.equals(ChartPanel.ZOOM_OUT_RANGE_COMMAND)) {
			zoomOutRange();
			return;
		}
		if (command.equals(ChartPanel.ZOOM_IN_BOTH_COMMAND)) {
			zoomInDomain();
			zoomInRange();
			return;
		}
		if (command.equals(ChartPanel.ZOOM_OUT_BOTH_COMMAND)) {
			zoomOutDomain();
			zoomOutRange();	
			return;
		}
		if (command.equals(ChartPanel.ZOOM_RESET_DOMAIN_COMMAND)) {
			resetDomain();
			return;
		}
		if (command.equals(ChartPanel.ZOOM_RESET_RANGE_COMMAND)) {
			resetRange();
			return;
		}		
		if (command.equals(ChartPanel.ZOOM_RESET_BOTH_COMMAND)) {
			resetDomain();
			resetRange();
			return;
		}
		if (command.equals(MasterPlotPanel.TOGGLE_DATA_POINTS_COMMAND)) {
			toggleDataPoints();
			return;
		}
		super.actionPerformed(event);
	}
	
	public Map<DataFile, Range>getSelectedRtRanges() {
		
		Map<DataFile, Range>filerangeMap = new TreeMap<DataFile, Range>();
		for(Entry<DataFile, XYPlot> entry : filePlotMap.entrySet()) {
			
			@SuppressWarnings("unchecked")
			Collection<Marker> markers = 
				entry.getValue().getDomainMarkers(Layer.FOREGROUND);
			if(markers != null && !markers.isEmpty()) {				
				Object marker = markers.iterator().next();
				if(marker instanceof IntervalMarker) {
					
					Range markerRange = new Range(
							((IntervalMarker)marker).getStartValue(), 
							((IntervalMarker)marker).getEndValue());
					if(markerRange.getLength() > 0.0d)
						filerangeMap.put(entry.getKey(), markerRange);
				}
			}
		}
		return filerangeMap;		
	}
	
	public void clearMarkers() {
		
		for(Entry<DataFile, XYPlot> entry : filePlotMap.entrySet())		
			entry.getValue().clearDomainMarkers();
	}
		
	private void zoomInDomain() {
		
		for(XYPlot fPlot : filePlotMap.values())
			fPlot.getDomainAxis().resizeRange(1 / ZOOM_FACTOR);		
	}
	
	private void zoomOutDomain() {
		
		for(XYPlot fPlot : filePlotMap.values())
			fPlot.getDomainAxis().resizeRange(ZOOM_FACTOR);		
	}
	
	private void zoomInRange() {
		
		for(XYPlot fPlot : filePlotMap.values())
			fPlot.getRangeAxis().setRange(
					0.0d, 
					fPlot.getRangeAxis().getRange().getLength() / ZOOM_FACTOR);	
	}
	
	private void zoomOutRange() {
		
		for(XYPlot fPlot : filePlotMap.values())
			fPlot.getRangeAxis().setRange(
					0.0d, 
					fPlot.getRangeAxis().getRange().getLength() * ZOOM_FACTOR);
	}
	
	private void resetRange() {
		
		for(XYPlot fPlot : filePlotMap.values())
			fPlot.getRangeAxis().setAutoRange(true);
	}
	
	private void resetDomain() {
		
		if(defaultRtRange != null)
			xicPlot.getDomainAxis().setRange(defaultRtRange);
		else
			xicPlot.getDomainAxis().setAutoRange(true);
	}
	
	public void toggleDataPoints() {

		dataPointsVisible = !dataPointsVisible;
		for(XYPlot fPlot : filePlotMap.values()) {
			
			final int count = fPlot.getDatasetCount();
			for (int i = 0; i < count; i++) {
				
				XYItemRenderer fRenderer = fPlot.getRenderer(i);
				if (fRenderer instanceof XYLineAndShapeRenderer) 
					((XYLineAndShapeRenderer)fRenderer).setDefaultShapesVisible(dataPointsVisible);
	
				if(fRenderer instanceof FilledChromatogramRenderer)
					((FilledChromatogramRenderer)fRenderer).setPlotShapes(dataPointsVisible);
			}
		}
		toolbar.toggleDataPointsIcon(dataPointsVisible);
	}
	
	@Override
	protected void initChart() {
		// TODO Auto-generated method stub
		chart = new JFreeChart(new CombinedDomainXYPlot());
		xicPlot = (CombinedDomainXYPlot) chart.getPlot();
		xicPlot.setDomainPannable(true);
		chart.setBackgroundPaint(Color.white);
		setChart(chart);
	}
	
	@Override
	protected void initAxes() {

		yAxis = new NumberAxis();
		yAxis.setNumberFormatOverride(MRC2ToolBoxConfiguration.getIntensityFormat());
		yAxis.setUpperMargin(0.1);
		xicPlot.setRangeAxis(yAxis);
		
		xAxis = new NumberAxis();
		xAxis.setNumberFormatOverride(MRC2ToolBoxConfiguration.getRtFormat());
		xicPlot.setDomainAxis(xAxis);
		xicPlot.getDomainAxis().setLabel("RetentionTime");
	}

	@Override
	protected void initPlot() {
		// TODO Auto-generated method stub

	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void removeAllDataSets() {

		if(xicPlot == null)
			return;

		List subplots = new ArrayList(xicPlot.getSubplots());
		for(Object spl : subplots)
			xicPlot.remove((XYPlot) spl);
		
		filePlotMap.clear();
	}
	
	private XYPlot getNewXicPlot() {

		XYPlot newPlot = (XYPlot) ChartFactory.createScatterPlot(
				"", // title
				"", // x-axis label
				"Intensity", // y-axis label
				null, // data set
				PlotOrientation.VERTICAL, // orientation
				legendVisible, // create legend?
				true, // generate tooltips?
				false // generate URLs?
		).getPlot();
		setBasicPlotGui(newPlot);
		return newPlot;
	}
	
	private void setBasicPlotGui(Plot newPlot) {

		newPlot.setBackgroundPaint(Color.white);
		XYPlot xyPlot = (XYPlot)newPlot;
		xyPlot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
		xyPlot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
		xyPlot.setDomainGridlinePaint(GRID_COLOR);
		xyPlot.setRangeGridlinePaint(GRID_COLOR);
		xyPlot.setDomainCrosshairVisible(false);
		xyPlot.setRangeCrosshairVisible(false);
		xyPlot.setDomainPannable(true);
		xyPlot.setRangePannable(false);
	}
	
	private void initRenderer() {
		
		filledChromatogramRenderer = new FilledChromatogramRenderer();
		filledChromatogramRenderer.setPlotShapes(dataPointsVisible);
		filledChromatogramRenderer.setDefaultShape(
				FilledChromatogramRenderer.dataPointsShape);
		
		linesChromatogramRenderer = new FilledChromatogramRenderer(
				FilledChromatogramRenderer.SHAPES_AND_LINES);
		linesChromatogramRenderer.setPlotShapes(dataPointsVisible);
		linesChromatogramRenderer.setDefaultShape(
				FilledChromatogramRenderer.dataPointsShape);
		
		splineRenderer = new DefaultSplineRenderer();
		((XYSplineRenderer)splineRenderer).setDefaultShapesVisible(dataPointsVisible);
		splineRenderer.setDefaultShape(DefaultSplineRenderer.dataPointsShape);
	}

	public double getRtWindowExtensionWidth() {
		return rtWindowExtensionWidth;
	}

	public void setRtWindowExtensionWidth(double rtWindowExtensionWidth) {
		this.rtWindowExtensionWidth = rtWindowExtensionWidth;
	}
	
	@Override
	public void mouseDragged(MouseEvent e) {

		if (MouseEvent.getMouseModifiersText(e.getModifiersEx()).equals("Shift+Button1") 
				&& this.getMousePosition() != null && markerStartPoint != null) {
	
			Rectangle2D scaledDataArea = getScreenDataArea((int) this.markerStartPoint.getX(),
			(int) this.markerStartPoint.getY());
			double xmax = Math.min(e.getX(), scaledDataArea.getMaxX());
			Rectangle2D markerRectangle = new Rectangle2D.Double(this.markerStartPoint.getX(), scaledDataArea.getMinY(),
					xmax - this.markerStartPoint.getX(), scaledDataArea.getHeight());
		
			Graphics2D g2 = (Graphics2D) getGraphics();
			drawMarkerRectangle(markerRectangle, g2, true);
			g2.dispose();			
		} 
		else {
			super.mouseDragged(e);
		}
	}
	
	@Override
	public void mousePressed(MouseEvent e) {

		if (MouseEvent.getMouseModifiersText(e.getModifiersEx()).equals("Shift+Button1")
				&& this.getMousePosition() != null) {

			XYPlot activePlot = xicPlot.findSubplot(this.getChartRenderingInfo().getPlotInfo(), this.getMousePosition());
			markerStart = getPosition(e, activePlot);
			Rectangle2D screenDataArea = getScreenDataArea(e.getX(), e.getY());

			if (screenDataArea != null)
				markerStartPoint = getMarkerStartPoint(e.getX(), e.getY(), screenDataArea);

		} else {
			super.mousePressed(e);
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {

		if (MouseEvent.getMouseModifiersText(e.getModifiersEx()).equals("Shift+Button1") 
				&& this.getMousePosition() != null) {

			XYPlot activePlot = xicPlot.findSubplot(this.getChartRenderingInfo().getPlotInfo(), this.getMousePosition());
			markerEnd = getPosition(e, activePlot);
			updateMarker(activePlot);
			markerStartPoint = null;
			repaint();
		} else {
			super.mouseReleased(e);
		}
	}
	
	private void updateMarker(XYPlot activePlot) {

		activePlot.clearDomainMarkers();	
		if (markerStart > 0.0d && markerEnd > 0.0d && markerEnd > markerStart) {

			IntervalMarker marker = new IntervalMarker(markerStart, markerEnd);
			marker.setPaint(markerColor);
			marker.setAlpha(0.5f);
			activePlot.addDomainMarker(marker, Layer.FOREGROUND);		
		}
	}
	
	private Point2D getMarkerStartPoint(int x, int y, Rectangle2D area) {
		double xx = Math.max(area.getMinX(), Math.min(x, area.getMaxX()));
		double yy = Math.max(area.getMinY(), Math.min(y, area.getMaxY()));
		return new Point2D.Double(xx, yy);
	}

	private double getPosition(MouseEvent e, XYPlot plot ) {
		Point2D p = translateScreenToJava2D(e.getPoint());
		Rectangle2D plotArea = getScreenDataArea();
		return plot.getDomainAxis().java2DToValue(p.getX(), plotArea, plot.getDomainAxisEdge());
	}
	
	private void drawMarkerRectangle(Rectangle2D markerRectangle, Graphics2D g2, boolean xor) {

		if (markerRectangle != null) {
			if (xor)
				g2.setXORMode(Color.gray);

			g2.setPaint(markerColor);
			g2.fill(markerRectangle);

			if (xor)
				g2.setPaintMode();
		}
	}

	public int getSubPlotHeight() {
		return subPlotHeight;
	}

	public void setSubPlotHeight(int newSubPlotHeight) {
		
		boolean update = subPlotHeight != newSubPlotHeight;
		this.subPlotHeight = newSubPlotHeight;
//		if(update && !fileFeatureMap.isEmpty()) {
		
//		}
	}
}























package edu.umich.med.mrc2.datoolbox.gui.rawdata.manint;

import java.awt.Color;
import java.awt.Dimension;
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
import java.util.stream.Collectors;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYTitleAnnotation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.XYToolTipGenerator;
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
import org.jfree.chart.title.TextTitle;
import org.jfree.chart.title.Title;
import org.jfree.chart.ui.Layer;
import org.jfree.chart.ui.RectangleAnchor;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.data.Range;
import org.jfree.data.xy.XYSeriesCollection;

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.ExtractedMsFeatureChromatogram;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.gui.plot.MasterPlotPanel;
import edu.umich.med.mrc2.datoolbox.gui.plot.renderer.DefaultSplineRenderer;
import edu.umich.med.mrc2.datoolbox.gui.plot.renderer.FilledChromatogramRenderer;
import edu.umich.med.mrc2.datoolbox.gui.plot.tooltip.ChromatogramToolTipGenerator;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;

public class PeakIntegrationPlotPanel extends MasterPlotPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5498283917213265924L;
	
	private CombinedDomainXYPlot xicPlot;
	private NumberAxis xAxis, yAxis;
	private FilledChromatogramRenderer filledChromatogramRenderer;
	private FilledChromatogramRenderer linesChromatogramRenderer;
	private DefaultSplineRenderer splineRenderer;
	private Map<DataFile,MsFeature>fileFeatureMap;
	private Map<DataFile, XYPlot>filePlotMap;
	private final XYToolTipGenerator toolTipGenerator = new ChromatogramToolTipGenerator();
	private final Paint c12Color = new Color(0.0f, 0.0f, Color.BLUE.getBlue()/255.0f, 0.2f);
	private final Paint c13Color = new Color(Color.RED.getRed()/255.0f, 0.0f, 0.0f, 0.2f);
	private final Paint markerColor = new Color(150, 150, 150);
	private Range defaultRtRange;
	private double rtWindowExtensionWidth;
	private double markerStart, markerEnd;
	private Point2D markerStartPoint;
	private int subPlotHeight;

	public PeakIntegrationPlotPanel() {
		
		super();
		initChart();
		initAxes();
		initTitles();
		initLegend(RectangleEdge.RIGHT, legendVisible);
		initRenderer();
		setMouseWheelEnabled(false);
		fileFeatureMap = new TreeMap<DataFile,MsFeature>();
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
			
			Collection<Marker> markers = entry.getValue().getDomainMarkers(Layer.FOREGROUND);
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

	@Override
	public synchronized void removeAllDataSets() {

		if(xicPlot != null) {

			List subplots = new ArrayList(xicPlot.getSubplots());
			for(Object spl : subplots)
				xicPlot.remove((XYPlot) spl);
		}
		fileFeatureMap.clear();
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
	
	public void showfeatureChromatograms(Collection<MsFeature>features) {
		
		removeAllDataSets();
//		chart.setNotify(false);
		edu.umich.med.mrc2.datoolbox.utils.Range featureRtRange = null;
		Dimension currentSize = getPreferredSize();
		int newHeight = subPlotHeight * features.size();
		setPreferredSize(new Dimension(currentSize.width, newHeight));
		
		for(MsFeature f : features) {
			
//			if(featureRtRange == null)
//				featureRtRange = f.getRtRange();
//			else
//				featureRtRange.extendRange(f.getRtRange());
//			
//			XYPlot fPlot = getNewXicPlot();
//			setBasicPlotGui(fPlot);
//			fileFeatureMap.put(f.getDataFile(), f);
//			filePlotMap.put(f.getDataFile(), fPlot);
//			
//			//	TODO add data set and set renderer
//			addFeatureXics(fPlot, f);
//			addAnnotationForFeature(fPlot, f);
//			try {
//				xicPlot.add(fPlot);
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				//e.printStackTrace();
//			}
		}
		double newMin = featureRtRange.getMin() - rtWindowExtensionWidth;
		if(newMin < 0.0d)
			newMin = 0.0d;
		
		double newMax = featureRtRange.getMax() + rtWindowExtensionWidth;	
		defaultRtRange = new Range(newMin, newMax);
		xicPlot.getDomainAxis().setRange(defaultRtRange);	
//		chart.fireChartChanged();
		revalidate();
		repaint();
	}
	
	public void updateReintegratedChromatograms(
			Collection<ExtractedMsFeatureChromatogram> reintegratedChromatograms) {
		
		ArrayList<MsFeature>fList = new ArrayList<MsFeature>();
		fList.addAll(fileFeatureMap.values());
		removeAllDataSets();
//		chart.setNotify(false);
		edu.umich.med.mrc2.datoolbox.utils.Range featureRtRange = null;
		List<MsFeature> toUpdate = reintegratedChromatograms.stream().
				map(c -> c.getParentFeature()).collect(Collectors.toList());
		
		for(MsFeature f : fList) {
			
			if(featureRtRange == null)
				featureRtRange = f.getRtRange();
			else
				featureRtRange.extendRange(f.getRtRange());
			
			XYPlot fPlot = getNewXicPlot();
			setBasicPlotGui(fPlot);
//			fileFeatureMap.put(f.getDataFile(), f);
//			filePlotMap.put(f.getDataFile(), fPlot);
			if(toUpdate.contains(f)) {
				
				MsFeature parentFeature = f;
				ExtractedMsFeatureChromatogram chrom = reintegratedChromatograms.stream().
						filter(c -> c.getParentFeature().equals(parentFeature)).findFirst().orElse(null);
				
				if(chrom != null) {
					addChromatogram(fPlot, chrom);
					addAnnotationForChromatogram(fPlot, chrom);
					try {
						xicPlot.add(fPlot);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						//e.printStackTrace();
					}
				}
			}
			else {
				addFeatureXics(fPlot, f);
				addAnnotationForFeature(fPlot, f);
				try {
					xicPlot.add(fPlot);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
				}
			}
		}
		double newMin = featureRtRange.getMin() - rtWindowExtensionWidth;
		if(newMin < 0.0d)
			newMin = 0.0d;
		
		double newMax = featureRtRange.getMax() + rtWindowExtensionWidth;	
		defaultRtRange = new Range(newMin, newMax);
		xicPlot.getDomainAxis().setRange(defaultRtRange);
//		chart.fireChartChanged();
		revalidate();
		repaint();
	}
	
	private void addAnnotationForChromatogram(XYPlot plot, ExtractedMsFeatureChromatogram chrom) {
		
		TextTitle title = new TextTitle(chrom.getParentFeature().toString(), 
			TextTitle.DEFAULT_FONT, Color.RED,
		      Title.DEFAULT_POSITION, Title.DEFAULT_HORIZONTAL_ALIGNMENT,
		      Title.DEFAULT_VERTICAL_ALIGNMENT, Title.DEFAULT_PADDING);

		title.setPadding(2, 5, 0, 0);
		XYTitleAnnotation ta = new XYTitleAnnotation(0.0, 1.0, title, RectangleAnchor.TOP_LEFT);
		plot.addAnnotation(ta);
	}
	
	private void addChromatogram(XYPlot plot, ExtractedMsFeatureChromatogram chrom) {
		
		XYSeriesCollection c12Collection = new XYSeriesCollection();
		DefaultSplineRenderer  c12Renderer = new DefaultSplineRenderer();
		((XYSplineRenderer)c12Renderer).setDefaultShapesVisible(dataPointsVisible);
		c12Renderer.setDefaultToolTipGenerator(toolTipGenerator);
		c12Renderer.setAutoPopulateSeriesPaint(false);
		
		XYSeriesCollection c13Collection = new XYSeriesCollection();
		DefaultSplineRenderer  c13Renderer = new DefaultSplineRenderer();
		((XYSplineRenderer)c13Renderer).setDefaultShapesVisible(dataPointsVisible);		
		c13Renderer.setDefaultToolTipGenerator(toolTipGenerator);
		c13Renderer.setAutoPopulateSeriesPaint(false);
			
		plot.setRenderer(0, c12Renderer);
		plot.setDataset(0, c12Collection);		
		plot.setRenderer(1, c13Renderer);
		plot.setDataset(1, c13Collection);
		
		int seriesCount = 0;
		MsFeature f = chrom.getParentFeature();	
//		Color fileColor = f.getDataFile().getColor();
//		double border = f.getC12C13border();
//		for (MsPoint p : f.getIsotopicPattern()) {
//
//			XYSeries mzseries = new XYSeries(
//					f.getDataFile().getFileName() + " | " + MRC2ToolBoxConfiguration.getMzFormat().format(p.getMz()));
//			chrom.getXicForMass(p.getMz()).forEach((rt, area) -> mzseries.add(rt, area));
//			if (p.getMz() <= border) {
//
//				// TODO change name if necessary to avoid collision
//				c12Collection.addSeries(mzseries);
//				seriesCount = c12Collection.getSeriesCount() - 1;
//				c12Renderer.setSeriesOutlinePaint(seriesCount, fileColor);
//				c12Renderer.setSeriesFillPaint(seriesCount, c12Color);
//				c12Renderer.setSeriesPaint(seriesCount, c12Color);
//				c12Renderer.setSeriesShape(seriesCount, FilledChromatogramRenderer.dataPointsShape);
//			} else {
//				c13Collection.addSeries(mzseries);
//				seriesCount = c13Collection.getSeriesCount() - 1;
//				c13Renderer.setSeriesOutlinePaint(seriesCount, fileColor);
//				c13Renderer.setSeriesPaint(seriesCount, c13Color);
//				c13Renderer.setSeriesFillPaint(seriesCount, c13Color);
//				c13Renderer.setSeriesShape(seriesCount, FilledChromatogramRenderer.dataPointsShape);
//			}
//		}
	}
	
	private void addFeatureXics(XYPlot plot, MsFeature f) {
		
		XYSeriesCollection c12Collection = new XYSeriesCollection();
		DefaultSplineRenderer  c12Renderer = new DefaultSplineRenderer();
		((XYSplineRenderer)c12Renderer).setDefaultShapesVisible(dataPointsVisible);
		c12Renderer.setDefaultToolTipGenerator(toolTipGenerator);
		c12Renderer.setAutoPopulateSeriesPaint(false);
		
		XYSeriesCollection c13Collection = new XYSeriesCollection();
		DefaultSplineRenderer  c13Renderer = new DefaultSplineRenderer();
		((XYSplineRenderer)c13Renderer).setDefaultShapesVisible(dataPointsVisible);		
		c13Renderer.setDefaultToolTipGenerator(toolTipGenerator);
		c13Renderer.setAutoPopulateSeriesPaint(false);
			
		plot.setRenderer(0, c12Renderer);
		plot.setDataset(0, c12Collection);		
		plot.setRenderer(1, c13Renderer);
		plot.setDataset(1, c13Collection);
		
		int seriesCount = 0;
			
//		Color fileColor = f.getDataFile().getColor();
//		double border = f.getC12C13border();
//		for (MsPoint p : f.getIsotopicPattern()) {
//
//			XYSeries mzseries = new XYSeries(
//					f.getDataFile().getFileName() + " | " + MRC2ToolBoxConfiguration.getMzFormat().format(p.getMz()));
//			f.getXicForMass(p.getMz()).forEach((rt, area) -> mzseries.add(rt, area));
//			if (p.getMz() <= border) {
//
//				// TODO change name if necessary to avoid collision
//				c12Collection.addSeries(mzseries);
//				seriesCount = c12Collection.getSeriesCount() - 1;
//				c12Renderer.setSeriesOutlinePaint(seriesCount, fileColor);
//				c12Renderer.setSeriesFillPaint(seriesCount, c12Color);
//				c12Renderer.setSeriesPaint(seriesCount, c12Color);
//				c12Renderer.setSeriesShape(seriesCount, FilledChromatogramRenderer.dataPointsShape);
//			} else {
//				c13Collection.addSeries(mzseries);
//				seriesCount = c13Collection.getSeriesCount() - 1;
//				c13Renderer.setSeriesOutlinePaint(seriesCount, fileColor);
//				c13Renderer.setSeriesPaint(seriesCount, c13Color);
//				c13Renderer.setSeriesFillPaint(seriesCount, c13Color);
//				c13Renderer.setSeriesShape(seriesCount, FilledChromatogramRenderer.dataPointsShape);
//			}
//		}
	}
	
	private void addAnnotationForFeature(XYPlot plot, MsFeature feature) {
		
		TextTitle title = new TextTitle(feature.toString());
//		title.setPaint(color);
		title.setPadding(2, 5, 0, 0);
		XYTitleAnnotation ta = new XYTitleAnnotation(0.0, 1.0, title, RectangleAnchor.TOP_LEFT);
		plot.addAnnotation(ta);
	}

	public double getRtWindowExtensionWidth() {
		return rtWindowExtensionWidth;
	}

	public void setRtWindowExtensionWidth(double rtWindowExtensionWidth) {
		this.rtWindowExtensionWidth = rtWindowExtensionWidth;
	}
	
	@Override
	public void mouseDragged(MouseEvent e) {

		if (MouseEvent.getMouseModifiersText(e.getModifiers()).equals("Shift+Button1") 
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

		if (MouseEvent.getMouseModifiersText(e.getModifiers()).equals("Shift+Button1")
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

		if (MouseEvent.getMouseModifiersText(e.getModifiers()).equals("Shift+Button1") 
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
		if(update && !fileFeatureMap.isEmpty()) {
			ArrayList<MsFeature>fList = new ArrayList<MsFeature>();
			fList.addAll(fileFeatureMap.values());
			showfeatureChromatograms(fList);			
		}
	}

	public Map<DataFile, MsFeature> getFileFeatureMap() {
		return fileFeatureMap;
	}
}













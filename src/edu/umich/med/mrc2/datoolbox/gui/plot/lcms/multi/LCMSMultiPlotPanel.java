package edu.umich.med.mrc2.datoolbox.gui.plot.lcms.multi;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
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
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.IntervalMarker;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.ui.Layer;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.data.Range;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import edu.umich.med.mrc2.datoolbox.data.MSFeatureInfoBundle;
import edu.umich.med.mrc2.datoolbox.data.MsFeaturePair;
import edu.umich.med.mrc2.datoolbox.data.MsPoint;
import edu.umich.med.mrc2.datoolbox.data.enums.MsDepth;
import edu.umich.med.mrc2.datoolbox.data.msclust.IMsFeatureInfoBundleCluster;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.plot.PlotType;
import edu.umich.med.mrc2.datoolbox.gui.plot.dataset.HeadToTailMsDataSet;
import edu.umich.med.mrc2.datoolbox.gui.plot.dataset.MsDataSet;
import edu.umich.med.mrc2.datoolbox.gui.plot.lcms.LCMSPlotPanel;
import edu.umich.med.mrc2.datoolbox.gui.plot.lcms.spectrum.MSReferenceDisplayType;
import edu.umich.med.mrc2.datoolbox.gui.plot.lcms.spectrum.MsReferenceType;
import edu.umich.med.mrc2.datoolbox.gui.plot.renderer.DefaultSplineRenderer;
import edu.umich.med.mrc2.datoolbox.gui.plot.renderer.FilledChromatogramRenderer;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;

public class LCMSMultiPlotPanel extends LCMSPlotPanel {

	
	private static final long serialVersionUID = 1L;
	protected int subPlotHeight;
		
	protected FilledChromatogramRenderer filledChromatogramRenderer;
	protected FilledChromatogramRenderer linesChromatogramRenderer;
	protected DefaultSplineRenderer splineRenderer;
	protected Map<Comparable, XYPlot>objectPlotMap;	
	protected Range defaultRtRange;
	protected double rtWindowExtensionWidth;

	public LCMSMultiPlotPanel(PlotType plotType) {
		
		super(plotType);
		
		dataPointsVisible = false;
		annotationsVisible = true;
		
		initChart();
		initAxes();
		initTitles();
		initLegend(RectangleEdge.BOTTOM, legendVisible);
		initRendererForPlotType();
		setMouseWheelEnabled(true);	// ?
		objectPlotMap = new TreeMap<Comparable, XYPlot>();
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
		if (command.equals(MainActionCommands.SHOW_PLOT_DATA_POINTS_COMMAND.getName())) {
			toggleDataPoints(true);
			return;
		}
		if (command.equals(MainActionCommands.HIDE_PLOT_DATA_POINTS_COMMAND.getName())) {
			toggleDataPoints(false);
			return;
		}
		super.actionPerformed(event);
	}
	
	public Map<Comparable, Range>getSelectedRtRanges() {
		
		Map<Comparable, Range>dataRangeMap = new TreeMap<Comparable, Range>();
		for(Entry<Comparable, XYPlot> entry : objectPlotMap.entrySet()) {
			
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
						dataRangeMap.put(entry.getKey(), markerRange);
				}
			}
		}
		return dataRangeMap;		
	}
	
	public void clearMarkers() {
		
		for(XYPlot fPlot : objectPlotMap.values())		
			fPlot.clearDomainMarkers();
	}
		
	protected void zoomInDomain() {
		
		for(XYPlot fPlot : objectPlotMap.values())
			fPlot.getDomainAxis().resizeRange(1 / ZOOM_FACTOR);		
	}
	
	protected void zoomOutDomain() {
		
		for(XYPlot fPlot : objectPlotMap.values())
			fPlot.getDomainAxis().resizeRange(ZOOM_FACTOR);		
	}
	
	protected void zoomInRange() {
		
		for(XYPlot fPlot : objectPlotMap.values())
			fPlot.getRangeAxis().setRange(
					0.0d, 
					fPlot.getRangeAxis().getRange().getLength() / ZOOM_FACTOR);	
	}
	
	protected void zoomOutRange() {
		
		for(XYPlot fPlot : objectPlotMap.values())
			fPlot.getRangeAxis().setRange(
					0.0d, 
					fPlot.getRangeAxis().getRange().getLength() * ZOOM_FACTOR);
	}
	
	protected void resetRange() {
		
		for(XYPlot fPlot : objectPlotMap.values())
			fPlot.getRangeAxis().setAutoRange(true);
	}
	
	protected void resetDomain() {
		
		if(defaultRtRange != null)
			plot.getDomainAxis().setRange(defaultRtRange);
		else
			plot.getDomainAxis().setAutoRange(true);
	}
	
	@Override
	public void toggleDataPoints(boolean show) {

		dataPointsVisible = show;
		for(XYPlot fPlot : objectPlotMap.values()) {
			
			final int count = fPlot.getDatasetCount();
			for (int i = 0; i < count; i++) {
				
				if (plot.getRenderer(i) instanceof XYLineAndShapeRenderer) {
					final XYLineAndShapeRenderer renderer = 
							(XYLineAndShapeRenderer) plot.getRenderer(i);
					renderer.setDefaultShapesVisible(dataPointsVisible);
				}
				if(plot.getRenderer(i) instanceof FilledChromatogramRenderer) {
					final FilledChromatogramRenderer renderer = 
							(FilledChromatogramRenderer) plot.getRenderer(i);
					renderer.setPlotShapes(dataPointsVisible);
				}			
			}
		}
	}
	
	@Override
	protected void initChart() {

		chart = new JFreeChart(new CombinedDomainXYPlot());
		plot = (CombinedDomainXYPlot) chart.getPlot();
		plot.setDomainPannable(true);
		chart.setBackgroundPaint(Color.white);
		setChart(chart);
	}

	@Override
	protected void initPlot() {
		// TODO Auto-generated method stub

	}
	
	@Override
	protected void initAxes() {
		
		yAxis = new NumberAxis();
		plot.setRangeAxis(yAxis);
		
		xAxis = new NumberAxis();
		plot.setDomainAxis(xAxis);

		if (plotType.equals(PlotType.CHROMATOGRAM)) {

			plot.getDomainAxis().setLabel("Retention");
			xAxis.setNumberFormatOverride(MRC2ToolBoxConfiguration.getRtFormat());
			annotationsVisible = false;
		}
		if (plotType.equals(PlotType.SPECTRUM)) {

			plot.getDomainAxis().setLabel("M/Z");
			xAxis.setNumberFormatOverride(MRC2ToolBoxConfiguration.getMzFormat());
			annotationsVisible = true;
		}
		xAxis.setUpperMargin(0.02);
		xAxis.setLowerMargin(0.001);
		xAxis.setTickLabelInsets(new RectangleInsets(0, 0, 20, 20));

		yAxis = (NumberAxis) plot.getRangeAxis();
		yAxis.setNumberFormatOverride(MRC2ToolBoxConfiguration.getIntensityFormat());
		yAxis.setUpperMargin(0.15);
		//yAxis.setLowerMargin(0.1);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void removeAllDataSets() {

		if(plot == null)
			return;

		List subplots = new ArrayList(((CombinedDomainXYPlot)plot).getSubplots());
		for(Object spl : subplots)
			((CombinedDomainXYPlot)plot).remove((XYPlot) spl);
		
		objectPlotMap.clear();
		clearMarkers();
	}
	
	protected XYPlot getNewXYPlot() {

		XYPlot newPlot = (XYPlot) ChartFactory.createXYLineChart(
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
	
	protected void setBasicPlotGui(Plot newPlot) {

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

	public void showMSFeatureCluster(
			IMsFeatureInfoBundleCluster cluster, 
			MsDepth msLevel, 
			MsReferenceType refType,
			MSReferenceDisplayType displayType) {
		
		removeAllDataSets();
		MSFeatureInfoBundle refBundle = null;
		int plotCount = cluster.getComponents().size();
		if(refType.equals(MsReferenceType.REFERENCE_FEATURE)) {
			
			plotCount = plotCount -1;
			refBundle = cluster.getComponents().stream().
					filter(b -> b.isUsedAsMatchingTarget()).
					findFirst().orElse(null);
			if(refBundle == null)
				return;
			
			List<MSFeatureInfoBundle> unkBundles = 
					cluster.getComponents().stream().
					filter(b -> !b.isUsedAsMatchingTarget()).
					collect(Collectors.toList());
			
			for(MSFeatureInfoBundle b : unkBundles) {
				
				MsFeaturePair featurePair = 
						new MsFeaturePair(refBundle.getMsFeature(), b.getMsFeature());
				
				MsDataSet dataSet = null;
				if(displayType.equals(MSReferenceDisplayType.HEAD_TO_TAIL))
					dataSet = new HeadToTailMsDataSet(featurePair, msLevel);
					
				if(displayType.equals(MSReferenceDisplayType.DIFFERENCE)) {
					
				}
				if(displayType.equals(MSReferenceDisplayType.HEAD_TO_HEAD)) {
					
				}				
				if(dataSet != null) {
					
					XYPlot newPlot = getNewXYPlot();
					if(msLevel.equals(MsDepth.MS1)) {
						
						newPlot.setDataset(0, dataSet);
						newPlot.setRenderer(0, defaultMsRenderer);
					}
					if(msLevel.equals(MsDepth.MS2)) {
						
						newPlot.setDataset(1, dataSet);
						newPlot.setRenderer(1, defaultMsRenderer);
						
//						addParentIonDataSeries(
//								dataSet,
//								newPlot,
//								featurePair.getUnknownFeatureParentIon(),
//								featurePair.getReferenceFeatureParentIon());
					}	
					ValueMarker marker = new ValueMarker(0.0d);
					marker.setPaint(Color.GRAY);
					newPlot.addRangeMarker(marker);
					try {
						((CombinedDomainXYPlot)plot).add(newPlot);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					objectPlotMap.put(featurePair, newPlot);
				}
			}
		}
		if(refType.equals(MsReferenceType.LIBRARY_MATCH)) {
			
		}
		setPreferredSize(new Dimension(
				getPreferredSize().width, subPlotHeight * plotCount));

		chart.fireChartChanged();
		revalidate();
		repaint();
	}
	
	private void addParentIonDataSeries(
			MsDataSet dataSet,
			XYPlot plot,
			MsPoint featureParentIon,
			MsPoint referenceParentIon) {
		
		XYSeriesCollection parentSet = new XYSeriesCollection();
		if(featureParentIon != null) {
			dataSet.getMassRange().extendRange(featureParentIon.getMz());
			XYSeries parentSeries = new XYSeries("Feature parent ion");
			parentSeries.add(featureParentIon.getMz(), featureParentIon.getIntensity());			
			parentSet.addSeries(parentSeries);
		}	
		if(referenceParentIon != null) {
			dataSet.getMassRange().extendRange(referenceParentIon.getMz());
			XYSeries refParentSeries = new XYSeries("Reference parent ion");
			refParentSeries.add(referenceParentIon.getMz(), -referenceParentIon.getIntensity());			
			parentSet.addSeries(refParentSeries);
		}
		plot.setRenderer(0, defaultParentIonRenderer);
		plot.setDataset(0, parentSet);
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

			XYPlot activePlot = ((CombinedDomainXYPlot)plot).findSubplot(
					this.getChartRenderingInfo().getPlotInfo(), this.getMousePosition());
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

			XYPlot activePlot = ((CombinedDomainXYPlot)plot).findSubplot(
					this.getChartRenderingInfo().getPlotInfo(), this.getMousePosition());
			markerEnd = getPosition(e, activePlot);
			updateMarker(activePlot);
			markerStartPoint = null;
			repaint();
		} else {
			super.mouseReleased(e);
		}
	}
	
	protected void updateMarker(XYPlot activePlot) {

		activePlot.clearDomainMarkers();	
		if (markerStart > 0.0d && markerEnd > 0.0d && markerEnd > markerStart) {

			IntervalMarker marker = new IntervalMarker(markerStart, markerEnd);
			marker.setPaint(markerColor);
			marker.setAlpha(0.5f);
			activePlot.addDomainMarker(marker, Layer.FOREGROUND);		
		}
	}
	
	protected Point2D getMarkerStartPoint(int x, int y, Rectangle2D area) {
		double xx = Math.max(area.getMinX(), Math.min(x, area.getMaxX()));
		double yy = Math.max(area.getMinY(), Math.min(y, area.getMaxY()));
		return new Point2D.Double(xx, yy);
	}

	protected double getPosition(MouseEvent e, XYPlot plot ) {
		Point2D p = translateScreenToJava2D(e.getPoint());
		Rectangle2D plotArea = getScreenDataArea();
		return plot.getDomainAxis().java2DToValue(p.getX(), plotArea, plot.getDomainAxisEdge());
	}
	
	protected void drawMarkerRectangle(Rectangle2D markerRectangle, Graphics2D g2, boolean xor) {

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























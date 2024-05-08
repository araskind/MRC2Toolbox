package edu.umich.med.mrc2.datoolbox.gui.plot.lcms.multi;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
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
import org.jfree.chart.ui.RectangleAnchor;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.chart.ui.TextAnchor;
import org.jfree.data.Range;
import org.jfree.data.xy.XYSeriesCollection;
import org.openscience.cdk.depict.Depiction;
import org.openscience.cdk.depict.DepictionGenerator;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.smiles.SmilesParser;

import edu.umich.med.mrc2.datoolbox.data.MSFeatureInfoBundle;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureIdentity;
import edu.umich.med.mrc2.datoolbox.data.MsPlotDataObject;
import edu.umich.med.mrc2.datoolbox.data.MsPoint;
import edu.umich.med.mrc2.datoolbox.data.enums.MsDepth;
import edu.umich.med.mrc2.datoolbox.data.msclust.IMsFeatureInfoBundleCluster;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.plot.LockedXYImageAnnotation;
import edu.umich.med.mrc2.datoolbox.gui.plot.LockedXYTextAnnotation;
import edu.umich.med.mrc2.datoolbox.gui.plot.PlotType;
import edu.umich.med.mrc2.datoolbox.gui.plot.dataset.HeadToTailMsDataSet;
import edu.umich.med.mrc2.datoolbox.gui.plot.lcms.LCMSPlotPanel;
import edu.umich.med.mrc2.datoolbox.gui.plot.lcms.spectrum.MSReferenceDisplayType;
import edu.umich.med.mrc2.datoolbox.gui.plot.lcms.spectrum.MsReferenceType;
import edu.umich.med.mrc2.datoolbox.gui.plot.renderer.DefaultSplineRenderer;
import edu.umich.med.mrc2.datoolbox.gui.plot.renderer.FilledChromatogramRenderer;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;

public class LCMSMultiPlotPanel extends LCMSPlotPanel {

	
	private static final long serialVersionUID = 1L;
	
	protected int minimumSubPlotHeight = 200;
	protected int maximumSubPlotHeight = 400;
	protected FilledChromatogramRenderer filledChromatogramRenderer;
	protected FilledChromatogramRenderer linesChromatogramRenderer;
	protected DefaultSplineRenderer splineRenderer;
	protected Map<Comparable, XYPlot>objectPlotMap;	
	protected Range defaultRtRange;
	protected double rtWindowExtensionWidth;
	protected int plotCount;

	private static final SmilesParser smipar = 
			new SmilesParser(SilentChemObjectBuilder.getInstance());
	private static final DepictionGenerator dptgen = 
			new DepictionGenerator().withAtomColors().
					withBackgroundColor(new Color(255,255,255,0));

	public LCMSMultiPlotPanel(PlotType plotType) {
		
		super(plotType);
		
		dataPointsVisible = false;
		annotationsVisible = true;
		legendVisible = false;
		
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

		if(refType.equals(MsReferenceType.REFERENCE_FEATURE))
			createFeatureToReferenceFeaturePlotForCluster(cluster, msLevel, displayType);
		
		if(refType.equals(MsReferenceType.LIBRARY_MATCH))
			createFeatureToLibraryMatchPlotForCluster(cluster, displayType);
		
		
		adjustSize(plotCount);
		chart.fireChartChanged();
		revalidate();
		repaint();
	}
	
	protected void createFeatureToReferenceFeaturePlotForCluster(
			IMsFeatureInfoBundleCluster cluster, 
			MsDepth msLevel, 
			MSReferenceDisplayType displayType) {

		MSFeatureInfoBundle refBundle = 
				cluster.getComponents().stream().
				filter(b -> b.isUsedAsMatchingTarget()).
				findFirst().orElse(null);
		if(refBundle == null)
			return;
		
		List<MSFeatureInfoBundle> unkBundles = 
				cluster.getComponents().stream().
				filter(b -> !b.isUsedAsMatchingTarget()).
				collect(Collectors.toList());
		
		plotCount = 0;
		for(MSFeatureInfoBundle b : unkBundles) {
			
			XYPlot newPlot = null;
			
			if(displayType.equals(MSReferenceDisplayType.HEAD_TO_TAIL)) {
				
				MsPlotDataObject unkData = createMsPlotDataObject(
						b, msLevel, MsReferenceType.REFERENCE_FEATURE);
				MsPlotDataObject refData = createMsPlotDataObject(
						refBundle, msLevel, MsReferenceType.REFERENCE_FEATURE);
				newPlot = createHeadToTailPlot(unkData, refData, msLevel);
			}
			if(displayType.equals(MSReferenceDisplayType.DIFFERENCE)) {
				
			}
			if(displayType.equals(MSReferenceDisplayType.OVERLAY)) {
				
			}
			if(newPlot != null) {
				try {
					((CombinedDomainXYPlot)plot).add(newPlot);
					plotCount++;
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}				
			}
		}
	}
	
	protected MsPlotDataObject createMsPlotDataObject(
			MSFeatureInfoBundle msf, 
			MsDepth msLevel,
			MsReferenceType refType) {
		
		Collection<MsPoint>spectrum = null;
		MsPoint parent = null;
		String label = null;
		Image compoundImage = null;
		if(msLevel.equals(MsDepth.MS1)) {
			
			if(msf.getMsFeature().getSpectrum() != null)
				spectrum = msf.getMsFeature().getSpectrum().getMsPoints();
		}
		if(msLevel.equals(MsDepth.MS2)) {
			
			if(msf.getMsFeature().getSpectrum() != null
					&& msf.getMsFeature().getSpectrum().getExperimentalTandemSpectrum() != null) {
				
				spectrum = msf.getMsFeature().getSpectrum().getExperimentalTandemSpectrum().getSpectrum();
				parent = msf.getMsFeature().getSpectrum().getExperimentalTandemSpectrum().getParent();
			}
		}
		if(refType.equals(MsReferenceType.REFERENCE_FEATURE)) {
			label = msf.getMsFeature().getName();
			compoundImage = getCompoundImage(msf.getMsFeature().getPrimaryIdentity());
		}
		if(refType.equals(MsReferenceType.LIBRARY_MATCH)) {
			
		}
		MsPlotDataObject pda = new MsPlotDataObject(spectrum, parent, label);
		pda.setImage(compoundImage);
		return pda;
	}
	
	private Image getCompoundImage(MsFeatureIdentity id) {
		
		if(id == null || id.getCompoundIdentity() == null 
				|| id.getCompoundIdentity().getSmiles() == null)
		return null;
		
		Depiction dpic = null;
		String smiles = id.getCompoundIdentity().getSmiles();
		if (smiles.isEmpty() || smiles.equals("NoSmile")) 
			return null;
		
		IAtomContainer mol = null;
		try {
			mol = smipar.parseSmiles(smiles);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		if (mol != null) {
			try {
				dpic = dptgen.depict(mol);
			} catch (CDKException e) {
				// e.printStackTrace();
			}
		}
		if(dpic != null)
			return dpic.toImg();
		else
			return null;
	}
	
	protected void createFeatureToLibraryMatchPlotForCluster(
			IMsFeatureInfoBundleCluster cluster, 
			MSReferenceDisplayType displayType) {
		
		plotCount = 0;
		for(MSFeatureInfoBundle b : cluster.getComponents()) {
			
			if(displayType.equals(MSReferenceDisplayType.HEAD_TO_TAIL)) {
				
			}
			if(displayType.equals(MSReferenceDisplayType.DIFFERENCE)) {
				
			}
			if(displayType.equals(MSReferenceDisplayType.OVERLAY)) {
				
			}
		}
	}
	
	protected void adjustSize(int subPlotCount) {
		
		if(getPreferredSize().height < minimumSubPlotHeight * subPlotCount)
			setPreferredSize(new Dimension(
					getPreferredSize().width, minimumSubPlotHeight * subPlotCount));
		
		if(getPreferredSize().height > maximumSubPlotHeight * subPlotCount)
			setPreferredSize(new Dimension(
					getPreferredSize().width, maximumSubPlotHeight * subPlotCount));
	}
	
//	protected XYPlot createReferenceFeaturePlot(
//			MsFeaturePair featurePair,
//			MsDepth msLevel, 
//			MSReferenceDisplayType displayType) {
//			
//		if(featurePair.getUnknownFeature().getSpectrum() == null 
//				|| featurePair.getReferenceFeature().getSpectrum() == null)
//			return null;
//		
//		XYPlot newPlot = null;
//		Collection<MsPoint>unkSpectrum = null;
//		MsPoint unkParent = null;
//		Collection<MsPoint>refSpectrum = null;
//		MsPoint refParent = null;
//		if(msLevel.equals(MsDepth.MS1)) {
//			
//			unkSpectrum = featurePair.getUnknownFeature().getSpectrum().getMsPoints();
//			refSpectrum = featurePair.getReferenceFeature().getSpectrum().getMsPoints();			
//		}
//		if(msLevel.equals(MsDepth.MS2)) {
//			
//			if(featurePair.getUnknownFeature().getSpectrum().getExperimentalTandemSpectrum() != null) {
//				unkSpectrum = featurePair.getUnknownFeature().getSpectrum().getExperimentalTandemSpectrum().getSpectrum();
//				unkParent = featurePair.getUnknownFeature().getMSMSParentIon();
//			}
//			if(featurePair.getReferenceFeature().getSpectrum().getExperimentalTandemSpectrum() != null) {
//				refSpectrum = featurePair.getReferenceFeature().getSpectrum().getExperimentalTandemSpectrum().getSpectrum();
//				refParent = featurePair.getReferenceFeature().getMSMSParentIon();
//			}
//		}
//		if(displayType.equals(MSReferenceDisplayType.HEAD_TO_TAIL))			
////			newPlot = createheadToTailPlot(
////					unkSpectrum, 
////					unkParent, 
////					featurePair.getUnknownFeature().getName()
////					refSpectrum, 
////					refParent);
//		
//		return newPlot;
//	}
	
	protected XYPlot createHeadToTailPlot(
				MsPlotDataObject unkData,
				MsPlotDataObject refData,
				MsDepth msLevel) {
		
		XYPlot newPlot = getNewXYPlot();
		HeadToTailMsDataSet dataSet = 
				new HeadToTailMsDataSet(unkData, refData);
		
		if(msLevel.equals(MsDepth.MS1)) {
			
			newPlot.setDataset(0, dataSet);
			newPlot.setRenderer(0, defaultMsRenderer);
		}
		if(msLevel.equals(MsDepth.MS2)) {
			
			XYSeriesCollection parentIonDataSet = dataSet.getParentIonDataSet();
			if(parentIonDataSet != null) {
				
				newPlot.setDataset(0, parentIonDataSet);
				newPlot.setRenderer(0, defaultParentIonRenderer);	
				newPlot.setDataset(1, dataSet);
				newPlot.setRenderer(1, defaultMsRenderer);
			}
			else {
				newPlot.setDataset(0, dataSet);
				newPlot.setRenderer(0, defaultMsRenderer);
			}
		}	
		ValueMarker marker = new ValueMarker(0.0d);
		marker.setPaint(Color.GRAY);
		newPlot.addRangeMarker(marker);
		newPlot.addAnnotation(
				createLockedAnnotation(
						unkData.getLabel(), TextAnchor.TOP_LEFT));
		newPlot.addAnnotation(
				createLockedAnnotation(
						refData.getLabel(), TextAnchor.BOTTOM_LEFT));
		
		newPlot.getRenderer().removeAnnotations();
		if(unkData.getImage() != null) {
			
			LockedXYImageAnnotation unkImage = 
					new LockedXYImageAnnotation(
							unkData.getImage(), RectangleAnchor.TOP_RIGHT, 0.03d, 1.0f);
			newPlot.getRenderer().addAnnotation(unkImage, Layer.BACKGROUND);
		}
		if(refData.getImage() != null) {
			
			LockedXYImageAnnotation unkImage = 
					new LockedXYImageAnnotation(
							refData.getImage(), RectangleAnchor.BOTTOM_RIGHT, 0.03d, 1.0f);
			newPlot.getRenderer().addAnnotation(unkImage, Layer.BACKGROUND);
		}
		return newPlot;
	}
	
	private LockedXYTextAnnotation createLockedAnnotation(
			String text, TextAnchor textAnchor) {
		
        LockedXYTextAnnotation a = 
        		new LockedXYTextAnnotation(text, 0.03d);
		a.setTextAnchor(textAnchor);
		a.setPaint(Color.BLACK);
		//	a.setOutlinePaint(Color.BLACK);
		a.setOutlineVisible(false);
		a.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 14));
        return a;
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
		return minimumSubPlotHeight;
	}

	public void setSubPlotHeight(int newSubPlotHeight) {
		
		boolean update = minimumSubPlotHeight != newSubPlotHeight;
		this.minimumSubPlotHeight = newSubPlotHeight;
//		if(update && !fileFeatureMap.isEmpty()) {
		
//		}
	}
}























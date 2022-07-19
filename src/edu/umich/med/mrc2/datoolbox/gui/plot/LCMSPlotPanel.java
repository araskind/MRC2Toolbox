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

package edu.umich.med.mrc2.datoolbox.gui.plot;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.IntervalMarker;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.renderer.xy.XYSplineRenderer;
import org.jfree.chart.ui.Layer;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.ExtractedChromatogram;
import edu.umich.med.mrc2.datoolbox.data.ExtractedIonData;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureChromatogramBundle;
import edu.umich.med.mrc2.datoolbox.data.MsPoint;
import edu.umich.med.mrc2.datoolbox.data.compare.SortDirection;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.plot.chromatogram.SmoothingPreferencesDialog;
import edu.umich.med.mrc2.datoolbox.gui.plot.dataset.MsDataSet;
import edu.umich.med.mrc2.datoolbox.gui.plot.renderer.ChromatogramToolTipGenerator;
import edu.umich.med.mrc2.datoolbox.gui.plot.renderer.ContinuousCromatogramRenderer;
import edu.umich.med.mrc2.datoolbox.gui.plot.renderer.DefaultSplineRenderer;
import edu.umich.med.mrc2.datoolbox.gui.plot.renderer.FilledChromatogramRenderer;
import edu.umich.med.mrc2.datoolbox.gui.plot.renderer.MassSpectrumRenderer;
import edu.umich.med.mrc2.datoolbox.gui.plot.renderer.MsLabelGenerator;
import edu.umich.med.mrc2.datoolbox.gui.rawdata.RawDataExaminerPanel;
import edu.umich.med.mrc2.datoolbox.gui.utils.ColorUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.main.RawDataManager;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.utils.Range;
import edu.umich.med.mrc2.datoolbox.utils.RawDataUtils;
import edu.umich.med.mrc2.datoolbox.utils.filter.Filter;
import umich.ms.datatypes.LCMSData;
import umich.ms.datatypes.scan.IScan;


public class LCMSPlotPanel extends MasterPlotPanel {

	/**
	 *
	 */
	private static final long serialVersionUID = -752801915848372103L;

	protected PlotType plotType = null;
	protected ValueMarker retentionMarker;

	protected int highlightMask = InputEvent.SHIFT_DOWN_MASK;

	protected ActionListener visualizer;

	protected Marker marker;
	protected Double markerStart = Double.NaN;
	protected Double markerEnd = Double.NaN;

	protected transient Rectangle2D markerRectangle = null;
	protected Point2D markerStartPoint = null;
	public static final Paint markerColor = new Color(150, 150, 150);;

	protected NumberAxis xAxis, yAxis;
	protected MassSpectrumRenderer defaultMsRenderer;
	protected MsLabelGenerator defaultMsLabelGenerator;
	protected MSReferenceDisplayType msReferenceDisplayType;
	
	protected FilledChromatogramRenderer filledChromatogramRenderer;
	protected FilledChromatogramRenderer linesChromatogramRenderer;
	protected XYItemRenderer splineRenderer;
	
	protected Collection<ExtractedChromatogram> chromatograms;
	protected RawDataExaminerPanel rawDataExaminerPanel;
	//	protected MsFeatureChromatogramBundle xicBundle;
	protected Collection<MsFeatureChromatogramBundle> xicBundles;
	protected Collection<Double>precursorMarkers;
	
	protected ChromatogramRenderingType chromatogramRenderingType;
	protected SmoothingPreferencesDialog smoothingPreferencesDialog;
	protected boolean smoothChromatogram;
	protected Filter smoothingFilter;
	protected String filterId;

	public LCMSPlotPanel(PlotType type) {

		super();
		plotType = type;
		legendVisible = true;

		initChart();
		initTitles();
		initLegend(RectangleEdge.BOTTOM, legendVisible);
		
		initPlot();
		initRendererForPlotType();
		setMouseWheelEnabled(true);

		if (plotType.equals(PlotType.SPECTRUM))
			msReferenceDisplayType = MSReferenceDisplayType.HEAD_TO_HEAD;

		dataPointsVisible = false;
		annotationsVisible = true;
		precursorMarkers = new TreeSet<Double>();
	}

	public void actionPerformed(ActionEvent event) {

		String command = event.getActionCommand();
		if (command.equals(ChartPanel.ZOOM_IN_DOMAIN_COMMAND)) {
			plot.getDomainAxis().resizeRange(1 / ZOOM_FACTOR);
			return;
		}

		if (command.equals(ChartPanel.ZOOM_OUT_DOMAIN_COMMAND)) {
			plot.getDomainAxis().resizeRange(ZOOM_FACTOR);
			return;
		}

		if (command.equals(ChartPanel.ZOOM_IN_RANGE_COMMAND)) {			
			double newTop = plot.getRangeAxis().getRange().getLength() / ZOOM_FACTOR;
			plot.getRangeAxis().setRange(0.0d, newTop);
			return;
		}
		if (command.equals(ChartPanel.ZOOM_OUT_RANGE_COMMAND)) {
			double newTop = plot.getRangeAxis().getRange().getLength() * ZOOM_FACTOR;
			plot.getRangeAxis().setRange(0.0d, newTop);
			return;
		}
		if (command.equals(ChartPanel.ZOOM_IN_BOTH_COMMAND)) {

			double newTop = plot.getRangeAxis().getRange().getLength() / ZOOM_FACTOR;
			plot.getRangeAxis().setRange(0.0d, newTop);
			plot.getDomainAxis().resizeRange(1 / ZOOM_FACTOR);
			return;
		}
		if (command.equals(ChartPanel.ZOOM_OUT_BOTH_COMMAND)) {

			double newTop = plot.getRangeAxis().getRange().getLength() * ZOOM_FACTOR;
			plot.getRangeAxis().setRange(0.0d, newTop);
			plot.getDomainAxis().resizeRange(ZOOM_FACTOR);
			return;
		}
		if (command.equals(ChartPanel.ZOOM_RESET_DOMAIN_COMMAND))
			plot.getDomainAxis().setAutoRange(true);

		if (command.equals(ChartPanel.ZOOM_RESET_RANGE_COMMAND)) {
			plot.getRangeAxis().setAutoRange(true);
			return;
		}
		if (command.equals(MasterPlotPanel.TOGGLE_DATA_POINTS_COMMAND)) {
			toggleDataPoints();
			return;
		}
		if (command.equals(MainActionCommands.SMOOTH_CHROMATOGRAM_COMMAND.getName())) 
			smoothChromatograms();
		
		if (command.equals(MainActionCommands.SHOW_RAW_CHROMATOGRAM_COMMAND.getName())) 
			showRawChromatograms();
		
		if (command.equals(MainActionCommands.SHOW_SMOOTHING_PREFERENCES_COMMAND.getName())) 
			showChromatogramSmoothingPreferences();
		
		if (command.equals(MainActionCommands.SAVE_SMOOTHING_PREFERENCES_COMMAND.getName())) 
			saveChromatogramSmoothingPreferences();
		
		super.actionPerformed(event);
	}
	
	private void smoothChromatograms() {

		if(plotType.equals(PlotType.SPECTRUM))
			return;
		
		if(smoothingFilter == null) {
			MessageDialog.showWarningMsg("Smoothing parameters not defined.", this);
			return;
		}
		smoothChromatogram = true;	
		redrawChromatograms(chromatogramRenderingType);
		toolbar.toggleSmoothingIcon(smoothChromatogram);
	}

	private void showRawChromatograms() {

		if(plotType.equals(PlotType.SPECTRUM))
			return;
		
		smoothChromatogram = false;
		redrawChromatograms(chromatogramRenderingType);		
		toolbar.toggleSmoothingIcon(smoothChromatogram);
	}

	private void showChromatogramSmoothingPreferences() {
		// TODO Auto-generated method stub
		if(plotType.equals(PlotType.SPECTRUM))
			return;
		
		smoothingPreferencesDialog = new SmoothingPreferencesDialog(this, filterId);
		smoothingPreferencesDialog.setLocationRelativeTo(this);
		smoothingPreferencesDialog.setVisible(true);
	}

	private void saveChromatogramSmoothingPreferences() {

		if(plotType.equals(PlotType.SPECTRUM))
			return;
		
		smoothingFilter = smoothingPreferencesDialog.getSmoothingFilter();
		smoothingPreferencesDialog.dispose();
		try {
			smoothChromatograms();
		} catch (Exception e) {
			MessageDialog.showErrorMsg("Bad filter parameters", this);
		}	
	}

	public void resetDomainAxis() {
		plot.getDomainAxis().setAutoRange(true);
	}

	public synchronized void addDataSet(XYDataset dataSet, Color color, boolean transparency) {

		XYItemRenderer newRenderer = new ContinuousCromatogramRenderer(color, transparency);

		plot.setDataset(numberOfDataSets, dataSet);
		plot.setRenderer(numberOfDataSets, newRenderer);
		numberOfDataSets++;
	}

	private void drawMarkerRectangle(Graphics2D g2, boolean xor) {

		if (this.markerRectangle != null) {
			if (xor)
				g2.setXORMode(Color.gray);

			g2.setPaint(markerColor);
			g2.fill(markerRectangle);

			if (xor)
				g2.setPaintMode();
		}
	}

	public MassSpectrumRenderer getDefaultMsRenderer() {
		return defaultMsRenderer;
	}

	public Range getHighlightedRTRange() {

		double lower = 0d;
		double upper = 0d;

		if (!(markerStart.isNaN() && markerEnd.isNaN())) {
			lower = markerStart;
			upper = markerEnd;
		}
		Range rtRange = new Range(lower, upper);

		return rtRange;
	}

	/**
	 * @return the marker
	 */
	public Marker getMarker() {
		return marker;
	}

	/**
	 * @return the markerEnd
	 */
	public Double getMarkerEnd() {
		return markerEnd;
	}

	/**
	 * @return the markerStart
	 */
	public Double getMarkerStart() {
		return markerStart;
	}

	private Point2D getMarkerStartPoint(int x, int y, Rectangle2D area) {
		double xx = Math.max(area.getMinX(), Math.min(x, area.getMaxX()));
		double yy = Math.max(area.getMinY(), Math.min(y, area.getMaxY()));
		return new Point2D.Double(xx, yy);
	}

	@Override
	public Plot getPlot() {
		return plot;
	}

	public PlotType getPlotType() {
		return plotType;
	}

	private Double getPosition(MouseEvent e) {
		Point2D p = translateScreenToJava2D(e.getPoint());
		Rectangle2D plotArea = getScreenDataArea();
		XYPlot plot = (XYPlot) chart.getPlot();
		return plot.getDomainAxis().java2DToValue(p.getX(), plotArea, plot.getDomainAxisEdge());
	}

	public Range getSelectedRTRange() {

		double lower = 0d;
		double upper = 0d;

		if (!(markerStart.isNaN() && markerEnd.isNaN())) {
			lower = markerStart;
			upper = markerEnd;
		} else {
			lower = plot.getDomainAxis().getLowerBound();
			upper = plot.getDomainAxis().getUpperBound();
		}
		Range rtRange = new Range(lower, upper);
		return rtRange;
	}

	@Override
	protected void initAxes() {

		xAxis = (NumberAxis) plot.getDomainAxis();

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
		xAxis.setUpperMargin(0.05);
		xAxis.setLowerMargin(0.001);
		xAxis.setTickLabelInsets(new RectangleInsets(0, 0, 20, 20));

		yAxis = (NumberAxis) plot.getRangeAxis();
		yAxis.setNumberFormatOverride(MRC2ToolBoxConfiguration.getIntensityFormat());
		yAxis.setUpperMargin(0.1);
		yAxis.setLowerMargin(0.1);
	}

	@Override
	protected void initChart() {

		chart = ChartFactory.createXYLineChart("", // title
				"", // x-axis label
				"Intensity", // y-axis label
				null, // data set
				PlotOrientation.VERTICAL, // orientation
				legendVisible, // create legend?
				true, // generate tooltips?
				false // generate URLs?
		);
		chart.setBackgroundPaint(Color.white);
		setChart(chart);
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
	}

	private void initRendererForPlotType() {

		if (plotType.equals(PlotType.SPECTRUM)) {

			defaultMsRenderer = new MassSpectrumRenderer(Color.RED, 3.0f);
			defaultMsLabelGenerator = new MsLabelGenerator(this);
			defaultMsRenderer.setDefaultItemLabelGenerator(defaultMsLabelGenerator);
			defaultMsRenderer.setDefaultItemLabelsVisible(true);
			defaultMsRenderer.setDefaultItemLabelPaint(LCMSPlotPanel.LABEL_COLOR);
		}
		if (plotType.equals(PlotType.CHROMATOGRAM)) {
			
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
	}

	@Override
	public void mouseDragged(MouseEvent e) {

		if (MouseEvent.getMouseModifiersText(e.getModifiersEx()).equals("Shift+Button1")) {

			Rectangle2D scaledDataArea = getScreenDataArea((int) this.markerStartPoint.getX(),
					(int) this.markerStartPoint.getY());

			double xmax = Math.min(e.getX(), scaledDataArea.getMaxX());
			this.markerRectangle = new Rectangle2D.Double(this.markerStartPoint.getX(), scaledDataArea.getMinY(),
					xmax - this.markerStartPoint.getX(), scaledDataArea.getHeight());

			Graphics2D g2 = (Graphics2D) getGraphics();
			drawMarkerRectangle(g2, true);
			g2.dispose();
		} else {
			super.mouseDragged(e);
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {

		if (MouseEvent.getMouseModifiersText(e.getModifiersEx()).equals("Shift+Button1")) {

			markerStart = getPosition(e);

			Rectangle2D screenDataArea = getScreenDataArea(e.getX(), e.getY());

			if (screenDataArea != null)
				markerStartPoint = getMarkerStartPoint(e.getX(), e.getY(), screenDataArea);

		} else {
			super.mousePressed(e);
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {

		if (MouseEvent.getMouseModifiersText(e.getModifiersEx()).equals("Shift+Button1")) {

			markerEnd = getPosition(e);
			updateMarker();
			markerRectangle = null;
			markerStartPoint = null;
			repaint();
		} else {
			super.mouseReleased(e);
		}
	}

	public void removeMarkers() {

		plot.clearDomainMarkers();
		plot.clearRangeMarkers();
		markerRectangle = null;
	}

	public void setDefaultMsRenderer(MassSpectrumRenderer defaultMsRenderer) {
		this.defaultMsRenderer = defaultMsRenderer;
	}

	/**
	 * @param markerEnd
	 *            the markerEnd to set
	 */
	public void setMarkerEnd(Double markerEnd) {
		this.markerEnd = markerEnd;
	}

	/**
	 * @param markerStart
	 *            the markerStart to set
	 */
	public void setMarkerStart(Double markerStart) {
		this.markerStart = markerStart;
	}

	@Override
	public void setToolbar(PlotToolbar toolbar) {

		this.toolbar = toolbar;
	}

	public void toggleDataPoints() {

		dataPointsVisible = !dataPointsVisible;

		final int count = plot.getRendererCount();
		for (int i = 0; i < count; i++) {

			if (plot.getRenderer(i) instanceof XYLineAndShapeRenderer) {

				final XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer(i);
				renderer.setDefaultShapesVisible(dataPointsVisible);
			}
		}
		toolbar.toggleDataPointssIcon(dataPointsVisible);
	}

	private void updateMarker() {
		if (marker != null) {
			plot.removeDomainMarker(marker, Layer.FOREGROUND);
		}
		if (!(markerStart.isNaN() && markerEnd.isNaN())) {
			if (markerEnd > markerStart) {
				marker = new IntervalMarker(markerStart, markerEnd);
				marker.setPaint(markerColor);
				marker.setAlpha(0.5f);
				plot.addDomainMarker(marker, Layer.FOREGROUND);
			}
		}
	}

	@Override
	public synchronized void removeAllDataSets() {

		if(plot != null) {

			removeMarkers();			
			for (int i = 0; i < plot.getDatasetCount(); i++)
				plot.setDataset(i, null);

			plot.clearAnnotations();
			numberOfDataSets = 0;
		}
		if(chromatograms != null)
			chromatograms.clear();
		
		xicBundles = null;
		
		if(precursorMarkers != null)
			precursorMarkers.clear();
	}
	
	/*
	 * Mass spectra display
	 */
	
	public void showScan(IScan s) {
		
		String labelText =  RawDataUtils.getScanLabel(s);
		Collection<MsPoint> pattern = RawDataUtils.getScanPoints(s, 0.0d);
		MsDataSet targetMs = new MsDataSet(pattern, false, labelText);	
		MassSpectrumRenderer msRenderer = new MassSpectrumRenderer(Color.DARK_GRAY, 2.0f);
		msRenderer.setDefaultItemLabelGenerator(defaultMsLabelGenerator);
		msRenderer.setDefaultItemLabelsVisible(annotationsVisible);
		msRenderer.setDefaultItemLabelPaint(LCMSPlotPanel.LABEL_COLOR);
		
		((XYPlot) this.getPlot()).setRenderer(1, msRenderer);
		((XYPlot) this.getPlot()).setDataset(1, targetMs);
				
		//	Mark precursor ranges or individual precursors
		//	Highest MS level
		if(s.getChildScans() == null || s.getChildScans().isEmpty())
			addParentIonDataSeriesToHighestLevelMSMSPlot(s);
		else 	//	Show precursors in parent scan
			addParentIonDataSeriesToParentMSMSPlot(s);		
	}
	
	private void addParentIonDataSeriesToParentMSMSPlot(IScan s) {
		
		double size = 16.0;
		Shape precursorMarker = new Ellipse2D.Double(-size/4.0, -size/4.0, size/2.0, size/2.0);		
		DataFile df = rawDataExaminerPanel.getDataFileForScan(s);
		if(df == null)
			return;

		LCMSData data = RawDataManager.getRawData(df);
		if(data == null) 
			return;
			
		XYSeriesCollection parentSet = new XYSeriesCollection();
		XYSeries parentSeries = new XYSeries("Parent ions");
		XYItemRenderer parentRenderer = new XYLineAndShapeRenderer(false, true);
		parentRenderer.setSeriesPaint(0, Color.RED);
		parentRenderer.setSeriesShape(0, precursorMarker);
		for(Integer child : s.getChildScans()) {
			
			IScan childScan = data.getScans().getScanByNum(child);
			if(childScan.getPrecursor() == null)
				continue;
			
			Double precursorMz = null;
			if (childScan.getPrecursor().getMzRangeStart() != null && childScan.getPrecursor().getMzRangeEnd() != null) {
				
				//	Highlight window
		        Marker isolationWindow = 
	        		new IntervalMarker(
        				childScan.getPrecursor().getMzRangeStart(), 
        				childScan.getPrecursor().getMzRangeEnd(), 
        				Color.RED, new BasicStroke( 2.0f ), null, null, 0.5f );
		        isolationWindow.setPaint( Color.RED );
		        ((XYPlot) this.getPlot()).addDomainMarker(isolationWindow);					
				
			} else if (childScan.getPrecursor().getMzTarget() != null) {
				precursorMz = childScan.getPrecursor().getMzTarget();
			} else {
				if (childScan.getPrecursor().getMzTargetMono() != null) {
					precursorMz = childScan.getPrecursor().getMzTargetMono();
				}
			}
			if(precursorMz != null) {
				
				double intensity = 0.0;
				Integer precursorIndex = s.getSpectrum().findClosestMzIdx(precursorMz);
				if(precursorIndex != null)
					intensity = s.getSpectrum().getIntensities()[precursorIndex];
				else
					intensity = s.getBasePeakIntensity() / 5.0d;
				
				MsPoint trueParent = new MsPoint(precursorMz, intensity);
				parentSeries.add(trueParent.getMz(), trueParent.getIntensity());			
			}
		}
		if(parentSeries.getItemCount() > 0) {
			parentSet.addSeries(parentSeries);
			((XYPlot) this.getPlot()).setRenderer(2, parentRenderer);
			((XYPlot) this.getPlot()).setDataset(2, parentSet);
		}
	}

	public void addParentIonDataSeriesToHighestLevelMSMSPlot(IScan s) {
		
		double size = 16.0;
		if(s.getPrecursor() != null) {
			
			Double precursorMz = null;
			if (s.getPrecursor().getMzRangeStart() != null && s.getPrecursor().getMzRangeEnd() != null) {
				
				//	Highlight window
		        Marker isolationWindow = 
	        		new IntervalMarker(
        				s.getPrecursor().getMzRangeStart(), 
        				s.getPrecursor().getMzRangeEnd(), 
        				Color.RED, new BasicStroke( 2.0f ), null, null, 0.5f );
		        isolationWindow.setPaint( Color.RED );
		        ((XYPlot) this.getPlot()).addDomainMarker(isolationWindow);					
				return;
				
			} else if (s.getPrecursor().getMzTarget() != null) {
				precursorMz = s.getPrecursor().getMzTarget();
			} else {
				if (s.getPrecursor().getMzTargetMono() != null) {
					precursorMz = s.getPrecursor().getMzTargetMono();
				}
			}
			if(precursorMz != null) {
				//	Add parent ion
				XYSeriesCollection parentSet = new XYSeriesCollection();
				XYSeries parentSeries = new XYSeries("Parent ion");
				
				double intensity = 0.0;
				Integer precursorIndex = s.getSpectrum().findClosestMzIdx(precursorMz);
				if(precursorIndex != null)
					intensity = s.getSpectrum().getIntensities()[precursorIndex];
				else
					intensity = s.getBasePeakIntensity() / 5.0d;
				
				MsPoint trueParent = new MsPoint(precursorMz, intensity);
				parentSeries.add(trueParent.getMz(), trueParent.getIntensity());
				parentSet.addSeries(parentSeries);
				XYItemRenderer parentRenderer = new XYLineAndShapeRenderer(false, true);
				parentRenderer.setSeriesPaint(0, Color.RED);
				parentRenderer.setSeriesShape(0, new Ellipse2D.Double(-size/4.0, -size/4.0, size/2.0, size/2.0));
				((XYPlot) this.getPlot()).setRenderer(2, parentRenderer);
				((XYPlot) this.getPlot()).setDataset(2, parentSet);
			}
		}
	}

	public void setRawDataExaminerPanel(RawDataExaminerPanel rawDataExaminerPanel) {
		this.rawDataExaminerPanel = rawDataExaminerPanel;
	}
	
	/*
	 * Chromatogram display
	 */
	
	public void showExtractedChromatogramCollection(
			Collection<ExtractedChromatogram> chromatograms, 
			ChromatogramRenderingType rType) {
		
		chromatogramRenderingType = rType;
		this.chromatograms = chromatograms;
		XYItemRenderer renderer = null;
		if(rType.equals(ChromatogramRenderingType.Spline))
			renderer = splineRenderer;
		
		if(rType.equals(ChromatogramRenderingType.Lines))
			renderer = linesChromatogramRenderer;
		
		if(rType.equals(ChromatogramRenderingType.Filled))
			renderer = filledChromatogramRenderer;
		
		final XYToolTipGenerator toolTipGenerator = 				
				new ChromatogramToolTipGenerator();		
		renderer.setDefaultToolTipGenerator(toolTipGenerator);
		renderer.setDefaultShape(FilledChromatogramRenderer.dataPointsShape);
		if(XYLineAndShapeRenderer.class.isAssignableFrom(renderer.getClass()))
			((XYLineAndShapeRenderer)renderer).setDefaultShapesVisible(dataPointsVisible);	
		
		XYSeriesCollection dataSet = new XYSeriesCollection();
		int seriesCount = 0;
		for(ExtractedChromatogram chrom : chromatograms) {
			
			XYSeries series = new XYSeries(chrom.toString());
			double[] times = chrom.getTimeValues();
			double[] intensities = chrom.getIntensityValues();				
			if(smoothChromatogram && smoothingFilter != null) {			
				try {
					intensities = smoothingFilter.filter(times, chrom.getIntensityValues());
				} catch (IllegalArgumentException e) {					
					//e.printStackTrace();
					MessageDialog.showErrorMsg("Bad filter parameters", this);
				}
			}
			for(int i=0; i<times.length; i++)
				series.add(times[i], intensities[i]);

			dataSet.addSeries(series);
			Color seriesColor = chrom.getColor();
			if(rType.equals(ChromatogramRenderingType.Lines)) {
				renderer.setSeriesFillPaint(seriesCount, seriesColor);
				renderer.setSeriesPaint(seriesCount, seriesColor);
			}
			else {
				Paint seriesColorTp = new Color(
						seriesColor.getRed()/255.0f, 
						seriesColor.getGreen()/255.0f, 
						seriesColor.getBlue()/255.0f, 
						0.3f);
				renderer.setSeriesFillPaint(seriesCount, seriesColorTp);
				renderer.setSeriesPaint(seriesCount, seriesColorTp);
			}
			seriesCount++;
		}	
		((XYPlot) this.getPlot()).setDataset(dataSet);
		((XYPlot) this.getPlot()).setRenderer(renderer);
	}

//	public void showMsFeatureChromatogramBundle(
//			MsFeatureChromatogramBundle xicBundle,
//			Collection<Double>markers,
//			ChromatogramRenderingType rType) {
//		
//		chromatogramRenderingType = rType;
//		this.xicBundle = xicBundle;	
//		XYItemRenderer renderer = null;
//		if(rType.equals(ChromatogramRenderingType.Spline))
//			renderer = splineRenderer;
//		
//		if(rType.equals(ChromatogramRenderingType.Lines))
//			renderer = linesChromatogramRenderer;
//		
//		if(rType.equals(ChromatogramRenderingType.Filled))
//			renderer = filledChromatogramRenderer;
//		
//		final XYToolTipGenerator toolTipGenerator = 				
//				new ChromatogramToolTipGenerator();		
//		renderer.setDefaultToolTipGenerator(toolTipGenerator);	
//		renderer.setDefaultShape(FilledChromatogramRenderer.dataPointsShape);
//		if(XYLineAndShapeRenderer.class.isAssignableFrom(renderer.getClass()))		
//			((XYLineAndShapeRenderer)renderer).setDefaultShapesVisible(dataPointsVisible);
//								
//		XYSeriesCollection dataSet = new XYSeriesCollection();
//		int seriesCount = 0;
//		int fileChromCount = 0;
//		for(Entry<DataFile, Collection<ExtractedIonData>> ce : xicBundle.getChromatograms().entrySet()) {
//			
//			DataFile dataFile = ce.getKey();
//            List<Color> lineColorListList = 
//            		ColorUtils.getColorBands(dataFile.getColor(), ce.getValue().size(), SortDirection.ASC);
//			for(ExtractedIonData eid : ce.getValue()) {
//				
//				fileChromCount = 0;
//				XYSeries series = new XYSeries(dataFile.getName() + " " + eid.toString());
//				double[] times = eid.getTimeValues();
//				double[] intensities = eid.getIntensityValues();	
//				if(smoothChromatogram && smoothingFilter != null) {			
//					try {
//						intensities = smoothingFilter.filter(times, eid.getIntensityValues());
//					} catch (IllegalArgumentException e) {					
//						//e.printStackTrace();
//						MessageDialog.showErrorMsg("Bad filter parameters", this);
//					}
//				}
//				for(int i=0; i<times.length; i++)
//					series.add(times[i], intensities[i]);	
//				
//				dataSet.addSeries(series);
//				
//				Color seriesColor = lineColorListList.get(fileChromCount);		
//				if(rType.equals(ChromatogramRenderingType.Lines)) {
//					renderer.setSeriesFillPaint(seriesCount, seriesColor);
//					renderer.setSeriesPaint(seriesCount, seriesColor);
//				}
//				else {
//					Paint seriesColorTp = new Color(
//							seriesColor.getRed()/255.0f, 
//							seriesColor.getGreen()/255.0f, 
//							seriesColor.getBlue()/255.0f, 
//							0.3f);
//					renderer.setSeriesFillPaint(seriesCount, seriesColorTp);
//					renderer.setSeriesPaint(seriesCount, seriesColorTp);
//				}
//				fileChromCount++;
//				seriesCount++;
//			}		
//		}
//		((XYPlot) this.getPlot()).setDataset(dataSet);	
//		((XYPlot) this.getPlot()).setRenderer(renderer);
//		precursorMarkers.addAll(markers);
//		if(markers != null && !markers.isEmpty()) {
//			
//			for(double markerPosition : markers) {
//				
//				ValueMarker marker = new ValueMarker(markerPosition);
//				marker.setPaint(Color.RED);
//				((XYPlot) this.getPlot()).addDomainMarker(marker);
//			}
//		}	
//	}
	
	public void showMsFeatureChromatogramBundles(
			Collection<MsFeatureChromatogramBundle> xicBundles,
			Collection<Double>markers,
			ChromatogramRenderingType rType) {
		
		chromatogramRenderingType = rType;
		this.xicBundles = xicBundles;	
		XYItemRenderer renderer = null;
		if(rType.equals(ChromatogramRenderingType.Spline))
			renderer = splineRenderer;
		
		if(rType.equals(ChromatogramRenderingType.Lines))
			renderer = linesChromatogramRenderer;
		
		if(rType.equals(ChromatogramRenderingType.Filled))
			renderer = filledChromatogramRenderer;
		
		final XYToolTipGenerator toolTipGenerator = 				
				new ChromatogramToolTipGenerator();		
		renderer.setDefaultToolTipGenerator(toolTipGenerator);	
		renderer.setDefaultShape(FilledChromatogramRenderer.dataPointsShape);
		if(XYLineAndShapeRenderer.class.isAssignableFrom(renderer.getClass()))		
			((XYLineAndShapeRenderer)renderer).setDefaultShapesVisible(dataPointsVisible);
								
		XYSeriesCollection dataSet = new XYSeriesCollection();
		int seriesCount = 0;
		int fileChromCount = 0;
		
		for(MsFeatureChromatogramBundle xicBundle : xicBundles) {
			
			for(Entry<DataFile, Collection<ExtractedIonData>> ce : xicBundle.getChromatograms().entrySet()) {
				
				DataFile dataFile = ce.getKey();
	            List<Color> lineColorListList = 
	            		ColorUtils.getColorBands(dataFile.getColor(), ce.getValue().size(), SortDirection.ASC);
				for(ExtractedIonData eid : ce.getValue()) {
				
					fileChromCount = 0;
					XYSeries series = 
							new XYSeries(dataFile.getName() + " " + 
								eid.toString() + " [" + xicBundle.getFeatureId() +"]");
					double[] times = eid.getTimeValues();
					double[] intensities = eid.getIntensityValues();	
					if(smoothChromatogram && smoothingFilter != null) {			
						try {
							intensities = smoothingFilter.filter(times, eid.getIntensityValues());
						} catch (IllegalArgumentException e) {					
							//e.printStackTrace();
							MessageDialog.showErrorMsg("Bad filter parameters", this);
						}
					}
					for(int i=0; i<times.length; i++)
						series.add(times[i], intensities[i]);	
					
					dataSet.addSeries(series);
					
					Color seriesColor = lineColorListList.get(fileChromCount);		
					if(rType.equals(ChromatogramRenderingType.Lines)) {
						renderer.setSeriesFillPaint(seriesCount, seriesColor);
						renderer.setSeriesPaint(seriesCount, seriesColor);
					}
					else {
						Paint seriesColorTp = new Color(
								seriesColor.getRed()/255.0f, 
								seriesColor.getGreen()/255.0f, 
								seriesColor.getBlue()/255.0f, 
								0.3f);
						renderer.setSeriesFillPaint(seriesCount, seriesColorTp);
						renderer.setSeriesPaint(seriesCount, seriesColorTp);
					}
					fileChromCount++;
					seriesCount++;
				}		
			}
		}
		((XYPlot) this.getPlot()).setDataset(dataSet);	
		((XYPlot) this.getPlot()).setRenderer(renderer);		
		precursorMarkers.addAll(markers);
		if(markers != null && !markers.isEmpty()) {
			
			for(double markerPosition : markers) {
				
				ValueMarker marker = new ValueMarker(markerPosition);
				marker.setPaint(Color.RED);
				((XYPlot) this.getPlot()).addDomainMarker(marker);
			}
		}	
	}
	
	public void removeChromatogramsForFiles(Collection<DataFile>files) {
		
		if(chromatograms == null)
			return;
		
		XYSeriesCollection dataSet = (XYSeriesCollection)((XYPlot) this.getPlot()).getDataset(1);
		for(DataFile f : files) {
			
			Collection<ExtractedChromatogram> chroms = 
					chromatograms.stream().filter(c -> c.getDataFile().equals(f)).collect(Collectors.toList());
			if(!chroms.isEmpty()) {
				
				for(ExtractedChromatogram c : chroms) {
					XYSeries series = dataSet.getSeries(c.toString());
					if(series != null)
						dataSet.removeSeries(series);
				}
			}
		}
	}
	
	public void redrawChromatograms(ChromatogramRenderingType newRtype) {

		if(plotType.equals(PlotType.CHROMATOGRAM)) {
			
			chromatogramRenderingType = newRtype;
			
			if(chromatograms != null && !chromatograms.isEmpty())
				showExtractedChromatogramCollection(chromatograms, newRtype);
			
			if(xicBundles != null)
				showMsFeatureChromatogramBundles(xicBundles, precursorMarkers, newRtype);
		}
	}

	public Collection<MsFeatureChromatogramBundle> getXicBundles() {
		return xicBundles;
	}

	public String getFilterId() {
		return filterId;
	}

	public void setFilterId(String filterId) {
		this.filterId = filterId;
	}

	public Filter getSmoothingFilter() {
		return smoothingFilter;
	}

	public void setSmoothingFilter(Filter smoothingFilter) {
		this.smoothingFilter = smoothingFilter;
	}
}












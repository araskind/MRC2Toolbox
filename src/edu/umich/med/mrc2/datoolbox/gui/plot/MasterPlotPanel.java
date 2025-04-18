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
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Collection;

import javax.swing.SwingUtilities;

import org.geotools.brewer.color.ColorBrewer;
import org.jfree.chart.ChartColor;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.plot.DefaultDrawingSupplier;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.title.TextTitle;
import org.jfree.chart.ui.Layer;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.graphics2d.svg.SVGGraphics2D;

import com.orsonpdf.PDFDocument;
import com.orsonpdf.Page;

import edu.umich.med.mrc2.datoolbox.data.enums.ImageExportFormat;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.plot.lcms.LCMSPlotPanel;
import edu.umich.med.mrc2.datoolbox.gui.utils.jnafilechooser.api.JnaFileChooser;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.utils.FIOUtils;


public abstract class MasterPlotPanel extends ChartPanel{

	/**
	 *
	 */
	private static final long serialVersionUID = 2216261578200990582L;

	protected static double ZOOM_FACTOR = 1.2;

	public static final Color[] PLOT_COLORS = {
			Color.black,
			Color.red,
			Color.green,
			Color.blue,
			Color.orange,
			Color.cyan,
			Color.magenta,
			Color.pink
		};

	protected static Color[] brewedColors;

	public static Color LABEL_COLOR = Color.darkGray;
	public static Color GRID_COLOR = Color.lightGray;
	public static Color HIGHLIGHT_OUTLINE_COLOR = new Color(255, 255, 0);
	public static Color HIGHLIGHT_BODY_COLOR = new Color(255, 255, 0, 20);
	public static Color CROSS_HAIR_COLOR = Color.gray;
	public static Stroke CROSS_HAIR_STROKE =
			new BasicStroke(
					1.0F,
					BasicStroke.CAP_BUTT,
					BasicStroke.JOIN_BEVEL,
					1.0f,
					new float[] { 5.0F, 3.0F },
					0.0F);

	public static Shape DATA_POINT_SHAPE = new Ellipse2D.Double(-2.0, -2.0, 5.0, 5.0);
	public static RectangleInsets AXIS_OFFSET = new RectangleInsets(5.0, 5.0, 5.0, 5.0);
	public static Font TITLE_FONT = new Font("SansSerif", Font.BOLD, 12);
	public static Font SUBTITLE_FONT = new Font("SansSerif", Font.PLAIN, 11);
	public static Font LEGEND_FONT = new Font("SansSerif", Font.PLAIN, 11);
	public static double AXIS_MARGINS = 0.001;
	public static double TITLE_TOP_MARGIN = 1.0;
	protected XYPlot plot;
	protected JFreeChart chart;
	protected TextTitle chartTitle, chartSubTitle;
	protected boolean annotationsVisible = false;
	protected boolean dataPointsVisible;
	protected boolean legendVisible;
	protected int numberOfDataSets = 0;
	protected LegendTitle legend;
	protected Paint[] paintArray;
	protected Shape[] shapeArray;
	protected DoubleClickResetChartMouseListener doubleClickResetChartMouseListener;
	protected boolean suppressMouseClicks;
	
	public static final String TOGGLE_ANNOTATIONS_COMMAND = "TOGGLE_ANNOTATIONS";
	public static final String TOGGLE_DATA_POINTS_COMMAND = "TOGGLE_DATA_POINTS";
	public static final String TOGGLE_MS_HEAD_TO_TAIL_COMMAND = "TOGGLE_MS_HEAD_TO_TAIL";
	public static final String TOGGLE_LEGEND_COMMAND = "TOGGLE_LEGEND";
	public static final String SETUP_AXES_COMMAND = "SETUP_AXES";
	public static final String PREVIOUS_SCAN_COMMAND = "PREVIOUS_SCAN";
	public static final String NEXT_SCAN_COMMAND = "NEXT_SCAN";
	
	protected boolean handlePlotSaveExternally;

	public MasterPlotPanel() {

		super(null, true);

		setBackground(Color.white);
		setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));

		setMaximumDrawWidth(Integer.MAX_VALUE);
		setMaximumDrawHeight(Integer.MAX_VALUE);
		setMinimumDrawHeight(0);

		legendVisible = false;
		setFocusable(true);

		paintArray = ChartColor.createDefaultPaintArray();
		shapeArray = DefaultDrawingSupplier.createStandardSeriesShapes();

		brewedColors = ColorBrewer.instance().getPalette("Paired").getColors();
	}

	@Override
	public void actionPerformed(ActionEvent event) {

		String command = event.getActionCommand();
		
		if(handlePlotSaveExternally 
				&& (command.equals(ChartPanel.SAVE_COMMAND) 
					|| command.equals(MainActionCommands.SAVE_AS_PNG.getName()) 
					|| command.equals(MainActionCommands.SAVE_AS_SVG.getName()) 
					|| command.equals(MainActionCommands.SAVE_AS_PDF.name())))
				return;

		if (command.equals(ChartPanel.ZOOM_RESET_BOTH_COMMAND))
			this.restoreAutoBounds();

		else if (command.equals(ChartPanel.COPY_COMMAND))
			try {
				this.doCopy();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				//e1.printStackTrace();
			}
		else if (command.equals(ChartPanel.SAVE_COMMAND)) {

			try {
				this.doSaveAs();
			} catch (IOException e) {
				//e.printStackTrace();
			}
		}
		else if (command.equals(ChartPanel.PRINT_COMMAND))
			this.createChartPrintJob();

		else if (command.equals(ChartPanel.PROPERTIES_COMMAND))
			this.doEditChartProperties();

		else if (command.equals(LCMSPlotPanel.SETUP_AXES_COMMAND)) {

			AxesSetupDialog dialog = new AxesSetupDialog(plot);
			dialog.setVisible(true);
		}
		else if (command.equals(MainActionCommands.SHOW_PLOT_LEGEND_COMMAND.getName())) 
			showLegend();
		
		else if (command.equals(MainActionCommands.HIDE_PLOT_LEGEND_COMMAND.getName())) 
			hideLegend();
		
		else if (command.equals(MainActionCommands.SHOW_PLOT_LABELS_COMMAND.getName())) 
			showAnnotations();
		
		else if (command.equals(MainActionCommands.HIDE_PLOT_LABELS_COMMAND.getName())) 
			hideAnnotations();
		
		else
			super.actionPerformed(event);
	}

	public void addDoubleClickReset() {
		
		if(doubleClickResetChartMouseListener == null)
			doubleClickResetChartMouseListener = 
				new DoubleClickResetChartMouseListener(this);

		this.addChartMouseListener(doubleClickResetChartMouseListener);
	}
	
	public void removeDoubleClickReset() {
		
		if(doubleClickResetChartMouseListener != null)
			this.removeChartMouseListener(doubleClickResetChartMouseListener);
	}
	
	public void disableMouseInputs() {
		
		setMouseZoomable(false);
		setMouseWheelEnabled(false);
		setPopupMenu(null);
		plot.setRangePannable(false);
		plot.setDomainPannable(false);
		removeDoubleClickReset();
		suppressMouseClicks = true;
	}

	public boolean areAnnotationsVisible() {
		return annotationsVisible;
	}

	public boolean areDataPointsVisible() {
		return dataPointsVisible;
	}

	/**
	 * @return the legend
	 */
	public LegendTitle getLegend() {
		return legend;
	}

	public Plot getPlot() {

		return plot;
	}

	protected abstract void initAxes();

	protected abstract void initChart();

	protected void initLegend(RectangleEdge legendPosition, boolean legendVisible2) {

		if (chart.getLegend() != null) {

			legend = chart.getLegend();
			legend.setItemFont(LEGEND_FONT);
			legend.setFrame(BlockBorder.NONE);
			legend.setPosition(legendPosition);

			legendVisible = legendVisible2;

			if (!legendVisible)
				chart.removeLegend();
		}
	}

	protected abstract void initPlot();

	protected void initTitles() {

		chartTitle = new TextTitle();
		chartTitle.setMargin(5, 0, 0, 0);
		chartTitle.setFont(TITLE_FONT);
		chart.setTitle(chartTitle);

		chartSubTitle = new TextTitle();
		chartSubTitle.setMargin(5, 0, 0, 0);
		chartSubTitle.setFont(SUBTITLE_FONT);
		chart.addSubtitle(chartSubTitle);
	}

	public boolean isLegendVisible() {
		return legendVisible;
	}

	@Override
	public void mouseClicked(MouseEvent event) {
		
		if(suppressMouseClicks)
			return;

		// let the parent handle the event (selection etc.)
		super.mouseClicked(event);

		// request focus to receive key events
		requestFocus();
	}

	public abstract void removeAllDataSets();

	/**
	 * @param legend
	 *            the legend to set
	 */
	public void setLegend(LegendTitle legend) {
		this.legend = legend;
	}

	public void setTitle(String title, String subTitle) {

		chartTitle.setText(title);
		chartSubTitle.setText(subTitle);
	}

	public void showLegend() {

		if (legendVisible)
			return;
		else {
			chart.addLegend(legend);
			legendVisible = true;
		}
	}

	public void hideLegend() {

		if (!legendVisible)
			return;
		else {
			chart.removeLegend();
			legendVisible = false;
		}
	}
	
	public Paint getSeriesPaint(int series) {

		return paintArray[series % paintArray.length];
	}

	public Shape getSeriesShape(int series) {

		return shapeArray[series % shapeArray.length];
	}

	public void showAnnotations() {
		
		if(annotationsVisible)
			return;
		else {
			for (int i = 0; i < plot.getDatasetCount(); i++)
				plot.getRenderer(i).setDefaultItemLabelsVisible(true);
			
			annotationsVisible = true;
		}
	}

	public void hideAnnotations() {
		
		if(!annotationsVisible)
			return;
		else {
			for (int i = 0; i < plot.getDatasetCount(); i++)
				plot.getRenderer(i).setDefaultItemLabelsVisible(false);
			
			annotationsVisible = false;
		}
	}
	
	public static Color getColor(int seriesNumber) {

		int j = 0;
		if (seriesNumber < LCMSPlotPanel.PLOT_COLORS.length) {
			j = seriesNumber;
		} else {
			j = seriesNumber - Math.floorDiv(seriesNumber, LCMSPlotPanel.PLOT_COLORS.length) * LCMSPlotPanel.PLOT_COLORS.length;
		}
		return LCMSPlotPanel.PLOT_COLORS[j];
	}

	public static Color getBrewerColor(int seriesNumber) {

		int j = 0;
		if (seriesNumber < brewedColors.length) {
			j = seriesNumber;
		} else {
			j = seriesNumber - Math.floorDiv(seriesNumber, brewedColors.length) * brewedColors.length;
		}
		return brewedColors[j];
	}
	
	protected Point2D getChartPointAtMouse(ChartMouseEvent e) {
		
		double x = plot.getDomainAxis().java2DToValue(e.getTrigger().getX(), getScreenDataArea(), plot.getDomainAxisEdge());
		double y = plot.getRangeAxis().java2DToValue(e.getTrigger().getY(), getScreenDataArea(), plot.getRangeAxisEdge());
		return new Point2D.Double(x,y);
	}
	
	protected Point2D getChartPointForCoordinates(double x, double y) {
		
		double xx = plot.getDomainAxis().java2DToValue(x, getScreenDataArea(), plot.getDomainAxisEdge());
		double yy = plot.getRangeAxis().java2DToValue(y, getScreenDataArea(), plot.getRangeAxisEdge());
		return new Point2D.Double(xx,yy);
	}
	
    protected Point2D getPointInRectangle(int x, int y, Rectangle2D area) {
        double xx = Math.max(area.getMinX(), Math.min(x, area.getMaxX()));
        double yy = Math.max(area.getMinY(), Math.min(y, area.getMaxY()));
        return new Point2D.Double(xx, yy);
    }
    
	protected Point2D getJavaPointForChartCoordinates(double chartX, double chartY) {
		
		Point p = new Point();
		p.setLocation(chartX, chartY);		
		return translateScreenToJava2D(p);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public synchronized void removeMarkers() {
		
		Collection domainMarkers = plot.getDomainMarkers(Layer.FOREGROUND);			
		if(domainMarkers != null && !domainMarkers.isEmpty()) {
			
			for (Object m : domainMarkers) 
				 plot.removeDomainMarker((Marker) m, Layer.FOREGROUND);	
		}
		
		Collection rangeMarkers = plot.getRangeMarkers(Layer.FOREGROUND);		
		if(rangeMarkers != null && !rangeMarkers.isEmpty()) {
			
			for (Object m : rangeMarkers) 
				 plot.removeRangeMarker((Marker) m, Layer.FOREGROUND);	
		}	
	}

	public void setSuppressMouseClicks(boolean suppressMouseClicks) {
		this.suppressMouseClicks = suppressMouseClicks;
	}

	public boolean isHandlePlotSaveExternally() {
		return handlePlotSaveExternally;
	}

	public void setHandlePlotSaveExternally(boolean handlePlotSaveExternally) {
		this.handlePlotSaveExternally = handlePlotSaveExternally;
	}
	
	public void saveChartAsImageToFile(
			File parentFolder, 
			String defaultFileName, 
			ImageExportFormat format) {
		
		//	JPG not supported
		if(format.equals(ImageExportFormat.JPG))
			return;
		
		if(parentFolder == null)
			parentFolder = new File(MRC2ToolBoxConfiguration.getDefaultExperimentsDirectory());
		if(defaultFileName == null || defaultFileName.isBlank())
			defaultFileName = "New plot-" + FIOUtils.getTimestamp() + "." + format.getExtension();
		
		JnaFileChooser fc = new JnaFileChooser(parentFolder);
		fc.setMode(JnaFileChooser.Mode.Files);
		fc.addFilter(format.getName(), format.getExtension());
		fc.setTitle("Save plot to file:");
		fc.setMultiSelectionEnabled(false);
		fc.setDefaultFileName(defaultFileName);
		
		if (fc.showSaveDialog(SwingUtilities.getWindowAncestor(this))) {
			
			File outputFile = fc.getSelectedFile();
			outputFile = FIOUtils.changeExtension(outputFile, format.getExtension());
			
			if(format.equals(ImageExportFormat.PNG))
				saveChartAsPNG(outputFile);
			
			if(format.equals(ImageExportFormat.PDF))
				saveChartAsPDF(outputFile);
			
			if(format.equals(ImageExportFormat.SVG))
				saveChartAsSVG(outputFile);
		}		
	}
	
	/**
	 * Writes the current chart to the specified file in PNG format.
	 */
	public void saveChartAsPNG(File file) {
		
		if (file == null)
			return;
		
        try {
			ChartUtils.saveChartAsPNG(file, this.chart, getWidth(), getHeight());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Writes the current chart to the specified file in PDF format.
	 */
	public void saveChartAsPDF(File file) {

		if (file == null)
			return;
		
		PDFDocument pdfDoc = new PDFDocument();
		Page graphPage = pdfDoc.createPage(new Rectangle(getWidth(), getHeight()));
		Graphics2D g2 = graphPage.getGraphics2D();

		// we suppress shadow generation, because PDF is a vector format and
		// the shadow effect is applied via bitmap effects...
		g2.setRenderingHint(JFreeChart.KEY_SUPPRESS_SHADOW_GENERATION, true);

		Rectangle2D drawArea = new Rectangle2D.Double(0, 0, getWidth(), getHeight());
		this.chart.draw(g2, drawArea);
		pdfDoc.writeToFile(file);
	}

	/**
	 * Writes the current chart to the specified file in SVG format.
	 */
	public void saveChartAsSVG(File file) {

		if (file == null)
			return;

		SVGGraphics2D g2 = new SVGGraphics2D(getWidth(), getHeight());
		// we suppress shadow generation, because SVG is a vector format and
		// the shadow effect is applied via bitmap effects...
		g2.setRenderingHint(JFreeChart.KEY_SUPPRESS_SHADOW_GENERATION, true);

		Rectangle2D drawArea = new Rectangle2D.Double(0, 0, getWidth(), getHeight());
		this.chart.draw(g2, drawArea);

		String svg = g2.getSVGDocument();

		if (svg != null && !svg.isBlank()) {

			try {
				Files.writeString(
						file.toPath(), 
						svg, 
						StandardCharsets.UTF_8, 
						StandardOpenOption.CREATE,
						StandardOpenOption.TRUNCATE_EXISTING);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
























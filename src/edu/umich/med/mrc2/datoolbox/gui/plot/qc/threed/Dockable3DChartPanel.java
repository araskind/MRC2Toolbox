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

package edu.umich.med.mrc2.datoolbox.gui.plot.qc.threed;

import java.awt.BorderLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.Icon;

import org.jfree.chart3d.Chart3D;
import org.jfree.chart3d.Chart3DFactory;
import org.jfree.chart3d.Chart3DPanel;
import org.jfree.chart3d.Colors;
import org.jfree.chart3d.data.xyz.XYZDataset;
import org.jfree.chart3d.data.xyz.XYZSeries;
import org.jfree.chart3d.data.xyz.XYZSeriesCollection;
import org.jfree.chart3d.graphics3d.Dimension3D;
import org.jfree.chart3d.graphics3d.ViewPoint3D;
import org.jfree.chart3d.label.StandardXYZLabelGenerator;
import org.jfree.chart3d.plot.XYZPlot;
import org.ujmp.core.Matrix;
import org.ujmp.core.calculation.Calculation.Ret;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignFactor;
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignSubset;
import edu.umich.med.mrc2.datoolbox.data.enums.FileSortingOrder;
import edu.umich.med.mrc2.datoolbox.data.enums.PlotDataGrouping;
import edu.umich.med.mrc2.datoolbox.data.enums.ThreeDChartType;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.plot.dataset.PlotDataSetUtils;
import edu.umich.med.mrc2.datoolbox.gui.plot.threed.ScatterThreeDrenderer;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class Dockable3DChartPanel  extends DefaultSingleCDockable implements ActionListener {

	private static final Icon componentIcon = GuiUtils.getIcon("scatterPlot3D", 16);

	private Chart3D chart;
	private Chart3DPanel chartPanel;
	private ThreeDplotToolbar toolbar;
	private double zoomMultiplier = 0.95;
	private PlotDataGrouping groupingType;
	private ExperimentDesignFactor category;
	private ExperimentDesignFactor subCategory;
	private ThreeDChartType chartType;
	private boolean splitByBatch;

	private Matrix projection;
	private DataPipeline aciveDataPipeline;
	private ExperimentDesignSubset activeDesign;
	private Collection<DataFile>dataFiles;

	public Dockable3DChartPanel(String id, String title) {

		super(id, componentIcon, title, null, Permissions.MIN_MAX_STACK);
		setCloseable(false);

		setLayout(new BorderLayout(0, 0));
		chartType = ThreeDChartType.PCA;
        toolbar = new ThreeDplotToolbar(this);
        add(toolbar, BorderLayout.NORTH);

        chart = Chart3DFactory.createScatterChart(
        		ThreeDChartType.PCA.getName(),  null, createInitDataset(), 
        		"Component 1", "Component 2", "Component 3");

        XYZPlot plot = (XYZPlot) chart.getPlot();
        plot.setDimensions(new Dimension3D(10.0, 10.0, 10.0));
        plot.setLegendLabelGenerator(
        		new StandardXYZLabelGenerator(StandardXYZLabelGenerator.COUNT_TEMPLATE));
        ScatterThreeDrenderer renderer = new ScatterThreeDrenderer();
        renderer.setSize(0.25);
        renderer.setColors(Colors.createIntenseColors());
        plot.setRenderer(renderer);

        chart.setViewPoint(ViewPoint3D.createAboveLeftViewPoint(40));
        chartPanel = new Chart3DPanel(chart);
        toolbar.setChartPanel(chartPanel);
        add(chartPanel, BorderLayout.CENTER);

        clearPlotPanel();
	}

	public void updateParametersFromToolbar() {

		groupingType = toolbar.getDataGroupingType();
		category = toolbar.getCategory();
		subCategory = toolbar.getSubCategory();
		splitByBatch = toolbar.splitByBatch();
	}

	public void clearPlotPanel(){

		projection = null;
		aciveDataPipeline = null;
		activeDesign = null;

		XYZPlot plot = (XYZPlot) chart.getPlot();
		plot.setDataset(new XYZSeriesCollection<String>());
		chart.setViewPoint(ViewPoint3D.createAboveLeftViewPoint(40));
	}

	public void redrawPlot() {

		if(chartType.equals(ThreeDChartType.PCA)) {

			if(projection != null && aciveDataPipeline != null && activeDesign != null)
				showPca(projection, aciveDataPipeline, activeDesign);
		}
	}

	public void showPca(
			Matrix projection, 
			DataPipeline aciveDataPipeline, 
			ExperimentDesignSubset activeDesign) {

		this.projection = projection;
		this.aciveDataPipeline = aciveDataPipeline;
		this.activeDesign = activeDesign;

		if(groupingType == null)
			updateParametersFromToolbar();

		Object[] filesMatrix = projection.getMetaDataDimensionMatrix(1).transpose(Ret.NEW).toObjectArray()[0];
		dataFiles = new ArrayList<DataFile>();
		for(Object o : filesMatrix)
			dataFiles.add((DataFile) o);

		Matrix projNorm = projection.normalize(Ret.NEW, 0);
		projNorm.setMetaDataDimensionMatrix(1, projection.getMetaDataDimensionMatrix(1));

		Map<String, DataFile[]> dataMap = PlotDataSetUtils.createSeriesFileMap(
										aciveDataPipeline,
										dataFiles,
										FileSortingOrder.NAME,
										activeDesign,
										groupingType,
										category,
										subCategory);

		XYZSeriesCollection<String> dataset = new XYZSeriesCollection<String>();
		for (Entry<String, DataFile[]> entry :dataMap.entrySet()) {

			XYZSeries<String> series = new XYZSeries<String>(entry.getKey());
			long fileRow;

			for(DataFile f : entry.getValue()) {

				fileRow = projection.getRowForLabel(f);
				double x = projNorm.getAsDouble(new long[] {fileRow,0}) * 80;
				double y = projNorm.getAsDouble(new long[] {fileRow,1}) * 80;
				double z = projNorm.getAsDouble(new long[] {fileRow,2}) * 80;

				series.add(x, y, z);
			}
			dataset.add(series);
		}
		XYZPlot plot = (XYZPlot)chart.getPlot();
		plot.setDataset(dataset);
//		double maxVal = projNorm.getMaxValue() * 1.05;
//		plot.setDimensions(new Dimension3D(maxVal, maxVal, maxVal));
	}

	@Override
	public void actionPerformed(ActionEvent event) {

		String command = event.getActionCommand();

		if (command.equals(MainActionCommands.COPY_AS_IMAGE.getName()))
			copyImage();

		if (command.equals(MainActionCommands.ZOOM_IN.getName()))
			zoom(1);

		if (command.equals(MainActionCommands.ZOOM_OUT.getName()))
			zoom(-1);

		if (command.equals(MainActionCommands.RESET_ZOOM.getName()))
			chartPanel.zoomToFit();

		if (command.equals(MainActionCommands.ROTATE_RIGHT.getName()))
			chartPanel.panLeftRight(-chartPanel.getPanIncrement());

		if (command.equals(MainActionCommands.ROTATE_LEFT.getName()))
			chartPanel.panLeftRight(chartPanel.getPanIncrement());

		if (command.equals(MainActionCommands.ROTATE_FORWARD.getName()))
			upDown(-1);

		if (command.equals(MainActionCommands.ROTATE_BACKWARD.getName()))
			upDown(1);

		if (command.equals(MainActionCommands.ROLL_LEFT.getName()))
			roll(-1);

		if (command.equals(MainActionCommands.ROLL_RIGHT.getName()))
			roll(1);
	}

    public void copyImage() {

        Clipboard systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        Insets insets = chartPanel.getInsets();
        int w = chartPanel.getWidth() - insets.left - insets.right;
        int h = chartPanel.getHeight() - insets.top - insets.bottom;
        Chart3DTransferable selection = new Chart3DTransferable(this.chart, w, h);
        systemClipboard.setContents(selection, null);
    }

    public void roll(int direction) {

    	double rollIncrement = chartPanel.getRollIncrement();
    	if(direction < 0)
    		rollIncrement = -chartPanel.getRollIncrement();

    	chartPanel.getViewPoint().roll(rollIncrement);
    	chartPanel.repaint();
    }

    public void upDown(int direction) {

    	double delta = chartPanel.getRotateIncrement();
    	if(direction < 0)
    		delta = -chartPanel.getRotateIncrement();

    	chartPanel.getViewPoint().moveUpDown(delta);
    	chartPanel.repaint();
    }

    public void zoom(int direction) {

    	double multiplier = zoomMultiplier;
    	if(direction < 0)
    		multiplier = 1.0d / zoomMultiplier;

        ViewPoint3D viewPt = chartPanel.getViewPoint();
        double minDistance = chartPanel.getMinViewingDistance();
        double maxDistance = minDistance  * chartPanel.getMaxViewingDistanceMultiplier();
        double valRho = Math.max(minDistance,  Math.min(maxDistance, viewPt.getRho() * multiplier));
        chartPanel.getViewPoint().setRho(valRho);
        chartPanel.repaint();
    }

	/**
	 * @return the zoomMultiplier
	 */
	public double getZoomMultiplier() {
		return zoomMultiplier;
	}

	/**
	 * @param zoomMultiplier the zoomMultiplier to set
	 */
	public void setZoomMultiplier(double zoomMultiplier) {
		this.zoomMultiplier = zoomMultiplier;
	}

	/**
	 * @return the chartPanel
	 */
	public Chart3DPanel getChartPanel() {
		return chartPanel;
	}
	
	public void updateGuiFromExperimentAndDataPipeline(
			DataAnalysisProject experiment, DataPipeline activeDataPipeline) {
		if(experiment != null)
			toolbar.populateCategories(experiment.getExperimentDesign().getActiveDesignSubset());

		redrawPlot();
	}

    public XYZDataset<String> createInitDataset() {

    	XYZSeriesCollection<String> dataset = new XYZSeriesCollection<String>();
//    	XYZSeries<String> s = new XYZSeries<String>("Series1");
//    	s.add(Math.random() * 100, Math.random() / 100, Math.random() * 100);
//    	dataset.add(s);

    	for(int i=1; i<5; i++) {

    		XYZSeries<String> s = createRandomSeries("S-"+i, i*20);
    		dataset.add(s);
    	}
        return dataset;
    }

    private XYZSeries<String> createRandomSeries(String name, int count) {

        XYZSeries<String> s = new XYZSeries<String>(name);

        for (int i = 0; i < count; i++)
            s.add(Math.random() * 100, Math.random() * 100, Math.random() * 100);

        return s;
    }
}






















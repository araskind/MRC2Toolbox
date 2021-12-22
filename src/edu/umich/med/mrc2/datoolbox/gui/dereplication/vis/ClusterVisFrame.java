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

package edu.umich.med.mrc2.datoolbox.gui.dereplication.vis;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.WindowConstants;

import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer;
import org.jfree.chart.ChartMouseListener;
import org.ujmp.core.Matrix;

import edu.umich.med.mrc2.datoolbox.data.IndexedDoublePoint;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureCluster;
import edu.umich.med.mrc2.datoolbox.gui.dereplication.clustering.CorrelationDisplay;
import edu.umich.med.mrc2.datoolbox.gui.dereplication.vis.heatmap.JFHeatChart;
import edu.umich.med.mrc2.datoolbox.gui.dereplication.vis.htree.AverageLinkageStrategy;
import edu.umich.med.mrc2.datoolbox.gui.dereplication.vis.htree.Cluster;
import edu.umich.med.mrc2.datoolbox.gui.dereplication.vis.htree.DefaultClusteringAlgorithm;
import edu.umich.med.mrc2.datoolbox.gui.dereplication.vis.htree.visualization.DendrogramPanel;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.plot.ColorGradient;
import edu.umich.med.mrc2.datoolbox.gui.plot.ColorScale;
import edu.umich.med.mrc2.datoolbox.gui.plot.HeatMapDataRange;
import edu.umich.med.mrc2.datoolbox.gui.plot.dataset.CorrelationMapDataSet;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class ClusterVisFrame extends JFrame implements ActionListener, ItemListener {

	/**
	 *
	 */
	private static final long serialVersionUID = -2100097403444093083L;

	private JLabel label;
	private ClusterVisToolbar heatMapToolbar;
	private KMeansPlusPlusClusterer<IndexedDoublePoint> clusterer;

	private MsFeatureCluster currentCluster;
	private DendrogramPanel dendrogramm;
	private DefaultClusteringAlgorithm clusteringAlg;

	private CorrelationDisplay activeData;

	private JScrollPane scrollPane;
	private JPanel panel;
	private JFHeatChart jfHeatChart;
	private CorrelationMapDataSet mapDataSet;
	private ChartMouseListener chartMouseListener;

	private static final Icon heatmapIcon = GuiUtils.getIcon("heatmap", 32);

	public ClusterVisFrame(ChartMouseListener chartMouseListener) {

		super("Correlation data");
		setIconImage(((ImageIcon) heatmapIcon).getImage());

		setSize(new Dimension(400, 400));
		setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		heatMapToolbar = new ClusterVisToolbar(this);
		getContentPane().add(heatMapToolbar, BorderLayout.NORTH);
		panel = new JPanel();
		panel.setBackground(Color.WHITE);
		scrollPane = new JScrollPane(panel);
		scrollPane.setViewportView(panel);
		panel.setLayout(new BorderLayout(0, 0));
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		getContentPane().add(scrollPane, BorderLayout.CENTER);

		this.chartMouseListener = chartMouseListener;

		// Heatmap
		label = new JLabel("");
		label.setBackground(Color.WHITE);

		// HeatChart
		jfHeatChart = new JFHeatChart();

		// Dendrogramm
		dendrogramm = new DendrogramPanel();
		dendrogramm.setBackground(Color.YELLOW);
		dendrogramm.setLineColor(Color.BLACK);
		dendrogramm.setScaleValueDecimals(0);
		dendrogramm.setScaleValueInterval(1);
		dendrogramm.setShowDistances(false);
		clusteringAlg = new DefaultClusteringAlgorithm();

		activeData = CorrelationDisplay.HETMAP;
		setVisible(false);
	}

	@Override
	public void actionPerformed(ActionEvent event) {

		String command = event.getActionCommand();

		if (command.equals(MainActionCommands.SHOW_HEATMAP_COMMAND.getName()))
			showHeatmap();

		if (command.equals(MainActionCommands.SHOW_DENDROGRAMM_COMMAND.getName()))
			showDendrogramm();
	}

	public synchronized void clearPanel() {

		panel.removeAll();
		label.setIcon(null);
		panel.revalidate();
		panel.repaint();
	}

	public void createDendrogramm() {

		Matrix mms = currentCluster.getCorrMatrix().getMetaDataDimensionMatrix(0);
		String[] names = new String[(int) mms.getColumnCount()];
		Object[] features = new Object[(int) mms.getColumnCount()];
		long[] coordinates = new long[2];
		coordinates[0] = 0;

		for (int i = 0; i < mms.getColumnCount(); i++) {

			coordinates[1] = i;
			MsFeature labelFeature = (MsFeature) mms.getAsObject(coordinates);
			names[i] = labelFeature.getName();
			features[i] = labelFeature;
		}
		Cluster cluster;
		try {
			cluster = clusteringAlg.performClustering(currentCluster.getInverseCorrMatrix().toDoubleArray(), names,
					features, new AverageLinkageStrategy());
			dendrogramm.setModel(cluster);
			dendrogramm.repaint();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void createHeatmap() {

		mapDataSet = new CorrelationMapDataSet(currentCluster);
		jfHeatChart.setDataSet(mapDataSet);
		jfHeatChart.addChartMouseListener(chartMouseListener);
	}

	public void showCorrelationData(MsFeatureCluster cluster) {

		currentCluster = cluster;
		createHeatmap();
		createDendrogramm();

		if (activeData.equals(CorrelationDisplay.HETMAP))
			showHeatmap();

		if (activeData.equals(CorrelationDisplay.DENDROGRAMM))
			showDendrogramm();
	}

	private void showDendrogramm() {

		panel.removeAll();
		panel.add(dendrogramm, BorderLayout.CENTER);
		panel.revalidate();
		panel.repaint();
		dendrogramm.revalidate();
		dendrogramm.repaint();

		activeData = CorrelationDisplay.DENDROGRAMM;
	}

	private void showHeatmap() {

		panel.removeAll();
		panel.add(jfHeatChart, BorderLayout.CENTER);
		panel.revalidate();
		panel.repaint();

		activeData = CorrelationDisplay.HETMAP;
	}

	@Override
	public void itemStateChanged(ItemEvent event) {

		if(event.getStateChange() == ItemEvent.SELECTED) {

			if (event.getItem() instanceof ColorGradient)
				jfHeatChart.setColorPalette((ColorGradient)event.getItem());

			if (event.getItem() instanceof ColorScale)
				jfHeatChart.setColorScale((ColorScale)event.getItem());

			if (event.getItem() instanceof HeatMapDataRange)
				jfHeatChart.setDataRange((HeatMapDataRange)event.getItem());
		}
	}
}


















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

package edu.umich.med.mrc2.datoolbox.misctest;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;

import edu.umich.med.mrc2.datoolbox.data.MsPoint;
import edu.umich.med.mrc2.datoolbox.gui.plot.LCMSPlotPanel;
import edu.umich.med.mrc2.datoolbox.gui.plot.LCMSPlotToolbar;
import edu.umich.med.mrc2.datoolbox.gui.plot.PlotType;
import edu.umich.med.mrc2.datoolbox.gui.plot.dataset.MsDataSet;
import edu.umich.med.mrc2.datoolbox.gui.plot.renderer.MassSpectrumRenderer;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;


public class HeadToTailTest extends JFrame implements ActionListener, WindowListener{

	/**
	 *
	 */
	private static final long serialVersionUID = -3344684193071637179L;
	public static void main(String[] args) {

		HeadToTailTest sm = new HeadToTailTest();
		sm.setVisible(true);
	}

	private LCMSPlotToolbar msPlotToolbar;
	private LCMSPlotPanel msPlot;

	public HeadToTailTest() throws HeadlessException {
		super();
		try {
			// Set System L&F
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (UnsupportedLookAndFeelException e) {
			// handle exception
		} catch (ClassNotFoundException e) {
			// handle exception
		} catch (InstantiationException e) {
			// handle exception
		} catch (IllegalAccessException e) {
			// handle exception
		}
		MRC2ToolBoxConfiguration.initConfiguration();
		setSize(new Dimension(600, 400));
		setPreferredSize(new Dimension(600, 400));

		msPlot = new LCMSPlotPanel(PlotType.SPECTRUM);
		msPlotToolbar = new LCMSPlotToolbar(msPlot, this);

		JPanel panel = new JPanel();
		getContentPane().add(panel, BorderLayout.CENTER);
		panel.setLayout(new BorderLayout(0, 0));
		panel.add(msPlot, BorderLayout.CENTER);
		panel.add(msPlotToolbar, BorderLayout.NORTH);
		createDataSet();
		pack();
	}

	private void createDataSet() {

		Collection<MsPoint> featurePoints = new ArrayList<MsPoint>();
		Collection<MsPoint> libraryPoints = new ArrayList<MsPoint>();

		featurePoints.add(new MsPoint(120.0d, 1000.0d));
		featurePoints.add(new MsPoint(121.0d, 100.0d));
		featurePoints.add(new MsPoint(122.0d, 10.0d));

		libraryPoints.add(new MsPoint(120.0d, 1000.0d));
		libraryPoints.add(new MsPoint(121.0d, 100.0d));
		libraryPoints.add(new MsPoint(122.0d, 10.0d));

		MsDataSet msDataSet = new MsDataSet(
				featurePoints,
				libraryPoints,
				"Feature",
				"Library",
				true);

		MassSpectrumRenderer msRenderer = msPlot.getDefaultMsRenderer();
		msRenderer.setSeriesPaint(0, Color.BLUE);
		msRenderer.setSeriesPaint(1, Color.RED);
		((XYPlot) msPlot.getPlot()).setRenderer(1, msRenderer);
		((XYPlot) msPlot.getPlot()).setDataset(1, msDataSet);

		ValueMarker marker = new ValueMarker(0.0d);
		marker.setPaint(Color.black);
		((XYPlot) msPlot.getPlot()).addRangeMarker(marker);
	}

	@Override
	public void windowOpened(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowClosing(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowClosed(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowIconified(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowDeiconified(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowActivated(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowDeactivated(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub

	}

}

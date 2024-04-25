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

package edu.umich.med.mrc2.datoolbox.gui.plot.lcms.spectrum;

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

import edu.umich.med.mrc2.datoolbox.data.LibraryMsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.gui.plot.PlotType;
import edu.umich.med.mrc2.datoolbox.gui.plot.lcms.LCMSPlotPanel;
import edu.umich.med.mrc2.datoolbox.gui.plot.lcms.LCMSPlotToolbar;

public class SpectrumPanel extends JPanel implements ActionListener {

	/**
	 *
	 */
	private static final long serialVersionUID = -5871970496622832245L;
	private JSplitPane splitPane;
	private boolean painted;
	private LCMSPlotPanel msPlot;
	private LCMSPlotToolbar msPlotToolbar;
	private JScrollPane msScrollPane;
	private MsTable msTable;

	public SpectrumPanel() {
		super();
		painted = false;
		initPanel();
	}

	public synchronized void clearPanel() {

		msPlot.removeAllDataSets();
		msTable.clearTable();
	}

	private void initPanel() {

		setLayout(new BorderLayout(0, 0));

		msPlot = new LCMSPlotPanel(PlotType.SPECTRUM);

		msPlotToolbar = 
				new LCMSPlotToolbar(msPlot, PlotType.SPECTRUM, this);
		add(msPlotToolbar, BorderLayout.NORTH);

		splitPane = new JSplitPane();
		splitPane.setDoubleBuffered(true);
		splitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
		splitPane.setDividerSize(10);
		splitPane.setOneTouchExpandable(true);
		add(splitPane, BorderLayout.CENTER);

		splitPane.setLeftComponent(msPlot);

		msTable = new MsTable();
		msScrollPane = new JScrollPane();
		msScrollPane.add(msTable);
		msScrollPane.setViewportView(msTable);
		msScrollPane.setPreferredSize(msTable.getPreferredScrollableViewportSize());
		splitPane.setRightComponent(msScrollPane);
	}

	@Override
	public void paint(Graphics g) {

		super.paint(g);

		if (!painted) {

			painted = true;

			splitPane.setDividerLocation(0.75);
			splitPane.setResizeWeight(0.75);
		}
	}

	public void showMsForFeature(MsFeature cf, boolean scaleMs) {

		//msPlot.showMsForFeature(cf, scaleMs);
		msTable.setTableModelFromFeature(cf, scaleMs);
	}

	public void showMsForFeatureList(Collection<MsFeature> featureList, boolean scaleMs) {

		//msPlot.showMsForFeatureList(featureList, scaleMs);
		msTable.setTableModelFromFeatureList(featureList, scaleMs);
	}

	public void showMsForLibraryFeature(LibraryMsFeature lt, boolean scaleMs) {

		//msPlot.showMsForLibraryFeature(lt, scaleMs);
		msTable.setTableModelFromFeature(lt, scaleMs);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub

	}
}













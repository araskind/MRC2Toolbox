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

import java.awt.LayoutManager;
import java.util.List;

import javax.swing.JPanel;

import org.jfree.chart.JFreeChart;

/**
 * A panel that is used in the demo applications.
 */
public class DemoPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8334842166058884989L;
	List charts;

	/**
	 * Creates a new demo panel with the specified layout manager.
	 *
	 * @param layout
	 *            the layout manager.
	 */
	public DemoPanel(LayoutManager layout) {
		super(layout);
		this.charts = new java.util.ArrayList();
	}

	/**
	 * Records a chart as belonging to this panel. It will subsequently be
	 * returned by the getCharts() method.
	 *
	 * @param chart
	 *            the chart.
	 */
	public void addChart(JFreeChart chart) {
		this.charts.add(chart);
	}

	/**
	 * Returns an array containing the charts within this panel.
	 *
	 * @return The charts.
	 */
	public JFreeChart[] getCharts() {
		int chartCount = this.charts.size();
		JFreeChart[] charts = new JFreeChart[chartCount];
		for (int i = 0; i < chartCount; i++) {
			charts[i] = (JFreeChart) this.charts.get(i);
		}
		return charts;
	}

}

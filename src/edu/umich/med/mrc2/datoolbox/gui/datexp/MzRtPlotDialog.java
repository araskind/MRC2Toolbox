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

package edu.umich.med.mrc2.datoolbox.gui.datexp;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collection;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import org.jfree.chart.plot.XYPlot;

import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.enums.DataScale;
import edu.umich.med.mrc2.datoolbox.gui.datexp.dataset.MsFeatureBubbleDataSet;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;

public class MzRtPlotDialog extends JDialog implements ItemListener, ActionListener {

	/**
	 *
	 */
	private static final long serialVersionUID = 5704068824887162571L;
	private DataExplorerPlotPanel plotPanel;
	private MzRtPlotToolbar toolbar;

	private static final Icon bubbleIcon = GuiUtils.getIcon("bubble", 32);

	public MzRtPlotDialog() {

		super(MRC2ToolBoxCore.getMainWindow(), "M/Z vs retention time plot", false);
		setIconImage(((ImageIcon) bubbleIcon).getImage());

		setSize(new Dimension(800, 600));
		setPreferredSize(new Dimension(800, 600));
		setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		getContentPane().setLayout(new BorderLayout(0, 0));

		JPanel panel = new JPanel(new BorderLayout(0, 0));
		//	panel.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(panel, BorderLayout.CENTER);

		plotPanel = new DataExplorerPlotPanel(DataExplorerPlotType.MZRT);
		panel.add(plotPanel, BorderLayout.CENTER);

		toolbar = new MzRtPlotToolbar(plotPanel, this, this);
		panel.add(toolbar, BorderLayout.NORTH);
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		// TODO Auto-generated method stub

	}

	public void loadFeatureCollection(String title, Collection<MsFeature>features) {

		plotPanel.removeAllDataSets();
		MsFeatureBubbleDataSet dataSet = new MsFeatureBubbleDataSet(title, features, DataScale.LN);
		((XYPlot) plotPanel.getPlot()).setDataset(0, dataSet);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
	}
}

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

package edu.umich.med.mrc2.datoolbox.gui.datexp.msone;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

import org.jfree.chart.plot.XYPlot;

import edu.umich.med.mrc2.datoolbox.gui.plot.PlotType;
import edu.umich.med.mrc2.datoolbox.gui.plot.lcms.LCMSPlotPanel;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;

public class MultispectraPlotPanel extends JPanel implements ActionListener, ItemListener, BackedByPreferences {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private MultispectraPlotPanelToolbar toolbar;
	private JPanel plotPanel;
	private Map<Object,LCMSPlotPanel>objectPlotMap;
	private int numColumns;
	private GridBagLayout gbl_plotPanel;
	
	private Preferences prefs;
	private static final String NUMBER_OF_COLUMNS = "NUMBER_OF_COLUMNS";

	public MultispectraPlotPanel() {
		super();
		setLayout(new BorderLayout(0,0));
		toolbar = new MultispectraPlotPanelToolbar(this);
		add(toolbar, BorderLayout.NORTH);
		plotPanel = new JPanel();
		plotPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		gbl_plotPanel = new GridBagLayout();
		plotPanel.setLayout(gbl_plotPanel);
		
		objectPlotMap = new HashMap<Object,LCMSPlotPanel>();
		
		loadPreferences();
		createGraphGrid();
		
		add(new JScrollPane(plotPanel), BorderLayout.CENTER);
	}
	
	private void createGraphGrid() {
		
		int numGraps = 20;
		int numRows = 1;
		if(numGraps > numColumns)
			numRows = Math.floorDiv(numGraps, numColumns) + 1;

		int count = 0;
		
		plotPanel.setPreferredSize(new Dimension(380, 1400));
		
		for(int i=0; i<numColumns; i++) {
			
			for(int j=0; j<numRows; j++) {
				
				count++;
				String gn = "Graph# "  + Integer.toString(count);
				LCMSPlotPanel spectrumPlot = new LCMSPlotPanel(PlotType.SPECTRUM);
				((XYPlot)spectrumPlot.getChart().getPlot()).getRangeAxis().setLabel(null);
				//spectrumPlot.setSize(new Dimension(120, 80));

				objectPlotMap.put(gn, spectrumPlot);
				
				GridBagConstraints gbc_spectrumPlot = new GridBagConstraints();
				gbc_spectrumPlot.fill = GridBagConstraints.BOTH;
				gbc_spectrumPlot.insets = new Insets(0, 0, 5, 5);
				gbc_spectrumPlot.gridx = i;
				gbc_spectrumPlot.gridy = j;
				plotPanel.add(spectrumPlot, gbc_spectrumPlot);
			}
		}		
		gbl_plotPanel.rowWeights = new double[numRows];
		Arrays.fill(gbl_plotPanel.rowWeights, 1.0d);
		gbl_plotPanel.columnWeights = new double[numColumns];
		Arrays.fill(gbl_plotPanel.columnWeights, 1.0d);
		
//		revalidate();
//		repaint();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub

	}
	
	@Override
	public void itemStateChanged(ItemEvent e) {
		// TODO Auto-generated method stub

	}
	
	@Override
	public void loadPreferences(Preferences preferences) {
		
		prefs = preferences;
		numColumns = prefs.getInt(NUMBER_OF_COLUMNS, 3);
	}

	@Override
	public void loadPreferences() {
		loadPreferences(Preferences.userNodeForPackage(this.getClass()));
	}

		@Override
	public void savePreferences() {

		prefs = Preferences.userNodeForPackage(this.getClass());
		prefs.putInt(NUMBER_OF_COLUMNS, numColumns);
	}
}

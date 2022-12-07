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

package edu.umich.med.mrc2.datoolbox.gui.idworks.summary;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Objects;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import edu.umich.med.mrc2.datoolbox.data.MsFeatureInfoBundleCollection;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;

public class DataSetStatsPanel extends JPanel implements ActionListener, ItemListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3189365893532024962L;

	private MsFeatureInfoBundleCollection activeFeatureCollection;
	private DataSetStatsPlotPanel dataSetStatsPlotPanel;
	
	private JLabel 
		dataSetNameLabel,
		createdByLabel,
		createdOnLabel,
		totalNumFeaturesLabel,
		numIdentifiedFeaturesLabel
		;
	
	public DataSetStatsPanel(MsFeatureInfoBundleCollection activeFeatureCollection) {
		
		super(new BorderLayout(0,0));
		this.activeFeatureCollection = activeFeatureCollection;
		dataSetStatsPlotPanel = new DataSetStatsPlotPanel(activeFeatureCollection);
		add(dataSetStatsPlotPanel, BorderLayout.CENTER);
		
		JPanel overviewPanel = new JPanel();
		overviewPanel.setBorder(
			new CompoundBorder(new EmptyBorder(10, 10, 10, 10), 
				new CompoundBorder(new TitledBorder(
					new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), 
							new Color(160, 160, 160)), "Overview", TitledBorder.LEADING, 
					TitledBorder.TOP, null, new Color(0, 0, 0)), new EmptyBorder(5, 5, 5, 5))));
		add(overviewPanel, BorderLayout.NORTH);
		GridBagLayout gbl_overviewPanel = new GridBagLayout();
		gbl_overviewPanel.columnWidths = new int[]{0, 0, 0, 0, 0};
		gbl_overviewPanel.rowHeights = new int[]{0, 0, 0, 0, 0};
		gbl_overviewPanel.columnWeights = new double[]{0.0, 1.0, 1.0, 1.0, Double.MIN_VALUE};
		gbl_overviewPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		overviewPanel.setLayout(gbl_overviewPanel);
		
		JLabel lblNewLabel = new JLabel("Data set name");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 0;
		overviewPanel.add(lblNewLabel, gbc_lblNewLabel);
		
		dataSetNameLabel = new JLabel("");
		dataSetNameLabel.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_dataSetNameLabel = new GridBagConstraints();
		gbc_dataSetNameLabel.gridwidth = 3;
		gbc_dataSetNameLabel.anchor = GridBagConstraints.WEST;
		gbc_dataSetNameLabel.insets = new Insets(0, 0, 5, 0);
		gbc_dataSetNameLabel.gridx = 1;
		gbc_dataSetNameLabel.gridy = 0;
		overviewPanel.add(dataSetNameLabel, gbc_dataSetNameLabel);
		
		JLabel lblNewLabel_4 = new JLabel("Created by");
		GridBagConstraints gbc_lblNewLabel_4 = new GridBagConstraints();
		gbc_lblNewLabel_4.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_4.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_4.gridx = 0;
		gbc_lblNewLabel_4.gridy = 1;
		overviewPanel.add(lblNewLabel_4, gbc_lblNewLabel_4);
		
		createdByLabel = new JLabel("");
		GridBagConstraints gbc_createdByLabel = new GridBagConstraints();
		gbc_createdByLabel.anchor = GridBagConstraints.WEST;
		gbc_createdByLabel.gridwidth = 3;
		gbc_createdByLabel.insets = new Insets(0, 0, 5, 0);
		gbc_createdByLabel.gridx = 1;
		gbc_createdByLabel.gridy = 1;
		overviewPanel.add(createdByLabel, gbc_createdByLabel);
		
		JLabel lblNewLabel_3 = new JLabel("Created on");
		GridBagConstraints gbc_lblNewLabel_3 = new GridBagConstraints();
		gbc_lblNewLabel_3.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_3.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_3.gridx = 0;
		gbc_lblNewLabel_3.gridy = 2;
		overviewPanel.add(lblNewLabel_3, gbc_lblNewLabel_3);
		
		createdOnLabel = new JLabel("");
		createdOnLabel.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_createdOnLabel = new GridBagConstraints();
		gbc_createdOnLabel.anchor = GridBagConstraints.WEST;
		gbc_createdOnLabel.insets = new Insets(0, 0, 5, 5);
		gbc_createdOnLabel.gridx = 1;
		gbc_createdOnLabel.gridy = 2;
		overviewPanel.add(createdOnLabel, gbc_createdOnLabel);
		
		JLabel lblNewLabel_1 = new JLabel("Total # of features");
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_1.insets = new Insets(0, 0, 0, 5);
		gbc_lblNewLabel_1.gridx = 0;
		gbc_lblNewLabel_1.gridy = 3;
		overviewPanel.add(lblNewLabel_1, gbc_lblNewLabel_1);
		
		totalNumFeaturesLabel = new JLabel("");
		totalNumFeaturesLabel.setMinimumSize(new Dimension(30, 14));
		totalNumFeaturesLabel.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_totalNumFeaturesLabel = new GridBagConstraints();
		gbc_totalNumFeaturesLabel.anchor = GridBagConstraints.WEST;
		gbc_totalNumFeaturesLabel.insets = new Insets(0, 0, 0, 5);
		gbc_totalNumFeaturesLabel.gridx = 1;
		gbc_totalNumFeaturesLabel.gridy = 3;
		overviewPanel.add(totalNumFeaturesLabel, gbc_totalNumFeaturesLabel);
		
		JLabel lblNewLabel_2 = new JLabel("# of identified features");
		GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
		gbc_lblNewLabel_2.insets = new Insets(0, 0, 0, 5);
		gbc_lblNewLabel_2.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_2.gridx = 2;
		gbc_lblNewLabel_2.gridy = 3;
		overviewPanel.add(lblNewLabel_2, gbc_lblNewLabel_2);
		
		numIdentifiedFeaturesLabel = new JLabel("");
		numIdentifiedFeaturesLabel.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_numIdentifiedFeaturesLabel = new GridBagConstraints();
		gbc_numIdentifiedFeaturesLabel.anchor = GridBagConstraints.WEST;
		gbc_numIdentifiedFeaturesLabel.gridx = 3;
		gbc_numIdentifiedFeaturesLabel.gridy = 3;
		overviewPanel.add(numIdentifiedFeaturesLabel, gbc_numIdentifiedFeaturesLabel);
		
		JPanel plotControlsPanel = new JPanel();
		plotControlsPanel.setBorder(
			new CompoundBorder(new EmptyBorder(10, 10, 10, 10), 
				new CompoundBorder(new TitledBorder(
					new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), 
						new Color(160, 160, 160)), "Plot controls", TitledBorder.LEADING, 
					TitledBorder.TOP, null, new Color(0, 0, 0)), new EmptyBorder(5, 5, 5, 5))));
		add(plotControlsPanel, BorderLayout.SOUTH);
		GridBagLayout gbl_plotControlsPanel = new GridBagLayout();
		gbl_plotControlsPanel.columnWidths = new int[]{0};
		gbl_plotControlsPanel.rowHeights = new int[]{0};
		gbl_plotControlsPanel.columnWeights = new double[]{Double.MIN_VALUE};
		gbl_plotControlsPanel.rowWeights = new double[]{Double.MIN_VALUE};
		plotControlsPanel.setLayout(gbl_plotControlsPanel);
		showDataSetInfo();
	}
	
	private void showDataSetInfo() {
		
		dataSetNameLabel.setText(activeFeatureCollection.getName());
		createdByLabel.setText(activeFeatureCollection.getOwner().getInfo());
		createdOnLabel.setText(
				MRC2ToolBoxConfiguration.getDateTimeFormat().format(activeFeatureCollection.getDateCreated()));
				
		totalNumFeaturesLabel.setText(Integer.toString(activeFeatureCollection.getFeatures().size()));
		long numIdentified = activeFeatureCollection.getFeatures().stream().
				filter(f -> Objects.nonNull(f.getMsFeature().getPrimaryIdentity())).count();
		
		numIdentifiedFeaturesLabel.setText(Long.toString(numIdentified));
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub

	}

}

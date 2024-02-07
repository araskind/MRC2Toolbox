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

package edu.umich.med.mrc2.datoolbox.gui.datexp.qchist;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureSet;
import edu.umich.med.mrc2.datoolbox.data.enums.MSFeatureSetStatisticalParameters;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class DockableQCHistogramPanel extends DefaultSingleCDockable 
								implements ActionListener, ItemListener {

	private static final Icon statsIcon = GuiUtils.getIcon("stats", 16);
	private QCHistogramPanel qcHistogramPanel;
	private JComboBox<MSFeatureSetStatisticalParameters> featureSetStatParamsComboBox;
	private MsFeatureSet featureSet;
	
	public DockableQCHistogramPanel() {

		super("DockableQCHistogramPanel", statsIcon, 
				"QC histograms", null, Permissions.MIN_MAX_STACK);
		setCloseable(false);
		
		setLayout(new BorderLayout(0, 0));
		qcHistogramPanel = new QCHistogramPanel(
				MSFeatureSetStatisticalParameters.PERCENT_MISSING_IN_SAMPLES);
		add(qcHistogramPanel, BorderLayout.CENTER);
		
		JPanel panel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		add(panel, BorderLayout.SOUTH);
		
		featureSetStatParamsComboBox = new JComboBox<MSFeatureSetStatisticalParameters>(
				new DefaultComboBoxModel<MSFeatureSetStatisticalParameters>(
						new MSFeatureSetStatisticalParameters[] {
								MSFeatureSetStatisticalParameters.PERCENT_MISSING_IN_SAMPLES,
								MSFeatureSetStatisticalParameters.PERCENT_MISSING_IN_POOLS
						}));
		featureSetStatParamsComboBox.setSelectedItem(
				MSFeatureSetStatisticalParameters.PERCENT_MISSING_IN_SAMPLES);
		featureSetStatParamsComboBox.addItemListener(this);
		panel.add(new JLabel("QC parameter to plot "));
		panel.add(featureSetStatParamsComboBox);
	}
	
	public void createQCvalueHistogram(MsFeatureSet featureSet) {
		
		this.featureSet = featureSet;
		MSFeatureSetStatisticalParameters par = 
				(MSFeatureSetStatisticalParameters) featureSetStatParamsComboBox.getSelectedItem();
		qcHistogramPanel.createQCvalueHistogram(featureSet, par);
	}
		
	@Override
	public void itemStateChanged(ItemEvent e) {

		if(featureSet == null)
			return;
		
		if (e.getStateChange() == ItemEvent.SELECTED) {
			
			if (e.getItem() instanceof MSFeatureSetStatisticalParameters) {
				
				qcHistogramPanel.createQCvalueHistogram(
						featureSet, (MSFeatureSetStatisticalParameters)e.getItem());
			}
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		//	TODO
	}
	
	public void clearPanel() {
		qcHistogramPanel.removeAllDataSets();
	}
}








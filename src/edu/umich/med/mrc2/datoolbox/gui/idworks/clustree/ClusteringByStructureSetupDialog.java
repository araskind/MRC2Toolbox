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

package edu.umich.med.mrc2.datoolbox.gui.idworks.clustree;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.prefs.Preferences;

import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;

import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.BasicDialogWithPreferences;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.utils.Range;

public class ClusteringByStructureSetupDialog extends BasicDialogWithPreferences{

	private static final long serialVersionUID = 1L;
	
	private Preferences preferences;
	private JFormattedTextField rtRangeStartField;
	private JFormattedTextField rtRangeEndField;
	private JFormattedTextField msmsRtGroupingWindowField;
	
	public static final String RT_START = "RT_START";
	public static final String RT_END = "RT_END";
	public static final String RT_WINDOW = "RT_WINDOW";
	
	public ClusteringByStructureSetupDialog(ActionListener actionListener) {
		super("Cluster features by structure",
				"clusterByStrructure",
				new Dimension(480, 250),
				actionListener);

		GridBagLayout gbl_dataPanel = new GridBagLayout();
		gbl_dataPanel.columnWidths = new int[]{0, 0, 0, 0, 0, 0, 0};
		gbl_dataPanel.rowHeights = new int[]{0, 0, 0};
		gbl_dataPanel.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_dataPanel.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		mainPanel.setLayout(gbl_dataPanel);
		
		JLabel lblNewLabel = new JLabel("RT range from ");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 0;
		mainPanel.add(lblNewLabel, gbc_lblNewLabel);
		
		rtRangeStartField = new JFormattedTextField(MRC2ToolBoxConfiguration.getRtFormat());
		rtRangeStartField.setPreferredSize(new Dimension(80, 20));
		rtRangeStartField.setMinimumSize(new Dimension(80, 20));
		rtRangeStartField.setColumns(10);
		GridBagConstraints gbc_rtRangeStartField = new GridBagConstraints();
		gbc_rtRangeStartField.insets = new Insets(0, 0, 5, 5);
		gbc_rtRangeStartField.fill = GridBagConstraints.HORIZONTAL;
		gbc_rtRangeStartField.gridx = 1;
		gbc_rtRangeStartField.gridy = 0;
		mainPanel.add(rtRangeStartField, gbc_rtRangeStartField);
		
		JLabel lblNewLabel_1 = new JLabel("to");
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_1.gridx = 2;
		gbc_lblNewLabel_1.gridy = 0;
		mainPanel.add(lblNewLabel_1, gbc_lblNewLabel_1);
		
		rtRangeEndField = new JFormattedTextField();
		rtRangeEndField.setMinimumSize(new Dimension(80, 20));
		rtRangeEndField.setPreferredSize(new Dimension(80, 20));
		GridBagConstraints gbc_formattedTextField = new GridBagConstraints();
		gbc_formattedTextField.insets = new Insets(0, 0, 5, 5);
		gbc_formattedTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_formattedTextField.gridx = 3;
		gbc_formattedTextField.gridy = 0;
		mainPanel.add(rtRangeEndField, gbc_formattedTextField);
		
		JLabel lblNewLabel_2 = new JLabel("min");
		GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
		gbc_lblNewLabel_2.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel_2.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_2.gridx = 4;
		gbc_lblNewLabel_2.gridy = 0;
		mainPanel.add(lblNewLabel_2, gbc_lblNewLabel_2);
		
		JButton btnNewButton = new JButton("Reset");
		GridBagConstraints gbc_btnNewButton = new GridBagConstraints();
		gbc_btnNewButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnNewButton.insets = new Insets(0, 0, 5, 0);
		gbc_btnNewButton.gridx = 5;
		gbc_btnNewButton.gridy = 0;
		mainPanel.add(btnNewButton, gbc_btnNewButton);
		
		JLabel lblNewLabel_3 = new JLabel("MSMS grouping window");
		GridBagConstraints gbc_lblNewLabel_3 = new GridBagConstraints();
		gbc_lblNewLabel_3.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_3.insets = new Insets(0, 0, 0, 5);
		gbc_lblNewLabel_3.gridx = 0;
		gbc_lblNewLabel_3.gridy = 1;
		mainPanel.add(lblNewLabel_3, gbc_lblNewLabel_3);
		
		msmsRtGroupingWindowField = new JFormattedTextField(MRC2ToolBoxConfiguration.getRtFormat());
		msmsRtGroupingWindowField.setPreferredSize(new Dimension(80, 20));
		msmsRtGroupingWindowField.setMinimumSize(new Dimension(80, 20));
		msmsRtGroupingWindowField.setColumns(10);
		GridBagConstraints gbc_rtRangeStartField_1 = new GridBagConstraints();
		gbc_rtRangeStartField_1.insets = new Insets(0, 0, 0, 5);
		gbc_rtRangeStartField_1.fill = GridBagConstraints.HORIZONTAL;
		gbc_rtRangeStartField_1.gridx = 1;
		gbc_rtRangeStartField_1.gridy = 1;
		mainPanel.add(msmsRtGroupingWindowField, gbc_rtRangeStartField_1);
		
		JLabel lblNewLabel_4 = new JLabel("min");
		GridBagConstraints gbc_lblNewLabel_4 = new GridBagConstraints();
		gbc_lblNewLabel_4.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel_4.insets = new Insets(0, 0, 0, 5);
		gbc_lblNewLabel_4.gridx = 2;
		gbc_lblNewLabel_4.gridy = 1;
		mainPanel.add(lblNewLabel_4, gbc_lblNewLabel_4);

		primaryActionButton.setText(
				MainActionCommands.CLUSTER_FEATURES_BY_STRUCTURE_COMMAND.getName());
		primaryActionButton.setActionCommand(
				MainActionCommands.CLUSTER_FEATURES_BY_STRUCTURE_COMMAND.getName());
		primaryActionButton.addActionListener(actionListener);

		loadPreferences();				
		pack();
		primaryActionButton.requestFocus();
	}
	
	public Range getRtRange() {
		
		Range rtRange = null;
		double rtStart = 0.0d;
		String rtStartString = rtRangeStartField.getText().trim();
		if(!rtStartString.isEmpty())
			rtStart = Double.parseDouble(rtStartString);

		double rtEnd = 0.0d;
		String rtEndString = rtRangeEndField.getText().trim();
		if(!rtEndString.isEmpty())
			rtEnd = Double.parseDouble(rtEndString);
		
		if(rtEnd > rtStart)
			rtRange = new Range(rtStart, rtEnd);
		
		return rtRange;
	}
		
	public double getMsmsRtGroupingWindow() {
		
		String windowString = msmsRtGroupingWindowField.getText().trim();
		if(windowString.isEmpty())
			return 0.0d;
		else
			return Double.parseDouble(windowString);
	}
		
	@Override
	public void loadPreferences(Preferences preferences) {
		
		this.preferences = preferences;
		double rtStart = preferences.getDouble(RT_START, 0.0d);
		rtRangeStartField.setText(Double.toString(rtStart));
		
		double rtEnd = preferences.getDouble(RT_END, 0.0d);
		rtRangeEndField.setText(Double.toString(rtEnd));
		
		double groupingWindow = preferences.getDouble(RT_WINDOW, 0.1d);
		msmsRtGroupingWindowField.setText(Double.toString(groupingWindow));
	}

	@Override
	public void loadPreferences() {
		loadPreferences(Preferences.userNodeForPackage(this.getClass()));		
	}

	@Override
	public void savePreferences() {

		preferences = Preferences.userNodeForPackage(this.getClass());
		Range rtRange = getRtRange();
		if(rtRange == null) {
			preferences.putDouble(RT_START, 0.0d);
			preferences.putDouble(RT_END, 0.0d);
		}
		else {
			preferences.putDouble(RT_START, rtRange.getMin());
			preferences.putDouble(RT_END, rtRange.getMax());
		}
		preferences.putDouble(RT_WINDOW, getMsmsRtGroupingWindow());		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
	}
}

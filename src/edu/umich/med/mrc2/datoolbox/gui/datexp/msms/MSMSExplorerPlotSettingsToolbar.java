/*******************************************************************************
 *
 * (C) Copyright 2018-2025 MRC2 (http://mrc2.umich.edu).
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

package edu.umich.med.mrc2.datoolbox.gui.datexp.msms;

import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;

import edu.umich.med.mrc2.datoolbox.data.enums.TableRowSubset;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.CommonToolbar;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.utils.Range;

public class MSMSExplorerPlotSettingsToolbar extends CommonToolbar{

	/**
	 *
	 */
	private static final long serialVersionUID = -8756954637925601169L;
	private static final Icon refreshDataIcon = GuiUtils.getIcon("rerun", 24);

	private JComboBox featureSubsetComboBox;
	private JButton recalculateButton;
	private JFormattedTextField startRtTextField, endRtTextField; 

	public MSMSExplorerPlotSettingsToolbar(ActionListener actionListener, ItemListener dropdownListener) {

		super(actionListener);
		
		add(new JLabel("MSMS feature subset: "));
		featureSubsetComboBox = new JComboBox<TableRowSubset>(
				new DefaultComboBoxModel<TableRowSubset>(TableRowSubset.values()));
		featureSubsetComboBox.setMinimumSize(new Dimension(200, 25));
		featureSubsetComboBox.setPreferredSize(new Dimension(200, 25));
		featureSubsetComboBox.addItemListener(dropdownListener);
		featureSubsetComboBox.setMaximumSize(new Dimension(120, 26));
		add(featureSubsetComboBox);
		
		addSeparator(buttonDimension);
		
		JLabel lblNewLabel = new JLabel("RT from ");
		add(lblNewLabel);
		
		startRtTextField = new JFormattedTextField(MRC2ToolBoxConfiguration.getRtFormat());
		startRtTextField.setPreferredSize(new Dimension(80, 25));
		startRtTextField.setSize(new Dimension(80, 25));
		startRtTextField.setMaximumSize(new Dimension(80, 25));
		startRtTextField.setColumns(6);
		add(startRtTextField);
		
		JLabel lblNewLabel_1 = new JLabel("  to ");
		add(lblNewLabel_1);
		
		endRtTextField = new JFormattedTextField(MRC2ToolBoxConfiguration.getRtFormat());
		endRtTextField.setPreferredSize(new Dimension(80, 25));
		endRtTextField.setColumns(6);
		endRtTextField.setMaximumSize(new Dimension(80, 25));
		endRtTextField.setSize(new Dimension(80, 25));
		add(endRtTextField);
		
		JLabel lblNewLabel_2 = new JLabel(" min");
		add(lblNewLabel_2);
		
		addSeparator(buttonDimension);
		
		recalculateButton = GuiUtils.addButton(this, null, refreshDataIcon, commandListener,
				MainActionCommands.REFRESH_MSMS_FEATURE_PLOT.getName(), 
				MainActionCommands.REFRESH_MSMS_FEATURE_PLOT.getName(), buttonDimension);
	}
	
	public void setTableRowSubset(TableRowSubset newSubset) {
		featureSubsetComboBox.setSelectedItem(newSubset);
	}
	
	public TableRowSubset getTableRowSubset() {
		return (TableRowSubset)featureSubsetComboBox.getSelectedItem();
	}

	public void setRtRange(Range rtRange) {
		
		startRtTextField.setText(Double.toString(rtRange.getMin()));
		endRtTextField.setText(Double.toString(rtRange.getMax()));
	}
	
	public Range getRtRange() {
		
		double startRt = 0.0d;
		if(!startRtTextField.getText().trim().isEmpty())
			startRt = Double.parseDouble(startRtTextField.getText().trim());
		
		double endRt = 0.0d;
		if(!endRtTextField.getText().trim().isEmpty())
			endRt = Double.parseDouble(endRtTextField.getText().trim());
		
		if(startRt <= endRt)
			return new Range(startRt, endRt);
		
		return null;
	}
	
	@Override
	public void updateGuiFromExperimentAndDataPipeline(
			DataAnalysisProject project, DataPipeline newDataPipeline) {
		// TODO Auto-generated method stub
		
	}
}



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

package edu.umich.med.mrc2.datoolbox.gui.integration;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;

import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;

import edu.umich.med.mrc2.datoolbox.data.MsFeatureClusterSet;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.dereplication.ClusterDisplayToolbar;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.SortedComboBoxModel;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class DataIntegratorToolbar extends ClusterDisplayToolbar {

	/**
	 *
	 */
	private static final long serialVersionUID = -1550999649378675670L;

	private static final Icon collectIDDataIcon = GuiUtils.getIcon("createIntegration", 32);
	private static final Icon deleteDataSetIcon = GuiUtils.getIcon("deleteIntegration", 32);
	private static final Icon acceptListIcon = GuiUtils.getIcon("acceptList", 32);

	private JButton
		collectIdentifiedCompoundDataButton,
		deleteDataSetButton,
		acceptListButton;

	private JComboBox activeSetcomboBox;

	public DataIntegratorToolbar(ActionListener commandListener) {

		super(commandListener);

		collectIdentifiedCompoundDataButton = GuiUtils.addButton(this, null, collectIDDataIcon, commandListener,
				MainActionCommands.DATA_INTEGRATION_DIALOG_COMMAND.getName(),
				MainActionCommands.DATA_INTEGRATION_DIALOG_COMMAND.getName(), buttonDimension);

		deleteDataSetButton = GuiUtils.addButton(this, null, deleteDataSetIcon, commandListener,
				MainActionCommands.DELETE_INTEGRATION_SET_COMMAND.getName(),
				MainActionCommands.DELETE_INTEGRATION_SET_COMMAND.getName(), buttonDimension);

		addSeparator(buttonDimension);

		acceptListButton = GuiUtils.addButton(this, null, acceptListIcon, commandListener,
				MainActionCommands.ACCEPT_CLEAN_ID_LIST_COMMAND.getName(),
				MainActionCommands.ACCEPT_CLEAN_ID_LIST_COMMAND.getName(), buttonDimension);

		JLabel lblActiveDataSet = new JLabel("Active data set  ");
		add(lblActiveDataSet);

		activeSetcomboBox = new JComboBox();
		activeSetcomboBox.setEditable(true);
		activeSetcomboBox.addItemListener((ItemListener) commandListener);
		activeSetcomboBox.setPreferredSize(new Dimension(150, 25));
		add(activeSetcomboBox);

		Component horizontalGlue = Box.createHorizontalGlue();
		add(horizontalGlue);
	}

	@Override
	public void updateGuiFromExperimentAndDataPipeline(DataAnalysisProject project, DataPipeline newDataPipeline) {

		boolean active = true;

		if(project == null)
			active = false;
		else {
			if(!project.projectHasData())
				active = false;
		}
		//	getIntergratedFeatureSets()
		collectIdentifiedCompoundDataButton.setEnabled(active);
		deleteDataSetButton.setEnabled(active);
		acceptListButton.setEnabled(active);

		if(project != null)
			deleteDataSetButton.setEnabled(project.getActiveIntegratedFeatureSet() != null);
	}

	@Override
	public void updateGuiFromActiveSet(MsFeatureClusterSet integratedSet) {

		activeSetcomboBox.removeItemListener((ItemListener) commandListener);

		DataAnalysisProject project = MRC2ToolBoxCore.getActiveMetabolomicsExperiment();

		if(project != null) {

			activeSetcomboBox.setModel(new SortedComboBoxModel<MsFeatureClusterSet>(project.getIntergratedFeatureSets()));
			activeSetcomboBox.setEnabled(true);
			collectIdentifiedCompoundDataButton.setEnabled(true);

			if(integratedSet != null) {

				if(project.getDataIntegrationClusterSets().contains(integratedSet)) {
					acceptListButton.setEnabled(false);
				}
				else {
					acceptListButton.setEnabled(true);
					activeSetcomboBox.addItem(integratedSet);
				}
				deleteDataSetButton.setEnabled(true);
				activeSetcomboBox.setSelectedItem(integratedSet);
			}
			else {
				activeSetcomboBox.setSelectedIndex(-1);
			}
		}
		else {
			activeSetcomboBox.setModel(new SortedComboBoxModel<MsFeatureClusterSet>());
			activeSetcomboBox.setEnabled(false);
			collectIdentifiedCompoundDataButton.setEnabled(false);
			deleteDataSetButton.setEnabled(false);
			acceptListButton.setEnabled(false);
		}
		activeSetcomboBox.addItemListener((ItemListener) commandListener);
	}

	public void toggleButtons(boolean enable) {

		deleteDataSetButton.setEnabled(enable);
		acceptListButton.setEnabled(enable);
	}
}

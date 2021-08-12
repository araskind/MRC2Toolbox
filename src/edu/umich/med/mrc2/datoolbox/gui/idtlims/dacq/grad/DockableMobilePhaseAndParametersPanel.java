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

package edu.umich.med.mrc2.datoolbox.gui.idtlims.dacq.grad;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;

import javax.swing.Icon;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import edu.umich.med.mrc2.datoolbox.data.lims.ChromatographicGradient;
import edu.umich.med.mrc2.datoolbox.gui.idtlims.dacq.mobph.MobilePhaseTable;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;

public class DockableMobilePhaseAndParametersPanel extends DefaultSingleCDockable implements ActionListener {

	private static final Icon componentIcon = GuiUtils.getIcon("editMobilePhaseIcon", 16);
	private MobilePhaseTable mobilePhaseTable;
	private MobilePhasePanelToolbar toolbar;
	private JFormattedTextField columnTemperatureFormattedTextField;
	
	public DockableMobilePhaseAndParametersPanel() {

		super("DockableMobilePhaseAndParametersPanel", componentIcon, "Mobile phases and parameters", null, Permissions.MIN_MAX_STACK);
		setCloseable(false);
		setLayout(new BorderLayout(0,0));
		toolbar = new MobilePhasePanelToolbar(this);
		add(toolbar, BorderLayout.NORTH);
		
		mobilePhaseTable = new MobilePhaseTable();
		add(new JScrollPane(mobilePhaseTable), BorderLayout.CENTER);
		
		JPanel parametersPanel = new JPanel();
		parametersPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		GridBagLayout gbl_gradientStepPanel = new GridBagLayout();
		gbl_gradientStepPanel.columnWidths = new int[]{0, 0, 0};
		gbl_gradientStepPanel.rowHeights = new int[]{0, 0};
		gbl_gradientStepPanel.columnWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		gbl_gradientStepPanel.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		parametersPanel.setLayout(gbl_gradientStepPanel);
		
		JLabel lblEndTimeMin = new JLabel("Column compartment temperature");
		GridBagConstraints gbc_lblEndTimeMin = new GridBagConstraints();
		gbc_lblEndTimeMin.anchor = GridBagConstraints.EAST;
		gbc_lblEndTimeMin.fill = GridBagConstraints.VERTICAL;
		gbc_lblEndTimeMin.insets = new Insets(0, 0, 0, 5);
		gbc_lblEndTimeMin.gridx = 0;
		gbc_lblEndTimeMin.gridy = 0;
		parametersPanel.add(lblEndTimeMin, gbc_lblEndTimeMin);
		
		add(parametersPanel, BorderLayout.SOUTH);		
		
		columnTemperatureFormattedTextField = new JFormattedTextField(MRC2ToolBoxConfiguration.getPpmFormat());
		columnTemperatureFormattedTextField.setColumns(10);
		GridBagConstraints gbc_columnTemperatureFormattedTextField = new GridBagConstraints();
		gbc_columnTemperatureFormattedTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_columnTemperatureFormattedTextField.gridx = 1;
		gbc_columnTemperatureFormattedTextField.gridy = 0;
		parametersPanel.add(columnTemperatureFormattedTextField, gbc_columnTemperatureFormattedTextField);
	}

	public void clearPanel() {
		mobilePhaseTable.clearTable();
		columnTemperatureFormattedTextField.setText("");
	}
	
	public void loadGradientData(ChromatographicGradient gradient) {
		mobilePhaseTable.setTableModelFromMobilePhaseCollection(Arrays.asList(gradient.getMobilePhases()));
		columnTemperatureFormattedTextField.setText("");
		if(gradient.getColumnCompartmentTemperature() > 0)
			columnTemperatureFormattedTextField.setText(Double.toString(gradient.getColumnCompartmentTemperature()));
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		if(e.getActionCommand().equals(MainActionCommands.ADD_MOBILE_PHASE_DIALOG_COMMAND.getName())) {
			
		}
		if(e.getActionCommand().equals(MainActionCommands.DELETE_MOBILE_PHASE_COMMAND.getName())) {
			
		}		
	}
}
